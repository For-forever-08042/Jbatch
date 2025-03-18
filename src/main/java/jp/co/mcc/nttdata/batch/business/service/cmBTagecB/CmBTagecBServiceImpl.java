package jp.co.mcc.nttdata.batch.business.service.cmBTagecB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MM_KOKYAKU_INFO_TBL;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;

/*******************************************************************************
 *   プログラム名   ： 年齢計算（cmBTagecB）
 *
 *   【処理概要】
 *       MM顧客情報、MMマタニティベビー情報より、
 *       前日誕生日である対象顧客を抽出し、新年齢計算を行い年齢更新する。
 *
 *   【引数説明】
 *    -DEBUG(-debug) :  デバッグモードでの実行
 *                     （トレース出力機能が有効）
 *
 *   【戻り値】
 *      10    ： 正常
 *      99    ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/12/12 SSI.吉岡 ： 初版
 *     30.00 :  2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 *     40.00 :  2022/09/30 SSI.川内 ： MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTagecBServiceImpl extends CmABfuncLServiceImpl implements CmBTagecBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */

    boolean DBG_LOG = true;                    /* デバッグメッセージ出力     */
    MM_KOKYAKU_INFO_TBL mmkoinf_t;            /* MM顧客情報バッファ         */

    int h_shori_yday;                 /* バッチ処理日付(前日)       */
    int h_shori_tday;                 /* バッチ処理日付(当日)       */
    ItemDto h_saishu_koshin_programid = new ItemDto(20 + 1);
    /* 最終更新プログラムID       */

    int h_ymd;                        /* 年月日                     */
    int h_md;                         /* 月日                       */
    int h_year;                       /* 年                         */
    int h_month;                      /* 月                         */
    int h_day;                        /* 日                         */
    int h_leap_ymd;                   /* 年月日(閏年)               */
    int h_leap_md;                    /* 月日(閏年)                 */
    int h_leap_month;                 /* 年(閏年)                   */
    int h_leap_day;                   /* 日(閏年)                   */

    StringDto str_sql = new StringDto(4096);                /* 実行用SQL文字列            */


    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;               /* OFF                          */
    int DEF_ON = 1;               /* ON                           */
    String PG_NAME = "年齢計算";           /* プログラム名称               */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ             */
    String DEF_debug = "-debug";        /* デバッグスイッチ             */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int in_mmko_cnt;
    /**
     * MM顧客情報_処理対象件数
     **/
    int ok_mmko_cnt;
    /**
     * MM顧客情報_正常処理件数
     **/


    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);   /** APログフォーマット  **/

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

        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                         /** 関数戻り値                       **/
        IntegerDto rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto arg_Work1 = new StringDto(256);                /** Work Buffer1                     **/

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /*-----------------------------------------------*/
        /*  プログラム名取得処理                         */
        /*-----------------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
           return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* HOST変数にバージョン付きプログラム名をコピーする */
        memset(h_saishu_koshin_programid, 0x00,
                sizeof(h_saishu_koshin_programid));
        memcpy(h_saishu_koshin_programid, Cg_Program_Name,
                sizeof(Cg_Program_Name));

        /*-----------------------------------------------*/
        /*  バッチデバッグ開始処理                       */
        /*-----------------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return  exit(C_const_APNG);
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
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "START");
            /*---------------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        rtn_cd = C_const_OK;                /* 関数戻り値 */
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else {
                /* 規定外パラメータ */
                rtn_cd = C_const_NG;
                sprintf(out_format_buf, "定義外の引数（%s）", arg_Work1);
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = [%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            /* パラメータのチェック結果がNG */
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
        /*  DBコネクト処理                               */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(C_ORACONN_MD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect", rtn_cd,
                    rtn_status, 0, 0, 0);
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
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTagecB_main();
        if (rtn_cd == C_const_APNG) {
            return exit(C_const_APNG);
        }
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTagecB_main NG rtn =[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "年齢計算処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlcaManager.rollbackRelease(sqlca);
            /* バッチデバッグ終了処理呼び出し */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* MM顧客情報 各件数出力 */
        APLOG_WT("106", 0, null, "MM顧客情報", in_mmko_cnt,
                ok_mmko_cnt, 0, 0, 0);

        /* 2022/09/30 MCCM初版 DEL START */
        /* MMマタニティベビー情報 各件数出力 */
/*    APLOG_WT("106", 0, null, "MMマタニティベビー情報",in_mmmb_cnt,
            ok_mmmb_cnt, 0, 0, 0);*/
        /* 2022/09/30 MCCM初版 DEL END */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease(sqlca.name);
        return exit(C_const_APOK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTagecB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTagecB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*             年齢計算処理                                                   */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*             なし                                                           */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTagecB_main() {
        int rtn_cd;                    /* 関数戻り値                          */
        IntegerDto rtn_status = new IntegerDto();                /* 関数ステータス                      */
        StringDto bat_date_y = new StringDto(9);             /* バッチ処理日付(前日)                */
        StringDto bat_date_t = new StringDto(9);             /* バッチ処理日付(当日)                */
        StringDto wk_date = new StringDto(9);                /* バッチ処理日付バッファ              */
        int leap_yaer_flg;             /* 閏年判定フラグ                      */
        /* 2022/09/30 MCCM初版 DEL START */
        /*    char    wk_md[5];*/                  /* 誕生日(月日)                        */
        /* 2022/09/30 MCCM初版 DEL END */
        IntegerDto wk_age = new IntegerDto();                    /* 年齢                                */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ                     */

        /* 初期化 */
        memset(bat_date_y, 0x00, sizeof(bat_date_y));
        memset(bat_date_t, 0x00, sizeof(bat_date_t));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("年齢計算処理");
            /*---------------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(前日日付)取得%s\n", "");
            /*--------------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(-1, bat_date_y, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(前日日付)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(前日日付)取得NG status= %d\n", rtn_status);
                /*---------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate(前日日付)",
                    rtn_cd, rtn_status, 0, 0, 0);
            return C_const_APNG;
        }
        h_shori_yday = atoi(bat_date_y);

        if (DBG_LOG) {
            /*-------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(前日日付)取得OK [%s]\n", bat_date_y);
            /*-------------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日日付)取得%s\n", "");
            /*--------------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(0, bat_date_t, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(当日日付)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(当日日付)取得NG status= %d\n", rtn_status);
                /*---------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate(当日日付)",
                    rtn_cd, rtn_status, 0, 0, 0);
            return C_const_APNG;
        }
        h_shori_tday = atoi(bat_date_t);

        if (DBG_LOG) {
            /*-------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日日付)取得OK [%s]\n", bat_date_t);
            /*-------------------------------------------------------------------*/
        }
        /* 当日日付(年月日) */
        h_ymd = atoi(bat_date_t);
        /* 当日日付(月日) */
        h_md = h_ymd % 10000;
        /* 当日日付(年) */
        h_year = h_ymd / 10000;
        /* 当日日付(月) */
        h_month = (h_ymd / 100) % 100;
        /* 当日日付(日) */
        h_day = h_ymd % 100;

        /* 平年における2月29日生まれ年齢更新用に、*/
        /* 3月1日当日に前日更新条件を付加設定する */
        leap_yaer_flg = 0;
        h_leap_ymd = 99999999;
        h_leap_md = 9999;
        h_leap_month = 99;
        h_leap_day = 99;
        /* 閏年チェック */
        if (h_year % 100 == 0) {
            if (h_year % 400 == 0) {
                /* 閏年 */
                leap_yaer_flg = 1;
            } else {
                /* 平年 */
            }
        } else {
            if (h_year % 4 == 0) {
                /* 閏年 */
                leap_yaer_flg = 1;
            } else {
                /* 平年 */
            }
        }
        if (h_month == 3 && h_day == 1) {
            if (leap_yaer_flg != 1) {
                memset(wk_date, 0x00, sizeof(wk_date));
                strncpy(wk_date, bat_date_y, 4);
                strcat(wk_date, "0229");
                /* 閏年(年月日) */
                h_leap_ymd = atoi(wk_date);
                /* 閏年(月日) */
                h_leap_md = 229;
                /* 閏年(月) */
                h_leap_month = 2;
                /* 閏年(日) */
                h_leap_day = 29;
            }
        }

        /* ------------------------ */
        /* MM顧客情報               */
        /* ------------------------ */
        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));

        /* MM顧客情報検索ＳＱＬの作成 */
        if ((h_month == 3 && h_day == 1) && leap_yaer_flg != 1) {
            sprintf(wk_sql,
                    "SELECT /*+ INDEX(MM顧客情報 IXMMCSTINF01) */ "
                            + " to_char(顧客番号, 'FM000000000000000'),"
                            + " NVL(誕生年,0), "
                            + " NVL(誕生月,0), "
                            + " NVL(誕生日,0) "
                            + " FROM  MM顧客情報 "
                            + " WHERE ( "
                            + " 誕生月 = %d "
                            + " AND 誕生日 = %d "
                            + " ) OR ( "
                            + " 誕生月 = %d "
                            + " AND 誕生日 = %d "
                            + " ) "
                    , h_month, h_day, h_leap_month, h_leap_day);

        } else {
            sprintf(wk_sql,
                    "SELECT /*+ INDEX(MM顧客情報 IXMMCSTINF01) */ "
                            + " to_char(顧客番号, 'FM000000000000000'),"
                            + " NVL(誕生年,0), "
                            + " NVL(誕生月,0), "
                            + " NVL(誕生日,0) "
                            + " FROM  MM顧客情報 "
                            + " WHERE 誕生月 = %d "
                            + " AND   誕生日 = %d "
                    , h_month, h_day);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** 動的ＳＱＬ =[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat1 from:
//        str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** 動的ＳＱＬ 解析NG = %d\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
//        EXEC SQL DECLARE AGEC_MMKO01 cursor for sql_stat1;
        SqlstmDto sqlca = sqlcaManager.get("AGEC_MMKO01");
        sqlca.declare();
        /* カーソルオープン */
//        EXEC SQL OPEN AGEC_MMKO01;
        sqlca.open();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** MM顧客情報 CURSOR OPEN " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** 動的SQL OPEN NG =[%d]\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, "CURSOR OPEN ERR",
                    0, 0, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* データが終了するまでフェッチを繰り返す */
        while (true) {
            /* 初期化 */
            mmkoinf_t=new MM_KOKYAKU_INFO_TBL();
            memset(mmkoinf_t.kokyaku_no.arr, 0x00, sizeof(mmkoinf_t.kokyaku_no.arr));
            mmkoinf_t.tanjo_y.arr = 0;
            mmkoinf_t.tanjo_m.arr = 0;
            mmkoinf_t.tanjo_d.arr = 0;
            mmkoinf_t.nenre.arr = 0;
            sqlca = sqlcaManager.get("AGEC_MMKO01");
            /* カーソルフェッチ */
            sqlca.fetchInto(mmkoinf_t.kokyaku_no,
                    mmkoinf_t.tanjo_y,
                    mmkoinf_t.tanjo_m,
                    mmkoinf_t.tanjo_d);
//            EXEC SQL FETCH AGEC_MMKO01
//            INTO:
//              mmkoinf_t.kokyaku_no,
//             :mmkoinf_t.tanjo_y,
//             :mmkoinf_t.tanjo_m,
//             :mmkoinf_t.tanjo_d;

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** MM顧客情報 " +
                            "FETCH NG sqlcode =[%d]\n", sqlca.sqlcode);
                    /*------------------------------------------------------------*/
                }
                sprintf(out_format_buf, "誕生月=%02d 誕生日=%02d 誕生月(閏年)=%02d " +
                        "誕生日(閏年)=%02d", h_month, h_day, h_leap_month, h_leap_day);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);
//                EXEC SQL CLOSE AGEC_MMKO01; /* カーソルクローズ */
                sqlcaManager.close(sqlca);
                /* 処理を終了する */
                return (C_const_NG);
            }
            /* データ無しの場合、ループを抜ける */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /* MM顧客情報_処理対象件数カウントアップ */
            in_mmko_cnt++;

            /* 新年齢取得 */
            wk_age.arr = 0;
            rtn_cd = C_CountAge(mmkoinf_t.tanjo_y.intVal(), mmkoinf_t.tanjo_m.intVal(),
                    mmkoinf_t.tanjo_d.intVal(), h_year, h_month, h_day, wk_age);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_CountAge", rtn_cd,
                        0, 0, 0, 0);
