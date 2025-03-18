package jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB.dto.AS_KIKANGENTEI_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB.dto.MONTH_ZENKAKU;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *   プログラム名   ： 期間限定ポイント集計（cmBTkzdkB.pc）
 *
 *   【処理概要】
 *     期間限定ポイントの残高や収支（発生、利用）を管理するための会計データを
 *     作成する。
 *     ・月次の会計向けポイント集計情報として、店舗単位に各ポイント数を集計し
 *       ポイント集計表に出力する。
 *
 *   【引数説明】
 *     -d処理日付                   :（任意）処理日付
 *     -DEBUG(-debug)               :（任意）デバッグモードでの実行
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
 *     30.00 :  2025/02/25 SSI.上野：初版
 *     31.00 :  2021/05/13 SSI.上野：CF-503　クリアＰ数をマイナス値で出力
 *     32.00 :  2021/08/13 NBS.山本：CF-486　売上金額合計を追加
 *     33.00 :  2021/09/13 NBS.山本：CF-550　期間限定利用Ｐ前月を追加
 *     34.00 :  2021/12/09 SSI.張  ：CF-104　企業名の出力判定を追加(MK)
 *
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C)  2020 NTT DATA BUSINESS SYSTEMS CORPORATION
 ******************************************************************************/
@Service
public class CmBTkzdkBServiceImpl extends CmABfuncLServiceImpl implements CmBTkzdkBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL INCLUDE  sqlca.h;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE  AS_KIKANGENTEI_INFO.h;     /* AS期間限定ポイント集計情報    */
    AS_KIKANGENTEI_INFO_TBL as_kgp_t = new AS_KIKANGENTEI_INFO_TBL();      /* AS期間限定ポイント集計情報    */
    ItemDto          h_kyu_hansya_name = new ItemDto(30*3+1);         /* 旧販社名                  */
    ItemDto          h_mise_kanji = new ItemDto(40*3+1);              /* 漢字店舗名称              */
    ItemDto          h_batdate_ymd = new ItemDto();                     /* バッチ処理日付(当日)      */
    ItemDto          h_batdate_ymd_prev = new ItemDto();                /* バッチ処理日付(前月)      */
    ItemDto          hs_batdate_ymd = new ItemDto(9);                 /* バッチ処理日付(当日)      */
    ItemDto          h_batdate_ym = new ItemDto();                      /* 集計対象月                */
    ItemDto          h_batdate_ym_start_date = new ItemDto();           /* 集計対象月初日            */
    ItemDto          h_batdate_ym_end_date = new ItemDto();             /* 集計対象月末日            */
    ItemDto          h_batdate_pym_start_date = new ItemDto();          /* 集計対象前月初日          */
    ItemDto          h_batdate_pym_end_date = new ItemDto();            /* 集計対象前月末日          */
    ItemDto          h_batdate_pym = new ItemDto();                     /* 集計対象月の前月          */
    ItemDto          h_batdate_nym = new ItemDto();                     /* 集計対象月の翌月          */
    StringDto str_sql = new StringDto(4096*24);                  /* 実行用SQL文字列１         */
    ItemDto          h_program_id = new ItemDto(20+1);                /* プログラムID              */
    ItemDto          h_system_date = new ItemDto(9);                  /* 作成日                    */
    ItemDto          h_system_time = new ItemDto(9);                  /* 作成時刻                  */

//    EXEC SQL END DECLARE SECTION;

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
            int DEF_OFF=              0 ;   /* OFF                                */
            int DEF_ON =              1 ;   /* ON                                 */
            /*-----  引数（引数の種類分定義する）----------*/
            String DEF_ARG_D = "-d" ;               /* 処理日付                           */
            String DEF_DEBUG = "-DEBUG" ;          /* デバッグスイッチ                   */
            String DEF_debug = "-debug" ;          /* デバッグスイッチ                   */
            /*---------------------------------------------*/
            String C_PRGNAME = "期間限定Ｐ集計表"      ;    /* APログ用機能名             */
            String C_FNAME_KP= "期間限定ポイント集計表_" ;  /* 期間限定Ｐ集計表ファイル名 */
            String C_P_DATE  = "作成日    "          ;      /* 会計帳票出力用             */
            String C_P_TIME  = "作成時刻  "          ;      /* 会計帳票出力用             */
            String C_MISENAME_ZENSHA = "全社計"         ;     /* 店舗名(全社集計)           */
            String C_MISENAME_HANSHA = "企業計"        ;      /* 店舗名(企業別集計)         */
            int C_MISENO   = 9999   ;                    /* 店舗ＣＤ(企業別/全社集計)  */
            int C_KAISHACD = 99      ;                   /* 会社コード(全社集計)       */
            String C_NULLSTR =  "NULL";                      /* 空文字置換文字列           */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int           arg_d_chk;                /* 引数dチェック用                    */
    long          g_kp_cnt;                 /* ファイル出力件数(期間限定Ｐ集計表) */
    StringDto          out_file_dir = new StringDto(4096);       /* ワークディレクトリ                 */
    StringDto          kp_fl_name = new StringDto(256);          /* 出力ファイル名(期間限定Ｐ集計表)   */
    StringDto          kp_fl_name_sjis = new StringDto(256);     /* 出力ファイル名(期間限定Ｐ集計表)   */
    StringDto          log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                  */
    FileStatusDto          fp_kp;               /* 出力ファイルポインタ(期間限定Ｐ集計表) */
//    typedef struct
//    {
//        int      i_MM;                      /* 集計対象月(４ヶ月)                 */
//        char     c_MMZen[16];               /* 集計対象月テーブル項目名(対象月)   */
//    } MONTH_ZENKAKU;
    MONTH_ZENKAKU[] g_MMZenkaku = new MONTH_ZENKAKU[5];			/* 前月・当月・～３カ月               */

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
    public MainResultDto main(int argc, String[] argv)
    {
        int     rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int     arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto    arg_Work1 = new StringDto(256);                 /* Work Buffer1                   */
        String env_wrk = null;                       /* 環境変数取得用                 */
        StringDto    bat_date_ymd = new StringDto(9);                /* バッチ処理年月日(一時格納)     */
        StringDto    wk_buf = new StringDto(256);                    /* 出力ファイル名SJIS変換用       */
        int     ret;
        IntegerDto len = new IntegerDto();


        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /* 変数初期化 */
        g_kp_cnt = 0;
        h_batdate_ymd.arr = 0;
        h_batdate_ymd_prev.arr = 0;
        h_batdate_ym.arr = 0;
        h_batdate_ym_start_date.arr = 0;
        h_batdate_ym_end_date.arr = 0;
        h_batdate_pym_start_date.arr = 0;
        h_batdate_pym_end_date.arr = 0;
        h_batdate_pym.arr = 0;
        h_batdate_nym.arr = 0;
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        memset(kp_fl_name, 0x00, sizeof(kp_fl_name));
        memset(kp_fl_name_sjis, 0x00, sizeof(kp_fl_name_sjis));
        memset(hs_batdate_ymd, 0x00, sizeof(hs_batdate_ymd));
        memset(h_program_id, 0x00, sizeof(h_program_id));
        arg_d_chk  = DEF_OFF;

        memset(g_MMZenkaku, 0x00, sizeof(g_MMZenkaku));

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname",rtn_cd,
                    0, 0, 0, 0);
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
            APLOG_WT("903", 0, null, "C_StartBatDbg",rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("*** main処理 ***");
                /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
                C_DbgMsg("*** main *** 入力引数の数[%d]\n", argc-1);
                /*------------------------------------------------------------*/
        }

        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                        /*------------------------------------------------------------*/
            }

            /* 引数の文字列が "-DEBUG" または "-debug" の場合 */
            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 ==strcmp(arg_Work1, DEF_debug)) {
                continue;

                /* 引数の文字列が "-d" で始まる場合 */
            } else if(0 == memcmp(arg_Work1, DEF_ARG_D, 2)){

                /* パラメータのチェックをする */
                rtn_cd = cmBTkzdkB_ChkArgdInf(arg_Work1);

                /* チェックがOKだったら、ホスト変数に代入する */
                if (rtn_cd == C_const_OK){
                    strncpy(hs_batdate_ymd, arg_Work1.substring(2), 8);
                    h_batdate_ymd.arr = atoi(hs_batdate_ymd);
                }

                /* それ以外(規定外パラメータ) */
            } else {                                    /* 定義外パラメータ     */
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了   */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
                /*------------------------------------------------------------*/
        }
            rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                        C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd,rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（当日）        */
        /*-------------------------------------*/
        if (StringUtils.isEmpty(hs_batdate_ymd.strVal())) {

        rtn_status = new IntegerDto();
        memset(bat_date_ymd, 0x00, sizeof(bat_date_ymd));

        /* バッチ処理日付を取得（当日） */
        rtn_cd = C_GetBatDate( 0, bat_date_ymd, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG){
                        /*----------------------------------------------------------------*/
                        C_DbgMsg("*** main *** バッチ処理日(前日)取得NG rtn=[%d]\n",
                                rtn_cd);
                        /*----------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd,rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }
        strcpy(hs_batdate_ymd, bat_date_ymd);
        h_batdate_ymd.arr = atoi(hs_batdate_ymd);
    }

        /*-------------------------------------*/
        /* 集計対象日付を取得                  */
        /*-------------------------------------*/
//        EXEC SQL SELECT TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -1), 'MM'), 'YYYYMMDD')),
//        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -1)), 'YYYYMMDD')),
//        TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -2), 'MM'), 'YYYYMMDD')),
//        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -2)), 'YYYYMMDD')),
//        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -1), 'YYYYMM')),
//        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -2), 'YYYYMM')),
//        TO_NUMBER(TO_CHAR(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), 'YYYYMM')),
//        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(:hs_batdate_ymd, 'YYYYMMDD'), -1), 'YYYYMMDD'))
//        INTO :h_batdate_ym_start_date,
//         :h_batdate_ym_end_date,
//         :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//         :h_batdate_ym,
//         :h_batdate_pym,
//         :h_batdate_nym,
//         :h_batdate_ymd_prev
//        FROM DUAL;

        sqlca.sql = new StringDto("SELECT TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'MM'), 'YYYYMMDD'))," +
                "        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1)), 'YYYYMMDD'))," +
                "        TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2), 'MM'), 'YYYYMMDD'))," +
                "        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2)), 'YYYYMMDD'))," +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'YYYYMM'))," +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2), 'YYYYMM'))," +
                "        TO_NUMBER(TO_CHAR(TO_DATE(?, 'YYYYMMDD'), 'YYYYMM'))," +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'YYYYMMDD'))" +
                "        FROM DUAL");
        sqlca.restAndExecute(hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd);
        sqlca.fetch();
        sqlca.recData(h_batdate_ym_start_date,
                h_batdate_ym_end_date,
                h_batdate_pym_start_date,
                h_batdate_pym_end_date,
                h_batdate_ym,
                h_batdate_pym,
                h_batdate_nym,
                h_batdate_ymd_prev);

        /* エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {

            sprintf( log_format_buf, "バッチ処理日付 = %s\n", hs_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT",sqlca.sqlcode,
                    "DUAL", log_format_buf, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 処理日付=[%d]\n", h_batdate_ymd);
                C_DbgMsg("*** main *** 集計対象月=[%d]\n", h_batdate_ym);
                C_DbgMsg("*** main *** 集計対象前月=[%d]\n", h_batdate_pym);
                C_DbgMsg("*** main *** 集計対象翌月=[%d]\n", h_batdate_nym);
                C_DbgMsg("*** main *** 集計対象日付(月初日)=[%d]\n", h_batdate_ym_start_date);
                C_DbgMsg("*** main *** 集計対象日付(月末日)=[%d]\n", h_batdate_ym_end_date);
                C_DbgMsg("*** main *** 集計対象日付(前月初日)=[%d]\n", h_batdate_pym_start_date);
                C_DbgMsg("*** main *** 集計対象日付(前月末日)=[%d]\n", h_batdate_pym_end_date);
                /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 出力ファイル名                      */
        /*-------------------------------------*/
        sprintf(kp_fl_name, "%s%d.csv", C_FNAME_KP, h_batdate_ym);

        /* ファイル名をSJISに変換 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        len = new IntegerDto();
        if((ret = C_ConvUT2SJ(kp_fl_name, strlen(kp_fl_name), wk_buf, len)) != 0){
        /* エラー発生 */
            if (DBG_LOG){
                    C_DbgMsg("*** main *** 期間限定Ｐ集計表ファイル名変換エラー[%d]\n",
                            ret);
            }
        APLOG_WT("903", 0, null, "C_ConvUT2SJ",ret, 0, 0, 0, 0);

        /* 処理をNGで終了 */
        return exit( C_const_APNG );
    }
    else {
        /* ポイント集計表ファイル名（SJIS） */
        sprintf(kp_fl_name_sjis, "%s", wk_buf);
    }

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n",
                        "CM_APWORK_DATE");
                /*-------------------------------------------------------------*/
        }
            env_wrk = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_wrk)) {
            if (DBG_LOG){
                        /*---------------------------------------------*/
                        C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
                        /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* 出力ファイルDIRセット */
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
                /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 作成日、作成時刻を取得              */
        /*-------------------------------------*/
        memset(h_system_date, 0x00, sizeof(h_system_date));
        memset(h_system_time, 0x00, sizeof(h_system_time));

//        EXEC SQL SELECT
//        TO_CHAR(sysdate, 'YYYYMMDD'),
//                TO_CHAR(sysdate, 'HH24:MI:SS')
//        INTO  :h_system_date,
//                   :h_system_time
//        FROM dual;

        sqlca.sql = new StringDto("SELECT TO_CHAR(sysdate(), 'YYYYMMDD'), TO_CHAR(sysdate(), 'HH24:MI:SS') FROM dual");
        sqlca.restAndExecute();
        sqlca.fetch();
        sqlca.recData(h_system_date, h_system_time);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "904", 0, null, "SELECT",sqlca.sqlcode,
                    "作成日の取得に失敗しました", 0, 0, 0);
            return exit( C_const_NG ) ;
        }

        /*-------------------------------------*/
        /* 対象月（０１～１２）を取得          */
        /*-------------------------------------*/
        cmBTkzdkB_SetMonth();

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*-------------------------*/
        /*  集計前処理             */
        /*-------------------------*/
        rtn_cd = cmBTkzdkB_PreTotal();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** cmBTkzdkB_PreTotal NG rtn=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "集計前処理に失敗しました", 0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            return exit( C_const_APNG );
        }

