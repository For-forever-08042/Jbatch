
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_VISIT_INFO.CSV



SELECT
/*+ LEADING (B A ) */
CONCAT('"' , LPAD(A.企画ＩＤ,10,'0') , '"' , CHR(9) , 
NVL(A.バージョン,0) , CHR(9) ,
'"' , A.付与制御フラグ , '"') 
FROM
 MS来店ポイントO A
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
;
\o
