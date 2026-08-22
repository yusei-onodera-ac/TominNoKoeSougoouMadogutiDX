package com.tominnokoe.security;

import com.tominnokoe.dao.DatasetLoader;
import com.tominnokoe.model.entity.OrgRuleEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 局ごとのログインアカウント。
 * 事務分掌データ（{@code org_jurisdiction_rules.json}）に登場する全ての局が、
 * それぞれ独立したログインアカウントとして管理画面にログインできる
 * （＝実際の局数だけ職員アカウントがある、という体裁のデモ）。
 *
 * デモ用の簡易実装として全アカウント共通のパスワードを使う
 * （{@link AdminCredentials} と同じソルト付きハッシュを流用）。
 * 本番では局ごと・職員ごとに個別の認証情報が必要（改訂版要件定義書に明記）。
 */
public final class BureauAccountRegistry {

    /** 政策企画局は「都民の声総合窓口」として全案件を横断的に閲覧できる特別なアカウント。 */
    public static final String GENERAL_DESK_BUREAU = "政策企画局";

    private BureauAccountRegistry() {
    }

    /** ログイン画面のプルダウンに表示する、全局名の一覧（政策企画局を先頭に）。 */
    public static List<String> allBureauNames() {
        Set<String> names = new LinkedHashSet<>();
        names.add(GENERAL_DESK_BUREAU);
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            names.add(rule.getBureauName());
        }
        return names.stream().toList();
    }

    public static boolean isGeneralDesk(String bureauName) {
        return GENERAL_DESK_BUREAU.equals(bureauName);
    }

    public static boolean verify(String bureauName, String password) {
        if (bureauName == null || !allBureauNames().contains(bureauName)) {
            return false;
        }
        return AdminCredentials.verifySharedDemoPassword(password);
    }
}
