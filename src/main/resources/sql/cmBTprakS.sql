
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_RANK_KK.CSV



SELECT
CONCAT('"' , B.���h�c , '"' , CHR(9) ,
'"' , B.�o�[�W���� , '"' , CHR(9) ,
'"' , A.�����N�R�[�h , '"' , CHR(9) ,
'"' , A.�v���~�A���|�C���g�敪 , '"' , CHR(9) ,
'"' , A.�v�Z��P�ʊz , '"' , CHR(9) ,
'"' , TRIM_SCALE(A.�|�C���g�t�^��), '"')
FROM
 WS�|�C���g�t�^��������NO A
INNER JOIN
 WS�|�C���g�t�^����O B 
ON
    A.�����ԍ� = B.�����ԍ� 
;

\o
