package jp.co.mcc.nttdata.batch.business.job.cmBTcrdiS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.CmABterbBServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABfzipS.CmABfzipSTask;
import jp.co.mcc.nttdata.batch.business.job.cmBTcfckS.CmBTcfckSTask;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  カード紐付け情報連携シェル
 * #    プログラムID  ：  cmBTcrdiS
 * #
 * #    【処理概要】
 * #        SPSS 向けのカード紐付け情報ファイルを作成する。
 * #        開始メッセージ出力、「cmABteraB」を起動、終了メッセージ出力、戻り値返却。
 * #
 * #    【引数説明】
 * #       -debug ：  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  警告
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      40.00 :  2022/11/16 SSI.田崎 ： MCCM 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2022 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTcrdiSTask extends NtBasicTask {

  @Autowired
  private CmABaplwBServiceImpl cmABaplwBServiceImpl;
  @Autowired
  private CmABterbBServiceImpl cmABterbBServiceImpl;
  @Autowired
  private CmBTcfckSTask cmBTcfckSTask;
  @Autowired
  private CmABfzipSTask cmABfzipSTask;

  //###########################################
  //#  定数定義
  //###########################################
  int Rtn_OK = 10;
  int Rtn_NG = 49;
  private final static String ARG_OPT1="-DEBUG";       //デバッグモードでの実行（トレース出力機能が有効）
  private final static String ARG_OPT2="-debug";       //デバッグモードでの実行（トレース出力機能が有効）
  String SHELL_DB="SD";              //接続先DB区分の指定：SD(顧客制度DB)
  String SHELL_FILENAME="KKMI0100";  //カード紐付け情報ファイル名
  String KINOUID="crdi";  //機能ID
  @Override
  public int taskExecuteCustom(String[] args) {

    //###########################################
    //#  プログラムIDを環境変数に設定
    //###########################################
    CM_MYPRGNAME = "カード紐付け情報連携";

    //###########################################
    //#  開始メッセージをAPログに出力
    //###########################################
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

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
    if (args.length > 1) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //#  変数初期化
    String OPTION1="";

    if (args.length != 0){
      for (int i = 0; i < args.length; i++){
        if (Objects.equals(args[i], ARG_OPT1) ||Objects.equals(args[i], ARG_OPT2)){
          OPTION1 = args[i];
          continue;
        }else{
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数("+ args[i] + ")").FE());
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        }
      }
    }

    //###########################################
    //#  連動ファイル作成
    //###########################################
    MainResultDto mainResultDto1 = cmABterbBServiceImpl.main(getExecuteBaseParam().add("-PcmBTcrdiP").add("-F" + CM_APWORK_DATE +"/"+SHELL_FILENAME+".csv").add("-S" + SHELL_DB).add("-A").add(OPTION1));
    int RTN = mainResultDto1.exitCode;
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイルの作成に失敗しました").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //#  連携ファイルのレコード重複の確認(KKMI0100)
    //###########################################
    RTN = cmBTcfckSTask.main(getExecuteBaseParam().add("-i"+SHELL_FILENAME + ".csv").add("-k" + KINOUID).add("-c1,2,3").add("-n1"));
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連携ファイルのレコード重複の確認に失敗しました["+SHELL_FILENAME + ".csv]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //###########################################
    //# 作成した連動ファイルの圧縮処理
    //###########################################
    RTN = cmABfzipSTask.main(getExecuteBaseParam().add("-O").add(CM_APWORK_DATE).add("-Z").add(SHELL_FILENAME+".zip").add("-D").add(CM_APWORK_DATE).add("-I").add(SHELL_FILENAME+".csv").add("-DEL"));
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("連動ファイルの圧縮に失敗しました["+SHELL_FILENAME + ".csv]").FE());
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
