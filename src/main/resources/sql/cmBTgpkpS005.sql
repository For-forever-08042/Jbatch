
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp005.tmp
--家族グループ数
SELECT TO_CHAR(COUNT(1),'FM999,999,999') FROM MS家族制度情報 M WHERE NVL(M.家族削除日,0) = 0;
\o
