<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "inappropriate"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>不適切監査ビュー - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main>
  <div class="card">
    <h1>不適切・規約違反監査ビュー</h1>
    <p class="hint">政策企画局専用の隔離枠です。各局へは通知されません。誤検知の場合は復元してください（監査ログに記録されます）。</p>

    <table>
      <thead>
      <tr><th>受付番号</th><th>受付日時</th><th>件名</th><th>理由</th><th>本文（一部）</th><th>操作</th></tr>
      </thead>
      <tbody>
      <c:forEach var="c" items="${cases}">
        <tr>
          <td><c:out value="${c.id}"/></td>
          <td><c:out value="${c.createdAt}"/></td>
          <td><c:out value="${c.subject}"/></td>
          <td><span class="badge inappropriate"><c:out value="${c.classification.inappropriateReason}"/></span></td>
          <td><c:out value="${c.body}"/></td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/admin/inappropriate">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="caseId" value="${c.id}">
              <button type="submit" class="small secondary">誤検知として復元</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty cases}">
        <tr><td colspan="6" class="hint">現在、不適切と判定された案件はありません。</td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</main>
</body>
</html>
