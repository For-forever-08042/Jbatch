package jp.co.mcc.nttdata.batch.business.job.cmBTdldmS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTdldmB.CmBTdldmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  DMリスト作成処理
 * #    プログラムID  ：  cmBTdldmS
 * #
 * #    【処理概要】
 * #      ダウンロード用（顧客ＰＣで使用）にファイル作成する処理
 * #     「DMリスト作成（cmBTdldmB）」を起動するためのシェル。
 * #      顧客ＰＣとの共有ディレクトリより保持期間を超過したファイルを削除する。
 * #      対象顧客リストの格納ディレクトリより保持期間を超過したファイルを削除する。
 * #
 * #    【引数説明】
 * #       -o出力ファイル名   : （任意）出力ファイルの物理ファイル名（$CM_APWORK）
 * #       -DEBUG or -debug   : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2012/11/28 SSI.越後谷  ： 初版
 * #      2.00 :   2016/01/27 SSI.上野    ： 対象顧客リストファイル削除処理削除
 * #                                         クーポン顧客登録処理で削除するため
 * #      3.00 :   2018/04/11 SSI.吉田    ： POS更改対応
 * #                                         キャンペーン情報を連携された当日に抽出処理を
 * #                                         行うため条件を変更
 * #     40.00 :   2022/09/27 SSI.川内    ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTdldmSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmABgdldBServiceImpl cmABgdldBServiceImpl;

    @Autowired
    private CmBTdldmBServiceImpl cmBTdldmBServiceImpl;

    private String DEL_YMD = "";
    private String DEL_YM = "";
    private String DEL_Y = "";
    private String DEL_YMD2 = "";

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "DMリスト作成";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;
        int Rtn_ER = 99;

        //#  引数定義
        String ARG_OPT1 = "-d";             //###  処理日付
        String DB_DELGRP = "キャンペーン";    //###  cmABgdldB

        String DB_KBN = "SD";

        //#  DB接続先
        setConnectConf();

        //#  ｢ＤＭリスト作成｣用ジョブ実績出力用ログファイル名称
        String CM_G21002D = "JBch_G21002D";  //# 本番用は「JBch_G21002D」

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
        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //##  引数格納変数初期化
        //###########################################
        String OPTION1 = "";

        //###########################################
        //##  引数格納
        //###########################################
        OPTION1 = args[0];

        //###########################################
        //#  バッチ処理日付(前日)の取得
        //###########################################
        MainResultDto mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
        int RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付(前日)の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        String BAT_YYYYMMDD_1 = mainResultDto.result;

        //################################################
        //#  処理対象情報をジョブ実績ログファイルに出力
        //################################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD_1", BAT_YYYYMMDD_1)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ＤＭリスト作成対象キャンペーンの取得に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# ログファイル初期化
        String logName = CM_JOBRESULTLOG + "/" + CM_G21002D + ".log";
        FileUtil.writeFile(logName, "");

        //# 取得結果のレコード単位に、以下の処理を繰り返す。
        ReadFileDto.getInstance().readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".tmp").loop2((CAMPAIGN_ID, CAMPAIGN_NAME) -> {
            String CM_COUPONLIST = getenv(C_aplcom1Service.CM_COUPONLIST);
            //# データ件数取得
            int FILE_DATA_CNT = FileUtil.countLinesByRegex(CM_COUPONLIST, "^" + CAMPAIGN_ID + "_CUSTOMER_.{14}.csv");
            //# ログファイルに追加出力
            FileUtil.writeFileByAppend(logName, "        " + CAMPAIGN_NAME + "：" + FILE_DATA_CNT + " 件");
            return Rtn_OK;
        });

        //###########################################
        //#  プログラム実行
        //###########################################
        int RNT = cmBTdldmBServiceImpl.main(getExecuteBaseParam().add(args)).exitCode;
        if (RNT != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + PidUtil.getPid();
        String TEMP_FILE3 = CM_APWORK_DATE + "/" + CM_MYPRGID + "03_" + PidUtil.getPid();

        //###########################################
        //#  削除対象日付取得
        //###########################################
        mainResultDto = cmABgdldBServiceImpl.main(getExecuteBaseParam().add(DB_DELGRP));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("データ削除日付取得エラー　TBL略称＝[" + OPTION1 + "]　テーブルGRP=" + DB_DELGRP + "　STATUS=" + RTN, FE);

            return Rtn_NG;
        }
        FileUtil.writeFile(TEMP_FILE1, mainResultDto.result);

        //###########################################
        //#  削除対象日付取得を種類ごとに分割
        //###########################################
        ReadFileDto.getInstance().readFile(TEMP_FILE1).loop4((READ2_FLD1, READ2_FLD2, READ2_FLD3, READ2_FLD4) -> {
            DEL_YMD = READ2_FLD1;
            DEL_YM = READ2_FLD2;
            DEL_Y = READ2_FLD3;
            DEL_YMD2 = READ2_FLD4;
            return Rtn_OK;
        });

        //###########################################
        //#  顧客ＰＣ共有ファイル削除
        //###########################################
        List<String> TEMP_FILE2_DATE =
                FileUtil.findByRegex(getenv(CmABfuncLServiceImpl.CM_PCRENKEI_KOJINARI), "^.{8}_DM_.*.csv");
        for (String item : TEMP_FILE2_DATE) {
            String filename = basename(item);
            String bat_date = StringUtils.substring(filename, 0, 8);
            if (DEL_YMD.compareTo(bat_date) > 0) {
                FileUtil.deleteFile(getenv(CmABfuncLServiceImpl.CM_PCRENKEI_KOJINARI) + "/" + filename);
            }
        }

        //# 「CM_APWORK_DATE」でtmpファイル削除
        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);

        //###########################################
        //#  対象顧客リストファイル削除
        //###########################################
        //#ls -1  $CM_COUPONLIST/*_CUSTOMER_??????????????.csv 2>/dev/null | xargs -i basename {} > ${TEMP_FILE3} 2>/dev/null
        //#
        //#cat ${TEMP_FILE3} | while read filename
        //#do
        //#    bat_date=`echo ${filename} | cut -d"_" -f3 | cut -c1-8`
        //#    if test "${DEL_YMD}" -gt "${bat_date}"
        //#    then
        //#        rm -f ${CM_COUPONLIST}/${filename} > /dev/null 2>&1
        //#    fi
        //#done

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
