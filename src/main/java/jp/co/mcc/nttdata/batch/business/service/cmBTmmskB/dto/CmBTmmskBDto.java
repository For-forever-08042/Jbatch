package jp.co.mcc.nttdata.batch.business.service.cmBTmmskB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class CmBTmmskBDto {

    public ItemDto str_sql1 = new ItemDto(16384);                    /* 実行用SQL文字列             */
    public ItemDto  wk_col_name_sql = new ItemDto(14*3+1);            /* SQLワーク 月間ランクＵＰ対象金額部 */
    public ItemDto  date_today = new ItemDto();                         /* 当日日付                    */
    public ItemDto  date_yesterday = new ItemDto();                     /* 前日日付                    */
    public ItemDto  date_yesterday_yyyymm = new ItemDto();              /* 前日年月                    */
    public ItemDto  date_yesterday_yyyy = new ItemDto();                /* 前日年                      */
    public ItemDto  date_yesterday_mm = new ItemDto();                  /* 前日月                      */
    public ItemDto  date_yesterday_mmdd = new ItemDto();                /* 前日月日                    */

    public ItemDto h_goopon_no = new ItemDto(16+1);                  /* GOOPON番号                  */
    public ItemDto h_taisho_ym = new ItemDto(6+1);                   /* 対象年月                    */
    public ItemDto h_stage_hantei_kingaku = new ItemDto(9+1);        /* ステージ判定金額            */
    public ItemDto h_koushin_ymd = new ItemDto(8+1);                 /* 更新日                      */

}
