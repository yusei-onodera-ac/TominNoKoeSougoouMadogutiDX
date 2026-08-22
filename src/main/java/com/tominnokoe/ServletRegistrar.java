package com.tominnokoe;

import com.tominnokoe.web.filter.AdminAuthFilter;
import com.tominnokoe.web.filter.EncodingFilter;
import com.tominnokoe.web.filter.RateLimitFilter;
import com.tominnokoe.web.filter.SecurityHeadersFilter;
import com.tominnokoe.web.servlet.*;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * すべてのサーブレット・フィルタの登録をここに集約する
 * （アノテーションスキャンに頼らず {@link Main} から明示的に呼び出す）。
 */
final class ServletRegistrar {

    private ServletRegistrar() {
    }

    static void registerAll(Tomcat tomcat, Context ctx) {
        registerFilters(ctx);
        registerServlets(tomcat, ctx);
    }

    private static void registerFilters(Context ctx) {
        addFilter(ctx, "encodingFilter", new EncodingFilter(), "/*");
        addFilter(ctx, "securityHeadersFilter", new SecurityHeadersFilter(), "/*");
        addFilter(ctx, "adminAuthFilter", new AdminAuthFilter(), "/admin/*");
        addFilter(ctx, "rateLimitFilter", new RateLimitFilter(), "/submit");
    }

    private static void registerServlets(Tomcat tomcat, Context ctx) {
        addServlet(tomcat, ctx, "citizenSubmit", new CitizenSubmitServlet(), "/submit");
        addServlet(tomcat, ctx, "suggest", new SuggestServlet(), "/api/suggest");
        addServlet(tomcat, ctx, "caseDetail", new CaseDetailServlet(), "/cases/*");

        addServlet(tomcat, ctx, "adminIndex", new AdminIndexServlet(), "/admin");
        addServlet(tomcat, ctx, "adminLogin", new AdminLoginServlet(), "/admin/login");
        addServlet(tomcat, ctx, "adminTriage", new AdminTriageServlet(), "/admin/triage");
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

        FilterMap map = new FilterMap();
        map.setFilterName(name);
        map.addURLPattern(mapping);
        map.setDispatcher(DispatcherType.REQUEST.name());
        ctx.addFilterMap(map);
    }
}
