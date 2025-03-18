--step2
TRUNCATE TABLE tmp_MM�Z���R�[�h���;
\set ON_ERROR_STOP true
\COPY  tmp_MM�Z���R�[�h��� (    �X�֔ԍ�,�Z���P,�Z���Q, �f�[�^���o�� ) FROM 'ADDRESS_MASTER.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE MM�Z���R�[�h���;
WITH max_id_cte AS (
    SELECT COALESCE(MAX(�A��), 0) AS max_id FROM MM�Z���R�[�h���
)

INSERT INTO MM�Z���R�[�h��� (
       �A��
     ,�X�֔ԍ�
     ,�Z���P
     ,�Z���Q
     ,�f�[�^���o��
) 
SELECT 
     max_id_cte.max_id + ROW_NUMBER() OVER () AS �A��
     ,�X�֔ԍ�
     ,�Z���P
     ,�Z���Q
     ,TO_NUMBER(TO_CHAR(�f�[�^���o��, 'YYYYMMDD') ) AS �f�[�^���o��
FROM tmp_MM�Z���R�[�h���, max_id_cte;
TRUNCATE TABLE tmp_MM�Z���R�[�h���;
