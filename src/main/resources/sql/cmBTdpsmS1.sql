\set BDATE :1                                                                                     --バッチ処理日
\set LAST_MONTH :2                                                                                --前月
\set SFILENAME :3                                                                                 --出力ファイルファイル名
\set START_DATE :4                                                                                --前月16日
\set END_DATE :5                                                                                  --前月月末日

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


\o ./:SFILENAME.csv

SELECT
  CONCAT(NULLIF(TRIM(NVL(T1.ポイント変動区分,'0')), '') ,',',
  TO_CHAR(TO_DATE(TO_CHAR(T1.加盟店発生年月日),'YYYYMMDD'),'YYYY/MM/DD') ,',',
  TO_CHAR(TO_DATE(TO_CHAR(T1.加盟店発生年月日),'YYYYMMDD'),'YYYY/MM/DD') ,' ',
  TO_CHAR(TO_TIMESTAMP(TO_CHAR(LPAD(T1.加盟店発生時刻,6,'0')), 'HH24MISS'),'HH24:MI:SS') ,',',
  NULLIF(TRIM(T1.チャネル区分), '') ,',',
  T1.加盟店コード ,',',
  T1.加盟店支部コード ,',',
  T1.加盟店店舗コード ,',',
  T1.加盟店端末ＮＯ ,',',
  T1.取引番号 ,',',
  NVL(T1.Ｄポイントカード番号,'0') ,',',
  NVL(T1.進呈Ｄポイントフラグ,0) ,',',
  NVL(T1.進呈Ｄポイント数,0) ,',',
  NVL(T1.利用Ｄポイントフラグ,0) ,',',
  NVL(T1.利用Ｄポイント数,0) ,',',
  NULLIF(TRIM(NVL(T1.ポイント進呈色区分１,'0')), '') ,',',
  NVL(T1.ポイント進呈数１,0) ,',',
  NULLIF(TRIM(NVL(T1.ポイント進呈色区分２,'0')), '') ,',',
  NVL(T1.ポイント進呈数２,0) ,',',
  NULLIF(TRIM(NVL(T1.ポイント進呈色区分３,'0')), '') ,',',
  NVL(T1.ポイント進呈数３,0) ,',',
  NVL(T1.来店ポイント進呈数,0) ,',',
  NVL(T1.売上点数,0) ,',',
  NVL(T1.売上金額,0) ,',',
  NVL(T1.売上対象外金額,0) ,',',
  NVL(T1.レシート番号,'0') ,',',
  NVL(T1.ポイント利用数,0) ,',',
  TO_CHAR(TO_DATE(TO_CHAR(T1.ポイント照会日),'YYYYMMDD'),'YYYY/MM/DD') ,',',
  NVL(T1.キャンペーン進呈フラグ,0) ,',',
  NVL(TO_CHAR(T1.Ｄポイント処理年月日時刻,'YYYY/MM/DD HH24:MI:SS'),'') ,',',
  TO_CHAR(TO_DATE(TO_CHAR(T1.最終更新日),'YYYYMMDD'),'YYYY/MM/DD') ,',',
  NVL(TO_CHAR(T1.最終更新日時,'YYYY/MM/DD HH24:MI:SS'),''))
FROM
  TSＤポイント日次取引明細:LAST_MONTH T1
LEFT OUTER JOIN TSＤポイント補正用データ:LAST_MONTH T2
ON CAST(T1.加盟店発生年月日 AS TEXT) = T2.取引日付
  AND CAST(T1.加盟店発生時刻 AS TEXT) = T2.取引時刻
  AND TRIM(T1.チャネル区分) = TRIM(T2.チャネル区分)
  AND TRIM(T1.加盟店コード) = TRIM(T2.取引先コード)
  AND TRIM(T1.加盟店支部コード) = TRIM(T2.取引先支部コード)
  AND TRIM(T1.加盟店店舗コード) = TRIM(T2.取引先店舗コード)
  AND TRIM(T1.加盟店端末ＮＯ) = TRIM(T2.端末識別番号)
  AND LTRIM(TRIM(T1.取引番号),'0') = TRIM(T2.伝票番号)
WHERE T1.チャネル区分 = '01'
  AND (T2.取引日付 IS NULL OR T2.取引日付 = '')
  AND (T2.取引時刻 IS NULL OR T2.取引時刻 = '')
  AND (T2.チャネル区分 IS NULL OR T2.チャネル区分 = '')
  AND (T2.取引先コード IS NULL OR T2.取引先コード = '')
  AND (T2.取引先支部コード IS NULL OR T2.取引先支部コード = '')
  AND (T2.取引先店舗コード IS NULL OR T2.取引先店舗コード = '')
  AND (T2.端末識別番号 IS NULL OR T2.端末識別番号 = '')
  AND (T2.伝票番号 IS NULL OR T2.伝票番号 = '')
  AND T1.加盟店発生年月日 BETWEEN TO_NUMBER(:START_DATE) AND TO_NUMBER(:END_DATE) 
ORDER BY T1.加盟店発生年月日, T1.加盟店発生時刻
;

\o

