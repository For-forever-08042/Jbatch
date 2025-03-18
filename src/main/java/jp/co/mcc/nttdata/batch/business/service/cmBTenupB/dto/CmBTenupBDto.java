package jp.co.mcc.nttdata.batch.business.service.cmBTenupB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MM_MATERNITY_BABY_INFO_TBL;

public class CmBTenupBDto {

    /* 動的ＳＱＬ作成用 */
    public ItemDto str_sql = new ItemDto(2048)                ; /* 実行用SQL文字列              */

    public MM_MATERNITY_BABY_INFO_TBL mmmb_t = new MM_MATERNITY_BABY_INFO_TBL();      /* MMマタニティベビー情報バッファ    */
    public ItemDto h_kaiin_no = new ItemDto(15+1);             /* 会員番号                    */
    public ItemDto h_batdate_t = new ItemDto();                    /* バッチ処理日付(当日)        */
    public ItemDto h_batdate_y = new ItemDto();                    /* バッチ処理日付(前日)        */
    public ItemDto h_ProcKbn = new ItemDto();                 /* 処理区分                    */
    public ItemDto h_program_id = new ItemDto(21);             /* プログラムID                */

}
