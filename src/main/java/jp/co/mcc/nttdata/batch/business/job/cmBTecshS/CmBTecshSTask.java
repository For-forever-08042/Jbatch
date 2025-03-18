package jp.co.mcc.nttdata.batch.business.job.cmBTecshS;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfkbhB.CmABfkbhBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ECサイト購買履歴作成
 * #    プログラムID  ：  cmBTecshS
 * #
 * #    【処理概要】
 * #        購買履歴データ（MK）ファイル、購買履歴データ（CF）ファイルを入力ファイルとし、
 * #        ECサイト購買履歴作成処理を行う処理
 * #        「COPYでのデータロード」（cmABdbl2S）を使用し、入力ファイルのデータをワークテーブルに登録する
 * #        ワークテーブルデータより,「連動ファイル作成」(cmABterbB)を使用し商品DNA向け連動ファイル、
 * #        「連動ファイル作成」(cmABteraB)を使用しSPSS、RetailCRM、GOOPON向け連動ファイルをそれぞれ作成する。
 * #        商品DNA向け連動ファイルは、「圧縮処理」(cmABfzipS)を使用してZIP圧縮する。
 * #        開始メッセージ出力、「cmABdbl2S」と「cmABteraB」と「cmABterbB」を起動し、
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
 * #      40.00 : 2022/12/06 SSI.山口 ：MCCM 初版
 * #      41.00 : 2023/06/06 SSI.小俣 ：MCCMPH2
 * #      41.01 : 2024/01/12 SSI.川内 ：MCCMPH2 MG-234仕変対応
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTecshSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmABfkbhBServiceImpl cmABfkbhBServiceImpl;

    @Autowired
    private CmABdbl2STask cmABdbl2STask;
    @Autowired
    private CmABfzipSTask cmABfzipSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ECサイト購買履歴作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 99;

        //#  引数定義
        String ARG_OPT1 = "-DEBUG";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";       //###  デバッグモードでの実行（トレース出力機能が有効）

        String DB_KBN = "SD";

        //# 購買履歴データファイル
        //# 2023/12/19 MCCMPH2 MOD START
        //#MK_KOBAI_FILE="ec_uri"
        String MK_KOBAI_FILE = "ecgo_order_history";
        //# 2023/12/19 MCCMPH2 MOD END
        String CF_KOBAI_FILE = "ec_kobai";

        //# 2023/06/06 MCCMPH2 MOD START
        //#MK_KOBAI_ZIP_FILE="ECKK0050.zip"
        String MK_KOBAI_ZIP_FILE = "DEKK0050.zip";
        //# 2023/06/06 MCCMPH2 MOD END
        String CF_KOBAI_ZIP_FILE = "^ec_kobai_.{14}.zip";

        //# 制御ファイル
        String CTL_FILE1 = "ldrWSKoubaiMK";
        String CTL_FILE2 = "ldrWSKoubaiCF";

        //# 購買履歴ワークテーブル
        String WS_KOBAI_TBL = "WS購買履歴";

        String DNA_RENDO_FILE = "KKIN0020";
        String SPSS_RENDO_FILE = "KKMI0130";
        String CRM_RENDO_FILE = "KKCR0120";
        //# 2023/06/06 MCCMPH2 DEL START
        //#GOOPON_RENDO_FILE="KKGO0020_PART1"
        //# 2023/06/06 MCCMPH2 DEL END

        //###########################################
        //#  DB接続先
        //###########################################
        setConnectConf();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の格納
        //###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";

        //#  引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  sqlファイル存在チェック
        //###########################################
        String CHECK_FILE = CM_APSQL + "/loader/" + CTL_FILE1 + ".sql";
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTL_FILE1 + ".sqlファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTL_FILE2 + ".sql";
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTL_FILE2 + ".sqlファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  テーブルTruncate
        //###########################################
        int RTN = cmABdbtrSTask.main(getExecuteBaseParam().sT(WS_KOBAI_TBL).sD(DB_KBN));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(WS_KOBAI_TBL + "のTruncateに失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //##################################################
        //#  COPY実行(購買履歴（MK）ファイル)
        //##################################################
        //#  圧縮ファイル解凍
        String zipP = CM_FILENOWRCV + "/" + MK_KOBAI_ZIP_FILE;
        if (FileUtil.isExistFile(zipP)) {
            RTN = ZipUtil.unzip(zipP, CM_APWORK_DATE);
            if (RTN != 0) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（MK）ファイルファイル解凍　失敗[" + MK_KOBAI_ZIP_FILE + "]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        String MK_KOBAI_FILE_p = CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".csv";
        if (FileUtil.isExistFile(MK_KOBAI_FILE_p)) {
            long FILE_SIZE_1 = FileUtil.countCharacters(MK_KOBAI_FILE_p);
            if (FILE_SIZE_1 > 0) {
                //#  改行コードからCRを削除
                //cat ${MK_KOBAI_FILE}.csv | tr -d '\r' > ${MK_KOBAI_FILE}.dat 2> /dev/null
                String MK_KOBAI_FILE_DATA = FileUtil.readFile(MK_KOBAI_FILE_p);
                FileUtil.writeFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat", MK_KOBAI_FILE_DATA);

                RTN = cmABdbl2STask.main(getExecuteBaseParam().sD(DB_KBN).sC(CTL_FILE1));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPY(ファイル：" + CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat)の実行に失敗しました").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                    return Rtn_NG;
                }
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（MK）ファイルのサイズは0").FI());
            }
        }

        //##################################################
        //#  COPY実行(購買履歴（CF）ファイル)
        //##################################################

        ArrayList<String> zipFiles = FileUtil.findByRegex(CM_FILENOWRCV, CF_KOBAI_ZIP_FILE);
        for (String F_NAME : zipFiles) {
            RTN = ZipUtil.unzip(F_NAME, CM_APWORK_DATE);
            if (0 != RTN) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（CF）ファイルファイル解凍　失敗[" + F_NAME + "]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }

            //# 2023/06/06 MCCMPH2 DEL START
            //#    # 入力ファイルサイズチェック
            //#    FILE_SIZE_2=0
            //#    FILE_SIZE_2=`wc -c < ${CF_KOBAI_FILE}.csv`
            //#    if test ${FILE_SIZE_2} -eq 0
            //#    then
            //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -M"購買履歴（CF）ファイルのサイズは0" -FI
            //#        continue
            //#    fi
            //# 2023/06/06 MCCMPH2 DEL END

            String CF_KOBAI_FILE_P = CM_APWORK_DATE + "/" + CF_KOBAI_FILE + ".csv";
            if (FileUtil.isExistFile(CF_KOBAI_FILE_P)) {
                //# 2023/06/06 MCCMPH2 ADD START
                //# 入力ファイルサイズチェック
                long FILE_SIZE_2 = 0;
                 FILE_SIZE_2 = FileUtil.countCharacters(CF_KOBAI_FILE_P);
                if (0 == FILE_SIZE_2) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴（CF）ファイルのサイズは0").FI());
                    continue;
                }

                //#  改行コードからCRを削除
                //cat ${CF_KOBAI_FILE}.csv | tr -d '\r' > ${CF_KOBAI_FILE}.dat 2> /dev/null
                String CF_KOBAI_FILE_DATA = FileUtil.readFile(CF_KOBAI_FILE_P);
                FileUtil.writeFile(CM_APWORK_DATE + "/" + CF_KOBAI_FILE + ".dat", CF_KOBAI_FILE_DATA);

                RTN = cmABdbl2STask.main(getExecuteBaseParam().sC(CTL_FILE2).sD(DB_KBN));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPY(ファイル：" + F_NAME + ")の実行に失敗しました").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                    return Rtn_NG;
                }
            }
        }

        //###########################################
        //# WS会員GOOPON番号登録
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ECサイト購買履歴作成処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  バッチ処理日日付の取得
        //###########################################
        MainResultDto mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = mainResultDto.result;

        //###########################################
        //#  連動ファイル作成（商品DNA向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("DNA_RENDO_FILE", DNA_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + DNA_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  改行コードをCRLFに変換
        String DNA_RENDO_FILE_p = CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".tmp";
        FileUtil.fileCoverToCRLF(DNA_RENDO_FILE_p, CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv");
        FileUtil.deleteFile(DNA_RENDO_FILE_p);

        //###########################################
        //#  暗号化してcsvファイルに出力する
        //###########################################
        //cmABplodB cmABfkbhB -PSD -i${DNA_RENDO_FILE}.csv -c, -n7 -E ${OPTION1}
        mainResultDto = cmABfkbhBServiceImpl.main(getExecuteBaseParam().add("-PSD").add("-i"+DNA_RENDO_FILE+".csv").add("-c,").add("-n7").add("-E").add(OPTION1));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル暗号化化処理に失敗しました[" + DNA_RENDO_FILE + ".csv]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        FileUtil.mvFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv.tmp", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv");

        //#  SJIS変換
        int b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + "_SJIS.csv");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csvのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + "_SJIS.csv", CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv");

        //#  処理件数取得
        Integer DATA_COUNT_DNA_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + DNA_RENDO_FILE + ".csv");
        if (ObjectUtils.isEmpty(DATA_COUNT_DNA_RENDO)) {
            //# 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + DNA_RENDO_FILE + ".csv]  出力件数：[" + DATA_COUNT_DNA_RENDO + "]").FI());

        //###########################################
        //#  連動ファイル作成（SPSS向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("SPSS_RENDO_FILE", SPSS_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + SPSS_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        String s = CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csv_1";
        //sed -i 's/,$//' ${SPSS_RENDO_FILE}.csv_1
        //#  改行コードをCRLFに変換
        String SPSS_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(s, FileUtil.getCharset(s), ",$", BT_aplcomService.CRLF);
        FileUtil.writeFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csv", SPSS_RENDO_FILE_DATA);
        FileUtil.deleteFile(s);

        //#  SJIS変換
        b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csv", CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + "_SJIS.csv");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csvのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + "_SJIS.csv", CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csv");

        //#  処理件数取得
        Integer DATA_COUNT_SPSS_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + SPSS_RENDO_FILE + ".csv");
        if (ObjectUtils.isEmpty(DATA_COUNT_SPSS_RENDO)) {
            //# 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + SPSS_RENDO_FILE + ".csv]  出力件数：[" + DATA_COUNT_SPSS_RENDO + "]").FI());

        //###########################################
        //#  連動ファイル作成（RetailCRM向け)
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("CRM_RENDO_FILE", CRM_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイル(" + CRM_RENDO_FILE + ")作成に失敗しました ").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        s = CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csv_1";
        //sed -i 's/,$//' ${CRM_RENDO_FILE}.csv_1
        //#  改行コードをCRLFに変換
        String CRM_RENDO_FILE_DATA = FileUtil.readFileLineRpByRegx(s, FileUtil.getCharset(s), ",$", BT_aplcomService.CRLF);
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csv", CRM_RENDO_FILE_DATA);
        FileUtil.deleteFile(s);

        //#  SJIS変換
        b = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csv", CM_APWORK_DATE + "/" + CRM_RENDO_FILE + "_SJIS.csv");
        if (0 != b) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csvのSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }
        FileUtil.mvFile(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + "_SJIS.csv", CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csv");

        //#  処理件数取得
        Integer DATA_COUNT_CRM_RENDO = FileUtil.countLines(CM_APWORK_DATE + "/" + CRM_RENDO_FILE + ".csv");
        if (ObjectUtils.isEmpty(DATA_COUNT_CRM_RENDO)) {
            //# 処理件数取得失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + CRM_RENDO_FILE + ".csv]  出力件数：[" + DATA_COUNT_CRM_RENDO + "]").FI());

        //# 2023/06/06 MCCMPH2 DEL START
        //# ###########################################
        //# #  連動ファイル作成（GOOPON向け)
        //# ###########################################
        //# RTN_VAL=`sqlplus -s ${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
        //#          SET PAUSE OFF
        //#          SET AUTOCOMMIT OFF
        //#          SET EXITCOMMIT OFF
        //#          whenever sqlerror exit 1
        //#          @${CM_APSQL}/cmBTgoecS.sql ${BAT_YYYYMMDD} ${GOOPON_RENDO_FILE}.csv
        //# exit
        //# EOF
        //# `
        //# RTN=${?}
        //# if test "${RTN}" -ne 0
        //# then
        //#     SQL_CD=`cat ${GOOPON_RENDO_FILE}.csv | grep ORA- | sed s/ORA-/'\n'ORA-/ | grep "ORA-" |  cut -c5-9`
        //#     cmABplodB cmABaplwB -P${CM_MYPRGID} -M"連動ファイル(${CRM_RENDO_FILE})作成に失敗しました " -FE
        //#     cmABplodB cmABaplwB -P${CM_MYPRGID} -E${CM_MYPRGNAME} -FE
        //#     exit ${Rtn_NG}
        //# fi
        //# 2023/06/06 MCCMPH2 DEL END

        //###########################################
        //#  圧縮処理(DNA向け連動ファイル)
        //###########################################
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD START
        //#  随時JOBで作成されたファイルが存在する場合、マージする
        String DNA_RENDO_FILE_MKEC_P = CM_APRESULT + "/" + DNA_RENDO_FILE + ".mkec";
        String DNA_RENDO_FILE_TMP_P = CM_APRESULT + "/" + DNA_RENDO_FILE + ".tmp";
        String DNA_RENDO_FILE_CSV_P = CM_APRESULT + "/" + DNA_RENDO_FILE + ".csv";
        if (FileUtil.isExistFile(DNA_RENDO_FILE_MKEC_P)) {
            String DNA_RENDO_FILE_MKEC_Data = FileUtil.readFile(DNA_RENDO_FILE_MKEC_P);
            FileUtil.writeFile(DNA_RENDO_FILE_TMP_P, DNA_RENDO_FILE_MKEC_Data);
            String DNA_RENDO_FILE_CSV_Data = FileUtil.readFile(DNA_RENDO_FILE_CSV_P);
            FileUtil.writeFileByAppend(DNA_RENDO_FILE_TMP_P, DNA_RENDO_FILE_CSV_Data);
            FileUtil.mvFile(DNA_RENDO_FILE_TMP_P, DNA_RENDO_FILE_CSV_P);
        }
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD END

        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_FILEWATSND).sZ(DNA_RENDO_FILE + ".zip").sD(CM_APWORK_DATE).sI(DNA_RENDO_FILE + ".csv").DEL());
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + DNA_RENDO_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# 送信連動用ファイル作成
        //ls -l ${CM_FILEWATSND}/${DNA_RENDO_FILE}.zip > ${CM_FILEWATSND}/D_ECSH_OK 2>&1
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_05")
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("DNA_RENDO_FILE", DNA_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=D_ECSH_OK　STATUS=" + shellExecuteDto.result).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  圧縮処理(SPSS向け連動ファイル)
        //###########################################
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD START
        //#  随時JOBで作成されたファイルが存在する場合、マージする
        String SPSS_RENDO_FILE_MKEC_P = CM_APRESULT + "/" + SPSS_RENDO_FILE + ".mkec";
        String SPSS_RENDO_FILE_TMP_P = CM_APRESULT + "/" + SPSS_RENDO_FILE + ".tmp";
        String SPSS_RENDO_FILE_CSV_P = CM_APRESULT + "/" + SPSS_RENDO_FILE + ".csv";
        if (FileUtil.isExistFile(SPSS_RENDO_FILE_MKEC_P)) {
            String SPSS_RENDO_FILE_MKEC_Data = FileUtil.readFile(SPSS_RENDO_FILE_MKEC_P);
            FileUtil.writeFile(SPSS_RENDO_FILE_TMP_P, SPSS_RENDO_FILE_MKEC_Data);
            String SPSS_RENDO_FILE_CSV_Data = FileUtil.readFile(SPSS_RENDO_FILE_CSV_P);
            FileUtil.writeFileByAppend(SPSS_RENDO_FILE_TMP_P, SPSS_RENDO_FILE_CSV_Data);
            FileUtil.mvFile(SPSS_RENDO_FILE_TMP_P, SPSS_RENDO_FILE_CSV_P);
        }
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD END

        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_FILEWATSND).sZ(SPSS_RENDO_FILE + ".zip").sD(CM_APWORK_DATE).sI(SPSS_RENDO_FILE + ".csv").DEL());
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + SPSS_RENDO_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# 送信連動用ファイル作成
        //ls -l ${CM_FILEWATSND}/${SPSS_RENDO_FILE}.zip > ${CM_FILEWATSND}/S_ECSH_OK 2>&1
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_06")
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("SPSS_RENDO_FILE", SPSS_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=S_ECSH_OK　STATUS=" + shellExecuteDto.result).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  圧縮処理(RetailCRM向け連動ファイル)
        //###########################################
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD START
        //#  随時JOBで作成されたファイルが存在する場合、マージする
        String CRM_RENDO_FILE_MKEC_P = CM_APRESULT + "/" + CRM_RENDO_FILE + ".mkec";
        String CRM_RENDO_FILE_TMP_P = CM_APRESULT + "/" + CRM_RENDO_FILE + ".tmp";
        String CRM_RENDO_FILE_CSV_P = CM_APRESULT + "/" + CRM_RENDO_FILE + ".csv";
        if (FileUtil.isExistFile(CRM_RENDO_FILE_MKEC_P)) {
            String CRM_RENDO_FILE_MKEC_Data = FileUtil.readFile(CRM_RENDO_FILE_MKEC_P);
            FileUtil.writeFile(CRM_RENDO_FILE_TMP_P, CRM_RENDO_FILE_MKEC_Data);
            String CRM_RENDO_FILE_CSV_Data = FileUtil.readFile(CRM_RENDO_FILE_CSV_P);
            FileUtil.writeFileByAppend(CRM_RENDO_FILE_TMP_P, CRM_RENDO_FILE_CSV_Data);
            FileUtil.mvFile(CRM_RENDO_FILE_TMP_P, CRM_RENDO_FILE_CSV_P);
        }
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD END

        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO(CM_FILEWATSND).sZ(CRM_RENDO_FILE + ".zip").sD(CM_APWORK_DATE).sI(CRM_RENDO_FILE + ".csv").DEL());
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + CRM_RENDO_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# 送信連動用ファイル作成
        //ls -l ${CM_FILEWATSND}/${CRM_RENDO_FILE}.zip > ${CM_FILEWATSND}/R_ECSH_OK 2>&1
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_07")
                .addEvn("CM_FILEWATSND", CM_FILEWATSND)
                .addEvn("CRM_RENDO_FILE", CRM_RENDO_FILE)
                .execute();
        if (shellExecuteDto.RTN0()) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("送信連動用ファイル作成エラー　ファイル名=R_ECSH_OK　STATUS=" + shellExecuteDto.result).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###################################################
        //#  バックアップファイルと一時ファイルを削除
        //###################################################
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".tmp");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + MK_KOBAI_FILE + ".dat");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CF_KOBAI_FILE + ".dat");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CF_KOBAI_FILE + ".csv");

        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD START
        FileUtil.deleteFile(CM_APRESULT + "/" + DNA_RENDO_FILE + ".mkec");
        FileUtil.deleteFile(CM_APRESULT + "/" + SPSS_RENDO_FILE + ".mkec");
        FileUtil.deleteFile(CM_APRESULT + "/" + CRM_RENDO_FILE + ".mkec");
        //# 2024/01/12 MCCMPH2 MG-234仕変対応 ADD END

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
