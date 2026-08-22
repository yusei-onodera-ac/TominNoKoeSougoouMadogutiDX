package com.tominnokoe.model.entity;

import java.util.List;

/**
 * データセット②（東京都組織規程・事務分掌データ）1行を表すエンティティ。
 * 局→部→課/現場出先機関の親子関係と、キーワードによる所掌事務範囲を保持する。
 * 担当組織名のハルシネーション防止のため、ルーティング結果の組織名は必ずこのデータセットに
 * 実在する値のみを使う（{@code src/main/resources/data/org_jurisdiction_rules.json} が実体）。
 */
public class OrgRuleEntity {

    private String id;
    private String bureauName;
    private String divisionName;
    private String sectionName;
    /** 実務対応を行う現場出先機関（Action Owner）。 */
    private String actionOwner;
    private String jurisdictionScope;
    private List<String> keywords;
    /** 都民が申告するジャンル選択と緩やかに対応させるためのヒント。 */
    private String categoryHint;
    /** 総合窓口（政策企画局）のフォールバック行かどうか。 */
    private boolean headOffice;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBureauName() { return bureauName; }
    public void setBureauName(String bureauName) { this.bureauName = bureauName; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getActionOwner() { return actionOwner; }
    public void setActionOwner(String actionOwner) { this.actionOwner = actionOwner; }

    public String getJurisdictionScope() { return jurisdictionScope; }
    public void setJurisdictionScope(String jurisdictionScope) { this.jurisdictionScope = jurisdictionScope; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getCategoryHint() { return categoryHint; }
    public void setCategoryHint(String categoryHint) { this.categoryHint = categoryHint; }

    public boolean isHeadOffice() { return headOffice; }
    public void setHeadOffice(boolean headOffice) { this.headOffice = headOffice; }
}
