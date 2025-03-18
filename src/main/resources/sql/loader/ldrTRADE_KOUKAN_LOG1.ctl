OPTIONS(ERRORS=-1)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'TRADE_KOUKAN_LOG.dat'
APPEND
INTO TABLE WS商品交換情報
FIELDS TERMINATED BY ','  OPTIONALLY ENCLOSED BY '"'
(
注文ＮＯ,
会員番号,
注文日付                   CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:注文日付, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS'))",
ターミナル番号             CHAR "TO_NUMBER(NVL(TRIM(:ターミナル番号), '0'))",
取引番号                   CHAR "TO_NUMBER(NVL(TRIM(:取引番号), '0'))",
決済手段,
購入金額                   CHAR "TO_NUMBER(NVL(TRIM(:購入金額), '0'))",
ポイント利用額             CHAR "TO_NUMBER(NVL(TRIM(:ポイント利用額), '0'))",
ステージ対象金額           CHAR "TO_NUMBER(NVL(TRIM(:ステージ対象金額), '0'))",
最終更新日                 SYSDATE
)
