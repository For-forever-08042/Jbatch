\set SDATE to_number(concat(to_char(add_months(sysdate(),-1),'yyyymm'),'01'))

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(sysdate(),'yyyymmdd') spl_date_text from dual \gset
\o ./count_family:spl_date_text.utf


select concat('処理日時=',to_char(sysdate())) from dual;

select concat('家族数(累計)=',count(*)) from MS家族制度情報
where 家族作成日 < :SDATE + 100
and 家族削除日=0;

select concat('家族登録数=',count(*)) from MS家族制度情報
where 家族作成日 >= :SDATE
and 家族作成日 < :SDATE + 100
and 家族削除日=0;


select 
decode(nvl(家族親顧客番号,0),'0',0,1) +
decode(nvl(家族１顧客番号,0),'0',0,1) +
decode(nvl(家族２顧客番号,0),'0',0,1) +
decode(nvl(家族３顧客番号,0),'0',0,1) +
decode(nvl(家族４顧客番号,0),'0',0,1) 家族構成人数,count(*) "家族数(累計)"
from MS家族制度情報
where 家族削除日 = 0
and 家族作成日 < :SDATE + 100
group by
decode(nvl(家族親顧客番号,0),'0',0,1) +
decode(nvl(家族１顧客番号,0),'0',0,1) +
decode(nvl(家族２顧客番号,0),'0',0,1) +
decode(nvl(家族３顧客番号,0),'0',0,1) +
decode(nvl(家族４顧客番号,0),'0',0,1)
order by 家族構成人数;

\o
