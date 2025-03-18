package jp.co.mcc.nttdata.batch.business.service.cmBTcareB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *   プログラム名   ： カード統合洗替処理（cmBTcareB）
 *
 *   【処理概要】
 *       カード統合を行った会員の
 *         ・ポイント日別情報
 *         ・ポイント明細取引情報の顧客番号の洗い替えを行う
 *           ※対象は４８ヵ月前～当月（４９ヶ月間）とする
 *
 *   【引数説明】
 *  -DEBUG(-debug)    :  デバッグモードでの実行（省略可）
 *                       （トレース出力機能が有効）
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
 *      1.00 :   2012/11/16 SSI.越後谷：初版
 *      2.00 :   2013/05/01 SSI.本田  ：更新TBLの基準日をバッチ処理日に変更
 *      3.00 :   2013/08/31 SSI.武藤  ：洗替期間を４ヶ月から１６ヶ月に変更
 *     30.00 :   2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 *     40.00 :   2022/10/17 SSI.申    ：MCCM初版
 *     41.00 :   2023/09/08 SSI.小俣  ：IT-0120 既存バグ対応
 *     41.00 :   2023/09/15 SSI.テッテッアウン  ：MCCM仕様変更JK215
 *     51.00 :   2023/09/21 SSI.テッテッアウン  ：MCCM仕様変更JK206
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTcareBServiceImpl extends CmABfuncLServiceImpl implements CmBTcareBService {
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */

    boolean DBG_LOG = true;                    /* デバッグメッセージ出力     */
    /*----------------------------------------------------------------------------*/
    /*  独自include                                                               */
    /*----------------------------------------------------------------------------*/
//            #include    "C_aplcom1.h"              /* 共通関数                            */
//            #include    "BT_aplcom.h"              /* バッチＡＰ業務共通ヘッダファイル    */

    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL INCLUDE  sqlca.h;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;
//    char             str_sql[4096*2];                     /* 実行用SQL文字列        */
//
//    char             gh_uid[15+1]              ; /* 顧客番号                         */
//    char             gh_kyu_uid[15+1]          ; /* 旧顧客番号                       */
//    char             gh_tblname_ym[9]          ; /* 対象ＴＢＬ名年月取得用           */
//    /* 2023/09/15 MCCM仕様変更JK215 ADD START */
//    char             gh_tblname_y[9]           ; /* 対象ＴＢＬ名年取得用             */
//    /* 2023/09/15 MCCM仕様変更JK215 ADD END */
//    char             gh_bat_date[9]            ; /* ホスト変数(前日)                 */
//    int              gh_tgt                    ; /* ホスト変数(処理対象月の相対値)   */
//    unsigned int     gh_batch_prev_ymd         ; /* バッチ処理日付前日               */
//    char             gh_goopon_no[16+1]        ; /* ＧＯＯＰＯＮ番号                 */            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_kyu_goopon_no[16+1]    ; /* 旧ＧＯＯＰＯＮ番号               */            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_kaiin_no[18+1]         ; /* 会員番号                         */            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_kyu_kaiin_no[18+1]     ; /* 旧会員番号                       */            /* 2022/10/17 MCCM初版 ADD */
//    int              gh_araigae_flg            ; /* 洗替フラグ 1:統合洗替  2:解除洗替*/            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_saishu_koshin_programid[20 + 1];     /* 最終更新プログラムID */            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_table_name[128];    /* MSクーポン利用顧客情報nnnnnテーブル名 */            /* 2022/10/17 MCCM初版 ADD */
//    char             gh_tougou_kaiin_no[18+1]  ; /* 統合採用会員番号                 */            /* 2022/12/20 MCCM初版 ADD */

//    EXEC SQL END DECLARE SECTION;

    StringDto str_sql = new StringDto(4096 * 2);                     /* 実行用SQL文字列        */

    StringDto gh_uid = new StringDto(15 + 1); /* 顧客番号                         */
    StringDto gh_kyu_uid = new StringDto(15 + 1); /* 旧顧客番号                       */
    StringDto gh_tblname_ym = new StringDto(9); /* 対象ＴＢＬ名年月取得用           */
    /* 2023/09/15 MCCM仕様変更JK215 ADD START */
    StringDto gh_tblname_y = new StringDto(9); /* 対象ＴＢＬ名年取得用             */
    /* 2023/09/15 MCCM仕様変更JK215 ADD END */
    StringDto gh_bat_date = new StringDto(9); /* ホスト変数(前日)                 */
    int gh_tgt; /* ホスト変数(処理対象月の相対値)   */
    int gh_batch_prev_ymd; /* バッチ処理日付前日               */
    StringDto gh_goopon_no = new StringDto(16 + 1); /* ＧＯＯＰＯＮ番号                 */            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_kyu_goopon_no = new StringDto(16 + 1); /* 旧ＧＯＯＰＯＮ番号               */            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_kaiin_no = new StringDto(18 + 1); /* 会員番号                         */            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_kyu_kaiin_no = new StringDto(18 + 1); /* 旧会員番号                       */            /* 2022/10/17 MCCM初版 ADD */
    IntegerDto gh_araigae_flg = new IntegerDto(); /* 洗替フラグ 1:統合洗替  2:解除洗替*/            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_saishu_koshin_programid = new StringDto(20 + 1);     /* 最終更新プログラムID */            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_table_name = new StringDto(128);    /* MSクーポン利用顧客情報nnnnnテーブル名 */            /* 2022/10/17 MCCM初版 ADD */
    StringDto gh_tougou_kaiin_no = new StringDto(18 + 1); /* 統合採用会員番号                 */            /* 2022/12/20 MCCM初版 ADD */

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int FLG_NASHI = -1;            /* 無                                */
    int FLG_ARI = 0;            /* 有                                */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";         /* デバッグスイッチ                  */
    String DEF_debug = "-debug";         /* デバッグスイッチ                  */

    String DEF_DB_SD = "SD";         /* デフォルトDB区分                  */
    String PG_NAME = "カード統合洗替";  /* プログラム名称                    */
    // 2018.08.31 洗替期間を４ヶ月から１６ヶ月に変更    --------------------[START]
    //#define C_YEAR_MAX     4                 /* 処理対象年数 (メモリ持ちする件数) */             /* 洗替期間を１６ヶ月から４９ヶ月に変更 */
    int C_YEAR_MAX = 49;              /* 処理対象年数 (メモリ持ちする件数) */               /* 2023/06/05 MCCM初版 MOD */
    // 2018.08.31 洗替期間を４ヶ月から１６ヶ月に変更    --------------------[ END ]
    /* 2023/09/15 MCCM仕様変更JK215 ADD START */
    int C_YEAR_MAX2 = 5;               /* 処理対象年数 (メモリ持ちする件数) */
    /* 2023/09/15 MCCM仕様変更JK215 ADD END */
    /* 2023/09/21 MCCM仕様変更JK206 ADD START */
    int C_YEAR_MAX3 = 3;              /* 処理対象年数 (メモリ持ちする件数)  DM送付状態情報 */
    /* 2023/09/21 MCCM仕様変更JK206 ADD END */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/

    int[] tbl_name_ym = new int[C_YEAR_MAX]; /* 対象ＴＢＬ名年月                    */
    /* 2023/09/15 MCCM仕様変更JK215 ADD START */
    int[] tbl_name_y = new int[C_YEAR_MAX2]; /* 対象ＴＢＬ名年                      */
    /* 2023/09/15 MCCM仕様変更JK215 ADD END */
    /* 2023/09/21 MCCM仕様変更JK206 ADD START */
    int[] tbl_name_y2 = new int[C_YEAR_MAX3]; /* 対象ＴＢＬ名年                      */
    /* 2023/09/21 MCCM仕様変更JK206 ADD END */

    int input_data_cnt; /* 入力データ件数(HSカード変更情報)    */

    StringDto out_format_buf = new StringDto(4096 * 2);    /* APログフォーマット           */

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
        int rtn_cd; /* 関数戻り値                */
        IntegerDto rtn_status = new IntegerDto(); /* 関数ステータス            */
        int arg_chk; /* 引数の種類チェック結果    */
        int arg_cnt; /* 引数チェック用カウンタ    */
        StringDto arg_Work1 = new StringDto(256); /* Work Buffer1              */
        StringDto bat_date_prev = new StringDto(9); /* 取得用 バッチ処理日付前日 */
        StringDto bat_date = new StringDto(9); /* 取得用 バッチ処理日付     */
        StringDto wk_char = new StringDto(9); /* 編集領域                  */
        int wi_loop; /* LOOP Counter              */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*-------------------------------------*/
        /*  プログラム名取得処理呼び出し       */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  開始メッセージ                     */
        /*-------------------------------------*/
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* HOST変数にプログラム名をコピー */                                                                                 /* 2022/10/17 MCCM初版 ADD */
//        memset(gh_saishu_koshin_programid, 0x00, sizeof(gh_saishu_koshin_programid));                                        /* 2022/10/17 MCCM初版 ADD */
        memcpy(gh_saishu_koshin_programid, Cg_Program_Name, sizeof(Cg_Program_Name));                                        /* 2022/10/17 MCCM初版 ADD */

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理呼び出し     */
        /*-------------------------------------*/
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

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "");
            /*---------------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, 256);
            strcpy(arg_Work1, argv[arg_cnt]);
            arg_chk = FLG_ARI;
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = %s\n", arg_Work1);
                /*----------------------------------------------------------------*/
            }
            if (0 == strcmp(arg_Work1, DEF_DEBUG)) {
                continue;
            } else if (0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
            } else {
                /* 規定外パラメータ  */
                arg_chk = FLG_NASHI;
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = %d\n", arg_chk);
                /*-------------------------------------------------------------*/
            }
            /* 規定外パラメータ  */
            if (arg_chk != FLG_ARI) {
                sprintf(out_format_buf,
                        "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", C_const_APNG, 0, 0);
                    /*---------------------------------------------*/
                }
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
        } /*---bottom for( arg_cnt = 1 ; arg_cnt < argc ; arg_cnt++ )---*/

        /*-------------------------------------*/
        /*  DBコネクト処理呼び出し             */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
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
        /** 変数初期化                **/
        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));
        memset(bat_date, 0x00, sizeof(bat_date));

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
            C_DbgMsg("*** main *** バッチ処理前日取得OK [%s]\n", bat_date_prev);
            /*---------------------------------------------*/
        }
        /*  バッチ処理日付前日     */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev, 8);
        gh_batch_prev_ymd = atoi(wk_char);

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
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理取得OK [%s]\n", bat_date);
            /*---------------------------------------------*/
        }

        /* 処理用日付（バッチ処理日付 前日）を設定 */
        memset(gh_bat_date, 0x00, sizeof(gh_bat_date));
        strcpy(gh_bat_date, bat_date);
        for (wi_loop = 0; wi_loop < C_YEAR_MAX; wi_loop++) {
            memset(gh_tblname_ym, 0x00, sizeof(gh_tblname_ym));
            gh_tgt = wi_loop;

            /*
            EXEC SQL SELECT
            TO_CHAR(ADD_MONTHS(TO_DATE(:gh_bat_date, 'YYYYMMDD'),- :gh_tgt),'YYYYMM')
            INTO:
            gh_tblname_ym
            FROM dual;
            */
            StringDto workSql = new StringDto();
            workSql.arr = "SELECT\n" +
                    "            TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'),- ?),'YYYYMM')\n" +
                    "            FROM dual";
            sqlca.sql = workSql;
            sqlca.restAndExecute(gh_bat_date, gh_tgt);
            sqlca.fetch();
            sqlca.recData(gh_tblname_ym);

            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                        "前月取得SQLが失敗しました", 0, 0, 0);
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
            tbl_name_ym[wi_loop] = atoi(gh_tblname_ym);
        }
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            for (wi_loop = 0; wi_loop < C_YEAR_MAX; wi_loop++) {
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                C_DbgMsg("*** getRankUpMoney *** 洗替対象の各ポイントＴＢＬ年月 =[%d]\n", tbl_name_ym[wi_loop]);
            }
            /*------------------------------------------------------------*/
        }

        /* 2023/09/15 MCCM仕様変更JK215 ADD START */
        for (wi_loop = 0; wi_loop < C_YEAR_MAX2; wi_loop++) {
            memset(gh_tblname_y, 0x00, sizeof(gh_tblname_y));
            strncpy(gh_tblname_y, gh_bat_date, 4);
            tbl_name_y[wi_loop] = atoi(gh_tblname_y) - wi_loop;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            for (wi_loop = 0; wi_loop < C_YEAR_MAX2; wi_loop++) {
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                C_DbgMsg("*** getRankUpMoney *** 洗替対象の各ポイントＴＢＬ年 =[%d]\n", tbl_name_y[wi_loop]);
            }
            /*------------------------------------------------------------*/
        }
        /* 2023/09/15 MCCM仕様変更JK215 ADD END */
        /* 2023/09/21 MCCM仕様変更JK206 ADD START */
        for (wi_loop = 0; wi_loop < C_YEAR_MAX3; wi_loop++) {
            memset(gh_tblname_y, 0x00, sizeof(gh_tblname_y));
            strncpy(gh_tblname_y, gh_bat_date, 4);
            tbl_name_y2[wi_loop] = atoi(gh_tblname_y) - wi_loop;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            for (wi_loop = 0; wi_loop < C_YEAR_MAX3; wi_loop++) {
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                C_DbgMsg("*** getRankUpMoney *** 洗替対象のDM送付状態情報ＴＢＬ年 =[%d]\n", tbl_name_y2[wi_loop]);
            }
            /*------------------------------------------------------------*/
        }
        /* 2023/09/21 MCCM仕様変更JK206 ADD END */

        /*-------------------------------------*/
        /*  処理件数領域を初期化               */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理件数領域を初期化%s\n", "");
            /*-------------------------------------------------------------*/
        }
        input_data_cnt = 0; /* 入力データ件数(HSカード変更情報)    */

        /*-------------------------------------*/
        /*  主処理                             */
        /*-------------------------------------*/
        rtn_cd = cmBTcareB_main();
        if (rtn_cd != C_const_OK) {
            /* APLOG出力 */
            APLOG_WT("106", 0, null, "HSカード変更情報",
                    input_data_cnt, 0, 0, 0, 0);
            APLOG_WT("912", 0, null,
                    "カード統合洗替処理に失敗しました", 0, 0, 0, 0, 0);
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

        /* APLOG出力 */
        APLOG_WT("106", 0, null, "HSカード変更情報",
                input_data_cnt, 0, 0, 0, 0);
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
    /*  関数名 ： cmBTcareB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcareB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード統合洗替処理                                           */
    /*  【概要】                                                                  */
    /*               カード統合洗替処理処理を行う。                               */
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
    public int cmBTcareB_main() {
        int rtn_cd; /* 関数戻り値                    */
        IntegerDto rtn_status = new IntegerDto(); /* 関数ステータス                */
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ               */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("カード統合洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*  開始メッセージ                     */
        APLOG_WT("102", 0, null, "カード統合洗替処理処理", 0, 0, 0, 0, 0);

        /*----------------------------------------------*/
        /* HSカード変更情報からカード統合した顧客を抽出 */
        /*----------------------------------------------*/
        memset(wk_sql, 0x00, sizeof(wk_sql));
        /* 2023/06/05 MCCM初版 ADD START */
        strcpy(wk_sql, "SELECT 顧客番号,                                                     " +
                "       旧顧客番号,                                                   " +
                "       ＧＯＯＰＯＮ番号,                                             " +
                "       旧ＧＯＯＰＯＮ番号,                                           " +
                "       会員番号,                                                     " +
                "       旧会員番号,                                                   " +
                "       統合採用会員番号,                                             " +
                "       洗替フラグ,                                                   " +
                "       最終更新日時                                                  " +
                "FROM(                                                                " +
                "SELECT  to_char(顧客番号,  'FM000000000000000') AS 顧客番号,   " +
                "        to_char(旧顧客番号,'FM000000000000000') AS 旧顧客番号, " +
                "        NVL(ＧＯＯＰＯＮ番号, 0) AS ＧＯＯＰＯＮ番号,          " +
                "        NVL(旧ＧＯＯＰＯＮ番号, 0) AS 旧ＧＯＯＰＯＮ番号,      " +
                "        会員番号,                                              " +
                "        旧会員番号,                                            " +
                "        NVL(統合採用会員番号, 0) AS 統合採用会員番号,          " +
                "        1 AS 洗替フラグ,                                       " +
                "        最終更新日時,                                          " +
                "        ROW_NUMBER() OVER (PARTITION BY 顧客番号,旧顧客番号 ORDER BY 最終更新日時) AS RN " +
                "  FROM  HSカード変更情報                                       " +
                " WHERE  最終更新日 = ?                                       " +
                " AND    最終更新日 != 統合切替解除年月日                       " +
                "  AND    (    MOD(理由コード, 100) = 3                         " +
                "  OR      (   MOD(理由コード, 100) = 2                         " +
                "  AND    旧顧客番号 != 顧客番号 )                              " +
                " )                                                             " +
                ")                                                                    " +
                "WHERE RN = 1                                                         " +
                " UNION ALL                                                          " +
                "SELECT  to_char(顧客番号,  'FM000000000000000') AS 顧客番号,        " +
                "        to_char(旧顧客番号,'FM000000000000000') AS 旧顧客番号,      " +
                "        NVL(ＧＯＯＰＯＮ番号, 0) AS ＧＯＯＰＯＮ番号,               " +
                "        NVL(旧ＧＯＯＰＯＮ番号, 0) AS 旧ＧＯＯＰＯＮ番号,           " +
                "        会員番号,                                                   " +
                "        旧会員番号,                                                 " +
                "        NVL(統合採用会員番号, 0) AS 統合採用会員番号,               " +
                "        2 AS 洗替フラグ,                                            " +
                "        最終更新日時                                                " +
                "  FROM  HSカード変更情報                                            " +
                " WHERE  統合切替解除年月日 = ?                                    " +
                " AND    最終更新日 != 統合切替解除年月日                            " +
                " AND    (    MOD(理由コード, 100) = 3                               " +
                " OR      (   MOD(理由コード, 100) = 2                               " +
                "          AND    旧顧客番号 != 顧客番号 )                           " +
                "        )                                                           " +
                " ORDER BY 最終更新日時                                              "
        );
        /*--------------------------------------------------------------------*/
        C_DbgMsg("*** cmBTcareB_main*** SQL %s\n", wk_sql);
        /*--------------------------------------------------------------------*/
        /* 2023/06/05 MCCM初版 ADD END */
        /* HOST変数に設定 */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql);
        /* 動的SQL文の解析 */
        SqlstmDto sqlca = sqlcaManager.get("CARE_HSCC01");
        // EXEC SQL PREPARE sql_stat1 from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcareB_main*** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgEnd("カード統合洗替処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            /* 処理をNGで終了する */
            return (C_const_NG);
        }
        /* カーソル定義 */
        // EXEC SQL DECLARE CARE_HSCC01 cursor for sql_stat1;
        sqlca.declare();
        /* カーソルオープン */
        /*
        EXEC SQL OPEN CARE_HSCC01 USING :gh_batch_prev_ymd, :gh_batch_prev_ymd;                                              *//* 2022/10/17 MCCM初版 MOD *//*
         */
        sqlca.open(gh_batch_prev_ymd, gh_batch_prev_ymd);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcareB_main *** カーソルOEPNエラー = %d\n", sqlca.sqlcode);
                C_DbgEnd("カード統合洗替処理", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode,
                    "HSカード変更情報", str_sql, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /*----------------------------------------------*/
        /* カード統合洗替候補ループ                     */
        /*----------------------------------------------*/
        while (true) {
            /* 初期化 */
            memset(gh_uid, 0x00, sizeof(gh_uid));
            memset(gh_kyu_uid, 0x00, sizeof(gh_kyu_uid));
            memset(gh_goopon_no, 0x00, sizeof(gh_goopon_no));                                                        /* 2022/10/17 MCCM初版 ADD */
            memset(gh_kyu_goopon_no, 0x00, sizeof(gh_kyu_goopon_no));                                                    /* 2022/10/17 MCCM初版 ADD */
            memset(gh_kaiin_no, 0x00, sizeof(gh_kaiin_no));                                                         /* 2022/10/17 MCCM初版 ADD */
            memset(gh_kyu_kaiin_no, 0x00, sizeof(gh_kyu_kaiin_no));                                                     /* 2022/10/17 MCCM初版 ADD */
            memset(gh_tougou_kaiin_no, 0x00, sizeof(gh_tougou_kaiin_no));                                                  /* 2022/12/20 MCCM初版 ADD */
            gh_araigae_flg.arr = 0;                                                                                              /* 2022/10/17 MCCM初版 ADD */
            /* ＦＥＴＣＨ */
            /*
            EXEC SQL FETCH CARE_HSCC01
            INTO
            :gh_uid,
            :gh_kyu_uid,
            :gh_goopon_no,                                                                                              *//* 2022/10/17 MCCM初版 ADD *//*
            :gh_kyu_goopon_no,                                                                                          *//* 2022/10/17 MCCM初版 ADD *//*
            :gh_kaiin_no,                                                                                               *//* 2022/10/17 MCCM初版 ADD *//*
            :gh_kyu_kaiin_no,                                                                                           *//* 2022/10/17 MCCM初版 ADD *//*
            :gh_tougou_kaiin_no,                                                                                        *//* 2022/12/20 MCCM初版 ADD *//*
            :gh_araigae_flg;                                                                                            *//* 2022/10/17 MCCM初版 ADD *//*
             */
            sqlca.fetch();
            /* データ無し以外エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("カード統合洗替処理", C_const_NG, 0, 0);
                    /*-------------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "HSカード変更情報", str_sql, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
//                sqlca.close();
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }
            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }
            sqlca.recData(gh_uid,
                    gh_kyu_uid,
                    gh_goopon_no,
                    gh_kyu_goopon_no,
                    gh_kaiin_no,
                    gh_kyu_kaiin_no,
                    gh_tougou_kaiin_no,
                    gh_araigae_flg);
            /* 処理件数をカウントアップ */
            input_data_cnt++;
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcareB_main *** [%d]レコード目の処理開始\n",
                        input_data_cnt);
                /*----------------------------------------------------------------*/
            }

            /*----------------------------------------------*/
            /* 顧客ロック（ポイント更新）                   */
            /*----------------------------------------------*/
            if (gh_uid.longVal() < gh_kyu_uid.longVal()) {
                rtn_cd = C_KdataLock(gh_uid, "1", rtn_status);
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd,
                            rtn_status, 0, 0, 0);
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcareB_main *** ロックエラー顧客=[%s]\n", gh_uid);
                        /*------------------------------------------------------------*/
                    }
                    // EXEC SQL CLOSE CARE_HSCC01; /* カーソルクローズ                   */

                    sqlcaManager.close("CARE_HSCC01");
                    return C_const_NG;
                }
                rtn_cd = C_KdataLock(gh_kyu_uid, "1", rtn_status);
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd,
                            rtn_status, 0, 0, 0);
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcareB_main *** ロックエラー顧客=[%s]\n", gh_kyu_uid);
                        /*------------------------------------------------------------*/
                    }
                    // EXEC SQL CLOSE CARE_HSCC01; /* カーソルクローズ                   */
                    sqlcaManager.close("CARE_HSCC01");
                    return C_const_NG;
                }
            } else if (gh_uid.longVal() > gh_kyu_uid.longVal()) {
                rtn_cd = C_KdataLock(gh_kyu_uid, "1", rtn_status);
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd,
                            rtn_status, 0, 0, 0);
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcareB_main *** ロックエラー顧客=[%s]\n", gh_kyu_uid);
                        /*------------------------------------------------------------*/
                    }
                    // EXEC SQL CLOSE CARE_HSCC01; /* カーソルクローズ                   */
                    sqlcaManager.close("CARE_HSCC01");
                    return C_const_NG;
                }
                rtn_cd = C_KdataLock(gh_uid, "1", rtn_status);
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd,
                            rtn_status, 0, 0, 0);
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcareB_main *** ロックエラー顧客=[%s]\n", gh_uid);
                        /*------------------------------------------------------------*/
                    }
                    // EXEC SQL CLOSE CARE_HSCC01; /* カーソルクローズ                   */
                    sqlcaManager.close("CARE_HSCC01");
                    return C_const_NG;
                }
            } else {
                continue;
            } /* 新旧同一顧客番号なら洗替の必要なし */

            /* 2022/10/17 MCCM初版 ADD START */
            /*----------------------------------------------*/
            /* ポイント日別内訳情報洗替                     */
            /*----------------------------------------------*/
            rtn_cd = UpdatePointDayUchiwake();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HSポイント日別内訳情報更新エラー%s\n", "");
                    C_DbgEnd("ポイント日別内訳情報洗替処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HSポイント日別内訳情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }
            /* 2022/10/17 MCCM初版 ADD START */

            /*----------------------------------------------*/
            /* ポイント日別情報洗替                         */
            /*----------------------------------------------*/
            rtn_cd = UpdatePointDay();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HSポイント日別情報更新エラー%s\n", "");
                    C_DbgEnd("ポイント日別情報洗替処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HSポイント日別情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /*----------------------------------------------*/
            /* ポイント明細取引情報洗替                     */
            /*----------------------------------------------*/
            rtn_cd = UpdatePointTrade();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HSポイント明細取引情報更新エラー%s\n", "");
                    C_DbgEnd("HSポイント明細取引情報", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HSポイント明細取引情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 2023/09/15 MCCM仕様変更JK215 ADD START */
            /*----------------------------------------------*/
            /* HSポイント履歴情報洗替                     */
            /*----------------------------------------------*/
            rtn_cd = UpdatePointRireki();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HSポイント履歴情報更新エラー%s\n", "");
                    C_DbgEnd("HSポイント履歴情報", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HSポイント履歴情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /*----------------------------------------------*/
            /* HS購買履歴情報洗替                     */
            /*----------------------------------------------*/
            rtn_cd = UpdateKoubaiRireki();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HS購買履歴情報更新エラー%s\n", "");
                    C_DbgEnd("HS購買履歴情報", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HS購買履歴情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }
            /* 2023/09/15 MCCM仕様変更JK215 ADD END */
            /* 2023/09/21 MCCM仕様変更JK206 ADD START */
            /*----------------------------------------------*/
            /* HSＤＭ送付状態情報洗替                     */
            /*----------------------------------------------*/
            rtn_cd = UpdateDirectMailSendInfo();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** HSＤＭ送付状態情報更新エラー%s\n", "");
                    C_DbgEnd("HSＤＭ送付状態情報", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("912", 0, null, "HSＤＭ送付状態情報の洗替に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC01;
                sqlcaManager.close("CARE_HSCC01");
                /* 処理をNGで終了する */
                return C_const_NG;
            }
            /* 2023/09/21 MCCM仕様変更JK206 ADD END */

            /* 2022/10/17 MCCM初版 ADD START */
            /*----------------------------------------------*/
            /* クーポン利用顧客情報統合処理                 */
            /*----------------------------------------------*/
            /* カード統合洗替対象のみ処理 */
            if (gh_araigae_flg.arr == 1) {
                rtn_cd = InsertGooponRiyoKokyakuInfo();
                if (rtn_cd != C_const_OK) {

                    if (DBG_LOG) {
                        /*--------------------------------------------------------*/
                        C_DbgMsg("*** cmBTcareB_main *** クーポン利用顧客情報統合処理エラー%s\n", "");
                        C_DbgEnd("クーポン利用顧客情報統合処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------*/
                    }
                    /* APLOG出力 */
                    APLOG_WT("912", 0, null, "クーポン利用顧客情報統合処理に失敗しました",
                            0, 0, 0, 0, 0);
                    /* カーソルクローズ */
                    // EXEC SQL CLOSE CARE_HSCC01;
                    sqlcaManager.close("CARE_HSCC01");
                    /* 処理をNGで終了する */
                    return C_const_NG;
                }
            }
            /* 2022/10/17 MCCM初版 ADD END */

            /* コミットする */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();
        }  /* while ( 1 ) bottom */
        /* カーソルクローズ */
        // EXEC SQL CLOSE CARE_HSCC01;
        sqlcaManager.close("CARE_HSCC01");

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("カード統合洗替処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----cmBTcareB_main Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePointDay                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdatePointDay()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント日別情報洗替処理                                     */
    /*  【概要】                                                                  */
    /*               HSポイント日別情報を更新する。                               */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdatePointDay() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */
        int wi_loop; /* LOOP Counter              */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ポイント日別情報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* ポイント日別情報洗替処理 対象年数分実施                             */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {                                                                                           /* 2022/10/17 MCCM初版 ADD */
                /* HSポイント日別情報更新 統合洗替 */                                                                            /* 2022/10/17 MCCM初版 MOD */
                /* 20221115 EDIT START */
                sprintf(wk_sql,
                        "UPDATE  HSポイント日別情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 顧客番号 = CAST(? AS NUMERIC)         "
                        , tbl_name_ym[wi_loop]);
                /* 20221115 EDIT END */
            } else if (gh_araigae_flg.arr == 2) {                                                                                   /* 2022/10/17 MCCM初版 ADD */
                /* HSポイント日別情報更新 解除洗替 */                                                                    /* 2022/10/17 MCCM初版 ADD */
                /* 20221115 EDIT START */
                sprintf(wk_sql,                                                                                          /* 2022/10/17 MCCM初版 ADD */
                        "UPDATE  HSポイント日別情報%d " +                                                             /* 2022/10/17 MCCM初版 ADD */
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +                                                           /* 2022/10/17 MCCM初版 ADD */
                                "WHERE 会員番号 = CAST(? AS NUMERIC)        "                                                               /* 2022/10/17 MCCM初版 ADD */
                        , tbl_name_ym[wi_loop]);                                                                         /* 2022/10/17 MCCM初版 ADD */
                /* 20221115 EDIT END */
            }                                                                                                         /* 2022/10/17 MCCM初版 ADD */
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointDay *** SQL=[%s]\n", str_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_upd_hspd from :str_sql
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdatePointDay *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HSポイント日別情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {                                                                                           /* 2022/10/17 MCCM初版 ADD */
                /* 更新実行 統合洗替 */                                                                                          /* 2022/10/17 MCCM初版 MOD */
                /* 20221115 EDIT START */
                // EXEC SQL EXECUTE sql_stat_upd_hspd using :gh_uid     ,:gh_kyu_uid;
                sqlca.restAndExecute(gh_uid, gh_kyu_uid);
            } else if (gh_araigae_flg.arr == 2) {                                                                                   /* 2022/10/17 MCCM初版 ADD */
                /* 更新実行 解除洗替 */                                                                                  /* 2022/10/17 MCCM初版 ADD */
                if (atol(gh_tougou_kaiin_no) != 0) {                                                                         /* 2022/12/20 MCCM初版 ADD */
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspd
                    using                                                             *//* 2022/12/20 MCCM初版 ADD *//*
                    :gh_kyu_uid      ,                                                     *//* 2022/12/20 MCCM初版 ADD *//*
                    :gh_kaiin_no;                                                     *//* 2022/12/20 MCCM初版 ADD *//*
                     */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kaiin_no);
                } else {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspd
                    using                                                           *//* 2022/10/17 MCCM初版 ADD *//*
                    :gh_kyu_uid      ,                                                     *//* 2022/10/17 MCCM初版 ADD *//*
                    :gh_kyu_kaiin_no;                                                     *//* 2022/10/17 MCCM初版 ADD *//*
                     */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kyu_kaiin_no);
                }                                                                                                   /* 2022/12/20 MCCM初版 ADD */
                /* 20221115 EDIT END */
            }                                                                                                                /* 2022/10/17 MCCM初版 ADD */
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointDay *** HSポイント日別情報UPDATE結果 = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HSポイント日別情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("ポイント日別情報洗替処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("ポイント日別情報洗替処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdatePointDay Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePointTrade                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdatePointTrade()                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント明細取引情報洗替処理                                 */
    /*  【概要】                                                                  */
    /*               HSポイント明細取引情報を更新する。                           */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdatePointTrade() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */
        int wi_loop; /* LOOP Counter              */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ポイント明細取引情報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* ポイント明細取引情報洗替処理 対象年数分実施                         */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {                                                                                         /* 2022/10/17 MCCM初版 ADD */
                /* HSポイント明細取引情報更新 統合洗替 */                                                                        /* 2022/10/17 MCCM初版 MOD */
                /* 20221115 EDIT START */
                sprintf(wk_sql,
                        "UPDATE  HSポイント明細取引情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC)      " +
                                "WHERE 顧客番号 = CAST(? AS NUMERIC)             "
                        , tbl_name_ym[wi_loop]);
                /* 20221115 EDIT END */
            } else if (gh_araigae_flg.arr == 2) {                                                                                   /* 2022/10/17 MCCM初版 ADD */
                /* HSポイント明細取引情報更新 解除洗替 */                                                                /* 2022/10/17 MCCM初版 ADD */
                /* 20221115 EDIT START */
                sprintf(wk_sql,                                                                                              /* 2022/10/17 MCCM初版 ADD */
                        "UPDATE  HSポイント明細取引情報%d " +                                                           /* 2022/10/17 MCCM初版 ADD */
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +                                                          /* 2022/10/17 MCCM初版 ADD */
                                "WHERE 会員番号 = CAST(? AS NUMERIC)        "                                                                   /* 2022/10/17 MCCM初版 ADD */
                        , tbl_name_ym[wi_loop]);                                                                             /* 2022/10/17 MCCM初版 ADD */
                /* 20221115 EDIT END */
            }                                                                                                                /* 2022/10/17 MCCM初版 ADD */
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointTrade *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_upd_hspt from:str_sql;
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdatePointTrade *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HSポイント明細取引情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {                                                                                           /* 2022/10/17 MCCM初版 ADD */
                /* 更新実行 統合洗替 */                                                                                          /* 2022/10/17 MCCM初版 MOD */
                /* 20221115 EDIT START */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hspt using
                :gh_uid     ,
                :gh_kyu_uid;
                */
                sqlca.restAndExecute(gh_uid, gh_kyu_uid);
                /* 20221115 EDIT END */
            } else if (gh_araigae_flg.arr == 2) {                                                                                   /* 2022/10/17 MCCM初版 ADD */
                /* 更新実行 解除洗替 */                                                                                      /* 2022/10/17 MCCM初版 ADD */
                if (atol(gh_tougou_kaiin_no) != 0) {                                                                         /* 2022/12/20 MCCM初版 ADD */
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspt
                    using                                                              *//* 2022/12/20 MCCM初版 ADD *//*
                    :gh_kyu_uid      ,                                                               *//* 2022/12/20 MCCM初版 ADD *//*
                    :gh_kaiin_no;                                                               *//* 2022/12/20 MCCM初版 ADD *//*
                     */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kaiin_no);
                }                                                                                                        /* 2022/12/20 MCCM初版 ADD */
                /* 20221115 EDIT START */
                else {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspt
                    using                                                                *//* 2022/10/17 MCCM初版 ADD *//*
                    :gh_kyu_uid      ,                                                               *//* 2022/10/17 MCCM初版 ADD *//*
                    :gh_kyu_kaiin_no;                                                               *//* 2022/10/17 MCCM初版 ADD *//*
                     */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kyu_kaiin_no);
                }                                                                                                        /* 2022/10/17 MCCM初版 ADD */
                /* 20221115 EDIT END */
            }                                                                                                                /* 2022/10/17 MCCM初版 ADD */
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointTrade *** HSポイント明細取引情報UPDATE結果 = %d\n",
                        sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HSポイント明細取引情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("ポイント明細取引情報洗替処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("ポイント明細取引情報洗替処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdatePointTrade Bottom----------------------------------------------*/
    }

    /* 2022/10/17 MCCM初版 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePointDayUchiwake                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdatePointDayUchiwake ()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント日別内訳情報洗替処理                                 */
    /*  【概要】                                                                  */
    /*               HSポイント日別内訳情報を更新する。                           */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdatePointDayUchiwake() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */
        int wi_loop; /* LOOP Counter              */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ポイント日別情内訳報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* ポイント日別内訳情報洗替処理 対象年数分実施                         */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {
                /* HSポイント日別内訳情報更新 統合洗替 */
                sprintf(wk_sql,
                        "UPDATE HSポイント日別内訳情報%d B" +
                                " SET 顧客番号 = CAST(? AS NUMERIC) " +
                                "    FROM           " +
                                " HSポイント日別情報%d A "+
                                "    WHERE " +
                                "            A.システム年月日 = B.システム年月日 " +
                                "        AND A.顧客番号 = B.顧客番号 " +
                                "        AND A.処理通番 = B.処理通番 " +
                                "        AND A.顧客番号 = CAST(? AS NUMERIC) "
                        , tbl_name_ym[wi_loop], tbl_name_ym[wi_loop]);
            } else if (gh_araigae_flg.arr == 2) {
                /* HSポイント日別内訳情報更新 解除洗替 */
                sprintf(wk_sql,
                        "UPDATE HSポイント日別内訳情報%d B " +
                                " SET 顧客番号 = CAST(? AS NUMERIC) "+
                                "    FROM " +
                                " HSポイント日別情報%d A " +
                                "    WHERE " +
                                "            A.システム年月日 = B.システム年月日 " +
                                "        AND A.顧客番号 = B.顧客番号 " +
                                "        AND A.処理通番 = B.処理通番 " +
                                "        AND A.会員番号 = CAST(? AS NUMERIC) "

                        , tbl_name_ym[wi_loop], tbl_name_ym[wi_loop]);
            }
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointDayUchiwake *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_upd_hspd_uchiwake from:str_sql;
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdatePointDayUchiwake *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HSポイント日別内訳情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {
                /* 更新実行 統合洗替 */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hspd_uchiwake using
                :gh_kyu_uid ,
                :gh_uid;
                */
                sqlca.restAndExecute(gh_uid,gh_kyu_uid);
            } else if (gh_araigae_flg.arr == 2) {
                /* 更新実行 解除洗替 */
                if (atol(gh_tougou_kaiin_no) != 0) {
                    /* 2023/09/08 PH2 IT-0120 MOD START */
                    /*                    EXEC SQL EXECUTE sql_stat_upd_hspd using */
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspd_uchiwake using
                    *//* 2023/09/08 PH2 IT-0120 MOD END *//*
                    :gh_kaiin_no    ,
                    :gh_kyu_uid;
                    */
                    sqlca.restAndExecute( gh_kyu_uid,gh_kaiin_no);
                } else {
                    /* 2023/09/08 PH2 IT-0120 MOD START */
                    /*                      EXEC SQL EXECUTE sql_stat_upd_hspd using */
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspd_uchiwake using
                    *//* 2023/09/08 PH2 IT-0120 MOD END *//*
                    :gh_kyu_kaiin_no ,
                    :gh_kyu_uid;
                    */
                    sqlca.restAndExecute( gh_kyu_uid,gh_kyu_kaiin_no);
                }
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointDayUchiwake *** HSポイント日別内訳情報UPDATE結果 = %d\n",
                        sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);                    /* 2022/11/02 MCCM初版 MOD */
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HSポイント日別内訳情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("ポイント日別内訳情報洗替処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("ポイント日別内訳情報洗替処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdatePointDayUchiwake Bottom----------------------------------------------*/
    }

    /* 2023/09/15 MCCM仕様変更JK215 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePointRireki                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdatePointRireki ()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント履歴情報洗替処理                                     */
    /*  【概要】                                                                  */
    /*               HSポイント履歴情報を更新する。                               */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdatePointRireki() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */
        int wi_loop; /* LOOP Counter              */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ポイント履歴情報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* ポイント履歴情報洗替処理 対象年数分実施                             */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX2; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {
                /* HSポイント履歴情報更新 統合洗替 */
                sprintf(wk_sql,
                        "UPDATE HSポイント履歴情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 顧客番号 = CAST(? AS NUMERIC)         "
                        , tbl_name_y[wi_loop]);
            } else if (gh_araigae_flg.arr == 2) {
                /* HSポイント履歴情報更新 解除洗替 */
                sprintf(wk_sql,
                        "UPDATE HSポイント履歴情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 会員番号 = CAST(? AS NUMERIC)        "
                        , tbl_name_y[wi_loop]);
            }
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointRireki *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            //EXEC SQL PREPARE sql_stat_upd_hspr from:str_sql;
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdatePointRireki *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HSポイント履歴情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {
                /* 更新実行 統合洗替 */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hspr using
                :gh_uid     ,
                :gh_kyu_uid;
                */
                sqlca.restAndExecute(gh_uid, gh_kyu_uid);
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 1 gh_uid = %s\n", gh_uid);
                    C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 1 gh_kyu_uid = %s\n", gh_kyu_uid);
                    /*-------------------------------------------------------------*/
                }
            } else if (gh_araigae_flg.arr == 2) {
                /* 更新実行 解除洗替 */
                if (atol(gh_tougou_kaiin_no) != 0) {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspr using
                    :gh_kyu_uid      ,
                    :gh_kaiin_no;
                    */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kaiin_no);
                    if (DBG_LOG) {
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 2 gh_kyu_uid = %s\n", gh_kyu_uid);
                        C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 2 gh_kaiin_no = %s\n", gh_kaiin_no);
                        /*-------------------------------------------------------------*/
                    }
                } else {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hspr using
                    :gh_kyu_uid      ,
                    :gh_kyu_kaiin_no;
                    */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kyu_kaiin_no);
                    if (DBG_LOG) {
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 2 gh_kyu_uid = %s\n", gh_kyu_uid);
                        C_DbgMsg("*** UpdatePointRireki *** gh_araigae_flg == 2 gh_kyu_kaiin_no = %s\n", gh_kyu_kaiin_no);
                        /*-------------------------------------------------------------*/
                    }
                }
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdatePointRireki *** HSポイント履歴情報UPDATE結果 = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HSポイント履歴情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("HSポイント履歴情報", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("HSポイント履歴情報", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdatePointRireki Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKoubaiRireki                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKoubaiRireki ()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               購買履歴情報洗替処理                                         */
    /*  【概要】                                                                  */
    /*               HS購買履歴情報を更新する。                                   */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdateKoubaiRireki() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */
        int wi_loop; /* LOOP Counter              */


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("購買履歴情報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* 購買履歴情報洗替処理 対象年数分実施                             */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX2; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {
                /* HS購買履歴情報更新 統合洗替 */
                sprintf(wk_sql,
                        "UPDATE HS購買履歴情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 顧客番号 = CAST(? AS NUMERIC)         "
                        , tbl_name_y[wi_loop]);
            } else if (gh_araigae_flg.arr == 2) {
                /* HS購買履歴情報更新 解除洗替 */
                sprintf(wk_sql,
                        "UPDATE HS購買履歴情報%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 会員番号 = CAST(? AS NUMERIC)        "
                        , tbl_name_y[wi_loop]);
            }
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateKoubaiRireki *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_upd_hskr from:str_sql;
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKoubaiRireki *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HS購買履歴情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {
                /* 更新実行 統合洗替 */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hskr using
                :gh_uid     ,
                :gh_kyu_uid;
                */
                sqlca.restAndExecute(gh_uid, gh_kyu_uid);
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 1 gh_uid = %s\n", gh_uid);
                    C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 1 gh_kyu_uid = %s\n", gh_kyu_uid);
                    /*-------------------------------------------------------------*/
                }
            } else if (gh_araigae_flg.arr == 2) {
                /* 更新実行 解除洗替 */
                if (atol(gh_tougou_kaiin_no) != 0) {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hskr using
                    :gh_kyu_uid      ,
                    :gh_kaiin_no;
                    */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kaiin_no);
                    if (DBG_LOG) {
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 2 gh_kyu_uid = %s\n", gh_kyu_uid);
                        C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 2 gh_kaiin_no = %s\n", gh_kaiin_no);
                        /*-------------------------------------------------------------*/
                    }
                } else {
                    /*
                    EXEC SQL EXECUTE sql_stat_upd_hskr using
                    :gh_kyu_uid      ,
                    :gh_kyu_kaiin_no;
                    */
                    sqlca.restAndExecute(gh_kyu_uid, gh_kyu_kaiin_no);
                    if (DBG_LOG) {
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 2 gh_kyu_uid = %s\n", gh_kyu_uid);
                        C_DbgMsg("*** UpdateKoubaiRireki *** gh_araigae_flg == 2 gh_kyu_kaiin_no = %s\n", gh_kyu_kaiin_no);
                        /*-------------------------------------------------------------*/
                    }
                }
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateKoubaiRireki *** HS購買履歴情報UPDATE結果 = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s],旧会員番号=[%s]", gh_kyu_uid, gh_kyu_kaiin_no);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HS購買履歴情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("HS購買履歴情報", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("HS購買履歴情報", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateKoubaiRireki Bottom----------------------------------------------*/
    }
    /* 2023/09/15 MCCM仕様変更JK215 ADD END */

    /* 2023/09/21 MCCM仕様変更JK206 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateDirectMailSendInfo                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateDirectMailSendInfo ()                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               HSＤＭ送付状態情報洗替処理                                   */
    /*  【概要】                                                                  */
    /*               HSＤＭ送付状態情報を更新する。                               */
    /*               ・旧顧客番号の紐付け                                         */
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
    public int UpdateDirectMailSendInfo() {
        StringDto wk_sql = new StringDto(4096 * 2);            /* 動的SQLバッファ           */
        int wi_loop;            /* LOOP Counter              */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("HSＤＭ送付状態情報洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*---------------------------------------------------------------------*/
        /* HSＤＭ送付状態情報洗替処理 対象年数分実施                             */
        /*---------------------------------------------------------------------*/
        for (wi_loop = 0; wi_loop < C_YEAR_MAX3; wi_loop++) {
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            if (gh_araigae_flg.arr == 1) {
                /* HSＤＭ送付状態情報更新 統合洗替 */
                sprintf(wk_sql,
                        "UPDATE HSＤＭ送付状態%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                ",旧顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 顧客番号 = CAST(? AS NUMERIC) "
                        , tbl_name_y2[wi_loop]);
            } else if (gh_araigae_flg.arr == 2) {
                /* HSＤＭ送付状態情報更新 解除洗替 */
                sprintf(wk_sql,
                        "UPDATE HSＤＭ送付状態%d " +
                                "SET 顧客番号          = CAST(? AS NUMERIC) " +
                                ",旧顧客番号          = CAST(? AS NUMERIC) " +
                                "WHERE 旧顧客番号 = CAST(? AS NUMERIC) "
                        , tbl_name_y2[wi_loop]);
            }
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql.arr);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateDirectMailSendInfo *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_upd_hsdm from:str_sql;
            sqlca.sql = wk_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateDirectMailSendInfo *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s]", gh_kyu_uid);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                        "HSＤＭ送付状態情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            if (gh_araigae_flg.arr == 1) {
                /* 更新実行 統合洗替 */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hsdm using
                :gh_uid     ,
                :gh_kyu_uid ,
                :gh_kyu_uid;
                */
                sqlca.restAndExecute(gh_uid, gh_kyu_uid, gh_kyu_uid);
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateDirectMailSendInfo *** gh_araigae_flg == 1 gh_uid = %s\n", gh_uid);
                    C_DbgMsg("*** UpdateDirectMailSendInfo *** gh_araigae_flg == 1 gh_kyu_uid = %s\n", gh_kyu_uid);
                    /*-------------------------------------------------------------*/
                }
            } else if (gh_araigae_flg.arr == 2) {
                /* 更新実行 解除洗替 */
                /*
                EXEC SQL EXECUTE sql_stat_upd_hsdm using
                :gh_kyu_uid ,
                :gh_kyu_uid ,
                :gh_kyu_uid;
                */
                sqlca.restAndExecute(gh_kyu_uid, gh_kyu_uid, gh_kyu_uid);
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateDirectMailSendInfo *** gh_araigae_flg == 2 gh_uid = %s\n", gh_uid);
                    C_DbgMsg("*** UpdateDirectMailSendInfo *** gh_araigae_flg == 2 gh_kyu_uid = %s\n", gh_kyu_uid);
                    /*-------------------------------------------------------------*/
                }
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateDirectMailSendInfo *** HSＤＭ送付状態情報UPDATE結果 = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s]", gh_kyu_uid);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "HSＤＭ送付状態情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("HSＤＭ送付状態情報", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }
        }  /* for loop bottom(対象年数分実施) */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("HSＤＭ送付状態情報", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateDirectMailSendInfo Bottom----------------------------------------*/
    }
    /* 2023/09/21 MCCM仕様変更JK206 ADD END */

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertGooponRiyoKokyakuInfo                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertGooponRiyoKokyakuInfo ()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MSクーポン利用顧客情報nnnnn登録処理                          */
    /*  【概要】                                                                  */
    /*               MSクーポン利用顧客情報nnnnnを登録する。                      */
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
    public int InsertGooponRiyoKokyakuInfo() {
        StringDto wk_sql = new StringDto(4096 * 2); /* 動的SQLバッファ           */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MSクーポン利用顧客情報nnnnn登録処理");
            /*---------------------------------------------------------------------*/
        }

        /* カーソル定義 */
        /*
        EXEC SQL DECLARE CARE_HSCC02 CURSOR FOR
        SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'MSクーポン利用顧客情報%';
        */
        SqlstmDto sqlca = sqlcaManager.get("CARE_HSCC02");
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT UPPER(TABLENAME) FROM PG_TABLES WHERE TABLENAME LIKE 'msクーポン利用顧客情報%'";
        sqlca.sql = workSql;
        sqlca.declare();

        /* カーソルオープン */
        // EXEC SQL OPEN CARE_HSCC02;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertGooponRiyoKokyakuInfo *** カーソルOEPNエラー = %d\n", sqlca.sqlcode);
                C_DbgEnd("MSクーポン利用顧客情報nnnnn登録処理", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode,
                    "USER_TABLES", str_sql, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        while (true) {
            /* 初期化 */
            memset(gh_table_name, 0x00, sizeof(gh_table_name));

            /* ＦＥＴＣＨ */
            /*
            EXEC SQL FETCH CARE_HSCC02
            INTO:gh_table_name;
            */
            sqlca.fetch();
            sqlca.recData(gh_table_name);
            /* データ無し以外エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcareB_main *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("MSクーポン利用顧客情報nnnnn登録処理", C_const_NG, 0, 0);
                    /*-------------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "USER_TABLES", str_sql, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CARE_HSCC02;
                sqlcaManager.close("CARE_HSCC02");
                /* 処理をNGで終了する */
                return C_const_NG;
            }
            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** InsertGooponRiyoKokyakuInfo *** テーブル名:[%s]\n",
                        gh_table_name);
                /*----------------------------------------------------------------*/
            }

            /*-----------------------------------------------------------------------------------*/
            /* MSクーポン利用顧客情報nnnnn登録処理 MSクーポン利用顧客情報nnnnnテーブル数分実施   */
            /*-----------------------------------------------------------------------------------*/
            /* 初期化 */
            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            /* MSクーポン利用顧客情報nnnnn登録処理 */
            sprintf(wk_sql, "INSERT INTO %s(              " +
                            "クーポンＩＤ,                " +
                            "顧客番号,                    " +
                            "利用可能残回数,              " +
                            "最終利用年月日,              " +
                            "最終更新日,                  " +
                            "最終更新日時,                " +
                            "最終更新プログラムＩＤ )     " +
                            "(SELECT                      " +
                            "クーポンＩＤ,                " +
                            "CAST(? AS NUMERIC),                         " +
                            "利用可能残回数,              " +
                            "最終利用年月日,              " +
                            "CAST(? AS NUMERIC),                " +
                            "SYSDATE(),                      " +
                            "?  " +
                            "FROM %s                      " +
                            " WHERE 顧客番号 = CAST(? AS NUMERIC)        " +
                            ")                            "
                    , gh_table_name, gh_table_name);
            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertGooponRiyoKokyakuInfo *** SQL=[%s]\n", wk_sql);
                /*-------------------------------------------------------------*/
            }
            /* 動的SQL文を解析する */
            // EXEC SQL PREPARE sql_stat_insert_grki from:str_sql;
            CmABfuncLServiceImpl.sqlca.sql = wk_sql;
            CmABfuncLServiceImpl.sqlca.prepare();

            if (CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** InsertGooponRiyoKokyakuInfo *** 動的SQL 解析NG = %d\n", CmABfuncLServiceImpl.sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s]", gh_kyu_uid);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "PREPARE", CmABfuncLServiceImpl.sqlca.sqlcode,
                        "MSクーポン利用顧客情報", out_format_buf, 0, 0);
                return C_const_NG;
            }
            /* 登録実行 */
            /*
            EXEC SQL EXECUTE sql_stat_insert_grki using
            :gh_uid     ,
            :gh_bat_date,
            :gh_saishu_koshin_programid,
            :gh_kyu_uid;
            */
            CmABfuncLServiceImpl.sqlca.restAndExecute(gh_uid, gh_bat_date, gh_saishu_koshin_programid, gh_kyu_uid);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertGooponRiyoKokyakuInfo *** MSクーポン利用顧客情報INSERT結果 = %d\n",
                        CmABfuncLServiceImpl.sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            if (CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_OK &&
                    CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_DUPL) {
                /* エラーの場合 */
                memset(out_format_buf, 0x00, sizeof(out_format_buf));
                sprintf(out_format_buf, "旧顧客番号=[%s]", gh_kyu_uid);
                /* APLOG出力 */
                APLOG_WT("904", 0, null, "INSERT", CmABfuncLServiceImpl.sqlca.sqlcode,
                        "MSクーポン利用顧客情報", out_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("MSクーポン利用顧客情報nnnnn登録処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                return C_const_NG;
            }

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("MSクーポン利用顧客情報nnnnn登録処理", C_const_OK, 0, 0);
                /*---------------------------------------------*/
            }

        }  /* while ( 1 ) bottom */
        /* カーソルクローズ */
        // EXEC SQL CLOSE CARE_HSCC02;
        sqlcaManager.close("CARE_HSCC02");

        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertGooponRiyoKokyakuInfo Bottom----------------------------------------------*/
    }
}
