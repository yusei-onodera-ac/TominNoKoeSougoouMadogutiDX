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
  <script defer src="${pageContext.request.contextPath}/assets/form-guard.js"></script>
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<%@ include file="/WEB-INF/views/common/adminNav.jspf" %>
<main id="main-content" style="max-width: 640px;">
  <div class="card">
    <h1>電話・FAX・窓口・手紙・意見箱 代筆入力</h1>
    <p class="hint">
      オンライン投稿（メールフォーム・LINE相当）以外のチャネルで寄せられた都民の声を、
      内容を受け取った職員がここから代筆入力します。項目は都民本人が直接入力するフォームと
      同一で、受付チャネルの選択のみが追加されています。入力後は都民本人による投稿と全く同じ
      判定エンジンで処理され、登録と同時にガバナンス通知チェーンの全階層（現場出先機関〜局〜
      総合窓口）へ自動で通知されます。
    </p>
    <c:if test="${not empty error}">
      <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/admin/manual-intake">
      <input type="hidden" name="csrfToken" value="${csrfToken}">

      <label for="intakeChannel">受付チャネル</label>
      <select id="intakeChannel" name="intakeChannel" required>
        <option value="">選択してください</option>
        <option value="PHONE" ${param.intakeChannel == 'PHONE' ? 'selected' : ''}>電話</option>
        <option value="FAX" ${param.intakeChannel == 'FAX' ? 'selected' : ''}>FAX</option>
        <option value="VISIT" ${param.intakeChannel == 'VISIT' ? 'selected' : ''}>窓口来訪</option>
        <option value="LETTER" ${param.intakeChannel == 'LETTER' ? 'selected' : ''}>手紙</option>
        <option value="OPINION_BOX" ${param.intakeChannel == 'OPINION_BOX' ? 'selected' : ''}>意見箱</option>
      </select>

      <fieldset style="border:1px solid var(--border); border-radius:6px; padding:12px 14px; margin-bottom:14px;">
        <legend style="font-size:13px; font-weight:bold; padding:0 4px;">区分：どちらか選択してください</legend>
        <label style="font-weight:normal; display:flex; align-items:flex-start; gap:6px;">
          <input type="radio" name="division" value="知事への提言" ${param.division == '知事への提言' ? 'checked' : ''} required style="width:auto; margin-top:4px;">
          <span>知事への提言 <span class="hint">— 都政に対する提言・意見</span></span>
        </label>
        <label style="font-weight:normal; display:flex; align-items:flex-start; gap:6px; margin-top:6px;">
          <input type="radio" name="division" value="要望・苦情" ${param.division == '要望・苦情' ? 'checked' : ''} style="width:auto; margin-top:4px;">
          <span>要望・苦情 <span class="hint">— 都の事業や職員の対応についての要望・苦情</span></span>
        </label>
      </fieldset>

      <label for="subject">タイトル（全角100文字まで）【任意】</label>
      <input type="text" id="subject" name="subject" maxlength="100" value="${fn:escapeXml(param.subject)}">

      <label for="body">コメント（内容を要約して記入してください・全角1,800文字まで）</label>
      <textarea id="body" name="body" maxlength="1800" required style="min-height:140px;">${fn:escapeXml(param.body)}</textarea>

      <label for="category">ジャンル（AIによる自動仕分けの精度向上のための任意項目）</label>
      <select id="category" name="category">
        <option value="">選択しない</option>
        <option value="道路・交通" ${param.category == '道路・交通' ? 'selected' : ''}>道路・交通</option>
        <option value="交通" ${param.category == '交通' ? 'selected' : ''}>交通（バス等）</option>
        <option value="環境・みどり" ${param.category == '環境・みどり' ? 'selected' : ''}>環境・みどり</option>
        <option value="環境" ${param.category == '環境' ? 'selected' : ''}>環境（騒音・大気等）</option>
        <option value="福祉" ${param.category == '福祉' ? 'selected' : ''}>福祉</option>
        <option value="上下水道" ${param.category == '上下水道' ? 'selected' : ''}>上下水道</option>
        <option value="都市整備" ${param.category == '都市整備' ? 'selected' : ''}>都市整備</option>
        <option value="河川・水害" ${param.category == '河川・水害' ? 'selected' : ''}>河川・水害</option>
        <option value="教育" ${param.category == '教育' ? 'selected' : ''}>教育</option>
      </select>

      <p class="hint">以下はすべて任意項目です。ご記入いただいた場合、担当局からの回答・確認の連絡にのみ利用し、AIによる仕分け判定には一切使用しません。</p>

      <label for="lastName">氏名（全角）【任意】</label>
      <div style="display:flex; gap:8px;">
        <input type="text" id="lastName" name="lastName" maxlength="64" placeholder="姓" value="${fn:escapeXml(param.lastName)}" style="flex:1;">
        <input type="text" id="firstName" name="firstName" maxlength="64" placeholder="名" value="${fn:escapeXml(param.firstName)}" style="flex:1;">
      </div>

      <label for="address">住所（全角100文字まで）【任意】</label>
      <input type="text" id="address" name="address" maxlength="100" value="${fn:escapeXml(param.address)}">

      <label for="phone">電話番号（半角数字、ハイフンなし）【任意】</label>
      <input type="text" id="phone" name="phone" maxlength="15" pattern="[0-9]*" value="${fn:escapeXml(param.phone)}">

      <label for="email">メールアドレス（半角）【任意】</label>
      <input type="text" id="email" name="email" maxlength="50" value="${fn:escapeXml(param.email)}">

      <button type="submit">登録する</button>
    </form>
  </div>
</main>
</body>
</html>
