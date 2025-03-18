OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_008.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＰ来店ポイント制御登録
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
    ,来店ポイント種別
    ,制御フラグ
    ,送信日時
    ,応答日時
    ,送信結果コード
)

