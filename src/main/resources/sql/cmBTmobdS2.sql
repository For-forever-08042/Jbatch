\set SDATE :1

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';



select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./KKMI0160.csv


TRUNCATE TABLE WSスマホアプリ登録状況;
INSERT INTO WSスマホアプリ登録状況 (顧客番号,プッシュ通知許可フラグ)
    SELECT 
         W.顧客番号
        ,K.プッシュ通知許可フラグ
    FROM
        WM顧客番号 W
       ,MM顧客情報 K
    WHERE W.顧客番号 = K.顧客番号
;
commit;


SELECT
    CONCAT(外部認証ＩＤ ,',',
    ＧＯＯＰＯＮ番号 ,',',
    削除フラグ ,',',
    登録日 ,',',
    最終更新日 ,',',
    NVL(プッシュ通知許可フラグ,0))
FROM(
    SELECT
        G.外部認証ＩＤ,
        C.ＧＯＯＰＯＮ番号,
        G.削除フラグ,
        G.登録日,
        G.最終更新日,
        NVL(W.プッシュ通知許可フラグ,0) AS プッシュ通知許可フラグ,
        ROW_NUMBER() OVER (PARTITION BY C.ＧＯＯＰＯＮ番号 ORDER BY G.最終更新日時 DESC) AS RN
    FROM 
         MSカード情報 C
        ,MS外部認証情報 G
        ,WSスマホアプリ登録状況 W
    WHERE W.顧客番号     = C.顧客番号
    AND   C.サービス種別 = G.サービス種別
    AND   C.会員番号     = G.会員番号
    AND   G.外部認証種別 = 'A')
WHERE RN = 1
;


\o
