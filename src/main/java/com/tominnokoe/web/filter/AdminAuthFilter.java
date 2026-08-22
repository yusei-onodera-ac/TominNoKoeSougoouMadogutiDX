package com.tominnokoe.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * {@code /admin/*} への未認証アクセスを {@code /admin/login} へリダイレクトする。
 * ログイン画面自体（{@code /admin/login}）はこのフィルタの対象から除外する。
 */
public class AdminAuthFilter implements Filter {

    public static final String SESSION_KEY = "adminUser";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        boolean isLoginPage = uri.equals(contextPath + "/admin/login");

        if (isLoginPage) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean authenticated = session != null && session.getAttribute(SESSION_KEY) != null;

        if (!authenticated) {
            response.sendRedirect(contextPath + "/admin/login");
            return;
        }
        chain.doFilter(req, res);
    }
}
