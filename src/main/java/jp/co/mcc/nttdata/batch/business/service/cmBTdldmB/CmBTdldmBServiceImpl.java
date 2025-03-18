package jp.co.mcc.nttdata.batch.business.service.cmBTdldmB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.*;

/**
 * /*******************************************************************************
 * *   プログラム名   ： DMリスト作成（cmBTdldmB）
 * *
 * *   【処理概要】
 * *     MSサークル顧客情報、MSサークル管理情報からデータを抽出し、
 * *     MSキャンペーン情報、MSキャンペーン顧客情報から、
 * *     顧客ＰＣで使用する「DMリスト」ファイルを作成する。
 * *
 * *   【引数説明】
 * *     -d処理日付          :（任意）処理日付（YYYYMMDD）
 * *     -DEBUG(-debug)      :（任意）デバッグモードでの実行
 * *
 * *   【戻り値】
 * *      10     ： 正常
 * *      99     ： 異常
 * *
 * *------------------------------------------------------------------------------
 * *   稼働環境
 * *      Red Hat Enterprise Linux 5（64bit）
 * *      (文字コード ： UTF8)
 * *------------------------------------------------------------------------------
 * *   改定履歴
 * *      1.00 :   2012/12/03 SSI.越後谷  ： 初版
 * *      1.01 :   2013/01/07 SSI.本田    ： 顧客リストの格納ディレクトリ用環境変数
 * *                                         を変更(CM_PCRENKEI → CM_COUPONLIST)
 * *      3.00 :   2013/06/26 SSI.石阪    ： FC対応
 * *                                         FC企業の出力枠を固定
 * *      4.00 :   2013/08/02 SSI.本田    ： えびなの出力を追加
 * *                                         電話番号の出力を追加
 * *      5.00 :   2014/02/18 SSI.上野    ： カーソル宣言後のSQLCODE判定を削除
 * *      6.00 :   2016/02/03 SSI.上野    ： 処理対象の顧客リスト削除処理を削除
 * *      7.00 :   2016/03/15 SSI.石戸　　： モバイルカスタマーポータル対応
 * *                                         補足説明の企業コードを変更。
 * *                                        「コダマ」→「モバイル」
 * *      8.00 :   2018/03/26 SSI.吉田    ： POS更改
 * *                                         キャンペーン情報を連携された当日に抽出処理を
 * *                                         行うため条件を変更
 * *     40.00 :   2022/09/27 SSI.川内    ： MCCM初版
 * *-------------------------------------------------------------------------------
 * *  $Id:$
 * *-------------------------------------------------------------------------------
 * *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTdldmBServiceImpl extends CmABfuncLServiceImpl implements CmBTdldmBService {
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */


    MM_KOKYAKU_INFO_TBL mmkoinf_t; /* MM顧客情報            バッファ */
    MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL mmkokgy_t; /* MM顧客企業別属性情報  バッファ */
    MM_KOKYAKU_ZOKUSE_INFO_TBL mmkozok_t; /* MM顧客属性情報        バッファ */
    MS_CARD_INFO_TBL mscdinf_t; /* MSカード情報          バッファ */
    MS_CAMPAIGN_INFO_TBL mscpinf_t; /* MSキャンペーン情報    バッファ */
    MS_CAMPAIGN_MANGE_TBL mscpmng_t; /* MSキャンペーン管理情報バッファ */
    MS_RIYU_INFO_TBL msryinf_t; /* MS理由情報            バッファ */

    /* 動的ＳＱＬ作成用 */
    StringDto str_sql = new StringDto(2048); /* 実行用SQL文字列                */

    StringDto h_Program_Name = new StringDto(10); /* バージョンなしプログラム名     */
    int h_bat_yesterday; /* バッチ処理日(前日)             */
    int h_bat_day; /* バッチ処理日(当日)             */
    long h_long_kokyaku_no; /* 処理顧客番号数値               */
    int h_rownum; /* 取得レコード数                 */

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_d = "-d";        /* 処理対象日付                       */
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ                   */
    String DEF_debug = "-debug";        /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "DMリスト作成";        /* APログ用機能名                  */
    /*---------------------------------------------*/
    int C_ITEM_BLOCK = 5;                /* 対象項目数/1 BLOCK                 */
    int DEF_Read_EOF = 9;                /* File read EOF                      */
    /*---------------------------------------------*/
    /* 補足説明の企業コードを変更 コダマ→モバイル                                 */
    int C_IDX_CFH = 0;                 /* 顧客企業コード（CFH）              */
    int C_IDX_EC = 1;                 /* 顧客企業コード（EC）               */
    int C_IDX_KDM = 2;                 /* 顧客企業コード（コダマ）           */
    int C_IDX_MBIL = 2;                 /* 顧客企業コード（モバイル）         */
    int C_IDX_EBN = 3;                 /* 顧客企業コード（えびな）           */
    int C_IDX_FC = 4;                 /* 顧客企業コード（FC）               */


    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    long inp_csv_cnt;
    /**
     * ファイル毎入力処理件数
     **/
    long out_csv_cnt;
    /**
     * ファイル毎出力処理件数
     **/

    int chk_arg_d; /* 引数-dチェック用         */
    StringDto arg_d_Value = new StringDto(256); /* 引数d設定値              */
    StringDto inp_file_dir = new StringDto(256); /* 入力ファイルディレクトリ */
    StringDto out_file_dir = new StringDto(256); /* 出力ファイルディレクトリ */
    StringDto inp_fl_name = new StringDto(512); /* 入力ファイル・パス名     */
    StringDto out_fl_name = new StringDto(512); /* 出力ファイル・パス名     */
    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                 */
    StringDto g_bat_date = new StringDto(9); /* バッチ処理日(当日)       */

    /*----------------------------------------------------------------------------*/
    /*  入力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* キャンペーン対象顧客ファイル構造体                                         */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_inp;        /* 入力ファイル用ポインタ(対象顧客リストファイル) */
    CAMPAIGN_KOKYAKU_DATA inp_kokyaku_t =new CAMPAIGN_KOKYAKU_DATA();

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* DMリスト構造体                                                       */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_out;                          /* 出力ファイルポインタ         */

    DM_LIST_DATA g_sDmlist;


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
        StringDto wk_bat_date = new StringDto(9);                 /* バッチ処理日付（前日）         */
        String env_inpdir;                    /* 入力ファイルDIR                */
        String env_outdir;                    /* 出力ファイルDIR                */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        memset(h_Program_Name, 0x00, sizeof(h_Program_Name));
        strcpy(h_Program_Name, Cg_Program_Name); /* バージョンなしプログラム名 */

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
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
            C_DbgStart("*** main処理 ***");
            /*------------------------------------------------------------*/
        }
        /** 変数初期化 **/
        rtn_cd = C_const_OK;
        chk_arg_d = DEF_OFF;
        memset(arg_d_Value, 0x00, sizeof(arg_d_Value));

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            /*------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;

            } else if (memcmp(arg_Work1, DEF_ARG_d, 2) == 0) {  /* -dの場合        */
                rtn_cd = cmBTdldmB_Chk_Arg(arg_Work1);   /* パラメータCHK      */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_d_Value, arg_Work1.substring(2));
                } else {
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else {                        /* 定義外パラメータ                   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /* 必須パラメータ未指定チェック なし */

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  入力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", "CM_COUPONLIST");
            /*-------------------------------------------------------------*/
        }
        env_inpdir = getenv("CM_COUPONLIST");
        if (StringUtils.isEmpty(env_inpdir)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_COUPONLIST]%s\n", "null");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_COUPONLIST)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        memset(inp_file_dir, 0x00, sizeof(inp_file_dir));
        strcpy(inp_file_dir, env_inpdir);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）[%s]\n", inp_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "CM_COUPONLIST");
            /*-------------------------------------------------------------*/
        }
        env_outdir = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_outdir)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_COUPONLIST]%s\n", "null");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_COUPONLIST)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_outdir);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
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
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /* バッチ処理日付を取得（当日）                  */
        /*-----------------------------------------------*/
        rtn_cd = C_GetBatDate(0, g_bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        h_bat_day = atoi(g_bat_date); /* バッチ処理日(前日) */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理対象日付:バッチ処理日_当日 =[%s]\n", g_bat_date);
            /*------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /* 処理日付を設定                                */
        /*-----------------------------------------------*/
        if (chk_arg_d == DEF_OFF) {  /* IF バッチ処理日付を取得（前日） */

            /*-----------------------------------------------*/
            /* バッチ処理日付を取得（前日）                  */
            /*-----------------------------------------------*/
            rtn_cd = C_GetBatDate(-1, wk_bat_date, rtn_status);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_GetBatDate",
                        rtn_cd, rtn_status, 0, 0, 0);
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
            /* バッチ処理日付(ホスト変数) */
            h_bat_yesterday = atoi(wk_bat_date); /* バッチ処理日(前日) */
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 処理対象日付:バッチ処理日_前日 =[%s]\n", wk_bat_date);
                /*------------------------------------------------------------*/
            }
        } else {
            /* 引数d設定値(ホスト変数) */
            h_bat_yesterday = atoi(arg_d_Value); /* 引数d設定値        */
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 処理対象日付:引数d設定値 =[%s]\n", arg_d_Value);
                /*------------------------------------------------------------*/
            }
        }  /* ENDIF バッチ処理日付を取得（前日） */

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /* DMリスト作成主処理 */
        rtn_cd = cmBTdldmB_Emailmain();
        if (rtn_cd == C_const_APNG) {
            return exit(C_const_APNG);
        }
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTdldmB_EmailMake NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "DMリスト作成主処理に失敗しました", 0, 0, 0, 0, 0);

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
    /*  関数名 ： cmBTdldmB_Emailmain                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_Emailmain()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      DMリスト作成主処理                                                    */
    /*      「DMリスト」対象データを抽出し、作成する。                            */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_Emailmain() {
        int rtn_cd;                         /* 関数戻り値                     */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_Emailmain処理");
            /*------------------------------------------------------------*/
        }
        /*----------------------*/
        /* DMリスト作成   */
        /*----------------------*/
        /* 初期化 */

        /* ホスト変数に取得情報を設定 */
        h_rownum = C_ITEM_BLOCK;

        /* カーソル宣言 */
        sqlca.sql.arr = "SELECT DISTINCT" +
                "        B.キャンペーンＩＤ," +
                "                B.クーポンコード," +
                "                A.予約日" +
                "        FROM" +
                "        MSキャンペーン情報 A," +
                "        MSキャンペーン管理情報 B" +
                "        WHERE" +
                "                (B.キャンペーン種別 = 1   AND                " +    /* 1：ＤＭ   */
                "                        A.連動済みフラグ   = 0   AND                 " +   /* 0：未連動 */
                "                        B.最終更新日       >= ?)        " + /* 2018.03.26 POS更改対応「=」→「>=」 */
                "        AND (A.キャンペーンＩＤ = B.キャンペーンＩＤ)" +
                "        GROUP BY" +
                "        B.キャンペーンＩＤ," +
                "                B.クーポンコード," +
                "                A.予約日" +
                "        ORDER BY" +
                "        B.キャンペーンＩＤ";
        sqlca.restAndExecute(h_bat_yesterday);