//                EXEC SQL CLOSE AGEC_MMKO01; /* カーソルクローズ */
                sqlcaManager.close(sqlca);
                /* 処理を終了する */
                return (C_const_NG);
            }
            mmkoinf_t.nenre.arr = wk_age;

            /* 顧客ポイントロック */
            rtn_cd = C_KdataLock(mmkoinf_t.kokyaku_no.strDto(), "1", rtn_status);
            /* 顧客データロック処理エラーの場合 */
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTagecB_cmBTagecB_main *** 顧客ロック NG" +
                            "status= %d\n", rtn_status);
                    /*----------------------------------------------------------------*/
                }

                APLOG_WT("913", 0, null, mmkoinf_t.kokyaku_no.arr,
                        0, 0, 0, 0, 0);
//                EXEC SQL CLOSE AGEC_MMKO01; /* カーソルクローズ */
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* MM顧客情報のレコードを更新(update)する */
            sqlca = sqlcaManager.getDefault();
            sqlca.sql.arr = "UPDATE MM顧客情報" +
                    "            SET 年齢 = " + mmkoinf_t.nenre + "," +
                    "                    バッチ更新日 = " + h_shori_tday + "," +
                    "                    最終更新日 = " + h_shori_tday + "," +
                    "                    最終更新日時 = sysdate()," +
                    "                    最終更新プログラムＩＤ = " + h_saishu_koshin_programid +
                    "            WHERE 顧客番号 = " + mmkoinf_t.kokyaku_no;
