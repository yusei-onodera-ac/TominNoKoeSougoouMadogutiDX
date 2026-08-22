package com.tominnokoe.dao;

import com.tominnokoe.model.entity.OrgRuleEntity;

/**
 * ガバナンス通知ツリーの部署名（department_name）から、事務分掌データの連絡先情報を
 * 逆引きするヘルパー。department_nameは階層によって表記が異なる（SECTION_SITEは
 * actionOwner、DIVISIONは"局名 部名"、BUREAU/HEAD_OFFICEは局名のみ）ため、
 * それぞれのパターンで一致するorg_jurisdiction_rulesの行を探す。
 */
public final class OrgRuleLookup {

    private OrgRuleLookup() {
    }

    /** 一致するcontactEmailを返す。見つからなければnull。 */
    public static String findContactEmail(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            return null;
        }
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            if (departmentName.equals(rule.getActionOwner())) {
                return rule.getContactEmail();
            }
        }
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            String divisionLabel = rule.getBureauName() + " " + rule.getDivisionName();
            if (departmentName.equals(divisionLabel)) {
                return rule.getContactEmail();
            }
        }
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            if (departmentName.equals(rule.getBureauName())) {
                return rule.getContactEmail();
            }
        }
        return null;
    }
}
