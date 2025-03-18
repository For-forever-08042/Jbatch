OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_TR_015.tmp1'
APPEND
INTO TABLE WSレシートクーポン発行履歴
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
     ,Ｄポイント番号
)

