package jp.co.mcc.nttdata.batch.business.job.cmBTkhifS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.CmABterbBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.job.cmBTgmdcS.CmBTgmdcSTask;
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
 * #    名称          ：  会員紐付け情報ファイル作成
 * #    プログラムID  ：  cmBTkhifS
 * #
 * #    【処理概要】
 * #      入会、退会、統合、統合解除された会員情報を抽出し、
 * #      会員紐付け情報ファイルを作成する。
 * #
 * #    【引数説明】
 * #       -NOCHECK  : （任意）重複チェック処理スキップ
 * #       -DEBUG or -debug  : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 7
 * #
 * #    改定履歴
 * #      40.00 :  2022/12/08 SSI.山口 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTkhifSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    private CmABterbBServiceImpl cmABterbBService;

    @Autowired
    private CmBTgmdcSTask cmBTgmdcSTask;

    @Autowired
    private CmABfzipSTask cmABfzipSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "会員紐付け情報ファイル作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 99;

        String ARG_OPT1 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-NOCHECK";        //##  重複チェック処理スキップ
        String ARG_OPT4 = "-nocheck";        //##  重複チェック処理スキップ

        //#  DB接続先
        setConnectConf();

        //# 出力ファイル名
        String OUTPUT_FILE = "KKIN0040";

        String DB_KBN = "SD";

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
        if (args.length > 2) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  引数格納
        //###########################################
        //#  変数初期化
        String OPTION1 = "";
        String CHECK_FLG = "1";

        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            } else if (StringUtils.equalsAny(arg, ARG_OPT3, ARG_OPT4)) {
                CHECK_FLG = "0";
            } else {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日前日の取得
        //###########################################
        MainResultDto mainResultDto = cmABgdatBService.main(getExecuteBaseParam().add("-DY"));
        int RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        String SYS_YYYYMMDD = mainResultDto.result;

        //###########################################
        //#  SQLファイル存在チェック
        //###########################################
        if (!FileUtil.isExistFile(CM_APSQL + "/" + CM_MYPRGID + ".sql")) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQLファイル[" + CM_APSQL + "/" + CM_MYPRGID + ".sql]が存在しません").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  SQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".tmp");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("会員紐付け情報ファイル作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  連動ファイル作成
        //###########################################
        mainResultDto =
                cmABterbBService.main(getExecuteBaseParam().add("-PcmBTkmstP").add("-F"+ CM_APWORK_DATE +"/"+ OUTPUT_FILE +
                ".csv_1").add("-S"+DB_KBN).add("-A").add(OPTION1));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + OUTPUT_FILE + ".csv)作成に失敗しました ").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  改行コードをCRLFに変換
        String s = CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv_1";
        String d = CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv";
        FileUtil.fileCoverToCRLF(s, d);
        FileUtil.deleteFile(s);

        //###########################################
        //#  出力ファイル存在確認
        //###########################################
        if (!FileUtil.isExistFile(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv")) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮対象ファイルなし [" + CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        if (StringUtils.equals("1", CHECK_FLG)) {
            RTN = cmBTgmdcSTask.main(getExecuteBaseParam().add("-i" + CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv"));
            if (RTN != Rtn_OK) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("重複チェック処理に失敗しました[" + OUTPUT_FILE + ".csv]").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        } else {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("重複チェック処理をスキップしました[" + OUTPUT_FILE + ".csv]").FI());
        }

        //###########################################
        //#  ファイル圧縮実行
        //###########################################
        //cmABplodB cmABfzipS -O ${CM_APWORK_DATE} -Z ${OUTPUT_FILE}.zip -D ${CM_APWORK_DATE} -I ${OUTPUT_FILE}.csv -DEL
        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_APWORK_DATE).sZ(OUTPUT_FILE + ".zip").sD(CM_APWORK_DATE).sI(OUTPUT_FILE + ".csv").add("-DEL"));
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + OUTPUT_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  一時ファイル削除
        //###########################################
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".tmp");

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
