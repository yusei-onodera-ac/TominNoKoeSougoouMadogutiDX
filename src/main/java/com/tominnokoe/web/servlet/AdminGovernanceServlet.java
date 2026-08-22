package com.tominnokoe.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.enums.NotificationStatus;
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
 * ガバナンス通知・進捗管理画面（F-A03）。各局・出先事業所向け。
 * 現場部署（Action Owner）だけでなく、上位の局・部へも同時に状況が共有される
 * 階層別ステータス管理を表示する。
 */
public class AdminGovernanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CaseEntity> cases = new ArrayList<>();
        for (CaseEntity c : CaseRepository.getInstance().findAll()) {
            if (c.getClassification() != null && !c.getClassification().isInappropriate()
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

        Optional<CaseEntity> found = CaseRepository.getInstance().findById(caseId);
        if (found.isEmpty() || departmentName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不正なリクエストです。");
            return;
        }
        CaseEntity entity = found.get();
        NotificationStatus current = entity.getNotificationStatuses().getOrDefault(departmentName, NotificationStatus.PENDING);
        NotificationStatus next = nextStatus(current);
        entity.getNotificationStatuses().put(departmentName, next);
        CaseRepository.getInstance().update(entity);

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);
        AuditLog.getInstance().record(actor, "NOTIFICATION_STATUS_CHANGE", caseId,
                departmentName + " のステータスを " + current + " → " + next + " に変更");

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
