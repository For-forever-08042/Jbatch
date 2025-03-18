package jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
/* *****************************************************************************/
/*   名前： ホスト変数構造体 AS期間限定ポイント集計情報 定義ファイル          */
/*                 AS_KIKANGENTEI_INFO.h                                      */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*       1.00 :2021/02/26 SSI 上野 ： 初版                                    */
/*       1.01 :2021/08/13 NBS 山本 ： CF-486　売上金額合計を追加              */
/*       1.02 :2021/09/13 NBS 山本 ： CF-550　期間限定利用Ｐ前月を追加        */
/*----------------------------------------------------------------------------*/
/*   $Id:$                                                                    */
/*----------------------------------------------------------------------------*/
/*   Copyright (C) 2020 NTT DATA CORPORATION                                  */
/* *****************************************************************************/
/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* AS期間限定ポイント集計情報構造体 */
public class AS_KIKANGENTEI_INFO_TBL {
    public  ItemDto shukei_ymd = new ItemDto();             /* 集計対象月                     */
    public  ItemDto  kyu_hansya_cd = new ItemDto();          /* 旧販社コード                   */
    public  ItemDto  mise_no = new ItemDto();                /* 店番号                         */
    public  ItemDto  fuyo_gentei_point0 = new ItemDto();     /* 付与期間限定Ｐ０               */
    public  ItemDto  kaiage_point0 = new ItemDto();          /* 買上Ｐ数Ｐ０                   */
    public  ItemDto  babycircle_point0 = new ItemDto();      /* ベビーサークルＰ数Ｐ０         */
    public  ItemDto  watashiplus_point0 = new ItemDto();     /* ワタシプラス連携付与Ｐ数Ｐ０   */
    public  ItemDto  app_koukan_point0 = new ItemDto();      /* アプリＰ交換Ｐ数Ｐ０           */
    public  ItemDto  catalina_point0 = new ItemDto();        /* カタリナ付与Ｐ数Ｐ０           */
    public  ItemDto  sonota_point0 = new ItemDto();          /* その他付与Ｐ数Ｐ０             */
    public  ItemDto  chosei_point0 = new ItemDto();          /* 調整Ｐ数Ｐ０                   */
    public  ItemDto  fuyo_gentei_point1 = new ItemDto();     /* 付与期間限定Ｐ１               */
    public  ItemDto  kaiage_point1 = new ItemDto();          /* 買上Ｐ数Ｐ１                   */
    public  ItemDto  babycircle_point1 = new ItemDto();      /* ベビーサークルＰ数Ｐ１         */
    public  ItemDto  watashiplus_point1 = new ItemDto();     /* ワタシプラス連携付与Ｐ数Ｐ１   */
    public  ItemDto  app_koukan_point1 = new ItemDto();      /* アプリＰ交換Ｐ数Ｐ１           */
    public  ItemDto  catalina_point1 = new ItemDto();        /* カタリナ付与Ｐ数Ｐ１           */
    public  ItemDto  sonota_point1 = new ItemDto();          /* その他付与Ｐ数Ｐ１             */
    public  ItemDto  chosei_point1 = new ItemDto();          /* 調整Ｐ数Ｐ１                   */
    public  ItemDto  fuyo_gentei_point2 = new ItemDto();     /* 付与期間限定Ｐ２               */
    public  ItemDto  kaiage_point2 = new ItemDto();          /* 買上Ｐ数Ｐ２                   */
    public  ItemDto  babycircle_point2 = new ItemDto();      /* ベビーサークルＰ数Ｐ２         */
    public  ItemDto  watashiplus_point2 = new ItemDto();     /* ワタシプラス連携付与Ｐ数Ｐ２   */
    public  ItemDto  app_koukan_point2 = new ItemDto();      /* アプリＰ交換Ｐ数Ｐ２           */
    public  ItemDto  catalina_point2 = new ItemDto();        /* カタリナ付与Ｐ数Ｐ２           */
    public  ItemDto  sonota_point2 = new ItemDto();          /* その他付与Ｐ数Ｐ２             */
    public  ItemDto  chosei_point2 = new ItemDto();          /* 調整Ｐ数Ｐ２                   */
    public  ItemDto  fuyo_gentei_point3 = new ItemDto();     /* 付与期間限定Ｐ３               */
    public  ItemDto  kaiage_point3 = new ItemDto();          /* 買上Ｐ数Ｐ３                   */
    public  ItemDto  babycircle_point3 = new ItemDto();      /* ベビーサークルＰ数Ｐ３         */
    public  ItemDto  watashiplus_point3 = new ItemDto();     /* ワタシプラス連携付与Ｐ数Ｐ３   */
    public  ItemDto  app_koukan_point3 = new ItemDto();      /* アプリＰ交換Ｐ数Ｐ３           */
    public  ItemDto  catalina_point3 = new ItemDto();        /* カタリナ付与Ｐ数Ｐ３           */
    public  ItemDto  sonota_point3 = new ItemDto();          /* その他付与Ｐ数Ｐ３             */
    public  ItemDto  chosei_point3 = new ItemDto();          /* 調整Ｐ数Ｐ３                   */
    public  ItemDto  fuyo_sum = new ItemDto();               /* 付与期間限定合計               */
    public  ItemDto  riyou_sum0 = new ItemDto();             /* 利用期間限定Ｐ０               */
    public  ItemDto  riyou_sum1 = new ItemDto();             /* 利用期間限定Ｐ１               */
    public  ItemDto  riyou_sum2 = new ItemDto();             /* 利用期間限定Ｐ２               */
    public  ItemDto  riyou_sum3 = new ItemDto();             /* 利用期間限定Ｐ３               */
    public  ItemDto  riyou_sum = new ItemDto();              /* 利用期間限定合計               */
    public  ItemDto  clear_pointkigen = new ItemDto();       /* クリアＰ数有効期限             */
    public  ItemDto  clear_pointtaikai = new ItemDto();      /* クリアＰ数顧客退会             */
    public  ItemDto  clear_sum = new ItemDto();              /* クリアＰ数合計                 */
    public  ItemDto  end_pointp1 = new ItemDto();            /* 月末Ｐ残高Ｐ１                 */
    public  ItemDto  end_pointp2 = new ItemDto();            /* 月末Ｐ残高Ｐ２                 */
    public  ItemDto  end_pointp3 = new ItemDto();            /* 月末Ｐ残高Ｐ３                 */
    public  ItemDto  end_point = new ItemDto();              /* 月末Ｐ残高                     */
    public  ItemDto  uriagekingaku_sum = new ItemDto();      /* 売上金額合計                   */
    public  ItemDto  riyou_sum00 = new ItemDto();            /* 利用期間限定Ｐ－１             */
}
