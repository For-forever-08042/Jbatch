
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_AMOUNT_POINT_KK.CSV


SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , A.枝番, '"' , CHR(9) ,
'"' , A.適用下限額 , '"' , CHR(9) ,
'"' , A.計算基準単位額 , '"' , CHR(9) ,
'"' , TRIM_SCALE(A.倍率固定ポイント値) , '"')
FROM
 WSポイント付与買上高O A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
;
\o
