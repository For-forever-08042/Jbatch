package jp.co.mcc.nttdata.batch.business.service.cmBTatlvB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTatlvBService {

    public int cmBTatlvB_main();           /* 顧客自動退会主処理                 */
    public int cmBTatlvB_Chk_Arg(StringDto Arg_in); /* 引数チェック処理            */
    public int cmBTatlvB_OpenFile();       /* ファイルオープン処理               */
    public int cmBTatlvB_WriteFile(int i); /* 処理結果ファイルに書込み処理       *//* 2022/10/07 MCCM初版 MOD */

}
