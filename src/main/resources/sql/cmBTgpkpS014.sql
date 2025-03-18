\set SDATE to_number(:1)
\set NENTUKI 'to_char(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),''yyyymm'')'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
\o ./cmBTgpkp014.tmp
    SELECT
        CONCAT(V.グループＩＤ , ' ' ,
        TO_CHAR(V.譲渡回数,'FM999,999,999') , ' ' ,
        NVL(TO_CHAR(V.ポイント数,'FM999,999,999'),'0'))
    FROM (
    SELECT
        A.家族ＩＤ AS グループＩＤ
       ,COUNT(A.利用ポイント) AS 譲渡回数
       ,SUM(A.利用ポイント) AS ポイント数
    FROM
        HSポイント日別情報:2 A
        , MS家族制度情報 B
    WHERE
        A.家族ＩＤ = B.家族ＩＤ
        AND MOD(A.理由コード, 100) = 77
        AND A.利用ポイント <> 0
        AND A.システム年月日 >= CAST(CONCAT(:2 , '01') AS NUMERIC)
        AND A.システム年月日 <= :1
    GROUP BY A.家族ＩＤ
    ORDER BY ポイント数 DESC
    LIMIT 5
 ) V
;
\o
