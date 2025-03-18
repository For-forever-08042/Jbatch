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
\o ./FUSEI_POS_POINT_OVER_:SDATE.csv

select
 CONCAT(会員番号 ,',',
 付与ポイント) 
from HSポイント日別情報:LAST_MONTH 
where 付与ポイント>=10000 
and 登録経路 in ('1','C','D')
and 取引区分 like '17%'
order by 会員番号 
;
\o
