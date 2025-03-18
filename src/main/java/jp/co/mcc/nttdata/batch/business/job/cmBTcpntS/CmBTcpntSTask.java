package jp.co.mcc.nttdata.batch.business.job.cmBTcpntS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABdbtrS.CmABdbtrSTask;
import jp.co.mcc.nttdata.batch.business.service.cmBTcpntB.CmBTcpntBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*

        -------------------------------------------------------------------------------
            名称          ：  顧客入会情報更新
            プログラムID  ：  cmBTcpntS

            【処理概要】
               TS利用可能ポイント情報の入会店、発券店の情報をMM顧客情報に反映する。

            【引数説明】
               -DEBUG             :  デバッグモードでの実行（トレース出力機能が有効）
               -debug             :  デバッグモードでの実行（トレース出力機能が有効）

            【戻り値】
               10     ：  正常
               49     ：  異常
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 5

            改定履歴
              1.00 :   2013/03/13 SSI.本田 ： 初版
              40.00:   2022/10/07 SSI.申   ： MCCM初版

        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2012 NTT DATA CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTcpntSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABdbtrSTask cmABdbtrSTask;

    @Autowired
    private CmBTcpntBServiceImpl cmBTcpntBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME="顧客入会情報更新";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  定数定義
//        ###########################################

        Rtn_OK=10;
        Rtn_NG=49;

//        引数定義

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

        if (args.length > 2) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }

//        ############################################
//        ##  引数格納変数初期化
//        ############################################

//        ###########################################
//        # WS顧客番号をTRUNCATE
//        ###########################################

        int RTN = cmABdbtrSTask.main(getExecuteBaseParam().sT().add("WM顧客番号").sD().add("MD"));
        if (RTN != Rtn_OK){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("WM顧客番号のTRUNCATEに失敗しました").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  プログラム実行
//        ###########################################

        MainResultDto mainResultDto = cmBTcpntBServiceImpl.main(getExecuteBaseParam().add(ARG_ALL));
        if (mainResultDto.exitCode != Rtn_OK){
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
