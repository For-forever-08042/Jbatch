package jp.co.mcc.nttdata.batch.business.job.cmBTspcdS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTspcdB.CmBTspcdBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  番号変換マスタデータ連動
 * #    プログラムID  ：  cmBTspcdS
 * #
 * #    【処理概要】
 * #       番号変換マスタデータ連動ファイル作成を行う「番号変換マスタデータ連動
 * #      （cmBTspcdB）」を起動するためのシェル。開始メッセージを出力し、
 * #      「cmBTspcdB」を起動、終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -sd         :  抽出対象開始日付（任意）
 * #       -ed         :  抽出対象終了日付（任意）
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）（任意）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）（任意）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2021/08/06 SSI.張：初版
 * #      2.00 :   2021/09/17 SSI.上野：ファイル転送修正に伴い圧縮ファイル名変更
 * #      3.00 :   2021/09/30 SSI.上野：(暫定)出力対象からEC会員データを除去
 * #      4.00 :   2021/11/19 SSI. 張 ：(暫定対処)を廃棄
 * #      4.10 :   2021/11/30 SSI. 張 ：レコード重複対応
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmBTspcdSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmBTspcdBServiceImpl cmBTspcdBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "番号変換マスタデータ連動";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        //#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 49;

        //#  引数定義
        String ARG_OPT1 = "-sd";          //###  抽出対象開始日付
        String ARG_OPT2 = "-ed";          //###  抽出対象終了日付
        String ARG_OPT3 = "-DEBUG";       //###  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT4 = "-debug";       //###  デバッグモードでの実行（トレース出力機能が有効）

        String FILE_NAME1 = "^tmp_.{14}_GPNCHG.csv";
        String ERR_FILE_NAME = "";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
            if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
                //# 作業ディレクトリ作成失敗
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //#  引数格納
        //###########################################
        //#  引数格納変数初期化
        String OPTION1 = "";
        String OPTION2 = "";
        String OPTION3 = "";
        String OPTION4 = "";

        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //#  引数チェック
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT3, ARG_OPT4)) {
                OPTION3 = arg;
            } else if (StringUtils.startsWith(arg, ARG_OPT1)) {
                OPTION1 = arg;
            } else if (StringUtils.startsWith(arg, ARG_OPT2)) {
                OPTION2 = arg;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [想定外パラメータ]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                return Rtn_NG;
            }
        }

        //###########################################
        //# 任意引数をチェック
        //###########################################

        //# 開始日付のみ指定
        if (StringUtils.isNotEmpty(OPTION1) && StringUtils.isEmpty(OPTION2)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [開始日指定あり　終了日指定なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //# 終了日付のみ指定
        if (StringUtils.isEmpty(OPTION1) && StringUtils.isNotEmpty(OPTION2)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [開始日指定なし　終了日指定あり]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  取得範囲チェック
        //###########################################
        String FM_DATE = OPTION1.replaceAll(ARG_OPT1, "");
        String TO_DATE = OPTION2.replaceAll(ARG_OPT2, "");

        if (FM_DATE.compareTo(TO_DATE) > 0) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [取得範囲不正]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  番号変換マスタデータ連動プログラム実行
        //###########################################
        MainResultDto cmBTspcdBResult = cmBTspcdBServiceImpl.main(getExecuteBaseParam().add(OPTION1).add(OPTION2).add(OPTION3));
        int RTN = cmBTspcdBResult.exitCode;
        if (RTN != Rtn_OK) {
            //# 終了メッセージをAPログに出力
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  連動ファイルチェック
        //###########################################

        if (!FileUtil.isExistFileByRegx(CM_APWORK_DATE  , FILE_NAME1)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮対象ファイルなし [" + FILE_NAME1 + "]").FI());
        } else {
            // tmp_??????????????_GPNCHG.csv
            ArrayList<String> filePathList = FileUtil.findByRegex(CM_APWORK_DATE, "tmp_.{14}_GPNCHG.csv");

            for (String s : filePathList) {
                String FILE_NAME = "KKMI0200";

                //###########################################
                //#  レコード重複判断(重複しないレコード処理)
                //##########################################
                String OK_FILE_NAME = FILE_NAME + ".csv";
                //cat ${FILE_NAME1} | awk -F, '!a[$1$2]++' > ${OK_FILE_NAME}
                ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_01")
                        .defaultEvn(this)
                        .addEvn("FILE_NAME1", FILE_NAME1)
                        .addEvn("OK_FILE_NAME", OK_FILE_NAME)
                        .execute();

                //###########################################
                //#  番号変換マスタデータファイル圧縮実行
                //###########################################
                String ZIP_FILE_NAME1 = FILE_NAME + ".zip";
                //zip -m ${ZIP_FILE_NAME1} ${OK_FILE_NAME} >/dev/null 2>&1
//                ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_02")
//                        .defaultEvn(this)
//                        .addEvn("ZIP_FILE_NAME1", ZIP_FILE_NAME1)
//                        .addEvn("OK_FILE_NAME", OK_FILE_NAME)
//                        .execute();
              RTN =ZipUtil.zipFileAndDelete(CM_APWORK_DATE+"/"+ZIP_FILE_NAME1,CM_APWORK_DATE+"/"+OK_FILE_NAME);
                if (RTN != 0) {
                    FileUtil.deleteFile(CM_APWORK_DATE + "/" + ZIP_FILE_NAME1);
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮失敗[" + OK_FILE_NAME + "][1]").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                    return Rtn_NG;
                }

                //###########################################
                //#  レコード重複判断(重複したレコード処理)
                //##########################################
                ERR_FILE_NAME = FILE_NAME + "_error.csv";
                //cat ${FILE_NAME1} | awk -F, 'a[$1$2]++' > ${ERR_FILE_NAME}
                ShellClientManager.getShellExecuteDto(CM_MYPRGID + "_03")
                        .defaultEvn(this)
                        .addEvn("FILE_NAME1", FILE_NAME1)
                        .addEvn("ERR_FILE_NAME", ERR_FILE_NAME)
                        .execute();

                if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + ERR_FILE_NAME)) {
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("重複レコードあり[" + ERR_FILE_NAME + "]"));

                    //###########################################
                    //#  重複したレコードをエラーファイル圧縮実行
                    //###########################################
                    String ZIP_FILE_NAME2 = FILE_NAME + "_error.zip";
                    //zip -m ${ZIP_FILE_NAME2} ${ERR_FILE_NAME} >/dev/null 2>&1
//                  ShellExecuteDto   shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGNAME + "_04")
//                            .defaultEvn(this)
//                            .addEvn("ZIP_FILE_NAME2", ZIP_FILE_NAME2)
//                            .addEvn("ERR_FILE_NAME", ERR_FILE_NAME)
//                            .execute();
                  RTN =ZipUtil.zipFileAndDelete(CM_APWORK_DATE+"/"+ZIP_FILE_NAME2,CM_APWORK_DATE+"/"+ERR_FILE_NAME);
                    if (RTN !=0) {
                        FileUtil.deleteFile(CM_APWORK_DATE + "/" + ZIP_FILE_NAME2);
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮失敗[" + ZIP_FILE_NAME2 + "][1]").FE());
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

                        return Rtn_NG;
                    }
                }
            }
        }

        //###########################################
        //#  元ファイル削除
        //###########################################
        if (!FileUtil.deleteFileByRegx(CM_APWORK_DATE , FILE_NAME1)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("元ファイル削除失敗  [" + FILE_NAME1 + "][1]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  元エラーファイル削除
        //###########################################
        if (FileUtil.deleteFile(CM_APWORK_DATE + "/" + ERR_FILE_NAME)!=0) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("元ファイル削除失敗  [" + ERR_FILE_NAME + "][1]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        //# 終了メッセージをAPログに出力
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
