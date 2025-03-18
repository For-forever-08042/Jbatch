\set SBATDATE :1                                                                                  --バッチ処理日
\set SBATDATEYYYYMM :2                                                                            --バッチ処理日YYYYMM
\set SFILENAME :3                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
\o ./:SFILENAME

--データ抽出
SELECT
      CONCAT('0001' ,CHR(9),
      LPAD(TBLA.店舗コード,5,'0') ,CHR(9),
      LPAD(TBLA.取引番号,10,'0') ,CHR(9),
      TBLA.取引日時 , LPAD(NVL(TBLA.取引時刻,'0'),6,'0') ,CHR(9),
      TBLA.ＧＯＯＰＯＮ会員番号 ,CHR(9),
      TBLA.取引区分 ,CHR(9),
      TBLA.取引高税込 ,CHR(9),
      TBLA.ポイント支払金額 ,CHR(9),
      TBLA.他社クレジット区分 ,CHR(9),
      LPAD(TBLA.企画ＩＤ,10,'0') ,CHR(9),
      TBLA.企画バージョン ,CHR(9),
      TBLA.ポイントカテゴリ ,CHR(9),
      TBLA.ポイント種別 ,CHR(9),
      TBLA.付与区分 ,CHR(9),
      TBLA.付与ポイント数 ,CHR(9),
      TBLA.ＪＡＮコード ,CHR(9),
      TBLA.商品購入数 ,CHR(9),
      LPAD(TBLA.買上高ポイント種別,5,'0') ,CHR(9),
      TBLA.対象金額税抜 ,CHR(9),
      TRIM_SCALE(TBLA.商品パーセントポイント付与率) ,CHR(9),
      RPAD(TBLA.ＥＤＹＮＯ,LENGTH(TBLA.ＥＤＹＮＯ)) ,CHR(9),
      TBLA.期間限定ポイントの有効期限 ,CHR(9),
      (CASE WHEN TBLA.非購買フラグ = 1 THEN '1' ELSE NULL END) ,CHR(9),
      (CASE WHEN TBLA.期間限定ポイント付与開始日 = 0 THEN NULL ELSE TO_CHAR(TBLA.期間限定ポイント付与開始日) END) ,CHR(9),
      RPAD(TBLA.クーポンコード,LENGTH(TBLA.クーポンコード)))
FROM
    TSＥＣポイント計算結果:SBATDATEYYYYMM TBLA                                                      --バッチ処理日YYYYMM
WHERE
    TBLA.システム年月日 = :SBATDATE                                                              --バッチ処理日
;

\o

