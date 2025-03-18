\set NENTUKI :1
\set SQL_DATE :2

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./:NENTUKI._sumk.log


TRUNCATE TABLE WSＳＡＰ付与還元実績抽出 ;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０１
             ,U.購買区分０１
             ,NVL(sum(U.付与ポイント０１),0)
             ,NVL(sum(U.利用ポイント０１),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０１ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０１
             ,U.購買区分０１
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０２
             ,U.購買区分０２
             ,NVL(sum(U.付与ポイント０２),0)
             ,NVL(sum(U.利用ポイント０２),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０２ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０２
             ,U.購買区分０２
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０３
             ,U.購買区分０３
             ,NVL(sum(U.付与ポイント０３),0)
             ,NVL(sum(U.利用ポイント０３),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０３ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０３
             ,U.購買区分０３
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０４
             ,U.購買区分０４
             ,NVL(sum(U.付与ポイント０４),0)
             ,NVL(sum(U.利用ポイント０４),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０４ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０４
             ,U.購買区分０４
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０５
             ,U.購買区分０５
             ,NVL(sum(U.付与ポイント０５),0)
             ,NVL(sum(U.利用ポイント０５),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０５ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０５
             ,U.購買区分０５
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０６
             ,U.購買区分０６
             ,NVL(sum(U.付与ポイント０６),0)
             ,NVL(sum(U.利用ポイント０６),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０６ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０６
             ,U.購買区分０６
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０７
             ,U.購買区分０７
             ,NVL(sum(U.付与ポイント０７),0)
             ,NVL(sum(U.利用ポイント０７),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０７ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０７
             ,U.購買区分０７
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０８
             ,U.購買区分０８
             ,NVL(sum(U.付与ポイント０８),0)
             ,NVL(sum(U.利用ポイント０８),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０８ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０８
             ,U.購買区分０８
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０９
             ,U.購買区分０９
             ,NVL(sum(U.付与ポイント０９),0)
             ,NVL(sum(U.利用ポイント０９),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分０９ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分０９
             ,U.購買区分０９
;

INSERT INTO WSＳＡＰ付与還元実績抽出 (
               登録年月日
              ,会社コード
              ,店舗コード
              ,理由コード
              ,通常期間限定区分
              ,購買区分
              ,付与ポイント数
              ,利用ポイント数
)
SELECT 
              D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分１０
             ,U.購買区分１０
             ,NVL(sum(U.付与ポイント１０),0)
             ,NVL(sum(U.利用ポイント１０),0)
FROM
     HSポイント日別情報:NENTUKI     D
    ,HSポイント日別内訳情報:NENTUKI U
WHERE 
     D.システム年月日 = U.システム年月日
 AND D.顧客番号       = U.顧客番号
 AND D.処理通番       = U.処理通番
 AND U.付与利用区分１０ IS NOT NULL
 AND ( D.登録年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)             --前月/1
      OR ( D.システム年月日 >= CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+4,'YYYYMMDD') AS NUMERIC)     --前月/4
       AND D.登録年月日 < CAST(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(CAST(:SQL_DATE AS TEXT)), -2))+1,'YYYYMMDD') AS NUMERIC)))        --前月/1
GROUP BY
              D.システム年月日
             ,D.登録年月日
             ,D.会社コードＭＣＣ
             ,D.店番号ＭＣＣ
             ,D.理由コード
             ,U.通常期間限定区分１０
             ,U.購買区分１０
;
commit ;

\o
