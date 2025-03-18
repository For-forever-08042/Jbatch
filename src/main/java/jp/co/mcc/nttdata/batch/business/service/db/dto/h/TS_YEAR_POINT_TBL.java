package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/*******************************************************************************/
/*   名前	： ホスト変数構造体 TSポイント年別情報 定義ファイル                */
/*                 C_TS_YEAR_POINT_DATA.h                                      */
/*-----------------------------------------------------------------------------*/
/*   稼働環境                                                                  */
/*      Red Hat Enterprise Linux 5（64bit）                                    */
/*      (文字コード ： UTF8)                                                   */
/*-----------------------------------------------------------------------------*/
/*   改定履歴                                                                  */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                    */
/*-----------------------------------------------------------------------------*/
/*   $Id:$                                                                     */
/*-----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/*******************************************************************************/

/*-----------------------------------------------------------------------------*/
/*	グローバル変数                                                         */
/*-----------------------------------------------------------------------------*/
/* TSポイント年別情報構造体 */
public class TS_YEAR_POINT_TBL  extends TBLBaseDto {
    public ItemDto year = new ItemDto();                                    /* 年                             */
    public ItemDto kokyaku_no = new ItemDto(15+1);                       /* 顧客番号                       */
    public ItemDto  nenkan_fuyo_point = new ItemDto();                      /* 年間付与ポイント               */
    public ItemDto  nenkan_riyo_point = new ItemDto();                      /* 年間利用ポイント               */
    public ItemDto  nenkan_kihon_pritsu_taisho_point = new ItemDto();       /* 年間基本Ｐ率対象ポイント       */
    public ItemDto  nenkan_rankup_taisho_kingaku = new ItemDto();           /* 年間ランクＵＰ対象金額         */
    public ItemDto  nenkan_point_taisho_kingaku = new ItemDto();            /* 年間ポイント対象金額           */
    public ItemDto  nenkan_kaiage_kingaku = new ItemDto();                  /* 年間買上額                     */
    public ItemDto  nenkan_kaiage_cnt = new ItemDto();                      /* 年間買上回数                   */
    public ItemDto  nenkan_kaiage_nissu = new ItemDto();                    /* 年間買上日数                   */
    public ItemDto  nenkan_kaimonoken_hakko_point = new ItemDto();          /* 年間買物券発行ポイント         */
    public ItemDto  gekkan_fuyo_point_01 = new ItemDto();                   /* 月間付与ポイント０１           */
    public ItemDto  gekkan_fuyo_point_02 = new ItemDto();                   /* 月間付与ポイント０２           */
    public ItemDto  gekkan_fuyo_point_03 = new ItemDto();                   /* 月間付与ポイント０３           */
    public ItemDto  gekkan_fuyo_point_04 = new ItemDto();                   /* 月間付与ポイント０４           */
    public ItemDto  gekkan_fuyo_point_05 = new ItemDto();                   /* 月間付与ポイント０５           */
    public ItemDto  gekkan_fuyo_point_06 = new ItemDto();                   /* 月間付与ポイント０６           */
    public ItemDto  gekkan_fuyo_point_07 = new ItemDto();                   /* 月間付与ポイント０７           */
    public ItemDto  gekkan_fuyo_point_08 = new ItemDto();                   /* 月間付与ポイント０８           */
    public ItemDto  gekkan_fuyo_point_09 = new ItemDto();                   /* 月間付与ポイント０９           */
    public ItemDto  gekkan_fuyo_point_10 = new ItemDto();                   /* 月間付与ポイント１０           */
    public ItemDto  gekkan_fuyo_point_11 = new ItemDto();                   /* 月間付与ポイント１１           */
    public ItemDto  gekkan_fuyo_point_12 = new ItemDto();                   /* 月間付与ポイント１２           */
    public ItemDto  gekkan_riyo_point_01 = new ItemDto();                   /* 月間利用ポイント０１           */
    public ItemDto  gekkan_riyo_point_02 = new ItemDto();                   /* 月間利用ポイント０２           */
    public ItemDto  gekkan_riyo_point_03 = new ItemDto();                   /* 月間利用ポイント０３           */
    public ItemDto  gekkan_riyo_point_04 = new ItemDto();                   /* 月間利用ポイント０４           */
    public ItemDto  gekkan_riyo_point_05 = new ItemDto();                   /* 月間利用ポイント０５           */
    public ItemDto  gekkan_riyo_point_06 = new ItemDto();                   /* 月間利用ポイント０６           */
    public ItemDto  gekkan_riyo_point_07 = new ItemDto();                   /* 月間利用ポイント０７           */
    public ItemDto  gekkan_riyo_point_08 = new ItemDto();                   /* 月間利用ポイント０８           */
    public ItemDto  gekkan_riyo_point_09 = new ItemDto();                   /* 月間利用ポイント０９           */
    public ItemDto  gekkan_riyo_point_10 = new ItemDto();                   /* 月間利用ポイント１０           */
    public ItemDto  gekkan_riyo_point_11 = new ItemDto();                   /* 月間利用ポイント１１           */
    public ItemDto  gekkan_riyo_point_12 = new ItemDto();                   /* 月間利用ポイント１２           */
    public ItemDto  gekkan_rankup_taisho_kingaku_01 = new ItemDto();        /* 月間ランクＵＰ対象金額０１     */
    public ItemDto  gekkan_rankup_taisho_kingaku_02 = new ItemDto();        /* 月間ランクＵＰ対象金額０２     */
    public ItemDto  gekkan_rankup_taisho_kingaku_03 = new ItemDto();        /* 月間ランクＵＰ対象金額０３     */
    public ItemDto  gekkan_rankup_taisho_kingaku_04 = new ItemDto();        /* 月間ランクＵＰ対象金額０４     */
    public ItemDto  gekkan_rankup_taisho_kingaku_05 = new ItemDto();        /* 月間ランクＵＰ対象金額０５     */
    public ItemDto  gekkan_rankup_taisho_kingaku_06 = new ItemDto();        /* 月間ランクＵＰ対象金額０６     */
    public ItemDto  gekkan_rankup_taisho_kingaku_07 = new ItemDto();        /* 月間ランクＵＰ対象金額０７     */
    public ItemDto  gekkan_rankup_taisho_kingaku_08 = new ItemDto();        /* 月間ランクＵＰ対象金額０８     */
    public ItemDto  gekkan_rankup_taisho_kingaku_09 = new ItemDto();        /* 月間ランクＵＰ対象金額０９     */
    public ItemDto  gekkan_rankup_taisho_kingaku_10 = new ItemDto();        /* 月間ランクＵＰ対象金額１０     */
    public ItemDto  gekkan_rankup_taisho_kingaku_11 = new ItemDto();        /* 月間ランクＵＰ対象金額１１     */
    public ItemDto  gekkan_rankup_taisho_kingaku_12 = new ItemDto();        /* 月間ランクＵＰ対象金額１２     */
    public ItemDto  gekkan_kaiage_cnt_01 = new ItemDto();                   /* 月間買上回数０１               */
    public ItemDto  gekkan_kaiage_cnt_02 = new ItemDto();                   /* 月間買上回数０２               */
    public ItemDto  gekkan_kaiage_cnt_03 = new ItemDto();                   /* 月間買上回数０３               */
    public ItemDto  gekkan_kaiage_cnt_04 = new ItemDto();                   /* 月間買上回数０４               */
    public ItemDto  gekkan_kaiage_cnt_05 = new ItemDto();                   /* 月間買上回数０５               */
    public ItemDto  gekkan_kaiage_cnt_06 = new ItemDto();                   /* 月間買上回数０６               */
    public ItemDto  gekkan_kaiage_cnt_07 = new ItemDto();                   /* 月間買上回数０７               */
    public ItemDto  gekkan_kaiage_cnt_08 = new ItemDto();                   /* 月間買上回数０８               */
    public ItemDto  gekkan_kaiage_cnt_09 = new ItemDto();                   /* 月間買上回数０９               */
    public ItemDto  gekkan_kaiage_cnt_10 = new ItemDto();                   /* 月間買上回数１０               */
    public ItemDto  gekkan_kaiage_cnt_11 = new ItemDto();                   /* 月間買上回数１１               */
    public ItemDto  gekkan_kaiage_cnt_12 = new ItemDto();                   /* 月間買上回数１２               */
    public ItemDto saishu_koshin_ymd = new ItemDto();                      /* 最終更新日                     */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();                   /* 最終更新日時                   */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);          /* 最終更新プログラムＩＤ         */
}
