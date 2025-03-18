package jp.co.mcc.nttdata.batch.business.service.cmBTecciB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_KIGYO_CD_EC;
import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;

/**
 * /*******************************************************************************
 * *   プログラム名   ： ＥＣ会員情報連動（cmBTecciB）
 * *
 * *   【処理概要】
 * *    本機能は当日更新されたＥＣ会員に対し、
 * *    静態情報を取得して顧客情報返送ファイルを作成する。
 * *
 * *   【引数説明】
 * *     -o顧客情報名       :（必須）出力の物理ファイル名 SJIS（$CM_APWORK_DATE）
 * *     -DEBUG(-debug)     :（任意）デバッグモードでの実行
 * *     -date処理日付      :（必須）処理日付（YYYYMMDD）
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
 * *     1.00 :   2013/04/23 SSI.上野：初版
 * *     1.01 :   2013/05/07 SSI.上野：前日分EC会員情報連動処理が異常終了の場合
 * *                                   前日連動対象分も含めての連動とする
 * *                                   （対象処理日付を引数で取得するよう変更）
 * *                                   出力時にUTF8-SJIS変換を追加
 * *     1.02 :   2013/05/16 SSI.上野：会員氏名（漢字）：全角変換処理追加
 * *     1.03 :   2013/06/18 SSI.本田：対象顧客番号抽出時にカード情報の有無を条件に追加
 * *     2.00 :   2014/01/02 SSI.本田：ダミー登録データを抽出対象外とする
 * *     3.00 :   2016/04/22 SSI.石戸：最終静態更新日の条件を追加
 * *     4.00 :   2016/08/04 SSI.田頭：C-MAS対応
 * *                                    ファイルレイアウト変更に伴い、
 * *                                    e-mail受信可否、ＤＭ受信可否を追加
 * *    30.00 :   2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 * *                                     (TS利用可能ポイント情報構造体更新のため)
 * *    40.00 :   2022/10/11 SSI.申    : MCCM初版
 * *------------------------------------------------------------------------------
 * *  $Id:$
 * *------------------------------------------------------------------------------
 * *  Copyright (C) 2012 NTT DATA CORPORATION
 * ******************************************************************************
 */
@Service
public class CmBTecciBServiceImpl extends CmABfuncLServiceImpl implements CmBTecciBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */


    MM_KOKYAKU_INFO_TBL mmkoinf_t;  /* MM顧客情報            バッファ */
    MM_KOKYAKU_ZOKUSE_INFO_TBL mmkozok_t;  /* MM顧客属性情報        バッファ */
    MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL mmkigyo_t;  /* MM顧客企業別属性情報  バッファ */
    MS_CARD_INFO_TBL mscardi_t;  /* MSカード情報          バッファ */
    TS_RIYO_KANO_POINT_TBL tsrypnt_t;  /* TS利用可能ポイント情報バッファ */

    /* 動的ＳＱＬ作成用 */
    StringDto str_sql = new StringDto(12288);               /* 実行用SQL文字列                */
    StringDto str_sql2 = new StringDto(2048);               /* 実行用SQL文字列                */

    /* 処理用 */
    int ghi_this_date;                 /* 処理日付                       */
    StringDto gh_kokyaku_no = new StringDto(15 + 1);           /* 顧客番号                       */
    StringDto gh_kaiin_no = new StringDto(15 + 1);             /* 会員番号                       */

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_O = "-o";           /* 顧客情報名                         */
    String DEF_DEBUG = "-DEBUG";           /* デバッグスイッチ                   */
    String DEF_debug = "-debug";           /* デバッグスイッチ                   */
    String DEF_ARG_D = "-date";           /* 処理対象日付                       */
    /*---------------------------------------------*/
    String C_PRGNAME = "ＥＣ会員情報連動";   /* APログ用機能名                     */
    /*---------------------------------------------*/
    String C_KAIIN_NO_HIKAIIN = "9999999999999";   /* 非会員会員番号                  */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int gi_ok_cnt;                /* 正常処理件数                       */
    int chk_arg_o;
    /**
     * 引数-oチェック用
     **/
    int chk_arg_d;
    /**
     * 引数-dateチェック用
     **/
    StringDto arg_o_Value = new StringDto(256);
    /**
     * 引数-o設定値
     **/
    StringDto arg_d_Value = new StringDto(256);
    /**
     * 引数-date設定値
     **/
    StringDto out_file_dir = new StringDto(4096);
    /**
     * ワークディレクトリ
     **/
    StringDto fl_name = new StringDto(4096);
    /**
     * ファイル名
     **/
    StringDto fl_name2 = new StringDto(1024);
    /**
     * ファイル名
     **/
    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                 **/

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* 顧客情報ファイル構造体                                                     */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_out;                          /* 出力ファイルポインタ         */


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

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        gi_ok_cnt = 0;                  /* 正常処理件数                       */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
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
        chk_arg_o = DEF_OFF;
        chk_arg_d = DEF_OFF;
        memset(arg_o_Value, 0x00, sizeof(arg_o_Value));
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

            } else if (memcmp(arg_Work1, DEF_ARG_O, 2) == 0) {   /* -oの場合       */
                rtn_cd = cmBTecciB_Chk_Arg(arg_Work1);    /* パラメータCHK      */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_o_Value, arg_Work1.substring(2));
                } else {
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else if (memcmp(arg_Work1, DEF_ARG_D, 5) == 0) {   /* -dateの場合    */
                rtn_cd = cmBTecciB_Chk_Arg(arg_Work1);    /* パラメータCHK      */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_d_Value, arg_Work1.substring(5));
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

        /* 必須パラメータ未指定チェック */
        if (chk_arg_o == DEF_OFF) {
            sprintf(chg_format_buf, "-o 引数の値が不正です");
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了                 */
            return exit(C_const_APNG);
        }
        if (chk_arg_d == DEF_OFF) {
            sprintf(chg_format_buf, "-date 引数の値が不正です");
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了                 */
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
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n",
                    "CM_APWORK_DATE");
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
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n",
                    out_file_dir);
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
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  ＥＣ会員情報連動主処理                       */
        rtn_cd = cmBTecciB_main();

        if (rtn_cd == C_const_APNG) {
            return exit(C_const_APNG);
        }
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTecciB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "ＥＣ会員情報連動主処理に失敗しました",
                    0, 0, 0, 0, 0);
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

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecciB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTecciB_main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ＥＣ会員情報連動主処理                                                 */
    /*     顧客情報ファイルを作成する。                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_main() {
        int rtn_cd;                     /* 関数戻り値                       */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_main処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;

        /*------------------------------------------------------------*/
        /* バッチ日付取得                                             */
        /*------------------------------------------------------------*/
        /* バインド用に数値化 */
        ghi_this_date = atoi(arg_d_Value);

        /*------------------------------------------------------------*/
        /* ワークテーブル作成                                         */
        /*------------------------------------------------------------*/
        rtn_cd = cmBTecciB_Make_WrkTBL();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTecciB_Make_WrkTBL NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "ワークテーブル作成に失敗しました",
                    0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        /* コミット                                                   */
        /*------------------------------------------------------------*/
