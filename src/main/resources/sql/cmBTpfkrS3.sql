
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_AMOUNT_POINT_2.CSV

SELECT 
CONCAT('"', LPAD(A.企画ＩＤ, 10, '0'), '"', CHR(9),
A.バージョン, CHR(9),
'"', LPAD(A.枝番, 2, '0'), '"', CHR(9),
A.適用下限額, CHR(9),
A.計算基準単位額, CHR(9),
CASE WHEN C.倍率固定値区分 = 1 THEN NVL(to_char(A.倍率固定ポイント値),'')   ELSE NVL(to_char(A.倍率固定ポイント値,'FM99999999'),'')   END  )
FROM
 MSポイント付与買上高O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND B.ポイント種別 = 2 
AND B.配信区分 <> 2 
INNER JOIN
 MSポイント付与条件O C 
ON
    A.企画ＩＤ = C .企画ＩＤ 
AND A.バージョン = C .バージョン
;

\o
