package jp.co.mcc.nttdata.batch.fw.com.basic;

import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;

/**
 * 　共通サービス
 */
public interface ComBusinessService {

    MainResultDto main(int argc, String[] args);

    /*-----------------------------------------------------------------------------*/
    /*	関数プロトタイプ宣言                                                   */
//    /*-----------------------------------------------------------------------------*/
//    int APLOG_WT(String msgid, int msgidsbt, String dbkbn,
//                 String param1, String param2, String param3,
//                 String param4, String param5, String param6); /* APLOG */
    int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2,
                 Object param3, Object param4, Object param5, Object param6);


    int APLOG_WT_903(Object param1, Object param2, Object param3); /* APLOG(903)  */


}
