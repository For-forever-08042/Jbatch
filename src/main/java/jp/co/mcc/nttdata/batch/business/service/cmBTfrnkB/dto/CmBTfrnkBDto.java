package jp.co.mcc.nttdata.batch.business.service.cmBTfrnkB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_KAZOKU_SEDO_INFO_TBL;

public class CmBTfrnkBDto {

    public MS_KAZOKU_SEDO_INFO_TBL mskased_t = new MS_KAZOKU_SEDO_INFO_TBL();          /* MS家族制度情報バッファ      */

    /* 処理用 */
    public long get_cnt_buf;             /* WSバッチ処理実行管理件数           */
    public int this_date;               /* 処理日付の年月日                   */
    public int this_year;               /* 処理日付の年                       */
    public int this_month;              /* 処理日付の月                       */
    public StringDto Program_Name= new StringDto(10);        /* バージョンなしプログラム名         */
    public StringDto str_sql1= new StringDto(4096);          /* 実行用SQL文字列                    */
}
