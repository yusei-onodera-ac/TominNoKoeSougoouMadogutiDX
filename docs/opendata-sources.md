# 利用オープンデータの出典一覧

本プロトタイプ（`common/src/main/resources/data/*.json`）は、要件定義書
（[`requirements-improved.ja.md`](requirements-improved.ja.md) 1-1節）で定義された5種類のオープンデータを
**模したモックデータ**を使用している。実際のデータをそのまま投入しているわけではない
（フォーマット統一・件数調整・ダミーの連絡先ドメインへの置換等のクレンジングを行うため）。

このページは、それぞれのモックデータが「実際にどのオープンデータを参照して作られたものか」を
出典として明記するためのものである。2026年8月23日時点で実際に
[東京都オープンデータカタログサイト](https://catalog.data.metro.tokyo.lg.jp/)等を検索し、該当する実データを
確認・取得した記録を残す（`docs/opendata-sources/` 配下に実データのサンプルを保存している）。

## 重要な前提の訂正

要件定義書1-1節の表は「5データセットがそれぞれ1本のファイルとして存在する」という前提で書かれているが、
**実際のオープンデータカタログには、政策企画局・総務局がまとめて公開する単一のファイルは存在しない**。
実態は、各局・各事業所がそれぞれ個別にデータセットを公開しており、同じ種類の情報でも局の数だけ
（またはそれ以上）ファイルが分散している。以下は、その中から実際に確認できた代表例である。

## データセット一覧と出典

### 1. 都政への提言・意見・要望等の受付・対応状況

- **実態**: 政策企画局がまとめて公開する統合データは無く、局ごとに個別の「◯◯局へ寄せられた都民の声」
  データセットが存在する（[検索結果: 6,000件超](https://catalog.data.metro.tokyo.lg.jp/dataset?q=%E9%83%BD%E6%B0%91%E3%81%AE%E5%A3%B0)）。
- **確認した実データ例**: 「[会計管理局へ寄せられた都民の声](https://catalog.data.metro.tokyo.lg.jp/dataset/t000016d1700000002)」
  （東京都会計管理局、CC BY 4.0）
  - 直リンク（平成29年10月分）: <https://www.kaikeikanri.metro.tokyo.lg.jp/2910tominnokoe.csv>
  - ローカル保存: [`docs/opendata-sources/kaikeikanri_tominnokoe_2910.csv`](opendata-sources/kaikeikanri_tominnokoe_2910.csv)
  - 内容: 提言・意見・苦情・要望・相談・問合せ等の件数集計（個票ではない）
- 他に確認できた例: 「[下水道局都民の声窓口に寄せられた都民の声](https://catalog.data.metro.tokyo.lg.jp/dataset/t000020d0000000031)」（東京都下水道局）

### 2. 東京都組織規程・事務分掌データ

- **実態**: 総務局がまとめて公開する統合データは無く、局・事業所ごとに個別の組織概要・分掌事務データが
  存在する（[検索結果: 383件](https://catalog.data.metro.tokyo.lg.jp/dataset?q=%E4%BA%8B%E5%8B%99%E5%88%86%E6%8E%8C)）。
- **確認した実データ例**: 「[組織概要_分掌事務](https://catalog.data.metro.tokyo.lg.jp/dataset/t000014d2000000010)」
  （東京都建設局、CC BY 4.0）
  - 直リンク（各課の担当事務及び連絡先一覧表）: <https://www.opendata.metro.tokyo.lg.jp/kensetsu/R3/bunsyojimu.csv>
  - ローカル保存: [`docs/opendata-sources/kensetsu_bunsyojimu_r3.csv`](opendata-sources/kensetsu_bunsyojimu_r3.csv)
  - 内容: 課名・担当名・担当事務・電話番号・メールアドレス
  - `org_jurisdiction_rules.json` のフィールド構成（局/部/課＋所掌事務＋連絡先）はこの形式を参考にしている
- 他に確認できた例: 「[東京都第一市街地整備事務所 組織及び事務分掌](https://catalog.data.metro.tokyo.lg.jp/dataset/t000008d1900000010)」（東京都都市整備局）

### 3. 東京都 都有施設一覧データ

- **実データ**: 「[公共施設一覧](https://catalog.data.metro.tokyo.lg.jp/dataset/t000029d0000000030)」
  （東京都デジタルサービス局、CC BY 4.0）
  - 直リンク: <https://www.opendata.metro.tokyo.lg.jp/suisyoudataset/130001_public_facility.csv>
  - ローカル保存: [`docs/opendata-sources/130001_public_facility.csv`](opendata-sources/130001_public_facility.csv)（156件、Shift_JIS）
  - 内容: 内閣官房情報通信技術（IT）総合戦略室が2017年12月22日公開の「推奨データセット」仕様に準拠。
    都立図書館・都立文化施設・都立公園庭園を収録
  - 5データセットの中で唯一、単一ファイルとして実在が確認できたもの

### 4. 東京都管理道路（都道）路線・境界データ

- **実データ**: 「[都道の街路樹](https://catalog.data.metro.tokyo.lg.jp/dataset/t000014d2000000029)」
  （東京都建設局、CC BY 4.0）
  - 直リンク（23区分）: <https://www.opendata.metro.tokyo.lg.jp/kensetsu/tokyo_gairoju.csv>（約13MB、144,183件）
  - 直リンク（多摩地域分）: `t000014d2000000029` データセットページから別リソースとして取得可能（未取得）
  - ローカル保存: [`docs/opendata-sources/tokyo_gairoju_23ku_sample200.csv`](opendata-sources/tokyo_gairoju_23ku_sample200.csv)
    （容量の都合上、全144,183件のうち先頭200件のサンプルのみ保存。UTF-8変換済み）
  - 内容: 樹種・樹高・幹周・行政区・路線名（例:「日本橋芝浦大森線」）・緯度経度
  - 路線名そのものの単独マスタは未確認。建設局の「[東京都道路現況図公開システム](https://www.genkyozu.metro.tokyo.lg.jp/top/)」が
    路線・境界情報の実務上の情報源と思われるが、内容までは未確認
  - `tokyo_roads.json` の `routeName`/`routeNumber`/`managingOffice` は、この街路樹データの路線名フィールドと
    道路現況図公開システムを参考にしている

### 5. 東京都 区市町村一覧・コードデータ

- **実データ**: 「[都道府県コード及び市区町村コード](https://www.soumu.go.jp/denshijiti/code.html)」（令和6年1月1日更新）
  （**総務省**、東京都オープンデータカタログではなく国のデータ）
  - 直リンク: <https://www.soumu.go.jp/main_content/000925835.xlsx>
  - ローカル保存: [`docs/opendata-sources/soumu_todoufuken_shikuchouson_code.xlsx`](opendata-sources/soumu_todoufuken_shikuchouson_code.xlsx)
  - 内容: 全国の都道府県コード・市区町村コード（JIS X 0402準拠）。東京都分（62市区町村）を抽出して利用
  - `municipalities.json` の `localGovCode`/`localGovName` はこのコード体系に準拠している

## ライセンス

東京都オープンデータカタログサイト掲載データは基本的に
[クリエイティブ・コモンズ 表示 4.0（CC BY 4.0）](https://creativecommons.org/licenses/by/4.0/deed.ja)。
総務省「都道府県コード及び市区町村コード」は
[政府標準利用規約（第2.0版）](https://www.digital.go.jp/resources/open_data/definition)に準拠する。
いずれも出典明記の上での二次利用・改変・商用利用が可能。

## 未確認・要継続調査

- 都道の路線・境界そのもの（線形・幅員等のGISデータ）の確定的な出典
- 政策企画局が総合窓口分として集計・公表する「都民の声」の統合レポート（月報・年報）の直接URL
  （[改訂版要件定義書 0-2節](requirements-improved.ja.md)に言及がある年間3.3〜3.4万件の集計の一次情報）

時間の都合で上記2点は未確認のまま。継続して調査するか、モックデータである旨を要件定義書・提出資料に
明記するかを判断されたい。
