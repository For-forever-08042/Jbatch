package jp.co.mcc.nttdata.batch.business.com.cmBTkaicB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTkaicB.dto.CmBTkaicBServiceDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;
import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *   プログラム名   ： 会員情報作成（cmBTkaicB）
 *
 *   【処理概要】
 *    MM顧客情報、MM顧客属性情報、MM顧客企業別属性情報、MS顧客制度情報、
 *    MSカード情報より連動用会員情報ファイルを作成する。
 *
 *   【引数説明】
 *     -DEBUG(-debug)     :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *      10     ： 正常
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 7（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *     40.00 : 2022/12/07 SSI.山口 ： MCCM初版
 *     40.01 : 2023/12/07 SSI.川内 ： MCCMPH2 MG-191 故障対応
 *     41.01 : 2024/01/24 SSI.川内 ： MCCMPH2 HS-0146 関心分野コードのダブルクォートを除去,nullを設定
 *     41.02 : 2024/02/27 SSI.川内 ： MCCMPH2 HS-0170 仕変対応 PS店舗変換マスタをもとに会社コード、店番号を変換
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTkaicBServiceImpl extends CmABfuncLServiceImpl implements CmBTkaicBService {
    boolean DBG_LOG = true;                  /* デバッグメッセージ出力             */
    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;
    CmBTkaicBServiceDto cmBTkaicBServiceDto = new CmBTkaicBServiceDto();

    /* 動的ＳＱＬ作成用 */
    StringDto str_sql = new StringDto(12288); /* 実行用SQL文字列                    */
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;     /* OFF                                */
    int DEF_ON = 1;     /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_O = "-o";                /* 連動用会員情報ファイル名           */
    String DEF_ARG_B = "-b";                /* 誕生年基準値                       */
    String DEF_ARG_D = "-d";                /* 抽出対象日付                       */
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    /*----------------------------------------*/
    String C_PRGNAME = "会員情報作成";    /* APログ用機能名              */
    String DEF_O_FILENAME = "getuji_POINT.txt";   /* 会員情報データファイル */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int gi_taisyo_cnt;            /* 処理対象件数                       */
    int gi_ok_cnt;                /* 正常処理件数                       */
    int arg_o_chk;                /* 引数oチェック用                    */
    int arg_b_chk;                /* 引数bチェック用                    */
    int arg_d_chk;                /* 引数dチェック用                    */
    StringDto fl_name = new StringDto(4096);            /* 出力ファイル名                     */
    StringDto out_file_dir = new StringDto(4096);       /* 出力ファイルディレクトリ           */
    int gi_kijunchi;              /* 誕生年基準値                       */
    int gi_target_date;           /* 抽出対象日付                       */
    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                 **/

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* 会員情報データファイル構造体                                                   */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_out;                       /* 出力ファイルポインタ         */
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
    public MainResultDto main(int argc, String[] argv) {
        int rtn_cd;                         /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス                 */
        int arg_cnt;                        /* 引数チェック用カウンタ             */
        String env_wrk;                       /* 出力ファイルDIR                    */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                       */
        StringDto bat_date = new StringDto(9);                    /* 取得用_バッチ処理日付（当日）      */
        StringDto wk_file_name = new StringDto(4096);             /* 出力ファイル名                     */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        gi_taisyo_cnt = 0;                     /* 処理対象件数                       */
        gi_ok_cnt = 0;                     /* 正常処理件数                       */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            /*------------------------------------------------------------*/
        }

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
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            C_DbgMsg("*** main *** 入力引数の数[%d]\n", argc - 1);
            /*------------------------------------------------------------*/
        }

        gi_kijunchi = 0;
        gi_target_date = 0;

        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }

            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;

            } else if (0 == memcmp(arg_Work1, DEF_ARG_O, strlen(DEF_ARG_O))) {
                rtn_cd = ChkArgoInf(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "出力ファイル名引数が誤っています（%s）", arg_Work1);
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
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（入力ファイル名）[%s]\n", fl_name);
                    /*-------------------------------------------------------------*/
                }

            } else if (0 == memcmp(arg_Work1, DEF_ARG_B, strlen(DEF_ARG_B))) {
                rtn_cd = ChkArgbInf(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "誕生年基準値引数が誤っています（%s）", arg_Work1);
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
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（誕生年基準値）[%d]\n", gi_kijunchi);
                    /*-------------------------------------------------------------*/
                }

            } else if (0 == memcmp(arg_Work1, DEF_ARG_D, strlen(DEF_ARG_D))) {
                rtn_cd = ChkArgdInf(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "抽出対象日付引数が誤っています（%s）", arg_Work1);
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
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（抽出対象日付）[%d]\n", gi_kijunchi);
                    /*-------------------------------------------------------------*/
                }


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

        /*-------------------------------------*/
        /*  必須引数チェック                   */
        /*-------------------------------------*/
        if (arg_o_chk == DEF_OFF || arg_b_chk == DEF_OFF) {
            sprintf(log_format_buf, "必須引数が指定されていません");
            APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();  /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "CM_APWORK_DATE");
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

        /* 出力ファイルDIRセット */
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
            /*-------------------------------------------------------------*/
        }

        memset(wk_file_name, 0x00, sizeof(wk_file_name));
        sprintf(wk_file_name, "%s/%s", out_file_dir, fl_name);
        sprintf(fl_name, "%s", wk_file_name);

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWSKokyakuNo処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（当日）        */
        /*-------------------------------------*/
        rtn_status.arr = 0;
        rtn_cd = C_const_OK;
        memset(bat_date, 0x00, sizeof(bat_date));

        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgMsg("*** GetYmd *** バッチ処理日(当日)取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        cmBTkaicBServiceDto.date_today = atoi(bat_date);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** GetYmd *** バッチ処理日(当日)=[%d]\n", cmBTkaicBServiceDto.date_today);
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（前日）        */
        /*-------------------------------------*/
        rtn_status.arr = 0;
        rtn_cd = C_const_OK;
        memset(bat_date, 0x00, sizeof(bat_date));

        /* バッチ処理日付を取得（前日） */
        rtn_cd = C_GetBatDate(-1, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgMsg("*** GetYmd *** バッチ処理日(前日)取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        cmBTkaicBServiceDto.date_yesterday = atoi(bat_date);

        /* 抽出対象日付設定 */
        if (gi_target_date == 0) {
            gi_target_date = cmBTkaicBServiceDto.date_yesterday;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** GetYmd *** 処理日付(前日)=[%d]\n", cmBTkaicBServiceDto.date_yesterday);
            /*------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  会員情報作成主処理         */
        rtn_cd = cmBTkaicB_main();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTkaicB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (StringUtils.isEmpty(Cg_ORASID)) {
                /* ロールバック */
//                EXEC SQL ROLLBACK RELEASE;
                sqlca.rollback();
            }

            APLOG_WT("912", 0, null, "会員情報作成主処理に失敗しました", 0, 0, 0, 0, 0);
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
    /*  関数名 ： cmBTkaicB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int cmBTkaicB_main()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     会員情報データファイル作成主処理                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTkaicB_main() {
        int rtn_cd;                     /* 関数戻り値                       */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTkaicB_main処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;

        /*------------------------------------------------------------*/
        /* WS顧客番号２登録                                             */
        /*------------------------------------------------------------*/
        rtn_cd = InsertWSKokyakuNo();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** InsertWSKokyakuNo NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "WS顧客番号２登録処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        /* WMＳＰＳＳ会員情報登録                                     */
        /*------------------------------------------------------------*/
        rtn_cd = InsertWMSPSSKaiin();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** InsertWMSPSSKaiin NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "WMＳＰＳＳ会員情報登録処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /*  会員情報データファイルOPEN     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 会員情報データファイル(%s)\n", "オープン");
            /*------------------------------------------------------------*/
        }
        rtn_cd = OpenFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 会員情報データファイルオープン NG rtn=[%d]\n", rtn_cd);
                /*---------------------------------------------*/
            }
            memset(log_format_buf, 0x00, sizeof(log_format_buf));
            sprintf(log_format_buf, "fopen（%s）", fl_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return C_const_APNG;
        }

        /*------------------------------------------------------------*/
        /* 会員情報データファイル情報設定                         */
        /*------------------------------------------------------------*/
        /* 出力ファイル情報設定処理 */
        rtn_cd = SetOutFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** SetOutFile NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            fclose(fp_out);                       /* File Close                     */
            APLOG_WT("912", 0, null, "出力ファイル情報設定処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        fclose(fp_out);                       /* File Close                     */

        /* 処理件数を出力 */
        APLOG_WT("101", 0, null, fl_name, gi_ok_cnt, 0, 0, 0, 0);

        return (C_const_OK);              /* 処理終了                           */
        /*--- cmBTkaicB_main Bottom ----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： OpenFile                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OpenFile()                                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              会員情報データファイルを追加書き込みモードでオープンする。    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int OpenFile() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("OpenFile処理");
            /*------------------------------------------------------------*/
        }

        /* ファイルオープン */
