package jp.co.mcc.nttdata.batch.business.job.cmABdbtrS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * -------------------------------------------------------------------------------
 * 名称　　　　　：　テーブルTruncate
 * プログラムID　：　cmABdbtrS
 * <p>
 * 【処理概要】
 * テーブルTruncateを行う。
 * <p>
 * 【引数説明】
 * -T  テーブル名	:　対象のテーブル名
 * -D  接続DB	:　接続対象のDB
 * <p>
 * 【戻り値】
 * 10　　 ：　正常
 * 49　　 ：　異常
 * -------------------------------------------------------------------------------
 * 稼働環境
 * Red Hat Enterprise Linux 6
 * <p>
 * 改定履歴
 * 1.00 :	2012/10/18 SSI.吉岡  ： 初版
 * -------------------------------------------------------------------------------
 * $Id:$
 * -------------------------------------------------------------------------------
 * Copyright (C) 2012 NTT DATA CORPORATION
 * -------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmABdbtrSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    private final static String ARG_OPT1 = "-T";  //    ###  テーブル名
    private final static String ARG_OPT3 = "-D";  //    ###  接続DB


    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ
        //  引数格納変数初期化
        String OPTION1 = null;
        String OPTION3 = null;
        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "テーブルTruncate";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        if (args.length != 4) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);
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
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + ARG_ALL + "]", FW);
                    return Rtn_NG;

            }
        }
        //必須引数チェック
        if (OPTION1 == null || OPTION3 == null) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }


        //接続先チェック
        if (!checkConnectionType(OPTION3)) {
            return Rtn_NG;
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
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + pid;
        String TEMP_FILE3 = WORK_DIR + "/" + CM_MYPRGID + "03_" + pid;


//        StringBuffer buffer = new StringBuffer();
//        buffer.append("SET PAUSE OFF").append("\n")
//                .append("SET AUTOCOMMIT OFF").append("\n")
//                .append("SET EXITCOMMIT OFF").append("\n")
//                .append("whenever sqlerror exit 1").append("\n")
//                .append("TRUNCATE TABLE ").append(OPTION1).append("\n")
//                .append("/").append("\n")
//                .append("exit").append("\n");
//        FileUtil.writeFile(getenv(C_aplcom1Service.CM_APWORK_DATE) + "/" + TEMP_FILE2 + ".sql", buffer.toString(), SystemConstant.Shift_JIS);
        //###########################################
        //#  SQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto01 = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .addEvn("WORK_DIR", WORK_DIR)
                .addEvn("OPTION1", OPTION1)
                .addEvn("CONNECT_DB", CONNECT_DB)
                .addEvn("TEMP_FILE2", TEMP_FILE2)
                .addEvn("TEMP_FILE3", TEMP_FILE3).execute();


        if (shellExecuteDto01.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto01.result);
            APLOG_WT("SQLエラー（" + OPTION1 + "のTruncate）　[SQLCODE：" + SQL_CD + "]", FE);
            return Rtn_NG;
        }


        APLOG_WT("SQL実行（" + OPTION1 + "のtruncate）　正常終了", FI);

        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

        return Rtn_OK;
    }


}
