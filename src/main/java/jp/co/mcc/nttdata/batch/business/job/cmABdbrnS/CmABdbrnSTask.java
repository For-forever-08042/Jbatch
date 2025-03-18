package jp.co.mcc.nttdata.batch.business.job.cmABdbrnS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　テーブルRename
 * #    プログラムID　：　cmABdbrnS
 * #
 * #    【処理概要】
 * #        テーブルRenameを行う。
 * #
 * #    【引数説明】
 * #	-B  変更前テーブル名	:　テーブル名の変更対象のテーブル名
 * #	-A  変更後テーブル名	:　変更後のテーブル名
 * #	-D  接続DB		:　接続対象のDB
 * #	-O  変更前キーワード	:　変更対象のテーブル名可変部文字列
 * #	-N  変更後キーワード	:　変更後のテーブル名可変部文字列
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :	2012/10/18 SSI.吉岡  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmABdbrnSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;


    //#  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;
    String OPTION3 = null;
    String OPTION4 = null;
    String OPTION5 = null;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "テーブルReName";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //###########################################
        //#  引数定義
        //###########################################
        final String ARG_OPT1 = "-B";//       ###  変更前テーブル名
        final String ARG_OPT2 = "-A";//		###  変更後テーブル名
        final String ARG_OPT3 = "-D";//		###  接続DB
        final String ARG_OPT4 = "-O";//		###  変更前インデックスキーワード
        final String ARG_OPT5 = "-N";//		###  変更後インデックスキーワード

        String KAIINCHOUFUKU_FILE_NAME = "kaiinchoufuku_error";

        //###########################################
        //#  引数チェック
        //###########################################
        if (args.length < 6) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        //#  引数格納

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case ARG_OPT1:
                    if (OPTION1 != null) {
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
                case ARG_OPT2:
                    if (OPTION2 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 2) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION2 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT3:
                    if (OPTION3 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 2) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
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
                    if (args.length < i + 2) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION4 = args[i + 1];
                    i++;
                    break;
                case ARG_OPT5:
                    if (OPTION5 != null) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    if (args.length < i + 2) {
                        APLOG_WT("引数エラー    [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION5 = args[i + 1];
                    i++;
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);
                    return Rtn_NG;

            }
        }


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

        //# 必須引数をチェック
        if (StringUtils.isEmpty(OPTION1) || StringUtils.isEmpty(OPTION2) || StringUtils.isEmpty(OPTION3)) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }
        if (StringUtils.isEmpty(OPTION4) && !StringUtils.isEmpty(OPTION5)) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }
        if (!StringUtils.isEmpty(OPTION4) && StringUtils.isEmpty(OPTION5)) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        //#  同一名チェック
        if (OPTION1.equals(OPTION2)) {
            APLOG_WT("引数エラー　リネーム前後のテーブル名が同一[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }
        if (OPTION4.equals(OPTION5)) {
            APLOG_WT("引数エラー　リネーム前後の可変部キーワードが同一[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        if (!checkConnectionType(OPTION3)) {
            return Rtn_NG;
        }

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
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
        String TEMP_FILE5 = WORK_DIR + "/" + CM_MYPRGID + "05_" + pid;
        String TEMP_FILE6 = WORK_DIR + "/" + CM_MYPRGID + "06_" + pid;
        String TEMP_FILE7 = WORK_DIR + "/" + CM_MYPRGID + "07_" + pid;
        String TEMP_FILE8 = WORK_DIR + "/" + CM_MYPRGID + "08_" + pid;

        String ORG_USR = "MBLUSER";
        //###########################################
        //#  SYNONYM名取得SQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("ORG_USR", ORG_USR)
                .addEvn("OPTION1", OPTION1)
                .addEvn("TEMP_FILE1", TEMP_FILE1)
                .addEvn("TEMP_FILE7", TEMP_FILE7)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(TEMP_FILE7);
            APLOG_WT("SQLエラー（synonymのselect）　[SQLCODE：" + SQL_CD + "]", FE);
            return Rtn_NG;
        }
        FileUtil.deleteFile(TEMP_FILE1);

        //###########################################
        //#  SYNONYM削除
        //###########################################

        String finalOPTION = OPTION1;
        ReadFileDto readFileDto = ReadFileDto.getInstance().readFile(TEMP_FILE7)
                .filter(item -> item.contains(finalOPTION))
                .loop2((String READ_FLD1, String READ_FLD2) -> {
                    String SYN_NAME = READ_FLD1;
                    String TBL_NAME = READ_FLD2;
                    if (!StringUtils.equals(SYN_NAME, TBL_NAME)) {
                        APLOG_WT("SYNONYM削除なし　シノニム：[" + SYN_NAME + "]変更前テーブル：[" + TBL_NAME + "]", FE);
                        return Rtn_OK;
                    }

                    //        ###########################################
                    //        #  SYNONYM削除SQL実行
                    //        ###########################################
                    ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
                            .defaultEvn(this)
                            .addEvn("ORG_USR", ORG_USR)
                            .addEvn("SYN_NAME", SYN_NAME)
                            .addEvn("TBL_NAME", TBL_NAME)
                            .addEvn("TEMP_FILE8", TEMP_FILE8)
                            .execute();
                    if (shellExecuteDto1.RTN0()) {
                        String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto1.result);
                        APLOG_WT("SQLエラー（" + ORG_USR + "." + SYN_NAME + "のsynonym削除）　[SQLCODE：" + SQL_CD + "]", FE);
                        return Rtn_NG;
                    }

                    APLOG_WT("SQL実行（" + ORG_USR + "." + SYN_NAME + "のsynonym削除）　正常", FI);
                    FileUtil.deleteFile(TEMP_FILE8 + ".sql");
                    return Rtn_OK;
                });

        if (readFileDto.getStatus() == Rtn_NG) {
            return Rtn_NG;
        }

        //###########################################
        //#  テーブルリネームSQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("OPTION1", OPTION1)
                .addEvn("OPTION2", OPTION2)
                .addEvn("TEMP_FILE2", TEMP_FILE2)
                .execute();
        if (shellExecuteDto1.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto1.result);
            APLOG_WT("SQLエラー（" + OPTION1 + "のReName）　[SQLCODE：" + SQL_CD + "]", FE);
            return Rtn_NG;
        }

        APLOG_WT("SQL実行（" + OPTION1 + "->" + OPTION2 + "のReName）　正常", FI);
        FileUtil.deleteFile(TEMP_FILE2 + ".sql");


        //###########################################
        //#  インデックス名取得SQL文編集
        //###########################################
        //###########################################
        //#  テーブルリネームSQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto4 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("OPTION2", OPTION2)
                .addEvn("TEMP_FILE3", TEMP_FILE3)
                .addEvn("TEMP_FILE4", TEMP_FILE4)
                .execute();
        if (shellExecuteDto4.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto4.result);
            APLOG_WT("SQLエラー（user_ind_columns より、" + OPTION2 + "のselect）[" + shellExecuteDto4.result + "]　[SQLCODE：" + SQL_CD + "]", FE);
            return Rtn_NG;
        }
        String TEMP_FILE5_DATA = FileUtil.awk(TEMP_FILE4, item -> "indx_select".equals(item.replaceAll("\\s+", " ").split(" ")[0]));
        FileUtil.writeFile(TEMP_FILE5, TEMP_FILE5_DATA);
        int IN_LCNT = FileUtil.countLines(TEMP_FILE5);


        //#  取得した行がゼロ件の場合はインデックスはリネームしない
        if (IN_LCNT == 0) {
            APLOG_WT("対象インデックスなし　（ＴＢＬ：" + OPTION2 + "）", FI);
        }

        FileUtil.deleteFile(TEMP_FILE3);
        FileUtil.deleteFile(TEMP_FILE4);
        ReadFileDto.getInstance().readFile(TEMP_FILE5).loop2((READ_FLD1, READ_FLD2) -> {

            String IDX_NAME = READ_FLD2;
            String IDX_NAME_NEW = IDX_NAME.replaceAll(OPTION1, OPTION2);
            IDX_NAME_NEW = IDX_NAME_NEW.replaceAll(OPTION4, OPTION5);
            if (IDX_NAME.equals(IDX_NAME_NEW)) {
                APLOG_WT("インデックスリネームなし[" + IDX_NAME + "]", FI);
                return Rtn_OK;
            }

            //        ###########################################
            //        #  SQL実行
            //        ###########################################
            ShellExecuteDto shellExecuteDto6 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_06")
                    .defaultEvn(this)
                    .addEvn("IDX_NAME", IDX_NAME)
                    .addEvn("IDX_NAME_NEW", IDX_NAME_NEW)
                    .addEvn("TEMP_FILE6", TEMP_FILE6)
                    .execute();

            if (shellExecuteDto6.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto6.result);
                APLOG_WT("SQLエラー（" + IDX_NAME + "->" + IDX_NAME_NEW + "のReName）　[SQLCODE：" + SQL_CD + "]", FE);
                return Rtn_NG;
            }
            APLOG_WT("SQL実行（" + IDX_NAME + "->" + IDX_NAME_NEW + "のIndexReName）　正常", FI);
            FileUtil.deleteFile(TEMP_FILE6);
            return Rtn_OK;
        });

        if (readFileDto.getStatus() == Rtn_NG) {
            return Rtn_NG;
        }
        FileUtil.deleteFile(TEMP_FILE5);


        //###########################################
        //#  SYNONYM作成
        //###########################################
        ReadFileDto.getInstance().readFile(TEMP_FILE7).filter(item -> item.contains(OPTION1)).loop2((READ_FLD1, READ_FLD2) -> {

            String SYN_NAME = READ_FLD1;
            String TBL_NAME = READ_FLD2;
            if (!SYN_NAME.equals(TBL_NAME)) {
                APLOG_WT("SYNONYM再作成なし　シノニム：[" + SYN_NAME + "]変更前テーブル：[" + TBL_NAME + "]", FI);
                return Rtn_OK;
            }


            //       ###########################################
            //        #  SYNONYM作成SQL実行
            //        ###########################################

            ShellExecuteDto shellExecuteDto7 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_07")
                    .defaultEvn(this)
                    .addEvn("ORG_USR", ORG_USR)
                    .addEvn("OPTION2", OPTION2)
                    .addEvn("CONNECT_USR", CONNECT_USR)
                    .addEvn("TEMP_FILE8", TEMP_FILE8)
                    .execute();


            if (shellExecuteDto7.RTN0()) {
                String SQL_CD = FileUtil.SQL_CD_ORA_STR(shellExecuteDto7.result);
                APLOG_WT("SQLエラー（" + ORG_USR + "." + OPTION2 + "のsynonym作成）　[SQLCODE：" + SQL_CD + "]", FE);
                return Rtn_NG;
            }
            APLOG_WT("SQL実行（" + ORG_USR + "." + OPTION2 + "のsynonym作成）　正常", FI);
            FileUtil.deleteFile(TEMP_FILE8);
            return Rtn_OK;
        });
        FileUtil.deleteFile(TEMP_FILE7);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
        return Rtn_OK;
    }

}
