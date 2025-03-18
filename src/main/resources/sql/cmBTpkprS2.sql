
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./NG_PROMOTION_COUPON_2.CSV


SELECT
/*+ LEADING (B A ) */
CONCAT('"', LPAD(A.企画ＩＤ,10,'0'), '"', CHR(9), 
A.バージョン, CHR(9),
'"', A.クーポンコード, '"', CHR(9),  
'"', A.計算方法区分, '"')
FROM
 MSポイント付与条件クーポンO A
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND B.ポイント種別 = 2
;

\o
