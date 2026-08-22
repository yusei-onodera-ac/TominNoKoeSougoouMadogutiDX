<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "dashboard"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ダッシュボード - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <div class="card">
    <h1>管理メニュー</h1>

    <p class="dashboard-section-title">よく使うメニュー</p>
    <div class="panel-grid">
      <a class="panel-button" href="${pageContext.request.contextPath}/admin/triage">
        <svg viewBox="0 0 24 24"><path d="M4 6h16M4 12h16M4 18h10" stroke-linecap="round"/></svg>
        <span>トリアージ・<br>案件一覧</span>
      </a>
      <a class="panel-button" href="${pageContext.request.contextPath}/admin/manual-intake">
        <svg viewBox="0 0 24 24"><path d="M6.6 10.8a15 15 0 0 0 6.6 6.6l2.2-2.2a1 1 0 0 1 1-.25c1.1.36 2.3.56 3.5.56a1 1 0 0 1 1 1V20a1 1 0 0 1-1 1C10.2 21 3 13.8 3 5a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.2.2 2.4.56 3.5a1 1 0 0 1-.25 1z" stroke-linejoin="round"/></svg>
        <span>電話・FAX・窓口<br>代筆入力</span>
      </a>
      <a class="panel-button" href="${pageContext.request.contextPath}/admin/governance">
        <svg viewBox="0 0 24 24"><path d="M12 3v4M12 7l-6 4M12 7l6 4M6 11v6M18 11v6M4 17h4v4H4zM10 17h4v4h-4zM16 17h4v4h-4z" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <span>ガバナンス通知・<br>進捗</span>
      </a>
    </div>

    <c:if test="${isGeneralDesk}">
      <details class="advanced-section" open>
        <summary>総合窓口専用機能（政策企画局）</summary>
        <div class="advanced-body">
          <p class="hint">政策企画局（都民の声総合窓口）としてログイン中のため、全案件の横断管理に加えて以下の機能を利用できます。</p>

          <p class="pill-category-label">監査・審査</p>
          <div class="pill-row">
            <a class="pill-button" href="${pageContext.request.contextPath}/admin/inappropriate">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 3l8 4v5c0 5-3.4 7.8-8 9-4.6-1.2-8-4-8-9V7z" stroke-linejoin="round"/></svg>
              不適切監査ビュー
            </a>
          </div>

          <p class="pill-category-label">案内・公表</p>
          <div class="pill-row">
            <a class="pill-button" href="${pageContext.request.contextPath}/admin/guidance">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 21s7-6.2 7-11.5A7 7 0 0 0 5 9.5C5 14.8 12 21 12 21z" stroke-linejoin="round"/><circle cx="12" cy="9.5" r="2.2"/></svg>
              区市町村案内文
            </a>
            <a class="pill-button" href="${pageContext.request.contextPath}/admin/opendata">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 20V10M10 20V4M16 20v-7M4 20h16" stroke-linecap="round" stroke-linejoin="round"/></svg>
              オープンデータ公表
            </a>
          </div>
        </div>
      </details>
    </c:if>
  </div>

  <div class="card hint">
    <p>本画面は東京都オープンデータハッカソン提出用プロトタイプです。左上のナビゲーションからも各機能へ直接移動できます。</p>
  </div>
</main>
</body>
</html>
