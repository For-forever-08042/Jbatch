package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/
/*   名前： ホスト変数構造体 MSカード情報 定義ファイル                        */
/*                 MS_CARD_INFO.h                                             */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*       1.00 :2012/11/02 ISS 越後谷 ： 初版                                  */
/*      40.00 :2022/09/05 SSI.岩井   ： MCCM初版                              */
/*----------------------------------------------------------------------------*/
/*   $Id:$                                                                    */
/*----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                  */

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* MSカード情報構造体                                                         */
/*----------------------------------------------------------------------------*/
public class MS_CARD_INFO_TBL  extends TBLBaseDto {
    public ItemDto service_shubetsu = new ItemDto();       /* サービス種別      PK */
    public ItemDto  kaiin_no = new ItemDto(18+1);         /* 会員番号          PK */
    public ItemDto  kokyaku_no = new ItemDto(15+1);       /* 顧客番号             */
    public ItemDto   goopon_no = new ItemDto(16+1);        /* ＧＯＯＰＯＮ番号     */
    public ItemDto  card_status = new ItemDto();            /* カードステータス     */
    public ItemDto  riyu_cd = new ItemDto();                /* 理由コード           */
    public ItemDto  hakko_ymd = new ItemDto();              /* 発行年月日           */
    public ItemDto  shuryo_ymd = new ItemDto();             /* 終了年月日           */
    public ItemDto  kigyo_cd = new ItemDto();               /* 企業コード           */
    public ItemDto  kyu_hansya_cd = new ItemDto();          /* 旧販社コード         */
    public ItemDto  sagyo_kigyo_cd = new ItemDto();         /* 作業企業コード       */
    public ItemDto sagyosha_id = new ItemDto();            /* 作業者ＩＤ           */
    public ItemDto  sagyo_ymd = new ItemDto();              /* 作業年月日           */
    public ItemDto  sagyo_hms = new ItemDto();              /* 作業時刻             */
    public ItemDto  batch_koshin_ymd = new ItemDto();       /* バッチ更新日         */
    public ItemDto  saishu_koshin_ymd = new ItemDto();      /* 最終更新日           */
    public ItemDto saishu_koshin_ymdhms = new ItemDto();   /* 最終更新日時         */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1); /* 最終更新プログラムID */
}
