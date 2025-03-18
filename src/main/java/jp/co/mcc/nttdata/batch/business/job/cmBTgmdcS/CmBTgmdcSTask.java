package jp.co.mcc.nttdata.batch.business.job.cmBTgmdcS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfkbhB.CmABfkbhBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  GOOPON番号紐付ファイルチェック処理
 * #    プログラムID  ：  cmBTgmdcS
 * #
 * #    【処理概要】
 * #        本機能は、GOOPON番号紐付けファイル連携前に、重複チェック処理を行うものである。
 * #
 * #    【引数説明】
 * #        -i 入力ファイル名(パス含む)  ：指定した場合、通常出力ファイルチェック。省略時リカバリファイル取り込み
 * #       -debug                        :  デバッグモードでの実行（トレース出力機能が有効）
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  未処理のトランザクションが存在する状態で終了
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      40.00 :    2023/09/25 SSI.張シン  ： MCCM初版
 * #      40.01 :    2024/05/27 SSI.石阪    ： MSＧＯＯＰＯＮ番号紐付情報のDelete&Insert処理全体的に見直し
 * #                                           (前段の連携データ抽出ＳＱＬの適正化に伴う修正）
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2014 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTgmdcSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBService;

    @Autowired
    private CmABfkbhBServiceImpl cmABfkbhBService;

    @Autowired
    private CmABdbtrSTask cmABdbtrS;

    @Autowired
    private CmABdbl2STask cmABdbl2S;

    @Override
    public int taskExecuteCustom(String[] args) {
        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "GOOPON番号紐付ファイルチェック処理";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        int Rtn_WAR = 49;
        Rtn_NG = 99;

        String ARG_OPT1 = "-i";    //### 入力ファイル名
        String ARG_OPT3 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT4 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）

        //#  ファイル
        String RECOVER_FNAME = "gmdc_recovery_file";
        String ERROR_FNAME = "gmdc_error";
        String ERROR_RECOVER_FNAME = "gmdc_error_recovery";

        String S_GPMB_FILE = "KKMI0090.csv";
        String R_GPMB_FILE = "KKCR0090.csv";
        String D_KHIF_FILE = "KKIN0040.csv";

        //#TMPファイル
        String TEMP_FILE01 = CM_MYPRGID + "_01";
        String TEMP_FILE02 = CM_MYPRGID + "02";
        String TEMP_FILE04 = CM_MYPRGID + "04";
        String TEMP_FILE05 = CM_MYPRGID + "05";

        String TEMP_CTL_FILE = CM_MYPRGID + "_ctl";

        String CTLFileName = "ldrWSgooponhimotsuke";

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  引数セット
        //###########################################
        //#引数を退避しておく
        String[] ARG_ORG = args;

        //#初期化
        int RECOVERY_FLG = 1;

        //#  引数格納
        String OPTION1 = null;
        String OPTION2 = null;
        if (ARG_ORG.length != 0) {
            //# 引数の数繰り返す
            for (String ARG_VALUE : args) {
                String ARG_OPT = ARG_VALUE.substring(0, 2);

                // # -i入力ファイル名が指定された場合
                if (ARG_OPT1.equals(ARG_OPT)) {
                    OPTION1 = ARG_VALUE.substring(2);
                    RECOVERY_FLG = 0;
                    continue;
                } else if (ARG_OPT3.equals(ARG_VALUE) || ARG_OPT4.equals(ARG_VALUE)) {
                    OPTION2 = ARG_VALUE;
                    continue;
                } else {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }
        }

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistFile(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //cd $CM_APWORK_DATE

        //###########################################
        //# バッチ処理日付取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBService.main(getExecuteBaseParam());
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());
            return Rtn_NG;
        }

        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //################################################
        //#  ファイル種別判定
        //################################################
        int CHECKED_FLG = 0;
        String IN_FILE_NAME = null;
        String OUT_ERR_FNAME = null;
        int WSBATCH_EXIST_FLG = 0;
        // #リカバリファイルではない場合
        if (RECOVERY_FLG == 0) {
            // #ファイル名を設定
            IN_FILE_NAME = OPTION1;
            OUT_ERR_FNAME = ERROR_FNAME + "_" + BAT_YYYYMMDD + ".csv";

            //#ファイルチェック済みかどうかをチェック
            ShellExecuteDto shellExecuteDto01 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                    .defaultEvn(this)
                    .addEvn("CONNECT_SD", CONNECT_SD)
                    .execute();
            if (shellExecuteDto01.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto01.getResultData());
                if ("01005".equals(SQL_CD) || "12154".equals(SQL_CD) || "01017".equals(SQL_CD) || "".equals(SQL_CD)) {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("ＤＢ接続時に失敗しました。").FE());
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WSバッチ処理実行管理の取得に失敗しました。").FE());
                }
                return Rtn_NG;
            }

            WSBATCH_EXIST_FLG = Integer.valueOf(shellExecuteDto01.getResultData());
            if (WSBATCH_EXIST_FLG > 0) {
                ShellExecuteDto shellExecuteDto02 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                        .defaultEvn(this)
                        .addEvn("CONNECT_SD", CONNECT_SD)
                        .execute();
                if (shellExecuteDto02.RTN0()) {
                    String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto02.getResultData());
                    if ("01005".equals(SQL_CD) || "12154".equals(SQL_CD) || "01017".equals(SQL_CD) || "".equals(SQL_CD)) {
                        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("ＤＢ接続時に失敗しました。").FE());
                        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WSバッチ処理実行管理の取得に失敗しました。").FE());
                    }
                    return Rtn_NG;
                }

                String RTN_VAL = shellExecuteDto02.getResultData();
                // #当日の場合、チェック済みフラグを設定
                if (RTN_VAL.equals(BAT_YYYYMMDD)) {
                    //#チェック済みフラグを設定
                    CHECKED_FLG = 1;
                }
            }
        } else {
            // #ファイル名を設定
            IN_FILE_NAME = CM_APWORK_DATE + "/" + RECOVER_FNAME + ".csv";
            OUT_ERR_FNAME = ERROR_RECOVER_FNAME + "_" + BAT_YYYYMMDD + SYS_HHMMSS + ".csv";
        }

        //###########################################
        //# 入力ファイルの有件データチェック
        //###########################################
        long SIZE = FileUtil.countCharacters(IN_FILE_NAME);
        if (SIZE == 0) {
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M(IN_FILE_NAME + "空ファイルのため、処理スキップ").FI());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_OK;
        }

        //###########################################
        //# GOOPON番号の復号化（To商品DNA）
        //###########################################
        FileUtil.copyFile(IN_FILE_NAME, CM_APWORK_DATE + "/" + TEMP_FILE01+".csv");

        String INFILE_BASENAME = basename(IN_FILE_NAME);
        if (INFILE_BASENAME.equals(D_KHIF_FILE)) {
            MainResultDto cmABfkbhBResult = cmABfkbhBService.main(getExecuteBaseParam()
                    .P("SD").add("-i" + TEMP_FILE01 + ".csv").add("-c,").add("-n1,2").add(OPTION2));
            RTN = cmABfkbhBResult.exitCode;
            if (RTN != Rtn_OK) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("GOOPON番号復号化処理に失敗しました").FE());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
            FileUtil.mvFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv.tmp", CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv");
        }

        //###########################################
        //# 重複チェック
        //###########################################
        if (CHECKED_FLG == 0) {
            //###########################################
            //# ファイル内重複チェック
            //###########################################
            ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                    .defaultEvn(this)
                    .addEvn("TEMP_FILE01", TEMP_FILE01)
                    .addEvn("OUT_ERR_FNAME", OUT_ERR_FNAME)
                    .execute();
            FileUtil.mvFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv.tmp1", CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv");
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv.tmp2");

            //###########################################
            //#  テーブルTruncate：WSＧＯＯＰＯＮ番号紐付
            //###########################################
            RTN = cmABdbtrS.main(getExecuteBaseParam().sT("WSＧＯＯＰＯＮ番号紐付").sD("SD"));
            if (RTN != Rtn_OK) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WSＧＯＯＰＯＮ番号紐付のTruncateに失敗しました").FW());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                return Rtn_NG;
            }

            //##################################################
            //#  COPY実行   ：WSＧＯＯＰＯＮ番号紐付
            //##################################################
            if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv")) {
                ShellExecuteDto shellExecuteDto04 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                        .defaultEvn(this)
                        .addEvn("TEMP_FILE01", TEMP_FILE01)
                        .addEvn("CM_APWORK_DATE", CM_APWORK_DATE)
                        .addEvn("TEMP_CTL_FILE", TEMP_CTL_FILE)
                        .execute();
            }
            SIZE = FileUtil.countCharacters(CM_APWORK_DATE + "/" + TEMP_CTL_FILE + ".tmp");
            if (SIZE != 0) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("WSＧＯＯＰＯＮ番号紐付データロード"));
                RTN = cmABdbl2S.main(getExecuteBaseParam().sD().add("SD").sC().add(CTLFileName));
                if (RTN != Rtn_OK) {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FE());
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }

            //#############################################################
            //# 過去の履歴とのデータ重複チェック(リカバリファイルのみ）
            //# リカバリファイルはデータ重複エラーの場合、ここで処理処理終了
            //#############################################################
            if (RECOVERY_FLG == 1) {
                ShellExecuteDto shellExecuteDto05 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_05")
                        .defaultEvn(this)
                        .addEvn("TEMP_FILE02", TEMP_FILE02)
                        .execute();
                if (shellExecuteDto05.RTN0()) {
                    String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + "1.log");
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("重複レコードの抽出に失敗しました。SQLCODE=" + SQL_CD).FE());
                    return Rtn_NG;
                }


                FileUtil.copyFile(CM_APWORK_DATE + "/" + TEMP_FILE02 + ".csv", CM_APWORK_DATE + "/" + OUT_ERR_FNAME);
                FileUtil.deleteFile(CM_APWORK_DATE + "/" + TEMP_FILE02 + ".csv");

                //# エラーファイルが0byteの場合、削除
                if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + OUT_ERR_FNAME)) {
                    SIZE = FileUtil.countCharacters(CM_APWORK_DATE + "/" + OUT_ERR_FNAME);
                    if (SIZE == 0) {
                        FileUtil.deleteFile(CM_APWORK_DATE + "/" + OUT_ERR_FNAME);
                    }
                }

                if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + OUT_ERR_FNAME)) {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("リカバリファイルに重複データが発生").FE());
                    FileUtil.mvFile(IN_FILE_NAME, CM_APWORK_DATE + "/" + RECOVER_FNAME + "_error_" + BAT_YYYYMMDD + SYS_HHMMSS + ".csv");
                    return Rtn_NG;
                }
            }
            //#############################################################
            //# MSＧＯＯＰＯＮ番号紐付情報 を　ＷＳＧＯＰＯＮ番号紐付けの削除フラグに応じて反映する(Delete/Update/Insert)
            //#############################################################
            String UPD_SQL_FILE = CM_APWORK_DATE + "/" +"gmdc_update.sql";
            FileUtil.copyFile(CM_APSQL + "/" + CM_MYPRGID + "2.sql", UPD_SQL_FILE);

            //#############################################################
            //# WSバッチ処理実行管理更新
            //# 整合性を保つため、MSＧＯＯＯＮ番号紐付情報のDEL/UP/INS　および、WSバッチ実行管理は　一括Commitする
            //#############################################################
            if (RECOVERY_FLG == 0) {
                if (WSBATCH_EXIST_FLG > 0) {
                    FileUtil.writeFileByAppend(UPD_SQL_FILE, "update WSバッチ処理実行管理 set シーケンス番号= " + BAT_YYYYMMDD + " where " +
                            "機能ＩＤ='GMDC';");
                } else {
                    FileUtil.writeFileByAppend(UPD_SQL_FILE, "insert into WSバッチ処理実行管理 (シーケンス番号,機能ＩＤ) values (" + BAT_YYYYMMDD + ",'GMDC');");
                }
            }

            ShellExecuteDto shellExecuteDto06 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_06")
                    .defaultEvn(this)
                    .addEvn("UPD_SQL_FILE", UPD_SQL_FILE)
                    .execute();
            if (shellExecuteDto06.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + "4.log");
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("MSＧＯＯＰＯＮ番号紐付情報またはWSバッチ処理実行管理更新に失敗しました。SQLCODE=" + SQL_CD).FE());
                return Rtn_NG;
            }

            FileUtil.deleteFile(UPD_SQL_FILE);
        }

        //#############################################################
        //# ファイル作成
        //#############################################################

        //########## 引数指定なし（リカバリファイルチェック）の場合 ############
        if (RECOVERY_FLG == 1) {
            //#############################################################
            //# WSＧＯＯＰＯＮ番号紐付からリカバリファイル抽出する（対向システム連携項目の取得）
            //#############################################################
            ShellExecuteDto shellExecuteDto07 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_07")
                    .defaultEvn(this)
                    .addEvn("TEMP_FILE04", TEMP_FILE04)
                    .execute();
            if (shellExecuteDto07.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + "5.log");
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("リカバリファイル作成に失敗しました。SQLCODE=" + SQL_CD).FE());
                return Rtn_NG;
            }

            FileUtil.deleteFile(CM_APWORK_DATE + "/tmp_" + S_GPMB_FILE);
            FileUtil.deleteFile(CM_APWORK_DATE + "/tmp_" + R_GPMB_FILE);
            FileUtil.deleteFile(CM_APWORK_DATE + "/tmp_" + D_KHIF_FILE);

            ShellExecuteDto shellExecuteDto08 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_08")
                    .defaultEvn(this)
                    .addEvn("TEMP_FILE04", TEMP_FILE04)
                    .addEvn("S_GPMB_FILE", S_GPMB_FILE)
                    .addEvn("R_GPMB_FILE", R_GPMB_FILE)
                    .addEvn("D_KHIF_FILE", D_KHIF_FILE)
                    .execute();
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + TEMP_FILE04 + ".csv");
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + RECOVER_FNAME + ".csv");

            if (FileUtil.isExistFile(CM_APWORK_DATE + "/tmp_" + S_GPMB_FILE)) {
                SIZE = FileUtil.countCharacters(CM_APWORK_DATE + "/tmp_" + S_GPMB_FILE);
                if (SIZE != 0) {
                    FileUtil.mvFile(CM_APWORK_DATE + "/tmp_" + S_GPMB_FILE, CM_FILEWATSND + "/" + S_GPMB_FILE);
                }
            }

            if (FileUtil.isExistFile(CM_APWORK_DATE + "/tmp_" + R_GPMB_FILE)) {
                SIZE = FileUtil.countCharacters(CM_APWORK_DATE + "/tmp_" + R_GPMB_FILE);
                if (SIZE != 0) {
                    FileUtil.mvFile(CM_APWORK_DATE + "/tmp_" + R_GPMB_FILE, CM_FILEWATSND + "/" + R_GPMB_FILE);
                }
            }

            if (FileUtil.isExistFile(CM_APWORK_DATE + "/tmp_" + D_KHIF_FILE)) {
                SIZE = FileUtil.countCharacters(CM_APWORK_DATE + "/tmp_" + D_KHIF_FILE);
                if (SIZE != 0) {
                    MainResultDto cmABfkbhBResult = cmABfkbhBService.main(getExecuteBaseParam()
                            .P("SD").add("-i" + "tmp_" + D_KHIF_FILE).add("-c,").add("-n1,2").sE().add(OPTION2));
                    RTN = cmABfkbhBResult.exitCode;
                    if (RTN != Rtn_OK) {
                        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("GOOPON番号暗号化処理に失敗しました").FE());
                        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                        return Rtn_NG;
                    }
                    FileUtil.mvFile(CM_APWORK_DATE+"/"+"tmp_" + D_KHIF_FILE + ".tmp", CM_FILEWATSND + "/" + D_KHIF_FILE);
                }
            }
        }
        // ########## 引数指定あり（本対外側ファイルの）の場合 ############
        else {
            int BEFORE_CNT = FileUtil.countLines(IN_FILE_NAME);
            int AFTER_CNT = FileUtil.countLines(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv");

            if (BEFORE_CNT > AFTER_CNT) {
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M(IN_FILE_NAME + "に重複データが発生").FE());
            }

            if (INFILE_BASENAME.equals(D_KHIF_FILE)) {
                MainResultDto cmABfkbhBResult = cmABfkbhBService.main(getExecuteBaseParam()
                        .P("SD").add("-i" + TEMP_FILE01 + ".csv").add("-c,").add("-n1,2").sE().add(OPTION2));
                RTN = cmABfkbhBResult.exitCode;
                if (RTN != Rtn_OK) {
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("GOOPON番号暗号化処理に失敗しました").FE());
                    cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
                FileUtil.mvFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv.tmp", CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv");
            }

            //# リカバリファイルが存在する場合、合併する
            String TGT_FILE_NAME = basename(IN_FILE_NAME);
            if (FileUtil.isExistFile(CM_FILEWATSND + "/" + TGT_FILE_NAME)) {
                FileUtil.writeFileByAppend(FileUtil.readFile(CM_FILEWATSND + "/" + TGT_FILE_NAME), CM_APWORK_DATE + "/" + TEMP_FILE05 + "_1.csv");
                FileUtil.writeFileByAppend(FileUtil.readFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv"), CM_APWORK_DATE + "/" + TEMP_FILE05 + "_1.csv");
                FileUtil.mvFile(CM_FILEWATSND + "/" + TGT_FILE_NAME, CM_APWORK_DATE + "/" + TGT_FILE_NAME + "_recover");
            } else {
                FileUtil.mvFile(CM_APWORK_DATE + "/" + TEMP_FILE01 + ".csv", CM_APWORK_DATE + "/" + TEMP_FILE05 + "_1.csv");
            }

            ShellExecuteDto shellExecuteDto09 = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_09")
                    .defaultEvn(this)
                    .addEvn("TEMP_FILE05", TEMP_FILE05)
                    .execute();
            FileUtil.mvFile(IN_FILE_NAME, IN_FILE_NAME + "_befor");
            FileUtil.mvFile(CM_APWORK_DATE + "/" + TEMP_FILE05 + "_2.csv", IN_FILE_NAME);
        }

        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^"+CM_MYPRGID+"_*");

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