//        EXEC SQL COMMIT WORK;
        sqlca.commit();

        /*-------------------------------------*/
        /*  顧客情報ファイル（管理側）OPEN     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 顧客情報ファイル(%s)\n", "オープン");
            /*------------------------------------------------------------*/
        }
        rtn_cd = cmBTecciB_OpenFile(2);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 顧客情報ファイルオープン NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fopen（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return C_const_APNG;
        }

        /*------------------------------------------------------------*/
        /* 管理ＤＢ連動設定                                           */
        /*------------------------------------------------------------*/
        /* 管理ＤＢ連動設定処理 */
        rtn_cd = cmBTecciB_Set_KanriDB();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTecciB_Set_KanriDB NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            fclose(fp_out);               /* File Close                         */
            APLOG_WT("912", 0, null, "管理ＤＢ連動設定処理に失敗しました",
                    0, 0, 0, 0, 0);
            return C_const_NG;
        }

        fclose(fp_out);                   /* File Close                         */
        /* 処理件数を出力 */
        APLOG_WT("101", 0, null, fl_name2, gi_ok_cnt, 0, 0, 0, 0);
        gi_ok_cnt = 0;                      /* 正常処理件数                       */

        /*-------------------------------------*/
        /*  顧客情報ファイル（制度側）OPEN     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 顧客情報ファイル(%s)\n", "オープン");
            /*------------------------------------------------------------*/
        }
        rtn_cd = cmBTecciB_OpenFile(1);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 顧客情報ファイルオープン NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fopen（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return C_const_APNG;
        }

        /*------------------------------------------------------------*/
        /* 制度ＤＢ連動設定                                           */
        /*------------------------------------------------------------*/
        /* 制度ＤＢ連動設定処理 */
        rtn_cd = cmBTecciB_Set_SedoDB();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTecciB_Set_SedoDB NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            fclose(fp_out);               /* File Close                         */
            APLOG_WT("912", 0, null, "制度ＤＢ連動設定処理に失敗しました",
                    0, 0, 0, 0, 0);
            return C_const_NG;
        }

        fclose(fp_out);                   /* File Close                         */
        /* 処理件数を出力 */
        APLOG_WT("101", 0, null, fl_name2, gi_ok_cnt, 0, 0, 0, 0);
        gi_ok_cnt = 0;                      /* 正常処理件数                       */

        return (C_const_OK);              /* 処理終了                           */
        /*--- cmBTecciB_main Bottom --------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecciB_Set_SedoDB                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTecciB_Set_SedoDB()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              制度ＤＢから連動対象顧客情報を編集し、                        */
    /*              「MSカード情報」、「TS利用可能ポイント情報」                  */
    /*              顧客情報ファイルに出力する。                                  */
    /*              WM顧客番号ワークに処理した顧客番号を登録する。                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_Set_SedoDB() {
        /* ローカル変数 */
        int rtn_cd;                       /* 関数戻り値                 */
        StringDto wk_sqlbuf = new StringDto(2048);              /* ＳＱＬ文編集用             */
        int wi_loop;                      /* ループカウンタ             */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_Set_SedoDB処理");
            /*---------------------------------------------------------------------*/
        }

        /* ---------------------------------------- */
        /* 顧客管理情報の抽出                       */
        /* ---------------------------------------- */
        /* 初期化 */
        rtn_cd = C_const_OK;
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        /* 顧客情報検索ＳＱＬ文作成            */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        sprintf(wk_sqlbuf,
                "SELECT WS顧客番号.顧客番号 "
                        + ",NVL(CardInfo.会員番号, 0) "
                        + ",NVL(TS利用可能ポイント情報.入会店舗, 0) "
                        + "FROM WS顧客番号 "
                        + "LEFT JOIN ( SELECT 会員番号, 顧客番号 "
                        + "FROM ( SELECT 会員番号 "
                        + ", 顧客番号 "
                        + ", ROW_NUMBER() OVER ( "
                        + "PARTITION BY 顧客番号 "
                        + "ORDER BY 発行年月日 DESC "
                        + ") G_Row "
                        + "FROM MSカード情報 "
                        + "WHERE 企業コード = 1020 ) "                         /* 20230603 MCCM CF会員のみ対応 */
                        + "WHERE G_Row = 1 ) CardInfo "
                        + "ON  WS顧客番号.顧客番号 = CardInfo.顧客番号 "
                        + "LEFT JOIN TS利用可能ポイント情報 "
                        + "  ON  WS顧客番号.顧客番号 = TS利用可能ポイント情報.顧客番号 "
                        + "ORDER BY WS顧客番号.顧客番号 ASC ");

        /* ＨＯＳＴ変数にセット */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sqlbuf);

        SqlstmDto sqlca = sqlcaManager.get("CMSD_KOSED01");
        /* 動的ＳＱＬ文解析 */
//        EXEC SQL PREPARE sql_sedodb FROM :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf,
                    0, 0, 0, 0);
            return C_const_NG;
        }

        sqlca.declare();
        /* カーソル宣言 */
//        EXEC SQL DECLARE CMSD_KOSED01 CURSOR FOR sql_sedodb;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "CURSOR ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソルオープン */
//        EXEC SQL OPEN CMSD_KOSED01;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "CURSOR OPEN ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /* 連動対象顧客情報を取得              */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) { /* for loop */
            /* ホスト変数・構造体を初期化 */
            mscardi_t = new MS_CARD_INFO_TBL();
            memset(mscardi_t, 0x00, sizeof(mscardi_t));
            /* MSカード情報バッファ           */
            tsrypnt_t = new TS_RIYO_KANO_POINT_TBL();
            memset(tsrypnt_t, 0x00, sizeof(tsrypnt_t));
            /* TS利用可能ポイント情報バッファ */
            /* カーソルフェッチ */
            sqlca.fetchInto(mscardi_t.kokyaku_no,
                    mscardi_t.kaiin_no,
                    tsrypnt_t.nyukai_tenpo);
