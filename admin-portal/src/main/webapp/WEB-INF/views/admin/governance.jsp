<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "governance"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ガバナンス通知・進捗管理 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main>
  <h1>ガバナンス通知・進捗管理</h1>
  <p class="hint">現場出先機関（Action Owner）だけでなく、上位の局・部・総合窓口へも同時に状況が共有される階層別ステータスです。クリックで次のステータスへ進みます。</p>

  <c:forEach var="c" items="${cases}">
    <div class="card">
      <h2><c:out value="${c.id}"/>: <c:out value="${c.subject}"/></h2>
      <table>
        <thead><tr><th>階層</th><th>部署</th><th>目的</th><th>ステータス</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="node" items="${c.classification.routing.governanceNotificationTree}">
          <tr>
            <td><c:out value="${node.level}"/></td>
            <td><c:out value="${node.departmentName}"/></td>
            <td><c:out value="${node.purpose}"/></td>
            <td>
              <c:set var="st" value="${c.notificationStatuses[node.departmentName]}"/>
              <span class="status-pill ${empty st ? 'PENDING' : st}"><c:out value="${empty st ? 'PENDING' : st}"/></span>
            </td>
            <td>
              <form method="post" action="${pageContext.request.contextPath}/admin/governance">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="caseId" value="${c.id}">
                <input type="hidden" name="departmentName" value="${node.departmentName}">
                <button type="submit" class="small secondary">次のステータスへ</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </c:forEach>
  <c:if test="${empty cases}">
    <div class="card hint">通知対象の案件はありません。</div>
  </c:if>
</main>
</body>
</html>
