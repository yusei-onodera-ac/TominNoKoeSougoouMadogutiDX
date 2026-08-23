/**
 * フォームの二重送信防止（連打対策）。
 * 「読み込み中に連打すると何件も送信されてしまう」という指摘を受けて追加。
 * submitイベントの捕捉フェーズでページ内の全フォームを監視し、
 * 1回目のsubmit以降はボタンを無効化して再送信をブロックする。
 * サーバー側のCSRFトークン検証等は変更しない、あくまでクライアント側の連打対策。
 */
(function () {
  document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!(form instanceof HTMLFormElement)) {
      return;
    }
    if (form.dataset.submitting === 'true') {
      // 既に送信済み（連打）。二重送信を防ぐため今回のsubmitは無視する。
      event.preventDefault();
      event.stopPropagation();
      return;
    }
    form.dataset.submitting = 'true';

    var buttons = form.querySelectorAll('button[type="submit"], input[type="submit"]');
    for (var i = 0; i < buttons.length; i++) {
      var btn = buttons[i];
      btn.disabled = true;
      if (btn.tagName === 'BUTTON') {
        btn.dataset.originalLabel = btn.textContent;
        btn.textContent = '送信中…';
      }
    }
  }, true);
})();
