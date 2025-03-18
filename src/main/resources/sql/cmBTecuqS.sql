\set BDATE :1                                                                                     --バッチ処理日
\set SFILENAME :2                                                                                 --出力ファイルファイル名


\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


\o ./:SFILENAME.csv

TRUNCATE TABLE WSコーポレート顧客番号;


INSERT INTO WSコーポレート顧客番号
       SELECT M.顧客番号
             ,M.デジタル会員ＥＣ入会フラグ 
             ,M.デジタル会員アプリ入会フラグ
       FROM MS顧客制度情報 M
       WHERE M.顧客ステータス = 1
       AND   M.コーポレート会員フラグ = 1
       AND   M.コーポレート会員登録日 <= :BDATE
;
commit ;

SELECT
  CONCAT(TO_CHAR(TO_DATE(TO_CHAR(t1.入会年月日), 'YYYYMMDD'), 'YYYY/MM/DD') ,',',
  COUNT(CASE WHEN t1.企業コード = 3020 AND NVL(t2.デジタル会員ＥＣ入会フラグ,0) =0 AND NVL(t2.デジタル会員アプリ入会フラグ,0) =0
             THEN 1 ELSE NULL END) ,',',                                          /* MK-ECの新規登録者をCOUNT */
  COUNT(CASE WHEN t1.企業コード = 1020
             THEN 1 ELSE NULL END),',',                                           /* CF-ECの新規登録者をCOUNT */
  COUNT(CASE WHEN t1.企業コード=3020 AND  ( NVL(t2.デジタル会員ＥＣ入会フラグ,0) <>0 OR NVL(t2.デジタル会員アプリ入会フラグ,0) <>0  )  /* デジタル会員の入会者数を抽出 */
             THEN 1 ELSE NULL END) )
FROM MM顧客企業別属性情報 t1
INNER JOIN WSコーポレート顧客番号 t2 
       ON t1.顧客番号 = t2.顧客番号 
WHERE
  t1.入会年月日 BETWEEN CAST(TO_CHAR(TRUNC(LAST_DAY(ADD_MONTHS(NVL(TO_DATE(CAST(:BDATE AS TEXT),'YYYYMMDD'),SYSDATE()),-2))+1),'YYYYMMDD') AS NUMERIC) AND
                     CAST(TO_CHAR(TRUNC(LAST_DAY(ADD_MONTHS(NVL(TO_DATE(CAST(:BDATE AS TEXT),'YYYYMMDD'),SYSDATE()),-1))),'YYYYMMDD') AS NUMERIC)
AND t1.企業コード in (1020, 3020)
GROUP BY t1.入会年月日
ORDER BY t1.入会年月日
;

\o

