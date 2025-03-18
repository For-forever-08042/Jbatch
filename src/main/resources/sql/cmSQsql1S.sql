\set SDATE to_number(concat(to_char(add_months(sysdate(),-1),'yyyymm'),'01'))

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

select to_char(sysdate(),'yyyymmdd') spl_date_text from dual \gset
\o ./count_card_month:spl_date_text.utf


/* レコード削除 */
truncate table WS顧客数関連集計;

/* 店舗情報の取得  */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select distinct 店番号, 0, 0 from PS店表示情報;
commit;

/* カード切換情報の取得 (切換先が新CFカードを抽出) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select hs.作業店番号, 1, count(hs.顧客番号)
    from HSカード変更情報 hs
    where hs.作業年月日>=:SDATE and hs.作業年月日<:SDATE+100 and hs.会員番号>=200000000000000 and hs.理由コード=2002
    group by hs.作業店番号
;
commit;

/* カード統合情報の取得 (新CFカードの条件は無く、統合された件数) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select hs.作業店番号, 2, count(hs.顧客番号)
    from HSカード変更情報 hs
    where hs.作業年月日>=:SDATE and hs.作業年月日<:SDATE+100 and hs.理由コード=2003
    group by hs.作業店番号
;
commit;

/* 新CFカード発行情報の取得 (OEC、モバイル以外で、正常状態のカードの件数) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 3, count(ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード not in (1020,1040) and e.入会年月日>=:SDATE and e.入会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.会員番号>=200000000000000  and ms.企業コード not in (1020,1040) and ((ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013)) or (ms.理由コード in (2011,2012)) )
    group by ts.入会店舗
;
commit;

/* カード退会情報の取得（OEC、モバイル以外で、退会状態のカードの件数） */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 7, count(ts.顧客番号)
    from (select e.顧客番号 from MSカード情報 e where e.企業コード not in (1020,1040) and e.理由コード in (2011,2012) and e.終了年月日 >=:SDATE and e.終了年月日 <:SDATE+100) ms,
    TS利用可能ポイント情報 ts
    where ms.顧客番号=ts.顧客番号
    group by ts.入会店舗
;
commit;

/* カード数の取得 (OEC、モバイル以外で、5年間の間に買上があるカードの件数) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 4, count(ts.顧客番号) 
    from MSカード情報 ms,TS利用可能ポイント情報 ts
    where ms.企業コード not in (1020,1040) and ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013) and ms.顧客番号=ts.顧客番号 and ts.最終買上日>=:SDATE-50000
    group by ts.入会店舗
;
commit;

/* 顧客数の取得 (OEC、モバイル以外で、5年間の間に買上がある顧客の件数) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 12, count(distinct ts.顧客番号) 
    from MSカード情報 ms,TS利用可能ポイント情報 ts
    where ms.企業コード not in (1020,1040) and ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013) and ms.顧客番号=ts.顧客番号 and ts.最終買上日>=:SDATE-50000
    group by ts.入会店舗
;
commit;

/* クラブサイト入会顧客数（累計）の取得 */
/* (OECのみ保持顧客はポータル入会不可のため除く。モバイルのみ保持顧客はポータル入会可のため取得対象。カード、モバイル保持の顧客のため顧客番号の重複を除いてカウントする) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 8, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客情報 e where e.ポータル入会年月日<>0 and e.ポータル入会年月日<:SDATE+100 and (e.ポータル退会年月日=0 or e.ポータル退会年月日>=:SDATE+100)) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード <>1020 and ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013)
    group by ts.入会店舗
;
commit;

/* クラブサイト入会顧客数の取得 */
/* (OECのみ保持顧客はポータル入会不可のため除く。モバイルのみ保持顧客はポータル入会可のため取得対象。カード、モバイル保持の顧客のため顧客番号の重複を除いてカウントする) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 5, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客情報 e where e.ポータル入会年月日>=:SDATE and e.ポータル入会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード <>1020
    group by ts.入会店舗
;
commit;

/* クラブサイト退会顧客数の取得 */
/* (OECのみ保持顧客はポータル入会不可のため除く。モバイルのみ保持顧客はポータル入会可のため取得対象。カード、モバイル保持の顧客のため顧客番号の重複を除いてカウントする) */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 9, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客情報 e where e.ポータル退会年月日>=:SDATE and e.ポータル退会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード <>1020
    group by ts.入会店舗
