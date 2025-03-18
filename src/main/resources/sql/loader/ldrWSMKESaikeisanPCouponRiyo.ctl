OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'EcCouponRecovData_bmee_sjis.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＥ再計算Ｐクーポン利用
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     グーポン会員ＩＤ
    ,企業コード
    ,ＭＤ企業コード
    ,店舗コード                  CHAR "SUBSTR(:店舗コード, 11)"
    ,レジ番号
    ,取引番号
    ,取引日時
    ,ポイントクーポン企画コード
    ,着荷日時
)
