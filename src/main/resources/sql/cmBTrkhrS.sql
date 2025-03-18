\set MAENEN 'to_char(ADD_MONTHS(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),-12),''yyyy'')' /* バッチ処理日付の前年の年 */
\set MAETUKI 'to_char(ADD_MONTHS(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),-1),''yyyymm'')' /* バッチ処理日付の前月の年月 */

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_RANK_REQUIRED_PAYMANT.CSV

SELECT 
CONCAT('"', A.ランク種別, '"', CHR(9),
'"', A.対象年月度, '"', CHR(9),
'"', A.ランクコード, '"', CHR(9),
A.必要金額ＦＲＯＭ, CHR(9),
A.必要金額ＴＯ, CHR(9),
'"', SUBSTR(TO_CHAR(A.作業者ＩＤ), 1, 8), '"', CHR(9),
'"', TO_CHAR(TO_DATE(CAST (A.作業年月日 AS TEXT)),'YYYY/MM/DD HH24:MI:SS'), '"', CHR(9),
'"', SUBSTR(TRIM(A.最終更新プログラムＩＤ), 1, 8), '"', CHR(9),
'"', TO_CHAR(A.最終更新日時,'YYYY/MM/DD HH24:MI:SS'), '"') 
FROM 
 MSランク別必要金額 A
WHERE
 A.対象年月度>=CAST(:MAENEN AS NUMERIC)
AND
 A.ランク種別=1
UNION
SELECT 
CONCAT('"', B.ランク種別, '"', CHR(9),
'"', B.対象年月度, '"', CHR(9),
'"', B.ランクコード, '"', CHR(9),
B.必要金額ＦＲＯＭ, CHR(9),
B.必要金額ＴＯ, CHR(9),
'"', SUBSTR(TO_CHAR(B.作業者ＩＤ), 1, 8), '"', CHR(9),
'"', TO_CHAR(TO_DATE(CAST(B.作業年月日 AS TEXT)),'YYYY/MM/DD HH24:MI:SS'), '"', CHR(9),
'"', SUBSTR(TRIM(B.最終更新プログラムＩＤ), 1, 8), '"', CHR(9),
'"', TO_CHAR(B.最終更新日時,'YYYY/MM/DD HH24:MI:SS'), '"') 
FROM 
 MSランク別必要金額 B
WHERE
 B.対象年月度>=CAST(:MAETUKI AS NUMERIC)
AND
 B.ランク種別=2
;

\o
