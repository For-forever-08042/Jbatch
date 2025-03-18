\set SBATDATE :1                                                                                  --バッチ処理日
\set SBATDATE_1 :2                                                                                --バッチ処理日前日
\set SBATDATEYYYYMM :3                                                                            --バッチ処理日YYYYMM
\set SBATDATEYYYYMM_1 :4                                                                          --バッチ処理日前日YYYYMM
\set SFILENAME :5                                                                                 --出力ファイルファイル名
\set SYSHHMMSS :6                                                                                 --システム時刻

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./:SFILENAME.tsv

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--① MK-POS 連携分
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
             ,グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,会社コード
             ,店舗コード
             ,ポイント付与日付
             ,カテゴリコード
             ,有効期限フラグ
             ,有効期限
             ,付与ポイント数
FROM
     HSカテゴリ別ポイント実績:SBATDATEYYYYMM                                                        --バッチ処理日YYYYMM
WHERE
     システム年月日 = :SBATDATE                                                                     --バッチ処理日
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--②－１(1) MK-EC
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０１,
               有効期限フラグ０１,
               有効期限０１,
               SUM(付与ポイント数０１)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０１ AS カテゴリコード０１,
        NVL(HSC.有効期限フラグ０１,0) AS 有効期限フラグ０１,
        NVL(HSC.有効期限０１,0) AS 有効期限０１,
        HSC.付与ポイント数０１,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０１ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０１, 有効期限フラグ０１, 有効期限０１
;
commit ;

--０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０２,
               有効期限フラグ０２,
               有効期限０２,
               SUM(付与ポイント数０２)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０２ AS カテゴリコード０２,
        NVL(HSC.有効期限フラグ０２,0) AS 有効期限フラグ０２,
        NVL(HSC.有効期限０２,0) AS 有効期限０２,
        HSC.付与ポイント数０２,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０２ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０２, 有効期限フラグ０２, 有効期限０２
;
commit ;

--０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０３,
               有効期限フラグ０３,
               有効期限０３,
               SUM(付与ポイント数０３)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０３ AS カテゴリコード０３,
        NVL(HSC.有効期限フラグ０３,0) AS 有効期限フラグ０３,
        NVL(HSC.有効期限０３,0) AS 有効期限０３,
        HSC.付与ポイント数０３,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０３ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０３, 有効期限フラグ０３, 有効期限０３
;
commit ;

--０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０４,
               有効期限フラグ０４,
               有効期限０４,
               SUM(付与ポイント数０４)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０４ AS カテゴリコード０４,
        NVL(HSC.有効期限フラグ０４,0) AS 有効期限フラグ０４,
        NVL(HSC.有効期限０４,0) AS 有効期限０４,
        HSC.付与ポイント数０４,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０４ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０４, 有効期限フラグ０４, 有効期限０４
;
commit ;

--０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０５,
               有効期限フラグ０５,
               有効期限０５,
               SUM(付与ポイント数０５)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０５ AS カテゴリコード０５,
        NVL(HSC.有効期限フラグ０５,0) AS 有効期限フラグ０５,
        NVL(HSC.有効期限０５,0) AS 有効期限０５,
        HSC.付与ポイント数０５,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０５ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０５, 有効期限フラグ０５, 有効期限０５
;
commit ;

--０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０６,
               有効期限フラグ０６,
               有効期限０６,
               SUM(付与ポイント数０６)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０６ AS カテゴリコード０６,
        NVL(HSC.有効期限フラグ０６,0) AS 有効期限フラグ０６,
        NVL(HSC.有効期限０６,0) AS 有効期限０６,
        HSC.付与ポイント数０６,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０６ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０６, 有効期限フラグ０６, 有効期限０６
;
commit ;

--０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０７,
               有効期限フラグ０７,
               有効期限０７,
               SUM(付与ポイント数０７)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０７ AS カテゴリコード０７,
        NVL(HSC.有効期限フラグ０７,0) AS 有効期限フラグ０７,
        NVL(HSC.有効期限０７,0) AS 有効期限０７,
        HSC.付与ポイント数０７,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０７ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０７, 有効期限フラグ０７, 有効期限０７
;
commit ;

