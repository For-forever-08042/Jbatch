package jp.co.mcc.nttdata.batch.business.job.cmBTefckS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　（汎用）結果ファイル存在チェック
 * #    プログラムID　：　cmBTefckS
 * #
 * #    【処理概要】
 * #        バッチ処理などの結果ファイル、エラーファイルの存在チェックを行う。
 * #
 * #    【引数説明】
 * #        -P パラメータファイル名：チェック対象を定義したパラメータ。
 * #        -T 文字列 　           ：パラメータファイルの第３フィールドと
 * #                                 マッチングし、チェック対象を特定する。
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       49　　 ：　異常
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
 **/
@Component
public class CmBTefckSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;
    @Autowired
    CmABgprmBServiceImpl cmABgprmBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;
    int Rtn_Exit = Rtn_OK;
    //#引数定義
    private final static String ARG_OPT1 = "-P";//  ###パラメータファイル名
    private final static String ARG_OPT2 = "-T";//  ###チェック対象区分
    //  引数格納変数初期化
    String OPTION1 = null;
    String OPTION2 = null;


    //#その他
    String BKUP_IFS = "@";

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "結果ファイル存在チェック";
        setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));


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
                    OPTION2 = args[i + 1];
                    i++;
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

        MainResultDto cmABgprmBResult = cmABgprmBServiceImpl.main(getExecuteBaseParam().add(OPTION1));
        String IN_PARAM_NAME = cmABgprmBResult.result;
        int RTN = cmABgprmBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("引数エラー　[パラメータファイル名不正]", FW);
            return RTN;
        }

        APLOG_WT("起動引数[" + ARG_ALL + "]", FI);

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.mkdir(WORK_DIR);

        //###########################################
        //#  当日、前日のシステム日付取得
        //###########################################
        String SYS_YYYYMMDD_T = DateUtil.getYYYYMMDD();
        String SYS_YYYYMMDD_Y = DateUtil.getYYYYMMDD_Y();

        //###########################################
        //#  パラメータファイル読み込み処理
        //###########################################


        ReadFileDto readFileDto = ReadFileDto.getInstance().readFile(IN_PARAM_NAME)
                .loop6((String READ_FLD1, String READ_FLD2, String READ_FLD3, String READ_FLD4, String READ_FLD5, String READ_FLD6) -> {
                    String IN_FILE_NAME = READ_FLD1;
                    String IN_SERVER_ID = READ_FLD2;
                    String IN_STRING = READ_FLD3;
                    String IN_CHKDIR = READ_FLD4;
                    String IN_APLOG_FLG = READ_FLD5;
                    if (!Objects.equals(IN_SERVER_ID, getenv(C_aplcom1Service.CM_SERVER_ID))) {
                        return Rtn_OK;
                    }
                    if (!Objects.equals(IN_STRING, OPTION2)) {
                        return Rtn_OK;
                    }
                    String WK_COMMENT = StringUtils.substring(IN_FILE_NAME, 1, 1);

                    //コメント行、タイトル行、読み飛ばし
                    if (Objects.equals(WK_COMMENT, "#") || Objects.equals(IN_FILE_NAME, "FILENAME") ||
                            Objects.equals(IN_FILE_NAME, "")) {
                        return Rtn_OK;
                    }

                    //当日のリザルトディレクトリをチェック
                    if (Objects.equals(IN_CHKDIR, "0")) {
                        if (FileUtil.contentLength(getenv(C_aplcom1Service.CM_APRESULT) + "/" + IN_FILE_NAME) != 0) {
                            if (Objects.equals(IN_APLOG_FLG, "E")) {
                                APLOG_WT("エラーファイル存在 [" + IN_FILE_NAME + "]", "-F" + IN_APLOG_FLG);
                            }
                            if (Objects.equals(IN_APLOG_FLG, "W")) {
                                APLOG_WT("警告ファイル存在 [" + IN_FILE_NAME + "]", "-F" + IN_APLOG_FLG);
                            }
                            if (Objects.equals(IN_APLOG_FLG, "I")) {
                                APLOG_WT("チェックファイル存在 [" + IN_FILE_NAME + "]", "-F" + IN_APLOG_FLG);
                            }
                        } else {
                            if (FileUtil.isExistFile(getenv(C_aplcom1Service.CM_APRESULT) + "/" + IN_FILE_NAME)) {
                                APLOG_WT("チェックファイルサイズ＝０", FI);
                            } else {
                                APLOG_WT("チェックファイルなし [" + IN_FILE_NAME + "]", FI);
                            }
                        }
                    } else {
                        //前日バックアップをチェック
                        if (Objects.equals(IN_CHKDIR, "1")) {
                            //前日のバッチ処理日付のバックアップディレクトリをチェック
                            MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
                            String BAT_YYYYMMDD = cmABgdatBResult.result;

                            String pid = PidUtil.getPid();


                            ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                                    .addEvn("WORK_DIR", WORK_DIR)
                                    .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                                    .addEvn("CM_BKUPRESULT", getenv(C_aplcom1Service.CM_BKUPRESULT))
                                    .addEvn("IN_FILE_NAME", IN_FILE_NAME)
                                    .addEvn("CM_MYPRGID", CM_MYPRGID)
                                    .addEvn("CUR_PID", pid).execute();

//                            ArrayList<ZipItemDto> CHK_RESULT_DATA = ZipUtil.zipList(getenv(C_aplcom1Service.CM_BKUPRESULT) + "/" + BAT_YYYYMMDD,
//                                    ".*\\.zip", IN_FILE_NAME, WORK_DIR + "/" + CM_MYPRGID + "_" + pid);

                            String CHK_RESULT = FileUtil.readFile(WORK_DIR + "/" + CM_MYPRGID + "_" + pid).replaceAll("\n", "");

                            //grep のリターン値を保存
                            //grep の結果を保存
                            if (!shellExecuteDto.RTN0()) {
                                String CHK_SIZE = FileUtil.splitData(CHK_RESULT)[3];
                                if (StringUtils.isNotEmpty(CHK_SIZE) && CHK_SIZE.compareTo("0") > 0) {
                                    if (Objects.equals(IN_APLOG_FLG, "E")) {
                                        APLOG_WT("エラーファイル存在 [" + CHK_RESULT + "]", "-F" + IN_APLOG_FLG);
                                    }
                                    if (Objects.equals(IN_APLOG_FLG, "W")) {
                                        APLOG_WT("警告ファイル存在 [" + CHK_RESULT + "]", "-F" + IN_APLOG_FLG);
                                    }
                                    if (Objects.equals(IN_APLOG_FLG, "I")) {
                                        APLOG_WT("チェックファイル存在 [" + CHK_RESULT + "]", "-F" + IN_APLOG_FLG);
                                    }
                                } else {
                                    //圧縮前のファイルサイズが0
                                    APLOG_WT("チェックファイルサイズ＝０ [" + CHK_RESULT + "]", FI);
                                }
                            } else {
                                //バックアップにファイルが存在しない
                                APLOG_WT("チェックファイルなし [" + IN_FILE_NAME + "]", FI);
                            }
                        } else {
                            //検索ディレクトリ指定が0(=当日),1(=前日)以外
                            APLOG_WT("検索ディレクトリ指定エラー [" + IN_CHKDIR + "]", FI);
                        }
                    }

                    return Rtn_OK;

                });

        if (readFileDto.getStatus() == Rtn_NG) {
            return Rtn_NG;
        }


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
}
