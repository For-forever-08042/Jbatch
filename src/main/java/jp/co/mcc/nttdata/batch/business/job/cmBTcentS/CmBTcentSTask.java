package jp.co.mcc.nttdata.batch.business.job.cmBTcentS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgmtxB.CmABgmtxBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTcentB.CmBTcentBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABmailS.CmABmailSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*

        -------------------------------------------------------------------------------
            名称          ：  カード入会連動
            プログラムID  ：  cmBTcentS

            【処理概要】
               カード入会連動ファイルより、入会登録を行う「カード入会連動（cmBTcentB）」を起動するためのシェル。
               開始メッセージを出力し、「cmBTcentB」を起動、終了メッセージを出力し、戻り値を返却。

            【引数説明】
               -DEBUG            :  デバッグモードでの実行（トレース出力機能が有効）
               -debug            :  デバッグモードでの実行（トレース出力機能が有効）

            【戻り値】
               10     ：  正常
               49     ：  警告
               99     ：  異常
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 6

            改定履歴
              1.00 :   2012/12/26 SSI.横山 ：初版
              2.00 :   2013/03/19 SSI.本田 ：入力ファイルのフォーマット変更に伴う対応
              3.00 :   2015/10/02 SSI.上野 ：メール送信処理の戻り値追加
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2012 NTT DATA CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTcentSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmBTcentBServiceImpl cmBTcentBServiceImpl;

    @Autowired
    CmABgmtxBServiceImpl cmABgmtxBServiceImpl;

    @Autowired
    CmABmailSTask cmABmailSTask;

    @Autowired
    CmABgdldBServiceImpl cmABgdldBServiceImpl;

    String DEL_YMD = "";
    String DEL_YM = "";
    String DEL_Y = "";
    String DEL_YMD2 = "";

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME = "カード入会連動";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  引数定義
//        ###########################################

        String ARG_OPT1 = "-DEBUG";          //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //  デバッグモードでの実行（トレース出力機能が有効）

//        ###########################################
//        #  引数格納変数初期化
//        ###########################################

//        ###########################################
//        #  APログ出力関数
//        ###########################################

//        ###########################################
//        #  定数定義
//        ###########################################

        Rtn_OK = 10;
        Rtn_NG = 49;

//        Rtn_ER = 99;

        String DB_DELGRP = "カード入会";

        String INPUT_FILE_NAME = "^cf.{8}\\.txt$";
        String ERR_FILE_NAME = "_kaiin_touroku_error.csv";
        String DEL_FILE = "^.{8}" + ERR_FILE_NAME + "$";

        // メール送信情報
//        本番機リリースの際は本番送信用のMLに変更を忘れずに！！
        String BTCENT_MAIL_TO = "cf_sys_cust@matsukiyococokara.com";
        String BTCENT_MAIL_FROM = "cfos_wf@matsukiyococokara.com";
        String BTCENT_MAIL_SUB = "【顧客基盤　エラー】入会エラーリスト";

        // メールテキストファイル名
        String MTEXT_FILE = "cmBTcentM";

        // ｢入会連動｣用ジョブ実績出力用ログファイル名称
        String CM_G16011D = "JBch_G16011D";

//        ###########################################
//        #  バッチ処理日付取得
//        ###########################################

        // 当日日付取得

        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", FE);
            return Rtn_NG;
        }

//        ###########################################
//        #  日付・時刻取得
//        ###########################################

        // システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

//          システム時刻
//        SYS_HHMMSS=`date '+%H%M%S'`

//          システム日付と時刻
//        SYS_YYYYMMDDHHMMSS=`date '+%Y%m%d%H%M%S'`

//        ###########################################
//        #  稼動ディレクトリ決定
//        ###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            APLOG_WT("稼動ディレクトリ作成エラー", FE);
            return Rtn_NG;
        }

//        ###########################################
//        #  引数の数チェック
//        ###########################################

        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        // 引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";

//          引数格納

        for (String arg : args) {
            if (arg.equals(ARG_OPT1) || arg.equals(ARG_OPT2)) {
                OPTION1 = arg;
            }
        }

