package jp.co.mcc.nttdata.batch.business.com.cmABgdatB;

import jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.ParamsExecuteDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_main;

@Service
public class CmABgdatBServiceImpl extends CmABfuncLServiceImpl implements CmABgdatBService {


    @Override
    public MainResultDto main(int argc, String[] argv) {
        argv[0]= "cmABgdatB";
        closeLog();
        /*-------------------------------------*/
        /*	ローカル変数定義               */
        /*-------------------------------------*/
        int rtn_cd = 0;                /** 関数戻り値               **/
        IntegerDto rtn_status = new IntegerDto();            /** 関数ステータス           **/
        String msg_param = null;    /** APログ置換文字           **/
        int loop_ix = 0;
        int adjust = 0;
        String w_dbkbn = null;
        StringDto w_server_id = new StringDto(32);
        StringDto w_date = new StringDto(9);

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
            // APLOG_WT_903(msg_param, rtn_cd, 0);
            APLOG_WT_903(msg_param, new StringDto(), new StringDto());
            return  exit(C_const_APNG);
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

        C_DbgStart(Cg_Program_Name);



        /*#####################################*/
        /*      主処理                         */
        /*#####################################*/

        if (DBG_main) {
            C_DbgMsg("cmABgdatB : %s\n", "主処理");
        }

        /*#####################################*/
        /*      引数を解析する                 */
        /*      指定区分のセット               */
        /*#####################################*/

        adjust = 0;

        if (argc > 1) {
            for (loop_ix = 1; loop_ix < argc; loop_ix++) {
                if (strcmp(argv[loop_ix], "-debug") == 0
                        || strcmp(argv[loop_ix], "-DEBUG") == 0) {
                    /* デバッグオプション */
                    continue;
                }

                if (strcmp(argv[loop_ix], "-DY") == 0) {
                    adjust = -1; /* 前日指定 */
                } else if (strcmp(argv[loop_ix], "-DY2") == 0) {
                    adjust = -2; /* ２日前指定 */
                } else if (strcmp(argv[loop_ix], "-DY3") == 0) {
                    adjust = -3; /* ３日前指定 */
                } else if (strcmp(argv[loop_ix], "-DY4") == 0) {
                    adjust = -4; /* ４日前指定 */
                } else if (strcmp(argv[loop_ix], "-DY5") == 0) {
                    adjust = -5; /* ５日前指定 */
                } else if (strcmp(argv[loop_ix], "-DY6") == 0) {
                    adjust = -6; /* ６日前指定 */
                } else if (strcmp(argv[loop_ix], "-DY7") == 0) {
                    adjust = -7; /* ７日前指定 */
                } else if (strcmp(argv[loop_ix], "-DT") == 0) {
                    adjust = 1; /* 翌日指定 */
                } else if (strcmp(argv[loop_ix], "-D2") == 0) {
                    adjust = 2; /* ２日後指定 */
                } else if (strcmp(argv[loop_ix], "-D3") == 0) {
                    adjust = 3; /* ３日後指定 */
                } else if (strcmp(argv[loop_ix], "-D4") == 0) {
                    adjust = 4; /* ４日後指定 */
                } else if (strcmp(argv[loop_ix], "-D5") == 0) {
                    adjust = 5; /* ５日後指定 */
                } else if (strcmp(argv[loop_ix], "-D6") == 0) {
                    adjust = 6; /* ６日後指定 */
                } else if (strcmp(argv[loop_ix], "-D7") == 0) {
                    adjust = 7; /* ７日後指定 */
                } else {
                    /* ＡＰログ出力 */
                    memset(msg_param, 0x00, sizeof(msg_param));
                    msg_param = sprintf(msg_param, "引数が誤っています（%s）", argv[loop_ix]);
                    APLOG_WT("700", 0, null, msg_param, 0, 0, 0, 0, 0);
                    /*exit(C_const_APNG);*/
                }
            }
        }
        if (DBG_main) {
            C_DbgMsg("cmABgdatB : adjust=%d\n", adjust);
        }


        /* 環境変数から接続先ＤＢを取得する */
        rtn_cd = getEnv("CM_SERVER_ID", w_server_id, 1);
        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdatB : getenv ng (%s)\n", "CM_SERVER_ID");
            }
            return exit(C_const_APNG);
        }


