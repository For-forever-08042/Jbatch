package jp.co.mcc.nttdata.batch.business.service.cmBTsgdkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.AS_MKSOUGO_SHUKEI_INFO_TBL;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/**
 * /*******************************************************************************
 * *   プログラム名   ： 店別ＭＫ相互付与取引集計（cmBTsgdkB.pc）
 * *
 * *   【処理概要】
 * *     MK社との精算チェック用として、『CF店舗利用のMK会員取引』のMK付与ポイント
 * *     を対象とした会計データを作成する。
 * *     ・月次の会計向けポイント集計情報として、
 * *       店舗単位に各ポイント数を集計しMK相互取引集計表に出力する。。
 * *
 * *   【引数説明】
 * *     -d処理日付           :（任意）処理日付
 * *     -DEBUG(-debug)       :（任意）デバッグモードでの実行
 * *
 * *   【戻り値】
 * *      10     ： 正常
 * *      99     ： 異常
 * *
 * *------------------------------------------------------------------------------
 * *   稼働環境
 * *      Red Hat Enterprise Linux 6（64bit）
 * *      (文字コード ： UTF8)
 * *------------------------------------------------------------------------------
 * *   改定履歴
 * *     30.00 :  2021/09/22  SSI.張：初版
 * *
 * *------------------------------------------------------------------------------
 * *  $Id:$
 * *------------------------------------------------------------------------------
 * *  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 * *****************************************************************************
 */
@Service
public class CmBTsgdkBServiceImpl extends CmABfuncLServiceImpl implements CmBTsgdkBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE  AS_MKSOUGO_SHUKEI_INFO.h;      /* ASMK相互取引集計情報      */
    AS_MKSOUGO_SHUKEI_INFO_TBL asmk_t = new AS_MKSOUGO_SHUKEI_INFO_TBL();            /* ASMK相互取引集計情報      */
    StringDto h_kyu_hansya_name = new StringDto(30 * 3 + 1);         /* 旧販社名                  */
    StringDto h_mise_kanji = new StringDto(40 * 3 + 1);               /* 漢字店舗名称              */
    ItemDto h_batdate_ymd = new ItemDto();                     /* バッチ処理日付(当日)      */
    ItemDto h_batdate_ymd_prev = new ItemDto();              /* バッチ処理日付(前月)      */
    ItemDto hs_batdate_ymd = new ItemDto(9);                  /* バッチ処理日付(当日)      */
    ItemDto h_batdate_ym = new ItemDto();                   /* 集計対象月                */
    ItemDto hs_batdate_ym = new ItemDto(7);                 /* 集計対象月                */
    ItemDto h_batdate_ym_start_date = new ItemDto();          /* 集計対象月初日            */
    ItemDto h_batdate_ym_end_date = new ItemDto();           /* 集計対象月末日            */
    ItemDto h_batdate_pym_start_date = new ItemDto();        /* 集計対象前月初日          */
    ItemDto h_batdate_pym_end_date = new ItemDto();           /* 集計対象前月末日          */
    ItemDto h_batdate_pym = new ItemDto();                    /* 集計対象月の前月          */
    ItemDto hs_batdate_pym = new ItemDto(7);                /* 集計対象月の前月          */
    ItemDto h_batdate_nym = new ItemDto();                   /* 集計対象月の翌月          */
    ItemDto hs_batdate_nym = new ItemDto(7);                /* 集計対象月の翌月          */
    StringDto str_sql = new StringDto(4096 * 18);                   /* 実行用SQL文字列１         */
    ItemDto h_program_id = new ItemDto(20 + 1);             /* プログラムID              */
    ItemDto h_system_date = new ItemDto(9);                 /* 作成日                    */
    ItemDto h_system_time = new ItemDto(9);                /* 作成時刻                  */

