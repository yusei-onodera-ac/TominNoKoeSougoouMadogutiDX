package com.tominnokoe.citizen;

import com.tominnokoe.citizen.web.filter.EncodingFilter;
import com.tominnokoe.citizen.web.filter.RateLimitFilter;
import com.tominnokoe.citizen.web.filter.SecurityHeadersFilter;
import com.tominnokoe.citizen.web.servlet.CaseDetailServlet;
import com.tominnokoe.citizen.web.servlet.CitizenSubmitServlet;
import com.tominnokoe.citizen.web.servlet.SuggestServlet;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * 都民向けポータルのサーブレット・フィルタ登録。
 * 行政向け管理画面（admin-portal）とは完全に別アプリケーション・別ポートで動作するため、
 * ここには都民向け機能のみを登録する（アノテーションスキャンには頼らず {@link Main} から明示的に呼び出す）。
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
        addFilter(ctx, "rateLimitFilter", new RateLimitFilter(), "/submit");
    }

    private static void registerServlets(Tomcat tomcat, Context ctx) {
        addServlet(tomcat, ctx, "citizenSubmit", new CitizenSubmitServlet(), "/submit");
        addServlet(tomcat, ctx, "suggest", new SuggestServlet(), "/api/suggest");
        addServlet(tomcat, ctx, "caseDetail", new CaseDetailServlet(), "/cases/*");
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
