package jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TS_SERCH extends TBLBaseDto {
    public ItemDto next_year= new ItemDto();       /* 翌年度                              */
    public ItemDto this_year= new ItemDto();       /* 当年度                              */
    public ItemDto ex_year= new ItemDto();         /* 当年度                              */
    public ItemDto next_year_cd= new ItemDto();    /* 翌年度コード（全角）                 */
    public ItemDto this_year_cd= new ItemDto();    /* 当年度コード（全角）                 */
    public ItemDto ex_year_cd= new ItemDto();      /* 前年度コード（全角）                 */
    public ItemDto month01= new ItemDto();         /*  対象月1(全角)                       */
    public ItemDto month02= new ItemDto();         /*  対象月2(全角)                       */
    public ItemDto month03= new ItemDto();         /*  対象月3(全角)                       */
    public ItemDto month04= new ItemDto();         /*  対象月4(全角)                       */
    public ItemDto month05= new ItemDto();         /*  対象月5(全角)                       */
}
