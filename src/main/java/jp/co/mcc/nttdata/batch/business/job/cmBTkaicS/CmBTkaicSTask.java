package jp.co.mcc.nttdata.batch.business.job.cmBTkaicS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTkaicB.CmBTkaicBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.job.cmBTcfckS.CmBTcfckSTask;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.IconvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*

        -------------------------------------------------------------------------------
            名称          ：  会員情報作成
            プログラムID  ：  cmBTkaicS

            【処理概要】
                MM顧客情報、MM顧客属性情報、MM顧客企業別属性情報より
                連動用会員情報ファイルを作成する「会員情報作成（cmBTkaicB）」を
                起動するためのシェル。
                開始メッセージを出力し、「cmBTkaicB」を起動、終了メッセージを出力し、
                戻り値を返却。

            【引数説明】
               -b誕生年基準値          : （必須）誕生年基準値
               -d抽出対象日付          : （任意）抽出対象日付（省略時はバッチ処理日前日）
               -DEBUG or -debug        : （任意）デバッグモードでの実行（トレース出力機能）

            【戻り値】
               10     ：  正常
               99     ：  異常
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 6

            改定履歴
              41.01 :  2024/01/24 SSI.川内 ： MCCMPH2 HS-0146 関心分野コードのダブルクォートを除去,NULLを設定
              41.02 :  2024/02/27 SSI.川内 ： MCCMPH2 HS-0170 仕変対応 PS店舗変換マスタをもとに会社コード、店番号を変換
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2022 NTT DATA CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTkaicSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmBTkaicBServiceImpl cmBTkaicBServiceImpl;

    @Autowired
    private CmBTcfckSTask cmBTcfckSTask;

    @Autowired
    private CmABfzipSTask cmABfzipSTask;

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME = "会員情報作成";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  定数定義
//        ###########################################

        Rtn_OK = 10;
        Rtn_NG = 99;

        String ARG_OPT1 = "-DEBUG";          //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2 = "-debug";          //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3 = "-o";              //  出力会員情報ファイル
        String ARG_OPT4 = "-b";              //  誕生年基準値
        String ARG_OPT5 = "-d";              //  抽出対象日付

        String OUT_FILE = "KKMI0150.csv";
        String OUT_ZIP_FILE = "KKMI0150.zip";
        String OUT_FILE_SJIS = "KKMI0150.tmp";

        String KINOUID = CM_MYPRGID.substring(4, 8); // 機能ID

//        ###########################################
//        #  稼動ディレクトリ決定
//        ###########################################

        setCM_APWORK_DATE();

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  バッチ処理日付前日の取得
//        ###########################################

        MainResultDto cmABgdatBResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
        if (cmABgdatBResultDto.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付前日の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        SYS_YYYYMMDD = cmABgdatBResultDto.result;

//        ###########################################
//        #  引数の数チェック
//        ###########################################

        if (args.length > 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  引数格納
//        ###########################################

//        変数初期化

        String OPTION1 = "";
        String OPTION2 = "";
        String OPTION3 = "";
        String OPTION4 = "";
        String BIRTH_YEAR_KIJUN = "";
        String TARGET_DATE = "";

        for (String arg : args) {
            if (arg.equals(ARG_OPT1) || arg.equals(ARG_OPT2)) {
                OPTION1 = arg;
            } else if (arg.startsWith(ARG_OPT4)) {
                OPTION3 = arg;
            } else if (arg.startsWith(ARG_OPT5)) {
                OPTION4 = arg;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

//        必須引数をチェック

        if (StringUtils.isEmpty(OPTION3)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//         -o出力ファイルからファイル名部分を抽出
//        OUT_FILE=`echo ${OPTION2} | cut -c3-`

//        -b誕生年基準値から誕生年基準値部分を抽出

        if (OPTION3.trim().length() > 2) {
            BIRTH_YEAR_KIJUN = OPTION3.substring(2);
        }

//        -d抽出対象日付から抽出対象日付部分を抽出

        if (OPTION4.trim().length() > 2) {
            TARGET_DATE = OPTION4.substring(2);
        }

//        抽出対象日付省略時、バッチ処理日前日を設定

        if (StringUtils.isEmpty(TARGET_DATE)) {
            TARGET_DATE = SYS_YYYYMMDD;
        }

//        ###########################################
//        #  プログラム実行
//        ###########################################

        MainResultDto cmBTkaicBResultDto = cmBTkaicBServiceImpl.main(getExecuteBaseParam().add("-o" + OUT_FILE).add("-b" + BIRTH_YEAR_KIJUN).add("-d" + TARGET_DATE).add(OPTION1));
        if (cmBTkaicBResultDto.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  SJIS変換
//        ###########################################

        int RNT = IconvUtil.main(SystemConstant.UTF8, SystemConstant.MS932, CM_APWORK_DATE + "/" + OUT_FILE,
                CM_APWORK_DATE + "/" + OUT_FILE_SJIS);

        if (RNT != 0) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_APWORK_DATE + "/" + OUT_FILE + "のSJIS変換に失敗しました").FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FW());
            return Rtn_NG;
        }

        FileUtil.mvFile(CM_APWORK_DATE + "/" + OUT_FILE_SJIS, CM_APWORK_DATE + "/" + OUT_FILE);

//        ###########################################
//        #  連携ファイルのレコード重複の確認(KKMI0150)
//        ###########################################

//         -i入力ファイル  -k機能ID  -cチェックキー項目の順番  -nグーポン番号の順番

        int RTN = cmBTcfckSTask.main(getExecuteBaseParam().add("-i" + OUT_FILE).add("-k" + KINOUID).add("-c1").add("-n1"));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連携ファイルのレコード重複の確認に失敗しました[" + OUT_FILE + "]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  ファイル圧縮実行
//        ###########################################

        RTN = cmABfzipSTask.main(getExecuteBaseParam().sO().add(CM_APWORK_DATE).sD().add(CM_APWORK_DATE).sI().add(OUT_FILE).sZ().add(OUT_ZIP_FILE).add("-DEL"));
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮ファイル(" + OUT_FILE + ")作成に失敗しました。").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  終了メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }

}
