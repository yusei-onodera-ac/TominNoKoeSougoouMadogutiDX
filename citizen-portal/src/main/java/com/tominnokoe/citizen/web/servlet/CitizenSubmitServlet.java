package com.tominnokoe.citizen.web.servlet;

import com.tominnokoe.classification.ClassificationEngine;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.citizen.security.CsrfTokenManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

/**
 * 都民向け意見投稿フォーム（F-C01/F-C04）。
 * GET: フォーム表示。POST: 分類実行＋永続化→受付確認画面へリダイレクト。
 */
public class CitizenSubmitServlet extends HttpServlet {

    private static final int SUBJECT_MAX_LENGTH = 100;
    private static final int BODY_MAX_LENGTH = 2000;

    private final ClassificationEngine engine = new ClassificationEngine();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/submit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }

        String category = trim(request.getParameter("category"));
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));

        if (body.isEmpty() || subject.isEmpty()) {
            request.setAttribute("error", "件名と本文は必須です。");
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/submit.jsp").forward(request, response);
            return;
        }
        if (subject.length() > SUBJECT_MAX_LENGTH || body.length() > BODY_MAX_LENGTH) {
            request.setAttribute("error", "入力内容が長すぎます（件名" + SUBJECT_MAX_LENGTH + "文字、本文" + BODY_MAX_LENGTH + "文字以内）。");
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/submit.jsp").forward(request, response);
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
        repo.add(entity);

        response.sendRedirect(request.getContextPath() + "/cases/" + entity.getId());
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
