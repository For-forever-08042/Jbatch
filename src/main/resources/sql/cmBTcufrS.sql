
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./:1

SELECT 
CONCAT('"' , NULLIF(TRIM(A.�N�[�|���h�c),'') , '",' , 
'"' , NULLIF(TRIM(A.�n�b�o�e���[�U�h�c),'') , '",' , 
'"' , C.�f�n�n�o�n�m�ԍ� , '",' , 
'"' , TO_CHAR(A.�N�[�|���z�M����, 'YYYY/MM/DD HH24:MI:SS') , '"') 
FROM
 WS�Q�[���N�[�|�����[�U�t�@�C�� A 
LEFT OUTER JOIN
 MS�O���F�؏�� B 
ON
 B.�O���F�؎�� = 'A'
AND
 TRIM(A.�n�b�o�e���[�U�h�c) = TRIM(B.�O���F�؂h�c)
LEFT OUTER JOIN
 MS�J�[�h��� C 
ON
 B.����ԍ� = C.����ԍ� 
AND
 B.�T�[�r�X��� = C.�T�[�r�X��� 
;

\o
