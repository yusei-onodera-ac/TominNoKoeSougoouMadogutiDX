package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.CaseStatus;
import com.tominnokoe.admin.security.CsrfTokenManager;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 総合窓口トリアージダッシュボード（F-A01）。政策企画局向け。
 * 不適切フラグの立った案件はここには表示しない（F-A02の隔離監査ビューへ分離）。
 */
public class AdminTriageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String classificationFilter = request.getParameter("classification");
        String bureauFilter = request.getParameter("bureau");

        List<CaseEntity> all = CaseRepository.getInstance().findAll();
        List<CaseEntity> filtered = new ArrayList<>();
        for (CaseEntity c : all) {
            if (c.getClassification() == null || c.getClassification().isInappropriate()) {
                continue;
            }
            if (classificationFilter != null && !classificationFilter.isBlank()
                    && !classificationFilter.equals(c.getClassification().getClassificationType().name())) {
                continue;
            }
            if (bureauFilter != null && !bureauFilter.isBlank()) {
                String bureau = c.getClassification().getRouting().getPrimaryBureau();
                if (bureau == null || !bureau.contains(bureauFilter)) {
                    continue;
                }
            }
            filtered.add(c);
        }

        request.setAttribute("cases", filtered);
        request.setAttribute("classificationFilter", classificationFilter);
        request.setAttribute("bureauFilter", bureauFilter);
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/triage.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }
        String caseId = request.getParameter("caseId");
        String bureau = request.getParameter("bureau");

        Optional<CaseEntity> found = CaseRepository.getInstance().findById(caseId);
        if (found.isEmpty() || bureau == null || bureau.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです。");
            return;
        }
        CaseEntity entity = found.get();
        entity.setAssignedBureauOverride(bureau);
        entity.setStatus(CaseStatus.ASSIGNED);
        CaseRepository.getInstance().update(entity);

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);
        AuditLog.getInstance().record(actor, "ASSIGN", caseId, "担当局を手動アサイン: " + bureau);

        response.sendRedirect(request.getContextPath() + "/admin/triage");
    }
}
