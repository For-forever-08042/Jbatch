--step2
TRUNCATE TABLE tmp_MS�����ޏ��;
\set ON_ERROR_STOP true
\COPY tmp_MS�����ޏ�� ( �����ރR�[�h,�J�n�N����,�I���N����,�����ޖ���,�����ޖ��̃J�i,�v�㕔��R�[�h ) FROM 'CATEGORY.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE MS�����ޏ��;
INSERT INTO MS�����ޏ�� 
SELECT 
0,
�����ރR�[�h,
�J�n�N����,
�I���N����,
�����ޖ���,
�����ޖ��̃J�i,
�v�㕔��R�[�h,
:BATDATE,
:BATDATE,
SYSDATE(),
'cmBTmsc2S'
FROM tmp_MS�����ޏ��;
TRUNCATE TABLE tmp_MS�����ޏ��;
