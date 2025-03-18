\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

/** To SPSS **/
\o ./KKMI0090.csv
SELECT CONCAT(グーポン番号, ',' ,変換前グーポン番号, ',' ,削除フラグ, ',' ,MAX(更新日)) FROM
(
/** ①入会情報 **************************************************/
SELECT LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS グーポン番号 
      ,LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS 変換前グーポン番号 
      ,0 AS 削除フラグ
      ,LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
  FROM (
        SELECT  /*+ INDEX(C IXMSCARD01) */ 
            C.ＧＯＯＰＯＮ番号, ROW_NUMBER() OVER ( PARTITION BY C.ＧＯＯＰＯＮ番号 ORDER BY 最終更新日 desc ) G_ROW
        FROM
            MSカード情報 C
        WHERE C.最終更新日=:SDATE
          AND C.カードステータス != 8
          AND C.顧客番号 != 0
          AND NOT EXISTS ( SELECT /*+ INDEX(H111 IXHSCARDHS03)*/ 1
                             FROM HSカード変更情報 H111
                            WHERE C.ＧＯＯＰＯＮ番号  = H111.ＧＯＯＰＯＮ番号
                              AND NVL(H111.統合切替解除年月日,0) = 0
                              AND H111.最終更新日<:SDATE     )
          AND NOT EXISTS ( SELECT /*+ INDEX(H112 IXHSCARDHS06)*/ 1
                             FROM HSカード変更情報 H112
                            WHERE C.ＧＯＯＰＯＮ番号  = H112.旧ＧＯＯＰＯＮ番号
                              AND NVL(H112.統合切替解除年月日,0) = 0
                              AND H112.最終更新日<:SDATE     )
    ) 
WHERE G_ROW = 1
/** ②退会    (退会時にアクティブであった会員番号) **************/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS グーポン番号
      ,LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS 変換前グーポン番号 
      ,1 AS 削除フラグ 
      ,LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM ( 
        SELECT  C2.ＧＯＯＰＯＮ番号
        FROM
            MSカード情報   C2
          , MS顧客制度情報 S2
        WHERE C2.顧客番号 = S2.顧客番号
          AND S2.最終更新日 = :SDATE
          AND S2.顧客ステータス = 9
)
GROUP BY ＧＯＯＰＯＮ番号
/** ③退会    (退会時に過去統合で不要となった会員番号) **********/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS グーポン番号 
      ,LPAD(NVL(TO_CHAR(旧ＧＯＯＰＯＮ番号),' '),16,' ') AS 変換前グーポン番号 
      ,1 AS 削除フラグ 
      ,LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM ( 
        SELECT 
            ＧＯＯＰＯＮ番号
           ,旧ＧＯＯＰＯＮ番号
           ,:SDATE AS 更新日 
           ,ROW_NUMBER() OVER ( 
                PARTITION BY ＧＯＯＰＯＮ番号, 旧ＧＯＯＰＯＮ番号
                ORDER BY 最終更新日時 DESC ) G_ROW 
        FROM 
            (
---------------------------------------------------------------------------------------------
                SELECT
                        V311.ＧＯＯＰＯＮ番号
                       ,H311.旧ＧＯＯＰＯＮ番号
                       ,H311.最終更新日時
                FROM 
                    HSカード変更情報 H311
                   , 
                    ( 
                        SELECT 
                            C.ＧＯＯＰＯＮ番号 
                           ,C.会員番号 
                           ,C.企業コード 
                        FROM 
                            MSカード情報   C 
                           ,MS顧客制度情報 S 
                        WHERE C.顧客番号 = S.顧客番号 
                         AND  S.最終更新日 = :SDATE 
                         AND  S.顧客ステータス = 9 
                    ) V311
                WHERE V311.会員番号 = H311.旧会員番号 
                 AND  V311.企業コード = H311.旧企業コード 
                 AND  NVL(H311.統合切替解除年月日,0) = 0
                 AND  H311.統合採用会員番号 IS NULL 
                 AND  H311.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
                UNION
---------------------------------------------------------------------------------------------
                SELECT
                        V312.ＧＯＯＰＯＮ番号
                       ,H312.旧ＧＯＯＰＯＮ番号
                       ,H312.最終更新日時
                FROM 
                    HSカード変更情報 H312
                   , 
                    ( 
                        SELECT 
                            C.ＧＯＯＰＯＮ番号 
                           ,C.会員番号 
                           ,C.企業コード
                        FROM 
                            MSカード情報   C 
                           ,MS顧客制度情報 S 
                        WHERE C.顧客番号 = S.顧客番号 
                         AND  S.最終更新日 = :SDATE 
                         AND  S.顧客ステータス = 9 
                    ) V312
                WHERE V312.会員番号 = H312.会員番号 
                 AND  V312.企業コード = H312.企業コード 
                 AND  NVL(H312.統合切替解除年月日,0) = 0
                 AND  H312.統合採用会員番号 IS NOT NULL 
                 AND  H312.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
            ) V31
    ) 
WHERE 
     G_ROW = 1
/** ④統合    (ＧＯＯＰＯＮ番号紐付けが変わったレコード) ********/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS グーポン番号
     , LPAD(NVL(TO_CHAR(旧ＧＯＯＰＯＮ番号),' '),16,' ') AS 変換前グーポン番号
     , 0 AS 削除フラグ
     , LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM ( 
        SELECT 
            ＧＯＯＰＯＮ番号 
           ,旧ＧＯＯＰＯＮ番号
           ,:SDATE AS 更新日 
           ,ROW_NUMBER() OVER ( 
                PARTITION BY ＧＯＯＰＯＮ番号, 旧ＧＯＯＰＯＮ番号
                ORDER BY 最終更新日時 DESC ) G_ROW 
        FROM 
            (
---------------------------------------------------------------------------------------------
                SELECT
                        V411.ＧＯＯＰＯＮ番号 
                       ,H411.旧ＧＯＯＰＯＮ番号
                       ,H411.最終更新日時
                FROM
                    HSカード変更情報 H411 
                   , 
                    ( 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE 
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NULL
                         AND NOT EXISTS ( SELECT 1 
                                          FROM HSカード変更情報 H421 
                                          WHERE H.ＧＯＯＰＯＮ番号 = H421.旧ＧＯＯＰＯＮ番号 )
                        UNION 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号         = H.統合採用会員番号 
                         AND C.企業コード        = H.旧企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NOT NULL
                         AND NOT EXISTS ( SELECT 1 
                                          FROM HSカード変更情報 H422 
                                          WHERE H.ＧＯＯＰＯＮ番号 = H422.旧ＧＯＯＰＯＮ番号 )
                    ) V411 
                WHERE V411.統合元会番号   = H411.旧会員番号
                 AND  V411.統合元企業コード   = H411.旧企業コード
                 AND  NVL(H411.統合切替解除年月日,0) = 0
                 AND  H411.統合採用会員番号 IS NULL
                 AND  H411.最終更新日 <= :SDATE
                 AND  H411.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
                UNION
---------------------------------------------------------------------------------------------
                SELECT
                        V412.ＧＯＯＰＯＮ番号 
                       ,H412.旧ＧＯＯＰＯＮ番号
                       ,H412.最終更新日時
                FROM
                    HSカード変更情報 H412 
                   , 
                    ( 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE 
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NULL
                         AND NOT EXISTS ( SELECT 1 
                                          FROM HSカード変更情報 H423 
                                          WHERE H.ＧＯＯＰＯＮ番号 = H423.旧ＧＯＯＰＯＮ番号 )
                        UNION 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号 
                         AND C.企業コード        = H.旧企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NOT NULL
                         AND NOT EXISTS ( SELECT 1 
                                          FROM HSカード変更情報 H424 
                                          WHERE H.ＧＯＯＰＯＮ番号 = H424.旧ＧＯＯＰＯＮ番号 )
                    ) V412 
                WHERE V412.統合元会番号   = H412.会員番号
                 AND  V412.統合元企業コード = H412.企業コード
                 AND  NVL(H412.統合切替解除年月日,0) = 0
                 AND  H412.統合採用会員番号 IS NOT NULL
                 AND  H412.最終更新日 <= :SDATE
                 AND  H412.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
            ) V41
    ) 
WHERE 
     G_ROW = 1
/** ⑤統合    (ＧＯＯＰＯＮ番号紐付けが不要になったレコード) ****/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(削除Ｇ番号),' '),16,' ') AS グーポン番号
     , LPAD(NVL(TO_CHAR(削除旧Ｇ番号),' '),16,' ') AS 変換前グーポン番号
     , 1 AS 削除フラグ
     , LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM 
    ( 
        SELECT 
            PG   AS 削除Ｇ番号
           ,CG   AS 削除旧Ｇ番号
           ,:SDATE AS 更新日 
           ,ROW_NUMBER() OVER ( 
                PARTITION BY PG, CG
                ORDER BY 最終更新日時 DESC ) G_ROW 
        FROM 
            (
---------------------------------------------------------------------------------------------
                SELECT
                        V511.旧ＧＯＯＰＯＮ番号 PG
                       ,H511.旧ＧＯＯＰＯＮ番号 CG
                       ,H511.最終更新日時
                FROM
                    HSカード変更情報 H511 
                   , 
                    ( 
                        SELECT 
                           H.旧ＧＯＯＰＯＮ番号
                          ,H.旧会員番号 as 統合元会番号
                          ,H.旧企業コード as 統合元企業コード
                          ,H.最終更新日時
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号
                         AND C.企業コード        = H.企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NULL
                        UNION
                        SELECT
                            H.旧ＧＯＯＰＯＮ番号
                           ,H.統合不採用会員番号 AS 統合元会番号
                           ,H.企業コード as 統合元企業コード
                           ,H.最終更新日時
                        FROM
                            MSカード情報   C
                           ,HSカード変更情報 H
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号
                         AND C.企業コード        = H.旧企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NOT NULL
                    ) V511
                WHERE V511.統合元会番号 = H511.旧会員番号 
                  AND V511.統合元企業コード = H511.旧企業コード 
                  AND NVL(H511.統合切替解除年月日,0) = 0
                  AND H511.統合採用会員番号 IS NULL
                  AND H511.最終更新日 <= :SDATE
                  AND H511.旧ＧＯＯＰＯＮ番号 IS NOT NULL
                  AND V511.最終更新日時 >= H511.最終更新日時
---------------------------------------------------------------------------------------------
                UNION
---------------------------------------------------------------------------------------------
                SELECT
                        V512.旧ＧＯＯＰＯＮ番号 PG
                       ,H512.旧ＧＯＯＰＯＮ番号 CG
                       ,H512.最終更新日時
                FROM
                    HSカード変更情報 H512 
                   , 
                    ( 
                        SELECT 
                           H.旧ＧＯＯＰＯＮ番号
                          ,H.旧会員番号 AS 統合元会番号
                          ,H.旧企業コード as 統合元企業コード
                          ,H.最終更新日時
                       FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号
                         AND C.企業コード        = H.企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NULL
                        UNION
                        SELECT
                            H.旧ＧＯＯＰＯＮ番号
                           ,H.統合不採用会員番号 AS 統合元会番号
                           ,H.企業コード as 統合元企業コード
                           ,H.最終更新日時
                        FROM
                            MSカード情報   C
                           ,HSカード変更情報 H
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号
                         AND C.企業コード        = H.旧企業コード
                         AND C.顧客番号          = S.顧客番号
                         AND S.顧客ステータス != 9
                         AND H.最終更新日 = :SDATE
                         AND NVL(H.統合切替解除年月日,0) = 0
                         AND H.統合採用会員番号 IS NOT NULL
                    ) V512
                WHERE V512.統合元会番号 = H512.会員番号 
                  AND V512.統合元企業コード = H512.企業コード 
                  AND NVL(H512.統合切替解除年月日,0) = 0
                  AND H512.統合採用会員番号 IS NOT NULL
                  AND H512.最終更新日 <= :SDATE
                  AND H512.旧ＧＯＯＰＯＮ番号 IS NOT NULL
                  AND V512.最終更新日時 >= H512.最終更新日時
---------------------------------------------------------------------------------------------
            ) V51
    ) 
