OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'NG_PROMOTION_CATEGORY_ORG.CSV'
TRUNCATE
INTO TABLE WSポイント付与部門O
FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
(
     発注番号
    ,部門レベル
    ,部門コード
)