//        if (( fp_out = fopen(fl_name, "w" )) == null) {
        if ((fp_out = open(fl_name.arr)).fd == -1) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** OpenFile *** 会員情報データファイルオープンERR%s\n",
                        "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** OpenFile *** 会員情報データファイル%s\n", fl_name);
            C_DbgEnd("OpenFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertWSKokyakuNo                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int InsertWSKokyakuNo()                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              連動対象の顧客番号を抽出し、WS顧客番号２に登録する              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int InsertWSKokyakuNo() {
        /* ローカル変数 */
        StringDto wk_sqlbuf = new StringDto(10240);               /* ＳＱＬ文編集用                  */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("InsertWSKokyakuNo処理");
            /*---------------------------------------------------------------------*/
        }

        /* WS顧客番号２テーブルのデータ削除 */
//        EXEC SQL TRUNCATE TABLE WS顧客番号２;

        sqlca.sql = new StringDto("TRUNCATE TABLE WS顧客番号２");
        sqlca.restAndExecute();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** InsertWSKokyakuNo *** WS顧客番号２ TRUNCATE " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            APLOG_WT("904", 0, null, "TRUNCATE", sqlca.sqlcode,
                    "WS顧客番号２", 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWSKokyakuNo処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /*-------------------------------------*/
        /* 実行ＳＱＬ文用バッファ初期化        */
        /*-------------------------------------*/
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));

        /*------------------------------------------*/
        /* 動的SQL文の設定                          */
        /*------------------------------------------*/
        sprintf(wk_sqlbuf, "INSERT INTO WS顧客番号２ ( 顧客番号 ) " +
                        " SELECT 顧客番号 FROM MM顧客情報 WHERE 最終更新日 >= %d " +
                        " UNION " +
                        " SELECT 顧客番号 FROM MM顧客属性情報 WHERE 最終更新日 >= %d " +
                        " UNION " +
                        " SELECT 顧客番号 FROM MM顧客企業別属性情報 WHERE 最終更新日 >= %d " +
                        " UNION " +
                        " SELECT 顧客番号 FROM MS顧客制度情報 WHERE 最終更新日 >= %d ",
                gi_target_date, gi_target_date, gi_target_date, gi_target_date
        );

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** InsertWSKokyakuNo *** WS顧客番号２登録ＳＱＬ文作成完了 %s\n", wk_sqlbuf);
            /*---------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
//        EXEC SQL PREPARE sql_ws_kokyaku_no FROM :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertWSKokyakuNo *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset(log_format_buf, 0x00, sizeof(log_format_buf));
            sprintf(log_format_buf, "抽出対象日付<=[%d]", gi_target_date);

            /* APLOG出力 */
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "WS顧客番号２", log_format_buf, 0, 0);

            return C_const_NG;
        }

