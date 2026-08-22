package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.security.BureauAccountRegistry;
import com.tominnokoe.admin.security.CsrfTokenManager;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 管理系画面の入口。局を選択してログインする（局ごとに独立したアカウント、F-A01/F-A03の
 * RBACの基礎になる）。デモ用に全局共通パスワードを使う（{@link BureauAccountRegistry}参照）。
 */
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("bureauNames", BureauAccountRegistry.allBureauNames());
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

        String bureau = request.getParameter("bureau");
        String password = request.getParameter("password");

        if (BureauAccountRegistry.verify(bureau, password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute(AdminAuthFilter.SESSION_KEY, bureau);
            session.setMaxInactiveInterval(30 * 60);
            AuditLog.getInstance().record(bureau, "LOGIN", null, "管理画面へログイン");
            response.sendRedirect(request.getContextPath() + "/admin/triage");
            return;
        }

        request.setAttribute("error", "局の選択またはパスワードが正しくありません。");
        request.setAttribute("bureauNames", BureauAccountRegistry.allBureauNames());
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }
}
