package jp.co.mcc.nttdata.batch.business.service.db.dto.h;
/******************************************************************************/
/*   名前： ホスト変数構造体 MSサークル顧客情報 定義ファイル                  */
/*                 MS_CIRCLE_KOKYAKU_INFO.h                                   */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/11/10 SSI.越後谷： 初版                                    */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MSサークル顧客情報構造体 */
public class MS_CIRCLE_KOKYAKU_INFO_TBL extends TBLBaseDto {
    public ItemDto    kokyaku_no = new ItemDto(15+1);              /* 顧客番号             PK */
    public ItemDto    circle_id = new ItemDto();                     /* サークルＩＤ         PK */
    public ItemDto    nyukai_ymd = new ItemDto();                    /* 入会日                  */
    public ItemDto    taikai_ymd = new ItemDto();                    /* 退会日                  */
    public ItemDto    tel_tome_kbn = new ItemDto();                  /* ＴＥＬ止め区分          */
    public ItemDto    dm_tome_kbn = new ItemDto();                   /* ＤＭ止め区分            */
    public ItemDto    email_tome_kbn = new ItemDto();                /* Ｅメール止め区分        */
    public ItemDto    ketai_tel_tome_kbn = new ItemDto();            /* 携帯ＴＥＬ止め区分      */
    public ItemDto    ketai_email_tome_kbn = new ItemDto();          /* 携帯Ｅメール止め区分    */
    public ItemDto    nyukai_point_fuyo_ymd = new ItemDto();         /* 入会ポイント付与日      */
    public ItemDto    memo1 = new ItemDto(80*3+1);                 /* メモ１                  */
    public ItemDto    memo2 = new ItemDto(80*3+1);                 /* メモ２                  */
    public ItemDto    memo3 = new ItemDto(80*3+1);                 /* メモ３                  */
    public ItemDto    sagyo_tenpo = new ItemDto();                   /* 作業店舗                */
    public ItemDto    sagyosha_id = new ItemDto();                   /* 作業者ＩＤ              */
    public ItemDto    sagyo_ymd = new ItemDto();                     /* 作業年月日              */
    public ItemDto    sagyo_hms = new ItemDto();                     /* 作業時刻                */
    public ItemDto    batch_koshin_ymd = new ItemDto();              /* バッチ更新日            */
    public ItemDto    saishu_koshin_ymd = new ItemDto();             /* 最終更新日              */
    public ItemDto    saishu_koshin_ymdhms = new ItemDto();          /* 最終更新日時            */
    public ItemDto    saishu_koshin_programid  = new ItemDto(20+1); /* 最終更新プログラムＩＤ  */
}