//        EXEC SQL EXECUTE sql_ws_kokyaku_no;
        sqlca.restAndExecute();

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertWSKokyakuNo *** WS顧客番号２INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset(log_format_buf, 0x00, sizeof(log_format_buf));
            sprintf(log_format_buf, "抽出対象日付<=[%d]", gi_target_date);

            /* APLOG出力 */
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "WS顧客番号２", log_format_buf, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWSKokyakuNo処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        /* コミット */
//        EXEC SQL COMMIT RELEASE;
        sqlca.commit();

        /* グローバル変数のnullクリア */
//        memset(Cg_ORASID, 0x00, sizeof(Cg_ORASID));
//        memset(Cg_ORAUSR, 0x00, sizeof(Cg_ORAUSR));
//        memset(Cg_ORAPWD, 0x00, sizeof(Cg_ORAPWD));

        Cg_ORASID = null;
        Cg_ORAUSR = null;
        Cg_ORAPWD = null;
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("InsertWSKokyakuNo処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----InsertWSKokyakuNo Bottom--------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertWMSPSSKaiin                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int InsertWMSPSSKaiin()                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              連動対象の制度DBの会員情報を抽出し、                          */
    /*              WMＳＰＳＳ会員情報に登録する                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int InsertWMSPSSKaiin() {
        int rtn_cd;                         /* 関数戻り値                      */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス              */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("InsertWMSPSSKaiin処理");
            /*---------------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(C_ORACONN_MD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }

            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWMSPSSKaiin処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* WS顧客番号２テーブルのデータ削除 */
//        EXEC SQL TRUNCATE TABLE WMＳＰＳＳ会員情報;
        sqlca.sql = new StringDto("TRUNCATE TABLE WMＳＰＳＳ会員情報");
        sqlca.restAndExecute();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** InsertWMSPSSKaiin *** WMＳＰＳＳ会員情報 TRUNCATE " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            APLOG_WT("904", 0, null, "TRUNCATE", sqlca.sqlcode,
                    "WMＳＰＳＳ会員情報", "", 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWMSPSSKaiin処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* コミット */
//        EXEC SQL COMMIT RELEASE;
        sqlca.commit();

        /* グローバル変数のnullクリア */
//        memset(Cg_ORASID, 0x00, sizeof(Cg_ORASID));
//        memset(Cg_ORAUSR, 0x00, sizeof(Cg_ORAUSR));
//        memset(Cg_ORAPWD, 0x00, sizeof(Cg_ORAPWD));
        Cg_ORASID = null;
        Cg_ORAUSR = null;
        Cg_ORAPWD = null;
        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }

            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWMSPSSKaiin処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*------------------------------------------*/
        /* ＳＱＬ実行                               */
        /*------------------------------------------*/
//        EXEC SQL INSERT INTO WMＳＰＳＳ会員情報@CMMD (
//            顧客番号
//            ,ＧＯＯＰＯＮ番号
//            ,シニア
//            ,コーポレート会員フラグ
//            ,コーポレート会員ステータス
//            ,コーポレート会員登録日
//            ,グローバル会員国コード
//            ,デジタル会員ＥＣ入会フラグ
//            ,デジタル会員アプリ入会フラグ
//            ,デジタル会員ＥＣ入会更新日時
//            ,デジタル会員アプリ入会更新日時
//    )
//            SELECT
//        W.顧客番号
//                ,C.ＧＯＯＰＯＮ番号
//                ,K.シニア
//                ,K.コーポレート会員フラグ
//                ,K.コーポレート会員ステータス
//                ,K.コーポレート会員登録日
//                ,K.グローバル会員国コード
//                ,K.デジタル会員ＥＣ入会フラグ
//                ,K.デジタル会員アプリ入会フラグ
//                ,K.デジタル会員ＥＣ入会更新日時
//                ,K.デジタル会員アプリ入会更新日時
//        FROM
//        WS顧客番号２ W
//                 ,MS顧客制度情報 K
//                 ,( SELECT DISTINCT 顧客番号, ＧＯＯＰＯＮ番号
//        FROM MSカード情報 C1
//        WHERE EXISTS (
//            SELECT 1 FROM WS顧客番号２ W1 WHERE C1.顧客番号=W1.顧客番号
//                    )
//                  ) C
//        WHERE W.顧客番号 = K.顧客番号
//        AND W.顧客番号 = C.顧客番号;

        StringDto WRKSQL = new StringDto("INSERT INTO WMＳＰＳＳ会員情報 (\n" +
                "            顧客番号\n" +
                "            ,ＧＯＯＰＯＮ番号\n" +
                "            ,シニア\n" +
                "            ,コーポレート会員フラグ\n" +
                "            ,コーポレート会員ステータス\n" +
                "            ,コーポレート会員登録日\n" +
                "            ,グローバル会員国コード\n" +
                "            ,デジタル会員ＥＣ入会フラグ\n" +
                "            ,デジタル会員アプリ入会フラグ\n" +
                "            ,デジタル会員ＥＣ入会更新日時\n" +
                "            ,デジタル会員アプリ入会更新日時\n" +
                "    )\n" +
                "            SELECT\n" +
                "        W.顧客番号\n" +
                "                ,C.ＧＯＯＰＯＮ番号\n" +
                "                ,K.シニア\n" +
                "                ,K.コーポレート会員フラグ\n" +
                "                ,K.コーポレート会員ステータス\n" +
                "                ,K.コーポレート会員登録日\n" +
                "                ,K.グローバル会員国コード\n" +
                "                ,K.デジタル会員ＥＣ入会フラグ\n" +
                "                ,K.デジタル会員アプリ入会フラグ\n" +
                "                ,K.デジタル会員ＥＣ入会更新日時\n" +
                "                ,K.デジタル会員アプリ入会更新日時\n" +
                "        FROM\n" +
                "        WS顧客番号２ W\n" +
                "                 ,MS顧客制度情報 K\n" +
                "                 ,( SELECT DISTINCT 顧客番号, ＧＯＯＰＯＮ番号\n" +
                "        FROM MSカード情報 C1\n" +
                "        WHERE EXISTS (\n" +
                "            SELECT 1 FROM WS顧客番号２ W1 WHERE C1.顧客番号=W1.顧客番号\n" +
                "                    )\n" +
                "                  ) C\n" +
                "        WHERE W.顧客番号 = K.顧客番号\n" +
                "        AND W.顧客番号 = C.顧客番号");

        sqlca.sql = WRKSQL;
        sqlca.restAndExecute();

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertWMSPSSKaiin *** WS顧客番号２INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* APLOG出力 */
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "WMＳＰＳＳ会員情報", 0, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWMSPSSKaiin処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        /* コミット */
//        EXEC SQL COMMIT RELEASE;
        sqlca.commit();

        /* グローバル変数のnullクリア */
