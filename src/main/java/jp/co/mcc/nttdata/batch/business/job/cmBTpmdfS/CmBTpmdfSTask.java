package jp.co.mcc.nttdata.batch.business.job.cmBTpmdfS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABptlmS.CmABptlmSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTpmdfB.CmBTpmdfBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;

import java.io.File;
import java.util.ArrayList;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ポイント修正処理
 * #    プログラムID  ：  cmBTpmdfS
 * #
 * #    【処理概要】
 * #        ポイント修正情報（入力ファイル）をもとに、顧客のボーナス対象ポイント、
 * #        クラブオン対象金額、還付可能ポイントを加減する処理「ポイント修正
 * #        （cmBTpmdfB）」を起動するためのシェル。
 * #        開始メッセージを出力し、「cmBTpmdfB」を起動、終了メッセージを出力し、
 * #        戻り値を返却。
 * #
 * #    【引数説明】
 * #   -rポイント修正ファイル格納ディレクトリ
 * #                                   :  処理対象のポイント修正ファイルが配置されているディレクトリ
 * #                                      環境変数による指定も可
 * #   -c                              :  ${CM_FILENOWRCV}にファイルコピーする、省略された場合コピーしない
 * #   -DEBUG                          :  デバッグモードでの実行（トレース出力機能が有効）
 * #   -debug                          :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/26 SSI.吉岡  ： 初版
 * #      2.00 :   2016/09/14 SSI.田    ： ポイント一括付与自動化対応
 * #                                       →処理種別削除
 * #                                       →実績ファイル追加出力
 * #               2016/09/27 SSI.本田  ： 読み込みファイルを処理対象ディレクトリ
 * #                                       取得する
 * #     3.00  :   2021/01/22 NDBS.緒方 :  ポイント修正ファイル名/項目変更
 * #     4.00  :   2021/04/07 NDBS.亀谷 :  実績ログ出力条件修正
 * #     40.00 :   2022/09/22 SSI.申    :  MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTpmdfSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    CmABptlmSTask cmABptlmSTask;

    @Autowired
    CmBTpmdfBServiceImpl cmBTpmdfBService;

    @Override
    public int taskExecuteCustom(String[] args) {
        //#------------------------------------------
        //#  プログラムIDを環境変数に設定
        //#------------------------------------------
        CM_MYPRGNAME = "ポイント修正";

        //#------------------------------------------
        //#  開始メッセージをAPログに出力
        //#------------------------------------------
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //#------------------------------------------
        //#  定数定義
        //#------------------------------------------
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        //# ｢ポイント修正｣用ジョブ実績出力用ログファイル名称
        String JBcs_G68003Z = "JBcs_G68003Z";

        //#出力結果ファイル
        String result_filename = "limitedPoint_result_*.txt";
        String InFile = "limitedPoint_change_??????????????.txt";

        String FinInFile = "finished_???";
        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-r";          //###  処理対象のポイント修正ファイル格納ディレクトリ
        String ARG_OPT2 = "-DEBUG";      //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-debug";      //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT4 = "-c";          //###  ${CM_FILENOWRCV}にファイルコピーする、省略された場合コピーしない

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

        //#------------------------------------------
        //#  引数チェック
        //#------------------------------------------
        if (args.length > 3) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";
        String OPTION3 = "0";
        //#  引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT2, ARG_OPT3)) {
                OPTION2 = arg;
            } else if (StringUtils.startsWith(arg, ARG_OPT1)) {
                OPTION1 = arg.replaceAll(ARG_OPT1, "");
            } else if (StringUtils.startsWith(arg, ARG_OPT4)) {
                OPTION3 = "1";
            }
        }

        //###########################################
        //#  前回分バックアップ
        //###########################################
        //#処理終了後、結果ファイルをするするため、存在しないはず
        //gzip ${CM_APWORK_DATE}/${result_filename} > /dev/null 2>&1
        ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("result_filename", result_filename)
                .execute();

        int skipCnt = 0;
        String CM_PCRENKEI_KOJINARI = getenv(C_aplcom1Service.CM_PCRENKEI_KOJINARI);

        ArrayList<String> fileList = FileUtil.findByRegex(OPTION1, "limitedPoint_change_.{14}.txt"); //"limitedPoint_change_??????????????.txt"
        for (String f : fileList) {

            //#------------------------------------------
            //#処理開始実績ログ出力
            //#------------------------------------------
            FileUtil.deleteFile(CM_JOBRESULTLOG + "/" + JBcs_G68003Z + ".log");
            cmABptlmSTask.main(getExecuteBaseParam().add("- " + CM_MYPRGNAME + " 開始").add(JBcs_G68003Z));

            //#------------------------------------------
            //#入力ファイルフォーマットチェック
            //#------------------------------------------
            //#for line in `cat ${InFile} | sed -e '/^,*$/d' -e '/^,/d'`
            ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                    .addEvn("filename", f)
                    .addEvn("Rtn_NG", Integer.toString(Rtn_NG))
                    .execute();
            if (StringUtils.equals(shellExecuteDto.result, Integer.toString(Rtn_NG))) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("入力ファイルフォーマットエラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
                cmABptlmSTask.main(getExecuteBaseParam().add("× " + CM_MYPRGNAME + " フォーマットエラー").add(JBcs_G68003Z));

                return Rtn_NG;
            }

            //#------------------------------------------
            //#  プログラム実行
            //#------------------------------------------
            //#echo "実行コマンド：cmABplodB mcBTpmdfB ${OPTION1} ${OPTION2} ${FROM} ${TO} ${OPTION3}"
            File file = new File(f);
            String ARG_FILENM = file.getName();
            //###########################################
            //#  重複取込チェック
            //###########################################
            int shoriFLG = 1;
            if (!StringUtils.equals(OPTION3, "1")) {
                shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_05")
                        .defaultEvn(this)
                        .addEvn("FinInFile", FinInFile)
                        .addEvn("InFile", InFile)
                        .addEvn("OPTION1", OPTION1)
                        .addEvn("ARG_FILENM", ARG_FILENM)
                        .addEvn("skipCnt", Integer.toString(skipCnt))
                        .execute();
                String skipCntStr = shellExecuteDto.result;
                if (StringUtils.isEmpty(skipCntStr)) {
                    skipCntStr = "0";
                }
                if (!StringUtils.equals("0", skipCntStr)) {
                    skipCnt = Integer.parseInt(skipCntStr);
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("[" + ARG_FILENM + "]処理済みのファイルのため、スキップ").FI());
                    shoriFLG = 0;
                }
            }

            if (shoriFLG == 1) {
                //#------------------------------------------
                //#処理開始実績ログ出力
                //#------------------------------------------
                FileUtil.deleteFile(CM_JOBRESULTLOG + "/" + JBcs_G68003Z + ".log");
                cmABptlmSTask.main(getExecuteBaseParam().add("- " + CM_MYPRGNAME + " 開始").add(JBcs_G68003Z));

                MainResultDto cmBTpmdfBResult = cmBTpmdfBService.main(getExecuteBaseParam().add("-i" + ARG_FILENM).add(OPTION2));
                int RTN = cmBTpmdfBResult.exitCode;
                if (RTN != Rtn_OK) {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

                    return Rtn_NG;
                }

                //#------------------------------------------
                //#実績ログ出力
                //#------------------------------------------
                shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                        .defaultEvn(this)
                        .addEvn("result_filename", result_filename)
                        .addEvn("CM_JOBRESULTLOG", CM_JOBRESULTLOG)
                        .addEvn("JBcs_G68003Z", JBcs_G68003Z)
                        .execute();
                String RESULT_SYMBOL = shellExecuteDto.result;
                cmABptlmSTask.main(getExecuteBaseParam().add(RESULT_SYMBOL.trim() + " " + CM_MYPRGNAME).add(JBcs_G68003Z));

                //#-------------------------------------------
                //# 結果ファイルを移動する
                //#-------------------------------------------
                if (StringUtils.equals(OPTION3, "1")) {
                    FileUtil.mvFile(CM_FILENOWRCV + "/" + ARG_FILENM, CM_FILEAFTRCV + "/" + ARG_FILENM);
                    FileUtil.copyFileByRegx(CM_APWORK_DATE, CM_FILENOWRCV, "^limitedPoint_result_.*.txt"); //limitedPoint_result_*.txt
                } else {
                    ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                            .defaultEvn(this)
                            .addEvn("FinInFile", FinInFile)
                            .addEvn("ARG_FILENM", ARG_FILENM)
                            .addEvn("CM_FILENOWRCV", CM_FILENOWRCV)
                            .execute();
                }

                FileUtil.mvFileByRegx(CM_APWORK_DATE, CM_PCRENKEI_KOJINARI, "^limitedPoint_result_.*.txt");
            }
        }

        //#------------------------------------------
        //#  終了メッセージをAPログに出力
        //#------------------------------------------
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }

}
