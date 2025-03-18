package jp.co.mcc.nttdata.batch.business.service.cmBTatlvB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTatlvB.dto.CmBTatlvBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 顧客自動退会処理（cmBTatlvB）
 *
 *   【処理概要】
 *   61カ月買上実績の無い顧客（退会者）を抽出する。
 *   結果を退会顧客データファイルに出力する。
 *
 *   全チャンネル無効状態の会員のうち、顧客依頼退会日から31日経過した顧客情報を
 *   退会顧客データファイルに出力する
 *
 *   【引数説明】
 *     -o退会顧客データファイル名      :（必須）出力ファイル名（$CM_APWORK_DATE）
 *     -s顧客ステータス退会ファイル名  :（必須）出力ファイル名（$CM_APWORK_DATE）
 *     -DEBUG(-debug)                  :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *     10 　　 ：　正常
 *     99 　　 ：　異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/11/10 SSI.越後谷   ： 初版
 *           :  2014/07/22 SSI.吉田     ： 管理PC、EC連動での退会顧客の自動退会を
 *                                         行わない為、条件抽出SQLを削除
 *      2.00：  2017/12/04 SSI.吉田(信) ： 自動退会対象仕様変更対応
 *     30.00 :  2021/01/29 NDBS. 緒方   :   期間限定Ｐ追加によるリコンパイル
 *     40.00 :  2022/10/06 SSI.申       :   MCCM初版
 *
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTatlvBServiceImpl extends CmABfuncLServiceImpl implements CmBTatlvBService{

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTatlvBDto cmBTatlvBDto = new CmBTatlvBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_O = "-o";                /* 出力ファイル名                     */
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    /* 2022/10/07 MCCM初版 ADD START */
    String DEF_ARG_S = "-s";                /* 出力ファイル名                     */
    /* 2022/10/07 MCCM初版 ADD END */
    /*---------------------------------------------*/
    String C_PRGNAME = "顧客自動退会";      /* APログ用機能名                     */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int taisyo_cnt_o;             /** 処理対象件数 退会処理（静態クリア） **/          /* 2022/12/26 MCCM初版 MOD */
    int ok_cnt_o;                 /** 正常処理件数 退会処理（静態クリア） **/          /* 2022/12/26 MCCM初版 MOD */
    int taisyo_cnt_s;             /** 処理対象件数 退会処理（顧客ステータス更新） **/  /* 2022/12/26 MCCM初版 ADD */
    int ok_cnt_s;                 /** 正常処理件数 退会処理（顧客ステータス更新） **/  /* 2022/12/26 MCCM初版 ADD */
    int chk_arg_o;                /** 引数-oチェック用              **/
    int chk_arg_s;                /** 引数-sチェック用              **//* 2022/10/07 MCCM初版 MOD */
    StringDto arg_o_Value = new StringDto(256);         /** 引数o設定値                   **/
    StringDto out_file_dir = new StringDto(4096);       /** 出力ファイルディレクトリ      **/
    StringDto out_fl_name = new StringDto(4096);        /** 出力ファイル・パス名          **/
    StringDto gl_bat_date = new StringDto(9);       /** バッチ処理日付                       **/
    int gl_out_cnt;           /** 出力ファイルデータ件数               **/
    /* 2022/10/07 MCCM初版 ADD START */
    StringDto arg_s_Value = new StringDto(256);         /** 引数s設定値                   **/
    /* 2022/10/07 MCCM初版 ADD END */

    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット */

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* 退会顧客データファイル構造体                                               */
    /*----------------------------------------------------------------------------*/
    FileStatusDto[] fp_out = new FileStatusDto[2];                       /* 出力ファイル用ポインタ             *//* 2022/10/07 MCCM初版 MOD */

    private class TaikaiKokyakuData {
        StringDto kokyaku_no = new StringDto(15+1);          /* 顧客番号                        */
        StringDto lf = new StringDto(1);                     /* 改行                            */
    }
    TaikaiKokyakuData out_kekka_t = new TaikaiKokyakuData();

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
        int rtn_cd;                     /* 関数戻り値                             */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数結果ステータス                     */
        int arg_cnt;                    /* 引数チェック用カウンタ                 */
        String env_outdir;                  /* 出力ファイルDIR                    */
        StringDto arg_Work1 = new StringDto(256);               /* Work Buffer1                         */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        taisyo_cnt_o = 0;                   /* 処理対象件数 退会処理（静態クリア）*/          /* 2022/12/26 MCCM初版 MOD */
        ok_cnt_o     = 0;                   /* 正常処理件数 退会処理（静態クリア）*/          /* 2022/12/26 MCCM初版 MOD */
        taisyo_cnt_s = 0;                   /* 処理対象件数 退会処理（顧客ステータス更新）*/  /* 2022/12/26 MCCM初版 ADD */
        ok_cnt_s     = 0;                   /* 正常処理件数 退会処理（顧客ステータス更新）*/  /* 2022/12/26 MCCM初版 ADD */
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        rtn_cd = C_GetPgname( argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        memset(cmBTatlvBDto.Program_Name, 0x00, sizeof(cmBTatlvBDto.Program_Name));
        strcpy(cmBTatlvBDto.Program_Name,     Cg_Program_Name);     /* バージョンなしプログラム名 */
        strcpy(cmBTatlvBDto.Program_Name_Ver, Cg_Program_Name_Ver); /* バージョンなしプログラム名 */
        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg( argc, argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            /*------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for ( arg_cnt = 1; arg_cnt < argc; arg_cnt++ ) {
            memset( arg_Work1, 0x00, sizeof(arg_Work1) );
            strcpy( arg_Work1, argv[arg_cnt] );

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }
            if ( 0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug) ) {
                continue;

            } else if( 0 == memcmp( arg_Work1, DEF_ARG_O, 2 ) ) { /* -oの場合         */
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** main *** -oチェック！！ %s \n", arg_Work1);
                    /*------------------------------------------------------------*/
                }
                rtn_cd = cmBTatlvB_Chk_Arg( arg_Work1 );    /* パラメータチェック */
                if ( rtn_cd == C_const_OK ) {
                    strcpy(arg_o_Value, arg_Work1.arr.substring(2));
                } else {
                    sprintf( chg_format_buf, "-o 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit( C_const_APNG );
                }
            }else if( 0 == memcmp( arg_Work1, DEF_ARG_S, 2 ) ) { /* -sの場合         */      /* 2022/10/07 MCCM初版 ADD START */
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** main *** -sチェック！！ %s \n", arg_Work1);
                    /*------------------------------------------------------------*/
                }
                rtn_cd = cmBTatlvB_Chk_Arg( arg_Work1 );    /* パラメータチェック */
                if ( rtn_cd == C_const_OK ) {
                    strcpy(arg_s_Value, arg_Work1.arr.substring(2));
                } else {
                    sprintf( chg_format_buf, "-s 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit( C_const_APNG );
                }
            } else {                        /* 定義外パラメータ                   */       /* 2022/10/07 MCCM初版 ADD END */
                sprintf( chg_format_buf, "定義外の引数（%s）", arg_Work1 );
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit( C_const_APNG );
            }
        }

        /* 必須パラメータ未指定チェック */
        if ( chk_arg_o == DEF_OFF ) {
            sprintf( chg_format_buf, "-o 引数の値が不正です" );
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit( C_const_APNG );
        }
        /* 2022/10/07 MCCM初版 ADD START */
        if ( chk_arg_s == DEF_OFF ) {
            sprintf( chg_format_buf, "-s 引数の値が不正です" );
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit( C_const_APNG );
        }
        /* 2022/10/07 MCCM初版 ADD END */
        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "CM_APWORK_DATE");
            /*-------------------------------------------------------------*/
        }
        env_outdir = getenv("CM_APWORK_DATE");
        if ( StringUtils.isEmpty(env_outdir) ) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_outdir);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
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
        rtn_cd = C_OraDBConnect( BT_aplcomService.C_ORACONN_SD, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn=[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*------------------------------------------*/
        /*  バッチ処理日取得処理                    */
        /*------------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日（前日）取得 %s\n", "START");
            /*------------------------------------------------------------*/
        }
        rtn_cd = C_GetBatDate( -1, gl_bat_date, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日（前日）取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日（前日）取得OK [%s]\n", gl_bat_date);
            /*------------------------------------------------------------*/
        }
        /*--------------------------------------*/
        /*  ファイルオープン（顧客自動退会）    */
        /*--------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** ファイルオープン(%s)\n", "START");
            /*------------------------------------------------------------*/
        }
        rtn_cd = cmBTatlvB_OpenFile();
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** ファイルオープンNG rtn=[%d]\n", rtn_cd);
                /*---------------------------------------------*/
            }
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }


        /*-----------------------------------------------*/
        /*  主処理(顧客自動退会）                        */
        /*-----------------------------------------------*/
        rtn_cd = cmBTatlvB_main();
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTatlvB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "顧客自動退会に失敗しました", 0, 0, 0, 0, 0);
            /* 各テーブル更新処理件数を出力 */
            APLOG_WT("107", 0, null, "出力ファイルデータ件数（顧客自動退会） ", gl_out_cnt,0,0,0,0);
            rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 出力ファイルデータ件数を出力 */
        APLOG_WT("106", 0, null, "TS利用可能ポイント情報（静態クリア等）", taisyo_cnt_o, ok_cnt_o,0,0,0);       /* 2022/12/26 MCCM初版 MOD */
        APLOG_WT("106", 0, null, "TS利用可能ポイント情報（顧客ステータス更新）", taisyo_cnt_s, ok_cnt_s,0,0,0); /* 2022/12/26 MCCM初版 ADD */

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */
        /* 警告: ‘sqlstm’ defined but not used : とりあえず参照だけする対処 */
        // arg_cnt = sqlstm.arrsiz; /* ワーニングを出ないようにするおまじない arg_cntは意味なし */

        return exit( C_const_APOK );
        /*-----main Bottom------------------------------------------------------------*/
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTatlvB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  cmBTatlvB_main()                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ４．顧客自動退会抽出処理                                               */
    /*     ６1カ月買上実績の無い顧客を抽出する。（自動退会）。                    */
    /*     異常時は、APログを出力し、戻り値を異常（1）として退出する。            */
    /*     買上実績の全くない顧客も、61カ月経過していたら、                       */
    /*     抽出対象とする（自動退会）。                                           */
    /*     （正常終了時　：　戻り値=0）                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTatlvB_main() {
        int rtn_cd;                          /* 関数戻り値                      */
        IntegerDto rtn_status = new IntegerDto();                      /* 関数結果ステータス              */

        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);       /* 動的SQLバッファ                 */

        /* 2022/10/06 MCCM初版 MOD START */
        StringDto taikai_yyyymmdd3 = new StringDto(9);             /* 退会対象年月日                  */
        StringDto taikai_yyyymm3 = new StringDto(7);               /* 退会対象年月                    */
        StringDto taikai_yyyy3 = new StringDto(5);                 /* 退会対象年                      */
        StringDto taikai_ymd3 = new StringDto();                  /* 退会対象年月日(参照用）         */
        /* 2022/10/06 MCCM初版 MOD END */

        StringDto taikai_yyyymmdd2 = new StringDto(9);             /* 退会対象年月日 ※保存期間１カ月 */
        StringDto taikai_yyyymm2 = new StringDto(7);               /* 退会対象年月             〃     */
        StringDto taikai_yyyy2 = new StringDto(5);                 /* 退会対象年               〃     */
        StringDto taikai_ymd2 = new StringDto(9);                  /* 退会対象年月日(参照用）  〃     */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTatlvB_main処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        gl_out_cnt = 0;    /* 出力ファイルデータ件数 */

        /* 2022/10/06 MCCM初版 MOD START */
        /*--------------------------------------------*/
        /* 保存期間取得処理 (退会対象年月日取得)      */
        /*--------------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得３ %s\n", "START");
            /*------------------------------------------------------------*/
        }

        memset(taikai_yyyymmdd3,0x00,sizeof(taikai_yyyymmdd3));
        memset(taikai_yyyymm3,0x00,sizeof(taikai_yyyymm3));
        memset(taikai_yyyy3,0x00,sizeof(taikai_yyyy3));
        memset(taikai_ymd3,0x00,sizeof(taikai_ymd3));

        /* 保存期間取得処理 */
        rtn_cd = C_GetSaveKkn("顧客退会３", gl_bat_date.arr,
                taikai_yyyymmdd3 ,
                taikai_yyyymm3 ,
                taikai_yyyy3,
                taikai_ymd3, rtn_status);

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得３NG rtn =[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetSaveKkn", rtn_cd,
                    rtn_status, 0,0,0);
            return (C_const_NG);
        }

        /* HOST変数に設定 */
        cmBTatlvBDto.gh_kijun_date3.arr = atoi(taikai_yyyymmdd3);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得３OK rtn =[%d]\n",
                    cmBTatlvBDto.gh_kijun_date3);
            /*------------------------------------------------------------*/
        }
        /* 2022/10/06 MCCM初版 MOD END */

        /* 2017.12.04 自動退会対象仕様変更対応 Sta */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得２ %s\n", "START");
            /*------------------------------------------------------------*/
        }


        memset(taikai_yyyymmdd2,0x00,sizeof(taikai_yyyymmdd2));
        memset(taikai_yyyymm2,0x00,sizeof(taikai_yyyymm2));
        memset(taikai_yyyy2,0x00,sizeof(taikai_yyyy2));
        memset(taikai_ymd2,0x00,sizeof(taikai_ymd2));

        /* 保存期間取得処理 */
        rtn_cd = C_GetSaveKkn("顧客退会２", gl_bat_date.arr,
                taikai_yyyymmdd2,
                taikai_yyyymm2,
                taikai_yyyy2,
                taikai_ymd2, rtn_status);

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得２NG rtn =[%d]\n",
                        rtn_cd);
            }
            APLOG_WT("903", 0, null, "C_GetSaveKkn", rtn_cd,
                    rtn_status, 0,0,0);
            return (C_const_NG);
        }

        /* HOST変数に設定 */
        cmBTatlvBDto.gh_kijun_date2.arr = atoi(taikai_yyyymmdd2);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTatlvB_main *** 退会対象年月日取得２OK rtn =[%d]\n",
                    cmBTatlvBDto.gh_kijun_date2);
            /*------------------------------------------------------------*/
        }

        /* 2017.12.04 自動退会対象仕様変更対応 End */

        /*-------------------------------------*/
        /* 顧客自動退会処理                    */
        /*-------------------------------------*/
        memset(wk_sql, 0x00, sizeof(wk_sql));
        /*-------------------------------------*/
        /* 自動退会顧客情報取得                */
        /*-------------------------------------*/
        /* 2022/10/07 MCCM初版 DEL START */
