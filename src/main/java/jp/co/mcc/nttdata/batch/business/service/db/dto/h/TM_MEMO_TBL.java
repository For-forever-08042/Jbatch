package jp.co.mcc.nttdata.batch.business.service.db.dto.h;
/******************************************************************************/
/*   名前： ホスト変数構造体 TMメモ 定義ファイル                              */
/*                 TM_MEMO.h                                                  */
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
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* TMメモ構造体 */
public class TM_MEMO_TBL extends TBLBaseDto {

    public ItemDto kokyaku_no = new ItemDto(15+1);             /* 顧客番号             PK */
    public ItemDto      memo1 = new ItemDto(60*3+1);                 /* メモ１                  */
    public ItemDto      memo2 = new ItemDto(60*3+1);                 /* メモ２                  */
    public ItemDto      memo3 = new ItemDto(60*3+1);                 /* メモ３                  */
    public ItemDto      memo4 = new ItemDto(60*3+1);                 /* メモ４                  */
    public ItemDto      memo5 = new ItemDto(60*3+1);                 /* メモ５                  */
    public ItemDto    sakujo_flg = new ItemDto();                    /* 削除フラグ              */
    public ItemDto    sagyo_kigyo_cd = new ItemDto();                /* 作業企業コード          */
    public ItemDto    sagyosha_id = new ItemDto();                   /* 作業者ＩＤ              */
    public ItemDto    sagyo_ymd = new ItemDto();                     /* 作業年月日              */
    public ItemDto    sagyo_hms = new ItemDto();                     /* 作業時刻                */
    public ItemDto    batch_koshin_ymd = new ItemDto();              /* バッチ更新日            */
    public ItemDto    saishu_koshin_ymd = new ItemDto();             /* 最終更新日              */
    public ItemDto    saishu_koshin_ymdhms = new ItemDto();          /* 最終更新日時            */
    public ItemDto    saishu_koshin_programid= new ItemDto(20+1); /* 最終更新プログラムＩＤ  */
}
