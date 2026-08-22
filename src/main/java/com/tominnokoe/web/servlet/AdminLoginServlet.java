package com.tominnokoe.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.security.AdminCredentials;
import com.tominnokoe.security.CsrfTokenManager;
import com.tominnokoe.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 管理系画面の入口。デモ用単一アカウントによるセッションベース認証（セキュリティ設計を参照）。
 */
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (AdminCredentials.verify(username, password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(AdminAuthFilter.SESSION_KEY, username);
            session.setMaxInactiveInterval(30 * 60);
            AuditLog.getInstance().record(username, "LOGIN", null, "管理画面へログイン");
            response.sendRedirect(request.getContextPath() + "/admin/triage");
            return;
        }

        request.setAttribute("error", "ユーザー名またはパスワードが正しくありません。");
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }
}