;
commit;

/* ＥＣ入会顧客数（累計）の取得 */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 10, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1020 and e.入会年月日<>0 and e.入会年月日<:SDATE+100 and (e.退会年月日=0 or e.退会年月日>=:SDATE+100)) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード = 1020 and ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013)
    group by ts.入会店舗
;
commit;

/* ＥＣ入会顧客数の取得 */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 6, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1020 and e.入会年月日>=:SDATE and e.入会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード = 1020
    group by ts.入会店舗
;
commit;

/* ＥＣ退会顧客数の取得 */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 11, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1020 and e.退会年月日>=:SDATE and e.退会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード = 1020
    group by ts.入会店舗
;
commit;

/* モバイル入会顧客数（累計）の取得　追加 */
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 13, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1040 and e.入会年月日<>0 and e.入会年月日<:SDATE+100 and (e.退会年月日=0 or e.退会年月日>=:SDATE+100)) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード =1040 and ms.カードステータス in (0,7) and ms.理由コード in (2000,2021,2022,2013)
    group by ts.入会店舗
;
commit;

/* モバイル入会顧客数の取得 追加*/
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 14, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1040 and e.入会年月日>=:SDATE and e.入会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード =1040
    group by ts.入会店舗
;
commit;

/* モバイル退会顧客数の取得 追加*/
insert into WS顧客数関連集計 (店番号, 種別, 件数)
    select ts.入会店舗, 15, count(distinct ts.顧客番号)
    from (select e.顧客番号 from MM顧客企業別属性情報 e where e.企業コード=1040 and e.退会年月日>=:SDATE and e.退会年月日<:SDATE+100) mm,
    MSカード情報 ms, TS利用可能ポイント情報 ts
    where mm.顧客番号=ms.顧客番号 and mm.顧客番号=ts.顧客番号 and ms.企業コード =1040
    group by ts.入会店舗
;
commit;

select concat('処理日時=',to_char(sysdate())) from dual;
/* モバイルを追加*/
select 
CONCAT('レコード種別',',',
'区分/店番',',',
'クラスタ',',',
'客層',',',
'面積区分',',',
'旧販社名',',',
'店舗名',',',
'事業本部',',',
'エリア',',',
'ブロック',',',
'変更件数',',',
'統合件数',',',
'入会件数',',',
'退会件数',',',
'小計',',',
'カード件数',',',
'顧客件数',',',
'クラブ申込件数(累計)',',',
'クラブ申込件数',',',
'クラブ停止件数',',',
'ＥＣ入会件数(累計)',',',
'ＥＣ入会件数',',',
'ＥＣ退会件数',',',
'モバイル入会件数（累計）',',',
'モバイル入会件数',',',
'モバイル退会件数')
from dual;
/* モバイルを追加*/
select 
concat('全社',',',
'全社',',',
'全店舗',',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
sum(nvl(H.変更件数,0)),',',
sum(nvl(T.統合件数,0)),',',
sum(nvl(N.入会件数,0)),',',
sum(nvl(K.退会件数,0)),',',
(sum(nvl(H.変更件数,0))+sum(nvl(N.入会件数,0))),',',
sum(nvl(A.カード件数,0)),',',
sum(nvl(B.顧客件数,0)),',',
sum(nvl(L.ＣＬ累計件数,0)),',',
sum(nvl(C.ＣＬ申込件数,0)),',',
sum(nvl(D.ＣＬ停止件数,0)),',',
sum(nvl(R.ＥＣ累計件数,0)),',',
sum(nvl(E.ＥＣ入会件数,0)),',',
sum(nvl(Y.ＥＣ退会件数,0)),',',
sum(nvl(F.モバイル累計件数,0)),',',
sum(nvl(G.モバイル入会件数,0)),',',
sum(nvl(I.モバイル退会件数,0)))
from 
    (select 店番号                from ws顧客数関連集計 where 種別=0) S
