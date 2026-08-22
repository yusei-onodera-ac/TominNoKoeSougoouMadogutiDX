package com.tominnokoe.citizen.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * セッション単位のCSRFトークン発行・検証。
 * 状態変更を伴う全POST（案件投稿、手動アサイン、不適切復元、通知ステータス切替、案内文再生成）で使用する。
 */
public final class CsrfTokenManager {

    private static final String SESSION_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokenManager() {
    }

    public static String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    /** リクエストパラメータ {@code csrfToken} の値をセッション内トークンと照合する。 */
    public static boolean verify(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_KEY);
        String submitted = request.getParameter("csrfToken");
        return expected != null && expected.equals(submitted);
    }
}
