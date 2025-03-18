\set SDATE 'to_number(concat(to_char(add_months(nvl(to_date(cast(:1 as text),''yyyymmdd''),sysdate()),-1),''yyyymm''),''01000000''))'

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(add_months(sysdate(),-1),'yyyymm') sql_date_text from dual \gset
\o ./:sql_date_text._商品交換集計表_入会店舗別.utf

/* レコード削除 */
truncate table WS商品交換関連集計;

/* 店舗×性別×年代の取得  */
insert into WS商品交換関連集計 (種別, 店番号, クラスタ, 事業本部, エリア, 性別, 年代, 数量, 利用ポイント, 購入金額)
    select distinct 20, T1.店番号, NVL(T1.クラスタ,0), NVL(T1.部コード,0), NVL(T1.ゾーンコード,0), T2.コード, T3.コード, 0, 0, 0
    from PS店表示情報 T1, 
         (select コード, 名称 from WS集計用コード where 種別 = 'GENDER') T2,
         (select コード, 名称 from WS集計用コード where 種別 = 'GENERATION') T3
;
commit;

/* 入会店×性別×年代の集計 */
insert into WS商品交換関連集計 (種別, 店番号, クラスタ, 事業本部, エリア, 性別, 年代, 数量, 利用ポイント, 購入金額)
        select 21, T1.入会店舗, T1.クラスタ, T1.部コード, T1.ゾーンコード, T1.性別, T1.年代, sum(T1.数量), sum(T1.ポイント利用額按分額), sum(T1.購入金額按分額)
        from
        (select 
               NVL(tsrk.入会店舗, 0) AS 入会店舗,
               NVL(psms.クラスタ, 0) AS クラスタ,
               NVL(psms.部コード, 990000) AS 部コード,
               NVL(psms.ゾーンコード, 999900) AS ゾーンコード,
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
               left outer join MSカード情報 msca
                 on cast(msca.会員番号 as text)= wssk.会員番号 and msca.サービス種別 = 1
               left outer join TS利用可能ポイント情報 tsrk
                 on tsrk.顧客番号 = msca.顧客番号
               left outer join MM顧客情報 mmko
                 on mmko.顧客番号 = msca.顧客番号
               LEFT OUTER JOIN (select 店番号,部コード,ゾーンコード,ブロックコード,クラスタ,開始年月日,終了年月日 from PS店表示情報 T1 where not exists (select 1 from PS店表示情報 T2 where T2.店番号 = T1.店番号 and T2.終了年月日 < T1.終了年月日)) psms
                 ON psms.店番号 = tsrk.入会店舗
        where wssk.注文日付>=:SDATE and wssk.注文日付<:SDATE+100000000
        ) T1
        group by T1.入会店舗, T1.クラスタ, T1.部コード, T1.ゾーンコード, T1.性別, T1.年代
;


commit;


select concat('処理日時=',to_char(sysdate())) from dual;

/* ヘッダ */
select 
concat('レコード種別',',',
'区分/店番',',',
'クラスタ',',',
'客層',',',
'面積区分',',',
'旧販社名',',',
'店舗名',',',
'事業本部',',',
'エリア',',',
'ブロック',',',
'性別',',',
'年代',',',
'数量',',',
'ポイント利用額',',',
'購入金額')
from dual;


/* 性別×年代 */
select 
CONCAT('全社',',',                                     --レコード種別
'全社',',',                                     --区分／店番号
'全店舗',',',                                   --クラスタ
NULL,',',                                         --客層
NULL,',',                                         --面積区分
NULL,',',                                         --旧販社名
NULL,',',                                         --店舗名
NULL,',',                                         --事業本部
NULL,',',                                         --エリア
NULL,',',                                         --ブロック
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                  --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',              --年代
NVL(SYUKEI.数量,0) ,',',                        --数量
NVL(SYUKEI.ポイント利用額,0) ,',',              --ポイント利用額
NVL(SYUKEI.購入金額,0))                            --購入金額
from 
    (select 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=20 group by 性別, 年代) BASE
    LEFT OUTER JOIN (select 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=21 group by 性別, 年代) SYUKEI
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


