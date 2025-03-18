package jp.co.mcc.nttdata.batch.business.service.cmBTmsstB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_BLOCK_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.PS_MISE_HYOJI_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.ComBusinessService;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;
import static jp.co.mcc.nttdata.batch.business.service.cmBTcardB.CmBTcardBServiceImpl.DEF_DEBUG;
import static jp.co.mcc.nttdata.batch.business.service.cmBTcardB.CmBTcardBServiceImpl.DEF_debug;

/*******************************************************************************
 *   プログラム名   ： 店表示情報作成（cmBTmsstB.pc）
 *
 *   【処理概要】
 *     MSブロック情報から取得した店舗階層情報を「店表示情報」に設定する。
 *
 *   【引数説明】
 *     -DEBUG(-debug)      :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *      10     ： 正常
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/11/12 SSI.吉岡 ： 初版
 *      2.00 :  2013/11/25 SSI.上野 ： 店舗全階層情報取得処理基準日取得修正
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTmsstBServiceImpl extends CmABfuncLServiceImpl implements CmBTmsstBService, ComBusinessService {



    String C_PRGNAME = "店表示情報作成";

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int psmh_data_cnt;            /* PS店表示情報情報更新件数           */
    StringDto log_format_buf = new StringDto(); /* APログ用                  */
    int h_date_today;        /* 当日日付                    */
    int h_date_base;         /* 基準日付                    */
    int h_joui_svblock_cd;   /* 上位ＳＶブロックコード      */
    String h_program_id;  /* プログラムID                */
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
        DBG_LOG = true;

        argv[0] = "cmBTmsstB";
        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto arg_Work1 = new StringDto();                 /* Work Buffer1                   */
        StringDto bat_date = new StringDto();                  /* バッチ処理日付（当日）         */
        StringDto h_program_id = new StringDto();

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        h_date_today = 0;                       /* 処理日付                       */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /* ホスト変数に設定 */
        strcpy(h_program_id, Cg_Program_Name);

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
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            C_DbgMsg("*** main *** 入力引数の数[%d]\n", argc - 1);
            /*------------------------------------------------------------*/
        }

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
            } else {                                       /* 定義外パラメータ   */
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                  /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
            /*------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
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
            exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（当日）        */
        /*-------------------------------------*/
        rtn_status.arr = 0;
        rtn_cd = C_const_OK;
        memset(bat_date, 0x00, sizeof(bat_date));

        /* バッチ処理日付を取得（当日） */
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        h_date_today = atoi(bat_date);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理日付(当日)=[%d]\n", h_date_today);
            /*------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  店舗階層情報設定処理                         */
        rtn_cd = cmBTmsstB_Set_Block_All();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTmsstB_Set_Block_All NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "店舗階層情報設定処理に失敗しました", 0, 0, 0, 0, 0);

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        APLOG_WT("107", 0, null, "PS店表示情報",
                psmh_data_cnt, 0, 0, 0, 0);

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /* コミット解放処理 */
        sqlca.commit();

        return exit(C_const_APOK);
    }

    PS_MISE_HYOJI_INFO_TBL psmise_t;            /* PS店表示情報バッファ        */
    MS_BLOCK_INFO_TBL msblock_t;           /* MSブロック情報バッファ      */
    StringDto WQSL = new StringDto();
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmsstB_Set_Block_All                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTmsstB_Set_Block_All()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      店舗階層情報設定処理                                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmsstB_Set_Block_All() {
        int rtn_cd;                           /* 関数戻り値                       */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTmsstB_Set_Block_All処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /* PS店表示情報情報更新件数を初期化 */
        psmh_data_cnt = 0;

        /* PS店表示情報より、全レコードを取得 */

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
        //    EXEC SQL DECLARE CUR_PSMISE CURSOR FOR
        //        SELECT
        //              PSMH.店番号,
        //              PSMH.開始年月日,
        //              PSMH.終了年月日,
        //              PSMH.企業コード,
        //              PSMH.ブロックコード
        //        FROM PS店表示情報 PSMH;
        WQSL.arr = "SELECT\n" +
                "        PSMH.店番号,\n" +
                "                PSMH.開始年月日,\n" +
                "                PSMH.終了年月日,\n" +
                "                PSMH.企業コード,\n" +
                "                PSMH.ブロックコード\n" +
                "        FROM PS店表示情報 PSMH";
        SqlstmDto sqlca = sqlcaManager.get("CUR_PSMISE");

        sqlca.sql = WQSL;
        sqlca.declare();
        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmsstB_Set_Block_All *** PS店表示情報 DECLARE CURSOR " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*--------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "PS店表示情報", "(全件)", 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Set_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_PSMISE;
        sqlca.open();

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmsstB_Set_Block_All *** PS店表示情報 CURSOR OPEN " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*--------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "PS店表示情報", "(全件)", 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Set_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {

//            memset(&psmise_t, 0x00, sizeof(psmise_t));

            psmise_t = new PS_MISE_HYOJI_INFO_TBL();
            sqlca.fetch();
            sqlca.recData(psmise_t.mise_no,
                    psmise_t.kaishi_ymd,
                    psmise_t.shuryo_ymd,
                    psmise_t.kigyo_cd,
                    psmise_t.block_cd);

            if (DBG_LOG) {
                /*--------------------------------------------------------------*/
                C_DbgMsg("*** cmBTmsstB_Set_Block_All *** PS店表示情報 FETCH " +
                        "sqlcode =[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "PS店表示情報", "(全件)", 0, 0);

//                EXEC SQL CLOSE CUR_PSMISE; /* カーソルクローズ                   */
//                CUR_PSMISE.close();

                sqlcaManager.close("CUR_PSMISE");
                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("cmBTmsstB_Set_Block_All処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return C_const_NG;
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTmsstB_Set_Block_All *** PS店表示情報 FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*-----------------------------------------------------------------------------*/
                }
                break;
            }

            /* 店舗の階層情報を取得する */
            rtn_cd = cmBTmsstB_Get_Block_All();

            /* データなしの場合は処理SKIP */
            if (rtn_cd == C_const_NOTEXISTS) {
            } else if (rtn_cd != C_const_OK) {

                APLOG_WT("912", 0, null, "店舗階層情報取得処理に失敗しました", 0, 0, 0, 0, 0);

//                CUR_PSMISE.close(); /* カーソルクローズ                    */
                sqlcaManager.close("CUR_PSMISE");
                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("cmBTmsstB_Set_Block_All処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            /* 店舗の階層情報を設定する */
            WQSL.arr = "UPDATE PS店表示情報 PSMH\n" +
                    "                 SET 企業名称               = SUBSTRB(?, 1, 40),\n" +
                    "                     企業短縮名称           = SUBSTRB(?, 1, 20),\n" +
                    "                     企業名称カナ           = SUBSTRB(?, 1,60),\n" +
                    "                     部コード               = ?,\n" +
                    "                     部名称                 = SUBSTRB(?, 1, 40),\n" +
                    "                     部短縮名称             = SUBSTRB(?, 1, 20),\n" +
                    "                     部名称カナ             = SUBSTRB(?, 1, 60),\n" +
                    "                     ゾーンコード           = ?,\n" +
                    "                     ゾーン名称             = SUBSTRB(?, 1, 40),\n" +
                    "                     ゾーン短縮名称         = SUBSTRB(?, 1, 20),\n" +
                    "                     ゾーン名称カナ         = SUBSTRB(?, 1, 60),\n" +
                    "                     ブロック名称           = SUBSTRB(?, 1, 40),\n" +
                    "                     ブロック短縮名称       = SUBSTRB(?, 1, 20),\n" +
                    "                     ブロック名称カナ       = SUBSTRB(?, 1, 60),\n" +
                    "                     バッチ更新日           = ?,\n" +
                    "                     最終更新日             = ?,\n" +
                    "                     最終更新日時           = SYSDATE(),\n" +
                    "                     最終更新プログラムＩＤ = ?\n" +
                    "               WHERE PSMH.店番号            = ?\n" +
                    "                 AND PSMH.開始年月日        = ?";
            SqlstmDto updateSql = sqlcaManager.get("updateSql");

            updateSql.sql = WQSL;
            updateSql.restAndExecute(psmise_t.kigyo_kanji,
                    psmise_t.kigyo_short,
                    psmise_t.kigyo_kana,
                    psmise_t.bu_cd.intVal(),
                    psmise_t.bu_kanji,
                    psmise_t.bu_short,
                    psmise_t.bu_kana,
                    psmise_t.zone_cd.intVal(),
                    psmise_t.zone_kanji,
                    psmise_t.zone_short,
                    psmise_t.zone_kana,
                    psmise_t.block_kanji,
                    psmise_t.block_short,
                    psmise_t.block_kana,
                    h_date_today,
                    h_date_today,
                    h_program_id,
                    psmise_t.mise_no,
                    psmise_t.kaishi_ymd);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTmsstB_Set_Block_All *** PS店表示情報 UPDATE " +
                        "sqlcode =[%d]\n", updateSql.sqlcode);
                /*------------------------------------------------------------*/
            }

            /* データ無し以外エラーの場合、処理を異常終了する */
            if (updateSql.sqlcode != C_const_Ora_OK &&
                    updateSql.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "店番号 = %d, 開始年月日 = %d", psmise_t.mise_no, psmise_t.kaishi_ymd);

                APLOG_WT("904", 0, null, "UPDATE", updateSql.sqlcode,
                        "PS店表示情報", log_format_buf, 0, 0);

//                updateSql.close(); /* カーソルクローズ                    */
                sqlcaManager.close("updateSql");
                if (DBG_LOG) {
                    /*---------------------------------------------------*/
                    C_DbgEnd("cmBTmsstB_Set_Block_All", C_const_NG, 0, 0);
                    /*---------------------------------------------------*/
                }
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* コミット処理 */
            updateSql.commit();

            /* 更新件数をカウントアップ */
            psmh_data_cnt++;

        } /* LOOP END */

        /* カーソルクローズ */
        sqlcaManager.close("CUR_PSMISE");

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTmsstB_Set_Block_All処理", C_const_OK, 0, 0);
            /*--------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmsstB_Get_Block_All                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTmsstB_Get_Block_All()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      店舗全階層情報取得処理                                                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmsstB_Get_Block_All() {
        int rtn_cd;                           /* 関数戻り値                       */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTmsstB_Get_Block_All処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /* 基準日付の設定 */
        if (psmise_t.kaishi_ymd.intVal() <= h_date_today && h_date_today <= psmise_t.shuryo_ymd.intVal()) {
            h_date_base = h_date_today;
        } else if (h_date_today < psmise_t.kaishi_ymd.intVal()) {
            h_date_base = psmise_t.kaishi_ymd.intVal();
        } else if (h_date_today > psmise_t.shuryo_ymd.intVal()) {
            h_date_base = psmise_t.shuryo_ymd.intVal();
        }

        /*--------------------------------*/
        /* 企業情報取得               */
        /*--------------------------------*/

        /* 上位ＳＶブロックコードの設定 */
        h_joui_svblock_cd = psmise_t.kigyo_cd.intVal();

        rtn_cd = cmBTmsstB_Get_Block();
        if (rtn_cd == C_const_NOTEXISTS) {
            sprintf(log_format_buf, "店舗階層情報（企業）が存在しません。店番号 = %d 、開始年月日 = %d",
                    psmise_t.mise_no, psmise_t.kaishi_ymd);
            APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            return (C_const_NOTEXISTS);
        } else if (rtn_cd != C_const_OK) {

            APLOG_WT("912", 0, null, "店舗階層情報(企業)取得処理に失敗しました", 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* 取得項目をホスト変数に設定 */
        strcpy(psmise_t.kigyo_kanji, msblock_t.sv_block_kanji);
        strcpy(psmise_t.kigyo_short, msblock_t.sv_block_short);
        strcpy(psmise_t.kigyo_kana, msblock_t.sv_block_kana);

        /*--------------------------------*/
        /* ブロック情報取得               */
        /*--------------------------------*/

        /* 上位ＳＶブロックコードの設定 */
        h_joui_svblock_cd = psmise_t.block_cd.intVal();

        rtn_cd = cmBTmsstB_Get_Block();
        if (rtn_cd == C_const_NOTEXISTS) {
            sprintf(log_format_buf, "店舗階層情報（ブロック）が存在しません。店番号 = %d 、開始年月日 = %d",
                    psmise_t.mise_no, psmise_t.kaishi_ymd);
            APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            strcpy(psmise_t.block_kanji, " ");
            strcpy(psmise_t.block_short, " ");
            strcpy(psmise_t.block_kana, " ");
            strcpy(psmise_t.zone_kanji, " ");
            strcpy(psmise_t.zone_short, " ");
            strcpy(psmise_t.zone_kana, " ");
            strcpy(psmise_t.bu_kanji, " ");
            strcpy(psmise_t.bu_short, " ");
            strcpy(psmise_t.bu_kana, " ");

            return (C_const_NOTEXISTS);
        } else if (rtn_cd != C_const_OK) {

            APLOG_WT("912", 0, null, "店舗階層情報(ブロック)取得処理に失敗しました", 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* 取得項目をホスト変数に設定 */
        strcpy(psmise_t.block_kanji, msblock_t.sv_block_kanji);
        strcpy(psmise_t.block_short, msblock_t.sv_block_short);
        strcpy(psmise_t.block_kana, msblock_t.sv_block_kana);
        psmise_t.zone_cd = msblock_t.joui_svblock_cd;

        /*--------------------------------*/
        /* ゾーン情報取得               */
        /*--------------------------------*/

        /* 上位ＳＶブロックコードの設定 */
        h_joui_svblock_cd = msblock_t.joui_svblock_cd.intVal();

        rtn_cd = cmBTmsstB_Get_Block();
        if (rtn_cd == C_const_NOTEXISTS) {
            sprintf(log_format_buf, "店舗階層情報（ゾーン）が存在しません。店番号 = %d 、開始年月日 = %d",
                    psmise_t.mise_no, psmise_t.kaishi_ymd);
            APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            strcpy(psmise_t.zone_kanji, " ");
            strcpy(psmise_t.zone_short, " ");
            strcpy(psmise_t.zone_kana, " ");
            strcpy(psmise_t.bu_kanji, " ");
            strcpy(psmise_t.bu_short, " ");
            strcpy(psmise_t.bu_kana, " ");

            return (C_const_NOTEXISTS);
        } else if (rtn_cd != C_const_OK) {

            APLOG_WT("912", 0, null, "店舗階層情報(ゾーン)取得処理に失敗しました", 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* 取得項目をホスト変数に設定 */
        strcpy(psmise_t.zone_kanji, msblock_t.sv_block_kanji);
        strcpy(psmise_t.zone_short, msblock_t.sv_block_short);
        strcpy(psmise_t.zone_kana, msblock_t.sv_block_kana);
        psmise_t.bu_cd = msblock_t.joui_svblock_cd;

        /*--------------------------------*/
        /* 部情報取得               */
        /*--------------------------------*/

        /* 上位ＳＶブロックコードの設定 */
        h_joui_svblock_cd = msblock_t.joui_svblock_cd.intVal();

        rtn_cd = cmBTmsstB_Get_Block();
        if (rtn_cd == C_const_NOTEXISTS) {
            sprintf(log_format_buf, "店舗階層情報（部）が存在しません。店番号 = %d 、開始年月日 = %d",
                    psmise_t.mise_no, psmise_t.kaishi_ymd);
            APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            strcpy(psmise_t.bu_kanji, " ");
            strcpy(psmise_t.bu_short, " ");
            strcpy(psmise_t.bu_kana, " ");

            return (C_const_NOTEXISTS);
        } else if (rtn_cd != C_const_OK) {

            APLOG_WT("912", 0, null, "店舗階層情報(部)取得処理に失敗しました", 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block_All処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* 取得項目をホスト変数に設定 */
        strcpy(psmise_t.bu_kanji, msblock_t.sv_block_kanji);
        strcpy(psmise_t.bu_short, msblock_t.sv_block_short);
        strcpy(psmise_t.bu_kana, msblock_t.sv_block_kana);

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTmsstB_Get_Block_All処理", C_const_OK, 0, 0);
            /*--------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */

    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmsstB_Get_Block                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTmsstB_Get_Block()                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      店舗１階層取得処理                                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmsstB_Get_Block() {

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTmsstB_Get_Block処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /*--------------------------------------*/
        /* MSブロック情報から店舗階層情報を取得 */
        /*--------------------------------------*/
        WQSL.arr = "SELECT\n" +
                "        MSBL.会社コード,\n" +
                "                RPAD(MSBL.ＳＶブロック名,LENGTH(MSBL.ＳＶブロック名)) AS ＳＶブロック名,\n" +
                "                RPAD(MSBL.ＳＶブロック短縮名,LENGTH(MSBL.ＳＶブロック短縮名)) AS ＳＶブロック短縮名,\n" +
                "                RPAD(MSBL.ＳＶブロック名カナ,LENGTH(MSBL.ＳＶブロック名カナ)) AS ＳＶブロック名カナ,\n" +
                "                MSBL.上位ＳＶブロックコード\n" +
                "        FROM MSブロック情報 MSBL\n" +
                "        WHERE MSBL.ＳＶブロックコード = ?\n" +
                "        AND MSBL.発効日 <= ?\n" +
                "        AND MSBL.失効日 >= ?";

        sqlca.sql = WQSL;
        msblock_t = new MS_BLOCK_INFO_TBL();
        sqlca.restAndExecute(h_joui_svblock_cd, h_date_base, h_date_base);
        sqlca.fetch();
        sqlca.recData(msblock_t.kakisha_cd,
                msblock_t.sv_block_kanji,
                msblock_t.sv_block_short,
                msblock_t.sv_block_kana,
                msblock_t.joui_svblock_cd);

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmsstB_Get_Block *** MSブロック情報 SELECT "+
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*--------------------------------------------------------------------*/
        }

        /* データ無しの場合 */
        if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            sprintf(log_format_buf, "該当データなし。TBL:MSブロック情報 KEY:ＳＶブロックコード = %d, 発効日 <= %d, 失効日 >= %d",
                    h_joui_svblock_cd, h_date_base, h_date_base);
            APLOG_WT("700", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block処理", C_const_NOTEXISTS, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NOTEXISTS);
        }

        /* エラーの場合 */
        else if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "ＳＶブロックコード = %d, 発効日 <= %d, 失効日 >= %d",
                    h_joui_svblock_cd, h_date_base, h_date_base);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MSブロック情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmsstB_Get_Block処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        return (C_const_OK);              /* 処理終了                           */

    }
}
