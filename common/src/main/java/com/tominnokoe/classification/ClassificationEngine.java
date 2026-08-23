package com.tominnokoe.classification;

import com.tominnokoe.classification.llm.GeminiClient;
import com.tominnokoe.classification.llm.LlmClassificationParser;
import com.tominnokoe.classification.retrieval.RetrievalService;
import com.tominnokoe.classification.rules.BureauRoutingService;
import com.tominnokoe.classification.rules.ConfidenceScorer;
import com.tominnokoe.classification.rules.GovernanceTreeBuilder;
import com.tominnokoe.classification.rules.InappropriateDetector;
import com.tominnokoe.classification.rules.JurisdictionResolver;
import com.tominnokoe.classification.rules.RoutingCandidate;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.model.enums.InappropriateReason;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.model.vo.ExternalGuidance;
import com.tominnokoe.model.vo.GovernanceNode;
import com.tominnokoe.model.vo.RetrievedContext;
import com.tominnokoe.model.vo.RoutingInfo;
import com.tominnokoe.model.vo.ScoredOrgRule;
import com.tominnokoe.model.vo.ScoredPastCase;

import java.util.ArrayList;
import java.util.List;

/**
 * 都民の声 高精度ルーティング・トリアージエンジン。
 *
 * <p><b>ハイブリッド動作: 実LLM（Gemini API）とルールベースのモックを自動切り替え。</b>
 * 環境変数 {@code GEMINI_API_KEY} が設定されていれば実際にGemini APIを呼び出して判定し、
 * 未設定の場合や呼び出し失敗時（ネットワークエラー・レート制限・レスポンス解析失敗等）は
 * RAG検索＋決定論的ルールによるモック判定へ自動フォールバックする。
 * {@link #classify(ClassificationInput, boolean)} のシグネチャと戻り値
 * ({@link ClassificationResult} = 要件定義書1-4CのJSONスキーマそのもの) は両経路で共通のため、
 * 呼び出し側（Servlet群）はどちらの経路が使われているか意識する必要がない。</p>
 */
public final class ClassificationEngine {

    private final RetrievalService retrievalService = new RetrievalService();
    private final InappropriateDetector inappropriateDetector = new InappropriateDetector();
    private final JurisdictionResolver jurisdictionResolver = new JurisdictionResolver();
    private final BureauRoutingService bureauRoutingService = new BureauRoutingService();
    private final ConfidenceScorer confidenceScorer = new ConfidenceScorer();
    private final GovernanceTreeBuilder governanceTreeBuilder = new GovernanceTreeBuilder();
    private final GeminiClient geminiClient = new GeminiClient();

    /**
     * @param input                    都民の入力（申告ジャンル・件名・本文）
     * @param skipInappropriateCheck   不適切判定をスキップするか
     *                                 （F-A02「誤検知として復元」フローで、既に不適切ではないと
     *                                 職員が判断した案件を再分類する際に使用）
     */
    public ClassificationResult classify(ClassificationInput input, boolean skipInappropriateCheck) {
        RetrievedContext context = retrievalService.retrieveContext(input);

        // 誹謗中傷・脅迫等の明白な不適切コンテンツはキーワード事前フィルタで判定する
        // （実LLM呼び出しのコスト削減・多層防御の観点から、安全チェックはここでは常にローカルで行う）。
        if (!skipInappropriateCheck) {
            InappropriateReason reason = inappropriateDetector.detect(input.combinedText());
            if (reason != InappropriateReason.NONE) {
                return new ClassificationResult(
                        true, reason, ClassificationType.UNKNOWN,
                        RoutingInfo.inappropriate(), ExternalGuidance.none(),
                        1.0, evidenceForInappropriate(reason), null);
            }
        }

        // GEMINI_API_KEY が設定されていれば実LLMへ委譲し、失敗時はルールベースのモックへ
        // フォールバックする（呼び出し側のシグネチャ・挙動は変わらない）。
        if (GeminiClient.isConfigured()) {
            try {
                String prompt = PromptTemplate.buildPrompt(input, context);
                String rawJson = geminiClient.generateJson(prompt);
                return LlmClassificationParser.parse(rawJson);
            } catch (Exception e) {
                System.err.println("[ClassificationEngine] Gemini呼び出しに失敗したため、ルールベースの判定にフォールバックします: "
                        + e.getMessage());
            }
        }

        ClassificationType type = jurisdictionResolver.resolve(context);
        RoutingCandidate routing = bureauRoutingService.selectRouting(context);
        double confidence = confidenceScorer.score(input, context, routing);

        if (type == ClassificationType.TOKYO_METROPOLITAN && confidence < ConfidenceScorer.HIGH_THRESHOLD) {
            // 確信度が閾値未満なら、無理に推測せずUNKNOWNへ倒す（ハルシネーション抑止の要）
            String hint = confidence >= ConfidenceScorer.LOW_THRESHOLD && routing.getPrimary() != null
                    ? routing.getPrimary().getRule().getBureauName()
                    : null;
            return new ClassificationResult(
                    false, InappropriateReason.NONE, ClassificationType.UNKNOWN,
                    governanceOnlyRouting(ClassificationType.UNKNOWN, null),
                    ExternalGuidance.none(), confidence,
                    evidenceForUnknown(context), hint);
        }

        return switch (type) {
            case TOKYO_METROPOLITAN -> buildTokyoResult(context, routing, confidence);
            case JURISDICTION_OTHER -> buildJurisdictionOtherResult(context, confidence);
            case UNKNOWN -> new ClassificationResult(
                    false, InappropriateReason.NONE, ClassificationType.UNKNOWN,
                    governanceOnlyRouting(ClassificationType.UNKNOWN, null),
                    ExternalGuidance.none(), confidence, evidenceForUnknown(context), null);
        };
    }

