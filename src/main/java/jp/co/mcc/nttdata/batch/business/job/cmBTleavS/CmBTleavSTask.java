package jp.co.mcc.nttdata.batch.business.job.cmBTleavS;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTleavB.CmBTleavBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  顧客退会処理
 * #    プログラムID  ：  cmBTleavS
 * #
 * #    【処理概要】
 * #       退会顧客データファイルの顧客に対して、
 * #       退会処理（カード停止、顧客静態クリア、エントリー停止等）を行う
 * #       処理「顧客退会（cmBTleavB）」を起動するためのシェル。
 * #       開始メッセージを出力し、「cmBTtrcmB」を起動、終了メッセージを出力し、
 * #       戻り値を返却。
 * #
 * #    【引数説明】
 * #       -i退会顧客データファイル名 : （必須）物理入力ファイル名($CM_APWORK_DATE)
 * #       -o処理結果ファイル名       : （必須）物理出力ファイル名($CM_APWORK_DATE)
 * #       -k顧客ステータス退会ファイル名 :（必須）物理入力ファイル名（$CM_APWORK_DATE）
 * #       -DEBUG or -debug           :  (任意) デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :   2012/11/07 SSI.越後谷  ： 初版
 * #      40.00:   2022/10/14 SSI.申      ： MCCM初版
 * #      41.00:   2023/05/08 SSI.陳セイキン：MCCMPH2
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTleavSTask  extends NtBasicTask {

  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;

  @Autowired
  CmBTleavBServiceImpl cmBTleavBServiceImpl;
  @Override
  public int taskExecuteCustom(String[] args) {

    /*
     * ###########################################
     * #  プログラムIDを環境変数に設定
     * ###########################################
     */
    CM_MYPRGNAME="顧客退会";
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
    int RTN = 0;

    //###########################################
    //#  引数定義
    //###########################################
    String ARG_OPT1="-i";           //###  退会顧客データファイル名
    String ARG_OPT2="-o";           //###  処理結果ファイル名
    String ARG_OPT3="-DEBUG";           //###  デバッグモードでの実行（トレース出力機能が有効）
    String ARG_OPT4="-debug";           //###  デバッグモードでの実行（トレース出力機能が有効）
    String ARG_OPT5="-s";           //###  顧客ステータス退会ファイル名

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
    if (args.length > 4) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

      return Rtn_NG;
    }

    //#  引数格納変数初期化
    String OPTION1="";
    String OPTION2="";
    String OPTION3="";
    String OPTION5="";

    //#  引数格納
    for (int i=0;i< args.length;i++){
      if (Objects.equals(args[i], ARG_OPT3)||Objects.equals(args[i], ARG_OPT4)){
        OPTION3 = args[i];
        continue;
      } else if (Objects.equals(StringUtils.substring(args[i],0,2), ARG_OPT1)){
        OPTION1 = args[i];
        continue;
      }else if (Objects.equals(StringUtils.substring(args[i],0,2), ARG_OPT2)){
        OPTION2 = args[i];
        continue;
      }else if (Objects.equals(StringUtils.substring(args[i],0,2), ARG_OPT5)){
        OPTION5 = args[i];
        continue;
      }else{
        continue;
      }
    }

    //###########################################
    //#  ファイルが存在しなければ正常終了
    //###########################################
    String IN_FILE = StringUtils.substring(OPTION1,2);
    if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + IN_FILE)){
      System.out.println(IN_FILE);
    }else{
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理対象ファイルなし(退会顧客データファイル)　ファイル名=" + IN_FILE).FI());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
      return Rtn_OK;
    }

    IN_FILE = StringUtils.substring(OPTION5,2);
    if (FileUtil.isExistFile(CM_APWORK_DATE + "/" + IN_FILE)){
      System.out.println(IN_FILE);
    }else{
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("処理対象ファイルなし(退会顧客データファイル)　ファイル名=" + IN_FILE).FI());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
      return Rtn_OK;
    }

    //###########################################
    //#  プログラム実行
    //###########################################

    MainResultDto mainResultDto1 = cmBTleavBServiceImpl.main(getExecuteBaseParam().add(OPTION1).add(OPTION2).add(OPTION3).add(OPTION5));
    RTN = mainResultDto1.exitCode;
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //# 結果ファイルの移動
    //###########################################
    String OUT_FILE = StringUtils.substring(OPTION2,2);
    FileUtil.mvFile(CM_APWORK_DATE + "/" + OUT_FILE,CM_APRESULT+ "/" + OUT_FILE);

    //###########################################
    //#  終了メッセージをAPログに出力
    //###########################################
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
    return Rtn_OK;

  }
}
