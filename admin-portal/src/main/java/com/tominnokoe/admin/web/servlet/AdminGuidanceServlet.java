package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.classification.GuidanceTemplates;
import com.tominnokoe.classification.retrieval.RetrievalService;
import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.enums.ClassificationType;
import com.tominnokoe.admin.security.CsrfTokenManager;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 区市町村向け案内状・回答文ドラフト生成画面（F-A04）。
 */
public class AdminGuidanceServlet extends HttpServlet {

    private final RetrievalService retrievalService = new RetrievalService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CaseEntity> cases = new ArrayList<>();
        for (CaseEntity c : CaseRepository.getInstance().findAll()) {
            if (c.getClassification() != null
                    && c.getClassification().getClassificationType() == ClassificationType.JURISDICTION_OTHER) {
                cases.add(c);
            }
        }
        request.setAttribute("cases", cases);
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/guidance.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }
        String caseId = request.getParameter("caseId");
        Optional<CaseEntity> found = CaseRepository.getInstance().findById(caseId);
        if (found.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです。");
            return;
        }
        CaseEntity entity = found.get();
        String text = (entity.getSubject() + "\n" + entity.getBody()).toLowerCase(Locale.JAPAN);
        MunicipalityEntity municipality = retrievalService.matchMunicipality(text);

        int currentVariant = entity.getGuidanceTextOverride() == null ? 0 : 1;
        String regenerated = GuidanceTemplates.render(municipality, currentVariant + 1);
        entity.setGuidanceTextOverride(regenerated);
        CaseRepository.getInstance().update(entity);

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);
        AuditLog.getInstance().record(actor, "GUIDANCE_REGENERATE", caseId, "案内文を再生成");

        response.sendRedirect(request.getContextPath() + "/admin/guidance");
    }
}
