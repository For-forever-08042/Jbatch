OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'PCS_TR_017.dat'
APPEND
INTO TABLE TSＰＯＳＡカード取引明細@PNAME@
FIELDS TERMINATED BY ','
TRAILING NULLCOLS
(
      システム年月日     CONSTANT '@BATDATE@'
    , 処理通番        SEQUENCE(COUNT)
    , 会社コード
    , 店舗コード
    , 売上日
    , ノードＮＯ
    , 取引連番
    , レシート明細行
    , 顧客コード
    , アクティベート区分
    , 商品区分
    , 主契約者番号ＰＡＮ
    , 取引金額
    , 値引割引額
    , 販売金額
    , 伝送日時
    , システムトレース監査番号
    , 現地トランザクション日付
    , 現地トランザクション時刻
    , マーチャントＩＤ
    , 端末ＩＤ
    , 通貨コード
    , ＪＡＮコード
    , ＪＡＮＶＡＮコード
    , メッセージタイプ
    , 承認ＩＤ応答
    , 応答コード
    , アプリケーションエラーフラグ
    , 最終結果フラグ
    , 自動取消フラグ
    , Ｄポイントカード番号
    , 自動値引金額
    , 自動割引金額
    , クーポン値引金額
    , クーポン割引金額
    , ＭＭ値引金額
    , 特売企画コード
    , クーポン値引企画コード
    , クーポン割引企画コード
    , バッチ更新日              CONSTANT '@BATDATE@'
    , 最終更新日                CONSTANT '@BATDATE@'
    , 最終更新日時              SYSDATE
    , 最終更新プログラムＩＤ    CONSTANT 'cmBTposaS'
)
