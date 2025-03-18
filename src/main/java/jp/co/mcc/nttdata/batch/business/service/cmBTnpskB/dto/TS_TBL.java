package jp.co.mcc.nttdata.batch.business.service.cmBTnpskB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/**
 * TS利用可能ポイント情報用
 */
public class TS_TBL extends TBLBaseDto {
    public ItemDto vr_kokyaku_no = new ItemDto(16);  /* 顧客番号                      */
    public ItemDto tujoP_tonen = new ItemDto();      /* 利用可能通常Ｐ当年度           */
    public ItemDto tujoP_zennen = new ItemDto();     /* 利用可能通常Ｐ前年度           */
    public ItemDto tujoP_expired = new ItemDto();    /* 利用可能通常Ｐ失効対象年度     */
    public ItemDto tujo_p_shiko_flg = new ItemDto(); /* 通常Ｐ失効フラグ               */
    public ItemDto nyukai_kigyo_cd = new ItemDto();  /* 入会企業コード                 */
    public ItemDto nyukai_tenpo = new ItemDto();     /* 入会店舗                       */
    public ItemDto hakken_kigyo_cd = new ItemDto();  /* 発券企業コード                 */
    public ItemDto hakken_tenpo = new ItemDto();     /* 発券店舗                       */
    public ItemDto kikanP_ex = new ItemDto();        /* 利用可能期間限定Ｐ前月         */
    public ItemDto kikanP_this00 = new ItemDto();    /* 利用可能期間限定Ｐ当月         */
    public ItemDto kikanP_this01 = new ItemDto();    /* 利用可能期間限定Ｐ当月＋１     */
    public ItemDto kikanP_this02 = new ItemDto();    /* 利用可能期間限定Ｐ当月＋２     */
    public ItemDto kikanP_this03 = new ItemDto();    /* 利用可能期間限定Ｐ当月＋３     */
    public ItemDto kikan_p_shikko_flg = new ItemDto();  /* 期間限定Ｐ失効フラグ        */
    /* 2022/12/01 MCCM初版 ADD START */
    public ItemDto shikko_kobai_p = new ItemDto();   /* 利用可能通常購買Ｐ失効対象年度   */
    public ItemDto shikko_hikobai_p = new ItemDto(); /* 利用可能通常非購買Ｐ失効対象年度 */
    public ItemDto shikko_sonota_p = new ItemDto();  /* 利用可能通常その他Ｐ失効対象年度 */
    public ItemDto nyukai_kigyo_cd_mcc = new ItemDto(); /* 入会会社コードＭＣＣ        */
    public ItemDto nyukai_tenpo_mcc = new ItemDto();    /* 入会店舗ＭＣＣ              */
    /* 2022/12/01 MCCM初版 ADD END */
}
