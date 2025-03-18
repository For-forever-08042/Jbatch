package jp.co.mcc.nttdata.batch.business.service.cmBTrankB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RANK_INFO_TBL;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_BOTTOM_RANK;
import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *   プログラム名   ： 顧客ランククリア処理（cmBTrankB）
 *
 *   【処理概要】
 * 2022/09/22 MCCM初版 MOD START *
 // *     MS顧客制度情報の翌々年用のランク初期設定を行う。
 *     TSランク情報の翌々年用のランク初期設定を行う。
 * 2022/09/22 MCCM初版 MOD END *
 *     １日更新最大件数まで処理する。
 *     ＦＥＴＣＨ件数が０となった時に、
 *     WSバッチ処理実行管理のシーケンス番号に当日日付（バッチ処理日）で更新する。
 *
 *   【引数説明】
 *     -c更新最大件数             :（必須）更新最大件数の設定
 *     -DEBUG(-debug)             :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *     10　　 ：　正常
 *     99　　 ：　異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/12/06 SSI.本田 ： 初版
 *      2.00 :  2015/01/30 SSI.上野 ： 引数追加（更新最大件数）
 *     30.00 :  2021/02/02 NDBS.緒方： 期間限定Ｐ対応によりリコンパイル
 *                                     (顧客データロック処理内容更新のため)
 *     40.00 :  2022/09/22 SSI.川内 ： MCCM初版
 *     41.00 :  2024/05/17 SSI.橋詰 :  HS-0228ランククリアバッチ改修
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTrankBServiceImpl extends CmABfuncLServiceImpl implements CmBTrankBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */
    TS_RANK_INFO_TBL mskosed_t;                  /* TSランク情報バッファ        */

    /* 処理用 */
    ItemDto get_cnt_buf = new ItemDto();             /* WSバッチ処理実行管理件数           */
    int this_date;               /* 処理日付の年月日                   */
    int this_year;               /* 処理日付の年                       */
    int this_month;              /* 処理日付の月                       */
    StringDto Program_Name = new StringDto(10);        /* バージョンなしプログラム名         */
    StringDto str_sql1 = new StringDto(4096 * 2);          /* 実行用SQL文字列                    */
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int DEF_OFF = 0;         /* OFF                                */
    int DEF_ON = 1;         /* ON                                 */
    String DEF_ARG_C = "-c";         /* 最大取得件数                       */
    String DEF_DEBUG = "-DEBUG";         /* デバッグスイッチ                   */
    String DEF_debug = "-debug";         /* デバッグスイッチ                   */
    String C_PRGNAME = "TSランククリア";  /* APログ用機能名                     */
    int C_LOCK_CHU = 2; /* ロック中戻り値用                   */

    /* 2022/09/22 MCCM初版 MOD START */
    int C_SED_UPDMAX = 700000;   /* TSランク情報 更新最大件数          */
    int C_SED_OUTINF = 10000; /* TSランク情報 出力件数情報境界      */
    /* 2022/09/22 MCCM初版 MOD END */


    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int next2year_1char_buf;
    /**
     * 翌々年の下１桁
     **/
    int nextyear_1char_buf;
    /**
     * 翌年の下１桁
     **/
    int next2month_3char_buf;
    /**
     * 翌々月の３桁
     **/
    int nextmonth_3char_buf;
    /**
     * 翌月の下３桁
     **/
    StringDto bat_date = new StringDto(9);
    /**
     * バッチ処理日付
     **/
    long g_max_data_count;
    /**
     * 更新最大件数
     **/
    int chk_arg_c;
    /**
     * 引数-cチェック用
     **/
    StringDto arg_c_Value = new StringDto(256);         /** 引数c設定値                      **/


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

        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        int rtn_cd;                         /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス                 */
        int arg_cnt;                        /* 引数チェック用カウンタ             */
        /* 2022/09/22 MCCM初版 MOD START */
        /*int     ms_kosed_upd_cnt_buf;*/           /* MS顧客制度情報 更新件数            */
        IntegerDto ms_kosed_upd_cnt_buf = new IntegerDto();           /* TSランク情報 更新件数              */
        /* 2022/09/22 MCCM初版 MOD END */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                       */
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット              */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /* 2022/09/22 MCCM初版 MOD START */
        /*    ms_kosed_upd_cnt_buf = 0;*/           /* MS顧客制度情報 更新レコード件数    */
        ms_kosed_upd_cnt_buf.arr = 0;           /* TSランク情報 更新レコード件数      */
        /* 2022/09/22 MCCM初版 MOD END */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

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
        memset(Program_Name, 0x00, sizeof(Program_Name));
        strcpy(Program_Name, Cg_Program_Name); /* プログラム名 */

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

        chk_arg_c = DEF_OFF;
        g_max_data_count = 0;
        memset(arg_c_Value, 0x00, sizeof(arg_c_Value));

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
                /* 2015/01/30 引数追加 START */
            } else if (memcmp(arg_Work1, DEF_ARG_C, 2) == 0) {  /* -cの場合            */
                rtn_cd = cmBTrankB_Chk_Arg(arg_Work1);       /* パラメータCHK       */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_c_Value, arg_Work1.substring(2));
                } else {
                    rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
                /* 2015/01/30 引数追加 END */
            } else {                        /* 定義外パラメータ                   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
                return exit(C_const_APNG);
            }
        }

        /* 必須パラメータ未指定チェック */
        if (chk_arg_c == DEF_OFF) {
            strcpy(chg_format_buf, "-c 引数の値が不正です[必須引数なし]");
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
                C_DbgMsg("*** main *** DBコネクトNG rtn=[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  バッチ処理日取得処理               */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得 %s\n", "START");
            /*------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_date);
            /*------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTrankB_main(ms_kosed_upd_cnt_buf);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTrankB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            /* 2022/09/22 MCCM初版 MOD START */
            /*        APLOG_WT("912", 0, null, "顧客ランククリアに失敗しました", 0, 0, 0, 0, 0);*/
            APLOG_WT("912", 0, null, "TSランククリアに失敗しました", 0, 0, 0, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            sqlca.rollback();             /* ロールバック                       */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 各テーブル更新処理件数を出力 */
        /* 2022/09/22 MCCM初版 MOD START */
        /*    APLOG_WT("107", 0, null, "MS顧客制度情報", ms_kosed_upd_cnt_buf,0,0,0,0);*/
        APLOG_WT("107", 0, null, "TSランク情報", ms_kosed_upd_cnt_buf, 0, 0, 0, 0);
        /* 2022/09/22 MCCM初版 MOD END */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            /* 2022/09/22 MCCM初版 MOD START */
            /*    C_DbgMsg("*** main *** MM顧客制度情報 処理件数:[%d]\n", ms_kosed_upd_cnt_buf);*/
            C_DbgMsg("*** main *** TSランク情報 処理件数:[%d]\n", ms_kosed_upd_cnt_buf);
            /* 2022/09/22 MCCM初版 MOD END */
            /*------------------------------------------------------------*/
        }

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
        sqlcaManager.commitRelease(sqlca);

        return exit(C_const_APOK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTrankB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTrankB_Chk_Arg( char *Arg_in )                             */
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
    public int cmBTrankB_Chk_Arg(StringDto Arg_in) {
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット              */
        StringDto arg_work = new StringDto(256);                     /* 引数チェック用エリア            */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTrankB_Chk_Arg処理");
            C_DbgMsg("*** cmBTrankB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
        memset(arg_work, 0x00, sizeof(arg_work));

        if (memcmp(Arg_in, DEF_ARG_C, 2) == 0) {        /* -c更新最大件数         */
            if (chk_arg_c != DEF_OFF) {
                sprintf(chg_format_buf, "-c 引数の値が不正です[重複]（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_c = DEF_ON;

            if (strlen(Arg_in) < 3) {                 /* 桁数チェック           */
                sprintf(chg_format_buf, "-c 引数の値が不正です[未指定]（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            strcpy(arg_work, Arg_in.substring(2));
            if (atol(arg_work) <= 0) {                /* 数字チェック           */
                sprintf(chg_format_buf, "-c 引数の値が不正です[数字以外]（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTrankB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTrankB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTrankB_main( int *ms_kosed_upd_cnt_buf )                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /* 2022/09/22 MCCM初版 MOD START */
    /*//               顧客ランククリア処理                                         */
    /*//               MS顧客制度情報の翌々年のランクコードおよびランク変更日付を   */
    /*//               更新する。                                                   */
    /*               TSランククリア処理                                           */
    /*               TSランク情報の翌々年のランクコードおよびランク変更日付を     */
    /*               更新する。                                                   */
    /* 2022/09/22 MCCM初版 MOD END */
    /*                                                                            */
    /*  【引数】                                                                  */
    /* 2022/09/22 MCCM初版 MOD START */
    /*//      int          * ms_kosed_upd_cnt_buf：MS顧客制度情報 更新レコード件数  */
    /*      int          * ms_kosed_upd_cnt_buf：TSランク情報 更新レコード件数    */
    /* 2022/09/22 MCCM初版 MOD END */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTrankB_main(IntegerDto ms_kosed_upd_cnt_buf) {
        int rtn_cd;                    /* 関数戻り値                        */
        IntegerDto rtn_status = new IntegerDto();                /* 関数結果ステータス                */
        int log_out_cnt_buf;           /* ログ出力カウンタ                  */
        /* 2022/09/22 MCCM初版 MOD START */
        /*int           ms_kosed_lck_cnt_buf;*/      /* MS顧客制度情報 ロックレコード件数 */
        int ms_kosed_lck_cnt_buf;      /* TSランク情報 ロックレコード件数   */
        /* 2022/09/22 MCCM初版 MOD END */
        int next2month_year;           /* 処理日付の翌々月の年              */
        int next2month;                /* 処理日付の翌々月                  */
        /* 2022/09/22 MCCM初版 ADD START */
        int next2month_lastyear;       /* 処理日付の翌々月の前年            */
        int nextmonth_year;            /* 処理日付の翌月の年                */
        int nextmonth;                 /* 処理日付の翌月                    */
        int nextmonth_lastyear;        /* 処理日付の翌月の前年              */
        /* 2022/09/22 MCCM初版 ADD END */
        StringDto i_kokyaku_no_buf = new StringDto(15 + 1);    /* 顧客番号                          */
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen * 2); /* 動的SQLバッファ                   */
        StringDto wk_in_next2year = new StringDto(2);
        /* 2022/09/22 MCCM初版 ADD START */
        StringDto wk_in_nextyear = new StringDto(2);
        /* 2022/09/22 MCCM初版 ADD END */
        StringDto wk_in_next2month = new StringDto(4);
        /* 2022/09/22 MCCM初版 ADD START */
        StringDto wk_in_nextmonth = new StringDto(4);
        /* 2022/09/22 MCCM初版 ADD END */
        StringDto wk_next2year = new StringDto(4);
        /* 2022/09/22 MCCM初版 ADD START */
        StringDto wk_nextyear = new StringDto(4);
        /* 2022/09/22 MCCM初版 ADD END */
        StringDto wk_next2month = new StringDto(10);
        /* 2022/09/22 MCCM初版 ADD START */
        StringDto wk_nextmonth = new StringDto(10);
        /* 2022/09/22 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTrankB_main処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        /* 2022/09/22 MCCM初版 MOD START */
        /*    *ms_kosed_upd_cnt_buf = 0;*/           /* MS顧客制度情報 更新レコード件数   */
        ms_kosed_upd_cnt_buf.arr = 0;           /* TSランク情報 更新レコード件数     */
        /* 2022/09/22 MCCM初版 MOD END */
        log_out_cnt_buf = 0;                 /* ログ出力カウンタ                  */
        /* 2022/09/22 MCCM初版 MOD START */
        /*    ms_kosed_lck_cnt_buf = 0;*/            /* MS顧客制度情報 ロックレコード件数 */
        ms_kosed_lck_cnt_buf = 0;            /* TSランク情報 ロックレコード件数   */
        /* 2022/09/22 MCCM初版 MOD END */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
        next2month_year = 0;
        nextmonth_year = 0;
        /* 2022/09/22 MCCM初版 ADD START */
        next2month_lastyear = 0;
        nextmonth_lastyear = 0;
        /* 2022/09/22 MCCM初版 ADD END */
        next2month = 0;
        /* 2022/09/22 MCCM初版 ADD START */
        nextmonth = 0;
        /* 2022/09/22 MCCM初版 ADD END */

        /* 後続処理で使用する為、先に取得しておく */
        this_date = atoi(bat_date);                 /* 処理日付(YYYYMMDD) */

        this_year = this_date / 10000;              /* 処理日付の年 */
        this_month = (this_date / 100) % 100;       /* 処理日付の月 */

        /* 2022/09/22 MCCM初版 MOD START */
//    /* 翌々月とその年を算出 */
        /* 翌々月とその年と翌々月の前年を算出 */
        /* 2022/09/22 MCCM初版 MOD END */
        next2month = this_month + 2;
        next2month_year = this_year;
        /* 2022/09/22 MCCM初版 ADD START */
        nextmonth_year = 0;
        next2month_lastyear = this_year - 1;
        nextmonth_lastyear = 0;
        /* 2022/09/22 MCCM初版 ADD END */
        if (next2month > 12) {
            next2month = next2month - 12;
            next2month_year = next2month_year + 1;
            /* 2022/09/22 MCCM初版 ADD START */
            next2month_lastyear = this_year;
            /* 2022/09/22 MCCM初版 ADD END */
        }

        /* 2022/09/22 MCCM初版 ADD START */
        /* 翌月とその年を算出 */
        nextmonth = this_month + 1;
        nextmonth_year = this_year;
        nextmonth_lastyear = this_year - 1;
        if (nextmonth > 12) {
            nextmonth = nextmonth - 12;
            nextmonth_year = nextmonth_year + 1;
            nextmonth_lastyear = this_year;
        }

        /* バッチ処理日付の月が≦３の場合 */
        if (this_month <= 3) {
            /* 翌年の下１桁=を算出 */
            next2year_1char_buf = ((this_date + 10000) / 10000) % 10;
            /* 今年の下１桁=を算出 */
            nextyear_1char_buf = (this_date / 10000) % 10;
        } else {
            /* 翌々年の下１桁=を算出 */
            /* 2022/09/22 MCCM初版 ADD END */
            next2year_1char_buf = ((this_date + 20000) / 10000) % 10;
            /* 2022/09/22 MCCM初版 ADD START */
            /* 翌年の下１桁=を算出 */
            nextyear_1char_buf = ((this_date + 10000) / 10000) % 10;
        }
        /* 2022/09/22 MCCM初版 ADD END */

        next2month_3char_buf = next2month;
        /* 2022/09/22 MCCM初版 ADD START */
        /* バッチ処理日付の翌々月が≦３の場合 */
        if (next2month <= 3) {
            /* バッチ処理日付の翌々月の前年が奇数年の場合 */
            if ((next2month_lastyear % 2) == 1) {
                next2month_3char_buf = next2month_3char_buf + 100;
            }
        } else {
            /* バッチ処理日付の翌々月の年が奇数年の場合 */
            /* 2022/09/22 MCCM初版 ADD END */
            if ((next2month_year % 2) == 1) {
                /* 奇数年の場合、1mmとして扱う */
                next2month_3char_buf = next2month_3char_buf + 100;
            }
            /* 2022/09/22 MCCM初版 ADD START */
        }

        nextmonth_3char_buf = nextmonth;
        /* バッチ処理日付の翌月が≦３の場合 */
        if (nextmonth <= 3) {
            /* バッチ処理日付の翌月の前年が奇数年の場合 */
            if ((nextmonth_lastyear % 2) == 1) {
                nextmonth_3char_buf = nextmonth_3char_buf + 100;
            }
        } else {
            /* バッチ処理日付の翌月の年が奇数年の場合 */
            if ((nextmonth_year % 2) == 1) {
                /* 奇数年の場合、1mmとして扱う */
                nextmonth_3char_buf = nextmonth_3char_buf + 100;
            }
        }
        /* 2022/09/22 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTrankB_main *** 当年の日付  =[%d]\n", this_date);
            C_DbgMsg("*** cmBTrankB_main *** 翌々年の下１桁=[%d]\n", next2year_1char_buf);
            /* 2022/09/22 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTrankB_main *** 翌年の下１桁=[%d]\n", nextyear_1char_buf);
            /* 2022/09/22 MCCM初版 ADD END */
            C_DbgMsg("*** cmBTrankB_main *** 翌々月=[%d]\n", next2month_3char_buf);
            /* 2022/09/22 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTrankB_main *** 翌月=[%d]\n", nextmonth_3char_buf);
            /* 2022/09/22 MCCM初版 ADD END */
            /*------------------------------------------------------------*/
        }

        memset(wk_in_next2year, 0x00, sizeof(wk_in_next2year));
        /* 2022/09/22 MCCM初版 ADD START */
        memset(wk_in_nextyear, 0x00, sizeof(wk_in_nextyear));
        /* 2022/09/22 MCCM初版 ADD END */
        memset(wk_in_next2month, 0x00, sizeof(wk_in_next2month));
        /* 2022/09/22 MCCM初版 ADD START */
        memset(wk_in_nextmonth, 0x00, sizeof(wk_in_nextmonth));
        /* 2022/09/22 MCCM初版 ADD END */

        sprintf(wk_in_next2year, "%d", next2year_1char_buf);
        /* 2022/09/22 MCCM初版 ADD START */
        sprintf(wk_in_nextyear, "%d", nextyear_1char_buf);
        /* 2022/09/22 MCCM初版 ADD END */
        sprintf(wk_in_next2month, "%03d", next2month_3char_buf);
        /* 2022/09/22 MCCM初版 ADD START */
        sprintf(wk_in_nextmonth, "%03d", nextmonth_3char_buf);
        /* 2022/09/22 MCCM初版 ADD END */

//        wk_in_next2year[sizeof(wk_in_next2year) - 1] = '\0';
//        /* 2022/09/22 MCCM初版 ADD START */
//        wk_in_nextyear[sizeof(wk_in_nextyear) - 1] = '\0';
//        /* 2022/09/22 MCCM初版 ADD END */
//        wk_in_next2month[sizeof(wk_in_next2month) - 1] = '\0';
//        /* 2022/09/22 MCCM初版 ADD START */
//        wk_in_nextmonth[sizeof(wk_in_nextmonth) - 1] = '\0';
//        /* 2022/09/22 MCCM初版 ADD END */

        /* 翌々年の参照カラム */
        memset(wk_next2year, 0x00, sizeof(wk_next2year));
        rtn_cd = C_ConvHalf2Full(wk_in_next2year, wk_next2year);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 2022/09/22 MCCM初版 ADD START */
        /* 翌年の参照カラム */
        memset(wk_nextyear, 0x00, sizeof(wk_nextyear));
        rtn_cd = C_ConvHalf2Full(wk_in_nextyear, wk_nextyear);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }
        /* 2022/09/22 MCCM初版 ADD END */

        /* 翌々月の参照カラム */
        memset(wk_next2month, 0x00, sizeof(wk_next2month));
        rtn_cd = C_ConvHalf2Full(wk_in_next2month, wk_next2month);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 2022/09/22 MCCM初版 ADD START */
        /* 翌月の参照カラム */
        memset(wk_nextmonth, 0x00, sizeof(wk_nextmonth));
        rtn_cd = C_ConvHalf2Full(wk_in_nextmonth, wk_nextmonth);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }
        /* 2022/09/22 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_main *** 翌々年ランク参照カラム=[%s]\n", wk_next2year);
            /* 2022/09/22 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTfrnkB_main *** 翌年ランク金額参照カラム=[%s]\n", wk_nextyear);
            /* 2022/09/22 MCCM初版 ADD END */
            C_DbgMsg("*** cmBTrankB_main *** 翌々月ランク参照カラム=[%s]\n", wk_next2month);
            /* 2022/09/22 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTrankB_main *** 翌月ランク金額金額参照カラム=[%s]\n", wk_nextmonth);
            /* 2022/09/22 MCCM初版 ADD END */
            /*------------------------------------------------------------*/
        }

        sprintf(wk_sql,
                "SELECT 顧客番号,"
                        + "       年次ランクコード０,"
                        + "       年次ランクコード１,"
                        + "       年次ランクコード２,"
                        + "       年次ランクコード３,"
                        + "       年次ランクコード４,"
                        + "       年次ランクコード５,"
                        + "       年次ランクコード６,"
                        + "       年次ランクコード７,"
                        + "       年次ランクコード８,"
                        + "       年次ランクコード９,"
                        /* 2022/09/22 MCCM初版 ADD START */
                        + "       年間ランクＵＰ対象金額０,"
                        + "       年間ランクＵＰ対象金額１,"
                        + "       年間ランクＵＰ対象金額２,"
                        + "       年間ランクＵＰ対象金額３,"
                        + "       年間ランクＵＰ対象金額４,"
                        + "       年間ランクＵＰ対象金額５,"
                        + "       年間ランクＵＰ対象金額６,"
                        + "       年間ランクＵＰ対象金額７,"
                        + "       年間ランクＵＰ対象金額８,"
                        + "       年間ランクＵＰ対象金額９,"
                        /* 2022/09/22 MCCM初版 ADD END */
                        + "       月次ランクコード００１,"
                        + "       月次ランクコード００２,"
                        + "       月次ランクコード００３,"
                        + "       月次ランクコード００４,"
                        + "       月次ランクコード００５,"
                        + "       月次ランクコード００６,"
                        + "       月次ランクコード００７,"
                        + "       月次ランクコード００８,"
                        + "       月次ランクコード００９,"
                        + "       月次ランクコード０１０,"
                        + "       月次ランクコード０１１,"
                        + "       月次ランクコード０１２,"
                        + "       月次ランクコード１０１,"
                        + "       月次ランクコード１０２,"
                        + "       月次ランクコード１０３,"
                        + "       月次ランクコード１０４,"
                        + "       月次ランクコード１０５,"
                        + "       月次ランクコード１０６,"
                        + "       月次ランクコード１０７,"
                        + "       月次ランクコード１０８,"
                        + "       月次ランクコード１０９,"
                        + "       月次ランクコード１１０,"
                        + "       月次ランクコード１１１,"
                        /* 2022/09/22 MCCM初版 MOD START */
                        /*           "       月次ランクコード１１２ "*/
                        + "       月次ランクコード１１２,"
                        /* 2022/09/22 MCCM初版 MOD END */
                        /* 2022/09/22 MCCM初版 ADD START */
                        + "       月間ランクＵＰ対象金額００１,"
                        + "       月間ランクＵＰ対象金額００２,"
                        + "       月間ランクＵＰ対象金額００３,"
                        + "       月間ランクＵＰ対象金額００４,"
                        + "       月間ランクＵＰ対象金額００５,"
                        + "       月間ランクＵＰ対象金額００６,"
                        + "       月間ランクＵＰ対象金額００７,"
                        + "       月間ランクＵＰ対象金額００８,"
                        + "       月間ランクＵＰ対象金額００９,"
                        + "       月間ランクＵＰ対象金額０１０,"
                        + "       月間ランクＵＰ対象金額０１１,"
                        + "       月間ランクＵＰ対象金額０１２,"
                        + "       月間ランクＵＰ対象金額１０１,"
                        + "       月間ランクＵＰ対象金額１０２,"
                        + "       月間ランクＵＰ対象金額１０３,"
                        + "       月間ランクＵＰ対象金額１０４,"
                        + "       月間ランクＵＰ対象金額１０５,"
                        + "       月間ランクＵＰ対象金額１０６,"
                        + "       月間ランクＵＰ対象金額１０７,"
                        + "       月間ランクＵＰ対象金額１０８,"
                        + "       月間ランクＵＰ対象金額１０９,"
                        + "       月間ランクＵＰ対象金額１１０,"
                        + "       月間ランクＵＰ対象金額１１１,"
                        + "       月間ランクＵＰ対象金額１１２,"
                        /* 2022/09/22 MCCM初版 ADD END */
                        /* 2022/12/20 MCCM初版 ADD START */
                        + "       月間プレミアムポイント数００１,"
                        + "       月間プレミアムポイント数００２,"
                        + "       月間プレミアムポイント数００３,"
                        + "       月間プレミアムポイント数００４,"
                        + "       月間プレミアムポイント数００５,"
                        + "       月間プレミアムポイント数００６,"
                        + "       月間プレミアムポイント数００７,"
                        + "       月間プレミアムポイント数００８,"
                        + "       月間プレミアムポイント数００９,"
                        + "       月間プレミアムポイント数０１０,"
                        + "       月間プレミアムポイント数０１１,"
                        + "       月間プレミアムポイント数０１２,"
                        + "       月間プレミアムポイント数１０１,"
                        + "       月間プレミアムポイント数１０２,"
                        + "       月間プレミアムポイント数１０３,"
                        + "       月間プレミアムポイント数１０４,"
                        + "       月間プレミアムポイント数１０５,"
                        + "       月間プレミアムポイント数１０６,"
                        + "       月間プレミアムポイント数１０７,"
                        + "       月間プレミアムポイント数１０８,"
                        + "       月間プレミアムポイント数１０９,"
                        + "       月間プレミアムポイント数１１０,"
                        + "       月間プレミアムポイント数１１１,"
                        + "       月間プレミアムポイント数１１２ "
                        /* 2022/12/20 MCCM初版 ADD END */
                        /* 2022/09/22 MCCM初版 MOD START */
                        /*           "FROM   MS顧客制度情報 "*/
                        + "FROM   TSランク情報 "
                        /* 2022/09/22 MCCM初版 MOD END */
                        + "WHERE  年次ランクコード%s <> 0 "
                        /* 2022/09/22 MCCM初版 ADD START */
                        + "OR     年間ランクＵＰ対象金額%s <> 0 "
                        /* 2022/09/22 MCCM初版 ADD END */
                        /* 2022/09/22 MCCM初版 MOD START */
                        /*           "OR     月次ランクコード%s <> 0 ",*/
                        + "OR     月次ランクコード%s <> 0 "
                        /* 2022/09/22 MCCM初版 MOD END */
                        /* 2022/09/22 MCCM初版 ADD START */
                        + "OR     月間ランクＵＰ対象金額%s <> 0 "
                        /* 2022/09/22 MCCM初版 ADD END */
                        /* 2022/12/20 MCCM初版 ADD START */
                        + "OR     月間プレミアムポイント数%s <> 0 ",
                /* 2022/12/20 MCCM初版 ADD END */
                wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth, wk_nextmonth);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTrankB_main *** SQL=[%s]\n", wk_sql);
            /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql1, wk_sql);

        SqlstmDto sqlca =sqlcaManager.get("RANK_MSKI01");
        /* 動的ＳＱＬ文の解析 */
        sqlca.sql = str_sql1;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_rank1 from:
//        str_sql1;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTrankB_main *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            /* 2022/09/22 MCCM初版 MOD START */
/*        sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌々月ランク=[%s]", wk_next2year, wk_next2month);
        APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode, "MS顧客制度情報", chg_format_buf, 0, 0);*/
            sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode, "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /* 2022/09/22 MCCM初版 MOD START */
//    /* 顧客の顧客制度情報取得 */
        /* 顧客のTSランク情報取得 */
        sqlca.declare();
//        EXEC SQL DECLARE RANK_MSKI01 CURSOR FOR sql_rank1;
        /* 2022/09/22 MCCM初版 MOD END */

        /* カーソルオープン */
        sqlca.open();
//        EXEC SQL OPEN RANK_MSKI01;
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* 2022/09/22 MCCM初版 MOD START */
/*        sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌々月ランク=[%s]", wk_next2year, wk_next2month);
        APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode, "MS顧客制度情報", chg_format_buf, 0, 0);*/
            sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode, "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /* 対象データ数分繰り返す */
        while (true) {
            /* 初期化 */
            mskosed_t = new TS_RANK_INFO_TBL();
            memset(mskosed_t, 0x00, sizeof(mskosed_t));

            sqlca.fetchInto(mskosed_t.kokyaku_no,
                    mskosed_t.nenji_rank_cd_0,
                    mskosed_t.nenji_rank_cd_1,
                    mskosed_t.nenji_rank_cd_2,
                    mskosed_t.nenji_rank_cd_3,
                    mskosed_t.nenji_rank_cd_4,
                    mskosed_t.nenji_rank_cd_5,
                    mskosed_t.nenji_rank_cd_6,
                    mskosed_t.nenji_rank_cd_7,
                    mskosed_t.nenji_rank_cd_8,
                    mskosed_t.nenji_rank_cd_9,
                    /* 2022/09/22 MCCM初版 ADD START */
                    mskosed_t.nenkan_rankup_taisho_kingaku_0,
                    mskosed_t.nenkan_rankup_taisho_kingaku_1,
                    mskosed_t.nenkan_rankup_taisho_kingaku_2,
                    mskosed_t.nenkan_rankup_taisho_kingaku_3,
                    mskosed_t.nenkan_rankup_taisho_kingaku_4,
                    mskosed_t.nenkan_rankup_taisho_kingaku_5,
                    mskosed_t.nenkan_rankup_taisho_kingaku_6,
                    mskosed_t.nenkan_rankup_taisho_kingaku_7,
                    mskosed_t.nenkan_rankup_taisho_kingaku_8,
                    mskosed_t.nenkan_rankup_taisho_kingaku_9,
                    /* 2022/09/22 MCCM初版 ADD END */
                    /* 2022/09/22 MCCM初版 MOD START */
/*                :mskosed_t.getuji_rank_cd_001,
                :mskosed_t.getuji_rank_cd_002,
                :mskosed_t.getuji_rank_cd_003,
                :mskosed_t.getuji_rank_cd_004,
                :mskosed_t.getuji_rank_cd_005,
                :mskosed_t.getuji_rank_cd_006,
                :mskosed_t.getuji_rank_cd_007,
                :mskosed_t.getuji_rank_cd_008,
                :mskosed_t.getuji_rank_cd_009,
                :mskosed_t.getuji_rank_cd_010,
                :mskosed_t.getuji_rank_cd_011,
                :mskosed_t.getuji_rank_cd_012,
                :mskosed_t.getuji_rank_cd_101,
                :mskosed_t.getuji_rank_cd_102,
                :mskosed_t.getuji_rank_cd_103,
                :mskosed_t.getuji_rank_cd_104,
                :mskosed_t.getuji_rank_cd_105,
                :mskosed_t.getuji_rank_cd_106,
                :mskosed_t.getuji_rank_cd_107,
                :mskosed_t.getuji_rank_cd_108,
                :mskosed_t.getuji_rank_cd_109,
                :mskosed_t.getuji_rank_cd_110,
                :mskosed_t.getuji_rank_cd_111,
                :mskosed_t.getuji_rank_cd_112;*/
                    mskosed_t.getsuji_rank_cd_001,
                    mskosed_t.getsuji_rank_cd_002,
                    mskosed_t.getsuji_rank_cd_003,
                    mskosed_t.getsuji_rank_cd_004,
                    mskosed_t.getsuji_rank_cd_005,
                    mskosed_t.getsuji_rank_cd_006,
                    mskosed_t.getsuji_rank_cd_007,
                    mskosed_t.getsuji_rank_cd_008,
                    mskosed_t.getsuji_rank_cd_009,
                    mskosed_t.getsuji_rank_cd_010,
                    mskosed_t.getsuji_rank_cd_011,
                    mskosed_t.getsuji_rank_cd_012,
                    mskosed_t.getsuji_rank_cd_101,
                    mskosed_t.getsuji_rank_cd_102,
                    mskosed_t.getsuji_rank_cd_103,
                    mskosed_t.getsuji_rank_cd_104,
                    mskosed_t.getsuji_rank_cd_105,
                    mskosed_t.getsuji_rank_cd_106,
                    mskosed_t.getsuji_rank_cd_107,
                    mskosed_t.getsuji_rank_cd_108,
                    mskosed_t.getsuji_rank_cd_109,
                    mskosed_t.getsuji_rank_cd_110,
                    mskosed_t.getsuji_rank_cd_111,
                    mskosed_t.getsuji_rank_cd_112,
                    /* 2022/09/22 MCCM初版 MOD END */
                    /* 2022/09/22 MCCM初版 ADD START */
                    mskosed_t.gekkan_rankup_taisho_kingaku_001,
                    mskosed_t.gekkan_rankup_taisho_kingaku_002,
                    mskosed_t.gekkan_rankup_taisho_kingaku_003,
                    mskosed_t.gekkan_rankup_taisho_kingaku_004,
                    mskosed_t.gekkan_rankup_taisho_kingaku_005,
                    mskosed_t.gekkan_rankup_taisho_kingaku_006,
                    mskosed_t.gekkan_rankup_taisho_kingaku_007,
                    mskosed_t.gekkan_rankup_taisho_kingaku_008,
                    mskosed_t.gekkan_rankup_taisho_kingaku_009,
                    mskosed_t.gekkan_rankup_taisho_kingaku_010,
                    mskosed_t.gekkan_rankup_taisho_kingaku_011,
                    mskosed_t.gekkan_rankup_taisho_kingaku_012,
                    mskosed_t.gekkan_rankup_taisho_kingaku_101,
                    mskosed_t.gekkan_rankup_taisho_kingaku_102,
                    mskosed_t.gekkan_rankup_taisho_kingaku_103,
                    mskosed_t.gekkan_rankup_taisho_kingaku_104,
                    mskosed_t.gekkan_rankup_taisho_kingaku_105,
                    mskosed_t.gekkan_rankup_taisho_kingaku_106,
                    mskosed_t.gekkan_rankup_taisho_kingaku_107,
                    mskosed_t.gekkan_rankup_taisho_kingaku_108,
                    mskosed_t.gekkan_rankup_taisho_kingaku_109,
                    mskosed_t.gekkan_rankup_taisho_kingaku_110,
                    mskosed_t.gekkan_rankup_taisho_kingaku_111,
                    mskosed_t.gekkan_rankup_taisho_kingaku_112,
                    /* 2022/09/22 MCCM初版 ADD END */
                    /* 2022/12/20 MCCM初版 ADD START */
                    mskosed_t.gekkan_premium_point_kingaku_001,
                    mskosed_t.gekkan_premium_point_kingaku_002,
                    mskosed_t.gekkan_premium_point_kingaku_003,
                    mskosed_t.gekkan_premium_point_kingaku_004,
                    mskosed_t.gekkan_premium_point_kingaku_005,
                    mskosed_t.gekkan_premium_point_kingaku_006,
                    mskosed_t.gekkan_premium_point_kingaku_007,
                    mskosed_t.gekkan_premium_point_kingaku_008,
                    mskosed_t.gekkan_premium_point_kingaku_009,
                    mskosed_t.gekkan_premium_point_kingaku_010,
                    mskosed_t.gekkan_premium_point_kingaku_011,
                    mskosed_t.gekkan_premium_point_kingaku_012,
                    mskosed_t.gekkan_premium_point_kingaku_101,
                    mskosed_t.gekkan_premium_point_kingaku_102,
                    mskosed_t.gekkan_premium_point_kingaku_103,
                    mskosed_t.gekkan_premium_point_kingaku_104,
                    mskosed_t.gekkan_premium_point_kingaku_105,
                    mskosed_t.gekkan_premium_point_kingaku_106,
                    mskosed_t.gekkan_premium_point_kingaku_107,
                    mskosed_t.gekkan_premium_point_kingaku_108,
                    mskosed_t.gekkan_premium_point_kingaku_109,
                    mskosed_t.gekkan_premium_point_kingaku_110,
                    mskosed_t.gekkan_premium_point_kingaku_111,
                    mskosed_t.gekkan_premium_point_kingaku_112);
//            EXEC SQL FETCH RANK_MSKI01
//            INTO:
//               ;
            /* 2022/12/20 MCCM初版 ADD END */

            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    /* 2022/09/22 MCCM初版 MOD START */
                    /*            C_DbgMsg("*** cmBTrankB_main *** MS顧客制度情報 FETCH NG=[%d]\n",sqlca.sqlcode );*/
                    C_DbgMsg("*** cmBTrankB_main *** TSランク情報 FETCH NG=[%d]\n", sqlca.sqlcode);
                    /* 2022/09/22 MCCM初版 MOD END */
                    /*------------------------------------------------------------*/
                }
                /* 2022/09/22 MCCM初版 MOD START */
/*            sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌々月ランク=[%s]", wk_next2year, wk_next2month);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode, "MS顧客制度情報", chg_format_buf, 0, 0);*/
                sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
                APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode, "TSランク情報", chg_format_buf, 0, 0);
                /* 2022/09/22 MCCM初版 MOD END */
//                EXEC SQL CLOSE RANK_MSKI01;
                sqlcaManager.close("RANK_MSKI01");

                return C_const_NG;

            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTrankB_main *** データなし%s\n", "");
                    /*----------------------------------------------------------------------------*/
                }
                /* WSバッチ処理実行管理追加／更新処理  */
                rtn_cd = cmBTrankB_upd_Ewrk();
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTrankB_main *** WSバッチ処理実行管理更新処理 NG=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
                    }
//                    EXEC SQL CLOSE RANK_MSKI01;
                    sqlcaManager.close("RANK_MSKI01");

                    APLOG_WT("912", 0, null, "WSバッチ処理実行管理の更新に失敗しました", 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

                sqlca.commit();
//                EXEC SQL COMMIT WORK;       /* コミット                           */
                break;
            }

            /* 翌々年のランクコード、ランク変更日付の設定 */
            switch (next2year_1char_buf) {
                case 0:
                    mskosed_t.nenji_rank_cd_0.arr = C_BOTTOM_RANK;
                    break;
                case 1:
                    mskosed_t.nenji_rank_cd_1.arr = C_BOTTOM_RANK;
                    break;
                case 2:
                    mskosed_t.nenji_rank_cd_2.arr = C_BOTTOM_RANK;
                    break;
                case 3:
                    mskosed_t.nenji_rank_cd_3.arr = C_BOTTOM_RANK;
                    break;
                case 4:
                    mskosed_t.nenji_rank_cd_4.arr = C_BOTTOM_RANK;
                    break;
                case 5:
                    mskosed_t.nenji_rank_cd_5.arr = C_BOTTOM_RANK;
                    break;
                case 6:
                    mskosed_t.nenji_rank_cd_6.arr = C_BOTTOM_RANK;
                    break;
                case 7:
                    mskosed_t.nenji_rank_cd_7.arr = C_BOTTOM_RANK;
                    break;
                case 8:
                    mskosed_t.nenji_rank_cd_8.arr = C_BOTTOM_RANK;
                    break;
                case 9:
                    mskosed_t.nenji_rank_cd_9.arr = C_BOTTOM_RANK;
                    break;
            }

            /* 2022/09/22 MCCM初版 ADD START */
            /* 翌年のランクＵＰ金額の設定 */
            switch (nextyear_1char_buf) {
                case 0:
                    mskosed_t.nenkan_rankup_taisho_kingaku_0.arr = 0;
                    break;
                case 1:
                    mskosed_t.nenkan_rankup_taisho_kingaku_1.arr = 0;
                    break;
                case 2:
                    mskosed_t.nenkan_rankup_taisho_kingaku_2.arr = 0;
                    break;
                case 3:
                    mskosed_t.nenkan_rankup_taisho_kingaku_3.arr = 0;
                    break;
                case 4:
                    mskosed_t.nenkan_rankup_taisho_kingaku_4.arr = 0;
                    break;
                case 5:
                    mskosed_t.nenkan_rankup_taisho_kingaku_5.arr = 0;
                    break;
                case 6:
                    mskosed_t.nenkan_rankup_taisho_kingaku_6.arr = 0;
                    break;
                case 7:
                    mskosed_t.nenkan_rankup_taisho_kingaku_7.arr = 0;
                    break;
                case 8:
                    mskosed_t.nenkan_rankup_taisho_kingaku_8.arr = 0;
                    break;
                case 9:
                    mskosed_t.nenkan_rankup_taisho_kingaku_9.arr = 0;
                    break;
            }
            /* 2022/09/22 MCCM初版 ADD END */

            /* 翌々月のランクコードの設定 */
            switch (next2month_3char_buf) {
                /* 2022/09/22 MCCM初版 MOD START */
/*            case 1:     mskosed_t.getuji_rank_cd_001        = C_BOTTOM_RANK;
                        break;
            case 2:     mskosed_t.getuji_rank_cd_002        = C_BOTTOM_RANK;
                        break;
            case 3:     mskosed_t.getuji_rank_cd_003        = C_BOTTOM_RANK;
                        break;
            case 4:     mskosed_t.getuji_rank_cd_004        = C_BOTTOM_RANK;
                        break;
            case 5:     mskosed_t.getuji_rank_cd_005        = C_BOTTOM_RANK;
                        break;
            case 6:     mskosed_t.getuji_rank_cd_006        = C_BOTTOM_RANK;
                        break;
            case 7:     mskosed_t.getuji_rank_cd_007        = C_BOTTOM_RANK;
                        break;
            case 8:     mskosed_t.getuji_rank_cd_008        = C_BOTTOM_RANK;
                        break;
            case 9:     mskosed_t.getuji_rank_cd_009        = C_BOTTOM_RANK;
                        break;
            case 10:    mskosed_t.getuji_rank_cd_010        = C_BOTTOM_RANK;
                        break;
            case 11:    mskosed_t.getuji_rank_cd_011        = C_BOTTOM_RANK;
                        break;
            case 12:    mskosed_t.getuji_rank_cd_012        = C_BOTTOM_RANK;
                        break;
            case 101:   mskosed_t.getuji_rank_cd_101        = C_BOTTOM_RANK;
                        break;
            case 102:   mskosed_t.getuji_rank_cd_102        = C_BOTTOM_RANK;
                        break;
            case 103:   mskosed_t.getuji_rank_cd_103        = C_BOTTOM_RANK;
                        break;
            case 104:   mskosed_t.getuji_rank_cd_104        = C_BOTTOM_RANK;
                        break;
            case 105:   mskosed_t.getuji_rank_cd_105        = C_BOTTOM_RANK;
                        break;
            case 106:   mskosed_t.getuji_rank_cd_106        = C_BOTTOM_RANK;
                        break;
            case 107:   mskosed_t.getuji_rank_cd_107        = C_BOTTOM_RANK;
                        break;
            case 108:   mskosed_t.getuji_rank_cd_108        = C_BOTTOM_RANK;
                        break;
            case 109:   mskosed_t.getuji_rank_cd_109        = C_BOTTOM_RANK;
                        break;
            case 110:   mskosed_t.getuji_rank_cd_110        = C_BOTTOM_RANK;
                        break;
            case 111:   mskosed_t.getuji_rank_cd_111        = C_BOTTOM_RANK;
                        break;
            case 112:   mskosed_t.getuji_rank_cd_112        = C_BOTTOM_RANK;
                        break;*/
                case 1:
                    mskosed_t.getsuji_rank_cd_001.arr = C_BOTTOM_RANK;
                    break;
                case 2:
                    mskosed_t.getsuji_rank_cd_002.arr = C_BOTTOM_RANK;
                    break;
                case 3:
                    mskosed_t.getsuji_rank_cd_003.arr = C_BOTTOM_RANK;
                    break;
                case 4:
                    mskosed_t.getsuji_rank_cd_004.arr = C_BOTTOM_RANK;
                    break;
                case 5:
                    mskosed_t.getsuji_rank_cd_005.arr = C_BOTTOM_RANK;
                    break;
                case 6:
                    mskosed_t.getsuji_rank_cd_006.arr = C_BOTTOM_RANK;
                    break;
                case 7:
                    mskosed_t.getsuji_rank_cd_007.arr = C_BOTTOM_RANK;
                    break;
                case 8:
                    mskosed_t.getsuji_rank_cd_008.arr = C_BOTTOM_RANK;
                    break;
                case 9:
                    mskosed_t.getsuji_rank_cd_009.arr = C_BOTTOM_RANK;
                    break;
                case 10:
                    mskosed_t.getsuji_rank_cd_010.arr = C_BOTTOM_RANK;
                    break;
                case 11:
                    mskosed_t.getsuji_rank_cd_011.arr = C_BOTTOM_RANK;
                    break;
                case 12:
                    mskosed_t.getsuji_rank_cd_012.arr = C_BOTTOM_RANK;
                    break;
                case 101:
                    mskosed_t.getsuji_rank_cd_101.arr = C_BOTTOM_RANK;
                    break;
                case 102:
                    mskosed_t.getsuji_rank_cd_102.arr = C_BOTTOM_RANK;
                    break;
                case 103:
                    mskosed_t.getsuji_rank_cd_103.arr = C_BOTTOM_RANK;
                    break;
                case 104:
                    mskosed_t.getsuji_rank_cd_104.arr = C_BOTTOM_RANK;
                    break;
                case 105:
                    mskosed_t.getsuji_rank_cd_105.arr = C_BOTTOM_RANK;
                    break;
                case 106:
                    mskosed_t.getsuji_rank_cd_106.arr = C_BOTTOM_RANK;
                    break;
                case 107:
                    mskosed_t.getsuji_rank_cd_107.arr = C_BOTTOM_RANK;
                    break;
                case 108:
                    mskosed_t.getsuji_rank_cd_108.arr = C_BOTTOM_RANK;
                    break;
                case 109:
                    mskosed_t.getsuji_rank_cd_109.arr = C_BOTTOM_RANK;
                    break;
                case 110:
                    mskosed_t.getsuji_rank_cd_110.arr = C_BOTTOM_RANK;
                    break;
                case 111:
                    mskosed_t.getsuji_rank_cd_111.arr = C_BOTTOM_RANK;
                    break;
                case 112:
                    mskosed_t.getsuji_rank_cd_112.arr = C_BOTTOM_RANK;
                    break;
                /* 2022/09/22 MCCM初版 MOD END */
            }

            /* 2022/09/22 MCCM初版 ADD START */
            /* 翌月のランクＵＰ金額の設定 */
            switch (nextmonth_3char_buf) {
                case 1:
                    mskosed_t.gekkan_rankup_taisho_kingaku_001.arr = 0;
                    break;
                case 2:
                    mskosed_t.gekkan_rankup_taisho_kingaku_002.arr = 0;
                    break;
                case 3:
                    mskosed_t.gekkan_rankup_taisho_kingaku_003.arr = 0;
                    break;
                case 4:
                    mskosed_t.gekkan_rankup_taisho_kingaku_004.arr = 0;
                    break;
                case 5:
                    mskosed_t.gekkan_rankup_taisho_kingaku_005.arr = 0;
                    break;
                case 6:
                    mskosed_t.gekkan_rankup_taisho_kingaku_006.arr = 0;
                    break;
                case 7:
                    mskosed_t.gekkan_rankup_taisho_kingaku_007.arr = 0;
                    break;
                case 8:
                    mskosed_t.gekkan_rankup_taisho_kingaku_008.arr = 0;
                    break;
                case 9:
                    mskosed_t.gekkan_rankup_taisho_kingaku_009.arr = 0;
                    break;
                case 10:
                    mskosed_t.gekkan_rankup_taisho_kingaku_010.arr = 0;
                    break;
                case 11:
                    mskosed_t.gekkan_rankup_taisho_kingaku_011.arr = 0;
                    break;
                case 12:
                    mskosed_t.gekkan_rankup_taisho_kingaku_012.arr = 0;
                    break;
                case 101:
                    mskosed_t.gekkan_rankup_taisho_kingaku_101.arr = 0;
                    break;
                case 102:
                    mskosed_t.gekkan_rankup_taisho_kingaku_102.arr = 0;
                    break;
                case 103:
                    mskosed_t.gekkan_rankup_taisho_kingaku_103.arr = 0;
                    break;
                case 104:
                    mskosed_t.gekkan_rankup_taisho_kingaku_104.arr = 0;
                    break;
                case 105:
                    mskosed_t.gekkan_rankup_taisho_kingaku_105.arr = 0;
                    break;
                case 106:
                    mskosed_t.gekkan_rankup_taisho_kingaku_106.arr = 0;
                    break;
                case 107:
                    mskosed_t.gekkan_rankup_taisho_kingaku_107.arr = 0;
                    break;
                case 108:
                    mskosed_t.gekkan_rankup_taisho_kingaku_108.arr = 0;
                    break;
                case 109:
                    mskosed_t.gekkan_rankup_taisho_kingaku_109.arr = 0;
                    break;
                case 110:
                    mskosed_t.gekkan_rankup_taisho_kingaku_110.arr = 0;
                    break;
                case 111:
                    mskosed_t.gekkan_rankup_taisho_kingaku_111.arr = 0;
                    break;
                case 112:
                    mskosed_t.gekkan_rankup_taisho_kingaku_112.arr = 0;
                    break;
            }
            /* 2022/09/22 MCCM初版 ADD END */
            /* 2022/12/20 MCCM初版 ADD START */
            switch (nextmonth_3char_buf) {
                case 1:
                    mskosed_t.gekkan_premium_point_kingaku_001.arr = 0;
                    break;
                case 2:
                    mskosed_t.gekkan_premium_point_kingaku_002.arr = 0;
                    break;
                case 3:
                    mskosed_t.gekkan_premium_point_kingaku_003.arr = 0;
                    break;
                case 4:
                    mskosed_t.gekkan_premium_point_kingaku_004.arr = 0;
                    break;
                case 5:
                    mskosed_t.gekkan_premium_point_kingaku_005.arr = 0;
                    break;
                case 6:
                    mskosed_t.gekkan_premium_point_kingaku_006.arr = 0;
                    break;
                case 7:
                    mskosed_t.gekkan_premium_point_kingaku_007.arr = 0;
                    break;
                case 8:
                    mskosed_t.gekkan_premium_point_kingaku_008.arr = 0;
                    break;
                case 9:
                    mskosed_t.gekkan_premium_point_kingaku_009.arr = 0;
                    break;
                case 10:
                    mskosed_t.gekkan_premium_point_kingaku_010.arr = 0;
                    break;
                case 11:
                    mskosed_t.gekkan_premium_point_kingaku_011.arr = 0;
                    break;
                case 12:
                    mskosed_t.gekkan_premium_point_kingaku_012.arr = 0;
                    break;
                case 101:
                    mskosed_t.gekkan_premium_point_kingaku_101.arr = 0;
                    break;
                case 102:
                    mskosed_t.gekkan_premium_point_kingaku_102.arr = 0;
                    break;
                case 103:
                    mskosed_t.gekkan_premium_point_kingaku_103.arr = 0;
                    break;
                case 104:
                    mskosed_t.gekkan_premium_point_kingaku_104.arr = 0;
                    break;
                case 105:
                    mskosed_t.gekkan_premium_point_kingaku_105.arr = 0;
                    break;
                case 106:
                    mskosed_t.gekkan_premium_point_kingaku_106.arr = 0;
                    break;
                case 107:
                    mskosed_t.gekkan_premium_point_kingaku_107.arr = 0;
                    break;
                case 108:
                    mskosed_t.gekkan_premium_point_kingaku_108.arr = 0;
                    break;
                case 109:
                    mskosed_t.gekkan_premium_point_kingaku_109.arr = 0;
                    break;
                case 110:
                    mskosed_t.gekkan_premium_point_kingaku_110.arr = 0;
                    break;
                case 111:
                    mskosed_t.gekkan_premium_point_kingaku_111.arr = 0;
                    break;
                case 112:
                    mskosed_t.gekkan_premium_point_kingaku_112.arr = 0;
                    break;
            }
            /* 2022/12/20 MCCM初版 ADD END */

            /* 2022/09/22 MCCM初版 MOD START */
//        /* 顧客制度情報ロック処理 */
            /* TSランク情報ロック処理 */
            /* 2022/09/22 MCCM初版 MOD END */
            memset(i_kokyaku_no_buf, 0x00, sizeof(i_kokyaku_no_buf));
            strncpy(i_kokyaku_no_buf, mskosed_t.kokyaku_no.strVal(), mskosed_t.kokyaku_no.len);

            /* 顧客ロック処理 */
            rtn_cd = C_KdataLock(i_kokyaku_no_buf, "1", rtn_status);

            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTrankB_main *** C_KdataLock NG rtn_cd=[%d]\n", rtn_cd);
                    C_DbgMsg("*** cmBTrankB_main *** C_KdataLock NG rtn_status=[%d]\n", rtn_status);
                    /*------------------------------------------------------------*/
                }

                sqlcaManager.close("RANK_MSKI01");
//                EXEC SQL CLOSE RANK_MSKI01; /* カーソルクローズ                   */
                /* 2022/09/22 MCCM初版 MOD START */
                /*                APLOG_WT("912", 0, null, "MS顧客制度情報のロックに失敗しました", 0, 0, 0, 0, 0);*/
                APLOG_WT("912", 0, null, "TSランク情報のロックに失敗しました", 0, 0, 0, 0, 0);
                /* 2022/09/22 MCCM初版 MOD END */
                return C_const_NG;

            }

            /* 2022/09/22 MCCM初版 MOD START */
//        /* 顧客制度情報更新処理 */
            /* TSランク情報更新処理 */
            /* 2022/09/22 MCCM初版 MOD END */
            rtn_cd = cmBTrankB_upd_Ksed();
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    /* 2022/09/22 MCCM初版 MOD START */
                    /*            C_DbgMsg("*** cmBTrankB_main *** MS顧客制度情報更新処理 NG=[%d]\n", rtn_cd);*/
                    C_DbgMsg("*** cmBTrankB_main *** TSランク情報更新処理 NG=[%d]\n", rtn_cd);
                    /* 2022/09/22 MCCM初版 MOD END */
                    /*------------------------------------------------------------*/
                }
                sqlcaManager.close("RANK_MSKI01");
//                EXEC SQL CLOSE RANK_MSKI01; /* カーソルクローズ                   */
                /* 2022/09/22 MCCM初版 MOD START */
                /*            APLOG_WT("912", 0, null, "MS顧客制度情報の更新に失敗しました", 0, 0, 0, 0, 0);*/
                APLOG_WT("912", 0, null, "TSランク情報の更新に失敗しました", 0, 0, 0, 0, 0);
                /* 2022/09/22 MCCM初版 MOD END */
                return C_const_NG;
            }

            sqlcaManager.getDefault().commit();
