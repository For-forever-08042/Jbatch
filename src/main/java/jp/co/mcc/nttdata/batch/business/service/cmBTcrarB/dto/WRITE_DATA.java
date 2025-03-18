package jp.co.mcc.nttdata.batch.business.service.cmBTcrarB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class WRITE_DATA {
    int DEF_BUFSIZE15 = 15;      /* 15バイト */
 public  ItemDto   new_kokyaku_no=new ItemDto(DEF_BUFSIZE15 + 1);  /* 1 顧客番号 */
 public  ItemDto   old_kokyaku_no=new ItemDto(DEF_BUFSIZE15 + 1);  /* 2 旧顧客番号 */
}
