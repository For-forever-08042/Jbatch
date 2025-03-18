package jp.co.mcc.nttdata.batch.business.job.cmABdbimS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------
 * 名称　　　　　：　テーブルImport
 * プログラムID　：　cmABdbimS
 * <p>
 * 【処理概要】
 * 引数に指定したテーブルのImportを行う。
 * <p>
 * 【引数説明】
 * -T  テーブル名          :　対象テーブル名
 * -P  テーブル名可変部指定:　対象テーブル名に付加する可変部の設定パターン
 * 　下記のパターンで自動付与（任意）
 * 　DD ： YYYYMMDD形式でバッチ処理日当日を付加
 * 　DP ： YYYYMMDD形式でバッチ処理日前日を付加
 * 　DN ： YYYYMMDD形式でバッチ処理日翌日を付加
 * 　MD ： YYYYMM形式でバッチ処理日当月を付加
 * 　MP ： YYYYMM形式でバッチ処理日前月を付加
 * 　MN ： YYYYMM形式でバッチ処理日翌月を付加
 * 　YD ： YYYY形式でバッチ処理日当年を付加
 * 　YP ： YYYY形式でバッチ処理日前年を付加
 * 　YN ： YYYY形式でバッチ処理日翌年を付加
 * -D  接続DB              :　接続対象のDB
 * -F  入力dmpファイル名   :　入力dmpファイル名文字列（半角英数字）
 * テーブル名可変部指定ありの場合、該当の
 * 設定パターンの文字列もファイル名に付与する
 * （例）out_file -> out_file_20110101_141235.dmp
 * 【戻り値】
 * 10　　 ：　正常
 * 49　　 ：　異常
 * ------------------------------------------------------------------------------
 * 稼働環境
 * Red Hat Enterprise Linux 6
 * <p>
 * 改定履歴
 * 1.00 :   2013/01/30 SSI.本田  ： 初版
 * ------------------------------------------------------------------------------
 * $Id:$
 * ------------------------------------------------------------------------------
 * Copyright (C) 2012 NTT DATA CORPORATION
 * -----------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmABdbimSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;


    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    private final static String ARG_OPT1 = "-T";  //    ###  テーブル名
    private final static String ARG_OPT2 = "-D";  //    ###  接続DB
    private final static String ARG_OPT3 = "-F";  //    ###  入力dmpファイル名文字列
    private final static String ARG_OPT4 = "-P";  //    ###  対象テーブル名に付加する可変部の設定パターン

    //  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;
    String OPTION3 = null;
    String OPTION4 = null;

    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ

        // プログラムIDを環境変数に設定
