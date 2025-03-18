package jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TERA_OUT_REC extends TBLBaseDto {
    public ItemDto[] offset=new ItemDto[255];                            /* オフセット */
    public ItemDto[] length=new ItemDto[255];                            /* レングス */
    public ItemDto[] dattyp=new ItemDto[255];       /* データタイプ */
    public ItemDto[] komoku=new ItemDto[255];      /* 項目名 */
    public ItemDto[] sjiscv=new ItemDto[255];                            /* SJIS変換する場合は１ */
    public ItemDto[] encdec=new ItemDto[255];                            /* 暗号復号する場合は１ */
}
