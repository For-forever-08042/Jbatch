package jp.co.mcc.nttdata.batch.business.com.cmABfuncL.dto;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_APLOG_flg_E;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_APLOG_flg_N;


public enum C_const_aplog_msg_enum {
    AP_LOG_0(C_const_APLOG_flg_E, "900", "%s"),
    AP_LOG_1(C_const_APLOG_flg_E, "901", "DB接続エラー  STATUS=%d"),
    AP_LOG_2(C_const_APLOG_flg_E, "902", "DBエラー  STATUS=%d(TBL:%s)"),
    AP_LOG_3(C_const_APLOG_flg_E, "903", "関数エラー:関数名=%s\nSTATUS=%d SUB=%d"),
    AP_LOG_4(C_const_APLOG_flg_E, "904", "DBエラー(%s)  STATUS=%d(TBL:%s KEY:%s)"),
    AP_LOG_5(C_const_APLOG_flg_E, "905", ""),
    AP_LOG_6(C_const_APLOG_flg_E, "906", ""),
    AP_LOG_7(C_const_APLOG_flg_E, "907", ""),
    AP_LOG_8(C_const_APLOG_flg_E, "908", ""),
    AP_LOG_9(C_const_APLOG_flg_E, "909", ""),
    AP_LOG_10(C_const_APLOG_flg_E, "910", "引数エラー:%s"),
    AP_LOG_11(C_const_APLOG_flg_E, "911", "ファイル入出力エラー:%s"),
    AP_LOG_12(C_const_APLOG_flg_E, "912", "異常終了:%s"),
    AP_LOG_13(C_const_APLOG_flg_E, "913", "異常終了:顧客ロックエラー:顧客番号=%15s"),
    AP_LOG_14(C_const_APLOG_flg_N, "102", "%s    開始"),
    AP_LOG_15(C_const_APLOG_flg_N, "103", "%s    正常終了");

    C_const_aplog_msg_enum(String c_const_APLOG_flg, String c_const_APLOG_msgid, String c_const_APLOG_format) {
        C_const_APLOG_flg = c_const_APLOG_flg;
        C_const_APLOG_msgid = c_const_APLOG_msgid;
        C_const_APLOG_format = c_const_APLOG_format;
    }

  public  String C_const_APLOG_flg;
  public  String C_const_APLOG_msgid;
  public  String C_const_APLOG_format;
}
