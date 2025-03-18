package jp.co.mcc.nttdata.batch.business.com.cmABfkbhB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABendeB.CmABendeBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
/******************************************************************************/
/*   プログラム名       ： ファイル会員番号復号補正（cmABfkbhB.c）            */
/*                                                                            */
/*   【処理概要】                                                             */
/*       バッチ処理日をもとに該当の削除対象日付を算出し、標準出力に出力する。 */
/*                                                                            */
/*   【引数説明】                                                             */
/*       -P     ： 暗号化パータン                                             */
/*                    MP1: MK-POS 電文ファイル                                */
/*                    MP2: MK-POS 電文ファイル以外                            */
/*                    SD: 商品DNA、SPSS                                       */
/*                    GL: 群豊                                                */
/*                    NO: 復号化しない                                        */
/*       -i     ： ファイル名                                                 */
/*       -c     ： 区切り文字                                                 */
/*       -n     ： 復号する項番(複数の場合、カンマ区切り)                     */
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
/*      40.00 : 2022/10/25 SSI.張シン ： MCCM初版                             */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/
@Service
public class CmABfkbhBServiceImpl extends CmABfuncLServiceImpl implements CmABfkbhBService {

    @Autowired
    CmABendeBServiceImpl cmABendeBService;
    /*----------------------------------------------------------------------------*/
    /*    トレース出力要否設定（0:不要、1:必要）                                */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */
    boolean DBG_main = true;                           /* main               */
    boolean DBG_APLOG_WT = true;                               /* APLOG_WT           */
    boolean DBG_LOG = true;                        /* デバッグメッセージ出力             */
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_P = "-P";    /* 暗号化パータン                     */
    String DEF_ARG_I = "-i";    /* ファイル名                         */
    String DEF_ARG_C = "-c";    /* 区切り文字                         */
    String DEF_ARG_N = "-n";    /* 復号項目番号(複数の場合、カンマ区切り)    */
    String DEF_ARG_E = "-E";    /* 暗号化の場合のみ指定（復号化の場合省略）  */
    String DEF_DEBUG = "-DEBUG";    /* デバッグスイッチ                   */
    String DEF_debug = "-debug";    /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "ファイル会員番号復号補正";  /* APログ用機能名                   */
    int DEF_Read_EOF = 9;      /* File read EOF                    */
    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int arg_p_chk;                /* 引数iチェック用                    */
    StringDto arg_p_Value = new StringDto(256);         /* 引数i設定値                        */
    int arg_s_chk;                /* 引数oチェック用                    */
    StringDto arg_s_Value = new StringDto(256);         /* 引数o設定値                        */
    int enc_dec_flg;              /* 暗号復号フラグ                     */
    StringDto in_pattern = new StringDto(4);            /* 暗号化パータン                     */
    StringDto fl_in_name = new StringDto(256);          /* 入力ファイル名                     */
    StringDto fl_out_name = new StringDto(256);         /* 入力ファイル名                     */
    StringDto divide_char = new StringDto(4);           /* 区切り文字列                       */
    StringDto dec_nos_char = new StringDto(256);        /* 復号項目番号文字列                 */
    StringDto read_buf = new StringDto(6000);           /* 入力ファイル読み込みバッファ       */
    StringDto out_buf = new StringDto(6000);            /* 出力ファイル用バッファ             */
    StringDto in_file_dir = new StringDto(4096);        /* 入力ファイルのディレクトリ         */
    StringDto kaigyou_cd = new StringDto(3);            /* 改行コード格納用                   */

    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                 **/


    FileStatusDto fp_in;                         /* 入力ファイルポインタ         */
    FileStatusDto fp_out;                         /* 出力ファイルポインタ         */
/******************************************************************************/
    /*                                                                            */
    /*  メイン関数                                                                */
    /*   int  main(int argc, char** argv)                                         */
    /*                                                                            */
    /*            argc ： 起動時の引数の数                                        */
    /*            argv ： 起動時の引数の文字列                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              メイン処理を行う                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              プログラムヘッダ参照                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              プログラムヘッダ参照                                          */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public MainResultDto main(int argc, String[] argv) {
        int rtn_cd;                         /* 関数戻り値                         */
        int arg_cnt;                        /* 引数チェック用カウンタ             */
        StringDto arg_Work1 = new StringDto(256);                /* Work Buffer1                       */
        StringDto out_str = new StringDto(256);                   /* 復号補正後文字列                   */
        String env_wrk;                          /* 環境変数取得用                 */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            /*------------------------------------------------------------*/
        }

