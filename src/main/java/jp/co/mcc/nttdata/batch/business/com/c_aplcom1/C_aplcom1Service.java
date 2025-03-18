package jp.co.mcc.nttdata.batch.business.com.c_aplcom1;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * 共通関数ライブラリ（cm_libxxxx.pc）ヘッダファイル
 */
public abstract class C_aplcom1Service extends CLanguageFunction {
    public static boolean DBG_LOG = true;
    /*-----------------------------------------------------------------------------*/
    /*	グローバル変数                                                         */
    /*-----------------------------------------------------------------------------*/
    public static String Cg_Program_Name_Ver = null;        /* ﾊﾞｰｼﾞｮﾝ付きﾌﾟﾛｸﾞﾗﾑ名        */
    public static String Cg_Program_Name = null;            /* ﾊﾞｰｼﾞｮﾝ無しﾌﾟﾛｸﾞﾗﾑ名        */
    public static String Cg_ORASID = null;                /* ORACLE SID                  */
    public static String Cg_ORAUSR = null;                /* ORACLE 接続ユーザID         */
    public static String Cg_ORAPWD = null;                /* ORACLE 接続パスワード       */

    /*----------------------------------C_aplcom1.h------------------------------------*/
    /*-----------------------------------------------------------------------------*/
    /*	定数定義                                                               */
    /*-----------------------------------------------------------------------------*/
    public static final int C_const_APOK = 10;        /* プログラム戻り値（正常）    */
    public static final int C_const_APNG = 99;        /* プログラム戻り値（異常）    */
    public static final int C_const_OK = 0;    /* 関数戻り値（正常）          */
    public static final int C_const_NG = 1;    /* 関数戻り値（異常）          */
    public static final int C_const_NOTEXISTS = -1;        /* 関数戻り値（顧客情報なし）  */
    public static final int C_const_DUPL = -2;        /* 関数戻り値（重複）          */
    public static final int C_const_PARAMERR = 2;        /* 関数戻り値（ﾊﾟﾗﾒｰﾀｴﾗｰ）     */
    public static final int C_const_Stat_OK = 0;        /* 関数ｽﾃｰﾀｽ（正常）           */
    public static final int C_const_Stat_PRMERR = 1;        /* 関数ｽﾃｰﾀｽ（ﾊﾟﾗﾒｰﾀｴﾗｰ）      */
    public static final int C_const_Stat_ENVERR = 2;        /* 関数ｽﾃｰﾀｽ（環境変数ｴﾗｰ）    */
    public static final int C_const_Stat_DBERR = 3;        /* 関数ｽﾃｰﾀｽ（DBｴﾗｰ）          */
    public static final int C_const_Stat_FMTERR = 4;        /* 関数ｽﾃｰﾀｽ（ﾌｫｰﾏｯﾄｴﾗｰ）      */
    public static final int C_const_Stat_ELSERR = 5;        /* 関数ｽﾃｰﾀｽ（その他ｴﾗｰ）      */
    public static final int C_const_Stat_NOTFND = 9;        /* 関数ｽﾃｰﾀｽ（該当ﾃﾞｰﾀなし）   */
    public static final int C_const_Ora_OK = 0;        /* オラクル処理ＯＫ            */
    public static final int C_const_Ora_NOTFOUND = 1403;        /* オラクル処理該当データなし  */
    public static final int C_const_Ora_DUPL = 1;              /* オラクル（重複）            */
    public static final int C_const_MsgMaxLen = 4096;        /* APログ出力最大サイズ        */
    public static final int C_const_SQLMaxLen = 4096;        /* SQL文文字列最大サイズ       */

    /***  APログ                                                                ***/
    /*  フラグ定義                                                                */
    public static final String C_const_APLOG_flg_E = "@";            /*  エラー               */
    public static final String C_const_APLOG_flg_N = " ";            /*  情報（通常）         */
    public static final String C_const_APLOG_flg_L = "L";            /*  情報（重要度低）     */
    public static final String C_const_APLOG_flg_W = "W";            /*  警告                 */