//            EXEC SQL FETCH CMSD_KOSED01
//            INTO:
//            mscardi_t.kokyaku_no,
//                     :mscardi_t.kaiin_no,
//                     :tsrypnt_t.nyukai_tenpo;

            /* データ無しの場合、処理を正常終了 */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* for1 を抜ける */
                break;
            }
            /* データ無し以外エラーの場合、処理を異常終了する */
            else if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                sprintf(chg_format_buf,
                        "顧客番号=%s", mscardi_t.kokyaku_no.arr);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "顧客情報（制度ＤＢ）", chg_format_buf, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CMSD_KOSED01;
                sqlcaManager.close(sqlca);
                return C_const_NG;
            }

            /* 顧客番号 設定  */
            memset(gh_kokyaku_no, 0x00, sizeof(gh_kokyaku_no));
            strncpy(gh_kokyaku_no, mscardi_t.kokyaku_no.strVal(),
                    mscardi_t.kokyaku_no.len);

            /* 会員番号 設定  */
            memset(gh_kaiin_no, 0x00, sizeof(gh_kaiin_no));
            if (mscardi_t.kaiin_no.intVal() != 0) {
                strncpy(gh_kaiin_no, mscardi_t.kaiin_no.strVal(),
                        mscardi_t.kaiin_no.len);
            }
            /* スペース削除 */
            BT_Rtrim(gh_kaiin_no, strlen(gh_kaiin_no));

            /*------------------------------------------*/
            /* 顧客情報ファイル作成                     */
            /*------------------------------------------*/
            /* 顧客情報ファイル作成処理 */
            rtn_cd = FileOutputS();
            if (rtn_cd == C_const_NG) {
                /* error */
                APLOG_WT("903", 0, null, "FileOutputS", rtn_cd,
                        0, 0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CMSD_KOSED01;
                sqlcaManager.close(sqlca);

                /* 処理を終了する */
                return C_const_NG;
            }

            /* 正常件数カウントアップ */
            gi_ok_cnt++;
            if (gi_ok_cnt % 100000 == 0) {
                APLOG_WT("105", 0, null, fl_name2, gi_ok_cnt,
                        0, 0, 0, 0);
            }

        } /* END for loop */

        sqlcaManager.close(sqlca);
