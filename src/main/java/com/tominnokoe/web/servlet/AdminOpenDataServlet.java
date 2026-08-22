package com.tominnokoe.web.servlet;

import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.dao.ExportService;
import com.tominnokoe.model.vo.AggregatedStats;
import com.tominnokoe.viz.ChartRenderer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * オープンデータ可視化ページ（F-A05拡張）。
 * 匿名化集計（生の意見本文・氏名・連絡先を含まない）をグラフ表示し、CSV/JSON/Excelの
 * ダウンロード導線も提供する。
 */
public class AdminOpenDataServlet extends HttpServlet {

    private final ExportService exportService = new ExportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AggregatedStats stats = exportService.buildAggregatedStats(CaseRepository.getInstance().findAll());

        String bureauChart = ChartRenderer.renderBarChart("局別件数（都管轄案件）", stats.getCountByBureau(), 480, 260);
        String typeChart = ChartRenderer.renderPieChart("分類タイプ別内訳", stats.getCountByClassificationType(), 420, 240);
        String monthChart = ChartRenderer.renderLineChart("月別件数推移", stats.getCountByMonth(), 480, 220);

        request.setAttribute("stats", stats);
        request.setAttribute("bureauChartSvg", bureauChart);
        request.setAttribute("typeChartSvg", typeChart);
        request.setAttribute("monthChartSvg", monthChart);
        request.getRequestDispatcher("/WEB-INF/views/admin/opendata.jsp").forward(request, response);
    }
}
