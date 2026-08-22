package com.tominnokoe.classification.rules;

import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.model.vo.RetrievedContext;

/**
 * 管轄判定ロジック。
 * (a) 都有施設・都道にヒット → TOKYO_METROPOLITAN
 * (b) 区市町村名にヒットし、かつ都有施設・都道の該当なし → JURISDICTION_OTHER
 * (c) 事務分掌ルールのキーワード重複が閾値未満（かつエンティティ一致も無い） → UNKNOWN
 * (d) 具体的なエンティティ一致は無いが事務分掌ルールのキーワード重複がある → TOKYO_METROPOLITAN（局ルーティングのみで判定）
 */
public final class JurisdictionResolver {

    /** これ未満の事務分掌スコアで、かつエンティティ一致も無ければ判定不能とみなす。 */
    private static final double MIN_ORG_RULE_SCORE = 0.15;

    public ClassificationType resolve(RetrievedContext context) {
        // (a) 都有施設・都道への名指しヒットは最も強い証拠 → 無条件でTOKYO_METROPOLITAN
        if (context.hasTokyoEntityMatch()) {
            return ClassificationType.TOKYO_METROPOLITAN;
        }
        // (b) 都有施設・都道のヒットが無く区市町村名にヒットした場合は、事務分掌キーワードが
        //     多少ヒットしていても（例:「公園」等の一般語）区市町村管轄を優先する。
        //     一般語のキーワード一致だけでは「都立」であることの証拠にならないため。
        if (context.getMatchedMunicipality() != null) {
            return ClassificationType.JURISDICTION_OTHER;
        }
        // (c)/(d) 具体的なエンティティ一致も区市町村名一致も無い場合、事務分掌キーワードの
        //     重複度で都管轄かどうかを判定する。
        boolean hasOrgRuleSignal = !context.getMatchedOrgRules().isEmpty()
                && context.getMatchedOrgRules().get(0).getScore() >= MIN_ORG_RULE_SCORE;
        return hasOrgRuleSignal ? ClassificationType.TOKYO_METROPOLITAN : ClassificationType.UNKNOWN;
    }
}
