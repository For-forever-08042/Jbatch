
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

INSERT /*+ APPEND */ INTO WMＤＭ対象者リスト 
SELECT
顧客番号
, ＧＯＯＰＯＮ番号 
, 会員番号 
, 店舗コード
, 会社コード 
FROM
(
SELECT
  TBLB.顧客番号
  , TBLA.ＧＯＯＰＯＮ番号 
  , TBLB.会員番号 
  , TBLA.店舗コード
  , TBLA.会社コード 
  , ROW_NUMBER() OVER (PARTITION BY TBLA.ＧＯＯＰＯＮ番号,TBLA.店舗コード,TBLA.会社コード ORDER BY TBLB.カードステータス, CASE WHEN TBLB.企業コード > 3000 THEN 1 ELSE 2 END) AS RN 
FROM
  WSＤＭ対象者リスト TBLA
  , MSカード情報 TBLB 
WHERE
  TBLA.ＧＯＯＰＯＮ番号 = TBLB.ＧＯＯＰＯＮ番号
)
WHERE RN=1
;

