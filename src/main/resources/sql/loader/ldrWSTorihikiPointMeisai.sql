--step2
TRUNCATE TABLE tmp_WS取引ポイント明細ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引ポイント明細ワーク (営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,ポイント明細行 ,企画ＩＤ ,ポイントカテゴリ ,ポイント種別 ,付与区分 ,付与ポイント数 ,ＪＡＮコード ,商品購入数 ,買上高ポイント種別 ,対象金額 ,商品パーセントポイント付与率  ,期間限定ポイントの有効期限  ,非購買フラグ   ) FROM 'S4142.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS取引ポイント明細ワーク;
INSERT INTO WS取引ポイント明細ワーク
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付
    ,会社コード
    ,店舗コード
    ,ノードＮＯ
    ,取引連番
    ,ポイント明細行
    ,TO_NUMBER(COALESCE(NULLIF(TRIM(CAST(企画ＩＤ AS VARCHAR)),''), '0') ) AS 企画ＩＤ
    ,ポイントカテゴリ
    ,ポイント種別
    ,付与区分
    ,付与ポイント数
    ,ＪＡＮコード
    ,商品購入数
    ,買上高ポイント種別
    ,対象金額
    ,TO_NUMBER(COALESCE(NULLIF(TRIM(CAST(商品パーセントポイント付与率 AS VARCHAR)),''), '0') ) AS 商品パーセントポイント付与率
    ,TO_NUMBER(TO_CHAR(期間限定ポイントの有効期限, 'YYYYMMDD') ) AS 期間限定ポイントの有効期限
    ,TO_NUMBER(NULLIF(TRIM(CAST(非購買フラグ AS VARCHAR)),'') ) AS 非購買フラグ
FROM tmp_WS取引ポイント明細ワーク;
TRUNCATE TABLE tmp_WS取引ポイント明細ワーク;

