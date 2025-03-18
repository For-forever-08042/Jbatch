package jp.co.mcc.nttdata.batch.business.service.cmBTnnrgB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_YEAR_POINT_TBL;

public class CmBTnnrgBDto {

    public TS_YEAR_POINT_TBL ts_year_point = new TS_YEAR_POINT_TBL()     ;   /* TSポイント年別情報（work）*/
    public TS_YEAR_POINT_TBL ts_year_point_next = new TS_YEAR_POINT_TBL();   /* TSポイント年別情報（翌年）*/
    public ItemDto  h_uid = new ItemDto(15+1);             /* 顧客番号                         */
    public ItemDto  str_sql = new ItemDto(4096);           /* 実行用SQL文字列                  */
    public ItemDto  gh_bat_date = new ItemDto(9);          /* 取得用 バッチ処理日付前日        */
    public ItemDto  h_batch_secno = new ItemDto();           /* バッチ処理実行シーケンス番号     */
    public ItemDto  h_this_year = new ItemDto();             /* バッチ更新日前日の当年           */
    public ItemDto  h_next_year = new ItemDto();             /* バッチ更新日前日の翌年           */
    public ItemDto h_last_upd_day = new ItemDto();          /* TSポイント年別情報の最終更新日   */
    public ItemDto  h_kino_id = new ItemDto(7);            /* 機能ＩＤ                         */

}