        /* 暗号復号化フラグ初期設定（2：復号化）           */
        enc_dec_flg = 2;

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  入力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", "CM_APWORK_DATE");
            /*-------------------------------------------------------------*/
        }
        env_wrk = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_wrk)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "null");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* 入力ファイルDIRセット */
        strcpy(in_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）[%s]\n", in_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            C_DbgMsg("*** main *** 入力引数の数[%d]\n", argc - 1);
            /*------------------------------------------------------------*/
        }

        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }

            if (memcmp(arg_Work1, DEF_ARG_P, strlen(DEF_ARG_P)) == 0) {
                rtn_cd = ChkArgpCom(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "暗号化パータン引数が誤っています（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();
                    return exit(C_const_APNG);
                }
                //変数に格納
                strcpy(in_pattern, arg_Work1.arr.substring(2));
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（暗号化パータン）[%s]\n", in_pattern);
                    /*-------------------------------------------------------------*/
                }
            } else if (memcmp(arg_Work1, DEF_ARG_I, strlen(DEF_ARG_I)) == 0) {
                rtn_cd = ChkArgpCom(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "入力ファイル引数が誤っています（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();
                    return exit(C_const_APNG);
                }
                //変数に格納
                sprintf(fl_in_name, "%s/%s", in_file_dir, arg_Work1.arr.substring(2));
                sprintf(fl_out_name, "%s.tmp", fl_in_name);
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（入力ファイル名）[%s]\n", fl_in_name);
                    /*-------------------------------------------------------------*/
                }
            } else if (memcmp(arg_Work1, DEF_ARG_C, strlen(DEF_ARG_C)) == 0) {
                rtn_cd = ChkArgpCom(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "区切り文字列引数が誤っています（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();
                    return exit(C_const_APNG);
                }
                //変数に格納
                strcpy(divide_char, arg_Work1.arr.substring(2));
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（区切り文字列）[%s]\n", divide_char);
                    /*-------------------------------------------------------------*/
                }
            } else if (memcmp(arg_Work1, DEF_ARG_N, strlen(DEF_ARG_N)) == 0) {
                rtn_cd = ChkArgpCom(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "復号項目番号が誤っています（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();
                    return exit(C_const_APNG);
                }
                //変数に格納
                strcpy(dec_nos_char, arg_Work1.arr.substring(2));
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（復号項目番号）[%s]\n", dec_nos_char);
                    /*-------------------------------------------------------------*/
                }
            } else if (memcmp(arg_Work1, DEF_ARG_E, strlen(DEF_ARG_E)) == 0) {
                enc_dec_flg = 1;
            } else if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else {    /* 定義外パラメータ   */
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", C_const_APNG, 0, 0);
                    /*---------------------------------------------*/
                }
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  会員番号復号補正処理         */
        memset(out_str, 0x00, sizeof(out_str));
        rtn_cd = DecFilePid();

        fclose(fp_in);
        fclose(fp_out);
        if (rtn_cd == C_const_APNG) {
            return exit(C_const_APNG);
        }
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DecFilePid NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "会員番号復号補正処理に失敗しました（%s）", fl_in_name);
            APLOG_WT("912", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ChkArgpCom                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ChkArgpCom( char *Arg_in )                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-P スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-P スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int ChkArgpCom(StringDto Arg_in) {
        arg_p_chk = DEF_OFF;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ChkArgpCom処理");
            /*---------------------------------------------------------------------*/
        }
        /*  重複指定チェック                   */
        if (arg_p_chk != DEF_OFF) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgpCom *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }
        arg_p_chk = DEF_ON;

        /*  設定値nullチェック  */
