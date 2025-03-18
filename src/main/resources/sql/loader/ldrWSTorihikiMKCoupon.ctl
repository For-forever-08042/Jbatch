OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'S4123.dat'
APPEND
PRESERVE BLANKS
INTO TABLE WS取引MKクーポンワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     営業日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,会社コード
    ,店舗コード
    ,ノードＮｏ
    ,取引連番
    ,利用フラグ
    ,クーポン区分
    ,クーポンコード
    ,クーポングループ連番
    ,特典区分
    ,割引率
    ,値引額
    ,付与ポイント
    ,期間限定ポイント
    ,期間限定ポイント失効期限 CHAR "TO_DATE(:期間限定ポイント失効期限, 'YYYY/MM/DD HH24:MI:SS')"
    ,枚数
    ,クーポンシリアル番号
    ,クーポングループＩＤ
    ,利用クーポン発行年月日
)

