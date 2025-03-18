package jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class MONTH_ZENKAKU {
    public ItemDto i_MM = new ItemDto();                      /* 集計対象月(４ヶ月)                 */
    public ItemDto     c_MMZen = new ItemDto(16);               /* 集計対象月テーブル項目名(対象月)   */
}
