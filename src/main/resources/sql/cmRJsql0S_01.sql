\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./cmRJsql0S_01.utf


SELECT CASE WHEN LENGTH(CAST(T1.会員番号 AS TEXT)) > 15 THEN '################' ELSE LPAD(CAST(T1.会員番号 AS TEXT),16,' ') END as 会員コード
FROM ( SELECT 会員番号, 顧客番号, カードステータス, 発行年月日, ROW_NUMBER() OVER ( PARTITION BY 顧客番号 ORDER BY カードステータス ASC, サービス種別 ASC, 発行年月日 DESC, 会員番号 ASC ) G_Row 
       FROM cmuser.MSカード情報 
       WHERE サービス種別 in (1,3)
       AND 顧客番号 <> 0 ) T1
WHERE T1.G_Row = 1 
and T1.発行年月日>=20200520
order by T1.会員番号;

\o
