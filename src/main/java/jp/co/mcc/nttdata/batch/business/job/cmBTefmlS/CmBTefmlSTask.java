package jp.co.mcc.nttdata.batch.business.job.cmBTefmlS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmBTfdmlS.CmBTfdmlSTask;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ファイルダウンロード通知メール送信監視シェル
 * #    プログラムID  ：  cmBTefmlS
 * #
 * #    【処理概要】
 * #       ファイルダウンロード通知メール送信を実行するものである。
 * #       当処理は常駐し、5分ごとにファイルダウンロード通知メール送信バッチを
 * #       実行する。
 * #
 * #    【引数説明】
 * #       -DEBUG or -debug        : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2015/09/16 SSI.上野：初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTefmlSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmBTfdmlSTask cmBTfdmlSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "ファイルダウンロード通知メール送信監視";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-DEBUG";          //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //###  デバッグモードでの実行（トレース出力機能が有効）

        //###########################################
        //#  定数定義
        //###########################################
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.nowDateFormat("yyyyMMdd");
        ;
        //#  システム日付と時刻
        String SYS_YYYYMMDDHHMMSS = DateUtil.getYYYYMMDDHHMMSS();

        //#  終了時刻監視用の基準日付と時刻
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String SYS_YYYYMMDD_N = sdf.format(calendar.getTime());

        String KIJYUN_YYYYMMDDHHMM = SYS_YYYYMMDD_N + "0005";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";

        //#  引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT1, ARG_OPT2)) {
                OPTION1 = arg;
            }
        }

        while (true) {

            //# ファイル監視実施タイミング制御用
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm");
            String SYS_MM = time.format(formatter);
            int SYS_MM_INT = Integer.parseInt(SYS_MM);

            if (0 == SYS_MM_INT || 5 == SYS_MM_INT || 10 == SYS_MM_INT || 15 == SYS_MM_INT || 20 == SYS_MM_INT || 25 == SYS_MM_INT ||
                    30 == SYS_MM_INT || 35 == SYS_MM_INT || 40 == SYS_MM_INT || 45 == SYS_MM_INT || 50 == SYS_MM_INT || 55 == SYS_MM_INT) {

                int RTN = cmBTfdmlSTask.main(getExecuteBaseParam().add(OPTION1));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイルダウンロード通知メール送信処理エラー").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                    return Rtn_NG;
                }

                sleep(60);
            }

            //# 常駐時間チェック
            //# ファイルダウンロード通知メール送信は翌2405までとする
            String CHK_YYYYMMDDHHMM = DateUtil.nowDateFormat("yyyyMMddHHmm");

            if (CHK_YYYYMMDDHHMM.compareTo(KIJYUN_YYYYMMDDHHMM) > 0) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("常駐監視終了").FI());
                break;
            }

            //# ファイル監視実施タイミング制御用
            time = LocalTime.now();
            SYS_MM = time.format(formatter);
            SYS_MM_INT = Integer.parseInt(SYS_MM);

            //# 次処理のファイル監視開始タイミングじゃなかったら６０秒SLEEP
            if (0 != SYS_MM_INT && 5 != SYS_MM_INT && 10 != SYS_MM_INT && 15 != SYS_MM_INT && 20 != SYS_MM_INT && 25 != SYS_MM_INT &&
                    30 != SYS_MM_INT && 35 != SYS_MM_INT && 40 != SYS_MM_INT && 45 != SYS_MM_INT && 50 != SYS_MM_INT && 55 != SYS_MM_INT) {
                sleep(60);
            }
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
