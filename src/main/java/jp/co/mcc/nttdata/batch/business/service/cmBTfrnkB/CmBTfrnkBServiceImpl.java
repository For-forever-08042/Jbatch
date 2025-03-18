package jp.co.mcc.nttdata.batch.business.service.cmBTfrnkB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTfrnkB.dto.CmBTfrnkBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

/* ******************************************************************************
 *   プログラム名   ： 家族ランククリア処理（cmBTfrnkB）
 *
 *   【処理概要】
 *     MS家族制度情報の翌々年用のランク初期設定を行う。
 *     MS家族制度情報の翌年用のランク金額初期設定を行う。
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
 *      1.00 :  2012/11/30 SSI.本田 ： 初版
 *      2.00 :  2015/01/30 SSI.上野 ： 引数追加（更新最大件数）
 *     30.00 :  2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 *                                     (顧客データロック処理内容更新のため)
 *     40.00 :  2022/09/26 SSI.川内 ： MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTfrnkBServiceImpl extends CmABfuncLServiceImpl implements CmBTfrnkBService {

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTfrnkBDto cmBTfrnkBDto = new CmBTfrnkBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int DEF_OFF = 0;                   /* OFF                                */
    int DEF_ON = 1;                   /* ON                                 */
    String DEF_ARG_C = "-c";                /* 最大取得件数                       */
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "家族ランククリア"; /* APログ用機能名                     */
    int C_LOCK_CHU = 2;      /* ロック中戻り値用                   */
    int C_SED_UPDMAX = 400000;      /* MS家族制度情報 更新最大件数        */
    int C_SED_OUTINF = 10000;      /* MS家族制度情報 出力件数情報境界    */

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

    /* *****************************************************************************/
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

    /* *****************************************************************************/
    public MainResultDto main(int argc, String[] argv) {
        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        int rtn_cd;                         /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス                 */
        int arg_cnt;                        /* 引数チェック用カウンタ             */
        IntegerDto ms_kased_upd_cnt_buf = new IntegerDto();           /* MS家族制度情報 更新件数            */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                       */
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット              */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        ms_kased_upd_cnt_buf.arr = 0;           /* MS家族制度情報 更新レコード件数    */
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
        memset(cmBTfrnkBDto.Program_Name, 0x00, 0);
        strcpy(cmBTfrnkBDto.Program_Name, Cg_Program_Name); /* プログラム名 */

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
            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
                /* 2015/01/30 引数追加 START */
            } else if (0 == memcmp(arg_Work1, DEF_ARG_C, 2)) {  /* -cの場合            */
                rtn_cd = cmBTfrnkB_Chk_Arg(arg_Work1);       /* パラメータCHK       */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_c_Value, arg_Work1.arr.substring(2));
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
            C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_SD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
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
        rtn_cd = cmBTfrnkB_main(ms_kased_upd_cnt_buf);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTfrnkB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "家族ランククリアに失敗しました", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            // EXEC SQL ROLLBACK;              /* ロールバック                       */
            sqlca.rollback();
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 各テーブル更新処理件数を出力 */
        APLOG_WT("107", 0, null, "MS家族制度情報", ms_kased_upd_cnt_buf, 0, 0, 0, 0);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** MM家族制度情報 処理件数:[%d]\n", ms_kased_upd_cnt_buf);
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
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit(C_const_APOK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTfrnkB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTfrnkB_main( int *ms_kased_upd_cnt_buf )                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               家族ランククリア処理                                         */
    /*               MS家族制度情報の翌々年のランクコードおよびランク変更日付を   */
    /*               更新する。                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int          * ms_kased_upd_cnt_buf：MS家族制度情報 更新レコード件数  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /* *****************************************************************************/
    @Override
    public int cmBTfrnkB_main(IntegerDto ms_kased_upd_cnt_buf) {
        int rtn_cd;                     /* 関数戻り値                       */
        int log_out_cnt_buf;            /* ログ出力カウンタ                 */
        int ms_kosed_lck_cnt_buf;       /* MS家族制度情報 ロックレコード件数*/
        int next2month_year;            /* 処理日付の翌々月の年             */
        int next2month;                 /* 処理日付の翌々月                 */
        /* 2022/09/26 MCCM初版 ADD START */
        int next2month_lastyear;        /* 処理日付の翌々月の前年           */
        /* 2022/09/26 MCCM初版 ADD END */
        int nextmonth_year;             /* 処理日付の翌月の年               */
        int nextmonth;                  /* 処理日付の翌月                   */
        /* 2022/09/26 MCCM初版 ADD START */
        int nextmonth_lastyear;         /* 処理日付の翌月の前年             */
        /* 2022/09/26 MCCM初版 ADD END */
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ                   */
        StringDto wk_in_next2year = new StringDto(2);
        StringDto wk_in_nextyear = new StringDto(2);
        StringDto wk_in_next2month = new StringDto(4);
        StringDto wk_in_nextmonth = new StringDto(4);
        StringDto wk_next2year = new StringDto(4);
        StringDto wk_nextyear = new StringDto(4);
        StringDto wk_next2month = new StringDto(10);
        StringDto wk_nextmonth = new StringDto(10);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTfrnkB_main処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        ms_kased_upd_cnt_buf.arr = 0;          /* MS家族制度情報 更新レコード件数    */
        log_out_cnt_buf = 0;                /* ログ出力カウンタ                   */
        ms_kosed_lck_cnt_buf = 0;           /* MS家族制度情報 ロックレコード件数  */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
        next2month_year = 0;
        nextmonth_year = 0;
        /* 2022/09/26 MCCM初版 ADD START */
        next2month_lastyear = 0;
        nextmonth_lastyear = 0;
        /* 2022/09/26 MCCM初版 ADD END */
        next2month = 0;
        nextmonth = 0;

        /* 後続処理で使用する為、先に取得しておく */
        cmBTfrnkBDto.this_date = atoi(bat_date);                 /* 処理日付(YYYYMMDD) */

        cmBTfrnkBDto.this_year = cmBTfrnkBDto.this_date / 10000;              /* 処理日付の年 */
        cmBTfrnkBDto.this_month = (cmBTfrnkBDto.this_date / 100) % 100;       /* 処理日付の月 */

        /* 2022/09/26 MCCM初版 MOD START */
//    /* 翌々月とその年を算出 */
        /* 翌々月とその年と翌々月の前年を算出 */
        /* 2022/09/26 MCCM初版 MOD END */
        next2month = cmBTfrnkBDto.this_month + 2;
        next2month_year = cmBTfrnkBDto.this_year;
        /* 2022/09/26 MCCM初版 ADD START */
        next2month_lastyear = cmBTfrnkBDto.this_year - 1;
        /* 2022/09/26 MCCM初版 ADD END */
        if (next2month > 12) {
            next2month = next2month - 12;
            next2month_year = next2month_year + 1;
            /* 2022/09/26 MCCM初版 ADD START */
            next2month_lastyear = cmBTfrnkBDto.this_year;
            /* 2022/09/26 MCCM初版 ADD END */
        }

        /* 翌月とその年を算出 */
        nextmonth = cmBTfrnkBDto.this_month + 1;
        nextmonth_year = cmBTfrnkBDto.this_year;
        /* 2022/09/26 MCCM初版 ADD START */
        nextmonth_lastyear = cmBTfrnkBDto.this_year - 1;
        /* 2022/09/26 MCCM初版 ADD END */
        if (nextmonth > 12) {
            nextmonth = nextmonth - 12;
            nextmonth_year = nextmonth_year + 1;
            /* 2022/09/26 MCCM初版 ADD START */
            nextmonth_lastyear = cmBTfrnkBDto.this_year;
            /* 2022/09/26 MCCM初版 ADD END */
        }

        /* 2022/09/26 MCCM初版 MOD START */
/*    next2year_1char_buf = ((this_date + 20000) / 10000) % 10;
    nextyear_1char_buf = ((this_date + 10000) / 10000) % 10;*/
        /* バッチ処理日付の月が<=３の場合 */
        if (cmBTfrnkBDto.this_month <= 3) {
            /* 翌年の下１桁=を算出 */
            next2year_1char_buf = ((cmBTfrnkBDto.this_date + 10000) / 10000) % 10;
            /* 今年の下１桁=を算出 */
            nextyear_1char_buf = (cmBTfrnkBDto.this_date / 10000) % 10;
        } else {
            /* 翌々年の下１桁=を算出 */
            next2year_1char_buf = ((cmBTfrnkBDto.this_date + 20000) / 10000) % 10;
            /* 翌年の下１桁=を算出 */
            nextyear_1char_buf = ((cmBTfrnkBDto.this_date + 10000) / 10000) % 10;
        }
        /* 2022/09/26 MCCM初版 MOD END */

        next2month_3char_buf = next2month;
        /* 2022/09/26 MCCM初版 ADD START */
        /* バッチ処理日付の翌々月が≦３の場合 */
        if (next2month <= 3) {
            /* バッチ処理日付の翌々月の前年が奇数年の場合 */
            if ((next2month_lastyear % 2) == 1) {
                next2month_3char_buf = next2month_3char_buf + 100;
            }
        } else {
            /* バッチ処理日付の翌々月の年が奇数年の場合 */
            /* 2022/09/26 MCCM初版 ADD END */
            if ((next2month_year % 2) == 1) {
                /* 奇数年の場合、1mmとして扱う */
                next2month_3char_buf = next2month_3char_buf + 100;
            }
            /* 2022/09/26 MCCM初版 ADD START */
        }
        /* 2022/09/26 MCCM初版 ADD END */

        nextmonth_3char_buf = nextmonth;
        /* 2022/09/26 MCCM初版 ADD START */
        /* バッチ処理日付の翌月が≦３の場合 */
        if (nextmonth <= 3) {
            /* バッチ処理日付の翌月の前年が奇数年の場合 */
            if ((nextmonth_lastyear % 2) == 1) {
                nextmonth_3char_buf = nextmonth_3char_buf + 100;
            }
        } else {
            /* バッチ処理日付の翌月の年が奇数年の場合 */
            /* 2022/09/26 MCCM初版 ADD END */
            if ((nextmonth_year % 2) == 1) {
                /* 奇数年の場合、1mmとして扱う */
                nextmonth_3char_buf = nextmonth_3char_buf + 100;
            }
            /* 2022/09/26 MCCM初版 ADD START */
        }
        /* 2022/09/26 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_main *** 当年の日付  =[%d]\n", cmBTfrnkBDto.this_date);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌々年の下１桁=[%d]\n", next2year_1char_buf);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌年の下１桁=[%d]\n", nextyear_1char_buf);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌々月=[%d]\n", next2month_3char_buf);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌月=[%d]\n", nextmonth_3char_buf);
            /*------------------------------------------------------------*/
        }

        memset(wk_in_next2year, 0x00, sizeof(wk_in_next2year));
        memset(wk_in_nextyear, 0x00, sizeof(wk_in_nextyear));
        memset(wk_in_next2month, 0x00, sizeof(wk_in_next2month));
        memset(wk_in_nextmonth, 0x00, sizeof(wk_in_nextmonth));

        sprintf(wk_in_next2year, "%d", next2year_1char_buf);
        sprintf(wk_in_nextyear, "%d", nextyear_1char_buf);
        sprintf(wk_in_next2month, "%03d", next2month_3char_buf);
        sprintf(wk_in_nextmonth, "%03d", nextmonth_3char_buf);

        /*wk_in_next2year[sizeof(wk_in_next2year) - 1] = '\0';
        wk_in_nextyear[sizeof(wk_in_nextyear) - 1] = '\0';
        wk_in_next2month[sizeof(wk_in_next2month) - 1] = '\0';
        wk_in_nextmonth[sizeof(wk_in_nextmonth) - 1] = '\0';*/

        /* 翌々年の参照カラム */
        memset(wk_next2year, 0x00, sizeof(wk_next2year));
        rtn_cd = C_ConvHalf2Full(wk_in_next2year, wk_next2year);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 翌年の参照カラム */
        memset(wk_nextyear, 0x00, sizeof(wk_nextyear));
        rtn_cd = C_ConvHalf2Full(wk_in_nextyear, wk_nextyear);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 翌々月の参照カラム */
        memset(wk_next2month, 0x00, sizeof(wk_next2month));
        rtn_cd = C_ConvHalf2Full(wk_in_next2month, wk_next2month);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 翌月の参照カラム */
        memset(wk_nextmonth, 0x00, sizeof(wk_nextmonth));
        rtn_cd = C_ConvHalf2Full(wk_in_nextmonth, wk_nextmonth);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_main *** 翌々年ランク参照カラム=[%s]\n", wk_next2year);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌年ランク金額参照カラム=[%s]\n", wk_nextyear);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌々月ランク参照カラム=[%s]\n", wk_next2month);
            C_DbgMsg("*** cmBTfrnkB_main *** 翌月ランク金額金額参照カラム=[%s]\n", wk_nextmonth);
            /*------------------------------------------------------------*/
        }

        sprintf(wk_sql, "SELECT 家族ＩＤ, " +
                        /* 2022/09/26 MCCM初版 DEL START */
                        /*           "       NVL(家族親顧客番号, 0), "*/
                        /* 2022/09/26 MCCM初版 DEL END */
                        "       NVL(家族１顧客番号, 0), " +
                        "       NVL(家族２顧客番号, 0), " +
                        "       NVL(家族３顧客番号, 0), " +
                        "       NVL(家族４顧客番号, 0), " +
                        "       NVL(家族５顧客番号, 0), " +
                        "       NVL(家族６顧客番号, 0)  " +
                        "FROM   MS家族制度情報 " +
                        "WHERE  年次ランクコード%s <> 0 " +
                        "OR     年間家族ランクＵＰ対象金額%s <> 0 " +
                        "OR     月次ランクコード%s <> 0 " +
                        "OR     月間家族ランクＵＰ金額%s <> 0 " +
                        "ORDER BY " +
                        "       家族ＩＤ ",
                wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_main *** SQL=[%s]\n", wk_sql);
            /*------------------------------------------------------------*/
        }
        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTfrnkBDto.str_sql1, wk_sql);

        /* 動的ＳＱＬ文の解析 */
        /*
        EXEC SQL PREPARE sql_frnk1 from:
        str_sql1;
        */
        SqlstmDto sqlca = sqlcaManager.get("cur_MSKA01");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTfrnkB_main *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode, "MS家族制度情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        /*
        EXEC SQL DECLARE cur_MSKA01 CURSOR FOR sql_frnk1;
        */
        sqlca.declare();

        /* カーソルオープン */
        /*EXEC SQL OPEN cur_MSKA01;*/
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode, "MS家族制度情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        /* 対象データ数分繰り返す */
        while (true) {

            /* 初期化 */
            memset(cmBTfrnkBDto.mskased_t, 0x00, 0);

            /*
            EXEC SQL FETCH cur_MSKA01
            INTO:
            mskased_t.kazoku_id,
            *//* 2022/09/26 MCCM初版 DEL START *//*
             *//*                 :mskased_t.kazoku_oya_kokyaku_no,*//*
             *//* 2022/09/26 MCCM初版 DEL END *//*
                 :mskased_t.kazoku_1_kokyaku_no,
                 :mskased_t.kazoku_2_kokyaku_no,
                 :mskased_t.kazoku_3_kokyaku_no,
                 :mskased_t.kazoku_4_kokyaku_no,
                 :mskased_t.kazoku_5_kokyaku_no,
                 :mskased_t.kazoku_6_kokyaku_no;
            */
            sqlca.fetch();
            sqlca.recData(cmBTfrnkBDto.mskased_t.kazoku_id,
                    cmBTfrnkBDto.mskased_t.kazoku_1_kokyaku_no,
                    cmBTfrnkBDto.mskased_t.kazoku_2_kokyaku_no,
                    cmBTfrnkBDto.mskased_t.kazoku_3_kokyaku_no,
                    cmBTfrnkBDto.mskased_t.kazoku_4_kokyaku_no,
                    cmBTfrnkBDto.mskased_t.kazoku_5_kokyaku_no,
                    cmBTfrnkBDto.mskased_t.kazoku_6_kokyaku_no);

            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報 FETCH NG=[%d]\n", sqlca.sqlcode);
                    /*------------------------------------------------------------*/
                }
                // EXEC SQL CLOSE cur_MSKA01;