    public static final String DEFAULT_ALTER_TIME_SQL="alter session set nls_date_format = 'YYYY-MM-DD HH24:MI:SS'";

    /* 環境変数                                                                    */
    public static final String C_TRACE_FILE_DIR = "CM_TRACEBAT";          /* ﾃﾞﾊﾞｯｸﾞﾄﾚｰｽﾌｧｲﾙの格納先     */
    public static final String CM_APWORK_DATE = "CM_APWORK_DATE";
    public static final String CONNECT_SD = "CONNECT_SD";
    public static final String CONNECT_USR = "CONNECT_USR";
    public static final String CONNECT_DB = "CONNECT_DB";
    public static final String CM_APWORK = "CM_APWORK";
    public static final String CM_MYPRGID = "CM_MYPRGID";
    public static final String CM_MYPRGNAME = "CM_MYPRGNAME";
    public static final String CM_APJCL = "CM_APJCL";
    public static final String CM_SERVER_ID = "CM_SERVER_ID";
    public static final String CM_APBIN = "CM_APBIN";
    public static final String CM_MAILTEXT = "CM_MAILTEXT";
    public static final String CM_JAVA_APBIN = "CM_JAVA_APBIN";
    public static final String CM_APPARAM = "CM_APPARAM";
    public static final String CM_APSQL = "CM_APSQL";
    public static final String CM_APLOG = "CM_APLOG";
    public static final String CM_CRMRENKEI = "CM_CRMRENKEI";
    public static final String CM_FILEWATSND = "CM_FILEWATSND";
    public static final String CM_FILENOWSND = "CM_FILENOWSND";
    public static final String CM_FILEAFTSND = "CM_FILEAFTSND";
    public static final String CM_FILE_RCV = "CM_FILE_RCV";
    public static final String CM_FILEWATRCV = "CM_FILEWATRCV";
    public static final String CM_FILENOWRCV = "CM_FILENOWRCV";
    public static final String CM_FILEAFTRCV = "CM_FILEAFTRCV";
    public static final String CM_TRACELOG = "CM_TRACELOG";
    public static final String CM_JOBRESULTLOG = "CM_JOBRESULTLOG";
    public static final String CM_TRACEBAT = "CM_TRACEBAT";
    public static final String CM_TODAY = "CM_TODAY";
    public static final String CM_TRACEJAVABAT = "CM_TRACEJAVABAT";
    public static final String WK_PWD = "WK_PWD";
    public static final String CM_SA_NO = "CM_SA_NO";
    public static final String CM_USR_MD = "CM_USR_MD";
    public static final String CM_USR_SD = "CM_USR_SD";
    public static final String CM_USR_BD = "CM_USR_BD";
    public static final String CM_USR_HD = "CM_USR_HD";
    public static final String CM_PSW_SD = "CM_PSW_SD";
    public static final String CM_PSW_BD = "CM_PSW_BD";
    public static final String CM_PSW_HD = "CM_PSW_HD";
    public static final String CM_PSW_MD = "CM_PSW_MD";
    public static final String CM_ORA_SID_SD = "CM_ORA_SID_SD";
    public static final String CM_ORA_SID_BD = "CM_ORA_SID_BD";
    public static final String CM_ORA_SID_HD = "CM_ORA_SID_HD";
    public static final String CM_ORA_SID_MD = "CM_ORA_SID_MD";
    public static final String CM_POS_SID_HD = "CM_POS_SID_HD";
    public static final String CM_POS_SID_SD = "CM_POS_SID_SD";
    public static final String CM_APRESULT = "CM_APRESULT";
    public static final String CM_BKUPRESULT = "CM_BKUPRESULT";
    public static final String CM_BKUPRCV = "CM_BKUPRCV";
    public static final String NLS_LANG = "NLS_LANG";

