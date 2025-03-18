package jp.co.mcc.nttdata.batch.business.job.cmBTfmjdS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.job.cmBTcfckS.CmBTcfckSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  家族会員情報作成
 * #    プログラムID  ：  cmBTfmjdS
 * #
 * #    【処理概要】
 * #      家族会員情報ファイルを作成する。
 * #
 * #    【引数説明】
 * #       -DEBUG or -debug  : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  警告
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
@Slf4j
@Component
public class CmBTfmjdSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    CmBTcfckSTask cmBTcfckSTask;

    @Autowired
    CmABfzipSTask cmABfzipSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "家族会員情報作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //# 接続先DB区分
        CONNECT_DB = "SD";

        //# 出力ファイル名
        String RESULT_FILE_NAME = "KKMI0140";

        //# 機能ID cmBTfmjdS
        String KINOUID = CM_MYPRGID.substring(4, 8);

        //###########################################
        //#  DB接続先
        //###########################################
        setConnectConf();

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                // 作業ディレクトリファイル作成失敗
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
        String IN_FILE = "";

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
        //#  バッチ処理前日日付の取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBService.main(getExecuteBaseParam().add("-DY"));
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        String SYS_YYYYMMDD = cmABgdatBResult.result;

        //###########################################
        //#  家族会員情報ファイルを作成
        //###########################################
        //# SQL実行結果をファイルに出力
        //# 引数:バッチ処理前日 出力ファイルファイル名(KKMI0140.csv)
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .addEvn("RESULT_FILE_NAME", RESULT_FILE_NAME)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("家族会員情報ファイルを作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  連携ファイルのレコード重複の確認(KKMI0140)
        //###########################################
        //# -i入力ファイル  -k機能ID  -cチェックキー項目の順番  -nグーポン番号の順番
        RTN = cmBTcfckSTask.main(getExecuteBaseParam().add("-i" + RESULT_FILE_NAME + ".csv").add("-k" + KINOUID).add("-c1,2").add("-n2"));
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("連携ファイルのレコード重複の確認に失敗しました[" + RESULT_FILE_NAME + ".csv]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  ファイル圧縮実行
        //###########################################
        RTN = cmABfzipSTask.main(getExecuteBaseParam().add("-O").add(CM_APWORK_DATE).add("-Z").add(RESULT_FILE_NAME + ".zip").add("-D").add(CM_APWORK_DATE).add("-I").add(RESULT_FILE_NAME + ".csv").add("-DEL"));
        if (RTN != Rtn_OK) {
            APLOG_WT("ファイルの圧縮失敗 [" + CM_APWORK_DATE + "/" + RESULT_FILE_NAME + "]", FW);
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