//                sqlca.close();
                sqlcaManager.close("cur_MSKA01");
                sprintf(chg_format_buf, "翌々年ランクの下１桁=[%s]\n翌年ランクの下１桁=[%s]\n翌々月ランク=[%s]\n翌月ランク=[%s]", wk_next2year, wk_nextyear, wk_next2month, wk_nextmonth);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode, "MS家族制度情報", chg_format_buf, 0, 0)
                ;
                return C_const_NG;

            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

                /* WSバッチ処理実行管理追加／更新処理        */
                rtn_cd = cmBTfrnkB_upd_Ewrk();
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTfrnkB_main *** WSバッチ処理実行管理更新処理 NG=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
                    }
                    // EXEC SQL CLOSE cur_MSKA01;
                    sqlcaManager.close("cur_MSKA01");
                    APLOG_WT("912", 0, null, "WSバッチ処理実行管理の更新に失敗しました", 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

                // EXEC SQL COMMIT WORK;          /* コミット                       */
                CmABfuncLServiceImpl.sqlca.commit();
                break;
            }

            /* 家族制度情報ロック処理 */
            rtn_cd = cmBTfrnkB_lck_Ksed();
            if (rtn_cd != C_const_OK && rtn_cd != C_LOCK_CHU) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報 LOCK NG=[%d]\n", rtn_cd);
                    /*------------------------------------------------------------*/
                }
                // EXEC SQL CLOSE cur_MSKA01; /* カーソルクローズ                   */
                sqlcaManager.close("cur_MSKA01");
                APLOG_WT("912", 0, null, "MS家族制度情報のロックに失敗しました", 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            if (rtn_cd == C_LOCK_CHU) {
                ms_kosed_lck_cnt_buf++;     /* MS家族制度情報 ロックレコード件数 */
                continue;
            }

            /*
            EXEC SQL SELECT
                    年次ランクコード０,
                    年次ランクコード１,
                    年次ランクコード２,
                    年次ランクコード３,
                    年次ランクコード４,
                    年次ランクコード５,
                    年次ランクコード６,
                    年次ランクコード７,
                    年次ランクコード８,
                    年次ランクコード９,
                    月次ランクコード００１,
                    月次ランクコード００２,
                    月次ランクコード００３,
                    月次ランクコード００４,
                    月次ランクコード００５,
                    月次ランクコード００６,
                    月次ランクコード００７,
                    月次ランクコード００８,
                    月次ランクコード００９,
                    月次ランクコード０１０,
                    月次ランクコード０１１,
                    月次ランクコード０１２,
                    月次ランクコード１０１,
                    月次ランクコード１０２,
                    月次ランクコード１０３,
                    月次ランクコード１０４,
                    月次ランクコード１０５,
                    月次ランクコード１０６,
                    月次ランクコード１０７,
                    月次ランクコード１０８,
                    月次ランクコード１０９,
                    月次ランクコード１１０,
                    月次ランクコード１１１,
                    月次ランクコード１１２,
                    年間家族ランクＵＰ対象金額０,
                    年間家族ランクＵＰ対象金額１,
                    年間家族ランクＵＰ対象金額２,
                    年間家族ランクＵＰ対象金額３,
                    年間家族ランクＵＰ対象金額４,
                    年間家族ランクＵＰ対象金額５,
                    年間家族ランクＵＰ対象金額６,
                    年間家族ランクＵＰ対象金額７,
                    年間家族ランクＵＰ対象金額８,
                    年間家族ランクＵＰ対象金額９,
                    月間家族ランクＵＰ金額００１,
                    月間家族ランクＵＰ金額００２,
                    月間家族ランクＵＰ金額００３,
                    月間家族ランクＵＰ金額００４,
                    月間家族ランクＵＰ金額００５,
                    月間家族ランクＵＰ金額００６,
                    月間家族ランクＵＰ金額００７,
                    月間家族ランクＵＰ金額００８,
                    月間家族ランクＵＰ金額００９,
                    月間家族ランクＵＰ金額０１０,
                    月間家族ランクＵＰ金額０１１,
                    月間家族ランクＵＰ金額０１２,
                    月間家族ランクＵＰ金額１０１,
                    月間家族ランクＵＰ金額１０２,
                    月間家族ランクＵＰ金額１０３,
                    月間家族ランクＵＰ金額１０４,
                    月間家族ランクＵＰ金額１０５,
                    月間家族ランクＵＰ金額１０６,
                    月間家族ランクＵＰ金額１０７,
                    月間家族ランクＵＰ金額１０８,
                    月間家族ランクＵＰ金額１０９,
                    月間家族ランクＵＰ金額１１０,
                    月間家族ランクＵＰ金額１１１,
                    月間家族ランクＵＰ金額１１２
            INTO:
            mskased_t.nenji_rank_cd_0,
                    :mskased_t.nenji_rank_cd_1,
                    :mskased_t.nenji_rank_cd_2,
                    :mskased_t.nenji_rank_cd_3,
                    :mskased_t.nenji_rank_cd_4,
                    :mskased_t.nenji_rank_cd_5,
                    :mskased_t.nenji_rank_cd_6,
                    :mskased_t.nenji_rank_cd_7,
                    :mskased_t.nenji_rank_cd_8,
                    :mskased_t.nenji_rank_cd_9,
                    :mskased_t.getuji_rank_cd_001,
                    :mskased_t.getuji_rank_cd_002,
                    :mskased_t.getuji_rank_cd_003,
                    :mskased_t.getuji_rank_cd_004,
                    :mskased_t.getuji_rank_cd_005,
                    :mskased_t.getuji_rank_cd_006,
                    :mskased_t.getuji_rank_cd_007,
                    :mskased_t.getuji_rank_cd_008,
                    :mskased_t.getuji_rank_cd_009,
                    :mskased_t.getuji_rank_cd_010,
                    :mskased_t.getuji_rank_cd_011,
                    :mskased_t.getuji_rank_cd_012,
                    :mskased_t.getuji_rank_cd_101,
                    :mskased_t.getuji_rank_cd_102,
                    :mskased_t.getuji_rank_cd_103,
                    :mskased_t.getuji_rank_cd_104,
                    :mskased_t.getuji_rank_cd_105,
                    :mskased_t.getuji_rank_cd_106,
                    :mskased_t.getuji_rank_cd_107,
                    :mskased_t.getuji_rank_cd_108,
                    :mskased_t.getuji_rank_cd_109,
                    :mskased_t.getuji_rank_cd_110,
                    :mskased_t.getuji_rank_cd_111,
                    :mskased_t.getuji_rank_cd_112,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,
                    :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,
                    :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112
            FROM MS家族制度情報
            WHERE 家族ＩＤ = :mskased_t.kazoku_id;
            */
            StringDto workSql = new StringDto();
            workSql.arr = "SELECT" +
                    "                    年次ランクコード０," +
                    "                    年次ランクコード１," +
                    "                    年次ランクコード２," +
                    "                    年次ランクコード３," +
                    "                    年次ランクコード４," +
                    "                    年次ランクコード５," +
                    "                    年次ランクコード６," +
                    "                    年次ランクコード７," +
                    "                    年次ランクコード８," +
                    "                    年次ランクコード９," +
                    "                    月次ランクコード００１," +
                    "                    月次ランクコード００２," +
                    "                    月次ランクコード００３," +
                    "                    月次ランクコード００４," +
                    "                    月次ランクコード００５," +
                    "                    月次ランクコード００６," +
                    "                    月次ランクコード００７," +
                    "                    月次ランクコード００８," +
                    "                    月次ランクコード００９," +
                    "                    月次ランクコード０１０," +
                    "                    月次ランクコード０１１," +
                    "                    月次ランクコード０１２," +
                    "                    月次ランクコード１０１," +
                    "                    月次ランクコード１０２," +
                    "                    月次ランクコード１０３," +
                    "                    月次ランクコード１０４," +
                    "                    月次ランクコード１０５," +
                    "                    月次ランクコード１０６," +
                    "                    月次ランクコード１０７," +
                    "                    月次ランクコード１０８," +
                    "                    月次ランクコード１０９," +
                    "                    月次ランクコード１１０," +
                    "                    月次ランクコード１１１," +
                    "                    月次ランクコード１１２," +
                    "                    年間家族ランクＵＰ対象金額０," +
                    "                    年間家族ランクＵＰ対象金額１," +
                    "                    年間家族ランクＵＰ対象金額２," +
                    "                    年間家族ランクＵＰ対象金額３," +
                    "                    年間家族ランクＵＰ対象金額４," +
                    "                    年間家族ランクＵＰ対象金額５," +
                    "                    年間家族ランクＵＰ対象金額６," +
                    "                    年間家族ランクＵＰ対象金額７," +
                    "                    年間家族ランクＵＰ対象金額８," +
                    "                    年間家族ランクＵＰ対象金額９," +
                    "                    月間家族ランクＵＰ金額００１," +
                    "                    月間家族ランクＵＰ金額００２," +
                    "                    月間家族ランクＵＰ金額００３," +
                    "                    月間家族ランクＵＰ金額００４," +
                    "                    月間家族ランクＵＰ金額００５," +
                    "                    月間家族ランクＵＰ金額００６," +
                    "                    月間家族ランクＵＰ金額００７," +
                    "                    月間家族ランクＵＰ金額００８," +
                    "                    月間家族ランクＵＰ金額００９," +
                    "                    月間家族ランクＵＰ金額０１０," +
                    "                    月間家族ランクＵＰ金額０１１," +
                    "                    月間家族ランクＵＰ金額０１２," +
                    "                    月間家族ランクＵＰ金額１０１," +
                    "                    月間家族ランクＵＰ金額１０２," +
                    "                    月間家族ランクＵＰ金額１０３," +
                    "                    月間家族ランクＵＰ金額１０４," +
                    "                    月間家族ランクＵＰ金額１０５," +
                    "                    月間家族ランクＵＰ金額１０６," +
                    "                    月間家族ランクＵＰ金額１０７," +
                    "                    月間家族ランクＵＰ金額１０８," +
                    "                    月間家族ランクＵＰ金額１０９," +
                    "                    月間家族ランクＵＰ金額１１０," +
                    "                    月間家族ランクＵＰ金額１１１," +
                    "                    月間家族ランクＵＰ金額１１２" +
                    "            FROM MS家族制度情報" +
                    "            WHERE 家族ＩＤ = ?";
            CmABfuncLServiceImpl.sqlca.sql = workSql;
            CmABfuncLServiceImpl.sqlca.restAndExecute(cmBTfrnkBDto.mskased_t.kazoku_id);
            CmABfuncLServiceImpl.sqlca.fetch();
            CmABfuncLServiceImpl.sqlca.recData(cmBTfrnkBDto.mskased_t.nenji_rank_cd_0,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_1,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_2,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_3,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_4,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_5,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_6,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_7,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_8,
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_9,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_001,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_002,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_003,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_004,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_005,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_006,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_007,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_008,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_009,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_010,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_011,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_012,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_101,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_102,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_103,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_104,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_105,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_106,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_107,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_108,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_109,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_110,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_111,
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_112,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112);

            if (CmABfuncLServiceImpl.sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報 SELECT NG=[%d]\n", CmABfuncLServiceImpl.sqlca.sqlcode);
                    /*------------------------------------------------------------*/
                }
                // EXEC SQL CLOSE cur_MSKA01;
                sqlcaManager.close("cur_MSKA01");
                /* 2022/09/26 MCCM初版 MOD START */
                /*            sprintf(chg_format_buf, "家族ＩＤ=[%d]", mskased_t.kazoku_id);*/
                sprintf(chg_format_buf, "家族ＩＤ=[%s]", cmBTfrnkBDto.mskased_t.kazoku_id.arr);
                /* 2022/09/26 MCCM初版 MOD END */
                APLOG_WT("904", 0, null, "SELECT", CmABfuncLServiceImpl.sqlca.sqlcode, "MS家族制度情報", chg_format_buf, 0, 0)
                ;

                return C_const_NG;

            }

            /* 翌々年のランクコードの設定 */
            switch (next2year_1char_buf) {
                case 0:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_0.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 1:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_1.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 2:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_2.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 3:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_3.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 4:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_4.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 5:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_5.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 6:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_6.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 7:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_7.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 8:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_8.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 9:
                    cmBTfrnkBDto.mskased_t.nenji_rank_cd_9.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
            }

            /* 翌年のランクＵＰ金額の設定 */
            switch (nextyear_1char_buf) {
                case 0:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.arr = (double) 0;
                    break;
                case 1:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.arr = (double) 0;
                    break;
                case 2:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.arr = (double) 0;
                    break;
                case 3:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.arr = (double) 0;
                    break;
                case 4:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.arr = (double) 0;
                    break;
                case 5:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.arr = (double) 0;
                    break;
                case 6:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.arr = (double) 0;
                    break;
                case 7:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.arr = (double) 0;
                    break;
                case 8:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.arr = (double) 0;
                    break;
                case 9:
                    cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.arr = (double) 0;
                    break;
            }

            /* 翌々月のランクコードの設定 */
            switch (next2month_3char_buf) {
                case 1:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_001.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 2:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_002.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 3:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_003.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 4:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_004.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 5:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_005.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 6:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_006.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 7:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_007.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 8:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_008.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 9:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_009.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 10:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_010.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 11:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_011.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 12:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_012.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 101:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_101.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 102:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_102.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 103:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_103.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 104:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_104.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 105:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_105.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 106:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_106.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 107:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_107.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 108:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_108.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 109:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_109.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 110:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_110.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 111:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_111.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
                case 112:
                    cmBTfrnkBDto.mskased_t.getuji_rank_cd_112.arr = BT_aplcomService.C_BOTTOM_RANK;
                    break;
            }

            /* 翌月のランクＵＰ金額の設定 */
            switch (nextmonth_3char_buf) {
                case 1:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.arr = (double) 0;
                    break;
                case 2:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.arr = (double) 0;
                    break;
                case 3:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.arr = (double) 0;
                    break;
                case 4:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.arr = (double) 0;
                    break;
                case 5:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.arr = (double) 0;
                    break;
                case 6:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.arr = (double) 0;
                    break;
                case 7:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.arr = (double) 0;
                    break;
                case 8:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.arr = (double) 0;
                    break;
                case 9:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.arr = (double) 0;
                    break;
                case 10:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.arr = (double) 0;
                    break;
                case 11:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.arr = (double) 0;
                    break;
                case 12:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.arr = (double) 0;
                    break;
                case 101:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.arr = (double) 0;
                    break;
                case 102:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.arr = (double) 0;
                    break;
                case 103:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.arr = (double) 0;
                    break;
                case 104:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.arr = (double) 0;
                    break;
                case 105:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.arr = (double) 0;
                    break;
                case 106:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.arr = (double) 0;
                    break;
                case 107:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.arr = (double) 0;
                    break;
                case 108:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.arr = (double) 0;
                    break;
                case 109:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.arr = (double) 0;
                    break;
                case 110:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.arr = (double) 0;
                    break;
                case 111:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.arr = (double) 0;
                    break;
                case 112:
                    cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.arr = (double) 0;
                    break;
            }

            /* 家族制度情報更新処理 */
            rtn_cd = cmBTfrnkB_upd_Ksed();
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報更新処理 NG=[%d]\n", rtn_cd);
                    /*------------------------------------------------------------*/
                }
                // EXEC SQL CLOSE cur_MSKA01; /* カーソルクローズ                   */
                sqlcaManager.close("cur_MSKA01");
                APLOG_WT("912", 0, null, "MS家族制度情報の更新に失敗しました", 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            // EXEC SQL COMMIT WORK;           /* コミット                           */
            CmABfuncLServiceImpl.sqlca.commit();

            /* ログ出力カウンタアップ */
            log_out_cnt_buf++;

            /* 5000件毎メッセージ出力 */
            if (log_out_cnt_buf >= C_SED_OUTINF) {
                APLOG_WT("107", 0, null, "MS家族制度情報", log_out_cnt_buf, 0, 0, 0, 0);
                log_out_cnt_buf = 0;        /* ログ出力カウンタ０クリア           */
            }

            /* MS家族制度情報 更新件数アップ */
            ms_kased_upd_cnt_buf.arr = ms_kased_upd_cnt_buf.arr + 1;

            /* MS家族制度情報 更新件数の最大件数チェック（1処理更新最大件数まで） */
            if (ms_kased_upd_cnt_buf.arr >= g_max_data_count) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報更新件数=[%d]\n", ms_kased_upd_cnt_buf);
                    /*------------------------------------------------------------*/
                }
                break;
            }

        } /* FETCH LOOP */

        // EXEC SQL CLOSE cur_MSKA01;
        sqlcaManager.close("cur_MSKA01");

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_main *** MS家族制度情報 ロック中件数=[%d]\n", ms_kosed_lck_cnt_buf);
            /*------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTfrnkB_main処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTfrnkB_lck_Ksed                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTfrnkB_lck_Ksed()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               家族制度情報ロック処理                                       */
    /*               家族制度情報のレコードをNOWAITでロックをかける。             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              2   ： ロック中                                               */
    /*                                                                            */

    /* *****************************************************************************/
    @Override
    public int cmBTfrnkB_lck_Ksed() {
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス                 */
        int rtn_cd;                         /* 関数戻り値                         */
        /* 2022/09/26 MCCM初版 MOD START */
        /*char    kazoku_kokyaku_wk[5][15+1];*/     /* 家族n顧客番号 + \0                 */
        StringDto[] kazoku_kokyaku_wk = new StringDto[6];     /* 家族n顧客番号 + \0                 */
        /* 2022/09/26 MCCM初版 MOD END */
        StringDto lock_kokyaku_no = new StringDto(15 + 1);          /* 家族顧客番号のロック用             */
        int i;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTfrnkB_lck_Ksed処理");
            /*------------------------------------------------------------*/
        }

        for (int u = 0; u < kazoku_kokyaku_wk.length; u++) {
            kazoku_kokyaku_wk[u] = new StringDto();
        }
        /*memset(kazoku_kokyaku_wk[0], 0x00, sizeof(kazoku_kokyaku_wk[0]));
        memset(kazoku_kokyaku_wk[1], 0x00, sizeof(kazoku_kokyaku_wk[1]));
        memset(kazoku_kokyaku_wk[2], 0x00, sizeof(kazoku_kokyaku_wk[2]));
        memset(kazoku_kokyaku_wk[3], 0x00, sizeof(kazoku_kokyaku_wk[3]));
        memset(kazoku_kokyaku_wk[4], 0x00, sizeof(kazoku_kokyaku_wk[4]));
        memset(kazoku_kokyaku_wk[5], 0x00, sizeof(kazoku_kokyaku_wk[5]));*/
        /* 2022/09/26 MCCM初版 DEL START */
        /*    memset(kazoku_kokyaku_wk[4], 0x00, sizeof(kazoku_kokyaku_wk[4]));*/
        /* 2022/09/26 MCCM初版 DEL END */

        /* 2022/09/26 MCCM初版 MOD START */
