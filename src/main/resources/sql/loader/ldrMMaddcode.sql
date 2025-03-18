--step2
TRUNCATE TABLE tmp_MM住所コード情報;
\set ON_ERROR_STOP true
\COPY  tmp_MM住所コード情報 (    郵便番号,住所１,住所２, データ抽出日 ) FROM 'ADDRESS_MASTER.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE MM住所コード情報;
WITH max_id_cte AS (
    SELECT COALESCE(MAX(連番), 0) AS max_id FROM MM住所コード情報
)

INSERT INTO MM住所コード情報 (
       連番
     ,郵便番号
     ,住所１
     ,住所２
     ,データ抽出日
) 
SELECT 
     max_id_cte.max_id + ROW_NUMBER() OVER () AS 連番
     ,郵便番号
     ,住所１
     ,住所２
     ,TO_NUMBER(TO_CHAR(データ抽出日, 'YYYYMMDD') ) AS データ抽出日
FROM tmp_MM住所コード情報, max_id_cte;
TRUNCATE TABLE tmp_MM住所コード情報;
