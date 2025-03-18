\set BAT_YYYYMMDD :1                                                                        --バッチ処理日
\set MAINTTBL :2                                                                            --メンテナンステーブル名
\set SOHIKISHITEIKUBUN :3                                                                   --組織して区分
\set INSERT_TBL :4                                                                          --登録テーブル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./cmBTpfksS.log

INSERT INTO :INSERT_TBL
( 
  会社コード
  , 店舗コード
  , 企画ＩＤ
  , バージョン
  , バッチ更新日
  , 最終更新日
  , 最終更新日時
  , 最終更新プログラムＩＤ
) ( 
  SELECT
    ( 
      CASE 
        WHEN 会社コード = '0001' 
          THEN 1000 
        ELSE TO_NUMBER(会社コード) 
        END
    ) 
    , 店番号
    , 企画ＩＤ
    , バージョン
    , :BAT_YYYYMMDD
    , :BAT_YYYYMMDD
    , SYSDATE()
    , 'cmBTpfksS' 
  FROM
    ( 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON
        , PS店表示情報ＭＣＣ 
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 1 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2) 
        and MSポイント付与組織.組織コード = PS店表示情報ＭＣＣ.旧企業コード
        and (
            %SUBWHERE%
            )
      UNION 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON
        , PS店表示情報ＭＣＣ 
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 2 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2) 
        and MSポイント付与組織.組織コード = PS店表示情報ＭＣＣ.業態コード 
        and (
            %SUBWHERE%
            )
      UNION 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON
        , PS店表示情報ＭＣＣ 
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 3 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2) 
        and TO_CHAR(MSポイント付与組織.組織コード) = SUBSTR(PS店表示情報ＭＣＣ.ＳＶブロックコード, 1, 2) 
        and (
            %SUBWHERE%
            )
      UNION 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON
        , PS店表示情報ＭＣＣ 
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 4 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2) 
        and TO_CHAR(MSポイント付与組織.組織コード) = SUBSTR(PS店表示情報ＭＣＣ.ＳＶブロックコード, 1, 4) 
        and (
            %SUBWHERE%
            )
      UNION 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON
        , PS店表示情報ＭＣＣ 
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 5 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2) 
        and TO_CHAR(MSポイント付与組織.組織コード) = PS店表示情報ＭＣＣ.ＳＶブロックコード 
        and (
            %SUBWHERE%
            )
      UNION 
      SELECT
        CON.企画ＩＤ
        , CON.バージョン
        , CON.組織指定区分
        , TO_CHAR(PS店表示情報ＭＣＣ.会社コード) as 会社コード
        , SUBSTR(PS店表示情報ＭＣＣ.店番号, 1, 4) as 店番号
        , CON.開始日
        , CON.終了日
        , NVL(CON.全曜日フラグ, 0) AS 全曜日フラグ
        , NVL(CON.月曜日フラグ, 0) AS 月曜日フラグ
        , NVL(CON.火曜日フラグ, 0) AS 火曜日フラグ
        , NVL(CON.水曜日フラグ, 0) AS 水曜日フラグ
        , NVL(CON.木曜日フラグ, 0) AS 木曜日フラグ
        , NVL(CON.金曜日フラグ, 0) AS 金曜日フラグ
        , NVL(CON.土曜日フラグ, 0) AS 土曜日フラグ
        , NVL(CON.日曜日フラグ, 0) AS 日曜日フラグ
        , RANK() OVER (partition by CON.企画ＩＤ,CON.バージョン,PS店表示情報ＭＣＣ.会社コード,PS店表示情報ＭＣＣ.店番号 order by PS店表示情報ＭＣＣ.開始年月日 desc, CON.バージョン desc) as rank_value 
      FROM
        MSポイント付与組織
        , MSポイント付与条件:MAINTTBL CON 
        , PS店表示情報ＭＣＣ
      WHERE
        CON.企画ＩＤ = MSポイント付与組織.企画ＩＤ 
        and CON.バージョン = MSポイント付与組織.バージョン 
        and MSポイント付与組織.組織レベル = 6 
        and CON.確定区分 = '1' 
        and CON.削除フラグ = 0 
        and CON.組織指定区分 IN (1, 2)
        and CAST(SUBSTR(LPAD(MSポイント付与組織.組織コード, 8, '0'), 1, 4) AS NUMERIC) = PS店表示情報ＭＣＣ.旧企業コード
        and CAST(SUBSTR(MSポイント付与組織.組織コード, - 4) AS NUMERIC)=PS店表示情報ＭＣＣ.店番号
        and (
            %SUBWHERE%
            )
    ) TGT
  WHERE
    rank_value = 1 
    AND NOT EXISTS (select 1 from :INSERT_TBL T where CAST(T.会社コード AS TEXT)=TGT.会社コード and CAST(T.店舗コード AS TEXT)=TGT.店番号 and T.企画ＩＤ=TGT.企画ＩＤ and T.バージョン=TGT.バージョン)
    AND 組織指定区分 = :SOHIKISHITEIKUBUN
)
;

\o
