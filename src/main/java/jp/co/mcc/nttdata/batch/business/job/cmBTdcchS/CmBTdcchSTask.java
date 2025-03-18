package jp.co.mcc.nttdata.batch.business.job.cmBTdcchS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　TSＤＣログ情報チェック処理
 * #    プログラムID　：　cmBTdcchS
 * #
 * #    【処理概要】
 * #        TSＤＣログ情報に未処理のトランザクションが残っているかを定期的にチェックする機能
 * #
 * #    【引数説明】
 * #                -s（サーバー番号）(01、02 指定)
 * #                -k（検索キー）
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       49　　 ：　未処理のトランザクションが存在する状態で終了
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :	2012/10/26 SSI.越後谷  ： 初版
 * #      40.00:   2022/12/26 SSI.申      ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTdcchSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "TSＤＣログ情報チェック処理";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_WAR = 49;
        int Rtn_NG = 99;

        //#RETRY_CNT=3         #リトライ回数
        //#PRE_SLEEP_TIME=6    #実行前スリープ時間
        //#RETRY_SLEEP_TIME=3  #リトライスリープ時間

        int RETRY_CNT = 3;            //#リトライ回数
        long PRE_SLEEP_TIME = 300;    //#実行前スリープ時間 5min
        int RETRY_SLEEP_TIME = 300;   //#リトライスリープ時間 5min

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-s";           //###  サーバー番号
        String ARG_OPT2 = "-k";           //###  検索キー

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        setCM_APWORK_DATE();

        String WORK_DIR = CM_APWORK_DATE;
        if (!FileUtil.isExistDir(WORK_DIR)) {
            if (!FileUtil.mkdir(WORK_DIR)) {
                APLOG_WT("テンポラリファイル用のディレクトリ作成エラー" + WORK_DIR, FE);

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#引数格納変数初期化
        //#サーバー番号の初期値01
        String OPTION1 = "01";
        //#検索キー初期値1
        String OPTION2 = "1";
        //#  引数格納
        for (String arg : args) {
            if (StringUtils.startsWith(arg, ARG_OPT1)) {
                OPTION1 = arg.substring(2);
            } else if (StringUtils.startsWith(arg, ARG_OPT2)) {
                OPTION2 = arg.substring(2);
            }
        }

        //###########################################
        //#  テンポラリファイル取得
        //###########################################
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + PidUtil.getPid();

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 当日日付取得
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー[" + RTN + "]", FW);

            return Rtn_NG;
        }
        String BAT_YYYYMMDD_T = cmABgdatBResult.result;

        //###########################################
        //#  DB接続先
        //###########################################
        setConnectConf();
        String DB_CONN_VALUE = CONNECT_SD;

        //###########################################
        //#  実行前スリープ
        //###########################################
        sleep(PRE_SLEEP_TIME); // seconds unit

        //###########################################
        //#  チェック実行
        //###########################################
        int ISLOOP = 1;
        int RETCNT = 0;
        int RETOVER = 0;

        while (ISLOOP != 0) {
            int CNT = 0;
            int SA01OK = 0;

            while (CNT != 1) {
                //###########################################
                //#  TSＤＣログ情報チェックSQL文編集
                //###########################################
                String SQL_VALUE = "select count(*) ";
                SQL_VALUE = SQL_VALUE + "from TSＤＣログ情報" + OPTION1 + BAT_YYYYMMDD_T;
                SQL_VALUE = SQL_VALUE + " where 処理フラグ = 0 ";
                if (Integer.parseInt(OPTION1) == 2) {
                    SQL_VALUE = SQL_VALUE + " and 検索キー = " + OPTION2;
                }

                //###########################################
                //#  TSＤＣログ情報チェックSQL文実行
                //###########################################
                ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                        .addEvn("SQL_VALUE", SQL_VALUE)
                        .addEvn("DB_CONN_VALUE", DB_CONN_VALUE)
                        .addEvn("TEMP_FILE1", TEMP_FILE1)
                        .addEvn("TEMP_FILE2", TEMP_FILE2).execute();
                if (shellExecuteDto.RTN0()) {
                    String RTNStr = shellExecuteDto.result;
                    // SQL_CD=`cat ${TEMP_FILE2} | sed s/ORA-/'\n'ORA-/ | grep "ORA-" |  cut -c5-9`
                    String SQL_CD =   FileUtil.SQL_CD_ORA_FILE(TEMP_FILE2);
                    // APLOG_WT "SQLエラー TSＤＣログ情報${SANO}${BAT_YYYYMMDD_T} のselect[${RTN}]　[SQLCODE：${SQL_CD}]" -FE
                    APLOG_WT("SQLエラー TSＤＣログ情報" + OPTION1 + BAT_YYYYMMDD_T + " のselect[" + RTNStr + "]　[SQLCODE：" + SQL_CD + "]", FE);
                    FileUtil.deleteFile(TEMP_FILE1 + ".sql");
                    FileUtil.deleteFile(TEMP_FILE2);
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FE());

                    return Rtn_NG;
                }

                // DCCNT=`cat ${TEMP_FILE2} | sed  -e "/^$/d" | sed  -e "s/ //g"`
                shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_03")
                        .addEvn("TEMP_FILE2", TEMP_FILE2).execute();
                String DCCNT = shellExecuteDto.result;
                if (StringUtils.equals(DCCNT, "0")) {
                    if (CNT == 0) {
                        SA01OK = 1;
                    }
                }
                CNT++;
            }
            ;

            if (SA01OK == 1) {
                ISLOOP = 0;
            } else {
                RETCNT++;

                if (RETCNT > RETRY_CNT) {
                    ISLOOP = 0;
                    RETOVER = 1;
                } else {
                    sleep(RETRY_SLEEP_TIME); // seconds unit
                }
            }
        }
        ;

        //###########################################
        //#  テンポラリファイル削除
        //###########################################
        FileUtil.deleteFile(TEMP_FILE1 + ".sql");
        FileUtil.deleteFile(TEMP_FILE2);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        if (RETOVER == 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().M("未処理のトランザクションデータが存在します。").FW());

            return Rtn_WAR;
        } else {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());

            return Rtn_OK;
        }
    }
}