//            EXEC SQL COMMIT WORK;          /* コミット                          */

            /* ログ出力カウンタアップ */
            log_out_cnt_buf++;

            /* 10000件毎メッセージ出力 */
            if (log_out_cnt_buf >= C_SED_OUTINF) {
                /* 2022/09/22 MCCM初版 MOD START */
                /*            APLOG_WT("107", 0, null, "MS顧客制度情報", log_out_cnt_buf, 0, 0, 0, 0);*/
                APLOG_WT("107", 0, null, "TSランク情報", log_out_cnt_buf, 0, 0, 0, 0);
                /* 2022/09/22 MCCM初版 MOD END */
                log_out_cnt_buf = 0;        /* ログ出力カウンタ０クリア           */
            }

            /* 2022/09/22 MCCM初版 MOD START */
//        /* MS顧客制度情報 更新件数アップ */
            /* TSランク情報 更新件数アップ */
            /* 2022/09/22 MCCM初版 MOD END */
            ms_kosed_upd_cnt_buf.arr = ms_kosed_upd_cnt_buf.arr + 1;

            /* 2015/01/30 引数追加（更新最大件数） */
            /* 2022/09/22 MCCM初版 MOD START */
//        /* MS顧客制度情報 更新件数の最大件数チェック（1処理更新最大件数まで） */
            /* TSランク情報 更新件数の最大件数チェック（1処理更新最大件数まで） */
            /* 2022/09/22 MCCM初版 MOD END */
            if (ms_kosed_upd_cnt_buf.arr >= g_max_data_count) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    /* 2022/09/22 MCCM初版 MOD START */
                    /*            C_DbgMsg("*** cmBTrankB_main *** MS顧客制度情報 更新件数=[%d]\n", *ms_kosed_upd_cnt_buf);*/
                    C_DbgMsg("*** cmBTrankB_main *** TSランク情報 更新件数=[%d]\n", ms_kosed_upd_cnt_buf);
                    /* 2022/09/22 MCCM初版 MOD END */
                    /*------------------------------------------------------------*/
                }
                break;
            }
        } /* FETCH LOOP */

