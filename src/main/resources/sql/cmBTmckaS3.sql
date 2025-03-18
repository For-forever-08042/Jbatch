\set NEN :1
\set NENTSUKI :2
\set BAT_DATE :3
\set DIRECTMAILCD :4
\set DIRECTMAILNAME :5
\set CNT :6
\set HAISOUDAY :7
\set KYOKUDASHIDAY :8

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';

INSERT INTO HS行動履歴共通:NENTSUKI ( 
  システム年月日
  , 行動履歴通番
  , 顧客番号
  , 会員番号
  , ＧＯＯＰＯＮ番号
  , 行動日時
  , 行動コード
  , 会社コード
  , 店舗コード
  , 個別項目情報
  , 作業企業コード
  , 作業者ＩＤ
  , 作業年月日
  , 作業時刻
  , バッチ更新日
  , 最終更新日
  , 最終更新日時
  , 最終更新プログラムＩＤ
) 
SELECT
  :BAT_DATE
  , nextval('SQ行動履歴通番')
  , A.顧客番号
  , A.会員番号
  , A.ＧＯＯＰＯＮ番号
  , TO_DATE(CAST(:KYOKUDASHIDAY AS TEXT),'YYYYMMDD')
  , '30020000'
  , NULL
  , NULL
  ,CONCAT( '{"sendDate":"' , TO_CHAR(TO_DATE(CAST(:KYOKUDASHIDAY AS TEXT),'YYYYMMDD'), 'YYYY/MM/DD') , 
    '","dmName":"' , NULLIF(TRIM(CAST(:DIRECTMAILNAME AS TEXT)), '') , 
    '","dmId":"' , LPAD(:DIRECTMAILCD,4,'0') , 
    '","treatmentCode":""}')
  , 0
  , 0
  , 0
  , 0
  , :BAT_DATE
  , :BAT_DATE
  , sysdate()
  , 'cmBTmckaS' 
FROM
    WMＤＭ対象者情報 A
;

INSERT INTO HSＤＭ送付状態:NEN ( 
  顧客番号
  , ダイレクトメールＩＤ
  , 連番
  , 送付状態
  , 送付日時
  , 登録日
  , 更新日
  , トリートメントコード
  , 旧顧客番号
  , バッチ更新日
  , 最終更新日
  , 最終更新日時
  , 最終更新プログラムＩＤ
) 
SELECT 
    顧客番号
  , LPAD(:DIRECTMAILCD,4,'0')
  , LPAD(ROW_NUMBER() OVER(),7,'0')
  , 2
  , :KYOKUDASHIDAY
  , :BAT_DATE
  , :BAT_DATE
  , NULL
  , NULL
  , :BAT_DATE
  , :BAT_DATE
  , sysdate()
  , 'cmBTmckaS' 
FROM
(
SELECT
    A.顧客番号
FROM
    WMＤＭ対象者情報 A
ORDER BY ファイル番号,ＧＯＯＰＯＮ番号,店舗コード,会社コード
)
; 

INSERT INTO MSＤＭマスタ ( 
  ダイレクトメールＩＤ
  , ダイレクトメール名称
  , 抽出日
  , 抽出件数
  , 配送日
  , トリートメントコード
  , 行動履歴出力日時
  , バッチ更新日
  , 最終更新日
  , 最終更新日時
  , 最終更新プログラムＩＤ
) 
VALUES ( 
  LPAD(:DIRECTMAILCD,4,'0')
  , CAST(:DIRECTMAILNAME AS TEXT)
  , :BAT_DATE
  , :CNT
  , :HAISOUDAY
  , NULL
  , sysdate()
  , :BAT_DATE
  , :BAT_DATE
  , sysdate()
  , 'cmBTmckaS'
);
