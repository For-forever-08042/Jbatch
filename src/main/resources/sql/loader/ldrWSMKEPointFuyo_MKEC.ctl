OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'ecgo_grant_point_bmeo_sjis.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＥポイント付与
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     ＭＤ企業コード
    ,店舗コード
    ,取引番号
    ,取引日時
    ,グーポン会員ＩＤ
    ,取引区分
    ,取引高税込
    ,ポイント支払金額
    ,他社クレジット区分
    ,企画ＩＤ
    ,企画バージョン
    ,ポイントカテゴリ
    ,ポイント種別
    ,付与区分
    ,付与ポイント数
    ,ＪＡＮコード
    ,商品購入数
    ,買上高ポイント種別
    ,対象金額税抜
    ,商品パーセントポイント付与率
    ,ＥＤＹ番号
    ,期間限定ポイント有効期限
    ,非購買フラグ
    ,期間限定ポイント付与開始日
    ,クーポンコード
    ,ポイント付与基準日時
)
