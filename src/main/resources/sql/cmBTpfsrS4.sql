\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./NG_PROMOTION_GOODS_POINT_3.CSV


SELECT 
CONCAT( '"', LPAD(A.ιζhc, 10, '0'), '"', CHR(9),
NVL(A.o[W, 0), CHR(9),
'"', NVL(RPAD(A.i`mR[h,LENGTH(A.i`mR[h)), ''), '"', CHR(9),
'"',  NULLIF(RTRIM(A.€iΌΜ),''), '"', CHR(9),
A.{¦Εθ|Cgl)
FROM
    MS|Cgt^€iO A 
INNER JOIN
    WS|Cgt^π B 
ON
    A.ιζhc = B.ιζhc 
AND A.o[W = B.o[W 
AND B.zMζͺ = 0
;

\o
