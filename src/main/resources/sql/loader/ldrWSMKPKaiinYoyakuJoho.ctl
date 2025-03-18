OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_007.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＰ会員予約情報登録
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     企業コード
    ,会社コード
    ,店舗コード
    ,レジ番号
    ,電文ＳＥＱ番号
    ,取引日時
    ,取引番号
    ,カード種別
    ,カード番号
    ,整理番号
    ,登録経路
    ,送信日時
    ,応答日時
    ,送信結果コード
)

