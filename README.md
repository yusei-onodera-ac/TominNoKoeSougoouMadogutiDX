# 都民の声 次世代ハイブリッド仕分け・公表プラットフォーム（プロトタイプ）

東京都オープンデータハッカソン提出用プロトタイプ。Java（Value Object / Entity / Enum / Servlet / JSP）で実装。

- **都民向けポータル（citizen-portal）と行政向け管理画面（admin-portal）は、実際には全く別のシステムとして
  それぞれ独立に動作する**（別ポート・別Tomcatインスタンス・別デプロイ）。共有するのはドメインロジック
  （`common`モジュール）とデータベースのみ。
- 判定エンジンは現時点では**ルールベースのモック**（実LLM未接続）。UI・全体ロジックの検証を優先し、
  [`common/src/main/java/com/tominnokoe/classification/ClassificationEngine.java`](common/src/main/java/com/tominnokoe/classification/ClassificationEngine.java)
  の内部実装だけを差し替えれば、将来Gemini API等の実LLM呼び出しに移行できる構造になっている。
- 要件定義書のレビュー・改訂版は [`docs/requirements-improved.ja.md`](docs/requirements-improved.ja.md) を参照。

## プロジェクト構成（Maven複数モジュール）

```
pom.xml            親プロジェクト（packaging=pom）
common/             共通ドメインロジック（Entity/VO/Enum、判定エンジン、DB永続化、集計・可視化）
                    Webフレームワークには一切依存しない
citizen-portal/     都民向けポータル（独立アプリ、既定ポート8080）
admin-portal/       行政向け管理画面（独立アプリ、既定ポート8081）
data/               H2データベースファイル（実行時生成、gitignore対象）
docs/               改訂版要件定義書
```

## 動かし方

前提: JDK 17以上、Maven 3.9以上。

```bash
# 初回、または common モジュールを変更した場合に実行（ローカルリポジトリへインストール）
mvn install -DskipTests

# 都民向けポータル（別ターミナルで）
mvn -pl citizen-portal exec:java

# 行政向け管理画面（別ターミナルで）
mvn -pl admin-portal exec:java
```

どちらか一方だけを起動することも可能（本当に別システムとして独立に動く）。両者は同じ
`data/`配下のH2データベースファイルを共有するため、citizen-portal側で投稿した案件は
admin-portal側の管理画面にすぐ反映される。

起動後、ブラウザで以下へアクセス:

- 都民向けポータル: http://localhost:8080/
- 行政向け管理画面: http://localhost:8081/ （`/admin/login` へ自動リダイレクト）

初回起動時（データベースが空の場合のみ）、5つのベンチマークカテゴリを網羅するデモ用ケース（8件）を
自動投入するため、管理画面はすぐに中身のある状態で確認できる。

### デモ用管理者アカウント

- ユーザー名: `admin`
- パスワード: `tominnokoe2026`

（デモ用の単一アカウント。本番では都のSSO/LDAP等への置き換えが前提。詳細は改訂版要件定義書のセキュリティ設計節を参照）

## デモの見せ方（5つのベンチマークカテゴリ）

`/submit`（都民向けポータル、8080番）から以下のような投稿を行うと、それぞれ異なる分類・ルーティングが
確認できる（初回起動時に自動投入されるデモケースでも同じパターンを確認可能）:

| # | 入力例 | 想定される結果 |
| --- | --- | --- |
| 1 | 「環八通りの街路樹の剪定をお願いしたい」 | TOKYO_METROPOLITAN / 建設局 第一建設事務所（現場）→道路管理部→建設局の通知ツリー |
| 2 | 「杉並区立の公園のブランコが破損している」 | JURISDICTION_OTHER / 杉並区の窓口案内文を自動生成 |
| 3 | 「駅前の違法駐輪対策と都営バスの増便をお願いしたい」 | TOKYO_METROPOLITAN（複数局複合）/ 建設局＋交通局＋政策企画局（調整）の通知ツリー |
| 4 | 「都内の緑をもっと増やしてほしい」 | UNKNOWN / 政策企画局（総合窓口）による手動トリアージへ |
| 5 | 誹謗中傷・営業スパム的な文言 | 不適切キューへ隔離（各局へは通知せず、政策企画局の監査ビューのみに表示） |

管理画面（admin-portal、8081番）側で確認できる操作:

- `/admin/triage` — 一覧・フィルタ・手動アサイン
- `/admin/inappropriate` — 隔離監査ビュー・誤検知復元
- `/admin/governance` — 通知ツリーの階層別ステータス管理（クリックで進捗を進める）
- `/admin/guidance` — 区市町村向け案内文の確認・再生成
- `/admin/opendata` — 匿名化集計のグラフ表示＋CSV/JSON/Excelダウンロード

## 技術メモ

- 各アプリは組み込みTomcat（`tomcat-embed-core`/`-jasper`/`-el`）を`Main.java`から起動する自己完結型アプリ。外部Tomcatのインストール不要。
- JSTL（`<c:out>` 等によるXSS対策込み）を使用するため、ビルド時（`generate-resources`フェーズ）に実行時依存JARを各モジュールの `src/main/webapp/WEB-INF/lib/` へ複製している（各モジュールの `pom.xml` 参照。`tomcat-embed-*`/`jakarta.servlet`/`jakarta.el`/`common`本体はクラスの二重ロード事故を避けるため複製対象から除外し、親クラスローダ経由で解決させる）。このディレクトリはビルド生成物のため `.gitignore` 対象。
- 永続化はH2（純Java・ネイティブ依存なしの組み込みRDB、`data/tominnokoe.mv.db`）。JDBC URLを環境変数
  `DB_JDBC_URL`/`DB_USER`/`DB_PASSWORD` で差し替えれば、PostgreSQL等の本番RDBMSへも移行できる設計
  （[`common/src/main/java/com/tominnokoe/dao/Database.java`](common/src/main/java/com/tominnokoe/dao/Database.java)参照）。
- セキュリティ設計（XSS/CSRF対策、管理画面認証、セキュリティヘッダ、CSV/Excelインジェクション対策、簡易レート制限、監査ログ）の詳細は改訂版要件定義書を参照。

## 非対応事項・今後の拡張（本プロトタイプの現在のスコープ）

実LLM呼び出し、実際の通知配信（メール/Slack等）、行政職員による電話・FAX・窓口来訪・手紙・意見箱の
代筆入力ページ、全局アカウント対応、アクセシビリティ強化、LGWAN疑似アクセス制限などは順次実装中。
詳細は改訂版要件定義書を参照。
