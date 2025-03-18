package jp.co.mcc.nttdata.batch.business.service.cmBTleavB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class RANK_UP_MONEY extends TBLBaseDto {
   public ItemDto[]        year_buf = new ItemDto[10]  ;          /* 年間家族ランクＵＰ金額[配列]       */
   public ItemDto[]      month0_buf = new ItemDto[12];          /* 月間家族ランクＵＰ金額[配列]偶数年 */
   public ItemDto[]      month1_buf = new ItemDto[12];          /* 月間家族ランクＵＰ金額[配列]奇数年 */
}
