\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./KKMI0160.csv


TRUNCATE TABLE WM顧客番号;
INSERT INTO WM顧客番号 (顧客番号)
        SELECT DISTINCT B.顧客番号
        FROM MS外部認証情報@CMSD A, MSカード情報 B
        WHERE A.サービス種別 = B.サービス種別
        AND A.会員番号 = B.会員番号
        AND A.最終更新日 >= :SDATE
        AND A.外部認証種別 =  'A'
;
commit;

\o
