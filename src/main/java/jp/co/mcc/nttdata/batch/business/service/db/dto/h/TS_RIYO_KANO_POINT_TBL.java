package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/*******************************************************************************/
/*   名前： ホスト変数構造体 TS利用可能ポイント情報                            */
/*                 TS_RIYO_KANO_POINT_DATA.h                                   */
/*-----------------------------------------------------------------------------*/
/*   稼働環境                                                                  */
/*      Red Hat Enterprise Linux 5（64bit）                                    */
/*      (文字コード ： UTF8)                                                   */
/*-----------------------------------------------------------------------------*/
/*   改定履歴                                                                  */
/*       1.00 :2012/11/01 SSI 越後谷：初版                                     */
/*       2.00 :2013/09/13 SSI 本田  ：チャージ実施日追加                       */
/*      30.00 :2020/12/23 NDBS.緒方:通常ポイント年度管理/期間限定ポイント対応  */
/*      40.00 :2022/09/05 SSI.岩井  ：MCCM初版                                 */
/*-----------------------------------------------------------------------------*/
/*   $Id:$                                                                     */
/*-----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/*******************************************************************************/

/*-----------------------------------------------------------------------------*/
/*グローバル変数                                                               */
/*-----------------------------------------------------------------------------*/
/* TS利用可能ポイント情報 */
public class TS_RIYO_KANO_POINT_TBL  extends TBLBaseDto {

