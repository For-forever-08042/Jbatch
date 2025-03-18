package jp.co.mcc.nttdata.batch.business.job.cmBTpclrS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTpclrB.CmBTpclrBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*

        -------------------------------------------------------------------------------
            名称          ：  ポイントクリア処理
            プログラムID  ：  cmBTpclrS

            【処理概要】
               失効済みの通常ポイント・期間限定ポイントのクリアを行う処理
               「ポイントクリア（cmBTpclrB）」を起動するためのシェル。
               開始メッセージを出力し
              「cmBTpclrB」を起動、終了メッセージを出力し、戻り値を返却。

            【引数説明】
               -c          :  更新最大件数(必須)
               -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
               -debug      :  デバッグモードでの実行（トレース出力機能が有効）

            【戻り値】
               10     ：  正常
               49     ：  異常
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 5

            改定履歴
              40.00 : 2022/10/07 SSI.申：MCCM初版
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTpclrSTask  extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Autowired
    private CmBTpclrBServiceImpl cmBTpclrBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME="ポイントクリア";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  定数定義
//        ###########################################

        Rtn_OK=10;
        int Rtn_WR=49;
        Rtn_NG=99;

//        引数定義
        String ARG_OPT1="-c";           //  更新最大件数
        String ARG_OPT2="-DEBUG";       //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT3="-debug";       //  デバッグモードでの実行（トレース出力機能が有効）

//        ###########################################
//        #  引数の数チェック
//        ###########################################

        if (args.length < 1){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_WR;
        }

        if (args.length > 2){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_WR;
        }

//        ###########################################
//        #  引数チェック
//        ###########################################

//        引数格納変数初期化

        String OPTION1="";
        String OPTION2="";

        for (String arg : args) {
            if (arg.equals(ARG_OPT2) || arg.equals(ARG_OPT3)){
                OPTION2 = arg;
            } else if (arg.startsWith(ARG_OPT1)){
                OPTION1 = arg;
            }
        }

//        必須引数をチェック

        if (StringUtils.isEmpty(OPTION1)){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [必須引数なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_WR;
        }

//        ###########################################
//        #  バッチ処理日付取得
//        ###########################################

        MainResultDto cmABgdatBResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());

        String BAT_YYYYMMDD = cmABgdatBResultDto.result;

        if (cmABgdatBResultDto.exitCode != Rtn_OK){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());
            return Rtn_WR;
        }

        String BAT_YYYYMM = BAT_YYYYMMDD.substring(0,6);

//        ###########################################
//        #  プログラム実行
//        ###########################################

        MainResultDto cmBTpclrBResultDto = cmBTpclrBServiceImpl.main(getExecuteBaseParam().add(OPTION1).add(OPTION2));

        if (cmBTpclrBResultDto.exitCode != Rtn_OK && cmBTpclrBResultDto.exitCode != Rtn_WR){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_WR;
        }

//        ###########################################
//        #  バッチ日付の比較
//        ###########################################

        String BAT_DD = BAT_YYYYMMDD.substring(6,8);

        if (Integer.parseInt(BAT_DD) >= 20 && cmBTpclrBResultDto.exitCode == Rtn_OK){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M(CM_MYPRGNAME + "を実行しました。日付：" + BAT_YYYYMMDD).FW());
        }

//        ###########################################
//        #  終了メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;

    }

}
