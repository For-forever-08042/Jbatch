package jp.co.mcc.nttdata.batch.business.service.cmBTnpskB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TS_SERCH extends TBLBaseDto {
   public ItemDto this_year = new ItemDto();      /* 当年度                              */
   public ItemDto ex_year = new ItemDto();        /* 前年度                              */
   public ItemDto expired_year = new ItemDto();   /* 失効対象年度                        */
   public ItemDto this_year_cd = new ItemDto(4);         /* 当年度コード（全角）                */
   public ItemDto ex_year_cd = new ItemDto(4);           /* 前年度コード（全角）                */
   public ItemDto expired_year_cd = new ItemDto(4);      /* 失効対象年度コード（全角）          */
   public ItemDto ex_month = new ItemDto(7);                /* 前月（全角）                        */
   public ItemDto this_month00 = new ItemDto(7);            /* 当月（全角）                        */
   public ItemDto this_month01 = new ItemDto(7);            /* 当月＋１ヶ月（全角）                */
   public ItemDto this_month02 = new ItemDto(7);            /* 当月＋２ヶ月（全角）                */
   public ItemDto this_month03 = new ItemDto(7);            /* 当月＋３ヶ月（全角）                */
}
