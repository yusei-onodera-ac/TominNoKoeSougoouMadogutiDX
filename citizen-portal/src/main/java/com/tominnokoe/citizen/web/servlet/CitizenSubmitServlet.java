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
 * 実際の都民の声総合窓口メールフォームの項目構成（区分・タイトル・コメント・氏名・住所・
 * 電話番号・メールアドレス）に準拠している。連絡先情報（氏名・住所・電話番号・メール
 * アドレス）はすべて任意項目で、分類エンジンには一切渡さない（プライバシー・バイ・デザイン）。
 * GET: フォーム表示。POST: 分類実行＋永続化→受付確認画面へリダイレクト。
 */
public class CitizenSubmitServlet extends HttpServlet {

    private static final int SUBJECT_MAX_LENGTH = 100;
    private static final int BODY_MAX_LENGTH = 1800;
    private static final int NAME_MAX_LENGTH = 64;
    private static final int ADDRESS_MAX_LENGTH = 100;
    private static final int PHONE_MAX_LENGTH = 15;
    private static final int EMAIL_MAX_LENGTH = 50;

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

        String division = trim(request.getParameter("division"));
        String category = trim(request.getParameter("category"));
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));
        String lastName = trim(request.getParameter("lastName"));
        String firstName = trim(request.getParameter("firstName"));
        String address = trim(request.getParameter("address"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));

        String error = validate(division, subject, body, lastName, firstName, address, phone, email);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/submit.jsp").forward(request, response);
            return;
        }

        // タイトルは任意項目（実フォームに準拠）。未入力の場合は本文冒頭から自動生成する
        // （管理画面の一覧表示のため。分類エンジンへはユーザー入力のタイトルではなく
        // この自動生成後の値を渡す点に注意）。
        String effectiveSubject = subject.isEmpty() ? autoSubject(body) : subject;

        ClassificationInput input = new ClassificationInput(category, effectiveSubject, body);
        ClassificationResult result = engine.classify(input, false);

        CaseEntity entity = new CaseEntity();
        CaseRepository repo = CaseRepository.getInstance();
        entity.setId(repo.nextCaseId());
        entity.setCreatedAt(Instant.now());
        entity.setDivision(division);
        entity.setCategory(category);
        entity.setSubject(effectiveSubject);
        entity.setBody(body);
        entity.setSubmitterLastName(emptyToNull(lastName));
        entity.setSubmitterFirstName(emptyToNull(firstName));
        entity.setSubmitterAddress(emptyToNull(address));
        entity.setSubmitterPhone(emptyToNull(phone));
        entity.setSubmitterEmail(emptyToNull(email));
        entity.setClassification(result);
        repo.add(entity);

        response.sendRedirect(request.getContextPath() + "/cases/" + entity.getId());
    }

    private String validate(String division, String subject, String body, String lastName, String firstName,
                             String address, String phone, String email) {
        if (division.isEmpty() || (!division.equals("知事への提言") && !division.equals("要望・苦情"))) {
            return "区分を選択してください。";
        }
        if (body.isEmpty()) {
            return "コメントを入力してください。";
        }
        if (subject.length() > SUBJECT_MAX_LENGTH) {
            return "タイトルが長すぎます（" + SUBJECT_MAX_LENGTH + "文字以内）。";
        }
        if (body.length() > BODY_MAX_LENGTH) {
            return "コメントが長すぎます（" + BODY_MAX_LENGTH + "文字以内）。";
        }
        if (lastName.length() > NAME_MAX_LENGTH || firstName.length() > NAME_MAX_LENGTH) {
            return "氏名が長すぎます。";
        }
        if (address.length() > ADDRESS_MAX_LENGTH) {
            return "住所が長すぎます（" + ADDRESS_MAX_LENGTH + "文字以内）。";
        }
        if (phone.length() > PHONE_MAX_LENGTH) {
            return "電話番号が長すぎます。";
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            return "メールアドレスが長すぎます。";
        }
        return null;
    }

    private String autoSubject(String body) {
        String oneLine = body.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 30 ? oneLine.substring(0, 30) + "…" : oneLine;
    }

    private String emptyToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