//        ################################################
//        #  テンポラリファイル取得
//        ################################################

        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + PidUtil.getPid();
        String TEMP_FILE3 = CM_APWORK_DATE + "/" + CM_MYPRGID + "03_" + PidUtil.getPid();
        String TEMP_FILE4 = CM_APWORK_DATE + "/" + CM_MYPRGID + "04_" + PidUtil.getPid();

        ArrayList<String> fileNowRcvFileNameList = FileUtil.findByRegex(getenv(C_aplcom1Service.CM_FILENOWRCV), INPUT_FILE_NAME);
        for (String fileName : fileNowRcvFileNameList) {
            FileUtil.copyFile(fileName, CM_APWORK_DATE + "/" + new File(fileName).getName());
        }

//        ################################################
//        #  処理対象情報をジョブ実績ログファイルに出力
//        ################################################

//        連動ファイル有無の取得

        ArrayList<String> apWorkDateFileNameList = FileUtil.findByRegex(CM_APWORK_DATE, INPUT_FILE_NAME);
        int RENDO_FILE_UMU = FileUtil.countLines(apWorkDateFileNameList);
        if (RENDO_FILE_UMU > 0) {
            FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + CM_G16011D + ".log", "        連動ファイル：あり");
        } else {
            FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + CM_G16011D + ".log", "        連動ファイル：なし");
            FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + CM_G16011D + ".log", "        件数：0 件");

            String IN_FILE = "";

//            ファイルが存在しなければ正常終了
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理対象ファイルなし　ファイル名=" + IN_FILE).FI());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_OK;
        }

        for (String filename : apWorkDateFileNameList) {

//            ################################################
//            #  連動ファイル複合化
//            ################################################

            ReadFileDto.getInstance().readFile(getenv(C_aplcom1Service.CM_APJCL) + "/" + "cmBTcentS000").loop2((txt1, txt2) -> {

                ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01").addEvn("filename", filename).addEvn("TEMP_FILE1", TEMP_FILE1).addEvn("txt1", txt1).addEvn("txt2", txt2).execute();

                // openssl enc -d -aes-128-ecb -in ${filename} -out ${TEMP_FILE1} -K "${txt1}" -iv "${txt2}" >/dev/null 2>&1

                return Rtn_OK;
            });

            FileUtil.mvFile(TEMP_FILE1, filename);

//            ################################################
//            #  処理対象情報をジョブ実績ログファイルに出力
//            ################################################

//            連動ファイルのデータ件数を取得

            int RENDO_FILE_DATA_CNT = FileUtil.countLines(filename);

            FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G16011D + ".log", "        件数：" + RENDO_FILE_DATA_CNT + " 件");

//            ###########################################
//            #  カード入会連動プログラム実行
//            ###########################################
            int lastSeparatorIndex = filename.lastIndexOf(File.separator);
            String filename1 = filename.substring(lastSeparatorIndex + 1);

            MainResultDto cmBTcentBResult = cmBTcentBServiceImpl.main(getExecuteBaseParam().add("-i" + filename1).add(OPTION1));
