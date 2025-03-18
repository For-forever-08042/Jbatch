package jp.co.mcc.nttdata.batch.business.job.cmBTenupS;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTenupB.CmBTenupBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  マタニティベビー有効期限更新処理
 * #    プログラムID  ：  cmBTenupS
 * #
 * #    【処理概要】
 * #        MMマタニティべビー情報のエントリ情報有効期限更新を行う
 * #       「マタニティベビー有効期限更新（cmBTenupB）」を起動するためのシェル。
 * #        開始メッセージを出力し、「cmBTenupB」を起動
 * #        終了メッセージを出力し、戻り値を返却。
 * #
 * #    【引数説明】
 * #   -DEBUG            :  デバッグモードでの実行（トレース出力機能が有効）
 * #   -debug            :  デバッグモードでの実行（トレース出力機能が有効）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/13 SSI.吉岡  ： 初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTenupSTask extends NtBasicTask {

  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;

  @Autowired
  CmABgdatBServiceImpl cmABgdatBServiceImpl;

  @Autowired
  CmBTenupBServiceImpl cmBTenupBServiceImpl;
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

    String DB_KBN = "SD";
    String SQL_CD ="";

    //#  ｢マタニティベビー有効期限更新｣用ジョブ実績出力用ログファイル名称
    String CM_G16003D = "JBch_G16003D";  //本番用は「JBch_G16003D」

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
    //###########################################
    //#  バッチ処理日前日の取得
    //###########################################
    MainResultDto mainResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY"));
    int RTN = mainResultDto.exitCode;
    if (RTN != Rtn_OK) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());

      return Rtn_NG;
    }
    String BAT_YYYYMMDD = mainResultDto.result;

    //################################################
    //#  処理対象情報をジョブ実績ログファイルに出力
    //################################################
    ShellExecuteDto shellExecuteDto = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID+"_01")
        .addEvn("CONNECT_MD", CONNECT_MD).addEvn("CM_MYPRGID", CM_MYPRGID).addEvn("CM_MYPRGID", CM_MYPRGID).addEvn("BAT_YYYYMMDD", BAT_YYYYMMDD).addEvn("CM_APWORK_DATE", CM_APWORK_DATE).execute();

    if (shellExecuteDto.RTN0()) {
      //# 戻り値が0でない場合は異常終了
      SQL_CD = FileUtil.SQL_CD_ORA_FILE(CM_APWORK_DATE + "/" + CM_MYPRGID + ".log");
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("マタニティベビー有効期限更新対象件数の取得に失敗しました。SQLCODE=" + SQL_CD).FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //# ログファイル初期化
    String DATA_CNT = FileUtil.readFile(CM_APWORK_DATE + "/" + CM_MYPRGID + ".tmp");
    FileUtil.writeFile(CM_JOBRESULTLOG + "/" + CM_G16003D + ".log","        対象顧客番号："+ DATA_CNT + " 件");

    //###########################################
    //#  プログラム実行
    //###########################################

    MainResultDto mainResultDto1 = cmBTenupBServiceImpl.main(getExecuteBaseParam().add(args));
    RTN = mainResultDto1.exitCode;
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
