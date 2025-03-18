package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;


public class EMAIL_LIST_DATA extends TBLBaseDto {
    int C_ITEM_BLOCK = 5;                /* 対象項目数/1 BLOCK                 */
    public ItemDto select_ymd = new ItemDto(); /* 抽出日                     */
    public ItemDto yoyaku_ymd = new ItemDto(); /* 送信予約日                 */
    public ItemDto campaign_id = new ItemDto(30 + 1); /* キャンペーンＩＤ           */
    public ItemDto coupon__cd = new ItemDto(); /* クーポンコード             */
    public ItemDto kokyaku_no = new ItemDto(15 + 1); /* 顧客番号                   */
    public ItemDto[] kaiin_no = new ItemDto[C_ITEM_BLOCK];/* 会員番号１～５     [17+1];        */
    public ItemDto kokyaku_mesho = new ItemDto(40 * 3 + 1); /* 氏名(漢字)                 */
    public ItemDto kokyaku_kana_mesho = new ItemDto(40 + 1); /* 氏名(カナ)                 */
    public ItemDto email_address_1 = new ItemDto(60 + 1); /* メールアドレス１           */
    public ItemDto email_address_2 = new ItemDto(60 + 1); /* メールアドレス２           */
    public ItemDto email_address_3 = new ItemDto(100 + 1); /* メールアドレス３           */
    public ItemDto email_address_4 = new ItemDto(100 + 1); /* メールアドレス４           */
    /* メール止め区分１～５(理由説明) */
    public ItemDto[] email_tome_kbn = new ItemDto[C_ITEM_BLOCK];  //50*3+1
    public ItemDto[] taikai_ymd = new ItemDto[C_ITEM_BLOCK]; /* 退会年月日１～５           */

}