//        EXEC SQL CLOSE RANK_MSKI01;
//        sqlca.curse_close();
        sqlcaManager.close("RANK_MSKI01");
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            /* 2022/09/22 MCCM初版 MOD START */
            /*    C_DbgMsg("*** cmBTrankB_main *** MS顧客制度情報 ロック中件数=[%d]\n", ms_kosed_lck_cnt_buf);*/
            C_DbgMsg("*** cmBTrankB_main *** TSランク情報 ロック中件数=[%d]\n", ms_kosed_lck_cnt_buf);
            /* 2022/09/22 MCCM初版 MOD END */
            C_DbgEnd("cmBTrankB_main処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTrankB_upd_Ksed                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTrankB_upd_Ksed( char *i_kokyaku_no_buf )                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /* 2022/09/22 MCCM初版 MOD START */
    /*//               顧客制度情報更新処理                                         */
    /*//               顧客制度情報のレコードの更新を行う。                         */
    /*               TSランク情報更新処理                                         */
    /*               TSランク情報のレコードの更新を行う。                         */
    /* 2022/09/22 MCCM初版 MOD END */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char      * i_kokyaku_no_buf ：顧客番号                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTrankB_upd_Ksed() {
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTrankB_upd_Ksed処理");
            /* 2022/09/22 MCCM初版 MOD START */
            /*    C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報[%s]\n", "UPDATE");*/
            C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報[%s]\n", "UPDATE");
            /* 2022/09/22 MCCM初版 MOD END */
            C_DbgMsg("*** cmBTrankB_upd_Ksed *** 顧客番号　　 =[%s]\n", mskosed_t.kokyaku_no.arr);
            /*------------------------------------------------------------*/
        }
//        SqlstmDto sqlca = sqlcaManager.get("cmBTrankB_upd_Ksed");

        /* 2024/05/17 HS-0228ランククリアバッチ回収 STRAT */
        /* 年次ランクコードのクリア */
        switch (next2year_1char_buf) {
            case 0:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年次ランクコード０ = " + mskosed_t.nenji_rank_cd_0 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 1:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年次ランクコード１ = " + mskosed_t.nenji_rank_cd_1 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 2:
                sqlca.sql.arr = " UPDATE TSランク情報" +
                        "                SET 年次ランクコード２ = " + mskosed_t.nenji_rank_cd_2 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 3:
                sqlca.sql.arr = " UPDATE TSランク情報" +
                        "                SET 年次ランクコード３ = " + mskosed_t.nenji_rank_cd_3 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 4:
                sqlca.sql.arr = "UPDATE TSランク情報 " +
                        "                SET 年次ランクコード４ = " + mskosed_t.nenji_rank_cd_4 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 5:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年次ランクコード５ = " + mskosed_t.nenji_rank_cd_5 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;

                break;
            case 6:
                sqlca.sql.arr = "UPDATE TSランク情報 " +
                        "                SET 年次ランクコード６ = " + mskosed_t.nenji_rank_cd_6 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 7:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年次ランクコード７ = " + mskosed_t.nenji_rank_cd_7 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;

                break;
            case 8:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年次ランクコード８ = " + mskosed_t.nenji_rank_cd_8 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;

                break;
            case 9:
                sqlca.sql.arr = " UPDATE TSランク情報 " +
                        "                SET 年次ランクコード９ = " + mskosed_t.nenji_rank_cd_9 + "," +
                        "                    最終更新日 = " + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ = '" + Program_Name +"'"+
                        "                WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
        }
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                /* 2022/09/22 MCCM初版 MOD START */
                /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
                C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /* 2022/09/22 MCCM初版 MOD END */
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr);
            /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /* 年間ランクＵＰ対象金額のクリア */
        switch (nextyear_1char_buf) {
            case 0:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額０ = " + mskosed_t.nenkan_rankup_taisho_kingaku_0 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;

                break;
            case 1:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額１ =" + mskosed_t.nenkan_rankup_taisho_kingaku_1 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                ;
                break;
            case 2:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額２ =" + mskosed_t.nenkan_rankup_taisho_kingaku_2 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 3:
                sqlca.sql.arr = " UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額３ =" + mskosed_t.nenkan_rankup_taisho_kingaku_3 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 4:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額４ =" + mskosed_t.nenkan_rankup_taisho_kingaku_4 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 5:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額５ =" + mskosed_t.nenkan_rankup_taisho_kingaku_5 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 6:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額６ =" + mskosed_t.nenkan_rankup_taisho_kingaku_6 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 7:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額７ =" + mskosed_t.nenkan_rankup_taisho_kingaku_7 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 8:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額８ =" + mskosed_t.nenkan_rankup_taisho_kingaku_8 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
            case 9:
                sqlca.sql.arr = "UPDATE TSランク情報" +
                        "                SET 年間ランクＵＰ対象金額９ =" + mskosed_t.nenkan_rankup_taisho_kingaku_9 + "," +
                        "                    最終更新日 =" + this_date + "," +
                        "                    最終更新日時 = SYSDATE()," +
                        "                    最終更新プログラムＩＤ ='" + Program_Name +"'"+
                        "                WHERE 顧客番号 =" + mskosed_t.kokyaku_no;
                break;
        }
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                /* 2022/09/22 MCCM初版 MOD START */
                /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
                C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /* 2022/09/22 MCCM初版 MOD END */
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr);
            /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        switch (next2month_3char_buf) {
            case 1:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "    SET 月次ランクコード００１ = " + mskosed_t.getsuji_rank_cd_001 + ","
                        + "        最終更新日 = " + this_date + ","
                        + "        最終更新日時 = SYSDATE(),"
                        + "        最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "    WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 2:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "    SET 月次ランクコード００２ = " + mskosed_t.getsuji_rank_cd_002 + ","
                        + "        最終更新日 = " + this_date + ","
                        + "        最終更新日時 = SYSDATE(),"
                        + "        最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "    WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 3:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "   SET 月次ランクコード００３ = " + mskosed_t.getsuji_rank_cd_003 + ","
                        + "       最終更新日 = " + this_date + ","
                        + "       最終更新日時 = SYSDATE(),"
                        + "       最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "   WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 4:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "   SET 月次ランクコード００４ = " + mskosed_t.getsuji_rank_cd_004 + ","
                        + "       最終更新日 = " + this_date + ","
                        + "       最終更新日時 = SYSDATE(),"
                        + "       最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "   WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 5:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "   SET 月次ランクコード００５ = " + mskosed_t.getsuji_rank_cd_005 + ","
                        + "       最終更新日 = " + this_date + ","
                        + "       最終更新日時 = SYSDATE(),"
                        + "       最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "   WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 6:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード００６ = " + mskosed_t.getsuji_rank_cd_006 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 7:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード００７ = " + mskosed_t.getsuji_rank_cd_007 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 8:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード００８ = " + mskosed_t.getsuji_rank_cd_008 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 9:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード００９ = " + mskosed_t.getsuji_rank_cd_009 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 10:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード０１０ = " + mskosed_t.getsuji_rank_cd_010 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 11:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード０１１ = " + mskosed_t.getsuji_rank_cd_011 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 12:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード０１２ = " + mskosed_t.getsuji_rank_cd_012 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 101:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０１ = " + mskosed_t.getsuji_rank_cd_101 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 102:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０２ = " + mskosed_t.getsuji_rank_cd_102 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 103:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０３ = " + mskosed_t.getsuji_rank_cd_103 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 104:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０４ = " + mskosed_t.getsuji_rank_cd_104 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 105:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０５ = " + mskosed_t.getsuji_rank_cd_105 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 106:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０６ = " + mskosed_t.getsuji_rank_cd_106 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 107:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０７ = " + mskosed_t.getsuji_rank_cd_107 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 108:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０８ = " + mskosed_t.getsuji_rank_cd_108 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 109:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１０９ = " + mskosed_t.getsuji_rank_cd_109 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 110:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１１０ = " + mskosed_t.getsuji_rank_cd_110 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 111:
                sqlca.sql.arr = "UPDATE  TSランク情報"
                        + "          SET 月次ランクコード１１１ = " + mskosed_t.getsuji_rank_cd_111 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 112:
                sqlca.sql.arr = "UPDATE TSランク情報"
                        + "          SET 月次ランクコード１１２ = " + mskosed_t.getsuji_rank_cd_112 + ","
                        + "              最終更新日 = " + this_date + ","
                        + "              最終更新日時 = SYSDATE(),"
                        + "              最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "          WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
        }
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                /* 2022/09/22 MCCM初版 MOD START */
                /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
                C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /* 2022/09/22 MCCM初版 MOD END */
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr);
            /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /*月間ランクUP対象金額のクリア */
        switch (nextmonth_3char_buf) {
            case 1:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００１ = " + mskosed_t.gekkan_rankup_taisho_kingaku_001 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 2:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００２ = " + mskosed_t.gekkan_rankup_taisho_kingaku_002 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 3:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００３ = " + mskosed_t.gekkan_rankup_taisho_kingaku_003 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 4:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００４ = " + mskosed_t.gekkan_rankup_taisho_kingaku_004 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 5:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００５ = " + mskosed_t.gekkan_rankup_taisho_kingaku_005 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 6:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００６ = " + mskosed_t.gekkan_rankup_taisho_kingaku_006 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 7:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００７ = " + mskosed_t.gekkan_rankup_taisho_kingaku_007 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 8:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００８ = " + mskosed_t.gekkan_rankup_taisho_kingaku_008 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 9:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額００９ = " + mskosed_t.gekkan_rankup_taisho_kingaku_009 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 10:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額０１０ = " + mskosed_t.gekkan_rankup_taisho_kingaku_010 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 11:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額０１１ = " + mskosed_t.gekkan_rankup_taisho_kingaku_011 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 12:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額０１２ = " + mskosed_t.gekkan_rankup_taisho_kingaku_012 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 101:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０１ = " + mskosed_t.gekkan_rankup_taisho_kingaku_101 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 102:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０２ = " + mskosed_t.gekkan_rankup_taisho_kingaku_102 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 103:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０３ = " + mskosed_t.gekkan_rankup_taisho_kingaku_103 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 104:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０４ = " + mskosed_t.gekkan_rankup_taisho_kingaku_104 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 105:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０５ = " + mskosed_t.gekkan_rankup_taisho_kingaku_105 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 106:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０６ = " + mskosed_t.gekkan_rankup_taisho_kingaku_106 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 107:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０７ = " + mskosed_t.gekkan_rankup_taisho_kingaku_107 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 108:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０８ = " + mskosed_t.gekkan_rankup_taisho_kingaku_108 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 109:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１０９ = " + mskosed_t.gekkan_rankup_taisho_kingaku_109 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 110:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１１０ = " + mskosed_t.gekkan_rankup_taisho_kingaku_110 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 111:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１１１ = " + mskosed_t.gekkan_rankup_taisho_kingaku_111 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 112:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "           SET 月間ランクＵＰ対象金額１１２ = " + mskosed_t.gekkan_rankup_taisho_kingaku_112 + ","
                        + "               最終更新日 = " + this_date + ","
                        + "               最終更新日時 = SYSDATE(),"
                        + "               最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "           WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
        }
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                /* 2022/09/22 MCCM初版 MOD START */
                /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
                C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /* 2022/09/22 MCCM初版 MOD END */
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr);
            /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /* 月間プレミアムポイント数クリア */
        switch (nextmonth_3char_buf) {
            case 1:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００１ = " + mskosed_t.gekkan_premium_point_kingaku_001 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 2:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００２ = " + mskosed_t.gekkan_premium_point_kingaku_002 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 3:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００３ = " + mskosed_t.gekkan_premium_point_kingaku_003 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 4:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００４ = " + mskosed_t.gekkan_premium_point_kingaku_004 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 5:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００５ = " + mskosed_t.gekkan_premium_point_kingaku_005 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 6:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００６ = " + mskosed_t.gekkan_premium_point_kingaku_006 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 7:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００７ = " + mskosed_t.gekkan_premium_point_kingaku_007 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 8:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００８ = " + mskosed_t.gekkan_premium_point_kingaku_008 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 9:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数００９ = " + mskosed_t.gekkan_premium_point_kingaku_009 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 10:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数０１０ = " + mskosed_t.gekkan_premium_point_kingaku_010 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 11:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数０１１ = " + mskosed_t.gekkan_premium_point_kingaku_011 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 12:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数０１２ = " + mskosed_t.gekkan_premium_point_kingaku_012 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 101:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０１ = " + mskosed_t.gekkan_premium_point_kingaku_101 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 102:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０２ = " + mskosed_t.gekkan_premium_point_kingaku_102 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 103:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０３ = " + mskosed_t.gekkan_premium_point_kingaku_103 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 104:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０４ = " + mskosed_t.gekkan_premium_point_kingaku_104 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 105:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０５ = " + mskosed_t.gekkan_premium_point_kingaku_105 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 106:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０６ = " + mskosed_t.gekkan_premium_point_kingaku_106 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 107:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０７ = " + mskosed_t.gekkan_premium_point_kingaku_107 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 108:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０８ = " + mskosed_t.gekkan_premium_point_kingaku_108 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 109:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１０９ = " + mskosed_t.gekkan_premium_point_kingaku_109 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 110:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１１０ = " + mskosed_t.gekkan_premium_point_kingaku_110 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 111:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１１１ = " + mskosed_t.gekkan_premium_point_kingaku_111 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
            case 112:
                sqlca.sql.arr = " UPDATE TSランク情報"
                        + "        SET 月間プレミアムポイント数１１２ = " + mskosed_t.gekkan_premium_point_kingaku_112 + ","
                        + "            最終更新日 =" + this_date + ","
                        + "            最終更新日時 = SYSDATE(),"
                        + "            最終更新プログラムＩＤ = '" + Program_Name +"'"
                        + "        WHERE 顧客番号 = " + mskosed_t.kokyaku_no;
                break;
        }
        sqlca.restAndExecute();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                /* 2022/09/22 MCCM初版 MOD START */
                /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
                C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /* 2022/09/22 MCCM初版 MOD END */
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr);
            /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", chg_format_buf, 0, 0);
            /* 2022/09/22 MCCM初版 MOD END */
            return C_const_NG;
        }

        /* 2024/05/17 HS-0228ランククリアバッチ改修 END */

        /* 該当データを更新する */
        /* 2022/09/22 MCCM初版 MOD START */
        /*    EXEC SQL UPDATE MS顧客制度情報*/
        /*    EXEC SQL UPDATE TSランク情報*/
        /* 2022/09/22 MCCM初版 MOD END */
