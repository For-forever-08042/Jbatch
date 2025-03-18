package jp.co.mcc.nttdata.batch.business.job.cmABdbdlS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTdlemB.CmBTdlemBServiceImpl;
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

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　テーブルデータ削除
 * #    プログラムID　：　cmABdbdlS
 * #
 * #    【処理概要】
 * #        指定されたテーブル略称に対して、削除パラメータおよび、保存期間情報に
 * #        したがって、対象テーブルと削除条件を編集し、データ削除を行う。
 * #
 * #    【引数説明】
 * #        -T  テーブル略称        :　対象のテーブル略称
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       49　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :        2012/10/18 SSI.吉岡  ： 初版
 * #      2.00 :        2012/12/19 SSI.上野  ： 削除対象日付全変換するよう修正
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmABdbdlSTask extends NtBasicTask {

    @Autowired
    CmABdbtrSTask cmABdbtrS;
    @Autowired
    CmABdbl2STask cmABdbl2S;
    @Autowired
    CmABfzipSTask cmABfzipS;
    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    CmABgprmBServiceImpl cmABgprmB;
    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTdlemBServiceImpl cmBTdlemB;
    @Autowired
    CmABgdldBServiceImpl cmABgdldB;

    final String ARG_OPT1 = "-T";//                   ###  テーブル略称
    String TBL_NAME = null;
    String DB_MD = null;
    String DB_SD = null;
    String DB_HD = null;
    String DB_BD = null;
    String DB_DELGRP = null;
    String DEL_TYPE = null;
    String DEL_WHERE = null;

    String DEL_YMD = null;
    String DEL_YM = null;
    String DEL_Y = null;
    String DEL_YMD2 = null;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().S(CM_MYPRGNAME));


        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 99;


        //#  パラメータファイル
        String PARAM_FILE = "cmABdbdfP";

        //#  その他
        String BKUP_IFS = IFS;

        //###########################################
        //#  引数チェック
        //###########################################
