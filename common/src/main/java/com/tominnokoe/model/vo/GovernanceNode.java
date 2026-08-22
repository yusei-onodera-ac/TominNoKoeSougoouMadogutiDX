package com.tominnokoe.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tominnokoe.model.enums.GovernanceLevel;

/** ガバナンス通知チェーンの1ノード。要件定義書1-4C routing.governance_notification_tree の要素に対応。 */
public final class GovernanceNode {

    private final GovernanceLevel level;
    private final String departmentName;
    private final String purpose;

    @JsonCreator
    public GovernanceNode(@JsonProperty("level") GovernanceLevel level,
                           @JsonProperty("departmentName") String departmentName,
                           @JsonProperty("purpose") String purpose) {
        this.level = level;
        this.departmentName = departmentName;
        this.purpose = purpose;
    }

    public GovernanceLevel getLevel() { return level; }
    public String getDepartmentName() { return departmentName; }
    public String getPurpose() { return purpose; }
}
