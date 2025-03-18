--&1：バッチ処理日前日
\set SDATE 'to_number(concat(to_char(add_months(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),-1),''yyyymm''),''01000000''))'
\set NENTSUKI :2
\set NENTSUKI_LM :3

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./KKHY0010.dat
SELECT CONCAT(SHORINEN , ',' , SHORITSUKI , ',' , STORE , ',' , CATEGORY , ',' , KANJO , ',' , SUM_POINT_ALL )
FROM (
SELECT SHORINEN,
       SHORITSUKI,
       STORE,
       CATEGORY,
       KANJO,
       SUM(SUM_POINT) AS SUM_POINT_ALL
FROM (
SELECT SHORINEN,
       SHORITSUKI,
       STORE,
       CASE
           WHEN CATEGORY IN ('C991', 'C993') THEN 'C990'
           ELSE CATEGORY
       END AS CATEGORY,
       KANJO,
       SUM_POINT
FROM (
--①-1 POS付与 － 直営
   SELECT
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
       ,SUM(POINT) AS SUM_POINT
   FROM
   (
     ----- 前月 -----
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                  AS SHORITSUKI
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END                                                             AS STORE
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))                          AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                             AS KANJO
      ,SUM(C.付与ポイント数)                                           AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
       LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S

     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
          OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
             AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
             )
          )
      AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
      AND S.会社コード <> 7367
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   UNION ALL
     ----- 当月 -----
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                  AS SHORITSUKI
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END                                                             AS STORE
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))                          AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                             AS KANJO
      ,SUM(C.付与ポイント数)                                           AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
      AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
      AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
      AND S.会社コード <> 7367
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   )
   GROUP BY
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
UNION ALL
--①-2 POS付与 － FC 店舗合計
--②-2 運用付与 － FC 店舗合計
   SELECT
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
       ,SUM(POINT) AS SUM_POINT
   FROM
   (
     ----- 前月 ----- ①-2 POS付与 － FC 店舗合計
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
      ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END , 'WS')                                                                          AS STORE
      ,'C989'                                                                AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                                   AS KANJO
      ,SUM(C.付与ポイント数)                                                 AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
          OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
             AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
             )
          )
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END , 'WS')
      ,CATEGORY   
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   UNION ALL
     ----- 当月 ----- ①-2 POS付与 － FC 店舗合計
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
      ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END , 'WS')                                                           AS STORE
      ,'C989'                                                                AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                                   AS KANJO
      ,SUM(C.付与ポイント数)                                                 AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
      AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END , 'WS')
      ,CATEGORY   
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   UNION ALL
     ----- 前月 ----- ②-2 運用付与 － FC 店舗合計
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
      ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000') , 'WS')               AS STORE
      ,'C989'                                                                AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                                   AS KANJO
      ,SUM(C.付与ポイント数)                                                 AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
       LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
          OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
             AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
             )
          )
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
      AND NVL(S.仮想店舗フラグ,0) = 0
      AND C.付与種別         =  2                                                                  -- 運用
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000') , 'WS')
      ,CATEGORY   
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
    UNION ALL
     ----- 前月 ----- ②-2 運用付与 － FC 店舗合計
    SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
      ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000') , 'WS')               AS STORE
      ,'C989'                                                                AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                                   AS KANJO
      ,SUM(C.付与ポイント数)                                                 AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
      AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
      AND NVL(S.仮想店舗フラグ,0) = 0
      AND C.付与種別         =  2                                                                  -- 運用
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000') , 'WS')
      ,CATEGORY  
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   )
   GROUP BY
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
UNION ALL
--①-3 POS付与 － FC
   SELECT
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
       ,SUM(POINT) AS SUM_POINT
   FROM
   (
     ----- 前月 -----
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                  AS SHORITSUKI
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END                                                             AS STORE
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))                          AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                             AS KANJO
      ,SUM(C.付与ポイント数)                                           AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
          OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
             AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
             )
          )
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not直営
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   UNION ALL
     ----- 当月 -----
     SELECT
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                AS SHORINEN
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                  AS SHORITSUKI
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END                                                             AS STORE
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))                          AS CATEGORY 
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END                                                             AS KANJO
      ,SUM(C.付与ポイント数)                                           AS POINT
     FROM
       HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
     LEFT JOIN PS店舗変換マスタ P
        ON  C.会社コード       =  P.旧会社コード
        AND C.店舗コード       =  P.旧店番号
      ,PS店表示情報ＭＣＣ S
     WHERE
          C.会社コード       =  S.会社コード
      AND C.店舗コード       =  S.店番号
      AND S.開始年月日       <= :1
      AND S.終了年月日       >= :1
      AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
      AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
      AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not直営
      AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
            OR NVL(S.仮想店舗フラグ,0) = 0
           )
      AND C.付与種別         IN  (1,5)                                                                  -- 購買
      AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
     GROUP BY
       TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
      ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
      ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
       END
      ,CONCAT('C',TO_CHAR(C.カテゴリコード,'FM000'))
      ,CASE C.有効期限フラグ 
          WHEN 1 THEN '62340W'                                           --通常
          ELSE        '62340Y'                                           --期間限定
       END
   )
   GROUP BY
        SHORINEN
       ,SHORITSUKI
       ,STORE
       ,CATEGORY 
       ,KANJO
