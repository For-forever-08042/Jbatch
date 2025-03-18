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
 CONCAT(TO_CHAR(TO_DATE(TO_CHAR(A.システム年月日), 'YYYY/MM/DD'), 'YYYY/MM/DD') ,',',
 '0031' , A.店番号ＭＣＣ ,',',
 NVL(SUM(A.ポイント付与件数取引計),0) ,',',
 NVL(SUM(A.付与ポイント数取引計),0) ,',',
 NVL(SUM(A.ポイント付与売上金額取引計),0) ,',',
 NVL(SUM(A.還元ポイント取引計),0) ,',',
 NVL(ROUND(SUM(A.還元ポイント取引計)/500,0),0) ,',',
 NVL(SUM(A.調剤P付与件数取引計),0) ,',',
 NVL(SUM(A.調剤付与ポイント数取引計),0) ,',',
 NVL(SUM(A.調剤P付与売上金額取引計),0) ,',',
 NVL(SUM(A.手入力P付与件数取引計),0) ,',',
 NVL(SUM(A.手入力ポイント数取引計),0))
FROM
    (SELECT
    T.システム年月日,
    T.顧客番号,
    T.処理通番,
    T.店番号ＭＣＣ,
    CASE WHEN SUM(T.ポイント付与件数明細計) >= 1 THEN 1 ELSE 0 END AS ポイント付与件数取引計,
    SUM(T.付与ポイント数明細計) AS 付与ポイント数取引計,
    SUM(T.ポイント付与売上金額明細計) AS ポイント付与売上金額取引計,
    SUM(T.還元ポイント明細計) AS 還元ポイント取引計,
    CASE WHEN SUM(T.調剤P付与件数明細計)>=1 THEN 1 ELSE 0 END  AS 調剤P付与件数取引計,
    SUM(T.調剤付与ポイント数明細計) AS 調剤付与ポイント数取引計,
    SUM(T.調剤P付与売上金額明細計) AS 調剤P付与売上金額取引計,
    CASE WHEN SUM(T.手入力P付与件数明細計) >= 1 THEN 1 ELSE 0 END AS 手入力P付与件数取引計,
    SUM(T.手入力ポイント数明細計) AS 手入力ポイント数取引計
    FROM
        (SELECT                                                                                                                                  --０１
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０１ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０１ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (121,123))
                        THEN T2.付与ポイント０１ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０１ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (121,123))
                        THEN T2.ポイント対象金額０１ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０１) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (118,119)
                        THEN T2.付与ポイント０１ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (118,119)
                        THEN T2.ポイント対象金額０１ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０１ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０１ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０１ IN (151,153))
                         THEN T2.付与ポイント０１ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０１ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０２
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０２ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０２ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (121,123))
                        THEN T2.付与ポイント０２ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０２ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (121,123))
                        THEN T2.ポイント対象金額０２ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０２) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (118,119)
                        THEN T2.付与ポイント０２ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (118,119)
                        THEN T2.ポイント対象金額０２ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０２ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０２ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０２ IN (151,153))
                         THEN T2.付与ポイント０２ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０２ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０３
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０３ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０３ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (121,123))
                        THEN T2.付与ポイント０３ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０３ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (121,123))
                        THEN T2.ポイント対象金額０３ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０３) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (118,119)
                        THEN T2.付与ポイント０３ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (118,119)
                        THEN T2.ポイント対象金額０３ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０３ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０３ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０３ IN (151,153))
                         THEN T2.付与ポイント０３ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０３ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０４
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０４ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０４ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (121,123))
                        THEN T2.付与ポイント０４ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０４ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (121,123))
                        THEN T2.ポイント対象金額０４ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０４) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (118,119)
                        THEN T2.付与ポイント０４ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (118,119)
                        THEN T2.ポイント対象金額０４ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０４ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０４ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０４ IN (151,153))
                         THEN T2.付与ポイント０４ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０４ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０５
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０５ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０５ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (121,123))
                        THEN T2.付与ポイント０５ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０５ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (121,123))
                        THEN T2.ポイント対象金額０５ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０５) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (118,119)
                        THEN T2.付与ポイント０５ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (118,119)
                        THEN T2.ポイント対象金額０５ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０５ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０５ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０５ IN (151,153))
                         THEN T2.付与ポイント０５ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０５ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０６
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０６ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０６ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (121,123))
                        THEN T2.付与ポイント０６ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０６ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (121,123))
                        THEN T2.ポイント対象金額０６ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０６) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (118,119)
                        THEN T2.付与ポイント０６ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (118,119)
                        THEN T2.ポイント対象金額０６ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０６ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０６ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０６ IN (151,153))
                         THEN T2.付与ポイント０６ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０６ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０７
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０７ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０７ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (121,123))
                        THEN T2.付与ポイント０７ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０７ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (121,123))
                        THEN T2.ポイント対象金額０７ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０７) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (118,119)
                        THEN T2.付与ポイント０７ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (118,119)
                        THEN T2.ポイント対象金額０７ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０７ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０７ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０７ IN (151,153))
                         THEN T2.付与ポイント０７ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０７ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０８
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０８ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０８ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (121,123))
                        THEN T2.付与ポイント０８ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０８ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (121,123))
                        THEN T2.ポイント対象金額０８ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０８) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (118,119)
                        THEN T2.付与ポイント０８ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (118,119)
                        THEN T2.ポイント対象金額０８ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０８ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０８ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０８ IN (151,153))
                         THEN T2.付与ポイント０８ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０８ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --０９
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０９ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０９ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (121,123))
                        THEN T2.付与ポイント０９ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別０９ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (121,123))
                        THEN T2.ポイント対象金額０９ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント０９) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (118,119)
                        THEN T2.付与ポイント０９ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (118,119)
                        THEN T2.ポイント対象金額０９ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０９ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別０９ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別０９ IN (151,153))
                         THEN T2.付与ポイント０９ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別０９ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        UNION ALL
        SELECT                                                                                                                                   --１０
        T1.システム年月日,
        T1.顧客番号,
        T1.処理通番,
        T1.店番号ＭＣＣ,
             COUNT(CASE WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別１０ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (121,123))
                        THEN 1 ELSE NULL END) AS ポイント付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別１０ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (121,123))
                        THEN T2.付与ポイント１０ ELSE NULL END) AS 付与ポイント数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (0) AND T2.買上高ポイント種別１０ IN (110,111,112,116,125,126,127,128,129,201))
                        OR   (T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (121,123))
                        THEN T2.ポイント対象金額１０ ELSE NULL END) AS ポイント付与売上金額明細計,
             SUM(T2.利用ポイント１０) AS 還元ポイント明細計,
             COUNT(CASE WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (118,119)
                        THEN 1 ELSE NULL END) AS 調剤P付与件数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (118,119)
                        THEN T2.付与ポイント１０ ELSE NULL END) AS 調剤付与ポイント数明細計,
             SUM(CASE   WHEN T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (118,119)
                        THEN T2.ポイント対象金額１０ ELSE NULL END) AS 調剤P付与売上金額明細計,
             COUNT(CASE WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別１０ IN (151,153))
                        THEN 1 ELSE NULL END ) AS 手入力P付与件数明細計,
             SUM(CASE   WHEN (T1.理由コード IN (5) AND T2.買上高ポイント種別１０ IN (114,115))
                        OR   (MOD(T1.理由コード,100) IN (11) AND T2.買上高ポイント種別１０ IN (151,153))
                         THEN T2.付与ポイント１０ ELSE NULL END ) AS 手入力ポイント数明細計
        FROM HSポイント日別情報:LAST_MONTH  T1
        INNER JOIN HSポイント日別内訳情報:LAST_MONTH T2
        ON T1.システム年月日 = T2.システム年月日
        AND  T1.顧客番号 = T2.顧客番号
        AND  T1.処理通番 = T2.処理通番
        WHERE T1.会社コードＭＣＣ = 2030
        and T2.買上高ポイント種別１０ in (110,111,112,116,125,126,127,128,129,201,123,121,118,119,114,115,151,153)
        GROUP BY T1.システム年月日,T1.顧客番号,T1.処理通番,T1.店番号ＭＣＣ
        ) T
    GROUP BY T.システム年月日,T.顧客番号,T.処理通番,T.店番号ＭＣＣ
) A
GROUP BY A.システム年月日, A.店番号ＭＣＣ
ORDER BY A.システム年月日, A.店番号ＭＣＣ
;

\o

