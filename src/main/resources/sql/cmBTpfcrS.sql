
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CARD_KIND.CSV

SELECT 
/*+ LEADING (B  A) */
CONCAT('"' , LPAD(A.企画ＩＤ, 10, '0') , '"' , CHR(9) , 
A.バージョン , CHR(9) , 
'"' , LPAD(A.カード種別,3, '0') , '"') 
FROM
 MSポイント付与カード種別O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
;
\o
