\set SFILENAME :1                                                                                 --出力ファイルファイル名


\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./:SFILENAME.csv

SELECT
    CONCAT(NVL(
        A2.クレジット全数 - ( 
            A2.クレジット甲信越販売 + A2.クレジットトウブドラッグ + A2.クレジット中島 + A2.クレジットミドリ + A2
            .クレジットミドリ社員カード + A2.クレジット杉浦薬品 + A2.クレジットダルマ薬局
        )
        , 0
    ) ,',',                                                                                                                     /* クレジット                                         */
    NVL(A2.クレジット甲信越販売, 0) ,',',                                                                                       /* クレジット(甲信越販売)                             */
    NVL(A2.クレジットトウブドラッグ, 0) ,',',                                                                                   /* クレジット(トウブドラッグ)                         */
    NVL(A2.クレジット中島, 0) ,',',                                                                                             /* クレジット(中島)                                   */
    NVL(A2.クレジットミドリ, 0) ,',',                                                                                           /* クレジット(ミドリ)                                 */
    NVL(A2.クレジットミドリ社員カード, 0) ,',',                                                                                 /* クレジット(ミドリ:社員カード)                      */
    NVL(A2.クレジット杉浦薬品, 0) ,',',                                                                                         /* クレジット(杉浦薬品)                               */
    NVL(A2.クレジットダルマ薬局, 0) ,',',                                                                                       /* クレジット(ダルマ薬局)                             */
    NVL(A1.一般, 0) ,',',                                                                                                       /* 一般                                               */
    NVL(A1.ユアースポーツ, 0) ,',',                                                                                             /* ユアースポーツ                                     */
    NVL(A1.マツモトキヨシ薬品, 0) ,',',                                                                                         /* マツモトキヨシ薬品                                 */
    NVL(A1.甲信越販売, 0) ,',',                                                                                                 /* 甲信越販売                                         */
    NVL(A1.トウブドラッグ, 0) ,',',                                                                                             /* トウブドラッグ                                     */
    NVL(A1.ＨアンドＢ, 0) ,',',                                                                                                 /* Ｈ＆Ｂ                                             */
    NVL(A1.中島ファミリー薬局, 0) ,',',                                                                                         /* 中島ファミリー薬局                                 */
    NVL(A1.ミドリ薬品, 0) ,',',                                                                                                 /* ミドリ薬品                                         */
    NVL(A1.ミドリ薬品ゴールド, 0) ,',',                                                                                         /* ミドリ薬品（ゴールド）                             */
    NVL(A1.ぱぱす, 0) ,',',                                                                                                     /* ぱぱす                                             */
    NVL(A1.杉浦薬品, 0) ,',',                                                                                                   /* 杉浦薬品                                           */
    NVL(A1.ダルマ薬局, 0) ,',',                                                                                                 /* ダルマ薬局                                         */
    NVL(A1.ラブドラッグス, 0))                                                                                                     /* ラブドラッグス                                     */
FROM
    (                                                                                                                             /* MSカード情報とPS会員番号体系のINNER JOINテーブルA1 */
        SELECT
            T1.カードステータス AS "キー",                                                                                        /* キー情報                                           */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 1  AND T2.カード種類 = 2  THEN 1 ELSE NULL END) AS "一般",                    /* 現金カード                                         */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 1  AND T2.カード種類 = 4  THEN 1 ELSE NULL END) AS "ユアースポーツ",          /* 現金カード（ユアースポーツカード（青））           */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 1  AND T2.カード種類 = 3  THEN 1 ELSE NULL END) AS "マツモトキヨシ薬品",      /* 現金カード(薬品カード（緑）)                       */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 2  AND T2.カード種類 = 5  THEN 1 ELSE NULL END) AS "甲信越販売",              /* 現金カード（甲信越販売）                           */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 3  AND T2.カード種類 = 7  THEN 1 ELSE NULL END) AS "トウブドラッグ",          /* 現金カード(トウブドラッグ)                         */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 4  AND T2.カード種類 = 10 THEN 1 ELSE NULL END) AS "ＨアンドＢ",              /* 現金カード（Ｈ＆Ｂ）                               */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 5  AND T2.カード種類 = 11 THEN 1 ELSE NULL END) AS "中島ファミリー薬局",      /* 現金カード(中島ファミリー薬局)                     */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 6  AND T2.カード種類 = 13 THEN 1 ELSE NULL END) AS "ミドリ薬品",              /* 現金カード（ミドリ薬品）                           */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 6  AND T2.カード種類 = 14 THEN 1 ELSE NULL END) AS "ミドリ薬品ゴールド",      /* 現金カード(ミドリ薬品ゴールド)                     */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 7  AND T2.カード種類 = 17 THEN 1 ELSE NULL END) AS "ぱぱす",                  /* 現金カード（ぱぱす）                               */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 8  AND T2.カード種類 = 18 THEN 1 ELSE NULL END) AS "杉浦薬品",                /* 現金カード(杉浦薬品)                               */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 9  AND T2.カード種類 = 20 THEN 1 ELSE NULL END) AS "ダルマ薬局",              /* 現金カード（ダルマ薬局）                           */
            COUNT(CASE WHEN T2.ハウスカードタイプ = 10 AND T2.カード種類 = 22 THEN 1 ELSE NULL END) AS "ラブドラッグス"           /* 現 金カード(ラブドラッグス)                        */
        FROM
            MSカード情報 T1
            INNER JOIN PS会員番号体系 T2
                ON T1.サービス種別 = T2.サービス種別
                AND T1.会員番号 >= T2.会員番号開始
                AND T1.会員番号 <= T2.会員番号終了
        WHERE
            T1.サービス種別 = 1
            AND T1.カードステータス = 1
            AND T1.理由コード IN (2000, 2020)
        GROUP BY
            T1.カードステータス
    ) A1
    INNER JOIN
    (                                                                                                                             /* MSカード情報とMS顧客制度情報のINNER JOINテーブルA2 */
        SELECT
            T1.カードステータス AS "キー",                                                                                        /* キー情報                                           */
            COUNT(T1.サービス種別) AS "クレジット全数",                                                                           /* クレジット全数                                     */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56424', '66424') THEN 1 ELSE NULL END) AS "クレジット甲信越販売",                /* クレジット(甲信越販売)                             */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56426', '66426') THEN 1 ELSE NULL END) AS "クレジットトウブドラッグ",            /* クレジット(トウブドラッグ)                         */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56632', '66632') THEN 1 ELSE NULL END) AS "クレジット中島",                      /* クレジット(中島)                                   */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56633', '66633', '56634', '66634') THEN 1 ELSE NULL END) AS "クレジットミドリ",  /* クレジット(ミドリ)                                 */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56635', '66635') THEN 1 ELSE NULL END) AS "クレジットミドリ社員カード",          /* クレジット(ミドリ:社員カード)                      */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56703', '66703') THEN 1 ELSE NULL END) AS "クレジット杉浦薬品",                  /* クレジット(杉浦薬品)                               */
            COUNT(CASE WHEN T2.契約管理種別 IN ('56702', '66702') THEN 1 ELSE NULL END) AS "クレジットダルマ薬局"                 /* クレジット(ダルマ薬局)                             */
        FROM
            MSカード情報 T1
            INNER JOIN MS顧客制度情報 T2
                ON T1.顧客番号 = T2.顧客番号
        WHERE
            T1.サービス種別 = 4
            AND T1.カードステータス = 1
            AND T1.理由コード IN (2000, 2020)
        GROUP BY
            T1.カードステータス
    ) A2 
        ON A1.キー = A2.キー
;

\o;

