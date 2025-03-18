package jp.co.mcc.nttdata.batch.business.job.cmBTnnrgS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTnnrgB.CmBTnnrgBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  翌年年別情報初期登録処理
 * #    プログラムID  ：  cmBTnnrgS
 * #
 * #    【処理概要】
 * #       翌年年別情報初期登録を行う「翌年年別情報初期登録処（cmBTnnrgB）」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTnnrgB」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -c          :  更新最大件数(必須)
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2014/07/07 SSI.吉田  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2014 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTnnrgSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;
    @Autowired
    CmBTnnrgBServiceImpl cmBTnnrgB;


    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-c";//               ###  更新最大件数
        String ARG_OPT2 = "-DEBUG";//           ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-debug";//           ###  デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
                if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                    // 作業ディレクトリファイル作成失敗
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }
        }
        //###########################################
        //#  引数格納
        //###########################################

        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";

        if (args.length < 1) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        if (args.length > 2) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        for (String ARG_VALUE : args) {
            if (ARG_VALUE.equals(ARG_OPT2) || ARG_VALUE.equals(ARG_OPT3)) {
                OPTION2 = ARG_VALUE;
            } else if (ARG_VALUE.startsWith(ARG_OPT1)) {
                OPTION1 = ARG_VALUE;
            }
        }
        //# 必須引数をチェック
        if (StringUtils.isEmpty(OPTION1)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  翌年年別情報初期登録プログラム実行
        //###########################################
        int RTN = cmBTnnrgB.main(getExecuteBaseParam().add(OPTION1).add(OPTION2)).exitCode;
        if (Rtn_OK != RTN) {
            //	# 終了メッセージをAPログに出力
            APLOG_WT("翌年年別情報初期登録プログラム実行失敗", FE);
            return Rtn_NG;
        }
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
