package jp.co.mcc.nttdata.batch.business.job.cmBTecciS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTecciB.CmBTecciBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ＥＣ会員情報連動処理
 * #    プログラムID  ：  cmBTecciS
 * #
 * #    【処理概要】
 * #       MM顧客情報、MM顧客属性情報、MM顧客企業別属性情報、
 * #       MSカード情報、TS利用可能ポイント情報から、
 * #       ＥＣ連動用にファイル作成する処理「ＥＣ会員情報連動（cmBTecciB）」
 * #       を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTecciB」を起動、
 * #       終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -o顧客情報ファイル名 :（必須）出力ファイルの物理ファイル名（$CM_APWORK）
 * #       -DEBUG               : デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug               : デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2013/04/24 SSI.上野  ： 初版
 * #      1.01 :   2013/05/07 SSI.上野  ： 前日分EC会員情報連動処理が異常終了の場合
 * #                                       前日連動対象分も含めての連動とする
 * #      2.00 :   2016/08/05 SSI.田頭　： C-MAS対応
 * #                                        e-mail受信可否、ＤＭ受信可否の追加
 * #      40.00:   2022/10/11 SSI.申    ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTecciSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    private CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    private CmBTecciBServiceImpl cmBTecciBService;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ＥＣ会員情報連動";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-DEBUG";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-o";           //###  出力ファイル名

        //#  EC会員情報連動抽出エラーファイル名
        String ECCI_ERR_FILE = CM_APWORK + "/ECCI_ERROR";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                //# エラーファイル作成
                FileUtil.createFolder(ECCI_ERR_FILE, true);

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            //# エラーファイル作成
            FileUtil.createFolder(ECCI_ERR_FILE, true);

            return Rtn_NG;
        }

        //############################################
        //##  引数格納変数初期化
        //############################################
        String OPTION1 = "";
        String OPTION2 = "";

        //############################################
        //##  引数格納
        //############################################
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            } else if (StringUtils.startsWith(arg, ARG_OPT3)) {
                OPTION2 = arg;
            }
        }

        //# 入力ファイル名
        String OUT_FILE = "";
        if (OPTION2.length() > 2) {
            OUT_FILE = OPTION2.substring(2);
        }

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();

        //###########################################
        //# WS顧客番号をTRUNCATE
        //###########################################
        int RTN = cmABdbtrSTask.main(getExecuteBaseParam().sT("WS顧客番号").sD("SD"));
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WS顧客番号のTRUNCATEに失敗しました").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            //# エラーファイル作成
            FileUtil.createFolder(ECCI_ERR_FILE, true);

            return Rtn_NG;
        }

        //###########################################
        //# WM顧客番号をTRUNCATE
        //###########################################
        RTN = cmABdbtrSTask.main(getExecuteBaseParam().sT(" WM顧客番号").sD("MD"));
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WM顧客番号のTRUNCATEに失敗しました").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            //# エラーファイル作成
            FileUtil.createFolder(ECCI_ERR_FILE, true);

            return Rtn_NG;
        }

        String BAT_YYYYMMDD = "";
        //###########################################
        //#  EC会員情報連動抽出エラーファイル存在チェック
        //###########################################
        if (!FileUtil.isExistFile(ECCI_ERR_FILE)) {
            //#エラーファイルなし:前日日付取得
            MainResultDto mainResultDto = cmABgdatBService.main(getExecuteBaseParam().add("-DY"));
            RTN = mainResultDto.exitCode;
            if (RTN != Rtn_OK) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日（前日）取得に失敗しました").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                //# エラーファイル作成
                FileUtil.createFolder(ECCI_ERR_FILE, true);

                return Rtn_NG;
            }
            BAT_YYYYMMDD = mainResultDto.result;
        } else {
            //#エラーファイルあり:２日前日付取得
            MainResultDto mainResultDto = cmABgdatBService.main(getExecuteBaseParam().add("-DY2"));
            RTN = mainResultDto.exitCode;
            if (RTN != Rtn_OK) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日（２日前）取得に失敗しました").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
            BAT_YYYYMMDD = mainResultDto.result;

            //#エラーファイル削除
            FileUtil.deleteFile(ECCI_ERR_FILE);
        }

        APLOG_WT("起動引数[" + OPTION2 + " " + OPTION1 + " -date" + BAT_YYYYMMDD + "]", FI);
        //###########################################
        //#  プログラム実行
        //###########################################
        MainResultDto mainResultDto = cmBTecciBService.main(getExecuteBaseParam().add(OPTION2).add(OPTION1).add("-date" + BAT_YYYYMMDD));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            //# エラーファイル作成
            FileUtil.createFolder(ECCI_ERR_FILE, true);

            return Rtn_NG;
        }

        ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .addEvn("CM_APWORK_DATE", CM_APWORK_DATE)
                .addEvn("OUT_FILE", OUT_FILE)
                .addEvn("TEMP_FILE1", TEMP_FILE1)
                .execute();


        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
