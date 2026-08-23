package com.tominnokoe.admin.web.servlet;

import com.tominnokoe.dao.CaseRepository;
import com.tominnokoe.dao.ExportService;
import com.tominnokoe.model.vo.AggregatedStats;
import com.tominnokoe.viz.ChartRenderer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
        String typeChart = ChartRenderer.renderPieChart("分類タイプ別内訳", translateTypeLabels(stats.getCountByClassificationType()), 420, 240);
        String monthChart = ChartRenderer.renderLineChart("月別件数推移", stats.getCountByMonth(), 480, 220);

        request.setAttribute("stats", stats);
        request.setAttribute("bureauChartSvg", bureauChart);
        request.setAttribute("typeChartSvg", typeChart);
        request.setAttribute("monthChartSvg", monthChart);
        request.getRequestDispatcher("/WEB-INF/views/admin/opendata.jsp").forward(request, response);
    }

    /**
     * 画面表示（グラフの凡例）用に、分類タイプの集計キーを日本語ラベルへ置き換える。
     * CSV/JSON/Excelエクスポート側のキー（ExportService経由）は要件定義書のスキーマに
     * 合わせて英語のenum名のまま維持するため、ここでは表示用のコピーのみを作る。
     */
    private Map<String, Integer> translateTypeLabels(Map<String, Integer> byType) {
        Map<String, Integer> translated = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : byType.entrySet()) {
            translated.put(translateTypeKey(e.getKey()), e.getValue());
        }
        return translated;
    }

    private String translateTypeKey(String key) {
        return switch (key) {
            case "TOKYO_METROPOLITAN" -> "都管轄";
            case "JURISDICTION_OTHER" -> "他管轄（区市町村等）";
            case "UNKNOWN" -> "不明（要確認）";
            case "INAPPROPRIATE" -> "不適切（隔離）";
            default -> key;
        };
    }
}
