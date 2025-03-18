--step2
TRUNCATE TABLE tmp_WSＭＫＥ購買金額積上;
\set ON_ERROR_STOP true
\COPY  tmp_WSＭＫＥ購買金額積上 (     ＭＤ企業コード ,店舗コード ,電文ＳＥＱ番号 ,取引日時 ,購入日時 ,取引番号 ,グーポン会員ＩＤ ,取引区分 ,送信日時 ,応答日時 ,送信結果コード ,ランク判定用金額 ,ランク判定用ポイント支払金額 ) FROM 'dekk_buy_price_log_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WSＭＫＥ購買金額積上;
INSERT INTO WSＭＫＥ購買金額積上 (
     ＭＤ企業コード
    ,店舗コード
    ,電文ＳＥＱ番号
    ,取引日時
    ,購入日時
    ,取引番号
    ,グーポン会員ＩＤ
    ,取引区分
    ,送信日時
    ,応答日時
    ,送信結果コード
    ,ランク判定用金額
    ,ランク判定用ポイント支払金額
) 
SELECT 
     ＭＤ企業コード
    ,店舗コード
    ,電文ＳＥＱ番号
    ,取引日時
    ,購入日時
    ,取引番号
    ,グーポン会員ＩＤ
    ,取引区分
    ,送信日時
    ,NVL(応答日時, '-1') AS 応答日時
    , NVL(送信結果コード, '-1') AS 送信結果コード
    ,ランク判定用金額
    ,ランク判定用ポイント支払金額
FROM tmp_WSＭＫＥ購買金額積上;
TRUNCATE TABLE tmp_WSＭＫＥ購買金額積上;