--０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０８,
               有効期限フラグ０８,
               有効期限０８,
               SUM(付与ポイント数０８)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０８ AS カテゴリコード０８,
        NVL(HSC.有効期限フラグ０８,0) AS 有効期限フラグ０８,
        NVL(HSC.有効期限０８,0) AS 有効期限０８,
        HSC.付与ポイント数０８,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０８ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０８, 有効期限フラグ０８, 有効期限０８
;
commit ;

--０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０９,
               有効期限フラグ０９,
               有効期限０９,
               SUM(付与ポイント数０９)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０９ AS カテゴリコード０９,
        NVL(HSC.有効期限フラグ０９,0) AS 有効期限フラグ０９,
        NVL(HSC.有効期限０９,0) AS 有効期限０９,
        HSC.付与ポイント数０９,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード０９ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０９, 有効期限フラグ０９, 有効期限０９
;
commit ;

--１０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード１０,
               有効期限フラグ１０,
               有効期限１０,
               SUM(付与ポイント数１０)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード１０ AS カテゴリコード１０,
        NVL(HSC.有効期限フラグ１０,0) AS 有効期限フラグ１０,
        NVL(HSC.有効期限１０,0) AS 有効期限１０,
        HSC.付与ポイント数１０,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE 
     AND A.カテゴリコード１０ IS NOT NULL
     AND A.会社コード = 1000  AND 店舗コード = 1034
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード１０, 有効期限フラグ１０, 有効期限１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--②－１(2) MK-POS再計算分
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０１,
               有効期限フラグ０１,
               有効期限０１,
               SUM(付与ポイント数０１)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０１ AS カテゴリコード０１,
        NVL(HSC.有効期限フラグ０１,0) AS 有効期限フラグ０１,
        NVL(HSC.有効期限０１,0) AS 有効期限０１,
        HSC.付与ポイント数０１,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０１ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０１, 有効期限フラグ０１, 有効期限０１
;
commit ;

--０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０２,
               有効期限フラグ０２,
               有効期限０２,
               SUM(付与ポイント数０２)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０２ AS カテゴリコード０２,
        NVL(HSC.有効期限フラグ０２,0) AS 有効期限フラグ０２,
        NVL(HSC.有効期限０２,0) AS 有効期限０２,
        HSC.付与ポイント数０２,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０２ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０２, 有効期限フラグ０２, 有効期限０２
;
commit ;

--０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０３,
               有効期限フラグ０３,
               有効期限０３,
               SUM(付与ポイント数０３)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０３ AS カテゴリコード０３,
        NVL(HSC.有効期限フラグ０３,0) AS 有効期限フラグ０３,
        NVL(HSC.有効期限０３,0) AS 有効期限０３,
        HSC.付与ポイント数０３,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０３ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０３, 有効期限フラグ０３, 有効期限０３
;
commit ;

--０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０４,
               有効期限フラグ０４,
               有効期限０４,
               SUM(付与ポイント数０４)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０４ AS カテゴリコード０４,
        NVL(HSC.有効期限フラグ０４,0) AS 有効期限フラグ０４,
        NVL(HSC.有効期限０４,0) AS 有効期限０４,
        HSC.付与ポイント数０４,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０４ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０４, 有効期限フラグ０４, 有効期限０４
;
commit ;

--０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０５,
               有効期限フラグ０５,
               有効期限０５,
               SUM(付与ポイント数０５)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０５ AS カテゴリコード０５,
        NVL(HSC.有効期限フラグ０５,0) AS 有効期限フラグ０５,
        NVL(HSC.有効期限０５,0) AS 有効期限０５,
        HSC.付与ポイント数０５,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０５ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０５, 有効期限フラグ０５, 有効期限０５
;
commit ;

--０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０６,
               有効期限フラグ０６,
               有効期限０６,
               SUM(付与ポイント数０６)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０６ AS カテゴリコード０６,
        NVL(HSC.有効期限フラグ０６,0) AS 有効期限フラグ０６,
        NVL(HSC.有効期限０６,0) AS 有効期限０６,
        HSC.付与ポイント数０６,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０６ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０６, 有効期限フラグ０６, 有効期限０６
;
commit ;

--０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０７,
               有効期限フラグ０７,
               有効期限０７,
               SUM(付与ポイント数０７)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０７ AS カテゴリコード０７,
        NVL(HSC.有効期限フラグ０７,0) AS 有効期限フラグ０７,
        NVL(HSC.有効期限０７,0) AS 有効期限０７,
        HSC.付与ポイント数０７,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０７ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０７, 有効期限フラグ０７, 有効期限０７
