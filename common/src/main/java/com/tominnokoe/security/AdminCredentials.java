package com.tominnokoe.security;

/**
 * デモ用の共有パスワード管理。
 * <b>本番では都のSSO/LDAP等の認証基盤への置き換えが前提。</b>
 * パスワードは平文では保持せず、ソルト付きSHA-256ハッシュのみを保持する
 * （{@link PasswordHasher} 参照）。
 *
 * 全30局分のログインアカウント（{@link BureauAccountRegistry}）が、デモ用にこの
 * 共有パスワードを使う。デモ用パスワード: {@code tominnokoe2026}（README.md にも記載）。
 */
public final class AdminCredentials {

    private static final String SALT_BASE64 = "uG43gdLazjmlG0/FFnZWew==";
    private static final String HASH_BASE64 = "KaBrebgIkpOX1ZktJ72s/wuQGDKOE/edABNazh6sA0s=";

    private AdminCredentials() {
    }

    public static boolean verifySharedDemoPassword(String password) {
        if (password == null) {
            return false;
        }
        return PasswordHasher.verify(password, SALT_BASE64, HASH_BASE64);
    }
}
