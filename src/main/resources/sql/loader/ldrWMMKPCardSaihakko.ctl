OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_006.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WMＭＫＰカード再発行
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
    ,旧カード種別
    ,旧カード番号
    ,新カード種別
    ,新カード番号
    ,処理区分
    ,送信日時
    ,応答日時
    ,送信結果コード
    ,会員ステータス
    ,姓カナ
)

