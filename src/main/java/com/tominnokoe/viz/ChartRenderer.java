package com.tominnokoe.viz;

import java.util.List;
import java.util.Map;

/**
 * 集計値からSVGグラフを直接組み立てるユーティリティ。
 * 外部JSライブラリ・CDNは一切使わない（セキュリティ設計のCSP方針 default-src 'self' と整合させるため）。
 * ラベル文字列はXMLエスケープした上で埋め込む（XSS対策）。
 */
public final class ChartRenderer {

    private static final String[] PALETTE = {
            "#3b6fd6", "#e08a2b", "#4caf7d", "#c14f4f", "#8a5fc2", "#4fa8c1", "#c1954f", "#7d7d7d"
    };

    private ChartRenderer() {
    }

    public static String renderBarChart(String title, Map<String, Integer> data, int width, int height) {
        if (data.isEmpty()) {
            return emptyChart(title, width, height);
        }
        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int marginLeft = 140;
        int marginTop = 40;
        int marginBottom = 20;
        int chartWidth = width - marginLeft - 20;
        int chartHeight = height - marginTop - marginBottom;
        int barGap = 8;
        int barHeight = Math.max(10, (chartHeight / Math.max(1, data.size())) - barGap);

        StringBuilder svg = new StringBuilder();
        svg.append(svgOpen(width, height));
        svg.append(titleText(title, width));

        int i = 0;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            int y = marginTop + i * (barHeight + barGap);
            double barWidth = max == 0 ? 0 : (double) e.getValue() / max * chartWidth;
            String color = PALETTE[i % PALETTE.length];
            svg.append(String.format(
                    "<text x=\"%d\" y=\"%d\" font-size=\"12\" text-anchor=\"end\" fill=\"currentColor\">%s</text>",
                    marginLeft - 8, y + barHeight - 4, escapeXml(truncate(e.getKey(), 16))));
            svg.append(String.format(
                    "<rect x=\"%d\" y=\"%d\" width=\"%.1f\" height=\"%d\" fill=\"%s\" rx=\"3\"/>",
                    marginLeft, y, barWidth, barHeight, color));
            svg.append(String.format(
                    "<text x=\"%.1f\" y=\"%d\" font-size=\"12\" fill=\"currentColor\">%d</text>",
                    marginLeft + barWidth + 6, y + barHeight - 4, e.getValue()));
            i++;
        }
        svg.append("</svg>");
        return svg.toString();
    }

    public static String renderPieChart(String title, Map<String, Integer> data, int width, int height) {
        if (data.isEmpty() || data.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            return emptyChart(title, width, height);
        }
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        double cx = width / 3.0;
        double cy = height / 2.0 + 10;
        double r = Math.min(cx, cy) - 20;

        StringBuilder svg = new StringBuilder();
        svg.append(svgOpen(width, height));
        svg.append(titleText(title, width));

        double startAngle = -90;
        int i = 0;
        int legendY = 40;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            double fraction = (double) e.getValue() / total;
            double sweep = fraction * 360;
            String color = PALETTE[i % PALETTE.length];
            svg.append(pieSlicePath(cx, cy, r, startAngle, startAngle + sweep, color));
            startAngle += sweep;

            int legendX = (int) (width * 0.62);
            svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"10\" height=\"10\" fill=\"%s\"/>",
                    legendX, legendY, color));
            svg.append(String.format(
                    "<text x=\"%d\" y=\"%d\" font-size=\"12\" fill=\"currentColor\">%s (%d)</text>",
                    legendX + 16, legendY + 9, escapeXml(truncate(e.getKey(), 18)), e.getValue()));
            legendY += 18;
            i++;
        }
        svg.append("</svg>");
        return svg.toString();
    }

    public static String renderLineChart(String title, Map<String, Integer> orderedData, int width, int height) {
        if (orderedData.isEmpty()) {
            return emptyChart(title, width, height);
        }
        List<String> keys = List.copyOf(orderedData.keySet());
        int max = orderedData.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int marginLeft = 40;
        int marginRight = 20;
        int marginTop = 40;
        int marginBottom = 30;
        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;

        StringBuilder svg = new StringBuilder();
        svg.append(svgOpen(width, height));
        svg.append(titleText(title, width));

        StringBuilder points = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            double x = marginLeft + (keys.size() == 1 ? 0 : (double) i / (keys.size() - 1) * chartWidth);
            int value = orderedData.get(keys.get(i));
            double y = marginTop + chartHeight - (max == 0 ? 0 : (double) value / max * chartHeight);
            points.append(String.format("%.1f,%.1f ", x, y));

            svg.append(String.format("<circle cx=\"%.1f\" cy=\"%.1f\" r=\"3\" fill=\"%s\"/>", x, y, PALETTE[0]));
            svg.append(String.format(
                    "<text x=\"%.1f\" y=\"%d\" font-size=\"10\" text-anchor=\"middle\" fill=\"currentColor\">%s</text>",
                    x, height - 8, escapeXml(keys.get(i))));
        }
        svg.append(String.format("<polyline points=\"%s\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\"/>",
                points.toString().trim(), PALETTE[0]));
        svg.append("</svg>");
        return svg.toString();
    }

    private static String pieSlicePath(double cx, double cy, double r, double startDeg, double endDeg, String color) {
        double startRad = Math.toRadians(startDeg);
        double endRad = Math.toRadians(endDeg);
        double x1 = cx + r * Math.cos(startRad);
        double y1 = cy + r * Math.sin(startRad);
        double x2 = cx + r * Math.cos(endRad);
        double y2 = cy + r * Math.sin(endRad);
        int largeArc = (endDeg - startDeg) > 180 ? 1 : 0;
        return String.format(
                "<path d=\"M%.1f,%.1f L%.1f,%.1f A%.1f,%.1f 0 %d 1 %.1f,%.1f Z\" fill=\"%s\" stroke=\"var(--bg,#fff)\" stroke-width=\"1\"/>",
                cx, cy, x1, y1, r, r, largeArc, x2, y2, color);
    }

    private static String svgOpen(int width, int height) {
        return String.format(
                "<svg viewBox=\"0 0 %d %d\" width=\"100%%\" height=\"auto\" xmlns=\"http://www.w3.org/2000/svg\" role=\"img\">",
                width, height);
    }

    private static String titleText(String title, int width) {
        return String.format(
                "<text x=\"%d\" y=\"20\" font-size=\"14\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"currentColor\">%s</text>",
                width / 2, escapeXml(title));
    }

    private static String emptyChart(String title, int width, int height) {
        StringBuilder svg = new StringBuilder();
        svg.append(svgOpen(width, height));
        svg.append(titleText(title, width));
        svg.append(String.format(
                "<text x=\"%d\" y=\"%d\" font-size=\"12\" text-anchor=\"middle\" fill=\"currentColor\">データがありません</text>",
                width / 2, height / 2));
        svg.append("</svg>");
        return svg.toString();
    }

    private static String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
