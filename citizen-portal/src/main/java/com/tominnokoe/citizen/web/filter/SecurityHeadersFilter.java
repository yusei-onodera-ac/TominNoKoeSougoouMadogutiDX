package com.tominnokoe.citizen.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 全レスポンスへセキュリティヘッダを付与する。
 * CSPは外部CDN・外部スクリプトを一切使わない方針（グラフはサーバ側SVG生成）と整合させ、
 * {@code default-src 'self'} を基本とする。
 */
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; script-src 'self'");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");

        String path = request.getRequestURI();
        if (path != null && (path.startsWith(request.getContextPath() + "/admin"))) {
            response.setHeader("Cache-Control", "no-store");
        }

        chain.doFilter(req, res);
    }
}
