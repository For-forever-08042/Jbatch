package jp.co.mcc.nttdata.batch.business.service.cmBTrankB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTrankBService {
    /* 2022/09/22 MCCM初版 MOD END */
     int  cmBTrankB_main(IntegerDto   ms_kosed_upd_cnt );
    /* 2022/09/22 MCCM初版 MOD START */
    /*static int  cmBTrankB_upd_Ksed();*/       /* 顧客制度情報更新処理               */
     int  cmBTrankB_upd_Ksed();       /* TSランク情報更新処理               */
    /* 2022/09/22 MCCM初版 MOD END */
     int  cmBTrankB_upd_Ewrk();       /* WSバッチ処理実行管理追加／更新処理 */
     int  cmBTrankB_Chk_Arg(StringDto Arg_in);   /* 引数チェック処理            */

}
