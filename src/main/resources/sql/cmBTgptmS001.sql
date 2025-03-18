\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgptm001.tmp
--登録者数(日別)
SELECT
  TO_CHAR(COUNT(1),'FM999,999,999')
FROM
(
SELECT
    M.顧客番号
FROM
    MS顧客制度情報 M
WHERE
    M.顧客ステータス = 1
    AND M.コーポレート会員フラグ = 1
    AND M.コーポレート会員登録日 = :SDATE
UNION
SELECT
    顧客番号
FROM
    (
    SELECT
        M.顧客番号
      , TO_CHAR(LEAST(NVL(M.デジタル会員ＥＣ入会更新日時, '99991231'), NVL(M.デジタル会員アプリ入会更新日時, '99991231')), 'YYYYMMDD') AS 入会日
    FROM
        MS顧客制度情報 M
    WHERE
        M.顧客ステータス = 1
        AND M.デジタル会員ＥＣ入会フラグ <> 0
        AND M.デジタル会員ＥＣ入会フラグ IS NOT NULL
        AND M.デジタル会員ＥＣ入会更新日時 BETWEEN TO_DATE(CONCAT(:SDATE , '000000'), 'YYYYMMDDHH24MISS') AND TO_DATE(CONCAT(:SDATE , '235959'), 'YYYYMMDDHH24MISS')
    UNION
    SELECT
        M.顧客番号
      , TO_CHAR(LEAST(NVL(M.デジタル会員ＥＣ入会更新日時, '99991231'), NVL(M.デジタル会員アプリ入会更新日時, '99991231')), 'YYYYMMDD') AS 入会日
    FROM
        MS顧客制度情報 M
    WHERE
        M.顧客ステータス = 1
        AND M.デジタル会員アプリ入会フラグ <> 0
        AND M.デジタル会員アプリ入会フラグ IS NOT NULL
        AND M.デジタル会員アプリ入会更新日時 BETWEEN TO_DATE(CONCAT(:SDATE , '000000'), 'YYYYMMDDHH24MISS') AND TO_DATE(CONCAT(:SDATE , '235959'), 'YYYYMMDDHH24MISS')
    )
WHERE
    入会日 = :SDATE
)
;
\o
