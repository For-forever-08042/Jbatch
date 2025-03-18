MERGE INTO PS店舗変換マスタ
USING (
        select distinct
          C.会社コード as 旧会社コード
          , C.店番号 as 旧店番号
          , case 
            when C.店番号 = 19 
              then 2000 
            when C.店番号 = 5934 
              then 2000 
            else S2.会社コード 
            end as 新会社コード
          , case 
            when C.店番号 = 19 
              then 7500 
            when C.店番号 = 5934 
              then 5943 
            else S2.店番号 
            end as 新店番号 
          ,S2.店舗形態区分 as 新店舗形態区分
        from
          PS店表示情報ＭＣＣ C 
          left outer join ( 
            select
              ROW_NUMBER() OVER ( 
                PARTITION BY
                  T1.店番号 
                ORDER BY
                  case 
                    when T1.会社コード = 2000 
                      then 1 
                    when T1.会社コード = 2510 
                      then 2 
                    when T1.会社コード = 2520 
                      then 3 
                    when T1.会社コード = 3060 
                      then 4 
                    when T1.会社コード between 7800 and 7999 
                      then 5 
                    when T1.会社コード between 6000 and 6999 
                      then 6 
                    else 7 
                    end ASC
                  , T1.開始年月日 DESC
              ) as G_ROW
              , T1.会社コード
              , T1.店番号
              , T1.開始年月日 
              , T1.店舗形態区分
            from
              PS店表示情報ＭＣＣ T1 
            where
              T1.開始年月日 <= :1 
              and ( 
                T1.会社コード in (2000, 2510, 2520, 3060) 
                or T1.会社コード between 7800 and 7999 
                or T1.会社コード between 6000 and 6999
              )
          ) S2 
            on S2.G_ROW = 1 
            AND C.店番号 = S2.店番号 
        where
          C.会社コード = 2500 
          and (S2.店番号 is not null or C.店番号 in (19, 5934))
) VAR
ON (PS店舗変換マスタ.旧会社コード = VAR.旧会社コード
  AND PS店舗変換マスタ.旧店番号 = VAR.旧店番号)
WHEN NOT MATCHED THEN
INSERT
  (
    旧会社コード,
    旧店番号,
    新会社コード,
    新店番号,
    新店舗形態区分
  )
  VALUES
  (
    VAR.旧会社コード,
    VAR.旧店番号,
    VAR.新会社コード,
    VAR.新店番号,
    VAR.新店舗形態区分
  )
;