/*    sprintf(kazoku_kokyaku_wk[0], "%015ld", atol((char *)mskased_t.kazoku_oya_kokyaku_no.arr));
    sprintf(kazoku_kokyaku_wk[1], "%015ld", atol((char *)mskased_t.kazoku_1_kokyaku_no.arr));
    sprintf(kazoku_kokyaku_wk[2], "%015ld", atol((char *)mskased_t.kazoku_2_kokyaku_no.arr));
    sprintf(kazoku_kokyaku_wk[3], "%015ld", atol((char *)mskased_t.kazoku_3_kokyaku_no.arr));
    sprintf(kazoku_kokyaku_wk[4], "%015ld", atol((char *)mskased_t.kazoku_4_kokyaku_no.arr));*/
        sprintf(kazoku_kokyaku_wk[0], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_1_kokyaku_no));
        sprintf(kazoku_kokyaku_wk[1], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_2_kokyaku_no));
        sprintf(kazoku_kokyaku_wk[2], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_3_kokyaku_no));
        sprintf(kazoku_kokyaku_wk[3], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_4_kokyaku_no));
        sprintf(kazoku_kokyaku_wk[4], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_5_kokyaku_no));
        sprintf(kazoku_kokyaku_wk[5], "%015d", atol(cmBTfrnkBDto.mskased_t.kazoku_6_kokyaku_no));
        /* 2022/09/26 MCCM初版 MOD END */

        /* ワークをソートする */
        /* 2022/09/26 MCCM初版 MOD START */
        /*    qsort(kazoku_kokyaku_wk, 5, sizeof(kazoku_kokyaku_wk[0]), (int(*)(const void*, const void*))strcmp);*/
        kazoku_kokyaku_wk = qsort(kazoku_kokyaku_wk, 6, kazoku_kokyaku_wk.length);
        /* 2022/09/26 MCCM初版 MOD END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid0=[%s]\n", kazoku_kokyaku_wk[0]);
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid1=[%s]\n", kazoku_kokyaku_wk[1]);
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid2=[%s]\n", kazoku_kokyaku_wk[2]);
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid3=[%s]\n", kazoku_kokyaku_wk[3]);
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid4=[%s]\n", kazoku_kokyaku_wk[4]);
            C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid5=[%s]\n", kazoku_kokyaku_wk[5]);
            /* 2022/09/26 MCCM初版 DEL START */
            /*    C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock uid4=[%s]\n", kazoku_kokyaku_wk[4]);*/
            /* 2022/09/26 MCCM初版 DEL END */
            /*------------------------------------------------------------*/
        }

        /* 2022/09/26 MCCM初版 MOD START */
        /*    for (i = 0; i < 5; i++) {*/
        for (i = 0; i < 6; i++) {
            /* 2022/09/26 MCCM初版 MOD END */
            if (memcmp(kazoku_kokyaku_wk[i].arr.replaceAll("\\[", "")
                            .replaceAll("]", "")
                            .replaceAll(",", "")
                    , "000000000000000", 15) != 0) {
                memset(lock_kokyaku_no, 0x00, sizeof(lock_kokyaku_no));
                memcpy(lock_kokyaku_no, kazoku_kokyaku_wk[i].arr.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll(",", ""),
                        15);
                /* 顧客ロック処理 */
                rtn_cd = C_KdataLock(lock_kokyaku_no, "1", rtn_status);

                if (sqlca.sqlcode == BT_aplcomService.C_ORA_BUSY_NOWAIT) {
                    /* ロック中 */
                    return C_LOCK_CHU;
                } else if (rtn_cd == C_const_NOTEXISTS) {
                    /* 該当顧客なし */
                    APLOG_WT("913", 0, null, lock_kokyaku_no, 0, 0, 0, 0, 0);
                    return C_const_NG;
                } else if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock NG rtn_cd=[%d]\n", rtn_cd);
                        C_DbgMsg("*** cmBTfrnkB_lck_Ksed *** C_KdataLock NG rtn_status=[%d]\n", rtn_status);
                        /*------------------------------------------------------------*/
                    }

                    APLOG_WT("903", 0, null, "C_KdataLock",
                            rtn_cd, rtn_status, 0, 0, 0);
                    return C_const_NG;
                }
            }
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTfrnkB_lck_Ksed処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTfrnkB_upd_Ksed                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTfrnkB_upd_Ksed()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               家族制度情報更新処理                                         */
    /*               家族制度情報のレコードの更新を行う。                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /* *****************************************************************************/
    @Override
    public int cmBTfrnkB_upd_Ksed() {
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTfrnkB_upd_Ksed処理");
            /*------------------------------------------------------------*/
        }

        /*
        EXEC SQL UPDATE MS家族制度情報
        SET 年次ランクコード０ = :mskased_t.nenji_rank_cd_0,
                年次ランクコード１ = :mskased_t.nenji_rank_cd_1,
                年次ランクコード２ = :mskased_t.nenji_rank_cd_2,
                年次ランクコード３ = :mskased_t.nenji_rank_cd_3,
                年次ランクコード４ = :mskased_t.nenji_rank_cd_4,
                年次ランクコード５ = :mskased_t.nenji_rank_cd_5,
                年次ランクコード６ = :mskased_t.nenji_rank_cd_6,
                年次ランクコード７ = :mskased_t.nenji_rank_cd_7,
                年次ランクコード８ = :mskased_t.nenji_rank_cd_8,
                年次ランクコード９ = :mskased_t.nenji_rank_cd_9,
                月次ランクコード００１ = :mskased_t.getuji_rank_cd_001,
                月次ランクコード００２ = :mskased_t.getuji_rank_cd_002,
                月次ランクコード００３ = :mskased_t.getuji_rank_cd_003,
                月次ランクコード００４ = :mskased_t.getuji_rank_cd_004,
                月次ランクコード００５ = :mskased_t.getuji_rank_cd_005,
                月次ランクコード００６ = :mskased_t.getuji_rank_cd_006,
                月次ランクコード００７ = :mskased_t.getuji_rank_cd_007,
                月次ランクコード００８ = :mskased_t.getuji_rank_cd_008,
                月次ランクコード００９ = :mskased_t.getuji_rank_cd_009,
                月次ランクコード０１０ = :mskased_t.getuji_rank_cd_010,
                月次ランクコード０１１ = :mskased_t.getuji_rank_cd_011,
                月次ランクコード０１２ = :mskased_t.getuji_rank_cd_012,
                月次ランクコード１０１ = :mskased_t.getuji_rank_cd_101,
                月次ランクコード１０２ = :mskased_t.getuji_rank_cd_102,
                月次ランクコード１０３ = :mskased_t.getuji_rank_cd_103,
                月次ランクコード１０４ = :mskased_t.getuji_rank_cd_104,
                月次ランクコード１０５ = :mskased_t.getuji_rank_cd_105,
                月次ランクコード１０６ = :mskased_t.getuji_rank_cd_106,
                月次ランクコード１０７ = :mskased_t.getuji_rank_cd_107,
                月次ランクコード１０８ = :mskased_t.getuji_rank_cd_108,
                月次ランクコード１０９ = :mskased_t.getuji_rank_cd_109,
                月次ランクコード１１０ = :mskased_t.getuji_rank_cd_110,
                月次ランクコード１１１ = :mskased_t.getuji_rank_cd_111,
                月次ランクコード１１２ = :mskased_t.getuji_rank_cd_112,
                年間家族ランクＵＰ対象金額０ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,
                年間家族ランクＵＰ対象金額１ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,
                年間家族ランクＵＰ対象金額２ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,
                年間家族ランクＵＰ対象金額３ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,
                年間家族ランクＵＰ対象金額４ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,
                年間家族ランクＵＰ対象金額５ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,
                年間家族ランクＵＰ対象金額６ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,
                年間家族ランクＵＰ対象金額７ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,
                年間家族ランクＵＰ対象金額８ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,
                年間家族ランクＵＰ対象金額９ = :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,
                月間家族ランクＵＰ金額００１ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,
                月間家族ランクＵＰ金額００２ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,
                月間家族ランクＵＰ金額００３ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,
                月間家族ランクＵＰ金額００４ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,
                月間家族ランクＵＰ金額００５ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,
                月間家族ランクＵＰ金額００６ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,
                月間家族ランクＵＰ金額００７ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,
                月間家族ランクＵＰ金額００８ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,
                月間家族ランクＵＰ金額００９ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,
                月間家族ランクＵＰ金額０１０ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,
                月間家族ランクＵＰ金額０１１ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,
                月間家族ランクＵＰ金額０１２ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,
                月間家族ランクＵＰ金額１０１ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,
                月間家族ランクＵＰ金額１０２ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,
                月間家族ランクＵＰ金額１０３ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,
                月間家族ランクＵＰ金額１０４ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,
                月間家族ランクＵＰ金額１０５ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,
                月間家族ランクＵＰ金額１０６ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,
                月間家族ランクＵＰ金額１０７ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,
                月間家族ランクＵＰ金額１０８ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,
                月間家族ランクＵＰ金額１０９ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,
                月間家族ランクＵＰ金額１１０ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,
                月間家族ランクＵＰ金額１１１ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,
                月間家族ランクＵＰ金額１１２ = :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112,
                バッチ更新日 = :this_date,
                最終更新日 = :this_date,
                最終更新日時 = SYSDATE,
                最終更新プログラムＩＤ = :Program_Name
        WHERE 家族ＩＤ = :mskased_t.kazoku_id;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "UPDATE MS家族制度情報" +
                "        SET 年次ランクコード０ = ?," +
                "                年次ランクコード１ = ?," +
                "                年次ランクコード２ = ?," +
                "                年次ランクコード３ = ?," +
                "                年次ランクコード４ = ?," +
                "                年次ランクコード５ = ?," +
                "                年次ランクコード６ = ?," +
                "                年次ランクコード７ = ?," +
                "                年次ランクコード８ = ?," +
                "                年次ランクコード９ = ?," +
                "                月次ランクコード００１ = ?," +
                "                月次ランクコード００２ = ?," +
                "                月次ランクコード００３ = ?," +
                "                月次ランクコード００４ = ?," +
                "                月次ランクコード００５ = ?," +
                "                月次ランクコード００６ = ?," +
                "                月次ランクコード００７ = ?," +
                "                月次ランクコード００８ = ?," +
                "                月次ランクコード００９ = ?," +
                "                月次ランクコード０１０ = ?," +
                "                月次ランクコード０１１ = ?," +
                "                月次ランクコード０１２ = ?," +
                "                月次ランクコード１０１ = ?," +
                "                月次ランクコード１０２ = ?," +
                "                月次ランクコード１０３ = ?," +
                "                月次ランクコード１０４ = ?," +
                "                月次ランクコード１０５ = ?," +
                "                月次ランクコード１０６ = ?," +
                "                月次ランクコード１０７ = ?," +
                "                月次ランクコード１０８ = ?," +
                "                月次ランクコード１０９ = ?," +
                "                月次ランクコード１１０ = ?," +
                "                月次ランクコード１１１ = ?," +
                "                月次ランクコード１１２ = ?," +
                "                年間家族ランクＵＰ対象金額０ = ?," +
                "                年間家族ランクＵＰ対象金額１ = ?," +
                "                年間家族ランクＵＰ対象金額２ = ?," +
                "                年間家族ランクＵＰ対象金額３ = ?," +
                "                年間家族ランクＵＰ対象金額４ = ?," +
                "                年間家族ランクＵＰ対象金額５ = ?," +
                "                年間家族ランクＵＰ対象金額６ = ?," +
                "                年間家族ランクＵＰ対象金額７ = ?," +
                "                年間家族ランクＵＰ対象金額８ = ?," +
                "                年間家族ランクＵＰ対象金額９ = ?," +
                "                月間家族ランクＵＰ金額００１ = ?," +
                "                月間家族ランクＵＰ金額００２ = ?," +
                "                月間家族ランクＵＰ金額００３ = ?," +
                "                月間家族ランクＵＰ金額００４ = ?," +
                "                月間家族ランクＵＰ金額００５ = ?," +
                "                月間家族ランクＵＰ金額００６ = ?," +
                "                月間家族ランクＵＰ金額００７ = ?," +
                "                月間家族ランクＵＰ金額００８ = ?," +
                "                月間家族ランクＵＰ金額００９ = ?," +
                "                月間家族ランクＵＰ金額０１０ = ?," +
                "                月間家族ランクＵＰ金額０１１ = ?," +
                "                月間家族ランクＵＰ金額０１２ = ?," +
                "                月間家族ランクＵＰ金額１０１ = ?," +
                "                月間家族ランクＵＰ金額１０２ = ?," +
                "                月間家族ランクＵＰ金額１０３ = ?," +
                "                月間家族ランクＵＰ金額１０４ = ?," +
                "                月間家族ランクＵＰ金額１０５ = ?," +
                "                月間家族ランクＵＰ金額１０６ = ?," +
                "                月間家族ランクＵＰ金額１０７ = ?," +
                "                月間家族ランクＵＰ金額１０８ = ?," +
                "                月間家族ランクＵＰ金額１０９ = ?," +
                "                月間家族ランクＵＰ金額１１０ = ?," +
                "                月間家族ランクＵＰ金額１１１ = ?," +
                "                月間家族ランクＵＰ金額１１２ = ?," +
                "                バッチ更新日 = ?," +
                "                最終更新日 = ?," +
                "                最終更新日時 = SYSDATE()," +
                "                最終更新プログラムＩＤ = ?" +
                "        WHERE 家族ＩＤ = ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTfrnkBDto.mskased_t.nenji_rank_cd_0,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_1,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_2,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_3,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_4,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_5,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_6,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_7,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_8,
                cmBTfrnkBDto.mskased_t.nenji_rank_cd_9,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_001,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_002,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_003,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_004,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_005,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_006,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_007,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_008,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_009,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_010,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_011,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_012,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_101,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_102,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_103,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_104,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_105,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_106,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_107,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_108,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_109,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_110,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_111,
                cmBTfrnkBDto.mskased_t.getuji_rank_cd_112,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,
                cmBTfrnkBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,
                cmBTfrnkBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112,
                cmBTfrnkBDto.this_date,
                cmBTfrnkBDto.this_date,
                cmBTfrnkBDto.Program_Name,
                cmBTfrnkBDto.mskased_t.kazoku_id);


        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTfrnkB_upd_Ksed *** MS家族制度情報 UPDATE NG=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            /* 2022/09/26 MCCM初版 MOD START */
            /*        sprintf( chg_format_buf, "家族ＩＤ=[%d]", mskased_t.kazoku_id );*/
            sprintf(chg_format_buf, "家族ＩＤ=[%s]", cmBTfrnkBDto.mskased_t.kazoku_id.arr);
            /* 2022/09/26 MCCM初版 MOD END */
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "MS家族制度情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTfrnkB_upd_Ksed処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTfrnkB_upd_Ewrk                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTfrnkB_upd_Ewrk()                                          */
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

    /* *****************************************************************************/
    @Override
    public int cmBTfrnkB_upd_Ewrk() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTfrnkB_upd_Ewrk処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        ItemDto get_cnt_buf = new ItemDto();

        /* レコード件数の取得 */
        /*
        EXEC SQL SELECT COUNT ( *)
        INTO:
        get_cnt_buf
        FROM WSバッチ処理実行管理
        WHERE 機能ＩＤ = 'FRNK';
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT COUNT ( *) FROM WSバッチ処理実行管理 WHERE 機能ＩＤ = 'FRNK' " ;
        sqlca.sql = workSql;
        sqlca.restAndExecute();
        sqlca.fetch();
        sqlca.recData(get_cnt_buf);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 SELECT=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 SELECT NG sqlcode=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("904", 0, null, "SELECT",
                    sqlca.sqlcode, "WSバッチ処理実行管理", "機能ＩＤ=[FRNK]", 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 get_cnt_buf=[%d]\n", get_cnt_buf);
            C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 機能ＩＤ[FRNK] シーケンス番号[%d]\n", cmBTfrnkBDto.this_date);
            /*------------------------------------------------------------*/
        }

        if (get_cnt_buf.intVal() == 0) {
            /* WSバッチ処理実行管理にINSERT */
            /*
            EXEC SQL INSERT INTO WSバッチ処理実行管理
                    (機能ＩＤ,
                            シーケンス番号
                    ) VALUES(
                    'FRNK',
               :this_date
             );
            */
            workSql.arr = "INSERT INTO WSバッチ処理実行管理" +
                    "                    (機能ＩＤ," +
                    "                            シーケンス番号" +
                    "                    ) VALUES(" +
                    "                    'FRNK'," +
                    "               ?" +
                    "             )";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTfrnkBDto.this_date);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 INSERT=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode, "WSバッチ処理実行管理", "", 0, 0);
                return C_const_NG;
            }

        } else {
            /* WSバッチ処理実行管理をUPDATE */
            /*
            EXEC SQL UPDATE WSバッチ処理実行管理
            SET シーケンス番号 = :this_date
            WHERE 機能ＩＤ = 'FRNK';
            */
            workSql.arr = "UPDATE WSバッチ処理実行管理" +
                    "            SET シーケンス番号 = ?" +
                    "            WHERE 機能ＩＤ = 'FRNK'";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTfrnkBDto.this_date);
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTfrnkB_upd_Ewrk *** WSバッチ処理実行管理 UPDATE=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
            }
            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT("904", 0, null, "UPDATE",
                        sqlca.sqlcode, "WSバッチ処理実行管理", "機能ＩＤ=[FRNK]", 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTfrnkB_upd_Ewrk処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTfrnkB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTfrnkB_Chk_Arg( char *Arg_in )                             */
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

    /* *****************************************************************************/
    @Override
    public int cmBTfrnkB_Chk_Arg(StringDto Arg_in) {
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット              */
        StringDto arg_work = new StringDto(256);                     /* 引数チェック用エリア            */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTfrnkB_Chk_Arg処理");
            C_DbgMsg("*** cmBTfrnkB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
        memset(arg_work, 0x00, sizeof(arg_work));

        if (0 == memcmp(Arg_in, DEF_ARG_C, 2)) {        /* -c更新最大件数         */
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

            strcpy(arg_work, Arg_in.arr.substring(2));
            if (atol(arg_work) <= 0) {                /* 数字チェック           */
                sprintf(chg_format_buf, "-c 引数の値が不正です[数字以外]（%s）", Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTfrnkB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }
}
