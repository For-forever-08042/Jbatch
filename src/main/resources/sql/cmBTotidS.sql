\set BATDATE :1
\set OUTFILE :2

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./:OUTFILE

SELECT
       CONCAT(ＧＯＯＰＯＮ番号
    , ','
    , 外部認証種別
    , ','
    , NULLIF(TRIM(外部認証ＩＤ),'')
    , ','
    , 削除フラグ
    , ','
    , TO_CHAR(TO_DATE(TO_CHAR(NVL(登録日,最終更新日)),'yyyyMMdd'),'yyyy/MM/dd')
    , ','
    , TO_CHAR(COALESCE(最終更新日時, TO_DATE(CONCAT(最終更新日 , '000000'), 'YYYYMMDDHH24MISS')), 'YYYY/MM/DD HH24:MI:SS'))
FROM
(
SELECT
     TBLA.ＧＯＯＰＯＮ番号
    ,TBLB.外部認証種別
    ,TBLB.外部認証ＩＤ
    ,TBLB.削除フラグ
    ,TBLB.登録日
    ,TBLB.最終更新日
    ,TBLB.最終更新日時
    ,ROW_NUMBER() OVER (PARTITION BY TBLA.ＧＯＯＰＯＮ番号 ORDER BY TBLB.最終更新日時 DESC, TBLB.削除フラグ, TBLB.登録日 DESC) as G_ROW
FROM
     MSカード情報    TBLA
    ,MS外部認証情報  TBLB
WHERE
        TBLB.最終更新日      =  :BATDATE
AND     TBLB.会員番号        =  TBLA.会員番号
AND     TBLB.サービス種別    =  TBLA.サービス種別
AND     TBLB.外部認証種別    =  'L'
)
WHERE G_ROW = 1
;

\o
