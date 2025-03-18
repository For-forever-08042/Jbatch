package jp.co.mcc.nttdata.batch.business.job.cmBTplkdS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  期間限定ポイント還元実績作成シェル
 * #    プログラムID  ：  cmBTplkdS
 * #
 * #    【処理概要】
 * #       RetailCRM向けの期間限定ポイント還元実績取得SQLを実行し、
 * #       実行結果をファイルに出力するためのシェル。
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
 * #      40.00 :  2022/11/21 SSI.田崎 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Component
public class CmBTplkdSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmABfzipSTask cmABfzipSTask;

    String SQL_CD = "";
    Integer DATA_COUNT;


    @Override
    public int taskExecuteCustom(String[] args) {


        /*
         * ###########################################
         * #  開始メッセージをAPログに出力
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        /*
         * ###########################################
         * #  定数定義
         * ###########################################
         */
        Rtn_OK = 10;
        Rtn_NG = 49;

        String ARG_OPT1 = "-DEBUG";          //デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //デバッグモードでの実行（トレース出力機能が有効）
        String OUTPUT_FILE = "KKCR0110";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

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
            String ARG_VALUE = args[i];
            if (ARG_VALUE.equals(ARG_OPT1) || ARG_VALUE.equals(ARG_OPT2)) {
                OPTION1 = ARG_VALUE;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 処理日前日の年月日を取得

        MainResultDto BAT_YYYYMMDD_DATA = cmABgdatB.main(getExecuteBaseParam().add("-DY"));
        String BAT_YYYYMMDD = BAT_YYYYMMDD_DATA.result;
        int RTN = BAT_YYYYMMDD_DATA.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー[" + RTN + "]").FE());
            return Rtn_NG;
        }

        //# 処理日前日の年月を生成
        String BAT_YYYYMM = StringUtils.substring(BAT_YYYYMMDD, 0, 6);

        //###########################################
        //#  プログラム実行
        //###########################################
        //# SQL実行結果をファイルに出力
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM", BAT_YYYYMM).execute();

        if (shellExecuteDto.RTN0()) {
            //# 戻り値が0でない場合は異常終了
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv");
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント還元実績作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#  処理件数取得
        if(FileUtil.isExistFile(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv")){
            DATA_COUNT = FileUtil.countLines(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv");
            if (DATA_COUNT == null) {
                //# 処理件数取得失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //# 実行結果出力ファイルを圧縮する
        RTN = cmABfzipSTask.main(
                getExecuteBaseParam().add("-O").add(CM_APWORK_DATE).add("-Z").add(OUTPUT_FILE + ".zip").add("-D")
                        .add(CM_APWORK_DATE).add("-I").add(OUTPUT_FILE + ".csv").add("-DEL"));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + OUTPUT_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  処理件数出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + OUTPUT_FILE + ".csv]  出力件数：[" + DATA_COUNT + "]").FI());

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