//        EXEC SQL DECLARE cur_canpaing CURSOR FOR
//        SELECT DISTINCT
//        B.キャンペーンＩＤ,
//                B.クーポンコード,
//                A.予約日
//        FROM
//        MSキャンペーン情報@CMSD A,
//        MSキャンペーン管理情報@CMSD B
//        WHERE
//                (B.キャンペーン種別 = 1   AND                    /* 1：ＤＭ   */
//                        A.連動済みフラグ   = 0   AND                    /* 0：未連動 */
//                        B.最終更新日       >= :h_bat_yesterday)         /* 2018.03.26 POS更改対応「=」→「>=」 */
//        AND (A.キャンペーンＩＤ = B.キャンペーンＩＤ)
//        GROUP BY
//        B.キャンペーンＩＤ,
//                B.クーポンコード,
//                A.予約日
//        ORDER BY
//        B.キャンペーンＩＤ;

        /* カーソルオープン */
//        EXEC SQL OPEN cur_canpaing;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "CURSOR OPEN ERR", sqlca.sqlcode,
                    "MSキャンペーン情報", 0, 0, 0);
            return C_const_NG;
        }

        /* 初期化 */
        while (true) { /* while DMリスト対象データ */
            /* ホスト変数・構造体を初期化 */
            g_sDmlist =new DM_LIST_DATA();
            mscpinf_t=new MS_CAMPAIGN_INFO_TBL();
            mscpmng_t=new MS_CAMPAIGN_MANGE_TBL();
            memset(mscpinf_t, 0x00, sizeof(mscpinf_t)); /* MSキャンペーン情報    バッファ */
            memset(mscpmng_t, 0x00, sizeof(mscpmng_t)); /* MSキャンペーン管理情報バッファ */
            memset(g_sDmlist, 0x00, sizeof(g_sDmlist)); /* DMリスト情報取得ＴＢＬ   */

            g_sDmlist.select_ymd.arr = 0;
            g_sDmlist.yoyaku_ymd.arr = 0;
            g_sDmlist.coupon__cd.arr = 0;


            /* カーソルフェッチ */
            sqlca.fetchInto(mscpmng_t.campaign_id,
                    mscpmng_t.coupon__cd,
                    mscpinf_t.yoyaku_ymd);
//            EXEC SQL FETCH cur_canpaing
//            INTO          :mscpmng_t.campaign_id,
//                          :mscpmng_t.coupon__cd ,
//                          :mscpinf_t.yoyaku_ymd ;

            /* データ無しの場合、ブレーク */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
                /* ＥＯＦ以外のエラーは異常終了 */
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "FETCH ERR", sqlca.sqlcode,
                        "MSキャンペーン情報", 0, 0, 0);
//                EXEC SQL CLOSE cur_canpaing;
                sqlca.curse_close();
                return C_const_NG;
            }
            /* 取得情報を出力ＷＯＲＫに退避 */
            memset(g_sDmlist.campaign_id, 0x00, sizeof(g_sDmlist.campaign_id));
            strcpy(g_sDmlist.campaign_id, mscpmng_t.campaign_id);
            g_sDmlist.coupon__cd = mscpmng_t.coupon__cd;
            g_sDmlist.yoyaku_ymd = mscpinf_t.yoyaku_ymd;

            /*-------------------------------------*/
            /*  出力ファイルOPEN                   */
            /*-------------------------------------*/
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_Emailmain *** DMリスト %s\n", "オープン");
                /*------------------------------------------------------------*/
            }
            rtn_cd = cmBTdldmB_OpenOutF();
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgMsg("*** cmBTdldmB_Emailmain *** DMリストオープン NG rtn=[%d]\n", rtn_cd);
                    /*---------------------------------------------*/
                }
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
                return C_const_APNG;
            }

            /*------------------------------*/
            /* キャンペーン対象顧客ファイル */
            /*------------------------------*/
            /* DMリスト作成処理 */
            rtn_cd = cmBTdldmB_EmailMake();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_Emailmain *** cmBTdldmB_EmailMake ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                cmBTdldmB_CloseOutF();
                APLOG_WT("903", 0, null, "cmBTdldmB_EmailMake", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /*------------------------------*/
            /* 対象キャンペーン連動済み設定 */
            /*------------------------------*/
            /* キャンペーン情報更新処理 */
            rtn_cd = cmBTdldmB_MsCnpnInf();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_Emailmain *** cmBTdldmB_MsCnpnInf ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                cmBTdldmB_CloseOutF();
                APLOG_WT("903", 0, null, "cmBTdldmB_MsCnpnInf", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /*-------------------------------------*/
            /*  出力ファイルCLOSE                  */
            /*-------------------------------------*/
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_Emailmain *** DMリスト %s\n", "CLOSE");
                /*------------------------------------------------------------*/
            }
            rtn_cd = cmBTdldmB_CloseOutF();

            /* 出力件数をAPログに出力 */
            APLOG_WT("104", 0, null, inp_fl_name, inp_csv_cnt, 0, 0, 0, 0);
            APLOG_WT("101", 0, null, out_fl_name, out_csv_cnt, 0, 0, 0, 0);

            /*-------------------------------------*/
            /*  キャンペーン対象顧客ファイル削除   */
            /*-------------------------------------*/
            /* 2016/02/03 ファイル削除処理削除 */
            /* 別バッチ処理（クーポン顧客登録）で顧客リストを使用するため */
            /* 当処理では顧客リストを削除しない。                         */
            /* cmBTdldmB_RemoveFile(); */

        } /* next DMリスト対象データ */

        /* カーソルクローズ */
