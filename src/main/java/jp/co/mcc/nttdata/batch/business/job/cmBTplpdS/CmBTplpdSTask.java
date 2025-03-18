package jp.co.mcc.nttdata.batch.business.job.cmBTplpdS;

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
 * #    名称          ：  期間限定ポイント付与実績作成シェル
 * #    プログラムID  ：  cmBTplpdS
 * #
 * #    【処理概要】
 * #       MSカード情報、HSポイント日別情報YYYYMM、HSポイント日別内訳情報YYYYMMからデータを抽出し、
 * #       期間限定ポイント付与実績ファイルを作成するためのシェル。
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
 */
@Component
public class CmBTplpdSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    private CmABfzipSTask cmABfzipSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "期間限定ポイント付与実績作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        String ARG_OPT1 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String OUTPUT_FILE = "KKCR0100";

        //#  DB接続先
        setConnectConf();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 1) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  引数格納
        //###########################################
        //#  変数初期化
        String OPTION1 = "";
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
        //# 処理日前日の年月日を取得
        MainResultDto resultDto = cmABgdatBService.main(getExecuteBaseParam().add("-DY"));
        int RTN = resultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", FE);

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = resultDto.result;

        //# 処理日前日の年月を生成
        String BAT_YYYYMM = BAT_YYYYMMDD.substring(0, 6);

        //###########################################
        //#  プログラム実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM", BAT_YYYYMM)
                .execute();
        String fileName = CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv";
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(fileName);

            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント付与実績作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  処理件数取得
        Integer DATA_COUNT = FileUtil.countLines(fileName);
        if (DATA_COUNT == null) {
            //# 処理件数取得失敗
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# 実行結果出力ファイルを圧縮する
        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_APWORK_DATE).sZ(OUTPUT_FILE + ".zip").sD(CM_APWORK_DATE).sI(OUTPUT_FILE + ".csv").DEL());
        if (RTN != Rtn_OK) { // # 戻り値がOKでない場合は異常終了
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + OUTPUT_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  処理件数出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + OUTPUT_FILE + ".csv]  出力件数：[" + DATA_COUNT + "]").FI());

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
