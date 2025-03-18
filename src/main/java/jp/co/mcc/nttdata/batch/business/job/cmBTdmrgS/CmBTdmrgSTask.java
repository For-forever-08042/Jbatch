package jp.co.mcc.nttdata.batch.business.job.cmBTdmrgS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTdmrgB.CmBTdmrgBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ダミー登録シェル
 * #    プログラムID  ：  cmBTdmrgS
 * #
 * #    【処理概要】
 * #       バッチ処理日付前日を基準としお買い上げ等あった顧客に対して、
 * #       顧客ダミー登録、翌年年別情報初期登録を行う処理
 * #      「ダミー登録処理（cmBTdmrgB）」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTdmrgB」を起動、終了メッセージを出力し、
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
 * #      1.00 :   2012/11/14 SSI.越後谷 ： 初版
 * #     40.00 :   2022/09/28 SSI.川内   ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTdmrgSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    CmBTdmrgBServiceImpl cmBTdmrgBService;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  変数定義
        //###########################################
        //# プログラムIDを環境変数に設定
        CM_MYPRGNAME = "ダミー登録";

        //# 引数保存領域
        String[] ARG_SV = args;

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        // ###########################################
        // #  稼動ディレクトリ決定
        // ###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                // 作業ディレクトリファイル作成失敗
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        //# 引数が1個より多い
        if (args.length > 1) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        MainResultDto cmBTdmrgBResultDto = cmBTdmrgBService.main(getExecuteBaseParam().add(ARG_SV));
        int RTN = cmBTdmrgBResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
