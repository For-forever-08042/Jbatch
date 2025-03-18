OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'EcPointCalcData_bmeo_sjis.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＥポイント計算
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     グーポン会員ＩＤ
    ,企業コード
    ,ＭＤ企業コード
    ,店舗コード                  CHAR "SUBSTR(:店舗コード, 11)"
    ,レジ番号
    ,取引番号
    ,取引日時
    ,配信ＳＥＱ番号
    ,元レジ番号
    ,元取引番号
    ,元取引日時
    ,取引高税込
    ,取引税額
    ,商品コード
    ,計上部門コード
    ,会社部門コード
    ,中分類コード
    ,小分類コード
    ,大カテゴリ
    ,中カテゴリ
    ,小カテゴリ
    ,ポイント非対象フラグ
    ,数量
    ,明細金額税抜税考慮有
    ,ポイント数
    ,カード種別
    ,カード会社番号
    ,支払カード種別
    ,支払カード番号
    ,支払金額
    ,他社クレジット区分
    ,取引識別子
    ,ＥＤＹＮＯ
    ,ＥＤＹ支払金額
    ,ポイント支払区分
    ,ポイント支払金額
    ,作成日時
    ,現在ランク月
    ,現在ランク年
    ,残高応答オフラインフラグ
    ,明細金額税抜税考慮無
    ,着荷日時
)
