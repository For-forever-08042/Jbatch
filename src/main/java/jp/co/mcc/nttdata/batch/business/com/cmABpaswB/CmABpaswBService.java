package jp.co.mcc.nttdata.batch.business.com.cmABpaswB;

public interface CmABpaswBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    int Chk_ArgdInf(String Arg_in);  /* 引数チェック（-d）               */

    int cmABpaswB_main();             /* パスワード作成処理               */
}