left join 
    (select 店番号, 件数 変更件数 from ws顧客数関連集計 where 種別=1) H on S.店番号 = H.店番号
left join 
    (select 店番号, 件数 統合件数 from ws顧客数関連集計 where 種別=2) T on S.店番号 = T.店番号
left join 
    (select 店番号, 件数 as 入会件数 from ws顧客数関連集計 where 種別 = 3) N on S.店番号 = N.店番号
left join 
    (select 店番号, 件数 as 退会件数 from ws顧客数関連集計 where 種別 = 7) K on S.店番号 = K.店番号
left join 
    (select 店番号, 件数 as カード件数 from ws顧客数関連集計 where 種別 = 4) A on S.店番号 = A.店番号
left join 
    (select 店番号, 件数 as 顧客件数 from ws顧客数関連集計 where 種別 = 12) B on S.店番号 = B.店番号
left join 
    (select 店番号, 件数 as ＣＬ累計件数 from ws顧客数関連集計 where 種別 = 8) L on S.店番号 = L.店番号
left join 
    (select 店番号, 件数 as ＣＬ申込件数 from ws顧客数関連集計 where 種別 = 5) C on S.店番号 = C.店番号
left join 
    (select 店番号, 件数 as ＣＬ停止件数 from ws顧客数関連集計 where 種別 = 9) D on S.店番号 = D.店番号
left join 
    (select 店番号, 件数 as ＥＣ累計件数 from ws顧客数関連集計 where 種別 = 10) R on S.店番号 = R.店番号
left join 
    (select 店番号, 件数 as ＥＣ入会件数 from ws顧客数関連集計 where 種別 = 6) E on S.店番号 = E.店番号
left join 
    (select 店番号, 件数 as ＥＣ退会件数 from ws顧客数関連集計 where 種別 = 11) Y on S.店番号 = Y.店番号
left join 
    (select 店番号, 件数 as モバイル累計件数 from ws顧客数関連集計 where 種別 = 13) F on S.店番号 = F.店番号
left join 
    (select 店番号, 件数 as モバイル入会件数 from ws顧客数関連集計 where 種別 = 14) G on S.店番号 = G.店番号
left join 
    (select 店番号, 件数 as モバイル退会件数 from ws顧客数関連集計 where 種別 = 15) I on S.店番号 = I.店番号;
