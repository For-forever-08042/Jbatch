package jp.co.mcc.nttdata.batch.business.service.cmBTnpskB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTnpskB.dto.TS_SERCH;
import jp.co.mcc.nttdata.batch.business.service.cmBTnpskB.dto.TS_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TSHS_DAY_POINT_TBL;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *    プログラム名   ： 通常ポイント失効処理（cmBTnpskB）
 *
 *   【処理概要】
 *     有効期限切れの通常ポイントを失効する。
 *
 *   【引数説明】
 *     -y                 :（必須）抽出対象年
 *     -m                 :（必須）抽出対象月
 *     -a                 :（任意）APLOGに出力する処理件数
 *     -DEBUG(-debug)     :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *      10     ： 正常
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *     30.00 : 2021/04/15 NDBS.緒方：初版
 *     31.00 : 2021/12/09 SSI.上野：共通関数(C_InsertDayPoint)修正によりリコンパイル
 *     40.00 : 2022/11/30 SSI.山口 ：MCCM 初版
 *     41,00 : 2024/06/24 SSI.石阪 ：HS-0232 旧販社コード取得時のnull考慮
 *                                   HS-0250 会員番号の桁不足
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 ******************************************************************************/
@Service
public class CmBTnpskBServiceImpl extends CmABfuncLServiceImpl implements CmBTnpskBService {

    TSHS_DAY_POINT_TBL hsptymd_t;       /* HSポイント日別情報バッファ         */
    StringDto h_str_sql = new StringDto(4096);       /* 実行用SQL文字列                     */

    /*---------失効予定情報取得用---------------*/
    double h_shikko_yotei_point;   /* 失効予定通常Ｐ合計値               */
    double h_shikko_yotei_kokyaku; /* 失効予定顧客数                     */
    int h_select_yyyymm;        /* 抽出対象年月                       */
    TS_TBL h_ts;
    /*---------MSカード情報用---------------*/
    ItemDto h_kaiin_no = new ItemDto(16);        /* 会員番号                           */
    ItemDto h_kigyo_cd = new ItemDto();        /* 企業コード                         */
    ItemDto h_kyu_hansya_cd = new ItemDto();   /* 旧販社コード                       */
    /*---------TSポイント年別情報用---------------*/
    ItemDto h_nenkan_rankup_kingaku = new ItemDto(); /* 年間ランクアップ対象金額     */
    ItemDto h_gekkan_rankup_kingaku = new ItemDto(); /* 月間ランクアップ対象金額     */
    ItemDto h_kaiage_cnt = new ItemDto();            /* 年間買上回数                 */
    /*---------MS顧客制度情報用---------------*/
    ItemDto h_nenji_rank_cd = new ItemDto();          /* 年次ランクコード                   */
    ItemDto h_getuji_rank_cd = new ItemDto();         /* 月次ランクコード                   */
    ItemDto h_kazoku_id = new ItemDto();             /* 家族ID                             */
    /*---------MS家族制度情報用---------------*/
    ItemDto h_kazoku_nenji_rank_cd = new ItemDto();          /* 家族年次ランクコード        */
    ItemDto h_kazoku_getuji_rank_cd = new ItemDto();         /* 家族月次ランクコード        */

    /* 2022/12/01 MCCM初版 ADD START */
    /*---------TSランク情報用-----------------*/
    ItemDto h_rank_nenji_rank_cd = new ItemDto();               /* TSランク情報 年次ランクコード */
    ItemDto h_rank_getuji_rank_cd = new ItemDto();               /* TSランク情報 月次ランクコード */
    ItemDto h_rank_nenkan_rankup_kingaku = new ItemDto();        /* TSランク情報 年間ランクアップ対象金額 */
    ItemDto h_rank_gekkan_rankup_kingaku = new ItemDto();        /* TSランク情報 月間ランクアップ対象金額 */
    /* 2022/12/01 MCCM初版 ADD END */

    /* -------各TBL検索用--------------------*/
    long h_kokyaku_no;      /* 顧客番号                           */

    int h_sysdate;                /* システム日付                       */
    StringDto Program_Name = new StringDto(10);                  /* バージョンなしプログラム名         */
    StringDto Program_Name_Ver = new StringDto(13);              /* バージョンつきプログラム名         */

    double w_seq;

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */

    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_Y = "-y";            /* 抽出対象年                         */
    String DEF_ARG_M = "-m";            /* 抽出対象月                         */
    String DEF_ARG_A = "-a";            /* APLOG出力処理件数                  */
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */

    String C_PRGNAME = "通常ポイント失効"; /* APログ用機能名                     */
    /*------------------------------------------*/
    int C_const_Ora_SNAP_OLD = -1555;   /* ORA-1555発生                      */
    int C_const_SNAP_OLD = 55;   /* ORA-1555発生                      */
    int C_const_Ora_null = -1405;   /* フェッチnull                      */
    int TUJO_RIYU_CD = 1191;   /* 有効期限切れ通常Ｐ失効            */
    int PROCESSING_CNT = 10000;/* 処理経過をAPLOGに出力する際のサイクル*/
    int C_const_MS_ERR = 5;   /* MSカード情報取得エラー            */
    /*-----------bit演算用----------------------*/
    int TUJO_BASE_BIT = 0x01;/* 通常Ｐ失効フラグ用                 */
    int TUJO_CHENGE_BIT = 0x20;/* 通常Ｐ失効フラグ用                 */
    int KIKAN_BASE_BIT = 0x0001;/* 期間限定Ｐ失効フラグ用             */
    int KIKAN_CHENGE_BIT = 0x1000;/* 期間限定Ｐ失効フラグ用             */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int gl_select_year;      /* 抽出対象年                         */
    int gl_select_month;     /* 抽出対象月                         */
    int gl_aplog_cnt;        /* APLOG出力処理件数                  */
    StringDto gl_sysdate = new StringDto(9);                     /* システム日付                       */
    StringDto gl_systime = new StringDto(7);                     /* システム時刻                       */
    int gl_sys_year;         /* システム年                         */
    StringDto gl_sys_y = new StringDto(4);                       /* システム年下1桁                    */
    StringDto gl_sys_kbn = new StringDto(4);                     /* 偶数年(０)/奇数年(１)区分          */
    StringDto gl_sys_month = new StringDto(7);                   /* システム月（全角）                 */

    StringDto gl_bat_date = new StringDto(9);                    /* バッチ処理日                       */
    StringDto gl_bat_date_yyyymm = new StringDto(7);             /* バッチ処理日付年月                 */
    StringDto gl_bat_date_yyyy = new StringDto(5);               /* バッチ処理日付年                   */
    StringDto gl_bat_date_mm = new StringDto(3);                 /* バッチ処理日月                     */

    StringDto aplog_yyyymm = new StringDto(32);                 /* APLOG出力用（抽出対象年月：[xxxx]) */

    int sql_flg;             /* ORA-1555発生フラグ                 */
    int fet_cnt;             /* 顧客処理件数                       */

    /* 2022/12/02 MCCM初版 ADD START */
    int gl_meisai_su;            /* 通常Ｐ明細数                   */
    /* 2022/12/02 MCCM初版 ADD END */


    /*-----  引数（引数の種類分定義する）----------*/
    int arg_y_chk;                          /* 引数yチェック用                */
    int arg_m_chk;                          /* 引数mチェック用                */
    int arg_a_chk;                          /* 引数oチェック用                */

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
        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        /*------------------------------------------------------------*/
        /*--------------------------------------*/
        /*  ローカル変数定義                    */
        /*--------------------------------------*/
        int rtn_cd;                     /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数結果ステータス                 */
        IntegerDto hs_ins_cnt = new IntegerDto();       /* HSポイント日別情報登録件数         */
        IntegerDto ts_upd_cnt = new IntegerDto();       /* TS利用可能ポイント情報更新件数     */
        IntegerDto shik_point_total = new IntegerDto();     /* 失効済み通常Ｐ合計                 */
        IntegerDto exclude_cnt = new IntegerDto();     /* 処理除外件数                       */
        StringDto buff = new StringDto(256);                  /* バッファ                           */
        int arg_cnt;                    /* 引数チェック用カウンタ             */
        StringDto get_year = new StringDto(5);                /* 抽出対象年                         */
        StringDto get_month = new StringDto(3);               /* 抽出対象月                         */
        StringDto arg_Work1 = new StringDto(256);             /* Work Buffer1                       */
        int bat_date_yyyymm;  /* バッチ日付年月                     */
        IntegerDto exp_yyyy = new IntegerDto();        /* 失効対象年度                       */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        hs_ins_cnt.arr = 0;
        ts_upd_cnt.arr = 0;
        shik_point_total.arr = 0;
        exclude_cnt.arr = 0;
        exp_yyyy.arr = 0;
        sql_flg = DEF_OFF;
        memset(gl_bat_date_yyyymm, 0x00, sizeof(gl_bat_date_yyyymm));
        memset(gl_bat_date_yyyy, 0x00, sizeof(gl_bat_date_yyyy));
        memset(gl_bat_date_mm, 0x00, sizeof(gl_bat_date_mm));
        memset(get_year, 0x00, sizeof(get_year));
        memset(get_month, 0x00, sizeof(get_month));
        arg_y_chk = DEF_OFF;
        arg_m_chk = DEF_OFF;
        arg_a_chk = DEF_OFF;
        fet_cnt = 0;

        /*--------------------------------------*/
        /*  プログラム名取得処理                */
        /*--------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }
        /*--------------------------------------*/
        /*  バッチデバッグ開始処理              */
        /*--------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        /*------------------------------------------------------------*/

