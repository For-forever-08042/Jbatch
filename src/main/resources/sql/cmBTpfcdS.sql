
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CARD_KIND_KK.CSV


SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , CASE WHEN SUBSTR(A.�J�[�h�ԍ�, 14, 5) ='67988'    THEN 701 ELSE 703 END  , '"') 
FROM
 WS�|�C���g�t�^���O A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
WHERE
    LENGTH(A.�J�[�h�ԍ�) = 20
UNION ALL
SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , C.�J�[�h��� , '"')
FROM
 (SELECT �����ԍ�, 
CASE WHEN SUBSTR(�J�[�h�ԍ�, 1, 4) = '9881' AND LENGTH(�J�[�h�ԍ�) = 16   THEN CONCAT(�J�[�h�ԍ� , '0') ELSE �J�[�h�ԍ� END  �J�[�h�ԍ�
  FROM WS�|�C���g�t�^���O   ) A
INNER JOIN
 WS�|�C���g�t�^����O B
ON
    A.�����ԍ� = B.�����ԍ�
JOIN PS����ԍ��̌n C
ON   A.�J�[�h�ԍ� >=  CAST(C.����ԍ��J�n AS TEXT)
AND  A.�J�[�h�ԍ� <=  CAST(C.����ԍ��I�� AS TEXT)
WHERE
    LENGTH(A.�J�[�h�ԍ�) <> 20
;
\o
