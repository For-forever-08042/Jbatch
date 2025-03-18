OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_004.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＰポイント付与明細
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     企業コード
    ,会社コード
    ,店舗コード
    ,レジ番号
    ,電文ＳＥＱ番号
    ,明細番号
    ,企画ＩＤ
    ,企画バージョン
    ,ポイントカテゴリ
    ,ポイント種別
    ,ポイント付与区分
    ,ポイント付与数
    ,ＪＡＮコード
    ,商品購入数
    ,買上高ポイント種別
    ,ポイント対象金額
    ,商品パーセントポイント付与率
    ,Ｅｄｙ番号
    ,期間限定ポイント有効期限
    ,非購買フラグ
)

