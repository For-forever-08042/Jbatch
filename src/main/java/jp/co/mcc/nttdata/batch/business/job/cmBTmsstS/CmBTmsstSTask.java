package jp.co.mcc.nttdata.batch.business.job.cmBTmsstS;

import jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbexS.CmABdbexSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbimS.CmABdbimSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTmsstB.CmBTmsstBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
//        #-------------------------------------------------------------------------------
//        #    名称          ：  店表示情報情報作成
//        #    プログラムID  ：  cmBTmsstS
//        #
//        #    【処理概要】
//        #      CFOSより連動される「店舗マスタ」ファイルをPS店表示情報にロードする。
//        #      cmBTmsstBを起動し、MSブロック情報から階層情報を取得してPS店表示情報に設定する。
//        #      PS店表示情報に本部店番のレコードを登録する。
//        #
//        #    【引数説明】
//        #       -DEBUG or -debug        : （任意）デバッグモードでの実行（トレース出力機能）
//        #
//        #    【戻り値】
//        #       10     ：  正常
//        #       49     ：  異常
//        #-------------------------------------------------------------------------------
//        #    稼働環境
//        #      Red Hat Enterprise Linux 6
//        #
//        #    改定履歴
//        #      1.00 :   2012/11/16 SSI.吉岡  ： 初版
//        #      2.00 :   2013/01/30 SSI.本田  ： Loaderでエラー発生時のリカバリ処理追加
//        #      3.00 :   2013/02/08 SSI.本田  ： 本部店番の登録処理を削除
//        #      4.00 :   2013/03/05 SSI.本田  ： 閉鎖店登録処理を追加
//        #                                       分析対象フラグ更新処理を追加
//        #      5.00 :   2013/03/30 SSI.本田  ： 2013/4/1以降開店の店舗を販社=CFとして扱う
//        #      6.00 :   2015/07/13 SSI.上野  ： Loaderでエラー発生時のリカバリ処理前に
//        #                                       テーブルTruncate追加
//        #      7.00 :   2018/05/09 SSI.武藤  ： フジモトHD連動対応に伴う変更
//        #                                       分析対象フラグ２更新処理を追加
//        #-------------------------------------------------------------------------------
//        #  $Id:$
//        #-------------------------------------------------------------------------------
//        #  Copyright (C) 2012 NTT DATA CORPORATION
//        #-------------------------------------------------------------------------------
//
//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

/**
 * ksh logic
 */
