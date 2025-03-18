\set SDATE to_number(concat(to_char(add_months(sysdate(),-1),'yyyymm'),'01'))

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(sysdate(),'yyyymmdd') spl_date_text from dual \gset
\o ./count_matarnitybaby:spl_date_text.utf


select concat('処理日時=',to_char(sysdate())) from dual;

select concat('登録人数(累計)=',count(*)) from MMマタニティベビー情報
where (退会年月日 = 0 or 退会年月日  >= :SDATE + 100)
and 入会年月日 < :SDATE + 100;

select concat('登録人数=',count(*)) from  MMマタニティベビー情報
where 入会年月日 >= :SDATE
and 入会年月日 < :SDATE + 100;

select concat('解除人数=',count(*)) from  MMマタニティベビー情報
where 退会年月日 >= :SDATE
and 退会年月日 < :SDATE + 100;


select 
concat('登録順',',',
'年齢',',',
'登録人数(累計)')
from dual;



select concat('第１子',',',T.第１子年齢,',',T.登録人数)
from (
select 第１子年齢,count(*) as 登録人数
from MMマタニティベビー情報
where (第１子出産予定日 > 0 or 第１子生年月日 > 0)
and (退会年月日 = 0 or 退会年月日  >= :SDATE + 100)
and 入会年月日 < :SDATE + 100
group by 第１子年齢
order by 第１子年齢) T
UNION ALL
select concat('第２子',',',T.第２子年齢,',',T.登録人数)
from (
select 第２子年齢,count(*) as 登録人数
from MMマタニティベビー情報
where (第２子出産予定日 > 0 or 第２子生年月日 > 0)
and (退会年月日 = 0 or 退会年月日  >= :SDATE + 100)
and 入会年月日 < :SDATE + 100
group by 第２子年齢
order by 第２子年齢) T
UNION ALL
select concat('第３子',',',T.第３子年齢,',',T.登録人数)
from (
select 第３子年齢,count(*) as 登録人数
from MMマタニティベビー情報
where (第３子出産予定日 > 0 or 第３子生年月日 > 0)
and (退会年月日 = 0 or 退会年月日  >= :SDATE + 100)
and 入会年月日 < :SDATE + 100
group by 第３子年齢
order by 第３子年齢) T;


\o
