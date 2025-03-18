
\set SDATE :1
\set LAST_MONTH :2  /* 前月YYYYMM */


\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0



SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset

\o ./STORE_POINT_KR_:SDATE.csv

select
CONCAT(TO_CHAR(a.会社コードＭＣＣ,'FM0000') ,',',
TO_CHAR(a.店番号ＭＣＣ,'FM0000') ,',',
NVL(SUM(a.処理件数1取引計),0) ,',',
NVL(SUM(a.加算ポイント1取引計),0) ,',',
NVL(SUM(a.処理件数2取引計),0) ,',',
NVL(SUM(a.加算ポイント2取引計),0) ,',',
NVL(SUM(a.処理件数3取引計),0) ,',',
NVL(SUM(a.加算ポイント3取引計),0) ,',',
NVL(SUM(a.処理件数4取引計),0) ,',',
NVL(SUM(a.加算ポイント4取引計),0) ,',',
NVL(SUM(a.処理件数5取引計),0) ,',',
NVL(SUM(a.加算ポイント5取引計),0) ,',',
NVL(SUM(a.処理件数6取引計),0) ,',',
NVL(SUM(a.加算ポイント6取引計),0)*-1  ,',', /*減算はマイナスで出力*/
NVL(SUM(a.処理件数7取引計),0) ,',',
NVL(SUM(a.加算ポイント7取引計),0) ,',',
NVL(SUM(a.処理件数8取引計),0) ,',',
NVL(SUM(a.加算ポイント8取引計),0)*-1 ,',',
NVL(SUM(a.処理件数9取引計),0) ,',',
NVL(SUM(a.加算ポイント9取引計),0) ,',',
NVL(SUM(a.処理件数10取引計),0) ,',',
NVL(SUM(a.加算ポイント10取引計),0) ,',',
NVL(SUM(a.処理件数11取引計),0) ,',',
NVL(SUM(a.加算ポイント11取引計),0) ,',',
NVL(SUM(a.処理件数12取引計),0) ,',',
NVL(SUM(a.加算ポイント12取引計),0) ,',',
NVL(SUM(a.処理件数13取引計),0) ,',',
NVL(SUM(a.加算ポイント13取引計),0))

