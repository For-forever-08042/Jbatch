\set SDATE :1
\set NENTUKI :2
\set NENTUKI_LAST :3
\set NENTUKI_NEXT :4

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select アクティブテーブル名 ATBL_TEXT from PSテーブルアクティブ作動管理 WHERE 区分 = 1 \gset
\o ./KKMI0110.csv


SELECT CONCAT(ROW_NUMBER() OVER() , ',', CSVTEXT )
FROM (
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０１ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０１ ,',',
C.ポイントカテゴリ０１ ,',',
C.ポイント種別０１ ,',',
NVL(LPAD(C.買上高ポイント種別０１,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０１>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０１) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０１,0) ,',',
NVL(C.ＪＡＮコード０１,'') ,',',
ABS(NVL(C.商品購入数０１,0)) ,',',
NVL(C.商品パーセントＰ付与率０１,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０１,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０１, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０１),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０１,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０１ = D.企画ＩＤ
AND C.企画バージョン０１ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０１ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)  
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０２ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０２ ,',',
C.ポイントカテゴリ０２ ,',',
C.ポイント種別０２ ,',',
NVL(LPAD(C.買上高ポイント種別０２,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０２>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０２) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０２,0) ,',',
NVL(C.ＪＡＮコード０２,'') ,',',
ABS(NVL(C.商品購入数０２,0)) ,',',
NVL(C.商品パーセントＰ付与率０２,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０２,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０２, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０２),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０２,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０２ = D.企画ＩＤ
AND C.企画バージョン０２ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０２ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０３ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０３ ,',',
C.ポイントカテゴリ０３ ,',',
C.ポイント種別０３ ,',',
NVL(LPAD(C.買上高ポイント種別０３,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０３>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０３) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０３,0) ,',',
NVL(C.ＪＡＮコード０３,'') ,',',
ABS(NVL(C.商品購入数０３,0)) ,',',
NVL(C.商品パーセントＰ付与率０３,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０３,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０３, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０３),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０３,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０３ = D.企画ＩＤ
AND C.企画バージョン０３ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０３ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０４ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０４ ,',',
C.ポイントカテゴリ０４ ,',',
C.ポイント種別０４ ,',',
NVL(LPAD(C.買上高ポイント種別０４,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０４>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０４) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０４,0) ,',',
NVL(C.ＪＡＮコード０４,'') ,',',
ABS(NVL(C.商品購入数０４,0)) ,',',
NVL(C.商品パーセントＰ付与率０４,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０４,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０４, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０４),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０４,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０４ = D.企画ＩＤ
AND C.企画バージョン０４ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０４ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０５ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０５ ,',',
C.ポイントカテゴリ０５ ,',',
C.ポイント種別０５ ,',',
NVL(LPAD(C.買上高ポイント種別０５,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０５>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０５) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０５,0) ,',',
NVL(C.ＪＡＮコード０５,'') ,',',
ABS(NVL(C.商品購入数０５,0)) ,',',
NVL(C.商品パーセントＰ付与率０５,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０５,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０５, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０５),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０５,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０５ = D.企画ＩＤ
AND C.企画バージョン０５ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０５ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０６ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０６ ,',',
C.ポイントカテゴリ０６ ,',',
C.ポイント種別０６ ,',',
NVL(LPAD(C.買上高ポイント種別０６,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０６>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０６) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０６,0) ,',',
NVL(C.ＪＡＮコード０６,'') ,',',
ABS(NVL(C.商品購入数０６,0)) ,',',
NVL(C.商品パーセントＰ付与率０６,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０６,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０６, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０６),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０６,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０６ = D.企画ＩＤ
AND C.企画バージョン０６ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０６ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０７ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０７ ,',',
C.ポイントカテゴリ０７ ,',',
C.ポイント種別０７ ,',',
NVL(LPAD(C.買上高ポイント種別０７,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０７>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０７) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０７,0) ,',',
NVL(C.ＪＡＮコード０７,'') ,',',
ABS(NVL(C.商品購入数０７,0)) ,',',
NVL(C.商品パーセントＰ付与率０７,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０７,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０７, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０７),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０７,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０７ = D.企画ＩＤ
AND C.企画バージョン０７ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０７ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０８ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０８ ,',',
C.ポイントカテゴリ０８ ,',',
C.ポイント種別０８ ,',',
NVL(LPAD(C.買上高ポイント種別０８,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０８>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０８) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０８,0) ,',',
NVL(C.ＪＡＮコード０８,'') ,',',
ABS(NVL(C.商品購入数０８,0)) ,',',
NVL(C.商品パーセントＰ付与率０８,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０８,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０８, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０８),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０８,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０８ = D.企画ＩＤ
AND C.企画バージョン０８ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０８ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０９ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０９ ,',',
C.ポイントカテゴリ０９ ,',',
C.ポイント種別０９ ,',',
NVL(LPAD(C.買上高ポイント種別０９,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０９>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０９) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０９,0) ,',',
NVL(C.ＪＡＮコード０９,'') ,',',
ABS(NVL(C.商品購入数０９,0)) ,',',
NVL(C.商品パーセントＰ付与率０９,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０９,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０９, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０９),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０９,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０９ = D.企画ＩＤ
AND C.企画バージョン０９ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０９ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号１０ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン１０ ,',',
C.ポイントカテゴリ１０ ,',',
C.ポイント種別１０ ,',',
NVL(LPAD(C.買上高ポイント種別１０,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント１０>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント１０) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額１０,0) ,',',
NVL(C.ＪＡＮコード１０,'') ,',',
ABS(NVL(C.商品購入数１０,0)) ,',',
NVL(C.商品パーセントＰ付与率１０,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ１０,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分１０, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限１０),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分１０,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
E.店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',E.ＭＫ取引番号,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.会社コードＭＣＣ <> 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ１０ = D.企画ＩＤ
AND C.企画バージョン１０ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分１０ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78) 
UNION
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０１ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０１ ,',',
C.ポイントカテゴリ０１ ,',',
C.ポイント種別０１ ,',',
NVL(LPAD(C.買上高ポイント種別０１,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０１>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０１) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０１,0) ,',',
NVL(C.ＪＡＮコード０１,'') ,',',
ABS(NVL(C.商品購入数０１,0)) ,',',
NVL(C.商品パーセントＰ付与率０１,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０１,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０１, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０１),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０１,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０１ = D.企画ＩＤ
AND C.企画バージョン０１ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０１ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０２ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０２ ,',',
C.ポイントカテゴリ０２ ,',',
C.ポイント種別０２ ,',',
NVL(LPAD(C.買上高ポイント種別０２,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０２>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０２) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０２,0) ,',',
NVL(C.ＪＡＮコード０２,'') ,',',
ABS(NVL(C.商品購入数０２,0)) ,',',
NVL(C.商品パーセントＰ付与率０２,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０２,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０２, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０２),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０２,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０２ = D.企画ＩＤ
AND C.企画バージョン０２ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０２ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０３ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０３ ,',',
C.ポイントカテゴリ０３ ,',',
C.ポイント種別０３ ,',',
NVL(LPAD(C.買上高ポイント種別０３,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０３>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０３) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０３,0) ,',',
NVL(C.ＪＡＮコード０３,'') ,',',
ABS(NVL(C.商品購入数０３,0)) ,',',
NVL(C.商品パーセントＰ付与率０３,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０３,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０３, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０３),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０３,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０３ = D.企画ＩＤ
AND C.企画バージョン０３ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０３ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０４ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０４ ,',',
C.ポイントカテゴリ０４ ,',',
C.ポイント種別０４ ,',',
NVL(LPAD(C.買上高ポイント種別０４,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０４>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０４) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０４,0) ,',',
NVL(C.ＪＡＮコード０４,'') ,',',
ABS(NVL(C.商品購入数０４,0)) ,',',
NVL(C.商品パーセントＰ付与率０４,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０４,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０４, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０４),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０４,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０４ = D.企画ＩＤ
AND C.企画バージョン０４ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０４ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０５ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０５ ,',',
C.ポイントカテゴリ０５ ,',',
C.ポイント種別０５ ,',',
NVL(LPAD(C.買上高ポイント種別０５,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０５>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０５) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０５,0) ,',',
NVL(C.ＪＡＮコード０５,'') ,',',
ABS(NVL(C.商品購入数０５,0)) ,',',
NVL(C.商品パーセントＰ付与率０５,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０５,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０５, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０５),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０５,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０５ = D.企画ＩＤ
AND C.企画バージョン０５ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０５ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０６ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０６ ,',',
C.ポイントカテゴリ０６ ,',',
C.ポイント種別０６ ,',',
NVL(LPAD(C.買上高ポイント種別０６,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０６>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０６) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０６,0) ,',',
NVL(C.ＪＡＮコード０６,'') ,',',
ABS(NVL(C.商品購入数０６,0)) ,',',
NVL(C.商品パーセントＰ付与率０６,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０６,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０６, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０６),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０６,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０６ = D.企画ＩＤ
AND C.企画バージョン０６ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０６ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０７ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０７ ,',',
C.ポイントカテゴリ０７ ,',',
C.ポイント種別０７ ,',',
NVL(LPAD(C.買上高ポイント種別０７,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０７>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０７) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０７,0) ,',',
NVL(C.ＪＡＮコード０７,'') ,',',
ABS(NVL(C.商品購入数０７,0)) ,',',
NVL(C.商品パーセントＰ付与率０７,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０７,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０７, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０７),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０７,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０７ = D.企画ＩＤ
AND C.企画バージョン０７ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０７ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０８ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０８ ,',',
C.ポイントカテゴリ０８ ,',',
C.ポイント種別０８ ,',',
NVL(LPAD(C.買上高ポイント種別０８,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０８>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０８) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０８,0) ,',',
NVL(C.ＪＡＮコード０８,'') ,',',
ABS(NVL(C.商品購入数０８,0)) ,',',
NVL(C.商品パーセントＰ付与率０８,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０８,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０８, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０８),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０８,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０８ = D.企画ＩＤ
AND C.企画バージョン０８ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０８ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０９ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０９ ,',',
C.ポイントカテゴリ０９ ,',',
C.ポイント種別０９ ,',',
NVL(LPAD(C.買上高ポイント種別０９,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０９>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０９) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０９,0) ,',',
NVL(C.ＪＡＮコード０９,'') ,',',
ABS(NVL(C.商品購入数０９,0)) ,',',
NVL(C.商品パーセントＰ付与率０９,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０９,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０９, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０９),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０９,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０９ = D.企画ＩＤ
AND C.企画バージョン０９ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０９ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号１０ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン１０ ,',',
C.ポイントカテゴリ１０ ,',',
C.ポイント種別１０ ,',',
NVL(LPAD(C.買上高ポイント種別１０,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント１０>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント１０) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額１０,0) ,',',
NVL(C.ＪＡＮコード１０,'') ,',',
ABS(NVL(C.商品購入数１０,0)) ,',',
NVL(C.商品パーセントＰ付与率１０,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ１０,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分１０, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限１０),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分１０,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ１０ = D.企画ＩＤ
AND C.企画バージョン１０ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分１０ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０１ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０１ ,',',
C.ポイントカテゴリ０１ ,',',
C.ポイント種別０１ ,',',
NVL(LPAD(C.買上高ポイント種別０１,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０１>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０１) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０１,0) ,',',
NVL(C.ＪＡＮコード０１,'') ,',',
ABS(NVL(C.商品購入数０１,0)) ,',',
NVL(C.商品パーセントＰ付与率０１,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０１,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０１, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０１),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０１,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０１ = D.企画ＩＤ
AND C.企画バージョン０１ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０１ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０２ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０２ ,',',
C.ポイントカテゴリ０２ ,',',
C.ポイント種別０２ ,',',
NVL(LPAD(C.買上高ポイント種別０２,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０２>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０２) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０２,0) ,',',
NVL(C.ＪＡＮコード０２,'') ,',',
ABS(NVL(C.商品購入数０２,0)) ,',',
NVL(C.商品パーセントＰ付与率０２,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０２,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０２, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０２),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０２,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０２ = D.企画ＩＤ
AND C.企画バージョン０２ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０２ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０３ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０３ ,',',
C.ポイントカテゴリ０３ ,',',
C.ポイント種別０３ ,',',
NVL(LPAD(C.買上高ポイント種別０３,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０３>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０３) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０３,0) ,',',
NVL(C.ＪＡＮコード０３,'') ,',',
ABS(NVL(C.商品購入数０３,0)) ,',',
NVL(C.商品パーセントＰ付与率０３,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０３,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０３, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０３),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０３,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０３ = D.企画ＩＤ
AND C.企画バージョン０３ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０３ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０４ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０４ ,',',
C.ポイントカテゴリ０４ ,',',
C.ポイント種別０４ ,',',
NVL(LPAD(C.買上高ポイント種別０４,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０４>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０４) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０４,0) ,',',
NVL(C.ＪＡＮコード０４,'') ,',',
ABS(NVL(C.商品購入数０４,0)) ,',',
NVL(C.商品パーセントＰ付与率０４,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０４,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０４, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０４),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０４,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０４ = D.企画ＩＤ
AND C.企画バージョン０４ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０４ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０５ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０５ ,',',
C.ポイントカテゴリ０５ ,',',
C.ポイント種別０５ ,',',
NVL(LPAD(C.買上高ポイント種別０５,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０５>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０５) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０５,0) ,',',
NVL(C.ＪＡＮコード０５,'') ,',',
ABS(NVL(C.商品購入数０５,0)) ,',',
NVL(C.商品パーセントＰ付与率０５,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０５,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０５, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０５),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０５,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０５ = D.企画ＩＤ
AND C.企画バージョン０５ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０５ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０６ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０６ ,',',
C.ポイントカテゴリ０６ ,',',
C.ポイント種別０６ ,',',
NVL(LPAD(C.買上高ポイント種別０６,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０６>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０６) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０６,0) ,',',
NVL(C.ＪＡＮコード０６,'') ,',',
ABS(NVL(C.商品購入数０６,0)) ,',',
NVL(C.商品パーセントＰ付与率０６,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０６,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０６, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０６),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０６,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０６ = D.企画ＩＤ
AND C.企画バージョン０６ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０６ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０７ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０７ ,',',
C.ポイントカテゴリ０７ ,',',
C.ポイント種別０７ ,',',
NVL(LPAD(C.買上高ポイント種別０７,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０７>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０７) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０７,0) ,',',
NVL(C.ＪＡＮコード０７,'') ,',',
ABS(NVL(C.商品購入数０７,0)) ,',',
NVL(C.商品パーセントＰ付与率０７,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０７,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０７, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０７),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０７,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０７ = D.企画ＩＤ
AND C.企画バージョン０７ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０７ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０８ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０８ ,',',
C.ポイントカテゴリ０８ ,',',
C.ポイント種別０８ ,',',
NVL(LPAD(C.買上高ポイント種別０８,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０８>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０８) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０８,0) ,',',
NVL(C.ＪＡＮコード０８,'') ,',',
ABS(NVL(C.商品購入数０８,0)) ,',',
NVL(C.商品パーセントＰ付与率０８,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０８,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０８, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０８),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０８,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０８ = D.企画ＩＤ
AND C.企画バージョン０８ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０８ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０９ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０９ ,',',
C.ポイントカテゴリ０９ ,',',
C.ポイント種別０９ ,',',
NVL(LPAD(C.買上高ポイント種別０９,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０９>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０９) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０９,0) ,',',
NVL(C.ＪＡＮコード０９,'') ,',',
ABS(NVL(C.商品購入数０９,0)) ,',',
NVL(C.商品パーセントＰ付与率０９,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０９,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０９, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０９),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０９,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０９ = D.企画ＩＤ
AND C.企画バージョン０９ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０９ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号１０ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン１０ ,',',
C.ポイントカテゴリ１０ ,',',
C.ポイント種別１０ ,',',
NVL(LPAD(C.買上高ポイント種別１０,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント１０>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント１０) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額１０,0) ,',',
NVL(C.ＪＡＮコード１０,'') ,',',
ABS(NVL(C.商品購入数１０,0)) ,',',
NVL(C.商品パーセントＰ付与率１０,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ１０,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分１０, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限１０),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分１０,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ１０ = D.企画ＩＤ
AND C.企画バージョン１０ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分１０ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０１ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０１ ,',',
C.ポイントカテゴリ０１ ,',',
C.ポイント種別０１ ,',',
NVL(LPAD(C.買上高ポイント種別０１,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０１>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０１) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０１,0) ,',',
NVL(C.ＪＡＮコード０１,'') ,',',
ABS(NVL(C.商品購入数０１,0)) ,',',
NVL(C.商品パーセントＰ付与率０１,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０１,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０１, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０１),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０１,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０１ = D.企画ＩＤ
AND C.企画バージョン０１ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０１ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０２ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０２ ,',',
C.ポイントカテゴリ０２ ,',',
C.ポイント種別０２ ,',',
NVL(LPAD(C.買上高ポイント種別０２,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０２>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０２) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０２,0) ,',',
NVL(C.ＪＡＮコード０２,'') ,',',
ABS(NVL(C.商品購入数０２,0)) ,',',
NVL(C.商品パーセントＰ付与率０２,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０２,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０２, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０２),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０２,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０２ = D.企画ＩＤ
AND C.企画バージョン０２ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０２ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０３ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０３ ,',',
C.ポイントカテゴリ０３ ,',',
C.ポイント種別０３ ,',',
NVL(LPAD(C.買上高ポイント種別０３,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０３>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０３) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０３,0) ,',',
NVL(C.ＪＡＮコード０３,'') ,',',
ABS(NVL(C.商品購入数０３,0)) ,',',
NVL(C.商品パーセントＰ付与率０３,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０３,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０３, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０３),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０３,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０３ = D.企画ＩＤ
AND C.企画バージョン０３ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０３ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０４ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０４ ,',',
C.ポイントカテゴリ０４ ,',',
C.ポイント種別０４ ,',',
NVL(LPAD(C.買上高ポイント種別０４,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０４>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０４) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０４,0) ,',',
NVL(C.ＪＡＮコード０４,'') ,',',
ABS(NVL(C.商品購入数０４,0)) ,',',
NVL(C.商品パーセントＰ付与率０４,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０４,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０４, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０４),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０４,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０４ = D.企画ＩＤ
AND C.企画バージョン０４ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０４ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０５ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０５ ,',',
C.ポイントカテゴリ０５ ,',',
C.ポイント種別０５ ,',',
NVL(LPAD(C.買上高ポイント種別０５,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０５>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０５) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０５,0) ,',',
NVL(C.ＪＡＮコード０５,'') ,',',
ABS(NVL(C.商品購入数０５,0)) ,',',
NVL(C.商品パーセントＰ付与率０５,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０５,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０５, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０５),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０５,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０５ = D.企画ＩＤ
AND C.企画バージョン０５ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０５ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０６ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０６ ,',',
C.ポイントカテゴリ０６ ,',',
C.ポイント種別０６ ,',',
NVL(LPAD(C.買上高ポイント種別０６,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０６>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０６) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０６,0) ,',',
NVL(C.ＪＡＮコード０６,'') ,',',
ABS(NVL(C.商品購入数０６,0)) ,',',
NVL(C.商品パーセントＰ付与率０６,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０６,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０６, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０６),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０６,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０６ = D.企画ＩＤ
AND C.企画バージョン０６ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０６ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０７ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０７ ,',',
C.ポイントカテゴリ０７ ,',',
C.ポイント種別０７ ,',',
NVL(LPAD(C.買上高ポイント種別０７,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０７>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０７) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０７,0) ,',',
NVL(C.ＪＡＮコード０７,'') ,',',
ABS(NVL(C.商品購入数０７,0)) ,',',
NVL(C.商品パーセントＰ付与率０７,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０７,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０７, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０７),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０７,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０７ = D.企画ＩＤ
AND C.企画バージョン０７ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０７ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０８ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０８ ,',',
C.ポイントカテゴリ０８ ,',',
C.ポイント種別０８ ,',',
NVL(LPAD(C.買上高ポイント種別０８,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０８>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０８) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０８,0) ,',',
NVL(C.ＪＡＮコード０８,'') ,',',
ABS(NVL(C.商品購入数０８,0)) ,',',
NVL(C.商品パーセントＰ付与率０８,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０８,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０８, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０８),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０８,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０８ = D.企画ＩＤ
AND C.企画バージョン０８ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０８ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号０９ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン０９ ,',',
C.ポイントカテゴリ０９ ,',',
C.ポイント種別０９ ,',',
NVL(LPAD(C.買上高ポイント種別０９,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント０９>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント０９) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額０９,0) ,',',
NVL(C.ＪＡＮコード０９,'') ,',',
ABS(NVL(C.商品購入数０９,0)) ,',',
NVL(C.商品パーセントＰ付与率０９,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ０９,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分０９, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限０９),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分０９,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ０９ = D.企画ＩＤ
AND C.企画バージョン０９ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分０９ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
UNION ALL
SELECT 
CONCAT(
A.ＧＯＯＰＯＮ番号 ,',',
NVL(LPAD(B.カード種別,3,'0'),'') ,',',
LPAD(B.店番号ＭＣＣ,4,'0') ,',',
CASE WHEN B.送信日時 > 0 THEN TO_CHAR(TO_DATE(TO_CHAR(B.送信日時),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE '' END ,',',
TO_CHAR(NVL(B.リアル更新日時,B.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),'yyyy/MM/dd') , ' ' ,
DECODE(B.時刻,'0','00:00:00',TO_CHAR(TO_DATE(LPAD(B.時刻,6,'0'),'HH24MISS'),'HH24:MI:SS')) ,',',
CASE WHEN B.購入日時 > 0 THEN TO_CHAR(TO_DATE(CONCAT(SUBSTR(B.購入日時,1,8),LPAD(NVL(SUBSTR(B.購入日時,9),'0'),6,'0')),'yyyyMMddHH24MISS'),'yyyy/MM/dd HH24:MI:SS') ELSE TO_CHAR(TO_DATE(CONCAT(TO_CHAR(B.登録年月日),DECODE(B.時刻,'0','000000',LPAD(B.時刻,6,'0')))),'yyyy/MM/dd HH24:MI:SS') END ,',',
LPAD(B.ターミナル番号,5,'0') ,',',
LPAD(B.取引番号,10,'0') ,',',
B.電文ＳＥＱ番号 ,',',
ABS(NVL(B.買上額,0)) ,',',
B.ポイント支払金額 ,',',
B.他社クレジット区分 ,',',
NVL(B.ＰＯＳ種別,'') ,',',
NVL(B.登録経路,'') ,',',
NVL(SUBSTR(B.取引区分,1,3),'') ,',',
C.明細番号１０ ,',',
NVL(TRIM(D.発注番号),'') ,',',
C.企画バージョン１０ ,',',
C.ポイントカテゴリ１０ ,',',
C.ポイント種別１０ ,',',
NVL(LPAD(C.買上高ポイント種別１０,5,'0'),'') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(B.登録年月日 AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
CASE WHEN C.付与ポイント１０>=0 THEN 1 ELSE 2 END ,',',
ABS(C.付与ポイント１０) ,',',
NVL(B.更新前利用可能ポイント,0)  ,',',
TO_CHAR(NVL(B.更新前利用可能ポイント,0) + NVL(B.付与ポイント,0) - NVL(B.利用ポイント,0)) ,',',
NVL(C.ポイント対象金額１０,0) ,',',
NVL(C.ＪＡＮコード１０,'') ,',',
ABS(NVL(C.商品購入数１０,0)) ,',',
NVL(C.商品パーセントＰ付与率１０,0) ,',',
TO_CHAR(TO_DATE(TO_CHAR(B.システム年月日),'yyyyMMdd'),'yyyy/MM/dd') ,',',
NVL(TO_CHAR(B.最終更新日時,'yyyy/MM/dd HH24:MI:SS'),'') ,',',
NVL(LPAD(C.企画ＩＤ１０,10,'0'),'') ,',',
B.受付ＳＥＱ番号 ,',',
NVL(LPAD(B.旧企業コード,4,'0'),'') ,',',
DECODE(C.通常期間限定区分１０, '2', TO_CHAR(TO_DATE(TO_CHAR(ポイント有効期限１０),'yyyyMMdd'),'yyyy/MM/dd'),'') ,',',
NVL(C.購買区分１０,0)
) AS CSVTEXT
FROM
(
SELECT
F.カード種別,
NVL(PSTM.新店番号,E.店番号ＭＣＣ) AS 店番号ＭＣＣ,
E.送信日時,
E.リアル更新日時,
E.ディレイ更新日時,
E.登録年月日,
E.時刻,
E.購入日時,
DECODE(E.会社コードＭＣＣ,'2500',DECODE(E.店番号ＭＣＣ,'1983','0000',E.ターミナル番号),E.ターミナル番号) AS ターミナル番号,
DECODE(E.会社コードＭＣＣ,'2500',G.取引連番,E.取引番号) AS 取引番号,
E.電文ＳＥＱ番号,
E.買上額,
E.ポイント支払金額,
E.他社クレジット区分,
E.ＰＯＳ種別,
E.登録経路,
E.取引区分,
E.更新前利用可能ポイント,
E.付与ポイント,
E.利用ポイント,
E.システム年月日,
E.最終更新日時,
E.受付ＳＥＱ番号,
E.会社コードＭＣＣ,
E.会員番号,
E.理由コード,
E.処理通番,
E.顧客番号,
F.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
HSポイント日別情報:NENTUKI E
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON  E.会社コードＭＣＣ = PSMMCC.会社コード
AND E.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON  E.会社コードＭＣＣ = PSTM.旧会社コード
AND E.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT G
,PS会員番号体系 F
WHERE
    E.会員番号 >=  F.会員番号開始
AND E.会員番号 <=  F.会員番号終了
AND E.登録年月日 = G.登録日付
AND E.会社コードＭＣＣ = G.登録会社コードＭＣＣ
AND E.店番号ＭＣＣ =  G.登録店番号ＭＣＣ
AND E.ターミナル番号 = G.登録ターミナル番号
AND E.取引番号 = G.登録取引番号
AND E.会社コードＭＣＣ = 2500    ) B
,MSカード情報 A
,HSポイント日別内訳情報:NENTUKI C
LEFT JOIN MSポイント付与条件:ATBL_TEXT D
ON C.企画ＩＤ１０ = D.企画ＩＤ
AND C.企画バージョン１０ = D.バージョン
WHERE
    B.会員番号 = A.会員番号
AND B.サービス種別 = A.サービス種別
AND B.システム年月日 = C.システム年月日
AND B.顧客番号 = C.顧客番号
AND B.処理通番 = C.処理通番
AND B.システム年月日 = :SDATE
AND B.付与ポイント <> 0
AND C.付与利用区分１０ = 1
AND MOD(B.理由コード, 100) NOT IN (6,7,9,77,78)
);


\o