//    sprintf(wk_sql,
//                "SELECT 顧客番号 "
//                "FROM  TS利用可能ポイント情報  "
//                "WHERE ( 最終買上日 = %d )     "
//                "  OR  ( 最終買上日 =  0  AND  "
//                "        最終更新日 = %d )   "
//                "union "
//                    "select 顧客番号 "
//                    "from ( "
//                    "select "
//                    "A.顧客番号, "
//                    "A.企業コード, "
//                    "CASE WHEN A.企業コード = 1020 THEN A.退会年月日 WHEN A.企業コード = 1040 THEN A.退会年月日 ELSE A.仮退会年月日 END AS 退会日, "
//                    "ROW_NUMBER() OVER ( "
//                    " PARTITION BY A.顧客番号 "
//                    " ORDER BY CASE WHEN A.企業コード = 1020 THEN A.退会年月日 WHEN A.企業コード = 1040 THEN A.退会年月日 ELSE A.仮退会年月日 END DESC, 企業コード) AS G_ROW "
//                    "from MM顧客企業別属性情報@CMMD A "
//                    ") B "
//                    "where B.G_ROW = 1 "
//                    "and B.退会日 = %d "
//                    "and not exists (select 1 from MSカード情報 C where C.顧客番号 = B.顧客番号 and C.カードステータス in (0,7)) "
//    , gh_kijun_date, gh_kijun_date,gh_kijun_date2);
        /* 2022/10/07 MCCM初版 DEL END */

        /* 2022/10/07 MCCM初版 ADD START */
        sprintf(wk_sql, "SELECT 顧客番号, " +
                        "       0 AS FLG  " +
                        "FROM ( " +
                        "SELECT " +
                        "A.顧客番号, " +
                        "A.企業コード, " +
                        "A.退会年月日 AS 退会日, " +
                        "ROW_NUMBER() OVER ( " +
                        " PARTITION BY A.顧客番号 " +
                        " ORDER BY A.退会年月日 ) AS G_ROW " +
                        "FROM MM顧客企業別属性情報 A " +
                        ") B " +
                        "WHERE B.G_ROW = 1 " +
                        "AND B.退会日 = %d " +
                        "AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE C.顧客番号 = B.顧客番号 AND C.カードステータス IN (0,1)) " +
                        "UNION " +
                        "SELECT 顧客番号, " +
                        "       1 AS FLG  " +
                        "FROM ( " +
                        "SELECT " +
                        "A.顧客番号, " +
                        "A.企業コード, " +
                        "A.退会年月日 AS 退会日, " +
                        "ROW_NUMBER() OVER ( " +
                        " PARTITION BY A.顧客番号 " +
                        " ORDER BY A.退会年月日 ) AS G_ROW " +
                        "FROM MM顧客企業別属性情報 A " +
                        ") B " +
                        "WHERE B.G_ROW = 1 " +
                        "AND B.退会日 = %d " +
                        "AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE C.顧客番号 = B.顧客番号 AND C.カードステータス IN (0,1)) "
                , cmBTatlvBDto.gh_kijun_date2, cmBTatlvBDto.gh_kijun_date3);

        /* 2022/10/07 MCCM初版 ADD END */

        /* HOST変数に設定 */
        memset(cmBTatlvBDto.str_sql, 0x00, sizeof(cmBTatlvBDto.str_sql));
        strcpy(cmBTatlvBDto.str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
        SqlstmDto sqlca=sqlcaManager.get("KYUM_TSRP01");
        // EXEC SQL PREPARE sql_stat1 from :str_sql;
        //SqlstmDto sql_stat1 = sqlcaManager.get("sql_stat1");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_main*** 動的ＳＱＬ 解析NG = %d\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT( "902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
        // EXEC SQL DECLARE KYUM_TSRP01 cursor for sql_stat1;
        sqlca.declare();


        /* カーソルオープン */
        // EXEC SQL OPEN KYUM_TSRP01;
        sqlca.open();

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TS利用可能ポイント情報", chg_format_buf, 0, 0);
            return C_const_NG;
        }

        /* 対象データ数分繰り返す */
        while( true ) {
            memset( cmBTatlvBDto.tsryokp_t.kokyaku_no, 0x00, sizeof(cmBTatlvBDto.tsryokp_t.kokyaku_no) );
            cmBTatlvBDto.tsryokp_t.kokyaku_no.len = 0;
            cmBTatlvBDto.gh_flg.arr = 0;                                                                         /* 2022/10/07 MCCM初版 ADD */

            /* カーソルフェッチ */
            /*
            EXEC SQL FETCH KYUM_TSRP01
            INTO :tsryokp_t.kokyaku_no,
             :gh_flg;                                                                       *//* 2022/10/07 MCCM初版 ADD *//*
            */
            sqlca.fetch();
            sqlca.recData(cmBTatlvBDto.tsryokp_t.kokyaku_no, cmBTatlvBDto.gh_flg);

            /* データ無し以外エラーの場合処理を異常終了する */
            if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "TS利用可能ポイント情報", chg_format_buf, 0, 0);
                // EXEC SQL CLOSE KYUM_TSRP01; /* カーソルクローズ                   */
