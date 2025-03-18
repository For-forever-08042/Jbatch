package jp.co.mcc.nttdata.batch.business.job.cmBTposmS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfkbhB.CmABfkbhBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTdlemB.CmBTdlemBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  POS売上明細連携
 * #    プログラムID  ：  cmBTposmS
 * #
 * #    【処理概要】
 * #        POS売上明細（MK）ファイル、POS売上明細（CF）ファイルを入力ファイルとし、
 * #        POS売上明細連携処理を行う処理
 * #        「COPYでのデータロード」（cmABdbl2S）を使用し、入力ファイルのデータをワークテーブルに登録する。
 * #        「連動ファイル作成」(cmABteraB)を使用し、ワークテーブルデータより
 * #        商品DNA、SPSS、RetailCRM向け連動ファイルを作成する。
 * #        商品DNA向け連動ファイルは、「圧縮処理」(cmABfzipS)を使用してZIP圧縮する。
 * #        開始メッセージ出力、「cmABdbl2S」と「cmABteraB」を起動し、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6.1
 * #
 * #    改定履歴
 * #      40.00 : 2022/11/23 SSI.一ノ瀬：MCCM 初版
 * #      40.00 : 2023/01/28 飯塚：(H016)RetailCRM向けの連動ファイル作成処理を追加し、CF会員除外の条件を追加
 * #                               (H038)GOOPON向けの連動ファイル作成処理を追加し、下記項目を追加
 * #                                     ＭＤ企業コード
 * #                                     店舗名
 * #      41.00 : 2023/06/08 SSI.小俣  ：MCCMPH2
 * #                                     ・商品DNA向けのID付きジャーナルデータ作成時、調剤データを除外する
 * #                                     ・GOOPON向けの連動ファイルの作成機能を削除する
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTposmSTask extends NtBasicTask {

    @Autowired
    CmABfkbhBServiceImpl cmABfkbhB;
    @Autowired
    CmABdbtrSTask cmABdbtrS;
    @Autowired
    CmABdbl2STask cmABdbl2S;
    @Autowired
    CmABfzipSTask cmABfzipS;
    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTdlemBServiceImpl cmBTdlemB;
    @Autowired
    CmABgdldBServiceImpl cmABgdldB;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));


        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
        String SYS_YYYYMM = DateUtil.getYYYYMM();

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 99;

        String ARG_OPT1 = "-DEBUG";//  ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";// ###  デバッグモードでの実行（トレース出力機能が有効）

        int MK_FLG = 0;
        //#  引数定義
        String DB_KBN = "SD";
        String FILE_NAME = "PCS_TR_012";
        String CF_ZIP_FILE = "^.{14}_HC0140.zip";
        String MK_ZIP_FILE = "PSKK0020.zip";
        String JOURNAL_RENKEI = "KKIN0010";
        String URIAGE_RENKEI_SPSS = "KKMI0050";
        String URIAGE_Retail = "KKCR0060";
        String WS_URIAGE_WK = "WS売上明細";
        String KAIIN_JOUHOU_WK = "WS売上明細会員ＧＯＯＰＯＮ番号";
        String JRE_FILE_ID = "D_POSM";//  ## 商品DNA向け(okファイル)
        String URS_FILE_ID = "S_POSM";//  ## SPSS向け(okファイル)
        String URG_FILE_ID = "G_POSM";//  ## GOOPON向け(okファイル)
        String URE_FILE_ID = "R_POSM";//  ## RetailCRM向け(okファイル)
