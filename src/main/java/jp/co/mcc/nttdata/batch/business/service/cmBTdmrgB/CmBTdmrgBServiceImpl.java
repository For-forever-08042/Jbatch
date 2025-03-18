package jp.co.mcc.nttdata.batch.business.service.cmBTdmrgB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTdmrgB.dto.*;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： ダミー登録処理（cmBTdmrgB）
 *
 *   【処理概要】
 *       バッチ処理日付前日を基準としお買い上げ等あった顧客に対して、
 *       顧客ダミー登録、翌年年別情報初期登録処理を行う。
 *       ・顧客ダミー登録処理
 *          買上実績があり、静態情報が未登録の顧客に対して、
 *          顧客マスタのダミー情報の登録を行う。
 *       ・翌年年別情報初期登録処理
 *          翌年の年別情報に該当レコードがない顧客に対して、年別情報の
 *          初期情報の登録を行う。
 *
 *   【引数説明】
 *  -DEBUG(-debug)    :  デバッグモードでの実行（省略可）
 *                       （トレース出力機能が有効）
 *
 *   【戻り値】
 *       10     ： 正常
 *       99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 : 2012/11/14 SSI.越後谷：初版
 *      2.00 : 2013/02/19 SSI.本田  ：MM顧客属性情報のカラム変更対応
 *                                      携帯電話番号->電話番号３
 *                                      検索携帯電話番号->電話番号４
 *                                      携帯Ｅメールアドレス１->検索電話番号３
 *                                      携帯Ｅメールアドレス２->検索電話番号４
 *      2.01 : 2013/03/29 SSI.本田  ：在籍開始年月の設定変更
 *      3.00 : 2013/09/13 SSI.本田  ：チャージ実施日の更新処理を追加
 *      4.00 : 2013/12/18 SSI.本田  ：翌年年別初期登録処理を廃止し独立したバッチ
 *                                    とする
 *      5.00 : 2015/03/02 SSI.上野  ：在籍開始年月の設定変更
 *      6.00 : 2017/07/12 SSI.齋藤  ：(問626)ダミー登録止め区分初期値変更対応
 *                                    ＤＭ止め区分、Ｅメール止め区分の設定変更
 *     30.00 : 2021/02/02 NDBS.緒方  : 期間限定Ｐ対応によりリコンパイル
 *                                     (TS利用可能ポイント情報構造体/
 *                                      顧客データロック処理内容更新のため)
 *     40.00 : 2022/09/28 SSI.川内 ： MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTdmrgBServiceImpl extends CmABfuncLServiceImpl implements CmBTdmrgBService {

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */

    boolean DBG_LOG = true;   /* デバッグメッセージ出力     */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTdmrgBDto cmBTdmrgBDto = new CmBTdmrgBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int FLG_OFF = 0;                /* OFF                               */
    int FLG_ON = 1;                /* ON                                */
    int FLG_NASHI = -1;               /* 無                                */
    int FLG_ARI = 0;                /* 有                                */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";             /* デバッグスイッチ                  */
    String DEF_debug = "-debug";             /* デバッグスイッチ                  */

    int RTN_REC_SKIP = 3;            /* 次レコード取得                    */

    String DEF_DB_SD = "SD";                   /* デフォルトDB区分                  */
    String PG_NAME = "ダミー登録";           /* プログラム名称                    */
    int LOG_CNT_DR = 5000;         /* ログ出力件数                      */
    int LOG_CNT_YR = 10000;        /* ログ出力件数                      */
    int C_RANK_MAX = 100;               /* MSランク別ボーナスポイント情報数
                                                         (メモリ持ちする件数) */

    /* 半角スペース4桁 */
    String HALF_SPACE4 = "    ";
    /* 半角スペース10桁 */
    String HALF_SPACE10 = "          ";
    /* 半角スペース11桁 */
    String HALF_SPACE11 = "           ";
    /* 半角スペース15桁 */
    String HALF_SPACE15 = "               ";
    /* 半角スペース20桁 */
    String HALF_SPACE20 = "                    ";
    /* 半角スペース23桁 */
    String HALF_SPACE23 = "                       ";
    /* 半角スペース40桁 */
    String HALF_SPACE40 = "                                        ";
    /* 半角スペース60桁 */
    String HALF_SPACE60 = "                                                            ";
    /* 半角スペース80桁 */
    String HALF_SPACE80 = "                                                                                ";
    /* 2022/09/28 MCCM初版 ADD START */
    /* 半角スペース200桁 */
    String HALF_SPACE200 = "                                                                                                                                                                                                        ";
    /* 半角スペース200桁 */
    String HALF_SPACE255 = "                                                                                                                                                                                                                                                               ";
    /* 2022/09/28 MCCM初版 ADD END */
    /* 0 11桁 */
    String ZERO11 = "00000000000";

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/

    int g_batch_ymd;          /* バッチ処理日付                         */
    int g_batch_prev_ymd;     /* バッチ処理日付前日                     */
    int g_batch_prev_y;       /* バッチ処理年                           */
    int g_batch_prev_y_next;  /* バッチ処理翌年                         */
    int input_data_cnt_dr;    /* 入力データ件数(ダミー登録)             */
    int input_data_cnt_yr;    /* 入力データ件数(翌年年別登録)           */
    int g_rank_cnt;           /* MSランク別ボーナスポイント情報取得件数 */
    int msks_data_cnt;        /* MS顧客制度情報登録件数合計             */
    int mmki_data_cnt;        /* MM顧客情報登録件数合計                 */
    int mmzk_data_cnt;        /* MM顧客属性情報登録件数合計             */
    int mmkg_data_cnt;        /* MM顧客企業別属性情報登録件数合計       */
    int mscd_data_cnt;        /* MSカード情報登録件数合計               */
    int tsyp_data_cnt;        /* TSポイント年別情報登録件数合計         */
    int tsry_data_cnt;        /* TS利用可能ポイント情報登録件数合計     */

    StringDto out_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット           */

    /* ランク情報 */
    private class SRankbuf {
        int  rank_shubetsu_buf;       /*  ランク種別                        */
        int  rank_cd_buf;             /*  ランクコード                      */
        double hitsuyo_kingaku_buf;     /*  必要金額                          */
    }
    SRankbuf[] sRankbuf = new SRankbuf[C_RANK_MAX];

    /* 年月ランクＵＰ金額 */
    private class RankUpMoney {
        Double[] year_buf = new Double[10]  ;          /* 年間ランクＵＰ金額[配列]           */
        Double[] month0_buf = new Double[12];          /* 月間ランクＵＰ金額[配列]偶数年     */
        Double[] month1_buf = new Double[12];          /* 月間ランクＵＰ金額[配列]奇数年     */
    }
    RankUpMoney rankUpMoney = new RankUpMoney();

    /* 処理用日付 */
    private class ProcBatchDate {
        StringDto yyyymmdd = new StringDto(9);        /*  処理用バッチ日付    (半角)        */
        StringDto hlf_yyyy = new StringDto(5);        /*  処理用バッチ日付年  (半角)        */
        StringDto hlf_mm = new StringDto(3);        /*  処理用バッチ日付月  (半角)        */
        StringDto hlf_dd = new StringDto(3);        /*  処理用バッチ日付日  (半角)        */
        StringDto hlf_yyyymm = new StringDto(7);        /*  処理用バッチ日付年月(半角)        */
        StringDto hlf_y_bottom = new StringDto(2);        /*  年 下１桁           (半角)        */
        StringDto all_yyyymmdd = new StringDto(24);        /*  処理用バッチ日付    (全角)        */
        StringDto all_yyyy = new StringDto(12);        /*  処理用バッチ日付年  (全角)        */
        StringDto all_mm = new StringDto(6);        /*  処理用バッチ日付月  (全角)        */
        StringDto all_dd = new StringDto(6);        /*  処理用バッチ日付日  (全角)        */
        StringDto all_yyyymm = new StringDto(18);        /*  処理用バッチ日付年月(全角)        */
        StringDto all_y_bottom = new StringDto(3);        /*  年 下１桁           (全角)        */
        int int_yyyymmdd    ;        /*  処理用バッチ日付    (数値)        */
        int int_yyyy        ;        /*  処理用バッチ日付年  (数値)        */
        int int_mm          ;        /*  処理用バッチ日付月  (数値)        */
        int int_dd          ;        /*  処理用バッチ日付日  (数値)        */
        int int_yyyymm      ;        /*  処理用バッチ日付年月(数値)        */
        int int_y_bottom    ;        /*  年 下１桁           (数値)        */
    }
    /* 2022/09/28 MCCM初版 ADD START */
    ProcBatchDate gstr_today = new ProcBatchDate();          /* 処理用日付(当日)当年 */
    ProcBatchDate gstr_t_nextyear = new ProcBatchDate();     /* 処理用日付(当日)来年 */
    /* 2022/09/28 MCCM初版 ADD END */
    ProcBatchDate gstr_yesterday = new ProcBatchDate();      /* 処理用日付(前日)当年 */
    ProcBatchDate gstr_lastyear = new ProcBatchDate();       /* 処理用日付(前日)前年 */
    ProcBatchDate gstr_lastmonth = new ProcBatchDate();      /* 処理用日付(前日)前月 */

    /* 2022/09/28 MCCM初版 ADD START */
    int int_nextmonthyear;           /* 処理用日付(前日)翌月の年 */
    int int_nextmonth;               /* 処理用日付(前日)翌月 */
    /* 2022/09/28 MCCM初版 ADD END */

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
        int rtn_cd;                /* 関数戻り値                       */
        IntegerDto rtn_status = new IntegerDto();            /* 関数ステータス                   */
        int arg_chk;               /* 引数の種類チェック結果           */
        int arg_cnt;               /* 引数チェック用カウンタ           */
        StringDto arg_Work1 = new StringDto(256);        /* Work Buffer1                     */
        StringDto bat_date = new StringDto(9);           /* 取得用 バッチ処理日付            */
        StringDto bat_date_prev = new StringDto(9);      /* 取得用 バッチ処理日付前日        */
        StringDto wk_char = new StringDto(9);            /* 編集領域                         */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/

        /*  プログラム名取得処理呼び出し       */
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  バッチデバッグ開始処理呼び出し    */
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("main処理");
            /*---------------------------------------------------------------------*/
        }

        /*  開始メッセージ                     */
        APLOG_WT("102", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/

        /** 変数初期化                **/
        memset(bat_date, 0x00, sizeof(bat_date));
        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "");
            /*---------------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, 256);
            strcpy(arg_Work1, argv[arg_cnt]);
            arg_chk = FLG_ARI;

            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = %s\n", arg_Work1);
                /*----------------------------------------------------------------*/
            }
            if (0 == strcmp(arg_Work1, DEF_DEBUG)) {
                continue;
            } else if (0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
            } else {
                /* 規定外パラメータ  */
                arg_chk = FLG_NASHI;
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = %d\n", arg_chk);
                /*-------------------------------------------------------------*/
            }
            /* 規定外パラメータ  */
            if (arg_chk != FLG_ARI) {
                sprintf(out_format_buf,
                        "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", C_const_APNG, 0, 0);
                }
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
        }

        /*-------------------------------------*/
        /*  DBコネクト処理呼び出し             */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_OraDBConnect", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  バッチ処理日取得処理呼び出し       */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
            /*-------------------------------------------------------------*/
        }
        /* バッチ処理日付取得 */
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }
        /* バッチ処理日付前日取得 */
        rtn_cd = C_GetBatDate(-1, bat_date_prev, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日前日取得NG rtn= %d\n", rtn_cd);
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
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_date);
            C_DbgMsg("*** main *** バッチ処理前日取得OK [%s]\n", bat_date_prev);
            /*---------------------------------------------*/
        }

        /*  バッチ処理日付         */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date.arr, 8);
        g_batch_ymd = atoi(wk_char);

        /*  バッチ処理日付前日     */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev.arr, 8);
        g_batch_prev_ymd = atoi(wk_char);

        /*  バッチ処理年       */
        memset(wk_char, 0x00, sizeof(wk_char));
        strncpy(wk_char, bat_date_prev.arr, 4);
        g_batch_prev_y = atoi(wk_char);

        /*  バッチ処理翌年     */
        g_batch_prev_y_next = g_batch_prev_y + 1;

        /* 2022/09/28 MCCM初版 ADD START */
        /* 処理用日付（バッチ処理日付（当日））を設定 */
        // memset(&gstr_today, 0x00, sizeof(PROC_BATCH_DATE));
        /* バッチ処理日付の月が≦３の場合、前年度の日付を設定 */
        if ((g_batch_ymd / 100) % 100 <= 3) {
            sprintf(gstr_today.yyyymmdd, "%d", g_batch_ymd - 10000);
        } else {
            strncpy(gstr_today.yyyymmdd, bat_date.arr, 8);
        }
        gstr_today.int_yyyymmdd = atoi(gstr_today.yyyymmdd); /*  処理用バッチ日付    (数値) */
        memcpy(gstr_today.hlf_yyyy, gstr_today.yyyymmdd, 4); /*  処理用バッチ日付年  (半角) */
        memcpy(gstr_today.hlf_mm, gstr_today.yyyymmdd.arr.substring(4), 2); /*  処理用バッチ日付月  (半角) */
        memcpy(gstr_today.hlf_y_bottom, gstr_today.yyyymmdd.arr.substring(3), 1); /*  年 下１桁           (半角) */
        gstr_today.int_yyyy = atoi(gstr_today.hlf_yyyy); /*  処理用バッチ日付年  (数値) */
        gstr_today.int_mm = atoi(gstr_today.hlf_mm); /*  処理用バッチ日付月  (数値) */
        gstr_today.int_y_bottom = atoi(gstr_today.hlf_y_bottom); /*  年 下１桁           (数値) */
        /* 処理用日付（バッチ処理日付（当日）来年）を設定 */
        //memset(&gstr_t_nextyear, 0x00, sizeof(PROC_BATCH_DATE));
        gstr_t_nextyear.int_yyyy = (gstr_today.int_yyyy + 1); /*  処理用バッチ日付年  (数値) */
        gstr_t_nextyear.int_y_bottom = (gstr_t_nextyear.int_yyyy % 10); /*  年 下１桁           (数値) */
        /* 2022/09/28 MCCM初版 ADD END */

        /* 処理用日付（バッチ処理日付（前日））を設定 */
        //memset(&gstr_yesterday, 0x00, sizeof(PROC_BATCH_DATE));
        /* 2022/09/28 MCCM初版 MOD START */
        /*    strncpy(gstr_yesterday.yyyymmdd, bat_date_prev, 8);*/
        /* バッチ処理日付の月が≦３の場合、前年度の日付を設定 */
        if ((g_batch_ymd / 100) % 100 <= 3) {
            sprintf(gstr_yesterday.yyyymmdd, "%d", g_batch_prev_ymd - 10000);
        } else {
            strncpy(gstr_yesterday.yyyymmdd, bat_date_prev.arr, 8);
        }
        /* 2022/09/28 MCCM初版 MOD END */
        gstr_yesterday.int_yyyymmdd = atoi(gstr_yesterday.yyyymmdd); /*  処理用バッチ日付    (数値) */
        memcpy(gstr_yesterday.hlf_yyyy, gstr_yesterday.yyyymmdd, 4); /*  処理用バッチ日付年  (半角) */
        memcpy(gstr_yesterday.hlf_mm, gstr_yesterday.yyyymmdd.arr.substring(4), 2); /*  処理用バッチ日付月  (半角) */
        memcpy(gstr_yesterday.hlf_y_bottom, gstr_yesterday.yyyymmdd.arr.substring(3), 1); /*  年 下１桁           (半角) */
        gstr_yesterday.int_yyyy = atoi(gstr_yesterday.hlf_yyyy); /*  処理用バッチ日付年  (数値) */
        gstr_yesterday.int_mm = atoi(gstr_yesterday.hlf_mm); /*  処理用バッチ日付月  (数値) */
        gstr_yesterday.int_y_bottom = atoi(gstr_yesterday.hlf_y_bottom); /*  年 下１桁           (数値) */
        /* 処理用日付（バッチ処理日付（前日）前年）を設定 */
        //memset(&gstr_lastyear, 0x00, sizeof(PROC_BATCH_DATE));
        gstr_lastyear.int_yyyy = (gstr_yesterday.int_yyyy - 1); /*  処理用バッチ日付年  (数値) */
        gstr_lastyear.int_y_bottom = (gstr_lastyear.int_yyyy % 10); /*  年 下１桁           (数値) */
        /* 処理用日付（バッチ処理日付（前日）前月）を設定 */
        memset(cmBTdmrgBDto.gh_lastmonth, 0x00, sizeof(cmBTdmrgBDto.gh_lastmonth));
        memset(cmBTdmrgBDto.gh_bat_date, 0x00, sizeof(cmBTdmrgBDto.gh_bat_date));
        /* 2022/09/28 MCCM初版 MOD START */
        /*    strcpy(gh_bat_date, gstr_yesterday.yyyymmdd);*/
        strncpy(cmBTdmrgBDto.gh_bat_date, bat_date_prev.arr, 8);
        /* 2022/09/28 MCCM初版 MOD END */

        /*EXEC SQL SELECT
        TO_CHAR(ADD_MONTHS(TO_DATE(:gh_bat_date,'YYYYMMDD'), -1), 'YYYYMMDD')
        INTO  :gh_lastmonth
        FROM dual;*/
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT TO_CHAR(ADD_MONTHS(TO_DATE(?,'YYYYMMDD'), -1), 'YYYYMMDD') FROM dual";
        sqlca.sql = workSql;
        sqlca.restAndExecute(cmBTdmrgBDto.gh_bat_date);
        sqlca.fetch();
        sqlca.recData(cmBTdmrgBDto.gh_lastmonth);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "前月取得SQLが失敗しました", 0, 0, 0);
            return exit(C_const_NG);
        }
        // memset(&gstr_lastmonth, 0x00, sizeof(PROC_BATCH_DATE));
        // memset(&gstr_lastmonth, 0x00, sizeof(PROC_BATCH_DATE));
        /* 2022/09/28 MCCM初版 MOD START */
        /*    strcpy(gstr_lastmonth.yyyymmdd, gh_lastmonth);
                gstr_lastmonth.int_yyyymmdd     = atoi( gh_lastmonth )*/
        ; /*  処理用バッチ日付    (数値) */
        /* バッチ処理日付の月が≦３の場合、前年度の日付を設定 */
        if ((g_batch_ymd / 100) % 100 <= 3) {
            sprintf(gstr_lastmonth.yyyymmdd, "%d", atoi(cmBTdmrgBDto.gh_lastmonth) - 10000);
        } else {
            strcpy(gstr_lastmonth.yyyymmdd, cmBTdmrgBDto.gh_lastmonth.arr());
        }
        gstr_lastmonth.int_yyyymmdd = atoi(gstr_lastmonth.yyyymmdd); /*  処理用バッチ日付    (数値) */

        /* 2022/09/28 MCCM初版 MOD END */
        memcpy(gstr_lastmonth.hlf_yyyy, gstr_lastmonth.yyyymmdd, 4); /*  処理用バッチ日付年  (半角) */
        memcpy(gstr_lastmonth.hlf_mm, gstr_lastmonth.yyyymmdd.arr.substring(4), 2); /*  処理用バッチ日付月  (半角) */
        memcpy(gstr_lastmonth.hlf_y_bottom, gstr_lastmonth.yyyymmdd.arr.substring(3), 1); /*  年 下１桁           (半角) */
        gstr_lastmonth.int_yyyy = atoi(gstr_lastmonth.hlf_yyyy); /*  処理用バッチ日付年  (数値) */
        gstr_lastmonth.int_mm = atoi(gstr_lastmonth.hlf_mm); /*  処理用バッチ日付月  (数値) */
        gstr_lastmonth.int_y_bottom = atoi(gstr_lastmonth.hlf_y_bottom); /*  年 下１桁           (数値) */

        /* 2022/09/28 MCCM初版 ADD START */
        /* 処理用日付（バッチ処理日付（前日）翌月）の年を設定 */
        int_nextmonthyear = gstr_yesterday.int_yyyy;
        int_nextmonth = gstr_yesterday.int_mm + 1;
        if (int_nextmonth > 12) {
            int_nextmonthyear = int_nextmonthyear + 1;
            int_nextmonth = int_nextmonth - 12;
        }
        /* 2022/09/28 MCCM初版 ADD END */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            /* 2022/09/28 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(当日)     年=[%d]\n", gstr_today.int_yyyy);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(当日)     月=[%d]\n", gstr_today.int_mm);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(当日)   来年=[%d]\n", gstr_t_nextyear.int_yyyy);
            /* 2022/09/28 MCCM初版 ADD END */
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)     年=[%d]\n", gstr_yesterday.int_yyyy);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)     月=[%d]\n", gstr_yesterday.int_mm);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)   前年=[%d]\n", gstr_lastyear.int_yyyy);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月)   =[%d]\n", gstr_lastmonth.int_yyyymmdd);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月) 年=[%d]\n", gstr_lastmonth.int_yyyy);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月) 月=[%d]\n", gstr_lastmonth.int_mm);
            /* 2022/09/28 MCCM初版 ADD START */
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日翌月) 年=[%d]\n", int_nextmonthyear);
            C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日翌月) 月=[%d]\n", int_nextmonth);
            /* 2022/09/28 MCCM初版 ADD END */
            /*------------------------------------------------------------*/
        }

        /*  MSランク別ボーナスポイント情報 */
        rtn_cd = GetMsrank();
        if (rtn_cd != C_const_OK) {
            APLOG_WT("912", 0, null,
                    "ランク情報の取得に失敗しました", 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  処理件数領域を初期化               */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理件数領域を初期化%s\n", "");
            /*-------------------------------------------------------------*/
        }
        input_data_cnt_dr = 0;
        /* 2022/09/28 MCCM初版 DEL START */
        /*    input_data_cnt_yr = 0;*/
        /* 2022/09/28 MCCM初版 DEL END */
        msks_data_cnt = 0;
        mmki_data_cnt = 0;
        mmzk_data_cnt = 0;
        mmkg_data_cnt = 0;
        mscd_data_cnt = 0;
        /* 2022/09/28 MCCM初版 DEL START */
        /*    tsyp_data_cnt = 0;*/
        /* 2022/09/28 MCCM初版 DEL END */
        tsry_data_cnt = 0;

        /*-------------------------------------*/
        /*  主処理                             */
        /*-------------------------------------*/
        rtn_cd = cmBTdmrgB_main();
        if (rtn_cd != C_const_OK) {
            /* APログ出力用設定 */
            memset(out_format_buf, 0x00, sizeof(out_format_buf));
            /* 2022/09/28 MCCM初版 DEL START */
            /*        sprintf( out_format_buf, "TSポイント年別情報%d", g_batch_prev_y_next );*/
            /* 2022/09/28 MCCM初版 DEL END */

            /* APLOG出力 */
            APLOG_WT("106", 0, null, "TS利用可能ポイント情報",
                    input_data_cnt_dr, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MS顧客制度情報",
                    msks_data_cnt, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MM顧客情報",
                    mmki_data_cnt, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MM顧客属性情報",
                    mmzk_data_cnt, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MM顧客企業別属性情報",
                    mmkg_data_cnt, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "MSカード情報",
                    mscd_data_cnt, 0, 0, 0, 0);
            APLOG_WT("107", 0, null, "TS利用可能ポイント情報",
                    tsry_data_cnt, 0, 0, 0, 0);
            /* 2022/09/28 MCCM初版 DEL START */
            /*        APLOG_WT( "106", 0, NULL, "TSポイント年別情報（翌年年別登録）",
                                (char *)(long)input_data_cnt_yr, 0, 0, 0, 0);
                                APLOG_WT( "107", 0, NULL, out_format_buf,
                                (char *)(long)tsyp_data_cnt, 0, 0, 0, 0);*/
            /* 2022/09/28 MCCM初版 DEL END */

            APLOG_WT("912", 0, null,
                    "ダミー登録処理に失敗しました", 0, 0, 0, 0, 0);

            /*  ロールバック処理呼び出し     */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
            }
            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /*  終了処理                           */
        /*-------------------------------------*/

        /* APログ出力用設定 */

        /* APLOG出力 */
        APLOG_WT( "106", 0, null, "TS利用可能ポイント情報",
                input_data_cnt_dr, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MS顧客制度情報",
                (long)msks_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MM顧客情報",
                (long)mmki_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MM顧客属性情報",
                (long)mmzk_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MM顧客企業別属性情報",
                (long)mmkg_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "MSカード情報",
                (long)mscd_data_cnt, 0, 0, 0, 0);
        APLOG_WT( "107", 0, null, "TS利用可能ポイント情報",
                (long)tsry_data_cnt, 0, 0, 0, 0);

        /* コミット、開放する */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("main処理", C_const_APOK, 0, 0);
            /*---------------------------------------------*/
        }

        /*  終了メッセージ */
        APLOG_WT( "103", 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        return exit( C_const_APOK );
        /*-----main Bottom----------------------------------------------*/
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTdmrgB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTdmrgB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ダミー登録処理                                               */
    /*  【概要】                                                                  */
    /*               顧客ダミー登録、翌年年別情報初期登録処理を行う。             */
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
    public int cmBTdmrgB_main() {
        int     rtn_cd;                      /* 関数戻り値                    */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ダミー登録主処理");
            /*---------------------------------------------------------------------*/
        }

        /* 顧客ダミー登録処理 */
        rtn_cd = DummyRegist();
        if( rtn_cd != C_const_OK ){

            if(DBG_LOG) {
                /*--------------------------------------------------------*/
                C_DbgMsg("*** cmBTdmrgB_main *** 顧客ダミー登録エラー%s\n", "");
                C_DbgEnd("ダミー登録主処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT( "912", 0, null, "顧客ダミー登録に失敗しました",
                    0, 0, 0, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("ダミー登録主処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----cmBTdmrgB_main Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： DummyRegist                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  DummyRegist()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客ダミー登録                                               */
    /*  【概要】                                                                  */
    /*               MS顧客制度情報、MM顧客情報、MM顧客属性情報に                 */
    /*               ダミーデータの登録を行う。                                   */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int DummyRegist() {
        int rtn_cd = C_const_OK;                      /* 関数戻り値                    */
        IntegerDto rtn_status = new IntegerDto();                  /* 関数ステータス                */
        StringDto aplog_table_name = new StringDto(256);       /* テーブル名                    */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);   /* 動的SQLバッファ               */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客ダミー登録");
            /*---------------------------------------------------------------------*/
        }

        /*  開始メッセージ                     */
        APLOG_WT( "102", 0, null, "顧客ダミー登録処理", 0, 0, 0, 0, 0);

        /* テーブル名セット */
        memset(aplog_table_name, 0x00, sizeof(aplog_table_name));
        sprintf( aplog_table_name, "TS利用可能ポイント情報%d", g_batch_prev_y );

        memset(wk_sql, 0x00, sizeof(wk_sql));
        sprintf(wk_sql,
                "SELECT " +
                        "       to_char(T1.顧客番号, 'FM000000000000000'), " +
                        "       T1.入会企業コード, " +
                        "       T1.入会旧販社コード, " +
                        "       T1.入会店舗, " +
                        "       T1.発券企業コード, " +
                        "       T1.発券店舗, " +
                        "       T2.会員番号, " +
                        "       T2.サービス種別, " +
                        "       T2.企業コード, " +
                        "       T1.最終買上日, " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*    "       T1.チャージ実施日 "*/
                        "       T1.チャージ実施日, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "       T1.入会会社コードＭＣＣ, " +
                        "       T1.入会店舗ＭＣＣ " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        " FROM  TS利用可能ポイント情報 T1, " +
                        "       MSカード情報 T2 " +
                        " WHERE T1.最終更新日 >= %d " +
                        " AND   T1.顧客番号 = T2.顧客番号 " +
                        " AND   T2.顧客番号 <> 0 " +
                        " AND   not exists (                                  " +
                        "            SELECT    顧客番号                       " +
                        "            FROM    MS顧客制度情報 T3                " +
                        "            WHERE    T3.顧客番号 = T1.顧客番号       " +
                        "       )                                           "
                , g_batch_prev_ymd);

        /*--------------------------------------------------------------------*/
        C_DbgMsg( "*** DummyRegist*** SQL %s\n", wk_sql ) ;
        /*--------------------------------------------------------------------*/

        /* HOST変数に設定 */
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql));
        strcpy(cmBTdmrgBDto.str_sql, wk_sql);

        /* 動的SQL文の解析 */
        SqlstmDto sql_stat1 =sqlcaManager.get("DMRG_TSPR01");
        // EXEC SQL PREPARE sql_stat1 from :str_sql;
        sql_stat1.sql = wk_sql;
        sql_stat1.prepare();

        if (sql_stat1.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** DummyRegist*** 動的SQL 解析NG = %d\n",
                        sql_stat1.sqlcode);
                C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT( "902", 0, null, sql_stat1.sqlcode, wk_sql, 0, 0, 0, 0);

            /* 処理をNGで終了する */
            return (C_const_NG);
        }

        /* カーソル定義 */
        // EXEC SQL DECLARE DMRG_TSPR01 cursor for sql_stat1;
        sql_stat1.declare();
        /* カーソルオープン */
        // EXEC SQL OPEN DMRG_TSPR01;
        sql_stat1.open();

        if ( sql_stat1.sqlcode != C_const_Ora_OK ) {

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** DummyRegist *** カーソルOEPNエラー = %d\n", sql_stat1.sqlcode);
                C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            /* APLOG出力 */
            APLOG_WT("904", 0, null, "OPEN", sql_stat1.sqlcode,
                    "TS利用可能ポイント情報", cmBTdmrgBDto.str_sql, 0, 0);
            /* 処理をNGで終了する */
            return C_const_NG;
        }

        /*-------------------------------------*/
        /*  ダミー登録候補顧客ループ           */
        /*-------------------------------------*/
        while (true){

            /* 初期化 */
            memset(cmBTdmrgBDto.h_uid, 0x00, sizeof(cmBTdmrgBDto.h_uid.arr));
            //memset(ms_card, 0x00, sizeof(MS_CARD_INFO_TBL));
            //memset(&ts_riyo_kano_point, 0x00, sizeof(TS_RIYO_KANO_POINT_TBL));

            /*
            EXEC SQL FETCH DMRG_TSPR01
            INTO :h_uid,
             :ts_riyo_kano_point.nyukai_kigyo_cd,
             :ts_riyo_kano_point.nyukai_kyu_hansya_cd,
             :ts_riyo_kano_point.nyukai_tenpo,
             :ts_riyo_kano_point.hakken_kigyo_cd,
             :ts_riyo_kano_point.hakken_tenpo,
             :ms_card.kaiin_no,
             :ms_card.service_shubetsu,
             :ms_card.kigyo_cd,
             :ts_riyo_kano_point.saishu_kaiage_ymd,
            *//* 2022/09/28 MCCM初版 MOD START *//*
            *//*             :ts_riyo_kano_point.charge_jishi_ymd;*//*
             :ts_riyo_kano_point.charge_jishi_ymd,
            *//* 2022/09/28 MCCM初版 MOD END *//*
            *//* 2022/09/28 MCCM初版 ADD START *//*
             :ts_riyo_kano_point.nyukai_kaisha_cd_mcc,
             :ts_riyo_kano_point.nyukai_tenpo_mcc;
            *//* 2022/09/28 MCCM初版 ADD END *//*
            */
            sql_stat1.fetch();
            sql_stat1.recData(cmBTdmrgBDto.h_uid,
                    cmBTdmrgBDto.ts_riyo_kano_point.nyukai_kigyo_cd,
                    cmBTdmrgBDto.ts_riyo_kano_point.nyukai_kyu_hansya_cd,
                    cmBTdmrgBDto.ts_riyo_kano_point.nyukai_tenpo,
                    cmBTdmrgBDto.ts_riyo_kano_point.hakken_kigyo_cd,
                    cmBTdmrgBDto.ts_riyo_kano_point.hakken_tenpo,
                    cmBTdmrgBDto.ms_card.kaiin_no,
                    cmBTdmrgBDto.ms_card.service_shubetsu,
                    cmBTdmrgBDto.ms_card.kigyo_cd,
                    cmBTdmrgBDto.ts_riyo_kano_point.saishu_kaiage_ymd,
                    cmBTdmrgBDto.ts_riyo_kano_point.charge_jishi_ymd,
                    cmBTdmrgBDto.ts_riyo_kano_point.nyukai_kaisha_cd_mcc,
                    cmBTdmrgBDto.ts_riyo_kano_point.nyukai_tenpo_mcc);

            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** DummyRegist *** h_uid = %s\n", cmBTdmrgBDto.h_uid);
                /*-------------------------------------------------------------*/
            }

            /* データ無し以外エラーの場合処理を異常終了する */
            if ( sql_stat1.sqlcode != C_const_Ora_OK && sql_stat1.sqlcode != C_const_Ora_NOTFOUND ) {

                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** カーソルFETCHエラー = %d\n", sql_stat1.sqlcode);
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*-------------------------------------------------------------*/
                }

                /* APLOG出力 */
                APLOG_WT("904", 0, null, "FETCH", sql_stat1.sqlcode,
                        "TS利用可能ポイント情報", wk_sql, 0, 0);

                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* データ無し */
            if ( sql_stat1.sqlcode == C_const_Ora_NOTFOUND ) {
                break;
            }

            /* 処理件数をカウントアップ */
            input_data_cnt_dr++;

            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** DummyRegist *** [%d]レコード目の処理開始\n",
                        input_data_cnt_dr);
                /*----------------------------------------------------------------*/
            }

            rtn_cd = C_KdataLock(cmBTdmrgBDto.h_uid.strDto(), "1", rtn_status);

            if ( rtn_cd == C_const_NG ) {
                APLOG_WT( "903", 0, null, "C_KdataLock", rtn_cd,
                        rtn_status, 0, 0, 0);
                if (DBG_LOG) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** ロックエラー顧客=[%s]\n", cmBTdmrgBDto.h_uid);
                    /*------------------------------------------------------------*/
                }
                // EXEC SQL CLOSE DMRG_TSPR01; /* カーソルクローズ                   */
                sqlcaManager.close("DMRG_TSPR01");
                return C_const_NG;
            }

            /* 2022/09/28 MCCM初版 ADD START */
            /* 初期化 */
            cmBTdmrgBDto.card_shubetsu.arr = "0";

            /* PS会員番号体系検索 */
            /*
            EXEC SQL SELECT
                    カード種別
            INTO
            :card_shubetsu
            FROM  PS会員番号体系
            WHERE サービス種別 = :ms_card.service_shubetsu
            AND 会員番号開始 <= :ms_card.kaiin_no
            AND 会員番号終了 >= :ms_card.kaiin_no;
            */
            StringDto workSql = new StringDto();
            workSql.arr = "SELECT\n" +
                    "                    カード種別\n" +
                    "            FROM  PS会員番号体系\n" +
                    "            WHERE サービス種別 = ?\n" +
                    "            AND 会員番号開始 <= ?\n" +
                    "            AND 会員番号終了 >= ?";
            sqlca.sql = workSql;
            sqlca.restAndExecute(cmBTdmrgBDto.ms_card.service_shubetsu, cmBTdmrgBDto.ms_card.kaiin_no, cmBTdmrgBDto.ms_card.kaiin_no);
            sqlca.fetch();
            sqlca.recData(cmBTdmrgBDto.card_shubetsu);
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** DummyRegist *** PS会員番号体系取得 カード種別=%d\n",
                        cmBTdmrgBDto.card_shubetsu);
                /*----------------------------------------------------------------*/
            }

            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                        "カード種別取得SQLが失敗しました", 0, 0, 0);
                return( C_const_NG ) ;
            }
            /* 2022/09/28 MCCM初版 ADD END */

            /* MS顧客制度情報登録 */
            rtn_cd = InsertKokyakuSeido();

            if( rtn_cd != C_const_OK ){

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** MS顧客制度情報登録エラー%s\n", "");
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "912", 0, null, "MS顧客制度情報の登録に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* MS顧客制度情報登録件数カウントアップ */
            msks_data_cnt++;

            /* MM顧客情報登録 */
            rtn_cd = InsertKokyaku();

            if( rtn_cd != C_const_OK ){

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** MM顧客情報登録エラー%s\n", "");
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "912", 0, null, "MM顧客情報の登録に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* MM顧客情報登録件数カウントアップ */
            mmki_data_cnt++;

            /* MM顧客属性情報登録 */
            rtn_cd = InsertKokyakuZokusei();

            if( rtn_cd != C_const_OK ){

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** MM顧客属性情報登録エラー%s\n", "");
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "912", 0, null, "MM顧客属性情報の登録に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* MM顧客属性情報登録件数カウントアップ */
            mmzk_data_cnt++;

            /* MSカード情報更新 */
            rtn_cd = UpdateCard();

            if( rtn_cd != C_const_OK ){

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** MSカード情報更新エラー%s\n", "");
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "912", 0, null, "MSカード情報の更新に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* MSカード情報登録件数カウントアップ */
            mscd_data_cnt++;

            /* MM顧客企業別属性情報登録 */
            rtn_cd = InsertKokyakuKigyobetu();

            if( rtn_cd != C_const_OK ){

                if (DBG_LOG) {
                    /*--------------------------------------------------------*/
                    C_DbgMsg("*** DummyRegist *** MM顧客企業別属性情報登録エラー%s\n", "");
                    C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                    /*--------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "912", 0, null, "MM顧客企業別属性情報の登録に失敗しました",
                        0, 0, 0, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE DMRG_TSPR01;
                sqlcaManager.close("DMRG_TSPR01");

                /* 処理をNGで終了する */
                return C_const_NG;
            }

            /* MM顧客企業別属性情報登録件数カウントアップ */
            mmkg_data_cnt++;

            /* チャージ実施日が０の場合、初回取引日をチャージ実施日として設定 */
            /* 申込書記入->買上->翌日チャージ時の静態登録の案内を抑止するため */
            if ( cmBTdmrgBDto.ts_riyo_kano_point.charge_jishi_ymd.intVal() == 0 ) {
                /* TS利用可能ポイント情報更新 */
                rtn_cd = UpdateRiyoKano();

                if( rtn_cd != C_const_OK ){

                    if (DBG_LOG) {
                        /*--------------------------------------------------------*/
                        C_DbgMsg("*** DummyRegist *** TS利用可能ポイント情報更新エラー%s\n", "");
                        C_DbgEnd("顧客ダミー登録", C_const_NG, 0, 0);
                        /*--------------------------------------------------------*/
                    }
                    /* APLOG出力 */
                    APLOG_WT( "912", 0, null, "TS利用可能ポイント情報の更新に失敗しました",
                            0, 0, 0, 0, 0);
                    /* カーソルクローズ */
                    // EXEC SQL CLOSE DMRG_TSPR01;
                    sqlcaManager.close("DMRG_TSPR01");

                    /* 処理をNGで終了する */
                    return C_const_NG;
                }

                /* TS利用可能ポイント情報登録件数カウントアップ */
                tsry_data_cnt++;
            }

            /* コミットする */
            // EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* 処理件数（途中経過）を出力 */
            if( (input_data_cnt_dr % LOG_CNT_DR) == 0 ){

                /* APLOG出力 */
                APLOG_WT( "106", 0, null, "TS利用可能ポイント情報(ダミー登録）",
                        input_data_cnt_dr, 0, 0, 0, 0);
                APLOG_WT( "107", 0, null, "MS顧客制度情報",
                        msks_data_cnt, 0, 0, 0, 0);
                APLOG_WT( "107", 0, null, "MM顧客情報",
                        mmki_data_cnt, 0, 0, 0, 0);
                APLOG_WT( "107", 0, null, "MM顧客属性情報",
                        mmzk_data_cnt, 0, 0, 0, 0);
                APLOG_WT( "107", 0, null, "MSカード情報",
                        mscd_data_cnt, 0, 0, 0, 0);
                APLOG_WT( "107", 0, null, "MM顧客企業別属性情報",
                        mmkg_data_cnt, 0, 0, 0, 0);
            }
        }
        /* カーソルクローズ */
        // EXEC SQL CLOSE DMRG_TSPR01;
        sqlcaManager.close("DMRG_TSPR01");

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客ダミー登録", rtn_cd, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
        /*-----DummyRegist Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakuSeido                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakuSeido()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客制度情報登録処理                                         */
    /*  【概要】                                                                  */
    /*               MS顧客制度情報にダミーデータを登録する。                     */
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
    public int InsertKokyakuSeido() {
        /* 2023/01/09 MCCM初版 DEL START */
        /*    int     rtn_cd;*/             /* 関数戻り値                       */
        /* 2023/01/09 MCCM初版 DEL END */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客制度情報登録処理");
            /*---------------------------------------------------------------------*/
        }

        /* 顧客制度情報構造体編集 */
        /* 初期化 */
        // memset(ms_kokyaku_sedo, 0x00, sizeof(ms_kokyaku_sedo) );

        /* 顧客番号               */
        memcpy(cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_no, cmBTdmrgBDto.h_uid.arr(), sizeof(cmBTdmrgBDto.h_uid.arr) );
        cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_no.len = strlen(cmBTdmrgBDto.h_uid);
        cmBTdmrgBDto.ms_kokyaku_sedo.tanjo_m.arr = 0                   ; /* 誕生月                 */
        cmBTdmrgBDto.ms_kokyaku_sedo.entry.arr = 0                     ; /* エントリー             */
        cmBTdmrgBDto.ms_kokyaku_sedo.senior.arr = 0                    ; /* シニア                 */
        /*--- ランクＵＰ情報取得処理 ---*/
                                                    /* 年次ランクコード１～９、
                                                       月次ランクコード００１～０１２、
                                                       月次ランクコード１０１～１１２ */
        /* 2023/01/09 MCCM初版 MOD START */
/*    rtn_cd = getRankUpMoney();
    if (rtn_cd == C_const_NG) {*/
        /* error */
/*        APLOG_WT("903", 0, NULL, "getRankUpMoney", (char *)(long)rtn_cd, 0, 0, 0, 0);
if(DBG_LOG) {*/
        /*------------------------------------------------------------*/
/*        C_DbgMsg("*** cmBTleavB_main *** getRankUpMoney ret=%d\n", rtn_cd);
        C_DbgEnd("顧客制度情報登録処理", C_const_NG, 0, 0);*/
        /*------------------------------------------------------------*/
        /*}*/
        /* 処理を終了する */
/*        return C_const_NG;
    }*/
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_0.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_0.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_1.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_2.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_3.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_4.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_5.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_6.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_7.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_8.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_9.arr = 0           ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_001.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_002.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_003.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_004.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_005.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_006.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_007.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_008.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_009.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_010.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_011.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_012.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_101.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_102.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_103.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_104.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_105.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_106.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_107.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_108.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_109.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_110.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_111.arr = 0        ;
        cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_112.arr = 0        ;
        /* 2023/01/09 MCCM初版 MOD END */
        cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_1.arr = 0               ; /* サークルＩＤ１         */
        cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_2.arr = 0               ; /* サークルＩＤ２         */
        cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_3.arr = 0               ; /* サークルＩＤ３         */
        cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_4.arr = 0               ; /* サークルＩＤ４         */
        cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_5.arr = 0               ; /* サークルＩＤ５         */
        /* 在籍開始年月           */
        /* 2015/03/02  最終買上日とチャージ実施日より0以外の最小値を取得 */
        if ( cmBTdmrgBDto.ts_riyo_kano_point.saishu_kaiage_ymd.intVal() != 0 ) {
            /* 最終買上日≠0 →　初回買上実施 …最終買上日設定  */
            cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym.arr = cmBTdmrgBDto.ts_riyo_kano_point.saishu_kaiage_ymd.intVal() / 100 ;
        } else {
            /* 最終買上日＝0 チャージ実施日≠0 →　初回チャージ実施 …ャージ実施日設定 */
            if ( cmBTdmrgBDto.ts_riyo_kano_point.charge_jishi_ymd.intVal() != 0 ) {
                /* 最終買上日＝0 チャージ実施日≠0 →　初回チャージ実施 …チャージ実施日設定 */
                cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym.arr = cmBTdmrgBDto.ts_riyo_kano_point.charge_jishi_ymd.intVal() / 100 ;
            } else {
                /* 最終買上日＝0 チャージ実施日＝0 →　運用上あり得ないパターン  */
                /* バッチ処理日付前日の年月 */
                cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym.arr = g_batch_prev_ymd / 100;
            }
        }
        if ( cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym.intVal() < 201304 ) {
            cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym.arr = 201304;
        }
        if(DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("最終買上日     =[%d]\n", cmBTdmrgBDto.ts_riyo_kano_point.saishu_kaiage_ymd);
            C_DbgMsg("チャージ実施日 =[%d]\n", cmBTdmrgBDto.ts_riyo_kano_point.charge_jishi_ymd);
            C_DbgMsg("==>在籍開始年月=[%d]\n", cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym);
            /*------------------------------------------------------------*/
        }
        cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_1.arr = 0; /* 出産クーポン発行可否１ */
        cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_2.arr = 0; /* 出産クーポン発行可否２ */
        cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_3.arr = 0; /* 出産クーポン発行可否３ */
        cmBTdmrgBDto.ms_kokyaku_sedo.shain_kbn.arr = 0                 ; /* 社員区分               */
        cmBTdmrgBDto.ms_kokyaku_sedo.portal_kaiin_flg.arr = 0          ; /* ポータル会員フラグ     */
        cmBTdmrgBDto.ms_kokyaku_sedo.ec_kaiin_flg.arr = 0              ; /* ＥＣ会員フラグ         */
        cmBTdmrgBDto.ms_kokyaku_sedo.mobile_kaiin_flg.arr = 0          ; /* モバイル会員フラグ     */
        cmBTdmrgBDto.ms_kokyaku_sedo.denwa_no_toroku_flg.arr = 0       ; /* 電話番号登録フラグ     */
        cmBTdmrgBDto.ms_kokyaku_sedo.setai_torikomizumi_flg.arr = 0    ; /* 静態取込済みフラグ     */
        /* 2022/09/28 MCCM初版 MOD START */
        /*    ms_kokyaku_sedo.kazoku_id = 0*/             ; /* 家族ＩＤ               */
        memcpy( cmBTdmrgBDto.ms_kokyaku_sedo.kazoku_id, "0", 1 );
        cmBTdmrgBDto.ms_kokyaku_sedo.kazoku_id.len = 1;
        /* 2022/09/28 MCCM初版 MOD END */
        cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_kigyo_cd.arr = 0            ; /* 作業企業コード         */
        cmBTdmrgBDto.ms_kokyaku_sedo.sagyosha_id.arr = (double) 0               ; /* 作業者ＩＤ             */
        cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_ymd.arr = 0                 ; /* 作業年月日             */
        cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_hms.arr = 0                 ; /* 作業時刻               */
        cmBTdmrgBDto.ms_kokyaku_sedo.batch_koshin_ymd.arr = g_batch_ymd; /* バッチ更新日           */
        cmBTdmrgBDto.ms_kokyaku_sedo.saishu_koshin_ymd.arr = g_batch_ymd;/* 最終更新日             */
        /* 最終更新プログラムＩＤ */
        strncpy( cmBTdmrgBDto.ms_kokyaku_sedo.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );
        /* 2022/09/28 MCCM初版 ADD START */
        cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_status.arr = 1            ; /* 顧客ステータス         */
        cmBTdmrgBDto.ms_kokyaku_sedo.kaiin_shikaku_kbn.arr = 1         ; /* 会員資格区分           */
        /* グローバル会員フラグ   */
        strncpy( cmBTdmrgBDto.ms_kokyaku_sedo.global_kaiin_flg, "0", 1 );
        cmBTdmrgBDto.ms_kokyaku_sedo.line_connect_jokyo.arr = 0        ; /* ＬＩＮＥコネクト状況   */
        cmBTdmrgBDto.ms_kokyaku_sedo.mcc_seido_kyodaku_flg.arr = 1     ; /* ＭＣＣ制度許諾フラグ   */
        cmBTdmrgBDto.ms_kokyaku_sedo.mcc_seido_kyodaku_koshinsha.arr = 4;/* ＭＣＣ制度許諾更新者   */
        cmBTdmrgBDto.ms_kokyaku_sedo.corporate_kaiin_flg.arr = 0       ; /* コーポレート会員フラグ */
        /* 属性管理主体システム   */
        if (cmBTdmrgBDto.card_shubetsu.arr.equals("301") || cmBTdmrgBDto.card_shubetsu.arr.equals("302")
                || cmBTdmrgBDto.card_shubetsu.arr.equals("504") || cmBTdmrgBDto.card_shubetsu.arr.equals("505")
                || cmBTdmrgBDto.card_shubetsu.arr.equals("998")) {
            cmBTdmrgBDto.ms_kokyaku_sedo.zokusei_kanri_shutai_system.arr = 1;
        } else {
            cmBTdmrgBDto.ms_kokyaku_sedo.zokusei_kanri_shutai_system.arr = 2;
        }
        /* 2022/09/28 MCCM初版 ADD END */

        /* SQL文 */
        sprintf(wk_sql,
                "INSERT INTO MS顧客制度情報 " +
                        "  ( " +
                        "        顧客番号, " +
                        "        誕生月, " +
                        "        エントリー, " +
                        "        シニア, " +
                        "        年次ランクコード０, " +
                        "        年次ランクコード１, " +
                        "        年次ランクコード２, " +
                        "        年次ランクコード３, " +
                        "        年次ランクコード４, " +
                        "        年次ランクコード５, " +
                        "        年次ランクコード６, " +
                        "        年次ランクコード７, " +
                        "        年次ランクコード８, " +
                        "        年次ランクコード９, " +
                        "        月次ランクコード００１, " +
                        "        月次ランクコード００２, " +
                        "        月次ランクコード００３, " +
                        "        月次ランクコード００４, " +
                        "        月次ランクコード００５, " +
                        "        月次ランクコード００６, " +
                        "        月次ランクコード００７, " +
                        "        月次ランクコード００８, " +
                        "        月次ランクコード００９, " +
                        "        月次ランクコード０１０, " +
                        "        月次ランクコード０１１, " +
                        "        月次ランクコード０１２, " +
                        "        月次ランクコード１０１, " +
                        "        月次ランクコード１０２, " +
                        "        月次ランクコード１０３, " +
                        "        月次ランクコード１０４, " +
                        "        月次ランクコード１０５, " +
                        "        月次ランクコード１０６, " +
                        "        月次ランクコード１０７, " +
                        "        月次ランクコード１０８, " +
                        "        月次ランクコード１０９, " +
                        "        月次ランクコード１１０, " +
                        "        月次ランクコード１１１, " +
                        "        月次ランクコード１１２, " +
                        "        サークルＩＤ１, " +
                        "        サークルＩＤ２, " +
                        "        サークルＩＤ３, " +
                        "        サークルＩＤ４, " +
                        "        サークルＩＤ５, " +
                        "        在籍開始年月, " +
                        "        出産クーポン発行可否１, " +
                        "        出産クーポン発行可否２, " +
                        "        出産クーポン発行可否３, " +
                        "        社員区分, " +
                        "        ポータル会員フラグ, " +
                        "        ＥＣ会員フラグ, " +
                        "        モバイル会員フラグ, " +
                        "        電話番号登録フラグ, " +
                        "        静態取込済みフラグ, " +
                        "        家族ＩＤ, " +
                        "        作業企業コード, " +
                        "        作業者ＩＤ, " +
                        "        作業年月日, " +
                        "        作業時刻, " +
                        "        バッチ更新日, " +
                        "        最終更新日, " +
                        "        最終更新日時, " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "        最終更新プログラムＩＤ "*/
                        "        最終更新プログラムＩＤ, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "        顧客ステータス, " +
                        "        会員資格区分, " +
                        "        グローバル会員フラグ, " +
                        "        ＬＩＮＥコネクト状況, " +
                        "        ＭＣＣ制度許諾フラグ, " +
                        "        ＭＣＣ制度許諾更新者, " +
                        "        ＭＣＣ制度許諾更新日時, " +
                        "        コーポレート会員フラグ, " +
                        "        属性管理主体システム " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "  ) " +
                        " VALUES " +
                        "  ( " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         sysdate(), " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "         :v61 "*/
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         sysdate(), " +
                        "         ?, " +
                        "         ? " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "     ) ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_ins_msks from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertKokyaku *** SQL=[%s]\n", wk_sql);
                C_DbgMsg("*** InsertKokyaku *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", wk_sql, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_ins_msks using
        :ms_kokyaku_sedo.kokyaku_no                 ,
                                :ms_kokyaku_sedo.tanjo_m                    ,
                                :ms_kokyaku_sedo.entry                      ,
                                :ms_kokyaku_sedo.senior                     ,
                                :ms_kokyaku_sedo.nenji_rank_cd_0            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_1            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_2            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_3            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_4            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_5            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_6            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_7            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_8            ,
                                :ms_kokyaku_sedo.nenji_rank_cd_9            ,
                                :ms_kokyaku_sedo.getuji_rank_cd_001         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_002         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_003         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_004         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_005         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_006         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_007         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_008         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_009         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_010         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_011         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_012         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_101         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_102         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_103         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_104         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_105         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_106         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_107         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_108         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_109         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_110         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_111         ,
                                :ms_kokyaku_sedo.getuji_rank_cd_112         ,
                                :ms_kokyaku_sedo.circle_id_1                ,
                                :ms_kokyaku_sedo.circle_id_2                ,
                                :ms_kokyaku_sedo.circle_id_3                ,
                                :ms_kokyaku_sedo.circle_id_4                ,
                                :ms_kokyaku_sedo.circle_id_5                ,
                                :ms_kokyaku_sedo.zaiseki_kaishi_ym          ,
                                :ms_kokyaku_sedo.shussan_coupon_hakko_flg_1 ,
                                :ms_kokyaku_sedo.shussan_coupon_hakko_flg_2 ,
                                :ms_kokyaku_sedo.shussan_coupon_hakko_flg_3 ,
                                :ms_kokyaku_sedo.shain_kbn                  ,
                                :ms_kokyaku_sedo.portal_kaiin_flg           ,
                                :ms_kokyaku_sedo.ec_kaiin_flg               ,
                                :ms_kokyaku_sedo.mobile_kaiin_flg           ,
                                :ms_kokyaku_sedo.denwa_no_toroku_flg        ,
                                :ms_kokyaku_sedo.setai_torikomizumi_flg     ,
                                :ms_kokyaku_sedo.kazoku_id                  ,
                                :ms_kokyaku_sedo.sagyo_kigyo_cd             ,
                                :ms_kokyaku_sedo.sagyosha_id                ,
                                :ms_kokyaku_sedo.sagyo_ymd                  ,
                                :ms_kokyaku_sedo.sagyo_hms                  ,
                                :ms_kokyaku_sedo.batch_koshin_ymd           ,
                                :ms_kokyaku_sedo.saishu_koshin_ymd          ,
        *//* 2022/09/28 MCCM初版 MOD START *//*
        *//*                                :ms_kokyaku_sedo.saishu_koshin_programid    ;*//*
                                :ms_kokyaku_sedo.saishu_koshin_programid    ,
        *//* 2022/09/28 MCCM初版 MOD END *//*
        *//* 2022/09/28 MCCM初版 ADD START *//*
                                :ms_kokyaku_sedo.kokyaku_status             ,
                                :ms_kokyaku_sedo.kaiin_shikaku_kbn          ,
                                :ms_kokyaku_sedo.global_kaiin_flg           ,
                                :ms_kokyaku_sedo.line_connect_jokyo         ,
                                :ms_kokyaku_sedo.mcc_seido_kyodaku_flg      ,
                                :ms_kokyaku_sedo.mcc_seido_kyodaku_koshinsha,
                                :ms_kokyaku_sedo.corporate_kaiin_flg        ,
                                :ms_kokyaku_sedo.zokusei_kanri_shutai_system;
        *//* 2022/09/28 MCCM初版 ADD END *//*
        */
        sqlca.query(cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_no,
                cmBTdmrgBDto.ms_kokyaku_sedo.tanjo_m                    ,
                cmBTdmrgBDto.ms_kokyaku_sedo.entry                      ,
                cmBTdmrgBDto.ms_kokyaku_sedo.senior                     ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_0            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_1            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_2            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_3            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_4            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_5            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_6            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_7            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_8            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.nenji_rank_cd_9            ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_001         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_002         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_003         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_004         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_005         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_006         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_007         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_008         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_009         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_010         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_011         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_012         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_101         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_102         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_103         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_104         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_105         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_106         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_107         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_108         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_109         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_110         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_111         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.getuji_rank_cd_112         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_1                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_2                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_3                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_4                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.circle_id_5                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.zaiseki_kaishi_ym          ,
                cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_1 ,
                cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_2 ,
                cmBTdmrgBDto.ms_kokyaku_sedo.shussan_coupon_hakko_flg_3 ,
                cmBTdmrgBDto.ms_kokyaku_sedo.shain_kbn                  ,
                cmBTdmrgBDto.ms_kokyaku_sedo.portal_kaiin_flg           ,
                cmBTdmrgBDto.ms_kokyaku_sedo.ec_kaiin_flg               ,
                cmBTdmrgBDto.ms_kokyaku_sedo.mobile_kaiin_flg           ,
                cmBTdmrgBDto.ms_kokyaku_sedo.denwa_no_toroku_flg        ,
                cmBTdmrgBDto.ms_kokyaku_sedo.setai_torikomizumi_flg     ,
                cmBTdmrgBDto.ms_kokyaku_sedo.kazoku_id                  ,
                cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_kigyo_cd             ,
                cmBTdmrgBDto.ms_kokyaku_sedo.sagyosha_id                ,
                cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_ymd                  ,
                cmBTdmrgBDto.ms_kokyaku_sedo.sagyo_hms                  ,
                cmBTdmrgBDto.ms_kokyaku_sedo.batch_koshin_ymd           ,
                cmBTdmrgBDto.ms_kokyaku_sedo.saishu_koshin_ymd          ,
                cmBTdmrgBDto.ms_kokyaku_sedo.saishu_koshin_programid    ,
                cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_status             ,
                cmBTdmrgBDto.ms_kokyaku_sedo.kaiin_shikaku_kbn          ,
                cmBTdmrgBDto.ms_kokyaku_sedo.global_kaiin_flg           ,
                cmBTdmrgBDto.ms_kokyaku_sedo.line_connect_jokyo         ,
                cmBTdmrgBDto.ms_kokyaku_sedo.mcc_seido_kyodaku_flg      ,
                cmBTdmrgBDto.ms_kokyaku_sedo.mcc_seido_kyodaku_koshinsha,
                cmBTdmrgBDto.ms_kokyaku_sedo.corporate_kaiin_flg        ,
                cmBTdmrgBDto.ms_kokyaku_sedo.zokusei_kanri_shutai_system);

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertKokyakuSeido *** MS顧客制度情報INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("顧客制度情報登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.ms_kokyaku_sedo.kokyaku_no.arr );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "INSERT", sqlca.sqlcode,
                    "MS顧客制度情報", wk_sql, 0, 0);

            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客制度情報登録処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertKokyakuSeido Bottom----------------------------------------------*/
    }

    @Override
    public int InsertKokyaku() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客情報登録処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql.arr));
        // memset((char *)&mm_kokyaku, 0x00, sizeof(mm_kokyaku) );

        /* 顧客情報構造体編集 */
        /* 顧客番号               */
        memcpy( cmBTdmrgBDto.mm_kokyaku.kokyaku_no, cmBTdmrgBDto.h_uid, sizeof(cmBTdmrgBDto.h_uid.arr) );
        cmBTdmrgBDto.mm_kokyaku.kokyaku_no.len = strlen(cmBTdmrgBDto.h_uid) ;
        cmBTdmrgBDto.mm_kokyaku.kyumin_flg.arr = 0                 ; /* 休眠フラグ             */
        /* 顧客名称               */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kokyaku_mesho, HALF_SPACE40 );
        /* 顧客カナ名称           */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kokyaku_kana_mesho, HALF_SPACE20 );
        /* 2022/09/28 MCCM初版 ADD START */
        /* 顧客名字               */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kokyaku_myoji, HALF_SPACE40 );
        /* 顧客名前               */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kokyaku_name, HALF_SPACE40 );
        /* カナ顧客名字           */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kana_kokyaku_myoji, HALF_SPACE40 );
        /* カナ顧客名前           */
        strcpy(cmBTdmrgBDto.mm_kokyaku.kana_kokyaku_name, HALF_SPACE40 );
        /* 2022/09/28 MCCM初版 ADD END */
        cmBTdmrgBDto.mm_kokyaku.nenre.arr = 0                      ; /* 年齢                   */
        cmBTdmrgBDto.mm_kokyaku.tanjo_y.arr = 0                    ; /* 誕生年                 */
        cmBTdmrgBDto.mm_kokyaku.tanjo_m.arr = 0                    ; /* 誕生月                 */
        cmBTdmrgBDto.mm_kokyaku.tanjo_d.arr = 0                    ; /* 誕生日                 */
        cmBTdmrgBDto.mm_kokyaku.sebetsu.arr = 0                    ; /* 性別                   */
        cmBTdmrgBDto.mm_kokyaku.konin.arr = 0                      ; /* 婚姻                   */
        cmBTdmrgBDto.mm_kokyaku.nyukai_kigyo_cd  = cmBTdmrgBDto.ts_riyo_kano_point.nyukai_kigyo_cd; /* 入会企業コード         */
        cmBTdmrgBDto.mm_kokyaku.nyukai_tenpo = cmBTdmrgBDto.ts_riyo_kano_point.nyukai_tenpo;        /* 入会店舗               */
        cmBTdmrgBDto.mm_kokyaku.hakken_kigyo_cd = cmBTdmrgBDto.ts_riyo_kano_point.hakken_kigyo_cd;  /* 発券企業コード         */
        cmBTdmrgBDto.mm_kokyaku.hakken_tenpo = cmBTdmrgBDto.ts_riyo_kano_point.hakken_tenpo;        /* 発券店舗               */
        cmBTdmrgBDto.mm_kokyaku.shain_kbn.arr = 0                  ; /* 社員区分               */
        cmBTdmrgBDto.mm_kokyaku. portal_nyukai_ymd.arr = 0         ; /* ポータル入会年月日     */
        cmBTdmrgBDto.mm_kokyaku. portal_taikai_ymd.arr = 0         ; /* ポータル退会年月日     */
        cmBTdmrgBDto.mm_kokyaku. sagyo_kigyo_cd.arr = 0            ; /* 作業企業コード         */
        cmBTdmrgBDto.mm_kokyaku.sagyosha_id.arr = (double) 0                ; /* 作業者ＩＤ             */
        cmBTdmrgBDto.mm_kokyaku.sagyo_ymd.arr = 0                  ; /* 作業年月日             */
        cmBTdmrgBDto.mm_kokyaku.sagyo_hms.arr = 0                  ; /* 作業時刻               */
        cmBTdmrgBDto.mm_kokyaku.batch_koshin_ymd.arr = g_batch_ymd ; /* バッチ更新日           */
        cmBTdmrgBDto.mm_kokyaku.saishu_setai_ymd.arr = 0           ; /* 最終静態更新日         */
        cmBTdmrgBDto.mm_kokyaku.saishu_setai_hms.arr = 0           ; /* 最終静態更新時刻       */
        cmBTdmrgBDto.mm_kokyaku.saishu_koshin_ymd.arr = g_batch_ymd; /* 最終更新日             */
        /* 最終更新プログラムＩＤ */
        strncpy( cmBTdmrgBDto.mm_kokyaku.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );
        /* 2022/09/28 MCCM初版 ADD START */
        cmBTdmrgBDto.mm_kokyaku.kokyaku_status.arr = 1             ; /* 顧客ステータス         */
        cmBTdmrgBDto.mm_kokyaku.nyukai_kaisha_cd_mcc = cmBTdmrgBDto.ts_riyo_kano_point.nyukai_kaisha_cd_mcc; /* 入会会社コードＭＣＣ   */
        cmBTdmrgBDto.mm_kokyaku.nyukai_tenpo_mcc = cmBTdmrgBDto.ts_riyo_kano_point.nyukai_tenpo_mcc;         /* 入会店舗ＭＣＣ         */
        cmBTdmrgBDto.mm_kokyaku.senior.arr = 0                     ; /* シニア                 */
        /* 属性管理主体システム   */
        if (cmBTdmrgBDto.card_shubetsu.arr.equals("301") || cmBTdmrgBDto.card_shubetsu.arr.equals("302")
                || cmBTdmrgBDto.card_shubetsu.arr.equals("504") || cmBTdmrgBDto.card_shubetsu.arr.equals("505")
                || cmBTdmrgBDto.card_shubetsu.arr.equals("998")) {
            cmBTdmrgBDto.mm_kokyaku.zokusei_kanri_shutai_system.arr = 1;
        } else {
            cmBTdmrgBDto.mm_kokyaku.zokusei_kanri_shutai_system.arr = 2;
        }
        cmBTdmrgBDto.mm_kokyaku.push_tsuchikyoka_flg.arr = 0       ; /* プッシュ通知許可フラグ */
        cmBTdmrgBDto.mm_kokyaku.mail_address_1_soshin_flg.arr = 0  ; /* メールアドレス１送信フラグ */
        cmBTdmrgBDto.mm_kokyaku.mail_address_2_soshin_flg.arr = 0  ; /* メールアドレス２送信フラグ */
        cmBTdmrgBDto.mm_kokyaku.mail_address_3_soshin_flg.arr = 0  ; /* メールアドレス３送信フラグ */
        /* 2022/09/28 MCCM初版 ADD END */

        /* SQL文 */
        sprintf(wk_sql,
                "INSERT INTO MM顧客情報 " +
                        "    ( " +
                        "         顧客番号, " +
                        "         休眠フラグ, " +
                        "         顧客名称, " +
                        "         顧客カナ名称, " +
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         顧客名字, " +
                        "         顧客名前, " +
                        "         カナ顧客名字, " +
                        "         カナ顧客名前, " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "         年齢, " +
                        "         誕生年, " +
                        "         誕生月, " +
                        "         誕生日, " +
                        "         性別, " +
                        "         婚姻, " +
                        "         入会企業コード, " +
                        "         入会店舗, " +
                        "         発券企業コード, " +
                        "         発券店舗, " +
                        "         社員区分, " +
                        "         ポータル入会年月日 , " +
                        "         ポータル退会年月日, " +
                        "         作業企業コード, " +
                        "         作業者ＩＤ, " +
                        "         作業年月日, " +
                        "         作業時刻, " +
                        "         バッチ更新日, " +
                        "         最終静態更新日, " +
                        "         最終静態更新時刻, " +
                        "         最終更新日, " +
                        "         最終更新日時, " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "         最終更新プログラムＩＤ "*/
                        "         最終更新プログラムＩＤ, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         顧客ステータス, " +
                        "         入会会社コードＭＣＣ, " +
                        "         入会店舗ＭＣＣ, " +
                        "         シニア, " +
                        "         属性管理主体システム, " +
                        "         プッシュ通知許可フラグ, " +
                        "         メールアドレス１送信フラグ, " +
                        "         メールアドレス２送信フラグ, " +
                        "         メールアドレス３送信フラグ " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "     ) " +
                        "   VALUES " +
                        "    ( " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "         sysdate(), " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "         :v26 "*/
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ? " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "     ) ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_ins_mmki from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertKokyaku *** SQL=[%s]\n", wk_sql);
                C_DbgMsg("*** InsertKokyaku *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgEnd("顧客情報登録処理", C_const_NG, 0, 0);
                /*-------------------------------------------------------------*/
            }
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MM顧客情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_ins_mmki using
        :mm_kokyaku.kokyaku_no              ,
                                :mm_kokyaku.kyumin_flg              ,
                                :mm_kokyaku.kokyaku_mesho           ,
                                :mm_kokyaku.kokyaku_kana_mesho      ,
        *//* 2022/09/28 MCCM初版 ADD START *//*
                                :mm_kokyaku.kokyaku_myoji           ,
                                :mm_kokyaku.kokyaku_name            ,
                                :mm_kokyaku.kana_kokyaku_myoji      ,
                                :mm_kokyaku.kana_kokyaku_name       ,
        *//* 2022/09/28 MCCM初版 ADD END *//*
                                :mm_kokyaku.nenre                   ,
                                :mm_kokyaku.tanjo_y                 ,
                                :mm_kokyaku.tanjo_m                 ,
                                :mm_kokyaku.tanjo_d                 ,
                                :mm_kokyaku.sebetsu                 ,
                                :mm_kokyaku.konin                   ,
                                :mm_kokyaku.nyukai_kigyo_cd         ,
                                :mm_kokyaku.nyukai_tenpo            ,
                                :mm_kokyaku.hakken_kigyo_cd         ,
                                :mm_kokyaku.hakken_tenpo            ,
                                :mm_kokyaku.shain_kbn               ,
                                :mm_kokyaku.portal_nyukai_ymd       ,
                                :mm_kokyaku.portal_taikai_ymd       ,
                                :mm_kokyaku.sagyo_kigyo_cd          ,
                                :mm_kokyaku.sagyosha_id             ,
                                :mm_kokyaku.sagyo_ymd               ,
                                :mm_kokyaku.sagyo_hms               ,
                                :mm_kokyaku.batch_koshin_ymd        ,
                                :mm_kokyaku.saishu_setai_ymd        ,
                                :mm_kokyaku.saishu_setai_hms        ,
                                :mm_kokyaku.saishu_koshin_ymd       ,
        *//* 2022/09/28 MCCM初版 MOD START *//*
        *//*                                :mm_kokyaku.saishu_koshin_programid ;*//*
                                :mm_kokyaku.saishu_koshin_programid ,
        *//* 2022/09/28 MCCM初版 MOD END *//*
        *//* 2022/09/28 MCCM初版 ADD START *//*
                                :mm_kokyaku.kokyaku_status          ,
                                :mm_kokyaku.nyukai_kaisha_cd_mcc    ,
                                :mm_kokyaku.nyukai_tenpo_mcc        ,
                                :mm_kokyaku.senior                  ,
                                :mm_kokyaku.zokusei_kanri_shutai_system,
                                :mm_kokyaku.push_tsuchikyoka_flg    ,
                                :mm_kokyaku.mail_address_1_soshin_flg,
                                :mm_kokyaku.mail_address_2_soshin_flg,
                                :mm_kokyaku.mail_address_3_soshin_flg;
        *//* 2022/09/28 MCCM初版 ADD END *//*
        */
        sqlca.query(cmBTdmrgBDto.mm_kokyaku.kokyaku_no              ,
                cmBTdmrgBDto.mm_kokyaku.kyumin_flg              ,
                cmBTdmrgBDto.mm_kokyaku.kokyaku_mesho           ,
                cmBTdmrgBDto.mm_kokyaku.kokyaku_kana_mesho      ,
                cmBTdmrgBDto.mm_kokyaku.kokyaku_myoji           ,
                cmBTdmrgBDto.mm_kokyaku.kokyaku_name            ,
                cmBTdmrgBDto.mm_kokyaku.kana_kokyaku_myoji      ,
                cmBTdmrgBDto.mm_kokyaku.kana_kokyaku_name       ,
                cmBTdmrgBDto.mm_kokyaku.nenre                   ,
                cmBTdmrgBDto.mm_kokyaku.tanjo_y                 ,
                cmBTdmrgBDto.mm_kokyaku.tanjo_m                 ,
                cmBTdmrgBDto.mm_kokyaku.tanjo_d                 ,
                cmBTdmrgBDto.mm_kokyaku.sebetsu                 ,
                cmBTdmrgBDto.mm_kokyaku.konin                   ,
                cmBTdmrgBDto.mm_kokyaku.nyukai_kigyo_cd         ,
                cmBTdmrgBDto.mm_kokyaku.nyukai_tenpo            ,
                cmBTdmrgBDto.mm_kokyaku.hakken_kigyo_cd         ,
                cmBTdmrgBDto.mm_kokyaku.hakken_tenpo            ,
                cmBTdmrgBDto.mm_kokyaku.shain_kbn               ,
                cmBTdmrgBDto.mm_kokyaku.portal_nyukai_ymd       ,
                cmBTdmrgBDto.mm_kokyaku.portal_taikai_ymd       ,
                cmBTdmrgBDto.mm_kokyaku.sagyo_kigyo_cd          ,
                cmBTdmrgBDto.mm_kokyaku.sagyosha_id             ,
                cmBTdmrgBDto.mm_kokyaku.sagyo_ymd               ,
                cmBTdmrgBDto.mm_kokyaku.sagyo_hms               ,
                cmBTdmrgBDto.mm_kokyaku.batch_koshin_ymd        ,
                cmBTdmrgBDto.mm_kokyaku.saishu_setai_ymd        ,
                cmBTdmrgBDto.mm_kokyaku.saishu_setai_hms        ,
                cmBTdmrgBDto.mm_kokyaku.saishu_koshin_ymd       ,
                cmBTdmrgBDto.mm_kokyaku.saishu_koshin_programid ,
                cmBTdmrgBDto.mm_kokyaku.kokyaku_status          ,
                cmBTdmrgBDto.mm_kokyaku.nyukai_kaisha_cd_mcc    ,
                cmBTdmrgBDto.mm_kokyaku.nyukai_tenpo_mcc        ,
                cmBTdmrgBDto.mm_kokyaku.senior                  ,
                cmBTdmrgBDto.mm_kokyaku.zokusei_kanri_shutai_system,
                cmBTdmrgBDto.mm_kokyaku.push_tsuchikyoka_flg    ,
                cmBTdmrgBDto.mm_kokyaku.mail_address_1_soshin_flg,
                cmBTdmrgBDto.mm_kokyaku.mail_address_2_soshin_flg,
                cmBTdmrgBDto.mm_kokyaku.mail_address_3_soshin_flg);
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertKokyaku *** MM顧客情報INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.mm_kokyaku.kokyaku_no.arr );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "INSERT", sqlca.sqlcode,
                    "MM顧客情報", out_format_buf, 0, 0);

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("顧客情報登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客情報登録処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertKokyaku Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakuZokusei                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakuZokusei()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客属性情報登録処理                                         */
    /*  【概要】                                                                  */
    /*               MM顧客属性情報にダミーデータを登録する。                     */
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
    public int InsertKokyakuZokusei() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客属性情報登録処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql.arr));
        // memset((char *)&mm_kokyaku_zokusei, 0x00, sizeof(mm_kokyaku_zokusei) );

        /* 顧客属性情報構造体編集 */
        /* 顧客番号               */
        memcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.kokyaku_no, cmBTdmrgBDto.h_uid, sizeof(cmBTdmrgBDto.h_uid.arr) );
        cmBTdmrgBDto.mm_kokyaku_zokusei.kokyaku_no.len = strlen(cmBTdmrgBDto.h_uid);
        cmBTdmrgBDto.mm_kokyaku_zokusei.kyumin_flg.arr = 0                                       ; /* 休眠フラグ             */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.yubin_no, HALF_SPACE10 )              ; /* 郵便番号               */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.yubin_no_cd, HALF_SPACE23 )           ; /* 郵便番号コード         */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_1, HALF_SPACE10 )               ; /* 住所１                 */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_2, HALF_SPACE80 )               ; /* 住所２                 */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_3, HALF_SPACE80 )               ; /* 住所３                 */
        /* 2022/09/28 MCCM初版 ADD START */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.address, HALF_SPACE200 )              ; /* 住所                   */
        /* 2022/09/28 MCCM初版 ADD END */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_1, HALF_SPACE15 )            ; /* 電話番号１             */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_2, HALF_SPACE15 )            ; /* 電話番号２             */
        memset(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_1, 0x00, sizeof(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_1.arr))  ; /* 検索電話番号１         */
        memset(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_2, 0x00, sizeof(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_2.arr))  ; /* 検索電話番号２         */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_1, HALF_SPACE60 )       ; /* Ｅメールアドレス１     */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_2, HALF_SPACE60 )       ; /* Ｅメールアドレス２     */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_3, HALF_SPACE15 )            ; /* 電話番号３             */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_4, HALF_SPACE15 )            ; /* 電話番号４             */
        memset(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_3, 0x00, sizeof(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_3.arr))  ; /* 検索電話番号３         */
        memset(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_4, 0x00, sizeof(cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_4.arr))  ; /* 検索電話番号４         */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.shokugyo, HALF_SPACE40 )              ; /* 職業                   */
        cmBTdmrgBDto.mm_kokyaku_zokusei.kinmu_kbn.arr = 0                                        ; /* 勤務区分               */
        strcpy(cmBTdmrgBDto.mm_kokyaku_zokusei.jitaku_jusho_cd, ZERO11 )             ; /* 自宅住所コード         */
        cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_kigyo_cd.arr = 0                                   ; /* 作業企業コード         */
        cmBTdmrgBDto.mm_kokyaku_zokusei.sagyosha_id.arr = (double) 0                                      ; /* 作業者ＩＤ             */
        cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_ymd.arr = 0                                        ; /* 作業年月日             */
        cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_hms.arr = 0                                        ; /* 作業時刻               */
        cmBTdmrgBDto.mm_kokyaku_zokusei.batch_koshin_ymd.arr = g_batch_ymd                       ; /* バッチ更新日           */
        cmBTdmrgBDto.mm_kokyaku_zokusei.saishu_koshin_ymd.arr = g_batch_ymd                      ; /* 最終更新日             */
        /* 最終更新プログラムＩＤ */
        strncpy(cmBTdmrgBDto.mm_kokyaku_zokusei.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );
        /* 2022/09/28 MCCM初版 ADD START */
        strncpy(cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_3, HALF_SPACE255, 255 )      ; /* Ｅメールアドレス３     */
        /* 2022/09/28 MCCM初版 ADD END */

        /* SQL文 */
        sprintf(wk_sql,
                "INSERT INTO MM顧客属性情報 " +
                        "    ( " +
                        "         顧客番号, " +
                        "         休眠フラグ, " +
                        "         郵便番号, " +
                        "         郵便番号コード, " +
                        "         住所１, " +
                        "         住所２, " +
                        "         住所３, " +
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         住所, " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "         電話番号１, " +
                        "         電話番号２, " +
                        "         検索電話番号１, " +
                        "         検索電話番号２, " +
                        "         Ｅメールアドレス１, " +
                        "         Ｅメールアドレス２, " +
                        "         電話番号３, " +
                        "         電話番号４, " +
                        "         検索電話番号３, " +
                        "         検索電話番号４, " +
                        "         職業, " +
                        "         勤務区分, " +
                        "         自宅住所コード, " +
                        "         作業企業コード, " +
                        "         作業者ＩＤ, " +
                        "         作業年月日, " +
                        "         作業時刻, " +
                        "         バッチ更新日, " +
                        "         最終更新日, " +
                        "         最終更新日時, " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "         最終更新プログラムＩＤ" */
                        "         最終更新プログラムＩＤ, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         Ｅメールアドレス３ " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "     ) " +
                        "   VALUES " +
                        "    ( " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "         sysdate(), " +
                        /* 2022/09/28 MCCM初版 MOD START */
                        /*                "         :v27 "*/
                        "         ?, " +
                        /* 2022/09/28 MCCM初版 MOD END */
                        /* 2022/09/28 MCCM初版 ADD START */
                        "         ? " +
                        /* 2022/09/28 MCCM初版 ADD END */
                        "     ) ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_ins_mmkz from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg( "*** InsertKokyakuZokusei *** SQL=[%s]\n", wk_sql);
            C_DbgMsg( "*** InsertKokyakuZokusei *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
            /*-------------------------------------------------------------*/
}
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_ins_mmkz using
        :mm_kokyaku_zokusei.kokyaku_no              ,
                                :mm_kokyaku_zokusei.kyumin_flg              ,
                                :mm_kokyaku_zokusei.yubin_no                ,
                                :mm_kokyaku_zokusei.yubin_no_cd             ,
                                :mm_kokyaku_zokusei.jusho_1                 ,
                                :mm_kokyaku_zokusei.jusho_2                 ,
                                :mm_kokyaku_zokusei.jusho_3                 ,
        *//* 2022/09/28 MCCM初版 ADD START *//*
                                :mm_kokyaku_zokusei.address                 ,
        *//* 2022/09/28 MCCM初版 ADD END *//*
                                :mm_kokyaku_zokusei.denwa_no_1              ,
                                :mm_kokyaku_zokusei.denwa_no_2              ,
                                :mm_kokyaku_zokusei.kensaku_denwa_no_1      ,
                                :mm_kokyaku_zokusei.kensaku_denwa_no_2      ,
                                :mm_kokyaku_zokusei.email_address_1         ,
                                :mm_kokyaku_zokusei.email_address_2         ,
                                :mm_kokyaku_zokusei.denwa_no_3              ,
                                :mm_kokyaku_zokusei.denwa_no_4              ,
                                :mm_kokyaku_zokusei.kensaku_denwa_no_3      ,
                                :mm_kokyaku_zokusei.kensaku_denwa_no_4      ,
                                :mm_kokyaku_zokusei.shokugyo                ,
                                :mm_kokyaku_zokusei.kinmu_kbn               ,
                                :mm_kokyaku_zokusei.jitaku_jusho_cd         ,
                                :mm_kokyaku_zokusei.sagyo_kigyo_cd          ,
                                :mm_kokyaku_zokusei.sagyosha_id             ,
                                :mm_kokyaku_zokusei.sagyo_ymd               ,
                                :mm_kokyaku_zokusei.sagyo_hms               ,
                                :mm_kokyaku_zokusei.batch_koshin_ymd        ,
                                :mm_kokyaku_zokusei.saishu_koshin_ymd       ,
        *//* 2022/09/28 MCCM初版 MOD START *//*
        *//*                                :mm_kokyaku_zokusei.saishu_koshin_programid ;*//*
                                :mm_kokyaku_zokusei.saishu_koshin_programid ,
        *//* 2022/09/28 MCCM初版 MOD END *//*
        *//* 2022/09/28 MCCM初版 ADD START *//*
                                :mm_kokyaku_zokusei.email_address_3         ;
        *//* 2022/09/28 MCCM初版 ADD END *//*
        */
        sqlca.query(cmBTdmrgBDto.mm_kokyaku_zokusei.kokyaku_no              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kyumin_flg              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.yubin_no                ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.yubin_no_cd             ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_1                 ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_2                 ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.jusho_3                 ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.address                 ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_1              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_2              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_1      ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_2      ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_1         ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_2         ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_3              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.denwa_no_4              ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_3      ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kensaku_denwa_no_4      ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.shokugyo                ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.kinmu_kbn               ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.jitaku_jusho_cd         ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_kigyo_cd          ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.sagyosha_id             ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_ymd               ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.sagyo_hms               ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.batch_koshin_ymd        ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.saishu_koshin_ymd       ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.saishu_koshin_programid ,
                cmBTdmrgBDto.mm_kokyaku_zokusei.email_address_3         );

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertKokyakuZokusei *** MM顧客属性情報INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.mm_kokyaku_zokusei.kokyaku_no.arr );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "INSERT", sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("顧客属性情報登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客属性情報登録処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertKokyakuZokusei Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakuKigyobetu                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakuKigyobetu()                                      */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報登録処理                                 */
    /*  【概要】                                                                  */
    /*               MM顧客企業別属性情報にダミーデータを登録する。               */
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
    public int InsertKokyakuKigyobetu() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart( "MM顧客企業別属性情報登録処理" );
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql.arr));
        //memset((char *)&mm_kokyaku_kigyo, 0x00, sizeof(mm_kokyaku_kigyo) );

        /* MM顧客企業別属性情報構造体編集 */
        /* 顧客番号                */
        memcpy( cmBTdmrgBDto.mm_kokyaku_kigyo.kokyaku_no, cmBTdmrgBDto.h_uid, sizeof(cmBTdmrgBDto.h_uid.arr) );
        cmBTdmrgBDto.mm_kokyaku_kigyo.kokyaku_no.len = strlen(cmBTdmrgBDto.h_uid);
        /* MCCM初版 20221219 MOD START */
        //mm_kokyaku_kigyo.kigyo_cd = ts_riyo_kano_point.nyukai_kigyo_cd; /* 企業コード             */
        cmBTdmrgBDto.mm_kokyaku_kigyo.kigyo_cd = cmBTdmrgBDto.ms_card.kigyo_cd; /* 企業コード             */
        /* MCCM初版 20221219 MOD END */
        cmBTdmrgBDto.mm_kokyaku_kigyo.nyukai_ymd.arr = g_batch_prev_ymd       ; /* 入会年月日             */
        cmBTdmrgBDto.mm_kokyaku_kigyo.taikai_ymd.arr = 0                      ; /* 退会年月日             */
        cmBTdmrgBDto.mm_kokyaku_kigyo.tel_tome_kbn.arr = 0                    ; /* ＴＥＬ止め区分         */
        cmBTdmrgBDto.mm_kokyaku_kigyo.dm_tome_kbn.arr = 3099                  ; /* ＤＭ止め区分           */
        cmBTdmrgBDto.mm_kokyaku_kigyo.email_tome_kbn.arr = 5099               ; /* Ｅメール止め区分       */
        cmBTdmrgBDto.mm_kokyaku_kigyo.ketai_tel_tome_kbn.arr = 0              ; /* 携帯ＴＥＬ止め区分     */
        cmBTdmrgBDto.mm_kokyaku_kigyo.ketai_email_tome_kbn.arr = 0            ; /* 携帯Ｅメール止め区分   */
        cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_kigyo_cd.arr = 0                  ; /* 作業企業コード         */
        cmBTdmrgBDto.mm_kokyaku_kigyo.sagyosha_id.arr = (double) 0                     ; /* 作業者ＩＤ             */
        cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_ymd.arr = 0                       ; /* 作業年月日             */
        cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_hms.arr = 0                       ; /* 作業時刻               */
        cmBTdmrgBDto.mm_kokyaku_kigyo.batch_koshin_ymd.arr = g_batch_ymd      ; /* バッチ更新日           */
        cmBTdmrgBDto.mm_kokyaku_kigyo.saishu_koshin_ymd.arr = g_batch_ymd     ; /* 最終更新日             */
        /* 最終更新プログラムＩＤ */
        strncpy( cmBTdmrgBDto.mm_kokyaku_kigyo.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );

        /* SQL文 */
        sprintf(wk_sql, "INSERT INTO MM顧客企業別属性情報 " +
                        "    ( " +
                        "         顧客番号, " +
                        "         企業コード, " +
                        "         入会年月日, " +
                        "         退会年月日, " +
                        "         ＴＥＬ止め区分, " +
                        "         ＤＭ止め区分, " +
                        "         Ｅメール止め区分, " +
                        "         携帯ＴＥＬ止め区分, " +
                        "         携帯Ｅメール止め区分, " +
                        "         作業企業コード, " +
                        "         作業者ＩＤ, " +
                        "         作業年月日, " +
                        "         作業時刻, " +
                        "         バッチ更新日, " +
                        "         最終更新日, " +
                        "         最終更新日時, " +
                        "         最終更新プログラムＩＤ " +
                        "     ) " +
                        "   VALUES " +
                        "    ( " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         ?, " +
                        "         sysdate(), " +
                        "         ? " +
                "     ) ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_ins_mmkz from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** InsertKokyakuKigyobetu *** SQL=[%s]\n", wk_sql);
                C_DbgMsg("*** InsertKokyakuKigyobetu *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_ins_mmkz using
        :mm_kokyaku_kigyo.kokyaku_no              ,
                                :mm_kokyaku_kigyo.kigyo_cd                ,
                                :mm_kokyaku_kigyo.nyukai_ymd              ,
                                :mm_kokyaku_kigyo.taikai_ymd              ,
                                :mm_kokyaku_kigyo.tel_tome_kbn            ,
                                :mm_kokyaku_kigyo.dm_tome_kbn             ,
                                :mm_kokyaku_kigyo.email_tome_kbn          ,
                                :mm_kokyaku_kigyo.ketai_tel_tome_kbn      ,
                                :mm_kokyaku_kigyo.ketai_email_tome_kbn    ,
                                :mm_kokyaku_kigyo.sagyo_kigyo_cd          ,
                                :mm_kokyaku_kigyo.sagyosha_id             ,
                                :mm_kokyaku_kigyo.sagyo_ymd               ,
                                :mm_kokyaku_kigyo.sagyo_hms               ,
                                :mm_kokyaku_kigyo.batch_koshin_ymd        ,
                                :mm_kokyaku_kigyo.saishu_koshin_ymd       ,
                                :mm_kokyaku_kigyo.saishu_koshin_programid ;
        */
        sqlca.query(cmBTdmrgBDto.mm_kokyaku_kigyo.kokyaku_no              ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.kigyo_cd                ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.nyukai_ymd              ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.taikai_ymd              ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.tel_tome_kbn            ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.dm_tome_kbn             ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.email_tome_kbn          ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.ketai_tel_tome_kbn      ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.ketai_email_tome_kbn    ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_kigyo_cd          ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.sagyosha_id             ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_ymd               ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.sagyo_hms               ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.batch_koshin_ymd        ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.saishu_koshin_ymd       ,
                cmBTdmrgBDto.mm_kokyaku_kigyo.saishu_koshin_programid );

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** InsertKokyakuKigyobetu *** MM顧客企業別属性情報INSERT結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.mm_kokyaku_kigyo.kokyaku_no.arr );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "INSERT", sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("MM顧客企業別属性情報登録処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客企業別属性情報登録処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----InsertKokyakuKigyobetu Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateCard                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateCard()                                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報更新処理                                           */
    /*  【概要】                                                                  */
    /*               MSカード情報を更新する。                                     */
    /*               ・顧客番号の紐付け                                           */
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
    public int UpdateCard() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("カード情報更新処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql.arr));

        /* カード情報構造体編集 */
        cmBTdmrgBDto.ms_card.riyu_cd.arr = 2000                 ; /* 理由コード             */
        cmBTdmrgBDto.ms_card.batch_koshin_ymd.arr = g_batch_ymd ; /* バッチ更新日           */
        cmBTdmrgBDto.ms_card.saishu_koshin_ymd.arr = g_batch_ymd; /* 最終更新日             */
        /* 最終更新プログラムＩＤ */
        strncpy( cmBTdmrgBDto.ms_card.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );

        /* SQL文 */
        sprintf(wk_sql, "UPDATE MSカード情報 " +
                        "   SET 理由コード             = DECODE(理由コード, '0', ?, 理由コード), " +
                        "       バッチ更新日           = ?, " +
                        "       最終更新日             = ?, " +
                        "       最終更新日時           = sysdate(), " +
                        "       最終更新プログラムＩＤ = ? " +
                        " WHERE サービス種別           = ? " +
                "   AND 会員番号               = ? ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateCard *** SQL=[%s]\n", wk_sql);
            /*-------------------------------------------------------------*/
        }

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_upd_mscd from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateCard *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_upd_mscd using
        :ms_card.riyu_cd,
                                :ms_card.batch_koshin_ymd,
                                :ms_card.saishu_koshin_ymd,
                                :ms_card.saishu_koshin_programid,
                                :ms_card.service_shubetsu,
                                :ms_card.kaiin_no;
        */
        sqlca.query(cmBTdmrgBDto.ms_card.riyu_cd,
                cmBTdmrgBDto.ms_card.batch_koshin_ymd,
                cmBTdmrgBDto.ms_card.saishu_koshin_ymd,
                cmBTdmrgBDto.ms_card.saishu_koshin_programid,
                cmBTdmrgBDto.ms_card.service_shubetsu,
                cmBTdmrgBDto.ms_card.kaiin_no);

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateCard *** MSカード情報UPDATE結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.ms_card.kokyaku_no.arr );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("カード情報更新処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("カード情報更新処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateCard Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateRiyoKano                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateRiyoKano()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               利用可能ポイント情報更新処理                                 */
    /*  【概要】                                                                  */
    /*               TS利用可能ポイント情報を更新する。                           */
    /*               ・チャージ実施日                                             */
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
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ           */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("利用可能ポイント情報更新処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTdmrgBDto.str_sql, 0x00, sizeof(cmBTdmrgBDto.str_sql.arr));

        /* 利用可能ポイント情報構造体編集 */
        cmBTdmrgBDto.ts_riyo_kano_point.saishu_koshin_ymd.arr = g_batch_ymd; /* 最終更新日             */
        strncpy( cmBTdmrgBDto.ts_riyo_kano_point.saishu_koshin_programid, Cg_Program_Name, strlen(Cg_Program_Name) );
        /* 最終更新プログラムＩＤ */

        /* SQL文 */
        sprintf(wk_sql, "UPDATE TS利用可能ポイント情報 " +
                        "   SET チャージ実施日         = DECODE(チャージ実施日, '0', ?, チャージ実施日), " +
                        "       最終更新日             = ?, " +
                        "       最終更新日時           = sysdate(), " +
                        "       最終更新プログラムＩＤ = ? " +
                        " WHERE 顧客番号               = ? ");

        /* ＨＯＳＴ変数にセット */
        strcpy( cmBTdmrgBDto.str_sql, wk_sql );

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateRiyoKano *** SQL=[%s]\n", wk_sql);
            /*-------------------------------------------------------------*/
        }

        /* 動的SQL文を解析する */
        // EXEC SQL PREPARE sql_stat_upd_rykn from :str_sql;
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** UpdateRiyoKano *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                /*-------------------------------------------------------------*/
            }
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]",cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", out_format_buf, 0, 0);

            return C_const_NG;
        }

        /*
        EXEC SQL EXECUTE sql_stat_upd_rykn using
        :gh_bat_date,
                                :ts_riyo_kano_point.saishu_koshin_ymd,
                                :ts_riyo_kano_point.saishu_koshin_programid,
                                :h_uid;
        */
        sqlca.query(cmBTdmrgBDto.gh_bat_date,
                cmBTdmrgBDto.ts_riyo_kano_point.saishu_koshin_ymd,
                cmBTdmrgBDto.ts_riyo_kano_point.saishu_koshin_programid,
                cmBTdmrgBDto.h_uid);

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** UpdateRiyoKano *** TS利用可能ポイント情報UPDATE結果 = %d\n",
                    sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {

            /* エラーの場合 */
            memset( out_format_buf, 0x00, sizeof(out_format_buf) );
            sprintf( out_format_buf, "顧客番号=[%s]", cmBTdmrgBDto.h_uid );

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", out_format_buf, 0, 0);

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("利用可能ポイント情報更新処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("利用可能ポイント情報更新処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----UpdateRiyoKano Bottom----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： GetMsrank                                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  GetMsrank()                                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MSランク別ボーナスポイント情報取得処理                       */
    /*  【概要】                                                                  */
    /*               MSランク別ボーナスポイント情報を全件抽出し、                 */
    /*               グローバル変数に設定する。                                   */
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
    public int GetMsrank() {
        int          i_loop;                /* ループカウンタ                */

        if(DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MSランク別ボーナスポイント情報取得処理");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        i_loop = 0;
        // memset(sRankbuf, 0x00, sizeof(sRankbuf));

        /* MSランク別ボーナスポイント情報取得処理 */
        /* カーソル定義 */
        /*
        EXEC SQL DECLARE RKCH_MSRK01 CURSOR FOR
        SELECT DISTINCT
        ランク種別,
                必要金額,
                ランクコード
        FROM   MSランク別ボーナスポイント情報
        ORDER BY ランク種別,必要金額;
        */
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT DISTINCT\n" +
                "        ランク種別,\n" +
                "                必要金額,\n" +
                "                ランクコード\n" +
                "        FROM   MSランク別ボーナスポイント情報\n" +
                "        ORDER BY ランク種別,必要金額";

        SqlstmDto sqlca =sqlcaManager.get("RKCH_MSRK01");
        sqlca.sql = workSql;

        sqlca.declare();
        // EXEC SQL OPEN RKCH_MSRK01;
        sqlca.open();

        if(DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** GetMsrank *** カーソルOPEN = %d\n", sqlca.sqlcode);
            /*-------------------------------------------------------------*/
        }
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /* APLOG出力 */
            APLOG_WT( "904", 0, null, "OPEN", sqlca.sqlcode,
                    "MSランク別ボーナスポイント情報", "全件検索", 0, 0);
            // EXEC SQL CLOSE RKCH_MSRK01;
            sqlcaManager.close("RKCH_MSRK01");
            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("MSランク別ボーナスポイント情報取得処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        for ( ; ; ) {
            /* ホスト変数初期化 */
            cmBTdmrgBDto.msrk_buff.rank_shubetsu.arr = 0;
            cmBTdmrgBDto.msrk_buff.hitsuyo_kingaku.arr = (double) 0;
            cmBTdmrgBDto.msrk_buff.rank_cd.arr = 0;

            /*
            EXEC SQL FETCH RKCH_MSRK01
            INTO :msrk_buff.rank_shubetsu,
                      :msrk_buff.hitsuyo_kingaku,
                      :msrk_buff.rank_cd;
            */
            sqlca.fetch();
            sqlca.recData(cmBTdmrgBDto.msrk_buff.rank_shubetsu,
                    cmBTdmrgBDto.msrk_buff.hitsuyo_kingaku,
                    cmBTdmrgBDto.msrk_buff.rank_cd);
            /* データ無し以外エラーの場合処理を異常終了する */
            if (   sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                if(DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** GetMsrank *** カーソルFETCH = %d\n", sqlca.sqlcode);
                    /*-------------------------------------------------------------*/
                }
                /* APLOG出力 */
                APLOG_WT( "904", 0, null, "FETCH", sqlca.sqlcode,
                        "MSランク別ボーナスポイント情報", "全件検索", 0, 0);
                // EXEC SQL CLOSE RKCH_MSRK01;
                sqlcaManager.close("RKCH_MSRK01");
                if(DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd( "MSランク別ボーナスポイント情報取得処理", C_const_NG, 0, 0);
                    /*---------------------------------------------*/
                }
                /* 処理を終了する */
                return C_const_NG;
            }
            /* データ無しの場合処理を終了する */
            else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* 取得件数を保存する */
                g_rank_cnt = i_loop;
                break;
            }

            if(DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg( "*** GetMsrank *** 取得 ランク種別 = [%d]\n",
                        cmBTdmrgBDto.msrk_buff.rank_shubetsu );
                C_DbgMsg( "*** GetMsrank *** 取得 必要金額 = [%f]\n",
                        cmBTdmrgBDto.msrk_buff.hitsuyo_kingaku );
                C_DbgMsg( "*** GetMsrank *** 取得 ランクコード = [%d]\n",
                        cmBTdmrgBDto.msrk_buff.rank_cd );
                /*---------------------------------------------*/
            }
            /* 取得した値を構造体に保管 */
            SRankbuf s = new SRankbuf();
            s.rank_shubetsu_buf = cmBTdmrgBDto.msrk_buff.rank_shubetsu.intVal();;
            s.hitsuyo_kingaku_buf = cmBTdmrgBDto.msrk_buff.hitsuyo_kingaku.intVal();
            s.rank_cd_buf = cmBTdmrgBDto.msrk_buff.rank_cd.intVal();
            sRankbuf[i_loop] = s;

            i_loop++;
        }

        // EXEC SQL CLOSE RKCH_MSRK01;
        sqlcaManager.close("RKCH_MSRK01");

        if(DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MSランク別ボーナスポイント情報取得処理", C_const_OK, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
        /*-----setRankInfo Bottom----------------------------------------------*/
    }

}
