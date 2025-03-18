package jp.co.mcc.nttdata.batch.business.job.cmBTpztbS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ポイント残高表（年度ポイント）ファイル作成
 * #    プログラムID  ：  cmBTpztbS040
 * #
 * #    【処理概要】
 * #     ポイント残高表（年度ポイント）ファイル作成
 * #     を行うものである。
 * #
 * #    【引数説明】
 * #       -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6.1
 * #
 * #    改定履歴
 * #      40.00 : 2023/03/13 SSI.申：MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTpztbSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ポイント残高表（年度ポイント）作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付と時刻
        String SYS_YYYYMMDDHHMMSS = DateUtil.getYYYYMMDDHHMMSS();

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-DEBUG";                       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";                       //###  デバッグモードでの実行（トレース出力機能が有効）

        //#  出力ファイル
        String PZTB_O_T = "POINT_LIST_T";                 //### 当年度
        String PZTB_O_Z = "POINT_LIST_Z";                 //### 前年度

        //#  sqlファイル名
        String SQLFile = "cmBTpztbS";

        String TOU_NEN = "24";                             //### 当年度
        String ZEN_NEN = "12";                             //### 前年度

        //###########################################
        //#  DB接続先
        //###########################################
        setConnectConf();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の格納
        //###########################################
        if (args.length > 1) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        //#  引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            } else {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        MainResultDto resultDto = cmABgdatBService.main(getExecuteBaseParam());
        int RTN = resultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = resultDto.result;

        //###########################################
        //#  バッチ処理日付(前月)の取得
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .execute();
        String BAT_YYYYMMDD_1 = shellExecuteDto.result.trim();
        String LAST_MONTH_YYYYMM = BAT_YYYYMMDD_1.substring(0, 6);

        //###########################################
        //#  ポイント残高表（年度ポイント）（当年度）ファイル出力
        //###########################################
        //# SQL実行結果をファイルに出力
        //# 出力ファイル名
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("SQLFile", SQLFile)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .addEvn("PZTB_O_T", PZTB_O_T)
                .addEvn("SYS_YYYYMMDDHHMMSS", SYS_YYYYMMDDHHMMSS)
                .addEvn("TOU_NEN", TOU_NEN)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + PZTB_O_T + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("当年度ポイント残高取得処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  ポイント残高表（年度ポイント）（前年度）ファイル出力
        //###########################################
        //# SQL実行結果をファイルに出力
        //# 出力ファイル名
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("SQLFile", SQLFile)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .addEvn("PZTB_O_Z", PZTB_O_Z)
                .addEvn("SYS_YYYYMMDDHHMMSS", SYS_YYYYMMDDHHMMSS)
                .addEvn("ZEN_NEN", ZEN_NEN)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + PZTB_O_Z + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("前年度ポイント残高取得処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  改行コードをCRLFに変換
        String sT = CM_APWORK_DATE + "/" + PZTB_O_T + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv";
        String dT = CM_APWORK_DATE + "/" + PZTB_O_T + "_" + SYS_YYYYMMDDHHMMSS + ".csv";
        FileUtil.fileCoverToCRLF(sT, dT);
        FileUtil.deleteFile(sT);

        String sZ = CM_APWORK_DATE + "/" + PZTB_O_Z + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv";
        String dZ = CM_APWORK_DATE + "/" + PZTB_O_Z + "_" + SYS_YYYYMMDDHHMMSS + ".csv";
        FileUtil.fileCoverToCRLF(sZ, dZ);
        FileUtil.deleteFile(sZ);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
