package com.tominnokoe.model.entity;

/** データセット④（東京都管理道路（都道）路線・境界データ）1行を表すエンティティ。 */
public class RoadEntity {

    private String id;
    private String routeName;
    private String routeNumber;
    private String managingOffice;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getManagingOffice() { return managingOffice; }
    public void setManagingOffice(String managingOffice) { this.managingOffice = managingOffice; }
}
