package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** ログアウト（局アカウントの切り替えデモをしやすくするため）。 */
public class AdminLogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String bureau = (String) session.getAttribute(AdminAuthFilter.SESSION_KEY);
            AuditLog.getInstance().record(bureau, "LOGOUT", null, "管理画面からログアウト");
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/admin/login");
    }
}
