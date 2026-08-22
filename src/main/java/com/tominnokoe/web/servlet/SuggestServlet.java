package com.tominnokoe.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tominnokoe.classification.retrieval.RetrievalService;
import com.tominnokoe.model.entity.FacilityEntity;
import com.tominnokoe.model.entity.MunicipalityEntity;
import com.tominnokoe.model.entity.RoadEntity;
import com.tominnokoe.model.vo.ScoredPastCase;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 入力中のリアルタイム類似事例サジェスト・管轄プレビュー（F-C02/F-C03）。
 * 非永続。CSRFチェックは行わない（データを変更しないGET相当の読み取り専用操作のため）。
 */
public class SuggestServlet extends HttpServlet {

    private final RetrievalService retrievalService = new RetrievalService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String subject = nullToEmpty(request.getParameter("subject"));
        String body = nullToEmpty(request.getParameter("body"));
        String category = request.getParameter("category");
        String text = (subject + "\n" + body).toLowerCase(java.util.Locale.JAPAN);

        List<ScoredPastCase> similar = retrievalService.retrieveSimilarCases(text, category);
        List<Map<String, Object>> similarCasesJson = new ArrayList<>();
        for (ScoredPastCase sc : similar) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", sc.getPastCase().getId());
            m.put("subject", sc.getPastCase().getSubject());
            m.put("handledBureau", sc.getPastCase().getHandledBureau());
            m.put("responseSummary", sc.getPastCase().getResponseSummary());
            m.put("score", Math.round(sc.getScore() * 100) / 100.0);
            similarCasesJson.add(m);
        }

        FacilityEntity facility = retrievalService.matchFacility(text);
        RoadEntity road = retrievalService.matchRoad(text);
        MunicipalityEntity municipality = retrievalService.matchMunicipality(text);

        Map<String, Object> jurisdictionPreview = new LinkedHashMap<>();
        boolean likelyMunicipality = municipality != null && facility == null && road == null;
        jurisdictionPreview.put("likelyMunicipality", likelyMunicipality);
        if (likelyMunicipality) {
            jurisdictionPreview.put("municipalityName", municipality.getLocalGovName());
            jurisdictionPreview.put("consultationDesk", municipality.getConsultationDesk());
            jurisdictionPreview.put("contactUrl", municipality.getContactUrl());
        }
        if (facility != null) {
            jurisdictionPreview.put("matchedFacility", facility.getFacilityName());
        }
        if (road != null) {
            jurisdictionPreview.put("matchedRoad", road.getRouteName());
        }

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("similarCases", similarCasesJson);
        responseBody.put("jurisdictionPreview", jurisdictionPreview);

        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), responseBody);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
