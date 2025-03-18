package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前： ホスト変数構造体 MS理由情報 定義ファイル                          */
/*                   MS_RIYU_INFO.h                                           */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/11/30 SSI.越後谷  ： 初版                                  */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* MS理由情報構造体                                                           */
/*----------------------------------------------------------------------------*/
public class MS_RIYU_INFO_TBL  extends TBLBaseDto {
    public ItemDto riyu_cd = new ItemDto()                       ; /* 理由コード           PK*/
    public ItemDto riyu_setumei = new ItemDto(100*3+1)         ; /* 理由説明               */
    public ItemDto riyu_flg = new ItemDto()                      ; /* フラグ                 */
}
