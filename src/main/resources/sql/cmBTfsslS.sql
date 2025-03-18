\set SDATE :1
\set LAST_MONTH :2
\set START_DATE :3
\set END_DATE :4

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./FUSEI_POS_KAIAGE_OVER_:SDATE.csv

select 
    CONCAT(会員番号,',', 
    TO_CHAR(TO_DATE(TO_CHAR(登録年月日),'YYYYMMDD'),'YYYY/MM/DD') 
    ,' ', 
    TO_CHAR(TO_TIMESTAMP(TO_CHAR(時刻),'HH24MISS'),'HH24:MI') ,',', 
    TO_CHAR(会社コードＭＣＣ, 'FM0000') ,',', 
    TO_CHAR(店番号ＭＣＣ, 'FM0000') ,',', 
    (CASE when 取引区分 like '10%' then 'R' else 'G' end ) ,',', 
    TO_CHAR(ターミナル番号, 'FM00000') ,',', 
    TO_CHAR(取引番号, 'FM0000000000') ,',', 
    買上額)  
from HSポイント日別情報:LAST_MONTH 
where 買上額 >= 100000 
    and 登録年月日 between :START_DATE and :END_DATE 
    and (取引区分 like '10%' or 取引区分 like '17%')
order by 登録年月日,会員番号 
;
\o
