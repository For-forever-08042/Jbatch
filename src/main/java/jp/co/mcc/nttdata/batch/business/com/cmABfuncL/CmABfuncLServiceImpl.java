package jp.co.mcc.nttdata.batch.business.com.cmABfuncL;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.dto.C_const_aplog_msg_enum;
import jp.co.mcc.nttdata.batch.business.com.cmBTfuncB.CmBTfuncBImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlcaManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.ParamsExecuteDto;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import static jp.co.mcc.nttdata.batch.business.com.cmABfuncL.constanst.CmABfuncLConstant.*;

@Slf4j
public class CmABfuncLServiceImpl extends C_aplcom1Service implements CmABfuncLService, ComBusinessService {
    public MainResultDto resultData = new MainResultDto();
    @Resource
    public CmBTfuncBImpl cmBTfuncB;
    public static SqlstmDto sqlca = new SqlstmDto();
    public String WORK = "WORK";
    @Resource
    protected SqlcaManager sqlcaManager;

    public void closeLog() {
        DBG_LOG = false;
        dbg_getaplogfmt = 0;     /* ＡＰログフォーマット取得処理           */
        dbg_aplogwrite = 0;     /* ＡＰログ出力処理                       */
        dbg_convut2sj = 0;     /* ＵＴＦ８ＳＪＩＳ変換処理               */
        dbg_convsj2ut = 0;     /* ＳＪＩＳＵＴＦ８変換処理               */
        dbg_oradbconnect = 1;     /* ＤＢ接続                               */
        dbg_getbatdate = 0;     /* バッチ日付取得                         */
        dbg_getsysdatetime = 0;     /* システム日付取得                       */
        dbg_strcntutf8 = 0;     /* ＵＴＦ８文字列の長さを取得             */
        dbg_getpgname = 0;     /* プログラム名取得                       */
    }

    public void openLog() {
        DBG_LOG = true;
        dbg_getaplogfmt = 0;     /* ＡＰログフォーマット取得処理           */
        dbg_aplogwrite = 0;     /* ＡＰログ出力処理                       */
        dbg_convut2sj = 0;     /* ＵＴＦ８ＳＪＩＳ変換処理               */
        dbg_convsj2ut = 0;     /* ＳＪＩＳＵＴＦ８変換処理               */
        dbg_oradbconnect = 1;     /* ＤＢ接続                               */
        dbg_getbatdate = 0;     /* バッチ日付取得                         */
        dbg_getsysdatetime = 1;     /* システム日付取得                       */
        dbg_strcntutf8 = 0;     /* ＵＴＦ８文字列の長さを取得             */
        dbg_getpgname = 1;     /* プログラム名取得                       */
    }
//    /*----------------------------------------------------------------------------*/
//    /*  トレース出力要否設定（0:不要、1:必要）                                */
//    /*----------------------------------------------------------------------------*/
//    /*      関数単位にトレース出力要否が設定できるように定義                      */
//    protected int dbg_getaplogfmt = 1;     /* ＡＰログフォーマット取得処理           */
//    protected int dbg_aplogwrite = 1;     /* ＡＰログ出力処理                       */
//    protected int dbg_convut2sj = 0;     /* ＵＴＦ８ＳＪＩＳ変換処理               */
//    protected int dbg_convsj2ut = 0;     /* ＳＪＩＳＵＴＦ８変換処理               */
//    protected int dbg_oradbconnect = 1;     /* ＤＢ接続                               */
//    protected int dbg_getbatdate = 1;     /* バッチ日付取得                         */
//    protected int dbg_getsysdatetime = 1;     /* システム日付取得                       */
//    protected int dbg_strcntutf8 = 1;     /* ＵＴＦ８文字列の長さを取得             */
//    protected int dbg_kdatalock = 0;     /* 顧客データロック処理                   */
//    protected int dbg_gettenaurbcd = 0;     /* テナント売場コード取得                 */
//    protected int dbg_getentpoint = 0;     /* エントリーポイント取得                 */
//    protected int dbg_changetkstatus = 1;     /* 提携ステータス変更                     */
//    protected int dbg_getpostbarcode = 1;     /* 郵便番号コード変換                     */
//    protected int dbg_GetTanCDV = 1;     /* 担当者コードＣＤＶ取得処理             */
//    protected int dbg_GetPidCDV = 1;     /* 会員コード、顧客コード CDV取得処理     */
//    protected int dbg_ConvTelNo = 1;     /* 電話番号ハイフンなし変換処理           */
//    protected int dbg_CountAge = 1;     /* 年齢計算処理                           */
//    protected int dbg_EncOrDec = 1;     /* 暗号化・復号化処理                     */
//    protected int dbg_getpgname = 1;     /* プログラム名取得                       */
//    protected int dbg_convhalf2full = 1;     /* 半角→全角変換処理                     */
//    protected int dbg_convfull2half = 1;     /* 全角→半角変換処理                     */
//    protected int dbg_setfullspace = 1;     /* 全角スペースセット処理                 */
//    protected int dbg_GetSaveKkn = 1;     /* 保存期間取得処理                       */
//    /* 2022/09/14 MCCM初版 ADD START */
//    protected int dbg_GetPrefecturesCode = 1;     /* 都道府県コード取得処理                 */
//    protected int dbg_GetPrefectures = 1;     /* 都道府県名取得処理処理                 */
//    /* 2022/09/14 MCCM初版 ADD END */
//    /* 2022/10/25 MCCM初版 ADD START */
//    protected int dbg_GetTanCDV12 = 1;     /* 担当者コードＣＤＶ取得処理             */
//    protected int dbg_GetPidCDV12 = 1;     /* 会員コード、顧客コード CDV取得処理     */
//    protected int dbg_CorrectMemberNo = 1;     /* 会員番号補正処理     */

    /*----------------------------------------------------------------------------*/
    /*  ホスト変数                                                            */
    /*----------------------------------------------------------------------------*/
    //    EXEC SQL BEGIN DECLARE SECTION;
    //     接続用
    //    static VARCHAR ORAUSR[32];
    //    static   VARCHAR PASSWD[32];
    //    static  VARCHAR ORASID[32];
    //
    //     SQL文・テーブル名
    //    static  VARCHAR WRKSQL[4096];
    //
    //     汎用
    //    VARCHAR KEY01[100];
    //    VARCHAR KEY02[100];
    //    VARCHAR KEY03[100];
    //    VARCHAR KEY04[100];
    //    VARCHAR KEY05[100];
    //    VARCHAR DATA01[100];
    //    VARCHAR DATA02[100];
    //    VARCHAR DATA03[100];
    //    VARCHAR DATA04[100];
    //    VARCHAR DATA05[100];
    //    VARCHAR DATA06[200];
    //    VARCHAR DATA07[200];
    //    VARCHAR DATA08[200];
    //    VARCHAR DATA09[200];
    //    VARCHAR DATA10[200];
    //    int     H_DATCNT;      select count(*) 用
    //
    //    int     H_UPDATE_DATE;  最終更新日
    //    VARCHAR H_UPDATE_PID[21];  最終更新プログラムＩＤ
    //
    //     ＡＰログフォーマット情報テーブル
    //    VARCHAR H_APLOGFMT_PGMID[15];    プログラムＩＤ
    //    VARCHAR H_APLOGFMT_MSGID[4];     メッセージＩＤ
    //    VARCHAR H_APLOGFMT_FLG[2];       フラグ
    //    VARCHAR H_APLOGFMT_PGMTYP[2];    プログラムタイプ
    //    VARCHAR H_APLOGFMT_FORMAT[361];  フォーマット
    //
    //    EXEC SQL END DECLARE SECTION;

    StringDto ORAUSR = new StringDto(32);
    StringDto PASSWD = new StringDto(32);
    StringDto ORASID = new StringDto(32);
    /* SQL文・テーブル名 */
    public StringDto WRKSQL = new StringDto(4096);
    /* 汎用          */
    StringDto KEY01 = new StringDto(100);
    StringDto KEY02 = new StringDto(100);
    StringDto KEY03 = new StringDto(100);
    StringDto KEY04 = new StringDto(100);
    StringDto KEY05 = new StringDto(100);
    StringDto DATA01 = new StringDto(100);
    StringDto DATA02 = new StringDto(100);
    StringDto DATA03 = new StringDto(100);
    StringDto DATA04 = new StringDto(100);
    StringDto DATA05 = new StringDto(100);
    StringDto DATA06 = new StringDto(200);
    StringDto DATA07 = new StringDto(200);
    StringDto DATA08 = new StringDto(200);
    StringDto DATA09 = new StringDto(200);
    StringDto DATA10 = new StringDto(200);
    int H_DATCNT;     /* select count(*) 用 */
    int H_UPDATE_DATE; /* 最終更新日 */
    StringDto H_UPDATE_PID = new StringDto(21); /* 最終更新プログラムＩＤ */
    /* ＡＰログフォーマット情報テーブル */
    StringDto H_APLOGFMT_PGMID = new StringDto(15);   /* プログラムＩＤ   */
    StringDto H_APLOGFMT_MSGID = new StringDto(4);    /* メッセージＩＤ   */
    StringDto H_APLOGFMT_FLG = new StringDto(2);      /* フラグ           */
    StringDto H_APLOGFMT_PGMTYP = new StringDto(2);   /* プログラムタイプ */
    StringDto H_APLOGFMT_FORMAT = new StringDto(361); /* フォーマット     */


    private static int APLOG_MSG_MAX = 16; /* APログメッセージテーブルの件数 */

    public static C_const_aplog_msg_enum[] C_const_APLOG_MSG = {
            C_const_aplog_msg_enum.AP_LOG_0,
            C_const_aplog_msg_enum.AP_LOG_1,
            C_const_aplog_msg_enum.AP_LOG_2,
            C_const_aplog_msg_enum.AP_LOG_3,
            C_const_aplog_msg_enum.AP_LOG_4,
            C_const_aplog_msg_enum.AP_LOG_5,
            C_const_aplog_msg_enum.AP_LOG_6,
            C_const_aplog_msg_enum.AP_LOG_7,
            C_const_aplog_msg_enum.AP_LOG_8,
            C_const_aplog_msg_enum.AP_LOG_9,
            C_const_aplog_msg_enum.AP_LOG_10,
            C_const_aplog_msg_enum.AP_LOG_11,
            C_const_aplog_msg_enum.AP_LOG_12,
            C_const_aplog_msg_enum.AP_LOG_13,
            C_const_aplog_msg_enum.AP_LOG_14,
            C_const_aplog_msg_enum.AP_LOG_15,
    };
//    C_const_APLOG_MSG[APLOG_MSG_MAX] =   {
//        {C_const_APLOG_flg_E, "900",  "%s"},
//        {C_const_APLOG_flg_E, "901",  "DB接続エラー  STATUS=%d"},
//        {C_const_APLOG_flg_E, "902",  "DBエラー  STATUS=%d(TBL:%s)"},
//        {C_const_APLOG_flg_E, "903",  "関数エラー:関数名=%s\nSTATUS=%d SUB=%d"},
//        {C_const_APLOG_flg_E, "904",  "DBエラー(%s)  STATUS=%d(TBL:%s KEY:%s)"},
//        {C_const_APLOG_flg_E, "905",  ""},
//        {C_const_APLOG_flg_E, "906",  ""},
//        {C_const_APLOG_flg_E, "907",  ""},
//        {C_const_APLOG_flg_E, "908",  ""},
//        {C_const_APLOG_flg_E, "909",  ""},
//        {C_const_APLOG_flg_E, "910",  "引数エラー:%s"},
//        {C_const_APLOG_flg_E, "911",  "ファイル入出力エラー:%s"},
//        {C_const_APLOG_flg_E, "912",  "異常終了:%s"},
//        {C_const_APLOG_flg_E, "913",  "異常終了:顧客ロックエラー:顧客番号=%15s"},
//        {C_const_APLOG_flg_N, "102",  "%s    開始"},
//        {C_const_APLOG_flg_N, "103",  "%s    正常終了"}
//    }

    public int C_GetPgname(String[] argv) {
        String buf2;
        String p;
        String q;

        // グローバル変数にプログラム名を編集する
        p = argv[0];
        q = p;
        for (int i = 0; i < p.length(); i++) {
            if (p.charAt(i) == '/') {
                q = p.substring(i + 1);
            }
        }

        buf2 = q.substring(0, 9);
        Cg_Program_Name_Ver = q;

        if (dbg_getpgname == 1) {
            C_DbgMsg("C_GetPgname : %s", "start");
        }

        // 引数のチェック
        if (argv == null) {
            if (dbg_getpgname == 1) {
                C_DbgMsg("C_GetPgname : %s", "prmerr");
            }
            return C_const_PARAMERR;
        }

        if (dbg_getpgname == 1) {
            C_DbgMsg("C_GetPgname : argv[0]=[%s]", argv[0]);
        }

        Cg_Program_Name = buf2;

        // プログラム名の桁数チェック
        // Cg_Program_Name_Verが12桁以外の場合
        // または、Cg_Program_Nameが9桁以外の場合、処理を終了する。
        if (Cg_Program_Name_Ver.length() != 9) {
            if (dbg_getpgname == 1) {
                C_DbgMsg("C_GetPgname : %s", "length NG");
                C_DbgMsg("C_GetPgname : Name_Ver=[%s]", Cg_Program_Name_Ver);
                C_DbgMsg("C_GetPgname : Name=[%s]", Cg_Program_Name);
            }
            return C_const_NG;
        }

        if (dbg_getpgname == 1) {
            C_DbgMsg("C_GetPgname : Name_Ver=[%s]", Cg_Program_Name_Ver);
            C_DbgMsg("C_GetPgname : Name=[%s]", Cg_Program_Name);
            C_DbgMsg("C_GetPgname : %s", "end");
        }

        return C_const_OK;
    }