//                sqlca.close();
                sqlcaManager.close("KYUM_TSRP01");
                return C_const_NG;
            }

            /* データ無し */
            if ( sqlca.sqlcode == C_const_Ora_NOTFOUND ) {
                break;
            }
            /* 退会処理（静態クリア）*/
            if(cmBTatlvBDto.gh_flg.intVal() == 0){                                                                    /* 2022/12/26 MCCM初版 MOD */
                taisyo_cnt_o++;                   /* 処理対象件数カウントアップ         */        /* 2022/12/26 MCCM初版 MOD */
            }                                                                                 /* 2022/12/26 MCCM初版 MOD */

            /* 退会処理（顧客ステータス更新）*/
            if(cmBTatlvBDto.gh_flg.intVal() == 1){                                                                    /* 2022/12/26 MCCM初版 ADD */
                taisyo_cnt_s++;                   /* 処理対象件数カウントアップ         */        /* 2022/12/26 MCCM初版 ADD */
            }                                                                                 /* 2022/12/26 MCCM初版 ADD */

            /*-------------------------------------------------------------*/
            /* 退会顧客データファイル出力・顧客ステータス退会ファイル      */
            /*-------------------------------------------------------------*/
            /* 検索結果の処理結果ファイルへの設定 */
            strncpy( out_kekka_t.kokyaku_no,                /* 顧客番号 */
                    cmBTatlvBDto.tsryokp_t.kokyaku_no.arr(), sizeof(out_kekka_t.kokyaku_no)-1 );
            /* 退会顧客データファイル出力処理      */
            rtn_cd = cmBTatlvB_WriteFile(cmBTatlvBDto.gh_flg.intVal());   /* ★処理結果ファイル書込み */              /* 2022/10/07 MCCM初版 MOD */
            if ( rtn_cd != C_const_OK ) {
                return C_const_NG;
            }
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_main ***退会顧客データ（顧客番号）=[%s]\n", cmBTatlvBDto.tsryokp_t.kokyaku_no.arr);
                /*------------------------------------------------------------*/
            }

        }

        // EXEC SQL CLOSE KYUM_TSRP01;         /* カーソルクローズ                   */