from
(/*取引単位*/
select
t.システム年月日, 
t.顧客番号 ,
t.処理通番,
t.会社コードＭＣＣ,
t.店番号ＭＣＣ,
case when sum(t.処理件数1明細計) >= 1 then 1 else 0 end as 処理件数1取引計,
sum(t.加算ポイント1明細計)as 加算ポイント1取引計,
case when sum(t.処理件数2明細計)>=1 then 1 else 0 end as 処理件数2取引計,
sum(t.加算ポイント2明細計)as 加算ポイント2取引計,
case when sum(t.処理件数3明細計)>=1 then 1 else 0 end as 処理件数3取引計,
sum(t.加算ポイント3明細計)as 加算ポイント3取引計,
case when sum(t.処理件数4明細計)>=1 then 1 else 0 end as 処理件数4取引計,
sum(t.加算ポイント4明細計)as 加算ポイント4取引計,
case when sum(t.処理件数5明細計)>=1 then 1 else 0 end as 処理件数5取引計,
sum(t.加算ポイント5明細計)as 加算ポイント5取引計,
case when sum(t.処理件数6明細計)>=1 then 1 else 0 end as 処理件数6取引計,
sum(t.加算ポイント6明細計)as 加算ポイント6取引計,
case when sum(t.処理件数7明細計)>=1 then 1 else 0 end as 処理件数7取引計,
sum(t.加算ポイント7明細計)as 加算ポイント7取引計,
case when sum(t.処理件数8明細計)>=1 then 1 else 0 end as 処理件数8取引計,
sum(t.加算ポイント8明細計)as 加算ポイント8取引計,
case when sum(t.処理件数9明細計)>=1 then 1 else 0 end as 処理件数9取引計,
sum(t.加算ポイント9明細計)as 加算ポイント9取引計,
case when sum(t.処理件数10明細計)>=1 then 1 else 0 end as 処理件数10取引計,
sum(t.加算ポイント10明細計)as 加算ポイント10取引計,
case when sum(t.処理件数11明細計)>=1 then 1 else 0 end as 処理件数11取引計,
sum(t.加算ポイント11明細計)as 加算ポイント11取引計,
case when sum(t.処理件数12明細計)>=1 then 1 else 0 end as 処理件数12取引計,
sum(t.加算ポイント12明細計)as 加算ポイント12取引計,
case when sum(t.処理件数13明細計)>=1 then 1 else 0 end as 処理件数13取引計,
sum(t.加算ポイント13明細計)as 加算ポイント13取引計

from
(/*買上高ポイント種別01*/
/*明細単位のcount、sum*/
SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０１ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(110) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０１ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(112) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(116) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(113) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(114) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(115) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(118) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(119) then t2.付与ポイント０１
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(127) then t2.付与ポイント０１
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(128) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０１ in(125) then t2.付与ポイント０１ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(201) then t2.付与ポイント０１
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０１ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０１ in(111) then t2.付与ポイント０１
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０１ = 2
and t2.購買区分０１ = 1
and t2.付与ポイント０１ >= 1
and t2.買上高ポイント種別０１ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ


/*買上ポイント種別02*/

union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０２ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(110) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０２ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(112) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(116) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(113) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(114) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(115) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(118) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(119) then t2.付与ポイント０２
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(127) then t2.付与ポイント０２
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(128) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０２ in(125) then t2.付与ポイント０２ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(201) then t2.付与ポイント０２
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０２ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０２ in(111) then t2.付与ポイント０２
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０２ = 2
and t2.購買区分０２ = 1
and t2.付与ポイント０２ >= 1
and t2.買上高ポイント種別０２ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

/*買上高ポイント種別03*/

union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０３ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(110) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０３ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(112) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(116) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(113) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(114) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(115) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(118) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(119) then t2.付与ポイント０３
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(127) then t2.付与ポイント０３
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(128) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０３ in(125) then t2.付与ポイント０３ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(201) then t2.付与ポイント０３
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０３ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０３ in(111) then t2.付与ポイント０３
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０３ = 2
and t2.購買区分０３ = 1
and t2.付与ポイント０３ >= 1
and t2.買上高ポイント種別０３ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ


/*買上高ポイント種別04*/

union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０４ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(110) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０４ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(112) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(116) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(113) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(114) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(115) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(118) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(119) then t2.付与ポイント０４
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(127) then t2.付与ポイント０４
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(128) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０４ in(125) then t2.付与ポイント０４ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(201) then t2.付与ポイント０４
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０４ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０４ in(111) then t2.付与ポイント０４
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０４ = 2
and t2.購買区分０４ = 1
and t2.付与ポイント０４ >= 1
and t2.買上高ポイント種別０４ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ


/*買上高ポイント種別05*/

union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０５ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(110) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０５ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(112) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(116) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(113) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(114) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(115) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(118) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(119) then t2.付与ポイント０５
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(127) then t2.付与ポイント０５
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(128) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０５ in(125) then t2.付与ポイント０５ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(201) then t2.付与ポイント０５
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０５ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０５ in(111) then t2.付与ポイント０５
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０５ = 2
and t2.購買区分０５ = 1
and t2.付与ポイント０５ >= 1
and t2.買上高ポイント種別０５ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ


/*買上高ポイント種別06*/
union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０６ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(110) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０６ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(112) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(116) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(113) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(114) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(115) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(118) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(119) then t2.付与ポイント０６
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(127) then t2.付与ポイント０６
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(128) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０６ in(125) then t2.付与ポイント０６ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(201) then t2.付与ポイント０６
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０６ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０６ in(111) then t2.付与ポイント０６
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０６ = 2
and t2.購買区分０６ = 1
and t2.付与ポイント０６ >= 1
and t2.買上高ポイント種別０６ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

/*買上高ポイント種別07*/ 
union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０７ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(110) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０７ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(112) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(116) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(113) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(114) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(115) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(118) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(119) then t2.付与ポイント０７
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(127) then t2.付与ポイント０７
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(128) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０７ in(125) then t2.付与ポイント０７ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(201) then t2.付与ポイント０７
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０７ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０７ in(111) then t2.付与ポイント０７
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０７ = 2
and t2.購買区分０７ = 1
and t2.付与ポイント０７ >= 1
and t2.買上高ポイント種別０７ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

/*買上高ポイント種別08*/
union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０８ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(110) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０８ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(112) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(116) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(113) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(114) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(115) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(118) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(119) then t2.付与ポイント０８
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(127) then t2.付与ポイント０８
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(128) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０８ in(125) then t2.付与ポイント０８ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(201) then t2.付与ポイント０８
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０８ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０８ in(111) then t2.付与ポイント０８
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０８ = 2
and t2.購買区分０８ = 1
and t2.付与ポイント０８ >= 1
and t2.買上高ポイント種別０８ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

/*買上高ポイント種別09*/
union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別０９ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(110) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別０９ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(112) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(116) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(113) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(114) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(115) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(118) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(119) then t2.付与ポイント０９
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(127) then t2.付与ポイント０９
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(128) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別０９ in(125) then t2.付与ポイント０９ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(201) then t2.付与ポイント０９
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別０９ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別０９ in(111) then t2.付与ポイント０９
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分０９ = 2
and t2.購買区分０９ = 1
and t2.付与ポイント０９ >= 1
and t2.買上高ポイント種別０９ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

/*買上高ポイント種別10*/
union all

SELECT 
t1.システム年月日,
t1.顧客番号,
t1.処理通番, 
t1.会社コードＭＣＣ,
t1.店番号ＭＣＣ,
COUNT(CASE WHEN 
t2.買上高ポイント種別１０ in(110) then 1
else NULL END ) as 処理件数1明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(110) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント1明細計,
COUNT(CASE WHEN 
t2.買上高ポイント種別１０ in(112) then 1 
else NULL END ) as 処理件数2明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(112) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント2明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(116) then 1 
else NULL END ) as 処理件数3明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(116) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント3明細計, 
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(113) then 1 
else NULL END ) as 処理件数4明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(113) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント4明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(114) then 1 
else NULL END ) as 処理件数5明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(114) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント5明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(115) then 1 
else NULL END ) as 処理件数6明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(115) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント6明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(118) then 1 
else NULL END ) as 処理件数7明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(118) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント7明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(119) then 1 
else NULL END ) as 処理件数8明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(119) then t2.付与ポイント１０
else NULL END ) as 加算ポイント8明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(127) then 1 
else NULL END ) as 処理件数9明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(127) then t2.付与ポイント１０
else NULL END ) as 加算ポイント9明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(128) then 1 
else NULL END ) as 処理件数10明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(128) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント10明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(125) then 1 
else NULL END ) as 処理件数11明細計, 
SUM(CASE WHEN 
t2.買上高ポイント種別１０ in(125) then t2.付与ポイント１０ 
else NULL END ) as 加算ポイント11明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(201) then 1 
else NULL END ) as 処理件数12明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(201) then t2.付与ポイント１０
else NULL END ) as 加算ポイント12明細計,
COUNT(CASE WHEN
t2.買上高ポイント種別１０ in(111) then 1 
else NULL END ) as 処理件数13明細計, 
SUM(CASE WHEN
t2.買上高ポイント種別１０ in(111) then t2.付与ポイント１０
else NULL END ) as 加算ポイント13明細計

from HSポイント日別情報:LAST_MONTH t1
inner join HSポイント日別内訳情報:LAST_MONTH t2
on t1.システム年月日 = t2.システム年月日
and t1.顧客番号 = t2.顧客番号
and t1.処理通番 = t2.処理通番
/*期間限定*/
where t2.通常期間限定区分１０ = 2
and t2.購買区分１０ = 1
and t2.付与ポイント１０ >= 1
and t2.買上高ポイント種別１０ in (110,112,116,113,114,115,118,119,127,128,125,201,111)

/*明細単位*/
group by t1.システム年月日,t1.顧客番号,t1.処理通番, t1.会社コードＭＣＣ,t1.店番号ＭＣＣ

)t

group by t.システム年月日,t.顧客番号,t.処理通番, t.会社コードＭＣＣ,t.店番号ＭＣＣ/*取引単位*/
)a

group by a.会社コードＭＣＣ,a.店番号ＭＣＣ
order by a.会社コードＭＣＣ,a.店番号ＭＣＣ
;

\o



