--step2
\set ON_ERROR_STOP true
\COPY  tmp_PS�X�\�����( ��ƃR�[�h , �X�ԍ� , �J�n�N���� , �I���N���� , �����X�ܖ��� , �X�܃J�i���� , �X�ܒZ�k���� , dummy01 , dummy02 , dummy03 , dummy04 , dummy05 , dummy06 , dummy07 , dummy08 , dummy09 , dummy10 , dummy11 , �J�X�� , �X�� , dummy14 , dummy15 , dummy16 , dummy17 , dummy18 , dummy19 , dummy20 , dummy21 , dummy22 , dummy23 , dummy24 , dummy25 , dummy26 , dummy27 , dummy28 , dummy29 , ���܂n�s�b�敪 , dummy31 , dummy32 , dummy33 , dummy34 , dummy35 , �A�g�p�X�ԍ� , dummy37 , dummy38 , dummy39 , dummy40 , dummy41 , dummy42 , dummy43 , dummy44 , dummy45 , �s���{���R�[�h , dummy46 , dummy47 , dummy48 , �u���b�N�R�[�h , dummy49 , dummy50 , dummy51 , dummy52 , dummy53 , dummy54 , dummy55 , dummy56 , dummy57 , dummy58 , dummy59 , dummy60 , dummy61 , ���f���P , dummy63 , dummy64 , dummy65 , dummy66 , dummy67 , dummy68 , dummy69 , dummy70 , dummy71 , dummy72 , dummy73 , dummy74 , dummy75 , dummy76 , dummy77 , dummy78 , dummy79 , dummy80 , dummy81 , dummy82 , dummy83 , �q�w , �ʐϋ敪 , ���̎ЃR�[�h , dummy87 , dummy88 , dummy89 , dummy90 , dummy91 , dummy92 , dummy93 , dummy94 , dummy95 , dummy96 , dummy97 , dummy98 , dummy99 , dummy100 , dummy101 , dummy102 , dummy103 , dummy104 , dummy105 , dummy106 , dummy107 , dummy108 , dummy109 , dummy110 , dummy111 , dummy112 , dummy113 , dummy114 , dummy115 , dummy116 , dummy117 , dummy118 , dummy119 , dummy120 , dummy121 , dummy122 , dummy123 , dummy124 , dummy125 , dummy126 , dummy127 , dummy128 , dummy129 , dummy130 , dummy131 , dummy132 , dummy133 , dummy134 , dummy135 , dummy136 , dummy137 , dummy138 , dummy139 , dummy140 , dummy141 , dummy142 , dummy143 , dummy144 , dummy145 , dummy146 , dummy147 , dummy148 , dummy149 , dummy150 , dummy151 , dummy152 , dummy153 , dummy154 , dummy155 , dummy156 , dummy157 , dummy158 , dummy159 , dummy160 , dummy161 , dummy162 , dummy163 , dummy164 , dummy165 , dummy166 , dummy167 , dummy168 , dummy169 , dummy170 , dummy171 , dummy172 , dummy173 , dummy174 , dummy175 , dummy176 , dummy177 , dummy178 , dummy179 , dummy180 , dummy181 , dummy182 , dummy183 , dummy184 , dummy185 , dummy186 , dummy187 , dummy188 , dummy189 , dummy190 , dummy191 , dummy192 , dummy193 , dummy194 , dummy195 , dummy196 , dummy197 , dummy198 , dummy199 , dummy200 , dummy201 , dummy202 , dummy203 , dummy204 , dummy205 , dummy206 , dummy207 , dummy208 , dummy209 , dummy210 , dummy211 , dummy212 , dummy213 , dummy214 , dummy215 , dummy216 , �N���X�^ , ��Ɩ��� , ��ƒZ�k���� , ��Ɩ��̃J�i , ������ , ���Z�k���� , �����̃J�i , �]�[������ , �]�[���Z�k���� , �]�[�����̃J�i , �u���b�N���� , �u���b�N�Z�k���� , �u���b�N���̃J�i , �o�b�`�X�V�� , �ŏI�X�V�� , �ŏI�X�V���� , �ŏI�X�V�v���O�����h�c  ) FROM 'MISE.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );

INSERT INTO  PS�X�\�����(
��ƃR�[�h,
�X�ԍ�,
�J�n�N����,
�I���N����,
�����X�ܖ���,
�X�܃J�i����,
�X�ܒZ�k����,
�J�X��,
�X��,
���܂n�s�b�敪,
�A�g�p�X�ԍ�,
�s���{���R�[�h,
�u���b�N�R�[�h,
���f���P,
�q�w,
�ʐϋ敪,
���̎ЃR�[�h,
�N���X�^,
��Ɩ���,
��ƒZ�k����,
��Ɩ��̃J�i,
������,
���Z�k����,
�����̃J�i,
�]�[������,
�]�[���Z�k����,
�]�[�����̃J�i,
�u���b�N����,
�u���b�N�Z�k����,
�u���b�N���̃J�i,
�o�b�`�X�V��,
�ŏI�X�V��,
�ŏI�X�V����,
�ŏI�X�V�v���O�����h�c
) 
SELECT 
��ƃR�[�h,
�X�ԍ�,
�J�n�N����,
�I���N����,
�����X�ܖ���,
�X�܃J�i����,
�X�ܒZ�k����,
TO_NUMBER(NULLIF(TRIM(CAST(�J�X�� AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(�X�� AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(���܂n�s�b�敪 AS VARCHAR)),'')),
�A�g�p�X�ԍ�,
�s���{���R�[�h,
TO_NUMBER(NVL(NULLIF(TRIM(CAST(�u���b�N�R�[�h AS VARCHAR)),''), '0')),
TO_NUMBER(NULLIF(TRIM(CAST(���f���P AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(�q�w AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(�ʐϋ敪 AS VARCHAR)),'')),
TO_NUMBER(NVL(NULLIF(TRIM(CAST(���̎ЃR�[�h AS VARCHAR)),''), '0')),
CASE NVL(NULLIF(TRIM(�N���X�^),''),'#')
          WHEN 'A' THEN 1
          WHEN 'B' THEN 2
          WHEN 'C' THEN 3
          WHEN 'D' THEN 4
          WHEN 'Z' THEN 5
          WHEN '#' THEN 9
ELSE 
0
END,
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
:BATDATE,
:BATDATE,
SYSDATE(),
'cmBTmsstS'
FROM  tmp_PS�X�\����� where ��ƃR�[�h in ('1010','1020','1030','1060');
TRUNCATE TABLE  tmp_PS�X�\�����;
