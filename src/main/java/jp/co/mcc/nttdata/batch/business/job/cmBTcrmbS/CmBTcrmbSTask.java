package jp.co.mcc.nttdata.batch.business.job.cmBTcrmbS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.CmABterbBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  マタニティベビー連動シェル
 * #    プログラムID  ：  cmBTcrmbS
 * #
 * #    【処理概要】
 * #       MMマタニティベビー情報からデータを抽出し、CRM連動用のファイル（マタニティベビーマスタファイル）
 * #       を作成する処理「連動ファイル作成（cmABteraB）」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmABteraB」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/11 SSI.横山直人：初版
 * #      1.01 :   2012/12/17 SSI.本田    ：CRM連動用ディレクトリへの移動は
 * #                                        共通の送信ファイル作成にて行う
 * #      2.00 :   2013/05/28 SSI.横山    ：ファイル出力処理を可変長対応版に変更
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTcrmbSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmABterbBServiceImpl cmABterbBServiceImpl;

    //###########################################
    //#  引数定義
    //###########################################
    private final static String ARG_OPT1 = "-DEBUG";       //デバッグモードでの実行（トレース出力機能が有効）
    private final static String ARG_OPT2 = "-debug";       //デバッグモードでの実行（トレース出力機能が有効）

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "マタニティベビー連動";
        String CM_MYPRGNAME_SUB = "マタニティベビーマスタファイル抽出";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME_SUB));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        String FILE_NAME = "MATERNITY_BABY.csv";

        String DB_KBUN = "MD";

        String OPTION1 = "";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                //# 作業ディレクトリファイル作成失敗
                APLOG_WT("稼動ディレクトリ作成エラー", FE);

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数格納
        //###########################################
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case ARG_OPT1:
                    case ARG_OPT2:
                        if (!OPTION1.isEmpty()) {
                            APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FE);
                            return Rtn_NG;
                        } else {
                            OPTION1 = args[i];
                            continue;
                        }
                    default:
                        APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FE);
                        return Rtn_NG;
                }
            }
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        MainResultDto mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
        int RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー", FW);
            return Rtn_NG;
        }
        String BAT_DAY = mainResultDto.result;

        //###########################################
        //#  マタニティベビーマスタ連動ファイル作成
        //###########################################
        MainResultDto mainResultDto1 =
                cmABterbBServiceImpl.main(getExecuteBaseParam().add("-PcmBTcrmbP").add("-F" + CM_APWORK_DATE + "/" + FILE_NAME).add("-D" + BAT_DAY).add("-S" + DB_KBUN).add("-A"));
        RTN = mainResultDto1.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("マタニティベビーマスタ連動ファイル作成エラー ", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME_SUB));
        return Rtn_OK;
    }
}
