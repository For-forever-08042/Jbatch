\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

TRUNCATE TABLE WS会員ＧＯＯＰＯＮ番号;

/* WS会員ＧＯＯＰＯＮ番号登録 */
INSERT INTO WS会員ＧＯＯＰＯＮ番号 ( 会員番号, ＧＯＯＰＯＮ番号) 
  SELECT DISTINCT C.会員番号 , C.ＧＯＯＰＯＮ番号 
  FROM MSカード情報 C, PS会員番号体系 P
  WHERE C.会員番号 BETWEEN P.会員番号開始 AND P.会員番号終了
  AND C.サービス種別 = P.サービス種別
  AND C.会員番号 IN ( SELECT DISTINCT CAST(NULLIF(TRIM(J.顧客コード),'')AS NUMERIC)
                 FROM   WSサンプル品お渡し実績 J )
;

commit;
