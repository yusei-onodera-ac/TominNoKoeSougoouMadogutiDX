package com.tominnokoe.model.vo;

/** 管轄外（区市町村等）案件向けの案内情報。要件定義書1-4C external_guidance に対応。 */
public final class ExternalGuidance {

    private final String targetEntity;
    private final String explanationText;
    private final String contactUrl;

    public ExternalGuidance(String targetEntity, String explanationText, String contactUrl) {
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
