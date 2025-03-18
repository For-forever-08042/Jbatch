

\set LAST_MONTH :1  /* 前月YYYYMM */
\set THIS_MONTH :2  /* 当月YYYYMM */
\set START_DATE :3  /* 前々月27日 */
\set END_DATE :4    /* 前月26日 */
\set SDATE :5



\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset

\o ./ANA_POINT_:SDATE.csv

SELECT 
CONCAT(TO_CHAR(TO_DATE(TO_CHAR(登録年月日), 'YYYYMMDD'), 'YYYY/MM/DD') ,',',
付与ポイント ,',',
COUNT(付与ポイント))as 付与件数/*出力項目*/
FROM
(SELECT 登録年月日,
付与ポイント
FROM HSポイント日別情報:THIS_MONTH/*処理日当月*/
WHERE 登録年月日 BETWEEN :START_DATE and :END_DATE
/*20230328理由コードを100で割った余りで判断、会社コード=1000に修正*/
AND mod(理由コード,100) = 95
AND 会社コードＭＣＣ = 1000
AND 店番号ＭＣＣ = 9963
union all
SELECT 登録年月日,
付与ポイント
FROM HSポイント日別情報:LAST_MONTH/*処理日前月*/
WHERE 登録年月日 BETWEEN :START_DATE and :END_DATE
AND mod(理由コード,100) = 95
AND 会社コードＭＣＣ = 1000
AND 店番号ＭＣＣ = 9963
)
GROUP BY 登録年月日,付与ポイント
ORDER BY 登録年月日,付与ポイント 
;

\o



