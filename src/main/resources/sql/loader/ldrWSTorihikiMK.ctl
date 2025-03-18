OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'S4107.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WS取引MKワーク
FIELDS TERMINATED BY "," OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
     営業日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,会社コード
    ,店舗コード
    ,ノードＮｏ
    ,取引連番
    ,トラン区分
    ,取引日時         CHAR "TO_DATE(:取引日時, 'YYYY/MM/DD HH24:MI:SS')"
    ,レシートＮｏ
    ,レジＮｏ
    ,発行店コード
    ,顧客コード       CHAR "NVL(trim(:顧客コード), '0')"
    ,キャッシャーコード
    ,チェッカーコード
    ,客層コード
    ,訂正取引フラグ
    ,訂正対象レジＮｏ
    ,訂正対象レシートＮｏ
    ,打消キャッシャーコード
    ,売掛取引フラグ
    ,売掛入金レジＮｏ
    ,売掛入金レシートＮｏ
    ,売掛取引日付         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,領収証Ｎｏ
    ,売上金額
    ,売上点数
    ,今回ポイント
    ,サービス発行券ポイント
    ,誕生日サービス実施日         CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:営業日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD'))"
    ,ポイント対象売上金額
    ,ポイント対象外売上金額
    ,外税金額
    ,内税金額
    ,売上外金額
    ,売上外点数
    ,外税対象売上金額
    ,外税対象売上点数
    ,内税対象売上金額
    ,内税対象売上点数
    ,非課税対象売上金額
    ,非課税対象売上点数
    ,不課税対象売上金額
    ,不課税対象売上点数
    ,小計金額
    ,小計値引割引集計区分
    ,小計値引割引区分
    ,小計値引割引金額
    ,小計割引率
    ,端数値引額
    ,入出金金額
    ,入出金点数
    ,売掛金額
    ,ポイント調整区分
    ,サービス券発行枚数
    ,直前訂正回数
    ,直前訂正金額
    ,指定訂正回数
    ,指定訂正金額
    ,領収証発行枚数
    ,両替回数
    ,万券枚数
    ,認証印字枚数
    ,顧客名称
    ,キャッシャー名称
    ,チェッカー名称
    ,客層名称
    ,打消キャッシャー名称
    ,社員販売コード
)


