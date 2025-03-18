\set SBATDATE :1                                                                                  --バッチ処理日
\set SBATDATEYYYYMM :2                                                                            --バッチ処理日YYYYMM
\set SFILENAME :3                                                                                 --出力ファイルファイル名
\set SYSHHMMSS :4                                                                                 --システム時刻

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./:SFILENAME.tsv

--データ抽出
SELECT
    CONCAT(CASE
      WHEN S.仮想店舗フラグ = 1 AND S.ＤＷＨ連携時変換店舗コード IS NOT NULL THEN SUBSTR(LPAD(S.ＤＷＨ連携時変換店舗コード,8,'0'),1,4)
      ELSE LPAD(NVL(P.新会社コード,C.会社コード),4,'0')
    END,CHR(9),
    CASE
      WHEN S.仮想店舗フラグ = 1 AND S.ＤＷＨ連携時変換店舗コード IS NOT NULL THEN SUBSTR(LPAD(S.ＤＷＨ連携時変換店舗コード,8,'0'),5,4)
      ELSE LPAD(NVL(P.新店番号,C.店舗コード),4,'0')
    END,CHR(9),
    C.システム年月日,CHR(9),
    LPAD(:SYSHHMMSS,6,'0'),CHR(9),
    C.ポイント付与日付,CHR(9),
    C.カテゴリコード,CHR(9),
    SUM(C.付与ポイント数),CHR(9),
    C.有効期限フラグ,CHR(9),
    C.有効期限,CHR(9),
    CASE
      WHEN C.グローバル会員フラグ  = 1 THEN 'TWN'
      WHEN C.グローバル会員フラグ  = 2 THEN 'HKG'
    END)
FROM
   HSカテゴリポイント情報:SBATDATEYYYYMM C                                                          --バッチ処理日YYYYMM
   LEFT JOIN PS店舗変換マスタ P ON C.会社コード = P.旧会社コード AND  C.店舗コード = P.旧店番号
  ,PS店表示情報ＭＣＣ S
WHERE C.会社コード = S.会社コード
 AND  C.店舗コード = S.店番号
 AND  S.開始年月日 <= :SBATDATE                                                                      --バッチ処理日
 AND  S.終了年月日 >= :SBATDATE                                                                      --バッチ処理日
 AND  C.システム年月日 = :SBATDATE                                                                  --バッチ処理日
 AND  C.グローバル会員フラグ IN (1,2)                                                               --(グローバル会員 1:TWN 台湾  2:HKG 香港)
 AND  C.付与ポイント数 <> 0
 AND  ((S.仮想店舗フラグ = 1 AND S.ＤＷＨ連携時変換店舗コード IS NOT NULL)
       OR NVL(S.仮想店舗フラグ,0) = 0
      )
 AND  NVL(P.新店舗形態区分,NVL(S.店舗形態区分,0)) NOT IN (31,32,33)                                                      --31:薬粧ダミー店舗  32:HCダミー店舗  33:SMダミー店舗を除く
GROUP BY 
    CASE
      WHEN S.仮想店舗フラグ = 1 AND S.ＤＷＨ連携時変換店舗コード IS NOT NULL THEN SUBSTR(LPAD(S.ＤＷＨ連携時変換店舗コード,8,'0'),1,4)
      ELSE LPAD(NVL(P.新会社コード,C.会社コード),4,'0')
    END,
    CASE
      WHEN S.仮想店舗フラグ = 1 AND S.ＤＷＨ連携時変換店舗コード IS NOT NULL THEN SUBSTR(LPAD(S.ＤＷＨ連携時変換店舗コード,8,'0'),5,4)
      ELSE LPAD(NVL(P.新店番号,C.店舗コード),4,'0')
    END,
    CASE
      WHEN C.グローバル会員フラグ  = 1 THEN 'TWN'
      WHEN C.グローバル会員フラグ  = 2 THEN 'HKG'
    END,
    C.システム年月日, C.ポイント付与日付, C.カテゴリコード, C.有効期限フラグ, C.有効期限
;

\o

