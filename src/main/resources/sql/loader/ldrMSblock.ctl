OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'BLOCK.dat'
TRUNCATE
INTO TABLE MSブロック情報
FIELDS TERMINATED BY ','
(
ＳＶブロックコード,
発効日,
失効日,
会社コード,
ＳＶブロック名,
ＳＶブロック短縮名,
ＳＶブロック名カナ,
担当ＳＶＩＤ,
上位ＳＶブロックコード    CHAR "NVL(trim(:上位ＳＶブロックコード), '0')",
階層,
最下層フラグ,
バッチ更新日              CONSTANT '@BATDATE@',
最終更新日                CONSTANT '@BATDATE@',
最終更新日時              SYSDATE,
最終更新プログラムＩＤ    CONSTANT 'cmBTmsblS'
)