/*        SET 年次ランクコード０   = :mskosed_t.nenji_rank_cd_0,
            年次ランクコード１   = :mskosed_t.nenji_rank_cd_1,
            年次ランクコード２   = :mskosed_t.nenji_rank_cd_2,
            年次ランクコード３   = :mskosed_t.nenji_rank_cd_3,
            年次ランクコード４   = :mskosed_t.nenji_rank_cd_4,
            年次ランクコード５   = :mskosed_t.nenji_rank_cd_5,
            年次ランクコード６   = :mskosed_t.nenji_rank_cd_6,
            年次ランクコード７   = :mskosed_t.nenji_rank_cd_7,
            年次ランクコード８   = :mskosed_t.nenji_rank_cd_8,
            年次ランクコード９   = :mskosed_t.nenji_rank_cd_9,*/
        /* 2022/09/22 MCCM初版 ADD START */
/*            年間ランクＵＰ対象金額０   = :mskosed_t.nenkan_rankup_taisho_kingaku_0,
            年間ランクＵＰ対象金額１   = :mskosed_t.nenkan_rankup_taisho_kingaku_1,
            年間ランクＵＰ対象金額２   = :mskosed_t.nenkan_rankup_taisho_kingaku_2,
            年間ランクＵＰ対象金額３   = :mskosed_t.nenkan_rankup_taisho_kingaku_3,
            年間ランクＵＰ対象金額４   = :mskosed_t.nenkan_rankup_taisho_kingaku_4,
            年間ランクＵＰ対象金額５   = :mskosed_t.nenkan_rankup_taisho_kingaku_5,
            年間ランクＵＰ対象金額６   = :mskosed_t.nenkan_rankup_taisho_kingaku_6,
            年間ランクＵＰ対象金額７   = :mskosed_t.nenkan_rankup_taisho_kingaku_7,
            年間ランクＵＰ対象金額８   = :mskosed_t.nenkan_rankup_taisho_kingaku_8,
            年間ランクＵＰ対象金額９   = :mskosed_t.nenkan_rankup_taisho_kingaku_9,*/
        /* 2022/09/22 MCCM初版 ADD END */
        /* 2022/09/22 MCCM初版 MOD START */
