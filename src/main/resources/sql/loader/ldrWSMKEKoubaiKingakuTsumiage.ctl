OPTIONS (ERRORS = 10000)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'dekk_buy_price_log_bmee_sjis.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＥ購買金額積上
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     ＭＤ企業コード
    ,店舗コード
    ,電文ＳＥＱ番号
    ,取引日時
    ,購入日時
    ,取引番号
    ,グーポン会員ＩＤ
    ,取引区分
    ,送信日時
    ,応答日時 "NVL(:応答日時, -1)"
    ,送信結果コード "NVL(:送信結果コード, -1)"
    ,ランク判定用金額
    ,ランク判定用ポイント支払金額
)
