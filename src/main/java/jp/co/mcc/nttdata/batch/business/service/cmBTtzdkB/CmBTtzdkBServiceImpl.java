package jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB.dto.AS_POINT_TICKET_SHUKEI_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB.dto.AS_TUJYO_POINT_SHUKEI_INFO_TBL;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/* ******************************************************************************
 *   プログラム名   ： 通常ポイント集計（cmBTtzdkB.pc）
 *
 *   【処理概要】
 *     ポイントの残高や収支（発生、利用）を管理するための会計データを作成する。
 *     ・月次の会計向け通常ポイント集計情報として、
 *       店舗単位に各ポイント数を集計し通常ポイント集計表に出力する。
 *     ・月次の会計向けポイント券集計情報として、
 *       店舗単位に各ポイント券数を集計しポイント券集計表に出力する。
 *
 *   【引数説明】
 *     -d処理日付           :（任意）処理日付
 *     -DEBUG(-debug)       :（任意）デバッグモードでの実行
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
 *     30.00 :  2021/04/05 SSI.上野：初版
 *     31.00 :  2021/05/13 SSI.上野：CF-503　クリアＰ数をマイナス値で出力
 *
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 ***************************************************************************** */
@Service
public class CmBTtzdkBServiceImpl extends CmABfuncLServiceImpl implements CmBTtzdkBService {

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */


    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE  AS_TUJYO_POINT_SHUKEI_INFO.h;  /* AS通常ポイント集計情報    */
//    EXEC SQL INCLUDE  AS_POINT_TICKET_SHUKEI_INFO.h; /* ASポイント券集計情報      */

    AS_TUJYO_POINT_SHUKEI_INFO_TBL aspnt_t = new AS_TUJYO_POINT_SHUKEI_INFO_TBL();       /* AS通常ポイント集計情報    */
    AS_POINT_TICKET_SHUKEI_INFO_TBL asptk_t = new AS_POINT_TICKET_SHUKEI_INFO_TBL();       /* ASポイント券集計情報      */

    ItemDto h_kyu_hansya_name = new ItemDto(30 * 3 + 1);         /* 旧販社名                  */
    ItemDto h_mise_kanji = new ItemDto(40 * 3 + 1);              /* 漢字店舗名称              */
    ItemDto h_batdate_ymd = new ItemDto();                     /* バッチ処理日付(当日)      */
    ItemDto h_batdate_ymd_prev = new ItemDto();                /* バッチ処理日付(前月)      */
    ItemDto hs_batdate_ymd = new ItemDto(9);                 /* バッチ処理日付(当日)      */
    ItemDto h_batdate_ym = new ItemDto();                      /* 集計対象月                */
    ItemDto hs_batdate_ym = new ItemDto(7);                  /* 集計対象月                */
    ItemDto h_batdate_ym_start_date = new ItemDto();           /* 集計対象月初日            */
    ItemDto h_batdate_ym_end_date = new ItemDto();             /* 集計対象月末日            */
    ItemDto h_batdate_pym_start_date = new ItemDto();          /* 集計対象前月初日          */
    ItemDto h_batdate_pym_end_date = new ItemDto();            /* 集計対象前月末日          */
    ItemDto h_batdate_nym_start_date = new ItemDto();          /* 集計対象翌月初日          */
    ItemDto h_batdate_pym = new ItemDto();                     /* 集計対象月の前月          */
    ItemDto hs_batdate_pym = new ItemDto(7);                 /* 集計対象月の前月          */
    ItemDto h_batdate_nym = new ItemDto();                     /* 集計対象月の翌月          */
    ItemDto hs_batdate_nym = new ItemDto(7);                 /* 集計対象月の翌月          */
    ItemDto h_batdate_nendo = new ItemDto();                   /* 集計対象月の年度          */
    ItemDto h_batdate_pnendo = new ItemDto();                  /* 集計対象月の前年度        */
    ItemDto h_batdate_nnendo = new ItemDto();                  /* 集計対象月の翌年度        */
    StringDto str_sql = new StringDto(4096 * 18);                  /* 実行用SQL文字列１         */
    ItemDto h_program_id = new ItemDto(20 + 1);                /* プログラムID              */
    ItemDto h_system_date = new ItemDto(9);                  /* 作成日                    */
    ItemDto h_system_time = new ItemDto(9);                  /* 作成時刻                  */
    ItemDto h_tujo_bit_zennen = new ItemDto();        /* （前年度）通常Ｐ失効フラグの基準値 */
    ItemDto h_tujo_bit_tonen = new ItemDto();         /* （当年度）通常Ｐ失効フラグの基準値 */

//    EXEC SQL END DECLARE SECTION;

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0; /* OFF                                */
    int DEF_ON = 1;     /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_D = "-d";         /* 処理日付                           */
    String DEF_DEBUG = "-DEBUG";         /* デバッグスイッチ                   */
    String DEF_debug = "-debug";         /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "通常ポイント集計";         /* APログ用機能名             */
    String C_FNAME_PD = "通常ポイント集計表_";   /* ポイント集計表ファイル名   */
    String C_FNAME_PT = "ポイント券集計表_";    /* ポイント券集計表ファイル名 */
    String C_P_DATE = "作成日    ";         /* 会計帳票出力用             */
    String C_P_TIME = "作成時刻  ";        /* 会計帳票出力用             */

    String C_PD_HEADER = "\"企業ＣＤ\",\"企業名\",\"店舗ＣＤ\",\"店舗名\",\"月初Ｐ残高\",\"月初Ｐ残高前年\",\"月初Ｐ残高当年\",\"買上付与Ｐ数\",\"ベビー＆サークル付与Ｐ数\",\"買上回数付与Ｐ数\",\"watashi+連携付与Ｐ数\",\"アプリＰ交換Ｐ数\",\"カタリナＷＥＢ付与Ｐ数\",\"その他付与Ｐ数\",\"調整付与Ｐ数\",\"付与Ｐ合計\",\"値引Ｐ数\",\"商品交換Ｐ数\",\"募金Ｐ数\",\"使用Ｐ合計\",\"累計調整Ｐ\",\"クリアＰ数\",\"クリアＰ数顧客退会前年\",\"クリアＰ数顧客退会当年\",\"クリアＰ数有効期限\",\"月末Ｐ残高\",\"月末Ｐ残高前年\",\"月末Ｐ残高当年\"";
    String C_PT_HEADER = "\"企業ＣＤ\",\"企業名\",\"店舗ＣＤ\",\"店舗名\",\"値引Ｐ数\",\"値引券実値引額\"";

    String C_MISENAME_ZENSHA = "全社計";            /* 店舗名(全社集計)           */
    String C_MISENAME_HANSHA = "企業計";            /* 店舗名(企業別集計)         */
    int C_MISENO = 9999;                   /* 店舗ＣＤ(企業別/全社集計)  */
    int C_KAISHACD = 99;                   /* 会社コード(全社集計)       */
    String C_NULLSTR = "NULL";                      /* 空文字置換文字列           */
    String C_COLNAME_ZANNEN = "SUM(月末Ｐ残高前年)";  /* 月初Ｐ項目(前年)           */
    String C_COLNAME_TONEN = "SUM(月末Ｐ残高当年)"; /* 月初Ｐ項目(当年)           */

    /*-----------bit演算用----------------------*/
    int TUJO_BASE_BIT = 0x01;   /* 通常Ｐ失効フラグベース(00001)     */
    int TUJO_CHENGE_BIT = 0x20;   /* 通常Ｐ失効フラグ固定箇所（100000) */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int arg_d_chk;            /* 引数dチェック用                        */
    long g_pd_cnt;             /* ファイル出力件数(通常ポイント集計表)   */
    long g_pt_cnt;             /* ファイル出力件数(ポイント券集計表)     */
    StringDto out_file_dir = new StringDto(4096);   /* ワークディレクトリ                     */
    StringDto pd_fl_name = new StringDto(45);       /* 出力ファイル名(通常ポイント集計表)     */
    StringDto pt_fl_name = new StringDto(45);       /* 出力ファイル名(ポイント券集計表)       */
    StringDto pd_fl_name_sjis = new StringDto(45);  /* 出力ファイル名(通常ポイント集計表)     */
    StringDto pt_fl_name_sjis = new StringDto(45);  /* 出力ファイル名(ポイント券集計表)       */
    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                  */
    FileStatusDto fp_pd;           /* 出力ファイルポインタ(通常ポイント集計表)   */
    FileStatusDto fp_pt;           /* 出力ファイルポインタ(ポイント券集計表)     */


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
    /* **************************************************************************** */
    public MainResultDto main(int argc, String[] argv) {

        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                   */
        String env_wrk = null;                       /* 環境変数取得用                 */
        StringDto bat_date_ymd = new StringDto(9);                /* バッチ処理年月日(一時格納)     */
        StringDto wk_buf = new StringDto(256);                    /* 出力ファイル名SJIS変換用       */
        int ret;
        IntegerDto len;

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        /* 変数初期化 */
        g_pd_cnt = 0;
        g_pt_cnt = 0;
        h_batdate_ymd.arr = 0;
        h_batdate_ymd_prev.arr = 0;
        h_batdate_ym.arr = 0;
        h_batdate_ym_start_date.arr = 0;
        h_batdate_ym_end_date.arr = 0;
        h_batdate_pym_start_date.arr = 0;
        h_batdate_pym_end_date.arr = 0;
        h_batdate_nym_start_date.arr = 0;
        h_batdate_pym.arr = 0;
        h_batdate_nym.arr = 0;
        h_batdate_nendo.arr = 0;
        h_batdate_pnendo.arr = 0;
        h_batdate_nnendo.arr = 0;
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        memset(pd_fl_name, 0x00, sizeof(pd_fl_name));
        memset(pt_fl_name, 0x00, sizeof(pt_fl_name));
        memset(pd_fl_name_sjis, 0x00, sizeof(pd_fl_name_sjis));
        memset(pt_fl_name_sjis, 0x00, sizeof(pt_fl_name_sjis));
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
            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;

                /* 引数の文字列が "-d" で始まる場合 */
            } else if (0 == memcmp(arg_Work1, DEF_ARG_D, 2)) {
                memset(log_format_buf, 0x00, sizeof(log_format_buf));
                /* パラメータのチェックをする */
                rtn_cd = cmBTtzdkB_ChkArgdInf(arg_Work1);
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
        /* バッチ処理日付を取得（当日）        */
        /*-------------------------------------*/
        if (StringUtils.isEmpty(hs_batdate_ymd.strVal())) {

            //        rtn_status = 0;
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
                "        TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE(?, 'YYYYMMDD'), -1), 'YYYYMMDD'))       " +
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

            sprintf(log_format_buf, "バッチ処理日付 = %s\n", hs_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "DUAL", log_format_buf, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /* 集計対象日付(年度)を取得            */
        /*-------------------------------------*/
        /* 集計対象月の年度 */
        if (h_batdate_ym.intVal() % 100 <= 3) {
            h_batdate_nendo.arr = (h_batdate_ym.intVal() / 100) - 1;
        } else {
            h_batdate_nendo.arr = (h_batdate_ym.intVal() / 100);
        }
        /* 集計対象月の前年度 */
        h_batdate_pnendo.arr = h_batdate_nendo.intVal() - 1;
        /* 集計対象月の翌年度 */
        h_batdate_nnendo.arr = h_batdate_nendo.intVal() + 1;

        /*-------------------------------------*/
        /* 集計対象翌月初日を取得              */
        /*-------------------------------------*/
        h_batdate_nym_start_date.arr = (h_batdate_nym.intVal() * 100) + 1;

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
            C_DbgMsg("*** main *** 集計対象日付(翌月初日)=[%d]\n",
                    h_batdate_nym_start_date);
            C_DbgMsg("*** main *** 集計対象月当年度=[%d]\n", h_batdate_nendo);
            C_DbgMsg("*** main *** 集計対象月前年度=[%d]\n", h_batdate_pnendo);
            C_DbgMsg("*** main *** 集計対象月翌年度=[%d]\n", h_batdate_nnendo);
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 出力ファイル名                      */
        /*-------------------------------------*/
        sprintf(pd_fl_name, "%s%d.csv", C_FNAME_PD, h_batdate_ym);
        sprintf(pt_fl_name, "%s%d.csv", C_FNAME_PT, h_batdate_ym);

        /* 通常ポイント集計表:ファイル名をSJISに変換 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        len = new IntegerDto();
        ret = C_ConvUT2SJ(pd_fl_name, strlen(pd_fl_name), wk_buf, len);
        if (ret != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 通常ポイント集計表ファイル名変換エラー = %d\n",
                        ret);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", ret, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        } else {
            /* 通常ポイント集計表ファイル名（SJIS） */
            sprintf(pd_fl_name_sjis, "%s", wk_buf);
        }

        /* ポイント券集計表:ファイル名をSJISに変換 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        len = new IntegerDto();
        ret = C_ConvUT2SJ(pt_fl_name, strlen(pt_fl_name), wk_buf, len);
        if (ret != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** ポイント券集計表ファイル名変換エラー = %d\n",
                        ret);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", ret, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        } else {
            /* ポイント券集計表ファイル名（SJIS） */
            sprintf(pt_fl_name_sjis, "%s", wk_buf);
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
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "作成日の取得に失敗しました", 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_NG);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/

        /*-------------------------*/
        /*  集計前処理             */
        /*-------------------------*/
        rtn_cd = cmBTtzdkB_PreTotal();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTtzdkB_PreTotal NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "集計前処理に失敗しました", 0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit(C_const_APNG);
        }
//        EXEC SQL COMMIT WORK;
        sqlca.commit();

