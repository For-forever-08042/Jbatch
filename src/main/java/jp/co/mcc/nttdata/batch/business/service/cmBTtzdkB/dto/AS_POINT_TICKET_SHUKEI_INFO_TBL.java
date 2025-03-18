package jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class AS_POINT_TICKET_SHUKEI_INFO_TBL {
    public ItemDto shukei_ymd = new ItemDto();                             /* 集計対象月                     */
    public ItemDto            kyu_hansya_cd = new ItemDto();                          /* 旧販社コード                   */
    public ItemDto            mise_no = new ItemDto();                                /* 店番号                         */
    public ItemDto            kyu_hansya_kanji= new ItemDto(10*3+1);               /* 旧販社名                       */
    public ItemDto            mise_kanji = new ItemDto(40*3+1);                     /* 店舗名                         */
    public ItemDto            begin_ticket_point = new ItemDto();                     /* 期首残高                       */
    public ItemDto            hakkou_ticket_point = new ItemDto();                    /* 発行Ｐ券ポイント数             */
    public ItemDto            hakkou_ticket_cnt = new ItemDto();                      /* 発行Ｐ券枚数                   */
    public ItemDto            use_ticket_point = new ItemDto();                       /* 利用Ｐ券ポイント数             */
    public ItemDto            use_ticket_cnt = new ItemDto();                         /* 利用Ｐ券枚数                   */
    public ItemDto            use_ticket_anbun_gaku = new ItemDto();                  /* 利用Ｐ券案分額                 */
    public ItemDto            end_ticket_point = new ItemDto();                       /* 期末残高                       */
    public ItemDto            use_ticket_point_hosei = new ItemDto();                 /* 利用Ｐ券ポイント数補正値       */
}
