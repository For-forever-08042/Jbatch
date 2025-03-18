package jp.co.mcc.nttdata.batch.business.job.cmBTadcdS;

import jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbldS.CmABdbldSTask;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.NTRegexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  住所コードマスター設定シェル
 * #    プログラムID  ：  cmBTadcdS
 * #
 * #    【処理概要】
 * #       住所コードマスターファイルを読込み、住所コードマスターへ設定し、
 * #       $CM_APWORK_DATEディレクトリに、住所コード情報入れ替えステータスファイルを作成する。
 * #       開始メッセージを出力し、「cmABdbldS」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #                  なし
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/14 SSI.横山直人  ： 初版
 * #
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmBTadcdSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmABdbldSTask cmABdbldSTask;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    private final static String ARG_OPT1 = "-T";  //    ###  テーブル名
    private final static String ARG_OPT2 = "-D";  //    ###  接続DB
    private final static String ARG_OPT3 = "-F";  //    ###  入力dmpファイル名文字列
    private final static String ARG_OPT4 = "-P";  //    ###  対象テーブル名に付加する可変部の設定パターン

    //  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;
    String OPTION3 = null;
    String OPTION4 = null;
    String CM_APWORK_DATE;

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "住所コードマスター設定";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));


        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //#  システム日付と時刻
        String SYS_YYYYMMDDHHMMSS = DateUtil.getYYYYMMDDHHMMSS();

        String CTL_DIR = "loader";
        String CTL_FILE = "ldrMMaddcode.sql";
        String MOTO_ADDRESS_STATUS_FILE = "ldrMMaddcode.log_utf8";

        String ADDRESS_FILE = "ADDRESS_MASTER";
        String ADDRESS_FILE_DAT = "ADDRESS_MASTER.dat";
        String ADDRESS_FILE_TMP = "ADDRESS_MASTER.tmp";
        String ADDRESS_STATUS_FILE = "ADDRESS.STS";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################

        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(CmABfuncLServiceImpl.CM_APWORK) + "/" + SYS_YYYYMMDD;
            setenv(CmABfuncLServiceImpl.CM_APWORK_DATE, CM_APWORK_DATE);
        }

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            APLOG_WT("稼動ディレクトリ作成エラー", FE);
            return Rtn_NG;
        }

        //###########################################
        //#  住所コードマスターファイルの存在チェック
        //###########################################
        String CM_FILENOWRCV = getenv(C_aplcom1Service.CM_FILENOWRCV);
        if (!FileUtil.isExistFile(CM_FILENOWRCV + "/" + ADDRESS_FILE)) {
            APLOG_WT("住所コードマスターファイルなし", FI);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_NG;
        }

        //################################################################
        //#  住所コードマスターファイルロード用制御ファイルの存在チェック
        //################################################################
        if (!FileUtil.isExistFile(CM_APSQL + "/" + CTL_DIR + "/" + CTL_FILE)) {
            APLOG_WT("住所コードマスターファイルロード用制御ファイルなし ", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  住所コードマスターファイルの変換
        //############################################
        if (FileUtil.mvFile(CM_FILENOWRCV + "/" + ADDRESS_FILE, CM_APWORK_DATE + "/" + ADDRESS_FILE_TMP) !=0) {
            APLOG_WT("住所コードマスターファイル名変換失敗", FW);
            return Rtn_NG;
        }


        //#  空行を除去
        String ADDRESS_FILE_TMP_STR = Arrays.stream(FileUtil.readFileToUtf8(CM_APWORK_DATE + "/" +ADDRESS_FILE_TMP).replaceAll("\r\n","\n").split("\n")).filter(item -> NTRegexUtil.any("[0-9]", item)).collect(Collectors.joining("\n"));
        FileUtil.writeFile( CM_APWORK_DATE + "/" + ADDRESS_FILE_DAT,ADDRESS_FILE_TMP_STR,SystemConstant.Shift_JIS);

        EnvironmentConstant.CM_APWORK_DATE=CM_APWORK_DATE;
        //#####################################################
        //#  COPYで住所コードマスターファイルをロードする
        //#####################################################
        int RTN = cmABdbldSTask.main(getExecuteBaseParam().add("-C").add("ldrMMaddcode").add("-D").add("MD"));
        if (RTN != Rtn_OK) {
            APLOG_WT("住所コードマスターファイルロード失敗 ", FW);
            return Rtn_NG;
        }

        //#####################################################
        //#  住所コード情報入れ替えステータスファイルをコピー
        //#####################################################
        if (!FileUtil.copyFile(CM_APWORK_DATE + "/" + MOTO_ADDRESS_STATUS_FILE, CM_APWORK_DATE + "/" + ADDRESS_STATUS_FILE)) {
            APLOG_WT("住所コード情報入れ替えステータスファイルコピー失敗 ", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME));

        return Rtn_OK;
    }

    @Override
    public void APLOG_WT(String... args) {
        IFS = "@";
        cmABaplwBServiceImpl.main(getExecuteBaseParam().add(args));
        IFS = getenv(CmABfuncLServiceImpl.BKUP_IFS);
    }


}
