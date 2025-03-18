package jp.co.mcc.nttdata.batch.business.job.cmBTpcreS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbrnS.CmABdbrnSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.NTRegexUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ポイント履歴テーブル作成シェル
 * #    プログラムID  ：  cmBTpcreS
 * #
 * #    【処理概要】
 * #        削除対象のテーブル種別を起動引数に指定することで、テーブル種別に対応した
 * #        テーブルで保存期間が経過したテーブルをTruncateしたのち、翌日、または翌月、
 * #        または翌年のテーブル名にRenameする機能。
 * #        引数で指定するテーブル種別に対応したテーブル名などは、パラメータファイル
 * #        設定しておくことを前提とする。
 * #
 * #    【引数説明】
 * #        対象テーブル種別    :  対象のテーブル種別（パラメータファイルに設定）
 * #                               -SD  ： 顧客制度データベースのテーブルを対象
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :        2012/12/13 SSI.横山直人  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTpcreSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    private CmABgprmBServiceImpl cmABgprmB;

    @Autowired
    private CmABdbrnSTask cmABdbrnS;
    @Autowired
    CmABdbtrSTask cmABdbtrS;
    @Autowired
    private CmABgdldBServiceImpl cmABgdldB;

    private String BAT_YYYYMM_Y = "";
    private String DEL_D_DATE = "";
    private String DEL_M_DATE = "";
    private String DEL_Y_DATE = "";
    private String DEL_I_DATE = "";
    private int RTN;
    private int IN_LCNT;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#  引数定義
        final String ARG_OPT1 = "-SD";      //###  顧客制度データベースのテーブルを対象

        //#  パラメータファイル
        String PARAM_FILE = "cmBTpcreP";

        //###########################################
        //#  引数チェック
        //###########################################

        //#  引数格納変数初期化
        String OPTION1 = "0";

        if (args.length == 0) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (!StringUtils.equals(OPTION1, "0")) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION1 = args[i];
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);
                    return Rtn_NG;

            }
        }

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK_DATE;
        if (!FileUtil.isExistFile(WORK_DIR)) {
            if (!FileUtil.createFolder(WORK_DIR, false)) {
                APLOG_WT("テンポラリファイル用のディレクトリ作成エラー[" + WORK_DIR + "]", FE);

                return Rtn_NG;
            }
        }
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + pid;
        String TEMP_FILE3 = WORK_DIR + "/" + CM_MYPRGID + "03_" + pid;
        String TEMP_FILE4 = WORK_DIR + "/" + CM_MYPRGID + "04_" + pid;
        String TEMP_FILE5 = WORK_DIR + "/" + CM_MYPRGID + "05_" + pid;

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 翌日日付取得
        MainResultDto cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam().add("-DT"));
        RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("翌日バッチ処理日付取得エラー[" + RTN + "]", FW);
            return Rtn_NG;
        }
        String BAT_YYYYMMDD_Y = cmABgdatBResult.result;

        //# 当日日付取得
        cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam());
        RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", FE);
            return Rtn_NG;
        }
        String BAT_YYYYMMDD_T = cmABgdatBResult.result;

        //# 翌月、翌年算出
        String BAT_YYYY_T = BAT_YYYYMMDD_T.substring(0, 4);
        String BAT_YYYY_Y = Integer.toString(Integer.parseInt(BAT_YYYY_T) + 1);

        String BAT_MM_Y = BAT_YYYYMMDD_T.substring(4, 6);
        if (StringUtils.equals("12", BAT_MM_Y)) {
            BAT_MM_Y = "01";
            BAT_YYYYMM_Y = BAT_YYYY_Y + BAT_MM_Y;
        } else {
            BAT_MM_Y = String.format("%02d", Integer.parseInt(BAT_MM_Y) + 1);
            BAT_YYYYMM_Y = BAT_YYYY_T + BAT_MM_Y;
        }

        //###########################################
        //#  DB接続先
        //###########################################
