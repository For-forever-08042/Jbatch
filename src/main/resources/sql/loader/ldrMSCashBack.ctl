OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'CASHBACK.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE MSキャッシュバック情報
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
(
     サービス種別                  CONSTANT 1
    ,会員番号
    ,キャッシュバック日付
    ,キャッシュバック金額
    ,最終更新日                    CONSTANT '@BATDATE@'
    ,最終更新日時                  SYSDATE
    ,最終更新プログラムＩＤ        CONSTANT 'cmBTcabaS'
)