/*            月次ランクコード００１   = :mskosed_t.getuji_rank_cd_001,
            月次ランクコード００２   = :mskosed_t.getuji_rank_cd_002,
            月次ランクコード００３   = :mskosed_t.getuji_rank_cd_003,
            月次ランクコード００４   = :mskosed_t.getuji_rank_cd_004,
            月次ランクコード００５   = :mskosed_t.getuji_rank_cd_005,
            月次ランクコード００６   = :mskosed_t.getuji_rank_cd_006,
            月次ランクコード００７   = :mskosed_t.getuji_rank_cd_007,
            月次ランクコード００８   = :mskosed_t.getuji_rank_cd_008,
            月次ランクコード００９   = :mskosed_t.getuji_rank_cd_009,
            月次ランクコード０１０   = :mskosed_t.getuji_rank_cd_010,
            月次ランクコード０１１   = :mskosed_t.getuji_rank_cd_011,
            月次ランクコード０１２   = :mskosed_t.getuji_rank_cd_012,
            月次ランクコード１０１   = :mskosed_t.getuji_rank_cd_101,
            月次ランクコード１０２   = :mskosed_t.getuji_rank_cd_102,
            月次ランクコード１０３   = :mskosed_t.getuji_rank_cd_103,
            月次ランクコード１０４   = :mskosed_t.getuji_rank_cd_104,
            月次ランクコード１０５   = :mskosed_t.getuji_rank_cd_105,
            月次ランクコード１０６   = :mskosed_t.getuji_rank_cd_106,
            月次ランクコード１０７   = :mskosed_t.getuji_rank_cd_107,
            月次ランクコード１０８   = :mskosed_t.getuji_rank_cd_108,
            月次ランクコード１０９   = :mskosed_t.getuji_rank_cd_109,
            月次ランクコード１１０   = :mskosed_t.getuji_rank_cd_110,
            月次ランクコード１１１   = :mskosed_t.getuji_rank_cd_111,
            月次ランクコード１１２   = :mskosed_t.getuji_rank_cd_112,*/
