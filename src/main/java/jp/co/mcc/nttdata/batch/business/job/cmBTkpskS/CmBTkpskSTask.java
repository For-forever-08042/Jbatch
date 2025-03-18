package jp.co.mcc.nttdata.batch.business.job.cmBTkpskS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTkpskB.CmBTkpskBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  期間限定ポイント失効
 * #    プログラムID  ：  cmBTkpskS
 * #
 * #    【処理概要】
 * #       「期間限定ポイント（cmBTkpskB)」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTkpskB」を起動、
 * #       終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
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
 * #      1.00 : 2020/12/02 NDBS.緒方：初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2020 NTT DATA BUSINESS SYSTEMS CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTkpskSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmBTkpskBServiceImpl cmBTkpskBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {
        /*
         * ###########################################
         * #  プログラムIDを環境変数に設定
         * ###########################################
         */
        CM_MYPRGNAME="期間限定ポイント失効処理";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        /*
         * ###########################################
         * #  開始メッセージをAPログに出力
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        /*
         * ###########################################
         * #  定数定義
         * ###########################################
         */
        int Rtn_OK = 10;
        int Rtn_NG = 99;

        /*
         * ###########################################
         * #  APログ出力関数
         * ###########################################
         */

        /*
         * ###########################################
         * #  期間限定ポイント失効（１回目）　プログラム実行
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント失効（１回目）実行").FI());
        MainResultDto cmBTkpskBResult = cmBTkpskBServiceImpl.main(getExecuteBaseParam().add(args));
        int RTN = cmBTkpskBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント失効（1回目）エラー").FI());
            return Rtn_NG;
        }

        /*
         * ###########################################
         * #  期間限定ポイント失効（２回目）　プログラム実行
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント失効（２回目）実行").FI());
        cmBTkpskBResult = cmBTkpskBServiceImpl.main(getExecuteBaseParam().add(args));
        RTN = cmBTkpskBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント失効（２回目）エラー").FI());
            return Rtn_NG;
        }

        /**
         * ###########################################
         * #  終了メッセージをAPログに出力
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
