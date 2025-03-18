package jp.co.mcc.nttdata.batch.business.service.cmBTcardB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TscdBuff;
import jp.co.mcc.nttdata.batch.business.com.cmABendeB.CmABendeBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.Math.log10;

@Service
public class CmBTcardBServiceImpl extends CmABfuncLServiceImpl implements CmBTcardBService {


    @Autowired
    CmABendeBServiceImpl cmABendeBService;
    public static String PG_NAME = "一括カード発行";
    public static int DEF_OFF = 0;
    public static int DEF_ON = 1;
    public static String DEF_DEBUG = "-DEBUG";       /* デバッグスイッチ             */
    public static String DEF_ARG_O = "-o";            /* エラーファイル名             */
    public static String DEF_debug = "-debug";       /* デバッグスイッチ             */

    public static int C_const_APWR = 49;

    String h_kaiin_no;               /* 会員番号                     */    /* 2022/10/06 MCCM初版 MOD */
    int h_status;                       /* ステータス                   */
    static int h_shori_ymd;                    /* 処理年月日(バッチ処理日付前日)*/
    static int h_bat_ymd;                      /* バッチ処理日付               */
    int h_kaiin_kigyo_cd;               /* 会員企業コード               */
    String h_saishu_koshin_programid;/* 最終更新プログラムID         */
    int h_kigyo_cd;                     /* 企業コード　                 */
    int h_kyuhansya_cd;              /* 旧販社コード                 */
    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int arg_o_chk;
    /**
     * 引数oチェック用
     **/
    String arg_o_Value;
    /**
     * 引数o設定値
     **/
    /*---------------------------------------------*/

    static StringDto bat_date = new StringDto();
    /**
     * バッチ処理日付
     **/
    static StringDto bat_date_prev = new StringDto();
    /**
     * バッチ処理日付前日
     **/
    String out_format_buf;
    /**
     * APログフォーマット
     **/
    String ap_work_dir;
    /**
     * 出力ファイルディレクトリ
     **/

    int in_rec_cnt;
    /**
     * 入力レコード件数
     **/
    int ok_rec_cnt;
    /**
     * 正常データ件数
     **/
    int ng_rec_cnt;
    /**
     * エラーデータ件数
     **/

    int in_rec_total;
    /**
     * 入力レコード合計件数
     **/
    int ok_rec_total;
    /**
     * 正常データ出力合計件数
     **/
    int ng_rec_total;
    /**
     * エラーデータ合計件数
     **/
    int out_err_cnt;
    /**
     * エラーファイル出力件数
     **/
    int status99_cnt;
    /**
     * 全件INSERTエラー(99)件数
     **/

    int cm_rec_cnt;
    /**
     * 3000件毎コミット件数
     **/
    FileStatusDto fp_out;

    /*******************************************************************************
     *   プログラム名   ： 一括カード発行処理（cmBTcardB）
     *
     *   【処理概要】
     *       TS一括カード発行情報から、MSカード情報に新規カードデータを作成する。
     *
     *   【引数説明】
     *  -oエラーファイル名              : エラーファイル名
     *  -DEBUG(-debug)                  : デバッグモードでの実行
     *                                     （トレース出力機能が有効）
     *
     *   【戻り値】
     *      10   ： 正常
     *      49   ： 警告
     *      99   ： 異常
     *
     *------------------------------------------------------------------------------
     *   稼働環境
     *      Red Hat Enterprise Linux 5（64bit）
     *      (文字コード ： UTF8)
     *------------------------------------------------------------------------------
     *   改定履歴
     *      1.00 :  2012/12/27 SSI.Suyama ： 初版
     *      2.00 :  2016/03/14 SSI.Tagashira :モバイルカスタマーポータル対応
     *                                         モバイル会員番号の一括発行を追加
     *      3.00 :  2016/07/20 SSI.Tagashira :C-MAS対応
     *                                        EC会員番号の一括発行追加
     *      40.00:  2022/10/06 SSI.申        :MCCM初版
     *      40.00:  2023/01/17 SSI.陳セイキン:【MCCM対応】H040
     *      41.00 : 2023/05/23 SSI.陳セイキン:MCCMPH2
     *------------------------------------------------------------------------------
     *  $Id:$
     *------------------------------------------------------------------------------
     *  Copyright (C) 2010 NTT DATA CORPORATION
     ******************************************************************************/
    @Override
    public MainResultDto main(int argc, String[] args) {
        args[0] = "cmBTcardB";
        openLog();
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                         /** 関数戻り値                       **/
        IntegerDto rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int arg_cnt;                        /** 引数チェック用カウンタ           **/
        String env_wrk;                       /** 出力ファイルDIR                  **/
        String arg_Work1 = null;                /** Work Buffer1                     **/

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /*-----------------------------------------------*/
        /*  プログラム名取得処理                         */
        /*-----------------------------------------------*/
        rtn_cd = C_GetPgname(args);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }
        /*  開始メッセージ */
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* HOST変数にバージョン付きプログラム名をコピーする */
        memset(h_saishu_koshin_programid, 0x00,
                sizeof(h_saishu_koshin_programid));
        h_saishu_koshin_programid = memcpy(h_saishu_koshin_programid, Cg_Program_Name,
                sizeof(Cg_Program_Name));

