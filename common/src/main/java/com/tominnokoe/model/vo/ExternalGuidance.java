package com.tominnokoe.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 管轄外（区市町村等）案件向けの案内情報。要件定義書1-4C external_guidance に対応。 */
public final class ExternalGuidance {

    private final String targetEntity;
    private final String explanationText;
    private final String contactUrl;

    @JsonCreator
    public ExternalGuidance(@JsonProperty("targetEntity") String targetEntity,
                             @JsonProperty("explanationText") String explanationText,
                             @JsonProperty("contactUrl") String contactUrl) {
        this.targetEntity = targetEntity;
        this.explanationText = explanationText;
        this.contactUrl = contactUrl;
    }

    public static ExternalGuidance none() {
        return new ExternalGuidance(null, null, null);
    }

    public String getTargetEntity() { return targetEntity; }
    public String getExplanationText() { return explanationText; }
    public String getContactUrl() { return contactUrl; }
}
