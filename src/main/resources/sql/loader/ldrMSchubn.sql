--step2
TRUNCATE TABLE tmp_MS中分類情報;
\set ON_ERROR_STOP true
\COPY tmp_MS中分類情報 ( 中分類コード,開始年月日,終了年月日,中分類名称,中分類名称カナ,計上部門コード ) FROM 'CATEGORY.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE MS中分類情報;
INSERT INTO MS中分類情報 
SELECT 
0,
中分類コード,
開始年月日,
終了年月日,
中分類名称,
中分類名称カナ,
計上部門コード,
:BATDATE,
:BATDATE,
SYSDATE(),
'cmBTmsc2S'
FROM tmp_MS中分類情報;
TRUNCATE TABLE tmp_MS中分類情報;
