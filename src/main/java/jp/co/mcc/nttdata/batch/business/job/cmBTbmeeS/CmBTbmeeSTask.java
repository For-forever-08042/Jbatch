package jp.co.mcc.nttdata.batch.business.job.cmBTbmeeS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTdlemB.CmBTdlemBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  売上明細取込（MK-EC）
 * #    プログラムID  ：  cmBTbmeeS
 * #
 * #    【処理概要】
 * #        EC取引ログを入力とし、xmlデータを作成し、作成したxmlデータをロギングクラスへ送信する処理
 * #        「バッチインプット-売上明細取込（MK-EC）（cmBTbmeeJ）　」を起動するためのシェル。
 * #        開始メッセージを出力し、パラメータファイルパス取得、「cmBTbmeeB」を起動、
 * #        終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug   :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6.1
 * #
 * #    改定履歴
 * #      40.00 : 2022/10/31 SSI.畑本：MCCM 初版
 * #      41.00 : 2023/07/19 SSI.畑本：MCCMPH2
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTbmeeSTask extends NtBasicTask {

    @Autowired
    CmABdbtrSTask cmABdbtrS;
    @Autowired
    CmABdbl2STask cmABdbl2S;
    @Autowired
    CmABfzipSTask cmABfzipS;
    @Autowired
    CmABaplwBServiceImpl cmABaplwB;

    @Autowired
    CmABgprmBServiceImpl cmABgprmB;
    @Autowired
    CmABgdatBServiceImpl cmABgdatB;

    @Autowired
    CmBTdlemBServiceImpl cmBTdlemB;
    @Autowired
    CmABgdldBServiceImpl cmABgdldB;
    //# 取込対象のファイル
    String[] TARGET_FILE_NAMES = {"dekk_use_point_log.csv",
            "DEKK0030.zip",
            "dekk_buy_price_log.csv",
            "dekk_grant_point.csv",
            "DEKK0020.zip"};

    //# 解凍対象のファイル
    String[] UNZIPPING_FILE_NAMES = {"DEKK0020.zip",
            "DEKK0030.zip"};
    //# 解凍して作られたファイル
    String[] UNZIPPED_FILE_NAMES = {"EcPointCalcData.dat",
            "EcCouponData.dat",
            "EcPointRecovData.dat",
            "EcCouponRecovData.dat"};

    String DB_KBN = "SD";
    String TRUNTBL1 = "WSＭＫＥポイント利用";
    String TRUNTBL2 = "WSＭＫＥポイント計算";
    String TRUNTBL3 = "WSＭＫＥＰクーポン利用";
    String TRUNTBL4 = "WSＭＫＥ購買金額積上";
    String TRUNTBL5 = "WSＭＫＥポイント付与";
    String TRUNTBL6 = "WSＭＫＥ再計算注文";
    String TRUNTBL7 = "WSＭＫＥ再計算Ｐクーポン利用";
    String CTLFile1 = "ldrWSMKEPointRiyo";
    String CTLFile2 = "ldrWSMKEPointKeisan";
    String CTLFile3 = "ldrWSMKEPCouponRiyo";
    String CTLFile4 = "ldrWSMKEKoubaiKingakuTsumiage";
    String CTLFile5 = "ldrWSMKEPointFuyo";
    String CTLFile6 = "ldrWSMKESaikeisanChumon";
    String CTLFile7 = "ldrWSMKESaikeisanPCouponRiyo";
    String CTLFileName1 = "ldrWSMKEPointRiyo.sql";
    String CTLFileName2 = "ldrWSMKEPointKeisan.sql";
    String CTLFileName3 = "ldrWSMKEPCouponRiyo.sql";
    String CTLFileName4 = "ldrWSMKEKoubaiKingakuTsumiage.sql";
    String CTLFileName5 = "ldrWSMKEPointFuyo.sql";
    String CTLFileName6 = "ldrWSMKESaikeisanChumon.sql";
    String CTLFileName7 = "ldrWSMKESaikeisanPCouponRiyo.sql";

    String EUC_LOGFileName1 = "dekk_use_point_log.csv";
    String EUC_LOGFileName2 = "EcPointCalcData.dat";
    String EUC_LOGFileName3 = "EcCouponData.dat";
    String EUC_LOGFileName4 = "dekk_buy_price_log.csv";
    String EUC_LOGFileName5 = "dekk_grant_point.csv";
    String EUC_LOGFileName6 = "EcPointRecovData.dat";
    String EUC_LOGFileName7 = "EcCouponRecovData.dat";
    String SJIS_LOGFileName1 = "dekk_use_point_log_bmee_sjis.dat";
    String SJIS_LOGFileName2 = "EcPointCalcData_bmee_sjis.dat";
    String SJIS_LOGFileName3 = "EcCouponData_bmee_sjis.dat";
    String SJIS_LOGFileName4 = "dekk_buy_price_log_bmee_sjis.dat";
    String SJIS_LOGFileName5 = "dekk_grant_point_bmee_sjis.dat";
    String SJIS_LOGFileName6 = "EcPointRecovData_bmee_sjis.dat";
    String SJIS_LOGFileName7 = "EcCouponRecovData_bmee_sjis.dat";
    String GOOPON_LOGFileName1 = "GOKK0040.tsv";

    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().S(CM_MYPRGNAME));


        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        Rtn_OK = 10;
        Rtn_NG = 99;

        String ARG_OPT1 = "-DEBUG";//  ###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";// ###  デバッグモードでの実行（トレース出力機能が有効）


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

        //#  引数格納変数初期化
        String OPTION1 = "";
        //#  引数格納
        for (String ARG_VALUE : args) {
            if (StringUtils.equals(ARG_VALUE, ARG_OPT1) || StringUtils.equals(ARG_VALUE, ARG_OPT2)) {
                OPTION1 = "ON";
            }
        }
        //###########################################
        //#  パラメータファイル名の取得
        //###########################################
        MainResultDto resultDto = cmABgprmB.main(getExecuteBaseParam().add("cmBTbmeeP"));
        if (Rtn_OK != resultDto.exitCode) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("パラメータファイルが存在しません。[cmBTbmeeP]").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        String ARG_BMEE_PRMFILE = resultDto.result;


        //###########################################
        //#  sqlファイル存在チェック
        //###########################################
        String CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName1;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName1 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName2;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName2 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName3;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName3 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName4;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName5;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName5 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName6;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName6 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName7;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName7 + "ファイルが存在しません").FE());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //#  移動前対象ファイルの存在チェック
        //###########################################
        for (String W_FILE_NAME : TARGET_FILE_NAMES) {
            String RCV_FILE_NAME = CM_FILENOWRCV + "/" + W_FILE_NAME;
            Integer FILE_CNT1 = FileUtil.countLines(RCV_FILE_NAME);
            if (FILE_CNT1 == null || FILE_CNT1 < 1) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(RCV_FILE_NAME + " ファイルが存在しません").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  入力データファイルを作業フォルダへ移動
        //###########################################
        for (String W_FILE_NAME : TARGET_FILE_NAMES) {
            int RTN = FileUtil.mvFile(CM_FILENOWRCV + "/" + W_FILE_NAME, CM_APWORK_DATE + "/" + W_FILE_NAME);
            if (RTN != 0) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("入力データファイル移動エラー　ファイル名=" + W_FILE_NAME + "　STATUS=1").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  移動後対象ファイルの存在チェック
        //###########################################
        for (String W_FILE_NAME : TARGET_FILE_NAMES) {
            String DST_FILE_NAME = CM_APWORK_DATE + "/" + W_FILE_NAME;
            Integer FILE_CNT2 = FileUtil.countLines(DST_FILE_NAME);
            if (FILE_CNT2 == null || FILE_CNT2 < 1) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(DST_FILE_NAME + " ファイルが存在しません").FE());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }
        int RNT = 0;
        //###########################################
        //#  解凍
        //###########################################
        for (String W_FILE_NAME : UNZIPPING_FILE_NAMES) {
            RNT = ZipUtil.unzip(CM_APWORK_DATE + "/" + W_FILE_NAME, CM_APWORK_DATE + "/");
            if (RNT != 0) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + W_FILE_NAME + "のzip" +
                        "解凍に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }

        //###########################################
        //#  文字コード変換
        //###########################################
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName1,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName1);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName1 + "のSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }


        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName2,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName2);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName2 + "のSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName3,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName3);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName3 + "のSJIS変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName4,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName4);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName4 + "のSJIS" +
                    "変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName5,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName5);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName5 + "のSJIS" +
                    "変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName6,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName6);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName6 + "のSJIS" +
                    "変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = IconvUtil.main(SystemConstant.EUC_JP, SystemConstant.Shift_JIS,
                CM_APWORK_DATE + "/" + EUC_LOGFileName7,
                CM_APWORK_DATE + "/" + SJIS_LOGFileName7);
        if (RNT != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + EUC_LOGFileName7 + "のSJIS" +
                    "変換に失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        //###########################################
        //#  テーブルTruncate
        //###########################################
        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL1).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL1 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }
        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL2).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL2 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL3).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL3 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL4).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL4 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL5).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL5 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL6).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL6 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        RNT = cmABdbtrS.main(getExecuteBaseParam().sT().add(TRUNTBL7).sD().add(DB_KBN));
        if (RNT != Rtn_OK) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL7 + "のTruncateに失敗しました").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        //###########################################
        //#  COPYの実行
        //###########################################
        long SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName1);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント利用ログのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile1));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName2);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント計算データのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile2));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName3);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイントクーポン利用実績データのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile3));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName4);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買金額積上ログのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile4));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName5);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント付与データのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile5));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName6);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント再計算用注文実績データのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile6));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        SIZE = FileUtil.contentLength(CM_APWORK_DATE + "/" + SJIS_LOGFileName7);
        if (SIZE != 0) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("再計算用ポイントクーポン利用実績データのデータロード"));
            RNT = cmABdbl2S.main(getExecuteBaseParam().sD().add(DB_KBN).sC().add(CTLFile7));
            if (RNT != Rtn_OK) {
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M("COPYの実行に失敗しました").FW());
                cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                DELETE_UNZIPPED_FILES();
                COPY_ERR_FILE();
                return Rtn_NG;
            }
        }
        //###########################################
        //#  プログラム実行
        //###########################################
        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID)
                .addEvn("ARG_BMEE_PRMFILE", ARG_BMEE_PRMFILE)
                .addEvn("OPTION1", OPTION1)
                .addEvn("CM_JAVA_APBIN", CM_JAVA_APBIN)
                .addEvn("CLASSPATH", CLASSPATH).execute();

        if (shellExecuteDto.RTN0()) {
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + " 異常終了").FW());
            cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            DELETE_LOAD_DATA();
            DELETE_UNZIPPED_FILES();
            COPY_ERR_FILE();
            return Rtn_NG;
        }

        //###########################################
        //#  処理売上明細ファイルを処理中ディレクトリへ戻す
        //###########################################
        for (String W_FILE_NAME : TARGET_FILE_NAMES) {
            FileUtil.mvFile(CM_APWORK_DATE + "/" + W_FILE_NAME, CM_FILENOWRCV + "/" + W_FILE_NAME);
        }

        //###########################################
        //#  COPY用ファイルの削除
        //###########################################
        DELETE_LOAD_DATA();
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwB.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }


    //###########################################
    //#  COPY用ファイルの削除関数
    //###########################################
    public void DELETE_LOAD_DATA() {
        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^.*_bmee_sjis.dat");
    }

    //###########################################
    //#  エラー発生時のファイル退避関数
    //###########################################
    public void COPY_ERR_FILE() {
        String ERROR_DIR = CM_APWORK_DATE + "/ERROR"  ;
        if (!FileUtil.isExistDir(ERROR_DIR)) {
            FileUtil.mkdir(ERROR_DIR);
        }

        for (String W_FILE_NAME : TARGET_FILE_NAMES) {
            // ###########################################
            //        #  処理売上明細ファイルをエラーフォルダに退避
            //        ###########################################
            FileUtil.copyFile(CM_APWORK_DATE + "/" + W_FILE_NAME, ERROR_DIR + "/" + W_FILE_NAME);

            //        ###########################################
            //        #  CRMへ連携した処理売上明細ファイルを削除する
            //        ###########################################
            FileUtil.deleteFile(CM_CRMRENKEI + "/" + W_FILE_NAME);

            //        ###########################################
            //        #  処理売上明細ファイルを処理中ディレクトリへ戻す
            //        ###########################################
            FileUtil.mvFile(CM_APWORK_DATE + "/" + W_FILE_NAME, CM_FILENOWRCV + "/" + W_FILE_NAME);
        }
    }

    //###########################################
    //#  解凍後ファイルの削除関数
    //###########################################
    public void DELETE_UNZIPPED_FILES() {
        for (String W_FILE_NAME : UNZIPPED_FILE_NAMES) {
            FileUtil.deleteFile(CM_APWORK_DATE + "/" + W_FILE_NAME);
        }
    }


}
