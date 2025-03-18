\set SDATE 'to_number(concat(to_char(add_months(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),-1),''yyyymm''),''01000000''))'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./:sql_date_text._商品交換集計表_商品別.utf



/* レコード削除 */
truncate table WS商品交換関連集計;

/* 交換商品情報×性別×年代の取得  */
insert into WS商品交換関連集計 (種別, 店番号, 大分類コード, 中分類コード, 商品コード, 性別, 年代, 数量, 利用ポイント, 購入金額)
    select distinct 10, 0, rpad(T1.大分類コード,length(T1.大分類コード)), rpad(T1.中分類コード,length(T1.中分類コード)), rpad(T1.商品コード,length(T1.商品コード)), T2.コード, T3.コード, 0, 0, 0
    from WS交換商品マスタ T1, 
         (select コード, 名称 from WS集計用コード where 種別 = 'GENDER') T2,
         (select コード, 名称 from WS集計用コード where 種別 = 'GENERATION') T3
;
commit;

/* 全商品×性別×年代の集計 */
insert into WS商品交換関連集計 (種別, 店番号, 大分類コード, 中分類コード, 商品コード, 性別, 年代, 数量, 利用ポイント, 購入金額)
        select 11, 0, T1.大分類コード, T1.中分類コード, T1.商品コード, T1.性別, T1.年代, sum(T1.数量), sum(T1.ポイント利用額按分額), sum(T1.購入金額按分額)
        from
        (select 
               wsks.大分類コード,
               wsks.中分類コード,
               wsks.中分類名称,
               wskm.商品コード,
               NVL(CASE WHEN mmko.性別 <> 1 AND mmko.性別 <> 2 THEN 999 ELSE mmko.性別 END, 999) AS 性別,
               NVL(CASE WHEN mmko.年齢 >= 10 AND mmko.年齢 < 20 THEN 1
                    WHEN mmko.年齢 >= 20 AND mmko.年齢 < 30 THEN 2
                    WHEN mmko.年齢 >= 30 AND mmko.年齢 < 40 THEN 3
                    WHEN mmko.年齢 >= 40 AND mmko.年齢 < 50 THEN 4
                    WHEN mmko.年齢 >= 50 AND mmko.年齢 < 60 THEN 5
                    WHEN mmko.年齢 >= 60 AND mmko.年齢 < 70 THEN 6
                    WHEN mmko.年齢 >= 70 AND mmko.年齢 < 80 THEN 7
                    WHEN mmko.年齢 >= 80 AND mmko.年齢 < 90 THEN 8
                    WHEN mmko.年齢 >= 90 AND mmko.年齢 < 100 THEN 9
                    ELSE 999 
                END, 999) AS 年代,
                wskm.数量, wskm.購入金額按分額, wskm.ポイント利用額按分額
        from WS商品交換情報 wssk
               inner join WS商品交換明細情報 wskm
                 on wskm.注文ＮＯ = wssk.注文ＮＯ
               left outer join WS交換商品マスタ wsks
                 on wsks.商品コード = wskm.商品コード
               left outer join MSカード情報 msca
                 on cast(msca.会員番号 as text)= wssk.会員番号 and msca.サービス種別 = 1
               left outer join MM顧客情報 mmko
                 on mmko.顧客番号 = msca.顧客番号
        where wssk.注文日付>=:SDATE and wssk.注文日付<:SDATE+100000000
        ) T1
        group by T1.大分類コード, T1.中分類コード, T1.商品コード, T1.性別, T1.年代
;
commit;


select concat('処理日時=',to_char(sysdate())) from dual;

/* ヘッダ */
select 
concat('レコード種別',',',
'大分類名',',',
'大分類ＣＤ',',',
'中分類名',',',
'中分類ＣＤ',',',
'商品名',',',
'商品ＣＤ',',',
'性別',',',
'年代',',',
'数量',',',
'ポイント利用額',',',
'購入金額')
from dual;


/* 性別×年代 */
select 
CONCAT('全商品',',',                                   --レコード種別
NULL,',',                                         --大分類名
NULL,',',                                         --大分類ＣＤ
NULL,',',                                         --中分類名
NULL,',',                                         --中分類ＣＤ
NULL,',',                                         --商品名
NULL,',',                                         --商品ＣＤ
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                  --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',              --年代
NVL(SYUKEI.数量,0) ,',',                        --数量
NVL(SYUKEI.ポイント利用額,0) ,',',              --ポイント利用額
NVL(SYUKEI.購入金額,0))                            --購入金額
from 
    (select 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=10 group by 性別, 年代) BASE
    LEFT OUTER JOIN (select 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=11 group by 性別, 年代) SYUKEI
      ON SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.性別, BASE.年代
;


