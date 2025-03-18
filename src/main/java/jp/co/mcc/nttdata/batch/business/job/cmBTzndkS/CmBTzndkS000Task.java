package jp.co.mcc.nttdata.batch.business.job.cmBTzndkS;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  店別ポイント集計シェル(圧縮を行う)
 * #    プログラムID  ：  cmBTzndkS
 * #
 * #    【処理概要】
 * #       店別ポイント集計処理にて作成された会計帳票ファイルの圧縮を実行する。
 * #
 * #    【引数説明】
 * #       -dバッチ処理日付          : （必須）バッチ処理日付
 * #
 * #    【戻り値】
 * #       10     ：  正常
 * #       20-44  ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2013/01/29 SSI.本田 ：初版
 * #      2.00 :   2013/03/28 SSI.本田 ：会計データ圧縮ファイル名はUTF8で出力
 * #      3.00 :   2021/02/22 NDBS.緒方 : 期間限定ポイント集計表追加
 * #      4.00 :   2021/04/19 SSI.上野 ：通常ポイント集計表追加
 * #                                     ポイントクリア予定表削除
 * #      5.00 :   2021/10/01 SSI.張 ：Mk相互取引集計追加
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Component
public class CmBTzndkS000Task extends NtBasicTask {

  public static final String LANG_KEY = "LANG";
  public String LANG;
  public String LANG_BK;

  @Autowired
  CmABaplwBServiceImpl cmABaplwBServiceImpl;

  //###########################################
  //#  定数定義
  //###########################################
  int Rtn_OK=10;
  int Rtn_NG=99;

  //# 削除する圧縮ファイル名の拡張子
  String FILE_KAKUCYOUSI=".zip";
  //
  //# ファイル名の拡張子
  String FILE_KAKUCYOUSI2=".csv";
  //
  //# 会計帳票ファイル名(頭)
  String CSV_KAIKEI_HEADER_FILE1="通常ポイント集計表_";
  String CSV_KAIKEI_HEADER_FILE2="ポイント券集計表_";
  String  CSV_KAIKEI_HEADER_FILE4="期間限定ポイント集計表_";
  String CSV_KAIKEI_HEADER_FILE5="MK相互取引集計表_";
  //
  //# 圧縮ファイル名
  String ZIP_KAIKEI_FILE="KAIKEI.zip";

  int RTN;

  @Override
  public int taskExecuteCustom(String[] args) {
    // プログラムIDを環境変数に設定
    CM_MYPRGNAME = "cmBTzndkS000";
    setenv(CmABfuncLServiceImpl.CM_MYPRGNAME, CM_MYPRGNAME);

    // 開始メッセージをAPログに出力
    cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

    //###########################################
    //#  引数定義
    //###########################################
    String ARG_OPT1="-d";

    //###########################################
    //#  引数の数チェック
    //###########################################
    if (args.length > 1) {
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return Rtn_NG;
    }

    //#  引数格納変数初期化
    String OPTION1="";

    //#  引数格納
    if (args != null){
      for (int i = 0; i < args.length; i++) {
        String ARG_VALUE = args[i];
        if (Objects.equals(StringUtils.substring(ARG_VALUE, 0, 2), ARG_OPT1)) {
          OPTION1 = ARG_VALUE;
        }
      }
    }

    //###########################################
    //#  バッチ処理日取得
    //###########################################
    //BAT_YYYYMMDD=`echo ${OPTION1} | cut -c3-`
    //BAT_YYYYMM=`echo $BAT_YYYYMMDD | cut -c1-6`
    String BAT_YYYYMMDD = StringUtils.substring(OPTION1,2);
    String BAT_YYYYMM = StringUtils.substring(BAT_YYYYMMDD,0,6);

    //###########################################
    //#  LANG関係を保存し、SJISに変換
    //###########################################
    LANG_BK = getenv(LANG_KEY);
    LANG = "ja_JP.SJIS";
    setenv(LANG_KEY, LANG);

    //################################################
    //#  会計帳票の圧縮ファイルを作成
    //################################################
    //# 会計帳票ファイルのファイル名


    String CSV_KAIKEI_FILE1 = CSV_KAIKEI_HEADER_FILE1 + BAT_YYYYMM + FILE_KAKUCYOUSI2;
    String CSV_KAIKEI_FILE2 = CSV_KAIKEI_HEADER_FILE2 + BAT_YYYYMM + FILE_KAKUCYOUSI2;
    String CSV_KAIKEI_FILE4 = CSV_KAIKEI_HEADER_FILE4 + BAT_YYYYMM + FILE_KAKUCYOUSI2;
    String CSV_KAIKEI_FILE5 = CSV_KAIKEI_HEADER_FILE5 + BAT_YYYYMM + FILE_KAKUCYOUSI2;

    //# ポイント集計表を圧縮
    int zipresult =ZipUtil.zipFile(CM_APWORK_DATE+"/"+ZIP_KAIKEI_FILE,CM_APWORK_DATE+"/"+CSV_KAIKEI_FILE1);
    if (zipresult!=0){
      RESET_LANG();
      RTN = 21;
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return RTN;
    }

    //# ポイント券集計表を圧縮
    int zipresult02 =ZipUtil.zipFile(CM_APWORK_DATE+"/"+ZIP_KAIKEI_FILE,CM_APWORK_DATE+"/"+CSV_KAIKEI_FILE2);
    if (zipresult02!=0){
      RESET_LANG();
      RTN = 22;
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return RTN;
    }



    //# 期間限定ポイント集計表を圧縮
    int zipresult04 =ZipUtil.zipFile(CM_APWORK_DATE+"/"+ZIP_KAIKEI_FILE,CM_APWORK_DATE+"/"+CSV_KAIKEI_FILE4);
    if (zipresult04!=0){
      RESET_LANG();
      RTN = 24;
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return RTN;
    }

    //# ポMK相互取引集計表を圧縮
    int zipresult05 =ZipUtil.zipFile(CM_APWORK_DATE+"/"+ZIP_KAIKEI_FILE,CM_APWORK_DATE+"/"+CSV_KAIKEI_FILE5);
    if (zipresult05!=0){
      RESET_LANG();
      RTN = 25;
      cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
      return RTN;
    }

    //###########################################
    //#  終了メッセージをAPログに出力
    //###########################################
    RESET_LANG();
    cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

    return Rtn_OK;
  }

  public void RESET_LANG() {
    LANG = LANG_BK;
    setenv(LANG_KEY, LANG);
  }
}