    public ItemDto kokyaku_no = new ItemDto(15+1);                       /* 顧客番号                    PK */
    /*ItemDto  riyo_kano_point = new ItemDto();*/                    /* 利用可能ポイント               */
    public ItemDto  riyo_kano_tujo_P1 = new ItemDto();                      /* 利用可能通常Ｐ１               */
    public ItemDto  riyo_kano_point_lf = new ItemDto();                     /* 利用可能ポイントＬＦ           */
    public ItemDto saishu_kaiage_ymd = new ItemDto();                      /* 最終買上日                     */
    public ItemDto nyukai_kigyo_cd = new ItemDto();                        /* 入会企業コード                 */
    public ItemDto nyukai_tenpo = new ItemDto();                           /* 入会店舗                       */
    public ItemDto nyukai_kyu_hansya_cd = new ItemDto();                   /* 入会旧販社コード               */
    public ItemDto hakken_kigyo_cd = new ItemDto();                        /* 発券企業コード                 */
    public ItemDto hakken_tenpo = new ItemDto();                           /* 発券店舗                       */
    public ItemDto  birthday_cpn_hakko_ymd = new ItemDto();                 /* バースディクーポン発行日       */
    public ItemDto  zaiseki_cpn_hakko_ymd = new ItemDto();                  /* 在籍期間クーポン発行日         */
    public ItemDto  shussan_cpn_hakko_ymd1 = new ItemDto();                 /* 出産クーポン発行日１           */
    public ItemDto  shussan_cpn_hakko_ymd2 = new ItemDto();                 /* 出産クーポン発行日２           */
    public ItemDto  shussan_cpn_hakko_ymd3 = new ItemDto();                 /* 出産クーポン発行日３           */
    public ItemDto  zenkaiin_cpn_hakko_ymd = new ItemDto();                 /* 全会員クーポン発行日           */
    public ItemDto  saishu_koshin_ymd = new ItemDto();                      /* 最終更新日                     */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();                   /* 最終更新日時                   */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);          /* 最終更新プログラムＩＤ         */
    public ItemDto  charge_jishi_ymd = new ItemDto();                       /* チャージ実施日                 */
    public ItemDto  riyo_kano_tujo_P0 = new ItemDto();              /* 利用可能通常Ｐ０                       */
    public ItemDto  riyo_kano_tujo_P2 = new ItemDto();              /* 利用可能通常Ｐ２                       */
    public ItemDto  riyo_kano_tujo_P3 = new ItemDto();              /* 利用可能通常Ｐ３                       */
    public ItemDto  riyo_kano_tujo_P4 = new ItemDto();              /* 利用可能通常Ｐ４                       */
    public ItemDto  tujo_p_shik_flg = new ItemDto();                /* 通常Ｐ失効フラグ                       */
    public ItemDto  riyo_kano_kikan_gentei_P01 = new ItemDto();     /* 利用可能期間限定Ｐ０１                 */
    public ItemDto  riyo_kano_kikan_gentei_P02 = new ItemDto();     /* 利用可能期間限定Ｐ０２                 */
    public ItemDto  riyo_kano_kikan_gentei_P03 = new ItemDto();     /* 利用可能期間限定Ｐ０３                 */
    public ItemDto  riyo_kano_kikan_gentei_P04 = new ItemDto();     /* 利用可能期間限定Ｐ０４                 */
    public ItemDto  riyo_kano_kikan_gentei_P05 = new ItemDto();     /* 利用可能期間限定Ｐ０５                 */
    public ItemDto  riyo_kano_kikan_gentei_P06 = new ItemDto();     /* 利用可能期間限定Ｐ０６                 */
    public ItemDto  riyo_kano_kikan_gentei_P07 = new ItemDto();     /* 利用可能期間限定Ｐ０７                 */
    public ItemDto  riyo_kano_kikan_gentei_P08 = new ItemDto();     /* 利用可能期間限定Ｐ０８                 */
    public ItemDto  riyo_kano_kikan_gentei_P09 = new ItemDto();     /* 利用可能期間限定Ｐ０９                 */
    public ItemDto  riyo_kano_kikan_gentei_P10 = new ItemDto();     /* 利用可能期間限定Ｐ１０                 */
    public ItemDto  riyo_kano_kikan_gentei_P11 = new ItemDto();     /* 利用可能期間限定Ｐ１１                 */
    public ItemDto  riyo_kano_kikan_gentei_P12 = new ItemDto();     /* 利用可能期間限定Ｐ１２                 */
    public ItemDto  kikan_gentei_p_shik_flg = new ItemDto();        /* 期間限定Ｐ失効フラグ                   */
    public ItemDto  riyo_kano_tujo_kobai_P0 = new ItemDto();        /* 利用可能通常購買Ｐ０                   */
    public ItemDto  riyo_kano_tujo_kobai_P1 = new ItemDto();        /* 利用可能通常購買Ｐ１                   */
    public ItemDto  riyo_kano_tujo_kobai_P2 = new ItemDto();        /* 利用可能通常購買Ｐ２                   */
    public ItemDto  riyo_kano_tujo_kobai_P3 = new ItemDto();        /* 利用可能通常購買Ｐ３                   */
    public ItemDto  riyo_kano_tujo_kobai_P4 = new ItemDto();        /* 利用可能通常購買Ｐ４                   */
    public ItemDto  riyo_kano_tujo_hikobai_P0 = new ItemDto();      /* 利用可能通常非購買Ｐ０                 */
    public ItemDto  riyo_kano_tujo_hikobai_P1 = new ItemDto();      /* 利用可能通常非購買Ｐ１                 */
    public ItemDto  riyo_kano_tujo_hikobai_P2 = new ItemDto();      /* 利用可能通常非購買Ｐ２                 */
    public ItemDto  riyo_kano_tujo_hikobai_P3 = new ItemDto();      /* 利用可能通常非購買Ｐ３                 */
    public ItemDto  riyo_kano_tujo_hikobai_P4 = new ItemDto();      /* 利用可能通常非購買Ｐ４                 */
    public ItemDto  riyo_kano_tujo_sonota_P0 = new ItemDto();       /* 利用可能通常その他Ｐ０                 */
    public ItemDto  riyo_kano_tujo_sonota_P1 = new ItemDto();       /* 利用可能通常その他Ｐ１                 */
    public ItemDto  riyo_kano_tujo_sonota_P2 = new ItemDto();       /* 利用可能通常その他Ｐ２                 */
    public ItemDto  riyo_kano_tujo_sonota_P3 = new ItemDto();       /* 利用可能通常その他Ｐ３                 */
    public ItemDto  riyo_kano_tujo_sonota_P4 = new ItemDto();       /* 利用可能通常その他Ｐ４                 */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P01 = new ItemDto(); /* 利用可能期間限定購買Ｐ０１           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P02 = new ItemDto(); /* 利用可能期間限定購買Ｐ０２           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P03 = new ItemDto(); /* 利用可能期間限定購買Ｐ０３           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P04 = new ItemDto(); /* 利用可能期間限定購買Ｐ０４           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P05 = new ItemDto(); /* 利用可能期間限定購買Ｐ０５           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P06 = new ItemDto(); /* 利用可能期間限定購買Ｐ０６           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P07 = new ItemDto(); /* 利用可能期間限定購買Ｐ０７           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P08 = new ItemDto(); /* 利用可能期間限定購買Ｐ０８           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P09 = new ItemDto(); /* 利用可能期間限定購買Ｐ０９           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P10 = new ItemDto(); /* 利用可能期間限定購買Ｐ１０           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P11 = new ItemDto(); /* 利用可能期間限定購買Ｐ１１           */
    public ItemDto  riyo_kano_kikan_gentei_kobai_P12 = new ItemDto(); /* 利用可能期間限定購買Ｐ１２           */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P01 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０１       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P02 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０２       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P03 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０３       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P04 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０４       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P05 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０５       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P06 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０６       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P07 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０７       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P08 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０８       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P09 = new ItemDto(); /* 利用可能期間限定非購買Ｐ０９       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P10 = new ItemDto(); /* 利用可能期間限定非購買Ｐ１０       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P11 = new ItemDto(); /* 利用可能期間限定非購買Ｐ１１       */
    public ItemDto  riyo_kano_kikan_gentei_hikobai_P12 = new ItemDto(); /* 利用可能期間限定非購買Ｐ１２       */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P01 = new ItemDto(); /* 利用可能期間限定その他Ｐ０１        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P02 = new ItemDto(); /* 利用可能期間限定その他Ｐ０２        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P03 = new ItemDto(); /* 利用可能期間限定その他Ｐ０３        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P04 = new ItemDto(); /* 利用可能期間限定その他Ｐ０４        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P05 = new ItemDto(); /* 利用可能期間限定その他Ｐ０５        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P06 = new ItemDto(); /* 利用可能期間限定その他Ｐ０６        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P07 = new ItemDto(); /* 利用可能期間限定その他Ｐ０７        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P08 = new ItemDto(); /* 利用可能期間限定その他Ｐ０８        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P09 = new ItemDto(); /* 利用可能期間限定その他Ｐ０９        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P10 = new ItemDto(); /* 利用可能期間限定その他Ｐ１０        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P11 = new ItemDto(); /* 利用可能期間限定その他Ｐ１１        */
    public ItemDto  riyo_kano_kikan_gentei_sonota_P12 = new ItemDto(); /* 利用可能期間限定その他Ｐ１２        */
    public ItemDto  nyukai_kaisha_cd_mcc = new ItemDto();           /* 入会会社コードＭＣＣ                   */
    public ItemDto  nyukai_tenpo_mcc = new ItemDto();               /* 入会店舗ＭＣＣ                         */
    public ItemDto  raiten_point_fuyo_ymd = new ItemDto();          /* 来店ポイント付与日                     */

}
