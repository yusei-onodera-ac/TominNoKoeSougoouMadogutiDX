<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("currentPage", "opendata"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>オープンデータ公表 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <div class="card">
    <h1>オープンデータ公表ページ</h1>
    <p class="hint">個人情報を自動マスキングした集計データです。生の意見本文・氏名・連絡先は一切含まれません。</p>
    <p>
      総件数: <strong><c:out value="${stats.totalCases}"/></strong> 件 /
      不適切検知: <strong><c:out value="${stats.inappropriateCount}"/></strong> 件
      (<fmt:formatNumber value="${stats.inappropriateRate * 100}" maxFractionDigits="1"/>%) /
      平均confidence: <fmt:formatNumber value="${stats.averageConfidence}" maxFractionDigits="2"/>
    </p>
    <div class="export-buttons">
      <a class="btn small" href="${pageContext.request.contextPath}/admin/opendata/export?format=csv">CSVダウンロード</a>
      <a class="btn small" href="${pageContext.request.contextPath}/admin/opendata/export?format=json">JSONダウンロード</a>
      <a class="btn small" href="${pageContext.request.contextPath}/admin/opendata/export?format=xlsx">Excelダウンロード</a>
    </div>
  </div>

  <div class="charts-row">
    <div class="card">
      <c:out value="${bureauChartSvg}" escapeXml="false"/>
    </div>
    <div class="card">
      <c:out value="${typeChartSvg}" escapeXml="false"/>
    </div>
  </div>
  <div class="card">
    <c:out value="${monthChartSvg}" escapeXml="false"/>
  </div>
</main>
</body>
</html>