//        #テンプラリファイル名
        String TEMPO_FILE_NAME = CM_MYPRGID + "_CF.dat";
        String CTLFileName1 = "ldrWSUriageMK";
        String CTLFileName2 = "ldrWSUriageCF";


        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //cd $CM_APWORK_DATE

        //###########################################
        //#  引数の格納
        //###########################################
        if (args.length > 1) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        //#  引数格納変数初期化
        String OPTION1 = "";
        //#  引数格納
        for (String ARG_VALUE : args) {
            if (StringUtils.equals(ARG_VALUE, ARG_OPT1) || StringUtils.equals(ARG_VALUE, ARG_OPT2)) {
                OPTION1 = ARG_VALUE;
            } else {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  sqlファイル存在チェック
        //###########################################
        String CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName1 + ".sql";
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName1 + ".sqlファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName2 + ".sql";
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName2 + ".sqlファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //#  POS売上明細（MK）入力ファイル解凍する
        //###########################################
        String CM_FILENOWRCV = getenv(C_aplcom1Service.CM_FILENOWRCV);
        if (FileUtil.isExistFile(CM_FILENOWRCV + "/" + MK_ZIP_FILE)) {
            if (ZipUtil.unzip(CM_FILENOWRCV + "/" + MK_ZIP_FILE, CM_APWORK_DATE) != 0) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（MK）ファイル解凍　失敗[" + MK_ZIP_FILE + "]").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        int SIZE_MK = 0;
        if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + FILE_NAME + ".csv")) {
            SIZE_MK = FileUtil.countLines(CM_APWORK_DATE + "/" + FILE_NAME + ".csv");
        }

        //###########################################
        //#  復号化してTMPファイルに出力する
        //###########################################
        if (SIZE_MK > 0) {
            MainResultDto resultDto = cmABfkbhB.main(getExecuteBaseParam().P("MP2").add("-i" + FILE_NAME + ".csv").add("-c,").add("-n7,40").add(OPTION1));
            if (Rtn_OK != resultDto.exitCode) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル復号化処理に失敗しました[" + FILE_NAME + ".csv]").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
            MK_FLG = 1;
        } else {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（MK）" + FILE_NAME + "ファイルのサイズは0").FI());
        }

        //###########################################
        //#  テーブルTruncate
        //###########################################
        int RTN = cmABdbtrS.main(getExecuteBaseParam().sT().add(WS_URIAGE_WK).sD().add(DB_KBN));
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(WS_URIAGE_WK + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            //  DELETE_LOAD_DATA();
            //  DELETE_UNZIPPED_FILES();
            //  COPY_ERR_FILE();
            return Rtn_NG;
        }


        //##################################################
        //#  COPY実行(POS売上明細（MK）ファイル)
        //##################################################
        if (MK_FLG == 1) {
            if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + FILE_NAME + ".csv.tmp")) {
                // #  改行コードからCRを削除
                FileUtil.writeFile(CM_APWORK_DATE + "/" + FILE_NAME + ".dat", FileUtil.readFile(CM_APWORK_DATE + "/" + FILE_NAME + ".csv.tmp").replaceAll("\r", ""));
            }
            Integer SIZE = FileUtil.countLines(CM_APWORK_DATE + "/" + FILE_NAME + ".dat");
            if (SIZE != null && SIZE != 0) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（MK）ファイルのデータロード"));
                RTN = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFileName1));
                if (Rtn_OK != RTN) {
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FE());
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
                FileUtil.deleteFileByRegx(CM_APWORK_DATE, CTLFileName1 + "*.sql");
            }
        }

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + FILE_NAME + ".dat");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + FILE_NAME + ".csv.tmp");
        //##################################################
        //#  COPY実行(POS売上明細（CF）ファイル)
        //##################################################
        ArrayList<String> CF_ZIP_FILE_DATA = FileUtil.findByRegex(CM_FILENOWRCV, CF_ZIP_FILE);
        for (String ZIP_F_NAME : CF_ZIP_FILE_DATA) {
            if (ZipUtil.unzip(ZIP_F_NAME, CM_APWORK_DATE) != 0) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（CF）ファイル解凍　失敗[" + ZIP_F_NAME + "]").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }

            ArrayList<String> F_NAME_DATA = FileUtil.findByRegex(CM_APWORK_DATE, "S5021_.{4}_.{6}_.{14}.csv");
            for (String F_NAME : F_NAME_DATA) {
                // # 入力ファイルサイズチェック
                long FILE_SIZE = 0;
                FILE_SIZE = FileUtil.contentLength(F_NAME);
                if (FILE_SIZE == 0) {
                    //# 処理対象のファイルを削除
                    FileUtil.deleteFile(F_NAME);
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（CF）" + F_NAME + "ファイルのサイズは0").FI());
                    continue;
                }
                //# 会員番号補正
                String F_NAME_T = basename(F_NAME);
                RTN = cmABfkbhB.main(getExecuteBaseParam().P("NO").add("-i" + F_NAME_T).add("-c,").add("-n7").add(OPTION1)).exitCode;
                if (RTN != Rtn_OK) {
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("会員番号補正処理に失敗しました[" + F_NAME + "]").FE());
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }

                //  #  改行コードからCRを削除
                FileUtil.writeFile(CM_APWORK_DATE + "/" + TEMPO_FILE_NAME,
                        FileUtil.readFile(F_NAME + ".tmp").replaceAll("\r", ""), SystemConstant.Shift_JIS);
                //# COPYでロード
                RTN = cmABdbl2S.main(getExecuteBaseParam().sC().add(CTLFileName2).sD().add(DB_KBN));
                if (RTN != Rtn_OK) {
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("POS売上明細（CF）ファイルの取り込みに失敗しました").FE());
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
                // # 処理対象のファイルを削除
                FileUtil.deleteFile(F_NAME);
                FileUtil.deleteFile(F_NAME + ".tmp");
                FileUtil.deleteFileByRegx(CM_APWORK_DATE, CTLFileName2 + "*.sql");
            }
        }
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + TEMPO_FILE_NAME);

        //###########################################
        //# WS売上明細会員ＧＯＯＰＯＮ番号登録
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTposmS").M("POS売上明細連携処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  バッチ処理日日付の取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam());
        RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = cmABgdatBResult.result;

        //#####################**************************************************************######################
        //###########################################
        //#  連動ファイル作成（商品DNA向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("JOURNAL_RENKEI", JOURNAL_RENKEI)
                .execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csv");
            APLOG_WT("連動ファイル作成（商品DNA向け)エラー ", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  暗号化してcsvファイルに出力する
        //###########################################
        RTN = cmABfkbhB.main(getExecuteBaseParam().P("SD").add("-i" + JOURNAL_RENKEI + ".csv").add("-c,").add("-n7").sE().add(OPTION1)).exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル暗号化化処理に失敗しました[" + JOURNAL_RENKEI + ".csv]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        FileUtil.mvFile(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csv.tmp", CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csv");
        //#  SJIS変換
        int RNT = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + JOURNAL_RENKEI +
                ".csv", CM_APWORK_DATE + "/" + JOURNAL_RENKEI + "_SJIS.csv");
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csvのSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + "_SJIS.csv", CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csv");

        //#  処理件数取得
        Integer DATA_COUNT_JOURNAL = FileUtil.countLines(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".csv");
        if (DATA_COUNT_JOURNAL == null) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + JOURNAL_RENKEI + ".csv]  出力件数：[" + DATA_COUNT_JOURNAL + "]").FI());

        //###########################################
        //#  圧縮処理
        //###########################################
        RTN = cmABfzipS.main(getExecuteBaseParam().sO().add(CM_APWORK_DATE).sZ().add(JOURNAL_RENKEI + ".zip").sD().add(CM_APWORK_DATE).sI().add(JOURNAL_RENKEI + ".csv").DEL());
        if (RTN != Rtn_OK) {
            APLOG_WT("対象ディレクトリの圧縮に失敗[" + JOURNAL_RENKEI + ".zip]", FW);
            return Rtn_NG;
        }

        //#####################**************************************************************######################
        //###########################################
        //#  連動ファイル作成（SPSS向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("URIAGE_RENKEI_SPSS", URIAGE_RENKEI_SPSS)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".csv");
            APLOG_WT("連動ファイル作成（SPSS向け)エラー ", FW);
            return Rtn_NG;
        }

