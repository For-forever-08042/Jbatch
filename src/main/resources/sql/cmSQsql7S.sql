\set SDATE to_number(to_char(sysdate()-1,'yyyymmdd'))

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(sysdate(),'yyyymmdd') spl_date_text from dual \gset
\o ./memo:spl_date_text.utf

select concat('処理日時=',to_char(sysdate())) from dual;

select 
 concat('会員番号',',',
'ステージ変更',',',
'過去のお問い合わせ',',',
'その他',',',
'対応期限',',',
'登録店舗・年月日',',',
'作業者ID',',',
'作業年月日',',',
'作業時刻')
from dual;

select concat(WScrd.会員番号, ',', WScrd.メモ１, ',', WScrd.メモ２, ',', WScrd.メモ３, ',', WScrd.メモ４, ',', WScrd.メモ５, ',',
WScrd.作業者ＩＤ, ',', WScrd.作業年月日, ',', WScrd.作業時刻)
from
 (
 SELECT
 WSmsc.会員番号,RPAD(tmme.メモ１,LENGTH(tmme.メモ１)) AS メモ１,RPAD(tmme.メモ２,LENGTH(tmme.メモ２)) AS メモ２,RPAD(tmme.メモ３,LENGTH(tmme.メモ３)) AS メモ３,RPAD(tmme.メモ４,LENGTH(tmme.メモ４)) AS メモ４,RPAD(tmme.メモ５, LENGTH(tmme.メモ５)) AS メモ５,tmme.作業者ＩＤ,tmme.作業年月日,tmme.作業時刻
,ROW_NUMBER() OVER (PARTITION BY WSmsc.顧客番号 ORDER BY CASE サービス種別 WHEN 1 THEN 1 WHEN 3 THEN 2 WHEN 2 THEN 3 ELSE 0 END, WSmsc.カードステータス ASC,WSmsc.発行年月日 DESC) G_ROW
 FROM
 MSカード情報 WSmsc,TMメモ tmme 
 WHERE
 WSmsc.顧客番号 =tmme.顧客番号
 and tmme.作業年月日=:SDATE
 ) WScrd
 WHERE
 WScrd.G_ROW=1
order by WScrd.作業年月日,WScrd.作業時刻;

\o
