\set SDATE :1
\set SMONTH :2

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./KKCR0110.csv


select /* 明細０１ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０１) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０１),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０１,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
        AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
        AND  PSMMCC.開始年月日 <= :SDATE
        AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
     AND CARD.サービス種別 = HIBETSU.サービス種別
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０１ <> 0
     AND UCHIWAKE.通常期間限定区分０１ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０２ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０２) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０２),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０２,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０２ <> 0
     AND UCHIWAKE.通常期間限定区分０２ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０３ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０３) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０３),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０３,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
     AND CARD.サービス種別 = HIBETSU.サービス種別
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０３ <> 0
     AND UCHIWAKE.通常期間限定区分０３ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０４ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０４) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０４),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０４,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０４ <> 0
     AND UCHIWAKE.通常期間限定区分０４ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０５ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０５) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０５),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０５,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０５ <> 0
     AND UCHIWAKE.通常期間限定区分０５ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０６ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０６) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０６),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０６,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０６ <> 0
     AND UCHIWAKE.通常期間限定区分０６ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０７ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０７) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０７),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０７,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０７ <> 0
     AND UCHIWAKE.通常期間限定区分０７ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０８ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０８) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０８),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０８,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０８ <> 0
     AND UCHIWAKE.通常期間限定区分０８ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細０９ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分０９) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限０９),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント０９,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント０９ <> 0
     AND UCHIWAKE.通常期間限定区分０９ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
UNION ALL
select /* 明細１０ */
CONCAT(TO_CHAR(CARD.ＧＯＯＰＯＮ番号) ,',',                                                    --顧客番号
TO_CHAR(TO_DATE(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業年月日, HIBETSU.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,' ', 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),1,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),3,2) , ':' , 
SUBSTR(TO_CHAR(DECODE(HIBETSU.登録年月日, '0', HIBETSU.作業時刻, HIBETSU.時刻),'FM000000'),5,2) , ',',      --還元日時
TO_CHAR(HIBETSU.還元種別) ,',',                                                         --還元種別
TO_CHAR(NVL(HIBETSU.旧企業コード,0),'FM0000') ,',',                                     --企業コード
TO_CHAR(NVL(HIBETSU.店番号ＭＣＣ,0),'FM0000') ,',',                                     --店舗コード
TO_CHAR(HIBETSU.ターミナル番号,'FM0000') ,',',                                          --レジ番号
TO_CHAR(HIBETSU.取引番号,'FM0000000000') ,',',                                          --取引番号
TO_CHAR(CAST(HIBETSU.変更ＩＤ AS NUMERIC),'FM0000000000') ,',',                                          --還元ID
TO_CHAR(UCHIWAKE.購買区分１０) ,',',                                                    --売上連動/非連動
'2,',                                                                                    --年度/期間限定
TO_CHAR(TO_DATE(TO_CHAR(UCHIWAKE.ポイント有効期限１０),'YYYYMMDD'),'YYYY/MM/DD') ,',',  --有効期限
TO_CHAR(NVL(UCHIWAKE.利用ポイント１０,0)*(-1),'S00000000'))                                --ポイント数
from 
   ( SELECT PntHBT.システム年月日,
            PntHBT.顧客番号,
            PntHBT.処理通番,
            PntHBT.会員番号,
            PntHBT.登録年月日,
            PntHBT.取引番号,
            PntHBT.時刻,
            PntHBT.理由コード,
            PntHBT.利用ポイント,
            PntHBT.買上額,
            PntHBT.作業年月日,
            PntHBT.作業時刻,
            PntHBT.購入日時,
            PntHBT.ポイント支払金額,
            PntHBT.会社コードＭＣＣ,
            PntHBT.店番号ＭＣＣ,
            PntHBT.変更ＩＤ,
            PntHBT.ターミナル番号,
            PntHBT.還元種別,
            KAIINNo.サービス種別,
            PSMMCC.旧企業コード
      FROM  HSポイント日別情報:SMONTH PntHBT
      JOIN  PS会員番号体系            KAIINNo
        ON  PntHBT.会員番号 >=  KAIINNo.会員番号開始
       AND  PntHBT.会員番号 <=  KAIINNo.会員番号終了
      LEFT JOIN  PS店表示情報ＭＣＣ PSMMCC
        ON  PntHBT.会社コードＭＣＣ = PSMMCC.会社コード
       AND  PntHBT.店番号ＭＣＣ = PSMMCC.店番号
       AND  PSMMCC.開始年月日 <= :SDATE
       AND  PSMMCC.終了年月日 >= :SDATE ) HIBETSU
    JOIN HSポイント日別内訳情報:SMONTH UCHIWAKE
      ON UCHIWAKE.システム年月日 = HIBETSU.システム年月日
     AND UCHIWAKE.顧客番号 = HIBETSU.顧客番号
     AND UCHIWAKE.処理通番 = HIBETSU.処理通番
    JOIN MSカード情報  CARD
      ON CARD.会員番号 = HIBETSU.会員番号
where
         HIBETSU.システム年月日 = :SDATE
     AND HIBETSU.利用ポイント <> 0
     AND MOD(HIBETSU.理由コード, 100) NOT IN (6,7,77,78,90,91,92,93,94)
     AND UCHIWAKE.利用ポイント１０ <> 0
     AND UCHIWAKE.通常期間限定区分１０ = 2
     AND HIBETSU.会社コードＭＣＣ <> 2500
;

\o
