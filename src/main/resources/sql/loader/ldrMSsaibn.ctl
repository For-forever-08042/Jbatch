OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'SAIBUNRUI.dat'
TRUNCATE
INTO TABLE MS細分類情報
FIELDS TERMINATED BY ','
(
大分類コード              CONSTANT 0,
中分類コード,
小分類コード,
細分類コード,
開始年月日,
終了年月日,
細分類名称,
細分類名称カナ,
バッチ更新日              CONSTANT '@BATDATE@',
最終更新日                CONSTANT '@BATDATE@',
最終更新日時              SYSDATE,
最終更新プログラムＩＤ    CONSTANT 'cmBTmsc4S'
)