//            警告終了している場合は、メール送信（cmABmailS）を呼び出す
            RTN = cmBTcentBResult.exitCode;

            if (RTN == Rtn_NG) {
//            メールテキストファイル名取得
                MainResultDto cmABgmtxBResult = cmABgmtxBServiceImpl.main(getExecuteBaseParam().add(MTEXT_FILE));
                String MTEXT_NAME = cmABgmtxBResult.result;
                RTN = cmABgmtxBResult.exitCode;
                if (RTN != Rtn_OK) {
                    APLOG_WT("メールテキストファイル名取得エラー", FW);
                    return RTN;
                }

//                ################################################
//                # メールテキストファイル内の置換文字列を変換
//                ################################################

//                処理年月日
                FileUtil.writeFile(TEMP_FILE4 + ".tmp", FileUtil.readFile(MTEXT_NAME).replaceAll("%SYORI_DATE%", BAT_YYYYMMDD));

//                エラー件数
                int ERR_CNT = FileUtil.countLines(CM_APWORK_DATE + "/" + BAT_YYYYMMDD + ERR_FILE_NAME);
                ERR_CNT = ERR_CNT - 1;

                FileUtil.writeFile(TEMP_FILE4, FileUtil.readFile(TEMP_FILE4 + ".tmp").replaceAll("%ERR_CNT%", String.valueOf(ERR_CNT)));

//                メールテキストファイル
                FileUtil.mvFile(TEMP_FILE4, getenv(C_aplcom1Service.CM_MAILTEXT) + "/" + MTEXT_FILE);

//                メール送信　戻り値（警告（メール送信不可））追加
                int RTN_ML = cmABmailSTask.main(getExecuteBaseParam().add(BTCENT_MAIL_TO).add(BTCENT_MAIL_FROM).add(BTCENT_MAIL_SUB).add(MTEXT_FILE));

                if (RTN_ML == Rtn_NG) {
//                    警告（メール送信不可）の場合
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
                } else if (RTN_ML != Rtn_OK) {
//                     異常の場合
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
                    return Rtn_NG;
                }
            } else if (RTN != Rtn_OK) {
                APLOG_WT("カード入会連動プログラム実行失敗 ", FW);
                return Rtn_NG;
            }

            FileUtil.deleteFile(filename);

        }

//        ###########################################
//        #  データ削除日付取得(削除対象日取得)
//        ###########################################

        MainResultDto cmABgdldBResult = cmABgdldBServiceImpl.main(getExecuteBaseParam().add(DB_DELGRP));
        FileUtil.writeFile(TEMP_FILE2, cmABgdldBResult.result);
        RTN = cmABgdldBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("削除日付取得エラー [" + RTN + "]", FE);
            return Rtn_NG;
        }

//        ###########################################
//        #  削除対象日付取得を種類ごとに分割
//        ###########################################


        ReadFileDto.getInstance().readFile(TEMP_FILE2).loop4((READ2_FLD1, READ2_FLD2, READ2_FLD3, READ2_FLD4) -> {
            DEL_YMD = READ2_FLD1;
            DEL_YM = READ2_FLD2;
            DEL_Y = READ2_FLD3;
            DEL_YMD2 = READ2_FLD4;
            return Rtn_OK;
        });

//        ###########################################
//        #  保持期間を超過したファイルを削除
//        ###########################################
        ArrayList<String> pcrenkeiKojinnashiFileNameList = FileUtil.findByRegex(getenv(C_aplcom1Service.CM_PCRENKEI_KOJINNASHI), DEL_FILE);

        FileUtil.writeFile(TEMP_FILE3, pcrenkeiKojinnashiFileNameList.stream()
                .map(filename -> {
                    File file = new File(filename);
                    return file.getName();
                })
                .collect(Collectors.joining("\n")));

        if (StringUtils.isNotEmpty(FileUtil.readFile(TEMP_FILE3))) {
            ReadFileDto.getInstance().readFile(TEMP_FILE3).loop1((filename) -> {
                String bat_date = filename.substring(0, 8);
                if (Integer.parseInt(DEL_YMD) > Integer.parseInt(bat_date)) {
                    if (FileUtil.deleteFile(getenv(C_aplcom1Service.CM_PCRENKEI_KOJINNASHI) + "/" + filename) != 0) {
                        APLOG_WT("入会エラーリストファイルの削除に失敗しました [" + filename + "]", FW);
                        return Rtn_NG;
                    }
                }
                return Rtn_OK;
            });
        }


//        ###########################################
//        #  入会エラーリストファイルをコピー
//        ###########################################
        int ERROR_FILE_DATA_CNT = FileUtil.countLines(CM_APWORK_DATE + "/" + BAT_YYYYMMDD + ERR_FILE_NAME);
        if (ERROR_FILE_DATA_CNT == 0) {
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + BAT_YYYYMMDD + ERR_FILE_NAME);
        }

        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);
        FileUtil.deleteFile(TEMP_FILE4);

//        ###########################################
//        #  終了メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }

}
