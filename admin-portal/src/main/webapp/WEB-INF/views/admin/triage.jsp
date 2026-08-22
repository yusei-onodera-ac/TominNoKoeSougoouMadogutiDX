<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("currentPage", "triage"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>総合窓口トリアージ - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main>
  <div class="card">
    <h1>総合窓口トリアージダッシュボード</h1>
    <p class="hint">政策企画局向け。不適切フラグの立った案件はここには表示されません（<a href="${pageContext.request.contextPath}/admin/inappropriate">不適切監査ビュー</a>を参照）。</p>

    <form method="get" action="${pageContext.request.contextPath}/admin/triage" style="display:flex; gap:12px; align-items:end; margin-bottom: 16px;">
      <div style="flex:1;">
        <label for="classification">分類タイプ</label>
        <select id="classification" name="classification">
          <option value="">すべて</option>
          <option value="TOKYO_METROPOLITAN" ${classificationFilter == 'TOKYO_METROPOLITAN' ? 'selected' : ''}>TOKYO_METROPOLITAN</option>
          <option value="JURISDICTION_OTHER" ${classificationFilter == 'JURISDICTION_OTHER' ? 'selected' : ''}>JURISDICTION_OTHER</option>
          <option value="UNKNOWN" ${classificationFilter == 'UNKNOWN' ? 'selected' : ''}>UNKNOWN</option>
        </select>
      </div>
      <div style="flex:1;">
        <label for="bureau">担当局で絞り込み</label>
        <input type="text" id="bureau" name="bureau" value="${fn:escapeXml(bureauFilter)}" placeholder="例: 建設局">
      </div>
      <div><button type="submit">絞り込む</button></div>
    </form>

    <table>
      <thead>
      <tr>
        <th>受付番号</th><th>受付日時</th><th>件名</th><th>分類</th><th>confidence</th>
        <th>担当局</th><th>ステータス</th><th>手動アサイン</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="c" items="${cases}">
        <tr>
          <td><a href="${pageContext.request.contextPath}/cases/${c.id}"><c:out value="${c.id}"/></a></td>
          <td><c:out value="${c.createdAt}"/></td>
          <td><c:out value="${c.subject}"/></td>
          <td>
            <c:choose>
              <c:when test="${c.classification.classificationType == 'TOKYO_METROPOLITAN'}"><span class="badge tokyo">TOKYO</span></c:when>
              <c:when test="${c.classification.classificationType == 'JURISDICTION_OTHER'}"><span class="badge other">OTHER</span></c:when>
              <c:otherwise><span class="badge unknown">UNKNOWN</span></c:otherwise>
            </c:choose>
          </td>
          <td><fmt:formatNumber value="${c.classification.confidenceScore}" maxFractionDigits="2"/>
            <c:if test="${not empty c.classification.suggestedBureauHint}">
              <div class="hint">推定局ヒント: <c:out value="${c.classification.suggestedBureauHint}"/></div>
            </c:if>
          </td>
          <td><c:out value="${not empty c.assignedBureauOverride ? c.assignedBureauOverride : c.classification.routing.primaryBureau}"/></td>
          <td><c:out value="${c.status}"/></td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/admin/triage" style="display:flex; gap:4px;">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="caseId" value="${c.id}">
              <input type="text" name="bureau" placeholder="担当局名" style="margin:0; padding:4px 6px; font-size:12px; width:110px;">
              <button type="submit" class="small">アサイン</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty cases}">
        <tr><td colspan="8" class="hint">該当する案件はありません。</td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</main>
</body>
</html>
