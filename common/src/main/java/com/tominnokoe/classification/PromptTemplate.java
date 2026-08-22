package com.tominnokoe.classification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tominnokoe.classification.rules.ConfidenceScorer;
import com.tominnokoe.model.entity.FacilityEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.entity.RoadEntity;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.RetrievedContext;
import com.tominnokoe.model.vo.ScoredOrgRule;
import com.tominnokoe.model.vo.ScoredPastCase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 要件定義書1-4Bのシステムプロンプトと、実LLM（{@link com.tominnokoe.classification.llm.GeminiClient}）へ
 * 渡す変数バインディングを組み立てるクラス。
 *
 * 変数マッピング（{@link ClassificationInput} / {@link RetrievedContext} → プロンプト変数）:
 * <pre>
 *   {{USER_SELECTED_CATEGORY}}   ← ClassificationInput#getCategory()
 *   {{USER_SUBMITTED_SUBJECT}}   ← ClassificationInput#getSubject()
 *   {{USER_SUBMITTED_BODY}}      ← ClassificationInput#getBody()
 *   {{RAG_OFFICIAL_RULES}}       ← RetrievedContext#getMatchedOrgRules()
 *   {{RAG_SIMILAR_CASES}}        ← RetrievedContext#getSimilarCases()
 *   {{RAG_MATCHED_ENTITIES}}     ← RetrievedContext#getMatchedFacility() / getMatchedRoad()
 *   {{RAG_MUNICIPALITY_INFOS}}   ← RetrievedContext#getMatchedMunicipality()
 *   {{CONFIDENCE_THRESHOLD_HIGH}} ← ConfidenceScorer#HIGH_THRESHOLD (0.85)
 *   {{CONFIDENCE_THRESHOLD_LOW}}  ← ConfidenceScorer#LOW_THRESHOLD (0.70)
 * </pre>
 *
 * 実LLMへの差し替えは {@link ClassificationEngine#classify} の内部実装のみで完結する
 * （{@code GEMINI_API_KEY} 環境変数が設定されていれば本クラスでプロンプトを組み立てて
 * {@link com.tominnokoe.classification.llm.GeminiClient} を呼び出し、未設定またはAPI呼び出し失敗時は
 * ルールベースのモックへフォールバックする）。呼び出し元（Servlet群）は一切変更不要。
 *
 * なお、誹謗中傷・営業スパム等の明白な不適切コンテンツは {@link com.tominnokoe.classification.rules.InappropriateDetector}
 * によるキーワード事前フィルタで既に除外されているため、本プロンプトの安全チェックは
 * それをすり抜けた微妙なケースに対する多層防御（defense in depth）として位置づける。
 */
public final class PromptTemplate {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PromptTemplate() {
    }

    public static final String SYSTEM_PROMPT_TEMPLATE = """
            # Role Definition
            あなたは東京都庁の広聴業務を支援する「都民の声 高精度ルーティング・トリアージエンジン」です。
            提供されたシステム変数およびオープンデータコンテキストのみを根拠として判定を行い、指定のJSONスキーマで出力してください。

            # System Constraints & Policies
            1. ハルシネーション（組織名の捏造）の完全禁止:
               担当組織名は必ず [公式事務分掌ルール] に存在する公式部署名のみを使用してください。
               存在しない部署名を出力した場合、システム側でポストホックに検出しUNKNOWNへ強制的に倒します。
            2. 安全・規約チェック:
               誹謗中傷・脅迫・営利目的の宣伝等の違反が本文に含まれる場合は、is_inappropriate = true とし、
               inappropriate_reason を DEFAMATION / THREAT / COMMERCIAL_SPAM / OUT_OF_SCOPE のいずれかにしてください。
            3. ユーザー申告ジャンルの活用:
               都民が申告したジャンル「%s」を事前ヒント（Prior）として考慮しつつ、本文との整合性を検証してください。
            4. 確信度閾値による決定論的ルーティング:
               confidence_score >= %.2f の場合のみ、局・部・課の自動割り振りを確定してください（classification_type = TOKYO_METROPOLITAN）。
               それ未満、または管轄の絞り込みが困難な場合は、無理に推測せず classification_type を "UNKNOWN" としてください。
            5. 既存行政フローの遵守（都外管轄の案内）:
               都の管轄外（区市町村）と判定された場合でも除外せず、[関係区市町村情報] を参照して
               都民への丁寧な案内文（external_guidance.explanation_text）を生成してください。
            6. 複数局にまたがる案件:
               本文が複数の異なる局の所掌にまたがる場合、governance_notification_tree に両局の通知チェーンを含め、
               政策企画局を調整役として追加してください。

            # Input Data
            [申告ジャンル]: %s
            [件名]: %s
            [本文]: %s

            # Dynamic Contexts (Open Data, RAGで検索済み)
            [公式事務分掌ルール]: %s
            [過去の類似対応実績]: %s
            [検出された都有施設・道路]: %s
            [関係区市町村情報]: %s

            指定のJSONスキーマに厳密に従い、JSON以外の文字列は一切出力しないでください。
            """;

    /**
     * {@link ClassificationInput} と {@link RetrievedContext} からプロンプト全文を組み立てる。
     */
    public static String buildPrompt(ClassificationInput input, RetrievedContext context) {
        return SYSTEM_PROMPT_TEMPLATE.formatted(
                nullToDash(input.getCategory()),
                ConfidenceScorer.HIGH_THRESHOLD,
                nullToDash(input.getCategory()),
                input.getSubject(),
                input.getBody(),
                toJson(summarizeOrgRules(context.getMatchedOrgRules())),
                toJson(summarizeSimilarCases(context.getSimilarCases())),
                toJson(summarizeEntities(context.getMatchedFacility(), context.getMatchedRoad())),
                toJson(summarizeMunicipality(context.getMatchedMunicipality())));
    }

    private static List<Map<String, Object>> summarizeOrgRules(List<ScoredOrgRule> rules) {
        return rules.stream().limit(6).map(r -> {
            OrgRuleEntity rule = r.getRule();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rule.getId());
            m.put("bureauName", rule.getBureauName());
            m.put("divisionName", rule.getDivisionName());
            m.put("sectionName", rule.getSectionName());
            m.put("actionOwner", rule.getActionOwner());
            m.put("keywords", rule.getKeywords());
            m.put("retrievalScore", r.getScore());
            return m;
        }).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> summarizeSimilarCases(List<ScoredPastCase> cases) {
        return cases.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getPastCase().getId());
            m.put("subject", c.getPastCase().getSubject());
            m.put("handledBureau", c.getPastCase().getHandledBureau());
            m.put("responseSummary", c.getPastCase().getResponseSummary());
            m.put("score", c.getScore());
            return m;
        }).collect(Collectors.toList());
    }

    private static Map<String, Object> summarizeEntities(FacilityEntity facility, RoadEntity road) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (facility != null) {
            m.put("facilityName", facility.getFacilityName());
            m.put("managingBureau", facility.getManagingBureau());
        }
        if (road != null) {
            m.put("routeName", road.getRouteName());
            m.put("managingOffice", road.getManagingOffice());
        }
        return m;
    }

    private static Map<String, Object> summarizeMunicipality(MunicipalityEntity municipality) {
        if (municipality == null) {
            return Map.of();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("localGovName", municipality.getLocalGovName());
        m.put("consultationDesk", municipality.getConsultationDesk());
        m.put("contactUrl", municipality.getContactUrl());
        return m;
    }

    private static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "（未選択）" : s;
    }
}
