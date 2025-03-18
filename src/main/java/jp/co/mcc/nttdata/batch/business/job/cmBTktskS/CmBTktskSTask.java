package jp.co.mcc.nttdata.batch.business.job.cmBTktskS;

import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.CmBTktskBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTkpskB.CmBTkpskBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;

/**
#-------------------------------------------------------------------------------
#    名称          ：  退会顧客ポイント失効
#    プログラムID  ：  cmBTktskS
#
#    【処理概要】
#       「退会顧客ポイント失効（cmBTktskB)」を起動するためのシェル。
#       開始メッセージを出力し、「cmBTktskB」を起動、
#       終了メッセージを出力し、戻り値を返却。
#
#    【引数説明】
#       -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
#       -debug      :  デバッグモードでの実行（トレース出力機能が有効）
#
#    【戻り値】
#       10     ：  正常
#       99     ：  異常
#-------------------------------------------------------------------------------
#    稼働環境
#      Red Hat Enterprise Linux 6
#
#    改定履歴
#      1.00 : 2021/1/05 NDBS.緒方：初版
#-------------------------------------------------------------------------------
#  $Id:$
#-------------------------------------------------------------------------------
#  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
#-------------------------------------------------------------------------------
**/
@Component
public class CmBTktskSTask extends NtBasicTask {

  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;

  @Autowired
  CmBTktskBServiceImpl cmBTktskBService;
  @Override
  public int taskExecuteCustom(String[] args) {
    /*
     * ###########################################
     * #  プログラムIDを環境変数に設定
     * ###########################################
     */
    CM_MYPRGNAME="退会顧客ポイント失効";
    setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

    /*
     * ###########################################
     * #  開始メッセージをAPログに出力
     * ###########################################
     */
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

    /*
     * ###########################################
     * #  定数定義
     * ###########################################
     */
    int Rtn_OK = 10;
    int Rtn_NG = 99;

    //###########################################
    //#  引数の数チェック
    //###########################################
    if (args.length > 1) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //#  顧客退会ポイント失効　プログラム実行
    //###########################################
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("顧客退会ポイント失効実行").FI());

    MainResultDto resultDto =cmBTktskBService.main(getExecuteBaseParam().add(args));
    if (resultDto.exitCode != Rtn_OK){
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("顧客退会ポイント失効エラー").FE());
      return Rtn_NG;
    }


    //###########################################
    //#  終了メッセージをAPログに出力
    //###########################################
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
    return Rtn_OK;
  }
}
