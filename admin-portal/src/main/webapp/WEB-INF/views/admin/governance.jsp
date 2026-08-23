<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("currentPage", "governance"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>通知・進捗管理 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content">
  <h1>通知・進捗管理</h1>
  <p class="hint">
    都民または職員が声を登録する（送信ボタンを押す）と、その瞬間に現場出先機関だけでなく、上位の
    部・局・総合窓口へも<strong>一斉に自動で</strong>通知されます（手動でボタンを押す必要はありません）。
    実際に対応するのは現場出先機関のみで、それ以外の部署への通知はあくまで
    「〇〇にこの内容の声が届きました」というお知らせ（状況共有）です。対応の要否は分けて伝わります：
    現場出先機関を特定できた場合は、それ以外の部署には対応不要のお知らせのみが届き、特定できなかった
    場合のみ総合窓口に対応をお願いするメッセージが届きます。
    以下のボタンは、通知を受けた各部署が「内容を確認した（確認済み）」「対応が完了した（対応完了）」ことを
    記録するための操作です。
  </p>

  <c:forEach var="c" items="${cases}">
    <div class="card">
      <h2><c:out value="${c.id}"/>: <c:out value="${c.subject}"/></h2>
      <div class="table-scroll">
      <table>
        <thead><tr><th scope="col">階層</th><th scope="col">部署</th><th scope="col">目的</th><th scope="col">ステータス</th><th scope="col"></th></tr></thead>
        <tbody>
        <c:forEach var="node" items="${c.classification.routing.governanceNotificationTree}">
          <c:set var="st" value="${c.notificationStatuses[node.departmentName]}"/>
          <c:set var="stValue" value="${empty st ? 'PENDING' : st}"/>
          <c:set var="stLabel" value="${empty st ? '未通知' : st.label}"/>
          <tr>
            <td style="white-space:nowrap;"><c:out value="${node.level.label}"/></td>
            <td class="col-wrap"><c:out value="${node.departmentName}"/></td>
            <td class="col-wrap"><c:out value="${node.purpose}"/></td>
            <td>
              <span class="status-pill ${stValue}"><c:out value="${stLabel}"/></span>
            </td>
            <td>
              <c:choose>
                <c:when test="${stValue == 'DONE'}">
                  <span class="hint">対応完了済み</span>
                </c:when>
                <c:otherwise>
                  <form method="post" action="${pageContext.request.contextPath}/admin/governance">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="caseId" value="${c.id}">
                    <input type="hidden" name="departmentName" value="${node.departmentName}">
                    <c:choose>
                      <c:when test="${stValue == 'ACKED'}">
                        <button type="submit" class="small secondary">対応完了にする</button>
                      </c:when>
                      <c:when test="${stValue == 'NOTIFIED'}">
                        <button type="submit" class="small secondary">確認しました</button>
                      </c:when>
                      <c:otherwise>
                        <%-- 通常は登録時点で自動的にNOTIFIEDになるため、ここに来るのは
                             自動通知未実施の既存データ等の想定外ケースのみ。 --%>
                        <button type="submit" class="small secondary">通知する</button>
                      </c:otherwise>
                    </c:choose>
                  </form>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
      </div>
    </div>
  </c:forEach>
  <c:if test="${empty cases}">
    <div class="card hint">通知対象の案件はありません。</div>
  </c:if>
</main>
</body>
</html>
