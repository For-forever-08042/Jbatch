package jp.co.mcc.nttdata.batch.business.service.cmBTpocdB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MM_KOKYAKU_ZOKUSE_INFO_TBL;

public class CmBTpocdBDto {

    public MM_KOKYAKU_ZOKUSE_INFO_TBL mmkozkinf_t = new MM_KOKYAKU_ZOKUSE_INFO_TBL(); /* MM顧客属性情報            */
    public ItemDto h_batdate_y = new ItemDto();                   /* バッチ処理日(前日)        */
    public ItemDto h_batdate_t = new ItemDto();                   /* バッチ処理日(当日)        */
    public ItemDto str_sql = new ItemDto(4096);                 /* 実行用SQL文字列           */

}
