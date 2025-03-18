
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CATEGORY_KK.CSV


SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , A.部門レベル , '"' , CHR(9) ,
'"' , A.部門コード  , '"')
FROM
 WSポイント付与部門O A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
;
\o
