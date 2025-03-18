package jp.co.mcc.nttdata.batch.business.job.cmBTstpfS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  店舗別ポイント付与実績ファイル作成シェル
 * #    プログラムID  ：  cmBTstpfS
 * #
 * #    【処理概要】
 * #       HSポイント日別情報YYYYMM、HSポイント日別内訳情報YYYYMMから店舗別ポイント付与実績ファイルを作成する。
 * #
 * #    【引数説明】
 * #       なし
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2022/11/10 NDBS.谷津：初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA BUSINESS SYSTEMS CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTstpfSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBService;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "店舗別ポイント付与実績ファイル作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        String FILE_NAME1 = "STORE_POINT_NR_";
        String FILE_NAME2 = "STORE_POINT_NH_";
        String FILE_NAME3 = "STORE_POINT_KR_";
        String FILE_NAME4 = "STORE_POINT_KH_";

        //#  システム日付+時刻
        SYS_YYYYMMDD = DateUtil.nowDateFormat("yyyyMMdd");

        //#  前月YYYYMM取得
        String LAST_MONTH_YYYYMM = DateUtil.getLM_YYYYMM();

        //#  DB接続先
        setConnectConf();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリファイル作成失敗
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FW());
                cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

                return Rtn_NG;
            }
        }

        //###########################################
        //# 店舗別ポイント付与実績(年度_連動)を出力
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME1 + SYS_YYYYMMDD + ".csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗別ポイント付与実績(年度_連動)取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }

        //###########################################
        //# 店舗別ポイント付与実績(年度_非連動)を出力
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME2 + SYS_YYYYMMDD + ".csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗別ポイント付与実績(年度_非連動)取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }

        //###########################################
        //# 店舗別ポイント付与実績(期間限定_連動)を出力
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME3 + SYS_YYYYMMDD + ".csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗別ポイント付与実績(期間限定_連動)取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }

        //###########################################
        //# 店舗別ポイント付与実績(期間限定_非連動)を出力
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD", SYS_YYYYMMDD)
                .addEvn("LAST_MONTH_YYYYMM", LAST_MONTH_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME4 + SYS_YYYYMMDD + ".csv");
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).M("店舗別ポイント付与実績(期間限定_非連動)取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());

            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBService.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
