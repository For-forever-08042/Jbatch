\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
\o ./cmBTgpst002.tmp
--内、新規登録
SELECT
    TO_CHAR(COUNT(distinct 顧客番号),'FM999,999,999')
FROM
    MSカード情報 K
    ,PS会員番号体系 P
WHERE
    EXISTS (SELECT 1 FROM WSコーポレート顧客番号 T WHERE T.顧客番号 = K.顧客番号)
    AND K.会員番号 >= P.会員番号開始
    AND K.会員番号 <= P.会員番号終了
    AND K.カードステータス = 1
    AND P.カード種類 in (9,32)
;
\o
