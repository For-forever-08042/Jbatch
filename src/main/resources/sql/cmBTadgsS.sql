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
  CONCAT(ＧＯＯＰＯＮ番号 , CHR(9) ,                                                      --ＧＯＯＰＯＮ番号
  NULL , CHR(9) ,                                                                    --統合会員ID
  NULL , CHR(9) ,                                                                    --プロファイルID
  NULL , CHR(9) ,                                                                    --姓
  NULL , CHR(9) ,                                                                    --名
  NULL , CHR(9) ,                                                                    --姓(カナ)
  NULL , CHR(9) ,                                                                    --名(カナ)
  NULL , CHR(9) ,                                                                    --メールアドレス
  '5092' , CHR(9) ,                                                                --メルマガ許諾
  '3092' , CHR(9) ,                                                                --DM許諾
  NULL , CHR(9) ,                                                                    --生年月日
  '3' , CHR(9) ,                                                                   --性別コード
  '0' , CHR(9) ,                                                                   --総ポイント残高
  '0' , CHR(9) ,                                                                   --期間限定ポイント残高1
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 0)),'YYYY/MM/DD') , CHR(9) ,       --期間限定ポイント期限1
  '0' , CHR(9) ,                                                                   --期間限定ポイント残高2
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 1)),'YYYY/MM/DD') , CHR(9) ,       --期間限定ポイント期限2
  '0' , CHR(9) ,                                                                   --期間限定ポイント残高3
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 2)),'YYYY/MM/DD') , CHR(9) ,       --期間限定ポイント期限3
  '0' , CHR(9) ,                                                                   --期間限定ポイント残高4
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 3)),'YYYY/MM/DD') , CHR(9) ,       --期間限定ポイント期限4
  NULL , CHR(9) ,                                                                    --ランク名称
  NULL , CHR(9) ,                                                                    --登録日
  TO_CHAR(最終更新日時,'YYYY/MM/DD HH:MM:SS') , CHR(9) ,                           --更新日
  NULL , CHR(9) ,                                                                    --MCC制度許諾登録日
  '0' , CHR(9) ,                                                                   --グループ会員区分
  NULL , CHR(9) ,                                                                    --グループ会員登録日
  '0' , CHR(9) ,                                                                   --今年度ポイント残高
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 9)),'YYYY') , '/03/31' , CHR(9) ,       --今年度ポイント期限
  '0' , CHR(9) ,                                                                   --次年度ポイント残高
  TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), 21)),'YYYY') , '/03/31' , CHR(9) ,       --次年度ポイント期限
  '0' , CHR(9) ,                                                                   --広告許諾情報
  '0' , CHR(9) ,                                                                   --MCCグループ会員区分
  NULL  , CHR(9) ,                                                                   --MCCグループ会員登録日
  '0' , CHR(9) ,                                                                   --GOOPON会員ステータス
  '1')                                                                                --GOOPON会員削除フラグ
FROM
  ( 
    SELECT
        B.旧ＧＯＯＰＯＮ番号 AS ＧＯＯＰＯＮ番号
      , B.最終更新日時
      , ROW_NUMBER() OVER (PARTITION BY B.旧顧客番号 ORDER BY B.最終更新日 DESC) as RN 
    FROM
      HSカード変更情報 B 
      ,MS顧客制度情報 M
    WHERE
      B.旧ＧＯＯＰＯＮ番号 IS NOT NULL 
      AND B.統合日付 >= CAST(TO_CHAR(ADD_MONTHS(TO_DATE(CAST(:BAT_DATE AS TEXT),'YYYYMMDD'), -1), 'YYYYMMDD') AS NUMERIC) 
      AND B.旧顧客番号 = M.顧客番号
      AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE B.旧顧客番号 = C.顧客番号) 
      AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE B.旧ＧＯＯＰＯＮ番号 = C.ＧＯＯＰＯＮ番号 )
  ) 
WHERE
  RN = 1; 

\o
