package jp.co.mcc.nttdata.batch.business.job.cmBTtareS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTtareB.CmBTtareBImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  止め区分洗替シェル
 * #    プログラムID  ：  cmBTtareS
 * #
 * #    【処理概要】
 * #       バッチ処理日付前日にMM顧客企業別属性情報のDM留め区分、
 * #       Eメール止め区分が変更された場合、
 * #       同じ顧客番号の止め区分の洗替処理を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTtareB」を起動、終了メッセージを出力し、
 * #       戻り値を返却。
 * #
 * #    【引数説明】
 * #   -debug(-DEBUG)    :  デバッグログ出力指定（任意）
 * #
 * #    【戻り値】
 * #        10     ：  正常
 * #        49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2016/10/03 SSI.田 ： 初版
 * #     40.00 :   2022/09/30 SSI.川内 ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTtareSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmBTtareBImpl cmBTtareBImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  変数定義
        //###########################################
        //# プログラムIDを環境変数に設定
        CM_MYPRGNAME = "止め区分洗替";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
                if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                    // 作業ディレクトリファイル作成失敗
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

                    return Rtn_NG;
                }
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        //# 引数が1個より多い
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        MainResultDto cmBTtareBResult = cmBTtareBImpl.main(getExecuteBaseParam().add(args));
        int RTN = cmBTtareBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }
        
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
