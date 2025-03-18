
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_PROMOTION_SHOP_2.CSV

SELECT 
/*+ LEADING (B  A) */
CONCAT('"', LPAD(A.企画ＩＤ, 10, '0'), '"', CHR(9),
A.バージョン, CHR(9),
'"', A.組織レベル, '"', CHR(9),
'"', 
CASE WHEN A.組織レベル = 5 THEN LPAD(A.組織コード, 6, '0')
     WHEN A.組織レベル = 3 THEN LPAD(A.組織コード, 2, '0') 
ELSE LPAD(A.組織コード, 4, '0') END , '"')
FROM
 MSポイント付与組織O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND A.組織レベル <> 1 
AND A.組織レベル <> 6 
AND B.配信区分 = 0 
UNION ALL 
SELECT 
/*+ LEADING (B  A  S ) USE_NL( S ) */
CONCAT('"', LPAD(A.企画ＩＤ, 10, '0'), '"', CHR(9),
A.バージョン, CHR(9),
'"', A.組織レベル, '"', CHR(9),
'"', LPAD(S.会社コード, 4, '0'), '"')
FROM
 MSポイント付与組織O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND A.組織レベル = 1 
AND B.配信区分 = 0 
INNER JOIN
 (SELECT 会社コード, 旧企業コード 
  FROM PS店表示情報ＭＣＣ  WHERE 開始年月日       <= :1 AND 終了年月日       >= :1 
  GROUP BY 会社コード, 旧企業コード) S 
ON
    A.組織コード       =  S.旧企業コード 
UNION ALL 
SELECT 
/*+ LEADING (B  A  S ) USE_NL( S ) */
CONCAT('"', LPAD(A.企画ＩＤ, 10, '0'), '"', CHR(9),
A.バージョン, CHR(9),
'"', A.組織レベル, '"', CHR(9),
'"', LPAD(S.会社コード, 4, '0'), LPAD(S.店番号, 4, '0'), '"')
FROM
 MSポイント付与組織O A 
INNER JOIN
 WSポイント付与条件 B 
ON
    A.企画ＩＤ = B.企画ＩＤ 
AND A.バージョン = B.バージョン 
AND A.組織レベル = 6 
AND B.配信区分 = 0 
INNER JOIN
 PS店表示情報ＭＣＣ S 
ON
    case when length(to_char(A.組織コード))>=5 then substr(to_char(A.組織コード),1,length(to_char(A.組織コード))-4) else  null end    =  cast(S.旧企業コード as text) 
AND case when length(to_char(A.組織コード))>=5 then substr(to_char(A.組織コード),length(to_char(A.組織コード))-3,4) else  null end    =  cast(S.店番号 as text)
AND S.開始年月日       <= :1 
AND S.終了年月日       >= :1
;
\o

