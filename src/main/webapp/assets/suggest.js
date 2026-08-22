(function () {
  "use strict";

  var subjectEl = document.getElementById("subject");
  var bodyEl = document.getElementById("body");
  var categoryEl = document.getElementById("category");
  var similarCasesEl = document.getElementById("similarCases");
  var jurisdictionEl = document.getElementById("jurisdictionPreview");

  if (!subjectEl || !bodyEl) {
    return;
  }

  var debounceTimer = null;

  function debounceFetch() {
    if (debounceTimer) {
      clearTimeout(debounceTimer);
    }
    debounceTimer = setTimeout(fetchSuggestions, 400);
  }

  function fetchSuggestions() {
    var subject = subjectEl.value || "";
    var body = bodyEl.value || "";
    if (subject.length < 2 && body.length < 2) {
      return;
    }
    var params = new URLSearchParams();
    params.set("subject", subject);
    params.set("body", body);
    params.set("category", categoryEl ? categoryEl.value : "");

    fetch(contextPath() + "/api/suggest", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString()
    })
      .then(function (res) { return res.json(); })
      .then(renderSuggestions)
      .catch(function () { /* デモ用途のため通信エラーは無視 */ });
  }

  function contextPath() {
    // このJSは "/submit" 配下から読み込まれる想定。ルートコンテキストならそのまま。
    return "";
  }

  function renderSuggestions(data) {
    renderSimilarCases(data.similarCases || []);
    renderJurisdictionPreview(data.jurisdictionPreview || {});
  }

  function clearChildren(el) {
    while (el.firstChild) {
      el.removeChild(el.firstChild);
    }
  }

  function renderSimilarCases(cases) {
    clearChildren(similarCasesEl);
    if (cases.length === 0) {
      similarCasesEl.textContent = "類似する事例は見つかりませんでした。";
      return;
    }
    var list = document.createElement("ul");
    cases.forEach(function (c) {
      var li = document.createElement("li");
      var strong = document.createElement("strong");
      strong.textContent = c.subject || "";
      li.appendChild(strong);
      li.appendChild(document.createTextNode(" （担当: " + (c.handledBureau || "-") + "）"));
      if (c.responseSummary) {
        var p = document.createElement("div");
        p.className = "hint";
        p.textContent = c.responseSummary;
        li.appendChild(p);
      }
      list.appendChild(li);
    });
    similarCasesEl.appendChild(list);
  }

  function renderJurisdictionPreview(preview) {
    clearChildren(jurisdictionEl);
    if (preview.likelyMunicipality) {
      var box = document.createElement("div");
      box.className = "notice-box";
      box.textContent = "本件は「" + preview.municipalityName + "」の管轄事項の可能性があります。"
        + "投稿後、" + preview.consultationDesk + " への案内をご案内する場合があります。";
      jurisdictionEl.appendChild(box);
      return;
    }
    if (preview.matchedFacility || preview.matchedRoad) {
      var box2 = document.createElement("div");
      box2.className = "hint";
      box2.textContent = "都が管理する"
        + (preview.matchedFacility ? "施設「" + preview.matchedFacility + "」" : "道路「" + preview.matchedRoad + "」")
        + "に関連する内容として認識されました。";
      jurisdictionEl.appendChild(box2);
      return;
    }
    jurisdictionEl.textContent = "区市町村が管轄する内容の可能性がある場合、ここに案内が表示されます。";
  }

  subjectEl.addEventListener("input", debounceFetch);
  bodyEl.addEventListener("input", debounceFetch);
})();
