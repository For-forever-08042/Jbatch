package jp.co.mcc.nttdata.batch.business.com.cmABfuncL;

/**
 * 共通関数サービス(cmABfuncL)
 */
public interface CmABfuncLService {


    /*----------------------------------------------------------------------------*/
    /*  定数                                                                  */
    /*----------------------------------------------------------------------------*/
    /* 環境変数関連                         */
    String C_CM_ORA_SID_MD = "CM_ORA_SID_MD";
    String C_CM_ORA_DBLINK_MD = "CM_ORA_DBLINK_MD";
    String C_CM_USR_MD = "CM_USR_MD";
    String C_CM_PSW_MD = "CM_PSW_MD";
    String C_CM_ORA_SID_SD = "CM_ORA_SID_SD";
    String C_CM_ORA_DBLINK_SD = "CM_ORA_DBLINK_SD";
    String C_CM_USR_SD = "CM_USR_SD";
    String C_CM_PSW_SD = "CM_PSW_SD";
    String C_CM_ORA_SID_BD = "CM_ORA_SID_BD";
    String C_CM_ORA_DBLINK_BD = "CM_ORA_DBLINK_BD";
    String C_CM_USR_BD = "CM_USR_BD";
    String C_CM_PSW_BD = "CM_PSW_BD";
    String C_CM_ORA_SID_HD = "CM_ORA_SID_HD";
    String C_CM_ORA_DBLINK_HD = "CM_ORA_DBLINK_HD";
    String C_CM_USR_HD = "CM_USR_HD";
    String C_CM_PSW_HD = "CM_PSW_HD";
    String C_CM_SERVER_ID = "CM_SERVER_ID";
    String C_CM_APLOG = "CM_APLOG";
    String C_CM_TODAY = "CM_TODAY";
    int APLW_INPUT_MAX = 11550; /* 128桁×30行 ＵＴＦ対応で３倍にする +\n*30     */
    int CF_CORPID = 1;           /* ココカラファインヘルスケア企業コード */


}
