OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'cmBTposmS_CF.dat'
APPEND
INTO TABLE WS売上明細
FIELDS TERMINATED BY ','
TRAILING NULLCOLS
(
       売上日
     ,会社コード
     ,店舗コード
     ,レジＮＯ
     ,レシートＮＯ
     ,行番号                SEQUENCE(MAX)
     ,売上時刻
     ,会員ＩＤ              "TRIM(:会員ＩＤ)"
     ,部門コード
     ,ＪＡＮコード
     ,商品名                "TRIM('\"' FROM :商品名)"
     ,売上数量
     ,売上金額税込
     ,粗利金額
     ,値引割引金額
     ,アイテム値引金額
     ,アイテム割引金額
     ,自動値引金額
     ,自動割引金額
     ,会員値引金額
     ,会員割引金額
     ,クーポン値引金額
     ,クーポン割引金額
     ,ＭＭ値引金額
     ,特売企画コード
     ,クーポン値引企画コード
     ,クーポン割引企画コード
     ,特売チラシ掲載フラグ
     ,会社部門コード
     ,計上部門コード
     ,分類コード
     ,売上金額税抜
     ,ＥＣデータ種別
     ,免税フラグ
     ,国コード
     ,性別
     ,生年月日
     ,旅券コード
     ,来店宅配フラグ
     ,データ区分 CONSTANT '2'
)
