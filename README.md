# 都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）

東京都オープンデータハッカソン提出用プロトタイプ。Java（Value Object / Entity / Enum / Servlet / JSP）で実装。

- 判定エンジンは現時点では**ルールベースのモック**（実LLM未接続）。UI・全体ロジックの検証を優先し、
  [`src/main/java/com/tominnokoe/classification/ClassificationEngine.java`](src/main/java/com/tominnokoe/classification/ClassificationEngine.java)
  の内部実装だけを差し替えれば、将来Gemini API等の実LLM呼び出しに移行できる構造になっている。
- 要件定義書のレビュー・改訂版は [`docs/requirements-improved.ja.md`](docs/requirements-improved.ja.md) を参照。

## 動かし方

前提: JDK 17以上、Maven 3.9以上。

```bash
mvn compile exec:java
```

起動後、ブラウザで以下へアクセス:

- 都民向けポータル: http://localhost:8080/
- 行政向け管理画面: http://localhost:8080/admin （要ログイン）

初回起動時に `data/cases.json` に投入済みのデモ用ケース（8件）が既に入っているため、
管理画面はすぐに中身のある状態で確認できる。

### デモ用管理者アカウント

- ユーザー名: `admin`
- パスワード: `tominnokoe2026`

（デモ用の単一アカウント。本番では都のSSO/LDAP等への置き換えが前提。詳細は改訂版要件定義書のセキュリティ設計節を参照）

## デモの見せ方（5つのベンチマークカテゴリ）

`/submit` から以下のような投稿を行うと、それぞれ異なる分類・ルーティングが確認できる
（`data/cases.json` に投入済みのケース C-2026-0001〜0008 でも同じパターンを確認可能）:

| # | 入力例 | 想定される結果 |
| --- | --- | --- |
| 1 | 「環八通りの街路樹の剪定をお願いしたい」 | TOKYO_METROPOLITAN / 建設局 第一建設事務所（現場）→道路管理部→建設局の通知ツリー |
| 2 | 「杉並区立の公園のブランコが破損している」 | JURISDICTION_OTHER / 杉並区の窓口案内文を自動生成 |
| 3 | 「駅前の違法駐輪対策と都営バスの増便をお願いしたい」 | TOKYO_METROPOLITAN（複数局複合）/ 建設局＋交通局＋政策企画局（調整）の通知ツリー |
| 4 | 「都内の緑をもっと増やしてほしい」 | UNKNOWN / 政策企画局（総合窓口）による手動トリアージへ |
| 5 | 誹謗中傷・営業スパム的な文言 | 不適切キューへ隔離（各局へは通知せず、政策企画局の監査ビューのみに表示） |

管理画面側で確認できる操作:

- `/admin/triage` — 一覧・フィルタ・手動アサイン
- `/admin/inappropriate` — 隔離監査ビュー・誤検知復元
- `/admin/governance` — 通知ツリーの階層別ステータス管理（クリックで進捗を進める）
- `/admin/guidance` — 区市町村向け案内文の確認・再生成
- `/admin/opendata` — 匿名化集計のグラフ表示＋CSV/JSON/Excelダウンロード

## 技術メモ

- 組み込みTomcat（`tomcat-embed-core`/`-jasper`/`-el`）を [`Main.java`](src/main/java/com/tominnokoe/Main.java) から起動する自己完結型アプリ。外部Tomcatのインストール不要。
- JSTL（`<c:out>` 等によるXSS対策込み）を使用するため、ビルド時（`generate-resources`フェーズ）に実行時依存JARを `src/main/webapp/WEB-INF/lib/` へ複製している（`pom.xml` 参照。`tomcat-embed-*`/`jakarta.servlet`/`jakarta.el` はクラスの二重ロード事故を避けるため複製対象から除外）。このディレクトリはビルド生成物のため `.gitignore` 対象。
- 永続化はネイティブ依存のあるDBを避け、`data/cases.json`（案件データ）・`data/audit_log.json`（監査ログ、gitignore対象）へのJSONファイル書き込みで行っている。
- セキュリティ設計（XSS/CSRF対策、管理画面認証、セキュリティヘッダ、CSV/Excelインジェクション対策、簡易レート制限、監査ログ）の詳細は改訂版要件定義書を参照。

## 非対応事項（本プロトタイプのスコープ外）

本番DB、実LLM呼び出し、実際の通知配信（メール/Slack等）、多要素認証、SSO/LDAP連携、
アクセシビリティ監査、ペネトレーションテスト、音声電話（Speech-to-Text）・紙/FAX（AI-OCR）等の
物理インジェスト連携。詳細は改訂版要件定義書を参照。
