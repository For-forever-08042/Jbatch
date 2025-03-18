package jp.co.mcc.nttdata.batch.business.job.cmBTspptS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTdlemB.CmBTdlemBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  ポイント付与実績作成
 * #    プログラムID  ：  cmBTspptS
 * #
 * #    【処理概要】
 * #      MSカード情報、HSポイント日別情報YYYYMM、HSポイント日別内訳情報YYYYMMから
 * #      データを抽出しポイント付与実績作成ファイルを作成する。
 * #
 * #    【引数説明】
 * #       -DEBUG or -debug  : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 7
 * #
 * #    改定履歴
 * #      40.00 :  2022/12/08 SSI.山口 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTspptSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    CmABfzipSTask cmABfzipS;
    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTdlemBServiceImpl cmBTdlemB;
    @Autowired
    CmABgdldBServiceImpl cmABgdldB;

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


        //#  引数定義
        String ARG_OPT1 = "-DEBUG";//    ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";//    ###  デバッグモードでの実行（トレース出力機能が有効）


        //# 出力ファイル名
        String OUTPUT_FILE = "KKMI0110";

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
        if (args.length > 1) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        //###########################################
        //##  引数格納変数初期化
        //###########################################


        //#引数格納変数初期化
        String OPTION1 = "";
        for (String ARG_VALUE : args) {
            if (StringUtils.equals(ARG_VALUE, ARG_OPT1) || StringUtils.equals(ARG_VALUE, ARG_OPT2)) {
                OPTION1 = ARG_VALUE;
            } else {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  バッチ処理日付取得
        //###########################################
        //# 当日日付取得
        MainResultDto cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam().add("-DY"));
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        String BAT_YYYYMM = null;
        if (StringUtils.isEmpty(BAT_YYYYMMDD) || BAT_YYYYMMDD.length() < 6) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付年月値の取得に失敗しました").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        BAT_YYYYMM = BAT_YYYYMMDD.substring(0, 6);

        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM", BAT_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv");
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント付与実績作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  出力ファイル存在確認
        //###########################################
        if (!FileUtil.isExistFile(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv")) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮対象ファイルなし [" + CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv" +
                    "]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  処理件数取得
        //###########################################
        Integer DATA_COUNT = FileUtil.countLines(CM_APWORK_DATE + "/" + OUTPUT_FILE + ".csv");
        if (DATA_COUNT == null) {
            //# 処理件数取得失敗
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理件数取得エラー").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  処理件数出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ファイル名：[" + OUTPUT_FILE + ".csv]  出力件数：[" + DATA_COUNT + "]").FI());

        //# 実行結果出力ファイルを圧縮する
        RTN = cmABfzipS.main(
                getExecuteBaseParam().add("-O").add(CM_APWORK_DATE).add("-Z").add(OUTPUT_FILE + ".zip").add("-D")
                        .add(CM_APWORK_DATE).add("-I").add(OUTPUT_FILE + ".csv").add("-DEL"));
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + OUTPUT_FILE + ".zip)作成に失敗しました。").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
