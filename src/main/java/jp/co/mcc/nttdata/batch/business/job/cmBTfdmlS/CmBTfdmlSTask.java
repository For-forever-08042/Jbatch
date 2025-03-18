package jp.co.mcc.nttdata.batch.business.job.cmBTfdmlS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgmtxB.CmABgmtxBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABmailS.CmABmailSTask;
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

import java.util.List;
import java.util.zip.ZipFile;
//      #!/bin/ksh
//#-------------------------------------------------------------------------------
//        #    名称          ：  ファイルダウンロード通知メール送信
//#    プログラムID  ：  cmBTfdmlS
//#
//        #    【処理概要】
//            #       個人情報ありのファイルを管理ＰＣからダウンロードした場合、
//            #       操作者・対象ファイル名をメールで通知するためのシェル。
//            #       開始メッセージを出力し、メール送信対象データを取得し、メールを送信し、
//            #       終了メッセージを出力し、戻り値を返却。
//            #
//            #    【引数説明】
//            #       -DEBUG            :  デバッグモードでの実行（トレース出力機能が有効）
//            #       -debug            :  デバッグモードでの実行（トレース出力機能が有効）
//            #
//            #    【戻り値】
//            #       10     ：  正常
//#       49     ：  警告
//#       99     ：  異常
//#-------------------------------------------------------------------------------
//        #    稼働環境
//#      Red Hat Enterprise Linux 6
//            #
//            #    改定履歴
//#      1.00 : 2015/09/04 SSI.上野：初版
//#      2.00 : 2021/10/05 SSI.上野： メール宛先差出人変更
//#-------------------------------------------------------------------------------
//        #  $Id:$
//#-------------------------------------------------------------------------------
//        #  Copyright (C) 2012 NTT DATA CORPORATION
//#-------------------------------------------------------------------------------

@Slf4j
@Component
public class CmBTfdmlSTask extends NtBasicTask {

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
        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ファイルダウンロード通知メール送信";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-DEBUG";
        String ARG_OPT2 = "-debug";

        //###########################################
        //#  引数格納変数初期化
        //###########################################
        //        ARG_ALL=${@}

        //###########################################
        //#  定数定義
        //###########################################
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        String DB_KBN = "MD";

        //#  DB接続先
        //                CONNECT_MD=${CM_USR_MD}/${CM_PSW_MD}@${CM_ORA_SID_MD}

        //# メール送信情報
        //### 本番機リリースの際は本番送信用のMLに変更を忘れずに！！ ###
        //#BTFLML_MAIL_TO="pi-warning@cocokarafine.co.jp"
        //#BTFLML_MAIL_FROM="cfos_wf@cocokarafine.co.jp"
        String BTFLML_MAIL_TO = "pi-warning@matsukiyococokara.com";
        String BTFLML_MAIL_FROM = "cfos_wf@matsukiyococokara.com";
        String BTFLML_MAIL_SUB = "【顧客基盤　ファイルダウンロード通知】";
        //# メールテキストファイル名
        String MTEXT_FILE = "cmBTflmlM";

        //# 区切り保存
        String BKUP_IFS = IFS;

