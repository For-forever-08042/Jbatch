--step2
TRUNCATE TABLE tmp_WS�l�j�d�o�N�[�|�����p;
\set ON_ERROR_STOP true
\COPY tmp_WS�l�j�d�o�N�[�|�����p (   �O�[�|������h�c ,��ƃR�[�h ,�l�c��ƃR�[�h ,�X�܃R�[�h ,���W�ԍ� ,����ԍ� ,������� ,�|�C���g�N�[�|�����R�[�h) FROM 'EcCouponData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS�l�j�d�o�N�[�|�����p;
INSERT INTO WS�l�j�d�o�N�[�|�����p (
     �O�[�|������h�c
    ,��ƃR�[�h
    ,�l�c��ƃR�[�h
    ,�X�܃R�[�h
    ,���W�ԍ�
    ,����ԍ�
    ,�������
    ,�|�C���g�N�[�|�����R�[�h
) 
SELECT 
     �O�[�|������h�c
    ,��ƃR�[�h
    ,�l�c��ƃR�[�h
    ,SUBSTR(�X�܃R�[�h, 11) AS �X�܃R�[�h
    ,���W�ԍ�
    ,����ԍ�
    ,�������
    ,�|�C���g�N�[�|�����R�[�h
FROM tmp_WS�l�j�d�o�N�[�|�����p;
TRUNCATE TABLE tmp_WS�l�j�d�o�N�[�|�����p;
