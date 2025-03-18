package jp.co.mcc.nttdata.batch.business.job.cmSQsql2S;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  家族入会状況集計
 * #    プログラムID  ：  cmSQsql2S
 * #
 * #    【処理概要】
 * #       家族制度の入会状況データを作成する処理、
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
 * #      1.00 :   2013/10/18 SSI.上野：初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Component
public class CmSQsql2STask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABgdatBServiceImpl cmABgdatBServiceImpl;

    String SQL_CD = "";

    @Override
    public int taskExecuteCustom(String[] args) {

        /*
         * ###########################################
         * #  プログラムIDを環境変数に設定
         * ###########################################
         */
        CM_MYPRGNAME = "家族入会状況集計";
        setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

        /*
         * ###########################################
         * #  開始メッセージをAPログに出力
         * ###########################################
         */
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        /*
         * ###########################################
         * #  定数定義
         * ###########################################
         */
        int Rtn_OK = 10;
        int Rtn_NG = 49;
        int Rtn_ER = 99;

        //#  その他
        String BKUP_IFS = IFS;

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String TEMP_FILE1 = getenv(CmABfuncLServiceImpl.CM_MYPRGID)
                + "01_" + PidUtil.getPid();

        //###########################################
        //#  システム日付の判定（毎月２日実行）
        //###########################################
        MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        String DATE_TODAY = StringUtils.substring(cmABgdatBResult.result, 6, 8);
        if (!"02".equals(DATE_TODAY)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_OK;
        }

      //###########################################
      //#  稼動ディレクトリ決定
      //###########################################
      if (StringUtils.isEmpty(CM_APWORK_DATE)) {
        CM_APWORK_DATE = getenv(CM_APWORK) + "/" + DateUtil.getYYYYMMDD();
        setenv(C_aplcom1Service.CM_APWORK_DATE, CM_APWORK_DATE);
      }

      if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
        FileUtil.mkdir(CM_APWORK_DATE);
      }

        //###########################################
        //# 会員入会状況の集計
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this).execute();

        if (shellExecuteDto.RTN0()) {
            //
            ArrayList<String> SQL_CD_data = FileUtil.findByRegex(CM_APWORK_DATE, "^count_family.{8}.utf");
            SQL_CD_data.forEach(item -> {
                SQL_CD += FileUtil.SQL_CD_ORA_FILE(item);
            });
            APLOG_WT("家族入会状況の集計処理に失敗しました。SQLCODE={" + SQL_CD + "}", FW);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        FileUtil.deleteFileByRegx(CM_APWORK_DATE ,"^count_family.{8}" + ".csv");
        ShellExecuteDto shellExecuteDto02 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this).execute();
        String splfl = shellExecuteDto02.result;
        if (!shellExecuteDto02.RTN0()) {
            IconvUtil.main("UTF-8", "SHIFT_JISX0213", splfl + ".utf", splfl + ".csv");
        }
        //
        FileUtil.deleteFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");

        //###########################################
        //#  保持期間を超過したファイルを削除
        //###########################################
        int FILE_CNT = FileUtil.countLinesByRegex(CM_PCRENKEI_KOJINNASHI ,"^count_family.{8}" + ".csv");
        if (FILE_CNT >= 3) {
            List<String> TEMP_FILE1_DATE = FileUtil.findByRegex(CM_PCRENKEI_KOJINNASHI,"^count_family.{8}" + ".csv");
          FileUtil.writeFile(CM_APWORK_DATE + "/" +TEMP_FILE1,TEMP_FILE1_DATE.stream().collect(Collectors.joining("\n")));
            for (String item : TEMP_FILE1_DATE) {
              String filename = basename(item);
              int RTN = FileUtil.deleteFile(CM_PCRENKEI_KOJINNASHI+"/"+filename);
              if (RTN != 0) {
                APLOG_WT("保持期間切れファイルの削除に失敗しました [" + filename + "]", FW);
              }
              FILE_CNT--;
              if (FILE_CNT < 3) {
                break;
              }
            }
        }
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
