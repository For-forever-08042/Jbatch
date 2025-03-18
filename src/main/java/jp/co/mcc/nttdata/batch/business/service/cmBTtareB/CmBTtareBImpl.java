package jp.co.mcc.nttdata.batch.business.service.cmBTtareB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;

/*******************************************************************************
 *   プログラム名   ： 止め区分洗替処理（cmBTtareB）
 *
 *   【処理概要】
 *       DM止め区分、Eメール止め区分の変更が行われた会員の
 *       MM顧客企業別属性情報の止め区分の洗い替えを行う
 *
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
 *      1.00 :   2016/09/30 SSI.田：初版
 *     30.00 :   2021/02/02 NDBS.緒方: 期間限定Ｐ対応によりリコンパイル
 *                                    (顧客データロック処理内容更新のため)
 *     40.00 :   2022/09/30 SSI.川内：MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTtareBImpl extends CmABfuncLServiceImpl implements CmBTtareB {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */

    boolean DBG_LOG = true;                    /* デバッグメッセージ出力     */
    StringDto h_kokyaku_id = new StringDto(15+1); /* 顧客番号                         */
    ItemDto h_kigyo_cd = new ItemDto(); /* 企業コード                       */
    ItemDto h_kubun = new ItemDto(); /* 区分                             */
    ItemDto h_tome_kbn = new ItemDto(); /* 止め区分設定値                   */
    int h_batch_prev = 0; /* バッチ処理日付前日               */
    int h_batch_date = 0; /* バッチ処理日付                   */
    String h_prog_id = ""; /* 最終更新プログラムID             */
    StringDto str_sql = new StringDto(); /* 実行用SQL文字列                  */

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int FLG_NASHI = -1;       /* 無                                */
    int FLG_ARI = 0;           /* 有                                */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ                  */
    String DEF_debug = "-debug";        /* デバッグスイッチ                  */
    String PG_NAME = "止め区分洗替";  /* プログラム名称                    */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int update_all_cnt; /* 更新総件数(HSカード変更情報)         */
    int update_dm_cnt; /* DM区分更新件数(HSカード変更情報)     */
    int update_em_cnt; /* Eメール区分更新件数(HSカード変更情報)*/

    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット           */
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
        int rtn_cd; /* 関数戻り値                */
        IntegerDto rtn_status = new IntegerDto(); /* 関数ステータス            */
        int arg_chk; /* 引数の種類チェック結果    */
        int arg_cnt; /* 引数チェック用カウンタ    */
        StringDto arg_Work1 = new StringDto(256); /* Work Buffer1              */
        StringDto bat_date_prev = new StringDto(9); /* 取得用 バッチ処理日付前日 */
        StringDto bat_date = new StringDto(9); /* 取得用 バッチ処理日付     */
        StringDto wk_char = new StringDto(9); /* 編集領域                  */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*-------------------------------------*/
        /*  プログラム名取得処理呼び出し       */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", (long) rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }
        /* HOST変数にバージョン付きプログラム名をコピーする */
        memset(h_prog_id, 0x00, sizeof(h_prog_id));
        h_prog_id=  memcpy(h_prog_id, Cg_Program_Name, sizeof(Cg_Program_Name));

        /*-------------------------------------*/
        /*  開始メッセージ                     */
        /*-------------------------------------*/
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理呼び出し     */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", (long) rtn_cd,
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
            if (strcmp(arg_Work1, DEF_DEBUG) == 0) {
                continue;
            } else if (strcmp(arg_Work1, DEF_debug) == 0) {
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
            C_DbgMsg("*** main *** DBコネクト%s\n", C_ORACONN_MD);
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(C_ORACONN_MD, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_OraDBConnect", (long) rtn_cd,
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
            APLOG_WT("903", 0, null, "C_GetBatDate", (long) rtn_cd,
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
        h_batch_prev = atoi(wk_char);

        /* バッチ処理日付取得 */
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", (long) rtn_cd,
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
        h_batch_date = atoi(bat_date);


        /*-------------------------------------*/
        /*  処理件数領域を初期化               */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理件数領域を初期化%s\n", "");
            /*-------------------------------------------------------------*/
        }
        update_all_cnt = 0; /* 更新総件数                         */
        update_dm_cnt = 0; /* DM止め区分更新件数                 */
        update_em_cnt = 0; /* Eメール止め区分更新件数            */


        /*-------------------------------------*/
        /*  主処理                             */
        /*-------------------------------------*/
        rtn_cd = cmBTtareB_main();
        if (rtn_cd != C_const_OK) {
            /* APLOG出力 */
            APLOG_WT("912", 0, null,
                    "止め区分洗替処理に失敗しました", 0, 0, 0, 0, 0);
            /*  ロールバック処理呼び出し     */
//            EXEC SQL ROLLBACK RELEASE;
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

        /*  APLOG出力     */
        APLOG_WT("107", 0, null, "MM顧客企業別制度情報", (long) update_all_cnt, 0, 0, 0, 0);
        APLOG_WT("107", 0, null, "MM顧客企業別制度情報(DM止め区分)", (long) update_dm_cnt, 0, 0, 0, 0);
        APLOG_WT("107", 0, null, "MM顧客企業別制度情報(Eメール止め区分)", (long) update_em_cnt, 0, 0, 0, 0);

        /* コミット、開放する */
//        EXEC SQL COMMIT WORK RELEASE;
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
    /*  関数名 ： cmBTtareB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTtareB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               止め区分洗替処理                                             */
    /*  【概要】                                                                  */
    /*               止め区分洗替処理処理を行う。                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTtareB_main() {
        int rtn_cd; /* 関数戻り値                    */
        IntegerDto rtn_status = new IntegerDto(); /* 関数ステータス                */
        StringDto wk_sql = new StringDto(); /* 動的SQLバッファ               */
        StringDto uid_before = new StringDto(); /* 前回顧客番号保持用            */

        memset(uid_before, 0x00, sizeof(uid_before));
        strcpy(uid_before, "0");


        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("止め区分洗替処理");
            /*---------------------------------------------------------------------*/
        }

        /*--------------------------------------------------------*/
        /* MM顧客企業別属性情報から止め区分を変更された顧客を抽出 */
        /*--------------------------------------------------------*/

        memset(wk_sql, 0x00, sizeof(wk_sql));
        strcpy(wk_sql,
                "SELECT 顧客番号,1, 企業コード,ＤＭ止め区分,最終更新日時 "
                        + "FROM ( "
                        + "SELECT T.顧客番号,T.企業コード,T.ＤＭ止め区分,T.最終更新日時, "
                        + "ROW_NUMBER() OVER ( "
                        + "PARTITION BY T.顧客番号 "
                        + "ORDER BY "
                        + "case T.ＤＭ止め区分 "
                        + "when 3092 then 3 "
                        + "when 3099 then 2 "
                        + "else 1 end, "
                        + "T.最終更新日時 desc, "
                        + "case T.企業コード "
                        /* 2022/09/30 MCCM初版 MOD START */
/*                    "when 1020 then 1 "
                    "when 1040 then 2 "
                "else 3 end "*/
                        + "when 3020 then 1 "
                        + "when 1020 then 2 "
                        + "when 3040 then 3 "
                        + "when 1040 then 4 "
                        + "when 3050 then 5 "
                        + "when 3010 then 6 "
                        + "when 1010 then 7 "
                        + "else 8 end "
                        /* 2022/09/30 MCCM初版 MOD END */
                        + ") G_ROW "
                        + "FROM MM顧客企業別属性情報 T "
                        + "WHERE T.顧客番号 in ( "
                        + "SELECT DISTINCT T.顧客番号 from cmuser.MM顧客企業別属性情報 T "
                        + "WHERE T.最終更新日 >= ? "
                        + "AND EXISTS ( "
                        + "SELECT 1 FROM MM顧客企業別属性情報 A "
                        + "WHERE A.顧客番号=T.顧客番号 "
                        + "AND A.企業コード<>T.企業コード "
                        + "AND A.ＤＭ止め区分<>T.ＤＭ止め区分 "
                        + ") "
                        + ") "
                        + ") DM "
                        + "WHERE DM.G_ROW = 1 "
                        + "UNION ALL "
                        + "SELECT 顧客番号,2, 企業コード,Ｅメール止め区分,最終更新日時 "
                        + "FROM ( "
                        + "SELECT T.顧客番号,T.企業コード,T.Ｅメール止め区分,T.最終更新日時, "
                        + "ROW_NUMBER() OVER ( "
                        + "PARTITION BY T.顧客番号 "
                        + "ORDER BY "
                        + "case when  "
                        + "T.企業コード=3020 and T.退会年月日=0 then 1 "
                        + "else 2 end, "
                        + "case T.Ｅメール止め区分 "
                        + "when 5092 then 3 "
                        + "when 5099 then 2 "
                        + "else 1 end, "
                        + "T.最終更新日時 desc, "
                        + "case T.企業コード "
                        /* 2022/09/30 MCCM初版 MOD START */
/*                    "when 1020 then 1 "
                    "when 1040 then 2 "
                "else 3 end "*/
                        + "when 3020 then 1 "
                        + "when 1020 then 2 "
                        + "when 3040 then 3 "
                        + "when 1040 then 4 "
                        + "when 3050 then 5 "
                        + "when 3010 then 6 "
                        + "when 1010 then 7 "
                        + "when 3060 then 8 "
                        + "else 9 end "
                        /* 2022/09/30 MCCM初版 MOD END */
                        + ") G_ROW "
                        + "FROM MM顧客企業別属性情報 T "
                        + "WHERE T.顧客番号 in (  "
                        + "SELECT DISTINCT T.顧客番号 from cmuser.MM顧客企業別属性情報 T "
                        + "WHERE T.最終更新日 >= ? "
                        + "AND EXISTS ( "
                        + "SELECT 1 FROM MM顧客企業別属性情報 A "
                        + "WHERE A.顧客番号=T.顧客番号 "
                        + "AND A.企業コード<>T.企業コード "
                        + "AND A.Ｅメール止め区分<>T.Ｅメール止め区分 "
                        + ") "
                        + ") "
                        + ") EM "
                        + "WHERE EM.G_ROW = 1 "
                        + "ORDER BY 顧客番号 "
        );

        /*--------------------------------------------------------------------*/
        C_DbgMsg("*** cmBTtareB_main*** SQL %s\n", wk_sql);
        /*--------------------------------------------------------------------*/
        /* HOST変数に設定 */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql);

        SqlstmDto cur_kdatalock1 = sqlcaManager.get("cur_kdatalock1");
        /* 動的SQL文の解析 */
//        EXEC SQL PREPARE sql_stat1 from:
//        str_sql;
        cur_kdatalock1.sql = str_sql;
        cur_kdatalock1.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcareB_main*** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgEnd("カード統合洗替処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("902", 0, null, (long) sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            /* 処理をNGで終了する */
            return (C_const_NG);
        }

        /* カーソル定義 */
//        EXEC SQL DECLARE cur_kdatalock1 cursor for sql_stat1;
        cur_kdatalock1.declare();
        /* カーソルオープン */
        cur_kdatalock1.query(h_batch_prev,h_batch_prev);
//        EXEC SQL OPEN cur_kdatalock1 USING:
//        h_batch_prev, :h_batch_prev;
        if (cur_kdatalock1.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcareB_main *** カーソルOEPNエラー = %d\n", cur_kdatalock1.sqlcode);
                C_DbgEnd("止め区分洗替処理", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("904", 0, null, "OPEN", (long) cur_kdatalock1.sqlcode,
                    "MM顧客企業別属性情報", str_sql, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* データが終了するまでフェッチを繰り返す */
        while (true) {
            memset(h_kokyaku_id, 0x00, sizeof(h_kokyaku_id));
            memset(h_kubun, 0x00, sizeof(h_kubun));
            memset(h_kigyo_cd, 0x00, sizeof(h_kigyo_cd));
            memset(h_tome_kbn, 0x00, sizeof(h_tome_kbn));

            cur_kdatalock1.fetch();
            /* カーソルフェッチ */
//            EXEC SQL FETCH cur_kdatalock1
//            INTO:
//            h_kokyaku_id,
//              :h_kubun,
//              :h_kigyo_cd,
//              :h_tome_kbn;
            cur_kdatalock1.recData(h_kokyaku_id,
                    h_kubun,
                    h_kigyo_cd,
                    h_tome_kbn);
            if (DBG_LOG) {
                C_DbgMsg("FETCH %s\n", "");
                C_DbgMsg("*** h_kokyaku_id[%s]\n", h_kokyaku_id);
                C_DbgMsg("*** h_kubun[%d]\n", h_kubun);
                C_DbgMsg("*** h_kigyo_cd[%d]\n", h_kigyo_cd);
                C_DbgMsg("*** h_tome_kbn[%d]\n", h_tome_kbn);
            }

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (cur_kdatalock1.sqlcode != C_const_Ora_OK &&
                    cur_kdatalock1.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    C_DbgMsg("FETCH NG sqlcode =[%d]\n", cur_kdatalock1.sqlcode);
                }
                sprintf(out_format_buf, "最終更新日=%d",
                        h_batch_date);
                APLOG_WT("904", 0, null, "CURSOR FETCH", (long) cur_kdatalock1.sqlcode,
                        "MM顧客企業別属性情報", out_format_buf, 0, 0);

                /* カーソルクローズ */
                sqlcaManager.close("cur_kdatalock1");

                return (C_const_NG);
            }
            if (cur_kdatalock1.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /* 前回の顧客番号と今回一致しない場合 */
            if (strcmp(h_kokyaku_id, uid_before) != 0) {
                /* コミットする */
//                EXEC SQL COMMIT WORK;
//                cur_kdatalock1.commit();
                sqlcaManager.commit("WORK");

                /* 処理件数をカウントアップ */
                update_all_cnt++;

                /*----------------------------------------------*/
                /* 顧客ロック（ポイント更新）                   */
                /*----------------------------------------------*/
                rtn_cd = C_KdataLock(h_kokyaku_id, "1", rtn_status);
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null, "C_KdataLock", (long) rtn_cd,
                            rtn_status, 0, 0, 0);
                    if (DBG_LOG) {
                        /*---------------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTtareB_main *** ロックエラー顧客=[%s]\n", h_kokyaku_id);
                        /*---------------------------------------------------------------------*/
                    }
//                    cur_kdatalock1.close();
                    sqlcaManager.close("cur_kdatalock1");
//                    EXEC SQL CLOSE cur_kdatalock1; /* カーソルクローズ                   */
                    return C_const_NG;
                }
            }

            if (h_kubun.intVal() == 1) {
                /*----------------------------------------------*/
                /* DM止め区分洗替                               */
                /*----------------------------------------------*/
                rtn_cd = UpdateDMkbn();
                if (rtn_cd != C_const_OK) {

                    if (DBG_LOG) {
                        /*--------------------------------------------------------*/
                        C_DbgMsg("*** cmBTtareB_main *** DM止め区分更新エラー%s\n", "");
                        C_DbgEnd("DM止め区分洗替処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------*/
                    }
                    /* APLOG出力 */
                    APLOG_WT("912", 0, null, "DM止め区分の洗替に失敗しました",
                            0, 0, 0, 0, 0);
                    /* カーソルクローズ */
                    sqlcaManager.close("cur_kdatalock1");
//                    EXEC SQL CLOSE cur_kdatalock1;
                    /* 処理をNGで終了する */
                    return C_const_NG;
                }

                /* DM止め区分の更新件数をカウントアップ */
                update_dm_cnt++;
            } else {
                /*----------------------------------------------*/
                /* Eメール止め区分洗替                          */
                /*----------------------------------------------*/
                rtn_cd = UpdateEMkbn();
                if (rtn_cd != C_const_OK) {

                    if (DBG_LOG) {
                        /*--------------------------------------------------------*/
                        C_DbgMsg("*** cmBTtareB_main *** Eメール止め区分更新エラー%s\n", "");
                        C_DbgEnd(" Eメール止め区分洗替処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------*/
                    }
                    /* APLOG出力 */
                    APLOG_WT("912", 0, null, "Eメール止め区分の洗替に失敗しました",
                            0, 0, 0, 0, 0);
                    /* カーソルクローズ */
//                    EXEC SQL CLOSE cur_kdatalock1;
                    sqlcaManager.close("cur_kdatalock1");
                    /* 処理をNGで終了する */
                    return C_const_NG;
                }

                /* Eメール止め区分の更新件数をカウントアップ */
                update_em_cnt++;
            }

            /*今回の顧客番号を保持*/
            memset(uid_before, 0x00, sizeof(uid_before));
            strcpy(uid_before, h_kokyaku_id);

        }  /* while ( 1 ) bottom */

        sqlcaManager.commit(WORK);
        /* 最後一件コミットする */
//        EXEC SQL COMMIT WORK;
        /* カーソルクローズ */
        sqlcaManager.close("cur_kdatalock1");
//        EXEC SQL CLOSE cur_kdatalock1;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("止め区分洗替処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----cmBTtareB_main Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateDMkbn                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateDMkbn()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               DM止め区分洗替処理                                           */
    /*  【概要】                                                                  */
    /*               MM顧客企業別属性情報を更新する。                             */
    /*               ・顧客番号、企業コードの紐付け                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateDMkbn() {
        if (DBG_LOG) {
            C_DbgStart("DM止め区分洗替処理");
        }

        /* UPDATEする */
//        EXEC SQL UPDATE MM顧客企業別属性情報
//        SET ＤＭ止め区分 = :h_tome_kbn,
//                バッチ更新日 = :h_batch_date,
//                最終更新日 = :h_batch_date,
//                最終更新日時 = sysdate,
//                最終更新プログラムＩＤ = :h_prog_id
//        WHERE 顧客番号 = :h_kokyaku_id
//        AND 企業コード            <> :h_kigyo_cd
//        AND ＤＭ止め区分          <> :h_tome_kbn;
        sqlca.sql=new StringDto();
        sqlca.sql.arr = "UPDATE MM顧客企業別属性情報" +
                "        SET ＤＭ止め区分 = ?," +
                "                バッチ更新日 = ?," +
                "                最終更新日 = ?," +
                "                最終更新日時 = sysdate()," +
                "                最終更新プログラムＩＤ = ?" +
                "        WHERE 顧客番号 = ?" +
                "        AND 企業コード            <> ?" +
                "        AND ＤＭ止め区分          <> ?";
        if (DBG_LOG) {
            C_DbgMsg("UPDATE %s\n", "");
            C_DbgMsg("*** 顧客番号[%s]\n", h_kokyaku_id);
            C_DbgMsg("*** 企業コード[%d]\n", h_kigyo_cd);
            C_DbgMsg("*** DM止め区分[%d]\n", h_tome_kbn);
        }
        sqlca.restAndExecute(h_tome_kbn, h_batch_date, h_batch_date, h_prog_id, h_kokyaku_id, h_kigyo_cd, h_tome_kbn);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s,企業コード=%d,DM止め区分=%d",
                    h_kokyaku_id, h_kigyo_cd, h_tome_kbn);
            APLOG_WT("904", 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateDMkbn Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateEMkbn                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateEMkbn()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               Eメール止め区分洗替処理                                      */
    /*  【概要】                                                                  */
    /*               MM顧客企業別属性情報を更新する。                             */
    /*               ・顧客番号、企業コードの紐付け                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateEMkbn() {
        if (DBG_LOG) {
            C_DbgStart("Eメール止め区分洗替処理");
        }

        /* UPDATEする */
//        EXEC SQL UPDATE MM顧客企業別属性情報
//        SET Ｅメール止め区分 = :h_tome_kbn,
//                バッチ更新日 = :h_batch_date,
//                最終更新日 = :h_batch_date,
//                最終更新日時 = sysdate,
//                最終更新プログラムＩＤ = :h_prog_id
//        WHERE 顧客番号 = :h_kokyaku_id
//        AND 企業コード            <> :h_kigyo_cd
//        AND Ｅメール止め区分      <> :h_tome_kbn;
        sqlca.sql=new StringDto();
        sqlca.sql.arr = " UPDATE MM顧客企業別属性情報" +
                "        SET Ｅメール止め区分 = ?," +
                "                バッチ更新日 = ?," +
                "                最終更新日 = ?," +
                "                最終更新日時 = sysdate()," +
                "                最終更新プログラムＩＤ = ?" +
                "        WHERE 顧客番号 = ?" +
                "        AND 企業コード            <> ?" +
                "        AND Ｅメール止め区分      <> ?";
        if (DBG_LOG) {
            C_DbgMsg("UPDATE %s\n", "");
            C_DbgMsg("*** 顧客番号[%s]\n", h_kokyaku_id);
            C_DbgMsg("*** 企業コード[%d]\n", h_kigyo_cd);
            C_DbgMsg("*** Eメール止め区分[%d]\n", h_tome_kbn);
        }
        sqlca.restAndExecute(h_tome_kbn, h_batch_date, h_batch_date, h_prog_id, h_kokyaku_id, h_kigyo_cd, h_tome_kbn);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s,企業コード=%d,Eメール止め区分=%d",
                    h_kokyaku_id, h_kigyo_cd, h_tome_kbn);
            APLOG_WT("904", 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateEMkbn Bottom----------------------------------------------*/
    }

}