//        EXEC SQL COMMIT WORK;
        sqlca.commit();

        /*------------------------------------*/
        /*  期間限定ポイント集計情報作成処理  */
        /*------------------------------------*/
        rtn_cd = cmBTkzdkB_CreKikangenteiPData();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** cmBTkzdkB_CreKikangenteiPData NG rtn=[%d]\n",
                                rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "期間限定Ｐ集計表情報作成処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            return exit( C_const_APNG );
        }

        /*------------------------------------*/
        /*  期間限定ポイント集計表作成処理    */
        /*------------------------------------*/
        rtn_cd = cmBTkzdkB_CreKikangenteiPFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** cmBTkzdkB_CreKikangenteiPFile NG rtn=[%d]\n",
                                rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "期間限定Ｐ集計表作成処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* 出力ファイルクローズ */
            fclose(fp_kp);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("*** main処理 ***", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        sprintf( log_format_buf, "%s/%s", out_file_dir, kp_fl_name );
        APLOG_WT("105", 0, null, log_format_buf,g_kp_cnt,
            0, 0, 0, 0);

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /* コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit(C_const_APOK);
    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_ChkArgdInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTkzdkB_ChkArgdInf( char *Arg_in )                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-d スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-d スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int  cmBTkzdkB_ChkArgdInf( StringDto Arg_in )
    {

        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart( "*** cmBTkzdkB_ChkArgdInf処理 ***" );
                /*---------------------------------------------------------------------*/
        }
        /*  重複指定チェック   */
        if (arg_d_chk != DEF_OFF) {

            if (DBG_LOG){
                        /*-------------------------------------------------------------*/
                        C_DbgMsg( "*** cmBTkzdkB_ChkArgdInf *** 重複指定NG = %s\n", Arg_in ) ;
                        /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数が複数設定されています（%s）", Arg_in);
            return C_const_NG;
        }
        arg_d_chk = DEF_ON;

        /*  設定値Nullチェック  */
        if (StringUtils.isEmpty(Arg_in.arr.substring(2))) {
            if (DBG_LOG){
                    /*-------------------------------------------------------------*/
                    C_DbgMsg( "*** cmBTkzdkB_ChkArgdInf *** 設定値Null = %s\n", Arg_in );
                    /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
            return C_const_NG;
        }

        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgEnd( "*** cmBTkzdkB_ChkArgdInf処理 ***", 0, 0, 0 );
                /*---------------------------------------------------------------------*/
        }
        return C_const_OK;
    }


/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_PreTotal                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTkzdkB_PreTotal()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      TS利用可能ポイント情報、TSポイント累計情報から非会員の                */
    /*      ポイント情報をクリアする。HSポイント日別情報から店舗別集計値を        */
    /*      ポイント集計情報に登録する。                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int  cmBTkzdkB_PreTotal()
    {

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgStart("*** cmBTkzdkB_PreTotal処理 ***");
                /*------------------------------------------------------------------------*/
        }

        /* 利用可能ポイント情報の更新 */
//        EXEC SQL UPDATE TS利用可能ポイント情報
//        SET 利用可能期間限定Ｐ０１ =  0,
//                利用可能期間限定Ｐ０２ =  0,
//                利用可能期間限定Ｐ０３ =  0,
//                利用可能期間限定Ｐ０４ =  0,
//                利用可能期間限定Ｐ０５ =  0,
//                利用可能期間限定Ｐ０６ =  0,
//                利用可能期間限定Ｐ０７ =  0,
//                利用可能期間限定Ｐ０８ =  0,
//                利用可能期間限定Ｐ０９ =  0,
//                利用可能期間限定Ｐ１０ =  0,
//                利用可能期間限定Ｐ１１ =  0,
//                利用可能期間限定Ｐ１２ =  0,
//                期間限定Ｐ失効フラグ = 4096,
//        最終買上日 = :h_batdate_ymd,
//            最終更新日 = :h_batdate_ymd,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :h_program_id
//        WHERE 顧客番号 = 9999999999999;

        sqlca.sql = new StringDto("UPDATE TS利用可能ポイント情報" +
                "    SET 利用可能期間限定Ｐ０１ =  0," +
                "        利用可能期間限定Ｐ０２ =  0," +
                "        利用可能期間限定Ｐ０３ =  0," +
                "        利用可能期間限定Ｐ０４ =  0," +
                "        利用可能期間限定Ｐ０５ =  0," +
                "        利用可能期間限定Ｐ０６ =  0," +
                "        利用可能期間限定Ｐ０７ =  0," +
                "        利用可能期間限定Ｐ０８ =  0," +
                "        利用可能期間限定Ｐ０９ =  0," +
                "        利用可能期間限定Ｐ１０ =  0," +
                "        利用可能期間限定Ｐ１１ =  0," +
                "        利用可能期間限定Ｐ１２ =  0," +
                "        期間限定Ｐ失効フラグ = 4096," +
                "    最終買上日 = ?," +
                "    最終更新日 = ?," +
                "    最終更新日時 = SYSDATE()," +
                "    最終更新プログラムＩＤ = ?" +
                "WHERE 顧客番号 = 9999999999999");
        sqlca.restAndExecute(h_batdate_ymd, h_batdate_ymd, h_program_id);

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_PreTotal *** TS利用可能ポイント情報 UPDATE " +
                        "sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf( log_format_buf, "顧客番号 = 9999999999999");
            APLOG_WT("904", 0, null, "UPDATE",sqlca.sqlcode,
                    "TS利用可能ポイント情報", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*----------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_PreTotal処理", C_const_NG, 0, 0);
                        /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ポイント累計情報の更新 */
//        EXEC SQL UPDATE TSポイント累計情報
//        SET 累計付与期間限定Ｐ =  0,
//                累計利用期間限定Ｐ =  0,
//        最終更新日 = :h_batdate_ymd,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :h_program_id
//        WHERE 顧客番号 = 9999999999999;

        sqlca.sql = new StringDto("UPDATE TSポイント累計情報" +
                " SET 累計付与期間限定Ｐ =  0," +
                "    累計利用期間限定Ｐ =  0," +
                "    最終更新日 = ?," +
                "    最終更新日時 = SYSDATE()," +
                "    最終更新プログラムＩＤ = ?" +
                " WHERE 顧客番号 = 9999999999999");
        sqlca.restAndExecute(h_batdate_ymd, h_program_id);

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf( log_format_buf, "顧客番号 = 9999999999999");
            APLOG_WT("904", 0, null, "UPDATE",sqlca.sqlcode,
                    "TSポイント累計情報", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*----------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_PreTotal処理", C_const_NG, 0, 0);
                        /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 期間限定ポイント情報の更新 */
//        EXEC SQL UPDATE TS期間限定ポイント情報
//        SET 付与期間限定Ｐ０１ =  0,
//                付与期間限定Ｐ０２ =  0,
//                付与期間限定Ｐ０３ =  0,
//                付与期間限定Ｐ０４ =  0,
//                付与期間限定Ｐ０５ =  0,
//                付与期間限定Ｐ０６ =  0,
//                付与期間限定Ｐ０７ =  0,
//                付与期間限定Ｐ０８ =  0,
//                付与期間限定Ｐ０９ =  0,
//                付与期間限定Ｐ１０ =  0,
//                付与期間限定Ｐ１１ =  0,
//                付与期間限定Ｐ１２ =  0,
//                利用期間限定Ｐ０１ =  0,
//                利用期間限定Ｐ０２ =  0,
//                利用期間限定Ｐ０３ =  0,
//                利用期間限定Ｐ０４ =  0,
//                利用期間限定Ｐ０５ =  0,
//                利用期間限定Ｐ０６ =  0,
//                利用期間限定Ｐ０７ =  0,
//                利用期間限定Ｐ０８ =  0,
//                利用期間限定Ｐ０９ =  0,
//                利用期間限定Ｐ１０ =  0,
//                利用期間限定Ｐ１１ =  0,
//                利用期間限定Ｐ１２ =  0,
//        バッチ更新日 = :h_batdate_ymd,
//            最終更新日 = :h_batdate_ymd,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :h_program_id
//        WHERE 顧客番号 = 9999999999999;


        sqlca.sql = new StringDto("UPDATE TS期間限定ポイント情報" +
                "    SET 付与期間限定Ｐ０１ =  0," +
                "        付与期間限定Ｐ０２ =  0," +
                "        付与期間限定Ｐ０３ =  0," +
                "        付与期間限定Ｐ０４ =  0," +
                "        付与期間限定Ｐ０５ =  0," +
                "        付与期間限定Ｐ０６ =  0," +
                "        付与期間限定Ｐ０７ =  0," +
                "        付与期間限定Ｐ０８ =  0," +
                "        付与期間限定Ｐ０９ =  0," +
                "        付与期間限定Ｐ１０ =  0," +
                "        付与期間限定Ｐ１１ =  0," +
                "        付与期間限定Ｐ１２ =  0," +
                "        利用期間限定Ｐ０１ =  0," +
                "        利用期間限定Ｐ０２ =  0," +
                "        利用期間限定Ｐ０３ =  0," +
                "        利用期間限定Ｐ０４ =  0," +
                "        利用期間限定Ｐ０５ =  0," +
                "        利用期間限定Ｐ０６ =  0," +
                "        利用期間限定Ｐ０７ =  0," +
                "        利用期間限定Ｐ０８ =  0," +
                "        利用期間限定Ｐ０９ =  0," +
                "        利用期間限定Ｐ１０ =  0," +
                "        利用期間限定Ｐ１１ =  0," +
                "        利用期間限定Ｐ１２ =  0," +
                "    バッチ更新日 = ?," +
                "    最終更新日 = ?," +
                "    最終更新日時 = SYSDATE()," +
                "    最終更新プログラムＩＤ = ?" +
                "    WHERE 顧客番号 = 9999999999999");
        sqlca.restAndExecute(h_batdate_ymd, h_batdate_ymd, h_program_id);

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf( log_format_buf, "顧客番号 = 9999999999999");
            APLOG_WT("904", 0, null, "UPDATE",sqlca.sqlcode,
                    "TS期間限定ポイント情報", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*----------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_PreTotal処理", C_const_NG, 0, 0);
                        /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        if (DBG_LOG){
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTkzdkB_PreTotal処理", C_const_OK, 0, 0);
                /*----------------------------------------------------------*/
        }

        /* 処理を終了する */
        return (C_const_OK);

    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_CreKikangenteiPData                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTkzdkB_CreKikangenteiPData()                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      HSポイント日別情報から店舗別集計値を期間限定ポイント集計情報に        */
    /*      登録する。                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int  cmBTkzdkB_CreKikangenteiPData()
    {
        StringDto    wk_sql = new StringDto(C_const_SQLMaxLen * 24);     /* 動的SQLバッファ             */

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgStart("*** cmBTkzdkB_CreKikangenteiPData処理 ***");
                /*------------------------------------------------------------------------*/
        }

        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));

        /* AS期間限定ポイント集計情報テーブルをTRUNCATE */