        /*--------------------------------*/
        /*  通常ポイント集計情報作成処理  */
        /*--------------------------------*/
        rtn_cd = cmBTtzdkB_CreateTujyoPoint();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTtzdkB_CreateTujyoPoint NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "通常ポイント集計情報作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit(C_const_APNG);
        }

        /*--------------------------------*/
        /*  通常ポイント集計表作成処理    */
        /*--------------------------------*/
        rtn_cd = cmBTtzdkB_CreateTujyoPointFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTtzdkB_CreateTujyoPointFile NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "通常ポイント集計表作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            /* 出力ファイルクローズ */
            fclose(fp_pd);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit(C_const_APNG);
        }

        /*--------------------------------*/
        /*  ポイント券集計情報作成処理    */
        /*--------------------------------*/
        rtn_cd = cmBTtzdkB_CreatePointTicket();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTtzdkB_CreatePointTicket NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "ポイント券集計情報作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
            return exit(C_const_APNG);
        }

        /*------------------------------*/
        /*  ポイント券集計表作成処理    */
        /*------------------------------*/
        rtn_cd = cmBTtzdkB_CreatePointTicketFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTtzdkB_CreatePointTicketFile NG rtn=[%d]\n",
                        rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "ポイント券集計表作成処理に失敗しました",
                    0, 0, 0, 0, 0);

            /* 出力ファイルクローズ */
            fclose(fp_pt);
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();
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
        sprintf(log_format_buf, "%s/%s", out_file_dir, pt_fl_name);
        APLOG_WT("105", 0, null, log_format_buf, g_pt_cnt,
                0, 0, 0, 0);

        /* 終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        /* コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit(C_const_APOK);
    }


    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTtzdkB_ChkArgdInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTtzdkB_ChkArgdInf(char *Arg_in)                            */
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
    /* **************************************************************************** */
    public int cmBTtzdkB_ChkArgdInf(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_ChkArgdInf処理 ***");
            /*---------------------------------------------------------------------*/
        }
        /*-------------------------------------*/
        /*  重複指定チェック                   */
        /*-------------------------------------*/
        if (arg_d_chk != DEF_OFF) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_ChkArgdInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数が複数設定されています（%s）", Arg_in);
            return (C_const_NG);
        }
        arg_d_chk = DEF_ON;

        /*-------------------------------------*/
        /*  設定値nullチェック                 */
        /*-------------------------------------*/
        if (StringUtils.isEmpty(Arg_in.arr.substring(2))) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_ChkArgdInf *** 設定値null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }
            sprintf(log_format_buf, "-d 引数の値が不正です（%s）", Arg_in);
            return (C_const_NG);
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("*** cmBTtzdkB_ChkArgdInf処理 ***", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ：   cmBTtzdkB_GetYearCd                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*      void    cmBTtzdkB_GetYearCd(int year, char *year_cd)                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数の年度コードを取得する                                    */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              int     year        ：年度                                    */
    /*              char    *year_cd    ：年度コード                              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /* *****************************************************************************/
    public void cmBTtzdkB_GetYearCd(int year, StringDto year_cd) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_GetYearCd処理 ***");
            /*---------------------------------------------------------------------*/
        }
        /* 年度コードを算出 */
        switch (year % 5) {
            case 1:
                strcpy(year_cd, "１");
                break;
            case 2:
                strcpy(year_cd, "２");
                break;
            case 3:
                strcpy(year_cd, "３");
                break;
            case 4:
                strcpy(year_cd, "４");
                break;
            case 0:
                strcpy(year_cd, "０");
                break;
        }
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("*** cmBTtzdkB_GetYearCd処理 ***", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTtzdkB_GetTujyoBit                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_GetTujyoBit()                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          対象年度より通常ポイント失効フラグのチェック値を取得する。        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              int     year        ：年度                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              int     tujo_and_bit：通常ポイント失効フラグのチェック値      */
    /*                                                                            */
    /* *****************************************************************************/
    public int cmBTtzdkB_GetTujyoBit(int nendo) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_GetTujyoBit処理 ***");
            /*---------------------------------------------------------------------*/
        }
        int check_flg;           /* bit演算用フラグ                */
        int tujo_and_bit;        /* ビットＡＮＤ（通常Ｐ）         */
        int tujo_xor_bit;        /* ビットＸＯＲ（通常Ｐ）         */

        check_flg = 0;
        tujo_and_bit = 0;
        tujo_xor_bit = 0;

        /* ビットＸＯＲ演算時計算値*/
        check_flg = TUJO_BASE_BIT;
        tujo_xor_bit = (check_flg << (4 - (nendo % 5)));

        /* ビットＡＮＤ演算時比較値*/
        tujo_and_bit = (tujo_xor_bit | TUJO_CHENGE_BIT);

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_GetTujyoBit *** 年度=[%d]\n", nendo);
            C_DbgMsg("*** cmBTtzdkB_GetTujyoBit *** チェック値=[%d]\n", tujo_and_bit);
            C_DbgEnd("*** cmBTtzdkB_GetTujyoBit処理 ***", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }
        return (tujo_and_bit);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTtzdkB_PreTotal                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_PreTotal()                                */
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
    public int cmBTtzdkB_PreTotal() {
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_PreTotal処理 ***");
            /*------------------------------------------------------------------------*/
        }

        /* 利用可能ポイント情報の更新 */
