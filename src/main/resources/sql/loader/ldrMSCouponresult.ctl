OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE '%INPUTFILE%'
APPEND
INTO TABLE MSクーポン企画実績@PNAME@
FIELDS TERMINATED BY ','
TRAILING NULLCOLS
(
      売上日
    , 会社コード
    , 店舗コード
    , レジＮＯ
    , レシートＮＯ
    , 会員番号
    , クーポン企画コード
    , 付与ポイント
    , 登録区分                  CONSTANT '1'
    , 取引区分                  CONSTANT '%TRNCTGR%'
    , バッチ更新日              CONSTANT '@BATDATE@'
    , 最終更新日                CONSTANT '@BATDATE@'
    , 最終更新日時              SYSDATE
    , 最終更新プログラムＩＤ    CONSTANT 'cmBTgpnjS'
)