/*  2015/11/16 (作業依頼No406)クラスタ設定改善 名称変更 */
/* モバイルを追加*/
select 
concat('クラスタ',',',
ST.クラスタ,',',
case ST.クラスタ when 1 then 'A_都市型' when 2 then 'B_商店街型' when 3 then 'C_住宅地型' when 4 then 'D_郊外型' when 5 then 'Z_その他' when 9 then 'NULL_未設定' else '不明' end,',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
sum(nvl(H.変更件数,0)),',',
sum(nvl(T.統合件数,0)),',',
sum(nvl(N.入会件数,0)),',',
sum(nvl(K.退会件数,0)),',',
(sum(nvl(H.変更件数,0))+sum(nvl(N.入会件数,0))),',',
sum(nvl(A.カード件数,0)),',',
sum(nvl(B.顧客件数,0)),',',
sum(nvl(L.ＣＬ累計件数,0)),',',
sum(nvl(C.ＣＬ申込件数,0)),',',
sum(nvl(D.ＣＬ停止件数,0)),',',
sum(nvl(R.ＥＣ累計件数,0)),',',
sum(nvl(E.ＥＣ入会件数,0)),',',
sum(nvl(Y.ＥＣ退会件数,0)),',',
sum(nvl(F.モバイル累計件数,0)),',',
sum(nvl(G.モバイル入会件数,0)),',',
sum(nvl(I.モバイル退会件数,0)))
from (select 店番号 from WS顧客数関連集計 where 種別=0) S
    left join (select 店番号, 件数 変更件数 from WS顧客数関連集計 where 種別=1) H on S.店番号 = H.店番号
    left join (select 店番号, 件数 統合件数 from WS顧客数関連集計 where 種別=2) T on S.店番号 = T.店番号
    left join (select 店番号, 件数 入会件数 from WS顧客数関連集計 where 種別=3) N on S.店番号 = N.店番号
    left join (select 店番号, 件数 退会件数 from WS顧客数関連集計 where 種別=7) K on S.店番号 = K.店番号
    left join (select 店番号, 件数 カード件数 from WS顧客数関連集計 where 種別=4) A on S.店番号 = A.店番号
    left join (select 店番号, 件数 顧客件数 from WS顧客数関連集計 where 種別=12) B on S.店番号 = B.店番号
    left join (select 店番号, 件数 ＣＬ累計件数 from WS顧客数関連集計 where 種別=8) L on S.店番号 = L.店番号
    left join (select 店番号, 件数 ＣＬ申込件数 from WS顧客数関連集計 where 種別=5) C on S.店番号 = C.店番号
    left join (select 店番号, 件数 ＣＬ停止件数 from WS顧客数関連集計 where 種別=9) D on S.店番号 = D.店番号
    left join (select 店番号, 件数 ＥＣ累計件数 from WS顧客数関連集計 where 種別=10) R on S.店番号 = R.店番号
    left join (select 店番号, 件数 ＥＣ入会件数 from WS顧客数関連集計 where 種別=6) E on S.店番号 = E.店番号
    left join (select 店番号, 件数 ＥＣ退会件数 from WS顧客数関連集計 where 種別=11) Y on S.店番号 = Y.店番号
    left join (
               select 店番号,店舗短縮名称,部コード,部短縮名称,ゾーンコード,ゾーン短縮名称,ブロックコード,ブロック短縮名称,クラスタ,客層,面積区分,旧販社コード,開始年月日,終了年月日
               from PS店表示情報 a
               where not exists (select 1 from PS店表示情報 b where a.店番号 = b.店番号 and a.終了年月日 < b.終了年月日)
    ) ST on S.店番号 = ST.店番号
    left join (select 店番号, 件数 モバイル累計件数 from WS顧客数関連集計 where 種別=13) F on S.店番号 = F.店番号
    left join (select 店番号, 件数 モバイル入会件数 from WS顧客数関連集計 where 種別=14) G on S.店番号 = G.店番号
    left join (select 店番号, 件数 モバイル退会件数 from WS顧客数関連集計 where 種別=15) I on S.店番号 = I.店番号
