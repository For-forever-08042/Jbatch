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
TRUNCATE TABLE WSÚqÔQ;
INSERT INTO WSÚqÔQ (ÚqÔ) 
        SELECT ÚqÔ FROM MMÚqîñ WHERE ÅIXVú >= :1
        UNION
        SELECT ÚqÔ FROM MMÚq®«îñ WHERE ÅIXVú >= :1
        UNION
        SELECT ÚqÔ FROM MMÚqéÆÊ®«îñ WHERE ÅIXVú >= :1
        UNION
        SELECT ÚqÔ FROM MSJ[hîñ WHERE ÅIXVú >= :1 AND ÚqÔ <> 0
        UNION
        SELECT C.ÚqÔ FROM MSOFØîñ G, MSJ[hîñ C 
                        WHERE G.T[rXíÊ = C.T[rXíÊ AND G.ïõÔ = C.ïõÔ
                         AND  G.OFØíÊ = 'L'            AND G.ÅIXVú >= :1
;
commit;
SELECT
'ÚqÔ,Z'
FROM 
 dual
;
SELECT
CONCAT(M.ÚqÔ , chr(9) ,
NVL(NULLIF(TRIM(M.Z),''),'@'))
FROM 
 MMÚq®«îñ M,
 WSÚqÔQ W
WHERE 
     W.ÚqÔ = M.ÚqÔ
;
\o
