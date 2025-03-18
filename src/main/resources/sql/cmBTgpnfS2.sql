\set BAT_DATE 'to_number(to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymmdd''))'
\set SMONTH :2
\set BAT_DATE_1 'to_number(to_char(nvl(to_date(cast(:3 as text),''yyyymmdd''),sysdate()),''yyyymmdd''))'
\set SMONTH_1 :SMONTH_1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./cmBTgpnfS2_:SDATE.log

TRUNCATE TABLE WS会員ＧＯＯＰＯＮ番号;

/* WS会員ＧＯＯＰＯＮ番号登録 */
INSERT INTO WS会員ＧＯＯＰＯＮ番号 ( 会員番号, ＧＯＯＰＯＮ番号) 
  SELECT DISTINCT C.会員番号 , C.ＧＯＯＰＯＮ番号 
  FROM MSカード情報 C, PS会員番号体系 P
  WHERE C.会員番号 BETWEEN P.会員番号開始 AND P.会員番号終了
  AND C.サービス種別 = P.サービス種別
  AND C.会員番号 IN ( SELECT DISTINCT J.会員番号
                 FROM   MSクーポン企画実績:SMONTH J 
                 WHERE  J.最終更新日 = :BAT_DATE
                   AND  J.登録区分 = '1'
                   AND  J.取引区分 = '3'
                UNION
                SELECT DISTINCT K.会員番号
                 FROM   MSクーポン企画実績:SMONTH_1 K
                 WHERE  K.最終更新日 = :BAT_DATE_1
                   AND  K.登録区分 = '2'
                   AND  K.取引区分 = '1'
               )
;
commit ;

\o