//        #  引数格納変数初期化
        String OPTION1 = "0";
        if (args.length != 2) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー  [" + ARG_ALL + "]").FW());
            return Rtn_NG;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (!StringUtils.equals(OPTION1, "0")) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 2) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION1 = args[i + 1];
                    i++;
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);
                    return Rtn_NG;
            }
        }
        //必須引数チェック
        if (StringUtils.equals(OPTION1, "0")) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        SYS_HHMMSS = DateUtil.getHHMMSS();
        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.createFolder(WORK_DIR, false);
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + pid;
        String TEMP_FILE3 = WORK_DIR + "/" + CM_MYPRGID + "03_" + pid;
        String TEMP_FILE4 = WORK_DIR + "/" + CM_MYPRGID + "04_" + pid;


        //###########################################
        //#  テーブル削除条件パラメータファイル名取得
        //###########################################
        MainResultDto resultDto = cmABgprmB.main(getExecuteBaseParam().add(PARAM_FILE));
        if (resultDto.exitCode != Rtn_OK) {
            APLOG_WT("テーブル削除条件パラメータファイル名取得エラー", FW);
            return Rtn_NG;
        }
        String PARAM_NAME = resultDto.result;

        //###########################################
        //#  パラメータファイル読み込み処理
        //###########################################
        String FILD_VALU = OPTION1;
        String PARAM_NAME_DATA = FileUtil.awk(PARAM_NAME, item -> FILD_VALU.equals(item.replaceAll("\\s+", " ").split(" ")[0]));
        FileUtil.writeFile(TEMP_FILE1, PARAM_NAME_DATA);
        int IN_LCNT = FileUtil.countLines(TEMP_FILE1);
        //#  取得した行がゼロ件または、複数件の場合はエラー
        if (IN_LCNT == 0) {
            FileUtil.deleteFile(TEMP_FILE1);
            APLOG_WT("削除未実施（パラメータファイルに該当データ未登録）　TBL略称＝[" + OPTION1 + "]", FE);
            return Rtn_NG;
        }
        if (IN_LCNT != 1) {
            FileUtil.deleteFile(TEMP_FILE1);
            APLOG_WT("パラメータファイル不正（該当データ複数存在）　TBL略称＝[" + OPTION1 + "]", FE);
            APLOG_WT("パラメータファイル取得行数　[" + IN_LCNT + "]", FI);
            return Rtn_NG;
        }


        //###########################################
        //#  パラメータファイルから抽出したレコードのフィールド数をチェック
        //###########################################
        String IN_FILE = TEMP_FILE1;
        int IN_FILD = 9;
        String TEMP_FILE2_DATA = FileUtil.awk(IN_FILE,
                item -> item.replaceAll("\\s+", " ").split(" ").length != IN_FILD);
        FileUtil.writeFile(TEMP_FILE2, TEMP_FILE2_DATA);
        IN_LCNT = FileUtil.countLines(TEMP_FILE2);
        //#  取得した行がゼロ件または、複数件の場合はエラー
        if (IN_LCNT != 0) {
            FileUtil.deleteFile(TEMP_FILE2);
            APLOG_WT("テーブル削除条件パラメータファイル形式エラー　TBL略称＝[" + OPTION1 + "]", FE);
            return Rtn_NG;
        }
        FileUtil.deleteFile(TEMP_FILE2);


        //###########################################
        //#  パラメータファイルから抽出したデータを１行読み込んでフィールド分割
        //###########################################
        ReadFileDto.getInstance().readFile(IN_FILE).loopBySize((data) -> {
            TBL_NAME = data[1];
            DB_MD = data[2];
            DB_SD = data[3];
            DB_HD = data[4];
            DB_BD = data[5];
            DB_DELGRP = data[6];
            DEL_TYPE = data[7];
            DEL_WHERE = data[8];
            return Rtn_OK;
        }, 9);


        //cd $CM_APWORK_DATE

        //###########################################
        //#  引数の格納
        //###########################################
        if (StringUtils.equals(DEL_TYPE, "no") || StringUtils.equals(DEL_TYPE, "NO")) {
            APLOG_WT("削除未実施（パラメータファイルに削除不要の設定）　TBL略称＝[" + OPTION1 + "]", FI);
            cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
            return Rtn_OK;
        }
        //###########################################
        //#  削除条件の設定値チェック
        //###########################################
        if (StringUtils.equals(DEL_WHERE, "no") || StringUtils.equals(DEL_WHERE, "NO")) {
            APLOG_WT("削除未実施（パラメータファイルに削除条件なしの設定）　TBL略称＝[" + OPTION1 + "]", FI);
            cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
            return Rtn_OK;
        }
        //###########################################
        //#  削除タイプの設定値チェック
        //###########################################
        if (!StringUtils.equals(DEL_TYPE, "rec") && !StringUtils.equals(DEL_TYPE, "REC")) {
            APLOG_WT("削除未実施（レコード単位の削除でない）　TBL＝[" + TBL_NAME + "]", FI);
            cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
            return Rtn_OK;
        }

        //###########################################
        //#  削除対象日付取得
        //###########################################
        resultDto = cmABgdldB.main(getExecuteBaseParam().add(DB_DELGRP));
        int RTN = resultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("データ削除日付取得エラー　TBL略称＝[" + OPTION1 + "]　テーブルGRP=" + DB_DELGRP + "　STATUS=" + RTN, FI);
            cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
            return Rtn_NG;
        }
        FileUtil.writeFile(TEMP_FILE2, resultDto.result);

        //###########################################
        //#  削除対象日付取得を種類ごとに分割
        //###########################################
        ReadFileDto.getInstance().readFile(TEMP_FILE2).loop4((READ2_FLD1, READ2_FLD2, READ2_FLD3, READ2_FLD4) -> {
            DEL_YMD = READ2_FLD1;
            DEL_YM = READ2_FLD2;
            DEL_Y = READ2_FLD3;
            DEL_YMD2 = READ2_FLD4;
            return Rtn_OK;
        });

        String SQL_WHERE = DEL_WHERE.replaceAll(";", " ");
        String CHG_TYPE = "0";
        if (!StringUtils.equals(DEL_YMD, "00000000")) {
            //        ###########################################
            //        #  削除条件　に、"D:"　が存在する場合
            //        ###########################################
            if (SQL_WHERE.contains("D:")) {
                CHG_TYPE = "D";
                String D_YYYYMMDD = DEL_YMD;
                String D_YYYY = DEL_YMD.substring(0, 4);
                String D_MM = DEL_YMD.substring(4, 6);
                String D_DD = DEL_YMD.substring(6, 8);
                SQL_WHERE = SQL_WHERE.replaceAll("D:YYYYMMDD", D_YYYYMMDD);
                SQL_WHERE = SQL_WHERE.replaceAll("D:YYYY", D_YYYY);
                SQL_WHERE = SQL_WHERE.replaceAll("D:MM", D_MM);
                SQL_WHERE = SQL_WHERE.replaceAll("D:DD", D_DD);
            }
        }

        if (!StringUtils.equals(DEL_YM, "000000")) {
            //        ###########################################
            //         削除条件　に、"M:"　が存在する場合
            //        ###########################################
            if (SQL_WHERE.contains("M:")) {
                CHG_TYPE = "M";
                String M_YYYYMM = DEL_YM;
                String M_YYYY = DEL_YM.substring(0, 4);
                String M_MM = DEL_YM.substring(4, 6);
                SQL_WHERE = SQL_WHERE.replaceAll("M:YYYYMM", M_YYYYMM);
                SQL_WHERE = SQL_WHERE.replaceAll("M:YYYY", M_YYYY);
                SQL_WHERE = SQL_WHERE.replaceAll("M:MM", M_MM);
            }
        }


        if (!StringUtils.equals(DEL_Y, "0000")) {
            //        ###########################################
            //         削除条件　に、"Y:"　が存在する場合
            //        ###########################################
            if (SQL_WHERE.contains("Y:")) {
                CHG_TYPE = "Y";
                String Y_YYYY = DEL_Y;
                SQL_WHERE = SQL_WHERE.replaceAll("Y:YYYY", Y_YYYY);
            }
        }
        if (!StringUtils.equals(DEL_YMD2, "00000000")) {
            //        ###########################################
            //        #  削除条件　に、"D:"　が存在する場合
            //        ###########################################
            if (SQL_WHERE.contains("I:")) {
                CHG_TYPE = "I";
                String I_YYYYMMDD = DEL_YMD2;
                String I_YYYY = DEL_YMD2.substring(0, 4);
                String I_MM = DEL_YMD2.substring(4, 6);
                String I_DD = DEL_YMD2.substring(6, 8);
                SQL_WHERE = SQL_WHERE.replaceAll("I:YYYYMMDD", I_YYYYMMDD);
                SQL_WHERE = SQL_WHERE.replaceAll("I:YYYY", I_YYYY);
                SQL_WHERE = SQL_WHERE.replaceAll("I:MM", I_MM);
                SQL_WHERE = SQL_WHERE.replaceAll("I:DD", I_DD);
            }
        }
        if (StringUtils.equals("0", CHG_TYPE)) {
            APLOG_WT("データ削除条件編集エラー　TBL略称＝[" + OPTION1 + "]　テーブルGRP=" + DB_DELGRP + "\n　[" + SQL_WHERE + "]", FW);
            APLOG_WT("データ削除日付　D:[" + DEL_YMD + "]　M:[" + DEL_YM + "]　Y:[" + DEL_Y + "]　I:[" + DEL_YMD2 + "]", FI);
            cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        //###########################################
        //#  コネクト先DB毎に削除実施
        //###########################################
        //# 制度、管理、明細、商談　の順に実施
        String[] Conn_DB_TBL = {DB_SD, DB_MD, DB_HD, DB_BD};

        String WK_SD = "0";
        String WK_MD = "0";
        String WK_HD = "0";
        String WK_BD = "0";
        String Conn_DB = "";
        for (String DB_Y_N : Conn_DB_TBL) {
            if (StringUtils.equals(WK_SD, "0")) {
                WK_SD = "1";
                if (StringUtils.equals(DB_Y_N, "YES") || StringUtils.equals(DB_Y_N, "yes") || StringUtils.equals(DB_Y_N, "Yes")) {
                    Conn_DB = CONNECT_SD;
                    APLOG_WT("削除対象（顧客制度ＤＢ）", FI);
                } else {
                    Conn_DB = "";
                    APLOG_WT("削除対象外（顧客制度ＤＢスキップ）", FI);
                    continue;
                }
            } else if (StringUtils.equals(WK_MD, "0")) {
                WK_MD = "1";
                if (StringUtils.equals(DB_Y_N, "YES") || StringUtils.equals(DB_Y_N, "yes") || StringUtils.equals(DB_Y_N, "Yes")) {
                    Conn_DB = CONNECT_MD;
                    APLOG_WT("削除対象（顧客管理ＤＢ）", FI);
                } else {
                    Conn_DB = "";
                    APLOG_WT("削除対象外（顧客管理ＤＢスキップ）", FI);
                    continue;
                }
            } else if (StringUtils.equals(WK_HD, "0")) {
                WK_HD = "1";
                if (StringUtils.equals(DB_Y_N, "YES") || StringUtils.equals(DB_Y_N, "yes") || StringUtils.equals(DB_Y_N, "Yes")) {
                    Conn_DB = CONNECT_HD;
                    APLOG_WT("削除対象（顧客明細ＤＢ）", FI);
                } else {
                    Conn_DB = "";
                    APLOG_WT("削除対象外（顧客明細ＤＢスキップ）", FI);
                    continue;
                }
            } else if (StringUtils.equals(WK_BD, "0")) {
                WK_BD = "1";
                if (StringUtils.equals(DB_Y_N, "YES") || StringUtils.equals(DB_Y_N, "yes") || StringUtils.equals(DB_Y_N, "Yes")) {
                    Conn_DB = CONNECT_BD;
                    APLOG_WT("削除対象（商談管理ＤＢ）", FI);
                } else {
                    Conn_DB = "";
                    APLOG_WT("削除対象外（商談管理ＤＢスキップ）", FI);
                    continue;
                }
            } else {
                Conn_DB = "";
                APLOG_WT("削除対象外[" + DB_Y_N + "]", FI);
                ;
                continue;
            }
            //        ###########################################
            //        #  SQL実行
            //        ###########################################
            APLOG_WT("削除実施（レコード単位削除）TBL＝[" + TBL_NAME + "]\n削除条件[" + SQL_WHERE + "]", FI);
            ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID).defaultEvn(this)
                    .addEvn("TBL_NAME", TBL_NAME)
                    .addEvn("SQL_WHERE", SQL_WHERE)
                    .addEvn("TEMP_FILE3", TEMP_FILE3)
                    .addEvn("TEMP_FILE4", TEMP_FILE4)
                    .addEvn("Conn_DB", Conn_DB).execute();

            if (shellExecuteDto.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_FILE(TEMP_FILE4);
                APLOG_WT("SQLエラー（" + TBL_NAME + "のdelete）[" + 1 + "]　[SQLCODE：" + SQL_CD + "]", FE);
                return Rtn_NG;
            }
            String DEL_MSG = FileUtil.echoAndgrep(FileUtil.readFile(TEMP_FILE4), "行が削除されました。");
            APLOG_WT("SQL実行（" + TBL_NAME + "のdelete）　正常\n[" + DEL_MSG + "]", FI);
            FileUtil.deleteFile(TEMP_FILE3 + ".sql");
            FileUtil.deleteFile(TEMP_FILE4);
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

        return Rtn_OK;
    }
}
