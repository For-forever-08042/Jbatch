DEFINE SDATE=&1
DEFINE SMONTH=&2

set echo off
set pause off
set pages 0
set lines 5000
set trim on
set trims on
set term off
set underline off
set feedback off
set heading off
set verify off
alter session set nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

spool ./KKCR0100.csv

SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０１,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０１,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０１,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０１,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０１)=8 THEN C.ＪＡＮコード０１ || '     ' ELSE NVL(C.ＪＡＮコード０１,'') END ||','||
TO_CHAR(NVL(C.商品購入数０１,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０１,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０１,0),'FM00000') ||','||
NVL(C.購買区分０１,0) ||','||
NVL(C.通常期間限定区分０１,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０１),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０１ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０１ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０２,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０２,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０２,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０２,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０２)=8 THEN C.ＪＡＮコード０２ || '     ' ELSE NVL(C.ＪＡＮコード０２,'') END ||','||
TO_CHAR(NVL(C.商品購入数０２,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０２,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０２,0),'FM00000') ||','||
NVL(C.購買区分０２,0) ||','||
NVL(C.通常期間限定区分０２,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０２),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０２ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０２ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０３,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０３,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０３,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０３,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０３)=8 THEN C.ＪＡＮコード０３ || '     ' ELSE NVL(C.ＪＡＮコード０３,'') END ||','||
TO_CHAR(NVL(C.商品購入数０３,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０３,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０３,0),'FM00000') ||','||
NVL(C.購買区分０３,0) ||','||
NVL(C.通常期間限定区分０３,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０３),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０３ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０３ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０４,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０４,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０４,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０４,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０４)=8 THEN C.ＪＡＮコード０４ || '     ' ELSE NVL(C.ＪＡＮコード０４,'') END ||','||
TO_CHAR(NVL(C.商品購入数０４,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０４,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０４,0),'FM00000') ||','||
NVL(C.購買区分０４,0) ||','||
NVL(C.通常期間限定区分０４,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０４),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０４ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０４ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０５,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０５,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０５,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０５,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０５)=8 THEN C.ＪＡＮコード０５ || '     ' ELSE NVL(C.ＪＡＮコード０５,'') END ||','||
TO_CHAR(NVL(C.商品購入数０５,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０５,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０５,0),'FM00000') ||','||
NVL(C.購買区分０５,0) ||','||
NVL(C.通常期間限定区分０５,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０５),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０５ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０５ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０６,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０６,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０６,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０６,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０６)=8 THEN C.ＪＡＮコード０６ || '     ' ELSE NVL(C.ＪＡＮコード０６,'') END ||','||
TO_CHAR(NVL(C.商品購入数０６,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０６,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０６,0),'FM00000') ||','||
NVL(C.購買区分０６,0) ||','||
NVL(C.通常期間限定区分０６,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０６),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０６ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０６ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０７,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０７,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０７,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０７,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０７)=8 THEN C.ＪＡＮコード０７ || '     ' ELSE NVL(C.ＪＡＮコード０７,'') END ||','||
TO_CHAR(NVL(C.商品購入数０７,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０７,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０７,0),'FM00000') ||','||
NVL(C.購買区分０７,0) ||','||
NVL(C.通常期間限定区分０７,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０７),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０７ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０７ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０８,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０８,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０８,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０８,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０８)=8 THEN C.ＪＡＮコード０８ || '     ' ELSE NVL(C.ＪＡＮコード０８,'') END ||','||
TO_CHAR(NVL(C.商品購入数０８,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０８,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０８,0),'FM00000') ||','||
NVL(C.購買区分０８,0) ||','||
NVL(C.通常期間限定区分０８,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０８),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０８ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０８ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ０９,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ０９,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント０９,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額０９,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード０９)=8 THEN C.ＪＡＮコード０９ || '     ' ELSE NVL(C.ＪＡＮコード０９,'') END ||','||
TO_CHAR(NVL(C.商品購入数０９,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率０９,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別０９,0),'FM00000') ||','||
NVL(C.購買区分０９,0) ||','||
NVL(C.通常期間限定区分０９,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限０９),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント０９ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分０９ = 2
AND B.会社コードＭＣＣ <> 2500
UNION ALL
SELECT
NVL(A.ＧＯＯＰＯＮ番号,0) ||','||
TO_CHAR(NVL(B.旧企業コード,0),'FM0000') ||','||
TO_CHAR(NVL(B.店番号ＭＣＣ,0),'FM0000') ||','||
TO_CHAR(B.ターミナル番号,'FM0000') ||','||
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(SUBSTR(B.購入日時,1,8)||LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0'),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日))||TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000')),'yyyy/MM/dd HH24:MI:SS') END ||','||
TO_CHAR(TO_DATE(TO_CHAR(DECODE(B.登録年月日, 0, B.作業年月日, B.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ||' '||
SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),1,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),3,2) || ':' || SUBSTR(TO_CHAR(DECODE(B.登録年月日, 0, B.作業時刻, B.時刻),'FM000000'),5,2) || ','||
TO_CHAR(B.取引番号,'FM0000000000') ||','||
TO_CHAR(NVL(C.企画ＩＤ１０,0),'FM0000000000') ||','||
TO_CHAR(NVL(B.買上額,0),'S00000000') ||','||
TO_CHAR(NVL(B.ポイント支払金額,0),'S00000000') ||','||
NVL(C.ポイントカテゴリ１０,0) ||','||
TO_CHAR(ADD_MONTHS(TO_DATE(TO_CHAR(DECODE(B.登録年月日,0,B.システム年月日,B.登録年月日)),'yyyyMMdd'),-3),'yyyy') ||','||
TO_CHAR(NVL(C.付与ポイント１０,0),'S00000000') ||','||
TO_CHAR(NVL(C.ポイント対象金額１０,0),'S00000000') ||','||
CASE WHEN LENGTH(C.ＪＡＮコード１０)=8 THEN C.ＪＡＮコード１０ || '     ' ELSE NVL(C.ＪＡＮコード１０,'') END ||','||
TO_CHAR(NVL(C.商品購入数１０,0),'S0000000') ||','||
NVL(C.商品パーセントＰ付与率１０,0) ||','||
TO_CHAR(NVL(C.買上高ポイント種別１０,0),'FM00000') ||','||
NVL(C.購買区分１０,0) ||','||
NVL(C.通常期間限定区分１０,0) ||','||
TO_CHAR(TO_DATE(TO_CHAR(C.ポイント有効期限１０),'yyyyMMdd'),'yyyy/MM/dd')
FROM
 MSカード情報 A,
 ( SELECT HIBETSU.システム年月日,
          HIBETSU.顧客番号,
          HIBETSU.処理通番,
          HIBETSU.会員番号,
          HIBETSU.登録年月日,
          HIBETSU.取引番号,
          HIBETSU.時刻,
          HIBETSU.理由コード,
          HIBETSU.付与ポイント,
          HIBETSU.買上額,
          HIBETSU.作業年月日,
          HIBETSU.作業時刻,
          HIBETSU.購入日時,
          HIBETSU.ポイント支払金額,
          HIBETSU.会社コードＭＣＣ,
          HIBETSU.店番号ＭＣＣ,
          HIBETSU.ターミナル番号,
          KAIINNo.サービス種別,
          PSMMCC.旧企業コード
   FROM   HSポイント日別情報&SMONTH HIBETSU
   JOIN   PS会員番号体系            KAIINNo
     ON   HIBETSU.会員番号 >=  KAIINNo.会員番号開始
    AND   HIBETSU.会員番号 <=  KAIINNo.会員番号終了
   LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
     ON  HIBETSU.会社コードＭＣＣ = PSMMCC.会社コード
    AND  HIBETSU.店番号ＭＣＣ = PSMMCC.店番号
    AND  PSMMCC.開始年月日 <= &SDATE
    AND  PSMMCC.終了年月日 >= &SDATE  ) B,
 HSポイント日別内訳情報&SMONTH C
WHERE
    A.会員番号 = B.会員番号
AND A.サービス種別 = B.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.システム年月日 = &SDATE
AND A.顧客番号 = C.顧客番号 
AND B.顧客番号 = C.顧客番号 
AND B.処理通番 = C.処理通番 
AND B.付与ポイント <> 0
AND C.付与ポイント１０ <> 0
AND MOD(B.理由コード, 100) not in (6,7,9,77,78)
AND C.通常期間限定区分１０ = 2
AND B.会社コードＭＣＣ <> 2500
;

spool off
