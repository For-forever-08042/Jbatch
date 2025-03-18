OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_TR_014.tmp1'
APPEND
INTO TABLE WSサンプル品お渡し実績
FIELDS TERMINATED BY ','
TRAILING NULLCOLS
(
     お渡し日
     ,企業コード
     ,店舗コード
     ,レジＮＯ
     ,レシートＮＯ
     ,お渡し時刻
     ,顧客コード
     ,販促企画コード
     ,お渡し区分
     ,Ｄポイント番号
)

