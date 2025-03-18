package jp.co.mcc.nttdata.batch.business.service.cmBTnpskB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TSHS_DAY_POINT_TBL;

public interface CmBTnpskBService {


    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
     int   cmBTnpskB_main(IntegerDto ts_upd_cnt,
                          IntegerDto hs_ins_cnt, IntegerDto shik_point_total,
                          IntegerDto exc_cnt, IntegerDto exp_yyyy);
    /* 失効主処理                                 */
     int  cmBTnpskB_ChkArgiInf(StringDto Arg_in );

}
