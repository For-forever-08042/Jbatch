\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


/** 7.1.4. 顧客情報RCVファイル作成 **/
\o ./:1

/** 7.1.3. 管理DBからの取得 **/
SELECT   CONCAT(NULL , LPAD(GOOPON,16,'0')                                                                        --- 顧客コード
     , ',' , DECODE(ENTSTORE,'0',LPAD(' ',4,' '),NULL,LPAD(' ',4,' '),LPAD(ENTSTORE,4,0))                 --- 入会店舗コード
     , ',' , DECODE(ENTCORP,'0',LPAD(' ',4,' '),NULL,LPAD(' ',4,' '),LPAD(ENTCORP,4,0))                   --- 企業コード
     , ',' , DECODE(ENTDATE,'0',19000101,NULL,19000101,ENTDATE)                                           --- 入会日
     , ',' , CASE WHEN SEIBETSU = 1 THEN '1'                                                            --- 性別
                    WHEN SEIBETSU = 2 THEN '2'                                                            --- 
                    ELSE '3'                                                                              --- 
               END                                                                                        --- 
     , ',' , CASE WHEN BIRTHY IS NULL OR BIRTHY=0                                                       --- 生年月日
                      OR BIRTHM IS NULL OR BIRTHM=0                                                       --- 
                      OR BIRTHD IS NULL OR BIRTHD=0 THEN '19000101'                                       --- 
                    ELSE CONCAT(TO_NUMBER(TO_CHAR(BIRTHY)) , LPAD(BIRTHM,2,'0') , LPAD(BIRTHD,2,'0'))           --- 
               END                                                                                        --- 
     , ',' , LPAD(' ',16,' ')                                                                           --- カード番号1
     , ',' , LPAD(' ',16,' ')                                                                           --- カード番号2
     , ',' , LPAD(' ',16,' ')                                                                           --- カード番号3
     , ',' , CASE WHEN DMTKBN = 3000 THEN '0'                                                           --- DM区分
                    ELSE '1'                                                                              --- 
               END                                                                                        --- 
     , ',' , CASE WHEN DMTKBN = 3000 THEN '0'                                                           --- DM区分無効区分
                    ELSE '1'                                                                              --- 
               END                                                                                        --- 
     , ',' , CASE WHEN NVL(KSTATUS, 0) = 1 THEN '0'                                                     --- 顧客ステータス
                    WHEN NVL(KSTATUS, 0) = 9 OR NVL(KSTATUS, 0) = 0 THEN '1' ELSE '0'                     --- 
               END                                                                                        --- 
     , ',' , CHR(ASCII(ADDCODE01)+16) ,                                --- 住所コード(01)
               CHR(ASCII(ADDCODE02)+16) ,                                --- 住所コード(02)
               CHR(ASCII(ADDCODE03)+16) ,                                --- 住所コード(03)
               CHR(ASCII(ADDCODE04)+16) ,                                --- 住所コード(04)
               CHR(ASCII(ADDCODE05)+16) ,                                --- 住所コード(05)
               CHR(ASCII(ADDCODE06)+16) ,                                --- 住所コード(06)
               CHR(ASCII(ADDCODE07)+16) ,                                --- 住所コード(07)
               CHR(ASCII(ADDCODE08)+16) ,                                --- 住所コード(08)
               CHR(ASCII(ADDCODE09)+16) ,                                --- 住所コード(09)
               CHR(ASCII(ADDCODE10)+16) ,                                --- 住所コード(10)
               CHR(ASCII(ADDCODE11)+16)                                   --- 住所コード(11)
     , ',' , NVL(TO_CHAR(XCODE,'FM000.0000000'), LPAD(' ',11,' '))                                      --- X座標コード
     , ',' , NVL(TO_CHAR(YCODE,'FM000.0000000'), LPAD(' ',11,' '))                                      --- Y座標コード
     , ',' , NVL(ECFLG, 0)                                                                              --- コーポレート会員フラグ
     , ',' , CASE WHEN EMTKBN = 5000 THEN 1                                                             --- メール配信許諾フラグ
                    ELSE 0                                                                                --- 
               END                                                                                        --- 
     , ',' , CASE WHEN EMTKBN = 5001 THEN 1                                                             --- メール配信エラーフラグ
                    ELSE 0                                                                                --- 
               END                                                                                        --- 
     , ',' , NVL(RPAD(LINEID,255,' '), LPAD(' ',255,' '))                                               --- 外部認証ＩＤ
     , ',' , NVL(LPAD(GLBLCODE,3,' '), LPAD(' ',3,' ')) , NULL)                                                 --- グローバル会員国コード
  FROM ( SELECT  W.ＧＯＯＰＯＮ番号             AS GOOPON
              , KM.入会店舗ＭＣＣ               AS ENTSTORE
              , PM.旧企業コード                 AS ENTCORP
              , KK.入会年月日                   AS ENTDATE
              , KM.性別                         AS SEIBETSU
              , KM.誕生年                       AS BIRTHY
              , KM.誕生月                       AS BIRTHM
              , KM.誕生日                       AS BIRTHD
              , KK.ＤＭ止め区分                 AS DMTKBN
              , KM.顧客ステータス               AS KSTATUS
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),1,1),'0')   AS ADDCODE01   ---「自宅住所コード」の1文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),2,1),'0')   AS ADDCODE02   ---「自宅住所コード」の2文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),3,1),'0')   AS ADDCODE03   ---「自宅住所コード」の3文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),4,1),'0')   AS ADDCODE04   ---「自宅住所コード」の4文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),5,1),'0')   AS ADDCODE05   ---「自宅住所コード」の5文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),6,1),'0')   AS ADDCODE06   ---「自宅住所コード」の6文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),7,1),'0')   AS ADDCODE07   ---「自宅住所コード」の7文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),8,1),'0')   AS ADDCODE08   ---「自宅住所コード」の8文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),9,1),'0')   AS ADDCODE09   ---「自宅住所コード」の9文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),10,1),'0')  AS ADDCODE10   ---「自宅住所コード」の10文字目を切り抜き
              , NVL(SUBSTR(TRIM(KZ.自宅住所コード),11,1),'0')  AS ADDCODE11   ---「自宅住所コード」の11文字目を切り抜き
              , TRIM_SCALE(KZ.Ｘ座標コード)                 AS XCODE
              , TRIM_SCALE(KZ.Ｙ座標コード)                 AS YCODE
              ,  W.コーポレート会員フラグ       AS ECFLG
              , KK.Ｅメール止め区分             AS EMTKBN
              ,  W.外部認証ＩＤ                 AS LINEID
              ,  RPAD(W.グローバル会員国コード,length(W.グローバル会員国コード))       AS GLBLCODE
              , ROW_NUMBER() OVER (PARTITION BY KK.顧客番号 ORDER BY KK.入会年月日 DESC) as G_ROW
           FROM MM顧客情報           KM
              LEFT JOIN PS店表示情報ＭＣＣ   PM
              ON KM.入会店舗ＭＣＣ = PM.店番号
              AND KM.入会会社コードＭＣＣ = PM.会社コード
              AND :2 >= PM.開始年月日
              AND :2 <= PM.終了年月日
              , MM顧客属性情報       KZ
              , MM顧客企業別属性情報 KK
              , WM顧客ＲＣＶ         W
          WHERE W.顧客番号 = KM.顧客番号
            AND W.顧客番号 = KZ.顧客番号
            AND W.顧客番号 = KK.顧客番号
            AND KK.企業コード IN (3010,3050,3020,3040,3060)   )
 WHERE G_ROW =1
;

\o