    public static final String CM_PCRENKEI_KOJINARI = "CM_PCRENKEI_KOJINARI";
    public static final String CM_PCRENKEI_KOJINNASHI = "CM_PCRENKEI_KOJINNASHI";

    public static final String BKUP_IFS = "BKUP_IFS";
    public static final String CM_WEBBAT = "CM_WEBBAT";
    public static final String CLASSPATH = "CLASSPATH";
    public static final String  CM_COUPONLIST= "CM_COUPONLIST";

    private static File file = null;
    private static PrintWriter printWriter = null;
    private static String startname = null;
    private static String nextLine = "\n";

    public static void setStartname(String startname) {
        C_aplcom1Service.startname = startname;
    }


    public static synchronized void C_DbgInit(String startname) {
        if (StringUtils.isNotEmpty(startname)) {
//            if (file == null) {
            if (printWriter != null) {
                printWriter.close();
                printWriter = null;
            }
            file = new File(startname);
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
        } catch (IOException e) {
            file = null;
        }
//        }
    }

    public static void C_DbgStart(String startname) {
        if (null != file && file.exists()) {
//            try (FileWriter fileWriter = new FileWriter(file, true);
//                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//                 PrintWriter printWriter = new PrintWriter(bufferedWriter);) {
            printWriter.printf("%s  トレース開始(%s)", DateUtil.nowDateFormat("yyyy/MM/dd HH:mm:ss.SSS"), startname);
            printWriter.printf(nextLine);
            printWriter.flush();

//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public static void C_DbgEnd(String Endname, int Result, int Rtncd, int Errorcd) {
        if (null != file && file.exists()) {

            int svError = Errorcd;
            printWriter.printf("%s  トレース終了(%s)", DateUtil.nowDateFormat("yyyy/MM/dd HH:mm:ss.SSS"), Endname);
            printWriter.printf(nextLine);
            printWriter.printf("   終了コード＝(%d)", Result);
            printWriter.printf(nextLine);
            printWriter.flush();
            if (Result != 0) {
                printWriter.printf("   関数戻り値＝(%d) ： Status＝(%d)", Rtncd, svError);
                printWriter.printf(nextLine);
                printWriter.flush();
            }
//            printWriter.close();

        }
    }

    public static void C_DbgMsg(String Format, Object Variable) {
        if (null != file && file.exists()) {
//            try (FileWriter fileWriter = new FileWriter(file, true);
//                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//                 PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
            printWriter.printf("%s  ", DateUtil.nowDateFormat("yyyy/MM/dd HH:mm:ss.SSS"));
            if (Variable instanceof StringDto) {
                printWriter.printf(Format, ((StringDto) Variable).arr == null ? "" : ((StringDto) Variable).arr);
            } else if (Variable instanceof IntegerDto) {
                printWriter.printf(Format, ((IntegerDto) Variable).arr);
            } else if (Variable instanceof String[]) {
                printWriter.printf(Format, ((String[]) Variable)[0] == null ? "" : ((String[]) Variable)[0]);
            } else if (Variable instanceof ItemDto) {
                if (Format.indexOf("%f") >= 0 || Format.indexOf("%10.0f") >= 0) {
                    printWriter.printf(Format, ((ItemDto) Variable).floatVal());
                } else if (Format.indexOf("%d") >= 0) {
                    printWriter.printf(Format, ((ItemDto) Variable).intVal());
                } else {
                    printWriter.printf(Format, ((ItemDto) Variable).strVal());
                }


            } else {
                printWriter.printf(Format, Variable == null ? "" : Variable);
            }
            if (!Format.endsWith(nextLine)) {
                printWriter.printf(nextLine);
            }
            printWriter.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    public abstract int C_GetPgname(String[] argv);

    public abstract int C_OraDBConnect(String dbkbn, IntegerDto status);   /* ＤＢ接続処理               */

    public abstract int C_KdataLock(StringDto uid, String locksbt, IntegerDto status); /* 顧客データロック処理 */

    public abstract int C_APLogWrite(String msgid, String[] format, String[] flg, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6); /* ＡＰログ出力処理 */

    public abstract int C_GetStoreCorpId(int store, int date, int corpid, int status); /* 処理企業コード取得処理 */

    public abstract int C_GetPidCorpId(String pid, int corpid, int status); /* 会員企業コード取得処理 */

    public abstract int C_GetBatDate(int mode, StringDto date, IntegerDto status); /* バッチ処理日付取得処理 */

    public abstract int C_GetSysDateTime(StringDto date, StringDto time);   /* 日付時刻取得処理           */

    public abstract int C_ConvHalf2Full(StringDto halfstr, StringDto fullstr);   /* 半角→全角変換処理    */

    public abstract int C_ConvFull2Half(StringDto fullstr, StringDto halfstr);   /* 全角→半角変換処理    */

    public abstract int C_ConvSJ2UT(StringDto sjisstr, int sjislen, StringDto utf8str, IntegerDto utf8len); /* SJIS→UTF8変換処理 */

    public abstract int C_ConvUT2SJ(StringDto utf8str, int utf8len, StringDto sjisstr, IntegerDto sjislen); /* UTF8→SJIS変換処理 */

    public abstract int C_StrcntUtf8(StringDto utf8str, int utf8len, IntegerDto cnt); /* UTF8文字列桁数カウント */

    public abstract int C_SetFullSpace(int cnt, int cdkbn, StringDto outbuf, IntegerDto outlen); /* 全角スペースセット処理 */

    public abstract int C_GetPidCDV(String pid); /* 会員コード、顧客コード CDV取得処理 */

    public abstract int C_ConvTelNo(StringDto telno_bef, int telno_len, StringDto telno_aft); /* 電話番号ハイフンなし変換 */

    public abstract int C_CountAge(int birth_yyyy, int birth_mm, int birth_dd, int base_yyyy, int base_mm, int base_dd, IntegerDto age); /* 年齢計算処理 */

    public abstract int C_GetPostBarCode(StringDto address, StringDto postcd, StringDto custbarcd, IntegerDto status); /* 郵便番号コード変換 */

    public abstract int C_GetSaveKkn(String tblgrp, String base_date, StringDto out_yyyymmdd, StringDto out_yyyymm, StringDto out_yyyy, StringDto out_yyyymmdd2, IntegerDto status); /* 保存期間取得処理 */

    public abstract int C_StartBatDbg(int argc, String[] argv);       /* バッチデバッグ開始処理     */

    public int C_EndBatDbg() {


        if (file != null) {
            /* ファイルをオープンしている場合 */
//            if (file =) {
            /* クローズエラー */

//            }
            /* ファイルポインタもクリアする */
//            FP_DEBUG_FILE = NULL;
            if (printWriter != null) {
                printWriter.close();
                if (printWriter.checkError()) {
                    printWriter = null;
                    return C_const_NG;
                }
                printWriter = null;
            }
            file = null;
        }

        return C_const_OK;
    }                        /* バッチデバッグ終了処理     */

    public abstract int C_GetAPLogFmt(String msgid, int msgidsbt, String dbkbn, String[] flg, String[] format, IntegerDto status); /* APログフォーマット取得 */

    public abstract int C_GetPrefecturesCode(String address, IntegerDto precd, IntegerDto status); /* 都道府県コード取得処理 */

    public abstract int C_GetPrefectures(int precd, StringDto prefectures, IntegerDto status); /* 都道府県名取得処理処理*/

    public abstract int C_GetPidCDV12(StringDto Pid); /* 会員コード、顧客コード CDV取得処理 */

    public abstract int C_CorrectMemberNo(StringDto Pid); /* 会員番号補正処理 */
}
