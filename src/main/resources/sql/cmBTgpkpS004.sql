\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp004.tmp
--解除会員数
SELECT
    TO_CHAR(COUNT(1),'FM999,999,999')
FROM
    ( 
        SELECT
            H.家族ＩＤ
            , H.会員番号
        FROM
            HS家族情報履歴 H
        WHERE
            H.システム年月日 = :SDATE
            AND H.申込区分 = 2
        GROUP BY
            H.家族ＩＤ
            , H.会員番号
    )
;
\o
