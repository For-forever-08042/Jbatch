OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_014.dat'
APPEND
PRESERVE BLANKS
INTO TABLE HSカテゴリ別ポイント実績@PNAME@
(
会社コード            POSITION(1:4) CHAR,
店舗コード            POSITION(5:8) CHAR,
データ年月日          POSITION(9:16) CHAR,
データ生成時刻        POSITION(17:22) CHAR,
ポイント付与日付      POSITION(23:30) CHAR,
カテゴリコード        POSITION(31:33) CHAR,
付与ポイント数        POSITION(34:42) CHAR,
有効期限フラグ        POSITION(43:43) CHAR,
有効期限              POSITION(44:51) CHAR,
システム年月日        CONSTANT '@BATDATE@',
グローバル会員フラグ  POSITION(52:54) CHAR
   "CASE WHEN :グローバル会員フラグ ='TWN' THEN 1
         WHEN :グローバル会員フラグ ='HKG' THEN 2
         ELSE NULL END"
)
