
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PRINT_RANK.CSV

SELECT
CONCAT('"' , A.ランク種別 , '"' , CHR(9) ,
'"' , A.ランクコード , '"' , CHR(9) ,
'"' , NULLIF(RTRIM(A.ランク名), '') , '"' , CHR(9) ,
'"' , A.ポイント倍率 , '"' , CHR(9) ,
'"' , TO_CHAR(A.適用開始日,'YYYY/MM/DD HH24:MI:SS'), '"' , CHR(9) ,
'"' , TO_CHAR(A.適用終了日,'YYYY/MM/DD HH24:MI:SS'), '"' , CHR(9) ,
'"' , A.削除フラグ , '"' , CHR(9) ,
'"' , SUBSTR(TO_CHAR(A.作業者ＩＤ), 1, 8) , '"' , CHR(9) ,
'"' , TO_CHAR(TO_DATE(CAST(A.作業年月日 AS TEXT)),'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,
'"' , SUBSTR(NULLIF(TRIM(A.最終更新プログラムＩＤ), ''), 1, 8) , '"' , CHR(9) ,
'"' , TO_CHAR(A.最終更新日時,'YYYY/MM/DD HH24:MI:SS') , '"')
FROM
 MS印字用ランク情報 A
;
\o
