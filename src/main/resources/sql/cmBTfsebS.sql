\set LAST_MONTH :2
\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./FUSEI_POINT_OVER_:SDATE.csv

select
 CONCAT(顧客番号 ,',',
 SUM(付与ポイント)) as 付与ポイント 
from HSポイント日別情報:LAST_MONTH 
group by 顧客番号
HAVING SUM(付与ポイント) >= 10000
order by 顧客番号 
;
\o