    @Override
    public int C_OraDBConnect(String dbkbn, IntegerDto status) {
        String p_wk_env; /* getenv()用ワーク */

        if (dbg_oradbconnect == 1) {
            C_DbgMsg("C_OraDBConnect : %s", "start");
            C_DbgMsg("C_OraDBConnect : dbkbn=%s\n", dbkbn);
        }

        /* 引数のチェックを行う */
        if (StringUtils.isEmpty(dbkbn) || status == null) {
            if (dbg_oradbconnect == 1) {
                C_DbgMsg("C_OraDBConnect : %s\n", "PRMERR");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        if (strcmp(dbkbn, "MD") != 0
                && strcmp(dbkbn, "SD") != 0
                && strcmp(dbkbn, "BD") != 0
                && strcmp(dbkbn, "HD") != 0) {
            if (dbg_oradbconnect == 1) {
                C_DbgMsg("C_OraDBConnect : dbkbn NG(%s)\n", dbkbn);
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        StringDto ORAUSR = new StringDto();
        StringDto PASSWD = new StringDto();
        StringDto ORASID = new StringDto();
        /* 環境変数の取得 */
//        memset(ORAUSR.arr, 0x00, 31);
//        memset(PASSWD.arr, 0x00, 31);
//        memset(ORASID.arr, 0x00, 31);
        ORAUSR.len = 31;
        PASSWD.len = 31;
        ORASID.len = 31;

        if (strcmp(dbkbn, "MD") == 0) {
            p_wk_env = getenv(C_CM_ORA_SID_MD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(SID)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORASID.arr = strncpy(ORASID.arr, p_wk_env, 31 - 1);

            p_wk_env = getenv(C_CM_USR_MD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(USR)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORAUSR.arr = strncpy(ORAUSR.arr, p_wk_env, 31 - 1);

            p_wk_env = getenv(C_CM_PSW_MD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(PSW)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            PASSWD.arr = strncpy(PASSWD.arr, p_wk_env, 31 - 1);
        } else if (strcmp(dbkbn, "SD") == 0) {
            p_wk_env = getenv(C_CM_ORA_SID_SD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(SID)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORASID.arr = strncpy(ORASID.arr, p_wk_env, 31 - 1);

            p_wk_env = getenv(C_CM_USR_SD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(USR)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORAUSR.arr = strncpy(ORAUSR.arr, p_wk_env, 31 - 1);

            p_wk_env = getenv(C_CM_PSW_SD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(PSW)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            PASSWD.arr = strncpy(PASSWD.arr, p_wk_env, 31 - 1);
        } else if (strcmp(dbkbn, "BD") == 0) {
            p_wk_env = getenv(C_CM_ORA_SID_BD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(SID)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORASID.arr = strncpy(ORASID.arr, p_wk_env, 31 - 1);
            p_wk_env = getenv(C_CM_USR_BD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(USR)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORAUSR.arr = strncpy(ORAUSR.arr, p_wk_env, 31 - 1);
            p_wk_env = getenv(C_CM_PSW_BD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(PSW)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            PASSWD.arr = strncpy(PASSWD.arr, p_wk_env, 31 - 1);
        } else if (strcmp(dbkbn, "HD") == 0) {
            p_wk_env = getenv(C_CM_ORA_SID_HD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(SID)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORASID.arr = strncpy(ORASID.arr, p_wk_env, sizeof(p_wk_env) - 1);
            p_wk_env = getenv(C_CM_USR_HD);
            if (StringUtils.isEmpty(p_wk_env)) {
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(USR)");
                }
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
            ORAUSR.arr = strncpy(ORAUSR.arr, p_wk_env, 31 - 1);
            p_wk_env = getenv(C_CM_PSW_HD);
            if (StringUtils.isEmpty(p_wk_env)) {
                status.arr = C_const_Stat_ENVERR;
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "getenv NG(PSW)");
                }
                return C_const_NG;
            }
            PASSWD.arr = strncpy(PASSWD.arr, p_wk_env, 31 - 1);
        }

        /* LENGTHの設定 */
        ORASID.len = strlen(ORASID.arr);
        ORAUSR.len = strlen(ORAUSR.arr);
        PASSWD.len = strlen(PASSWD.arr);

        if (dbg_oradbconnect == 1) {
            C_DbgMsg("C_OraDBConnect : SID=[%s]\n", ORASID.arr);
            C_DbgMsg("C_OraDBConnect : USR=[%s]\n", ORAUSR.arr);
            C_DbgMsg("C_OraDBConnect : PSW=[%s]\n", PASSWD.arr);
        }

        if (null != Cg_ORASID && strlen(Cg_ORASID) != 0) { /* ＤＢに接続中 */
            if (strcmp(Cg_ORASID, ORASID.arr) == 0
                    && strcmp(Cg_ORAUSR, ORAUSR.arr) == 0
                    && strcmp(Cg_ORAPWD, PASSWD.arr) == 0) {
                /* 該当ＤＢに接続済み */
                /* 正常終了           */
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "該当ＤＢに接続済み");
                }
                status.arr = C_const_Stat_OK;
                return C_const_OK;
            } else {
                /* 該当ＤＢ以外に接続ずみ */
                if (dbg_oradbconnect == 1) {
                    C_DbgMsg("C_OraDBConnect : %s\n", "該当ＤＢ以外に接続済み");
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
        }

        if (dbg_oradbconnect == 1) {
            C_DbgMsg("C_OraDBConnect : %s\n", "接続 check end");
        }
        /* ＤＢコネクトを行う */
        Cg_ORASID = strcpy(Cg_ORASID, ORASID.arr);
        Cg_ORAUSR = strcpy(Cg_ORAUSR, ORAUSR.arr);
        Cg_ORAPWD = strcpy(Cg_ORAPWD, PASSWD.arr);
        if (dbg_oradbconnect == 1) {
            C_DbgMsg("C_OraDBConnect : %s\n", "接続 start");
        }
        /* EXEC SQL CONNECT :ORAUSR IDENTIFIED BY :PASSWD USING :ORASID; */

//        sqlca = new SqlstmDto();
        sqlca.ORASID = ORASID;
        sqlca.ORAUSR = ORAUSR;
        sqlca.PASSWD = PASSWD;

        sqlcaManager.init(sqlca);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* グローバル変数のnullクリア */
            memset(Cg_ORASID, 0x00, sizeof(Cg_ORASID));
            memset(Cg_ORAUSR, 0x00, sizeof(Cg_ORAUSR));
            memset(Cg_ORAPWD, 0x00, sizeof(Cg_ORAPWD));
            Cg_ORASID = null;
            Cg_ORAUSR = null;
            Cg_ORAPWD = null;
            if (dbg_oradbconnect == 1) {
                C_DbgMsg("C_OraDBConnect : %s\n", "接続NG");
                C_DbgMsg("C_OraDBConnect : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 正常終了 */
        if (dbg_oradbconnect == 1) {
            C_DbgMsg("C_OraDBConnect : %s\n", "end");
        }
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }


    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_KdataLock                                                 */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_KdataLock(char *uid, char *locksbt, int *status)               */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              顧客データロック処理                                          */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    uid     ： 顧客番号(15桁)                     */
    /*              char       *    locksbt ： ロック種別(1,2,3)                  */
    /*              int        *    status  ： 結果ステータス                     */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： 顧客ロック対象レコードなし                         */
    /*                                                                            */
    @Override
    public int C_KdataLock(StringDto uid, String locksbt, IntegerDto status) {
        /* ローカル変数 */
        /*        int  ret; */
        int res;
        String penv = null;
        StringDto sid_sd = new StringDto(16);       /* SID */
        StringDto dblink_sd = new StringDto(16);    /* dblink */
        /*        char sys_year[5];       システム年 */
        /*        char wk_date[9];  */
        /*         char wk_time[7];     */
        StringDto tbl_name_pnj = new StringDto(80); /* ポイント年別情報 */
        StringDto tbl_name_ksd = new StringDto(80); /* 顧客制度 */
        StringDto sqlbuf = new StringDto(4096);     /* ＳＱＬ文編集用 */



        /* ホスト変数 */
        // EXEC SQL BEGIN DECLARE SECTION;
        /*        int     sysyyyy;              */
        // varchar    u_id[16];
        // varchar    u_id_sel[16];
        // EXEC SQL END DECLARE SECTION;
        StringDto u_id = new StringDto(16);
        StringDto u_id_sel = new StringDto(16);

        if (dbg_kdatalock == 1) {
            C_DbgMsg("C_KdataLock : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (StringUtils.isEmpty(uid.arr) || StringUtils.isEmpty(locksbt) || status == null) {
            /* 入力引数エラー */
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "PRMERR(null)");
            }
            if (null != status) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;

        }

        /* uidの桁数が15桁かチェックする */
        if (strlen(uid) > 15) {
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "PRMERR(uid len NG)");
            }
            /* 入力引数エラー */
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;

        }

        /* locksbtが1,2,3のいずれかかチェックする */
        if (strcmp(locksbt, "1") != 0 && strcmp(locksbt, "2") != 0 && strcmp(locksbt, "3") != 0) {
            /* 入力引数エラー */
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "PRMERR(locksbt typ NG)");
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;

        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* システム日付の年を取得する */
        /*        memset(wk_date, 0x00, sizeof(wk_date));           */
        /*        memset(wk_time, 0x00, sizeof(wk_time));           */
        /*        ret = C_GetSysDateTime(wk_date, wk_time);         */
        /*        memset(sys_year, 0x00, sizeof(sys_year));         */
        /*        memcpy(sys_year, wk_date, 4);                 */
        /*if (dbg_kdatalock == 1) {                     */
        /*        C_DbgMsg("C_KdataLock : wk_date=[%s]\n", wk_date);        */
        /*        C_DbgMsg("C_KdataLock : wk_time=[%s]\n", wk_time);        */
        /*        C_DbgMsg("C_KdataLock : sys_year=[%s]\n", sys_year);      */
        /*}                                 */

        /* 環境変数からＳＩＤを取得する */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "ENVERR(SID_SD)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        if (strcmp(Cg_ORASID, penv) != 0) {
            /* ＳＤ以外に接続している */
            strcpy(sid_sd, penv);
        } else {
            memset(sid_sd, 0x00, sizeof(sid_sd));
        }

        if (dbg_kdatalock == 1) {
            C_DbgMsg("C_KdataLock : sid_sd=[%s]\n", sid_sd);
        }

        /* 環境変数からＤＢリンク先を取得する */
        penv = getenv(C_CM_ORA_DBLINK_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : %s\n", "ENVERR(DBLINK_SD)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        if (strcmp(Cg_ORASID, penv) != 0) {
            /* ＳＤ以外に接続している */
            strcpy(dblink_sd, penv);
        } else {
            memset(dblink_sd, 0x00, sizeof(dblink_sd));
        }

        if (dbg_kdatalock == 1) {
            C_DbgMsg("C_KdataLock : dblink_sd=[%s]\n", dblink_sd);
        }

        /* 対象テーブル名を編集する */
        memset(tbl_name_pnj, 0x00, sizeof(tbl_name_pnj));
        memset(tbl_name_ksd, 0x00, sizeof(tbl_name_ksd));
        if (strcmp(locksbt, "1") == 0 || strcmp(locksbt, "3") == 0) {
            /* locksbtが１または3の場合 */

            strcpy(tbl_name_pnj, "TS利用可能ポイント情報");
            /*                strcat(tbl_name_pnj, sys_year);       */
            if (strlen(sid_sd) > 0) {
                strcat(tbl_name_pnj, "@");
                strcat(tbl_name_pnj, dblink_sd);
            }

        }

        if (strcmp(locksbt, "2") == 0 || strcmp(locksbt, "3") == 0) {
            /* locksbtが2または3の場合 */
            strcpy(tbl_name_ksd, "MS顧客制度情報");
            if (strlen(sid_sd) > 0) {
                strcat(tbl_name_ksd, "@");
                strcat(tbl_name_ksd, dblink_sd.arr);
            }

        }

        if (dbg_kdatalock == 1) {
            C_DbgMsg("C_KdataLock : tbl_name_pnj=[%s]\n", tbl_name_pnj);
            C_DbgMsg("C_KdataLock : tbl_name_ksd=[%s]\n", tbl_name_ksd);
        }

        /* ＳＱＬ用のホスト変数を編集する */

        /* システム年 */
        /*        sysyyyy = atoi(sys_year);         */

        /* uid */
        strcpy(u_id, uid.arr);
        u_id.len = strlen(uid);

        /* ポイント更新向けのロック */
        if (strcmp(locksbt, "1") == 0 || strcmp(locksbt, "3") == 0) {
            /* locksbtが１または3の場合 */

            /* select ... for update でロックを取得する */

            /* select文を生成する */
            strcpy(sqlbuf, "select to_char(顧客番号) from ");
            strcat(sqlbuf, tbl_name_pnj.arr);
            /*                strcat(sqlbuf, " where 年 = :sysyyyy ");          */
            strcat(sqlbuf, " where 顧客番号 = to_number(?) for update ");

            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : sqlbuf=[%s]\n", sqlbuf);
            }

            /* ＳＱＬ文をセットする */
            memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
            strcpy(WRKSQL, sqlbuf.arr);
            WRKSQL.len = strlen(WRKSQL.arr);

            SqlstmDto sqlca = sqlcaManager.get("sql_kdatalock1");
            // EXEC SQL PREPARE sql_kdatalock1 from :WRKSQL;
            sqlca.sql = WRKSQL;
            sqlca.prepare();
            if (CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : PREPARE : sqlca.sqlcode=[%d]\n", CmABfuncLServiceImpl.sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
            //EXEC SQL DECLARE cur_kdatalock1 cursor for sql_kdatalock1;
            sqlca.declare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            /* カーソルのオープン */
            //EXEC SQL OPEN cur_kdatalock1 using  :u_id;
            sqlca.open(u_id);
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }


            /* カーソルのフェッチ */
            // EXEC SQL FETCH cur_kdatalock1 into :u_id_sel;
            sqlca.fetch();
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
            sqlca.recData(u_id_sel);

            res = sqlca.sqlcode;

            /* カーソルのクローズ */
            // EXEC SQL CLOSE cur_kdatalock1;
            sqlca.curse_close();
//            sqlcaManager.close("sql_kdatalock1");
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
            }

            if (res == C_const_Ora_NOTFOUND) {
                /* 検索件数が０件の場合 */

                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : %s\n", "start insert");
                }

                /* 2021.01.25 利用可能ポイント情報TBL項目追加 */
                /* insert文を生成する */

                strcpy(sqlbuf, "insert into ");
                strcat(sqlbuf, tbl_name_pnj);
                strcat(sqlbuf, " (顧客番号,利用可能通常Ｐ０,最終買上日,");
                strcat(sqlbuf, "入会企業コード,入会店舗,発券企業コード,発券店舗,");
                strcat(sqlbuf, "最終更新日, 最終更新日時, 最終更新プログラムＩＤ,");
                strcat(sqlbuf, "利用可能通常Ｐ１, 利用可能通常Ｐ２, 利用可能通常Ｐ３,");
                strcat(sqlbuf, "利用可能通常Ｐ４, 通常Ｐ失効フラグ, 利用可能期間限定Ｐ０１,");
                strcat(sqlbuf, "利用可能期間限定Ｐ０２, 利用可能期間限定Ｐ０３, 利用可能期間限定Ｐ０４,");
                strcat(sqlbuf, "利用可能期間限定Ｐ０５, 利用可能期間限定Ｐ０６, 利用可能期間限定Ｐ０７,");
                strcat(sqlbuf, "利用可能期間限定Ｐ０８, 利用可能期間限定Ｐ０９, 利用可能期間限定Ｐ１０,");
                strcat(sqlbuf, "利用可能期間限定Ｐ１１, 利用可能期間限定Ｐ１２, 期間限定Ｐ失効フラグ) ");
                strcat(sqlbuf, "  values ");
                strcat(sqlbuf, " (to_number(?), 0,0,  0,0,0,0, ");
                strcat(sqlbuf, " 0, sysdate(), ?,0,0,0,0, ");
                strcat(sqlbuf, " 32,0,0,0,0,0,0,0,0,0,0,0,0,4096 ) ");


                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : sqlbuf=[%s]\n", sqlbuf);
                }

                strcpy(WRKSQL, sqlbuf.arr);
                WRKSQL.len = strlen(sqlbuf);

                strcpy(H_UPDATE_PID, Cg_Program_Name);
                H_UPDATE_PID.len = strlen(Cg_Program_Name);

                /* insert文を発行する */
                // EXEC SQL PREPARE s_kdatalock from :WRKSQL;
//                SqlstmDto sqlca = sqlcaManager.get("s_kdatalock");
                sqlca.sql = WRKSQL;
                sqlca.prepare();
                if (sqlca.sqlcode != C_const_Ora_OK) {
                    if (dbg_kdatalock == 1) {
                        C_DbgMsg("C_KdataLock : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                    }
                    status.arr = C_const_Stat_DBERR;
                    return C_const_NG;
                }

                // EXEC SQL EXECUTE s_kdatalock using :u_id, :H_UPDATE_PID;
                sqlca.query(u_id, H_UPDATE_PID);
                if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {
                    if (dbg_kdatalock == 1) {
                        C_DbgMsg("C_KdataLock : EXECUTE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                    }
                    status.arr = C_const_Stat_DBERR;
                    return C_const_NG;

                }

                if (sqlca.sqlcode == C_const_Ora_DUPL) {
                    /* insertでキー重複になった場合 */
                    if (dbg_kdatalock == 1) {
                        C_DbgMsg("C_KdataLock : %s\n", "ORA_DUPL");
                    }

                    /* select ... for update でロックを取得する */


                    /* select文を生成する */
                    strcpy(sqlbuf, "select to_char(顧客番号)  from ");
                    strcat(sqlbuf, tbl_name_pnj);
                    strcat(sqlbuf, "where 顧客番号 = to_number(?) for update ");

                    if (dbg_kdatalock == 1) {
                        C_DbgMsg("C_KdataLock : sqlbuf=[%s]\n", sqlbuf);
                    }

                    /* ＳＱＬ文をセットする */
                    memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
                    strcpy(WRKSQL, sqlbuf.arr);
                    WRKSQL.len = strlen(WRKSQL.arr);

                    // EXEC SQL PREPARE sql_kdatalock11 from :WRKSQL;
                    sqlca.sql = WRKSQL;
//                    SqlstmDto sqlca = sqlcaManager.get("cur_kdatalock11");
                    sqlca.prepare();
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        if (dbg_kdatalock == 1) {
                            C_DbgMsg("C_KdataLock : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                        }
                        status.arr = C_const_Stat_DBERR;
                        return C_const_NG;
                    }

                    // EXEC SQL DECLARE cur_kdatalock11 cursor for sql_kdatalock11;
                    sqlca.declare();
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        if (dbg_kdatalock == 1) {
                            C_DbgMsg("C_KdataLock : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                        }
                        status.arr = C_const_Stat_DBERR;
                        return C_const_NG;
                    }

                    /* カーソルのオープン */
                    // EXEC SQL OPEN cur_kdatalock11 using  :u_id;
                    sqlca.query(u_id);
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        if (dbg_kdatalock == 1) {
                            C_DbgMsg("C_KdataLock : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                        }
                        status.arr = C_const_Stat_DBERR;
                        return C_const_NG;
                    }


                    /* カーソルのフェッチ */
                    // EXEC SQL FETCH cur_kdatalock11 into :u_id_sel;
                    sqlca.fetch();
                    sqlca.recData(u_id_sel);
                    if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                        if (dbg_kdatalock == 1) {
                            C_DbgMsg("C_KdataLock : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                        }
                        status.arr = C_const_Stat_DBERR;
                        return C_const_NG;
                    }

                    res = sqlca.sqlcode;

                    /* カーソルのクローズ */
                    // EXEC SQL CLOSE cur_kdatalock11;
                    sqlca.curse_close();
//                    sqlcaManager.close("cur_kdatalock11");
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        if (dbg_kdatalock == 1) {
                            C_DbgMsg("C_KdataLock : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                        }
                    }

                } else {
                    if (dbg_kdatalock == 1) {
                        C_DbgMsg("C_KdataLock : %s\n", "end insert");
                    }
                }
            }

        }

        /* 顧客情報更新向けのロック */
        if (strcmp(locksbt, "2") == 0 || strcmp(locksbt, "3") == 0) {
            /* locksbtが2または3の場合 */

            /* select ... for update でロックを取得する */

            /* select文を生成する */
            strcpy(sqlbuf, "select 顧客番号 from ");
            strcat(sqlbuf, tbl_name_ksd);
            strcat(sqlbuf, " where 顧客番号 = to_number(?) for update ");

            if (dbg_kdatalock == 1) {
                C_DbgMsg("C_KdataLock : sqlbuf=[%s]\n", sqlbuf);
            }

            /* ＳＱＬ文をセットする */
            memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
            strcpy(WRKSQL, sqlbuf.arr);
            WRKSQL.len = strlen(WRKSQL.arr);

            SqlstmDto sqlca = sqlcaManager.get("cur_kdatalock21");
            // EXEC SQL PREPARE sql_kdatalock21 from :WRKSQL;
            sqlca.sql = WRKSQL;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            // EXEC SQL DECLARE cur_kdatalock21 cursor for sql_kdatalock21;
            sqlca.declare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            /* カーソルのオープン */
            // EXEC SQL OPEN cur_kdatalock21 using :u_id;
            sqlca.query(u_id);
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            /* カーソルのフェッチ */
            // EXEC SQL FETCH cur_kdatalock21 into :u_id_sel;
            sqlca.fetch();
            sqlca.recData(u_id_sel);
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            res = sqlca.sqlcode;

            /* カーソルのクローズ */
            // EXEC SQL CLOSE cur_kdatalock21;
//            sqlcaManager.close("cur_kdatalock21");
//            sqlca.close();
            sqlca.curse_close();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_kdatalock == 1) {
                    C_DbgMsg("C_KdataLock : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
            }

            if (res == C_const_Ora_NOTFOUND) {
                /* 検索件数が０件の場合 */
                status.arr = C_const_Stat_OK;
                return C_const_NOTEXISTS;
            }

        }

        /* 正常終了 */
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

    public int C_APLogWrite(String msgid, String format, String flg, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        String[] fo = new String[2];
        fo[0] = format;
        String[] fl = new String[2];
        fl[0] = flg;
        return C_APLogWrite(msgid, fo, fl, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public int C_APLogWrite(String msgid, String[] format, String[] flg, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        StringDto msgbuf = new StringDto(); /* UTF  */
        String msgbuf2 = null; /* UTF  */
        String logbuf = null; /* SJIS 分割結果を格納 */
        String logbuf2 = null; /* UTFに変換した結果を格納 */
        StringDto msgsjs = new StringDto(); /* SJIS */
        int logcnt = 0; /* 分割した数 */
        IntegerDto msglen = new IntegerDto();
        IntegerDto sjslen = new IntegerDto();
        int ret;
        StringDto wk_date = new StringDto();
        StringDto wk_time = new StringDto();
        String srvid = null;
        String aplogpath = null;
        File st = null;
        FileStatusDto fd = null;
        int wk_cnt;
        String wk_p;
        String wk_q;
//        String c;
        int i = 0;
        String wrtbuf = null;
        IntegerDto cnvlen = new IntegerDto();
        int prmcnt;
        String prmtyp = "";
        String p = null;
        String p0 = null;
        String p2 = null;
        String p3 = null;
        String prm1 = null;
        String prm2 = null;
        String prm3 = null;
        String prm4 = null;
        String prm5 = null;
        String prm6 = null;
        String fmt0 = null;
        String fmt1 = null;
        String fmt2 = null;
        String fmt3 = null;
        String fmt4 = null;
        String fmt5 = null;
        String fmt6 = null;
        long plong;
        String format2 = "";
        int chr_typ; /* 文字種フラグ 1:SJISの１バイト目, 2:SJISの２バイト目, 0:その他 */
        int dev_flg;
        int n_msgid;
        String wk_type;
        int wk_flg = 0;
        String p_dst;
        double pdbl;
        int wk_len;
        String tmp_p;
        int ii;
        StringDto cnvbuf = new StringDto();


        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : %s\n", "start");
        }
        /* 引数のチェック */
        if (msgid == null || format == null || flg == null) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : %s\n", "prmerr");
            }
            return C_const_NG;
        }

        /* ＡＰログ出力用のメッセージを編集する */
        n_msgid = StringUtils.isEmpty(StringUtils.substringBefore(msgid.replaceAll("[^\\d]", " "), " "))
                ? 0 : Integer.parseInt(StringUtils.substringBefore(msgid.replaceAll("[^\\d]", " "), " "));

        n_msgid = atoi(msgid);

        prmcnt = 0;
//        StringBuffer b = new StringBuffer();
//        for (i = 0; i <= 7; i++) {
//            b.append('\0');
//        }
//        prmtyp = b.toString();
//        memset(fmt1,0x00,sizeof(fmt1));
//        memset(fmt2,0x00,sizeof(fmt2));
//        memset(fmt3,0x00,sizeof(fmt3));
//        memset(fmt4,0x00,sizeof(fmt4));
//        memset(fmt5,0x00,sizeof(fmt5));
//        memset(fmt6,0x00,sizeof(fmt6));

        /* フォーマットの解析 */
        p = format[0];
        p_dst = format2;
//        memset(format2,0x00,sizeof(format2));


        StringBuffer p_dst_tmp = new StringBuffer();
        while (null != p && i < p.length()) {
            if (p.length() > (i + 1) && p.charAt(i) == '%' && p.charAt(i + 1) != '%') {
                /* フォーマット指定 */
//                p0 = fmt0;
//                p2 = p;
                fmt0 = "%";
                wk_type = "diuoxXeEfFgGcpsS";
                while (i < p.length() && wk_type.indexOf(p.charAt(i)) < 0) {
                    i++;
                    fmt0 += p.charAt(i);
                }
                if (wk_type.indexOf(p.charAt(i)) >= 0) {
                    /* 書式の文字があった場合 */
//                    p = p2;
                    prmcnt++;
                    prmtyp += p.charAt(i);
                    switch (prmcnt) {
                        case 1:
                            fmt1 = fmt0;
                            break;
                        case 2:
                            fmt2 = fmt0;
                            break;
                        case 3:
                            fmt3 = fmt0;
                            break;
                        case 4:
                            fmt4 = fmt0;
                            break;
                        case 5:
                            fmt5 = fmt0;
                            break;
                        case 6:
                            fmt6 = fmt0;
                            break;
                    }

//                    p_dst = "%s";
                    p_dst_tmp.append("%s");
                } else {
//                    p_dst = "%";
                    p_dst_tmp.append("%");
                }

            } else {
                p_dst_tmp.append(p.charAt(i));
                //p_dst[0] = p[i];
//                i++;
            }
            i++;
        }
        format2 = p_dst_tmp.toString();
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : prmcnt=[%d]\n", prmcnt);
            for (i = 0; i < prmcnt; i++) {
                C_DbgMsg("C_APLogWrite : prmtyp=[%c]\n", Character.valueOf(prmtyp.charAt(i)));
            }
            C_DbgMsg("C_APLogWrite : format=[%s]\n", format);
            C_DbgMsg("C_APLogWrite : format2=[%s]\n", format2);
            C_DbgMsg("C_APLogWrite : fmt1=[%s]\n", fmt1);
            C_DbgMsg("C_APLogWrite : fmt2=[%s]\n", fmt2);
            C_DbgMsg("C_APLogWrite : fmt3=[%s]\n", fmt3);
            C_DbgMsg("C_APLogWrite : fmt4=[%s]\n", fmt4);
            C_DbgMsg("C_APLogWrite : fmt5=[%s]\n", fmt5);
            C_DbgMsg("C_APLogWrite : fmt6=[%s]\n", fmt6);
        }

//        memset(prm1, 0x00, sizeof(prm1));
//        memset(prm2, 0x00, sizeof(prm2));
//        memset(prm3, 0x00, sizeof(prm3));
//        memset(prm4, 0x00, sizeof(prm4));
//        memset(prm5, 0x00, sizeof(prm5));
//        memset(prm6, 0x00, sizeof(prm6));

        if (prmcnt > 0) {
            /* 文字列 */
            if (prmtyp.charAt(0) == 's' || prmtyp.charAt(0) == 'S') {
                prm1 = sprintf(prm1, fmt1, param1);
            }
            /* 実数   */
            else if (prmtyp.charAt(0) == 'e' || prmtyp.charAt(0) == 'E'
                    || prmtyp.charAt(0) == 'f' || prmtyp.charAt(0) == 'F'
                    || prmtyp.charAt(0) == 'g' || prmtyp.charAt(0) == 'G') {
                pdbl = getDoubleCus(param1);
                prm1 = sprintf(prm1, fmt1, pdbl);
            }
            /* ポインタ */
            else if (prmtyp.charAt(0) == 'p') {
                prm1 = sprintf(prm1, fmt1, param1);
            }
            /* その他は整数として扱う */
            else {
                plong = getLongCus(param1);
                prm1 = sprintf(prm1, fmt1, plong);
            }
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm1=[%s]\n", prm1);
            }
        }

        if (prmcnt > 1) {
            memset(prm2, 0x00, sizeof(prm2));
            /* 文字 */
            if (prmtyp.charAt(1) == 's' || prmtyp.charAt(1) == 'S') {
                prm2 = sprintf(prm2, fmt2, param2);
            }
            /* ポインタ */
            else if (prmtyp.charAt(1) == 'p') {
                prm2 = sprintf(prm2, fmt2, param2);
            }
            /* 実数   */
            else if (prmtyp.charAt(1) == 'e' || prmtyp.charAt(1) == 'E'
                    || prmtyp.charAt(1) == 'f' || prmtyp.charAt(1) == 'F'
                    || prmtyp.charAt(1) == 'g' || prmtyp.charAt(1) == 'G') {
                pdbl = getDoubleCus(param2);
                prm2 = sprintf(prm2, fmt2, pdbl);
            }
            /* その他は整数で処理する */
            else {
                plong = getLongCus(param2);
                prm2 = sprintf(prm2, fmt2, plong);
            }

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm2=[%s]\n", prm2);
            }
        }

        if (prmcnt > 2) {
            memset(prm3, 0x00, sizeof(prm3));
            if (prmtyp.charAt(2) == 's' || prmtyp.charAt(2) == 'S') {
                prm3 = sprintf(prm3, fmt3, param3);
            } else if (prmtyp.charAt(2) == 'p') {
                prm3 = sprintf(prm3, fmt3, param3);
            } else if (prmtyp.charAt(2) == 'f' || prmtyp.charAt(2) == 'F'
                    || prmtyp.charAt(2) == 'e' || prmtyp.charAt(2) == 'E'
                    || prmtyp.charAt(2) == 'g' || prmtyp.charAt(2) == 'G') {
                pdbl = getDoubleCus(param3);
                prm3 = sprintf(prm3, fmt3, pdbl);
            } else {
                plong = getLongCus(param3);
                prm3 = sprintf(prm3, fmt3, plong);
            }

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm3=[%s]\n", prm3);
            }
        }

        if (prmcnt > 3) {
            memset(prm4, 0x00, sizeof(prm4));
            if (prmtyp.charAt(3) == 's' || prmtyp.charAt(3) == 'S') {
                prm4 = sprintf(prm4, fmt4, param4);
            } else if (prmtyp.charAt(3) == 'p') {
                prm4 = sprintf(prm4, fmt4, param4);
            } else if (prmtyp.charAt(3) == 'f' || prmtyp.charAt(3) == 'F'
                    || prmtyp.charAt(3) == 'e' || prmtyp.charAt(3) == 'E'
                    || prmtyp.charAt(3) == 'g' || prmtyp.charAt(3) == 'G') {
                pdbl = getDoubleCus(param4);
                prm4 = sprintf(prm4, fmt4, pdbl);

            } else {
                plong = getLongCus(param4);
                prm4 = sprintf(prm4, fmt4, plong);
            }

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm4=[%s]\n", prm4);
            }
        }

        if (prmcnt > 4) {
            memset(prm5, 0x00, sizeof(prm5));
            if (prmtyp.charAt(4) == 's' || prmtyp.charAt(4) == 'S') {
                prm5 = sprintf(prm5, fmt5, param5);
            } else if (prmtyp.charAt(4) == 'p') {
                prm5 = sprintf(prm5, fmt5, param5);
            } else if (prmtyp.charAt(4) == 'e' || prmtyp.charAt(4) == 'E'
                    || prmtyp.charAt(4) == 'f' || prmtyp.charAt(4) == 'F'
                    || prmtyp.charAt(4) == 'g' || prmtyp.charAt(4) == 'G') {
                pdbl = getDoubleCus(param5);
                prm5 = sprintf(prm5, fmt5, pdbl);
            } else {
                plong = getLongCus(param5);
                prm5 = sprintf(prm5, fmt5, plong);
            }

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm5=[%s]\n", prm5);
            }
        }

        if (prmcnt > 5) {
//            memset(prm6, 0x00, sizeof(prm6));
            if (prmtyp.charAt(5) == 's' || prmtyp.charAt(5) == 'S') {
                prm6 = sprintf(prm6, fmt6, param6);
            } else if (prmtyp.charAt(5) == 'p') {
                prm6 = sprintf(prm6, fmt6, param6);
            } else if (prmtyp.charAt(5) == 'e' || prmtyp.charAt(5) == 'E'
                    || prmtyp.charAt(5) == 'f' || prmtyp.charAt(5) == 'F'
                    || prmtyp.charAt(5) == 'g' || prmtyp.charAt(5) == 'G') {
                pdbl = getDoubleCus(param6);
                prm6 = sprintf(prm6, fmt6, pdbl);
            } else {
                plong = getLongCus(param6);
                prm6 = sprintf(prm6, fmt6, plong);
            }

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : prm6=[%s]\n", prm6);
            }
        }

//        memset(msgbuf, 0x00, sizeof(msgbuf));
        sprintf(msgbuf, format2, prm1, prm2, prm3, prm4, prm5, prm6);

        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : msgbuf=[%s]\n", msgbuf);
        }

        /* "\n"を'\n'に変換する */

        wk_p = msgbuf.arr;
        wk_q = msgbuf2;
//        memset(msgbuf2, 0x00, sizeof(msgbuf2));
//        msgbuf2 = wk_p.replaceAll("\\\\\\\\\\\\", "\n").replaceAll("\\\\\\\\", "").replaceAll("\\\\", "\n");
        msgbuf2 = wk_p;
//        while (wk_p != '\0') {
//            if (wk_p == 'n') {
//                /* \n */
//
//                if (*(wk_p - 1) == '\\' && *(wk_p - 2) != '\\') {
//                    wk_q -= 1;
//                                *wk_q = '\n';
//                }
//                /* \\n */
//                        else if (*(wk_p - 1) == '\\' && *(wk_p - 2) == '\\' && *(wk_p - 3) != '\\') {
//                    wk_q -= 1; /* \１つを削除 */
//                                *wk_q = *wk_p;
//                }
//                /* \\\n */
//                        else if (*(wk_p - 1) == '\\' && *(wk_p - 2) == '\\' && *(wk_p - 3) == '\\') {
//                    wk_q -= 3;
//                                *wk_q = '\n';
//                }
//                        else {
//                                *wk_q = *wk_p;
//                }
//            }
//                else {
//                        *wk_q = *wk_p;
//            }
//            wk_p++;
//            wk_q++;
//        }

        /* ３０個目の\nを\0に変換する */
        wk_cnt = 0;
//        wk_q = msgbuf2;
        msgbuf2 = msgbuf2;
        //TODO
//        for ( ; ; ) {
//            if (*wk_q == '\0') break;
//
//            if (*wk_q == '\n') wk_cnt++;
//            if (wk_cnt == 30) {
//                        *(wk_q+1) = '\0';
//                break;
//            }
//
//            wk_q++;
//
//        }

        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : wk_cnt=[%d]\n", wk_cnt);
            C_DbgMsg("C_APLogWrite : msgbuf2=[%s]\n", msgbuf2);
        }
        strcpy(msgbuf, msgbuf2);

        ret = C_StrcntUtf8(msgbuf, strlen(msgbuf), msglen);
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : C_StrcntUtf8 -- ret=%d\n", ret);
            C_DbgMsg("C_APLogWrite : C_StrcntUtf8 -- msglen=%d\n", msglen);
        }
        memset(logbuf, 0x00, sizeof(logbuf));
        dev_flg = 0;
//        List<String> logBufTmp = new ArrayList<>();
        if (ret == C_const_OK && (msglen.arr > 90 || msgbuf.arr.contains("\n"))) {
            /* 文字列の長さが８８を超えるか、改行コードがある場合 */

            dev_flg = 1;
            /* コード変換(UTF -> SJIS) */
//            memset(msgsjs, 0x00, sizeof(msgsjs));

            ret = C_ConvUT2SJ(msgbuf, strlen(msgbuf), msgsjs, sjslen);

            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : C_ConvUT2SJ -- ret=%d\n", ret);
                C_DbgMsg("C_APLogWrite : C_ConvUT2SJ -- sjslen=%d\n", sjslen);
                C_DbgMsg("C_APLogWrite : C_ConvUT2SJ -- strlen(msgsjs)=%d\n", strlenCp932(msgsjs.arr));
//                C_DbgMsg("C_APLogWrite : C_ConvUT2SJ -- strlen(msgsjs)=%ld\n", strlen(msgsjs[0]));
                /* C_DbgMsg("C_APLogWrite : C_ConvUT2SJ -- msgsjs=[%s]\n", msgsjs); */
            }

            /* メッセージを90桁ごとに分割する                           */
            /*   90桁ごとに\0を入れる                                   */
            /*   ただし改行コードがあった場合は改行コードを\0に変換する */
            /*   全角文字が分割位置にまたがる場合は前にスペースを入れ   */
            /*   全角文字は先頭にずらしてコピーする                     */

            wk_p = msgsjs.arr;
            wk_q = logbuf;
            logcnt = 1; /* 分割した件数 */
            wk_cnt = 0; /* 桁数のカウンタを０にする   */
            chr_typ = 0; /* 文字種フラグの初期化 */

//            byte[] bt = new byte[0];
//            try {
//                bt = wk_p.getBytes(SystemConstant.Shift_JIS);
//            } catch (UnsupportedEncodingException e) {
//                bt = wk_p.getBytes();
//            }
            StringBuffer bg = new StringBuffer();
//            StringBuffer bgtmp = new StringBuffer();

            for (int t = 0; t < wk_p.length(); t++) {
                String ti = wk_p.substring(t, t + 1);

                byte[] bts;
                try {
                    bts = ti.getBytes(SystemConstant.Shift_JIS);
                } catch (UnsupportedEncodingException e) {
                    bts = ti.getBytes();
                }
//                bgtmp.append(ti);
//                bg.append(ti);
                boolean nextLine = false;
                for (byte c : bts) {
                    wk_cnt++;
                    /* 文字種の判定 */
                    if (chr_typ == 0 || chr_typ == 2) {
                        if ((0x81 <= c && c <= 0x9f) || (0xe0 <= c && c <= 0xfc)) {
                            /* 全角の１バイト目 */
                            chr_typ = 1;
                        } else {
                            chr_typ = 0;
                        }
                    } else if (chr_typ == 1) { /* 直前が全角の１バイト目の場合 */
                        if ((0x40 <= c && c <= 0x7e) || (0x80 <= c && c <= 0xfc)) {
                            /* 全角の２バイト目 */
                            chr_typ = 2;
                        } else {
                            chr_typ = 0;
                        }
                    }
                    if (c == '\n') {
                        /* 改行コード */
                        if (dbg_aplogwrite == 1) {
                            C_DbgMsg("C_APLogWrite : %s\n", "改行コードあり");
                        }
                        logcnt++;
                        wk_cnt = 0;
                    }
                    if (wk_cnt == 89) {
                        /* ９０桁目 */
                        if (chr_typ == 1) {
                            /* 全角の１バイト目なので、スペースをコピーする */
                            if (dbg_aplogwrite == 1) {
                                C_DbgMsg("C_APLogWrite : %s\n", "９０バイト目=全角の１バイト目");
                            }
                            bg.append(" ");
                            bg.append("\n");
                        } else {
                            if (dbg_aplogwrite == 1) {
                                C_DbgMsg("C_APLogWrite : %s\n", "９０バイト目=半角か全角の２バイト目");
                            }
                            /* 全角の１バイト目以外は、そのままコピー */
                            nextLine = true;
                        }
//                        logBufTmp.add(bgtmp.toString());
//                        bgtmp.setLength(0);

                        logcnt++; /* 続きあり */
                        chr_typ = 0;
                        wk_cnt = 0;
                    }
                }
                bg.append(ti);
                if (nextLine) {
                    bg.append("\n");
                    nextLine = false;
                }
            }
//            logBufTmp.add(bgtmp.toString());
            logbuf2 = logbuf = wk_q = bg.toString();
        } else if (ret != C_const_OK) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : C_ConvUT2SJ=%d\n", ret);
            }

            return C_const_NG;

        } else {
            /* ＵＴＦ８をそのままコピーする */
            logbuf2 = memcpy(logbuf2, msgbuf.arr, strlen(msgbuf));
//            logbuf2[strlen(msgbuf)] = '\0';
            logcnt = 1;
        }
//
//        if(logBufTmp.isEmpty()){
//            logBufTmp.add(logbuf2);
//        }
        logcnt = logbuf2.split("\n").length;
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : logcnt = %d\n", logcnt);
            tmp_p = logbuf;
            for (ii = 0; ii < logcnt; ii++) {
                C_DbgMsg("logbuf[]=[%s]\n", tmp_p);
                wk_len = strlen(tmp_p);
                tmp_p += wk_len + 1;
            }
        }
        if (logcnt > 1 || dev_flg == 1) {
            /* 分割した場合は、コード変換(SJIS -> UTF8)してＵＴＦ８に戻す */
            wk_p = logbuf;
            wk_q = logbuf2;
//            memset(logbuf2, 0x00, sizeof(logbuf2));
            for (i = 0; i < logcnt; i++) {
                if (dbg_aplogwrite == 1) {
                    C_DbgMsg("C_APLogWrite : strlen(wk_p) = %d\n", strlen(wk_p));
                }

                wk_len = strlen(wk_p);

//                memset(cnvbuf, 0x00, sizeof(cnvbuf));
                strcpy(cnvbuf, wk_p);

                StringDto tmwk = new StringDto();
                ret = C_ConvSJ2UT(cnvbuf, wk_len, tmwk, cnvlen);
                wk_q = tmwk.arr;
                if (ret != C_const_OK) {
                    if (dbg_aplogwrite == 1) {
                        C_DbgMsg("C_APLogWrite : ＵＴＦ８に戻すところでエラー %d\n", ret);
                    }
                } else {
                    if (dbg_aplogwrite == 1) {
                        C_DbgMsg("C_APLogWrite : cnvlen = %d\n", cnvlen);
                        C_DbgMsg("C_APLogWrite : ＵＴＦ８に戻した[%s]\n", wk_q);
                    }
                }

//                wk_p += wk_len + 1;       /* +1は\0 */
//                wk_q += strlen(wk_q) + 1; /* +1は\0 */
            }
        }

        /* ＡＰログ出力用の共通情報を取得する */
        /* 日付、時刻 */
//        memset(wk_date, 0x00, sizeof(wk_date));
//        memset(wk_time, 0x00, sizeof(wk_time));
        ret = C_GetSysDateTime(wk_date, wk_time);

        /* サーバＩＤ、ＡＰログのディレクトリを取得する */
        wk_p = getenv(C_CM_SERVER_ID);
        if (wk_p == null) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : %s\n", "getenv NG(svrid)");
            }

            if ((901 <= n_msgid && n_msgid <= 909) || (102 <= n_msgid && n_msgid <= 103)) {
                wk_p = "    "; /* 固定値の場合はスペースにして処理続行 */
            } else {
                return C_const_NG;
            }
        }
        srvid = strcpy(srvid, wk_p);

        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : svrid=[%s]\n", srvid);
        }
        wk_p = getenv(C_CM_APLOG);
        if (wk_p == null) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : %s\n", "getenv NG(aplog)");
            }
            return C_const_NG;
        }
        aplogpath = strcpy(aplogpath, wk_p);

        /* ＡＰログファイルをオープンする */
        aplogpath = strcat(aplogpath, "/APLOG");
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : aplogpath=[%s]\n", aplogpath);
        }
        /* ファイルの存在チェック */
        ret = stat(aplogpath, st);
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : stat=%d\n", ret);
        }
        if (ret != 0) {
            if (dbg_aplogwrite == 1) {
                //todo
                C_DbgMsg("C_APLogWrite : stat:errno=%d\n", ret);
//                C_DbgMsg("C_APLogWrite : stat:errno=%d\n", errno);
            }
        }
        if (ret == 0) {
            /* ファイルあり */
            //, (O_WRONLY | O_APPEND)
            fd = open(aplogpath);
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : open=%d\n", fd.fd);
            }
        }
        if (ret != 0 || fd.fd < 0) {
            /* ファイルなし、または追加書き込みオープン失敗 */
            /* fd = open(aplogpath, (O_WRONLY | O_CREAT)); */
//            (O_WRONLY | O_APPEND | O_CREAT), 0664
            fd = open(aplogpath);
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : open(CREAT)=%d\n", fd.fd);
            }
            if (fd.fd < 0) {
                if (dbg_aplogwrite == 1) {
                    C_DbgMsg("C_APLogWrite : %s\n", "open NG\n");
                    C_DbgMsg("C_APLogWrite : aplog=[%s]\n", aplogpath);
                }
                return C_const_NG;
            }
        }

        /* ファイルをロックする */
        ret = fd.flock();
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : flock = %d\n", ret);
        }
        if (ret != C_const_OK) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : flock : errno = %d\n", fd.error);
            }
            fd.close();
            return C_const_NG;
        }

        /* ＡＰログファイルへ分割したメッセージを出力する */
//        wk_q = logbuf2;
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : write loop start : logcnt = %d\n", logcnt);
        }
        for (i = 0; i < logcnt; i++) {
            wk_q = logbuf2.split("\n")[i];
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : write loop  : i = [%d]\n", i);
                C_DbgMsg("C_APLogWrite : write loop  : wk_q = [%s]\n", wk_q);
            }
            /* 分割したメッセージを書き込む */
