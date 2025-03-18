package jp.co.mcc.nttdata.batch.business.job.cmBTdmupS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTdmupB.CmBTdmupBImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.PidUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ＤＭ止め区分一括更新
 * #    プログラムID  ：  cmBTdmupS
 * #
 * #    【処理概要】
 * #        DM止め区分一括更新対象会員リスト（入力ファイル）をもとに、
 * #        DM止め区分を送信不可に更新するためのシェル。
 * #        「ＤＭ止め区分一括更新（cmBTdmupB）」を起動するためのシェル。
 * #        顧客ＰＣとの共有ディレクトリより保持期間を超過したファイルを削除する。
 * #
 * #    【引数説明】
 * #       -DEBUG or -debug  : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      0.01 :   2019/08/06 SSI.上野：初版
 * #     40.00 :   2022/09/30 SSI.川内：MCCM初版
 * #
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2016 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmBTdmupSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    @Autowired
    CmBTdmupBImpl CmBTdmupBImpl;

    @Autowired
    CmABgdldBServiceImpl cmABgdldBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################
    // 戻り値
    int Rtn_OK = 10;
    int Rtn_WN = 49;
    int Rtn_NG = 99;

    //# DB接続先
    String DB_KBN = "SD";

    //#引数定義
    private static final String ARG_OPT1 = "-DEBUG";//     ###  デバッグモードでの実行（トレース出力機能が有効）
    String OPTION1 = null;
    // ＤＭ止め区分一括更新対象会員リストファイル
    String INPUT_FILENAME = "^IN_.{14}_updateDM_.*.csv$";
    // ｢ＤＭ止め区分一括更新｣用ジョブ実績出力用ログファイル名称
    String CM_G86002D = "JBch_G86002D";

    String DEL_YMD = "";
    String DEL_YM = "";
    String DEL_Y = "";
    String DEL_YMD2 = "";

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
        CM_MYPRGNAME = "ＤＭ止め区分一括更新";

        // 開始メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //# 保持期間グループ
        String DB_DELGRP = "止め区分更新";
        //###########################################
        //#  日付・時刻取得
        //###########################################
        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();

        //#  システム時刻
        String SYS_HHMMSS = DateUtil.getHHMMSS();

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //    # 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_WN;
            }
        }

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_WN;
        }
        //###########################################
        //#  ジョブ実績ログファイル
        //###########################################

        FileUtil.deleteFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G86002D + ".log");

        //# 入力ファイルチェック
        if (!FileUtil.isExistFileByRegx(getenv(C_aplcom1Service.CM_FILENOWRCV), INPUT_FILENAME)) {
            APLOG_WT("入力ファイルなし [" + INPUT_FILENAME + "]", FI);
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_OK;
        } else {
            //###########################################
            //#  ステータス初期化
            //###########################################
            int RTN_STS = 0;

            //###########################################
            //#  入力ファイル名取得
            //###########################################
            ArrayList<String> INPUT_FILENAME_DATA = FileUtil.findByRegex(getenv(CM_FILENOWRCV), INPUT_FILENAME);
            for (int i = 0; i < INPUT_FILENAME_DATA.size(); i++) {
                String filename = basename(INPUT_FILENAME_DATA.get(i));
                //       ###########################################
                //        #  出力ファイル名
                //        ###########################################
                String OUTPUT_FILE_NAME = StringUtils.substring(filename, 3);

                //###########################################
                //#  処理開始ログ
                //###########################################
                cmABaplwBServiceImpl.main(
                        getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + "対象ファイル名：" + filename).FI());

                //###########################################
                //#  プログラム実行
                //###########################################
                MainResultDto mainResultDto = CmBTdmupBImpl.main(
                        getExecuteBaseParam().add("-i" + filename).add("-o" + OUTPUT_FILE_NAME)
                                .add(args.length > 0 ? args[0] : null));
                RTN_STS = mainResultDto.exitCode;
                if (RTN_STS == Rtn_NG) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME).FI());
                    return Rtn_WN;
                }
                if (RTN_STS == Rtn_OK) {
                    //###########################################
                    //#  出力ファイル名取得（拡張子なし）
                    //###########################################
                    String OUTPUT_FILE_NAME_BASE = basename(OUTPUT_FILE_NAME) + ".csv";

                    //###########################################
                    //#  連動ファイル圧縮
                    //###########################################
                    String OUTPUT_FILE_NAME_ZIP = OUTPUT_FILE_NAME_BASE + ".zip";
                    int RTN = ZipUtil.zipFile(OUTPUT_FILE_NAME_ZIP, OUTPUT_FILE_NAME);
                    if (RTN != 0) {
                        cmABaplwBServiceImpl.main(
                                getExecuteBaseParam().P(CM_MYPRGID).M("圧縮失敗  [" + OUTPUT_FILE_NAME + "][" + RTN + "]").FE());
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                        return Rtn_WN;
                    }
                }

                //###########################################
                //#  ジョブ実績ログファイル
                //###########################################
                if (RTN_STS == Rtn_OK) {
                    int OUTPUT_FILE_CNT = FileUtil.countLines(OUTPUT_FILE_NAME);
                    OUTPUT_FILE_CNT = OUTPUT_FILE_CNT - 1;
                    FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G86002D + ".log",
                            "        " + OUTPUT_FILE_NAME + ":" + OUTPUT_FILE_CNT + "件");
                } else if (RTN_STS == Rtn_WN) {
                    FileUtil.writeFile(getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G86002D + ".log",
                            "        " + OUTPUT_FILE_NAME + ":ヘッダ項目不正のため更新なし");
                }
            }
        }
        //################################################
        //#  テンポラリファイル取得
        //################################################
        String TEMP_FILE1 = CM_APWORK_DATE + "/" + CM_MYPRGID + "01_" + PidUtil.getPid();
        String TEMP_FILE2 = CM_APWORK_DATE + "/" + CM_MYPRGID + "02_" + PidUtil.getPid();

        //###########################################
        //#  削除対象日付取得
        //###########################################
        MainResultDto mainResultDto = cmABgdldBServiceImpl.main(getExecuteBaseParam().add(DB_DELGRP));
        FileUtil.writeFile(TEMP_FILE1, mainResultDto.result);
        if (mainResultDto.exitCode != Rtn_OK) {
            APLOG_WT("データ削除日付取得エラー　TBL略称＝[" + OPTION1 + "]　テーブルGRP=" + DB_DELGRP + "　STATUS=" +
                    mainResultDto.exitCode, FE);
            return Rtn_WN;
        }
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
                FileUtil.findByRegex(getenv(CmABfuncLServiceImpl.CM_PCRENKEI_KOJINARI), "^.{14}_updateDM_*.zip");
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

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME));

        return Rtn_OK;
    }

}
