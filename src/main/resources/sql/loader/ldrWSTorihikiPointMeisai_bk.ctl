OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'S4142.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WS取引ポイント明細ワーク
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     営業日付              CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,会社コード
    ,店舗コード
    ,ノードＮｏ
    ,取引連番
    ,ポイント明細行
    ,企画ＩＤ
    ,ポイントカテゴリ
    ,ポイント種別
    ,付与区分
    ,付与ポイント数
    ,ＪＡＮコード
    ,商品購入数
    ,買上高ポイント種別
    ,対象金額
    ,商品パーセントポイント付与率
    ,期間限定ポイントの有効期限 CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:期間限定ポイントの有効期限, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,非購買フラグ
)
