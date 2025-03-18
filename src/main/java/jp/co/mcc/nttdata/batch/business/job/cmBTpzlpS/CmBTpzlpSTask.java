package jp.co.mcc.nttdata.batch.business.job.cmBTpzlpS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABgdatB.CmABgdatBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*

        -------------------------------------------------------------------------------
            名称          ：  ポイント残高表（期間限定ポイント）ファイル作成
            プログラムID  ：  cmBTpzlpS040

            【処理概要】
             ポイント残高表（期間限定ポイント）ファイル作成
             を行うものである。

            【引数説明】
               -DEBUG   :  デバッグモードでの実行（トレース出力機能が有効）
               -debug   :  デバッグモードでの実行（トレース出力機能が有効）

            【戻り値】
               10     ：  正常
               49     ：  警告
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 6.1

            改定履歴
              40.00 : 2023/03/17 SSI.申：MCCM 初版
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2022 NTT DATA CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTpzlpSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmABgdatBServiceImpl cmABgdatBServiceImpl;

    @Override
    public int taskExecuteCustom(String[] args) {

//        ###########################################
//        #  プログラムIDを環境変数に設定
//        ###########################################

        CM_MYPRGNAME="ポイント残高表（期間限定）作成";

//        ###########################################
//        #  開始メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

//        ###########################################
//        #  APログ出力関数
//        ###########################################

//        ###########################################
//        #  日付・時刻取得
//        ###########################################

//          システム日付と時刻
        String SYS_YYYYMMDDHHMMSS = DateUtil.getYYYYMMDDHHMMSS();

//        ###########################################
//        #  定数定義
//        ###########################################

//          戻り値
        Rtn_OK=10;
        Rtn_NG=49;

//          引数定義
        String ARG_OPT1="-DEBUG";                       //  デバッグモードでの実行（トレース出力機能が有効）
        String ARG_OPT2="-debug";                       //  デバッグモードでの実行（トレース出力機能が有効）

//          出力ファイル

        String PZLP_O="POINT_LIST_K";

//          sqlファイル名
        String SQLFile="cmBTpzlpS";

//        ###########################################
//        #  DB接続先
//        ###########################################

//        ###########################################
//        #  稼動ディレクトリ決定
//        ###########################################

        setCM_APWORK_DATE();

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  引数の格納
//        ###########################################

        if (args.length > 1) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//          引数格納変数初期化

        String OPTION1="";

//          引数格納

        for (String arg : args) {
            if (arg.equals(ARG_OPT1) || arg.equals(ARG_OPT2)){
                OPTION1 = arg;
            } else {
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [定義外の引数(" + arg + ")]").FE());
                cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                return Rtn_NG;
            }
        }

//        ###########################################
//        #  バッチ処理日付取得
//        ###########################################

        MainResultDto cmABgdatBResultDto = cmABgdatBServiceImpl.main(getExecuteBaseParam());

        String BAT_YYYYMMDD = cmABgdatBResultDto.result;

        if (cmABgdatBResultDto.exitCode != Rtn_OK){
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("バッチ処理日付取得エラー").FE());
            return Rtn_NG;
        }

//        ###########################################
//        #  バッチ処理日付(前月)の取得
//        ###########################################

        ShellExecuteDto shellExecuteDto1 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_01")
                .defaultEvn(this)
                .addEvn("BAT_YYYYMMDD",BAT_YYYYMMDD)
                .execute();

        String LAST_MONTH_YYYYMM = shellExecuteDto1.result.substring(0,6);

//        ###########################################
//        #  ポイント残高表（期間限定ポイント）ファイル出力
//        ###########################################

//         SQL実行結果をファイルに出力
//         出力ファイル名

        ShellExecuteDto shellExecuteDto2 = ShellClientManager.getSqlPlusExecuteDto(CM_MYPRGID + "_02")
                .defaultEvn(this)
                .addEvn("SQLFile",SQLFile)
                .addEvn("LAST_MONTH_YYYYMM",LAST_MONTH_YYYYMM)
                .addEvn("PZLP_O",PZLP_O)
                .addEvn("SYS_YYYYMMDDHHMMSS",SYS_YYYYMMDDHHMMSS)
                .execute();

        if (shellExecuteDto2.RTN0()){ // 戻り値が0でない場合は異常終了
            String SQL_CD = FileUtil.SQL_CD_ORA_STR(FileUtil.readFile(CM_APWORK_DATE + PZLP_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv"));
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("期間限定ポイント残高取得処理に失敗しました。SQLCODE=" + SQL_CD).FW());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

//          改行コードをCRLFに変換

        FileUtil.writeFile(CM_APWORK_DATE + "/" + PZLP_O + "_" + SYS_YYYYMMDDHHMMSS + ".csv",FileUtil.readFile(CM_APWORK_DATE + PZLP_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv").replaceAll("\\$","\r"));

        FileUtil.deleteFile(CM_APWORK_DATE + "/" + PZLP_O + "_" + SYS_YYYYMMDDHHMMSS + "_1.csv");

//        ###########################################
//        #  終了メッセージをAPログに出力
//        ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;

    }

}
