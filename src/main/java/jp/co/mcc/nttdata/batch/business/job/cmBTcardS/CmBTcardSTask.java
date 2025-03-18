package jp.co.mcc.nttdata.batch.business.job.cmBTcardS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgmtxB.CmABgmtxBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABmailS.CmABmailSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTcardB.CmBTcardBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;

/**
 *#-------------------------------------------------------------------------------
 * #    名称          ：  一括カード発行シェル
 * #    プログラムID  ：  cmBTcardS
 * #
 * #    【処理概要】
 * #        TS一括カード発行情報のステータスが未設定のレコードを編集し、MSカード情報
 * #        に出力する処理「一括カード発行（cmBTcardB）」を起動するためのシェル。
 * #        開始メッセージを出力し、「cmBTcardB」を起動、終了メッセージを出力し、
 * #        戻り値を返却。
 * #
 * #    【引数説明】
 * #   -oエラーファイル名     :  エラーファイル名
 * #   -DEBUG                 :  デバッグモードでの実行（トレース出力機能が有効）
 * #   -debug                 :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :  2012/12/27 SSI.Suyama ： 初版
 * #      2.00 :  2015/10/02 SSI.上野   ： メール送信処理の戻り値追加
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2010 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTcardSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmBTcardBServiceImpl cmBTcardB;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmABmailSTask cmABmailSTask;

    @Autowired
    CmABgmtxBServiceImpl cmABgmtxBServiceImpl;


    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ

        // プログラムIDを環境変数に設定
//        String CM_MYPRGID = this.getClass().getSimpleName().substring(0, 9);
//        String CM_MYPRGID = args[0].substring(0, 9);
//        args[0]= CM_MYPRGID;
//        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);
        CM_MYPRGNAME = "一括カード発行";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));
        // 定数定義
        String ARG_OPT1 = "-o";
//        String CONNECT_SD = getenv(CmABfuncLServiceImpl.C_CM_USR_SD)
//                + "/" + getenv(CmABfuncLServiceImpl.C_CM_PSW_SD)
//                + "@" +getenv(CmABfuncLServiceImpl.C_CM_ORA_SID_SD);


        // 戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        String BTCARD_MAIL_TO = "cf_sys_cust@matsukiyococokara.com";
        String BTCARD_MAIL_FROM = "cfos_wf@matsukiyococokara.com";
        String BTCARD_MAIL_SUB = "【顧客基盤　エラー】会員番号一括発行履歴一覧";
        String ERRLOG = "ERRLOG_IKKATSU_CARD.txt";

        // メールテキストファイル名
        String MTEXT_FILE = "cmBTcardM";
//
//        String[] cmABgmtxBArgs = { MTEXT_FILE };
//        MainResultDto cmABgmtxBResult = cmABgmtxBServiceImpl.main(cmABgmtxBArgs.length, cmABgmtxBArgs);
        // 稼動ディレクトリ決定
        String CM_APWORK_DATE = getenv(CmABfuncLServiceImpl.CM_APWORK_DATE);
        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(CmABfuncLServiceImpl.CM_APWORK) + "/" + DateUtil.nowDateFormat("yyyyMMdd");
            setenv(CmABfuncLServiceImpl.CM_APWORK_DATE, CM_APWORK_DATE);
        }

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                // 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());

                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // 引数の数チェック
        String WK_OUTFILE = "0";
        if (1 == args.length || 2 == args.length) {
            String WK_OUTOPT = args[0].substring(0, 2);
            if (WK_OUTOPT.equals(ARG_OPT1)) {
                WK_OUTFILE = args[0].substring(2);
            }
        }

        if (2 == args.length && WK_OUTFILE.equals("0")) {
            String WK_OUTOPT = args[1].substring(0, 2);
            if (WK_OUTOPT.equals(ARG_OPT1)) {
                WK_OUTFILE = args[1].substring(2);
            }
        }
        // テンポラリファイル取得
        String TEMP_FILE1 = getenv(CmABfuncLServiceImpl.CM_APWORK_DATE)
                + "/" + getenv(CmABfuncLServiceImpl.CM_MYPRGID)
                + "01_" + PidUtil.getPid();

        // バッチ処理日付取得
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam().add(args));
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());
            return Rtn_NG;
        }

        // バッチ処理日付の７日前を取得
        setenv("CONNECT_SD", CONNECT_SD);
        setenv("BAT_YYYYMMDD", BAT_YYYYMMDD);

        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .addEvn("CONNECT_SD", CONNECT_SD).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).execute();
        String RTN_VAL = shellExecuteDto.getResult();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto.result);
            if ("01005".equals(SQL_CD) || "12154".equals(SQL_CD) || "01017".equals(SQL_CD) || StringUtils.isEmpty(SQL_CD)) {

                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ＤＢ接続時に失敗しました。").FE());
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付７日前の取得に失敗しました。").FE());
            }
            return Rtn_NG;
        }

        String BAT_7DAYSBFR = RTN_VAL;

        // 保存期限切れのエラーログ削除
        File errorLogFile = null;
        try {
            File directory = FileUtil.getFile(FileUtil.FileType.Directory, getenv(CmABfuncLServiceImpl.CM_PCRENKEI_KOJINNASHI));
            if (null != directory && directory.isDirectory() && directory.exists()) {
                File[] files = directory.listFiles();
                for (File f : files) {
                    if (f.getName().matches("\\*" + ERRLOG)) {
                        errorLogFile = f;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (null != errorLogFile) {
            try (FileReader fileReader = new FileReader(errorLogFile);
                 BufferedReader bufferedReader = new BufferedReader(fileReader);) {
                String line = null;
                while (null != (line = bufferedReader.readLine())) {
                    String baseName = line;
                    String BASE_NAME_DATE = line.substring(0, 8);
                    if (Long.parseLong(BASE_NAME_DATE) < Long.parseLong(BAT_7DAYSBFR)) {
                        APLOG_WT("保存期限切れのエラーログ削除対象[" + line + "]", "-FI");
                        File baseNameFile = null;
                        try {
                            baseNameFile = FileUtil.getFile(FileUtil.FileType.File, line);
                            baseNameFile.delete();
                        } catch (IOException e) {
                            APLOG_WT("保存期限切れのエラーログ削除エラー", "-FE");
                            return Rtn_NG;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // プログラム実行
        MainResultDto cmBTcarcBResult = cmBTcardB.main(getExecuteBaseParam().add(args));
        RTN = cmBTcarcBResult.exitCode;

        // 警告終了している場合は、メール送信（cmABmailS）を呼び出す
        if (RTN == Rtn_NG) {
            // メールテキストファイル名取得
            String[] cmABgmtxBArgs = {"", MTEXT_FILE};
            MainResultDto cmABgmtxBResult = cmABgmtxBServiceImpl.main(cmABgmtxBArgs.length, cmABgmtxBArgs);
            String MTEXT_NAME = cmABgmtxBResult.result;
            RTN = cmABgmtxBResult.exitCode;

            if (RTN != Rtn_OK) {
                APLOG_WT("メールテキストファイル名取得エラー", "-FW");
                return RTN;
            }

            // メールテキストファイル内の置換文字列を変換
            // 処理年月日
            File MTEXT_NAMEFile = null;
            File TEMP_FILE1File = null;
            try {
                MTEXT_NAMEFile = FileUtil.getFile(FileUtil.FileType.File, MTEXT_NAME);
                TEMP_FILE1File = FileUtil.getFile(FileUtil.FileType.File, TEMP_FILE1 + ".tmp");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (null != MTEXT_NAMEFile && null != TEMP_FILE1File) {
                try (FileReader MTEXT_NAMEFileReader = new FileReader(MTEXT_NAMEFile);
                     BufferedReader MTEXT_NAMEBufferedReader = new BufferedReader(MTEXT_NAMEFileReader);
                     FileWriter TEMP_FILE1FileWriter = new FileWriter(TEMP_FILE1File);
                     BufferedWriter TEMP_FILE1BufferedWriter = new BufferedWriter(TEMP_FILE1FileWriter)) {
                    String line = null;
                    while (null != (line = MTEXT_NAMEBufferedReader.readLine())) {
                        line = line.replace("%SYORI_DATE%", BAT_YYYYMMDD);
                        TEMP_FILE1BufferedWriter.write(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            long ERR_CNT = 0;
            if (errorLogFile.exists()) {
                // エラー件数
                 ERR_CNT = FileUtil.contentLength(CM_APWORK_DATE+"/"+ERRLOG);
            }

            if (null != TEMP_FILE1File) {
                try (FileReader TEMP_FILE1FileReader = new FileReader(TEMP_FILE1File);
                     BufferedReader TEMP_FILE1BufferedReader = new BufferedReader(TEMP_FILE1FileReader);
                     FileWriter TEMP_FILE1FileWriter = new FileWriter(TEMP_FILE1File);
                     BufferedWriter TEMP_FILE1BufferedWriter = new BufferedWriter(TEMP_FILE1FileWriter)) {
                    String line = null;
                    while (null != (line = TEMP_FILE1BufferedReader.readLine())) {
                        line = line.replace("%ERR_CNT%", String.valueOf(ERR_CNT));
                        TEMP_FILE1BufferedWriter.write(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // メールテキストファイル
            try {
                Files.copy(TEMP_FILE1File.toPath(), new File(getenv(C_aplcom1Service.CM_MAILTEXT) + "/" + MTEXT_FILE).toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // メール送信　戻り値（警告（メール送信不可））追加
            String[] cmABmailSArgs = {BTCARD_MAIL_TO, BTCARD_MAIL_FROM, BTCARD_MAIL_SUB, MTEXT_FILE};
            int RTN_ML  = cmABmailSTask.main(getExecuteBaseParam().add(cmABmailSArgs));
            if (RTN_ML == Rtn_NG) {
                // 警告（メール送信不可）の場合
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
            } else if (RTN_ML != Rtn_OK) {
                // # 異常の場合
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
                return Rtn_NG;
            }
        } else if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        if (!"0".equals(WK_OUTFILE)) {
            FileUtil.mvFile(CM_APWORK_DATE + "/" + WK_OUTFILE, getenv(C_aplcom1Service.CM_APRESULT) + "/" + WK_OUTFILE);
        }
        // ###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }


}
