package jp.co.mcc.nttdata.batch.business.job.cmBTecenS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTecenB.CmBTecenBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ＥＣ会員情報取込
 * #    プログラムID  ：  cmBTecenS
 * #
 * #    【処理概要】
 * #       ＥＣ会員情報ファイルより、ＥＣ会員情報取込を行う「ＥＣ会員情報取込（cmBTecenB）」
 * #       を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTecenB」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG            :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug            :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00  : 2013/05/03 SSI.本田：初版
 * #      40.00 : 2022/10/11 SSI.申  ：MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTecenSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmBTecenBServiceImpl cmBTecenBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ＥＣ会員情報取込";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;
        //#Rtn_ER=99

        String DB_DELGRP = "カード入会";

        String IN_FILE = "ec_kaiin.csv";
        String ERR_FILE_NAME = "_kaiin_touroku_error.csv";
        String DEL_FILE = "????????${ERR_FILE_NAME}";

        //#  ｢ＥＣ会員情報取込｣用ジョブ実績出力用ログファイル名称
        String CM_G18002D = "JBch_G18002D";

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 当日日付取得
        MainResultDto mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        int RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", FE);

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = mainResultDto.result;

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム日付と時刻
        //#SYS_YYYYMMDDHHMMSS=`date '+%Y%m%d%H%M%S'`

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリファイル作成失敗
                APLOG_WT("稼動ディレクトリ作成エラー", FE);

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";
        //#  引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            }
        }

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + pid;
        String TEMP_FILE3 = CM_APWORK_DATE + "/" + CM_MYPRGID + "03_" + pid;
        String TEMP_FILE4 = CM_APWORK_DATE + "/" + CM_MYPRGID + "04_" + pid;

        String work_in_file = CM_APWORK_DATE + "/" + IN_FILE;
        FileUtil.copyFile(CM_FILENOWRCV + "/" + IN_FILE, work_in_file);

        //################################################
        //#  処理対象情報をジョブ実績ログファイルに出力
        //################################################
        String logPath = CM_JOBRESULTLOG + "/" + CM_G18002D + ".log";
        //# 連動ファイル有無の取得
        Integer RENDO_FILE_UMU = FileUtil.countLines(work_in_file);
        if (!FileUtil.isExistFile(work_in_file) || ObjectUtils.isEmpty(RENDO_FILE_UMU) || RENDO_FILE_UMU == 0) {
            FileUtil.writeFile(logPath, "        連動ファイル：なし");
            FileUtil.writeFileByAppend(logPath, "        件数：0 件");

            //#  ファイルが存在しなければ正常終了
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理対象ファイルなし　ファイル名=" + IN_FILE).FI());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

            return Rtn_OK;
        }

        FileUtil.writeFile(logPath, "        連動ファイル：あり");
        FileUtil.writeFileByAppend(logPath, "        件数：" + RENDO_FILE_UMU + " 件");

        //###########################################
        //#  ＥＣ会員情報取込プログラム実行
        //###########################################
        mainResultDto = cmBTecenBServiceImpl.main(getExecuteBaseParam().add("-i" + IN_FILE).add(OPTION1));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK && RTN != Rtn_NG) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        FileUtil.deleteFile(work_in_file);

        //###########################################
        //#  入会エラーリストファイルをコピー
        //###########################################
        String fileName = CM_APWORK_DATE + "/" + BAT_YYYYMMDD + ERR_FILE_NAME;
        Integer ERROR_FILE_DATA_CNT = FileUtil.countLines(fileName);
        if (ObjectUtils.isNotEmpty(ERROR_FILE_DATA_CNT) && ERROR_FILE_DATA_CNT == 0) {
            FileUtil.deleteFile(fileName);
        }

        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);
        FileUtil.deleteFile(TEMP_FILE4);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
