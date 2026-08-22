package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.dao.OrgRuleLookup;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.NotificationStatus;
import com.tominnokoe.notification.NotificationDispatcher;
import com.tominnokoe.notification.NotificationMessage;
import com.tominnokoe.admin.security.BureauScope;
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
 * ガバナンス通知・進捗管理画面（F-A03）。各局・出先事業所向け。
 * 現場部署（Action Owner）だけでなく、上位の局・部へも同時に状況が共有される
 * 階層別ステータス管理を表示する。
 */
public class AdminGovernanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sessionBureau = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);

        List<CaseEntity> cases = new ArrayList<>();
        for (CaseEntity c : CaseRepository.getInstance().findAll()) {
            if (c.getClassification() != null && BureauScope.isVisible(c, sessionBureau)
                    && !c.getClassification().getRouting().getGovernanceNotificationTree().isEmpty()) {
                cases.add(c);
            }
        }
        request.setAttribute("cases", cases);
        request.setAttribute("csrfToken", CsrfTokenManager.getOrCreateToken(request));
        request.getRequestDispatcher("/WEB-INF/views/admin/governance.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenManager.verify(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです（CSRFトークン不一致）。");
            return;
        }
        String caseId = request.getParameter("caseId");
        String departmentName = request.getParameter("departmentName");

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);

        Optional<CaseEntity> found = CaseRepository.getInstance().findById(caseId);
        if (found.isEmpty() || departmentName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです。");
            return;
        }
        CaseEntity entity = found.get();
        if (!BureauScope.isVisible(entity, actor)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "この案件への操作権限がありません。");
            return;
        }
        NotificationStatus current = entity.getNotificationStatuses().getOrDefault(departmentName, NotificationStatus.PENDING);
        NotificationStatus next = nextStatus(current);
        entity.getNotificationStatuses().put(departmentName, next);
        CaseRepository.getInstance().update(entity);

        String logDetails = departmentName + " のステータスを " + current + " → " + next + " に変更";

        // NOTIFIED へ遷移した場合、実際に通知配信（メール/Slack）を試みる
        // （SMTP_HOST/SLACK_WEBHOOK_URL等が未設定の場合は自動でスキップされ、アプリは壊れない）。
        if (next == NotificationStatus.NOTIFIED) {
            String recipientEmail = OrgRuleLookup.findContactEmail(departmentName);
            NotificationMessage message = new NotificationMessage(
                    caseId, departmentName, "実務対応・状況共有", entity.getSubject(), recipientEmail);
            List<String> dispatchResults = NotificationDispatcher.getInstance().dispatch(message);
            logDetails += " / 通知配信結果: " + String.join(", ", dispatchResults);
        }

        AuditLog.getInstance().record(actor, "NOTIFICATION_STATUS_CHANGE", caseId, logDetails);

        response.sendRedirect(request.getContextPath() + "/admin/governance");
    }

    private NotificationStatus nextStatus(NotificationStatus current) {
        return switch (current) {
            case PENDING -> NotificationStatus.NOTIFIED;
            case NOTIFIED -> NotificationStatus.ACKED;
            case ACKED -> NotificationStatus.DONE;
            case DONE -> NotificationStatus.PENDING;
        };
    }
}
