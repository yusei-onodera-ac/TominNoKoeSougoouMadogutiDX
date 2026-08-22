package com.tominnokoe.model.vo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 匿名化・集計済みの公開用統計データ（F-A05）。
 * 生の意見本文・氏名・連絡先は一切含めない — このVOのフィールドに追加する際は
 * 必ず「集計値のみ」であることを確認すること（個人情報を直接埋め込まない）。
 */
public final class AggregatedStats {

    private int totalCases;
    private int inappropriateCount;
    private double averageConfidence;
    private Map<String, Integer> countByBureau = new LinkedHashMap<>();
    private Map<String, Integer> countByClassificationType = new LinkedHashMap<>();
    private Map<String, Integer> countByMonth = new LinkedHashMap<>();

    public int getTotalCases() { return totalCases; }
    public void setTotalCases(int totalCases) { this.totalCases = totalCases; }

    public int getInappropriateCount() { return inappropriateCount; }
    public void setInappropriateCount(int inappropriateCount) { this.inappropriateCount = inappropriateCount; }

    public double getInappropriateRate() {
        return totalCases == 0 ? 0.0 : (double) inappropriateCount / totalCases;
    }

    public double getAverageConfidence() { return averageConfidence; }
    public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }

    public Map<String, Integer> getCountByBureau() { return countByBureau; }
    public void setCountByBureau(Map<String, Integer> countByBureau) { this.countByBureau = countByBureau; }

    public Map<String, Integer> getCountByClassificationType() { return countByClassificationType; }
    public void setCountByClassificationType(Map<String, Integer> countByClassificationType) { this.countByClassificationType = countByClassificationType; }

    public Map<String, Integer> getCountByMonth() { return countByMonth; }
    public void setCountByMonth(Map<String, Integer> countByMonth) { this.countByMonth = countByMonth; }
}
