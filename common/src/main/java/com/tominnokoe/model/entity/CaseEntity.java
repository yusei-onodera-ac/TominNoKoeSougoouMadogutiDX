package com.tominnokoe.model.entity;

import com.tominnokoe.model.enums.CaseStatus;
import com.tominnokoe.model.enums.IntakeChannel;
import com.tominnokoe.model.enums.NotificationStatus;
import com.tominnokoe.model.vo.ClassificationResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 都民から投稿された1件の案件（可変データ）。{@code data/cases.json} に永続化される。
 * 分類結果（{@link ClassificationResult}）はエンジンの出力をそのまま埋め込み、
 * 以降の管理画面での操作（アサイン・通知ステータス変更・案内文再生成）で
 * このエンティティ自体を更新していく。
 */
public class CaseEntity {

    private String id;
    private Instant createdAt;
    private String category;
    private String subject;
    private String body;
    /** 区分（実際の都民の声総合窓口フォームに準拠）: "知事への提言" または "要望・苦情"。 */
    private String division;

    /**
     * 都民の任意入力による連絡先情報（すべて任意項目、実際の都民の声総合窓口メールフォームに
     * 準拠）。分類エンジン（RAG・LLM）には一切渡さない — プライバシー・バイ・デザインとして、
     * 判定ロジックはあくまで件名・本文のみを根拠とする。オープンデータ出力・監査ログ・
     * 案件一覧（トリアージ画面の簡易表示）にも含めず、案件詳細（回答画面）でのみ表示する。
     */
    private String submitterLastName;
    private String submitterFirstName;
    private String submitterAddress;
    private String submitterPhone;
    private String submitterEmail;

    private ClassificationResult classification;
    private CaseStatus status = CaseStatus.NEW;

    /** 受付チャネル（WEB_FORMのみ都民本人による完全自動投稿、他は職員代筆入力）。 */
    private IntakeChannel intakeChannel = IntakeChannel.WEB_FORM;
    /** 代筆入力した局（WEB_FORM以外の場合のみ設定）。 */
    private String intakeStaffBureau;

    /** 総合窓口職員が手動で上書きした担当局（UNKNOWN案件のトリアージ結果など）。 */
    private String assignedBureauOverride;

    /** ガバナンス通知ツリーの各ノード（department_name をキーにする）の状態。 */
    private Map<String, NotificationStatus> notificationStatuses = new LinkedHashMap<>();

    /** 案内文（external_guidance.explanation_text）の再生成・編集後の内容。未編集ならnull。 */
    private String guidanceTextOverride;

    /** 担当局職員が入力した回答文（既存の行政の回答フローに相当）。未回答ならnull。 */
    private String responseText;
    /** 回答を入力した局（セッションの局名）。 */
    private String respondedBy;
    private Instant respondedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getSubmitterLastName() { return submitterLastName; }
    public void setSubmitterLastName(String submitterLastName) { this.submitterLastName = submitterLastName; }

    public String getSubmitterFirstName() { return submitterFirstName; }
    public void setSubmitterFirstName(String submitterFirstName) { this.submitterFirstName = submitterFirstName; }

    public String getSubmitterAddress() { return submitterAddress; }
    public void setSubmitterAddress(String submitterAddress) { this.submitterAddress = submitterAddress; }

    public String getSubmitterPhone() { return submitterPhone; }
    public void setSubmitterPhone(String submitterPhone) { this.submitterPhone = submitterPhone; }

    public String getSubmitterEmail() { return submitterEmail; }
    public void setSubmitterEmail(String submitterEmail) { this.submitterEmail = submitterEmail; }

    public ClassificationResult getClassification() { return classification; }
    public void setClassification(ClassificationResult classification) { this.classification = classification; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) { this.status = status; }

    public IntakeChannel getIntakeChannel() { return intakeChannel; }
    public void setIntakeChannel(IntakeChannel intakeChannel) { this.intakeChannel = intakeChannel; }

    public String getIntakeStaffBureau() { return intakeStaffBureau; }
    public void setIntakeStaffBureau(String intakeStaffBureau) { this.intakeStaffBureau = intakeStaffBureau; }

    public String getAssignedBureauOverride() { return assignedBureauOverride; }
    public void setAssignedBureauOverride(String assignedBureauOverride) { this.assignedBureauOverride = assignedBureauOverride; }

    public Map<String, NotificationStatus> getNotificationStatuses() { return notificationStatuses; }
    public void setNotificationStatuses(Map<String, NotificationStatus> notificationStatuses) { this.notificationStatuses = notificationStatuses; }

    public String getGuidanceTextOverride() { return guidanceTextOverride; }
    public void setGuidanceTextOverride(String guidanceTextOverride) { this.guidanceTextOverride = guidanceTextOverride; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public String getRespondedBy() { return respondedBy; }
    public void setRespondedBy(String respondedBy) { this.respondedBy = respondedBy; }

    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
}