//            EXEC SQL UPDATE MM顧客情報
//            SET 年齢 = :mmkoinf_t.nenre,
//                    バッチ更新日 = :h_shori_tday,
//                    最終更新日 = :h_shori_tday,
//                    最終更新日時 = sysdate,
//                    最終更新プログラムＩＤ = :h_saishu_koshin_programid
//            WHERE 顧客番号 = :mmkoinf_t.kokyaku_no;

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                sprintf(out_format_buf, "顧客番号=%s",
                        mmkoinf_t.kokyaku_no.arr);

                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);
//                EXEC SQL CLOSE AGEC_MMKO01; /* カーソルクローズ */
                sqlcaManager.close("AGEC_MMKO01");
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* コミット */
//            EXEC SQL COMMIT WORK;
            sqlca.commit();
            /* MM顧客情報_正常処理件数カウントアップ */
            ok_mmko_cnt++;
        }
//        EXEC SQL CLOSE AGEC_MMKO01; /* カーソルクローズ */
        sqlcaManager.close("AGEC_MMKO01");

        /* 2022/09/30 MCCM初版 DEL START */
        /* ------------------------ */
        /* MMマタニティベビー情報   */
        /* ------------------------ */
        /* 初期化 */
/*    memset(wk_sql, 0x00, sizeof(wk_sql));
    memset(str_sql, 0x00, sizeof(str_sql));*/

        /* MMマタニティベビー情報検索ＳＱＬの作成 */
