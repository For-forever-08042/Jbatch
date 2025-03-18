package jp.co.mcc.nttdata.batch.business.com.cmABaplwB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.ParamsExecuteDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_main;
import static jp.co.mcc.nttdata.batch.business.com.cmABfuncL.constanst.CmABfuncLConstant.dbg_aplogwrite;
import static jp.co.mcc.nttdata.batch.business.com.cmABfuncL.constanst.CmABfuncLConstant.dbg_convut2sj;

/******************************************************************************/
/*   プログラム名	： ＡＰログ出力処理（cmABaplw.c）                     */
/*                                                                            */
/*   【処理概要】                                                             */
/*       指定されたメッセージをＡＰログファイルに出力する。                   */
/*                                                                            */
/*   【引数説明】                                                             */
/*      -IメッセージＩＤ  (３桁）                                             */
/*      -Fメッセージ種別  (１桁）                                             */
/*      -M出力メッセージ                                                      */
/*      -PモジュールＩＤ  (９桁）                                             */
/*      -Sモジュール名称                                                      */
/*      -Eモジュール名称                                                      */
/*                                                                            */
/*   【戻り値】                                                               */
/*      10       ： 正常                                                      */
/*      50～99   ： 異常                                                      */
/*                                                                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
@Service
@Slf4j
public class CmABaplwBServiceImpl extends CmABfuncLServiceImpl implements CmABaplwBService {

    @Override
    public MainResultDto main(int argc, String[] argv) {
        argv[0] = "cmABaplwB";
        closeLog();
        int rtn_cd;
        String penv;
        long lret, lret2 = 0;
        String p_modbk = null;
        int nl_cnt;
        int c;
        AplwParam param = new AplwParam();


        /*#####################################*/
        /*	プログラム名取得処理呼び出し   */
        /*#####################################*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            return functionCode(201);
        }

        /*#####################################*/
        /*	バッチデバッグ開始処理呼び出し */
        /*#####################################*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            resultData.functionCode = 202;
            return functionCode(202);
        }

        C_DbgStart("CmABaplwB");

        /*#####################################*/
        /*	主処理                         */
        /*#####################################*/

        if (DBG_main) {
            C_DbgMsg("CmABaplwB : %s", "主処理");
        }

        /* 引数解析処理の呼び出し */
        rtn_cd = param_proc(argc, argv, param);

        if (DBG_main) {
            C_DbgMsg("CmABaplwB : param_proc=%d", rtn_cd);
            C_DbgMsg("CmABaplwB : p_msgid=%s", param.p_msgid);
            C_DbgMsg("CmABaplwB : p_msgty=%s", param.p_msgty);
            C_DbgMsg("CmABaplwB : p_flg=%s", param.p_flg);
            C_DbgMsg("CmABaplwB : p_msg=%s", param.p_msg);
            C_DbgMsg("CmABaplwB : p_mod=%s", param.p_mod);
            C_DbgMsg("CmABaplwB : p_modname=%s", param.p_modname);
        }
        if (rtn_cd != C_const_OK) {
            return functionCode(204);
        }

        /* メッセージ */
        if (strlen(param.p_msg) == 0) {
            /* 引数にメッセージが指定されていない場合、標準入力から取得する */
            InputStream is = new ByteArrayInputStream(argv[1].getBytes(StandardCharsets.UTF_8));
            try {
                if (is.available() <= 0) {
                    param.p_msg = "（メッセージ指定なし）";
                } else {
                    nl_cnt = 0;

                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    int lineCount = 0;
                    while ((line = br.readLine()) != null) {
                        if (StringUtils.isEmpty(param.p_msg)) {
                            param.p_msg = line;
                        } else {
                            param.p_msg += line;
                        }
                        param.p_msg += "\n";
                        nl_cnt++;
                        /* 最大行数は３０ */
                        if (nl_cnt == 30) {
                            break;
                        }
                    }
                }

                /* 最後の改行コードを削除する(改行コードだけ以外) */
                if (strlen(param.p_msg) > 1) {
                    if (param.p_msg.endsWith("\n")) {
                        param.p_msg = param.p_msg.substring(0, param.p_msg.length() - 1);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (DBG_main) {
                C_DbgMsg("CmABaplwB : p_msg(stdin)=[%s]", param.p_msg);
                C_DbgMsg("CmABaplwB : p_msg(stdin)=[%d]", param.p_msg.length());
            }
        }
        if (DBG_main) {
            C_DbgMsg("CmABaplwB : p_msg=[%s]", param.p_msg);
        }
        /* モジュール名 */
        if (StringUtils.isEmpty(param.p_mod)) {
            //penv = this.getClass().getName().substring(0, 9);
            penv = getenv(CM_MYPRGID);
            if (StringUtils.isNotEmpty(penv)) {
                param.p_mod = penv.substring(0, 9);
            } else {
                param.p_mod = "---------";
            }
        }

        /* メッセージの出力 */
        Cg_Program_Name = param.p_mod;

        if (StringUtils.isEmpty(param.p_flg)) {
            param.p_flg = " ";
        }

        dbg_aplogwrite = 1;
        dbg_convut2sj = 1;
        rtn_cd = C_APLogWrite(param.p_msgid, param.p_msg, param.p_flg,
                null, null, null, null, null, null);
        if (DBG_main) {
            C_DbgMsg("CmABaplwB : C_APLogWrite=%d", rtn_cd);
            C_DbgMsg("CmABaplwB : C_APLogWritemsg=[%s]", param.p_msg);
        }
        dbg_aplogwrite = 0;
        dbg_convut2sj = 0;

        if (rtn_cd != C_const_OK) {
            return functionCode(204);
        }

        Cg_Program_Name = p_modbk;

        C_DbgEnd("CmABaplwB", rtn_cd, 0, 0);

        /*#####################################*/
        /*	バッチデバッグ終了処理呼び出し */
        /*#####################################*/
        C_EndBatDbg();

        return functionCode(C_const_OK);
    }

    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        return super.APLOG_WT(msgid, msgidsbt, dbkbn, param1, param2, param3, param4, param5, param6);
    }

    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
        return super.APLOG_WT_903(param1, param2, param3);
    }

    /**
     * 引数解析処理
     *
     * @param argc  プログラム起動時の引数
     * @param argv  プログラム起動時の引数
     * @param param 解析結果を格納する
     * @return 0： 正常; 1： 異常
     */
    public int param_proc(int argc, String[] argv, AplwParam param) {
        int i = 0;
        int se_flg = 0; /* 開始終了区分 */
        int ret = 0;
        String wk_msgid = null;
        String[] wk_flg = new String[2];
        String[] wk_format = new String[2];
        IntegerDto sts = new IntegerDto();


        C_DbgMsg("CmABaplwB : param_proc %s\n", "start");

        for (i = 1; i < argc; i++) {
            C_DbgMsg("CmABaplwB : i=%d\n", i);
            if ("-debug".equals(argv[i]) || "-DEBUG".equals(argv[i])) {
                /* 処理なし */
                continue;
            }

            if (memcmp(argv[i], "-I", 2) == 0) {
                C_DbgMsg("CmABaplwB : param=[%s]\n", "-I");

                if (argv[i].length() > 4) {
                    param.p_msgid = memcpy(param.p_msgid, argv[i].substring(2), 3);
                    C_DbgMsg("CmABaplwB : p_msgid=[%s]\n", param.p_msgid);
                }
            }

            if (memcmp(argv[i], "-F", 2) == 0) {
                param.p_msgty = String.valueOf(argv[i].charAt(2));
                if (argv[i].length() > 2) {
                    if (argv[i].charAt(2) == 'E') {
                        param.p_flg = C_const_APLOG_flg_E;
                    } else if (argv[i].charAt(2) == 'W') {
                        param.p_flg = C_const_APLOG_flg_W;
                    } else if (argv[i].charAt(2) == 'I') {
                        param.p_flg = C_const_APLOG_flg_N;
                    } else {
                        param.p_flg = C_const_APLOG_flg_L;
                    }
                } else {
                    param.p_flg = "L";
                }
            }

            if (memcmp(argv[i], "-M", 2) == 0) {
                C_DbgMsg("CmABaplwB : param=[%s]", "-M");
                if (argv[i].length() > 2) {
                    param.p_msg = memcpy(param.p_msg, argv[i].substring(2), argv[i].length() - 2);
                    C_DbgMsg("CmABaplwB : p_msg=[%s]", param.p_msg);
                }
            }

            if (memcmp(argv[i], "-P", 2) == 0) {
                if (argv[i].length() > 2) {
                    param.p_mod = memcpy(param.p_mod, argv[i].substring(2), 9);
                }
            }

            if (memcmp(argv[i], "-S", 2) == 0) {
                if (argv[i].length() > 2) {
                    param.p_modname = memcpy(param.p_modname, argv[i].substring(2), 150);
                    se_flg = 1;
                }
            }

            if (memcmp(argv[i], "-E", 2) == 0) {
                if (argv[i].length() > 2) {
                    param.p_modname = memcpy(param.p_modname, argv[i].substring(2), 150);
                    se_flg = 9;
                }
            }
        }

        C_DbgMsg("CmABaplwB : param_proc %s", "argv loop end");

        if (se_flg == 1) {
            wk_msgid = "102";
        }

        if (se_flg == 9) {
            if ("I".equals(param.p_msgty)) {
                wk_msgid = "103";
            } else if ("W".equals(param.p_msgty)) {
                wk_msgid = "703";
            } else if ("E".equals(param.p_msgty)) {
                wk_msgid = "912";
            } else {
                wk_msgid = "103";
            }
        }

        if (StringUtils.isNotEmpty(wk_msgid)) {
            param.p_msgid = wk_msgid;
            sts.arr = 0;
            ret = C_GetAPLogFmt(wk_msgid, 0, null, wk_flg, wk_format, sts); /* APログフォーマット取得 */

            C_DbgMsg("CmABaplwB : C_GetAPLogFmt=%d", ret);

            param.p_msg = sprintf(wk_format[0], param.p_modname);

            C_DbgMsg("CmABaplwB : param->p_msgid=[%s]", param.p_msgid);
            C_DbgMsg("CmABaplwB : param->p_msgty=[%s]", param.p_msgty);
        }

        if (StringUtils.isEmpty(param.p_msgid)) {
            if ("E".equals(param.p_msgty)) {
                param.p_msgid = "900";
            } else if ("W".equals(param.p_msgty)) {
                param.p_msgid = "700";
            } else if ("I".equals(param.p_msgty)) {
                param.p_msgid = "100";
            } else {
                param.p_msgid = "100";
            }
        }

        C_DbgMsg("CmABaplwB : param->p_msgid=[%s]", param.p_msgid);
        C_DbgMsg("CmABaplwB : param_proc %s", "end");

        return C_const_OK;
    }

}
