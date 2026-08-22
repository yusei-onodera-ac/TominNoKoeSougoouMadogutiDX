<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>都民の声 次世代ハイブリッド仕分けプラットフォーム</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/style.css">
</head>
<body>
<a class="skip-link" href="#main-content">本文へスキップ</a>
<header class="site-header">
  <p class="site-title">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</p>
</header>
<main id="main-content">
  <div class="card">
    <h1>ようこそ</h1>
    <p>本サービスは、都政への提言・意見・要望等の受付・仕分け・伝達・公表業務を、東京都の公開オープンデータとルールベースの判定エンジン（将来的にLLMへ差し替え可能な設計）で支援するデモです。</p>
    <p>
      <a class="btn" href="<%=request.getContextPath()%>/submit">都民として意見を投稿する</a>
    </p>
  </div>
  <div class="card hint">
    <p>本ページ（都民向けポータル）は「メールフォーム・LINE」相当のオンライン投稿専用です。電話・FAX・窓口来訪・手紙・意見箱で寄せられた声は、行政職員が別システム（管理画面）から代筆入力し、同じ判定エンジンで処理します。</p>
    <p>行政職員の方は管理画面（別システムとして独立稼働、既定ではポート8081）からログインしてください。</p>
  </div>
</main>
<footer class="site-footer">都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）</footer>
</body>
</html>
