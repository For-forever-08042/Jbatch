\set SDATE to_number(:1)

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 
select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./AddressCode_IN.tmp
TRUNCATE TABLE WS顧客番号２;
INSERT INTO WS顧客番号２ (顧客番号) 
        SELECT 顧客番号 FROM MM顧客情報 WHERE 最終更新日 >= :1
        UNION
        SELECT 顧客番号 FROM MM顧客属性情報 WHERE 最終更新日 >= :1
        UNION
        SELECT 顧客番号 FROM MM顧客企業別属性情報 WHERE 最終更新日 >= :1
        UNION
        SELECT 顧客番号 FROM MSカード情報 WHERE 最終更新日 >= :1 AND 顧客番号 <> 0
        UNION
        SELECT C.顧客番号 FROM MS外部認証情報 G, MSカード情報 C 
                        WHERE G.サービス種別 = C.サービス種別 AND G.会員番号 = C.会員番号
                         AND  G.外部認証種別 = 'L'            AND G.最終更新日 >= :1
;
commit;
SELECT
'顧客番号,住所'
FROM 
 dual
;
SELECT
CONCAT(M.顧客番号 , chr(9) ,
NVL(NULLIF(TRIM(M.住所),''),'　'))
FROM 
 MM顧客属性情報 M,
 WS顧客番号２ W
WHERE 
     W.顧客番号 = M.顧客番号
;
\o
