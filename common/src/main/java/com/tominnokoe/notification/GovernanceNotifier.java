package com.tominnokoe.notification;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.OrgRuleLookup;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.NotificationStatus;
import com.tominnokoe.model.vo.GovernanceNode;

import java.util.List;

/**
 * 案件登録時に、ガバナンス通知チェーンの全階層（現場出先機関〜局〜総合窓口）へ
 * 同時に自動で通知する。
 *
 * 従来は管理画面（/admin/governance）で階層ごとに職員が手動でボタンを押して
 * 「次のステータスへ」進める設計だったが、「都民や職員が声を登録した時点で
 * 自動的に管轄へ送信されるべきで、そこに手動ボタンは要らない」との指摘を受けて、
 * 案件作成（分類確定）の直後にこのクラスを呼び出す方式に変更した。
 * 都民自身の投稿（citizen-portal）・職員による代筆入力（admin-portal）のどちらの
 * 経路でも同じ挙動になるよう、通知処理をこの1箇所に集約する。
 *
 * ここで自動化するのはあくまで「通知の送信（NOTIFIED）」までであり、通知を受けた
 * 部署が実際に内容を確認した（ACKED）・対応を完了した（DONE）は、引き続き
 * 管理画面（/admin/governance）から各部署の職員が手動で記録する。
 */
public final class GovernanceNotifier {

    /** 監査ログ上、職員操作と区別するための自動処理の行為者名。 */
    public static final String SYSTEM_ACTOR = "SYSTEM（登録時自動通知）";

    private GovernanceNotifier() {
    }

    /**
     * 案件の分類結果からガバナンス通知ツリーを取得し、その全ノードへ同時に通知する。
     * 各ノードの通知ステータスを NOTIFIED にした上で、実際の通知配信（メール/Slack）を
     * 試み、監査ログに記録する。分類が未確定・通知対象なし（JURISDICTION_OTHER等）の
     * 場合は何もしない。
     */
    public static void notifyAll(CaseEntity entity) {
        if (entity.getClassification() == null || entity.getClassification().getRouting() == null) {
            return;
        }
        List<GovernanceNode> tree = entity.getClassification().getRouting().getGovernanceNotificationTree();
        for (GovernanceNode node : tree) {
            entity.getNotificationStatuses().put(node.getDepartmentName(), NotificationStatus.NOTIFIED);

            String recipientEmail = OrgRuleLookup.findContactEmail(node.getDepartmentName());
            NotificationMessage message = new NotificationMessage(
                    entity.getId(), node.getDepartmentName(), node.getPurpose(), entity.getSubject(), recipientEmail);
            List<String> dispatchResults = NotificationDispatcher.getInstance().dispatch(message);

            AuditLog.getInstance().record(SYSTEM_ACTOR, "NOTIFICATION_STATUS_CHANGE", entity.getId(),
                    node.getDepartmentName() + "（" + node.getLevel() + "）へ登録時に自動通知（目的: " + node.getPurpose()
                            + "） / 通知配信結果: " + String.join(", ", dispatchResults));
        }
    }
}
