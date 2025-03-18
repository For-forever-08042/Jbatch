--step2
\set ON_ERROR_STOP true
\COPY tmp_WSＭＫＥ再計算Ｐクーポン利用 (   グーポン会員ＩＤ ,企業コード ,ＭＤ企業コード ,店舗コード ,レジ番号 ,取引番号 ,取引日時 ,ポイントクーポン企画コード,着荷日時) FROM 'EcCouponRecovData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );

INSERT INTO WSＭＫＥ再計算Ｐクーポン利用(
     グーポン会員ＩＤ
    ,企業コード
    ,ＭＤ企業コード
    ,店舗コード
    ,レジ番号
    ,取引番号
    ,取引日時
    ,ポイントクーポン企画コード
    ,着荷日時
) 
SELECT 
     グーポン会員ＩＤ
    ,企業コード
    ,ＭＤ企業コード
    ,SUBSTR(店舗コード, 11) AS 店舗コード
    ,レジ番号
    ,取引番号
    ,取引日時
    ,ポイントクーポン企画コード
    ,着荷日時
FROM tmp_WSＭＫＥ再計算Ｐクーポン利用;
TRUNCATE TABLE tmp_WSＭＫＥ再計算Ｐクーポン利用;
