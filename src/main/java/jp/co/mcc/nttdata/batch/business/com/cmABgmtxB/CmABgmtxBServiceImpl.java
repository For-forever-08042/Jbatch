package jp.co.mcc.nttdata.batch.business.com.cmABgmtxB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;

import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_APLOG_WT;
import static jp.co.mcc.nttdata.batch.business.com.EnvironmentConstant.DBG_main;
/******************************************************************************/
/*   プログラム名	： パラメータファイル名取得処理（cmABgmtx.c）         */
/*                                                                            */
/*   【処理概要】                                                             */
/*       最新バージョンのパラメータファイル名を標準出力に出力する。           */
/*                                                                            */
/*   【引数説明】                                                             */
/*      パラメータファイルＩＤ（必須）                                        */
/*                                                                            */
/*   【戻り値】                                                               */
/*      10      ： 正常                                                       */
/*      99      ： 異常                                                       */
/*                                                                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/
@Service
public class CmABgmtxBServiceImpl extends CmABfuncLServiceImpl implements CmABgmtxBService {


    /*   プログラム名	： パラメータファイル名取得処理（cmABgmtx.c）         */
    public static boolean DBG_getMailTextModName = true;                       /* getMailTextModName    */

    /******************************************************************************/
    /*                                                                            */
    /*	関数名 ： getMailTextModName                                          */
    /*                                                                            */
    /*	書式                                                                  */
    /*	static int  getMailTextModName(char *pname char *ppath)               */
    /*                                                                            */
    /*	【説明】                                                              */
    /*              バージョン番号付きの最新ファイル名の取得                      */
    /*                                                                            */
    /*                                                                            */
    /*	【引数】                                                              */
    /*		char       *    pname      ：ファイルＩＤ (I)                 */
    /*		char       *    dirname    ：ディレクトリ名                   */
    /*		char       *    pname2     ：最新ファイル名 (O)               */
    /*                                                                            */
    /*	【戻り値】                                                            */
    /*              0	： 正常                                               */
    /*              1	： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int getMailTextModName(String pname, String dirname, String[] pname2) {
        LinkedList<File> d;
        File dd;
        int ver1;
        int ver2;
        String[] logbuf = new String[2];
        String wk_pname;

        if (DBG_getMailTextModName) {
            C_DbgMsg("cmABgmtxB : getMailTextModName: %s\n", "start");
            C_DbgMsg("cmABgmtxB : getMailTextModName: pname=[%s]\n", pname);
        }

        strcpy(pname2, "");

        if (DBG_getMailTextModName) {
            C_DbgMsg("cmABgmtxB : getMailTextModName: opendir(%s) start\n", dirname);
        }
        wk_pname = null;
//        memset(wk_pname, 0x00, sizeof(wk_pname));
        d = opendir(dirname);

        if (d == null) {
            /* ディレクトリオープン失敗 */

            if (DBG_getMailTextModName) {
                C_DbgMsg("cmABgmtxB : getMailTextModName: opendir(%s) = NULL\n", dirname);
            }

            sprintf(logbuf, "opendir(%s)", dirname);
            /* ＡＰログを出力(903) */
            APLOG_WT("903", 0, null, logbuf, 0, 0, 0, 0, 0);

            return C_const_NG;
        }
        if (DBG_getMailTextModName) {
            C_DbgMsg("cmABgmtxB : getMailTextModName: opendir(%s) end\n", dirname);
        }

        for (; ; ) {
            dd = readdir(d);
            if (dd == null) break;

            if (strlen(dd.getName()) != 12) continue; /* ファイル名が１２桁以外は対象外 */

            if (memcmp(pname, dd.getName(), 9) != 0) continue; /* 名前(前９桁)が不一致なので対象外 */

            if (DBG_getMailTextModName) {
                C_DbgMsg("cmABgmtxB : getMailTextModName: dd->d_name = %s\n", dd.getName());
            }

            if (strlen(wk_pname) == 0) {
                /* １番目に見つかった場合 */
                wk_pname = strcpy(wk_pname, dd.getName());
                if (DBG_getMailTextModName) {
                    C_DbgMsg("cmABgmtxB : getMailTextModName: wk_pname = %s\n", wk_pname);
                }
            } else {
                /* ２番目以降           */
                /* バージョンを比較する */
                ver1 = atoi(wk_pname.charAt(9));
                ver2 = atoi(dd.getName().charAt(9));

                if (ver1 < ver2) {
                    /* 最新バージョン */
                    wk_pname = strcpy(wk_pname, dd.getName());
                    if (DBG_getMailTextModName) {
                        C_DbgMsg("cmABgmtxB : getMailTextModName: wk_pname = %s\n", wk_pname);
                    }
                }
            }
        }

        closedir(d);

        if (strlen(wk_pname) == 0) {
            /* 該当なし */
            if (DBG_getMailTextModName) {
                C_DbgMsg("cmABgmtxB : getMailTextModName: %s\n", "notfound pname");
            }

            return C_const_NG;
        }

        if (DBG_getMailTextModName) {
            C_DbgMsg("cmABgmtxB : getMailTextModName: %s\n", "end");
        }

        /* 結果をフルパスで格納する */
        strcpy(pname2, dirname);
        strcat(pname2, "/");
        strcat(pname2, wk_pname);

        return C_const_OK;
    }

    @Override
    public MainResultDto main(int argc, String[] argv) {
        argv[0]="cmABgmtxB";
        closeLog();
        /*-------------------------------------*/
        /*	ローカル変数定義               */
        /*-------------------------------------*/
        int rtn_cd;                /** 関数戻り値               **/
        int rtn_status = 0;            /** 関数ステータス           **/
        String msg_param = null;    /** APログ置換文字           **/
        String prm_name = null; /* 起動モジュール名 */
        int loop_ix;
        String[] exec_path = new String[2]; /* 該当ファイル名 フルパスでバージョン番号付き  */
        int ret;
        String penv;
        String param_path = null; /* パラメータファイルのあるディレクトリ        */

        /*#####################################*/
        /*	プログラム名取得処理呼び出し   */
        /*#####################################*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            msg_param = sprintf(msg_param, "%s:getPgname", Cg_Program_Name);
            APLOG_WT("903", 0, null, msg_param, rtn_cd, 0, 0, 0, 0);
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
            APLOG_WT("903", 0, null, msg_param, rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        C_DbgStart("cmABgmtxB");

        /*#####################################*/
        /*	主処理                         */
        /*#####################################*/

        if (DBG_main) {
            C_DbgMsg("cmABgmtxB : %s\n", "主処理");
        }

//        memset(prm_name, 0x00, sizeof(prm_name));

        if (argc > 1) {
            for (loop_ix = 1; loop_ix < argc; loop_ix++) {
                if (strcmp(argv[loop_ix], "-debug") == 0
                        || strcmp(argv[loop_ix], "-DEBUG") == 0) {
                    /* デバッグオプション */
                    continue;
                }

                if (strlen(argv[loop_ix]) == 9 && strlen(prm_name) == 0) {
                    prm_name= strcpy(prm_name, argv[loop_ix]);
                } else {
                    /* ＡＰログ出力 */
                    memset(msg_param, 0x00, sizeof(msg_param));
                    msg_param = sprintf(msg_param, "引数が誤っています（%s）", argv[loop_ix]);
                    APLOG_WT("910", 0, null, msg_param, 0, 0, 0, 0, 0);
                    return exit(C_const_APNG);
                }
            }
        } else {
            /* ＡＰログ出力 */
            APLOG_WT("910", 0, null, "引数が誤っています(引数なし)", 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_main) {
            C_DbgMsg("cmABgmtxB : getExecModName %s\n", "start");
        }

        penv = getenv("CM_MAILTEXT");
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数取得失敗 */

            if (DBG_getMailTextModName) {
                C_DbgMsg("cmABgmtxB : getenv(%s)=NULL\n", "CM_MAILTEXT");
            }

            msg_param = sprintf(msg_param, "getenv(%s)", "CM_MAILTEXT");
            /* ＡＰログを出力(903) */
            APLOG_WT("903", 0, null, msg_param, 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }
        param_path = strcpy(param_path, penv);

        ret = getMailTextModName(prm_name, param_path, exec_path);

        if (DBG_main) {
            C_DbgMsg("cmABgmtxB : getMailTextModName=%d\n", ret);
        }

        if (ret != C_const_OK) {
            /* メールテキストファイル名を取得できない */
            msg_param = sprintf(msg_param, "メールテキストファイル名の取得異常(%s)", prm_name);
            /* ＡＰログを出力(911) */
            APLOG_WT("911", 0, null, msg_param, 0, 0, 0, 0, 0);
            return  exit(C_const_APNG);

        }

        /* 該当ファイル名を標準出力に出力する */
        printf("%s\n", exec_path[0]);

        C_DbgEnd("cmABgmtxB", ret, rtn_status, 0);

        /*#####################################*/
        /*	バッチデバッグ終了処理呼び出し */
        /*#####################################*/
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);
    }