WHERE
     G_ROW = 1
/** ⑥統合解除(解除により、統合時に利用で出力した分を削除) ******/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(ＧＯＯＰＯＮ番号),' '),16,' ') AS グーポン番号
     , LPAD(NVL(TO_CHAR(旧ＧＯＯＰＯＮ番号),' '),16,' ') AS 変換前グーポン番号
     , 1 AS 削除フラグ
     , LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM 
    ( 
        SELECT 
            ＧＯＯＰＯＮ番号 
           ,旧ＧＯＯＰＯＮ番号
           ,:SDATE AS 更新日 
           ,ROW_NUMBER() OVER ( 
                PARTITION BY ＧＯＯＰＯＮ番号, 旧ＧＯＯＰＯＮ番号
                ORDER BY 最終更新日時 DESC ) G_ROW 
        FROM 
            (
---------------------------------------------------------------------------------------------
                SELECT
                        V611.ＧＯＯＰＯＮ番号 
                       ,H611.旧ＧＯＯＰＯＮ番号
                       ,H611.最終更新日時
                FROM
                    HSカード変更情報 H611 
                   , 
                    ( 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NULL
                        UNION 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号 
                         AND  C.企業コード        = H.旧企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NOT NULL
                    ) V611 
                WHERE V611.統合元会番号 =  H611.旧会員番号
                 AND  V611.統合元企業コード =  H611.旧企業コード
                 AND  H611.統合採用会員番号 IS NULL
                 AND  H611.最終更新日 <= :SDATE
                 AND  H611.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
                UNION
---------------------------------------------------------------------------------------------
                SELECT
                        V612.ＧＯＯＰＯＮ番号 
                       ,H612.旧ＧＯＯＰＯＮ番号
                       ,H612.最終更新日時
                FROM
                    HSカード変更情報 H612 
                   , 
                    ( 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NULL
                        UNION 
                        SELECT 
                            H.ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号 
                         AND  C.企業コード        = H.旧企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NOT NULL
                    ) V612 
                WHERE V612.統合元会番号 = H612.会員番号
                 AND  V612.統合元企業コード = H612.企業コード
                 AND  H612.統合採用会員番号 IS NOT NULL
                 AND  H612.最終更新日 <= :SDATE
                 AND  H612.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
            ) V61
    ) 