        /*-----------------------------------------------*/
        /*  バッチデバッグ開始処理                       */
        /*-----------------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, args);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  入力引数チェック                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        arg_o_chk = DEF_OFF;
        memset(arg_o_Value, 0x00, sizeof(arg_o_Value));
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "START");
            /*---------------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        rtn_cd = C_const_OK;                /* 関数戻り値 */
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
//            memset(arg_Work1, 0x00 , sizeof(arg_Work1));
            arg_Work1 = strcpy(arg_Work1, args[arg_cnt]);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
//            if (strcmp(arg_Work1, DEF_DEBUG)==1|| strcmp(arg_Work1, DEF_debug) == 1) {
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else if (memcmp(arg_Work1, DEF_ARG_O, 2) == 0) {
                /* エラーファイル名指定 -o */
                rtn_cd = Chk_ArgoInf(arg_Work1);

            } else {
                /* 規定外パラメータ  */
                rtn_cd = C_const_NG;
                out_format_buf = sprintf(out_format_buf,
                        "定義外の引数（%s）", arg_Work1);
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = [%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }

            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------*/
                    C_DbgMsg("*** main *** チェックNG = [%d]\n", rtn_cd);
                    /*-----------------------------------------------------*/
                }
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
        }

        /*-----------------------------------------------*/
        /*  環境変数取得                                 */
        /*-----------------------------------------------*/

        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "");
        /*-------------------------------------------------------------*/

        env_wrk = getenv("CM_APWORK_DATE");

        if (StringUtils.isEmpty(env_wrk)) {

            /*---------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得NG（出力ファイルDIR）%s\n", "");
            /*---------------------------------------------*/

            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** 環境変数取得OK（出力ファイルDIR）%s\n", env_wrk);
        /*-------------------------------------------------------------*/


        memset(ap_work_dir, 0x00, sizeof(ap_work_dir));
        ap_work_dir = strcpy(ap_work_dir, env_wrk);

        /*-----------------------------------------------*/
        /*  DBコネクト処理                               */
        /*-----------------------------------------------*/
        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** DBコネクト%s\n", "");
        /*-------------------------------------------------------------*/
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
            /*-------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0,
                    0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトOK rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトOK status= %d\n", rtn_status);
            /*-------------------------------------------------------------*/
        }
        /*-----------------------------------------------*/
        /*  バッチ処理日取得処理呼び出し                 */
        /*-----------------------------------------------*/
        if (DBG_LOG) {   /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
            /*-------------------------------------------------------------*/
        }
        memset(bat_date, 0x00, sizeof(bat_date));
        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));

        rtn_cd = C_GetBatDate(-1, bat_date_prev, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
            /*---------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得（前日）OK [%s]\n", bat_date_prev);
            /*---------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
            /*---------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {    /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_date);
        }
        /*---------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ファイルオープン                             */
        /*-----------------------------------------------*/

        /* 出力ファイル名が指定されている場合 */
        if (StringUtils.isNotEmpty(arg_o_Value)) {
            rtn_cd = OpenFile();
            if (rtn_cd != C_const_OK) {
                if (fp_out.fd == 0x00) {
                    APLOG_WT("912", 0, null,
                            "エラーファイルのオープンに失敗しました",
                            0, 0, 0, 0, 0);
                }
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** ファイルオープンOK rtn= %d\n", rtn_cd);
            /*---------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /* 初期化 */
        in_rec_cnt = 0;
        ok_rec_cnt = 0;
        ng_rec_cnt = 0;

        in_rec_total = 0;
        ok_rec_total = 0;
        ng_rec_total = 0;

        out_err_cnt = 0;
        status99_cnt = 0;

        rtn_cd = cmBTcardB_main();
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** cmBTcardB_main NG rtn =[%d]\n", rtn_cd);
            /*------------------------------------------------------------*/
            APLOG_WT("912", 0, null, "一括カード発行処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* 出力ファイルがオープンされていれば、closeする */
            if (fp_out.fd != 0x00) {
                /*** Output File Close ***/
                fclose(fp_out);
            }
            /* ロールバック */
            /* EXEC SQL ROLLBACK RELEASE; */
//
//            {
//                struct sqlexd sqlstm;
//                sqlstm.sqlvsn = 13;
//                sqlstm.arrsiz = 0;
//                sqlstm.sqladtp = &sqladt;
//                sqlstm.sqltdsp = &sqltds;
//                sqlstm.iters = (unsigned int  )1;
//                sqlstm.offset = (unsigned int  )5;
//                sqlstm.cud = sqlcud0;
//                sqlstm.sqlest = (unsigned char  *)&sqlca;
//                sqlstm.sqlety = (unsigned short)4352;
//                sqlstm.occurs = (unsigned int  )0;
//                sqlcxt((void **)0, &sqlctx, &sqlstm, &sqlfpn);
//            }

            sqlca.rollback();

            /* バッチデバッグ終了処理呼び出し */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /* 出力ファイルが指定されていれば、closeする */
        if (StringUtils.isNotEmpty(arg_o_Value)) {
            /*** Output File Close ***/
            fclose(fp_out);
        }

        /* 各件数出力 */
        APLOG_WT("106", 0, null, "TS一括カード発行情報", in_rec_total,
                ok_rec_total, ng_rec_total, 0, 0);

        /* エラーファイル出力件数 */
        if (StringUtils.isEmpty(arg_o_Value) && (out_err_cnt > 0)) {
            APLOG_WT("710", 0, null, arg_o_Value,
                    out_err_cnt, 0, 0, 0, 0);
        }

        /* バッチデバッグ終了処理呼び出し */
        rtn_cd = C_EndBatDbg();

        /*------------------------------------------------------------*/
        C_DbgEnd("*** main処理 ***", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, PG_NAME, 0, 0, 0, 0, 0);
        /* コミット解放処理 */
        /* EXEC SQL COMMIT WORK RELEASE; */

//        {
//            struct sqlexd sqlstm;
//            sqlstm.sqlvsn = 13;
//            sqlstm.arrsiz = 0;
//            sqlstm.sqladtp = &sqladt;
//            sqlstm.sqltdsp = &sqltds;
//            sqlstm.iters = (unsigned int  )1;
//            sqlstm.offset = (unsigned int  )20;
//            sqlstm.cud = sqlcud0;
//            sqlstm.sqlest = (unsigned char  *)&sqlca;
//            sqlstm.sqlety = (unsigned short)4352;
//            sqlstm.occurs = (unsigned int  )0;
//            sqlcxt((void **)0, &sqlctx, &sqlstm, &sqlfpn);
//        }

        sqlcaManager.commitRelease(); // TODO:

        /* 終了値判定 */
        if (status99_cnt >= 1) rtn_cd = C_const_APWR;
        else rtn_cd = C_const_APOK;
        return exit(rtn_cd);
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgoInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgoInf( char *Arg_in )                                   */
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
    int Chk_ArgoInf(String Arg_in) {
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("Chk_ArgoInf処理");
        }
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  重複指定チェック                             */
        /*-----------------------------------------------*/
        if (arg_o_chk != DEF_OFF) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** Chk_ArgoInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            out_format_buf = sprintf(out_format_buf, "-o 引数が重複しています（%s）", Arg_in);
            return (C_const_NG);
        }
        arg_o_chk = DEF_ON;
        /*-----------------------------------------------*/
        /*  値の内容チェック                             */
        /*-----------------------------------------------*/
        /*  設定値Nullチェック  */
        if (StringUtils.isEmpty(Arg_in)) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** Chk_ArgiInf *** 設定値Null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            out_format_buf = sprintf(out_format_buf, "-o 引数の値が不正です（%s）", Arg_in);
            return (C_const_NG);
        }
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("Chk_ArgoInf処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        arg_o_Value = strcpy(arg_o_Value, Arg_in.substring(2));
        return (C_const_OK);
    }

    /*                                                                            */
    /*  関数名 ： OpenFile                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OpenFile()                                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              出力ファイルをオープンする                                    */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    int OpenFile() {
        String fl_name = null;
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgStart("OpenFile処理");
            /*-------------------------------------------------------------*/
        }
        /* 出力ファイル(エラーファイル)のオープン */
        fl_name = sprintf(fl_name, "%s/%s", ap_work_dir, arg_o_Value);

        if ((fp_out = open(fl_name)).fd == -1) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** OpenFile *** エラーファイルオープンNG%s\n", "");
                /*-------------------------------------------------------------*/
            }
            out_format_buf = sprintf(out_format_buf, "fopen（%s/%s）", ap_work_dir, arg_o_Value);
            APLOG_WT("903", 0, null, out_format_buf, 0, 0, 0, 0, 0);
            return (C_const_NG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgEnd("OpenFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }
        return (C_const_OK);
    }

    /*                                                                            */
    /*  関数名 ： cmBTcardB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcardB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               一括カード発行主処理                                         */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTcardB_main() {
        int rtn_cd;             /* 関数戻り値                           */
        long loop_cnt;           /* ループカウンタ                       */
        int log_flg;            /* ログ出力フラグ                       */
        String output_buff = null;   /* 出力ファイルバッファ                 */
        StringDto pid_buf = new StringDto();      /* 会員番号                             */
        int cnt_num;            /* 会員番号桁数                         */
        String pid_buf_henkoumae = null;    /* 会員番号 変更前              */

        /* 2023/1/17 MCCM初版 ADD START */
        String wk_pid = null;       /* 会員番号 先頭4桁「9881」固定        */
        /* 2023/1/17 MCCM初版 ADD END */
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTcardB_main *** 一括カード発行主処理");
            /*---------------------------------------------------------------------*/
        }
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 CURSOR TSCD01 [%s]\n", "DECLARE");
            /*------------------------------------------------------------*/
        }
        /* バッチ処理年月日をHOST変数にセット */
        h_shori_ymd = atoi(bat_date_prev);
        h_bat_ymd = atoi(bat_date);

        /* 20160314 モバイルカスタマーポータル対応 サービス種別追加　*/
        /* カーソル定義 */
    /* EXEC SQL DECLARE CARD_TSCD01 CURSOR FOR
        SELECT NVL(TS.処理年月日, 0),
               TS.開始番号,
               NVL(TS.終了番号, 0),
               NVL(TS.有効期限, 0),
               NVL(TS.作業企業コード, 0),
               NVL(TS.作業者ＩＤ, 0),
               NVL(TS.作業年月日, 0),
               NVL(TS.作業時刻, 0),
               TS.サービス種別                                                        /o 2022/10/06 MCCM初版 MODo/
        FROM   TS一括カード発行情報 TS
        WHERE  TS.ステータス = 0
        AND    TS.処理年月日 = :h_shori_ymd; */


        /* カーソル定義エラーは、カーソルオープンで発生する為、エラーチェックなし */

        /* カーソルオープン */
        /* EXEC SQL OPEN CARD_TSCD01; */
        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = sprintf(WRKSQL.arr, "SELECT NVL(TS.処理年月日, 0),TS.開始番号,NVL(TS.終了番号, 0),NVL(TS.有効期限, 0),NVL(TS.作業企業コード, 0),NVL(TS.作業者ＩＤ, 0),NVL(TS.作業年月日, 0),NVL(TS.作業時刻, 0),TS.サービス種別 FROM   TS一括カード発行情報 TS WHERE  TS.ステータス = 0 AND    TS.処理年月日 = '%s'"
                , h_shori_ymd);
        SqlstmDto sqlca = sqlcaManager.get("CARD_TSCD01");
        sqlca.sql = WRKSQL;
//        sqlca.declare();
        sqlca.restAndExecute();
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 CURSOR OPEN sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 " +
                        "CURSOR OPEN ERR sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "CURSOR OPEN ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* データが終了するまでフェッチを繰り返す */
        for (; ; ) {
            /* 初期化 */
//            tscd_buff.kaishi_no = 0;
//            tscd_buff.shuryo_no = 0;
//            tscd_buff.yukokigen = 0;
//            tscd_buff.sagyo_kigyo_cd = 0;
//            tscd_buff.sagyosha_id = 0;
//            tscd_buff.sagyo_ymd = 0;
//            tscd_buff.sagyo_hms = 0;
//            tscd_buff.shori_ymd = 0;
//            /* 20160314 モバイルカスタマーポータル対応 サービス種別の追加 */
//            tscd_buff.service_syubetsu = 0;

            in_rec_cnt = 0;
            ok_rec_cnt = 0;
            ng_rec_cnt = 0;

            /* カーソルフェッチ */
            /* 20160314 モバイルカスタマーポータル対応 サービス種別の追加 */
        /* EXEC SQL FETCH CARD_TSCD01
        INTO :tscd_buff.shori_ymd,
             :tscd_buff.kaishi_no,
             :tscd_buff.shuryo_no,
             :tscd_buff.yukokigen,
             :tscd_buff.sagyo_kigyo_cd,
             :tscd_buff.sagyosha_id,
             :tscd_buff.sagyo_ymd,
             :tscd_buff.sagyo_hms,
             :tscd_buff.service_syubetsu; */

            sqlca.fetch();


            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 FETCH NG sqlcode =[%d]\n", sqlca.sqlcode);
                }
                /*------------------------------------------------------------*/
                APLOG_WT("902", 0, null, sqlca.sqlcode,
                        "FETCH", 0, 0, 0, 0);
                /* カーソルクローズ */
                /* EXEC SQL CLOSE CARD_TSCD01; */

//                sqlca.close();
                sqlcaManager.close("CARD_TSCD01");

                /* 処理を終了する */
                return (C_const_NG);
            }


            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 FETCH NOTFOUND =[%d]\n", sqlca.sqlcode);
                    /*------------------------------------------------------------*/
                }
                break;
            }

            TscdBuff tscd_buff = new TscdBuff();
            sqlca.recData(tscd_buff.shori_ymd,
                    tscd_buff.kaishi_no,
                    tscd_buff.shuryo_no,
                    tscd_buff.yukokigen,
                    tscd_buff.sagyo_kigyo_cd,
                    tscd_buff.sagyosha_id,
                    tscd_buff.sagyo_ymd,
                    tscd_buff.sagyo_hms,
                    tscd_buff.service_syubetsu);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 開始番号       =[%d]\n", tscd_buff.kaishi_no);
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 終了番号       =[%d]\n", tscd_buff.shuryo_no);
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 作業者ＩＤ     =[%f]\n", tscd_buff.sagyosha_id);
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 処理年月日     =[%d]\n", tscd_buff.shori_ymd);
                /*------------------------------------------------------------*/
            }
            /* ログ出力フラグ初期化 */
            log_flg = 0;
            cm_rec_cnt = 0;
            for (loop_cnt = tscd_buff.kaishi_no.longVal(); loop_cnt <= tscd_buff.shuryo_no.longVal(); loop_cnt++) {
                /* 処理対象件数をカウントアップ */
                in_rec_cnt++;
                in_rec_total++;

                /* 初期化 */
//                memset(pid_buf, 0x00, sizeof(pid_buf));
//                memset(pid_buf_henkoumae, 0x00, sizeof(pid_buf_henkoumae));
//
//                /* 2023/1/17 MCCM初版 ADD START */
//                memset(wk_pid, 0x00, sizeof(wk_pid));
                /* 2023/1/17 MCCM初版 ADD END */

                /* 2022/10/06 MCCM初版 DEL START */
                /* 20160720 C-MAS対応 EC会員番号の設定条件を追加 */
//            if ( 2 == tscd_buff.service_syubetsu){
//                /* 会員番号をそのまま設定する */
//                sprintf(pid_buf, "%015ld", loop_cnt);
//            }else {
//                /* 会員番号を生成する */
//                cnt_num = (int)log10((double)loop_cnt) + 1;
//                if(14 == cnt_num){
//                    sprintf(pid_buf, "%014ld", loop_cnt);
//                    rtn_cd = C_GetPidCDV(pid_buf);
//                }else{
//                    /* 会員番号が14ケタ以外の場合 */
//                    /* ファイル名が指定されている場合、エラーログを出力する */
//                    if (fp_out != 0x00) {
//                        memset(output_buff, 0x00, sizeof(output_buff));
//                        sprintf(output_buff, "処理日[%08d] 企業コード[%010d] 会員番号[%15ld]\n",
//                                tscd_buff.shori_ymd, tscd_buff.sagyo_kigyo_cd, loop_cnt);
//
//                        /* 「出力ファイル名」にエラーログを出力する */
//                        fwrite(output_buff, strlen(output_buff), 1, fp_out);
//                        if (ferror(fp_out) != C_const_OK){
//                            sprintf(out_format_buf, "%s/%s", ap_work_dir, arg_o_Value);
//                            APLOG_WT( "903", 0, NULL, out_format_buf , 0, 0, 0, 0, 0);
//                            /* カーソルクローズ */
                /*                            EXEC SQL CLOSE CARD_TSCD01;              */
//                            return (C_const_NG);
//                        }
//                    }
//
//                    /* エラー件数をカウントアップする */
//                    ng_rec_cnt++;
//                    ng_rec_total++;
//
//                    /* エラーファイル出力件数をカウントアップする */
//                    out_err_cnt++;
//                    continue;
//                }
//            }
                /* 2022/10/06 MCCM初版 DEL END */

                /* 2022/10/06 MCCM初版 ADD START */
                cnt_num = (int) log10((double) loop_cnt) + 1;

                /* 2023/1/17 MCCM初版 ADD START */
                wk_pid = sprintf(wk_pid, "%d", loop_cnt);
                /* 2023/1/17 MCCM初版 ADD END */

                if ((tscd_buff.service_syubetsu.intVal() == 1 || tscd_buff.service_syubetsu.intVal() == 3) && cnt_num == 14) {        /* 会員番号を生成する */
                    sprintf(pid_buf, "%014d", loop_cnt);
                    rtn_cd = C_GetPidCDV(pid_buf.arr);
//             }else if( tscd_buff.service_syubetsu == 1 && cnt_num == 16 ){                                       /* 会員番号を生成する(先頭4桁「9881」固定) */

                    /* 2023/05/23 MCCMPH2 MOD START */
                } else if (tscd_buff.service_syubetsu.intVal() == 2 && cnt_num == 14 && strncmp(wk_pid, "200030", 6) == 0) {      /* 会員番号を生成する(デジタル会員) */
                    sprintf(pid_buf, "%014d", loop_cnt);
                    rtn_cd = C_GetPidCDV(pid_buf.arr);
                    /* 2023/05/23 MCCMPH2 MOD END */

                    /* 2023/1/17 MCCM初版 MOD START */
                } else if (tscd_buff.service_syubetsu.intVal() == 1 && cnt_num == 16 && strncmp(wk_pid, "9881", 4) == 0) {                                       /* 会員番号を生成する(先頭4桁「9881」固定) */
                    /* 2023/1/17 MCCM初版 MOD END */

                    pid_buf_henkoumae = sprintf(pid_buf_henkoumae, "%016d", loop_cnt);

                    /* 2023/1/17 MCCM初版 ADD START */
                    if (loop_cnt % 10 != 0) {
                        in_rec_cnt--;
                        continue;
                    }
                    /* 2023/1/17 MCCM初版 ADD END */

                    rtn_cd = cmABendeBService.C_EncDec_Pid(2, "NO", pid_buf_henkoumae, pid_buf);

                } else if (tscd_buff.service_syubetsu.intVal() == 2 && cnt_num <= 15) {                                /* TS一括カード発行情報.開始番号～終了番号 */
                    sprintf(pid_buf, "%d", loop_cnt);
                } else {
                    /* ファイル名が指定されている場合、エラーログを出力する */
                    if (fp_out != null) {
                        memset(output_buff, 0x00, sizeof(output_buff));
                        output_buff = sprintf(output_buff, "処理日[%08d] 企業コード[%010d] 会員番号[%15d]\n",
                                tscd_buff.shori_ymd, tscd_buff.sagyo_kigyo_cd, loop_cnt);

                        /* 「出力ファイル名」にエラーログを出力する */
                        fp_out.write(output_buff);
                        if (ferror(fp_out) != C_const_OK) {
                            out_format_buf = sprintf(out_format_buf, "%s/%s", ap_work_dir, arg_o_Value);
                            APLOG_WT("903", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                            /* カーソルクローズ */
                            /* EXEC SQL CLOSE CARD_TSCD01; */

//                            {
//                                struct sqlexd sqlstm;
//                                sqlstm.sqlvsn = 13;
//                                sqlstm.arrsiz = 9;
//                                sqlstm.sqladtp = &sqladt;
//                                sqlstm.sqltdsp = &sqltds;
//                                sqlstm.iters = (unsigned int  )1;
//                                sqlstm.offset = (unsigned int  )120;
//                                sqlstm.cud = sqlcud0;
//                                sqlstm.sqlest = (unsigned char  *)&sqlca;
//                                sqlstm.sqlety = (unsigned short)4352;
//                                sqlstm.occurs = (unsigned int  )0;
//                                sqlcxt((void **)0, &sqlctx, &sqlstm, &sqlfpn);
//                            }
//                            sqlca.close();
                            sqlcaManager.close("CARD_TSCD01");

                            return (C_const_NG);
                        }
                    }

                    /* エラー件数をカウントアップする */
                    ng_rec_cnt++;
                    ng_rec_total++;

                    /* エラーファイル出力件数をカウントアップする */
                    out_err_cnt++;
                    continue;
                }

                /* 2022/10/06 MCCM初版 ADD END */

                /* HOST変数に会員番号をコピーする */
                h_kaiin_no = memcpy(h_kaiin_no, pid_buf.arr, sizeof(pid_buf));

                /* 2022/11/30 MCCM初版 MOD START */

                /* 20160720 C-MAS対応 企業コード、旧販社コード追加 */
                /* 20160314 モバイルカスタマーポータル対応 企業コード、旧販社コード追加 */
                /* 2022/10/06 MCCM初版 ADD START */
                if (tscd_buff.service_syubetsu.intVal() == 1) {
                    if (cnt_num == 14) {
                        h_kigyo_cd = 1010;
                    } else if (cnt_num == 16) {
                        h_kigyo_cd = 3010;
                    } else {
                        h_kigyo_cd = 0;
                    }
//             h_kyuhansya_cd = "";
                }
                /* 2022/10/06 MCCM初版 ADD END */
                /* 2023/05/23 MCCMPH2 MOD START */
                else if (2 == tscd_buff.service_syubetsu.intVal()) {
                    if (cnt_num == 14 && strncmp(wk_pid, "200030", 6) == 0) {
                        h_kigyo_cd = 3020;
                    } else {
                        h_kigyo_cd = 1020;
                    }
                    /* 2023/05/23 MCCMPH2 MOD END */
//                h_kyuhansya_cd = "10";
                } else if (3 == tscd_buff.service_syubetsu.intVal()) {
                    h_kigyo_cd = 1040;
//                h_kyuhansya_cd = "9";
                } else {
                    h_kigyo_cd = 0;
//                h_kyuhansya_cd = "";
                }

                /*---PS会員番号体系テーブルから旧販社コード取得---*/
                /* 2023/03/09 MCCM初版 ADD START */
                h_kyuhansya_cd = 0;
                /* 2023/03/09 MCCM初版 ADD END */
                /* ＳＱＬを実行する */
            /* EXEC SQL SELECT
                        NVL(旧販社コード,0)
                      INTO :h_kyuhansya_cd
                     FROM
                        PS会員番号体系
                   WHERE
                   会員番号開始 <= TO_NUMBER(:h_kaiin_no)
                   AND 会員番号終了 >= TO_NUMBER(:h_kaiin_no)
                   AND サービス種別 = :tscd_buff.service_syubetsu; */


                WRKSQL = new StringDto();
                WRKSQL.arr = sprintf(WRKSQL.arr, "SELECT NVL(旧販社コード,0) FROM PS会員番号体系 WHERE 会員番号開始 <= TO_NUMBER('%s') AND 会員番号終了 >= TO_NUMBER('%s') AND サービス種別 = '%s'"
                        , h_kyuhansya_cd, h_kaiin_no, tscd_buff.service_syubetsu);
                sqlca.sql = WRKSQL;
                sqlca.restAndExecute();
//                sqlca.reQuery();
//                sqlca.commit();

                /* ＳＱＬを実行結果を判定する */
                if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                    out_format_buf = sprintf(out_format_buf, "会員番号=[%s], サービス種別=[%d]", h_kaiin_no, tscd_buff.service_syubetsu);
                    APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                            "PS会員番号体系", out_format_buf, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }

                C_DbgMsg("会員番号            =[%s]\n", h_kaiin_no);
                C_DbgMsg("旧販社コード        =[%d]\n", h_kyuhansya_cd);
                /* 2022/11/30 MCCM初版 MOD END */

                /* 20160314 モバイルカスタマーポータル対応 企業コード、旧販社コード追加 */
            /* EXEC SQL INSERT INTO MSカード情報
                     (
                        サービス種別,
                        会員番号,
                        顧客番号,
                        カードステータス,
                        理由コード,
                        発行年月日,
                        終了年月日,
                        有効期限,
                        企業コード,
　　　　　　　　　　　　旧販社コード,
                        作業企業コード,
                        作業者ＩＤ,
                        作業年月日,
                        作業時刻,
                        バッチ更新日,
                        最終更新日,
                        最終更新日時,
                        最終更新プログラムＩＤ
                     )
                VALUES
                     (
                        :tscd_buff.service_syubetsu,
                        :h_kaiin_no,
                        0,
                        0,
                        0,
                        0,
                        0,
                        :tscd_buff.yukokigen,
                        :h_kigyo_cd,
                        :h_kyuhansya_cd,
                        :tscd_buff.sagyo_kigyo_cd,
                        :tscd_buff.sagyosha_id,
                        :tscd_buff.shori_ymd,
                        :tscd_buff.sagyo_hms,
                        :h_bat_ymd,
                        :h_bat_ymd,
                        sysdate,
                        :h_saishu_koshin_programid
                     ); */


                WRKSQL = new StringDto();
                WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MSカード情報(サービス種別,会員番号,顧客番号,カードステータス,理由コード,発行年月日,終了年月日,有効期限,企業コード,旧販社コード,作業企業コード,作業者ＩＤ,作業年月日,作業時刻,バッチ更新日,最終更新日,最終更新日時,最終更新プログラムＩＤ)\n" +
                                "VALUES('%s','%s',0,0,0,0,0,'%s','%s','%s','%s','%s','%s','%s','%s','%s',sysdate(),'%s')"
                        , tscd_buff.service_syubetsu, h_kaiin_no, tscd_buff.yukokigen, h_kigyo_cd, h_kyuhansya_cd, tscd_buff.sagyo_kigyo_cd, tscd_buff.sagyosha_id, tscd_buff.shori_ymd, tscd_buff.sagyo_hms, h_bat_ymd, h_bat_ymd, h_saishu_koshin_programid);
                sqlca.sql = WRKSQL;
                sqlca.restAndExecute();
