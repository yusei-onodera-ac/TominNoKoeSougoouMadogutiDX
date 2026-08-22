package com.tominnokoe.notification;

/**
 * ガバナンス通知チェーンの1ノードへの通知内容。
 * {@code recipientEmail} が null の場合、メールチャネルはスキップされる
 * （事務分掌データに連絡先が無い行等）。
 */
public final class NotificationMessage {

    private final String caseId;
    private final String departmentName;
    private final String purpose;
    private final String caseSubject;
    private final String recipientEmail;

    public NotificationMessage(String caseId, String departmentName, String purpose,
                                String caseSubject, String recipientEmail) {
        this.caseId = caseId;
        this.departmentName = departmentName;
        this.purpose = purpose;
        this.caseSubject = caseSubject;
        this.recipientEmail = recipientEmail;
    }

    public String getCaseId() { return caseId; }
    public String getDepartmentName() { return departmentName; }
    public String getPurpose() { return purpose; }
    public String getCaseSubject() { return caseSubject; }
    public String getRecipientEmail() { return recipientEmail; }

    public String subjectLine() {
        return "【都民の声】案件 " + caseId + " のご連絡（" + departmentName + "）";
    }

    public String bodyText() {
        return """
                %s 様

                都民の声プラットフォームより、案件のご連絡です。

                案件番号: %s
                件名: %s
                通知先部署: %s
                目的: %s

                詳細は管理画面（/admin/triage）でご確認ください。
                本メールはシステムからの自動送信です。
                """.formatted(departmentName, caseId, caseSubject, departmentName, purpose);
    }
}
