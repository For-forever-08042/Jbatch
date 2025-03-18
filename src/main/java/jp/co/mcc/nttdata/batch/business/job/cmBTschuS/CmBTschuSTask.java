package jp.co.mcc.nttdata.batch.business.job.cmBTschuS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTschuB.CmBTschuBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  出産クーポン発行可否更新処理
 * #    プログラムID  ：  cmBTschuS
 * #
 * #    【処理概要】
 * #       マタニティベビーの各エントリー情報のお子様の出生年月日より、バッチ処理
 * #       日付時点に出産クーポン発行可否の変更対象となる会員の出産クーポン発行可
 * #       否を更新する処理「出産クーポン発行可否更新（cmBTschuB）」を起動するため
 * #       のシェル。
 * #       開始メッセージを出力し、「cmBTschuB」を起動、終了メッセージを出力し、
 * #       戻り値を返却。
 * #
 * #    【引数説明】
 * #       -d 処理対象日付      : （任意）省略時はバッチ処理日付とする。
 * #       -DEBUG or -debug     :  (任意) デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2013/01/11 SSI.本田  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTschuSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;
    @Autowired
    CmBTschuBServiceImpl cmBTschuB;

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

        //###########################################
        //#  引数定義
        //###########################################

        String ARG_OPT1 = "-d";// ###  処理対象日付
        String ARG_OPT2 = "-DEBUG";//      ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-debug";// ###  デバッグモードでの実行（トレース出力機能が有効）

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
        //#  引数の数チェック
        //###########################################

        if (args.length > 2) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String OPTION1 = "";
        String OPTION2 = "";
        //#  引数格納変数初期化
        for (String ARG_VALUE : args) {
            if (StringUtils.equals(ARG_VALUE, ARG_OPT2) || StringUtils.equals(ARG_VALUE, ARG_OPT3)) {
                OPTION2 = ARG_VALUE;
            } else if (ARG_VALUE.startsWith(ARG_OPT1)) {
                OPTION1 = ARG_VALUE;
            }
        }
        //###########################################
        //#  プログラム実行
        //###########################################
        int RTN = cmBTschuB.main(getExecuteBaseParam().add(OPTION1).add(OPTION2)).exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
