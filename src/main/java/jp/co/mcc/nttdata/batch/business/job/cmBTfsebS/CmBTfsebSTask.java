package jp.co.mcc.nttdata.batch.business.job.cmBTfsebS;

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
 #-------------------------------------------------------------------------------
 #    名称          ：  不正防止データ(１万Ｐ以上付与)ファイル作成シェル
 #    プログラムID  ：  cmBTfsebS
 #
 #    【処理概要】
 #       HSポイント日別情報YYYYMMから不正防止データ(1万P以上付与)を作成する。
 #
 #    【引数説明】
 #       なし
 #
 #    【戻り値】
 #       10     ：  正常
 #       49     ：  異常
 #-------------------------------------------------------------------------------
 #    稼働環境
 #      Red Hat Enterprise Linux 6
 #
 #    改定履歴
 #      1.00 :   2022/11/10 NDBS.高橋：初版
 #-------------------------------------------------------------------------------
 #  $Id:$
 #-------------------------------------------------------------------------------
 #  Copyright (C) 2022 NTT DATA BUSINESS SYSTEMS CORPORATION
 #-------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTfsebSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Override
    public int taskExecuteCustom(String[] args) {



        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;


        String FILE_NAME = "FUSEI_POINT_OVER_";
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDDHHMMSS();

        //#  前月取得
        String LAST_MONTH = DateUtil.getLM_YYYYMM();


        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
                if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                    // 作業ディレクトリファイル作成失敗
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                    cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }
        }

        //###########################################
        //# 不正防止データ（1万P以上）付与を出力
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("SYS_YYYYMMDD",SYS_YYYYMMDD)
                .addEvn("LAST_MONTH", LAST_MONTH).execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + FILE_NAME + SYS_YYYYMMDD + ".csv");
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("不正防止データ(１万Ｐ以上付与）取得に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