;
commit ;

--０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０８,
               有効期限フラグ０８,
               有効期限０８,
               SUM(付与ポイント数０８)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０８ AS カテゴリコード０８,
        NVL(HSC.有効期限フラグ０８,0) AS 有効期限フラグ０８,
        NVL(HSC.有効期限０８,0) AS 有効期限０８,
        HSC.付与ポイント数０８,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０８ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０８, 有効期限フラグ０８, 有効期限０８
;
commit ;

--０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０９,
               有効期限フラグ０９,
               有効期限０９,
               SUM(付与ポイント数０９)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０９ AS カテゴリコード０９,
        NVL(HSC.有効期限フラグ０９,0) AS 有効期限フラグ０９,
        NVL(HSC.有効期限０９,0) AS 有効期限０９,
        HSC.付与ポイント数０９,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード０９ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０９, 有効期限フラグ０９, 有効期限０９
;
commit ;

--１０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード１０,
               有効期限フラグ１０,
               有効期限１０,
               SUM(付与ポイント数１０)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード１０ AS カテゴリコード１０,
        NVL(HSC.有効期限フラグ１０,0) AS 有効期限フラグ１０,
        NVL(HSC.有効期限１０,0) AS 有効期限１０,
        HSC.付与ポイント数１０,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
       ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1 
     AND A.カテゴリコード１０ IS NOT NULL
     AND A.会社コード != 2500 AND NOT ( A.会社コード = 1000  AND 店舗コード = 1034)
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード１０, 有効期限フラグ１０, 有効期限１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--②-2 CF-POS、CF-EC
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０１,
               有効期限フラグ０１,
               有効期限０１,
               SUM(付与ポイント数０１)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０１ AS カテゴリコード０１,
        NVL(HSC.有効期限フラグ０１,0) AS 有効期限フラグ０１,
        NVL(HSC.有効期限０１,0) AS 有効期限０１,
        HSC.付与ポイント数０１,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０１ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０１, 有効期限フラグ０１, 有効期限０１
;
commit ;

--０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０２,
               有効期限フラグ０２,
               有効期限０２,
               SUM(付与ポイント数０２)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０２ AS カテゴリコード０２,
        NVL(HSC.有効期限フラグ０２,0) AS 有効期限フラグ０２,
        NVL(HSC.有効期限０２,0) AS 有効期限０２,
        HSC.付与ポイント数０２,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０２ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０２, 有効期限フラグ０２, 有効期限０２
;
commit ;

--０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０３,
               有効期限フラグ０３,
               有効期限０３,
               SUM(付与ポイント数０３)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        カテゴリコード０３ AS カテゴリコード０３,
        NVL(HSC.有効期限フラグ０３,0) AS 有効期限フラグ０３,
        NVL(HSC.有効期限０３,0) AS 有効期限０３,
        HSC.付与ポイント数０３,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０３ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０３, 有効期限フラグ０３, 有効期限０３
;
commit ;

--０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０４,
               有効期限フラグ０４,
               有効期限０４,
               SUM(付与ポイント数０４)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０４ AS カテゴリコード０４,
        NVL(HSC.有効期限フラグ０４,0) AS 有効期限フラグ０４,
        NVL(HSC.有効期限０４,0) AS 有効期限０４,
        HSC.付与ポイント数０４,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０４ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０４, 有効期限フラグ０４, 有効期限０４
;
commit ;

--０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０５,
               有効期限フラグ０５,
               有効期限０５,
               SUM(付与ポイント数０５)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        カテゴリコード０５ AS カテゴリコード０５,
        NVL(HSC.有効期限フラグ０５,0) AS 有効期限フラグ０５,
        NVL(HSC.有効期限０５,0) AS 有効期限０５,
        HSC.付与ポイント数０５,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０５ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０５, 有効期限フラグ０５, 有効期限０５
;
commit ;

--０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０６,
               有効期限フラグ０６,
               有効期限０６,
               SUM(付与ポイント数０６)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０６ AS カテゴリコード０６,
        NVL(HSC.有効期限フラグ０６,0) AS 有効期限フラグ０６,
        NVL(HSC.有効期限０６,0) AS 有効期限０６,
        HSC.付与ポイント数０６,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０６ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０６, 有効期限フラグ０６, 有効期限０６
