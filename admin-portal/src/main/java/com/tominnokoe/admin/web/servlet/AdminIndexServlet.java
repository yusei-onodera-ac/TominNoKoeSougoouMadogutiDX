package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.security.BureauAccountRegistry;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** 管理画面ダッシュボード（よく使うメニューをパネルボタンで、総合窓口専用機能を折りたたみ表示） */
public class AdminIndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sessionBureau = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);
        request.setAttribute("isGeneralDesk", BureauAccountRegistry.isGeneralDesk(sessionBureau));
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
