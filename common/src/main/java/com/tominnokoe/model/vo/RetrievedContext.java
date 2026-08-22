package com.tominnokoe.model.vo;

import com.tominnokoe.model.entity.FacilityEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.RoadEntity;

import java.util.Collections;
import java.util.List;

/**
 * 5大オープンデータからハイブリッド検索(RAG)で取得したコンテキスト。
 * {@link com.tominnokoe.classification.retrieval.RetrievalService#retrieveContext} の戻り値。
 */
public final class RetrievedContext {

    private final List<ScoredOrgRule> matchedOrgRules;
    private final List<ScoredPastCase> similarCases;
    private final FacilityEntity matchedFacility;
    private final RoadEntity matchedRoad;
    private final MunicipalityEntity matchedMunicipality;

    public RetrievedContext(List<ScoredOrgRule> matchedOrgRules, List<ScoredPastCase> similarCases,
                             FacilityEntity matchedFacility, RoadEntity matchedRoad,
                             MunicipalityEntity matchedMunicipality) {
        this.matchedOrgRules = matchedOrgRules == null ? Collections.emptyList() : matchedOrgRules;
        this.similarCases = similarCases == null ? Collections.emptyList() : similarCases;
        this.matchedFacility = matchedFacility;
        this.matchedRoad = matchedRoad;
        this.matchedMunicipality = matchedMunicipality;
    }

    public List<ScoredOrgRule> getMatchedOrgRules() { return matchedOrgRules; }
    public List<ScoredPastCase> getSimilarCases() { return similarCases; }
    public FacilityEntity getMatchedFacility() { return matchedFacility; }
    public RoadEntity getMatchedRoad() { return matchedRoad; }
    public MunicipalityEntity getMatchedMunicipality() { return matchedMunicipality; }

    public boolean hasTokyoEntityMatch() {
        return matchedFacility != null || matchedRoad != null;
    }
}
