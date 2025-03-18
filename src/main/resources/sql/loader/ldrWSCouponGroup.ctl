OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'cmBTcpupS_B0005.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSクーポングループマスタワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     企業コード
    ,クーポンＩＤ
    ,クーポングループＩＤ
    ,最終更新日時             CHAR "TO_DATE(:最終更新日時, 'YYYY/MM/DD HH24:MI:SS')"
    ,最終更新ユーザＩＤ
    ,削除フラグ
    ,発行登録済みフラグ       CONSTANT '0'
    ,メッセージクーポンフラグ CONSTANT '0'
    ,ＣＲＭクーポンフラグ     CONSTANT '0'
)
