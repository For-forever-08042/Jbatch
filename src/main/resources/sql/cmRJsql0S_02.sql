\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./cmRJsql0S_02.utf


SELECT CONCAT(A.会員番号, ',',
       A.カードステータス, ',', 
       NULL, ',',
       RPAD(A.顧客カナ名称,LENGTH(A.顧客カナ名称)), ',',
       A.性別, ',',
       A.誕生年月日, ',', 
       NULL, ',', 
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL, ',',
       NULL)
FROM (
SELECT T1.会員番号 as 会員番号,
       DECODE(T1.カードステータス,'0',0,1) as カードステータス,
       nullif(trim(T2.顧客カナ名称), '') as 顧客カナ名称,
       nvl(T2.性別,0) as 性別,
       DECODE(T2.誕生年,'0',0,CAST(CONCAT(T2.誕生年,'0101') AS NUMERIC)) as 誕生年月日
FROM ( SELECT 会員番号, 顧客番号, カードステータス, ROW_NUMBER() OVER ( PARTITION BY 顧客番号 ORDER BY カードステータス ASC, サービス種別 ASC, 発行年月日 DESC, 会員番号 ASC ) G_Row 
       FROM cmuser.MSカード情報 
       WHERE サービス種別 in (1,3)
       AND 顧客番号 <> 0 ) T1, 
     cmuser.MM顧客情報 T2
WHERE T1.G_Row = 1 
and T1.顧客番号=T2.顧客番号
and T2.最終静態更新日>=20200520
union
SELECT T1.会員番号 as 会員番号,
       DECODE(T1.カードステータス,'0',0,1) as カードステータス,
       nullif(trim(T2.顧客カナ名称), '') as 顧客カナ名称,
       nvl(T2.性別,0) as 性別,
       DECODE(T2.誕生年,'0',0,CAST(CONCAT(T2.誕生年,'0101') AS NUMERIC)) as 誕生年月日
FROM ( SELECT 会員番号, 顧客番号, カードステータス, 発行年月日, ROW_NUMBER() OVER ( PARTITION BY 顧客番号 ORDER BY カードステータス ASC, サービス種別 ASC, 発行年月日 DESC, 会員番号 ASC ) G_Row 
       FROM cmuser.MSカード情報 
       WHERE サービス種別 in (1,3)
       AND 顧客番号 <> 0 ) T1 
left outer join cmuser.MM顧客情報 T2
on T1.顧客番号=T2.顧客番号
WHERE T1.G_Row = 1 
and T1.発行年月日>=20200520
) A
order by A.会員番号;

\o

