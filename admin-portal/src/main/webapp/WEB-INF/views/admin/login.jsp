<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>管理画面ログイン - 都民の声総合窓口DX化</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<header class="site-header">
  <p class="site-title">都民の声総合窓口DX化（管理画面）</p>
</header>
<main id="main-content" style="max-width: 460px;">
  <div class="card">
    <h1>職員ログイン</h1>
    <p class="hint">所属局を選択してログインしてください。「政策企画局」は都民の声総合窓口として全案件を横断的に確認できます。</p>
    <c:if test="${not empty error}">
      <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/admin/login">
      <input type="hidden" name="csrfToken" value="${csrfToken}">
      <label for="bureau">所属局</label>
      <select id="bureau" name="bureau" required autofocus>
        <option value="">選択してください</option>
        <c:forEach var="b" items="${bureauNames}">
          <option value="${b}"><c:out value="${b}"/></option>
        </c:forEach>
      </select>
      <label for="password">パスワード</label>
      <input type="password" id="password" name="password" autocomplete="current-password" required>
      <button type="submit">ログイン</button>
    </form>
    <p class="hint">デモ用共通パスワード: tominnokoe2026（README参照。本番では局・職員ごとの認証基盤への置き換えが前提）</p>
  </div>
</main>
</body>
</html>
