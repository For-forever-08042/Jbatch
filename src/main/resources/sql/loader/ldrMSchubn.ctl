OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'CATEGORY.dat'
TRUNCATE
INTO TABLE MS中分類情報
FIELDS TERMINATED BY ','
(
大分類コード              CONSTANT 0,
中分類コード,
開始年月日,
終了年月日,
中分類名称,
中分類名称カナ,
計上部門コード,
バッチ更新日              CONSTANT '@BATDATE@',
最終更新日                CONSTANT '@BATDATE@',
最終更新日時              SYSDATE,
最終更新プログラムＩＤ    CONSTANT 'cmBTmsc2S'
)
