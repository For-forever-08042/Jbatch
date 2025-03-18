\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./RESENDLIST_CMD.TXT

SELECT 
        CONCAT('aws ses send-templated-email  --source "' , NVL(A.送信者,'') ,
        '" --template "' , NVL(A.テンプレート,'') ,
        '" --destination ToAddresses="' , NVL(A.送信先ＴＯ,'') ,
        CASE WHEN A.送信先ＣＣ IS NOT NULL AND A.送信先ＣＣ != '' THEN 
        CONCAT('" --destination CcAddresses="' , NVL(A.送信先ＣＣ,''))
        ELSE NULL END ,
        CASE WHEN A.送信先ＢＣＣ IS NOT NULL AND A.送信先ＢＣＣ != '' THEN 
        CONCAT('" --destination BccAddresses="' , NVL(A.送信先ＢＣＣ,''))
        ELSE NULL END ,
        '" --template-data "' , NVL(A.置換文字列,'') ,
        '" --configuration-set-name "' , NVL(A.設定セット,'') , '" ' , NVL(A.備考,'')   , ' --output "TEXT"')
FROM
 TMメール送信エラー履歴 A
WHERE
 A.システム年月日 = :1
AND
 A.メール履歴通番 = :2
;
\o
