\set SBATDATE :1                                                                                  --バッチ処理日
\set SFILENAME :2                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
\o ./:SFILENAME

--データ抽出
SELECT CONCAT(LPAD(TO_CHAR(家族ＩＤ),10,'0') ,',',
       LPAD(TO_CHAR(ＧＯＯＰＯＮ番号),16,'0') ,',',
       (CASE WHEN 申込区分=1 THEN '0' WHEN 申込区分=2 THEN '1' END) ,',',
       (CASE WHEN 家族登録日 >= 19000101 THEN TO_CHAR(TO_DATE(CAST(家族登録日 AS TEXT),'YYYYMMDD'),'YYYY/MM/DD') ELSE TO_CHAR(TO_DATE(CAST(家族作成日 AS TEXT),'YYYYMMDD'),'YYYY/MM/DD') END) ,',',
       (CASE WHEN 申込区分=1 THEN '1990/01/01' WHEN 申込区分=2 THEN TO_CHAR(TO_DATE(CAST(最終更新日 AS TEXT),'YYYYMMDD'),'YYYY/MM/DD') END) ,',',
       TO_CHAR(TO_DATE(CAST(家族作成日 AS TEXT),'YYYYMMDD'),'YYYY/MM/DD') ,',',
       TO_CHAR(最終更新日時,'YYYY/MM/DD HH24:MI:SS'))
FROM
(
SELECT T.家族ＩＤ,
       T.ＧＯＯＰＯＮ番号,
       T.申込区分,
       T.家族登録日,
       T.最終更新日,
       T.家族作成日,
       T.最終更新日時,
       ROW_NUMBER() OVER (PARTITION BY T.家族ＩＤ,T.ＧＯＯＰＯＮ番号 ORDER BY 家族履歴通番 DESC) RN
       FROM HS家族情報履歴 T
       WHERE T.申込区分 <>  0
         AND T.システム年月日 = :SBATDATE
)
WHERE RN=1
;

\o
