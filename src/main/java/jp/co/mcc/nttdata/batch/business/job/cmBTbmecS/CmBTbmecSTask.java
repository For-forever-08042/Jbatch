package jp.co.mcc.nttdata.batch.business.job.cmBTbmecS;

import jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbl2S.CmABdbl2STask;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  売上明細取込
 * #    プログラムID  ：  cmBTbmecS
 * #
 * #    【処理概要】
 * #        POS明細データファイルを入力とし、xmlデータを作成し、作成した
 * #        xmlデータをロギングクラスへ送信する処理「売上明細取込（cmBTbmecB）」
 * #        を起動するためのシェル。
 * #        開始メッセージを出力し、パラメータファイルパス取得、「cmBTbmecB」を起動、
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
 * #      1.00 : 2012/11/04 SSI.本田：初版
 * #      2.00 : 2013/05/30 SSI.本田：EC売上明細ファイルの処理を追加
 * #      3.00 : 2014/05/27 SSI.上野：Javaヒープサイズ指定を追加
 * #      4.00 : 2014/08/14 SSI.吉田：売上明細ファイルを１ファイルずつ処理するよう変更
 * #      5.00 : 2015/11/27 SSI.上野：(クーポンシリアル化対応)以下のファイルを追加
 * #                                 取引顧客明細ログ(シリアル)、
 * #                                 取引クーポンログ(シリアル)、取引サービス券ログ
 * #      6.00 : 2015/11/27 SSI.武藤：(POS更改に伴う対応)
 * #      7.00 : 2020/12/04 SSI.上野：(期間限定ポイント対応)以下のファイルを追加
 * #                                 取引顧客ログ(期間限定)、
 * #                                 取引顧客明細ログ(期間限定)
 * #      8.00 : 2021/07/28 SSI.上野：(DM合流対応)以下のファイルを追加
 * #                                 取引クーポンログ(DM)、
 * #                                 取引顧客明細ログ(DM)
 * #      9.00 : 2022/03/07 SSI.薗田：(倍デーポイントの期間限定対応)以下のファイルを追加
 * #                                 取引顧客ログ(期間限定倍デー)
 * #     40.00 : 2022/10/07 SSI.瀬尾：MCCM初版
 * #                                  機能IDをbmei⇒bmecに置換
 * #                                  集信ファイル名を(HC011⇒HC013)に置換
 * #                                  ポイントカテゴリ明細ログファイル取込を追加
 * #                                  取込ファイル名置換
 * #                                    (新３)取引顧客明細ログ(S4114⇒S4143)
 * #                                    (新２)取引顧客ログ(S4115⇒S4141)
 * #                                  GOOPON連携ファイル（カード再発行データ）出力を追加
 * #      41.00 : 2023/05/10 SSI.畑本：MCCMPH2
 * #                                  GOOPON連携ファイル（カード再発行データ）出力を削除
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTbmecSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    private CmABdbl2STask cmABdbl2STask;

    @Autowired
    private CmABgprmBServiceImpl cmABgprmBServiceImpl;

    //###########################################
    //#  定数定義
    //###########################################

    //#  戻り値
    int Rtn_OK = 10;
    int Rtn_NG = 49;

    //#  引数定義
    //#ARG_OPT1="-i"           ###  バッチインプット入力データファイル名
    //#ARG_OPT2="-o"           ###  出力エラーファイル名
    //#ARG_OPT3="-c"           ###  企業指定文字列（SE、SO）
    public static final String ARG_OPT1 = "-DEBUG";        // ###  デバッグモードでの実行（トレース出力機能が有効）
    public static final String ARG_OPT2 = "-debug";        // ###  デバッグモードでの実行（トレース出力機能が有効）

    int SLEEP_TIME = 60;

    String DB_KBN = "SD";
    String TRUNTBL1 = "WS取引ワーク";
    String TRUNTBL2 = "WS取引明細ワーク";
    String TRUNTBL3 = "WS取引顧客ワーク";
    String TRUNTBL4 = "WS取引顧客明細ワーク";
    String TRUNTBL5 = "WS取引クーポンワーク";
    String TRUNTBL6 = "WS取引顧客登録ワーク";
    String CTLFile1 = "ldrWSTorihiki";
    String CTLFile2 = "ldrWSTorihikiMeisai";
    String CTLFile3 = "ldrWSTorihikiKokyaku";
    String CTLFile4 = "ldrWSTorihikiKokyakuMeisai";
    String CTLFile5 = "ldrWSTorihikiCoupon";
    String CTLFile6 = "ldrWSTorihikiKokyakuTouroku";
    String CTLFileName1 = "ldrWSTorihiki.sql";
    String CTLFileName2 = "ldrWSTorihikiMeisai.sql";
    String CTLFileName3 = "ldrWSTorihikiKokyaku.sql";
    String CTLFileName4 = "ldrWSTorihikiKokyakuMeisai.sql";
    String CTLFileName5 = "ldrWSTorihikiCoupon.sql";
    String CTLFileName6 = "ldrWSTorihikiKokyakuTouroku.sql";
    String CRMRenkeiCtlFileName = "HC0110.sql";

    //    # クーポンシリアル化
    String TRUNTBL7 = "WS取引サービス券ワーク";
    String CTLFile7 = "ldrWSTorihikiServiceTicket";
    String CTLFileName7 = "ldrWSTorihikiServiceTicket.sql";
    String CTLFile4_Serial = "ldrWSTorihikiKokyakuMeisaiSerial";
    String CTLFile5_Serial = "ldrWSTorihikiCouponSerial";
    String CTLFileName4_Serial = "ldrWSTorihikiKokyakuMeisaiSerial.sql";
    String CTLFileName5_Serial = "ldrWSTorihikiCouponSerial.sql";

    //    # 2020/12/04 add 期間限定ポイント対応
    String CTLFile3_Kikangentei = "ldrWSTorihikiKokyakuKikangentei";
    String CTLFile4_Kikangentei = "ldrWSTorihikiKokyakuMeisaiKikangentei";
    String CTLFileName3_Kikangentei = "ldrWSTorihikiKokyakuKikangentei.sql";
    String CTLFileName4_Kikangentei = "ldrWSTorihikiKokyakuMeisaiKikangentei.sql";

    //    # 2021/07/28 add DM合流対応
    //    # 2022/10/31 MCCM初版 MOD START
    //    #CTLFile4_DM="ldrWSTorihikiKokyakuMeisaiDM"
    //    #CTLFileName4_DM="ldrWSTorihikiKokyakuMeisaiDM.ctl"
    String CTLFile4_DM = "ldrWSTorihikiKokyakuMeisaiDMBmec";
    String CTLFileName4_DM = "ldrWSTorihikiKokyakuMeisaiDMBmec.sql";
    //    # 2022/10/31 MCCM初版 MOD END
    String CTLFile5_DM = "ldrWSTorihikiCouponDM";
    String CTLFileName5_DM = "ldrWSTorihikiCouponDM.sql";

    //    # 2022/03/07 add 倍デーポイントの期間限定ポイント対応
    //    # 2022/10/31 MCCM初版 MOD START
    //    #CTLFile3_KikangenteiBaiDay="ldrWSTorihikiKokyakuKikangenteiBaiDay"
    //    #CTLFileName3_KikangenteiBaiDay="ldrWSTorihikiKokyakuKikangenteiBaiDay.ctl"
    String CTLFile3_KikangenteiBaiDay = "ldrWSTorihikiKokyakuKikangenteiBaiDayBmec";
    String CTLFileName3_KikangenteiBaiDay = "ldrWSTorihikiKokyakuKikangenteiBaiDayBmec.sql";
    //    # 2022/10/31 MCCM初版 MOD END

    //    # 2022/10/07 MCCM初版 ADD START
    String TRUNTBL4_A = "WS取引ポイント明細ワーク";
    String CTLFile4_A = "ldrWSTorihikiPointMeisai";
    String CTLFileName4_A = "ldrWSTorihikiPointMeisai.sql";
    //    # 2022/10/07 MCCM初版 ADD END

    //    #  引数格納変数初期化
    String OPTION1 = "";
    //    #OPTION2=""
    //    #OPTION3=""
    //    #OPTION4=""
    String ARG_bmec_PRMFILE = "";

    String FILE_NAME2 = "";

    String CM_APWORK_DATE;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################

        CM_MYPRGNAME = "売上明細取込";
        setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGID);

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = CM_APWORK + "/" + DateUtil.nowDateFormat("yyyyMMdd");
            setenv(CmABfuncLServiceImpl.CM_APWORK_DATE, CM_APWORK_DATE);
        }

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                // 作業ディレクトリファイル作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //#------------------------------------------
        //#  多重起動チェック
        //#  前回実行の明細取込処理終了までSLEEP
        //#------------------------------------------

        ShellExecuteDto shellExecuteDto = ShellClientManager
                .getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .addEvn(CM_FILENOWRCV, CM_FILENOWRCV)
                .addEvn(CM_APWORK, CM_APWORK)
                .addEvn("CM_MYPRGID", CM_MYPRGID)
                .addEvn("SLEEP_TIME", String.valueOf(SLEEP_TIME)).execute();

        //    # 2023/05/10 MCCMPH2 MOD END
        //    # 2022/10/07 MCCM初版 MOD END
        int RTN = 0;
        // int RTN = StringUtils.isEmpty(shellExecuteDto.getError()) ? 0 : Integer.valueOf(shellExecuteDto.getError());

        //###########################################
        //#  引数の数チェック
        //###########################################
        //#  引数格納
        for (String arg : args) {
            if (ARG_OPT1.equals(arg) || ARG_OPT2.equals(arg)) {
                OPTION1 = "ON";
                break;
            }
        }

        //###########################################
        //#  sqlファイル存在チェック
        //###########################################
        String CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName1;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName1 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL + "/loader/" + CTLFileName2;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName2 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName3;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName3 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE =CM_APSQL + "/loader/" + CTLFileName4;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2022/10/07 MCCM初版 ADD START
        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName4_A;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4_A + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2022/10/07 MCCM初版 ADD END
        CHECK_FILE =CM_APSQL + "/loader/" + CTLFileName5;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName5 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName6;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName6 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# クーポンシリアル化
        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName4_Serial;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4_Serial + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName5_Serial;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName5_Serial + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName7;
        //#if test -f ${CHECK_FILE}
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            //#cmABplodB cmABaplwB -P${CM_MYPRGID} -M"ctlファイルチェックOK"
            //#else
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName7 + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2020/12/04 add 期間限定ポイント対応
        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName3_Kikangentei;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName3_Kikangentei + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName4_Kikangentei;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4_Kikangentei + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2021/07/28 add DM合流対応
        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName4_DM;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName4_DM + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName5_DM;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName5_DM + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2022/03/07 add 倍デーポイントの期間限定ポイント対応
        CHECK_FILE = CM_APSQL+ "/loader/" + CTLFileName3_KikangenteiBaiDay;
        if (!FileUtil.isExistFile(CHECK_FILE)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CTLFileName3_KikangenteiBaiDay + "ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  売上明細ファイルをCRMへ連携
        //###########################################
        //#find ${CM_FILENOWRCV} -name "*_HC0110.zip" -exec cp -p {} ${CM_CRMRENKEI} \; > /dev/null 2>&1
        //#echo `date +%Y%m%d` > ${CM_CRMRENKEI}/${CRMRenkeiCtlFileName}

        //###########################################
        //#  対象ファイルの存在チェック
        //###########################################

        List<String> RCV_FILE_NAME = FileUtil.findByRegex(CM_FILENOWRCV, "^(.)*_HC013(.)*.zip$");
        int FILE_CNT1 = FileUtil.countLines(RCV_FILE_NAME);
        if (FILE_CNT1 < 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_FILENOWRCV + "/*_HC013*.zip ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  売上明細ファイルを作業フォルダへ移動
        //###########################################

        for (String file : RCV_FILE_NAME) {
            int STATUS = FileUtil.mvFile(file, CM_APWORK_DATE + "/" + new File(file).getName());
            if (STATUS != 0) {
                //cmABplodB cmABaplwB -P${CM_MYPRGID} -M"結果ファイル移動エラー　ファイル名=${FILE_NAME2}　STATUS=${RTN}" -FE
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("結果ファイル移動エラー　ファイル名=" + FILE_NAME2 + " STATUS=1").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //###########################################
        //#  対象ファイルの存在チェック
        //###########################################

        List<String> DST_FILE_NAME = FileUtil.findByRegex(CM_APWORK_DATE, "^(.)*_HC013(.)*.zip$");
        int FILE_CNT2 = FileUtil.countLines(DST_FILE_NAME);
        if (FILE_CNT2 < 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/*_HC013*.zip ファイルが存在しません").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //# 2023/05/10 MCCMPH2 DEL START
        //## 2022/10/07 MCCM初版 ADD START
        //############################################
        //##  GOOPON連携ファイルの空ファイルを生成
        //############################################
        //#GCS_FILE_WORK=${CM_APWORK_DATE}/MCC_PT_006_bmec.tsv
        //#touch ${GCS_FILE_WORK}
        //## 2022/10/07 MCCM初版 ADD END
        //# 2023/05/10 MCCMPH2 DEL END

        //###########################################
        //#  1圧縮ファイル毎に処理実行
        //###########################################
        for (String FILE_NAME : DST_FILE_NAME) {
            //        ###########################################
            //        #  対象ファイル名取得
            //        ###########################################
            String W_FILE_NAME = FILE_NAME;
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + " 対象ファイル名：" + new File(W_FILE_NAME).getName()).FI());

            //    ###########################################
            //    #  解凍
            //    ###########################################

            try {
                RTN = FileUtil.unzip(new ZipFile(W_FILE_NAME), CM_APWORK_DATE);
                if (RTN != 0) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(W_FILE_NAME + "のzip解凍に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            } catch (IOException e) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(W_FILE_NAME + "のzip解凍に失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //    ################################# 2018/03/30 削除 [START]
            //    #    find ${CM_APWORK_DATE} -name "CS011*.zip" -exec unzip -o {} -d ${CM_APWORK_DATE} \; > /dev/null 2>&1
            //    ################################# 2018/03/30 削除 [END]
            //        ###########################################
            //        #  売上明細ファイルの結合
            //        ###########################################

            List<String> s4101List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4101(.)*.csv$");
            for (String s : s4101List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4101.tmp",true);
            }

            List<String> s4102List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4102(.)*.csv$");
            for (String s : s4102List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4102.tmp",true);
            }

            List<String> s4109List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4109(.)*.csv$");
            for (String s : s4109List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4109.tmp",true);
            }

            List<String> s4110List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4110(.)*.csv$");
            for (String s : s4110List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4110.tmp",true);
            }

            List<String> s4120List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4120(.)*.csv$");
            for (String s : s4120List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4120.tmp",true);
            }

            List<String> s4130List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4130(.)*.csv$");
            for (String s : s4130List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4130.tmp",true);
            }

            //      # クーポンシリアル化
            List<String> s4111List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4111(.)*.csv$");
            for (String s : s4111List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4111.tmp",true);
            }

            List<String> s4121List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4121(.)*.csv$");
            for (String s : s4121List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4121.tmp",true);
            }

            List<String> s4140List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4140(.)*.csv$");
            for (String s : s4140List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4140.tmp",true);
            }

            //     # 2020/12/04 add 期間限定ポイント対応
            List<String> s4112List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4112(.)*.csv$");
            for (String s : s4112List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4112.tmp",true);
            }

            List<String> s4113List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4113(.)*.csv$");
            for (String s : s4113List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4113.tmp",true);
            }

            //    # 2021/07/28 add DM合流対応
            List<String> s4143List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4143(.)*.csv$");
            for (String s : s4143List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4143.tmp",true);
            }

            List<String> s4122List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4122(.)*.csv$");
            for (String s : s4122List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4122.tmp",true);
            }

            //    # 2022/03/07 add 倍デーポイントの期間限定ポイント対応
            List<String> s4141List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4141(.)*.csv$");
            for (String s : s4141List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4141.tmp",true);
            }

            //    # 2022/10/07 MCCM初版 ADD START

            List<String> s4142List = FileUtil.findByRegex(CM_APWORK_DATE, "^S4142(.)*.csv$");
            for (String s : s4142List) {
                FileUtil.copyFile(s, CM_APWORK_DATE + "/S4142.tmp",true);
            }

            //    # 2022/10/07 MCCM初版 ADD END

            //    ###########################################
            //    #  改行コードからCRを削除
            //    ###########################################
            String s4101Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4101.tmp");
            if (StringUtils.isNotBlank(s4101Tmp)) {
                String s4101Dat = s4101Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4101.dat", s4101Dat, SystemConstant.UTF8);
            }
            String s4102Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4102.tmp");
            if (StringUtils.isNotBlank(s4102Tmp)) {
                String s4102Dat = s4102Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4102.dat", s4102Dat, SystemConstant.UTF8);
            }
            String s4109Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4109.tmp");
            if (StringUtils.isNotBlank(s4109Tmp)) {
                String s4109Dat = s4109Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4109.dat", s4109Dat, SystemConstant.UTF8);
            }
            String s4110Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4110.tmp");
            if (StringUtils.isNotBlank(s4110Tmp)) {
                String s4110Dat = s4110Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4110.dat", s4110Dat, SystemConstant.UTF8);
            }
            String s4120Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4120.tmp");
            if (StringUtils.isNotBlank(s4120Tmp)) {
                String s4120Dat = s4120Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4120.dat", s4120Dat, SystemConstant.UTF8);
            }
            String s4130Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4130.tmp");
            if (StringUtils.isNotBlank(s4130Tmp)) {
                String s4130Dat = s4130Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4130.dat", s4130Dat, SystemConstant.UTF8);
            }

            //    # クーポンシリアル化
            String s4111Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4111.tmp");
            if (StringUtils.isNotBlank(s4111Tmp)) {
                String s4111Dat = s4111Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4111.dat", s4111Dat, SystemConstant.UTF8);
            }
            String s4121Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4121.tmp");
            if (StringUtils.isNotBlank(s4121Tmp)) {
                String s4121Dat = s4121Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4121.dat", s4121Dat, SystemConstant.UTF8);
            }
            String s4140Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4140.tmp");
            if (StringUtils.isNotBlank(s4140Tmp)) {
                String s4140Dat = s4140Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4140.dat", s4140Dat, SystemConstant.UTF8);
            }

            //    # 2020/12/04 add 期間限定ポイント対応
            String s4112Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4112.tmp");
            if (StringUtils.isNotBlank(s4112Tmp)) {
                String s4112Dat = s4112Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4112.dat", s4112Dat, SystemConstant.UTF8);
            }

            String s4113Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4113.tmp");
            if (StringUtils.isNotBlank(s4113Tmp)) {
                String s4113Dat = s4113Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4113.dat", s4113Dat, SystemConstant.UTF8);
            }

            //    # 2021/07/28 add DM合流対応
            String s4143Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4143.tmp");
            if (StringUtils.isNotBlank(s4143Tmp)) {
                String s4143Dat = s4143Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4143.dat", s4143Dat, SystemConstant.UTF8);
            }
            String s4122Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4122.tmp");
            if (StringUtils.isNotBlank(s4122Tmp)) {
                String s4122Dat = s4122Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4122.dat", s4122Dat, SystemConstant.UTF8);
            }

            //    # 2022/03/07 add 倍デーポイントの期間限定ポイント対応
            String s4141Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4141.tmp");
            if (StringUtils.isNotBlank(s4141Tmp)) {
                String s4141Dat = s4141Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4141.dat", s4141Dat, SystemConstant.UTF8);
            }

            //    # 2022/10/07 MCCM初版 ADD START
            String s4142Tmp = FileUtil.readFileByShiftJis(CM_APWORK_DATE + "/S4142.tmp");
            if (StringUtils.isNotBlank(s4142Tmp)) {
                String s4142Dat = s4142Tmp.replace("\r", "");
                FileUtil.writeFile(CM_APWORK_DATE + "/S4142.dat", s4142Dat, SystemConstant.UTF8);
            }

            //    # 2022/10/07 MCCM初版 ADD END

            //     ###########################################
            //     #  テーブルTruncate
            //     ###########################################

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL1).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL1 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL2).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL2 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL3).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL3 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL4).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL4 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //      # 2022/10/07 MCCM初版 ADD START
            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL4_A).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL4_A + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }
            //     # 2022/10/07 MCCM初版 ADD END

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL5).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL5 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL6).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL6 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //    # クーポンシリアル化
            RTN = cmABdbtrSTask.main(getExecuteBaseParam().add("-T").add(TRUNTBL7).add("-D").add(DB_KBN));
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(TRUNTBL7 + "のTrncateに失敗しました").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //     ###########################################
            //     #  COPYの実行
            //     ###########################################
            int SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4101.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引ログのデータロード"));

                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile1));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4102.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引明細ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile2));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4109.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile3));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4110.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客明細ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile4));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //       # 2022/10/07 MCCM初版 ADD START
            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4142.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイントカテゴリ明細ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile4_A).doInput());
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //     # 2022/10/07 MCCM初版 ADD END
            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4120.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引クーポンログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile5));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4130.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客登録ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile6));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //        # クーポンシリアル化
            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4111.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客明細ログ(シリアル)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile4_Serial));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4121.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引クーポンログ(シリアル)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile5_Serial));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4140.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引サービス券ログのデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile7));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //    # 2020/12/04 add 期間限定ポイント対応

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4112.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客ログ(期間限定)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile3_Kikangentei));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4113.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客明細ログ(期間限定)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile4_Kikangentei));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //       # 2021/07/28 add DM合流対応
            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4143.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客明細ログ(DM)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile4_DM));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4122.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引クーポンログ(DM)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile5_DM));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //       # 2022/03/07 add 倍デーポイントの期間限定ポイント対応
            SIZE = FileUtil.readFile(CM_APWORK_DATE + "/S4141.dat").getBytes().length;
            if (0 != SIZE) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("取引顧客ログ(期間限定倍デー)のデータロード"));
                RTN = cmABdbl2STask.main(getExecuteBaseParam().add("-D").add(DB_KBN).add("-C").add(CTLFile3_KikangenteiBaiDay));
                if (RTN != Rtn_OK) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("Copyの実行に失敗しました").FW());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                    DELETE_LOAD_DATA();
                    COPY_ERR_FILE(W_FILE_NAME);
                    continue;
                }
            }

            //    ###########################################
            //    #  パラメータファイル名の取得
            //    ###########################################
            MainResultDto ARG_BNET_PRMFILE_DATA = cmABgprmBServiceImpl.main(getExecuteBaseParam().add("cmBTbmecP"));

            String ARG_BNET_PRMFILE =ARG_BNET_PRMFILE_DATA.result;
            //     #  パラメータファイル名の取得に失敗した場合
            if (ARG_BNET_PRMFILE_DATA.exitCode != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("パラメータファイルが存在しません。[cmBTbmecP]").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //    ###########################################
            //    #  プログラム実行
            //    ###########################################
            //    # 2022/10/31 MCCM初版 ADD START
            //    # 環境変数のサーバ番号を次世代向けに固定

            setenv(CM_SA_NO, "02");

            //    # 2022/10/31 MCCM初版 ADD END
            //    #OPTION1=${CM_HULNOWRCV}"/"${OPTION1}
            //    #OPTION2=${CM_APWORK_DATE}"/"${OPTION2}

            //     #java jp.co.cfh.nttdata.cmBTbmeiJ.main.MCBTbmeiMain ${OPTION1} ${OPTION2} ${ARG_BNET_PRMFILE} ${OPTION4} > /dev/null 2>&1
            //     # 2022/10/07 MCCM初版 MOD START
            //     #java -Xms1024m -Xmx1024m jp.co.cfh.nttdata.cmBTbmeiJ.main.CMBTbmeiMain ${ARG_BNET_PRMFILE} ${OPTION1} > /dev/null 2>&1
            //     # 2023/05/10 MCCMPH2 MOD START
            //     # java -Xms1024m -Xmx1024m jp.co.mcc.nttdata.cmBTbmecJ.main.CMBTbmecMain ${ARG_BNET_PRMFILE} ${GCS_FILE_WORK} ${OPTION1} > /dev/null 2>&1

            shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
                    .addEvn("CM_JAVA_APBIN", CM_JAVA_APBIN)
            .addEvn("ARG_BNET_PRMFILE",ARG_BNET_PRMFILE).addEvn("OPTION1",OPTION1).execute();

            //    # 2023/05/10 MCCMPH2 MOD END
            //    # 2022/10/07 MCCM初版 MOD END

//            RTN = StringUtils.isEmpty(shellExecuteDto.getError()) ? 0 : Integer.valueOf(shellExecuteDto.getError());
            if (!"10".equals(shellExecuteDto.result)) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + " 異常終了").FW());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
                DELETE_LOAD_DATA();
                COPY_ERR_FILE(W_FILE_NAME);
                continue;
            }

            //     ###########################################
            //     #  エラーファイルができていなければ作成
            //     ###########################################
            //     #if [[ -f ${OPTION2} ]]
            //     #then echo
            //     #else
            //     #    echo -n > ${OPTION2}
            //     #fi
            //
            //     ###########################################
            //     #  処理売上明細ファイルを処理中ディレクトリへ戻す
            //     ###########################################

            FileUtil.mvFile(W_FILE_NAME, CM_FILENOWRCV + "/" + new File(W_FILE_NAME).getName());

            //    ###########################################
            //    #  COPY用ファイルの削除
            //    ###########################################

            DELETE_LOAD_DATA();

        }

        //# 2023/05/10 MCCMPH2 DEL START
        //## 2022/10/07 MCCM初版 ADD START
        //############################################
        //##  GOOPON連携ファイルのリネーム
        //############################################
        //#GCS_FILE_NAME=MCC_PT_006.tsv
        //#GCS_FILE=${CM_APWORK_DATE}/${GCS_FILE_NAME}
        //#
        //#mv -f ${GCS_FILE_WORK} ${GCS_FILE} > /dev/null 2>&1
        //#RTN=${?}
        //#if test "${RTN}" -ne 0
        //#then
        //#    cmABplodB cmABaplwB -P${CM_MYPRGID} -M"${GCS_FILE}のリネームに失敗しました" -FE
        //#    cmABplodB cmABaplwB -P${CM_MYPRGID} -E${CM_MYPRGNAME} -FE
        //#    exit ${Rtn_NG}
        //#fi
        //#
        //############################################
        //##  GOOPON連携ファイルの圧縮・移動
        //############################################
        //#ZIP_FILE_NAME=KKGO0034.zip
        //#G_FILE_ID=S_BMEC
        //#
        //## 空ファイルでは圧縮ファイルを作成しない
        //#GCS_SIZE=$(wc -c < ${GCS_FILE_NAME})
        //#if (( $GCS_SIZE == 0 ))
        //#then
        //#    rm ${GCS_FILE_NAME} > /dev/null 2>&1
        //#else
        //#    zip -m ${ZIP_FILE_NAME} ${GCS_FILE_NAME} >/dev/null 2>&1
        //#    RTN=${?}
        //#    if test "${RTN}" -ne 0
        //#    then
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -M"${ZIP_FILE_NAME}の圧縮に失敗しました" -FE
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -E${CM_MYPRGNAME} -FE
        //#        exit ${Rtn_NG}
        //#    fi
        //#
        //#    # 圧縮ファイル移動
        //#    mv -f ${ZIP_FILE_NAME} ${CM_FILEWATSND}/ >/dev/null 2>&1
        //#    RTN=${?}
        //#    if test "${RTN}" -ne 0
        //#    then
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -M"結果ファイル移動エラー　ファイル名=${ZIP_FILE_NAME}　STATUS=${RTN}" -FE
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -E${CM_MYPRGNAME} -FE
        //#        exit ${Rtn_NG}
        //#    fi
        //#
        //#    # 送信連動用ファイル作成
        //#    ls -l ${CM_FILEWATSND}/${ZIP_FILE_NAME} > ${CM_FILEWATSND}/${G_FILE_ID}_OK 2>&1
        //#    RTN=${?}
        //#    if test "${RTN}" -ne 0
        //#    then
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -M"送信連動用ファイル作成エラー　ファイル名=${G_FILE_ID}_OK　STATUS=${RTN}" -FE
        //#        cmABplodB cmABaplwB -P${CM_MYPRGID} -E${CM_MYPRGNAME} -FE
        //#        exit ${Rtn_NG}
        //#    fi
        //#fi
        //## 2022/10/07 MCCM初版 ADD END
        //# 2023/05/10 MCCMPH2 DEL END
        //
        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME));
        return Rtn_OK;
    }

    //###########################################
    //#  エラー発生時のファイル退避関数
    //###########################################
    private void COPY_ERR_FILE(String W_FILE_NAME) {
        String fileName = new File(W_FILE_NAME).getName();
        String ERROR_DIR = CM_APWORK_DATE + "/" + "ERROR";
        FileUtil.mkdir(ERROR_DIR);
        FileUtil.copyFile(W_FILE_NAME, ERROR_DIR + "/" + fileName);

        // ###########################################
        // #  CRMへ連携した処理売上明細ファイルを削除する。
        // ###########################################

        FileUtil.deleteFile(getenv(CM_CRMRENKEI) + "/" + fileName);

        //###########################################
        //#  処理売上明細ファイルを処理中ディレクトリへ戻す
        //###########################################

        FileUtil.mvFile(W_FILE_NAME, getenv(CM_FILENOWRCV) + "/" + fileName);

    }

    //###########################################
    //#  COPY用ファイルの削除関数
    //###########################################
    private void DELETE_LOAD_DATA() {
        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^S4(.)*.csv$");
        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^S4(.)*.tmp$");
        FileUtil.deleteFileByRegx(CM_APWORK_DATE, "^S4(.)*.dat$");
        //################################# 2018/03/30 削除 [START]
        //#    rm ${CM_APWORK_DATE}/CS011*.zip > /dev/null 2>&1
        //################################# 2018/03/30 削除 [END]

    }

}
