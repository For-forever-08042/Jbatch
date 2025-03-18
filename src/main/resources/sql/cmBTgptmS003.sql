\set SDATE to_number(:1) 

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgptm003.tmp
--メルマガ許諾者数
TRUNCATE TABLE WS顧客番号;
INSERT INTO WS顧客番号
SELECT
    M.顧客番号
FROM
    MS顧客制度情報 M
WHERE
    M.顧客ステータス = 1
    AND M.コーポレート会員フラグ = 1
    AND M.コーポレート会員登録日 <= :SDATE
UNION
SELECT
    M.顧客番号
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
    TO_CHAR(COUNT(1),'FM999,999,999')
FROM
    MM顧客企業別属性情報 K
WHERE
    EXISTS (
        SELECT
            1
        FROM
            WS顧客番号 T
        WHERE
            T.顧客番号 = K.顧客番号
    ) 
    AND K.企業コード = 3020
    AND K.退会年月日 = 0
    AND K.Ｅメール止め区分 IN (5000, 5001)
;
\o
