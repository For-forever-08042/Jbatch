package jp.co.mcc.nttdata.batch.business.job.cmABptlmS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　Hinemosジョブ実績ログ出力
 * #    プログラムID　：　cmABptlmS
 * #
 * #    【処理概要】
 * #        指定されたファイルIDにて、FTP受信を行う機能
 * #        受信パラメータファイルに、ファイルIDごとの対象ファイルが定義されて
 * #        いることを前提とする。
 * #
 * #    【引数説明】
 * #       メッセージ   :　ログファイルに出力するメッセージ
 * #
 * #    【戻り値】
 * #       0 　　 ：　正常
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2013/04/19 SSI.本田：初版
 * #
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmABptlmSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    @Override
    public int taskExecuteCustom(String[] args) {
        //#########################################
        //# スクリプト変数設定
        //#########################################
        //#メッセージファイル名
        String RESULTLOG_FILENAME = "result_c";
        //#ロックファイルパス
        String DIR_LOCK = "/var/log/syskiban";
        //#ロックファイル名
        String LOCK_FILENAME = "sk_job_jobrsltwri_lock_c";
        //
        //#メッセージ出力用日付
        String DATE = DateUtil.getTHHMMSS();

        //#########################################
        //# 引数セット
        //#########################################
        String MESSAGE = args[0];
        String JOBID = args[1];
        //#########################################
        //# ジョブ結果ログの有無を確認し、文字列として読み込む
        //#########################################
        //
        String LOG_RESULT_STR = "";
        if (StringUtils.isNotEmpty(JOBID)) {
            if (FileUtil.isExistFile(CM_JOBRESULTLOG + "/" + JOBID + ".log")) {
                LOG_RESULT_STR = FileUtil.readFile(CM_JOBRESULTLOG + "/" + JOBID + ".log");
            }
        }

        //#########################################
        //# 結果書き込み処理
        //#########################################
        while (true) {
            // # 他プロセスの書き込み中を示すロックファイルが存在する限り、1秒ずつ待つ
            if (FileUtil.isExistFile(DIR_LOCK + "/" + LOCK_FILENAME)) {
                sleep(1);
                continue;
            } else {
                //# ロックファイル生成(ファイル内容: 自身のPID)
                FileUtil.writeFile(DIR_LOCK + "/" + LOCK_FILENAME, PidUtil.getPid());
                sleep(1);

                // # ロックファイル内容とPIDが一致する場合、結果出力処理を行う
                if (FileUtil.isExistFile(DIR_LOCK + "/" + LOCK_FILENAME)) {
                    String MY_PID = PidUtil.getPid();
                    String LOCK_FILE_VAR = FileUtil.readFile(DIR_LOCK + "/" + LOCK_FILENAME);
                    if (MY_PID.equals(LOCK_FILE_VAR)) {
                        //  # 結果ファイルへの書き込み処理
                        //  # 日付スペースまで12文字まで固定
                        int CMD_RC = FileUtil.writeFileByAppend(CM_JOBRESULTLOG + "/" + RESULTLOG_FILENAME +
                                ".log", DATE + " " + MESSAGE + "\n", SystemConstant.UTF8);

                        if (StringUtils.isNotEmpty(LOG_RESULT_STR)) {
                            CMD_RC += FileUtil.writeFileByAppend(CM_JOBRESULTLOG + "/" + RESULTLOG_FILENAME + ".log",
                                    LOG_RESULT_STR, SystemConstant.UTF8);
                        }
                        if (CMD_RC != 0) {
                            FileUtil.deleteFile(DIR_LOCK + "/" + LOCK_FILENAME);
                            return Rtn_NG;
                        }

                        // # 結果出力成功時はロックファイルを削除し、結果ログ出力処理を終了する
                        if (FileUtil.deleteFile(DIR_LOCK + "/" + LOCK_FILENAME) != 0) {
                            return Rtn_NG;
                        }
                        break;
                    } else {
                        //   # ロックファイル内容がPIDと異なる場合は他プロセスとの競合回避のため、1秒待つ
                        sleep(1);
                        continue;
                    }
                } else {
                    sleep(1);
                    continue;
                }
            }
        }
        return Rtn_OK;
    }
}