WHERE 
     G_ROW = 1
/** ⑦統合解除(解除により、統合時に削除で出力した分の復帰) ******/
UNION ALL
SELECT LPAD(NVL(TO_CHAR(PG),' '),16,' ') AS グーポン番号
     , LPAD(NVL(TO_CHAR(CG),' '),16,' ') AS 変換前グーポン番号
     , 0 AS 削除フラグ
     , LPAD(TO_CHAR(:SDATE),8,' ') AS 更新日
FROM 
    ( 
        SELECT 
            PG
           ,CG
           ,:SDATE AS 更新日 
           ,ROW_NUMBER() OVER ( 
                PARTITION BY PG  , CG
                ORDER BY 最終更新日時 DESC ) G_ROW 
        FROM 
            (
---------------------------------------------------------------------------------------------
                SELECT
                        V711.旧ＧＯＯＰＯＮ番号 AS PG
                       ,H711.旧ＧＯＯＰＯＮ番号 AS CG
                       ,H711.最終更新日時
                FROM
                    HSカード変更情報 H711 
                   , 
                    ( 
                        SELECT 
                            H.旧ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NULL
                        UNION 
                        SELECT 
                            H.旧ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号 
                         AND  C.企業コード        = H.旧企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NOT NULL
                    ) V711 
                WHERE V711.統合元会番号 =  H711.旧会員番号
                 AND  V711.統合元企業コード =  H711.旧企業コード
                 AND  H711.統合採用会員番号 IS NULL
                 AND  H711.最終更新日 <= :SDATE
                 AND  H711.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
                UNION
---------------------------------------------------------------------------------------------
                SELECT
                        V712.旧ＧＯＯＰＯＮ番号 AS PG
                       ,H712.旧ＧＯＯＰＯＮ番号 AS CG
                       ,H712.最終更新日時
                FROM
                    HSカード変更情報 H712 
                   , 
                    ( 
                        SELECT 
                            H.旧ＧＯＯＰＯＮ番号 
                           ,H.旧会員番号 AS 統合元会番号
                           ,H.旧企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.会員番号 
                         AND  C.企業コード        = H.企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NULL
                        UNION 
                        SELECT 
                            H.旧ＧＯＯＰＯＮ番号 
                           ,H.会員番号 AS 統合元会番号
                           ,H.企業コード AS 統合元企業コード
                        FROM 
                            MSカード情報   C 
                           ,HSカード変更情報 H 
                           ,MS顧客制度情報 S
                        WHERE C.会員番号          = H.統合採用会員番号 
                         AND  C.企業コード        = H.旧企業コード
                         AND  C.顧客番号          = S.顧客番号
                         AND  S.顧客ステータス != 9
                         AND  H.統合切替解除年月日 = :SDATE 
                         AND  H.統合切替解除年月日 != H.統合日付
                         AND  H.統合採用会員番号 IS NOT NULL
                    ) V712 
                WHERE V712.統合元会番号 = H712.会員番号
                 AND  V712.統合元企業コード = H712.企業コード
                 AND  H712.統合採用会員番号 IS NOT NULL
                 AND  H712.最終更新日 <= :SDATE
                 AND  H712.旧ＧＯＯＰＯＮ番号 IS NOT NULL
---------------------------------------------------------------------------------------------
            ) V71
    ) 
WHERE
     G_ROW = 1
 )
 GROUP BY グーポン番号, 変換前グーポン番号, 削除フラグ
;
\o
