package jp.co.mcc.nttdata.batch.business.com.cmBTdmupB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTdmupB.dto.CmBTdmupBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： ＤＭ止め区分一括更新（cmBTdnupB.pc）
 *
 *   【処理概要】
 *     DM止め区分一括更新対象会員リストを入力として、
 *     DM止め区分を送信不可に更新を行う。
 *
 *   【引数説明】
 *     -i      :（必須）DM止め区分更新対象会員番号ファイル名
 *     -o      :（必須）DM止め区分更新結果ファイル名
 *     -DEBUG(-debug)      :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *      10     ： 正常
 *      49     ： 警告（ヘッダ不正）
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2019/08/06 SSI.上野：初版
 *     30.00 :  2021/02/02 NDBS.緒方: 期間限定Ｐ対応によりリコンパイル
 *                                    (顧客データロック処理内容更新のため)
 *     40.00 :  2022/09/30 SSI.川内：MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTdmupBImpl extends CmABfuncLServiceImpl implements CmBTdmupB {


    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTdmupBDto cmBTdmupBDto = new CmBTdmupBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */

    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_I = "-i";                /* 入力ファイル名                     */
    String DEF_ARG_O = "-o";                /* 出力ファイル名                     */
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */

    /*---------------------------------------------*/
    String C_PRGNAME = "ＤＭ止め区分一括更新";         /* APログ用機能名         */
    int DEF_Read_EOF = 9;                 /* File read EOF                      */
    int C_const_APWR = 49;                 /* 警告終了値                         */

    /*-----  ファイルヘッダ----------*/
    String C_IN_HEADER = "DM止め区分更新";                            /* 入力   */
    String C_OUT_HEADER = "\"会員番号\",\"顧客番号\",\"処理内容\"";    /* 出力   */

    /*-----  処理結果----------*/
    /* 処理結果 */
    int C_STATUS_CNT = 3;       /* 処理結果件数                       */
    int C_STATUS_OK_UPD = 6010;    /* 更新あり                           */
    int C_STATUS_OK_NOUPD = 6011;    /* 更新なし                           */
    int C_STATUS_NG = 6099;    /* 顧客基盤登録なし                   */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int arg_i_chk;                      /* 引数iチェック用              */
    int arg_o_chk;                      /* 引数oチェック用              */

    StringDto bat_yyyymmdd = new StringDto(8 + 1);                  /* バッチ処理日付(当日)   */
    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログ用               */

    int out_status;                   /* 処理結果                       */
    StringDto fl_name_in = new StringDto(256);              /* 入力ファイル名                 */
    StringDto fl_name_out = new StringDto(256);             /* 出力ファイル名                 */
    StringDto fl_dir_in = new StringDto(4096);              /* 入力ファイルのディレクトリ     */
    StringDto fl_dir_out = new StringDto(4096);             /* 出力ファイルのディレクトリ     */
    StringDto read_buf = new StringDto(256);                /* 入力ファイル読み込みバッファ   */

    long sel_riyu_info_cnt;            /* MS理由情報データ件数           */
    long input_data_cnt;               /* 入力データ件数                 */
    long input_data_ok_cnt;            /* 入力データ正常処理件数         */
    long file_write_cnt;               /* ファイル出力件数               */
    long upd_data_cnt_ok;          /* MM顧客企業別属性情報更新あり件数   */
    long upd_data_cnt_no;          /* MM顧客企業別属性情報更新なし件数   */

    FileStatusDto fp_in = new FileStatusDto();                         /* 入力ファイルポインタ         */
    FileStatusDto fp_out = new FileStatusDto();                        /* 出力ファイルポインタ         */

    /* MS理由情報   バッファ(SJIS変換用) */
    private class RIYU_INFO_SJIS {
        int riyu_cd;                        /* 理由コード           PK*/
        StringDto riyu_setumei_sjis = new StringDto(100 * 3 + 1);     /* 理由説明(SJIS変換)     */
    }

    RIYU_INFO_SJIS[] g_RiyuInfo = new RIYU_INFO_SJIS[C_STATUS_CNT];

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
        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                   */
        String env_wrk;                       /* 環境変数取得用                 */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /* HOST変数にプログラム名をコピー */
        memset(cmBTdmupBDto.h_saishu_koshin_programid, 0x00, sizeof(cmBTdmupBDto.h_saishu_koshin_programid));
        memcpy(cmBTdmupBDto.h_saishu_koshin_programid, Cg_Program_Name, sizeof(Cg_Program_Name));

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            /*------------------------------------------------------------*/
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

        memset(fl_name_in, 0x00, sizeof(fl_name_in));
        memset(fl_name_out, 0x00, sizeof(fl_name_out));

        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {

            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }
            if (0 == memcmp(arg_Work1, DEF_ARG_I, strlen(DEF_ARG_I)) ||
                    0 == memcmp(arg_Work1, DEF_ARG_O, strlen(DEF_ARG_O))) {
                /* -i,-o出力ファイルCHK */
                rtn_cd = cmBTdmupB_ChkArgiInf(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    sprintf(log_format_buf,
                            "入力ファイル名引数が誤っています（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                    return exit(C_const_APNG);
                }
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** main *** 引数取得（入力ファイル名）[%s]\n",
                            fl_name_in);
                    C_DbgMsg("*** main *** 引数取得（出力ファイル名）[%s]\n",
                            fl_name_out);
                    /*-------------------------------------------------------------*/
                }
            } else if (0 == strcmp(arg_Work1, DEF_DEBUG) ||
                    0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
            } else {
                /* 定義外パラメータ   */
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", C_const_APNG, 0, 0);
                    /*---------------------------------------------*/
                }
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /* 必須パラメータ未指定チェック */
        if (arg_i_chk == DEF_OFF) {
            sprintf(log_format_buf, "-i 引数の値が不正です");
            APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (arg_o_chk == DEF_OFF) {
            sprintf(log_format_buf, "-o 引数の値が不正です");
            APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理 */
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
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n",
                    "CM_FILENOWRCV");
            /*-------------------------------------------------------------*/
        }
        env_wrk = getenv("CM_FILENOWRCV");
        if (StringUtils.isEmpty(env_wrk)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_FILENOWRCV]%s\n", "NULL");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_FILENOWRCV)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        /* 入力ファイルDIRセット */
        strcpy(fl_dir_in, env_wrk);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）[%s]\n", fl_dir_in);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n",
                    "CM_APWORK_DATE");
            /*-------------------------------------------------------------*/
        }
        env_wrk = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_wrk)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        /* 出力ファイルDIRセット */
        strcpy(fl_dir_out, env_wrk);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", fl_dir_out);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_SD);
            /*------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  バッチ処理日取得(当日指定)         */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得(当日指定)%s\n", "");
            /*------------------------------------------------------------*/
        }

        memset(bat_yyyymmdd, 0x00, sizeof(bat_yyyymmdd));
        // 当日を指定
        rtn_cd = C_GetBatDate(0, bat_yyyymmdd, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG status= %d\n",
                        rtn_status);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);

            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得(当日指定)OK [%s]\n", bat_yyyymmdd);
            /*------------------------------------------------------------*/
        }
        cmBTdmupBDto.h_bat_yyyymmdd = (int) atol(bat_yyyymmdd);

        /*-------------------------------------*/
        /*  MS理由情報取得処理                 */
        /*-------------------------------------*/
        rtn_cd = cmBTdmupB_SelRiyuInfo();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTdmupB_SelRiyuInfo処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*--------------------------------*/
        /* ファイルOPEN                   */
        /*--------------------------------*/
        rtn_cd = cmBTdmupB_OpenFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTdmupB_OpenFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*--------------------------------*/
        /* ファイル出力（先頭行）         */
        /*--------------------------------*/
        rtn_cd = cmBTdmupB_WriteFileHead();
        if (rtn_cd != C_const_OK) {
            APLOG_WT("912", 0, null, "ファイル出力処理（先頭行）に失敗しました",
                    0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTdmupB_WriteFileHead処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  DM止め区分更新処理                           */
        /*-----------------------------------------------*/
        rtn_cd = cmBTdmupB_Main();
        if (rtn_cd == C_const_NG) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTdmupB_Main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "DM止め区分更新処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* ロールバック */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* ファイルクローズ */
            cmBTdmupB_CloseFile();

            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        if (rtn_cd != C_const_OK) {
            rtn_status.arr = C_const_APWR;      /* 警告（ヘッダ不正） */
        } else {
            rtn_status.arr = C_const_APOK;      /* 正常 */

            /*--------------------------------*/
            /* 入力件数出力                   */
            /*--------------------------------*/
            sprintf(log_format_buf, "%s/%s", fl_dir_in, fl_name_in);
            APLOG_WT("104", 0, null, log_format_buf, input_data_cnt,
                    input_data_ok_cnt, 0, 0, 0);

            /*--------------------------------*/
            /* 出力件数出力                   */
            /*--------------------------------*/
            sprintf(log_format_buf, "%s/%s", fl_dir_out, fl_name_out);
            APLOG_WT("105", 0, null, log_format_buf, file_write_cnt,
                    0, 0, 0, 0);

            /* MM顧客企業別属性情報 更新件数出力 */
            APLOG_WT("107", 0, null, "MM顧客企業別属性情報(更新あり)",
                    upd_data_cnt_ok, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MM顧客企業別属性情報(更新なし)",
                    upd_data_cnt_no, 0, 0, 0, 0);
        }

        /* コミット解放処理 */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        /* ファイルクローズ */
        cmBTdmupB_CloseFile();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */

        return exit(rtn_status.arr);
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_Main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTdmupB_Main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      DM止め区分更新処理                                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             -1   ： 警告（ヘッダ不正）                                     */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_Main() {
        int rtn_cd;                           /* 関数戻り値                   */
        int head_flg;                         /* ヘッダ行有無フラグ           */
        IntegerDto utf8_len = new IntegerDto();                         /* UTF8変換用                   */
        StringDto utf8_buf = new StringDto();                   /* UTF8文字列格納領域           */


        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTdmupB_Main処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /* 初期化 */
        head_flg = 0;
        input_data_cnt = 0;
        input_data_ok_cnt = 0;
        file_write_cnt = 0;
        upd_data_cnt_ok = 0;
        upd_data_cnt_no = 0;

        /*  入力ファイル読み込みループ */
        while (true) {
            /*------------------------------*/
            /* ファイル読み込み             */
            /*------------------------------*/
            rtn_cd = cmBTdmupB_ReadFile();
            if (rtn_cd == DEF_Read_EOF) {
                if (head_flg != 1) {
                    /* ヘッダ行なしの場合 */
                    sprintf(log_format_buf, "ファイルヘッダなし　入力ファイル=%s",
                            fl_name_in);
                    APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    /* 処理を警告終了する */
                    return C_const_NOTEXISTS;
                }
                break;
            } else if (rtn_cd != C_const_OK) {

                APLOG_WT("912", 0, null, "ファイル読み込み処理に失敗しました",
                        0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTdmupB_Main処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return C_const_NG;
            }

            /* 入力データ件数カウントアップ */
            input_data_cnt++;

            /* 先頭行の場合 */
            if (input_data_cnt == 1) {
                /* SJIS→UTF8変換処理 */
                utf8_len.arr = 0;
                /// memset(utf8_buf, 0x00, sizeof(utf8_buf));
                rtn_cd = C_ConvSJ2UT(read_buf, strlen(read_buf), utf8_buf,
                        utf8_len);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd,
                            0, 0, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }

                /* 先頭行チェック */
                if (strncmp(StringUtils.join(utf8_buf), C_IN_HEADER, strlen(C_IN_HEADER)) != 0) {
                    /* 先頭行が想定と異なる */
                    sprintf(log_format_buf, "ファイルヘッダ不正　入力ファイル=%s",
                            fl_name_in);
                    APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    /* 処理を警告終了する */
                    return C_const_NOTEXISTS;
                }

                /* ヘッダ行あり */
                head_flg = 1;
                /* 次のレコードへ */
                continue;
            }

            /* 以下、先頭行以外の場合 */
            /* 初期化 */
            out_status = 0;

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_Main *** 読込[%s]\n", read_buf);
                C_DbgMsg("*** cmBTdmupB_Main *** 桁数[%d]\n", (int) strlen(read_buf));
                /*------------------------------------------------------------*/
            }
            if (strlen(read_buf) != 0 && strlen(read_buf) <= 17) {
                /* 数値チェック */
                rtn_cd = cmBTdmupB_CheckNumber(read_buf);
                if (rtn_cd != C_const_OK) {
                    /* 数値不正(顧客基盤登録なし) */
                    out_status = C_STATUS_NG;
                } else {
                    /* MSカード情報取得 */
                    rtn_cd = cmBTdmupB_SelCardInfo();
                    if (rtn_cd != C_const_OK) {
                        /* 処理を終了する */
                        return C_const_NG;
                    }
                }
            } else {
                /* 顧客基盤登録なし */
                out_status = C_STATUS_NG;
            }

            if (out_status != C_STATUS_NG) {
                /* MM顧客企業別属性情報更新 */
                rtn_cd = cmBTdmupB_UpdKigyobetuInfo();
                if (rtn_cd != C_const_OK) {
                    /* 処理を終了する */
                    return C_const_NG;
                }
            }

            /* ファイル出力 */
            rtn_cd = cmBTdmupB_WriteFile();
            if (rtn_cd != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 入力データ正常処理件数カウントアップ */
            input_data_ok_cnt++;

        } /* FOR END */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_Main処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_ChkArgiInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_ChkArgiInf( char *Arg_in )                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：引数                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_ChkArgiInf(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_ChkArgiInf処理");
            /*---------------------------------------------------------------------*/
        }

        if (0 == memcmp(Arg_in, DEF_ARG_I, 2)) {       /* -i出力ファイルCHK      */
            /* 重複指定チェック */
            if (arg_i_chk != DEF_OFF) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTdmupB_ChkArgiInf *** 重複指定NG(i)=%s\n",
                            Arg_in);
                    /*-------------------------------------------------------------*/
                }
                return C_const_NG;
            }
            /* 設定値Nullチェック */
            if (StringUtils.isEmpty(Arg_in.arr.substring(2))) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTdmupB_ChkArgiInf *** 設定値Null(i)=%s\n",
                            Arg_in);
                    /*-------------------------------------------------------------*/
                }
                return C_const_NG;
            }
            arg_i_chk = DEF_ON;
            strcpy(fl_name_in, Arg_in.arr.substring(2));
        } else if (0 == memcmp(Arg_in, DEF_ARG_O, 2)) {  /* -o出力ファイルCHK      */
            /* 重複指定チェック */
            if (arg_o_chk != DEF_OFF) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTdmupB_ChkArgiInf *** 重複指定NG(o)=%s\n",
                            Arg_in);
                    /*-------------------------------------------------------------*/
                }
                return C_const_NG;
            }
            /* 設定値Nullチェック */
            if (StringUtils.isEmpty(Arg_in.arr.substring(2))) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTdmupB_ChkArgiInf *** 設定値Null(o)=%s\n",
                            Arg_in);
                    /*-------------------------------------------------------------*/
                }
                return C_const_NG;
            }
            arg_o_chk = DEF_ON;
            strcpy(fl_name_out, Arg_in.arr.substring(2));
        }
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_ChkArgiInf処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              入力ファイルをオープンする。                                  */
    /*              出力ファイルをオープンする。                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_OpenFile() {
        StringDto fp_name = new StringDto(4096);                      /** 出力ファイルパス名      **/

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_OpenFile処理");
            /*------------------------------------------------------------*/
        }

        /* 入力ファイル名作成、オープン */
        sprintf(fp_name, "%s/%s", fl_dir_in, fl_name_in);
        if ((fp_in = fopen(fp_name.arr, SystemConstant.Shift_JIS, FileOpenType.r)).fd ==C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_OpenFile *** 入力ファイルオープンERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* 出力ファイル名作成、オープン */
        sprintf(fp_name, "%s/%s", fl_dir_out, fl_name_out);
        if ((fp_out = fopen(fp_name.arr, SystemConstant.Shift_JIS, FileOpenType.w)).fd ==C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_OpenFile *** 出力ファイルオープンERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("mcBTrgtaB_OpenFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_ReadFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_ReadFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル読込処理                                              */
    /*              ファイルを読み込みする。                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_ReadFile() {
        int rtn_cd;                         /* 関数戻り値                     */
        StringDto wk_buf = new StringDto(256);                    /* 入力ファイル読み込みバッファ   */

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_ReadFile処理");
            /*----------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memset(read_buf, 0x00, sizeof(read_buf));

        fgets(wk_buf, sizeof(wk_buf) - 1, fp_in);
        if (feof(fp_in) != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** ReadFile *** READ EOF status= %d\n", feof(fp_in));
                /*---------------------------------------------*/
            }
            return DEF_Read_EOF;
        }

        rtn_cd = ferror(fp_in);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ReadFile *** 入力ファイルリードNG%s\n", "");
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "fgets(%s/%s)", fl_dir_in, fl_name_in);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_ReadFile処理", C_const_OK, 0, 0);
            /*----------------------------------------------------------------------*/
        }

        /* 改行コードCRLF除去 */
        strncpy(read_buf, wk_buf, strlen(wk_buf));
        read_buf.arr = read_buf.arr.replaceAll("\r\n", "");
        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_WriteFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_WriteFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル出力処理                                              */
    /*              ファイルを書き込みする。                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_WriteFile() {
        int i;                              /* ループカウンタ             */
        int rtn_cd;                         /* 関数戻り値                 */
        StringDto wk_buf = new StringDto(6000);                   /* 編集バッファ               */
        StringDto riyu_setumei_sjis = new StringDto(100 * 3 + 1);     /* 理由説明(SJIS変換)         */

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_WriteFile処理");
            /*----------------------------------------------------------------------*/
        }

        memset(riyu_setumei_sjis, 0x00, sizeof(riyu_setumei_sjis));
        /* 処理内容取得 */
        for (i = 0; i < sel_riyu_info_cnt; i++) {
            if (g_RiyuInfo[i].riyu_cd == out_status) {
                strcpy(riyu_setumei_sjis, g_RiyuInfo[i].riyu_setumei_sjis);
                break;
            }
        }

        /* 初期化 */
        memset(wk_buf, 0x00, sizeof(wk_buf));

        /*--------------------------------*/
        /* 出力レコードバッファの編集     */
        /*--------------------------------*/
        if (out_status == C_STATUS_NG) {
            /* 顧客基盤登録なし */
            sprintf(wk_buf, "%s,,%d:%s\r\n",
                    read_buf,
                    out_status - 6000,
                    riyu_setumei_sjis);
        } else {
            /* 更新あり、更新なし */
            sprintf(wk_buf, "%s,%s,%d:%s\r\n",
                    cmBTdmupBDto.mscd_buff.kaiin_no.arr,
                    cmBTdmupBDto.mscd_buff.kokyaku_no.arr,
                    out_status - 6000,
                    riyu_setumei_sjis);
        }

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            /* C_DbgMsg( "*** cmBTdmupB_WriteFile *** buf=[%s]\n", wk_buf);         */
            /*----------------------------------------------------------------------*/
        }

        fp_out.write(wk_buf.arr);
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s/%s)", fl_dir_out, fl_name_out);
            APLOG_WT("903", 0, null, log_format_buf, rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTdmupB_WriteFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* ファイル出力件数カウントアップ */
        file_write_cnt++;

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_WriteFile処理", C_const_OK, 0, 0);
            /*----------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_WriteFileHead                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_WriteFileHead()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル出力処理（先頭行）                                    */
    /*              項目名の行をファイルに書き込みする。                          */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_WriteFileHead() {
        int rtn_cd;                           /* 関数戻り値                   */
        IntegerDto sjis_len = new IntegerDto();                         /* SJIS変換用                   */
        StringDto utf8_buf = new StringDto(256);                    /* UTF8文字列格納領域           */
        StringDto sjis_buf = new StringDto();                    /* SJIS文字列格納領域           */

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_WriteFileHead処理");
            /*----------------------------------------------------------------------*/
        }

        /* UTF8→SJIS変換処理 */
        sjis_len.arr = 0;
        // memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s\r\n", C_OUT_HEADER);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* ファイル出力処理 */
        fp_out.write(StringUtils.join(sjis_buf));
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s/%s)", fl_dir_out, fl_name_out);
            APLOG_WT("903", 0, null, log_format_buf, rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTdmupB_WriteFileHead処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_WriteFileHead処理", C_const_OK, 0, 0);
            /*----------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_CloseFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_CloseFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルクローズ処理                                          */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_CloseFile() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_CloseFile処理");
            /*------------------------------------------------------------*/
        }
        fclose(fp_in);
        fclose(fp_out);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_CloseFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_SelRiyuInfo                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_SelRiyuInfo()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              MS理由情報取得処理                                            */
    /*              MS理由情報から理由名称を取得する。                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_SelRiyuInfo() {
        int i;                              /* ループカウンタ               */
        int rtn_cd;                         /* 関数戻り値                   */
        IntegerDto sjis_len_riyu_setumei = new IntegerDto();          /* SJIS変換後文字列のレングス   */
        StringDto sjis_str_riyu_setumei = new StringDto(200);     /* SJIS変換後文字列(理由説明)   */
        StringDto wk_buff = new StringDto(512);                   /* ファイル出力内容編集バッファ */
        StringDto wk_buff2 = new StringDto(512);                  /* 編集バッファ                 */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_SelRiyuInfo処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        sel_riyu_info_cnt = 0;

        /* カーソル宣言 */
        /*
        EXEC SQL DECLARE CUR_RIYU CURSOR FOR
        SELECT  理由コード
                ,NVL(TRIM(理由説明),' ')
        FROM   MS理由情報
        WHERE  理由コード in (6010,6011,6099)
        ORDER BY 理由コード;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT  理由コード\n" +
                "                ,NVL(NULLIF(TRIM(理由説明),''),' ')\n" +
                "        FROM   MS理由情報\n" +
                "        WHERE  理由コード in (6010,6011,6099)\n" +
                "        ORDER BY 理由コード";
        SqlstmDto sqlca = sqlcaManager.get("CUR_RIYU");
        sqlca.sql = workSql;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_RIYU;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "CURSOR OPEN ERR", sqlca.sqlcode,
                    "MS理由情報", "理由コード in (6010,6011,6099)", 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdmupB_SelRiyuInfo", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        /* バッファ初期化 */
        memset(g_RiyuInfo, 0x00, sizeof(g_RiyuInfo));

        for (i = 0; i < C_STATUS_CNT; i++) {

            /* バッファ初期化 */
            memset(cmBTdmupBDto.msryinf_t, 0x00, 0);

            /* カーソルフェッチ */
            /*
            EXEC SQL FETCH CUR_RIYU
            INTO   :msryinf_t.riyu_cd,
                :msryinf_t.riyu_setumei;
            */
            sqlca.fetch();
            sqlca.recData(cmBTdmupBDto.msryinf_t.riyu_cd, cmBTdmupBDto.msryinf_t.riyu_setumei);

            /* データ無しの場合、ブレーク */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
                /* ＥＯＦ以外のエラーは異常終了 */
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "FETCH ERR", sqlca.sqlcode,
                        "MS理由情報", "理由コード in (6010,6011,6099)", 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("cmBTdmupB_SelRiyuInfo", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                // EXEC SQL CLOSE CUR_RIYU;
//                sqlca.close();
                sqlcaManager.close("CUR_RIYU");
                return C_const_NG;
            }

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_SelRiyuInfo msryinf_t.riyu_cd=[%d]\n",
                        cmBTdmupBDto.msryinf_t.riyu_cd);
                C_DbgMsg("*** cmBTdmupB_SelRiyuInfo msryinf_t.riyu_setumei=[%s]\n",
                        cmBTdmupBDto.msryinf_t.riyu_setumei);
                /*------------------------------------------------------------*/
            }

            /*------------------------*/
            /* 理由説明の全角SJIS変換 */
            /*------------------------*/
            memset(wk_buff, 0x00, sizeof(wk_buff));
            memset(wk_buff2, 0x00, sizeof(wk_buff2));

            memcpy(wk_buff, cmBTdmupBDto.msryinf_t.riyu_setumei.arr(),
                    cmBTdmupBDto.msryinf_t.riyu_setumei.len);
            BT_Rtrim(wk_buff, strlen(wk_buff));

            /* 全角に変換する */
            rtn_cd = C_ConvHalf2Full(wk_buff, wk_buff2);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                        0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("cmBTdmupB_SelRiyuInfo", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                // EXEC SQL CLOSE CUR_RIYU;
//                sqlca.close();
                sqlcaManager.close("CUR_RIYU");
                return C_const_NG;
            }

            memset(sjis_str_riyu_setumei, 0x00, sjis_str_riyu_setumei.len);
            /* UTF8 → SJIS変換処理 */
            rtn_cd = C_ConvUT2SJ(wk_buff2, strlen(wk_buff2),
                    sjis_str_riyu_setumei, sjis_len_riyu_setumei);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("cmBTdmupB_SelRiyuInfo", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                // EXEC SQL CLOSE CUR_RIYU;
                sqlcaManager.close("CUR_RIYU");
                return C_const_NG;
            }
            BT_Rtrim(sjis_str_riyu_setumei, sjis_str_riyu_setumei.len);

            /* 取得情報を退避 */
            RIYU_INFO_SJIS riyu_info_sjis = new RIYU_INFO_SJIS();
            riyu_info_sjis.riyu_cd = cmBTdmupBDto.msryinf_t.riyu_cd.intVal();
            g_RiyuInfo[i] = riyu_info_sjis;
            memcpy(g_RiyuInfo[i].riyu_setumei_sjis, StringUtils.join(sjis_str_riyu_setumei),
                    sjis_len_riyu_setumei.arr);

            /* 取得件数カウントアップ */
            sel_riyu_info_cnt++;
        }

        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_RIYU;
        sqlcaManager.close("CUR_RIYU");

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_SelRiyuInfo", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdmupB_SelRiyuInfo Bottom ------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_SelCardInfo                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_SelCardInfo()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              MSカード情報取得処理                                          */
    /*              MSカード情報から顧客番号を取得する。                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_SelCardInfo() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_SelCardInfo処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(cmBTdmupBDto.mscd_buff, 0x00, 0);

        /* 会員番号(改行コードCRLF) */
        memcpy(cmBTdmupBDto.mscd_buff.kaiin_no, read_buf.arr, strlen(read_buf));
        cmBTdmupBDto.mscd_buff.kaiin_no.len = strlen(read_buf);

        /* MSカード情報取得 */
        /*
        EXEC SQL SELECT to_char(NVL(顧客番号,0), 'FM000000000000000' )
                    ,NVL(企業コード, 0)
        INTO :mscd_buff.kokyaku_no,
                    :mscd_buff.kigyo_cd
        FROM MSカード情報
        WHERE サービス種別 = 1
        AND 会員番号 = :mscd_buff.kaiin_no;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT to_char(NVL(顧客番号,0), 'FM000000000000000' )\n" +
                "                    ,NVL(企業コード, 0)\n" +
                "        FROM MSカード情報\n" +
                "        WHERE サービス種別 = 1\n" +
                "        AND 会員番号 = ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTdmupBDto.mscd_buff.kaiin_no);

        sqlca.fetchInto(cmBTdmupBDto.mscd_buff.kokyaku_no, cmBTdmupBDto.mscd_buff.kigyo_cd);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            sprintf(log_format_buf, "サービス種別=1 会員番号=%s",
                    cmBTdmupBDto.mscd_buff.kaiin_no.arr);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MSカード情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdmupB_SelCardInfo", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }
        /* データ無しエラーの場合 */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 顧客基盤登録なし */
            out_status = C_STATUS_NG;
            /* 処理を終了する */
            return C_const_OK;
        }

        /* 顧客番号が０の場合 */
        if (cmBTdmupBDto.mscd_buff.kokyaku_no.arr().charAt(0) == '0' &&
                cmBTdmupBDto.mscd_buff.kokyaku_no.len == 1) {
            /* 顧客基盤登録なし */
            out_status = C_STATUS_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdmupB_SelCardInfo mscd_buff.kokyaku_no=[%s]\n",
                    cmBTdmupBDto.mscd_buff.kokyaku_no.arr);
            C_DbgMsg("*** cmBTdmupB_SelCardInfo mscd_buff.kigyo_cd=[%d]\n",
                    cmBTdmupBDto.mscd_buff.kigyo_cd);
            /*------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdmupB_SelCardInfo", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*--- cmBTdmupB_SelCardInfo Bottom ------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_UpdKigyobetuInfo                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmupB_UpdKigyobetuInfo()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報更新処理                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_UpdKigyobetuInfo() {
        int rtn_cd;                    /* 関数戻り値                          */
        IntegerDto rtn_status = new IntegerDto();                /* 関数ステータス                      */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTdmupB_UpdKigyobetuInfo処理");
            /*---------------------------------------------------------------------*/
        }

        /* 顧客データロック */
        rtn_cd = C_KdataLock(cmBTdmupBDto.mscd_buff.kokyaku_no.strDto(), "2", rtn_status);

        /* 顧客データロック処理エラーの場合 */
        if (rtn_cd != C_const_OK && rtn_cd != C_const_NOTEXISTS) {
            if (DBG_LOG) {
                /*-----------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_UpdKigyobetuInfo *** 顧客ロック NG\n" +
                        "status= %d\n", rtn_status);
                /*-----------------------------------------------------------------*/
            }
            APLOG_WT("913", 0, null, cmBTdmupBDto.mscd_buff.kokyaku_no.arr,
                    0, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        /* MM顧客企業別属性情報更新 */
        /*
        EXEC SQL UPDATE MM顧客企業別属性情報@CMMD
        SET ＤＭ止め区分 = 3001,
        バッチ更新日 = :h_bat_yyyymmdd,
                最終更新日 = :h_bat_yyyymmdd,
                最終更新日時 = SYSDATE,
                最終更新プログラムＩＤ = :h_saishu_koshin_programid
        WHERE 顧客番号   = :mscd_buff.kokyaku_no
        *//* 2022/09/30 MCCM初版 MOD START *//*
         *//*              AND   企業コード not in (1020,1040)*//*
        AND   企業コード in (1010,3010,3050)
        *//* 2022/09/30 MCCM初版 MOD END *//*
        AND   ＤＭ止め区分 =3000;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "UPDATE MM顧客企業別属性情報\n" +
                "        SET ＤＭ止め区分 = 3001,\n" +
                "        バッチ更新日 = ?,\n" +
                "                最終更新日 = ?,\n" +
                "                最終更新日時 = SYSDATE(),\n" +
                "                最終更新プログラムＩＤ = ?\n" +
                "        WHERE 顧客番号   = ?\n" +
                "        AND   企業コード in (1010,3010,3050)\n" +
                "        AND   ＤＭ止め区分 =3000";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTdmupBDto.h_bat_yyyymmdd, cmBTdmupBDto.h_bat_yyyymmdd,
                cmBTdmupBDto.h_saishu_koshin_programid, cmBTdmupBDto.mscd_buff.kokyaku_no);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            sprintf(log_format_buf, "顧客番号=%s",
                    cmBTdmupBDto.mscd_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "MM顧客企業別属性情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdmupB_UpdKigyobetuInfo", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* ロールバック */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* 処理を終了する */
            return C_const_NG;
        }

        /* データ無しの場合 */
        if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 更新なし */
            out_status = C_STATUS_OK_NOUPD;
            /* 更新なし件数カウントアップ */
            upd_data_cnt_no++;
        } else {
            /* 更新あり */
            out_status = C_STATUS_OK_UPD;
            /* 更新あり件数カウントアップ */
            upd_data_cnt_ok++;
        }

        /* コミット */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("cmBTdmupB_UpdKigyobetuInfo", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;

        /*-----cmBTdmupB_UpdKigyobetuInfo Bottom-------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmupB_CheckNumber                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int cmBTdmupB_CheckNumber( char *numstring )                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      数値チェックを行う。（マイナス数値も不可）                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char *numstring  指定エリア                                           */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常（数値である）                                     */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTdmupB_CheckNumber(StringDto numstring) {
        int i;
        int len;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdmupB_CheckNumber *** 指定文字列 [%s]\n", numstring);
            /*------------------------------------------------------------*/
        }

        len = strlen(numstring);

        for (i = 0; i < len; i++) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmupB_CheckNumber *** 指定文字列1 [%c]\n",
                        numstring.arr.charAt(i));
                C_DbgMsg("*** cmBTdmupB_CheckNumber *** 数値チェック [%d]\n",
                        isdigit(numstring.arr.charAt(i)));
                /*------------------------------------------------------------*/
            }
            if (0 == isdigit(numstring.arr.charAt(i))) {
                return C_const_NG;
            }
        }
        return C_const_OK;
    }

}
