package jp.co.mcc.nttdata.batch.business.job.cmBTzndkS;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdldB.CmABgdldBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB.CmBTkzdkBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTsgdkB.CmBTsgdkBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB.CmBTtzdkBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  店別ポイント集計
 * #    プログラムID  ：  cmBTzndkS
 * #
 * #    【処理概要】
 * #      ポイントの残高や収支（発生、利用）を管理するための会計データを作成する処理、
 * #     「店別ポイント集計（cmBTzndkB）」を起動する
 * #
 * #    【引数説明】
 * #       -d処理対象日付          : （任意）処理対象日付。省略時はバッチ処理年月日(前日)とする。
 * #       -DEBUG or -debug        : （任意）デバッグモードでの実行（トレース出力機能）
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       99     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2012/12/04 SSI.吉岡：初版
 * #      2.00 :   2013/01/29 SSI.本田：会計帳票のファイル名変更対応
 * #      3.00 :   2013/03/28 SSI.本田：会計帳票の圧縮ファイル名はUTF8で出力
 * #      4.00 :   2013/09/26 SSI.本田：集計実施日を変更(１日->６日)
 * #      5.00 :   2021/02/22 NDBS.緒方 : 期間限定ポイント対応
 * #      6.00 :   2021/04/19 SSI.上野：通常ポイント対応
 * #      7.00 :   2021/10/01 SSI.張：Mk相互取引集計通常対応
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTzndkSTask extends NtBasicTask {
  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;
  @Autowired
  CmABgdatBServiceImpl cmABgdatBServiceImpl;
  @Autowired
  CmBTtzdkBServiceImpl cmBTtzdkBServiceImpl;
  @Autowired
  CmBTkzdkBServiceImpl cmBTkzdkBServiceImpl;
  @Autowired
  CmBTsgdkBServiceImpl cmBTsgdkBServiceImpl;
  @Autowired
  CmABgdldBServiceImpl cmABgdldBServiceImpl;
  @Autowired
  CmBTzndkS000Task cmBTzndkS000Task;


  //###########################################
  //#  定数定義
  //###########################################
  int Rtn_OK=10;
  int Rtn_NG=99;
  String TBL_ID="ポイント集計表";
  String ZIP_KAIKEI_FILE_KAKUCHOUSHI="_会計データ.zip";
  String ZIP_KAIKEI_FILE="KAIKEI.zip";
    @Override
    public int taskExecuteCustom(String[] args) {

      // プログラムIDを環境変数に設定
      CM_MYPRGNAME = "ポイント集計";
      setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

      // 開始メッセージをAPログに出力
      cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

      //###########################################
      //#  引数の数チェック
      //###########################################
      if (args.length > 2) {
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      //###########################################
      //#  システム日付の判定
      //###########################################
      //#DATE_TODAY=`date +%d`
      MainResultDto cmABgdatBResult = cmABgdatBServiceImpl.main(getExecuteBaseParam());
      String DATE_TODAY = StringUtils.substring(cmABgdatBResult.result,6, 8);
      if (!"06".equals(DATE_TODAY)){
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));
        return Rtn_OK;
      }

      //###########################################
      //#  稼動ディレクトリ決定
      //###########################################
      setCM_APWORK_DATE();

      if (!FileUtil.isExistDir(CM_APWORK_DATE)) {
        if (!FileUtil.createFolder(CM_APWORK_DATE, false)) {
          //    # 作業ディレクトリファイル作成失敗
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
          return Rtn_NG;
        }
      }

      //###########################################
      //#  バッチ処理日取得
      //###########################################
      MainResultDto cmABgdatBResult02 = cmABgdatBServiceImpl.main(getExecuteBaseParam().add("-DY7"));
      if (cmABgdatBResult02.exitCode != Rtn_OK){
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付の取得に失敗しました").FE());
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      String BAT_YYYYMM = StringUtils.substring(cmABgdatBResult02.result,0,5);

      //###########################################
      //#  プログラム実行
      //###########################################
      //# 店別通常ポイント集計 実行
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("店別通常ポイント集計 実行").FI());
      MainResultDto cmBTtzdkBResult = cmBTtzdkBServiceImpl.main(getExecuteBaseParam().add(args));
      if (cmBTtzdkBResult.exitCode != Rtn_OK) {
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      //# 店別期間限定ポイント集計 実行
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("店別期間限定ポイント集計 実行").FI());
      MainResultDto cmBTkzdkBResult = cmBTkzdkBServiceImpl.main(getExecuteBaseParam().add(args));
      if (cmBTkzdkBResult.exitCode != Rtn_OK) {
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      //# MK相互取引集計 実行
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("MK相互取引集計 実行").FI());
      MainResultDto cmBTsgdkBResult = cmBTsgdkBServiceImpl.main(getExecuteBaseParam().add(args));
      if (cmBTsgdkBResult.exitCode != Rtn_OK) {
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      //###########################################
      //#  ファイル削除年月取得
      //###########################################
      MainResultDto cmABgdldBResult = cmABgdldBServiceImpl.main(getExecuteBaseParam().add(TBL_ID));
      if (cmABgdldBResult.exitCode != Rtn_OK) {
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("ポイント集計表ファイル削除年月取得エラー　テーブルID="+TBL_ID+"["+cmABgdldBResult.exitCode+"]").FE());
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      String[] parts = cmABgdldBResult.result.split(" ");
      String DEL_M_DATE = parts[1];

      //###########################################
      //#  会計ファイルの圧縮バッチを起動
      //###########################################
      int cmBTzndkS000Result = cmBTzndkS000Task.main(getExecuteBaseParam().add("-d"+cmABgdatBResult02.result));
      if (cmBTzndkS000Result == 20){
        APLOG_WT("バッチ処理日付取得エラー(cmBTcrccS000)", FE);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      if (cmBTzndkS000Result == 21){
        APLOG_WT("通常ポイント集計表の圧縮エラー", FE);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      if (cmBTzndkS000Result == 22){
        APLOG_WT("ポイント券集計表の圧縮エラー", FE);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      if (cmBTzndkS000Result == 24){
        APLOG_WT("期間限定ポイント集計表の圧縮エラー", FE);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      if (cmBTzndkS000Result == 25){
        APLOG_WT("MK相互取引集計表の圧縮エラー", FE);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
        return Rtn_NG;
      }

      //###########################################
      //#  該当するファイルを削除
      //###########################################
      //# 削除する会計データの圧縮ファイル名
      String DELETE_KOKYAKU_FILENAME = getenv(CmABfuncLServiceImpl.CM_PCRENKEI_KOJINNASHI) + "/" + DEL_M_DATE + ZIP_KAIKEI_FILE_KAKUCHOUSHI;

      //# ファイルを削除
      if (FileUtil.isExistFile(DELETE_KOKYAKU_FILENAME)){
        if (FileUtil.deleteFile(DELETE_KOKYAKU_FILENAME)!=0){
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("保存期限切れ会計データの削除に失敗しました").FW());
        }
      }

      //################################################
      //#  圧縮ファイルをコピー、移動
      //################################################
      //# 会計帳票ファイルの圧縮ファイルをコピー
      if (FileUtil.isExistFile(ZIP_KAIKEI_FILE)){
        FileUtil.mvFile(CM_APWORK_DATE+"/"+ZIP_KAIKEI_FILE,CM_APWORK_DATE+"/"+BAT_YYYYMM+ZIP_KAIKEI_FILE_KAKUCHOUSHI);

        // # 会計帳票ファイルの圧縮ファイルをコピー
        boolean copyresult = FileUtil.copyFile(CM_APWORK_DATE+"/"+BAT_YYYYMM+ZIP_KAIKEI_FILE_KAKUCHOUSHI,getenv(CmABfuncLServiceImpl.CM_APRESULT));
        if (!copyresult){
          cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("会計データの結果フォルダへのコピーに失敗しました").FW());
        }
      }

      //###########################################
      //#  終了メッセージをAPログに出力
      //###########################################
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

      return Rtn_OK;
    }
}