//            memset(wrtbuf, 0x00, sizeof(wrtbuf));
            if (i == 0) {
                wrtbuf = sprintf(wrtbuf, "%s%s%s %s %s %s %s:%s\n",
                        flg[0], " ", srvid, wk_date.arr, wk_time.arr,
                        Cg_Program_Name, msgid,
                        wk_q);
            } else {
                wrtbuf = sprintf(wrtbuf, "%s%s%s %s %s %s %s:%s\n",
                        "-", " ", "    ", "        ", "      ",
                        "         ", "   ",
                        wk_q);

            }
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : wrkbuf=[%s]\n", wrtbuf);
            }

            /* ファイル出力 */
            ret = fd.write(wrtbuf);
            if (ret != strlen(wrtbuf)) {
                if (dbg_aplogwrite == 1) {
                    C_DbgMsg("C_APLogWrite : %s\n", "write NG");
                }
                /* write error */
            }

//            wk_q += strlen(wk_q) + 1;
        }
        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : write loop end%s\n", "");
        }

        /* バッファに格納されているデータをフラッシュする */
        ret = fd.fsync();
        if (ret != C_const_OK) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : fsync : errno = %d", fd.error);
            }
        }

        /* ＡＰログファイルをクローズする */

        /* ロックの開放 */
        ret = fd.flock();
        if (ret != C_const_OK) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : flock : errno = %d", fd.error);
            }
        }

        ret = fd.close();
        if (ret != C_const_OK) {
            if (dbg_aplogwrite == 1) {
                C_DbgMsg("C_APLogWrite : close NG : errno = %d", fd.error);
            }
        }

        if (dbg_aplogwrite == 1) {
            C_DbgMsg("C_APLogWrite : %s", "end\n");
        }
        return C_const_OK;
    }


    @Override
    public int C_GetStoreCorpId(int store, int date, int corpid, int status) {
        /*String env_sid_sd;       *//** SID環境変数名        **//*
        String env_dblink_sd;        *//** DBLINK環境変数名     **//*
        String dblink_name_sd;    *//** ＳＩＤ（DBリンク用） **//*
        String wk_tbl;       *//** テーブル名           **//*
        String sql_buf;         *//** ＳＱＬ文編集用       **//*

         *//* ホスト変数 *//*
        int   wk_store;
        int   wk_date;
        int   wk_corpid;

        CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "start");

        *//* 引数のチェックを行う *//*
        if (corpid == null || status == null) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "prmerr");
            if (status != null) status[0] = CommonConstant.C_const_Stat_PRMERR;
            return CommonConstant.C_const_NG;
        }

        *//* 引数のチェックを行う *//*
        if (store == 0 || date == 0) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "prmerr");
            status[0] = CommonConstant.C_const_Stat_PRMERR;
            return CommonConstant.C_const_NG;
        }

        *//* ＤＢコネクトのチェックを行う *//*
        if (Cg_ORASID.length() == 0 || Cg_ORAUSR.length() == 0 || Cg_ORAPWD.length() == 0) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s", "conncect check NG\n");
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* 環境変数からＳＩＤを取得する *//*
        env_sid_sd = getenv(C_CM_ORA_SID_SD);
        if (env_sid_sd == null) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "getenv NG(SID)");
            status[0] = CommonConstant.C_const_Stat_ENVERR;
            return CommonConstant.C_const_NG;
        }

        *//* 環境変数からDBLINK名を取得する *//*
        env_dblink_sd = getenv(C_CM_ORA_DBLINK_SD);
        if (env_dblink_sd == null) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "getenv NG(DBLINK)");
            status[0] = CommonConstant.C_const_Stat_ENVERR;
            return CommonConstant.C_const_NG;
        }

        *//* グローバル変数と環境変数を比較してＤＢリンク名を設定 *//*
        if (Cg_ORASID.equals(env_sid_sd)) {
            dblink_name_sd = "";
        }
        else {
            dblink_name_sd = "@" + env_dblink_sd;
        }
        CommonUtil.C_DbgMsg("C_GetStoreCorpId : DBリンク名 = [%s]\n", dblink_name_sd );

        *//* テーブル名とＤＢリンク名を設定 *//*
        wk_tbl = "PS店表示情報";
        if (dblink_name_sd.length() != 0) {
            wk_tbl += dblink_name_sd;
        }

        *//* ＳＱＬ用のホスト変数を編集する *//*
         *//* 店番号 *//*
        wk_store = store;
        wk_date = date;

        *//* ＳＱＬを生成する *//*
        sql_buf = " select 企業コード into :wk_corpid from ";
        sql_buf += wk_tbl;
        sql_buf += " where 店番号 = :wk_store ";
        sql_buf += " and 開始年月日 <= :wk_date ";
        sql_buf += " and 終了年月日 >= :wk_date ";
        CommonUtil.C_DbgMsg("C_GetStoreCorpId : sql_buf=[%s]\n", sql_buf);

        *//* ＳＱＬ文をセットする *//*
        String WRKSQL = sql_buf;

        *//* EXEC SQL PREPARE sql_getstorecorpid from :WRKSQL; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* EXEC SQL DECLARE cur_getstorecorpid cursor for sql_getstorecorpid; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* select文を発行する *//*
         *//* カーソルのオープン *//*
         *//* EXEC SQL OPEN cur_getstorecorpid using :wk_store, :wk_date, :wk_date; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* カーソルのフェッチ *//*
         *//* EXEC SQL FETCH cur_getstorecorpid into :wk_corpid; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* カーソルのフェッチ（２件以上データあるか確認） *//*
         *//* EXEC SQL FETCH cur_getstorecorpid into :wk_corpid; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_NOTFOUND) {
            if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
                CommonUtil.C_DbgMsg("C_GetStoreCorpId : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                status[0] = CommonConstant.C_const_Stat_DBERR;
                return CommonConstant.C_const_NG;
            }
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : FETCH : %s\n", "件数エラー");
            status[0] = CommonConstant.C_const_Stat_DBERR;
            return CommonConstant.C_const_NG;
        }

        *//* カーソルのクローズ *//*
         *//* EXEC SQL CLOSE cur_getstorecorpid; *//*

        if (sqlca.sqlcode != CommonConstant.C_const_Ora_OK) {
            CommonUtil.C_DbgMsg("C_GetStoreCorpId : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
        }

        *//* 処理企業コード取得 *//*
        corpid[0] = wk_corpid;

        CommonUtil.C_DbgMsg("C_GetStoreCorpId : 処理企業コード = [%d]\n", corpid[0]);
        CommonUtil.C_DbgMsg("C_GetStoreCorpId : %s\n", "end");

        *//* 正常終了 *//*
        status[0] = CommonConstant.C_const_Stat_OK;*/
        return C_aplcom1Service.C_const_OK;
    }

    @Override
    public int C_GetPidCorpId(String pid, int corpid, int status) {
        return 0;
    }

    @Override
    public int C_GetBatDate(int adjust, StringDto date, IntegerDto status) {
        String penv;
        StringDto wk_sid = new StringDto(16);
        StringDto wk_dblink = new StringDto(16);
        StringDto wk_day = new StringDto(10);
        String calc_date = null; /* 計算日数 */
        String wk_sql = null;
        StringDto wk_day2 = new StringDto(10);
        StringDto wk_tbl = new StringDto(128);
        StringDto DATA01 = new StringDto(100);
        int result;

        if (dbg_getbatdate == 1) {
            C_DbgMsg("C_GetBatDate : %s\n", "start");
        }
        /* 引数のチェックを行う */
        if (date == null || status == null) {
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : %s\n", "prmerr");
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /*         引数のadjustが０、１，２以外の場合はパラメータエラー */
        /*        if (adjust != 0 && adjust !=1 && adjust !=2) {        */
        /*if (dbg_getbatdate == 1) {                        */
        /*                C_DbgMsg("C_GetBatDate : %s", "adjust NG\n");     */
        /*}                                 */
        /*                *status = C_const_Stat_PRMERR;            */
        /*                return C_const_NG;                    */
        /*        }                             */

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : %s", "conncect check NG\n");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 環境変数からＳＩＤを取得する */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : %s\n", "getenv NG(SID)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }
        memset(wk_sid, 0x00, sizeof(wk_sid));
        wk_sid.arr = strncpy(wk_sid.arr, penv, sizeof(wk_sid) - 1);

        /* 環境変数からＤＢＬＩＮＫ名を取得する */
        penv = getenv(C_CM_ORA_DBLINK_SD);
        if (StringUtils.isEmpty(penv)) {
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : %s\n", "getenv NG(DBLINK)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }
        memset(wk_dblink, 0x00, sizeof(wk_dblink));
        wk_dblink.arr = strncpy(wk_dblink.arr, penv, sizeof(wk_dblink) - 1);

        wk_tbl.arr = strcpy(wk_tbl.arr, "PSバッチ日付情報");
        if (strcmp(Cg_ORASID, wk_sid.arr) != 0) {
            wk_tbl.arr = strcat(wk_tbl.arr, "@");
            wk_tbl.arr = strcat(wk_tbl.arr, wk_dblink.arr);
        }

        /* 環境変数からCM_TODAYを取得する */
        memset(wk_day, 0x00, sizeof(wk_day));
        penv = getenv(C_CM_TODAY);
        if (StringUtils.isNotEmpty(penv) && strlen(penv) == 8) {
            memset(wk_day, 0x00, sizeof(wk_day));
            wk_day.arr = memcpy(wk_day.arr, penv, 8); /* YYYYMMDD */
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : CM_TODAY=[%s]\n", wk_day);
            }
        } else {
            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : %s\n", "getenv=null(CM_TODAY)");
            }
        }

        /* 計算日数をセットする */
        /*        if (adjust == 1) {                */
        /*                strcpy(calc_date, " +1 ");        */
        /*        }                     */
        /*        else if (adjust == 2) {           */
        /*                strcpy(calc_date, " -1 ");        */
        /*        }                     */
        /*        else {                    */
        /*                strcpy(calc_date, "");        */
        /*        }                     */
        if (adjust == 0) {
            calc_date = strcpy(calc_date, "");
        } else {
            calc_date = sprintf(calc_date, "%+d", adjust);
        }

        memset(wk_day2, 0x00, sizeof(wk_day2));
        StringDto WRKSQL = new StringDto();
        if (strlen(wk_day.arr) == 8) {
            /* 対象の日付を取得する（CM_TODAYを取得している場合は、マシン日付を元にする) */

            wk_sql = sprintf(wk_sql, "select to_char(to_date('%s', 'YYYYMMDD') %s, 'YYYYMMDD') from dual ",
                    wk_day, calc_date);

            WRKSQL.arr = strcpy(WRKSQL.arr, wk_sql);
            WRKSQL.len = strlen(WRKSQL.arr);

            result = C_const_Ora_OK;

            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : WRKSQL=[%s]\n", WRKSQL.arr);
            }
            SqlstmDto sqlca = sqlcaManager.get("cur_getbatdate");
            sqlca.sql = WRKSQL;
            /* EXEC SQL PREPARE sql_getbatdate from :WRKSQL; */
            sqlca.prepare();


            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */

                result = C_const_Stat_DBERR;
            }

            if (result == C_const_Ora_OK) {
                /* EXEC SQL DECLARE cur_getbatdate CURSOR FOR sql_getbatdate; */

            }

            if (result == C_const_Ora_OK) {
                /* EXEC SQL OPEN cur_getbatdate; */

                sqlca.open();
                if (sqlca.sqlcode != C_const_Ora_OK) {
                    /* DBERR */
                    result = C_const_Stat_DBERR;
                }
            }

            if (result == C_const_Ora_OK) {
                /* EXEC SQL FETCH cur_getbatdate into :DATA01; */

                sqlca.fetch();
                sqlca.recData(DATA01);
//                DATA01.arr = sqlca.getDataByIndex(1);
                if (sqlca.sqlcode == C_const_Ora_OK) {
                    wk_day2.arr = memcpy(wk_day2.arr, DATA01.arr, 8);
                    strcpy(date, wk_day2.arr);
                }
            }

            /* EXEC SQL CLOSE cur_getbatdate; */
