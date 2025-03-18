
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./:1

SELECT 
CONCAT('"' , NULLIF(TRIM(A.クーポンＩＤ),'') , '",' , 
'"' , NULLIF(TRIM(A.ＯＣＰＦユーザＩＤ),'') , '",' , 
'"' , C.ＧＯＯＰＯＮ番号 , '",' , 
'"' , TO_CHAR(A.クーポン配信日時, 'YYYY/MM/DD HH24:MI:SS') , '"') 
FROM
 WSゲームクーポンユーザファイル A 
LEFT OUTER JOIN
 MS外部認証情報 B 
ON
 B.外部認証種別 = 'A'
AND
 TRIM(A.ＯＣＰＦユーザＩＤ) = TRIM(B.外部認証ＩＤ)
LEFT OUTER JOIN
 MSカード情報 C 
ON
 B.会員番号 = C.会員番号 
AND
 B.サービス種別 = C.サービス種別 
;

\o
