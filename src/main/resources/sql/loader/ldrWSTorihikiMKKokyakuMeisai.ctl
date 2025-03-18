OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'S4118.dat'
APPEND
PRESERVE BLANKS
INTO TABLE WS取引MK顧客明細ワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     営業日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,会社コード
    ,店舗コード
    ,ノードＮｏ
    ,取引連番
    ,レシート明細行
    ,ポイントレートコード
    ,ポイント対象フラグ
    ,単品ポイント
    ,期間限定倍率ポイント
    ,期間限定付与ポイント
    ,期間限定倍率ポイント失効期限          CHAR "TO_DATE(:期間限定倍率ポイント失効期限, 'YYYY/MM/DD HH24:MI:SS')"
    ,期間限定付与ポイント失効期限          CHAR "TO_DATE(:期間限定付与ポイント失効期限, 'YYYY/MM/DD HH24:MI:SS')"
    ,分類倍率通常Ｐ
    ,分類通常Ｐクーポンコード
    ,分類倍率期間限定Ｐ
    ,分類期間限定Ｐクーポンコード
    ,分類期間限定Ｐ失効期限                CHAR "TO_DATE(:分類期間限定Ｐ失効期限, 'YYYY/MM/DD HH24:MI:SS')"
    ,クーポンポイント
    ,単品ポイント倍率
    ,期間限定ポイント倍率
    ,採用ポイント倍率
    ,クーポンコード
    ,クーポン割引率
    ,クーポン値引額
    ,クーポン枚数
    ,クーポン区分
    ,利用単品クーポンシリアル番号
    ,利用クーポン発行年月日
)

