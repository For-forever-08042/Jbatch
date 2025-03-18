package jp.co.mcc.nttdata.batch.business.job.cmABdbl2S;

import jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * -------------------------------------------------------------------------------
 * 名称　　　　　：　Copyでのデータロード
 * プログラムID　：　cmABdbl2S
 * <p>
 * 【処理概要】
 * 対象テーブルにCopyにてデータをロードする機能。
 * <p>
 * 【引数説明】
 * -D  接続DB	:　接続対象のDB
 * -C  制御ファイル名	:　copy用の制御ファイル名を指定
 * （拡張子は不要　：　".sql"固定）
 * -P  テーブル名可変部	:　対象テーブル名に可変部がある場合に指定
 * -I  データファイル名	:　copy用のデータファイル名を指定
 * （拡張子は不要　：　".dat"固定）
 * -L  ログファイル名	:　copy用のログファイル名を指定
 * （拡張子は不要　：　".log"固定）
 * -E  エラーファイル名	:　copy用のエラーファイル名を指定
 * （拡張子は不要　：　".bad"固定）
 * -H  最大廃棄件数	:　制御ファイルに指定したwhen条件で廃棄される最大件数
 * （指定なしの場合はチェックしない）
 * <p>
 * 【戻り値】
 * 10　　 ：　正常
 * 99　　 ：　異常
 * -------------------------------------------------------------------------------
 * 稼働環境
 * Red Hat Enterprise Linux 6
 * <p>
 * 改定履歴
 * 1.00 :	2012/10/23 SSI.吉岡  ： 初版
 * -------------------------------------------------------------------------------
 * $Id:$
 * -------------------------------------------------------------------------------
 * Copyright (C) 2012 NTT DATA CORPORATION
 * -------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmABdbl2STask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;


    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 99;

    //#引数定義
    private final static String ARG_OPT1 = "-D";//  ###  接続DB
    private final static String ARG_OPT2 = "-C";//  ###  制御ファイル名
    private final static String ARG_OPT3 = "-P";//  ###  テーブル名可変部
    private final static String ARG_OPT4 = "-I";//  ###  データファイル名
    private final static String ARG_OPT5 = "-L";//  ###  ログファイルID
    private final static String ARG_OPT6 = "-E";//  ###  エラーファイルID
    private final static String ARG_OPT7 = "-H";//		###  最大廃棄件数


    int exit_cd = 0;

    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ
        //  引数格納変数初期化
        String OPTION1 = null;
        String OPTION2 = null;
        String OPTION3 = null;
        String OPTION4 = null;
        String OPTION5 = null;
        String OPTION6 = null;
        String OPTION7 = "NO";
        // プログラムIDを環境変数に設定