    private ClassificationResult buildTokyoResult(RetrievedContext context, RoutingCandidate routing, double confidence) {
        OrgRuleEntity primaryRule = routing.getPrimary().getRule();
        List<GovernanceNode> tree = governanceTreeBuilder.build(ClassificationType.TOKYO_METROPOLITAN, routing);
        RoutingInfo routingInfo = new RoutingInfo(
                primaryRule.getBureauName(), primaryRule.getDivisionName(), primaryRule.getSectionName(),
                primaryRule.getActionOwner(), tree);

        List<String> evidence = new ArrayList<>();
        evidence.add("org_jurisdiction_rules:" + primaryRule.getId());
        if (routing.isMultiBureau()) {
            evidence.add("org_jurisdiction_rules:" + routing.getSecondary().getRule().getId());
        }
        addEntityEvidence(context, evidence);
        addSimilarCaseEvidence(context, evidence);

        return new ClassificationResult(
                false, InappropriateReason.NONE, ClassificationType.TOKYO_METROPOLITAN,
                routingInfo, ExternalGuidance.none(), confidence, evidence, null);
    }

    private ClassificationResult buildJurisdictionOtherResult(RetrievedContext context, double confidence) {
        MunicipalityEntity municipality = context.getMatchedMunicipality();
        String explanation = GuidanceTemplates.render(municipality, 0);

        ExternalGuidance guidance = new ExternalGuidance(
                municipality == null ? null : municipality.getLocalGovName(),
                explanation,
                municipality == null ? null : municipality.getContactUrl());

        List<String> evidence = new ArrayList<>();
        if (municipality != null) {
            evidence.add("municipalities:" + municipality.getLocalGovCode());
        }
        addSimilarCaseEvidence(context, evidence);

        return new ClassificationResult(
                false, InappropriateReason.NONE, ClassificationType.JURISDICTION_OTHER,
                RoutingInfo.none(), guidance, confidence, evidence, null);
    }

    private RoutingInfo governanceOnlyRouting(ClassificationType type, RoutingCandidate routing) {
        List<GovernanceNode> tree = governanceTreeBuilder.build(type, routing);
        return new RoutingInfo(null, null, null, null, tree);
    }

    private void addEntityEvidence(RetrievedContext context, List<String> evidence) {
        if (context.getMatchedFacility() != null) {
            evidence.add("tokyo_facilities:" + context.getMatchedFacility().getId());
        }
        if (context.getMatchedRoad() != null) {
            evidence.add("tokyo_roads:" + context.getMatchedRoad().getId());
        }
    }

    private void addSimilarCaseEvidence(RetrievedContext context, List<String> evidence) {
        for (ScoredPastCase sc : context.getSimilarCases()) {
            evidence.add(String.format("opinions_past_cases:%s(score=%.2f)", sc.getPastCase().getId(), sc.getScore()));
        }
    }

    private List<String> evidenceForUnknown(RetrievedContext context) {
        List<String> evidence = new ArrayList<>();
        addSimilarCaseEvidence(context, evidence);
        if (evidence.isEmpty()) {
            evidence.add("(該当する根拠データなし。総合窓口による手動トリアージが必要)");
        }
        return evidence;
    }

    private List<String> evidenceForInappropriate(InappropriateReason reason) {
        return List.of("safety_policy_rules:" + reason.name());
    }
}
