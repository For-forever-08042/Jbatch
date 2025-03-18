package jp.co.mcc.nttdata.batch.business.job.cmABbase;

import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_APNG;

/**
 * #-------------------------------------------------------------------------------
 * #    名称          ：  バックアップコントローラ
 * #    プログラムID  ：  cmABbkclS
 * #
 * #    【処理概要】
 * #        下記ＤＢサーバ向けのバックアップ開始ファイルを作成し
 * #        各ＤＢサーバからバックアップ完了ファイルが作成されるまで監視する。
 * #        対象ＤＢサーバ
 * #        顧客管理ＤＢ＃１
 * #        顧客制度ＤＢ＃１
 * #
 * #    【引数説明】
 * #      -h対象ホスト名                   ：バックアップ対象サーバのホスト名（必須）
 * #      -dyyyymmdd                       ：処理日付（任意）
 * #      -wmmm                            ：最大処理時間（分）（任意）
 * #
 * #    【戻り値】
 * #        10     ：  正常
 * #        49     ：  異常
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 5
 * #
 * #    改定履歴
 * #      1.00 :2012/12/20 SSI.Suyama  ： 初版
 * #      2.00 :2014/07/22 SSI.上野    ： 最大処理時間変更 2H → 3H
 * #     40.00 :2022/09/30 SSI.川内    ： MCCM初版
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 **/
@Slf4j
@Component
public class CmBTBaseTask extends NtBasicTask {

    @Override
    public void setCM_APWORK_DATE() {
    }

    @Override
    public int taskExecuteCustom(String[] args) {

        // プログラムIDを環境変数に設定
//        CM_MYPRGID = args[0];
        //CM_MYPRGID = args[0].substring(0, 9);
        //args[0] = CM_MYPRGID;

        KshScriptTypes kshScriptTypes = KshScriptTypes.getType(CM_MYPRGID);
        if (kshScriptTypes == KshScriptTypes.NULL) {
            log.error(" 起動対象モジュールＩＤが誤っています");
        } else {
            Object comBusinessService = SpringUtils.getBean(kshScriptTypes.clazz);
            if (comBusinessService instanceof ComBusinessService) {
                return ((ComBusinessService) comBusinessService).main(args.length, args).exitCode;
            }

            log.error("起動対象モジュールＩＤが誤っています");
        }

//        起動対象モジュールＩＤが誤っています
        return C_const_APNG;
    }


}
