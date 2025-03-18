package jp.co.mcc.nttdata.batch.business.job.cmBTfrnkS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTfrnkB.CmBTfrnkBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  家族ランククリア処理
 * #    プログラムID  ：  cmBTfrnkS
 * #
 * #    【処理概要】
 * #       MS家族制度情報の翌年用のランク初期設定を行う処理
 * #       家族ランククリア（cmBTfrnkB）」を起動するためのシェル。
 * #       開始メッセージを出力する。
 * #       WSバッチ処理実行管理.シーケンス番号(ランククリア完了年月日)の判断
 * #       シーケンス番号（ランククリア完了年月日)の年がシステム日付の年と同じ場合終了）、
 * #      「cmBTfrnkB」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -c          :  更新最大件数(必須)
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2012/11/30 SSI.本田  ： 初版
 * #      2.00 :   2015/01/30 SSI.上野  ： 引数追加（更新最大件数）
 * #      3.00 :   2015/06/02 SSI.上野  ： 家族ランククリア処理を20日以降に
 * #                                       実行した場合、警告ログ出力（検知用）
 * #     40.00 :   2022/09/26 SSI.川内  ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTfrnkSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmBTfrnkBServiceImpl cmBTfrnkBImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "家族ランククリア";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        setConnectConf();

        //#  引数定義
        String ARG_OPT1 = "-c";     //###  更新最大件数
        String ARG_OPT2 = "-DEBUG"; //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-debug"; //###  デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length < 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  引数チェック
        //###########################################
        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT2, ARG_OPT3)) {
                OPTION2 = arg;

            } else if (StringUtils.startsWith(arg, ARG_OPT1)) {
                OPTION1 = arg;
            }
        }

        //# 必須引数をチェック
        if (StringUtils.isEmpty(OPTION1)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());

            return Rtn_NG;
        }
        String BAT_YYYYMM = BAT_YYYYMMDD.substring(0, 6);

        //###########################################
        //#  シーケンス番号（ランククリア完了日付）の取得
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .addEvn("CONNECT_SD", CONNECT_SD)
                .execute();
        String result = shellExecuteDto.result;
        log.info("shell sqlplus execute result:{}", result);

        if (shellExecuteDto.RTN0()) {
            // SQL_CD=`echo ${RTN_VAL} | sed s/ORA-/'\n'ORA-/ | grep "ORA-" |  cut -c5-9`
            // if test "${SQL_CD}" = "01005" -o "${SQL_CD}" = "12154" -o "${SQL_CD}" = "01017" -o "${SQL_CD}" = ""
            boolean isDBLinkError = result.contains("FATAL") || !result.contains("ERROR");
            if (isDBLinkError) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ＤＢ接続時に失敗しました。").FE());
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("WSバッチ処理実行管理の取得に失敗しました。").FE());
            }

            return Rtn_NG;
        }
        // 0 20330420
        String RTN_VAL = Arrays.stream(result.split(" "))
                .filter(it -> it.length() == 8)
                .findFirst().orElse("");

        //###########################################
        //#  取得日付とバッチ日付の比較
        //###########################################
        String RTN_YYYYMM = RTN_VAL.substring(0, 6);
        if (StringUtils.equals(RTN_YYYYMM, BAT_YYYYMM)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

            return Rtn_OK;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        //#cmABplodB cmBTfrnkB $1
        MainResultDto cmBTfrnkBResult = cmBTfrnkBImpl.main(getExecuteBaseParam().add(OPTION1).add(OPTION2));
        RTN = cmBTfrnkBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  バッチ日付の比較
        //###########################################
        String BAT_DD = BAT_YYYYMMDD.substring(6, 8);
        if (Integer.parseInt(BAT_DD) > 20) {
            String msg = String.format("%sを実行しました。日付：%s", CM_MYPRGNAME, BAT_YYYYMMDD);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(msg).FE());
        }

        // ###########################################
        // #  終了メッセージをAPログに出力
        // ###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
