
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CARD_KIND_KK.CSV


SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , CASE WHEN SUBSTR(A.カード番号, 14, 5) ='67988'    THEN 701 ELSE 703 END  , '"') 
FROM
 WSポイント付与会員O A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
WHERE
    LENGTH(A.カード番号) = 20
UNION ALL
SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , C.カード種別 , '"')
FROM
 (SELECT 発注番号, 
CASE WHEN SUBSTR(カード番号, 1, 4) = '9881' AND LENGTH(カード番号) = 16   THEN CONCAT(カード番号 , '0') ELSE カード番号 END  カード番号
  FROM WSポイント付与会員O   ) A
INNER JOIN
 WSポイント付与条件O B
ON
    A.発注番号 = B.発注番号
JOIN PS会員番号体系 C
ON   A.カード番号 >=  CAST(C.会員番号開始 AS TEXT)
AND  A.カード番号 <=  CAST(C.会員番号終了 AS TEXT)
WHERE
    LENGTH(A.カード番号) <> 20
;
\o
