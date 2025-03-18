\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./MEMBERINFO_:SDATE.csv


TRUNCATE TABLE WMスマホアプリ会員情報;

INSERT INTO WMスマホアプリ会員情報 (顧客番号, ＧＯＯＰＯＮ番号,企業コード)
  SELECT distinct
     C.顧客番号
    ,C.ＧＯＯＰＯＮ番号
    ,C.企業コード
  FROM
     MSカード情報 C
    ,MS外部認証情報 G
    ,WS顧客番号２ W
    ,MM顧客情報 A
  WHERE
     W.顧客番号     = C.顧客番号
AND  C.サービス種別 = G.サービス種別
AND  C.会員番号     = G.会員番号
AND  C.顧客番号     = A.顧客番号
AND  G.外部認証種別 = 'A'
AND  G.削除フラグ   = 0
AND  G.外部認証ＩＤ = A.アプリユーザＩＤ -- 未削除
;
commit ;


SELECT
CONCAT(A.アプリユーザＩＤ ,',',
CASE WHEN E.退会年月日 is not null AND NVL(E.退会年月日,0) = 0 THEN '1' WHEN D.退会年月日 is not null AND NVL(D.退会年月日,0) = 0 THEN '3' ELSE NULL END  ,',', 
C.ＧＯＯＰＯＮ番号 ,',',
NVL(A.性別,'0') ,',',
CASE NVL(A.誕生年,0) WHEN 0 THEN NULL ELSE TO_CHAR(NVL(A.誕生年,0),'FM9999') END ,',',
CASE NVL(A.誕生月,0) WHEN 0 THEN NULL   ELSE TO_CHAR(NVL(A.誕生月,0),'FM09') END ,',',
A.メールアドレス１送信フラグ ,',',
A.メールアドレス３送信フラグ ,',',
CASE WHEN C.企業コード = 3040  THEN 1  ELSE 2 END   ,',',
CASE B.入会年月日 WHEN 0 THEN '0000-00-00' ELSE TO_CHAR(TO_DATE(TO_CHAR(B.入会年月日),'YYYYMMDD'), 'YYYY-MM-DD') END ,',',
CASE WHEN NVL(A.最終静態更新日,0) = 0 THEN TO_CHAR(TO_DATE(CONCAT(TO_CHAR(A.最終更新日) , ' 00:00:00'),'YYYYMMDD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(A.最終静態更新日),TO_CHAR(A.最終静態更新時刻,'099999')),'YYYYMMDDHH24MISS'), 'YYYY-MM-DD HH24:MI:SS') END)
FROM
  MM顧客情報 A
  LEFT JOIN MM顧客企業別属性情報 B          /* MK-アプリ */
  ON A.顧客番号 = B.顧客番号 AND 3040       = B.企業コード          /*：MK-アプリ */
  LEFT JOIN MM顧客企業別属性情報 D          /* MK-現金 */
  ON A.顧客番号 = D.顧客番号 AND 3010       = D.企業コード          /*：MK-現金 */
  LEFT JOIN MM顧客企業別属性情報 E          /* MK-クレカ */
  ON A.顧客番号 = E.顧客番号 AND 3050       = E.企業コード          /*：MK-クレカ */ 
 ,WMスマホアプリ会員情報 C                     /* MK-アプリ */  
WHERE
    A.アプリユーザＩＤ is not null and A.アプリユーザＩＤ !=''
AND A.顧客番号 = C.顧客番号
;


\o

