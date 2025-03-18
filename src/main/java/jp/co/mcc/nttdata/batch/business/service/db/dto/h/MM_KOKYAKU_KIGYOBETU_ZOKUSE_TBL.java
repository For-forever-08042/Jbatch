package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/
/*   名前： ホスト変数構造体 MM顧客企業別属性情報 定義ファイル                */
/*                 C_MM_KOKYAKU_KIGYOBETU_ZOKUSE.h                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*      2.00 :2015/04/13 SSI.上野   ： 退会改善                               */
/*                                       仮退会年月日、仮退会解除年月日追加   */
/*     40.00 :2022/09/06 SSI.岩井   ： MCCM初版                               */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION
/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MM顧客企業別属性情報構造体 */
public class MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL  extends TBLBaseDto {
    public ItemDto kokyaku_no = new ItemDto(15+1);                 /* 顧客番号          PK */
    public ItemDto kigyo_cd = new ItemDto();                         /* 企業コード        PK */
    public ItemDto nyukai_ymd = new ItemDto();                       /* 入会年月日           */
    public ItemDto taikai_ymd = new ItemDto();                       /* 退会年月日           */
    public ItemDto tel_tome_kbn = new ItemDto();                     /* ＴＥＬ止め区分       */
    public ItemDto dm_tome_kbn = new ItemDto();                      /* ＤＭ止め区分         */
    public ItemDto email_tome_kbn = new ItemDto();                   /* Ｅメール止め区分     */
    public ItemDto ketai_tel_tome_kbn = new ItemDto();               /* 携帯ＴＥＬ止め区分   */
    public ItemDto ketai_email_tome_kbn = new ItemDto();             /* 携帯Ｅメール止め区分 */
    public ItemDto sagyo_kigyo_cd = new ItemDto();                   /* 作業企業コード       */
    public ItemDto  sagyosha_id = new ItemDto();                      /* 作業者ＩＤ           */
    public ItemDto sagyo_ymd = new ItemDto();                        /* 作業年月日           */
    public ItemDto sagyo_hms = new ItemDto();                        /* 作業時刻             */
    public ItemDto batch_koshin_ymd = new ItemDto();                 /* バッチ更新日         */
    public ItemDto saishu_koshin_ymd = new ItemDto();                /* 最終更新日           */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();             /* 最終更新日時         */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);    /* 最終更新プログラムＩＤ */
    public ItemDto kari_taikai_ymd = new ItemDto();                  /* 仮退会年月日         */
    public ItemDto kari_taikai_kaijyo_ymd = new ItemDto();           /* 仮退会解除年月日     */
}
