package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class AS_MKSOUGO_SHUKEI_INFO_TBL extends TBLBaseDto {
    public ItemDto shukei_ymd = new ItemDto();                 /* 集計対象月                 */
    public ItemDto kyu_hansya_cd = new ItemDto();              /* 旧販社コード               */
    public ItemDto mise_no = new ItemDto();                    /* 店番号                     */
    public ItemDto kyu_hansya_kanji = new ItemDto(10 * 3 + 1);   /* 旧販社名                   */
    public ItemDto mise_kanji = new ItemDto(40 * 3 + 1);         /* 店舗名                     */
    public ItemDto kaiage_cnt = new ItemDto();                 /* 買上取引数                 */
    public ItemDto sales = new ItemDto();                      /* 売上金額                   */
    public ItemDto kaiage_mk_point = new ItemDto();            /* 買上ＭＫ付与ポイント数     */
    public ItemDto chousei_hikitori = new ItemDto();           /* 調整引取数                 */
    public ItemDto kigyou_mkp = new ItemDto();                 /* 企業調整ＭＫ付与ポイント数 */
    public ItemDto kokyaku_mkp = new ItemDto();                /* 顧客調整ＭＫ付与ポイント数 */
    public ItemDto total_mkp = new ItemDto();                  /* 合計ＭＫ付与ポイント数     */
}
