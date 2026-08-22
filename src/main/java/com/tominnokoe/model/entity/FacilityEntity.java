package com.tominnokoe.model.entity;

/**
 * データセット③（都有施設一覧データ）1行を表すエンティティ。
 * ここに載っている施設は「都立＝都管轄」であることの根拠になる。
 * 区市町村立の施設はこのデータセットには含まれない（未ヒット＝区市町村管轄、という判定材料にするため）。
 */
public class FacilityEntity {

    private String id;
    private String facilityName;
    private String facilityType;
    private String managingBureau;
    private String address;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }

    public String getManagingBureau() { return managingBureau; }
    public void setManagingBureau(String managingBureau) { this.managingBureau = managingBureau; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
