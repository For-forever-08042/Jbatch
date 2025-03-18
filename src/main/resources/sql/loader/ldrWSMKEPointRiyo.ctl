OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'dekk_use_point_log_bmee_sjis.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＥポイント利用
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     ＭＤ企業コード
    ,店舗コード
    ,電文ＳＥＱ番号
    ,取引日時
    ,取引番号
    ,グーポン会員ＩＤ
    ,還元区分
    ,ポイント数
    ,送信日時
    ,応答日時
    ,送信結果コード
    ,還元結果フラグ
    ,変更ＩＤ
)
