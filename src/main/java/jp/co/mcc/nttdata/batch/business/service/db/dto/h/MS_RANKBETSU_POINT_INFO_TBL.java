package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前   ： ホスト変数構造体 MSランク別ボーナスポイント情報 定義ファイル   */
/*                 MS_RANKBETSU_POINT_INFO.h                                  */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/22 ISS 越後谷 ： 初版                                   */
/*     40.00 :2022/09/06 SSI.岩井   ： MCCM初版                               */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* MSランク別ボーナスポイント情報構造体                                       */
/*----------------------------------------------------------------------------*/
public class MS_RANKBETSU_POINT_INFO_TBL  extends TBLBaseDto {

    public ItemDto rank_shubetsu = new ItemDto();                     /* ランク種別          PK */
    public ItemDto rank_cd = new ItemDto();                           /* ランクコード        PK */
    public ItemDto kaishi_ymd = new ItemDto();                        /* 開始年月日          PK */
    public ItemDto shuryo_ymd = new ItemDto();                        /* 終了年月日             */
    public ItemDto settei_point_ritsu = new ItemDto();                /* 設定ポイント率         */
    public ItemDto rank_meisho_counter_hyoji = new ItemDto(40*3+1); /* ランク名称カウンタ表示 */
    public ItemDto rank_meisho_receipt_hyoji = new ItemDto(40*3+1); /* ランク名称レシート表示 */
    public ItemDto  keisan_kijun_tanigaku = new ItemDto();             /* 計算基準単位額         */
    public ItemDto  hitsuyo_kingaku = new ItemDto();                   /* 必要金額               */
    public ItemDto joi_rank_cd = new ItemDto();                       /* 上位ランクコード       */
    public ItemDto sagyo_kigyo_cd = new ItemDto();                    /* 作業企業コード         */
    public ItemDto  sagyosha_id = new ItemDto();                       /* 作業者ＩＤ             */
    public ItemDto sagyo_ymd = new ItemDto();                         /* 作業年月日             */
    public ItemDto sagyo_hms = new ItemDto();                         /* 作業時刻               */
    public ItemDto batch_koshin_ymd = new ItemDto();                  /* バッチ更新日           */
    public ItemDto saishu_koshin_ymd = new ItemDto();                 /* 最終更新日             */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();              /* 最終更新日時           */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);     /* 最終更新プログラムＩＤ */

}
