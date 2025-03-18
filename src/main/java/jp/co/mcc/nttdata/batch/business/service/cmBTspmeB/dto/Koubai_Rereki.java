package jp.co.mcc.nttdata.batch.business.service.cmBTspmeB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public class Koubai_Rereki {

    public ItemDto sales_date = new ItemDto(19+1);        /* 売上日                    */
    public ItemDto company_cd = new ItemDto();              /* 会社コード                */
    public ItemDto store_cd = new ItemDto();                /* 店舗コード                */
    public ItemDto regi_no = new ItemDto(4+1);            /* レジNo                    */
    public ItemDto receipt_no = new ItemDto();              /* レシートNo                */
    public ItemDto line_num = new ItemDto();                /* 行番号                    */
    public ItemDto sales_time = new ItemDto(8+1);         /* 売上時刻                  */
    public StringDto goopn_num = new StringDto(16+1);         /* グーポン番号              */
    public ItemDto dapartmnt_cd = new ItemDto(7+1);       /* 部門コード                */
    public StringDto jan_cd = new StringDto(13+1);            /* JANコード                 */
    public StringDto item_name = new StringDto(156+1);        /* 商品名                    */
    public ItemDto sales_vol = new ItemDto();               /* 売上数量                  */
    public ItemDto sales_amount_in_tax = new ItemDto();     /* 売上金額（税込）          */
    public ItemDto arari_amount = new ItemDto();            /* 粗利金額                  */
    public ItemDto coupon_nebiki_amount = new ItemDto();    /* クーポン値引金額          */
    public ItemDto coupon_waribiki_amount = new ItemDto();  /* クーポン割引金額          */
    public ItemDto coupon_nbk_amt_kkk_cd = new ItemDto();   /* クーポン値引金額企画コード*/
    public ItemDto coupon_wbk_amt_kkk_cd = new ItemDto();   /* クーポン割引金額企画コード*/
    public ItemDto cpny_dpt_cd = new ItemDto(4+1);        /* 会社部門コード            */
    public ItemDto kejo_dpt_cd = new ItemDto(4+1);        /* 計上部門コード            */
    public ItemDto category_cd = new ItemDto(4+1);        /* カテゴリーコード          */
    public ItemDto sales_amount = new ItemDto();            /* 売上金額（税抜）          */
    public ItemDto tax_free_flg = new ItemDto();            /* 免税フラグ                */

}
