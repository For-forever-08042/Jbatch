\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./cmBTkmstS1.log



TRUNCATE TABLE WS顧客番号;
INSERT INTO WS顧客番号 (顧客番号)
        SELECT 顧客番号 FROM MM顧客情報 WHERE 最終更新日 >= :SDATE
        UNION
        SELECT 顧客番号 FROM MM顧客企業別属性情報 WHERE 最終更新日 >=  :SDATE
;
commit;

\o