/******************************************************************************/
    /*                                                                            */
    /*	関数名 ： APLOG_WT                                                    */
    /*                                                                            */
    /*	書式                                                                  */
    /*	static int  APLOG_WT( char *msgid, int  msgidsbt, char *dbkbn,        */
    /*                            caddr_t param1, caddr_t param2, caddr_t param3, */
    /*                            caddr_t param4, caddr_t param5, caddr_t param6) */
    /*                                                                            */
    /*	【説明】                                                              */
    /*              ＡＰログ出力を行う                                            */
    /*                                                                            */
    /*                                                                            */
    /*	【引数】                                                              */
    /*		char       *	msgid      ：メッセージＩＤ                   */
    /*		int     	msgidsbt   ：メッセージ登録種別               */
    /*		char       *	dbkbn      ：ＤＢ区分                         */
    /*		caddr_t       	param1     ：置換変数１                       */
    /*		caddr_t       	param2     ：置換変数２                       */
    /*		caddr_t       	param3     ：置換変数３                       */
    /*		caddr_t       	param4     ：置換変数４                       */
    /*		caddr_t       	param5     ：置換変数５                       */
    /*		caddr_t       	param6     ：置換変数６                       */
    /*                                                                            */
    /*	【戻り値】                                                            */
    /*              0	： 正常                                               */
    /*              1	： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        /*#####################################*/
        /*	ローカル変数定義               */
        /*#####################################*/
        String[] out_flg = new String[2];                /** APログフラグ             **/
        String[] out_format = new String[C_const_MsgMaxLen];        /** APログフォーマット       **/
        IntegerDto out_status = new IntegerDto();                /** フォーマット取得結果     **/
        int rtn_cd;                    /** 関数戻り値               **/

        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgStart("APLOG_WT処理");
        }    /*--------------------------------------------------------------------*/

        /*#####################################*/
        /*	APログフォーマット取得処理     */
        /*#####################################*/

//        memset(out_flg, 0x00, sizeof(out_flg));
//        memset(out_format, 0x00, sizeof(out_format));
        out_status.arr = 0;
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtn_cd);
            /*--------------------------------------------------------------------*/
        }

        /*#####################################*/
        /*	APログ出力処理                 */
        /*#####################################*/
        rtn_cd = C_APLogWrite(msgid, out_format, out_flg,
                param1, param2, param3, param4, param5, param6);
        if (DBG_APLOG_WT) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);
    }

    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
//none
        return 0;
    }
}
