\set SDATE to_number(:1)
\set NENTUKI 'to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymm'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp008.tmp
    SELECT
        TO_CHAR(COUNT(*),'FM999,999,999') AS 未完了
    FROM
        HS家族情報履歴 A 
    WHERE
        A.申込区分 = 0
        AND A.システム年月日 = :1
        AND NOT EXISTS (SELECT 1 FROM HS家族情報履歴 B WHERE B.家族ＩＤ=A.家族ＩＤ AND B.申込区分=1 AND B.システム年月日 = :1 )
;
\o

