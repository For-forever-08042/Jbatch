package jp.co.mcc.nttdata.batch.business.job.cmBTfsslS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-----------------------------------------------------------------------------------
 * #    名称          ：  不正防止データ(1回の買上げで10万円以上)ファイル作成シェル
 * #    プログラムID  ：  cmBTfsslS
 * #
 * #    【処理概要】
 * #       HSポイント日別情報YYYYMMから不正防止データ(1回の買上げで10万円以上)
 * #       を作成する。
 * #
 * #    【引数説明】
 * #       なし
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-----------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2022/11/29 NDBS.高橋：初版
 * #-----------------------------------------------------------------------------------
 * #  $Id:$
 * #-----------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA BUSINESS SYSTEMS CORPORATION
 * #-----------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTfsslSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "不正防止データ（1回の買上げで10万円以上）作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;


        String FILE_NAME = "FUSEI_POS_KAIAGE_OVER_";
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDDHHMMSS();

        //#  前月取得
        String LAST_MONTH = DateUtil.getLM_YYYYMM();

        //#  前月初日取得
        String START_DATE = DateUtil.getLMFirs_YYYYMMDD();

        //#  前月末日取得
        String END_DATE = DateUtil.getLMLast_YYYYMMDD();


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

        //################################################
        //#  PMダウンロードファイル情報取得
        //################################################
        ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this).addEvn("CM_APSQL",getenv(C_aplcom1Service.CM_APSQL))
                .addEvn("LAST_MONTH", LAST_MONTH)
                .addEvn("START_DATE", START_DATE)
                .addEvn("SYS_YYYYMMDD",SYS_YYYYMMDD)
                .addEvn("END_DATE", END_DATE).execute();
        if (shellExecuteDto1.RTN0()) {
//            String SQL_CD = FileUtil.readFile(CM_APWORK_DATE + "/" + FILE_NAME + SYS_YYYYMMDD + ".csv");
//            SQL_CD = Arrays.stream(SQL_CD.replaceAll("ORA-", "\nORA-").split("\n"))
//                    .filter(item -> item.indexOf("ORA-") >= 0)
//                    .map(item -> item.substring(4, 9)).collect(Collectors.joining("\n"));
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME + SYS_YYYYMMDD + ".csv");
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("不正防止データ(1回の買上げで10万円以上）取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
