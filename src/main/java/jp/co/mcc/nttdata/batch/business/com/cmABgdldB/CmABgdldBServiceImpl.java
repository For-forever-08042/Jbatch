package jp.co.mcc.nttdata.batch.business.com.cmABgdldB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_APLOG_WT;
import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_main;
/**
 * /*   プログラム名       ： データ削除日付取得（cmABgdldB.c）
 */
/*                                                                            */
/*   【処理概要】                                                             */
/*       バッチ処理日をもとに該当の削除対象日付を算出し、標準出力に出力する。 */
/*                                                                            */
/*   【引数説明】                                                             */
/*      テーブルID（テーブル削除グループ名）（必須）                          */
/*                                                                            */
/*   【戻り値】                                                               */
/*      10      ： 正常                                                       */
/*      99      ： 異常                                                       */
/*                                                                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 : 2012/10/18 SSI.吉岡      ： 初版                               */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/
@Slf4j
@Service
public class CmABgdldBServiceImpl extends CmABfuncLServiceImpl implements CmABgdldBService {
/******************************************************************************/
    /*                                                                            */
    /*	メイン関数                                                            */
    /*	 int  main(int argc, char** argv)                                     */
    /*                                                                            */
    /*            argc ： 起動時の引数の数                                        */
    /*            argv ： 起動時の引数の文字列                                    */
    /*                                                                            */
    /*	【説明】                                                              */
    /*              削除対象日付を算出し、標準出力に出力する                      */
    /*                                                                            */
    /*	【引数】                                                              */
    /*		プログラムヘッダ参照                                          */
    /*                                                                            */
    /*	【戻り値】                                                            */
    /*              プログラムヘッダ参照                                          */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public MainResultDto main(int argc, String[] argv) {
        argv[0] = "cmABgdldB";

        /*-------------------------------------*/
        /*	ローカル変数定義               */
        /*-------------------------------------*/
        int rtn_cd;                /** 関数戻り値               **/
        IntegerDto rtn_status = new IntegerDto();            /** 関数ステータス           **/
        String msg_param = null;    /** APログ置換文字           **/
        String this_tblgrp = "                                              ";  /** テーブルグループ(入力)   char this_tblgrp[46]; **/
        String base_date = "";                      /** 基準日(入力)             **/
        StringDto out_yyyymmdd = new StringDto();                   /** 保存日(出力)             **/
        StringDto out_yyyymm = new StringDto();                     /** 保存月(出力)             **/
        StringDto out_yyyy = new StringDto();                       /** 保存年(出力)             **/
        StringDto out_yyyy2 = new StringDto();                      /** 参照テーブル保存日(出力) **/
        int loop_ix;
        int sitei_kbn;
        String w_dbkbn = null;
        String[] w_server_id = new String[2];
        String w_date = "         ";

        /*#####################################*/
        /*      開始処理                       */
        /*#####################################*/

        /*#####################################*/
        /*	プログラム名取得処理呼び出し   */
        /*#####################################*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
//            memset(msg_param, 0x00, sizeof(msg_param));
            msg_param = sprintf(msg_param, "%s:getPgname", Cg_Program_Name);
            APLOG_WT_903(msg_param, rtn_cd, 0);
            return exit(C_const_APNG);
        }

        /*#####################################*/
        /*	バッチデバッグ開始処理呼び出し */
        /*#####################################*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力 */
//            memset(msg_param, 0x00, sizeof(msg_param));
            msg_param = sprintf(msg_param, "%s:C_StartBatDbg", Cg_Program_Name);
            APLOG_WT_903("903", rtn_cd, 0);
            return exit(C_const_APNG);
        }
        if (DBG_main) {
            C_DbgStart(Cg_Program_Name);
        }


        /*#####################################*/
        /*      主処理                         */
        /*#####################################*/

        if (DBG_main) {
            C_DbgMsg("cmABgdldB : %s\n", "主処理");
        }

        /*#####################################*/
        /*      引数を解析する                 */
        /*      指定区分のセット               */
        /*#####################################*/

        sitei_kbn = 0;
        memset(this_tblgrp, 0x00, sizeof(this_tblgrp));
        if (argc > 1) {
            for (loop_ix = 1; loop_ix < argc; loop_ix++) {
                if (strcmp(argv[loop_ix], "-debug") == 0
                        || strcmp(argv[loop_ix], "-DEBUG") == 0) {
                    /* デバッグオプション */
                    continue;
                }

                if (strlen(argv[loop_ix]) != 0 &&
                        strlen(argv[loop_ix]) < sizeof(this_tblgrp) &&
                        sitei_kbn == 0) {
                    sitei_kbn = 1;                   /* 引数指定 */
                    this_tblgrp = strcpy(this_tblgrp, argv[loop_ix]);
                } else {
                    /* ＡＰログ出力 */
                    memset(msg_param, 0x00, sizeof(msg_param));
                    msg_param = sprintf(msg_param, "引数が誤っています（%s）", argv[loop_ix]);
                    APLOG_WT("910", 0, null, msg_param, 0, 0, 0, 0, 0);
                    return exit(C_const_APNG);
                }
            }
        }
        if (sitei_kbn == 0) {
            /* ＡＰログ出力 */
            memset(msg_param, 0x00, sizeof(msg_param));
            msg_param = sprintf(msg_param, "必須引数が指定されていません");
            APLOG_WT("910", 0, null, msg_param, 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_main) {
            C_DbgMsg("cmABgdldB : sitei_kbn=%d\n", sitei_kbn);
        }

        /* 環境変数から接続先ＤＢを取得する */
        rtn_cd = getEnv("CM_SERVER_ID", w_server_id, 1);
        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdldB : getenv ng (%s)\n", "CM_SERVER_ID");
            }

