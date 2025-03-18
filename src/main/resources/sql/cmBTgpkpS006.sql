
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp006.tmp
--家族会員数
SELECT TO_CHAR(COUNT(1),'FM999,999,999') FROM MS顧客制度情報 M WHERE M.顧客ステータス = 1 AND 家族ＩＤ != 0;
\o
