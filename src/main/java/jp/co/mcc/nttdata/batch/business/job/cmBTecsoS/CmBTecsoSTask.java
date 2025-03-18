package jp.co.mcc.nttdata.batch.business.job.cmBTecsoS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfkbhB.CmABfkbhBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ECサイト購買履歴作成（随時）
 * #    プログラムID  ：  cmBTecsoS
 * #
 * #    【処理概要】
 * #        購買履歴データ（MK）ファイルを入力ファイルとし、
 * #        ECサイト購買履歴作成（随時）処理を行う。
 * #        「COPYでのデータロード」（cmABdbl2S）を使用し、入力ファイルのデータをワークテーブルに登録する
 * #        ワークテーブルデータより、SPSS、RetailCRM、商品DNA向け連動ファイルをそれぞれ作成する。
 * #        開始メッセージを出力、「cmABdbl2S」を起動、SPSS、RetailCRM、商品DNA向け連動ファイル作成ＳＱＬを実行、
 * #        終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 7
 * #
 * #    改定履歴
 * #      50.00 : 2024/01/12 SSI.川内 ：MCCMPH2初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2024 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTecsoSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    CmABdbl2STask cmABdbl2S;

    @Autowired
    CmABfkbhBServiceImpl cmABfkbhB;

    @Override
    public int taskExecuteCustom(String[] args) {
//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################
        CM_MYPRGNAME = "ECサイト購買履歴作成（随時）";

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//###########################################
//#  定数定義
//###########################################
        //          戻り値
        Rtn_OK = 10;
        Rtn_NG = 99;

        //          引数定義
        String ARG_OPT1 = "-DEBUG";                       //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";                       //  デバッグモードでの実行（トレース出力機能が有効）

        String DB_KBN = "SD";

//# 購買履歴データファイル
        String MK_KOBAI_FILE = "ec_uri";

        String MK_KOBAI_ZIP_FILE = "ECKK0050.zip";

//# 制御ファイル
        String CTL_FILE = "ldrWSKoubaiMK2";

//# 購買履歴ワークテーブル
        String WS_KOBAI_TBL = "WS購買履歴";

        String DNA_RENDO_FILE = "KKIN0020";
        String SPSS_RENDO_FILE = "KKMI0130";
        String CRM_RENDO_FILE = "KKCR0120";

//###########################################
//#  DB接続先
//###########################################
//        CONNECT_SD=${CM_USR_SD}/${CM_PSW_SD}@${CM_ORA_SID_SD}

//###########################################
//#  稼動ディレクトリ決定
//###########################################

//    # 作業ディレクトリファイル作成失敗
        setCM_APWORK_DATE();

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//###########################################
//#  引数の格納
//###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//          引数格納変数初期化

        String OPTION1 = "";

        //          引数格納

        for (String arg : args) {
            if (arg.equals(ARG_OPT1) || arg.equals(ARG_OPT2)) {
                OPTION1 = arg;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

//###########################################
//#  ctlファイル存在チェック
//###########################################
        String CHECK_FILE = CM_APSQL + "/loader/" + CTL_FILE + ".sql";
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTL_FILE + ".sqlファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//###########################################
//#  テーブルTruncate
//###########################################
        int RTN_TRUNCATE = cmABdbtrSTask.main(getExecuteBaseParam().sD(DB_KBN).sT(WS_KOBAI_TBL));
        if (Rtn_OK != RTN_TRUNCATE) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(WS_KOBAI_TBL + "のTruncateに失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//##################################################
//#  COPY実行(購買履歴（MK）ファイル)
//##################################################
//#  圧縮ファイル解凍
        String FILENOWRCV_FILE = CM_FILENOWRCV + "/" + MK_KOBAI_ZIP_FILE;
        if (FileUtil.isExistFile(FILENOWRCV_FILE)) {
            int RTN = ZipUtil.unzip(FILENOWRCV_FILE, CM_APWORK_DATE);
            if (RTN != 0) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（MK）ファイルファイル解凍　失敗[" + MK_KOBAI_ZIP_FILE + "]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        String FILE_NAME = CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".csv";
        if (FileUtil.isExistFile(FILE_NAME)) {
            long FILE_SIZE = FileUtil.contentLength(FILE_NAME);
            if (FILE_SIZE > 0) {
                String FILE_DATA = FileUtil.readFile( FILE_NAME).replaceAll("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat", FILE_DATA,SystemConstant.UTF8);
                int RTN = cmABdbl2S.main(getExecuteBaseParam().sD(DB_KBN).sC(CTL_FILE));
                if (Rtn_OK != RTN) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPY(ファイル：" + CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat)の実行に失敗しました").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（MK）ファイルのサイズは0").FI());
            }
        }


//###########################################
//# WS会員GOOPON番号登録
//###########################################
        ShellExecuteDto shellExecuteDto01 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this).execute();

        if (shellExecuteDto01.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ECサイト購買履歴作成（随時）処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//###########################################
//#  バッチ処理日日付の取得
//###########################################

        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = cmABgdatBResult.result;

//###########################################
//#  連動ファイル作成（商品DNA向け)
//###########################################
        ShellExecuteDto shellExecuteDto02 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("DNA_RENDO_FILE", DNA_RENDO_FILE).execute();
        if (shellExecuteDto02.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".tmp");

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + DNA_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//#  改行コードをCRLFに変換
        String DNA_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".tmp", SystemConstant.Shift_JIS, "$", "\r");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec", DNA_RENDO_FILE_DATA);
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".tmp");

//###########################################
//#  暗号化してmkecファイルに出力する
//###########################################
        RTN = cmABfkbhB.main(getExecuteBaseParam().P("SD").add("-i" + DNA_RENDO_FILE + ".mkec").add("-c,").add("-n7").add("-E").add(OPTION1)).exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル暗号化化処理に失敗しました[" + DNA_RENDO_FILE + ".mkec]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec.tem", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec");

//#  SJIS変換
        int b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + "_SJIS.mkec");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkecのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + "_SJIS.mkec", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec");


//#  処理件数取得
        Integer DATA_COUNT_DNA_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec");
        if (DATA_COUNT_DNA_RENDO == null) {
//                # 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + DNA_RENDO_FILE + ".mkec]  出力件数：[" + DATA_COUNT_DNA_RENDO + "]").FI());

//###########################################
//#  連動ファイル作成（SPSS向け)
//###########################################

        ShellExecuteDto shellExecuteDto03 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("SPSS_RENDO_FILE", SPSS_RENDO_FILE).execute();
        if (shellExecuteDto03.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".tmp");

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + SPSS_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        String DNA_SPSS_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".tmp", SystemConstant.Shift_JIS, ",$", "");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".tmp", DNA_SPSS_RENDO_FILE_DATA);