/* クラスタ×性別×年代 */
select 
CONCAT('クラスタ',',',                                                                                                                                --レコード種別
BASE.クラスタ,',',                                                                                                                             --区分／店番号
case BASE.クラスタ when 0 then '0_不明' when 1 then '1_居住者少 駅近隣' when 2 then '2_居住者多 住宅地' when 3 then '3_居住者少 郊外地' when 4 then '4_SC/MALL 商業地' when 5 then '5_除業態' else null end,',',     --クラスタ
NULL,',',                                                                                                                                        --客層
NULL,',',                                                                                                                                        --面積区分
NULL,',',                                                                                                                                        --旧販社名
NULL,',',                                                                                                                                        --店舗名
NULL,',',                                                                                                                                        --事業本部
NULL,',',                                                                                                                                        --エリア
NULL,',',                                                                                                                                        --ブロック
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                                                                                                                 --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',                                                                                                             --年代
NVL(SYUKEI.数量,0) ,',',                                                                                                                       --数量
NVL(SYUKEI.ポイント利用額,0) ,',',                                                                                                             --ポイント利用額
NVL(SYUKEI.購入金額,0))                                                                                                                           --購入金額
from 
    (select クラスタ, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=20 group by クラスタ, 性別, 年代) BASE
    LEFT OUTER JOIN (select クラスタ, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=21 group by クラスタ, 性別, 年代) SYUKEI
      ON SYUKEI.クラスタ = BASE.クラスタ
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.クラスタ, BASE.性別, BASE.年代
;


/* 事業本部×性別×年代 */
select 
CONCAT('事業本部',',',                                                                                                                                --レコード種別
BASE.事業本部,',',                                                                                                                             --区分／店番号
nullif(rtrim(ST.部短縮名称),''),',',                                                                                                                    --クラスタ
NULL,',',                                                                                                                                        --客層
NULL,',',                                                                                                                                        --面積区分
NULL,',',                                                                                                                                        --旧販社名
NULL,',',                                                                                                                                        --店舗名
NULL,',',                                                                                                                                        --事業本部
NULL,',',                                                                                                                                        --エリア
NULL,',',                                                                                                                                        --ブロック
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                                                                                                                 --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',                                                                                                             --年代
NVL(SYUKEI.数量,0) ,',',                                                                                                                       --数量
NVL(SYUKEI.ポイント利用額,0) ,',',                                                                                                             --ポイント利用額
NVL(SYUKEI.購入金額,0))                                                                                                                           --購入金額
from 
    (select 事業本部, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=20 group by 事業本部, 性別, 年代) BASE
    LEFT OUTER JOIN (select 事業本部, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=21 group by 事業本部, 性別, 年代) SYUKEI
      ON SYUKEI.事業本部 = BASE.事業本部
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select distinct 部コード,部短縮名称 from PS店表示情報 T1 where not exists (select 1 from PS店表示情報 T2 where T2.店番号 = T1.店番号 and T2.終了年月日 < T1.終了年月日)) ST
      ON ST.部コード = BASE.事業本部
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.事業本部, BASE.性別, BASE.年代
;


/* エリア×性別×年代 */
select 
CONCAT('エリア',',',                                                                                                                                  --レコード種別
BASE.エリア,',',                                                                                                                         --区分／店番号
nullif(rtrim(ST.ゾーン短縮名称),''),',',                                                                                                                --クラスタ
NULL,',',                                                                                                                                        --客層
NULL,',',                                                                                                                                        --面積区分
NULL,',',                                                                                                                                        --旧販社名
NULL,',',                                                                                                                                        --店舗名
NULL,',',                                                                                                                                        --事業本部
NULL,',',                                                                                                                                        --エリア
NULL,',',                                                                                                                                        --ブロック
TRIM(WKCODE_GENDER.名称) ,',',                                                                                                                 --性別
TRIM(WKCODE_GENERATION.名称) ,',',                                                                                                             --年代
NVL(SYUKEI.数量,0) ,',',                                                                                                                       --数量
NVL(SYUKEI.ポイント利用額,0) ,',',                                                                                                             --ポイント利用額
NVL(SYUKEI.購入金額,0))                                                                                                                           --購入金額
from 
    (select エリア, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=20 group by エリア, 性別, 年代) BASE
    LEFT OUTER JOIN (select エリア, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=21 group by エリア, 性別, 年代) SYUKEI
      ON SYUKEI.エリア = BASE.エリア
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select distinct ゾーンコード,ゾーン短縮名称 from PS店表示情報 T1 where not exists (select 1 from PS店表示情報 T2 where T2.店番号 = T1.店番号 and T2.終了年月日 < T1.終了年月日)) ST
      ON ST.ゾーンコード = BASE.エリア
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.エリア, BASE.性別, BASE.年代
;


