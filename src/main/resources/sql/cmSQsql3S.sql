\set SDATE to_number(concat(to_char(add_months(sysdate(),-1),'yyyymm'),'01'))

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(sysdate(),'yyyymmdd') spl_date_text from dual \gset
\o ./count_circle:spl_date_text.utf


select concat('処理日時=',to_char(sysdate())) from dual;

select 
concat('サークルID',',',
'サークル名称',',',
'サークル開始日',',',
'サークル終了日',',',
'サークル有効期限',',',
'登録人数(累計)',',',
'登録人数',',',
'解除人数')
from dual;

select concat(C1.サークルＩＤ , ',' , nullif(trim(C1.サークル名称),'') , ',' , C1.サークル開始日 , ',' , C1.サークル終了日 ,',' , C1.サークル有効期限 ,
 ',' , nvl(sum(C3.件数),0) , ',' , nvl(sum(C4.件数),0) , ',' , nvl(sum(C5.件数),0))
from MSサークル管理情報 C1
left join
(select a.サークルＩＤ,count(a.顧客番号) as 件数
from MSサークル顧客情報 a
where a.入会日 < :SDATE + 100
and a.退会日 >= :SDATE +100
group by a.サークルＩＤ) C3 on C1.サークルＩＤ=C3.サークルＩＤ
left join
(select b.サークルＩＤ,count(b.顧客番号) as 件数
from MSサークル顧客情報 b
where b.入会日 >= :SDATE
and b.入会日 < :SDATE + 100
group by b.サークルＩＤ) C4 on C1.サークルＩＤ=C4.サークルＩＤ
left join
(select c.サークルＩＤ,count(c.顧客番号) as 件数
from MSサークル顧客情報 c
where c.退会日 >= :SDATE
and c.退会日 < :SDATE + 100
group by c.サークルＩＤ) C5 on C1.サークルＩＤ=C5.サークルＩＤ
group by C1.サークルＩＤ, C1.サークル名称, C1.サークル開始日, C1.サークル終了日, C1.サークル有効期限
order by C1.サークルＩＤ ASC;

\o
