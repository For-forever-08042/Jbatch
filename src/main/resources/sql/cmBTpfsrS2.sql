\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./NG_PROMOTION_GOODS_POINT_2.CSV

SELECT 
CONCAT('"', LPAD(A.���h�c, 10, '0'), '"', CHR(9),
NVL(A.�o�[�W����, 0), CHR(9),
'"', NVL(RPAD(A.�i�`�m�R�[�h,LENGTH(A.�i�`�m�R�[�h)), ''), '"', CHR(9),
'"',  NULLIF(RTRIM(A.���i����),''), '"', CHR(9),
A.�{���Œ�|�C���g�l)
FROM
    MS�|�C���g�t�^���iO A 
INNER JOIN
    WS�|�C���g�t�^���� B 
ON
    A.���h�c = B.���h�c 
AND A.�o�[�W���� = B.�o�[�W����
AND B.�|�C���g��� = 2
;

\o
