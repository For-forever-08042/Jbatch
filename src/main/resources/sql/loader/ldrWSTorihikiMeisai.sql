--step2
TRUNCATE TABLE tmp_WS取引明細ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引明細ワーク (営業日付  ,レシート明細行 ,商品登録区分 ,単価採用区分 ,値引割引集計区分 ,値引割引区分 ,単品コード ,本部特定単品ＮＯ ,店舗特定単品ＮＯ ,企画ＮＯ,売価 ,点数 ,値引割引金額 ,通常単価 ,原価 ,特売単価 ,特売原価 ,顧客単価 ,ＢＭ一点単価 ,単価変更額 ,オフシール単価 ,クラスコード ,部門コード ,グループコード ,分類コード ,ＢＭコード,書籍分類コード ,入出金科目コード ,緊急ＪＡＮフラグ ,割引除外フラグ ,単価変更フラグ ,オフシール採用フラグ ,商品売上フラグ ,マイナス部門フラグ ,顧客取引累積対象フラグ ,税率コード ,税区分 ,税率 ,よりどり登録番号 ,よりどり値引按分額 ,小計値引按分額 ,割引率 ,ＢＭ多段階区分 ,ＢＭ第一段階成立金額 ,ＢＭ第一段階成立個数 ,ＢＭ第二段階成立金額 ,ＢＭ第二段階成立個数 ,入出金金額 ,入出金点数 ,医薬品区分 ,特売開始日付  ,特売終了日付 ,商品名称 ,商品カナ名称 ,ＢＭ名称 ,ＢＭカナ名称 ,入出金科目名称 ,入出金科目カナ名称 ,税率名称 ,特売名称 ,値引割引名称 ,クラス名称 ,部門名称 ,グループ名称 ,分類名称 ,商品略称 ,重点コード ,クーポン単価 ,クーポン企画no   ,ＰＭコード,内税金額 ,商品コード２ ,ＰＭ割引区分  ,統一単価   ,強制単価    ,サービス券按分額   ,自動発注対象外フラグ ) FROM 'S4102.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS取引明細ワーク;
INSERT INTO WS取引明細ワーク
(
    営業日付
    ,レシート明細行
    ,商品登録区分
    ,単価採用区分
    ,値引割引集計区分
    ,値引割引区分
    ,単品コード
    ,本部特定単品ＮＯ
    ,店舗特定単品ＮＯ
    ,企画ＮＯ
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
    ,特売開始日付 
    ,特売終了日付
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
    ,クーポン企画no  
    ,内税金額
    ,商品コード２
    ,ＰＭ割引区分
    ,統一単価  
    ,強制単価
    ,サービス券按分額
    ,自動発注対象外フラグ
)
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付
    ,レシート明細行
    ,商品登録区分
    ,単価採用区分
    ,値引割引集計区分
    ,値引割引区分
    ,単品コード
    ,本部特定単品ＮＯ
    ,店舗特定単品ＮＯ
    ,企画ＮＯ
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
    ,特売開始日付 
    ,特売終了日付
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
    ,クーポン企画no  
    ,内税金額
    ,商品コード２
    ,CAST(ＰＭ割引区分 AS INTEGER )
    ,CAST(統一単価 AS INTEGER )
    ,CAST(強制単価 AS INTEGER )
    ,CAST(サービス券按分額 AS INTEGER )
    ,CAST(自動発注対象外フラグ: AS INTEGER )
FROM tmp_WS取引明細ワーク;
TRUNCATE TABLE tmp_WS取引明細ワーク;

