package com.tominnokoe.classification.rules;

import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.model.enums.GovernanceLevel;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.vo.GovernanceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ガバナンス通知チェーン（governance_notification_tree）の構築。
 * 現場出先機関（Action Owner）を特定した場合でも、上位の局・部・総合窓口へ
 * 同時に情報が共有される階層連動型ワークフローを表現する。
 */
public final class GovernanceTreeBuilder {

    public static final String HEAD_OFFICE_NAME = "政策企画局";

    public List<GovernanceNode> build(ClassificationType type, RoutingCandidate routing) {
        if (type == ClassificationType.UNKNOWN) {
            return List.of(new GovernanceNode(GovernanceLevel.HEAD_OFFICE, HEAD_OFFICE_NAME,
                    "手動トリアージ・分類不能案件対応"));
        }
        if (type == ClassificationType.JURISDICTION_OTHER) {
            return Collections.emptyList();
        }
        if (routing == null || routing.getPrimary() == null) {
            return List.of(new GovernanceNode(GovernanceLevel.HEAD_OFFICE, HEAD_OFFICE_NAME,
                    "手動トリアージ・分類不能案件対応"));
        }

        List<GovernanceNode> nodes = new ArrayList<>();
        appendBureauChain(nodes, routing.getPrimary().getRule(), "実務対応（現場出先機関が一次対応）");

        if (routing.isMultiBureau()) {
            appendBureauChain(nodes, routing.getSecondary().getRule(), "関連案件として共有");
            nodes.add(new GovernanceNode(GovernanceLevel.HEAD_OFFICE, HEAD_OFFICE_NAME,
                    "複数局にまたがる案件の調整"));
        }
        return nodes;
    }

    private void appendBureauChain(List<GovernanceNode> nodes, OrgRuleEntity rule, String sitePurpose) {
        nodes.add(new GovernanceNode(GovernanceLevel.SECTION_SITE, rule.getActionOwner(), sitePurpose));
        nodes.add(new GovernanceNode(GovernanceLevel.DIVISION, rule.getBureauName() + " " + rule.getDivisionName(),
                "上位部署への状況共有（CC）"));
        nodes.add(new GovernanceNode(GovernanceLevel.BUREAU, rule.getBureauName(),
                "局全体としての把握・進捗管理"));
    }
}
