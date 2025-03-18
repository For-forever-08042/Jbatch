package jp.co.mcc.nttdata.batch.business.job.cmABbkclS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.NTRegexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  バックアップコントローラ
 * #    プログラムID  ：  cmABbkclS
 * #
 * #    【処理概要】
 * #        下記ＤＢサーバ向けのバックアップ開始ファイルを作成し
 * #        各ＤＢサーバからバックアップ完了ファイルが作成されるまで監視する。
 * #        対象ＤＢサーバ
 * #        顧客管理ＤＢ＃１
 * #        顧客制度ＤＢ＃１
 * #
 * #    【引数説明】
 * #      -h対象ホスト名                   ：バックアップ対象サーバのホスト名（必須）
 * #      -dyyyymmdd                       ：処理日付（任意）
 * #      -wmmm                            ：最大処理時間（分）（任意）
 * #
 * #    【戻り値】
 * #        10     ：  正常
 * #        49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :2012/12/20 SSI.Suyama  ： 初版
 * #      2.00 :2014/07/22 SSI.上野    ： 最大処理時間変更 2H → 3H
 * #     40.00 :2022/09/30 SSI.川内    ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmABbkclSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;
    @Autowired
    CmABgprmBServiceImpl cmABgprmBService;


    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    private final static String ARG_OPT1 = "-h";//  ### ホスト名
    private final static String ARG_OPT2 = "-d";// ### 処理日付
    private final static String ARG_OPT3 = "-w";// ### 最大処理時間

    //  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;
    String OPTION3 = null;

    String DB_SERVER_NAME = null;
    String PROC_TODAY = null;
    int CHK_REL = 0;

    @Override
    public int taskExecuteCustom(String[] args) {
        // save service name in first index for the APログ

        // ###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "バックアップコントローラ";
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME + "(" + Arrays.stream(args).collect(Collectors.joining(" ")) + ")"));

        //#  パラメータファイル
        String PARAM_FILE = "cmABbkclP";
        //#  作業ディレクトリ
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.mkdir(WORK_DIR);

        //#  送信ディレクトリ
        String SEND_DIR = "/backup/dc_backup/cm_work";

        //#  退避ディレクトリ
        String TAIHI_DIR = CM_APRESULT;

        //#  バックアップ開始ファイル名
        String BKUP_START_FNAME = "BKUP_START";

        //#  バックアップ開始OKファイル
        String START_OK_FNAME = "BKMA_START_OK";

        //#  バックアップ結果ファイル名
        String OUTPUT_FNAME = "DATA_RESULT";

        //#  バックアップ完了ファイル名
        String OUTPUT_END_FNAME = "DATA_END";

        //#  パラメータファイルチェック用ファイル名
        String TEMP_FILE1 = "BKUP_TEMP1";
        String TEMP_FILE2 = "BKUP_TEMP2";
        //#  バックアップ完了NGファイル名
        String OUTPUT_NG_FNAME = "DATA_NG";

        // #  待ち時間(秒)
        int WAIT_TIME = 60;
        //#  最大処理時間(分)
        int MAX_TIME = 180;

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        //###########################################
        //#  引数セット
        //###########################################
        //#引数を退避しておく
        //TODO
        //ARG_ORG=${@}

        //#  引数格納
        for (int i = 0; i < args.length; i++) {
            String ARG_OPT = args[i];
            if (ARG_OPT.startsWith(ARG_OPT1)) {
                DB_SERVER_NAME = ARG_OPT.substring(2);
                continue;
            }
            // # -d処理日付が指定された場合
            if (ARG_OPT.startsWith(ARG_OPT2)) {
                PROC_TODAY = ARG_OPT.substring(2);
                continue;
            }
            // # -w最大処理時間が指定された場合
            if (ARG_OPT.startsWith(ARG_OPT3)) {
                MAX_TIME = Integer.valueOf(ARG_OPT.substring(2));
                //            if  test "${MAX_TIME}" -lt 0 -a "${MAX_TIME}" -gt 200//
                if (MAX_TIME > 200 || MAX_TIME < 0) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [最大処理時間が不正]").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    return Rtn_NG;
                }
                continue;
            }

        }

        if (StringUtils.isEmpty(DB_SERVER_NAME)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [対象ホスト名が指定されていません]").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        String PROC_YEDAY = null;
        //###########################################
        //#  バッチ処理日付の取得
        //###########################################
        if (StringUtils.isEmpty(PROC_TODAY)) {
            MainResultDto resultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
            PROC_YEDAY = resultDto.result;
            if (resultDto.exitCode != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("前日バッチ処理日付の取得に失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                return Rtn_NG;
            }
        }

        //#  -新- バッチ処理日付（当日）取得
        //#　-新-（バッチ処理日付変更直後に起動されるため、当日を当年の年別テーブル名用の元日付とする）
        MainResultDto resultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        PROC_TODAY = resultDto.result;
        if (resultDto.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("当日バッチ処理日付の取得に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }
        int BAT_YYYY_T = Integer.valueOf(PROC_TODAY.substring(0, 4));
        int BAT_YYYY_B = BAT_YYYY_T - 1;

        //###########################################
        //#  バックアップ開始ファイル作成パラメータファイル名取得
        //###########################################
        resultDto = cmABgprmBService.main(getExecuteBaseParam().add(PARAM_FILE));
        String PARAM_NAME = resultDto.result;
        if (resultDto.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バックアップ開始ファイル作成パラメータファイル名取得エラー").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        //###########################################
        //#  パラメータファイル読み込み処理
        //###########################################

        String tmp = Arrays.stream(FileUtil.readFile(PARAM_NAME.split("\n")[0]).split("\n")).filter(item -> item.split(" ")[0].equals(DB_SERVER_NAME)).collect(Collectors.joining("\n"));
        FileUtil.writeFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE1, tmp);

        //###########################################
        //#  パラメータファイルから抽出したレコードのフィールド数をチェック
        //###########################################
        String IN_FILE = WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE1;
        int IN_FILD = 6;
        String IN_FILE_DATA = FileUtil.readFile(IN_FILE);
        tmp = Arrays.stream(IN_FILE_DATA.split("\n")).filter(item -> item.split("( )+").length != IN_FILD).collect(Collectors.joining("\n"));
        int lines = 0;
        if (StringUtils.isNotEmpty(tmp)) {
            FileUtil.writeFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE2, tmp);
            lines = tmp.split("\n").length;
        }
        if (lines != 0) {
            FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE2);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バックアップ開始ファイル作パラメータファイル形式エラー").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE2);

        FileUtil.createFolder(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME, true);

        //###########################################
        //#  パラメータファイルから抽出したデータ読み込む
        //###########################################
        String[] IN_FILE_DATA_A = IN_FILE_DATA.split("\n");
        if (IN_FILE_DATA_A.length == IN_FILD) {

            for (String item : IN_FILE_DATA_A) {

                String[] itd = item.split("\\s+");
                String READ_FLD1 = itd[0];
                String READ_FLD2 = itd[1];
                String READ_FLD3 = itd[2];
                String READ_FLD4 = itd[3];
                String READ_FLD5 = itd[4];
                String READ_FLD6 = itd[5];
                if ("#".equals(READ_FLD1)) {
                    continue;
                }

                String HOST_NAME = READ_FLD1;
                String TBL_NAME = READ_FLD2;
                String TBL_FILE_ORG = READ_FLD3;
                String USR_NAME = READ_FLD4;
                String PSW_NAME = READ_FLD5;
                String SID_NAME = READ_FLD6;
                String CHG_NAME = "";

                //  # ファイル名とファイル番号取得
                int TBL_FILE_LEN = TBL_FILE_ORG.length();
                int TBL_FILE_LEN_F = TBL_FILE_LEN - 3;
                int TBL_FILE_LEN_D = TBL_FILE_LEN - 1;
                String TBL_FILE_NO = TBL_FILE_ORG.substring(TBL_FILE_LEN_D);
                String TBL_FILE = TBL_FILE_ORG.substring(0, TBL_FILE_LEN_F);
                //      # 末尾にYYYYがあるかチェック
                String TBL_TAIL_YB = NTRegexUtil.find(".*(YYYYB)", TBL_NAME);
                String TBL_TAIL_YT = NTRegexUtil.find(".*(YYYYT)", TBL_NAME);
                // ###########################################
                // #  リネーム後可変部の編集
                // ###########################################
                String BASE_TBL_NAME = null;
                String BASE_TBL_FILE = null;
                if ("YYYYB".equals(TBL_TAIL_YB)) {

                    CHG_NAME = String.valueOf(BAT_YYYY_T);
                    BASE_TBL_NAME = TBL_NAME.replaceAll("YYYYB", "");
                    BASE_TBL_NAME = BASE_TBL_NAME + BAT_YYYY_B;
                    BASE_TBL_FILE = TBL_FILE.replaceAll("YYYYB", "");
                    BASE_TBL_FILE = BASE_TBL_FILE + BAT_YYYY_B;
                } else if ("YYYYT".equals(TBL_TAIL_YT)) {

                    CHG_NAME = String.valueOf(BAT_YYYY_T);
                    BASE_TBL_NAME = TBL_NAME.replaceAll("YYYYT", "");
                    BASE_TBL_NAME = BASE_TBL_NAME + BAT_YYYY_T;
                    BASE_TBL_FILE = TBL_FILE.replaceAll("YYYYT", "");
                    BASE_TBL_FILE = BASE_TBL_FILE + BAT_YYYY_T;
                }
                //###########################################
                //#  バックアップ開始ファイル作成
                //###########################################
                if (StringUtils.isEmpty(CHG_NAME)) {
                    String td = HOST_NAME + " " + TBL_NAME + " " + TBL_FILE + " " + TBL_FILE_NO + " " + USR_NAME + " " + PSW_NAME + " " + SID_NAME + " " + PROC_YEDAY;
                    FileUtil.writeFile(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME, td);
                } else {
                    String td = HOST_NAME + " " + BASE_TBL_NAME + " " + BASE_TBL_FILE + " " + TBL_FILE_NO + " " + USR_NAME + " " + PSW_NAME + " " + SID_NAME + " " + PROC_YEDAY;
                    FileUtil.writeFile(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME, td);
                }

            }
        }

        //###########################################
        //#  バックアップ開始ファイル送信
        //#  バックアップ開始OKファイル作成
        //###########################################
        FileUtil.copyFile(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME, SEND_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME);
        FileUtil.createFolder(SEND_DIR + "/" + DB_SERVER_NAME + START_OK_FNAME, true);

        String IN_FILE2 = WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME;
        int CHK_CNT = 0;
        //###########################################
        //#  バックアップ完了ファイル存在チェック
        //###########################################
        String IN_FILE2_DATA = null;
        while (true) {
            IN_FILE2_DATA = FileUtil.readFile(IN_FILE2);
            if (StringUtils.isNotEmpty(IN_FILE2_DATA)) {
                Arrays.stream(IN_FILE2_DATA.split("\n")).forEach(item -> {
                    String CHKTBL = item.split(" ")[2];
                    if (FileUtil.isExistFile(SEND_DIR + "/" + CHKTBL + OUTPUT_END_FNAME)
                            || FileUtil.isExistFile(SEND_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME)) {
                        CHK_REL = 0;
                    } else {
                        CHK_REL = 1;
                    }
                });
            }
            //#  バックアップ対象のファイルがすべてあれば終了
            if (CHK_REL == 0) {
                break;
            }
            CHK_CNT = CHK_CNT + 1;
            //#  最大処理時間を越えた場合、異常終了
            if (CHK_CNT > MAX_TIME) {
                IN_FILE2_DATA = FileUtil.readFile(IN_FILE2);
                //###########################################
                //#  バックアップ開始ファイルから抽出したデータ読み込む
                //###########################################
                Arrays.stream(IN_FILE2_DATA.split("\n")).forEach(item -> {
                    String CHKTBL = item.split(" ")[2];
                    // #  バックアップ処理で異常終了した処理が無いかチェック
                    if (!FileUtil.isExistFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME)) {
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイルなし[" + CHKTBL + "]").FW());
                    } else {
                        String[] td2 = FileUtil.readFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME).split("\n");
                        for (String READ_LINE : td2) {
                            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(READ_LINE));
                        }
                    }
                    // #  バックアップ結果ファイル完了ファイルを退避
                    FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_FNAME);
                    FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_END_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_END_FNAME);
                    FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME);
                });


                //###########################################
                //#  バックアップ開始ファイルを削除
                //#  バックアップ開始ＯＫファイルを削除
                //###########################################
                FileUtil.deleteFile(SEND_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME);
                FileUtil.deleteFile(SEND_DIR + "/" + DB_SERVER_NAME + START_OK_FNAME);
                FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE1);
                FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME);

                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("最大処理時間を越えても終了していません").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                return Rtn_NG;
            }
            sleep(WAIT_TIME);
        }


        //###########################################
        //#  バックアップ開始ファイルから抽出したデータ読み込む
        //###########################################
        IN_FILE2_DATA = FileUtil.readFile(IN_FILE2);
        if (StringUtils.isNotEmpty(IN_FILE2_DATA)) {
            Arrays.stream(IN_FILE2_DATA.split("\n")).forEach(item -> {
                String CHKTBL = item.split(" ")[2];
                boolean status = true;
                if (!FileUtil.isExistFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME)) {
                    status = false;
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイルなし[" + CHKTBL + "]").FW());
                }
                // #  NGファイルが無いかチェック
                else if (FileUtil.isExistFile(SEND_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME)) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果NG[" + CHKTBL + "]").FW());
                }
                if (status) {
                    String[] td2 = FileUtil.readFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME).split("\n");
                    for (String READ_LINE : td2) {
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(READ_LINE));
                    }
                }
                //#  バックアップ結果ファイル完了ファイルを退避
                FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_FNAME);
                FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_END_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_END_FNAME);
                FileUtil.mvFile(SEND_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME, TAIHI_DIR + "/" + CHKTBL + OUTPUT_NG_FNAME);
            });
        }

        //###########################################
        //#  バックアップ開始ファイルを削除
        //###########################################
        FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + TEMP_FILE1);
        FileUtil.deleteFile(WORK_DIR + "/" + DB_SERVER_NAME + BKUP_START_FNAME);


        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME + "(" + ARG_ALL + ")"));
        return Rtn_OK;
    }


}
