
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_SHOP.CSV

SELECT 
/*+ LEADING (B  A) */
CONCAT('"' , LPAD(A.企画ＩＤ, 10, '0') , '"' , CHR(9) , 
A.バージョン , CHR(9) , 
'"' , A.組織レベル , '"'  , CHR(9) , 
'"' , 
CASE WHEN A.組織レベル = 5 THEN LPAD(A.組織コード, 6, '0')
     WHEN A.組織レベル = 3 THEN LPAD(A.組織コード, 2, '0')
     WHEN A.組織レベル = 6 THEN LPAD(A.組織コード, 8, '0') 
ELSE LPAD(A.組織コード, 4, '0') END , '"') 
FROM
 MSポイント付与組織O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
;

\o

