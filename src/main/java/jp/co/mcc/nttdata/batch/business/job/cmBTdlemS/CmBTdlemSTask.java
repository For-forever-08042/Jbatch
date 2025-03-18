package jp.co.mcc.nttdata.batch.business.job.cmBTdlemS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTdlemB.CmBTdlemBServiceImpl;
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

import java.util.ArrayList;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  Ｅメールリスト作成処理
 * #    プログラムID  ：  cmBTdlemS
 * #
 * #    【処理概要】
 * #      ダウンロード用（顧客ＰＣで使用）にファイル作成する処理
 * #     「Eメールリスト作成（cmBTdlemB）」を起動するためのシェル。
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
 * #     40.00 :   2022/09/26 SSI.川内    ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTdlemSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTdlemBServiceImpl cmBTdlemB;
    @Autowired
    CmABgdldBServiceImpl cmABgdldB;
    String DEL_YMD;
    String DEL_YM;
    String DEL_Y;
    String DEL_YMD2;

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;
        int Rtn_ER = 99;


        //#  引数定義

        String ARG_OPT1 = "-d";//   ###  処理日付
        String DB_DELGRP = "キャンペーン";// ###  cmABgdldB
        String DB_KBN = "SD";

        //#  ｢Ｅメールリスト作成｣用ジョブ実績出力用ログファイル名称
        String CM_G21001D = "JBch_G21001D";// # 本番用は「JBch_G21001D」

        int RETRY_CNT = 3;            //#リトライ回数
        long PRE_SLEEP_TIME = 300;    //#実行前スリープ時間 5min
        int RETRY_SLEEP_TIME = 300;   //#リトライスリープ時間 5min

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //cd $CM_APWORK_DATE


        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 2) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        //###########################################
        //##  引数格納変数初期化
        //###########################################


        //#引数格納変数初期化
        //#サーバー番号の初期値01
        String OPTION1 = args[0];

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 当日日付取得
        MainResultDto cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam().add("-DY"));
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付(前日)の取得に失敗しました").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String BAT_YYYYMMDD_1 = cmABgdatBResult.result;

        //################################################
        //#  処理対象情報をジョブ実績ログファイルに出力
        //################################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .defaultEvn(this).addEvn("BAT_YYYYMMDD_1", BAT_YYYYMMDD_1)
                .execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("Ｅメールリスト作成対象キャンペーンの取得に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //# ログファイル初期化
        //###########################################
        FileUtil.writeFile(getenv(CM_JOBRESULTLOG) + "/" + CM_G21001D + ".log", "");

        //# 取得結果のレコード単位に、以下の処理を繰り返す。
        ReadFileDto.getInstance().readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".tmp").loop2((CAMPAIGN_ID, CAMPAIGN_NAME) -> {
            // # データ件数取得
            int FILE_DATA_CNT = FileUtil.countLinesByRegex(getenv(C_aplcom1Service.CM_COUPONLIST), CAMPAIGN_ID + "_CUSTOMER_.{14}.csv");
            // # ログファイルに追加出力
            FileUtil.writeFileByAppend(CM_JOBRESULTLOG + "/" + CM_G21001D + ".log", "        " + CAMPAIGN_NAME + "：" + FILE_DATA_CNT + " 件");
            return Rtn_OK;
        });

        //###########################################
        //#  プログラム実行
        //###########################################
        MainResultDto mainResultDto = cmBTdlemB.main(getExecuteBaseParam().add(args));
        if (mainResultDto.exitCode != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //################################################
        //#  テンポラリファイル取得
        //################################################
        String pid = PidUtil.getPid();
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + pid;
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + pid;
        String TEMP_FILE3 = CM_APWORK_DATE + "/" + CM_MYPRGID + "03_" + pid;
        //###########################################
        //#  削除対象日付取得
        //###########################################
        mainResultDto = cmABgdldB.main(getExecuteBaseParam().add(DB_DELGRP));
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            APLOG_WT("データ削除日付取得エラー　TBL略称＝[" + OPTION1 + "]　テーブルGRP=" + DB_DELGRP + "　STATUS=" + RTN, FE);
            return Rtn_ER;
        }
        FileUtil.writeFile( TEMP_FILE1, mainResultDto.result);

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
        ArrayList<String> TEMP_FILE2_DATA = FileUtil.findByRegex(getenv(C_aplcom1Service.CM_PCRENKEI_KOJINARI), "^.{14}_Mail_*.csv");
        for (String filename : TEMP_FILE2_DATA) {
            String bat_date = filename.substring(0, 8);
            if (StringUtils.compare(DEL_YMD, bat_date) > 0) {
                FileUtil.deleteFile(getenv(C_aplcom1Service.CM_PCRENKEI_KOJINARI) + "/" + filename);
            }
        }


//        # 「CM_APWORK_DATE」でtmpファイル削除
        FileUtil.deleteFile(TEMP_FILE1);
        FileUtil.deleteFile(TEMP_FILE2);
        FileUtil.deleteFile(TEMP_FILE3);

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