/* 大分類×性別×年代 */
select 
CONCAT('大分類',',',                                   --レコード種別
'"', NULLIF(TRIM(SYOUHIN.大分類名称),'') ,'",',           --大分類名
NULLIF(TRIM(SYOUHIN.大分類コード),'') ,',',                --大分類ＣＤ
NULL,',',                                         --中分類名
NULL,',',                                         --中分類ＣＤ
NULL,',',                                         --商品名
NULL,',',                                         --商品ＣＤ
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                  --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',              --年代
NVL(SYUKEI.数量,0) ,',',                        --数量 
NVL(SYUKEI.ポイント利用額,0) ,',',              --ポイント利用額
NVL(SYUKEI.購入金額,0))                            --購入金額
from 
    (select 大分類コード, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=10 group by 大分類コード, 性別, 年代) BASE
    LEFT OUTER JOIN (select 大分類コード, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=11 group by 大分類コード, 性別, 年代) SYUKEI
      ON SYUKEI.大分類コード = BASE.大分類コード
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select 大分類コード, 大分類名称 from WS交換商品マスタ group by 大分類コード, 大分類名称) SYOUHIN
      ON SYOUHIN.大分類コード = BASE.大分類コード
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by SYOUHIN.大分類コード, BASE.性別, BASE.年代
;


/* 分類×中分類×性別×年代 */
select 
CONCAT('中分類',',',                                   --レコード種別
'"', NULLIF(TRIM(SYOUHIN.大分類名称),'') ,'",',           --大分類名
NULLIF(TRIM(SYOUHIN.大分類コード),'') ,',',                --大分類ＣＤ
'"', NULLIF(TRIM(SYOUHIN.中分類名称),'') ,'",',           --中分類名
NULLIF(TRIM(SYOUHIN.中分類コード),'') ,',',                --中分類ＣＤ
NULL,',',                                         --商品名
NULL,',',                                         --商品ＣＤ
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                  --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',              --年代
NVL(SYUKEI.数量,0) ,',',                        --数量
NVL(SYUKEI.ポイント利用額,0) ,',',              --ポイント利用額
NVL(SYUKEI.購入金額,0))                            --購入金額
from 
    (select 大分類コード, 中分類コード, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=10 group by 大分類コード, 中分類コード, 性別, 年代) BASE
    LEFT OUTER JOIN (select 大分類コード, 中分類コード, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=11 group by 大分類コード, 中分類コード, 性別, 年代) SYUKEI
      ON SYUKEI.大分類コード = BASE.大分類コード
     AND SYUKEI.中分類コード = BASE.中分類コード
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select 大分類コード, 大分類名称, 中分類コード, 中分類名称 from WS交換商品マスタ group by 大分類コード, 大分類名称, 中分類コード, 中分類名称) SYOUHIN
      ON SYOUHIN.大分類コード = BASE.大分類コード
     AND SYOUHIN.中分類コード = BASE.中分類コード
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.大分類コード, BASE.中分類コード, BASE.性別, BASE.年代
;


/* 商品×性別×年代 */
select 
CONCAT('商品',',',                                     --レコード種別
'"', NULLIF(TRIM(SYOUHIN.大分類名称),'') ,'",',           --大分類名
NULLIF(TRIM(SYOUHIN.大分類コード),'') ,',',                --大分類ＣＤ
'"', NULLIF(TRIM(SYOUHIN.中分類名称),'') ,'",',           --中分類名
NULLIF(TRIM(SYOUHIN.中分類コード),'') ,',',                --中分類ＣＤ
'"', NULLIF(TRIM(SYOUHIN.商品名称),'') ,'",',             --商品名称
NULLIF(TRIM(SYOUHIN.商品コード),'') ,',',                  --商品ＣＤ
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                  --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',              --年代
NVL(SYUKEI.数量,0) ,',',                        --数量
NVL(SYUKEI.ポイント利用額,0) ,',',              --ポイント利用額
NVL(SYUKEI.購入金額,0))                            --購入金額
from 
    (select 大分類コード, 中分類コード, 商品コード, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=10 group by 大分類コード, 中分類コード, 商品コード, 性別, 年代) BASE
    LEFT OUTER JOIN (select 大分類コード, 中分類コード, 商品コード, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=11 group by 大分類コード, 中分類コード, 商品コード, 性別, 年代) SYUKEI
      ON SYUKEI.商品コード = BASE.商品コード
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select 大分類コード, 大分類名称, 中分類コード, 中分類名称, 商品コード, 商品名称 from WS交換商品マスタ group by 大分類コード, 大分類名称, 中分類コード, 中分類名称, 商品コード, 商品名称) SYOUHIN
      ON SYOUHIN.商品コード = BASE.商品コード
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.大分類コード, BASE.中分類コード, BASE.商品コード, BASE.性別, BASE.年代
;

\o
