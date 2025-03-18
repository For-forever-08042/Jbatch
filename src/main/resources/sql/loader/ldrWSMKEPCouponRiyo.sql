--step2
TRUNCATE TABLE tmp_WSＭＫＥＰクーポン利用;
\set ON_ERROR_STOP true
\COPY tmp_WSＭＫＥＰクーポン利用 (   グーポン会員ＩＤ ,企業コード ,ＭＤ企業コード ,店舗コード ,レジ番号 ,取引番号 ,取引日時 ,ポイントクーポン企画コード) FROM 'EcCouponData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WSＭＫＥＰクーポン利用;
INSERT INTO WSＭＫＥＰクーポン利用 (
     グーポン会員ＩＤ
    ,企業コード
    ,ＭＤ企業コード
    ,店舗コード
    ,レジ番号
    ,取引番号
    ,取引日時
    ,ポイントクーポン企画コード
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
FROM tmp_WSＭＫＥＰクーポン利用;
TRUNCATE TABLE tmp_WSＭＫＥＰクーポン利用;
