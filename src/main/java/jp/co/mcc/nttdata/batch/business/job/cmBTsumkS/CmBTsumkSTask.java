package jp.co.mcc.nttdata.batch.business.job.cmBTsumkS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
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
 * #    名称          ：  ＳＡＰ付与還元実績抽出
 * #    プログラムID  ：  cmBTsumkS
 * #
 * #    【処理概要】
 * #      HSポイント日別情報YYYYMM、HSポイント日別内訳情報YYYYMM、PS店表示情報ＭＣＣ、
 * #      WSＳＡＰ付与還元実績抽出からデータを抽出し、下記ファイルを作成する。
 * #       ①売上連動・年度ポイント付与実績
 * #       ②非売上連動・年度ポイント還元実績
 * #       ③売上連動・期間限定ポイント付与実績
 * #       ④非売上連動・期間限定ポイント還元実績
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
 * #      40.00 :  2022/09/06 SSI.本多 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTsumkSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

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
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 49;

        String ARG_OPT1 = "-DEBUG";//  ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";// ###  デバッグモードでの実行（トレース出力機能が有効）

        //#  引数定義
        // # 売上連動・年度ポイント付与実績ファイル名
        String URIAGE_NENDO_FILE_NAME = "GP00000001";

        //# 非売上連動・年度ポイント還元実績ファイル名
        String HIURIAGE_NENDO_FILE_NAME = "GP00000002";

        //# 売上連動・期間限定ポイント付与実績ファイル名
        String URIAGE_GENTEI_FILE_NAME = "GP00000005";

        //# 非売上連動・期間限定ポイント還元実績ファイル名
        String HIURIAGE_GENTEI_FILE_NAME = "GP00000006";
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
        //#  変数初期化
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
        MainResultDto cmABgdatBResult = cmABgdatB.main(getExecuteBaseParam());
        int RTN = cmABgdatBResult.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String BAT_YYYYMMDD = cmABgdatBResult.result;
        String BAT_YYYYMM = BAT_YYYYMMDD.substring(0, 6);

        //###########################################
        //#  バッチ処理日付(前月)の取得
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM", BAT_YYYYMM)
                .execute();
        String BAT_YYYYMM_1 = shellExecuteDto.getResultData().substring(0, 6);
        //###########################################
        //#  バッチ処理日付(前月)の取得
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM_1", BAT_YYYYMM_1)
                .execute();

        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + BAT_YYYYMM_1 + "_sumk.log");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("WSＳＡＰ付与還元実績抽出の作成に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //#  WSＳＡＰ付与還元実績抽出（当月分）
        //###########################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .addEvn("BAT_YYYYMM", BAT_YYYYMM)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + BAT_YYYYMM + "_sumk.log");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("WSＳＡＰ付与還元実績抽出（当月分）の作成に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //##############################################
        //#  売上連動・年度ポイント付与実績作成情報処理
        //##############################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_04")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/KK00000001.lst");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("売上連動・年度ポイント付与実績作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //################################################
        //#  非売上連動・年度ポイント還元実績作成情報処理
        //################################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_05")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/KK00000002.lst");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("非売上連動・年度ポイント還元実績作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //##################################################
        //#  売上連動・期間限定ポイント付与実績作成情報処理
        //##################################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_06")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/KK00000003.lst");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("売上連動・期間限定ポイント付与実績作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //##################################################
        //#  非売上連動・期間限定ポイント還元実績作成情報処理
        //##################################################
        shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_07")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD)
                .execute();
        if (shellExecuteDto.RTN0()) {
            String SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/KK00000004.lst");
            cmABaplwB.main(getExecuteBaseParam().P("cmBTsumkS").M("非売上連動・期間限定ポイント還元実績作成情報処理に失敗しました。SQLCODE=" + SQL_CD).FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_08")
                .defaultEvn(this)
                .execute();

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }
}
