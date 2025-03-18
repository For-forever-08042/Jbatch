
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_GOODS_POINT_KK.CSV

SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , RPAD(A.ＪＡＮコード,LENGTH(A.ＪＡＮコード)) , '"' , CHR(9) ,
'"' , RPAD(A.商品名称,LENGTH(A.商品名称)) , '"' , CHR(9) ,
'"' , A.倍率固定ポイント値 , '"')
FROM
 WSポイント付与商品O A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
;
\o
