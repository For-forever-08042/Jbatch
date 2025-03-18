package jp.co.mcc.nttdata.batch.business.job.cmBTpkjdS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ポイント還元実績作成
 * #    プログラムID  ：  cmBTpkjdS
 * #
 * #    【処理概要】
 * #      HSポイント日別情報YYYYMM・PS会員番号体系・MSカード情報からデータを抽出し、
 * #      ポイント還元実績作成ファイルを作成する。
 * #
 * #    【引数説明】
 * #       -DEBUG or -debug  : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      40.00 :  2022/09/06 SSI.本多 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Component
public class CmBTpkjdSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmABfzipSTask cmABfzipSTask;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    String ARG_OPT1 = "-DEBUG";
    String ARG_OPT2 = "-debug";

    //結果ファイル名
    String RESULT_FILE_NAME = "KKMI0120";

    @Override
    public int taskExecuteCustom(String[] args) {
        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "ポイント還元実績作成";
        setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK) + "/" + DateUtil.getYYYYMMDD();
            setenv(C_aplcom1Service.CM_APWORK_DATE, CM_APWORK_DATE);
        }

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //    # 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
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

        //###########################################
        //#  引数格納
        //###########################################
        //#  変数初期化
        String OPTION1 = "";
        for (int i = 0; i < args.length; i++) {
            if (Objects.equals(args[i], ARG_OPT1) || Objects.equals(args[i], ARG_OPT2)) {
                OPTION1 = args[i];
            } else {
                cmABaplwBServiceImpl.main(
                        getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + args[i] + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日付前日の取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return RTN;
        }

//        //当月
//        String BAT_YYYYMM = StringUtils.substring(BAT_YYYYMMDD, 0, 6);
//
//        //前月
//        ShellExecuteDto shellExecuteDto01 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
//                .addEvn("CONNECT_SD", CONNECT_SD).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).execute();
//        String BAT_YYYYMMDD_LAST = shellExecuteDto01.result;
//        String BAT_YYYYMM_LAST = StringUtils.substring(BAT_YYYYMMDD_LAST, 0, 6);
//
//        //来月
//        ShellExecuteDto shellExecuteDto02 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
//                .addEvn("CONNECT_SD", CONNECT_SD).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).execute();
//        String BAT_YYYYMMDD_NEXT = shellExecuteDto02.result;
//        String BAT_YYYYMM_NEXT = StringUtils.substring(BAT_YYYYMMDD_NEXT, 0, 6);

        //###########################################
        //#  プログラム実行
        //###########################################
        ShellExecuteDto shellExecuteDto03 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).execute();

        if (shellExecuteDto03.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv");
            cmABaplwBServiceImpl.main(
                    getExecuteBaseParam().P("cmBTpkjdS").M("ポイント還元実績作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  処理件数取得
        //###########################################
        Integer DATA_COUNT = FileUtil.countLines(CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv");

        if (DATA_COUNT == null) {
            // # 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  処理件数出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID)
                .M("ファイル名：[" + RESULT_FILE_NAME + ".csv]  出力件数：[" + DATA_COUNT + "]").FI());

        //###########################################
        //#  ファイル圧縮実行
        //###########################################
        RTN = cmABfzipSTask.main(
                getExecuteBaseParam().add("-O").add(CM_APWORK_DATE).add("-Z").add(RESULT_FILE_NAME + ".zip").add("-D")
                        .add(CM_APWORK_DATE).add("-I").add(RESULT_FILE_NAME + ".csv").add("-DEL"));
        if (RTN != Rtn_OK) {
            APLOG_WT("ファイルの圧縮失敗 [" + CM_APWORK_DATE + "/" + RESULT_FILE_NAME + "]", FW);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
