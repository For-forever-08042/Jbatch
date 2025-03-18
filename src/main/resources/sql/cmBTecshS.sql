\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

TRUNCATE TABLE WS会員ＧＯＯＰＯＮ番号;

INSERT INTO WS会員ＧＯＯＰＯＮ番号 ( 会員番号, ＧＯＯＰＯＮ番号) 
  SELECT DISTINCT CAST(J.会員番号 AS NUMERIC) , NVL(C.ＧＯＯＰＯＮ番号,0) 
  FROM WS購買履歴 J
       LEFT JOIN MSカード情報 C ON LTRIM(TRIM(J.会員番号),'0') = TO_CHAR(C.会員番号), 
       PS会員番号体系 P
  WHERE C.会員番号 BETWEEN P.会員番号開始 AND P.会員番号終了
    AND C.サービス種別 = P.サービス種別
    AND J.データ区分 = '2'
    AND LTRIM(TRIM(J.会員番号),'0') > '0'
  UNION
  SELECT DISTINCT CAST(J.会員番号 AS NUMERIC) , NVL(C.ＧＯＯＰＯＮ番号,0) 
  FROM WS購買履歴 J
       LEFT JOIN MSカード情報 C ON LTRIM(TRIM(J.会員番号),'0') = TO_CHAR(C.ＧＯＯＰＯＮ番号), 
       PS会員番号体系 P
  WHERE C.会員番号 BETWEEN P.会員番号開始 AND P.会員番号終了
    AND C.サービス種別 = P.サービス種別
    AND J.データ区分 = '1'
    AND LTRIM(TRIM(J.会員番号),'0') > '0'
;