//                sqlca.commit();


                /* INSERTが異常(重複エラー)の場合 */
                if (sqlca.sqlcode == C_const_Ora_DUPL) {
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcardB_main *** MSカード情報 INSERT NG =[%d]\n", sqlca.sqlcode);
                        /*------------------------------------------------------------*/
                    }
                    /* 初回の重複エラーのみ、ＡＰログを出力する（警告） */
                    if (log_flg == 0) {
                        APLOG_WT("712", 0, null, h_kaiin_no,
                                0, 0, 0, 0, 0);
                        log_flg = 1;
                        /* 初期化 */
                        memset(out_format_buf, 0x00, sizeof(out_format_buf));
                    }

                    /* ファイル名が指定されている場合、エラーログを出力する */
                    if (fp_out.fd != 0x00) {
                        memset(output_buff, 0x00, sizeof(output_buff));
                        output_buff = sprintf(output_buff, "処理日[%08d] 企業コード[%010d] 会員番号[%15s]\n",
                                tscd_buff.shori_ymd, tscd_buff.sagyo_kigyo_cd, h_kaiin_no);

                        /* 「出力ファイル名」にエラーログを出力する */
                        fp_out.write(output_buff);
                        if (ferror(fp_out) != C_const_OK) {
                            out_format_buf = sprintf(out_format_buf, "%s/%s", ap_work_dir, arg_o_Value);
                            APLOG_WT("903", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                            /* カーソルクローズ */
                            /* EXEC SQL CLOSE CARD_TSCD01; */
                            sqlcaManager.close("CARD_TSCD01");

                            return (C_const_NG);
                        }
                    }

                    /* エラー件数をカウントアップする */
                    ng_rec_cnt++;
                    ng_rec_total++;

                    /* エラーファイル出力件数をカウントアップする */
                    out_err_cnt++;

                    /* INSERTが異常(重複エラー以外)の場合 */
                } else if (sqlca.sqlcode != C_const_Ora_OK &&
                        sqlca.sqlcode != C_const_Ora_DUPL) {
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcardB_main *** MSカード情報 INSERT ERR" +
                                "NG =[%d]\n", sqlca.sqlcode);
                        /*------------------------------------------------------------*/
                    }
                    APLOG_WT("904", 0, null, "INSERT ERR", sqlca.sqlcode,
                            "MSカード情報", 0, 0, 0);
                    /* カーソルクローズ */
                    /* EXEC SQL CLOSE CARD_TSCD01; */
