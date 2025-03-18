package jp.co.mcc.nttdata.batch.business.job.cmBTmmskS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmBTcfckS.CmBTcfckSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTmmskB.CmBTmmskBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  月別会員別購買金額連携作成
 * #    プログラムID  ：  cmBTmmskS
 * #
 * #    【処理概要】
 * #      月別会員別購買金額を集計しCSVファイルを作成、zip圧縮する。
 * #
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
 * #      40.00 :  2023/01/12 SSI.石   ： H016CF会員除外条件追加
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTmmskSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmBTcfckSTask cmBTcfckSTask;

    @Autowired
    CmBTmmskBServiceImpl cmBTmmskBImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        // ###########################################
        // #  プログラムIDを環境変数に設定
        // ###########################################
        CM_MYPRGNAME = "月別会員別購買金額連携作成";

        // ###########################################
        // #  開始メッセージをAPログに出力
        // ###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        // ###########################################
        // #  定数定義
        // ###########################################
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        // 月別会員別購買金額連携作成ファイル名
        String RESULT_FILE_NAME = "KKMI0080";
        String RESULT_FILE_NAME2 = "KKCR0080";

        // 機能ID
        String KINOUID = this.getClass().getSimpleName().substring(4, 8);

        // ###########################################
        // #  引数定義
        // ###########################################
        String ARG_OPT1 = "-DEBUG";          // ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          // ###  デバッグモードでの実行（トレース出力機能が有効）

        // ###########################################
        // #  稼動ディレクトリ決定
        // ###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                // 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        // ###########################################
        // #  引数の数チェック
        // ###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // ###########################################
        // #  引数格納
        // ###########################################
        // #  変数初期化
        String OPTION1 = "";
        for (String ARG_VALUE : args) {
            if (ARG_VALUE.equals(ARG_OPT1) || ARG_VALUE.equals(ARG_OPT2)) {
                OPTION1 = ARG_VALUE;
            } else {
                String errMsg = String.format("引数エラー [定義外の引数(%s)]", ARG_VALUE);
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(errMsg).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        // ###########################################
        // #  プログラム実行
        // ###########################################
        MainResultDto cmBTmmskBResult = cmBTmmskBImpl.main(getExecuteBaseParam().add(OPTION1));
        int RTN = cmBTmmskBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // #/* 2023/01/12 H016CF会員除外条件追加 ADD START */
        // ###########################################
        // #  プログラム実行
        // ###########################################
        cmBTmmskBResult = cmBTmmskBImpl.main(getExecuteBaseParam().add(OPTION1).add("-cf"));
        RTN = cmBTmmskBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        // #/* 2023/01/12 H016CF会員除外条件追加 ADD END */


        // ###########################################
        // #  連携ファイルのレコード重複の確認(KKMI0080)
        // ###########################################
        // # -i入力ファイル  -k機能ID  -cチェックキー項目の順番  -nグーポン番号の順番
        RTN = cmBTcfckSTask.main(getExecuteBaseParam()
                .add(String.format("-i%s.csv", RESULT_FILE_NAME))
                .add(String.format("-k%s", KINOUID))
                .add("-c1")
                .add("-n1"));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(String.format("連携ファイルのレコード重複の確認に失敗しました[%s.csv]", RESULT_FILE_NAME)).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // ###########################################
        // #  連携ファイルの圧縮・移動
        // ###########################################
        String S_FILE_ID = "S_MMSK";
        String R_FILE_ID = "R_MMSK";

        // # 空ファイルは圧縮ファイルに含めない
        String filePath = CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".csv";
        long FILE_SIZE = FileUtil.contentLength(filePath);
        if (FILE_SIZE == 0) {
            FileUtil.deleteFile(filePath);
        }

        // # 空ファイルの場合は圧縮ファイルを作成しない
        if (FILE_SIZE > 0) {
            String zipPath = CM_APWORK_DATE + "/" + RESULT_FILE_NAME + ".zip";
            if (ZipUtil.zipFileAndDelete(zipPath, filePath)!=0) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(String.format("%s.zipの圧縮に失敗しました", RESULT_FILE_NAME)).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }

            // # 圧縮ファイル移動
            String CM_FILEWATSND = getenv(CmABfuncLServiceImpl.CM_FILEWATSND);
            String mvPath = CM_FILEWATSND + "/" + RESULT_FILE_NAME + ".zip";
            RTN = FileUtil.mvFile(zipPath, mvPath); // mv fail retur
            if (RTN != 0) {
                String errMsg = String.format("結果ファイル移動エラー　ファイル名=%s.zip　STATUS=%d", RESULT_FILE_NAME, RTN);
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(errMsg).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }

            // # 送信連動用ファイル作成
            // ls -l ${CM_FILEWATSND}/${RESULT_FILE_NAME}.zip > ${CM_FILEWATSND}/${S_FILE_ID}_OK 2>&1
            ShellExecuteDto shellExecute = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                    .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                    .addEvn("RESULT_FILE_NAME", RESULT_FILE_NAME)
                    .addEvn("S_FILE_ID", S_FILE_ID)
                    .execute();
            if (shellExecute.RTN0()) {
                String errMsg = String.format("送信連動用ファイル作成エラー　ファイル名=%s_OK　STATUS=%s", S_FILE_ID, shellExecute.getResult());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(errMsg).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        // ###########################################
        // #  連携ファイルのレコード重複の確認(KKCR0080)
        // ###########################################
        // # -i入力ファイル  -k機能ID  -cチェックキー項目の順番  -nグーポン番号の順番
        // cmABplodB cmBTcfckS -i${RESULT_FILE_NAME2}.csv -k${KINOUID} -c1 -n1
        RTN = cmBTcfckSTask.main(getExecuteBaseParam()
                .add(String.format("-i%s.csv", RESULT_FILE_NAME2))
                .add(String.format("-k%s", KINOUID))
                .add("-c1")
                .add("-n1"));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(String.format("連携ファイルのレコード重複の確認に失敗しました[%s.csv]", RESULT_FILE_NAME2)).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // #/* 2023/01/12 H016CF会員除外条件追加 ADD START */
        // ###########################################
        // #  連携ファイルの圧縮・移動
        // ###########################################
        String S_FILE_ID2 = "S_MMSK";
        String R_FILE_ID2 = "R_MMSK";

        // # 空ファイルは圧縮ファイルに含めない
        String filePath2 = CM_APWORK_DATE + "/" + RESULT_FILE_NAME2 + ".csv";
        long FILE_SIZE2 = FileUtil.contentLength(filePath2);
        if (FILE_SIZE2 == 0) {
            FileUtil.deleteFile(filePath2);
        }

        // # 空ファイルの場合は圧縮ファイルを作成しない
        if (FILE_SIZE2 > 0) {
            String zipPath = CM_APWORK_DATE + "/" + RESULT_FILE_NAME2 + ".zip";
            if (ZipUtil.zipFileAndDelete(zipPath, filePath2)!=0) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(String.format("%s.zipの圧縮に失敗しました", RESULT_FILE_NAME2)).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }


            // # 圧縮ファイル移動
            String CM_FILEWATSND = getenv(CmABfuncLServiceImpl.CM_FILEWATSND);
            String mvPath = CM_FILEWATSND + "/" + RESULT_FILE_NAME2 + ".zip";
            RTN = FileUtil.mvFile(zipPath, mvPath);
            if (RTN != 0) {
                String errMsg = String.format("結果ファイル移動エラー　ファイル名=%s.zip　STATUS=%d", RESULT_FILE_NAME2, RTN);
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(errMsg).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }

            // # 送信連動用ファイル作成
            // ls -l ${CM_FILEWATSND}/${RESULT_FILE_NAME2}.zip > ${CM_FILEWATSND}/${R_FILE_ID2}_OK 2>&1
            ShellExecuteDto shellExecute = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                    .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                    .addEvn("RESULT_FILE_NAME2", RESULT_FILE_NAME2)
                    .addEvn("R_FILE_ID2", R_FILE_ID2)
                    .execute();
            if (shellExecute.RTN0()) {
                String errMsg = String.format("送信連動用ファイル作成エラー　ファイル名=%s_OK　STATUS=%s", R_FILE_ID2, shellExecute.getResult());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(errMsg).FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }
        // #/* 2023/01/12 H016CF会員除外条件追加 ADD END */

        // ###########################################
        // #  終了メッセージをAPログに出力
        // ###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
