package com.tominnokoe.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tominnokoe.model.entity.CaseEntity;
import com.tominnokoe.model.vo.AggregatedStats;
import com.tominnokoe.util.SpreadsheetSanitizer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 案件データを匿名化・集計し、CSV/JSON/Excelの3形式で出力するサービス（F-A05拡張）。
 * 生の意見本文・氏名・連絡先は出力に一切含めない。
 * CSV/Excelのセル値は {@link SpreadsheetSanitizer} で数式インジェクション対策を施す。
 */
public final class ExportService {

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.of("Asia/Tokyo"));

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public AggregatedStats buildAggregatedStats(List<CaseEntity> cases) {
        AggregatedStats stats = new AggregatedStats();
        stats.setTotalCases(cases.size());

        Map<String, Integer> byBureau = new TreeMap<>();
        Map<String, Integer> byType = new TreeMap<>();
        Map<String, Integer> byMonth = new TreeMap<>();
        int inappropriate = 0;
        double confidenceSum = 0.0;

        for (CaseEntity c : cases) {
            var classification = c.getClassification();
            if (classification == null) {
                continue;
            }
            confidenceSum += classification.getConfidenceScore();

            String typeKey = classification.isInappropriate() ? "INAPPROPRIATE" : classification.getClassificationType().name();
            byType.merge(typeKey, 1, Integer::sum);

            if (classification.isInappropriate()) {
                inappropriate++;
            } else if (classification.getClassificationType() != null
                    && classification.getClassificationType().name().equals("TOKYO_METROPOLITAN")) {
                String bureau = c.getAssignedBureauOverride() != null
                        ? c.getAssignedBureauOverride()
                        : classification.getRouting().getPrimaryBureau();
                if (bureau != null) {
                    byBureau.merge(bureau, 1, Integer::sum);
                }
            }

            if (c.getCreatedAt() != null) {
                String month = MONTH_FORMAT.format(c.getCreatedAt());
                byMonth.merge(month, 1, Integer::sum);
            }
        }

        stats.setInappropriateCount(inappropriate);
        stats.setAverageConfidence(cases.isEmpty() ? 0.0 : confidenceSum / cases.size());
        stats.setCountByBureau(byBureau);
        stats.setCountByClassificationType(byType);
        stats.setCountByMonth(byMonth);
        return stats;
    }

    public void writeJson(AggregatedStats stats, OutputStream out) throws IOException {
        MAPPER.writeValue(out, stats);
    }

    public void writeCsv(AggregatedStats stats, OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("category,key,count\n");
        appendCsvSection(sb, "bureau", stats.getCountByBureau());
        appendCsvSection(sb, "classification_type", stats.getCountByClassificationType());
        appendCsvSection(sb, "month", stats.getCountByMonth());
        sb.append("summary,total_cases,").append(stats.getTotalCases()).append('\n');
        sb.append("summary,inappropriate_count,").append(stats.getInappropriateCount()).append('\n');
        sb.append(String.format("summary,average_confidence,%.3f%n", stats.getAverageConfidence()));
        out.write(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void appendCsvSection(StringBuilder sb, String category, Map<String, Integer> map) {
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            sb.append(csvEscape(category)).append(',')
                    .append(csvEscape(e.getKey())).append(',')
                    .append(e.getValue()).append('\n');
        }
    }

    private String csvEscape(String raw) {
        String sanitized = SpreadsheetSanitizer.sanitize(raw);
        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n")) {
            return "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }

    public void writeXlsx(AggregatedStats stats, OutputStream out) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("集計データ");
            int rowIdx = 0;

            rowIdx = writeSection(sheet, rowIdx, "局別件数（都管轄案件のみ）", stats.getCountByBureau());
            rowIdx = writeSection(sheet, rowIdx, "分類タイプ別内訳", stats.getCountByClassificationType());
            rowIdx = writeSection(sheet, rowIdx, "月別件数推移", stats.getCountByMonth());

            Row summaryHeader = sheet.createRow(rowIdx++);
            summaryHeader.createCell(0).setCellValue("サマリ");
            Row totalRow = sheet.createRow(rowIdx++);
            totalRow.createCell(0).setCellValue("総件数");
            totalRow.createCell(1).setCellValue(stats.getTotalCases());
            Row inapRow = sheet.createRow(rowIdx++);
            inapRow.createCell(0).setCellValue("不適切検知件数");
            inapRow.createCell(1).setCellValue(stats.getInappropriateCount());
            Row confRow = sheet.createRow(rowIdx);
            confRow.createCell(0).setCellValue("平均confidence");
            confRow.createCell(1).setCellValue(stats.getAverageConfidence());

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            workbook.write(out);
        }
    }

    private int writeSection(Sheet sheet, int startRow, String title, Map<String, Integer> map) {
        int rowIdx = startRow;
        Row titleRow = sheet.createRow(rowIdx++);
        titleRow.createCell(0).setCellValue(title);
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue(SpreadsheetSanitizer.sanitize(e.getKey()));
            row.createCell(1).setCellValue(e.getValue());
        }
        rowIdx++; // セクション間の空行
        return rowIdx;
    }
}
