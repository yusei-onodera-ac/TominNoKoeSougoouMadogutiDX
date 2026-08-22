<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>管理画面ログイン - 都民の声プラットフォーム</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<header class="site-header">
  <p class="site-title">都民の声 次世代ハイブリッド仕分けプラットフォーム（管理画面）</p>
</header>
<main style="max-width: 420px;">
  <div class="card">
    <h1>職員ログイン</h1>
    <c:if test="${not empty error}">
      <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/admin/login">
      <input type="hidden" name="csrfToken" value="${csrfToken}">
      <label for="username">ユーザー名</label>
      <input type="text" id="username" name="username" autocomplete="username" required>
      <label for="password">パスワード</label>
      <input type="password" id="password" name="password" autocomplete="current-password" required>
      <button type="submit">ログイン</button>
    </form>
    <p class="hint">デモ用アカウント: admin / tominnokoe2026（README参照）</p>
  </div>
</main>
</body>
</html>
