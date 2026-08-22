package com.tominnokoe.model.vo;

import java.util.Collections;
import java.util.List;

/**
 * ルーティング結果。要件定義書1-4C routing に対応。
 * is_inappropriate=true の場合は primaryBureau に {@code INAPPROPRIATE_QUEUE} という
 * センチネル値を入れる（元スキーマに不適切キュー用の分類が無いための暫定対応。
 * 改訂版要件定義書 docs/requirements-improved.ja.md にこの設計判断を明記している）。
 */
public final class RoutingInfo {

    public static final String INAPPROPRIATE_QUEUE_SENTINEL = "INAPPROPRIATE_QUEUE";

    private final String primaryBureau;
    private final String primaryDivision;
    private final String primarySection;
    private final String actionOwner;
    private final List<GovernanceNode> governanceNotificationTree;

    public RoutingInfo(String primaryBureau, String primaryDivision, String primarySection,
                        String actionOwner, List<GovernanceNode> governanceNotificationTree) {
        this.primaryBureau = primaryBureau;
        this.primaryDivision = primaryDivision;
        this.primarySection = primarySection;
        this.actionOwner = actionOwner;
        this.governanceNotificationTree = governanceNotificationTree == null
                ? Collections.emptyList() : governanceNotificationTree;
    }

    public static RoutingInfo unknown() {
        return new RoutingInfo("UNKNOWN", null, null, null, Collections.emptyList());
    }

    public static RoutingInfo inappropriate() {
        return new RoutingInfo(INAPPROPRIATE_QUEUE_SENTINEL, null, null, null, Collections.emptyList());
    }

    public static RoutingInfo none() {
        return new RoutingInfo(null, null, null, null, Collections.emptyList());
    }

    public String getPrimaryBureau() { return primaryBureau; }
    public String getPrimaryDivision() { return primaryDivision; }
    public String getPrimarySection() { return primarySection; }
    public String getActionOwner() { return actionOwner; }
    public List<GovernanceNode> getGovernanceNotificationTree() { return governanceNotificationTree; }
}
