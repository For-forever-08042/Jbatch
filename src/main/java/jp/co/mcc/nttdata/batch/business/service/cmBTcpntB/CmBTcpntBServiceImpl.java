package jp.co.mcc.nttdata.batch.business.service.cmBTcpntB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTcpntB.dto.CmBTcpntBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 顧客入会情報更新（cmBTcpntB）
 *
 *   【処理概要】
 *     TS利用可能ポイント情報の入会店、発券店の情報をMM顧客情報に更新する。
 *
 *   【引数説明】
 *     -DEBUG(-debug)     :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *      10     ： 正常
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *     1.00 :   2013/03/13 SSI.本田  ： 初版
 *     1.02 :   2013/07/04 SSI.石阪  ： 企業コード、販社コードを併せて更新
 *     3.00 :   2016/03/25 SSI.田頭  ： モバイルカスタマーポータル対応
 *                                       顧客企業別属性情報更新条件変更
 *    30.00 :   2021/02/02 NDBS.緒方  : 期間限定Ｐ対応によりリコンパイル
 *                                      (TS利用可能ポイント情報構造体/
 *                                       顧客データロック処理内容更新のため)
 *    40.00 :   2022/10/07 SSI.申     : MCCM初版
 *    40.01 :   2023/02/13 SSI.飯塚   : 顧客ステータス更新処理追加
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTcpntBServiceImpl extends CmABfuncLServiceImpl implements CmBTcpntBService {

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTcpntBDto cmBTcpntBDto = new CmBTcpntBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "顧客入会情報更新";    /* APログ用機能名                   */

    long C_KAIIN_NO_HIKAIIN = 9999999999999L;  /* 非会員会員番号                  */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int gi_taisyo_cnt;            /* 処理対象件数                       */
    int gi_ok_cnt;                /* 正常処理件数                       */
    /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
    int gi_ok_cnt2;               /* 正常処理件数 main2用               */
    /* 2023/02/13 顧客ステータス情報更新処理 ADD END */
    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                 **/


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
        StringDto bat_date_prev = new StringDto(9);               /* 取得用 バッチ処理日付前日          */
        StringDto bat_date_this = new StringDto(9);               /* 取得用 バッチ処理日付当日          */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                       */
        StringDto wk_char = new StringDto(9);                     /* 編集領域                           */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        gi_taisyo_cnt = 0;                  /* 処理対象件数                       */
        gi_ok_cnt = 0;                  /* 正常処理件数                       */
        /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
        gi_ok_cnt2 = 0;                  /* 正常処理件数 main2用               */
        /* 2023/02/13 顧客ステータス情報更新処理 ADD END */

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

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
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

            } else {                        /* 定義外パラメータ                   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_MD, rtn_status);
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

        /** 変数初期化                **/
        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));
        memset(bat_date_this, 0x00, sizeof(bat_date_this));

        /* バッチ処理日付前日取得 */
        rtn_cd = C_GetBatDate(0, bat_date_this, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日当日取得NG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理当日取得OK [%s]\n", bat_date_this);
            /*---------------------------------------------*/
        }

        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_this, 8);
        cmBTcpntBDto.gh_bat_date_this.arr = atoi(wk_char);


        /* バッチ処理日付前日取得 */
        rtn_cd = C_GetBatDate(-1, bat_date_prev, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
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

        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev, 8);
        cmBTcpntBDto.gh_bat_date_prev.arr = atoi(wk_char);

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  顧客入会情報更新主処理               */
        rtn_cd = cmBTcpntB_main();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTcpntB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "顧客入会情報更新主処理に失敗しました", 0, 0, 0, 0, 0);
            /* ロールバック */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
        /* 顧客ステータス情報更新主処理 */
        rtn_cd = cmBTcpntB_main2();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main2 *** cmBTcpntB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "顧客ステータス情報更新主処理に失敗しました", 0, 0, 0, 0, 0);
            /* ロールバック */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        /* 2023/02/13 顧客ステータス情報更新処理 ADD END */

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /* APLOG出力 */
        APLOG_WT("106", 0, null, "TS利用可能ポイント情報",
                gi_taisyo_cnt, gi_taisyo_cnt, 0, 0, 0);
        APLOG_WT("107", 0, null, "MM顧客情報",
                gi_ok_cnt, 0, 0, 0, 0);

        /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
        APLOG_WT("107", 0, null, "MM顧客情報(顧客ステータス)",
                gi_ok_cnt2, 0, 0, 0, 0);
        /* 2023/02/13 顧客ステータス情報更新処理 ADD END */

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main2処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /*  コミット解放処理 */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTcpntB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTcpntB_main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     顧客入会情報更新主処理                                                 */
    /*     TS利用可能ポイント情報から入会店情報が設定された顧客を抽出し、         */
    /*     MM顧客情報の入会店情報を更新します。                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTcpntB_main() {
        /* ローカル変数 */
        int rtn_cd; /* 関数戻り値                   */
        IntegerDto rtn_status = new IntegerDto();                /* 関数ステータス                      */
        StringDto wk_sqlbuf = new StringDto(C_const_SQLMaxLen); /* ＳＱＬ文編集用               */
        int wi_loop; /* ループカウンタ               */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTcpntB_main処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;

        /*------------------------------------------*/
        /* WM顧客番号ワークに抽出済み顧客番号を登録 */
        /*------------------------------------------*/
        /* WM顧客番号登録処理 */
        rtn_cd = InsertWmkokyakuWk();
        if (rtn_cd == C_const_NG) {
            /* error */
            APLOG_WT("903", 0, null, "InsertWmkokyakuWk", rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        /* ＳＱＬ文作成 */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        strcpy(wk_sqlbuf, "SELECT 顧客番号 FROM WM顧客番号");

        /* ＨＯＳＴ変数にセット */
        memset(cmBTcpntBDto.str_sql, 0x00, sizeof(cmBTcpntBDto.str_sql));
        strcpy(cmBTcpntBDto.str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
        SqlstmDto sqlca = sqlcaManager.get("CMMD_CPNT01");
        // EXEC SQL PREPARE sql_kosed01 FROM :str_sql;
        sqlca.sql = wk_sqlbuf;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CMMD_CPNT01 CURSOR FOR sql_kosed01;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null
                    , sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソルオープン */
        // EXEC SQL OPEN CMMD_CPNT01;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, "CURSOR OPEN ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /* 更新対象顧客情報を取得              */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) { /* for loop */
            // memset(&mmkoinf_t , 0x00, sizeof(mmkoinf_t));

            /*
            EXEC SQL FETCH CMMD_CPNT01
            INTO :mmkoinf_t.kokyaku_no;
            */
            sqlca.fetch();
            /* データ無しの場合、処理を正常終了 */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* for1 を抜ける */
                break;
            }

            /* データ無し以外エラーの場合、処理を異常終了する */
            else if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "WM顧客番号", chg_format_buf, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CMMD_CPNT01;
//                sqlca.close();
                sqlcaManager.close("CMMD_CPNT01");
                return C_const_NG;
            }

            sqlca.recData(cmBTcpntBDto.mmkoinf_t.kokyaku_no);

            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcpntB_main *** 顧客番号[%s]\n", cmBTcpntBDto.mmkoinf_t.kokyaku_no.arr);
                /*----------------------------------------------------------------*/
            }
            /* 処理対象件数カウントアップ */
            gi_taisyo_cnt++;

            /* 顧客番号 設定  */
            memset(cmBTcpntBDto.gh_kokyaku_no, 0x00, sizeof(cmBTcpntBDto.gh_kokyaku_no));
            strncpy(cmBTcpntBDto.gh_kokyaku_no, cmBTcpntBDto.mmkoinf_t.kokyaku_no.strVal(), cmBTcpntBDto.mmkoinf_t.kokyaku_no.len);
            /* 最終更新プログラムＩＤ 設定 */
            strcpy(cmBTcpntBDto.gh_saishu_koshin_programid, Cg_Program_Name);

            /* 顧客ポイントロック */
            rtn_cd = C_KdataLock(cmBTcpntBDto.mmkoinf_t.kokyaku_no.strDto(), "1", rtn_status);

            /* 顧客データロック処理エラーの場合 */
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcpntB_main *** 顧客ロック NG\n" +
                            "status= %d\n", rtn_status);
                    /*----------------------------------------------------------------*/
                }

                APLOG_WT("913", 0, null, cmBTcpntBDto.mmkoinf_t.kokyaku_no.arr,
                        0, 0, 0, 0, 0);
                // EXEC SQL CLOSE CMMD_CPNT01; /* カーソルクローズ */
                sqlcaManager.close("CMMD_CPNT01");
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客情報の更新 */
            /*
            EXEC SQL UPDATE MM顧客情報
            SET (入会会社コードＭＣＣ, 入会店舗ＭＣＣ,                                                                   *//* 2022/10/07 MCCM初版 MOD *//*
                    バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) =
                    (SELECT 入会会社コードＭＣＣ, 入会店舗ＭＣＣ,                                                            *//* 2022/10/07 MCCM初版 MOD *//*
                                 :gh_bat_date_this, :gh_bat_date_this, SYSDATE, :gh_saishu_koshin_programid
            FROM TS利用可能ポイント情報@CMSD
            WHERE 顧客番号 = :gh_kokyaku_no)
            WHERE 顧客番号 = :gh_kokyaku_no;
            */
            StringDto workSql = new StringDto();
            workSql.arr = "UPDATE MM顧客情報 SET (入会会社コードＭＣＣ, 入会店舗ＭＣＣ, バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) =" +
                    "                    (SELECT 入会会社コードＭＣＣ, 入会店舗ＭＣＣ, ?, ?, SYSDATE(), ? FROM TS利用可能ポイント情報  WHERE 顧客番号 = ?)" +
                    "            WHERE 顧客番号 = ?";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTcpntBDto.gh_bat_date_this, cmBTcpntBDto.gh_bat_date_this,
                    cmBTcpntBDto.gh_saishu_koshin_programid, cmBTcpntBDto.gh_kokyaku_no,
                    cmBTcpntBDto.gh_kokyaku_no);

            /* データ無し以外エラーの場合、処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(chg_format_buf, "顧客番号 = %s", cmBTcpntBDto.gh_kokyaku_no);
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "MM顧客情報", chg_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*----------------------------------------------------------*/
                    C_DbgEnd("cmBTcpntB_main", C_const_NG, 0, 0);
                    /*----------------------------------------------------------*/
                }
                /* 処理を終了する */
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                continue;
            }
            /* 2022/10/07 MCCM初版 DEL START */
            /*        EXEC SQL UPDATE MM顧客企業別属性情報 K                                                                     */
            /*                    SET (企業コード,                                                                               */
            /*                        バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ )=                          */
            /*                        (SELECT  入会企業コード,                                                                   */
            /*                                 :gh_bat_date_this, :gh_bat_date_this, SYSDATE, :gh_saishu_koshin_programid        */
            /*                           FROM MM顧客情報                                                                         */
            /*                          WHERE 顧客番号 = K.顧客番号                                                              */
            /*                           AND  入会企業コード <> K.企業コード)                                                    */
            /*                  WHERE 顧客番号 = :gh_kokyaku_no                                                                  */
            /*                   AND  企業コード NOT IN (1020,1040)                                                              */
            /*                   AND  EXISTS ( SELECT 'X'                                                                        */
            /*                                 FROM   TS利用可能ポイント情報@CMSD                                                */
            /*                                 WHERE  顧客番号 = K.顧客番号                                                      */
            /*                                  AND   入会企業コード NOT IN (1020,1040) )                                        */
            /*                   AND  EXISTS ( SELECT 'X'                                                                        */
            /*                                 FROM   MM顧客情報                                                                 */
            /*                                 WHERE  顧客番号 = K.顧客番号                                                      */
            /*                                  AND   入会企業コード <> K.企業コード                                             */
            /*                                  AND   K.企業コード NOT IN (1020,1040) );                                         */
            //
            //        /* データ無し以外エラーの場合、処理を異常終了する */
            //        if (sqlca.sqlcode != C_const_Ora_OK &&
            //            sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            //
            //            sprintf( chg_format_buf, "顧客番号 = %s", gh_kokyaku_no);
            //            APLOG_WT("904", 0, NULL, "UPDATE", (char *)(long)sqlca.sqlcode,
            //                                     "MM顧客情報", chg_format_buf, 0, 0);
            //#if DBG_LOG
            //            /*----------------------------------------------------------*/
            //            C_DbgEnd("cmBTcpntB_main", C_const_NG, 0, 0);
            //            /*----------------------------------------------------------*/
            //#endif
            //            /* 処理を終了する */
            //            return (C_const_NG);
            //        }
            /* 2022/10/07 MCCM初版 DEL END */

            /* 正常件数カウントアップ */
            gi_ok_cnt++;

            // EXEC SQL COMMIT WORK;            /* コミット                          */
            sqlca.commit();

        } /* END for loop */

        // EXEC SQL CLOSE CMMD_CPNT01;         /* カーソルクローズ                  */
        sqlcaManager.close("CMMD_CPNT01");

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTcpntB_main", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----cmBTcpntB_main Bottom--------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertWmkokyakuWk                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  InsertWmkokyakuWk()                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*       WM顧客番号登録処理                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int InsertWmkokyakuWk() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("InsertWmkokyakuWk処理");
            /*------------------------------------------------------------*/
        }
        /* ---------------------------------------- */
        /* WM顧客番号登録                           */
        /* ---------------------------------------- */
        /*
        EXEC SQL INSERT INTO WM顧客番号 SELECT 顧客番号 FROM TS利用可能ポイント情報@CMSD T
        WHERE 最終更新日 >= :gh_bat_date_prev
        AND NOT (入会会社コードＭＣＣ = 2500
        AND (入会店舗ＭＣＣ IN (0, 9901, 9902, 9904)))                             *//* 2022/10/06 MCCM初版 MOD *//*
        AND EXISTS (
                SELECT 顧客番号
        FROM MM顧客情報
        WHERE 顧客番号 = T.顧客番号
        AND (    入会会社コードＭＣＣ <> T.入会会社コードＭＣＣ               *//* 2022/10/06 MCCM初版 MOD *//*
        OR 入会店舗ＭＣＣ <> T.入会店舗ＭＣＣ ));                       *//* 2022/10/06 MCCM初版 MOD *//*
         */
        StringDto workSql = new StringDto();
        workSql.arr = "INSERT INTO WM顧客番号 SELECT 顧客番号 FROM TS利用可能ポイント情報 T\n" +
                "        WHERE 最終更新日 >= ?\n" +
                "        AND NOT (入会会社コードＭＣＣ = 2500\n" +
                "        AND (入会店舗ＭＣＣ IN (0, 9901, 9902, 9904)))                            \n" +
                "        AND EXISTS (\n" +
                "                SELECT 顧客番号\n" +
                "        FROM MM顧客情報\n" +
                "        WHERE 顧客番号 = T.顧客番号\n" +
                "        AND (    入会会社コードＭＣＣ <> T.入会会社コードＭＣＣ               \n" +
                "        OR 入会店舗ＭＣＣ <> T.入会店舗ＭＣＣ ))";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTcpntBDto.gh_bat_date_prev);

        /* INSERT エラー */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "WM顧客番号", chg_format_buf, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("WM顧客番号ワーク登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }


        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("InsertWmkokyakuWk処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        // EXEC SQL COMMIT WORK;            /* コミット                          */
        sqlca.commit();

        return (C_const_OK);              /* 処理終了                           */

        /*--- InsertWmkokyakuWk Bottom -----------------------------------------------*/
    }

    /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTcpntB_main2                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTcpntB_main2()                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     顧客ステータス情報更新主処理                                           */
    /*     「MS顧客制度情報」と「MM顧客情報」から対象データを抽出し、             */
    /*     MM顧客情報の顧客ステータス情報を更新します。                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTcpntB_main2() {
        /* ローカル変数 */
        int rtn_cd; /* 関数戻り値                   */
        IntegerDto rtn_status = new IntegerDto(); /* 関数ステータス               */
        StringDto wk_sqlbuf = new StringDto(C_const_SQLMaxLen); /* ＳＱＬ文編集用               */
        int wi_loop; /* ループカウンタ               */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTcpntB_main2処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;

        /*------------------------------------------*/
        /* WM顧客番号ワークを初期化する             */
        /*------------------------------------------*/
        // EXEC SQL TRUNCATE TABLE WM顧客番号;
        StringDto workSql = new StringDto();
        workSql.arr = "TRUNCATE TABLE WM顧客番号";
        SqlstmDto sqlca = sqlcaManager.get("CMMD_CPNT01");
        sqlca.sql = workSql;
        sqlca.restAndExecute();

        /*------------------------------------------*/
        /* WM顧客番号ワークに抽出済み顧客番号を登録 */
        /*------------------------------------------*/
        /* WM顧客番号登録処理 */
        rtn_cd = InsertWmkokyakuStatusWk();
        if (rtn_cd == C_const_NG) {
            /* error */
            APLOG_WT("903", 0, null, "InsertWmkokyakuStatusWk", rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        /* ＳＱＬ文作成 */
        memset(wk_sqlbuf, 0x00, sizeof(wk_sqlbuf));
        strcpy(wk_sqlbuf, "SELECT 顧客番号 FROM WM顧客番号");

        /* ＨＯＳＴ変数にセット */
        memset(cmBTcpntBDto.str_sql, 0x00, sizeof(cmBTcpntBDto.str_sql));
        strcpy(cmBTcpntBDto.str_sql, wk_sqlbuf);

        /* 動的ＳＱＬ文解析 */
        // EXEC SQL PREPARE sql_kosed01 FROM :str_sql;
        sqlca.sql = wk_sqlbuf;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sqlbuf, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CMMD_CPNT01 CURSOR FOR sql_kosed01;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null
                    , sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソルオープン */
        // EXEC SQL OPEN CMMD_CPNT01;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode, "CURSOR OPEN ERR", 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------------------*/
        /* 更新対象顧客情報を取得              */
        /*-------------------------------------*/
        for (wi_loop = 0; ; wi_loop++) { /* for loop */
            // memset(&mskosed_t , 0x00, sizeof(mskosed_t));

            /*
            EXEC SQL FETCH CMMD_CPNT01
            INTO :mskosed_t.kokyaku_no;
            */
            sqlca.fetch();

            /* データ無しの場合、処理を正常終了 */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* for1 を抜ける */
                break;
            }

            /* データ無し以外エラーの場合、処理を異常終了する */
            else if (sqlca.sqlcode != C_const_Ora_OK) {
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "WM顧客番号", chg_format_buf, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CMMD_CPNT01;
                sqlcaManager.close("CMMD_CPNT01");
                return C_const_NG;
            }

            sqlca.recData(cmBTcpntBDto.mskosed_t.kokyaku_no);
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcpntB_main2 *** 顧客番号[%s]\n", cmBTcpntBDto.mskosed_t.kokyaku_no.arr);
                /*----------------------------------------------------------------*/
            }
            /* 処理対象件数カウントアップ */
            gi_taisyo_cnt++;

            /* 顧客番号 設定  */
            memset(cmBTcpntBDto.gh_kokyaku_no, 0x00, sizeof(cmBTcpntBDto.gh_kokyaku_no));
            strncpy(cmBTcpntBDto.gh_kokyaku_no, cmBTcpntBDto.mskosed_t.kokyaku_no.strVal(), cmBTcpntBDto.mskosed_t.kokyaku_no.len);
            /* 最終更新プログラムＩＤ 設定 */
            strcpy(cmBTcpntBDto.gh_saishu_koshin_programid, Cg_Program_Name);

            /* 顧客ポイントロック */
            rtn_cd = C_KdataLock(cmBTcpntBDto.mmkoinf_t.kokyaku_no.strDto(), "1", rtn_status);

            /* 顧客データロック処理エラーの場合 */
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTcpntB_main2 *** 顧客ロック NG\n" +
                            "status= %d\n", rtn_status);
                    /*----------------------------------------------------------------*/
                }

                APLOG_WT("913", 0, null, cmBTcpntBDto.mskosed_t.kokyaku_no.arr,
                        0, 0, 0, 0, 0);
                // EXEC SQL CLOSE CMMD_CPNT01; /* カーソルクローズ */
                sqlcaManager.close("CMMD_CPNT01");
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客情報の更新 */
            /*
            EXEC SQL UPDATE MM顧客情報
            SET (顧客ステータス, バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) =
                    (SELECT 顧客ステータス, :gh_bat_date_this, :gh_bat_date_this, SYSDATE, :gh_saishu_koshin_programid
            FROM MS顧客制度情報@CMSD
            WHERE 顧客番号 = :gh_kokyaku_no)
            WHERE 顧客番号 = :gh_kokyaku_no;
            */
            workSql = new StringDto();
            workSql.arr = "UPDATE MM顧客情報" +
                    "            SET (顧客ステータス, バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) =" +
                    "                    (SELECT 顧客ステータス, ?, ?, SYSDATE(), ?" +
                    "            FROM MS顧客制度情報" +
                    "            WHERE 顧客番号 = ?)" +
                    "            WHERE 顧客番号 = ?";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTcpntBDto.gh_bat_date_this, cmBTcpntBDto.gh_bat_date_this,
                    cmBTcpntBDto.gh_saishu_koshin_programid, cmBTcpntBDto.gh_kokyaku_no,
                    cmBTcpntBDto.gh_kokyaku_no);

            /* データ無し以外エラーの場合、処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(chg_format_buf, "顧客番号 = %s", cmBTcpntBDto.gh_kokyaku_no);
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "MM顧客情報", chg_format_buf, 0, 0);
                if (DBG_LOG) {
                    /*----------------------------------------------------------*/
                    C_DbgEnd("cmBTcpntB_main2", C_const_NG, 0, 0);
                    /*----------------------------------------------------------*/
                }
                /* 処理を終了する */
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                continue;
            }

            /* 正常件数カウントアップ */
            gi_ok_cnt2++;

            // EXEC SQL COMMIT WORK;            /* コミット                          */
            sqlca.commit();

        } /* END for loop */

        // EXEC SQL CLOSE CMMD_CPNT01;         /* カーソルクローズ                  */
        sqlcaManager.close("CMMD_CPNT01");

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTcpntB_main2", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);              /* 処理終了                           */

        /*-----cmBTcpntB_main2 Bottom--------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertWmkokyakuStatusWk                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  InsertWmkokyakuStatusWk()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*       WM顧客ステータス情報登録処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int InsertWmkokyakuStatusWk() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("InsertWmkokyakuStatusWk処理");
            /*------------------------------------------------------------*/
        }
        /* ---------------------------------------- */
        /* WM顧客番号登録                           */
        /* ---------------------------------------- */
        /*
        EXEC SQL INSERT INTO WM顧客番号 SELECT K.顧客番号 FROM MS顧客制度情報@CMSD S, MM顧客情報 K
        WHERE S.最終更新日 >= :gh_bat_date_prev
        AND S.顧客ステータス <> K.顧客ステータス
        AND S.顧客番号 = K.顧客番号;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "INSERT INTO WM顧客番号 SELECT K.顧客番号 FROM MS顧客制度情報 S, MM顧客情報 K" +
                "        WHERE S.最終更新日 >= ?" +
                "        AND S.顧客ステータス <> K.顧客ステータス" +
                "        AND S.顧客番号 = K.顧客番号";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTcpntBDto.gh_bat_date_prev);

        /* INSERT エラー */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "WM顧客番号", chg_format_buf, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("WM顧客番号ワーク登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }


        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("InsertWmkokyakuStatusWk処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        // EXEC SQL COMMIT WORK;               /* コミット                           */
        sqlca.commit();

        return (C_const_OK);              /* 処理終了                           */

        /*--- InsertWmkokyakuStatusWk Bottom -----------------------------------------*/
    }

}
