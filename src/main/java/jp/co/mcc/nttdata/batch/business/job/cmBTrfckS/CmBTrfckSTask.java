package jp.co.mcc.nttdata.batch.business.job.cmBTrfckS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　FILE受信ファイルチェック
 * #    プログラムID　：　cmBTrfckS
 * #
 * #    【処理概要】
 * #        受信ファイルの内、ファイル受信必須のファイルが
 * #        到着しているかのチェックおよび、複数回の受信を想定していないファイルが
 * #        到着していないことをチェックする機能。
 * #        引数に指定したパラメータファイルに、受信必須ファイルのFILEIDが定義
 * #        されていることを前提とする。
 * #
 * #    【引数説明】
 * #        -P パラメータファイル名　：　チェック対象のFILEIDを定義したパラメータ
 * #                                     ファイル名を指定する
 * #        -B        　：　前日分のバックアップディレクトリをチェック対象とする場合に
 * #                        指定する。
 * #        -T 文字列 　：　パラメータファイルのチェック対象とするデータを特定する
 * #                        場合に指定する。
 * #        -W        　：　集信処理待ちDIRに存在する場合、チェックOKとする場合に
 * #                        指定する。
 * #
 * <p>
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2012/10/26 SSI.越後谷  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTrfckSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgprmBServiceImpl cmABgprmBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    private String IN_FILE = "";
    private String FILE_NAME = "";
    private int Rtn_Exit = 10;
    private String TEMP_FILE1 = "";
    private String TEMP_FILE2 = "";
    private String PARAM_NAME = "";
    private String CM_FILE_RCV = "";
    private String CM_FILEWATRCV = "";
    private String CM_FILENOWRCV = "";
    private String CM_SERVER_ID = "";

    private String WK_RCV_NAME = "";
    private String WK_RCV_FILE = "";
    private String WK_FILE_ID = "";
    private String IN_PARAM_NAME = "";

    //#  引数格納変数初期化
    private String OPTION1 = null;
    private String OPTION2 = null;
    private String OPTION3 = null;
    private String OPTION4 = null;

    @Override
    public int taskExecuteCustom(String[] args) {

        //环境変数取值
        CM_FILE_RCV = getenv(C_aplcom1Service.CM_FILE_RCV);
        CM_FILEWATRCV = getenv(C_aplcom1Service.CM_FILEWATRCV);
        CM_FILENOWRCV = getenv(C_aplcom1Service.CM_FILENOWRCV);
        CM_SERVER_ID = getenv(C_aplcom1Service.CM_SERVER_ID);

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "FILE受信ファイルチェック";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        //#  パラメータファイル
        String PARAM_FILE = "cmABrcvaP";

        //#  引数定義
        final String ARG_OPT1 = "-P"; //###  パラメータファイル名
        final String ARG_OPT2 = "-B"; //###  BKUPチェック対象区分
        final String ARG_OPT3 = "-T"; //###  チェック対象区分
        final String ARG_OPT4 = "-W"; //###  集信処理待ちチェック対象区分

        //###########################################
        //#  引数チェック
        //###########################################
        if (args.length < 2) {
            APLOG_WT("引数エラー　[引数指定数不正]", FW);

            return Rtn_NG;
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (OPTION1 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    if (args.length == i + 1) {
                        APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    OPTION1 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT2:
                    if (OPTION2 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    if (i + 1 < args.length) {
                        OPTION2 = args[i + 1];
                    }
                    break;
                case ARG_OPT3:
                    if (OPTION3 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    if (args.length == i + 1) {
                        APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    OPTION3 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT4:
                    if (OPTION4 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);

                        return Rtn_NG;
                    }
                    if (i + 1 < args.length) {
                        OPTION4 = args[i + 1];
                    }
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);

                    return Rtn_NG;
            }
        }

        //#  必須引数チェック
        if (StringUtils.isAnyEmpty(OPTION1, OPTION3)) {
            APLOG_WT("引数エラー　必須引数不足[" + ARG_ALL + "]", FW);

            return Rtn_NG;
        }

        MainResultDto cmABgprmBResult = cmABgprmBServiceImpl.main(getExecuteBaseParam().add(OPTION1));
        int RTN = cmABgprmBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("引数エラー　[パラメータファイル名不正]", FW);

            return RTN;
        }
        IN_PARAM_NAME = cmABgprmBResult.result;

        APLOG_WT("起動引数[" + ARG_ALL + "]", FI);

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        setCM_APWORK_DATE();
        String WORK_DIR = CM_APWORK_DATE;

        TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();
        TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + PidUtil.getPid();
        String TEMP_FILE3 = WORK_DIR + "/" + CM_MYPRGID + "03_" + PidUtil.getPid();

        //###########################################
        //#  パラメータファイル名取得
        //###########################################
        cmABgprmBResult = cmABgprmBServiceImpl.main(getExecuteBaseParam().add(PARAM_FILE));
        RTN = cmABgprmBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("引数エラー　[パラメータファイル名取得エラー]", FE);

            return RTN;
        }

        PARAM_NAME = cmABgprmBResult.result;
        final String CM_FILEAFTRCV = getenv(C_aplcom1Service.CM_FILEAFTRCV);
        final String fCM_MYPRGID = CM_MYPRGID;
        final String CM_BKUPRCV = getenv(C_aplcom1Service.CM_BKUPRCV);

        ReadFileDto result = ReadFileDto.getInstance().readFile(IN_PARAM_NAME).loop5((PARA_FLD1, PARA_FLD2, PARA_FLD3, PARA_FLD4, PARA_FLD5) -> {

            String IN_FILE_ID = PARA_FLD1;
            String IN_SERVER_ID = PARA_FLD2;
            String IN_TGT_ID = PARA_FLD3;
            String IN_APLOG_FLG = PARA_FLD4;

            if (!StringUtils.equals(IN_SERVER_ID, CM_SERVER_ID)) {
                return Rtn_OK;
            }

            if (StringUtils.equals(IN_APLOG_FLG, "A")) {
                return Rtn_OK;
            }

            if (!StringUtils.equals(IN_TGT_ID, OPTION3)) {
                return Rtn_OK;
            }

            String WK_COMMENT = IN_FILE_ID.substring(0, 1);

            boolean isOK = StringUtils.equals(WK_COMMENT, "#") || StringUtils.equalsAny("FILEID", "");
            if (isOK) {
                return Rtn_OK;
            }
            //#  パラメータファイルから、第一フィールドがFILE-IDに一致する行を取得
            IN_FILE = PARAM_NAME;
            String filterContent = Arrays.stream(FileUtil.readFile(IN_FILE).split("\n"))
                    .filter(item -> IN_FILE_ID.equals(item.replaceAll("\\s+", " ").split(" ")[0]))
                    .collect(Collectors.joining("\n"));
            FileUtil.writeFile(TEMP_FILE1, filterContent);

            //#  パラメータファイルから抽出したレコード数をチェック
            Integer IN_LCNT = FileUtil.countLines(TEMP_FILE1);
            if (IN_LCNT != 1) {
                FileUtil.deleteFile(TEMP_FILE1);
                APLOG_WT("受信パラメータファイル取得エラー　FILEID=" + IN_FILE_ID, FE);

                return Rtn_NG;
            }

            IN_FILE = TEMP_FILE1;
            ReadFileDto.getInstance().readFile(IN_FILE).loop6((READ_FLD1, READ_FLD2, READ_FLD3, READ_FLD4, READ_FLD5, READ_FLD6) -> {
                FILE_NAME = READ_FLD2;
                return Rtn_OK;
            });

            //#  当日、前日のシステム日付取得
            String SYS_YYYYMMDD_T = DateUtil.nowDateFormat("yyyyMMdd");
            String SYS_YYYYMMDD_Y = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            //#  当日、前日に作成された処理済ファイルリスト取得
            ShellClientManager.getShellExecuteDto(fCM_MYPRGID + "_01")
                    .defaultEvn(this)
                    .addEvn("CM_FILEAFTRCV", CM_FILEAFTRCV)
                    .addEvn("FILE_NAME", FILE_NAME)
                    .addEvn("SYS_YYYYMMDD_T", SYS_YYYYMMDD_T)
                    .addEvn("SYS_YYYYMMDD_Y", SYS_YYYYMMDD_Y)
                    .addEvn("TEMP_FILE2", TEMP_FILE2)
                    .execute();

            //#  前日分バックアップファイルリスト取得
            String BAT_YYYYMMDD = cmABgdatBServiceImpl.main(getExecuteBaseParam()).result;
            if (StringUtils.equals(BAT_YYYYMMDD, SYS_YYYYMMDD_T) && StringUtils.isNotEmpty(OPTION2)) {
                ShellClientManager.getShellExecuteDto(fCM_MYPRGID + "_02")
                        .defaultEvn(this)
                        .addEvn("CM_BKUPRCV", CM_BKUPRCV)
                        .addEvn("FILE_NAME", FILE_NAME)
                        .addEvn("SYS_YYYYMMDD_T", SYS_YYYYMMDD_T)
                        .addEvn("SYS_YYYYMMDD_Y", SYS_YYYYMMDD_Y)
                        .addEvn("TEMP_FILE2", TEMP_FILE2)
                        .execute();
            }

            //#  集信処理待ちファイルリスト取得
            BAT_YYYYMMDD = cmABgdatBServiceImpl.main(getExecuteBaseParam()).result;
            if (StringUtils.isNotEmpty(OPTION4)) {
                ShellClientManager.getShellExecuteDto(fCM_MYPRGID + "_03")
                        .defaultEvn(this)
                        .addEvn("CM_FILEWATRCV", CM_FILEWATRCV)
                        .addEvn("FILE_NAME", FILE_NAME)
                        .addEvn("TEMP_FILE2", TEMP_FILE2)
                        .execute();
            }

            //#  ディレクトリから抽出したレコード数をチェック
            IN_LCNT = FileUtil.countLines(TEMP_FILE2);
            if (IN_LCNT !=null && 0 == IN_LCNT) {
                if (StringUtils.equals(IN_APLOG_FLG, "E")) {
                    APLOG_WT("未集信ファイル【異常】FILEID=" + IN_FILE_ID, "-F" + IN_APLOG_FLG);
                } else {
                    APLOG_WT("未集信ファイル【異常】FILEID=" + IN_FILE_ID, "-F" + IN_APLOG_FLG);
                }

                Rtn_Exit = Rtn_NG;
            } else {
                APLOG_WT("集信ファイル取込済　FILEID=" + IN_FILE_ID, FI);
            }

            FileUtil.deleteFile(TEMP_FILE1);
            FileUtil.deleteFile(TEMP_FILE2);

            return Rtn_OK;
        });
        if (result.getStatus() == Rtn_NG) {
            return Rtn_NG;
        }

        //###########################################
        //#  集信ディレクトリに存在したままの集信ファイルチェック
        //###########################################
        FileUtil.directoryFileNamesWritetoFile(CM_FILE_RCV, TEMP_FILE1);
        if (Rtn_NG == CHK_FILE("集信", 10, CM_FILE_RCV)) {
            return Rtn_NG;
        }

        FileUtil.directoryFileNamesWritetoFile(CM_FILEWATRCV, TEMP_FILE1);
        if (Rtn_NG == CHK_FILE("集信処理待ち", 10, CM_FILEWATRCV)) {
            return Rtn_NG;
        }

        FileUtil.directoryFileNamesWritetoFile(CM_FILENOWRCV, TEMP_FILE1);
        if (Rtn_NG == CHK_FILE("集信処理中", 10, CM_FILENOWRCV)) {
            return Rtn_NG;
        }

        FileUtil.deleteFile(TEMP_FILE1);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        if (Rtn_Exit == Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
        } else {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FW());
        }

        return Rtn_Exit;
    }

    private int CHK_FILE(String a, int b, String c) {

        int WK_SLEEP = b;
        
        ReadFileDto result = ReadFileDto.getInstance().readFile(TEMP_FILE1).loop1((READ2_FLD1) -> {

            WK_RCV_NAME = READ2_FLD1;
            WK_RCV_FILE = READ2_FLD1;

            if (StringUtils.startsWith(WK_RCV_FILE, "#") || StringUtils.equals(WK_RCV_FILE, "FILEID") || StringUtils.isEmpty(WK_RCV_FILE)) {
                return Rtn_OK;
            }

            //# ファイル名の動的部分（日付等）を変換
            WK_RCV_NAME = RegExUtils.replaceAll(WK_RCV_NAME, "[0-9]", "");

            //#  受信パラメータファイルから、第一フィールドがFILE-IDに一致する行を取得
            IN_FILE = PARAM_NAME;
            String IN_FILE_DATA = Arrays.stream(FileUtil.readFile(IN_FILE).split("\n"))
                    .filter(item -> RegExUtils.replaceAll(item.replaceAll("\\s+", " ").split(" ")[2], "[0-9]", "").equals(WK_RCV_NAME))
                    .collect(Collectors.joining("\n"));

            FileUtil.writeFile(TEMP_FILE2, IN_FILE_DATA);

            //#  パラメータファイルから抽出したレコード数をチェック
            int IN_LCNT = FileUtil.countLines(TEMP_FILE2);
            if (0 == IN_LCNT) {
                return Rtn_OK;
            }

            if (1 != IN_LCNT) {
                APLOG_WT("受信パラメータファイル重複データエラー　FILE=" + WK_RCV_NAME, FE);
                return Rtn_NG;
            }

            //#  パラメータファイルから抽出したレコードのフィールド数をチェック
            IN_FILE = TEMP_FILE2;
            ReadFileDto.getInstance().readFile(IN_FILE).loop6((READ_FLD1, READ_FLD2, READ_FLD3, READ_FLD4, READ_FLD5, READ_FLD6) -> {
                WK_FILE_ID = READ_FLD1;
                return Rtn_OK;
            });

            FileUtil.deleteFile(TEMP_FILE2);

            //#  受信ファイルチェック対象パラメータファイルから、第一フィールドがFILE-IDに一致する行を取得
            IN_FILE = IN_PARAM_NAME;
            String filterContent = Arrays.stream(FileUtil.readFile(IN_FILE).split("\n"))
                    .filter(item -> (WK_FILE_ID.equals(item.replaceAll("\\s+", " ").split(" ")[1]) &&
                            "A".equals(item.replaceAll("\\s+", " ").split(" ")[4]) &&
                            OPTION3.equals(item.replaceAll("\\s+", " ").split(" ")[3]) &&
                            CM_SERVER_ID.equals(item.replaceAll("\\s+", " ").split(" ")[2])))
                    .collect(Collectors.joining("\n"));
            FileUtil.writeFile(TEMP_FILE2, filterContent);

            // #  受信ファイルチェック対象パラメータファイルから抽出したレコード数をチェック
            IN_LCNT = FileUtil.countLines(TEMP_FILE2);
            if (0 == IN_LCNT) {
                return Rtn_OK;
            }

            if (1 != IN_LCNT) {
                APLOG_WT("受信ファイルチェック対象パラメータファイル重複データエラー　FILE=" + WK_RCV_NAME, FE);

                return Rtn_NG;
            }

            String filePath = c + WK_RCV_NAME;
            if (FileUtil.isExistFile(filePath)) {
                APLOG_WT(a + "ディレクトリに未処理ファイルあり\n【警告】　FILE_ID=[" + WK_FILE_ID + "]　FILE=[" + filePath + "]", FW);
                Rtn_Exit = Rtn_NG;
            }

            FileUtil.deleteFile(TEMP_FILE2);

            return Rtn_OK;
        });

        if (result.getStatus() == Rtn_NG) {
            return Rtn_NG;
        }

        return Rtn_OK;
    }

}
