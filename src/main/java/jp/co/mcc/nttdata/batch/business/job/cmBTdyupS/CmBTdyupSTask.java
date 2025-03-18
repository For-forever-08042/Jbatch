package jp.co.mcc.nttdata.batch.business.job.cmBTdyupS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　バッチ処理日付更新
 * #    プログラムID　：　cmABdyupS
 * #
 * #    【処理概要】
 * #        「PSバッチ日付情報」に、翌日のバッチ処理年月日のレコードを
 * #        追加する処理。
 * #
 * #    【引数説明】
 * #        なし
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       99　　 ：　異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :        2012/10/26 SSI.越後谷  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmBTdyupSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 99;

    //#その他
    String BKUP_IFS = "@";

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "バッチ処理日付更新";

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length != 0) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }

        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //###########################################
        //#  作業用ディレクトリ・テンポラリファイル
        //###########################################
        String WORK_DIR = CM_APWORK + "/" + SYS_YYYYMMDD;
        FileUtil.mkdir(WORK_DIR);
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = WORK_DIR + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = WORK_DIR + "/" + CM_MYPRGID + "02_" + pid;

        //###########################################
        //#  翌日バッチ処理日付取得
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DT"));
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("バッチ処理日付取得エラー", FW);
            return RTN;
        }

        //###########################################
        //#  SQL実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID+"_01")
                .defaultEvn(this).addEvn("WORK_DIR",WORK_DIR).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).addEvn("TEMP_FILE1", TEMP_FILE1).addEvn("TEMP_FILE2", TEMP_FILE2).execute();


        if (shellExecuteDto.RTN0()) {
            String SQL_CD  = FileUtil.SQL_CD_ORA_FILE(TEMP_FILE2);
            APLOG_WT("SQLエラー（バッチ日付情報の追加に失敗）　翌日日付[" + BAT_YYYYMMDD + "]　[SQLCODE：" + SQL_CD + "]", FE);
            return Rtn_NG;
        }

        APLOG_WT("SQL実行（PSバッチ日付情報の追加[" + BAT_YYYYMMDD + "]）正常終了", FI);
        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
        return Rtn_OK;
    }
}
