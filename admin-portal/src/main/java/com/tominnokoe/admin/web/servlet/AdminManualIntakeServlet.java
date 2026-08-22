package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.classification.ClassificationEngine;
import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.IntakeChannel;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.admin.security.CsrfTokenManager;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

/**
 * 電話・FAX・窓口来訪・手紙・意見箱の統一代筆入力ページ（F-A？追加機能）。
 * 東京都公式サイトの受付フロー図にある6チャネルのうち、都民が直接オンライン入力しない
 * チャネル（{@link IntakeChannel} の WEB_FORM 以外）を、内容を受け取った職員がここから
 * 代筆入力する。入力後は都民向けの自動投稿（{@code /submit}）と全く同じ判定エンジンで
 * 処理される（同一のClassificationEngineを使用）。
 */
public class AdminManualIntakeServlet extends HttpServlet {

    private static final int SUBJECT_MAX_LENGTH = 100;
    private static final int BODY_MAX_LENGTH = 2000;

    private final ClassificationEngine engine = new ClassificationEngine();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/manualIntake.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }

        String channelParam = request.getParameter("intakeChannel");
        String category = trim(request.getParameter("category"));
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));
        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);

        IntakeChannel channel = parseChannel(channelParam);

        if (channel == null || channel == IntakeChannel.WEB_FORM || body.isEmpty() || subject.isEmpty()) {
            request.setAttribute("error", "受付チャネル・件名・本文は必須です。");
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/admin/manualIntake.jsp").forward(request, response);
            return;
        }
        if (subject.length() > SUBJECT_MAX_LENGTH || body.length() > BODY_MAX_LENGTH) {
            request.setAttribute("error", "入力内容が長すぎます（件名" + SUBJECT_MAX_LENGTH + "文字、本文" + BODY_MAX_LENGTH + "文字以内）。");
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/admin/manualIntake.jsp").forward(request, response);
            return;
        }

        ClassificationInput input = new ClassificationInput(category, subject, body);
        ClassificationResult result = engine.classify(input, false);

        CaseEntity entity = new CaseEntity();
        CaseRepository repo = CaseRepository.getInstance();
        entity.setId(repo.nextCaseId());
        entity.setCreatedAt(Instant.now());
        entity.setCategory(category);
        entity.setSubject(subject);
        entity.setBody(body);
        entity.setClassification(result);
        entity.setIntakeChannel(channel);
        entity.setIntakeStaffBureau(actor);
        repo.add(entity);

        AuditLog.getInstance().record(actor, "MANUAL_INTAKE", entity.getId(),
                channel + " による代筆入力（受付番号: " + entity.getId() + "）");

        response.sendRedirect(request.getContextPath() + "/admin/triage");
    }

    private IntakeChannel parseChannel(String value) {
        try {
            return value == null ? null : IntakeChannel.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
