OPTIONS(ERRORS=-1)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PRODUCT_KOUKAN.dat'
TRUNCATE
INTO TABLE PRODUCT_KOUKAN
FIELDS TERMINATED BY ','  OPTIONALLY ENCLOSED BY '"'
(
PRODUCT,
PRODUCT_NAME,
CHU_BUNRUI_CODE,
CHU_BUNRUI_NAME,
DAI_BUNRUI_CODE,
DAI_BUNRUI_NAME,
LASTMODIFY               SYSDATE
)
