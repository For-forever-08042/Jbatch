package jp.co.mcc.nttdata.batch.business.service.cmBTschuB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlcaManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 出産クーポン発行可否更新処理（cmBTschuB）
 *
 *   【処理概要】
 *       マタニティベビーの各エントリー情報のお子様の出生年月日より、バッチ処理
 *       日付時点に出産クーポン発行可否の変更対象となる会員の出産クーポン発行可
 *       否を更新する。
 *
 *   【引数説明】
 *      -d処理対象日付    : 処理対象日付（省略時はバッチ処理日付）
 *      -DEBUG(-debug)    : デバッグモードでの実行
 *                         （トレース出力機能が有効）
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
 *      1.00 :  2013/01/11 SSI.本田 ： 初版
 *      2.00 :  2014/04/03 SSI.上野 ： 発行可否更新条件を年齢別に変更
 *     30.00 :  2021/02/02 NDBS.緒方:  期間限定Ｐ対応によりリコンパイル
 *                                     (顧客データロック処理内容更新のため)
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTschuBServiceImpl  extends CmABfuncLServiceImpl implements CmBTschuBService {

    boolean DBG_LOG = true;                    /* デバッグメッセージ出力     */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    /* 動的ＳＱＬ作成用 */
    ItemDto str_sql = new ItemDto(4096);  /* 実行用SQL文字列              */
    ItemDto str_sql2 = new ItemDto(8192);                /* 実行用SQL文字列            */

    /* 使用テーブルヘッダーファイルをインクルード                                 */
    ItemDto  h_kokyaku_no = new ItemDto(15+1);           /* 顧客番号                    */
    ItemDto  h_shussan_coupon_flg1 = new ItemDto();        /* 第１子出産クーポン発行可否  */
    ItemDto  h_shussan_coupon_flg2 = new ItemDto();        /* 第２子出産クーポン発行可否  */
    ItemDto  h_shussan_coupon_flg3 = new ItemDto();        /* 第３子出産クーポン発行可否  */
    ItemDto  h_batdate = new ItemDto();                  /* バッチ処理日付(当日)        */
    ItemDto  h_program_id = new ItemDto(21);             /* プログラムID                */
    ItemDto   h_ymd;                        /* 年月日                     */
    ItemDto   h_md = new ItemDto();                         /* 月日                       */
    ItemDto   h_year = new ItemDto();                       /* 年                         */
    ItemDto   h_month = new ItemDto();                      /* 月                         */
    ItemDto   h_day = new ItemDto();                        /* 日                         */
    ItemDto   h_leap_ymd = new ItemDto();                   /* 年月日(閏年)               */
    ItemDto   h_leap_md = new ItemDto();                    /* 月日(閏年)                 */
    ItemDto   h_leap_month = new ItemDto();                 /* 年(閏年)                   */
    ItemDto   h_leap_day = new ItemDto();                   /* 日(閏年)                   */
    ItemDto   h_dai1shi_nisuu = new ItemDto();              /* 第１子_日数                */
    ItemDto   h_dai1shi_se_y = new ItemDto();               /* 第１子_年                  */
    ItemDto   h_dai1shi_se_m = new ItemDto();               /* 第１子_月                  */
    ItemDto   h_dai1shi_se_d = new ItemDto();               /* 第１子_日                  */
    ItemDto   h_dai2shi_nisuu = new ItemDto();              /* 第２子_日数                */
    ItemDto   h_dai2shi_se_y = new ItemDto();               /* 第２子_年                  */
    ItemDto   h_dai2shi_se_m = new ItemDto();               /* 第２子_月                  */
    ItemDto   h_dai2shi_se_d = new ItemDto();               /* 第２子_日                  */
    ItemDto   h_dai3shi_nisuu = new ItemDto();              /* 第３子_日数                */
    ItemDto   h_dai3shi_se_y = new ItemDto();               /* 第３子_年                  */
    ItemDto   h_dai3shi_se_m = new ItemDto();               /* 第３子_月                  */
    ItemDto   h_dai3shi_se_d = new ItemDto();               /* 第３子_日                  */


    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF   =     0;       /* OFF                         */
    int DEF_ON    =     1;       /* ON                          */
    int UMU_ARI   =     1;       /* データ有無 - 有              */
    int UMU_NASI  =     0;       /* データ有無 - 無              */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_d = "-d";            /* 処理対象日付                   */
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ               */
    String DEF_debug = "-debug";        /* デバッグスイッチ               */
    /*---------------------------------------------*/
    String PG_NAME  =  "出産クーポン発行可否更新";  /* プログラム名称     */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    StringDto    bat_date_t = new StringDto(9);          /** バッチ処理日付(当日)       **/
    int     mmmb_data_cnt;      /** 処理対象件数（マタニティベビー情報     **/
    int     msks_data_cnt;      /** MS顧客制度情報更新件数                 **/
    int     msks_err_cnt;       /** MS顧客制度情報更新エラー件数           **/
    int     chk_arg_d                         ; /* 引数-dチェック用         */
    StringDto    arg_d_Value = new StringDto(256);                  ; /* 引数d設定値              */
    StringDto    out_format_buf = new StringDto(C_const_MsgMaxLen);      /** APログフォーマット   **/
    int     leap_yaer_flg;                          /* 閏年判定フラグ             */


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
    public MainResultDto main(int argc, String[] argv ) {
        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        int     rtn_cd;                         /** 関数戻り値                       **/
        int     arg_chk;                        /** 引数の種類チェック結果           **/
        IntegerDto rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int     arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto    wk_bat_date = new StringDto(9);                 /** バッチ処理日付                   **/
        StringDto    arg_Work1 = new StringDto(256) ;                /** Work Buffer1                     **/
        StringDto    wk_date = new StringDto(9);                     /** バッチ処理日付バッファ           **/

        /*                                               */
        /*  初期処理                                     */
        /*                                               */
        /** 変数初期化                **/
        memset( bat_date_t , 0x00 , sizeof(bat_date_t) ) ;
        memset( out_format_buf , 0x00 , sizeof(out_format_buf) ) ;
        memset( h_program_id , 0x00 , sizeof(h_program_id) ) ;
        chk_arg_d = DEF_OFF;
        memset( arg_d_Value, 0x00, sizeof(arg_d_Value) );
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
        strcpy( h_program_id , Cg_Program_Name );

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
            } else if( 0 == memcmp( arg_Work1, DEF_ARG_d, 2 ) ) {  /* -dの場合        */
                rtn_cd = cmBTschuB_Chk_Arg(arg_Work1);   /* パラメータCHK      */
                if ( rtn_cd == C_const_OK ) {
                    strcpy(arg_d_Value, arg_Work1.substring(2));
                } else {
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit( C_const_APNG );
                }
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

        /*-----------------------------------------------*/
        /* 処理日付を設定                                */
        /*-----------------------------------------------*/
        if (chk_arg_d == DEF_OFF)  {  /* IF バッチ処理日付を取得（当日） */

            /*-----------------------------------------------*/
            /* バッチ処理日付を取得（当日）                  */
            /*-----------------------------------------------*/
            rtn_cd = C_GetBatDate(0, wk_bat_date, rtn_status);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_GetBatDate",
                        rtn_cd, rtn_status, 0, 0, 0);
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
                return exit(C_const_APNG)    ;
            }
            /* バッチ処理日付(ホスト変数) */
            h_batdate.arr = atoi(wk_bat_date) ; /* バッチ処理日(当日) */
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 処理対象日付:バッチ処理日_当日 =[%s]\n", wk_bat_date);
                /*------------------------------------------------------------*/
            }
        } else {
            /* 引数d設定値(ホスト変数) */
            h_batdate.arr = atoi(arg_d_Value) ; /* 引数d設定値        */
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 処理対象日付:引数d設定値 =[%s]\n", arg_d_Value);
                /*------------------------------------------------------------*/
            }
        }  /* ENDIF バッチ処理日付を取得（当日） */

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日)取得OK [%d]\n", h_batdate);
            /*---------------------------------------------*/
        }

        /* 当日日付(年月日) */
        h_ymd = h_batdate;
        /* 当日日付(月日) */
        h_md.arr = h_ymd.intVal() % 10000;
        /* 当日日付(年) */
        h_year.arr = h_ymd.intVal() / 10000;
        /* 当日日付(月) */
        h_month.arr = (h_ymd.intVal() / 100) % 100;
        /* 当日日付(日) */
        h_day.arr = h_ymd.intVal() % 100;

        /* 平年における2月29日生まれ年齢更新用に、*/
        /* 3月1日当日に前日更新条件を付加設定する */
        leap_yaer_flg = 0;
        h_leap_ymd.arr = 99999999;
        h_leap_md.arr = 9999;
        h_leap_month.arr = 99;
        h_leap_day.arr = 99;
        /* 閏年チェック */
        if (h_year.intVal() % 100 == 0) {
            if (h_year.intVal() % 400 == 0) {
                /* 閏年 */
                leap_yaer_flg = 1;
            } else {
                /* 平年 */
            }
        } else {
            if (h_year.intVal() % 4 == 0) {
                /* 閏年 */
                leap_yaer_flg = 1;
            } else {
                /* 平年 */
            }
        }
        if (h_month.intVal() == 3 && h_day.intVal() == 1) {
            if (leap_yaer_flg != 1) {
                memset(wk_date, 0x00, sizeof(wk_date));
                sprintf(wk_date, "%04d", h_year);
                strcat(wk_date, "0229");
                /* 閏年(年月日) */
                h_leap_ymd.arr = atoi(wk_date);
                /* 閏年(月日) */
                h_leap_md.arr = 229;
                /* 閏年(月) */
                h_leap_month.arr = 2;
                /* 閏年(日) */
                h_leap_day.arr = 29;
            }
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 閏年判定フラグ：leap_yaer_flg=[%d]\n", leap_yaer_flg);
            C_DbgMsg("*** main *** バッチ日付：h_ymd=[%d]\n", h_ymd);
            C_DbgMsg("*** main *** バッチ日付：h_md=[%d]\n", h_md);
            C_DbgMsg("*** main *** バッチ日付：h_year=[%d]\n", h_year);
            C_DbgMsg("*** main *** バッチ日付：h_month=[%d]\n", h_month);
            C_DbgMsg("*** main *** バッチ日付：h_day=[%d]\n", h_day);
            C_DbgMsg("*** main *** 閏年：h_leap_ymd=[%d]\n", h_leap_ymd);
            C_DbgMsg("*** main *** 閏年：h_leap_md=[%d]\n", h_leap_md);
            C_DbgMsg("*** main *** 閏年：h_leap_month=[%d]\n", h_leap_month);
            C_DbgMsg("*** main *** 閏年：h_leap_day=[%d]\n", h_leap_day);
            /*-------------------------------------------------------------*/
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
        msks_data_cnt = 0;
        msks_err_cnt = 0;

        /*                                               */
        /*  主処理                                       */
        /*                                               */

        /* 出産クーポン発行可否更新処理 */
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 出産クーポン発行可否更新処理%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = UpdateShussanHakkouKahi();
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "912", 0, null,
                    "出産クーポン発行可否更新処理に失敗しました" , 0, 0, 0, 0, 0);

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
        APLOG_WT( "106", 0, null, "MMマタニティベビー情報" ,
                mmmb_data_cnt, msks_data_cnt, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MS顧客制度情報" ,
                msks_data_cnt, msks_err_cnt, 0, 0, 0);

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
                rtn_cd = C_KdataLock(h_kokyaku_no.strDto(), "1", rtn_status);
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
                /*-----------------------------------------------------------------*/
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
    /*  関数名 ： UpdateShussanHakkouKahi                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateShussanHakkouKahi()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               出産クーポン発行可否更新処理                                 */
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
    public int UpdateShussanHakkouKahi() {
        int     rtn_cd;             /* 関数戻り値                  */
        StringDto    wk_sql = new StringDto(C_const_SQLMaxLen)  ; /* 動的SQLバッファ               */
        StringDto    wk_sql2 = new StringDto(8192);               /* 動的SQLバッファ               */
        IntegerDto     wk_age = new IntegerDto();                    /* 年齢                                */
        StringDto    wk_md = new StringDto(5);                  /* 誕生日(月日)                        */

        /* 初期化 */
        rtn_cd = C_const_OK;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("出産クーポン発行可否更新処理");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(wk_sql2, 0x00, sizeof(wk_sql2));
        memset(str_sql2, 0x00, sizeof(str_sql2));

        /* MMマタニティベビー情報取得処理 */
        if ((h_month.intVal() == 3 && h_day.intVal() == 1) && leap_yaer_flg != 1) {
            /* 平年（閏年以外）かつ3/1 */
            sprintf(wk_sql2,
                    "SELECT " +
                    "顧客番号, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第１子生年月日, 0), '0', 19000101, 第１子生年月日)AS TEXT), 'yyyymmdd') as 日数１, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第２子生年月日, 0), '0', 19000101, 第２子生年月日)AS TEXT), 'yyyymmdd') as 日数２, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第３子生年月日, 0), '0', 19000101, 第３子生年月日)AS TEXT), 'yyyymmdd') as 日数３, " +
                            "TRUNC(NVL(第１子生年月日, 0)/10000) as 第１子生年, " +
                            "TRUNC(MOD(NVL(第１子生年月日,0),10000)/100) as 第１子生月, " +
                            "MOD(NVL(第１子生年月日,0),100) as 第１子生日, " +
                            "TRUNC(NVL(第２子生年月日, 0)/10000) as 第２子生年, " +
                            "TRUNC(MOD(NVL(第２子生年月日,0),10000)/100) as 第２子生月, " +
                            "MOD(NVL(第２子生年月日,0),100) as 第２子生日, " +
                            "TRUNC(NVL(第３子生年月日, 0)/10000) as 第３子生年, " +
                            "TRUNC(MOD(NVL(第３子生年月日,0),10000)/100) as 第３子生月, " +
                            "MOD(NVL(第３子生年月日,0),100) as 第３子生日 " +
                            "FROM " +
                            "MMマタニティベビー情報 " +
                            "WHERE  ( " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第１子生年月日, 0), '0', 19000101, 第１子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            "OR " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第２子生年月日, 0), '0', 19000101, 第２子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            "OR " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第３子生年月日, 0), '0', 19000101, 第３子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            ") OR ( " +
                            "MOD(NVL(第１子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第２子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第３子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第１子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第２子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第３子生年月日, 0), 10000) = %d " +
                            ") "
                    , h_batdate, h_batdate, h_batdate,
                    h_batdate, h_batdate, h_batdate,
                    h_md, h_md, h_md, h_leap_md, h_leap_md, h_leap_md );
        } else {
            sprintf(wk_sql2,
                    "SELECT " +
                            "顧客番号, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第１子生年月日, 0), '0', 19000101, 第１子生年月日)AS TEXT), 'yyyymmdd') as 日数１, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第２子生年月日, 0), '0', 19000101, 第２子生年月日)AS TEXT), 'yyyymmdd') as 日数２, " +
                            "to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第３子生年月日, 0), '0', 19000101, 第３子生年月日)AS TEXT), 'yyyymmdd') as 日数３, " +
                            "TRUNC(NVL(第１子生年月日, 0)/10000) as 第１子生年, " +
                            "TRUNC(MOD(NVL(第１子生年月日,0),10000)/100) as 第１子生月, " +
                            "MOD(NVL(第１子生年月日,0),100) as 第１子生日, " +
                            "TRUNC(NVL(第２子生年月日, 0)/10000) as 第２子生年, " +
                            "TRUNC(MOD(NVL(第２子生年月日,0),10000)/100) as 第２子生月, " +
                            "MOD(NVL(第２子生年月日,0),100) as 第２子生日, " +
                            "TRUNC(NVL(第３子生年月日, 0)/10000) as 第３子生年, " +
                            "TRUNC(MOD(NVL(第３子生年月日,0),10000)/100) as 第３子生月, " +
                            "MOD(NVL(第３子生年月日,0),100) as 第３子生日 " +
                            "FROM " +
                            "MMマタニティベビー情報 " +
                            "WHERE  ( " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第１子生年月日, 0), '0', 19000101, 第１子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            "OR " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第２子生年月日, 0), '0', 19000101, 第２子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            "OR " +
                            "(to_date(%d, 'yyyymmdd') - to_date(CAST(DECODE(NVL(第３子生年月日, 0), '0', 19000101, 第３子生年月日)AS TEXT), 'yyyymmdd') = 184) " +
                            ") OR ( " +
                            "MOD(NVL(第１子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第２子生年月日, 0), 10000) = %d " +
                            "OR " +
                            "MOD(NVL(第３子生年月日, 0), 10000) = %d " +
                            ") "
                    , h_batdate, h_batdate, h_batdate,
                    h_batdate, h_batdate, h_batdate,
                    h_md, h_md, h_md );
        }
        /* ＨＯＳＴ変数にセット */
        strcpy( str_sql2, wk_sql2 );
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateShussanHakkouKahi *** SQL=[%s]\n", wk_sql2);
            /*-------------------------------------------------------------*/
        }

        /* 動的ＳＱＬ文の解析 */
        // EXEC SQL PREPARE sql_stat1 from :str_sql2;
        SqlstmDto sql_stat1 = sqlcaManager.get("sql_stat1");
        sql_stat1.sql = wk_sql2;
        sql_stat1.prepare();

        if (sql_stat1.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** UpdateShussanHakkouKahi *** 動的ＳＱＬ 解析NG = %d\n",
                        sql_stat1.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT( "902", 0, null, sql_stat1.sqlcode, wk_sql2, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
        // EXEC SQL DECLARE CUR_SCHU1 cursor for sql_stat1;
        sql_stat1.declare();

        /* カーソルオープン */
        //EXEC SQL OPEN CUR_SCHU1;
        sql_stat1.open();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** UpdateShussanHakkouKahi *** MMマタニティベビー情報 CURSOR OPEN "+
                    "sqlcode =[%d]\n", sql_stat1.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sql_stat1.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** UpdateShussanHakkouKahi *** 動的SQL OPEN NG =[%d]\n",
                        sql_stat1.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sql_stat1.sqlcode, "CURSOR OPEN ERR",
                    0, 0, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));

        /* MS顧客制度情報更新 */
        sprintf(wk_sql,
                "UPDATE  MS顧客制度情報 " +
                "SET " +
                "      出産クーポン発行可否１ = ?, " +
                "      出産クーポン発行可否２ = ?, " +
                "      出産クーポン発行可否３ = ?, " +
                "      バッチ更新日 = ?, " +
                "      最終更新日 = ?, " +
                "      最終更新日時 = sysdate(), " +
                "      最終更新プログラムＩＤ = ? " +
                "WHERE 顧客番号 = ?");
        /* ＨＯＳＴ変数にセット */
        strcpy( str_sql, wk_sql );
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** pdateShussanHakkouKahi *** SQL=[%s]\n", wk_sql);
            /*-------------------------------------------------------------*/
        }

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE UPD_SCHU1 from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateShussanHakkouKahi *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", h_kokyaku_no );
            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            return C_const_NG;
        }

        /* データが終了するまでフェッチを繰り返す */
        while( true ) {

            /* 初期化 */
            memset(h_kokyaku_no, 0x00, sizeof(h_kokyaku_no));
            h_dai1shi_nisuu.arr = 0;
            h_dai1shi_se_y.arr = 0;
            h_dai1shi_se_m.arr = 0;
            h_dai1shi_se_d.arr = 0;
            h_dai2shi_nisuu.arr = 0;
            h_dai2shi_se_y.arr = 0;
            h_dai2shi_se_m.arr = 0;
            h_dai2shi_se_d.arr = 0;
            h_dai3shi_nisuu.arr = 0;
            h_dai3shi_se_y.arr = 0;
            h_dai3shi_se_m.arr = 0;
            h_dai3shi_se_d.arr = 0;

            /* カーソルフェッチ */
            /*
            EXEC SQL FETCH CUR_SCHU1
            INTO :h_kokyaku_no,
                 :h_dai1shi_nisuu,
                 :h_dai2shi_nisuu,
                 :h_dai3shi_nisuu,
                 :h_dai1shi_se_y,
                 :h_dai1shi_se_m,
                 :h_dai1shi_se_d,
                 :h_dai2shi_se_y,
                 :h_dai2shi_se_m,
                 :h_dai2shi_se_d,
                 :h_dai3shi_se_y,
                 :h_dai3shi_se_m,
                 :h_dai3shi_se_d;
            */
            sql_stat1.fetch();
            sql_stat1.recData(h_kokyaku_no,
            h_dai1shi_nisuu,
            h_dai2shi_nisuu,
            h_dai3shi_nisuu,
            h_dai1shi_se_y,
            h_dai1shi_se_m,
            h_dai1shi_se_d,
            h_dai2shi_se_y,
            h_dai2shi_se_m,
            h_dai2shi_se_d,
            h_dai3shi_se_y,
            h_dai3shi_se_m,
            h_dai3shi_se_d);

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sql_stat1.sqlcode != C_const_Ora_OK &&
                    sql_stat1.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateShussanHakkouKahi *** MMマタニティベビー情報 "+
                            "FETCH NG sqlcode =[%d]\n", sql_stat1.sqlcode);
                }

                sprintf(out_format_buf, "誕生月=%02d 誕生日=%02d 誕生月(閏年)=%02d "+
                        "誕生日(閏年)=%02d", h_month, h_day, h_leap_month, h_leap_day);
                APLOG_WT("904", 0, null, "FETCH", sql_stat1.sqlcode,
                        "MMマタニティベビー情報", out_format_buf, 0, 0);
                // EXEC SQL CLOSE CUR_SCHU1; /* カーソルクローズ */
                sql_stat1.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }
            /* データ無しの場合、ループを抜ける */
            if (sql_stat1.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /* MMマタニティベビー情報_処理対象件数カウントアップ */
            mmmb_data_cnt++;

            /* 第一子 */
            /* 新年齢取得(第１子) */
            wk_age.arr = 0;
            rtn_cd = C_CountAge(h_dai1shi_se_y.intVal(), h_dai1shi_se_m.intVal(), h_dai1shi_se_d.intVal(),
                    h_year.intVal(), h_month.intVal(), h_day.intVal(), wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge", rtn_cd,
                        0 ,0 ,0 ,0);
                // EXEC SQL CLOSE CUR_SCHU1; /* カーソルクローズ */
                sql_stat1.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 発行可否判定(第１子) */
            h_shussan_coupon_flg1.arr = cmBTschuB_Chk_HakkoKahi(wk_age.arr, h_dai1shi_nisuu.intVal());

            /* 新年齢取得(第２子) */
            wk_age.arr = 0;
            rtn_cd = C_CountAge(h_dai2shi_se_y.intVal(), h_dai2shi_se_m.intVal(), h_dai2shi_se_d.intVal(),
                    h_year.intVal(), h_month.intVal(), h_day.intVal(), wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge", rtn_cd,
                        0 ,0 ,0 ,0);
                // EXEC SQL CLOSE CUR_SCHU1; /* カーソルクローズ */
                sql_stat1.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 発行可否判定(第２子) */
            h_shussan_coupon_flg2.arr = cmBTschuB_Chk_HakkoKahi(wk_age.arr, h_dai2shi_nisuu.intVal());

            /* 新年齢取得(第３子) */
            wk_age.arr = 0;
            rtn_cd = C_CountAge(h_dai3shi_se_y.intVal(), h_dai3shi_se_m.intVal(), h_dai3shi_se_d.intVal(),
                    h_year.intVal(), h_month.intVal(), h_day.intVal(), wk_age);
            if (rtn_cd != C_const_OK){
                APLOG_WT("903", 0, null, "C_CountAge", rtn_cd,
                        0 ,0 ,0 ,0);
                // EXEC SQL CLOSE CUR_SCHU1; /* カーソルクローズ */
                sql_stat1.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 発行可否判定(第３子) */
            h_shussan_coupon_flg3.arr = cmBTschuB_Chk_HakkoKahi(wk_age.arr, h_dai3shi_nisuu.intVal());

            /* ポイント・顧客ロック */
            rtn_cd = KokyakuLock();
            if( rtn_cd == C_const_NG ) {

                // EXEC SQL CLOSE CUR_SCHU1;
                sql_stat1.curse_close();

                /* 処理を終了する */
                return C_const_NG;

            }else if( rtn_cd == C_const_NOTEXISTS ) {

                memset( out_format_buf , 0x00 , sizeof(out_format_buf) ) ;
                sprintf( out_format_buf, "顧客ロックエラー：顧客番号=[%s]", h_kokyaku_no);
                APLOG_WT("700", 0, null, out_format_buf, 0, 0, 0, 0, 0);

                /* エラー件数カウントアップ */
                msks_err_cnt++;

                continue;
            }

            /* MMマタニティベビー情報更新処理 */
            /*
            EXEC SQL EXECUTE UPD_SCHU1 using
            :h_shussan_coupon_flg1,
                                    :h_shussan_coupon_flg2,
                                    :h_shussan_coupon_flg3,
                                    :h_batdate,
                                    :h_batdate,
                                    :h_program_id,
                                    :h_kokyaku_no ;
            */
            sqlca.fetch();
            sqlca.recData(h_shussan_coupon_flg1,
                                    h_shussan_coupon_flg2,
                                    h_shussan_coupon_flg3,
                                    h_batdate,
                                    h_batdate,
                                    h_program_id,
                                    h_kokyaku_no);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf( out_format_buf, "顧客番号=[%s]", h_kokyaku_no );
                APLOG_WT( "904", 0, null, "UPDATE",
                        sqlca.sqlcode,
                        "MS顧客制度情報", out_format_buf, 0, 0);

                // EXEC SQL CLOSE CUR_SCHU1;
                sql_stat1.curse_close();

                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("出産クーポン発行可否更新処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                /* 処理を終了する */
                return C_const_NG;

            }else if (sqlca.sqlcode == C_const_Ora_OK) {
                /* 正常の場合のみ                         */
                /* MS顧客制度情報更新件数カウントアップ */
                msks_data_cnt++;
            }

            /* コミットする */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();
        }

        // EXEC SQL CLOSE CUR_SCHU1;
        sql_stat1.curse_close();

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("出産クーポン発行可否更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTschuB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTschuB_Chk_Arg(char *Arg_in)                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
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
    public int cmBTschuB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTschuB_Chk_Arg処理");
            C_DbgMsg("*** cmBTschuB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset( out_format_buf, 0x00, sizeof(out_format_buf) );

        if (0 == memcmp( Arg_in, DEF_ARG_d, 2 ) ) {        /* -d処理対象日付CHK   */
            if( chk_arg_d != DEF_OFF ) {
                sprintf( out_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_d = DEF_ON;

            if ( strlen(Arg_in) != 10) {   /* 桁数チェック           */
                sprintf( out_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTschuB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTschuB_Chk_HakkoKahi                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTschuB_Chk_HakkoKahi(int age, int days)                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              年齢と日数からクーポン発行可否を判定する                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int         age      ：年齢                                           */
    /*      int         days     ：日数                                           */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0～7 ：クーポン発行可否(0～7)                                 */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTschuB_Chk_HakkoKahi(int age, int days) {
        int hakkoKahi = 0;          /* 発行可否 */
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTschuB_Chk_HakkoKahi処理");
            C_DbgMsg("*** cmBTschuB_Chk_HakkoKahi *** 年齢=[%d]\n", age);
            C_DbgMsg("*** cmBTschuB_Chk_HakkoKahi *** 日数=[%d]\n", days);
            /*---------------------------------------------------------------------*/
        }

        switch (age) {
            case 0:
                if ( days <= 365 && days >= 184 ) {
                    /* 184日以上：6か月 */
                    hakkoKahi = 2;
                } else if (days < 184) {
                    /* 0歳 */
                    hakkoKahi = 1;
                } else {
                    hakkoKahi = 0;
                }
                break;
            case 1:
                /* 1歳 */
                hakkoKahi = 3;
                break;
            case 2:
                /* 2歳 */
                hakkoKahi = 4;
                break;
            case 3:
                /* 3歳 */
                hakkoKahi = 5;
                break;
            case 4:
                /* 4歳 */
                hakkoKahi = 6;
                break;
            case 5:
                /* 5歳 */
                hakkoKahi = 7;
                break;
            default:
                /* 6歳以上 */
                hakkoKahi = 0;
                break;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTschuB_Chk_HakkoKahi処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return hakkoKahi;
    }
}