;
commit ;

--０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０７,
               有効期限フラグ０７,
               有効期限０７,
               SUM(付与ポイント数０７)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０７ AS カテゴリコード０７,
        NVL(HSC.有効期限フラグ０７,0) AS 有効期限フラグ０７,
        NVL(HSC.有効期限０７,0) AS 有効期限０７,
        HSC.付与ポイント数０７,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０７ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０７, 有効期限フラグ０７, 有効期限０７
;
commit ;

--０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０８,
               有効期限フラグ０８,
               有効期限０８,
               SUM(付与ポイント数０８)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０８ AS カテゴリコード０８,
        NVL(HSC.有効期限フラグ０８,0) AS 有効期限フラグ０８,
        NVL(HSC.有効期限０８,0) AS 有効期限０８,
        HSC.付与ポイント数０８,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０８ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０８, 有効期限フラグ０８, 有効期限０８
;
commit ;

--０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード０９,
               有効期限フラグ０９,
               有効期限０９,
               SUM(付与ポイント数０９)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード０９ AS カテゴリコード０９,
        NVL(HSC.有効期限フラグ０９,0) AS 有効期限フラグ０９,
        NVL(HSC.有効期限０９,0) AS 有効期限０９,
        HSC.付与ポイント数０９,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード０９ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード０９, 有効期限フラグ０９, 有効期限０９
;
commit ;

--１０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
               :SBATDATE,
               (CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
                     WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
               END) AS グローバル会員フラグ,
               1,
               会社コード,
               店舗コード,
               登録年月日,
               カテゴリコード１０,
               有効期限フラグ１０,
               有効期限１０,
               SUM(付与ポイント数１０)
FROM
     (SELECT
        DISTINCT
        HSP.顧客番号,
        HSC.会社コード,
        HSC.店舗コード,
        HSC.登録年月日,
        HSC.カテゴリコード１０ AS カテゴリコード１０,
        NVL(HSC.有効期限フラグ１０,0) AS 有効期限フラグ１０,
        NVL(HSC.有効期限１０,0) AS 有効期限１０,
        HSC.付与ポイント数１０,
        HSC.システム年月日,
        HSC.処理通番,
        HSC.枝番,
        HSC.グローバル会員フラグ
      FROM
        HSカテゴリ別ポイント内訳:SBATDATEYYYYMM_1 HSC
        LEFT JOIN HSポイント日別情報:SBATDATEYYYYMM_1 HSP
               ON  HSC.取引番号 = HSP.取引番号
               AND HSC.システム年月日 = HSP.システム年月日
               AND HSC.会社コード = HSP.会社コードＭＣＣ
               AND HSC.店舗コード = HSP.店番号ＭＣＣ
               AND HSC.登録年月日 = HSP.登録年月日
               AND HSC.ターミナル番号 = HSP.ターミナル番号) A
LEFT JOIN MS顧客制度情報 B
ON A.顧客番号 = B.顧客番号
WHERE    A.システム年月日 = :SBATDATE_1
     AND A.カテゴリコード１０ IS NOT NULL
     AND A.会社コード = 2500
GROUP BY CASE WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'TWN' THEN 1
              WHEN A.グローバル会員フラグ = 1 AND グローバル会員国コード = 'HKG' THEN 2
              ELSE 0
         END,
         1, 会社コード, 店舗コード, 登録年月日,カテゴリコード１０, 有効期限フラグ１０, 有効期限１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--③運用分
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０１
             ,ポイント有効期限０１
             ,sum(付与ポイント０１)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０１ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０１ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０１
             ,ポイント有効期限０１
;
commit ;

--０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０２
             ,ポイント有効期限０２
             ,sum(付与ポイント０２)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０２ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０２ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０２
             ,ポイント有効期限０２
;
commit ;

--０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０３
             ,ポイント有効期限０３
             ,sum(付与ポイント０３)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０３ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０３ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０３
             ,ポイント有効期限０３
;
commit ;

--０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０４
             ,ポイント有効期限０４
             ,sum(付与ポイント０４)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０４ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０４ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０４
             ,ポイント有効期限０４
;
commit ;

--０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０５
             ,ポイント有効期限０５
             ,sum(付与ポイント０５)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０５ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０５ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０５
             ,ポイント有効期限０５
