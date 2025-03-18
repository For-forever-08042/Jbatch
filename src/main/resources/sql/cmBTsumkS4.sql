\set SQL_DATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./KK00000004




SELECT
 CONCAT('KK00000004',
 TRIM(TO_CHAR(ROW_NUMBER() OVER(), '0000000000')),  
 COL_B,
 COL_C, 
 COL_D,
 COL_E,
 COL_F,
 '                  04204150000JPY',
 COL_G,
 COL_H)
FROM (
   SELECT
      LPAD(NVL(P.新会社コード,W.会社コード), 4, '0') AS COL_B
     ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -1)), 'YYYYMMDD') AS COL_C
     ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -1)), 'YYYYMMDD') AS COL_D
     ,CASE
         WHEN S.仮想店舗フラグ = 1 AND S.ＳＡＰ連携フラグ = 1 THEN SUBSTR(S.ＳＡＰ連携店舗番号,1,4)
         ELSE LPAD(TO_CHAR(NVL(P.新会社コード,W.会社コード)), 4, '0')
      END AS COL_E
     ,CASE
         WHEN S.仮想店舗フラグ = 1 AND S.ＳＡＰ連携フラグ = 1 THEN SUBSTR(S.ＳＡＰ連携店舗番号,5,4)
         ELSE LPAD(TO_CHAR(NVL(P.新店番号,W.店舗コード)), 4, '0')
      END AS COL_F
     ,CASE
         WHEN SUM(利用ポイント数) < 0 THEN '-'
         ELSE '+'
      END AS COL_G
     ,TRIM(TO_CHAR(ABS(SUM(利用ポイント数)), '0000000000000')) AS COL_H
   FROM
          WSＳＡＰ付与還元実績抽出 W
   JOIN
          PS店表示情報ＭＣＣ S
   ON
        W.会社コード       = S.会社コード
    AND W.店舗コード       = S.店番号
    AND :SQL_DATE BETWEEN S.開始年月日 AND S.終了年月日
   LEFT OUTER JOIN
          PS店舗変換マスタ P
   ON
        W.会社コード       = P.旧会社コード
    AND W.店舗コード       = P.旧店番号
   WHERE
        MOD(W.理由コード,100)       not in ( 6, 7, 77 ,78, 90, 91, 92, 93, 94 )
    AND W.通常期間限定区分  = 2               -- 期間限定ポイント
    AND W.購買区分         != 1               -- 購買
    AND ( (S.仮想店舗フラグ = 1 AND S.ＳＡＰ連携フラグ=1 )
        OR NVL(S.仮想店舗フラグ,0) = 0
        )
    AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)   -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
   GROUP BY
      LPAD(NVL(P.新会社コード,W.会社コード), 4, '0')
     ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -1)), 'YYYYMMDD')
     ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -1)), 'YYYYMMDD')
     ,CASE
         WHEN S.仮想店舗フラグ = 1 AND S.ＳＡＰ連携フラグ = 1 THEN SUBSTR(S.ＳＡＰ連携店舗番号,1,4)
         ELSE LPAD(TO_CHAR(NVL(P.新会社コード,W.会社コード)), 4, '0')
      END
     ,CASE
         WHEN S.仮想店舗フラグ = 1 AND S.ＳＡＰ連携フラグ = 1 THEN SUBSTR(S.ＳＡＰ連携店舗番号,5,4)
         ELSE LPAD(TO_CHAR(NVL(P.新店番号,W.店舗コード)), 4, '0')
      END
)
WHERE COL_H != '0'
;
\o