//    EXEC SQL END DECLARE SECTION;


    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;   /* OFF                                */
    int DEF_ON = 1;   /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_D = "-d";      /* 処理日付                           */
    String DEF_DEBUG = "-DEBUG";      /* デバッグスイッチ                   */
    String DEF_debug = "-debug";      /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "MK相互取引集計";     /* APログ用機能名             */
    String C_FNAME_PD = "MK相互取引集計表_";    /* ポイント集計表ファイル名   */
    String C_P_DATE = "作成日    ";        /* 会計帳票出力用             */
    String C_P_TIME = "作成時刻  ";       /* 会計帳票出力用             */

    String C_PD_HEADER = "\"企業ＣＤ\",\"企業名\",\"店舗ＣＤ\",\"店舗名\",\"買上取引数\",\"売上金額\",\"買上ＭＫ付与ポイント数\",\"調整引取数\",\"企業調整ＭＫ付与ポイント数\",\"顧客調整ＭＫ付与ポイント数\",\"合計ＭＫ付与ポイント数\"";

    String C_MISENAME_ZENSHA = "全社計";        /* 店舗名(全社集計)           */
    String C_MISENAME_HANSHA = "企業計";        /* 店舗名(企業別集計)         */
    int C_MISENO = 9999;           /* 店舗ＣＤ(企業別/全社集計)  */
    int C_KAISHACD = 99;           /* 会社コード(全社集計)       */
    String C_nullSTR = "null";           /* 空文字置換文字列           */
    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int arg_d_chk;            /* 引数dチェック用                        */
    int g_pd_cnt;             /* ファイル出力件数(MKポイント集計表)     */
    StringDto out_file_dir = new StringDto(4096);   /* ワークディレクトリ                     */
    StringDto pd_fl_name = new StringDto(45);       /* 出力ファイル名(MKポイント集計表)       */
    StringDto pd_fl_name_sjis = new StringDto(45);  /* 出力ファイル名(MKポイント集計表)       */
    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                  */
    FileStatusDto fp_pd;           /* 出力ファイルポインタ(MKポイント集計表)     */
    /*----------------------------------------------------------------------------*/

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
        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                   */
        String env_wrk;                       /* 環境変数取得用                 */
        StringDto bat_date_ymd = new StringDto(9);                /* バッチ処理年月日(一時格納)     */
        StringDto wk_buf = new StringDto(256);                    /* 出力ファイル名SJIS変換用       */
        IntegerDto ret = new IntegerDto();
        IntegerDto len = new IntegerDto();

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /* 変数初期化 */
        g_pd_cnt = 0;
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
        memset(pd_fl_name, 0x00, sizeof(pd_fl_name));
        memset(pd_fl_name_sjis, 0x00, sizeof(pd_fl_name_sjis));
        memset(hs_batdate_ymd, 0x00, sizeof(hs_batdate_ymd));
        memset(hs_batdate_ym, 0x00, sizeof(hs_batdate_ym));
        memset(hs_batdate_pym, 0x00, sizeof(hs_batdate_pym));
        memset(hs_batdate_nym, 0x00, sizeof(hs_batdate_nym));
        memset(h_program_id, 0x00, sizeof(h_program_id));
        arg_d_chk = DEF_OFF;

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
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
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
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

            /* 引数の文字列が "-DEBUG" または "-debug" の場合 */
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;

                /* 引数の文字列が "-d" で始まる場合 */
            } else if (memcmp(arg_Work1, DEF_ARG_D, 2) == 0) {
                memset(log_format_buf, 0x00, sizeof(log_format_buf));
                /* パラメータのチェックをする */
                rtn_cd = cmBTsgdkB_ChkArgdInf(arg_Work1);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();
                    return exit(C_const_APNG);
                } else {
                    /* チェックがOKだったら、ホスト変数に代入する */
                    strncpy(hs_batdate_ymd, arg_Work1.arr.substring(2), 8);
                    h_batdate_ymd.arr = atoi(hs_batdate_ymd);
                }

                /* それ以外(規定外パラメータ) */
            } else {
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
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
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（当日）             */
        /*-------------------------------------*/
        if (StringUtils.isEmpty(hs_batdate_ymd.strVal())) {

            rtn_status.arr = 0;
            memset(bat_date_ymd, 0x00, sizeof(bat_date_ymd));

            /* バッチ処理日付を取得（当日） */
            rtn_cd = C_GetBatDate(0, bat_date_ymd, rtn_status);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
                    /*----------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_GetBatDate",
                        rtn_cd, rtn_status, 0, 0, 0);
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
            strcpy(hs_batdate_ymd, bat_date_ymd);
            h_batdate_ymd.arr = atoi(hs_batdate_ymd);
        }

        /*-------------------------------------*/
        /* 集計対象日付を取得                  */
        /*-------------------------------------*/
        sqlca.sql = new StringDto(" SELECT TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'MM'), 'YYYYMMDD')),\n" +
                "        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1)), 'YYYYMMDD')),\n" +
                "        TO_NUMBER(TO_CHAR(TRUNC(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2), 'MM'), 'YYYYMMDD')),\n" +
                "        TO_NUMBER(TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2)), 'YYYYMMDD')),\n" +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'YYYYMM')),\n" +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -2), 'YYYYMM')),\n" +
                "        TO_NUMBER(TO_CHAR(TO_DATE(?, 'YYYYMMDD'), 'YYYYMM')),\n" +
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'YYYYMMDD')) FROM DUAL");

        sqlca.restAndExecute(hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd,
                hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd, hs_batdate_ymd);
        sqlca.fetchInto(h_batdate_ym_start_date,
                h_batdate_ym_end_date,
                h_batdate_pym_start_date,
                h_batdate_pym_end_date,
                h_batdate_ym,
                h_batdate_pym,
                h_batdate_nym,
                h_batdate_ymd_prev);
        /* エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {

            sprintf(log_format_buf, "バッチ処理日付 = %s\n", hs_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "DUAL", log_format_buf, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理日付=[%d]\n", h_batdate_ymd);
            C_DbgMsg("*** main *** 集計対象月=[%d]\n", h_batdate_ym);
            C_DbgMsg("*** main *** 集計対象前月=[%d]\n", h_batdate_pym);
            C_DbgMsg("*** main *** 集計対象翌月=[%d]\n", h_batdate_nym);
            C_DbgMsg("*** main *** 集計対象日付(月初日)=[%d]\n",
                    h_batdate_ym_start_date);
            C_DbgMsg("*** main *** 集計対象日付(月末日)=[%d]\n", h_batdate_ym_end_date);
            C_DbgMsg("*** main *** 集計対象日付(前月初日)=[%d]\n",
                    h_batdate_pym_start_date);
            C_DbgMsg("*** main *** 集計対象日付(前月末日)=[%d]\n",
                    h_batdate_pym_end_date);
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 出力ファイル名                      */
        /*-------------------------------------*/
        sprintf(pd_fl_name, "%s%d.csv", C_FNAME_PD, h_batdate_ym);

        /* MKポイント集計表:ファイル名をSJISに変換 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        len.arr = 0;
        ret.arr = C_ConvUT2SJ(pd_fl_name, strlen(pd_fl_name), wk_buf, len);
        if (ret.arr != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** MKポイント集計表ファイル名変換エラー = %d\n",
                        ret);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", ret, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return functionCode(C_const_APNG);
        } else {
            /* MKポイント集計表ファイル名（SJIS） */
            sprintf(pd_fl_name_sjis, "%s", wk_buf);
        }

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n",
                    "CM_APWORK_DATE");
            /*-------------------------------------------------------------*/
        }
        env_wrk = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_wrk)) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "null");
                /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }
        /* 出力ファイルDIRセット */
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n",
                    out_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 作成日、作成時刻を取得              */
        /*-------------------------------------*/
        memset(h_system_date, 0x00, sizeof(h_system_date));
        memset(h_system_time, 0x00, sizeof(h_system_time));

        sqlca.sql.arr = "SELECT\n" +
                "        TO_CHAR(sysdate(), 'YYYYMMDD'),\n" +
                "                TO_CHAR(sysdate(), 'HH24:MI:SS')    FROM dual";
        sqlca.restAndExecute();
        sqlca.fetchInto(h_system_date,
                h_system_time);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "作成日の取得に失敗しました", 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return functionCode(C_const_NG);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/

        /*-------------------------*/
        /*  集計前処理             */
        /*-------------------------*/
        rtn_cd = cmBTsgdkB_PreTotal();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTsgdkB_PreTotal NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "集計前処理に失敗しました", 0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
            sqlca.rollback();
