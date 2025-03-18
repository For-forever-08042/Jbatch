package jp.co.mcc.nttdata.batch.business.service.cmBTfrnkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTfrnkBService {

    public int cmBTfrnkB_main(IntegerDto ms_kased_upd_cnt); /* 家族ランククリア処理    */
    public int cmBTfrnkB_lck_Ksed();   /* 家族制度情報ロック処理             */
    public int cmBTfrnkB_upd_Ksed();   /* 家族制度情報更新処理               */
    public int cmBTfrnkB_upd_Ewrk();   /* WSバッチ処理実行管理追加／更新処理 */
    public int cmBTfrnkB_Chk_Arg(StringDto Arg_in);   /* 引数チェック処理            */

}
