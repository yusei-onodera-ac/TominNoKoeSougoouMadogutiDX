package com.tominnokoe.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 都民投稿エンドポイント（{@code /submit}）向けの簡易レート制限（IPアドレス単位）。
 * メモリ内トークンバケットによる連投・スパムのデモレベルでの抑止。
 * 本番ではWAF/APIゲートウェイでの対策が前提（改訂版要件定義書に明記）。
 */
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, k -> new Bucket());
            if (!bucket.tryConsume()) {
                response.sendError(429, "投稿回数の上限を超えました。しばらく待ってから再度お試しください。");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private static final class Bucket {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MILLIS) {
                windowStart = now;
                count = 0;
            }
            if (count >= MAX_REQUESTS_PER_WINDOW) {
                return false;
            }
            count++;
            return true;
        }
    }
}