UNION ALL
--②-1 運用付与 － 直営
     ----- 前月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))        AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END                                                                   AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
  LEFT JOIN PS店舗変換マスタ P
     ON  C.会社コード       =  P.旧会社コード
     AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
       OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
          AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
          )
       )
   AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
   AND S.会社コード <> 7367
   AND C.付与種別         =  2                                                                  -- 運用
   AND NVL(S.仮想店舗フラグ,0) = 0
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000')) 
   ,CATEGORY   
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END
UNION ALL
     ----- 当月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))        AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END                                                                   AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
   AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
   AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
   AND S.会社コード <> 7367
   AND C.付与種別         =  2                                                                  -- 運用
   AND NVL(S.仮想店舗フラグ,0) = 0
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
   ,CATEGORY  
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END
UNION ALL
--②-3 運用付与 － FC
     ----- 前月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))        AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END                                                                   AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
  LEFT JOIN PS店舗変換マスタ P
     ON  C.会社コード       =  P.旧会社コード
     AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
       OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
          AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
          )
       )
   AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
   AND C.付与種別         =  2                                                                  -- 運用
   AND NVL(S.仮想店舗フラグ,0) = 0
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000')) 
   ,CATEGORY   
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END
UNION ALL
     ----- 当月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))        AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END                                                                   AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
   AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
   AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
   AND C.付与種別         =  2                                                                  -- 運用
   AND NVL(S.仮想店舗フラグ,0) = 0
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000')) 
   ,CATEGORY   
   ,CASE C.有効期限フラグ 
       WHEN 1 THEN '62340W'                                           --通常
       ELSE        '62340Y'                                           --期間限定
    END
UNION ALL
--③-1 還元 － 直営
     ----- 前月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END                                                                   AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,'62340X'                                                              AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
       OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
          AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
          )
       )
   AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
   AND S.会社コード <> 7367
   AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
         OR NVL(S.仮想店舗フラグ,0) = 0
        )
   AND C.付与種別         =  3                                                                  -- 還元
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END
   ,CATEGORY   
   ,KANJO
UNION ALL
     ----- 当月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END                                                                   AS STORE
   ,'C990'                                                                AS CATEGORY 
   ,'62340X'                                                              AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
   AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
   AND SUBSTR(LPAD(S.会社コード,4,'0'),1,1)     <>  '5'                                         -- 直営
   AND S.会社コード <> 7367
   AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
         OR NVL(S.仮想店舗フラグ,0) = 0
        )
   AND C.付与種別         =  3                                                                  -- 還元
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
            ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END
   ,CATEGORY   
   ,KANJO
UNION ALL
--③-2 還元 － FC
     ----- 前月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END , 'WS')                                                        AS STORE
   ,'C989'                                                                AS CATEGORY 
   ,'62340X'                                                              AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI_LM C   -- 前月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND ( C.ポイント付与日付 >= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 以降
       OR (   C.システム年月日   >  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+4,'YYYYMMDD'))  -- 前月/4 より後
          AND C.ポイント付与日付 <  TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-2))+1,'YYYYMMDD'))  -- 前月/1 より前
          )
       )
   AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
   AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
         OR NVL(S.仮想店舗フラグ,0) = 0
        )
   AND C.付与種別         =  3                                                                  -- 還元
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
     END , 'WS')
   ,CATEGORY  
   ,KANJO
UNION ALL
     ----- 当月 -----
  SELECT
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')                      AS SHORINEN
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')                        AS SHORITSUKI
   ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
    END , 'WS')                                                        AS STORE
   ,'C989'                                                                AS CATEGORY 
   ,'62340X'                                                              AS KANJO
   ,SUM(C.付与ポイント数)                                                 AS SUM_POINT
  FROM
    HSカテゴリポイント情報:NENTSUKI C   -- 当月テーブル
  LEFT JOIN PS店舗変換マスタ P
    ON  C.会社コード       =  P.旧会社コード
    AND C.店舗コード       =  P.旧店番号
   ,PS店表示情報ＭＣＣ S
  WHERE
       C.会社コード       =  S.会社コード
   AND C.店舗コード       =  S.店番号
   AND S.開始年月日       <= :1
   AND S.終了年月日       >= :1
   AND C.システム年月日   <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1))+4,'YYYYMMDD'))  -- 当月/4  以前
   AND C.ポイント付与日付 <= TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYYMMDD'))    -- 前月/末 以前
   AND (SUBSTR(LPAD(S.会社コード,4,'0'),1,1) = '5' or S.会社コード = 7367)                      -- Not 直営
   AND  ((S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ = 1)
         OR NVL(S.仮想店舗フラグ,0) = 0
        )
   AND C.付与種別         =  3                                                                  -- 還元
   AND NVL(S.店舗形態区分, 0)     NOT IN (31,32,33)                                                     -- 31:薬粧ﾀﾞﾐｰ店舗 32:HCﾀﾞﾐｰ店舗 33:SMﾀﾞﾐｰ店舗 を除く
  GROUP BY
    TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'YYYY')
   ,TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:1 AS TEXT),'YYYYMMDD'),-1)),'MM')
   ,CONCAT(CASE WHEN S.仮想店舗フラグ = 1 AND S.ＨＹＰ連携フラグ=1 THEN S.ＨＹＰ連携時変換店舗コード
         ELSE CONCAT(TO_CHAR(NVL(P.新会社コード,C.会社コード),'FM0000'),TO_CHAR(NVL(P.新店番号,C.店舗コード),'FM0000'))
     END , 'WS')
   ,CATEGORY   
   ,KANJO
)
)
GROUP BY
     SHORINEN
    ,SHORITSUKI
    ,STORE
    ,CATEGORY
    ,KANJO
) WHERE SUM_POINT_ALL <> 0
;


\o