;
commit ;

--０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０６
             ,ポイント有効期限０６
             ,sum(付与ポイント０６)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０６ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０６ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０６
             ,ポイント有効期限０６
;
commit ;

--０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０７
             ,ポイント有効期限０７
             ,sum(付与ポイント０７)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０７ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０７ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０７
             ,ポイント有効期限０７
;
commit ;

--０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０８
             ,ポイント有効期限０８
             ,sum(付与ポイント０８)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０８ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０８ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０８
             ,ポイント有効期限０８
;
commit ;

--０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０９
             ,ポイント有効期限０９
             ,sum(付与ポイント０９)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０９ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分０９ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０９
             ,ポイント有効期限０９
;
commit ;

--１０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,2                                                                                     --運用
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分１０
             ,ポイント有効期限１０
             ,sum(付与ポイント１０)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分１０ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,0)
 AND U.購買区分１０ = 1                                                                             --(1:購買)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,2
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分１０
             ,ポイント有効期限１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--④還元分
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０１
             ,ポイント有効期限０１
             ,sum(利用ポイント０１)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０１ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０１ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０１
             ,ポイント有効期限０１
;
commit ;

--０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０２
             ,ポイント有効期限０２
             ,sum(利用ポイント０２)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０２ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０２ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０２
             ,ポイント有効期限０２
;
commit ;

--０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０３
             ,ポイント有効期限０３
             ,sum(利用ポイント０３)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０３ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０３ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０３
             ,ポイント有効期限０３
;
commit ;

--０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０４
             ,ポイント有効期限０４
             ,sum(利用ポイント０４)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０４ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０４ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０４
             ,ポイント有効期限０４
;
commit ;

--０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０５
             ,ポイント有効期限０５
             ,sum(利用ポイント０５)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０５ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０５ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０５
             ,ポイント有効期限０５
;
commit ;

--０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０６
             ,ポイント有効期限０６
             ,sum(利用ポイント０６)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０６ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０６ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０６
             ,ポイント有効期限０６
;
commit ;

--０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０７
             ,ポイント有効期限０７
             ,sum(利用ポイント０７)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０７ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０７ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０７
             ,ポイント有効期限０７
;
commit ;

--０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０８
             ,ポイント有効期限０８
             ,sum(利用ポイント０８)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０８ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０８ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０８
             ,ポイント有効期限０８
;
commit ;

--０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０９
             ,ポイント有効期限０９
             ,sum(利用ポイント０９)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０９ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分０９ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０９
             ,ポイント有効期限０９
;
commit ;

