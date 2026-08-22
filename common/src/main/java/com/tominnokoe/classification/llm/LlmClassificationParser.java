package com.tominnokoe.classification.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tominnokoe.dao.DatasetLoader;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.model.enums.GovernanceLevel;
import com.tominnokoe.model.enums.InappropriateReason;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.model.vo.ExternalGuidance;
import com.tominnokoe.model.vo.GovernanceNode;
import com.tominnokoe.model.vo.RoutingInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Geminiが返したJSON文字列を {@link ClassificationResult} へ変換するパーサ。
 *
 * <p><b>ハルシネーション防止のポストホック検証（要件定義書のKPI「誤判定率0%」の
 * アーキテクチャ上の保証）をここで実施する。</b>
 * classification_type が TOKYO_METROPOLITAN の場合、LLMが出力した primary_bureau が
 * {@code org_jurisdiction_rules.json}（事務分掌データ）に実在する局名と一致するかを検証し、
 * 一致しない場合は存在しない部署名の捏造とみなして機械的にUNKNOWNへ強制フォールバックする。</p>
 */
public final class LlmClassificationParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LlmClassificationParser() {
    }

    public static ClassificationResult parse(String rawJson) {
        JsonNode root;
        try {
            root = MAPPER.readTree(rawJson);
        } catch (Exception e) {
            throw new RuntimeException("Geminiの応答をJSONとして解析できませんでした: " + e.getMessage(), e);
        }

        boolean isInappropriate = root.path("is_inappropriate").asBoolean(false);
        InappropriateReason inappropriateReason = parseEnum(
                root.path("inappropriate_reason").asText(null), InappropriateReason.class, InappropriateReason.NONE);
        ClassificationType classificationType = parseEnum(
                root.path("classification_type").asText(null), ClassificationType.class, ClassificationType.UNKNOWN);
        double confidenceScore = root.path("confidence_score").asDouble(0.0);

        List<String> evidenceSources = new ArrayList<>();
        root.path("evidence_sources").forEach(n -> evidenceSources.add(n.asText()));

        RoutingInfo routing = parseRouting(root.path("routing"));
        ExternalGuidance guidance = parseGuidance(root.path("external_guidance"));

        // --- ハルシネーション防止のポストホック検証 ---
        if (!isInappropriate && classificationType == ClassificationType.TOKYO_METROPOLITAN) {
            if (!bureauExistsInOfficialRules(routing.getPrimaryBureau())) {
                evidenceSources.add("(LLMが実在しない部署名「" + routing.getPrimaryBureau()
                        + "」を出力したため、システムが機械的にUNKNOWNへフォールバックしました)");
                return new ClassificationResult(
                        false, InappropriateReason.NONE, ClassificationType.UNKNOWN,
                        new RoutingInfo("UNKNOWN", null, null, null, List.of(
                                new GovernanceNode(GovernanceLevel.HEAD_OFFICE, "政策企画局", "手動トリアージ・LLM出力検証エラー対応"))),
                        ExternalGuidance.none(), Math.min(confidenceScore, 0.5), evidenceSources, null);
            }
        }

        if (isInappropriate) {
            routing = RoutingInfo.inappropriate();
        }

        return new ClassificationResult(
                isInappropriate, inappropriateReason, classificationType, routing, guidance,
                confidenceScore, evidenceSources, null);
    }

    private static boolean bureauExistsInOfficialRules(String bureauName) {
        if (bureauName == null || bureauName.isBlank()) {
            return false;
        }
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            if (bureauName.equals(rule.getBureauName())) {
                return true;
            }
        }
        return false;
    }

    private static RoutingInfo parseRouting(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return RoutingInfo.none();
        }
        String primaryBureau = textOrNull(node, "primary_bureau");
        String primaryDivision = textOrNull(node, "primary_division");
        String primarySection = textOrNull(node, "primary_section");
        String actionOwner = textOrNull(node, "action_owner");

        List<GovernanceNode> tree = new ArrayList<>();
        for (JsonNode n : node.path("governance_notification_tree")) {
            GovernanceLevel level = parseEnum(n.path("level").asText(null), GovernanceLevel.class, GovernanceLevel.SECTION_SITE);
            tree.add(new GovernanceNode(level, textOrNull(n, "department_name"), textOrNull(n, "purpose")));
        }
        return new RoutingInfo(primaryBureau, primaryDivision, primarySection, actionOwner, tree);
    }

    private static ExternalGuidance parseGuidance(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return ExternalGuidance.none();
        }
        return new ExternalGuidance(textOrNull(node, "target_entity"), textOrNull(node, "explanation_text"),
                textOrNull(node, "contact_url"));
    }

    private static String textOrNull(JsonNode parent, String field) {
        JsonNode n = parent.path(field);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> type, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
