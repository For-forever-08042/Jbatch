\set NENTUKI 'to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymm'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgptm004.tmp
SELECT
    TO_CHAR(COUNT(DISTINCT A.顧客番号),'FM999,999,999') 
FROM
    MM顧客情報 A 
   ,MM顧客企業別属性情報 B 
WHERE
        A.顧客番号 = B.顧客番号 
    AND A.顧客ステータス = 1 
    AND B.企業コード IN (3020, 3040) 
    AND B.Ｅメール止め区分 <> 5000
;
\o
