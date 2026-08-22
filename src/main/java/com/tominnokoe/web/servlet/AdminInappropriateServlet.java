package com.tominnokoe.web.servlet;

import com.tominnokoe.classification.ClassificationEngine;
import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.CaseStatus;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.security.CsrfTokenManager;
import com.tominnokoe.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 不適切・規約違反監査ビュー（F-A02）。政策企画局専用の隔離枠。
 * 各局へは通知せず、ここで理由タグの確認と誤検知時の復元のみを行う。
 */
public class AdminInappropriateServlet extends HttpServlet {

    private final ClassificationEngine engine = new ClassificationEngine();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CaseEntity> flagged = new ArrayList<>();
        for (CaseEntity c : CaseRepository.getInstance().findAll()) {
            if (c.getClassification() != null && c.getClassification().isInappropriate()) {
                flagged.add(c);
            }
        }
        request.setAttribute("cases", flagged);
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/inappropriate.jsp").forward(request, response);
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
        ClassificationInput input = new ClassificationInput(entity.getCategory(), entity.getSubject(), entity.getBody());
        ClassificationResult reclassified = engine.classify(input, true); // 不適切判定をスキップして再分類
        entity.setClassification(reclassified);
        entity.setStatus(CaseStatus.NEW);
        CaseRepository.getInstance().update(entity);

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);
        AuditLog.getInstance().record(actor, "RESTORE_FROM_INAPPROPRIATE", caseId, "誤検知として通常フローへ復元");

        response.sendRedirect(request.getContextPath() + "/admin/inappropriate");
    }
}
