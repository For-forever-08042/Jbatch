\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./cmBTposmS.log


TRUNCATE TABLE WS売上明細会員ＧＯＯＰＯＮ番号;

/* WS売上明細会員ＧＯＯＰＯＮ番号登録 */
INSERT /*+ APPEND */ INTO WS売上明細会員ＧＯＯＰＯＮ番号 ( 会員番号, ＧＯＯＰＯＮ番号) 
  SELECT DISTINCT C.会員番号 , C.ＧＯＯＰＯＮ番号 
  FROM MSカード情報 C, PS会員番号体系 P
  WHERE C.会員番号 BETWEEN P.会員番号開始 AND P.会員番号終了
  AND C.サービス種別 = P.サービス種別
  AND CAST(C.会員番号 AS VARCHAR) IN (SELECT DISTINCT NULLIF(RTRIM(J.会員ＩＤ),'')
                 FROM   WS売上明細 J )
;
commit;
\o
