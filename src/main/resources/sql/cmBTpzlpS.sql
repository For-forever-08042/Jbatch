\set LAST_MONTH :1                                                                                --前月YYYYMM
\set SFILENAME :2                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./:SFILENAME.csv


SELECT
    CONCAT(SUM(T.売上連動期間限定付与) ,',',                                                                                 /* 売上連動ポイント           */
    SUM(T.売上連動期間限定還元) ,',',
    SUM(T.売上連動期間限定失効) ,',',
    SUM(T.売上非連動期間限定付与) ,',',                                                                               /* 売上非連動ポイント         */
    SUM(T.売上非連動期間限定還元) ,',',
    SUM(T.売上非連動期間限定失効) ,',',
    SUM(T.株主期間限定付与) ,',',                                                                                     /* 株主ポイント               */
    SUM(T.株主期間限定還元) ,',',
    SUM(T.株主期間限定失効))
FROM(
    SELECT                                                                                                              /* 売上明細の NN=０１ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０１= 1  AND 付与利用区分０１= 1
                 THEN T1.付与ポイント０１ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０１= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０１= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０１= 2  AND 付与利用区分０１= 1
                 THEN T1.付与ポイント０１ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０１= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０１= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０１= 3  AND 付与利用区分０１= 1
                 THEN T1.付与ポイント０１ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０１= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０１= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０１= 2
                 THEN T1.利用ポイント０１ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０１ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０２ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０２= 1  AND 付与利用区分０２= 1
                 THEN T1.付与ポイント０２ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０２= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０２= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０２= 2  AND 付与利用区分０２= 1
                 THEN T1.付与ポイント０２ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０２= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０２= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０２= 3  AND 付与利用区分０２= 1
                 THEN T1.付与ポイント０２ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０２= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０２= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０２= 2
                 THEN T1.利用ポイント０２ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０２ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０３ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０３= 1  AND 付与利用区分０３= 1
                 THEN T1.付与ポイント０３ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０３= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０３= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０３= 2  AND 付与利用区分０３= 1
                 THEN T1.付与ポイント０３ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０３= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０３= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０３= 3  AND 付与利用区分０３= 1
                 THEN T1.付与ポイント０３ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０３= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０３= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０３= 2
                 THEN T1.利用ポイント０３ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０３ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０４ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０４= 1  AND 付与利用区分０４= 1
                 THEN T1.付与ポイント０４ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０４= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０４= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０４= 2  AND 付与利用区分０４= 1
                 THEN T1.付与ポイント０４ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０４= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０４= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０４= 3  AND 付与利用区分０４= 1
                 THEN T1.付与ポイント０４ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０４= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０４= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０４= 2
                 THEN T1.利用ポイント０４ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０４ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０５ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０５= 1  AND 付与利用区分０５= 1
                 THEN T1.付与ポイント０５ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０５= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０５= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０５= 2  AND 付与利用区分０５= 1
                 THEN T1.付与ポイント０５ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０５= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０５= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０５= 3  AND 付与利用区分０５= 1
                 THEN T1.付与ポイント０５ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０５= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０５= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０５= 2
                 THEN T1.利用ポイント０５ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０５ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０６ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０６= 1  AND 付与利用区分０６= 1
                 THEN T1.付与ポイント０６ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０６= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０６= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０６= 2  AND 付与利用区分０６= 1
                 THEN T1.付与ポイント０６ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０６= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０６= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０６= 3  AND 付与利用区分０６= 1
                 THEN T1.付与ポイント０６ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０６= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０６= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０６= 2
                 THEN T1.利用ポイント０６ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０６ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０７ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０７= 1  AND 付与利用区分０７= 1
                 THEN T1.付与ポイント０７ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０７= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０７= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０７= 2  AND 付与利用区分０７= 1
                 THEN T1.付与ポイント０７ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０７= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０７= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０７= 3  AND 付与利用区分０７= 1
                 THEN T1.付与ポイント０７ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０７= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０７= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０７= 2
                 THEN T1.利用ポイント０７ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０７ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０８ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０８= 1  AND 付与利用区分０８= 1
                 THEN T1.付与ポイント０８ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０８= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０８= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０８= 2  AND 付与利用区分０８= 1
                 THEN T1.付与ポイント０８ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０８= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０８= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０８= 3  AND 付与利用区分０８= 1
                 THEN T1.付与ポイント０８ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０８= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０８= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０８= 2
                 THEN T1.利用ポイント０８ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０８ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=０９ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分０９= 1  AND 付与利用区分０９= 1
                 THEN T1.付与ポイント０９ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分０９= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分０９= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分０９= 2  AND 付与利用区分０９= 1
                 THEN T1.付与ポイント０９ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分０９= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分０９= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分０９= 3  AND 付与利用区分０９= 1
                 THEN T1.付与ポイント０９ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分０９= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分０９= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分０９= 2
                 THEN T1.利用ポイント０９ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分０９ = 2                                                                                   /* 期間限定を対象             */
    UNION ALL
    SELECT                                                                                                              /* 売上明細の NN=１０ 単位    */
        NVL(SUM(                                                                                                        /* 売上連動（付与ポイント）   */
            CASE WHEN T1.購買区分１０= 1  AND 付与利用区分１０= 1
                 THEN T1.付与ポイント１０ ELSE NULL END)
         , 0) AS 売上連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上連動（利用ポイント）   */
            CASE WHEN T1.購買区分１０= 1 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 売上連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上連動（失効ポイント）   */
            CASE WHEN T1.購買区分１０= 1 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 売上連動期間限定失効,
        NVL(SUM(                                                                                                        /* 売上非連動（付与ポイント） */
            CASE WHEN T1.購買区分１０= 2  AND 付与利用区分１０= 1
                 THEN T1.付与ポイント１０ ELSE NULL END)
         , 0) AS 売上非連動期間限定付与,
        NVL(SUM(                                                                                                        /* 売上非連動（利用ポイント） */
            CASE WHEN T1.購買区分１０= 2 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 売上非連動期間限定還元,
        NVL(SUM(                                                                                                        /* 売上非連動（失効ポイント） */
            CASE WHEN T1.購買区分１０= 2 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 売上非連動期間限定失効,
        NVL(SUM(                                                                                                        /* 株主（付与ポイント）       */
            CASE WHEN T1.購買区分１０= 3  AND 付与利用区分１０= 1
                 THEN T1.付与ポイント１０ ELSE NULL END)
         , 0) AS 株主期間限定付与,
        NVL(SUM(                                                                                                         /* 株主（利用ポイント）      */
            CASE WHEN T1.購買区分１０= 3 AND MOD(T2.理由コード,100) NOT IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 株主期間限定還元,
        NVL(SUM(                                                                                                         /* 株主（失効ポイント）      */
            CASE WHEN T1.購買区分１０= 3 AND MOD(T2.理由コード,100) IN (92,94) AND 付与利用区分１０= 2
                 THEN T1.利用ポイント１０ ELSE NULL END)
         , 0) AS 株主期間限定失効
    FROM HSポイント日別内訳情報:LAST_MONTH T1
    INNER JOIN HSポイント日別情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND T1.顧客番号 = T2.顧客番号
        AND T1.処理通番 = T2.処理通番
    WHERE T1.通常期間限定区分１０ = 2                                                                                   /* 期間限定を対象             */
) T
;

\o

