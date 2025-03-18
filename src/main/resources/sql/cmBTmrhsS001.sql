\set SDATE 'to_char(nvl(to_date(cast(:2 as text),''yyyymmdd''),sysdate()),''yyyymmdd'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./RESENDLIST.TXT

SELECT 
 CONCAT(A.システム年月日 , ' ' , 
 A.メール履歴通番 ,  ' ' , 
 NVL(A.リトライ回数,0)) 
FROM
 TMメール送信エラー履歴 A
WHERE
 A.送信ステータス=0 --未送信
AND
 A.削除フラグ=0 --未削除
AND
 A.リトライ回数< :1 --リトライ回数上限
AND
 A.最終更新日 >= CAST(:SDATE AS NUMERIC) --バッチ処理日付前日
;
\o
