package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class DM_LIST_DATA extends TBLBaseDto {
  public int  C_ITEM_BLOCK  =5;
  public ItemDto select_ymd     =new ItemDto()              ; /* 抽出日                     */
  public ItemDto yoyaku_ymd   =new ItemDto()                ; /* 送信予約日                 */
  public ItemDto campaign_id=new ItemDto(30+1)          ; /* キャンペーンＩＤ           */
  public ItemDto coupon__cd    =new ItemDto()              ; /* クーポンコード             */
  public ItemDto kokyaku_no=new ItemDto(15+1)           ; /* 顧客番号                   */
  public ItemDto[] kaiin_no=new ItemDto[C_ITEM_BLOCK]; /* 会員番号１～５             */
  public ItemDto kokyaku_mesho=new ItemDto(80*3+1)      ; /* 氏名(漢字)                 */
  public ItemDto kokyaku_kana_mesho=new ItemDto(40*3+1) ; /* 氏名(カナ)                 */
  public ItemDto yubin_no=new ItemDto(10+1)             ; /* 郵便番号                   */
  public ItemDto jusho_1=new ItemDto(10*3+1)            ; /* 住所１                     */
  public ItemDto jusho_2=new ItemDto(80*3+1)            ; /* 住所２                     */
  public ItemDto jusho_3=new ItemDto(80*3+1)            ; /* 住所３                     */
  public ItemDto yubin_no_cd=new ItemDto(23+1)          ; /* 郵便番号コード             */
  public ItemDto tel_no1=new ItemDto(15+1)              ; /* 電話番号１                 */
  public ItemDto tel_no2=new ItemDto(15+1)              ; /* 電話番号２                 */
  public ItemDto tel_no3=new ItemDto(15+1)              ; /* 電話番号３                 */
  public ItemDto tel_no4=new ItemDto(15+1)              ; /* 電話番号４                 */
   /* ＤＭ止め区分１～５(理由説明) */
  public ItemDto[] dm_tome_kbn=new ItemDto[C_ITEM_BLOCK];
  public ItemDto[] taikai_ymd=new ItemDto[C_ITEM_BLOCK]   ; /* 退会年月日１～５           */
}
