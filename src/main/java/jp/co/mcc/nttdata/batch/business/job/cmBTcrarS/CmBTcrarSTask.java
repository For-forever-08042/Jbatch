package jp.co.mcc.nttdata.batch.business.job.cmBTcrarS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTcrarB.CmBTcrarBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  顧客番号洗い替えリスト連動シェル
 * #    プログラムID  ：  cmBTcrarS
 * #
 * #    【処理概要】
 * #       HSカード変更情報からデータを抽出し、CRM連動用のファイル（顧客番号洗い替えリスト）
 * #       を作成する処理「顧客番号洗い替えリスト連動（cmBTcrarB）」を起動するためのシェル。
 * #       このシェルでは、cmBTcrarB起動する。
 * #
 * #    【引数説明】
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/07 SSI.横山直人  ： 初版
 * #      1.01 :   2012/12/17 SSI.本田      ： CRM連動用ディレクトリへの移動は
 * #                                           共通の送信ファイル作成にて行う
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTcrarSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmBTcrarBServiceImpl cmBTcrarBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "顧客番号洗い替えリスト連動";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-DEBUG";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String OPTION1 = "";

        String FILE_NAME = "KOKYAKU_ARAIGAE.csv";

        //###########################################
        //#  定数定義
        //###########################################
        int Rtn_OK = 10;
        int Rtn_NG = 49;
        int PG_Rtn_NG = 99;

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
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                if (StringUtils.isNotEmpty(OPTION1)) {
                    APLOG_WT("引数重複指定エラー [" + ARG_ALL + "]", FE);

                    return Rtn_NG;
                }
                
                OPTION1 = arg;
            } else {
                APLOG_WT("引数エラー 定義外の引数 [" + ARG_ALL + "]", FE);

                return Rtn_NG;
            }
        }

        //###########################################
        //#  顧客番号洗い替えリスト連動プログラム実行
        //###########################################
        MainResultDto cmBTcrarBResult = cmBTcrarBServiceImpl.main(getExecuteBaseParam().add("-o" + FILE_NAME));
        int RTN = cmBTcrarBResult.exitCode;
        if (PG_Rtn_NG == RTN) {
            APLOG_WT("顧客番号洗い替えリスト連動実行失敗 ", FW);

            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
