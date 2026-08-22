package com.tominnokoe.classification.rules;

import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.RetrievedContext;
import com.tominnokoe.model.vo.ScoredPastCase;

import java.util.List;
import java.util.Locale;

/**
 * confidence_score のヒューリスティック算出。
 * 都有施設・都道の名指しヒットは（住所同定に近い強い証拠のため）単独で高confidenceとなる。
 * named entityの無い案件（福祉・上下水道・教育等、施設/道路台帳が存在しないカテゴリ）でも、
 * 事務分掌キーワードに複数ヒットすれば確信度を積み上げられるようにする
 * （キーワード一致数ベースの加点。1件一致で暫定ルーティング相当、3件以上一致でほぼ確定）。
 *
 * HIGH({@value #HIGH_THRESHOLD})/LOW({@value #LOW_THRESHOLD}) 閾値の意味づけ
 * （要件定義書の原文では曖昧だったため、本実装で採用した解釈）:
 * <ul>
 *   <li>score &gt;= HIGH: 自動ルーティングを確定する（TOKYO_METROPOLITAN として局を確定）</li>
 *   <li>LOW &lt;= score &lt; HIGH: classification_type は UNKNOWN に倒すが、
 *       管理画面には「推定局ヒント」を補助表示する（都民向けの確定ルーティングには使わない）</li>
 *   <li>score &lt; LOW: ヒントも出さず、単純にUNKNOWNとして総合窓口の手動トリアージに委ねる</li>
 * </ul>
 */
public final class ConfidenceScorer {

    public static final double HIGH_THRESHOLD = 0.85;
    public static final double LOW_THRESHOLD = 0.70;

    private static final double ENTITY_MATCH_SCORE = 0.92;
    private static final double KEYWORD_BASE_SCORE = 0.55;
    private static final double KEYWORD_PER_MATCH_BONUS = 0.15;
    private static final int KEYWORD_MATCH_CAP = 3;
    private static final double CATEGORY_ALIGN_BONUS = 0.05;
    private static final double SIMILAR_CASE_BUREAU_BONUS = 0.05;

    public double score(ClassificationInput input, RetrievedContext context, RoutingCandidate routing) {
        double score = 0.0;

        if (context.hasTokyoEntityMatch()) {
            score = ENTITY_MATCH_SCORE;
        } else if (routing != null && routing.getPrimary() != null) {
            OrgRuleEntity primaryRule = routing.getPrimary().getRule();
            int matched = countMatchedKeywords(input.combinedText(), primaryRule.getKeywords());
            score = KEYWORD_BASE_SCORE + KEYWORD_PER_MATCH_BONUS * Math.min(matched, KEYWORD_MATCH_CAP);

            if (input.getCategory() != null && input.getCategory().equals(primaryRule.getCategoryHint())) {
                score += CATEGORY_ALIGN_BONUS;
            }
        }

        if (routing != null && routing.getPrimary() != null && !context.getSimilarCases().isEmpty()) {
            String primaryBureau = routing.getPrimary().getRule().getBureauName();
            for (ScoredPastCase similar : context.getSimilarCases()) {
                if (primaryBureau.equals(similar.getPastCase().getHandledBureau())) {
                    score += SIMILAR_CASE_BUREAU_BONUS;
                    break;
                }
            }
        }

        return Math.max(0.0, Math.min(score, 1.0));
    }

    private int countMatchedKeywords(String combinedText, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }
        String normalizedText = combinedText.toLowerCase(Locale.JAPAN);
        int count = 0;
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && normalizedText.contains(keyword.toLowerCase(Locale.JAPAN))) {
                count++;
            }
        }
        return count;
    }
}
