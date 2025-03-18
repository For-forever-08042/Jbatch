package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/*******************************************************************************/
/*   名前： ホスト変数構造体 TSポイント累計情報 定義ファイル                   */
/*                 C_TOTAL_POINT_DATA.h                                        */
/*-----------------------------------------------------------------------------*/
/*   稼働環境                                                                  */
/*      Red Hat Enterprise Linux 5（64bit）                                    */
/*      (文字コード ： UTF8)                                                   */
/*-----------------------------------------------------------------------------*/
/*   改定履歴                                                                  */
/*       1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*      30.00 :2021/01/05 NBS   亀谷 ： 項目追加                               */
/*-----------------------------------------------------------------------------*/
/*   $Id:$                                                                     */
/*-----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/*******************************************************************************/

/*-----------------------------------------------------------------------------*/
/*グローバル変数                                                             */
/*-----------------------------------------------------------------------------*/
/* TSポイント累計情報構造体 */
public class TS_TOTAL_POINT_TBL  extends TBLBaseDto {

    public ItemDto kokyaku_no = new ItemDto(15+1);                       /* 顧客番号                       */
    public ItemDto ruike_fuyo_point = new ItemDto();                       /* 累計付与ポイント               */
    public ItemDto ruike_riyo_point = new ItemDto();                       /* 累計利用ポイント               */
    public ItemDto ruike_kihon_pritsu_taisho_point = new ItemDto();        /* 累計基本Ｐ率対象ポイント       */
    public ItemDto ruike_rankup_taisho_kingaku = new ItemDto();            /* 累計ランクＵＰ対象金額         */
    public ItemDto ruike_point_taisho_kingaku = new ItemDto();             /* 累計ポイント対象金額           */
    public ItemDto ruike_kaiage_kingaku = new ItemDto();                   /* 累計買上額                     */
    public ItemDto ruike_kaiage_cnt = new ItemDto();                       /* 累計買上回数                   */
    public ItemDto ruike_kaiage_nissu = new ItemDto();                     /* 累計買上日数                   */
    public ItemDto  saishu_koshin_ymd = new ItemDto();                      /* 最終更新日                     */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();                   /* 最終更新日時                   */
    public ItemDto  saishu_koshin_programid = new ItemDto(20+1);          /* 最終更新プログラムＩＤ         */
    public ItemDto  ruike_fuyo_tsujo_point = new ItemDto();                 /* 累計付与通常Ｐ                 */
    public ItemDto  ruike_riyo_tsujo_point = new ItemDto();                 /* 累計利用通常Ｐ                 */
    public ItemDto  ruike_fuyo_kikan_gentei_point = new ItemDto();          /* 累計付与期間限定Ｐ             */
    public ItemDto  ruike_riyo_kikan_gentei_point = new ItemDto();          /* 累計利用期間限定Ｐ                 */

}
