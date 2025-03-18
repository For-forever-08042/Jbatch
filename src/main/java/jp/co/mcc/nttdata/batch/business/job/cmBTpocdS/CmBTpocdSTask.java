package jp.co.mcc.nttdata.batch.business.job.cmBTpocdS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTpocdB.CmBTpocdBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  郵便番号コード設定
 * #    プログラムID  ：  cmBTpocdS
 * #
 * #    【処理概要】
 * #        MM顧客属性情報の郵便番号コード未設定のものに対して
 * #        郵便番号コードを設定する処理「郵便番号コード設定（cmBTpocdB）」を
 * #        起動するためのシェル。
 * #        開始メッセージを出力し、「cmBTpocdB」を起動、終了メッセージを出力し、
 * #        戻り値を返却。
 * #
 * #    【引数説明】
 * #      -d処理日付    :  処理日付
 * #      -DEBUG        :  デバッグモードでの実行（トレース出力機能が有効）
 * #      -debug        :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/12 SSI.吉岡  ： 初版
 * #     40.00 :   2022/09/30 SSI.川内  ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTpocdSTask   extends NtBasicTask {
  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;

  @Autowired
  CmBTpocdBServiceImpl cmBTpocdBServiceImpl;

  @Override
  public int taskExecuteCustom(String[] args) {

    /*
     * ###########################################
     * #  プログラムIDを環境変数に設定
     * ###########################################
     */
    CM_MYPRGNAME="郵便番号コード設定";
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
    int Rtn_NG = 49;

    //###########################################
    //#  稼動ディレクトリ決定
    //###########################################
    setCM_APWORK_DATE();

    if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
      if(!FileUtil.mkdir(CM_APWORK_DATE)){
        //# 作業ディレクトリファイル作成失敗
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }
    }

    //###########################################
    //#  引数の数チェック
    //###########################################
    if (args.length > 2) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //#  プログラム実行
    //###########################################
    String ARG_VALUE = "";
    String CHECK_FILE = CM_APWORK_DATE+ "/ADDRESS.STS";
    if(FileUtil.isExistFile(CHECK_FILE)){
      ARG_VALUE = "-zmc";
    }else{
      ARG_VALUE = "-zip";
    }

    MainResultDto mainResultDto = cmBTpocdBServiceImpl.main(getExecuteBaseParam().add(ARG_VALUE).add(args));
    int RTN = mainResultDto.exitCode;
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //#  終了メッセージをAPログに出力
    //###########################################
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
    return Rtn_OK;
  }
}