//        memset(Cg_ORASID, 0x00, sizeof(Cg_ORASID));
//        memset(Cg_ORAUSR, 0x00, sizeof(Cg_ORAUSR));
//        memset(Cg_ORAPWD, 0x00, sizeof(Cg_ORAPWD));
        Cg_ORASID = null;
        Cg_ORAUSR = null;
        Cg_ORAPWD = null;
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("InsertWMSPSSKaiin処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----InsertWMSPSSKaiin Bottom--------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： SetOutFile                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int SetOutFile()                                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              WMＳＰＳＳ会員情報、MM顧客情報、MM顧客属性情報、              */
    /*              MM顧客企業別属性情報から連動対象の会員情報を抽出する。        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int SetOutFile() {
        /* ローカル変数 */
        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        StringDto wk_sqlbuf = new StringDto(10240);               /* ＳＱＬ文編集用                 */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("SetOutFile処理");
            /*---------------------------------------------------------------------*/
        }
        /*-------------------------------------*/
        /* 会員情報データ検索ＳＱＬ文作成  */
        /*-------------------------------------*/
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));

        /* 会員情報データ検索ＳＱＬ文作成処理呼び出し */
        MakeSqlStr(wk_sqlbuf);

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** SetOutFile *** 会員情報データ検索ＳＱＬ文作成完了 %s\n", "");
            /*---------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(C_ORACONN_MD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("InsertWSKokyakuNo処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ＨＯＳＴ変数にセット */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
//        EXEC SQL PREPARE sql_kaiin FROM :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("cur_kaiin");
        sqlca.sql.arr =DEFAULT_ALTER_TIME_SQL;
        sqlca.restAndExecute();
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertWSKokyakuNo() *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgMsg("*** InsertWSKokyakuNo() *** 動的SQL 解析NG = %s\n", str_sql);
            }
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode, "WMＳＰＳＳ会員情報", 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE cur_kaiin CURSOR FOR sql_kaiin;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null
                    , sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソルオープン */
