
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_SHOP_KK.CSV


SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , A.�g�D���x�� , '"' , CHR(9) ,
'"' , A.�g�D�R�[�h, '"')
FROM
 WS�|�C���g�t�^�g�DO A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
;
\o
