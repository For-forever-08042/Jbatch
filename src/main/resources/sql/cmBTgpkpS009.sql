\set SDATE to_number(:1)
\set NENTUKI 'to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymm'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp009.tmp
    SELECT
        TO_CHAR(COUNT(*),'FM999,999,999') AS 登録申請
    FROM
        HS家族情報履歴 A 
    WHERE
        A.申込区分 = 0
;
\o