//        EXEC SQL OPEN cur_kaiin;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode, "WMＳＰＳＳ会員情報", 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /* 会員情報データを取得              */
        /*-------------------------------------*/
        while (true) { /* loop */
            /* ホスト変数・構造体を初期化 */
//            memset(h_kaiin_info, 0x00, sizeof(h_kaiin_info)); /*出力ファイル       バッファ */

            /* カーソルフェッチ */
//            EXEC SQL FETCH cur_kaiin
//                  INTO :cmBTkaicBServiceDto.h_kaiin_info.goopon_no                  :indi_goopon_no,
//                       :cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_status        :indi_goopon_kaiin_status,
//                       :cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_sakujo_flg    :indi_goopon_kaiin_sakujo_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.seibetu_cd                 :indi_seibetu_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.kekkon_cd                  :indi_kekkon_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.shokugyo_cd                :indi_shokugyo_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.sei_ymd                    :indi_sei_ymd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.yubin_no_header            :indi_yubin_no_header,
//                       :cmBTkaicBServiceDto.h_kaiin_info.yubin_no_body              :indi_yubin_no_body,
//                       :cmBTkaicBServiceDto.h_kaiin_info.todofuken_cd               :indi_todofuken_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.toroku_ymd                 :indi_toroku_ymd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.koshin_ymdhms              :indi_koshin_ymdhms,
//                       :cmBTkaicBServiceDto.h_kaiin_info.dm_fuyo_flg                :indi_dm_fuyo_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.nyukai_tempo_cd            :indi_nyukai_tempo_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.dm_atesaki_fumei_flg       :indi_dm_atesaki_fumei_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.mk_nyukai_moshikomi_ymd    :indi_mk_nyukai_moshikomi_ymd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.md_kigyo_cd                :indi_md_kigyo_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.zokusei_flg                :indi_zokusei_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_kbn            :indi_group_kaiin_kbn,
//                       :cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_toroku_ymd     :indi_group_kaiin_toroku_ymd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_status         :indi_group_kaiin_status,
//                       :cmBTkaicBServiceDto.h_kaiin_info.kaisha_mei                 :indi_kaisha_mei,
//                       :cmBTkaicBServiceDto.h_kaiin_info.busho_mei                  :indi_busho_mei,
//                       :cmBTkaicBServiceDto.h_kaiin_info.kanshin_bunya_cd           :indi_kanshin_bunya_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_error_flg     :indi_mail_haishin_error_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_kyodaku_flg   :indi_mail_haishin_kyodaku_flg,
//                       :cmBTkaicBServiceDto.h_kaiin_info.global_kaiin_kuni_cd       :indi_global_kaiin_kuni_cd,
//                       :cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_kbn        :indi_mcc_group_kaiin_kbn,
//                       :cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_toroku_ymd :indi_mcc_group_kaiin_toroku_ymd;
            sqlca.fetch();
            sqlca.recData(cmBTkaicBServiceDto.h_kaiin_info.goopon_no
                    , cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_status
                    , cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_sakujo_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.seibetu_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.kekkon_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.shokugyo_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.sei_ymd
                    , cmBTkaicBServiceDto.h_kaiin_info.yubin_no_header
                    , cmBTkaicBServiceDto.h_kaiin_info.yubin_no_body
                    , cmBTkaicBServiceDto.h_kaiin_info.todofuken_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.toroku_ymd
                    , cmBTkaicBServiceDto.h_kaiin_info.koshin_ymdhms
                    , cmBTkaicBServiceDto.h_kaiin_info.dm_fuyo_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.nyukai_tempo_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.dm_atesaki_fumei_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.mk_nyukai_moshikomi_ymd
                    , cmBTkaicBServiceDto.h_kaiin_info.md_kigyo_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.zokusei_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_kbn
                    , cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_toroku_ymd
                    , cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_status
                    , cmBTkaicBServiceDto.h_kaiin_info.kaisha_mei
                    , cmBTkaicBServiceDto.h_kaiin_info.busho_mei
                    , cmBTkaicBServiceDto.h_kaiin_info.kanshin_bunya_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_error_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_kyodaku_flg
                    , cmBTkaicBServiceDto.h_kaiin_info.global_kaiin_kuni_cd
                    , cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_kbn
                    , cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_toroku_ymd);

            /* データ無しの場合、処理を正常終了 */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* for を抜ける */
                break;
            }

            /* データ無し以外エラーの場合、処理を異常終了する */
            else if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(log_format_buf, 0x00, sizeof(log_format_buf));
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "WMＳＰＳＳ会員情報", 0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE cur_kaiin;
//                sqlca.close();
                sqlcaManager.close("cur_kaiin");
                return C_const_NG;
            }

            /* 処理対象件数カウントアップ */
            gi_taisyo_cnt++;

            /*------------------------------------------*/
            /* 会員情報作成                             */
            /*------------------------------------------*/
            /* 会員情報作成処理 */
            rtn_cd = FileOutput();
            if (rtn_cd == C_const_NG) {
                /* error */
                APLOG_WT("903", 0, null, "FileOutput", rtn_cd, 0, 0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE cur_kaiin;
                sqlcaManager.close("cur_kaiin");
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 正常件数カウントアップ */
            gi_ok_cnt++;

        } /* END loop */

//        EXEC SQL CLOSE cur_kaiin;         /* カーソルクローズ                  */

        sqlcaManager.close("cur_kaiin");

        /* コミット */
//        EXEC SQL COMMIT RELEASE;
        sqlcaManager.commit();

        /* グローバル変数のnullクリア */
//        memset(Cg_ORASID, 0x00, sizeof(Cg_ORASID));
//        memset(Cg_ORAUSR, 0x00, sizeof(Cg_ORAUSR));
//        memset(Cg_ORAPWD, 0x00, sizeof(Cg_ORAPWD));
        Cg_ORASID = null;
        Cg_ORAUSR = null;
        Cg_ORAPWD = null;
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("SetOutFile処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----SetOutFile Bottom--------------------------------------------*/
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： MakeSqlStr                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static void MakeSqlStr( char *wk_sqlbuf )                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              WMＳＰＳＳ会員情報、MM顧客情報、MM顧客属性情報、              */
    /*              MM顧客企業別属性情報から連動対象の会員情報を抽出              */
    /*              するための動的SQL文作成する。                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： char          * wk_sqlbuf      ： ＳＱＬ文                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*               なし                                                         */
    /*                                                                            */

    /******************************************************************************/
    public void MakeSqlStr(StringDto wk_sqlbuf) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MakeSqlStr処理");
            /*---------------------------------------------------------------------*/
        }

        /*------------------------------------------*/
        /* 動的SQL文の設定                          */
        /*------------------------------------------*/
        sprintf(wk_sqlbuf, "SELECT " +
                        "    W.ＧＯＯＰＯＮ番号  " +
                        "   ,CASE WHEN K1.顧客ステータス = 0 OR k1.顧客ステータス = 1 THEN 0 " +
                        "         WHEN K1.顧客ステータス = 9 THEN 9 END AS GOOPON会員ステータス" +
                        "   ,CASE WHEN K1.顧客ステータス = 9 THEN 1 ELSE 0 END AS GOOPON会員削除フラグ " +
                        "   ,NVL(K1.性別,0) AS 性別コード " +
                        "   ,NVL(K1.婚姻,0) AS 結婚コード " +
                        "   ,NVL(K2.勤務区分,0) AS 職業コード " +
                        "   ,CASE WHEN NVL(K1.誕生年,0) < 1900 OR NVL(K1.誕生月,0) < 1 OR NVL(K1.誕生月,0) > 12 OR NVL(K1.誕生日,0) < 1 " +
                        "      OR K1.誕生日 > TO_NUMBER(TO_CHAR(LAST_DAY(TO_DATE(CONCAT(TO_CHAR(K1.誕生年,'FM0000'),TO_CHAR(K1.誕生月,'FM00'),'01'),'YYYYMMDD')),'DD') )" +
                        "      OR K1.誕生年 <= TO_NUMBER(TO_CHAR(ADD_MONTHS(SYSDATE(),-12*%d),'YYYY')) " +
                        "      OR TO_DATE(CONCAT(TO_CHAR(K1.誕生年,'FM0000'),TO_CHAR(K1.誕生月,'FM00'),TO_CHAR(K1.誕生日,'FM00')),'YYYYMMDD') > SYSDATE() THEN '1900/01/01'" +
                        "         ELSE CONCAT(TO_CHAR(K1.誕生年,'FM0000'),'/',TO_CHAR(K1.誕生月,'FM00'),'/',TO_CHAR(K1.誕生日,'FM00')) END AS 生年月日" +
                        "   ,LPAD(SUBSTR(TRIM(k2.郵便番号),1,3),3,'0') AS 郵便番号前3桁 " +
                        "   ,LPAD(SUBSTR(TRIM(k2.郵便番号),4,4),4,'0') AS 郵便番号後4桁 " +
                        "   ,LPAD(K2.都道府県コード,2,'0') AS 都道府県コード " +
                        "   ,CASE WHEN K3.入会年月日 > 19000101 THEN TO_CHAR(TO_DATE(CAST(K3.入会年月日 AS TEXT)),'YYYY/MM/DD') ELSE NULL END AS 登録日" +
                        "   ,TO_CHAR(K1.最終更新日時,'YYYY/MM/DD HH24:MI:SS') AS 更新日時 " +
                        "   ,DECODE(K3.ＤＭ止め区分,'3000',1,0) AS DM要不要フラグ " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD START */
//            "   ,K1.入会店舗ＭＣＣ AS 入会店舗コード "
                        "   ,NVL(P.新店番号,K1.入会店舗ＭＣＣ) AS 入会店舗コード " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD END */
                        "   ,DECODE(K3.ＤＭ止め区分,'3000',0,1) AS DM宛先不明フラグ " +
                        "   ,CASE WHEN K1.入会申込用紙記載日 > 19000101 THEN TO_CHAR(TO_DATE(CAST(K1.入会申込用紙記載日 AS TEXT)),'YYYY/MM/DD') ELSE NULL END AS MK入会申込用紙記載日 " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD START */
//            "   ,K1.入会会社コードＭＣＣ AS MD企業コード "
                        "   ,NVL(P.新会社コード,K1.入会会社コードＭＣＣ) AS MD企業コード " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD END */
                        "   ,CASE WHEN W.シニア = 1 THEN '1000000000' ELSE '0000000000' END AS 属性フラグ " +
                        "   ,W.コーポレート会員フラグ AS グループ会員区分 " +
                        "   ,CASE WHEN W.コーポレート会員登録日 > 19000101 THEN CONCAT(TO_CHAR(TO_DATE(CAST(W.コーポレート会員登録日 AS TEXT)),'YYYY/MM/DD') , ' 00:00:00') ELSE NULL END  " +
                        "   ,W.コーポレート会員ステータス  " +
                        "   ,TRIM(SUBSTRB(K2.会社名,1,80)) " +
                        "   ,TRIM(SUBSTRB(K2.部署名,1,80)) " +
                        "   ,K2.関心分野コード " +
                        "   ,DECODE(K3.Ｅメール止め区分,'5001',1,0) AS メール配信エラーフラグ " +
                        "   ,DECODE(K3.Ｅメール止め区分,'5000',1,0) AS メール配信許諾フラグ " +
                        "   ,RPAD(W.グローバル会員国コード,LENGTH(W.グローバル会員国コード)) AS グローバル会員国コード  " +
                        "   ,CASE WHEN W.デジタル会員ＥＣ入会フラグ<>0 or W.デジタル会員アプリ入会フラグ<>0 THEN 1 ELSE 0 END AS MCCグループ会員フラグ " +
                        "   ,CASE WHEN NVL(W.デジタル会員ＥＣ入会フラグ,0)=0 and NVL(W.デジタル会員アプリ入会フラグ,0)=0 THEN NULL  " +
                        "         WHEN COALESCE(W.デジタル会員ＥＣ入会更新日時,TO_DATE('99991231')) < COALESCE(W.デジタル会員アプリ入会更新日時,TO_DATE('99991231')) THEN TO_CHAR(W.デジタル会員ＥＣ入会更新日時, 'YYYY/MM/DD HH24:MI:SS')  " +
                        "         ELSE TO_CHAR(W.デジタル会員アプリ入会更新日時, 'YYYY/MM/DD HH24:MI:SS') END AS MCCグループ会員登録日 " +
                        " FROM  " +
                        "    WMＳＰＳＳ会員情報 W " +
                        "   ,MM顧客情報 K1 " +
                        "    LEFT JOIN " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 ADD START */
                        "     PS店舗変換マスタ P " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 ADD END */
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 ADD START */
                        "    ON K1.入会会社コードＭＣＣ = P.旧会社コード AND K1.入会店舗ＭＣＣ = P.旧店番号 " +
                /* 2024/02/27 MCCMPH2 HS-0170仕変対応 ADD END */
                        "   ,MM顧客属性情報 K2 " +
                        "   ,( " +
                        "     SELECT " +
                        "        顧客番号 " +
                        "       ,ＤＭ止め区分 " +
                        "       ,Ｅメール止め区分 " +
                        "       ,入会年月日 " +
                        "     FROM ( " +
                        "       SELECT " +
                        "          顧客番号 " +
                        "         ,ＤＭ止め区分 " +
                        "         ,Ｅメール止め区分 " +
                        "         ,入会年月日 " +
                        "         ,ROW_NUMBER() OVER (PARTITION BY 顧客番号 ORDER BY 入会年月日 ) RECNUM " +
                        "       FROM MM顧客企業別属性情報 K31 " +
                        "       WHERE EXISTS (  " +
                        "         SELECT 1 FROM WMＳＰＳＳ会員情報 W1 WHERE K31.顧客番号=W1.顧客番号 " +
                        "       ) " +
                        "     ) " +
                        "     WHERE RECNUM = 1 " +
                        "   ) K3 " +
                        " WHERE W.顧客番号 = K1.顧客番号  " +
                        "   AND W.顧客番号 = K2.顧客番号 " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD START */
//            "   AND W.顧客番号 = K3.顧客番号 ",
                        "   AND W.顧客番号 = K3.顧客番号 " +
                        /* 2024/02/27 MCCMPH2 HS-0170仕変対応 MOD END */
                gi_kijunchi
        );

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** MakeSqlStr *** 動的ＳＱＬ=[%s]", wk_sqlbuf);
            C_DbgEnd("MakeSqlStr処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return;              /* 処理終了                           */
        /*-----MakeSqlStr Bottom-------------------------------------------------*/
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： FileOutput                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  FileOutput()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              会員情報作成処理                                              */
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
    public int FileOutput() {
        StringDto wk_out_buff = new StringDto(4096); /* 出力情報格納用               */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("FileOutput処理");
            /*---------------------------------------------------------------------*/
        }
        /*------------------------------*/
        /* 会員情報データファイルの編集 */
        /*------------------------------*/
        memset(wk_out_buff, 0x00, sizeof(wk_out_buff));

        sprintf(wk_out_buff,
                "%s,%d,%d,%d,%d,%d,%s,%s,%s,%s," +
                        "%s,%s,%d,%s,%d,%s,%s,%s,%d,%s," +
                        /* 2023/12/07 MCCMPH2 MG-191故障対応 MOD START */
//            "%s,\"%s\",\"%s\",%s,%d,%d,%s\n",
                        /* 2024/01/24 MCCMPH2 HS-0146故障対応 MOD START */
//            "%s,\"%s\",\"%s\",\"%s\",%d,%d,%s,%d,%s\n",
                        /* 2023/12/07 MCCMPH2 MG-191故障対応 MOD END */
                        "%s,\"%s\",\"%s\",,%d,%d,%s,%d,%s\n",
                cmBTkaicBServiceDto.h_kaiin_info.goopon_no,                    /* グーポン番号                  */
                cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_status.intVal(),              /* GOOPON会員ステータス          */
                cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_sakujo_flg.intVal(),          /* GOOPON会員削除フラグ          */
                cmBTkaicBServiceDto.h_kaiin_info.seibetu_cd.intVal(),                       /* 性別コード                    */
                cmBTkaicBServiceDto.h_kaiin_info.kekkon_cd.intVal(),                        /* 結婚コード                    */
                cmBTkaicBServiceDto.h_kaiin_info.shokugyo_cd.intVal(),                      /* 職業コード                    */
                cmBTkaicBServiceDto.h_kaiin_info.sei_ymd,                          /* 生年月日                      */
                cmBTkaicBServiceDto.h_kaiin_info.yubin_no_header.arr,                  /* 郵便番号前3桁                 */
                cmBTkaicBServiceDto.h_kaiin_info.yubin_no_body.arr,                    /* 郵便番号後4桁                 */
                cmBTkaicBServiceDto.h_kaiin_info.todofuken_cd.arr,                     /* 都道府県コード                */

                cmBTkaicBServiceDto.h_kaiin_info.toroku_ymd.arr,                       /* 登録日                        */
                cmBTkaicBServiceDto.h_kaiin_info.koshin_ymdhms.arr,                    /* 更新日時                      */
                cmBTkaicBServiceDto.h_kaiin_info.dm_fuyo_flg.intVal(),                      /* DM要不要フラグ                */
                cmBTkaicBServiceDto.h_kaiin_info.nyukai_tempo_cd,                  /* 入会店舗コード                */
                cmBTkaicBServiceDto.h_kaiin_info.dm_atesaki_fumei_flg.intVal(),             /* DM宛先不明フラグ              */
                cmBTkaicBServiceDto.h_kaiin_info.mk_nyukai_moshikomi_ymd,          /* MK入会申込用紙記載日          */
                cmBTkaicBServiceDto.h_kaiin_info.md_kigyo_cd.arr,                      /* MD企業コード                  */
                cmBTkaicBServiceDto.h_kaiin_info.zokusei_flg,                      /* 属性フラグ                    */
                cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_kbn.intVal(),                  /* グループ会員区分              */
                cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_toroku_ymd.arr,           /* グループ会員登録日            */

                cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_status.arr,               /* グループ会員ステータス        */
                cmBTkaicBServiceDto.h_kaiin_info.kaisha_mei.arr,                   /* 会社名                        */
                cmBTkaicBServiceDto.h_kaiin_info.busho_mei.arr,                    /* 部署名                        */
                /* 2024/01/24 MCCMPH2 HS-0146故障対応 DEL START */
//        h_kaiin_info.kanshin_bunya_cd.arr,             /* 関心分野コード                */
                /* 2024/01/24 MCCMPH2 HS-0146故障対応 DEL END */
                cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_error_flg,           /* メール配信エラーフラグ        */
                cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_kyodaku_flg,         /* メール配信許諾フラグ          */
                cmBTkaicBServiceDto.h_kaiin_info.global_kaiin_kuni_cd.arr,              /* グローバル会員国コード        */
                cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_kbn.intVal(),              /* MCCグループ会員区分        */
                cmBTkaicBServiceDto.h_kaiin_info.mcc_group_kaiin_toroku_ymd.arr   /* MCCグループ会員登録日        */
        );

        /*------------------------------*/
        /* 会員情報データファイルの出力 */
        /*------------------------------*/
        fwrite(wk_out_buff.arr, strlen(wk_out_buff), 1, fp_out);
        if (ferror(fp_out) != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutput *** fwrite NG rtn=[%d]\n", ferror(fp_out));
                /*-------------------------------------------------------------*/
            }
            memset(log_format_buf, 0x00, sizeof(log_format_buf));
            sprintf(log_format_buf, "fwrite（%s）", fl_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        fflush(fp_out);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** FileOutput *** fwrite=[%s]\n", fl_name);
            /*-------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("グーポン番号                [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.goopon_no.arr);
            C_DbgMsg("GOOPON会員ステータス        [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_status);
            C_DbgMsg("GOOPON会員削除フラグ        [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.goopon_kaiin_sakujo_flg);
            C_DbgMsg("性別コード                  [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.seibetu_cd);
            C_DbgMsg("結婚コード                  [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.kekkon_cd);
            C_DbgMsg("職業コード                  [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.shokugyo_cd);
            C_DbgMsg("生年月日                    [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.sei_ymd);
            C_DbgMsg("郵便番号前3桁               [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.yubin_no_header.arr);
            C_DbgMsg("郵便番号後4桁               [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.yubin_no_body.arr);
            C_DbgMsg("都道府県コード              [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.todofuken_cd.arr);
            C_DbgMsg("登録日                      [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.toroku_ymd.arr);
            C_DbgMsg("更新日時                    [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.koshin_ymdhms.arr);
            C_DbgMsg("DM要不要フラグ              [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.dm_fuyo_flg);
            C_DbgMsg("入会店舗コード              [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.nyukai_tempo_cd.arr);
            C_DbgMsg("DM宛先不明フラグ            [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.dm_atesaki_fumei_flg);
            C_DbgMsg("MK入会申込用紙記載日        [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.mk_nyukai_moshikomi_ymd.arr);
            C_DbgMsg("MD企業コード                [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.md_kigyo_cd.arr);
            C_DbgMsg("属性フラグ                  [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.zokusei_flg);
            C_DbgMsg("グループ会員区分            [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_kbn);
            C_DbgMsg("グループ会員登録日          [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_toroku_ymd.arr);
            C_DbgMsg("グループ会員ステータス      [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.group_kaiin_status.arr);
            C_DbgMsg("会社名                      [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.kaisha_mei.arr);
            C_DbgMsg("部署名                      [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.busho_mei.arr);
            C_DbgMsg("関心分野コード              [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.kanshin_bunya_cd.arr);
            C_DbgMsg("メール配信エラーフラグ      [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_error_flg);
            C_DbgMsg("メール配信許諾フラグ        [%d]\n", cmBTkaicBServiceDto.h_kaiin_info.mail_haishin_kyodaku_flg);
            C_DbgMsg("グローバル会員国コード      [%s]\n", cmBTkaicBServiceDto.h_kaiin_info.global_kaiin_kuni_cd.arr);

            /*---------------------------------------------------------------------*/
            C_DbgMsg(" 会員情報データファイル出力バッファ      [%s]\n", wk_out_buff);
            /*---------------------------------------------------------------------*/

        }
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("FileOutput処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----FileOutput Bottom------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ChkArgoInf                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ChkArgoInf( char *Arg_in )                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-o スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-o スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int ChkArgoInf(StringDto Arg_in) {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ChkArgoInf処理");
            /*---------------------------------------------------------------------*/
        }
        /*  重複指定チェック                   */
        if (arg_o_chk != DEF_OFF) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgoInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }
        arg_o_chk = DEF_ON;

        /*  設定値nullチェック  */

        if (Arg_in.arr.trim().length() <= 2) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgoInf *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        strcpy(fl_name, Arg_in.arr.substring(2));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("ChkArgoInf処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----ChkArgoInf Bottom------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ChkArgbInf                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ChkArgbInf( char *Arg_in )                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-b スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-b スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int ChkArgbInf(StringDto Arg_in) {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ChkArgbInf処理");
            /*---------------------------------------------------------------------*/
        }
        /*  重複指定チェック                   */
        if (arg_b_chk != DEF_OFF) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgbInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }
        arg_b_chk = DEF_ON;

        /*  設定値nullチェック  */
        if (Arg_in.arr.trim().length() <= 2) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgbInf *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        gi_kijunchi = atoi(Arg_in.arr.substring(2));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("ChkArgbInf処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----ChkArgbInf Bottom------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ChkArgdInf                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ChkArgdInf( char *Arg_in )                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-d スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-d スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int ChkArgdInf(StringDto Arg_in) {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ChkArgdInf処理");
            /*---------------------------------------------------------------------*/
        }
        /*  重複指定チェック                   */
        if (arg_d_chk != DEF_OFF) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgdInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }
        arg_d_chk = DEF_ON;

        /*  設定値nullチェック  */
        if (Arg_in.arr.trim().length() <= 2) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ChkArgdInf *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        gi_target_date = atoi(Arg_in.arr.substring(2));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("ChkArgdInf処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----ChkArgdInf Bottom------------------------------------------------------*/
    }
}
