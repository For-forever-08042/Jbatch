OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'NG_PROMOTION_VISIT_INFO_ORG.CSV'
TRUNCATE
INTO TABLE WS来店ポイントO
FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
(
     発注番号
    ,付与制御フラグ
)
