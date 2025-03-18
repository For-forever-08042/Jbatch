package jp.co.mcc.nttdata.batch.business.job.cmBTbmshS;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  バッチインプット-売上明細取込（MK-EC_購買履歴データ）起動シェル
 * #    プログラムID  ：  cmBTbmshS
 * #
 * #    【処理概要】
 * #        購買履歴データを入力とし、明細TBLの登録・更新を行う処理
 * #       「バッチインプット-売上明細取込（MK-EC_購買履歴データ）（cmBTbmshJ）　」を起動するためのシェル。
 * #        開始メッセージを出力し、パラメータファイルパス取得、「cmBTbmshJ」を起動、
 * #        終了メッセージを出力し、戻り値を返却。
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
 * #     40.00 : 2022/10/21 SSI.畑本：MCCM 初版
 * #     41.00 : 2023/08/10 SSI.畑本：MCCMPH2
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Component
public class CmBTbmshSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgprmBServiceImpl cmABgprmBServiceImpl;

    String INPUT_ZIP_FILE_NAME = "DEKK0050.zip";
    String INPUT_CSV_FILE_NAME = "ecgo_order_history.csv";

    @Override
    public int taskExecuteCustom(String[] args) {

//    /*
//     * ###########################################
//     * #  プログラムIDを環境変数に設定
//     * ###########################################
//     */
//    CM_MYPRGNAME="売上明細取込（MK-EC_購買履歴データ）";
//    setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

        /*
         * ###########################################
         * #  開始メッセージをAPログに出力
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        /*
         * ###########################################
         * #  定数定義
         * ###########################################
         */
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-DEBUG";        //デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";        //デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
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

        //#  引数格納変数初期化
        String OPTION1 = "";
        //#  引数格納
        for (String ARG_VALUE : args) {
            if (StringUtils.equals(ARG_VALUE, ARG_OPT1) || StringUtils.equals(ARG_VALUE, ARG_OPT2)) {
                OPTION1 = "ON";
            }
        }

        //###########################################
        //#  ファイル名文字列を生成
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + " 対象ファイル名：" + INPUT_CSV_FILE_NAME).FI());

        //###########################################
        //#  パラメータファイル名の取得
        //###########################################
        MainResultDto resultDto = cmABgprmBServiceImpl.main(getExecuteBaseParam().add("cmBTbmshP"));
        if (Rtn_OK != resultDto.exitCode) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("パラメータファイルが存在しません。[cmBTbmshP]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String ARG_BMSH_PRMFILE = resultDto.result;

        //###########################################
        //#  対象ファイルの存在チェック
        //###########################################
        String RCV_FILE_NAME = CM_APWORK_DATE + "/" + INPUT_CSV_FILE_NAME;
        int FILE_CNT1 = FileUtil.countLines(RCV_FILE_NAME);
        if (FILE_CNT1 < 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(RCV_FILE_NAME + " ファイルが存在しません").FI());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        String INPUT_CSV_FILE_PATH = CM_APWORK_DATE + "/" + INPUT_CSV_FILE_NAME;
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .addEvn("ARG_BMSH_PRMFILE", ARG_BMSH_PRMFILE)
                .addEvn("OPTION1", OPTION1)
                .addEvn("CM_JAVA_APBIN", CM_JAVA_APBIN)
                .addEvn("INPUT_CSV_FILE_PATH", INPUT_CSV_FILE_PATH).execute();

        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + " 異常終了").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        //# 「CM_APWORK_DATE」で入力ファイル削除
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + INPUT_CSV_FILE_NAME);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME));

        return Rtn_OK;
    }

    //###########################################
    //#  エラー発生時のファイル退避関数
    //###########################################
    public void COPY_ERR_FILE() {
        String ERROR_DIR = CM_APWORK_DATE + "/ERROR";
        if (!FileUtil.isExistDir(ERROR_DIR)) {
            FileUtil.mkdir(ERROR_DIR);
        }

        FileUtil.mvFile(CM_APWORK_DATE + "/" + INPUT_CSV_FILE_NAME, ERROR_DIR);
        //        ###########################################
        //        #  CRMへ連携した購買履歴ファイルを削除する。
        //        ###########################################
        FileUtil.deleteFile(CM_CRMRENKEI + "/" + INPUT_ZIP_FILE_NAME);

    }
}
