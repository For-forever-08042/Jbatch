--step2
TRUNCATE TABLE WS�l�j�d�|�C���g���p;
\COPY WS�l�j�d�|�C���g���p(  �l�c��ƃR�[�h ,�X�܃R�[�h ,�d���r�d�p�ԍ� ,������� ,����ԍ� ,�O�[�|������h�c ,�Ҍ��敪 ,�|�C���g�� ,���M���� ,�������� ,���M���ʃR�[�h ,�Ҍ����ʃt���O ,�ύX�h�c ) FROM 'dekk_use_point_log_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
