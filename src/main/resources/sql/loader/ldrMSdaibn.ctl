OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'BUMON.dat'
TRUNCATE
INTO TABLE MS大分類情報
FIELDS TERMINATED BY ','
(
大分類コード,
dummy FILLER,
開始年月日,
終了年月日,
大分類名称,
大分類名称カナ,
バッチ更新日              CONSTANT '@BATDATE@',
最終更新日                CONSTANT '@BATDATE@',
最終更新日時              SYSDATE,
最終更新プログラムＩＤ    CONSTANT 'cmBTmsc1S'
)
