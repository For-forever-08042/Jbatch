--step2
TRUNCATE TABLE WS�l�j�d�|�C���g�t�^;
\COPY WS�l�j�d�|�C���g�t�^ (    �l�c��ƃR�[�h ,�X�܃R�[�h ,����ԍ� ,������� ,�O�[�|������h�c ,����敪 ,������ō� ,�|�C���g�x�����z ,���ЃN���W�b�g�敪 ,���h�c ,���o�[�W���� ,�|�C���g�J�e�S�� ,�|�C���g��� ,�t�^�敪 ,�t�^�|�C���g�� ,�i�`�m�R�[�h ,���i�w���� ,���㍂�|�C���g��� ,�Ώۋ��z�Ŕ� ,���i�p�[�Z���g�|�C���g�t�^�� ,�d�c�x�ԍ� ,���Ԍ���|�C���g�L������ ,��w���t���O ,���Ԍ���|�C���g�t�^�J�n�� ,�N�[�|���R�[�h ,�|�C���g�t�^�����  ) FROM 'dekk_grant_point_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