//        if ( Arg_in.substring(2) == 0x00 ){
        if (Arg_in.arr.length() == 2) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgpCom *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("ChkArgpCom処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： DecFilePid                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  DecFilePid()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-P スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-P スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int DecFilePid() {
        int rtn_cd;                           /* 関数戻り値                   */
        StringDto p_buf = new StringDto();                           /* カラム切り出し用             */
        String p_col = null;                           /* カラム切り出し用             */
        int col_cnt;                          /* カラム数カウンタ             */
        String p_dno;                           /* 復号項目番号切り出し用       */
        StringDto in_moto_str = new StringDto(256);                 /* 変換元文字列                 */
        StringDto out_str = new StringDto(256);                     /* 復号補正後文字列             */
        int d_no;                             /* 復号項目番号                 */
        StringDto wk_space = new StringDto(1024);                   /* スペース判定用               */
        StringDto wk_dec_nos_char = new StringDto(256);             /* 復号項番格納用               */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("DecFilePid処理");
            /*---------------------------------------------------------------------*/
        }

        /* 入力ファイルオープン */
        if ((fp_in = fopen(fl_in_name.arr, FileOpenType.r)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fl_in_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** DecFilePid *** 入力ファイルオープンERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* 出力ファイルオープン */
        if ((fp_out = fopen(fl_out_name.arr, SystemConstant.UTF8, FileOpenType.w)).fd == C_const_NG) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** DecFilePid *** 出力ファイルファイルオープンERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*  入力ファイル読み込みループ         */
        while (true) {

            /* 初期化 */
            memset(read_buf, 0x00, sizeof(read_buf));
            memset(p_buf, 0x00, sizeof(p_buf));
            memset(p_col, 0x00, sizeof(p_col));
            memset(out_buf, 0x00, sizeof(out_buf));
            memset(wk_dec_nos_char, 0x00, sizeof(wk_dec_nos_char));
            memset(kaigyou_cd, 0x00, sizeof(kaigyou_cd));
            memset(wk_space, 0x20, sizeof(wk_space));
            p_buf = null;
            p_col = null;
            wk_dec_nos_char.arr = null;
            wk_space.arr = " ";
            for (int i = 0; i < wk_space.len; i++) {
                wk_space.arr += " ";
            }

            kaigyou_cd.arr = null;
            out_buf.arr = null;
            read_buf.arr = null;

            /*------------------------------*/
            /* ファイル読み込み             */
            /*------------------------------*/
            rtn_cd = ReadFile();
            if (rtn_cd == DEF_Read_EOF) {
                break;
            } else if (rtn_cd != C_const_OK) {

                APLOG_WT("912", 0, null, "ファイル読み込み処理に失敗しました", 0, 0, 0, 0, 0);

                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("DecFilePid処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            col_cnt = 0;

            p_buf = read_buf;
            byte[] p_buf_b = p_buf.arr.getBytes();
            if (p_buf_b[p_buf_b.length - 1] == '\n') {
                p_buf_b = Arrays.copyOf(p_buf_b, p_buf_b.length - 1);
//                p_buf_b[strlen(p_buf) - 1] = '';
                if (p_buf_b[p_buf_b.length - 1] == '\r') {
                    p_buf_b = Arrays.copyOf(p_buf_b, p_buf_b.length - 1);
//                    p_buf_b[strlen(p_buf) - 1] = '\0';
                    sprintf(kaigyou_cd, "\r\n");
                } else {
                    sprintf(kaigyou_cd, "\n");
                }
            }
            p_buf.arr = new String(p_buf_b);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** DecFilePid *** 行内容=[%s]\n", p_buf);
                /*------------------------------------------------------------*/
            }
            /* ファイル行を分割してループする */
            int p_buf_L = p_buf.arr.split(divide_char.arr).length;
            while (true) {

                p_col = p_buf.strsep(divide_char.arr);
                col_cnt++;

                if (p_col != null) {

                    /* 復号する項番を分割し、ループ処理 */
                    strcpy(wk_dec_nos_char, dec_nos_char);
                    p_dno = wk_dec_nos_char.strtok(wk_dec_nos_char, ",");

                    memset(out_str, 0x00, sizeof(out_str));
                    memset(in_moto_str, 0x00, sizeof(in_moto_str));

                    while (p_dno != null) {

                        d_no = atoi(p_dno);

                        if (col_cnt == d_no && strlen(p_col) > 0) {

                            strcpy(in_moto_str, p_col);
                        }
                        p_dno = wk_dec_nos_char.strtok(null, ",");
                    } // while END

                    /*  会員番号復号補正処理         */
                    if (strlen(in_moto_str) > 0) {
                        if (DBG_LOG) {
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** DecFilePid *** 変換前文字列=[%s]\n", in_moto_str);
                            /*------------------------------------------------------------*/
                        }
                        rtn_cd = cmABendeBService.C_EncDec_Pid(enc_dec_flg, in_pattern.arr, in_moto_str.arr, out_str);
                        if (rtn_cd != C_const_OK) {
                            if (DBG_LOG) {
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** main *** C_EncDec_Pid NG rtn=[%d]\n", rtn_cd);
                                /*------------------------------------------------------------*/
                            }
                            sprintf(log_format_buf, "会員番号復号補正処理に失敗しました（%s）", in_moto_str);
                            APLOG_WT("912", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
                            return C_const_APNG;
                        }
                        if (strncmp(in_moto_str.arr, wk_space.arr, strlen(in_moto_str)) == 0) {
                            if (enc_dec_flg == 2) {
                                strcat(out_buf, " ");
                            } else {
                                strncat(out_buf, wk_space.arr, strlen(out_str));
                            }
                        } else {
                            strcat(out_buf, out_str);
                        }
                        strcat(out_buf, divide_char);
                    } else {
                        strcat(out_buf, p_col);
                        if (p_buf_L != col_cnt) {
                            strcat(out_buf, divide_char);
                        }
                    }

                } else {
                    break;
                }
            } // while END

            /*  テンプレートファイル出力処理         */
            rtn_cd = WriteTmpFile();
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** main *** WriteTmpFile NG rtn=[%d]\n", rtn_cd);
                    /*------------------------------------------------------------*/
                }
                sprintf(log_format_buf, "テンプレートファイル出力処理に失敗しました");
                APLOG_WT("912", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
                return C_const_APNG;
            }

        } // while END

        fclose(fp_in);

        fclose(fp_out);                       /* File Close                     */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("DecFilePid処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ReadFile                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ReadFile()                                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル入力処理                                              */
    /*              ファイルを読み込みする。                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int ReadFile() {
        int rtn_cd;                         /* 関数戻り値                     */

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgStart("ReadFile処理");
            /*----------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(read_buf, 0x00, sizeof(read_buf));

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            /* C_DbgMsg( "*** ReadFile *** buf=[%s]\n", wk_buf);         */
            /*----------------------------------------------------------------------*/
        }

        fgets(read_buf, read_buf.len - 1, fp_in);

        if (feof(fp_in) != C_const_OK) {

            /*---------------------------------------------*/
            C_DbgMsg("*** ReadFile *** READ EOF status= %d\n", feof(fp_in));
            /*---------------------------------------------*/

            return (DEF_Read_EOF);
        }

        rtn_cd = ferror(fp_in);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ReadFile *** 入力ファイルリードNG%s\n", "");
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "fgets(%s)", fl_in_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            return (C_const_NG);
        }

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgEnd("ReadFile処理", C_const_OK, 0, 0);
            /*----------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： WriteTmpFile                                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  WriteTmpFile()                                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              テンプレートファイル作成処理                                  */
    /*              取得情報を編集し、ファイル出力を行う。                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    public int WriteTmpFile() {
        StringDto wk_out_buf = new StringDto(4096);          /* 出力情報格納用               */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("WriteTmpFile処理");
            /*---------------------------------------------------------------------*/
        }
//        out_buf[strlen(out_buf) - 1] = '\0';
        strcat(out_buf, kaigyou_cd);
        sprintf(wk_out_buf, "%s", out_buf);

        /*--------------------------*/
        /* 月次ポイントデータファイルの出力 */
        /*--------------------------*/
        fwrite(wk_out_buf.arr, wk_out_buf.len, 1, fp_out);
        if (ferror(fp_out) != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** WriteTmpFile *** fwrite NG rtn=[%d]\n", ferror(fp_out));
                /*-------------------------------------------------------------*/
            }
            memset(log_format_buf, 0x00, sizeof(log_format_buf));
            sprintf(log_format_buf, "fwrite（%s）", fl_out_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        fflush(fp_out);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** WriteTmpFile *** fwrite=[%s]\n", fl_out_name);
            /*-------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("WriteTmpFile処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----WriteTmpFile Bottom------------------------------------------------------*/
    }


}