//        EXEC SQL CLOSE cur_canpaing;
        sqlca.curse_close();
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_Emailmain処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_Emailmain Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_EmailMake                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_MsCnpnInf()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      DMリスト作成処理                                                      */
    /*      キャンペーン対象顧客ファイルを読込、                                  */
    /*      顧客ＰＣで使用する「DMリスト」ファイルを作成する。                    */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_EmailMake() {
        int rtn_cd; /* 関数戻り値                   */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_EmailMake処理");
            /*------------------------------------------------------------*/
        }
        /*------------------------------*/
        /* キャンペーン対象顧客ファイル */
        /*------------------------------*/
        /* 初期化 */
        inp_csv_cnt = 0; /* DMリスト_処理対象件数 */
        out_csv_cnt = 0; /* DMリスト_正常処理件数 */

        /*----------------------*/
        /* 入力ファイルオープン */
        /*----------------------*/
        /* 入力ファイルオープン処理 */
        rtn_cd = cmBTdldmB_OpenInpF();
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_OpenInpF ret=%d\n", rtn_cd);
            /*------------------------------------------------------------*/
        }
        if (rtn_cd == DEF_Read_EOF) {
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "キャンペーン対象顧客ファイルオープン異常（キャンペーンID=%s)", mscpmng_t.campaign_id);
            APLOG_WT("700", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return (DEF_Read_EOF);
        }

        while (true) { /* while キャンペーン対象顧客ファイル */
            /*------------------------*/
            /* ファイル読込           */
            /*------------------------*/
            rtn_cd = cmBTdldmB_ReadInpF();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_ReadInpF ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                APLOG_WT("903", 0, null, "cmBTdldmB_ReadInpF", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            } else if (rtn_cd == DEF_Read_EOF) {
                break;
            } /* ループ処理終了 */

            inp_csv_cnt++; /* DMリスト_処理対象件数++ */

            /*------------------------*/
            /* 顧客情報取得           */
            /*------------------------*/
            rtn_cd = cmBTdldmB_MmKoInf();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_MmKoInf ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                APLOG_WT("903", 0, null, "cmBTdldmB_MmKoInf", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /*------------------------*/
            /* 顧客企業別属性情報取得 */
            /*------------------------*/
            rtn_cd = cmBTdldmB_MmKoKigyo();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_MmKoKigyo ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                APLOG_WT("903", 0, null, "cmBTdldmB_MmKoKigyo", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /*------------------------*/
            /* カード情報取得         */
            /*------------------------*/
            rtn_cd = cmBTdldmB_MsCardInf();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_MsCardInf ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                APLOG_WT("903", 0, null, "cmBTdldmB_MsCardInf", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /*------------------------*/
            /* DMリスト出力     */
            /*------------------------*/
            rtn_cd = cmBTdldmB_WriteOutF();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_EmailMake *** cmBTdldmB_WriteOutF ret=%d\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            if (rtn_cd == C_const_NG) {
                APLOG_WT("903", 0, null, "cmBTdldmB_WriteOutF", rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            out_csv_cnt++; /* DMリスト_正常処理件数++ */

        } /* next キャンペーン対象顧客ファイル */

        /*------------------------*/
        /* 入力ファイルクローズ   */
        /*------------------------*/
        rtn_cd = cmBTdldmB_CloseInpF();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_EmailMake処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_EmailMake Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_MmKoInf                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTdldmB_MmKoInf()                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      顧客情報取得処理                                                      */
    /*      MM顧客情報からDMリストファイル情報を取得する。                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_MmKoInf() {
        int rtn_cd; /* 関数戻り値                    */
        IntegerDto rtn_status = new IntegerDto(); /* 関数結果ステータス            */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    char    wk_buff[512]                    ;*/ /* ファイル出力内容編集バッファ  */
        StringDto wk_buff = new StringDto(601); /* ファイル出力内容編集バッファ  */
        /*    char    wk_buff2[512]                   ;*/ /* 編集バッファ                  */
        StringDto wk_buff2 = new StringDto(1446); /* 編集バッファ                  */
        /* 2022/09/27 MCCM初版 MOD END */
        /* 2022/09/27 MCCM初版 ADD START */
        StringDto wk_buff3 = new StringDto(121); /* 編集バッファ                  */
        StringDto wk_buff4 = new StringDto(723); /* 編集バッファ                  */
        /* 2022/09/27 MCCM初版 ADD END */
        StringDto sjis_str_kokyaku_mesho = new StringDto(300); /* SJIS変換後文字列(顧客名称)    */
        IntegerDto sjis_len_kokyaku_mesho = new IntegerDto(); /* SJIS変換後文字列のレングス    */
        StringDto sjis_str_kokyaku_kana_mesho = new StringDto(200); /* SJIS変換後文字列(顧客カナ名称)*/
        IntegerDto sjis_len_kokyaku_kana_mesho = new IntegerDto();  /* SJIS変換後文字列のレングス    */
        StringDto sjis_str_jusho_1 = new StringDto(200); /* SJIS変換後文字列(住所１)      */
        IntegerDto sjis_len_jusho_1 = new IntegerDto();  /* SJIS変換後文字列のレングス    */
        StringDto sjis_str_jusho_2 = new StringDto(200); /* SJIS変換後文字列(住所２)      */
        IntegerDto sjis_len_jusho_2 = new IntegerDto();  /* SJIS変換後文字列のレングス    */
        /* 2022/09/27 MCCM初版 DEL START */
        /*    char    sjis_str_jusho_3=[200]           ;*/ /* SJIS変換後文字列(住所３)      */
        /*    int     sjis_len_jusho_3                ;*/ /* SJIS変換後文字列のレングス    */
        /* 2022/09/27 MCCM初版 DEL END */
        /* 2022/09/27 MCCM初版 ADD START */
        StringDto prefectures = new StringDto(40 * 3 + 1);              /* 都道府県名                    */
        /* 2022/09/27 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_MmKoInf処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        mmkoinf_t=new MM_KOKYAKU_INFO_TBL();
        mmkozok_t=new MM_KOKYAKU_ZOKUSE_INFO_TBL();
        memset(mmkoinf_t, 0x00, sizeof(mmkoinf_t));
        memset(mmkozok_t, 0x00, sizeof(mmkozok_t));

        sqlca.sql.arr = "SELECT " +
                "                NVL(RPAD(A.顧客名字,LENGTH(A.顧客名字)), ' ') " +
                "                , NVL(RPAD(A.顧客名前,LENGTH(A.顧客名前)), ' ')" +
                "                , NVL(RPAD(A.カナ顧客名字,LENGTH(A.カナ顧客名字)), ' ')" +
                "                , NVL(RPAD(A.カナ顧客名前,LENGTH(A.カナ顧客名前)), ' ')" +
                /* 2022/09/27 MCCM初版 MOD END */
                "                , NVL(RPAD(B.郵便番号,LENGTH(B.郵便番号)), ' ')" +
                /* 2022/09/27 MCCM初版 MOD START */
                /*                    ,NVL(B.住所１,' ')*/
                /*                    ,NVL(B.住所２,' ')*/
                /*                    ,NVL(B.住所３,' ')*/
                "                , NVL(RPAD(B.住所,LENGTH(B.住所)), ' ')" +
                /* 2022/09/27 MCCM初版 MOD END */
                "                , NVL(RPAD(B.郵便番号コード,LENGTH(B.郵便番号コード)), ' ')" +
                "                , NVL(RPAD(B.電話番号１,LENGTH(B.電話番号１)), ' ')" +
                "                , NVL(RPAD(B.電話番号２,LENGTH(B.電話番号２)), ' ')" +
                "                , NVL(RPAD(B.電話番号３,LENGTH(B.電話番号３)), ' ')" +
                "                , NVL(RPAD(B.電話番号４,LENGTH(B.電話番号４)), ' ')" +
                /* 2022/09/27 MCCM初版 ADD START */
                "                , NVL(B.都道府県コード, 0) " +
                "                 FROM MM顧客情報 A," +
                "                MM顧客属性情報 B" +
                "        WHERE A.顧客番号 = ?     " +/* 対象顧客リストより取得 */
                "        AND A.顧客番号 = B.顧客番号";
        sqlca.restAndExecute(h_long_kokyaku_no);
        /* ＳＱＬ文 */
//        EXEC SQL SELECT /*+
//                        INDEX(A PKMMCTCSMS00)
//                        INDEX(B PKMMCTCSEL00)
//                    */
//        /* 2022/09/27 MCCM初版 MOD START */
//        /*                     NVL(A.顧客名称,' ')*/
//        /*                    ,NVL(A.顧客カナ名称,' ')*/
//        NVL(A.顧客名字, ' ')
//                , NVL(A.顧客名前, ' ')
//                , NVL(A.カナ顧客名字, ' ')
//                , NVL(A.カナ顧客名前, ' ')
//                /* 2022/09/27 MCCM初版 MOD END */
//                , NVL(B.郵便番号, ' ')
//                /* 2022/09/27 MCCM初版 MOD START */
//                /*                    ,NVL(B.住所１,' ')*/
//                /*                    ,NVL(B.住所２,' ')*/
//                /*                    ,NVL(B.住所３,' ')*/
//                , NVL(B.住所, ' ')
//                /* 2022/09/27 MCCM初版 MOD END */
//                , NVL(B.郵便番号コード, ' ')
//                , NVL(B.電話番号１, ' ')
//                , NVL(B.電話番号２, ' ')
//                , NVL(B.電話番号３, ' ')
//                , NVL(B.電話番号４, ' ')
//                /* 2022/09/27 MCCM初版 ADD START */
//                , NVL(B.都道府県コード, 0)
        /* 2022/09/27 MCCM初版 ADD END */
        /* 2022/09/27 MCCM初版 MOD START */
        /*             INTO   :mmkoinf_t.kokyaku_mesho     ,*/
        /*                    :mmkoinf_t.kokyaku_kana_mesho,*/
//        INTO:
//        mmkoinf_t.kokyaku_myoji,
//                    :mmkoinf_t.kokyaku_name,
//                    :mmkoinf_t.kana_kokyaku_myoji,
//                    :mmkoinf_t.kana_kokyaku_name,
//        /* 2022/09/27 MCCM初版 MOD END */
//                    :mmkozok_t.yubin_no          ,
//        /* 2022/09/27 MCCM初版 MOD START */
//        /*                    :mmkozok_t.jusho_1           ,*/
//        /*                    :mmkozok_t.jusho_2           ,*/
//        /*                    :mmkozok_t.jusho_3           ,*/
//                    :mmkozok_t.address           ,
//        /* 2022/09/27 MCCM初版 MOD END */
//                    :mmkozok_t.yubin_no_cd       ,
//                    :mmkozok_t.denwa_no_1        ,
//                    :mmkozok_t.denwa_no_2        ,
//                    :mmkozok_t.denwa_no_3        ,
//        /* 2022/09/27 MCCM初版 MOD START */
//        /*                    :mmkozok_t.denwa_no_4*/
//                    :mmkozok_t.denwa_no_4        ,
//        /* 2022/09/27 MCCM初版 MOD END */
//        /* 2022/09/27 MCCM初版 ADD START */
//                    :mmkozok_t.todofuken_cd
//        /* 2022/09/27 MCCM初版 ADD END */
////        FROM MM顧客情報 A,
////                MM顧客属性情報 B
////        WHERE A.顧客番号 = :h_long_kokyaku_no      /* 対象顧客リストより取得 */
////        AND A.顧客番号 = B.顧客番号;

        sqlca.fetchInto(mmkoinf_t.kokyaku_myoji,
                mmkoinf_t.kokyaku_name,
                mmkoinf_t.kana_kokyaku_myoji,
                mmkoinf_t.kana_kokyaku_name,
                /* 2022/09/27 MCCM初版 MOD END */
                mmkozok_t.yubin_no,
                /* 2022/09/27 MCCM初版 MOD START */
                /*                    :mmkozok_t.jusho_1           ,*/
                /*                    :mmkozok_t.jusho_2           ,*/
                /*                    :mmkozok_t.jusho_3           ,*/
                mmkozok_t.address,
                /* 2022/09/27 MCCM初版 MOD END */
                mmkozok_t.yubin_no_cd,
                mmkozok_t.denwa_no_1,
                mmkozok_t.denwa_no_2,
                mmkozok_t.denwa_no_3,
                /* 2022/09/27 MCCM初版 MOD START */
                /*                    :mmkozok_t.denwa_no_4*/
                mmkozok_t.denwa_no_4,
                /* 2022/09/27 MCCM初版 MOD END */
                /* 2022/09/27 MCCM初版 ADD START */
                mmkozok_t.todofuken_cd);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** MM顧客情報 SELECT rtn=[%d]\n", sqlca.sqlcode);
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** MM顧客情報 SELECT key=[%d]\n", h_long_kokyaku_no);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外のエラーの場合処理終了 */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_MmKoInf *** MM顧客情報 SELECT ERR=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "[%d]", h_long_kokyaku_no);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MM顧客情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        /*----------------------*/
        /* 顧客名称             */
        /*----------------------*/
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        /* 2022/09/27 MCCM初版 ADD START */
        memset(wk_buff3, 0x00, sizeof(wk_buff3));
        memset(wk_buff4, 0x00, sizeof(wk_buff4));
        /* 2022/09/27 MCCM初版 ADD END */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    memcpy(wk_buff , mmkoinf_t.kokyaku_mesho,*/
        /*              strlen(mmkoinf_t.kokyaku_mesho));*/
        memcpy(wk_buff, mmkoinf_t.kokyaku_myoji,
                strlen(mmkoinf_t.kokyaku_myoji));
        memcpy(wk_buff3, mmkoinf_t.kokyaku_name,
                strlen(mmkoinf_t.kokyaku_name));
        /* 2022/09/27 MCCM初版 MOD END */
        BT_Rtrim(wk_buff, strlen(wk_buff));
        /* 2022/09/27 MCCM初版 ADD START */
        BT_Rtrim(wk_buff3, strlen(wk_buff3));
        sprintf(wk_buff4, "%s%s%s", wk_buff, " ", wk_buff3);
        /* 2022/09/27 MCCM初版 ADD END */

        /* 全角に変換する */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    rtn_cd = C_ConvHalf2Full(wk_buff, wk_buff2)       ;*/
        rtn_cd = C_ConvHalf2Full(wk_buff4, wk_buff2);
        /* 2022/09/27 MCCM初版 MOD END */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full=%d\n", rtn_cd);
            /* 2022/09/27 MCCM初版 MOD START */
            /*    C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full from [%s]\n", wk_buff);*/
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full from [%s]\n", wk_buff4);
            /* 2022/09/27 MCCM初版 MOD END */
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full to   [%s]\n", wk_buff2);
            /*-------------------------------------------------------------*/
        }
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        memset(sjis_str_kokyaku_mesho, 0x00, sizeof(sjis_str_kokyaku_mesho));

        /* UTF8 → SJIS変換処理 */
        rtn_cd = C_ConvUT2SJ(wk_buff2,
                strlen(wk_buff2),
                sjis_str_kokyaku_mesho, sjis_len_kokyaku_mesho);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(g_sDmlist.kokyaku_mesho, 0x00, sizeof(g_sDmlist.kokyaku_mesho));
        memcpy(g_sDmlist.kokyaku_mesho, sjis_str_kokyaku_mesho.strVal(), sjis_len_kokyaku_mesho.arr);
//TODO削除
        C_DbgMsg("*** cmBTdldmB_MmKoInf *** g_sDmlist.kokyaku_mesho    [%s]\n", g_sDmlist.kokyaku_mesho);

        /*----------------------*/
        /* 顧客カナ名称         */
        /*----------------------*/
        memset(wk_buff, 0x00, sizeof(wk_buff));
        /* 2022/09/27 MCCM初版 ADD START */
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        memset(wk_buff3, 0x00, sizeof(wk_buff3));
        /* 2022/09/27 MCCM初版 ADD END */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    memcpy(wk_buff, mmkoinf_t.kokyaku_kana_mesho,*/
        /*             strlen(mmkoinf_t.kokyaku_kana_mesho));*/
        memcpy(wk_buff, mmkoinf_t.kana_kokyaku_myoji,
                strlen(mmkoinf_t.kana_kokyaku_myoji));
        memcpy(wk_buff3, mmkoinf_t.kana_kokyaku_name,
                strlen(mmkoinf_t.kana_kokyaku_name));
        /* 2022/09/27 MCCM初版 MOD END */
        BT_Rtrim(wk_buff, strlen(wk_buff));
        /* 2022/09/27 MCCM初版 ADD START */
        BT_Rtrim(wk_buff3, strlen(wk_buff3));
        sprintf(wk_buff2, "%s%s%s", wk_buff, "　", wk_buff3);
        /* 2022/09/27 MCCM初版 ADD END */
        memset(sjis_str_kokyaku_kana_mesho, 0x00,
                sizeof(sjis_str_kokyaku_kana_mesho));

        /* UTF8 → SJIS変換処理 */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    rtn_cd = C_ConvUT2SJ(wk_buff, strlen(wk_buff)     ,*/
        rtn_cd = C_ConvUT2SJ(wk_buff2, strlen(wk_buff2),
                /* 2022/09/27 MCCM初版 MOD END */
                sjis_str_kokyaku_kana_mesho,
                sjis_len_kokyaku_kana_mesho);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ",
                    rtn_cd, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(g_sDmlist.kokyaku_kana_mesho, 0x00, sizeof(g_sDmlist.kokyaku_kana_mesho));
        memcpy(g_sDmlist.kokyaku_kana_mesho, sjis_str_kokyaku_kana_mesho.arr, sjis_len_kokyaku_kana_mesho.arr);



        /* 2022/09/27 MCCM初版 ADD START */
        /* 都道府県名 */
        memset(prefectures, 0x00, sizeof(prefectures));
        if (mmkozok_t.todofuken_cd.intVal() != 0) {
            rtn_cd = C_GetPrefectures(mmkozok_t.todofuken_cd.intVal(), prefectures, rtn_status);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTdldmB_MmKoInf *** 都道府県名 %s\n", "");
                    /*------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_GetPrefectures",
                        rtn_cd, rtn_status,
                        0, 0, 0);

                return C_const_NG;
            }
        }

        memset(sjis_str_jusho_1, 0x00, sizeof(sjis_str_jusho_1));

        /* UTF8 → SJIS変換処理 */
        /* 2022/09/27 MCCM初版 MOD START */
        /*    rtn_cd = C_ConvUT2SJ(wk_buff2, */
        /*                         strlen(wk_buff2),*/
        /*                         sjis_str_jusho_1, &sjis_len_jusho_1);*/
        rtn_cd = C_ConvUT2SJ(prefectures,
                strlen(prefectures),
                sjis_str_jusho_1, sjis_len_jusho_1);
        /* 2022/09/27 MCCM初版 MOD END */

        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(g_sDmlist.jusho_1, 0x00, sizeof(g_sDmlist.jusho_1));
        memcpy(g_sDmlist.jusho_1, sjis_str_jusho_1, sjis_len_jusho_1.arr);

        /*--------------------*/
        /* 住所２             */
        /*--------------------*/
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        /* 2022/09/27 MCCM初版 MOD START */
        /*    memcpy(wk_buff , mmkozok_t.jusho_2,*/
        /*              strlen(mmkozok_t.jusho_2));*/
        memcpy(wk_buff, mmkozok_t.address,
                strlen(mmkozok_t.address));
        /* 2022/09/27 MCCM初版 MOD END */
        BT_Rtrim(wk_buff, strlen(wk_buff));

        /* 全角に変換する */
        rtn_cd = C_ConvHalf2Full(wk_buff, wk_buff2);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full=%d\n", rtn_cd);
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full from [%s]\n", wk_buff);
            C_DbgMsg("*** cmBTdldmB_MmKoInf *** C_ConvHalf2Full to   [%s]\n", wk_buff2);
            /*-------------------------------------------------------------*/
        }
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        memset(sjis_str_jusho_2, 0x00, sizeof(sjis_str_jusho_2));

        /* UTF8 → SJIS変換処理 */
        rtn_cd = C_ConvUT2SJ(wk_buff2,
                strlen(wk_buff2),
                sjis_str_jusho_2, sjis_len_jusho_2);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("cmBTdldmB_MmKoInf", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(g_sDmlist.jusho_2, 0x00, sizeof(g_sDmlist.jusho_2));
        memcpy(g_sDmlist.jusho_2, sjis_str_jusho_2, sjis_len_jusho_2);



        /*-------------------------*/
        /* 郵便番号&郵便番号コード */
        /*-------------------------*/
        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.yubin_no, strlen(mmkozok_t.yubin_no));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.yubin_no, 0x00, sizeof(g_sDmlist.yubin_no));
        strcpy(g_sDmlist.yubin_no, wk_buff);

        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.yubin_no_cd, strlen(mmkozok_t.yubin_no_cd));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.yubin_no_cd, 0x00, sizeof(g_sDmlist.yubin_no_cd));
        strcpy(g_sDmlist.yubin_no_cd, wk_buff);

        /*-------------------------*/
        /* 電話番号１              */
        /*-------------------------*/
        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.denwa_no_1, strlen(mmkozok_t.denwa_no_1));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.tel_no1, 0x00, sizeof(g_sDmlist.tel_no1));
        strcpy(g_sDmlist.tel_no1, wk_buff);

        /*-------------------------*/
        /* 電話番号２              */
        /*-------------------------*/
        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.denwa_no_2, strlen(mmkozok_t.denwa_no_2));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.tel_no2, 0x00, sizeof(g_sDmlist.tel_no2));
        strcpy(g_sDmlist.tel_no2, wk_buff);

        /*-------------------------*/
        /* 電話番号３              */
        /*-------------------------*/
        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.denwa_no_3, strlen(mmkozok_t.denwa_no_3));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.tel_no3, 0x00, sizeof(g_sDmlist.tel_no3));
        strcpy(g_sDmlist.tel_no3, wk_buff);

        /*-------------------------*/
        /* 電話番号４              */
        /*-------------------------*/
        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memcpy(wk_buff, mmkozok_t.denwa_no_4, strlen(mmkozok_t.denwa_no_4));
        BT_Rtrim(wk_buff, strlen(wk_buff));
        memset(g_sDmlist.tel_no4, 0x00, sizeof(g_sDmlist.tel_no4));
        strcpy(g_sDmlist.tel_no4, wk_buff);

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_MmKoInf", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*--- cmBTdldmB_MmKoInf Bottom -----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_MmKoKigyo                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTdldmB_MmKoKigyo()                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      顧客企業別属性情報取得処理                                            */
    /*      MM顧客企業別属性情報からDMリストファイル情報を取得する。              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_MmKoKigyo() {
        int wi_loop, i; /* ループカウンタ             */
        int rtn_cd; /* 関数戻り値                   */
        StringDto wk_buff = new StringDto(512); /* ファイル出力内容編集バッファ */
        StringDto wk_buff2 = new StringDto(512); /* 編集バッファ                 */
        StringDto sjis_str_riyu_setumei = new StringDto(200); /* SJIS変換後文字列(理由説明)   */
        IntegerDto sjis_len_riyu_setumei = new IntegerDto(); /* SJIS変換後文字列のレングス   */
        int wk_psw; /* 処理ＳＷ＆ＩＤＸ             */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_MmKoKigyo処理");
            /*------------------------------------------------------------*/
        }

        SqlstmDto sqlca = sqlcaManager.get("CUR_MMKG");
        sqlca.sql.arr = "SELECT T.企業コード" +
                "                , NVL(RPAD(T.理由説明, LENGTH(T.理由説明)), ' ')" +
                "                , T.退会年月日" +
                "        FROM(SELECT " + /*+ INDEX(A PKMMCTCSKE00) */
                "                A.企業コード" +
                "                , B.理由説明" +
                "                , A.退会年月日" +
                "                FROM MM顧客企業別属性情報 A" +
                "                LEFT JOIN MS理由情報 B ON A.ＤＭ止め区分 = B.理由コード" +
                "                WHERE A.顧客番号 = ?" +  /* 対象顧客リストより取得 */
                "              )T" +
                "        ORDER BY T.企業コード";
        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_MMKG CURSOR FOR
