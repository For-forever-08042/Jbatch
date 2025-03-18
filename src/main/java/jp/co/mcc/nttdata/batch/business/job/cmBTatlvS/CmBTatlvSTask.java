package jp.co.mcc.nttdata.batch.business.job.cmBTatlvS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTatlvB.CmBTatlvBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
        -------------------------------------------------------------------------------
            名称          ：  自動退会処理
            プログラムID  ：  cmBTatlvS

            【処理概要】
               自動退会顧客データファイルの顧客に対して、
               自動退会処理（退会顧客データファイル出力）を行う
               処理「自動退会（cmBTatlvB）」を起動するためのシェル。
               開始メッセージを出力し、「cmBTtrcmB」を起動、終了メッセージを出力し、
               戻り値を返却。

            【引数説明】
               -o処理結果ファイル名       : （必須）物理出力ファイル名($CM_APWORK_DATE)
               -DEBUG or -debug           :  (任意) デバッグモードでの実行（トレース出力機能）

            【戻り値】
               10     ：  正常
               49     ：  異常
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 5

            改定履歴
              1.00 :   2012/11/10 SSI.越後谷  ： 初版
              40.00:   2022/10/06 SSI.申      ： MCCM初版
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2012 NTT DATA CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTatlvSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmBTatlvBServiceImpl cmBTatlvBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME="自動退会";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  定数定義
//        ###########################################

        Rtn_OK=10;
        Rtn_NG=49;

//        ｢顧客自動退会｣用ジョブ実績出力用ログファイル名称

        String CM_G16001D = "JBch_G16001D";   //本番用は「JBch_G16001D」

//        ###########################################
//        #  引数定義
//        ###########################################

        String ARG_OPT1="-o";           //  処理結果ファイル名
        String ARG_OPT2="-DEBUG";       //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3="-debug";       //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT4="-s";           //  処理結果ファイル名

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
//        #  引数の数チェック
//        ###########################################

        if (args.length > 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }

//          引数格納変数初期化

        String OPTION1="";
        String OPTION2="";
        String OPTION3="";

//          引数格納
        for (String arg : args) {
            if (arg.equals(ARG_OPT2) || arg.equals(ARG_OPT3)){
                OPTION2 = arg;
            } else if (arg.startsWith(ARG_OPT1)){
                OPTION1 = arg;
            } else if (arg.startsWith(ARG_OPT4)){
                OPTION3 = arg;
            }
        }

//        ###########################################
//        #  プログラム実行
//        ###########################################

        MainResultDto mainResultDto = cmBTatlvBServiceImpl.main(getExecuteBaseParam().add(OPTION1).add(OPTION2).add(OPTION3));
        if (mainResultDto.exitCode != Rtn_OK){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ################################################
//        #  処理対象情報をジョブ実績ログファイルに出力
//        ################################################

//         引数よりファイル名の取得

        String OUT_FILE = OPTION1.substring(2);
        String OUT_FILE2 = OPTION3.substring(2);

//         出力ファイルのデータ件数を取得

        int OUTPUT_FILE_DATA_CNT = FileUtil.countLines(CM_APWORK_DATE + "/" + OUT_FILE);
        int OUTPUT_FILE_DATA_CNT2 = FileUtil.countLines(CM_APWORK_DATE + "/" + OUT_FILE2);

        FileUtil.writeFile(CM_APWORK_DATE + "/" + getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G16001D + ".log",
                "        退会処理（静態クリア等）対象顧客番号：" + OUTPUT_FILE_DATA_CNT + " 件");

        FileUtil.writeFile(CM_APWORK_DATE + "/" + getenv(C_aplcom1Service.CM_JOBRESULTLOG) + "/" + CM_G16001D + ".log",
                "        退会処理（顧客ステータス更新）対象顧客番号：" + OUTPUT_FILE_DATA_CNT2 + " 件");

//        ###########################################
//        # 結果ファイルの移動
//        ###########################################

//        echo "移動コマンド：mv -f ${CM_APWORK}/${OUT_FILE} ${CM_APRESULT}/."
//        mv -f ${CM_APWORK_DATE}/${OUT_FILE} ${CM_APRESULT}/. > /dev/null 2>&1


        FileUtil.copyFile(CM_APWORK_DATE + "/" + OUT_FILE,getenv(C_aplcom1Service.CM_APRESULT) + "/" + OUT_FILE);
        FileUtil.copyFile(CM_APWORK_DATE + "/" + OUT_FILE2,getenv(C_aplcom1Service.CM_APRESULT) + "/" + OUT_FILE2);

//        ###########################################
//        #  終了メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;

    }

}