/*    if ((h_month == 3 && h_day == 1) && leap_yaer_flg != 1) {
        sprintf(wk_sql,
            "SELECT to_char(顧客番号, 'FM000000000000000'), "
                  " NVL(SUBSTR(第１子生年月日,1,4),0), "
                  " NVL(SUBSTR(第１子生年月日,5,2),0), "
                  " NVL(SUBSTR(第１子生年月日,7,2),0), "
                  " NVL(SUBSTR(第２子生年月日,1,4),0), "
                  " NVL(SUBSTR(第２子生年月日,5,2),0), "
                  " NVL(SUBSTR(第２子生年月日,7,2),0), "
                  " NVL(SUBSTR(第３子生年月日,1,4),0), "
                  " NVL(SUBSTR(第３子生年月日,5,2),0), "
                  " NVL(SUBSTR(第３子生年月日,7,2),0), "
                  " NVL(第１子年齢,0), "
                  " NVL(第２子年齢,0), "
                  " NVL(第３子年齢,0) "
            " FROM  MMマタニティベビー情報 "
            " WHERE SUBSTR(第１子生年月日,5,4) = %d OR "
                  " SUBSTR(第１子生年月日,5,4) = %d OR "
                  " SUBSTR(第２子生年月日,5,4) = %d OR "
                  " SUBSTR(第２子生年月日,5,4) = %d OR "
                  " SUBSTR(第３子生年月日,5,4) = %d OR "
                  " SUBSTR(第３子生年月日,5,4) = %d "
            , h_md, h_leap_md, h_md, h_leap_md, h_md, h_leap_md);

    } else {
        sprintf(wk_sql,
            "SELECT to_char(顧客番号, 'FM000000000000000'), "
                  " NVL(SUBSTR(第１子生年月日,1,4),0), "
                  " NVL(SUBSTR(第１子生年月日,5,2),0), "
                  " NVL(SUBSTR(第１子生年月日,7,2),0), "
                  " NVL(SUBSTR(第２子生年月日,1,4),0), "
                  " NVL(SUBSTR(第２子生年月日,5,2),0), "
                  " NVL(SUBSTR(第２子生年月日,7,2),0), "
                  " NVL(SUBSTR(第３子生年月日,1,4),0), "
                  " NVL(SUBSTR(第３子生年月日,5,2),0), "
                  " NVL(SUBSTR(第３子生年月日,7,2),0), "
                  " NVL(第１子年齢,0), "
                  " NVL(第２子年齢,0), "
                  " NVL(第３子年齢,0) "
            " FROM  MMマタニティベビー情報 "
            " WHERE SUBSTR(第１子生年月日,5,4) = %d OR "
                  " SUBSTR(第２子生年月日,5,4) = %d OR "
                  " SUBSTR(第３子生年月日,5,4) = %d "
            , h_md, h_md, h_md);
    }

if (DBG_LOG){*/
        /*------------------------------------------------------------------------*/
        /*    C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** 動的ＳＱＬ =[%s]\n", wk_sql ) ;*/
        /*------------------------------------------------------------------------*/
        /*}*/

        /* ＨＯＳＴ変数にセット */
        /*    strcpy(str_sql, wk_sql);*/

        /* 動的ＳＱＬ文の解析 */
