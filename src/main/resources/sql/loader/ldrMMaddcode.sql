--step2
TRUNCATE TABLE tmp_MMZR[hîñ;
\set ON_ERROR_STOP true
\COPY  tmp_MMZR[hîñ (    XÖÔ,ZP,ZQ, f[^oú ) FROM 'ADDRESS_MASTER.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE MMZR[hîñ;
WITH max_id_cte AS (
    SELECT COALESCE(MAX(AÔ), 0) AS max_id FROM MMZR[hîñ
)

INSERT INTO MMZR[hîñ (
       AÔ
     ,XÖÔ
     ,ZP
     ,ZQ
     ,f[^oú
) 
SELECT 
     max_id_cte.max_id + ROW_NUMBER() OVER () AS AÔ
     ,XÖÔ
     ,ZP
     ,ZQ
     ,TO_NUMBER(TO_CHAR(f[^oú, 'YYYYMMDD') ) AS f[^oú
FROM tmp_MMZR[hîñ, max_id_cte;
TRUNCATE TABLE tmp_MMZR[hîñ;
