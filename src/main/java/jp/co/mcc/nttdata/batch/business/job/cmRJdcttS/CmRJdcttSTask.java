package jp.co.mcc.nttdata.batch.business.job.cmRJdcttS;

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
 * -------------------------------------------------------------------------------
 * 名称          ：  (臨時)該当なし後付取引繰越処理
 * プログラムID  ：  cmRJdcttS
 * <p>
 * 【処理概要】
 * TSＤＣログ情報NNYYYYMMDD（前日分）より任意の該当なし後付取引のトランザ
 * クションデータを当日テーブルに登録する。
 * 開始メッセージを出力し、パラメータファイルパス取得、「cmRJdcttJ」を起動、
 * 終了メッセージを出力し、戻り値を返却。
 * <p>
 * 【引数説明】
 * -f1      :  システム年月日(FROM)
 * -f2      :  サービス対応番号(FROM)
 * -f3      :  取引通番(FROM)
 * -t1      :  システム年月日(TO)
 * -t2      :  サービス対応番号(TO)
 * -t3      :  取引通番(TO)
 * -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * <p>
 * 【戻り値】
 * 10     ：  正常
 * 49     ：  警告
 * -------------------------------------------------------------------------------
 * 稼働環境
 * Red Hat Enterprise Linux 6.1
 * <p>
 * 改定履歴
 * 1.00 :   2014/06/06 SSI.本田  ： 初版
 * -------------------------------------------------------------------------------
 * $Id:$
 * -------------------------------------------------------------------------------
 * Copyright (C) 2012 NTT DATA CORPORATION
 * -------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmRJdcttSTask extends NtBasicTask {

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
    private static final String ARG_OPT3 = "-f1";//  ###  システム年月日(FROM)
    private static final String ARG_OPT4 = "-f2";//  ###  サービス対応番号(FROM)
    private static final String ARG_OPT5 = "-f3";//  ###  取引通番(FROM)
    private static final String ARG_OPT6 = "-t1";//  ###  システム年月日(TO)
    private static final String ARG_OPT7 = "-t2";//  ###  サービス対応番号(TO)
    private static final String ARG_OPT8 = "-t3";//  ###  取引通番(TO)
    //  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;
    String OPTION3 = null;
    String OPTION4 = null;
    String OPTION5 = null;
    String OPTION6 = null;
    String OPTION7 = null;
    String OPTION8 = null;

    String CM_APWORK_DATE = null;
    int exit_cd = 0;
    //#  引数格納変数初期化
    String ARG_DCTT_PRMFILE = "";

    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ

        // プログラムIDを環境変数に設定
//        String CM_MYPRGID = args[0].substring(0, 9);
//        args[0]= CM_MYPRGID;
//        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);
        CM_MYPRGNAME = "(臨時)該当なし後付取引繰越処理";
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
            } else if (item.startsWith(ARG_OPT3)) {
                OPTION3 = item.substring(3);
            } else if (item.startsWith(ARG_OPT4)) {
                OPTION4 = item.substring(3);
            } else if (item.startsWith(ARG_OPT5)) {
                OPTION5 = item.substring(3);
            } else if (item.startsWith(ARG_OPT6)) {
                OPTION6 = item.substring(3);
            } else if (item.startsWith(ARG_OPT7)) {
                OPTION7 = item.substring(3);
            } else if (item.startsWith(ARG_OPT8)) {
                OPTION8 = item.substring(3);
            }
        }
        //###########################################
        //#  パラメータファイル名の取得
        //###########################################
        MainResultDto mainResultDto = cmABgprmBService.main(getExecuteBaseParam().add("cmOLdcttP"));

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
                .addEvn("ARG_DCTT_PRMFILE", ARG_DCTT_PRMFILE).addEvn("OPTION1", OPTION1).execute();
        if (shellExecuteDto.RTN10()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

        return Rtn_OK;
    }


}