//            EXEC SQL ROLLBACK RELEASE;
            return exit(C_const_APNG);
        }
//        EXEC SQL COMMIT WORK;
        sqlca.commit();
        /*--------------------------------*/
        /*  MKポイント集計情報作成処理  */
        /*--------------------------------*/
        rtn_cd = cmBTsgdkB_CreateMKPoint();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTsgdkB_CreateMKPoint NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "MKポイント集計情報作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit(C_const_APNG);
        }

        /*--------------------------------*/
        /*  MKポイント集計表作成処理    */
        /*--------------------------------*/
        rtn_cd = cmBTsgdkB_CreateMKPointFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTsgdkB_CreateMKPointFile NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "MKポイント集計表作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            /* 出力ファイルクローズ */
            fclose(fp_pd);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
            sqlca.rollback();
//            EXEC SQL ROLLBACK RELEASE;
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        /* 処理件数出力 */
        sprintf(log_format_buf, "%s/%s", out_file_dir, pd_fl_name);
        APLOG_WT("105", 0, null, log_format_buf, g_pd_cnt,
                0, 0, 0, 0);

        /* 終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        /* コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease(sqlca);

        return exit(C_const_APOK);
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTsgdkB_ChkArgdInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTsgdkB_ChkArgdInf(char *Arg_in)                            */
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

    /******************************************************************************/

    public int cmBTsgdkB_ChkArgdInf(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTsgdkB_ChkArgdInf処理 ***");
            /*---------------------------------------------------------------------*/
        }
        /*-------------------------------------*/
        /*  重複指定チェック                   */
        /*-------------------------------------*/
        if (arg_d_chk != DEF_OFF) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTsgdkB_ChkArgdInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数が複数設定されています（%s）", Arg_in);
            return (C_const_NG);
        }
        arg_d_chk = DEF_ON;

        /*-------------------------------------*/
        /*  設定値nullチェック                 */
        /*-------------------------------------*/
        if (Arg_in.arr.length() <= 2) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTsgdkB_ChkArgdInf *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
            return (C_const_NG);
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("*** cmBTsgdkB_ChkArgdInf処理 ***", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTsgdkB_PreTotal                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTsgdkB_PreTotal()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      ASMK相互取引集計のポイント情報をクリアする。                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTsgdkB_PreTotal() {
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTsgdkB_PreTotal処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /* ASMK相互取引集計のクリア */
//        EXEC SQL TRUNCATE TABLE ASMK相互取引集計;
        sqlca.sql.arr = "TRUNCATE TABLE ASMK相互取引集計";
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_PreTotal *** ASMK相互取引集計 TRUNCATE " +
                    "sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        sqlca.restAndExecute();
        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "クリア");
            APLOG_WT("904", 0, null, "TRUNCATE", sqlca.sqlcode,
                    "ASMK相互取引集計", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_PreTotal処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTsgdkB_CreateMKPoint                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTsgdkB_CreateMKPoint()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      HSポイント日別情報から店舗別集計値をポイント集計情報に登録する。      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTsgdkB_CreateMKPoint() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen * 18);       /* 動的SQLバッファ            */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTsgdkB_CreateMKPoint処理 ***");
            /*------------------------------------------------------------------------*/
        }

        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));

        /*-------------------------------------------------------*/
        /* ASMK相互取引集計情報に                                */
        /* HSポイント日別情報より店舗毎に集計したデータを登録    */
        /*-------------------------------------------------------*/
        sprintf(wk_sql,
                "INSERT INTO ASMK相互取引集計 " +
                "( " +
                        "集計対象月, " +
                        "旧販社コード, " +
                        "店番号, " +
                        "買上取引数, " +
                        "売上金額, " +
                        "買上ＭＫ付与ポイント数, " +
                        "調整取引数, " +
                        "企業調整ＭＫ付与ポイント数, " +
                        "顧客調整ＭＫ付与ポイント数, " +
                        "合計ＭＫ付与ポイント数 " +
                        ") " +
                        "SELECT ?, " +
                        "NVL(店情報.企業コード, 0) as 会社ＣＤ, " +
                        "MK相互取引集計.店番号 as 店舗ＣＤ, " +
                        "SUM(MK相互取引集計.買上取引数) as 買上取引数, " +
                        "SUM(MK相互取引集計.売上金額) as 売上金額, " +
                        "SUM(MK相互取引集計.買上ＭＫ付与ポイント数) as 買上ＭＫ付与ポイント数, " +
                        "SUM(MK相互取引集計.調整取引数) as 調整取引数, " +
                        "SUM(MK相互取引集計.企業調整ＭＫ付与ポイント数) as 企業調整ＭＫ付与ポイント数, " +
                        "SUM(MK相互取引集計.顧客調整ＭＫ付与ポイント数) as 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(MK相互取引集計.合計ＭＫ付与ポイント数) as 合計ＭＫ付与ポイント数 " +
                        "FROM ( " +
                        "(SELECT 店番号, " +
                        "COUNT(*) AS 買上取引数, " +
                        "SUM(ポイント対象金額) AS 売上金額, " +
                        "SUM(付与ポイント) AS 買上ＭＫ付与ポイント数, " +
                        "0 AS 調整取引数, " +
                        "0 AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 40 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 < ? " +
                        "AND 精算年月日 between ? and ? " +
                        "AND 付与ポイント <> 0 " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                        "COUNT(*) AS 買上取引数, " +
                        "SUM(ポイント対象金額) AS 売上金額, " +
                        "SUM(付与ポイント) AS 買上ＭＫ付与ポイント数, " +
                        "0 AS 調整取引数, " +
                        "0 AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 40 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 < ? " +
                        "AND 精算年月日 between ? and ? " +
                        "AND 付与ポイント <> 0 " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                        "COUNT(*) AS 買上取引数, " +
                        "SUM(ポイント対象金額) AS 売上金額, " +
                        "SUM(付与ポイント) AS 買上ＭＫ付与ポイント数, " +
                        "0 AS 調整取引数, " +
                        "0 AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 40 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 < ? " +
                        "AND 精算年月日 between ? and ? " +
                        "AND 付与ポイント <> 0 " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                        "COUNT(*) AS 買上取引数, " +
                        "SUM(ポイント対象金額) AS 売上金額, " +
                        "SUM(付与ポイント) AS 買上ＭＫ付与ポイント数, " +
                        "0 AS 調整取引数, " +
                        "0 AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 40 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 >= ? " +
                        "AND 精算年月日 between ? and ? " +
                        "AND 付与ポイント <> 0 " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                        "COUNT(*) AS 買上取引数, " +
                        "SUM(ポイント対象金額) AS 売上金額, " +
                        "SUM(付与ポイント) AS 買上ＭＫ付与ポイント数, " +
                        "0 AS 調整取引数, " +
                        "0 AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 40 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 >= ? " +
                        "AND 精算年月日 between ? and ? " +
                        "AND 付与ポイント <> 0 " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                        "0 AS 買上取引数, " +
                        "0 AS 売上金額, " +
                        "0 AS 買上ＭＫ付与ポイント数, " +
                        "COUNT(*) AS 調整取引数, " +
                        "SUM(付与ポイント) AS 企業調整ＭＫ付与ポイント数, " +
                        "0 AS 顧客調整ＭＫ付与ポイント数, " +
                        "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                        "FROM cmuser.HSポイント日別情報%d " +
                        "WHERE MOD(理由コード, 100) = 41 " +
                        "AND 顧客番号 <> 9999999999999 " +
                        "AND 最終更新日 < ? " +
                        "AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ? " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                "0 AS 買上取引数, " +
                "0 AS 売上金額, " +
                "0 AS 買上ＭＫ付与ポイント数, " +
                "COUNT(*) AS 調整取引数, " +
                "SUM(付与ポイント) AS 企業調整ＭＫ付与ポイント数, " +
                "0 AS 顧客調整ＭＫ付与ポイント数, " +
                "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                "FROM cmuser.HSポイント日別情報%d " +
                "WHERE MOD(理由コード, 100) = 41 " +
                "AND 顧客番号 <> 9999999999999 " +
                "AND 最終更新日 < ? " +
                        "AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ? " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                "0 AS 買上取引数, " +
                "0 AS 売上金額, " +
                "0 AS 買上ＭＫ付与ポイント数, " +
                "COUNT(*) AS 調整取引数, " +
                "0 AS 企業調整ＭＫ付与ポイント数, " +
                "SUM(付与ポイント) AS 顧客調整ＭＫ付与ポイント数, " +
                "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                "FROM cmuser.HSポイント日別情報%d " +
                "WHERE MOD(理由コード, 100) = 42 " +
                "AND 顧客番号 <> 9999999999999 " +
                "AND 最終更新日 < ? " +
                "AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ? " +
                        "GROUP BY 店番号) " +

                        "UNION ALL (SELECT 店番号, " +
                "0 AS 買上取引数, " +
                "0 AS 売上金額, " +
                "0 AS 買上ＭＫ付与ポイント数, " +
                "COUNT(*) AS 調整取引数, " +
                "0 AS 企業調整ＭＫ付与ポイント数, " +
                "SUM(付与ポイント) AS 顧客調整ＭＫ付与ポイント数, " +
                "SUM(付与ポイント) AS 合計ＭＫ付与ポイント数 " +
                "FROM cmuser.HSポイント日別情報%d " +
                "WHERE MOD(理由コード, 100) = 42 " +
                "AND 顧客番号 <> 9999999999999 " +
                "AND 最終更新日 < ? " +
                "AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ? " +
                        "GROUP BY 店番号) " +
                        ") MK相互取引集計 " +
                "LEFT JOIN (SELECT 店番号 " +
                ",企業コード " +
                "FROM (SELECT /*+ INDEX(cmuser.PS店表示情報 PKPSSYSDSI00) */店番号 " +
                ",DECODE(店番号, '999001', 1000, '999002', 1000, 企業コード) AS 企業コード " +
                ",ROW_NUMBER() OVER (PARTITION BY 店番号 " +
                "ORDER BY 終了年月日 DESC) G_row " +
                "FROM cmuser.PS店表示情報 " +
                "WHERE 開始年月日 <= ?) PSST " +
                "WHERE G_row = 1) 店情報 " +
                "ON MK相互取引集計.店番号 = 店情報.店番号 " +
                "GROUP BY 店情報.企業コード, MK相互取引集計.店番号 " +
                "ORDER BY 店情報.企業コード, MK相互取引集計.店番号 "
                /* １．買上付与Ｐ */
                /* １－１．当月分 */
                , h_batdate_ym
                /* １－２．月跨ぎでの処理分 */
                , h_batdate_pym
                /* １－３．処理日当月処理分 */
                , h_batdate_nym
                /* １－４．前月集計漏れ分 */
                , h_batdate_ym
                /* １－５．前月集計漏れの月またぎ分 */
                , h_batdate_pym
                /* ２．企業調整Ｐ（ＰＣ） */
                /* ２－１．当月分 */
                , h_batdate_ym
                /* ２－２．処理部当月処理分 */
                , h_batdate_nym
                /* ３．顧客調整Ｐ（ＰＣ） */
                /* ３－１．当月分 */
                , h_batdate_ym
                /* ３－２．処理部当月処理分 */
                , h_batdate_nym
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPoint *** (ポイント集計情報作成)" +
                    "動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        sqlca.sql = str_sql;
        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_ins1 from:
//        str_sql;

        sqlca.prepare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPoint *** ASMK相互取引集計" +
                    " INSERT PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASMK相互取引集計", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPoint処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* INSERT文を実行する */
        sqlca.restAndExecute(h_batdate_ym,
                /* １－１～１－５．買上付与Ｐ */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ２－１～２－２．企業調整Ｐ（ＰＣ） */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* ３－１．～３－２．顧客調整Ｐ（ＰＣ） */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* 店情報 */
                h_batdate_ym_end_date);
////        EXEC SQL EXECUTE sql_stat_ins1 USING
////        :
//        h_batdate_ym,
//        /* １－１～１－５．買上付与Ｐ */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ２－１～２－２．企業調整Ｐ（ＰＣ） */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ３－１．～３－２．顧客調整Ｐ（ＰＣ） */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* 店情報 */
//         :h_batdate_ym_end_date;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPoint *** ASMK相互取引集計 " +
                    "INSERT sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "ASMK相互取引集計", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPoint処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 処理を終了する */
        return (C_const_OK);

    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTsgdkB_CreateMKPointFile                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTsgdkB_CreateMKPointFile()                       */
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

    /******************************************************************************/
    public int cmBTsgdkB_CreateMKPointFile() {

        int rtn_cd;                           /* 関数戻り値                   */
        StringDto fp_name = new StringDto(4096);                    /* 出力ファイルパス名           */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);        /* 動的SQLバッファ              */
        StringDto wk_buf1 = new StringDto(256);                     /* 編集バッファ                 */
        StringDto wk_buf2 = new StringDto(256);                     /* 編集バッファ                 */
        int buf_len;                          /* 編集用                       */
        IntegerDto sjis_len = new IntegerDto();                         /* SJIS変換用                   */
        StringDto utf8_buf = new StringDto(256);                    /* UTF8文字列格納領域           */
        StringDto sjis_buf = new StringDto(256);                    /* SJIS文字列格納領域           */

        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        memset(wk_buf2, 0x00, sizeof(wk_buf2));

        /* ポイント集計表ファイルオープン */
        sprintf(fp_name, "%s/%s", out_file_dir, pd_fl_name_sjis);

        if ((fp_pd = fopen(fp_name.arr, SystemConstant.Shift_JIS, FileOpenType.w)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ファイルオープン" +
                        "ERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /********************/
        /* 作成日の出力     */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len.arr = 0;
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s%s\r\n", C_P_DATE, h_system_date);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /********************/
        /* 作成時刻の出力   */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len.arr = 0;
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s%s\r\n", C_P_TIME,
                h_system_time);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /********************/
        /* ヘダー項目の出力 */
        /********************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len.arr = 0;
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        sprintf(utf8_buf, "%s\r\n", C_PD_HEADER);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_pd_cnt++;

        /******************************************/
        /* ASMK相互取引集計情報（店舗別）の取得   */
        /******************************************/
        sprintf(wk_sql,
                "SELECT "
                        + "ASMK相互取引集計.集計対象月, "
                        + "DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) AS 旧販社コード, "
                        + "ASMK相互取引集計.店番号, "
                        + "ASMK相互取引集計.買上取引数, "
                        + "ASMK相互取引集計.売上金額, "
                        + "ASMK相互取引集計.買上ＭＫ付与ポイント数, "
                        + "ASMK相互取引集計.調整取引数, "
                        + "ASMK相互取引集計.企業調整ＭＫ付与ポイント数, "
                        + "ASMK相互取引集計.顧客調整ＭＫ付与ポイント数, "
                        + "ASMK相互取引集計.合計ＭＫ付与ポイント数, "
                        + "(CASE DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END) AS 企業名, "
                        + "NVL(RPAD(PS店表示情報.漢字店舗名称,LENGTH(PS店表示情報.漢字店舗名称)), '%s') "
                        + "FROM "
                        + "ASMK相互取引集計 "
                        + "LEFT JOIN (SELECT "
                        + "店番号, "
                        + "漢字店舗名称 "
                        + "FROM "
                        + "(SELECT "
                        + "店番号, "
                        + "漢字店舗名称, "
                        + "ROW_NUMBER() OVER ( "
                        + "PARTITION BY 店番号 "
                        + "ORDER BY 終了年月日 DESC "
                        + ") G_row "
                        + "FROM "
                        + "PS店表示情報 "
                        + "WHERE "
                        + "開始年月日 <= ? "
                        + ") PSST "
                        + "WHERE G_row = 1 "
                        + ") PS店表示情報 "
                        + "ON ASMK相互取引集計.店番号 = PS店表示情報.店番号 "
                        + "WHERE "
                        + "ASMK相互取引集計.集計対象月 = ? "
                        + "ORDER BY "
                        + "ASMK相互取引集計.集計対象月, "
                        + "DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード), "
                        + "ASMK相互取引集計.店番号 "
                , C_nullSTR
                , C_nullSTR
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                    "(店舗別ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel1 from:
//        str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(店舗別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASMK相互取引集計(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_MKPNT_MISE CURSOR FOR sql_stat_sel1;
        sqlca.declare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(店舗別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "ASMK相互取引集計(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_MKPNT_MISE USING:
        sqlca.open(h_batdate_ymd, h_batdate_ym);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(店舗別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "ASMK相互取引集計(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {
            asmk_t = new AS_MKSOUGO_SHUKEI_INFO_TBL();
            memset(asmk_t, 0x00, sizeof(asmk_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

            sqlca.fetchInto(asmk_t.shukei_ymd,
                    asmk_t.kyu_hansya_cd,
                    asmk_t.mise_no,
                    asmk_t.kaiage_cnt,
                    asmk_t.sales,
                    asmk_t.kaiage_mk_point,
                    asmk_t.chousei_hikitori,
                    asmk_t.kigyou_mkp,
                    asmk_t.kokyaku_mkp,
                    asmk_t.total_mkp,
                    h_kyu_hansya_name, h_mise_kanji);


            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                                "ASMK相互取引集計(店舗別) FETCH sqlcode=[%d]\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "ASMK相互取引集計(店舗別集計)", log_format_buf,
                        0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_MKPNT_MISE;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                                    "ASMK相互取引集計(店舗別) FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント集計表に出力           */
            /*--------------------------------*/
            /************/
            /* 企業ＣＤ */
            /************/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf(wk_buf1, "\"%d\",\"", asmk_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /************/
            /* 企業名   */
            /************/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name, strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name, C_nullSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }

            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len.arr = 0;
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name);
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /************/
            /* 店舗ＣＤ */
            /************/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf(wk_buf2, "\",\"%d\",\"", asmk_t.mise_no);

            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /************/
            /* 店舗名   */
            /************/
            /*  店舗名の末尾のスペース削除 */
            BT_Rtrim(h_mise_kanji, strlen(h_mise_kanji));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_mise_kanji, C_nullSTR) == 0) {
                strcpy(h_mise_kanji, "");
            }

            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len.arr = 0;
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_mise_kanji);
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /****************************************/
            /* 買上引取数～合計ＭＫ付与ポイント数   */
            /****************************************/
            sprintf(wk_buf2,
                    "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n",
                    asmk_t.kaiage_cnt,
                    asmk_t.sales,
                    asmk_t.kaiage_mk_point,
                    asmk_t.chousei_hikitori,
                    asmk_t.kigyou_mkp,
                    asmk_t.kokyaku_mkp,
                    asmk_t.total_mkp
            );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* ファイル出力   */
            /*----------------*/
            fwrite(wk_buf1, buf_len, 1, fp_pd);
            rtn_cd = ferror(fp_pd);
            if (rtn_cd != C_const_OK) {

                /* ファイル書き込みエラー */
                sprintf(log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_MKPNT_MISE;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pd_cnt++;

        } /* LOOP END */

        /* カーソルクローズ */
        sqlca.curse_close();
//        EXEC SQL CLOSE CUR_MKPNT_MISE;

        /******************************************/
        /*  ASMK相互取引集計情報（企業別）の取得  */
        /******************************************/
        sprintf(wk_sql,
                " SELECT "
                        + "ASMK相互取引集計.集計対象月, "
                        + " DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) AS 旧販社コード, "
                        + "SUM(ASMK相互取引集計.買上取引数), "
                        + "SUM(ASMK相互取引集計.売上金額), "
                        + "SUM(ASMK相互取引集計.買上ＭＫ付与ポイント数), "
                        + "SUM(ASMK相互取引集計.調整取引数), "
                        + "SUM(ASMK相互取引集計.企業調整ＭＫ付与ポイント数), "
                        + "SUM(ASMK相互取引集計.顧客調整ＭＫ付与ポイント数), "
                        + "SUM(ASMK相互取引集計.合計ＭＫ付与ポイント数), "
                        + "(CASE DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1040 THEN 'CF' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END)  AS 企業名 "
                        + " FROM "
                        + " ASMK相互取引集計 "
                        + " WHERE "
                        + " ASMK相互取引集計.集計対象月  = ? "
                        + " GROUP BY "
                        + " ASMK相互取引集計.集計対象月, "
                        + " DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) "
                        + " ORDER BY "
                        + " ASMK相互取引集計.集計対象月, "
                        + " DECODE(ASMK相互取引集計.旧販社コード,'1040',1010,ASMK相互取引集計.旧販社コード) "
                , C_nullSTR
        );


        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                    "(企業別ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        memset(str_sql, 0x00, sizeof(str_sql));

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel2 from:
//        str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(企業別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASMK相互取引集計(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_MKPNT_HANSHA CURSOR FOR sql_stat_sel2;
        sqlca.declare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(企業別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "ASMK相互取引集計(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_MKPNT_HANSHA USING:
//        h_batdate_ym;
        sqlca.open(h_batdate_ym);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                    "(企業別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "ASMK相互取引集計(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {
            memset(asmk_t, 0x00, sizeof(asmk_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

            sqlca.fetchInto(asmk_t.shukei_ymd,
                    asmk_t.kyu_hansya_cd,
                    asmk_t.kaiage_cnt,
                    asmk_t.sales,
                    asmk_t.kaiage_mk_point,
                    asmk_t.chousei_hikitori,
                    asmk_t.kigyou_mkp,
                    asmk_t.kokyaku_mkp,
                    asmk_t.total_mkp,
                    h_kyu_hansya_name);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** ASMK相互取引集計" +
                        "(企業別) FETCH sqlcode=[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "ASMK相互取引集計(企業別集計)", log_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_MKPNT_HANSHA;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                                    "ASMK相互取引集計(企業別) FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント集計表に出力(企業別)   */
            /*--------------------------------*/
            /************/
            /* 企業ＣＤ */
            /************/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf(wk_buf1, "\"%d\",\"", asmk_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /************/
            /* 企業名   */
            /************/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name, strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name, C_nullSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }
            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len.arr = 0;
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name);
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        , (long) rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /************/
            /* 店舗ＣＤ */
            /************/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf(wk_buf2, "\",\"%d\",\"", C_MISENO);
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /************/
            /* 店舗名   */
            /************/
            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len.arr = 0;
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, C_MISENAME_HANSHA);
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /****************************************/
            /* 買上引取数～合計ＭＫ付与ポイント数   */
            /****************************************/
            sprintf(wk_buf2,
                    "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n",
                    asmk_t.kaiage_cnt,
                    asmk_t.sales,
                    asmk_t.kaiage_mk_point,
                    asmk_t.chousei_hikitori,
                    asmk_t.kigyou_mkp,
                    asmk_t.kokyaku_mkp,
                    asmk_t.total_mkp
            );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* ファイル出力   */
            /*----------------*/
            fwrite(wk_buf1, buf_len, 1, fp_pd);
            rtn_cd = ferror(fp_pd);
            if (rtn_cd != C_const_OK) {

                /* ファイル書き込みエラー */
                sprintf(log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

                /* カーソルクローズ */
                sqlca.curse_close();
//                EXEC SQL CLOSE CUR_MKPNT_HANSHA;
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pd_cnt++;
        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_MKPNT_HANSHA;
        sqlca.curse_close();

        /****************************************/
        /* ASMK相互取引集計（全社）の取得 */
        /****************************************/

        memset(asmk_t, 0x00, sizeof(asmk_t));
        sqlca.sql.arr = "SELECT" +
                "        ASMK相互取引集計.集計対象月," +
                "                SUM(ASMK相互取引集計.買上取引数)," +
                "                SUM(ASMK相互取引集計.売上金額)," +
                "                SUM(ASMK相互取引集計.買上ＭＫ付与ポイント数)," +
                "                SUM(ASMK相互取引集計.調整取引数)," +
                "                SUM(ASMK相互取引集計.企業調整ＭＫ付与ポイント数)," +
                "                SUM(ASMK相互取引集計.顧客調整ＭＫ付与ポイント数)," +
                "                SUM(ASMK相互取引集計.合計ＭＫ付与ポイント数)"
                + " FROM" +
                "        ASMK相互取引集計" +
                "                WHERE" +
                "        ASMK相互取引集計.集計対象月 = ?" +
                "        GROUP BY" +
                "        ASMK相互取引集計.集計対象月" +
                "        ORDER BY" +
                "        ASMK相互取引集計.集計対象月";
//        EXEC SQL SELECT
//        ASMK相互取引集計.集計対象月,
//                SUM(ASMK相互取引集計.買上取引数),
//                SUM(ASMK相互取引集計.売上金額),
//                SUM(ASMK相互取引集計.買上ＭＫ付与ポイント数),
//                SUM(ASMK相互取引集計.調整取引数),
//                SUM(ASMK相互取引集計.企業調整ＭＫ付与ポイント数),
//                SUM(ASMK相互取引集計.顧客調整ＭＫ付与ポイント数),
//                SUM(ASMK相互取引集計.合計ＭＫ付与ポイント数)
//        INTO:
//             asmk_t.shukei_ymd,
//            :asmk_t.kaiage_cnt,
//            :asmk_t.sales,
//            :asmk_t.kaiage_mk_point,
//            :asmk_t.chousei_hikitori,
//            :asmk_t.kigyou_mkp,
//            :asmk_t.kokyaku_mkp,
//            :asmk_t.total_mkp
//                FROM
//        ASMK相互取引集計
//                WHERE
//        ASMK相互取引集計.集計対象月 = :h_batdate_ym
//        GROUP BY
//        ASMK相互取引集計.集計対象月
//        ORDER BY
//        ASMK相互取引集計.集計対象月;
        sqlca.restAndExecute(h_batdate_ym);
        sqlca.fetchInto(asmk_t.shukei_ymd,
                asmk_t.kaiage_cnt,
                asmk_t.sales,
                asmk_t.kaiage_mk_point,
                asmk_t.chousei_hikitori,
                asmk_t.kigyou_mkp,
                asmk_t.kokyaku_mkp,
                asmk_t.total_mkp);
        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTsgdkB_CreateMKPointFile *** " +
                            "ASMK相互取引集計(全社) SELECT sqlcode=[%d]\n",
                    sqlca.sqlcode);
            /*--------------------------------------------------------------------*/
        }
        /* データ無し以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "ASMK相互取引集計(全社集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        /*--------------------------------*/
        /* ポイント集計表に出力(全社)     */
        /*--------------------------------*/
        /* 会社名(,店舗名)のUTF8→SJIS変換(共通関数)   */
        sjis_len.arr = 0;
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        strcpy(utf8_buf, C_MISENAME_ZENSHA);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        /************/
        /* 企業ＣＤ */
        /************/
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        sprintf(wk_buf1, "\"%d\",\"", C_KAISHACD);
        buf_len = strlen(wk_buf1);

        /************/
        /* 企業名   */
        /************/
        memcpy(wk_buf1, sjis_buf, sjis_len.arr);
        buf_len += sjis_len.arr;

        /************/
        /* 店舗ＣＤ */
        /************/
        sprintf(wk_buf2, "\",\"%d\",\"", C_MISENO);
        memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
        buf_len += strlen(wk_buf2);

        /************/
        /* 店舗名   */
        /************/
        memcpy(wk_buf1, sjis_buf, sjis_len.arr);
        buf_len += sjis_len.arr;

        /****************************************/
        /* 買上引取数～合計ＭＫ付与ポイント数   */
        /****************************************/
        sprintf(wk_buf2,
                "\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\r\n",
                asmk_t.kaiage_cnt,
                asmk_t.sales,
                asmk_t.kaiage_mk_point,
                asmk_t.chousei_hikitori,
                asmk_t.kigyou_mkp,
                asmk_t.kokyaku_mkp,
                asmk_t.total_mkp
        );
        memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
        buf_len += strlen(wk_buf2);

        /*----------------*/
        /* ファイル出力   */
        /*----------------*/
        fwrite(wk_buf1, buf_len, 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {

            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTsgdkB_CreateMKPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_pd_cnt++;

        /* 出力ファイルクローズ */
        fclose(fp_pd);

        /* 処理を終了する */
        return (C_const_OK);
    }

}
