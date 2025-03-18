
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CATEGORY.CSV


SELECT
CONCAT('"' , LPAD(A.企画ＩＤ,10,'0') , '"' , CHR(9) , 
NVL(A.バージョン,0) , CHR(9) ,
'"' , A.部門レベル , '"' , CHR(9) ,  
'"' , RPAD(CAST(A.部門コード AS TEXT),7) , '"') 
FROM
 MSポイント付与部門O A
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
;
\o
