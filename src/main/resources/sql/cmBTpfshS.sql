
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_GOODS_POINT_KK.CSV

SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , RPAD(A.�i�`�m�R�[�h,LENGTH(A.�i�`�m�R�[�h)) , '"' , CHR(9) ,
'"' , RPAD(A.���i����,LENGTH(A.���i����)) , '"' , CHR(9) ,
'"' , A.�{���Œ�|�C���g�l , '"')
FROM
 WS�|�C���g�t�^���iO A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
;
\o
