package com.tominnokoe.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.model.enums.InappropriateReason;

import java.util.Collections;
import java.util.List;

/**
 * 分類エンジンの出力。要件定義書1-4C（JSON Schema）と1対1対応するValue Object。
 * {@link com.tominnokoe.classification.ClassificationEngine#classify} の戻り値そのもの。
 *
 * 元スキーマのままだと is_inappropriate と classification_type が直交しており
 * 「不適切かつ分類不能」のようなケースを表現しづらいという穴があるため、
 * この実装では is_inappropriate=true の場合 classificationType には便宜上 UNKNOWN を入れ、
 * ルーティングは {@link RoutingInfo#inappropriate()} のセンチネルで判別する方式を取る
 * （改訂版要件定義書にこの設計判断を明記）。
 */
public final class ClassificationResult {

    private final boolean isInappropriate;
    private final InappropriateReason inappropriateReason;
    private final ClassificationType classificationType;
    private final RoutingInfo routing;
    private final ExternalGuidance externalGuidance;
    private final double confidenceScore;
    private final List<String> evidenceSources;

    /**
     * 元のJSONスキーマには無い、管理画面専用の追加フィールド。
     * confidence が LOW〜HIGH の間でUNKNOWNに倒された場合でも、職員のトリアージの
     * 出発点として「もっとも近いと思われる局」のヒントを表示するためのもの。
     * 都民向けの確定ルーティングとしては絶対に使わない（あくまで補助情報）。
     */
    private final String suggestedBureauHint;

    @JsonCreator
    public ClassificationResult(@JsonProperty("inappropriate") boolean isInappropriate,
                                 @JsonProperty("inappropriateReason") InappropriateReason inappropriateReason,
                                 @JsonProperty("classificationType") ClassificationType classificationType,
                                 @JsonProperty("routing") RoutingInfo routing,
                                 @JsonProperty("externalGuidance") ExternalGuidance externalGuidance,
                                 @JsonProperty("confidenceScore") double confidenceScore,
                                 @JsonProperty("evidenceSources") List<String> evidenceSources,
                                 @JsonProperty("suggestedBureauHint") String suggestedBureauHint) {
        this.isInappropriate = isInappropriate;
        this.inappropriateReason = inappropriateReason;
        this.classificationType = classificationType;
        this.routing = routing;
        this.externalGuidance = externalGuidance;
        this.confidenceScore = confidenceScore;
        this.evidenceSources = evidenceSources == null ? Collections.emptyList() : evidenceSources;
        this.suggestedBureauHint = suggestedBureauHint;
    }

    public boolean isInappropriate() { return isInappropriate; }
    public InappropriateReason getInappropriateReason() { return inappropriateReason; }
    public ClassificationType getClassificationType() { return classificationType; }
    public RoutingInfo getRouting() { return routing; }
    public ExternalGuidance getExternalGuidance() { return externalGuidance; }
    public double getConfidenceScore() { return confidenceScore; }
    public List<String> getEvidenceSources() { return evidenceSources; }
    public String getSuggestedBureauHint() { return suggestedBureauHint; }
}
