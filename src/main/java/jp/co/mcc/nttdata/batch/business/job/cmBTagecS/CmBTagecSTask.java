package jp.co.mcc.nttdata.batch.business.job.cmBTagecS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTagecB.CmBTagecBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  年齢計算
 * #    プログラムID  ：  cmBTagecS
 * #
 * #    【処理概要】
 * #        MM顧客情報、MMマタニティベビー情報より、
 * #        翌日誕生日である対象顧客を抽出し,新年齢計算を行い年齢更新する処理
 * #       「年齢計算（cmBTagecB）」を起動するためのシェル。
 * #        開始メッセージを出力し、「cmBTagecB」を起動、終了メッセージを出力し、
 * #        戻り値を返却。
 * #
 * #    【引数説明】
 * #      -DEBUG        :  デバッグモードでの実行（トレース出力機能が有効）
 * #      -debug        :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/12 SSI.吉岡  ： 初版
 * #     40.00 :   2022/09/30 SSI.川内  ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmBTagecSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;
    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTagecBServiceImpl cmBTagecB;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));


        //###########################################
        //#  定数定義
        //###########################################
        // 戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();
        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 1) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        int RTN = cmBTagecB.main(getExecuteBaseParam().add(args)).exitCode;
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