//        EXEC SQL TRUNCATE TABLE AS期間限定ポイント集計情報;
        StringDto workSql = new StringDto();
        workSql.arr = "TRUNCATE TABLE AS期間限定ポイント集計情報";
        sqlca.sql = workSql;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK )  {
            APLOG_WT("904", 0, null, "TRUNCATE",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報", 0, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPData処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* AS期間限定ポイント集計情報に、HSポイント日別情報より店舗毎に */
        /* 集計したデータを登録                                         */
        StringBuilder builder=new StringBuilder();
        builder.append(" INSERT INTO AS期間限定ポイント集計情報");
        builder.append(" (");
        builder.append(" 集計対象月,");
        builder.append(" 旧販社コード,");
        builder.append(" 店番号,");
        builder.append(" 付与期間限定Ｐ０,");
        builder.append(" 買上Ｐ数Ｐ０,");
        builder.append(" ベビーサークルＰ数Ｐ０,");
        builder.append(" ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" カタリナ付与Ｐ数Ｐ０,");
        builder.append(" その他付与Ｐ数Ｐ０,");
        builder.append(" 調整Ｐ数Ｐ０,");
        builder.append(" 付与期間限定Ｐ１,");
        builder.append(" 買上Ｐ数Ｐ１,");
        builder.append(" ベビーサークルＰ数Ｐ１,");
        builder.append(" ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" カタリナ付与Ｐ数Ｐ１,");
        builder.append(" その他付与Ｐ数Ｐ１,");
        builder.append(" 調整Ｐ数Ｐ１,");
        builder.append(" 付与期間限定Ｐ２,");
        builder.append(" 買上Ｐ数Ｐ２,");
        builder.append(" ベビーサークルＰ数Ｐ２,");
        builder.append(" ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" カタリナ付与Ｐ数Ｐ２,");
        builder.append(" その他付与Ｐ数Ｐ２,");
        builder.append(" 調整Ｐ数Ｐ２,");
        builder.append(" 付与期間限定Ｐ３,");
        builder.append(" 買上Ｐ数Ｐ３,");
        builder.append(" ベビーサークルＰ数Ｐ３,");
        builder.append(" ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" カタリナ付与Ｐ数Ｐ３,");
        builder.append(" その他付与Ｐ数Ｐ３,");
        builder.append(" 調整Ｐ数Ｐ３,");
        builder.append(" 付与期間限定合計,");
        builder.append(" 利用期間限定Ｐ０,");
        builder.append(" 利用期間限定Ｐ１,");
        builder.append(" 利用期間限定Ｐ２,");
        builder.append(" 利用期間限定Ｐ３,");
        builder.append(" 利用期間限定合計,");
        builder.append(" クリアＰ数有効期限,");
        builder.append(" クリアＰ数顧客退会,");
        builder.append(" クリアＰ数合計,");
        builder.append(" 月末Ｐ残高Ｐ１,");
        builder.append(" 月末Ｐ残高Ｐ２,");
        builder.append(" 月末Ｐ残高Ｐ３,");
        builder.append(" 月末Ｐ残高合計,");
        builder.append(" 売上金額合計,");
        builder.append(" 利用期間限定Ｐ００");
        builder.append(" )");
        builder.append(" SELECT ?,");
        builder.append(" NVL(店情報.企業コード, 0) as 会社ＣＤ,");
        builder.append(" ポイント集計.店番号 as 店舗ＣＤ,");

        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ０) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ０) as 付与期間限定Ｐ０,");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ０) as 買上Ｐ数Ｐ０,");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ０) as ベビーサークルＰ数Ｐ０,");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ０) as ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ０) as アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ０) as カタリナ付与Ｐ数Ｐ０,");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ０) as その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ０) as 調整Ｐ数Ｐ０,");

        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ１) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ１) as 付与期間限定Ｐ１,");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ１) as 買上Ｐ数Ｐ１,");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ１) as ベビーサークルＰ数Ｐ１,");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ１) as ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ１) as アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ１) as カタリナ付与Ｐ数Ｐ１,");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ１) as その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ１) as 調整Ｐ数Ｐ１,");

        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ２) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ２) as 付与期間限定Ｐ２,");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ２) as 買上Ｐ数Ｐ２,");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ２) as ベビーサークルＰ数Ｐ２,");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ２) as ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ２) as アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ２) as カタリナ付与Ｐ数Ｐ２,");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ２) as その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ２) as 調整Ｐ数Ｐ２,");

        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ３) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ３) as 付与期間限定Ｐ３,");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ３) as 買上Ｐ数Ｐ３,");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ３) as ベビーサークルＰ数Ｐ３,");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ３) as ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ３) as アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ３) as カタリナ付与Ｐ数Ｐ３,");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ３) as その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ３) as 調整Ｐ数Ｐ３,");

        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ０) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ０) +");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ１) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ１) +");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ２) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ２) +");
        builder.append(" SUM(ポイント集計.買上Ｐ数Ｐ３) + ");
        builder.append(" SUM(ポイント集計.ベビーサークルＰ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.ワタシプラス連携付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.アプリＰ交換Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.カタリナ付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.その他付与Ｐ数Ｐ３) +");
        builder.append(" SUM(ポイント集計.調整Ｐ数Ｐ３) as 付与期間限定合計,");

        builder.append(" SUM(ポイント集計.利用期間限定Ｐ０) as 利用期間限定Ｐ０,");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ１) as 利用期間限定Ｐ１,");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ２) as 利用期間限定Ｐ２,");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ３) as 利用期間限定Ｐ３,");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ０) + ");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ１) +");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ２) +");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ３) +");
        builder.append(" SUM(ポイント集計.利用期間限定Ｐ００) as 利用期間限定合計,");

        builder.append(" SUM(ポイント集計.クリアＰ数有効期限) as クリアＰ数有効期限,");
        builder.append(" SUM(ポイント集計.クリアＰ数顧客退会) as クリアＰ数顧客退会,");
        builder.append(" SUM(ポイント集計.クリアＰ数有効期限) + ");
        builder.append(" SUM(ポイント集計.クリアＰ数顧客退会) as クリアＰ数合計,");

        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ１) as 月末Ｐ残高Ｐ１,");
        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ２) as 月末Ｐ残高Ｐ２,");
        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ３) as 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ１) + ");
        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ２) +");
        builder.append(" SUM(ポイント集計.月末Ｐ残高Ｐ３) as 月末Ｐ残高合計,");

        builder.append(" SUM(ポイント集計.売上金額) as 売上金額合計,");

        builder.append(" SUM(ポイント集計.利用期間限定Ｐ００) as 利用期間限定Ｐ００");
        builder.append(" FROM (");


        builder.append(" (SELECT 店番号,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 付与ポイント <> 0");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 付与ポイント <> 0");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 付与ポイント <> 0");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 付与ポイント <> 0");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 付与ポイント <> 0");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 75");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 75");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 72");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 72");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 76");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 79");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 79");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (10, 30)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" SUM(更新付与期間限定Ｐ%s) AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" SUM(ポイント対象金額) AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (10, 30)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ０,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ０,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ０,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ０,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ０,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 入会店舗,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" SUM(更新利用期間限定Ｐ)*(-1) AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 92");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 入会店舗)");

        builder.append(" UNION ALL (SELECT 入会店舗,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(更新利用期間限定Ｐ)*(-1) AS クリアＰ数顧客退会,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ１,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ２,");
        builder.append(" 0 AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 94");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 入会店舗)");

        builder.append(" UNION ALL (SELECT 入会店舗 AS 店番号,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" SUM(利用可能期間限定Ｐ%s) AS 月末Ｐ残高Ｐ１,");
        builder.append(" SUM(利用可能期間限定Ｐ%s) AS 月末Ｐ残高Ｐ２,");
        builder.append(" SUM(利用可能期間限定Ｐ%s) AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.TS利用可能ポイント情報");
        builder.append(" WHERE 顧客番号 <> 9999999999999");
        builder.append(" GROUP BY 入会店舗)");

        builder.append(" UNION ALL (SELECT 入会店舗,");
        builder.append(" 0 AS 買上Ｐ数Ｐ０,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ０,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ０,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ０,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ０,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ０,");
        builder.append(" 0 AS 調整Ｐ数Ｐ０,");
        builder.append(" 0 AS 買上Ｐ数Ｐ１,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ１,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ１,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ１,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ１,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ１,");
        builder.append(" 0 AS 調整Ｐ数Ｐ１,");
        builder.append(" 0 AS 買上Ｐ数Ｐ２,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ２,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ２,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ２,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ２,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ２,");
        builder.append(" 0 AS 調整Ｐ数Ｐ２,");
        builder.append(" 0 AS 買上Ｐ数Ｐ３,");
        builder.append(" 0 AS ベビーサークルＰ数Ｐ３,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数Ｐ３,");
        builder.append(" 0 AS アプリＰ交換Ｐ数Ｐ３,");
        builder.append(" 0 AS カタリナ付与Ｐ数Ｐ３,");
        builder.append(" 0 AS その他付与Ｐ数Ｐ３,");
        builder.append(" 0 AS 調整Ｐ数Ｐ３,");
        builder.append(" 0 AS 利用期間限定Ｐ０,");
        builder.append(" 0 AS 利用期間限定Ｐ１,");
        builder.append(" 0 AS 利用期間限定Ｐ２,");
        builder.append(" 0 AS 利用期間限定Ｐ３,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS クリアＰ数顧客退会,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 月末Ｐ残高Ｐ１,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 月末Ｐ残高Ｐ２,");
        builder.append(" SUM(更新利用期間限定Ｐ%s)*(-1) AS 月末Ｐ残高Ｐ３,");
        builder.append(" 0 AS 売上金額,");
        builder.append(" 0 AS 利用期間限定Ｐ００");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 94");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" GROUP BY 入会店舗)");
        builder.append(" ) ポイント集計 LEFT JOIN");
        builder.append(" (SELECT 店番号");
        builder.append(" ,企業コード");
        builder.append(" FROM (SELECT /*+ INDEX(cmuser.PS店表示情報 PKPSSYSDSI00) */店番号");
        builder.append(" ,DECODE(店番号, '999001', 1000, '999002', 1000, 企業コード) AS 企業コード");
        builder.append(" ,ROW_NUMBER() OVER (PARTITION BY 店番号");
        builder.append(" ORDER BY 終了年月日 DESC) G_row");
        builder.append(" FROM cmuser.PS店表示情報");
        builder.append(" WHERE 開始年月日 <= ?) PSST");
        builder.append(" WHERE G_row = 1) 店情報");
        builder.append(" ON ポイント集計.店番号 = 店情報.店番号");
        builder.append(" GROUP BY 店情報.企業コード, ポイント集計.店番号");
        builder.append(" ORDER BY 店情報.企業コード, ポイント集計.店番号");
        sprintf(wk_sql,
                builder.toString()
                /* １－１．POS買上  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* １－２．POS買上  月跨ぎでの処理分 （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_pym
                /* １－３．POS買上  処理日当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* １－４．POS買上  前月集計漏れ分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* １－５．POS買上  前月集計漏れの月またぎ分  （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_pym
                /* ２－１．ベビーサークルＰ  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ２－２．ベビーサークルＰ  処理日当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ２－３．ベビーサークルＰ  前月集計漏れ分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ３－１．ワタシプラス連携付与Ｐ数  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ３－２．ワタシプラス連携付与Ｐ数  処理日当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ４－１．アプリＰ交換Ｐ数  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ４－２．アプリＰ交換Ｐ数  処理日当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ５－１．カタリナ付与Ｐ数（ＰＣ／バッチ）  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ６－１．その他付与Ｐ数（ＰＣ／バッチ）   当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ６－２．その他付与Ｐ数（ＰＣ／バッチ） 処理日当月処理分（翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ７－１．調整Ｐ（ＰＯＳ）  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ７－２．調整Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_pym
                /* ７－３．調整Ｐ（ＰＯＳ）  処理日当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ７－４．調整Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ７－５．調整Ｐ（ＰＯＳ）  前月集計漏れの月またぎ分  （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_pym
                /* ８－１．調整Ｐ（ＰＣ）  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
                /* ８－２．調整Ｐ（ＰＣ）  処理部当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , h_batdate_nym
                /* ９－１．発行Ｐ（ＰＯＳ）  当月分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , g_MMZenkaku[4].c_MMZen, h_batdate_ym
                /* ９－２．発行Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , g_MMZenkaku[4].c_MMZen, h_batdate_pym
                /* ９－３．発行Ｐ（ＰＯＳ）  処理部当月処理分 （翌月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , g_MMZenkaku[4].c_MMZen, h_batdate_nym
                /* ９－４．発行Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , g_MMZenkaku[4].c_MMZen, h_batdate_ym
                /* ９－５．発行Ｐ（ＰＯＳ  前月集計漏れの月またぎ分 （前月）*/
                , g_MMZenkaku[0].c_MMZen, g_MMZenkaku[1].c_MMZen
                , g_MMZenkaku[2].c_MMZen, g_MMZenkaku[3].c_MMZen
                , g_MMZenkaku[4].c_MMZen, h_batdate_pym
                /* １０．クリアＰ数有効期限 （翌月）*/
                , h_batdate_nym
                /* １１．クリアＰ数顧客退会 （当月）*/
                , h_batdate_ym
                /* １２－１．月末Ｐ */
                , g_MMZenkaku[1].c_MMZen, g_MMZenkaku[2].c_MMZen
                , g_MMZenkaku[3].c_MMZen
                /* １２－２．月末Ｐ 顧客退会除外 （当月）*/
                , g_MMZenkaku[1].c_MMZen, g_MMZenkaku[2].c_MMZen
                , g_MMZenkaku[3].c_MMZen
                , h_batdate_ym
        );

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPData *** (期間限定Ｐ集計表情報作成)動的ＳＱＬ=[%s]\n", wk_sql);
                /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_ins from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPData *** AS期間限定ポイント集計情報 INSERT PREPARE sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "PREPARE",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPData処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* INSERT文を実行する */
