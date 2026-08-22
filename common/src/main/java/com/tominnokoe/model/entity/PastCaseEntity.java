package com.tominnokoe.model.entity;

import java.util.List;

/** データセット①（都政への提言・意見・要望等の受付・対応状況）1行を表すエンティティ。RAGの類似事例検索対象。 */
public class PastCaseEntity {

    private String id;
    private String category;
    private String subject;
    private String body;
    private String handledBureau;
    private String handledDivision;
    private String responseSummary;
    private List<String> keywords;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getHandledBureau() { return handledBureau; }
    public void setHandledBureau(String handledBureau) { this.handledBureau = handledBureau; }

    public String getHandledDivision() { return handledDivision; }
    public void setHandledDivision(String handledDivision) { this.handledDivision = handledDivision; }

    public String getResponseSummary() { return responseSummary; }
    public void setResponseSummary(String responseSummary) { this.responseSummary = responseSummary; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