//        SELECT T.企業コード
//                , NVL(T.理由説明, ' ')
//                , T.退会年月日
//        FROM(SELECT /*+ INDEX(A PKMMCTCSKE00) */
//                A.企業コード
//                , B.理由説明
//                , A.退会年月日
//                FROM MM顧客企業別属性情報 A
//                , MS理由情報 B
//                WHERE A.顧客番号 = :h_long_kokyaku_no  /* 対象顧客リストより取得 */
//        AND A.ＤＭ止め区分 = B.理由コード(+)
//              )T
//        ORDER BY T.企業コード;
        sqlca.declare();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MmKoKigyo *** MM顧客企業別属性情報 SELECT rtn=[%d]\n", sqlca.sqlcode);
            C_DbgMsg("*** cmBTdldmB_MmKoKigyo *** MM顧客企業別属性情報 SELECT key=[%d]\n", h_long_kokyaku_no);
            /*------------------------------------------------------------*/
        }

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_MMKG;
        sqlca.restAndExecute(h_long_kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(chg_format_buf, "[%d]", h_long_kokyaku_no);
            APLOG_WT("904", 0, null, "CURSOR OPEN ERR", sqlca.sqlcode,
                    "MM顧客企業別属性情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        memset(g_sDmlist.dm_tome_kbn, 0x00, sizeof(g_sDmlist.dm_tome_kbn));
        for (i = 0; i < C_ITEM_BLOCK; i++)
            g_sDmlist.taikai_ymd[i].arr = 0;

        /*-------------------------------------*/
        /* DMリスト作成情報を取得        */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) {  /* FOR DMリスト作成処理 */
            /* ホスト変数・構造体を初期化 */
            mmkokgy_t =new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
            msryinf_t=new MS_RIYU_INFO_TBL();
            memset(mmkokgy_t, 0x00, sizeof(mmkokgy_t)); /* MM顧客企業別属性情報  バッファ */
            memset(msryinf_t, 0x00, sizeof(msryinf_t)); /* MS理由情報            バッファ */


            sqlca.fetchInto(mmkokgy_t.kigyo_cd,
                    msryinf_t.riyu_setumei,
                    mmkokgy_t.taikai_ymd);
            /* カーソルフェッチ */
//            EXEC SQL FETCH CUR_MMKG
//            INTO:
//            mmkokgy_t.kigyo_cd    ,
//                :msryinf_t.riyu_setumei,
//                :mmkokgy_t.taikai_ymd;

            /* データ無しの場合、ブレーク */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
                /* ＥＯＦ以外のエラーは異常終了 */
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                sprintf(chg_format_buf, "[%d]", h_long_kokyaku_no);
                APLOG_WT("904", 0, null, "FETCH ERR", sqlca.sqlcode,
                        "MM顧客企業別属性情報", chg_format_buf, 0, 0);
//                EXEC SQL CLOSE CUR_MMKG;
                sqlcaManager.close("CUR_MMKG");
                return C_const_NG;
            }

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_MmKoKigyo mmkokgy_t.kigyo_cd    =[%d]\n", mmkokgy_t.kigyo_cd);
                C_DbgMsg("*** cmBTdldmB_MmKoKigyo msryinf_t.riyu_setumei=[%s]\n", msryinf_t.riyu_setumei);
                C_DbgMsg("*** cmBTdldmB_MmKoKigyo mmkokgy_t.taikai_ymd  =[%d]\n", mmkokgy_t.taikai_ymd);
                /*------------------------------------------------------------*/
            }

            /* 取得情報を出力ＷＯＲＫに退避 */
            /* 補足説明の企業コードを変更。KDM→MBIL  */
            wk_psw = -1;
            if (mmkokgy_t.kigyo_cd.intVal() == C_KIGYO_CD_CFH) {
                wk_psw = C_IDX_CFH;
                g_sDmlist.taikai_ymd[C_IDX_CFH] = mmkokgy_t.taikai_ymd;
            } else if (mmkokgy_t.kigyo_cd.intVal() == C_KIGYO_CD_EC) {
                wk_psw = C_IDX_EC;
                g_sDmlist.taikai_ymd[C_IDX_EC] = mmkokgy_t.taikai_ymd;
            }/*else if ( mmkokgy_t.kigyo_cd == C_KIGYO_CD_KODAMA ) {
            wk_psw = C_IDX_KDM                                        ;
            g_sDmlist.taikai_ymd[C_IDX_KDM]      = mmkokgy_t.taikai_ymd;
        }*/ else if (mmkokgy_t.kigyo_cd.intVal() == C_KIGYO_CD_MOBILE) {
                wk_psw = C_IDX_MBIL;
                g_sDmlist.taikai_ymd[C_IDX_MBIL] = mmkokgy_t.taikai_ymd;
            } else if (mmkokgy_t.kigyo_cd.intVal() == C_KIGYO_CD_EBINA) {
                wk_psw = C_IDX_EBN;
                g_sDmlist.taikai_ymd[C_IDX_EBN] = mmkokgy_t.taikai_ymd;
            } else {
                wk_psw = C_IDX_FC;
                g_sDmlist.taikai_ymd[C_IDX_FC] = mmkokgy_t.taikai_ymd;
            }

            if (wk_psw != -1) { /* IF 理由説明の全角SJIS変換 */
                /*------------------------*/
                /* 理由説明の全角SJIS変換 */
                /*------------------------*/
                memset(wk_buff, 0x00, sizeof(wk_buff));
                memset(wk_buff2, 0x00, sizeof(wk_buff2));
                memcpy(wk_buff, msryinf_t.riyu_setumei,
                        strlen(msryinf_t.riyu_setumei));
                BT_Rtrim(wk_buff, strlen(wk_buff));

                /* 全角に変換する */
                rtn_cd = C_ConvHalf2Full(wk_buff, wk_buff2);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                            0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("cmBTdldmB_MmKoKigyo", C_const_NG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* 処理を終了する */
                    return (C_const_NG);
                }

                memset(sjis_str_riyu_setumei, 0x00, sizeof(sjis_str_riyu_setumei));

                /* UTF8 → SJIS変換処理 */
                rtn_cd = C_ConvUT2SJ(wk_buff2,
                        strlen(wk_buff2),
                        sjis_str_riyu_setumei, sjis_len_riyu_setumei);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                            0, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------*/
                        C_DbgEnd("cmBTdldmB_MmKoKigyo", C_const_NG, 0, 0);
                        /*---------------------------------------------*/
                    }
                    /* 処理を終了する */
                    return (C_const_NG);
                }

                /* 取得情報を出力ＷＯＲＫに退避 */
                memcpy(g_sDmlist.dm_tome_kbn[wk_psw], sjis_str_riyu_setumei, sjis_len_riyu_setumei);

            } /* ENDIF 理由説明の全角SJIS変換 */

        }  /* NEXT DMリスト作成処理 */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_MMKG;
        sqlcaManager.close("CUR_MMKG");

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_MmKoKigyo", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*--- cmBTdldmB_MmKoKigyo Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_MsCardInf                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTdldmB_MsCardInf()                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      カード情報取得処理                                                    */
    /*      MSカード情報からDMリストファイル情報を取得する。                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_MsCardInf() {
        int wi_loop; /* ループカウンタ               */
        StringDto wk_sqlbuf = new StringDto(2048); /* ＳＱＬ文編集用               */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_MsCardInf処理");
            /*------------------------------------------------------------*/
        }

        /* 顧客情報検索ＳＱＬ文作成            */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        strcpy(wk_sqlbuf,
                "SELECT  D.会員番号 "
                        + ",D.企業コード "
                        + "FROM ( SELECT "
                        + "C.会員番号 "
                        + ",C.企業コード "
                        + ",ROW_NUMBER() OVER  "
                        + "(PARTITION BY  "
                        + "C.企業コード "
                        + ",C.顧客番号  "
                        + "ORDER BY C.カードステータス "
                        + ")  G_row  "
                        + "FROM  MSカード情報 C "
                        + "WHERE C.顧客番号 = :h_long_kokyaku_no "  /* 対象顧客リストより取得 */
                        + "ORDER BY "
                        + "C.企業コード "
                        + ", C.カードステータス "
                        + ") D "
                        + "WHERE G_row   =  1 "
                /*           "AND ROWNUM <= :h_rownum " */
        );

        /* ＨＯＳＴ変数にセット */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
        sqlca.sql = str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_cardinf FROM:
//        str_sql;

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_MSCD CURSOR FOR sql_cardinf;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_MSCD USING:
//        h_long_kokyaku_no;
        sqlca.open(h_long_kokyaku_no);
        /*                                    :h_rownum         ; */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "CURSOR OPEN ERR", sqlca.sqlcode,
                    "MSカード情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MsCardInf *** MSカード情報 SQL=[%s]\n", str_sql);
            C_DbgMsg("*** cmBTdldmB_MsCardInf *** MSカード情報 SELECT key=[%d]\n", h_long_kokyaku_no);
            /*------------------------------------------------------------*/
        }

        memset(g_sDmlist.kaiin_no, 0x00, sizeof(g_sDmlist.kaiin_no));

        /*-------------------------------------*/
        /* DMリスト作成情報を取得        */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) {  /* FOR DMリスト作成処理 */
            /* ホスト変数・構造体を初期化 */
            mscdinf_t=new MS_CARD_INFO_TBL();
            memset(mscdinf_t, 0x00, sizeof(mscdinf_t)); /* MSカード情報  バッファ */

            /* カーソルフェッチ */
            sqlca.fetchInto(mscdinf_t.kaiin_no,
                    mscdinf_t.kigyo_cd);
//            EXEC SQL FETCH CUR_MSCD
//            INTO:
//            mscdinf_t.kaiin_no,
//                :mscdinf_t.kigyo_cd;

            /* データ無しの場合、ブレーク */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
                /* ＥＯＦ以外のエラーは異常終了 */
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                sprintf(chg_format_buf, "[%d]", h_long_kokyaku_no);
                APLOG_WT("904", 0, null, "FETCH ERR", sqlca.sqlcode,
                        "MSカード情報", chg_format_buf, 0, 0);
//                EXEC SQL CLOSE CUR_MSCD;
                sqlca.curse_close();
                return C_const_NG;
            }
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_MsCardInf *** 企業      [%d]\n", mmkokgy_t.kigyo_cd);
                C_DbgMsg("*** cmBTdldmB_MsCardInf *** 会員番号  [%s]\n", mscdinf_t.kaiin_no.arr);
                /*------------------------------------------------------------*/
            }
            /* 取得情報を出力ＷＯＲＫに退避 */
            /* 企業コード名を変更 KDM →MBIL */
            if (mscdinf_t.kigyo_cd.intVal() == C_KIGYO_CD_CFH) {
                sprintf(g_sDmlist.kaiin_no[C_IDX_CFH], "%s", mscdinf_t.kaiin_no.arr);
            } else if (mscdinf_t.kigyo_cd.intVal() == C_KIGYO_CD_EC) {
                sprintf(g_sDmlist.kaiin_no[C_IDX_EC], "%s", mscdinf_t.kaiin_no.arr);
            }/* else if ( mscdinf_t.kigyo_cd == C_KIGYO_CD_KODAMA ) {
            sprintf( g_sDmlist.kaiin_no[C_IDX_KDM], "%s", mscdinf_t.kaiin_no.arr);
        }*/ else if (mscdinf_t.kigyo_cd.intVal() == C_KIGYO_CD_MOBILE) {
                sprintf(g_sDmlist.kaiin_no[C_IDX_MBIL], "%s", mscdinf_t.kaiin_no.arr);
            } else if (mscdinf_t.kigyo_cd.intVal() == C_KIGYO_CD_EBINA) {
                sprintf(g_sDmlist.kaiin_no[C_IDX_EBN], "%s", mscdinf_t.kaiin_no.arr);
            } else {
                sprintf(g_sDmlist.kaiin_no[C_IDX_FC], "%s", mscdinf_t.kaiin_no.arr);
            }


        }  /* NEXT DMリスト作成処理 */

        /* カーソルクローズ */