//        EXEC SQL EXECUTE sql_ins USING
//    :h_batdate_ym,
//        /* １－１．POS買上  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １－２．POS買上  月跨ぎでの処理分 （前月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １－３．POS買上  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １－４．POS買上  前月集計漏れ分 （当月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* １－５．POS買上  前月集計漏れの月またぎ分  （前月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* ２－１．ベビーサークルＰ  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ２－２．ベビーサークルＰ  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ２－３．ベビーサークルＰ  前月集計漏れ分 （当月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* ３－１．ワタシプラス連携付与Ｐ数  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ３－２．ワタシプラス連携付与Ｐ数  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ４－１．アプリＰ交換Ｐ数  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ４－２．アプリＰ交換Ｐ数  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ５－１．カタリナ付与Ｐ数（ＰＣ／バッチ）  当月分 （当月）*/
//        /* バインド変数なし */
//        /* ６－１．その他付与Ｐ数（ＰＣ／バッチ）   当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ６－２．その他付与Ｐ数（ＰＣ／バッチ）  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ７－１．調整Ｐ（ＰＯＳ）  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ７－２．調整Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ７－３．調整Ｐ（ＰＯＳ）  処理日当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ７－４．調整Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* ７－５．調整Ｐ（ＰＯＳ）  前月集計漏れの月またぎ分  （前月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* ８－１．調整Ｐ（ＰＣ）  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ８－２．調整Ｐ（ＰＣ）  処理部当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ９－１．発行Ｐ（ＰＯＳ）  当月分 （当月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ９－２．発行Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ９－３．発行Ｐ（ＰＯＳ）  処理部当月処理分 （翌月）*/
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ９－４．発行Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* ９－５．発行Ｐ（ＰＯＳ  前月集計漏れの月またぎ分 （前月）*/
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date,
//         :h_batdate_pym_end_date,
//        /* １０．クリアＰ数有効期限 （翌月）*/
//         :h_batdate_ym_start_date, :h_batdate_ymd,
//        /* １１．クリアＰ数顧客退会 （当月）*/
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １２－１．月末Ｐ */
//        /* バインド変数なし */
//        /* １２－２．月末Ｐ 顧客退会除外 （当月）*/
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* 店情報 */
//         :h_batdate_ym_end_date;

         sqlca.restAndExecute(h_batdate_ym,
                 /* １－１．POS買上  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* １－２．POS買上  月跨ぎでの処理分 （前月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* １－３．POS買上  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* １－４．POS買上  前月集計漏れ分 （当月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* １－５．POS買上  前月集計漏れの月またぎ分  （前月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* ２－１．ベビーサークルＰ  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ２－２．ベビーサークルＰ  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ２－３．ベビーサークルＰ  前月集計漏れ分 （当月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* ３－１．ワタシプラス連携付与Ｐ数  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ３－２．ワタシプラス連携付与Ｐ数  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ４－１．アプリＰ交換Ｐ数  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ４－２．アプリＰ交換Ｐ数  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ５－１．カタリナ付与Ｐ数（ＰＣ／バッチ）  当月分 （当月）*/
                 /* バインド変数なし */
                 /* ６－１．その他付与Ｐ数（ＰＣ／バッチ）   当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ６－２．その他付与Ｐ数（ＰＣ／バッチ）  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ７－１．調整Ｐ（ＰＯＳ）  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ７－２．調整Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ７－３．調整Ｐ（ＰＯＳ）  処理日当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ７－４．調整Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* ７－５．調整Ｐ（ＰＯＳ）  前月集計漏れの月またぎ分  （前月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* ８－１．調整Ｐ（ＰＣ）  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ８－２．調整Ｐ（ＰＣ）  処理部当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ９－１．発行Ｐ（ＰＯＳ）  当月分 （当月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ９－２．発行Ｐ（ＰＯＳ）  月跨ぎでの処理分 （前月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ９－３．発行Ｐ（ＰＯＳ）  処理部当月処理分 （翌月）*/
                 h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* ９－４．発行Ｐ（ＰＯＳ）  前月集計漏れ分 （当月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* ９－５．発行Ｐ（ＰＯＳ  前月集計漏れの月またぎ分 （前月）*/
                 h_batdate_ymd_prev, h_batdate_pym_start_date,
                 h_batdate_pym_end_date,
                 /* １０．クリアＰ数有効期限 （翌月）*/
                 h_batdate_ym_start_date, h_batdate_ymd,
                 /* １１．クリアＰ数顧客退会 （当月）*/
                 h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* １２－１．月末Ｐ */
                 /* バインド変数なし */
                 /* １２－２．月末Ｐ 顧客退会除外 （当月）*/
                 h_batdate_ym_start_date, h_batdate_ym_end_date,
                 /* 店情報 */
                 h_batdate_ym_end_date);

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPData *** AS期間限定Ｐ集計情報 INSERT sqlcode =[%d]\n", sqlca.sqlcode);
                C_DbgMsg( "*** Bind h_batdate_ym[%d]\n", h_batdate_ym);
                C_DbgMsg( "*** Bind 当月:h_batdate_ymd[%d]\n", h_batdate_ymd);
                C_DbgMsg( "*** Bind 当月:h_batdate_ym_start_date[%d]\n", h_batdate_ym_start_date);
                C_DbgMsg( "*** Bind 当月:h_batdate_ym_end_date[%d]\n", h_batdate_ym_end_date);
                C_DbgMsg( "*** Bind 前月:h_batdate_ymd_prev[%d]\n", h_batdate_ymd_prev);
                C_DbgMsg( "*** Bind 前月:h_batdate_pym_start_date[%d]\n", h_batdate_pym_start_date);
                C_DbgMsg( "*** Bind 前月:h_batdate_pym_end_date[%d]\n", h_batdate_pym_end_date);
                /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf( log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "INSERT",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*----------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPData処理", C_const_NG, 0, 0);
                        /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_CreKikangenteiPFile                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTkzdkB_CreKikangenteiPFile()                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      店舗毎に集計したデータを明細に、販社、全社の集計をフッターに作成する。*/
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int  cmBTkzdkB_CreKikangenteiPFile()
    {
        int     rtn_cd;                           /* 関数戻り値                   */
        StringDto    fp_name= new StringDto(C_const_SQLMaxLen);       /* 出力ファイルパス名           */
        StringDto    wk_sql = new StringDto(C_const_SQLMaxLen * 24);   /* 動的SQLバッファ              */
        StringDto    wk_buf1= new StringDto(C_const_SQLMaxLen);       /* 編集バッファ                 */
        StringDto    wk_buf2= new StringDto(C_const_SQLMaxLen);       /* 編集バッファ                 */
        int     buf_len;                          /* 編集用                       */
        IntegerDto     sjis_len;                         /* SJIS変換用                   */
        StringDto    utf8_buf= new StringDto(C_const_SQLMaxLen);      /* UTF8文字列格納領域           */
        StringDto    sjis_buf= new StringDto(C_const_SQLMaxLen);      /* SJIS文字列格納領域           */


        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        memset(wk_buf2, 0x00, sizeof(wk_buf2));

        /********************/
        /* ファイルオープン */
        /********************/
        sprintf(fp_name, "%s/%s", out_file_dir, kp_fl_name_sjis);
//        if ((fp_kp = fopen(fp_name, "w" )) == null) {
        if ((fp_kp = fopen(fp_name.arr, SystemConstant.Shift_JIS, FileOpenType.w)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPFile *** ファイルオープンERR%s\n","");
                        /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /********************/
        /* 作成日の出力     */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s%s\r\n", C_P_DATE, h_system_date);
        rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                    ,rtn_cd ,0 ,0 ,0 ,0);
            /* 処理を終了する */
            return C_const_NG ;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_kp);
        rtn_cd = ferror(fp_kp);
        if( rtn_cd != C_const_OK ) {
            /* ファイル書き込みエラー */
            sprintf( log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG){
                        /*------------------------------------------------------------------*/
                        C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                        /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /********************/
        /* 作成時刻の出力   */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s%s\r\n", C_P_TIME, h_system_time);
        rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                    ,rtn_cd ,0 ,0 ,0 ,0);
            /* 処理を終了する */
            return C_const_NG ;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_kp);
        rtn_cd = ferror(fp_kp);
        if( rtn_cd != C_const_OK ){
            /* ファイル書き込みエラー */
            sprintf( log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG){
                        /*------------------------------------------------------------------*/
                        C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                        /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /********************/
        /* ヘダー項目の出力 */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf,
                 "\"企業ＣＤ\",\"企業名\",\"店舗ＣＤ\",\"店舗名\","
                + "\"付与期間限定%d月\",\"買上Ｐ数%d月\",\"ベビーサークルＰ数%d月\","
                + "\"ワタシプラス連携付与Ｐ数%d月\",\"アプリＰ交換Ｐ数%d月\","
                + "\"カタリナ付与Ｐ数%d月\",\"その他付与Ｐ数%d月\",\"調整Ｐ数%d月\","
                + "\"付与期間限定%d月\",\"買上Ｐ数%d月\",\"ベビーサークルＰ数%d月\","
                + "\"ワタシプラス連携付与Ｐ数%d月\",\"アプリＰ交換Ｐ数%d月\","
                + "\"カタリナ付与Ｐ数%d月\",\"その他付与Ｐ数%d月\",\"調整Ｐ数%d月\","
                + "\"付与期間限定%d月\",\"買上Ｐ数%d月\",\"ベビーサークルＰ数%d月\","
                + "\"ワタシプラス連携付与Ｐ数%d月\",\"アプリＰ交換Ｐ数%d月\","
                + "\"カタリナ付与Ｐ数%d月\",\"その他付与Ｐ数%d月\",\"調整Ｐ数%d月\","
                + "\"付与期間限定%d月\",\"買上Ｐ数%d月\",\"ベビーサークルＰ数%d月\","
                + "\"ワタシプラス連携付与Ｐ数%d月\",\"アプリＰ交換Ｐ数%d月\","
                + "\"カタリナ付与Ｐ数%d月\",\"その他付与Ｐ数%d月\",\"調整Ｐ数%d月\","
                + "\"付与期間限定合計\",\"売上金額合計\",\"利用期間限定%d月\",\"利用期間限定%d月\"," /* 売上金額合計追加 */
                + "\"利用期間限定%d月\",\"利用期間限定%d月\",\"利用期間限定%d月\",\"利用期間限定合計\","
                + "\"クリアＰ数有効期限\",\"クリアＰ数顧客退会\",\"クリアＰ数合計\","
                + "\"月末Ｐ残高%d月\",\"月末Ｐ残高%d月\",\"月末Ｐ残高%d月\","
                + "\"月末Ｐ残高合計\"\r\n",
                /* 付与期間限定 対象月 */
                g_MMZenkaku[0].i_MM, g_MMZenkaku[0].i_MM, g_MMZenkaku[0].i_MM,
                g_MMZenkaku[0].i_MM, g_MMZenkaku[0].i_MM, g_MMZenkaku[0].i_MM,
                g_MMZenkaku[0].i_MM, g_MMZenkaku[0].i_MM,
                /* 付与期間限定 対象月+1ヶ月 */
                g_MMZenkaku[1].i_MM, g_MMZenkaku[1].i_MM, g_MMZenkaku[1].i_MM,
                g_MMZenkaku[1].i_MM, g_MMZenkaku[1].i_MM, g_MMZenkaku[1].i_MM,
                g_MMZenkaku[1].i_MM, g_MMZenkaku[1].i_MM,
                /* 付与期間限定 対象月+2ヶ月 */
                g_MMZenkaku[2].i_MM, g_MMZenkaku[2].i_MM, g_MMZenkaku[2].i_MM,
                g_MMZenkaku[2].i_MM, g_MMZenkaku[2].i_MM, g_MMZenkaku[2].i_MM,
                g_MMZenkaku[2].i_MM, g_MMZenkaku[2].i_MM,
                /* 付与期間限定 対象月+3ヶ月 */
                g_MMZenkaku[3].i_MM, g_MMZenkaku[3].i_MM, g_MMZenkaku[3].i_MM,
                g_MMZenkaku[3].i_MM, g_MMZenkaku[3].i_MM, g_MMZenkaku[3].i_MM,
                g_MMZenkaku[3].i_MM, g_MMZenkaku[3].i_MM,
                /* 利用期間限定 */
                g_MMZenkaku[4].i_MM, g_MMZenkaku[0].i_MM, g_MMZenkaku[1].i_MM,
                g_MMZenkaku[2].i_MM, g_MMZenkaku[3].i_MM,
                /* クリア */
                g_MMZenkaku[1].i_MM, g_MMZenkaku[2].i_MM, g_MMZenkaku[3].i_MM);

        rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                    ,rtn_cd ,0 ,0 ,0 ,0);
            /* 処理を終了する */
            return C_const_NG ;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_kp);
        rtn_cd = ferror(fp_kp);
        if( rtn_cd != C_const_OK ){
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG){
                        /*------------------------------------------------------------------*/
                        C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                        /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_kp_cnt ++;

        /**************************************/
        /* AS期間限定Ｐ集計情報（店別）の取得 */
        /**************************************/
        sprintf(wk_sql,
                "SELECT"
                + " 集計情報.集計対象月,"
                + " DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード) AS 旧販社コード,"
                + " 集計情報.店番号,"
                + " 集計情報.付与期間限定Ｐ０,"
                + " 集計情報.買上Ｐ数Ｐ０,"
                + " 集計情報.ベビーサークルＰ数Ｐ０,"
                + " 集計情報.ワタシプラス連携付与Ｐ数Ｐ０,"
                + " 集計情報.アプリＰ交換Ｐ数Ｐ０,"
                + " 集計情報.カタリナ付与Ｐ数Ｐ０,"
                + " 集計情報.その他付与Ｐ数Ｐ０,"
                + " 集計情報.調整Ｐ数Ｐ０,"
                + " 集計情報.付与期間限定Ｐ１,"
                + " 集計情報.買上Ｐ数Ｐ１,"
                + " 集計情報.ベビーサークルＰ数Ｐ１,"
                + " 集計情報.ワタシプラス連携付与Ｐ数Ｐ１,"
                + " 集計情報.アプリＰ交換Ｐ数Ｐ１,"
                + " 集計情報.カタリナ付与Ｐ数Ｐ１,"
                + " 集計情報.その他付与Ｐ数Ｐ１,"
                + " 集計情報.調整Ｐ数Ｐ１,"
                + " 集計情報.付与期間限定Ｐ２,"
                + " 集計情報.買上Ｐ数Ｐ２,"
                + " 集計情報.ベビーサークルＰ数Ｐ２,"
                + " 集計情報.ワタシプラス連携付与Ｐ数Ｐ２,"
                + " 集計情報.アプリＰ交換Ｐ数Ｐ２,"
                + " 集計情報.カタリナ付与Ｐ数Ｐ２,"
                + " 集計情報.その他付与Ｐ数Ｐ２,"
                + " 集計情報.調整Ｐ数Ｐ２,"
                + " 集計情報.付与期間限定Ｐ３,"
                + " 集計情報.買上Ｐ数Ｐ３,"
                + " 集計情報.ベビーサークルＰ数Ｐ３,"
                + " 集計情報.ワタシプラス連携付与Ｐ数Ｐ３,"
                + " 集計情報.アプリＰ交換Ｐ数Ｐ３,"
                + " 集計情報.カタリナ付与Ｐ数Ｐ３,"
                + " 集計情報.その他付与Ｐ数Ｐ３,"
                + " 集計情報.調整Ｐ数Ｐ３,"
                + " 集計情報.付与期間限定合計,"
                + " 集計情報.売上金額合計," /* 売上金額合計追加 */
                + " 集計情報.利用期間限定Ｐ００," /* 期間限定利用Ｐ前月追加 */
                + " 集計情報.利用期間限定Ｐ０,"
                + " 集計情報.利用期間限定Ｐ１,"
                + " 集計情報.利用期間限定Ｐ２,"
                + " 集計情報.利用期間限定Ｐ３,"
                + " 集計情報.利用期間限定合計,"
                + " 集計情報.クリアＰ数有効期限,"
                + " 集計情報.クリアＰ数顧客退会,"
                + " 集計情報.クリアＰ数合計,"
                + " 集計情報.月末Ｐ残高Ｐ１,"
                + " 集計情報.月末Ｐ残高Ｐ２,"
                + " 集計情報.月末Ｐ残高Ｐ３,"
                + " 集計情報.月末Ｐ残高合計,"
                + " CASE DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' WHEN 3000 THEN 'MK' ELSE '%s' END AS 企業名,"
                + " NVL(RPAD(PS店表示情報.漢字店舗名称,LENGTH(PS店表示情報.漢字店舗名称)), '%s')"
                + " FROM"
                + " AS期間限定ポイント集計情報 集計情報 LEFT JOIN "
                + "(SELECT"
                + " 店番号,"
                + " 漢字店舗名称"
                + " FROM"
                + " (SELECT "
                + " 店番号, "
                + " 漢字店舗名称, "
                + " ROW_NUMBER() OVER ( "
                + " PARTITION BY 店番号 "
                + " ORDER BY 終了年月日 DESC "
                + " ) G_row "
                + " FROM "
                + " PS店表示情報 "
                + " WHERE "
                + " 開始年月日 <= ? "
                + " ) PSST "
                + " WHERE G_row = 1 "
                + " ) PS店表示情報"
                + " ON  集計情報.店番号    = PS店表示情報.店番号"
                + " WHERE"
                + " 集計情報.集計対象月  = ?"
                + " ORDER BY"
                + " 集計情報.集計対象月,"
                + " DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード),"
                + " 集計情報.店番号"
                , C_NULLSTR
                , C_NULLSTR
        );

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPFile *** (期間限定ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
                /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql );

        /* 動的ＳＱＬ文の解析 */
        sqlca.sql = str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_sel1 from :str_sql;

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(店別) PREPARE sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(店別集計)", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASKGP_MISE CURSOR FOR sql_sel1;
        sqlca.declare();

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(店別) DECLARE CURSOR sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(店別集計)", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_ASKGP_MISE USING
//    :h_batdate_ym_end_date,
//        :h_batdate_ym;
        sqlca.open(h_batdate_ym_end_date, h_batdate_ym);

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(店別) CURSOR OPEN sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(店別集計)", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for ( ; ; ) {
            memset(as_kgp_t, 0x00, sizeof(as_kgp_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

            sqlca.fetch();
            sqlca.recData(as_kgp_t.shukei_ymd,
                    as_kgp_t.kyu_hansya_cd,
                    as_kgp_t.mise_no,
                    as_kgp_t.fuyo_gentei_point0,
                    as_kgp_t.kaiage_point0,
                    as_kgp_t.babycircle_point0,
                    as_kgp_t.watashiplus_point0,
                    as_kgp_t.app_koukan_point0,
                    as_kgp_t.catalina_point0,
                    as_kgp_t.sonota_point0,
                    as_kgp_t.chosei_point0,
                    as_kgp_t.fuyo_gentei_point1,
                    as_kgp_t.kaiage_point1,
                    as_kgp_t.babycircle_point1,
                    as_kgp_t.watashiplus_point1,
                    as_kgp_t.app_koukan_point1,
                    as_kgp_t.catalina_point1,
                    as_kgp_t.sonota_point1,
                    as_kgp_t.chosei_point1,
                    as_kgp_t.fuyo_gentei_point2,
                    as_kgp_t.kaiage_point2,
                    as_kgp_t.babycircle_point2,
                    as_kgp_t.watashiplus_point2,
                    as_kgp_t.app_koukan_point2,
                    as_kgp_t.catalina_point2,
                    as_kgp_t.sonota_point2,
                    as_kgp_t.chosei_point2,
                    as_kgp_t.fuyo_gentei_point3,
                    as_kgp_t.kaiage_point3,
                    as_kgp_t.babycircle_point3,
                    as_kgp_t.watashiplus_point3,
                    as_kgp_t.app_koukan_point3,
                    as_kgp_t.catalina_point3,
                    as_kgp_t.sonota_point3,
                    as_kgp_t.chosei_point3,
                    as_kgp_t.fuyo_sum,
                    as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                    as_kgp_t.riyou_sum00, /* 期間限定利用Ｐ前月追加 */
                    as_kgp_t.riyou_sum0,
                    as_kgp_t.riyou_sum1,
                    as_kgp_t.riyou_sum2,
                    as_kgp_t.riyou_sum3,
                    as_kgp_t.riyou_sum,
                    as_kgp_t.clear_pointkigen,
                    as_kgp_t.clear_pointtaikai,
                    as_kgp_t.clear_sum,
                    as_kgp_t.end_pointp1,
                    as_kgp_t.end_pointp2,
                    as_kgp_t.end_pointp3,
                    as_kgp_t.end_point,
                    h_kyu_hansya_name,
                    h_mise_kanji);
//            EXEC SQL FETCH CUR_ASKGP_MISE
//            INTO :as_kgp_t.shukei_ymd,
//                          :as_kgp_t.kyu_hansya_cd,
//                          :as_kgp_t.mise_no,
//                          :as_kgp_t.fuyo_gentei_point0,
//                          :as_kgp_t.kaiage_point0,
//                          :as_kgp_t.babycircle_point0,
//                          :as_kgp_t.watashiplus_point0,
//                          :as_kgp_t.app_koukan_point0,
//                          :as_kgp_t.catalina_point0,
//                          :as_kgp_t.sonota_point0,
//                          :as_kgp_t.chosei_point0,
//                          :as_kgp_t.fuyo_gentei_point1,
//                          :as_kgp_t.kaiage_point1,
//                          :as_kgp_t.babycircle_point1,
//                          :as_kgp_t.watashiplus_point1,
//                          :as_kgp_t.app_koukan_point1,
//                          :as_kgp_t.catalina_point1,
//                          :as_kgp_t.sonota_point1,
//                          :as_kgp_t.chosei_point1,
//                          :as_kgp_t.fuyo_gentei_point2,
//                          :as_kgp_t.kaiage_point2,
//                          :as_kgp_t.babycircle_point2,
//                          :as_kgp_t.watashiplus_point2,
//                          :as_kgp_t.app_koukan_point2,
//                          :as_kgp_t.catalina_point2,
//                          :as_kgp_t.sonota_point2,
//                          :as_kgp_t.chosei_point2,
//                          :as_kgp_t.fuyo_gentei_point3,
//                          :as_kgp_t.kaiage_point3,
//                          :as_kgp_t.babycircle_point3,
//                          :as_kgp_t.watashiplus_point3,
//                          :as_kgp_t.app_koukan_point3,
//                          :as_kgp_t.catalina_point3,
//                          :as_kgp_t.sonota_point3,
//                          :as_kgp_t.chosei_point3,
//                          :as_kgp_t.fuyo_sum,
//                          :as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
//                          :as_kgp_t.riyou_sum00, /* 期間限定利用Ｐ前月追加 */
//                          :as_kgp_t.riyou_sum0,
//                          :as_kgp_t.riyou_sum1,
//                          :as_kgp_t.riyou_sum2,
//                          :as_kgp_t.riyou_sum3,
//                          :as_kgp_t.riyou_sum,
//                          :as_kgp_t.clear_pointkigen,
//                          :as_kgp_t.clear_pointtaikai,
//                          :as_kgp_t.clear_sum,
//                          :as_kgp_t.end_pointp1,
//                          :as_kgp_t.end_pointp2,
//                          :as_kgp_t.end_pointp3,
//                          :as_kgp_t.end_point,
//                          :h_kyu_hansya_name,
//                          :h_mise_kanji;

            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(店別) FETCH sqlcode =[%d]\n", sqlca.sqlcode);
                        /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if ( sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                sprintf( log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH",sqlca.sqlcode,
                        "AS期間限定ポイント集計情報(店別集計)", log_format_buf,
                        0, 0);

//                EXEC SQL CLOSE CUR_ASKGP_MISE; /* カーソルクローズ               */
                sqlca.curse_close();
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                                /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if ( sqlca.sqlcode == C_const_Ora_NOTFOUND ) {
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(店別) FETCH NOTFOUND=[%d]\n", sqlca.sqlcode);
                                /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント集計表に出力           */
            /*--------------------------------*/
            /*------------*/
            /* 企業コード */
            /*------------*/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf( wk_buf1, "\"%d\",\"", as_kgp_t.kyu_hansya_cd );
            buf_len = strlen(wk_buf1);

            /*------------*/
            /* 企業名     */
            /*------------*/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim( h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if(strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }

            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
            rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        ,rtn_cd ,0 ,0 ,0 ,0);
                /* 処理を終了する */
                return C_const_NG ;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /*------------*/
            /* 店舗コード */
            /*------------*/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf( wk_buf2, "\",\"%d\",\"", as_kgp_t.mise_no );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*------------*/
            /* 店舗名     */
            /*------------*/
            /*  店舗名の末尾のスペース削除 */
            BT_Rtrim( h_mise_kanji.strDto(), strlen(h_mise_kanji));

            /* フェッチ用に置換した空文字列を戻す */
            if(strcmp(h_mise_kanji.strVal(), C_NULLSTR) == 0) {
                strcpy(h_mise_kanji, "");
            }

            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_mise_kanji.strDto());
            rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        ,rtn_cd ,0 ,0 ,0 ,0);
                /* 処理を終了する */
                return C_const_NG ;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /*------------*/
            /* P数        */
            /*------------*/
            sprintf( wk_buf2,
                     "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n", /* 売上金額合計追加、期間限定利用Ｐ前月 */
                    /* 付与期間限定Ｐ０ */
                    as_kgp_t.fuyo_gentei_point0,  as_kgp_t.kaiage_point0,
                    as_kgp_t.babycircle_point0,  as_kgp_t.watashiplus_point0,
                    as_kgp_t.app_koukan_point0,  as_kgp_t.catalina_point0,
                    as_kgp_t.sonota_point0,  as_kgp_t.chosei_point0,
                    /* 付与期間限定Ｐ１ */
                    as_kgp_t.fuyo_gentei_point1,  as_kgp_t.kaiage_point1,
                    as_kgp_t.babycircle_point1,  as_kgp_t.watashiplus_point1,
                    as_kgp_t.app_koukan_point1,  as_kgp_t.catalina_point1,
                    as_kgp_t.sonota_point1,  as_kgp_t.chosei_point1,
                    /* 付与期間限定Ｐ２ */
                    as_kgp_t.fuyo_gentei_point2,  as_kgp_t.kaiage_point2,
                    as_kgp_t.babycircle_point2,  as_kgp_t.watashiplus_point2,
                    as_kgp_t.app_koukan_point2,  as_kgp_t.catalina_point2,
                    as_kgp_t.sonota_point2,  as_kgp_t.chosei_point2,
                    /* 付与期間限定Ｐ３ */
                    as_kgp_t.fuyo_gentei_point3,  as_kgp_t.kaiage_point3,
                    as_kgp_t.babycircle_point3,  as_kgp_t.watashiplus_point3,
                    as_kgp_t.app_koukan_point3,  as_kgp_t.catalina_point3,
                    as_kgp_t.sonota_point3,  as_kgp_t.chosei_point3,
                    /* 付与期間限定Ｐ合計 */
                    as_kgp_t.fuyo_sum,
                    /* 売上金額合計 */
                    as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                    /* 利用期間限定Ｐ */
                    as_kgp_t.riyou_sum00, as_kgp_t.riyou_sum0,  as_kgp_t.riyou_sum1, /* 期間限定利用Ｐ前月 */
                    as_kgp_t.riyou_sum2, as_kgp_t.riyou_sum3,  as_kgp_t.riyou_sum,
                    /* クリアＰ */
                    as_kgp_t.clear_pointkigen,  as_kgp_t.clear_pointtaikai,
                    as_kgp_t.clear_sum,
                    /* 月末Ｐ残高 */
                    as_kgp_t.end_pointp1,  as_kgp_t.end_pointp2,
                    as_kgp_t.end_pointp3,  as_kgp_t.end_point
            );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /* ファイル書き込み */
            fwrite(wk_buf1, buf_len, 1, fp_kp);
            rtn_cd = ferror(fp_kp);
            if( rtn_cd != C_const_OK ){
                /* ファイル書き込みエラー */
                sprintf( log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

//                EXEC SQL CLOSE CUR_ASKGP_MISE; /* カーソルクローズ           */
                sqlca.curse_close();
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                                /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_kp_cnt ++;

        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASKGP_MISE;
        sqlca.curse_close();

        /****************************************/
        /* AS期間限定Ｐ集計情報（企業別）の取得 */
        /****************************************/
        sprintf(wk_sql,
                "SELECT"
                + " 集計情報.集計対象月,"
                + " DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード) AS 旧販社コード,"
                + " SUM(集計情報.付与期間限定Ｐ０),"
                + " SUM(集計情報.買上Ｐ数Ｐ０),"
                + " SUM(集計情報.ベビーサークルＰ数Ｐ０),"
                + " SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ０),"
                + " SUM(集計情報.アプリＰ交換Ｐ数Ｐ０),"
                + " SUM(集計情報.カタリナ付与Ｐ数Ｐ０),"
                + " SUM(集計情報.その他付与Ｐ数Ｐ０),"
                + " SUM(集計情報.調整Ｐ数Ｐ０),"
                + " SUM(集計情報.付与期間限定Ｐ１),"
                + " SUM(集計情報.買上Ｐ数Ｐ１),"
                + " SUM(集計情報.ベビーサークルＰ数Ｐ１),"
                + " SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ１),"
                + " SUM(集計情報.アプリＰ交換Ｐ数Ｐ１),"
                + " SUM(集計情報.カタリナ付与Ｐ数Ｐ１),"
                + " SUM(集計情報.その他付与Ｐ数Ｐ１),"
                + " SUM(集計情報.調整Ｐ数Ｐ１),"
                + " SUM(集計情報.付与期間限定Ｐ２),"
                + " SUM(集計情報.買上Ｐ数Ｐ２),"
                + " SUM(集計情報.ベビーサークルＰ数Ｐ２),"
                + " SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ２),"
                + " SUM(集計情報.アプリＰ交換Ｐ数Ｐ２),"
                + " SUM(集計情報.カタリナ付与Ｐ数Ｐ２),"
                + " SUM(集計情報.その他付与Ｐ数Ｐ２),"
                + " SUM(集計情報.調整Ｐ数Ｐ２),"
                + " SUM(集計情報.付与期間限定Ｐ３),"
                + " SUM(集計情報.買上Ｐ数Ｐ３),"
                + " SUM(集計情報.ベビーサークルＰ数Ｐ３),"
                + " SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ３),"
                + " SUM(集計情報.アプリＰ交換Ｐ数Ｐ３),"
                + " SUM(集計情報.カタリナ付与Ｐ数Ｐ３),"
                + " SUM(集計情報.その他付与Ｐ数Ｐ３),"
                + " SUM(集計情報.調整Ｐ数Ｐ３),"
                + " SUM(集計情報.付与期間限定合計),"
                + " SUM(集計情報.売上金額合計)," /* 売上金額合計追加 */
                + " SUM(集計情報.利用期間限定Ｐ００)," /* 期間限定利用Ｐ前月追加 */
                + " SUM(集計情報.利用期間限定Ｐ０),"
                + " SUM(集計情報.利用期間限定Ｐ１),"
                + " SUM(集計情報.利用期間限定Ｐ２),"
                + " SUM(集計情報.利用期間限定Ｐ３),"
                + " SUM(集計情報.利用期間限定合計),"
                + " SUM(集計情報.クリアＰ数有効期限),"
                + " SUM(集計情報.クリアＰ数顧客退会),"
                + " SUM(集計情報.クリアＰ数合計),"
                + " SUM(集計情報.月末Ｐ残高Ｐ１),"
                + " SUM(集計情報.月末Ｐ残高Ｐ２),"
                + " SUM(集計情報.月末Ｐ残高Ｐ３),"
                + " SUM(集計情報.月末Ｐ残高合計),"
                + " CASE DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' WHEN 3000 THEN 'MK' ELSE '%s' END AS 企業名"
                + " FROM"
                + " AS期間限定ポイント集計情報 集計情報"
                + " WHERE"
                + " 集計情報.集計対象月  = ?"
                + " GROUP BY"
                + " 集計情報.集計対象月,"
                + " DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード)"
                + " ORDER BY"
                + " 集計情報.集計対象月,"
                + " DECODE(集計情報.旧販社コード,'1040',1010,集計情報.旧販社コード)"
                , C_NULLSTR
        );

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPFile *** (企業別ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
                /*------------------------------------------------------------------------*/
        }

        memset(str_sql, 0x00, sizeof(str_sql));

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_sel2 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(企業別) PREPARE sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(企業別集計)", log_format_buf,
                    0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASKGP_HANSHA CURSOR FOR sql_sel2;
        sqlca.declare();

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(企業別) DECLARE CURSOR sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(企業別集計)", log_format_buf,
                    0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_ASKGP_HANSHA USING :h_batdate_ym;
        sqlca.open(h_batdate_ym);

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(企業別) CURSOR OPEN sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(企業別集計)", log_format_buf,
                    0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for ( ; ; ) {
            memset(as_kgp_t, 0x00, sizeof(as_kgp_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

//            EXEC SQL FETCH CUR_ASKGP_HANSHA
//            INTO :as_kgp_t.shukei_ymd,
//                          :as_kgp_t.kyu_hansya_cd,
//                          :as_kgp_t.fuyo_gentei_point0,
//                          :as_kgp_t.kaiage_point0,
//                          :as_kgp_t.babycircle_point0,
//                          :as_kgp_t.watashiplus_point0,
//                          :as_kgp_t.app_koukan_point0,
//                          :as_kgp_t.catalina_point0,
//                          :as_kgp_t.sonota_point0,
//                          :as_kgp_t.chosei_point0,
//                          :as_kgp_t.fuyo_gentei_point1,
//                          :as_kgp_t.kaiage_point1,
//                          :as_kgp_t.babycircle_point1,
//                          :as_kgp_t.watashiplus_point1,
//                          :as_kgp_t.app_koukan_point1,
//                          :as_kgp_t.catalina_point1,
//                          :as_kgp_t.sonota_point1,
//                          :as_kgp_t.chosei_point1,
//                          :as_kgp_t.fuyo_gentei_point2,
//                          :as_kgp_t.kaiage_point2,
//                          :as_kgp_t.babycircle_point2,
//                          :as_kgp_t.watashiplus_point2,
//                          :as_kgp_t.app_koukan_point2,
//                          :as_kgp_t.catalina_point2,
//                          :as_kgp_t.sonota_point2,
//                          :as_kgp_t.chosei_point2,
//                          :as_kgp_t.fuyo_gentei_point3,
//                          :as_kgp_t.kaiage_point3,
//                          :as_kgp_t.babycircle_point3,
//                          :as_kgp_t.watashiplus_point3,
//                          :as_kgp_t.app_koukan_point3,
//                          :as_kgp_t.catalina_point3,
//                          :as_kgp_t.sonota_point3,
//                          :as_kgp_t.chosei_point3,
//                          :as_kgp_t.fuyo_sum,
//                          :as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
//                          :as_kgp_t.riyou_sum00,
//                          :as_kgp_t.riyou_sum0,
//                          :as_kgp_t.riyou_sum1,
//                          :as_kgp_t.riyou_sum2,
//                          :as_kgp_t.riyou_sum3,
//                          :as_kgp_t.riyou_sum,
//                          :as_kgp_t.clear_pointkigen,
//                          :as_kgp_t.clear_pointtaikai,
//                          :as_kgp_t.clear_sum,
//                          :as_kgp_t.end_pointp1,
//                          :as_kgp_t.end_pointp2,
//                          :as_kgp_t.end_pointp3,
//                          :as_kgp_t.end_point,
//                          :h_kyu_hansya_name;
                          sqlca.fetch();
                          sqlca.recData(as_kgp_t.shukei_ymd,
                                  as_kgp_t.kyu_hansya_cd,
                                  as_kgp_t.fuyo_gentei_point0,
                                  as_kgp_t.kaiage_point0,
                                  as_kgp_t.babycircle_point0,
                                  as_kgp_t.watashiplus_point0,
                                  as_kgp_t.app_koukan_point0,
                                  as_kgp_t.catalina_point0,
                                  as_kgp_t.sonota_point0,
                                  as_kgp_t.chosei_point0,
                                  as_kgp_t.fuyo_gentei_point1,
                                  as_kgp_t.kaiage_point1,
                                  as_kgp_t.babycircle_point1,
                                  as_kgp_t.watashiplus_point1,
                                  as_kgp_t.app_koukan_point1,
                                  as_kgp_t.catalina_point1,
                                  as_kgp_t.sonota_point1,
                                  as_kgp_t.chosei_point1,
                                  as_kgp_t.fuyo_gentei_point2,
                                  as_kgp_t.kaiage_point2,
                                  as_kgp_t.babycircle_point2,
                                  as_kgp_t.watashiplus_point2,
                                  as_kgp_t.app_koukan_point2,
                                  as_kgp_t.catalina_point2,
                                  as_kgp_t.sonota_point2,
                                  as_kgp_t.chosei_point2,
                                  as_kgp_t.fuyo_gentei_point3,
                                  as_kgp_t.kaiage_point3,
                                  as_kgp_t.babycircle_point3,
                                  as_kgp_t.watashiplus_point3,
                                  as_kgp_t.app_koukan_point3,
                                  as_kgp_t.catalina_point3,
                                  as_kgp_t.sonota_point3,
                                  as_kgp_t.chosei_point3,
                                  as_kgp_t.fuyo_sum,
                                  as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                                  as_kgp_t.riyou_sum00,
                                  as_kgp_t.riyou_sum0,
                                  as_kgp_t.riyou_sum1,
                                  as_kgp_t.riyou_sum2,
                                  as_kgp_t.riyou_sum3,
                                  as_kgp_t.riyou_sum,
                                  as_kgp_t.clear_pointkigen,
                                  as_kgp_t.clear_pointtaikai,
                                  as_kgp_t.clear_sum,
                                  as_kgp_t.end_pointp1,
                                  as_kgp_t.end_pointp2,
                                  as_kgp_t.end_pointp3,
                                  as_kgp_t.end_point,
                                  h_kyu_hansya_name);

            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(企業別) FETCH sqlcode =[%d]\n", sqlca.sqlcode);
                        /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if ( sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND ) {

                sprintf( log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH",sqlca.sqlcode,
                        "AS期間限定ポイント集計情報(企業別集計)", log_format_buf,
                        0, 0);

//                EXEC SQL CLOSE CUR_ASKGP_HANSHA; /* カーソルクローズ           */
                sqlca.curse_close();
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                                /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if ( sqlca.sqlcode == C_const_Ora_NOTFOUND ) {
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(企業別) FETCH NOTFOUND=[%d]\n", sqlca.sqlcode);
                                /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント集計表に出力           */
            /*--------------------------------*/
            /*------------*/
            /* 企業コード */
            /*------------*/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf( wk_buf1, "\"%d\",\"", as_kgp_t.kyu_hansya_cd );
            buf_len = strlen(wk_buf1);

            /*------------*/
            /* 企業名     */
            /*------------*/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim( h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if(strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }

            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
            rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        ,rtn_cd ,0 ,0 ,0 ,0);
                /* 処理を終了する */
                return C_const_NG ;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /*------------*/
            /* 店舗コード */
            /*------------*/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf( wk_buf2, "\",\"%d\",\"", C_MISENO );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* 店舗名(企業計) */
            /*----------------*/
            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, C_MISENAME_HANSHA);
            rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        ,rtn_cd ,0 ,0 ,0 ,0);
                /* 処理を終了する */
                return C_const_NG ;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /*------------*/
            /* P数        */
            /*------------*/
            sprintf( wk_buf2,
                    "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
                    + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n", /* 売上金額合計追加、期間限定利用Ｐ前月追加 */
                    /* 付与期間限定Ｐ０ */
                    as_kgp_t.fuyo_gentei_point0,  as_kgp_t.kaiage_point0,
                    as_kgp_t.babycircle_point0,  as_kgp_t.watashiplus_point0,
                    as_kgp_t.app_koukan_point0,  as_kgp_t.catalina_point0,
                    as_kgp_t.sonota_point0,  as_kgp_t.chosei_point0,
                    /* 付与期間限定Ｐ１ */
                    as_kgp_t.fuyo_gentei_point1,  as_kgp_t.kaiage_point1,
                    as_kgp_t.babycircle_point1,  as_kgp_t.watashiplus_point1,
                    as_kgp_t.app_koukan_point1,  as_kgp_t.catalina_point1,
                    as_kgp_t.sonota_point1,  as_kgp_t.chosei_point1,
                    /* 付与期間限定Ｐ２ */
                    as_kgp_t.fuyo_gentei_point2,  as_kgp_t.kaiage_point2,
                    as_kgp_t.babycircle_point2,  as_kgp_t.watashiplus_point2,
                    as_kgp_t.app_koukan_point2,  as_kgp_t.catalina_point2,
                    as_kgp_t.sonota_point2,  as_kgp_t.chosei_point2,
                    /* 付与期間限定Ｐ３ */
                    as_kgp_t.fuyo_gentei_point3,  as_kgp_t.kaiage_point3,
                    as_kgp_t.babycircle_point3,  as_kgp_t.watashiplus_point3,
                    as_kgp_t.app_koukan_point3,  as_kgp_t.catalina_point3,
                    as_kgp_t.sonota_point3,  as_kgp_t.chosei_point3,
                    /* 付与期間限定Ｐ合計 */
                    as_kgp_t.fuyo_sum,
                    /* 売上金額合計 */
                    as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                    /* 利用期間限定Ｐ */
                    as_kgp_t.riyou_sum00,  as_kgp_t.riyou_sum0,  as_kgp_t.riyou_sum1,
                    as_kgp_t.riyou_sum2,    as_kgp_t.riyou_sum3,  as_kgp_t.riyou_sum, /* 期間限定利用Ｐ前月追加 */
                    /* クリアＰ */
                    as_kgp_t.clear_pointkigen,  as_kgp_t.clear_pointtaikai,
                    as_kgp_t.clear_sum,
                    /* 月末Ｐ残高 */
                    as_kgp_t.end_pointp1,  as_kgp_t.end_pointp2,
                    as_kgp_t.end_pointp3,  as_kgp_t.end_point
            );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /* ファイル書き込み */
            fwrite(wk_buf1, buf_len, 1, fp_kp);
            rtn_cd = ferror(fp_kp);
            if( rtn_cd != C_const_OK ){
                /* ファイル書き込みエラー */
                sprintf( log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

//                EXEC SQL CLOSE CUR_ASKGP_HANSHA; /* カーソルクローズ           */
                sqlca.curse_close();
                if (DBG_LOG){
                                /*----------------------------------------------------------------*/
                                C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                                /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_kp_cnt ++;

        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASKGP_HANSHA;
        sqlca.curse_close();

        /**************************************/
        /* AS期間限定Ｐ集計情報（全社）の取得 */
        /**************************************/
        memset(as_kgp_t, 0x00, sizeof(as_kgp_t));

//        EXEC SQL SELECT
//        集計情報.集計対象月,
//                SUM(集計情報.付与期間限定Ｐ０),
//                SUM(集計情報.買上Ｐ数Ｐ０),
//                SUM(集計情報.ベビーサークルＰ数Ｐ０),
//                SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ０),
//                SUM(集計情報.アプリＰ交換Ｐ数Ｐ０),
//                SUM(集計情報.カタリナ付与Ｐ数Ｐ０),
//                SUM(集計情報.その他付与Ｐ数Ｐ０),
//                SUM(集計情報.調整Ｐ数Ｐ０),
//                SUM(集計情報.付与期間限定Ｐ１),
//                SUM(集計情報.買上Ｐ数Ｐ１),
//                SUM(集計情報.ベビーサークルＰ数Ｐ１),
//                SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ１),
//                SUM(集計情報.アプリＰ交換Ｐ数Ｐ１),
//                SUM(集計情報.カタリナ付与Ｐ数Ｐ１),
//                SUM(集計情報.その他付与Ｐ数Ｐ１),
//                SUM(集計情報.調整Ｐ数Ｐ１),
//                SUM(集計情報.付与期間限定Ｐ２),
//                SUM(集計情報.買上Ｐ数Ｐ２),
//                SUM(集計情報.ベビーサークルＰ数Ｐ２),
//                SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ２),
//                SUM(集計情報.アプリＰ交換Ｐ数Ｐ２),
//                SUM(集計情報.カタリナ付与Ｐ数Ｐ２),
//                SUM(集計情報.その他付与Ｐ数Ｐ２),
//                SUM(集計情報.調整Ｐ数Ｐ２),
//                SUM(集計情報.付与期間限定Ｐ３),
//                SUM(集計情報.買上Ｐ数Ｐ３),
//                SUM(集計情報.ベビーサークルＰ数Ｐ３),
//                SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ３),
//                SUM(集計情報.アプリＰ交換Ｐ数Ｐ３),
//                SUM(集計情報.カタリナ付与Ｐ数Ｐ３),
//                SUM(集計情報.その他付与Ｐ数Ｐ３),
//                SUM(集計情報.調整Ｐ数Ｐ３),
//                SUM(集計情報.付与期間限定合計),
//                SUM(集計情報.売上金額合計), /* 売上金額合計追加 */
//                SUM(集計情報.利用期間限定Ｐ００), /* 期間限定利用Ｐ前月 */
//                SUM(集計情報.利用期間限定Ｐ０),
//                SUM(集計情報.利用期間限定Ｐ１),
//                SUM(集計情報.利用期間限定Ｐ２),
//                SUM(集計情報.利用期間限定Ｐ３),
//                SUM(集計情報.利用期間限定合計),
//                SUM(集計情報.クリアＰ数有効期限),
//                SUM(集計情報.クリアＰ数顧客退会),
//                SUM(集計情報.クリアＰ数合計),
//                SUM(集計情報.月末Ｐ残高Ｐ１),
//                SUM(集計情報.月末Ｐ残高Ｐ２),
//                SUM(集計情報.月末Ｐ残高Ｐ３),
//                SUM(集計情報.月末Ｐ残高合計)
//        INTO :as_kgp_t.shukei_ymd,
//            :as_kgp_t.fuyo_gentei_point0,
//            :as_kgp_t.kaiage_point0,
//            :as_kgp_t.babycircle_point0,
//            :as_kgp_t.watashiplus_point0,
//            :as_kgp_t.app_koukan_point0,
//            :as_kgp_t.catalina_point0,
//            :as_kgp_t.sonota_point0,
//            :as_kgp_t.chosei_point0,
//            :as_kgp_t.fuyo_gentei_point1,
//            :as_kgp_t.kaiage_point1,
//            :as_kgp_t.babycircle_point1,
//            :as_kgp_t.watashiplus_point1,
//            :as_kgp_t.app_koukan_point1,
//            :as_kgp_t.catalina_point1,
//            :as_kgp_t.sonota_point1,
//            :as_kgp_t.chosei_point1,
//            :as_kgp_t.fuyo_gentei_point2,
//            :as_kgp_t.kaiage_point2,
//            :as_kgp_t.babycircle_point2,
//            :as_kgp_t.watashiplus_point2,
//            :as_kgp_t.app_koukan_point2,
//            :as_kgp_t.catalina_point2,
//            :as_kgp_t.sonota_point2,
//            :as_kgp_t.chosei_point2,
//            :as_kgp_t.fuyo_gentei_point3,
//            :as_kgp_t.kaiage_point3,
//            :as_kgp_t.babycircle_point3,
//            :as_kgp_t.watashiplus_point3,
//            :as_kgp_t.app_koukan_point3,
//            :as_kgp_t.catalina_point3,
//            :as_kgp_t.sonota_point3,
//            :as_kgp_t.chosei_point3,
//            :as_kgp_t.fuyo_sum,
//            :as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
//            :as_kgp_t.riyou_sum00, /* 期間限定利用Ｐ前月 */
//            :as_kgp_t.riyou_sum0,
//            :as_kgp_t.riyou_sum1,
//            :as_kgp_t.riyou_sum2,
//            :as_kgp_t.riyou_sum3,
//            :as_kgp_t.riyou_sum,
//            :as_kgp_t.clear_pointkigen,
//            :as_kgp_t.clear_pointtaikai,
//            :as_kgp_t.clear_sum,
//            :as_kgp_t.end_pointp1,
//            :as_kgp_t.end_pointp2,
//            :as_kgp_t.end_pointp3,
//            :as_kgp_t.end_point
//            FROM
//        AS期間限定ポイント集計情報 集計情報
//        WHERE
//        集計情報.集計対象月  = :h_batdate_ym
//        GROUP BY
//        集計情報.集計対象月
//        ORDER BY
//        集計情報.集計対象月;

        sqlca.sql = new StringDto("SELECT"
                + "    集計情報.集計対象月,"
                + "    SUM(集計情報.付与期間限定Ｐ０),"
                + "    SUM(集計情報.買上Ｐ数Ｐ０),"
                + "    SUM(集計情報.ベビーサークルＰ数Ｐ０),"
                + "    SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ０),"
                + "    SUM(集計情報.アプリＰ交換Ｐ数Ｐ０),"
                + "    SUM(集計情報.カタリナ付与Ｐ数Ｐ０),"
                + "    SUM(集計情報.その他付与Ｐ数Ｐ０),"
                + "    SUM(集計情報.調整Ｐ数Ｐ０),"
                + "    SUM(集計情報.付与期間限定Ｐ１),"
                + "    SUM(集計情報.買上Ｐ数Ｐ１),"
                + "    SUM(集計情報.ベビーサークルＰ数Ｐ１),"
                + "    SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ１),"
                + "    SUM(集計情報.アプリＰ交換Ｐ数Ｐ１),"
                + "    SUM(集計情報.カタリナ付与Ｐ数Ｐ１),"
                + "    SUM(集計情報.その他付与Ｐ数Ｐ１),"
                + "    SUM(集計情報.調整Ｐ数Ｐ１),"
                + "    SUM(集計情報.付与期間限定Ｐ２),"
                + "    SUM(集計情報.買上Ｐ数Ｐ２),"
                + "    SUM(集計情報.ベビーサークルＰ数Ｐ２),"
                + "    SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ２),"
                + "    SUM(集計情報.アプリＰ交換Ｐ数Ｐ２),"
                + "    SUM(集計情報.カタリナ付与Ｐ数Ｐ２),"
                + "    SUM(集計情報.その他付与Ｐ数Ｐ２),"
                + "    SUM(集計情報.調整Ｐ数Ｐ２),"
                + "    SUM(集計情報.付与期間限定Ｐ３),"
                + "    SUM(集計情報.買上Ｐ数Ｐ３),"
                + "    SUM(集計情報.ベビーサークルＰ数Ｐ３),"
                + "    SUM(集計情報.ワタシプラス連携付与Ｐ数Ｐ３),"
                + "    SUM(集計情報.アプリＰ交換Ｐ数Ｐ３),"
                + "    SUM(集計情報.カタリナ付与Ｐ数Ｐ３),"
                + "    SUM(集計情報.その他付与Ｐ数Ｐ３),"
                + "    SUM(集計情報.調整Ｐ数Ｐ３),"
                + "    SUM(集計情報.付与期間限定合計),"
                + "    SUM(集計情報.売上金額合計), /* 売上金額合計追加 */"
                + "    SUM(集計情報.利用期間限定Ｐ００), /* 期間限定利用Ｐ前月 */"
                + "    SUM(集計情報.利用期間限定Ｐ０),"
                + "    SUM(集計情報.利用期間限定Ｐ１),"
                + "    SUM(集計情報.利用期間限定Ｐ２),"
                + "    SUM(集計情報.利用期間限定Ｐ３),"
                + "    SUM(集計情報.利用期間限定合計),"
                + "    SUM(集計情報.クリアＰ数有効期限),"
                + "    SUM(集計情報.クリアＰ数顧客退会),"
                + "    SUM(集計情報.クリアＰ数合計),"
                + "    SUM(集計情報.月末Ｐ残高Ｐ１),"
                + "    SUM(集計情報.月末Ｐ残高Ｐ２),"
                + "    SUM(集計情報.月末Ｐ残高Ｐ３),"
                + "    SUM(集計情報.月末Ｐ残高合計)"
                + "    FROM"
                + "    AS期間限定ポイント集計情報 集計情報"
                + "    WHERE"
                + "    集計情報.集計対象月  = ?"
                + "    GROUP BY"
                + "    集計情報.集計対象月"
                + "    ORDER BY"
                + "    集計情報.集計対象月");
        sqlca.restAndExecute(h_batdate_ym);
        sqlca.fetch();
        sqlca.recData(as_kgp_t.shukei_ymd,
                as_kgp_t.fuyo_gentei_point0,
                as_kgp_t.kaiage_point0,
                as_kgp_t.babycircle_point0,
                as_kgp_t.watashiplus_point0,
                as_kgp_t.app_koukan_point0,
                as_kgp_t.catalina_point0,
                as_kgp_t.sonota_point0,
                as_kgp_t.chosei_point0,
                as_kgp_t.fuyo_gentei_point1,
                as_kgp_t.kaiage_point1,
                as_kgp_t.babycircle_point1,
                as_kgp_t.watashiplus_point1,
                as_kgp_t.app_koukan_point1,
                as_kgp_t.catalina_point1,
                as_kgp_t.sonota_point1,
                as_kgp_t.chosei_point1,
                as_kgp_t.fuyo_gentei_point2,
                as_kgp_t.kaiage_point2,
                as_kgp_t.babycircle_point2,
                as_kgp_t.watashiplus_point2,
                as_kgp_t.app_koukan_point2,
                as_kgp_t.catalina_point2,
                as_kgp_t.sonota_point2,
                as_kgp_t.chosei_point2,
                as_kgp_t.fuyo_gentei_point3,
                as_kgp_t.kaiage_point3,
                as_kgp_t.babycircle_point3,
                as_kgp_t.watashiplus_point3,
                as_kgp_t.app_koukan_point3,
                as_kgp_t.catalina_point3,
                as_kgp_t.sonota_point3,
                as_kgp_t.chosei_point3,
                as_kgp_t.fuyo_sum,
                as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                as_kgp_t.riyou_sum00, /* 期間限定利用Ｐ前月 */
                as_kgp_t.riyou_sum0,
                as_kgp_t.riyou_sum1,
                as_kgp_t.riyou_sum2,
                as_kgp_t.riyou_sum3,
                as_kgp_t.riyou_sum,
                as_kgp_t.clear_pointkigen,
                as_kgp_t.clear_pointtaikai,
                as_kgp_t.clear_sum,
                as_kgp_t.end_pointp1,
                as_kgp_t.end_pointp2,
                as_kgp_t.end_pointp3,
                as_kgp_t.end_point);

        if (DBG_LOG){
                /*--------------------------------------------------------------------*/
                C_DbgMsg( "*** cmBTkzdkB_CreKikangenteiPFile *** AS期間限定Ｐ集計情報(全社) SELECT sqlcode =[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
        }
        /* データ無し以外のエラーの場合処理を異常終了する */
        if ( sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND ) {

            sprintf( log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT",sqlca.sqlcode,
                    "AS期間限定ポイント集計情報(全社集計)", log_format_buf, 0, 0);
            if (DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgEnd("cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0, 0);
                        /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG ;
        }

        /*--------------------------------*/
        /* ポイント集計表に出力           */
        /*--------------------------------*/
        /*------------*/
        /* 企業コード */
        /*------------*/
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        sprintf( wk_buf1, "\"%d\",\"", C_KAISHACD );
        buf_len = strlen(wk_buf1);

        /*----------------*/
        /* 企業名(全社計) */
        /*----------------*/
        /* 会社名(,店舗名)のUTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        strcpy(utf8_buf, C_MISENAME_ZENSHA);
        rtn_cd = C_ConvUT2SJ( utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                    ,rtn_cd ,0 ,0 ,0 ,0);
            /* 処理を終了する */
            return C_const_NG ;
        }
        memcpy(wk_buf1, sjis_buf, sjis_len.arr);
        buf_len += sjis_len.arr;

        /*------------*/
        /* 店舗コード */
        /*------------*/
        sprintf( wk_buf2, "\",\"%d\",\"", C_MISENO );
        memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
        buf_len += strlen(wk_buf2);

        /*----------------*/
        /* 店舗名(全社計) */
        /*----------------*/
        memcpy(wk_buf1, sjis_buf, sjis_len.arr);
        buf_len += sjis_len.arr;

        /*------------*/
        /* P数        */
        /*------------*/
        sprintf( wk_buf2,
                "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
               + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
               + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
               + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
               + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\""
               + ",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n", /* 売上金額合計追加、期間限定利用Ｐ前月 */
                /* 付与期間限定Ｐ０ */
                as_kgp_t.fuyo_gentei_point0,  as_kgp_t.kaiage_point0,
                as_kgp_t.babycircle_point0,  as_kgp_t.watashiplus_point0,
                as_kgp_t.app_koukan_point0,  as_kgp_t.catalina_point0,
                as_kgp_t.sonota_point0,  as_kgp_t.chosei_point0,
                /* 付与期間限定Ｐ１ */
                as_kgp_t.fuyo_gentei_point1,  as_kgp_t.kaiage_point1,
                as_kgp_t.babycircle_point1,  as_kgp_t.watashiplus_point1,
                as_kgp_t.app_koukan_point1,  as_kgp_t.catalina_point1,
                as_kgp_t.sonota_point1,  as_kgp_t.chosei_point1,
                /* 付与期間限定Ｐ２ */
                as_kgp_t.fuyo_gentei_point2,  as_kgp_t.kaiage_point2,
                as_kgp_t.babycircle_point2,  as_kgp_t.watashiplus_point2,
                as_kgp_t.app_koukan_point2,  as_kgp_t.catalina_point2,
                as_kgp_t.sonota_point2,  as_kgp_t.chosei_point2,
                /* 付与期間限定Ｐ３ */
                as_kgp_t.fuyo_gentei_point3,  as_kgp_t.kaiage_point3,
                as_kgp_t.babycircle_point3,  as_kgp_t.watashiplus_point3,
                as_kgp_t.app_koukan_point3,  as_kgp_t.catalina_point3,
                as_kgp_t.sonota_point3,  as_kgp_t.chosei_point3,
                /* 付与期間限定Ｐ合計 */
                as_kgp_t.fuyo_sum,
                /* 売上金額合計 */
                as_kgp_t.uriagekingaku_sum, /* 売上金額合計追加 */
                /* 利用期間限定Ｐ */
                as_kgp_t.riyou_sum00, as_kgp_t.riyou_sum0,  as_kgp_t.riyou_sum1,
                as_kgp_t.riyou_sum2,   as_kgp_t.riyou_sum3,  as_kgp_t.riyou_sum,
                /* クリアＰ */
                as_kgp_t.clear_pointkigen,  as_kgp_t.clear_pointtaikai,
                as_kgp_t.clear_sum,
                /* 月末Ｐ残高 */
                as_kgp_t.end_pointp1,  as_kgp_t.end_pointp2,
                as_kgp_t.end_pointp3,  as_kgp_t.end_point
        );
        memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
        buf_len += strlen(wk_buf2);

        /* ファイル書き込み */
        fwrite(wk_buf1, buf_len, 1, fp_kp);
        rtn_cd = ferror(fp_kp);
        if( rtn_cd != C_const_OK ){
            /* ファイル書き込みエラー */
            sprintf( log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT( "911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG){
                        /*------------------------------------------------------------------*/
                        C_DbgEnd( "cmBTkzdkB_CreKikangenteiPFile処理", C_const_NG, 0,0);
                        /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_kp_cnt ++;

        /* 出力ファイルクローズ */
        fclose(fp_kp);

        /* 処理を終了する */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_SetMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTkzdkB_SetMonth()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      対象年月の月から４ヶ月分、前月分の月名（０１～１２）を設定する        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public void cmBTkzdkB_SetMonth()
    {

        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgStart("*** cmBTkzdkB_SetMonth処理 ***");
                /*------------------------------------------------------------------------*/
        }

        int i;                      /* ループカウンタ    */
        int i_target_MM = 0;            /* 対象月            */
        int i_wkMM;                 /* 作業用対象月      */
        StringDto c_wkMM = new StringDto(16);            /* 作業用対象月      */

        /* 対象月取得 */
        if( h_batdate_ym.intVal() != 0 )
        {
            i_target_MM = h_batdate_ym.intVal() % 100;
        }

        i = 0;
        for (i = 0; i < 4; i++) {
            i_wkMM = 0;
            i_wkMM = i_target_MM + i;
            if(i_wkMM > 12){
                // １～１２月に変換
                i_wkMM = i_wkMM - 12;
            }

            /* 対象月（０１～１２）取得 */
            memset(c_wkMM, 0x00, sizeof(c_wkMM));
            cmBTkzdkB_SetMonthMM(i_wkMM, c_wkMM);

            g_MMZenkaku[i] = new MONTH_ZENKAKU();
            g_MMZenkaku[i].i_MM.arr = i_wkMM;
            strcpy(g_MMZenkaku[i].c_MMZen, c_wkMM);
        } /* FOR END */

        /* 前月分取得 */
        i_wkMM = 0;
        if (i_target_MM != 1) {
            i_wkMM = i_target_MM - 1;
        } else {
            i_wkMM = 12;
        }
        /* 対象月（０１～１２）取得 */
        memset(c_wkMM, 0x00, sizeof(c_wkMM));
        cmBTkzdkB_SetMonthMM(i_wkMM, c_wkMM);
        if (null == g_MMZenkaku[4]) {
            g_MMZenkaku[4] = new MONTH_ZENKAKU();
        }
        g_MMZenkaku[4].i_MM.arr = i_wkMM;
        strcpy(g_MMZenkaku[4].c_MMZen, c_wkMM);

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月[%d]\n", i_target_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月前月[%d]\n",
                        g_MMZenkaku[4].i_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 変換前月[%s]\n",
                        g_MMZenkaku[4].c_MMZen);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月０[%d]\n", g_MMZenkaku[0].i_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 変換０[%s]\n", g_MMZenkaku[0].c_MMZen);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月１[%d]\n", g_MMZenkaku[1].i_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 変換１[%s]\n", g_MMZenkaku[1].c_MMZen);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月２[%d]\n", g_MMZenkaku[2].i_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 変換２[%s]\n", g_MMZenkaku[2].c_MMZen);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 対象月３[%d]\n", g_MMZenkaku[3].i_MM);
                C_DbgMsg("*** cmBTkzdkB_SetMonth *** 変換３[%s]\n", g_MMZenkaku[3].c_MMZen);
                /*------------------------------------------------------------*/
                /*------------------------------------------------------------*/
                C_DbgEnd( "cmBTkzdkB_SetMonth処理", C_const_OK, 0,0);
                /*------------------------------------------------------------*/
        }
        return;

    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkzdkB_SetMonthMM                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTkzdkB_SetMonthMM()                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      対象年月の月から４ヶ月分の月名（０１～１２）を設定する                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              int             month     ： 対象月                           */
    /*              char       *    monthNM   ： 対象月（０１～１２）を返す       */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    public void cmBTkzdkB_SetMonthMM(int month, StringDto monthNM)
    {
        if (DBG_LOG){
                /*------------------------------------------------------------------------*/
                C_DbgStart("*** cmBTkzdkB_SetMonthMM処理 ***");
                /*------------------------------------------------------------------------*/
        }

        switch (month) {
            case 1:
                strcpy(monthNM, "０１");
                break;
            case 2:
                strcpy(monthNM, "０２");
                break;
            case 3:
                strcpy(monthNM, "０３");
                break;
            case 4:
                strcpy(monthNM, "０４");
                break;
            case 5:
                strcpy(monthNM, "０５");
                break;
            case 6:
                strcpy(monthNM, "０６");
                break;
            case 7:
                strcpy(monthNM, "０７");
                break;
            case 8:
                strcpy(monthNM, "０８");
                break;
            case 9:
                strcpy(monthNM, "０９");
                break;
            case 10:
                strcpy(monthNM, "１０");
                break;
            case 11:
                strcpy(monthNM, "１１");
                break;
            case 12:
                strcpy(monthNM, "１２");
                break;
            default:
                strcpy(monthNM, "００");
                break;
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkzdkB_SetMonthMM *** 対象月[%d]\n", month);
                C_DbgMsg("*** cmBTkzdkB_SetMonthMM ***   変換[%s]\n", monthNM);
                /*------------------------------------------------------------*/
                /*------------------------------------------------------------*/
                C_DbgEnd( "cmBTkzdkB_SetMonthMM処理", C_const_OK, 0,0);
                /*------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return;
    }
}
