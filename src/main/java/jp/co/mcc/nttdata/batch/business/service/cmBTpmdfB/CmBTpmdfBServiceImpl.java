package jp.co.mcc.nttdata.batch.business.service.cmBTpmdfB;

import io.micrometer.core.instrument.util.StringUtils;
import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTfuncB.CmBTfuncBImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTpmdfB.dto.CmBTpmdfBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_KAZOKU_SEDO_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_RANKBETSU_POINT_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RANK_INFO_TBL;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： ポイント修正処理（cmBTpmdfB）
 *
 *   【処理概要】
 *       ポイント修正データより取得した顧客に対して、ポイントの付与を行う。
 *       HSポイント日別情報、TSポイント年別情報、TSポイント累計
 *       情報、TS利用可能ポイント情報の更新を行う。
 *
 *   【引数説明】
 *  -i有効期限ポイント修正ファイル名 :　処理対象のポイント修正情報ファイル名
 *  -DEBUG(-debug)                   :　デバッグモードでの実行
 *                                     （トレース出力機能が有効）
 *
 *   【戻り値】
 *       10　　 ：　正常
 *       99　　 ：　異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/12/19 SSI.吉岡 ： 初版
 *      2.00 :  2013/07/12 SSI.本田 ： データ年月日をINPUTファイル項目に追加
 *      3.00 :  2013/07/23 SSI.上野 ： TS利用可能ポイント情報.最終買上日更新追加
 *      4.00 :  2016/09/12 SSI.田   ： ポイント一括付与自動化対応
 *                                     →TSポイント修正テープルに参照しない
 *                                     →修正結果ファイルを追加出力
 *     30.00 :  2021/01/21 NDBS.緒方 : 年度管理変更/期間限定ポイント追加対応
 *              2021/02/17 NDBS.亀谷 : TS期間限定ポイント情報を追加
 *     31.00 :  2021/12/09 SSI.上野  : 共通関数修正によりリコンパイル
 *                                     (C_InsertDayPoint,C_UpdateKikanPoint)
 *     40.00 :  2022/09/22 SSI.申    : MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTpmdfBServiceImpl extends CmABfuncLServiceImpl implements CmBTpmdfBService {

    @Autowired
    CmBTfuncBImpl cmBTfuncBImpl;

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */
    private boolean DBG_LOG = true;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTpmdfBDto cmBTpmdfBDto = new CmBTpmdfBDto();

    /*----------------------------------------------------------------------------*/
    /*  入出力ファイル                                                            */
    /*----------------------------------------------------------------------------*/
    /*** 入力ファイル ***/
    FileStatusDto fp_in = new FileStatusDto();             /* 入力ファイル用ポインタ      */

    /*** 出力ファイル ***/
    FileStatusDto fp_out = new FileStatusDto();            /* 出力ファイル用ポインタ      */
    String OUT_FILE_NAME = "limitedPoint_result"; /* 出力ファイル           */


    StringDto ap_work_dir = new StringDto();                   /** ワークディレクトリ               **/
    StringDto out_file_dir = new StringDto(4096);             /** 出力ファイルディレクトリ         **/

    IntegerDto msks_up_rec_cnt = new IntegerDto();                /** MS顧客制度情報更新レコード件数 **/
    IntegerDto mskz_up_rec_cnt = new IntegerDto();                /** MS家族制度情報更新レコード件数 **/
    int lock_chk;



    /** 顧客ロックデータ有無フラグ    **/

    /* 入力ファイル構造体 */
    private class POINT_SHUSEI_DATA {
        int irai_kigyo;              /* 依頼企業コード             */
        int irai_tenpo;              /* 依頼店舗                   */
        int shusei_taisho_nen;       /* 修正対象年                 */
        int shusei_taisho_tsuki;     /* 修正対象月                 */
        int data_ymd;                /* データ年月日               */
        int kigyo_cd;                /* 企業コード                 */
        StringDto pid = new StringDto(18);                 /* 会員番号                   */
        long rankup_taisho_kingaku_kagenti; /* ランクＵＰ対象金額加減値   */
        /*int    riyo_kano_point_kagenti; *//* 利用可能ポイント加減値     */
        int tujo_point_kijun_nendo;       /* 通常Ｐ基準年度       */
        long tujo_point_kagenti_tonen;     /* 通常Ｐ加減値（当年） */
        long tujo_point_kagenti_zennen;    /* 通常Ｐ加減値（前年） */
        int kikan_point_kijun_month;      /* 期間限定Ｐ基準月     */
        long kikan_point_kagenti_month0;   /* 期間限定Ｐ加減値（当月）*/
        long kikan_point_kagenti_month1;   /* 期間限定Ｐ加減値（1ヶ月後）*/
        long kikan_point_kagenti_month2;   /* 期間限定Ｐ加減値（2ヶ月後）*/
        long kikan_point_kagenti_month3;   /* 期間限定Ｐ加減値（3ヶ月後）*/
        int riyu_code;                        /* 理由コード                 */
        int uard_flg;                     /* 売上連動フラグ             */
        StringDto renban = new StringDto(10+1);                 /* 連番                       */
    }
    POINT_SHUSEI_DATA   in_point_shusei = new POINT_SHUSEI_DATA();

    StringDto get_point_shusei = new StringDto(488);

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;       /* OFF                         */
    int DEF_ON  = 1;       /* ON                          */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_I = "-i";            /* ポイント修正データファイル名 */
    String DEF_ARG_K = "-k";            /* 処理種別                     */
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ             */
    String DEF_debug = "-debug";        /* デバッグスイッチ             */
    /*---------------------------------------------*/
    int DEF_Read_EOF   =  9;           /* File read EOF                */
    int LOG_OUTPUT_CNT   =   5000;    /* ログ出力件数                 */
    String PG_NAME    = "ポイント修正";  /* プログラム名称               */
    String FILE_HEAD  = "依頼企業コード";/* 入力ファイルレコードヘッダ   */

    int DEF_TBLKBN_SEDO       =  0;   /* テーブル区分：顧客制度情報       */
    int DEF_TBLKBN_KAZOKU     =  1;   /* テーブル区分：家族制度情報       */
    int DEF_COLKBN_RANKUPGAKU =  0;   /* カラム区分：ランクＵＰ対象金額   */
    int DEF_COLKBN_RANKCD     =  1;   /* カラム区分：ランクコード         */
    int DEF_COLKBN_RANKYMD    =  2;   /* カラム区分：ランク変更日付       */

    /* 2023/01/09 MCCM初版 MOD START */
    //#define DEF_RANK_MONTH          1   /* 月次ランク                   */
    //#define DEF_RANK_YEAR           2   /* 年次ランク                   */
    int DEF_RANK_MONTH    =    2;   /* 月次ランク                   */
    int DEF_RANK_YEAR     =    1;   /* 年次ランク                   */
    /* 2023/01/09 MCCM初版 MOD END */

    /*-----  ポイント付与結果コード----------*/
    int TUJO_POINT_KIJUN_NEDO_HANIGAI     =  23;  /* 通常Ｐ基準年度範囲外     */
    int GENTEI_POINT_KIJUN_MONTH_HANIGAI  =  24;  /* 期間限定Ｐ基準月範囲外   */
    int GENTEI_POINT_KAGENTI_FUSEI        =  25;  /* 期間限定Ｐ加減値不正     */
    /* 2022/10/04 MCCM初版 ADD START */
    String KOBAI     = "購買";                        /* 購買                     */
    String HIKOBAI   = "非購買";                      /* 非購買                   */
    String SONOHOKA  = "その他";                      /* その他                   */
    /* 2022/10/04 MCCM初版 ADD END */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int arg_i_chk;          /** 引数iチェック用           **/

    StringDto arg_i_Value = new StringDto(256);   /** 引数i設定値               **/
    /*---------------------------------------------*/
    StringDto bat_date = new StringDto(9);            /** バッチ処理日付(当日)            **/
    StringDto bat_yyyymm = new StringDto(7);          /*  バッチ処理日付（年月）           */
    StringDto bat_yyyy = new StringDto(5);            /*  バッチ処理日付（年）             */
    StringDto bat_mm = new StringDto(3);              /*  バッチ処理日付（月）             */
    StringDto g_sys_date = new StringDto(9);          /*  システム日付                     */
    StringDto sys_time = new StringDto(7);            /** 現在時刻                        **/

    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);      /** APログフォーマット       **/
    StringDto arg_ks_Value = new StringDto(9);          /** 特別処理期間開始日        **/
    StringDto arg_ke_Value = new StringDto(9);          /** 特別処理期間終了日        **/
    int is_toku_syo_kikan;        /* 特別処理期間なら１ */
    int this_date;                /* バッチ日付                         */
    int this_time;                /* 現在時刻                           */
    StringDto fl_o_name = new StringDto(4096);                   /* 出力ファイル名                     */
    /* 2023/01/08 MCCM初版 ADD START */
    long nenkan_rankup_taisho_kingaku_old;  /* 更新前年間ランクＵＰ対象金額       */
    long gekkan_rankup_taisho_kingaku_old;  /* 更新前月間ランクＵＰ対象金額       */
    int    shuse_taisho_nen_moto;  /* 元修正対象年                       */
    /* 2023/01/08 MCCM初版 ADD END */

    MS_RANKBETSU_POINT_INFO_TBL[] MsrkData = new MS_RANKBETSU_POINT_INFO_TBL[100]; /* MSランク情報                 */

    /*-----  TS利用可能ポイント情報検索用----------*/
    private class TS_SERCH{
        IntegerDto next_year = new IntegerDto();      /* 翌年度                              */
        IntegerDto this_year = new IntegerDto();      /* 当年度                              */
        IntegerDto ex_year = new IntegerDto();        /* 当年度                              */
        StringDto next_year_cd = new StringDto(4);           /* 翌年度コード（全角）                */
        StringDto this_year_cd = new StringDto(4);           /* 当年度コード（全角）                */
        StringDto ex_year_cd = new StringDto(4);             /* 前年度コード（全角）                */
        StringDto month00 = new StringDto(7);                /*  対象月1(全角)                         */
        StringDto month01 = new StringDto(7);                /*  対象月2(全角)                         */
        StringDto month02 = new StringDto(7);                /*  対象月3(全角)                         */
        StringDto month03 = new StringDto(7);                /*  対象月4(全角)                         */
        StringDto month04 = new StringDto(7);                /*  対象月5(全角)                         */
    }
    TS_SERCH ts_ser = new TS_SERCH();

    /*-----  年度/月跨り判定用----------*/
    protected class CHECK_OVER_YM {
        int  kijun_nendo;   /* 通常Ｐ基準年度                      */
        double  tujo_point_kagenti_zennen; /* 通常Ｐ加減値（前年）                */
        double  tujo_point_kagenti_tonen;  /* 通常Ｐ加減値（当年）                */
        int  kijun_month;   /* 期間限定Ｐ基準月                    */
        double  kikan_point_kagenti_month0;/* 期間限定Ｐ加減値（当月）            */
        double  kikan_point_kagenti_month1;/* 期間限定Ｐ加減値（1ヶ月後）         */
        double  kikan_point_kagenti_month2;/* 期間限定Ｐ加減値（2ヶ月後）         */
        double  kikan_point_kagenti_month3;/* 期間限定Ｐ加減値（3ヶ月後）         */
    }


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
        IntegerDto     rtn_status = new IntegerDto();                     /** 関数ステータス                   **/
        int     arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto msg_param = new StringDto(C_const_MsgMaxLen);   /** APログ置換文字                   **/
        StringDto arg_Work1 = new StringDto(256);                 /** Work Buffer1                     **/
        IntegerDto in_rec_cnt = new IntegerDto();                     /** 入力レコード件数                 **/
        IntegerDto ok_rec_cnt = new IntegerDto();                     /** 正常データ出力件数               **/
        IntegerDto total_cnt = new IntegerDto();                      /** 総付与ポイント数                 **/
        IntegerDto ng_rec_cnt = new IntegerDto();                     /** エラーデータ出力件数             **/
        IntegerDto hsph_in_rec_cnt = new IntegerDto();                /** HSポイント日別情報追加レコード件数 **/
        IntegerDto tspn_up_rec_cnt = new IntegerDto();                /** TSポイント年別情報更新レコード件数 **/
        IntegerDto tspr_up_rec_cnt = new IntegerDto();                /** TSポイント累計情報更新レコード件数 **/
        IntegerDto tspu_up_rec_cnt = new IntegerDto();                /** TS利用可能ポイント情報更新レコード件数 **/
        StringDto env_out_wrk = new StringDto();                   /** 出力ファイルDIR                  **/
        int wk_month;            /* バッチ処理月                   */

        /* 2021/02/17 TS期間限定ポイント情報追加  Start*/
        IntegerDto tskp_up_rec_cnt = new IntegerDto();                /** TS期間限定ポイント情報更新レコード件数 **/
        /* 2021/02/17 TS期間限定ポイント情報追加  End*/

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        memset(arg_ks_Value, 0x00, sizeof(arg_ks_Value));
        memset(arg_ke_Value, 0x00, sizeof(arg_ke_Value));
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        in_rec_cnt.arr = 0;
        ok_rec_cnt.arr = 0;
        total_cnt.arr  = 0;
        ng_rec_cnt.arr = 0;
        hsph_in_rec_cnt.arr = 0;
        tspn_up_rec_cnt.arr = 0;
        tspr_up_rec_cnt.arr = 0;
        msks_up_rec_cnt.arr = 0;
        mskz_up_rec_cnt.arr = 0;
        memset(ts_ser, 0x00, sizeof(ts_ser));
        /* 2021/02/17 TS期間限定ポイント情報追加  Start*/
        tskp_up_rec_cnt.arr = 0;
        /* 2021/02/17 TS期間限定ポイント情報追加  End*/

        /*-------------------------------------*/
        /*  プログラム名取得処理呼び出し       */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname( argv );
        if( rtn_cd != C_const_OK ) {
            sprintf(msg_param, "%s ： C_GetPgname", argv[0]);
            APLOG_WT("903", 0, null, msg_param, rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        /*  開始メッセージ */
        APLOG_WT( "102", 0, null, PG_NAME , 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理呼び出し     */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg( argc, argv );
        if( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("main処理");
            /*---------------------------------------------------------------------*/
        }

        /** 変数初期化                **/
        arg_i_chk = DEF_OFF;
        if (DBG_LOG) {
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "");
        }
        /*** 引数チェック ***/
        for( arg_cnt = 1 ; arg_cnt < argc ; arg_cnt++ ) {
            memset( arg_Work1 , 0x00 , 256 ) ;
            strcpy( arg_Work1 , argv[arg_cnt] ) ;

            if (DBG_LOG) {
                C_DbgMsg("*** main *** チェック対象パラメータ = %s\n", arg_Work1);
            }
            if( 0 == strcmp( arg_Work1 , DEF_DEBUG )) {
                continue ;
            } else if( 0 == strcmp( arg_Work1 , DEF_debug )) {
                continue ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_I , 2 )) {
                /* ポイント修正データファイル名指定 -i */
                rtn_cd  = Chk_ArgiInf( arg_Work1 ) ;
            } else {
                /* 規定外パラメータ  */
                rtn_cd  = C_const_NG ;
                sprintf( out_format_buf, "規定外のパラメータです（%s）", arg_Work1);
            }
            if (DBG_LOG) {
                C_DbgMsg("*** main *** チェック結果 = %d\n", rtn_cd);
            }

            if( rtn_cd != C_const_OK ) {

                if (DBG_LOG) {
                    C_DbgMsg("*** main *** チェックNG = %d\n", rtn_cd);
                }
                APLOG_WT( "910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                return exit( C_const_APNG ) ;
            }
        }

        /* 必須パラメータ未指定の場合エラー出力 */
        if ( arg_i_chk == DEF_OFF)
        {
            sprintf( out_format_buf,
                    "処理対象のポイント修正データファイル名が誤っています　（-i）" );
            APLOG_WT( "910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", "");
            /*-------------------------------------------------------------*/
        }
        ap_work_dir.arr = getenv("CM_FILENOWRCV");
        if (StringUtils.isEmpty(ap_work_dir.arr)){
            if (DBG_LOG) {
                C_DbgMsg("*** main *** 環境変数取得NG [CM_FILENOWRCV]%s\n", "");
            }
            APLOG_WT( "903", 0, null, "getenv(CM_FILENOWRCV)", 0, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", ap_work_dir);
            /*-------------------------------------------------------------*/
        }


        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "");
            /*-------------------------------------------------------------*/
        }

        env_out_wrk.arr = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_out_wrk.arr)){
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG（出力ファイルDIR）%s\n", "");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit (C_const_APNG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得OK（出力ファイルDIR）%s\n", env_out_wrk);
            /*-------------------------------------------------------------*/
        }

        strcpy(out_file_dir, env_out_wrk);

        /*-------------------------------------*/
        /*  DBコネクト処理呼び出し             */
        /*-------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status.arr);
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect", rtn_cd, rtn_status, 0,
                    0, 0);
            return exit( C_const_APNG ) ;
        }

        /*-------------------------------------------*/
        /*  バッチ処理日(当日)取得処理呼び出し       */
        /*-------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日(当日)取得%s\n", "");
        }
        rtn_cd = C_GetBatDate(0,  bat_date, rtn_status );
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG status= %d\n", rtn_status.arr);
            }
            APLOG_WT( "903", 0, null,
                    "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日(当日)取得OK [%s]\n", bat_date);
        }

        /*--------------------------------*/
        /* 現在日時の取得                 */
        /*--------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** 現在日時取得%s\n", "");
        }
        rtn_cd = C_GetSysDateTime(g_sys_date, sys_time);  /* 時刻             */
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** システム時刻取得NG [%s]\n", sys_time);
            }
            APLOG_WT( "903", 0, null,
                    "C_GetSysDateTime" ,  rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }
        if (DBG_LOG) {
            C_DbgMsg("*** main *** システム時刻取得OK [%s]\n", sys_time);
        }

        /* 2021/01/21 年度管理変更期間限定ポイント追加対応 */
        /*------------------------------------------*/
        /*  バッチ処理日編集                        */
        /*------------------------------------------*/
        memset(bat_yyyymm, 0x00, sizeof(bat_yyyymm));
        memset(bat_yyyy, 0x00, sizeof(bat_yyyy));
        memset(bat_mm, 0x00, sizeof(bat_mm));

        /* バッチ処理日付年月の取得*/
        memcpy(bat_yyyymm, bat_date, 6);
        /* バッチ処理日付年の取得*/
        memcpy(bat_yyyy, bat_date, 4);
        /* バッチ処理日付月の取得*/
        memcpy(bat_mm, bat_date.arr.substring(4), 2);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日付年   [%s]\n", bat_yyyy);
        C_DbgMsg("*** main *** バッチ処理日付月   [%s]\n", bat_mm);
        /*------------------------------------------------------------*/

        /* 対象年度を設定 */
        cmBTpmdfB_getYear(ts_ser.next_year, ts_ser.this_year, ts_ser.ex_year);

        /* 対象年度（全角）変換 */
        cmBTpmdfB_getYearCd(ts_ser.next_year, ts_ser.next_year_cd); /* 翌年度*/
        cmBTpmdfB_getYearCd(ts_ser.this_year, ts_ser.this_year_cd); /* 当年度*/
        cmBTpmdfB_getYearCd(ts_ser.ex_year, ts_ser.ex_year_cd);     /* 前年度*/

        /* 文字→数値変換 */
        wk_month = atoi(bat_mm);
        /*対象月（全角）設定 */
        cmBTpmdfB_getMonth(wk_month, ts_ser.month00);
        cmBTpmdfB_getMonth(wk_month+1, ts_ser.month01);
        cmBTpmdfB_getMonth(wk_month+2, ts_ser.month02);
        cmBTpmdfB_getMonth(wk_month+3, ts_ser.month03);
        cmBTpmdfB_getMonth(wk_month+4, ts_ser.month04);

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        is_toku_syo_kikan = 0;
        this_date = atoi( bat_date );
        this_time = atoi( sys_time );
        sprintf( fl_o_name, "%s/%s_%08d%06d.txt", out_file_dir,
                OUT_FILE_NAME,this_date,this_time);

        rtn_cd = UpdatePoint(in_rec_cnt,
                         ok_rec_cnt, total_cnt, ng_rec_cnt,
                         hsph_in_rec_cnt,
                         tspn_up_rec_cnt, tspr_up_rec_cnt,
                         tspu_up_rec_cnt, tskp_up_rec_cnt);
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "912", 0, null,
                    "ポイント修正に失敗しました" , 0, 0, 0, 0, 0);

            /*  バッチデバッグ終了処理呼び出し     */
            rtn_cd = C_EndBatDbg();
            if( rtn_cd != C_const_OK ) {
                APLOG_WT( "903", 0, null,
                        "C_EndBatDbg", rtn_cd, 0, 0, 0, 0);
                // EXEC SQL ROLLBACK RELEASE;
                sqlca.rollback();
                return exit( C_const_APNG ) ;
            }

            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit( C_const_APNG ) ;
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /*** Input File Close ***/
        fclose( fp_in );
        /*** Output File Close ***/
        fclose( fp_out );

        /*  各件数出力     */
        APLOG_WT( "104", 0, null, "ポイント修正情報ファイル", in_rec_cnt, ok_rec_cnt, ng_rec_cnt,
                0, 0);
        APLOG_WT( "107", 0, null, "HSポイント日別情報", hsph_in_rec_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TSポイント年別情報", tspn_up_rec_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TSポイント累計情報", tspr_up_rec_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TS利用可能ポイント情報", tspu_up_rec_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TSランク情報", msks_up_rec_cnt, 0, 0, 0, 0);                      /* 2022/09/26 MCCM初版 MOD */
        APLOG_WT( "107", 0, null, "MS家族制度情報", mskz_up_rec_cnt, 0, 0, 0, 0);
        /* 2021/02/17 TS期間限定情報追加 Start*/
        APLOG_WT( "107", 0, null, "TS期間限定ポイント情報", tskp_up_rec_cnt, 0, 0, 0, 0);
        /* 2021/02/17 TS期間限定情報追加 End*/

        /*  コミット処理処理呼び出し     */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();
        if (DBG_LOG) {
            C_DbgMsg("*** COMMIT WORK RELEASE %s\n", "");
        }

        rtn_cd = C_EndBatDbg();
        if( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_EndBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG ) ;
        }

        /*  終了メッセージ */
        APLOG_WT( "103", 0, null, PG_NAME , 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            C_DbgMsg("*** main End %s\n", "");
        }
        return exit( C_const_APOK ) ;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgiInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgiInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-i スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-i スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArgiInf(StringDto Arg_in) {
        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        /*#define DEF_ARG_Leng    255 + 2 */        /* 文字サイズ                  */

        if (DBG_LOG) {
            C_DbgStart("Chk_ArgiInf処理");
        }
        /*-------------------------------------*/
        /*  重複指定チェック                   */
        /*-------------------------------------*/
        if( arg_i_chk != DEF_OFF ) {

            if (DBG_LOG) {
                C_DbgMsg("*** Chk_ArgiInf *** 重複指定NG = %s\n", Arg_in);
            }

            sprintf(out_format_buf, "-i 引数が重複しています（%s）", Arg_in);
            return( C_const_NG ) ;
        }
        arg_i_chk = DEF_ON;
        /*-------------------------------------*/
        /*  値の内容チェック                   */
        /*-------------------------------------*/
        /*  設定値Nullチェック  */
        if (Arg_in.arr.length() <= 2) {

            if (DBG_LOG) {
                C_DbgMsg("*** Chk_ArgiInf *** 設定値Null = %s\n", Arg_in);
            }
            sprintf(out_format_buf, "-i 引数が設定されていません（%s）", Arg_in);

            return( C_const_NG ) ;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("Chk_ArgiInf処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_i_Value, Arg_in.arr.substring(2));
        return(C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： ReadFile                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ReadFile()                                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルより１レコードを読み込む                          */
    /*                                                                            */
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
    public int ReadFile() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ReadFile処理");
            /*---------------------------------------------------------------------*/
        }
        memset(in_point_shusei, 0x00, sizeof(in_point_shusei) );
        memset(get_point_shusei, 0x00, sizeof(get_point_shusei) );

        fgets(get_point_shusei, sizeof(get_point_shusei), fp_in );
        /*fread( (char *)&in_point_shusei, POINT_SHUSEI_DATA_LEN, 1, fp_in );*/

        if ( feof( fp_in ) != C_const_OK ){
            /*---------------------------------------------*/
            C_DbgMsg( "*** ReadFile *** READ EOF status= %d\n", feof( fp_in ) ) ;
            /*---------------------------------------------*/
            return( DEF_Read_EOF );
//            if (DBG_LOG) {
//                /*-------------------------------------------------------------*/
//                C_DbgMsg("*** ReadFile *** 入力ファイルリードEOF%s\n", "");
//                /*-------------------------------------------------------------*/
//            }
        }
        if ( ferror( fp_in ) != C_const_OK ){
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ReadFile *** 入力ファイルリードNG%s\n", "");
                /*-------------------------------------------------------------*/
            }

            sprintf( out_format_buf, "fread（%s/%s）", ap_work_dir, arg_i_Value);
            APLOG_WT( "903", 0, null, out_format_buf , 0, 0, 0, 0, 0);
            return( C_const_NG );
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgEnd("ReadFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }
        return(C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： OpenInFile                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OpenInFile()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルをオープンする                                    */
    /*                                                                            */
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
    public int OpenInFile() {
        StringDto    fl_name = new StringDto(4096);

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("OpenInFile処理");
            /*---------------------------------------------------------------------*/
        }

        /* ポイント修正データファイルのオープン */
        sprintf( fl_name, "%s/%s", ap_work_dir, arg_i_Value);

        if (( fp_in = fopen(fl_name, SystemConstant.Shift_JIS, FileOpenType.r )).fd == C_const_NG){
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** OpenInFile *** ポイント修正データファイルオープンNG%s\n",
                        "");
                /*-------------------------------------------------------------*/
            }
            sprintf( out_format_buf, "fopen（%s/%s）", ap_work_dir, arg_i_Value);
            APLOG_WT( "903", 0, null, out_format_buf , 0, 0, 0, 0, 0);
            return (C_const_NG);
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgEnd("OpenInFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }


        return(C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： OpenOutFile                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OpenOutFile()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              出力ファイルをオープンする                                    */
    /*                                                                            */
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
    public int OpenOutFile() {
        /*--------------------------------------*/
        /*  ローカル変数定義                    */
        /*--------------------------------------*/
        StringDto buff = new StringDto(1024);                         /* バッファ */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("OpenOutFile処理");
            /*---------------------------------------------------------------------*/
        }

        /* ポイント修正データファイルのオープン */
        if (( fp_out = fopen(fl_o_name.arr, SystemConstant.Shift_JIS, FileOpenType.a )).fd == C_const_NG){
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** OpenOutFile *** ポイント修正データファイルオープンNG%s\n",
                        "");
                /*-------------------------------------------------------------*/
            }
            sprintf( out_format_buf, "fopen（ fl_o_name ）");
            APLOG_WT( "903", 0, null, out_format_buf , 0, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* ファイルヘッダー出力 */
        strcpy(buff, "依頼企業コード,依頼店舗,修正対象年,修正対象月," +
                "データ年月日,企業コード,会員番号,ランクＵＰ対象金額加減値," +
                "通常Ｐ基準年度,通常Ｐ加減値（当年）,通常Ｐ加減値（前年）," +
                "期間限定Ｐ基準月,期間限定Ｐ加減値（当月）,期間限定Ｐ加減値（1ヶ月後）," +
                "期間限定Ｐ加減値（2ヶ月後）,期間限定Ｐ加減値（3ヶ月後）," +
                "理由コード,ポイント一括付与結果コード,利用可能通常（当年）," +
                "利用可能通常（前年）,利用可能期間限定（当月）," +
                "利用可能期間限定（1ヶ月後）,利用可能期間限定（2ヶ月後）," +
                "利用可能期間限定（3ヶ月後）,売上連動フラグ,ファイル連番\r\n");      /* 2022/11/18 MCCM初版 MOD */

        /* 出力結果ファイルに書き込む */
        fwrite(buff.arr, strlen(buff), 1, fp_out);
        fflush(fp_out);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** OpenOutFile *** 出力結果ファイルヘッダ=%s", buff);
            C_DbgEnd("OpenOutFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }

        return(C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePoint                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int UpdatePoint(&in_rec_cnt,                                       */
    /*                         &ok_rec_cnt, &total_cnt, &ng_rec_cnt,              */
    /*                         &hsph_in_rec_cnt,                                  */
    /*                         &tspn_up_rec_cnt, &tspr_up_rec_cnt,                */
    /*                         &tspu_up_rec_cnt, &tskp_up_rec_cnt)                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント修正処理                                             */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int       *    in_rec_cnt     ：入力レコード件数                      */
    /*      int       *    ok_rec_cnt     ：正常レコード件数                      */
    /*      int       *    total_cnt      ：総付与ポイント数                      */
    /*      int       *    ng_rec_cnt     ：エラーレコード件                      */
    /*      int       *    hsph_in_rec_cnt：HSポイント日別情報追加レコード件数    */
    /*      int       *    tspn_up_rec_cnt：TSポイント年別情報更新レコード件数    */
    /*      int       *    tspr_up_rec_cnt：TSポイント累計情報更新レコード件数    */
    /*      int       *    tspu_up_rec_cnt：TS利用可能ポイント情報更新レコード件数*/
    /*      int       *    tskp_up_rec_cnt：TS期間限定ポイント情報更新レコード件数*/
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int UpdatePoint(IntegerDto in_rec_cnt, IntegerDto ok_rec_cnt, IntegerDto total_cnt, IntegerDto ng_rec_cnt, IntegerDto hsph_in_rec_cnt, IntegerDto tspn_up_rec_cnt, IntegerDto tspr_up_rec_cnt, IntegerDto tspu_up_rec_cnt, IntegerDto tskp_up_rec_cnt) {
        /* 2022/09/26 MCCM初版 DEL START */
        //  int     log_cnt;            /* ログ出力カウンタ                      */
        /* 2022/09/26 MCCM初版 DEL END */
        int     rtn_cd;             /* 関数戻り値                            */
        IntegerDto     rtn_status = new IntegerDto();         /* 関数ステータス                        */
        StringBuilder locksbt = new StringBuilder();         /* ロック種別                            */
        int     year_buf;           /* 対象年                                */
        StringDto    wk_buf = new StringDto(9);
        StringDto    kokyaku_no = new StringDto(15+1);
        StringDto    kokyaku_name = new StringDto(80*3+1);
        IntegerDto kojin_honnen_rank_cd = new IntegerDto();
        IntegerDto kojin_tougetu_rank_cd = new IntegerDto();
        IntegerDto kazoku_honnen_rank_cd = new IntegerDto();
        IntegerDto kazoku_tougetu_rank_cd = new IntegerDto();
        IntegerDto kazoku_id = new IntegerDto();
        IntegerDto in_rec_cnt_buf = new IntegerDto();
        int ok_rec_cnt_buf;
        int total_cnt_buf;
        int ng_rec_cnt_buf;
        IntegerDto hsph_in_rec_cnt_buf = new IntegerDto();
        IntegerDto tspn_up_rec_cnt_buf = new IntegerDto();
        IntegerDto tspr_up_rec_cnt_buf = new IntegerDto();
        int tspu_up_rec_cnt_buf;
        int     proc_result;
        /* 2021/02/17 TS期間限定ポイント情報追加 Start*/
        IntegerDto tskp_up_rec_cnt_buf = new IntegerDto();
        /* 2021/02/17 TS期間限定ポイント情報追加 Start*/
        /* 2022/09/26 MCCM初版 DEL START */
        //  int msks_up_flg;
        //  int mskz_up_flg;
        /* 2022/09/26 MCCM初版 DEL END */

        double  taisho_nenkan_rankup_taisho_kingaku = 0;
        /* 対象年間ランクアップ対象金額         */
        double  taisho_gekkan_rankup_taisho_kingaku = 0;
        /* 対象月間ランクアップ対象金額         */
        //  int     batyear;                /* バッチ処理日付の年                   */      /* 2022/12/30 MCCM初版 END */
        int     batmonth;               /* バッチ処理日付の月                   */
        StringDto  value_buf = new StringDto();              /* ランク関連処理取得設定値             */
        int     sedo_rankcd_year;       /* 顧客制度情報ランクコード対象年       */
        int     sedo_rankcd_month;      /* 顧客制度情報ランクコード対象月       */
        int     sedo_rankcd_month_year; /* 顧客制度情報ランクコード対象月の年   */
        int     kazoku_rankupgaku_year; /* 家族制度情報ランクＵＰ対象金額対象年 */
        int     kazoku_rankupgaku_month; /* 家族制度情報ランクＵＰ対象金額対象月 */
        int     kazoku_rankcd_year;     /* 家族制度情報ランクコード対象年       */
        int     kazoku_rankcd_month;    /* 家族制度情報ランクコード対象月       */
        int     kazoku_rankcd_month_year; /* 家族制度情報ランクコード対象月の年 */

        int     upd_pre_rankcd_year;    /* 更新前年次ランクコード               */
        int     upd_pre_rankcd_month;   /* 更新前月次ランクコード               */
        IntegerDto     get_rank_cd_year = new IntegerDto();       /* 年次ランクコード取得設定値           */
        IntegerDto     get_rank_cd_month = new IntegerDto();      /* 月次ランクコード取得設定値           */
        StringDto[] kazoku_kokyaku_wk = new StringDto[6]; /* 家族n顧客番号 + 家族n登録日 + \0 */    /* 2022/09/26 MCCM初版 MOD */
        int     i;                      /* ループカウンタ */
        StringDto    lock_kokyaku_no = new StringDto(15+1);  /* 家族顧客番号のロック用 */

        StringDto main_pt = new StringDto();           /* 基準となるポインタ                   */
        String search_pt = "";         /* ","検索用ポインタ                    */
        IntegerDto     utf8_len = new IntegerDto();                /* UTF8変換用                           */
        StringDto utf8_buf = new StringDto(488);           /* UTF8文字列格納領域                   */

        /* 年度/月跨り判定用構造体宣言 */
        CHECK_OVER_YM check_over_ym = new CHECK_OVER_YM();

        /* 初期化 */

        /*
         *in_rec_cnt = 0;
         *ok_rec_cnt = 0;
         *total_cnt  = 0
         *ng_rec_cnt = 0;
         *hsph_in_rec_cnt = 0;
         *tspn_up_rec_cnt = 0;
         *tspr_up_rec_cnt = 0;
         */
        in_rec_cnt_buf.arr = 0;
        ok_rec_cnt_buf = 0;
        total_cnt_buf  = 0;
        ng_rec_cnt_buf = 0;
        hsph_in_rec_cnt_buf.arr = 0;
        tspn_up_rec_cnt_buf.arr = 0;
        tspr_up_rec_cnt_buf.arr = 0;
        tspu_up_rec_cnt_buf = 0;
        /* 2021/02/17 TS期間限定ポイント情報追加 Start */
        tskp_up_rec_cnt_buf.arr = 0;
        /* 2021/02/17 TS期間限定ポイント情報追加 End */
        /* 2022/09/26 MCCM初版 DEL START */
        // log_cnt = 0;
        /* 2022/09/26 MCCM初版 DEL END */
        /* 2022/12/28 MCCM初版 ADD START */
        nenkan_rankup_taisho_kingaku_old = 0;  /* 更新前年間ランクＵＰ対象金額 */
        gekkan_rankup_taisho_kingaku_old = 0;  /* 更新前月間ランクＵＰ対象金額 */
        /* 2022/12/28 MCCM初版 ADD START */

        cmBTpmdfBDto.h_bat_date.arr = atoi(bat_date);
        //  batyear = h_bat_date / 10000;                 /* 2022/12/30 MCCM初版 END */
        batmonth = ( cmBTpmdfBDto.h_bat_date.intVal() / 100 ) % 100;

        /*-------------------------------------*/
        /*  入力ファイルオープン                   */
        /*-------------------------------------*/
        rtn_cd = OpenInFile();
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** 入力ファイルオープンNG rtn= %d\n", rtn_cd);
            }
            if (fp_in == null) {
                APLOG_WT("912", 0, null,
                        "ポイント修正入力ファイルのオープンに失敗しました",
                        0, 0, 0, 0, 0);
            }
            return C_const_NG;
        }

        /*-------------------------------------*/
        /*  出力ファイルオープン                   */
        /*-------------------------------------*/
        rtn_cd = OpenOutFile();
            if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** 出力ファイルオープンNG rtn= %d\n", rtn_cd);
            }
            if ( fp_out == null )
            {
                APLOG_WT( "912", 0, null,
                        "ポイント修正結果ファイルのオープンに失敗しました",
                        0, 0, 0, 0, 0);
            }
            return  C_const_NG  ;
        }


        /*-------------------------------------*/
        /*  入力ファイル読み込みループ         */
        /*-------------------------------------*/
        while ( true ) {
            proc_result = 10; /* 処理結果初期化 */
            memset( check_over_ym, 0x00, sizeof(check_over_ym));

            /*** File Read ***/
            if ((rtn_cd = ReadFile()) == DEF_Read_EOF) {
                break;
            } else if (rtn_cd != C_const_OK) {

                APLOG_WT("912", 0, null,
                        "ポイント修正情報ファイルの読込みに失敗しました",
                        0, 0, 0, 0, 0);

                sprintf(out_format_buf, "fread（%s/%s）",
                        ap_work_dir, arg_i_Value);
                APLOG_WT("903", 0, null, out_format_buf, 0, 0, 0, 0, 0);

                /*** Input File Close ***/
                fclose(fp_in);
                /*** Output File Close ***/
                fclose(fp_out);

                return C_const_NG;
            }

            /* SJIS→UTF8変換 */
            utf8_len.arr = 0;
            memset(utf8_buf, 0x00, sizeof(utf8_buf));
            rtn_cd = C_ConvSJ2UT(get_point_shusei, strlen(get_point_shusei),
                    utf8_buf, utf8_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            get_point_shusei.strtok(get_point_shusei, "\r");
            get_point_shusei.strtok(get_point_shusei, "\n");
            /* ヘッダ行かチェック */
            if (strncmp(utf8_buf.arr, FILE_HEAD, strlen(FILE_HEAD)) == 0) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpmdfB_main *** ヘッダー行=%s\n"
                        , utf8_buf);
                /*------------------------------------------------------------*/
                continue;
            } else {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpmdfB_main *** レコード=%s\n"
                        , get_point_shusei);
                /*------------------------------------------------------------*/

                in_rec_cnt_buf.arr = in_rec_cnt_buf.arr + 1;

                main_pt = get_point_shusei;

                /* 依頼企業コード */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.irai_kigyo = atoi(search_pt);

                /* 依頼店舗 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.irai_tenpo = atoi(search_pt);

                /* 修正対象年 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.shusei_taisho_nen = atoi(search_pt);

                /* 修正対象月 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.shusei_taisho_tsuki = atoi(search_pt);

                /* データ年月日 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.data_ymd = atoi(search_pt);

                /* 企業コード */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kigyo_cd = atoi(search_pt);

                /* 会員番号 */
                search_pt = strchr(main_pt, ',');

                memcpy(in_point_shusei.pid, search_pt, 17);

                /* ランクＵＰ対象金額加減値 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.rankup_taisho_kingaku_kagenti = atol(search_pt);

                /* 利用可能ポイント加減値 */
            /*search_pt = strchr( main_pt, ',') ;

            in_point_shusei.riyo_kano_point_kagenti = atof( main_pt );

            */

                /* 通常Ｐ基準年度 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.tujo_point_kijun_nendo = atoi(search_pt);

                /* 通常Ｐ加減値（当年） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.tujo_point_kagenti_tonen = atol(search_pt);

                /* 通常Ｐ加減値（前年） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.tujo_point_kagenti_zennen = atol(search_pt);

                /* 期間限定Ｐ基準月 */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kikan_point_kijun_month = atoi(search_pt);

                /* 期間限定Ｐ加減値（当月） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kikan_point_kagenti_month0 = atol(search_pt);

                /* 期間限定Ｐ加減値（1ヶ月後） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kikan_point_kagenti_month1 = atol(search_pt);

                /* 期間限定Ｐ加減値（2ヶ月後） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kikan_point_kagenti_month2 = atol(search_pt);

                /* 期間限定Ｐ加減値（3ヶ月後） */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.kikan_point_kagenti_month3 = atol(search_pt);

                /* 理由コード */
                search_pt = strchr(main_pt, ',');                        /* 2022/10/04 MCCM初版 ADD */
                /* 2022/10/04 MCCM初版 ADD */
                in_point_shusei.riyu_code = atoi(search_pt);

                /* 2022/10/04 MCCM初版 ADD */

                /* 2022/10/04 MCCM初版 ADD START */
                /* 売上連動フラグ */
                search_pt = strchr(main_pt, ',');

                in_point_shusei.uard_flg = atoi(search_pt);

                /* 連番 */
                memcpy(in_point_shusei.renban, search_pt, strlen(search_pt));
                /* 2022/10/04 MCCM初版 ADD END */

                proc_result = 10;

                memset( cmBTpmdfBDto.point_shuse_data, 0x00, sizeof(cmBTpmdfBDto.point_shuse_data));
                /* 処理通番 */
                /* 入力レコード件数＋当日の処理通番の最大値 */
                cmBTpmdfBDto.point_shuse_data.shori_seq.arr = in_rec_cnt_buf;
                cmBTpmdfBDto.point_shuse_data.irai_kigyo.arr = in_point_shusei.irai_kigyo;
                cmBTpmdfBDto.point_shuse_data.irai_tenpo.arr = in_point_shusei.irai_tenpo;
                cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.arr
                        = in_point_shusei.shusei_taisho_nen;
                cmBTpmdfBDto.point_shuse_data.shuse_taisho_tsuki.arr
                        = in_point_shusei.shusei_taisho_tsuki;
                cmBTpmdfBDto.point_shuse_data.data_ymd.arr
                        = in_point_shusei.data_ymd;
                cmBTpmdfBDto.point_shuse_data.kigyo_code.arr = in_point_shusei.kigyo_cd;
                memcpy(cmBTpmdfBDto.point_shuse_data.kaiin_no, in_point_shusei.pid.arr,
                        sizeof(in_point_shusei.pid) - 1);
                cmBTpmdfBDto.point_shuse_data.kaiin_no.len
                        = strlen(cmBTpmdfBDto.point_shuse_data.kaiin_no);
                cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.arr = in_point_shusei.rankup_taisho_kingaku_kagenti;
                /* point_shuse_data.riyo_kano_point_kagenti = in_point_shusei.riyo_kano_point_kagenti;*/
                /*point_shuse_data.riyu_code = in_point_shusei.riyu_code + 1100;*/
                cmBTpmdfBDto.point_shuse_data.riyu_code.arr = in_point_shusei.riyu_code;
                cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = proc_result;

                /* 店舗を取得する */
                rtn_cd = CheckTenpo(cmBTpmdfBDto.point_shuse_data.irai_kigyo.intVal(), cmBTpmdfBDto.point_shuse_data.irai_tenpo.intVal());
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("912", 0, null,
                            "依頼店舗の取得に失敗しました", 0, 0, 0, 0, 0);
                    return (C_const_NG);
                }
                /* 店舗存在しない場合 */
                else if (rtn_cd == C_const_OK && cmBTpmdfBDto.point_shuse_data.shori_kekka.intVal() == 20) {
                    /* エラー内容出力 */
                    rtn_cd = cmBTpmdfB_write_err();
                    if (rtn_cd != C_const_OK) {
                        return (C_const_NG);
                    }

                    /* エラー件数カウントアップ、次のレコードを読込み */
                    ng_rec_cnt_buf++;
                    continue;
                }

                /* UIDを取得する */
                memset(kokyaku_no, 0x00, sizeof(kokyaku_no));
                rtn_cd = GetUid(cmBTpmdfBDto.point_shuse_data.kigyo_code.intVal(),
                        cmBTpmdfBDto.point_shuse_data.kaiin_no.strDto(), kokyaku_no);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("912", 0, null,
                            "UIDの取得に失敗しました", 0, 0, 0, 0, 0);
                    return (C_const_NG);
                }
                /* 顧客番号が存在しない場合 */
                else if (rtn_cd == C_const_OK && cmBTpmdfBDto.point_shuse_data.shori_kekka.intVal() == 21) {
                    /* エラー内容出力 */
                    rtn_cd = cmBTpmdfB_write_err();
                    if (rtn_cd != C_const_OK) {
                        return (C_const_NG);
                    }

                    /* エラー件数カウントアップ、次のレコードを読込み */
                    ng_rec_cnt_buf++;
                    continue;
                }

                memset(kokyaku_name, 0x00, sizeof(kokyaku_name));
                /* 顧客名称、本年ランクコードの取得を行う */
                rtn_cd = GetKokyakuTeikeiRank(kokyaku_no,
                        kokyaku_name,
                        kojin_honnen_rank_cd,
                        kojin_tougetu_rank_cd,
                        kazoku_honnen_rank_cd,
                        kazoku_tougetu_rank_cd,
                        kazoku_id);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("912", 0, null,
                            "顧客名称／本年ランクコードの取得に失敗しました",
                            0, 0, 0, 0, 0);
                    return (C_const_NG);
                }
                /* 顧客制度が存在しない場合 */
                else if (rtn_cd == C_const_OK && cmBTpmdfBDto.point_shuse_data.shori_kekka.intVal() == 21) {
                    /* エラー内容出力 */
                    rtn_cd = cmBTpmdfB_write_err();
                    if (rtn_cd != C_const_OK) {
                        return (C_const_NG);
                    }

                    /* エラー件数カウントアップ、次のレコードを読込み */
                    ng_rec_cnt_buf++;
                    continue;
                }
                strcpy(cmBTpmdfBDto.point_shuse_data.kokyaku_name, kokyaku_name);

                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpmdfB_main *** GetKokyakuTeikeiRank" +
                            " 1 kokyaku_name=[%s]\n", kokyaku_name);
                    /*------------------------------------------------------------*/
                }

                /* ロック処理 */
                if (0 < atol(cmBTpmdfBDto.kokyaku_seido_data.kazoku_id)) {
                    /* 家族制度入会の場合家族ロック */
                    /* 家族ＩＤ＞０の場合 */
                    memset(cmBTpmdfBDto.mskased_t, 0x00, sizeof(cmBTpmdfBDto.mskased_t));
                    /* 家族ＩＤでＭＳ家族情報を取得する */
                    //              mskased_t.kazoku_id = kokyaku_seido_data.kazoku_id;                                  /* 2022/09/26 MCCM初版 DEL */
                    strcpy(cmBTpmdfBDto.mskased_t.kazoku_id, cmBTpmdfBDto.kokyaku_seido_data.kazoku_id);   /* 2022/09/26 MCCM初版 ADD */
                    cmBTpmdfBDto.mskased_t.kazoku_id.len = cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.len;                          /* 2022/09/26 MCCM初版 ADD */
                    rtn_cd = getKazokuSedoInfo(cmBTpmdfBDto.mskased_t, rtn_status);
                    if (rtn_cd != C_const_OK) {
                        if (DBG_LOG) {
                            /*--------------------------------------------------------*/
                            C_DbgMsg("*** cmBTpmdfB_main *** ＭＳ家族情報 " +
                                    "SELECT rtn_cd=[%d]\n", rtn_cd);
                            /*--------------------------------------------------------*/
                        }
                        /* error */
                        APLOG_WT("903", 0, null, "getKazokuSedoInfo",
                                rtn_cd, rtn_status, 0, 0, 0);
                        /* 処理を終了する */
                        return C_const_NG;
                    }

                    /* 家族ｎ顧客番号を顧客ロックする(顧客ＩＤ順) */
                    for (int y = 0; y < kazoku_kokyaku_wk.length; y++) {
                        kazoku_kokyaku_wk[y] = new StringDto(24);
                    }
                    memset(kazoku_kokyaku_wk[0], 0x00, 24);
                    memset(kazoku_kokyaku_wk[1], 0x00, 24);
                    memset(kazoku_kokyaku_wk[2], 0x00, 24);
                    memset(kazoku_kokyaku_wk[3], 0x00, 24);
                    memset(kazoku_kokyaku_wk[4], 0x00, 24);
                    memset(kazoku_kokyaku_wk[5], 0x00, 24);                                              /* 2022/09/26 MCCM初版 ADD */

                    /* 2022/09/26 MCCM初版 MOD START */
                    // sprintf(kazoku_kokyaku_wk[0], "%015ld%08d", atol((char *)mskased_t.kazoku_oya_kokyaku_no.arr), mskased_t.kazoku_oya_toroku_ymd);
                    sprintf(kazoku_kokyaku_wk[0], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_1_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_1_toroku_ymd.floatVal());
                    sprintf(kazoku_kokyaku_wk[1], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_2_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_2_toroku_ymd.floatVal());
                    sprintf(kazoku_kokyaku_wk[2], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_3_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_3_toroku_ymd.floatVal());
                    sprintf(kazoku_kokyaku_wk[3], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_4_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_4_toroku_ymd.floatVal());
                    sprintf(kazoku_kokyaku_wk[4], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_5_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_5_toroku_ymd.floatVal());
                    sprintf(kazoku_kokyaku_wk[5], "%015d%08.0f", atol(cmBTpmdfBDto.mskased_t.kazoku_6_kokyaku_no.arr),
                            cmBTpmdfBDto.mskased_t.kazoku_6_toroku_ymd.floatVal());
                    /* 2022/09/26 MCCM初版 MOD END */

                    /* ワークをソートする */
                    kazoku_kokyaku_wk = qsort(kazoku_kokyaku_wk, 6, 24);

                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid0=[%s]\n",
                                kazoku_kokyaku_wk[0]);
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid1=[%s]\n",
                                kazoku_kokyaku_wk[1]);
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid2=[%s]\n",
                                kazoku_kokyaku_wk[2]);
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid3=[%s]\n",
                                kazoku_kokyaku_wk[3]);
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid4=[%s]\n",
                                kazoku_kokyaku_wk[4]);
                        C_DbgMsg("*** cmBTpmdfB_main *** C_KdataLock uid5=[%s]\n",
                                kazoku_kokyaku_wk[5]);
                        /*------------------------------------------------------------*/
                    }

                    lock_chk = DEF_OFF;

                    for (i = 0; i < 6; i++) {
                        if (memcmp(kazoku_kokyaku_wk[i], "000000000000000", 15) != 0) {
                            memset(lock_kokyaku_no, 0x00,
                                    sizeof(lock_kokyaku_no));
                            memcpy(lock_kokyaku_no, kazoku_kokyaku_wk[i], 15);
                            /* 顧客ポイントロック処理 */
                            rtn_cd = C_KdataLock(lock_kokyaku_no,
                                    "1", rtn_status);

                            if (rtn_cd == C_const_NOTEXISTS) {
                                /* 該当顧客なし */
                                APLOG_WT("913", 0, null,
                                        lock_kokyaku_no, 0, 0, 0, 0, 0);
                                lock_chk = DEF_ON;
                                break;
                            } else if (rtn_cd != C_const_OK) {
                                /*----------------------------------------*/
                                C_DbgMsg("*** cmBTpmdfB_main *** " +
                                        "C_KdataLock NG rtn_cd=[%d]\n", rtn_cd);
                                C_DbgMsg("*** cmBTpmdfB_main *** "+
                                        "C_KdataLock NG rtn_status=[%d]\n",
                                        rtn_status.arr);
                                /*----------------------------------------*/
                                APLOG_WT("903", 0, null, "C_KdataLock",
                                        rtn_cd,
                                        rtn_status, 0, 0, 0);
                                return C_const_NG;
                            }
                        }
                    }

                    /* 該当顧客なし   */
                    /* 次のレコードへ */
                    if (lock_chk == DEF_ON) {
                        continue;
                    }
                } else {
                    //memset(locksbt, 0x00, sizeof(locksbt));
                    locksbt.append("1");   /* 顧客＋ポイントロック */
                    rtn_cd = C_KdataLock(kokyaku_no, locksbt.toString(), rtn_status);
                    if (DBG_LOG) {
                        C_DbgMsg("顧客ポイントロック [%d]\n", rtn_cd);
                    }
                    if (rtn_cd != C_const_OK) {
                        APLOG_WT("903", 0, null,
                                "C_KdataLock", rtn_cd,
                                rtn_status, 0, 0, 0);
                        return (C_const_NG);
                    }
                }

                if (DBG_LOG) {
                    /*---------------------------------------------------*/
                    C_DbgMsg("*** cmBTpmdfB_main *** GetKokyakuTeikeiRank" +
                            " 2 kokyaku_name=[%s]\n", kokyaku_name);
                    /*----------------------------------------------- ---*/
                }

                /* 修正対象年チェック */
                sprintf(wk_buf, "%d%02d", cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen,
                        cmBTpmdfBDto.point_shuse_data.shuse_taisho_tsuki);

                /* チェックエラーの場合 */
                if (0 != memcmp(wk_buf, bat_date.arr, 6)) {
                    cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = 22;

                    /* エラー内容出力 */
                    rtn_cd = cmBTpmdfB_write_err();
                    if (rtn_cd != C_const_OK) {
                        return (C_const_NG);
                    }

                    /* エラー件数カウントアップ、次のレコードを読込み */
                    ng_rec_cnt_buf++;
                    continue;
                }

                /* 2023/01/08 MCCM初版 ADD START */
                shuse_taisho_nen_moto = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal();
                /* 修正対象年取得 */
                if (in_point_shusei.shusei_taisho_tsuki < 4) {
                    cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.arr = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() - 1;
                }
                if (DBG_LOG) {
                    C_DbgMsg("修正対象年=[%d]\n", cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen);
                }
                /* 2023/01/08 MCCM初版 ADD END */

                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTpmdfB_main *** GetKokyakuTeikeiRank 3 kokyaku_name=[%s]\n", kokyaku_name);
                    /*------------------------------------------------------------*/
                }
                /* 2021/01/21 年度管理変更/期間限定ポイント追加対応 */

                if (in_point_shusei.tujo_point_kijun_nendo != 0) {
                    /* 通常Ｐ基準年度チェック */
                    rtn_cd = cmBTpmdfB_check_kijun_nendo();
                    /* チェックエラーの場合 */
                    if (rtn_cd == C_const_Stat_ELSERR) {
                        /* エラー件数カウントアップ、次のレコードを読込み */
                        ng_rec_cnt_buf++;
                        continue;
                    } else if (rtn_cd == C_const_NG) {
                        return (C_const_NG);
                    }
                }

                if (in_point_shusei.kikan_point_kijun_month != 0) {
                    /* 期間限定Ｐ基準月チェック */
                    rtn_cd = cmBTpmdfB_check_kijun_month();
                    /* チェックエラーの場合 */
                    if (rtn_cd == C_const_Stat_ELSERR) {
                        /* エラー件数カウントアップ、次のレコードを読込み */
                        ng_rec_cnt_buf++;
                        continue;
                    } else if (rtn_cd == C_const_NG) {
                        return (C_const_NG);
                    }
                }

                /* 通常Ｐ年度跨ぎ判定処理 */
                cmBTpmdfB_check_over_years(check_over_ym);

                /* 通常Ｐ年度跨ぎ判定処理 */
                cmBTpmdfB_check_over_months(check_over_ym);

                /* 当年ポイント年別情報取得 */
                memset(wk_buf, 0x00, sizeof(wk_buf));
                memcpy(wk_buf, bat_date, 4);
                year_buf = atoi(wk_buf);
                memset(cmBTpmdfBDto.tonen_point_data, 0x00, sizeof(cmBTpmdfBDto.tonen_point_data));
                strcpy(cmBTpmdfBDto.tonen_point_data.kokyaku_no, kokyaku_no);
                cmBTpmdfBDto.tonen_point_data.kokyaku_no.len
                        = strlen(cmBTpmdfBDto.tonen_point_data.kokyaku_no);
                cmBTpmdfBDto.tonen_point_data.year.arr = year_buf;

                rtn_cd = cmBTfuncBImpl.C_GetYearPoint(cmBTpmdfBDto.tonen_point_data, year_buf, rtn_status );
                if (DBG_LOG) {
                    C_DbgMsg("当年ポイント年別情報取得 [%d]\n", rtn_cd);
                }
                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null,
                            "C_GetYearPoint",  rtn_cd,
                             rtn_status, 0, 0, 0);
                    return (C_const_NG);
                }
                if (rtn_cd == C_const_NOTEXISTS) {

                    /* 年別未存在の場合は、処理結果を設定して処理を続行する */
                    /* point_shuse_data.shori_kekka = 8140; */
                }

                /* 2022/12/28 MCCM初版 ADD START */
                memset(cmBTpmdfBDto.h_ts_rank_info_data, 0x00, sizeof(cmBTpmdfBDto.h_ts_rank_info_data));
                strcpy(cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no, kokyaku_no);
                cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no.len = strlen(cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no);

                /* TSランク情報取得 */
                rtn_cd = GetTsRankInfo(cmBTpmdfBDto.h_ts_rank_info_data, rtn_status);

                if (rtn_cd == C_const_NG) {
                    APLOG_WT("903", 0, null,
                            "GetTsRankInfo", rtn_cd, 0, 0, 0, 0);

                    return (C_const_NG);
                }

                if (rtn_cd == C_const_NOTEXISTS) {
                    /* TSランク情報の追加 */
                    /*EXEC SQL INSERT INTO TSランク情報(
                            顧客番号,
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
                            最終更新日,
                            最終更新日時,
                            最終更新プログラムＩＤ)
                    VALUES(
                                                      :ms_card_data.kokyaku_no,
                                                      :h_ts_rank_info_data.nenji_rank_cd_0,
                                                      :h_ts_rank_info_data.nenji_rank_cd_1,
                                                      :h_ts_rank_info_data.nenji_rank_cd_2,
                                                      :h_ts_rank_info_data.nenji_rank_cd_3,
                                                      :h_ts_rank_info_data.nenji_rank_cd_4,
                                                      :h_ts_rank_info_data.nenji_rank_cd_5,
                                                      :h_ts_rank_info_data.nenji_rank_cd_6,
                                                      :h_ts_rank_info_data.nenji_rank_cd_7,
                                                      :h_ts_rank_info_data.nenji_rank_cd_8,
                                                      :h_ts_rank_info_data.nenji_rank_cd_9,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_001,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_002,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_003,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_004,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_005,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_006,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_007,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_008,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_009,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_010,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_011,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_012,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_101,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_102,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_103,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_104,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_105,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_106,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_107,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_108,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_109,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_110,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_111,
                                                      :h_ts_rank_info_data.getsuji_rank_cd_112,
                                                      :h_bat_date,
                            sysdate,
                                                      :h_ts_rank_info_data.saishu_koshin_programid);*/
                    StringDto workSql = new StringDto();
                    workSql.arr = "INSERT INTO TSランク情報(\n" +
                            "                            顧客番号,\n" +
                            "                            年次ランクコード０,\n" +
                            "                            年次ランクコード１,\n" +
                            "                            年次ランクコード２,\n" +
                            "                            年次ランクコード３,\n" +
                            "                            年次ランクコード４,\n" +
                            "                            年次ランクコード５,\n" +
                            "                            年次ランクコード６,\n" +
                            "                            年次ランクコード７,\n" +
                            "                            年次ランクコード８,\n" +
                            "                            年次ランクコード９,\n" +
                            "                            月次ランクコード００１,\n" +
                            "                            月次ランクコード００２,\n" +
                            "                            月次ランクコード００３,\n" +
                            "                            月次ランクコード００４,\n" +
                            "                            月次ランクコード００５,\n" +
                            "                            月次ランクコード００６,\n" +
                            "                            月次ランクコード００７,\n" +
                            "                            月次ランクコード００８,\n" +
                            "                            月次ランクコード００９,\n" +
                            "                            月次ランクコード０１０,\n" +
                            "                            月次ランクコード０１１,\n" +
                            "                            月次ランクコード０１２,\n" +
                            "                            月次ランクコード１０１,\n" +
                            "                            月次ランクコード１０２,\n" +
                            "                            月次ランクコード１０３,\n" +
                            "                            月次ランクコード１０４,\n" +
                            "                            月次ランクコード１０５,\n" +
                            "                            月次ランクコード１０６,\n" +
                            "                            月次ランクコード１０７,\n" +
                            "                            月次ランクコード１０８,\n" +
                            "                            月次ランクコード１０９,\n" +
                            "                            月次ランクコード１１０,\n" +
                            "                            月次ランクコード１１１,\n" +
                            "                            月次ランクコード１１２,\n" +
                            "                            最終更新日,\n" +
                            "                            最終更新日時,\n" +
                            "                            最終更新プログラムＩＤ)\n" +
                            "                    VALUES(" +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            ?," +
                            "                            sysdate()," +
                            "                            ?" +
                            ")";
                    sqlca.sql = workSql;
                    sqlca.restAndExecute(cmBTpmdfBDto.ms_card_data.kokyaku_no,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_0,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_1,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_2,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_3,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_4,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_5,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_6,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_7,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_8,
                                    cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_9,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_001,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_002,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_003,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_004,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_005,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_006,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_007,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_008,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_009,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_010,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_011,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_012,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_101,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_102,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_103,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_104,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_105,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_106,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_107,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_108,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_109,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_110,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_111,
                                    cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_112,
                                    cmBTpmdfBDto.h_bat_date,
                                    cmBTpmdfBDto.h_ts_rank_info_data.saishu_koshin_programid);

                    /* エラーの場合処理を異常終了する */
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        sprintf(out_format_buf,
                                "顧客番号=[%17s] 会員番号=[%17s]",
                                cmBTpmdfBDto.ms_card_data.kokyaku_no.arr,
                                cmBTpmdfBDto.point_shuse_data.kaiin_no.arr);
                        APLOG_WT("904", 0, null, "UPDATE",
                                sqlca.sqlcode,
                                "TSランク情報", out_format_buf, 0, 0);
                        return C_const_NG;
                    }
                }
                /* 2022/12/28 MCCM初版 ADD END */

                cmBTpmdfBDto.point_shuse_data.kaiage_nissu_koshin_mae =
                        cmBTpmdfBDto.tonen_point_data.nenkan_kaiage_nissu;
                cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_koshin_mae =
                        cmBTpmdfBDto.tonen_point_data.nenkan_rankup_taisho_kingaku;

                /* TS利用可能ポイント情報から利用可能ポイント他を取得する */
                /*EXEC SQL SELECT 利用可能ポイント,
                            入会企業コード,
                            入会店舗,
                            発券企業コード,
                            発券店舗
                       INTO :tsrkpoint_t.nyukai_kigyo_cd,
                            :tsrkpoint_t.nyukai_tenpo,
                            :tsrkpoint_t.hakken_kigyo_cd,
                            :tsrkpoint_t.hakken_tenpo
                       FROM TS利用可能ポイント情報
                      WHERE 顧客番号 = :ms_card_data.kokyaku_no;*/

                /* エラーの場合処理を異常終了する */
                /*if ( sqlca.sqlcode != C_const_Ora_OK ) {*/
                /* DBERR */
                   /* sprintf( out_format_buf, "顧客番号=%s",
                             ms_card_data.kokyaku_no.arr);
                    APLOG_WT( "904", 0, NULL, "SELECT",
                        (char *)(long)sqlca.sqlcode,
                              "TS利用可能ポイント情報", out_format_buf, 0, 0);*/
                /* 処理を終了する */
                   /* return C_const_NG;
                }*/

                /* 2021/01/21 年度管理変更/期間限定ポイント追加対応 */
                /* TS利用可能ポイント情報取得 */
                rtn_cd = cmBTpmdfB_getTSriyo();
                if (rtn_cd != C_const_OK) {
                    return C_const_NG;
                }

                /* 期間限定Ｐ更新値のマイナスチェック */
                rtn_cd = cmBTpmdfB_check_updval(check_over_ym);
                /* チェックエラーの場合 */
                if (rtn_cd == C_const_Stat_ELSERR) {
                    /* エラー件数カウントアップ、次のレコードを読込み */
                    ng_rec_cnt_buf++;
                    continue;
                } else if (rtn_cd == C_const_NG) {
                    return (C_const_NG);
                }
                /*point_shuse_data.riyo_kano_point_koshin_mae =0;*/

                sedo_rankcd_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;               /* 2022/12/28 MCCM初版 MOD */
                sedo_rankcd_month = batmonth + 1;
                sedo_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal();             /* 2022/12/28 MCCM初版 MOD */
                if (sedo_rankcd_month >= 13) {
                    sedo_rankcd_month -= 12;
                    sedo_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;      /* 2022/12/28 MCCM初版 MOD */
                }
                if (sedo_rankcd_month == 4)                                            /* 2022/12/30 MCCM初版 ADD */ {                                                                        /* 2022/12/30 MCCM初版 ADD */
                    sedo_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;     /* 2022/12/30 MCCM初版 ADD */
                }                                                                        /* 2022/12/30 MCCM初版 MOD */
                kazoku_rankupgaku_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal();              /* 2022/12/30 MCCM初版 MOD */
                kazoku_rankupgaku_month = batmonth;
                kazoku_rankcd_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;              /* 2022/12/30 MCCM初版 MOD */
                kazoku_rankcd_month = batmonth + 1;
                kazoku_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal();            /* 2022/12/30 MCCM初版 MOD */
                if (kazoku_rankcd_month >= 13) {
                    kazoku_rankcd_month -= 12;
                    kazoku_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;    /* 2022/12/30 MCCM初版 MOD */
                }
                if (kazoku_rankcd_month == 4)                                          /* 2022/12/30 MCCM初版 MOD */ {                                                                        /* 2022/12/30 MCCM初版 MOD */
                    kazoku_rankcd_month_year = cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() + 1;   /* 2022/12/30 MCCM初版 MOD */
                }

                /* 顧客制度情報更新 */

                /* 当年の年間ランクＵＰ対象金額にランクＵＰ対象金額加減値を加算する */
                /* 2022/12/28 MCCM初版 ADD START */
                switch (cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() % 10) {
                    case 0:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0.longVal();
                        break;
                    case 1:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1.longVal();
                        break;
                    case 2:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2.longVal();
                        break;
                    case 3:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3.longVal();
                        break;
                    case 4:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4.longVal();
                        break;
                    case 5:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5.longVal();
                        break;
                    case 6:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6.longVal();
                        break;
                    case 7:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7.longVal();
                        break;
                    case 8:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8.longVal();
                        break;
                    case 9:
                        taisho_nenkan_rankup_taisho_kingaku
                                = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9.floatVal()
                                + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                        nenkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9.longVal();
                        break;
                }
                /* 2022/12/28 MCCM初版 ADD END */

                /* 年次ランク取得処理 */
                rtn_cd = cmBTfuncBImpl.C_GetRank(taisho_nenkan_rankup_taisho_kingaku,
                        DEF_RANK_YEAR, get_rank_cd_year, rtn_status);
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*---------------------------------------------------------------*/
                        C_DbgMsg("*** C_GetRank *** ランクコード設定NG%s\n", "");
                        /*---------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_GetRank",
                            rtn_cd, rtn_status, 0, 0, 0 );

                    /* 処理を終了する */
                    return C_const_NG;
                }

                /* 当月の月間ランクＵＰ対象金額にランクＵＰ対象金額加減値を加算する */
                if ((cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal() % 2) == 0) {
                    switch (batmonth) {
                        case 1:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 2:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 3:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 4:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 5:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 6:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 7:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 8:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 9:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 10:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 11:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                        case 12:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012.floatVal()                                        /* 2022/12/28 MCCM初版 MOD */
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012.longVal();      /* 2022/12/28 MCCM初版 ADD */
                            break;
                    }
                }
                /* 2022/12/28 MCCM初版 ADD START */
                else {
                    switch (batmonth) {
                        case 1:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101.longVal();
                            break;
                        case 2:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102.longVal();
                            break;
                        case 3:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103.longVal();
                            break;
                        case 4:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104.longVal();
                            break;
                        case 5:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105.longVal();
                            break;
                        case 6:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106.longVal();
                            break;
                        case 7:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107.longVal();
                            break;
                        case 8:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108.longVal();
                            break;
                        case 9:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109.longVal();
                            break;
                        case 10:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110.longVal();
                            break;
                        case 11:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111.longVal();
                            break;
                        case 12:
                            taisho_gekkan_rankup_taisho_kingaku
                                    = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112.floatVal()
                                    + cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal();
                            gekkan_rankup_taisho_kingaku_old = cmBTpmdfBDto.h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112.longVal();
                            break;
                    }
                }
                /* 2022/12/28 MCCM初版 ADD END */

                /* 月次ランク取得処理 */
                rtn_cd = cmBTfuncBImpl.C_GetRank(taisho_gekkan_rankup_taisho_kingaku,
                        DEF_RANK_MONTH, get_rank_cd_month, rtn_status);
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*----------------------------------------------------------------*/
                        C_DbgMsg("*** C_GetRank *** ランクコード設定NG%s\n", "");
                        /*----------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_GetRank",
                            rtn_cd, rtn_status, 0, 0, 0 );

                    /* 処理を終了する */
                    return C_const_NG;
                }

                /* 更新対象年の年次ランクコードを取得する */
                getAboutRankColumn(DEF_TBLKBN_SEDO, DEF_RANK_YEAR,
                        DEF_COLKBN_RANKCD, sedo_rankcd_year, 0, value_buf);
                upd_pre_rankcd_year = value_buf.intVal();

                /* 更新対象月の月次ランクコードを取得する */
                getAboutRankColumn(DEF_TBLKBN_SEDO, DEF_RANK_MONTH,
                        DEF_COLKBN_RANKCD, sedo_rankcd_month_year, sedo_rankcd_month,
                        value_buf);
                upd_pre_rankcd_month = value_buf.intVal();

                /* 更新対象年の年次ランクコードと
                年別情報のランクＵＰ対象金額から取得した年次ランクコードが
                異なる場合 */
                /*  or 更新対象年の月次ランクコードと
                年別情報のランクＵＰ対象金額から取得した月次ランクコードが
                異なる場合 */
                if (get_rank_cd_year.arr != upd_pre_rankcd_year ||
                        get_rank_cd_month.arr != upd_pre_rankcd_month) {

                    /* 更新対象年のランクコードを設定、更新 */
                    if (get_rank_cd_year.arr != upd_pre_rankcd_year) {
                        setAboutRankColumn(DEF_TBLKBN_SEDO,
                                DEF_RANK_YEAR, DEF_COLKBN_RANKCD, sedo_rankcd_year,
                                0, get_rank_cd_year.arr);
                    }
                    /* 更新対象月のランクコードを設定、更新 */
                    if (get_rank_cd_month.arr != upd_pre_rankcd_month) {
                        setAboutRankColumn(DEF_TBLKBN_SEDO, DEF_RANK_MONTH,
                                DEF_COLKBN_RANKCD, sedo_rankcd_month_year,
                                sedo_rankcd_month, get_rank_cd_month.arr);
                    }

                    strcpy(cmBTpmdfBDto.h_ts_rank_info_data.saishu_koshin_programid,
                            Cg_Program_Name);

                    /* 2022/09/26 MCCM初版 MOD START */
                    /* TSランク情報の更新 */
                   /* EXEC SQL UPDATE TSランク情報
                    SET 年次ランクコード０ = :h_ts_rank_info_data.nenji_rank_cd_0,
                            年次ランクコード１ = :h_ts_rank_info_data.nenji_rank_cd_1,
                            年次ランクコード２ = :h_ts_rank_info_data.nenji_rank_cd_2,
                            年次ランクコード３ = :h_ts_rank_info_data.nenji_rank_cd_3,
                            年次ランクコード４ = :h_ts_rank_info_data.nenji_rank_cd_4,
                            年次ランクコード５ = :h_ts_rank_info_data.nenji_rank_cd_5,
                            年次ランクコード６ = :h_ts_rank_info_data.nenji_rank_cd_6,
                            年次ランクコード７ = :h_ts_rank_info_data.nenji_rank_cd_7,
                            年次ランクコード８ = :h_ts_rank_info_data.nenji_rank_cd_8,
                            年次ランクコード９ = :h_ts_rank_info_data.nenji_rank_cd_9,
                            月次ランクコード００１ = :h_ts_rank_info_data.getsuji_rank_cd_001,
                            月次ランクコード００２ = :h_ts_rank_info_data.getsuji_rank_cd_002,
                            月次ランクコード００３ = :h_ts_rank_info_data.getsuji_rank_cd_003,
                            月次ランクコード００４ = :h_ts_rank_info_data.getsuji_rank_cd_004,
                            月次ランクコード００５ = :h_ts_rank_info_data.getsuji_rank_cd_005,
                            月次ランクコード００６ = :h_ts_rank_info_data.getsuji_rank_cd_006,
                            月次ランクコード００７ = :h_ts_rank_info_data.getsuji_rank_cd_007,
                            月次ランクコード００８ = :h_ts_rank_info_data.getsuji_rank_cd_008,
                            月次ランクコード００９ = :h_ts_rank_info_data.getsuji_rank_cd_009,
                            月次ランクコード０１０ = :h_ts_rank_info_data.getsuji_rank_cd_010,
                            月次ランクコード０１１ = :h_ts_rank_info_data.getsuji_rank_cd_011,
                            月次ランクコード０１２ = :h_ts_rank_info_data.getsuji_rank_cd_012,
                            月次ランクコード１０１ = :h_ts_rank_info_data.getsuji_rank_cd_101,
                            月次ランクコード１０２ = :h_ts_rank_info_data.getsuji_rank_cd_102,
                            月次ランクコード１０３ = :h_ts_rank_info_data.getsuji_rank_cd_103,
                            月次ランクコード１０４ = :h_ts_rank_info_data.getsuji_rank_cd_104,
                            月次ランクコード１０５ = :h_ts_rank_info_data.getsuji_rank_cd_105,
                            月次ランクコード１０６ = :h_ts_rank_info_data.getsuji_rank_cd_106,
                            月次ランクコード１０７ = :h_ts_rank_info_data.getsuji_rank_cd_107,
                            月次ランクコード１０８ = :h_ts_rank_info_data.getsuji_rank_cd_108,
                            月次ランクコード１０９ = :h_ts_rank_info_data.getsuji_rank_cd_109,
                            月次ランクコード１１０ = :h_ts_rank_info_data.getsuji_rank_cd_110,
                            月次ランクコード１１１ = :h_ts_rank_info_data.getsuji_rank_cd_111,
                            月次ランクコード１１２ = :h_ts_rank_info_data.getsuji_rank_cd_112,
                            最終更新日 = :h_bat_date,
                            最終更新日時 = sysdate,
                            最終更新プログラムＩＤ = :h_ts_rank_info_data.saishu_koshin_programid
                    WHERE 顧客番号 = :ms_card_data.kokyaku_no;*/
                    StringDto worksql = new StringDto();
                    worksql.arr = "UPDATE TSランク情報\n" +
                            "                    SET 年次ランクコード０ = ?,\n" +
                            "                        年次ランクコード１ = ?,\n" +
                            "                        年次ランクコード２ = ?,\n" +
                            "                        年次ランクコード３ = ?,\n" +
                            "                        年次ランクコード４ = ?,\n" +
                            "                        年次ランクコード５ = ?,\n" +
                            "                        年次ランクコード６ = ?,\n" +
                            "                        年次ランクコード７ = ?,\n" +
                            "                        年次ランクコード８ = ?,\n" +
                            "                        年次ランクコード９ = ?,\n" +
                            "                        月次ランクコード００１ = ?,\n" +
                            "                        月次ランクコード００２ = ?,\n" +
                            "                        月次ランクコード００３ = ?,\n" +
                            "                        月次ランクコード００４ = ?,\n" +
                            "                        月次ランクコード００５ = ?,\n" +
                            "                        月次ランクコード００６ = ?,\n" +
                            "                        月次ランクコード００７ = ?,\n" +
                            "                        月次ランクコード００８ = ?,\n" +
                            "                        月次ランクコード００９ = ?,\n" +
                            "                        月次ランクコード０１０ = ?,\n" +
                            "                        月次ランクコード０１１ = ?,\n" +
                            "                        月次ランクコード０１２ = ?,\n" +
                            "                        月次ランクコード１０１ = ?,\n" +
                            "                        月次ランクコード１０２ = ?,\n" +
                            "                        月次ランクコード１０３ = ?,\n" +
                            "                        月次ランクコード１０４ = ?,\n" +
                            "                        月次ランクコード１０５ = ?,\n" +
                            "                        月次ランクコード１０６ = ?,\n" +
                            "                        月次ランクコード１０７ = ?,\n" +
                            "                        月次ランクコード１０８ = ?,\n" +
                            "                        月次ランクコード１０９ = ?,\n" +
                            "                        月次ランクコード１１０ = ?,\n" +
                            "                        月次ランクコード１１１ = ?,\n" +
                            "                        月次ランクコード１１２ = ?,\n" +
                            "                        最終更新日 = ?,\n" +
                            "                        最終更新日時 = sysdate(),\n" +
                            "                        最終更新プログラムＩＤ = ?\n" +
                            "                    WHERE 顧客番号 = ?";
                    sqlca.sql = worksql;
                    sqlca.restAndExecute(cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_0,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_1,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_2,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_3,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_4,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_5,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_6,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_7,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_8,
                                            cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_9,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_001,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_002,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_003,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_004,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_005,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_006,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_007,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_008,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_009,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_010,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_011,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_012,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_101,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_102,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_103,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_104,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_105,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_106,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_107,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_108,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_109,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_110,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_111,
                                            cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_112,
                                            cmBTpmdfBDto.h_bat_date,
                                            cmBTpmdfBDto.h_ts_rank_info_data.saishu_koshin_programid,
                                            cmBTpmdfBDto.ms_card_data.kokyaku_no);

                    /* エラーの場合処理を異常終了する */
                    if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                        sprintf(out_format_buf,
                                "顧客番号=[%17s] 会員番号=[%17s]",
                                cmBTpmdfBDto.ms_card_data.kokyaku_no.arr,
                                cmBTpmdfBDto.point_shuse_data.kaiin_no.arr);
                        APLOG_WT("904", 0, null, "UPDATE",
                                sqlca.sqlcode,
                                "TSランク情報", out_format_buf, 0, 0);
                        return C_const_NG;
                    }

                    //              msks_up_flg = 1;                                                                     /* 2022/09/26 MCCM初版 MOD */
                    msks_up_rec_cnt.arr++;
                }

                /* 2022/09/26 MCCM初版 MOD END */

                /* 家族制度情報更新 */
                if ((0 < atol(cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.arr)) && cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.intVal() != 0) {

                    /* 年次ランク取得 */

                    /* 当年の年間ランクＵＰ対象金額に
                    ランクＵＰ対象金額加減値を加算する */
                    setAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_YEAR,
                            DEF_COLKBN_RANKUPGAKU, kazoku_rankupgaku_year, 0,
                            cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal());

                    if (DBG_LOG) {
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** kazoku_rankupgaku_year%d\n",
                                kazoku_rankupgaku_year);
                        /*------------------------------------------------------------*/
                    }

                    /* 当年の年間家族ランクアップ対象金額を取得する */
                    getAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_YEAR,
                            DEF_COLKBN_RANKUPGAKU, kazoku_rankupgaku_year,
                            0, value_buf);

                    /* ランク取得処理 */
                    rtn_cd = cmBTfuncBImpl.C_GetRank(value_buf.floatVal(), DEF_RANK_YEAR,
                            get_rank_cd_year, rtn_status);
                    if (rtn_cd != C_const_OK) {
                        if (DBG_LOG) {
                            /*--------------------------------------------------------*/
                            C_DbgMsg("*** C_GetRank *** ランクコード設定NG%s\n", "");
                            /*-------------------------------------------------------*/
                        }
                        APLOG_WT("903", 0, null, "C_GetRank",
                                rtn_cd, rtn_status, 0, 0, 0 );

                        /* 処理を終了する */
                        return C_const_NG;
                    }

                    /* 月次ランク取得 */

                    /* 当月の月間ランクＵＰ対象金額に
                    ランクＵＰ対象金額加減値を加算する */
                    setAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_MONTH,
                            DEF_COLKBN_RANKUPGAKU, kazoku_rankupgaku_year,
                            kazoku_rankupgaku_month, cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.floatVal());

                    /* ランクアップ対象金額を取得する */
                    getAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_MONTH,
                            DEF_COLKBN_RANKUPGAKU, kazoku_rankupgaku_year,
                            kazoku_rankupgaku_month, value_buf);

                    /* ランク取得処理 */
                    rtn_cd = cmBTfuncBImpl.C_GetRank(value_buf.floatVal(), DEF_RANK_MONTH,
                            get_rank_cd_month, rtn_status);
                    if (rtn_cd != C_const_OK) {
                        if (DBG_LOG) {
                            /*-------------------------------------------------------*/
                            C_DbgMsg("*** C_GetRank *** ランクコード設定NG%s\n", "");
                            /*-------------------------------------------------------*/
                        }
                        APLOG_WT("903", 0, null, "C_GetRank",
                                rtn_cd, rtn_status, 0, 0, 0 );

                        /* 処理を終了する */
                        return C_const_NG;
                    }

                    /* 更新対象年のランクコードを取得する */
                    getAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_YEAR,
                            DEF_COLKBN_RANKCD, kazoku_rankcd_year, 0, value_buf);
                    upd_pre_rankcd_year = value_buf.intVal();

                    /* 更新対象月のランクコードを取得する */
                    getAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_MONTH,
                            DEF_COLKBN_RANKCD, kazoku_rankcd_month_year,
                            kazoku_rankcd_month, value_buf);
                    upd_pre_rankcd_month = value_buf.intVal();

                    /* 更新対象年のランクコードと家族制度情報ランクＵＰ対象金額から
                    取得したランクコードが異なる場合 */
                    /*  or 更新対象年の月次ランクコードと
                    年別情報のランクＵＰ対象金額から取得した月次ランクコードが
                    異なる場合 */
                    if (get_rank_cd_year.arr != upd_pre_rankcd_year ||
                            get_rank_cd_month.arr != upd_pre_rankcd_month) {
                        /* 更新対象年のランクコードを設定、更新 */
                        if (get_rank_cd_year.arr != upd_pre_rankcd_year) {
                            setAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_YEAR,
                                    DEF_COLKBN_RANKCD, kazoku_rankcd_year, 0,
                                    Double.valueOf(get_rank_cd_year.arr));
                        }
                        /* 更新対象月のランクコードを設定、更新 */
                        if (get_rank_cd_month.arr != upd_pre_rankcd_month) {
                            setAboutRankColumn(DEF_TBLKBN_KAZOKU, DEF_RANK_MONTH,
                                    DEF_COLKBN_RANKCD, kazoku_rankcd_month_year,
                                    kazoku_rankcd_month, Double.valueOf(get_rank_cd_month.arr));
                        }
                    }

                    strcpy(cmBTpmdfBDto.mskased_t.saishu_koshin_programid,
                        Cg_Program_Name);

                    /* 家族制度情報を更新 */
                    /*EXEC SQL UPDATE MS家族制度情報
                    SET 家族ランクＵＰ金額最終更新日 = :h_bat_date,
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
                        年次ランクコード０ = :mskased_t.nenji_rank_cd_0,
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
                        バッチ更新日 = :h_bat_date,
                        最終更新日 = :h_bat_date,
                        最終更新日時 = sysdate,
                        最終更新プログラムＩＤ = :mskased_t.saishu_koshin_programid
                    WHERE 家族ＩＤ = :kokyaku_seido_data.kazoku_id;*/
                    StringDto worksql = new StringDto();
                    worksql.arr = "UPDATE MS家族制度情報\n" +
                            "                    SET 家族ランクＵＰ金額最終更新日 = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額０ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額１ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額２ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額３ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額４ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額５ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額６ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額７ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額８ = ?,\n" +
                            "                        年間家族ランクＵＰ対象金額９ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００１ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００２ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００３ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００４ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００５ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００６ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００７ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００８ = ?,\n" +
                            "                        月間家族ランクＵＰ金額００９ = ?,\n" +
                            "                        月間家族ランクＵＰ金額０１０ = ?,\n" +
                            "                        月間家族ランクＵＰ金額０１１ = ?,\n" +
                            "                        月間家族ランクＵＰ金額０１２ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０１ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０２ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０３ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０４ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０５ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０６ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０７ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０８ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１０９ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１１０ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１１１ = ?,\n" +
                            "                        月間家族ランクＵＰ金額１１２ = ?,\n" +
                            "                        年次ランクコード０ = ?,\n" +
                            "                        年次ランクコード１ = ?,\n" +
                            "                        年次ランクコード２ = ?,\n" +
                            "                        年次ランクコード３ = ?,\n" +
                            "                        年次ランクコード４ = ?,\n" +
                            "                        年次ランクコード５ = ?,\n" +
                            "                        年次ランクコード６ = ?,\n" +
                            "                        年次ランクコード７ = ?,\n" +
                            "                        年次ランクコード８ = ?,\n" +
                            "                        年次ランクコード９ = ?,\n" +
                            "                        月次ランクコード００１ = ?,\n" +
                            "                        月次ランクコード００２ = ?,\n" +
                            "                        月次ランクコード００３ = ?,\n" +
                            "                        月次ランクコード００４ = ?,\n" +
                            "                        月次ランクコード００５ = ?,\n" +
                            "                        月次ランクコード００６ = ?,\n" +
                            "                        月次ランクコード００７ = ?,\n" +
                            "                        月次ランクコード００８ = ?,\n" +
                            "                        月次ランクコード００９ = ?,\n" +
                            "                        月次ランクコード０１０ = ?,\n" +
                            "                        月次ランクコード０１１ = ?,\n" +
                            "                        月次ランクコード０１２ = ?,\n" +
                            "                        月次ランクコード１０１ = ?,\n" +
                            "                        月次ランクコード１０２ = ?,\n" +
                            "                        月次ランクコード１０３ = ?,\n" +
                            "                        月次ランクコード１０４ = ?,\n" +
                            "                        月次ランクコード１０５ = ?,\n" +
                            "                        月次ランクコード１０６ = ?,\n" +
                            "                        月次ランクコード１０７ = ?,\n" +
                            "                        月次ランクコード１０８ = ?,\n" +
                            "                        月次ランクコード１０９ = ?,\n" +
                            "                        月次ランクコード１１０ = ?,\n" +
                            "                        月次ランクコード１１１ = ?,\n" +
                            "                        月次ランクコード１１２ = ?,\n" +
                            "                        バッチ更新日 = ?,\n" +
                            "                        最終更新日 = ?,\n" +
                            "                        最終更新日時 = sysdate(),\n" +
                            "                        最終更新プログラムＩＤ = ?\n" +
                            "                    WHERE 家族ＩＤ = ?";
                    sqlca.sql = worksql;
                    sqlca.restAndExecute(cmBTpmdfBDto.h_bat_date,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,
                                cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,
                                cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_0,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_1,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_2,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_3,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_4,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_5,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_6,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_7,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_8,
                                cmBTpmdfBDto.mskased_t.nenji_rank_cd_9,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_001,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_002,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_003,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_004,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_005,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_006,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_007,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_008,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_009,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_010,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_011,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_012,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_101,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_102,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_103,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_104,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_105,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_106,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_107,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_108,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_109,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_110,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_111,
                                cmBTpmdfBDto.mskased_t.getuji_rank_cd_112,
                                cmBTpmdfBDto.h_bat_date,
                                cmBTpmdfBDto.h_bat_date,
                                cmBTpmdfBDto.mskased_t.saishu_koshin_programid,
                                cmBTpmdfBDto.kokyaku_seido_data.kazoku_id);

                    /* エラーの場合処理を異常終了する */
                    if (sqlca.sqlcode != C_const_Ora_OK) {
                        sprintf(out_format_buf,
                                "家族ＩＤ=[%d]  会員番号=[%17s]",                                        /* 2022/09/26 MCCM初版 MOD */
                                cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.arr,                              /* 2022/09/26 MCCM初版 MOD */
                                cmBTpmdfBDto.point_shuse_data.kaiin_no.arr);
                        APLOG_WT("904", 0, null, "UPDATE",
                                sqlca.sqlcode,
                                "MS家族制度情報", out_format_buf, 0, 0);
                        return C_const_NG;
                    }
                    // mskz_up_flg = 1;                                                                   /* 2022/09/26 MCCM初版 MOD */
                    mskz_up_rec_cnt.arr++;
                }

                /* 日別追加、年別更新、累計更新を行う */
                rtn_cd = InsertDayPoint(in_rec_cnt_buf,
                                    hsph_in_rec_cnt_buf,
                                    tspn_up_rec_cnt_buf,
                                    tspr_up_rec_cnt_buf,
                                    tskp_up_rec_cnt_buf,
                                    kokyaku_no,
                                    kojin_honnen_rank_cd.arr,
                                    kojin_tougetu_rank_cd.arr,
                                    kazoku_honnen_rank_cd.arr,
                                    kazoku_tougetu_rank_cd.arr,
                                    kazoku_id.arr,
                                    check_over_ym);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("903", 0, null,
                            "InsertDayPoint", rtn_cd, 0, 0, 0, 0);
                    return (C_const_NG);
                }

                cmBTpmdfBDto.point_shuse_data.kaiage_nissu_koshin_mae =
                        cmBTpmdfBDto.tonen_point_data.nenkan_kaiage_nissu;
                cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_koshin_mae =
                        cmBTpmdfBDto.tonen_point_data.nenkan_rankup_taisho_kingaku;
                /*point_shuse_data.riyo_kano_point_koshin_mae =0;*/

                /* ホスト変数に設定 */
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_zennen.arr
                        = check_over_ym.tujo_point_kagenti_zennen;
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_tonen.arr
                        = check_over_ym.tujo_point_kagenti_tonen;
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month0.arr
                        = check_over_ym.kikan_point_kagenti_month0;
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month1.arr
                        = check_over_ym.kikan_point_kagenti_month1;
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month2.arr
                        = check_over_ym.kikan_point_kagenti_month2;
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month3.arr
                        = check_over_ym.kikan_point_kagenti_month3;

                /* TS利用可能ポイント情報更新 */
                rtn_cd = TsRiyoKanoPointUpdate(in_point_shusei.uard_flg);                                /* 2022/10/04 MCCM初版 MOD */
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("912", 0, null,
                            "TS利用可能ポイント情報更新に失敗しました",
                            0, 0, 0, 0, 0);
                    return (C_const_NG);
                }

                /* TS利用可能ポイント情報更新レコード件数カウントアップ */
                tspu_up_rec_cnt_buf++;

                /* ポイント修正結果出力 */
                rtn_cd = cmBTpmdfB_write_file();
                if (rtn_cd != C_const_OK) {
                    return (C_const_NG);
                }

                /* 正常件数カウントアップ、次のレコードを読込み */
                ok_rec_cnt_buf++;
                /*total_cnt_buf += point_shuse_data.riyo_kano_point_kagenti;*/

                // EXEC SQL COMMIT WORK;
                sqlca.commit();
            }
        }

        in_rec_cnt.arr = in_rec_cnt_buf.arr;
        ok_rec_cnt.arr = ok_rec_cnt_buf;
        total_cnt.arr  = total_cnt_buf;
        ng_rec_cnt.arr = ng_rec_cnt_buf;
        hsph_in_rec_cnt.arr = hsph_in_rec_cnt_buf.arr;
        tspn_up_rec_cnt.arr = tspn_up_rec_cnt_buf.arr;
        tspr_up_rec_cnt.arr = tspr_up_rec_cnt_buf.arr;
        tspu_up_rec_cnt.arr = tspu_up_rec_cnt_buf;
        /* 2021/02/17 TS期間限定ポイント情報 Start*/
        tskp_up_rec_cnt.arr = tskp_up_rec_cnt_buf.arr;
        /* 2021/02/17 TS期間限定ポイント情報 End*/

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetUid                                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetUid()                                                      */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               UID取得処理                                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int             kigyo_cd      ：企業コード                            */
    /*      char       *    kaiin_no      ：会員番号                              */
    /*      char       *    kokyaku_no    ：顧客番号                              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int GetUid(int kigyo_cd, StringDto kaiin_no, StringDto kokyaku_no) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("UID取得処理");
            /*---------------------------------------------------------------------*/
        }


        strcpy(cmBTpmdfBDto.ms_card_data.kaiin_no, kaiin_no);
        cmBTpmdfBDto.ms_card_data.kaiin_no.len = strlen(cmBTpmdfBDto.ms_card_data.kaiin_no);
        cmBTpmdfBDto.ms_card_data.kigyo_cd.arr = kigyo_cd;

        /* Uid取得処理 */
        /*EXEC SQL SELECT to_char(NVL(顧客番号,0), 'FM000000000000000' ),
        NVL(企業コード, 0),
                NVL(旧販社コード, 0),
                サービス種別                    *//* 2022/10/03 MCCM初版 ADD *//*
        INTO :ms_card_data.kokyaku_no,
                    :ms_card_data.kigyo_cd,
                    :ms_card_data.kyu_hansya_cd,
                    :ms_card_data.service_shubetsu  *//* 2022/10/03 MCCM初版 ADD *//*
        FROM MSカード情報
        WHERE 会員番号 = :ms_card_data.kaiin_no
        AND 企業コード = :ms_card_data.kigyo_cd;*/
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT to_char(NVL(顧客番号,0), 'FM000000000000000' ),\n" +
                "        NVL(企業コード, 0),\n" +
                "                NVL(旧販社コード, 0),\n" +
                "                サービス種別        \n" +             //* 2022/10/03 MCCM初版 ADD *//
                "        FROM MSカード情報\n" +
                "        WHERE 会員番号 = ?\n" +
                "        AND 企業コード = ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTpmdfBDto.ms_card_data.kaiin_no, cmBTpmdfBDto.ms_card_data.kigyo_cd);
        sqlca.fetch();
        sqlca.recData(cmBTpmdfBDto.ms_card_data.kokyaku_no, cmBTpmdfBDto.ms_card_data.kigyo_cd, cmBTpmdfBDto.ms_card_data.kyu_hansya_cd, cmBTpmdfBDto.ms_card_data.service_shubetsu);


        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "会員番号=%s",
                    cmBTpmdfBDto.ms_card_data.kaiin_no.arr);
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = 21;
            return C_const_OK;
        }

        /* 顧客番号が０の場合 */
        if (cmBTpmdfBDto.ms_card_data.kokyaku_no.strVal().startsWith("0") && cmBTpmdfBDto.ms_card_data.kokyaku_no.len == 1) {
            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = 21;
        }else{
            memcpy(kokyaku_no, cmBTpmdfBDto.ms_card_data.kokyaku_no, cmBTpmdfBDto.ms_card_data.kokyaku_no.len);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** kokyaku_no[%s]\n", kokyaku_no);
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： CheckTenpo                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  CheckTenpo()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               依頼店舗情報取得処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int             kigyo_cd      ：企業コード                            */
    /*      int             tenpo_cd      ：店舗コード                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int CheckTenpo(int kigyo_cd, int tenpo_cd) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("依頼店舗情報取得処理");
        }
        cmBTpmdfBDto.ps_mise_hyoji_date.kigyo_cd.arr = kigyo_cd;
        cmBTpmdfBDto.ps_mise_hyoji_date.mise_no.arr = tenpo_cd;
        cmBTpmdfBDto.h_bat_date.arr = atoi( bat_date );

        cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd.arr = 0;
        cmBTpmdfBDto.ps_mise_hyoji_mcc_data.mise_no.arr = 0;
        /*******************************************************************************/
        /* 会社コードと店番号の取得 */
        /*EXEC SQL SELECT DISTINCT
        会社コード,
                店番号
        INTO :ps_mise_hyoji_mcc_data.kaisha_cd,
                    :ps_mise_hyoji_mcc_data.mise_no
        FROM PS店表示情報ＭＣＣ
        WHERE 会社コード = :ps_mise_hyoji_date.kigyo_cd
        AND 店番号 = :ps_mise_hyoji_date.mise_no
        AND 開始年月日 <= :h_bat_date
        AND 終了年月日 >= :h_bat_date;*/
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT DISTINCT\n" +
                "        会社コード,\n" +
                "        店番号\n" +
                "        FROM PS店表示情報ＭＣＣ\n" +
                "        WHERE 会社コード = ?\n" +
                "        AND 店番号 = ?\n" +
                "        AND 開始年月日 <= ?\n" +
                "        AND 終了年月日 >= ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTpmdfBDto.ps_mise_hyoji_date.kigyo_cd, cmBTpmdfBDto.ps_mise_hyoji_date.mise_no, cmBTpmdfBDto.h_bat_date, cmBTpmdfBDto.h_bat_date);
        sqlca.fetch();
        sqlca.recData(cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd, cmBTpmdfBDto.ps_mise_hyoji_mcc_data.mise_no);

        /* データ無し以外エラーの場合処理を異常終了する */
        if ( sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
            /* DBERR */
            sprintf( out_format_buf, "会社コード=%d,店番号=%d",
                    cmBTpmdfBDto.point_shuse_data.irai_kigyo.intVal(), cmBTpmdfBDto.point_shuse_data.irai_tenpo.intVal());
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "PS店表示情報ＭＣＣ", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }else if( sqlca.sqlcode == C_const_Ora_NOTFOUND ){

            /* 店舗情報取得処理 */
            /*EXEC SQL SELECT DISTINCT NVL(連携用店番号, 0)
            INTO :ps_mise_hyoji_mcc_data.mise_no
            FROM PS店表示情報
            WHERE 店番号 = :ps_mise_hyoji_date.mise_no
            AND :h_bat_date BETWEEN 開始年月日 and 終了年月日;*/
            workSql.arr = "SELECT DISTINCT NVL(連携用店番号, 0)\n" +
                    "            FROM PS店表示情報\n" +
                    "            WHERE 店番号 = ?\n" +
                    "            AND ? BETWEEN 開始年月日 and 終了年月日";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTpmdfBDto.ps_mise_hyoji_date.mise_no, cmBTpmdfBDto.h_bat_date);
            sqlca.fetch();
            sqlca.recData(cmBTpmdfBDto.ps_mise_hyoji_mcc_data.mise_no);

            /* データ無し以外エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf( out_format_buf, "企業コード=%d, 店番号=%d,処理日付=%d",
                        cmBTpmdfBDto.ps_mise_hyoji_date.kigyo_cd.intVal(),
                        cmBTpmdfBDto.ps_mise_hyoji_date.mise_no.intVal(),
                        cmBTpmdfBDto.h_bat_date.intVal());
                APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                        "PS店表示情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return C_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = 20;
                return C_const_OK;
            }
            cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd.arr = 2500;
        }
        if (DBG_LOG) {
            C_DbgMsg("*** mise_no[%d]\n", tenpo_cd);
            C_DbgMsg("*** 会社コード[%d]\n", cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd.intVal());
            C_DbgMsg("*** 店番号[%d]\n", cmBTpmdfBDto.ps_mise_hyoji_mcc_data.mise_no.intVal());
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetKokyakuTeikeiRank                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetKokyakuTeikeiRank()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客名称、本年ランクコード取得処理                           */
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
    public int GetKokyakuTeikeiRank(StringDto kokyaku_no, StringDto kokyaku_name, IntegerDto kojin_honnen_rank_cd, IntegerDto kojin_tougetu_rank_cd, IntegerDto kazoku_honnen_rank_cd, IntegerDto kazoku_tougetu_rank_cd, IntegerDto kazoku_id) {
        int     rtn_cd;               /* 関数戻り値                      */
        int     thisyear_1char_buf;   /** 当年の下１桁                  **/
        IntegerDto     rtn_status = new IntegerDto();           /* 関数ステータス                        */
        int     this_date;
        int     this_year;
        int     this_month;
        /*
            memset(&csMaster, 0x00, sizeof(csMaster));
            strcpy((char*)csMaster.kokyaku_no.arr, kokyaku_no);
            csMaster.kokyaku_no.len = strlen((char*)csMaster.kokyaku_no.arr);
            rtn_cd = C_GetCmMaster(&csMaster, &rtn_status);
            if (rtn_cd == C_const_NG)
            {
                APLOG_WT( "903", 0, NULL,
                          "C_GetCmMaster" , (char *)(long)rtn_cd, (char *)(long)rtn_status, 0, 0, 0);
                return( C_const_NG ) ;
            } else if (rtn_cd == C_const_NOTEXISTS) {
                point_shuse_data.shori_kekka = 21;
                return(C_const_OK);
            }
        */
                /* 顧客名称取得 */
        /*
            strcpy((char*)kokyaku_name, (char*)csMaster.kokyaku_mesho);
            BT_Rtrim( kokyaku_name, strlen((char*)kokyaku_name) );
        */

        /* 2022/09/27 MCCM初版 ADD START */
        memset(cmBTpmdfBDto.h_ts_rank_info_data, 0x00, sizeof(cmBTpmdfBDto.h_ts_rank_info_data));
        strcpy(cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no, kokyaku_no);
        cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no.len = strlen(cmBTpmdfBDto.h_ts_rank_info_data.kokyaku_no);

        /* TSランク情報取得 */
        rtn_cd = GetTsRankInfo(cmBTpmdfBDto.h_ts_rank_info_data, rtn_status);
        if( rtn_cd == C_const_NG ) {
            APLOG_WT( "903", 0, null,
                    "GetTsRankInfo", rtn_cd, 0, 0, 0, 0);

            return(C_const_NG);
        }
        /* 2022/09/27 MCCM初版 ADD END */

        memset(cmBTpmdfBDto.kokyaku_seido_data, 0x00, sizeof(cmBTpmdfBDto.kokyaku_seido_data));
        strcpy(cmBTpmdfBDto.kokyaku_seido_data.kokyaku_no, kokyaku_no);
        cmBTpmdfBDto.kokyaku_seido_data.kokyaku_no.len = strlen(cmBTpmdfBDto.kokyaku_seido_data.kokyaku_no);

        /* 顧客制度情報取得 */
        rtn_cd = cmBTfuncBImpl.C_GetCsMaster(cmBTpmdfBDto.kokyaku_seido_data, rtn_status);
        if( rtn_cd == C_const_NG ) {
            APLOG_WT( "903", 0, null,
                    "C_GetCsMaster", rtn_cd, 0, 0, 0, 0);

            return(C_const_NG);

        }else if( rtn_cd == C_const_NOTEXISTS ) {
            if (DBG_LOG) {
                C_DbgMsg("*** C_GetCsMaster *** 顧客制度情報データなし%s\n", "");
            }

            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = 21;
            return(C_const_OK);
        }

        /* 本年ランクコード取得 */
        this_date  = atoi(bat_date);              /* 当年の日付の取得(YYYYMMDD) */
        /* 当年の下１桁の取得         */
        thisyear_1char_buf = (this_date / 10000) % 10;

        switch (thisyear_1char_buf) {
            case 0:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_0.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 1:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_1.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 2:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_2.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 3:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_3.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 4:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_4.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 5:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_5.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 6:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_6.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 7:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_7.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 8:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_8.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
            case 9:
                kojin_honnen_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_9.intVal();                      /* 2022/09/27 MCCM初版 MOD */
                break;
        }

        this_year = (this_date / 10000);
        this_month = (this_date / 100) % 100;

        /* 今月ランクコード取得 */
        if ( ( this_year % 2 ) == 0 )
        {
            /* 偶数年 */
            switch (this_month) {
                case 1:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_001.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 2:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_002.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 3:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_003.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 4:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_004.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 5:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_005.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 6:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_006.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 7:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_007.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 8:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_008.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 9:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_009.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 10:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_010.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 11:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_011.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 12:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_012.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
            }

        }
        else
        {
            /* 奇数年 */
            switch (this_month) {
                case 1:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_101.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 2:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_102.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 3:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_103.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 4:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_104.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 5:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_105.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 6:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_106.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 7:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_107.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 8:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_108.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 9:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_109.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 10:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_110.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 11:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_111.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
                case 12:
                    kojin_tougetu_rank_cd.arr = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_112.intVal();                       /* 2022/09/27 MCCM初版 MOD */
                    break;
            }
        }

        if (DBG_LOG) {
            C_DbgMsg("本年ランクコード[%d]\n", kojin_honnen_rank_cd);
            C_DbgMsg("当月ランクコード[%d]\n", kojin_tougetu_rank_cd);
        }

        if (DBG_LOG) {
            C_DbgMsg("kokyaku_seido_data.kazoku_id[%s]\n", cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.arr);           /* 2022/09/26 MCCM初版 MOD */
        }
        kazoku_honnen_rank_cd.arr = 0;
        kazoku_tougetu_rank_cd.arr = 0;
        if (atol(cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.arr) > 0) {
            rtn_cd = cmBTfuncBImpl.C_GetFamilyRank((int) atol(cmBTpmdfBDto.kokyaku_seido_data.kazoku_id),                     /* 2022/09/26 MCCM初版 MOD */
            kazoku_honnen_rank_cd, kazoku_tougetu_rank_cd, rtn_status);
            if( rtn_cd != C_const_OK ) {
                APLOG_WT( "903", 0, null,
                        "C_GetFamilyRank", rtn_cd,
                        rtn_status, 0, 0, 0);
                return(C_const_NG);
            }
            if (DBG_LOG) {
                C_DbgMsg("家族本年ランクコード[%d]\n", kazoku_honnen_rank_cd);
                C_DbgMsg("家族当月ランクコード[%d]\n", kazoku_tougetu_rank_cd);
            }
        }

        /* 家族ＩＤ */
        kazoku_id.arr = (int) atol(cmBTpmdfBDto.kokyaku_seido_data.kazoku_id.arr);                                     /* 2022/09/26 MCCM初版 MOD */
        if (DBG_LOG) {
            C_DbgMsg("家族ＩＤ[%d]\n", kazoku_id);
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_InsertDayPoint                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_InsertDayPoint(DAY_POINT_DATA  *dayPointData, int *date,        */
    /*                           int *status)                                     */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ポイント日別情報追加処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              DAY_POINT_DATA * dayPointData ： ポイント日別情報構造体取得   */
    /*                                               パラメータ                   */
    /*              int           date   ： 更新対象テーブル日付(YYYYMMDD)        */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int InsertDayPoint(IntegerDto in_rec_cnt, IntegerDto hsph_in_rec_cnt, IntegerDto tspn_up_rec_cnt, IntegerDto tspr_up_rec_cnt, IntegerDto tskp_up_rec_cnt, StringDto kokyaku_no, int kojin_honnen_rank_cd, int kojin_tougetu_rank_cd, int kazoku_honnen_rank_cd, int kazoku_tougetu_rank_cd, int kazoku_id, CmBTpmdfBServiceImpl.CHECK_OVER_YM check_over_ym) {
        int     rtn_cd;             /* 関数戻り値                           */
        StringDto sys_date = new StringDto(9);
        StringDto  sys_time = new StringDto(7);
        IntegerDto     rtn_status = new IntegerDto();         /* 関数ステータス                        */
        /*    int     year_buf;           *//* 対象年                                */
        int     in_rec_cnt_buf;
        int     hsph_in_rec_cnt_buf;
        int     tspn_up_rec_cnt_buf;
        int     tspr_up_rec_cnt_buf;
        int     max_rank;
        int     this_date;
        int     this_month;
        /* 2021/02/17 TS期間限定ポイント情報追加 Start */
        int     tskp_up_rec_cnt_buf;
        /* 2021/02/17 TS期間限定ポイント情報追加 End */
        /* 2022/10/03 MCCM初版 ADD START */
        int      meisaisu = 0;   /* 明細数 */
        /* 2022/10/03 MCCM初版 ADD END */

//    char    wk_taisho_nen[5];
//    char    wk_year_buf[5];


        in_rec_cnt_buf = in_rec_cnt.arr;
        hsph_in_rec_cnt_buf = hsph_in_rec_cnt.arr;
        tspn_up_rec_cnt_buf = tspn_up_rec_cnt.arr;
        tspr_up_rec_cnt_buf = tspr_up_rec_cnt.arr;
        /* 2021/02/17 TS期間限定ポイント情報追加 Start */
        tskp_up_rec_cnt_buf = tskp_up_rec_cnt.arr;
        /* 2021/02/17 TS期間限定ポイント情報追加 End */

        this_date = atoi( bat_date );
        this_month = (this_date / 100) % 100;

        /* ポイント日別情報構造体の編集 */
        memset(cmBTpmdfBDto.honjitsu_point_data, 0x00, sizeof(cmBTpmdfBDto.honjitsu_point_data));

        /* 2022/12/28 MCCM初版 DEL START */
        /* 更新前月間ランクＵＰ対象金額(ポイント日別) */
        /* 月間ランクＵＰ対象金額(ポイント年別) */
//        switch( this_month ) {
//         case 1:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_01;
//             point_year_data.gekkan_rankup_taisho_kingaku_01 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 2:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_02;
//             point_year_data.gekkan_rankup_taisho_kingaku_02 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 3:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_03;
//             point_year_data.gekkan_rankup_taisho_kingaku_03 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 4:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_04;
//             point_year_data.gekkan_rankup_taisho_kingaku_04 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 5:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_05;
//             point_year_data.gekkan_rankup_taisho_kingaku_05 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 6:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_06;
//             point_year_data.gekkan_rankup_taisho_kingaku_06 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 7:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_07;
//             point_year_data.gekkan_rankup_taisho_kingaku_07 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 8:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_08;
//             point_year_data.gekkan_rankup_taisho_kingaku_08 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 9:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_09;
//             point_year_data.gekkan_rankup_taisho_kingaku_09 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 10:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_10;
//             point_year_data.gekkan_rankup_taisho_kingaku_10 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 11:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_11;
//             point_year_data.gekkan_rankup_taisho_kingaku_11 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//         case 12:
//             honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tonen_point_data.gekkan_rankup_taisho_kingaku_12;
//             point_year_data.gekkan_rankup_taisho_kingaku_12 = point_shuse_data.rankup_taisho_kingaku_kagenti;
//             break;
//        }
        /* 2022/12/28 MCCM初版 DEL END */
        /* 2022/12/28 MCCM初版 MOD START */
        /* 更新前月間ランクＵＰ対象金額(ポイント日別) */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku.arr = gekkan_rankup_taisho_kingaku_old;
        /* 更新前年間ランクＵＰ対象金額 */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_nenkan_kojin_rankup_taisho_kingaku.arr = nenkan_rankup_taisho_kingaku_old;
        /* 2022/12/28 MCCM初版 MOD END */

        cmBTpmdfBDto.honjitsu_point_data.system_ymd.arr = atoi(bat_date);
        strcpy(cmBTpmdfBDto.honjitsu_point_data.kokyaku_no, kokyaku_no); /* 顧客番号   */
        cmBTpmdfBDto.honjitsu_point_data.kokyaku_no.len = strlen(cmBTpmdfBDto.honjitsu_point_data.kokyaku_no);
        cmBTpmdfBDto.honjitsu_point_data.shori_seq.arr = in_rec_cnt_buf;     /* 処理通番     */
        cmBTpmdfBDto.honjitsu_point_data.kaiin_kigyo_cd = cmBTpmdfBDto.ms_card_data.kigyo_cd;    /* 会員企業コード */
        cmBTpmdfBDto.honjitsu_point_data.kaiin_kyu_hansya_cd = cmBTpmdfBDto.ms_card_data.kyu_hansya_cd;    /* 会員旧販社コード */
        /* 会員番号         */
        memcpy(cmBTpmdfBDto.honjitsu_point_data.kaiin_no, cmBTpmdfBDto.point_shuse_data.kaiin_no,
                cmBTpmdfBDto.point_shuse_data.kaiin_no.len);
        cmBTpmdfBDto.honjitsu_point_data.kaiin_no.len = cmBTpmdfBDto.point_shuse_data.kaiin_no.len;
        /* 2022/09/26 MCCM初版 MOD START */
        cmBTpmdfBDto.honjitsu_point_data.nyukai_kaisha_cd_mcc = cmBTpmdfBDto.h_ts.nyukai_kigyo_cd;   /* 入会会社コードＭＣＣ */
        cmBTpmdfBDto.honjitsu_point_data.nyukai_tenpo_mcc = cmBTpmdfBDto.h_ts.nyukai_tenpo;   /* 入会店舗ＭＣＣ */
//      honjitsu_point_data.hakken_kigyo_cd = h_ts.hakken_kigyo_cd;   /* 発券企業コード */
//      honjitsu_point_data.hakken_tenpo = h_ts.hakken_tenpo;    /* 発券店舗         */
        /* 2022/09/26 MCCM初版 MOD END */
        cmBTpmdfBDto.honjitsu_point_data.seisan_ymd.arr = 0;    /* 精算年月日         */
        cmBTpmdfBDto.honjitsu_point_data.toroku_ymd = cmBTpmdfBDto.point_shuse_data.data_ymd;    /* 登録年月日         */      /* 2022/09/26 MCCM初版 MOD */
        cmBTpmdfBDto.honjitsu_point_data.data_ymd = cmBTpmdfBDto.point_shuse_data.data_ymd;    /* データ年月日     */
        if((cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd.intVal()) == 2500
                && ( (cmBTpmdfBDto.point_shuse_data.irai_tenpo.intVal() >= 100000)
                && (cmBTpmdfBDto.point_shuse_data.irai_tenpo.intVal() <= 999999) )){                         /* 2022/10/03 MCCM初版 MOD */
            cmBTpmdfBDto.honjitsu_point_data.kigyo_cd = cmBTpmdfBDto.point_shuse_data.irai_kigyo;   /* 企業コード */
            cmBTpmdfBDto.honjitsu_point_data.mise_no = cmBTpmdfBDto.point_shuse_data.irai_tenpo; /* 店番号           */
        }                                                                                            /* 2022/10/03 MCCM初版 MOD */
        cmBTpmdfBDto.honjitsu_point_data.terminal_no.arr = 0;                /* ターミナル番号   */
        cmBTpmdfBDto.honjitsu_point_data.torihiki_no.arr = 0;                /* 取引番号         */
        rtn_cd = C_GetSysDateTime( sys_date, sys_time );  /* 時刻             */
        if (rtn_cd != C_const_OK) {
            APLOG_WT( "903", 0, null,
                    "C_GetSysDateTime" , rtn_cd, 0, 0, 0, 0);
            return( C_const_NG ) ;
        }
        cmBTpmdfBDto.honjitsu_point_data.jikoku_hms.arr = atoi(sys_time);    /* 時刻             */
        cmBTpmdfBDto.honjitsu_point_data.riyu_cd.arr = cmBTpmdfBDto.point_shuse_data.riyu_code.intVal() + 1100; /* 理由コード       */
        if (cmBTpmdfBDto.point_shuse_data.riyu_code.intVal() == 1011) {
            cmBTpmdfBDto.honjitsu_point_data.riyu_cd = cmBTpmdfBDto.point_shuse_data.riyu_code;
        }
        cmBTpmdfBDto.honjitsu_point_data.card_nyuryoku_kbn.arr = 2;          /* カード入力区分   */
        cmBTpmdfBDto.honjitsu_point_data.shori_taisho_file_record_no.arr = 0; /* 処理対象ファイルレコード */
        cmBTpmdfBDto.honjitsu_point_data.real_koshin_flg.arr = 0;            /* リアル更新フラグ */
        cmBTpmdfBDto.honjitsu_point_data.fuyo_point.arr                   /* 付与ポイント     */
                = in_point_shusei.tujo_point_kagenti_zennen
                + in_point_shusei.tujo_point_kagenti_tonen
                + in_point_shusei.kikan_point_kagenti_month0
                + in_point_shusei.kikan_point_kagenti_month1
                + in_point_shusei.kikan_point_kagenti_month2
                + in_point_shusei.kikan_point_kagenti_month3;
        /* point_shuse_data.riyo_kano_point_kagenti; */

        cmBTpmdfBDto.honjitsu_point_data.riyo_point.arr = 0;                 /* 利用ポイント     */
        /* 基本Ｐ率対象ポイント */
        cmBTpmdfBDto.honjitsu_point_data.kihon_pritsu_taisho_point.arr = 0;
        /* ランクＵＰ対象金額   */
        cmBTpmdfBDto.honjitsu_point_data.rankup_taisho_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        /* ポイント対象金額 */
        cmBTpmdfBDto.honjitsu_point_data.point_taisho_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        cmBTpmdfBDto.honjitsu_point_data.service_hakko_maisu.arr = 0;      /* サービス券発行枚数 */
        cmBTpmdfBDto.honjitsu_point_data.service_riyo_maisu.arr = 0;       /* サービス券利用枚数 */
        cmBTpmdfBDto.honjitsu_point_data.kojin_getuji_rank_cd.arr = kojin_tougetu_rank_cd;      /* 個人月次ランクコード */
        cmBTpmdfBDto.honjitsu_point_data.kojin_nenji_rank_cd.arr = kojin_honnen_rank_cd;        /* 個人年次ランクコード */
        cmBTpmdfBDto.honjitsu_point_data.kazoku_getuji_rank_cd.arr = kazoku_tougetu_rank_cd;    /* 家族月次ランクコード */
        cmBTpmdfBDto.honjitsu_point_data.kazoku_nenji_rank_cd.arr = kazoku_honnen_rank_cd;      /* 家族年次ランクコード */

        max_rank = kojin_tougetu_rank_cd;
        if (kojin_honnen_rank_cd > max_rank) {
            max_rank = kojin_honnen_rank_cd;
        }
        if (kazoku_tougetu_rank_cd > max_rank) {
            max_rank = kazoku_tougetu_rank_cd;
        }
        if (kazoku_honnen_rank_cd > max_rank) {
            max_rank = kazoku_honnen_rank_cd;
        }
        cmBTpmdfBDto.honjitsu_point_data.shiyo_rank_cd.arr = max_rank;   /* 使用ランクコード */
        /* 買上額           */
        cmBTpmdfBDto.honjitsu_point_data.kaiage_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        cmBTpmdfBDto.honjitsu_point_data.kaiage_cnt                    /* 買上回数         */
                =  cmBTpmdfBDto.tonen_point_data.nenkan_kaiage_cnt;
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_point.arr/* 更新前利用可能ポイント*/
                =  cmBTpmdfBDto.h_ts.zennen_p.intVal() +  cmBTpmdfBDto.h_ts.tonen_p.intVal() +  cmBTpmdfBDto.h_ts.yokunen_p.intVal()
                +  cmBTpmdfBDto.h_ts.kikan_p01.intVal() +  cmBTpmdfBDto.h_ts.kikan_p02.intVal() +  cmBTpmdfBDto.h_ts.kikan_p03.intVal()
                +  cmBTpmdfBDto.h_ts.kikan_p04.intVal() +  cmBTpmdfBDto.h_ts.kikan_p05.intVal();

        this_date  = atoi(bat_date);              /* 当年の日付の取得(YYYYMMDD) */
        this_month = (this_date / 100) % 100;

        //honjitsu_point_data.koshinmae_nenkan_kojin_rankup_taisho_kingaku =                                                    /* 2022/12/28 MCCM初版 DEL */
        //   tonen_point_data.nenkan_rankup_taisho_kingaku;   /* 更新前年間ランクＵＰ対象金額 */                                /* 2022/12/28 MCCM初版 DEL */

        sprintf(cmBTpmdfBDto.honjitsu_point_data.kazoku_id, "%d", kazoku_id);        /* 家族ＩＤ         */                       /* 2022/10/03 MCCM初版 MOD */
        cmBTpmdfBDto.honjitsu_point_data.sagyosha_id.arr = 0;              /* 作業者ＩＤ       */
        cmBTpmdfBDto.honjitsu_point_data.real_koshin_ymd.arr = 0;          /* リアル更新年月日 */
        cmBTpmdfBDto.honjitsu_point_data.real_koshin_apl_version.arr = 0x00; /* リアル更新ＡＰＬバージョン       */
        cmBTpmdfBDto.honjitsu_point_data.delay_koshin_ymd.arr = 0;     /* ディレイ更新年月日     */
        strcpy(cmBTpmdfBDto.honjitsu_point_data.delay_koshin_apl_version, Cg_Program_Name_Ver);
        /* ディレイ更新ＡＰＬバージョン       */
        cmBTpmdfBDto.honjitsu_point_data.sosai_flg.arr = 0;                /* 相殺フラグ       */
        cmBTpmdfBDto.honjitsu_point_data.mesai_check_kbn.arr = 0;          /* 明細チェック区分 */
        cmBTpmdfBDto.honjitsu_point_data.sagyo_kigyo_cd.arr = 0;           /* 作業企業コード   */
        cmBTpmdfBDto.honjitsu_point_data.sagyosha_id.arr = 0;              /* 作業者ＩＤ       */
        cmBTpmdfBDto.honjitsu_point_data.sagyo_ymd.arr = 0;                /* 作業年月日       */
        cmBTpmdfBDto.honjitsu_point_data.sagyo_hms.arr = 0;                /* 作業時刻         */
        cmBTpmdfBDto.honjitsu_point_data.batch_koshin_ymd.arr = atoi(bat_date);  /* バッチ更新日     */
        cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_ymd.arr = atoi(bat_date); /* 最終更新日       */
        cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_ymdhms.arr = 0;       /* 最終更新日時     */
        /* 最終更新プログラムID */
        strcpy(cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_programid, Cg_Program_Name);
        /* 2021/01/21 NDBS.緒方 : 年度管理変更/期間限定ポイント追加対応 */
        /* 更新前利用可能通常Ｐ基準年度   */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_kijun_nendo.arr
                = check_over_ym.kijun_nendo;

        /* 更新前利用可能通常Ｐ前年度     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_zennendo
                = cmBTpmdfBDto.h_ts.zennen_p;

        /* 更新前利用可能通常Ｐ当年度     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_tonendo
                = cmBTpmdfBDto.h_ts.tonen_p;

        /* 更新前利用可能通常Ｐ翌年度     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_yokunendo
                = cmBTpmdfBDto.h_ts.yokunen_p;

        /* 要求付与通常Ｐ                 */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_point.arr
                = in_point_shusei.tujo_point_kagenti_zennen
                + in_point_shusei.tujo_point_kagenti_tonen;

        /* 要求付与通常Ｐ基準年度         */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_point_kijun_nendo.arr
                = in_point_shusei.tujo_point_kijun_nendo;

        /* 要求付与通常Ｐ前年度           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_poin_zennendo.arr
                = in_point_shusei.tujo_point_kagenti_zennen;

        /* 要求付与通常Ｐ当年度           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_poin_tonendo.arr
                = in_point_shusei.tujo_point_kagenti_tonen;

        /* 要求利用通常Ｐ基準年度         */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point_kijun_nendo.arr
                = in_point_shusei.tujo_point_kijun_nendo;

        /* 更新付与通常Ｐ                 */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point.arr
                = check_over_ym.tujo_point_kagenti_zennen
                + check_over_ym.tujo_point_kagenti_tonen;

        /* 更新付与通常Ｐ基準年度         */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_kijun_nendo.arr
                = check_over_ym.kijun_nendo;

        /* 更新付与通常Ｐ前年度           */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_zennendo.arr
                = check_over_ym.tujo_point_kagenti_zennen;

        /* 更新付与通常Ｐ当年度           */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_tonendo.arr
                = check_over_ym.tujo_point_kagenti_tonen;

        /* 更新利用通常Ｐ基準年度         */
        cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_kijun_nendo.arr
                = check_over_ym.kijun_nendo;

        /* 更新前期間限定Ｐ基準月         */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_kikan_gentei_point_kijun_month.arr
                = check_over_ym.kijun_month;

        /* 更新前利用可能期間限定Ｐ０     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point0
                = cmBTpmdfBDto.h_ts.kikan_p01;

        /* 更新前利用可能期間限定Ｐ１     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point1
                = cmBTpmdfBDto.h_ts.kikan_p02;

        /* 更新前利用可能期間限定Ｐ２     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point2
                = cmBTpmdfBDto.h_ts.kikan_p03;

        /* 更新前利用可能期間限定Ｐ３     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point3
                = cmBTpmdfBDto.h_ts.kikan_p04;

        /* 更新前利用可能期間限定Ｐ４     */
        cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point4
                = cmBTpmdfBDto.h_ts.kikan_p05;

        /* 要求付与期間限定Ｐ             */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point.arr
                = in_point_shusei.kikan_point_kagenti_month0
                + in_point_shusei.kikan_point_kagenti_month1
                + in_point_shusei.kikan_point_kagenti_month2
                + in_point_shusei.kikan_point_kagenti_month3;

        /* 要求付与期間限定Ｐ基準月       */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point_kijun_month.arr
                = in_point_shusei.kikan_point_kijun_month;

        /* 要求付与期間限定Ｐ０           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point0.arr
                = in_point_shusei.kikan_point_kagenti_month0;

        /* 要求付与期間限定Ｐ１           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point1.arr
                = in_point_shusei.kikan_point_kagenti_month1;

        /* 要求付与期間限定Ｐ２           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point2.arr
                = in_point_shusei.kikan_point_kagenti_month2;

        /* 要求付与期間限定Ｐ３           */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point3.arr
                = in_point_shusei.kikan_point_kagenti_month3;

        /* 要求利用期間限定Ｐ基準月       */
        cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point_kijun_month.arr
                = in_point_shusei.kikan_point_kijun_month;

        /* 更新付与期間限定Ｐ             */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point.arr
                = check_over_ym.kikan_point_kagenti_month0
                + check_over_ym.kikan_point_kagenti_month1
                + check_over_ym.kikan_point_kagenti_month2
                + check_over_ym.kikan_point_kagenti_month3;

        /* 更新付与期間限定Ｐ基準月       */
        cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.arr
                = check_over_ym.kijun_month;

        /* 更新付与期間限定ＰＭＭ設定 */
        /* 当月 */
        cmBTpmdfB_setMonth(this_month,check_over_ym.kikan_point_kagenti_month0);

        /* １ヶ月後 */
        cmBTpmdfB_setMonth(this_month+1,
                check_over_ym.kikan_point_kagenti_month1);

        /* 2ヶ月後 */
        cmBTpmdfB_setMonth(this_month+2,
                check_over_ym.kikan_point_kagenti_month2);

        /* 3ヶ月後 */
        cmBTpmdfB_setMonth(this_month+3,
                check_over_ym.kikan_point_kagenti_month3);

        /* 更新利用期間限定Ｐ基準月       */
        cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point_kijun_month.arr
                = check_over_ym.kijun_month;

        /* 2022/10/03 MCCM初版 ADD START */
        /* 会社コードＭＣＣ */
        cmBTpmdfBDto.honjitsu_point_data.kaisha_cd_mcc = cmBTpmdfBDto.ps_mise_hyoji_mcc_data.kaisha_cd;
        /* 店番号ＭＣＣ */
        cmBTpmdfBDto.honjitsu_point_data.mise_no_mcc = cmBTpmdfBDto.ps_mise_hyoji_mcc_data.mise_no;

        /* カード種別 */
        rtn_cd = GetCardShubetsu();
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "912", 0, null,
                    "カード種別の取得に失敗しました" , 0, 0, 0, 0, 0);
            return( C_const_NG ) ;
        }
        cmBTpmdfBDto.honjitsu_point_data.card_syubetsu = cmBTpmdfBDto.h_card_shubetsu;

        /* 登録経路 */
        strcpy(cmBTpmdfBDto.honjitsu_point_data.touroku_keiro, "4");

        /* 取引区分 */
        //NULLので、設定しない。

        /* 明細数 */
        if(in_point_shusei.tujo_point_kagenti_tonen != 0)  {meisaisu++;}          /* 通常Ｐ加減値（当年） */
        if(in_point_shusei.tujo_point_kagenti_zennen != 0) {meisaisu++;}          /* 通常Ｐ加減値（前年） */
        if(in_point_shusei.kikan_point_kagenti_month0 !=0) {meisaisu++;}       /* 期間限定Ｐ加減値（当月）*/
        if(in_point_shusei.kikan_point_kagenti_month1 !=0) {meisaisu++;}    /* 期間限定Ｐ加減値（1ヶ月後）*/
        if(in_point_shusei.kikan_point_kagenti_month2 !=0) {meisaisu++;}    /* 期間限定Ｐ加減値（2ヶ月後）*/
        if(in_point_shusei.kikan_point_kagenti_month3 !=0) {meisaisu++;}    /* 期間限定Ｐ加減値（3ヶ月後）*/

        cmBTpmdfBDto.honjitsu_point_data.meisai_su.arr = meisaisu;

        /* 2022/10/03 MCCM初版 ADD END */
        /****************************************************************************************************************************/
        if (DBG_LOG) {
            C_DbgMsg("******************************ポイント日別情報を追加*******************************************%s\n", "");
            C_DbgMsg("システム年月日                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.system_ymd.intVal());
            C_DbgMsg("顧客番号                      [%s]\n", cmBTpmdfBDto.honjitsu_point_data.kokyaku_no.arr);
            C_DbgMsg("処理通番                      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.shori_seq.intVal());
            C_DbgMsg("会員企業コード                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kaiin_kigyo_cd.intVal());
            C_DbgMsg("会員旧販社コード              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kaiin_kyu_hansya_cd.intVal());
            C_DbgMsg("会員番号                      [%s]\n", cmBTpmdfBDto.honjitsu_point_data.kaiin_no.arr);
            C_DbgMsg("入会企業コード                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.nyukai_kigyo_cd.intVal());
            C_DbgMsg("入会店舗                      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.nyukai_tenpo.intVal());
            C_DbgMsg("発券企業コード                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.hakken_kigyo_cd.intVal());
            C_DbgMsg("発券店舗                      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.hakken_tenpo.intVal());
            C_DbgMsg("精算年月日                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.seisan_ymd.intVal());
            C_DbgMsg("登録年月日                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.toroku_ymd.intVal());
            C_DbgMsg("データ年月日                  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.data_ymd.intVal());
            C_DbgMsg("企業コード                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kigyo_cd.intVal());
            C_DbgMsg("店番号                        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mise_no.intVal());
            C_DbgMsg("ターミナル番号                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.terminal_no.intVal());
            C_DbgMsg("取引番号                      [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.torihiki_no.floatVal());
            C_DbgMsg("時刻                          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.jikoku_hms.intVal());
            C_DbgMsg("理由コード                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.riyu_cd.intVal());
            C_DbgMsg("サークルＩＤ                  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.circle_id.intVal());
            C_DbgMsg("カード入力区分                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.card_nyuryoku_kbn.intVal());
            C_DbgMsg("処理対象ファイルレコード番号  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.shori_taisho_file_record_no.intVal());
            C_DbgMsg("リアル更新フラグ              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.real_koshin_flg.intVal());
            C_DbgMsg("付与ポイント                  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.fuyo_point.floatVal());
            C_DbgMsg("利用ポイント                  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.riyo_point.floatVal());
            C_DbgMsg("基本Ｐ率対象ポイント          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.kihon_pritsu_taisho_point.floatVal());
            C_DbgMsg("ランクＵＰ対象金額            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.rankup_taisho_kingaku.floatVal());
            C_DbgMsg("ポイント対象金額              [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.point_taisho_kingaku.floatVal());
            C_DbgMsg("サービス券発行枚数            [%d]\n", cmBTpmdfBDto.honjitsu_point_data.service_hakko_maisu.intVal());
            C_DbgMsg("サービス券利用枚数            [%d]\n", cmBTpmdfBDto.honjitsu_point_data.service_riyo_maisu.intVal());
            C_DbgMsg("個人月次ランクコード          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kojin_getuji_rank_cd.intVal());
            C_DbgMsg("個人年次ランクコード          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kojin_nenji_rank_cd.intVal());
            C_DbgMsg("家族月次ランクコード          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kazoku_getuji_rank_cd.intVal());
            C_DbgMsg("家族年次ランクコード          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kazoku_nenji_rank_cd.intVal());
            C_DbgMsg("使用ランクコード              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.shiyo_rank_cd.intVal());
            C_DbgMsg("買上額                        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.kaiage_kingaku.floatVal());
            C_DbgMsg("買上回数                      [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.kaiage_cnt.floatVal());
            C_DbgMsg("更新前利用可能ポイント        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_point.floatVal());
            C_DbgMsg("更新前付与ポイント            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_fuyo_point.floatVal());
            C_DbgMsg("更新前基本Ｐ率対象ポイント    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_kihon_pritsu_taisho_point.floatVal());
            C_DbgMsg("更新前月間ランクＵＰ対象金額  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_gekkan_kojin_rankup_taisho_kingaku.floatVal());
            C_DbgMsg("更新前年間ランクＵＰ対象金額  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_nenkan_kojin_rankup_taisho_kingaku.floatVal());
            C_DbgMsg("更新前ポイント対象金額        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_point_taisho_kingaku.floatVal());
            C_DbgMsg("更新前買上額                  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_kaiage_kingaku.floatVal());
            C_DbgMsg("家族ＩＤ                      [%s]\n", cmBTpmdfBDto.honjitsu_point_data.kazoku_id.arr);
            C_DbgMsg("更新前月間家族ランクＵＰ金額  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_gekkan_kazoku_rankup_taisho_kingaku.floatVal());
            C_DbgMsg("更新前年間家族ランクＵＰ金額  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_nenkan_kazoku_rankup_taisho_kingaku.floatVal());
            C_DbgMsg("リアル更新日時                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.real_koshin_ymd.intVal());
            C_DbgMsg("ディレイ更新日時              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.delay_koshin_ymd.intVal());
            C_DbgMsg("相殺フラグ                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.sosai_flg.intVal());
            C_DbgMsg("明細チェックフラグ            [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mesai_check_flg.intVal());
            C_DbgMsg("明細チェック区分              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mesai_check_kbn.intVal());
            C_DbgMsg("作業企業コード                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.sagyo_kigyo_cd.intVal());
            C_DbgMsg("作業者ＩＤ                    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.sagyosha_id.floatVal());
            C_DbgMsg("作業年月日                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.sagyo_ymd.intVal());
            C_DbgMsg("作業時刻                      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.sagyo_hms.intVal());
            C_DbgMsg("バッチ更新日                  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.batch_koshin_ymd.intVal());
            C_DbgMsg("最終更新日                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_ymd.intVal());
            C_DbgMsg("最終更新日時                  [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_ymdhms.floatVal());
            C_DbgMsg("要求利用Ｐ内訳フラグ          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_putiwake_flg.intVal());
            C_DbgMsg("更新前利用可能通常Ｐ基準年度  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_kijun_nendo.intVal());
            C_DbgMsg("更新前利用可能通常Ｐ前年度    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_zennendo.floatVal());
            C_DbgMsg("更新前利用可能通常Ｐ当年度    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_tonendo.floatVal());
            C_DbgMsg("更新前利用可能通常Ｐ翌年度    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_tujo_point_yokunendo.floatVal());
            C_DbgMsg("要求付与通常Ｐ                [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_point.floatVal());
            C_DbgMsg("要求付与通常Ｐ基準年度        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_point_kijun_nendo.intVal());
            C_DbgMsg("要求付与通常Ｐ前年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_poin_zennendo.floatVal());
            C_DbgMsg("要求付与通常Ｐ当年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_tujo_poin_tonendo.floatVal());
            C_DbgMsg("要求利用通常Ｐ                [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point.floatVal());
            C_DbgMsg("要求利用通常Ｐ基準年度        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point_kijun_nendo.intVal());
            C_DbgMsg("要求利用通常Ｐ前年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point_zennendo.floatVal());
            C_DbgMsg("要求利用通常Ｐ当年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point_tonendo.floatVal());
            C_DbgMsg("要求利用通常Ｐ翌年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_tujo_point_yokunendo.floatVal());
            C_DbgMsg("更新付与通常Ｐ                [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point.floatVal());
            C_DbgMsg("更新付与通常Ｐ基準年度        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_kijun_nendo.intVal());
            C_DbgMsg("更新付与通常Ｐ前年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_zennendo.floatVal());
            C_DbgMsg("更新付与通常Ｐ当年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_tujo_point_tonendo.floatVal());
            C_DbgMsg("更新利用通常Ｐ                [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point.floatVal());
            C_DbgMsg("更新利用通常Ｐ基準年度        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_kijun_nendo.intVal());
            C_DbgMsg("更新利用通常Ｐ前年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_zennendo.floatVal());
            C_DbgMsg("更新利用通常Ｐ当年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_tonendo.floatVal());
            C_DbgMsg("更新利用通常Ｐ翌年度          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_yokunendo.floatVal());
            C_DbgMsg("更新前期間限定Ｐ基準月        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_kikan_gentei_point_kijun_month.intVal());
            C_DbgMsg("更新前利用可能期間限定Ｐ０    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point0.floatVal());
            C_DbgMsg("更新前利用可能期間限定Ｐ１    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point1.floatVal());
            C_DbgMsg("更新前利用可能期間限定Ｐ２    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point2.floatVal());
            C_DbgMsg("更新前利用可能期間限定Ｐ３    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point3.floatVal());
            C_DbgMsg("更新前利用可能期間限定Ｐ４    [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshinmae_riyo_kano_kikan_gentei_point4.floatVal());
            C_DbgMsg("要求付与期間限定Ｐ            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point.floatVal());
            C_DbgMsg("要求付与期間限定Ｐ基準月      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point_kijun_month.intVal());
            C_DbgMsg("要求付与期間限定Ｐ０          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point0.floatVal());
            C_DbgMsg("要求付与期間限定Ｐ１          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point1.floatVal());
            C_DbgMsg("要求付与期間限定Ｐ２          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point2.floatVal());
            C_DbgMsg("要求付与期間限定Ｐ３          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_fuyo_kikan_gentei_point3.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ基準月      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point_kijun_month.intVal());
            C_DbgMsg("要求利用期間限定Ｐ０          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point0.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ１          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point1.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ２          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point2.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ３          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point3.floatVal());
            C_DbgMsg("要求利用期間限定Ｐ４          [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.yokyu_riyo_kikan_gentei_point4.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ基準月      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal());
            C_DbgMsg("更新付与期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point01.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０２        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point02.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０３        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point03.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０４        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point04.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０５        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point05.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０６        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point06.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０７        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point07.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０８        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point08.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ０９        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point09.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ１０        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point10.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ１１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point11.floatVal());
            C_DbgMsg("更新付与期間限定Ｐ１２        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point12.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ            [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ基準月      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point_kijun_month.intVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point01.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point02.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point03.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point04.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point05.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point06.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point07.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point08.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point09.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point10.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point11.floatVal());
            C_DbgMsg("更新利用期間限定Ｐ０１        [%.0f]\n", cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_kikan_gentei_point12.floatVal());
            C_DbgMsg("店番号                        [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mk_mise_no.intVal());
            C_DbgMsg("取引番号                      [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mk_torihiki_no.intVal());
            C_DbgMsg("リアル更新ＡＰＬバージョン    [%s]\n", cmBTpmdfBDto.honjitsu_point_data.real_koshin_apl_version);
            C_DbgMsg("ディレイ更新ＡＰＬバージョン  [%s]\n", cmBTpmdfBDto.honjitsu_point_data.delay_koshin_apl_version);
            C_DbgMsg("最終更新プログラムＩＤ        [%s]\n", cmBTpmdfBDto.honjitsu_point_data.saishu_koshin_programid);
            C_DbgMsg("入会会社コードＭＣＣ          [%d]\n", cmBTpmdfBDto.honjitsu_point_data.nyukai_kaisha_cd_mcc.intVal());
            C_DbgMsg("入会店舗ＭＣＣ                [%d]\n", cmBTpmdfBDto.honjitsu_point_data.nyukai_tenpo_mcc.intVal());
            C_DbgMsg("会社コードＭＣＣ              [%d]\n", cmBTpmdfBDto.honjitsu_point_data.kaisha_cd_mcc.intVal());
            C_DbgMsg("店番号ＭＣＣ                  [%d]\n", cmBTpmdfBDto.honjitsu_point_data.mise_no_mcc.intVal());
            C_DbgMsg("カード種別                    [%d]\n", cmBTpmdfBDto.honjitsu_point_data.card_syubetsu.intVal());
            C_DbgMsg("登録経路                      [%s]\n", cmBTpmdfBDto.honjitsu_point_data.touroku_keiro);
            C_DbgMsg("取引区分                      [%s]\n", cmBTpmdfBDto.honjitsu_point_data.torihiki_kbn);
            C_DbgMsg("**********ポイント日別情報を追加**********%s\n", "");
        }
        /****************************************************************************************************************************/

        /* ポイント日別情報を追加する */
        rtn_cd = cmBTfuncBImpl.C_InsertDayPoint(cmBTpmdfBDto.honjitsu_point_data, atoi(bat_date), rtn_status  );
        if (DBG_LOG) {
            C_DbgMsg("ポイント日別情報追加 [%d]\n", rtn_cd);
        }
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null,
                    "C_InsertDayPoint" , rtn_cd, 0, 0, 0, 0);
            return( C_const_NG ) ;
        }

        /* HSポイント日別情報追加レコード件数 */
        /* をカウントアップする               */
        hsph_in_rec_cnt_buf++;

        /* 2022/10/04 MCCM初版 ADD START */
        /* ポイント日別内訳情報を追加する */
        /* ポイント日別内訳情報構造体の編集 */
        memset(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data, 0x00, sizeof(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data));
        /* システム年月日 */
        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.arr = atoi(bat_date);

        /* 顧客番号 */
        strcpy(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no, kokyaku_no);
        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.len = strlen(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no);

        /* 処理通番 */
        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq = cmBTpmdfBDto.honjitsu_point_data.shori_seq;

        /* 枝番 */
        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.arr = 1;

        //int pointsu[6] = {in_point_shusei.tujo_point_kagenti_tonen,
        //                  in_point_shusei.tujo_point_kagenti_zennen,
        //                  in_point_shusei.kikan_point_kagenti_month0,
        //                  in_point_shusei.kikan_point_kagenti_month1,
        //                  in_point_shusei.kikan_point_kagenti_month2,
        //                  in_point_shusei.kikan_point_kagenti_month3
        //               };
        int[] pointsu ={(int)check_over_ym.tujo_point_kagenti_tonen,
                (int)check_over_ym.tujo_point_kagenti_zennen,
                (int)check_over_ym.kikan_point_kagenti_month0,
                (int)check_over_ym.kikan_point_kagenti_month1,
                (int)check_over_ym.kikan_point_kagenti_month2,
                (int)check_over_ym.kikan_point_kagenti_month3
        };
        int[] tujo_kikan_gentei_kbn = {1,1,2,2,2,2};
        int i = 0;
        int j = 0;
        IntegerDto yukokigen = new IntegerDto();
        yukokigen.arr = 0;
        for(i=0; i<6; i++){
            if(pointsu[i] == 0){continue;}
            GetYukokigen(i, yukokigen);           /* ポイント有効期限取得 */
            j++;
            switch( j ) {
                case 1:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.arr = in_point_shusei.uard_flg;                     /* 購買区分 */
                    }
                    break;

                case 2:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.arr = in_point_shusei.uard_flg;                     /* 購買区分 */
                    }
                    break;

                case 3:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_03.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.arr = in_point_shusei.uard_flg;                     /* 購買区分 */
                    }
                    break;

                case 4:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_04.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04.arr = in_point_shusei.uard_flg;                      /* 購買区分 */
                    }
                    break;

                case 5:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_05.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_05.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_05.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_05.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_05.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05.arr = in_point_shusei.uard_flg;                      /* 購買区分 */
                    }
                    break;

                case 6:
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_06.arr = 1;                                        /* 付与利用区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_06.arr = 2;                                   /* ポイントカテゴリ */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_06.arr = 2;                                       /* ポイント種別 */

                    if(pointsu[i] < 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_06.arr = 153;                /* 買上高ポイント種別 */
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_06.arr = 151;             /* 買上高ポイント種別 */
                    }

                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_06.arr = pointsu[i];                                  /* 付与ポイント */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_06.arr = tujo_kikan_gentei_kbn[i];     /* 通常期間限定区分 */
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_06.arr = yukokigen;                          /* ポイント有効期限 */

                    if(in_point_shusei.uard_flg == 0){
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_06.arr = 1;
                    }else{
                        cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_06.arr = in_point_shusei.uard_flg;                      /* 購買区分 */
                    }
                    break;
            }
        }

        /****************************************************************************************************************************/
        if (DBG_LOG) {
            C_DbgMsg("****************************ポイント日別内訳情報を追加*****************************************%s\n", "");
            C_DbgMsg("顧客番号                      [%s]\n", cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.arr);


            C_DbgMsg("**********ポイント日別内訳情報を追加**********%s\n", "");
        }
        /****************************************************************************************************************************/

                /* HSポイント日別内訳情報を追加する */
                rtn_cd = InsertDayPointUchiwake();

        if (DBG_LOG) {
            C_DbgMsg("HSポイント日別内訳情報追加 [%d]\n", rtn_cd);
        }
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null,
                    "InsertDayPointUchiwake" , rtn_cd, 0, 0, 0, 0);
            return( C_const_NG ) ;
        }
        /* 2022/10/04 MCCM初版 ADD END */

        /* ポイント年別情報構造体の編集 */
        memset(cmBTpmdfBDto.point_year_data, 0x00, sizeof(cmBTpmdfBDto.point_year_data));
        /*        memset(wk_buf, 0x00, sizeof(wk_buf));*/
        /*        memcpy(wk_buf, bat_date, 4);*/
        /*        year_buf = (unsigned int)atoi(wk_buf);*/
        /*        point_year_data.year = year_buf;*/
        cmBTpmdfBDto.point_year_data.year.arr = shuse_taisho_nen_moto;              /* 年 */                   /* 2023/01/08 MCCM初版 MOD */
        strcpy(cmBTpmdfBDto.point_year_data.kokyaku_no, kokyaku_no); /* 顧客番号       */
        cmBTpmdfBDto.point_year_data.kokyaku_no.len = strlen(cmBTpmdfBDto.point_year_data.kokyaku_no);
        /* 年間付与ポイント  */
        cmBTpmdfBDto.point_year_data.nenkan_fuyo_point.arr
                = in_point_shusei.tujo_point_kagenti_zennen
                + in_point_shusei.tujo_point_kagenti_tonen
                + in_point_shusei.kikan_point_kagenti_month0
                + in_point_shusei.kikan_point_kagenti_month1
                + in_point_shusei.kikan_point_kagenti_month2
                + in_point_shusei.kikan_point_kagenti_month3;
        /* =point_shuse_data.riyo_kano_point_kagenti; */
        cmBTpmdfBDto.point_year_data.nenkan_riyo_point.arr = 0;           /* 年間利用ポイント  */
        /* 年間基本Ｐ率対象ポイント       */
        cmBTpmdfBDto.point_year_data.nenkan_kihon_pritsu_taisho_point.arr = 0;
        /* 年間ランクＵＰ対象金額         */
        //point_year_data.nenkan_rankup_taisho_kingaku = point_shuse_data.rankup_taisho_kingaku_kagenti;   /* 2022/10/04 MCCM初版DEL */
        /* 年間ポイント対象金額           */
        cmBTpmdfBDto.point_year_data.nenkan_point_taisho_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        /* 年間買上額        */
        cmBTpmdfBDto.point_year_data.nenkan_kaiage_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        cmBTpmdfBDto.point_year_data.nenkan_kaiage_cnt.arr = 0;             /* 年間買上回数      */
        cmBTpmdfBDto.point_year_data.nenkan_kaiage_nissu.arr = 0;           /* 年間買上日数      */
        cmBTpmdfBDto.point_year_data.nenkan_kaimonoken_hakko_point.arr = 0; /* 年間買物券発行ポイント         */

        cmBTpmdfBDto.point_year_data.saishu_koshin_ymd.arr=atoi(bat_date); /* 最終更新日       */
        strcpy(cmBTpmdfBDto.point_year_data.saishu_koshin_programid, Cg_Program_Name);

        /* ポイント年別情報を更新する */
        rtn_cd = cmBTfuncBImpl.C_UpdateYearPoint(cmBTpmdfBDto.point_year_data, cmBTpmdfBDto.point_year_data.year.intVal(), this_month, rtn_status );
        if (DBG_LOG) {
            C_DbgMsg("ポイント年別情報更新 [%d]\n", rtn_cd);
        }
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null, "C_UpdateYearPoint" , rtn_cd, rtn_status, 0, 0, 0);
            return( C_const_NG ) ;
        }
        /* TSポイント年別情報更新レコード件数をカウントアップする */
        tspn_up_rec_cnt_buf++;


        /* 2023/01/08 MCCM初版 MOD START */
        rtn_cd = UpdateTsRankInfo(cmBTpmdfBDto.point_shuse_data.shuse_taisho_nen.intVal(), this_month);
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null, "UpdateTsRankInfo" , rtn_cd, rtn_cd, 0, 0, 0);
            return( C_const_NG ) ;
        }
        /* 2023/01/08 MCCM初版 MOD END */


        /* ポイント累計情報構造体の編集 */
        memset(cmBTpmdfBDto.total_point_data, 0x00, sizeof(cmBTpmdfBDto.total_point_data));
        strcpy(cmBTpmdfBDto.total_point_data.kokyaku_no, kokyaku_no); /* 顧客番号         */
        cmBTpmdfBDto.total_point_data.kokyaku_no.len = strlen(cmBTpmdfBDto.total_point_data.kokyaku_no);
        /* 累計付与ポイント */
        cmBTpmdfBDto.total_point_data.ruike_fuyo_point.arr
                = in_point_shusei.tujo_point_kagenti_zennen
                + in_point_shusei.tujo_point_kagenti_tonen
                + in_point_shusei.kikan_point_kagenti_month0
                + in_point_shusei.kikan_point_kagenti_month1
                + in_point_shusei.kikan_point_kagenti_month2
                + in_point_shusei.kikan_point_kagenti_month3;
        /*= point_shuse_data.riyo_kano_point_kagenti;*/

        cmBTpmdfBDto.total_point_data.ruike_riyo_point.arr = 0;              /* 累計利用ポイント */
        /* 累計基本Ｐ率対象ポイント       */
        cmBTpmdfBDto.total_point_data.ruike_kihon_pritsu_taisho_point.arr = 0;
        /* 累計ランクＵＰ対象金額         */
        cmBTpmdfBDto.total_point_data.ruike_rankup_taisho_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        /* 累計ポイント対象金額           */
        cmBTpmdfBDto.total_point_data.ruike_point_taisho_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        /* 累計買上額       */
        cmBTpmdfBDto.total_point_data.ruike_kaiage_kingaku = cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti;
        cmBTpmdfBDto.total_point_data.ruike_kaiage_cnt.arr = 0;              /* 累計買上回数     */
        cmBTpmdfBDto.total_point_data.ruike_kaiage_nissu.arr = 0;            /* 累計買上日数     */
        cmBTpmdfBDto.total_point_data.saishu_koshin_ymd.arr = atoi(bat_date); /* 最終更新日      */
        strcpy(cmBTpmdfBDto.total_point_data.saishu_koshin_programid, Cg_Program_Name);
        /* 2021/01/21 NDBS.緒方 : 年度管理変更/期間限定ポイント追加対応 */
        /* 累計付与通常Ｐ */
        cmBTpmdfBDto.total_point_data.ruike_fuyo_tsujo_point.arr
                = in_point_shusei.tujo_point_kagenti_zennen
                + in_point_shusei.tujo_point_kagenti_tonen;

        /* 累計付与期間限定Ｐ */
        cmBTpmdfBDto.total_point_data.ruike_fuyo_kikan_gentei_point.arr
                = in_point_shusei.kikan_point_kagenti_month0
                + in_point_shusei.kikan_point_kagenti_month1
                + in_point_shusei.kikan_point_kagenti_month2
                + in_point_shusei.kikan_point_kagenti_month3;

        /* ポイント累計情報を追加/更新する */
        rtn_cd = cmBTfuncBImpl.C_UpdateTotalPoint(cmBTpmdfBDto.total_point_data, rtn_status );
        if (DBG_LOG) {
            C_DbgMsg("ポイント累計情報追加/更新 [%d]\n", rtn_cd);
        }
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null, "C_UpdateTotalPoint" , rtn_cd, rtn_status, 0, 0, 0);
            return( C_const_NG ) ;
        }
        /* TSポイント累計情報更新レコード件数をカウントアップする */
        tspr_up_rec_cnt_buf++;


        /* 2021/02/17 TS期間限定ポイント情報追加 Start */
        /* 期間限定ポイント情報構造体の編集 */
        memset(cmBTpmdfBDto.kikan_point_data, 0x00, sizeof(cmBTpmdfBDto.kikan_point_data));
        strcpy(cmBTpmdfBDto.kikan_point_data.kokyaku_no, kokyaku_no); /* 顧客番号         */
        cmBTpmdfBDto.kikan_point_data.kokyaku_no.len = strlen(cmBTpmdfBDto.kikan_point_data.kokyaku_no);

        /* 付与期間限定ＰＭＭ設定 */
        /* 当月 */
        cmBTpmdfB_setKikanPoint(this_month,check_over_ym.kikan_point_kagenti_month0);

        /* １ヶ月後 */
        cmBTpmdfB_setKikanPoint(this_month+1,
                check_over_ym.kikan_point_kagenti_month1);

        /* 2ヶ月後 */
        cmBTpmdfB_setKikanPoint(this_month+2,
                check_over_ym.kikan_point_kagenti_month2);

        /* 3ヶ月後 */
        cmBTpmdfB_setKikanPoint(this_month+3,
                check_over_ym.kikan_point_kagenti_month3);

        cmBTpmdfBDto.kikan_point_data.sagyo_kigyo_cd.arr = 0;           /* 作業企業コード   */
        cmBTpmdfBDto.kikan_point_data.sagyosha_id.arr = 0;              /* 作業者ＩＤ       */
        cmBTpmdfBDto.kikan_point_data.sagyo_ymd.arr = 0;                /* 作業年月日       */
        cmBTpmdfBDto.kikan_point_data.sagyo_hms.arr = 0;                /* 作業時刻         */
        cmBTpmdfBDto.kikan_point_data.batch_koshin_ymd.arr = atoi(bat_date);  /* バッチ更新日     */
        cmBTpmdfBDto.kikan_point_data.saishu_koshin_ymd.arr = atoi(bat_date); /* 最終更新日       */
        cmBTpmdfBDto.kikan_point_data.saishu_koshin_ymdhms.arr = 0;       /* 最終更新日時     */
        /* 最終更新プログラムID */
        strcpy(cmBTpmdfBDto.kikan_point_data.saishu_koshin_programid, Cg_Program_Name);

        /* 期間限定ポイント情報を追加/更新する */
        rtn_cd = cmBTfuncBImpl.C_UpdateKikanPoint(cmBTpmdfBDto.kikan_point_data, rtn_status );
        if (DBG_LOG) {
            C_DbgMsg("期間限定ポイント情報追加/更新 [%d]\n", rtn_cd);
        }
        if ( rtn_cd != C_const_OK )
        {
            APLOG_WT( "903", 0, null, "C_UpdateKikanPoint" , rtn_cd, rtn_status, 0, 0, 0);
            return( C_const_NG ) ;
        }
        /* TS期間限定ポイント情報更新レコード件数をカウントアップする */
        tskp_up_rec_cnt_buf++;
        /* 2021/02/17 TS期間限定ポイント情報追加 End */

        hsph_in_rec_cnt.arr = hsph_in_rec_cnt_buf;
        tspn_up_rec_cnt.arr = tspn_up_rec_cnt_buf;
        tspr_up_rec_cnt.arr = tspr_up_rec_cnt_buf;
        /* 2021/02/17 TS期間限定ポイント情報追加 Start */
        tskp_up_rec_cnt.arr = tskp_up_rec_cnt_buf;
        /* 2021/02/17 TS期間限定ポイント情報追加 End */


        if (DBG_LOG) {
            C_DbgMsg("*** hsph_in_rec_cnt[%d]\n", hsph_in_rec_cnt );
            C_DbgMsg("*** tspn_up_rec_cnt[%d]\n", tspn_up_rec_cnt );
            C_DbgMsg("*** tspr_up_rec_cnt[%d]\n", tspr_up_rec_cnt );
            /* 2021/02/17 TS期間限定ポイント情報追加 Start */
            C_DbgMsg("*** tskp_up_rec_cnt[%d]\n", tskp_up_rec_cnt );
            /* 2021/02/17 TS期間限定ポイント情報追加 End */
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： getKazokuSedoInfo                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL *ksMaster, int *status) */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              家族制度情報取得                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              MS_KAZOKU_SEDO_INFO_TBL * ksMaster ： 家族制度情報構造体      */
    /*              int                     * status   ： 結果ステータス          */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL ksMaster, IntegerDto status) {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */

        /* ホスト変数 */
        // EXEC SQL BEGIN DECLARE SECTION;

        MS_KAZOKU_SEDO_INFO_TBL h_ms_kazoku_sedo_buff = new MS_KAZOKU_SEDO_INFO_TBL(); /* MS家族制度情報バッファ */
        // EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("getKazokuSedoInfo : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (ksMaster == null || status == null) {
            /* 入力引数エラー */
            C_DbgMsg("getKazokuSedoInfo : %s\n", "PRMERR(NULL)");
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            C_DbgMsg("getKazokuSedoInfo : %s\n", "DBERR(connect check NG)");
                status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* 出力エリアのクリア */
        /* 2022/09/26 MCCM初版 DEL START */
//      memset(ksMaster->kazoku_oya_kokyaku_no.arr,0x00,sizeof(ksMaster->kazoku_oya_kokyaku_no.arr));
//      ksMaster->kazoku_oya_kokyaku_no.len = 0;  /* 家族親顧客番号 */
        /* 2022/09/26 MCCM初版 DEL END */
        memset(ksMaster.kazoku_1_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_1_kokyaku_no.arr));
        ksMaster.kazoku_1_kokyaku_no.len = 0;  /* 家族１顧客番号 */
        memset(ksMaster.kazoku_2_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_2_kokyaku_no.arr));
        ksMaster.kazoku_2_kokyaku_no.len = 0;  /* 家族２顧客番号 */
        memset(ksMaster.kazoku_3_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_3_kokyaku_no.arr));
        ksMaster.kazoku_3_kokyaku_no.len = 0;  /* 家族３顧客番号 */
        memset(ksMaster.kazoku_4_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_4_kokyaku_no.arr));
        ksMaster.kazoku_4_kokyaku_no.len = 0;  /* 家族４顧客番号 */
        /* 2022/09/26 MCCM初版 ADD START */
        memset(ksMaster.kazoku_5_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_5_kokyaku_no.arr));
        ksMaster.kazoku_5_kokyaku_no.len = 0;  /* 家族５顧客番号 */
        memset(ksMaster.kazoku_6_kokyaku_no.arr,0x00,sizeof(ksMaster.kazoku_6_kokyaku_no.arr));
        ksMaster.kazoku_6_kokyaku_no.len = 0;  /* 家族６顧客番号 */
        /* 2022/09/26 MCCM初版 ADD END */
        /* 2022/09/26 MCCM初版 DEL START */
//      ksMaster->kazoku_oya_toroku_ymd = 0;    /* 家族親登録日         */
        /* 2022/09/26 MCCM初版 DEL END */
        ksMaster.kazoku_1_toroku_ymd.arr   = 0;    /* 家族１登録日         */
        ksMaster.kazoku_2_toroku_ymd.arr   = 0;    /* 家族２登録日         */
        ksMaster.kazoku_3_toroku_ymd.arr   = 0;    /* 家族３登録日         */
        ksMaster.kazoku_4_toroku_ymd.arr   = 0;    /* 家族４登録日         */
        /* 2022/09/26 MCCM初版 ADD START */
        ksMaster.kazoku_5_toroku_ymd.arr   = 0;    /* 家族５登録日         */
        ksMaster.kazoku_6_toroku_ymd.arr   = 0;    /* 家族６登録日         */
        /* 2022/09/26 MCCM初版 ADD END */
        ksMaster.kazoku_sakusei_ymd.arr    = 0;    /* 家族作成日           */

        ksMaster.kazoku_rankup_kingaku_saishu_koshin_ymd.arr = 0; /* 家族ランクＵＰ金額最終更新日 */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_0.arr = 0.0; /* 年間家族ランクアップ対象金額０ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_1.arr = 0.0; /* 年間家族ランクアップ対象金額１ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_2.arr = 0.0; /* 年間家族ランクアップ対象金額２ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_3.arr = 0.0; /* 年間家族ランクアップ対象金額３ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_4.arr = 0.0; /* 年間家族ランクアップ対象金額４ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_5.arr = 0.0; /* 年間家族ランクアップ対象金額５ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_6.arr = 0.0; /* 年間家族ランクアップ対象金額６ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_7.arr = 0.0; /* 年間家族ランクアップ対象金額７ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_8.arr = 0.0; /* 年間家族ランクアップ対象金額８ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_9.arr = 0.0; /* 年間家族ランクアップ対象金額９ */

        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_001.arr = 0.0; /* 月間家族ランクアップ対象金額００１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_002.arr = 0.0; /* 月間家族ランクアップ対象金額００２ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_003.arr = 0.0; /* 月間家族ランクアップ対象金額００３ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_004.arr = 0.0; /* 月間家族ランクアップ対象金額００４ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_005.arr = 0.0; /* 月間家族ランクアップ対象金額００５ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_006.arr = 0.0; /* 月間家族ランクアップ対象金額００６ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_007.arr = 0.0; /* 月間家族ランクアップ対象金額００７ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_008.arr = 0.0; /* 月間家族ランクアップ対象金額００８ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_009.arr = 0.0; /* 月間家族ランクアップ対象金額００９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_010.arr = 0.0; /* 月間家族ランクアップ対象金額０１０ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_011.arr = 0.0; /* 月間家族ランクアップ対象金額０１１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_012.arr = 0.0; /* 月間家族ランクアップ対象金額０１２ */

        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_101.arr = 0.0; /* 月間家族ランクアップ対象金額１０１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_102.arr = 0.0; /* 月間家族ランクアップ対象金額１０２ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_103.arr = 0.0; /* 月間家族ランクアップ対象金額１０３ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_104.arr = 0.0; /* 月間家族ランクアップ対象金額１０４ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_105.arr = 0.0; /* 月間家族ランクアップ対象金額１０５ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_106.arr = 0.0; /* 月間家族ランクアップ対象金額１０６ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_107.arr = 0.0; /* 月間家族ランクアップ対象金額１０７ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_108.arr = 0.0; /* 月間家族ランクアップ対象金額１０８ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_109.arr = 0.0; /* 月間家族ランクアップ対象金額１０９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_110.arr = 0.0; /* 月間家族ランクアップ対象金額１１０ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_111.arr = 0.0; /* 月間家族ランクアップ対象金額１１１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_112.arr = 0.0; /* 月間家族ランクアップ対象金額１１２ */

        ksMaster.nenji_rank_cd_0.arr = 0;                      /* 年次ランクコード０       */
        ksMaster.nenji_rank_cd_1.arr = 0;                      /* 年次ランクコード１       */
        ksMaster.nenji_rank_cd_2.arr = 0;                      /* 年次ランクコード２       */
        ksMaster.nenji_rank_cd_3.arr = 0;                      /* 年次ランクコード３       */
        ksMaster.nenji_rank_cd_4.arr = 0;                      /* 年次ランクコード４       */
        ksMaster.nenji_rank_cd_5.arr = 0;                      /* 年次ランクコード５       */
        ksMaster.nenji_rank_cd_6.arr = 0;                      /* 年次ランクコード６       */
        ksMaster.nenji_rank_cd_7.arr = 0;                      /* 年次ランクコード７       */
        ksMaster.nenji_rank_cd_8.arr = 0;                      /* 年次ランクコード８       */
        ksMaster.nenji_rank_cd_9.arr = 0;                      /* 年次ランクコード９       */

        ksMaster.getuji_rank_cd_001.arr = 0;                   /* 月次ランクコード００１    */
        ksMaster.getuji_rank_cd_002.arr = 0;                   /* 月次ランクコード００２    */
        ksMaster.getuji_rank_cd_003.arr = 0;                   /* 月次ランクコード００３    */
        ksMaster.getuji_rank_cd_004.arr = 0;                   /* 月次ランクコード００４    */
        ksMaster.getuji_rank_cd_005.arr = 0;                   /* 月次ランクコード００５    */
        ksMaster.getuji_rank_cd_006.arr = 0;                   /* 月次ランクコード００６    */
        ksMaster.getuji_rank_cd_007.arr = 0;                   /* 月次ランクコード００７    */
        ksMaster.getuji_rank_cd_008.arr = 0;                   /* 月次ランクコード００８    */
        ksMaster.getuji_rank_cd_009.arr = 0;                   /* 月次ランクコード００９    */
        ksMaster.getuji_rank_cd_010.arr = 0;                   /* 月次ランクコード０１０    */
        ksMaster.getuji_rank_cd_011.arr = 0;                   /* 月次ランクコード０１１    */
        ksMaster.getuji_rank_cd_012.arr = 0;                   /* 月次ランクコード０１２    */

        ksMaster.getuji_rank_cd_101.arr = 0;                   /* 月次ランクコード１０１    */
        ksMaster.getuji_rank_cd_102.arr = 0;                   /* 月次ランクコード１０２    */
        ksMaster.getuji_rank_cd_103.arr = 0;                   /* 月次ランクコード１０３    */
        ksMaster.getuji_rank_cd_104.arr = 0;                   /* 月次ランクコード１０４    */
        ksMaster.getuji_rank_cd_105.arr = 0;                   /* 月次ランクコード１０５    */
        ksMaster.getuji_rank_cd_106.arr = 0;                   /* 月次ランクコード１０６    */
        ksMaster.getuji_rank_cd_107.arr = 0;                   /* 月次ランクコード１０７    */
        ksMaster.getuji_rank_cd_108.arr = 0;                   /* 月次ランクコード１０８    */
        ksMaster.getuji_rank_cd_109.arr = 0;                   /* 月次ランクコード１０９    */
        ksMaster.getuji_rank_cd_110.arr = 0;                   /* 月次ランクコード１１０    */
        ksMaster.getuji_rank_cd_111.arr = 0;                   /* 月次ランクコード１１１    */
        ksMaster.getuji_rank_cd_112.arr = 0;                   /* 月次ランクコード１１２    */

        ksMaster.kazoku_sakujo_ymd.arr    = 0;           /* 家族削除日           */

        ksMaster.sagyo_kigyo_cd.arr = 0;                 /* 作業企業コード       */
        ksMaster.sagyosha_id.arr = 0.0;                  /* 作業者ＩＤ           */
        ksMaster.sagyo_ymd.arr = 0;                      /* 作業年月日           */
        ksMaster.sagyo_hms.arr = 0;                      /* 作業時刻             */

        ksMaster.batch_koshin_ymd.arr = 0;               /* バッチ更新日         */
        ksMaster.saishu_koshin_ymd.arr = 0;
        ksMaster.saishu_koshin_ymdhms.arr = 0.0;
        strcpy(ksMaster.saishu_koshin_programid, "                    "); /* スペース２０桁 */


        /* ホスト変数を編集する */
        /* (検索条件)           */
        memset(h_ms_kazoku_sedo_buff, 0x00, sizeof(h_ms_kazoku_sedo_buff));
        //h_ms_kazoku_sedo_buff.kazoku_id = ksMaster->kazoku_id;                                     /* 2022/09/26 MCCM初版 DEL */
        //strcpy((char*)h_ms_kazoku_sedo_buff.kazoku_id.arr, (char*)ksMaster->kazoku_id.arr);          /* 2022/09/26 MCCM初版 ADD */
        memcpy(h_ms_kazoku_sedo_buff.kazoku_id, ksMaster.kazoku_id, ksMaster.kazoku_id.len);
        h_ms_kazoku_sedo_buff.kazoku_id.len = ksMaster.kazoku_id.len;                               /* 2022/09/26 MCCM初版 ADD */

        if (DBG_LOG) {
            C_DbgMsg("getKazokuSedoInfo : kazoku_id=%s\n", h_ms_kazoku_sedo_buff.kazoku_id.arr);     /* 2022/09/26 MCCM初版 MOD */
        }

        /* ＳＱＬを実行する */

        /* 環境変数の取得 */
        /* 顧客データベースに接続している場合 */

        if (DBG_LOG) {
            C_DbgMsg("getKazokuSedoInfo : %s\n", "@ MCSD");
        }
        /*EXEC SQL
        *//*                  SELECT  NVL(家族親顧客番号,0),                                       *//**//* 2022/09/26 MCCM初版 DEL *//*
        SELECT  NVL(家族１顧客番号,0),                                         *//* 2022/09/26 MCCM初版 MOD *//*
        NVL(家族２顧客番号,0),
                NVL(家族３顧客番号,0),
                NVL(家族４顧客番号,0),
                NVL(家族５顧客番号,0),                                         *//* 2022/09/26 MCCM初版 ADD *//*
                NVL(家族６顧客番号,0),                                         *//* 2022/09/26 MCCM初版 ADD *//*
                *//*                          NVL(家族親登録日,0),                                         *//**//* 2022/09/26 MCCM初版 DEL *//*
                NVL(家族１登録日,0),                                           *//* 2022/09/26 MCCM初版 MOD *//*
                NVL(家族２登録日,0),                                           *//* 2022/09/26 MCCM初版 MOD *//*
                NVL(家族３登録日,0),                                           *//* 2022/09/26 MCCM初版 MOD *//*
                NVL(家族４登録日,0),                                           *//* 2022/09/26 MCCM初版 MOD *//*
                NVL(家族５登録日,0),                                           *//* 2022/09/26 MCCM初版 ADD *//*
                NVL(家族６登録日,0),                                           *//* 2022/09/26 MCCM初版 ADD *//*
                NVL(家族作成日,0),
                NVL(家族ランクＵＰ金額最終更新日,0),
                NVL(年間家族ランクＵＰ対象金額０,0),
                NVL(年間家族ランクＵＰ対象金額１,0),
                NVL(年間家族ランクＵＰ対象金額２,0),
                NVL(年間家族ランクＵＰ対象金額３,0),
                NVL(年間家族ランクＵＰ対象金額４,0),
                NVL(年間家族ランクＵＰ対象金額５,0),
                NVL(年間家族ランクＵＰ対象金額６,0),
                NVL(年間家族ランクＵＰ対象金額７,0),
                NVL(年間家族ランクＵＰ対象金額８,0),
                NVL(年間家族ランクＵＰ対象金額９,0),
                NVL(月間家族ランクＵＰ金額００１,0),
                NVL(月間家族ランクＵＰ金額００２,0),
                NVL(月間家族ランクＵＰ金額００３,0),
                NVL(月間家族ランクＵＰ金額００４,0),
                NVL(月間家族ランクＵＰ金額００５,0),
                NVL(月間家族ランクＵＰ金額００６,0),
                NVL(月間家族ランクＵＰ金額００７,0),
                NVL(月間家族ランクＵＰ金額００８,0),
                NVL(月間家族ランクＵＰ金額００９,0),
                NVL(月間家族ランクＵＰ金額０１０,0),
                NVL(月間家族ランクＵＰ金額０１１,0),
                NVL(月間家族ランクＵＰ金額０１２,0),
                NVL(月間家族ランクＵＰ金額１０１,0),
                NVL(月間家族ランクＵＰ金額１０２,0),
                NVL(月間家族ランクＵＰ金額１０３,0),
                NVL(月間家族ランクＵＰ金額１０４,0),
                NVL(月間家族ランクＵＰ金額１０５,0),
                NVL(月間家族ランクＵＰ金額１０６,0),
                NVL(月間家族ランクＵＰ金額１０７,0),
                NVL(月間家族ランクＵＰ金額１０８,0),
                NVL(月間家族ランクＵＰ金額１０９,0),
                NVL(月間家族ランクＵＰ金額１１０,0),
                NVL(月間家族ランクＵＰ金額１１１,0),
                NVL(月間家族ランクＵＰ金額１１２,0),
                NVL(年次ランクコード０,0),
                NVL(年次ランクコード１,0),
                NVL(年次ランクコード２,0),
                NVL(年次ランクコード３,0),
                NVL(年次ランクコード４,0),
                NVL(年次ランクコード５,0),
                NVL(年次ランクコード６,0),
                NVL(年次ランクコード７,0),
                NVL(年次ランクコード８,0),
                NVL(年次ランクコード９,0),
                NVL(月次ランクコード００１,0),
                NVL(月次ランクコード００２,0),
                NVL(月次ランクコード００３,0),
                NVL(月次ランクコード００４,0),
                NVL(月次ランクコード００５,0),
                NVL(月次ランクコード００６,0),
                NVL(月次ランクコード００７,0),
                NVL(月次ランクコード００８,0),
                NVL(月次ランクコード００９,0),
                NVL(月次ランクコード０１０,0),
                NVL(月次ランクコード０１１,0),
                NVL(月次ランクコード０１２,0),
                NVL(月次ランクコード１０１,0),
                NVL(月次ランクコード１０２,0),
                NVL(月次ランクコード１０３,0),
                NVL(月次ランクコード１０４,0),
                NVL(月次ランクコード１０５,0),
                NVL(月次ランクコード１０６,0),
                NVL(月次ランクコード１０７,0),
                NVL(月次ランクコード１０８,0),
                NVL(月次ランクコード１０９,0),
                NVL(月次ランクコード１１０,0),
                NVL(月次ランクコード１１１,0),
                NVL(月次ランクコード１１２,0),
                NVL(家族削除日,0),
                NVL(作業企業コード,0),
                NVL(作業者ＩＤ,0),
                NVL(作業年月日,0),
                NVL(作業時刻,0),
                NVL(バッチ更新日,0),
                NVL(最終更新日,0),
                NVL(to_number(to_char(最終更新日時,'yyyymmddhh24miss')), 0),   *//* 2022/09/26 MCCM初版 MOD *//*
                NVL(最終更新プログラムＩＤ,'                    ')
        *//*                    INTO  :h_ms_kazoku_sedo_buff.kazoku_oya_kokyaku_no,                *//**//* 2022/09/26 MCCM初版 DEL *//*
        INTO  :h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,                    *//* 2022/09/26 MCCM初版 MOD *//*
                            :h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,
                            :h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,
                            :h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,
                            :h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,                    *//* 2022/09/26 MCCM初版 ADD *//*
                            :h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,                    *//* 2022/09/26 MCCM初版 ADD *//*
        *//*                          :h_ms_kazoku_sedo_buff.kazoku_oya_toroku_ymd,                *//**//* 2022/09/26 MCCM初版 DEL *//*
                            :h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,
                            :h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,
                            :h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,
                            :h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,
                            :h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,                    *//* 2022/09/26 MCCM初版 ADD *//*
                            :h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,                    *//* 2022/09/26 MCCM初版 ADD *//*
                            :h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,
                            :h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,
                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111,
                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_0,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_1,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_2,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_3,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_4,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_5,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_6,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_7,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_8,
                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_9,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_001,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_002,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_003,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_004,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_005,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_006,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_007,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_008,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_009,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_010,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_011,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_012,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_101,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_102,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_103,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_104,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_105,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_106,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_107,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_108,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_109,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_110,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_111,
                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_112,
                            :h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,
                            :h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,
                            :h_ms_kazoku_sedo_buff.sagyosha_id,
                            :h_ms_kazoku_sedo_buff.sagyo_ymd,
                            :h_ms_kazoku_sedo_buff.sagyo_hms,
                            :h_ms_kazoku_sedo_buff.batch_koshin_ymd,
                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymd,
                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,
                            :h_ms_kazoku_sedo_buff.saishu_koshin_programid
        FROM  MS家族制度情報
        WHERE 家族ＩＤ = :h_ms_kazoku_sedo_buff.kazoku_id;*/

        StringDto workSql = new StringDto();
        workSql.arr = "SELECT  NVL(家族１顧客番号,0),\n" +
                "        NVL(家族２顧客番号,0),\n" +
                "                NVL(家族３顧客番号,0),\n" +
                "                NVL(家族４顧客番号,0),\n" +
                "                NVL(家族５顧客番号,0),\n" +
                "                NVL(家族６顧客番号,0),\n" +
                "                NVL(家族１登録日,0),\n" +
                "                NVL(家族２登録日,0),\n" +
                "                NVL(家族３登録日,0),\n" +
                "                NVL(家族４登録日,0),\n" +
                "                NVL(家族５登録日,0),\n" +
                "                NVL(家族６登録日,0),\n" +
                "                NVL(家族作成日,0),\n" +
                "                NVL(家族ランクＵＰ金額最終更新日,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額０,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額１,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額２,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額３,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額４,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額５,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額６,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額７,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額８,0),\n" +
                "                NVL(年間家族ランクＵＰ対象金額９,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００１,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００２,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００３,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００４,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００５,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００６,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００７,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００８,0),\n" +
                "                NVL(月間家族ランクＵＰ金額００９,0),\n" +
                "                NVL(月間家族ランクＵＰ金額０１０,0),\n" +
                "                NVL(月間家族ランクＵＰ金額０１１,0),\n" +
                "                NVL(月間家族ランクＵＰ金額０１２,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０１,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０２,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０３,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０４,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０５,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０６,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０７,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０８,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１０９,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１１０,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１１１,0),\n" +
                "                NVL(月間家族ランクＵＰ金額１１２,0),\n" +
                "                NVL(年次ランクコード０,0),\n" +
                "                NVL(年次ランクコード１,0),\n" +
                "                NVL(年次ランクコード２,0),\n" +
                "                NVL(年次ランクコード３,0),\n" +
                "                NVL(年次ランクコード４,0),\n" +
                "                NVL(年次ランクコード５,0),\n" +
                "                NVL(年次ランクコード６,0),\n" +
                "                NVL(年次ランクコード７,0),\n" +
                "                NVL(年次ランクコード８,0),\n" +
                "                NVL(年次ランクコード９,0),\n" +
                "                NVL(月次ランクコード００１,0),\n" +
                "                NVL(月次ランクコード００２,0),\n" +
                "                NVL(月次ランクコード００３,0),\n" +
                "                NVL(月次ランクコード００４,0),\n" +
                "                NVL(月次ランクコード００５,0),\n" +
                "                NVL(月次ランクコード００６,0),\n" +
                "                NVL(月次ランクコード００７,0),\n" +
                "                NVL(月次ランクコード００８,0),\n" +
                "                NVL(月次ランクコード００９,0),\n" +
                "                NVL(月次ランクコード０１０,0),\n" +
                "                NVL(月次ランクコード０１１,0),\n" +
                "                NVL(月次ランクコード０１２,0),\n" +
                "                NVL(月次ランクコード１０１,0),\n" +
                "                NVL(月次ランクコード１０２,0),\n" +
                "                NVL(月次ランクコード１０３,0),\n" +
                "                NVL(月次ランクコード１０４,0),\n" +
                "                NVL(月次ランクコード１０５,0),\n" +
                "                NVL(月次ランクコード１０６,0),\n" +
                "                NVL(月次ランクコード１０７,0),\n" +
                "                NVL(月次ランクコード１０８,0),\n" +
                "                NVL(月次ランクコード１０９,0),\n" +
                "                NVL(月次ランクコード１１０,0),\n" +
                "                NVL(月次ランクコード１１１,0),\n" +
                "                NVL(月次ランクコード１１２,0),\n" +
                "                NVL(家族削除日,0),\n" +
                "                NVL(作業企業コード,0),\n" +
                "                NVL(作業者ＩＤ,0),\n" +
                "                NVL(作業年月日,0),\n" +
                "                NVL(作業時刻,0),\n" +
                "                NVL(バッチ更新日,0),\n" +
                "                NVL(最終更新日,0),\n" +
                "                NVL(to_number(to_char(最終更新日時,'yyyymmddhh24miss')), 0),\n" +
                "                NVL(RPAD(最終更新プログラムＩＤ ,LENGTH(最終更新プログラムＩＤ)),'                    ')\n" +
                "        FROM  MS家族制度情報\n" +
                "        WHERE 家族ＩＤ = ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(h_ms_kazoku_sedo_buff.kazoku_id);
        sqlca.fetch();
        sqlca.recData(h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,
        h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,
        h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,
        h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,
        h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111,
        h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_0,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_1,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_2,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_3,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_4,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_5,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_6,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_7,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_8,
        h_ms_kazoku_sedo_buff.nenji_rank_cd_9,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_001,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_002,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_003,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_004,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_005,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_006,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_007,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_008,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_009,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_010,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_011,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_012,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_101,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_102,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_103,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_104,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_105,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_106,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_107,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_108,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_109,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_110,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_111,
        h_ms_kazoku_sedo_buff.getuji_rank_cd_112,
        h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,
        h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,
        h_ms_kazoku_sedo_buff.sagyosha_id,
        h_ms_kazoku_sedo_buff.sagyo_ymd,
        h_ms_kazoku_sedo_buff.sagyo_hms,
        h_ms_kazoku_sedo_buff.batch_koshin_ymd,
        h_ms_kazoku_sedo_buff.saishu_koshin_ymd,
        h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,
        h_ms_kazoku_sedo_buff.saishu_koshin_programid);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "家族ＩＤ=%s 会員番号=%s ",
                    h_ms_kazoku_sedo_buff.kazoku_id, cmBTpmdfBDto.point_shuse_data.kaiin_no);        /* 2022/09/26 MCCM初版 MOD */
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MS家族制度情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        /* 出力引数の設定 */
        /* 2022/09/26 MCCM初版 DEL START */
//      strcpy((char *)ksMaster->kazoku_oya_kokyaku_no.arr, (char *)h_ms_kazoku_sedo_buff.kazoku_oya_kokyaku_no.arr);
//      ksMaster->kazoku_oya_kokyaku_no.len = strlen((char *)ksMaster->kazoku_oya_kokyaku_no.arr);
        /* 2022/09/26 MCCM初版 DEL END */
        strcpy(ksMaster.kazoku_1_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no);
        ksMaster.kazoku_1_kokyaku_no.len = strlen(ksMaster.kazoku_1_kokyaku_no);
        strcpy(ksMaster.kazoku_2_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no);
        ksMaster.kazoku_2_kokyaku_no.len = strlen(ksMaster.kazoku_2_kokyaku_no);
        strcpy(ksMaster.kazoku_3_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no);
        ksMaster.kazoku_3_kokyaku_no.len = strlen(ksMaster.kazoku_3_kokyaku_no);
        strcpy(ksMaster.kazoku_4_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no);
        ksMaster.kazoku_4_kokyaku_no.len = strlen(ksMaster.kazoku_4_kokyaku_no);

        /* 2022/09/26 MCCM初版 DEL START */
//      ksMaster->kazoku_oya_toroku_ymd = h_ms_kazoku_sedo_buff.kazoku_oya_toroku_ymd;
        /* 2022/09/26 MCCM初版 DEL END */
        ksMaster.kazoku_1_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd;
        ksMaster.kazoku_2_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd;
        ksMaster.kazoku_3_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd;
        ksMaster.kazoku_4_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd;

        ksMaster.kazoku_sakusei_ymd = h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd;
        ksMaster.kazoku_rankup_kingaku_saishu_koshin_ymd = h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd;

        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_0 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_1 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_2 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_3 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_4 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_5 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_6 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_7 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_8 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8;
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_9 = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9;

        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_001 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_002 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_003 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_004 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_005 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_006 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_007 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_008 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_009 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_010 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_011 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_012 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012;

        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_101 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_102 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_103 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_104 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_105 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_106 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_107 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_108 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_109 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_110 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_111 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111;
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_112 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112;

        ksMaster.nenji_rank_cd_0          = h_ms_kazoku_sedo_buff.nenji_rank_cd_0;
        ksMaster.nenji_rank_cd_1          = h_ms_kazoku_sedo_buff.nenji_rank_cd_1;
        ksMaster.nenji_rank_cd_2          = h_ms_kazoku_sedo_buff.nenji_rank_cd_2;
        ksMaster.nenji_rank_cd_3          = h_ms_kazoku_sedo_buff.nenji_rank_cd_3;
        ksMaster.nenji_rank_cd_4          = h_ms_kazoku_sedo_buff.nenji_rank_cd_4;
        ksMaster.nenji_rank_cd_5          = h_ms_kazoku_sedo_buff.nenji_rank_cd_5;
        ksMaster.nenji_rank_cd_6          = h_ms_kazoku_sedo_buff.nenji_rank_cd_6;
        ksMaster.nenji_rank_cd_7          = h_ms_kazoku_sedo_buff.nenji_rank_cd_7;
        ksMaster.nenji_rank_cd_8          = h_ms_kazoku_sedo_buff.nenji_rank_cd_8;
        ksMaster.nenji_rank_cd_9          = h_ms_kazoku_sedo_buff.nenji_rank_cd_9;

        ksMaster.getuji_rank_cd_001       = h_ms_kazoku_sedo_buff.getuji_rank_cd_001;
        ksMaster.getuji_rank_cd_002       = h_ms_kazoku_sedo_buff.getuji_rank_cd_002;
        ksMaster.getuji_rank_cd_003       = h_ms_kazoku_sedo_buff.getuji_rank_cd_003;
        ksMaster.getuji_rank_cd_004       = h_ms_kazoku_sedo_buff.getuji_rank_cd_004;
        ksMaster.getuji_rank_cd_005       = h_ms_kazoku_sedo_buff.getuji_rank_cd_005;
        ksMaster.getuji_rank_cd_006       = h_ms_kazoku_sedo_buff.getuji_rank_cd_006;
        ksMaster.getuji_rank_cd_007       = h_ms_kazoku_sedo_buff.getuji_rank_cd_007;
        ksMaster.getuji_rank_cd_008       = h_ms_kazoku_sedo_buff.getuji_rank_cd_008;
        ksMaster.getuji_rank_cd_009       = h_ms_kazoku_sedo_buff.getuji_rank_cd_009;
        ksMaster.getuji_rank_cd_010       = h_ms_kazoku_sedo_buff.getuji_rank_cd_010;
        ksMaster.getuji_rank_cd_011       = h_ms_kazoku_sedo_buff.getuji_rank_cd_011;
        ksMaster.getuji_rank_cd_012       = h_ms_kazoku_sedo_buff.getuji_rank_cd_012;

        ksMaster.getuji_rank_cd_101       = h_ms_kazoku_sedo_buff.getuji_rank_cd_101;
        ksMaster.getuji_rank_cd_102       = h_ms_kazoku_sedo_buff.getuji_rank_cd_102;
        ksMaster.getuji_rank_cd_103       = h_ms_kazoku_sedo_buff.getuji_rank_cd_103;
        ksMaster.getuji_rank_cd_104       = h_ms_kazoku_sedo_buff.getuji_rank_cd_104;
        ksMaster.getuji_rank_cd_105       = h_ms_kazoku_sedo_buff.getuji_rank_cd_105;
        ksMaster.getuji_rank_cd_106       = h_ms_kazoku_sedo_buff.getuji_rank_cd_106;
        ksMaster.getuji_rank_cd_107       = h_ms_kazoku_sedo_buff.getuji_rank_cd_107;
        ksMaster.getuji_rank_cd_108       = h_ms_kazoku_sedo_buff.getuji_rank_cd_108;
        ksMaster.getuji_rank_cd_109       = h_ms_kazoku_sedo_buff.getuji_rank_cd_109;
        ksMaster.getuji_rank_cd_110       = h_ms_kazoku_sedo_buff.getuji_rank_cd_110;
        ksMaster.getuji_rank_cd_111       = h_ms_kazoku_sedo_buff.getuji_rank_cd_111;
        ksMaster.getuji_rank_cd_112       = h_ms_kazoku_sedo_buff.getuji_rank_cd_112;

        ksMaster.kazoku_sakujo_ymd = h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd;

        ksMaster.sagyo_kigyo_cd     = h_ms_kazoku_sedo_buff.sagyo_kigyo_cd;
        ksMaster.sagyosha_id        = h_ms_kazoku_sedo_buff.sagyosha_id;
        ksMaster.sagyo_ymd          = h_ms_kazoku_sedo_buff.sagyo_ymd;
        ksMaster.sagyo_hms          = h_ms_kazoku_sedo_buff.sagyo_hms;
        ksMaster.batch_koshin_ymd   = h_ms_kazoku_sedo_buff.batch_koshin_ymd;
        ksMaster.saishu_koshin_ymd = h_ms_kazoku_sedo_buff.saishu_koshin_ymd;
        ksMaster.saishu_koshin_ymdhms = h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms;
        strcpy(ksMaster.saishu_koshin_programid,
                h_ms_kazoku_sedo_buff.saishu_koshin_programid);

        /* 戻り値の設定 */

        if (DBG_LOG) {
            C_DbgMsg("getKazokuSedoInfo : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： getAboutRankColumn                                          */
    /*                                                                            */
    /*      書式                                                                  */
    /*      void getAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn ,         */
    /*                              int year, double *value)                      */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ランク関連情報取得                                            */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int     tbl_kbn ：テーブル種別                                */
    /*                                 ( 0 : 顧客制度情報 / 1 : 家族制度情報 )    */
    /*              int     ym_kbn ： 年／月区分 ( 1 : 月 / 2 : 年 )              */
    /*              int     colkbn  ：カラム種別                                  */
    /*                              ( 0 : ランクＵＰ対象金額 / 1 : ランクコード ) */
    /*              int     year    ： 取得対象年                                 */
    /*              int     month   ： 取得対象月(年／月区分=月の場合に参照)      */
    /*              double* value   ： 取得値                                     */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void getAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn, int year, int month, StringDto value) {
        int year_buf;

        year_buf = year % 10;
        value.arr = "0";

        /* ランク関連項目を取得 */
        if ( tbl_kbn == DEF_TBLKBN_SEDO )
        {
            /* 対象テーブルは顧客制度 */

            if( ym_kbn == DEF_RANK_YEAR )
            {
                /* 年次情報取得 */
                switch( year_buf ) {
                    case 0:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_0.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 1:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_1.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 2:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_2.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 3:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_3.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 4:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_4.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 5:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_5.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 6:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_6.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 7:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_7.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 8:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_8.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                    case 9:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_9.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                        break;
                }
            }
            else
            {

                /* 月次情報取得 */
                if ( ( year % 2 ) == 0 )
                {
                    /* 偶数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_001.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_002.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_003.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_004.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_005.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_006.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_007.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_008.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_009.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_010.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_011.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_012.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                    }
                }
                else
                {
                    /* 奇数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_101.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_102.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_103.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_104.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_105.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_106.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_107.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_108.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_109.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_110.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_111.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_112.strDto(); }     /* 2022/12/28 MCCM初版 MOD */
                            break;
                    }
                }
            }
        }
        else
        {
            /* 対象テーブルは家族制度 */
            if( ym_kbn == DEF_RANK_YEAR )
            {

                /* 年次情報取得 */
                switch( year_buf ) {
                    case 0:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_0.strDto(); }
                        break;
                    case 1:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_1.strDto(); }
                        break;
                    case 2:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_2.strDto(); }
                        break;
                    case 3:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_3.strDto(); }
                        break;
                    case 4:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_4.strDto(); }
                        break;
                    case 5:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_5.strDto(); }
                        break;
                    case 6:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_6.strDto(); }
                        break;
                    case 7:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_7.strDto(); }
                        break;
                    case 8:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_8.strDto(); }
                        break;
                    case 9:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.strDto(); }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.nenji_rank_cd_9.strDto(); }
                        break;
                }
            }
            else
            {
                /* 月次情報取得 */
                if ( ( year % 2 ) == 0 )
                {
                    /* 偶数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_001.strDto(); }
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_002.strDto(); }
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_003.strDto(); }
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_004.strDto(); }
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_005.strDto(); }
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_006.strDto(); }
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_007.strDto(); }
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_008.strDto(); }
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_009.strDto(); }
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_010.strDto(); }
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_011.strDto(); }
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_012.strDto(); }
                            break;
                    }
                }
                else
                {
                    /* 奇数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_101.strDto(); }
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_102.strDto(); }
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_103.strDto(); }
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_104.strDto(); }
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_105.strDto(); }
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_106.strDto(); }
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_107.strDto(); }
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_108.strDto(); }
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_109.strDto(); }
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_110.strDto(); }
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_111.strDto(); }
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { value = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.strDto(); }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { value = cmBTpmdfBDto.mskased_t.getuji_rank_cd_112.strDto(); }
                            break;
                    }
                }
            }
        }
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： setAboutRankColumn                                          */
    /*                                                                            */
    /*      書式                                                                  */
    /*      void setAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn ,         */
    /*                              int year, double *value)                      */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ランク関連情報設定                                            */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int     tbl_kbn ： テーブル種別                               */
    /*                                 ( 0 : 顧客制度情報 / 1 : 家族制度情報 )    */
    /*              int     ym_kbn ： 年／月区分 ( 1 : 月 / 2 : 年 )              */
    /*              int     colkbn  ： カラム種別                                 */
    /*                              ( 0 : ランクＵＰ対象金額 / 1 : ランクコード ) */
    /*              int     year    ： 取得対象年                                 */
    /*              int     month   ： 取得対象月(年／月区分=月の場合に参照)      */
    /*              double* value   ： 設定値                                     */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void setAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn, int year, int month, double value) {
        int year_buf;

        /* ランク関連項目を設定 */
        year_buf = year % 10;

        /* ランク関連項目を取得 */
        if ( tbl_kbn == DEF_TBLKBN_SEDO )
        {
            /* 対象テーブルは顧客制度 */

            if( ym_kbn == DEF_RANK_YEAR )
            {

                /* 年次情報設定 */
                switch( year_buf ) {
                    case 0:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_0.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 1:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_1.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 2:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_2.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 3:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_3.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 4:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_4.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 5:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_5.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 6:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_6.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 7:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_7.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 8:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_8.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                    case 9:
                        if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.nenji_rank_cd_9.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                        break;
                }
            }
            else
            {
                /* 月次情報設定 */
                if ( ( year % 2 ) == 0 )
                {
                    /* 偶数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_001.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_002.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_003.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_004.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_005.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_006.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_007.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_008.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_009.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_010.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_011.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_012.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                    }
                }
                else
                {
                    /* 奇数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_101.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_102.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_103.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_104.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_105.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_106.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_107.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_108.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_109.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_110.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_111.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKCD  )     {  cmBTpmdfBDto.h_ts_rank_info_data.getsuji_rank_cd_112.arr = value; }     /* 2022/12/29 MCCM初版 MOD */
                            break;
                    }
                }
            }
        }
        else
        {
            /* 対象テーブルは家族制度 */
            if( ym_kbn == DEF_RANK_YEAR )
            {

                /* 年次情報設定 */
                switch( year_buf ) {
                    case 0:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_0.arr = value; }
                        break;
                    case 1:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_1.arr = value; }
                        break;
                    case 2:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_2.arr = value; }
                        break;
                    case 3:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_3.arr = value; }
                        break;
                    case 4:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_4.arr = value; }
                        break;
                    case 5:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_5.arr = value; }
                        break;
                    case 6:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_6.arr = value; }
                        break;
                    case 7:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_7.arr = value; }
                        break;
                    case 8:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_8.arr = value; }
                        break;
                    case 9:
                        if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.arr = cmBTpmdfBDto.mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.floatVal() + value; }
                        if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.nenji_rank_cd_9.arr = value; }
                        break;
                }
            }
            else
            {
                /* 月次情報取得 */
                if ( ( year % 2 ) == 0 )
                {
                    /* 偶数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_001.arr = value; }
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_002.arr = value; }
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_003.arr = value; }
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_004.arr = value; }
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_005.arr = value; }
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_006.arr = value; }
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_007.arr = value; }
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_008.arr = value; }
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_009.arr = value; }
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_010.arr = value; }
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_011.arr = value; }
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_012.arr = value; }
                            break;
                    }
                }
                else
                {
                    /* 奇数年 */
                    switch( month ) {
                        case 1:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_101.arr = value; }
                            break;
                        case 2:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_102.arr = value; }
                            break;
                        case 3:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_103.arr = value; }
                            break;
                        case 4:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_104.arr = value; }
                            break;
                        case 5:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_105.arr = value; }
                            break;
                        case 6:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_106.arr = value; }
                            break;
                        case 7:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_107.arr = value; }
                            break;
                        case 8:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_108.arr = value; }
                            break;
                        case 9:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_109.arr = value; }
                            break;
                        case 10:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_110.arr = value; }
                            break;
                        case 11:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_111.arr = value; }
                            break;
                        case 12:
                            if ( colkbn == DEF_COLKBN_RANKUPGAKU  ) { cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.arr = cmBTpmdfBDto.mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.floatVal() + value; }
                            if ( colkbn == DEF_COLKBN_RANKCD  )     { cmBTpmdfBDto.mskased_t.getuji_rank_cd_112.arr = value; }
                            break;
                    }
                }
            }
        }
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： TsRiyoKanoPointUpdate                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  TsRiyoKanoPointUpdate(int uard_flg)                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TS利用可能ポイント情報更新処理                               */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*         int   uard_flg  売上連動フラグ                                     */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int TsRiyoKanoPointUpdate(int uard_flg) {
        StringDto wk_shurui = new StringDto(8);
        if (DBG_LOG) {
            C_DbgStart("TS利用可能ポイント情報更新処理");
        }

        strcpy(cmBTpmdfBDto.h_ts.saishu_koshin_programid, Cg_Program_Name);
        /* 2022/10/04 MCCM初版 ADD START */
        memset(wk_shurui, 0x00, sizeof(wk_shurui));
        if(uard_flg == 1){
            strcpy(wk_shurui, KOBAI);
        }else if(uard_flg == 2){
            strcpy(wk_shurui, HIKOBAI);
        }else if(uard_flg == 3){
            strcpy(wk_shurui, SONOHOKA);
        }else {
            strcpy(wk_shurui, SONOHOKA);
        }
        /* 2022/10/04 MCCM初版 ADD END */

        /* UPDATEする */
        /* 更新項目追加（最終買上日）*/
           /* EXEC SQL UPDATE TS利用可能ポイント情報
                        SET 利用可能ポイント             = 利用可能ポイント ,
                            最終買上日                   = GREATEST(最終買上日, :h_bat_date),
                            最終更新日                   = :h_bat_date,
                            最終更新日時                 = sysdate,
                            最終更新プログラムＩＤ       = :tsrkpoint_t.saishu_koshin_programid
                      WHERE 顧客番号                     = :ms_card_data.kokyaku_no;*/

        /*#if DBG_LOG
            C_DbgMsg( "*** UPDATE TS利用可能ポイント情報%s\n", "");
            C_DbgMsg( "*** sqlca.sqlcode[%d]\n", sqlca.sqlcode);
            C_DbgMsg( "*** h_bat_date[%d]\n", h_bat_date);
            C_DbgMsg( "*** point_shuse_data.riyo_kano_point_kagenti[%f]\n", point_shuse_data.riyo_kano_point_kagenti);
            C_DbgMsg( "*** point_shuse_data.data_ymd[%d]\n", point_shuse_data.data_ymd);
        #endif*/

        /* 2021/01/21 年度管理変更/期間限定ポイント追加対応 */
        /* ポイント失効退会顧客抽出 */
        sprintf(cmBTpmdfBDto.h_str_sql,
                "UPDATE TS利用可能ポイント情報 "+
                "SET    最終買上日              = GREATEST(最終買上日, ?),  "+
                "       最終更新日              = ?,  "+
                "       最終更新日時            =  sysdate(),  "+
                "       最終更新プログラムＩＤ  = ?,"+
                "       利用可能通常Ｐ%s        = 利用可能通常Ｐ%s + ?,  "+
                "       利用可能通常Ｐ%s        = 利用可能通常Ｐ%s + ?,  "+
                "       利用可能期間限定Ｐ%s    = 利用可能期間限定Ｐ%s + ?,    "+
                "       利用可能期間限定Ｐ%s    = 利用可能期間限定Ｐ%s + ?,    "+
                "       利用可能期間限定Ｐ%s    = 利用可能期間限定Ｐ%s + ?,    "+
                "       利用可能期間限定Ｐ%s    = 利用可能期間限定Ｐ%s + ?,    "+
                "       利用可能通常%sＰ%s      = 利用可能通常%sＰ%s + ?,     "+        /* 2022/10/04 MCCM初版 ADD */
                "       利用可能通常%sＰ%s      = 利用可能通常%sＰ%s + ?,     "+        /* 2022/10/04 MCCM初版 ADD */
                "       利用可能期間限定%sＰ%s  = 利用可能期間限定%sＰ%s + ?, " +       /* 2022/10/04 MCCM初版 ADD */
                "       利用可能期間限定%sＰ%s  = 利用可能期間限定%sＰ%s + ?, " +       /* 2022/10/04 MCCM初版 ADD */
                "       利用可能期間限定%sＰ%s  = 利用可能期間限定%sＰ%s + ?, " +       /* 2022/10/04 MCCM初版 ADD */
                "       利用可能期間限定%sＰ%s  = 利用可能期間限定%sＰ%s + ?  " +       /* 2022/10/04 MCCM初版 ADD */
                "WHERE 顧客番号= ? "
                ,ts_ser.ex_year_cd, ts_ser.ex_year_cd,
                ts_ser.this_year_cd, ts_ser.this_year_cd,
                ts_ser.month00, ts_ser.month00,
                ts_ser.month01, ts_ser.month01,
                ts_ser.month02, ts_ser.month02,
                ts_ser.month03, ts_ser.month03,
                wk_shurui,ts_ser.ex_year_cd, wk_shurui,ts_ser.ex_year_cd,                 /* 2022/10/04 MCCM初版 ADD */
                wk_shurui,ts_ser.this_year_cd, wk_shurui,ts_ser.this_year_cd,             /* 2022/10/04 MCCM初版 ADD */
                wk_shurui,ts_ser.month00, wk_shurui,ts_ser.month00,                       /* 2022/10/04 MCCM初版 ADD */
                wk_shurui,ts_ser.month01, wk_shurui,ts_ser.month01,                       /* 2022/10/04 MCCM初版 ADD */
                wk_shurui,ts_ser.month02, wk_shurui,ts_ser.month02,                       /* 2022/10/04 MCCM初版 ADD */
                wk_shurui,ts_ser.month03, wk_shurui,ts_ser.month03);                      /* 2022/10/04 MCCM初版 ADD */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** TS利用可能ポイント情報更新処理 *** 抽出SQL [%s]\n",
                cmBTpmdfBDto.h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        // EXEC SQL PREPARE sql_stat2 from :h_str_sql;
//        SqlstmDto sqlca = sqlcaManager.get("sql_stat2");
        sqlca.sql.arr = cmBTpmdfBDto.h_str_sql.strVal();
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** TS利用可能ポイント情報更新処理 *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("TS利用可能ポイント情報更新処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    cmBTpmdfBDto.h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 動的ＳＱＬ文実行 */
        /*EXEC SQL EXECUTE sql_stat2
        USING  :h_bat_date,
            :h_bat_date,
            :h_ts.saishu_koshin_programid,
            :point_shuse_data.tujo_point_kagenti_zennen,
            :point_shuse_data.tujo_point_kagenti_tonen,
            :point_shuse_data.kikan_point_kagenti_month0,
            :point_shuse_data.kikan_point_kagenti_month1,
            :point_shuse_data.kikan_point_kagenti_month2,
            :point_shuse_data.kikan_point_kagenti_month3,
            :point_shuse_data.tujo_point_kagenti_zennen,                          *//* 2022/10/04 MCCM初版 ADD *//*
            :point_shuse_data.tujo_point_kagenti_tonen,                           *//* 2022/10/04 MCCM初版 ADD *//*
            :point_shuse_data.kikan_point_kagenti_month0,                         *//* 2022/10/04 MCCM初版 ADD *//*
            :point_shuse_data.kikan_point_kagenti_month1,                         *//* 2022/10/04 MCCM初版 ADD *//*
            :point_shuse_data.kikan_point_kagenti_month2,                         *//* 2022/10/04 MCCM初版 ADD *//*
            :point_shuse_data.kikan_point_kagenti_month3,                         *//* 2022/10/04 MCCM初版 ADD *//*
            :ms_card_data.kokyaku_no;*/
        sqlca.restAndExecute(cmBTpmdfBDto.h_bat_date.longVal(),
                cmBTpmdfBDto.h_bat_date.longVal(),
                    cmBTpmdfBDto.h_ts.saishu_koshin_programid,
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_zennen.longVal(),
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_tonen.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month0.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month1.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month2.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month3.longVal(),
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_zennen.longVal(),
                cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_tonen.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month0.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month1.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month2.longVal(),
                cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month3.longVal(),
                cmBTpmdfBDto.ms_card_data.kokyaku_no.longVal());

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=%s", cmBTpmdfBDto.ms_card_data.kokyaku_no.arr);
            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* 処理を終了する */
        return C_const_OK;
        /*--- TsRiyoKanoPointUpdate処理 ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_getYear                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTpmdfB_getYear( unsigned short int *next_yyyy,                 */
    /*       unsigned short int *this_yyyy, unsigned short int *ex_yyyy)          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     年度を考慮した、翌年度、当年度、前年度を取得する。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    unsigned short int *next_yyyy     翌年度                                */
    /*    unsigned short int *this_yyyy     当年度                                */
    /*    unsigned short int *ex_yyyy       前年度                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_getYear(IntegerDto next_yyyy, IntegerDto this_yyyy, IntegerDto ex_yyyy) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_getYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */
        int wk_mm;                       /* バッチ処理月           */
        int wk_yyyy;                     /* バッチ処理年           */
        int wk_yyyymm;                   /* バッチ処理年月         */

        /* 文字列→数値に変換 */
        wk_yyyymm = atoi(bat_yyyymm);
        wk_mm = atoi(bat_mm);
        wk_yyyy = atoi(bat_yyyy);

        /* 新年度管理変更特別処理 */
        if ( wk_yyyymm <= 202203 ){
            this_yyyy.arr = 2021;
        }else{/* 年度管理変更後 */
            /* 月の当年度算出 */
            if ( wk_mm <=3){
                this_yyyy.arr = wk_yyyy-1; /* 年度切替前 */
            }else{
                this_yyyy.arr = wk_yyyy;   /* 年度切替後 */
            }
        }

        /* 前年度算出 */
        ex_yyyy.arr = this_yyyy.arr-1;
        /* 翌年度算出 */
        next_yyyy.arr = this_yyyy.arr+1;


        /*------------------------------------------------------------*/
        sprintf( buff, "翌年度:[%d]、当年度:[%d]、前年度:[%d]",
                next_yyyy, this_yyyy, ex_yyyy);
        C_DbgMsg("*** cmBTpmdfB_getYear *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_getYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTpmdfB_getYear処理 --------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmodfB_getYearCd                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*     voidt  cmBTpmdfB_getYearCd(unsigned short int year, char *year_cd)     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     引数の年度コードを取得する。                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    unsigned short int year           年度                                  */
    /*    char              *year_cd        年度コード                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_getYearCd(IntegerDto year, StringDto year_cd) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_getYearCd処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 年度コードを算出 */
        switch( year.arr % 5){
            case 1 : strcpy(year_cd, "１");
                break;
            case 2 : strcpy(year_cd, "２");
                break;
            case 3 : strcpy(year_cd, "３");
                break;
            case 4 : strcpy(year_cd, "４");
                break;
            case 0 : strcpy(year_cd, "０");
                break;
        }

        /*------------------------------------------------------------*/
        sprintf( buff, "年度:[%d]、年度コード:[%s]", year, year_cd);
        C_DbgMsg("*** cmBTpmdfB_getYearCd *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_getYearCd処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTpmdfB_getYearcd処理 ------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_getMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTpmdfB_getMonth(unsigned short int i_month,           */
    /*                                            char *month)                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の全角を取得する。                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*     char     *month       月（全角）                                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_getMonth(int i_month, StringDto month) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_getMonth処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                             /*  バッファ                  */
        /*--------------------------------------*/

        switch(i_month % 12){
            case 1 : strcpy(month, "０１");
                break;
            case 2 : strcpy(month, "０２");
                break;
            case 3 : strcpy(month, "０３");
                break;
            case 4 : strcpy(month, "０４");
                break;
            case 5 : strcpy(month, "０５");
                break;
            case 6 : strcpy(month, "０６");
                break;
            case 7 : strcpy(month, "０７");
                break;
            case 8 : strcpy(month, "０８");
                break;
            case 9 : strcpy(month, "０９");
                break;
            case 10 : strcpy(month, "１０");
                break;
            case 11 : strcpy(month, "１１");
                break;
            case 0 : strcpy(month, "１２");
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d], 格納月:[%s]", i_month, month);
        C_DbgMsg("*** cmBTpmdfB_getMonth *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_getMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTpmdfB_getMonth処理 -------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ：cmBTpmdfB_check_kijun_nendo                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpmdfB_check_kijun_nendo()                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               通常Ｐ基準年度チェック                                       */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               なし                                                         */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              5   :  チェックエラー                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_check_kijun_nendo() {
        /*------------------------------------------------------------*/
        C_DbgStart( "cmBTpmdfB_check_kijun_nendo処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int     rtn_cd;                 /* 関数戻り値                            */

        /* エラーチェック */
        if ( in_point_shusei.tujo_point_kijun_nendo != ts_ser.this_year.arr &&
                in_point_shusei.tujo_point_kijun_nendo != ts_ser.ex_year.arr){

            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = TUJO_POINT_KIJUN_NEDO_HANIGAI;

            /* 出力結果ファイル書込み */
            rtn_cd = cmBTpmdfB_write_err();
            if (rtn_cd !=  C_const_OK){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpmdfB_check_kijun_nendo処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return (C_const_NG);
            }
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTpmdfB_check_kijun_nendo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* エラー */
            return C_const_Stat_ELSERR;
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTpmdfB_check_kijun_nendo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /* 処理を終了する */
        return C_const_OK;
        /*--- cmBTpmdfB_check_kijun_nendo処理 ----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ：cmBTpmdfB_check_kijun_month                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpmdfB_check_kijun_month()                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               期間限定Ｐ基準月チェック                                     */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               なし                                                         */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              5   :  チェックエラー                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_check_kijun_month() {
        /*------------------------------------------------------------*/
        C_DbgStart( "cmBTpmdfB_check_kijun_month処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int  wk_this_month;              /* 当月                 */
        int  wk_ex_month;                /* 前月                 */
        int     rtn_cd;                 /* 関数戻り値                           */

        /* 当月、前月設定 */
        wk_this_month = atoi(bat_mm);
        if ( wk_this_month == 1 ){
            wk_ex_month = 12;
        }else{
            wk_ex_month = wk_this_month -1;
        }

        /* エラーチェック */
        if ( in_point_shusei.kikan_point_kijun_month != wk_this_month &&
                in_point_shusei.kikan_point_kijun_month != wk_ex_month){

            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = GENTEI_POINT_KIJUN_MONTH_HANIGAI;

            /* 出力結果ファイル書込み */
            rtn_cd = cmBTpmdfB_write_err();
            if (rtn_cd != C_const_OK){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpmdfB_check_kijun_month処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return (C_const_NG);
            }
            /* エラー */
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTpmdfB_check_kijun_month処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_Stat_ELSERR;
        }
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTpmdfB_check_kijun_month処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* 処理を終了する */
        return C_const_OK;
        /*--- cmBTpmdfB_check_kijun_month処理 ----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ：cmBTpmdfB_write_err                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpmdfB_write_err()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               チェックエラー時に出力結果ファイルに書き込む                 */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               なし                                                         */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_write_err() {
        /*------------------------------------------------------------*/
        C_DbgStart( "cmBTpmdfB_write_err処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    out_rec_buff = new StringDto(256);      /* 出力レコードバッファ                 */
        int     rtn_cd;                 /* 関数戻り値                           */

        sprintf(out_rec_buff,
                "%d,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,-,-,-,-,-,-,%d,%s\r\n",
                in_point_shusei.irai_kigyo,
                in_point_shusei.irai_tenpo,
                in_point_shusei.shusei_taisho_nen,
                in_point_shusei.shusei_taisho_tsuki,
                in_point_shusei.data_ymd,
                in_point_shusei.kigyo_cd,
                in_point_shusei.pid,
                in_point_shusei.rankup_taisho_kingaku_kagenti,
                in_point_shusei.tujo_point_kijun_nendo,
                in_point_shusei.tujo_point_kagenti_tonen,
                in_point_shusei.tujo_point_kagenti_zennen,
                in_point_shusei.kikan_point_kijun_month,
                in_point_shusei.kikan_point_kagenti_month0,
                in_point_shusei.kikan_point_kagenti_month1,
                in_point_shusei.kikan_point_kagenti_month2,
                in_point_shusei.kikan_point_kagenti_month3,
                in_point_shusei.riyu_code,
                cmBTpmdfBDto.point_shuse_data.shori_kekka,
                /* 2022/11/18 MCCM初版 ADD START */
                in_point_shusei.uard_flg,
                in_point_shusei.renban
                /* 2022/11/18 MCCM初版 ADD END */);

        /* 出力結果ファイルに書き込む */
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK){
            sprintf(out_format_buf, "fwrite, ファイル名 = %s", fl_o_name);
            APLOG_WT("903", 0, null, out_format_buf , 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTpmdfB_write_err処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTpmdfB_write_err処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* 処理を終了する */
        return C_const_OK;
        /*--- cmBTpmdfB_write_err処理 -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_check_over_years                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTpmdfB_check_over_years(struct CHECK_OVER_YM *c )              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     年度跨ぎ判定を行う                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct CHECK_OVER_YM *c   年度/月跨り判定用構造体ポインタ             */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_check_over_years(CHECK_OVER_YM c) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_check_over_years処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_bat_yyyymm;                     /* バッチ処理日（年月）   */
        //    unsigned short int wk_bat_yyyy;                 /* バッチ処理日（年）     */    /* 2022/12/29 MCCM初版 DEL */
        //    unsigned short int wk_batch_mm;                 /* バッチ処理日（月）     */    /* 2022/12/29 MCCM初版 ADD */
        StringDto buff = new StringDto(256);                                 /* バッファ               */

        wk_bat_yyyymm = atoi(bat_yyyymm);
        //    wk_bat_yyyy = atoi(bat_yyyy);                                                   /* 2022/12/29 MCCM初版 DEL */
        //    wk_batch_mm   = atoi(bat_mm);                                                   /* 2022/12/29 MCCM初版 ADD */

        /* 通常Ｐ基準年度（年跨り判定用構造体)設定 */
        c.kijun_nendo = ts_ser.this_year.arr;
        /* バッチ処理日が2022年03月（年度管理変更特別処理）までの場合 */
        if ( wk_bat_yyyymm <= 202203 ){
            /*c->tujo_point_kagenti_zennen = 0 */
            c.tujo_point_kagenti_tonen = in_point_shusei.tujo_point_kagenti_zennen
                    +in_point_shusei.tujo_point_kagenti_tonen;
            /* 2021/02/19 内結故障対応（SI_0015） Start */
            /* 通常Ｐ基準年度が設定なし(0)の場合、処理日付（年度）を設定  */
            if(in_point_shusei.tujo_point_kijun_nendo == 0){
                in_point_shusei.tujo_point_kijun_nendo = ts_ser.this_year.arr;
            }
            /* 2021/02/19 内結故障対応（SI_0015） End */
        }
        /* 通常Ｐ基準年度が設定なし(0)の場合 */
        else if( in_point_shusei.tujo_point_kijun_nendo == 0){
            c.tujo_point_kagenti_zennen =in_point_shusei.tujo_point_kagenti_zennen;
            c.tujo_point_kagenti_tonen = in_point_shusei.tujo_point_kagenti_tonen;
            /* 2021/02/19 内結故障対応（SI_0015） Start */
            in_point_shusei.tujo_point_kijun_nendo = ts_ser.this_year.arr;
            /* 2021/02/19 内結故障対応（SI_0015） End */
        }
    /* 通常Ｐ基準年度が設定あり かつ
        年度跨り（バッチ処理日＞通常Ｐ基準年度）の場合 */
        else if ( in_point_shusei.tujo_point_kijun_nendo != 0 &&
                ts_ser.this_year.arr > in_point_shusei.tujo_point_kijun_nendo ){
            /*c.tujo_point_kagenti_zennen = 0 */
            c.tujo_point_kagenti_tonen = in_point_shusei.tujo_point_kagenti_zennen
                    +in_point_shusei.tujo_point_kagenti_tonen;
        }
        /* 年度跨りなしの場合 */
        else{
            c.tujo_point_kagenti_zennen =in_point_shusei.tujo_point_kagenti_zennen;
            c.tujo_point_kagenti_tonen = in_point_shusei.tujo_point_kagenti_tonen;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "年度跨り判定後 通常Ｐ基準年度=[%d]  "+
                "通常Ｐ加減値（前年）=[%10.0f], 通常Ｐ加減値（当年）=[%10.0f]",
                c.kijun_nendo ,c.tujo_point_kagenti_zennen,
                c.tujo_point_kagenti_tonen);
        C_DbgMsg("*** cmBTpmdfB_check_over_years *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_check_over_years処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTpmdfB_check_over_years処理 -----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_check_over_months                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTpmdfB_check_over_months(struct CHECK_OVER_YM *c )             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     月跨ぎ判定を行う                                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct CHECK_OVER_YM *c   年度/月跨り判定用構造体ポインタ             */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_check_over_months(CHECK_OVER_YM c) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_check_over_months処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_bat_mm;                         /* バッチ処理日（月）     */
        StringDto buff = new StringDto(256);                                 /* バッファ               */

        wk_bat_mm = atoi(bat_mm);

        /* 期間限定Ｐ基準月（月跨り判定用構造体)設定 */
        c.kijun_month = atoi(bat_mm);

        /* 期間限定Ｐ基準月が設定なし(0)の場合、当月に設定 */
        if ( in_point_shusei.kikan_point_kijun_month == 0 ){
            in_point_shusei.kikan_point_kijun_month = wk_bat_mm;
        }

        /* バッチ処理日（月）＞期間限定Ｐ基準月（月跨り）の場合 */
        if ( wk_bat_mm >in_point_shusei.kikan_point_kijun_month ||
                ( wk_bat_mm == 1 && in_point_shusei.kikan_point_kijun_month == 12)){
            c.kikan_point_kagenti_month0
                    = in_point_shusei.kikan_point_kagenti_month0
                    + in_point_shusei.kikan_point_kagenti_month1;
            c.kikan_point_kagenti_month1
                    = in_point_shusei.kikan_point_kagenti_month2;
            c.kikan_point_kagenti_month2
                    = in_point_shusei.kikan_point_kagenti_month3;
            /*c->kikan_point_kagenti_month3 = 0 *//* ポイント修正に項目なし */
        }
        /* 月跨りなしの場合 */
        else{
            c.kikan_point_kagenti_month0 =
                    in_point_shusei.kikan_point_kagenti_month0;
            c.kikan_point_kagenti_month1 =
                    in_point_shusei.kikan_point_kagenti_month1;
            c.kikan_point_kagenti_month2 =
                    in_point_shusei.kikan_point_kagenti_month2;
            c.kikan_point_kagenti_month3 =
                    in_point_shusei.kikan_point_kagenti_month3;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "月跨り判定後 期間限定Ｐ基準月=[%d] 期間限定Ｐ加減値（当月）=[%10.0f], 期間限定Ｐ加減値（1ヶ月後）=[%10.0f]",
                c.kijun_month, c.kikan_point_kagenti_month0,
                c.kikan_point_kagenti_month1);
        C_DbgMsg("*** cmBTpmdfB_check_over_months *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_check_over_months処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTpmdfB_check_over_months処理 ----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_getTSriyo                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int    cmBTpmdfB_getTSriyo()                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報取得                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_getTSriyo() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_getTSriyo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(2048);                  /* バッファ               */

        /* 初期化 */
        memset(cmBTpmdfBDto.h_ts, 0x00, sizeof(cmBTpmdfBDto.h_ts));

        /* ポイント失効退会顧客抽出 */
        sprintf(cmBTpmdfBDto.h_str_sql,
                "SELECT  /*+ INDEX(TS利用可能ポイント情報 PKTSPTPTRY00) */ " +
                "    入会会社コードＭＣＣ,  " +                                                               /* 2022/09/26 MCCM初版 MOD */
                "    入会店舗ＭＣＣ,  "      +                                                                /* 2022/09/26 MCCM初版 MOD */
                "    発券企業コード,  "+
                "    発券店舗,        "+
                "    利用可能通常Ｐ%s,  "+
                "    利用可能通常Ｐ%s,  "+
                "    利用可能通常Ｐ%s,  "+
                "    利用可能期間限定Ｐ%s,  "+
                "    利用可能期間限定Ｐ%s,  "+
                "    利用可能期間限定Ｐ%s,  "+
                "    利用可能期間限定Ｐ%s,  "+
                "    利用可能期間限定Ｐ%s  "+
                "FROM  TS利用可能ポイント情報 "+
                "WHERE 顧客番号= ? "
                ,ts_ser.ex_year_cd, ts_ser.this_year_cd,
                ts_ser.next_year_cd, ts_ser.month00, ts_ser.month01,
                ts_ser.month02, ts_ser.month03, ts_ser.month04);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTpmdfB_geTSriyo *** 抽出SQL [%s]\n", cmBTpmdfBDto.h_str_sql);
        /*------------------------------------------------------------*/

        sqlca.sql.arr = cmBTpmdfBDto.h_str_sql.strVal();
        /* 動的ＳＱＬ文を解析する */
        // EXEC SQL PREPARE sql_stat1 from :h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpmdfB_getTSriyo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTpmdfB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    cmBTpmdfBDto.h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        //EXEC SQL DECLARE CUR_SQL01 CURSOR FOR sql_stat1;
        sqlca.declare();

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL01 USING :ms_card_data.kokyaku_no;
        sqlca.open(cmBTpmdfBDto.ms_card_data.kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTpmdfB_getTSriyo *** "+
                "TS利用可能ポイント情報 CURSOR OPEN "+
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "顧客番号=[%s]", cmBTpmdfBDto.ms_card_data.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "OPEN CURSOR(CUR_SQL01)",
                    sqlca.sqlcode, "TS利用可能ポイント情報情報",
                    buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpmdfB_getTSriyo *** %s\n","カーソルエラー");
            C_DbgEnd("cmBTpmdfB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }
        /* カーソルフェッチ */
        /*
        EXEC SQL FETCH CUR_SQL01
        INTO :h_ts.nyukai_kigyo_cd,        *//* 入会会社コードＭＣＣ               *//*                     *//* 2022/09/26 MCCM初版 MOD *//*
          :h_ts.nyukai_tenpo,           *//* 入会店舗ＭＣＣ                     *//*                     *//* 2022/09/26 MCCM初版 MOD *//*
          :h_ts.hakken_kigyo_cd,        *//* 発券企業コード                     *//*
          :h_ts.hakken_tenpo,           *//* 発券店舗                           *//*
          :h_ts.zennen_p,               *//* 利用可能通常Ｐ前年度               *//*
          :h_ts.tonen_p,                *//* 利用可能通常Ｐ当年度               *//*
          :h_ts.yokunen_p,              *//* 利用可能通常Ｐ翌年度               *//*
          :h_ts.kikan_p01,              *//* 利用可能期間限定Ｐ当月             *//*
          :h_ts.kikan_p02,              *//* 利用可能期間限定Ｐ当月＋１         *//*
          :h_ts.kikan_p03,              *//* 利用可能期間限定Ｐ当月＋２         *//*
          :h_ts.kikan_p04,              *//* 利用可能期間限定Ｐ当月＋３         *//*
          :h_ts.kikan_p05;              *//* 利用可能期間限定Ｐ当月＋４         *//*
        */
        sqlca.fetch();
        sqlca.recData(cmBTpmdfBDto.h_ts.nyukai_kigyo_cd,
                cmBTpmdfBDto.h_ts.nyukai_tenpo,
                cmBTpmdfBDto.h_ts.hakken_kigyo_cd,
                cmBTpmdfBDto.h_ts.hakken_tenpo,
                cmBTpmdfBDto.h_ts.zennen_p,
                cmBTpmdfBDto.h_ts.tonen_p,
                cmBTpmdfBDto.h_ts.yokunen_p,
                cmBTpmdfBDto.h_ts.kikan_p01,
                cmBTpmdfBDto.h_ts.kikan_p02,
                cmBTpmdfBDto.h_ts.kikan_p03,
                cmBTpmdfBDto.h_ts.kikan_p04,
                cmBTpmdfBDto.h_ts.kikan_p05         );


        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf(buff, "顧客番号=[%s]", cmBTpmdfBDto.ms_card_data.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "FETCH(CUR_SQL01)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    buff, 0, 0);
            /* カーソルクローズ */
            //EXEC SQL CLOSE CUR_SQL01;
            sqlca.curse_close();
//            sqlcaManager.close(sqlca);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpmdfB_getTSriyo *** %s\n","フェッチエラー");
            C_DbgEnd("cmBTpmdfB_getTSriyo 処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* カーソルクローズ */
        //EXEC SQL CLOSE CUR_SQL01;
//        sqlcaManager.close(sqlca);
        sqlca.curse_close();

        /*------------------------------------------------------------*/
        sprintf(buff,
                "入会企業コード:[%d],入会店舗:[%d],発券企業コード:[%d],発券店舗:[%d]," +
                "利用可能通常Ｐ%s:[%10.0f],利用可能通常Ｐ%s:[%10.0f]," +
                "利用可能通常Ｐ%s:[%10.0f]," +
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f]," +
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f]," +
                "利用可能期間限定Ｐ%s:[%10.0f]",
                cmBTpmdfBDto.h_ts.nyukai_kigyo_cd.intVal(), cmBTpmdfBDto.h_ts.nyukai_tenpo.intVal(), cmBTpmdfBDto.h_ts.hakken_kigyo_cd.intVal(),
                cmBTpmdfBDto.h_ts.hakken_tenpo.intVal(),
                ts_ser.ex_year_cd, cmBTpmdfBDto.h_ts.zennen_p.floatVal(), ts_ser.this_year_cd, cmBTpmdfBDto.h_ts.tonen_p.floatVal(),
                ts_ser.next_year_cd, cmBTpmdfBDto.h_ts.yokunen_p.floatVal(), ts_ser.month00,
                cmBTpmdfBDto.h_ts.kikan_p01.floatVal(), ts_ser.month01, cmBTpmdfBDto.h_ts.kikan_p02.floatVal(), ts_ser.month02,
                cmBTpmdfBDto.h_ts.kikan_p03.floatVal(), ts_ser.month03, cmBTpmdfBDto.h_ts.kikan_p04.floatVal(), ts_ser.month04,
                cmBTpmdfBDto.h_ts.kikan_p05.floatVal());
        C_DbgMsg("*** cmBTpmdfB_getTSriyo *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_getTSriyo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK ;
        /*--- cmBTpmdfB_getTSriyo処理 ------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ：cmBTpmdfB_check_updval                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpmdfB_check_updval(struct CHECK_OVER_YM check_over_ym)    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          期間限定Ｐの更新値がマイナスにならないか確認する                  */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              struct CHECK_OVER_YM check_over_ym 年度/月跨ぎ判定構造体      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              5   :  チェックエラー                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_check_updval(CHECK_OVER_YM check_over_ym) {
        /*------------------------------------------------------------*/
        C_DbgStart( "cmBTpmdfB_check_updval処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int     rtn_cd;                 /* 関数戻り値                           */

        /* エラーチェック */
        if ( cmBTpmdfBDto.h_ts.kikan_p01.floatVal() + check_over_ym.kikan_point_kagenti_month0 < 0 ||
                cmBTpmdfBDto.h_ts.kikan_p02.floatVal() + check_over_ym.kikan_point_kagenti_month1 <0 ||
                cmBTpmdfBDto.h_ts.kikan_p03.floatVal() + in_point_shusei.kikan_point_kagenti_month2 < 0 ||
                cmBTpmdfBDto.h_ts.kikan_p04.floatVal() + in_point_shusei.kikan_point_kagenti_month3 < 0){

            cmBTpmdfBDto.point_shuse_data.shori_kekka.arr = GENTEI_POINT_KAGENTI_FUSEI;

            /* 出力結果ファイル書込み */
            rtn_cd = cmBTpmdfB_write_err();
            if (rtn_cd != C_const_OK){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpmdfB_check_updval処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return (C_const_NG);
            }
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTpmdfB_check_updval処理", 0, 0, 0);
            /* エラー */
            return C_const_Stat_ELSERR;
            /*------------------------------------------------------------*/
        }
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTpmdfB_check_updval処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* 処理を終了する */
        return C_const_OK;
        /*--- cmBTpmdfB_check_updval処理 ---------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_setMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTpmdfB_setMonth(unsigned short int i_month,           */
    /*                                      double kikan_p)                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の                                                          */
    /*    ポイント日別情報．更新付与期間限定ＰＭＭに値を設定する。                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*     double   kikan_p     期間限定ポイント                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_setMonth(int i_month, double kikan_p) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_setMonth処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(40);                             /*  バッファ                  */

        switch(i_month % 12){
            case 1 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point01.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０１         */
                break;
            case 2 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point02.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０２         */
                break;
            case 3 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point03.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０３         */
                break;
            case 4 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point04.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０４         */
                break;
            case 5 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point05.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０５         */
                break;
            case 6 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point06.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０６         */
                break;
            case 7 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point07.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０７         */
                break;
            case 8 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point08.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０８         */
                break;
            case 9 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point09.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ０９         */
                break;
            case 10 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point10.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ１０         */
                break;
            case 11 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point11.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ１１         */
                break;
            case 0 :
                cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point12.arr = kikan_p;
                /* ポイント日別情報．更新付与期間限定Ｐ１２         */
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d]", i_month);
        C_DbgMsg("*** cmBTpmdfB_setMonth *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_setMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTpmdfB_setMonth処理 -------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpmdfB_setKikanPoint                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTpmdfB_setKikanPoint(unsigned short int i_month,      */
    /*                                      double kikan_p)                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の                                                          */
    /*    期間限定情報．付与期間限定ＰＭＭに値を設定する。                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*     double   kikan_p     期間限定ポイント                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTpmdfB_setKikanPoint(int i_month, double kikan_p) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTpmdfB_setKikanPoint処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(40);                             /*  バッファ                  */

        switch(i_month % 12){
            case 1 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point01.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０１         */
                break;
            case 2 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point02.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０２         */
                break;
            case 3 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point03.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０３         */
                break;
            case 4 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point04.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０４         */
                break;
            case 5 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point05.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０５         */
                break;
            case 6 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point06.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０６         */
                break;
            case 7 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point07.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０７         */
                break;
            case 8 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point08.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０８         */
                break;
            case 9 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point09.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ０９         */
                break;
            case 10 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point10.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ１０         */
                break;
            case 11 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point11.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ１１         */
                break;
            case 0 :
                cmBTpmdfBDto.kikan_point_data.fuyo_kikan_gentei_point12.arr = kikan_p;
                /* 期間限定ポイント情報．付与期間限定Ｐ１２         */
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d]", i_month);
        C_DbgMsg("*** cmBTpmdfB_setKikanPoint *** %s\n", buff);
        C_DbgEnd("cmBTpmdfB_setKikanPoint処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTpmdfB_setKikanPoint処理 -------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ：cmBTpmdfB_write_file                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTpmdfB_write_file()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ポイント修正結果を出力結果ファイルに書き込む                 */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               なし                                                         */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int cmBTpmdfB_write_file() {
        /*------------------------------------------------------------*/
        C_DbgStart( "cmBTpmdfB_write_file処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    out_rec_buff = new StringDto(320);      /* 出力レコードバッファ                  */
        int     rtn_cd;                 /* 関数戻り値                            */
        long wk_riyo_kano_tujop_zennen; /* 利用可能通常Ｐ（前年）*/
        long wk_riyo_kano_tujop_tounen; /* 利用可能通常Ｐ（当年）*/
        long wk_riyo_kano_kikanp_mm0;  /* 利用可能期間限定Ｐ（当月）*/
        long wk_riyo_kano_kikanp_mm1;  /*利用可能期間限定Ｐ(1ヶ月後)*/
        long wk_riyo_kano_kikanp_mm2;  /*利用可能期間限定Ｐ(2ヶ月後)*/
        long wk_riyo_kano_kikanp_mm3;  /*利用可能期間限定Ｐ(3ヶ月後)*/

        wk_riyo_kano_tujop_zennen
                = cmBTpmdfBDto.h_ts.zennen_p.longVal() + cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_zennen.longVal();
        wk_riyo_kano_tujop_tounen
                = cmBTpmdfBDto.h_ts.tonen_p.longVal() + cmBTpmdfBDto.point_shuse_data.tujo_point_kagenti_tonen.longVal();
        wk_riyo_kano_kikanp_mm0
                = cmBTpmdfBDto.h_ts.kikan_p01.longVal() + cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month0.longVal();
        wk_riyo_kano_kikanp_mm1
                = cmBTpmdfBDto.h_ts.kikan_p02.longVal() + cmBTpmdfBDto.point_shuse_data.kikan_point_kagenti_month1.longVal();
        wk_riyo_kano_kikanp_mm2
                = cmBTpmdfBDto.h_ts.kikan_p03.longVal() + in_point_shusei.kikan_point_kagenti_month2;
        wk_riyo_kano_kikanp_mm3
                = cmBTpmdfBDto.h_ts.kikan_p04.longVal() + in_point_shusei.kikan_point_kagenti_month3;

        sprintf(out_rec_buff,
                "%d,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d," +
                "%d,%d,%d,%d,%d,%d,%d,%d,%d,%s\r\n",                        /* 2022/11/18 MCCM初版 MOD */
                in_point_shusei.irai_kigyo,
                in_point_shusei.irai_tenpo,
                in_point_shusei.shusei_taisho_nen,
                in_point_shusei.shusei_taisho_tsuki,
                in_point_shusei.data_ymd,
                in_point_shusei.kigyo_cd,
                in_point_shusei.pid,
                in_point_shusei.rankup_taisho_kingaku_kagenti,
                in_point_shusei.tujo_point_kijun_nendo,
                in_point_shusei.tujo_point_kagenti_tonen,
                in_point_shusei.tujo_point_kagenti_zennen,
                in_point_shusei.kikan_point_kijun_month,
                in_point_shusei.kikan_point_kagenti_month0,
                in_point_shusei.kikan_point_kagenti_month1,
                in_point_shusei.kikan_point_kagenti_month2,
                in_point_shusei.kikan_point_kagenti_month3,
                in_point_shusei.riyu_code,
                cmBTpmdfBDto.point_shuse_data.shori_kekka,
                /* 2021/02/19 内結故障対応（SI_0015） Start */
                wk_riyo_kano_tujop_tounen,
                wk_riyo_kano_tujop_zennen,
                /* 2021/02/19 内結故障対応（SI_0015） End  */
                wk_riyo_kano_kikanp_mm0, wk_riyo_kano_kikanp_mm1,
                wk_riyo_kano_kikanp_mm2, wk_riyo_kano_kikanp_mm3,
                /* 2022/11/18 MCCM初版 ADD START */
                in_point_shusei.uard_flg,
                in_point_shusei.renban
                /* 2022/11/18 MCCM初版 ADD END */
        );


        /* 出力結果ファイルに書き込む */
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK){
            sprintf(out_format_buf, "fwrite, ファイル名 = %s", fl_o_name);
            APLOG_WT("903", 0, null, out_format_buf , 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTpmdfB_write_file処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTpmdfB_write_file処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* 処理を終了する */
        return C_const_OK;
        /*--- cmBTpmdfB_write_file処理 -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： GetTsRankInfo                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int GetTsRankInfo(TS_RANK_INFO_TBL  *tsRankInfo, int *status)         */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              TSランク情報取得                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*          TS_RANK_INFO_TBL *tsRankInfo ： TSランク情報構造体取得パラメータ  */
    /*          int              *status     ： 結果ステータス                    */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int GetTsRankInfo(TS_RANK_INFO_TBL tsRankInfo, IntegerDto status) {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */


        /* ホスト変数 */
        //EXEC SQL BEGIN DECLARE SECTION;

        TS_RANK_INFO_TBL h_ts_rank_info_buff = new TS_RANK_INFO_TBL(); /* TSランク情報バッファ */
        // EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("GetTsRankInfo : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (tsRankInfo == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("GetTsRankInfo : %s\n", "PRMERR(NULL)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* 出力エリアのクリア */
        tsRankInfo.nenji_rank_cd_0.arr = 0;                               /* 年次ランクコード０             */
        tsRankInfo.nenji_rank_cd_1.arr = 0;                               /* 年次ランクコード１             */
        tsRankInfo.nenji_rank_cd_2.arr = 0;                               /* 年次ランクコード２             */
        tsRankInfo.nenji_rank_cd_3.arr = 0;                               /* 年次ランクコード３             */
        tsRankInfo.nenji_rank_cd_4.arr = 0;                               /* 年次ランクコード４             */
        tsRankInfo.nenji_rank_cd_5.arr = 0;                               /* 年次ランクコード５             */
        tsRankInfo.nenji_rank_cd_6.arr = 0;                               /* 年次ランクコード６             */
        tsRankInfo.nenji_rank_cd_7.arr = 0;                               /* 年次ランクコード７             */
        tsRankInfo.nenji_rank_cd_8.arr = 0;                               /* 年次ランクコード８             */
        tsRankInfo.nenji_rank_cd_9.arr = 0;                               /* 年次ランクコード９             */
        tsRankInfo.nenkan_rankup_taisho_kingaku_0.arr = 0;                /* 年間ランクＵＰ対象金額０       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_1.arr = 0;                /* 年間ランクＵＰ対象金額１       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_2.arr = 0;                /* 年間ランクＵＰ対象金額２       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_3.arr = 0;                /* 年間ランクＵＰ対象金額３       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_4.arr = 0;                /* 年間ランクＵＰ対象金額４       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_5.arr = 0;                /* 年間ランクＵＰ対象金額５       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_6.arr = 0;                /* 年間ランクＵＰ対象金額６       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_7.arr = 0;                /* 年間ランクＵＰ対象金額７       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_8.arr = 0;                /* 年間ランクＵＰ対象金額８       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_9.arr = 0;                /* 年間ランクＵＰ対象金額９       */
        tsRankInfo.getsuji_rank_cd_004.arr = 0;                           /* 月次ランクコード００４         */
        tsRankInfo.getsuji_rank_cd_005.arr = 0;                           /* 月次ランクコード００５         */
        tsRankInfo.getsuji_rank_cd_006.arr = 0;                           /* 月次ランクコード００６         */
        tsRankInfo.getsuji_rank_cd_007.arr = 0;                           /* 月次ランクコード００７         */
        tsRankInfo.getsuji_rank_cd_008.arr = 0;                           /* 月次ランクコード００８         */
        tsRankInfo.getsuji_rank_cd_009.arr = 0;                           /* 月次ランクコード００９         */
        tsRankInfo.getsuji_rank_cd_010.arr = 0;                           /* 月次ランクコード０１０         */
        tsRankInfo.getsuji_rank_cd_011.arr = 0;                           /* 月次ランクコード０１１         */
        tsRankInfo.getsuji_rank_cd_012.arr = 0;                           /* 月次ランクコード０１２         */
        tsRankInfo.getsuji_rank_cd_001.arr = 0;                           /* 月次ランクコード００１         */
        tsRankInfo.getsuji_rank_cd_002.arr = 0;                           /* 月次ランクコード００２         */
        tsRankInfo.getsuji_rank_cd_003.arr = 0;                           /* 月次ランクコード００３         */
        tsRankInfo.getsuji_rank_cd_104.arr = 0;                           /* 月次ランクコード１０４         */
        tsRankInfo.getsuji_rank_cd_105.arr = 0;                           /* 月次ランクコード１０５         */
        tsRankInfo.getsuji_rank_cd_106.arr = 0;                           /* 月次ランクコード１０６         */
        tsRankInfo.getsuji_rank_cd_107.arr = 0;                           /* 月次ランクコード１０７         */
        tsRankInfo.getsuji_rank_cd_108.arr = 0;                           /* 月次ランクコード１０８         */
        tsRankInfo.getsuji_rank_cd_109.arr = 0;                           /* 月次ランクコード１０９         */
        tsRankInfo.getsuji_rank_cd_110.arr = 0;                           /* 月次ランクコード１１０         */
        tsRankInfo.getsuji_rank_cd_111.arr = 0;                           /* 月次ランクコード１１１         */
        tsRankInfo.getsuji_rank_cd_112.arr = 0;                           /* 月次ランクコード１１２         */
        tsRankInfo.getsuji_rank_cd_101.arr = 0;                           /* 月次ランクコード１０１         */
        tsRankInfo.getsuji_rank_cd_102.arr = 0;                           /* 月次ランクコード１０２         */
        tsRankInfo.getsuji_rank_cd_103.arr = 0;                           /* 月次ランクコード１０３         */
        tsRankInfo.gekkan_rankup_taisho_kingaku_004.arr = 0;              /* 月間ランクＵＰ対象金額００４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_005.arr = 0;              /* 月間ランクＵＰ対象金額００５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_006.arr = 0;              /* 月間ランクＵＰ対象金額００６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_007.arr = 0;              /* 月間ランクＵＰ対象金額００７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_008.arr = 0;              /* 月間ランクＵＰ対象金額００８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_009.arr = 0;              /* 月間ランクＵＰ対象金額００９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_010.arr = 0;              /* 月間ランクＵＰ対象金額０１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_011.arr = 0;              /* 月間ランクＵＰ対象金額０１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_012.arr = 0;              /* 月間ランクＵＰ対象金額０１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_001.arr = 0;              /* 月間ランクＵＰ対象金額００１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_002.arr = 0;              /* 月間ランクＵＰ対象金額００２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_003.arr = 0;              /* 月間ランクＵＰ対象金額００３   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_104.arr = 0;              /* 月間ランクＵＰ対象金額１０４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_105.arr = 0;              /* 月間ランクＵＰ対象金額１０５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_106.arr = 0;              /* 月間ランクＵＰ対象金額１０６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_107.arr = 0;              /* 月間ランクＵＰ対象金額１０７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_108.arr = 0;              /* 月間ランクＵＰ対象金額１０８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_109.arr = 0;              /* 月間ランクＵＰ対象金額１０９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_110.arr = 0;              /* 月間ランクＵＰ対象金額１１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_111.arr = 0;              /* 月間ランクＵＰ対象金額１１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_112.arr = 0;              /* 月間ランクＵＰ対象金額１１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_101.arr = 0;              /* 月間ランクＵＰ対象金額１０１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_102.arr = 0;              /* 月間ランクＵＰ対象金額１０２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_103.arr = 0;              /* 月間ランクＵＰ対象金額１０３   */
        tsRankInfo.gekkan_premium_point_kingaku_004.arr = 0;              /* 月間プレミアムポイント数００４ */
        tsRankInfo.gekkan_premium_point_kingaku_005.arr = 0;              /* 月間プレミアムポイント数００５ */
        tsRankInfo.gekkan_premium_point_kingaku_006.arr = 0;              /* 月間プレミアムポイント数００６ */
        tsRankInfo.gekkan_premium_point_kingaku_007.arr = 0;              /* 月間プレミアムポイント数００７ */
        tsRankInfo.gekkan_premium_point_kingaku_008.arr = 0;              /* 月間プレミアムポイント数００８ */
        tsRankInfo.gekkan_premium_point_kingaku_009.arr = 0;              /* 月間プレミアムポイント数００９ */
        tsRankInfo.gekkan_premium_point_kingaku_010.arr = 0;              /* 月間プレミアムポイント数０１０ */
        tsRankInfo.gekkan_premium_point_kingaku_011.arr = 0;              /* 月間プレミアムポイント数０１１ */
        tsRankInfo.gekkan_premium_point_kingaku_012.arr = 0;              /* 月間プレミアムポイント数０１２ */
        tsRankInfo.gekkan_premium_point_kingaku_001.arr = 0;              /* 月間プレミアムポイント数００１ */
        tsRankInfo.gekkan_premium_point_kingaku_002.arr = 0;              /* 月間プレミアムポイント数００２ */
        tsRankInfo.gekkan_premium_point_kingaku_003.arr = 0;              /* 月間プレミアムポイント数００３ */
        tsRankInfo.gekkan_premium_point_kingaku_104.arr = 0;              /* 月間プレミアムポイント数１０４ */
        tsRankInfo.gekkan_premium_point_kingaku_105.arr = 0;              /* 月間プレミアムポイント数１０５ */
        tsRankInfo.gekkan_premium_point_kingaku_106.arr = 0;              /* 月間プレミアムポイント数１０６ */
        tsRankInfo.gekkan_premium_point_kingaku_107.arr = 0;              /* 月間プレミアムポイント数１０７ */
        tsRankInfo.gekkan_premium_point_kingaku_108.arr = 0;              /* 月間プレミアムポイント数１０８ */
        tsRankInfo.gekkan_premium_point_kingaku_109.arr = 0;              /* 月間プレミアムポイント数１０９ */
        tsRankInfo.gekkan_premium_point_kingaku_110.arr = 0;              /* 月間プレミアムポイント数１１０ */
        tsRankInfo.gekkan_premium_point_kingaku_111.arr = 0;              /* 月間プレミアムポイント数１１１ */
        tsRankInfo.gekkan_premium_point_kingaku_112.arr = 0;              /* 月間プレミアムポイント数１１２ */
        tsRankInfo.gekkan_premium_point_kingaku_101.arr = 0;              /* 月間プレミアムポイント数１０１ */
        tsRankInfo.gekkan_premium_point_kingaku_102.arr = 0;              /* 月間プレミアムポイント数１０２ */
        tsRankInfo.gekkan_premium_point_kingaku_103.arr = 0;              /* 月間プレミアムポイント数１０３ */
        tsRankInfo.saishu_koshin_ymd.arr = 0;                             /* 最終更新日                     */
        tsRankInfo.saishu_koshin_ymdhms.arr = 0;                          /* 最終更新日時                   */
        strcpy(tsRankInfo.saishu_koshin_programid,
                "                    ");                                /* 最終更新プログラムＩＤ         */

        /* ホスト変数を編集する */
        //memset(&h_ts_rank_info_buff, 0x00, sizeof(TS_RANK_INFO_TBL));
        memcpy(h_ts_rank_info_buff.kokyaku_no, tsRankInfo.kokyaku_no,tsRankInfo.kokyaku_no.len);
        h_ts_rank_info_buff.kokyaku_no.len = tsRankInfo.kokyaku_no.len;

        /* ＳＱＬを実行する */
        /*EXEC SQL SELECT NVL(年次ランクコード０,0),
        NVL(年次ランクコード１,0),
                NVL(年次ランクコード２,0),
                NVL(年次ランクコード３,0),
                NVL(年次ランクコード４,0),
                NVL(年次ランクコード５,0),
                NVL(年次ランクコード６,0),
                NVL(年次ランクコード７,0),
                NVL(年次ランクコード８,0),
                NVL(年次ランクコード９,0),
                NVL(年間ランクＵＰ対象金額０,0),
                NVL(年間ランクＵＰ対象金額１,0),
                NVL(年間ランクＵＰ対象金額２,0),
                NVL(年間ランクＵＰ対象金額３,0),
                NVL(年間ランクＵＰ対象金額４,0),
                NVL(年間ランクＵＰ対象金額５,0),
                NVL(年間ランクＵＰ対象金額６,0),
                NVL(年間ランクＵＰ対象金額７,0),
                NVL(年間ランクＵＰ対象金額８,0),
                NVL(年間ランクＵＰ対象金額９,0),
                NVL(月次ランクコード００４,0),
                NVL(月次ランクコード００５,0),
                NVL(月次ランクコード００６,0),
                NVL(月次ランクコード００７,0),
                NVL(月次ランクコード００８,0),
                NVL(月次ランクコード００９,0),
                NVL(月次ランクコード０１０,0),
                NVL(月次ランクコード０１１,0),
                NVL(月次ランクコード０１２,0),
                NVL(月次ランクコード００１,0),
                NVL(月次ランクコード００２,0),
                NVL(月次ランクコード００３,0),
                NVL(月次ランクコード１０４,0),
                NVL(月次ランクコード１０５,0),
                NVL(月次ランクコード１０６,0),
                NVL(月次ランクコード１０７,0),
                NVL(月次ランクコード１０８,0),
                NVL(月次ランクコード１０９,0),
                NVL(月次ランクコード１１０,0),
                NVL(月次ランクコード１１１,0),
                NVL(月次ランクコード１１２,0),
                NVL(月次ランクコード１０１,0),
                NVL(月次ランクコード１０２,0),
                NVL(月次ランクコード１０３,0),
                NVL(月間ランクＵＰ対象金額００４,0),
                NVL(月間ランクＵＰ対象金額００５,0),
                NVL(月間ランクＵＰ対象金額００６,0),
                NVL(月間ランクＵＰ対象金額００７,0),
                NVL(月間ランクＵＰ対象金額００８,0),
                NVL(月間ランクＵＰ対象金額００９,0),
                NVL(月間ランクＵＰ対象金額０１０,0),
                NVL(月間ランクＵＰ対象金額０１１,0),
                NVL(月間ランクＵＰ対象金額０１２,0),
                NVL(月間ランクＵＰ対象金額００１,0),
                NVL(月間ランクＵＰ対象金額００２,0),
                NVL(月間ランクＵＰ対象金額００３,0),
                NVL(月間ランクＵＰ対象金額１０４,0),
                NVL(月間ランクＵＰ対象金額１０５,0),
                NVL(月間ランクＵＰ対象金額１０６,0),
                NVL(月間ランクＵＰ対象金額１０７,0),
                NVL(月間ランクＵＰ対象金額１０８,0),
                NVL(月間ランクＵＰ対象金額１０９,0),
                NVL(月間ランクＵＰ対象金額１１０,0),
                NVL(月間ランクＵＰ対象金額１１１,0),
                NVL(月間ランクＵＰ対象金額１１２,0),
                NVL(月間ランクＵＰ対象金額１０１,0),
                NVL(月間ランクＵＰ対象金額１０２,0),
                NVL(月間ランクＵＰ対象金額１０３,0),
                NVL(月間プレミアムポイント数００４,0),
                NVL(月間プレミアムポイント数００５,0),
                NVL(月間プレミアムポイント数００６,0),
                NVL(月間プレミアムポイント数００７,0),
                NVL(月間プレミアムポイント数００８,0),
                NVL(月間プレミアムポイント数００９,0),
                NVL(月間プレミアムポイント数０１０,0),
                NVL(月間プレミアムポイント数０１１,0),
                NVL(月間プレミアムポイント数０１２,0),
                NVL(月間プレミアムポイント数００１,0),
                NVL(月間プレミアムポイント数００２,0),
                NVL(月間プレミアムポイント数００３,0),
                NVL(月間プレミアムポイント数１０４,0),
                NVL(月間プレミアムポイント数１０５,0),
                NVL(月間プレミアムポイント数１０６,0),
                NVL(月間プレミアムポイント数１０７,0),
                NVL(月間プレミアムポイント数１０８,0),
                NVL(月間プレミアムポイント数１０９,0),
                NVL(月間プレミアムポイント数１１０,0),
                NVL(月間プレミアムポイント数１１１,0),
                NVL(月間プレミアムポイント数１１２,0),
                NVL(月間プレミアムポイント数１０１,0),
                NVL(月間プレミアムポイント数１０２,0),
                NVL(月間プレミアムポイント数１０３,0),
                NVL(最終更新日,0),
                to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
                NVL(最終更新プログラムＩＤ,'                    ')
        INTO  :h_ts_rank_info_buff.nenji_rank_cd_0,
                            :h_ts_rank_info_buff.nenji_rank_cd_1,
                            :h_ts_rank_info_buff.nenji_rank_cd_2,
                            :h_ts_rank_info_buff.nenji_rank_cd_3,
                            :h_ts_rank_info_buff.nenji_rank_cd_4,
                            :h_ts_rank_info_buff.nenji_rank_cd_5,
                            :h_ts_rank_info_buff.nenji_rank_cd_6,
                            :h_ts_rank_info_buff.nenji_rank_cd_7,
                            :h_ts_rank_info_buff.nenji_rank_cd_8,
                            :h_ts_rank_info_buff.nenji_rank_cd_9,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8,
                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9,
                            :h_ts_rank_info_buff.getsuji_rank_cd_004,
                            :h_ts_rank_info_buff.getsuji_rank_cd_005,
                            :h_ts_rank_info_buff.getsuji_rank_cd_006,
                            :h_ts_rank_info_buff.getsuji_rank_cd_007,
                            :h_ts_rank_info_buff.getsuji_rank_cd_008,
                            :h_ts_rank_info_buff.getsuji_rank_cd_009,
                            :h_ts_rank_info_buff.getsuji_rank_cd_010,
                            :h_ts_rank_info_buff.getsuji_rank_cd_011,
                            :h_ts_rank_info_buff.getsuji_rank_cd_012,
                            :h_ts_rank_info_buff.getsuji_rank_cd_001,
                            :h_ts_rank_info_buff.getsuji_rank_cd_002,
                            :h_ts_rank_info_buff.getsuji_rank_cd_003,
                            :h_ts_rank_info_buff.getsuji_rank_cd_104,
                            :h_ts_rank_info_buff.getsuji_rank_cd_105,
                            :h_ts_rank_info_buff.getsuji_rank_cd_106,
                            :h_ts_rank_info_buff.getsuji_rank_cd_107,
                            :h_ts_rank_info_buff.getsuji_rank_cd_108,
                            :h_ts_rank_info_buff.getsuji_rank_cd_109,
                            :h_ts_rank_info_buff.getsuji_rank_cd_110,
                            :h_ts_rank_info_buff.getsuji_rank_cd_111,
                            :h_ts_rank_info_buff.getsuji_rank_cd_112,
                            :h_ts_rank_info_buff.getsuji_rank_cd_101,
                            :h_ts_rank_info_buff.getsuji_rank_cd_102,
                            :h_ts_rank_info_buff.getsuji_rank_cd_103,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102,
                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_004,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_005,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_006,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_007,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_008,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_009,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_010,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_011,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_012,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_001,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_002,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_003,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_104,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_105,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_106,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_107,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_108,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_109,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_110,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_111,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_112,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_101,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_102,
                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_103,
                            :h_ts_rank_info_buff.saishu_koshin_ymd,
                            :h_ts_rank_info_buff.saishu_koshin_ymdhms,
                            :h_ts_rank_info_buff.saishu_koshin_programid
        FROM  TSランク情報
        WHERE 顧客番号       = :h_ts_rank_info_buff.kokyaku_no;*/

        StringDto workSql = new StringDto();
        workSql.arr = "SELECT NVL(年次ランクコード０,0),\n" +
                "        NVL(年次ランクコード１,0),\n" +
                "                NVL(年次ランクコード２,0),\n" +
                "                NVL(年次ランクコード３,0),\n" +
                "                NVL(年次ランクコード４,0),\n" +
                "                NVL(年次ランクコード５,0),\n" +
                "                NVL(年次ランクコード６,0),\n" +
                "                NVL(年次ランクコード７,0),\n" +
                "                NVL(年次ランクコード８,0),\n" +
                "                NVL(年次ランクコード９,0),\n" +
                "                NVL(年間ランクＵＰ対象金額０,0),\n" +
                "                NVL(年間ランクＵＰ対象金額１,0),\n" +
                "                NVL(年間ランクＵＰ対象金額２,0),\n" +
                "                NVL(年間ランクＵＰ対象金額３,0),\n" +
                "                NVL(年間ランクＵＰ対象金額４,0),\n" +
                "                NVL(年間ランクＵＰ対象金額５,0),\n" +
                "                NVL(年間ランクＵＰ対象金額６,0),\n" +
                "                NVL(年間ランクＵＰ対象金額７,0),\n" +
                "                NVL(年間ランクＵＰ対象金額８,0),\n" +
                "                NVL(年間ランクＵＰ対象金額９,0),\n" +
                "                NVL(月次ランクコード００４,0),\n" +
                "                NVL(月次ランクコード００５,0),\n" +
                "                NVL(月次ランクコード００６,0),\n" +
                "                NVL(月次ランクコード００７,0),\n" +
                "                NVL(月次ランクコード００８,0),\n" +
                "                NVL(月次ランクコード００９,0),\n" +
                "                NVL(月次ランクコード０１０,0),\n" +
                "                NVL(月次ランクコード０１１,0),\n" +
                "                NVL(月次ランクコード０１２,0),\n" +
                "                NVL(月次ランクコード００１,0),\n" +
                "                NVL(月次ランクコード００２,0),\n" +
                "                NVL(月次ランクコード００３,0),\n" +
                "                NVL(月次ランクコード１０４,0),\n" +
                "                NVL(月次ランクコード１０５,0),\n" +
                "                NVL(月次ランクコード１０６,0),\n" +
                "                NVL(月次ランクコード１０７,0),\n" +
                "                NVL(月次ランクコード１０８,0),\n" +
                "                NVL(月次ランクコード１０９,0),\n" +
                "                NVL(月次ランクコード１１０,0),\n" +
                "                NVL(月次ランクコード１１１,0),\n" +
                "                NVL(月次ランクコード１１２,0),\n" +
                "                NVL(月次ランクコード１０１,0),\n" +
                "                NVL(月次ランクコード１０２,0),\n" +
                "                NVL(月次ランクコード１０３,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００４,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００５,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００６,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００７,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００８,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００９,0),\n" +
                "                NVL(月間ランクＵＰ対象金額０１０,0),\n" +
                "                NVL(月間ランクＵＰ対象金額０１１,0),\n" +
                "                NVL(月間ランクＵＰ対象金額０１２,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００１,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００２,0),\n" +
                "                NVL(月間ランクＵＰ対象金額００３,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０４,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０５,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０６,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０７,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０８,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０９,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１１０,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１１１,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１１２,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０１,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０２,0),\n" +
                "                NVL(月間ランクＵＰ対象金額１０３,0),\n" +
                "                NVL(月間プレミアムポイント数００４,0),\n" +
                "                NVL(月間プレミアムポイント数００５,0),\n" +
                "                NVL(月間プレミアムポイント数００６,0),\n" +
                "                NVL(月間プレミアムポイント数００７,0),\n" +
                "                NVL(月間プレミアムポイント数００８,0),\n" +
                "                NVL(月間プレミアムポイント数００９,0),\n" +
                "                NVL(月間プレミアムポイント数０１０,0),\n" +
                "                NVL(月間プレミアムポイント数０１１,0),\n" +
                "                NVL(月間プレミアムポイント数０１２,0),\n" +
                "                NVL(月間プレミアムポイント数００１,0),\n" +
                "                NVL(月間プレミアムポイント数００２,0),\n" +
                "                NVL(月間プレミアムポイント数００３,0),\n" +
                "                NVL(月間プレミアムポイント数１０４,0),\n" +
                "                NVL(月間プレミアムポイント数１０５,0),\n" +
                "                NVL(月間プレミアムポイント数１０６,0),\n" +
                "                NVL(月間プレミアムポイント数１０７,0),\n" +
                "                NVL(月間プレミアムポイント数１０８,0),\n" +
                "                NVL(月間プレミアムポイント数１０９,0),\n" +
                "                NVL(月間プレミアムポイント数１１０,0),\n" +
                "                NVL(月間プレミアムポイント数１１１,0),\n" +
                "                NVL(月間プレミアムポイント数１１２,0),\n" +
                "                NVL(月間プレミアムポイント数１０１,0),\n" +
                "                NVL(月間プレミアムポイント数１０２,0),\n" +
                "                NVL(月間プレミアムポイント数１０３,0),\n" +
                "                NVL(最終更新日,0),\n" +
                "                to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS')),\n" +
                "                NVL(RPAD(最終更新プログラムＩＤ ,LENGTH(最終更新プログラムＩＤ)),'                    ')\n" +
                "        FROM  TSランク情報\n" +
                "        WHERE 顧客番号       = ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(h_ts_rank_info_buff.kokyaku_no);
        sqlca.fetch();
        sqlca.recData(h_ts_rank_info_buff.nenji_rank_cd_0,
                            h_ts_rank_info_buff.nenji_rank_cd_1,
                            h_ts_rank_info_buff.nenji_rank_cd_2,
                            h_ts_rank_info_buff.nenji_rank_cd_3,
                            h_ts_rank_info_buff.nenji_rank_cd_4,
                            h_ts_rank_info_buff.nenji_rank_cd_5,
                            h_ts_rank_info_buff.nenji_rank_cd_6,
                            h_ts_rank_info_buff.nenji_rank_cd_7,
                            h_ts_rank_info_buff.nenji_rank_cd_8,
                            h_ts_rank_info_buff.nenji_rank_cd_9,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8,
                            h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9,
                            h_ts_rank_info_buff.getsuji_rank_cd_004,
                            h_ts_rank_info_buff.getsuji_rank_cd_005,
                            h_ts_rank_info_buff.getsuji_rank_cd_006,
                            h_ts_rank_info_buff.getsuji_rank_cd_007,
                            h_ts_rank_info_buff.getsuji_rank_cd_008,
                            h_ts_rank_info_buff.getsuji_rank_cd_009,
                            h_ts_rank_info_buff.getsuji_rank_cd_010,
                            h_ts_rank_info_buff.getsuji_rank_cd_011,
                            h_ts_rank_info_buff.getsuji_rank_cd_012,
                            h_ts_rank_info_buff.getsuji_rank_cd_001,
                            h_ts_rank_info_buff.getsuji_rank_cd_002,
                            h_ts_rank_info_buff.getsuji_rank_cd_003,
                            h_ts_rank_info_buff.getsuji_rank_cd_104,
                            h_ts_rank_info_buff.getsuji_rank_cd_105,
                            h_ts_rank_info_buff.getsuji_rank_cd_106,
                            h_ts_rank_info_buff.getsuji_rank_cd_107,
                            h_ts_rank_info_buff.getsuji_rank_cd_108,
                            h_ts_rank_info_buff.getsuji_rank_cd_109,
                            h_ts_rank_info_buff.getsuji_rank_cd_110,
                            h_ts_rank_info_buff.getsuji_rank_cd_111,
                            h_ts_rank_info_buff.getsuji_rank_cd_112,
                            h_ts_rank_info_buff.getsuji_rank_cd_101,
                            h_ts_rank_info_buff.getsuji_rank_cd_102,
                            h_ts_rank_info_buff.getsuji_rank_cd_103,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102,
                            h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_004,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_005,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_006,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_007,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_008,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_009,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_010,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_011,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_012,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_001,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_002,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_003,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_104,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_105,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_106,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_107,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_108,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_109,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_110,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_111,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_112,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_101,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_102,
                            h_ts_rank_info_buff.gekkan_premium_point_kingaku_103,
                            h_ts_rank_info_buff.saishu_koshin_ymd,
                            h_ts_rank_info_buff.saishu_koshin_ymdhms,
                            h_ts_rank_info_buff.saishu_koshin_programid);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=%s",
                    h_ts_rank_info_buff.kokyaku_no.arr );

            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "TSランク情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        /* 出力引数の設定 */
        tsRankInfo.nenji_rank_cd_0                         = h_ts_rank_info_buff.nenji_rank_cd_0;                     /* 年次ランクコード０             */
        tsRankInfo.nenji_rank_cd_1                         = h_ts_rank_info_buff.nenji_rank_cd_1;                     /* 年次ランクコード１             */
        tsRankInfo.nenji_rank_cd_2                         = h_ts_rank_info_buff.nenji_rank_cd_2;                     /* 年次ランクコード２             */
        tsRankInfo.nenji_rank_cd_3                         = h_ts_rank_info_buff.nenji_rank_cd_3;                     /* 年次ランクコード３             */
        tsRankInfo.nenji_rank_cd_4                         = h_ts_rank_info_buff.nenji_rank_cd_4;                     /* 年次ランクコード４             */
        tsRankInfo.nenji_rank_cd_5                         = h_ts_rank_info_buff.nenji_rank_cd_5;                     /* 年次ランクコード５             */
        tsRankInfo.nenji_rank_cd_6                         = h_ts_rank_info_buff.nenji_rank_cd_6;                     /* 年次ランクコード６             */
        tsRankInfo.nenji_rank_cd_7                         = h_ts_rank_info_buff.nenji_rank_cd_7;                     /* 年次ランクコード７             */
        tsRankInfo.nenji_rank_cd_8                         = h_ts_rank_info_buff.nenji_rank_cd_8;                     /* 年次ランクコード８             */
        tsRankInfo.nenji_rank_cd_9                         = h_ts_rank_info_buff.nenji_rank_cd_9;                     /* 年次ランクコード９             */
        tsRankInfo.nenkan_rankup_taisho_kingaku_0          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0;      /* 年間ランクＵＰ対象金額０       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_1          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1;      /* 年間ランクＵＰ対象金額１       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_2          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2;      /* 年間ランクＵＰ対象金額２       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_3          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3;      /* 年間ランクＵＰ対象金額３       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_4          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4;      /* 年間ランクＵＰ対象金額４       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_5          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5;      /* 年間ランクＵＰ対象金額５       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_6          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6;      /* 年間ランクＵＰ対象金額６       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_7          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7;      /* 年間ランクＵＰ対象金額７       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_8          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8;      /* 年間ランクＵＰ対象金額８       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_9          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9;      /* 年間ランクＵＰ対象金額９       */
        tsRankInfo.getsuji_rank_cd_004                     = h_ts_rank_info_buff.getsuji_rank_cd_004;                 /* 月次ランクコード００４         */
        tsRankInfo.getsuji_rank_cd_005                     = h_ts_rank_info_buff.getsuji_rank_cd_005;                 /* 月次ランクコード００５         */
        tsRankInfo.getsuji_rank_cd_006                     = h_ts_rank_info_buff.getsuji_rank_cd_006;                 /* 月次ランクコード００６         */
        tsRankInfo.getsuji_rank_cd_007                     = h_ts_rank_info_buff.getsuji_rank_cd_007;                 /* 月次ランクコード００７         */
        tsRankInfo.getsuji_rank_cd_008                     = h_ts_rank_info_buff.getsuji_rank_cd_008;                 /* 月次ランクコード００８         */
        tsRankInfo.getsuji_rank_cd_009                     = h_ts_rank_info_buff.getsuji_rank_cd_009;                 /* 月次ランクコード００９         */
        tsRankInfo.getsuji_rank_cd_010                     = h_ts_rank_info_buff.getsuji_rank_cd_010;                 /* 月次ランクコード０１０         */
        tsRankInfo.getsuji_rank_cd_011                     = h_ts_rank_info_buff.getsuji_rank_cd_011;                 /* 月次ランクコード０１１         */
        tsRankInfo.getsuji_rank_cd_012                     = h_ts_rank_info_buff.getsuji_rank_cd_012;                 /* 月次ランクコード０１２         */
        tsRankInfo.getsuji_rank_cd_001                     = h_ts_rank_info_buff.getsuji_rank_cd_001;                 /* 月次ランクコード００１         */
        tsRankInfo.getsuji_rank_cd_002                     = h_ts_rank_info_buff.getsuji_rank_cd_002;                 /* 月次ランクコード００２         */
        tsRankInfo.getsuji_rank_cd_003                     = h_ts_rank_info_buff.getsuji_rank_cd_003;                 /* 月次ランクコード００３         */
        tsRankInfo.getsuji_rank_cd_104                     = h_ts_rank_info_buff.getsuji_rank_cd_104;                 /* 月次ランクコード１０４         */
        tsRankInfo.getsuji_rank_cd_105                     = h_ts_rank_info_buff.getsuji_rank_cd_105;                 /* 月次ランクコード１０５         */
        tsRankInfo.getsuji_rank_cd_106                     = h_ts_rank_info_buff.getsuji_rank_cd_106;                 /* 月次ランクコード１０６         */
        tsRankInfo.getsuji_rank_cd_107                     = h_ts_rank_info_buff.getsuji_rank_cd_107;                 /* 月次ランクコード１０７         */
        tsRankInfo.getsuji_rank_cd_108                     = h_ts_rank_info_buff.getsuji_rank_cd_108;                 /* 月次ランクコード１０８         */
        tsRankInfo.getsuji_rank_cd_109                     = h_ts_rank_info_buff.getsuji_rank_cd_109;                 /* 月次ランクコード１０９         */
        tsRankInfo.getsuji_rank_cd_110                     = h_ts_rank_info_buff.getsuji_rank_cd_110;                 /* 月次ランクコード１１０         */
        tsRankInfo.getsuji_rank_cd_111                     = h_ts_rank_info_buff.getsuji_rank_cd_111;                 /* 月次ランクコード１１１         */
        tsRankInfo.getsuji_rank_cd_112                     = h_ts_rank_info_buff.getsuji_rank_cd_112;                 /* 月次ランクコード１１２         */
        tsRankInfo.getsuji_rank_cd_101                     = h_ts_rank_info_buff.getsuji_rank_cd_101;                 /* 月次ランクコード１０１         */
        tsRankInfo.getsuji_rank_cd_102                     = h_ts_rank_info_buff.getsuji_rank_cd_102;                 /* 月次ランクコード１０２         */
        tsRankInfo.getsuji_rank_cd_103                     = h_ts_rank_info_buff.getsuji_rank_cd_103;                 /* 月次ランクコード１０３         */
        tsRankInfo.gekkan_rankup_taisho_kingaku_004        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004;    /* 月間ランクＵＰ対象金額００４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_005        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005;    /* 月間ランクＵＰ対象金額００５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_006        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006;    /* 月間ランクＵＰ対象金額００６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_007        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007;    /* 月間ランクＵＰ対象金額００７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_008        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008;    /* 月間ランクＵＰ対象金額００８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_009        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009;    /* 月間ランクＵＰ対象金額００９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_010        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010;    /* 月間ランクＵＰ対象金額０１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_011        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011;    /* 月間ランクＵＰ対象金額０１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_012        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012;    /* 月間ランクＵＰ対象金額０１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_001        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001;    /* 月間ランクＵＰ対象金額００１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_002        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002;    /* 月間ランクＵＰ対象金額００２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_003        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003;    /* 月間ランクＵＰ対象金額００３   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_104        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104;    /* 月間ランクＵＰ対象金額１０４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_105        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105;    /* 月間ランクＵＰ対象金額１０５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_106        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106;    /* 月間ランクＵＰ対象金額１０６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_107        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107;    /* 月間ランクＵＰ対象金額１０７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_108        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108;    /* 月間ランクＵＰ対象金額１０８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_109        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109;    /* 月間ランクＵＰ対象金額１０９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_110        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110;    /* 月間ランクＵＰ対象金額１１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_111        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111;    /* 月間ランクＵＰ対象金額１１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_112        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112;    /* 月間ランクＵＰ対象金額１１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_101        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101;    /* 月間ランクＵＰ対象金額１０１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_102        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102;    /* 月間ランクＵＰ対象金額１０２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_103        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103;    /* 月間ランクＵＰ対象金額１０３   */
        tsRankInfo.gekkan_premium_point_kingaku_004        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_004;    /* 月間プレミアムポイント数００４ */
        tsRankInfo.gekkan_premium_point_kingaku_005        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_005;    /* 月間プレミアムポイント数００５ */
        tsRankInfo.gekkan_premium_point_kingaku_006        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_006;    /* 月間プレミアムポイント数００６ */
        tsRankInfo.gekkan_premium_point_kingaku_007        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_007;    /* 月間プレミアムポイント数００７ */
        tsRankInfo.gekkan_premium_point_kingaku_008        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_008;    /* 月間プレミアムポイント数００８ */
        tsRankInfo.gekkan_premium_point_kingaku_009        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_009;    /* 月間プレミアムポイント数００９ */
        tsRankInfo.gekkan_premium_point_kingaku_010        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_010;    /* 月間プレミアムポイント数０１０ */
        tsRankInfo.gekkan_premium_point_kingaku_011        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_011;    /* 月間プレミアムポイント数０１１ */
        tsRankInfo.gekkan_premium_point_kingaku_012        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_012;    /* 月間プレミアムポイント数０１２ */
        tsRankInfo.gekkan_premium_point_kingaku_001        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_001;    /* 月間プレミアムポイント数００１ */
        tsRankInfo.gekkan_premium_point_kingaku_002        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_002;    /* 月間プレミアムポイント数００２ */
        tsRankInfo.gekkan_premium_point_kingaku_003        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_003;    /* 月間プレミアムポイント数００３ */
        tsRankInfo.gekkan_premium_point_kingaku_104        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_104;    /* 月間プレミアムポイント数１０４ */
        tsRankInfo.gekkan_premium_point_kingaku_105        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_105;    /* 月間プレミアムポイント数１０５ */
        tsRankInfo.gekkan_premium_point_kingaku_106        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_106;    /* 月間プレミアムポイント数１０６ */
        tsRankInfo.gekkan_premium_point_kingaku_107        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_107;    /* 月間プレミアムポイント数１０７ */
        tsRankInfo.gekkan_premium_point_kingaku_108        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_108;    /* 月間プレミアムポイント数１０８ */
        tsRankInfo.gekkan_premium_point_kingaku_109        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_109;    /* 月間プレミアムポイント数１０９ */
        tsRankInfo.gekkan_premium_point_kingaku_110        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_110;    /* 月間プレミアムポイント数１１０ */
        tsRankInfo.gekkan_premium_point_kingaku_111        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_111;    /* 月間プレミアムポイント数１１１ */
        tsRankInfo.gekkan_premium_point_kingaku_112        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_112;    /* 月間プレミアムポイント数１１２ */
        tsRankInfo.gekkan_premium_point_kingaku_101        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_101;    /* 月間プレミアムポイント数１０１ */
        tsRankInfo.gekkan_premium_point_kingaku_102        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_102;    /* 月間プレミアムポイント数１０２ */
        tsRankInfo.gekkan_premium_point_kingaku_103        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_103;    /* 月間プレミアムポイント数１０３ */
        tsRankInfo.saishu_koshin_ymd                       = h_ts_rank_info_buff.saishu_koshin_ymd;                   /* 最終更新日                     */
        tsRankInfo.saishu_koshin_ymdhms                    = h_ts_rank_info_buff.saishu_koshin_ymdhms;                /* 最終更新日時                   */
        strcpy(tsRankInfo.saishu_koshin_programid, h_ts_rank_info_buff.saishu_koshin_programid);                      /* 最終更新プログラムＩＤ         */

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("顧客番号      : %s\n", h_ts_rank_info_buff.kokyaku_no.arr);
            C_DbgMsg("GetTsRankInfo : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetCardShubetsu                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetCardShubetsu()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード種別の取得処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               なし                                                         */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int GetCardShubetsu() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("GetCardShubetsu処理");
            /*---------------------------------------------------------------------*/
        }

        cmBTpmdfBDto.h_card_shubetsu.arr = 0;

        /* カード種別の取得処理 */
        /*EXEC SQL SELECT カード種別
        INTO :h_card_shubetsu
        FROM PS会員番号体系
        WHERE サービス種別 = :ms_card_data.service_shubetsu
        AND 会員番号開始 <= :ms_card_data.kaiin_no
        AND 会員番号終了 >= :ms_card_data.kaiin_no;*/
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT カード種別\n" +
                "        FROM PS会員番号体系\n" +
                "        WHERE サービス種別 = ?\n" +
                "        AND 会員番号開始 <= ?\n" +
                "        AND 会員番号終了 >= ?";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTpmdfBDto.ms_card_data.service_shubetsu, cmBTpmdfBDto.ms_card_data.kaiin_no, cmBTpmdfBDto.ms_card_data.kaiin_no);
        sqlca.fetch();
        sqlca.recData(cmBTpmdfBDto.h_card_shubetsu);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (   sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "会員番号=%s",
                    cmBTpmdfBDto.ms_card_data.kaiin_no.arr);
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "PS会員番号体系", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("カード種別     = [%d]\n", cmBTpmdfBDto.h_card_shubetsu);
            C_DbgEnd("GetCardShubetsu処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： UpdateTsRankInfo                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      static int UpdateTsRankInfo(int year, int month)                      */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              TSランク情報を更新する                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*       int    year   ： 更新対象テーブル年(YYYY)                            */
    /*       int    month  ： 月                                                  */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int UpdateTsRankInfo(int year, int month) {
        int  rtn_cd;                                            /* 関数戻り値 */
        int  w_nen_code_int;                           /* 更新対象年コードint */
        StringDto w_nen_code = new StringDto(2);                               /* 更新対象年コード */
        StringDto w_nen_code_full = new StringDto(4);                      /* 更新対象年コード全角 */
        StringDto w_bat_mm = new StringDto(3);                                     /* バッチ処理月 */
        StringDto w_bat_mm_full = new StringDto(6);                            /* バッチ処理月全角 */
        StringDto wk_item1 = new StringDto(512);      /* 項目名編集バッファ 年間ランクＵＰ対象金額 */
        StringDto wk_item2 = new StringDto(512);      /* 項目名編集バッファ 月間ランクＵＰ対象金額 */
        StringDto sql_buf = new StringDto(4096);                                 /* ＳＱＬ文編集用 */

        if (DBG_LOG) {
            C_DbgStart("UpdateTsRankInfo処理");
        }

        /* ホスト変数を編集する */
        w_nen_code_int = 0;
        memset(w_nen_code, 0x00, sizeof(w_nen_code));
        memset(w_nen_code_full, 0x00, sizeof(w_nen_code_full));
        memset(w_bat_mm, 0x00, sizeof(w_bat_mm));
        memset(w_bat_mm_full, 0x00, sizeof(w_bat_mm_full));
        strcpy(cmBTpmdfBDto.h_ts_rank_info_data.saishu_koshin_programid, Cg_Program_Name);
        w_nen_code_int = year % 10;
        sprintf(w_nen_code, "%d", w_nen_code_int);
        sprintf(w_bat_mm, "%02d", month);
        rtn_cd = C_ConvHalf2Full(w_bat_mm, w_bat_mm_full);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        rtn_cd = C_ConvHalf2Full(w_nen_code, w_nen_code_full);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* ＳＱＬを生成する */
        /* 項目名編集 */
        memset(wk_item1, 0x00, sizeof(wk_item1));
        memset(wk_item2, 0x00, sizeof(wk_item2));

        sprintf(wk_item1, "年間ランクＵＰ対象金額%s = 年間ランクＵＰ対象金額%s + ?,", w_nen_code_full, w_nen_code_full);

        if ( ( year % 2 ) == 0 ) {
            /* 偶数年 */
            sprintf(wk_item2, "月間ランクＵＰ対象金額０%s = 月間ランクＵＰ対象金額０%s + ?,", w_bat_mm_full, w_bat_mm_full);
        }else {
            /* 奇数年 */
            sprintf(wk_item2, "月間ランクＵＰ対象金額１%s = 月間ランクＵＰ対象金額１%s + ?,", w_bat_mm_full, w_bat_mm_full);
        }

        /* ホスト変数設定 */
        strcpy(sql_buf, "UPDATE TSランク情報 ");
        strcat(sql_buf, "SET ");
        strcat(sql_buf, wk_item1);
        strcat(sql_buf, wk_item2);
        strcat(sql_buf, "最終更新日 = ?,");
        strcat(sql_buf, "最終更新日時 = sysdate(),");
        strcat(sql_buf, "最終更新プログラムＩＤ = ?");
        strcat(sql_buf, " WHERE 顧客番号 = ?");

        if (DBG_LOG) {
            C_DbgMsg("UpdateTsRankInfot : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(cmBTpmdfBDto.h_str_sql, 0x00, sizeof(cmBTpmdfBDto.h_str_sql));
        strcpy(cmBTpmdfBDto.h_str_sql, sql_buf);

        // EXEC SQL PREPARE sql_stat3 from :h_str_sql;
//        SqlstmDto sqlca = sqlcaManager.get("sql_stat3");
        sqlca.sql = sql_buf;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("UpdateTsRankInfo : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTpmdfBDto.ms_card_data.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "UPDATE PREPARE",
                    sqlca.sqlcode, "TSランク情報",
                    out_format_buf, 0, 0);

            return C_const_NG;
        }

        /* UPDATE文を実行する */
        /*
        EXEC SQL EXECUTE sql_stat3
        USING
        :point_shuse_data.rankup_taisho_kingaku_kagenti,         *//* 年間ランクＵＰ対象金額 *//*
            :point_shuse_data.rankup_taisho_kingaku_kagenti,         *//* 月間ランクＵＰ対象金額 *//*
            :h_bat_date,                                                         *//* 最終更新日 *//*
            :h_ts_rank_info_data.saishu_koshin_programid,            *//* 最終更新プログラムＩＤ *//*
            :ms_card_data.kokyaku_no;                                              *//* 顧客番号 *//*
        */
        sqlca.restAndExecute(cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.longVal(),
                cmBTpmdfBDto.point_shuse_data.rankup_taisho_kingaku_kagenti.longVal(),
                cmBTpmdfBDto.h_bat_date.longVal(),
                                cmBTpmdfBDto.h_ts_rank_info_data.saishu_koshin_programid,
                cmBTpmdfBDto.ms_card_data.kokyaku_no.longVal());

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode !=
                C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("UpdateTsRankInfo : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTpmdfBDto.ms_card_data.kokyaku_no.arr );
            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSランク情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /* 正常の場合終了 */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            if (DBG_LOG) {
                C_DbgEnd("UpdateTsRankInfo処理", 0, 0, 0);
            }

            return C_const_OK;
        }

        if (DBG_LOG) {
            C_DbgEnd("UpdateTsRankInfo処理", 0, 0, 0);
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetYukokigen                                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetYukokigen(int point_flg, int *yukokigen)                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               有効期限の取得                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*               有効期限の取得引数                                           */
    /*               int  *yukokigen                                              */
    /*                                                                            */
    /*               int  point_flg   0 : 通常P加減値（当年）                     */
    /*                                1 : 通常P加減値（前年）                     */
    /*                                2 : 期間限定P加減値（当月）                 */
    /*                                3 : 期間限定P加減値（1ヶ月後）              */
    /*                                4 : 期間限定P加減値（2ヶ月後）              */
    /*                                5 : 期間限定P加減値（3ヶ月後）              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void GetYukokigen(int point_flg, IntegerDto yukokigen) {
        int getsumatsu = 0;                                               /* 月末 */
        int nen = 0;
        int getsu = 0;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("GetYukokigen処理");
            /*---------------------------------------------------------------------*/
        }

        /* 有効期限の取得の取得 */

        /* 通常P加減値（当年）の有効期限: 判定後_通常P基準年度 + 2年の3月31日 */
        if(point_flg == 0){
            //*yukokigen = ((in_point_shusei.tujo_point_kijun_nendo + 2) *10000) + 331;
            yukokigen.arr = ((cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_kijun_nendo.intVal() + 2) *10000) + 331;
        }

        /* 通常P加減値（前年）の有効期限: 判定後_通常P基準年度 + 1年の3月31日 */
        if(point_flg == 1){
            //*yukokigen = ((in_point_shusei.tujo_point_kijun_nendo + 1) *10000) + 331;
            yukokigen.arr = ((cmBTpmdfBDto.honjitsu_point_data.koshin_riyo_tujo_point_kijun_nendo.intVal() + 1) *10000) + 331;
        }

        /* 期間限定P加減値（当月）の有効期限:判定後_期間限定P基準月の当月月末 */
        if(point_flg == 2){
            //getsumatsu = daysInMonth(in_point_shusei.tujo_point_kijun_nendo, in_point_shusei.kikan_point_kijun_month);
            //*yukokigen = in_point_shusei.tujo_point_kijun_nendo * 10000 + in_point_shusei.kikan_point_kijun_month * 100 + getsumatsu;
            getsumatsu = daysInMonth(atoi(bat_yyyy), cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal());
            yukokigen.arr = atoi(bat_yyyy) * 10000 + cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() * 100 + getsumatsu;
        }

        /* 期間限定P加減値（1ヶ月後）の有効期限:判定後_期間限定P基準月の1か月後の月末 */
        if(point_flg == 3){
            //if((in_point_shusei.kikan_point_kijun_month + 1) > 12){
            //   nen = in_point_shusei.tujo_point_kijun_nendo + 1;
            //   getsu = in_point_shusei.kikan_point_kijun_month - 11;
            //}else{
            //   nen = in_point_shusei.tujo_point_kijun_nendo;
            //   getsu = in_point_shusei.kikan_point_kijun_month + 1;
            //}
            if((cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 1) > 12){
                nen = atoi(bat_yyyy) + 1;
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() - 11;
            }else{
                nen = atoi(bat_yyyy);
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 1;
            }
            getsumatsu = daysInMonth(nen, getsu);
            yukokigen.arr = nen * 10000 + getsu * 100 + getsumatsu;
        }

        /* 期間限定P加減値（2ヶ月後）の有効期限:判定後_期間限定P基準月の2か月後の月末 */
        if(point_flg == 4){
            //if((in_point_shusei.kikan_point_kijun_month + 2) > 12){
            //   nen = in_point_shusei.tujo_point_kijun_nendo + 1;
            //   getsu = in_point_shusei.kikan_point_kijun_month - 10;
            //}else{
            //   nen = in_point_shusei.tujo_point_kijun_nendo;
            //   getsu = in_point_shusei.kikan_point_kijun_month + 2;
            //}
            if((cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 2) > 12){
                nen = atoi(bat_yyyy) + 1;
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() - 10;
            }else{
                nen = atoi(bat_yyyy);
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 2;
            }
            getsumatsu = daysInMonth(nen, getsu);
            yukokigen.arr = nen * 10000 + getsu * 100 + getsumatsu;
        }

        /* 期間限定P加減値（3ヶ月後）の有効期限:判定後_期間限定P基準月の3か月後の月末 */
        if(point_flg == 5){
            //if((in_point_shusei.kikan_point_kijun_month + 3) > 12){
            //   nen = in_point_shusei.tujo_point_kijun_nendo + 1;
            //   getsu = in_point_shusei.kikan_point_kijun_month - 9;
            //}else{
            //   nen = in_point_shusei.tujo_point_kijun_nendo;
            //   getsu = in_point_shusei.kikan_point_kijun_month + 3;
            //}
            if((cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 3) > 12){
                nen = atoi(bat_yyyy) + 1;
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() - 9;
            }else{
                nen = atoi(bat_yyyy);
                getsu = cmBTpmdfBDto.honjitsu_point_data.koshin_fuyo_kikan_gentei_point_kijun_month.intVal() + 3;
            }
            getsumatsu = daysInMonth(nen, getsu);
            yukokigen.arr = nen * 10000 + getsu * 100 + getsumatsu;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("有効期限     = [%d]\n", yukokigen);
            C_DbgEnd("GetYukokigen処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： daysInMonth                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  daysInMonth( int year, int month )                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*           指定された年月の月末日を取得する。                               */
    /*           指定された月が不正(１～１２以外）の場合、０を返却する。          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int  year   判定対象年                                                */
    /*      int  month  判定対象月                                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              指定された年月の月末日                                        */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int daysInMonth(int year, int month) {
        int   lastDayInMonth;
        int nDaysInMonth[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("daysInMonth処理");
            /*------------------------------------------------------------*/
        }

        if (( month < 1 ) || ( 12 < month )) {
            lastDayInMonth =  0;
        } else if (( 2 == month ) && 1 == isLeapYear( year )) {
            lastDayInMonth = 29;
        } else {
            lastDayInMonth = nDaysInMonth[month - 1];
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** daysInMonth *** 月末日取得 [%d]\n", lastDayInMonth);
            C_DbgEnd("daysInMonth処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return lastDayInMonth;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： isLeapYear                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  isLeapYear()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*           閏年判定                                                         */
    /*           ４で割り切れる年を閏年とする。                                   */
    /*           ただし４００で割り切れないが１００で割り切ることのできる         */
    /*           年は閏年ではない。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int  year  判定対象年                                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 閏年ではない                                           */
    /*              1   ： 閏年である                                             */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int isLeapYear(int year) {
        int isLeapYear;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("isLeapYear処理");
            /*------------------------------------------------------------*/
        }

        isLeapYear = (0 == (year % 400)) || ((0 != (year % 100)) && (0 == (year % 4))) ? 1 : 0;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** isLeapYear *** 閏年判定 [%d]\n", isLeapYear);
            C_DbgEnd("isLeapYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return isLeapYear;
    }

    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ： InsertDayPointUchiwake                                      */
    /*                                                                            */
    /*      書式                                                                  */
    /*      static int InsertDayPointUchiwake()                                   */
    /*                                                                            */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              HSポイント日別内訳情報追加処理                                */
    /*                                                                            */
    /*      【引数】                                                              */
    /*          DAY_POINT_DATA * dayPointData ： ポイント日別内訳情報構造体取得   */
    /*                                               パラメータ                   */
    /*              int           date   ： 更新対象テーブル日付(YYYYMMDD)        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int InsertDayPointUchiwake() {
        StringDto tbl_nam = new StringDto(200);
        StringDto sql_buf = new StringDto(8192);     /* ＳＱＬ文編集用 */
        StringDto sql_buf2 = new StringDto(6144);    /* ＳＱＬ文編集用 */
        StringDto sql_buf3 = new StringDto(2048);    /* ＳＱＬ文編集用 */
        StringDto buff = new StringDto(10);
        StringDto w_date = new StringDto(10);

        if (DBG_LOG) {
            C_DbgMsg("InsertDayPointUchiwake : %s\n", "start");
        }

        /* テーブル名編集 */
        memset(buff, 0x00, sizeof(buff));
        sprintf(buff, "%s", bat_date);
        memset(w_date, 0x00, sizeof(w_date));
        memcpy(w_date, buff, 6);
        memset(tbl_nam, 0x00, sizeof(tbl_nam));
        sprintf(tbl_nam, "HSポイント日別内訳情報%s", w_date);


        strcpy(sql_buf2, " (システム年月日,");
        strcat(sql_buf2, "顧客番号,");
        strcat(sql_buf2, "処理通番,");
        strcat(sql_buf2, "枝番");

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０１,");
            strcat(sql_buf2, "明細番号０１,");
            strcat(sql_buf2, "企画ＩＤ０１,");
            strcat(sql_buf2, "企画バージョン０１,");
            strcat(sql_buf2, "ポイントカテゴリ０１,");
            strcat(sql_buf2, "ポイント種別０１,");
            strcat(sql_buf2, "買上高ポイント種別０１,");
            strcat(sql_buf2, "付与ポイント０１,");
            strcat(sql_buf2, "利用ポイント０１,");
            strcat(sql_buf2, "ポイント対象金額０１,");
            strcat(sql_buf2, "ＪＡＮコード０１,");
            strcat(sql_buf2, "商品購入数０１,");
            strcat(sql_buf2, "商品パーセントＰ付与率０１,");
            strcat(sql_buf2, "通常期間限定区分０１,");
            strcat(sql_buf2, "ポイント有効期限０１,");
            strcat(sql_buf2, "購買区分０１");
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０２,");
            strcat(sql_buf2, "明細番号０２,");
            strcat(sql_buf2, "企画ＩＤ０２,");
            strcat(sql_buf2, "企画バージョン０２,");
            strcat(sql_buf2, "ポイントカテゴリ０２,");
            strcat(sql_buf2, "ポイント種別０２,");
            strcat(sql_buf2, "買上高ポイント種別０２,");
            strcat(sql_buf2, "付与ポイント０２,");
            strcat(sql_buf2, "利用ポイント０２,");
            strcat(sql_buf2, "ポイント対象金額０２,");
            strcat(sql_buf2, "ＪＡＮコード０２,");
            strcat(sql_buf2, "商品購入数０２,");
            strcat(sql_buf2, "商品パーセントＰ付与率０２,");
            strcat(sql_buf2, "通常期間限定区分０２,");
            strcat(sql_buf2, "ポイント有効期限０２,");
            strcat(sql_buf2, "購買区分０２");
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０３,");
            strcat(sql_buf2, "明細番号０３,");
            strcat(sql_buf2, "企画ＩＤ０３,");
            strcat(sql_buf2, "企画バージョン０３,");
            strcat(sql_buf2, "ポイントカテゴリ０３,");
            strcat(sql_buf2, "ポイント種別０３,");
            strcat(sql_buf2, "買上高ポイント種別０３,");
            strcat(sql_buf2, "付与ポイント０３,");
            strcat(sql_buf2, "利用ポイント０３,");
            strcat(sql_buf2, "ポイント対象金額０３,");
            strcat(sql_buf2, "ＪＡＮコード０３,");
            strcat(sql_buf2, "商品購入数０３,");
            strcat(sql_buf2, "商品パーセントＰ付与率０３,");
            strcat(sql_buf2, "通常期間限定区分０３,");
            strcat(sql_buf2, "ポイント有効期限０３,");
            strcat(sql_buf2, "購買区分０３");
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０４,");
            strcat(sql_buf2, "明細番号０４,");
            strcat(sql_buf2, "企画ＩＤ０４,");
            strcat(sql_buf2, "企画バージョン０４,");
            strcat(sql_buf2, "ポイントカテゴリ０４,");
            strcat(sql_buf2, "ポイント種別０４,");
            strcat(sql_buf2, "買上高ポイント種別０４,");
            strcat(sql_buf2, "付与ポイント０４,");
            strcat(sql_buf2, "利用ポイント０４,");
            strcat(sql_buf2, "ポイント対象金額０４,");
            strcat(sql_buf2, "ＪＡＮコード０４,");
            strcat(sql_buf2, "商品購入数０４,");
            strcat(sql_buf2, "商品パーセントＰ付与率０４,");
            strcat(sql_buf2, "通常期間限定区分０４,");
            strcat(sql_buf2, "ポイント有効期限０４,");
            strcat(sql_buf2, "購買区分０４");
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０５,");
            strcat(sql_buf2, "明細番号０５,");
            strcat(sql_buf2, "企画ＩＤ０５,");
            strcat(sql_buf2, "企画バージョン０５,");
            strcat(sql_buf2, "ポイントカテゴリ０５,");
            strcat(sql_buf2, "ポイント種別０５,");
            strcat(sql_buf2, "買上高ポイント種別０５,");
            strcat(sql_buf2, "付与ポイント０５,");
            strcat(sql_buf2, "利用ポイント０５,");
            strcat(sql_buf2, "ポイント対象金額０５,");
            strcat(sql_buf2, "ＪＡＮコード０５,");
            strcat(sql_buf2, "商品購入数０５,");
            strcat(sql_buf2, "商品パーセントＰ付与率０５,");
            strcat(sql_buf2, "通常期間限定区分０５,");
            strcat(sql_buf2, "ポイント有効期限０５,");
            strcat(sql_buf2, "購買区分０５");
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_06.intVal() != 0){
            strcat(sql_buf2, ",");
            strcat(sql_buf2, "付与利用区分０６,");
            strcat(sql_buf2, "明細番号０６,");
            strcat(sql_buf2, "企画ＩＤ０６,");
            strcat(sql_buf2, "企画バージョン０６,");
            strcat(sql_buf2, "ポイントカテゴリ０６,");
            strcat(sql_buf2, "ポイント種別０６,");
            strcat(sql_buf2, "買上高ポイント種別０６,");
            strcat(sql_buf2, "付与ポイント０６,");
            strcat(sql_buf2, "利用ポイント０６,");
            strcat(sql_buf2, "ポイント対象金額０６,");
            strcat(sql_buf2, "ＪＡＮコード０６,");
            strcat(sql_buf2, "商品購入数０６,");
            strcat(sql_buf2, "商品パーセントＰ付与率０６,");
            strcat(sql_buf2, "通常期間限定区分０６,");
            strcat(sql_buf2, "ポイント有効期限０６,");
            strcat(sql_buf2, "購買区分０６");
        }

        strcat(sql_buf2, " ) VALUES ( ");


        strcpy(sql_buf3, "?,");      /* システム年月日               */
        strcat(sql_buf3, "?,");      /* 顧客番号                     */
        strcat(sql_buf3, "?,");      /* 処理通番                     */
        strcat(sql_buf3, "?");       /* 枝番                         */

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");      /* 付与利用区分０１             */
            strcat(sql_buf3, "?,");      /* 明細番号０１                 */
            strcat(sql_buf3, "?,");      /* 企画ＩＤ０１                 */
            strcat(sql_buf3, "?,");      /* 企画バージョン０１           */
            strcat(sql_buf3, "?,");      /* ポイントカテゴリ０１         */
            strcat(sql_buf3, "?,");     /* ポイント種別０１             */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０１       */
            strcat(sql_buf3, "?,");     /* 付与ポイント０１             */
            strcat(sql_buf3, "?,");     /* 利用ポイント０１             */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０１         */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０１             */
            strcat(sql_buf3, "?,");     /* 商品購入数０１               */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０１   */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０１         */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０１         */
            strcat(sql_buf3, "?");      /* 購買区分０１                 */
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");     /* 付与利用区分０２             */
            strcat(sql_buf3, "?,");     /* 明細番号０２                 */
            strcat(sql_buf3, "?,");     /* 企画ＩＤ０２                 */
            strcat(sql_buf3, "?,");     /* 企画バージョン０２           */
            strcat(sql_buf3, "?,");     /* ポイントカテゴリ０２         */
            strcat(sql_buf3, "?,");     /* ポイント種別０２             */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０２       */
            strcat(sql_buf3, "?,");     /* 付与ポイント０２             */
            strcat(sql_buf3, "?,");     /* 利用ポイント０２             */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０２         */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０２             */
            strcat(sql_buf3, "?,");     /* 商品購入数０２               */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０２   */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０２         */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０２         */
            strcat(sql_buf3, "?");      /* 購買区分０２                 */
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");     /* 付与利用区分０３             */
            strcat(sql_buf3, "?,");     /* 明細番号０３                 */
            strcat(sql_buf3, "?,");     /* 企画ＩＤ０３                 */
            strcat(sql_buf3, "?,");     /* 企画バージョン０３           */
            strcat(sql_buf3, "?,");     /* ポイントカテゴリ０３         */
            strcat(sql_buf3, "?,");     /* ポイント種別０３             */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０３       */
            strcat(sql_buf3, "?,");     /* 付与ポイント０３             */
            strcat(sql_buf3, "?,");     /* 利用ポイント０３             */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０３         */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０３             */
            strcat(sql_buf3, "?,");     /* 商品購入数０３               */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０３   */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０３         */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０３         */
            strcat(sql_buf3, "?");      /* 購買区分０３                 */
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");     /* 付与利用区分０４             */
            strcat(sql_buf3, "?,");     /* 明細番号０４                 */
            strcat(sql_buf3, "?,");     /* 企画ＩＤ０４                 */
            strcat(sql_buf3, "?,");     /* 企画バージョン０４           */
            strcat(sql_buf3, "?,");     /* ポイントカテゴリ０４         */
            strcat(sql_buf3, "?,");     /* ポイント種別０４             */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０４       */
            strcat(sql_buf3, "?,");     /* 付与ポイント０４             */
            strcat(sql_buf3, "?,");     /* 利用ポイント０４             */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０４         */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０４             */
            strcat(sql_buf3, "?,");     /* 商品購入数０４               */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０４   */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０４         */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０４         */
            strcat(sql_buf3, "?");      /* 購買区分０４                 */
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");     /* 付与利用区分０５            */
            strcat(sql_buf3, "?,");     /* 明細番号０５                */
            strcat(sql_buf3, "?,");     /* 企画ＩＤ０５                */
            strcat(sql_buf3, "?,");     /* 企画バージョン０５          */
            strcat(sql_buf3, "?,");     /* ポイントカテゴリ０５        */
            strcat(sql_buf3, "?,");     /* ポイント種別０５            */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０５      */
            strcat(sql_buf3, "?,");     /* 付与ポイント０５            */
            strcat(sql_buf3, "?,");     /* 利用ポイント０５            */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０５        */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０５            */
            strcat(sql_buf3, "?,");     /* 商品購入数０５              */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０５  */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０５        */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０５        */
            strcat(sql_buf3, "?");      /* 購買区分０５                */
        }

        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_06.intVal() != 0){
            strcat(sql_buf3, ",");
            strcat(sql_buf3, "?,");     /* 付与利用区分０６            */
            strcat(sql_buf3, "?,");     /* 明細番号０６                */
            strcat(sql_buf3, "?,");     /* 企画ＩＤ０６                */
            strcat(sql_buf3, "?,");     /* 企画バージョン０６          */
            strcat(sql_buf3, "?,");     /* ポイントカテゴリ０６        */
            strcat(sql_buf3, "?,");     /* ポイント種別０６            */
            strcat(sql_buf3, "?,");     /* 買上高ポイント種別０６      */
            strcat(sql_buf3, "?,");     /* 付与ポイント０６            */
            strcat(sql_buf3, "?,");     /* 利用ポイント０６            */
            strcat(sql_buf3, "?,");     /* ポイント対象金額０６        */
            strcat(sql_buf3, "?,");     /* ＪＡＮコード０６            */
            strcat(sql_buf3, "?,");     /* 商品購入数０６              */
            strcat(sql_buf3, "?,");     /* 商品パーセントＰ付与率０６  */
            strcat(sql_buf3, "?,");     /* 通常期間限定区分０６        */
            strcat(sql_buf3, "?,");     /* ポイント有効期限０６        */
            strcat(sql_buf3, "?");     /* 購買区分０６                */
        }

        strcat(sql_buf3, " ) ");
        strcpy(sql_buf, "INSERT INTO ");
        strcat(sql_buf, tbl_nam);
        strcat(sql_buf, sql_buf2);
        strcat(sql_buf, sql_buf3);

        if (DBG_LOG) {
            C_DbgMsg("InsertDayPointUchiwake : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(cmBTpmdfBDto.h_str_sql, 0x00, sizeof(cmBTpmdfBDto.h_str_sql));
        strcpy(cmBTpmdfBDto.h_str_sql, sql_buf);

        // EXEC SQL PREPARE sql_stat5 from :h_str_sql;
//        SqlstmDto sqlca = sqlcaManager.get("sql_stat5");
        sqlca.sql.arr = cmBTpmdfBDto.h_str_sql.strVal();
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("InsertDayPointUchiwake : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            sprintf( out_format_buf, "年月日 =[%d], 顧客番号=[%s],処理通番号=[%f]",
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.arr,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.floatVal());
            APLOG_WT( "904", 0, null, "INSERT PREPARE",
                    sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);

            return C_const_NG;
        }

        /* INSERT文を実行する */
        if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_06.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                            */  /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                           */   /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                            */   /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                               */   /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,                     */   /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                         */   /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                         */   /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                        */   /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,                    */   /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,                    */   /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,        */   /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                        */   /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                        */   /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,              */   /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                            */   /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,                   */   /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,        */   /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,             */   /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,                   */   /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01,                         */   /* 購買区分０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02,                     */   /* 付与利用区分０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_02,                         */   /* 明細番号０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_02,                         */   /* 企画ＩＤ０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02,                        */   /* 企画バージョン０２           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_02,                    */   /* ポイントカテゴリ０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02,                    */   /* ポイント種別０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02,        */   /* 買上高ポイント種別０２       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_02,                        */   /* 付与ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_02,                        */   /* 利用ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02,              */   /* ポイント対象金額０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_02,                            */   /* ＪＡＮコード０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02,                   */   /* 商品購入数０２               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02,        */   /* 商品パーセントＰ付与率０２   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02,             */   /* 通常期間限定区分０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02,                   */   /* ポイント有効期限０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02,                         */   /* 購買区分０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03,                     */   /* 付与利用区分０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_03,                         */   /* 明細番号０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_03,                         */   /* 企画ＩＤ０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03,                        */   /* 企画バージョン０３           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_03,                    */   /* ポイントカテゴリ０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03,                    */   /* ポイント種別０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03,        */   /* 買上高ポイント種別０３       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_03,                        */   /* 付与ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_03,                        */   /* 利用ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03,              */   /* ポイント対象金額０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_03,                            */   /* ＪＡＮコード０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03,                   */   /* 商品購入数０３               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03,        */   /* 商品パーセントＰ付与率０３   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03,             */   /* 通常期間限定区分０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03,                   */   /* ポイント有効期限０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03,                         */   /* 購買区分０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04,                     */   /* 付与利用区分０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_04,                         */   /* 明細番号０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_04,                         */   /* 企画ＩＤ０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04,                        */   /* 企画バージョン０４           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_04,                    */   /* ポイントカテゴリ０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04,                    */   /* ポイント種別０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04,        */   /* 買上高ポイント種別０４       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_04,                        */   /* 付与ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_04,                        */   /* 利用ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04,              */   /* ポイント対象金額０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_04,                            */   /* ＪＡＮコード０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04,                   */   /* 商品購入数０４               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04,        */   /* 商品パーセントＰ付与率０４   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04,             */   /* 通常期間限定区分０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04,                   */   /* ポイント有効期限０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04,                         */   /* 購買区分０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_05,                     */   /* 付与利用区分０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_05,                         */   /* 明細番号０５                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_05,                         */   /* 企画ＩＤ０５                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_05,                        */   /* 企画バージョン０５           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_05,                    */   /* ポイントカテゴリ０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_05,                    */   /* ポイント種別０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05,        */   /* 買上高ポイント種別０５       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_05,                        */   /* 付与ポイント０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_05,                        */   /* 利用ポイント０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_05,              */   /* ポイント対象金額０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_05,                            */   /* ＪＡＮコード０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_05,                   */   /* 商品購入数０５               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_05,        */   /* 商品パーセントＰ付与率０５   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_05,             */   /* 通常期間限定区分０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_05,                   */   /* ポイント有効期限０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05,                         */   /* 購買区分０５                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_06,                     */   /* 付与利用区分０６             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_06,                         */   /* 明細番号０６                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_06,                         */   /* 企画ＩＤ０６                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_06,                        */   /* 企画バージョン０６           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_06,                    */   /* ポイントカテゴリ０６         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_06,                    */   /* ポイント種別０６             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_06,        */   /* 買上高ポイント種別０６       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_06,                        */   /* 付与ポイント０６             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_06,                        */   /* 利用ポイント０６             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_06,              */   /* ポイント対象金額０６         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_06,                            */   /* ＪＡＮコード０６             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_06,                   */   /* 商品購入数０６               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_06,        */   /* 商品パーセントＰ付与率０６   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_06,             */   /* 通常期間限定区分０６         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_06,                   */   /* ポイント有効期限０６         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_06;                         */   /* 購買区分０６                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_02,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_03,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_04,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_05,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_06,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_06.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_06.longVal());
        }
        else if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                         */     /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                        */      /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                         */      /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                            */      /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,                  */      /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                      */      /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                      */      /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                     */      /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,                 */      /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,                 */      /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,     */      /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                     */      /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                     */      /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,           */      /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                         */      /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,                */      /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,     */      /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,          */      /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,                */      /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01,                      */      /* 購買区分０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02,                  */      /* 付与利用区分０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_02,                      */      /* 明細番号０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_02,                      */      /* 企画ＩＤ０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02,                     */      /* 企画バージョン０２           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_02,                 */      /* ポイントカテゴリ０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02,                 */      /* ポイント種別０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02,     */      /* 買上高ポイント種別０２       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_02,                     */      /* 付与ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_02,                     */      /* 利用ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02,           */      /* ポイント対象金額０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_02,                         */      /* ＪＡＮコード０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02,                */      /* 商品購入数０２               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02,     */      /* 商品パーセントＰ付与率０２   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02,          */      /* 通常期間限定区分０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02,                */      /* ポイント有効期限０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02,                      */      /* 購買区分０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03,                  */      /* 付与利用区分０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_03,                      */      /* 明細番号０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_03,                      */      /* 企画ＩＤ０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03,                     */      /* 企画バージョン０３           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_03,                 */      /* ポイントカテゴリ０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03,                 */      /* ポイント種別０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03,     */      /* 買上高ポイント種別０３       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_03,                     */      /* 付与ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_03,                     */      /* 利用ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03,           */      /* ポイント対象金額０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_03,                         */      /* ＪＡＮコード０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03,                */      /* 商品購入数０３               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03,     */      /* 商品パーセントＰ付与率０３   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03,          */      /* 通常期間限定区分０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03,                */      /* ポイント有効期限０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03,                      */      /* 購買区分０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04,                  */      /* 付与利用区分０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_04,                      */      /* 明細番号０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_04,                      */      /* 企画ＩＤ０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04,                     */      /* 企画バージョン０４           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_04,                 */      /* ポイントカテゴリ０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04,                 */      /* ポイント種別０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04,     */      /* 買上高ポイント種別０４       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_04,                     */      /* 付与ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_04,                     */      /* 利用ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04,           */      /* ポイント対象金額０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_04,                         */      /* ＪＡＮコード０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04,                */      /* 商品購入数０４               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04,     */      /* 商品パーセントＰ付与率０４   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04,          */      /* 通常期間限定区分０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04,                */      /* ポイント有効期限０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04,                      */      /* 購買区分０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_05,                  */      /* 付与利用区分０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_05,                      */      /* 明細番号０５                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_05,                      */      /* 企画ＩＤ０５                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_05,                     */      /* 企画バージョン０５           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_05,                 */      /* ポイントカテゴリ０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_05,                 */      /* ポイント種別０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05,     */      /* 買上高ポイント種別０５       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_05,                     */      /* 付与ポイント０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_05,                     */      /* 利用ポイント０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_05,           */      /* ポイント対象金額０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_05,                         */      /* ＪＡＮコード０５             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_05,                */      /* 商品購入数０５               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_05,     */      /* 商品パーセントＰ付与率０５   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_05,          */      /* 通常期間限定区分０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_05,                */      /* ポイント有効期限０５         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05;                      */      /* 購買区分０５                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_02,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_03,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_04,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_05,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_05.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_05.longVal());
        }
        else if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                      */        /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                     */         /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                      */         /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                         */         /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,               */         /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                   */         /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                   */         /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                  */         /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,              */         /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,              */         /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,  */         /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                  */         /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                  */         /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,        */         /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                      */         /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,             */         /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,  */         /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,       */         /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,             */         /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01,                   */         /* 購買区分０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02,               */         /* 付与利用区分０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_02,                   */         /* 明細番号０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_02,                   */         /* 企画ＩＤ０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02,                  */         /* 企画バージョン０２           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_02,              */         /* ポイントカテゴリ０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02,              */         /* ポイント種別０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02,  */         /* 買上高ポイント種別０２       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_02,                  */         /* 付与ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_02,                  */         /* 利用ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02,        */         /* ポイント対象金額０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_02,                      */         /* ＪＡＮコード０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02,             */         /* 商品購入数０２               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02,  */         /* 商品パーセントＰ付与率０２   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02,       */         /* 通常期間限定区分０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02,             */         /* ポイント有効期限０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02,                   */         /* 購買区分０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03,               */         /* 付与利用区分０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_03,                   */         /* 明細番号０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_03,                   */         /* 企画ＩＤ０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03,                  */         /* 企画バージョン０３           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_03,              */         /* ポイントカテゴリ０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03,              */         /* ポイント種別０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03,  */         /* 買上高ポイント種別０３       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_03,                  */         /* 付与ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_03,                  */         /* 利用ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03,        */         /* ポイント対象金額０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_03,                      */         /* ＪＡＮコード０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03,             */         /* 商品購入数０３               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03,  */         /* 商品パーセントＰ付与率０３   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03,       */         /* 通常期間限定区分０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03,             */         /* ポイント有効期限０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03,                   */         /* 購買区分０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04,               */         /* 付与利用区分０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_04,                   */         /* 明細番号０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_04,                   */         /* 企画ＩＤ０４                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04,                  */         /* 企画バージョン０４           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_04,              */         /* ポイントカテゴリ０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04,              */         /* ポイント種別０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04,  */         /* 買上高ポイント種別０４       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_04,                  */         /* 付与ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_04,                  */         /* 利用ポイント０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04,        */         /* ポイント対象金額０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_04,                      */         /* ＪＡＮコード０４             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04,             */         /* 商品購入数０４               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04,  */         /* 商品パーセントＰ付与率０４   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04,       */         /* 通常期間限定区分０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04,             */         /* ポイント有効期限０４         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04;                   */         /* 購買区分０４                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_02,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_03,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_04,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_04.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_04.longVal());
        }
        else if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                          */    /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                         */     /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                          */     /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                             */     /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,                   */     /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                       */     /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                       */     /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                      */     /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,                  */     /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,                  */     /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,      */     /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                      */     /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                      */     /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,            */     /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                          */     /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,                 */     /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,      */     /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,           */     /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,                 */     /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01,                       */     /* 購買区分０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02,                   */     /* 付与利用区分０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_02,                       */     /* 明細番号０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_02,                       */     /* 企画ＩＤ０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02,                      */     /* 企画バージョン０２           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_02,                  */     /* ポイントカテゴリ０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02,                  */     /* ポイント種別０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02,      */     /* 買上高ポイント種別０２       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_02,                      */     /* 付与ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_02,                      */     /* 利用ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02,            */     /* ポイント対象金額０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_02,                          */     /* ＪＡＮコード０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02,                 */     /* 商品購入数０２               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02,      */     /* 商品パーセントＰ付与率０２   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02,           */     /* 通常期間限定区分０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02,                 */     /* ポイント有効期限０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02,                       */     /* 購買区分０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03,                   */     /* 付与利用区分０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_03,                       */     /* 明細番号０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_03,                       */     /* 企画ＩＤ０３                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03,                      */     /* 企画バージョン０３           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_03,                  */     /* ポイントカテゴリ０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03,                  */     /* ポイント種別０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03,      */     /* 買上高ポイント種別０３       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_03,                      */     /* 付与ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_03,                      */     /* 利用ポイント０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03,            */     /* ポイント対象金額０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_03,                          */     /* ＪＡＮコード０３             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03,                 */     /* 商品購入数０３               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03,      */     /* 商品パーセントＰ付与率０３   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03,           */     /* 通常期間限定区分０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03,                 */     /* ポイント有効期限０３         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03;                       */     /* 購買区分０３                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_02,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_03,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_03.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_03.intVal());
        }
        else if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                          */    /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                         */     /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                          */     /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                             */     /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,                   */     /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                       */     /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                       */     /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                      */     /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,                  */     /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,                  */     /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,      */     /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                      */     /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                      */     /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,            */     /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                          */     /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,                 */     /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,      */     /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,           */     /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,                 */     /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01,                       */     /* 購買区分０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02,                   */     /* 付与利用区分０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_02,                       */     /* 明細番号０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_02,                       */     /* 企画ＩＤ０２                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02,                      */     /* 企画バージョン０２           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_02,                  */     /* ポイントカテゴリ０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02,                  */     /* ポイント種別０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02,      */     /* 買上高ポイント種別０２       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_02,                      */     /* 付与ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_02,                      */     /* 利用ポイント０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02,            */     /* ポイント対象金額０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_02,                          */     /* ＪＡＮコード０２             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02,                 */     /* 商品購入数０２               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02,      */     /* 商品パーセントＰ付与率０２   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02,           */     /* 通常期間限定区分０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02,                 */     /* ポイント有効期限０２         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02;                       */     /* 購買区分０２                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_02,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_02.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_02.longVal());
        }
        else if(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.intVal() != 0){
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,                         */     /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,                        */      /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,                         */      /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no,                            */      /* 枝番                         */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01,                  */      /* 付与利用区分０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.meisai_no_01,                      */      /* 明細番号０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_id_01,                      */      /* 企画ＩＤ０１                 */
            /* :hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01,                     */      /* 企画バージョン０１           */
            /* :hs_day_point_hibetsu_uchiwake_data.point_category_01,                 */      /* ポイントカテゴリ０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01,                 */      /* ポイント種別０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01,     */      /* 買上高ポイント種別０１       */
            /* :hs_day_point_hibetsu_uchiwake_data.fuyo_point_01,                     */      /* 付与ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.riyo_point_01,                     */      /* 利用ポイント０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01,           */      /* ポイント対象金額０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.jan_cd_01,                         */      /* ＪＡＮコード０１             */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01,                */      /* 商品購入数０１               */
            /* :hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01,     */      /* 商品パーセントＰ付与率０１   */
            /* :hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01,          */      /* 通常期間限定区分０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01,                */      /* ポイント有効期限０１         */
            /* :hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01;                      */      /* 購買区分０１                 */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_riyo_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.meisai_no_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_id_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kikaku_ver_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_category_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kaiage_daka_point_syubetsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.fuyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.riyo_point_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_taisho_kingaku_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.jan_cd_01,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_konyu_su_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shohin_percent_p_fuyoritsu_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.tujo_kikan_gentei_kbn_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.point_yukokigen_01.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kobai_kbn_01.longVal());
        }
        else{
            /*EXEC SQL EXECUTE sql_stat5 USING*/
            /*:hs_day_point_hibetsu_uchiwake_data.system_ymd,         */                     /* システム年月日               */
            /* :hs_day_point_hibetsu_uchiwake_data.kokyaku_no,        */                      /* 顧客番号                     */
            /* :hs_day_point_hibetsu_uchiwake_data.shori_seq,         */                      /* 処理通番                     */
            /* :hs_day_point_hibetsu_uchiwake_data.eda_no;            */                      /* 枝番                         */
            sqlca.restAndExecute(cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq.longVal(),
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.eda_no.longVal());
        }


        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("InsertDayPointUchiwake : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf( out_format_buf, "年月日 =[%d], 顧客番号=[%s],処理通番号=[%f]",
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.system_ymd,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.kokyaku_no.arr,
                    cmBTpmdfBDto.hs_day_point_hibetsu_uchiwake_data.shori_seq);
            APLOG_WT( "904", 0, null, "INSERT", sqlca.sqlcode,
                    tbl_nam, out_format_buf, 0, 0);

            return C_const_NG;
        }

        if (DBG_LOG) {
            C_DbgEnd("InsertDayPointUchiwake処理", 0, 0, 0);
        }

        return C_const_OK;
    }

}
