package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class CAMPAIGN_KOKYAKU_DATA extends TBLBaseDto {
    public ItemDto campaign_id=new ItemDto(30+1)           ; /* キャンペーンＩＤ           */
    public ItemDto kokyaku_no=new ItemDto(15+1)            ; /* 顧客番号                   */
    public ItemDto lf=new ItemDto(1)                      ; /* 改行                       */
}
