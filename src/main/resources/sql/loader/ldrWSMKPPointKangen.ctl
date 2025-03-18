OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_PT_005.dat'
TRUNCATE
PRESERVE BLANKS
INTO TABLE WSＭＫＰポイント還元
FIELDS TERMINATED BY '\t'
TRAILING NULLCOLS
(
     企業コード
    ,会社コード
    ,店舗コード
    ,レジ番号
    ,電文ＳＥＱ番号
    ,取引日時
    ,取引番号
    ,カード種別
    ,カード番号
    ,還元種別
    ,登録経路
    ,還元ポイント付与数
    ,送信日時
    ,応答日時
    ,送信結果コード
    ,還元結果フラグ
    ,前期ポイント数
    ,前期ポイント有効期限
    ,今期ポイント数
    ,今期ポイント有効期限
    ,変更ＩＤ
)