//        EXEC SQL CLOSE CMSD_KOSED01;         /* カーソルクローズ                  */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTecciB_Set_SedoDB処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----cmBTecciB_Set_SedoDB Bottom--------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecciB_Set_KanriDB                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*        public  int  cmBTecciB_Set_KanriDB()                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              管理ＤＢから連動対象顧客情報を編集し、                        */
    /*               ｢MM顧客情報｣｢MM顧客属性情報｣｢MM顧客企業別属性情報｣           */
    /*              顧客情報ファイルに出力する。                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_Set_KanriDB() {

        /* ローカル変数 */
        int rtn_cd;                       /* 関数戻り値                 */
        StringDto wk_sqlbuf = new StringDto(2048);              /* ＳＱＬ文編集用             */
        int wi_loop;                      /* ループカウンタ             */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_Set_KanriDB処理");
            /*------------------------------------------------------------*/
        }
        /* ---------------------------------------- */
        /* 顧客管理情報の抽出                       */
        /* ---------------------------------------- */
        /* 初期化 */
        rtn_cd = C_const_OK;
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        /* 顧客情報検索ＳＱＬ文作成            */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        sprintf(wk_sqlbuf,
                "SELECT WM顧客番号.顧客番号 "
                        + ",CONCAT(CONCAT(NULLIF(TRIM((NVL(MM顧客情報.顧客名字,' '))),''),'　',NULLIF(TRIM((NVL(MM顧客情報.顧客名前,' '))),''))) AS 会員氏名_漢字"             /*              2022/10/11 MCCM初版 MOD */
                        + ",CONCAT(CONCAT(NULLIF(TRIM((NVL(MM顧客情報.カナ顧客名字,' '))),''),'　',NULLIF(TRIM((NVL(MM顧客情報.カナ顧客名前,' '))),''))) AS 会員氏名_カナ"     /*                  2022/10/11 MCCM初版 MOD */
                        + ",NVL(RPAD(MM顧客属性情報.郵便番号,LENGTH(MM顧客属性情報.郵便番号)), ' ') "
                        + ",NVL(MM顧客属性情報.都道府県コード, 0) AS 住所１ "                                                                     /* 2022/10/11 MCCM初版 MOD */
                        + ",NVL(REPLACE(RPAD(MM顧客属性情報.住所,LENGTH(MM顧客属性情報.住所)), ',', ''), ' ') AS 住所２ "                                                          /* 2022/10/11 MCCM初版 MOD */
                        /*              ",NVL(MM顧客属性情報.住所３, ' ') "                                                                     */                  /* 2022/10/11 MCCM初版 DEL */
                        + ",NVL(RPAD(MM顧客属性情報.電話番号１,LENGTH(MM顧客属性情報.電話番号１)), ' ') "
                        + ",NVL(RPAD(MM顧客属性情報.電話番号２,LENGTH(MM顧客属性情報.電話番号２)), ' ') "
                        + ",NVL(RPAD(MM顧客属性情報.電話番号３,LENGTH(MM顧客属性情報.電話番号３)), ' ') "
                        + ",NVL(RPAD(MM顧客属性情報.電話番号４,LENGTH(MM顧客属性情報.電話番号４)), ' ') "
                        + ",NVL(MM顧客情報.誕生年,0) "
                        + ",NVL(MM顧客情報.誕生月,0) "
                        + ",NVL(MM顧客情報.誕生日,0) "
                        + ",NVL(MM顧客情報.性別,0) "
                        + ",NVL(MM顧客情報.最終静態更新日,0) "
                        + ",NVL(MM顧客情報.最終静態更新時刻,0) "
                        + ",NVL(MM顧客企業別属性情報.退会年月日,0) "
                        + ",NVL(MM顧客企業別属性情報.Ｅメール止め区分,5031) "
                        + ",NVL(MM顧客企業別属性情報.ＤＭ止め区分,3031) "
                        + "  FROM WM顧客番号 "
                        + "LEFT JOIN MM顧客情報 "
                        + " ON  WM顧客番号.顧客番号 = MM顧客情報.顧客番号 "
                        + "LEFT JOIN MM顧客属性情報 "
                        + " ON  WM顧客番号.顧客番号 = MM顧客属性情報.顧客番号 "
                        + "LEFT JOIN MM顧客企業別属性情報 "
                        + " ON  WM顧客番号.顧客番号 = MM顧客企業別属性情報.顧客番号 "
                        + " WHERE  MM顧客企業別属性情報.企業コード = %d "
                        + "ORDER BY WM顧客番号.顧客番号", C_KIGYO_CD_EC);

        /* ＨＯＳＴ変数にセット */
        memset(str_sql2, 0x00, sizeof(str_sql2));
        strcpy(str_sql2, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
        SqlstmDto sqlca = sqlcaManager.get("CMMD_KOINF01");
//        EXEC SQL PREPARE sql_kanridb FROM:
        sqlca.sql = str_sql2;

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, "PREPARE",
                    0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        sqlca.declare();
//        EXEC SQL DECLARE CMMD_KOINF01 CURSOR FOR sql_kanridb;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "CURSOR ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソルオープン */
        sqlca.open();
//        EXEC SQL OPEN CMMD_KOINF01;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "CURSOR OPEN ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /* 連動対象顧客情報を取得              */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) { /* for loop */
            /* ホスト変数・構造体を初期化 */
            mmkoinf_t = new MM_KOKYAKU_INFO_TBL();
            memset(mmkoinf_t, 0x00, sizeof(mmkoinf_t));  /* MM顧客情報バッファ */
            mmkozok_t = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
            memset(mmkozok_t, 0x00, sizeof(mmkozok_t));
            /* MM顧客属性情報バッファ         */
            mmkigyo_t = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
            memset(mmkigyo_t, 0x00, sizeof(mmkigyo_t));
            /* MM顧客企業別属性情報バッファ   */

            /* カーソルフェッチ */
            sqlca.fetchInto(mmkoinf_t.kokyaku_no,
                    mmkoinf_t.kokyaku_mesho,
                    mmkoinf_t.kokyaku_kana_mesho,
                    mmkozok_t.yubin_no,

                    mmkozok_t.todofuken_cd,                                                                          /* 2022/10/11 MCCM初版 MOD */
                    mmkozok_t.jusho_2,
                    mmkozok_t.denwa_no_1,
                    mmkozok_t.denwa_no_2,
                    mmkozok_t.denwa_no_3,
                    mmkozok_t.denwa_no_4,
                    mmkoinf_t.tanjo_y,
                    mmkoinf_t.tanjo_m,
                    mmkoinf_t.tanjo_d,
                    mmkoinf_t.sebetsu,
                    mmkoinf_t.saishu_setai_ymd,
                    mmkoinf_t.saishu_setai_hms,
                    mmkigyo_t.taikai_ymd,
                    mmkigyo_t.email_tome_kbn,
                    mmkigyo_t.dm_tome_kbn);
//            EXEC SQL FETCH CMMD_KOINF01
//            INTO:
//            mmkoinf_t.kokyaku_no,
//                     :mmkoinf_t.kokyaku_mesho,
//                     :mmkoinf_t.kokyaku_kana_mesho,
//                     :mmkozok_t.yubin_no,
//                     :
//            mmkozok_t.todofuken_cd,                                                                          /* 2022/10/11 MCCM初版 MOD */
//                     :mmkozok_t.jusho_2,
//            /*                   :mmkozok_t.jusho_3,                                                                            */ /* 2022/10/11 MCCM初版 DEL */
//                     :mmkozok_t.denwa_no_1,
//                     :mmkozok_t.denwa_no_2,
//                     :mmkozok_t.denwa_no_3,
//                     :mmkozok_t.denwa_no_4,
//                     :mmkoinf_t.tanjo_y,
//                     :mmkoinf_t.tanjo_m,
//                     :mmkoinf_t.tanjo_d,
//                     :mmkoinf_t.sebetsu,
//                     :mmkoinf_t.saishu_setai_ymd,
//                     :mmkoinf_t.saishu_setai_hms,
//                     :mmkigyo_t.taikai_ymd,
//                     :mmkigyo_t.email_tome_kbn,
//                     :mmkigyo_t.dm_tome_kbn;

            /* データ無しの場合、処理を正常終了 */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* for1 を抜ける */
                break;
            }
            /* データ無し以外エラーの場合、処理を異常終了する */
            else if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                sprintf(chg_format_buf,
                        "顧客番号=%s", mmkoinf_t.kokyaku_no.arr);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "顧客情報（管理ＤＢ）", chg_format_buf, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CMMD_KOINF01;
                sqlcaManager.close(sqlca);
                return C_const_NG;
            }

            /* 顧客番号 設定  */
            memset(gh_kokyaku_no, 0x00, sizeof(gh_kokyaku_no));
            strncpy(gh_kokyaku_no, mmkoinf_t.kokyaku_no.strVal(),
                    mmkoinf_t.kokyaku_no.len);

            /* スペース削除 */
            BT_Rtrim(mmkoinf_t.kokyaku_mesho,
                    strlen(mmkoinf_t.kokyaku_mesho));
            BT_Rtrim(mmkoinf_t.kokyaku_kana_mesho,
                    strlen(mmkoinf_t.kokyaku_kana_mesho));
            BT_Rtrim(mmkozok_t.yubin_no,
                    strlen(mmkozok_t.yubin_no));