//        String CM_MYPRGID = args[0].substring(0, 9);
//        args[0]= CM_MYPRGID;
//        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);
        CM_MYPRGNAME = "テーブルImport";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));


        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (OPTION1 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 1) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION1 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT2:
                    if (OPTION2 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 1) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION2 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT3:
                    if (OPTION3 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 1) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION3 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT4:
                    if (OPTION4 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 1) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION4 = args[i + 1];
                    i++;
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + ARG_ALL + "]", FW);
                    return Rtn_NG;

            }
        }
        //必須引数チェック
        if (OPTION1 == null || OPTION2 == null) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }


        //接続先チェック
        if (!checkConnectionType(OPTION2)) {
            return Rtn_NG;
        }

        if (checkDBSize()) {
            APLOG_WT("引数エラー  （起動環境と接続先DBの不整合）\n該当環境では指定不可の接続先DBが指定されている  [" + OPTION2 + "]", FW);
            return Rtn_NG;
        }

        if (checkUnicodeLength(OPTION3)) {
            APLOG_WT("引数エラー　（出力dmpファイル名不正[" + OPTION3 + "]）\\n出力dmpファイル名は半角英数字で指定してください  [" + OPTION3 + "]", FW);
            return Rtn_NG;
        }
        if (OPTION3 == null) {
            OPTION3 = "";
        }

        String ADD_VALUE = "";
        String BAT_YYYYMMDD_D = null;
        String ADD_VALUE_Y = null;

        String ADD_VALUE_M = null;
        if (OPTION4 != null) {
            BAT_YYYYMMDD_D = cmABgdatBServiceImpl.main(getExecuteParam()).result;


            // 可変部の設定パターンチェック
            switch (OPTION4) {
                case "0":
                    ADD_VALUE = "";
                    break;
                case "DD":
                    ADD_VALUE = BAT_YYYYMMDD_D;
                    break;
                case "DP":
                    ADD_VALUE = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY")).result;
                    break;
                case "DN":
                    ADD_VALUE = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DT")).result;
                    break;
                case "MD":
                    ADD_VALUE = BAT_YYYYMMDD_D.substring(0, 6);
                    break;
                case "MP":
                    ADD_VALUE_Y = BAT_YYYYMMDD_D.substring(0, 4);
                    ADD_VALUE_M = BAT_YYYYMMDD_D.substring(4, 6);
                    if ("01".equals(ADD_VALUE_M)) {
                        ADD_VALUE_Y = String.valueOf(Integer.valueOf(ADD_VALUE_Y) - 1);
                        ADD_VALUE_M = "12";
                    } else {
                        ADD_VALUE_M = String.valueOf(Integer.valueOf(ADD_VALUE_M) - 1);
                    }
                    ADD_VALUE = ADD_VALUE_Y + ADD_VALUE_M;
                    break;
                case "MN":
                    ADD_VALUE_Y = BAT_YYYYMMDD_D.substring(0, 4);
                    ADD_VALUE_M = BAT_YYYYMMDD_D.substring(4, 6);
                    if ("12".equals(ADD_VALUE_M)) {
                        ADD_VALUE_Y = String.valueOf(Integer.valueOf(ADD_VALUE_Y) + 1);
                        ADD_VALUE_M = "01";
                    } else {
                        ADD_VALUE_M = String.valueOf(Integer.valueOf(ADD_VALUE_M) + 1);
                    }
                    ADD_VALUE = ADD_VALUE_Y + ADD_VALUE_M;
                    break;
                case "YD":
                    ADD_VALUE = BAT_YYYYMMDD_D.substring(0, 4);
                    break;
                case "YP":
                    ADD_VALUE_Y = BAT_YYYYMMDD_D.substring(0, 4);
                    ADD_VALUE = String.valueOf(Integer.valueOf(ADD_VALUE_Y) - 1);
                    break;
                case "YN":
                    ADD_VALUE_Y = BAT_YYYYMMDD_D.substring(0, 4);
                    ADD_VALUE = String.valueOf(Integer.valueOf(ADD_VALUE_Y) + 1);
                    break;
                default:
                    APLOG_WT("引数エラー　[" + ARG_ALL + "]\n可変部の指定パターンエラー[" + OPTION4 + "]", FW);
                    return Rtn_NG;
            }
        }


        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.mkdir(WORK_DIR);
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + pid + "_utf8.prm";
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + pid + "_sjis.prm";
        String TEMP_FILE3 = WORK_DIR + "/" + CM_MYPRGID + "03_" + pid + "_sjis.log";
        String TEMP_FILE4 = WORK_DIR + "/" + CM_MYPRGID + "04_" + pid + "_utf8.log";
        String TEMP_FILE5 = WORK_DIR + "/" + CM_MYPRGID + "05_" + pid;

        //###########################################
        //#  Importファイル存在チェック
        //###########################################

        if (!FileUtil.isExistFile(WORK_DIR + "/" + OPTION3 + ADD_VALUE + ".dmp")) {

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Importファイルが存在しません[" + OPTION3 + ADD_VALUE + ".dmp]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  imp実行
        //###########################################
        ShellExecuteDto shellExecuteDto01 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .addEvn("WORK_DIR", WORK_DIR)
                .addEvn("OPTION1", OPTION1)
                .addEvn("ADD_VALUE", ADD_VALUE)
                .addEvn("OPTION3", OPTION3)
                .addEvn("TEMP_FILE1", TEMP_FILE1)
                .addEvn("TEMP_FILE2", TEMP_FILE2)
                .addEvn("TEMP_FILE5", TEMP_FILE5).execute();
        String iconv_err = null;
        if (shellExecuteDto01.RTN0()) {
            iconv_err = FileUtil.readFile(TEMP_FILE5);
            APLOG_WT("iconv：パラメータファイルsjis変換エラー（" + iconv_err + "）", FE);
            return Rtn_NG;
        }

        APLOG_WT("pg_restore開始（" + OPTION1 + ADD_VALUE + "：" + OPTION2 + "）", FI);
        String NLS_LANG_BK = NLS_LANG;


        ShellExecuteDto shellExecuteDto02 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .addEvn("CONNECT_DB", CONNECT_DB)
                .addEvn("TEMP_FILE2", TEMP_FILE2)
                .addEvn("TEMP_FILE3", TEMP_FILE3).execute();

        ShellExecuteDto shellExecuteDto03 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .addEvn("NLS_LANG", NLS_LANG_BK)
                .addEvn("TEMP_FILE3", TEMP_FILE3)
                .addEvn("TEMP_FILE4", TEMP_FILE4)
                .addEvn("TEMP_FILE5", TEMP_FILE5).execute();

        if (shellExecuteDto03.RTN0()) {
            iconv_err = FileUtil.readFile(TEMP_FILE5);
            APLOG_WT("iconv：実行結果ログutf8変換エラー（" + iconv_err + "）", FE);
            return Rtn_NG;
        }

        //String TEMP_FILE4_DATA = FileUtil.readFile(TEMP_FILE4);
        if (shellExecuteDto02.RTN0()) {
            //String EXP_CD = Arrays.stream(TEMP_FILE4_DATA.replace("IMP-", "\nIMP-")
            //       .split("\n"))
            //        .filter(item -> item.indexOf("EXP-") >= 0).collect(Collectors.joining("\n"));
            //String SQL_CD = Arrays.stream(TEMP_FILE4_DATA.replace("ORA-", "\nORA-")
            //       .split("\n")).filter(item -> item.indexOf("ORA-") >= 0).collect(Collectors.joining("\n"));
            APLOG_WT("pg_restoreエラー（" + OPTION1 + ADD_VALUE + "：" + OPTION2 + "）" , FE);
            return Rtn_NG;
        }
        //String imp_value = Arrays.stream(TEMP_FILE4_DATA.replaceAll(" ", "\n").split("\n"))
        //        .filter(item -> item.indexOf("行インポートされました。") >= 0).collect(Collectors.joining("\n"));
        APLOG_WT("pg_restore正常（" + OPTION1 + ADD_VALUE + "：" + OPTION2 + "）\n" , FI);

        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);
        FileUtil.deleteFile(TEMP_FILE4);
        FileUtil.deleteFile(TEMP_FILE5);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

        return Rtn_OK;
    }

    @Override
    public void APLOG_WT(String... args) {
        IFS = "@";
        cmABaplwBServiceImpl.main(getExecuteBaseParam().add(args));
        if (!FI.equals(args[1])) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).add(args[1]));
        }
        IFS = getenv(CmABfuncLServiceImpl.BKUP_IFS);
    }
}
