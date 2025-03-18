\set LOOPCNT :1

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
INSERT /*+ APPEND */ INTO WMＤＭ対象者情報 
SELECT
  C.ＧＯＯＰＯＮ番号
  , C.顧客番号
  , C.会員番号
  , C.店舗コード
  , C.会社コード
  , SUBSTRB(CONCAT(CONCAT(NULLIF(TRIM(A.顧客名字), ''), '　'), NULLIF(TRIM(A.顧客名前), '')), 1, 50) AS 氏名
  , CASE 
    WHEN CONCAT( 
      CONCAT(LPAD(SUBSTR(NULLIF(TRIM(B.郵便番号), ''), 1, 3), 3, '0'), '-')
      , LPAD(SUBSTR(NULLIF(TRIM(B.郵便番号), ''), 4, 4), 4, '0')
    ) = '-' 
      THEN ' ' 
    ELSE CONCAT( 
      CONCAT(LPAD(SUBSTR(NULLIF(TRIM(B.郵便番号), ''), 1, 3), 3, '0'), '-')
      , LPAD(SUBSTR(NULLIF(TRIM(B.郵便番号), ''), 4, 4), 4, '0')
    ) 
    END AS 郵便番号
  , NVL(REPLACE(RPAD(B.住所, LENGTH(B.住所)),',',''), ' ') AS 住所
  , TO_DATE(CONCAT(CASE WHEN NVL(A.最終静態更新日,0)=0 THEN 19000101 ELSE A.最終静態更新日 END , LPAD(最終静態更新時刻,6,'0')), 'YYYYMMDDHH24MISS') AS 最終静態更新日時
  , B.都道府県コード
  , :LOOPCNT
FROM
  MM顧客情報 A
  , MM顧客属性情報 B
  , WMＤＭ対象者リスト C
WHERE
  B.顧客番号 = C.顧客番号 
  AND A.顧客番号 = C.顧客番号 
  AND EXISTS (SELECT 1 FROM MM顧客企業別属性情報 D WHERE D.顧客番号 = C.顧客番号 AND D.ＤＭ止め区分=3000)
;

