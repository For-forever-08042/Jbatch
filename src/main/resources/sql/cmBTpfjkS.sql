
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 


\o ./NG_ALL_PROMOTION_KK.CSV

SELECT
CONCAT(CONCAT('"' , A.���h�c , '"' , CHR(9) , 
'"' , A.�o�[�W���� , '"' , CHR(9) , 
'"' , RPAD(A.�m��敪,LENGTH(A.�m��敪)) , '"' , CHR(9) ,  
'"' , RPAD(A.��ƃR�[�h,LENGTH(A.��ƃR�[�h)) , '"' , CHR(9) ,  
'"' , RPAD(A.�����ԍ�,LENGTH(A.�����ԍ�)) , '"' , CHR(9) ,  
'"' , RPAD(A.��於��,LENGTH(A.��於��)) , '"' , CHR(9) ,  
'"' , A.�|�C���g�J�e�S�� , '"' , CHR(9) ,  
'"' , A.�g�D�w��敪 , '"' , CHR(9) ,  
'"' , A.����w��敪 , '"' , CHR(9) ,  
'"' , A.�J�[�h��ʎw��敪 , '"' , CHR(9) ,  
'"' , A.����w��敪 , '"' , CHR(9) ,  
'"' , A.�{���Œ�l�敪 , '"' , CHR(9) ,  
'"' , A.�v�Z�ΏۊO�敪 , '"' , CHR(9) ,  
'"' , A.�|�C���g�x���t�^�ΏۊO�敪 , '"' , CHR(9) ,  
'"' , A.���㍂�|�C���g�v�Z���@�t���O , '"' , CHR(9) ,  
'"' , A.�|�C���g�t�^����C���t���O , '"' , CHR(9) ,  
'"' , A.�|�C���g��� , '"' , CHR(9) ,  
'"' , A.�J�n�� , '"' , CHR(9) ,  
'"' , A.�I���� , '"' , CHR(9) ,  
'"' , A.�폜�t���O , '"' , CHR(9) ,  
'"' , TO_CHAR(A.���M��,'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , TO_CHAR(A.�쐬��,'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , TO_CHAR(COALESCE(A.�m�����,SYSDATE()),'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , A.���Ԍ���|�C���g�t�^�J�n�� , '"' , CHR(9)) ,  
CONCAT('"' , A.���Ԍ���|�C���g�L������ , '"' , CHR(9) ,  
'"' , A.�^�C���T�[�r�X���ԑъJ�n���� , '"' , CHR(9) ,  
'"' , A.�^�C���T�[�r�X���ԑяI������ , '"' , CHR(9) ,  
'"' , A.�S�j���t���O , '"' , CHR(9) ,  
'"' , A.���j���t���O , '"' , CHR(9) ,  
'"' , A.�Ηj���t���O , '"' , CHR(9) ,  
'"' , A.���j���t���O , '"' , CHR(9) ,  
'"' , A.�ؗj���t���O , '"' , CHR(9) ,  
'"' , A.���j���t���O , '"' , CHR(9) ,  
'"' , A.�y�j���t���O , '"' , CHR(9) ,  
'"' , A.���j���t���O , '"' , CHR(9) ,    
'"' , A.���Ԍ���|�C���g�v�Z���@�敪 , '"')) 
FROM
 WS�|�C���g�t�^����O A 
;
\o