            return functionCode(C_const_APNG);
        }

        memset(w_dbkbn, 0x00, sizeof(w_dbkbn));
        if (w_server_id[0].charAt(0) == 'D') {
            /* 自身のＤＢに接続 */
            w_dbkbn = memcpy(w_dbkbn, w_server_id[0], 2);
        } else {
            /* 顧客制度ＤＢに接続 */
            w_dbkbn = memcpy(w_dbkbn, "SD", 2);
        }

        /* ＤＢに接続 */
        rtn_cd = C_OraDBConnect(w_dbkbn, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdldB : connect error(%s)\n", w_dbkbn);
            }
            /* 処理を終了する */
            /* ＡＰログ出力 */
            APLOG_WT_903("C_OraDBConnect", rtn_cd, rtn_status);
            return functionCode(C_const_APNG);
        }

        /* 保存期間取得処理 */
//        memset(out_yyyymmdd, 0x00, sizeof(out_yyyymmdd));
//        memset(out_yyyymm, 0x00, sizeof(out_yyyymm));
//        memset(out_yyyy, 0x00, sizeof(out_yyyy));
//        memset(out_yyyy2, 0x00, sizeof(out_yyyy2));
//        memset(base_date, 0x00, sizeof(base_date));
        base_date = sprintf("%s", "00000000");
        rtn_status.arr = 0;
        rtn_cd = C_GetSaveKkn(this_tblgrp, base_date, out_yyyymmdd,
                out_yyyymm, out_yyyy, out_yyyy2, rtn_status);

        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdldB : C_GetSaveKkn error(rtn_cd=%d)\n", rtn_cd);
            }
            /* 処理を終了する */
            /* ＡＰログ出力 */
            APLOG_WT_903("C_GetSaveKkn", rtn_cd, rtn_status);
            return functionCode(C_const_APNG);
        }

        if (DBG_main) {
            C_DbgMsg("cmABgdldB : date=%s\n", w_date);
        }
        /* 保存期間を標準出力する */
        printf("%s %s %s %s\n", out_yyyymmdd, out_yyyymm, out_yyyy, out_yyyy2);



        /*#####################################*/
        /*      終了処理                       */
        /*#####################################*/
        if (DBG_main) {
            C_DbgEnd(Cg_Program_Name, rtn_cd, rtn_status.arr, 0);
        }
        /*#####################################*/
        /*	バッチデバッグ終了処理呼び出し */
        /*#####################################*/
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);
    }

    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        /*#####################################*/
        /*	ローカル変数定義               */
        /*#####################################*/
        String[] out_flg = new String[2];                /** APログフラグ             **/
        String[] out_format = new String[2];         /** APログフォーマット       **/
        IntegerDto out_status = new IntegerDto();                /** フォーマット取得結果     **/
        int rtn_cd;                    /** 関数戻り値               **/

        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgStart("APLOG_WT処理");
        }    /*--------------------------------------------------------------------*/

        /*#####################################*/
        /*	APログフォーマット取得処理     */
        /*#####################################*/

//        memset(out_flg, 0x00, sizeof(out_flg[0]));
//        memset(out_format, 0x00, sizeof(out_format[0]));
        out_status.arr = 0;
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = ret=%d\n", rtn_cd);
            C_DbgMsg("*** APLOG_WT *** out_status = %d\n", out_status);
            C_DbgMsg("*** APLOG_WT *** out_flg = %s\n", out_flg);
            C_DbgMsg("*** APLOG_WT *** out_format = %s\n", out_flg);
        }    /*--------------------------------------------------------------------*/

        /*#####################################*/
        /*	APログ出力処理                 */
        /*#####################################*/
        rtn_cd = C_APLogWrite(msgid, out_format, out_flg,
                param1, param2, param3, param4, param5, param6);
        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        } /*---------------------------------------------------------------------*/

        return (C_const_OK);
    }

    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
        int ret;
        ret = APLOG_WT("903", 0, null, param1, param2,
                param3, 0, 0, 0);

        return ret;
    }

    @Override
    public int getEnv(String p_key, String[] p_val, int logflg) {
        String penv;
        String msgbuf = null;
        int ret;

        penv = getenv(p_key);

        if (StringUtils.isEmpty(penv)) {
            if (logflg == 1) {
                /* ＡＰログ出力 */
                msgbuf = sprintf(msgbuf, "getenv(%s)", p_key);
                ret = APLOG_WT_903(msgbuf, 0, 0);
                if (ret != C_const_OK) {
                    /* */
                }
            }

            return C_const_NG;
        }

        strcpy(p_val, penv);
        return C_const_OK;
    }
}
