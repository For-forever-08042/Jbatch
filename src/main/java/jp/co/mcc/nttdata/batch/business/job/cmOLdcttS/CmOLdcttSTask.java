package jp.co.mcc.nttdata.batch.business.job.cmOLdcttS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  該当なし後付取引繰越処理
 * #    プログラムID  ：  cmOLdcttS
 * #
 * #    【処理概要】
 * #        TSＤＣログ情報NNYYYYMMDD（前日分）より該当なし後付取引のトランザクション
 * #        データを当日テーブルに登録する。
 * #        開始メッセージを出力し、パラメータファイルパス取得、「cmOLdcttJ」を起動、
 * #        終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6.1
 * #
 * #    改定履歴
 * #      1.00 :   2012/11/04 SSI.本田  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmOLdcttSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;
    @Autowired
    CmABgprmBServiceImpl cmABgprmBService;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    //#引数定義
    private static final String ARG_OPT1 = "-DEBUG";//     ###  デバッグモードでの実行（トレース出力機能が有効）
    private static final String ARG_OPT2 = "-debug";//     ###  デバッグモードでの実行（トレース出力機能が有効）
    String OPTION1 = null;
    String OPTION2 = null;

    String CM_APWORK_DATE = null;
    int exit_cd = 0;
    //#  引数格納変数初期化
    String ARG_DCTT_PRMFILE = "";

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "該当なし後付取引繰越処理";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################

        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK) + "/" + DateUtil.getYYYYMMDD();
            setenv(C_aplcom1Service.CM_APWORK_DATE, CM_APWORK_DATE);
        }

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //    # 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        for (int i = 0; i < args.length; i++) {
            String item = args[i];
            if (ARG_OPT1.equals(item) || ARG_OPT2.equals(item)) {
                OPTION1 = "ON";
            }
        }
        //###########################################
        //#  パラメータファイル名の取得
        //###########################################
        MainResultDto mainResultDto = cmABgprmBService.main(getExecuteBaseParam().add("cmOLdcttP"));
        ARG_DCTT_PRMFILE = mainResultDto.result;
        if (mainResultDto.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("パラメータファイルが存在しません。[cmOLdcttP]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            //###########################################
            //#  COPY用ファイルの削除関数
            //###########################################
            ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                    .addEvn("CM_APWORK_DATE", CM_APWORK_DATE).execute();
            return Rtn_NG;
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .addEvn("ARG_DCTT_PRMFILE", ARG_DCTT_PRMFILE)
                .addEvn("OPTION1", OPTION1)
                .addEvn("CM_JAVA_APBIN", getenv(C_aplcom1Service.CM_WEBBAT))
                .addEvn("CLASSPATH", getenv(C_aplcom1Service.CLASSPATH))
                .addEvn("CM_WEBBAT", getenv(C_aplcom1Service.CM_WEBBAT)).execute();
        if (shellExecuteDto.RTN10()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME));

        return Rtn_OK;
    }


}
