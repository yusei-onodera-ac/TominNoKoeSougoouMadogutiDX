package com.tominnokoe.model.vo;

import com.tominnokoe.model.enums.GovernanceLevel;

/** ガバナンス通知チェーンの1ノード。要件定義書1-4C routing.governance_notification_tree の要素に対応。 */
public final class GovernanceNode {

    private final GovernanceLevel level;
    private final String departmentName;
    private final String purpose;

    public GovernanceNode(GovernanceLevel level, String departmentName, String purpose) {
        this.level = level;
        this.departmentName = departmentName;
        this.purpose = purpose;
    }

    public GovernanceLevel getLevel() { return level; }
    public String getDepartmentName() { return departmentName; }
    public String getPurpose() { return purpose; }
}