//      BT_Rtrim( (char *)mmkozok_t.jusho_1,                                                                      /* 2022/10/11 MCCM初版 DEL */
//                strlen((char *)mmkozok_t.jusho_1) );                                                            /* 2022/10/11 MCCM初版 DEL */
            BT_Rtrim(mmkozok_t.jusho_2,
                    strlen(mmkozok_t.jusho_2));
            BT_Rtrim(mmkozok_t.jusho_3,
                    strlen(mmkozok_t.jusho_3));
            BT_Rtrim(mmkozok_t.denwa_no_1,
                    strlen(mmkozok_t.denwa_no_1));
            BT_Rtrim(mmkozok_t.denwa_no_2,
                    strlen(mmkozok_t.denwa_no_2));
            BT_Rtrim(mmkozok_t.denwa_no_3,
                    strlen(mmkozok_t.denwa_no_3));
            BT_Rtrim(mmkozok_t.denwa_no_4,
                    strlen(mmkozok_t.denwa_no_4));

            /*------------------------------------------*/
            /* 顧客情報ファイル作成                     */
            /*------------------------------------------*/
            /* 顧客情報ファイル作成処理 */
            rtn_cd = FileOutputM();
            if (rtn_cd == C_const_NG) {
                /* error */
                APLOG_WT("903", 0, null, "FileOutputM", rtn_cd,
                        0, 0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CMMD_KOINF01;
                sqlcaManager.close("CMMD_KOINF01");
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 正常件数カウントアップ */
            gi_ok_cnt++;
            if (gi_ok_cnt % 100000 == 0) {
                APLOG_WT("105", 0, null, fl_name2, gi_ok_cnt,
                        0, 0, 0, 0);
            }

        } /* END for loop */

//        EXEC SQL CLOSE CMMD_KOINF01;         /* カーソルクローズ                  */
        sqlcaManager.close("CMMD_KOINF01");
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTecciB_Set_KanriDB処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */

        /*--- cmBTecciB_Set_KanriDB Bottom -------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： FileOutputS                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  FileOutputS()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              顧客情報ファイル作成処理                                      */
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
    public int FileOutputS() {

//  int           rtn_cd;                       /* 関数戻り値                 */                                  /* 2022/10/11 MCCM初版 DEL */
        StringDto wk_out_buff = new StringDto(4096);            /* 出力情報格納用             */
        StringDto wk_buff = new StringDto(256 + 1);               /* 出力情報一時格納用         */

        /* 初期化 */
//  rtn_cd = C_const_OK;                                                                                          /* 2022/10/11 MCCM初版 DEL */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("FileOutputS処理");
            /*---------------------------------------------------------------------*/
        }
        /*--------------------------*/
        /* 顧客情報ファイルの編集 */
        /*--------------------------*/
        memset(wk_out_buff, 0x00, sizeof(wk_out_buff));
        memset(wk_buff, 0x00, sizeof(wk_buff));
        /* 各項目の編集 */
        /* 1   顧客番号             */
        strncpy(wk_out_buff, gh_kokyaku_no, sizeof(gh_kokyaku_no));

        /* 2   会員番号             */
        sprintf(wk_buff, ",%s", gh_kaiin_no);
        strcat(wk_out_buff, wk_buff);

        /* 3   入会店舗             */

        /* 2022/10/11 MCCM初版 ADD START */
        if ((tsrypnt_t.nyukai_tenpo.intVal() == 0) || (tsrypnt_t.nyukai_tenpo.intVal() < 100000) || (tsrypnt_t.nyukai_tenpo.intVal() > 999999)) {
            tsrypnt_t.nyukai_tenpo.arr = 999030;
        }
        /* 2022/10/11 MCCM初版 ADD END */
        sprintf(wk_buff, ",%d", tsrypnt_t.nyukai_tenpo);
        strcat(wk_out_buff, wk_buff);

        /* 改行 */
        strcat(wk_out_buff, "\n");

        /*--------------------------*/
        /* 顧客情報ファイルの出力   */
        /*--------------------------*/
        fwrite(wk_out_buff, strlen(wk_out_buff), 1, fp_out);
        if (ferror(fp_out) != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputS *** fwrite NG rtn=[%d]\n", ferror(fp_out));
                /*-------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fwrite（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        fflush(fp_out);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** FileOutputS *** fwrite=[%s]\n", fl_name);
            /*-------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("顧客番号  [%s]\n", gh_kokyaku_no);
            C_DbgMsg("会員番号  [%s]\n", gh_kaiin_no);
            C_DbgMsg("入会店舗  [%d]\n", tsrypnt_t.nyukai_tenpo);
            /*---------------------------------------------------------------------*/
            C_DbgMsg(" 顧客情報ファイル出力バッファ [%s]\n", wk_out_buff);
            /*---------------------------------------------------------------------*/
        }
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("FileOutputS処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----FileOutputS Bottom-----------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： FileOutputM                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  FileOutputM()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              顧客情報ファイル作成処理                                      */
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
    public int FileOutputM() {

        int rtn_cd;                     /* 関数戻り値                   */
        StringDto wk_out_buff = new StringDto(4096);         /* 出力情報格納用               */
        StringDto wk_buff = new StringDto(256 + 1);            /* 出力情報一時格納用           */
        StringDto utf8_buf = new StringDto(4069);            /* UTF8文字列格納領域           */
        IntegerDto sjis_len = new IntegerDto();                   /* SJIS変換用                   */
        StringDto sjis_buf = new StringDto(1024);            /* SJIS文字列格納領域           */
        StringDto in_buf = new StringDto(512);               /* 取得文字列格納領域           */
        StringDto full_buf = new StringDto(512);             /* 全角変換文字列格納領域       */
        StringDto edit_buf = new StringDto(512);             /* 編集用文字列格納領域         */
        /* 2022/10/11 MCCM初版 ADD START */
        StringDto prefectures = new StringDto(128);          /* 都道府県名                   */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数ステータス               */
        IntegerDto utf8_len = new IntegerDto();                   /* UTF8変換用                   */
        StringDto wk_out_buff_utf8 = new StringDto(4096);    /* 出力情報格納用utf8           */
        /* 2022/10/11 MCCM初版 ADD END */

        /* 初期化 */
        rtn_cd = C_const_OK;
        /* 2022/10/11 MCCM初版 ADD START */
        utf8_len.arr = 0;
        memset(wk_out_buff_utf8, 0x00, sizeof(wk_out_buff_utf8));
        /* 2022/10/11 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("FileOutputM処理");
            /*---------------------------------------------------------------------*/
        }
        /*--------------------------*/
        /* 顧客情報ファイルの編集   */
        /*--------------------------*/
        memset(wk_out_buff, 0x00, sizeof(wk_out_buff));
        memset(wk_buff, 0x00, sizeof(wk_buff));
        /* 各項目の編集 */

        /* 1   顧客番号             */
        strncpy(wk_out_buff, gh_kokyaku_no, sizeof(gh_kokyaku_no));

        /* 2   会員氏名（漢字） 半角→全角変換/UTF8→SJIS変換    */
        /* 半角→全角変換 */
        memset(in_buf, 0x00, sizeof(in_buf));
        memset(full_buf, 0x00, sizeof(full_buf));
        memcpy(in_buf, mmkoinf_t.kokyaku_mesho,
                strlen(mmkoinf_t.kokyaku_mesho));
        rtn_cd = C_ConvHalf2Full(in_buf, full_buf);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** C_ConvHalf2Full NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            return C_const_NG;
        }

        /* UTF8→SJIS変換 */
        sjis_len.arr = 0;
        memset(utf8_buf, 0x00, sizeof(utf8_buf));
        memset(sjis_buf, 0x00, sizeof(sjis_buf));

        strcpy(utf8_buf, full_buf);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            return C_const_NG;
        }

        /* 80バイト分出力 */
        memset(edit_buf, 0x00, sizeof(edit_buf));
        memcpy(edit_buf, sjis_buf, 80);
        sprintf(wk_buff, ",%s", edit_buf);
        strcat(wk_out_buff, wk_buff);

        /* 3   会員氏名（カナ） UTF8→SJIS変換    */
        sjis_len.arr = 0;
        memset(utf8_buf, 0x00, sizeof(utf8_buf));
        memset(sjis_buf, 0x00, sizeof(sjis_buf));

        strcpy(utf8_buf, mmkoinf_t.kokyaku_kana_mesho.strVal());
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            return C_const_NG;
        }
        sprintf(wk_buff, ",%s", sjis_buf);
        strcat(wk_out_buff, wk_buff);

        /* 4   郵便番号             */
        sprintf(wk_buff, ",%s", mmkozok_t.yubin_no);
        strcat(wk_out_buff, wk_buff);

        /* 2022/10/11 MCCM初版 ADD START */
        /*5 都道府県名取得 */
        memset(prefectures, 0x00, sizeof(prefectures));
        if (mmkozok_t.todofuken_cd.intVal() != 0) {
            rtn_cd = C_GetPrefectures(mmkozok_t.todofuken_cd.intVal(), prefectures, rtn_status);
            /* 都道府県名取得処理が異常の場合 */
            if (rtn_cd != C_const_OK) {

                APLOG_WT("903", 0, null, "C_GetPrefectures", rtn_cd,
                        rtn_status, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
        }
        sjis_len.arr = 0;
        memset(utf8_buf, 0x00, sizeof(utf8_buf));
        memset(sjis_buf, 0x00, sizeof(sjis_buf));

        strcpy(utf8_buf, prefectures);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            return C_const_NG;
        }
        sprintf(wk_buff, ",%s", sjis_buf);
        strcat(wk_out_buff, wk_buff);
        /* 2022/10/11 MCCM初版 ADD END */

        /* 2022/10/11 MCCM初版 DEl START */
        /* 5   住所１  UTF8→SJIS変換             */
//  sjis_len = 0;
//  memset( utf8_buf, 0x00, sizeof(utf8_buf) );
//  memset( sjis_buf, 0x00, sizeof(sjis_buf) );

//  strcpy( utf8_buf, (char *)mmkozok_t.jusho_1 );
//  rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, &sjis_len );
//  if ( rtn_cd != C_const_OK ) {
//if(DBG_LOG){
//      /*-------------------------------------------------------------*/
//      C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
//      /*-------------------------------------------------------------*/
//}
//      APLOG_WT( "903", 0, null, "C_ConvUT2SJ", rtn_cd,
//                0, 0, 0, 0 );
//      return C_const_NG;
//  }
//  sprintf( wk_buff, ",%s", sjis_buf );
//  strcat( wk_out_buff, wk_buff );
        /* 2022/10/11 MCCM初版 DEL END */

        /* 6   住所２  UTF8→SJIS変換             */
        sjis_len.arr = 0;
        memset(utf8_buf, 0x00, sizeof(utf8_buf));
        memset(sjis_buf, 0x00, sizeof(sjis_buf));

        strcpy(utf8_buf, mmkozok_t.jusho_2.strVal());
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            return C_const_NG;
        }
        sprintf(wk_buff, ",%s", sjis_buf);
        strcat(wk_out_buff, wk_buff);

        /* 2022/10/11 MCCM初版 DEL START */
        /* 7   住所３  UTF8→SJIS変換             */
