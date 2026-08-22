package com.tominnokoe.web.servlet;

import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * 都民向け受付確認画面（F-C04）。
 * 不適切フラグの有無は都民には見せない（管理画面専用の情報）。
 */
public class CaseDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // "/C-2026-0001"
        String caseId = pathInfo == null ? "" : pathInfo.replaceFirst("^/", "");

        Optional<CaseEntity> found = CaseRepository.getInstance().findById(caseId);
        if (found.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "該当する案件が見つかりません。");
            return;
        }

        request.setAttribute("caseEntity", found.get());
        request.getRequestDispatcher("/WEB-INF/views/caseConfirmation.jsp").forward(request, response);
    }
}