//#  改行コードをCRLFに変換
        DNA_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".tmp", SystemConstant.Shift_JIS, "$", "\r");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec", DNA_RENDO_FILE_DATA);
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".tmp");

//#  SJIS変換
        b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + "_SJIS.mkec");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkecのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + "_SJIS.mkec", CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec");

//#  処理件数取得
        Integer DATA_COUNT_SPSS_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec");
        if (DATA_COUNT_DNA_RENDO == null) {
//                # 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + SPSS_RENDO_FILE + ".mkec]  出力件数：[" + DATA_COUNT_SPSS_RENDO + "]").FI());

//###########################################
//#  連動ファイル作成（RetailCRM向け)
//###########################################
        ShellExecuteDto shellExecuteDto04 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("CRM_RENDO_FILE", CRM_RENDO_FILE).execute();
        if (shellExecuteDto04.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".tmp");

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + CRM_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        String DNA_CRM_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".tmp", SystemConstant.Shift_JIS, ",$", "");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".tmp", DNA_CRM_RENDO_FILE_DATA);

//#  改行コードをCRLFに変換
        DNA_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".tmp", SystemConstant.Shift_JIS, "$", "\r");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec", DNA_RENDO_FILE_DATA);
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".tmp");

//#  SJIS変換
        b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec", CM_APWORK_DATE + "/" + CRM_RENDO_FILE + "_SJIS.mkec");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkecのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + "_SJIS.mkec", CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec");

//#  処理件数取得
        Integer DATA_COUNT_CRM_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec");
        if (DATA_COUNT_CRM_RENDO == null) {
//                # 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + CRM_RENDO_FILE + ".mkec]  出力件数：[" + DATA_COUNT_CRM_RENDO + "]").FI());

//###########################################
//#  退避処理(DNA向け連動ファイル)
//###########################################
//#  前回作成されたファイルが存在する場合、マージする
        FILE_NAME = CM_APRESULT + "/" + DNA_RENDO_FILE + ".mkec";
        if (FileUtil.isExistFile(FILE_NAME)) {
            String RESULT_STR = FileUtil.readFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec");
            FileUtil.writeFileByAppend(FILE_NAME, RESULT_STR, SystemConstant.UTF8);
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec");
//            #  前回作成されたファイルが存在しない場合、移動する
        } else {
            FileUtil.mvFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".mkec", CM_APRESULT + "/" + DNA_RENDO_FILE + ".mkec");
        }


//###########################################
//#  退避処理(SPSS向け連動ファイル)
//###########################################
//#  前回作成されたファイルが存在する場合、マージする
        FILE_NAME = CM_APRESULT + "/" + SPSS_RENDO_FILE + ".mkec";
        if (FileUtil.isExistFile(FILE_NAME)) {
            String RESULT_STR = FileUtil.readFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec");
            FileUtil.writeFileByAppend(FILE_NAME, RESULT_STR, SystemConstant.UTF8);
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec");
//            #  前回作成されたファイルが存在しない場合、移動する
        } else {
            FileUtil.mvFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".mkec", CM_APRESULT + "/" + SPSS_RENDO_FILE + ".mkec");
        }

//###########################################
//#  退避処理(RetailCRM向け連動ファイル)
//###########################################
//#  前回作成されたファイルが存在する場合、マージする
        FILE_NAME = CM_APRESULT + "/" + CRM_RENDO_FILE + ".mkec";
        if (FileUtil.isExistFile(FILE_NAME)) {
            String RESULT_STR = FileUtil.readFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec");
            FileUtil.writeFileByAppend(FILE_NAME, RESULT_STR, SystemConstant.UTF8);
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec");
//            #  前回作成されたファイルが存在しない場合、移動する
        } else {
            FileUtil.mvFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".mkec", CM_APRESULT + "/" + CRM_RENDO_FILE + ".mkec");
        }

//###################################################
//#  バックアップファイルと一時ファイルを削除
//###################################################
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".tmp");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat");

//###########################################
//#  終了メッセージをAPログに出力
//###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
