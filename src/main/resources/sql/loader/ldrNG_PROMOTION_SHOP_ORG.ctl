OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'NG_PROMOTION_SHOP_ORG.CSV'
TRUNCATE
INTO TABLE WSポイント付与組織O
FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
(
     発注番号
    ,組織レベル
    ,組織コード
)
