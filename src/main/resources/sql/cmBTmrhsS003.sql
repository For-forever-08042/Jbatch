\set NENKAPI 'to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymmdd'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

UPDATE
TMメール送信エラー履歴 A
SET
リトライ回数=リトライ回数+1,
最終更新日=CAST(:NENKAPI AS NUMERIC),
最終更新日時=sysdate(),
A.最終更新プログラムＩＤ='cmBTmrhsS'
WHERE
A.システム年月日 = :2
AND
A.メール履歴通番 = :3
;
\o
