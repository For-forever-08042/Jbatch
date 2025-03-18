package jp.co.mcc.nttdata.batch.business.service.cmBTnnrgB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTnnrgB.dto.CmBTnnrgBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 翌年年別情報初期登録処理（cmBTnnrgB）
 *
 *   【処理概要】
 *       バッチ処理日付前日を基準としお買い上げ等あった顧客に対して、
 *       翌年年別情報初期登録処理を行う。
 *       ・翌年年別情報初期登録処理
 *          翌年の年別情報に該当レコードがない顧客に対して、年別情報の
 *          初期情報の登録を行う。
 *
 *   【引数説明】
 *     -c更新最大件数    :（必須）更新最大件数の設定
 *     -DEBUG(-debug)    :  デバッグモードでの実行（省略可）
 *                        （トレース出力機能が有効）
 *
 *   【戻り値】
 *       10     ： 正常
 *       99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 : 2014/07/07 SSI.吉田  ：初版
 *     30.00 : 2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 *                                    (顧客データロック処理内容更新のため)
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTnnrgBServiceImpl extends CmABfuncLServiceImpl implements CmBTnnrgBService {

    boolean DBG_LOG = true;                    /* デバッグメッセージ出力     */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTnnrgBDto cmBTnnrgBDto = new CmBTnnrgBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;                      /* OFF                             */
    int DEF_ON = 1;                      /* ON                              */
    String DEF_ARG_C = "-c";                   /* 最大取得件数                    */
    String DEF_DEBUG = "-DEBUG";               /* デバッグスイッチ                */
    String DEF_debug = "-debug";               /* デバッグスイッチ                */

    String DEF_DB_SD = "SD";                   /* デフォルトDB区分                */
    String PG_NAME = "翌年年別情報初期登録"; /* プログラム名称                  */
    int LOG_CNT = 50000;                  /* ログ出力件数                    */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int g_batch_ymd;          /* バッチ処理日付                         */
    int g_batch_prev_ymd;     /* バッチ処理日付前日                     */
    int g_batch_prev_y;       /* バッチ処理年                           */
    int g_batch_prev_y_next;  /* バッチ処理翌年                         */
    int input_data_cnt_yr;    /* 入力データ件数(翌年年別登録)           */
    int tsyp_data_cnt;        /* TSポイント年別情報登録件数合計         */
    long g_max_data_count;     /* 更新最大件数                           */
    int g_batch_secno;        /* バッチ処理実行シーケンス番号           */
    int chk_arg_c;            /* 引数-cチェック用                       */
    StringDto arg_c_Value = new StringDto(256);     /* 引数c設定値                            */
    StringDto g_uid = new StringDto(15 + 1);          /* 顧客番号                               */
    StringDto g_kino_id = new StringDto(7);         /* 機能ＩＤ                               */
    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット           */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /** APログ用                 **/


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
        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        int rtn_cd;                /* 関数戻り値                       */
        IntegerDto rtn_status = new IntegerDto();            /* 関数ステータス                   */
        int arg_cnt;               /* 引数チェック用カウンタ           */
        StringDto arg_Work1 = new StringDto(256);        /* Work Buffer1                     */
        StringDto bat_date = new StringDto(9);           /* 取得用 バッチ処理日付            */
        StringDto bat_date_prev = new StringDto(9);      /* 取得用 バッチ処理日付前日        */
        StringDto wk_char = new StringDto(9);            /* 編集領域                         */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*  プログラム名取得処理呼び出し       */
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  バッチデバッグ開始処理呼び出し    */
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("main処理");
            /*---------------------------------------------------------------------*/
        }

        /*  開始メッセージ                     */
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/

        /** 変数初期化                **/
        memset(bat_date, 0x00, sizeof(bat_date));
        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));
        chk_arg_c = DEF_OFF;
        memset(arg_c_Value, 0x00, sizeof(arg_c_Value));
        memset(cmBTnnrgBDto.h_kino_id, 0x00, sizeof(cmBTnnrgBDto.h_kino_id));
        memset(g_kino_id, 0x00, sizeof(g_kino_id));

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
            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;

            } else if (0 == memcmp(arg_Work1, DEF_ARG_C, 2)) {  /* -cの場合            */
                rtn_cd = cmBTnnrgB_Chk_Arg(arg_Work1);       /* パラメータCHK       */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_c_Value, arg_Work1.substring(2));
                } else {
                    rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else {                                            /* 定義外パラメータ   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /* 必須パラメータ未指定チェック */
        if (chk_arg_c == DEF_OFF) {
            sprintf(chg_format_buf, "-c 引数の値が不正です");
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了                 */
            return exit(C_const_APNG);
        }

        /* グローバル変数(long)にセット */
        g_max_data_count = atol(arg_c_Value);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("引数の値(-c)     =[%d]\n", g_max_data_count);
            /*-------------------------------------------------------------*/
        }

        /* 機能ＩＤをセット */
        strcpy(cmBTnnrgBDto.h_kino_id, "NNRG");
        strcpy(g_kino_id, "NNRG");

        /*-------------------------------------*/
        /*  DBコネクト処理呼び出し             */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_OraDBConnect", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  バッチ処理日取得処理呼び出し       */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
            /*-------------------------------------------------------------*/
        }
        /* バッチ処理日付取得 */
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }
        /* バッチ処理日付前日取得 */
        rtn_cd = C_GetBatDate(-1, bat_date_prev, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日前日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日前日取得NG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_date);
            C_DbgMsg("*** main *** バッチ処理前日取得OK [%s]\n", bat_date_prev);
            /*---------------------------------------------*/
        }

        /*  バッチ処理日付         */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date, 8);
        g_batch_ymd = atoi(wk_char);

        /*  バッチ処理日付前日     */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev, 8);
        g_batch_prev_ymd = atoi(wk_char);

        /*  バッチ処理年       */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev, 4);
        g_batch_prev_y = atoi(wk_char);

        /*  バッチ処理翌年     */
        g_batch_prev_y_next = g_batch_prev_y + 1;

        /*-------------------------------------*/
        /*  処理件数領域を初期化               */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理件数領域を初期化%s\n", "");
            /*-------------------------------------------------------------*/
        }
        input_data_cnt_yr = 0;
        tsyp_data_cnt = 0;

        /*-------------------------------------*/
        /*  主処理                             */
        /*-------------------------------------*/
        rtn_cd = cmBTnnrgB_main();
        if (rtn_cd != C_const_OK) {
            /* APログ出力用設定 */
            memset(out_format_buf, 0x00, sizeof(out_format_buf));
            sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y);
            /* APLOG出力 */
            APLOG_WT("106", 0, null, out_format_buf,
                    input_data_cnt_yr, 0, 0, 0, 0);
            /* APログ出力用設定 */
            memset(out_format_buf, 0x00, sizeof(out_format_buf));
            sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y_next);
            /* APLOG出力 */
            APLOG_WT("107", 0, null, out_format_buf,
                    tsyp_data_cnt, 0, 0, 0, 0);

            APLOG_WT("912", 0, null,
                    "翌年年別情報初期登録処理に失敗しました", 0, 0, 0, 0, 0);

            /*  ロールバック処理呼び出し     */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  終了処理                           */
        /*-------------------------------------*/

        /* APログ出力用設定 */
        memset(out_format_buf, 0x00, sizeof(out_format_buf));
        sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y);
        /* APLOG出力 */
        APLOG_WT("106", 0, null, out_format_buf,
                input_data_cnt_yr, 0, 0, 0, 0);
        /* APログ出力用設定 */
        memset(out_format_buf, 0x00, sizeof(out_format_buf));
        sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y_next);
        /* APLOG出力 */
        APLOG_WT("107", 0, null, out_format_buf,
                tsyp_data_cnt, 0, 0, 0, 0);

        /* コミット、開放する */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("main処理", C_const_APOK, 0, 0);
            /*---------------------------------------------*/
        }

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);
        /*-----main Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnnrgB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTnnrgB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               翌年年別情報初期登録主処理                                   */
    /*  【概要】                                                                  */
    /*               翌年年別情報初期登録処理を行う。                             */
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
    public int cmBTnnrgB_main() {
        int rtn_cd;                      /* 関数戻り値                    */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("翌年年別情報初期登録");
            /*---------------------------------------------------------------------*/
        }
        /* バッチ処理実行シーケンス番号取得処理 */
        rtn_cd = GetBatchSeqno();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------*/
                C_DbgMsg("*** cmBTnnrgB_main *** バッチ処理実行シーケンス番号取得エラー%s\n", "");
                C_DbgEnd("翌年年別情報初期登録主処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 翌年年別情報初期データ登録処理 */
        rtn_cd = YokunenNenbetsuRegist();
        if (rtn_cd != C_const_OK) {

            if (DBG_LOG) {
                /*--------------------------------------------------------*/
                C_DbgMsg("*** cmBTnnrgB_main *** 翌年年別情報初期登録エラー%s\n", "");
                C_DbgEnd("翌年年別情報初期登録主処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* バッチ処理実行シーケンス番号更新処理 */
        rtn_cd = UpdateBatchSeqno();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------*/
                C_DbgMsg("*** cmBTnnrgB_main *** バッチ処理実行シーケンス番号更新エラー%s\n", "");
                C_DbgEnd("翌年年別情報初期登録主処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("翌年年別情報初期登録", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----cmBTnnrgB_main Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnnrgB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTnnrgB_Chk_Arg( char *Arg_in )                             */
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
    @Override
    public int cmBTnnrgB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTnnrgB_Chk_Arg処理");
            C_DbgMsg("*** cmBTnnrgB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        if (0 == memcmp(Arg_in, DEF_ARG_C, 2)) {        /* -c更新最大件数         */
            if (chk_arg_c != DEF_OFF) {
                sprintf(chg_format_buf, "-c 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_c = DEF_ON;

            if (strlen(Arg_in) < 3) {                 /* 桁数チェック           */
                sprintf(chg_format_buf, "-c 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTnnrgB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetBatchSeqno                                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetBatchSeqno()                                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      翌年年別情報初期登録対象顧客検索対象日付取得処理                      */
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
    @Override
    public int GetBatchSeqno() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("翌年年別情報初期登録対象顧客検索対象日付取得処理");
            /*---------------------------------------------------------------------*/
        }
        /* シーケンス番号取得処理 */
        /*
        EXEC SQL SELECT シーケンス番号
        INTO   :h_batch_secno
        FROM   WSバッチ処理実行管理
        WHERE  機能ＩＤ = :h_kino_id;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT シーケンス番号  FROM   WSバッチ処理実行管理  WHERE  機能ＩＤ = '" + cmBTnnrgBDto.h_kino_id.strVal() + "'";
        sqlca.sql = workSql;
        sqlca.restAndExecute();
        sqlca.fetchInto(cmBTnnrgBDto.h_batch_secno);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** GetSeqno *** SELECT結果 = %d\n", sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "WSバッチ処理実行管理", g_kino_id, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("翌年年別情報初期登録対象顧客検索対象日付取得処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }
        /* データ無しエラーの場合、シーケンス番号を 19000101 とする */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            cmBTnnrgBDto.h_batch_secno.arr = 19000101;
        }

        g_batch_secno = cmBTnnrgBDto.h_batch_secno.intVal();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** GetSeqno *** 取得 シーケンス番号 = [%d]\n", cmBTnnrgBDto.h_batch_secno);
            C_DbgEnd("翌年年別情報初期登録対象顧客検索対象日付取得処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----GetBatchSeqno Bottom---------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateBatchSeqno                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateBatchSeqno()                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      翌年年別情報初期登録対象顧客検索対象日付更新処理                      */
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
    @Override
    public int UpdateBatchSeqno() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("翌年年別情報初期登録対象顧客検索対象日付更新処理");
            C_DbgMsg("*** UpdateBatchSeqno *** 更新 シーケンス番号 = [%d]\n", cmBTnnrgBDto.h_batch_secno);
            /*---------------------------------------------------------------------*/
        }

        /* UPDATEする */
        /*
        EXEC SQL UPDATE WSバッチ処理実行管理
        SET    シーケンス番号 = :h_batch_secno
        WHERE  機能ＩＤ       = :h_kino_id;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "UPDATE WSバッチ処理実行管理" +
                "        SET    シーケンス番号 = '" + cmBTnnrgBDto.h_batch_secno.strVal() + "'" +
                "        WHERE  機能ＩＤ       = '" + cmBTnnrgBDto.h_kino_id.strVal() + "'";
        sqlca.sql = workSql;
        sqlca.restAndExecute();

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateBatchSeqno *** UPDATE結果 = %d\n", sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "WSバッチ処理実行管理", g_kino_id, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("翌年年別情報初期登録対象顧客検索対象日付更新処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 未存在の場合はINSERTする */
            /*
            EXEC SQL INSERT INTO WSバッチ処理実行管理
                    (シーケンス番号, 機能ＩＤ)
            VALUES
                    (:h_batch_secno, :h_kino_id);
            */
            workSql = new StringDto();
            workSql.arr = "INSERT INTO WSバッチ処理実行管理\n" +
                    "                    (シーケンス番号, 機能ＩＤ)\n" +
                    "            VALUES\n" +
                    "                    (?, ?)";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTnnrgBDto.h_batch_secno, cmBTnnrgBDto.h_kino_id);

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateBatchSeqno *** INSERT結果 = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                        "WSバッチ処理実行管理", g_kino_id, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("翌年年別情報初期登録対象顧客検索対象日付更新処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                /* 処理を終了する */
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("翌年年別情報初期登録対象顧客検索対象日付更新処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();

        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateBatchSeqno Bottom------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： YokunenNenbetsuRegist                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  YokunenNenbetsuRegist()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               翌年年別情報初期登録                                         */
    /*  【概要】                                                                  */
    /*               翌年年別情報に初期データの登録を行う。                       */
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
    public int YokunenNenbetsuRegist() {
        int rtn_cd = rtn_cd();                      /* 関数戻り値
         */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);   /* 動的SQLバッファ               */
        int lock_sbt;                    /* ロック種別                    */
        StringDto wk_lock = new StringDto(2);                  /* ロック種別                    */
        IntegerDto rtn_status = new IntegerDto();                  /* 関数ステータス                */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("翌年年別情報初期登録");
            /*---------------------------------------------------------------------*/
        }
        memset(wk_sql, 0x00, sizeof(wk_sql));
        sprintf(wk_sql,
                "SELECT " +
                        "    T2.顧客番号 " +
                        "   ,T2.最終更新日 " +
                        "FROM " +
                        "( " +
                        "    SELECT " +
                        "        to_char(T1.顧客番号, 'FM000000000000000') AS 顧客番号 " +
                        "       ,T1.最終更新日 " +
                        "      FROM TSポイント年別情報%d T1 " +
                        "     WHERE T1.最終更新日 >= %d " +
                        "       AND not exists ( " +
                        "                  SELECT 顧客番号 " +
                        "                  FROM   TSポイント年別情報%d T2 " +
                        "                  WHERE  T2.顧客番号 = T1.顧客番号 " +
                        "                    AND  T2.年 = %d " +
                        "                 ) " +
                        "    ORDER BY T1.最終更新日 " +
                        "LIMIT %d ) T2 "
                , g_batch_prev_y        /* バッチ更新日当年 */
                , g_batch_secno         /* シーケンス番号   */
                , g_batch_prev_y_next   /* バッチ更新日翌年 */
                , g_batch_prev_y_next   /* バッチ更新日翌年 */
                , g_max_data_count      /* 更新最大件数     */
        );

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** YokunenNenbetsuRegist*** SQL %s\n", wk_sql);
            /*--------------------------------------------------------------------*/
        }

        /* HOST変数に設定 */
        memset(cmBTnnrgBDto.str_sql, 0x00, sizeof(cmBTnnrgBDto.str_sql));
        strcpy(cmBTnnrgBDto.str_sql, wk_sql);

        /* 動的SQL文の解析 */
        // EXEC SQL PREPARE sql_stat2 from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** YokunenNenbetsuRegist*** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgEnd("翌年年別情報初期登録", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("902", 0, null, sqlca.sqlcode, cmBTnnrgBDto.str_sql, 0, 0, 0, 0);

            /* 処理をNGで終了する */
            return (C_const_NG);
        }

        /* カーソル定義 */
        // EXEC SQL DECLARE DMRG_TSPY02 cursor for sql_stat2;
        sqlca.declare();
        /* カーソルオープン */
        // EXEC SQL OPEN DMRG_TSPY02;
        sqlca.open();

        if (sqlca.sqlcode != C_const_Ora_OK) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** YokunenNenbetsuRegist *** カーソルOEPNエラー = %d\n", sqlca.sqlcode);
                C_DbgEnd("翌年年別情報初期登録", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode,
                    "TSポイント年別情報", cmBTnnrgBDto.str_sql, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /*-------------------------------------*/
        /*  翌年年別初期登録候補顧客ループ     */
        /*-------------------------------------*/
        while (true) {

            /* 初期化 */
            memset(cmBTnnrgBDto.h_uid, 0x00, sizeof(cmBTnnrgBDto.h_uid));

            /*
            EXEC SQL FETCH DMRG_TSPY02
            INTO :h_uid,
             :h_last_upd_day;
            */
            sqlca.fetch();
            sqlca.recData(cmBTnnrgBDto.h_uid, cmBTnnrgBDto.h_last_upd_day);

            /* データ無し以外エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** YokunenNenbetsuRegist *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("翌年年別情報初期登録", C_const_NG, 0, 0);
                    /*-------------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "TSポイント年別情報", "", 0, 0);

                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPY02;
                sqlca.curse_close();

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /*----------------------------------*/
            /*----ポイント・顧客ロック----------*/
            /*----------------------------------*/
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** YokunenNenbetsuRegist *** ポイント・顧客ロック  顧客番号 =[%s]\n", cmBTnnrgBDto.h_uid);
                /*-------------------------------------------------------------*/
            }
            rtn_status.arr = 0;

            /* ロック種別は、1(更新) */
            lock_sbt = 1;
            memset(wk_lock, 0x00, sizeof(wk_lock));
            sprintf(wk_lock, "%d", lock_sbt);
            memset(g_uid, 0x00, sizeof(g_uid));
            strcpy(g_uid, cmBTnnrgBDto.h_uid.strVal());
            rtn_cd = C_KdataLock(g_uid, wk_lock.arr, rtn_status);
            if (rtn_cd != C_const_OK) {
                /* 顧客ロック異常終了 */
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** YokunenNenbetsuRegist *** ポイント・顧客ロック status= %d\n", rtn_status);
                    /*-------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 処理件数をカウントアップ */
            input_data_cnt_yr++;

            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** YokunenNenbetsuRegist *** [%d]レコード目の処理開始\n",
                        input_data_cnt_yr);
                /*----------------------------------------------------------------*/
            }

            /* TSポイント年別情報登録 */
            rtn_cd = InsertYokunenNenbetsu(g_batch_prev_y_next);

            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** YokunenNenbetsuRegist *** TSポイント年別情報登録エラー%s\n", "");
                    C_DbgEnd("翌年年別情報初期登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPY02;
                sqlca.curse_close();

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* TSポイント年別情報登録件数カウントアップ */
            tsyp_data_cnt++;

            /* コミットする */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* 処理件数（途中経過）を出力 */
            if ((input_data_cnt_yr % LOG_CNT) == 0) {

                /* APログ出力用設定 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y);
                /* APLOG出力 */
                APLOG_WT("106", 0, null, out_format_buf,
                        input_data_cnt_yr, 0, 0, 0, 0);
                /* APログ出力用設定 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "TSポイント年別情報%d", g_batch_prev_y_next);
                /* APLOG出力 */
                APLOG_WT("107", 0, null, out_format_buf,
                        tsyp_data_cnt, 0, 0, 0, 0);
            }

            /* 最終更新日を更新 */
            cmBTnnrgBDto.h_batch_secno.arr = cmBTnnrgBDto.h_last_upd_day.arr;
        }
        /* カーソルクローズ */
        // EXEC SQL CLOSE DMRG_TSPY02;
        sqlca.curse_close();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("翌年年別情報初期登録", rtn_cd, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
        /*-----YokunenNenbetsuRegist Bottom-------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertYokunenNenbetsu                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertYokunenNenbetsu(int regist_year)                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               翌年年別情報登録処理                                         */
    /*  【概要】                                                                  */
    /*               TSポイント年別情報に初期データを登録する。                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int                regist_year     : 登録対象年                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int InsertYokunenNenbetsu(int regist_year) {
        StringDto aplog_table_name = new StringDto(256);     /* テーブル名                */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("翌年年別情報登録処理");
            /*---------------------------------------------------------------------*/
        }

        /* テーブル名セット */
        memset(aplog_table_name, 0x00, sizeof(aplog_table_name));
        sprintf(aplog_table_name, "TSポイント年別情報%d", regist_year);

        /* 初期化 */
        memset(cmBTnnrgBDto.ts_year_point_next, 0x00, sizeof(cmBTnnrgBDto.ts_year_point_next));
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTnnrgBDto.str_sql, 0x00, sizeof(cmBTnnrgBDto.str_sql));

        /* 翌年年別情報構造体編集 */
        cmBTnnrgBDto.ts_year_point_next.year.arr = regist_year; /* 年                         */
        /* 顧客番号                   */
        memcpy(cmBTnnrgBDto.ts_year_point_next.kokyaku_no, cmBTnnrgBDto.h_uid, sizeof(cmBTnnrgBDto.h_uid));
        cmBTnnrgBDto.ts_year_point_next.kokyaku_no.len = strlen(cmBTnnrgBDto.h_uid);
        cmBTnnrgBDto.ts_year_point_next.nenkan_fuyo_point.arr = 0; /* 年間付与ポイント           */
        cmBTnnrgBDto.ts_year_point_next.nenkan_riyo_point.arr = 0; /* 年間利用ポイント           */
        cmBTnnrgBDto.ts_year_point_next.nenkan_kihon_pritsu_taisho_point.arr = 0; /* 年間基本Ｐ率対象ポイント   */
        cmBTnnrgBDto.ts_year_point_next.nenkan_rankup_taisho_kingaku.arr = 0; /* 年間ランクＵＰ対象金額     */
        cmBTnnrgBDto.ts_year_point_next.nenkan_point_taisho_kingaku.arr = 0; /* 年間ポイント対象金額       */
        cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_kingaku.arr = 0; /* 年間買上額                 */
        cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_cnt.arr = 0; /* 年間買上回数               */
        cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_nissu.arr = 0; /* 年間買上日数               */
        cmBTnnrgBDto.ts_year_point_next.nenkan_kaimonoken_hakko_point.arr = 0; /* 年間買物券発行ポイント     */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_01.arr = 0; /* 月間付与ポイント０１       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_02.arr = 0; /* 月間付与ポイント０２       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_03.arr = 0; /* 月間付与ポイント０３       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_04.arr = 0; /* 月間付与ポイント０４       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_05.arr = 0; /* 月間付与ポイント０５       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_06.arr = 0; /* 月間付与ポイント０６       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_07.arr = 0; /* 月間付与ポイント０７       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_08.arr = 0; /* 月間付与ポイント０８       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_09.arr = 0; /* 月間付与ポイント０９       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_10.arr = 0; /* 月間付与ポイント１０       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_11.arr = 0; /* 月間付与ポイント１１       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_12.arr = 0; /* 月間付与ポイント１２       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_01.arr = 0; /* 月間利用ポイント０１       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_02.arr = 0; /* 月間利用ポイント０２       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_03.arr = 0; /* 月間利用ポイント０３       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_04.arr = 0; /* 月間利用ポイント０４       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_05.arr = 0; /* 月間利用ポイント０５       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_06.arr = 0; /* 月間利用ポイント０６       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_07.arr = 0; /* 月間利用ポイント０７       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_08.arr = 0; /* 月間利用ポイント０８       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_09.arr = 0; /* 月間利用ポイント０９       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_10.arr = 0; /* 月間利用ポイント１０       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_11.arr = 0; /* 月間利用ポイント１１       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_12.arr = 0; /* 月間利用ポイント１２       */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_01.arr = 0; /* 月間ランクＵＰ対象金額０１ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_02.arr = 0; /* 月間ランクＵＰ対象金額０２ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_03.arr = 0; /* 月間ランクＵＰ対象金額０３ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_04.arr = 0; /* 月間ランクＵＰ対象金額０４ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_05.arr = 0; /* 月間ランクＵＰ対象金額０５ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_06.arr = 0; /* 月間ランクＵＰ対象金額０６ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_07.arr = 0; /* 月間ランクＵＰ対象金額０７ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_08.arr = 0; /* 月間ランクＵＰ対象金額０８ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_09.arr = 0; /* 月間ランクＵＰ対象金額０９ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_10.arr = 0; /* 月間ランクＵＰ対象金額１０ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_11.arr = 0; /* 月間ランクＵＰ対象金額１１ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_12.arr = 0; /* 月間ランクＵＰ対象金額１２ */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_01.arr = 0; /* 月間買上回数０１           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_02.arr = 0; /* 月間買上回数０２           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_03.arr = 0; /* 月間買上回数０３           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_04.arr = 0; /* 月間買上回数０４           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_05.arr = 0; /* 月間買上回数０５           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_06.arr = 0; /* 月間買上回数０６           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_07.arr = 0; /* 月間買上回数０７           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_08.arr = 0; /* 月間買上回数０８           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_09.arr = 0; /* 月間買上回数０９           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_10.arr = 0; /* 月間買上回数１０           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_11.arr = 0; /* 月間買上回数１１           */
        cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_12.arr = 0; /* 月間買上回数１２           */
        cmBTnnrgBDto.ts_year_point_next.saishu_koshin_ymd.arr = 0; /* 最終更新日                 */
        /* 最終更新プログラムＩＤ     */
        strncpy(cmBTnnrgBDto.ts_year_point_next.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name));

        /* SQL文 */
        sprintf(wk_sql,
                "INSERT INTO TSポイント年別情報%d " +
                        "    ( " +
                        "         年, " +
                        "         顧客番号, " +
                        "         年間付与ポイント, " +
                        "         年間利用ポイント, " +
                        "         年間基本Ｐ率対象ポイント, " +
                        "         年間ランクＵＰ対象金額, " +
                        "         年間ポイント対象金額, " +
                        "         年間買上額, " +
                        "         年間買上回数, " +
                        "         年間買上日数, " +
                        "         年間買物券発行ポイント, " +
                        "         月間付与ポイント０１, " +
                        "         月間付与ポイント０２, " +
                        "         月間付与ポイント０３, " +
                        "         月間付与ポイント０４, " +
                        "         月間付与ポイント０５, " +
                        "         月間付与ポイント０６, " +
                        "         月間付与ポイント０７, " +
                        "         月間付与ポイント０８, " +
                        "         月間付与ポイント０９, " +
                        "         月間付与ポイント１０, " +
                        "         月間付与ポイント１１, " +
                        "         月間付与ポイント１２, " +
                        "         月間利用ポイント０１, " +
                        "         月間利用ポイント０２, " +
                        "         月間利用ポイント０３, " +
                        "         月間利用ポイント０４, " +
                        "         月間利用ポイント０５, " +
                        "         月間利用ポイント０６, " +
                        "         月間利用ポイント０７, " +
                        "         月間利用ポイント０８, " +
                        "         月間利用ポイント０９, " +
                        "         月間利用ポイント１０, " +
                        "         月間利用ポイント１１, " +
                        "         月間利用ポイント１２, " +
                        "         月間ランクＵＰ対象金額０１, " +
                        "         月間ランクＵＰ対象金額０２, " +
                        "         月間ランクＵＰ対象金額０３, " +
                        "         月間ランクＵＰ対象金額０４, " +
                        "         月間ランクＵＰ対象金額０５, " +
                        "         月間ランクＵＰ対象金額０６, " +
                        "         月間ランクＵＰ対象金額０７, " +
                        "         月間ランクＵＰ対象金額０８, " +
                        "         月間ランクＵＰ対象金額０９, " +
                        "         月間ランクＵＰ対象金額１０, " +
                        "         月間ランクＵＰ対象金額１１, " +
                        "         月間ランクＵＰ対象金額１２, " +
                        "         月間買上回数０１, " +
                        "         月間買上回数０２, " +
                        "         月間買上回数０３, " +
                        "         月間買上回数０４, " +
                        "         月間買上回数０５, " +
                        "         月間買上回数０６, " +
                        "         月間買上回数０７, " +
                        "         月間買上回数０８, " +
                        "         月間買上回数０９, " +
                        "         月間買上回数１０, " +
                        "         月間買上回数１１, " +
                        "         月間買上回数１２, " +
                        "         最終更新日, " +
                        "         最終更新日時, " +
                        "         最終更新プログラムＩＤ " +
                        "     ) " +
                        "   VALUES " +
                        "    ( " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         sysdate(), " +
                        "         ? " +
                        "     ) ", regist_year);

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTnnrgBDto.str_sql, wk_sql);

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_ins_tsyp from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat_ins_tsyp");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertYokunenNenbetsu *** SQL=[%s]\n", wk_sql);
                C_DbgMsg("*** InsertYokunenNenbetsu *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset(out_format_buf, 0x00, sizeof(out_format_buf));
            sprintf(out_format_buf, "顧客番号=[%s]", cmBTnnrgBDto.h_uid);

            /* APLOG出力 */
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    aplog_table_name, out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_ins_tsyp using
        :ts_year_point_next.year                             ,
                                :ts_year_point_next.kokyaku_no                       ,
                                :ts_year_point_next.nenkan_fuyo_point                ,
                                :ts_year_point_next.nenkan_riyo_point                ,
                                :ts_year_point_next.nenkan_kihon_pritsu_taisho_point ,
                                :ts_year_point_next.nenkan_rankup_taisho_kingaku     ,
                                :ts_year_point_next.nenkan_point_taisho_kingaku      ,
                                :ts_year_point_next.nenkan_kaiage_kingaku            ,
                                :ts_year_point_next.nenkan_kaiage_cnt                ,
                                :ts_year_point_next.nenkan_kaiage_nissu              ,
                                :ts_year_point_next.nenkan_kaimonoken_hakko_point    ,
                                :ts_year_point_next.gekkan_fuyo_point_01             ,
                                :ts_year_point_next.gekkan_fuyo_point_02             ,
                                :ts_year_point_next.gekkan_fuyo_point_03             ,
                                :ts_year_point_next.gekkan_fuyo_point_04             ,
                                :ts_year_point_next.gekkan_fuyo_point_05             ,
                                :ts_year_point_next.gekkan_fuyo_point_06             ,
                                :ts_year_point_next.gekkan_fuyo_point_07             ,
                                :ts_year_point_next.gekkan_fuyo_point_08             ,
                                :ts_year_point_next.gekkan_fuyo_point_09             ,
                                :ts_year_point_next.gekkan_fuyo_point_10             ,
                                :ts_year_point_next.gekkan_fuyo_point_11             ,
                                :ts_year_point_next.gekkan_fuyo_point_12             ,
                                :ts_year_point_next.gekkan_riyo_point_01             ,
                                :ts_year_point_next.gekkan_riyo_point_02             ,
                                :ts_year_point_next.gekkan_riyo_point_03             ,
                                :ts_year_point_next.gekkan_riyo_point_04             ,
                                :ts_year_point_next.gekkan_riyo_point_05             ,
                                :ts_year_point_next.gekkan_riyo_point_06             ,
                                :ts_year_point_next.gekkan_riyo_point_07             ,
                                :ts_year_point_next.gekkan_riyo_point_08             ,
                                :ts_year_point_next.gekkan_riyo_point_09             ,
                                :ts_year_point_next.gekkan_riyo_point_10             ,
                                :ts_year_point_next.gekkan_riyo_point_11             ,
                                :ts_year_point_next.gekkan_riyo_point_12             ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_01  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_02  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_03  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_04  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_05  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_06  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_07  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_08  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_09  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_10  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_11  ,
                                :ts_year_point_next.gekkan_rankup_taisho_kingaku_12  ,
                                :ts_year_point_next.gekkan_kaiage_cnt_01             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_02             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_03             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_04             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_05             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_06             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_07             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_08             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_09             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_10             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_11             ,
                                :ts_year_point_next.gekkan_kaiage_cnt_12             ,
                                :ts_year_point_next.saishu_koshin_ymd                ,
                                :ts_year_point_next.saishu_koshin_programid          ;
        */
        sqlca.restAndExecute(cmBTnnrgBDto.ts_year_point_next.year,
                cmBTnnrgBDto.ts_year_point_next.kokyaku_no,
                cmBTnnrgBDto.ts_year_point_next.nenkan_fuyo_point,
                cmBTnnrgBDto.ts_year_point_next.nenkan_riyo_point,
                cmBTnnrgBDto.ts_year_point_next.nenkan_kihon_pritsu_taisho_point,
                cmBTnnrgBDto.ts_year_point_next.nenkan_rankup_taisho_kingaku,
                cmBTnnrgBDto.ts_year_point_next.nenkan_point_taisho_kingaku,
                cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_kingaku,
                cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_cnt,
                cmBTnnrgBDto.ts_year_point_next.nenkan_kaiage_nissu,
                cmBTnnrgBDto.ts_year_point_next.nenkan_kaimonoken_hakko_point,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_01,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_02,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_03,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_04,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_05,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_06,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_07,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_08,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_09,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_10,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_11,
                cmBTnnrgBDto.ts_year_point_next.gekkan_fuyo_point_12,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_01,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_02,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_03,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_04,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_05,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_06,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_07,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_08,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_09,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_10,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_11,
                cmBTnnrgBDto.ts_year_point_next.gekkan_riyo_point_12,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_01,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_02,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_03,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_04,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_05,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_06,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_07,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_08,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_09,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_10,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_11,
                cmBTnnrgBDto.ts_year_point_next.gekkan_rankup_taisho_kingaku_12,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_01,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_02,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_03,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_04,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_05,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_06,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_07,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_08,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_09,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_10,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_11,
                cmBTnnrgBDto.ts_year_point_next.gekkan_kaiage_cnt_12,
                cmBTnnrgBDto.ts_year_point_next.saishu_koshin_ymd,
                cmBTnnrgBDto.ts_year_point_next.saishu_koshin_programid);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertYokunenNenbetsu *** TSポイント年別情報INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset(out_format_buf, 0x00, sizeof(out_format_buf));
            sprintf(out_format_buf, "顧客番号=[%s]", cmBTnnrgBDto.ts_year_point_next.kokyaku_no.arr);

            /* APLOG出力 */
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    aplog_table_name, out_format_buf, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("翌年年別情報登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("翌年年別情報登録処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertYokunenNenbetsu Bottom----------------------------------------------*/
    }

}