//        memset(w_dbkbn, 0x00, sizeof(w_dbkbn));
        if (w_server_id.arr.charAt(1) == 'D') {
            /* 自身のＤＢに接続 */
            w_dbkbn = memcpy(w_dbkbn, w_server_id.arr, 2);
        } else {
            /* 顧客制度ＤＢに接続 */
            w_dbkbn = memcpy(w_dbkbn, "SD", 2);
        }

        /* ＤＢに接続 */
        rtn_cd = C_OraDBConnect(w_dbkbn, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdatB : connect error(%s)\n", w_dbkbn);
            }
            /* 処理を終了する */
            /* ＡＰログ出力 */
            APLOG_WT_903("C_OraDBConnect", rtn_cd, rtn_status);
            return exit(C_const_APNG);
        }

        /* 日付の取得 */
//        memset(w_date, 0x00, sizeof(w_date));
        rtn_cd = C_GetBatDate(adjust, w_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_main) {
                C_DbgMsg("cmABgdatB : C_GetBatDate error(rtn_cd=%d)\n", rtn_cd);
            }
            /* 処理を終了する */
            /* ＡＰログ出力 */
            APLOG_WT_903("C_GetBatDate", rtn_cd, rtn_status);
            return exit(C_const_APNG);
        }

        if (DBG_main) {
            C_DbgMsg("cmABgdatB : date=%s\n", w_date);
        }
        /* 日付を標準出力する */
        printf("%s", w_date.arr);



        /*#####################################*/
        /*      終了処理                       */
        /*#####################################*/
        C_DbgEnd(Cg_Program_Name, rtn_cd, rtn_status.arr, 0);

        /*#####################################*/
        /*	バッチデバッグ終了処理呼び出し */
        /*#####################################*/
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);

//        return 0;
    }

    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2,
                        Object param3, Object param4, Object param5, Object param6) {
        /*#####################################*/
        /*	ローカル変数定義               */
        /*#####################################*/
        String[] out_flg = new String[2];                /** APログフラグ             **/
        String[] out_format = new String[2];       /** APログフォーマット       **/
        IntegerDto out_status;                /** フォーマット取得結果     **/
        int rtn_cd;                    /** 関数戻り値               **/

        if (EnvironmentConstant.DBG_APLOG_WT) {
            C_DbgStart("APLOG_WT処理");
        }

        /*#####################################*/
        /*	APログフォーマット取得処理     */
        /*#####################################*/
//        out_flg = null;
//        out_format = null;
        out_status = new IntegerDto();
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        if (EnvironmentConstant.DBG_APLOG_WT) {
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = ret=%d\n", rtn_cd);
            C_DbgMsg("*** APLOG_WT *** out_status = %d\n", out_status);
            C_DbgMsg("*** APLOG_WT *** out_flg = %s\n", out_flg);
            C_DbgMsg("*** APLOG_WT *** out_format = %s\n", out_flg);
        }
        /*--------------------------------------------------------------------*/

        /*--------------------------------------------------------------------*/

        /*#####################################*/
        /*	APログ出力処理                 */
        /*#####################################*/
        rtn_cd = C_APLogWrite(msgid, out_format, out_flg,
                param1, param2, param3, param4, param5, param6);

        if (EnvironmentConstant.DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        }/*---------------------------------------------------------------------*/

        return (C_const_OK);

    }

    @Override
    public int getEnv(String p_key, StringDto p_val, int logflg) {
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


    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
        int ret;

        ret = APLOG_WT("903", 0, null,param1, param2, param3, null, null, null);
        return ret;
    }


}