/*    EXEC SQL PREPARE sql_stat1 from :str_sql;

    if (sqlca.sqlcode != C_const_Ora_OK) {
if (DBG_LOG){*/
        /*--------------------------------------------------------------------*/
/*        C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** 動的ＳＱＬ 解析NG = %d\n",
                   sqlca.sqlcode ) ;*/
        /*--------------------------------------------------------------------*/
/*}
        APLOG_WT( "902", 0, null,sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
        return (C_const_NG);
    }*/

        /* カーソル定義 */
        /*    EXEC SQL DECLARE AGEC_MMMB01 cursor for sql_stat1;*/

        /* カーソルオープン */
/*    EXEC SQL OPEN AGEC_MMMB01;

if (DBG_LOG){*/
        /*------------------------------------------------------------------------*/
/*    C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** MMマタニティベビー情報 CURSOR OPEN "
              "sqlcode =[%d]\n", sqlca.sqlcode );*/
        /*------------------------------------------------------------------------*/
/*}

    if (sqlca.sqlcode != C_const_Ora_OK) {
if (DBG_LOG){*/
        /*--------------------------------------------------------------------*/
/*        C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** 動的SQL OPEN NG =[%d]\n",
                   sqlca.sqlcode ) ;*/
        /*--------------------------------------------------------------------*/