group by ST.クラスタ
order by ST.クラスタ;
/* モバイルを追加*/
select 
concat('事業本部',',',
ST.部コード,',',
nullif(rtrim(ST.部短縮名称),''),',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
sum(nvl(H.変更件数,0)),',',
sum(nvl(T.統合件数,0)),',',
sum(nvl(N.入会件数,0)),',',
sum(nvl(K.退会件数,0)),',',
(sum(nvl(H.変更件数,0))+sum(nvl(N.入会件数,0))),',',
sum(nvl(A.カード件数,0)),',',
sum(nvl(B.顧客件数,0)),',',
sum(nvl(L.ＣＬ累計件数,0)),',',
sum(nvl(C.ＣＬ申込件数,0)),',',
sum(nvl(D.ＣＬ停止件数,0)),',',
sum(nvl(R.ＥＣ累計件数,0)),',',
sum(nvl(E.ＥＣ入会件数,0)),',',
sum(nvl(Y.ＥＣ退会件数,0)),',',
sum(nvl(F.モバイル累計件数,0)),',',
sum(nvl(G.モバイル入会件数,0)),',',
sum(nvl(I.モバイル退会件数,0)))
from (select 店番号 from WS顧客数関連集計 where 種別=0) S
    left join (select 店番号, 件数 変更件数 from WS顧客数関連集計 where 種別=1) H on S.店番号 = H.店番号
    left join (select 店番号, 件数 統合件数 from WS顧客数関連集計 where 種別=2) T on S.店番号 = T.店番号
    left join (select 店番号, 件数 入会件数 from WS顧客数関連集計 where 種別=3) N on S.店番号 = N.店番号
    left join (select 店番号, 件数 退会件数 from WS顧客数関連集計 where 種別=7) K on S.店番号 = K.店番号
    left join (select 店番号, 件数 カード件数 from WS顧客数関連集計 where 種別=4) A on S.店番号 = A.店番号
    left join (select 店番号, 件数 顧客件数 from WS顧客数関連集計 where 種別=12) B on S.店番号 = B.店番号
    left join (select 店番号, 件数 ＣＬ累計件数 from WS顧客数関連集計 where 種別=8) L on S.店番号 = L.店番号
    left join (select 店番号, 件数 ＣＬ申込件数 from WS顧客数関連集計 where 種別=5) C on S.店番号 = C.店番号
    left join (select 店番号, 件数 ＣＬ停止件数 from WS顧客数関連集計 where 種別=9) D on S.店番号 = D.店番号
    left join (select 店番号, 件数 ＥＣ累計件数 from WS顧客数関連集計 where 種別=10) R on S.店番号 = R.店番号
    left join (select 店番号, 件数 ＥＣ入会件数 from WS顧客数関連集計 where 種別=6) E on S.店番号 = E.店番号
    left join (select 店番号, 件数 ＥＣ退会件数 from WS顧客数関連集計 where 種別=11) Y on S.店番号 = Y.店番号
    left join (
               select 店番号,店舗短縮名称,部コード,部短縮名称,ゾーンコード,ゾーン短縮名称,ブロックコード,ブロック短縮名称,クラスタ,客層,面積区分,旧販社コード,開始年月日,終了年月日
               from PS店表示情報 a
               where not exists (select 1 from PS店表示情報 b where a.店番号 = b.店番号 and a.終了年月日 < b.終了年月日)
    ) ST on S.店番号 = ST.店番号
    left join (select 店番号, 件数 モバイル累計件数 from WS顧客数関連集計 where 種別=13) F on S.店番号 = F.店番号
    left join (select 店番号, 件数 モバイル入会件数 from WS顧客数関連集計 where 種別=14) G on S.店番号 = G.店番号
    left join (select 店番号, 件数 モバイル退会件数 from WS顧客数関連集計 where 種別=15) I on S.店番号 = I.店番号