//    EXEC SQL
//    CLOSE CUR_MSCD;
        sqlca.curse_close();

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_MsCardInf", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*--- cmBTdldmB_MsCardInf Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_MsCnpnInf                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_MsCnpnInf()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      キャンペーン情報更新処理                                              */
    /*      DMリスト出力対象のキャンペーンの「連動済みフラグ」を更新する。        */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_MsCnpnInf() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_MsCnpnInf処理");
            /*------------------------------------------------------------*/
        }
        sqlca.sql.arr = "UPDATE MSキャンペーン情報 " +
                "        SET 連動済みフラグ = 1," +
                "        バッチ更新日 = ?," +
                "                最終更新日 = ?," +
                "                最終更新日時 = SYSDATE()," +
                "                最終更新プログラムＩＤ = ?" +
                "        WHERE キャンペーンＩＤ = ?";
        /* MSキャンペーン情報の更新 */
//        EXEC SQL UPDATE MSキャンペーン情報 @CMSD
//        SET 連動済みフラグ = 1,
//        バッチ更新日 = :h_bat_day,
//                最終更新日 = :h_bat_day,
//                最終更新日時 = SYSDATE,
//                最終更新プログラムＩＤ = :h_Program_Name
//        WHERE キャンペーンＩＤ = :mscpmng_t.campaign_id;

        sqlca.restAndExecute(h_bat_day, h_bat_day, h_Program_Name, mscpmng_t.campaign_id);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_MsCnpnInf *** MSキャンペーン情報 UPDATE sqlcode=[%d]\n",
                    sqlca.sqlcode);
            C_DbgMsg("*** cmBTdldmB_MsCnpnInf *** キャンペーンＩＤ=%s", mscpmng_t.campaign_id);
            /*------------------------------------------------------------*/
        }

        /* エラーの場合処理終了 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "キャンペーンＩＤ=%s", mscpmng_t.campaign_id);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "MSキャンペーン情報", chg_format_buf, 0, 0);
//            EXEC SQL ROLLBACK RELEASE;
            sqlcaManager.rollbackRelease(sqlca);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_MsCnpnInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
//        EXEC SQL COMMIT WORK;
        sqlcaManager.commit();

        return C_const_OK;
        /*--- cmBTdldmB_MsCnpnInf Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_Chk_Arg(char *Arg_in)                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：引数値                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_Chk_Arg処理");
            C_DbgMsg("*** cmBTdldmB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        if (memcmp(Arg_in, DEF_ARG_d, 2) == 0) {        /* -d処理対象日付CHK   */
            if (chk_arg_d != DEF_OFF) {
                sprintf(chg_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_d = DEF_ON;

            if (strlen(Arg_in) < 3) {                 /* 桁数チェック           */
                sprintf(chg_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_Chk_Arg Bottom -----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_OpenInpF                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_OpenInpF()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルオープン処理                                      */
    /*              キャンペーン対象顧客ファイルをオープンする                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_OpenInpF() {
        int rtn_cd; /* 関数戻り値                   */
        StringDto wk_buff = new StringDto(256); /* ファイル名一時格納用         */
        StringDto wk_buf1 = new StringDto(128); /* 照合ファイル文字列格納用     */
        StringDto wk_buf2 = new StringDto(40); /* TRIM文字列格納用             */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_OpenInpF処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(inp_fl_name, 0x00, sizeof(inp_fl_name));
        memset(wk_buff, 0x00, sizeof(wk_buff));

        /*----------------*/
        /* ファイル名作成 */
        /*----------------*/
        /* 検索文字列の設定(ファイル名) [キャンペーンＩＤ_CUSTOMER_YYYYMMDDHHMMSS.csv] */
        memset(wk_buf2, 0x00, sizeof(wk_buf2));
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        memcpy(wk_buf2, g_sDmlist.campaign_id
                , strlen(g_sDmlist.campaign_id));
        /* スペース削除処理 */
        BT_Rtrim(wk_buf2, strlen(wk_buf2));
        sprintf(wk_buf1, "%s_CUSTOMER", wk_buf2);
        /* 入力ファイル名取得処理 */
        rtn_cd = getFileName(wk_buf1, inp_file_dir, wk_buff);
        sprintf(inp_fl_name, "%s/%s", inp_file_dir, wk_buff);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_OpenInpF *** 入力ファイル名作成 rtn [%d]\n", rtn_cd);
            C_DbgMsg("*** cmBTdldmB_OpenInpF *** 入力ファイル名         [%s]\n", inp_fl_name);
            /*------------------------------------------------------------*/
        }
        if (rtn_cd == DEF_Read_EOF) {
            /* ファイルなしで戻る */
            return (DEF_Read_EOF);
        }

        /* ファイルオープン */
        if ((fp_inp = fopen(inp_fl_name.arr, FileOpenType.r)).fd == C_const_NG) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_OpenInpF *** 入力ファイルオープンNG[%s]\n", inp_fl_name);
                /*------------------------------------------------------------*/
            }
            /* ファイルなしで戻る */
            return (DEF_Read_EOF);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_OpenInpF *** 入力ファイル[%s]\n", inp_fl_name);
            C_DbgEnd("cmBTdldmB_OpenInpF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_OpenInpF Bottom ----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_OpenOutF                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_OpenOutF()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              出力ファイルオープン処理                                      */
    /*              DMリストをオープンする                                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_OpenOutF() {
        StringDto wk_buff = new StringDto(256); /* ファイル名一時格納用         */
        StringDto wk_buff2 = new StringDto(31); /* 文字列    一時格納用         */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_OpenOutF処理");
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------------------------------------*/
        /* ファイル名作成、オープン                                          */
        /* ファイル作成日(yyyymmdd)_Mail_キャンペーンＩＤ_クーポンコード.csv */
        /*-------------------------------------------------------------------*/
        /* 初期化 */
        memset(out_fl_name, 0x00, sizeof(out_fl_name));
        memset(wk_buff, 0x00, sizeof(wk_buff));
        /* ファイル名作成 */
        strncpy(wk_buff, g_bat_date, sizeof(g_bat_date)); /* ファイル作成日   */
        strcat(wk_buff, "_DM_");
        /* キャンペーンＩＤ ＴＲＩＭ */
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        memcpy(wk_buff2, g_sDmlist.campaign_id
                , strlen(g_sDmlist.campaign_id));
        /* スペース削除処理 */
        BT_Rtrim(wk_buff2, strlen(wk_buff2));
        strcat(wk_buff, wk_buff2); /* キャンペーンＩＤ */
        sprintf(wk_buff2, "_%f", g_sDmlist.coupon__cd); /* クーポンコード   */
        strcat(wk_buff, wk_buff2);
        sprintf(out_fl_name, "%s/%s.csv", out_file_dir, wk_buff);

        /* ファイル名作成、オープン */
        if ((fp_out = fopen(out_fl_name.arr, FileOpenType.w)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(chg_format_buf, "fopen(%s)", out_fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_OpenOutF *** %s\n", "ファイルオープンERR");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_OpenOutF *** 出力ファイル[%s]\n", out_fl_name);
            C_DbgEnd("cmBTdldmB_OpenOutF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_OpenOutF Bottom ----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_ReadInpF                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_ReadInpF()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル読込処理                                              */
    /*              ファイル読み込みをする。                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_ReadInpF() {
        int rtn_cd; /* 関数戻り値                   */
        int rtn_cd2; /* 関数戻り値                   */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_ReadInpF処理");
            /*------------------------------------------------------------*/
        }
        rtn_cd = fscanf(fp_inp, "%[^,],%[^\r\n]", inp_kokyaku_t.campaign_id
                , inp_kokyaku_t.kokyaku_no);
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** クーポンＩＤ[%s]\n", inp_kokyaku_t.campaign_id);
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** 顧客番号    [%s]\n", inp_kokyaku_t.kokyaku_no);
            /*---------------------------------------------*/
        }
        if (feof(fp_inp) != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_ReadInpF *** 入力ファイル読込EOF%s\n", "");
                C_DbgEnd("cmBTdldmB_ReadInpF処理", C_const_NOTEXISTS, 0, 0);
                /*---------------------------------------------*/
            }
            return (DEF_Read_EOF);
        }
        rtn_cd2 = ferror(fp_inp);
        if (rtn_cd2 != C_const_OK) {
            sprintf(chg_format_buf, "fscanf（%s）", inp_fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, rtn_cd2,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTdldmB_ReadInpF *** 入力ファイル読込ERROR%s\n", "");
                C_DbgEnd("cmBTdldmB_ReadInpF処理", C_const_NOTEXISTS, 0, 0);
                /*-------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* ホスト変数に取得情報を設定 */
        h_long_kokyaku_no = atol(inp_kokyaku_t.kokyaku_no);

        /* 取得情報を出力ＷＯＲＫに退避 */
        memset(g_sDmlist.kokyaku_no, 0x00, sizeof(g_sDmlist.kokyaku_no));
        sprintf(g_sDmlist.kokyaku_no, "%d", h_long_kokyaku_no);
        /*    strncpy(g_sDmlist.kokyaku_no, inp_kokyaku_t.kokyaku_no, sizeof(inp_kokyaku_t.kokyaku_no));*/

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** 顧客番号-出力WRK    [%d]\n", h_long_kokyaku_no);
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** 顧客番号-出力WRK    [%s]\n", g_sDmlist.kokyaku_no);
            C_DbgEnd("cmBTdldmB_ReadInpF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_ReadInpF Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_WriteOutF                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_WriteOutF()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル出力処理                                              */
    /*              ファイル書き込みをする。                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_WriteOutF() {
        int rtn_cd; /* 関数戻り値                   */

        int wk_slet; /* 関数戻り値(fwrite)           */
        StringDto wk_out_buff = new StringDto(4096); /* 出力情報格納用               */
        StringDto wk_buff = new StringDto(256); /* 出力情報一時格納用           */
        StringDto wk_buff2 = new StringDto(256); /* 出力情報一時格納用           */
        StringDto wk_buf1 = new StringDto(4096); /* 出力情報一時格納用(SJIS)     */
        StringDto sjis_str_header = new StringDto(4096); /* SJIS変換後文字列(ヘッダ)     */
        IntegerDto sjis_len_header = new IntegerDto(); /* SJIS変換後文字列のレングス    */
        int wi_loop; /* ループカウンタ               */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_WriteOutF処理");
            /*------------------------------------------------------------*/
        }

        /*------------------------------*/
        /* DMリストファイルの編集 */
        /*------------------------------*/
        /* ヘッダレコードファイル出力 */
        if (out_csv_cnt == 0) {  /* IF ヘッダレコードファイル出力 */

            memset(sjis_str_header, 0x00, sizeof(sjis_str_header));
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            memset(wk_out_buff, 0x00, sizeof(wk_out_buff));
            sprintf(wk_buf1, "抽出日,送信予約日,キャンペーンID,クーポンコード,顧客番号,"
                    + "会員番号１,会員番号２,会員番号３,会員番号４,会員番号５,"
                    + "氏名(漢字),氏名(カナ）,郵便番号,住所１,住所２,住所３,郵便番号バーコード,"
                    + "電話番号１,電話番号２,電話番号３,電話番号４,"
                    + "DM止め区分１,DM止め区分２,DM止め区分３,DM止め区分４,DM止め区分５,"
                    + "退会年月日１,退会年月日２,退会年月日３,退会年月日４,退会年月日５"
            );

            /* UTF8 → SJIS変換処理 */
            rtn_cd = C_ConvUT2SJ(wk_buf1,
                    strlen(wk_buf1),
                    sjis_str_header, sjis_len_header);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
            memcpy(wk_out_buff, sjis_str_header, sjis_len_header.arr);
            strcat(wk_out_buff, "\r\n");

            wk_slet = fwrite(wk_out_buff, strlen(wk_out_buff), 1, fp_out);
            rtn_cd = ferror(fp_out);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("cmBTdldmB_WriteOutF ヘッダレコードNG rtn=[%d]\n", rtn_cd);
                    /*-------------------------------------------------------------*/
                }
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                sprintf(chg_format_buf, "ヘッダレコード出力NG fwrite(%s)", out_fl_name);
                APLOG_WT("903", 0, null, chg_format_buf, wk_slet, 0, 0, 0, 0);
                return (C_const_NG);
            }
        }  /* ENDIF ヘッダレコードファイル出力 */

        /* 各項目の編集 */
        memset(wk_out_buff, 0x00, sizeof(wk_out_buff));
        memset(wk_buff, 0x00, sizeof(wk_buff));

        g_sDmlist.select_ymd.arr = h_bat_day; /* バッチ処理日  */
        sprintf(wk_buff, "\"%d\"", g_sDmlist.select_ymd); /* 抽出日               */
        strcpy(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%d\"", g_sDmlist.yoyaku_ymd); /* 送信予約日           */
        strcat(wk_out_buff, wk_buff);

        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        memcpy(wk_buff2, g_sDmlist.campaign_id, sizeof(g_sDmlist.campaign_id));
        BT_Rtrim(wk_buff2, strlen(wk_buff2));
        sprintf(wk_buff, ",\"%s\"", wk_buff2);                              /* キャンペーンＩＤ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%10.0f\"", g_sDmlist.coupon__cd); /* クーポンコード       */
        strcat(wk_out_buff, wk_buff);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** 顧客番号-出力WRK    [%s]\n", g_sDmlist.kokyaku_no);
            /*------------------------------------------------------------*/
        }
        sprintf(wk_buff, ",\"%s\"", g_sDmlist.kokyaku_no); /* 顧客番号       */
        strcat(wk_out_buff, wk_buff);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTdldmB_ReadInpF *** 出力エリア出力WRK    [%s]\n", wk_out_buff);
            /*------------------------------------------------------------*/
        }

        for (wi_loop = 0; wi_loop < C_ITEM_BLOCK; wi_loop++) {  /* FOR 会員番号１～５ */
            if (strlen(g_sDmlist.kaiin_no[wi_loop]) == 0) {
                strcat(wk_out_buff, ",\"\"");
            } else {
                sprintf(wk_buff, ",\"%s\"", g_sDmlist.kaiin_no[wi_loop]); /*  会員番号１～５       */
                strcat(wk_out_buff, wk_buff);
            }
        } /* NEXT 会員番号１～５ */

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.kokyaku_mesho);            /* 氏名(漢字)           */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.kokyaku_kana_mesho);       /* 氏名(カナ)           */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.yubin_no);       /* 郵便番号 */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", "");       /* 住所１ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.jusho_2);       /* 住所２ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.jusho_3);       /* 住所３ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.yubin_no_cd);       /* 郵便番号コード */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.tel_no1);           /* 電話番号１ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.tel_no2);           /* 電話番号２ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.tel_no3);           /* 電話番号３ */
        strcat(wk_out_buff, wk_buff);

        sprintf(wk_buff, ",\"%s\"", g_sDmlist.tel_no4);           /* 電話番号４ */
        strcat(wk_out_buff, wk_buff);


        for (wi_loop = 0; wi_loop < C_ITEM_BLOCK; wi_loop++) {  /* FOR ＤＭ止め区分１～５ */
            if (StringUtils.isEmpty(g_sDmlist.dm_tome_kbn[wi_loop].strVal())) {
                strcat(wk_out_buff, ", ");
            } else {
                sprintf(wk_buff, ",\"%s\"", g_sDmlist.dm_tome_kbn[wi_loop]);           /* ＤＭ止め区分１～５ */
                strcat(wk_out_buff, wk_buff);
            }
        } /* NEXT ＤＭ止め区分１～５ */

        for (wi_loop = 0; wi_loop < C_ITEM_BLOCK; wi_loop++) {  /* FOR 退会年月日１～５ */
            if (StringUtils.isEmpty(g_sDmlist.taikai_ymd[wi_loop].strVal())) {
                strcat(wk_out_buff, ",\"\"");
            } else {
                sprintf(wk_buff, ",\"%d\"", g_sDmlist.taikai_ymd[wi_loop]); /* 退会年月日１～５     */
                strcat(wk_out_buff, wk_buff);
            }
        } /* NEXT 退会年月日１～５ */

        /* 改行 */
        strcat(wk_out_buff, "\r\n");

        /* ファイルに出力 */
        wk_slet = fwrite(wk_out_buff, strlen(wk_out_buff), 1, fp_out);
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK) {
            sprintf(chg_format_buf, "明細レコード出力NG fwrite(%s)", out_fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, wk_slet, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_WriteOutF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTdldmB_WriteOutF Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_CloseInpF                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_CloseInpF()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルクローズ処理                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_CloseInpF() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_CloseInpF処理");
            /*------------------------------------------------------------*/
        }
        fclose(fp_inp);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_CloseInpF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return C_const_OK;
        /*--- cmBTdldmB_CloseInpF Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdldmB_CloseOutF                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdldmB_CloseOutF()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              出力ファイルクローズ処理                                      */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTdldmB_CloseOutF() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTdldmB_CloseOutF処理");
            /*------------------------------------------------------------*/
        }
        fclose(fp_out);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTdldmB_CloseOutF処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return C_const_OK;
        /*--- cmBTdldmB_CloseOutF Bottom ---------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： getFileName                                                 */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int getFileName(char *file_id, char *file_dir, char *file_name)       */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              入力ファイル名取得処理                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    file_id   ： ファイルＩＤ                     */
    /*              char       *    file_dir  ： 検索対象にするディレクトリ       */
    /*              char       *    file_name ： 対象ファイル名を返す             */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    public int getFileName(StringDto file_id, StringDto file_dir, StringDto file_name) {
        LinkedList<File> dir;
        StringDto logbuf = new StringDto(200);
        File dent;



        /* 初期化 */
        strcpy(file_name, "");

        /* ディレクトリのオープン */
        dir = opendir(file_dir.arr);
        if (dir == null) {
            sprintf(logbuf, "opendir(%s)", file_dir);
            APLOG_WT("903", 0, null, logbuf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }


        for (; ; ) {
            dent = readdir(dir);
            if (dent == null) break;

            if (strncmp(dent.getName(), file_id.arr, strlen(file_id)) == 0) {
                /* キャンペーンID_CUSTOMERまで一致する */
                strcpy(file_name, dent.getName());
            }
        } /* next */

        closedir(dir);

        if (strlen(file_name) == 0) return DEF_Read_EOF; /* 該当なし */


        return C_const_OK;
        /*--- getFileName Bottom ----------------------------------------------*/
    }

}
