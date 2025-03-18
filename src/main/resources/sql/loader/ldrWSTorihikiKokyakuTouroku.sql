--step2
TRUNCATE TABLE tmp_WS取引顧客登録ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引顧客登録ワーク (  営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,登録区分 ,新顧客コード ,元顧客コード ,移行ポイント ,処理ステータス ,カード統合強制実行フラグ ) FROM 'S4130.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS取引顧客登録ワーク;
INSERT INTO WS取引顧客登録ワーク
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付,
    会社コード,
    店舗コード,
    ノードＮＯ,
    取引連番,
    登録区分,
    新顧客コード,
    TO_NUMBER(TRIM(REPLACE(CAST(元顧客コード AS TEXT), CHR(9), '')) ) AS 元顧客コード, 
    移行ポイント,
    処理ステータス,
    カード統合強制実行フラグ
FROM tmp_WS取引顧客登録ワーク;
TRUNCATE TABLE tmp_WS取引顧客登録ワーク;

