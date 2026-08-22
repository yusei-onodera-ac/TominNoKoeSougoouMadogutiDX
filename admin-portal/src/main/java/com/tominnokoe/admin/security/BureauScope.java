package com.tominnokoe.admin.security;

import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.model.vo.GovernanceNode;
import com.tominnokoe.security.BureauAccountRegistry;

/**
 * 局アカウントのRBAC（アクセス範囲制御）。
 * 政策企画局（総合窓口）は全案件を横断的に閲覧できるが、それ以外の局アカウントは
 * 自局が主担当（またはガバナンス通知ツリーに登場する関連局）の案件のみ閲覧できる。
 */
public final class BureauScope {

    private BureauScope() {
    }

    /**
     * セッションの局が、この案件を（通常のトリアージ・ガバナンス通知画面で）閲覧・操作できるかどうか。
     * 不適切フラグの立った案件は、政策企画局（総合窓口）であっても常にfalseを返す
     * — 隔離監査ビュー（{@code /admin/inappropriate}）専用の別クエリで扱うため、
     * 通常のトリアージ一覧に混入させない（F-A02の隔離設計を局横断アカウントでも徹底する）。
     */
    public static boolean isVisible(CaseEntity entity, String sessionBureau) {
        if (sessionBureau == null) {
            return false;
        }
        ClassificationResult classification = entity.getClassification();
        if (classification == null || classification.isInappropriate()) {
            return false;
        }
        if (BureauAccountRegistry.isGeneralDesk(sessionBureau)) {
            return true;
        }

        String primaryBureau = entity.getAssignedBureauOverride() != null
                ? entity.getAssignedBureauOverride()
                : classification.getRouting().getPrimaryBureau();
        if (sessionBureau.equals(primaryBureau)) {
            return true;
        }
        // 複数局複合案件: ガバナンス通知ツリーに自局が関連局として含まれていれば閲覧可
        for (GovernanceNode node : classification.getRouting().getGovernanceNotificationTree()) {
            if (node.getDepartmentName() != null && node.getDepartmentName().startsWith(sessionBureau)) {
                return true;
            }
        }
        return false;
    }
}
