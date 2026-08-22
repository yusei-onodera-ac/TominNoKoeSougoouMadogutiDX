package com.tominnokoe.model.vo;

import com.tominnokoe.model.entity.OrgRuleEntity;

/** キーワードマッチのスコア付きで検索された事務分掌ルール。 */
public final class ScoredOrgRule {
    private final OrgRuleEntity rule;
    private final double score;

    public ScoredOrgRule(OrgRuleEntity rule, double score) {
        this.rule = rule;
        this.score = score;
    }

    public OrgRuleEntity getRule() { return rule; }
    public double getScore() { return score; }
}
