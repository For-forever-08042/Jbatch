package jp.co.mcc.nttdata.batch.business.service.db.dto.h;
/******************************************************************************/
/*   名前： ホスト変数構造体 MSキャンペーン管理情報 定義ファイル              */
/*                   MS_CAMPAIGN_MANGE.h                                      */
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

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* MSキャンペーン管理情報構造体                                               */
/*----------------------------------------------------------------------------*/
public class MS_CAMPAIGN_MANGE_TBL  {
    public ItemDto    campaign_id=new ItemDto(30+1)              ; /* キャンペーンＩＤ     PK*/
    public ItemDto    campaign_shubetsu    =new ItemDto()  ; /* キャンペーン種別     PK*/
    public ItemDto    kigyo_cd             =new ItemDto()  ; /* 企業コード           PK*/
    public ItemDto    bu_cd                =new ItemDto()  ; /* 部コード             PK*/
    public ItemDto    zone_cd              =new ItemDto()  ; /* ゾーンコード         PK*/
    public ItemDto    block_cd             =new ItemDto()  ; /* ブロックコード       PK*/
    public ItemDto    mise_no              =new ItemDto()  ; /* 店番号               PK*/
    public ItemDto    hakko_kaishi_ymd     =new ItemDto()  ; /* 発行開始年月日         */
    public ItemDto    hakko_shuryo_ymd     =new ItemDto()  ; /* 発行終了年月日         */
    public ItemDto    riyo_kaishi_ymd      =new ItemDto()  ; /* 利用開始年月日         */
    public ItemDto    riyo_shuryo_ymd      =new ItemDto()  ; /* 利用終了年月日         */
    public ItemDto    coupon__id           =new ItemDto()  ; /* クーポンＩＤ           */
    public ItemDto    coupon__cd           =new ItemDto()  ; /* クーポンコード         */
    public ItemDto    service_naiyo        =new ItemDto()  ; /* サービス内容           */
    public ItemDto    kigyo_tenpo_set      =new ItemDto()  ; /* 企業店舗設定           */
    public ItemDto    service_tokuten      =new ItemDto()  ; /* サービス特典           */
    public ItemDto    msg_1=new ItemDto(30*3+1)                 ; /* メッセージ１           */
    public ItemDto    msg_2=new ItemDto(30*3+1)                 ; /* メッセージ２           */
    public ItemDto    msg_3=new ItemDto(30*3+1)                 ; /* メッセージ３           */
    public ItemDto    msg_4=new ItemDto(30*3+1)                 ; /* メッセージ４           */
    public ItemDto    msg_5=new ItemDto(30*3+1)                 ; /* メッセージ５           */
    public ItemDto    msg_6=new ItemDto(30*3+1)                 ; /* メッセージ６           */
    public ItemDto    msg_7=new ItemDto(30*3+1)                 ; /* メッセージ７           */
    public ItemDto    msg_8=new ItemDto(30*3+1)                 ; /* メッセージ８           */
    public ItemDto    msg_9=new ItemDto(30*3+1)                 ; /* メッセージ９           */
    public ItemDto    sagyo_kigyo_cd       =new ItemDto()  ; /* 作業企業コード         */
    public ItemDto    sagyosha_id          =new ItemDto()  ; /* 作業者ＩＤ             */
    public ItemDto    sagyo_ymd            =new ItemDto()  ; /* 作業年月日             */
    public ItemDto    sagyo_hms            =new ItemDto()  ; /* 作業時刻               */
    public ItemDto    batch_koshin_ymd     =new ItemDto()  ; /* バッチ更新日           */
    public ItemDto    saishu_koshin_ymd    =new ItemDto()  ; /* 最終更新日             */
    public ItemDto    saishu_koshin_ymdhms =new ItemDto()  ; /* 最終更新日時           */
    public ItemDto    saishu_koshin_programid=new ItemDto(20+1) ; /* 最終更新プログラムＩＤ */
}