group by ST.部コード,ST.部短縮名称
order by ST.部コード,ST.部短縮名称;
/* モバイルを追加*/
select 
concat('エリア',',',
ST.ゾーンコード,',',
nullif(rtrim(ST.ゾーン短縮名称),''),',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
null,',',
sum(nvl(H.変更件数,0)),',',
sum(nvl(T.統合件数,0)),',',
sum(nvl(N.入会件数,0)),',',
sum(nvl(K.退会件数,0)),',',
(sum(nvl(H.変更件数,0))+sum(nvl(N.入会件数,0))),',',
sum(nvl(A.カード件数,0)),',',
sum(nvl(B.顧客件数,0)),',',
sum(nvl(L.ＣＬ累計件数,0)),',',
sum(nvl(C.ＣＬ申込件数,0)),',',
sum(nvl(D.ＣＬ停止件数,0)),',',
sum(nvl(R.ＥＣ累計件数,0)),',',
sum(nvl(E.ＥＣ入会件数,0)),',',
sum(nvl(Y.ＥＣ退会件数,0)),',',
sum(nvl(F.モバイル累計件数,0)),',',
sum(nvl(G.モバイル入会件数,0)),',',
sum(nvl(I.モバイル退会件数,0)))
from (select 店番号 from WS顧客数関連集計 where 種別=0) S
    left join (select 店番号, 件数 変更件数 from WS顧客数関連集計 where 種別=1) H on S.店番号 = H.店番号
    left join (select 店番号, 件数 統合件数 from WS顧客数関連集計 where 種別=2) T on S.店番号 = T.店番号
    left join (select 店番号, 件数 入会件数 from WS顧客数関連集計 where 種別=3) N on S.店番号 = N.店番号
    left join (select 店番号, 件数 退会件数 from WS顧客数関連集計 where 種別=7) K on S.店番号 = K.店番号
    left join (select 店番号, 件数 カード件数 from WS顧客数関連集計 where 種別=4) A on S.店番号 = A.店番号
    left join (select 店番号, 件数 顧客件数 from WS顧客数関連集計 where 種別=12) B on S.店番号 = B.店番号
    left join (select 店番号, 件数 ＣＬ累計件数 from WS顧客数関連集計 where 種別=8) L on S.店番号 = L.店番号
    left join (select 店番号, 件数 ＣＬ申込件数 from WS顧客数関連集計 where 種別=5) C on S.店番号 = C.店番号
    left join (select 店番号, 件数 ＣＬ停止件数 from WS顧客数関連集計 where 種別=9) D on S.店番号 = D.店番号
    left join (select 店番号, 件数 ＥＣ累計件数 from WS顧客数関連集計 where 種別=10) R on S.店番号 = R.店番号
    left join (select 店番号, 件数 ＥＣ入会件数 from WS顧客数関連集計 where 種別=6) E on S.店番号 = E.店番号
    left join (select 店番号, 件数 ＥＣ退会件数 from WS顧客数関連集計 where 種別=11) Y on S.店番号 = Y.店番号
    left join (
               select 店番号,店舗短縮名称,部コード,部短縮名称,ゾーンコード,ゾーン短縮名称,ブロックコード,ブロック短縮名称,クラスタ,客層,面積区分,旧販社コード,開始年月日,終了年月日
               from PS店表示情報 a
               where not exists (select 1 from PS店表示情報 b where a.店番号 = b.店番号 and a.終了年月日 < b.終了年月日)
    ) ST on S.店番号 = ST.店番号
    left join (select 店番号, 件数 モバイル累計件数 from WS顧客数関連集計 where 種別=13) F on S.店番号 = F.店番号
    left join (select 店番号, 件数 モバイル入会件数 from WS顧客数関連集計 where 種別=14) G on S.店番号 = G.店番号
    left join (select 店番号, 件数 モバイル退会件数 from WS顧客数関連集計 where 種別=15) I on S.店番号 = I.店番号
