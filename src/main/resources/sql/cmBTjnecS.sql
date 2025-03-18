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
          ELSE TO_CHAR(TBLD.新会社コード, 'FM0000')
          END), RPAD(' ', 4)) ,',',
    NVL((CASE WHEN TBLA.データ区分 = '1'
          THEN TO_CHAR(CAST(TBLA.店舗コード AS NUMERIC), 'FM0000')
          ELSE TO_CHAR(TBLD.新店番号, 'FM0000')
          END), RPAD(' ', 4)) ,',',
    TO_CHAR(TBLA.レジＮＯ, 'FM0000') ,',',
    TO_CHAR(TBLA.レシートＮＯ, 'FM0000000000') ,',',
    TO_CHAR(TBLA.売上時刻, 'FM0000') ,',',
    NVL((CASE WHEN TBLC.ＧＯＯＰＯＮ番号 = 0 
          THEN RPAD(' ', 16)
          ELSE TO_CHAR(TBLC.ＧＯＯＰＯＮ番号)
          END), RPAD(' ', 16)) ,',',
    NVL(RPAD(TBLA.部門コード,LENGTH(TBLA.部門コード)), ' ') ,',',
    RPAD(TBLA.ＪＡＮコード,LENGTH(TBLA.ＪＡＮコード)) ,',"',
    RPAD(NULLIF(TRIM(TBLA.商品名), ''), 60, '　') ,'",',
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
    NVL(RPAD(TBLA.特売チラシ掲載フラグ,LENGTH(TBLA.特売チラシ掲載フラグ)), '0') ,',',
    RPAD(TBLA.会社部門コード,LENGTH(TBLA.会社部門コード)) ,',',
    RPAD(TBLA.計上部門コード,LENGTH(TBLA.計上部門コード)) ,',',
    RPAD(TBLA.分類コード,LENGTH(TBLA.分類コード)) ,',',
    TO_CHAR(TBLA.売上金額税抜, 'S00000000') ,',',
    '0' ,',',
    '9')
FROM
      WS購買履歴 TBLA
      LEFT JOIN WS会員ＧＯＯＰＯＮ番号 TBLC ON LTRIM(TRIM(TBLA.会員番号), '0') = TO_CHAR(TBLC.会員番号)
      LEFT JOIN PS店舗変換マスタ TBLD ON 2500 = TBLD.旧会社コード AND TBLA.店舗コード = CAST(TBLD.旧店番号 AS TEXT)
;

\o;
