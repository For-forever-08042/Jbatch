--step2
\set ON_ERROR_STOP true
\COPY tmp_WS�l�j�d�Čv�Z�o�N�[�|�����p (   �O�[�|������h�c ,��ƃR�[�h ,�l�c��ƃR�[�h ,�X�܃R�[�h ,���W�ԍ� ,����ԍ� ,������� ,�|�C���g�N�[�|�����R�[�h,���ד���) FROM 'EcCouponRecovData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );

INSERT INTO WS�l�j�d�Čv�Z�o�N�[�|�����p(
     �O�[�|������h�c
    ,��ƃR�[�h
    ,�l�c��ƃR�[�h
    ,�X�܃R�[�h
    ,���W�ԍ�
    ,����ԍ�
    ,�������
    ,�|�C���g�N�[�|�����R�[�h
    ,���ד���
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
    ,���ד���
FROM tmp_WS�l�j�d�Čv�Z�o�N�[�|�����p;
TRUNCATE TABLE tmp_WS�l�j�d�Čv�Z�o�N�[�|�����p;
