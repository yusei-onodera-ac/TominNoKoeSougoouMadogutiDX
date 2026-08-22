package com.tominnokoe.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 管理者パスワードのソルト付きハッシュ化・検証。
 * <p><b>これはハッカソン用プロトタイプの簡易実装である。</b>
 * SHA-256は本来パスワードハッシュ用の適応的関数（BCrypt/Argon2等）ではなく、
 * 本番運用に移行する際は必ずBCrypt等へ置き換えることを改訂版要件定義書にも明記している。
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String randomSaltBase64() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltBase64) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(saltBase64));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean verify(String password, String saltBase64, String expectedHashBase64) {
        String actual = hash(password, saltBase64);
        // タイミング攻撃対策として定数時間比較を使用
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expectedHashBase64.getBytes(StandardCharsets.UTF_8));
    }
}