/*}
        APLOG_WT("902", 0, null,sqlca.sqlcode, "CURSOR OPEN ERR",
                  0, 0, 0, 0);*/
        /* 処理を終了する */
/*        return (C_const_NG);
    }*/

        /* データが終了するまでフェッチを繰り返す */
        /*    while( 1 ) {*/

        /* 初期化 */
/*        memset(mmmbinf_t.kokyaku_no.arr, 0x00, sizeof(mmmbinf_t.kokyaku_no.arr));
        h_dai1shi_se_y = 0;
        h_dai1shi_se_m = 0;
        h_dai1shi_se_d = 0;
        h_dai2shi_se_y = 0;
        h_dai2shi_se_m = 0;
        h_dai2shi_se_d = 0;
        h_dai3shi_se_y = 0;
        h_dai3shi_se_m = 0;
        h_dai3shi_se_d = 0;
        mmmbinf_t.dai1shi_nenre = 0;
        mmmbinf_t.dai2shi_nenre = 0;
        mmmbinf_t.dai3shi_nenre = 0;*/

        /* カーソルフェッチ */
/*        EXEC SQL FETCH AGEC_MMMB01
            INTO :mmmbinf_t.kokyaku_no,
                 :h_dai1shi_se_y,
                 :h_dai1shi_se_m,
                 :h_dai1shi_se_d,
                 :h_dai2shi_se_y,
                 :h_dai2shi_se_m,
                 :h_dai2shi_se_d,
                 :h_dai3shi_se_y,
                 :h_dai3shi_se_m,
                 :h_dai3shi_se_d,
                 :mmmbinf_t.dai1shi_nenre,
                 :mmmbinf_t.dai2shi_nenre,
                 :mmmbinf_t.dai3shi_nenre;*/

        /* データ無し以外のエラーの場合処理を異常終了する */
/*        if (sqlca.sqlcode != C_const_Ora_OK &&
             sqlca.sqlcode != C_const_Ora_NOTFOUND) {
if (DBG_LOG){*/
        /*------------------------------------------------------------*/
/*            C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** MMマタニティベビー情報 "
                      "FETCH NG sqlcode =[%d]\n", sqlca.sqlcode );*/
        /*------------------------------------------------------------*/
/*}

            sprintf(out_format_buf, "誕生月=%02d 誕生日=%02d 誕生月(閏年)=%02d "
                    "誕生日(閏年)=%02d", h_month, h_day, h_leap_month, h_leap_day);
            APLOG_WT("904", 0, null, "FETCH",sqlca.sqlcode,
                     "MMマタニティベビー情報", out_format_buf, 0, 0);
            EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*            return (C_const_NG);
        }*/
        /* データ無しの場合、ループを抜ける */
/*        if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            break;
        }*/

        /* MMマタニティベビー情報_処理対象件数カウントアップ */
/*        in_mmmb_cnt++;

        memset(wk_md, 0x00, sizeof(wk_md));
        sprintf(wk_md, "%02d%02d", h_dai1shi_se_m, h_dai1shi_se_d);*/
        /* 前日誕生日の場合 */
        /*        if (atoi(wk_md) == h_md || atoi(wk_md) == h_leap_md) {*/
        /* 新年齢取得(第１子) */
/*            wk_age = 0;
            rtn_cd = C_CountAge(h_dai1shi_se_y, h_dai1shi_se_m, h_dai1shi_se_d,
                                h_year, h_month, h_day, &wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge",rtn_cd,
                          0 ,0 ,0 ,0);
                EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*                return (C_const_NG);
            }
            mmmbinf_t.dai1shi_nenre = wk_age;
        }

        memset(wk_md, 0x00, sizeof(wk_md));
        sprintf(wk_md, "%02d%02d", h_dai2shi_se_m, h_dai2shi_se_d);*/
        /* 前日誕生日の場合 */
        /*        if (atoi(wk_md) == h_md || atoi(wk_md) == h_leap_md) {*/
        /* 新年齢取得(第２子) */
