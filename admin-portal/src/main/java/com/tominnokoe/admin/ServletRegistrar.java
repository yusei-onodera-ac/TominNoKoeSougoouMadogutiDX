package com.tominnokoe.admin;

import com.tominnokoe.admin.web.filter.AdminAuthFilter;
import com.tominnokoe.admin.web.filter.EncodingFilter;
import com.tominnokoe.admin.web.filter.GeneralDeskOnlyFilter;
import com.tominnokoe.admin.web.filter.LgwanSimulationFilter;
import com.tominnokoe.admin.web.filter.SecurityHeadersFilter;
import com.tominnokoe.admin.web.servlet.AdminGovernanceServlet;
import com.tominnokoe.admin.web.servlet.AdminGuidanceServlet;
import com.tominnokoe.admin.web.servlet.AdminInappropriateServlet;
import com.tominnokoe.admin.web.servlet.AdminIndexServlet;
import com.tominnokoe.admin.web.servlet.AdminLoginServlet;
import com.tominnokoe.admin.web.servlet.AdminLogoutServlet;
import com.tominnokoe.admin.web.servlet.AdminManualIntakeServlet;
import com.tominnokoe.admin.web.servlet.AdminOpenDataExportServlet;
import com.tominnokoe.admin.web.servlet.AdminOpenDataServlet;
import com.tominnokoe.admin.web.servlet.AdminTriageServlet;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * 行政向け管理画面のサーブレット・フィルタ登録。
 * ルートパス（"/"）から {@code /admin/...} で配信する（独立アプリのためコンテキストパス的な
 * 衝突は気にしなくてよいが、URLの読みやすさのため引き続き /admin プレフィックスを使う）。
 */
final class ServletRegistrar {

    private ServletRegistrar() {
    }

    static void registerAll(Tomcat tomcat, Context ctx) {
        registerFilters(ctx);
        registerServlets(tomcat, ctx);
    }

    private static void registerFilters(Context ctx) {
        // LGWAN相当のネットワークアクセス制御は最外層（他の全フィルタより前）で行う
        addFilter(ctx, "lgwanSimulationFilter", new LgwanSimulationFilter(), "/*");
        addFilter(ctx, "encodingFilter", new EncodingFilter(), "/*");
        addFilter(ctx, "securityHeadersFilter", new SecurityHeadersFilter(), "/*");
        addFilter(ctx, "adminAuthFilter", new AdminAuthFilter(), "/admin/*");

        // 政策企画局（総合窓口）専用機能へのアクセス制御（複数URLパターンに同一フィルタを適用）
        FilterDef generalDeskDef = new FilterDef();
        generalDeskDef.setFilterName("generalDeskOnlyFilter");
        generalDeskDef.setFilter(new GeneralDeskOnlyFilter());
        ctx.addFilterDef(generalDeskDef);
        addFilterMapping(ctx, "generalDeskOnlyFilter", "/admin/inappropriate");
        addFilterMapping(ctx, "generalDeskOnlyFilter", "/admin/guidance");
        addFilterMapping(ctx, "generalDeskOnlyFilter", "/admin/opendata");
        addFilterMapping(ctx, "generalDeskOnlyFilter", "/admin/opendata/export");
    }

    private static void registerServlets(Tomcat tomcat, Context ctx) {
        addServlet(tomcat, ctx, "root", new AdminIndexServlet(), "");
        addServlet(tomcat, ctx, "adminIndex", new AdminIndexServlet(), "/admin");
        addServlet(tomcat, ctx, "adminLogin", new AdminLoginServlet(), "/admin/login");
        addServlet(tomcat, ctx, "adminLogout", new AdminLogoutServlet(), "/admin/logout");
        addServlet(tomcat, ctx, "adminTriage", new AdminTriageServlet(), "/admin/triage");
        addServlet(tomcat, ctx, "adminManualIntake", new AdminManualIntakeServlet(), "/admin/manual-intake");
        addServlet(tomcat, ctx, "adminInappropriate", new AdminInappropriateServlet(), "/admin/inappropriate");
        addServlet(tomcat, ctx, "adminGovernance", new AdminGovernanceServlet(), "/admin/governance");
        addServlet(tomcat, ctx, "adminGuidance", new AdminGuidanceServlet(), "/admin/guidance");
        addServlet(tomcat, ctx, "adminOpenData", new AdminOpenDataServlet(), "/admin/opendata");
        addServlet(tomcat, ctx, "adminOpenDataExport", new AdminOpenDataExportServlet(), "/admin/opendata/export");
    }

    private static void addServlet(Tomcat tomcat, Context ctx, String name, Servlet servlet, String mapping) {
        tomcat.addServlet(ctx, name, servlet);
        ctx.addServletMappingDecoded(mapping, name);
    }

    private static void addFilter(Context ctx, String name, Filter filter, String mapping) {
        FilterDef def = new FilterDef();
        def.setFilterName(name);
        def.setFilter(filter);
        ctx.addFilterDef(def);
        addFilterMapping(ctx, name, mapping);
    }

    private static void addFilterMapping(Context ctx, String filterName, String mapping) {
        FilterMap map = new FilterMap();
        map.setFilterName(filterName);
        map.addURLPattern(mapping);
        map.setDispatcher(DispatcherType.REQUEST.name());
        ctx.addFilterMap(map);
    }
}
