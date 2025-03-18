package jp.co.mcc.nttdata.batch.business.job.cmBTstnbS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgmtxB.CmABgmtxBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABmailS.CmABmailSTask;
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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  店舗別入会者数作成シェル
 * #    プログラムID  ：  cmBTstnbS040
 * #
 * #    【処理概要】
 * #     店舗別入会者数ファイル作成
 * #     を行うものである。
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
 * #      40.00 : 2023/03/17 SSI.申：MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTstnbSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmABgmtxBServiceImpl cmABgmtxBServiceImpl;

    @Autowired
    CmABmailSTask cmABmailSTask;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "店舗別入会者数作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付と時刻
        String SYS_YYYYMMDDHHMMSS = DateUtil.getYYYYMMDDHHMMSS();

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-DEBUG";   //                    ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";//                      ###  デバッグモードでの実行（トレース出力機能が有効）
        //
        //#  出力ファイル
        String STNB_O = "STORE_MEMBER";
        //
        //#  sqlファイル名
        String SQLFile = "cmBTstnbS";


        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
                if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                    // 作業ディレクトリファイル作成失敗
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }
        }

        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        //#  引数格納

        for (int i = 0; i < args.length; i++) {
            String ARG_VALUE = args[i];
            if (ARG_VALUE.equals(ARG_OPT1) || ARG_VALUE.equals(ARG_OPT2)) {
                OPTION1 = ARG_VALUE;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################

        MainResultDto BAT_YYYYMMDD = cmABgdatBServiceImpl.main(getExecuteBaseParam());

        if (BAT_YYYYMMDD.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());
            return Rtn_NG;
        }

        //###########################################
        //#  店舗別入会者数作成ファイル出力
        //###########################################
        //# SQL実行結果をファイルに出力
        //# 出力ファイル名
        ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("CM_APSQL", getenv(C_aplcom1Service.CM_APSQL))
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD.result)
                .addEvn("STNB_O", STNB_O)
                .addEvn("SYS_YYYYMMDDHHMMSS", SYS_YYYYMMDDHHMMSS)
                .addEvn("SQLFile", SQLFile)
                .execute();

        if (shellExecuteDto1.RTN0()) {
            String SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + STNB_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv");
            // SQL_CD = Arrays.stream(SQL_CD.replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 9)).collect(Collectors.joining("\n"));
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗別入会者数取得処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#  改行コードをCRLFに変換
        String STNB_O_DATE = FileUtil.readFile(CM_APWORK_DATE + "/" + STNB_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv").replaceAll("$", "\r");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + STNB_O + "_" + SYS_YYYYMMDDHHMMSS + ".csv", STNB_O_DATE);
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + STNB_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv");

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
