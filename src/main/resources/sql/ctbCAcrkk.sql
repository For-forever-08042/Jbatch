------------------------------------------------------------
--  �ڋq�V�X�e��
--    �e�[�u���X�y�[�X : �ڋq���x
--    �e�[�u����       : MS�N�[�|�����p�ڋq���nnnnn
--                     : �z��ő�35000000��
--    �t�@�C����       : ctbMSCAcprc.sql
--    �쐬��           : 2023/05/15 18:26:58
------------------------------------------------------------

create table  MS�N�[�|�����p�ڋq���nnnnn
(
        �N�[�|���h�c                    NUMERIC(5)            DEFAULT 0 NOT NULL
      , �ڋq�ԍ�                        NUMERIC(15)           DEFAULT 0 NOT NULL
      , ���p�\�c��                  NUMERIC(2)           
      , �ŏI���p�N����                  NUMERIC(8)           
      , �ŏI�X�V��                      NUMERIC(8)           
      , �ŏI�X�V����                    TIMESTAMP(0)                
      , �ŏI�X�V�v���O�����h�c          CHAR(20)            
);
ALTER TABLE MS�N�[�|�����p�ڋq���nnnnn ADD CONSTRAINT PKMSCAcprc00nnnnn PRIMARY KEY (�N�[�|���h�c,�ڋq�ԍ�);
ALTER INDEX PKMSCAcprc00nnnnn SET (fillfactor = 90);
ALTER TABLE MS�N�[�|�����p�ڋq���nnnnn SET (fillfactor = 90);