/*            wk_age = 0;
            rtn_cd = C_CountAge(h_dai2shi_se_y, h_dai2shi_se_m, h_dai2shi_se_d,
                                h_year, h_month, h_day, &wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge",rtn_cd,
                          0 ,0 ,0 ,0);
                EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*                return (C_const_NG);
            }
            mmmbinf_t.dai2shi_nenre = wk_age;
        }

        memset(wk_md, 0x00, sizeof(wk_md));
        sprintf(wk_md, "%02d%02d", h_dai3shi_se_m, h_dai3shi_se_d);*/
        /* 前日誕生日の場合 */
        /*        if (atoi(wk_md) == h_md || atoi(wk_md) == h_leap_md) {*/
        /* 新年齢取得(第３子) */
/*            wk_age = 0;
            rtn_cd = C_CountAge(h_dai3shi_se_y, h_dai3shi_se_m, h_dai3shi_se_d,
                                h_year, h_month, h_day, &wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge",rtn_cd,
                          0 ,0 ,0 ,0);
                EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*                return (C_const_NG);
            }
            mmmbinf_t.dai3shi_nenre = wk_age;
        }*/

        /* 顧客データロック */
        /*        rtn_cd = C_KdataLock((char *)mmmbinf_t.kokyaku_no.arr, "2", &rtn_status);*/

        /* 処理不要データをスキップ、ループの先頭に戻る */
/*        if (rtn_cd == C_const_NOTEXISTS) {
if (DBG_LOG){*/
        /*----------------------------------------------------------------*/
/*            C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** 顧客ロック NOTEXISTS"
                      "顧客番号= %s\n", (char *)mmmbinf_t.kokyaku_no.arr ) ;*/
        /*----------------------------------------------------------------*/
/*}
            continue;
        }*/
        /* 顧客データロック処理エラーの場合 */
/*        if (rtn_cd != C_const_OK) {
if (DBG_LOG){*/
        /*----------------------------------------------------------------*/
/*            C_DbgMsg( "*** cmBTagecB_cmBTagecB_main *** 顧客ロック NG"
                      "status= %d\n", rtn_status ) ;*/
        /*----------------------------------------------------------------*/
/*}

            APLOG_WT("913", 0, null, (char *)mmmbinf_t.kokyaku_no.arr,
                      0, 0, 0, 0, 0);
            EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*            return (C_const_NG);
        }*/

        /* MMマタニティベビー情報のレコードを更新(update)する */
/*        EXEC SQL UPDATE MMマタニティベビー情報
                 SET    第１子年齢             = :mmmbinf_t.dai1shi_nenre,
                        第２子年齢             = :mmmbinf_t.dai2shi_nenre,
                        第３子年齢             = :mmmbinf_t.dai3shi_nenre,
                        バッチ更新日           = :h_shori_tday,
                        最終更新日             = :h_shori_tday,
                        最終更新日時           = sysdate,
                        最終更新プログラムＩＤ = :h_saishu_koshin_programid
                 WHERE  顧客番号               = :mmmbinf_t.kokyaku_no;*/

        /* エラーの場合処理を異常終了する */
/*        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(out_format_buf, "顧客番号=%s",
                    (char *)mmmbinf_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE",sqlca.sqlcode,
                      "MMマタニティベビー情報", out_format_buf, 0, 0);
            EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 処理を終了する */
/*            return (C_const_NG);
        }*/

        /* コミット */
        /*        EXEC SQL COMMIT WORK;*/
        /* MMマタニティベビー情報_正常処理件数カウントアップ */
/*        ok_mmmb_cnt++;
    }
    EXEC SQL CLOSE AGEC_MMMB01;*/ /* カーソルクローズ */
        /* 2022/09/30 MCCM初版 DEL END */

        return (C_const_OK);
    }

}
