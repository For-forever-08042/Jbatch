OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'S4108.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WS取引MK明細ワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     営業日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,会社コード
    ,店舗コード
    ,ノードＮｏ
    ,取引連番
    ,レシート明細行
    ,商品登録区分
    ,単価採用区分
    ,値引割引集計区分
    ,値引割引区分
    ,単品コード
    ,本部特定単品Ｎｏ
    ,店舗特定単品Ｎｏ
    ,企画Ｎｏ
    ,売価
    ,点数
    ,値引割引金額
    ,通常単価
    ,原価
    ,特売単価
    ,特売原価
    ,顧客単価
    ,ＢＭ一点単価
    ,単価変更額
    ,オフシール単価
    ,クラスコード
    ,部門コード
    ,グループコード
    ,分類コード
    ,DUMMY1 FILLER
    ,書籍分類コード
    ,入出金科目コード
    ,緊急ＪＡＮフラグ
    ,割引除外フラグ
    ,単価変更フラグ
    ,オフシール採用フラグ
    ,商品売上フラグ
    ,マイナス部門フラグ
    ,顧客取引累積対象フラグ
    ,税率コード
    ,税区分
    ,税率
    ,よりどり登録番号
    ,よりどり値引按分額
    ,小計値引按分額
    ,割引率
    ,ＢＭ多段階区分
    ,ＢＭ第一段階成立金額
    ,ＢＭ第一段階成立個数
    ,ＢＭ第二段階成立金額
    ,ＢＭ第二段階成立個数
    ,入出金金額
    ,入出金点数
    ,医薬品区分
    ,特売開始日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,特売終了日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,商品名称
    ,商品カナ名称
    ,ＢＭ名称
    ,ＢＭカナ名称
    ,入出金科目名称
    ,入出金科目カナ名称
    ,税率名称
    ,特売名称
    ,値引割引名称
    ,クラス名称
    ,部門名称
    ,グループ名称
    ,分類名称
    ,商品略称
    ,重点コード
    ,クーポン単価
    ,クーポン企画ＮＯ
    ,DUMMY2 FILLER
    ,内税金額
    ,商品コード２
    ,ＰＭ割引区分         INTEGER EXTERNAL
    ,統一単価             INTEGER EXTERNAL
    ,強制単価             INTEGER EXTERNAL
    ,サービス券按分額     INTEGER EXTERNAL
    ,自動発注対象外フラグ INTEGER EXTERNAL
)
