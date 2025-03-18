------------------------------------------------------------
--  顧客システム
--    テーブルスペース : 顧客制度
--    テーブル名       : MSクーポン利用顧客情報nnnnn
--                     : 想定最大35000000件
--    ファイル名       : ctbMSCAcprc.sql
--    作成日           : 2023/05/15 18:26:58
------------------------------------------------------------

create table  MSクーポン利用顧客情報nnnnn
(
        クーポンＩＤ                    NUMERIC(5)            DEFAULT 0 NOT NULL
      , 顧客番号                        NUMERIC(15)           DEFAULT 0 NOT NULL
      , 利用可能残回数                  NUMERIC(2)           
      , 最終利用年月日                  NUMERIC(8)           
      , 最終更新日                      NUMERIC(8)           
      , 最終更新日時                    TIMESTAMP(0)                
      , 最終更新プログラムＩＤ          CHAR(20)            
);
ALTER TABLE MSクーポン利用顧客情報nnnnn ADD CONSTRAINT PKMSCAcprc00nnnnn PRIMARY KEY (クーポンＩＤ,顧客番号);
ALTER INDEX PKMSCAcprc00nnnnn SET (fillfactor = 90);
ALTER TABLE MSクーポン利用顧客情報nnnnn SET (fillfactor = 90);
