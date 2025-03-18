package jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TERA_PARA extends TBLBaseDto {
    public ItemDto in_file_name   = new ItemDto(9+1);           /* パラメータファイル名 */
    public ItemDto out_file_name  = new ItemDto(1000); /* 出力ファイル名 */
    public ItemDto table_name_ymd = new ItemDto(9);           /* テーブル名用日付 */
    public ItemDto ymd            = new ItemDto(9);                      /* 日付 */
    public ItemDto corp_cd        = new ItemDto(10+1);  /* 企業 */
    public ItemDto com_param      = new ItemDto(1000);             /* 汎用パラメータ */
    public ItemDto db_kbn         = new ItemDto(2+1);                 /* ＤＢ区分 */
    public char[] kaigyo_kbn     = new char[2];               /* 改行指定 -Aの場合、0x0aがセットされる */
}