//        sqlca.close();
        sqlcaManager.close("KYUM_TSRP01");

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTatlvB_main *** 出力ファイルデータ件数（顧客自動退会）=[%d]\n", gl_out_cnt);
            /*------------------------------------------------------------*/
        }

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTatlvB_main Bottom--------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTatlvB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTatlvB_Chk_Arg( char *Arg_in )                             */
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
    public int cmBTatlvB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTatlvB_Chk_Arg処理");
            C_DbgMsg("*** cmBTatlvB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );

        if( 0 == memcmp( Arg_in, DEF_ARG_O, 2 ) ) { /* -o出力ファイルチェック  */
            if( chk_arg_o != DEF_OFF ) {
                return C_const_NG;
            }
            chk_arg_o = DEF_ON;

            if ( strlen(Arg_in) <= 2 ) {               /* 桁数チェック            */
                return C_const_NG;
            }
        }
        /* 2022/10/07 MCCM初版 ADD START */
        if( 0 == memcmp( Arg_in, DEF_ARG_S, 2 ) ) { /* -S出力ファイルチェック  */
            if( chk_arg_s != DEF_OFF ) {
                return C_const_NG;
            }
            chk_arg_s = DEF_ON;

            if ( strlen(Arg_in) <= 2 ) {               /* 桁数チェック            */
                return C_const_NG;
            }
        }
        /* 2022/10/07 MCCM初版 ADD END */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTatlvB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----cmBTatlvB_Chk_Arg Bottom--------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTatlvB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTatlvB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              出力ファイルをオープンする                                    */
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
    public int cmBTatlvB_OpenFile() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTatlvB_OpenFile処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        // memset (out_fl_name, 0x00, sizeof(out_fl_name) );              /* 2022/10/07 MCCM初版 DEL */
        /* 2022/10/07 MCCM初版 ADD START */
        StringDto[] arg_value = new StringDto[]{arg_o_Value, arg_s_Value};
        int  i = 0;
        /* 2022/10/07 MCCM初版 ADD END */

        /* 出力ファイルのオープン */
        for(i=0;i<2;i++){                                             /* 2022/10/07 MCCM初版 MOD */
            memset (out_fl_name, 0x00, sizeof(out_fl_name) );             /* 2022/10/07 MCCM初版 MOD */
            sprintf( out_fl_name, "%s/%s", out_file_dir, arg_value[i]);   /* 2022/10/07 MCCM初版 MOD */

            if (( fp_out[i] = open(out_fl_name.arr)).fd < 0) {
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTatlvB_OpenFile *** 出力ファイルオープンNG[%s]\n", out_fl_name);
                    /*------------------------------------------------------------*/
                }
                memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                sprintf( chg_format_buf, "fopen（%s）", out_fl_name );
                APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_OpenFile *** 出力ファイル[%s]\n", out_fl_name);
                C_DbgEnd("cmBTatlvB_OpenFile処理", 0, 0, 0);
                /*------------------------------------------------------------*/
            }

        }

        return C_const_OK;

        /*-----cmBTatlvB_OpenFile Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTatlvB_WriteFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTatlvB_WriteFile(int i)                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              編集バッファを退会顧客データファイルに書込む                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*          i(出力ファイル区分)                                               */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTatlvB_WriteFile(int i) {
        int rtn_cd;                   /* 関数戻り値                  */
        StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット */

        StringDto outbuf = new StringDto(2000); /* 出力する行のバッファ */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTatlvB_WriteFile処理");
            /*---------------------------------------------------------------------*/
        }

        /*** 行編集 ***/
        strcpy(outbuf, "");

        /* 顧客番号         */
        strcat(outbuf, out_kekka_t.kokyaku_no);
        strcat(outbuf, "\n");

        /* 処理結果ファイル書込み */
        // rtn_cd = fputs(outbuf, fp_out[i]);
        // if (rtn_cd == EOF) {
        rtn_cd = fp_out[i].write(outbuf.arr);
        if (rtn_cd < 0) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTatlvB_WriteFile *** fputs NG rtn=[%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
            sprintf( chg_format_buf, "fputs（%s）", out_fl_name );
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        gl_out_cnt++;           /* 出力ファイルデータ件数カウントアップ         */

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgEnd("cmBTatlvB_WriteFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }
        /* 退会処理（静態クリア）*/
        if(i==0){                                                                           /* 2022/12/26 MCCM初版 MOD */
            ok_cnt_o++;                         /* 正常処理件数カウントアップ         */      /* 2022/12/26 MCCM初版 MOD */
        }                                                                                   /* 2022/12/26 MCCM初版 MOD */

        /* 退会処理（顧客ステータス更新）*/
        if(i==1){                                                                           /* 2022/12/26 MCCM初版 ADD */
            ok_cnt_s++;                         /* 正常処理件数カウントアップ         */      /* 2022/12/26 MCCM初版 ADD */
        }                                                                                   /* 2022/12/26 MCCM初版 ADD */

        return C_const_OK;

        /*-----cmBTatlvB_WriteFile Bottom----------------------------------------------*/
    }
}
