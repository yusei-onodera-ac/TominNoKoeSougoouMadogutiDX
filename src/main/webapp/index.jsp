<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>都民の声 次世代ハイブリッド仕分けプラットフォーム</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/style.css">
</head>
<body>
<header class="site-header">
  <p class="site-title">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</p>
</header>
<main>
  <div class="card">
    <h1>ようこそ</h1>
    <p>本サービスは、都政への提言・意見・要望等の受付・仕分け・伝達・公表業務を、東京都の公開オープンデータとルールベースの判定エンジン（将来的にLLMへ差し替え可能な設計）で支援するデモです。</p>
    <p>
      <a class="btn" href="<%=request.getContextPath()%>/submit">都民として意見を投稿する</a>
      &nbsp;
      <a class="btn secondary" href="<%=request.getContextPath()%>/admin">行政職員として管理画面を開く</a>
    </p>
  </div>
  <div class="card hint">
    <p>本プロトタイプは東京都オープンデータハッカソン向けのデモです。デジタル化・自動仕分けの対象はテキスト入力チャネル（メールフォーム・LINE相当）のみで、電話・来訪・FAX・手紙・意見箱等は対象外です。</p>
  </div>
</main>
<footer class="site-footer">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</footer>
</body>
</html>
