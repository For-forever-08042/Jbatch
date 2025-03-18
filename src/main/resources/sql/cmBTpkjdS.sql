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

\o ./KKMI0120.csv


SELECT
CONCAT(LPAD(NVL(RTRIM(A.変更ＩＤ),'0'),10,'0') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(DECODE(A.登録年月日,'0',A.作業年月日,A.登録年月日) AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
NVL(B.ＧＯＯＰＯＮ番号,0) ,',',
NVL(LPAD(A.カード種別,3,'0'),'') ,',',
NVL(A.還元種別,0) ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(A.システム年月日),'yyyyMMdd'),'yyyy/MM/dd'), '0000/00/00') ,',',
TO_CHAR(COALESCE(A.最終更新日時,TO_DATE(:SDATE,'yyyyMMdd')),'yyyy/MM/dd HH24:MI:SS') ,',',
NVL(LPAD(A.店番号ＭＣＣ,4,'0'),'') ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.送信日時,'0',NULL,A.送信日時)),'yyyyMMdd HH24MISS'),'yyyy/MM/dd HH24:MI:SS'), '') ,',',
TO_CHAR(NVL(A.リアル更新日時,A.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.登録年月日, '0', A.作業年月日, A.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,
DECODE(A.時刻,'0',' 00:00:00',TO_CHAR(TO_TIMESTAMP(LPAD(A.時刻,6,'0'),'HH24MISS'),' HH24:MI:SS')) ,',',
LPAD(A.ターミナル番号,5,'0') ,',',
LPAD(A.取引番号,10,'0') ,',',
NVL(CAST(A.電文ＳＥＱ番号 AS TEXT),'') ,',',
NVL(A.登録経路,'') ,',',
A.利用ポイント ,',',
NVL(LPAD(A.旧企業コード,4,'0'),'')) 
FROM
(
SELECT
D.変更ＩＤ,
D.登録年月日,
D.作業年月日,
D.カード種別,
D.還元種別,
D.システム年月日,
D.最終更新日時,
D.店番号ＭＣＣ,
D.送信日時,
D.リアル更新日時,
D.ディレイ更新日時,
D.時刻,
DECODE(D.会社コードＭＣＣ,'2500',DECODE(D.店番号ＭＣＣ,'1983','0000',D.ターミナル番号),D.ターミナル番号) AS ターミナル番号,
DECODE(D.会社コードＭＣＣ,'2500',D.ＭＫ取引番号,D.取引番号) AS 取引番号,
D.電文ＳＥＱ番号,
D.登録経路,
D.利用ポイント,
D.会社コードＭＣＣ,
D.会員番号,
D.理由コード,
C.サービス種別,
PSMMCC.旧企業コード
FROM
 HSポイント日別情報:NENTUKI D
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC 
ON D.会社コードＭＣＣ = PSMMCC.会社コード 
AND D.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
,PS会員番号体系 C
WHERE
    D.会員番号 >=  C.会員番号開始
AND D.会員番号 <=  C.会員番号終了
AND D.会社コードＭＣＣ <> 2500  ) A
,MSカード情報 B
WHERE
    A.サービス種別 = B.サービス種別
AND A.会員番号 = B.会員番号
AND A.システム年月日     =   :SDATE
AND A.利用ポイント <> 0
AND MOD(A.理由コード, 100) not in (6,7,77,78,90,91,92,93,94)
UNION
SELECT                                                                                                                     --HSポイント明細取引情報(当月)
CONCAT(LPAD(NVL(RTRIM(A.変更ＩＤ),'0'),10,'0') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(DECODE(A.登録年月日,'0',A.作業年月日,A.登録年月日) AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
NVL(B.ＧＯＯＰＯＮ番号,0) ,',',
NVL(LPAD(A.カード種別,3,'0'),'') ,',',
NVL(A.還元種別,0) ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(A.システム年月日),'yyyyMMdd'),'yyyy/MM/dd'), '0000/00/00') ,',',
TO_CHAR(COALESCE(A.最終更新日時,TO_DATE(:SDATE,'yyyyMMdd')),'yyyy/MM/dd HH24:MI:SS') ,',',
NVL(LPAD(A.店番号ＭＣＣ,4,'0'),'') ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.送信日時,'0',NULL,A.送信日時)),'yyyyMMdd HH24MISS'),'yyyy/MM/dd HH24:MI:SS'), '') ,',',
TO_CHAR(NVL(A.リアル更新日時,A.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.登録年月日, '0', A.作業年月日, A.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,
DECODE(A.時刻,'0',' 00:00:00',TO_CHAR(TO_TIMESTAMP(LPAD(A.時刻,6,'0'),'HH24MISS'),' HH24:MI:SS')) ,',',
LPAD(A.ターミナル番号,5,'0') ,',',
LPAD(A.取引番号,10,'0') ,',',
NVL(CAST(A.電文ＳＥＱ番号 AS TEXT),'') ,',',
NVL(A.登録経路,'') ,',',
A.利用ポイント ,',',
NVL(LPAD(A.旧企業コード,4,'0'),'')) 
FROM
(
SELECT
D.変更ＩＤ,
D.登録年月日,
D.作業年月日,
D.カード種別,
D.還元種別,
D.システム年月日,
D.最終更新日時,
NVL(PSTM.新店番号,D.店番号ＭＣＣ) AS 店番号ＭＣＣ,
D.送信日時,
D.リアル更新日時,
D.ディレイ更新日時,
D.時刻,
DECODE(D.会社コードＭＣＣ,'2500',DECODE(D.店番号ＭＣＣ,'1983','0000',D.ターミナル番号),D.ターミナル番号) AS ターミナル番号,
DECODE(D.会社コードＭＣＣ,'2500',E.取引連番,D.取引番号) AS 取引番号,
D.電文ＳＥＱ番号,
D.登録経路,
D.利用ポイント,
D.会社コードＭＣＣ,
D.会員番号,
D.理由コード,
C.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
 HSポイント日別情報:NENTUKI D
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON D.会社コードＭＣＣ = PSMMCC.会社コード
AND D.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON D.会社コードＭＣＣ = PSTM.旧会社コード
AND D.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI E
,PS会員番号体系 C
WHERE
    D.会員番号 >=  C.会員番号開始
AND D.会員番号 <=  C.会員番号終了
AND D.登録年月日 = E.登録日付
AND D.会社コードＭＣＣ = E.登録会社コードＭＣＣ
AND D.店番号ＭＣＣ =  E.登録店番号ＭＣＣ
AND D.ターミナル番号 = E.登録ターミナル番号
AND D.取引番号 = E.登録取引番号
AND D.会社コードＭＣＣ = 2500 ) A
,MSカード情報 B
WHERE
    A.サービス種別 = B.サービス種別
AND A.会員番号 = B.会員番号
AND A.システム年月日     =   :SDATE
AND A.利用ポイント <> 0
AND MOD(A.理由コード, 100) not in (6,7,77,78,90,91,92,93,94)
UNION                                                                                                                      --HSポイント明細取引情報(前月)
SELECT
CONCAT(LPAD(NVL(RTRIM(A.変更ＩＤ),'0'),10,'0') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(DECODE(A.登録年月日,'0',A.作業年月日,A.登録年月日) AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
NVL(B.ＧＯＯＰＯＮ番号,0) ,',',
NVL(LPAD(A.カード種別,3,'0'),'') ,',',
NVL(A.還元種別,0) ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(A.システム年月日),'yyyyMMdd'),'yyyy/MM/dd'), '0000/00/00') ,',',
TO_CHAR(COALESCE(A.最終更新日時,TO_DATE(:SDATE,'yyyyMMdd')),'yyyy/MM/dd HH24:MI:SS') ,',',
NVL(LPAD(A.店番号ＭＣＣ,4,'0'),'') ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.送信日時,'0',NULL,A.送信日時)),'yyyyMMdd HH24MISS'),'yyyy/MM/dd HH24:MI:SS'), '') ,',',
TO_CHAR(NVL(A.リアル更新日時,A.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.登録年月日, '0', A.作業年月日, A.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,
DECODE(A.時刻,'0',' 00:00:00',TO_CHAR(TO_TIMESTAMP(LPAD(A.時刻,6,'0'),'HH24MISS'),' HH24:MI:SS')) ,',',
LPAD(A.ターミナル番号,5,'0') ,',',
LPAD(A.取引番号,10,'0') ,',',
NVL(CAST(A.電文ＳＥＱ番号 AS TEXT),'') ,',',
NVL(A.登録経路,'') ,',',
A.利用ポイント ,',',
NVL(LPAD(A.旧企業コード,4,'0'),'')) 
FROM
(
SELECT
D.変更ＩＤ,
D.登録年月日,
D.作業年月日,
D.カード種別,
D.還元種別,
D.システム年月日,
D.最終更新日時,
NVL(PSTM.新店番号,D.店番号ＭＣＣ) AS 店番号ＭＣＣ,
D.送信日時,
D.リアル更新日時,
D.ディレイ更新日時,
D.時刻,
DECODE(D.会社コードＭＣＣ,'2500',DECODE(D.店番号ＭＣＣ,'1983','0000',D.ターミナル番号),D.ターミナル番号) AS ターミナル番号,
DECODE(D.会社コードＭＣＣ,'2500',E.取引連番,D.取引番号) AS 取引番号,
D.電文ＳＥＱ番号,
D.登録経路,
D.利用ポイント,
D.会社コードＭＣＣ,
D.会員番号,
D.理由コード,
C.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
 HSポイント日別情報:NENTUKI D
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON D.会社コードＭＣＣ = PSMMCC.会社コード
AND D.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON D.会社コードＭＣＣ = PSTM.旧会社コード
AND D.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_LAST E
,PS会員番号体系 C
WHERE
    D.会員番号 >=  C.会員番号開始
AND D.会員番号 <=  C.会員番号終了
AND D.登録年月日 = E.登録日付
AND D.会社コードＭＣＣ = E.登録会社コードＭＣＣ
AND D.店番号ＭＣＣ =  E.登録店番号ＭＣＣ
AND D.ターミナル番号 = E.登録ターミナル番号
AND D.取引番号 = E.登録取引番号
AND D.会社コードＭＣＣ = 2500 ) A
,MSカード情報 B
WHERE
    A.サービス種別 = B.サービス種別
AND A.会員番号 = B.会員番号
AND A.システム年月日     =   :SDATE
AND A.利用ポイント <> 0
AND MOD(A.理由コード, 100) not in (6,7,77,78,90,91,92,93,94)
UNION                                                                                                                      --HSポイント明細取引情報(来月)
SELECT
CONCAT(LPAD(NVL(RTRIM(A.変更ＩＤ),'0'),10,'0') ,',',
TO_CHAR(ADD_MONTHS(TO_DATE(CAST(DECODE(A.登録年月日,'0',A.作業年月日,A.登録年月日) AS TEXT),'yyyyMMdd'),-3),'yyyy') ,',',
NVL(B.ＧＯＯＰＯＮ番号,0) ,',',
NVL(LPAD(A.カード種別,3,'0'),'') ,',',
NVL(A.還元種別,0) ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(A.システム年月日),'yyyyMMdd'),'yyyy/MM/dd'), '0000/00/00') ,',',
TO_CHAR(COALESCE(A.最終更新日時,TO_DATE(:SDATE,'yyyyMMdd')),'yyyy/MM/dd HH24:MI:SS') ,',',
NVL(LPAD(A.店番号ＭＣＣ,4,'0'),'') ,',',
NVL(TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.送信日時,'0',NULL,A.送信日時)),'yyyyMMdd HH24MISS'),'yyyy/MM/dd HH24:MI:SS'), '') ,',',
TO_CHAR(NVL(A.リアル更新日時,A.ディレイ更新日時),'yyyy/MM/dd HH24:MI:SS') ,',',
TO_CHAR(TO_DATE(TO_CHAR(DECODE(A.登録年月日, '0', A.作業年月日, A.登録年月日)),'yyyyMMdd'),'yyyy/MM/dd') ,
DECODE(A.時刻,'0',' 00:00:00',TO_CHAR(TO_TIMESTAMP(LPAD(A.時刻,6,'0'),'HH24MISS'),' HH24:MI:SS')) ,',',
LPAD(A.ターミナル番号,5,'0') ,',',
LPAD(A.取引番号,10,'0') ,',',
NVL(CAST(A.電文ＳＥＱ番号 AS TEXT),'') ,',',
NVL(A.登録経路,'') ,',',
A.利用ポイント ,',',
NVL(LPAD(A.旧企業コード,4,'0'),'')) 
FROM
(
SELECT
D.変更ＩＤ,
D.登録年月日,
D.作業年月日,
D.カード種別,
D.還元種別,
D.システム年月日,
D.最終更新日時,
NVL(PSTM.新店番号,D.店番号ＭＣＣ) AS 店番号ＭＣＣ,
D.送信日時,
D.リアル更新日時,
D.ディレイ更新日時,
D.時刻,
DECODE(D.会社コードＭＣＣ,'2500',DECODE(D.店番号ＭＣＣ,'1983','0000',D.ターミナル番号),D.ターミナル番号) AS ターミナル番号,
DECODE(D.会社コードＭＣＣ,'2500',E.取引連番,D.取引番号) AS 取引番号,
D.電文ＳＥＱ番号,
D.登録経路,
D.利用ポイント,
D.会社コードＭＣＣ,
D.会員番号,
D.理由コード,
C.サービス種別,
NVL(PSTM.新会社コード,PSMMCC.旧企業コード) AS 旧企業コード
FROM
 HSポイント日別情報:NENTUKI D
LEFT JOIN PS店表示情報ＭＣＣ PSMMCC
ON D.会社コードＭＣＣ = PSMMCC.会社コード
AND D.店番号ＭＣＣ = PSMMCC.店番号
AND :SDATE >= PSMMCC.開始年月日
AND :SDATE <= PSMMCC.終了年月日
LEFT JOIN PS店舗変換マスタ PSTM
ON D.会社コードＭＣＣ = PSTM.旧会社コード
AND D.店番号ＭＣＣ = PSTM.旧店番号
,HSポイント明細取引情報:NENTUKI_NEXT E
,PS会員番号体系 C
WHERE
    D.会員番号 >=  C.会員番号開始
AND D.会員番号 <=  C.会員番号終了
AND D.登録年月日 = E.登録日付
AND D.会社コードＭＣＣ = E.登録会社コードＭＣＣ
AND D.店番号ＭＣＣ =  E.登録店番号ＭＣＣ
AND D.ターミナル番号 = E.登録ターミナル番号
AND D.取引番号 = E.登録取引番号
AND D.会社コードＭＣＣ = 2500 ) A
,MSカード情報 B
WHERE
    A.サービス種別 = B.サービス種別
AND A.会員番号 = B.会員番号
AND A.システム年月日     =   :SDATE
AND A.利用ポイント <> 0
AND MOD(A.理由コード, 100) not in (6,7,77,78,90,91,92,93,94)
;

\o