//        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_08").defaultEvn(this)
//                .addEvn("URIAGE_RENKEI_SPSS", URIAGE_RENKEI_SPSS).execute();
        String URIAGE_RENKEI_SPSS_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS +
                ".csv", FileUtil.getCharset(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".csv"), ",$", "\n");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".csv", URIAGE_RENKEI_SPSS_DATA);
        RNT = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS +
                ".csv", CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + "_SJIS.csv");
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + "csvのSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + "_SJIS.csv", CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".csv");

        //#  処理件数取得
        Integer DATA_COUNT_URIAGE_RENKEI = FileUtil.countLines(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".csv");
        if (DATA_COUNT_URIAGE_RENKEI == null) {
            // # 処理件数取得失敗
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + URIAGE_RENKEI_SPSS + ".csv]  出力件数：[" + DATA_COUNT_URIAGE_RENKEI + "]").FI());


        //###########################################
        //#  圧縮処理
        //###########################################
        RTN = cmABfzipS.main(getExecuteBaseParam().sO().add(CM_APWORK_DATE).sZ().add(URIAGE_RENKEI_SPSS + ".zip").sD().add(CM_APWORK_DATE).sI().add(URIAGE_RENKEI_SPSS + ".csv").DEL());
        if (RTN != Rtn_OK) {
            APLOG_WT("対象ディレクトリの圧縮に失敗[" + URIAGE_RENKEI_SPSS + ".zip]", FW);
            return Rtn_NG;
        }

        //#####################**************************************************************######################

        //###########################################
        //#  連動ファイル作成（RetailCRM向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("URIAGE_Retail", URIAGE_Retail)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + URIAGE_Retail + ".csv");
            APLOG_WT("連動ファイル作成（RetailCRM向け)エラー ", FW);
            return Rtn_NG;
        }
        String URIAGE_Retail_DATA = FileUtil.readFileLineRpByRegx(CM_APWORK_DATE + "/" + URIAGE_Retail + ".csv", SystemConstant.Shift_JIS, ",$", "");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + URIAGE_Retail + ".csv", URIAGE_Retail_DATA);
        RNT = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS +
          ".csv", CM_APWORK_DATE + "/" + URIAGE_Retail + "_SJIS.csv");
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + URIAGE_Retail + "csvのSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
        return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + URIAGE_Retail + "_SJIS.csv", CM_APWORK_DATE + "/" + URIAGE_Retail + ".csv");
        //#  処理件数取得
        Integer DATA_COUNT_URIAGE_Retai = FileUtil.countLines(CM_APWORK_DATE + "/" + URIAGE_Retail + ".csv");
        if (DATA_COUNT_URIAGE_Retai == null) {
            // # 処理件数取得失敗
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + URIAGE_Retail + ".csv]  出力件数：[" + DATA_COUNT_URIAGE_Retai + "]").FI());

        //###########################################
        //#  圧縮処理
        //###########################################
        RTN = cmABfzipS.main(getExecuteBaseParam().sO().add(CM_APWORK_DATE).sZ().add(URIAGE_Retail + ".zip").sD().add(CM_APWORK_DATE).sI().add(URIAGE_Retail + ".csv").DEL());
        if (RTN != Rtn_OK) {
            APLOG_WT("対象ディレクトリの圧縮に失敗[" + URIAGE_Retail + ".zip]", FW);
            return Rtn_NG;
        }


        //###########################################
        //# 圧縮ファイルは「CM_FILEWATSND」に移動
        //# 送信連動用ファイル作成
        //###########################################
        //##########################圧縮ファイル移動
        //#####圧縮ファイル移動(商品DNA向け)
        RTN = FileUtil.mvFile(CM_APWORK_DATE + "/" + JOURNAL_RENKEI + ".zip", CM_FILEWATSND + "/" + JOURNAL_RENKEI + ".zip");
        if (RTN != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイル移動エラー　ファイル名=" + JOURNAL_RENKEI + ".zip　STATUS" +
                    "=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#####圧縮ファイル移動(SPSS向け)
        RTN = FileUtil.mvFile(CM_APWORK_DATE + "/" + URIAGE_RENKEI_SPSS + ".zip", CM_FILEWATSND + "/" + URIAGE_RENKEI_SPSS + ".zip");
        if (RTN != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイル移動エラー　ファイル名=" + URIAGE_RENKEI_SPSS + ".zip　STATUS" +
                    "=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#####圧縮ファイル移動(RetailCRM向け)
        RTN = FileUtil.mvFile(CM_APWORK_DATE + "/" + URIAGE_Retail + ".zip", CM_FILEWATSND + "/" + URIAGE_Retail + ".zip");
        if (RTN != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイル移動エラー　ファイル名=" + URIAGE_Retail + ".zip　STATUS=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //##########################送信連動用ファイル作成
        //#####送信連動用ファイル作成(商品DNA向け)
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_05")
                .defaultEvn(this)
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("JOURNAL_RENKEI", JOURNAL_RENKEI)
                .addEvn("JRE_FILE_ID", JRE_FILE_ID)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=" + JRE_FILE_ID + "_OK　STATUS=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_06")
                .defaultEvn(this)
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("URIAGE_RENKEI_SPSS", URIAGE_RENKEI_SPSS)
                .addEvn("URS_FILE_ID", URS_FILE_ID)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=" + URS_FILE_ID + "_OK　STATUS=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_07")
                .defaultEvn(this)
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("URIAGE_Retail", URIAGE_Retail)
                .addEvn("URE_FILE_ID", URE_FILE_ID)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=" + URE_FILE_ID + "_OK　STATUS=1").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
