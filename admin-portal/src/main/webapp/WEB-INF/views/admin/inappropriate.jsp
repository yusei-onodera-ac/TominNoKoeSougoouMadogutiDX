<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "inappropriate"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>不適切監査ビュー - 都民の声総合窓口DX化（管理画面）</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <div class="card">
    <h1>不適切・規約違反監査ビュー</h1>
    <p class="hint">政策企画局専用の隔離枠です。各局へは通知されません。誤検知の場合は復元してください（監査ログに記録されます）。</p>

    <div class="table-scroll">
    <table>
      <thead>
      <tr><th scope="col">受付番号</th><th scope="col">受付日時</th><th scope="col">件名</th><th scope="col">理由</th><th scope="col">本文（一部）</th><th scope="col">操作</th></tr>
      </thead>
      <tbody>
      <c:forEach var="c" items="${cases}">
        <tr>
          <td><c:out value="${c.id}"/></td>
          <td style="white-space:nowrap;"><c:out value="${c.createdAtDisplay}"/></td>
          <td class="col-wrap"><c:out value="${c.subject}"/></td>
          <td><span class="badge inappropriate"><c:out value="${c.classification.inappropriateReason.label}"/></span></td>
          <td class="col-wrap"><c:out value="${c.body}"/></td>
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
  </div>
</main>
</body>
</html>
