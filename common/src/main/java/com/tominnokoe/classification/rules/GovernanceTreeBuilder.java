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

    /** 現場出先機関を特定できなかった場合に総合窓口へ送る目的文言（「あなたたちで対応してください」側）。 */
    private static final String UNIDENTIFIED_PURPOSE =
            "現場出先機関を特定できませんでした。総合窓口（政策企画局）で内容を確認し、担当局・担当課の判断と一次対応をお願いします。";

    public List<GovernanceNode> build(ClassificationType type, RoutingCandidate routing) {
        if (type == ClassificationType.UNKNOWN) {
            return List.of(new GovernanceNode(GovernanceLevel.HEAD_OFFICE, HEAD_OFFICE_NAME, UNIDENTIFIED_PURPOSE));
        }
        if (type == ClassificationType.JURISDICTION_OTHER) {
            return Collections.emptyList();
        }
        if (routing == null || routing.getPrimary() == null) {
            return List.of(new GovernanceNode(GovernanceLevel.HEAD_OFFICE, HEAD_OFFICE_NAME, UNIDENTIFIED_PURPOSE));
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

    /**
     * 現場出先機関（Action Owner）が特定できた場合の通知チェーン。
     * 実際に対応するのは現場出先機関のみ。それ以外（部・局）への通知は、対応を求めるものではなく
     * 「この内容の声が現場出先機関に届いた」という状況共有のお知らせに徹する文言にする。
     */
    private void appendBureauChain(List<GovernanceNode> nodes, OrgRuleEntity rule, String sitePurpose) {
        String actionOwner = rule.getActionOwner();
        String fyiPurpose = "「" + actionOwner + "」にこの内容の声が届きました（お知らせのみ・対応不要）。";
        nodes.add(new GovernanceNode(GovernanceLevel.SECTION_SITE, actionOwner, sitePurpose));
        nodes.add(new GovernanceNode(GovernanceLevel.DIVISION, rule.getBureauName() + " " + rule.getDivisionName(), fyiPurpose));
        nodes.add(new GovernanceNode(GovernanceLevel.BUREAU, rule.getBureauName(), fyiPurpose));
    }
}
