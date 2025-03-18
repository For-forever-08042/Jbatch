package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前： ホスト変数構造体 MMマタニティベビー情報 定義ファイル              */
/*                 MM_MATERNITY_BABY_INFO.h                                   */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/11/07 SSI.越後谷  ： 初版                                  */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MMマタニティベビー情報 */
public class MM_MATERNITY_BABY_INFO_TBL {

    public ItemDto kokyaku_no = new ItemDto(15+1);              /* 顧客番号             PK */
    public ItemDto nyukai_ymd = new ItemDto();                    /* 入会年月日              */
    public ItemDto taikai_ymd = new ItemDto();                    /* 退会年月日              */
    public ItemDto kaiin_shubetsu = new ItemDto();                /* 会員種別                */
    public ItemDto dai1shi_shussan_yotei_ymd = new ItemDto();     /* 第１子出産予定日        */
    public ItemDto dai1shi_mesho = new ItemDto(40*3+1);         /* 第１子名称              */
    public ItemDto dai1shi_kana_mesho = new ItemDto(20*3+1);    /* 第１子カナ名称          */
    public ItemDto dai1shi_sebetsu = new ItemDto();               /* 第１子性別              */
    public ItemDto dai1shi_nenre = new ItemDto();                 /* 第１子年齢              */
    public ItemDto dai1shi_se_ymd = new ItemDto();                /* 第１子生年月日          */
    public ItemDto dai2shi_shussan_yotei_ymd = new ItemDto();     /* 第２子出産予定日        */
    public ItemDto dai2shi_mesho = new ItemDto(40*3+1);         /* 第２子名称              */
    public ItemDto dai2shi_kana_mesho = new ItemDto(20*3+1);    /* 第２子カナ名称          */
    public ItemDto dai2shi_sebetsu = new ItemDto();               /* 第２子性別              */
    public ItemDto dai2shi_nenre = new ItemDto();                 /* 第２子年齢              */
    public ItemDto dai2shi_se_ymd = new ItemDto();                /* 第２子生年月日          */
    public ItemDto dai3shi_shussan_yotei_ymd = new ItemDto();     /* 第３子出産予定日        */
    public ItemDto dai3shi_mesho = new ItemDto(40*3+1);         /* 第３子名称              */
    public ItemDto dai3shi_kana_mesho = new ItemDto(20*3+1);    /* 第３子カナ名称          */
    public ItemDto dai3shi_sebetsu = new ItemDto();               /* 第３子性別              */
    public ItemDto dai3shi_nenre = new ItemDto();                 /* 第３子年齢              */
    public ItemDto dai3shi_se_ymd = new ItemDto();                /* 第３子生年月日          */
    public ItemDto tel_tome_kbn = new ItemDto();                  /* ＴＥＬ止め区分          */
    public ItemDto dm_tome_kbn = new ItemDto();                   /* ＤＭ止め区分            */
    public ItemDto email_tome_kbn = new ItemDto();                /* Ｅメール止め区分        */
    public ItemDto ketai_tel_tome_kbn = new ItemDto();            /* 携帯ＴＥＬ止め区分      */
    public ItemDto ketai_email_tome_kbn = new ItemDto();          /* 携帯Ｅメール止め区分    */
    public ItemDto yuko_kigen_ymd = new ItemDto();                /* 有効期限                */
    public ItemDto sakujo_flg = new ItemDto();                    /* 削除フラグ              */
    public ItemDto sagyo_kigyo_cd = new ItemDto();                /* 作業企業コード          */
    public ItemDto sagyosha_id = new ItemDto();                   /* 作業者ＩＤ              */
    public ItemDto sagyo_ymd = new ItemDto();                     /* 作業年月日              */
    public ItemDto sagyo_hms = new ItemDto();                     /* 作業時刻                */
    public ItemDto batch_koshin_ymd = new ItemDto();              /* バッチ更新日            */
    public ItemDto saishu_koshin_ymd = new ItemDto();             /* 最終更新日              */
    public ItemDto saishu_koshin_ymdhms = new ItemDto();          /* 最終更新日時            */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1); /* 最終更新プログラムＩＤ  */

}
