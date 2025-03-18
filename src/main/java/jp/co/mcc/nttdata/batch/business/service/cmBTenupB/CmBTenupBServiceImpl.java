package jp.co.mcc.nttdata.batch.business.service.cmBTenupB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTenupB.dto.CmBTenupBDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;


/*******************************************************************************
 *   プログラム名   ： マタニティベビー有効期限更新処理（cmBTenupB）
 *
 *   【処理概要】
 *       MMマタニティベビー情報の
 *       各エントリ情報有効期限更新を行う。
 *       夜間処理のバッチ処理日変更後に起動される。
 *
 *   【引数説明】
 *  -DEBUG(-debug)    : デバッグモードでの実行
 *                      （トレース出力機能が有効）
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
 *      1.00 :  2012/12/13 SSI.吉岡 ： 初版
 *      1.01 :  2013/01/10 SSI.本田 ： 出産クーポン発行日のクリア処理追加
 *      1.02 :  2016/03/29 SSI.石戸 ：会員番号の優先順位を変更。
 *     30.00 :  2021/02/02 NDBS.緒方 : 期間限定Ｐ対応によりリコンパイル
 *                                    (顧客データロック処理内容更新のため)
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTenupBServiceImpl  extends CmABfuncLServiceImpl implements CmBTenupBService {

    boolean DBG_LOG = true;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTenupBDto cmBTenupBDto = new CmBTenupBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;       /* OFF                         */
    int DEF_ON  = 1;       /* ON                          */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ               */
    String DEF_debug = "-debug";        /* デバッグスイッチ               */
    /*---------------------------------------------*/
    String PG_NAME  =  "マタニティベビー有効期限更新";  /* プログラム名称     */
    int UMU_ARI     =       1;       /* データ有無 - 有              */
    int UMU_NASI    =       0;       /* データ有無 - 無              */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    StringDto bat_date_y = new StringDto(9);          /** バッチ処理日付(前日)       **/
    StringDto bat_date_t = new StringDto(9);          /** バッチ処理日付(当日)       **/

    int     mmmb_data_cnt;      /** マタニティベビー情報更新件数           **/
    int     mmmr_data_cnt;      /** マタニティベビー履歴情報更新件数       **/
    int     msps_data_cnt;      /** MS顧客制度情報更新件数                 **/
    int     tsrk_data_cnt;      /** TS利用可能ポイント情報更新件数         **/

    StringDto    out_format_buf = new StringDto(C_const_MsgMaxLen);      /** APログフォーマット   **/


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
        int     rtn_cd;                         /** 関数戻り値                       **/
        int     arg_chk;                        /** 引数の種類チェック結果           **/
        IntegerDto rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int     arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto    arg_Work1 = new StringDto(256) ;                /** Work Buffer1                     **/

        /*                                               */
        /*  初期処理                                     */
        /*                                               */
        /** 変数初期化                **/
        memset( bat_date_y , 0x00 , sizeof(bat_date_y) ) ;
        memset( bat_date_t , 0x00 , sizeof(bat_date_t) ) ;
        memset( out_format_buf , 0x00 , sizeof(out_format_buf) ) ;
        memset( cmBTenupBDto.h_program_id , 0x00 , sizeof(cmBTenupBDto.h_program_id) ) ;
        rtn_cd = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /*                                     */
        /*  プログラム名取得処理呼び出し       */
        /*                                     */
        rtn_cd = C_GetPgname( argv );
        if( rtn_cd != C_const_OK ) {
            APLOG_WT( "903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        /* プログラムIDセット */
        strcpy( cmBTenupBDto.h_program_id , Cg_Program_Name );

        /*                                     */
        /*  開始メッセージ                     */
        /*                                     */
        APLOG_WT( "102", 0, null, PG_NAME , 0, 0, 0, 0, 0);

        /*                                    */
        /*  バッチデバッグ開始処理呼び出し    */
        /*                                    */
        rtn_cd = C_StartBatDbg( argc, argv );
        if( rtn_cd != C_const_OK ) {
            APLOG_WT( "903", 0, null, "C_StartBatDbg" , rtn_cd,
                    0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("main処理");
            /*---------------------------------------------------------------------*/
        }

        /*                                     */
        /*  入力引数チェック                   */
        /*                                     */
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "");
            /*---------------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for( arg_cnt = 1 ; arg_cnt < argc ; arg_cnt++ ) {
            memset( arg_Work1 , 0x00 , 256 ) ;
            strcpy( arg_Work1 , argv[arg_cnt] ) ;
            arg_chk  = UMU_ARI ;

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = %s\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
            if( 0 == strcmp( arg_Work1 , DEF_DEBUG )) {
                continue ;
            } else if( 0 == strcmp( arg_Work1 , DEF_debug )) {
                continue ;
            } else {
                /* 規定外パラメータ  */
                arg_chk  = UMU_NASI ;
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = %d\n", arg_chk);
                /*-------------------------------------------------------------*/
            }

            /* 規定外パラメータ  */
            if( arg_chk != UMU_ARI ) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------------*/
                    C_DbgMsg("*** main *** チェックNG(規定外) %s\n", "");
                    /*-----------------------------------------------------------*/
                }
                sprintf( out_format_buf,
                        "定義外の引数（%s）", arg_Work1);
                APLOG_WT( "910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit( C_const_APNG ) ;
            }

        }

        /*                                     */
        /*  DBコネクト処理呼び出し             */
        /*                                     */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_MD, rtn_status );
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT( "903", 0, null, "C_OraDBConnect", rtn_cd,
                    rtn_status, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit( C_const_APNG ) ;
        }

        /*                                     */
        /*  バッチ処理日(前日)取得処理呼び出し       */
        /*                                     */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(前日)取得%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(-1,  bat_date_y, rtn_status );
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(前日)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(前日)取得NG status= %d\n", rtn_status);
                /*---------------------------------------------*/
            }
            APLOG_WT( "903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit( C_const_APNG ) ;
        }

        /* ホスト変数に保存 */
        cmBTenupBDto.h_batdate_y.arr = atoi(bat_date_y);

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(前日)取得OK [%s]\n", bat_date_y);
            /*---------------------------------------------*/
        }


        /*                                     */
        /*  バッチ処理日取得処理呼び出し       */
        /*                                     */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日)取得%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(0,  bat_date_t, rtn_status );
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG status= %d\n", rtn_status);
                /*---------------------------------------------*/
            }
            APLOG_WT( "903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit( C_const_APNG ) ;
        }

        /* ホスト変数に保存 */
        cmBTenupBDto.h_batdate_t.arr = atoi(bat_date_t);

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日)取得OK [%s]\n", bat_date_t);
            /*---------------------------------------------*/
        }

        /*                                     */
        /*  処理件数領域を初期化               */
        /*                                     */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理件数領域を初期化%s\n", "");
            /*-------------------------------------------------------------*/
        }
        mmmb_data_cnt = 0;
        mmmr_data_cnt = 0;
        msps_data_cnt = 0;
        tsrk_data_cnt = 0;

        /*                                               */
        /*  主処理                                       */
        /*                                               */

        /* マタニティベビー有効期限更新処理 */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** マタニティベビー有効期限更新処理%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = UpdateMBaby();
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "912", 0, null,
                    "マタニティベビー有効期限更新処理に失敗しました" , 0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /*  ロールバック処理呼び出し     */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            return exit( C_const_APNG ) ;
        }

        /*                                               */
        /*  終了処理                                     */
        /*                                               */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 終了処理%s\n", "");
            /*-------------------------------------------------------------*/
        }

        /*  各件数出力     */
        APLOG_WT( "107", 0, null, "MMマタニティベビー情報" ,
                mmmb_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MMマタニティベビー履歴情報" ,
                mmmr_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MS顧客制度情報" ,
                msps_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TS利用可能ポイント情報" ,
                tsrk_data_cnt, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("main処理", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        /*  終了メッセージ */
        APLOG_WT( "103", 0, null, PG_NAME , 0, 0, 0, 0, 0);

        /*  コミット処理呼び出し     */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit( C_const_APOK ) ;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetCardNo                                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  GetCardNo()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      カード情報取得処理                                                    */
    /*      MSカード情報から最新の会員番号を取得する。                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int GetCardNo() {
        StringDto          wk_sqlbuf = new StringDto(2048)           ; /* ＳＱＬ文編集用               */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("GetCardNo処理");
            /*------------------------------------------------------------*/
        }

        /* MSカード情報検索ＳＱＬ文作成            */
        memset(wk_sqlbuf , 0x00, sizeof(wk_sqlbuf ));
        strcpy(wk_sqlbuf,
                "SELECT  D.会員番号 " +
                "FROM ( SELECT C.会員番号 " +
                        "              ,ROW_NUMBER() OVER " +
                        "              (PARTITION BY " +
                        "               C.顧客番号 " +
                        "               ORDER BY " +
                        "                 CASE サービス種別 " +
                        "                    WHEN 1 THEN 1 " +
                        "                    WHEN 3 THEN 2 " +
                        "                    WHEN 2 THEN 3 " +
                        "                    ELSE 0 " +
                        "                 END, " +
                        "                 C.カードステータス ASC, " +
                        "                 C.発行年月日 DESC " +
                        "               ) G_row " +
                        "        FROM  MSカード情報 C " +
                        "        WHERE C.顧客番号 = ? " +
                        "        ORDER BY C.カードステータス " +
                        "     ) D " +
                        "WHERE G_row   =  1 ");

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** GetCardNo *** 動的ＳＱＬ=[%s]\n", wk_sqlbuf);
            /*-------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        memset(cmBTenupBDto.str_sql, 0x00, sizeof(cmBTenupBDto.str_sql));
        strcpy(cmBTenupBDto.str_sql, wk_sqlbuf)            ;

        /* 動的ＳＱＬ文解析 */
        // EXEC SQL PREPARE sql_cardinf FROM :str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "PREPARE",
                    sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("GetCardNo", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_MSCD CURSOR FOR sql_cardinf;
        sqlca.declare();
        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "DECLARE CURSOR",
                    sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("GetCardNo", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_MSCD USING :mmmb_t.kokyaku_no;
        sqlca.open(cmBTenupBDto.mmmb_t.kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "OPEN CURSOR",
                    sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("GetCardNo", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        memset(cmBTenupBDto.h_kaiin_no.arr, 0x00, sizeof(cmBTenupBDto.h_kaiin_no.arr));

        /* カーソルフェッチ */
        /*
        EXEC SQL FETCH CUR_MSCD
        INTO   :h_kaiin_no;
        */
        sqlca.fetch();
        sqlca.recData(cmBTenupBDto.h_kaiin_no);

        /* エラーの場合処理を異常終了する */
        if (   sqlca.sqlcode != C_const_Ora_OK ) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "FETCH",
                    sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("GetCardNo", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }

            /* 処理を終了する */
            return C_const_NG;
        }

        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_MSCD;
        sqlca.curse_close();

        if (DBG_LOG) {
            /*-----------------------------------------------------------------*/
            C_DbgMsg("*** GetCardNo *** 会員番号 = %s\n", cmBTenupBDto.h_kaiin_no.arr);
            /*-----------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("GetCardNo", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return ( C_const_OK );              /* 処理終了                           */
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateMBaby                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateMBaby()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               マテニティベビー有効期限更新処理                             */
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
    public int UpdateMBaby() {
        int     rtn_cd;             /* 関数戻り値                  */
        StringDto          wk_sqlbuf = new StringDto(2048)           ; /* ＳＱＬ文編集用               */

        /* 初期化 */
        rtn_cd = C_const_OK;
        cmBTenupBDto.h_ProcKbn.arr = 1;
        memset(wk_sqlbuf , 0x00, sizeof(wk_sqlbuf ));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("マタニティベビー有効期限更新処理");
            /*---------------------------------------------------------------------*/
        }
        /* MMマタニティベビー情報取得処理 */
        // EXEC SQL DECLARE ENUP_MMBB01 CURSOR FOR
        /*
        SELECT *//*+ INDEX(MMマタニティベビー情報  IXMMMATBBY00) *//*
                顧客番号,
                入会年月日,
                会員種別,
        NVL(第１子名称, ' '),
                NVL(第１子カナ名称, ' '),
                NVL(第１子性別, 0),
                NVL(第１子年齢, 0),
                NVL(第１子生年月日, 0),
                NVL(第２子名称, ' '),
                NVL(第２子カナ名称, ' '),
                NVL(第２子性別, 0),
                NVL(第２子年齢, 0),
                NVL(第２子生年月日, 0),
                NVL(第３子名称, ' '),
                NVL(第３子カナ名称, ' '),
                NVL(第３子性別, 0),
                NVL(第３子年齢, 0),
                NVL(第３子生年月日, 0),
                ＤＭ止め区分,
                Ｅメール止め区分
        FROM  MMマタニティベビー情報
        WHERE 有効期限   < :h_batdate_t
        AND   削除フラグ =  0
        ORDER BY 顧客番号;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT\n" +
                "                顧客番号,\n" +
                "                入会年月日,\n" +
                "                会員種別,\n" +
                "        NVL(RPAD(第１子名称,LENGTH(第１子名称)), ' '),\n" +
                "                NVL(RPAD(第１子カナ名称,LENGTH(第１子カナ名称)), ' '),\n" +
                "                NVL(第１子性別, 0),\n" +
                "                NVL(第１子年齢, 0),\n" +
                "                NVL(第１子生年月日, 0),\n" +
                "                NVL(RPAD(第２子名称,LENGTH(第２子名称)), ' '),\n" +
                "                NVL(RPAD(第２子カナ名称,LENGTH(第２子カナ名称)), ' '),\n" +
                "                NVL(第２子性別, 0),\n" +
                "                NVL(第２子年齢, 0),\n" +
                "                NVL(第２子生年月日, 0),\n" +
                "                NVL(RPAD(第３子名称,LENGTH(第３子名称)), ' '),\n" +
                "                NVL(RPAD(第３子カナ名称,LENGTH(第３子カナ名称)), ' '),\n" +
                "                NVL(第３子性別, 0),\n" +
                "                NVL(第３子年齢, 0),\n" +
                "                NVL(第３子生年月日, 0),\n" +
                "                ＤＭ止め区分,\n" +
                "                Ｅメール止め区分\n" +
                "        FROM  MMマタニティベビー情報\n" +
                "        WHERE 有効期限   < ?\n" +
                "        AND   削除フラグ =  0\n" +
                "        ORDER BY 顧客番号";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTenupBDto.h_batdate_t);

        // EXEC SQL OPEN ENUP_MMBB01;

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            APLOG_WT( "904", 0, null, "OPEN", sqlca.sqlcode,
                    "MMマタニティベビー情報", "有効期限検索", 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("マタニティベビー有効期限更新処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        for ( ; ; ) {

            /* 初期化 */
            memset(cmBTenupBDto.mmmb_t, 0x00, sizeof(cmBTenupBDto.mmmb_t));

            /*
            EXEC SQL FETCH ENUP_MMBB01
            INTO :mmmb_t.kokyaku_no,
                      :mmmb_t.nyukai_ymd,
                      :mmmb_t.kaiin_shubetsu,
                      :mmmb_t.dai1shi_mesho,
                      :mmmb_t.dai1shi_kana_mesho,
                      :mmmb_t.dai1shi_sebetsu,
                      :mmmb_t.dai1shi_nenre,
                      :mmmb_t.dai1shi_se_ymd,
                      :mmmb_t.dai2shi_mesho,
                      :mmmb_t.dai2shi_kana_mesho,
                      :mmmb_t.dai2shi_sebetsu,
                      :mmmb_t.dai2shi_nenre,
                      :mmmb_t.dai2shi_se_ymd,
                      :mmmb_t.dai3shi_mesho,
                      :mmmb_t.dai3shi_kana_mesho,
                      :mmmb_t.dai3shi_sebetsu,
                      :mmmb_t.dai3shi_nenre,
                      :mmmb_t.dai3shi_se_ymd,
                      :mmmb_t.dm_tome_kbn,
                      :mmmb_t.email_tome_kbn;
            */
            sqlca.fetch();
            sqlca.recData(cmBTenupBDto.mmmb_t.kokyaku_no,
                     cmBTenupBDto.mmmb_t.nyukai_ymd,
                     cmBTenupBDto.mmmb_t.kaiin_shubetsu,
                     cmBTenupBDto.mmmb_t.dai1shi_mesho,
                     cmBTenupBDto.mmmb_t.dai1shi_kana_mesho,
                     cmBTenupBDto.mmmb_t.dai1shi_sebetsu,
                     cmBTenupBDto.mmmb_t.dai1shi_nenre,
                     cmBTenupBDto.mmmb_t.dai1shi_se_ymd,
                     cmBTenupBDto.mmmb_t.dai2shi_mesho,
                     cmBTenupBDto.mmmb_t.dai2shi_kana_mesho,
                     cmBTenupBDto.mmmb_t.dai2shi_sebetsu,
                     cmBTenupBDto.mmmb_t.dai2shi_nenre,
                     cmBTenupBDto.mmmb_t.dai2shi_se_ymd,
                     cmBTenupBDto.mmmb_t.dai3shi_mesho,
                     cmBTenupBDto.mmmb_t.dai3shi_kana_mesho,
                     cmBTenupBDto.mmmb_t.dai3shi_sebetsu,
                     cmBTenupBDto.mmmb_t.dai3shi_nenre,
                     cmBTenupBDto.mmmb_t.dai3shi_se_ymd,
                     cmBTenupBDto.mmmb_t.dm_tome_kbn,
                     cmBTenupBDto.mmmb_t.email_tome_kbn);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* DBERR */
                APLOG_WT( "904", 0, null, "FETCH", sqlca.sqlcode,
                        "MMマタニティベビー情報", "有効期限検索", 0, 0);

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("マタニティベビー有効期限更新処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                /* 処理を終了する */
                return C_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateMBaby *** データなし 終了%s\n", "");
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateMBaby *** 顧客番号=[%s]\n", cmBTenupBDto.mmmb_t.kokyaku_no.arr);
                /*-------------------------------------------------------------*/
            }

            /* スペース削除 */
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai1shi_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai1shi_mesho) );
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai1shi_kana_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai1shi_kana_mesho) );
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai2shi_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai2shi_mesho) );
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai2shi_kana_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai2shi_kana_mesho) );
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai3shi_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai3shi_mesho) );
            BT_Rtrim( cmBTenupBDto.mmmb_t.dai3shi_kana_mesho.strDto(), strlen(cmBTenupBDto.mmmb_t.dai3shi_kana_mesho) );

            /* MSカード情報より最新の会員番号を取得 */
            rtn_cd = GetCardNo();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                /* 処理を終了する */
                return C_const_NG;

            }

            /* MMマタニティベビー履歴情報登録 */
            rtn_cd = InsertMBabyHist();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                /* 処理を終了する */
                return C_const_NG;

            }

            /* ポイント・顧客ロック */
            rtn_cd = KokyakuLock();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                /* 処理を終了する */
                return C_const_NG;

            }else if( rtn_cd == C_const_NOTEXISTS ) {
                continue;
            }

            /* MMマタニティベビー情報更新処理 */
            /*
            EXEC SQL UPDATE MMマタニティベビー情報
            SET 削除フラグ   = 2,
            退会年月日   = :h_batdate_t,
                    第１子名称 = null,
                    第１子カナ名称 = null,
                    第２子名称 = null,
                    第２子カナ名称 = null,
                    第３子名称 = null,
                    第３子カナ名称 = null,
                    バッチ更新日 = :h_batdate_t,
                    最終更新日   = :h_batdate_t,
                    最終更新日時 = sysdate,
                    最終更新プログラムＩＤ = :h_program_id
            WHERE  顧客番号  = :mmmb_t.kokyaku_no;
            */
            workSql = new StringDto();
            workSql.arr = "UPDATE MMマタニティベビー情報\n" +
                    "            SET 削除フラグ   = 2,\n" +
                    "            退会年月日   = ?,\n" +
                    "                    第１子名称 = null,\n" +
                    "                    第１子カナ名称 = null,\n" +
                    "                    第２子名称 = null,\n" +
                    "                    第２子カナ名称 = null,\n" +
                    "                    第３子名称 = null,\n" +
                    "                    第３子カナ名称 = null,\n" +
                    "                    バッチ更新日 = ?,\n" +
                    "                    最終更新日   = ?,\n" +
                    "                    最終更新日時 = sysdate(),\n" +
                    "                    最終更新プログラムＩＤ = ?\n" +
                    "            WHERE  顧客番号  = ?";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTenupBDto.h_batdate_t, cmBTenupBDto.h_batdate_t, cmBTenupBDto.h_batdate_t,
                    cmBTenupBDto.h_program_id, cmBTenupBDto.mmmb_t.kokyaku_no);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
                APLOG_WT( "904", 0, null, "UPDATE",
                        sqlca.sqlcode,
                        "MMマタニティベビー情報", out_format_buf, 0, 0);

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("マタニティベビー有効期限更新処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                /* 処理を終了する */
                return C_const_NG;

            }else if (sqlca.sqlcode == C_const_Ora_OK) {
                /* 正常の場合のみ                         */
                /* MMマタニティベビー情報更新件数カウントアップ */
                mmmb_data_cnt++;
            }

            /* MS顧客制度情報更新処理 */
            rtn_cd = UpdateSeido();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                /* 処理を終了する */
                return C_const_NG;
            }

            /* TS利用可能ポイント情報更新処理 */
            rtn_cd = UpdateRiyoKano();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE ENUP_MMBB01;
                sqlca.curse_close();

                /* 処理を終了する */
                return C_const_NG;
            }

            /* コミットする */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();
        }

        // EXEC SQL CLOSE ENUP_MMBB01;
        sqlca.curse_close();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("マタニティベビー有効期限更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertMBabyHist                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  InsertMBabyHist()                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      MMマタニティベビー履歴情報を登録する。                                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int InsertMBabyHist() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("InsertMBabyHist処理");
            /*------------------------------------------------------------*/
        }

        /* MMマタニティベビー履歴情報登録 */
        /*
        EXEC SQL INSERT INTO MMマタニティベビー履歴情報
                ( 顧客番号,
                        会員番号,
                        入会年月日,
                        会員種別,
                        ＤＭ止め区分,
                        Ｅメール止め区分,
                        第１子名称,
                        第１子カナ名称,
                        第１子性別,
                        第１子年齢,
                        第１子生年月日,
                        第２子名称,
                        第２子カナ名称,
                        第２子性別,
                        第２子年齢,
                        第２子生年月日,
                        第３子名称,
                        第３子カナ名称,
                        第３子性別,
                        第３子年齢,
                        第３子生年月日 )
        VALUES
                (   :mmmb_t.kokyaku_no,
                                    :h_kaiin_no,
                                    :mmmb_t.nyukai_ymd,
                                    :mmmb_t.kaiin_shubetsu,
                                    :mmmb_t.dm_tome_kbn,
                                    :mmmb_t.email_tome_kbn,
                                    :mmmb_t.dai1shi_mesho,
                                    :mmmb_t.dai1shi_kana_mesho,
                                    :mmmb_t.dai1shi_sebetsu,
                                    :mmmb_t.dai1shi_nenre,
                                    :mmmb_t.dai1shi_se_ymd,
                                    :mmmb_t.dai2shi_mesho,
                                    :mmmb_t.dai2shi_kana_mesho,
                                    :mmmb_t.dai2shi_sebetsu,
                                    :mmmb_t.dai2shi_nenre,
                                    :mmmb_t.dai2shi_se_ymd,
                                    :mmmb_t.dai3shi_mesho,
                                    :mmmb_t.dai3shi_kana_mesho,
                                    :mmmb_t.dai3shi_sebetsu,
                                    :mmmb_t.dai3shi_nenre,
                                    :mmmb_t.dai3shi_se_ymd );
        */
        StringDto workSql = new StringDto();
        workSql.arr = "\n" +
                "        INSERT INTO MMマタニティベビー履歴情報\n" +
                "                ( 顧客番号,\n" +
                "                        会員番号,\n" +
                "                        入会年月日,\n" +
                "                        会員種別,\n" +
                "                        ＤＭ止め区分,\n" +
                "                        Ｅメール止め区分,\n" +
                "                        第１子名称,\n" +
                "                        第１子カナ名称,\n" +
                "                        第１子性別,\n" +
                "                        第１子年齢,\n" +
                "                        第１子生年月日,\n" +
                "                        第２子名称,\n" +
                "                        第２子カナ名称,\n" +
                "                        第２子性別,\n" +
                "                        第２子年齢,\n" +
                "                        第２子生年月日,\n" +
                "                        第３子名称,\n" +
                "                        第３子カナ名称,\n" +
                "                        第３子性別,\n" +
                "                        第３子年齢,\n" +
                "                        第３子生年月日 )\n" +
                "        VALUES\n" +
                "                (  ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?,"+
                "                   ?)";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTenupBDto.mmmb_t.kokyaku_no,
                cmBTenupBDto.h_kaiin_no,
                cmBTenupBDto.mmmb_t.nyukai_ymd,
                cmBTenupBDto.mmmb_t.kaiin_shubetsu,
                cmBTenupBDto.mmmb_t.dm_tome_kbn,
                cmBTenupBDto.mmmb_t.email_tome_kbn,
                cmBTenupBDto.mmmb_t.dai1shi_mesho,
                cmBTenupBDto.mmmb_t.dai1shi_kana_mesho,
                cmBTenupBDto.mmmb_t.dai1shi_sebetsu,
                cmBTenupBDto.mmmb_t.dai1shi_nenre,
                cmBTenupBDto.mmmb_t.dai1shi_se_ymd,
                cmBTenupBDto.mmmb_t.dai2shi_mesho,
                cmBTenupBDto.mmmb_t.dai2shi_kana_mesho,
                cmBTenupBDto.mmmb_t.dai2shi_sebetsu,
                cmBTenupBDto.mmmb_t.dai2shi_nenre,
                cmBTenupBDto.mmmb_t.dai2shi_se_ymd,
                cmBTenupBDto.mmmb_t.dai3shi_mesho,
                cmBTenupBDto.mmmb_t.dai3shi_kana_mesho,
                cmBTenupBDto.mmmb_t.dai3shi_sebetsu,
                cmBTenupBDto.mmmb_t.dai3shi_nenre,
                cmBTenupBDto.mmmb_t.dai3shi_se_ymd);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "INSERT",
                    sqlca.sqlcode,
                    "MMマタニティベビー履歴情報", out_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("InsertMBabyHist処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;

        }else if (sqlca.sqlcode == C_const_Ora_OK) {
            /* 正常の場合のみ                       */
            /* MMマタニティベビー履歴情報更新件数カウントアップ */
            mmmr_data_cnt++;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("InsertMBabyHist処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return ( C_const_OK );              /* 処理終了                           */
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： KokyakuLock                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  KokyakuLock()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント・顧客ロック処理                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             -1   ： データなし                                             */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int KokyakuLock() {
        int     rtn_cd;             /* 関数戻り値                  */
        IntegerDto     rtn_status = new IntegerDto();         /* 関数ステータス              */

        /* 初期化 */
        rtn_cd = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ポイント・顧客ロック処理");
            /*---------------------------------------------------------------------*/
        }
        rtn_cd = C_KdataLock(cmBTenupBDto.mmmb_t.kokyaku_no.strDto(), "1", rtn_status);
        if( rtn_cd == C_const_NG ) {
            if (DBG_LOG) {
                /*-----------------------------------------------------------------*/
                C_DbgMsg("*** KokyakuLock *** ポイント・顧客ロック status= %d\n"
                        , rtn_status);
                /*-----------------------------------------------------------------*/
            }
            APLOG_WT( "903", 0, null, "C_KdataLock", rtn_cd,
                    rtn_status, 0, 0, 0);

            /* 処理を終了する */
            return C_const_NG;

        }else if( rtn_cd == C_const_NOTEXISTS ) {
            if (DBG_LOG) {
                /*-----------------------------------------------------------------*/
                C_DbgMsg("*** KokyakuLock *** データなし%s\n", "");
            }
            return C_const_NOTEXISTS;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("ポイント・顧客ロック処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateSeido                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateSeido()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報更新処理                                       */
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
    public int UpdateSeido() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MS顧客制度情報更新処理");
            /*---------------------------------------------------------------------*/
        }
        /* MS顧客制度情報更新処理 */
        /*
        EXEC SQL UPDATE MS顧客制度情報@CMSD
        SET エントリー   = エントリー  - :h_ProcKbn,
                バッチ更新日 = :h_batdate_t,
                最終更新日   = :h_batdate_t,
                最終更新日時 = sysdate,
                最終更新プログラムＩＤ = :h_program_id
        WHERE  顧客番号                =  :mmmb_t.kokyaku_no
        AND    MOD( エントリー, (:h_ProcKbn * 2)) / :h_ProcKbn >= 1;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "UPDATE MS顧客制度情報\n" +
                "                 SET エントリー   = エントリー  - ?, \n" +
                "                     バッチ更新日 = ?,\n" +
                "                     最終更新日   = ?,\n" +
                "                     最終更新日時 = sysdate(),\n" +
                "                     最終更新プログラムＩＤ = ?\n" +
                "                 WHERE  顧客番号                =  ?\n" +
                "                 AND    MOD( エントリー, (? * 2)) / ? >= 1";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTenupBDto.h_ProcKbn, cmBTenupBDto.h_batdate_t, cmBTenupBDto.h_batdate_t,
                cmBTenupBDto.h_program_id, cmBTenupBDto.mmmb_t.kokyaku_no, cmBTenupBDto.h_ProcKbn, cmBTenupBDto.h_ProcKbn);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "UPDATE",
                    sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;

        }else if (sqlca.sqlcode == C_const_Ora_OK) {
            /* 正常の場合のみ                       */
            /* MS顧客制度情報更新件数カウントアップ */
            msps_data_cnt++;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MS顧客制度情報更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateRiyoKano                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateRiyoKano()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TS利用可能ポイント情報更新処理                               */
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
    public int UpdateRiyoKano() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("TS利用可能ポイント情報更新処理");
            /*---------------------------------------------------------------------*/
        }
        /* TS利用可能ポイント情報更新処理 */
        /*
        EXEC SQL UPDATE TS利用可能ポイント情報@CMSD
        SET 出産クーポン発行日１ = 0,
                出産クーポン発行日２ = 0,
                出産クーポン発行日３ = 0,
        最終更新日   = :h_batdate_t,
                最終更新日時 = sysdate,
                最終更新プログラムＩＤ = :h_program_id
        WHERE  顧客番号            =  :mmmb_t.kokyaku_no;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "\n" +
                "        UPDATE TS利用可能ポイント情報\n" +
                "        SET 出産クーポン発行日１ = 0,\n" +
                "                出産クーポン発行日２ = 0,\n" +
                "                出産クーポン発行日３ = 0,\n" +
                "        最終更新日   = ?,\n" +
                "                最終更新日時 = sysdate(),\n" +
                "                最終更新プログラムＩＤ = ?\n" +
                "        WHERE  顧客番号            =  ?\n" +
                "        ";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTenupBDto.h_batdate_t, cmBTenupBDto.h_program_id, cmBTenupBDto.mmmb_t.kokyaku_no);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTenupBDto.mmmb_t.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "UPDATE",
                    sqlca.sqlcode,
                    "TS利用可能ポイント情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;

        }else if (sqlca.sqlcode == C_const_Ora_OK) {
            /* 正常の場合のみ                       */
            /* TS利用可能ポイント情報更新件数カウントアップ */
            tsrk_data_cnt++;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("TS利用可能ポイント情報更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }
}
