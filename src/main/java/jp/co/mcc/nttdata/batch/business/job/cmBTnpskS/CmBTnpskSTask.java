package jp.co.mcc.nttdata.batch.business.job.cmBTnpskS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ReadFileDto;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgprmB.CmABgprmBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTnpskB.CmBTnpskBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  通常ポイント失効
 * #    プログラムID  ：  cmBTnpskS
 * #
 * #    【処理概要】
 * #       バッチ処理日の年から、失効対象年度の年まで、単年単月毎に
 * #      「通常ポイント失効（cmBTnpskB)」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTnpskB」を起動、
 * #       終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #       -pパラレル数:  モジュールのパラレル数
 * #       -sパラレルシーケンス
 * #                   :  パラレルのシーケンス
 * #       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
 * #       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 : 2021/05/11 NDBS.緒方：初版
 * #      40.00: 2023/01/30 SSI.申   ：MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTnpskSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgprmBServiceImpl cmABgprmBServiceImpl;

    //#初期化
    private String OPT_CNT = "";  //#APLOG出力処理件数

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmBTnpskBServiceImpl cmBTnpskBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "通常ポイント失効処理";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;

        //#パラメータファイル
        String PARAM_FILE = "cmABcmpmP";

        //#パラメータファイルの項目名
        String PARAM_INM = "APLOG出力処理件数";

        //#初年度対応
        String START_YEAR = "2023";

        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-p";       //#モジュールのパラレル数
        String ARG_OPT2 = "-s";       //#パラレルのシーケンス
        String ARG_OPT3 = "-DEBUG";   //#デバッグモードでの実施
        String ARG_OPT4 = "-debug";   //#デバッグモードでの実施

        //###########################################
        //#  引数チェック
        //###########################################
        //
        //#------------------------------------------
        //#個数チェック
        //#------------------------------------------
        //#引数が3より多ければエラー
        if (args.length > 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //#------------------------------------------
        //#必須チェック
        //#------------------------------------------
        //
        //#初期化
        String OPT1_FLG = "0";      //#パラレル数フラグ
        String OPT2_FLG = "0";      //#パラレルシーケンスフラグ
        String OPT_PAR = "";        //#パラレル数
        String OPT_SEQ = "";        //#パラレルシーケンス
        String OPT_DEB = "";        //#デバッグ用文字列

        //#引数格納
        for (String arg : args) {
            if (StringUtils.equalsAny(arg, ARG_OPT3, ARG_OPT4)) {
                OPT_DEB = arg;
            } else if (StringUtils.startsWith(arg, ARG_OPT1)) { //#引数が-pの場合
                if (arg.length() > 2) {
                    OPT_PAR = arg.substring(2);
                }
                //#パラレル数フラグON
                OPT1_FLG = "1";
            } else if (StringUtils.startsWith(arg, ARG_OPT2)) { //#引数が-nの場合
                if (arg.length() > 2) {
                    OPT_SEQ = arg.substring(2);
                }
                //#パラレルシーケンスフラグON
                OPT2_FLG = "1";
            } else { //#指定以外の場合スキップ
                continue;
            }
        }

        //#必須引数チェック
        if (StringUtils.equalsAny("0", OPT1_FLG, OPT2_FLG)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //#NULLチェック
        if (StringUtils.isAnyEmpty(OPT_PAR, OPT_SEQ)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数NULL]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //#必須引数の範囲チェック
        //#パラレル数が13以上または、パラレルシーケンスが12以上の場合エラー
        if (Integer.parseInt(OPT_PAR) >= 13 || Integer.parseInt(OPT_SEQ) >= 12) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数範囲エラー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //#パラレル数0チェック
        if (Integer.parseInt(OPT_PAR) == 0) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [パラレル数0エラー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());

            return Rtn_NG;
        }

        //###########################################
        //#  処理件数設定
        //###########################################
        //#パラメータファイル名取得
        MainResultDto mainResultDto = cmABgprmBServiceImpl.main(getExecuteBaseParam().add(PARAM_FILE));
        int RTN = mainResultDto.exitCode;
        if (RTN == Rtn_OK) {
            String PARAM_NAME = mainResultDto.result;
            ReadFileDto.getInstance().readFile(PARAM_NAME).loop3((READ_FLD1, READ_FLD2, READ_FLD3) -> {
                String WK_SNM = READ_FLD1;
                String WK_INM = READ_FLD2;
                String WK_NUM = READ_FLD3;

                //#コメント行でない場合、APLOG出力処理件数を設定
                if (!StringUtils.startsWith(WK_SNM, "#")) {
                    //#シェル名、項目名チェック
                    if (StringUtils.equals(WK_SNM, CM_MYPRGID) && StringUtils.equals(WK_INM, PARAM_INM)) {
                        OPT_CNT = WK_NUM;
                        return Rtn_OK;
                    }
                }

                return Rtn_OK;
            });
        } else {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("パラメータファイル名取得エラー").FW());
        }

        //###########################################
        //#  バッチ処理日付の取得
        //###########################################
        mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());
        RTN = mainResultDto.exitCode;
        if (RTN != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

            return Rtn_NG;
        }
        String BAT_YYYYMMDD = mainResultDto.result;

        //###########################################
        //#  処理開始年月の設定
        //###########################################
        //
        //#処理開始年の設定
        String YYYY = BAT_YYYYMMDD.substring(0, 4);

        String BAT_MM = BAT_YYYYMMDD.substring(4, 6);

        //#処理開始月の設定
        //#（バッチ処理月）ー（パラレルシーケンス）
        int MM = Integer.parseInt(BAT_MM) - Integer.parseInt(OPT_SEQ);

        //#年跨ぎの処理
        if (MM < 0) {
            YYYY = Integer.toString(Integer.parseInt(YYYY) - 1);
            MM = 12 + MM;
        }

        //###########################################
        //#  処理終了年月の設定
        //###########################################
        //
        //#処理終了年の設定
        String BAT_YYYY = BAT_YYYYMMDD.substring(0, 4);

        String END_YYYY = "";
        //#初回稼働の判定（2023年チェック）
        if (StringUtils.equals(BAT_YYYY, START_YEAR)) {
            END_YYYY = "2020";
        } else { //#それ以外の年で稼働
            // #年跨ぎ判定
            if (Integer.parseInt(BAT_MM) < 3) {
                //#1-3月の場合、3年分遡る
                END_YYYY = Integer.toString(Integer.parseInt(BAT_YYYY) - 3);
            } else {
                //#4－12月の場合、2年分遡る
                END_YYYY = Integer.toString(Integer.parseInt(BAT_YYYY) - 2);
            }
        }

        //#処理終了年月の設定（処理終了年の2月）
        String END_YYYYMM = Long.toString(Long.parseLong(END_YYYY) * 100 + 2);

        //###########################################
        //#  通常ポイント失効　プログラム実行
        //###########################################
        //
        //#処理終了になるまでループ
        for (; ; ) {
            //#通常ポイント失効ＢＴ実行
            mainResultDto = cmBTnpskBServiceImpl.main(getExecuteBaseParam().add("-y" + YYYY).add("-m" + MM).add("-a" + OPT_CNT).add(OPT_DEB));
            RTN = mainResultDto.exitCode;
            if (RTN != Rtn_OK) {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("通常ポイント失効エラー").FE());

                return Rtn_NG;
            }

            //#処理月の設定
            //#（直前のBTに設定した処理月MM）－（パラレル数）
            MM = MM - Integer.parseInt(OPT_PAR);

            //#年跨ぎの処理
            if (MM <= 0) {
                YYYY = Integer.toString(Integer.parseInt(YYYY) - 1);
                MM = 12 + MM;
            }

            //#処理年月の設定
            String YYYYMM = Long.toString(Long.parseLong(YYYY) * 100 + MM);

            //#ループ継続チェック
            //#処理年月が処理終了年月以下の場合、処理終了
            if (Long.parseLong(YYYYMM) <= Long.parseLong(END_YYYYMM)) {
                break;
            }
        }

        //###########################################
        //#  終了メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }
}
