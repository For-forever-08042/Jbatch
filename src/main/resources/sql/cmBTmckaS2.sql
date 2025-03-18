
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';
delete from WMＤＭ対象者情報 WHERE ＧＯＯＰＯＮ番号 IN ( 
    SELECT
      ＧＯＯＰＯＮ番号 
    FROM
      ( 
        SELECT
          ＧＯＯＰＯＮ番号
          , ROW_NUMBER() OVER (PARTITION BY 氏名, 住所 ORDER BY 最終静態更新日時 DESC) AS RN 
        FROM
          WMＤＭ対象者情報
        WHERE ファイル番号=:1
      ) 
    WHERE
      RN <> 1
  )
  AND ファイル番号=:1
;
delete from WMＤＭ対象者情報 WHERE (TRIM('　' FROM TRIM(氏名)) IS NULL OR  TRIM('　' FROM TRIM(氏名)) = '' ) OR (TRIM('　' FROM TRIM(住所)) IS NULL OR TRIM('　' FROM TRIM(住所)) = '' ) AND ファイル番号=:1;
delete from WMＤＭ対象者情報 WHERE TRIM('　' FROM TRIM(氏名)) = 'あ' AND ファイル番号=:1;
delete from WMＤＭ対象者情報 WHERE regexp_LIKE(TRIM(住所), '^.*市$', 'i') AND ファイル番号=:1;
delete from WMＤＭ対象者情報 WHERE regexp_LIKE(住所, '^[a-zA-Z]', 'i') AND ファイル番号=:1;


