package jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class AS_TUJYO_POINT_SHUKEI_INFO_TBL {
    public ItemDto            shukei_ymd = new ItemDto();                 /* 集計対象月                 */
    public ItemDto            kyu_hansya_cd = new ItemDto();              /* 旧販社コード               */
    public ItemDto            mise_no = new ItemDto();                    /* 店番号                     */
    public ItemDto            kyu_hansya_kanji= new ItemDto(10*3+1);   /* 旧販社名                   */
    public ItemDto            mise_kanji = new ItemDto(40*3+1);         /* 店舗名                     */
    public ItemDto            begin_point = new ItemDto();                /* 月初Ｐ残高                 */
    public ItemDto            begin_point_zennen = new ItemDto();         /* 月初Ｐ残高前年             */
    public ItemDto            begin_point_tounen = new ItemDto();         /* 月初Ｐ残高当年             */
    public ItemDto            kaiage_point = new ItemDto();               /* 買上Ｐ数                   */
    public ItemDto            kaiage_cnt_point = new ItemDto();           /* 買上回数Ｐ数               */
    public ItemDto            babycircle = new ItemDto();                 /* ベビーサークル             */
    public ItemDto            watashiplus = new ItemDto();                /* ワタシプラス連携付与Ｐ数   */
    public ItemDto            app_koukan_point = new ItemDto();           /* アプリＰ交換Ｐ数           */
    public ItemDto            catalina_point = new ItemDto();             /* カタリナ付与Ｐ数           */
    public ItemDto            sonota = new ItemDto();                     /* その他付与Ｐ数             */
    public ItemDto            chosei_point = new ItemDto();               /* 調整Ｐ                     */
    public ItemDto            hakkou_point_sum = new ItemDto();           /* 発行Ｐ合計                 */
    public ItemDto            ticket_use_point = new ItemDto();           /* Ｐ券使用ポイント           */
    public ItemDto            syouhin_koukan_point = new ItemDto();       /* 商品交換Ｐ数               */
    public ItemDto            bokin_point = new ItemDto();                /* 募金Ｐ数ト                 */
    public ItemDto            use_point_sum = new ItemDto();              /* 使用Ｐ合計                 */
    public ItemDto            ruikei_chosei_point = new ItemDto();        /* 累計調整Ｐ                 */
    public ItemDto            clear_point = new ItemDto();                /* クリアＰ数                 */
    public ItemDto            clear_point_taikai_zennen = new ItemDto();  /* クリアＰ数顧客退会前年     */
    public ItemDto            clear_point_taikai_tounen = new ItemDto();  /* クリアＰ数顧客退会当年     */
    public ItemDto            clear_point_yuukou = new ItemDto();         /* クリアＰ数有効期限         */
    public ItemDto            end_point = new ItemDto();                  /* 月末Ｐ残高                 */
    public ItemDto            end_point_zennen = new ItemDto();           /* 月末Ｐ残高前年             */
    public ItemDto            end_point_tounen = new ItemDto();           /* 月末Ｐ残高当年             */
    public ItemDto            uriagekingaku_sum = new ItemDto();          /* 売上金額合計               */
}
