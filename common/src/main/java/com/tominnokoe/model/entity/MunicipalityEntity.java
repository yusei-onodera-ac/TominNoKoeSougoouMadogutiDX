package com.tominnokoe.model.entity;

/** データセット⑤（区市町村一覧・コードデータ）1行を表すエンティティ。案内文生成に使う。 */
public class MunicipalityEntity {

    private String localGovCode;
    private String localGovName;
    private String contactUrl;
    private String consultationDesk;

    public String getLocalGovCode() { return localGovCode; }
    public void setLocalGovCode(String localGovCode) { this.localGovCode = localGovCode; }

    public String getLocalGovName() { return localGovName; }
    public void setLocalGovName(String localGovName) { this.localGovName = localGovName; }

    public String getContactUrl() { return contactUrl; }
    public void setContactUrl(String contactUrl) { this.contactUrl = contactUrl; }

    public String getConsultationDesk() { return consultationDesk; }
    public void setConsultationDesk(String consultationDesk) { this.consultationDesk = consultationDesk; }
}
