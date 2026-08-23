<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<% request.setAttribute("currentPage", "triage"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>案件一覧 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <div class="card">
    <c:if test="${not empty error}">
      <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
    <c:choose>
      <c:when test="${isGeneralDesk}">
        <h1>総合窓口 案件一覧（全局横断）</h1>
        <p class="hint">政策企画局（都民の声総合窓口）向け。全局の案件を横断的に確認できます。不適切フラグの立った案件はここには表示されません（<a href="${pageContext.request.contextPath}/admin/inappropriate">不適切監査ビュー</a>を参照）。</p>
      </c:when>
      <c:otherwise>
        <h1><c:out value="${sessionBureau}"/> の案件一覧</h1>
        <p class="hint">自局が担当（または関連局）の案件のみ表示されます。案件へ回答文を入力・送信できます。</p>
      </c:otherwise>
    </c:choose>

    <form method="get" action="${pageContext.request.contextPath}/admin/triage" style="display:flex; gap:12px; align-items:end; margin-bottom: 16px;">
      <div style="flex:1;">
        <label for="classification">分類タイプ</label>
        <select id="classification" name="classification">
          <option value="">すべて</option>
          <option value="TOKYO_METROPOLITAN" ${classificationFilter == 'TOKYO_METROPOLITAN' ? 'selected' : ''}>都管轄</option>
          <option value="JURISDICTION_OTHER" ${classificationFilter == 'JURISDICTION_OTHER' ? 'selected' : ''}>他管轄（区市町村等）</option>
          <option value="UNKNOWN" ${classificationFilter == 'UNKNOWN' ? 'selected' : ''}>不明（要確認）</option>
        </select>
      </div>
      <c:if test="${isGeneralDesk}">
        <div style="flex:1;">
          <label for="bureau">担当局で絞り込み</label>
          <input type="text" id="bureau" name="bureau" value="${fn:escapeXml(bureauFilter)}" placeholder="例: 建設局">
        </div>
      </c:if>
      <div><button type="submit">絞り込む</button></div>
    </form>

    <div class="table-scroll">
    <table>
      <thead>
      <tr>
        <th scope="col">受付番号</th><th scope="col">受付チャネル</th><th scope="col">受付日時</th><th scope="col">件名</th><th scope="col">分類</th><th scope="col">確信度</th>
        <th scope="col">担当局</th><th scope="col">ステータス</th>
        <c:if test="${isGeneralDesk}"><th scope="col">手動アサイン</th></c:if>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="c" items="${cases}" varStatus="caseStatus">
        <tr class="${caseStatus.index % 2 == 0 ? 'case-row-even' : 'case-row-odd'}">
          <td style="white-space:nowrap;"><a href="${citizenPortalBaseUrl}/cases/${c.id}" target="_blank" rel="noopener"><c:out value="${c.id}"/></a></td>
          <td>
            <c:choose>
              <c:when test="${c.intakeChannel == 'WEB_FORM'}">Web投稿</c:when>
              <c:when test="${c.intakeChannel == 'PHONE'}">電話（<c:out value="${c.intakeStaffBureau}"/>代筆）</c:when>
              <c:when test="${c.intakeChannel == 'FAX'}">FAX（<c:out value="${c.intakeStaffBureau}"/>代筆）</c:when>
              <c:when test="${c.intakeChannel == 'VISIT'}">窓口来訪（<c:out value="${c.intakeStaffBureau}"/>代筆）</c:when>
              <c:when test="${c.intakeChannel == 'LETTER'}">手紙（<c:out value="${c.intakeStaffBureau}"/>代筆）</c:when>
              <c:when test="${c.intakeChannel == 'OPINION_BOX'}">意見箱（<c:out value="${c.intakeStaffBureau}"/>代筆）</c:when>
            </c:choose>
          </td>
          <td style="white-space:nowrap;"><c:out value="${c.createdAtDisplay}"/></td>
          <td class="col-wrap"><c:out value="${c.subject}"/></td>
          <td style="white-space:nowrap;">
            <c:choose>
              <c:when test="${c.classification.classificationType == 'TOKYO_METROPOLITAN'}"><span class="badge tokyo">都管轄</span></c:when>
              <c:when test="${c.classification.classificationType == 'JURISDICTION_OTHER'}"><span class="badge other">他管轄</span></c:when>
              <c:otherwise><span class="badge unknown">不明</span></c:otherwise>
            </c:choose>
          </td>
          <td style="white-space:nowrap;"><fmt:formatNumber value="${c.classification.confidenceScore}" maxFractionDigits="2"/>
            <c:if test="${not empty c.classification.suggestedBureauHint}">
              <div class="hint" style="white-space:normal;">推定局ヒント: <c:out value="${c.classification.suggestedBureauHint}"/></div>
            </c:if>
          </td>
          <c:set var="displayBureau" value="${not empty c.assignedBureauOverride ? c.assignedBureauOverride : c.classification.routing.primaryBureau}"/>
          <%-- 過去データに残る旧仕様のプレースホルダ文字列"UNKNOWN"は、担当局が未特定という意味であり
               表示上は空欄にする（英語のまま出さないため）。 --%>
          <td style="white-space:nowrap;"><c:out value="${displayBureau == 'UNKNOWN' ? '' : displayBureau}"/></td>
          <td style="white-space:nowrap;"><c:out value="${c.status.label}"/></td>
          <c:if test="${isGeneralDesk}">
            <td>
              <form method="post" action="${pageContext.request.contextPath}/admin/triage" style="display:flex; gap:4px;">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="action" value="assign">
                <input type="hidden" name="caseId" value="${c.id}">
                <input type="text" name="bureau" placeholder="担当局名" style="margin:0; padding:4px 6px; font-size:12px; width:110px;">
                <button type="submit" class="small">アサイン</button>
              </form>
            </td>
          </c:if>
        </tr>
        <tr class="${caseStatus.index % 2 == 0 ? 'case-row-even' : 'case-row-odd'}">
          <td colspan="${isGeneralDesk ? 9 : 8}">
            <div class="hint" style="margin-bottom:8px;">
              通知先:
              <c:choose>
                <c:when test="${empty c.classification.routing.governanceNotificationTree}">通知対象なし</c:when>
                <c:otherwise>
                  <c:forEach var="node" items="${c.classification.routing.governanceNotificationTree}" varStatus="ns"><c:out value="${node.departmentName}"/>（<c:out value="${node.level.label}"/>）<c:if test="${not ns.last}"> / </c:if></c:forEach>
                </c:otherwise>
              </c:choose>
            </div>
            <c:if test="${not empty c.submitterLastName or not empty c.submitterPhone or not empty c.submitterEmail or not empty c.submitterAddress}">
              <div class="hint" style="margin-bottom:8px;">
                連絡先（都民が任意で入力・回答連絡専用・個人情報のため取り扱い注意）:
                <c:if test="${not empty c.submitterLastName}"> <c:out value="${c.submitterLastName}"/> <c:out value="${c.submitterFirstName}"/> 様</c:if>
                <c:if test="${not empty c.submitterPhone}"> / TEL: <c:out value="${c.submitterPhone}"/></c:if>
                <c:if test="${not empty c.submitterEmail}"> / Mail: <c:out value="${c.submitterEmail}"/></c:if>
                <c:if test="${not empty c.submitterAddress}"> / 住所: <c:out value="${c.submitterAddress}"/></c:if>
              </div>
            </c:if>
            <c:choose>
              <c:when test="${not empty c.responseText}">
                <div class="hint">回答済み（<c:out value="${c.respondedBy}"/> / <c:out value="${c.respondedAtDisplay}"/>）</div>
                <p style="white-space: pre-wrap;"><c:out value="${c.responseText}"/></p>
              </c:when>
              <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/admin/triage">
                  <input type="hidden" name="csrfToken" value="${csrfToken}">
                  <input type="hidden" name="action" value="respond">
                  <input type="hidden" name="caseId" value="${c.id}">
                  <label for="responseText-${c.id}">回答文（都民への回答・対応内容）</label>
                  <textarea id="responseText-${c.id}" name="responseText" required style="min-height:60px;" placeholder="現地確認の上、対応いたしました。等"></textarea>
                  <button type="submit" class="small secondary">回答を送信</button>
                </form>
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
        <c:if test="${not caseStatus.last}">
          <tr class="case-gap" aria-hidden="true"><td colspan="${isGeneralDesk ? 9 : 8}"></td></tr>
        </c:if>
      </c:forEach>
      <c:if test="${empty cases}">
        <tr><td colspan="9" class="hint">該当する案件はありません。</td></tr>
      </c:if>
      </tbody>
    </table>
    </div>
  </div>
</main>
</body>
</html>
