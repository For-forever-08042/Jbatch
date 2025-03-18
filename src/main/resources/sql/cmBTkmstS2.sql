\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./cmBTkmstS2.log



TRUNCATE TABLE WM商品ＤＮＡ会員情報;
INSERT INTO WM商品ＤＮＡ会員情報( 顧客番号, ＧＯＯＰＯＮ番号,グローバル会員国コード) 
SELECT 
   顧客番号
  ,ＧＯＯＰＯＮ番号
  ,グローバル会員国コード
FROM
(
    SELECT
       W.顧客番号
      ,C.ＧＯＯＰＯＮ番号
      ,S.グローバル会員国コード
      ,ROW_NUMBER() OVER (PARTITION BY C.顧客番号 ORDER BY C.カードステータス, CASE C.サービス種別 WHEN 1 THEN 1 WHEN 4 THEN 2 WHEN 3 THEN 3 WHEN 2 THEN 4 WHEN 5 THEN 5 END,  C.発行年月日 DESC) as G_ROW
    FROM
      MSカード情報   C
     ,MS顧客制度情報 S
     ,WS顧客番号     W
    WHERE
          W.顧客番号     = S.顧客番号
     AND  W.顧客番号     = C.顧客番号
)
WHERE G_ROW =1
;
commit;


\o
