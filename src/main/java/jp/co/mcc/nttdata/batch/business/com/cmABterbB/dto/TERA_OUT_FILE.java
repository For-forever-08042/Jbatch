package jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TERA_OUT_FILE extends TBLBaseDto {
    public ItemDto rec_len = new ItemDto(); /* レコード長       */
    public ItemDto fld_cnt = new ItemDto(); /* フィールド数     */
    public ItemDto rec_cnt = new ItemDto(); /* 出力レコード件数 */
}
