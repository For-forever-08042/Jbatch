package jp.co.mcc.nttdata.batch.business.service.cmBTpocdB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTpocdB.dto.CmBTpocdBDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 郵便番号コード設定（cmBTpocdB）
 *
 *   【処理概要】
 *       顧客属性マスターの郵便番号コード未設定のものに対して、
 *       郵便番号コードを設定する
 *
 *   【引数説明】
 *    -[zip｜zmc]    :  MM顧客属性情報の入れ替えが行われている場合、"-zmc"を指定
 *                      それ以外の場合、"-zip"を指定
 *    -d処理日付     :  処理日付
 *    -DEBUG(-debug) :  デバッグモードでの実行
 *                     （トレース出力機能が有効）
 *
 *   【戻り値】
 *      10    ： 正常
 *      99    ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 : 2012/12/11 SSI.吉岡：初版
 *      3.00 : 2013/08/16 SSI.本田：共通の住所取得処理SQLをバインド変数化したため
 *                                  再ビルド
 *     30.00 : 2021/02/02 NDBS.緒方:期間限定Ｐ対応によりリコンパイル
 *                                  (顧客データロック処理内容更新のため)
 *     40.00 : 2022/09/30 SSI.川内：MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTpocdBServiceImpl extends CmABfuncLServiceImpl implements CmBTpocdBService {

    boolean DBG_LOG = true;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTpocdBDto cmBTpocdBDto = new CmBTpocdBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;                       /* OFF                          */
    int DEF_ON = 1;                       /* ON                           */
    String PG_NAME = "郵便番号コード設定";        /* プログラム名称               */
    String DEF_NO_ZIP = "00000000000000000000000";  /* 郵便番号コード設定対象       */
    /*-----   引数（引数の種類分定義する）----------*/
    String DEF_ARG_ZIP = "-zip";                    /* 郵便番号コード設定対象       */
    String DEF_ARG_ZMC = "-zmc";                    /* 郵便番号コード設定対象       */
    String DEF_ARG_D = "-d";                  /* 処理日付                     */
    String DEF_DEBUG = "-DEBUG";                  /* デバッグスイッチ             */
    String DEF_debug = "-debug";                  /* デバッグスイッチ             */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int     arg_d_chk;                                   /** 引数dチェック用     **/
    StringDto arg_d_Value = new StringDto(256);                            /** 引数d設定値         **/
    StringDto arg_z_Value = new StringDto(5);                              /** 引数[-zip|-zmc]     **/
    /*---------------------------------------------*/
    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);           /** APログフォーマット  **/
    int     in_data_cnt;                           /* MM顧客属性情報_処理対象件数 */
    int     ok_data_cnt;                           /* MM顧客属性情報_正常処理件数 */


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
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int     rtn_cd;                         /** 関数戻り値                       **/
        IntegerDto rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int     arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto    arg_Work1 = new StringDto(256) ;                /** Work Buffer1                     **/
        StringDto    batdate_t = new StringDto(9);                   /** バッチ処理日付(当日)             **/

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /*-----------------------------------------------*/
        /*  プログラム名取得処理                         */
        /*-----------------------------------------------*/
        rtn_cd = C_GetPgname( argv );
        if (rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return  exit(C_const_APNG) ;
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /*-----------------------------------------------*/
        /*  バッチデバッグ開始処理                       */
        /*-----------------------------------------------*/
        rtn_cd = C_StartBatDbg( argc, argv );
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg" , rtn_cd,
                    0, 0, 0, 0);
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
        memset(arg_z_Value, 0x00, sizeof(arg_z_Value));
        memset(arg_d_Value, 0x00, sizeof(arg_d_Value));
        memset(batdate_t, 0x00, sizeof(batdate_t));
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "START");
            /*---------------------------------------------------------------------*/
        }
                /*** 引数チェック ***/
                rtn_cd = C_const_OK;                /* 関数戻り値 */
        for (arg_cnt = 1; arg_cnt < argc ; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
            } else if (0 == memcmp(arg_Work1, DEF_ARG_ZIP, 4)) {
                /* 郵便番号コード設定対象 引数[-zip] */
                strcpy(arg_z_Value, DEF_ARG_ZIP);

            } else if (0 == memcmp(arg_Work1, DEF_ARG_ZMC, 4)) {
                /* 郵便番号コード設定対象 引数[-zmc] */
                strcpy(arg_z_Value, DEF_ARG_ZMC);

            } else if (0 == memcmp(arg_Work1, DEF_ARG_D, 2)) {
                /* 処理日付 -d */
                rtn_cd = Chk_ArgdInf(arg_Work1);

            } else {
                /* 規定外パラメータ */
                rtn_cd  = C_const_NG ;
                sprintf(out_format_buf, "定義外の引数（%s）", arg_Work1);
            }

            /* パラメータのチェック結果がNG */
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------*/
                    C_DbgMsg("*** main *** パラメータチェックNG = [%d]\n", rtn_cd);
                    C_DbgEnd("*** main ***", C_const_APNG, 0, 0);
                    /*-----------------------------------------------------*/
                }
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                return  exit(C_const_APNG);
            }
        }

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** パラメータチェック結果OK = [%d]\n", rtn_cd);
            /*-------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  DBコネクト処理                               */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_MD, rtn_status );
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
                C_DbgEnd("*** main ***", C_const_APNG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect", rtn_cd,
                    rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return  exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトOK rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトOK status= %d\n", rtn_status);
            /*-------------------------------------------------------------*/
        }

        /* 処理日付が設定されていない場合 */
        if (StringUtils.isEmpty(arg_d_Value.arr)) {
            /*-----------------------------------------------*/
            /*  バッチ処理日取得処理呼び出し                 */
            /*-----------------------------------------------*/
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(前日)取得%s\n", "");
                /*--------------------------------------------------------------------*/
            }
            rtn_cd = C_GetBatDate(-1, arg_d_Value, rtn_status );
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------*/
                    C_DbgMsg("*** main *** バッチ処理日(前日)取得NG rtn= %d\n", rtn_cd);
                    C_DbgMsg("*** main *** バッチ処理日(前日)取得NG status= %d\n", rtn_status);
                    C_DbgEnd("*** main ***", C_const_APNG, 0, 0);
                    /*---------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_GetBatDate",
                        rtn_cd, rtn_status, 0, 0, 0);
                return  exit(C_const_APNG);
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(前日)取得OK [%s]\n", arg_d_Value);
                /*-------------------------------------------------------------------*/
            }
        }

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日)取得%s\n", "");
            /*--------------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate(0, batdate_t, rtn_status );
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG status= %d\n", rtn_status);
                C_DbgEnd("*** main ***", C_const_APNG, 0, 0);
                /*---------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            return  exit(C_const_APNG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(当日)取得OK [%s]\n", batdate_t);
            /*-------------------------------------------------------------------*/
        }
        /* HOST変数に設定 */
        cmBTpocdBDto.h_batdate_t.arr = atoi(batdate_t);

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTpocdB_main();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTpocdB_main NG rtn =[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "郵便番号コード設定処理に失敗しました" ,
                    0, 0, 0, 0, 0);

            /* ロールバック */
            //EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            return  exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* MM顧客属性情報 各件数出力 */
        APLOG_WT("106", 0, null, "MM顧客属性情報", in_data_cnt,
                ok_data_cnt, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        /*  終了メッセージ */
        APLOG_WT( "103", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* コミット解放処理 */
        //EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        return  exit(C_const_APOK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpocdB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpocdB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*             郵便番号コード設定処理                                         */
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
    public int cmBTpocdB_main() {
        int     rtn_cd;                    /* 関数戻り値                          */
        IntegerDto     rtn_status = new IntegerDto();                /* 関数ステータス                      */
        StringDto    wk_kokyaku_no = new StringDto(16);         /* 顧客番号                            */
        StringDto    wk_yubin_no = new StringDto(10+1);         /* 郵便番号                            */
        /* 2022/09/30 MCCM初版 MOD START */
        /*    char    wk_jusho1[10*3+1];*/         /* 住所１                              */
        /*    char    wk_jusho2[80*3+1];*/         /* 住所２                              */
        /*    char    wk_jusho3[80*3+1];*/         /* 住所３                              */
        StringDto    wk_address = new StringDto(200*3+1);         /* 住所                              */
        /* 2022/09/30 MCCM初版 MOD END */
        StringDto    custbarcode = new StringDto(24);           /* カスタマーバーコード                */
        StringDto    wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ                     */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTpocdB_main *** 郵便番号コード設定処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTpocdBDto.str_sql, 0x00, sizeof(cmBTpocdBDto.str_sql));

        /* HOST変数に設定 */
        cmBTpocdBDto.h_batdate_y.arr = atoi(arg_d_Value);

        /* MM顧客属性情報検索ＳＱＬの作成 */
        /* 郵便番号コード設定対象の値が"-zip"の場合 */
        /* 住所コード入れ替えステータスファイルが存在しない */
        if (0 == memcmp(arg_z_Value, DEF_ARG_ZIP, 4)) {
            sprintf(wk_sql,
                    "SELECT to_char(顧客番号, 'FM000000000000000')," +
                    " nvl(rpad(郵便番号,length(郵便番号)),' ')," +
                    /* 2022/09/30 MCCM初版 MOD START */
                    /*                  " nvl(住所１,' '),"
                    " nvl(住所２,' '),"
                    " nvl(住所３,' ')"*/
                    " nvl(rpad(住所,length(住所)),' ')" +
                            /* 2022/09/30 MCCM初版 MOD END */
                    "  FROM  MM顧客属性情報" +
                    "  WHERE 郵便番号コード = '00000000000000000000000'" +
                    "  AND   最終更新日    >= %d "
                    , cmBTpocdBDto.h_batdate_y);

            /* 郵便番号コード設定対象の値が"-zmc"の場合 */
            /* 住所コード入れ替えステータスファイルが存在する */
        } else if (0 == memcmp(arg_z_Value, DEF_ARG_ZMC, 4)) {
            sprintf(wk_sql,
                    "SELECT to_char(顧客番号, 'FM000000000000000')," +
                    "  nvl(rpad(郵便番号,length(郵便番号)),' ')," +
                    /* 2022/09/30 MCCM初版 MOD START */
                    /*                  "  nvl(住所１,' '),"
                    "  nvl(住所２,' '),"
                    "  nvl(住所３,' ')"*/
                    "  nvl(rpad(住所,length(住所)),' ')" +
                    /* 2022/09/30 MCCM初版 MOD END */
                    "  FROM  MM顧客属性情報" +
                            "  WHERE 郵便番号コード  =  '00000000000000000000000'"
            );
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpocdB_main *** 動的ＳＱＬ =[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpocdBDto.str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
        // EXEC SQL PREPARE sql_stat1 from :str_sql;
      sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpocdB_main *** 動的ＳＱＬ 解析NG = %d\n",
                        sqlca.sqlcode);
                C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT( "902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
        // EXEC SQL DECLARE POCD_MMKZ01 cursor for sql_stat1;
        sqlca.declare();

        /* カーソルオープン */
        // EXEC SQL OPEN POCD_MMKZ01;
        sqlca.open();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpocdB_main *** MM顧客属性情報 CURSOR OPEN " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpocdB_main *** CURSOR OPEN ERR =[%d]\n",
                        sqlca.sqlcode);
                C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, "CURSOR OPEN ERR",
                    0, 0, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* データが終了するまでフェッチを繰り返す */
        while( true ) {
            /* 初期化 */
            memset(cmBTpocdBDto.mmkozkinf_t.kokyaku_no.arr, 0x00,
                    sizeof(cmBTpocdBDto.mmkozkinf_t.kokyaku_no.arr));
            /* 2022/09/30 MCCM初版 MOD START */
            /*        memset(mmkozkinf_t.jusho_1, 0x00, sizeof(mmkozkinf_t.jusho_1));
                    memset(mmkozkinf_t.jusho_2, 0x00, sizeof(mmkozkinf_t.jusho_2));
                    memset(mmkozkinf_t.jusho_3, 0x00, sizeof(mmkozkinf_t.jusho_3));*/
            memset(cmBTpocdBDto.mmkozkinf_t.address, 0x00, sizeof(cmBTpocdBDto.mmkozkinf_t.address));
            /* 2022/09/30 MCCM初版 MOD END */

            /* カーソルフェッチ */
            /*
            EXEC SQL FETCH POCD_MMKZ01
            INTO :mmkozkinf_t.kokyaku_no,
             :mmkozkinf_t.yubin_no,
            *//* 2022/09/30 MCCM初版 MOD START *//*
*//*             :mmkozkinf_t.jusho_1,
             :mmkozkinf_t.jusho_2,
             :mmkozkinf_t.jusho_3;*//*
             :mmkozkinf_t.address;
             */
            /* 2022/09/30 MCCM初版 MOD END */
            sqlca.fetch();
            sqlca.recData(cmBTpocdBDto.mmkozkinf_t.kokyaku_no, cmBTpocdBDto.mmkozkinf_t.yubin_no, cmBTpocdBDto.mmkozkinf_t.address);

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpocdB_main *** MM顧客属性情報 "+
                            "FETCH ERR sqlcode =[%d]\n", sqlca.sqlcode);
                    C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                }
                APLOG_WT("902", 0, null, sqlca.sqlcode, "FETCH ERR",
                        0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE POCD_MMKZ01;
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* データ無しの場合、ループを抜ける */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /* 初期化 */
            memset(wk_kokyaku_no, 0x00, sizeof(wk_kokyaku_no));
            memset(wk_yubin_no, 0x00, sizeof(wk_yubin_no));
            /* 2022/09/30 MCCM初版 MOD START */
            /*        memset(wk_jusho1, 0x00, sizeof(wk_jusho1));
                memset(wk_jusho2, 0x00, sizeof(wk_jusho2));
                memset(wk_jusho3, 0x00, sizeof(wk_jusho3));*/
            memset(wk_address, 0x00, sizeof(wk_address));
            /* 2022/09/30 MCCM初版 MOD END */

            strcpy(wk_kokyaku_no, cmBTpocdBDto.mmkozkinf_t.kokyaku_no.strVal());

            strcpy(wk_yubin_no, cmBTpocdBDto.mmkozkinf_t.yubin_no.strVal());
            /* 2022/09/30 MCCM初版 MOD START */
/*        strcpy(wk_jusho1, (char *)mmkozkinf_t.jusho_1);
        strcpy(wk_jusho2, (char *)mmkozkinf_t.jusho_2);
        strcpy(wk_jusho3, (char *)mmkozkinf_t.jusho_3);*/
            strcpy(wk_address, cmBTpocdBDto.mmkozkinf_t.address.strVal());
            /* 2022/09/30 MCCM初版 MOD END */

            BT_Rtrim(wk_yubin_no, sizeof(wk_yubin_no) - 1);
            /* 2022/09/30 MCCM初版 MOD START */
/*        BT_Rtrim(wk_jusho1, sizeof(wk_jusho1) - 1);
        BT_Rtrim(wk_jusho2, sizeof(wk_jusho2) - 1);
        BT_Rtrim(wk_jusho3, sizeof(wk_jusho3) - 1);*/
            BT_Rtrim(wk_address, sizeof(wk_address) - 1);
            /* 2022/09/30 MCCM初版 MOD END */

            /* 郵便番号コード変換処理 */
            /* 2022/09/30 MCCM初版 MOD START */
/*        rtn_cd = C_GetPostBarCode(wk_jusho1, wk_jusho2, wk_jusho3,
                                  wk_yubin_no, custbarcode, &rtn_status);*/
            rtn_cd = C_GetPostBarCode(wk_address, wk_yubin_no, custbarcode, rtn_status);
            /* 2022/09/30 MCCM初版 MOD END */

            /* 郵便番号コード変換処理が異常の場合 */
            if (rtn_cd != C_const_OK) {

                APLOG_WT("903", 0, null, "C_GetPostBarCode", rtn_cd,
                        rtn_status, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE POCD_MMKZ01;
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 郵便番号コード変換処理結果がALLゼロならば該当レコードの更新をスキップする */
            if (0 == memcmp(custbarcode, DEF_NO_ZIP, 23)) {
                continue;
            }
            /* MM顧客属性情報_処理対象件数をカウントアップ */
            in_data_cnt++;

            /* 顧客データロック */
            rtn_cd = C_KdataLock(wk_kokyaku_no, "1", rtn_status);

            /* 顧客ロック対象レコード無しの場合 */
            if (rtn_cd == C_const_NOTEXISTS) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpocdB_main *** 顧客ロックNG 対象レコード無し" +
                            "status= %d\n", rtn_status);
                    C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                }
                APLOG_WT("913", 0, null, wk_kokyaku_no, 0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE POCD_MMKZ01;
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客ロック対象レコードなし以外のエラーの場合 */
            if (rtn_cd != C_const_OK && rtn_cd != C_const_NOTEXISTS) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpocdB_main *** 顧客ロック ERR" +
                            "status= %d\n", rtn_status);
                    C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                    /*-----------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd,
                        rtn_status, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE POCD_MMKZ01;
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            memset(cmBTpocdBDto.mmkozkinf_t.yubin_no_cd, 0x00, sizeof(cmBTpocdBDto.mmkozkinf_t.yubin_no_cd));
            strcpy(cmBTpocdBDto.mmkozkinf_t.yubin_no_cd, custbarcode);

            /* HOST変数にバージョン付きプログラム名をコピーする */
            memcpy(cmBTpocdBDto.mmkozkinf_t.saishu_koshin_programid, Cg_Program_Name,
                    sizeof(Cg_Program_Name));

            /* MM顧客属性情報のレコードを更新(update)する */
            /*
            EXEC SQL UPDATE MM顧客属性情報
            SET    郵便番号コード         = :mmkozkinf_t.yubin_no_cd,
                    バッチ更新日           = :h_batdate_t,
                    最終更新日             = :h_batdate_t,
                    最終更新日時           = sysdate,
                    最終更新プログラムＩＤ = :mmkozkinf_t.saishu_koshin_programid
            WHERE  顧客番号               = :mmkozkinf_t.kokyaku_no;
            */
            StringDto workSql = new StringDto();
            workSql.arr = "UPDATE MM顧客属性情報\n" +
                    "            SET    郵便番号コード         = ?,\n" +
                    "                    バッチ更新日           = ?,\n" +
                    "                    最終更新日             = ?,\n" +
                    "                    最終更新日時           = sysdate(),\n" +
                    "                    最終更新プログラムＩＤ = ?\n" +
                    "            WHERE  顧客番号               = ?";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTpocdBDto.mmkozkinf_t.yubin_no_cd, cmBTpocdBDto.h_batdate_t, cmBTpocdBDto.h_batdate_t,
                    cmBTpocdBDto.mmkozkinf_t.saishu_koshin_programid, cmBTpocdBDto.mmkozkinf_t.kokyaku_no);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*-----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpocdB_main *** MM顧客属性情報 UPDATE ERR " +
                            "sqlcode=[%d]\n", sqlca.sqlcode);
                    C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
                    /*-----------------------------------------------------------------*/
                }
                sprintf(out_format_buf, "顧客番号=%s",
                        cmBTpocdBDto.mmkozkinf_t.kokyaku_no.arr);
                APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                        "MM顧客属性情報", out_format_buf, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE POCD_MMKZ01;
                sqlca.curse_close();
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* コミット */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* MM顧客属性情報_正常処理件数をカウントアップ */
            ok_data_cnt++;

        }

        /* カーソルクローズ */
        // EXEC SQL CLOSE POCD_MMKZ01;
        sqlca.curse_close();

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** cmBTpocdB_main *** 郵便番号コード設定処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgdInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgdInf( char *Arg_in )                                   */
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
    public int Chk_ArgdInf(StringDto Arg_in) {
        int     rtn_cd;                         /* 関数戻り値                         */
        int     loop_cnt;                       /* ループカウンタ                     */
        StringDto    wk_ymd = new StringDto(9);                      /* 処理日付変換用                     */
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("Chk_ArgdInf処理");
            C_DbgMsg("*** Chk_ArgdInf *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        if ( 0 == memcmp( Arg_in, DEF_ARG_D, 2 ) ) {        /* -d処理日付チェック     */
            if( arg_d_chk != DEF_OFF ) {
                sprintf(out_format_buf, "-d 引数が重複しています（%s）", Arg_in);
                return (C_const_NG);
            }
            arg_d_chk = DEF_ON;

            if ( strlen(Arg_in) != (2+8) ) {           /* 日付桁数チェック        */
                sprintf( out_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                return (C_const_NG);
            }

            memset( wk_ymd, 0x00, sizeof(wk_ymd) );    /* 数字チェック            */
            strncpy(wk_ymd , Arg_in.substring(2), 8);
            for ( loop_cnt=0; loop_cnt<8; loop_cnt++ ) {
                rtn_cd = isdigit(wk_ymd.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf( out_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
                    return (C_const_NG);
                }
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("Chk_ArgdInf処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        strcpy(arg_d_Value, Arg_in.substring(2));
        return (C_const_OK);
    }
}