/*            月次ランクコード００１   = :mskosed_t.getsuji_rank_cd_001,
            月次ランクコード００２   = :mskosed_t.getsuji_rank_cd_002,
            月次ランクコード００３   = :mskosed_t.getsuji_rank_cd_003,
            月次ランクコード００４   = :mskosed_t.getsuji_rank_cd_004,
            月次ランクコード００５   = :mskosed_t.getsuji_rank_cd_005,
            月次ランクコード００６   = :mskosed_t.getsuji_rank_cd_006,
            月次ランクコード００７   = :mskosed_t.getsuji_rank_cd_007,
            月次ランクコード００８   = :mskosed_t.getsuji_rank_cd_008,
            月次ランクコード００９   = :mskosed_t.getsuji_rank_cd_009,
            月次ランクコード０１０   = :mskosed_t.getsuji_rank_cd_010,
            月次ランクコード０１１   = :mskosed_t.getsuji_rank_cd_011,
            月次ランクコード０１２   = :mskosed_t.getsuji_rank_cd_012,
            月次ランクコード１０１   = :mskosed_t.getsuji_rank_cd_101,
            月次ランクコード１０２   = :mskosed_t.getsuji_rank_cd_102,
            月次ランクコード１０３   = :mskosed_t.getsuji_rank_cd_103,
            月次ランクコード１０４   = :mskosed_t.getsuji_rank_cd_104,
            月次ランクコード１０５   = :mskosed_t.getsuji_rank_cd_105,
            月次ランクコード１０６   = :mskosed_t.getsuji_rank_cd_106,
            月次ランクコード１０７   = :mskosed_t.getsuji_rank_cd_107,
            月次ランクコード１０８   = :mskosed_t.getsuji_rank_cd_108,
            月次ランクコード１０９   = :mskosed_t.getsuji_rank_cd_109,
            月次ランクコード１１０   = :mskosed_t.getsuji_rank_cd_110,
            月次ランクコード１１１   = :mskosed_t.getsuji_rank_cd_111,
            月次ランクコード１１２   = :mskosed_t.getsuji_rank_cd_112,*/
        /* 2022/09/22 MCCM初版 MOD END */
        /* 2022/09/22 MCCM初版 ADD START */
