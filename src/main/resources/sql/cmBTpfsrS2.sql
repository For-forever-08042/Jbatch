\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./NG_PROMOTION_GOODS_POINT_2.CSV

SELECT 
CONCAT('"', LPAD(A.企画ＩＤ, 10, '0'), '"', CHR(9),
NVL(A.バージョン, 0), CHR(9),
'"', NVL(RPAD(A.ＪＡＮコード,LENGTH(A.ＪＡＮコード)), ''), '"', CHR(9),
'"',  NULLIF(RTRIM(A.商品名称),''), '"', CHR(9),
A.倍率固定ポイント値)
FROM
    MSポイント付与商品O A 
INNER JOIN
    WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン
AND B.ポイント種別 = 2
;

\o