@Slf4j
@Component
public class CmBTmsstSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgprmBServiceImpl cmABgprmBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    CmBTmsstBServiceImpl cmBTmsstBServiceImpl;

    @Autowired
    CmABdbexSTask cmABdbexSTask;

    @Autowired
    CmABdbl2STask cmABdbl2STask;

    @Autowired
    CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    CmABdbimSTask cmABdbimSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        CM_MYPRGNAME = "店表示情報作成";
        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));
        //###########################################
        //#  定数定義
        //###########################################

        String DB_KBN = "SD";
        String CTLFile = "ldrPSmise";
        String CTLFileName = "ldrPSmise.ctl";
        String RECOVERY_DMP_NAME = "MISE_BK";

        String CM_APSQL = getenv(C_aplcom1Service.CM_APSQL);

        //#  DB接続先

        //#  パラメータファイル
        String PARAM_FILE = "cmBTmsstP";

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

        // 引数の数チェック
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  パラメータファイル名取得
        //###########################################
        MainResultDto cmABgprmBResult = cmABgprmBServiceImpl.main(getExecuteBaseParam().add(PARAM_FILE));
        String PARAM_NAME = cmABgprmBResult.result;
        int RTN = cmABgprmBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", "-FE");
            return Rtn_NG;
        }

        //###########################################
        //#  バッチ処理日付の取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int cmABgdatBResultRTN = cmABgdatBResult.exitCode;
        if (cmABgdatBResultRTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  sqlファイル存在チェック
        //###########################################
        if (!FileUtil.isExistFile(CM_APSQL + "/loader/" + CTLFileName)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  店舗マスタファイル存在チェック
        //###########################################
        if (!FileUtil.isFileExistByRegx(CM_APWORK_DATE, "^.{14}_TM_TEMPO.csv")) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗マスタファイルが存在しません").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_NG;
        }

        //        FILENAME_MASTER=`basename ${FILE_MASTER}`

        //################################################
        //#  店舗マスタファイルを作業ディレクトリにコピー
        //################################################
        if (!FileUtil.copyFileByRegx(CM_APWORK_DATE, CM_APWORK_DATE,"MISE.dat", "^.{14}_TM_TEMPO.csv")) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗マスタファイル複写エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //##################################################
        //#  店表示情報のExport（リカバリ用）
        //##################################################
        int cmABdbexSRNT = cmABdbexSTask.main(getExecuteBaseParam().sD("SD")
                .sF(RECOVERY_DMP_NAME)
                .sT("PS店表示情報").sG().sS());
        if (Rtn_OK != cmABdbexSRNT) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Exportの実行に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return cmABdbexSRNT;
        }


        //##################################################
        //#  COPYでデータロード（cmABdbl2S）を呼び出す
        //##################################################
        int cmABdbl2SRNT = cmABdbl2STask.main(getExecuteBaseParam().sC(CTLFile).sD(DB_KBN));
        if (Rtn_OK != cmABdbl2SRNT) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FE());
            //    #  テーブルTruncate
            int RTN_TRUNCATE = cmABdbtrSTask.main(getExecuteBaseParam().sD("SD").sT("PS店表示情報"));
            if (Rtn_OK != RTN_TRUNCATE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("データリカバリの実行に失敗しました").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return cmABdbl2SRNT;
            } else {
                //                #  dmpファイルからリカバリ
                int RTN_RECOVER = cmABdbimSTask.main(getExecuteBaseParam().sD("SD").sT("PS店表示情報").sF(RECOVERY_DMP_NAME));
                if (Rtn_OK != RTN_RECOVER) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("データリカバリの実行に失敗しました").FE());
                }
                FileUtil.deleteFile(CM_APWORK_DATE + "/" + RECOVERY_DMP_NAME + ".dmp");
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return cmABdbl2SRNT;
            }
        }

        //###########################################
        //#  プログラム実行
        //###########################################
        MainResultDto cmBTmsstBResult = cmBTmsstBServiceImpl.main(getExecuteBaseParam().add(args));
        int cmBTmsstBRTN = cmBTmsstBResult.exitCode;
        if (cmBTmsstBRTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //# 閉鎖店登録
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this).CM_APSQL(CM_APSQL).execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（閉鎖店登録）に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        String SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
        Pattern pattern = Pattern.compile("1行が作成されました");
        Matcher matcher = pattern.matcher(SQL_CD);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（閉鎖店登録）) " + count + "行が作成されました。").FI());

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //# 分析対象フラグ更新
        //###########################################
        //# 分析対象フラグ更新用ＳＱＬ作成
        StringBuffer buffer = new StringBuffer();
        buffer.append("").append("\n")
                .append("").append("\n")
                .append("\\set bind_store_no :1 ").append("\n")
                .append("\\set bind_flg :2 ").append("\n")
                .append("update PS店表示情報 set 分析対象フラグ = :bind_flg where 店番号 = :bind_store_no;").append("\n");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_1.sql", buffer.toString());
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_2.sql", "");


        String[] paramNamelist = FileUtil.readFile(PARAM_NAME).split("\n");

        if (paramNamelist != null && paramNamelist.length > 0) {
            for (int i = 0; i < paramNamelist.length; i++) {
                String[] liselist = paramNamelist[i].split("\\s+");
                if (liselist.length >= 3) {
                    String WK_SYORI_KBN = liselist[0];
                    String WK_STORE_NO = liselist[1];
                    String WK_VALUE = liselist[2];
                    String WK_COMMENT = WK_STORE_NO.substring(0, 1);
                    if ("#".equals(WK_COMMENT)) {
                        continue;
                    } else if ("ANALYZE_FLG".equals(WK_SYORI_KBN)) {

                        FileUtil.writeFileByAppend(CM_APWORK_DATE + "/" + CM_MYPRGID + "_2.sql", "@" + CM_MYPRGID + "_1.sql " + WK_STORE_NO + " " + WK_VALUE + ";\n", SystemConstant.UTF8);
                    }
                }
            }
        }

        ShellExecuteDto shellExecuteDto4 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this).execute();

        if (shellExecuteDto4.RTN0()) {
            // SQL_CD = Arrays.stream(FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log")
            //         .replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 8))
            //         .collect(Collectors.joining("\n"));
            SQL_CD = "";
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（分析対象フラグ更新）に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
        pattern = Pattern.compile("行が更新されました");
        matcher = pattern.matcher(SQL_CD);
        count = 0;
        while (matcher.find()) {
            count++;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（分析対象フラグ更新）) " + count + "行が更新されました。").FI());

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");


        //###########################################
        //# 2013/4/1 以降開店の店舗の販社コードの更新
        //###########################################
        ShellExecuteDto shellExecuteDto5 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_05")
                .defaultEvn(this).execute();

        if (shellExecuteDto5.RTN0()) {
            // SQL_CD = Arrays.stream(FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log")
            //         .replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 8))
            //         .collect(Collectors.joining("\n"));
            SQL_CD = "";
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（旧販社コード更新（CF販社））に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
//        pattern = Pattern.compile("行が更新されました");
//        matcher = pattern.matcher(SQL_CD);
//        count = 0;
//        while (matcher.find()) {
//            count++;
//        }

        SQL_CD=  Arrays.stream(SQL_CD.split("\n")).filter(item->item.contains("行が更新されました")).collect(Collectors.joining("\n"));

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（旧販社コード更新（CF販社））) " + SQL_CD).FI());

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //# 旧販社コード更新
        //###########################################
        //# 旧販社コード更新用ＳＱＬ作成
        buffer = new StringBuffer();
        buffer.append("").append("\n")
                .append("").append("\n")
                .append("\\set bind_store_no :1 ").append("\n")
                .append("\\set bind_hansha_cd :2 ").append("\n")
                .append("update PS店表示情報 set 旧販社コード = :bind_hansha_cd where 店番号 = :bind_store_no;").append("\n");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_3.sql", buffer.toString());
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_4.sql", "");

        paramNamelist = FileUtil.readFile(PARAM_NAME).split("\n");
        if (paramNamelist != null && paramNamelist.length > 0) {
            for (int i = 0; i < paramNamelist.length; i++) {
                String[] liselist = paramNamelist[i].split("\\s+");
                if (liselist.length >= 3) {
                    String WK_SYORI_KBN = liselist[0];
                    String WK_STORE_NO = liselist[1];
                    String WK_VALUE = liselist[2];
                    String WK_COMMENT = WK_STORE_NO.substring(1);
                    if ("#".equals(WK_COMMENT)) {
                        continue;
                    } else if ("HANSHA_CD".equals(WK_SYORI_KBN)) {
                        FileUtil.writeFileByAppend(CM_APWORK_DATE + "/" + CM_MYPRGID + "_4.sql", "@" + CM_MYPRGID + "_3.sql " + WK_STORE_NO + " " + WK_VALUE + ";\n", SystemConstant.UTF8);
                    }
                }
            }
        }

        ShellExecuteDto shellExecuteDto8 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_08")
                .defaultEvn(this).execute();


        if (shellExecuteDto8.RTN0()) {
            // SQL_CD = Arrays.stream(FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log")
            //         .replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 8))
            //         .collect(Collectors.joining("\n"));
            SQL_CD = "";
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（旧販社コード更新）に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
        pattern = Pattern.compile("行が更新されました");
        matcher = pattern.matcher(SQL_CD);
        count = 0;
        while (matcher.find()) {
            count++;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（旧販社コード更新）) " + count + "行が更新されました。").FI());

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //# 分析対象フラグ２更新
        //###########################################
        //# 分析対象フラグ２更新用ＳＱＬ作成
        buffer = new StringBuffer();
        buffer.append("").append("\n")
                .append("").append("\n")
                .append("\\set bind_store_no :1 ").append("\n")
                .append("\\set bind_flg :2 ").append("\n")
                .append("update PS店表示情報 set 分析対象フラグ２ = :bind_flg where 店番号 = :bind_store_no;").append("\n");
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_5.sql", buffer.toString());
        FileUtil.writeFile(CM_APWORK_DATE + "/" + CM_MYPRGID + "_6.sql", "");

        paramNamelist = FileUtil.readFile(PARAM_NAME).split("\n");
        if (paramNamelist != null && paramNamelist.length > 0) {
            for (int i = 0; i < paramNamelist.length; i++) {
                String[] liselist = paramNamelist[i].split("\\s+");
                if (liselist.length >= 3) {
                    String WK_SYORI_KBN = liselist[0];
                    String WK_STORE_NO = liselist[1];
                    String WK_VALUE = liselist[2];
                    String WK_COMMENT = WK_STORE_NO.substring(1);
                    if ("#".equals(WK_COMMENT)) {
                        continue;
                    } else if ("ANALYZE_FLG2".equals(WK_SYORI_KBN)) {
                        FileUtil.writeFileByAppend(CM_APWORK_DATE + "/" + CM_MYPRGID + "_6.sql", "@" + CM_MYPRGID + "_5.sql " + WK_STORE_NO + " " + WK_VALUE + ";\n", SystemConstant.UTF8);
                    }
                }
            }
        }

        ShellExecuteDto shellExecuteDto11 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_11").defaultEvn(this).execute();
//
        if (shellExecuteDto11.RTN0()) {
            // SQL_CD = Arrays.stream(FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log")
            //         .replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 8))
            //         .collect(Collectors.joining("\n"));
            SQL_CD = "";
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（分析対象フラグ２更新）に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
        pattern = Pattern.compile("行が更新されました");
        matcher = pattern.matcher(SQL_CD);
        count = 0;
        while (matcher.find()) {
            count++;
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（分析対象フラグ２更新）) " + count + "行が更新されました。").FI());

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //# 管理DBのPS店表示情報をTRUNCATE
        //###########################################
        RTN = cmABdbtrSTask.main(getExecuteBaseParam().sD("MD").sT("PS店表示情報"));
        if (Rtn_OK != RTN) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報のTRUNCATEに失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //#############################################################
        //# 制度DBのPS店表示情報のデータを管理DBのPS店表示情報にINSERT
        //#############################################################
        ShellExecuteDto shellExecuteDto12 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_12")
                .defaultEvn(this)
                .addEvn("CM_ORA_DBLINK_MD", getenv("CM_ORA_DBLINK_MD")).execute();

//        ShellClientManager.executeCommand(shellExecuteDto12);
//        log.debug("shell execute sqlplus end: {}, {}", shellExecuteDto12.getResult(), shellExecuteDto12.getError());

        if (shellExecuteDto12.RTN0()) {
            // SQL_CD = Arrays.stream(FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log")
            //         .replaceAll("ORA-", "\nORA-").split("\n"))
            //         .filter(item -> item.indexOf("ORA-") >= 0)
            //         .map(item -> item.substring(4, 8))
            //         .collect(Collectors.joining("\n"));
            SQL_CD = "";
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("PS店表示情報（顧客管理ＤＢ登録）に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
        SQL_CD=  Arrays.stream(SQL_CD.split("\n")).filter(item->item.contains("が作成されました")).collect(Collectors.joining("\n"));
//        matcher = pattern.matcher(SQL_CD);
//        count = 0;
//        while (matcher.find()) {
//            count++;
//        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("SQL実行（PS店表示情報（顧客管理ＤＢ登録）) " + SQL_CD).FI());

        // FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //#  店舗マスタファイルを削除
        //###########################################
        FileUtil.deleteFile(CM_APWORK_DATE + "MISE.dat");
        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^.{14}_TM_TEMPO.csv");
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + RECOVERY_DMP_NAME + ".dmp");

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
