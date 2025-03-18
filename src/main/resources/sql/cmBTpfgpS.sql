
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_SHOP_KK.CSV


SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , A.組織レベル , '"' , CHR(9) ,
'"' , A.組織コード, '"')
FROM
 WSポイント付与組織O A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
;
\o
