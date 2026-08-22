<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("currentPage", "manual-intake"); %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>電話・FAX・窓口 代筆入力 - 都民の声プラットフォーム管理画面</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content" style="max-width: 640px;">
  <div class="card">
    <h1>電話・FAX・窓口・手紙・意見箱 代筆入力</h1>
    <p class="hint">
      オンライン投稿（メールフォーム・LINE相当）以外のチャネルで寄せられた都民の声を、
      内容を受け取った職員がここから代筆入力します。入力後は都民本人による投稿と全く同じ
      判定エンジンで処理されます。
    </p>
    <c:if test="${not empty error}">
      <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/admin/manual-intake">
      <input type="hidden" name="csrfToken" value="${csrfToken}">

      <label for="intakeChannel">受付チャネル</label>
      <select id="intakeChannel" name="intakeChannel" required>
        <option value="">選択してください</option>
        <option value="PHONE">電話</option>
        <option value="FAX">FAX</option>
        <option value="VISIT">窓口来訪</option>
        <option value="LETTER">手紙</option>
        <option value="OPINION_BOX">意見箱</option>
      </select>

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

      <label for="body">本文（内容を要約して記入してください）</label>
      <textarea id="body" name="body" maxlength="2000" required style="min-height:140px;">${fn:escapeXml(param.body)}</textarea>

      <button type="submit">登録する</button>
    </form>
  </div>
</main>
</body>
</html>
