package jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class TS_TBL {
    public ItemDto vr_kokyaku_no = new ItemDto();      /* 顧客番号                     */
    public ItemDto yokunen_p = new ItemDto();          /* 利用可能通常Ｐ翌年度          */
    public ItemDto tonen_p = new ItemDto();            /* 利用可能通常Ｐ当年度          */
    public ItemDto zennen_p = new ItemDto();           /* 利用可能通常Ｐ前年度          */
    public ItemDto tujo_p_shiko_flg = new ItemDto();   /* 通常Ｐ失効フラグ              */
    public ItemDto nyukai_kigyo_cd = new ItemDto();    /* 入会企業コード                */
    public ItemDto nyukai_tenpo = new ItemDto();       /* 入会店舗                     */
    public ItemDto hakken_kigyo_cd = new ItemDto();    /* 発券企業コード                */
    public ItemDto hakken_tenpo = new ItemDto();       /* 発券店舗                      */
    public ItemDto kikan_p01 = new ItemDto();          /* 利用可能期間限定Ｐ当月         */
    public ItemDto kikan_p02 = new ItemDto();          /* 利用可能期間限定Ｐ当月＋１     */
    public ItemDto kikan_p03 = new ItemDto();          /* 利用可能期間限定Ｐ当月＋２     */
    public ItemDto kikan_p04 = new ItemDto();          /* 利用可能期間限定Ｐ当月＋３     */
    public ItemDto kikan_p05 = new ItemDto();          /* 利用可能期間限定Ｐ当月＋４     */
    public ItemDto kikan_p_shikko_flg = new ItemDto(); /* 期間限定Ｐ失効フラグ           */
    /* 2022/11/29 MCCM初版 ADD START */
    public ItemDto yokunen_kobai_p = new ItemDto();    /* 利用可能通常購買Ｐ翌年度       */
    public ItemDto tonen_kobai_p = new ItemDto();      /* 利用可能通常購買Ｐ当年度       */
    public ItemDto zennen_kobai_p = new ItemDto();     /* 利用可能通常購買Ｐ前年度       */
    public ItemDto yokunen_hikobai_p = new ItemDto();  /* 利用可能通常非購買Ｐ翌年度     */
    public ItemDto tonen_hikobai_p = new ItemDto();    /* 利用可能通常非購買Ｐ当年度     */
    public ItemDto zennen_hikobai_p = new ItemDto();   /* 利用可能通常非購買Ｐ前年度     */
    public ItemDto yokunen_sonota_p = new ItemDto();   /* 利用可能通常その他Ｐ翌年度     */
    public ItemDto tonen_sonota_p = new ItemDto();     /* 利用可能通常その他Ｐ当年度     */
    public ItemDto zennen_sonota_p = new ItemDto();    /* 利用可能通常その他Ｐ前年度     */
    public ItemDto kikan_kobai_p01 = new ItemDto();    /* 利用可能期間限定購買Ｐ当月     */
    public ItemDto kikan_kobai_p02 = new ItemDto();    /* 利用可能期間限定購買Ｐ当月＋１ */
    public ItemDto kikan_kobai_p03 = new ItemDto();    /* 利用可能期間限定購買Ｐ当月＋２ */
    public ItemDto kikan_kobai_p04 = new ItemDto();    /* 利用可能期間限定購買Ｐ当月＋３ */
    public ItemDto kikan_kobai_p05 = new ItemDto();    /* 利用可能期間限定購買Ｐ当月＋４ */
    public ItemDto kikan_hikobai_p01 = new ItemDto();  /* 利用可能期間限定非購買Ｐ当月     */
    public ItemDto kikan_hikobai_p02 = new ItemDto();  /* 利用可能期間限定非購買Ｐ当月＋１ */
    public ItemDto kikan_hikobai_p03 = new ItemDto();  /* 利用可能期間限定非購買Ｐ当月＋２ */
    public ItemDto kikan_hikobai_p04 = new ItemDto();  /* 利用可能期間限定非購買Ｐ当月＋３ */
    public ItemDto kikan_hikobai_p05 = new ItemDto();  /* 利用可能期間限定非購買Ｐ当月＋４ */
    public ItemDto kikan_sonota_p01 = new ItemDto();   /* 利用可能期間限定その他Ｐ当月     */
    public ItemDto kikan_sonota_p02 = new ItemDto();   /* 利用可能期間限定その他Ｐ当月＋１ */
    public ItemDto kikan_sonota_p03 = new ItemDto();   /* 利用可能期間限定その他Ｐ当月＋２ */
    public ItemDto kikan_sonota_p04 = new ItemDto();   /* 利用可能期間限定その他Ｐ当月＋３ */
    public ItemDto kikan_sonota_p05 = new ItemDto();   /* 利用可能期間限定その他Ｐ当月＋４ */
    public ItemDto nyukai_kigyo_cd_mcc = new ItemDto();/* 入会会社コードＭＣＣ        */
    public ItemDto nyukai_tenpo_mcc = new ItemDto();   /* 入会店舗ＭＣＣ              */
}
