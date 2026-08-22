package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.AuditLog;
import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.dao.ExportService;
import com.tominnokoe.model.vo.AggregatedStats;
import com.tominnokoe.admin.web.filter.AdminAuthFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 匿名化集計のダウンロード出力（F-A05拡張）。{@code ?format=csv|json|xlsx}
 */
public class AdminOpenDataExportServlet extends HttpServlet {

    private final ExportService exportService = new ExportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String format = request.getParameter("format");
        if (format == null) {
            format = "json";
        }
        AggregatedStats stats = exportService.buildAggregatedStats(CaseRepository.getInstance().findAll());

        String actor = (String) request.getSession().getAttribute(AdminAuthFilter.SESSION_KEY);

        switch (format) {
            case "csv" -> {
                response.setContentType("text/csv;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"opendata_summary.csv\"");
                exportService.writeCsv(stats, response.getOutputStream());
            }
            case "xlsx" -> {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=\"opendata_summary.xlsx\"");
                exportService.writeXlsx(stats, response.getOutputStream());
            }
            case "json" -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"opendata_summary.json\"");
                exportService.writeJson(stats, response.getOutputStream());
            }
            default -> {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不明な出力形式です: " + format);
                return;
            }
        }

        AuditLog.getInstance().record(actor, "OPEN_DATA_EXPORT", null, "オープンデータ出力（" + format + "）を実行");
    }
}
