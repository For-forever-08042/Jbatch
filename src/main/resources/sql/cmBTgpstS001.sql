\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgpst001.tmp
--有効登録件数
TRUNCATE TABLE WSコーポレート顧客番号;
INSERT
INTO WSコーポレート顧客番号
SELECT
    M.顧客番号
   ,M.デジタル会員ＥＣ入会フラグ
   ,M.デジタル会員アプリ入会フラグ
FROM
    MS顧客制度情報 M
WHERE
    M.顧客ステータス = 1
    AND M.コーポレート会員フラグ = 1
    AND M.コーポレート会員登録日 <= :SDATE
UNION
SELECT
    M.顧客番号
   ,M.デジタル会員ＥＣ入会フラグ
   ,M.デジタル会員アプリ入会フラグ
FROM
    MS顧客制度情報 M
WHERE
    M.顧客ステータス = 1
    AND M.デジタル会員ＥＣ入会フラグ <> 0
    AND M.デジタル会員ＥＣ入会フラグ IS NOT NULL
    AND M.デジタル会員ＥＣ入会更新日時 <= TO_DATE(CONCAT(:SDATE , '235959'), 'YYYYMMDDHH24MISS')
UNION
SELECT
    M.顧客番号
   ,M.デジタル会員ＥＣ入会フラグ
   ,M.デジタル会員アプリ入会フラグ
FROM
    MS顧客制度情報 M
WHERE
    M.顧客ステータス = 1
    AND M.デジタル会員アプリ入会フラグ <> 0
    AND M.デジタル会員アプリ入会フラグ IS NOT NULL
    AND M.デジタル会員アプリ入会更新日時 <= TO_DATE(CONCAT(:SDATE , '235959'), 'YYYYMMDDHH24MISS')
;
COMMIT;
SELECT
    TO_CHAR(COUNT(distinct S.顧客番号),'FM999,999,999')
FROM
    WSコーポレート顧客番号 S,
    MM顧客企業別属性情報 M
WHERE
    S.顧客番号 = M.顧客番号
    AND (    M.企業コード = 3040
        OR ( M.企業コード = 3020 AND S.デジタル会員ＥＣ入会フラグ    <> 0 )
        OR ( M.企業コード = 3020 AND S.デジタル会員アプリ入会フラグ  <> 0 ) )
    AND M.退会年月日 = 0
;
\o
