\set DIRECTMAILCD :1
\set FNAME :2
\set LOOPCNT :3
\set NENBAN :4

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./:FNAME

SELECT 
    CONCAT(TO_CHAR(ＧＯＯＰＯＮ番号) , ',' ,
    氏名 , ',' ,
    郵便番号 , ',' ,
    (CASE WHEN 都道府県文字数=3 AND SUBSTR(住所, 1, 3) <> 都道府県名称
         THEN CONCAT(都道府県名称 , 住所)
         WHEN 都道府県文字数=4 AND SUBSTR(住所, 1, 4) <> 都道府県名称
         THEN CONCAT(都道府県名称 , 住所)
         ELSE 住所 END) , ',' ,
    LPAD(:DIRECTMAILCD,4,'0') , LPAD(ROW_NUMBER() OVER()+:NENBAN,7,'0') , ',' ,
    LPAD(店舗コード,4,'0') , ',' ,
    LPAD(会社コード,4,'0'))
FROM
(
SELECT
    A.ＧＯＯＰＯＮ番号
    , NULLIF(TRIM(A.氏名), '') AS 氏名
    , NULLIF(TRIM(A.郵便番号), '') AS 郵便番号
    , NULLIF(TRIM(A.住所), '') AS 住所
    , RPAD(A.店舗コード,LENGTH(A.店舗コード)) AS 店舗コード
    , RPAD(A.会社コード,LENGTH(A.会社コード)) AS 会社コード
    , NULLIF(TRIM(C.名称), '') AS 都道府県名称
    , LENGTH(NULLIF(TRIM(C.名称), '')) AS 都道府県文字数
FROM
    WMＤＭ対象者情報 A
LEFT JOIN PSコード情報 C ON CAST(A.都道府県コード AS TEXT) = C.コード  AND  C.コード種別 = 3 
WHERE
     A.ファイル番号 = :LOOPCNT
ORDER BY ＧＯＯＰＯＮ番号,店舗コード,会社コード
)
;

\o