//  sjis_len = 0;
//  memset( utf8_buf, 0x00, sizeof(utf8_buf) );
//  memset( sjis_buf, 0x00, sizeof(sjis_buf) );

//  strcpy( utf8_buf, (char *)mmkozok_t.jusho_3 );
//  rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, &sjis_len );
//   if ( rtn_cd != C_const_OK ) {
//if(DBG_LOG){
//      /*-------------------------------------------------------------*/
//      C_DbgMsg("*** FileOutputM *** C_ConvUT2SJ NG rtn=[%d]\n", rtn_cd);
//      /*-------------------------------------------------------------*/
//}
//      APLOG_WT( "903", 0, null, "C_ConvUT2SJ", rtn_cd,
//                0, 0, 0, 0 );
//      return C_const_NG;
//  }
//  sprintf( wk_buff, ",%s", sjis_buf );
//  strcat( wk_out_buff, wk_buff );
        /* 2022/10/11 MCCM初版 DEL END */

        /* 8   電話番号１           */
        sprintf(wk_buff, ",%s", mmkozok_t.denwa_no_1);
        strcat(wk_out_buff, wk_buff);

        /* 9   電話番号２           */
        sprintf(wk_buff, ",%s", mmkozok_t.denwa_no_2);
        strcat(wk_out_buff, wk_buff);

        /* 10   電話番号３          */
        sprintf(wk_buff, ",%s", mmkozok_t.denwa_no_3);
        strcat(wk_out_buff, wk_buff);

        /* 11   電話番号４          */
        sprintf(wk_buff, ",%s", mmkozok_t.denwa_no_4);
        strcat(wk_out_buff, wk_buff);

        /* 12   生年月日            */
        if (mmkoinf_t.tanjo_y.intVal() != 0 && mmkoinf_t.tanjo_m.intVal() != 0 &&
                mmkoinf_t.tanjo_d.intVal() != 0) {
            /* 年月日が全て設定されている場合 */
            sprintf(wk_buff, ",%04d%02d%02d",
                    mmkoinf_t.tanjo_y.intVal(), mmkoinf_t.tanjo_m.intVal(), mmkoinf_t.tanjo_d.intVal());
        } else {
            /* 年月日がいずれかが設定されていない場合 */
            strcpy(wk_buff, ",");
        }
        strcat(wk_out_buff, wk_buff);

        /* 13   性別:不明＝0        */
        sprintf(wk_buff, ",%d", mmkoinf_t.sebetsu.intVal());
        strcat(wk_out_buff, wk_buff);

        /* 14   最終更新日時        */
        if (mmkoinf_t.saishu_setai_ymd.intVal() != 0 && mmkoinf_t.saishu_setai_hms.intVal() != 0) {
            sprintf(wk_buff, ",%d/%02d/%02d %02d:%02d:%02d",
                    mmkoinf_t.saishu_setai_ymd.intVal() / 10000,
                    (mmkoinf_t.saishu_setai_ymd.intVal() % 10000) / 100,
                    mmkoinf_t.saishu_setai_ymd.intVal() % 100,
                    mmkoinf_t.saishu_setai_hms.intVal() / 10000,
                    (mmkoinf_t.saishu_setai_hms.intVal() % 10000) / 100,
                    mmkoinf_t.saishu_setai_hms.intVal() % 100);
            strcat(wk_out_buff, wk_buff);

        } else {

            /* 最終静態更新日=0場合 */
            if (mmkoinf_t.saishu_setai_ymd.intVal() != 0) {
                sprintf(wk_buff, ",%d/%02d/%02d ",
                        mmkoinf_t.saishu_setai_ymd.intVal() / 10000,
                        (mmkoinf_t.saishu_setai_ymd.intVal() % 10000) / 100,
                        mmkoinf_t.saishu_setai_ymd.intVal() % 100);
            } else {
                sprintf(wk_buff, ",%d/%02d/%02d ",
                        1900,
                        1,
                        1);
            }
            strcat(wk_out_buff, wk_buff);

            /* 最終静態更新時刻=0場合 */
            if (mmkoinf_t.saishu_setai_hms.intVal() != 0) {
                sprintf(wk_buff, "%02d:%02d:%02d",
                        mmkoinf_t.saishu_setai_hms.intVal() / 10000,
                        (mmkoinf_t.saishu_setai_hms.intVal() % 10000) / 100,
                        mmkoinf_t.saishu_setai_hms.intVal() % 100);
            } else {
                sprintf(wk_buff, "%02d:%02d:%02d",
                        0,
                        0,
                        0);
            }
            strcat(wk_out_buff, wk_buff);

            /* 最終更新日時が設定されていない場合 */
            /*strcpy( wk_buff, "," );*/
        }

        /* 15   退会年月日          */
        if (mmkigyo_t.taikai_ymd.intVal() != 0) {
            sprintf(wk_buff, ",%d", mmkigyo_t.taikai_ymd);
        } else {
            /* 退会年月日が設定されていない場合 */
            strcpy(wk_buff, ",");
        }
        strcat(wk_out_buff, wk_buff);

        /* 16   Ｅメール止め区分           */
        if (mmkigyo_t.email_tome_kbn.intVal() == 5000) {
            sprintf(wk_buff, ",%d", mmkigyo_t.email_tome_kbn.intVal());
            strcat(wk_out_buff, wk_buff);
        } else {
            sprintf(wk_buff, ",%d", 5031);
            strcat(wk_out_buff, wk_buff);
        }

        /* 17   ＤＭ止め区分           */
        if (mmkigyo_t.dm_tome_kbn.intVal() == 3000) {
            sprintf(wk_buff, ",%d", mmkigyo_t.dm_tome_kbn.intVal());
            strcat(wk_out_buff, wk_buff);
        } else {
            sprintf(wk_buff, ",%d", 3031);
            strcat(wk_out_buff, wk_buff);
        }

        /* 改行 */
        strcat(wk_out_buff, "\r\n");

        /*--------------------------*/
        /* 顧客情報ファイルの出力   */
        /*--------------------------*/
        fwrite(wk_out_buff, strlen(wk_out_buff), 1, fp_out);
        if (ferror(fp_out) != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** FileOutputM *** fwrite NG rtn=[%d]\n", ferror(fp_out));
                /*-------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fwrite（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        fflush(fp_out);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** FileOutputM *** fwrite=[%s]\n", fl_name);
            /*-------------------------------------------------------------*/
        }

        /* 2022/10/11 MCCM初版 ADD START */
        /* SJIS→UTF8変換 */
        rtn_cd = C_ConvSJ2UT(wk_out_buff, strlen(wk_out_buff), wk_out_buff_utf8, utf8_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }
        /* 2022/10/11 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("顧客番号          [%s]\n", gh_kokyaku_no);
            C_DbgMsg("会員氏名（漢字）  [%s]\n", mmkoinf_t.kokyaku_mesho);
            C_DbgMsg("会員氏名（カナ）  [%s]\n", mmkoinf_t.kokyaku_kana_mesho);
            C_DbgMsg("郵便番号          [%s]\n", mmkozok_t.yubin_no);
            C_DbgMsg("住所１            [%s]\n", prefectures);                                                                /* 2022/10/11 MCCM初版 MOD */
            C_DbgMsg("住所２            [%s]\n", mmkozok_t.jusho_2);
