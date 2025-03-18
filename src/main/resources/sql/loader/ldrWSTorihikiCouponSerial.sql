--step2
TRUNCATE TABLE tmp_WS取引クーポンワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引クーポンワーク (   営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,利用フラグ ,クーポン区分 ,クーポンコード ,特典区分 ,割引率 ,値引額 ,付与ポイント ,枚数 ,クーポンシリアル番号 ,利用クーポン発行年月日  ) FROM 'S4121.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
INSERT INTO WS取引クーポンワーク
(
     営業日付 
    ,会社コード
    ,店舗コード
    ,ノードＮＯ
    ,取引連番
    ,利用フラグ
    ,クーポン区分
    ,クーポンコード
    ,特典区分
    ,割引率
    ,値引額
    ,付与ポイント
    ,枚数
    ,クーポンシリアル番号
    ,利用クーポン発行年月日
)
SELECT
     TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付
    ,会社コード
    ,店舗コード
    ,ノードＮＯ
    ,取引連番
    ,利用フラグ
    ,クーポン区分
    ,クーポンコード
    ,特典区分
    ,割引率
    ,値引額
    ,付与ポイント
    ,枚数
    ,クーポンシリアル番号
    ,利用クーポン発行年月日
FROM tmp_WS取引クーポンワーク;
--step4
TRUNCATE TABLE tmp_WS取引クーポンワーク;

