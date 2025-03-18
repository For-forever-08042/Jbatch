\set NENTUKI :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgptm005.tmp
SELECT
  TO_CHAR(COUNT(1),'FM999,999,999')
FROM
  ( 
    SELECT
      顧客番号
    FROM
      MS顧客制度情報
    WHERE
      コーポレート会員フラグ = 0
      AND コーポレート会員登録日 <= :NENTUKI
      AND コーポレート会員登録日 > 0
    UNION
    SELECT
      顧客番号
    FROM
      MS顧客制度情報
    WHERE
    デジタル会員ＥＣ入会フラグ = 0
    AND デジタル会員ＥＣ入会更新日時 IS NOT NULL
    AND デジタル会員ＥＣ入会更新日時 <= TO_DATE(CONCAT(:NENTUKI , '235959'), 'YYYYMMDDHH24MISS')
    UNION
    SELECT
      顧客番号
    FROM
      MS顧客制度情報
    WHERE
    デジタル会員アプリ入会フラグ = 0
    AND デジタル会員アプリ入会更新日時 IS NOT NULL
    AND デジタル会員アプリ入会更新日時 <= TO_DATE(CONCAT(:NENTUKI , '235959'), 'YYYYMMDDHH24MISS')
  )
;
\o
