<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>都民の声総合窓口メールフォーム - 都民の声プラットフォーム</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<header class="site-header">
  <p class="site-title">都民の声総合窓口 メールフォーム（次世代ハイブリッド仕分けプラットフォーム・プロトタイプ）</p>
</header>
<main>
  <p><a href="${pageContext.request.contextPath}/">&larr; トップへ戻る</a></p>
  <div class="grid-2">
    <div class="card">
      <h1>提言・意見・要望等のご入力</h1>

      <div class="notice-box" style="line-height:1.8;">
        <p><strong>ご入力の前に、以下をご確認ください。</strong></p>
        <ul style="margin:8px 0 0 20px; padding:0;">
          <li>お寄せいただいた個別のご意見等について、到達確認や内容に関するお電話等でのお問い合わせには、個人情報保護の観点からお答えできません。</li>
          <li>本文中にURLを記載されても、情報セキュリティ上の理由によりリンク先の内容は確認いたしません。</li>
          <li>企業の案内・営業活動、特定個人への誹謗中傷は受け付けできません。</li>
          <li>同一・同様の内容を大量または頻繁に送信された場合は受け付けをお断りする場合があります。</li>
          <li>都の業務以外の内容は、所管する国・区市町村等へお問い合わせください（本システムでは該当する場合に窓口をご案内します）。</li>
        </ul>
      </div>

      <c:if test="${not empty error}">
        <div class="error-box"><c:out value="${error}"/></div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/submit" id="submitForm">
        <input type="hidden" name="csrfToken" value="${csrfToken}">

        <label>区分：どちらか選択してください</label>
        <div style="margin-bottom:14px;">
          <label style="font-weight:normal; display:flex; align-items:flex-start; gap:6px;">
            <input type="radio" name="division" value="知事への提言" required style="width:auto; margin-top:4px;">
            <span>知事への提言 <span class="hint">— 都政に対する提言・意見をお寄せください</span></span>
          </label>
          <label style="font-weight:normal; display:flex; align-items:flex-start; gap:6px; margin-top:6px;">
            <input type="radio" name="division" value="要望・苦情" style="width:auto; margin-top:4px;">
            <span>要望・苦情 <span class="hint">— 都の事業や職員の対応についての要望・苦情</span></span>
          </label>
        </div>

        <label for="subject">タイトル（全角100文字まで）【任意】</label>
        <input type="text" id="subject" name="subject" maxlength="100" value="${fn:escapeXml(param.subject)}">

        <label for="body">コメントを入力してください（全角1,800文字まで）</label>
        <textarea id="body" name="body" maxlength="1800" required style="min-height:160px;">${fn:escapeXml(param.body)}</textarea>

        <label for="category">ジャンル（AIによる自動仕分けの精度向上のための任意項目・本システム独自）</label>
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

        <p class="hint">以下はすべて任意項目です。ご記入いただいた場合、担当局からの回答・確認のご連絡にのみ利用し、AIによる仕分け判定には一切使用しません。</p>

        <label for="lastName">氏名（全角）【任意】</label>
        <div style="display:flex; gap:8px;">
          <input type="text" id="lastName" name="lastName" maxlength="64" placeholder="姓" style="flex:1;">
          <input type="text" id="firstName" name="firstName" maxlength="64" placeholder="名" style="flex:1;">
        </div>

        <label for="address">住所（全角100文字まで）【任意】</label>
        <input type="text" id="address" name="address" maxlength="100">

        <label for="phone">電話番号（半角数字、ハイフンなし）【任意】</label>
        <input type="text" id="phone" name="phone" maxlength="15" pattern="[0-9]*">

        <label for="email">メールアドレス（半角）【任意】</label>
        <input type="text" id="email" name="email" maxlength="50">

        <button type="submit">確認画面へ進む</button>
      </form>
    </div>

    <div>
      <div class="card">
        <h2>類似する過去の対応事例</h2>
        <div id="similarCases" class="hint">コメントを入力すると、類似する過去の事例がここに表示されます。</div>
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
