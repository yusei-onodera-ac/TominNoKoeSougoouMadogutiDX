package com.tominnokoe.classification.rules;

import com.tominnokoe.model.vo.ScoredOrgRule;

/** {@link BureauRoutingService} の判定結果。複数局複合案件の場合は secondary が入る。 */
public final class RoutingCandidate {

    private final ScoredOrgRule primary;
    private final ScoredOrgRule secondary;

    public RoutingCandidate(ScoredOrgRule primary, ScoredOrgRule secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public ScoredOrgRule getPrimary() { return primary; }
    public ScoredOrgRule getSecondary() { return secondary; }
    public boolean isMultiBureau() { return secondary != null; }
}