//        EXEC SQL UPDATE TS利用可能ポイント情報
//        SET 利用可能通常Ｐ０ = 0,
//                利用可能通常Ｐ１ = 0,
//                利用可能通常Ｐ２ = 0,
//                利用可能通常Ｐ３ = 0,
//                利用可能通常Ｐ４ = 0,
//                通常Ｐ失効フラグ = 32,
//        最終買上日 = :h_batdate_ymd,
//            最終更新日 = :h_batdate_ymd,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :h_program_id
//        WHERE 顧客番号 = 9999999999999;

        sqlca.sql = new StringDto("UPDATE TS利用可能ポイント情報" +
                "        SET 利用可能通常Ｐ０ = 0," +
                "                利用可能通常Ｐ１ = 0," +
                "                利用可能通常Ｐ２ = 0," +
                "                利用可能通常Ｐ３ = 0," +
                "                利用可能通常Ｐ４ = 0," +
                "                通常Ｐ失効フラグ = 32," +
                "        最終買上日 = ?," +
                "            最終更新日 = ?," +
                "            最終更新日時 = SYSDATE()," +
                "            最終更新プログラムＩＤ = ?" +
                "        WHERE 顧客番号 = 9999999999999");
        sqlca.restAndExecute(h_batdate_ymd, h_batdate_ymd, h_program_id);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_PreTotal *** TS利用可能ポイント情報 UPDATE " +
                    "sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "顧客番号 = 9999999999999");
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_PreTotal処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ポイント累計情報の更新 */
//        EXEC SQL UPDATE TSポイント累計情報
//        SET 累計付与ポイント = 0,
//                累計利用ポイント = 0,
//                累計基本Ｐ率対象ポイント = 0,
//                累計ランクＵＰ対象金額 = 0,
//                累計ポイント対象金額 = 0,
//                累計買上額 = 0,
//                累計買上回数 = 0,
//                累計買上日数 = 0,
//                累計付与通常Ｐ = 0,
//                累計利用通常Ｐ = 0,
//        最終更新日 = :h_batdate_ymd,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :h_program_id
//        WHERE 顧客番号 = 9999999999999;

        sqlca.sql = new StringDto("UPDATE TSポイント累計情報 " +
                "SET 累計付与ポイント = 0," +
                "        累計利用ポイント = 0," +
                "        累計基本Ｐ率対象ポイント = 0," +
                "        累計ランクＵＰ対象金額 = 0," +
                "        累計ポイント対象金額 = 0," +
                "        累計買上額 = 0," +
                "        累計買上回数 = 0," +
                "        累計買上日数 = 0," +
                "        累計付与通常Ｐ = 0," +
                "        累計利用通常Ｐ = 0," +
                "最終更新日 = ?," +
                "    最終更新日時 = SYSDATE()," +
                "    最終更新プログラムＩＤ = ?" +
                "WHERE 顧客番号 = 9999999999999");
        sqlca.restAndExecute(h_batdate_ymd, h_program_id);

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "顧客番号 = 9999999999999");
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSポイント累計情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_PreTotal処理", C_const_NG, 0, 0);
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
    /*  関数名 ： cmBTtzdkB_CreateTujyoPoint                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_CreateTujyoPoint()                        */
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
    /* *****************************************************************************/
    public int cmBTtzdkB_CreateTujyoPoint() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen * 18);       /* 動的SQLバッファ            */
        StringDto year_cd_zennen = new StringDto(4);                  /* 前年度コード（全角）       */
        StringDto year_cd_tonen = new StringDto(4);                   /* 当年度コード（全角）       */
        StringDto col_nm_zennen = new StringDto(64);                  /* 前年度項目名（全角）       */
        StringDto col_nm_tonen = new StringDto(64);                   /* 前年度項目名（全角）       */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_CreateTujyoPoint処理 ***");
            /*------------------------------------------------------------------------*/
        }

        h_tujo_bit_zennen.arr = 0;
        h_tujo_bit_tonen.arr = 0;
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(year_cd_zennen, 0x00, sizeof(year_cd_zennen));
        memset(year_cd_tonen, 0x00, sizeof(year_cd_tonen));
        memset(col_nm_zennen, 0x00, sizeof(col_nm_zennen));
        memset(col_nm_tonen, 0x00, sizeof(col_nm_tonen));

        /*-------------------*/
        /* 年度コード取得    */
        /*-------------------*/
        cmBTtzdkB_GetYearCd(h_batdate_nendo.intVal(), year_cd_tonen);
        cmBTtzdkB_GetYearCd(h_batdate_nendo.intVal() - 1, year_cd_zennen);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 当年度コード=[%s]\n",
                    year_cd_tonen);
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 前年度コード=[%s]\n",
                    year_cd_zennen);
            /*------------------------------------------------------------*/
        }

        /*-----------------------------------------*/
        /* 通常ポイント失効フラグチェック値取得    */
        /*-----------------------------------------*/
        h_tujo_bit_zennen.arr = cmBTtzdkB_GetTujyoBit(h_batdate_nendo.intVal() - 1);
        h_tujo_bit_tonen.arr = cmBTtzdkB_GetTujyoBit(h_batdate_nendo.intVal());
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 当年度基準値=[%d]\n",
                    h_tujo_bit_tonen);
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 前年度基準値=[%d]\n",
                    h_tujo_bit_zennen);
            /*------------------------------------------------------------*/
        }

        /*-------------------*/
        /* 月初Ｐ項目名取得  */
        /*-------------------*/
        if (h_batdate_ym.intVal() % 100 == 4 && h_batdate_ym.intVal() / 100 != 2021) {
            /* 年度替わり:2021年4月以外 */
            strcpy(col_nm_zennen, C_COLNAME_TONEN);
            strcpy(col_nm_tonen, "0");
        } else {
            /* 2021年4月の場合は当年・前年を取得 */
            strcpy(col_nm_zennen, C_COLNAME_ZANNEN);
            strcpy(col_nm_tonen, C_COLNAME_TONEN);
        }


        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 当年度コード=[%s]\n",
                    year_cd_tonen);
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** 前年度コード=[%s]\n",
                    year_cd_zennen);
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------------------------*/
        /* AS通常ポイント集計情報に                              */
        /* HSポイント日別情報より店舗毎に集計したデータを登録    */
        /*-------------------------------------------------------*/
        StringBuilder builder=new StringBuilder();
        builder.append("INSERT INTO AS通常ポイント集計情報");
        builder.append(" (");
        builder.append(" 集計対象月,");
        builder.append(" 旧販社コード,");
        builder.append(" 店番号,");
        builder.append(" 月初Ｐ残高,");
        builder.append(" 月初Ｐ残高前年,");
        builder.append(" 月初Ｐ残高当年,");
        builder.append(" 買上Ｐ数,");
        builder.append(" 買上回数Ｐ数,");
        builder.append(" ベビーサークル,");
        builder.append(" ワタシプラス連携付与Ｐ数,");
        builder.append(" アプリＰ交換Ｐ数,");
        builder.append(" カタリナ付与Ｐ数,");
        builder.append(" その他付与Ｐ数,");
        builder.append(" 調整Ｐ,");
        builder.append(" 発行Ｐ合計,");
        builder.append(" Ｐ券使用ポイント,");
        builder.append(" 商品交換Ｐ数,");
        builder.append(" 募金Ｐ数,");
        builder.append(" 使用Ｐ合計,");
        builder.append(" 累計調整Ｐ,");
        builder.append(" クリアＰ数,");
        builder.append(" クリアＰ数顧客退会前年,");
        builder.append(" クリアＰ数顧客退会当年,");
        builder.append(" クリアＰ数有効期限,");
        builder.append(" 月末Ｐ残高,");
        builder.append(" 月末Ｐ残高前年,");
        builder.append(" 月末Ｐ残高当年");
        builder.append(" )");
        builder.append(" SELECT ?,");
        builder.append(" NVL(店情報.企業コード, 0) as 会社ＣＤ,");
        builder.append(" 通常Ｐ集計.店番号 as 店舗ＣＤ,");
        builder.append(" (SUM(通常Ｐ集計.月初Ｐ残高前年) +");
        builder.append(" SUM(通常Ｐ集計.月初Ｐ残高当年)) as 月初Ｐ残高,");
        builder.append(" SUM(通常Ｐ集計.月初Ｐ残高前年) as 月初Ｐ残高前年,");
        builder.append(" SUM(通常Ｐ集計.月初Ｐ残高当年) as 月初Ｐ残高当年,");
        builder.append(" SUM(通常Ｐ集計.買上付与Ｐ数) as 買上付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.買上回数付与Ｐ数) as 買上回数付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.ベビーサークルＰ数) as ベビーサークル付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.ワタシプラス連携付与Ｐ数) as ワタシプラス連携付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.アプリＰ交換Ｐ数) as アプリＰ交換Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.カタリナ付与Ｐ数) as カタリナ付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.その他付与Ｐ数) as その他付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.調整付与Ｐ数) as 調整付与Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.買上付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.買上回数付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.ベビーサークルＰ数) +");
        builder.append(" SUM(通常Ｐ集計.調整付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.ワタシプラス連携付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.その他付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.アプリＰ交換Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.カタリナ付与Ｐ数) as 付与Ｐ合計,");
        builder.append(" SUM(通常Ｐ集計.値引券発行Ｐ数) as 値引券発行Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.商品交換Ｐ数) as 商品交換Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.募金Ｐ数) as 募金Ｐ数,");
        builder.append(" SUM(通常Ｐ集計.値引券発行Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.商品交換Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.募金Ｐ数) as 使用Ｐ合計,");
        builder.append(" ((SUM(通常Ｐ集計.月初Ｐ残高前年) +");
        builder.append(" SUM(通常Ｐ集計.月初Ｐ残高当年)) +");
        builder.append(" (SUM(通常Ｐ集計.買上付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.買上回数付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.ベビーサークルＰ数) +");
        builder.append(" SUM(通常Ｐ集計.調整付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.ワタシプラス連携付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.その他付与Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.アプリＰ交換Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.カタリナ付与Ｐ数)) +");
        builder.append(" (SUM(通常Ｐ集計.値引券発行Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.商品交換Ｐ数) +");
        builder.append(" SUM(通常Ｐ集計.募金Ｐ数)) +");
        builder.append(" (SUM(通常Ｐ集計.クリアＰ数顧客退会前年) +");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数顧客退会当年) +");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数有効期限)) -");
        builder.append(" (SUM(通常Ｐ集計.月末Ｐ残高前年) +");
        builder.append(" SUM(通常Ｐ集計.月末Ｐ残高当年))) as 累計調整Ｐ,");
        builder.append(" (SUM(通常Ｐ集計.クリアＰ数顧客退会前年) +");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数顧客退会当年) +");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数有効期限)) as クリアＰ数,");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数顧客退会前年) as クリアＰ数顧客退会前年,");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数顧客退会当年) as クリアＰ数顧客退会当年,");
        builder.append(" SUM(通常Ｐ集計.クリアＰ数有効期限) as クリアＰ数有効期限,");
        builder.append(" (SUM(通常Ｐ集計.月末Ｐ残高前年) +");
        builder.append(" SUM(通常Ｐ集計.月末Ｐ残高当年)) as 月末Ｐ残高,");
        builder.append(" SUM(通常Ｐ集計.月末Ｐ残高前年) as 月末Ｐ残高前年,");
        builder.append(" SUM(通常Ｐ集計.月末Ｐ残高当年) as 月末Ｐ残高当年");
        builder.append(" FROM (");


        builder.append(" (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 0");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (62, 63, 64, 65, 66)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (62, 63, 64, 65, 66)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (62, 63, 64, 65, 66)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (62, 63, 64, 65, 66)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (62, 63, 64, 65, 66)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (60, 61)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 75");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 75");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 72");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 72");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 76");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 79");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 79");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 5");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (10, 30)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (10, 30)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" SUM(更新付与通常Ｐ) AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE 理由コード = 1105");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND システム年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) in (0, 8)");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 >= ?");
        builder.append(" AND 精算年月日 between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 70");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 70");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 71");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d");
        builder.append(" WHERE MOD(理由コード, 100) = 71");
        builder.append(" AND 顧客番号 <> 9999999999999");
        builder.append(" AND 最終更新日 < ?");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND 会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY 店番号)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ前年度) * -1 AS クリアＰ数顧客退会前年,");
        builder.append(" SUM(更新利用通常Ｐ当年度) * -1 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE MOD(理由コード, 100) = 93");
        builder.append(" AND T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" SUM(更新利用通常Ｐ当年度) * -1 AS クリアＰ数顧客退会前年,");
        builder.append(" SUM(更新利用通常Ｐ翌年度) * -1 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE MOD(理由コード, 100) = 93");
        builder.append(" AND T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" SUM(更新利用通常Ｐ) * -1 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE MOD(理由コード, 100) = 91");
        builder.append(" AND T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND CASE WHEN 登録年月日 <> 0 THEN 登録年月日 ELSE システム年月日 END between ? and ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY T2.入会店舗)");
        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30)");
        builder.append(" GROUP BY T2.入会店舗)");
        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (10, 30, 72, 75, 76, 79)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND T1.理由コード = 1105");
        builder.append(" AND NOT T1.システム年月日 between ? and ?");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (10, 30, 72, 75, 76, 79)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新付与通常Ｐ当年度) * -1 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND T1.理由コード = 1105");
        builder.append(" AND NOT T1.システム年月日 between ? and ?");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND (MOD(T1.理由コード, 100) in (0, 60, 61, 62, 63, 64, 65, 66)");
        builder.append(" OR T1.理由コード = 5)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (10, 30, 72, 75, 76, 79)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新付与通常Ｐ前年度) * -1 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND T1.理由コード = 1105");
        builder.append(" AND NOT T1.システム年月日 between ? and ?");
        builder.append(" AND T1.更新付与通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (70, 71)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ翌年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ翌年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.更新利用通常Ｐ当年度) AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ翌年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (70, 71)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (0, 8)");
        builder.append(" AND (T1.精算年月日 > ?");
        builder.append(" OR T1.精算年月日 = 0");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");

        builder.append(" UNION ALL (SELECT T2.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.更新利用通常Ｐ前年度) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.HSポイント日別情報%d T1,");
        builder.append(" cmuser.TS利用可能ポイント情報 T2");
        builder.append(" WHERE T1.顧客番号 = T2.顧客番号");
        builder.append(" AND T1.顧客番号 <> 9999999999999");
        builder.append(" AND MOD(T1.理由コード, 100) in (70, 71)");
        builder.append(" AND (NOT CASE WHEN T1.登録年月日 <> 0 THEN T1.登録年月日 ELSE T1.システム年月日 END between ? and ?");
        builder.append(" OR T1.最終更新日 >= ?)");
        builder.append(" AND T1.更新利用通常Ｐ基準年度 = ?");
        builder.append(" AND T1.会員旧販社コード NOT IN (20,30) ");
        builder.append(" GROUP BY T2.入会店舗)");


        builder.append(" UNION ALL (SELECT T1.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" SUM(T1.利用可能通常Ｐ%s) AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.TS利用可能ポイント情報 T1");
        builder.append(" WHERE T1.顧客番号 <> 9999999999999");
        builder.append(" AND BITAND(CAST(T1.通常Ｐ失効フラグ AS INTEGER), ?) <> ?");
        builder.append(" AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE C.顧客番号=T1.顧客番号 AND 旧販社コード IN (20,30) )");
        builder.append(" GROUP BY T1.入会店舗)");

        builder.append(" UNION ALL (SELECT T1.入会店舗 AS 店番号,");
        builder.append(" 0 AS 月初Ｐ残高前年,");
        builder.append(" 0 AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" SUM(T1.利用可能通常Ｐ%s) AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.TS利用可能ポイント情報 T1");
        builder.append(" WHERE T1.顧客番号 <> 9999999999999");
        builder.append(" AND BITAND(CAST(T1.通常Ｐ失効フラグ AS INTEGER), ?) <> ?");
        builder.append(" AND NOT EXISTS (SELECT 1 FROM MSカード情報 C WHERE C.顧客番号=T1.顧客番号 AND 旧販社コード IN (20,30) )");
        builder.append(" GROUP BY T1.入会店舗)");

        builder.append(" UNION ALL (SELECT 店番号,");
        builder.append(" %s AS 月初Ｐ残高前年,");
        builder.append(" %s AS 月初Ｐ残高当年,");
        builder.append(" 0 AS 買上付与Ｐ数,");
        builder.append(" 0 AS 買上回数付与Ｐ数,");
        builder.append(" 0 AS ベビーサークルＰ数,");
        builder.append(" 0 AS ワタシプラス連携付与Ｐ数,");
        builder.append(" 0 AS アプリＰ交換Ｐ数,");
        builder.append(" 0 AS カタリナ付与Ｐ数,");
        builder.append(" 0 AS その他付与Ｐ数,");
        builder.append(" 0 AS 調整付与Ｐ数,");
        builder.append(" 0 AS 値引券発行Ｐ数,");
        builder.append(" 0 AS 商品交換Ｐ数,");
        builder.append(" 0 AS 募金Ｐ数,");
        builder.append(" 0 AS クリアＰ数顧客退会前年,");
        builder.append(" 0 AS クリアＰ数顧客退会当年,");
        builder.append(" 0 AS クリアＰ数有効期限,");
        builder.append(" 0 AS 月末Ｐ残高前年,");
        builder.append(" 0 AS 月末Ｐ残高当年");
        builder.append(" FROM cmuser.AS通常ポイント集計情報");
        builder.append(" WHERE 集計対象月 = ?");
        builder.append(" GROUP BY 店番号)");
        builder.append(") 通常Ｐ集計  LEFT JOIN");
        builder.append(" (SELECT 店番号");
        builder.append(" ,企業コード");
        builder.append(" FROM (SELECT /*+ INDEX(cmuser.PS店表示情報 PKPSSYSDSI00) */店番号");
        builder.append(" ,DECODE(店番号, '999001', 1000, '999002', 1000, 企業コード) AS 企業コード");
        builder.append(" ,ROW_NUMBER() OVER (PARTITION BY 店番号");
        builder.append(" ORDER BY 終了年月日 DESC) G_row");
        builder.append(" FROM cmuser.PS店表示情報");
        builder.append(" WHERE 開始年月日 <= ?) PSST");
        builder.append(" WHERE G_row = 1) 店情報");
        builder.append(" ON 通常Ｐ集計.店番号 = 店情報.店番号");
        builder.append(" GROUP BY 店情報.企業コード, 通常Ｐ集計.店番号");
        builder.append(" ORDER BY 店情報.企業コード, 通常Ｐ集計.店番号");
        sprintf(wk_sql,
                builder.toString()
                /* １－１～１－５．買上付与Ｐ */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* ２－１～２－５．買上回数Ｐ */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* ３－１．～３－３．ベビーサークルＰ */
                , h_batdate_ym
                , h_batdate_nym
                , h_batdate_ym
                /* ４－１．～４－２．ワタシプラス連携付与Ｐ数 */
                , h_batdate_ym
                , h_batdate_nym
                /* ５－１．～５－２．アプリＰ交換Ｐ数 */
                , h_batdate_ym
                , h_batdate_nym
                /* ６－１．カタリナ付与Ｐ数 */
                , h_batdate_ym
                /* ７－１．～７－２．その他付与Ｐ数 */
                , h_batdate_ym
                , h_batdate_nym
                /* ８－１．～８－５．調整Ｐ（ＰＯＳ） */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* ８－６．～８－７．調整Ｐ（ＰＣ） */
                , h_batdate_ym
                , h_batdate_nym
                /* ６－８．調整Ｐ（バッチ） */
                , h_batdate_ym
                /* ９－１．～９－５．値引券発行 */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* １０－１．～１０－２．商品交換Ｐ数 */
                , h_batdate_ym
                , h_batdate_nym
                /* １１－１．～１１－２．募金Ｐ数 */
                , h_batdate_ym
                , h_batdate_nym
                /* １２－１．～１２－２．クリアＰ(顧客退会) */
                , h_batdate_ym
                , h_batdate_ym
                /* １３－１．クリアＰ(有効期限) */
                , h_batdate_nym
                /* １４－１．～１４－４．月末残補正（付与Ｐ）（当年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                , h_batdate_nym
                /* １５－１．～１５－４．月末残補正（付与Ｐ）（前年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                , h_batdate_nym
                /* １６－１．～１６－４．月末残補正（付与Ｐ）（翌年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                , h_batdate_nym
                /* １７－１．～１７－３．月末残補正（利用Ｐ）（当年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                /* １８－１．～１８－３．月末残補正（利用Ｐ）（前年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                /* １９－１．～１９－３．月末残補正（利用Ｐ）（翌年度） */
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_nym
                /* ２０－１．～２０－２．月末Ｐ  */
                , year_cd_zennen
                , year_cd_tonen
                /* ２１－１．月初Ｐ  */
                , col_nm_zennen
                , col_nm_tonen
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** (ポイント集計情報作成)" +
                    "動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_ins1 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** AS通常ポイント集計情報" +
                    " INSERT PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "AS通常ポイント集計情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPoint処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        sqlca.query();
        /* INSERT文を実行する */
//        EXEC SQL EXECUTE sql_stat_ins1 USING
//    :h_batdate_ym,
//        /* １－１～１－５．買上付与Ｐ */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ２－１～２－５．買上回数Ｐ */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ３－１．～３－３．ベビーサークルＰ */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ４－１．～４－２．ワタシプラス連携付与Ｐ数 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ５－１．～５－２．アプリＰ交換Ｐ数 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ７－１．～７－２．その他付与Ｐ数 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ８－１．～８－５．調整Ｐ（ＰＯＳ） */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ８－６．～８－８．調整Ｐ（ＰＣ／バッチ） */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* ９－１．～９－５．値引券発行 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev,
//             :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* １０－１．～１０－２．商品交換Ｐ数 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １１－１．～１１－２．募金Ｐ数 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//        /* １２－１．～１２－２．クリアＰ(顧客退会) */
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date, :h_batdate_nendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date, :h_batdate_pnendo,
//        /* １３－１．クリアＰ(有効期限) */
//         :h_batdate_nym_start_date, :h_batdate_ymd,
//        /* １４－１．～１４－４．月末残補正（付与Ｐ）（当年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_nendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_nendo,
//        /* １５－１．～１５－４．月末残補正（付与Ｐ）（前年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_pnendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_pnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_pnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_pnendo,
//        /* １６－１．～１６－４．月末残補正（付与Ｐ）（翌年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nnendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_nnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_nnendo,
//        /* １７－１．～１７－３．月末残補正（利用Ｐ）（当年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_nendo,
//        /* １８－１．～１８－３．月末残補正（利用Ｐ）（前年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_pnendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_pnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_pnendo,
//        /* １９－１．～１９－３．月末残補正（利用Ｐ）（翌年度） */
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nnendo,
//         :h_batdate_ym_end_date, :h_batdate_ymd, :h_batdate_nnendo,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//             :h_batdate_ymd, :h_batdate_nnendo,
//        /* ２０－１．～２０－２．月末Ｐ残高 */
//         :h_tujo_bit_zennen, :h_tujo_bit_zennen,
//         :h_tujo_bit_tonen, :h_tujo_bit_tonen,
//        /* ２１．月初Ｐ */
//         :h_batdate_pym,
//        /* 店情報 */
//         :h_batdate_ym_end_date;

        sqlca.restAndExecute(h_batdate_ym,
                /* １－１～１－５．買上付与Ｐ */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ２－１～２－５．買上回数Ｐ */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ３－１．～３－３．ベビーサークルＰ */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ４－１．～４－２．ワタシプラス連携付与Ｐ数 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* ５－１．～５－２．アプリＰ交換Ｐ数 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* ７－１．～７－２．その他付与Ｐ数 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* ８－１．～８－５．調整Ｐ（ＰＯＳ） */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ８－６．～８－８．調整Ｐ（ＰＣ／バッチ） */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* ９－１．～９－５．値引券発行 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev,
                h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* １０－１．～１０－２．商品交換Ｐ数 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* １１－１．～１１－２．募金Ｐ数 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                /* １２－１．～１２－２．クリアＰ(顧客退会) */
                h_batdate_ym_start_date, h_batdate_ym_end_date, h_batdate_nendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date, h_batdate_pnendo,
                /* １３－１．クリアＰ(有効期限) */
                h_batdate_nym_start_date, h_batdate_ymd,
                /* １４－１．～１４－４．月末残補正（付与Ｐ）（当年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_nendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_nendo,
                /* １５－１．～１５－４．月末残補正（付与Ｐ）（前年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_pnendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_pnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_pnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_pnendo,
                /* １６－１．～１６－４．月末残補正（付与Ｐ）（翌年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nnendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_nnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_nnendo,
                /* １７－１．～１７－３．月末残補正（利用Ｐ）（当年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_nendo,
                /* １８－１．～１８－３．月末残補正（利用Ｐ）（前年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_pnendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_pnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_pnendo,
                /* １９－１．～１９－３．月末残補正（利用Ｐ）（翌年度） */
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nnendo,
                h_batdate_ym_end_date, h_batdate_ymd, h_batdate_nnendo,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_nnendo,
                /* ２０－１．～２０－２．月末Ｐ残高 */
                h_tujo_bit_zennen, h_tujo_bit_zennen,
                h_tujo_bit_tonen, h_tujo_bit_tonen,
                /* ２１．月初Ｐ */
                h_batdate_pym,
                /* 店情報 */
                h_batdate_ym_end_date);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPoint *** AS通常ポイント集計情報 " +
                    "INSERT sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "AS通常ポイント集計情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPoint処理", C_const_NG, 0, 0);
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
    /*  関数名 ： cmBTtzdkB_CreateTujyoPointFile                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_CreateTujyoPointFile()                    */
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
    public int cmBTtzdkB_CreateTujyoPointFile() {

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

//        if ((fp_pd = fopen(fp_name, "w")) == null) {
        if ((fp_pd = fopen(fp_name.arr, SystemConstant.Shift_JIS, FileOpenType.w)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** ファイルオープン" +
                        "ERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* *******************/
        /* 作成日の出力     */
        /* *******************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
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

        fwrite(sjis_buf.arr, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* *******************/
        /* 作成時刻の出力   */
        /* *******************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
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

        fwrite(sjis_buf.arr, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* *******************/
        /* ヘダー項目の出力 */
        /* *******************/
        /* UTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
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

        fwrite(sjis_buf.arr, strlen(sjis_buf), 1, fp_pd);
        rtn_cd = ferror(fp_pd);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_pd_cnt++;

        /* *****************************************/
        /* AS通常ポイント集計情報（店舗別）の取得 */
        /* *****************************************/
        sprintf(wk_sql,
                "SELECT"
                        + " AS通常ポイント集計情報.集計対象月,"
                        + " DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード) AS 旧販社コード,"
                        + " AS通常ポイント集計情報.店番号,"
                        + " AS通常ポイント集計情報.月初Ｐ残高,"
                        + " AS通常ポイント集計情報.月初Ｐ残高前年,"
                        + " AS通常ポイント集計情報.月初Ｐ残高当年,"
                        + " AS通常ポイント集計情報.買上Ｐ数,"
                        + " AS通常ポイント集計情報.ベビーサークル,"
                        + " AS通常ポイント集計情報.買上回数Ｐ数,"
                        + " AS通常ポイント集計情報.ワタシプラス連携付与Ｐ数,"
                        + " AS通常ポイント集計情報.アプリＰ交換Ｐ数,"
                        + " AS通常ポイント集計情報.カタリナ付与Ｐ数,"
                        + " AS通常ポイント集計情報.その他付与Ｐ数,"
                        + " AS通常ポイント集計情報.調整Ｐ,"
                        + " AS通常ポイント集計情報.発行Ｐ合計,"
                        + " AS通常ポイント集計情報.Ｐ券使用ポイント,"
                        + " AS通常ポイント集計情報.商品交換Ｐ数,"
                        + " AS通常ポイント集計情報.募金Ｐ数,"
                        + " AS通常ポイント集計情報.使用Ｐ合計,"
                        + " AS通常ポイント集計情報.累計調整Ｐ,"
                        + " AS通常ポイント集計情報.クリアＰ数,"
                        + " AS通常ポイント集計情報.クリアＰ数顧客退会前年,"
                        + " AS通常ポイント集計情報.クリアＰ数顧客退会当年,"
                        + " AS通常ポイント集計情報.クリアＰ数有効期限,"
                        + " AS通常ポイント集計情報.月末Ｐ残高,"
                        + " AS通常ポイント集計情報.月末Ｐ残高前年,"
                        + " AS通常ポイント集計情報.月末Ｐ残高当年,"
                        + " CASE DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END AS 企業名,"
                        + " NVL(RPAD(PS店表示情報.漢字店舗名称,LENGTH(PS店表示情報.漢字店舗名称)), '%s')"
                        + " FROM"
                        + " AS通常ポイント集計情報 LEFT JOIN "
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
                        + " ON AS通常ポイント集計情報.店番号      = PS店表示情報.店番号 "
                        + " WHERE"
                        + " AS通常ポイント集計情報.集計対象月  = ?"
                        + " ORDER BY"
                        + " AS通常ポイント集計情報.集計対象月,"
                        + " DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード),"
                        + " AS通常ポイント集計情報.店番号"
                , C_NULLSTR
                , C_NULLSTR
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                    "(店舗別ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel1 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(店舗別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "AS通常ポイント集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASPNT_MISE CURSOR FOR sql_stat_sel1;
        sqlca.declare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(店舗別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "AS通常ポイント集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
        sqlca.open(h_batdate_ymd, h_batdate_ym);
//        EXEC SQL OPEN CUR_ASPNT_MISE USING :h_batdate_ymd,
//                                  :h_batdate_ym;

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(店舗別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "AS通常ポイント集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {
            memset(aspnt_t, 0x00, sizeof(aspnt_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

//            EXEC SQL FETCH CUR_ASPNT_MISE
//            INTO :aspnt_t.shukei_ymd,
//                          :aspnt_t.kyu_hansya_cd,
//                          :aspnt_t.mise_no,
//                          :aspnt_t.begin_point,
//                          :aspnt_t.begin_point_zennen,
//                          :aspnt_t.begin_point_tounen,
//                          :aspnt_t.kaiage_point,
//                          :aspnt_t.babycircle,
//                          :aspnt_t.kaiage_cnt_point,
//                          :aspnt_t.watashiplus,
//                          :aspnt_t.app_koukan_point,
//                          :aspnt_t.catalina_point,
//                          :aspnt_t.sonota,
//                          :aspnt_t.chosei_point,
//                          :aspnt_t.hakkou_point_sum,
//                          :aspnt_t.ticket_use_point,
//                          :aspnt_t.syouhin_koukan_point,
//                          :aspnt_t.bokin_point,
//                          :aspnt_t.use_point_sum,
//                          :aspnt_t.ruikei_chosei_point,
//                          :aspnt_t.clear_point,
//                          :aspnt_t.clear_point_taikai_zennen,
//                          :aspnt_t.clear_point_taikai_tounen,
//                          :aspnt_t.clear_point_yuukou,
//                          :aspnt_t.end_point,
//                          :aspnt_t.end_point_zennen,
//                          :aspnt_t.end_point_tounen,
//                          :h_kyu_hansya_name,
//                          :h_mise_kanji;

            sqlca.fetch();
            sqlca.recData(aspnt_t.shukei_ymd,
                    aspnt_t.kyu_hansya_cd,
                    aspnt_t.mise_no,
                    aspnt_t.begin_point,
                    aspnt_t.begin_point_zennen,
                    aspnt_t.begin_point_tounen,
                    aspnt_t.kaiage_point,
                    aspnt_t.babycircle,
                    aspnt_t.kaiage_cnt_point,
                    aspnt_t.watashiplus,
                    aspnt_t.app_koukan_point,
                    aspnt_t.catalina_point,
                    aspnt_t.sonota,
                    aspnt_t.chosei_point,
                    aspnt_t.hakkou_point_sum,
                    aspnt_t.ticket_use_point,
                    aspnt_t.syouhin_koukan_point,
                    aspnt_t.bokin_point,
                    aspnt_t.use_point_sum,
                    aspnt_t.ruikei_chosei_point,
                    aspnt_t.clear_point,
                    aspnt_t.clear_point_taikai_zennen,
                    aspnt_t.clear_point_taikai_tounen,
                    aspnt_t.clear_point_yuukou,
                    aspnt_t.end_point,
                    aspnt_t.end_point_zennen,
                    aspnt_t.end_point_tounen,
                    h_kyu_hansya_name,
                    h_mise_kanji);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                                "AS通常ポイント集計情報(店舗別) FETCH sqlcode=[%d]\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "AS通常ポイント集計情報(店舗別集計)", log_format_buf,
                        0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPNT_MISE;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                                    "AS通常ポイント集計情報(店舗別) FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント集計表に出力           */
            /*--------------------------------*/
            /* ***********/
            /* 企業ＣＤ */
            /* ***********/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf(wk_buf1, "\"%d\",\"", aspnt_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /* ***********/
            /* 企業名   */
            /* ***********/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }

            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
//            memcpy(wk_buf1, sjis_buf, sjis_len);
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /************/
            /* 店舗ＣＤ */
            /************/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf(wk_buf2, "\",\"%d\",\"", aspnt_t.mise_no);

            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /************/
            /* 店舗名   */
            /************/
            /*  店舗名の末尾のスペース削除 */
            BT_Rtrim(h_mise_kanji.strDto(), strlen(h_mise_kanji));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_mise_kanji.strVal(), C_NULLSTR) == 0) {
                strcpy(h_mise_kanji, "");
            }

            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_mise_kanji.strDto());
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /********************************/
            /* 月初Ｐ残高～月末Ｐ残高当年   */
            /********************************/
            sprintf(wk_buf2,
                    "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n",
                    aspnt_t.begin_point,
                    aspnt_t.begin_point_zennen,
                    aspnt_t.begin_point_tounen,
                    aspnt_t.kaiage_point,
                    aspnt_t.babycircle,
                    aspnt_t.kaiage_cnt_point,
                    aspnt_t.watashiplus,
                    aspnt_t.app_koukan_point,
                    aspnt_t.catalina_point,
                    aspnt_t.sonota,
                    aspnt_t.chosei_point,
                    aspnt_t.hakkou_point_sum,
                    aspnt_t.ticket_use_point,
                    aspnt_t.syouhin_koukan_point,
                    aspnt_t.bokin_point,
                    aspnt_t.use_point_sum,
                    aspnt_t.ruikei_chosei_point,
                    aspnt_t.clear_point,
                    aspnt_t.clear_point_taikai_zennen,
                    aspnt_t.clear_point_taikai_tounen,
                    aspnt_t.clear_point_yuukou,
                    aspnt_t.end_point,
                    aspnt_t.end_point_zennen,
                    aspnt_t.end_point_tounen
            );
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* ファイル出力   */
            /*----------------*/
            fwrite(wk_buf1.arr, buf_len, 1, fp_pd);
            rtn_cd = ferror(fp_pd);
            if (rtn_cd != C_const_OK) {

                /* ファイル書き込みエラー */
                sprintf(log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPNT_MISE;
                sqlca.curse_close();

                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pd_cnt++;

        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASPNT_MISE;
        sqlca.curse_close();
        /* *****************************************/
        /* AS通常ポイント集計情報（企業別）の取得 */
        /* *****************************************/
        sprintf(wk_sql,
                "SELECT"
                        + " AS通常ポイント集計情報.集計対象月,"
                        + " DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード) AS 旧販社コード,"
                        + " SUM(AS通常ポイント集計情報.月初Ｐ残高),"
                        + " SUM(AS通常ポイント集計情報.月初Ｐ残高前年),"
                        + " SUM(AS通常ポイント集計情報.月初Ｐ残高当年),"
                        + " SUM(AS通常ポイント集計情報.買上Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.ベビーサークル),"
                        + " SUM(AS通常ポイント集計情報.買上回数Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.ワタシプラス連携付与Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.アプリＰ交換Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.カタリナ付与Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.その他付与Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.調整Ｐ),"
                        + " SUM(AS通常ポイント集計情報.発行Ｐ合計),"
                        + " SUM(AS通常ポイント集計情報.Ｐ券使用ポイント),"
                        + " SUM(AS通常ポイント集計情報.商品交換Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.募金Ｐ数),"
                        + " SUM(AS通常ポイント集計情報.使用Ｐ合計),"
                        + " SUM(AS通常ポイント集計情報.累計調整Ｐ),"
                        + " SUM(AS通常ポイント集計情報.クリアＰ数),"
                        + " SUM(AS通常ポイント集計情報.クリアＰ数顧客退会前年),"
                        + " SUM(AS通常ポイント集計情報.クリアＰ数顧客退会当年),"
                        + " SUM(AS通常ポイント集計情報.クリアＰ数有効期限),"
                        + " SUM(AS通常ポイント集計情報.月末Ｐ残高),"
                        + " SUM(AS通常ポイント集計情報.月末Ｐ残高前年),"
                        + " SUM(AS通常ポイント集計情報.月末Ｐ残高当年),"
                        + " CASE DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END AS 企業名"
                        + " FROM"
                        + " AS通常ポイント集計情報"
                        + " WHERE"
                        + " AS通常ポイント集計情報.集計対象月  = ?"
                        + " GROUP BY"
                        + " AS通常ポイント集計情報.集計対象月,"
                        + " DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード)"
                        + " ORDER BY"
                        + " AS通常ポイント集計情報.集計対象月,"
                        + " DECODE(AS通常ポイント集計情報.旧販社コード,'1040',1010,AS通常ポイント集計情報.旧販社コード)"
                , C_NULLSTR
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                    "(企業別ポイント集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        memset(str_sql, 0x00, sizeof(str_sql));

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel2 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(企業別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "AS通常ポイント集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASPNT_HANSHA CURSOR FOR sql_stat_sel2;
        sqlca.declare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(企業別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "AS通常ポイント集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_ASPNT_HANSHA USING :h_batdate_ym;
        sqlca.open(h_batdate_ym);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                    "(企業別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "AS通常ポイント集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {
            memset(aspnt_t, 0x00, sizeof(aspnt_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

//            EXEC SQL FETCH CUR_ASPNT_HANSHA
//            INTO :aspnt_t.shukei_ymd,
//                          :aspnt_t.kyu_hansya_cd,
//                          :aspnt_t.begin_point,
//                          :aspnt_t.begin_point_zennen,
//                          :aspnt_t.begin_point_tounen,
//                          :aspnt_t.kaiage_point,
//                          :aspnt_t.babycircle,
//                          :aspnt_t.kaiage_cnt_point,
//                          :aspnt_t.watashiplus,
//                          :aspnt_t.app_koukan_point,
//                          :aspnt_t.catalina_point,
//                          :aspnt_t.sonota,
//                          :aspnt_t.chosei_point,
//                          :aspnt_t.hakkou_point_sum,
//                          :aspnt_t.ticket_use_point,
//                          :aspnt_t.syouhin_koukan_point,
//                          :aspnt_t.bokin_point,
//                          :aspnt_t.use_point_sum,
//                          :aspnt_t.ruikei_chosei_point,
//                          :aspnt_t.clear_point,
//                          :aspnt_t.clear_point_taikai_zennen,
//                          :aspnt_t.clear_point_taikai_tounen,
//                          :aspnt_t.clear_point_yuukou,
//                          :aspnt_t.end_point,
//                          :aspnt_t.end_point_zennen,
//                          :aspnt_t.end_point_tounen,
//                          :h_kyu_hansya_name;

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** AS通常ポイント集計情報" +
                        "(企業別) FETCH sqlcode=[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }

            sqlca.fetch();
            sqlca.recData(aspnt_t.shukei_ymd,
                    aspnt_t.kyu_hansya_cd,
                    aspnt_t.begin_point,
                    aspnt_t.begin_point_zennen,
                    aspnt_t.begin_point_tounen,
                    aspnt_t.kaiage_point,
                    aspnt_t.babycircle,
                    aspnt_t.kaiage_cnt_point,
                    aspnt_t.watashiplus,
                    aspnt_t.app_koukan_point,
                    aspnt_t.catalina_point,
                    aspnt_t.sonota,
                    aspnt_t.chosei_point,
                    aspnt_t.hakkou_point_sum,
                    aspnt_t.ticket_use_point,
                    aspnt_t.syouhin_koukan_point,
                    aspnt_t.bokin_point,
                    aspnt_t.use_point_sum,
                    aspnt_t.ruikei_chosei_point,
                    aspnt_t.clear_point,
                    aspnt_t.clear_point_taikai_zennen,
                    aspnt_t.clear_point_taikai_tounen,
                    aspnt_t.clear_point_yuukou,
                    aspnt_t.end_point,
                    aspnt_t.end_point_zennen,
                    aspnt_t.end_point_tounen,
                    h_kyu_hansya_name);

            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "AS通常ポイント集計情報(企業別集計)", log_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPNT_HANSHA;
                sqlca.curse_close();

                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                                    "AS通常ポイント集計情報(企業別) FETCH NOTFOUND=[%d]\n",
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
            sprintf(wk_buf1, "\"%d\",\"", aspnt_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /************/
            /* 企業名   */
            /************/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }
            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                        , rtn_cd, 0, 0, 0, 0);
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
            sjis_len = new IntegerDto();
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

            /* *******************************/
            /* 月初Ｐ残高～月末Ｐ残高当年   */
            /* *******************************/
            sprintf(wk_buf2,
                    "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n",
                    aspnt_t.begin_point,
                    aspnt_t.begin_point_zennen,
                    aspnt_t.begin_point_tounen,
                    aspnt_t.kaiage_point,
                    aspnt_t.babycircle,
                    aspnt_t.kaiage_cnt_point,
                    aspnt_t.watashiplus,
                    aspnt_t.app_koukan_point,
                    aspnt_t.catalina_point,
                    aspnt_t.sonota,
                    aspnt_t.chosei_point,
                    aspnt_t.hakkou_point_sum,
                    aspnt_t.ticket_use_point,
                    aspnt_t.syouhin_koukan_point,
                    aspnt_t.bokin_point,
                    aspnt_t.use_point_sum,
                    aspnt_t.ruikei_chosei_point,
                    aspnt_t.clear_point,
                    aspnt_t.clear_point_taikai_zennen,
                    aspnt_t.clear_point_taikai_tounen,
                    aspnt_t.clear_point_yuukou,
                    aspnt_t.end_point,
                    aspnt_t.end_point_zennen,
                    aspnt_t.end_point_tounen
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
//                EXEC SQL CLOSE CUR_ASPNT_HANSHA;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pd_cnt++;
        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASPNT_HANSHA;
        sqlca.curse_close();

        /* ***************************************/
        /* AS通常ポイント集計情報（全社）の取得 */
        /* ***************************************/

        memset(aspnt_t, 0x00, sizeof(aspnt_t));

//        EXEC SQL SELECT
//        AS通常ポイント集計情報.集計対象月,
//                SUM(AS通常ポイント集計情報.月初Ｐ残高),
//                SUM(AS通常ポイント集計情報.月初Ｐ残高前年),
//                SUM(AS通常ポイント集計情報.月初Ｐ残高当年),
//                SUM(AS通常ポイント集計情報.買上Ｐ数),
//                SUM(AS通常ポイント集計情報.ベビーサークル),
//                SUM(AS通常ポイント集計情報.買上回数Ｐ数),
//                SUM(AS通常ポイント集計情報.ワタシプラス連携付与Ｐ数),
//                SUM(AS通常ポイント集計情報.アプリＰ交換Ｐ数),
//                SUM(AS通常ポイント集計情報.カタリナ付与Ｐ数),
//                SUM(AS通常ポイント集計情報.その他付与Ｐ数),
//                SUM(AS通常ポイント集計情報.調整Ｐ),
//                SUM(AS通常ポイント集計情報.発行Ｐ合計),
//                SUM(AS通常ポイント集計情報.Ｐ券使用ポイント),
//                SUM(AS通常ポイント集計情報.商品交換Ｐ数),
//                SUM(AS通常ポイント集計情報.募金Ｐ数),
//                SUM(AS通常ポイント集計情報.使用Ｐ合計),
//                SUM(AS通常ポイント集計情報.累計調整Ｐ),
//                SUM(AS通常ポイント集計情報.クリアＰ数),
//                SUM(AS通常ポイント集計情報.クリアＰ数顧客退会前年),
//                SUM(AS通常ポイント集計情報.クリアＰ数顧客退会当年),
//                SUM(AS通常ポイント集計情報.クリアＰ数有効期限),
//                SUM(AS通常ポイント集計情報.月末Ｐ残高),
//                SUM(AS通常ポイント集計情報.月末Ｐ残高前年),
//                SUM(AS通常ポイント集計情報.月末Ｐ残高当年)
//        INTO :aspnt_t.shukei_ymd,
//            :aspnt_t.begin_point,
//            :aspnt_t.begin_point_zennen,
//            :aspnt_t.begin_point_tounen,
//            :aspnt_t.kaiage_point,
//            :aspnt_t.babycircle,
//            :aspnt_t.kaiage_cnt_point,
//            :aspnt_t.watashiplus,
//            :aspnt_t.app_koukan_point,
//            :aspnt_t.catalina_point,
//            :aspnt_t.sonota,
//            :aspnt_t.chosei_point,
//            :aspnt_t.hakkou_point_sum,
//            :aspnt_t.ticket_use_point,
//            :aspnt_t.syouhin_koukan_point,
//            :aspnt_t.bokin_point,
//            :aspnt_t.use_point_sum,
//            :aspnt_t.ruikei_chosei_point,
//            :aspnt_t.clear_point,
//            :aspnt_t.clear_point_taikai_zennen,
//            :aspnt_t.clear_point_taikai_tounen,
//            :aspnt_t.clear_point_yuukou,
//            :aspnt_t.end_point,
//            :aspnt_t.end_point_zennen,
//            :aspnt_t.end_point_tounen
//            FROM
//        AS通常ポイント集計情報
//                WHERE
//        AS通常ポイント集計情報.集計対象月  = :h_batdate_ym
//        GROUP BY
//        AS通常ポイント集計情報.集計対象月
//        ORDER BY
//        AS通常ポイント集計情報.集計対象月;

        sqlca.sql = new StringDto("SELECT AS通常ポイント集計情報.集計対象月," +
                " SUM(AS通常ポイント集計情報.月初Ｐ残高)," +
                " SUM(AS通常ポイント集計情報.月初Ｐ残高前年)," +
                " SUM(AS通常ポイント集計情報.月初Ｐ残高当年)," +
                " SUM(AS通常ポイント集計情報.買上Ｐ数)," +
                " SUM(AS通常ポイント集計情報.ベビーサークル)," +
                " SUM(AS通常ポイント集計情報.買上回数Ｐ数)," +
                " SUM(AS通常ポイント集計情報.ワタシプラス連携付与Ｐ数)," +
                " SUM(AS通常ポイント集計情報.アプリＰ交換Ｐ数)," +
                " SUM(AS通常ポイント集計情報.カタリナ付与Ｐ数)," +
                " SUM(AS通常ポイント集計情報.その他付与Ｐ数)," +
                " SUM(AS通常ポイント集計情報.調整Ｐ)," +
                " SUM(AS通常ポイント集計情報.発行Ｐ合計)," +
                " SUM(AS通常ポイント集計情報.Ｐ券使用ポイント)," +
                " SUM(AS通常ポイント集計情報.商品交換Ｐ数)," +
                " SUM(AS通常ポイント集計情報.募金Ｐ数)," +
                " SUM(AS通常ポイント集計情報.使用Ｐ合計)," +
                " SUM(AS通常ポイント集計情報.累計調整Ｐ)," +
                " SUM(AS通常ポイント集計情報.クリアＰ数)," +
                " SUM(AS通常ポイント集計情報.クリアＰ数顧客退会前年)," +
                " SUM(AS通常ポイント集計情報.クリアＰ数顧客退会当年)," +
                " SUM(AS通常ポイント集計情報.クリアＰ数有効期限)," +
                " SUM(AS通常ポイント集計情報.月末Ｐ残高)," +
                " SUM(AS通常ポイント集計情報.月末Ｐ残高前年)," +
                " SUM(AS通常ポイント集計情報.月末Ｐ残高当年)" +
                " FROM" +
                " AS通常ポイント集計情報" +
                "         WHERE" +
                " AS通常ポイント集計情報.集計対象月  = ?" +
                " GROUP BY" +
                " AS通常ポイント集計情報.集計対象月" +
                " ORDER BY" +
                " AS通常ポイント集計情報.集計対象月");
        sqlca.restAndExecute(h_batdate_ym);
        sqlca.fetch();
        sqlca.recData(aspnt_t.shukei_ymd,
                aspnt_t.begin_point,
                aspnt_t.begin_point_zennen,
                aspnt_t.begin_point_tounen,
                aspnt_t.kaiage_point,
                aspnt_t.babycircle,
                aspnt_t.kaiage_cnt_point,
                aspnt_t.watashiplus,
                aspnt_t.app_koukan_point,
                aspnt_t.catalina_point,
                aspnt_t.sonota,
                aspnt_t.chosei_point,
                aspnt_t.hakkou_point_sum,
                aspnt_t.ticket_use_point,
                aspnt_t.syouhin_koukan_point,
                aspnt_t.bokin_point,
                aspnt_t.use_point_sum,
                aspnt_t.ruikei_chosei_point,
                aspnt_t.clear_point,
                aspnt_t.clear_point_taikai_zennen,
                aspnt_t.clear_point_taikai_tounen,
                aspnt_t.clear_point_yuukou,
                aspnt_t.end_point,
                aspnt_t.end_point_zennen,
                aspnt_t.end_point_tounen);

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreateTujyoPointFile *** " +
                            "AS通常ポイント集計情報(全社) SELECT sqlcode=[%d]\n",
                    sqlca.sqlcode);
            /*--------------------------------------------------------------------*/
        }
        /* データ無し以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "AS通常ポイント集計情報(全社集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        /*--------------------------------*/
        /* ポイント集計表に出力(全社)     */
        /*--------------------------------*/
        /* 会社名(,店舗名)のUTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
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

        /* ***********/
        /* 企業ＣＤ */
        /* ***********/
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        sprintf(wk_buf1, "\"%d\",\"", C_KAISHACD);
        buf_len = strlen(wk_buf1);

        /* ***********/
        /* 企業名   */
        /* ***********/
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

        /********************************/
        /* 月初Ｐ残高～月末Ｐ残高当年   */
        /********************************/
        sprintf(wk_buf2,
                "\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\",\"%d\"\r\n",
                aspnt_t.begin_point,
                aspnt_t.begin_point_zennen,
                aspnt_t.begin_point_tounen,
                aspnt_t.kaiage_point,
                aspnt_t.babycircle,
                aspnt_t.kaiage_cnt_point,
                aspnt_t.watashiplus,
                aspnt_t.app_koukan_point,
                aspnt_t.catalina_point,
                aspnt_t.sonota,
                aspnt_t.chosei_point,
                aspnt_t.hakkou_point_sum,
                aspnt_t.ticket_use_point,
                aspnt_t.syouhin_koukan_point,
                aspnt_t.bokin_point,
                aspnt_t.use_point_sum,
                aspnt_t.ruikei_chosei_point,
                aspnt_t.clear_point,
                aspnt_t.clear_point_taikai_zennen,
                aspnt_t.clear_point_taikai_tounen,
                aspnt_t.clear_point_yuukou,
                aspnt_t.end_point,
                aspnt_t.end_point_zennen,
                aspnt_t.end_point_tounen
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
                C_DbgEnd("cmBTtzdkB_CreateTujyoPointFile処理", C_const_NG, 0, 0);
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTtzdkB_CreatePointTicket                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_CreatePointTicket()                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      HSポイント日別情報から店舗別集計値をポイント券集計情報に登録する。    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTtzdkB_CreatePointTicket() {

        StringDto wk_sql = new StringDto(C_const_SQLMaxLen * 7);       /* 動的SQLバッファ              */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTtzdkB_CreatePointTicket処理 ***");
            /*------------------------------------------------------------------------*/
        }

        memset(wk_sql, 0x00, sizeof(wk_sql));

        /* ASポイント券集計情報に                             */
        /* HSポイント日別情報より店舗毎に集計したデータを登録 */
        sprintf(wk_sql,
                "INSERT INTO ASポイント券集計情報"
                        + " ("
                        + " 集計対象月,"
                        + " 旧販社コード,"
                        + " 店番号,"
                        + " 発行Ｐ券ポイント数,"
                        + " 利用Ｐ券按分額"
                        + " )"
                        + " SELECT ?"
                        + " ,NVL(店情報.企業コード, 0) AS 企業ＣＤ"
                        + " ,ポイント券集計.店番号 AS 店舗ＣＤ"
                        + " ,SUM(ポイント券集計.値引券発行Ｐ数) as 値引券発行Ｐ数"
                        + " ,SUM(ポイント券集計.値引券実値引額) as 値引券実値引額"
                        + " FROM ("
                        /* １．値引券発行 */
                        /* １－１．当月分 */
                        + " (SELECT T1.店番号"
                        + " ,SUM(T1.利用ポイント) AS 値引券発行Ｐ数"
                        + " ,0 AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) in (0, 8, 92)"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.顧客番号 <> 9999999999999"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* １－２．月跨ぎでの処理分 */
                        + " UNION ALL (SELECT T1.店番号"
                        + " ,SUM(T1.利用ポイント) AS 値引券発行Ｐ数"
                        + " ,0 AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) in (0, 8, 92)"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.顧客番号 <> 9999999999999"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* １－３．処理日当月処理分 */
                        + " UNION ALL (SELECT T1.店番号"
                        + " ,SUM(T1.利用ポイント) AS 値引券発行Ｐ数"
                        + " ,0 AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) in (0, 8, 92)"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.顧客番号 <> 9999999999999"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* １－４．前月集計漏れ分 */
                        + " UNION ALL (SELECT T1.店番号"
                        + " ,SUM(T1.利用ポイント) AS 値引券発行Ｐ数"
                        + " ,0 AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) in (0, 8, 92)"
                        + " AND T1.最終更新日 >= ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.顧客番号 <> 9999999999999"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* １－５．前月集計漏れの月またぎ分 */
                        + " UNION ALL (SELECT T1.店番号"
                        + " ,SUM(T1.利用ポイント) AS 値引券発行Ｐ数"
                        + " ,0 AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) in (0, 8, 92)"
                        + " AND T1.最終更新日 >= ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.顧客番号 <> 9999999999999"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ２．ECの利用Ｐ */
                        /* ２－１．当月分 */
                        + " UNION ALL (SELECT T1.店番号 AS 店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T1.利用ポイント) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) = 0"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.企業コード = 1020"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ２－２．月跨ぎでの処理分 */
                        + " UNION ALL (SELECT T1.店番号 AS 店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T1.利用ポイント) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) = 0"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.企業コード = 1020"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ２－３．処理日当月処理分 */
                        + " UNION ALL (SELECT T1.店番号 AS 店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T1.利用ポイント) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) = 0"
                        + " AND T1.最終更新日 < ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.企業コード = 1020"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ２－４．前月集計漏れ分 */
                        + " UNION ALL (SELECT T1.店番号 AS 店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T1.利用ポイント) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) = 0"
                        + " AND T1.最終更新日 >= ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.企業コード = 1020"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ２－５．前月集計漏れの月またぎ分 */
                        + " UNION ALL (SELECT T1.店番号 AS 店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T1.利用ポイント) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント日別情報%d T1"
                        + " WHERE MOD(T1.理由コード, 100) = 0"
                        + " AND T1.最終更新日 >= ?"
                        + " AND T1.精算年月日 between ? and ?"
                        + " AND T1.企業コード = 1020"
                        + " AND T1.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T1.店番号)"
                        /* ３．値引実値引額 */
                        /* ３－１．当月分 */
                        + " UNION ALL (SELECT T2.登録店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T3.サービス券按分額) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント明細取引情報%d T2,"
                        + " cmuser.HSポイント明細商品情報%d T3"
                        + " WHERE T2.システム年月日 = T3.システム年月日"
                        + " AND T2.サービス対応番号 = T3.サービス対応番号"
                        + " AND T2.取引通番 = T3.取引通番"
                        + " AND T2.精算日付 between ? and ?"
                        + " AND T2.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T2.登録店番号)"
                        /* ３－２．月跨ぎでの処理分 */
                        + " UNION ALL (SELECT T2.登録店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T3.サービス券按分額) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント明細取引情報%d T2,"
                        + " cmuser.HSポイント明細商品情報%d T3"
                        + " WHERE T2.システム年月日 = T3.システム年月日"
                        + " AND T2.サービス対応番号 = T3.サービス対応番号"
                        + " AND T2.取引通番 = T3.取引通番"
                        + " AND T2.精算日付 between ? and ?"
                        + " AND T2.システム年月日 < ?"
                        + " AND T2.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T2.登録店番号)"
                        /* ３－３．前月TBLの集計対象精算データ */
                        + " UNION ALL (SELECT T2.登録店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T3.サービス券按分額) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント明細取引情報%d T2,"
                        + " cmuser.HSポイント明細商品情報%d T3"
                        + " WHERE T2.システム年月日 = T3.システム年月日"
                        + " AND T2.サービス対応番号 = T3.サービス対応番号"
                        + " AND T2.取引通番 = T3.取引通番"
                        + " AND T2.精算日付 between ? and ?"
                        + " AND T2.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T2.登録店番号)"
                        /* ３－４．前月の集計漏れ分 前回集計基準日以降に取り込まれた前回集計対象の精算年月の明細情報を集計 */
                        + " UNION ALL (SELECT T2.登録店番号"
                        + " ,0 AS 値引券発行Ｐ数"
                        + " ,SUM(T3.サービス券按分額) AS 値引券実値引額"
                        + " FROM cmuser.HSポイント明細取引情報%d T2,"
                        + " cmuser.HSポイント明細商品情報%d T3"
                        + " WHERE T2.システム年月日 = T3.システム年月日"
                        + " AND T2.サービス対応番号 = T3.サービス対応番号"
                        + " AND T2.取引通番 = T3.取引通番"
                        + " AND T2.精算日付 between ? and ?"
                        + " AND T2.システム年月日 >= ?"
                        + " AND T2.会員旧販社コード NOT IN (20,30) "
                        + " GROUP BY T2.登録店番号)"
                        + " ) ポイント券集計 LEFT JOIN "
                        + " (SELECT 店番号"
                        + " ,企業コード"
                        + " FROM (SELECT /*+ INDEX(cmuser.PS店表示情報 PKPSSYSDSI00) */店番号"
                        + " ,DECODE(店番号, '999001', 1000, '999002', 1000, 企業コード) AS 企業コード"
                        + " ,ROW_NUMBER() OVER (PARTITION BY 店番号"
                        + " ORDER BY 終了年月日 DESC) G_row"
                        + " FROM cmuser.PS店表示情報"
                        + " WHERE 開始年月日 <= ?) PSST"
                        + " WHERE G_row = 1) 店情報"
                        + " ON ポイント券集計.店番号 = 店情報.店番号"
                        + " GROUP BY 店情報.企業コード, ポイント券集計.店番号"
                        + " ORDER BY 店情報.企業コード, ポイント券集計.店番号"
                /* １－１～１－５．値引券発行 */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* ２－１～２－５．ECの利用Ｐ */
                , h_batdate_ym
                , h_batdate_pym
                , h_batdate_nym
                , h_batdate_ym
                , h_batdate_pym
                /* ３－１～３－４．値引実値引額 */
                , h_batdate_ym, h_batdate_ym
                , h_batdate_nym, h_batdate_nym
                , h_batdate_pym, h_batdate_pym
                , h_batdate_ym, h_batdate_ym
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicket *** (ポイント券集計情報作成)" +
                    "動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        memset(str_sql, 0x00, sizeof(str_sql));

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
        sqlca.sql = str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_stat_ins2 from ?;

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicket *** ASポイント券集計情報" +
                    " INSERT PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASポイント券集計情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicket処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /* INSERT文を実行する */
//        EXEC SQL EXECUTE sql_stat_ins2 USING
//    :h_batdate_ym,
//        /* １－１～１－５．値引券発行 */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ２－１～２－５．ECの利用Ｐ */
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd, :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//         :h_batdate_ymd_prev, :h_batdate_pym_start_date, :h_batdate_pym_end_date,
//        /* ３－１～３－４．値引実値引額 */
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date, :h_batdate_ymd,
//         :h_batdate_ym_start_date, :h_batdate_ym_end_date,
//         :h_batdate_pym_start_date, :h_batdate_pym_end_date, :h_batdate_ymd_prev,
//        /* 店情報 */
//         :h_batdate_ym_end_date;

        sqlca.restAndExecute(h_batdate_ym,
                /* １－１～１－５．値引券発行 */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev, h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev, h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ２－１～２－５．ECの利用Ｐ */
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd, h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ymd_prev, h_batdate_pym_start_date, h_batdate_pym_end_date,
                h_batdate_ymd_prev, h_batdate_pym_start_date, h_batdate_pym_end_date,
                /* ３－１～３－４．値引実値引額 */
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_ym_start_date, h_batdate_ym_end_date, h_batdate_ymd,
                h_batdate_ym_start_date, h_batdate_ym_end_date,
                h_batdate_pym_start_date, h_batdate_pym_end_date, h_batdate_ymd_prev,
                /* 店情報 */
                h_batdate_ym_end_date);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicket *** ASポイント券集計情報 INSERT " +
                    "sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------*/
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "集計対象月 = %d", h_batdate_ym);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "ASポイント券集計情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicket処理", C_const_NG, 0, 0);
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
    /*  関数名 ： cmBTtzdkB_CreatePointTicketFile                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTtzdkB_CreatePointTicketFile()                   */
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
    public int cmBTtzdkB_CreatePointTicketFile() {

        int rtn_cd;                           /* 関数戻り値                   */
        StringDto fp_name = new StringDto(4096);                    /* 出力ファイルパス名           */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);        /* 動的SQLバッファ              */
        StringDto wk_buf1 = new StringDto(256);                     /* 編集バッファ                 */
        StringDto wk_buf2 = new StringDto(256);                     /* 編集バッファ                 */
        StringDto utf8_buf = new StringDto(256);                    /* UTF8文字列格納領域           */
        StringDto sjis_buf = new StringDto(256);                    /* SJIS文字列格納領域           */
        int buf_len;                          /* 編集用                       */
        IntegerDto sjis_len = new IntegerDto();                         /* SJIS変換用                   */


        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_buf1, 0x00, sizeof(wk_buf1));
        memset(wk_buf2, 0x00, sizeof(wk_buf2));

        /* ポイント券集計表ファイルオープン */
        sprintf(fp_name, "%s/%s", out_file_dir, pt_fl_name_sjis);

//        if ((fp_pt = open(fp_name.strVal(), "w")) == null) {
        if ((fp_pt = fopen(fp_name.strVal(), SystemConstant.Shift_JIS,FileOpenType.w)).fd == C_const_NG) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fp_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile ***" +
                        " ファイルオープンERR%s\n", "");
                /*--------------------------------------------------------------------*/
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

        sprintf(utf8_buf, "%s%s\r\n", C_P_DATE,
                h_system_date);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pt);
        rtn_cd = ferror(fp_pt);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
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

        sprintf(utf8_buf, "%s%s\r\n", C_P_TIME,
                h_system_time);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pt);
        rtn_cd = ferror(fp_pt);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
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

        sprintf(utf8_buf, "%s\r\n", C_PT_HEADER);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                    0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        fwrite(sjis_buf, strlen(sjis_buf), 1, fp_pt);
        rtn_cd = ferror(fp_pt);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_pt_cnt++;

        /* ***************************************/
        /* ASポイント券集計情報（店舗別）の取得 */
        /* ***************************************/
        sprintf(wk_sql,
                "SELECT"
                        + " ASポイント券集計情報.集計対象月,"
                        + " DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード),"
                        + " ASポイント券集計情報.店番号,"
                        + " ASポイント券集計情報.発行Ｐ券ポイント数,"
                        + " ASポイント券集計情報.利用Ｐ券按分額,"
                        + " CASE DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END AS 企業名,"
                        + " NVL(RPAD(PS店表示情報.漢字店舗名称,LENGTH(PS店表示情報.漢字店舗名称)), '%s')"
                        + " FROM"
                        + " ASポイント券集計情報 LEFT JOIN "
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
                        + " ON  ASポイント券集計情報.店番号      = PS店表示情報.店番号"
                        + " WHERE"
                        + " ASポイント券集計情報.集計対象月  = ?"
                        + " ORDER BY"
                        + " ASポイント券集計情報.集計対象月,"
                        + " DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード),"
                        + " ASポイント券集計情報.店番号"
                , C_NULLSTR
                , C_NULLSTR
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile ***" +
                    " (店舗別ポイント券集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel3 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(店舗別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASポイント券集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASPTK_MISE CURSOR FOR sql_stat_sel3;
        sqlca.declare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(店舗別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "ASポイント券集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_ASPTK_MISE USING :h_batdate_ymd,
//                                  :h_batdate_ym;
        sqlca.open(h_batdate_ymd, h_batdate_ym);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(店舗別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "ASポイント券集計情報(店舗別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {

            memset(asptk_t, 0x00, sizeof(asptk_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));
            memset(h_mise_kanji, 0x00, sizeof(h_mise_kanji));

//            EXEC SQL FETCH CUR_ASPTK_MISE
//            INTO :asptk_t.shukei_ymd,
//                          :asptk_t.kyu_hansya_cd,
//                          :asptk_t.mise_no,
//                          :asptk_t.hakkou_ticket_point,
//                          :asptk_t.use_ticket_anbun_gaku,
//                          :h_kyu_hansya_name,
//                          :h_mise_kanji;
            sqlca.fetch();
            sqlca.recData(asptk_t.shukei_ymd, asptk_t.kyu_hansya_cd, asptk_t.mise_no, asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku, h_kyu_hansya_name, h_mise_kanji);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                        "(店舗別) FETCH sqlcode=[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "ASポイント券集計情報(店舗別集計)", log_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPTK_MISE;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** " +
                                    "ASポイント券集計情報(店舗別) FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*----------------------------------------------------------------*/
                }
                break;
            }

            /*--------------------------------*/
            /* ポイント券集計表に出力(店別)   */
            /*--------------------------------*/
            /************/
            /* 企業ＣＤ */
            /************/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf(wk_buf1, "\"%d\",\"", asptk_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /************/
            /* 企業名   */
            /************/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }
            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
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
            sprintf(wk_buf2, "\",\"%d\",\"", asptk_t.mise_no);

            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /************/
            /* 店舗名   */
            /************/
            /*  店舗名の末尾のスペース削除 */
            BT_Rtrim(h_mise_kanji.strDto(), strlen(h_mise_kanji));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_mise_kanji.strVal(), C_NULLSTR) == 0) {
                strcpy(h_mise_kanji, "");
            }

            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_mise_kanji.strDto());
            rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvUT2SJ", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            memcpy(wk_buf1, sjis_buf, sjis_len.arr);
            buf_len += sjis_len.arr;

            /****************************/
            /* 値引Ｐ数、値引券実値引額 */
            /****************************/
            memset(wk_buf2, 0x00, sizeof(wk_buf2));
            sprintf(wk_buf2, "\",\"%d\",\"%d\"\r\n",
                    asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku);
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* ファイル出力   */
            /*----------------*/
            fwrite(wk_buf1, buf_len, 1, fp_pt);
            rtn_cd = ferror(fp_pt);
            if (rtn_cd != C_const_OK) {

                /* ファイル書き込みエラー */
                sprintf(log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPTK_MISE;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pt_cnt++;

        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASPTK_MISE;
        sqlca.curse_close();

        /****************************************/
        /* ASポイント券集計情報（企業別）の取得 */
        /****************************************/
        sprintf(wk_sql,
                "SELECT"
                        + " ASポイント券集計情報.集計対象月,"
                        + " DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード),"
                        + " SUM(ASポイント券集計情報.発行Ｐ券ポイント数),"
                        + " SUM(ASポイント券集計情報.利用Ｐ券按分額),"
                        + " CASE DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード) WHEN 1000 THEN 'CF' WHEN 1010 THEN 'HC' WHEN 1020 THEN 'OEC' WHEN 1030 THEN 'KD' WHEN 1060 THEN 'EB' WHEN 4204 THEN '上原' WHEN 4502 THEN 'サンフォレスト' WHEN 4506 THEN 'タクボ' WHEN 4507 THEN 'フリーダム' WHEN 4020 THEN 'そうごうファーマシー' WHEN 4032 THEN 'ファインケア' WHEN 4440 THEN '漢薬堂' WHEN 4443 THEN 'まるおか薬局' WHEN 4445 THEN 'くすり屋つつじが丘' ELSE '%s' END AS 企業名"
                        + " FROM"
                        + " ASポイント券集計情報"
                        + " WHERE"
                        + " ASポイント券集計情報.集計対象月  = ?"
                        + " GROUP BY"
                        + " ASポイント券集計情報.集計対象月,"
                        + " DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード)"
                        + " ORDER BY"
                        + " ASポイント券集計情報.集計対象月,"
                        + " DECODE(ASポイント券集計情報.旧販社コード,'1040',1010,ASポイント券集計情報.旧販社コード)"
                , C_NULLSTR
        );

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** " +
                    "(企業別ポイント券集計情報取得)動的ＳＱＬ=[%s]\n", wk_sql);
            /*------------------------------------------------------------------------*/
        }

        memset(str_sql, 0x00, sizeof(str_sql));

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat_sel4 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(企業別) PREPARE sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "ASポイント券集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
//        EXEC SQL DECLARE CUR_ASPTK_HANSHA CURSOR FOR sql_stat_sel4;
        sqlca.declare();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(企業別) DECLARE CURSOR sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "DECLARE CURSOR", sqlca.sqlcode,
                    "ASポイント券集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
//        EXEC SQL OPEN CUR_ASPTK_HANSHA USING :h_batdate_ym;
        sqlca.open(h_batdate_ym);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(企業別) CURSOR OPEN sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "ASポイント券集計情報(企業別集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        for (; ; ) {

            memset(asptk_t, 0x00, sizeof(asptk_t));
            memset(h_kyu_hansya_name, 0x00, sizeof(h_kyu_hansya_name));

//            EXEC SQL FETCH CUR_ASPTK_HANSHA
//            INTO :asptk_t.shukei_ymd,
//                          :asptk_t.kyu_hansya_cd,
//                          :asptk_t.hakkou_ticket_point,
//                          :asptk_t.use_ticket_anbun_gaku,
//                          :h_kyu_hansya_name;
            sqlca.fetch();
            sqlca.recData(asptk_t.shukei_ymd, asptk_t.kyu_hansya_cd, asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku, h_kyu_hansya_name);

            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                        "(企業別) FETCH sqlcode=[%d]\n", sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                sprintf(log_format_buf, "(%d)", h_batdate_ymd);
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "ASポイント券集計情報(企業別集計)", log_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPTK_HANSHA;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** " +
                                    "ASポイント券集計情報(企業別) FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*----------------------------------------------------------------*/
                }
                break;
            }

            /*---------------------------------*/
            /* ポイント券集計表に出力(企業別)  */
            /*---------------------------------*/
            /************/
            /* 企業ＣＤ */
            /************/
            memset(wk_buf1, 0x00, sizeof(wk_buf1));
            sprintf(wk_buf1, "\"%d\",\"", asptk_t.kyu_hansya_cd);
            buf_len = strlen(wk_buf1);

            /************/
            /* 企業名   */
            /************/
            /*  企業名の末尾のスペース削除 */
            BT_Rtrim(h_kyu_hansya_name.strDto(), strlen(h_kyu_hansya_name));

            /* フェッチ用に置換した空文字列を戻す */
            if (strcmp(h_kyu_hansya_name.strVal(), C_NULLSTR) == 0) {
                strcpy(h_kyu_hansya_name, "");
            }
            /* 企業名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
            memset(sjis_buf, 0x00, sizeof(sjis_buf));
            memset(utf8_buf, 0x00, sizeof(utf8_buf));

            strcpy(utf8_buf, h_kyu_hansya_name.strDto());
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
            sprintf(wk_buf2, "\",\"%d\",\"", C_MISENO);
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /************/
            /* 店舗名   */
            /************/
            /* 店舗名のUTF8→SJIS変換(共通関数)   */
            sjis_len = new IntegerDto();
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

            /****************************/
            /* 値引Ｐ数、値引券実値引額 */
            /****************************/
            sprintf(wk_buf2, "\",\"%d\",\"%d\"\r\n",
                    asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku);
            memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
            buf_len += strlen(wk_buf2);

            /*----------------*/
            /* ファイル出力   */
            /*----------------*/
            fwrite(wk_buf1, buf_len, 1, fp_pt);
            rtn_cd = ferror(fp_pt);
            if (rtn_cd != C_const_OK) {

                /* ファイル書き込みエラー */
                sprintf(log_format_buf, "fwrite(%s)", fp_name);
                APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_ASPTK_HANSHA;
                sqlca.curse_close();
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                    /*----------------------------------------------------------------*/
                }
                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* 処理件数のカウント */
            g_pt_cnt++;

        } /* LOOP END */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_ASPTK_HANSHA;
        sqlca.curse_close();

        /**************************************/
        /* ASポイント券集計情報（全社）の取得 */
        /**************************************/
        memset(asptk_t, 0x00, sizeof(asptk_t));

//        EXEC SQL SELECT
//        ASポイント券集計情報.集計対象月,
//                SUM(ASポイント券集計情報.発行Ｐ券ポイント数),
//                SUM(ASポイント券集計情報.利用Ｐ券按分額)
//        INTO :asptk_t.shukei_ymd,
//            :asptk_t.hakkou_ticket_point,
//            :asptk_t.use_ticket_anbun_gaku
//            FROM
//        ASポイント券集計情報
//                WHERE
//        ASポイント券集計情報.集計対象月  = :h_batdate_ym
//        GROUP BY
//        ASポイント券集計情報.集計対象月
//        ORDER BY
//        ASポイント券集計情報.集計対象月;

        sqlca.sql = new StringDto("SELECT" +
                "        ASポイント券集計情報.集計対象月," +
                "                SUM(ASポイント券集計情報.発行Ｐ券ポイント数)," +
                "                SUM(ASポイント券集計情報.利用Ｐ券按分額)" +
                "            FROM" +
                "        ASポイント券集計情報" +
                "                WHERE" +
                "        ASポイント券集計情報.集計対象月  = ?" +
                "        GROUP BY" +
                "        ASポイント券集計情報.集計対象月" +
                "        ORDER BY" +
                "        ASポイント券集計情報.集計対象月");
        sqlca.restAndExecute(h_batdate_ym);
        sqlca.fetch();
        sqlca.recData(asptk_t.shukei_ymd, asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku);

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTtzdkB_CreatePointTicketFile *** ASポイント券集計情報" +
                    "(全社) SELECT sqlcode=[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------*/
        }
        /* データ無し以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {

            sprintf(log_format_buf, "(%d)", h_batdate_ymd);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "ASポイント券集計情報(全社集計)", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        /*---------------------------------*/
        /* ポイント券集計表に出力(全社)    */
        /*---------------------------------*/
        /* 会社名(,店舗名)のUTF8→SJIS変換(共通関数)   */
        sjis_len = new IntegerDto();
        memset(sjis_buf, 0x00, sizeof(sjis_buf));
        memset(utf8_buf, 0x00, sizeof(utf8_buf));

        strcpy(utf8_buf, C_MISENAME_ZENSHA);
        rtn_cd = C_ConvUT2SJ(utf8_buf, strlen(utf8_buf), sjis_buf, sjis_len);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvUT2SJ"
                    , rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        /************/
        /* 企業ＣＤ */
        /************/
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

        /****************************/
        /* 値引Ｐ数、値引券実値引額 */
        /****************************/
        sprintf(wk_buf2, "\",\"%s\",\"%s\"\r\n",
                asptk_t.hakkou_ticket_point, asptk_t.use_ticket_anbun_gaku);
        memcpy(wk_buf1, wk_buf2, strlen(wk_buf2));
        buf_len += strlen(wk_buf2);

        /*----------------*/
        /* ファイル出力   */
        /*----------------*/
        fwrite(wk_buf1, buf_len, 1, fp_pt);
        rtn_cd = ferror(fp_pt);
        if (rtn_cd != C_const_OK) {

            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s)", fp_name);
            APLOG_WT("911", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTtzdkB_CreatePointTicketFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /* 処理件数のカウント */
        g_pt_cnt++;

        /* 出力ファイルクローズ */
        fclose(fp_pt);

        /* 処理を終了する */
        return (C_const_OK);
    }
}