//        String CM_MYPRGID = args[0].substring(0, 9);
//        args[0]= CM_MYPRGID;
//        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);
        CM_MYPRGNAME = "Copyでのデータロード";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        if (args.length > 12) {
            APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }


        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (OPTION1 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 2) {
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
                    if (args.length < i + 2) {
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
                    if (args.length < i + 2) {
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
                    OPTION4 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT5:
                    if (OPTION5 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION5 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT6:
                    if (OPTION6 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION6 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT7:
                    if (!"NO".equals(OPTION7)) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION7 = args[i + 1];
                    i++;
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);
                    return Rtn_NG;

            }
        }
        //必須引数チェック
        if (OPTION1 == null || OPTION2 == null) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }


        //接続先チェック
        if (!checkConnectionType(OPTION1)) {
            return Rtn_NG;
        }

        if (checkDBSize()) {
            APLOG_WT("引数エラー  （起動環境と接続先DBの不整合）\n該当環境では指定不可の接続先DBが指定されている  [" + OPTION2 + "]", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  最大廃棄件数の数値チェック
        //###########################################
        if (!"NO".equals(OPTION7)) {
            if (!OPTION7.equals(NTRegexUtil.find("([0-9]*)", OPTION7))) {
                APLOG_WT("引数エラー（最大廃棄件数）[" + OPTION7 + "]", FE);
                return Rtn_NG;
            }
        }


        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  バッチ処理日付
        MainResultDto RTN = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        if (RTN.exitCode != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー", FW);
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = RTN.result;


        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.mkdir(WORK_DIR);
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + pid;
        String WK_PWD = WORK_DIR;

        //###########################################
        //#  Copyの起動パラメータを編集
        //###########################################
        String SQL_FILE = CM_APSQL + "/loader/" + OPTION2 + ".sql";
        String DAT_FILE = null;
        String LOG_FILE = null;
        String BAD_FILE = null;
        long LOC_SZ = 67108864;
        if (OPTION4 == null) {
            DAT_FILE = WK_PWD + "/" + OPTION2 + ".dat";
        } else {
            DAT_FILE = WK_PWD + "/" + OPTION4 + ".dat";
        }
        if (OPTION5 == null) {
            LOG_FILE = WORK_DIR + "/" + OPTION2 + ".log";
        } else {
            LOG_FILE = WORK_DIR + "/" + OPTION5 + ".log";
        }
        if (OPTION6 == null) {
            BAD_FILE = WORK_DIR + "/" + OPTION2 + ".bad";
        } else {
            BAD_FILE = WORK_DIR + "/" + OPTION6 + ".bad";
        }

        //###########################################
        //#  ファイル存在チェック
        //###########################################
        //#  コントロールファイル指定有無で対象ファイルを編集
        if (!FileUtil.isExistFile(SQL_FILE)) {
            APLOG_WT("制御ファイルなし  [" + SQL_FILE + "]", FE);
            return Rtn_NG;
        }

        //#  データファイル指定有無で対象ファイルを編集
        if (OPTION4 != null) {
            if (!FileUtil.isExistFile(DAT_FILE)) {
                APLOG_WT("データファイルなし  [" + DAT_FILE + "]", FE);
                return Rtn_NG;
            }
        }

        String CTL_FILE = null;
        //###########################################
        //#  テーブル名可変部の指定可否チェック
        //###########################################
        //#  引数にテーブル名可変部の指定がある場合
        String SQL_FILE_DATA = FileUtil.readFile(SQL_FILE);
        if (OPTION3 != null) {
            //#  		コントロールファイルにテーブル名可変部の指定がない場合
            if (StringUtils.isEmpty(SQL_FILE_DATA) || SQL_FILE_DATA.indexOf("@PNAME@") < 0) {
                APLOG_WT("引数エラー  [" + ARG_ALL + "]\n対象の制御ファイルにはテーブル名可変部を指定不可  [" + SQL_FILE + "]", FW);
                return Rtn_NG;
            } else {
                //#  		コントロールファイルにテーブル名可変部の指定がある場合
                CTL_FILE = WORK_DIR + "/" + OPTION2 + OPTION3 + ".sql";
                String output = SQL_FILE_DATA.replaceAll("@PNAME@", OPTION3).replaceAll("@BATDATE@", BAT_YYYYMMDD);
                if (FileUtil.writeFile(CTL_FILE, output) != 0) {
                    APLOG_WT("置換エラー　　テーブル名可変部 [" + OPTION3 + "]、バッチ処理日 [" + BAT_YYYYMMDD + "]", FE);
                    return Rtn_NG;
                }
            }
        } else {
            if (SQL_FILE_DATA.indexOf("@PNAME@") > 0) {
                APLOG_WT("引数エラー  [" + ARG_ALL + "]\n対象の制御ファイルにはテーブル名可変部が指定必須  [" + SQL_FILE + "]", FW);
                return Rtn_NG;
            }
            //#  		コントロールファイルにテーブル名可変部の指定がある場合
            CTL_FILE = WORK_DIR + "/" + OPTION2 + BAT_YYYYMMDD + "_" + PidUtil.getPid() + ".sql";
            String output = SQL_FILE_DATA.replaceAll("@BATDATE@", BAT_YYYYMMDD);
            if (FileUtil.writeFile(CTL_FILE, output) != 0) {
                APLOG_WT("バッチ処理日置換エラー  [" + BAT_YYYYMMDD + "]", FE);
                return Rtn_NG;
            }
        }


        String DATA_PARA = null;
        //###########################################
        //#  制御ファイルにデータファイルが指定されている場合は、それを有効にする
        //###########################################
        if (SQL_FILE_DATA.indexOf("INFILE") < 0) {

            if (SQL_FILE_DATA.indexOf("infile") < 0) {
                //#		コントロールファイルにデータファイル（小文字）の指定がない場合
                DATA_PARA = "data=" + DAT_FILE;
            } else {
                //#		コントロールファイルにデータファイル（小文字）の指定がある場合
                DATA_PARA = "";
            }
        } else {
            //#	コントロールファイルにデータファイル（大文字）の指定がある場合
            DATA_PARA = "";

        }
        //###########################################
        //#  引数にデータファイルが指定されている場合は、それを優先する
        //###########################################

        if (OPTION4 != null) {
            DATA_PARA = "data=" + DAT_FILE;
        }
        //###########################################
        //#  copy実行
        //###########################################
//        rm -f ${LOG_FILE}  >/dev/null 2>&1
//        rm -f ${BAD_FILE}  >/dev/null 2>&1
        String SJIS_CTL = new File(CTL_FILE).getParent() + "/SJIS_" + new File(CTL_FILE).getName();

        String NLS_LANG_BK = NLS_LANG;
//        ShellExecuteDto shellExecuteDto01 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
//                .addEvn("LOG_FILE", LOG_FILE)
//                .addEvn("BAD_FILE", BAD_FILE)
//                .addEvn("CTL_FILE", CTL_FILE).execute();

        FileUtil.deleteFile(LOG_FILE);
        FileUtil.deleteFile(BAD_FILE);
        int RNT = IconvUtil.main(SystemConstant.UTF8, SystemConstant.UTF8, CTL_FILE, SJIS_CTL);
        if (RNT != 0) {
            APLOG_WT("コントロールファイルのSjis変換エラー", FE);
            return Rtn_NG;
        }

        ShellExecuteDto shellExecuteDto02 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .addEvn("SJIS_CTL", SJIS_CTL)
                .addEvn("WK_PWD", WK_PWD)
                .addEvn("CONNECT_DB", CONNECT_DB)
                //.addEvn("DATA_PARA", DATA_PARA)
                //.addEvn("BAD_FILE", BAD_FILE)
                //sqlloader must cd floder to dat file director
                //.addEvn("WK_PWD", WK_PWD)
                //.addEvn("LOC_SZ", String.valueOf(LOC_SZ))
                .addEvn("LOG_FILE", LOG_FILE).execute();

//        ShellExecuteDto shellExecuteDto03 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
//                .addEvn("NLS_LANG", NLS_LANG_BK)
//                .addEvn("LOG_FILE", LOG_FILE).execute();
        IconvUtil.main(SystemConstant.Shift_JIS, SystemConstant.UTF8, LOG_FILE, LOG_FILE + "_utf8");
        String SQL_CD = "";

        String LOG_FILE_DATA = FileUtil.readFile(LOG_FILE + "_utf8");
        if (shellExecuteDto02.result.startsWith("0")){
            exit_cd = Rtn_OK;
        }else{
            APLOG_WT("copy 異常終了:[" + shellExecuteDto02.result + "]", FE);
            return Rtn_NG;
        }
//        switch (shellExecuteDto02.result) {
//            default:
//                APLOG_WT("sqlldr 終了コード:[" + shellExecuteDto02.result + "]", FE);
//                String SQLLDR_LOG = "";
//                Optional<String> optional = Arrays.stream(LOG_FILE_DATA.replaceAll("SQL\\*Loader-", "\nSQL\\*Loader-").split("\n")).filter(item -> item.startsWith("SQL*Loader-")).findFirst();
//                if (optional.isPresent()) {
//                    SQLLDR_LOG = optional.get();
//                }
//                if (StringUtils.isNotEmpty(SQLLDR_LOG)) {
//                    APLOG_WT("[" + SQLLDR_LOG + "]", FI);
//                }
//                optional = Arrays.stream(LOG_FILE_DATA.replaceAll("ORA-", "\nORA-").split("\n")).filter(item -> item.startsWith("ORA-")).findFirst();
//                if (optional.isPresent()) {
//                    SQL_CD = optional.get();
//                }
//                if (StringUtils.isNotEmpty(SQL_CD)) {
//                    APLOG_WT("[SQLCODE：" + SQL_CD + "]", FI);
//                }
//
//                return Rtn_NG;
//        }


//        String SKIP_CNT = Arrays.stream(LOG_FILE_DATA.split("\n")).
//                filter(item -> item.indexOf("スキップされた論理レコードの合計") >= 0).
//                collect(Collectors.joining("\n")).replace(" ","");
//        String LOAD_CNT = Arrays.stream(LOG_FILE_DATA.split("\n")).filter(item -> item.indexOf("読み込まれた論理レコードの合計") >= 0).collect(Collectors.joining("\n")).replace(" ","");
//        String LOSS_CNT = Arrays.stream(LOG_FILE_DATA.split("\n")).filter(item -> item.indexOf("拒否された論理レコードの合計") >= 0).collect(Collectors.joining("\n")).replace(" ","");
//        String DUST_CNT = Arrays.stream(LOG_FILE_DATA.split("\n")).filter(item -> item.indexOf("廃棄された論理レコードの合計") >= 0).collect(Collectors.joining("\n")).replace(" ","");
//        List<String> DUST_CNT2 = Arrays.stream(LOG_FILE_DATA.split("\n")).filter(item -> item.indexOf("廃棄された論理レコードの合計") >= 0).map(item -> item.split(" ")[1]).collect(Collectors.toList());

//        APLOG_WT(SKIP_CNT, FI);
//        APLOG_WT(LOAD_CNT, FI);
//        APLOG_WT(LOSS_CNT, FI);
//        APLOG_WT(DUST_CNT, FI);

//        if (!"NO".equals(OPTION7)) {
//            if (!CollectionUtils.isEmpty(DUST_CNT2)) {
//                String finalOPTION = OPTION7;
//                if (DUST_CNT2.stream().anyMatch(item -> item.compareTo(finalOPTION) > 0)) {
//                    APLOG_WT("ロード失敗（廃棄件数超過）[" + DAT_FILE + "]", FW);
//                    cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FE());
//                    return Rtn_NG;
//                }
//            }

//        }

        //###########################################
        //#  最大廃棄件数の超過チェック
        //###########################################
        if (FileUtil.isExistFile(BAD_FILE)) {
            APLOG_WT("ロード失敗[" + DAT_FILE + "]", FW);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FE());

            ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                    .addEvn("NLS_LANG", NLS_LANG_BK)
                    .addEvn("LOG_FILE", BAD_FILE).execute();
            return Rtn_NG;
        }

        FileUtil.deleteFile(SJIS_CTL);
        FileUtil.deleteFile(LOG_FILE);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

        return Rtn_OK;
    }


}