//            sqlca.close();
            sqlcaManager.close("cur_getbatdate");
        }

        /* 対象の日付を取得する（CM_TODAYを取得できなかった場合は、テーブルを元にする) */
        if (strlen(wk_day.arr) == 0 || strlen(wk_day2.arr) == 0) {
            wk_sql = sprintf(wk_sql, "select to_char(to_date(%s, 'YYYYMMDD') %s, 'YYYYMMDD') from %s ",
                    "max(バッチ処理年月日)", calc_date, wk_tbl);

            WRKSQL.arr = strcpy(WRKSQL.arr, wk_sql);
            WRKSQL.len = strlen(WRKSQL.arr);

            if (dbg_getbatdate == 1) {
                C_DbgMsg("C_GetBatDate : WRKSQL=[%s]\n", WRKSQL.arr);
            }
            SqlstmDto sqlca = sqlcaManager.get("cur_getbatdate2");
            /* EXEC SQL PREPARE sql_getbatdate from :WRKSQL; */
            sqlca.sql = WRKSQL;

            sqlca.prepare();


            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                memset(date, 0x00, 8);
                status.arr = C_const_Stat_DBERR;
                if (dbg_getbatdate == 1) {
                    C_DbgMsg("C_GetBatDate : %s\n", "PREPARE NG");
                }
                return C_const_NG;
            }

            /* EXEC SQL DECLARE cur_getbatdate2 CURSOR FOR sql_getbatdate; */


            /* EXEC SQL OPEN cur_getbatdate2; */

            sqlca.open();


            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                memset(date, 0x00, 8);
                if (dbg_getbatdate == 1) {
                    C_DbgMsg("C_GetBatDate : %s\n", "OPEN NG");
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            /* EXEC SQL FETCH cur_getbatdate2 into :DATA01; */

            sqlca.fetch();


            if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(date, 0x00, 8);
                if (dbg_getbatdate == 1) {
                    C_DbgMsg("C_GetBatDate : %s\n", "FETCH NG");
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
            sqlca.recData(DATA01);
            wk_day2.arr = memcpy(wk_day2.arr, DATA01.arr, 8);

            /* EXEC SQL CLOSE cur_getbatdate2; */

//            sqlca.close();
            sqlcaManager.close("cur_getbatdate2");

            if (strlen(wk_day2.arr) == 0) {
                memset(date, 0x00, 8);
                if (dbg_getbatdate == 1) {
                    C_DbgMsg("C_GetBatDate : %s\n", "CLOSE NG");
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }

            strcpy(date, wk_day2.arr);

        }

        if (dbg_getbatdate == 1) {
            C_DbgMsg("C_GetBatDate : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetSysDateTime                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_GetSysDateTime(char *date, char *time)                         */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              システム日付時刻取得処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    date  ： 取得した日付を格納                   */
    /*              char       *    time  ： 取得した時間を格納                   */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetSysDateTime(StringDto date, StringDto time) {

        String wk_buf = null;

        if (dbg_getsysdatetime == 1) {
            C_DbgMsg("C_GetSysDateTime : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (date == null || time == null) {
            if (dbg_getsysdatetime == 1) {
                C_DbgMsg("C_GetSysDateTime : %s\n", "prmerr");
            }
            return C_const_NG;
        }

        /* システム日付・システム時刻を取得する */
//        ftime(&tmb);
//        tm = localtime((time_t *)&tmb.time);
        Calendar calendar = Calendar.getInstance();
        wk_buf = sprintf(wk_buf, "%04d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        date.arr = memcpy(date.arr, wk_buf, 8);

        wk_buf = sprintf(wk_buf, "%02d%02d%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        time.arr = memcpy(time.arr, wk_buf, 6);

        if (dbg_getsysdatetime == 1) {
            C_DbgMsg("C_GetSysDateTime : %s\n", "end");
        }
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_ConvHalf2Full                                             */
    /*                                                                            */
    /*  書式                                                                  */
    /*  int  C_ConvHalf2Full(char *halfstr, char *fullstr)                    */
    /*                                                                            */
    /*  【説明】                                                              */
    /*              半角→全角変換処理                                            */
    /*              引数で指定された半角文字列を全角に変換する                    */
    /*              入出力ともにUTF8とする（SJIS変換は行わない）                  */
    /*                                                                            */
    /*  【引数】                                                              */
    /*      char       *    halfstr    ： 半角文字列                      */
    /*      char       *    fullstr    ： 全角文字列                      */
    /*                                                                            */
    /*  【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*              2   ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_ConvHalf2Full(StringDto halfstr, StringDto fullstr) {

        StringDto wkbuf = new StringDto();
//        String ptr =null;

        if (dbg_convhalf2full == 1) {
            C_DbgMsg("C_ConvHalf2Full : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (halfstr == null || fullstr == null) {

            if (dbg_convhalf2full == 1) {
                C_DbgMsg("C_ConvHalf2Full : %s\n", "prmerr");
            }

            return C_const_PARAMERR;
        }

        memset(wkbuf, 0x00, strlen(halfstr) * 3 + 1);

        /* for (ptr=halfstr; *ptr!='\0'; *ptr++) { */
//       char[] temps =  halfstr.arr.toCharArray();
        for (int i = 0; i < halfstr.arr.length(); i++) {
            String ptr1 = String.valueOf(halfstr.arr.charAt(i));
            int ptr = 0;

            if (strncmp(ptr1, " ", 1) == 0) {
                strcat(wkbuf, "　");
            } else if (strncmp(ptr1, "!", 1) == 0) {
                strcat(wkbuf, "！");
            } else if (strncmp(ptr1, "\"", 1) == 0) {
                strcat(wkbuf, "”");
            } else if (strncmp(ptr1, "#", 1) == 0) {
                strcat(wkbuf, "＃");
            } else if (strncmp(ptr1, "$", 1) == 0) {
                strcat(wkbuf, "＄");
            } else if (strncmp(ptr1, "%", 1) == 0) {
                strcat(wkbuf, "％");
            } else if (strncmp(ptr1, "&", 1) == 0) {
                strcat(wkbuf, "＆");
            } else if (strncmp(ptr1, "'", 1) == 0) {
                strcat(wkbuf, "’");
            } else if (strncmp(ptr1, "(", 1) == 0) {
                strcat(wkbuf, "（");
            } else if (strncmp(ptr1, ")", 1) == 0) {
                strcat(wkbuf, "）");
            } else if (strncmp(ptr1, "*", 1) == 0) {
                strcat(wkbuf, "＊");
            } else if (strncmp(ptr1, "+", 1) == 0) {
                strcat(wkbuf, "＋");
            } else if (strncmp(ptr1, ",", 1) == 0) {
                strcat(wkbuf, "，");
            } else if (strncmp(ptr1, "-", 1) == 0) {
                strcat(wkbuf, "－");
            } else if (strncmp(ptr1, ".", 1) == 0) {
                strcat(wkbuf, "．");
            } else if (strncmp(ptr1, "/", 1) == 0) {
                strcat(wkbuf, "／");
            } else if (strncmp(ptr1, "0", 1) == 0) {
                strcat(wkbuf, "０");
            } else if (strncmp(ptr1, "1", 1) == 0) {
                strcat(wkbuf, "１");
            } else if (strncmp(ptr1, "2", 1) == 0) {
                strcat(wkbuf, "２");
            } else if (strncmp(ptr1, "3", 1) == 0) {
                strcat(wkbuf, "３");
            } else if (strncmp(ptr1, "4", 1) == 0) {
                strcat(wkbuf, "４");
            } else if (strncmp(ptr1, "5", 1) == 0) {
                strcat(wkbuf, "５");
            } else if (strncmp(ptr1, "6", 1) == 0) {
                strcat(wkbuf, "６");
            } else if (strncmp(ptr1, "7", 1) == 0) {
                strcat(wkbuf, "７");
            } else if (strncmp(ptr1, "8", 1) == 0) {
                strcat(wkbuf, "８");
            } else if (strncmp(ptr1, "9", 1) == 0) {
                strcat(wkbuf, "９");
            } else if (strncmp(ptr1, ":", 1) == 0) {
                strcat(wkbuf, "：");
            } else if (strncmp(ptr1, ";", 1) == 0) {
                strcat(wkbuf, "；");
            } else if (strncmp(ptr1, "<", 1) == 0) {
                strcat(wkbuf, "＜");
            } else if (strncmp(ptr1, "=", 1) == 0) {
                strcat(wkbuf, "＝");
            } else if (strncmp(ptr1, ">", 1) == 0) {
                strcat(wkbuf, "＞");
            } else if (strncmp(ptr1, "?", 1) == 0) {
                strcat(wkbuf, "？");
            } else if (strncmp(ptr1, "@", 1) == 0) {
                strcat(wkbuf, "＠");
            } else if (strncmp(ptr1, "A", 1) == 0) {
                strcat(wkbuf, "Ａ");
            } else if (strncmp(ptr1, "B", 1) == 0) {
                strcat(wkbuf, "Ｂ");
            } else if (strncmp(ptr1, "C", 1) == 0) {
                strcat(wkbuf, "Ｃ");
            } else if (strncmp(ptr1, "D", 1) == 0) {
                strcat(wkbuf, "Ｄ");
            } else if (strncmp(ptr1, "E", 1) == 0) {
                strcat(wkbuf, "Ｅ");
            } else if (strncmp(ptr1, "F", 1) == 0) {
                strcat(wkbuf, "Ｆ");
            } else if (strncmp(ptr1, "G", 1) == 0) {
                strcat(wkbuf, "Ｇ");
            } else if (strncmp(ptr1, "H", 1) == 0) {
                strcat(wkbuf, "Ｈ");
            } else if (strncmp(ptr1, "I", 1) == 0) {
                strcat(wkbuf, "Ｉ");
            } else if (strncmp(ptr1, "J", 1) == 0) {
                strcat(wkbuf, "Ｊ");
            } else if (strncmp(ptr1, "K", 1) == 0) {
                strcat(wkbuf, "Ｋ");
            } else if (strncmp(ptr1, "L", 1) == 0) {
                strcat(wkbuf, "Ｌ");
            } else if (strncmp(ptr1, "M", 1) == 0) {
                strcat(wkbuf, "Ｍ");
            } else if (strncmp(ptr1, "N", 1) == 0) {
                strcat(wkbuf, "Ｎ");
            } else if (strncmp(ptr1, "O", 1) == 0) {
                strcat(wkbuf, "Ｏ");
            } else if (strncmp(ptr1, "P", 1) == 0) {
                strcat(wkbuf, "Ｐ");
            } else if (strncmp(ptr1, "Q", 1) == 0) {
                strcat(wkbuf, "Ｑ");
            } else if (strncmp(ptr1, "R", 1) == 0) {
                strcat(wkbuf, "Ｒ");
            } else if (strncmp(ptr1, "S", 1) == 0) {
                strcat(wkbuf, "Ｓ");
            } else if (strncmp(ptr1, "T", 1) == 0) {
                strcat(wkbuf, "Ｔ");
            } else if (strncmp(ptr1, "U", 1) == 0) {
                strcat(wkbuf, "Ｕ");
            } else if (strncmp(ptr1, "V", 1) == 0) {
                strcat(wkbuf, "Ｖ");
            } else if (strncmp(ptr1, "W", 1) == 0) {
                strcat(wkbuf, "Ｗ");
            } else if (strncmp(ptr1, "X", 1) == 0) {
                strcat(wkbuf, "Ｘ");
            } else if (strncmp(ptr1, "Y", 1) == 0) {
                strcat(wkbuf, "Ｙ");
            } else if (strncmp(ptr1, "Z", 1) == 0) {
                strcat(wkbuf, "Ｚ");
            } else if (strncmp(ptr1, "[", 1) == 0) {
                strcat(wkbuf, "［");
            } else if (strncmp(ptr1, "\\", 1) == 0) {
                strcat(wkbuf, "￥");
            } else if (strncmp(ptr1, "]", 1) == 0) {
                strcat(wkbuf, "］");
            } else if (strncmp(ptr1, "^", 1) == 0) {
                strcat(wkbuf, "＾");
            } else if (strncmp(ptr1, "_", 1) == 0) {
                strcat(wkbuf, "＿");
            } else if (strncmp(ptr1, "`", 1) == 0) {
                strcat(wkbuf, "‘");
            } else if (strncmp(ptr1, "a", 1) == 0) {
                strcat(wkbuf, "ａ");
            } else if (strncmp(ptr1, "b", 1) == 0) {
                strcat(wkbuf, "ｂ");
            } else if (strncmp(ptr1, "c", 1) == 0) {
                strcat(wkbuf, "ｃ");
            } else if (strncmp(ptr1, "d", 1) == 0) {
                strcat(wkbuf, "ｄ");
            } else if (strncmp(ptr1, "e", 1) == 0) {
                strcat(wkbuf, "ｅ");
            } else if (strncmp(ptr1, "f", 1) == 0) {
                strcat(wkbuf, "ｆ");
            } else if (strncmp(ptr1, "g", 1) == 0) {
                strcat(wkbuf, "ｇ");
            } else if (strncmp(ptr1, "h", 1) == 0) {
                strcat(wkbuf, "ｈ");
            } else if (strncmp(ptr1, "i", 1) == 0) {
                strcat(wkbuf, "ｉ");
            } else if (strncmp(ptr1, "j", 1) == 0) {
                strcat(wkbuf, "ｊ");
            } else if (strncmp(ptr1, "k", 1) == 0) {
                strcat(wkbuf, "ｋ");
            } else if (strncmp(ptr1, "l", 1) == 0) {
                strcat(wkbuf, "ｌ");
            } else if (strncmp(ptr1, "m", 1) == 0) {
                strcat(wkbuf, "ｍ");
            } else if (strncmp(ptr1, "n", 1) == 0) {
                strcat(wkbuf, "ｎ");
            } else if (strncmp(ptr1, "o", 1) == 0) {
                strcat(wkbuf, "ｏ");
            } else if (strncmp(ptr1, "p", 1) == 0) {
                strcat(wkbuf, "ｐ");
            } else if (strncmp(ptr1, "q", 1) == 0) {
                strcat(wkbuf, "ｑ");
            } else if (strncmp(ptr1, "r", 1) == 0) {
                strcat(wkbuf, "ｒ");
            } else if (strncmp(ptr1, "s", 1) == 0) {
                strcat(wkbuf, "ｓ");
            } else if (strncmp(ptr1, "t", 1) == 0) {
                strcat(wkbuf, "ｔ");
            } else if (strncmp(ptr1, "u", 1) == 0) {
                strcat(wkbuf, "ｕ");
            } else if (strncmp(ptr1, "v", 1) == 0) {
                strcat(wkbuf, "ｖ");
            } else if (strncmp(ptr1, "w", 1) == 0) {
                strcat(wkbuf, "ｗ");
            } else if (strncmp(ptr1, "x", 1) == 0) {
                strcat(wkbuf, "ｘ");
            } else if (strncmp(ptr1, "y", 1) == 0) {
                strcat(wkbuf, "ｙ");
            } else if (strncmp(ptr1, "z", 1) == 0) {
                strcat(wkbuf, "ｚ");
            } else if (strncmp(ptr1, "{", 1) == 0) {
                strcat(wkbuf, "｛");
            } else if (strncmp(ptr1, "|", 1) == 0) {
                strcat(wkbuf, "｜");
            } else if (strncmp(ptr1, "}", 1) == 0) {
                strcat(wkbuf, "｝");
            } else if (strncmp(ptr1, "ｳﾞ", 6) == 0) {
                strcat(wkbuf, "ヴ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｱ", 3) == 0) {
                strcat(wkbuf, "ア");
                ptr += 2;
            } else if (strncmp(ptr1, "ｧ", 3) == 0) {
                strcat(wkbuf, "ァ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｲ", 3) == 0) {
                strcat(wkbuf, "イ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｨ", 3) == 0) {
                strcat(wkbuf, "ィ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｳ", 3) == 0) {
                strcat(wkbuf, "ウ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｩ", 3) == 0) {
                strcat(wkbuf, "ゥ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｴ", 3) == 0) {
                strcat(wkbuf, "エ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｪ", 3) == 0) {
                strcat(wkbuf, "ェ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｵ", 3) == 0) {
                strcat(wkbuf, "オ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｫ", 3) == 0) {
                strcat(wkbuf, "ォ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｶﾞ", 6) == 0) {
                strcat(wkbuf, "ガ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｷﾞ", 6) == 0) {
                strcat(wkbuf, "ギ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｸﾞ", 6) == 0) {
                strcat(wkbuf, "グ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｹﾞ", 6) == 0) {
                strcat(wkbuf, "ゲ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｺﾞ", 6) == 0) {
                strcat(wkbuf, "ゴ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｶ", 3) == 0) {
                strcat(wkbuf, "カ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｷ", 3) == 0) {
                strcat(wkbuf, "キ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｸ", 3) == 0) {
                strcat(wkbuf, "ク");
                ptr += 2;
            } else if (strncmp(ptr1, "ｹ", 3) == 0) {
                strcat(wkbuf, "ケ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｺ", 3) == 0) {
                strcat(wkbuf, "コ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｻﾞ", 6) == 0) {
                strcat(wkbuf, "ザ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｼﾞ", 6) == 0) {
                strcat(wkbuf, "ジ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｽﾞ", 6) == 0) {
                strcat(wkbuf, "ズ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｾﾞ", 6) == 0) {
                strcat(wkbuf, "ゼ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｿﾞ", 6) == 0) {
                strcat(wkbuf, "ゾ");
                ptr += 5;
            } else if (strncmp(ptr1, "ｻ", 3) == 0) {
                strcat(wkbuf, "サ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｼ", 3) == 0) {
                strcat(wkbuf, "シ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｽ", 3) == 0) {
                strcat(wkbuf, "ス");
                ptr += 2;
            } else if (strncmp(ptr1, "ｾ", 3) == 0) {
                strcat(wkbuf, "セ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｿ", 3) == 0) {
                strcat(wkbuf, "ソ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾀﾞ", 6) == 0) {
                strcat(wkbuf, "ダ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾁﾞ", 6) == 0) {
                strcat(wkbuf, "ヂ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾂﾞ", 6) == 0) {
                strcat(wkbuf, "ヅ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾃﾞ", 6) == 0) {
                strcat(wkbuf, "デ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾄﾞ", 6) == 0) {
                strcat(wkbuf, "ド");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾀ", 3) == 0) {
                strcat(wkbuf, "タ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾁ", 3) == 0) {
                strcat(wkbuf, "チ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾂ", 3) == 0) {
                strcat(wkbuf, "ツ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｯ", 3) == 0) {
                strcat(wkbuf, "ッ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾃ", 3) == 0) {
                strcat(wkbuf, "テ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾄ", 3) == 0) {
                strcat(wkbuf, "ト");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾅ", 3) == 0) {
                strcat(wkbuf, "ナ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾆ", 3) == 0) {
                strcat(wkbuf, "ニ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾇ", 3) == 0) {
                strcat(wkbuf, "ヌ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾈ", 3) == 0) {
                strcat(wkbuf, "ネ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾉ", 3) == 0) {
                strcat(wkbuf, "ノ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾊﾞ", 6) == 0) {
                strcat(wkbuf, "バ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾋﾞ", 6) == 0) {
                strcat(wkbuf, "ビ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾌﾞ", 6) == 0) {
                strcat(wkbuf, "ブ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾍﾞ", 6) == 0) {
                strcat(wkbuf, "ベ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾎﾞ", 6) == 0) {
                strcat(wkbuf, "ボ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾊﾟ", 6) == 0) {
                strcat(wkbuf, "パ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾋﾟ", 6) == 0) {
                strcat(wkbuf, "ピ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾌﾟ", 6) == 0) {
                strcat(wkbuf, "プ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾍﾟ", 6) == 0) {
                strcat(wkbuf, "ペ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾎﾟ", 6) == 0) {
                strcat(wkbuf, "ポ");
                ptr += 5;
            } else if (strncmp(ptr1, "ﾊ", 3) == 0) {
                strcat(wkbuf, "ハ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾋ", 3) == 0) {
                strcat(wkbuf, "ヒ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾌ", 3) == 0) {
                strcat(wkbuf, "フ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾍ", 3) == 0) {
                strcat(wkbuf, "ヘ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾎ", 3) == 0) {
                strcat(wkbuf, "ホ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾏ", 3) == 0) {
                strcat(wkbuf, "マ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾐ", 3) == 0) {
                strcat(wkbuf, "ミ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾑ", 3) == 0) {
                strcat(wkbuf, "ム");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾒ", 3) == 0) {
                strcat(wkbuf, "メ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾓ", 3) == 0) {
                strcat(wkbuf, "モ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾔ", 3) == 0) {
                strcat(wkbuf, "ヤ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｬ", 3) == 0) {
                strcat(wkbuf, "ャ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾕ", 3) == 0) {
                strcat(wkbuf, "ユ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｭ", 3) == 0) {
                strcat(wkbuf, "ュ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾖ", 3) == 0) {
                strcat(wkbuf, "ヨ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｮ", 3) == 0) {
                strcat(wkbuf, "ョ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾗ", 3) == 0) {
                strcat(wkbuf, "ラ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾘ", 3) == 0) {
                strcat(wkbuf, "リ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾙ", 3) == 0) {
                strcat(wkbuf, "ル");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾚ", 3) == 0) {
                strcat(wkbuf, "レ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾛ", 3) == 0) {
                strcat(wkbuf, "ロ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾜ", 3) == 0) {
                strcat(wkbuf, "ワ");
                ptr += 2;
            } else if (strncmp(ptr1, "ｦ", 3) == 0) {
                strcat(wkbuf, "ヲ");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾝ", 3) == 0) {
                strcat(wkbuf, "ン");
                ptr += 2;
            } else if (strncmp(ptr1, "ｰ", 3) == 0) {
                strcat(wkbuf, "ー");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾞ", 3) == 0) {
                strcat(wkbuf, "゛");
                ptr += 2;
            } else if (strncmp(ptr1, "ﾟ", 3) == 0) {
                strcat(wkbuf, "゜");
                ptr += 2;
            } else if (strncmp(ptr1, "､", 3) == 0) {
                strcat(wkbuf, "、");
                ptr += 2;
            } else if (strncmp(ptr1, "｡", 3) == 0) {
                strcat(wkbuf, "。");
                ptr += 2;
            } else if (strncmp(ptr1, "･", 3) == 0) {
                strcat(wkbuf, "・");
                ptr += 2;
            } else if (strncmp(ptr1, "｣", 3) == 0) {
                strcat(wkbuf, "」");
                ptr += 2;
            } else if (strncmp(ptr1, "｢", 3) == 0) {
                strcat(wkbuf, "「");
                ptr += 2;
            } else {
                strncat(wkbuf, ptr1, 1);
            }
//               else { strncat(wkbuf, ptr, 1); }
        }

        memcpy(fullstr, wkbuf.arr, strlen(wkbuf));

        if (dbg_convhalf2full == 1) {
            C_DbgMsg("C_ConvHalf2Full : %s\n", "end");
        }

        /* 正常終了 */
        return C_const_OK;
    }

    public int C_ConvFull2Half(String[] strp, StringDto halfstr) {
        StringDto item = new StringDto();
        item.arr = strp[0];
        int result = C_ConvFull2Half(item, halfstr);
        strp[0] = item.arr;
        return result;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_ConvFull2Half                                             */
    /*                                                                            */
    /*  書式                                                                  */
    /*  int  C_ConvFull2Half(char *fullstr, char *halfstr)                    */
    /*                                                                            */
    /*  【説明】                                                              */
    /*              全角→半角変換処理                                            */
    /*              引数で指定された全角文字列を半角に変換する                    */
    /*              入出力ともにUTF8とする（SJIS変換は行わない）                  */
    /*                                                                            */
    /*  【引数】                                                              */
    /*      char       *    fullstr    ： 全角文字列                      */
    /*      char       *    halfstr    ： 半角文字列                      */
    /*                                                                            */
    /*  【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*              2   ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_ConvFull2Half(StringDto fullstr, StringDto halfstr) {
        StringDto wkbuf = new StringDto();
//        String ptr=null;

        if (dbg_convfull2half == 1) {
            C_DbgMsg("C_ConvFull2Half : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (halfstr == null || fullstr == null) {

            if (dbg_convfull2half == 1) {
                C_DbgMsg("C_ConvFull2Half : %s\n", "prmerr");
            }

            return C_const_PARAMERR;
        }

        memset(wkbuf, 0x00, strlen(fullstr) * 2 + 1);

        /* for (ptr=fullstr; *ptr!='\0'; *ptr++) { */
        for (int i = 0; i < fullstr.arr.length(); i++) {
            String ptr1 = String.valueOf(fullstr.arr.charAt(i));
//        for (ptr=fullstr; *ptr!='\0'; ptr++) {
            int ptr = 0;
            if (strncmp(ptr1, "　", 3) == 0) {
                strcat(wkbuf, " ");
                ptr += 2;
            } else if (strncmp(ptr1, "！", 3) == 0) {
                strcat(wkbuf, "!");
                ptr += 2;
            } else if (strncmp(ptr1, "”", 3) == 0) {
                strcat(wkbuf, "\"");
                ptr += 2;
            } else if (strncmp(ptr1, "＃", 3) == 0) {
                strcat(wkbuf, "#");
                ptr += 2;
            } else if (strncmp(ptr1, "＄", 3) == 0) {
                strcat(wkbuf, "$");
                ptr += 2;
            } else if (strncmp(ptr1, "％", 3) == 0) {
                strcat(wkbuf, "%");
                ptr += 2;
            } else if (strncmp(ptr1, "＆", 3) == 0) {
                strcat(wkbuf, "&");
                ptr += 2;
            } else if (strncmp(ptr1, "’", 3) == 0) {
                strcat(wkbuf, "'");
                ptr += 2;
            } else if (strncmp(ptr1, "（", 3) == 0) {
                strcat(wkbuf, "(");
                ptr += 2;
            } else if (strncmp(ptr1, "）", 3) == 0) {
                strcat(wkbuf, ")");
                ptr += 2;
            } else if (strncmp(ptr1, "＊", 3) == 0) {
                strcat(wkbuf, "*");
                ptr += 2;
            } else if (strncmp(ptr1, "＋", 3) == 0) {
                strcat(wkbuf, "+");
                ptr += 2;
            } else if (strncmp(ptr1, "，", 3) == 0) {
                strcat(wkbuf, ",");
                ptr += 2;
            } else if (strncmp(ptr1, "－", 3) == 0) {
                strcat(wkbuf, "-");
                ptr += 2;
            } else if (strncmp(ptr1, "．", 3) == 0) {
                strcat(wkbuf, ".");
                ptr += 2;
            } else if (strncmp(ptr1, "／", 3) == 0) {
                strcat(wkbuf, "/");
                ptr += 2;
            } else if (strncmp(ptr1, "０", 3) == 0) {
                strcat(wkbuf, "0");
                ptr += 2;
            } else if (strncmp(ptr1, "１", 3) == 0) {
                strcat(wkbuf, "1");
                ptr += 2;
            } else if (strncmp(ptr1, "２", 3) == 0) {
                strcat(wkbuf, "2");
                ptr += 2;
            } else if (strncmp(ptr1, "３", 3) == 0) {
                strcat(wkbuf, "3");
                ptr += 2;
            } else if (strncmp(ptr1, "４", 3) == 0) {
                strcat(wkbuf, "4");
                ptr += 2;
            } else if (strncmp(ptr1, "５", 3) == 0) {
                strcat(wkbuf, "5");
                ptr += 2;
            } else if (strncmp(ptr1, "６", 3) == 0) {
                strcat(wkbuf, "6");
                ptr += 2;
            } else if (strncmp(ptr1, "７", 3) == 0) {
                strcat(wkbuf, "7");
                ptr += 2;
            } else if (strncmp(ptr1, "８", 3) == 0) {
                strcat(wkbuf, "8");
                ptr += 2;
            } else if (strncmp(ptr1, "９", 3) == 0) {
                strcat(wkbuf, "9");
                ptr += 2;
            } else if (strncmp(ptr1, "：", 3) == 0) {
                strcat(wkbuf, ":");
                ptr += 2;
            } else if (strncmp(ptr1, "；", 3) == 0) {
                strcat(wkbuf, ";");
                ptr += 2;
            } else if (strncmp(ptr1, "＜", 3) == 0) {
                strcat(wkbuf, "<");
                ptr += 2;
            } else if (strncmp(ptr1, "＝", 3) == 0) {
                strcat(wkbuf, "=");
                ptr += 2;
            } else if (strncmp(ptr1, "＞", 3) == 0) {
                strcat(wkbuf, ">");
                ptr += 2;
            } else if (strncmp(ptr1, "？", 3) == 0) {
                strcat(wkbuf, "?");
                ptr += 2;
            } else if (strncmp(ptr1, "＠", 3) == 0) {
                strcat(wkbuf, "@");
                ptr += 2;
            } else if (strncmp(ptr1, "Ａ", 3) == 0) {
                strcat(wkbuf, "A");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｂ", 3) == 0) {
                strcat(wkbuf, "B");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｃ", 3) == 0) {
                strcat(wkbuf, "C");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｄ", 3) == 0) {
                strcat(wkbuf, "D");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｅ", 3) == 0) {
                strcat(wkbuf, "E");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｆ", 3) == 0) {
                strcat(wkbuf, "F");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｇ", 3) == 0) {
                strcat(wkbuf, "G");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｈ", 3) == 0) {
                strcat(wkbuf, "H");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｉ", 3) == 0) {
                strcat(wkbuf, "I");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｊ", 3) == 0) {
                strcat(wkbuf, "J");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｋ", 3) == 0) {
                strcat(wkbuf, "K");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｌ", 3) == 0) {
                strcat(wkbuf, "L");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｍ", 3) == 0) {
                strcat(wkbuf, "M");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｎ", 3) == 0) {
                strcat(wkbuf, "N");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｏ", 3) == 0) {
                strcat(wkbuf, "O");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｐ", 3) == 0) {
                strcat(wkbuf, "P");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｑ", 3) == 0) {
                strcat(wkbuf, "Q");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｒ", 3) == 0) {
                strcat(wkbuf, "R");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｓ", 3) == 0) {
                strcat(wkbuf, "S");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｔ", 3) == 0) {
                strcat(wkbuf, "T");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｕ", 3) == 0) {
                strcat(wkbuf, "U");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｖ", 3) == 0) {
                strcat(wkbuf, "V");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｗ", 3) == 0) {
                strcat(wkbuf, "W");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｘ", 3) == 0) {
                strcat(wkbuf, "X");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｙ", 3) == 0) {
                strcat(wkbuf, "Y");
                ptr += 2;
            } else if (strncmp(ptr1, "Ｚ", 3) == 0) {
                strcat(wkbuf, "Z");
                ptr += 2;
            } else if (strncmp(ptr1, "［", 3) == 0) {
                strcat(wkbuf, "[");
                ptr += 2;
            } else if (strncmp(ptr1, "￥", 3) == 0) {
                strcat(wkbuf, "\\");
                ptr += 2;
            } else if (strncmp(ptr1, "］", 3) == 0) {
                strcat(wkbuf, "]");
                ptr += 2;
            } else if (strncmp(ptr1, "＾", 3) == 0) {
                strcat(wkbuf, "^");
                ptr += 2;
            } else if (strncmp(ptr1, "＿", 3) == 0) {
                strcat(wkbuf, "_");
                ptr += 2;
            } else if (strncmp(ptr1, "‘", 3) == 0) {
                strcat(wkbuf, "`");
                ptr += 2;
            } else if (strncmp(ptr1, "ａ", 3) == 0) {
                strcat(wkbuf, "a");
                ptr += 2;
            } else if (strncmp(ptr1, "ｂ", 3) == 0) {
                strcat(wkbuf, "b");
                ptr += 2;
            } else if (strncmp(ptr1, "ｃ", 3) == 0) {
                strcat(wkbuf, "c");
                ptr += 2;
            } else if (strncmp(ptr1, "ｄ", 3) == 0) {
                strcat(wkbuf, "d");
                ptr += 2;
            } else if (strncmp(ptr1, "ｅ", 3) == 0) {
                strcat(wkbuf, "e");
                ptr += 2;
            } else if (strncmp(ptr1, "ｆ", 3) == 0) {
                strcat(wkbuf, "f");
                ptr += 2;
            } else if (strncmp(ptr1, "ｇ", 3) == 0) {
                strcat(wkbuf, "g");
                ptr += 2;
            } else if (strncmp(ptr1, "ｈ", 3) == 0) {
                strcat(wkbuf, "h");
                ptr += 2;
            } else if (strncmp(ptr1, "ｉ", 3) == 0) {
                strcat(wkbuf, "i");
                ptr += 2;
            } else if (strncmp(ptr1, "ｊ", 3) == 0) {
                strcat(wkbuf, "j");
                ptr += 2;
            } else if (strncmp(ptr1, "ｋ", 3) == 0) {
                strcat(wkbuf, "k");
                ptr += 2;
            } else if (strncmp(ptr1, "ｌ", 3) == 0) {
                strcat(wkbuf, "l");
                ptr += 2;
            } else if (strncmp(ptr1, "ｍ", 3) == 0) {
                strcat(wkbuf, "m");
                ptr += 2;
            } else if (strncmp(ptr1, "ｎ", 3) == 0) {
                strcat(wkbuf, "n");
                ptr += 2;
            } else if (strncmp(ptr1, "ｏ", 3) == 0) {
                strcat(wkbuf, "o");
                ptr += 2;
            } else if (strncmp(ptr1, "ｐ", 3) == 0) {
                strcat(wkbuf, "p");
                ptr += 2;
            } else if (strncmp(ptr1, "ｑ", 3) == 0) {
                strcat(wkbuf, "q");
                ptr += 2;
            } else if (strncmp(ptr1, "ｒ", 3) == 0) {
                strcat(wkbuf, "r");
                ptr += 2;
            } else if (strncmp(ptr1, "ｓ", 3) == 0) {
                strcat(wkbuf, "s");
                ptr += 2;
            } else if (strncmp(ptr1, "ｔ", 3) == 0) {
                strcat(wkbuf, "t");
                ptr += 2;
            } else if (strncmp(ptr1, "ｕ", 3) == 0) {
                strcat(wkbuf, "u");
                ptr += 2;
            } else if (strncmp(ptr1, "ｖ", 3) == 0) {
                strcat(wkbuf, "v");
                ptr += 2;
            } else if (strncmp(ptr1, "ｗ", 3) == 0) {
                strcat(wkbuf, "w");
                ptr += 2;
            } else if (strncmp(ptr1, "ｘ", 3) == 0) {
                strcat(wkbuf, "x");
                ptr += 2;
            } else if (strncmp(ptr1, "ｙ", 3) == 0) {
                strcat(wkbuf, "y");
                ptr += 2;
            } else if (strncmp(ptr1, "ｚ", 3) == 0) {
                strcat(wkbuf, "z");
                ptr += 2;
            } else if (strncmp(ptr1, "｛", 3) == 0) {
                strcat(wkbuf, "{");
                ptr += 2;
            } else if (strncmp(ptr1, "｜", 3) == 0) {
                strcat(wkbuf, "|");
                ptr += 2;
            } else if (strncmp(ptr1, "｝", 3) == 0) {
                strcat(wkbuf, "}");
                ptr += 2;
            } else if (strncmp(ptr1, "ヴ", 3) == 0) {
                strcat(wkbuf, "ｳﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ア", 3) == 0) {
                strcat(wkbuf, "ｱ");
                ptr += 2;
            } else if (strncmp(ptr1, "ァ", 3) == 0) {
                strcat(wkbuf, "ｧ");
                ptr += 2;
            } else if (strncmp(ptr1, "イ", 3) == 0) {
                strcat(wkbuf, "ｲ");
                ptr += 2;
            } else if (strncmp(ptr1, "ィ", 3) == 0) {
                strcat(wkbuf, "ｨ");
                ptr += 2;
            } else if (strncmp(ptr1, "ウ", 3) == 0) {
                strcat(wkbuf, "ｳ");
                ptr += 2;
            } else if (strncmp(ptr1, "ゥ", 3) == 0) {
                strcat(wkbuf, "ｩ");
                ptr += 2;
            } else if (strncmp(ptr1, "エ", 3) == 0) {
                strcat(wkbuf, "ｴ");
                ptr += 2;
            } else if (strncmp(ptr1, "ェ", 3) == 0) {
                strcat(wkbuf, "ｪ");
                ptr += 2;
            } else if (strncmp(ptr1, "オ", 3) == 0) {
                strcat(wkbuf, "ｵ");
                ptr += 2;
            } else if (strncmp(ptr1, "ォ", 3) == 0) {
                strcat(wkbuf, "ｫ");
                ptr += 2;
            } else if (strncmp(ptr1, "ガ", 3) == 0) {
                strcat(wkbuf, "ｶﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ギ", 3) == 0) {
                strcat(wkbuf, "ｷﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "グ", 3) == 0) {
                strcat(wkbuf, "ｸﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ゲ", 3) == 0) {
                strcat(wkbuf, "ｹﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ゴ", 3) == 0) {
                strcat(wkbuf, "ｺﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "カ", 3) == 0) {
                strcat(wkbuf, "ｶ");
                ptr += 2;
            } else if (strncmp(ptr1, "キ", 3) == 0) {
                strcat(wkbuf, "ｷ");
                ptr += 2;
            } else if (strncmp(ptr1, "ク", 3) == 0) {
                strcat(wkbuf, "ｸ");
                ptr += 2;
            } else if (strncmp(ptr1, "ケ", 3) == 0) {
                strcat(wkbuf, "ｹ");
                ptr += 2;
            } else if (strncmp(ptr1, "コ", 3) == 0) {
                strcat(wkbuf, "ｺ");
                ptr += 2;
            } else if (strncmp(ptr1, "ザ", 3) == 0) {
                strcat(wkbuf, "ｻﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ジ", 3) == 0) {
                strcat(wkbuf, "ｼﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ズ", 3) == 0) {
                strcat(wkbuf, "ｽﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ゼ", 3) == 0) {
                strcat(wkbuf, "ｾﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ゾ", 3) == 0) {
                strcat(wkbuf, "ｿﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "サ", 3) == 0) {
                strcat(wkbuf, "ｻ");
                ptr += 2;
            } else if (strncmp(ptr1, "シ", 3) == 0) {
                strcat(wkbuf, "ｼ");
                ptr += 2;
            } else if (strncmp(ptr1, "ス", 3) == 0) {
                strcat(wkbuf, "ｽ");
                ptr += 2;
            } else if (strncmp(ptr1, "セ", 3) == 0) {
                strcat(wkbuf, "ｾ");
                ptr += 2;
            } else if (strncmp(ptr1, "ソ", 3) == 0) {
                strcat(wkbuf, "ｿ");
                ptr += 2;
            } else if (strncmp(ptr1, "ダ", 3) == 0) {
                strcat(wkbuf, "ﾀﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヂ", 3) == 0) {
                strcat(wkbuf, "ﾁﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヅ", 3) == 0) {
                strcat(wkbuf, "ﾂﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "デ", 3) == 0) {
                strcat(wkbuf, "ﾃﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ド", 3) == 0) {
                strcat(wkbuf, "ﾄﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "タ", 3) == 0) {
                strcat(wkbuf, "ﾀ");
                ptr += 2;
            } else if (strncmp(ptr1, "チ", 3) == 0) {
                strcat(wkbuf, "ﾁ");
                ptr += 2;
            } else if (strncmp(ptr1, "ツ", 3) == 0) {
                strcat(wkbuf, "ﾂ");
                ptr += 2;
            } else if (strncmp(ptr1, "ッ", 3) == 0) {
                strcat(wkbuf, "ｯ");
                ptr += 2;
            } else if (strncmp(ptr1, "テ", 3) == 0) {
                strcat(wkbuf, "ﾃ");
                ptr += 2;
            } else if (strncmp(ptr1, "ト", 3) == 0) {
                strcat(wkbuf, "ﾄ");
                ptr += 2;
            } else if (strncmp(ptr1, "ナ", 3) == 0) {
                strcat(wkbuf, "ﾅ");
                ptr += 2;
            } else if (strncmp(ptr1, "ニ", 3) == 0) {
                strcat(wkbuf, "ﾆ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヌ", 3) == 0) {
                strcat(wkbuf, "ﾇ");
                ptr += 2;
            } else if (strncmp(ptr1, "ネ", 3) == 0) {
                strcat(wkbuf, "ﾈ");
                ptr += 2;
            } else if (strncmp(ptr1, "ノ", 3) == 0) {
                strcat(wkbuf, "ﾉ");
                ptr += 2;
            } else if (strncmp(ptr1, "バ", 3) == 0) {
                strcat(wkbuf, "ﾊﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ビ", 3) == 0) {
                strcat(wkbuf, "ﾋﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ブ", 3) == 0) {
                strcat(wkbuf, "ﾌﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ベ", 3) == 0) {
                strcat(wkbuf, "ﾍﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "ボ", 3) == 0) {
                strcat(wkbuf, "ﾎﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "パ", 3) == 0) {
                strcat(wkbuf, "ﾊﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "ピ", 3) == 0) {
                strcat(wkbuf, "ﾋﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "プ", 3) == 0) {
                strcat(wkbuf, "ﾌﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "ペ", 3) == 0) {
                strcat(wkbuf, "ﾍﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "ポ", 3) == 0) {
                strcat(wkbuf, "ﾎﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "ハ", 3) == 0) {
                strcat(wkbuf, "ﾊ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヒ", 3) == 0) {
                strcat(wkbuf, "ﾋ");
                ptr += 2;
            } else if (strncmp(ptr1, "フ", 3) == 0) {
                strcat(wkbuf, "ﾌ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヘ", 3) == 0) {
                strcat(wkbuf, "ﾍ");
                ptr += 2;
            } else if (strncmp(ptr1, "ホ", 3) == 0) {
                strcat(wkbuf, "ﾎ");
                ptr += 2;
            } else if (strncmp(ptr1, "マ", 3) == 0) {
                strcat(wkbuf, "ﾏ");
                ptr += 2;
            } else if (strncmp(ptr1, "ミ", 3) == 0) {
                strcat(wkbuf, "ﾐ");
                ptr += 2;
            } else if (strncmp(ptr1, "ム", 3) == 0) {
                strcat(wkbuf, "ﾑ");
                ptr += 2;
            } else if (strncmp(ptr1, "メ", 3) == 0) {
                strcat(wkbuf, "ﾒ");
                ptr += 2;
            } else if (strncmp(ptr1, "モ", 3) == 0) {
                strcat(wkbuf, "ﾓ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヤ", 3) == 0) {
                strcat(wkbuf, "ﾔ");
                ptr += 2;
            } else if (strncmp(ptr1, "ャ", 3) == 0) {
                strcat(wkbuf, "ｬ");
                ptr += 2;
            } else if (strncmp(ptr1, "ユ", 3) == 0) {
                strcat(wkbuf, "ﾕ");
                ptr += 2;
            } else if (strncmp(ptr1, "ュ", 3) == 0) {
                strcat(wkbuf, "ｭ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヨ", 3) == 0) {
                strcat(wkbuf, "ﾖ");
                ptr += 2;
            } else if (strncmp(ptr1, "ョ", 3) == 0) {
                strcat(wkbuf, "ｮ");
                ptr += 2;
            } else if (strncmp(ptr1, "ラ", 3) == 0) {
                strcat(wkbuf, "ﾗ");
                ptr += 2;
            } else if (strncmp(ptr1, "リ", 3) == 0) {
                strcat(wkbuf, "ﾘ");
                ptr += 2;
            } else if (strncmp(ptr1, "ル", 3) == 0) {
                strcat(wkbuf, "ﾙ");
                ptr += 2;
            } else if (strncmp(ptr1, "レ", 3) == 0) {
                strcat(wkbuf, "ﾚ");
                ptr += 2;
            } else if (strncmp(ptr1, "ロ", 3) == 0) {
                strcat(wkbuf, "ﾛ");
                ptr += 2;
            } else if (strncmp(ptr1, "ワ", 3) == 0) {
                strcat(wkbuf, "ﾜ");
                ptr += 2;
            } else if (strncmp(ptr1, "ヲ", 3) == 0) {
                strcat(wkbuf, "ｦ");
                ptr += 2;
            } else if (strncmp(ptr1, "ン", 3) == 0) {
                strcat(wkbuf, "ﾝ");
                ptr += 2;
            } else if (strncmp(ptr1, "ー", 3) == 0) {
                strcat(wkbuf, "ｰ");
                ptr += 2;
            } else if (strncmp(ptr1, "゛", 3) == 0) {
                strcat(wkbuf, "ﾞ");
                ptr += 2;
            } else if (strncmp(ptr1, "゜", 3) == 0) {
                strcat(wkbuf, "ﾟ");
                ptr += 2;
            } else if (strncmp(ptr1, "、", 3) == 0) {
                strcat(wkbuf, "､");
                ptr += 2;
            } else if (strncmp(ptr1, "。", 3) == 0) {
                strcat(wkbuf, "｡");
                ptr += 2;
            } else if (strncmp(ptr1, "・", 3) == 0) {
                strcat(wkbuf, "･");
                ptr += 2;
            } else if (strncmp(ptr1, "」", 3) == 0) {
                strcat(wkbuf, "｣");
                ptr += 2;
            } else if (strncmp(ptr1, "「", 3) == 0) {
                strcat(wkbuf, "｢");
                ptr += 2;
            } else if (strncmp(ptr1, "□", 3) == 0) {
                strcat(wkbuf, " ");
                ptr += 2;
            } else {
                strncat(wkbuf, ptr1, 1);
            }
        }

        memcpy(halfstr, wkbuf.arr, strlen(wkbuf));

        if (dbg_convfull2half == 1) {
            C_DbgMsg("C_ConvFull2Half : %s\n", "end");
        }

        /* 正常終了 */
        return C_const_OK;
    }


/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_ConvSJ2UT                                                 */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_ConvSJ2UT(char *sjisstr, int sjislen,                          */
    /*                       char *utf8str, int *utf8len)                         */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ＳＪＩＳ->ＵＴＦ８変換処理                                    */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    sjisstr  ： メッセージ                        */
    /*              int             sjislen  ： メッセージ長                      */
    /*              char       *    utf8str  ： メッセージ                        */
    /*              int        *    utf8len  ： 結果を格納                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*              2       ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_ConvSJ2UT(StringDto sjisstr, int sjislen, StringDto utf8str, IntegerDto utf8len) {

        if (dbg_convsj2ut == 1) {
            C_DbgMsg("C_ConvSJ2UT : %s\n", "start");
        }
        /* 引数のチェックを行う */
        if (utf8str == null || utf8len == null || sjisstr == null) {
            if (dbg_convsj2ut == 1) {
                C_DbgMsg("C_ConvSJ2UT : %s\n", "prmerr");
            }
            return C_const_PARAMERR;
        }

        if (dbg_convsj2ut == 1) {
            C_DbgMsg("C_ConvSJ2UT : sjislen=%d\n", sjislen);
        }
        try {
            byte[] utf = sjisstr.arr.getBytes("CP932");
            utf8str.arr = new String(utf, "CP932");
            utf8len.arr = utf.length;
            /* 文字セット変換のためのディスクリプタを解放する */
            if (dbg_convsj2ut == 1) {
                C_DbgMsg("C_ConvSJ2UT : iconv_close=%d\n", 0);
            }
            if (dbg_convsj2ut == 1) {
                C_DbgMsg("C_ConvSJ2UT : utf8len=%d\n", utf8len);
                C_DbgMsg("C_ConvSJ2UT : %s\n", "end");
            }

        } catch (UnsupportedEncodingException e) {
            if (dbg_convsj2ut == 1) {
                C_DbgMsg("C_ConvSJ2UT : iconv : errno=%d\n", -1);
            }
            return C_const_NG;
        }
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_ConvUT2SJ                                                 */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_ConvUT2SJ(char *utf8str, int utf8len,                          */
    /*                       char *sjisstr, int *sjislen)                         */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ＵＴＦ８->ＳＪＩＳ変換処理                                    */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    utf8str  ： メッセージ                        */
    /*              int             utf8len  ： メッセージ長                      */
    /*              char       *    sjisstr  ： メッセージ                        */
    /*              int        *    sjislen  ： 結果を格納                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*              2       ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_ConvUT2SJ(StringDto utf8str, int utf8len, StringDto sjisstr, IntegerDto sjislen) {
        try {
            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : %s\n", "start");
            }

            /* 引数のチェックを行う */
            if (utf8str == null || sjisstr == null || sjislen == null) {
                if (dbg_convut2sj == 1) {
                    C_DbgMsg("C_ConvUT2SJ : %s\n", "prmerr");
                }
                return C_const_PARAMERR;
            }
            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : utf8len=%d\n", utf8len);
            }


            sjisstr.arr = IconvUtil.change(SystemConstant.UTF8, SystemConstant.MS932, utf8str.arr);
            sjislen.arr = sizeof(sjisstr.arr, SystemConstant.MS932);

//            byte[] utf = utf8str.arr.getBytes("CP932");
//            sjisstr.arr = new String(utf, "CP932");
//            sjislen.arr = utf.length;
            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : sjislen=%d\n", sjislen);
            }

            /* 文字セット変換のためのディスクリプタを解放する */
            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : iconv_close=%d\n", 0);
            }

            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : %s\n", "end");
            }
        } catch (Exception e) {
            if (dbg_convut2sj == 1) {
                C_DbgMsg("C_ConvUT2SJ : iconv_open : errno=%d\n", -1);
            }
            return C_const_NG;
        }
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_StrcntUtf8                                                */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_StrcntUtf8(char *utf8str, int utf8len, int *cnt)               */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ＵＴＦ８文字列桁数カウント処理                                */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    utf8str  ： メッセージ                        */
    /*              int             utf8len  ： メッセージ長                      */
    /*              int        *    cnt      ： 結果を格納                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*              2       ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_StrcntUtf8(StringDto utf8str, int utf8len, IntegerDto cnt) {
        int ret;
        StringDto bufu = new StringDto();
        StringDto bufs = new StringDto();
        IntegerDto cnvlen = new IntegerDto();

        if (dbg_strcntutf8 == 1) {
            C_DbgMsg("C_StrcntUtf8 : %s\n", "start");
        }
        /* 引数のチェックを行う */
        if (utf8str == null || cnt == null) {
            if (dbg_strcntutf8 == 1) {
                C_DbgMsg("C_StrcntUtf8 : %s\n", "prmerr");
            }
            return C_const_PARAMERR;
        }

        /* UTF8->SJIS変換処理を呼び出し、sjisの文字サイズを取得する */
//        memset(bufu, 0x00, sizeof(bufu));
        memcpy(bufu, utf8str, utf8len);
//        memset(bufs, 0x00, sizeof(bufs));

        ret = C_ConvUT2SJ(bufu, strlen(bufu), bufs, cnvlen);
        if (ret != C_const_OK) {
            if (dbg_strcntutf8 == 1) {
                C_DbgMsg("C_StrcntUtf8 : %s\n", "C_ConvUT2SJ NG");
            }
            return C_const_NG;
        }

        cnt.arr = cnvlen.arr;
        if (dbg_strcntutf8 == 1) {
            C_DbgMsg("C_StrcntUtf8 : %s\n", "end");
        }
        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_SetFullSpace                                              */
    /*                                                                            */
    /*  書式                                                                  */
    /*  int  C_SetFullSpace(int cnt, int cdkbn, char *outbuf, int *outlen)    */
    /*                                                                            */
    /*  【説明】                                                              */
    /*              全角スペースセット処理                                        */
    /*              引数に指定した文字コードにて、全角スペースを文字数分設定する  */
    /*              全角スペースを編集するエリアには                              */
    /*              指定の文字数分の全角スペースが、指定の文字コードにて          */
    /*              格納できるだけのサイズを確保しておく必要がある                */
    /*              （UTF8、SJIS共に最大レングスは4096バイトとする）              */
    /*                                                                            */
    /*  【引数】                                                              */
    /*      int         cnt       ： 編集する全角スペースの文字数     */
    /*      int         cdkbn     ： 文字コード区分                   */
    /*      char       *    outbuf    ： 全角スペースの格納先アドレス     */
    /*      int        *    outlen    ： 全角スペースのレングス           */
    /*                                                                            */
    /*  【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*              2   ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_SetFullSpace(int cnt, int cdkbn, StringDto outbuf, IntegerDto outlen) {
        StringDto wkbuf = new StringDto();   /** 全角スペースデータ                   **/
        int wkdata;        /** ループ用                             **/
        int rtn_cd;        /** 関数戻り値                           **/
        StringDto wkoutbuf = new StringDto();    /** SJIS変換後全角スペースデータ         **/
        IntegerDto wkoutlen = new IntegerDto();      /** SJIS変換後全角スペースデータレングス **/

        if (dbg_setfullspace == 1) {
            C_DbgMsg("C_SetFullSpace : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (outbuf == null || outlen == null) {
            if (dbg_setfullspace == 1) {
                C_DbgMsg("C_SetFullSpace : %s\n", "prmerr");
            }
            return C_const_PARAMERR;
        }

        if (cnt <= 0) {
            if (dbg_setfullspace == 1) {
                C_DbgMsg("C_SetFullSpace : %s\n", "prmerr cnt");
            }
            return C_const_PARAMERR;
        }

        /* 初期化 */
        memset(wkbuf, 0x00, sizeof(wkbuf));
        memset(wkoutbuf, 0x00, sizeof(wkoutbuf));
        wkoutlen.arr = 0;

        /* 引数で指定された文字数分の全角スペースデータ作成 */
        for (wkdata = 1; wkdata <= cnt; wkdata++) {
            strcat(wkbuf, "　");
        }

        /* UTF8→SJIS変換処理（文字コード区分が「１」のときのみ） */
        if (cdkbn == 1) {
            rtn_cd = C_ConvUT2SJ(wkbuf, strlen(wkbuf), wkoutbuf, wkoutlen);
            if (rtn_cd != C_const_OK) {
                if (dbg_setfullspace == 1) {
                    C_DbgMsg("C_SetFullSpace : %s\n", "ut2sjerr");
                    C_DbgMsg("C_SetFullSpace : rtn_cd=[%d]\n", rtn_cd);
                }
                return C_const_NG;
            }
        }

        /* 出力用引数の編集 */
        if (cdkbn == 1) {
            strcpy(outbuf, wkoutbuf);
            outlen.arr = wkoutlen.arr;
        } else {
            strcpy(outbuf, wkbuf);
            outlen.arr = strlen(outbuf);
        }

        if (dbg_setfullspace == 1) {
            C_DbgMsg("C_SetFullSpace : %s\n", "end");
        }

        /* 正常終了 */
        return C_const_OK;
    }

    @Override
    public int C_GetPidCDV(String pid) {
        String wk_PidCD = null;                    /* 会員コード                   */
        int wk_in_PidCD_len = 14;            /* 会員コード桁数               */
        int wk_Modulus;                      /* モジュラス                   */
        String wk_Weight = null;                   /* ウエイト                     */
        String wk_W1 = null;
        String wk_P1 = null;
        int wk_Ans;
        int wk_Sum;
        int wk_Amari;
        int wk_CD;
        int i;
        boolean dbg_GetPidCDV = true;
        boolean dbg_GetTanCDV = true;
        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (StringUtils.isEmpty(pid)) {
            if (dbg_GetPidCDV) {
                C_DbgMsg("C_GetPidCDV : %s\n", "NO PRMERR");
            }
            return C_const_PARAMERR;
        }
        memset(wk_PidCD, 0x00, sizeof(wk_PidCD));
        wk_PidCD = memcpy(wk_PidCD, pid, wk_in_PidCD_len);
        if (strlen(wk_PidCD) != wk_in_PidCD_len) {
            if (dbg_GetPidCDV) {
                C_DbgMsg("C_GetPidCDV : %s\n", "PRM LENG ERR");
            }
            return C_const_PARAMERR;
        }
        wk_Sum = 0;
        wk_Amari = 0;
        wk_CD = 0;
        /************************/
        /* 主処理               */
        /************************/
        /* モジュラスとウエイトを設定 */
        wk_Modulus = 10;
        wk_Weight = "13131313131313";
//        wk_Weight = sprintf(wk_Weight, "13131313131313");
        for (i = 0; i < wk_in_PidCD_len; i++) {
            memset(wk_W1, 0x00, sizeof(wk_W1));
            memset(wk_P1, 0x00, sizeof(wk_P1));
//            memcpy(wk_W1, wk_Weight.charAt(i), 1);
//            memcpy(wk_P1, wk_PidCD.charAt(i), 1);
            wk_W1 = String.valueOf(wk_Weight.charAt(i));
            wk_P1 = String.valueOf(wk_PidCD.charAt(i));
            if (strcmp(wk_P1, "0") < 0
                    || strcmp(wk_P1, "9") > 0) {
                if (dbg_GetTanCDV) {
                    C_DbgMsg("C_GetPidCDV : %s\n", "PRM NUMERIC ERR");
                }
                return C_const_PARAMERR;
            }
            wk_Ans = (atoi(wk_W1) * atoi(wk_P1)) % 10;
            wk_Sum += wk_Ans;
        }
        wk_CD = (wk_Modulus - (wk_Sum % wk_Modulus)) % 10;
        if (dbg_GetPidCDV) {
            C_DbgMsg("C_GetPidCDV : CD=[%d]\n", wk_CD);
        }

        /* 15ケタ目にチェックディジットを設定する */
//        *(pid + wk_in_PidCD_len) = wk_CD + '0';
        //TODO
        if (dbg_GetPidCDV) {
            C_DbgMsg("C_GetPidCDV : return PidCD[%s]\n", pid);
        }

        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_ConvTelNo                                                 */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_ConvTelNo(char * telno_bef, int telno_len, char * telno_aft)    */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              電話番号ハイフンなし変換処理                                  */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char *    telno_bef ： ハイフン付きの電話番号                 */
    /*              int       telno_len ： ハイフン付きの電話番号のエリアサイズ   */
    /*              char *    telno_aft ： ハイフンなしの電話番号                 */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*              2   ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_ConvTelNo(StringDto telno_bef, int telno_len, StringDto telno_aft) {
        int i;
//        int ii;
        String wk_buf;

        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (telno_bef == null || telno_aft == null || telno_len == 0) {
            if (dbg_ConvTelNo == 1) {
                C_DbgMsg("C_ConvTelNo : %s\n", "NO PRMERR");
            }
            return C_const_PARAMERR;
        }
        /************************/
        /* 主処理               */
        /************************/
        wk_buf = telno_bef.arr;
//        ii = 0;
        for (i = 0; i < telno_len; i++) {
            String wk_buf_c = wk_buf.substring(i, i + 1);
            if (strncmp(wk_buf_c, "-", 1) == 0) {
//                wk_buf++;
                if (dbg_ConvTelNo == 1) {
                    C_DbgMsg("C_ConvTelNo : %s\n", "ハイフン");
                }
            } else {
                telno_aft.arr += wk_buf_c;
//            memcpy(telno_aft + ii,wk_buf,1);
//                wk_buf++;
//                ii++;
                if (dbg_ConvTelNo == 1) {
                    C_DbgMsg("C_ConvTelNo : %s\n", "ハイフン以外");
                }
            }
        }

        if (dbg_ConvTelNo == 1) {
            C_DbgMsg("C_ConvTelNo : return %s\n", "OK");
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_CountAge                                                  */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_CountAge( int birth_yyyy, int birth_mm, int birth_dd,           */
    /*                      int base_yyyy,  int base_mm,  int base_dd, int *age ) */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              年齢計算処理                                                  */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int   birth_yyyy： 誕生年(yyyy)を格納                         */
    /*              int   birth_mm  ： 誕生月(mm)を格納                           */
    /*              int   birth_dd  ： 誕生日(dd)を格納                           */
    /*              int   base_yyyy ： 年齢計算を行う基準となる日の年(yyyy)を格納 */
    /*              int   base_mm   ： 年齢計算を行う基準となる日の月(mm)を格納   */
    /*              int   base_dd   ： 年齢計算を行う基準となる日(dd)を格納       */
    /*              int * age       ： 算出した年齢                               */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*              2   ： 引数エラー                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_CountAge(int birth_yyyy, int birth_mm, int birth_dd, int base_yyyy, int base_mm, int base_dd, IntegerDto age) {
        int wk_birth_yyyymmdd;               /* 誕生日                       */
        int wk_base_yyyymmdd;                /* 基準日                       */
        int out_age;                         /* 計算結果年齢                 */

        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (birth_yyyy == 0 || birth_mm == 0 || birth_dd == 0 ||
                base_yyyy == 0 || base_mm == 0 || base_dd == 0) {
            if (dbg_CountAge == 1) {
                C_DbgMsg("C_CountAge : %s\n", "NO PRMERR");
            }
            out_age = 0;
            age.arr = out_age;
            return C_const_OK;
        }

        wk_birth_yyyymmdd = (birth_yyyy * 10000) +
                (birth_mm * 100) +
                birth_dd;
        wk_base_yyyymmdd = (base_yyyy * 10000) +
                (base_mm * 100) +
                base_dd;

        if (wk_birth_yyyymmdd > wk_base_yyyymmdd) {
            if (dbg_CountAge == 1) {
                C_DbgMsg("C_CountAge : %s\n", "PRM Big Small ERR");
            }
            out_age = 0;
            age.arr = out_age;
            return C_const_OK;
        }

        /************************/
        /* 主処理               */
        /************************/
        if ((birth_mm < base_mm) ||
                (birth_mm == base_mm && birth_dd <= base_dd)) {
            if (dbg_CountAge == 1) {
                C_DbgMsg("C_CountAge : %s\n", "年齢　＝　ベース年　－　誕生年");
            }
            out_age = base_yyyy - birth_yyyy;
        } else {
            if (dbg_CountAge == 1) {
                C_DbgMsg("C_CountAge : %s\n", "年齢　＝　ベース年　－　誕生年　－　１");
            }
            out_age = base_yyyy - birth_yyyy - 1;
        }

        if (out_age >= 150) {
            out_age = 0;
        }


        if (dbg_CountAge == 1) {
            C_DbgMsg("C_CountAge : return OK[%d]\n", out_age);
        }
        age.arr = out_age;
        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetPostBarCode                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /* 2022/09/14 MCCM初版 MOD START */
    /*      //int C_GetPostBarCode(char *address1, char *address2, char *address3,  */
    /*      //                     char *postcd,   char *custbarcd,  int status)    */
    /*      int C_GetPostBarCode(char *address,                                   */
    /*                           char *postcd,   char *custbarcd,  int status)    */
    /* 2022/09/14 MCCM初版 MOD END */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              郵便番号コード変換処理                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /* 2022/09/14 MCCM初版 MOD START */
    /*              char       *    address   : 住所が格納されているエリアの      */
    /*                                          アドレス                          */
    /*              //char     *    address1  : 住所１                            */
    /*              //char     *    address2  : 住所２                            */
    /*              //char     *    address3  : 住所３                            */
    /*              char       *    postcd    : 郵便番号                          */
    /*              char       *    custbarcd : カスタマーバーコード              */
    /*              int        *    status    : 結果ステータス                    */
    /* 2022/09/14 MCCM初版 MOD END */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetPostBarCode(StringDto address, StringDto postcd, StringDto custbarcd, IntegerDto status) {

        int rtn_cd;


        String penv;
        StringDto sid_md = new StringDto(16);
        StringDto dblink_md = new StringDto(16);
        StringDto sid_md_lnk = new StringDto(20);

        StringDto wk_addr_del = new StringDto(256);/* 該当住所削除文字列 */
        StringDto wk_addr_disp = new StringDto(256);/* 住所表示番号 */
        StringDto wk_addr_disp2 = new StringDto(256);/* 住所表示番号(変換後) */
        StringDto wk_chkdgt = new StringDto(2);/* チェックディジット */
        StringDto wk_barcd = new StringDto(30); /* バーコード(編集用) */

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("C_GetPostBarCode : %s \n", "start");
        }

        /* 引数のチェックを行う */
        /* 2022/09/14 MCCM初版 MOD START */
/*        if (address1 == null || address2 == null || address3 == null
           || custbarcd == null || postcd == null || status == null) {*/
        if (address == null || custbarcd == null || postcd == null || status == null) {
            /* 2022/09/14 MCCM初版 MOD END */
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("C_GetPostBarCode : %s \n", "PRMERR");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("C_GetPostBarCode : %s \n", "DBERR");
            }
            /* ＤＢアクセスエラー */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 環境変数の取得 */

        penv = getenv("CM_ORA_DBLINK_MD");
        if (StringUtils.isEmpty(penv)) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("C_GetPostBarCode : %s \n", "ENVERR(DBLINK_MD)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }
        strcpy(dblink_md, penv);

        /* 環境変数の取得 */

        penv = getenv("CM_ORA_SID_MD");
        if (StringUtils.isEmpty(penv)) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("C_GetPostBarCode : %s \n", "ENVERR(SID_MD)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }
        if (strcmp(Cg_ORASID, penv) != 0) {
            strcpy(sid_md, penv);
            strcpy(sid_md_lnk, "@");
            strcat(sid_md_lnk, dblink_md);
        } else {
            memset(sid_md, 0x00, sizeof(sid_md));
            memset(sid_md_lnk, 0x00, sizeof(sid_md_lnk));
        }

        /* 住所をチェックする */
        memset(wk_addr_del, 0x00, sizeof(wk_addr_del));
        memset(wk_addr_disp, 0x00, sizeof(wk_addr_disp));
        memset(wk_addr_disp2, 0x00, sizeof(wk_addr_disp2));
        /* 2022/09/14 MCCM初版 MOD START */
        /*        rtn_cd = checkAddressCodeMaster(address1, address2, address3, postcd, sid_md_lnk, wk_addr_del);*/
        rtn_cd = checkAddressCodeMaster(address, postcd, sid_md_lnk, wk_addr_del);
        /* 2022/09/14 MCCM初版 MOD END */
        if (rtn_cd != C_const_OK) {
            /* NOTFOUND or DBERR */
            status.arr = rtn_cd;
            if (rtn_cd == C_const_Stat_NOTFND) {
                /* 該当なしの場合、オール０を返す */
                memset(custbarcd, '0', 23);
//                custbarcd[23] = 0x00;
                return C_const_OK;
            }
            return C_const_NG;
        }

        /* 住所表示番号を抽出する */
        rtn_cd = editAddressNumber(wk_addr_del.arr, wk_addr_disp);

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("C_GetPostBarCode : wk_addr_disp=[%s]\n", wk_addr_disp);
        }

        /* 住所表示番号に制御コードを付与する */
        rtn_cd = convAddressNumber(wk_addr_disp.arr, wk_addr_disp2);

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("C_GetPostBarCode : wk_addr_disp2=[%s]\n", wk_addr_disp2);
        }

        /* 住所表示番号のＣＤを付与する */
        rtn_cd = calcPostBarCodeCheckDigit(postcd.arr, wk_addr_disp2.arr, wk_chkdgt);

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("C_GetPostBarCode : wk_chkdgt=[%s]\n", wk_chkdgt);
        }

        /* カスタマーバーコードを出力引数に編集する */
        memset(wk_barcd, 0x00, sizeof(wk_barcd));
        strcat(wk_barcd, "<");          /* スタートコード */
        strcat(wk_barcd, postcd);     /* 郵便番号 */
        strcat(wk_barcd, wk_addr_disp2); /* 住所表示番号 */
        strcat(wk_barcd, wk_chkdgt);    /* チェックディジット */
        strcat(wk_barcd, ">");          /* ストップコード */
        strcpy(custbarcd, wk_barcd);

        /* 正常終了 */
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： convAddressNumber                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      static int convAddressNumber(char *instr, char *outstr)               */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              住所表示番号制御コード編集処理                                */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    instr  ： (I)編集前                           */
    /*              char       *    outstr ： (O)編集後                           */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    int convAddressNumber(String instr, StringDto outstr) {
        String numstr = "0123456789";

        int i;
        int c;
        int siz;
        StringDto buf = new StringDto(64);
        String p;

        siz = strlen(instr);
        memset(buf, 0x00, sizeof(buf));
        p = buf.arr;
        for (i = 0; i < siz; i++) {
            c = instr.charAt(i);
            if ('A' <= c && c <= 'J') {
                /* +? */
                p += '+';
                p += numstr.charAt(c - 'A');
            } else if ('K' <= c && c <= 'T') {
                /* &? */
                p += '&';
                p += numstr.charAt(c - 'K');
            } else if ('U' <= c && c <= 'Z') {
                /* \? */
                p += '\\';
                p += numstr.charAt(c - 'U');
            } else {
                p += c;
            }
        }

        siz = strlen(buf);
        if (siz > 13) siz = 13;
        memset(outstr, '*', 13); /* CC4 */
        memcpy(outstr, buf, siz);
//        outstr[13] = '\0';

        /* １３桁目の補正（制御コードだった場合） */
        if (outstr.arr.charAt(12) == '+' || outstr.arr.charAt(12) == '&' || outstr.arr.charAt(12) == '\\') {
            outstr.arr = outstr.arr.substring(0, 12) + '*';
        }
        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： calcPostBarCodeCheckDigit                                   */
    /*                                                                            */
    /*      書式                                                                  */
    /*      static int calcPostBarCodeCheckDigit(char *zip, char *addr,           */
    /*                                           char *outstr)                    */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              チェックディジット計算処理                                    */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    zip    ： 郵便番号                            */
    /*              char       *    addr   ： 住所表示番号                        */
    /*              char       *    outstr ： チェックディジット                  */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*                                                                            */

    /******************************************************************************/
    int calcPostBarCodeCheckDigit(String zip, String addr, StringDto outstr) {
        String chr_tbl = "0123456789-+&\\*%#@="; /* CD計算用 */

        StringDto wk_buf = new StringDto(100);
        int i;
        int pos;
        int j;
        int wk_sum;
        int wk_mod;
        int wk_cd;


        strcpy(wk_buf, zip);
        strcat(wk_buf, addr);

        wk_sum = 0;
        for (i = 0; i < strlen(wk_buf); i++) {

            pos = -1;

            for (j = 0; j < strlen(chr_tbl); j++) {
                if (wk_buf.arr.charAt(i) == chr_tbl.charAt(j)) {
                    pos = j;
                    break;
                }
            }

            if (pos == -1) {
                /* ないはず */
            } else {
                wk_sum += pos;
            }
        }

        wk_mod = wk_sum % 19;
        if (wk_mod == 0) {
            wk_cd = 0;
        } else {
            wk_cd = 19 - wk_mod;
        }

        /* 結果を出力変数に設定する */
        outstr.arr = chr_tbl.substring(wk_cd, wk_cd + 1);
//        outstr[1] = '\0';

        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_ConvAlphaFull2Half                                        */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_ConvAlphaFull2Half(char *fullstr, char *halfstr)               */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              全角アルファベットを半角に変換する                            */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    fullstr ： 変換元                             */
    /*              char       *    halfstr ： 変換先                             */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              変換後の文字列の長さ                                          */
    /*                                                                            */

    /******************************************************************************/
    static int C_ConvAlphaFull2Half(String fullstr, StringDto halfstr) {
        String ptr;

        ptr = fullstr;

        if (strncmp(ptr, "Ａ", 3) == 0) {
            strcpy(halfstr, "A");
        } else if (strncmp(ptr, "Ｂ", 3) == 0) {
            strcpy(halfstr, "B");
        } else if (strncmp(ptr, "Ｃ", 3) == 0) {
            strcpy(halfstr, "C");
        } else if (strncmp(ptr, "Ｄ", 3) == 0) {
            strcpy(halfstr, "D");
        } else if (strncmp(ptr, "Ｅ", 3) == 0) {
            strcpy(halfstr, "E");
        } else if (strncmp(ptr, "Ｆ", 3) == 0) {
            strcpy(halfstr, "F");
        } else if (strncmp(ptr, "Ｇ", 3) == 0) {
            strcpy(halfstr, "G");
        } else if (strncmp(ptr, "Ｈ", 3) == 0) {
            strcpy(halfstr, "H");
        } else if (strncmp(ptr, "Ｉ", 3) == 0) {
            strcpy(halfstr, "I");
        } else if (strncmp(ptr, "Ｊ", 3) == 0) {
            strcpy(halfstr, "J");
        } else if (strncmp(ptr, "Ｋ", 3) == 0) {
            strcpy(halfstr, "K");
        } else if (strncmp(ptr, "Ｌ", 3) == 0) {
            strcpy(halfstr, "L");
        } else if (strncmp(ptr, "Ｍ", 3) == 0) {
            strcpy(halfstr, "M");
        } else if (strncmp(ptr, "Ｎ", 3) == 0) {
            strcpy(halfstr, "N");
        } else if (strncmp(ptr, "Ｏ", 3) == 0) {
            strcpy(halfstr, "O");
        } else if (strncmp(ptr, "Ｐ", 3) == 0) {
            strcpy(halfstr, "P");
        } else if (strncmp(ptr, "Ｑ", 3) == 0) {
            strcpy(halfstr, "Q");
        } else if (strncmp(ptr, "Ｒ", 3) == 0) {
            strcpy(halfstr, "R");
        } else if (strncmp(ptr, "Ｓ", 3) == 0) {
            strcpy(halfstr, "S");
        } else if (strncmp(ptr, "Ｔ", 3) == 0) {
            strcpy(halfstr, "T");
        } else if (strncmp(ptr, "Ｕ", 3) == 0) {
            strcpy(halfstr, "U");
        } else if (strncmp(ptr, "Ｖ", 3) == 0) {
            strcpy(halfstr, "V");
        } else if (strncmp(ptr, "Ｗ", 3) == 0) {
            strcpy(halfstr, "W");
        } else if (strncmp(ptr, "Ｘ", 3) == 0) {
            strcpy(halfstr, "X");
        } else if (strncmp(ptr, "Ｙ", 3) == 0) {
            strcpy(halfstr, "Y");
        } else if (strncmp(ptr, "Ｚ", 3) == 0) {
            strcpy(halfstr, "Z");
        } else if (strncmp(ptr, "ａ", 3) == 0) {
            strcpy(halfstr, "a");
        } else if (strncmp(ptr, "ｂ", 3) == 0) {
            strcpy(halfstr, "b");
        } else if (strncmp(ptr, "ｃ", 3) == 0) {
            strcpy(halfstr, "c");
        } else if (strncmp(ptr, "ｄ", 3) == 0) {
            strcpy(halfstr, "d");
        } else if (strncmp(ptr, "ｅ", 3) == 0) {
            strcpy(halfstr, "e");
        } else if (strncmp(ptr, "ｆ", 3) == 0) {
            strcpy(halfstr, "f");
        } else if (strncmp(ptr, "ｇ", 3) == 0) {
            strcpy(halfstr, "g");
        } else if (strncmp(ptr, "ｈ", 3) == 0) {
            strcpy(halfstr, "h");
        } else if (strncmp(ptr, "ｉ", 3) == 0) {
            strcpy(halfstr, "i");
        } else if (strncmp(ptr, "ｊ", 3) == 0) {
            strcpy(halfstr, "j");
        } else if (strncmp(ptr, "ｋ", 3) == 0) {
            strcpy(halfstr, "k");
        } else if (strncmp(ptr, "ｌ", 3) == 0) {
            strcpy(halfstr, "l");
        } else if (strncmp(ptr, "ｍ", 3) == 0) {
            strcpy(halfstr, "m");
        } else if (strncmp(ptr, "ｎ", 3) == 0) {
            strcpy(halfstr, "n");
        } else if (strncmp(ptr, "ｏ", 3) == 0) {
            strcpy(halfstr, "o");
        } else if (strncmp(ptr, "ｐ", 3) == 0) {
            strcpy(halfstr, "p");
        } else if (strncmp(ptr, "ｑ", 3) == 0) {
            strcpy(halfstr, "q");
        } else if (strncmp(ptr, "ｒ", 3) == 0) {
            strcpy(halfstr, "r");
        } else if (strncmp(ptr, "ｓ", 3) == 0) {
            strcpy(halfstr, "s");
        } else if (strncmp(ptr, "ｔ", 3) == 0) {
            strcpy(halfstr, "t");
        } else if (strncmp(ptr, "ｕ", 3) == 0) {
            strcpy(halfstr, "u");
        } else if (strncmp(ptr, "ｖ", 3) == 0) {
            strcpy(halfstr, "v");
        } else if (strncmp(ptr, "ｗ", 3) == 0) {
            strcpy(halfstr, "w");
        } else if (strncmp(ptr, "ｘ", 3) == 0) {
            strcpy(halfstr, "x");
        } else if (strncmp(ptr, "ｙ", 3) == 0) {
            strcpy(halfstr, "y");
        } else if (strncmp(ptr, "ｚ", 3) == 0) {
            strcpy(halfstr, "z");
        } else return C_const_Stat_NOTFND;

        /* 正常終了 */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_ConvNumFull2Half                                          */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  C_ConvNumFull2Half(char *fullstr, char *halfstr)                 */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              全角算用数字を半角算用数字に変換する                          */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    fullstr ： 変換元                             */
    /*              char       *    halfstr ： 変換先                             */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              ０ : 正常                                                     */
    /*              ９ : 全角算用数字でない                                       */

    /******************************************************************************/
    static int C_ConvNumFull2Half(String fullstr, StringDto halfstr) {
        String ptr;

        ptr = fullstr;

        if (strncmp(ptr, "０", 3) == 0) {
            strcpy(halfstr, "0");
        } else if (strncmp(ptr, "１", 3) == 0) {
            strcpy(halfstr, "1");
        } else if (strncmp(ptr, "２", 3) == 0) {
            strcpy(halfstr, "2");
        } else if (strncmp(ptr, "３", 3) == 0) {
            strcpy(halfstr, "3");
        } else if (strncmp(ptr, "４", 3) == 0) {
            strcpy(halfstr, "4");
        } else if (strncmp(ptr, "５", 3) == 0) {
            strcpy(halfstr, "5");
        } else if (strncmp(ptr, "６", 3) == 0) {
            strcpy(halfstr, "6");
        } else if (strncmp(ptr, "７", 3) == 0) {
            strcpy(halfstr, "7");
        } else if (strncmp(ptr, "８", 3) == 0) {
            strcpy(halfstr, "8");
        } else if (strncmp(ptr, "９", 3) == 0) {
            strcpy(halfstr, "9");
        } else return C_const_Stat_NOTFND;

        /* 正常終了 */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： convKansuujiSuuji                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  convKansuujiSuuji(char *src, char *dst)                          */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              漢数字を数字に変換する                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    src ： 変換元                                 */
    /*              char       *    dst ： 変換先                                 */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              変換後の文字列の長さ                                          */
    /*                                                                            */

    /******************************************************************************/
    public int convKansuujiSuuji(String src, StringDto dst) {
        int ret;

        ret = 0;

        strcpy(dst, src);

        if (memcmp(src, "一", strlen("一")) == 0) {
            strcpy(dst, "1");
            ret = 1;
        }
        if (memcmp(src, "二", strlen("二")) == 0) {
            strcpy(dst, "2");
            ret = 1;
        }
        if (memcmp(src, "三", strlen("三")) == 0) {
            strcpy(dst, "3");
            ret = 1;
        }
        if (memcmp(src, "四", strlen("四")) == 0) {
            strcpy(dst, "4");
            ret = 1;
        }
        if (memcmp(src, "五", strlen("五")) == 0) {
            strcpy(dst, "5");
            ret = 1;
        }
        if (memcmp(src, "六", strlen("六")) == 0) {
            strcpy(dst, "6");
            ret = 1;
        }
        if (memcmp(src, "七", strlen("七")) == 0) {
            strcpy(dst, "7");
            ret = 1;
        }
        if (memcmp(src, "八", strlen("八")) == 0) {
            strcpy(dst, "8");
            ret = 1;
        }
        if (memcmp(src, "九", strlen("九")) == 0) {
            strcpy(dst, "9");
            ret = 1;
        }
        if (memcmp(src, "〇", strlen("〇")) == 0) {
            strcpy(dst, "0");
            ret = 1;
        }
        if (memcmp(src, "十", strlen("十")) == 0) {
            strcpy(dst, "J");
            ret = 1;
        }
        if (memcmp(src, "百", strlen("百")) == 0) {
            strcpy(dst, "H");
            ret = 1;
        }
        if (memcmp(src, "千", strlen("千")) == 0) {
            strcpy(dst, "S");
            ret = 1;
        }
        if (memcmp(src, "万", strlen("万")) == 0) {
            strcpy(dst, "M");
            ret = 1;
        }

        return ret;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： editAddressNumber                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  editAddressNumber(char *wk_addr_del, char *outstr)               */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              住所表示番号抽出処理                                          */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    wk_addr_del ： 該当住所削除文字列１           */
    /*              char       *    outstr   ： 抽出結果を格納                    */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*                                                                            */

    /******************************************************************************/
    int editAddressNumber(String wk_addr_del, StringDto outstr) {
        StringDto buf_src = new StringDto(1024);
        StringDto buf_dst = new StringDto(1024);
        StringDto wk_tmp = new StringDto(100);
        StringDto wk_num = new StringDto(100);
        StringDto p = new StringDto();
        StringDto q = new StringDto();
        StringDto q1 = new StringDto();
        StringDto q2 = new StringDto();
        StringDto buf_tmp = new StringDto(100);
        int numflg;
        int ret;
        int ret2;
        StringDto cnvbuf = new StringDto(100);
        int tyoflg;
        int tyolen;
        int c;
        int c0;
        int chkflg;
        int cc;
        int ii;
        int wk_sum;
        int wk_val;
        int knjflg;


        /* (1) アルファベット(全角半角大文字小文字)を半角大文字に変換する */
        /* 編集元は住所２＋住所３ */
        memset(buf_src, 0x00, sizeof(buf_src));
        strcpy(buf_src, wk_addr_del);
        memset(buf_dst, 0x00, sizeof(buf_dst));

        p = buf_src;
        q = buf_dst;
        StringBuffer qBf = new StringBuffer();
        for (int i = 0; i < p.getLen(); i++) {
            String pi = buf_src.arr.substring(i, i + 1);
            if (C_ConvAlphaFull2Half(pi, buf_tmp) == C_const_OK) {
                /* 全角アルファベット */
                qBf.append(buf_tmp.arr);
            } else {
                qBf.append(pi);
            }
        }
        buf_dst.arr = qBf.toString();

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 1 : buf_dst=[%s]\n", buf_dst);
        }


        /* (2) 算用数字（全角）を算用数字（半角）に変換する */
        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        qBf = new StringBuffer();
        for (int i = 0; i < p.getLen(); i++) {
            String pi = buf_src.arr.substring(i, i + 1);
            if (C_ConvNumFull2Half(pi, buf_tmp) == C_const_OK) {
                /* 全角アルファベット */
                qBf.append(buf_tmp.arr);
            } else {
                qBf.append(pi);
            }
        }
        buf_dst.arr = qBf.toString();
//        while (*p != '\0'){
//            if (C_ConvNumFull2Half(p, buf_tmp) == C_const_OK) {
//                /* 全角アルファベット */
//                        *q = buf_tmp[0];
//                p += 3;
//                q++;
//            } else {
//                        *q = *p;
//                p++;
//                q++;
//            }
//        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 2 : buf_dst=[%s]\n", buf_dst);
        }

        /* (3) 特殊文字（アンパサンド、スラッシュ、中グロ、ピリオド）をとりのぞく */
        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        qBf = new StringBuffer();
        for (int i = 0; i < p.getLen(); i++) {
            String pi = buf_src.arr.substring(i, i + 1);
            if (memcmp(p, "&", strlen("&")) == 0) {
                /* カットする */
//                p += strlen("&");
            } else if (memcmp(p, "＆", strlen("＆")) == 0) {
                /* カットする */
//                p += strlen("＆");
            } else if (memcmp(p, "/", strlen("/")) == 0) {
                /* カットする */
//                p += strlen("/");
            } else if (memcmp(p, "／", strlen("／")) == 0) {
                /* カットする */
//                p += strlen("／");
            } else if (memcmp(p, "･", strlen("･")) == 0) {
                /* 半中グロ カットする */
//                p += strlen("･");
            } else if (memcmp(p, "・", strlen("・")) == 0) {
                /* 全中グロ カットする */
//                p += strlen("・");
            } else if (memcmp(p, ".", strlen(".")) == 0) {
                /* ピリオド カットする */
//                p += strlen(".");
            } else if (memcmp(p, "．", strlen("．")) == 0) {
                /* ピリオド カットする */
//                p += strlen("．");
            } else {
                /* コピーする */
                qBf.append(pi);
//                        *q = *p;

            }
        }
        buf_dst.arr = qBf.toString();

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 3 : buf_dst=[%s]\n", buf_dst);
        }


        /* (4) 下記の特定文字の前にある連続した漢数字または最後にある連続した漢数字は算用数字半角に変換する */
        /*        丁目 丁 番地 番 号 地割 線 の ノ ﾉ                                                        */

        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        memset(wk_tmp, 0x00, sizeof(wk_tmp));
        memset(wk_num, 0x00, sizeof(wk_num));
        p = buf_src;
        q = buf_dst;
        q1 = wk_tmp;
        q2 = wk_num;

        numflg = 0;
        qBf = new StringBuffer();


        while (p.getLen() > 0) {
            ret = isNumStr(p, 3);
            if (ret > 0) {
                /* 漢数字 */

                /* 漢数字をコピーする */
                memcpy(q1, p, ret);
//                q1 += ret;
                /* 漢数字を半角数字に変換してコピーする */
                ret2 = convKansuujiSuuji(p.arr, cnvbuf);
                memcpy(q2, cnvbuf, ret2);
//                q2 += ret2;

                p.arr = p.arr.substring(1);

                numflg = 1;
            } else {
                /* 漢数字以外 */

                if (numflg == 1) {
                    /* 漢数字が前にあり */

                    /* 特定文字かチェックする */
                    tyoflg = 0;

                    if (memcmp(p, "丁目", strlen("丁目")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("丁目");
                    } else if (memcmp(p, "丁堀", strlen("丁堀")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "丁", strlen("丁")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("丁");
                    } else if (memcmp(p, "番地", strlen("番地")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番地");
                    } else if (memcmp(p, "番館", strlen("番館")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番館");
                    } else if (memcmp(p, "番1", strlen("番1")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番1");
                    } else if (memcmp(p, "番2", strlen("番2")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番2");
                    } else if (memcmp(p, "番3", strlen("番3")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番3");
                    } else if (memcmp(p, "番4", strlen("番4")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番4");
                    } else if (memcmp(p, "番5", strlen("番5")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番5");
                    } else if (memcmp(p, "番6", strlen("番6")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番6");
                    } else if (memcmp(p, "番7", strlen("番7")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番7");
                    } else if (memcmp(p, "番8", strlen("番8")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番8");
                    } else if (memcmp(p, "番9", strlen("番9")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番9");
                    } else if (memcmp(p, "番一", strlen("番一")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番二", strlen("番二")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番三", strlen("番三")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番四", strlen("番四")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番五", strlen("番五")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番六", strlen("番六")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番七", strlen("番七")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番八", strlen("番八")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "番九", strlen("番九")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("番");
                    } else if (memcmp(p, "号", strlen("号")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("号");
                    } else if (memcmp(p, "地割", strlen("地割")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("地割");
                    } else if (memcmp(p, "線", strlen("線")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("線");
                    } else if (memcmp(p, "の1", strlen("の1")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の1");
                    } else if (memcmp(p, "の2", strlen("の2")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の2");
                    } else if (memcmp(p, "の3", strlen("の3")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の3");
                    } else if (memcmp(p, "の4", strlen("の4")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の4");
                    } else if (memcmp(p, "の5", strlen("の5")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の5");
                    } else if (memcmp(p, "の6", strlen("の6")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の6");
                    } else if (memcmp(p, "の7", strlen("の7")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の7");
                    } else if (memcmp(p, "の8", strlen("の8")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の8");
                    } else if (memcmp(p, "の9", strlen("の9")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の9");
                    } else if (memcmp(p, "ノ1", strlen("ノ1")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ1");
                    } else if (memcmp(p, "ノ2", strlen("ノ2")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ2");
                    } else if (memcmp(p, "ノ3", strlen("ノ3")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ3");
                    } else if (memcmp(p, "ノ4", strlen("ノ4")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ4");
                    } else if (memcmp(p, "ノ5", strlen("ノ5")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ5");
                    } else if (memcmp(p, "ノ6", strlen("ノ6")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ6");
                    } else if (memcmp(p, "ノ7", strlen("ノ7")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ7");
                    } else if (memcmp(p, "ノ8", strlen("ノ8")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ8");
                    } else if (memcmp(p, "ノ9", strlen("ノ9")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ9");
                    } else if (memcmp(p, "ﾉ1", strlen("ﾉ1")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ1");
                    } else if (memcmp(p, "ﾉ2", strlen("ﾉ2")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ2");
                    } else if (memcmp(p, "ﾉ3", strlen("ﾉ3")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ3");
                    } else if (memcmp(p, "ﾉ4", strlen("ﾉ4")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ4");
                    } else if (memcmp(p, "ﾉ5", strlen("ﾉ5")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ5");
                    } else if (memcmp(p, "ﾉ6", strlen("ﾉ6")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ6");
                    } else if (memcmp(p, "ﾉ7", strlen("ﾉ7")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ7");
                    } else if (memcmp(p, "ﾉ8", strlen("ﾉ8")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ8");
                    } else if (memcmp(p, "ﾉ9", strlen("ﾉ9")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ9");
                    } else if (memcmp(p, "の一", strlen("の一")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の二", strlen("の二")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の三", strlen("の三")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の四", strlen("の四")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の五", strlen("の五")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の六", strlen("の六")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の七", strlen("の七")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の八", strlen("の八")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "の九", strlen("の九")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("の");
                    } else if (memcmp(p, "ノ一", strlen("ノ一")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ二", strlen("ノ二")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ三", strlen("ノ三")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ四", strlen("ノ四")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ五", strlen("ノ五")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ六", strlen("ノ六")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ七", strlen("ノ七")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ八", strlen("ノ八")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ノ九", strlen("ノ九")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ノ");
                    } else if (memcmp(p, "ﾉ一", strlen("ﾉ一")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ二", strlen("ﾉ二")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ三", strlen("ﾉ三")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ四", strlen("ﾉ四")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ五", strlen("ﾉ五")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ六", strlen("ﾉ六")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ七", strlen("ﾉ七")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ八", strlen("ﾉ八")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "ﾉ九", strlen("ﾉ九")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("ﾉ");
                    } else if (memcmp(p, "－", strlen("－")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("－");
                    } else if (memcmp(p, "-", strlen("-")) == 0) {
                        tyoflg = 1;
                        tyolen = strlen("-");
                    } else if ("".equals(p.arr)) {
                        tyoflg = 1;
                    }

                    if (memcmp(p, "番1丁目", strlen("番1丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番2丁目", strlen("番2丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番3丁目", strlen("番3丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番4丁目", strlen("番4丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番5丁目", strlen("番5丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番6丁目", strlen("番6丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番7丁目", strlen("番7丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番8丁目", strlen("番8丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番9丁目", strlen("番9丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番1－", strlen("番1－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番2－", strlen("番2－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番3－", strlen("番3－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番4－", strlen("番4－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番5－", strlen("番5－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番6－", strlen("番6－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番7－", strlen("番7－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番8－", strlen("番8－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番9－", strlen("番9－")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番一丁目", strlen("番一丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番二丁目", strlen("番二丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番三丁目", strlen("番三丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番四丁目", strlen("番四丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番五丁目", strlen("番五丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番六丁目", strlen("番六丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番七丁目", strlen("番七丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番八丁目", strlen("番八丁目")) == 0) {
                        tyoflg = 0;
                    } else if (memcmp(p, "番九丁目", strlen("番九丁目")) == 0) {
                        tyoflg = 0;
                    }

                    if (tyoflg == 1) {
                        /* 特殊文字 */
                        /* 変換した算用数字をコピーする */

                        /*** 千百十等の対応をするか？ */
                        chkflg = 0; /* 0..9以外があれば１になる */
                        for (ii = 0; ii < strlen(wk_num); ii++) {
                            cc = wk_num.arr.charAt(ii);
                            if ('0' <= cc && cc <= '9') {
                                /* 数字 */
                            } else if (cc == '\t') {
                                /* tab */
                            } else {
                                chkflg = 1;
                                break;
                            }
                        }

                        if (dbg_getpostbarcode == 1) {
                            C_DbgMsg("editAddressNumber : 4 : chkflg=[%d]\n", chkflg);
                        }

                        if (chkflg == 1) {
                            /*** 千百十等の対応をする */
                            cc = wk_num.arr.charAt(strlen(wk_num) - 1);
                            if ('0' <= cc && cc <= '9') {
                                strcat(wk_num, "T"); /* １の位を追加 */
                            }

                            wk_sum = 0;
                            wk_val = 1;
                            for (ii = 0; ii < strlen(wk_num); ii++) {
                                if ('0' <= wk_num.arr.charAt(ii) && wk_num.arr.charAt(ii) <= '9') {
                                    /* 数字 */
                                    wk_val = wk_num.arr.charAt(ii) - '0';
                                } else if (wk_num.arr.charAt(ii) == '\t') {
                                    /* tab */
                                } else {
                                    if (wk_num.arr.charAt(ii) == 'M') {
                                        wk_sum += wk_val * 10000;
                                        wk_val = 1;
                                    } else if (wk_num.arr.charAt(ii) == 'S') {
                                        wk_sum += wk_val * 1000;
                                        wk_val = 1;
                                    } else if (wk_num.arr.charAt(ii) == 'H') {
                                        wk_sum += wk_val * 100;
                                        wk_val = 1;
                                    } else if (wk_num.arr.charAt(ii) == 'J') {
                                        wk_sum += wk_val * 10;
                                        wk_val = 1;
                                    } else if (wk_num.arr.charAt(ii) == 'T') {
                                        wk_sum += wk_val * 1;
                                        wk_val = 1;
                                    }

                                }
                            }
                            sprintf(wk_num, "%d", wk_sum);

                        }

                        strcpy(q, wk_num);
                        qBf.append(wk_num);
                        q.arr = q.arr.substring(strlen(wk_num));

                    } else {
                        /* ためていた漢数字をコピーする */
                        strcpy(q, wk_tmp);
                        q.arr = q.arr.substring(strlen(wk_tmp));
                    }

                    memset(wk_tmp, 0x00, sizeof(wk_tmp));
                    memset(wk_num, 0x00, sizeof(wk_num));
                    q1 = wk_tmp;
                    q2 = wk_num;

                    numflg = 0;

                } else {
                    /* その他の文字 */
//                   *q = *p;
//                    p++;
//                    q++;
                    qBf.append(q.arr.substring(0, 1));
                    q.arr = q.arr.substring(1);
                    numflg = 0;

                }
            }
        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 4 : buf_dst=[%s]\n", buf_dst);
        }

        /* (5) 全角文字、連続したアルファベット文字を半角ハイフン１文字に変換 */
        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        knjflg = 0;

        qBf = new StringBuffer();
        for (int i = 0; i < p.arr.length(); i++) {
            c = p.arr.charAt(i);

            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("editAddressNumber : 5-1 : c=[%d]\n", c);
            }
            if (0 < c && c < 128) {
                /* ASCII文字 */
                if (dbg_getpostbarcode == 1) {
                    C_DbgMsg("editAddressNumber : 5-2 : knjflg=[%d]\n", knjflg);
                }
                if ('A' <= c && c <= 'Z') {
                    /* アルファベット */
                    /* １桁前と後ろをチェックする */
                    if ((('A' <= p.arr.charAt(i - 1)) && (p.arr.charAt(i - 1) <= 'Z'))) {
                        /* 前がアルファベット */
                        qBf.append(c);
                    } else if ((('A' <= p.arr.charAt(i + 1)) && (p.arr.charAt(i + 1) <= 'Z'))) {
                        /* 後がアルファベット */
                        qBf.append('-');
                    } else {
                        qBf.append(c);
                    }
                } else {
                    qBf.append(c);
                }

                knjflg = 0;

            } else {
                if (dbg_getpostbarcode == 1) {
                    C_DbgMsg("editAddressNumber : 5-4 : knjflg=[%d]\n", knjflg);
                }
                /* 全角文字 */
                if (-65 < c && c < -33) {
                    /* 全角文字（２バイト） */
                    if (knjflg == 0) {
                        qBf.append('-');
                    } else {
                        /* 読み捨てる */
//                        p += 2;
                    }

                } else {
                    /* 全角文字（３バイト） */
                    if (knjflg == 0) {
                        qBf.append('-');
//                        p += 3;
//                        q++;
                    } else {
                        /* 読み捨てる */
//                        p += 3;
                    }

                }
                knjflg = 1;
            }
        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 5 : buf_dst=[%s]\n", buf_dst);
        }

        /* (6) 算用数字半角、ハイフン半角、連続していないアルファベット１文字(半角)を抜き出す */

        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        //TODO
//        while (*p != '\0'){
//            c = *p;
//
//            if ('0' <= c && c <= '9') {
//                /* 算用数字半角 */
//                        *q = *p;
//                p++;
//                q++;
//            } else if (c == '-') {
//                        *q = *p;
//                p++;
//                q++;
//
//            } else if (c == 'F') {
//                /* (7) 算用数字に続く「F」はハイフンに変換 */
//                c0 = *(q - 1);
//                if (c0 != '-') {
//                    if ('0' <= c0 && c0 <= '9') {
//                                        *q = '-';
//                        p++;
//                        q++;
//                    } else {
//                                        *q = 'F';
//                        p++;
//                        q++;
//                    }
//                } else {
//                                *q = 'F';
//                    p++;
//                    q++;
//                }
//            } else if ('A' <= c && c <= 'Z') {
//                        *q = *p;
//                q++;
//                p++;
//            } else {
//                p++; /* 読み捨てる */
//            }
//        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 6,7 : buf_dst=[%s]\n", buf_dst);
        }

        /* 連続するハイフンは１つにまとめる */
        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        c = 0;

        //todo
//        }
//        while (*p != '\0'){
//            if (*p != '-'){
//                c = *p;
//                        *q = *p;
//                p++;
//                q++;
//            }
//                else{
//                if (c == '-') {
//                    p++;
//                } else {
//                                *q = *p;
//                    p++;
//                    q++;
//                }
//                c = '-';
//            }
//
//        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 7_2 : buf_dst=[%s]\n", buf_dst);
        }

        /* (8) アルファベット文字の前後にあるハイフンは取り除く         */

        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
        qBf = new StringBuffer();
        for (int i = 0; i < p.getLen(); i++) {
            c = p.arr.charAt(i);
            if ('A' <= c && c <= 'Z') {
                /* アルファベットの場合は、直前の文字を見る */
                c0 = p.arr.charAt(i - 1);
                if (c0 == '-') {
                    qBf.setCharAt(qBf.length() - 1, (char) c);
                } else {
                    qBf.append(c);
                }
            } else if (c == '-') {
                /* ハイフンの場合は、直前の文字を見る */
                c0 = p.arr.charAt(i - 1);
                if ('A' <= c0 && c0 <= 'Z') {
                    /* コピーしない */
//                    p++;
                } else {
                    qBf.append(c);
                }
            } else {
                qBf.append(c);
            }
        }
        buf_dst.arr = qBf.toString();
        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 8 : buf_dst=[%s]\n", buf_dst);
        }

        /* (9) 先頭と末尾のハイフンを削除する */
        strcpy(buf_src, buf_dst);
        memset(buf_dst, 0x00, sizeof(buf_dst));
        p = buf_src;
        q = buf_dst;
//        qBf = new StringBuffer();
//        for (int i = 0; i < p.getLen(); i++) {
//        }
//        while (*p == '-' && *p != '\0'){
//            p++;
//        }
//        while (*p != '\0'){
//
//                *q = *p;
//            p++;
//            q++;
//        }

//        for (ii = strlen(buf_dst); 0 < ii; ii--) {
//            if (buf_dst.arr.charAt(ii - 1) != '-') break;
////            buf_dst[ii - 1] = '\0';
//        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("editAddressNumber : 9 : buf_dst=[%s]\n", buf_dst);
        }

        strcpy(outstr, buf_dst);
        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： checkAddressCodeMaster                                      */
    /*                                                                            */
    /*      書式                                                                  */
    /* 2022/09/14 MCCM初版 MOD START */
    /*      //static int checkAddressCodeMaster(char *address1, char *address2 ,    */
    /*      //                                  char *address3, char *postcd,       */
    /*      //                                  char *sid_md,   char *wk_addr_del)  */
    /*      static int checkAddressCodeMaster(char *address,  char *postcd,       */
    /*                                        char *sid_md,   char *wk_addr_del)  */
    /* 2022/09/14 MCCM初版 MOD END */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              住所チェック処理                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /* 2022/09/14 MCCM初版 MOD START */
    /*              char       *    address     ： 住所が格納されているエリアの   */
    /*                                             アドレス                       */
    /*              //char     *    address1    ： 住所１                         */
    /*              //char     *    address2    ： 住所２                         */
    /*              //char     *    address3    ： 住所３                         */
    /*              char       *    postcd      ： 郵便番号                       */
    /*              char       *    sid_md      ： 接続先ＳＩＤ                   */
    /*              char       *    wk_addr_del ： 該当住所削除文字列             */
    /* 2022/09/14 MCCM初版 MOD END */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              3       ： ＤＢエラー                                         */
    /*              9       ： 該当データなし                                     */
    /*                                                                            */

    /******************************************************************************/
    /* 2022/09/14 MCCM初版 MOD START */
    /*static int checkAddressCodeMaster(char *address1, char *address2, char *address3, char *postcd, char *sid_md, char *wk_addr_del)*/
    int checkAddressCodeMaster(StringDto address, StringDto postcd, StringDto sid_md, StringDto wk_addr_del)
    /* 2022/09/14 MCCM初版 MOD END */ {
        StringDto tbl_nam = new StringDto(200);
        StringDto sql_buf = new StringDto(4096);     /* ＳＱＬ文編集用 */
        StringDto wk_addr1 = new StringDto(100);
        StringDto wk_addr2 = new StringDto(256);

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;
        StringDto h_postcd = new StringDto(11);
//        EXEC SQL END DECLARE SECTION;


        strcpy(h_postcd, postcd);
        h_postcd.len = strlen(postcd);

        /* 住所コードマスターを検索する */

        /* ＳＱＬを生成する */
        strcpy(tbl_nam, "MM住所コード情報");
        if (strlen(sid_md) > 0) strcat(tbl_nam, sid_md);

        strcpy(sql_buf, " select nullif(rtrim(住所１), '') , nullif(rtrim(住所２), '') from ");
        strcat(sql_buf, tbl_nam);
        strcat(sql_buf, " where 郵便番号 = ? ");

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("checkAddressCodeMaster : sql=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(sql_buf);

        SqlstmDto sqlca = sqlcaManager.get("sql_checkaddresscodemaster");
        sqlca.sql = WRKSQL;
//        EXEC SQL PREPARE sql_checkaddresscodemaster from :WRKSQL;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("checkAddressCodeMaster : DBERR : sqlcode=[%d]\n", sqlca.sqlcode);
            }
            return C_const_Stat_DBERR;
        }

//        EXEC SQL DECLARE cur_checkaddresscodemaster cursor for sql_checkaddresscodemaster;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("checkAddressCodeMaster : DBERR : sqlcode=[%d]\n", sqlca.sqlcode);
            }
            return C_const_Stat_DBERR;
        }

        /* 生成したＳＱＬを発行する */

        /* カーソルのオープン */
        sqlca.open(h_postcd);
//        EXEC SQL OPEN cur_checkaddresscodemaster using :h_postcd;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("checkAddressCodeMaster : DBERR : sqlcode=[%d]\n", sqlca.sqlcode);
            }
            return C_const_Stat_DBERR;
        }

        /* カーソルのフェッチ */
        memset(DATA01.arr, 0x00, 100);
        DATA01.len = 0;
        memset(DATA02.arr, 0x00, 100);
        DATA02.len = 0;
        sqlca.fetchInto(DATA01, DATA02);
//        EXEC SQL FETCH cur_checkaddresscodemaster into :DATA01, :DATA02;
        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("checkAddressCodeMaster : FETCH : sqlcode=[%d]\n", sqlca.sqlcode);
        }
        if (sqlca.sqlcode == C_const_Ora_OK) {
            /* 該当データあり */
            H_DATCNT = 1;
            /* return C_const_Stat_OK; */
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 該当データなし */
            H_DATCNT = 0;
            /* return C_const_Stat_NOTFND; */
        } else {
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("checkAddressCodeMaster : DBERR : sqlcode=[%d]\n", sqlca.sqlcode);
            }
            /* DBERR */
            return C_const_Stat_DBERR;
        }


        /* カーソルのクローズ */
//        EXEC SQL CLOSE cur_checkaddresscodemaster;
        sqlcaManager.close("sql_checkaddresscodemaster");
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* error */
            if (dbg_getpostbarcode == 1) {
                C_DbgMsg("checkAddressCodeMaster : DBERR(close) : sqlcode=[%d]\n", sqlca.sqlcode);
            }
        }

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("checkAddressCodeMaster : DATCNT=[%d]\n", H_DATCNT);
            C_DbgMsg("checkAddressCodeMaster : ADD1=[%s]\n", DATA01.arr);
            C_DbgMsg("checkAddressCodeMaster : ADD2=[%s]\n", DATA02.arr);
        }

        /* 住所マスタ検索データ無し */
        if (H_DATCNT == 0) return C_const_Stat_NOTFND;


        /* 2022/09/14 MCCM初版 MOD START */
//        /* 入力の住所を連結する 右の全角半角は削除する*/
        /*        strcpy(wk_addr2, address1);*/
        /* 入力の住所の右の全角半角を削除する*/
        strcpy(wk_addr2, address);
        /* 2022/09/14 MCCM初版 MOD END */
        addr_rtrim(wk_addr2);
        /* 2022/09/14 MCCM初版 DEL START */
/*        strcat(wk_addr2, address2);
        addr_rtrim( wk_addr2 );
        strcat(wk_addr2, address3);
        addr_rtrim( wk_addr2 );*/
        /* 2022/09/14 MCCM初版 DEL END */


        /* 住所マスターの住所を連結する 右の全角半角は削除する*/
        strcpy(wk_addr1, DATA01.arr);
        addr_rtrim(wk_addr1);
        strcat(wk_addr1, DATA02.arr);
        addr_rtrim(wk_addr1);

        if (dbg_getpostbarcode == 1) {
            C_DbgMsg("checkAddressCodeMaster : ADD1(結合後)=[%s]\n", wk_addr1);
            /* 2022/09/14 MCCM初版 MOD START */
            /*        C_DbgMsg("checkAddressCodeMaster : ADD2(結合後)=[%s]\n", wk_addr2);*/
            C_DbgMsg("checkAddressCodeMaster : ADD2(修正後)=[%s]\n", wk_addr2);
            /* 2022/09/14 MCCM初版 MOD END */
        }

        /* 入力の住所と住所マスターの住所がマッチするかチェック */
        if (memcmp(wk_addr1, wk_addr2.arr, strlen(wk_addr1)) != 0) {
            return C_const_Stat_NOTFND;
        }


        /* 該当住所文字列を編集する */
        memcpy(wk_addr_del, (wk_addr2.arr.substring(strlen(wk_addr1))), (strlen(wk_addr2) - strlen(wk_addr1)));
        return C_const_Stat_OK;

    }


/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： addr_rtrim                              */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  addr_rtrim(char *wkr_addr)                                       */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              右の半角スペース、全角スペースを削除する                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    wkr_addr  ： 入力文字列                       */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              無し                                                          */
    /*                                                                            */
    /*                                                                            */

    /******************************************************************************/
    void addr_rtrim(StringDto wrk_addr) {

        while (trim(wrk_addr)) {
        }
    }

    public boolean trim(StringDto str) {
        if (str.arr.endsWith(" ")) {
            str.arr = str.arr.substring(0, str.arr.lastIndexOf(" "));
            return true;
        } else if (str.arr.endsWith("　")) {
            str.arr = str.arr.substring(0, str.arr.lastIndexOf("　"));
            return true;
        }
        return false;
    }


/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： isNumStr                                                    */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  isNumStr(char *buf, int flg)                                     */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              算用文字か判定する                                            */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *  buf ： 処理対象の文字列                         */
    /*              int           flg ： 処理区分(1:半角数字 2:全角数字 3:漢数字) */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              算用文字の場合はサイズを返す                                  */
    /*              算用文字以外の場合は０を返す                                  */
    /*                                                                            */

    /******************************************************************************/
    static int isNumStr(StringDto buf, int flg) {
        int c;

        if (flg == 1) {
            /* 0...9 */
            c = buf.arr.charAt(0);
            if ('0' <= c && c <= '9') {
                return 1;
            }
        } else if (flg == 2) {
            if (memcmp(buf, "０", strlen("０")) == 0) return strlen("０");
            if (memcmp(buf, "１", strlen("１")) == 0) return strlen("１");
            if (memcmp(buf, "２", strlen("２")) == 0) return strlen("２");
            if (memcmp(buf, "３", strlen("３")) == 0) return strlen("３");
            if (memcmp(buf, "４", strlen("４")) == 0) return strlen("４");
            if (memcmp(buf, "５", strlen("５")) == 0) return strlen("５");
            if (memcmp(buf, "６", strlen("６")) == 0) return strlen("６");
            if (memcmp(buf, "７", strlen("７")) == 0) return strlen("７");
            if (memcmp(buf, "８", strlen("８")) == 0) return strlen("８");
            if (memcmp(buf, "９", strlen("９")) == 0) return strlen("９");
        } else if (flg == 3) {
            if (memcmp(buf, "〇", strlen("〇")) == 0) return strlen("〇");
            if (memcmp(buf, "一", strlen("一")) == 0) return strlen("一");
            if (memcmp(buf, "二", strlen("二")) == 0) return strlen("二");
            if (memcmp(buf, "三", strlen("三")) == 0) return strlen("三");
            if (memcmp(buf, "四", strlen("四")) == 0) return strlen("四");
            if (memcmp(buf, "五", strlen("五")) == 0) return strlen("五");
            if (memcmp(buf, "六", strlen("六")) == 0) return strlen("六");
            if (memcmp(buf, "七", strlen("七")) == 0) return strlen("七");
            if (memcmp(buf, "八", strlen("八")) == 0) return strlen("八");
            if (memcmp(buf, "九", strlen("九")) == 0) return strlen("九");
            if (memcmp(buf, "十", strlen("十")) == 0) return strlen("十");
            if (memcmp(buf, "百", strlen("百")) == 0) return strlen("百");
            if (memcmp(buf, "千", strlen("千")) == 0) return strlen("千");
            if (memcmp(buf, "万", strlen("万")) == 0) return strlen("万");
        }

        return 0;

    }


    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetSaveKkn                                                */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetSaveKkn( char *tblgrp, char *base_date, char *out_yyyymmdd,  */
    /*                        char *out_yyyymm, char *out_yyyy, char *out_yyyy2,  */
    /*                        int  *status )                                      */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              保存期間取得処理                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char *tblgrp       ： テーブル削除グループ名を格納            */
    /*              char *base_date    ： 基準日をYYYYMMDDで指定                  */
    /*                                    ALL ゼロ("00000000"が指定された場合、   */
    /*                                    バッチ処理日が適用される。              */
    /*              char *out_yyyymmdd ： 日別削除年月日                          */
    /*              char *out_yyyymm   ： 月別削除年月日                          */
    /*              char *out_yyyy     ： 年別削除年                              */
    /*              char *out_yyyymmdd2： 参照テーブルの削除年月日                */
    /*              int  *status       ： ステータス                              */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetSaveKkn(String tblgrp, String base_date, StringDto out_yyyymmdd, StringDto out_yyyymm, StringDto out_yyyy, StringDto out_yyyymmdd2, IntegerDto status) {
        String wk_sql = "";                    /* 実行SQL文字列                */
        int rtn_cd;                          /* 関数戻り値                   */
        String in_msg_id = "";                  /* APログメッセージID           */
        IntegerDto out_status = new IntegerDto();                      /* フォーマット取得結果         */
        String[] out_flg = new String[2];                      /* APログフラグ                 */
        String[] out_format = new String[2];   /* APログフォーマット           */
        String out_keybuf = "";   /* APログ出力情報               */
        String wk_outstr = "";
        int sitei_kbn;                       /* 取得日付区分                 */
        StringDto w_date = new StringDto();                       /* 取得日付文字列               */
        IntegerDto rtn_status = new IntegerDto();                      /* 日付取得関数ステータス       */
        /*----------------------*/
        /*  ホスト変数          */
        /*----------------------*/
        /* EXEC SQL BEGIN DECLARE SECTION; */

        /* SQL文・テーブル名 */
        /* VARCHAR       WRKSQL[4096]; */
        StringDto WRKSQL = new StringDto();

        String this_tblgrp = null;        /* テーブルグループ            */
        IntegerDto get_ymd = new IntegerDto();                /* 日保存期間                  */
        IntegerDto get_ym = new IntegerDto();                 /* 月保存期間                  */
        IntegerDto get_y = new IntegerDto();                  /* 年保存期間                  */
        /* VARCHAR       ymd_date[9]; */
        ItemDto ymd_date = new ItemDto();
        /* 保存日付（YMD)              */
        /* VARCHAR       ym_date[9]; */
        ItemDto ym_date = new ItemDto();
        /* 保存日付（YM)               */
        /* VARCHAR       y_date[9]; */
        ItemDto y_date = new ItemDto();
        /* 保存日付（Y)                */
        /* VARCHAR       ym_date2[9]; */
        ItemDto ym_date2 = new ItemDto();
        /* 保存日付（YM2)              */
        /* VARCHAR       y_date2[9]; */
        ItemDto y_date2 = new ItemDto();
        /* 保存日付（Y2)               */
        /* EXEC SQL END DECLARE SECTION; */


        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (tblgrp == null || base_date == null) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** %s\n", "NO PRMERR");
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        if (strlen(tblgrp) == 0 || strlen(base_date) != 8) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** %s\n", "PRM DATA ERR");
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        if (strlen(Cg_ORASID) == 0 ||
                strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** %s\n", "PRM DB NOT CONNECT ERR");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /************************/
        /* 主処理               */
        /************************/
        /* 保存期間情報より対象データを取得 */
        memset(this_tblgrp, 0x00, sizeof(this_tblgrp));
        this_tblgrp = strcpy(this_tblgrp, tblgrp);
        get_ymd = new IntegerDto();
        get_ym = new IntegerDto();
        get_y = new IntegerDto();
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** テーブルＩＤ=%s\n", this_tblgrp);
        }

        /* EXEC SQL SELECT 日保存期間,月保存期間,年保存期間
            INTO :get_ymd,:get_ym,:get_y
            FROM   PS保存期間情報
           WHERE テーブルＩＤ = :this_tblgrp; */
        WRKSQL.arr = sprintf("SELECT 日保存期間 ,月保存期間 ,年保存期間  from PS保存期間情報 where テーブルＩＤ= '%s'", this_tblgrp);
        sqlca.sql = WRKSQL;

        sqlca.restAndExecute();
        sqlca.fetch();
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** PS保存期間情報 SELECT sqlcode=[%d]\n", sqlca.sqlcode);
        }
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** PS保存期間情報 SELECT %s\n", "NG");
            }
            memset(out_keybuf, 0x00, sizeof(out_keybuf));
            out_keybuf = sprintf(out_keybuf, "テーブルＩＤ = %s ", this_tblgrp);
            /*-------------------------------------*/
            /*  APログフォーマット取得処理         */
            /*-------------------------------------*/
            rtn_cd = C_const_OK;
            memset(in_msg_id, 0x00, sizeof(in_msg_id));
            in_msg_id = strcpy(in_msg_id, "904");
            rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null, out_flg, out_format, out_status);

            /*-------------------------------------*/
            /*  APログ出力処理                     */
            /*-------------------------------------*/
            rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                    "SELECT", (long) sqlca.sqlcode, "PS保存期間情報", out_keybuf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* データ無し */
        if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            strcpy(out_yyyymmdd, "00000000");
            strcpy(out_yyyymm, "000000");
            strcpy(out_yyyy, "0000");
            strcpy(out_yyyymmdd2, "00000000");
            status.arr = C_const_Stat_ELSERR;
            return C_const_NG;
        }

        sqlca.recData(get_ymd, get_ym, get_y);

        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** get_ymd=%d\n", get_ymd);
            C_DbgMsg("*** C_GetSaveKkn *** get_ym=%d\n", get_ym);
            C_DbgMsg("*** C_GetSaveKkn *** get_y=%d\n", get_y);
        }
        /* 基準日がALLゼロの場合、バッチ処理日を取得する */
        if (strcmp(base_date, "00000000") == 0) {
            sitei_kbn = 0;
            memset(w_date, 0x00, sizeof(w_date));
            rtn_cd = C_GetBatDate(sitei_kbn, w_date, rtn_status);
            if (rtn_cd != C_const_OK) {
                if (dbg_GetSaveKkn == 1) {
                    C_DbgMsg("*** C_GetSaveKkn *** error(rtn_cd=%d)\n", rtn_cd);
                }
                /*-------------------------------------*/
                /*  APログフォーマット取得処理         */
                /*-------------------------------------*/
                rtn_cd = C_const_OK;
                memset(in_msg_id, 0x00, sizeof(in_msg_id));
                in_msg_id = strcpy(in_msg_id, "903");
                rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null,
                        out_flg, out_format, out_status);

                /*-------------------------------------*/
                /*  APログ出力処理                     */
                /*-------------------------------------*/
                rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                        "C_GetBatDate", (long) rtn_cd,
                        rtn_status, 0, 0, 0);
                status.arr = rtn_status.arr;
                return C_const_NG;
            }

            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** date=%s\n", w_date);
            }

        } else {
            strcpy(w_date, base_date);
        }

        /*  日付算出SQLを編集 */
        wk_sql = sprintf(wk_sql, "select  to_char( to_date( '%s', 'YYYYMMDD' ) - %d, 'YYYYMMDD' ) as Fld_dd, "
                        + "        to_char( last_day ( add_months(  to_date('%s', 'YYYYMMDD' ) , -1 * %d  ) ), 'YYYYMMDD' ) as Fld_mm, "
                        + "        concat(to_char(to_number( substr( '%s',1,4 ))  - %d),'1231') as Fld_yy,  "
                        + "        to_char( last_day ( add_months(  to_date('%s', 'YYYYMMDD' ) , -1 * %d - 1  ) ), 'YYYYMMDD' ) as Fld_mm2, "
                        + "        concat(to_char(to_number( substr( '%s',1,4 ))  - %d - 1 ),'1231') as Fld_yy2  "
                        + "from dual", w_date, get_ymd,
                w_date, get_ym,
                w_date, get_y,
                w_date, get_ym,
                w_date, get_y);

        strcpy(WRKSQL, wk_sql);
        WRKSQL.len = strlen(WRKSQL.arr);

        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** WRKSQL=[%s]\n", WRKSQL.arr);
        }
        SqlstmDto sqlca = sqlcaManager.get("cur_getdate");
        /* EXEC SQL PREPARE sql_getdec from :WRKSQL; */
        sqlca.sql = WRKSQL;
        sqlca.prepare();


        if (sqlca.sqlcode == -1841 || sqlca.sqlcode == -1843 || sqlca.sqlcode == -1847) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** PREPARE 基準日指定エラー（日付算出不能） sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        } else if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */

            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** PREPARE ERR sqlcode=[%d]\n", sqlca.sqlcode);
            }

            /*-------------------------------------*/
            /*  APログフォーマット取得処理         */
            /*-------------------------------------*/
            rtn_cd = C_const_OK;
            memset(in_msg_id, 0x00, sizeof(in_msg_id));
            in_msg_id = strcpy(in_msg_id, "902");
            rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null,
                    out_flg, out_format, out_status);

            /*-------------------------------------*/
            /*  APログ出力処理                     */
            /*-------------------------------------*/
            rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                    sqlca.sqlcode, "dual  [dec PREPARE]", 0, 0, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* EXEC SQL DECLARE cur_getdate CURSOR FOR sql_getdec; */


        /* EXEC SQL OPEN cur_getdate; */
        sqlca.open();


        if (sqlca.sqlcode == -1841 || sqlca.sqlcode == -1843 || sqlca.sqlcode == -1847) {
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** OPEN 基準日指定エラー（日付算出不能） sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        } else if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** OPEN ERR sqlcode=[%d]\n", sqlca.sqlcode);
            }
            /*-------------------------------------*/
            /*  APログフォーマット取得処理         */
            /*-------------------------------------*/
            rtn_cd = C_const_OK;
            memset(in_msg_id, 0x00, sizeof(in_msg_id));
            in_msg_id = strcpy(in_msg_id, "902");
            rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null,
                    out_flg, out_format, out_status);

            /*-------------------------------------*/
            /*  APログ出力処理                     */
            /*-------------------------------------*/
            rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                    sqlca.sqlcode, "dual  [dec OPEN]", 0, 0, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        sqlca.fetch();
        /* EXEC SQL FETCH cur_getdate into :ymd_date,:ym_date,:y_date,:ym_date2,:y_date2; */
        sqlca.recData(ymd_date, ym_date, y_date, ym_date2, y_date2);


        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            if (dbg_GetSaveKkn == 1) {
                C_DbgMsg("*** C_GetSaveKkn *** FETCH ERR sqlcode=[%d]\n", sqlca.sqlcode);
            }
            /*-------------------------------------*/
            /*  APログフォーマット取得処理         */
            /*-------------------------------------*/
            rtn_cd = C_const_OK;
            memset(in_msg_id, 0x00, sizeof(in_msg_id));
            in_msg_id = strcpy(in_msg_id, "902");
            rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null,
                    out_flg, out_format, out_status);

            /*-------------------------------------*/
            /*  APログ出力処理                     */
            /*-------------------------------------*/
            rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                    sqlca.sqlcode, "dual  [dec FETCH]", 0, 0, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* EXEC SQL CLOSE cur_getdate; */
//        sqlca.close();
        sqlcaManager.close("cur_getdate");



        /* 参照テーブルの保存日付編集 */
        strcpy(out_yyyymmdd2, "99999999");
        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, ymd_date.strVal(), ymd_date.strVal().length());
        if (get_ymd.arr != 0 && atoi(out_yyyymmdd2) > atoi(wk_outstr)) {
            strcpy(out_yyyymmdd2, wk_outstr);
        }
        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, ym_date2.strVal(), ym_date2.strVal().length());
        if (get_ym.arr != 0 && atoi(out_yyyymmdd2) > atoi(wk_outstr)) {
            strcpy(out_yyyymmdd2, wk_outstr);
        }
        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, y_date2.strVal(), y_date2.strVal().length());
        if (get_y.arr != 0 && atoi(out_yyyymmdd2) > atoi(wk_outstr)) {
            strcpy(out_yyyymmdd2, wk_outstr);
        }

        /* 保存期間がすべてゼロなら、参照テーブルの保存日付はALLゼロ */
        if (get_ymd.arr == 0 && get_ym.arr == 0 && get_y.arr == 0) {
            strcpy(out_yyyymmdd2, "00000000");
        }
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** 参照テーブル保存日付3=[%s]\n", out_yyyymmdd2);
        }
        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, ymd_date.strVal(), ymd_date.strVal().length());
        strcpy(out_yyyymmdd, wk_outstr);

        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, ym_date.strVal(), ym_date.strVal().length());
        strncpy(out_yyyymm, wk_outstr, 6);

        memset(wk_outstr, 0x00, sizeof(wk_outstr));
        wk_outstr = memcpy(wk_outstr, y_date.strVal(), y_date.strVal().length());
        strncpy(out_yyyy, wk_outstr, 4);

        if (get_ymd.arr == 0) {
            strcpy(out_yyyymmdd, "00000000");
        }
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** 日別保存日付=[%s]\n", out_yyyymmdd);
        }
        if (get_ym.arr == 0) {
            strcpy(out_yyyymm, "000000");
        }
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** 月別保存日付=[%s]\n", out_yyyymm);
        }
        if (get_y.arr == 0) {
            strcpy(out_yyyy, "0000");
        }
        if (dbg_GetSaveKkn == 1) {
            C_DbgMsg("*** C_GetSaveKkn *** 年別保存日付=[%s]\n", out_yyyy);
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

    }

    /**
     * バッチデバッグ開始処理
     *
     * @param argc
     * @param argv
     * @return 0： 正常; 1： 異常; 2： 引数エラー
     */
    public int C_StartBatDbg(int argc, String[] argv) {
        int found_flg;
        String p_trace_file_dir;
        String w_trace_file_dir = ".";
        String pgm_name = argv[0];
        long w_pid;
        String w_trace_file_name;

        // 引数が設定されていない場合はエラー
        if (argc == 0 || argv == null) {
            return C_const_PARAMERR;
        }

        // 引数で-DEBUGまたは-debugが指定されているか調べる
        found_flg = 0;
        for (int i = 0; i < argc; i++) {
            if (argv[i].equals("-DEBUG") || argv[i].equals("-debug")) {
                found_flg = 1;
                break;
            }
        }
        if (found_flg == 0) {
            // トレースファイルポインタを初期化する
            return C_const_OK;
        }

        // 環境変数からトレースファイル格納先を取得する
        p_trace_file_dir = getenv(C_TRACE_FILE_DIR);
        if (p_trace_file_dir != null) {
            w_trace_file_dir = p_trace_file_dir;
        }

        // プロセスIDを取得する
        w_pid = Long.parseLong(PidUtil.getPid());
//        w_pid = ProcessHandle.current().pid();

        // ファイル名を編集する
        w_trace_file_name = String.format("%s/%s_%d.log", w_trace_file_dir, pgm_name, w_pid);

        // ファイルをオープンする
        C_DbgInit(w_trace_file_name);
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_EndBatDbg                                                 */
    /*                                                                            */
    /*  書式                                                                  */
    /*  int  C_EndBatDbg()                                                    */
    /*                                                                            */
    /*  【説明】                                                              */
    /*              バッチデバッグ終了処理                                        */
    /*                                                                            */
    /*  【引数】                                                              */
    /*      なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/

//    public int C_EndBatDbg() {
//      return   super.C_EndBatDbg();
//////
////        if (FP_DEBUG_FILE != null) {
////            /* ファイルをオープンしている場合 */
////            if (fclose(FP_DEBUG_FILE) != 0) {
////                /* クローズエラー */
////                return C_const_NG;
////            }
////            /* ファイルポインタもクリアする */
////            FP_DEBUG_FILE = null;
////        }
////
////        return C_const_OK;
//    }
    @Override
    public int C_GetAPLogFmt(String msgid, int msgidsbt, String dbkbn, String[] flg, String[] format, IntegerDto status) {
        int n_msgid;
        int conn_exec_flg; /* コネクト実行フラグ */
        String w_sv_pgname = null;   /* セーブプログラム名 */
        String w_msgid = null;       /* メッセージＩＤ */
        String w_format = null;    /* フォーマット */
        String w_flg = null;          /* フラグ */
        String w_sv_msgid = null;    /* セーブメッセージＩＤ */
        String w_sv_format = null; /* セーブフォーマット */
        String w_sv_flg = null;       /* セーブフラグ */
        String w_cm_msgid = null;    /* 汎用メッセージＩＤ */
        String w_cm_format = null; /* 汎用フォーマット */
        String w_cm_flg = null;       /* 汎用フラグ */
        String penv = null;
        String pwrk = null;
        String w_server_id = null;   /* サーバ識別 */
        String w_dbkbn = null;        /* ＤＢ区分 */
        int ret;
        IntegerDto sts = new IntegerDto();
        int i;
        int j;
        String w_tblname = null;     /* テーブル名 */
        String w_where = null;
        String w_cmp = null;


        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : %s\n", "start");
        }

        /* 引数のチェック */
        if (msgid == null || flg == null || format == null || status == null) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : %s\n", "PRMERR");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }
        if (msgidsbt != 0 && msgidsbt != 1 && msgidsbt != 2) {
            if (atoi(msgid) < 901 || 909 < atoi(msgid)) {
                if (dbg_getaplogfmt == 1) {
                    C_DbgMsg("C_GetAPLogFmt : %s\n", "PRMERR(msgidsbt)");
                    C_DbgMsg("C_GetAPLogFmt : msgidsbt=%d\n", msgidsbt);
                }
                if (status != null) status.arr = C_const_Stat_PRMERR;
                return C_const_NG;
            }
        }

        if (msgidsbt == 0) {
            /* 固定メッセージを検索する */
            ret = getAplogMsg(msgid, flg, format);
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : getAplogMsg=%d\n", ret);
                C_DbgMsg("C_GetAPLogFmt : msgid=%s\n", msgid);
            }
            if (ret == C_const_OK) {
                if (dbg_getaplogfmt == 1) {
                    C_DbgMsg("C_GetAPLogFmt : flg=[%s]\n", flg);
                    C_DbgMsg("C_GetAPLogFmt : fmt=[%s]\n", format);
                    C_DbgMsg("C_GetAPLogFmt : %s\n", "end");
                }
                status.arr = C_const_Stat_OK;
                return C_const_OK;
            }
        }

        /* プログラム名のチェックを行う */
        n_msgid = atoi(msgid);
        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : n_msgid=%d\n", n_msgid);
            C_DbgMsg("C_GetAPLogFmt : msgidsbt=%d\n", msgidsbt);
        }
        if ((901 <= n_msgid && n_msgid <= 909) || (strlen(Cg_Program_Name) > 0)) {
            /* OK */
        } else {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : %s\n", "ELSERR");
            }
            if (status != null) status.arr = C_const_Stat_ELSERR;
            return C_const_NG;
        }

        /* ローカル変数の初期化 */
        conn_exec_flg = 0;

//        memset(w_sv_pgname, 0x00, sizeof(w_sv_pgname));
//        memset(w_msgid, 0x00, sizeof(w_msgid));
//        memset(w_format, 0x00, sizeof(w_format));
//        memset(w_flg, 0x00, sizeof(w_flg));
//        memset(w_sv_msgid, 0x00, sizeof(w_sv_msgid));
//        memset(w_sv_format, 0x00, sizeof(w_sv_format));
//        memset(w_sv_flg, 0x00, sizeof(w_sv_flg));
//        memset(w_cm_msgid, 0x00, sizeof(w_cm_msgid));
//        memset(w_cm_format, 0x00, sizeof(w_cm_format));
//        memset(w_cm_flg, 0x00, sizeof(w_cm_flg));

        /* 環境変数から接続先ＤＢを取得する */
        penv = getenv(C_CM_SERVER_ID);
        if (StringUtils.isEmpty(penv)) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : %s\n", "getenv = null");
            }
            /* msgidの１桁目が'9'の場合 */
            if (msgid.charAt(0) == '9') {
                flg[0] = C_const_APLOG_flg_E;
                flg[1] = "\0";
            }
            /* msgidの１桁目が'7'の場合 */
            else if (msgid.charAt(0) == '7') {
                flg[0] = C_const_APLOG_flg_W;
                flg[1] = "\0";
            } else {
                strcpy(flg, " ");
            }

            if ((901 <= n_msgid && n_msgid <= 909)) {
                /* 処理を続行する */
                w_server_id = strcpy(w_server_id, "    "); /* サーバＩＤをブランクにして続行 */
            } else {
                /* 処理を終了する */
                sprintf(format, "フォーマット取得エラー メッセージＩＤ=%s 登録種別=%d", msgid, msgidsbt);
                status.arr = C_const_Stat_ENVERR;
                return C_const_NG;
            }
        }

        if (penv != null) {
            w_server_id = strcpy(w_server_id, penv);
        }

//        memset(w_dbkbn, 0x00, sizeof(w_dbkbn));
        if (w_server_id.startsWith("D")) {
            w_dbkbn = memcpy(w_dbkbn, w_server_id, 2);
        } else {
            /* 顧客制度ＤＢがデフォルト接続先 */
            w_dbkbn = memcpy(w_dbkbn, "SD", 2);
        }

        /* ＤＢコネクトのチェックを行う */
        if (901 <= n_msgid && n_msgid <= 909) {
            /* チェックなし(固定値を使用するのでＤＢ検索不要) */
        } else {
            if (dbkbn == null && strlen(Cg_ORASID) == 0) {
                if (dbg_getaplogfmt == 1) {
                    C_DbgMsg("C_GetAPLogFmt : %s\n", "not connect");
                }
            }


        }

        if (strlen(Cg_ORASID) == 0 && (901 > n_msgid || n_msgid > 909)) {
            /* ＤＢ接続処理呼び出し */
            if (dbkbn != null) {
                ret = C_OraDBConnect(dbkbn, sts);
            } else {
                ret = C_OraDBConnect(w_dbkbn, sts);
            }

            if (ret == C_const_OK) {
                conn_exec_flg = 1;
            } else {
                if (dbg_getaplogfmt == 1) {
                    C_DbgMsg("C_GetAPLogFmt : %s\n", "DB connect NG");
                }
                /* msgidの１桁目が'9'の場合 */
                if (msgid.charAt(0) == '9') {
                    flg[0] = C_const_APLOG_flg_E;
//                    flg[1] = '\0';
                }
                /* msgidの１桁目が'7'の場合 */
                else if (msgid.charAt(0) == '7') {
                    flg[0] = C_const_APLOG_flg_W;
//                    flg[1] = '\0';
                } else {
                    strcpy(flg, " ");
                }
                /* 処理を終了する */
                sprintf(format, "フォーマット取得エラー メッセージＩＤ=%s 登録種別=%d", msgid, msgidsbt);
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
        }

        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : n_msgid=%d\n", n_msgid);
        }
        /* if (n_msgid < 901 || n_msgid > 909) { */
        /* ＤＢ検索 */
        StringDto H_APLOGFMT_MSGID = new StringDto();
        /* ホスト変数の編集 */
        memset(H_APLOGFMT_MSGID.arr, 0x00, sizeof(H_APLOGFMT_MSGID.arr));
        H_APLOGFMT_MSGID.arr = strcpy(H_APLOGFMT_MSGID.arr, msgid);
        H_APLOGFMT_MSGID.len = strlen(H_APLOGFMT_MSGID.arr);

        w_tblname = strcpy(w_tblname, "PSＡＰログフォーマット情報");

        /* where句の編集 */
        if (msgidsbt == 0) w_where = strcpy(w_where, " and プログラムＩＤ = '?????????' ");
        else if (msgidsbt == 1)
            w_where = strcpy(w_where, " and プログラムＩＤ != '?????????' and (プログラムＩＤ like '%?%' or length(プログラムＩＤ) < 9) ");
        else if (msgidsbt == 2) w_where = strcpy(w_where, " and プログラムＩＤ not like '%?%' ");
        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : where=[%s]\n", w_where);
        }
        StringDto WRKSQL = new StringDto();
//        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        WRKSQL.arr = sprintf(WRKSQL.arr, "select nullif(rtrim(プログラムＩＤ),'') , rpad(メッセージＮＯ,length(メッセージＮＯ)) as メッセージＮＯ ,nvl(rpad(フラグ,length(フラグ)), ' '),nullif(rtrim(nvl(フォーマット, ' ')),'') from %s where メッセージＮＯ = '%s' %s ", w_tblname, msgid, w_where);
        WRKSQL.len = strlen(WRKSQL.arr);

        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : sql=[%s]\n", WRKSQL.arr);
        }

        SqlstmDto sqlca = sqlcaManager.get("cur_getaplogfmt");
        /* カーソルのオープン */
        /* EXEC SQL PREPARE sql_getaplogfmt FROM :WRKSQL; */
        sqlca.sql = WRKSQL;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : PREPARE NG(%d) \n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        sqlca.declare();
        /* EXEC SQL DECLARE cur_getaplogfmt CURSOR FOR sql_getaplogfmt; */

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : DECLARE NG(%d) \n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        sqlca.open();
        /* カーソルのオープン */
        /* EXEC SQL OPEN cur_getaplogfmt; */

//        {
//            struct sqlexd sqlstm;
//            sqlstm.sqlvsn = 13;
//            sqlstm.arrsiz = 4;
//            sqlstm.sqladtp = &sqladt;
//            sqlstm.sqltdsp = &sqltds;
//            sqlstm.stmt = "";
//            sqlstm.iters = (unsigned int  )1;
//            sqlstm.offset = (unsigned int  )191;
//            sqlstm.selerr = (unsigned short)1;
//            sqlstm.sqlpfmem = (unsigned int  )0;
//            sqlstm.cud = sqlcud0;
//            sqlstm.sqlest = (unsigned char  *)&sqlca;
//            sqlstm.sqlety = (unsigned short)4352;
//            sqlstm.occurs = (unsigned int  )0;
//            sqlstm.sqcmod = (unsigned int )0;
//            sqlcxt(( void **)0, &sqlctx, &sqlstm, &sqlfpn);
//        }


        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : OPEN NG(%d) \n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        for (; ; ) {
            /* カーソルのフェッチ */
//            memset(H_APLOGFMT_FORMAT.arr, 0x00, sizeof(H_APLOGFMT_FORMAT.arr));
            /* EXEC SQL FETCH cur_getaplogfmt INTO :H_APLOGFMT_PGMID, H_APLOGFMT_MSGID, H_APLOGFMT_FLG, H_APLOGFMT_FORMAT; */
//            {
//                struct sqlexd sqlstm;
//                sqlstm.sqlvsn = 13;
//                sqlstm.arrsiz = 4;
//                sqlstm.sqladtp = &sqladt;
//                sqlstm.sqltdsp = &sqltds;
//                sqlstm.iters = (unsigned int  )1;
//                sqlstm.offset = (unsigned int  )206;
//                sqlstm.selerr = (unsigned short)1;
//                sqlstm.sqlpfmem = (unsigned int  )0;
//                sqlstm.cud = sqlcud0;
//                sqlstm.sqlest = (unsigned char  *)&sqlca;
//                sqlstm.sqlety = (unsigned short)4352;
//                sqlstm.occurs = (unsigned int  )0;
//                sqlstm.sqfoff = (int) 0;
//                sqlstm.sqfmod = (unsigned int )2;
//                sqlstm.sqhstv[0] = (unsigned char  *)&H_APLOGFMT_PGMID;
//                sqlstm.sqhstl[0] = (unsigned long )17;
//                sqlstm.sqhsts[0] = (int) 0;
//                sqlstm.sqindv[0] = ( short *)0;
//                sqlstm.sqinds[0] = (int) 0;
//                sqlstm.sqharm[0] = (unsigned long )0;
//                sqlstm.sqadto[0] = (unsigned short )0;
//                sqlstm.sqtdso[0] = (unsigned short )0;
//                sqlstm.sqhstv[1] = (unsigned char  *)&H_APLOGFMT_MSGID;
//                sqlstm.sqhstl[1] = (unsigned long )6;
//                sqlstm.sqhsts[1] = (int) 0;
//                sqlstm.sqindv[1] = ( short *)0;
//                sqlstm.sqinds[1] = (int) 0;
//                sqlstm.sqharm[1] = (unsigned long )0;
//                sqlstm.sqadto[1] = (unsigned short )0;
//                sqlstm.sqtdso[1] = (unsigned short )0;
//                sqlstm.sqhstv[2] = (unsigned char  *)&H_APLOGFMT_FLG;
//                sqlstm.sqhstl[2] = (unsigned long )4;
//                sqlstm.sqhsts[2] = (int) 0;
//                sqlstm.sqindv[2] = ( short *)0;
//                sqlstm.sqinds[2] = (int) 0;
//                sqlstm.sqharm[2] = (unsigned long )0;
//                sqlstm.sqadto[2] = (unsigned short )0;
//                sqlstm.sqtdso[2] = (unsigned short )0;
//                sqlstm.sqhstv[3] = (unsigned char  *)&H_APLOGFMT_FORMAT;
//                sqlstm.sqhstl[3] = (unsigned long )363;
//                sqlstm.sqhsts[3] = (int) 0;
//                sqlstm.sqindv[3] = ( short *)0;
//                sqlstm.sqinds[3] = (int) 0;
//                sqlstm.sqharm[3] = (unsigned long )0;
//                sqlstm.sqadto[3] = (unsigned short )0;
//                sqlstm.sqtdso[3] = (unsigned short )0;
//                sqlstm.sqphsv = sqlstm.sqhstv;
//                sqlstm.sqphsl = sqlstm.sqhstl;
//                sqlstm.sqphss = sqlstm.sqhsts;
//                sqlstm.sqpind = sqlstm.sqindv;
//                sqlstm.sqpins = sqlstm.sqinds;
//                sqlstm.sqparm = sqlstm.sqharm;
//                sqlstm.sqparc = sqlstm.sqharc;
//                sqlstm.sqpadto = sqlstm.sqadto;
//                sqlstm.sqptdso = sqlstm.sqtdso;
//                sqlcxt(( void **)0, &sqlctx, &sqlstm, &sqlfpn);
//            }

            sqlca.fetch();

            String H_APLOGFMT_PGMID = sqlca.getDataByIndex(1);
            H_APLOGFMT_MSGID.arr = sqlca.getDataByIndex(2);
            String H_APLOGFMT_FLG = sqlca.getDataByIndex(3);
            String H_APLOGFMT_FORMAT = sqlca.getDataByIndex(4);


            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* DBERR */
                if (dbg_getaplogfmt == 1) {
                    C_DbgMsg("C_GetAPLogFmt : %s\n", "FETCH NG");
                }
                /* msgidの１桁目が'9'の場合 */
                if (msgid.charAt(0) == '9') {
                    flg[0] = C_const_APLOG_flg_E;
                    flg[1] = "\0";
                }
                /* msgidの１桁目が'7'の場合 */
                else if (msgid.charAt(0) == '7') {
                    flg[0] = C_const_APLOG_flg_W;
                    flg[1] = "\0";
                } else {
                    strcpy(flg, " ");
                }
                /* 処理を終了する */
                sprintf(format, "フォーマット取得エラー メッセージＩＤ=%s 登録種別=%d", msgid, msgidsbt);
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* NOTFOUND */
                break;
            }

            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : fetch : msgid=[%s] \n", H_APLOGFMT_MSGID.arr);
                C_DbgMsg("C_GetAPLogFmt : fetch : pgmid=[%s] \n", H_APLOGFMT_PGMID);
            }
            /* プログラム名の比較 */
            if (memcmp(Cg_Program_Name, H_APLOGFMT_PGMID, H_APLOGFMT_PGMID.length()) == 0) {
                /* XXXX if (strlen(Cg_Program_Name) < H_APLOGFMT_PGMID.len) { */
                if (strlen(w_sv_pgname) < H_APLOGFMT_PGMID.length()) {
                    /* セーブ用変数に保存する */
                    memset(w_sv_pgname, 0x00, sizeof(w_sv_pgname));
                    memset(w_sv_msgid, 0x00, sizeof(w_sv_msgid));
                    memset(w_sv_flg, 0x00, sizeof(w_sv_flg));
                    memset(w_sv_format, 0x00, sizeof(w_sv_format));
                    w_sv_pgname = strcpy(w_sv_pgname, H_APLOGFMT_PGMID);
                    w_sv_msgid = strcpy(w_sv_msgid, H_APLOGFMT_MSGID.arr);
                    w_sv_flg = strcpy(w_sv_flg, H_APLOGFMT_FLG);
                    w_sv_format = strcpy(w_sv_format, H_APLOGFMT_FORMAT);
                } else {
                    /* 処理なし、次のデータへ */
                }
            } else {
                /* 今回フェッチしたPGM名に?が含まれる場合 */
                if (strstr(H_APLOGFMT_PGMID, "?") != null) {
                    ret = 0;
                    pwrk = H_APLOGFMT_PGMID;
                    memset(w_cmp, 0x00, sizeof(w_cmp));
//                    j = 0;
//                    for (i = 0; i < H_APLOGFMT_PGMID.len; i++) {
//                        if (pwrk != '?'){
//                            ret = 1;
//                            w_cmp[j] = pwrk; /* ?以外の文字をコピーする */
//                            j++;
//                        }
//                        pwrk++;
//                    }
                    pwrk = pwrk.replace("?", "");
                    w_cmp = pwrk;

                    if (ret == 1) {
                        if (dbg_getaplogfmt == 1) {
                            C_DbgMsg("C_GetAPLogFmt : %s\n", "msg is part ???");
                        }
                        /* グローバル変数のプログラム名と？以外の部分を比較する */
                        ret = strncmp(Cg_Program_Name, w_cmp, strlen(w_cmp));
                        if (dbg_getaplogfmt == 1) {
                            C_DbgMsg("C_GetAPLogFmt : compareCg_Program_Name : ret = %d\n", ret);
                        }
                        if (ret == 0) {
                            /* ？以外の部分が一致                       */
                            /* セーブしたＰＧＭ名の桁数と比較する */
                            /* ???? if (strlen(w_sv_pgname) < strlen(w_cmp)) { ???? */
                            if (strlen(w_sv_pgname) < strlen(w_cmp)) {
                                /* セーブ用変数に保存する */
                                memset(w_sv_pgname, 0x00, sizeof(w_sv_pgname));
                                memset(w_sv_msgid, 0x00, sizeof(w_sv_msgid));
                                memset(w_sv_flg, 0x00, sizeof(w_sv_flg));
                                memset(w_sv_format, 0x00, sizeof(w_sv_format));

                                w_sv_pgname = strcpy(w_sv_pgname, w_cmp);
                                w_sv_msgid = strcpy(w_sv_msgid, H_APLOGFMT_MSGID.arr);
                                w_sv_flg = strcpy(w_sv_flg, H_APLOGFMT_FLG);
                                w_sv_format = strcpy(w_sv_format, H_APLOGFMT_FORMAT);
                            } else {
                                /* 処理なし、次のデータへ */
                            }
                        }
                    } else {
                        if (dbg_getaplogfmt == 1) {
                            C_DbgMsg("C_GetAPLogFmt : %s\n", "msg is all ???");
                        }
                        /* 全て?の場合 */
                        /* セーブ用変数に保存する */
                        w_cm_msgid = strcpy(w_cm_msgid, H_APLOGFMT_MSGID.arr);
                        w_cm_flg = strcpy(w_cm_flg, H_APLOGFMT_FLG);
                        w_cm_format = strcpy(w_cm_format, H_APLOGFMT_FORMAT);
                    }
                }
            }
        }
//        sqlca.close();
        sqlcaManager.close("cur_getaplogfmt");
        /* カーソルのクローズ */
        /* EXEC SQL CLOSE cur_getaplogfmt; */
//
//        {
//            struct sqlexd sqlstm;
//            sqlstm.sqlvsn = 13;
//            sqlstm.arrsiz = 4;
//            sqlstm.sqladtp = &sqladt;
//            sqlstm.sqltdsp = &sqltds;
//            sqlstm.iters = (unsigned int  )1;
//            sqlstm.offset = (unsigned int  )237;
//            sqlstm.cud = sqlcud0;
//            sqlstm.sqlest = (unsigned char  *)&sqlca;
//            sqlstm.sqlety = (unsigned short)4352;
//            sqlstm.occurs = (unsigned int  )0;
//            sqlcxt(( void **)0, &sqlctx, &sqlstm, &sqlfpn);
//        }


        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : CLOSe NG(%d) \n", sqlca.sqlcode);
            }
            /* *status = C_const_Stat_DBERR; */
            /* return C_const_NG;            */
        }

        if (strlen(w_sv_msgid) == 0 && strlen(w_cm_msgid) == 0) {
            /* 該当なしの場合 */
            if (dbg_getaplogfmt == 1) {
                C_DbgMsg("C_GetAPLogFmt : %s\n", "msg not found");
            }
            /* msgidの１桁目が'9'の場合 */
            if (msgid.charAt(0) == '9') {
                flg[0] = C_const_APLOG_flg_E;
                flg[1] = "\0";
            }
            /* msgidの１桁目が'7'の場合 */
            else if (msgid.charAt(0) == '7') {
                flg[0] = C_const_APLOG_flg_W;
                flg[1] = "\0";
            } else {
                strcpy(flg, " ");
            }
            /* 処理を終了する */
            sprintf(format, "フォーマット取得エラー メッセージＩＤ=%s 登録種別=%d", msgid, msgidsbt);
            status.arr = C_const_Stat_FMTERR;
            return C_const_NG;
        }

        if (strlen(w_sv_msgid) > 0) {
            /* セーブ用をコピーする */
            w_msgid = strcpy(w_msgid, w_sv_msgid);
            w_flg = strcpy(w_flg, w_sv_flg);
            w_format = strcpy(w_format, w_sv_format);
        } else if (strlen(w_cm_msgid) > 0) {
            /* セーブ用(汎用)をコピーする */
            w_msgid = strcpy(w_msgid, w_cm_msgid);
            w_flg = strcpy(w_flg, w_cm_flg);
            w_format = strcpy(w_format, w_cm_format);
        }
        /* } */

        /* 出力引数の編集 */
        strcpy(format, w_format);
        strcpy(flg, w_flg);

        if (dbg_getaplogfmt == 1) {
            C_DbgMsg("C_GetAPLogFmt : %s\n", "end");
        }
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }


/******************************************************************************/
    /*                                                                            */
    /*      関数名 ：                                                             */
    /*                                                                            */
    /*      【書式】                                                              */
    /*              int  getAplogMsg(char *msgid, char *flg, char *fmt)           */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              固定値フォーマットの検索処理                                  */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char    *       msgid ： (I) メッセージＩＤ                   */
    /*              char    *       flg   ： (O) フラグ                           */
    /*              char    *       fmt   ： (O) フォーマット                     */
    /*                                                                            */
    /*      【戻値】                                                              */
    /*              0       ： 正常                                               */
    /*              1       ： 該当なし                                           */
    /*                                                                            */

    /******************************************************************************/
    static int getAplogMsg(String msgid, String[] flg, String[] fmt) {
        int i;

        for (i = 0; i < APLOG_MSG_MAX; i++) {
            if (memcmp(msgid, C_const_APLOG_MSG[i].C_const_APLOG_msgid, 3) == 0) {
                flg[0] = C_const_APLOG_MSG[i].C_const_APLOG_flg;
                flg[1] = "\0";
                strcpy(fmt, C_const_APLOG_MSG[i].C_const_APLOG_format);
                return C_const_OK;
            }
        }
        return C_const_NG;
    }

    /* 2022/09/14 MCCM初版 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetPrefecturesCode                                        */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetPrefecturesCode( char *address, int *precd, int *status )    */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              都道府県コード取得処理                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char *address      ： 住所                                    */
    /*              int  *precd        ： 都道府県コード                          */
    /*              int  *status       ： ステータス                              */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetPrefecturesCode(String address, IntegerDto precd, IntegerDto status) {
        String env_sid_sd;           /*  SID環境変数名         */
        String env_dblink_sd;        /*  DBLINK環境変数名      */
        StringDto dblink_name_sd = new StringDto(32);    /*  ＳＩＤ（DBリンク用）  */
        StringDto wk_tbl = new StringDto(128);           /*  テーブル名            */
        StringDto sql_buf = new StringDto(4096);         /*  ＳＱＬ文編集用        */

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;
        StringDto wk_precd = new StringDto(40 + 1);        /* コード                 */
        StringDto wk_address = new StringDto(200 * 3 + 1);   /* 名称                   */
//        EXEC SQL END DECLARE SECTION;

        if (dbg_GetPrefecturesCode == 1) {
            C_DbgMsg("C_GetPrefecturesCode : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (address == null || precd == null || status == null) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : %s\n", "prmerr");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : %s", "conncect check NG\n");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 環境変数からＳＩＤを取得する */
        env_sid_sd = getenv(C_CM_ORA_SID_SD);
        if (env_sid_sd == null) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : %s\n", "getenv NG(SID)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* 環境変数からDBLINK名を取得する */
        env_dblink_sd = getenv(C_CM_ORA_DBLINK_SD);
        if (env_dblink_sd == null) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : %s\n", "getenv NG(DBLINK)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* グローバル変数と環境変数を比較してＤＢリンク名を設定 */
        if (strcmp(Cg_ORASID, env_sid_sd) == 0) {
            memset(dblink_name_sd, 0x00, sizeof(dblink_name_sd));
        } else {
            sprintf(dblink_name_sd, "@%s", env_dblink_sd);
        }
        if (dbg_GetPrefecturesCode == 1) {
            C_DbgMsg("C_GetPrefecturesCode : DBリンク名 = [%s]\n", dblink_name_sd);
        }

        /* テーブル名とＤＢリンク名を設定 */
        memset(wk_tbl, 0x00, sizeof(wk_tbl));
        strcpy(wk_tbl, "PSコード情報");
        if (strlen(dblink_name_sd) != 0) {
            strcat(wk_tbl, dblink_name_sd);
        }

        /* ＳＱＬ用のホスト変数を編集する */
        /* 住所 */
        strcpy(wk_address, address);

        /* ＳＱＬを生成する */
        memset(sql_buf, 0x00, sizeof(sql_buf));
        strcpy(sql_buf, " SELECT RPAD(コード,LENGTH(コード)) AS コード FROM ");
        strcat(sql_buf, wk_tbl);
        strcat(sql_buf, " WHERE コード種別 = 3 ");
        strcat(sql_buf, " AND ? like CONCAT(NULLIF(RTRIM(名称),''),'%') ");
        if (dbg_GetPrefecturesCode == 1) {
            C_DbgMsg("C_GetPrefecturesCode : sql_buf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);

        SqlstmDto sqlca = sqlcaManager.get("sql_getprefecturescode");
        sqlca.sql = WRKSQL;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_getprefecturescode from :WRKSQL;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        sqlca.declare();
//        EXEC SQL DECLARE cur_getprefecturescode cursor FOR sql_getprefecturescode;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* select文を発行する */
        /* カーソルのオープン */
//        EXEC SQL OPEN cur_getprefecturescode using :wk_address;
        sqlca.open(wk_address);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* カーソルのフェッチ */
//        EXEC SQL FETCH cur_getprefecturescode into :wk_precd;
        sqlca.fetchInto(wk_precd);
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* ２件以上データあるか確認 */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_GetPrefecturesCode == 1) {
                    C_DbgMsg("C_GetPrefecturesCode : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
            /* 都道府県コード取得 */
            precd.arr = atoi(wk_precd);
        } else {
            /* 取得件数が０件の場合 */
            precd.arr = 00;
        }

        sqlcaManager.close(sqlca);
        /* カーソルのクローズ */
//        EXEC SQL CLOSE cur_getprefecturescode;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefecturesCode == 1) {
                C_DbgMsg("C_GetPrefecturesCode : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
        }

        if (dbg_GetPrefecturesCode == 1) {
            C_DbgMsg("C_GetPrefecturesCode : 都道府県コード = [%d]\n", precd);
            C_DbgMsg("C_GetPrefecturesCode : %s\n", "end");
        }

        /* 正常終了 */
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetPrefectures                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetPrefectures( int precd, char *prefectures, int *status  )    */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              都道府県名取得処理処理                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int  precd         ： 都道府県コード                          */
    /*              char *prefectures  ： 都道府県名                              */
    /*              int  *status       ： ステータス                              */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetPrefectures(int precd, StringDto prefectures, IntegerDto status) {
        String env_sid_sd;           /*  SID環境変数名         */
        String env_dblink_sd;        /*  DBLINK環境変数名      */
        StringDto dblink_name_sd = new StringDto(32);    /*  ＳＩＤ（DBリンク用）  */
        StringDto wk_tbl = new StringDto(128);           /*  テーブル名            */
        StringDto sql_buf = new StringDto(4096);         /*  ＳＱＬ文編集用        */

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;
        int wk_precd;              /* コード                 */
        StringDto wk_prefectures = new StringDto(40 * 3 + 1);/* 名称                   */
//        EXEC SQL END DECLARE SECTION;

        if (dbg_GetPrefectures == 1) {
            C_DbgMsg("C_GetPrefectures : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (prefectures == null || status == null) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : %s\n", "prmerr");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* 引数のチェックを行う */
        if (precd == 0) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : %s\n", "prmerr");
            }
            status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : %s", "conncect check NG\n");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 環境変数からＳＩＤを取得する */
        env_sid_sd = getenv(C_CM_ORA_SID_SD);
        if (env_sid_sd == null) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : %s\n", "getenv NG(SID)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* 環境変数からDBLINK名を取得する */
        env_dblink_sd = getenv(C_CM_ORA_DBLINK_SD);
        if (env_dblink_sd == null) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : %s\n", "getenv NG(DBLINK)");
            }
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* グローバル変数と環境変数を比較してＤＢリンク名を設定 */
        if (strcmp(Cg_ORASID, env_sid_sd) == 0) {
            memset(dblink_name_sd, 0x00, sizeof(dblink_name_sd));
        } else {
            sprintf(dblink_name_sd, "@%s", env_dblink_sd);
        }
        if (dbg_GetPrefectures == 1) {
            C_DbgMsg("C_GetPrefectures : DBリンク名 = [%s]\n", dblink_name_sd);
        }

        /* テーブル名とＤＢリンク名を設定 */
        memset(wk_tbl, 0x00, sizeof(wk_tbl));
        strcpy(wk_tbl, "PSコード情報");
        if (strlen(dblink_name_sd) != 0) {
            strcat(wk_tbl, dblink_name_sd);
        }

        /* ＳＱＬ用のホスト変数を編集する */
        /* 都道府県コード */
        wk_precd = precd;

        /* ＳＱＬを生成する */
        memset(sql_buf, 0x00, sizeof(sql_buf));
        strcpy(sql_buf, " SELECT nullif(rtrim(名称),'') FROM ");
        strcat(sql_buf, wk_tbl);
        strcat(sql_buf, " WHERE コード種別 = 3 ");
        strcat(sql_buf, " AND コード = ? ");
        if (dbg_GetPrefectures == 1) {
            C_DbgMsg("C_GetPrefectures : sql_buf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);

        SqlstmDto sqlca = sqlcaManager.get("sql_getprefectures");
        sqlca.sql = WRKSQL;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_getprefectures from :WRKSQL;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        sqlca.declare();
//        EXEC SQL DECLARE cur_getprefectures cursor for sql_getprefectures;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : DECLARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* select文を発行する */
        /* カーソルのオープン */
        sqlca.open(wk_precd);
//        EXEC SQL OPEN cur_getprefectures using :wk_precd;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : OPEN : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* カーソルのフェッチ */
        memset(wk_prefectures, 0x00, sizeof(wk_prefectures));
//        EXEC SQL FETCH cur_getprefectures into:
//        wk_prefectures;
        sqlca.fetchInto(wk_prefectures);

        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* ２件以上データあるか確認 */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (dbg_GetPrefectures == 1) {
                    C_DbgMsg("C_GetPrefectures : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
                }
                status.arr = C_const_Stat_DBERR;
                return C_const_NG;
            }
            /* 都道府県名取得処理 */
            strcpy(prefectures, wk_prefectures);
            addr_rtrim(prefectures);
        } else {
            /* 取得件数が０件の場合 */
            memset(prefectures, 0x00, strlen(prefectures));
        }

        /* カーソルのクローズ */
        sqlcaManager.close(sqlca);
//        EXEC SQL CLOSE cur_getprefectures;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (dbg_GetPrefectures == 1) {
                C_DbgMsg("C_GetPrefectures : CLOSE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
        }

        if (dbg_GetPrefectures == 1) {
            C_DbgMsg("C_GetPrefectures : 都道府県名 = [%s]\n", prefectures);
            C_DbgMsg("C_GetPrefectures : %s\n", "end");
        }

        /* 正常終了 */
        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

    @Override
    public int C_GetPidCDV12(StringDto Pid) {
        String wk_PidCD = null;                    /* 会員コード                   */
        int wk_in_PidCD_len = 12;            /* 会員コード桁数               */
        int wk_Modulus;                      /* モジュラス                   */
        String wk_Weight = null;                 /* ウエイト                     */
        String wk_W1 = null;
        String wk_P1 = null;
        int wk_Ans;
        int wk_Sum;
        int wk_CD;
        int i;

        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (Pid == null) {
            if (dbg_GetPidCDV12 == 1) {
                C_DbgMsg("C_GetPidCDV12 : %s\n", "NO PRMERR");
            }
            return C_const_PARAMERR;
        }
//        memset(wk_PidCD, 0x00, sizeof(wk_PidCD));
        wk_PidCD = memcpy(wk_PidCD, Pid.arr, wk_in_PidCD_len);
        if (strlen(wk_PidCD) != wk_in_PidCD_len) {
            if (dbg_GetPidCDV12 == 1) {
                C_DbgMsg("C_GetPidCDV12 : %s\n", "PRM LENG ERR");
            }
            return C_const_PARAMERR;
        }
        wk_Sum = 0;
        wk_CD = 0;
        /************************/
        /* 主処理               */
        /************************/
        /* モジュラスとウエイトを設定 */
        wk_Modulus = 10;
//        wk_Weight=  sprintf(wk_Weight, "131313131313");
        wk_Weight = "131313131313";
        for (i = 0; i < wk_in_PidCD_len; i++) {
//            memset(wk_W1, 0x00, sizeof(wk_W1));
//            memset(wk_P1, 0x00, sizeof(wk_P1));
            wk_W1 = memcpy(wk_W1, String.valueOf(wk_Weight.charAt(i)), 1);
            wk_P1 = memcpy(wk_P1, String.valueOf(wk_PidCD.charAt(i)), 1);
            if (strcmp(wk_P1, "0") < 0
                    || strcmp(wk_P1, "9") > 0) {
                if (dbg_GetTanCDV == 1) {
                    C_DbgMsg("C_GetPidCDV12 : %s\n", "PRM NUMERIC ERR");
                }
                return C_const_PARAMERR;
            }
            wk_Ans = (atoi(wk_W1) * atoi(wk_P1)) % 10;
            wk_Sum += wk_Ans;
        }
        wk_CD = (wk_Modulus - (wk_Sum % wk_Modulus)) % 10;
        if (dbg_GetPidCDV12 == 1) {
            C_DbgMsg("C_GetPidCDV12 : CD=[%d]\n", wk_CD);
        }

        /* 15ケタ目にチェックディジットを設定する */
        Pid.arr += wk_CD + '0';
        if (dbg_GetPidCDV12 == 1) {
            C_DbgMsg("C_GetPidCDV12 : return PidCD[%s]\n", Pid);
        }

        return C_const_OK;
    }

    @Override
    public int C_CorrectMemberNo(StringDto Pid) {
        String wk_PidCD_in = null;                    /* 会員コード                   */
        int wk_in_PidCD_len;                    /* 会員コード桁数               */
        StringDto wk_PidCD_out = new StringDto();                      /* 会員コード                   */
        StringDto wk_PidCD = new StringDto();                        /* ワーク用会員コード           */
        String in_msg_id = null;                          /* APログメッセージID           */
        IntegerDto out_status = new IntegerDto();                         /* フォーマット取得結果         */
        String[] out_flg = new String[2];                            /* APログフラグ                 */
        String[] out_format = new String[2];               /* APログフォーマット           */
        int rtn_cd;

        /************************/
        /* 初期処理             */
        /************************/
        /* 引数のチェックを行う */
        if (Pid == null) {
            if (dbg_CorrectMemberNo == 1) {
                C_DbgMsg("C_CorrectMemberNo : %s\n", "NO PRMERR");
            }
            return C_const_PARAMERR;
        }
//        memset(wk_PidCD_in, 0x00, sizeof(wk_PidCD_in));
        wk_PidCD_in = strcpy(wk_PidCD_in, Pid.arr);

        wk_in_PidCD_len = 0;
        wk_in_PidCD_len = strlen(wk_PidCD_in);

        /************************/
        /* 主処理               */
        /************************/
        /* 初期設定 */
//        memset(wk_PidCD_out, 0x00, sizeof(wk_PidCD_out));
//        memset(wk_PidCD, 0x00, sizeof(wk_PidCD));
        /* 桁数により補正処理を行う */
        switch (wk_in_PidCD_len) {
            case 16:
                if (strncmp(wk_PidCD_in, "9881", 4) == 0) {
                    strcpy(wk_PidCD, wk_PidCD_in);
                    /* CDV処理 */
                    rtn_cd = C_GetPidCDV12(wk_PidCD);
                    if (rtn_cd != C_const_OK) {
                        /*-------------------------------------*/
                        /*  APログフォーマット取得処理         */
                        /*-------------------------------------*/
                        rtn_cd = C_const_OK;
//                        memset(in_msg_id, 0x00, sizeof(in_msg_id));
                        in_msg_id = strcpy(in_msg_id, "902");
                        rtn_cd = C_GetAPLogFmt(in_msg_id, 0, null,
                                out_flg, out_format, out_status);

                        /*-------------------------------------*/
                        /*  APログ出力処理                     */
                        /*-------------------------------------*/
                        rtn_cd = C_APLogWrite(in_msg_id, out_format, out_flg,
                                sqlca.sqlcode, "dual  [dec FETCH]", 0, 0, 0, 0);

                        return C_const_NG;
                    }
                    sprintf(wk_PidCD_out, "%s%s", "9881", wk_PidCD);
                } else if (strncmp(wk_PidCD_in, "0002", 4) == 0) {
                    strcpy(wk_PidCD_out, wk_PidCD_in + 3);
                } else {
                    strcpy(wk_PidCD_out, wk_PidCD_in);
                }
                break;

            case 15:
                strcpy(wk_PidCD_out, wk_PidCD_in);
                break;

            case 20:
                strcpy(wk_PidCD_out, wk_PidCD_in);
                break;

            case 13:
                if (strncmp(wk_PidCD_in, "271", 3) == 0) {
                    sprintf(wk_PidCD_out, "%s%s", "9881", wk_PidCD_in);
                } else if (strncmp(wk_PidCD_in, "273", 3) == 0) {
                    sprintf(wk_PidCD_out, "%s%s", "9881", wk_PidCD_in);
                } else if (strncmp(wk_PidCD_in, "2", 1) == 0) {
                    strcpy(wk_PidCD_out, wk_PidCD_in);
                } else {
                    strcpy(wk_PidCD_out, wk_PidCD_in);
                }
                break;

            default:
                strcpy(wk_PidCD_out, wk_PidCD_in);
                break;

        }

        /* 結果をセット */
        strcpy(Pid, wk_PidCD_out);

        if (dbg_CorrectMemberNo == 1) {
            C_DbgMsg("C_CorrectMemberNo : return PidCD[%s]\n", Pid);
        }
        return C_const_OK;
    }


    public MainResultDto exit(int code) {
        resultData.exitCode = code;
        return resultData;
    }

    public MainResultDto printf(String str1, Object... str2) {
        resetData(str2);
        resultData.result = String.format(str1, str2);
        System.out.printf(resultData.result);
        return resultData;
    }

    public MainResultDto functionCode(int code) {
        resultData.functionCode = code;
        resultData.exitCode = code;
        return resultData;
    }

    public void fflush(FileStatusDto statusDto) {
        statusDto.flush();
    }

    public int fclose(FileStatusDto statusDto) {
        if (statusDto != null) {
            return statusDto.close();
        }
        return C_const_OK;
    }


    public int ferror(FileStatusDto statusDto) {
        return StringUtils.isEmpty(statusDto.error) ? C_const_OK : C_const_NG;
    }

    public int fwrite(StringDto wk_out_buff, int wk_out_buff_len, int count, FileStatusDto dto) {
        return dto.write(wk_out_buff.arr, count);
    }

    public int fwrite(String wk_out_buff, int wk_out_buff_len, int count, FileStatusDto dto) {
        return dto.write(wk_out_buff, count);
    }

    public MainResultDto main(ParamsExecuteDto paramsExecuteDto) {
        String[] strings = paramsExecuteDto.doInput();
        if (strings[0] == null) {
            strings[0] = toLowerCaseFirst(this.getClass().getSimpleName().substring(0, 9));
        }
        //init params
        Cg_ORASID = null;
        Cg_ORAUSR = null;
        Cg_ORAPWD = null;
        return main(strings.length, strings);
    }

    @Override
    public MainResultDto main(int argc, String[] args) {
        return null;
    }

    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        return cmBTfuncB.APLOG_WT(msgid, msgidsbt, dbkbn, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
        int ret;
        ret = APLOG_WT("903", 0, null, param1, param2,
                param3, 0, 0, 0);

        return ret;
    }

    public int BT_Rtrim(ItemDto strp, int size) {
        StringDto stringDto = strp.strDto();
        int res = cmBTfuncB.BT_Rtrim(stringDto, size);
        strp.arr = stringDto.arr;
        return res;
    }

    public int BT_Rtrim(StringDto strp, int size) {
        return cmBTfuncB.BT_Rtrim(strp, size);
    }

    public String setChartAt(String src, int index, char c) {
        String s1 = src.substring(0, Math.min(index, src.length()));
        String s2 = src.substring(Math.min(src.length(), index + 1));
        return s1 + c + s2;
    }
}
