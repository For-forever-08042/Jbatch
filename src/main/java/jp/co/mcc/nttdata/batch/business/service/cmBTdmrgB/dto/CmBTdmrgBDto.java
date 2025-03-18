package jp.co.mcc.nttdata.batch.business.service.cmBTdmrgB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;

public class CmBTdmrgBDto {

    public MM_KOKYAKU_INFO_TBL mm_kokyaku = new MM_KOKYAKU_INFO_TBL();           /* MM顧客情報                */
    public MM_KOKYAKU_ZOKUSE_INFO_TBL mm_kokyaku_zokusei = new MM_KOKYAKU_ZOKUSE_INFO_TBL();   /* MM顧客属性情報            */
    public MS_KOKYAKU_SEDO_INFO_TBL ms_kokyaku_sedo = new MS_KOKYAKU_SEDO_INFO_TBL();      /* MS顧客制度情報            */
    public MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL mm_kokyaku_kigyo = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();/* MM顧客企業別属性情報     */
    public MS_CARD_INFO_TBL ms_card = new MS_CARD_INFO_TBL();              /* MSカード情報              */
    public MS_RANKBETSU_POINT_INFO_TBL msrk_buff = new MS_RANKBETSU_POINT_INFO_TBL();     /* MSランク別ボーナスポイント情報 */
    public TS_YEAR_POINT_TBL ts_year_point = new TS_YEAR_POINT_TBL();   /* TSポイント年別情報（work）*/
    public TS_YEAR_POINT_TBL ts_year_point_next = new TS_YEAR_POINT_TBL();   /* TSポイント年別情報（翌年）*/
    public TS_RIYO_KANO_POINT_TBL ts_riyo_kano_point = new TS_RIYO_KANO_POINT_TBL();   /* TS利用可能ポイント情報    */
    /* 2022/09/28 MCCM初版 ADD START */
    public TS_RANK_INFO_TBL ts_rank_info = new TS_RANK_INFO_TBL();   /* TSランク情報              */

    public ItemDto  h_uid = new ItemDto(15+1);             /* 顧客番号                         */
    public ItemDto str_sql = new ItemDto(4096);           /* 実行用SQL文字列                  */
    public ItemDto gh_bat_date = new ItemDto(9);          /* 取得用 バッチ処理日付前日        */
    public ItemDto gh_lastmonth = new ItemDto(9);         /* 取得用 バッチ処理日付前月(INTO)  */
    /* 2022/09/28 MCCM初版 ADD START */
    public ItemDto card_shubetsu = new ItemDto();           /* カード種別                       */
    /* 2022/09/28 MCCM初版 ADD END */

}
