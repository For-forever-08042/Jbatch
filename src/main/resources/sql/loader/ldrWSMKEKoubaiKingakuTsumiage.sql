--step2
TRUNCATE TABLE tmp_WS�l�j�d�w�����z�Ϗ�;
\set ON_ERROR_STOP true
\COPY  tmp_WS�l�j�d�w�����z�Ϗ� (     �l�c��ƃR�[�h ,�X�܃R�[�h ,�d���r�d�p�ԍ� ,������� ,�w������ ,����ԍ� ,�O�[�|������h�c ,����敪 ,���M���� ,�������� ,���M���ʃR�[�h ,�����N����p���z ,�����N����p�|�C���g�x�����z ) FROM 'dekk_buy_price_log_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS�l�j�d�w�����z�Ϗ�;
INSERT INTO WS�l�j�d�w�����z�Ϗ� (
     �l�c��ƃR�[�h
    ,�X�܃R�[�h
    ,�d���r�d�p�ԍ�
    ,�������
    ,�w������
    ,����ԍ�
    ,�O�[�|������h�c
    ,����敪
    ,���M����
    ,��������
    ,���M���ʃR�[�h
    ,�����N����p���z
    ,�����N����p�|�C���g�x�����z
) 
SELECT 
     �l�c��ƃR�[�h
    ,�X�܃R�[�h
    ,�d���r�d�p�ԍ�
    ,�������
    ,�w������
    ,����ԍ�
    ,�O�[�|������h�c
    ,����敪
    ,���M����
    ,NVL(��������, '-1') AS ��������
    , NVL(���M���ʃR�[�h, '-1') AS ���M���ʃR�[�h
    ,�����N����p���z
    ,�����N����p�|�C���g�x�����z
FROM tmp_WS�l�j�d�w�����z�Ϗ�;
TRUNCATE TABLE tmp_WS�l�j�d�w�����z�Ϗ�;
