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

    /** セッションの局が、この案件を閲覧・操作できるかどうか。 */
    public static boolean isVisible(CaseEntity entity, String sessionBureau) {
        if (sessionBureau == null) {
            return false;
        }
        if (BureauAccountRegistry.isGeneralDesk(sessionBureau)) {
            return true;
        }
        ClassificationResult classification = entity.getClassification();
        if (classification == null || classification.isInappropriate()) {
            return false; // 不適切キューは政策企画局専用
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
