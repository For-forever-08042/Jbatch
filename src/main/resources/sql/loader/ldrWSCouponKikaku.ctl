OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'cmBTcpupS_B0001.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSクーポン企画マスタワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     企業コード
    ,クーポン区分
    ,クーポンＩＤ
    ,キャンペーンＩＤ
    ,バーコード番号
    ,クーポン名称
    ,発行期間開始日           CHAR "TO_DATE(:発行期間開始日, 'YYYY/MM/DD HH24:MI:SS')"
    ,発行期間終了日           CHAR "TO_DATE(:発行期間終了日, 'YYYY/MM/DD HH24:MI:SS')"
    ,発行開始日               CHAR "TO_DATE(:発行開始日, 'YYYY/MM/DD HH24:MI:SS')"
    ,利用期間開始日           CHAR "TO_DATE(:利用期間開始日, 'YYYY/MM/DD HH24:MI:SS')"
    ,利用期間終了日           CHAR "TO_DATE(:利用期間終了日, 'YYYY/MM/DD HH24:MI:SS')"
    ,利用期間ヶ月
    ,利用期間日
    ,利用者制限
    ,利用可能回数顧客
    ,利用可能回数券
    ,利用可能最低金額
    ,利用店舗
    ,特典                     "DECODE (:特典, 'A', 10, 'B', 11, :特典)"
    ,対象単品
    ,特典内容
    ,最終更新日時             CHAR "TO_DATE(:最終更新日時, 'YYYY/MM/DD HH24:MI:SS')"
    ,最終更新者ＩＤ
    ,削除フラグ
    ,キャンペーン種別
    ,発行対象顧客
    ,予備項目１
    ,予備項目２
    ,予備項目３
    ,予備項目４
    ,予備項目５
)
