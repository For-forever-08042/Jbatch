package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
/******************************************************************************/
/*   名前： ホスト変数構造体 MSキャンペーン情報 定義ファイル                  */
/*                   MS_CAMPAIGN_INFO.h                                       */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/11/28 SSI.越後谷  ： 初版                                  */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */
/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* MSキャンペーン情報構造体                                                   */
/*----------------------------------------------------------------------------*/
public class MS_CAMPAIGN_INFO_TBL {
   public ItemDto campaign_id=new ItemDto(30+1)             ; /* キャンペーンＩＤ     PK*/
   public ItemDto    campaign_shubetsu=new ItemDto()             ; /* キャンペーン種別     PK*/
   public ItemDto    campaign_name=new ItemDto(100*3+1)        ; /* キャンペーン名称       */
   public ItemDto    yoyaku_ymd            =new ItemDto()  ; /* 予約日                 */
   public ItemDto    torikomi_ymd          =new ItemDto()  ; /* 取込日                 */
   public ItemDto    rendozumi_flg         =new ItemDto()  ; /* 連動済みフラグ         */
   public ItemDto    sagyo_kigyo_cd        =new ItemDto()  ; /* 作業企業コード         */
   public ItemDto    sagyosha_id           =new ItemDto()  ; /* 作業者ＩＤ             */
   public ItemDto    sagyo_ymd             =new ItemDto()  ; /* 作業年月日             */
   public ItemDto    sagyo_hms             =new ItemDto()  ; /* 作業時刻               */
   public ItemDto    batch_koshin_ymd      =new ItemDto()  ; /* バッチ更新日           */
   public ItemDto    saishu_koshin_ymd     =new ItemDto()  ; /* 最終更新日             */
   public ItemDto    saishu_koshin_ymdhms  =new ItemDto()  ; /* 最終更新日時           */
   public ItemDto    saishu_koshin_programid=new ItemDto(20+1) ; /* 最終更新プログラムＩＤ */
}
