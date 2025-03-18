OPTIONS(ERRORS=-1)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'TRADE_KOUKAN_MEISAI_LOG.dat'
APPEND
INTO TABLE WS商品交換明細情報
FIELDS TERMINATED BY ','  OPTIONALLY ENCLOSED BY '"'
(
注文ＮＯ,
明細連番,
商品コード,
数量                      CHAR "TO_NUMBER(NVL(TRIM(:数量), '0'))",
決済手段,
購入金額按分額            CHAR "TO_NUMBER(NVL(TRIM(:購入金額按分額), '0'))",
ポイント利用額按分額      CHAR "TO_NUMBER(NVL(TRIM(:ポイント利用額按分額), '0'))",
単価                      CHAR "TO_NUMBER(NVL(TRIM(:単価), '0'))",
最終更新日                SYSDATE
)
