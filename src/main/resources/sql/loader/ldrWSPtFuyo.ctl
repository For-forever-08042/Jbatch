OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_016.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSポイント付与データ
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     企業コード
    ,会社コード
    ,店舗コード
    ,レジ番号
    ,電文ＳＥＱ番号
    ,取引日時   CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:取引日時, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS'))"
    ,購入日時   CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:購入日時, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS'))"
    ,取引番号
    ,カード種別
    ,カード番号
    ,取引区分
    ,登録経路
    ,電文明細数
    ,取引高
    ,ＰＯＳ種別
    ,ポイント支払金額
    ,他社クレジット区分
    ,受付区分
    ,送信日時   CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:送信日時, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS'))"
    ,応答日時   CHAR "TO_NUMBER(TO_CHAR(TO_DATE(:応答日時, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS'))"
    ,送信結果コード
    ,ランク判定用売上金額
    ,ランク判定用ポイント支払金額
    ,販促企画番号１
    ,お渡し済みフラグ１
    ,販促企画番号２
    ,お渡し済みフラグ２
    ,営業日                         "NVL(:営業日, ' ')"
)
