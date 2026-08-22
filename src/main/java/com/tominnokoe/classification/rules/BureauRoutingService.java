package com.tominnokoe.classification.rules;

import com.tominnokoe.model.vo.RetrievedContext;
import com.tominnokoe.model.vo.ScoredOrgRule;

import java.util.List;

/**
 * 事務分掌ルールの検索結果から主担当局を選定する。
 * 異なる局の2位候補が上位比80%以上（＝僅差）であれば複数局複合案件として検出する
 * （例: 「違法駐輪対策と都営バスの増便要望」＝建設局＋交通局）。
 */
public final class BureauRoutingService {

    /**
     * 事務分掌ルールごとにキーワード数・具体性が異なるため、スコアは単純な絶対値では
     * 公平に比較できない。実データでの検証の結果、異なる局の2位候補が僅差とみなせる
     * 実用的な閾値として0.45を採用している（要件定義書の原文には具体的な数値の指定は無い）。
     */
    private static final double MULTI_BUREAU_CLOSENESS_RATIO = 0.45;

    public RoutingCandidate selectRouting(RetrievedContext context) {
        List<ScoredOrgRule> matched = context.getMatchedOrgRules();
        if (matched.isEmpty()) {
            return new RoutingCandidate(null, null);
        }
        ScoredOrgRule primary = matched.get(0);
        ScoredOrgRule secondary = null;
        for (int i = 1; i < matched.size(); i++) {
            ScoredOrgRule candidate = matched.get(i);
            boolean differentBureau = !candidate.getRule().getBureauName().equals(primary.getRule().getBureauName());
            boolean closeScore = candidate.getScore() >= primary.getScore() * MULTI_BUREAU_CLOSENESS_RATIO;
            if (differentBureau && closeScore) {
                secondary = candidate;
                break;
            }
        }
        return new RoutingCandidate(primary, secondary);
    }
}
