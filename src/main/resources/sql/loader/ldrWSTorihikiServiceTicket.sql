--step2
TRUNCATE TABLE tmp_WS取引サービス券ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引サービス券ワーク (  営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,利用フラグ ,サービス券シリアル番号 ,発行番号 ,利用サービス券発行年月日 ) FROM 'S4140.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS取引サービス券ワーク;
INSERT INTO WS取引サービス券ワーク
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付
    ,会社コード
    ,店舗コード
    ,ノードＮＯ
    ,取引連番
    ,利用フラグ
    ,サービス券シリアル番号
    ,発行番号
    ,利用サービス券発行年月日
FROM tmp_WS取引サービス券ワーク;
--step4
TRUNCATE TABLE tmp_WS取引サービス券ワーク;