//                    sqlca.close();

                    sqlcaManager.close("CARD_TSCD01");
                    return (C_const_NG);
                }
                /* INSERTが正常の場合 */
                else {
                    /* 正常処理件数をカウントアップする */
                    ok_rec_cnt++;
                    ok_rec_total++;

                    cm_rec_cnt++;
                    if (cm_rec_cnt >= 3000) {
                        if (DBG_LOG) {
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTcardB_main *** MSカード情報 INSERT 3000件毎COMMIT=[%d]\n", cm_rec_cnt);
                            /*------------------------------------------------------------*/
                        }
                        /* EXEC SQL COMMIT WORK; */
                        sqlca.commit();

                        cm_rec_cnt = 0;
                    }
                }
            }

            /* MSカード情報に全件正常INSERTした場合 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcardB_main in_rec_cnt=[%d]\n", in_rec_cnt);
                C_DbgMsg("*** cmBTcardB_main ok_rec_cnt=[%d]\n", ok_rec_cnt);
                C_DbgMsg("*** cmBTcardB_main ng_rec_cnt=[%d]\n", ng_rec_cnt);
                C_DbgMsg("*** cmBTcardB_main in_rec_total=[%d]\n", in_rec_total);
                C_DbgMsg("*** cmBTcardB_main ok_rec_total=[%d]\n", ok_rec_total);
                C_DbgMsg("*** cmBTcardB_main ng_rec_total=[%d]\n", ng_rec_total);
            }
            if (in_rec_cnt == ok_rec_cnt && in_rec_cnt > 0) {
                h_status = 1;
                /* MSカード情報に全件INSERTエラーした場合、又は１件もINSERTしなかった場合 */
            } else if (in_rec_cnt == ng_rec_cnt || in_rec_cnt == 0) {
                h_status = 99;
                status99_cnt++;
                /* MSカード情報に一部INSERTエラーした場合 */
            } else {
                h_status = 9;
            }

            /* TS一括カード発行情報のレコードを更新(update)する */
        /* EXEC SQL UPDATE TS一括カード発行情報
            SET   ステータス             = :h_status,
                  バッチ更新日           = :h_bat_ymd,
                  最終更新日             = :h_bat_ymd,
                  最終更新日時           = sysdate,
                  最終更新プログラムＩＤ = :h_saishu_koshin_programid
            WHERE 処理年月日     = :tscd_buff.shori_ymd
              AND 開始番号       = :tscd_buff.kaishi_no; */
            WRKSQL = new StringDto();
            WRKSQL.arr = "UPDATE TS一括カード発行情報\n" +
                    "            SET   ステータス             = ?,\n" +
                    "                  バッチ更新日           = ?,\n" +
                    "                  最終更新日             = ?,\n" +
                    "                  最終更新日時           = sysdate(),\n" +
                    "                  最終更新プログラムＩＤ = ?\n" +
                    "            WHERE 処理年月日     = ?\n" +
                    "              AND 開始番号       = ?";
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(h_status, h_bat_ymd, h_bat_ymd, h_saishu_koshin_programid, tscd_buff.shori_ymd, tscd_buff.kaishi_no);


            /* UPDATEが異常（データ未存在以外）の場合 */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcardB_main *** MSカード情報 UPDATE ERR" +
                            "NG =[%d]\n", sqlca.sqlcode);
                    /*------------------------------------------------------------*/
                }
                out_format_buf = sprintf(out_format_buf, "処理年月日=[%d] 開始番号=[%d]",
                        tscd_buff.shori_ymd, tscd_buff.kaishi_no);
                APLOG_WT("904", 0, null, "UPDATE ERR", sqlca.sqlcode,
                        "TS一括カード発行情報", out_format_buf, 0, 0);
                /* カーソルクローズ */
                /* EXEC SQL CLOSE CARD_TSCD01; */

                sqlcaManager.close("CARD_TSCD01");

                return (C_const_NG);
            }


            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcardB_main *** TS一括カード発行情報 " +
                        "DB更新OK =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            /* コミット */
            /* EXEC SQL COMMIT WORK; */
            sqlcaManager.commit();
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** cmBTcardB_main *** 一括カード発行主処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        /* カーソルクローズ */
        /* EXEC SQL CLOSE CARD_TSCD01; */
        sqlcaManager.close("CARD_TSCD01");
        return (C_const_OK);
    }


    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        cmBTfuncB.APLOG_WT(msgid, msgidsbt, dbkbn, param1, param2, param3, param4, param5, param6);
        return (C_const_OK);
    }

    @Override
    public int APLOG_WT_903(Object param1, Object param2, Object param3) {
        return APLOG_WT("903", 0, null, param1, param2, param3, null, null, null);

    }
}
