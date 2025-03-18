\set SFILENAME :1                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


\o ./:SFILENAME

--リカバリデータからの連携用ファイルデータ取得

select
    CONCAT(TO_CHAR(親ＧＯＯＰＯＮ番号)
    , ',' , TO_CHAR(子ＧＯＯＰＯＮ番号)
    , ',' , 削除フラグ
    , ',' , MIN(発行年月日)
    , ',' , 最終更新日
    , ',' , SUM(RCM連動可否)) 
from
  ( 
--統合によってアクティブとなったレコードの取得（顧客基盤主幹）
    select  /*+ INDEX(C IXMSCARD02) */
      A.親ＧＯＯＰＯＮ番号
      , A.子ＧＯＯＰＯＮ番号
      , A.削除フラグ
      , case when A.親ＧＯＯＰＯＮ番号 = A.子ＧＯＯＰＯＮ番号 
          then case when C.発行年月日 = 0
                 then A.最終更新日
                 else C.発行年月日
               end
          else H.統合日付
        end as 発行年月日
      , A.最終更新日
      , case when カード種別 not in (504, 505, 998) 
          then 1 
          else 0 
        end as RCM連動可否 
    from
      WSＧＯＯＰＯＮ番号紐付 A
      , MSカード情報 C
      , HSカード変更情報 H
      , PS会員番号体系 P 
    where
          A.親ＧＯＯＰＯＮ番号 = H.ＧＯＯＰＯＮ番号 
      and C.会員番号 = H.会員番号
      and H.統合採用会員番号 IS NULL
      and H.統合切替解除年月日=0
      and A.削除フラグ = 0
      and C.サービス種別 = P.サービス種別 
      and C.会員番号 >= P.会員番号開始 
      and C.会員番号 <= P.会員番号終了 
    UNION 
--統合によってアクティブとなったレコードの取得（GOOPON主幹）
    select  /*+ INDEX(C IXMSCARD02) */
      A.親ＧＯＯＰＯＮ番号
      , A.子ＧＯＯＰＯＮ番号
      , A.削除フラグ
      , case when A.親ＧＯＯＰＯＮ番号 = A.子ＧＯＯＰＯＮ番号 
          then case when C.発行年月日 = 0
                 then A.最終更新日
                 else C.発行年月日
               end
          else H.統合日付
        end as 発行年月日
      , A.最終更新日
      , case when カード種別 not in (504, 505, 998) 
          then 1 
          else 0 
        end as RCM連動可否 
    from
      WSＧＯＯＰＯＮ番号紐付 A
      , MSカード情報 C
      , HSカード変更情報 H
      , PS会員番号体系 P 
    where
          A.親ＧＯＯＰＯＮ番号 = H.ＧＯＯＰＯＮ番号 
      and C.会員番号 = H.旧会員番号
      and H.統合採用会員番号 IS NOT NULL
      and H.統合切替解除年月日=0
      and A.削除フラグ = 0
      and C.サービス種別 = P.サービス種別 
      and C.会員番号 >= P.会員番号開始 
      and C.会員番号 <= P.会員番号終了 
    UNION 
--統合によって削除となったレコードの取得（顧客基盤主幹）
    select  /*+ INDEX(C IXMSCARD02) */
      A.親ＧＯＯＰＯＮ番号
      , A.子ＧＯＯＰＯＮ番号
      , A.削除フラグ
      , case when A.親ＧＯＯＰＯＮ番号 = A.子ＧＯＯＰＯＮ番号 
          then case when C.発行年月日 = 0
                 then A.最終更新日
                 else C.発行年月日
               end
          else H.統合日付
        end as 発行年月日
      , A.最終更新日
      , case when カード種別 not in (504, 505, 998) 
          then 1 
          else 0 
        end as RCM連動可否 
    from
      WSＧＯＯＰＯＮ番号紐付 A
      , MSカード情報 C
      , HSカード変更情報 H
      , PS会員番号体系 P 
    where
          A.子ＧＯＯＰＯＮ番号 = H.旧ＧＯＯＰＯＮ番号
      and C.会員番号 = H.旧会員番号
      and H.統合採用会員番号 IS NULL
      and C.サービス種別 = P.サービス種別 
      and H.統合切替解除年月日=0
      and A.削除フラグ = 1
      and C.会員番号 >= P.会員番号開始 
      and C.会員番号 <= P.会員番号終了 
    UNION 
--統合によって削除となったレコードの取得（GOOPON主幹）
    select  /*+ INDEX(C IXMSCARD02) */
      A.親ＧＯＯＰＯＮ番号
      , A.子ＧＯＯＰＯＮ番号
      , A.削除フラグ
      , case when A.親ＧＯＯＰＯＮ番号 = A.子ＧＯＯＰＯＮ番号 
          then case when C.発行年月日 = 0
                 then A.最終更新日
                 else C.発行年月日
               end
          else H.統合日付
        end as 発行年月日
      , A.最終更新日
      , case when カード種別 not in (504, 505, 998) 
          then 1 
          else 0 
        end as RCM連動可否 
    from
      WSＧＯＯＰＯＮ番号紐付 A
      , MSカード情報 C
      , HSカード変更情報 H
      , PS会員番号体系 P 
    where
          A.子ＧＯＯＰＯＮ番号 = H.旧ＧＯＯＰＯＮ番号
      and C.会員番号 = H.会員番号
      and H.統合採用会員番号 IS NOT NULL
      and H.統合切替解除年月日=0
      and A.削除フラグ = 1
      and C.サービス種別 = P.サービス種別 
      and C.会員番号 >= P.会員番号開始 
      and C.会員番号 <= P.会員番号終了 
    UNION 
--統合してない会員の取得
    select  /*+ INDEX(C IXMSCARD04) */
      A.親ＧＯＯＰＯＮ番号
      , A.子ＧＯＯＰＯＮ番号
      , A.削除フラグ
      , case when C.発行年月日 = 0
          then A.最終更新日
          else C.発行年月日
        end as 発行年月日
      , A.最終更新日
      , case 
        when カード種別 not in (504, 505, 998) 
          then 1 
          else 0 
        end as RCM連動可否 
    from
      WSＧＯＯＰＯＮ番号紐付 A
      , MSカード情報 C
      , PS会員番号体系 P 
    where
          A.親ＧＯＯＰＯＮ番号 = C.ＧＯＯＰＯＮ番号
      and C.サービス種別 = P.サービス種別
      and C.会員番号 >= P.会員番号開始 
      and C.会員番号 <= P.会員番号終了 
      and NOT EXISTS ( SELECT 1 FROM HSカード変更情報 H1 WHERE A.親ＧＯＯＰＯＮ番号 = H1.ＧＯＯＰＯＮ番号)
      and NOT EXISTS ( SELECT 1 FROM HSカード変更情報 H2 WHERE A.親ＧＯＯＰＯＮ番号 = H2.旧ＧＯＯＰＯＮ番号)
      and NOT EXISTS ( SELECT 1 FROM HSカード変更情報 H3 WHERE A.子ＧＯＯＰＯＮ番号 = H3.ＧＯＯＰＯＮ番号)
      and NOT EXISTS ( SELECT 1 FROM HSカード変更情報 H4 WHERE A.子ＧＯＯＰＯＮ番号 = H4.旧ＧＯＯＰＯＮ番号)
  ) 
GROUP BY
  親ＧＯＯＰＯＮ番号
  , 子ＧＯＯＰＯＮ番号
  , 削除フラグ
  , 最終更新日
;
\o

