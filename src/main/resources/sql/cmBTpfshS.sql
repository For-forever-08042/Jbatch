
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_GOODS_POINT_KK.CSV

SELECT
CONCAT('"' , B.ιζhc , '"' , CHR(9) ,
'"' , B.o[W , '"' , CHR(9) ,
'"' , RPAD(A.i`mR[h,LENGTH(A.i`mR[h)) , '"' , CHR(9) ,
'"' , RPAD(A.€iΌΜ,LENGTH(A.€iΌΜ)) , '"' , CHR(9) ,
'"' , A.{¦Εθ|Cgl , '"')
FROM
 WS|Cgt^€iO A
INNER JOIN
 WS|Cgt^πO B 
ON
    A.­Τ = B.­Τ 
;
\o
