
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_RANK_KK.CSV



SELECT
CONCAT('"' , B.企画ＩＤ , '"' , CHR(9) ,
'"' , B.バージョン , '"' , CHR(9) ,
'"' , A.ランクコード , '"' , CHR(9) ,
'"' , A.プレミアムポイント区分 , '"' , CHR(9) ,
'"' , A.計算基準単位額 , '"' , CHR(9) ,
'"' , TRIM_SCALE(A.ポイント付与率), '"')
FROM
 WSポイント付与会員ランクO A
INNER JOIN
 WSポイント付与条件O B 
ON
    A.発注番号 = B.発注番号 
;

\o