/*            月間ランクＵＰ対象金額００１   = :mskosed_t.gekkan_rankup_taisho_kingaku_001,
            月間ランクＵＰ対象金額００２   = :mskosed_t.gekkan_rankup_taisho_kingaku_002,
            月間ランクＵＰ対象金額００３   = :mskosed_t.gekkan_rankup_taisho_kingaku_003,
            月間ランクＵＰ対象金額００４   = :mskosed_t.gekkan_rankup_taisho_kingaku_004,
            月間ランクＵＰ対象金額００５   = :mskosed_t.gekkan_rankup_taisho_kingaku_005,
            月間ランクＵＰ対象金額００６   = :mskosed_t.gekkan_rankup_taisho_kingaku_006,
            月間ランクＵＰ対象金額００７   = :mskosed_t.gekkan_rankup_taisho_kingaku_007,
            月間ランクＵＰ対象金額００８   = :mskosed_t.gekkan_rankup_taisho_kingaku_008,
            月間ランクＵＰ対象金額００９   = :mskosed_t.gekkan_rankup_taisho_kingaku_009,
            月間ランクＵＰ対象金額０１０   = :mskosed_t.gekkan_rankup_taisho_kingaku_010,
            月間ランクＵＰ対象金額０１１   = :mskosed_t.gekkan_rankup_taisho_kingaku_011,
            月間ランクＵＰ対象金額０１２   = :mskosed_t.gekkan_rankup_taisho_kingaku_012,
            月間ランクＵＰ対象金額１０１   = :mskosed_t.gekkan_rankup_taisho_kingaku_101,
            月間ランクＵＰ対象金額１０２   = :mskosed_t.gekkan_rankup_taisho_kingaku_102,
            月間ランクＵＰ対象金額１０３   = :mskosed_t.gekkan_rankup_taisho_kingaku_103,
            月間ランクＵＰ対象金額１０４   = :mskosed_t.gekkan_rankup_taisho_kingaku_104,
            月間ランクＵＰ対象金額１０５   = :mskosed_t.gekkan_rankup_taisho_kingaku_105,
            月間ランクＵＰ対象金額１０６   = :mskosed_t.gekkan_rankup_taisho_kingaku_106,
            月間ランクＵＰ対象金額１０７   = :mskosed_t.gekkan_rankup_taisho_kingaku_107,
            月間ランクＵＰ対象金額１０８   = :mskosed_t.gekkan_rankup_taisho_kingaku_108,
            月間ランクＵＰ対象金額１０９   = :mskosed_t.gekkan_rankup_taisho_kingaku_109,
            月間ランクＵＰ対象金額１１０   = :mskosed_t.gekkan_rankup_taisho_kingaku_110,
            月間ランクＵＰ対象金額１１１   = :mskosed_t.gekkan_rankup_taisho_kingaku_111,
            月間ランクＵＰ対象金額１１２   = :mskosed_t.gekkan_rankup_taisho_kingaku_112,*/
        /* 2022/09/22 MCCM初版 ADD END */
        /* 2022/12/20 MCCM初版 ADD START */
