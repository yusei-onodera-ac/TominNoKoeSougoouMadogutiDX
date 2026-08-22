package com.tominnokoe.classification.retrieval;

import com.tominnokoe.dao.DatasetLoader;
import com.tominnokoe.model.entity.FacilityEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.OrgRuleEntity;
import com.tominnokoe.model.entity.PastCaseEntity;
import com.tominnokoe.model.entity.RoadEntity;
import com.tominnokoe.model.vo.ClassificationInput;
import com.tominnokoe.model.vo.RetrievedContext;
import com.tominnokoe.model.vo.ScoredOrgRule;
import com.tominnokoe.model.vo.ScoredPastCase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 5大オープンデータに対するハイブリッド検索(RAG)のモック実装。
 * ベクトルDB・埋め込みは使わず、各データ行に手作業で付与した keywords 配列への
 * 部分一致（{@link String#contains}）でスコアリングする。
 * 将来 {@link com.tominnokoe.classification.ClassificationEngine} を実LLM呼び出しに
 * 差し替える際も、このRAGコンテキスト取得部分はそのまま流用できる想定。
 */
public final class RetrievalService {

    private static final int SIMILAR_CASES_LIMIT = 3;

    public RetrievedContext retrieveContext(ClassificationInput input) {
        String text = normalize(input.combinedText());

        List<ScoredOrgRule> orgRules = matchOrgRules(text, input.getCategory());
        List<ScoredPastCase> similarCases = retrieveSimilarCases(text, input.getCategory());
        FacilityEntity facility = matchFacility(text);
        RoadEntity road = matchRoad(text);
        MunicipalityEntity municipality = matchMunicipality(text);

        return new RetrievedContext(orgRules, similarCases, facility, road, municipality);
    }

    private List<ScoredOrgRule> matchOrgRules(String text, String category) {
        List<ScoredOrgRule> scored = new ArrayList<>();
        for (OrgRuleEntity rule : DatasetLoader.orgRules()) {
            if (rule.isHeadOffice()) {
                continue; // フォールバック行はスコアリング対象に含めない
            }
            double score = keywordScore(text, rule.getKeywords());
            if (category != null && category.equals(rule.getCategoryHint())) {
                score += 0.15;
            }
            if (score > 0) {
                scored.add(new ScoredOrgRule(rule, score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredOrgRule::getScore).reversed());
        return scored;
    }

    public List<ScoredPastCase> retrieveSimilarCases(String text, String category) {
        List<ScoredPastCase> scored = new ArrayList<>();
        for (PastCaseEntity pastCase : DatasetLoader.pastCases()) {
            double score = keywordScore(text, pastCase.getKeywords());
            if (category != null && category.equals(pastCase.getCategory())) {
                score += 0.1;
            }
            if (score > 0) {
                scored.add(new ScoredPastCase(pastCase, score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredPastCase::getScore).reversed());
        return scored.size() > SIMILAR_CASES_LIMIT ? scored.subList(0, SIMILAR_CASES_LIMIT) : scored;
    }

    public FacilityEntity matchFacility(String text) {
        for (FacilityEntity facility : DatasetLoader.facilities()) {
            if (text.contains(normalize(facility.getFacilityName()))) {
                return facility;
            }
        }
        return null;
    }

    public RoadEntity matchRoad(String text) {
        for (RoadEntity road : DatasetLoader.roads()) {
            if (text.contains(normalize(road.getRouteName())) || text.contains(normalize(road.getRouteNumber()))) {
                return road;
            }
        }
        return null;
    }

    public MunicipalityEntity matchMunicipality(String text) {
        for (MunicipalityEntity m : DatasetLoader.municipalities()) {
            if (text.contains(normalize(m.getLocalGovName()))) {
                return m;
            }
        }
        return null;
    }

    /**
     * キーワード重複によるスコアリング。マッチしたキーワードの文字数（＝具体性）に応じて
     * 重み付けし、より特徴的な語（「環八」等）がヒットした場合に高スコアになるようにする。
     */
    private double keywordScore(String text, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0.0;
        }
        double raw = 0.0;
        int matchedCount = 0;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            String normalizedKeyword = normalize(keyword);
            if (text.contains(normalizedKeyword)) {
                matchedCount++;
                raw += Math.min(normalizedKeyword.length(), 6) * 0.08;
            }
        }
        if (matchedCount == 0) {
            return 0.0;
        }
        double coverageBonus = (double) matchedCount / keywords.size() * 0.2;
        return Math.min(raw + coverageBonus, 1.0);
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.JAPAN);
    }
}
