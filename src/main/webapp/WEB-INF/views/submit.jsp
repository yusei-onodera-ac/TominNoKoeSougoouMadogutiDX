<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>意見を投稿する - 都民の声プラットフォーム</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<header class="site-header">
  <p class="site-title">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</p>
</header>
<main>
  <p><a href="${pageContext.request.contextPath}/">&larr; トップへ戻る</a></p>
  <div class="grid-2">
    <div class="card">
      <h1>ご意見・ご要望の投稿</h1>
      <c:if test="${not empty error}">
        <div class="error-box"><c:out value="${error}"/></div>
      </c:if>
      <form method="post" action="${pageContext.request.contextPath}/submit" id="submitForm">
        <input type="hidden" name="csrfToken" value="${csrfToken}">

        <label for="category">ジャンル（任意選択）</label>
        <select id="category" name="category">
          <option value="">選択しない</option>
          <option value="道路・交通">道路・交通</option>
          <option value="交通">交通（バス等）</option>
          <option value="環境・みどり">環境・みどり</option>
          <option value="環境">環境（騒音・大気等）</option>
          <option value="福祉">福祉</option>
          <option value="上下水道">上下水道</option>
          <option value="都市整備">都市整備</option>
          <option value="河川・水害">河川・水害</option>
          <option value="教育">教育</option>
        </select>

        <label for="subject">件名</label>
        <input type="text" id="subject" name="subject" maxlength="100" required value="${fn:escapeXml(param.subject)}">

        <label for="body">本文</label>
        <textarea id="body" name="body" maxlength="2000" required>${fn:escapeXml(param.body)}</textarea>
        <p class="hint">個人情報（氏名・連絡先等）は本文に記載しないようご協力ください。</p>

        <button type="submit">投稿する</button>
      </form>
    </div>

    <div>
      <div class="card">
        <h2>類似する過去の対応事例</h2>
        <div id="similarCases" class="hint">件名・本文を入力すると、類似する過去の事例がここに表示されます。</div>
      </div>
      <div class="card">
        <h2>管轄についてのご案内</h2>
        <div id="jurisdictionPreview" class="hint">区市町村が管轄する内容の可能性がある場合、ここに案内が表示されます。</div>
      </div>
    </div>
  </div>
</main>
<footer class="site-footer">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</footer>
<script src="${pageContext.request.contextPath}/assets/suggest.js"></script>
</body>
</html>
