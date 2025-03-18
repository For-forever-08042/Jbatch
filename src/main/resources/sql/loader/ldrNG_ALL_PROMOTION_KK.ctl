OPTIONS (ERRORS = 0)
LOAD DATA
CHARACTERSET JA16SJIS
INFILE 'NG_ALL_PROMOTION_KK.CSV'
APPEND
INTO TABLE MSポイント付与条件O
FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
TRAILING NULLCOLS
(
    企画ＩＤ
   ,バージョン
   ,確定区分
   ,企業コード
   ,発注番号
   ,企画名称
   ,ポイントカテゴリ
   ,組織指定区分
   ,部門指定区分
   ,カード種別指定区分
   ,会員指定区分
   ,倍率固定値区分
   ,計算対象外区分
   ,ポイント支払付与対象外区分
   ,買上高ポイント計算方法フラグ
   ,ポイント付与会員修正フラグ
   ,ポイント種別
   ,開始日
   ,終了日
   ,削除フラグ
   ,送信日 CHAR "TO_DATE(:送信日, 'YYYY/MM/DD HH24:MI:SS')"
   ,作成日 CHAR "TO_DATE(:作成日, 'YYYY/MM/DD HH24:MI:SS')"
   ,確定日時 CHAR "TO_DATE(:確定日時, 'YYYY/MM/DD HH24:MI:SS')"
   ,期間限定ポイント付与開始日
   ,期間限定ポイント有効期限
   ,タイムサービス時間帯開始時刻
   ,タイムサービス時間帯終了時刻
   ,全曜日フラグ
   ,月曜日フラグ
   ,火曜日フラグ
   ,水曜日フラグ
   ,木曜日フラグ
   ,金曜日フラグ
   ,土曜日フラグ
   ,日曜日フラグ
   ,期間限定ポイント計算方法区分
   ,バッチ更新日              CONSTANT '@BATDATE@'
   ,最終更新日                CONSTANT '@BATDATE@'
   ,最終更新日時              SYSDATE
   ,最終更新プログラムＩＤ    CONSTANT 'cmBThfjkS'
   ,配信区分                  CONSTANT '%HAISHINKBN%'
)