
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_AMOUNT_POINT_KK.CSV


SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , A.�}��, '"' , CHR(9) ,
'"' , A.�K�p�����z , '"' , CHR(9) ,
'"' , A.�v�Z��P�ʊz , '"' , CHR(9) ,
'"' , TRIM_SCALE(A.�{���Œ�|�C���g�l) , '"')
FROM
 WS�|�C���g�t�^���㍂O A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
;
\o
