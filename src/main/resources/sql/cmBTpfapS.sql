
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_CATEGORY_KK.CSV


SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , A.���僌�x�� , '"' , CHR(9) ,
'"' , A.����R�[�h  , '"')
FROM
 WS�|�C���g�t�^����O A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
;
\o
