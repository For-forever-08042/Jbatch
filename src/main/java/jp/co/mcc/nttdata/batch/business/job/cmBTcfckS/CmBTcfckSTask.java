package jp.co.mcc.nttdata.batch.business.job.cmBTcfckS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  重複チェック
 * #    プログラムID  ：  cmBTcfckS
 * #
 * #    【処理概要】
 * #        SPSS向けGOOPON番号紐付け情報の再発防止策。
 * #
 * #    【引数説明】
 * #        -i入力ファイル名                  : 入力ファイル名
 * #        -k機能ID                          : 機能ID
 * #        -c重複チェック処理の項目の順番    : 重複チェック処理の項目の順番(例:-c1,3)
 * #        -nグーポン番号の順番              : errorファイルの出力項目の順番(グーポン番号)(例:-n1)
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      40.00 : 2023/09/13 SSI.申 : 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2023 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */

@Slf4j
@Component
public class CmBTcfckSTask extends NtBasicTask {

    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;


    @Override
    public int taskExecuteCustom(String[] args) {


        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################
        CM_MYPRGNAME = "重複チェック";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //###########################################
        //#  定数定義
        //###########################################
        Rtn_OK = 10;
        Rtn_NG = 49;


        String KAIINCHOUFUKU_FILE_NAME = "kaiinchoufuku_error";
        //###########################################
        //#  引数定義
        //###########################################
        String ARG_OPT1 = "-i";   // ###  入力ファイル名
        String ARG_OPT2 = "-k";   // ###  機能ID
        String ARG_OPT3 = "-c";   // ###  重複チェック処理の項目
        String ARG_OPT4 = "-n";   // ###  errorファイルの出力項目の順番(グーポン番号)

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################
        setCM_APWORK_DATE();

        if (StringUtils.isNotEmpty(CM_APWORK_DATE)) {
            if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
                if (!FileUtil.mkdir(CM_APWORK_DATE)) {
                    // 作業ディレクトリファイル作成失敗
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
                    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                    return Rtn_NG;
                }
            }
        }

        //#  システム日付
        String SYS_YYYYMMDD = DateUtil.getYYYYMMDD();


        //###########################################
        //#  引数の数チェック
        //###########################################
        if (args.length > 4) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }
        //###########################################
        //#  引数格納
        //###########################################
        //#  変数初期化
        String OPTION1 = "";//     ### 入力ファイル名
        String OPTION2 = "";//     ### 機能ID
        String OPTION3 = "";//     ### 重複チェック処理の項目
        String OPTION4 = "";//     ### errorファイルの出力項目の順番(グーポン番号)

        for (String ARG_VALUE : args) {
            if (ARG_VALUE.startsWith(ARG_OPT1)) {
                OPTION1 = ARG_VALUE.substring(2);
            } else if (ARG_VALUE.startsWith(ARG_OPT2)) {
                OPTION2 = ARG_VALUE.substring(2);
            } else if (ARG_VALUE.startsWith(ARG_OPT3)) {
                OPTION3 = ARG_VALUE.substring(2);
            } else if (ARG_VALUE.startsWith(ARG_OPT4)) {
                OPTION4 = ARG_VALUE.substring(2);
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + ARG_VALUE + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

        //# 必須引数をチェック
        if (StringUtils.isEmpty(OPTION1) || StringUtils.isEmpty(OPTION2) || StringUtils.isEmpty(OPTION3) || StringUtils.isEmpty(OPTION4)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }


        //###########################################
        //# 入力ファイル存在チェック
        //###########################################
        if (!FileUtil.isExistFile(CM_APWORK_DATE + "/" + OPTION1)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("入力ファイルなし [" + CM_APWORK_DATE + "/" + OPTION1 + "]").FI());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
            return Rtn_OK;
        }
        //###########################################
        //#  重複チェック処理
        //###########################################
        //################################################
        //#  PMダウンロードファイル情報取得
        //################################################
        ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID)
                .addEvn("CM_APWORK_DATE",CM_APWORK_DATE)
                .addEvn("KAIINCHOUFUKU_FILE_NAME",KAIINCHOUFUKU_FILE_NAME)
                .addEvn("SYS_YYYYMMDD",SYS_YYYYMMDD)
                .addEvn("OPTION1", OPTION1)
                .addEvn("OPTION2", OPTION2)
                .addEvn("OPTION3", OPTION3)
                .addEvn("OPTION4", OPTION4)
                .execute();

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
    }

}