        /*--------------------------------------*/
        /*  DBコネクト処理                      */
        /*--------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
        /*------------------------------------------------------------*/

        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*------------------------------------------*/
        /*  バッチ処理日取得処理                    */
        /*------------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日取得 %s\n", "START");
        /*------------------------------------------------------------*/
        rtn_cd = C_GetBatDate(0, gl_bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*------------------------------------------*/
        /*  バッチ処理日編集                        */
        /*------------------------------------------*/
        /* バッチ処理日付年月の取得*/
        memcpy(gl_bat_date_yyyymm, gl_bat_date, 6);

        /* バッチ処理日付年の取得*/
        memcpy(gl_bat_date_yyyy, gl_bat_date, 4);

        /* バッチ処理日付月の取得*/
        memcpy(gl_bat_date_mm, gl_bat_date.substring(4), 2);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日付年月 [%s]\n", gl_bat_date_yyyymm);
        C_DbgMsg("*** main *** バッチ処理日付年   [%s]\n", gl_bat_date_yyyy);
        C_DbgMsg("*** main *** バッチ処理日付月   [%s]\n", gl_bat_date_mm);
        /*------------------------------------------------------------*/

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/

        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
        C_DbgMsg("*** main *** 入力引数の数[%d]\n", argc - 1);
        /*------------------------------------------------------------*/
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {

            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
            /*------------------------------------------------------------*/

            if (memcmp(arg_Work1, DEF_ARG_Y, strlen(DEF_ARG_Y)) == 0 ||
                    memcmp(arg_Work1, DEF_ARG_M, strlen(DEF_ARG_M)) == 0 ||
                    memcmp(arg_Work1, DEF_ARG_A, strlen(DEF_ARG_A)) == 0) {
                /* -y,-m,-aCHK */
                rtn_cd = cmBTnpskB_ChkArgiInf(arg_Work1);
                if (rtn_cd == C_const_NG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", 0, 0, 0);
                    /*---------------------------------------------*/
                    rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                    return exit(C_const_APNG);
                }
            } else if (strcmp(arg_Work1, DEF_DEBUG) == 0 ||
                    strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else {
                /* 定義外パラメータ   */
                sprintf(buff, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, buff, 0, 0, 0, 0, 0);
                /*---------------------------------------------*/
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
                /*---------------------------------------------*/
                rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /* 必須パラメータ未指定チェック */
        if (arg_y_chk == DEF_OFF) {
            APLOG_WT("910", 0, null, "-y 引数の値が未設定です", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (arg_m_chk == DEF_OFF) {
            APLOG_WT("910", 0, null, "-m 引数の値が未設定です", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        if (arg_a_chk == DEF_OFF) {
            gl_aplog_cnt = PROCESSING_CNT;
        }
        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** 引数取得（抽出対象年）[%d]\n",
                gl_select_year);
        C_DbgMsg("*** main *** 引数取得（抽出対象月）[%d]\n",
                gl_select_month);
        C_DbgMsg("*** main *** 引数取得（APLOG出力処理件数）[%d]\n",
                gl_aplog_cnt);
        /*-------------------------------------------------------------*/

        /* 抽出対象年月設定 */
        sprintf(buff, "%d%02d", gl_select_year, gl_select_month);
        h_select_yyyymm = atoi(buff);

        /* 抽出対象年月範囲チェック */
        bat_date_yyyymm = atoi(gl_bat_date_yyyymm);
        if (bat_date_yyyymm < h_select_yyyymm) {
            APLOG_WT("910", 0, null, "引数の対象年月がバッチ日付年月より未来です",
                    0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }
        /* APLOG共通出力内容設定 */
        sprintf(aplog_yyyymm, "抽出年月[%d]", h_select_yyyymm);

        /*  開始メッセージ */
        sprintf(buff, "%s %s", C_PRGNAME, aplog_yyyymm);
        APLOG_WT("102", 0, null, buff, 0, 0, 0, 0, 0);
        /* バージョンなしプログラム名 */
        strcpy(Program_Name, Cg_Program_Name);
        /* バージョンなしプログラム名 */
        strcpy(Program_Name_Ver, Cg_Program_Name_Ver);

        /*------------------------------------------*/
        /*  システム日付取得処理                    */
        /*------------------------------------------*/
        rtn_cd = C_GetSysDateTime(gl_sysdate, gl_systime);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** システム日付取得NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(buff, "%s %s", "C_GetSysDateTime", aplog_yyyymm);
            APLOG_WT("903", 0, null, buff,
                    rtn_cd, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* システム日付編集 */
        rtn_cd = cmBTnpskB_setDateInf();
        if (rtn_cd != C_const_OK) {
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        /*--------------------------------------*/
        /*  主処理                              */
        /*--------------------------------------*/
        rtn_cd = cmBTnpskB_main(ts_upd_cnt,
                hs_ins_cnt, shik_point_total, exclude_cnt, exp_yyyy);

        /* 各テーブル更新処理件数を出力 */
        sprintf(buff, "%s テーブル名[TS利用可能ポイント情報] 処理件数[%d]",
                aplog_yyyymm, ts_upd_cnt);
        APLOG_WT("100", 0, null, buff, 0, 0, 0, 0, 0);
        sprintf(buff, "%s テーブル名[HSポイント日別情報] 処理件数[%d]",
                aplog_yyyymm, ts_upd_cnt);
        APLOG_WT("100", 0, null, buff, 0, 0, 0, 0, 0);
        sprintf(buff, "%s 失効対象年度[%d] " +
                        "失効処理件数[%d] 失効済み通常Ｐ合計[%d] 除外件数[%d]"
                , aplog_yyyymm, exp_yyyy, hs_ins_cnt, shik_point_total, exclude_cnt);
        APLOG_WT("100", 0, null, buff, 0, 0, 0, 0, 0);

        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** cmBTnpskB_main NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(buff, "通常ポイント失効処理に失敗しました。%s",
                    aplog_yyyymm);
            APLOG_WT("912", 0, null, buff, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
//            EXEC SQL ROLLBACK WORK RELEASE;     /* ロールバック               */
            sqlcaManager.rollbackRelease(sqlca);
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        sprintf(buff, "%s %s", C_PRGNAME, aplog_yyyymm);
        /*  終了メッセージ */
        APLOG_WT("103", 0, null, buff, 0, 0, 0, 0, 0);

        /*------------------------------------------------------------*/
        C_DbgEnd("*** main処理 ***", 0, 0, 0);
        /*------------------------------------------------------------*/
        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理          */

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease(sqlca);
        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_main(unsigned long int *ts_upd_cnt,       */
    /*                                       unsigned long int *hs_ins_cnt,       */
    /*                                       long int *shik_point_total,          */
    /*                                       unsigned int *exc_cnt,               */
    /*                                       unsigned short int *exp_yyyy )       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     有効期限切れの通常Ｐを失効する。                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      unsigned long int *ts_upd_cnt : TS利用可能ポイント情報更新件数        */
    /*      unsigned lomg int *hs_ins_cnt : HSポイント日別情報更新件数            */
    /*      long int *shik_point_total    : 失効済み通常Ｐ合計値                  */
    /*      unisgned int  *exc_cnt        : 失効処理除外数                        */
    /*      unsigned short int  *exp_yyyy : 失効対象年度                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_main(IntegerDto ts_upd_cnt,
                              IntegerDto hs_ins_cnt, IntegerDto shik_point_total,
                              IntegerDto exc_cnt, IntegerDto exp_yyyy) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_main処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_month;       /* 月                                  */
        int rtn_cd;                    /* 関数戻り値                          */

        /* TS利用可能ポイント情報検索用構造体宣言 */
        TS_SERCH ts_serch = new TS_SERCH();

        /* 初期化 */
        memset(ts_serch, 0x00, sizeof(ts_serch));

        /* バッチ処理日付の当年度・前年度・前々年度（失効対象年度）を取得 */
        cmBTnpskB_getYear(ts_serch.this_year, ts_serch.ex_year, ts_serch.expired_year);
        exp_yyyy.arr = ts_serch.expired_year.intVal();

        /* 対象年度のコード（全角）を設定 */
        cmBTnpskB_getYearCd(ts_serch.this_year, ts_serch.this_year_cd); /* 当年度*/
        cmBTnpskB_getYearCd(ts_serch.ex_year, ts_serch.ex_year_cd);     /* 前年度*/
        cmBTnpskB_getYearCd(ts_serch.expired_year, ts_serch.expired_year_cd); /* 失効対象年度*/

        /* バッチ処理日の前月 */
        wk_month = atoi(gl_bat_date_mm);
        if (wk_month != 1) {
            wk_month = wk_month - 1;
        } else {
            wk_month = 12;
        }
        /*対象月（全角）設定 */
        cmBTnpskB_getMonth(wk_month, ts_serch.ex_month);
        cmBTnpskB_getMonth(wk_month + 1, ts_serch.this_month00);
        cmBTnpskB_getMonth(wk_month + 2, ts_serch.this_month01);
        cmBTnpskB_getMonth(wk_month + 3, ts_serch.this_month02);
        cmBTnpskB_getMonth(wk_month + 4, ts_serch.this_month03);

        /* 失効予定情報を取得 */
        rtn_cd = cmBTnpskB_getInfo(ts_serch.expired_year, ts_serch.expired_year_cd);
        if (rtn_cd != C_const_OK) {
            return C_const_NG;
        }

        /* 通常ポイント失効処理 */
        while (true) {
            rtn_cd = cmBTnpskB_shikko(ts_serch, ts_upd_cnt, hs_ins_cnt,
                    shik_point_total, exc_cnt);
            if (rtn_cd == C_const_SNAP_OLD) {
                /* 処理継続 */
                continue;
            } else if (rtn_cd == C_const_OK) {
                /* 通常ポイント失効失効処理終了 */
                break;
            } else {
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTnpskB_main処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG;
            }
        }
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_main処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (C_const_OK);              /* 処理終了 */
        /*--- cmBTnpskB_main Bottom --------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getYear                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTnpskB_getYear( unsigned short int *this_yyyy,                 */
    /*       unsigned short int *ex_yyyy, unsigned short int *exp_yyyy)           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     バッチ処理日付から当年度・前年度・前々年度（失効対象年度）を取得する。 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    unsigned short int *this_yyyy     当年度                                */
    /*    unsigned short int *ex_yyyy       前年度                                */
    /*    unsigned short int *exp_yyyy      失効対象年度                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */

    /******************************************************************************/
    public void cmBTnpskB_getYear(ItemDto this_yyyy, ItemDto ex_yyyy, ItemDto exp_yyyy) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */
        int wk_mm;                       /* バッチ処理月           */
        int wk_yyyy;                     /* バッチ処理年           */

        wk_mm = atoi(gl_bat_date_mm);
        wk_yyyy = atoi(gl_bat_date_yyyy);

        /* バッチ処理日の当年度 */
        if (wk_mm <= 3) {
            this_yyyy.arr = wk_yyyy - 1; /* 年度切替前 */
        } else {
            this_yyyy.arr = wk_yyyy;   /* 年度切替後 */
        }

        /* バッチ処理日の前年度 */
        ex_yyyy.arr = this_yyyy.intVal() - 1;
        /* バッチ処理日の前々年度（失効対象年度） */
        exp_yyyy.arr = this_yyyy.intVal() - 2;

        /*------------------------------------------------------------*/
        sprintf(buff, "当年度:[%d]、前年度:[%d]、前々年度（失効対象年度）:[%d]",
                this_yyyy, ex_yyyy, exp_yyyy);
        C_DbgMsg("*** cmBTnpskB_getYear *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_getYear処理 --------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getYearCd                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*     voidt  cmBTnpskB_getYearCd(unsigned short int year, char *year_cd)     */
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
    void cmBTnpskB_getYearCd(ItemDto year, ItemDto year_cd) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getYearCd処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 年度コードを算出 */
        switch (year.intVal() % 5) {
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

        /*------------------------------------------------------------*/
        sprintf(buff, "年度:[%d]、年度コード:[%s]", year, year_cd);
        C_DbgMsg("*** cmBTnpskB_getYearCd *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getYearCd処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_getYearcd処理 -----------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTnpskB_getMonth(unsigned short int i_month,           */
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
    void cmBTnpskB_getMonth(int i_month, ItemDto month) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getMonth処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                             /*  バッファ                  */
        /*--------------------------------------*/

        switch (i_month % 12) {
            case 1:
                strcpy(month, "０１");
                break;
            case 2:
                strcpy(month, "０２");
                break;
            case 3:
                strcpy(month, "０３");
                break;
            case 4:
                strcpy(month, "０４");
                break;
            case 5:
                strcpy(month, "０５");
                break;
            case 6:
                strcpy(month, "０６");
                break;
            case 7:
                strcpy(month, "０７");
                break;
            case 8:
                strcpy(month, "０８");
                break;
            case 9:
                strcpy(month, "０９");
                break;
            case 10:
                strcpy(month, "１０");
                break;
            case 11:
                strcpy(month, "１１");
                break;
            case 0:
                strcpy(month, "１２");
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d], 格納月:[%s]", i_month, month);
        C_DbgMsg("*** cmBTnpskB_getMonth *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_getMonth処理 -------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_setDateInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_setDateInf( )                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MS顧客制度、MS家族制度、TSポイント年別情報検索用の日付を設定。         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_setDateInf() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_setDateNowInf処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_mm = new StringDto(3);                           /* 月                         */
        StringDto wk_buff = new StringDto(256);                       /* MSG用バッファ              */
        StringDto wk_y = new StringDto(2);                            /* 年下一桁                   */
        StringDto wk_yyyy = new StringDto(5);                         /* 年                         */
        int wk_year;                 /* 年                         */
        /* 2022/12/02 MCCM初版 ADD START */
        /* unsigned int wk_date; */                      /* システム日                 */
        int wk_i_mm;                 /* 月整数値                   */
        /* 2022/12/02 MCCM初版 ADD END */
        int rtn_cd;                  /* 戻り値                     */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        memset(wk_mm, 0x00, sizeof(wk_mm));
        memset(gl_sys_month, 0x00, sizeof(gl_sys_month));
        memset(wk_yyyy, 0x00, sizeof(wk_yyyy));
        memset(wk_y, 0x00, sizeof(wk_y));
        memset(gl_sys_kbn, 0x00, sizeof(gl_sys_kbn));

        /* システム月の取得 */
        memcpy(wk_mm, (gl_sysdate.substring(4)), 2);

        /* システム月を全角に変換   */
        rtn_cd = C_ConvHalf2Full(wk_mm, gl_sys_month);
        if (rtn_cd != C_const_OK) {
            sprintf(wk_buff, "%s %s", "C_ConvHalf2Ful(システム月）", aplog_yyyymm);
            APLOG_WT("903", 0, null, wk_buff,
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_setDateInf *** %s\n", "システム月全角エラー");
            C_DbgEnd("cmBTnpskB_setDateInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* システム年取得 */
        memcpy(wk_yyyy, gl_sysdate, 4);
        gl_sys_year = atoi(wk_yyyy);

        /* 2022/12/02 MCCM初版 MOD START */
        /* 年下一桁取得 */
        /* memcpy(wk_y,(gl_sysdate[3]), 1); */
        wk_i_mm = atoi(wk_mm);

        /* 年度値取得 */
        if (wk_i_mm <= 3) {
            wk_year = gl_sys_year - 1;
        } else {
            wk_year = gl_sys_year;
        }

        /* 年度下一桁取得 */
        sprintf(wk_y, "%d", wk_year % 10);
        /* 2022/12/02 MCCM初版 MOD END */

        rtn_cd = C_ConvHalf2Full(wk_y, gl_sys_y);
        if (rtn_cd != C_const_OK) {
            sprintf(wk_buff, "%s %s", "C_ConvHalf2Full(システム年)", aplog_yyyymm);
            APLOG_WT("903", 0, null, wk_buff,
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_setDateInf *** %s\n", "年下一桁全角エラー");
            C_DbgEnd("cmBTnpskB_setDateInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* 偶数年（０）／奇数年（１）区分取得 */
        /* 2022/12/02 MCCM初版 DEL START */
    /* wk_date = atoi(gl_sysdate);
    wk_year = (wk_date / 10000) % 10; */
        /* 2022/12/02 MCCM初版 DEL END */
        if ((wk_year % 2) == 0) {
            strcpy(gl_sys_kbn, "０");
        }/*偶数年 */ else {
            strcpy(gl_sys_kbn, "１");
        }

        /*------------------------------------------------------------*/
        sprintf(wk_buff,
                "システム月:[%s]、システム年:[%d]、システム年下一桁:[%s]、システム年区分:[%s]", gl_sys_month, gl_sys_year, gl_sys_y, gl_sys_kbn);
        C_DbgMsg("*** cmBTnpskB_setDateInf *** %s\n", wk_buff);
        /*------------------------------------------------------------*/

        return (C_const_OK);
        /*--- cmBTnpsk_setDateInf処理 ------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getInfo                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_getInfo(unsigned short int exp_year,      */
    /*                                          char *exp_year_cd)                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     失効予定の通常Ｐ数、顧客数を取得する。                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      unsigned short int exp_year : 失効対象年度                            */
    /*      char *exp_year_cd       : 失効対象年度コード                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_getInfo(ItemDto exp_year, ItemDto exp_year_cd) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getInfo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_buff = new StringDto(1026);                         /* バッファ                   */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        h_shikko_yotei_point = 0;
        h_shikko_yotei_kokyaku = 0;

        /* ＨＯＳＴ変数にセット */
        sprintf(h_str_sql,
                "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ "
                        + "COUNT(*) AS 失効予定顧客数, "
                        + "SUM (利用可能通常Ｐ%s) AS 失効予定通常Ｐ合計 "
                        + "FROM cmuser.TS利用可能ポイント情報 "
                        + "WHERE CAST(SUBSTR(最終更新日,1,6) AS NUMERIC) = ? "
                        + "AND 利用可能通常Ｐ%s <> 0 "
                        + "AND 顧客番号 <> 9999999999999 ",
                exp_year_cd, exp_year_cd);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getInfo *** 抽出SQL[%s] \n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat0 from :h_str_sql;
        sqlca.sql = h_str_sql;
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getInfo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        sqlca.declare();
//        EXEC SQL DECLARE CUR_SQL00 CURSOR FOR sql_stat0;

        /* カーソルオープン */
        sqlca.open(h_select_yyyymm);
//        EXEC SQL OPEN CUR_SQL00 USING :h_select_yyyymm;
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getInfo *** TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(wk_buff, "[%d]年度の通常Ｐ失効 %s", exp_year, aplog_yyyymm);
            APLOG_WT("904", 0, null, "OPEN CURSOR(失効予定情報)",
                    sqlca.sqlcode,
                    "TS利用可能ポイント情報情報", wk_buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getInfo *** %s\n", "カーソルオープンエラー");
            C_DbgEnd("cmBTnpskB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* カーソルフェッチ */
        sqlca.fetchInto(h_shikko_yotei_kokyaku,
                h_shikko_yotei_point);
//        EXEC SQL FETCH CUR_SQL00
//        INTO :h_shikko_yotei_kokyaku,      /* 失効予定顧客数            */
//         :    h_shikko_yotei_point;        /* 失効予定通常Ｐ合計        */

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_null &&
                sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(wk_buff, "[%d]年度の通常Ｐ失効 %s", exp_year, aplog_yyyymm);
            APLOG_WT("904", 0, null, "FETCH(失効予定情報)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    wk_buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getInfo *** %s\n", "フェッチエラー");
            C_DbgEnd("cmBTpskB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL00;
            sqlca.curse_close();
            return C_const_NG;
        }

        /* 対象顧客あり */
        if (h_shikko_yotei_kokyaku != 0) {
            /* メッセージ編集 */
            sprintf(wk_buff,
                    "%s 失効対象年度[%d] 失効予定件数[%.0f] " +
                            "失効予定通常Ｐ合計[%.0f]", aplog_yyyymm,
                    exp_year, h_shikko_yotei_kokyaku, h_shikko_yotei_point);
        }
        /* 対象顧客0件 */
        else {
            /* メッセージ編集 */
            sprintf(wk_buff, "%s 失効対象年度[%d]の対象顧客なし",
                    aplog_yyyymm, exp_year);
        }
        /* 失効予定情報をAPログに出力 */
        APLOG_WT("100", 0, null, wk_buff, 0, 0, 0, 0, 0);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBnnpskB_getInfo *** %s\n", wk_buff);
        C_DbgEnd("cmBTnpskB_getInfo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /* カーソルクローズ */
        sqlca.curse_close();
//        EXEC SQL CLOSE CUR_SQL00;
        return (C_const_OK);
        /*--- cmBTnpskB_getInfo処理 --------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_shikko                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_shikko(struct TS_SERCH ts_ser,            */
    /*                                  unsigned long int *ts_upd_cnt,            */
    /*                                  unsigned long int *hs_ins_cnt,            */
    /*                                  long int *shik_point_tota,                */
    /*                                  unsigned int *exc_cnt)                    */
    /*  【説明】                                                                  */
    /*     失効対象の顧客を抽出し、有効期限切れの通常Ｐを失効する。               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser          :   TS利用可能ポイント情報検索用      */
    /*      unsigned long int *ts_upd_cnt   ： TS利用可能ポイント情報更新件数     */
    /*      unsigned long int *hs_ins_cnt   ： HSポイント日別情報更新件数         */
    /*      long int *shik_point_total      : 失効済み通常Ｐ合計値                */
    /*      unsigned int *exc_cnt           : 失効処理除外件数                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             55   :  ORA-1555発生                                           */

    /******************************************************************************/
    public int cmBTnpskB_shikko(TS_SERCH ts_ser,
                                IntegerDto ts_upd_cnt,
                                IntegerDto hs_ins_cnt,
                                IntegerDto shik_point_total, IntegerDto exc_cnt) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_shikko処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_kokyaku_no = new StringDto(16);       /* 顧客番号                   */
        int rtn_cd;                  /* 戻り値                     */
        IntegerDto rtn_status = new IntegerDto();              /* 関数結果ステータス         */
        StringDto buff = new StringDto(256);               /* バッファ                   */

        /* 失効対象者抽出 */
        if (sql_flg == DEF_OFF) {
            /* 初回時 */
            cmBTnpskB_setSql(ts_ser);
        } else {
            /* ORA-1555発生時 */
            cmBTnpskB_setSql_ora1555(ts_ser);
        }

        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_stat1 from :h_str_sql;
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getInfo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL01 CURSOR FOR sql_stat1;
        sqlca.declare();
        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL01 USING :h_select_yyyymm;
        sqlca.open(h_select_yyyymm);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "[%d]年度の通常Ｐ失効 %s", ts_ser.expired_year,
                    aplog_yyyymm);
            APLOG_WT("904", 0, null, "OPEN CURSOR(失効対象者)",
                    sqlca.sqlcode,
                    "TS利用可能ポイント情報", buff, 0, 0);
            return C_const_NG;
        }
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_shikko *** TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        while (true) {
            /* 構造体初期化 */
            h_ts = new TS_TBL();
            memset(h_ts, 0x00, 0);

            /* カーソルフェッチ */
            sqlca.fetchInto(h_ts.vr_kokyaku_no, h_ts.tujo_p_shiko_flg);
//            EXEC SQL FETCH CUR_SQL01
//            INTO :h_ts.vr_kokyaku_no,        /* 顧客番号              */
//             :    h_ts.tujo_p_shiko_flg;     /* 通常Ｐ失効フラグ      */

            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* カーソルクローズ */
                sqlca.curse_close();
//                EXEC SQL CLOSE CUR_SQL01;
                break; /* ループ抜ける */
            }
            /* スナップショットが古すぎる場合 */
//             else if (sqlca.sqlcode == C_const_Ora_SNAP_OLD) {
//                 /* スナップショットが古すぎる場合 */
//                 sprintf(buff,
//                         " %s DBエラー(FETCH) STATUS=%d(TBL:TS利用可能ポイント情報 " +
//                                 "KEY:[%d]年度の通常Ｐ失効", aplog_yyyymm,
//                         C_const_Ora_SNAP_OLD, ts_ser.expired_year);
//                 APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
//                 /*------------------------------------------------------------*/
//                 C_DbgMsg("*** cmBTnpskB_shikko *** %s\n", "ORA-1555発生");
//                 /*------------------------------------------------------------*/
//                 /* カーソルクローズ */
// //                EXEC SQL CLOSE CUR_SQL01;
//                 sqlca.curse_close();
//                 /* ORA-1555発生フラグON */
//                 sql_flg = DEF_ON;
//                 return C_const_SNAP_OLD;
//             }
            /* スナップショットが古すぎる、データ無し以外エラーの場合 */
            else if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    sqlca.sqlcode != C_const_Ora_SNAP_OLD) {
                sprintf(buff, "[%d]年度の通常Ｐ失効 %s",
                        ts_ser.expired_year, aplog_yyyymm);
                APLOG_WT("904", 0, null, "FETCH(CUR_SQL01)",
                        sqlca.sqlcode, "TS利用可能ポイント情報",
                        buff, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_SQL01;
                sqlca.curse_close();
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_shikko *** %s\n", "フェッチエラー");
                C_DbgEnd("cmBTnpskB_shikko処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG; /* 処理を異常終了する */
            }

            /* 顧客処理件数カウントアップ */
            ++fet_cnt;
            /* 処理状況をＡＰログに出力する */
            if (fet_cnt % gl_aplog_cnt == 0) {
                sprintf(buff, "%s 処理件数[%d]/[%.0f]",
                        aplog_yyyymm, fet_cnt, h_shikko_yotei_kokyaku);
                APLOG_WT("100", 0, null, buff, 0, 0, 0, 0, 0);
            }

            /* 失効対象年度の失効状況確認 */
            rtn_cd = cmBTnpskB_check_tujo_bit(ts_ser.expired_year);
            if (rtn_cd != DEF_OFF) {
                /* 処理除外件数カウント */
                exc_cnt.arr = exc_cnt.arr + 1;
                continue;
            }
            /* 顧客番号（数値）設定 */
            h_kokyaku_no = atol(h_ts.vr_kokyaku_no.arr);

            /*顧客情報ロック */
            sprintf(wk_kokyaku_no, "%15d", h_kokyaku_no);
            rtn_cd = C_KdataLock(wk_kokyaku_no, "1", rtn_status);
            if (rtn_cd == C_const_NG) {
                sprintf(buff, "%s %s", "C_KdataLock(1)", aplog_yyyymm);
                APLOG_WT("903", 0, null, buff,
                        rtn_cd, rtn_status, 0, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_shikko *** ロックエラー顧客=[%s]\n",
                        wk_kokyaku_no);
                C_DbgEnd("cmBTnpskB_shikko処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------*/
//                EXEC SQL CLOSE CUR_SQL01; /* カーソルクローズ       */
                sqlca.curse_close();
                return C_const_NG;
            }
            /* ポイント情報の取得 */
            rtn_cd = cmBTnpskB_getTSriyo(ts_ser);
            if (rtn_cd == C_const_Stat_NOTFND) {
                /* 処理除外件数カウント */
                exc_cnt.arr = exc_cnt.arr + 1;
                /* ロールバック */
//                EXEC SQL ROLLBACK;
                sqlca.rollback();
                /* 処理を中断し、次のデータ取得 */
                continue;
            } else if (rtn_cd != C_const_Stat_NOTFND &&
                    rtn_cd != C_const_OK) {
//                EXEC SQL CLOSE CUR_SQL01; /* カーソルクローズ       */
                sqlca.curse_close();
                return C_const_NG;
            }

            /* 再度失効対象年度の失効状況確認 */
            rtn_cd = cmBTnpskB_check_tujo_bit(ts_ser.expired_year);
            if (rtn_cd != DEF_OFF) {
                /* 処理除外件数カウント */
                exc_cnt.arr = exc_cnt.arr + 1;
                /* ロールバック */
//                EXEC SQL ROLLBACK;
                sqlca.rollback();
                /* 処理を中断し、次のデータ取得 */
                continue;
            }
            /* 通常ポイント失効処理 */
            rtn_cd = cmBTnpskB_pointLost(ts_ser, ts_upd_cnt, hs_ins_cnt,
                    shik_point_total);
            if (rtn_cd == C_const_MS_ERR) {
                /* ロールバック */
//                EXEC SQL ROLLBACK;
                sqlca.rollback();
                /* 処理を中断し、次のデータ取得 */
                continue;
            } else if (rtn_cd != C_const_OK) {
//                EXEC SQL CLOSE CUR_SQL01;/* カーソルクローズ */
                sqlca.curse_close();
                /*-----------------------------------------------------*/
                C_DbgEnd("cmBTnpskB_shikko処理", C_const_NG, 0, 0);
                /*-----------------------------------------------------*/
                return C_const_NG;
            }
        }/* ループ終了 */
//        EXEC SQL CLOSE CUR_SQL01;/* カーソルクローズ */
        sqlca.curse_close();
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_shikko処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;
        /*--- cmBTnpskB_shikko処理 ------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_setSql                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void        cmBTnpskB_setSql(struct TS_SERCH ts_ser_sql)        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント失効対象の顧客を抽出するＳＱＬを作成する。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */

    /******************************************************************************/
    public void cmBTnpskB_setSql(TS_SERCH ts_ser_sql) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_setSql処理");
        /*---------------------------------------------------------------------*/

        /* 失効対象年度の通常Ｐ保有者取得抽出 */
        sprintf(h_str_sql,
                "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ "
                        + "顧客番号, "
                        + "通常Ｐ失効フラグ "
                        + "FROM cmuser.TS利用可能ポイント情報 "
                        + "WHERE CAST(SUBSTR(最終更新日,1,6) AS NUMERIC) = ? "
                        + "AND 利用可能通常Ｐ%s <> 0 "
                        + "AND 顧客番号 <> 9999999999999 "
                        + "ORDER BY 最終更新日 DESC "
                , ts_ser_sql.expired_year_cd);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_setSql *** 動的ＳＱＬ=[%s]\n ", h_str_sql);
        C_DbgEnd("cmBTnpskB_setSql処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTnpskB_setSql処理 ---------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_setSql_ora1555                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTnpskB_setSql_ora1555(struct TS_SERCH ts_ser_sql)     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ORA-1555発生時                                                         */
    /*     ポイント失効対象の顧客を抽出するＳＱＬを作成する。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */

    /******************************************************************************/
    public void cmBTnpskB_setSql_ora1555(TS_SERCH ts_ser_sql) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_setSql_ora1555処理");
        /*---------------------------------------------------------------------*/

        /* 失効対象年度の通常Ｐ保有者取得抽出 */
        sprintf(h_str_sql,
                "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ "
                        + "顧客番号, "
                        + "通常Ｐ失効フラグ "
                        + "FROM cmuser.TS利用可能ポイント情報 "
                        + "WHERE CAST(SUBSTR(最終更新日,1,6) AS NUMERIC) = ? "
                        + "AND 利用可能通常Ｐ%s <> 0 "
                        + "AND NOT(最終更新プログラムＩＤ = '%s' AND 最終更新日 = %s) "
                        + "AND 顧客番号 <> 9999999999999 "
                        + "ORDER BY 最終更新日 DESC "
                , ts_ser_sql.expired_year_cd
                , Program_Name, gl_sysdate);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_setSql_ora1555 *** 動的ＳＱＬ=[%s]\n ", h_str_sql);
        C_DbgEnd("cmBTnpskB_setSql_ora1555処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTnpskB_setSql_ora1555処理 -------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_check_tujo_point                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_check_tujo_point(unsigned int ex_year,          */
    /*                                              unisgned int this_year)       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度毎に失効状況を確認する                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       unsigned short int ex_year    前年度                                 */
    /*       unsigned short int this_year  当年度                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */

    /******************************************************************************/
    public void cmBTnpskB_check_tujo_point(ItemDto ex_year, ItemDto this_year) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_check_tujo_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                  /* 戻り値                */

        /* 前年度の残ポイント確認 */
        if (h_ts.tujoP_zennen.intVal() != 0) {
            /* 前年度の失効状況確認 */
            rtn_cd = cmBTnpskB_check_tujo_bit(ex_year);
            if (rtn_cd == DEF_ON) {
                h_ts.tujoP_zennen.arr = 0;
            }
        }

        /* 当年度の残ポイント確認 */
        if (h_ts.tujoP_tonen.intVal() != 0) {
            /* 当年度の失効状況確認 */
            rtn_cd = cmBTnpskB_check_tujo_bit(this_year);
            if (rtn_cd == DEF_ON) {
                h_ts.tujoP_tonen.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_check_tujo_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_check_tujo_point処理 ------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_check_tujo_bit                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_check_tujo_bit(unsigned short int year)   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度の通常ポイントが失効しているか確認する                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       unsigned short int year    対象年度                                  */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_check_tujo_bit(ItemDto year) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_check_tujo_bit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int shikko_flg;                  /* 失効状況確認用         */
        int wk_year_cd;                  /* 対象年度コード         */
        int check_flg;                   /* 対象年度フラグ         */
        int bit_kekka;                   /* bit計算結果            */
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 対象年度コード算出 */
        wk_year_cd = (year.intVal() % 5);

        /* 対象年度フラグ設定*/
        check_flg = (TUJO_BASE_BIT << (4 - wk_year_cd) | TUJO_CHENGE_BIT);

        /* 失効状況確認 */
        bit_kekka = h_ts.tujo_p_shiko_flg.intVal() & check_flg;
        if (bit_kekka == check_flg) { /* 失効済み */
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf(buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                            "bit結果：[%#x]、失効済み",
                    year, h_ts.tujo_p_shiko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTnpskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        } else { /* 未失効 */
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf(buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                            "bit結果：[%#x]、未失効",
                    year, h_ts.tujo_p_shiko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTnpskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_check_tujo_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTnpskB_check_tujo_bit処理 -------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getTSriyo                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int    cmBTnpskB_getTSriyo(struct TS_SERCH ts_ser_sql)   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報ＴＢＬから                                       */
    /*     ポイント情報を取得する。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              9   :  データ無し                                             */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public int cmBTnpskB_getTSriyo(TS_SERCH ts_ser_sql) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getTSriyo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(2048);                  /* バッファ               */

        /* 2022/12/02 MCCM初版 MOD START */
        /*-----------------------------------------------*/
        /*  変数初期化                                   */
        /*-----------------------------------------------*/
        gl_meisai_su = 0;
        /* 2022/12/02 MCCM初版 MOD END */

        /* ポイント情報取得 */
        sprintf(h_str_sql,
                "SELECT  /*+ INDEX(TS利用可能ポイント情報 PKTSPTPTRY00) */ "
                        /* 2022/12/01 MCCM初版 MOD START */
        /* "    利用可能通常Ｐ%s,  "
        "    利用可能通常Ｐ%s,  "
        "    利用可能通常Ｐ%s,  "
        "    通常Ｐ失効フラグ,  "
        "    入会企業コード,  "
        "    入会店舗,  "
        "    発券企業コード,  "
        "    発券店舗,  "
        "    利用可能期間限定Ｐ%s,  "
        "    利用可能期間限定Ｐ%s,  "
        "    利用可能期間限定Ｐ%s,  "
        "    利用可能期間限定Ｐ%s,  "
        "    利用可能期間限定Ｐ%s,  "
        "    期間限定Ｐ失効フラグ  "
        "FROM  TS利用可能ポイント情報 "
        "WHERE 顧客番号= :v1 "
        "AND  SUBSTR(最終更新日,1,6) = :v2 "
        "AND   利用可能通常Ｐ%s <> 0 " ,
        ts_ser_sql.this_year_cd, ts_ser_sql.ex_year_cd,
        ts_ser_sql.expired_year_cd, ts_ser_sql.ex_month,
        ts_ser_sql.this_month00, ts_ser_sql.this_month01,
        ts_ser_sql.this_month02, ts_ser_sql.this_month03,
        ts_ser_sql.expired_year_cd); */
                        + "    利用可能通常Ｐ%s,  "
                        + "    利用可能通常Ｐ%s,  "
                        + "    利用可能通常Ｐ%s,  "
                        + "    通常Ｐ失効フラグ,  "
                        + "    入会企業コード,  "
                        + "    入会店舗,  "
                        + "    発券企業コード,  "
                        + "    発券店舗,  "
                        + "    利用可能期間限定Ｐ%s,  "
                        + "    利用可能期間限定Ｐ%s,  "
                        + "    利用可能期間限定Ｐ%s,  "
                        + "    利用可能期間限定Ｐ%s,  "
                        + "    利用可能期間限定Ｐ%s,  "
                        + "    期間限定Ｐ失効フラグ,  "
                        + "    利用可能通常購買Ｐ%s,  "
                        + "    利用可能通常非購買Ｐ%s,  "
                        + "    利用可能通常その他Ｐ%s,  "
                        + "    入会会社コードＭＣＣ,  "
                        + "    入会店舗ＭＣＣ  "
                        + "FROM  TS利用可能ポイント情報 "
                        + "WHERE 顧客番号= ? "
                        + "AND  CAST(SUBSTR(最終更新日,1,6) AS NUMERIC) = ? "
                        + "AND   利用可能通常Ｐ%s <> 0 ",
                ts_ser_sql.this_year_cd, ts_ser_sql.ex_year_cd,
                ts_ser_sql.expired_year_cd, ts_ser_sql.ex_month,
                ts_ser_sql.this_month00, ts_ser_sql.this_month01,
                ts_ser_sql.this_month02, ts_ser_sql.this_month03,
                ts_ser_sql.expired_year_cd, ts_ser_sql.expired_year_cd,
                ts_ser_sql.expired_year_cd, ts_ser_sql.expired_year_cd);
        /* 2022/12/01 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_geTSriyo *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        SqlstmDto sqlca = sqlcaManager.get("sql_stat2");
//        EXEC SQL PREPARE sql_stat2 from :h_str_sql;
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSriyo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTnpskB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(buff, "%s SQL[%s] 顧客番号[%d]", aplog_yyyymm,
                    h_str_sql, h_kokyaku_no);
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    buff, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL02 CURSOR FOR sql_stat2;
        sqlca.declare();

        /* カーソルオープン */
        sqlca.open(h_kokyaku_no, h_select_yyyymm);
//        EXEC SQL OPEN CUR_SQL02 USING :h_kokyaku_no, :h_select_yyyymm;

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getTSriyo *** " +
                "TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s [%d]年度の通常Ｐ失効 顧客番号[%d]", aplog_yyyymm,
                    ts_ser_sql.expired_year, h_kokyaku_no);
            APLOG_WT("904", 0, null, "OPEN CURSOR(CUR_SQL02)",
                    sqlca.sqlcode, "TS利用可能ポイント情報情報",
                    buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSriyo *** %s\n", "カーソルエラー");
            C_DbgEnd("cmBTnpskB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }
        /* カーソルフェッチ */
        sqlca.fetchInto(h_ts.tujoP_tonen,            /* 利用可能通常Ｐ当年度               */
                h_ts.tujoP_zennen,           /* 利用可能通常Ｐ前年度               */
                h_ts.tujoP_expired,          /* 利用可能通常Ｐ失効対象年度         */
                h_ts.tujo_p_shiko_flg,       /* 通常Ｐ失効フラグ                   */
                h_ts.nyukai_kigyo_cd,        /* 入会企業コード                     */
                h_ts.nyukai_tenpo,           /* 入会店舗                           */
                h_ts.hakken_kigyo_cd,        /* 発券企業コード                     */
                h_ts.hakken_tenpo,           /* 発券店舗                           */
                h_ts.kikanP_ex,              /* 利用可能期間限定Ｐ前月             */
                h_ts.kikanP_this00,          /* 利用可能期間限定Ｐ当月             */
                h_ts.kikanP_this01,          /* 利用可能期間限定Ｐ当月＋１         */
                h_ts.kikanP_this02,          /* 利用可能期間限定Ｐ当月＋２         */
                h_ts.kikanP_this03,          /* 利用可能期間限定Ｐ当月＋３         */
                /* 2022/12/01 MCCM初版 MOD START */
                /* :h_ts.kikan_p_shikko_flg; */  /* 期間限定Ｐ失効フラグ               */
                h_ts.kikan_p_shikko_flg,     /* 期間限定Ｐ失効フラグ               */
                h_ts.shikko_kobai_p,         /* 利用可能通常購買Ｐ失効対象年度     */
                h_ts.shikko_hikobai_p,       /* 利用可能通常非購買Ｐ失効対象年度   */
                h_ts.shikko_sonota_p,        /* 利用可能通常その他Ｐ失効対象年度   */
                h_ts.nyukai_kigyo_cd_mcc,
                h_ts.nyukai_tenpo_mcc);
//        EXEC SQL FETCH CUR_SQL02
//          :h_ts.tujoP_tonen,            /* 利用可能通常Ｐ当年度               */
//          :h_ts.tujoP_zennen,           /* 利用可能通常Ｐ前年度               */
//          :h_ts.tujoP_expired,          /* 利用可能通常Ｐ失効対象年度         */
//          :h_ts.tujo_p_shiko_flg,       /* 通常Ｐ失効フラグ                   */
//          :h_ts.nyukai_kigyo_cd,        /* 入会企業コード                     */
//          :h_ts.nyukai_tenpo,           /* 入会店舗                           */
//          :h_ts.hakken_kigyo_cd,        /* 発券企業コード                     */
//          :h_ts.hakken_tenpo,           /* 発券店舗                           */
//          :h_ts.kikanP_ex,              /* 利用可能期間限定Ｐ前月             */
//          :h_ts.kikanP_this00,          /* 利用可能期間限定Ｐ当月             */
//          :h_ts.kikanP_this01,          /* 利用可能期間限定Ｐ当月＋１         */
//          :h_ts.kikanP_this02,          /* 利用可能期間限定Ｐ当月＋２         */
//          :h_ts.kikanP_this03,          /* 利用可能期間限定Ｐ当月＋３         */
//        /* 2022/12/01 MCCM初版 MOD START */
//        /* :h_ts.kikan_p_shikko_flg; */  /* 期間限定Ｐ失効フラグ               */
//          :h_ts.kikan_p_shikko_flg,     /* 期間限定Ｐ失効フラグ               */
//          :h_ts.shikko_kobai_p,         /* 利用可能通常購買Ｐ失効対象年度     */
//          :h_ts.shikko_hikobai_p,       /* 利用可能通常非購買Ｐ失効対象年度   */
//          :h_ts.shikko_sonota_p,        /* 利用可能通常その他Ｐ失効対象年度   */
//	      :h_ts.nyukai_kigyo_cd_mcc,
//	      :h_ts.nyukai_tenpo_mcc;
//        /* 2022/12/01 MCCM初版 MOD END */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s [%d]年度の通常Ｐ失効 顧客番号[%d]", aplog_yyyymm,
                    ts_ser_sql.expired_year, h_kokyaku_no);
            APLOG_WT("904", 0, null, "FETCH(CUR_SQL02)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    buff, 0, 0);
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL02;
            sqlca.curse_close();
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSriyo *** %s\n", "フェッチエラー");
            C_DbgEnd("cmBTnpskB_getTSriyo 処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* データ無しの場合、処理を終了する */
        if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* カーソルクローズ */

            sqlca.curse_close();
//            EXEC SQL CLOSE CUR_SQL02;
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSriyo *** %s\n",
                    "TS利用可能ポイント情報なし");
            C_DbgEnd("cmBTnpskB_getTSriyo 処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_Stat_NOTFND;
        }

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL02;
        sqlca.curse_close();
        /* 2022/12/02 MCCM初版 ADD START */
        if (h_ts.shikko_kobai_p.intVal() != 0) {     /* 購買Ｐ <> 0 の場合、通常Ｐ明細数 +1   */
            gl_meisai_su++;
        }
        if (h_ts.shikko_hikobai_p.intVal() != 0) {   /* 非購買Ｐ <> 0 の場合、通常Ｐ明細数 +1 */
            gl_meisai_su++;
        }
        if (h_ts.shikko_sonota_p.intVal() != 0) {    /* その他Ｐ <> 0 の場合、通常Ｐ明細数 +1 */
            gl_meisai_su++;
        }
        /* 2022/12/02 MCCM初版 ADD END */

        /*------------------------------------------------------------*/
        sprintf(buff,
                /* 2022/12/01 MCCM初版 MOD START */
    /*    "入会企業コード:[%d],入会店舗:[%d],発券企業コード:[%d],発券店舗:[%d],"
        "利用可能通常Ｐ%s:[%10.0lf],利用可能通常Ｐ%s:[%10.0lf],"
        "利用可能通常Ｐ%s:[%10.0lf],通常Ｐ失効フラグ:[%d],"
        "利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],"
        "利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],"
        "利用可能期間限定Ｐ%s:[%10.0lf],期間限定Ｐ失効フラグ:[%d]",
        h_ts.nyukai_kigyo_cd, h_ts.nyukai_tenpo, h_ts.hakken_kigyo_cd,
        h_ts.hakken_tenpo, ts_ser_sql.this_year_cd, h_ts.tujoP_tonen,
        ts_ser_sql.ex_year_cd, h_ts.tujoP_zennen,
        ts_ser_sql.expired_year_cd, h_ts.tujoP_expired,
        h_ts.tujo_p_shiko_flg,
        ts_ser_sql.ex_month, h_ts.kikanP_ex,
        ts_ser_sql.this_month00, h_ts.kikanP_this00,
        ts_ser_sql.this_month01, h_ts.kikanP_this01,
        ts_ser_sql.this_month02, h_ts.kikanP_this02,
        ts_ser_sql.this_month03, h_ts.kikanP_this03,
        h_ts.kikan_p_shikko_flg); */
                "入会企業コード:[%d],入会店舗:[%d],発券企業コード:[%d],発券店舗:[%d],"
                        + "利用可能通常Ｐ%s:[%10.0f],利用可能通常Ｐ%s:[%10.0f],"
                        + "利用可能通常Ｐ%s:[%10.0f],通常Ｐ失効フラグ:[%d],"
                        + "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],"
                        + "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],"
                        + "利用可能期間限定Ｐ%s:[%10.0f],期間限定Ｐ失効フラグ:[%d],"
                        + "利用可能通常購買Ｐ%s:[%10.0f],利用可能通常非購買Ｐ%s:[%10.0f],"
                        + "利用可能通常その他Ｐ%s:[%10.0f] ",
                h_ts.nyukai_kigyo_cd.longVal(), h_ts.nyukai_tenpo.longVal(), h_ts.hakken_kigyo_cd.longVal(), h_ts.hakken_tenpo.longVal(),
                ts_ser_sql.this_year_cd, h_ts.tujoP_tonen.floatVal(),
                ts_ser_sql.ex_year_cd, h_ts.tujoP_zennen.floatVal(),
                ts_ser_sql.expired_year_cd, h_ts.tujoP_expired.floatVal(),
                h_ts.tujo_p_shiko_flg.longVal(),
                ts_ser_sql.ex_month, h_ts.kikanP_ex.floatVal(),
                ts_ser_sql.this_month00, h_ts.kikanP_this00.floatVal(),
                ts_ser_sql.this_month01, h_ts.kikanP_this01.floatVal(),
                ts_ser_sql.this_month02, h_ts.kikanP_this02.floatVal(),
                ts_ser_sql.this_month03, h_ts.kikanP_this03.floatVal(),
                h_ts.kikan_p_shikko_flg.longVal(),
                ts_ser_sql.expired_year_cd, h_ts.shikko_kobai_p.floatVal(),
                ts_ser_sql.expired_year_cd, h_ts.shikko_hikobai_p.floatVal(),
                ts_ser_sql.expired_year_cd, h_ts.shikko_sonota_p.floatVal());
        /* 2022/12/01 MCCM初版 MOD END */
        C_DbgMsg("*** cmBTnpskB_getTSriyo *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getTSriyo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTnpskB_getTSriyo処理 ------------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_check_kikan_point                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_check_kikan_point()                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象月の失効状況を確認する                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       なし                                                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */

    /******************************************************************************/
    public void cmBTnpskB_check_kikan_point() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_check_kikan_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_month;               /* バッチ処理月           */
        int rtn_cd;                 /* 戻り値                 */

        wk_month = atoi(gl_bat_date_mm);

        /* 前月取得 */
        if (wk_month == 1) {
            wk_month = 12;
        } else {
            wk_month = wk_month - 1;
        }

        /* 前月の残ポイント確認 */
        if (h_ts.kikanP_ex.intVal() != 0) {
            /* 失効状況確認 */
            rtn_cd = cmBTnpskB_check_kikan_bit(wk_month);
            if (rtn_cd == DEF_ON) {
                h_ts.kikanP_ex.arr = 0;
            }
        }

        /* バッチ処理月の残ポイント確認 */
        if (h_ts.kikanP_this00.intVal() != 0) {
            /* 失効状況確認 */
            rtn_cd = cmBTnpskB_check_kikan_bit(wk_month + 1);
            if (rtn_cd == DEF_ON) {
                h_ts.kikanP_this00.arr = 0;
            }
        }

        /* バッチ処理月＋１の残ポイント確認 */
        if (h_ts.kikanP_this01.intVal() != 0) {
            /* 失効状況確認 */
            rtn_cd = cmBTnpskB_check_kikan_bit(wk_month + 2);
            if (rtn_cd == DEF_ON) {
                h_ts.kikanP_this01.arr = 0;
            }
        }

        /* バッチ処理月＋２の残ポイント確認 */
        if (h_ts.kikanP_this02.intVal() != 0) {
            /* 失効状況確認 */
            rtn_cd = cmBTnpskB_check_kikan_bit(wk_month + 3);
            if (rtn_cd == DEF_ON) {
                h_ts.kikanP_this02.arr = 0;
            }
        }

        /* バッチ処理月＋３の残ポイント確認 */
        if (h_ts.kikanP_this03.intVal() != 0) {
            /* 失効状況確認 */
            rtn_cd = cmBTnpskB_check_kikan_bit(wk_month + 4);
            if (rtn_cd == DEF_ON) {
                h_ts.kikanP_this03.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_check_kikan_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_check_kikan_point処理 ----------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_check_kikan_bit                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_check_kikan_bit(unsigned short int month) */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象月の期間限定ポイントの失効状況を確認する                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       unsigned short int month    対象月                                   */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_check_kikan_bit(int month) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_check_kikan_bit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int shikko_flg;                  /* 失効状況確認用         */
        int check_flg;                   /* bit演算用フラグ        */
        int bit_kekka;                   /* bit計算結果            */
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 対象月を算出する */
        month = (month % 12);
        if (month == 0) {
            month = 12;
        }

        /* 失効確認フラグ設定*/
        check_flg = (KIKAN_BASE_BIT << (12 - month) | KIKAN_CHENGE_BIT);

        bit_kekka = h_ts.kikan_p_shikko_flg.intVal() & check_flg;

        /* 失効フラグの値確認 */
        if (bit_kekka == check_flg) {
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf(buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、対象月のbit：[%#x]、" +
                            "bit結果：[%#x]、失効済み",
                    month, h_ts.kikan_p_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTnpskB_check_kikan_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        } else {
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf(buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、対象月のbit：[%#x]、" +
                            "bit結果：[%#x]、未失効",
                    month, h_ts.kikan_p_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTnpskB_check_kikan_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_check_kikan_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTnpskB_check_kikan_bit処理 ------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_pointLost                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_pointLost(struct TS_SERCH ts_ser,         */
    /*                                  unsigned long int *ts_upd_cnt,            */
    /*                                  unsigned long int *hs_ins_cnt,            */
    /*                                  long int *shik_point_total)               */
    /*                                                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     失効ポイント登録処理。                                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser       : TS利用可能ポイント情報検索用           */
    /*      unsigned long int *ts_upd_cnt: TS利用可能ポイント情報更新件数         */
    /*      unsigned long int *hs_ins_cnt:                                        */
    /*                             HSポイント日別情報（通常ポイント失効）更新件数 */
    /*      long int *shik_point_total   : 失効済み通常Ｐ合計値                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              5   :  MSカード情報取得エラー                                 */

    /******************************************************************************/
    public int cmBTnpskB_pointLost(TS_SERCH ts_ser,
                                   IntegerDto ts_upd_cnt, IntegerDto hs_ins_cnt,
                                   IntegerDto shik_point_total) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_pointLost処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                                 /* 戻り値                 */

        /* MSカード情報取得*/
        rtn_cd = cmBTnpskB_getMSCard();
        if (rtn_cd != C_const_OK) {
            return C_const_MS_ERR;
        }

        /* TSポイント年別情報取得 */
        cmBTnpskB_getTSYear();

        /* MS顧客制度報取得 */
        cmBTnpskB_getMSKokyaku();

        /* MS家族制度報取得 */
        if (h_kazoku_id.intVal() == 0) { /* 家族制度未入会 */
            h_kazoku_nenji_rank_cd.arr = 0;
            h_kazoku_getuji_rank_cd.arr = 0;
        } else { /* 家族制度入会済 */
            cmBTnpskB_getMSKazoku();
        }

        /* 2022/12/02 MCCM初版 ADD START */
        /* TSランク情報取得 */
        rtn_cd = cmBTnpskB_getTSRank();
        if (rtn_cd != C_const_OK) {
            return C_const_NG;
        }
        /* 2022/12/02 MCCM初版 ADD END */

        /* HSポイント日別情報登録 */
        rtn_cd = cmBTnpskB_tujo_insertHS(ts_ser);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* 2022/12/02 MCCM初版 ADD START */
        /* HSポイント日別内訳情報登録（通常ポイント） */
        rtn_cd = cmBTnpskB_insertHSUchiwake(ts_ser.expired_year.intVal());
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* 2022/12/02 MCCM初版 ADD END */

        /* TS利用可能ポイント情報の更新*/
        rtn_cd = cmBTnpskB_updTS(ts_ser.expired_year.intVal());
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /*HSポイント日別情報登録件数アップ */
        hs_ins_cnt.arr = hs_ins_cnt.arr + 1;

        /* TS利用可能ポイント情報更新件数アップ */
        ts_upd_cnt.arr = ts_upd_cnt.arr + 1;

        /*失効済み通常ポイントに失効ポイント加算 */
        shik_point_total.arr = shik_point_total.arr + h_ts.tujoP_expired.intVal();

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_pointLost処理", 0, 0, 0);
        /*------------------------------------------------------------*/

//        EXEC SQL COMMIT WORK;           /* コミット  */

        sqlca.commit();
        return C_const_OK;
        /*--- cmBTnpskB_pointLost処理 -----------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getMSCard                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_getMScard()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MSカード情報取得。                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*           なし                                                             */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public int cmBTnpskB_getMSCard() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getMSCard処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                              /* バッファ               */
        long kaiin_no;                  /* 会員番号               */

        /* 初期化 */
        memset(h_kaiin_no.arr, 0x00, sizeof(h_kaiin_no.arr));
        h_kaiin_no.len = 0;
        h_kigyo_cd.arr = 0;
        h_kyu_hansya_cd.arr = 0;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat3");
        sqlca.sql.arr = "SELECT\n" +
                "\tMS.会員番号,\n" +
                "\tMS.企業コード,\n" +
                "\tMS.旧販社コード \n" +
                "FROM\n" +
                "\t(\n" +
                "SELECT\n" +
                "\tサービス種別,\n" +
                "\t会員番号,\n" +
                "\t企業コード,\n" +
                "\t旧販社コード \n" +
                "FROM\n" +
                "\tMSカード情報 \n" +
                "WHERE\n" +
                "\t顧客番号 = ? \n" +
                "\tAND サービス種別 IN ( 1, 2, 3, 4, 5 ) \n" +
                "\tAND カードステータス IN ( 0, 1, 7, 8 ) \n" +
                "ORDER BY\n" +
                "\tカードステータス ASC,\n" +
                "CASE\n" +
                "\tサービス種別 \n" +
                "\tWHEN 1 THEN\n" +
                "\t1 \n" +
                "\tWHEN 4 THEN\n" +
                "\t2 \n" +
                "\tWHEN 3 THEN\n" +
                "\t3 \n" +
                "\tWHEN 2 THEN\n" +
                "\t4 ELSE 5 \n" +
                "\tEND ASC,\n" +
                "\t発行年月日 DESC \n" +
                "\t LIMIT 1) MS \n";
//        EXEC SQL SELECT     MS.会員番号,
//            MS.企業コード,
//            MS.旧販社コード
//        INTO :h_kaiin_no,
//                    :h_kigyo_cd,
//                    :h_kyu_hansya_cd
//        FROM (SELECT    サービス種別,
//                会員番号,
//                企業コード,
//                旧販社コード
//                FROM     MSカード情報
//                WHERE    顧客番号 = :h_kokyaku_no
//        /* 2022/12/02 MCCM初版 MOD START */
//                        /* AND     サービス種別 IN (1,2,3)
//                        AND     カードステータス IN (0,7,8)
//                        ORDER BY
//                        CASE    サービス種別
//                                    WHEN 1 THEN 1
//                                    WHEN 3 THEN 2
//                                    WHEN 2 THEN 3
//                                    ELSE 0
//                                END,
//                                    カードステータス ASC, */
//        AND     サービス種別 IN (1,2,3,4,5)
//        AND     カードステータス IN (0,1,7,8)
//        ORDER BY
//        カードステータス ASC,
//        CASE    サービス種別
//        WHEN 1 THEN 1
//        WHEN 4 THEN 2
//        WHEN 3 THEN 3
//        WHEN 2 THEN 4
//        ELSE 5
//        END ASC,
//        /* 2022/12/02 MCCM初版 MOD END */
//        発行年月日 DESC
//                        )MS
//        WHERE rownum  =  1;
        /* エラーの場合処理を異常終了する */
        sqlca.restAndExecute(h_kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s SQL検索失敗。" +
                            "STATUS=[%d](TBL:MSカード情報)顧客番号=[%d]",
                    aplog_yyyymm, sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getMScard *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getMSCard処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        kaiin_no = atol(h_kaiin_no.arr);
        sprintf(buff, "会員番号:[%d],企業コード:[%d],旧販社コード:[%d]",
                kaiin_no, h_kigyo_cd, h_kyu_hansya_cd);
        C_DbgMsg("*** cmBTnpskB_getMScard *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getMSCard処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;              /* 処理終了                           */
        /*-----cmBTnpskB_getMSCard Bottom---------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getTSyear                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_getTSYear()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TSポイント年別情報取得。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*            なし                                                            */
    /*  【戻り値】                                                                */
    /*            なし                                                            */

    /******************************************************************************/
    public void cmBTnpskB_getTSYear() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getTSYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ             */

        /* 初期化 */
        h_nenkan_rankup_kingaku.arr = 0;
        h_gekkan_rankup_kingaku.arr = 0;
        h_kaiage_cnt.arr = 0;

        sprintf(h_str_sql,
                /* 2022/12/02 MCCM初版 MOD START */
    /*  "SELECT 年間ランクＵＰ対象金額, "
        "       月間ランクＵＰ対象金額%s, "
        "       年間買上回数 "
        "  FROM TSポイント年別情報%d "
        " WHERE 年        =  %d "
        "   AND 顧客番号  =  :v1  ",
         gl_sys_month, gl_sys_year, gl_sys_year); */
                "SELECT 年間買上回数 "
                        + "  FROM TSポイント年別情報%d "
                        + " WHERE 年        =  %d "
                        + "   AND 顧客番号  =  ?  ",
                gl_sys_year, gl_sys_year);
        /* 2022/12/02 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_geTSYear *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        SqlstmDto sqlca = sqlcaManager.get("sql_stat4");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_stat4 from :h_str_sql;
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSYear *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            sprintf(buff, "%s SQL解析NG。 STATUS=%d、%s", aplog_yyyymm,
                    sqlca.sqlcode, h_str_sql);
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    buff, 0, 0, 0, 0);
            return;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL04 CURSOR FOR sql_stat4;
        sqlca.declare();
        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL04 USING :h_kokyaku_no;

        sqlca.open(h_kokyaku_no);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getTSYear *** TSポイント年別情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            sprintf(buff, "%s カーソルオープンエラー。" +
                            "STATUS=[%d](TBL:TSポイント年別情報)顧客番号=[%d]",
                    aplog_yyyymm, sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSYaer *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL04
        sqlca.fetchInto(h_kaiage_cnt);
        /* 2022/12/02 MCCM初版 MOD START */
        /* INTO :h_nenkan_rankup_kingaku,*/ /* 年間ランクUP対象金額           */
        /*      :h_gekkan_rankup_kingaku,*/ /* 月間ランクUP対象金額           */
        /*      :h_kaiage_cnt;           */ /* 年間買上回数                   */
//        INTO :h_kaiage_cnt;            /* 年間買上回数                   */
        /* 2022/12/02 MCCM初版 MOD END */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            sprintf(buff, "%s フェッチエラー。 STATUS=[%d] TSポイント年別情報%d" +
                            "顧客番号=[%d]", aplog_yyyymm, sqlca.sqlcode,
                    gl_sys_year, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getTSYaer *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL04;
            sqlca.curse_close();
            return;
        }
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL04;
        sqlca.curse_close();
        /*------------------------------------------------------------*/
        /* 2022/12/02 MCCM初版 MOD START */
    /* sprintf(buff, "年間ランクＵＰ対象金額:[%10.0lf],"
        "月間ランクＵＰ対象金額%s:[%10.0lf],年間買上回数:[%10.0lf] ",
        h_nenkan_rankup_kingaku, gl_sys_month, h_gekkan_rankup_kingaku,
        h_kaiage_cnt); */
        sprintf(buff, "年間買上回数:[%10.0f] ", h_kaiage_cnt.floatVal());
        /* 2022/12/02 MCCM初版 MOD END */

        C_DbgMsg("*** cmBTnpskB_getTSYaer *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getTSYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return;              /* 処理終了                           */
        /*-----cmBTnpskB_getTSYear Bottom---------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getMSKokyaku                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_getMSKokyaku()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MS顧客制度情報取得。                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public void cmBTnpskB_getMSKokyaku() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getMSKokyaku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ             */
        memset(h_str_sql, 0x00, sizeof(h_str_sql));

        /* 初期化 */
        h_kazoku_id.arr = 0;
        h_nenji_rank_cd.arr = 0;
        h_getuji_rank_cd.arr = 0;

        sprintf(h_str_sql,
                /* 2022/12/02 MCCM初版 MOD START */
                "SELECT 年次ランクコード%s, "
                        + "       月次ランクコード%s%s, "
                        + "       家族ＩＤ "
                        + "  FROM MS顧客制度情報 "
                        + " WHERE 顧客番号  =  ? ",
                gl_sys_y, gl_sys_kbn, gl_sys_month);
//        "SELECT 家族ＩＤ "
//        "  FROM MS顧客制度情報 "
//        " WHERE 顧客番号  =  :v1 " );
        /* 2022/12/02 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_geMSKokyaku*** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat5 from :h_str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat5");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_geMSKokyakur *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            sprintf(buff, "%s SQL解析NG STATUS=%d、%s",
                    aplog_yyyymm, sqlca.sqlcode, h_str_sql);
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    buff, 0, 0, 0, 0);
            return;
        }

        /* カーソル宣言 */
        sqlca.declare();
//        EXEC SQL DECLARE CUR_SQL05 CURSOR FOR sql_stat5;

        sqlca.open(h_kokyaku_no);
        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL05 USING :h_kokyaku_no;

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_geMSKokyaku *** MS顧客制度情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s カーソルオープンエラー。" +
                            "STATUS=[%d](TBL:MS顧客制度情報)顧客番号=[%d]", aplog_yyyymm,
                    sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_geMSKokyaku *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */

        sqlca.fetchInto(h_nenji_rank_cd,
                h_getuji_rank_cd,
                h_kazoku_id);
//        EXEC SQL FETCH CUR_SQL05
//        INTO :h_nenji_rank_cd,         /* 年次ランクコード           */
//             :h_getuji_rank_cd,        /* 月次ランクコード           */
//             :h_kazoku_id;             /* 家族ID                     */


        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s フェッチエラー。" +
                            "STATUS=[%d] MS顧客制度情報 顧客番号=[%d]", aplog_yyyymm,
                    sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getMSKokyaku *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL05;
            sqlca.curse_close();
            return;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "家族ＩＤ:[%d]", h_kazoku_id.longVal());
        C_DbgMsg("*** cmBTnpskB_geMSKokyaku *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getMSKokyakur処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL05;
        sqlca.curse_close();
        return;              /* 処理終了                           */
        /*-----cmBTnpskB_getMSKokyakur Bottom-----------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getMSKazoku                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTnpskB_getMSKazoku()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MS家族制度情報取得。                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public void cmBTnpskB_getMSKazoku() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getMSKazoku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ             */

        /* 初期化 */
        h_kazoku_nenji_rank_cd.arr = 0;
        h_kazoku_getuji_rank_cd.arr = 0;

        sprintf(h_str_sql,
                "SELECT 年次ランクコード%s, " +
                        "       月次ランクコード%s%s " +
                        "  FROM MS家族制度情報 " +
                        " WHERE 家族ＩＤ  =  ? ",
                gl_sys_y, gl_sys_kbn, gl_sys_month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getMSKazoku *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat6 from:
        SqlstmDto sqlca = sqlcaManager.get("sql_stat6");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getMSKazoku *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTnpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(buff, "%s SQL解析NG。STATUS=%d、%s", aplog_yyyymm,
                    sqlca.sqlcode, h_str_sql);
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    buff, 0, 0, 0, 0);
            return;
        }

        sqlca.declare();
        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL06 CURSOR FOR sql_stat6;

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL06 USING:
//        h_kazoku_id;
        sqlca.open(h_kazoku_id);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getMSKazoku *** MS家族制度情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "%s カーソルオープンエラー。" +
                            "STATUS=[%d](TBL:MS家族制度情報)顧客番号=[%d]",
                    aplog_yyyymm, sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getMSKazoku *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */
        sqlca.fetchInto(h_kazoku_nenji_rank_cd,   /* 家族年次ランクコード           */
                /* 家族月次ランクコード           */
                h_kazoku_getuji_rank_cd);
//        EXEC SQL FETCH CUR_SQL06
//        INTO:
//        h_kazoku_nenji_rank_cd,     /* 家族年次ランクコード           */
//         :h_kazoku_getuji_rank_cd; /* 家族月次ランクコード           */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {

            sprintf(buff, "%s フェッチエラー。STATUS=[%d]" +
                            " MS家族制度情報 顧客番号=[%d]", aplog_yyyymm,
                    sqlca.sqlcode, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_getMSKazoku *** %s\n", buff);
            C_DbgEnd("cmBTnpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL06;
            sqlca.curse_close();
            return;
        }
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL06;

        sqlca.curse_close();
        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],月次ランクコード%s%s:[%d]",
                gl_sys_y, h_kazoku_nenji_rank_cd, gl_sys_kbn, gl_sys_month,
                h_kazoku_getuji_rank_cd);
        C_DbgMsg("*** cmBTnpskB_getMSKazoku *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getMSKazoku処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return;              /* 処理終了                           */
        /*-----cmBTnpskB_getMSKazoku Bottom-------------------------------------------*/
    }

    /* 2022/12/02 MCCM初版 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_getTSRank                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_getTSRank()                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TSランク情報取得。                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public int cmBTnpskB_getTSRank() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_getTSRank処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ              */

        /* 初期化 */
        h_rank_nenji_rank_cd.arr = 0;
        h_rank_getuji_rank_cd.arr = 0;

        sprintf(h_str_sql,
                "SELECT 年次ランクコード%s, "
                        + "       月次ランクコード%s%s, "
                        + "       年間ランクＵＰ対象金額%s, "
                        + "       月間ランクＵＰ対象金額%s%s "
                        + "  FROM TSランク情報 "
                        + " WHERE 顧客番号  =  ? ",
                gl_sys_y, gl_sys_kbn, gl_sys_month,
                gl_sys_y, gl_sys_kbn, gl_sys_month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getTSRank *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        SqlstmDto sqlca = sqlcaManager.get("sql_stat_tsrank");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_stat_tsrank from:
//        h_str_sql;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL_TSRANK CURSOR FOR sql_stat_tsrank;

        sqlca.declare();
        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL_TSRANK USING:
//        h_kokyaku_no;

        sqlca.open(h_kokyaku_no);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_getTSRank *** TSランク情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "顧客番号=[%d]", h_kokyaku_no);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* カーソルフェッチ */
        sqlca.fetchInto(h_rank_nenji_rank_cd,         /* TS年次ランクコード           */
                h_rank_getuji_rank_cd,        /* TS月次ランクコード           */
                h_rank_nenkan_rankup_kingaku, /* TSランク情報 年間ランクアップ対象金額 */
                h_rank_gekkan_rankup_kingaku /* TSランク情報 月間ランクアップ対象金額 */);
//        EXEC SQL FETCH CUR_SQL_TSRANK
//        INTO:
//        h_rank_nenji_rank_cd,         /* TS年次ランクコード           */
//         :h_rank_getuji_rank_cd,        /* TS月次ランクコード           */
//         :h_rank_nenkan_rankup_kingaku, /* TSランク情報 年間ランクアップ対象金額 */
//         :h_rank_gekkan_rankup_kingaku; /* TSランク情報 月間ランクアップ対象金額 */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "顧客番号=[%d]", h_kokyaku_no);
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTnpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL_TSRANK;
            sqlca.curse_close();
            return C_const_NG;
        }
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL_TSRANK;
        sqlca.curse_close();
        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],"
                        + "月次ランクコード%s%s:[%d],"
                        + "年間ランクアップ対象金額%s:[%f],"
                        + "月間ランクアップ対象金額%s%s:[%f],"
                        + "顧客番号:[%d]",
                gl_sys_y, h_rank_nenji_rank_cd.intVal(),
                gl_sys_kbn, gl_sys_month, h_rank_getuji_rank_cd.intVal(),
                gl_sys_y, h_rank_nenkan_rankup_kingaku.floatVal(),
                gl_sys_kbn, gl_sys_month, h_rank_gekkan_rankup_kingaku.floatVal(),
                h_kokyaku_no);
        C_DbgMsg("*** cmBTnpskB_getTSRank *** %s\n", buff);
        C_DbgEnd("cmBTnpskB_getTSRank処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (C_const_OK);              /* 処理終了                           */
        /*-----cmBTnpskB_getTSRank Bottom---------------------------------------------*/
    }
    /* 2022/12/01 MCCM初版 ADD END */

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_tujo_insertHS                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_tujo_insertHS(struct TS_SERCH ts_ser )    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別情報（通常ポイント失効）登録。                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser          登録用データ格納構造体                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    public int cmBTnpskB_tujo_insertHS(TS_SERCH ts_ser) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_tujo_insertHS処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_sysdate;           /* システム日付                  */
        int wk_i;                 /* 使用ランクコードの算出用Index */
        ItemDto[] wk_runk = new ItemDto[4];           /* 使用ランクコードの算出用配列  */
        int wk_batdate;           /* バッチ処理日付                */
        int wk_month;             /* バッチ処理月                  */
        int wk_exmonth;           /* バッチ処理日の前月            */
        int rtn_cd;                        /* 戻り値                        */
        IntegerDto rtn_status = new IntegerDto();                          /* 結果ステータス                */
        StringDto buff = new StringDto(256);                          /* バッファ                      */

        /* ポイント日別情報構造体初期化 */
        hsptymd_t = new TSHS_DAY_POINT_TBL();
        memset(hsptymd_t, 0x00, sizeof(hsptymd_t));

        /* 更新前利用可能ポイント確認 */
        /* 各年度の通常ポイントの失効状況確認 */
        cmBTnpskB_check_tujo_point(ts_ser.ex_year, ts_ser.this_year);

        /* 各月の期間限定ポイントの失効状況確認処理 */
        cmBTnpskB_check_kikan_point();

        /* システム日時取得 */
        rtn_cd = C_GetSysDateTime(gl_sysdate, gl_systime);
        if (rtn_cd != C_const_OK) {
            sprintf(buff, "%s 日付時刻取得処理に失敗しました。", aplog_yyyymm);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
        }

        wk_sysdate = atoi(gl_sysdate);
        wk_batdate = atoi(gl_bat_date);
        /* 期間限定Ｐの基準月を設定（バッチ処理日の前月） */
        wk_month = atoi(gl_bat_date_mm);
        if (wk_month != 1) {
            wk_exmonth = wk_month - 1;
        } else {
            wk_exmonth = 12;
        }

        /* ---追加情報をセット--------------------------------------------------- */
        hsptymd_t.system_ymd.arr = wk_sysdate;                           /* システム年月日               */
        strcpy(hsptymd_t.kokyaku_no, h_ts.vr_kokyaku_no);
        hsptymd_t.kokyaku_no.len = strlen(h_ts.vr_kokyaku_no);
        /* 顧客番号                     */
        hsptymd_t.kaiin_kigyo_cd = h_kigyo_cd;                          /* 会員企業コード               */
        hsptymd_t.kaiin_kyu_hansya_cd = h_kyu_hansya_cd;                     /* 会員旧販社コード             */
        strcpy(hsptymd_t.kaiin_no, h_kaiin_no);
        hsptymd_t.kaiin_no.len = strlen(h_kaiin_no);
        /* 会員番号                     */

        /* 2022/12/02 MCCM初版 DEL START */
        /* hsptymd_t.nyukai_kigyo_cd                        = h_ts.nyukai_kigyo_cd; */               /* 入会企業コード               */
        /* hsptymd_t.nyukai_tenpo                           = h_ts.nyukai_tenpo; */                  /* 入会店舗                     */
        /* hsptymd_t.hakken_kigyo_cd                        = h_ts.hakken_kigyo_cd; */               /* 発券企業コード               */
        /* hsptymd_t.hakken_tenpo                           = h_ts.hakken_tenpo; */                  /* 発券店舗                     */
        /* 2022/12/02 MCCM初版 DEL END */

        hsptymd_t.toroku_ymd.arr = wk_batdate;
        /* 登録年月日                   */
        hsptymd_t.data_ymd.arr = wk_batdate;                          /* データ年月日                 */

        /* 2022/12/02 MCCM初版 DEL START */
        /* hsptymd_t.kigyo_cd                               = h_ts.nyukai_kigyo_cd; */               /* 企業コード                   */
        /* hsptymd_t.mise_no                                = h_ts.nyukai_tenpo; */                  /* 店番号                       */
        /* 2022/12/02 MCCM初版 DEL END */

        hsptymd_t.riyu_cd.arr = TUJO_RIYU_CD;                        /* 理由コード                   */
        hsptymd_t.card_nyuryoku_kbn.arr = 4;                                   /* カード入力区分               */
        hsptymd_t.riyo_point = h_ts.tujoP_expired;
        /* 利用ポイント                 */

        /* 2022/12/02 MCCM初版 MOD START */
        /* hsptymd_t.kojin_getuji_rank_cd                   = h_getuji_rank_cd; */                   /* 個人月次ランクコード         */
        /* hsptymd_t.kojin_nenji_rank_cd                    = h_nenji_rank_cd; */                    /* 個人年次ランクコード         */
        hsptymd_t.kojin_getuji_rank_cd = h_rank_getuji_rank_cd;               /* 個人月次ランクコード         */
        hsptymd_t.kojin_nenji_rank_cd = h_rank_nenji_rank_cd;                /* 個人年次ランクコード         */
        /* 2022/12/02 MCCM初版 MOD END */

        hsptymd_t.kazoku_getuji_rank_cd = h_kazoku_getuji_rank_cd;             /* 家族月次ランクコード         */
        hsptymd_t.kazoku_nenji_rank_cd = h_kazoku_nenji_rank_cd;              /* 家族年次ランクコード         */
        /* 使用ランクコードの算出 */
        wk_runk[0] = h_getuji_rank_cd;
        wk_runk[1] = h_nenji_rank_cd;
        wk_runk[2] = h_kazoku_nenji_rank_cd;
        wk_runk[3] = h_kazoku_getuji_rank_cd;
        hsptymd_t.shiyo_rank_cd.arr = 0;
        for (wk_i = 0; wk_i <= 3; wk_i++) {
            if (wk_runk[wk_i].intVal() > hsptymd_t.shiyo_rank_cd.intVal()) {
                hsptymd_t.shiyo_rank_cd = wk_runk[wk_i];                       /* 使用ランクコード             */
            }
        }
        hsptymd_t.kaiage_cnt = h_kaiage_cnt;                 /* 買上回数                     */
        hsptymd_t.koshinmae_riyo_kano_point.arr
                = h_ts.tujoP_expired.intVal() + h_ts.tujoP_zennen.intVal() + h_ts.tujoP_tonen.intVal() + h_ts.kikanP_ex.intVal()
                + h_ts.kikanP_this00.intVal() + h_ts.kikanP_this01.intVal() + h_ts.kikanP_this02.intVal()
                + h_ts.kikanP_this03.intVal();
        /* 更新前利用可能ポイント       */

        /* 2022/12/02 MCCM初版 MOD START */
    /* hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku
        = h_gekkan_rankup_kingaku; */
        /* 更新前月間ランクＵＰ対象金額 */
    /* hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku
        = h_nenkan_rankup_kingaku; */
        /* 更新前年間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku = h_rank_gekkan_rankup_kingaku; /* 更新前月間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku = h_rank_nenkan_rankup_kingaku; /* 更新前年間ランクＵＰ対象金額 */
        /* 2022/12/02 MCCM初版 MOD END */

        /* 2022/12/01 MCCM初版 MOD START */
        /* hsptymd_t.kazoku_id                                     = h_kazoku_id; */               /* 家族ＩＤ                     */
        sprintf(hsptymd_t.kazoku_id, "%d", h_kazoku_id.longVal());                                     /* 家族ＩＤ                     */
        /* 2022/12/01 MCCM初版 MOD END */

        /* ディレイ更新日時             */    /* = 共通関数内でSYSDATE設定 */
        strcpy(hsptymd_t.delay_koshin_apl_version, Program_Name_Ver);            /* ディレイ更新ＡＰＬバージョン */
        hsptymd_t.batch_koshin_ymd.arr = wk_sysdate;                    /* バッチ更新日                 */
        hsptymd_t.saishu_koshin_ymd.arr = wk_sysdate;                    /* 最終更新日                   */
        /* 最終更新日時                 */    /* = 共通関数内でSYSDATE設定 */
        strcpy(hsptymd_t.saishu_koshin_programid, Program_Name);                /* 最終更新プログラムＩＤ       */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo = ts_ser.ex_year;               /* 更新前利用可能通常Ｐ基準年度 */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo = h_ts.tujoP_expired;
        /* 更新前利用可能通常Ｐ前年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo = h_ts.tujoP_zennen;
        /* 更新前利用可能通常Ｐ当年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo = h_ts.tujoP_tonen;
        /* 更新前利用可能通常Ｐ翌年度   */
        hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo = ts_ser.ex_year;
        /* 要求付与通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point = h_ts.tujoP_expired;
        /* 要求利用通常Ｐ               */
        hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo = ts_ser.ex_year;
        /* 要求利用通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point_zennendo = h_ts.tujoP_expired;
        /* 要求利用通常Ｐ前年度         */
        hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo = ts_ser.ex_year;
        /* 更新付与通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point = h_ts.tujoP_expired;
        /* 更新利用通常Ｐ               */
        hsptymd_t.koshin_riyo_tujo_point_kijun_nendo = ts_ser.ex_year;
        /* 更新利用通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point_zennendo = h_ts.tujoP_expired;
        /* 更新利用通常Ｐ前年度         */
        hsptymd_t.koshinmae_kikan_gentei_point_kijun_month.arr = wk_exmonth;
        /* 更新前期間限定Ｐ基準月       */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0 = h_ts.kikanP_ex;
        /* 更新前利用可能期間限定Ｐ０   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1 = h_ts.kikanP_this00;
        /* 更新前利用可能期間限定Ｐ１   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2 = h_ts.kikanP_this01;
        /* 更新前利用可能期間限定Ｐ２   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3 = h_ts.kikanP_this02;
        /* 更新前利用可能期間限定Ｐ３   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4 = h_ts.kikanP_this03;
        /* 更新前利用可能期間限定Ｐ４   */
        hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month.arr = wk_exmonth;
        /* 要求付与期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month.arr = wk_exmonth;
        /* 要求利用期間限定Ｐ基準月     */
        hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month.arr = wk_exmonth;
        /* 更新付与期間限定Ｐ基準月     */
        hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month.arr = wk_exmonth;
        /* 更新利用期間限定Ｐ基準月     */

        /* 2022/12/02 MCCM初版 ADD START */
        hsptymd_t.kaisha_cd_mcc = h_ts.nyukai_kigyo_cd_mcc;     /* 会社コードＭＣＣ               */
        hsptymd_t.mise_no_mcc = h_ts.nyukai_tenpo_mcc;        /* 店番号ＭＣＣ                   */
        hsptymd_t.meisai_su.arr = gl_meisai_su;                 /* 通常Ｐ明細数                   */
        hsptymd_t.nyukai_kaisha_cd_mcc = h_ts.nyukai_kigyo_cd_mcc;     /* 入会会社コードＭＣＣ           */
        hsptymd_t.nyukai_tenpo_mcc = h_ts.nyukai_tenpo_mcc;        /* 入会店舗ＭＣＣ                 */
        /* 2022/12/02 MCCM初版 ADD END */

        /*----------------------------------------------------------------*/
        C_DbgMsg("*** HSポイント日別情報追加結果 *** %s\n", "入力引数情報");
        C_DbgMsg("システム年月日                 =[%d]\n", hsptymd_t.system_ymd.longVal());
        C_DbgMsg("顧客番号                       =[%s]\n",
                hsptymd_t.kokyaku_no.arr);
        C_DbgMsg("会員企業コード                 =[%d]\n",
                hsptymd_t.kaiin_kigyo_cd.longVal());
        C_DbgMsg("会員旧販社コード               =[%d]\n",
                hsptymd_t.kaiin_kyu_hansya_cd.longVal());
        C_DbgMsg("会員番号                       =[%s]\n",
                hsptymd_t.kaiin_no.arr);
        C_DbgMsg("入会企業コード                 =[%d]\n",
                hsptymd_t.nyukai_kigyo_cd.longVal());
        C_DbgMsg("入会店舗                       =[%d]\n",
                hsptymd_t.nyukai_tenpo.longVal());
        C_DbgMsg("発券企業コード                 =[%d]\n",
                hsptymd_t.hakken_kigyo_cd.longVal());
        C_DbgMsg("発券店舗                       =[%d]\n",
                hsptymd_t.hakken_tenpo.longVal());
        C_DbgMsg("精算年月日                   0 =[%d]\n",
                hsptymd_t.seisan_ymd.longVal());
        C_DbgMsg("登録年月日                     =[%d]\n",
                hsptymd_t.toroku_ymd.longVal());
        C_DbgMsg("データ年月日             today =[%d]\n",
                hsptymd_t.data_ymd.longVal());
        C_DbgMsg("企業コード                     =[%d]\n",
                hsptymd_t.kigyo_cd.longVal());
        C_DbgMsg("店番号                         =[%d]\n",
                hsptymd_t.mise_no.longVal());
        C_DbgMsg("ターミナル番号               0 =[%d]\n",
                hsptymd_t.terminal_no.longVal());
        /* 2022/12/02 MCCM初版 MOD START */
        /* C_DbgMsg( "取引番号                     0 =[%d]\n"     , */
        C_DbgMsg("取引番号                     0 =[%f]\n",
                /* 2022/12/02 MCCM初版 MOD END */
                hsptymd_t.torihiki_no.floatVal());
        C_DbgMsg("時刻                         0 =[%d]\n",
                hsptymd_t.jikoku_hms.longVal());
        C_DbgMsg("理由コード                1193 =[%d]\n",
                hsptymd_t.riyu_cd.longVal());
        C_DbgMsg("カード入力区分               4 =[%d]\n",
                hsptymd_t.card_nyuryoku_kbn.longVal());
        C_DbgMsg("処理対象ファイルレコード番号 0 =[%d]\n",
                hsptymd_t.shori_taisho_file_record_no.longVal());
        C_DbgMsg("リアル更新フラグ             0 =[%d]\n",
                hsptymd_t.real_koshin_flg.longVal());
        C_DbgMsg("付与ポイント                 0 =[%.0f]\n",
                hsptymd_t.fuyo_point.floatVal());
        C_DbgMsg("利用ポイント                   =[%.0f]\n",
                hsptymd_t.riyo_point.floatVal());
        C_DbgMsg("基本Ｐ率対象ポイント         0 =[%.0f]\n",
                hsptymd_t.kihon_pritsu_taisho_point.floatVal());
        C_DbgMsg("ランクＵＰ対象金額           0 =[%.0f]\n",
                hsptymd_t.rankup_taisho_kingaku.floatVal());
        C_DbgMsg("ポイント対象金額             0 =[%.0f]\n",
                hsptymd_t.point_taisho_kingaku.floatVal());
        C_DbgMsg("サービス券発行枚数           0 =[%d]\n",
                hsptymd_t.service_hakko_maisu.longVal());
        C_DbgMsg("サービス券利用枚数           0 =[%d]\n",
                hsptymd_t.service_riyo_maisu.longVal());
        C_DbgMsg("個人月次ランクコード           =[%d]\n",
                hsptymd_t.kojin_getuji_rank_cd.longVal());
        C_DbgMsg("個人年次ランクコード           =[%d]\n",
                hsptymd_t.kojin_nenji_rank_cd.longVal());
        C_DbgMsg("家族月次ランクコード           =[%d]\n",
                hsptymd_t.kazoku_getuji_rank_cd.longVal());
        C_DbgMsg("家族年次ランクコード           =[%d]\n",
                hsptymd_t.kazoku_nenji_rank_cd.longVal());
        C_DbgMsg("使用ランクコード               =[%d]\n",
                hsptymd_t.shiyo_rank_cd.longVal());
        C_DbgMsg("買上額                       0 =[%.0f]\n",
                hsptymd_t.kaiage_kingaku.floatVal());
        C_DbgMsg("買上回数                       =[%.0f]\n",
                hsptymd_t.kaiage_cnt.floatVal());
        C_DbgMsg("更新前利用可能ポイント         =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_point.floatVal());
        C_DbgMsg("更新前付与ポイント             =[%.0f]\n",
                hsptymd_t.koshinmae_fuyo_point.floatVal());
        C_DbgMsg("更新前基本Ｐ率対象ポイント     =[%.0f]\n",
                hsptymd_t.koshinmae_kihon_pritsu_taisho_point.floatVal());
        C_DbgMsg("更新前月間ランクＵＰ対象金額   =[%.0f]\n",
                hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku.floatVal());
        C_DbgMsg("更新前年間ランクＵＰ対象金額   =[%.0f]\n",
                hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku.floatVal());
        C_DbgMsg("更新前ポイント対象金額         =[%.0f]\n",
                hsptymd_t.koshinmae_point_taisho_kingaku.floatVal());
        C_DbgMsg("更新前買上額                   =[%.0f]\n",
                hsptymd_t.koshinmae_kaiage_kingaku.floatVal());

        /* 2022/12/02 MCCM初版 MOD START */
    /* C_DbgMsg( "家族ＩＤ                       =[%d]\n"     ,
                                    hsptymd_t.kazoku_id                       ); */
        C_DbgMsg("家族ＩＤ                       =[%s]\n",
                hsptymd_t.kazoku_id.arr);
        /* 2022/12/02 MCCM初版 MOD END */

        C_DbgMsg("更新前月間家族ランクＵＰ金額   =[%.0f]\n",
                hsptymd_t.koshinmae_gekkan_kazoku_rankup_taisho_kingaku.floatVal());
        C_DbgMsg("更新前年間家族ランクＵＰ金額   =[%.0f]\n",
                hsptymd_t.koshinmae_nenkan_kazoku_rankup_taisho_kingaku.floatVal());
        C_DbgMsg("リアル更新日時              nl =[%d]\n",
                hsptymd_t.real_koshin_ymd.longVal());
        C_DbgMsg("リアル更新ＡＰＬバージョン  nl =[%s]\n",
                hsptymd_t.real_koshin_apl_version);
        C_DbgMsg("ディレイ更新日時         today =[%d]\n",
                hsptymd_t.delay_koshin_ymd.longVal());
        C_DbgMsg("ディレイ更新ＡＰＬバージョン   =[%s]\n",
                hsptymd_t.delay_koshin_apl_version);
        C_DbgMsg("相殺フラグ                   0 =[%d]\n",
                hsptymd_t.sosai_flg.longVal());
        C_DbgMsg("明細チェックフラグ           0 =[%d]\n",
                hsptymd_t.mesai_check_flg.longVal());
        C_DbgMsg("明細チェック区分             0 =[%d]\n",
                hsptymd_t.mesai_check_kbn.longVal());
        C_DbgMsg("作業企業コード               0 =[%d]\n",
                hsptymd_t.sagyo_kigyo_cd.longVal());
        C_DbgMsg("作業者ＩＤ                   0 =[%f]\n",
                hsptymd_t.sagyosha_id.floatVal());
        C_DbgMsg("作業年月日                   0 =[%d]\n",
                hsptymd_t.sagyo_ymd.longVal());
        C_DbgMsg("作業時刻                     0 =[%d]\n",
                hsptymd_t.sagyo_hms.longVal());
        C_DbgMsg("バッチ更新日             today =[%d]\n",
                hsptymd_t.batch_koshin_ymd.longVal());
        C_DbgMsg("最終更新日               today =[%d]\n",
                hsptymd_t.saishu_koshin_ymd.longVal());
        C_DbgMsg("最終更新日時             today =[%f]\n",
                hsptymd_t.saishu_koshin_ymdhms.floatVal());
        C_DbgMsg("最終更新プログラムＩＤ         =[%s]\n",
                hsptymd_t.saishu_koshin_programid);
        C_DbgMsg("要求利用Ｐ内訳フラグ         0 =[%d]\n",
                hsptymd_t.yokyu_riyo_putiwake_flg.longVal());
        C_DbgMsg("更新前利用可能通常Ｐ基準年度   =[%d]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo.longVal());
        C_DbgMsg("更新前利用可能通常Ｐ前年度     =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo.floatVal());
        C_DbgMsg("更新前利用可能通常Ｐ当年度     =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo.floatVal());
        C_DbgMsg("更新前利用可能通常Ｐ翌年度     =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo.floatVal());
        C_DbgMsg("要求付与通常Ｐ               0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_point.floatVal());
        C_DbgMsg("要求付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo.longVal());
        C_DbgMsg("要求付与通常Ｐ前年度         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_zennendo.floatVal());
        C_DbgMsg("要求付与通常Ｐ当年度         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_tonendo.floatVal());
        C_DbgMsg("要求利用通常Ｐ                 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point.floatVal());
        C_DbgMsg("要求利用通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo.longVal());
        C_DbgMsg("要求利用通常Ｐ前年度           =[%.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_zennendo.floatVal());
        C_DbgMsg("要求利用通常Ｐ当年度         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_tonendo.floatVal());
        C_DbgMsg("要求利用通常Ｐ翌年度         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_yokunendo.floatVal());
        C_DbgMsg("更新付与通常Ｐ               0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point.floatVal());
        C_DbgMsg("更新付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg("更新付与通常Ｐ前年度         0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_zennendo.floatVal());
        C_DbgMsg("更新付与通常Ｐ当年度         0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_tonendo.floatVal());
        C_DbgMsg("更新利用通常Ｐ               0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point.floatVal());
        C_DbgMsg("更新利用通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.koshin_riyo_tujo_point_kijun_nendo.longVal());
        C_DbgMsg("更新利用通常Ｐ前年度           =[%.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_zennendo.floatVal());
        C_DbgMsg("更新利用通常Ｐ当年度           =[%.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_tonendo.floatVal());
        C_DbgMsg("更新利用通常Ｐ翌年度           =[%.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_yokunendo.floatVal());
        C_DbgMsg("更新前期間限定Ｐ基準月         =[%d]\n",
                hsptymd_t.koshinmae_kikan_gentei_point_kijun_month.longVal());
        C_DbgMsg("更新前利用可能期間限定Ｐ０    =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0.floatVal());
        C_DbgMsg("更新前利用可能期間限定Ｐ１    =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1.floatVal());
        C_DbgMsg("更新前利用可能期間限定Ｐ２    =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2.floatVal());
        C_DbgMsg("更新前利用可能期間限定Ｐ３    =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3.floatVal());
        C_DbgMsg("更新前利用可能期間限定Ｐ４    =[%.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4.floatVal());
        C_DbgMsg("要求付与期間限定Ｐ           0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point.floatVal());
        C_DbgMsg("要求付与期間限定Ｐ基準月       =[%d]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month.longVal());
        C_DbgMsg("要求付与期間限定Ｐ０         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point0.floatVal());
        C_DbgMsg("要求付与期間限定Ｐ１         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point1.floatVal());
        C_DbgMsg("要求付与期間限定Ｐ２         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point2.floatVal());
        C_DbgMsg("要求付与期間限定Ｐ３         0 =[%.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point3.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ           0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ基準月       =[%d]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month.longVal());
        C_DbgMsg("要求利用期間限定Ｐ０           =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point0.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ１         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point1.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ２         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point2.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ３         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point3.floatVal());
        C_DbgMsg("要求利用期間限定Ｐ４         0 =[%.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point4.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ           0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ基準月       =[%d]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month.longVal());
        C_DbgMsg("更新付与期間限定Ｐ０１       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point01.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０２       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point02.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０３       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point03.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０４       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point04.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０５       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point05.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０６       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point06.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０７       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point07.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０８       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point08.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ０９       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point09.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ１０       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point10.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ１１       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point11.floatVal());
        C_DbgMsg("更新付与期間限定Ｐ１２       0 =[%.0f]\n",
                hsptymd_t.koshin_fuyo_kikan_gentei_point12.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ           0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ基準月       =[%d]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month.longVal());
        C_DbgMsg("更新利用期間限定Ｐ０１       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point01.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０２       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point02.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０３       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point03.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０４       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point04.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０５       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point05.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０６       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point06.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０７       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point07.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０８       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point08.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ０９       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point09.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ１０       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point10.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ１１       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point11.floatVal());
        C_DbgMsg("更新利用期間限定Ｐ１２       0 =[%.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point12.floatVal());
        /*----------------------------------------------------------------*/

        /* HSポイント日別情報追加 */
//    rtn_cd = C_InsertDayPoint(&hsptymd_t, wk_sysdate,rtn_status);
        rtn_cd = cmBTfuncB.C_InsertDayPoint(hsptymd_t, atoi(gl_bat_date), rtn_status);
        if (rtn_cd != C_const_OK) {
            sprintf(buff, "C_InsertDayPoint %s", aplog_yyyymm);
            APLOG_WT("903", 0, null, buff, rtn_cd, 0, 0, 0, 0);
            /*----------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_tujo_insertHS *** " +
                    "C_InsertDayPoint rtn_cd=[%d]\n", rtn_cd);
            C_DbgMsg("*** cmBTnpskB_tujo_insertHS *** C_InsertDayPoint " +
                    "rtn_status=[%d]\n", rtn_status);
            /*----------------------------------------------------------------*/
            /* 処理を終了する */
            return C_const_NG;
        }
        w_seq = hsptymd_t.shori_seq.floatVal();
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_tujo_insertHS処理処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;              /* 処理終了                           */
        /*-----cmBTnpskB_tujo_insertHS処理r Bottom------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_updTS                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  cmBTnpskB_updTS (unsigned short int year);                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報の更新を行う。                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     unsigned short int year : 失効対象年度                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_updTS(int year) {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_updTS処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(256);                         /*  バッファ                      */

        h_sysdate = atoi(gl_sysdate);

        /* 失効対象年度のフラグ更新 */
        cmBTnpskB_upd_tujo_bit(year);
        SqlstmDto sqlca = sqlcaManager.get("sql_stat_hsuchiwake");
        /* 該当データを更新する */
        sqlca.sql.arr = "UPDATE TS利用可能ポイント情報" +
                "        SET 最終更新日 = CAST(? AS NUMERIC)," +
                "                最終更新日時 = SYSDATE()," +
                "                最終更新プログラムＩＤ = ?," +
                "                通常Ｐ失効フラグ = ?" +
                "        WHERE 顧客番号 = ?";
//        EXEC SQL UPDATE TS利用可能ポイント情報
//        SET 最終更新日 = :h_sysdate,
//                最終更新日時 = SYSDATE,
//                最終更新プログラムＩＤ = :Program_Name,
//                通常Ｐ失効フラグ = :h_ts.tujo_p_shiko_flg
//        WHERE 顧客番号 = :h_kokyaku_no;

        sqlca.restAndExecute(h_sysdate, Program_Name, h_ts.tujo_p_shiko_flg, h_kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "顧客番号=[%d] %s", h_kokyaku_no, aplog_yyyymm);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_updTSt *** TS利用可能情報更新 エラー%s\n", buff);
            C_DbgEnd(" cmBTnpskB_updTS処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /*------------------------------------------------------------*/
        sprintf(buff, "更新完了 顧客番号:[%d]、通常Ｐ失効フラグ:[%d]"
                , h_kokyaku_no, h_ts.tujo_p_shiko_flg);
        C_DbgMsg("*** cmBTnpskB_updTSt *** %s\n", buff);
        C_DbgEnd(" cmBTnpskB_updTS処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;              /* 処理終了                           */
        /*-----cmBTnpskB_updTS Bottom-------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_upd_tujo_bit                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_upd_tujo_bit(unsigned short int year)     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度に対応するフラグを失効済みにする                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       unsigned short int year    対象年度                                  */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */

    /******************************************************************************/
    void cmBTnpskB_upd_tujo_bit(int year) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_upd_tujo_bit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_year_cd;                  /* 対象年度コード         */
        int check_flg;                   /* 対象年度フラグ         */
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 対象年度コード算出 */
        wk_year_cd = (year % 5);

        /* 対象年度フラグ設定*/
        check_flg = (TUJO_BASE_BIT << (4 - wk_year_cd) | TUJO_CHENGE_BIT);

        /*------------------------------------------------------------*/
        sprintf(buff, "更新前通常Ｐ失効フラグ：[%#x]、失効対象年度：[%d]、" +
                        "対象年度のbit：[%#x]",
                h_ts.tujo_p_shiko_flg.intVal(), year, check_flg);
        C_DbgMsg("*** cmBTnpskB_upd_tujo_bit *** %s\n", buff);
        /*------------------------------------------------------------*/

        /* 失効対象年度のフラグ更新 */
        h_ts.tujo_p_shiko_flg.arr = h_ts.tujo_p_shiko_flg.intVal() | check_flg;

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_upd_tujo_bit *** 更新後通常Ｐ失効フラグ：[%#x]\n",
                h_ts.tujo_p_shiko_flg.intVal());
        C_DbgEnd("cmBTnpskB_upd_tujo_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTnpskB_upd_tujo_bit処理 ---------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_ChkArgiInf                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTnpskB_ChkArgiInf( char *Arg_in )                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：引数                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTnpskB_ChkArgiInf(StringDto Arg_in) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("*** cmBTnpskB_ChkArgiInf処理 ***");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_buff = new StringDto(10);                               /* バッファ              */
        int wk_arg_year;                 /* 抽出対象年度          */
        int wk_month;                    /* 抽出対象年月          */
        int wk_bat_year;                 /* バッチ処理日年        */
        StringDto log_buff = new StringDto(128);                             /* ログ出力用バッファ    */

        if (memcmp(Arg_in, DEF_ARG_Y, 2) == 0) {       /* -y抽出対象年CHK      */
            /* 重複指定チェック */
            if (arg_y_chk != DEF_OFF) {
                sprintf(log_buff,
                        "引数が重複しています（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_ChkArgiInf *** 重複指定NG(y)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            /* 設定値nullチェック */
            if (Arg_in.arr == null || Arg_in.arr.length() <= 2) {
                sprintf(log_buff,
                        "引数が設定されていません（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_ChkArgiInf *** 設定値null(y)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            /* 設定値の範囲チェック */
            strcpy(wk_buff, Arg_in.substring(2));
            wk_arg_year = atoi(wk_buff);
            wk_bat_year = atoi(gl_bat_date_yyyy);
            if (wk_bat_year < wk_arg_year) {
                sprintf(log_buff,
                        "引数がバッチ日付年より未来です（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_ChkArgiInf *** 設定値範囲外(y)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            arg_y_chk = DEF_ON;
            gl_select_year = wk_arg_year;
        } else if (memcmp(Arg_in, DEF_ARG_M, 2) == 0) {  /* -m抽出対象月CHK      */
            /* 重複指定チェック */
            if (arg_m_chk != DEF_OFF) {
                sprintf(log_buff,
                        "引数が重複しています（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcdupB_ChkArgiInf *** 重複指定NG(m)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            /* 設定値nullチェック */
            if (Arg_in.arr == null || Arg_in.arr.length() <= 2) {
                sprintf(log_buff,
                        "引数が設定されていません（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcdupB_ChkArgiInf *** 設定値null(m)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            /* 設定値の範囲チェック */
            strcpy(wk_buff, Arg_in.substring(2));
            wk_month = atoi(wk_buff);
            if (wk_month < 1 || 12 < wk_month) {
                sprintf(log_buff,
                        "引数が月の範囲外です（%s）", Arg_in);
                APLOG_WT("910", 0, null, log_buff, 0, 0, 0, 0, 0);
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTnpskB_ChkArgiInf *** 設定値範囲外(m)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_NG;
            }
            arg_m_chk = DEF_ON;
            gl_select_month = wk_month;
        } else if (memcmp(Arg_in, DEF_ARG_A, 2) == 0) {  /* -a処理件数CHK      */
            arg_a_chk = DEF_ON;

            /* 設定値nullチェック */
            if (Arg_in.arr == null || Arg_in.arr.length() <= 2) {
                /* カウント値設定 */
                gl_aplog_cnt = PROCESSING_CNT;
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** cmBTcdupB_ChkArgiInf *** 設定値null(a)=%s\n",
                        Arg_in);
                /*-------------------------------------------------------------*/
                return C_const_OK;
            }
            strcpy(wk_buff, Arg_in.substring(2));
            gl_aplog_cnt = atoi(wk_buff);
        }

        /*---------------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_ChkArgiInf処理", C_const_OK, 0, 0);
        /*---------------------------------------------------------------------*/

        return C_const_OK;
    }

    /* 2022/12/02 MCCM初版 ADD START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTnpskB_insertHSUchiwake                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTnpskB_insertHSUchiwake(unsigned short int year) */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別内訳情報（通常ポイント失効）登録。                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     unsigned short int year : 失効対象年度                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */

    /******************************************************************************/
    int cmBTnpskB_insertHSUchiwake(int year) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTnpskB_InsertHSUchiwake処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_i_loop;                /* 明細番号                  */
        int wk_data_no;                     /* 登録データ番号            */
        ItemDto[] wk_riyo_point = new ItemDto[3];               /* 利用ポイント              */
        int[] wk_kobai_kbn = new int[3];                /* 購買区分                  */
        StringDto wk_column = new StringDto(4096);                /* 登録列                    */
        StringDto wk_column_data = new StringDto(4096);           /* 登録データ                */
        StringDto wk_data_no_text = new StringDto(5);             /* データ番号文字列          */
        StringDto buff = new StringDto(2048);                     /* バッファ                  */
        StringDto wk_c = new StringDto(4096);                     /* 登録列                    */
        StringDto wk_cd = new StringDto(4096);                    /* 登録データ                    */

        double wk_shori_seq;

        /*-----------------------------------------------*/
        /*  変数初期化                                   */
        /*-----------------------------------------------*/
        memset(wk_riyo_point, 0x00, sizeof(wk_riyo_point));
        memset(wk_kobai_kbn, 0x00, sizeof(wk_kobai_kbn));
        memset(wk_c, 0x00, sizeof(wk_c));
        memset(wk_cd, 0x00, sizeof(wk_cd));
        wk_data_no = 0;

        /* 登録データ配列に購買Ｐデータをセットする */
        if (h_ts.shikko_kobai_p.intVal() != 0) {
            wk_riyo_point[wk_data_no] = h_ts.shikko_kobai_p;
            wk_kobai_kbn[wk_data_no] = 1;
            wk_data_no++;
        }

        /* 登録データ配列に非購買Ｐデータをセットする */
        if (h_ts.shikko_hikobai_p.intVal() != 0) {
            wk_riyo_point[wk_data_no] = h_ts.shikko_hikobai_p;
            wk_kobai_kbn[wk_data_no] = 2;
            wk_data_no++;
        }

        /* 登録データ配列にその他Ｐデータをセットする */
        if (h_ts.shikko_sonota_p.intVal() != 0) {
            wk_riyo_point[wk_data_no] = h_ts.shikko_sonota_p;
            wk_kobai_kbn[wk_data_no] = 3;
            wk_data_no++;
        }
        wk_shori_seq = 0;
        wk_shori_seq = w_seq;

        /* 登録列作成 */
        for (wk_i_loop = 0; wk_i_loop < wk_data_no; wk_i_loop++) {
            memset(wk_data_no_text, 0x00, sizeof(wk_data_no_text));
            switch (wk_i_loop) {
                case 0:
                    strcpy(wk_data_no_text, "０１");
                    break;
                case 1:
                    strcpy(wk_data_no_text, "０２");
                    break;
                case 2:
                    strcpy(wk_data_no_text, "０３");
                    break;
            }

            memset(wk_column, 0x00, sizeof(wk_column));
            /* 登録列SQL作成 */
            sprintf(wk_column,
                    " %s,"
                            + " 付与利用区分%s,"
                            + " ポイント種別%s,"
                            + " 利用ポイント%s,"
                            + " 通常期間限定区分%s,"
                            + " ポイント有効期限%s,"
                            + " 購買区分%s ",
                    wk_column, wk_data_no_text, wk_data_no_text, wk_data_no_text,
                    wk_data_no_text, wk_data_no_text, wk_data_no_text);
            strcat(wk_c, wk_column);

            /* 登録データSQL作成 */
            memset(wk_column_data, 0x00, sizeof(wk_column_data));
            sprintf(wk_column_data,
                    " %s, 2, 2, %f, 1, %d%s, %d ",
                    wk_column_data, wk_riyo_point[wk_i_loop].floatVal(), year, "0331", wk_kobai_kbn[wk_i_loop]);

            strcat(wk_cd, wk_column_data);
            C_DbgMsg("wk_cd : %s\n", wk_cd);
            C_DbgMsg("wk_column_data : %s\n", wk_column_data);
        }

        /* SQL文字列作成 */
        sprintf(h_str_sql,
                " INSERT INTO HSポイント日別内訳情報%s"
                        + " ( "
                        + " システム年月日,"
                        + " 顧客番号,"
                        + " 処理通番,"
                        + " 枝番 "
                        + " %s "
                        + " ) "
                        + " VALUES "
                        + " ( "
                        + " %s, %s, %f, 1 "
                        + " %s "
                        + " ) ",
                gl_bat_date_yyyymm, wk_c,
                gl_sysdate, h_ts.vr_kokyaku_no.arr, wk_shori_seq, wk_cd);

        /*----------------------------------------------------------------*/
        C_DbgMsg("*** HSポイント日別内訳情報(通常Ｐ)追加結果 *** %s\n", "入力引数情報");
        C_DbgMsg("システム年月日                 =[%d]\n", h_sysdate);
        C_DbgMsg("顧客番号                       =[%s]\n", h_ts.vr_kokyaku_no.arr);
        C_DbgMsg("利用ポイント０１               =[%f]\n", wk_riyo_point[0].floatVal());
        C_DbgMsg("購買区分０１                   =[%d]\n", wk_kobai_kbn[0]);
        C_DbgMsg("利用ポイント０２               =[%f]\n", wk_riyo_point[1].floatVal());
        C_DbgMsg("購買区分０２                   =[%d]\n", wk_kobai_kbn[1]);
        C_DbgMsg("利用ポイント０３               =[%f]\n", wk_riyo_point[2].floatVal());
        C_DbgMsg("購買区分０３                   =[%d]\n", wk_kobai_kbn[2]);
        /*----------------------------------------------------------------*/

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTnpskB_InsertHSUchiwake *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        SqlstmDto sqlca = sqlcaManager.get("sql_stat_hsuchiwake");
        sqlca.sql = h_str_sql;
//        EXEC SQL PREPARE sql_stat_hsuchiwake from:
//        h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_InsertHSUchiwake *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd(" cmBTnpskB_InsertHSUchiwake処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* ＳＱＬ文を実行する */
        sqlca.restAndExecute();
//        EXEC SQL EXECUTE sql_stat_hsuchiwake;

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "顧客番号=[%s]", h_ts.vr_kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "HSポイント日別内訳情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTnpskB_updTSt *** HSポイント日別内訳情報更新 エラー%s\n", buff);
            C_DbgEnd(" cmBTnpskB_InsertHSUchiwake処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTnpskB_InsertHSUchiwake処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;              /* 処理終了 */
        /*-----cmBTnpskB_InsertHSUchiwake処理 Bottom-----------------------------*/
    }
    /* 2022/12/02 MCCM初版 ADD END */
}
