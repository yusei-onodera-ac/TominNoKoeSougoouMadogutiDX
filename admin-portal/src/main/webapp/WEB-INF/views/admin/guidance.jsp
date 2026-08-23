<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "guidance"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>区市町村案内文 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <h1>区市町村向け案内状・回答文ドラフト</h1>
  <p class="hint">都の管轄外と判定された案件について、AIが起案した案内文です。「再生成」で言い回しを変更できます。</p>

  <c:forEach var="c" items="${cases}">
    <div class="card">
      <h2><c:out value="${c.id}"/>: <c:out value="${c.subject}"/></h2>
      <p class="hint">案内対象: <c:out value="${c.classification.externalGuidance.targetEntity}"/></p>
      <p><c:out value="${not empty c.guidanceTextOverride ? c.guidanceTextOverride : c.classification.externalGuidance.explanationText}"/></p>
      <p class="hint">
        参考リンク:
        <c:out value="${c.classification.externalGuidance.contactUrl}"/>
      </p>
      <form method="post" action="${pageContext.request.contextPath}/admin/guidance">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="caseId" value="${c.id}">
        <button type="submit" class="small secondary">再生成</button>
      </form>
    </div>
  </c:forEach>
  <c:if test="${empty cases}">
    <div class="card hint">対象の案件はありません。</div>
  </c:if>
</main>
</body>
</html>
