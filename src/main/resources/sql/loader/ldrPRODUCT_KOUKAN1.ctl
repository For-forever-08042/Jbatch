OPTIONS(ERRORS=-1)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PRODUCT_KOUKAN.dat'
TRUNCATE
INTO TABLE WS交換商品マスタ
FIELDS TERMINATED BY ','  OPTIONALLY ENCLOSED BY '"'
(
商品コード,
商品名称,
中分類コード,
中分類名称,
大分類コード,
大分類名称,
最終更新日               SYSDATE
)