/*            月間プレミアムポイント数００１   = :mskosed_t.gekkan_premium_point_kingaku_001,
            月間プレミアムポイント数００２   = :mskosed_t.gekkan_premium_point_kingaku_002,
            月間プレミアムポイント数００３   = :mskosed_t.gekkan_premium_point_kingaku_003,
            月間プレミアムポイント数００４   = :mskosed_t.gekkan_premium_point_kingaku_004,
            月間プレミアムポイント数００５   = :mskosed_t.gekkan_premium_point_kingaku_005,
            月間プレミアムポイント数００６   = :mskosed_t.gekkan_premium_point_kingaku_006,
            月間プレミアムポイント数００７   = :mskosed_t.gekkan_premium_point_kingaku_007,
            月間プレミアムポイント数００８   = :mskosed_t.gekkan_premium_point_kingaku_008,
            月間プレミアムポイント数００９   = :mskosed_t.gekkan_premium_point_kingaku_009,
            月間プレミアムポイント数０１０   = :mskosed_t.gekkan_premium_point_kingaku_010,
            月間プレミアムポイント数０１１   = :mskosed_t.gekkan_premium_point_kingaku_011,
            月間プレミアムポイント数０１２   = :mskosed_t.gekkan_premium_point_kingaku_012,
            月間プレミアムポイント数１０１   = :mskosed_t.gekkan_premium_point_kingaku_101,
            月間プレミアムポイント数１０２   = :mskosed_t.gekkan_premium_point_kingaku_102,
            月間プレミアムポイント数１０３   = :mskosed_t.gekkan_premium_point_kingaku_103,
            月間プレミアムポイント数１０４   = :mskosed_t.gekkan_premium_point_kingaku_104,
            月間プレミアムポイント数１０５   = :mskosed_t.gekkan_premium_point_kingaku_105,
            月間プレミアムポイント数１０６   = :mskosed_t.gekkan_premium_point_kingaku_106,
            月間プレミアムポイント数１０７   = :mskosed_t.gekkan_premium_point_kingaku_107,
            月間プレミアムポイント数１０８   = :mskosed_t.gekkan_premium_point_kingaku_108,
            月間プレミアムポイント数１０９   = :mskosed_t.gekkan_premium_point_kingaku_109,
            月間プレミアムポイント数１１０   = :mskosed_t.gekkan_premium_point_kingaku_110,
            月間プレミアムポイント数１１１   = :mskosed_t.gekkan_premium_point_kingaku_111,
            月間プレミアムポイント数１１２   = :mskosed_t.gekkan_premium_point_kingaku_112,*/
        /* 2022/12/20 MCCM初版 ADD END */
        /* 2022/09/22 MCCM初版 DEL START */
        /*            バッチ更新日 = :this_date,*/
        /* 2022/09/22 MCCM初版 DEL END */
/*            最終更新日   = :this_date,
            最終更新日時 = SYSDATE,
            最終更新プログラムＩＤ = :Program_Name
        WHERE  顧客番号 = "+mskosed_t.kokyaku_no;
*/
        /*    if (sqlca.sqlcode != C_const_Ora_OK) {*/
        /*if(DBG_LOG){*/
        /*------------------------------------------------------------*/
        /* 2022/09/22 MCCM初版 MOD START */
        /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** MS顧客制度情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
        /*        C_DbgMsg("*** cmBTrankB_upd_Ksed *** TSランク情報 UPDATE NG=[%d]\n",sqlca.sqlcode );*/
        /* 2022/09/22 MCCM初版 MOD END */
        /*------------------------------------------------------------*/
        /*}*/

/*        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
        sprintf( chg_format_buf, "顧客番号=[%s]", mskosed_t.kokyaku_no.arr );*/
        /* 2022/09/22 MCCM初版 MOD START */
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "MS顧客制度情報", chg_format_buf, 0, 0);*/
/*        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                 "TSランク情報", chg_format_buf, 0, 0);*/
        /* 2022/09/22 MCCM初版 MOD END */
/*        return C_const_NG;
    }*/

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTrankB_upd_Ksed処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTrankB_upd_Ewrk                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTrankB_upd_Ewrk();                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               WSバッチ処理実行管理追加／更新処理                           */
    /*               WSバッチ処理実行管理を追加／更新し、                         */
    /*               ランククリア完了年月日にバッチ処理日を設定する。             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTrankB_upd_Ewrk() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTrankB_upd_Ewrk処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        get_cnt_buf.arr = 0;

        /* レコード件数の取得 */
        sqlca.sql.arr = "SELECT COUNT ( *) FROM WSバッチ処理実行管理" +
                "        WHERE 機能ＩＤ = 'RANK'";
//        EXEC SQL SELECT COUNT ( *)
//        INTO:
//        get_cnt_buf
//        FROM WSバッチ処理実行管理
//        WHERE 機能ＩＤ = 'RANK';
        sqlca.restAndExecute();
        sqlca.fetchInto(get_cnt_buf);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 SELECT=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 SELECT NG sqlcode=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode, "WSバッチ処理実行管理", "機能ＩＤ=[RANK]", 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 get_cnt_buf=[%d]\n", get_cnt_buf);
            C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 機能ＩＤ[RANK] シーケンス番号[%d]\n", this_date);
            /*------------------------------------------------------------*/
        }

        if (get_cnt_buf.intVal() == 0) {

            sqlca.sql.arr = "INSERT INTO WSバッチ処理実行管理" +
                    "                    (機能ＩＤ," +
                    "                            シーケンス番号" +
                    "                    ) VALUES(" +
                    "'RANK'," + this_date + ")";
            sqlca.restAndExecute();
            /* WSバッチ処理実行管理にINSERT */
//            EXEC SQL INSERT INTO WSバッチ処理実行管理
//                    (機能ＩＤ,
//                            シーケンス番号
//                    ) VALUES(
//                    'RANK',
//               :this_date
//             );

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 INSERT=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode, "WSバッチ処理実行管理", "", 0, 0);
                return C_const_NG;
            }
        } else {
            /* WSバッチ処理実行管理をUPDATE */
//            EXEC SQL UPDATE WSバッチ処理実行管理
//            SET シーケンス番号 = :this_date
//            WHERE 機能ＩＤ = 'RANK';

            sqlca.sql.arr = "UPDATE WSバッチ処理実行管理\n" +
                    "            SET シーケンス番号 = " + this_date + " WHERE 機能ＩＤ = 'RANK'";
            sqlca.restAndExecute();
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTrankB_upd_Ewrk *** WSバッチ処理実行管理 UPDATE=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode, "WSバッチ処理実行管理", "機能ＩＤ=[RANK]", 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTrankB_upd_Ewrk処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

}
