package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TS_KIKAN_POINT_TBL  extends TBLBaseDto {
    public ItemDto          kokyaku_no = new ItemDto(15+1);                      /* 顧客番号                   */
    public ItemDto           fuyo_kikan_gentei_point01 = new ItemDto();            /* 付与期間限定Ｐ０１         */
    public ItemDto           fuyo_kikan_gentei_point02 = new ItemDto();            /* 付与期間限定Ｐ０２         */
    public ItemDto           fuyo_kikan_gentei_point03 = new ItemDto();            /* 付与期間限定Ｐ０３         */
    public ItemDto           fuyo_kikan_gentei_point04 = new ItemDto();            /* 付与期間限定Ｐ０４         */
    public ItemDto           fuyo_kikan_gentei_point05 = new ItemDto();            /* 付与期間限定Ｐ０５         */
    public ItemDto           fuyo_kikan_gentei_point06 = new ItemDto();            /* 付与期間限定Ｐ０６         */
    public ItemDto           fuyo_kikan_gentei_point07 = new ItemDto();            /* 付与期間限定Ｐ０７         */
    public ItemDto           fuyo_kikan_gentei_point08 = new ItemDto();            /* 付与期間限定Ｐ０８         */
    public ItemDto           fuyo_kikan_gentei_point09 = new ItemDto();            /* 付与期間限定Ｐ０９         */
    public ItemDto           fuyo_kikan_gentei_point10 = new ItemDto();            /* 付与期間限定Ｐ１０         */
    public ItemDto           fuyo_kikan_gentei_point11 = new ItemDto();            /* 付与期間限定Ｐ１１         */
    public ItemDto           fuyo_kikan_gentei_point12 = new ItemDto();            /* 付与期間限定Ｐ１２         */
    public ItemDto           riyo_kikan_gentei_point01 = new ItemDto();            /* 利用期間限定Ｐ０１         */
    public ItemDto           riyo_kikan_gentei_point02 = new ItemDto();            /* 利用期間限定Ｐ０２         */
    public ItemDto           riyo_kikan_gentei_point03 = new ItemDto();            /* 利用期間限定Ｐ０３         */
    public ItemDto           riyo_kikan_gentei_point04 = new ItemDto();            /* 利用期間限定Ｐ０４         */
    public ItemDto           riyo_kikan_gentei_point05 = new ItemDto();            /* 利用期間限定Ｐ０５         */
    public ItemDto           riyo_kikan_gentei_point06 = new ItemDto();            /* 利用期間限定Ｐ０６         */
    public ItemDto           riyo_kikan_gentei_point07 = new ItemDto();            /* 利用期間限定Ｐ０７         */
    public ItemDto           riyo_kikan_gentei_point08 = new ItemDto();            /* 利用期間限定Ｐ０８         */
    public ItemDto           riyo_kikan_gentei_point09 = new ItemDto();            /* 利用期間限定Ｐ０９         */
    public ItemDto           riyo_kikan_gentei_point10 = new ItemDto();            /* 利用期間限定Ｐ１０         */
    public ItemDto           riyo_kikan_gentei_point11 = new ItemDto();            /* 利用期間限定Ｐ１１         */
    public ItemDto           riyo_kikan_gentei_point12 = new ItemDto();            /* 利用期間限定Ｐ１２         */
    public ItemDto    sagyo_kigyo_cd= new ItemDto();                        /* 作業企業コード             */
    public ItemDto           sagyosha_id= new ItemDto();                           /* 作業者ＩＤ                 */
    public ItemDto    sagyo_ymd= new ItemDto();                             /* 作業年月日                 */
    public ItemDto    sagyo_hms= new ItemDto();                             /* 作業時刻                   */
    public ItemDto    batch_koshin_ymd= new ItemDto();                      /* バッチ更新日               */
    public ItemDto    saishu_koshin_ymd= new ItemDto();                     /* 最終更新日                 */
    public ItemDto           saishu_koshin_ymdhms= new ItemDto();                  /* 最終更新日時               */
    public ItemDto              saishu_koshin_programid = new ItemDto(20+1);         /* 最終更新プログラムＩＤ     */
}
