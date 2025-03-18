package jp.co.mcc.nttdata.batch.business.service.cmBTecenB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileReadBaseDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

/* 入力ファイルの構造体 */
/* 入力ファイルに変更があれば、要修正箇所 */
public class EC_RENDO_DATA extends FileReadBaseDto {
    public ItemDto kaiin_no = new ItemDto(15 + 1);                /* 会員番号      */
    public ItemDto    tenpo_no = new ItemDto(6 + 1);                 /* 店舗番号      */
    public ItemDto    kanji_name = new ItemDto(80 + 1);              /* 会員氏名（漢字） */
    public ItemDto    kana_name = new ItemDto(80 + 1);               /* 会員氏名（カナ） */
    public ItemDto    zip = new ItemDto(8 + 1);                      /* 郵便番号      */
    public ItemDto    address1 = new ItemDto(10 + 1);                /* 住所１        */
    public ItemDto    address2 = new ItemDto(200 + 1);               /* 住所２        */
    public ItemDto    address3 = new ItemDto(80 + 1);                /* 住所３        */
    public ItemDto    telephone1 = new ItemDto(15 + 1);              /* 電話番号１    */
    public ItemDto    telephone2 = new ItemDto(15 + 1);              /* 電話番号２    */
    public ItemDto    telephone3 = new ItemDto(15 + 1);              /* 電話番号３    */
    public ItemDto    telephone4 = new ItemDto(15 + 1);              /* 電話番号４    */
    public ItemDto    birth_date = new ItemDto(8 + 1);               /* 生年月日      */
    public ItemDto    seibetsu = new ItemDto(1 + 1);                 /* 性別          */
    public ItemDto    last_upd_date_yyyymmddhhmmss = new ItemDto(19+ 1);/* 最終更新日時 */
    public ItemDto    nyukaibi_yyyymmdd = new ItemDto(8 + 1);        /* 入会日        */
    public ItemDto    taikaibi_yyyymmdd = new ItemDto(8 + 1);        /* 退会日        */
    public ItemDto    rendoubi_yyyymmdd = new ItemDto(8 + 1);        /* 登録日        */
    public ItemDto    email_zyushin_kahi = new ItemDto(5 + 1);       /* e-mail受信可否 */
    public ItemDto    dm_zyushin_kahi = new ItemDto(5 + 1);          /* ＤＭ受信可否  */
}
