OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'CAMP.dat'
APPEND
INTO TABLE MSキャンペーン情報
FIELDS TERMINATED BY','
(
キャンペーンＩＤ,
キャンペーン種別,
キャンペーン名称,
予約日,
取込日                   CONSTANT '@BATDATE@',
バッチ更新日             CONSTANT '@BATDATE@',
最終更新日               CONSTANT '@BATDATE@',
最終更新日時             SYSDATE,
最終更新プログラムＩＤ   CONSTANT 'cmBTcrcpS'
)
