package com.tominnokoe.security;

/**
 * デモ用の単一管理者アカウント。
 * <b>本番では都のSSO/LDAP等の認証基盤への置き換えが前提。</b>
 * パスワードは平文では保持せず、ソルト付きSHA-256ハッシュのみを保持する
 * （{@link PasswordHasher} 参照）。
 *
 * デモ用ログイン情報: ユーザー名 {@code admin} / パスワード {@code tominnokoe2026}
 * （README.md にも記載）。
 */
public final class AdminCredentials {

    public static final String USERNAME = "admin";
    private static final String SALT_BASE64 = "uG43gdLazjmlG0/FFnZWew==";
    private static final String HASH_BASE64 = "KaBrebgIkpOX1ZktJ72s/wuQGDKOE/edABNazh6sA0s=";

    private AdminCredentials() {
    }

    public static boolean verify(String username, String password) {
        if (!USERNAME.equals(username) || password == null) {
            return false;
        }
        return PasswordHasher.verify(password, SALT_BASE64, HASH_BASE64);
    }
}
