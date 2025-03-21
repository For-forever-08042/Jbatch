--step2
TRUNCATE TABLE tmp_WS取引顧客明細ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引顧客明細ワーク ( 営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,レシート明細行 ,ポイントレートコード ,ポイント対象フラグ ,単品ポイント ,期間限定倍率ポイント ,期間限定付与ポイント ,期間限定倍率ポイント失効期限  ,期間限定付与ポイント失効期限 ,分類倍率通常Ｐ ,分類通常Ｐクーポンコード ,分類倍率期間限定Ｐ ,分類期間限定Ｐクーポンコード ,分類期間限定Ｐ失効期限 ,クーポンポイント ,単品ポイント倍率 ,期間限定ポイント倍率 ,採用ポイント倍率 ,クーポンコード ,クーポン割引率 ,クーポン値引額 ,クーポン枚数 ,クーポン区分 ,利用単品クーポンシリアル番号 ,利用クーポン発行年月日 ,採用プレミアムポイント倍率 ,ポイント計算金額 ) FROM 'S4143.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
INSERT INTO WS取引顧客明細ワーク
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付
    ,会社コード
    ,店舗コード
    ,ノードＮＯ
    ,取引連番
    ,レシート明細行
    ,ポイントレートコード
    ,ポイント対象フラグ
    ,単品ポイント
    ,期間限定倍率ポイント
    ,期間限定付与ポイント
    ,期間限定倍率ポイント失効期限
    ,期間限定付与ポイント失効期限
    ,分類倍率通常Ｐ
    ,分類通常Ｐクーポンコード
    ,分類倍率期間限定Ｐ
    ,分類期間限定Ｐクーポンコード
    ,分類期間限定Ｐ失効期限
    ,クーポンポイント
    ,単品ポイント倍率
    ,期間限定ポイント倍率
    ,採用ポイント倍率
    ,クーポンコード
    ,クーポン割引率
    ,クーポン値引額
    ,クーポン枚数
    ,クーポン区分
    ,利用単品クーポンシリアル番号
    ,利用クーポン発行年月日
    ,採用プレミアムポイント倍率
    ,ポイント計算金額
FROM tmp_WS取引顧客明細ワーク;
TRUNCATE TABLE tmp_WS取引顧客明細ワーク;

