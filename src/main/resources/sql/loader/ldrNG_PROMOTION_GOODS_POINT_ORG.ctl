OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'NG_PROMOTION_GOODS_POINT_ORG.CSV'
TRUNCATE
INTO TABLE WSポイント付与商品O
FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
(
     発注番号
    ,ＪＡＮコード
    ,商品名称
    ,倍率固定ポイント値
)
