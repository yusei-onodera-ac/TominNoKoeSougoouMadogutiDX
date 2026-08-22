package com.tominnokoe.model.vo;

/**
 * 分類エンジンへの入力（Value Object）。要件定義書1-4Aの
 * USER_SELECTED_CATEGORY / USER_SUBMITTED_SUBJECT / USER_SUBMITTED_BODY に対応。
 * 不変オブジェクトとして扱う。
 */
public final class ClassificationInput {

    private final String category;
    private final String subject;
    private final String body;

    public ClassificationInput(String category, String subject, String body) {
        this.category = category;
        this.subject = subject == null ? "" : subject;
        this.body = body == null ? "" : body;
    }

    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }

    /** 分類ロジックが検索対象とする結合テキスト（件名＋本文）。 */
    public String combinedText() {
        return subject + "\n" + body;
    }
}
