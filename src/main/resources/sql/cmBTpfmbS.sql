
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_MEMBER_LIST_KK.CSV


SELECT
CONCAT('"', B.���h�c, '"', CHR(9),
'"', B.�o�[�W����, '"', CHR(9),
'"', CASE WHEN SUBSTR(A.�J�[�h�ԍ�, 14, 5) = '67988'    THEN 71 ELSE 73 END,  '"', CHR(9),
'"', A.�J�[�h�ԍ�, '"' )
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
CONCAT( '"', B.���h�c, '"', CHR(9),
'"', B.�o�[�W����, '"', CHR(9),
'"', CASE WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 5) = '00021'    THEN '2' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 5) = '00022'    THEN '2' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 5) = '00023'    THEN '2' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '9881271'  THEN '3' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '9881272'  THEN '3' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 6) = '000271'   THEN '4' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 6) = '000272'   THEN '4' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '0200030'  THEN '5' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '0200000'  THEN '30' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '0200001'  THEN '30' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '9881273'  THEN '40' 
            WHEN SUBSTR(A.�J�[�h�ԍ�, 1, 7) = '0200020'  THEN '41' ELSE '0' END, '"', CHR(9),
'"', A.�J�[�h�ԍ�, '"')
FROM
 WS�|�C���g�t�^���O A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
WHERE
   LENGTH(A.�J�[�h�ԍ�) <> 20 
;
\o
