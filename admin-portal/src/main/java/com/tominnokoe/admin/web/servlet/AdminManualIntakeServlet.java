package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.classification.ClassificationEngine;
import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.IntakeChannel;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.ClassificationResult;
import com.tominnokoe.notification.GovernanceNotifier;
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

    // 都民本人が入力するフォーム（citizen-portalのsubmit.jsp）と同じ項目構成・文字数制限に揃える。
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
        String division = trim(request.getParameter("division"));
        String category = trim(request.getParameter("category"));
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));
        String lastName = trim(request.getParameter("lastName"));
        String firstName = trim(request.getParameter("firstName"));
        String address = trim(request.getParameter("address"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);

        IntakeChannel channel = parseChannel(channelParam);

        String error = validate(channel, division, subject, body, lastName, firstName, address, phone, email);
        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
            request.getRequestDispatcher("/WEB-INF/views/admin/manualIntake.jsp").forward(request, response);
            return;
        }

        // タイトルは任意項目（都民向けフォームと同様）。未入力の場合は本文冒頭から自動生成する。
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
        entity.setIntakeChannel(channel);
        entity.setIntakeStaffBureau(actor);

        // 登録と同時に、ガバナンス通知チェーンの全階層（現場出先機関〜局〜総合窓口）へ
        // 一括で自動通知する（都民本人によるWeb投稿と同じ経路・同じ挙動）。
        GovernanceNotifier.notifyAll(entity);

        repo.add(entity);

        AuditLog.getInstance().record(actor, "MANUAL_INTAKE", entity.getId(),
                channel + " による代筆入力（受付番号: " + entity.getId() + "）");

        response.sendRedirect(request.getContextPath() + "/admin/triage");
    }

    private String validate(IntakeChannel channel, String division, String subject, String body,
                             String lastName, String firstName, String address, String phone, String email) {
        if (channel == null || channel == IntakeChannel.WEB_FORM) {
            return "受付チャネルを選択してください。";
        }
        if (division.isEmpty() || (!division.equals("知事への提言") && !division.equals("要望・苦情"))) {
            return "区分を選択してください。";
        }
        if (body.isEmpty()) {
            return "コメント（本文）を入力してください。";
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