group by ST.ゾーンコード,ST.ゾーン短縮名称
order by ST.ゾーンコード,ST.ゾーン短縮名称;
/*  2015/11/16 (作業依頼No406)クラスタ設定改善 名称変更 */
/* モバイルを追加*/
select 
concat('店舗',',',
ST.店番号,',',
case ST.クラスタ when 1 then 'A_都市型' when 2 then 'B_商店街型' when 3 then 'C_住宅地型' when 4 then 'D_郊外型' when 5 then 'Z_その他' when 9 then 'NULL_未設定' else '不明' end,',',
case ST.客層 when 1 then '1_A' when 2 then '2_B' when 3 then '3_C' when 4 then '4_D' when 5 then '5_E' else null end,',',
case ST.面積区分 when 1 then '1_小型' when 2 then '2_中型' when 3 then '3_大型' when 4 then '4_SC' when 5 then '5_除業態' else null end,',',
case ST.旧販社コード when 1 then '1_SJ' when 2 then '2_SG' when 3 then '3_ZD' when 4 then '4_LF' when 5 then '5_SZ' when 6 then '6_KD' when 9 then '9_CFH' when 10 then '10_OEC' else null end,',',
nullif(rtrim(ST.店舗短縮名称),''),',',
nullif(rtrim(ST.部短縮名称),''),',',
nullif(rtrim(ST.ゾーン短縮名称),''),',',
nullif(rtrim(ST.ブロック短縮名称),''),',',
nvl(H.変更件数,0),',',
nvl(T.統合件数,0),',',
nvl(N.入会件数,0),',',
nvl(K.退会件数,0),',',
(nvl(H.変更件数,0)+nvl(N.入会件数,0)),',',
nvl(A.カード件数,0),',',
nvl(B.顧客件数,0),',',
nvl(L.ＣＬ累計件数,0),',',
nvl(C.ＣＬ申込件数,0),',',
nvl(D.ＣＬ停止件数,0),',',
nvl(R.ＥＣ累計件数,0),',',
nvl(E.ＥＣ入会件数,0),',',
nvl(Y.ＥＣ退会件数,0),',',
nvl(F.モバイル累計件数,0),',',
nvl(G.モバイル入会件数,0),',',
nvl(I.モバイル退会件数,0))
from (select 店番号 from WS顧客数関連集計 where 種別=0) S
    left join (select 店番号, 件数 変更件数 from WS顧客数関連集計 where 種別=1) H on S.店番号 = H.店番号
    left join (select 店番号, 件数 統合件数 from WS顧客数関連集計 where 種別=2) T on S.店番号 = T.店番号
    left join (select 店番号, 件数 入会件数 from WS顧客数関連集計 where 種別=3) N on S.店番号 = N.店番号
    left join (select 店番号, 件数 退会件数 from WS顧客数関連集計 where 種別=7) K on S.店番号 = K.店番号
    left join (select 店番号, 件数 カード件数 from WS顧客数関連集計 where 種別=4) A on S.店番号 = A.店番号
    left join (select 店番号, 件数 顧客件数 from WS顧客数関連集計 where 種別=12) B on S.店番号 = B.店番号
    left join (select 店番号, 件数 ＣＬ累計件数 from WS顧客数関連集計 where 種別=8) L on S.店番号 = L.店番号
    left join (select 店番号, 件数 ＣＬ申込件数 from WS顧客数関連集計 where 種別=5) C on S.店番号 = C.店番号
    left join (select 店番号, 件数 ＣＬ停止件数 from WS顧客数関連集計 where 種別=9) D on S.店番号 = D.店番号
    left join (select 店番号, 件数 ＥＣ累計件数 from WS顧客数関連集計 where 種別=10) R on S.店番号 = R.店番号
    left join (select 店番号, 件数 ＥＣ入会件数 from WS顧客数関連集計 where 種別=6) E on S.店番号 = E.店番号
    left join (select 店番号, 件数 ＥＣ退会件数 from WS顧客数関連集計 where 種別=11) Y on S.店番号 = Y.店番号
    left join (
               select 店番号,店舗短縮名称,部コード,部短縮名称,ゾーンコード,ゾーン短縮名称,ブロックコード,ブロック短縮名称,クラスタ,客層,面積区分,旧販社コード,開始年月日,終了年月日
               from PS店表示情報 a
               where not exists (select 1 from PS店表示情報 b where a.店番号 = b.店番号 and a.終了年月日 < b.終了年月日)
    ) ST on S.店番号 = ST.店番号
    left join (select 店番号, 件数 モバイル累計件数 from WS顧客数関連集計 where 種別=13) F on S.店番号 = F.店番号
    left join (select 店番号, 件数 モバイル入会件数 from WS顧客数関連集計 where 種別=14) G on S.店番号 = G.店番号
    left join (select 店番号, 件数 モバイル退会件数 from WS顧客数関連集計 where 種別=15) I on S.店番号 = I.店番号
order by ST.部コード,ST.ゾーンコード,ST.ブロックコード,ST.店番号;

\o
