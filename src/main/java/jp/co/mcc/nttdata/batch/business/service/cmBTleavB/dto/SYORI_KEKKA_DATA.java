package jp.co.mcc.nttdata.batch.business.service.cmBTleavB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class SYORI_KEKKA_DATA {
    public ItemDto kokyaku_no = new ItemDto(15 + 1);             /*  1 顧客番号                   */
    public ItemDto       kokyaku_mesho = new ItemDto(80*3+1);        /*  2 顧客名称                   */                                                                                 /* 2022/10/14 MCCM初版 MOD */
    public ItemDto       kokyaku_kana_mesho = new ItemDto(80*3+1);   /*  3 顧客カナ名称               */                                                                                 /* 2022/10/14 MCCM初版 MOD */
    public ItemDto       nenre = new ItemDto(3+1);                   /*  4 年齢                       */
    public ItemDto       tanjo_y = new ItemDto(4+1);                 /*  5 誕生年                     */
    public ItemDto       tanjo_m = new ItemDto(2+1);                 /*  6 誕生月                     */
    public ItemDto       tanjo_d = new ItemDto(2+1);                 /*  7 誕生日                     */
    public ItemDto       sebetsu = new ItemDto(1+1);                 /*  8 性別                       */
    public ItemDto       yubin_no = new ItemDto(10+1);               /*  9 郵便番号                   */
    public ItemDto       yubin_no_cd = new ItemDto(23+1);            /* 10 郵便番号コード             */
    public ItemDto       jusho_1 = new ItemDto(10*3+1);              /* 11 住所１                     */
    public ItemDto       jusho_2 = new ItemDto(200*3+1);             /* 12 住所２                     */                                                                                 /* 2022/10/14 MCCM初版 MOD */
     //  char       jusho_3 = new ItemDto(80*3+1);              /* 13 住所３                     */                                                                                 /* 2022/10/14 MCCM初版 DEL */
    public ItemDto       denwa_no_1 = new ItemDto(15+1);             /* 14 電話番号１                 */
    public ItemDto       denwa_no_2 = new ItemDto(15+1);             /* 15 電話番号２                 */
    public ItemDto       denwa_no_3 = new ItemDto(15+1);             /* 16 電話番号３                 */
    public ItemDto       denwa_no_4 = new ItemDto(15+1);             /* 17 電話番号４                 */
    public ItemDto       kensaku_denwa_no_1 = new ItemDto(15+1);     /* 18 検索電話番号１             */
    public ItemDto       kensaku_denwa_no_2 = new ItemDto(15+1);     /* 19 検索電話番号２             */
    public ItemDto       kensaku_denwa_no_3 = new ItemDto(15+1);     /* 20 検索電話番号３             */
    public ItemDto       kensaku_denwa_no_4 = new ItemDto(15+1);     /* 21 検索電話番号４             */
    public ItemDto       email_address_1 = new ItemDto(60+1);        /* 22 Ｅメールアドレス１         */
    public ItemDto       email_address_2 = new ItemDto(60+1);        /* 23 Ｅメールアドレス２         */
    public ItemDto       shokugyo = new ItemDto(40*3+1);             /* 24 職業                       */
    public ItemDto       kinmu_kbn = new ItemDto(3+1);               /* 25 勤務区分                   */
    public ItemDto       email_address_3 = new ItemDto(100+1);       /* 26 Ｅメールアドレス３         */
    public ItemDto       email_address_4 = new ItemDto(100+1);       /* 27 Ｅメールアドレス４         */
}