//  C_DbgMsg("住所３            [%s]\n", mmkozok_t.jusho_3 );                                                          /* 2022/10/11 MCCM初版 DEL */
            C_DbgMsg("電話番号１        [%s]\n", mmkozok_t.denwa_no_1);
            C_DbgMsg("電話番号２        [%s]\n", mmkozok_t.denwa_no_2);
            C_DbgMsg("電話番号３        [%s]\n", mmkozok_t.denwa_no_3);
            C_DbgMsg("電話番号４        [%s]\n", mmkozok_t.denwa_no_4);
            C_DbgMsg("生年月日年        [%04d]\n", mmkoinf_t.tanjo_y.intVal());
            C_DbgMsg("生年月日月        [%02d]\n", mmkoinf_t.tanjo_m.intVal());
            C_DbgMsg("生年月日日        [%02d]\n", mmkoinf_t.tanjo_d.intVal());
            C_DbgMsg("性別              [%d]\n", mmkoinf_t.sebetsu.intVal());
            C_DbgMsg("最終静態更新日    [%d]\n", mmkoinf_t.saishu_setai_ymd.longVal());
            C_DbgMsg("最終静態更新時刻  [%d]\n", mmkoinf_t.saishu_setai_hms.longVal());
            C_DbgMsg("退会年月日        [%d]\n", mmkigyo_t.taikai_ymd.longVal());
            C_DbgMsg("Ｅメール止め区分  [%d]\n", mmkigyo_t.email_tome_kbn.longVal());
            C_DbgMsg("ＤＭ止め区分      [%d]\n", mmkigyo_t.dm_tome_kbn.longVal());
            /*---------------------------------------------------------------------*/
            C_DbgMsg(" 顧客情報ファイル出力バッファ [%s]\n", wk_out_buff_utf8)
            ;                                        /* 2022/10/11 MCCM初版 DEL */
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
    /*  関数名 ： cmBTecciB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcrcmB_Cchk_Arg( char *Arg_in )                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*              １）重複チェック                                              */
    /*              ２）桁数チェック                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：引数値                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_Chk_Arg処理");
            C_DbgMsg("*** cmBTecciB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        if (memcmp(Arg_in, DEF_ARG_O, 2) == 0) {        /* -o出力ファイルCHK      */
            if (chk_arg_o != DEF_OFF) {
                sprintf(chg_format_buf, "-o 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_o = DEF_ON;

            if (strlen(Arg_in) < 3) {                 /* 桁数チェック           */
                sprintf(chg_format_buf, "-o 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (memcmp(Arg_in, DEF_ARG_D, 5) == 0) {        /* -date出力ファイルCHK   */
            if (chk_arg_d != DEF_OFF) {
                sprintf(chg_format_buf, "-date 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_d = DEF_ON;

            if (strlen(Arg_in) != 13) {               /* 桁数チェック           */
                sprintf(chg_format_buf, "-date 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTecciB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecciB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTecciB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              顧客情報ファイルを追加書き込みモードでオープンする。          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int       fl_flag      ：1:制度側Open / 2:管理側Open                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_OpenFile(int fl_flag) {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_OpenFile処理");
            /*------------------------------------------------------------*/
        }

        /* ファイル名作成、オープン */
        memset(fl_name, 0x00, sizeof(fl_name));
        memset(fl_name2, 0x00, sizeof(fl_name2));
        sprintf(fl_name, "%s/%s_%d", out_file_dir, arg_o_Value, fl_flag);
        sprintf(fl_name2, "%s_%d", arg_o_Value, fl_flag);

        if ((fp_out = fopen(fl_name.arr, FileOpenType.w)).fd == C_const_NG) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTecciB_OpenFile *** 顧客情報ファイルオープンERR%s\n",
                        "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTecciB_OpenFile *** 顧客情報ファイル%s\n", fl_name);
            C_DbgEnd("cmBTecciB_OpenFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecciB_Make_WrkTBL                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTecciB_Make_WrkTBL()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ワークテーブルを作成する                                      */
    /*              抽出対象の顧客番号を取得しワークテーブル（制度・管理）に      */
    /*              登録する                                                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecciB_Make_WrkTBL() {
        StringDto wk_sqlbuf = new StringDto(12288);            /* ＳＱＬ文編集用             */
        StringDto wk_buf = new StringDto(512);                 /* バインド変数用             */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTecciB_Make_WrkTBL処理");
            /*------------------------------------------------------------*/
        }

        /* 顧客番号をワークテーブルに設定 */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        strcpy(wk_sqlbuf, "insert into WM顧客番号 ( ");

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf,
                " SELECT /*+ INDEX(MC IXMMCSTINF04) */ MC.顧客番号 from MM顧客情報 MC, MM顧客企業別属性情報 MK");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        sprintf(wk_buf, " where MC.最終静態更新日>=%d", ghi_this_date);
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and MC.最終更新プログラムＩＤ<>'cmBTecenB'");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        sprintf(wk_buf, " and MK.企業コード=%d", C_KIGYO_CD_EC);
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and MC.顧客番号=MK.顧客番号");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and exists (select 会員番号 from MSカード情報 where 顧客番号 = MC.顧客番号)");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " UNION SELECT /*+ INDEX(MK IXMMCSTKAT00) */ 顧客番号 from MM顧客企業別属性情報 MK");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        sprintf(wk_buf, " where 最終更新日>=%d", ghi_this_date);
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        sprintf(wk_buf, " and 企業コード=%d", C_KIGYO_CD_EC);
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and 最終更新プログラムＩＤ<>'cmBTecenB'");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and exists (select 会員番号 from MSカード情報 where 顧客番号 = MK.顧客番号)");
        strcat(wk_sqlbuf, wk_buf);

        memset(wk_buf, 0x00, sizeof(wk_buf));
        strcpy(wk_buf, " and exists (select 顧客番号 from MM顧客情報 where 顧客番号 = MK.顧客番号 and 最終静態更新日<>0)");
        strcat(wk_sqlbuf, wk_buf);

        strcat(wk_sqlbuf, " )");

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTecciB_Make_WrkTBL *** SQL=%s\n", wk_sqlbuf);
            /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */

        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf,
                    0, 0, 0, 0);
            return C_const_NG;
        }

        /* 動的ＳＱＬ文実行 */
//        EXEC SQL EXECUTE sql_wrk1;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf,
                    0, 0, 0, 0);
            return C_const_NG;
        }

        /* 非会員を除外 */
        memset(gh_kokyaku_no, 0x00, sizeof(gh_kokyaku_no));
        sprintf(gh_kokyaku_no, "%d", C_KAIIN_NO_HIKAIIN);

//        EXEC SQL DELETE WM顧客番号 WHERE 顧客番号 = :gh_kokyaku_no OR 顧客番号 = 0;
        sqlca.sql.arr = "DELETE FROM WM顧客番号 WHERE 顧客番号 = " + gh_kokyaku_no + " OR 顧客番号 = 0";
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            APLOG_WT("904", 0, null, "DELETE", sqlca.sqlcode,
                    "WM顧客番号の非会員の削除に失敗しました", 0, 0, 0);
            return C_const_NG;
        }
        sqlca.sql.arr = "INSERT INTO WS顧客番号 SELECT * FROM WM顧客番号";
        /* ワークテーブル作成 */
//        EXEC SQL INSERT INTO WS顧客番号 @CMSD SELECT *FROM WM顧客番号;
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "WS顧客番号のINSERTに失敗しました", 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTecciB_Make_WrkTBL処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

}
