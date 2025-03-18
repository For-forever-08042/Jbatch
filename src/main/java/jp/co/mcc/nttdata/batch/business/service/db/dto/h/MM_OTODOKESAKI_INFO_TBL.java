package jp.co.mcc.nttdata.batch.business.service.db.dto.h;
/******************************************************************************/
/*   名前： ホスト変数構造体 MMお届け先情報 定義ファイル                      */
/*                 MM_OTODOKESAKI_INFO.h                                      */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      41.00：2023/04/26 SSI.川内：MCCMPH2                                   */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2023 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MMお届け先情報構造体 */
public class MM_OTODOKESAKI_INFO_TBL extends TBLBaseDto {
    public ItemDto   kokyaku_no = new ItemDto();                       /* 顧客番号          PK */
    public ItemDto   otodokesaki_no = new ItemDto();                   /* お届け先番号      PK */
    public ItemDto   otodokesaki_hyoji_mei = new ItemDto(40*3+1);    /* お届け先表示名       */
    public ItemDto   otodokesaki_shimei_kanji_sei = new ItemDto(40*3+1); /* お届け先氏名漢字姓 */
    public ItemDto   otodokesaki_shimei_kanji_mei = new ItemDto(40*3+1); /* お届け先氏名漢字名 */
    public ItemDto   otodokesaki_shimei_kana_sei = new ItemDto(40+1);/* お届け先氏名カナ姓   */
    public ItemDto   otodokesaki_shimei_kana_mei = new ItemDto(40+1);/* お届け先氏名カナ名   */
    public ItemDto   otodokesaki_yubin_no_1 = new ItemDto(3+1);      /* お届け先郵便番号１   */
    public ItemDto   otodokesaki_yubin_no_2 = new ItemDto(4+1);      /* お届け先郵便番号２   */
    public ItemDto   otodokesaki_todofuken = new ItemDto(2+1);       /* お届け先都道府県     */
    public ItemDto   otodokesaki_jusho = new ItemDto(400*3+1);       /* お届け先住所         */
    public ItemDto   otodokesaki_denwa_no = new ItemDto(13+1);       /* お届け先電話番号     */
    public ItemDto   otodokesaki_kaisha_mei = new ItemDto(160*3+1);  /* お届け先会社名       */
    public ItemDto   otodokesaki_busho_mei = new ItemDto(160*3+1);   /* お届け先部署名       */
    public ItemDto   default_otodokesaki_flg = new ItemDto();          /* デフォルトお届け先フラグ */
    public ItemDto   sakujo_flg = new ItemDto();                       /* 削除フラグ           */
    public ItemDto   toroku_ymdhms = new ItemDto();                    /* 登録日時             */
    public ItemDto   sagyo_kigyo_cd = new ItemDto();                   /* 作業企業コード       */
    public ItemDto   sagyosha_id = new ItemDto();                      /* 作業者ＩＤ           */
    public ItemDto   sagyo_ymd = new ItemDto();                        /* 作業年月日           */
    public ItemDto   sagyo_hms = new ItemDto();                        /* 作業時刻             */
    public ItemDto   batch_koshin_ymd = new ItemDto();                 /* バッチ更新日         */
    public ItemDto   saishu_koshin_ymd = new ItemDto();                /* 最終更新日           */
    public ItemDto   saishu_koshin_ymdhms = new ItemDto();             /* 最終更新日時         */
    public ItemDto   saishu_koshin_programid = new ItemDto(20+1);    /* 最終更新プログラムＩＤ */
}