//        setConnectConf();

        //###########################################
        //#  履歴テーブル作成パラメータファイル名取得
        //###########################################
        MainResultDto cmABgprmBResult = cmABgprmB.main(getExecuteBaseParam().add(PARAM_FILE));
        RTN = cmABgprmBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("履歴テーブル作成パラメータファイル名取得エラー", FE);
            return Rtn_NG;
        }
        String PARAM_NAME = cmABgprmBResult.result;

        //###########################################
        //#  パラメータファイル抽出処理
        //###########################################
        //#  パラメータファイルから条件に一致する行を取得
        String FILD_VALU = OPTION1;
        if (!StringUtils.equals(FILD_VALU, "0")) {
            String TEMP_FILE1_DATA = FileUtil.awk(PARAM_NAME, item -> FILD_VALU.equals(item.replaceAll("\\s+", " ").split(" ")[0]));
            RTN = FileUtil.writeFile(TEMP_FILE1, TEMP_FILE1_DATA);
            if (RTN != 0) {
                APLOG_WT("パラメータファイルより顧客制度（" + FILD_VALU + "）の対象データ取得エラー  [" + RTN + "]", FE);
                return Rtn_NG;
            }
        }

        //##################################################################
        //#  パラメータファイルから抽出したレコードのフィールド数をチェック
        //##################################################################
        String IN_FILE = TEMP_FILE1;
        int IN_FILD = 4;
        //awk '{ if (NF != "'"${IN_FILD}"'") print $0 }' ${IN_FILE} > ${TEMP_FILE2} 2>/dev/null
        String TEMP_FILE2_DATA = FileUtil.awk(IN_FILE,
                item -> item.replaceAll("\\s+", " ").split(" ").length != IN_FILD);
        FileUtil.writeFile(TEMP_FILE2, TEMP_FILE2_DATA);
        IN_LCNT = FileUtil.countLines(TEMP_FILE2);
        if (IN_LCNT != 0) {
            FileUtil.deleteFile(TEMP_FILE2);
            APLOG_WT("履歴テーブル作成パラメータファイル形式エラー（フィールド数不正）", FE);
            return Rtn_NG;
        }

        FileUtil.deleteFile(TEMP_FILE2);

        //################################################
        //#  パラメータファイルから抽出したデータ読み込む
        //################################################
        ReadFileDto readFileDto = ReadFileDto.getInstance().readFile(TEMP_FILE1).loop4((READ_FLD1, READ_FLD2, READ_FLD3, READ_FLD4) -> {
            String TBL_SBT = READ_FLD1;
            String TBL_NAME = READ_FLD2;
            String TBL_ID = READ_FLD3;
            String HZN_KKN_KBN = READ_FLD4;

            String CHG_VALUE = "0";
            String CHG_NAME = "";
            int BKUP_FLG = 0;


//          String  TBL_TAIL_YMD= NTRegexUtil.find(".*(YYYYMMDD)",TBL_NAME) ;
//          String  TBL_TAIL_YM= NTRegexUtil.find(".*(YYYYMM)",TBL_NAME) ;
//          String  TBL_TAIL_Y_BK= NTRegexUtil.find(".*(YYYY_BK)",TBL_NAME) ;
//          String  TBL_TAIL_Y= NTRegexUtil.find(".*(YYYY)",TBL_NAME) ;

            //###########################################
            //#  リネーム後可変部の編集
            //###########################################
            if (StringUtils.contains(TBL_NAME, "YYYYMMDD")) {
                CHG_VALUE = "1";
                CHG_NAME = BAT_YYYYMMDD_Y;
            } else if (StringUtils.contains(TBL_NAME, "YYYYMM")) {
                CHG_VALUE = "2";
                CHG_NAME = BAT_YYYYMM_Y;
            } else if (StringUtils.contains(TBL_NAME, "YYYY_BK")) {
                CHG_VALUE = "3";
                CHG_NAME = BAT_YYYY_Y;
                BKUP_FLG = 1;
            } else if (StringUtils.contains(TBL_NAME, "YYYY")) {
                CHG_VALUE = "3";
                CHG_NAME = BAT_YYYY_Y;
            }

            //###########################################
            //#  パラメータファイル整合性チェック
            //###########################################
            if (StringUtils.equals("0", CHG_VALUE)) {
                APLOG_WT("パラメータファイルのテーブル名に年月日等の可変部がないテーブルは指定不可  [" + TBL_NAME + "]", FE);
                return Rtn_NG;
            }
            //#  年月日指定チェック
            if (StringUtils.equals("1", CHG_VALUE) && !StringUtils.equals("D", HZN_KKN_KBN) && !StringUtils.equals("I", HZN_KKN_KBN)) {
                APLOG_WT("パラメータファイルのテーブル名の年月日の可変部と、保存期間使用種別が不整合  [" + TBL_NAME + "][" + HZN_KKN_KBN + "]", FE);
                return Rtn_NG;
            }

            //#  年月指定チェック
            if (StringUtils.equals("2", CHG_VALUE) && !StringUtils.equals("M", HZN_KKN_KBN) && !StringUtils.equals("I", HZN_KKN_KBN)) {
                APLOG_WT("パラメータファイルのテーブル名の年月の可変部と、保存期間使用種別が不整合  [" + TBL_NAME + "][" + HZN_KKN_KBN + "]", FE);
                return Rtn_NG;
            }

            //#  年指定チェック
            if (StringUtils.equals("3", CHG_VALUE) && !StringUtils.equals("Y", HZN_KKN_KBN) && !StringUtils.equals("I", HZN_KKN_KBN)) {
                APLOG_WT("パラメータファイルのテーブル名の年の可変部と、保存期間使用種別が不整合  [" + TBL_NAME + "][" + HZN_KKN_KBN + "]", FE);
                return Rtn_NG;
            }

            //###########################################
            //#  テーブル名をLIKE検索用に変換
            //###########################################
            String SQL_TBL_NAME = TBL_NAME.replaceFirst("YYYYMMDD", "________");
            SQL_TBL_NAME = SQL_TBL_NAME.replaceFirst("YYYYMM", "______");
            SQL_TBL_NAME = SQL_TBL_NAME.replaceFirst("YYYY", "____");

            String BASE_TBL_NAME = TBL_NAME.replaceFirst("YYYYMMDD", "");
            BASE_TBL_NAME = BASE_TBL_NAME.replaceFirst("YYYYMM", "");
            BASE_TBL_NAME = BASE_TBL_NAME.replaceFirst("YYYY", "");

            String NEW_BKUP_TBL = "";
            if (BKUP_FLG == 1) {
                NEW_BKUP_TBL = TBL_NAME.replaceFirst("YYYY", CHG_NAME);
            }

            //###########################################
            //#  削除対象日付取得
            //###########################################
            MainResultDto cmABgdldBResult = cmABgdldB.main(getExecuteBaseParam().add(TBL_ID));
            RTN = cmABgdldBResult.exitCode;
            if (RTN != Rtn_OK) {
                APLOG_WT("データ削除日付取得エラー  テーブルID=" + TBL_ID + "[" + RTN + "]", FE);
                return Rtn_NG;
            }
            String DEL_DATE1234 = cmABgdldBResult.result;

            String[] DEL_DATE1234_arr = DEL_DATE1234.split(" ");
            DEL_D_DATE = DEL_DATE1234_arr[0];
            DEL_M_DATE = DEL_DATE1234_arr[1];
            DEL_Y_DATE = DEL_DATE1234_arr[2];
            DEL_I_DATE = DEL_DATE1234_arr[3];

            //###########################################
            //#  テーブル名取得SQL文編集
            //###########################################
            //#SQL_VALUE="select min(TABLE_NAME) as MIN_TABLE_NAME, max(TABLE_NAME) as MAX_TABLE_NAME "
            String SQL_VALUE = "select concat(min(TABLENAME), ' ', max(TABLENAME)) as MAX_TABLE_NAME ";
            SQL_VALUE = SQL_VALUE + "  from PG_TABLES ";
            SQL_VALUE = SQL_VALUE + "  where TABLENAME like '" + SQL_TBL_NAME + "'";

            String DB_CONN = "";
            String DB_CONN_VALUE = "";
            if (StringUtils.equals(ARG_OPT1, TBL_SBT)) {
                DB_CONN_VALUE = CONNECT_SD;
                DB_CONN = "SD";
            }

            //###########################################
            //#  テーブル名取得SQL実行
            //###########################################
            ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                    .defaultEvn(this)
                    .addEvn("SQL_VALUE", SQL_VALUE)
                    .addEvn("DB_CONN_VALUE", DB_CONN_VALUE)
                    .addEvn("TEMP_FILE3", TEMP_FILE3)
                    .addEvn("TEMP_FILE4", TEMP_FILE4)
                    .execute();
            if (shellExecuteDto.RTN0()) {
                RTN = Integer.parseInt(shellExecuteDto.result);
                String SQL_CD = FileUtil.SQL_CD_ORA_FILE(TEMP_FILE4);
                APLOG_WT("SQLエラー（USER_TABLES より、" + SQL_TBL_NAME + "のselect）[" + RTN + "]  [SQLCODE：" + SQL_CD + "]", FE);
                return Rtn_NG;
            }
            //cat ${TEMP_FILE4} | awk '{ if (NF == "'"2"'") print $0 }' > ${TEMP_FILE5}
            String TEMP_FILE5_DATA = FileUtil.awk(TEMP_FILE4, item -> item.replaceAll("\\s+", " ").split(" ").length == 2);
            FileUtil.writeFile(TEMP_FILE5, TEMP_FILE5_DATA);
            //IN_LCNT=`wc -l ${TEMP_FILE5} |  awk '{ print $1}'`
            IN_LCNT = FileUtil.countLines(TEMP_FILE5);

            //#  取得した行がゼロ件の場合はLOOPの先頭へ進む
            if (0 == IN_LCNT) {
                FileUtil.deleteFile(TEMP_FILE3);
                FileUtil.deleteFile(TEMP_FILE4);
                FileUtil.deleteFile(TEMP_FILE5);
                return Rtn_OK;
            }


            String MIN_TBL = FileUtil.awk(TEMP_FILE5, item -> item.replaceAll("\\s+", " ").split(" ").length == 2, 0);
            String MAX_TBL = FileUtil.awk(TEMP_FILE5, item -> item.replaceAll("\\s+", " ").split(" ").length == 2, 1);

            FileUtil.deleteFile(TEMP_FILE3);
            FileUtil.deleteFile(TEMP_FILE4);
            FileUtil.deleteFile(TEMP_FILE5);

            //###########################################
            //#  テーブル名から可変部の年月日を取り出す
            //###########################################
            //#  年月日指定
            String TBL_MIN_YMD = "";
            String TBL_MAX_YMD = "";
            String DEL_TAISIYO = "";
            if (StringUtils.equals(CHG_VALUE, "1")) {
                TBL_MIN_YMD = NTRegexUtil.find("([2][0][0-9][0-9][0-1][0-9][0-3][0-9])", MIN_TBL);
                TBL_MAX_YMD = NTRegexUtil.find("([2][0][0-9][0-9][0-1][0-9][0-3][0-9])", MAX_TBL);

                if (StringUtils.equals(HZN_KKN_KBN, "D")) {

                    if (StringUtils.equals(DEL_D_DATE, "00000000")) {
                        APLOG_WT("保存期間の指定なし （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_D_DATE;
                } else {
                    if (StringUtils.equals(DEL_D_DATE, "00000000")) {
                        APLOG_WT("保存期間の指定なし  （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_I_DATE;
                }
            }
            //#  年月指定
            if (StringUtils.equals(CHG_VALUE, "2")) {
                TBL_MIN_YMD = NTRegexUtil.find("([2][0][0-9][0-9][0-1][0-9])", MIN_TBL);
                TBL_MAX_YMD = NTRegexUtil.find("([2][0][0-9][0-9][0-1][0-9])", MAX_TBL);

                if (StringUtils.equals(HZN_KKN_KBN, "M")) {

                    if (StringUtils.equals(DEL_M_DATE, "000000")) {
                        APLOG_WT("保存期間の指定なし  （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_M_DATE;
                } else {
                    if (StringUtils.equals(DEL_I_DATE, "00000000")) {
                        APLOG_WT("保存期間の指定なし  （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_I_DATE.substring(0, 6);
                }
            }
            //     #  年指定
            if (StringUtils.equals(CHG_VALUE, "3")) {
                TBL_MIN_YMD = NTRegexUtil.find("([2][0][0-9][0-9])", MIN_TBL);
                TBL_MAX_YMD = NTRegexUtil.find("([2][0][0-9][0-9])", MAX_TBL);

                if (StringUtils.equals(HZN_KKN_KBN, "Y")) {

                    if (StringUtils.equals(DEL_Y_DATE, "0000")) {
                        APLOG_WT("保存期間の指定なし  （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_Y_DATE;
                } else {
                    if (StringUtils.equals(DEL_I_DATE, "00000000")) {
                        APLOG_WT("保存期間の指定なし  （[" + TBL_NAME + "][" + TBL_ID + "][" + HZN_KKN_KBN + "]）", FW);
                        return Rtn_OK;
                    }
                    DEL_TAISIYO = DEL_I_DATE.substring(0, 4);
                }
            }

            // ###########################################
            // #  保存期間経過している場合
            // ###########################################
            if ((!StringUtils.equals(HZN_KKN_KBN, "I") && StringUtils.compare(TBL_MIN_YMD, DEL_TAISIYO) <= 0)
                    || (StringUtils.equals(HZN_KKN_KBN, "I") && StringUtils.compare(TBL_MIN_YMD, DEL_TAISIYO) <= 0)) {

                //###########################################
                //#  テーブルトランケート
                //###########################################
                int RTN = cmABdbtrS.main(getExecuteBaseParam().sT(MIN_TBL).sD(DB_CONN));
                if (RTN != Rtn_OK) {
                    APLOG_WT("テーブルTruncateエラー  Rtn=" + RTN + "  （ＴＢＬ：" + MIN_TBL + "  DB：" + DB_CONN + "）", FE);
                    return Rtn_NG;
                }
            } else {
                APLOG_WT("Truncate対象テーブルなし （ＴＢＬ：" + TBL_NAME + "）", FI);
                return Rtn_OK;
            }

            // #################################################
            // #  リネーム後対象テーブル名存在チェックSQL文編集
            // #################################################
            shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID).defaultEvn(this)
                    .addEvn("BKUP_FLG", String.valueOf(BKUP_FLG))
                    .addEvn("NEW_BKUP_TBL", NEW_BKUP_TBL)
                    .addEvn("BASE_TBL_NAME", BASE_TBL_NAME)
                    .addEvn("CHG_NAME", CHG_NAME)
                    .addEvn("TEMP_FILE3", TEMP_FILE3)
                    .addEvn("DB_CONN_VALUE", DB_CONN_VALUE)
                    .addEvn("TEMP_FILE4", TEMP_FILE4)
                    .execute();
            if (shellExecuteDto.RTN0()) {
                RTN = Integer.parseInt(shellExecuteDto.result);
                String SQL_CD = FileUtil.SQL_CD_ORA_FILE(TEMP_FILE4);
                APLOG_WT("SQLエラー（USER_TABLES より、" + BASE_TBL_NAME + CHG_NAME + "のselect）[" + RTN + "]  [SQLCODE：" + SQL_CD +
                        "]", FE);
                return Rtn_NG;
            }

            TEMP_FILE5_DATA = FileUtil.awk(TEMP_FILE4, item -> "tbl_chk".equals(item.replaceAll("\\s+", " ").split(" ")[0]));
            FileUtil.writeFile(TEMP_FILE5, TEMP_FILE5_DATA);
            int CHK_LCNT = FileUtil.countLines(TEMP_FILE5);
            //##############################################
            //#  翌日、翌月、翌年のテーブルが存在しない場合
            //##############################################
            int RNT = 0;
            if (CHK_LCNT == 0) {
                //###########################################
                //#  テーブルリネーム
                //###########################################
                if (BKUP_FLG == 1) {
                    RNT = cmABdbrnS.main(getExecuteBaseParam().sB(MIN_TBL).sA(NEW_BKUP_TBL).sD(DB_CONN).sO(TBL_MIN_YMD).sN(CHG_NAME));
                } else {
                    RNT = cmABdbrnS.main(getExecuteBaseParam().sB(MIN_TBL).sA(BASE_TBL_NAME + CHG_NAME).sD(DB_CONN).sO(TBL_MIN_YMD).sN(CHG_NAME));
                }
                if (RNT != Rtn_OK) {
                    APLOG_WT("テーブルRenameエラー  Rtn=" + RNT + "   （ＴＢＬ：" + MIN_TBL + "  DB：" + DB_CONN + "）", FE);
                    return Rtn_NG;
                }
            } else {
                APLOG_WT("Rename対象テーブルなし （ＴＢＬ：" + TBL_NAME+"）", FI);
                return Rtn_OK;
            }
            return Rtn_OK;
        });
        if (readFileDto.getStatus() != Rtn_OK) {
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
