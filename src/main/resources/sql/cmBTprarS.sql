\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_RANK.CSV


SELECT 
/*+ LEADING (B A ) */
CONCAT('"' , LPAD(A.企画ＩＤ, 10, '0') , '"' , CHR(9) , 
NVL(A.バージョン, 0) , CHR(9) , 
'"' , A.ランクコード , '"' , CHR(9) , 
'"' , A.プレミアムポイント区分 , '"' , CHR(9) , 
NVL(A.計算基準単位額, 0) , CHR(9) , 
NVL(TRIM_SCALE(A.ポイント付与率), 0) )
FROM
    MSポイント付与会員ランクO A 
INNER JOIN
    WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND B.ポイント種別 IN ( 2, 4 ) 
;

\o
