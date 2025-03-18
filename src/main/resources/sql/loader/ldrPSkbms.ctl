OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'KBMS_DATA.dat' "FIX 1574"
INTO TABLE PS区分値情報
TRUNCATE
TRAILING NULLCOLS
(
区分ＩＤ                 POSITION(1:10)       CHAR(10),
区分値                   POSITION(11:20)      CHAR(10) "NVL(trim(:区分値), ' ')",
カナ名                   POSITION(21:60)      CHAR(40),
区分名                   POSITION(61:160)     CHAR(100),
備考                     POSITION(161:560)    CHAR(400),
付加属性１               POSITION(561:660)    CHAR(100),
付加属性２               POSITION(661:760)    CHAR(100),
付加属性３               POSITION(761:860)    CHAR(100),
付加属性４               POSITION(861:960)    CHAR(100),
付加属性５               POSITION(961:1060)   CHAR(100),
付加属性６               POSITION(1061:1160)  CHAR(100),
付加属性７               POSITION(1161:1260)  CHAR(100),
付加属性８               POSITION(1261:1360)  CHAR(100),
付加属性９               POSITION(1361:1460)  CHAR(100),
付加属性１０             POSITION(1461:1560)  CHAR(100),
表示順                   POSITION(1561:1564)  CHAR(4),
桁数                     POSITION(1565:1574)  CHAR(10),
バッチ更新日             CONSTANT '@BATDATE@',
最終更新日               CONSTANT '@BATDATE@',
最終更新日時             SYSDATE,
最終更新プログラムＩＤ   CONSTANT 'cmBTkbmsS'
)
