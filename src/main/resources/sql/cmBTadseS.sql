\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./AddressCode_IN.tmp
TRUNCATE TABLE WS�ڋq�ԍ��Q;
INSERT INTO WS�ڋq�ԍ��Q (�ڋq�ԍ�) 
        SELECT �ڋq�ԍ� FROM MM�ڋq��� WHERE �ŏI�X�V�� >= :1
        UNION
        SELECT �ڋq�ԍ� FROM MM�ڋq������� WHERE �ŏI�X�V�� >= :1
        UNION
        SELECT �ڋq�ԍ� FROM MM�ڋq��ƕʑ������ WHERE �ŏI�X�V�� >= :1
        UNION
        SELECT �ڋq�ԍ� FROM MS�J�[�h��� WHERE �ŏI�X�V�� >= :1 AND �ڋq�ԍ� <> 0
        UNION
        SELECT C.�ڋq�ԍ� FROM MS�O���F�؏�� G, MS�J�[�h��� C 
                        WHERE G.�T�[�r�X��� = C.�T�[�r�X��� AND G.����ԍ� = C.����ԍ�
                         AND  G.�O���F�؎�� = 'L'            AND G.�ŏI�X�V�� >= :1
;
commit;
SELECT
'�ڋq�ԍ�,�Z��'
FROM 
 dual
;
SELECT
CONCAT(M.�ڋq�ԍ� , chr(9) ,
NVL(NULLIF(TRIM(M.�Z��),''),'�@'))
FROM 
 MM�ڋq������� M,
 WS�ڋq�ԍ��Q W
WHERE 
     W.�ڋq�ԍ� = M.�ڋq�ԍ�
;
\o
