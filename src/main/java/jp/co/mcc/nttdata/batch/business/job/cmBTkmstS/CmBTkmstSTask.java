package jp.co.mcc.nttdata.batch.business.job.cmBTkmstS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.CmABterbBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.job.cmBTcfckS.CmBTcfckSTask;
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
 * #    名称          ：  会員マスタ作成
 * #    プログラムID  ：  cmBTkmstS
 * #
 * #    【処理概要】
 * #      MSカード情報、MS顧客制度情報、WS顧客番号からデータを抽出し、
 * #      会員マスタファイルを作成する。
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
public class CmBTkmstSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    private CmABterbBServiceImpl cmABterbBService;

    @Autowired
    private CmABfzipSTask cmABfzipSTask;

    @Autowired
    private CmBTcfckSTask cmBTcfckSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "会員マスタ作成";

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

        //#  DB接続先
        setConnectConf();

        //# 接続先DB区分
        CONNECT_DB = "MD";

        //# 結果ZIPファイル名
        String RESULT_FILE_NAME = "KKIN0030";

        //# 機能ID
        String KINOUID = CM_MYPRGID.substring(4, 8);

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
        MainResultDto resultDto = cmABgdatBService.main(getExecuteBaseParam());
        int RTN = resultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付の取得に失敗しました", FE);

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = resultDto.result;

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        resultDto = cmABgdatBService.main(getExecuteBaseParam().add("-DY"));
        RTN = resultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付の取得に失敗しました", FE);

            return Rtn_NG;
        }
        String BAT_YYYYMMDD_1 = resultDto.result;

        //###########################################
        //#  プログラム実行
        //###########################################
        //#SD分
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD_1", BAT_YYYYMMDD_1)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + "1.log");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("会員マスタ作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#MD分
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("CONNECT_MD", CONNECT_MD)
                .addEvn("BAT_YYYYMMDD_1", BAT_YYYYMMDD_1)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + "2.log");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("会員マスタ作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  会員マスタファイルを作成
        //###########################################
        //cmABplodB cmABterbB -PcmBTkmstP -F${RESULT_FILE_NAME}.csv_1 -T${BAT_YYYYMMDD} -D${BAT_YYYYMMDD} -S${CONNECT_DB} -A ${OPTION1}
        MainResultDto mainResultDto =
                cmABterbBService.main(getExecuteBaseParam().add("-PcmBTkmstP").add("-F" + RESULT_FILE_NAME + ".csv_1").add("-T" + BAT_YYYYMMDD).add("-D" + BAT_YYYYMMDD).add("-S" + CONNECT_DB).add("-A").add(OPTION1));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("会員マスタファイルの作成に失敗しました").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  改行コードをCRLFに変換
        String s = CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv_1";
        String d = CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv";
        FileUtil.fileCoverToCRLF(s, d);
        FileUtil.deleteFile(s);

        //###########################################
        //#  連携ファイルのレコード重複の確認(KKIN0030)
        //###########################################
        //# -i入力ファイル  -k機能ID  -cチェックキー項目の順番  -nグーポン番号の順番
        //cmABplodB cmBTcfckS -i${RESULT_FILE_NAME}.csv -k${KINOUID} -c1 -n1
        RTN = cmBTcfckSTask.main(getExecuteBaseParam().add("-i" + RESULT_FILE_NAME + ".csv").add("-k" + KINOUID).add("-c1").add("-n1"));
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("連携ファイルのレコード重複の確認に失敗しました[" + RESULT_FILE_NAME + ".csv]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  ファイル圧縮実行
        //###########################################
        //cmABplodB cmABfzipS -O ${CM_APWORK_DATE} -Z ${RESULT_FILE_NAME}.zip -D ${CM_APWORK_DATE} -I ${RESULT_FILE_NAME}.csv -DEL
        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_APWORK_DATE).sZ(RESULT_FILE_NAME + ".zip").sD(CM_APWORK_DATE).sI(RESULT_FILE_NAME + ".csv").add("-DEL"));
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
