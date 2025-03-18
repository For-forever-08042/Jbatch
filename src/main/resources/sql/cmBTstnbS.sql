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


SELECT 
  CONCAT(TO_CHAR(T3.旧企業コード,'FM0000') ,',',                                                                                         /* 企業コード                         */
  TO_CHAR(T3.会社コード,'FM0000') ,',',                                                                                           /* ECC会社コード                      */
  TO_CHAR(T3.店番号,'FM0000') ,',',                                                                                               /* 店舗コード                         */
  TRIM(T3.漢字店舗名称) ,',',                                                                                                     /* 店舗名                             */
  COUNT(CASE WHEN T1.サービス種別 = 1 AND T1.企業コード=3010 AND T1.会員番号 BETWEEN 98812710000000000 AND 98812719999999909        /* MK現金カードの入会者数を抽出       */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 1 AND T1.企業コード=3010 AND T1.会員番号 BETWEEN 98812730000000000 AND 98812739999999909        /* MCM現金カードの入会者数を抽出      */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 4 AND T1.企業コード=3050                                                                        /* MKクレジットカードの入会者数を抽出 */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 3 AND T1.企業コード=3040                                                                        /* MK公式アプリの入会者数を抽出       */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 2 AND T1.企業コード=3020 AND t4.カード種別=999   /* MKオンラインサイトの入会者数を抽出 */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 5 AND T1.企業コード=3060                                                                        /* MKグローバルアプリの入会者数を抽出 */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 1 AND T1.企業コード=1010 AND T1.会員番号 BETWEEN 200000000000000 AND 200000999999999            /* CFプリカカードの入会者数を抽出     */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 1 AND T1.企業コード=1010 AND T1.会員番号 BETWEEN 200020000000000 AND 200020999999999            /* MCC現金カードの入会者数を抽出      */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 3 AND T1.企業コード=1040                                                                        /* CF公式アプリの入会者数を抽出       */
             THEN 1 ELSE NULL END) ,',',
  COUNT(CASE WHEN T1.サービス種別 = 2 AND T1.企業コード=1020                                                                        /* CFクラブサイトの入会者数を抽出     */
             THEN 1 ELSE NULL END),',',
  COUNT(CASE WHEN T1.サービス種別 = 2 AND T1.企業コード=3020 AND t4.カード種別=5  /* デジタル会員の入会者数を抽出 */
             THEN 1 ELSE NULL END))              
FROM MSカード情報 T1
INNER JOIN TS利用可能ポイント情報 T2
        ON T1.顧客番号 = T2.顧客番号
INNER JOIN PS店表示情報ＭＣＣ T3
        ON T2.入会会社コードＭＣＣ = T3.会社コード
        AND T2.入会店舗ＭＣＣ = T3.店番号
        AND T1.発行年月日 >= T3.開始年月日
        AND T1.発行年月日 <= T3.終了年月日
INNER JOIN PS会員番号体系 t4 
       ON T1.サービス種別 = t4.サービス種別
       AND T1.会員番号 >= t4.会員番号開始
       AND T1.会員番号 <= t4.会員番号終了
WHERE CAST(T1.発行年月日 AS TEXT) BETWEEN TO_CHAR(TRUNC(LAST_DAY(ADD_MONTHS(NVL(TO_DATE(:BDATE,'YYYYMMDD'),SYSDATE()),-2))+1),'YYYYMMDD') AND
                            TO_CHAR(TRUNC(LAST_DAY(ADD_MONTHS(NVL(TO_DATE(:BDATE,'YYYYMMDD'),SYSDATE()),-1))),'YYYYMMDD')
GROUP BY T3.旧企業コード, T3.会社コード, T3.店番号, T3.漢字店舗名称
ORDER BY T3.会社コード, T3.店番号
;

\o
