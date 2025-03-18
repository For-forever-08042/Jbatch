\set SBATDATE :1                                                                                  --バッチ処理日
\set SFILENAME :2                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./:SFILENAME


--データ抽出
SELECT
    CONCAT(TBLA.売上日 ,',',
    NVL((CASE WHEN TBLA.データ区分 = '1'
          THEN TO_CHAR(CAST(TBLA.会社コード AS NUMERIC), 'FM0000')
          ELSE '2500'
          END), RPAD(' ', 4))  ,',',
    NVL((CASE WHEN TBLA.データ区分 = '1' 
          THEN TO_CHAR(CAST(TBLA.店舗コード AS NUMERIC), 'FM00000')
          ELSE TO_CHAR(TBLB.連携用店番号, 'FM00000')
          END), RPAD(' ', 5))  ,',',
    TO_CHAR(TBLA.レジＮＯ, 'FM00000') ,',',
    TO_CHAR(TBLA.レシートＮＯ, 'FM0000000000') ,',',
    TO_CHAR(TBLA.売上時刻, 'FM0000') ,',',
    NVL((CASE WHEN TBLC.ＧＯＯＰＯＮ番号 = 0
              THEN RPAD(' ', 16)
              ELSE TO_CHAR(TBLC.ＧＯＯＰＯＮ番号)
              END), RPAD(' ', 16)) ,',',
    NVL(RPAD(TBLA.部門コード,LENGTH(TBLA.部門コード)), ' ') ,',',
    RPAD(TBLA.ＪＡＮコード, 14, ' ') ,',',
    RPAD(TBLA.商品名, 200, ' ') ,',',
    TO_CHAR(TBLA.売上数量, 'S000000') ,',',
    TO_CHAR(TBLA.売上金額税込, 'S00000000') ,',',
    TO_CHAR(TBLA.粗利金額, 'S00000000') ,',',
    TO_CHAR(TBLA.値引割引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.アイテム値引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.アイテム割引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.自動値引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.自動割引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.会員値引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.会員割引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.クーポン値引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.クーポン割引金額, 'S00000000') ,',',
    TO_CHAR(TBLA.ＭＭ値引金額, 'S00000000') ,',',
    NVL(RPAD(TBLA.特売企画コード,LENGTH(TBLA.特売企画コード)), ' ') ,',',
    NVL(RPAD(TBLA.クーポン値引企画コード,LENGTH(TBLA.クーポン値引企画コード)), ' ') ,',',
    NVL(RPAD(TBLA.クーポン割引企画コード,LENGTH(TBLA.クーポン割引企画コード)), ' ') ,',',
    NVL(TBLA.特売チラシ掲載フラグ, '0'),',',
    RPAD(TBLA.会社部門コード,LENGTH(TBLA.会社部門コード)) ,',',
    RPAD(TBLA.計上部門コード,LENGTH(TBLA.計上部門コード)) ,',',
    RPAD(TBLA.分類コード,LENGTH(TBLA.分類コード)) ,',',
    TO_CHAR(TBLA.売上金額税抜, 'S00000000') ,',',
    RPAD(TBLA.ＥＣデータ種別,LENGTH(TBLA.ＥＣデータ種別)) ,',',
    RPAD(TBLA.免税フラグ,LENGTH(TBLA.免税フラグ)) ,',',
    NVL(RPAD(TBLA.国コード,LENGTH(TBLA.国コード)), ' ') ,',',
    NVL(RPAD(TBLA.性別,LENGTH(TBLA.性別)), ' ') ,',',
    NVL(RPAD(TBLA.生年月日,LENGTH(TBLA.生年月日)), '1900/01/01') ,',',
    NVL(RPAD(TBLA.旅券コード,LENGTH(TBLA.旅券コード)), ' ') ,',',
    RPAD(TBLA.来店宅配フラグ,LENGTH(TBLA.来店宅配フラグ)) ,',',
    NVL(CAST(TBLA.セルフメディケーションフラグ AS TEXT), RPAD(' ', 1)) ,',',
    NVL(CAST(TBLA.Ｄポイントカード番号 AS TEXT), RPAD(' ', 20)) ,',',
    NVL(CAST(TBLA.カード種別 AS TEXT), RPAD(' ', 3)) ,',',
    CASE WHEN TBLA.データ区分 = '1' 
         THEN NVL(TO_CHAR(TBLD.旧企業コード, 'FM0000'), RPAD(' ', 4)) 
         WHEN TBLA.データ区分 = '2' 
         THEN NVL(TO_CHAR(TBLD1.旧企業コード, 'FM0000'), RPAD(' ', 4)) 
         END ,',',
    CASE WHEN TBLA.データ区分 = '1' 
         THEN NVL(RPAD(TBLD.漢字店舗名称, 160, ' '), RPAD(' ', 160))
         WHEN TBLA.データ区分 = '2' 
         THEN NVL(RPAD(TBLD1.漢字店舗名称, 160, ' '), RPAD(' ', 160))
         END ,',', '')
FROM
      WS売上明細 TBLA
      LEFT JOIN PS店表示情報 TBLB ON TBLA.店舗コード = CAST(TBLB.店番号 AS TEXT)
      AND TBLB.開始年月日 <= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)
      AND TBLB.終了年月日 >= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)
      LEFT JOIN WS売上明細会員ＧＯＯＰＯＮ番号 TBLC ON TRIM(TBLA.会員ＩＤ) = TO_CHAR(TBLC.会員番号)
      LEFT JOIN PS店表示情報ＭＣＣ TBLD ON TBLA.会社コード = CAST(TBLD.会社コード AS TEXT) AND TBLA.店舗コード = CAST(TBLD.店番号 AS TEXT)
      AND TBLD.開始年月日 <= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)
      AND TBLD.終了年月日 >= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)
      LEFT JOIN PS店表示情報ＭＣＣ TBLD1 ON 2500 = TBLD1.会社コード AND TBLB.連携用店番号 = CAST(TBLD1.店番号 AS TEXT) 
      AND TBLD1.開始年月日 <= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)
      AND TBLD1.終了年月日 >= CAST(TO_CHAR(ADD_MONTHS(TO_DATE( CAST(:SBATDATE AS TEXT)) , - 1), 'YYYYMMDD') AS NUMERIC)       
;


\o
