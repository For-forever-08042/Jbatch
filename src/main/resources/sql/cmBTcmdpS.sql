\set BAT_DATE :1
\set FNAME :2

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

\o ./:FNAME

SELECT
    CONCAT(ＧＯＯＰＯＮ番号 , ',' , 広告配信許諾フラグ , ',' , 登録日 , ',' , 更新日)
FROM
(
    SELECT
         NVL(TBLA.ＧＯＯＰＯＮ番号,TBLC.旧ＧＯＯＰＯＮ番号) AS ＧＯＯＰＯＮ番号 
        ,CASE WHEN TBLB.顧客ステータス <> 1 THEN 0 
              WHEN TBLB.広告配信許諾フラグ更新日時 IS NOT NULL THEN NVL(TBLB.広告配信許諾フラグ, 0) 
              ELSE 1 END AS 広告配信許諾フラグ
        ,CASE WHEN TBLB.広告配信許諾フラグ更新日時 IS NOT NULL AND TBLB.広告配信許諾フラグ登録日 > 19000101
              THEN TO_CHAR(COALESCE(TO_DATE(CAST(TBLB.広告配信許諾フラグ登録日 AS TEXT),'YYYYMMDD'),TBLB.広告配信許諾フラグ更新日時),'yyyy/MM/dd') 
              WHEN TBLA.発行年月日>19000101 THEN TO_CHAR(TO_DATE(CAST ( TBLA.発行年月日 AS TEXT),'YYYYMMDD'),'yyyy/MM/dd')
              ELSE TO_CHAR(TO_DATE(CAST (:BAT_DATE AS TEXT),'YYYYMMDD'),'yyyy/MM/dd') END AS 登録日
        ,TO_CHAR(COALESCE(TBLB.広告配信許諾フラグ更新日時, TO_DATE(CAST (TBLB.最終更新日 AS TEXT),'YYYYMMDD')), 'YYYY/MM/DD HH24:MI:SS') AS 更新日
        ,ROW_NUMBER() OVER (PARTITION BY TBLA.顧客番号 ORDER BY CASE WHEN TBLA.発行年月日 < 19000101 THEN 99991231 ELSE TBLA.発行年月日 END, TBLC.最終更新日 DESC) as RN
    FROM
        MSカード情報 TBLA
        LEFT JOIN MS顧客制度情報 TBLB
        ON TBLA.顧客番号 = TBLB.顧客番号
        LEFT JOIN HSカード変更情報 TBLC
        ON TBLA.顧客番号 = TBLC.旧顧客番号 
        AND TBLC.旧ＧＯＯＰＯＮ番号 IS NOT NULL
        AND TBLC.最終更新日 >= :BAT_DATE
    WHERE
        TBLB.最終更新日 >= :BAT_DATE
)
WHERE RN=1
;

\o
