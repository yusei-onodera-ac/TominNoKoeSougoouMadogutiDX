package com.tominnokoe.admin.web.filter;

import com.tominnokoe.security.BureauAccountRegistry;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 政策企画局（都民の声総合窓口）専用の機能（不適切監査ビュー・区市町村案内文・
 * オープンデータ公表）を、それ以外の局アカウントから遮断する。
 * {@link AdminAuthFilter} の後段で動作する想定（未ログインは既にリダイレクト済み）。
 */
public class GeneralDeskOnlyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        String bureau = session == null ? null : (String) session.getAttribute(AdminAuthFilter.SESSION_KEY);

        if (bureau == null || !BureauAccountRegistry.isGeneralDesk(bureau)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "この機能は政策企画局（都民の声総合窓口）専用です。");
            return;
        }
        chain.doFilter(req, res);
    }
}