--１０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(                                              --バッチ処理日YYYYMM
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,3                                                                                     -- 還元
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分１０
             ,ポイント有効期限１０
             ,sum(利用ポイント１０)
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D                                                        --バッチ処理日YYYYMM
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U                                                        --バッチ処理日YYYYMM
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分１０ = 2                                                                         --(2:利用)
 AND MOD(D.理由コード, 100) NOT IN (6,7,9,77,78,90,91,92,93,94)
 AND U.購買区分１０ != 1                                                                            --(!1:購買 以外)
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,3
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分１０
             ,ポイント有効期限１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
--⑤プラスポイント
--０１
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０１                                                                  --０１～１０
             ,ポイント有効期限０１                                                                  --０１～１０
             ,sum(付与ポイント０１)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０１ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０１ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０１ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０１                                                                  --０１～１０
             ,ポイント有効期限０１                                                                  --０１～１０
;
commit ;

-- ０２
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０２                                                                  --０１～１０
             ,ポイント有効期限０２                                                                  --０１～１０
             ,sum(付与ポイント０２)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０２ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０２ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０２ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０２                                                                  --０１～１０
             ,ポイント有効期限０２                                                                  --０１～１０
;
commit ;

-- ０３
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０３                                                                  --０１～１０
             ,ポイント有効期限０３                                                                  --０１～１０
             ,sum(付与ポイント０３)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０３ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０３ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０３ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０３                                                                  --０１～１０
             ,ポイント有効期限０３                                                                  --０１～１０
;
commit ;

-- ０４
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０４                                                                  --０１～１０
             ,ポイント有効期限０４                                                                  --０１～１０
             ,sum(付与ポイント０４)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０４ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０４ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０４ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０４                                                                  --０１～１０
             ,ポイント有効期限０４                                                                  --０１～１０
;
commit ;

-- ０５
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０５                                                                  --０１～１０
             ,ポイント有効期限０５                                                                  --０１～１０
             ,sum(付与ポイント０５)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０５ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０５ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０５ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０５                                                                  --０１～１０
             ,ポイント有効期限０５                                                                  --０１～１０
;
commit ;

-- ０６
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０６                                                                  --０１～１０
             ,ポイント有効期限０６                                                                  --０１～１０
             ,sum(付与ポイント０６)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０６ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０６ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０６ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０６                                                                  --０１～１０
             ,ポイント有効期限０６                                                                  --０１～１０
;
commit ;

-- ０７
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０７                                                                  --０１～１０
             ,ポイント有効期限０７                                                                  --０１～１０
             ,sum(付与ポイント０７)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０７ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０７ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０７ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０７                                                                  --０１～１０
             ,ポイント有効期限０７                                                                  --０１～１０
;
commit ;

-- ０８
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０８                                                                  --０１～１０
             ,ポイント有効期限０８                                                                  --０１～１０
             ,sum(付与ポイント０８)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０８ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０８ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０８ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０８                                                                  --０１～１０
             ,ポイント有効期限０８                                                                  --０１～１０
;
commit ;

-- ０９
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分０９                                                                  --０１～１０
             ,ポイント有効期限０９                                                                  --０１～１０
             ,sum(付与ポイント０９)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分０９ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分０９ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別０９ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分０９                                                                  --０１～１０
             ,ポイント有効期限０９                                                                  --０１～１０
;
commit ;

-- １０
INSERT INTO HSカテゴリポイント情報:SBATDATEYYYYMM(
               システム年月日
              ,グローバル会員フラグ
              ,付与種別
              ,会社コード
              ,店舗コード
              ,ポイント付与日付
              ,カテゴリコード
              ,有効期限フラグ
              ,有効期限
              ,付与ポイント数
)
SELECT
              :SBATDATE
              ,(CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                     WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                     ELSE 0
                END) AS グローバル会員フラグ
             ,1                                                                                     --購買（POS・EC)
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,990
             ,通常期間限定区分１０                                                                  --０１～１０
             ,ポイント有効期限１０                                                                  --０１～１０
             ,sum(付与ポイント１０)                                                                 --０１～１０
FROM
     HSポイント日別情報:SBATDATEYYYYMM_1     D
     LEFT JOIN MS顧客制度情報 M ON D.顧客番号 = M.顧客番号
    ,HSポイント日別内訳情報:SBATDATEYYYYMM_1 U
WHERE
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND D.システム年月日 = :SBATDATE_1                                                                 --バッチ処理日前日
 AND U.付与利用区分１０ = 1                                                                         --(1:付与)
 AND MOD(D.理由コード, 100) = 0
 AND U.購買区分１０ = 1                                                                             --(1:購買)
 AND U.買上高ポイント種別１０ = 111
 AND NOT ( (D.会社コードＭＣＣ = 1000 AND D.店番号ＭＣＣ = 1034) OR (D.会社コードＭＣＣ = 2500) )
GROUP BY      CASE WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'TWN' THEN 1
                   WHEN D.会員企業コード=3060 AND M.グローバル会員国コード = 'HKG' THEN 2
                   ELSE 0
              END
             ,1
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.登録年月日
             ,通常期間限定区分１０                                                                  --０１～１０
             ,ポイント有効期限１０                                                                  --０１～１０
;
commit ;

----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------------------------
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
    C.有効期限)
FROM
   HSカテゴリポイント情報:SBATDATEYYYYMM C                                                          --バッチ処理日YYYYMM
   LEFT JOIN PS店舗変換マスタ P
   ON  C.会社コード = P.旧会社コード
   AND  C.店舗コード = P.旧店番号
  ,PS店表示情報ＭＣＣ S
WHERE C.会社コード = S.会社コード
 AND  C.店舗コード = S.店番号
 AND  S.開始年月日 <= :SBATDATE                                                                     --バッチ処理日
 AND  S.終了年月日 >= :SBATDATE                                                                     --バッチ処理日
 AND  C.システム年月日 = :SBATDATE                                                                  --バッチ処理日
 AND  C.グローバル会員フラグ = 0                                                                    --(0:非グローバル会員)
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
    C.システム年月日, C.ポイント付与日付, C.カテゴリコード, C.有効期限フラグ, C.有効期限
;

\o

