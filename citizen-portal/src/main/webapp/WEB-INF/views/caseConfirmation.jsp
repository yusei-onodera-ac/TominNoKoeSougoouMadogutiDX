<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>受付完了 - 都民の声プラットフォーム</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<header class="site-header">
  <p class="site-title">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</p>
</header>
<main>
  <p><a href="${pageContext.request.contextPath}/">&larr; トップへ戻る</a></p>
  <div class="card">
    <h1>ご意見を受け付けました</h1>
    <p>受付番号: <strong><c:out value="${caseEntity.id}"/></strong></p>
    <p><strong>件名:</strong> <c:out value="${caseEntity.subject}"/></p>

    <c:choose>
      <c:when test="${caseEntity.classification.inappropriate}">
        <div class="notice-box">
          現在、内容を確認しております。追ってご案内する場合があります。
        </div>
      </c:when>
      <c:when test="${caseEntity.classification.classificationType == 'JURISDICTION_OTHER'}">
        <div class="notice-box">
          <c:out value="${caseEntity.classification.externalGuidance.explanationText}"/>
        </div>
      </c:when>
      <c:when test="${caseEntity.classification.classificationType == 'TOKYO_METROPOLITAN'}">
        <div class="notice-box">
          <p>想定される担当局: <strong><c:out value="${caseEntity.classification.routing.primaryBureau}"/></strong></p>
          <c:if test="${not empty caseEntity.classification.routing.primaryDivision}">
            <p>担当部: <c:out value="${caseEntity.classification.routing.primaryDivision}"/></p>
          </c:if>
          <c:if test="${not empty caseEntity.classification.routing.actionOwner}">
            <p>現場対応窓口: <c:out value="${caseEntity.classification.routing.actionOwner}"/></p>
          </c:if>
        </div>
      </c:when>
      <c:otherwise>
        <div class="notice-box">
          担当部署を確認中です。政策企画局（都民の声総合窓口）にて内容を精査の上、対応いたします。
        </div>
      </c:otherwise>
    </c:choose>

    <p class="hint">本内容は自動判定によるものであり、内容確認の上で担当部署が変更される場合があります。</p>
    <p class="hint">個人情報保護の観点から、本受付番号に関する到達確認等のお問い合わせには個別にお答えできません。ご連絡先をご入力いただいた場合は、担当局からの回答のみに利用します。</p>
  </div>
</main>
<footer class="site-footer">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</footer>
</body>
</html>