/* 店舗×性別×年代 */
select 
concat('店舗',',',                                                                                                                                    --レコード種別
BASE.店番号,',',                                                                                                                               --区分／店番号
case ST.クラスタ when 0 then '0_不明' when 1 then '1_居住者少 駅近隣' when 2 then '2_居住者多 住宅地' when 3 then '3_居住者少 郊外地' when 4 then '4_SC/MALL 商業地' when 5 then '5_除業態' else null end,',',     --クラスタ
case ST.客層 when 1 then '1_A' when 2 then '2_B' when 3 then '3_C' when 4 then '4_D' when 5 then '5_E' else null end,',',                   --客層
case ST.面積区分 when 1 then '1_小型' when 2 then '2_中型' when 3 then '3_大型' when 4 then '4_SC' when 5 then '5_除業態' else null end,',',    --面積区分
case ST.旧販社コード when 1 then '1_SJ' when 2 then '2_SG' when 3 then '3_ZD' when 4 then '4_LF' when 5 then '5_SZ' when 6 then '6_KD' when 9 then '9_CFH' when 10 then '10_OEC' else null end,',',   --旧販社名
nullif(rtrim(ST.店舗短縮名称),''),',',                                                                                                         --店舗名
nullif(rtrim(ST.部短縮名称),''),',',                                                                                                           --事業本部
nullif(rtrim(ST.ゾーン短縮名称),''),',',                                                                                                       --エリア
nullif(rtrim(ST.ブロック短縮名称),''),',',                                                                                                     --ブロック
NULLIF(TRIM(WKCODE_GENDER.名称),'') ,',',                                                                                                                 --性別
NULLIF(TRIM(WKCODE_GENERATION.名称),'') ,',',                                                                                                             --年代
NVL(SYUKEI.数量,0) ,',',                                                                                                                       --数量
NVL(SYUKEI.ポイント利用額,0) ,',',                                                                                                             --ポイント利用額
NVL(SYUKEI.購入金額,0))                                                                                                                           --購入金額
from 
    (select 店番号, 性別, 年代                                                                                                    from WS商品交換関連集計 where 種別=20 group by 店番号, 性別, 年代) BASE
    LEFT OUTER JOIN (select 店番号, 性別, 年代, SUM(数量) AS 数量, SUM(利用ポイント) AS ポイント利用額, SUM(購入金額) AS 購入金額 from WS商品交換関連集計 where 種別=21 group by 店番号, 性別, 年代) SYUKEI
      ON SYUKEI.店番号 = BASE.店番号
     AND SYUKEI.性別 = BASE.性別
     AND SYUKEI.年代 = BASE.年代
    LEFT OUTER JOIN (select 店番号,店舗短縮名称,部コード,部短縮名称,ゾーンコード,ゾーン短縮名称,ブロックコード,ブロック短縮名称,クラスタ,客層,面積区分,旧販社コード from PS店表示情報 T1 where not exists (select 1 from PS店表示情報 T2 where T2.店番号 = T1.店番号 and T2.終了年月日 < T1.終了年月日)) ST
      ON ST.店番号 = BASE.店番号
    INNER JOIN WS集計用コード WKCODE_GENDER
      ON WKCODE_GENDER.種別 = 'GENDER'
     AND WKCODE_GENDER.コード = BASE.性別
    INNER JOIN WS集計用コード WKCODE_GENERATION
      ON WKCODE_GENERATION.種別 = 'GENERATION'
     AND WKCODE_GENERATION.コード = BASE.年代
order by BASE.店番号, BASE.性別, BASE.年代
;

\o
