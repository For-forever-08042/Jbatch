--step2
TRUNCATE TABLE tmp_WS�l�j�d�|�C���g�v�Z;
\set ON_ERROR_STOP true
\COPY tmp_WS�l�j�d�|�C���g�v�Z( �O�[�|������h�c ,��ƃR�[�h ,�l�c��ƃR�[�h ,�X�܃R�[�h ,���W�ԍ� ,����ԍ� ,������� ,�z�M�r�d�p�ԍ� ,�����W�ԍ� ,������ԍ� ,��������� ,������ō� ,����Ŋz ,���i�R�[�h ,�v�㕔��R�[�h ,��Е���R�[�h ,�����ރR�[�h ,�����ރR�[�h ,��J�e�S�� ,���J�e�S�� ,���J�e�S�� ,�|�C���g��Ώۃt���O ,���� ,���׋��z�Ŕ��ōl���L ,�|�C���g�� ,�J�[�h��� ,�J�[�h��Дԍ� ,�x���J�[�h��� ,�x���J�[�h�ԍ� ,�x�����z ,���ЃN���W�b�g�敪 ,������ʎq ,�d�c�x�m�n ,�d�c�x�x�����z ,�|�C���g�x���敪 ,�|�C���g�x�����z ,�쐬���� ,���݃����N�� ,���݃����N�N ,�c�������I�t���C���t���O ,���׋��z�Ŕ��ōl���� ,���ד��� ) FROM 'EcPointCalcData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WS�l�j�d�|�C���g�v�Z;
INSERT INTO tmp_WS�l�j�d�|�C���g�v�Z 
SELECT 
     �O�[�|������h�c
    ,��ƃR�[�h
    ,�l�c��ƃR�[�h
    ,SUBSTR(�X�܃R�[�h, 11) AS �X�܃R�[�h
    ,���W�ԍ�
    ,����ԍ�
    ,�������
    ,�z�M�r�d�p�ԍ�
    ,�����W�ԍ�
    ,������ԍ�
    ,���������
    ,������ō�
    ,����Ŋz
    ,���i�R�[�h
    ,�v�㕔��R�[�h
    ,��Е���R�[�h
    ,�����ރR�[�h
    ,�����ރR�[�h
    ,��J�e�S��
    ,���J�e�S��
    ,���J�e�S��
    ,�|�C���g��Ώۃt���O
    ,����
    ,���׋��z�Ŕ��ōl���L
    ,�|�C���g��
    ,�J�[�h���
    ,�J�[�h��Дԍ�
    ,�x���J�[�h���
    ,�x���J�[�h�ԍ�
    ,�x�����z
    ,���ЃN���W�b�g�敪
    ,������ʎq
    ,�d�c�x�m�n
    ,�d�c�x�x�����z
    ,�|�C���g�x���敪
    ,�|�C���g�x�����z
    ,�쐬����
    ,���݃����N��
    ,���݃����N�N
    ,�c�������I�t���C���t���O
    ,���׋��z�Ŕ��ōl����
    ,���ד���
FROM tmp_WS�l�j�d�|�C���g�v�Z;
TRUNCATE TABLE tmp_WS�l�j�d�|�C���g�v�Z;