        //# 処理ステータス
        int SYORI_STS_MI = 0;
        int SYORI_STS_OK = 10;
        int SYORI_STS_WN = 90;
        String CM_APWORK_DATE;

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 当日日付取得
        //#  バッチ処理日付
        MainResultDto RTN = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        if (RTN.exitCode != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー", FW);
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = RTN.result;

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK_DATE);
        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK) + "/" + SYS_YYYYMMDD;
        }
        setenv(C_aplcom1Service.CM_APWORK_DATE, CM_APWORK_DATE);

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

        //        cd $CM_APWORK_DATE

        // 引数の数チェック
        if (args.length >= 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";
        //#  引数格納
        for (String arg : args) {
            if (ARG_OPT1.equals(arg) || ARG_OPT2.equals(arg)) {
                OPTION1 = arg;
                break;
            }
        }

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + pid;

        //################################################
        //#  PMダウンロードファイル情報取得
        //################################################
        ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .addEvn("CONNECT_MD", CONNECT_MD).addEvn("CM_MYPRGID", CM_MYPRGID).addEvn("TEMP_FILE1", TEMP_FILE1).addEvn("SYORI_STS_MI", String.valueOf(SYORI_STS_MI)).execute();

        String RTN_VAL = shellExecuteDto1.getResult();
        if (shellExecuteDto1.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PMファイルダウンロード情報取得に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        String lstFile = FileUtil.readFile(TEMP_FILE1 + ".lst");
        if (!StringUtils.isEmpty(lstFile)) {
            String[] paramNamelist = lstFile.split("\n");
            if (paramNamelist != null && paramNamelist.length > 0) {
                for (int i = 0; i < paramNamelist.length; i++) {
                    String[] liselist = paramNamelist[i].split(",");
                    String READ_FLD1 = liselist[0];
                    String READ_FLD2 = liselist[1];
                    String READ_FLD3 = liselist[2];
                    String READ_FLD4 = liselist[3];
                    String READ_FLD5 = liselist[4];
                    String READ_FLD6 = liselist[5];
                    int FILE_LINECNT = 0;
                    String CM_PCRENKEI_KOJINARI = getenv("CM_PCRENKEI_KOJINARI");
                    if (!FileUtil.isExistFile(CM_PCRENKEI_KOJINARI + "/" + READ_FLD6)) {
                        APLOG_WT("ダウンロード対象ファイルなし　ファイル名=" + READ_FLD6, FI);
                    } else {
                        String FILE_KAKUCYOUSI = READ_FLD6.substring(READ_FLD6.indexOf(".") + 1);
                        String regex = READ_FLD6.substring(0, READ_FLD6.indexOf("."));
                        if ("zip".equals(FILE_KAKUCYOUSI)) {
                            try {
                                List<String> zipFileList = FileUtil.findByRegex(CM_PCRENKEI_KOJINARI, READ_FLD6);
                                for (String file : zipFileList) {
                                    FileUtil.unzip(new ZipFile(CM_PCRENKEI_KOJINARI + "/" + file), getenv(CM_APWORK_DATE));
                                }

                                List<String> RCV_FILE_NAME = FileUtil.findByRegex(getenv(CM_FILENOWRCV), regex);
                                FILE_LINECNT = FileUtil.countLines(RCV_FILE_NAME);
                            } catch (Exception e) {

                            }
                        } else if ("csv".equals(FILE_KAKUCYOUSI)) {
                            List<String> RCV_FILE_NAME = FileUtil.findByRegex(CM_PCRENKEI_KOJINARI, READ_FLD6);
                            FILE_LINECNT = FileUtil.countLines(RCV_FILE_NAME);
                        } else {
                            APLOG_WT("ダウンロード対象ファイル拡張子想定外　ファイル名" + READ_FLD6, FI);
                        }
                    }
                    //# 稼動ディレクトリに移動
                    //                cd ${CM_APWORK_DATE}
                    // メールテキストファイル名取得
                    String[] cmABgmtxBArgs = {"", MTEXT_FILE};
                    MainResultDto cmABgmtxBResult = cmABgmtxBServiceImpl.main(cmABgmtxBArgs.length, cmABgmtxBArgs);
                    String MTEXT_NAME = cmABgmtxBResult.result;
                    int RTN_Result = cmABgmtxBResult.exitCode;

                    if (RTN_Result != Rtn_OK) {
                        APLOG_WT("メールテキストファイル名取得エラー", FW);
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                        return cmABgmtxBResult.exitCode;
                    }
                    //    # メールテキストファイル内の置換文字列を変換
                    //    # ダウンロード日付
                    String content = FileUtil.readFile(MTEXT_NAME).replaceAll("%SYORI_DATE%", READ_FLD3)
                            .replaceAll("%SYORI_TIME%", READ_FLD4).replaceAll("%SYORI_ID%", READ_FLD2).replaceAll("%SYORI_NAME%", READ_FLD6)
                            .replaceAll("%FAILE_NAME%", READ_FLD5).replaceAll("%FAILE_CNT%", String.valueOf(FILE_LINECNT));

                    FileUtil.writeFile(TEMP_FILE2 + ".tmp2", content.toString());

                    //                   # メールテキストファイル
                    FileUtil.copyFile(TEMP_FILE2 + ".tmp2", TEMP_FILE2);
                    FileUtil.copyFile(TEMP_FILE2, getenv("CM_MAILTEXT") + "/" + MTEXT_FILE);

                    //    #  メール送信
                    int SYORI_STS = SYORI_STS_OK;
                    String[] paramList = {BTFLML_MAIL_TO, BTFLML_MAIL_FROM, BTFLML_MAIL_SUB, MTEXT_FILE};
                    int RTN_ML = cmABmailSTask.main(getExecuteBaseParam().add(paramList));
                    if (RTN_ML == Rtn_NG) {
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
                        SYORI_STS = SYORI_STS_WN;
                    } else if (RTN_ML != Rtn_OK) {
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("メール送信エラー").FW());
                        return Rtn_NG;
                    }
                    //  ################################################
                    //    #  処理済みステータス更新
                    //    ################################################
                    ShellExecuteDto shellExecuteDto2 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
                            .addEvn("CONNECT_MD", CONNECT_MD).addEvn("CM_MYPRGID", CM_MYPRGID).addEvn("SYORI_STS", String.valueOf(SYORI_STS)).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                            .addEvn("READ_FLD1", READ_FLD1).addEvn("READ_FLD2", READ_FLD2).addEvn("READ_FLD3", READ_FLD3).addEvn("READ_FLD4", READ_FLD4).execute();

                    RTN_VAL = shellExecuteDto2.getResult();
                    if (shellExecuteDto2.RTN0()) {
                        String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PMファイルダウンロード情報更新に失敗しました。SQLCODE=" + SQL_CD).FE());
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                        return Rtn_NG;
                    }

                }
            }
        }

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
