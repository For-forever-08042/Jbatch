OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_003.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＰポイント付与
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     企業コード
    ,会社コード
    ,店舗コード
    ,レジ番号
    ,電文ＳＥＱ番号
    ,取引日時
    ,購入日時
    ,取引番号
    ,カード種別
    ,カード番号
    ,取引区分
    ,登録経路
    ,電文明細数
    ,取引高
    ,ＰＯＳ種別
    ,ポイント支払金額
    ,他社クレジット区分
    ,受付区分
    ,送信日時
    ,応答日時
    ,送信結果コード
    ,ランク判定用売上金額
    ,ランク判定用ポイント支払金額
    ,販促企画番号１
    ,お渡し済みフラグ１
    ,販促企画番号２
    ,お渡し済みフラグ２
    ,営業日
)

