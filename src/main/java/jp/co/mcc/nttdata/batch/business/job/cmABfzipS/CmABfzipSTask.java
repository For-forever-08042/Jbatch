package jp.co.mcc.nttdata.batch.business.job.cmABfzipS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABpaswB.CmABpaswBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　圧縮処理
 * #    プログラムID　：　cmABfzipS
 * #
 * #    【処理概要】
 * #        圧縮対象として指定したディレクトリ全体または、指定された対象ファイルを、
 * #        圧縮後ファイル格納ディレクトリに、zipにて圧縮する。
 * #
 * #    【引数説明】
 * #	-O  圧縮後ファイル格納ディレクトリ	:　圧縮後のファイルを格納する
 * #						   ディレクトリを指定する
 * #	-D  対象ファイル格納ディレクトリ	:　対象ファイルが格納されている
 * #						   ディレクトリを指定する
 * #	-I  対象ファイル名			:　圧縮対象のファイル名
 * #	-Z  圧縮後ファイル名			:　圧縮後のファイル名
 * #	-DEL					:　圧縮後に圧縮対象のファイル削除
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :	2012/10/18 SSI.吉岡 ：初版
 * #      2.00 :	2013/12/25 SSI.上野 ：パスワード付き圧縮処理を追加
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmABfzipSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;


    @Autowired
    CmABpaswBServiceImpl cmABpaswBService;
    @Autowired
    CmABgprmBServiceImpl cmABgprmBService;

    int IS_PASSWD = 0;
    String PSW_DATA = "";
    String CD_DIR = "";
    String INPUT_NAME = "";

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "圧縮処理";

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
        final String ARG_OPT1 = "-O";//###  圧縮後ファイル格納ディレクトリ
        final String ARG_OPT2 = "-D";//		###  対象ファイル格納ディレクトリ
        final String ARG_OPT3 = "-I";//		###  対象ファイル名
        final String ARG_OPT4 = "-Z";//		###  圧縮後ファイル名
        final String ARG_OPT5 = "-DEL";//		###  圧縮後、対象ファイル削除指定

        //#  パラメータファイル
        String PARAM_FILE = "cmABfzipP";
        //
        //#  モジュール用ディレクトリ
        String PRG_DIR = "/backup/dc_backup/cm_exec";


        //#  その他
        String BKUP_IFS = IFS;
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();

        //###########################################
        //#  引数チェック
        //###########################################
        //#  引数格納変数初期化
        String OPTION1 = "0";
        String OPTION2 = "0";
        String OPTION3 = "";
        String OPTION4 = "";
        String OPTION5 = "0";

        //#  引数格納
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
                case ARG_OPT2:
                    if (!StringUtils.equals(OPTION2, "0")) {
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
                    if (!StringUtils.isEmpty(OPTION3)) {
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
                    if (!StringUtils.isEmpty(OPTION4)) {
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
                    if (!StringUtils.equals(OPTION5, "0")) {
                        APLOG_WT("引数重複指定エラー  [" + ARG_ALL + "]", FW);
                        return Rtn_NG;
                    }
                    OPTION5 = "1";
                    break;
                default:
                    APLOG_WT("引数エラー　定義外の引数[" + args[i] + "]", FW);
                    return Rtn_NG;

            }
        }

        //必須引数チェック
        if (StringUtils.equals(OPTION1, "0") || StringUtils.equals(OPTION2, "0")) {
            APLOG_WT("引数エラー  必須引数不足[" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        //# 必須引数をチェック
        String ZIP_NAME = "";
        if (StringUtils.equals(OPTION4, "") && !StringUtils.equals(OPTION3, "")) {
            ZIP_NAME = OPTION3 + ".zip";
        }
        if (StringUtils.equals(OPTION4, "") && StringUtils.equals(OPTION3, "")) {
            String IN_DIR = basename(OPTION2);
            ZIP_NAME = IN_DIR + ".zip";
        }
        if (!StringUtils.equals(OPTION4, "")) {
            ZIP_NAME = OPTION4;
        }
        if (StringUtils.equals(OPTION3, "")) {
            CD_DIR = dirname(OPTION2);
            INPUT_NAME = basename(OPTION2);
        } else {
            CD_DIR = OPTION2;
            INPUT_NAME = OPTION3;
        }


        if (!FileUtil.isExistDir(OPTION2)) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]\n指定された対象ファイル格納ディレクトリが存在しません。", FW);
            return Rtn_NG;
        }

        //#  対象ファイル存在チェック

        String IN_TYPE = "D";
        String IN_NAME = OPTION2;

        if (!StringUtils.equals(OPTION3, "")) {
            IN_TYPE = "0";
            if (FileUtil.isExistFile(OPTION2 + lp + OPTION3)) {
                IN_TYPE = "F";
            }
            if (FileUtil.isExistDir(OPTION2 + lp + OPTION3)) {

                IN_TYPE = "D";
            }
            IN_NAME = OPTION2 + lp + OPTION3;

            if (StringUtils.equals(IN_TYPE, "0")) {
                APLOG_WT("引数エラー  [" + ARG_ALL + "]\n指定された対象ファイルが存在しません。", FW);
                return Rtn_NG;
            }
        }

        //#  圧縮後ディレクトリ存在チェック
        if (!FileUtil.isExistDir(OPTION1)) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]\n指定された圧縮後ファイル格納ディレクトリが存在しません。", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  パラメータファイル名取得
        //###########################################
        MainResultDto PARAM_NAME_DATA = cmABgprmBService.main(getExecuteBaseParam().add(PARAM_FILE));
        String PARAM_NAME = PARAM_NAME_DATA.result;
        if (PARAM_NAME_DATA.exitCode != Rtn_OK) {
            APLOG_WT("パラメータファイル名取得エラー", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  パラメータファイル読み込み処理
        //###########################################
        String IN_FILE = PARAM_NAME;
        ReadFileDto.getInstance().readFile(IN_FILE).loop2((READ_FLD1, READ_FLD2) -> {
            String FILENAME = READ_FLD1;
            String WK_COMMENT = READ_FLD1.substring(0, 1);
            // #  コメント行、ヘッダ行読み飛ばし
            if ("#".equals(WK_COMMENT) || "ファイル名".equals(FILENAME)) {
                return Rtn_OK;
            }
            ShellExecuteDto result = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                    .addEvn("FILENAME", FILENAME)
                    .addEvn("INPUT_NAME", INPUT_NAME)
                    .execute();
            if ("10".equals(result.result)) {
                APLOG_WT("パスワード付き圧縮対象　FILE=" + INPUT_NAME, FI);
                IS_PASSWD = 1;
                //        ###########################################
                //        #  １回目圧縮ファイルのタイムスタンプ取得
                //        ###########################################
                String FILE_TIME = FileUtil.lastModifyTime(CD_DIR + "/" + INPUT_NAME, "YYYYMMddHHmmss");
                //        ###########################################
                //        #  パスワード発行処理
                //        ###########################################

                MainResultDto PSW = cmABpaswBService.main(getExecuteBaseParam().add("-d" + FILE_TIME));
                PSW_DATA = PSW.result;
                if (PSW.exitCode != Rtn_OK) {
                    APLOG_WT("パスワード発行処理異常終了　FILE=" + INPUT_NAME, FW);
                    PSW_DATA = INPUT_NAME + "_" + FILE_TIME;
                }
            }
            return Rtn_OK;
        });


//
        //###########################################
        //#  zip実行
        //###########################################
//        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID).addEvn("CD_DIR", CD_DIR)
//                .addEvn("IN_TYPE", IN_TYPE)
//                .addEvn("IS_PASSWD", String.valueOf(IS_PASSWD))
//                .addEvn("PSW_DATA", PSW_DATA)
//                .addEvn("OPTION1", OPTION1)
//                .addEvn("ZIP_NAME", ZIP_NAME)
//                .addEvn("INPUT_NAME", INPUT_NAME)
//                .execute();

        int RNT = 0;
        StringDto resultOutput = new StringDto();
        if ("D".equals(IN_TYPE)) {
            if (IS_PASSWD == 1) {
                RNT = ZipUtil.zipFile(OPTION1 + "/" + ZIP_NAME, OPTION2 + "/" + INPUT_NAME, PSW_DATA, resultOutput);
            } else {
                RNT = ZipUtil.zipFile(OPTION1 + "/" + ZIP_NAME, OPTION2 + "/" + INPUT_NAME, resultOutput);
            }

        } else {
            if (IS_PASSWD == 1) {
                RNT = ZipUtil.zipFile(OPTION1 + "/" + ZIP_NAME, OPTION2 + "/" + INPUT_NAME, PSW_DATA, resultOutput);
            } else {
                RNT = ZipUtil.zipFile(OPTION1 + "/" + ZIP_NAME, OPTION2 + "/" + INPUT_NAME, resultOutput);
            }
        }

        if (RNT != 0) {
            APLOG_WT("圧縮失敗  [" + IN_NAME + "][" + resultOutput.arr + "]", FW);
            return Rtn_NG;
        }

        APLOG_WT("zip実行（" + IN_NAME + "の圧縮）　正常", FI);
//
        //###########################################
        //#  元ファイル削除指定がされている場合、対象ファイルを削除
        //###########################################
        int RTN = 0;
        if (StringUtils.equals(OPTION5, "1")) {
            if (StringUtils.equals(IN_TYPE, "D")) {
                RTN = FileUtil.deleteDir(new File(IN_NAME));
            } else {
                RTN = FileUtil.deleteFile(IN_NAME);
            }
            if (RTN == 0) {
                APLOG_WT("圧縮後に元ファイル削除  [" + IN_NAME + "]", FI);
            } else {
                APLOG_WT("圧縮後に元ファイル削除失敗  [" + IN_NAME + "][" + 1 + "]", FI);
                return Rtn_NG;
            }
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
        return Rtn_OK;
    }

}
