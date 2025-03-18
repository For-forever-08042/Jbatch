package jp.co.mcc.nttdata.batch.business.service.cmBTkpskB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTfuncB.CmBTfuncBImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TSHS_DAY_POINT_TBL;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *    プログラム名   ： 期間限定ポイント失効処理（cmBTkpskB）
 *
 *   【処理概要】
 *     有効期限切れの期間限定ポイントを失効する。
 *
 *   【引数説明】
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
 *     30.00 : 2020/11/30 NDBS.緒方：初版
 *     31.00 : 2021/04/15 NDBS.亀谷：HSポイント日別情報設定値修正
 *     32.00 : 2021/05/19 NDBS.緒方：更新前利用可能ポイントチェック追加
 *     33.00 : 2021/08/02 NDBS.緒方：バグ改修（TS利用可能ポイント情報取得）
 *     34.00 : 2021/12/09 SSI.上野：共通関数(C_InsertDayPoint)修正によりリコンパイル
 *     40.00 : 2022/11/30 SSI.山口 ： MCCM 初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2020 NTT DATA BUSINESS SYSTEMS CORPORATION
 ******************************************************************************/
@Service
public class CmBTkpskBServiceImpl extends CmABfuncLServiceImpl implements CmBTkpskBService {


    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    StringDto h_str_sql = new StringDto(4096);       /* 実行用SQL文字列                     */
    /* 使用テーブルヘッダーファイルをインクルード                                 */
    /* HSポイント日別情報         */
    TSHS_DAY_POINT_TBL hsptymd_t;       /* HSポイント日別情報バッファ         */
    /*---------失効予定情報取得用---------------*/
    ItemDto h_shikko_yotei_point = new ItemDto();   /* 失効予定期間限定P合計値            */
    ItemDto h_shikko_yotei_kokyaku = new ItemDto(); /* 失効予定顧客数                     */
    /*---------TS利用可能ポイント情報用---------------*/
    ItemDto h_vr_kokyaku_no = new ItemDto(16);          /* 顧客番号                          */
    ItemDto h_yokunen_p = new ItemDto();       /* 利用可能通常Ｐ翌年度               */
    ItemDto h_tonen_p = new ItemDto();         /* 利用可能通常Ｐ当年度               */
    ItemDto h_zennen_p = new ItemDto();        /* 利用可能通常Ｐ前年度               */
    /* 2021.04.15 HSポイント日別情報修正 Start */
    ItemDto h_tujo_shikko_flg = new ItemDto();  /* 通常P失効フラグ                    */
    /* 2021.04.15 HSポイント日別情報修正 End */
    int h_nyukai_kigyo_cd; /* 入会企業コード                     */
    int h_nyukai_tenpo;    /* 入会店舗                           */
    int h_hakken_kigyo_cd; /* 発券企業コード                     */
    int h_hakken_tenpo;    /* 発券店舗                           */
    ItemDto h_kikan_p = new ItemDto();         /* 期間限定P（失効P）                 */
    ItemDto h_kikan_p1 = new ItemDto();        /* 期間限定P1                         */
    ItemDto h_kikan_p2 = new ItemDto();        /* 期間限定P2                         */
    ItemDto h_kikan_p3 = new ItemDto();        /* 期間限定P3                         */
    /* 2021.04.15 HSポイント日別情報修正 Start */
    ItemDto h_kikan_p4 = new ItemDto();        /* 期間限定P4                         */
    /* 2021.04.15 HSポイント日別情報修正 End */
    ItemDto h_shikko_flg = new ItemDto();      /* 期間限定P失効フラグ                */
    /* 2022/11/30 MCCM初版 ADD START */
    ItemDto h_kikan_kobai_before_month = new ItemDto();   /* 利用可能期間限定購買Ｐ前月     */
    ItemDto h_kikan_hikobai_before_month = new ItemDto(); /* 利用可能期間限定非購買Ｐ前月   */
    ItemDto h_kikan_sonota_before_month = new ItemDto();  /* 利用可能期間限定その他Ｐ前月   */
    /* 2022/11/30 MCCM初版 ADD END */

    /*---------MSカード情報用---------------*/
    /* 2022/11/30 MCCM初版 MOD START */
    /* VARCHAR    h_kaiin_no[16]      ; */  /* 会員番号                           */
    ItemDto h_kaiin_no = new ItemDto(18);    /* 会員番号                           */
    /* 2022/11/30 MCCM初版 MOD END */

    ItemDto h_kigyo_cd = new ItemDto();        /* 企業コード                         */
    ItemDto h_kyu_hansya_cd = new ItemDto();   /* 旧販社コード                       */

    /*---------TSポイント年別情報用---------------*/
    double h_nenkan_rankup_kingaku; /* 年間ランクアップ対象金額     */
    double h_gekkan_rankup_kingaku; /* 月間ランクアップ対象金額     */
    ItemDto h_kaiage_cnt = new ItemDto();            /* 年間買上回数                 */
    /* 2022/11/30 MCCM初版 ADD START */
    ItemDto h_nyukai_kigyo_cd_mcc = new ItemDto();   /* 入会会社コードＭＣＣ         */
    ItemDto h_nyukai_tenpo_mcc = new ItemDto();      /* 入会店舗ＭＣＣ               */
    /* 2022/11/30 MCCM初版 ADD END */

    /*---------MS顧客制度情報用---------------*/
    int h_nenji_rank_cd;   /* 年次ランクコード                   */
    int h_getuji_rank_cd;  /* 月次ランクコード                   */
    ItemDto h_kazoku_id = new ItemDto();       /* 家族ID                             */
    /*---------MS家族制度情報用---------------*/
    int h_kazoku_nenji_rank_cd;   /* 家族年次ランクコード        */
    int h_kazoku_getuji_rank_cd;  /* 家族月次ランクコード        */

    /* 2022/12/01 MCCM初版 ADD START */
    /*---------TSランク情報用-----------------*/
    ItemDto h_rank_nenji_rank_cd = new ItemDto();         /* TSランク情報 年次ランクコード */
    ItemDto h_rank_getuji_rank_cd = new ItemDto();;        /* TSランク情報 月次ランクコード */
    ItemDto h_rank_nenkan_rankup_kingaku = new ItemDto(); /* TSランク情報 年間ランクアップ対象金額 */
    ItemDto h_rank_gekkan_rankup_kingaku = new ItemDto(); /* TSランク情報 月間ランクアップ対象金額 */
    /* 2022/12/01 MCCM初版 ADD END */
    double w_seq;
    /* -------各TBL検索用--------------------*/
    long h_kokyaku_no;      /* 顧客番号                           */
    int i_sysdate;                /* システム日付                       */
    /* 2021.04.15 HSポイント日別情報修正 Start */
    int i_bat_date;               /* バッチ処理日付                     */
    /* 2021.04.15 HSポイント日別情報修正 End */
    StringDto Program_Name = new StringDto(10);                  /* バージョンなしプログラム名         */
    StringDto Program_Name_Ver = new StringDto(13);              /* バージョンつきプログラム名         */



    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;     /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */

    String C_PRGNAME = "期間限定ポイント失効";  /* APログ用機能名                 */
    /*------------------------------------------*/
    int C_const_Ora_SNAP_OLD = -1555;    /* ORA-1555発生                      */
    int C_const_SNAP_OLD = 55;       /* ORA-1555発生                      */

    int RIYU_CD = 1192;     /* 有効期限切れ期間限定Ｐ失効        */

    int PROCESSING_CNT = 10000; /* 処理経過をAPLOGに出力する際のサイクル*/
    /*-----------bit演算用----------------------*/
    char BASE_BIT = 0x0001;  /* 期間限定Ｐ失効フラグ用             */
    char CHENGE_BIT = 0x1000;  /* 期間限定Ｐ失効フラグ用             */
    char TUJO_BASE_BIT = 0x01;    /* 通常Ｐ失効フラグ用                 */
    char TUJO_CHENGE_BIT = 0x20;    /* 通常Ｐ失効フラグ用                 */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    StringDto gl_sysdate = new StringDto(9);                     /* システム日付                       */
    StringDto gl_systime = new StringDto(7);                     /* システム時刻                       */
    int gl_sys_year;         /* システム年                         */
    StringDto gl_sys_y = new StringDto(4);                       /* システム年下1桁                    */
    StringDto gl_sys_kbn = new StringDto(4);                     /* 偶数年(０)/奇数年(１)区分          */
    StringDto gl_sys_month = new StringDto(7);                   /* システム月（全角）                 */
    StringDto gl_bat_date = new StringDto(9);                    /* バッチ処理日                       */
    int gl_bat_year;         /* バッチ処理年                       */
    int gl_bat_month;        /* バッチ処理月                       */
    StringDto gl_shikko_month = new StringDto(7);                /* 失効対象月（全角）                 */
    int gl_i_shikko_month;   /* 失効対象月                         */
    int gl_base_date;              /* 基準更新日                         */

    int sql_flg;             /* ORA-1555発生フラグ                 */

    /* 2022/11/29 MCCM初版 ADD START */
    int gl_sys_nendo;        /* システム日付の年度                 */
    int gl_i_sys_month;      /* システム日付の月                   */
    StringDto gl_sys_nendo_y = new StringDto(4);                 /* システム日付の年度下1桁(全角)      */
    /* 2022/11/29 MCCM初版 ADD END */

    /* 2022/12/01 MCCM初版 ADD START */
    int gl_kikan_meisai_su;      /* 期間限定Ｐ明細数               */
    int gl_kikan_data_no;        /* 期間限定Ｐ登録データ番号       */
    /* 2022/12/01 MCCM初版 ADD END */


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
        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        /*------------------------------------------------------------*/
        /*--------------------------------------*/
        /*  ローカル変数定義                    */
        /*--------------------------------------*/
        int rtn_cd;                     /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数結果ステータス                 */
        IntegerDto hs_ins_cnt = new IntegerDto();            /* HSポイント日別情報登録件数         */
        IntegerDto ts_upd_cnt = new IntegerDto();            /* TS利用可能ポイント情報更新件数     */
        IntegerDto shik_point_total = new IntegerDto();      /* 失効済み期間限定P合計              */
        StringDto buff = new StringDto(256);                  /* バッファ                           */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        hs_ins_cnt.arr = 0;
        ts_upd_cnt.arr = 0;
        shik_point_total.arr = 0;
        sql_flg = DEF_OFF;
        /*--------------------------------------*/
        /*  プログラム名取得処理                */
        /*--------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        /* バージョンなしプログラム名 */
        strcpy(Program_Name, Cg_Program_Name);
        /* バージョンなしプログラム名 */
        strcpy(Program_Name_Ver, Cg_Program_Name_Ver);

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
        C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_SD);
        /*------------------------------------------------------------*/

        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status.arr);
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
        rtn_cd = C_GetBatDate( 0, gl_bat_date, rtn_status);
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }
        /*------------------------------------------*/
        /*  システム日付取得処理                    */
        /*------------------------------------------*/
        rtn_cd = C_GetSysDateTime(gl_sysdate, gl_systime);
        if ( rtn_cd != C_const_OK){
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** システム日付取得NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetSysDateTime",
                    rtn_cd, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*--------------------------------------*/
        /*  主処理                              */
        /*--------------------------------------*/
        rtn_cd = cmBTkpskB_main(ts_upd_cnt, hs_ins_cnt, shik_point_total);
        sprintf(buff, "失効済み期間限定Ｐ合計値:[%d]", shik_point_total.arr);
        if ( rtn_cd != C_const_OK ) {
            /* 各テーブル更新処理件数を出力 */
            APLOG_WT("107", 0, null,
                    "TS利用可能ポイント情報（期間限定Ｐ失効）",
                    ts_upd_cnt,0,0,0,0);
            APLOG_WT("107", 0, null, "HSポイント日別情報（期間限定Ｐ失効）",
                    hs_ins_cnt,0,0,0,0);
            APLOG_WT("100", 0, null, buff, 0,0,0,0,0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** cmBTkpskB_main NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("912", 0, null, "cmBTkpskB_main処理に失敗しました",
                    0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            sqlca.rollback();     /* ロールバック               */
            return exit( C_const_APNG );

        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /* 各テーブル更新処理件数を出力 */
        APLOG_WT("107", 0, null,
                "TS利用可能ポイント情報（期間限定Ｐポイント失効）",
                ts_upd_cnt,0,0,0,0);
        APLOG_WT("107", 0, null, "HSポイント日別情報（期間限定Ｐ失効）",
                hs_ins_cnt,0,0,0,0);
        APLOG_WT("100", 0, null, buff, 0,0,0,0,0);

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*------------------------------------------------------------*/
        C_DbgEnd("*** main処理 ***", 0, 0, 0);
        /*------------------------------------------------------------*/
        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理          */

        /*  コミット解放処理 */
        sqlca.commit();

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     有効期限切れの期間限定を失効する。                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_main(IntegerDto ts_upd_cnt, IntegerDto hs_ins_cnt,
                               IntegerDto shik_point_total) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_main処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                      /* 戻り値                 */

        StringDto month01 = new StringDto(7);                /* バッチ処理月（全角）                */
        StringDto month02 = new StringDto(7);                /* バッチ処理月の1ヶ月後（全角）       */
        StringDto month03 = new StringDto(7);                /* バッチ処理月の2ヶ月後（全角）       */
        StringDto month04 = new StringDto(7);                /* バッチ処理月の3ヶ月後（全角）       */

        /* 2021.04.15 HSポイント情報設定値修正 Start */
        StringDto month05 = new StringDto(7);                /* バッチ処理月の4ヶ月後（全角）       */
        /* 2021.04.15 HSポイント情報設定値修正 End */

        StringDto next_year_cd = new StringDto(4);           /* 翌年度コード（全角）                */
        StringDto this_year_cd = new StringDto(4);           /* 当年度コード（全角）                */
        StringDto ex_year_cd = new StringDto(4);             /* 前年度コード（全角）                */
        int next_year = 0;      /* 翌年度                              */
        int this_year = 0;      /* 当年度                              */
        int ex_year = 0;        /* 当年度                              */

        /* 失効予定情報を取得 */
        rtn_cd = cmBTkpskB_getInfo();
        if (rtn_cd != C_const_OK){
            return C_const_NG;
        }

        /* 4か月分の月（全角）を取得 */
        cmBTkpskB_getMonth(gl_bat_month, month01);
        cmBTkpskB_getMonth(gl_bat_month+1, month02);
        cmBTkpskB_getMonth(gl_bat_month+2, month03);
        cmBTkpskB_getMonth(gl_bat_month+3, month04);

        /* 2021.04.15 HSポイント情報設定値修正 Start */
        cmBTkpskB_getMonth(gl_bat_month+4, month05);
        /* 2021.04.15 HSポイント情報設定値修正 End */

        /* 当年度、前年度を取得 */
        int [] yearArray = cmBTkpskB_getYear(next_year, this_year, ex_year);
        next_year = yearArray[0];
        this_year = yearArray[1];
        ex_year = yearArray[2];

        cmBTkpskB_getYearCd(next_year, next_year_cd); /* 翌年度コード取得 */
        cmBTkpskB_getYearCd(this_year, this_year_cd); /* 当年度コード取得 */
        cmBTkpskB_getYearCd(ex_year, ex_year_cd);     /* 前年度コード取得 */

        /* MS顧客制度、MS家族制度、TSポイント年別情報検索用日付編集 */
        cmBTkpskB_setNowInf();
        if (rtn_cd != C_const_OK){
            return C_const_NG;
        }

        /* 期間限定P失効処理 */
        while(true){
            rtn_cd = cmBTkpskB_shikko(month01, month02, month03, month04, month05,
                    next_year_cd, this_year_cd, ex_year_cd, next_year,
                    this_year,ex_year,hs_ins_cnt, ts_upd_cnt, shik_point_total);
            if( rtn_cd == C_const_SNAP_OLD){
                continue;
            }else {
                if(rtn_cd == C_const_OK){
                    break;
                }else{
                    APLOG_WT("912", 0, null, "期間限定Ｐ失効処理に失敗しました",
                            0, 0, 0, 0, 0);
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTkpskiB_main処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }

            }
        }
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskiB_main処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了 */
        /*--- main Bottom ------------------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getInfo                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_getInfo( )                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     失効予定の期間限定Ｐ数、顧客数を取得する。                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_getInfo() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getInfo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_buff1 = new StringDto(2);                        /* 編集バッファ               */
        StringDto wk_buff2 = new StringDto(1026);                     /* MSG用バッファ              */
        StringDto wk_base_date = new StringDto(9);                    /* 基準更新日                 */
        StringDto wk_month = new StringDto(3);                        /* バッチ処理日の4か月前の月  */
        StringDto wk_yyyy = new StringDto(5);                         /* 年                         */
        IntegerDto wk_year = new IntegerDto();                 /* 年                         */
        int rtn_cd;                  /* 戻り値                     */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        memset(wk_buff1, 0x00, sizeof(wk_buff1));
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        memset(gl_shikko_month, 0x00, sizeof(gl_shikko_month));
        memset(wk_yyyy, 0x00, sizeof(wk_yyyy));
        memset(wk_month, 0x00, sizeof(wk_month));
        memset(wk_base_date, 0x00, sizeof(wk_base_date));
        h_shikko_yotei_point.arr = 0;
        h_shikko_yotei_kokyaku.arr = 0;
        wk_year.arr=0;

        /* バッチ処理月月の取得 */
        memcpy(wk_buff1, gl_bat_date.arr.substring(4), 2);
        gl_bat_month = atoi(wk_buff1);

        /* バッチ処理年取得 */
        memcpy(wk_yyyy, gl_bat_date.arr, 4);
        gl_bat_year = atoi(wk_yyyy);

        wk_year.arr = gl_bat_year;


        /* 失効処理月取得 */
        if (gl_bat_month == 1){
            gl_i_shikko_month = 12;
        }else{
            gl_i_shikko_month = gl_bat_month -1;
        }
        sprintf( wk_buff1, "%02d", gl_i_shikko_month);

        /* 失効対象月を全角に変換   */
        rtn_cd = C_ConvHalf2Full(wk_buff1, gl_shikko_month);
        if (rtn_cd != C_const_OK){
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_getInfo *** %s\n", "失効対象月全角変換エラー");
            C_DbgEnd("cmBTkpskiB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* 基準更新日設定   */
        cmBTkpskB_4monthAgo(wk_month, wk_year);
        sprintf( wk_base_date, "%d%s01", wk_year.arr, wk_month.arr);
        gl_base_date = atoi(wk_base_date);

        /*------------------------------------------------------------*/
        // TODO: wk_yyyy[4]是bug？？？
        /*sprintf(wk_buff2, "失効対象月:[%s]、基準更新日:[%d] %d %c %d %s "
                , gl_shikko_month, gl_base_date, wk_year.arr, wk_yyyy.arr.charAt(4), gl_bat_year, gl_bat_date.arr);*/
        sprintf(wk_buff2, "失効対象月:[%s]、基準更新日:[%d] %d %s %d %s "
                , gl_shikko_month, gl_base_date, wk_year.arr, "", gl_bat_year, gl_bat_date.arr);
        C_DbgMsg("*** cmBTkpskiB_getInfo *** %s\n", wk_buff2);
        /*------------------------------------------------------------*/

        /* ＨＯＳＴ変数にセット */
         sprintf(h_str_sql,
                 "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ " +
                 "COUNT(*) AS 失効予定顧客数, " +
                 "SUM (利用可能期間限定Ｐ%s) AS 失効予定期間限定P合計 " +
                 "FROM cmuser.TS利用可能ポイント情報 " +
                 "WHERE 最終更新日 >= %d " +
                 "AND 利用可能期間限定Ｐ%s <> 0 " +
                 "AND 顧客番号 <> 9999999999999 ",
                 gl_shikko_month.arr, gl_base_date, gl_shikko_month.arr);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskiB_getInfo *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        // EXEC SQL PREPARE sql_stat1 from :h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL01");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SQL01 CURSOR FOR sql_stat1;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL01;
        sqlca.query();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_getInfo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }




        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskiB_getInfo *** TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(wk_buff2, "[%s]月の期間限定Ｐ失効", gl_shikko_month);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TS利用可能ポイント情報情報", wk_buff2, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_getInfo *** %s\n", "カーソルオープンエラー");
            C_DbgEnd("cmBTkpskiB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }

        /* カーソルフェッチ */
        // EXEC SQL FETCH CUR_SQL01
        // INTO :h_shikko_yotei_kokyaku,
        // :h_shikko_yotei_point;
        sqlca.fetch();
        /* 失効予定顧客数            */
        /* 失効予定期間限定P         */
        sqlca.recData(h_shikko_yotei_kokyaku, h_shikko_yotei_point);

        /* 対象顧客がいない場合、処理を終了する */
        if (h_shikko_yotei_kokyaku.intVal() == 0 ){
            /* 失効予定情報をAPログに出力 */
            sprintf(wk_buff2, "失効対象月:[%s]の対象顧客なし", gl_shikko_month);
            APLOG_WT("100", 0, null, wk_buff2, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getInfo *** 失効対象月：" +
                    "[%s]の期間限定Ｐが残っている顧客がいません。\n",gl_shikko_month );
            C_DbgEnd("cmBTkpskiB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            /* カーソルクローズ */
            // EXEC SQL CLOSE CUR_SQL01;
            sqlcaManager.close("CUR_SQL01");

            return (C_const_OK);
        }

        /* データ無し以外エラーの場合、処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(wk_buff2, "[%s]月の期間限定Ｐ失効", gl_shikko_month);
            APLOG_WT("904", 0, null, "FETCH(CUR_SQL01)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    wk_buff2, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_getInfo *** %s\n", "フェッチエラー");
            C_DbgEnd("cmBTkpskiB_getInfo処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
            // EXEC SQL CLOSE CUR_SQL01;
           sqlcaManager.close("CUR_SQL01");
            return C_const_NG;
        }


        /* 失効予定情報をAPログに出力 */
        sprintf(wk_buff2,
                "失効対象月:[%s]、失効予定顧客数:[%10.0f]、失効予定期間限Ｐ合計:[%10.0f]"
                , gl_shikko_month.toString(), h_shikko_yotei_kokyaku.floatVal(), h_shikko_yotei_point.floatVal());
        APLOG_WT("100", 0, null, wk_buff2, 0, 0, 0, 0, 0);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getInfo *** %s\n", wk_buff2);
        C_DbgEnd("cmBTkpskiB_getInfo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_SQL01;
        sqlcaManager.close("CUR_SQL01");
        return (C_const_OK);
        /*--- cmBTkpskiB_getInfo処理 -------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_4monthAgo                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_4monthAgo(StringDto month_ago,                      */
    /*                                      IntegerDto wk_year)          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     バッチ処理日から４か月前の月を取得する。                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     *month_ago  バッチ処理月の4か月前の月                         */
    /*     IntegerDto wk_year  年                                        */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTkpskB_4monthAgo(StringDto month_ago, IntegerDto wk_year)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_4monthAgo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                            /* バッファ               */

        /* バッチ処理月から４か月前の月を計算する */
        if (gl_bat_month <= 4){
        wk_year.arr = wk_year.arr - 1; /* 基準更新日の年を前年に設定 */
            sprintf(month_ago, "%02d", gl_bat_month+8);
        }else{
            sprintf(month_ago, "%02d", gl_bat_month-4);
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "バッチ処理月の4か月前の月：[%s]", month_ago);
        C_DbgMsg("*** cmBTkpskB_4monthAgo *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_4monthAgo処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTkpskiB_4monthAgo処理 -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTkpskB_getMonth( int i_month,           */
    /*                                            StringDto month)                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の全角を取得する。                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*     char    *month       月（全角）                                        */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void   cmBTkpskB_getMonth( int i_month, StringDto month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getMonth処理");
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
        C_DbgMsg("*** cmBTkpskB_getMonth *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTkpskiB_cmBTkpskB_getMonth処理 --------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getYear                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTkpskB_getYear( IntegerDto next_yyyy,                 */
    /*       IntegerDto this_yyyy, IntegerDto ex_yyyy)          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     年度を考慮した、翌年度、当年度、前年度を取得する。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IntegerDto next_yyyy     翌年度                                */
    /*    IntegerDto this_yyyy     当年度                                */
    /*    IntegerDto ex_yyyy       前年度                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int []  cmBTkpskB_getYear(int next_yyyy, int this_yyyy, int ex_yyyy) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 月の当年度算出 */
        /* 2022/11/30 MCCM初版 MOD START */
        /* if ( gl_bat_month >= 1 && gl_bat_month <=4){ */
        if ( gl_bat_month <= 3 ){
            /* 2022/11/30 MCCM初版 MOD START */
        this_yyyy = gl_bat_year-1; /* 年度切替前 */
        }else{
        this_yyyy = gl_bat_year; /* 年度切替後 */
        }

        /* 前年度算出 */
        ex_yyyy = this_yyyy-1;
        /* 翌年度算出 */
        next_yyyy = this_yyyy+1;

        /*------------------------------------------------------------*/
        sprintf(buff, "翌年度:[%d]、当年度:[%d]、前年度:[%d]",
                next_yyyy,this_yyyy, ex_yyyy);
        C_DbgMsg("*** cmBTkpskB_getYear *** %s\n", buff);
        C_DbgEnd("cmBTkpskiB_getYear処理", 0, 0, 0);
        return new int[]{next_yyyy, this_yyyy, ex_yyyy};
        /*------------------------------------------------------------*/

        /*--- cmBTkpskiB_getYear処理 -------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getYearCd                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*     voidt  cmBTkpskB_getYearCd( int year, StringDto year_cd)     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     引数の年度コードを取得する。                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     int year           年度                                  */
    /*    char              *year_cd        年度コード                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void  cmBTkpskB_getYearCd( int year, StringDto year_cd)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_setYearCd処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 年度コードを算出 */
        switch( year % 5){
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
        C_DbgMsg("*** cmBTkpskB_getYearCd *** %s\n", buff);
        C_DbgEnd("cmBTkpskiB_getYearCd処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTkpskiB_getYearcd処理 -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_setNowInf                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_setNowInf( )                              */
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
    @Override
    public int  cmBTkpskB_setNowInf()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_NowInf処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto wk_buff1 = new StringDto(3);                        /* 編集バッファ               */
        StringDto wk_buff2 = new StringDto(1026);                     /* MSG用バッファ              */
        StringDto wk_y = new StringDto(2);                            /* 年下一桁                   */
        StringDto wk_yyyy = new StringDto(5);                         /* 年                         */
         int wk_year;                 /* 年                         */
        /* 2022/11/30 MCCM初版 DEL START */
        /*  int wk_date; */                      /* システム日                 */
        /* 2022/11/30 MCCM初版 DEL START */
         int rtn_cd;                  /* 戻り値                     */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        memset( wk_buff1, 0x00, sizeof(wk_buff1));
        memset( gl_sys_month, 0x00, sizeof(gl_sys_month));
        memset( wk_y, 0x00, sizeof(wk_y));
        memset(gl_sys_kbn,  0x00, sizeof(gl_sys_kbn));

        /* システム月の取得 */
        memcpy(wk_buff1, gl_sysdate.arr.substring(4), 2);

        /* システム月を全角に変換   */
        rtn_cd = C_ConvHalf2Full(wk_buff1, gl_sys_month);
        if (rtn_cd != C_const_OK){
            APLOG_WT("903", 0, null, "C_ConvHalf2Ful(gl_sys_month）",
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_setNowInf *** %s\n", "システム月全角エラー");
            C_DbgEnd("cmBTkpskiB_setNowInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* システム年取得 */
        memcpy(wk_yyyy, gl_sysdate.arr, 4);
        gl_sys_year = atoi(wk_yyyy);

        /* 年下一桁取得 */
        /* 2022/11/30 MCCM初版 MOD START */
        /* memcpy(wk_y, &(gl_sysdate[3]), 1); */
        gl_i_sys_month = atoi(wk_buff1);
        /* システム日付月の当年度算出 */
        if ( gl_i_sys_month <= 3 ){
            wk_year = gl_sys_year-1; /* 年度切替前 */
        }else{
            wk_year = gl_sys_year; /* 年度切替後 */
        }
        sprintf(wk_y,"%d",wk_year%10 );
        /* 2022/11/30 MCCM初版 MOD END */

        rtn_cd = C_ConvHalf2Full(wk_y, gl_sys_y);
        if (rtn_cd != C_const_OK){
            APLOG_WT("903", 0, null, "C_ConvHalf2Full(gl_sys_y)",
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_setNowInf *** %s\n", "年下一桁全角エラー");
            C_DbgEnd("cmBTkpskiB_setNowInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* 偶数年（０）／奇数年（１）区分取得 */
        /* 2022/11/30 MCCM初版 DEL START */
    /* wk_date = atoi(gl_sysdate);
       wk_year = (wk_date / 10000) % 10; */
        /* 2022/11/30 MCCM初版 DEL END */
        if ((wk_year % 2) == 0){ strcpy(gl_sys_kbn, "０");}/*偶数年 */
        else{ strcpy(gl_sys_kbn, "１");}

        /*------------------------------------------------------------*/
        sprintf(wk_buff2,
                "システム月:[%s]、システム年:[%d]、システム年下一桁:[%s]、システム年区分:[%s]", gl_sys_month, gl_sys_year, gl_sys_y, gl_sys_kbn);
        C_DbgMsg("*** cmBTkpskiB_setNowInf *** %s\n", wk_buff2);
        /*------------------------------------------------------------*/

        return (C_const_OK);
        /*--- cmBTkpskiB_setNowInf処理 -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_shikko                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_shikko(StringDto month01, StringDto month02,      */
    /*                      StringDto month03,StringDto month04, StringDto month05,           */
    /*                      StringDto next_yyyy_cd, StringDto this_yyyy_cd,               */
    /*                      StringDto ex_yyyy_cd, int next_yyyy,        */
    /*                       int this_yyyy,                         */
    /*                       int ex_yyyy, IntegerDto hs_ins_cnt, */
    /*                      IntegerDto ts_upd_cnt,                             */
    /*                      IntegerDto shik_point_total  )                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     期間限定Pを失効する。                                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      StringDto month01       期間限定Ｐ有効月１                                */
    /*      StringDto month02       期間限定Ｐ有効月２                                */
    /*      StringDto month03       期間限定Ｐ有効月３                                */
    /*      StringDto month04       期間限定Ｐ有効月４                                */
    /*      StringDto month05       期間限定Ｐ有効月５                                */
    /*      StringDto next_yyyy_cd  次年度コード                                      */
    /*      StringDto this_yyyy_cd  当年度コード                                      */
    /*      StringDto ex_yyyy_cd    前年度コード                                      */
    /*       int next_yyyy 翌年度                                   */
    /*       int this_yyyy 当年度                                   */
    /*       int ex_yyyy   前年度                                   */
    /*      IntegerDto hs_ins_cnt     HSポイント日別情報登録件数               */
    /*      IntegerDto ts_upd_cnt     TS利用可能ポイント情報更新件数           */
    /*      IntegerDto shik_point_total  失効済み期間限定Ｐ                    */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             55   :  ORA-1555発生                                           */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_shikko(StringDto month01, StringDto month02, StringDto month03,
                                 StringDto month04, StringDto month05, StringDto next_yyyy_cd,
                                 StringDto this_yyyy_cd, StringDto ex_yyyy_cd,
                                  int next_yyyy,
                                  int this_yyyy,
                                  int ex_yyyy,
                                 IntegerDto hs_ins_cnt, IntegerDto ts_upd_cnt,
                                 IntegerDto shik_point_total)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_shikko処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_date;                           /* バッチ処理日           */
        StringDto wk_kokyaku_no = new StringDto(16);                         /* 顧客番号               */
        int rtn_cd;                               /* 戻り値                 */
        IntegerDto rtn_status = new IntegerDto();                                 /* 関数結果ステータス     */
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /* 失効対象顧客抽出 */

        /* ＨＯＳＴ変数にセット */
        if (sql_flg == DEF_OFF){
            sprintf(h_str_sql,
                    "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ " +
                    "顧客番号, " +
                    "期間限定Ｐ失効フラグ " +
                    "FROM cmuser.TS利用可能ポイント情報 " +
                    "WHERE 最終更新日 >= %d " +
                    "AND 利用可能期間限定Ｐ%s <> 0 " +
                    "AND 顧客番号 <>9999999999999 " ,
                    gl_base_date, gl_shikko_month);

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_shikko *** 抽出SQL [%s]\n", h_str_sql);
            /*------------------------------------------------------------*/
        }else{
            wk_date = atoi(gl_bat_date);
            sprintf(h_str_sql,
                    "SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ " +
                    "顧客番号, " +
                    "期間限定Ｐ失効フラグ " +
                    "FROM cmuser.TS利用可能ポイント情報 " +
                    "WHERE 最終更新日 >= %d " +
                    "AND 利用可能期間限定Ｐ%s <> 0 " +
                    "AND NOT(最終更新プログラムＩＤ = '%s' AND 最終更新日 = %d) " +
                    "AND 顧客番号 <>9999999999999 ",
                    gl_base_date, gl_shikko_month, Program_Name, wk_date);

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_shikko *** 抽出SQL [%s]\n", h_str_sql);
            /*------------------------------------------------------------*/
        }

        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL02");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_shikko *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTkpskB_shikko処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        //EXEC SQL DECLARE CUR_SQL02 CURSOR FOR sql_stat2;

        /* カーソルオープン */
        //EXEC SQL OPEN CUR_SQL02;
        sqlca.query();

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskiB_shikko *** TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        while(true){
            memset(h_vr_kokyaku_no.arr, 0x00, sizeof(h_vr_kokyaku_no.toString()));
            h_vr_kokyaku_no.len = 0;
            h_shikko_flg.arr = 0;

            /* カーソルフェッチ */
            /* 顧客番号             */
            /* 期間限定Ｐ失効フラグ  */
            sqlca.fetch();
            sqlca.recData(h_vr_kokyaku_no, h_shikko_flg);



            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND){
                /* カーソルクローズ */
                // EXEC SQL CLOSE CUR_SQL02;
                sqlcaManager.close("CUR_SQL02");
                break; /* ループ抜ける */
            }
            /* スナップショットが古すぎる場合 */
        //     else if(sqlca.sqlcode == C_const_Ora_SNAP_OLD){
        //         /* スナップショットが古すぎる場合 */
        //         sprintf(buff,
        //                 "DBエラー(FETCH) STATUS=%d(TBL:TS利用可能ポイント情報 KEY:[%s]" +
        //                 "月の期間限定Ｐ失効", C_const_Ora_SNAP_OLD, gl_shikko_month);
        //         APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
        //         /*------------------------------------------------------------*/
        //         C_DbgMsg("*** cmBTkpskiB_shikko *** %s\n", "ORA-1555発生");
        //         /*------------------------------------------------------------*/
        //         /* カーソルクローズ */
        //         // EXEC SQL CLOSE CUR_SQL02;
        //         sqlcaManager.close("CUR_SQL02");
        //         /* ORA-1555発生フラグON */
        //         sql_flg = DEF_ON;
        //         return C_const_SNAP_OLD;
        //     }
            /* スナップショットが古すぎる、データ無し以外エラーの場合 */
            else if(sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    sqlca.sqlcode != C_const_Ora_SNAP_OLD) {
                sprintf(buff, "[%s]月の期間限定Ｐ失効", gl_shikko_month);
                APLOG_WT("904", 0, null, "FETCH(CUR_SQL02)",
                        sqlca.sqlcode, "TS利用可能ポイント情報",
                        buff, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CUR_SQL02;
                sqlcaManager.close("CUR_SQL02");
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTkpskiB_shikko *** %s\n", "フェッチエラー");
                C_DbgEnd("cmBTkpskB_shikko処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG; /* 処理を異常終了する */

            }

            /* 顧客番号（数値）設定 */
            h_kokyaku_no = atol(h_vr_kokyaku_no);

            sprintf(buff, "顧客番号:[%d]、期間限定Ｐ失効フラグ:[%d]",
                    h_kokyaku_no, h_shikko_flg);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskiB_shikko *** %s\n", buff);
            /*------------------------------------------------------------*/


            /* 失効対象月のポイントが失効されているかチェック */
            rtn_cd = cmBTkpskB_checkBit(gl_i_shikko_month);
            if(rtn_cd == DEF_OFF){

                /*顧客情報ロック */
                sprintf(wk_kokyaku_no, "%s", h_kokyaku_no);
                rtn_cd = C_KdataLock(wk_kokyaku_no, "1", rtn_status);
                if (rtn_cd == C_const_NG){
                    APLOG_WT( "903", 0, null, "C_KdataLock(1)",
                            rtn_cd, rtn_status, 0, 0, 0);
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTkpskiB_shikko *** ロックエラー顧客=[%s]\n",
                            wk_kokyaku_no);
                    C_DbgEnd("cmBTkpskB_shikko処理", C_const_NG, 0, 0);
                    /*------------------------------------------------------------*/
                    // EXEC SQL CLOSE CUR_SQL02; /* カーソルクローズ       */
                    sqlcaManager.close("CUR_SQL02");
                    return C_const_NG;
                }

                /* 現在の顧客情報を取得する */
                rtn_cd =cmBTkpskB_getRiyoKanoPoint(month01, month02, month03,
                        month04, month05, next_yyyy_cd, this_yyyy_cd, ex_yyyy_cd);
                if (rtn_cd != C_const_OK){
                    // EXEC SQL CLOSE CUR_SQL02;/* カーソルクローズ */
                    sqlcaManager.close("CUR_SQL02");
                    APLOG_WT("912", 0, null,
                            "TS利用可能ポイント情報取得エラー",
                            0, 0, 0, 0, 0);
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTkpskiB_shikko *** %s\n",
                            "TS利用可能ポイント情報取得エラー");
                    C_DbgEnd("cmBTkpskB_shikko処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
                /* 失効対象月のポイントが失効されているかチェック */
                rtn_cd = cmBTkpskB_checkBit(gl_i_shikko_month);
                if(rtn_cd == DEF_OFF){

                    /* 2021.04.15 HSポイント情報設定値修正 Start */
                    /* 前年度の失効状態を確認 */
                    /*cmBTkpskB_check_tujo_point(ex_yyyy);*/
                    /* 2021.04.15 HSポイント情報設定値修正 End */

                    /* 2021.05.19 更新前利用可能ポイントチェック追加 Start */
                    /* 各年度の失効状態を確認 */
                    cmBTkpskB_check_tujo_point(ex_yyyy, this_yyyy, next_yyyy);

                    /* 各月の失効状態を確認 */
                    cmBTkpskB_check_kikan_point();

                    /* 2021.05.19 更新前利用可能ポイントチェック追加 End */

                    /* ポイント失効処理 */
                    rtn_cd = cmBTkpskB_pointLost(this_yyyy);
                    if (rtn_cd != C_const_OK){
                        // EXEC SQL CLOSE CUR_SQL02;/* カーソルクローズ */
                        sqlcaManager.close("CUR_SQL02");
                        /*-----------------------------------------------------*/
                        C_DbgEnd("cmBTkpskB_shikko処理", C_const_NG, 0, 0);
                        /*-----------------------------------------------------*/
                        return C_const_NG;
                    }

                    /*HSポイント日別情報登録件数アップ */
                hs_ins_cnt.arr = hs_ins_cnt.arr + 1;
                    /* TS利用可能ポイント情報更新件数アップ */
                ts_upd_cnt.arr = ts_upd_cnt.arr + 1;
                    /* 失効済み期間限定Ｐ合計値加算 */
                shik_point_total.arr = shik_point_total.arr + h_kikan_p.intVal();

                    /* 処理状況をＡＰログに出力する */
                    if(hs_ins_cnt.arr % PROCESSING_CNT == 0){
                        sprintf(buff, "HSポイント日別情報　更新件数：[%d]、" +
                                "失効済み期間限定Ｐ合計値:[%d]",
                                hs_ins_cnt.arr, shik_point_total.arr);
                        APLOG_WT("100", 0, null, buff, 0,0,0,0,0);
                    }

                    sqlca.commit(); /* コミット  */
                }else{
                    sqlca.rollback();     /* ロールバック           */
                }
            }
        }

        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_SQL02;
        sqlcaManager.close("CUR_SQL02");

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_shikko処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;
        /*--- cmBTkpskiB_shikko処理 ------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_checkBit                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_checkBit( int month)        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象月の期間限定に対応するbitがOFF(0)か確認する。                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*        int month    対象月                                   */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_checkBit( int month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_checkBit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
         int shikko_flg;                  /* 失効確認用フラグ       */
         int check_flg;                   /* bit演算用フラグ        */
         int bit_kekka;                   /* bit計算結果            */
        StringDto buff =new StringDto(160);                   /* バッファ               */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        check_flg = BASE_BIT;

        /* 対象月を算出する */
        month = (month % 12);
        if (month == 0){
            month = 12;
        }

        /* 失効確認フラグ設定*/
        check_flg =( check_flg << (12 - month) | CHENGE_BIT);

        bit_kekka = h_shikko_flg.intVal() & check_flg;

        /* 失効フラグの値確認 */
        if ( bit_kekka == check_flg){
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、月のbit：[%#x]、" +
                    "bit結果：[%#x]、失効済み",
                    month, h_shikko_flg, check_flg, bit_kekka);
            /*------------------------------------------------------------*/
        }else{
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、月のbit：[%#x]、" +
                    "bit結果：[%#x]、未失効",
                    month, h_shikko_flg.intVal(), check_flg, bit_kekka);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_checkBit *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_checkBit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTkpskiB_checkBit処理 ------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getRiyoKanoPoint                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  int  cmBTkpskB_getRiyoKanoPoint(StringDto month01, StringDto month02,      */
    /*                      StringDto month03, StringDto month04,  StringDto month04,         */
    /*                      StringDto next_year_cd,StringDto this_year_cd,                */
    /*                      StringDto ex_year_cd)                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報取得                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_getRiyoKanoPoint(StringDto month01, StringDto month02,
                                           StringDto month03, StringDto month04,  StringDto month05, StringDto next_year_cd,
                                           StringDto this_year_cd, StringDto ex_year_cd)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getRiyokanoPoint処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(2048);                  /* バッファ               */


        /* 初期化 */
        h_yokunen_p.arr = 0;
        h_tonen_p.arr = 0;
        h_zennen_p.arr = 0;
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        h_tujo_shikko_flg.arr = 0;
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        h_nyukai_kigyo_cd = 0;
        h_nyukai_tenpo = 0;
        h_hakken_kigyo_cd = 0;
        h_hakken_tenpo = 0;
        h_kikan_p.arr = 0;
        h_kikan_p1.arr = 0;
        h_kikan_p2.arr = 0;
        h_kikan_p3.arr = 0;
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        h_kikan_p4.arr = 0;
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        h_shikko_flg.arr = 0;

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        /* ＨＯＳＴ変数にセット */
        sprintf(h_str_sql,
                /* 2021.08.02 バグ改修 Start */
                //"SELECT /*+ INDEX (TS利用可能ポイント情報 IXTSPRYO00) */ "
                /* 2021.08.02 バグ改修 End */
                "SELECT " +
                "利用可能通常Ｐ%s," +
                "利用可能通常Ｐ%s," +
                "利用可能通常Ｐ%s," +
                "通常Ｐ失効フラグ," +
                /* 2022/12/01 MCCM初版 DEL START */
                /*              "入会企業コード," */
                /*              "入会店舗," */
                /*              "発券企業コード," */
                /*              "発券店舗," */
                /* 2022/12/01 MCCM初版 DEL DEL */
                "利用可能期間限定Ｐ%s," +
                "利用可能期間限定Ｐ%s," +
                "利用可能期間限定Ｐ%s," +
                "利用可能期間限定Ｐ%s," +
                "利用可能期間限定Ｐ%s," +
                /* 2022/11/30 MCCM初版 MOD START */
                /*              "期間限定Ｐ失効フラグ " */
                "期間限定Ｐ失効フラグ, " +
                "利用可能期間限定購買Ｐ%s,  " +
                "利用可能期間限定非購買Ｐ%s,  " +
                "利用可能期間限定その他Ｐ%s,  " +
                "入会会社コードＭＣＣ, " +
                "入会店舗ＭＣＣ " +
                /* 2022/11/30 MCCM初版 MOD END */
                "FROM cmuser.TS利用可能ポイント情報 " +
                "WHERE   顧客番号        = ? " +
                "AND     最終更新日 >= %d " +
                "AND     利用可能期間限定Ｐ%s <> 0 " ,
                next_year_cd, this_year_cd, ex_year_cd,gl_shikko_month,
                month01, month02, month03, month04,
                /* 2022/11/30 MCCM初版 ADD START */
                /*              month01, month02, month03, month04,gl_base_date, */
                gl_shikko_month, gl_shikko_month, gl_shikko_month, gl_base_date,
                /* 2022/11/30 MCCM初版 ADD END */
                gl_shikko_month);
        /* 2021.04.15 HSポイント日別情報設定値修正 End */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;

        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL03");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTkpskB_getRiyokanoPoint処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        //EXEC SQL DECLARE CUR_SQL03 CURSOR FOR sql_stat3;

        /* カーソルオープン */
        //EXEC SQL OPEN CUR_SQL03 USING :h_kokyaku_no;
        sqlca.query(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** " +
                "TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(buff, "[%s]月の期間限定Ｐ失効、顧客番号=[%d]",
                    gl_shikko_month, h_kokyaku_no);
            APLOG_WT("904", 0, null, "OPEN CURSOR(CUR_SQL03)",
                    sqlca.sqlcode, "TS利用可能ポイント情報情報",
                    buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** %s\n","カーソルエラー");
            C_DbgEnd("cmBTkpskB_getRiyokanoPoint処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        /* カーソルフェッチ */
        /* 利用可能通常Ｐ翌年度               */
        /* 利用可能通常Ｐ当年度               */
        /* 利用可能通常Ｐ前年度               */
        /* 通常Ｐ失効フラグ                   */
        /* 2022/12/01 MCCM初版 DEL START */
        /*        :h_nyukai_kigyo_cd, */ /* 入会企業コード                     */
        /*        :h_nyukai_tenpo,    */ /* 入会店舗                           */
        /*        :h_hakken_kigyo_cd, */ /* 発券企業コード                     */
        /*        :h_hakken_tenpo,    */ /* 発券店舗                           */
        /* 2022/12/01 MCCM初版 DEL END */
        /* 期間限定Ｐ（失効月）               */
        /* 期間限定Ｐ＋1か月                  */
        /* 期間限定Ｐ＋2か月                  */
        /* 期間限定Ｐ＋3か月                  */
        /* 期間限定Ｐ＋4か月                  */
        /* 2022/11/30 MCCM初版 MOD START */
        /*        :h_shikko_flg; */     /* 期間限定Ｐ失効フラグ               */
        /* 期間限定Ｐ失効フラグ               */
        /* 利用可能期間限定購買Ｐ前月     */
        /* 利用可能期間限定非購買Ｐ前月   */
        /* 利用可能期間限定その他Ｐ前月   */
        /* 2022/11/30 MCCM初版 MOD END */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        sqlca.fetch();
        sqlca.recData(h_yokunen_p, h_tonen_p, h_zennen_p, h_tujo_shikko_flg, h_kikan_p, h_kikan_p1, h_kikan_p2,
                h_kikan_p3, h_kikan_p4, h_shikko_flg, h_kikan_kobai_before_month, h_kikan_hikobai_before_month,
                h_kikan_sonota_before_month, h_nyukai_kigyo_cd_mcc, h_nyukai_tenpo_mcc);


        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){

            sprintf(buff, "[%s]月の期間限定Ｐ失効、顧客番号=[%d]",
                    gl_shikko_month, h_kokyaku_no);
            APLOG_WT("904", 0, null, "FETCH(CUR_SQL03)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    buff, 0, 0);
            /* カーソルクローズ */
            // EXEC SQL CLOSE CUR_SQL03;
            sqlcaManager.close("CUR_SQL03");
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** %s\n","フェッチエラー");
            C_DbgEnd("cmBTkpskB_getRiyokanoPoint処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }

        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_SQL03;
        sqlcaManager.close("CUR_SQL03");

        /* 2022/12/01 MCCM初版 ADD START */
        gl_kikan_meisai_su = 0;
        if (h_kikan_kobai_before_month.intVal() != 0 ){     /* 期間限定購買Ｐ計 <> 0 の場合、期間限定Ｐ明細数 +1   */
            gl_kikan_meisai_su++;
        }
        if (h_kikan_hikobai_before_month.intVal() != 0 ){   /* 期間限定非購買Ｐ計 <> 0 の場合、期間限定Ｐ明細数 +1 */
            gl_kikan_meisai_su++;
        }
        if (h_kikan_sonota_before_month.intVal() != 0 ){    /* 期間限定その他Ｐ計 <> 0 の場合、期間限定Ｐ明細数 +1 */
            gl_kikan_meisai_su++;
        }
        /* 2022/12/01 MCCM初版 ADD END */

        sprintf(buff,
                /* 2022/12/01 MCCM初版 MOD START */
/*      "利用可能通常Ｐ%s:[%10.0lf],利用可能通常Ｐ%s:[%10.0lf],利用可能通常Ｐ%s:[%10.0lf],通常Ｐ失効フラグ:[%d],入会企業コード:[%d],入会店舗:[%d],発券企業コード:[%d],発券店舗:[%d],利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],利用可能期間限定Ｐ%s:[%10.0lf],期間限定Ｐ失効フラグ:[%d]",
        next_year_cd, h_yokunen_p, this_year_cd, h_tonen_p, ex_year_cd,
        h_zennen_p, h_tujo_shikko_flg, h_nyukai_kigyo_cd,
        h_nyukai_tenpo, h_hakken_kigyo_cd, h_hakken_tenpo, gl_shikko_month,
        h_kikan_p, month01, h_kikan_p1, month02, h_kikan_p2, month03,
        h_kikan_p3, month04, h_kikan_p4, h_shikko_flg); */
                "利用可能通常Ｐ%s:[%10.0f],利用可能通常Ｐ%s:[%10.0f],利用可能通常Ｐ%s:[%10.0f],通常Ｐ失効フラグ:[%d]," +
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f]," +
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],期間限定Ｐ失効フラグ:[%d]," +
                "利用可能期間限定購買Ｐ%s:[%10.0f], 利用可能期間限定非購買Ｐ%s:[%10.0f], 利用可能期間限定その他Ｐ%s:[%10.0f],  " +
                "入会会社コードＭＣＣ:[%d], 入会店舗ＭＣＣ:[%d]",
                next_year_cd, h_yokunen_p.floatVal(), this_year_cd, h_tonen_p.floatVal(), ex_year_cd, h_zennen_p.floatVal(), h_tujo_shikko_flg,
                gl_shikko_month, h_kikan_p.floatVal(), month01, h_kikan_p1.floatVal(), month02, h_kikan_p2.floatVal(),
                month03, h_kikan_p3.floatVal(), month04, h_kikan_p4.floatVal(), h_shikko_flg,
                gl_shikko_month, h_kikan_kobai_before_month.floatVal(),
                gl_shikko_month, h_kikan_hikobai_before_month.floatVal(),
                gl_shikko_month, h_kikan_sonota_before_month.floatVal(),
                h_nyukai_kigyo_cd_mcc, h_nyukai_tenpo_mcc );
        /* 2022/12/01 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getRiyokanoPoint *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getRiyokanoPoint処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (C_const_OK );
        /*--- cmBTkpskiB_getRiyokanoPoint処理 ----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_pointLost                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_pointLost( int this_yyyy )  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     失効ポイント登録処理。                                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*        int this_yyyy   当年度                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_pointLost( int this_yyyy  )
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_pointLost処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int     rtn_cd;                                 /* 戻り値                 */

        /* MSカード情報取得*/
        cmBTkpskB_getMSCard();

        /* TSポイント年別情報取得 */
        cmBTkpskB_getTSYear();

        /* MS顧客制度報取得 */
        cmBTkpskB_getMSKokyaku();

        if ( h_kazoku_id.intVal() == 0 ) { /* 家族制度未入会 */
            h_kazoku_nenji_rank_cd  = 0;
            h_kazoku_getuji_rank_cd = 0;
        } else                        { /* 家族制度入会済 */
            cmBTkpskB_getMSKazoku();
        }

        /* 2022/11/30 MCCM初版 ADD START */
        /* TSランク情報取得 */
        rtn_cd = cmBTkpskB_getTSRank();
        if ( rtn_cd != C_const_OK ) {
            return C_const_NG;
        }
        /* 2022/11/30 MCCM初版 ADD END */

        /* HSポイント日別情報更新 */
        rtn_cd = cmBTkpskB_insertHS(this_yyyy);
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("912", 0, null,
                    "ポイント日別情報更新処理に失敗しました",
                    0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_pointLost *** %s\n",
                    "HSポイント日別情報更新エラー");
            C_DbgEnd("cmBTkpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }

        /* TS利用可能ポイント情報の登録*/
        rtn_cd = cmBTkpskB_updTS();
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("912", 0, null,
                    "TS利用可能ポイント情報更新処理に失敗しました", 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_pointLost *** %s\n",
                    "TS利用可能ポイント情報更新エラー");
            C_DbgEnd("cmBTkpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }

        /* HSポイント日別内訳情報登録（期間限定ポイント） */
        rtn_cd = cmBTkpskB_kikan_insertHSUchiwake();
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTkpskB_pointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_pointLost処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTkpskiB_pointLost処理 -----------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getMSCard                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_getMScard()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MSカード情報取得。                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*           なし                                                             */
    /*  【戻り値】                                                                */
    /*           なし                                                             */
    /******************************************************************************/
    @Override
    public void  cmBTkpskB_getMSCard()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getMSCard処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    buff = new StringDto(512);                              /* バッファ               */
        long    kaiin_no;                  /* 会員番号               */

        /* 初期化 */
        memset(h_kaiin_no.arr, 0x00, sizeof(h_kaiin_no.toString()));
        h_kaiin_no.len = 0;
        h_kigyo_cd.arr = 0;
        h_kyu_hansya_cd.arr = 0;

        StringDto sql = new StringDto();
        /* 2022/11/30 MCCM初版 MOD START */
        /*  AND     サービス種別 IN (1,2,3)
                        AND     カードステータス IN (0,7,8)
                        ORDER BY
                        CASE    サービス種別
                                    WHEN 1 THEN 1
                                    WHEN 3 THEN 2
                                    WHEN 2 THEN 3
                                    ELSE 0
                                END,
                                    カードステータス ASC,*/
        /* 2022/11/30 MCCM初版 MOD END */
        sql.arr = "SELECT MS.会員番号, MS.企業コード, MS.旧販社コード FROM " +
                "(SELECT サービス種別, 会員番号, 企業コード, 旧販社コード FROM MSカード情報 " +
                "WHERE 顧客番号 = ? AND サービス種別 IN (1,2,3,4,5) " +
                "AND カードステータス IN (0,1,7,8) ORDER BY カードステータス ASC, " +
                "CASE  サービス種別 " +
                "WHEN 1 THEN 1" +
                "WHEN 4 THEN 2" +
                "WHEN 3 THEN 3" +
                "WHEN 2 THEN 4" +
                "ELSE 5" +
                "END ASC, 発行年月日 DESC LIMIT 1) MS " +
                "";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_kokyaku_no);

        /* エラーの場合処理を異常終了する */
        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( buff, "SQL検索失敗。" +
                    "STATUS=[%d](TBL:MSカード情報)顧客番号=[%d]",
                    sqlca.sqlcode, h_kokyaku_no );
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getMScard *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getMSCard処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }

        sqlca.fetch();
        sqlca.recData(h_kaiin_no, h_kigyo_cd, h_kyu_hansya_cd);
        /*------------------------------------------------------------*/
        kaiin_no = atol(h_kaiin_no);
        sprintf(buff, "会員番号:[%d],企業コード:[%d],旧販社コード:[%d]",
                kaiin_no, h_kigyo_cd, h_kyu_hansya_cd);
        C_DbgMsg("*** cmBTkpskB_getMScard *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getMSCard処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return;              /* 処理終了                           */
        /*-----cmBTshikB_getMSCard Bottom---------------------------------------------*/
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getTSyear                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_getTSYear()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TSポイント年別情報取得。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*            なし                                                            */
    /*  【戻り値】                                                                */
    /*            なし                                                            */
    /******************************************************************************/
    @Override
    public void  cmBTkpskB_getTSYear()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getTSYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    buff = new StringDto(512);                               /* バッファ             */

        /* 初期化 */
        h_nenkan_rankup_kingaku = 0;
        h_gekkan_rankup_kingaku = 0;
        h_kaiage_cnt.arr = 0;


        /* 2022/11/30 MCCM初版 MOD START */
        /*    "SELECT 年間ランクＵＰ対象金額, "
        "       月間ランクＵＰ対象金額%s, "
        "       年間買上回数 "

        "  FROM TSポイント年別情報%d "
        " WHERE 年        =  %d "
        "   AND 顧客番号  =  :v1  ",
         gl_sys_month, gl_sys_year, gl_sys_year); */
        sprintf(h_str_sql, "SELECT 年間買上回数 FROM TSポイント年別情報%d WHERE 年 = %d AND 顧客番号 = ?", gl_sys_year, gl_sys_year);
        /* 2022/11/30 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_geTSYear *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL04");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getTSYear *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            sprintf(buff, "STATUS=%d、%s",sqlca.sqlcode, h_str_sql );
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SQL04 CURSOR FOR sql_stat4;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL04 USING :h_kokyaku_no;
        sqlca.query(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getTSYear *** TSポイント年別情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "SQL検索失敗。" +
                    "STATUS=[%d](TBL:TSポイント年別情報)顧客番号=[%d]",
                    sqlca.sqlcode, h_kokyaku_no );
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getTSYaer *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */
        /* 2022/11/30 MCCM初版 MOD START */
        /*  INTO :h_nenkan_rankup_kingaku, */ /* 年間ランクUP対象金額           */
        /*       :h_gekkan_rankup_kingaku, */ /* 月間ランクUP対象金額           */
        /*       :h_kaiage_cnt;            */ /* 年間買上回数                   */
        sqlca.fetch();
        sqlca.recData(h_kaiage_cnt); /* 年間買上回数                   */
        /* 2022/11/30 MCCM初版 MOD END */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){

            sprintf(buff, "STATUS=[%d] TSポイント年別情報%d" +
                    "[%s]月の期間限定Ｐ失効、顧客番号=[%d]",
                    sqlca.sqlcode, gl_sys_year, gl_shikko_month, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getTSYaer *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            sqlca.close();
            sqlcaManager.close("CUR_SQL04");
            return;
        }

        /*------------------------------------------------------------*/
        /* 2022/11/30 MCCM初版 MOD START */
/*  sprintf(buff, "年間ランクＵＰ対象金額:[%10.0lf],"
        "月間ランクＵＰ対象金額%s:[%10.0lf],年間買上回数:[%10.0lf] ",
        h_nenkan_rankup_kingaku, gl_sys_month, h_gekkan_rankup_kingaku,
        h_kaiage_cnt); */
        sprintf(buff, "年間買上回数:[%10.0f] ", h_kaiage_cnt);
        /* 2022/11/30 MCCM初版 MOD END */
        C_DbgMsg("*** cmBTkpskB_getTSYaer *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getTSYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /* カーソルクローズ */
        sqlcaManager.close("CUR_SQL04");

        return;              /* 処理終了                           */
        /*-----cmBTshikB_getTSYear Bottom---------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getMSKokyaku                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_getMSKokyaku()                                  */
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
    @Override
    public void   cmBTkpskB_getMSKokyaku()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getMSKokyaku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ             */

        /* 初期化 */
        h_kazoku_id.arr = 0;
        h_nenji_rank_cd = 0;
        h_getuji_rank_cd = 0;

        /* 2022/11/30 MCCM初版 MOD START */
        /* "SELECT 年次ランクコード%s, "
        "       月次ランクコード%s%s, "
        "       家族ＩＤ "
        "  FROM MS顧客制度情報 "
        " WHERE 顧客番号  =  :v1 ",
         gl_sys_y, gl_sys_kbn, gl_sys_month); */
        sprintf(h_str_sql, "SELECT 家族ＩＤ FROM MS顧客制度情報 WHERE 顧客番号 = ? " );
        /* 2022/11/30 MCCM初版 MOD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_geMSKokyaku*** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL05");
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_geMSKokyakur *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            sprintf(buff, "STATUS=%d、%s",sqlca.sqlcode, h_str_sql );
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SQL05 CURSOR FOR sql_stat5;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL05 USING :h_kokyaku_no;
        sqlca.query(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_geMSKokyaku *** MS顧客制度情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "SQL検索に失敗しました。" +
                    "STATUS=[%d](TBL:MS顧客制度情報)顧客番号=[%d]",
                    sqlca.sqlcode, h_kokyaku_no );
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_geMSKokyaku *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */
        /* 2022/11/30 MCCM初版 MOD START */
        /*    INTO :h_nenji_rank_cd,      */   /* 年次ランクコード           */
        /*         :h_getuji_rank_cd,     */   /* 月次ランクコード           */
        /*         :h_kazoku_id;          */   /* 家族ID                     */
        sqlca.fetch();
        sqlca.recData(h_kazoku_id); /* 家族ID                     */
        /* 2022/11/30 MCCM初版 MOD END */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf(buff, "STATUS=[%d] MS顧客制度情報 " +
                    "[%s]月の期間限定Ｐ失効、顧客番号=[%d]",
                    sqlca.sqlcode, gl_shikko_month, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getMSKokyaku *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            sqlca.close();
            sqlcaManager.close("CUR_SQL05");
            return;
        }

        /*------------------------------------------------------------*/
        /* 2022/11/30 MCCM初版 MOD START */
/*   sprintf(buff, "年次ランクコード%s:[%d],"
        "月次ランクコード%s%s:[%d],家族ＩＤ:[%d]", gl_sys_y,
        h_nenji_rank_cd, gl_sys_kbn, gl_sys_month, h_getuji_rank_cd,
        h_kazoku_id); */
        sprintf(buff, "家族ＩＤ:[%d]", h_kazoku_id);
        /* 2022/11/30 MCCM初版 MOD END */
        C_DbgMsg("*** cmBTkpskB_geMSKokyaku *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getMSKokyakur処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* カーソルクローズ */
        sqlcaManager.close("CUR_SQL05");
        return;              /* 処理終了                           */
        /*-----cmBTshikB_getMSKokyakur Bottom-----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getMSKazoku                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_getMSKazoku()                                  */
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
    @Override
    public void  cmBTkpskB_getMSKazoku()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getMSKazoku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ             */

        /* 初期化 */
        h_kazoku_nenji_rank_cd = 0;
        h_kazoku_getuji_rank_cd = 0;

        sprintf(h_str_sql, "SELECT 年次ランクコード%s, 月次ランクコード%s%s FROM MS家族制度情報 WHERE 家族ＩＤ = ? ",
                gl_sys_y, gl_sys_kbn, gl_sys_month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getMSKazoku *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        // EXEC SQL PREPARE sql_stat6 from :h_str_sql;
        sqlca.sql = h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL06");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getMSKazoku *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTkpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(buff, "STATUS=%d、%s",sqlca.sqlcode, h_str_sql );
            APLOG_WT("700", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SQL06 CURSOR FOR sql_stat6;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL06 USING :h_kazoku_id;
        sqlca.query(h_kazoku_id);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getMSKazoku *** MS家族制度情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "SQL検索に失敗しました。" +
                    "STATUS=[%d](TBL:MS家族制度情報)顧客番号=[%d]", sqlca.sqlcode,
                    h_kokyaku_no );
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getMSKazoku *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return;
        }
        /* カーソルフェッチ */
        // EXEC SQL FETCH CUR_SQL06
        // INTO :h_kazoku_nenji_rank_cd,  /* 家族年次ランクコード           */
        // :h_kazoku_getuji_rank_cd; /* 家族月次ランクコード           */
        sqlca.fetch();
        sqlca.recData(h_kazoku_nenji_rank_cd, h_kazoku_getuji_rank_cd);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){

            sprintf(buff, "STATUS=[%d] MS家族制度情報" +
                    "[%s]月の期間限定Ｐ失効、顧客番号=[%d]",
                    sqlca.sqlcode, gl_shikko_month, h_kokyaku_no);
            APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_getMSKazoku *** %s\n", buff);
            C_DbgEnd("cmBTkpskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
            // EXEC SQL CLOSE CUR_SQL06;
//            sqlca.close();
            sqlcaManager.close("CUR_SQL06");
            return;
        }
        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_SQL06;
        sqlcaManager.close("CUR_SQL06");

        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],月次ランクコード%s%s:[%d]",
                gl_sys_y, h_kazoku_nenji_rank_cd, gl_sys_kbn, gl_sys_month,
                h_kazoku_getuji_rank_cd);
        C_DbgMsg("*** cmBTkpskB_getMSKazoku *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getMSKazoku処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return;              /* 処理終了                           */
        /*-----cmBTshikB_getMSKazoku Bottom-------------------------------------------*/
    }

    /* 2022/11/30 MCCM初版 ADD START */
    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_getTSRank                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_getTSRank()                               */
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
    @Override
    public int  cmBTkpskB_getTSRank()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_getTSRank処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(512);                               /* バッファ              */

        /* 初期化 */
        h_rank_nenji_rank_cd.arr = 0;
        h_rank_getuji_rank_cd.arr = 0;

        sprintf(h_str_sql,
                "SELECT 年次ランクコード%s, " +
                "       月次ランクコード%s%s, " +
                "       年間ランクＵＰ対象金額%s, " +
                "       月間ランクＵＰ対象金額%s%s " +
                "  FROM TSランク情報 " +
                " WHERE 顧客番号  =  ? ",
                gl_sys_y, gl_sys_kbn, gl_sys_month,
                gl_sys_y, gl_sys_kbn, gl_sys_month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getTSRank *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
        // EXEC SQL PREPARE sql_stat8 from :h_str_sql;
        sqlca.sql = h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL08");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTkpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SQL08 CURSOR FOR sql_stat8;

        /* カーソルオープン */
        // EXEC SQL OPEN CUR_SQL08 USING :h_kokyaku_no;
        sqlca.query(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_getTSRank *** TSランク情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTkpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* カーソルフェッチ */
        // EXEC SQL FETCH CUR_SQL08
        // INTO :h_rank_nenji_rank_cd,         /* TS年次ランクコード           */
        // :h_rank_getuji_rank_cd,        /* TS月次ランクコード           */
        // :h_rank_nenkan_rankup_kingaku, /* TSランク情報 年間ランクアップ対象金額 */
        // :h_rank_gekkan_rankup_kingaku; /* TSランク情報 月間ランクアップ対象金額 */
        sqlca.fetch();
        sqlca.recData(h_rank_getuji_rank_cd, h_rank_nenkan_rankup_kingaku, h_rank_gekkan_rankup_kingaku);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTkpskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
            // EXEC SQL CLOSE CUR_SQL08;
//            sqlca.close();
            sqlcaManager.close("CUR_SQL08");
            return C_const_NG;
        }
        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_SQL08;
//        sqlca.close();
        sqlcaManager.close("CUR_SQL08");
        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d]," +
                "月次ランクコード%s%s:[%d]," +
                "年間ランクアップ対象金額%s:[%f]," +
                "月間ランクアップ対象金額%s%s:[%f]," +
                "顧客番号:[%d]",
                gl_sys_y, h_rank_nenji_rank_cd.intVal(),
                gl_sys_kbn, gl_sys_month, h_rank_getuji_rank_cd.intVal(),
                gl_sys_y, h_rank_nenkan_rankup_kingaku.floatVal(),
                gl_sys_kbn, gl_sys_month, h_rank_gekkan_rankup_kingaku.floatVal(),
                h_kokyaku_no );
        C_DbgMsg("*** cmBTkpskB_getTSRank *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_getTSRank処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTkpskB_getTSRank Bottom---------------------------------------------*/
    }
    /* 2022/11/30 MCCM初版 ADD END */

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_insertHS                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_insertHS( int this_yyyy )   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別情報登録。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*           int this_yyyy  当年度                              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /******************************************************************************/
    @Override
    public int   cmBTkpskB_insertHS( int this_yyyy )
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_insertHS処理");
        /*---------------------------------------------------------------------*/

        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/

         int wk_i;                 /* 使用ランクコードの算出用Index */
         Integer[] wk_runk = new Integer[4];           /* 使用ランクコードの算出用配列  */
         int rtn_cd;                        /* 戻り値                        */
        IntegerDto rtn_status = new IntegerDto();                          /* 結果ステータス                */

        /* システム日時取得 */
        rtn_cd = C_GetSysDateTime(gl_sysdate, gl_systime);
        if ( rtn_cd != C_const_OK){
            APLOG_WT("700", 0, null, "日付時刻取得処理に失敗しました。",
                    0, 0, 0, 0, 0);
        }
        i_sysdate = atoi(gl_bat_date);

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        /* システム日時取得 */
        i_bat_date = atoi(gl_bat_date);
        /* 2021.04.15 HSポイント日別情報設定値修正 End */

        /* ポイント日別情報構造体初期化 */
        hsptymd_t = new TSHS_DAY_POINT_TBL();

        /* ---追加情報をセット--------------------------------------------------- */
        hsptymd_t.system_ymd.arr                             = i_sysdate;
        strcpy(hsptymd_t.kokyaku_no, h_vr_kokyaku_no.toString());
        hsptymd_t.kokyaku_no.len = strlen(h_vr_kokyaku_no.toString());
        /* 顧客番号                     */
        hsptymd_t.kaiin_kigyo_cd.arr = h_kigyo_cd.intVal();                          /* 会員企業コード               */
        hsptymd_t.kaiin_kyu_hansya_cd.arr = h_kyu_hansya_cd.longVal();                     /* 会員旧販社コード             */
        strcpy(hsptymd_t.kaiin_no, h_kaiin_no.toString());              /* 会員番号                     */
        hsptymd_t.kaiin_no.len = strlen(h_kaiin_no.toString());
        /* 2022/12/01 MCCM初版 DEL END */
        /*  hsptymd_t.nyukai_kigyo_cd                        = h_nyukai_kigyo_cd; */                /* 入会企業コード               */
        /*  hsptymd_t.nyukai_tenpo                           = h_nyukai_tenpo; */                   /* 入会店舗                     */
        /*  hsptymd_t.hakken_kigyo_cd                        = h_hakken_kigyo_cd; */                /* 発券企業コード               */
        /*  hsptymd_t.hakken_tenpo                           = h_hakken_tenpo; */                   /* 発券店舗                     */
        /* 2022/12/01 MCCM初版 DEL END */
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        /* hsptymd_t.toroku_ymd                             = i_sysdate; */                     /* 登録年月日                   */
        /*hsptymd_t.data_ymd                               = i_sysdate;  */                     /* データ年月日                 */
        hsptymd_t.toroku_ymd.arr = i_bat_date;                           /* 登録年月日                   */
        hsptymd_t.data_ymd.arr = i_bat_date;                           /* データ年月日                 */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        /* 2022/12/01 MCCM初版 DEL END */
        /*  hsptymd_t.kigyo_cd                               = h_nyukai_kigyo_cd; */                /* 企業コード                   */
        /*  hsptymd_t.mise_no                                = h_nyukai_tenpo; */                   /* 店番号                       */
        /* 2022/12/01 MCCM初版 DEL END */
        hsptymd_t.riyu_cd.arr = RIYU_CD;                             /* 理由コード                   */
        hsptymd_t.card_nyuryoku_kbn.arr = 4;                                   /* カード入力区分               */
        hsptymd_t.riyo_point.arr = h_kikan_p.floatVal();                           /* 利用ポイント                 */

        /* 2022/12/01 MCCM初版 MOD START */
        /*  hsptymd_t.kojin_getuji_rank_cd                   = h_getuji_rank_cd; */                 /* 個人月次ランクコード         */
        /*  hsptymd_t.kojin_nenji_rank_cd                    = h_nenji_rank_cd; */                  /* 個人年次ランクコード         */
        hsptymd_t.kojin_getuji_rank_cd.arr = h_rank_getuji_rank_cd.intVal();               /* 個人月次ランクコード         */
        hsptymd_t.kojin_nenji_rank_cd.arr = h_rank_nenji_rank_cd.intVal();                /* 個人年次ランクコード         */
        /* 2022/12/01 MCCM初版 MOD END */

        hsptymd_t.kazoku_getuji_rank_cd.arr = h_kazoku_getuji_rank_cd;             /* 家族月次ランクコード         */
        hsptymd_t.kazoku_nenji_rank_cd.arr = h_kazoku_nenji_rank_cd;              /* 家族年次ランクコード         */
        /* 使用ランクコードの算出 */
        wk_runk[0] = h_getuji_rank_cd;
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        wk_runk[1] = h_nenji_rank_cd;
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        wk_runk[2] = h_kazoku_nenji_rank_cd;
        wk_runk[3] = h_kazoku_getuji_rank_cd;
        hsptymd_t.shiyo_rank_cd.arr = 0;
        for(wk_i=0;wk_i<=3;wk_i++)
        {
            if(wk_runk[wk_i] > hsptymd_t.shiyo_rank_cd.intVal())
            {
                hsptymd_t.shiyo_rank_cd.arr = wk_runk[wk_i];                       /* 使用ランクコード             */
            }
        }
        hsptymd_t.kaiage_cnt.arr = h_kaiage_cnt.floatVal();                 /* 買上回数                     */

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        hsptymd_t.koshinmae_riyo_kano_point.arr
                =h_yokunen_p.floatVal()+h_tonen_p.floatVal()+h_zennen_p.floatVal()+h_kikan_p.floatVal()+h_kikan_p1.floatVal()+h_kikan_p2.floatVal()+h_kikan_p3.floatVal()+h_kikan_p4.floatVal();
        /* 更新前利用可能ポイント       */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        /* 2022/12/01 MCCM初版 MOD START */
/*  hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku
        = h_gekkan_rankup_kingaku;      */
        /* 更新前月間ランクＵＰ対象金額 */
/*  hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku
        = h_nenkan_rankup_kingaku;     */
        /* 更新前年間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku.arr  = h_rank_gekkan_rankup_kingaku.floatVal(); /* 更新前月間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku.arr  = h_rank_nenkan_rankup_kingaku.floatVal(); /* 更新前年間ランクＵＰ対象金額 */
        /* 2022/12/01 MCCM初版 MOD END */

        /* 2022/12/01 MCCM初版 MOD START */
        /*  hsptymd_t.kazoku_id                                     = h_kazoku_id; */               /* 家族ＩＤ                     */
        sprintf(hsptymd_t.kazoku_id , "%d" , h_kazoku_id);                                     /* 家族ＩＤ                     */
        /* 2022/12/01 MCCM初版 MOD END */

        hsptymd_t.delay_koshin_ymd.arr                              = i_sysdate;                    /* ディレイ更新年月日           */
        strcpy(hsptymd_t.delay_koshin_apl_version, Program_Name_Ver.arr);            /* ディレイ更新ＡＰＬバージョン */
        hsptymd_t.batch_koshin_ymd.arr                              = i_sysdate;                    /* バッチ更新日                 */
        hsptymd_t.saishu_koshin_ymd.arr                             = i_sysdate;                    /* 最終更新日                   */
        hsptymd_t.saishu_koshin_ymdhms.arr                          = (double) i_sysdate;                    /* 最終更新日時                 */
        strcpy(hsptymd_t.saishu_koshin_programid,                 Program_Name.arr);                /* 最終更新プログラムＩＤ       */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo.arr    = this_yyyy;                    /* 更新前利用可能通常Ｐ基準年度 */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo.arr       = h_zennen_p.floatVal();                   /* 更新前利用可能通常Ｐ前年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo.arr        = h_tonen_p.floatVal();                    /* 更新前利用可能通常Ｐ当年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo.arr      = h_yokunen_p.floatVal();                  /* 更新前利用可能通常Ｐ翌年度   */
        hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo.arr             = this_yyyy;
        /* 要求付与通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo.arr             = this_yyyy;                    /* 要求利用通常Ｐ基準年度       */
        hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo.arr            = this_yyyy;
        /* 更新付与通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point_kijun_nendo.arr            = this_yyyy;                    /* 更新利用通常Ｐ基準年度       */
        hsptymd_t.koshinmae_kikan_gentei_point_kijun_month.arr      = gl_i_shikko_month;            /* 更新前期間限定Ｐ基準月       */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0.arr       = h_kikan_p.floatVal();                    /* 更新前利用可能期間限定Ｐ０   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1.arr       = h_kikan_p1.floatVal();                   /* 更新前利用可能期間限定Ｐ１   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2.arr       = h_kikan_p2.floatVal();                   /* 更新前利用可能期間限定Ｐ２   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3.arr       = h_kikan_p3.floatVal();                   /* 更新前利用可能期間限定Ｐ３   */
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4.arr       = h_kikan_p4.floatVal();                   /* 更新前利用可能期間限定Ｐ４   */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month.arr     = gl_i_shikko_month;            /* 要求付与期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point.arr                 = h_kikan_p.floatVal();                    /* 要求利用期間限定Ｐ           */
        hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month.arr     = gl_i_shikko_month;            /* 要求利用期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point0.arr                = h_kikan_p.floatVal();                    /* 要求利用期間限定Ｐ０         */
        hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month.arr    = gl_i_shikko_month;            /* 更新付与期間限定Ｐ基準月     */
        hsptymd_t.koshin_riyo_kikan_gentei_point.arr                = h_kikan_p.floatVal();                    /* 更新利用期間限定Ｐ           */
        hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month.arr    = gl_i_shikko_month;            /* 更新利用期間限定Ｐ基準月     */

        /* 2022/12/01 MCCM初版 ADD START */
        hsptymd_t.kaisha_cd_mcc.arr                                 = h_nyukai_kigyo_cd_mcc.intVal();        /* 会社コードＭＣＣ               */
        hsptymd_t.mise_no_mcc.arr                                   = h_nyukai_tenpo_mcc.intVal();           /* 店番号ＭＣＣ                   */
        hsptymd_t.meisai_su.arr                                     = gl_kikan_meisai_su;           /* 期間限定Ｐ明細数               */
        hsptymd_t.nyukai_kaisha_cd_mcc.arr                          = h_nyukai_kigyo_cd_mcc.intVal();        /* 入会会社コードＭＣＣ           */
        hsptymd_t.nyukai_tenpo_mcc.arr                              = h_nyukai_tenpo_mcc.intVal();           /* 入会店舗ＭＣＣ                 */
        /* 2022/12/01 MCCM初版 ADD END */


        /* 失効対象月に対応した期間限定ＰＭＭに値を設定する */
        cmBTkpskB_setMonth(gl_i_shikko_month);
        /*----------------------------------------------------------------*/
        C_DbgMsg( "*** HSポイント日別情報追加結果 *** %s\n" , "入力引数情報" );
        C_DbgMsg( "システム年月日                 =[%d]\n"  , hsptymd_t.system_ymd);
        C_DbgMsg( "顧客番号                       =[%s]\n"  ,
                hsptymd_t.kokyaku_no.arr                  );
        C_DbgMsg( "会員企業コード                 =[%d]\n"     ,
                hsptymd_t.kaiin_kigyo_cd                  );
        C_DbgMsg( "会員旧販社コード               =[%d]\n"     ,
                hsptymd_t.kaiin_kyu_hansya_cd             );
        C_DbgMsg( "会員番号                       =[%s]\n"     ,
                hsptymd_t.kaiin_no.arr                    );
        C_DbgMsg( "入会企業コード                 =[%d]\n"     ,
                hsptymd_t.nyukai_kigyo_cd                 );
        C_DbgMsg( "入会店舗                       =[%d]\n"     ,
                hsptymd_t.nyukai_tenpo                    );
        C_DbgMsg( "発券企業コード                 =[%d]\n"     ,
                hsptymd_t.hakken_kigyo_cd                 );
        C_DbgMsg( "発券店舗                       =[%d]\n"     ,
                hsptymd_t.hakken_tenpo                    );
        C_DbgMsg( "精算年月日                   0 =[%d]\n"     ,
                hsptymd_t.seisan_ymd                      );
        C_DbgMsg( "登録年月日                   0 =[%d]\n"     ,
                hsptymd_t.toroku_ymd                      );
        C_DbgMsg( "データ年月日             today =[%d]\n"     ,
                hsptymd_t.data_ymd                        );
        C_DbgMsg( "企業コード                     =[%d]\n"     ,
                hsptymd_t.kigyo_cd                        );
        C_DbgMsg( "店番号                         =[%d]\n"     ,
                hsptymd_t.mise_no                         );
        C_DbgMsg( "ターミナル番号               0 =[%d]\n"     ,
                hsptymd_t.terminal_no                     );
        /* 2022/11/30 MCCM初版 MOD START */
        /*  C_DbgMsg( "取引番号                     0 =[%d]\n"     , */
        C_DbgMsg( "取引番号                     0 =[%f]\n"     ,
                /* 2022/11/30 MCCM初版 MOD END */
                hsptymd_t.torihiki_no                     );
        C_DbgMsg( "時刻                         0 =[%d]\n"     ,
                hsptymd_t.jikoku_hms                      );
        C_DbgMsg( "理由コード                1192 =[%d]\n"     ,
                hsptymd_t.riyu_cd                         );
        C_DbgMsg( "カード入力区分               4 =[%d]\n"     ,
                hsptymd_t.card_nyuryoku_kbn               );
        C_DbgMsg( "処理対象ファイルレコード番号 0 =[%d]\n"     ,
                hsptymd_t.shori_taisho_file_record_no     );
        C_DbgMsg( "リアル更新フラグ             0 =[%d]\n"     ,
                hsptymd_t.real_koshin_flg                 );
        C_DbgMsg( "付与ポイント                 0 =[%10.0f]\n",
                hsptymd_t.fuyo_point                      );
        C_DbgMsg( "利用ポイント                   =[%10.0f]\n",
                hsptymd_t.riyo_point                      );
        C_DbgMsg( "基本Ｐ率対象ポイント         0 =[%10.0f]\n",
                hsptymd_t.kihon_pritsu_taisho_point       );
        C_DbgMsg( "ランクＵＰ対象金額           0 =[%10.0f]\n",
                hsptymd_t.rankup_taisho_kingaku           );
        C_DbgMsg( "ポイント対象金額             0 =[%10.0f]\n",
                hsptymd_t.point_taisho_kingaku            );
        C_DbgMsg( "サービス券発行枚数           0 =[%d]\n"     ,
                hsptymd_t.service_hakko_maisu             );
        C_DbgMsg( "サービス券利用枚数           0 =[%d]\n"     ,
                hsptymd_t.service_riyo_maisu              );
        C_DbgMsg( "個人月次ランクコード           =[%d]\n"     ,
                hsptymd_t.kojin_getuji_rank_cd            );
        C_DbgMsg( "個人年次ランクコード           =[%d]\n"     ,
                hsptymd_t.kojin_nenji_rank_cd             );
        C_DbgMsg( "家族月次ランクコード           =[%d]\n"     ,
                hsptymd_t.kazoku_getuji_rank_cd           );
        C_DbgMsg( "家族年次ランクコード           =[%d]\n"     ,
                hsptymd_t.kazoku_nenji_rank_cd            );
        C_DbgMsg( "使用ランクコード               =[%d]\n"     ,
                hsptymd_t.shiyo_rank_cd                   );
        C_DbgMsg( "買上額                       0 =[%10.0f]\n",
                hsptymd_t.kaiage_kingaku                  );
        C_DbgMsg( "買上回数                       =[%10.0f]\n",
                hsptymd_t.kaiage_cnt                      );
        C_DbgMsg( "更新前利用可能ポイント         =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_point       );
        C_DbgMsg( "更新前付与ポイント             =[%10.0f]\n",
                hsptymd_t.koshinmae_fuyo_point            );
        C_DbgMsg( "更新前基本Ｐ率対象ポイント     =[%10.0f]\n",
                hsptymd_t.koshinmae_kihon_pritsu_taisho_point);
        C_DbgMsg( "更新前月間ランクＵＰ対象金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku);
        C_DbgMsg( "更新前年間ランクＵＰ対象金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku);
        C_DbgMsg( "更新前ポイント対象金額         =[%10.0f]\n",
                hsptymd_t.koshinmae_point_taisho_kingaku  );
        C_DbgMsg( "更新前買上額                   =[%10.0f]\n",
                hsptymd_t.koshinmae_kaiage_kingaku        );
        /* 2022/12/01 MCCM初版 ADD START */
/*  C_DbgMsg( "家族ＩＤ                       =[%d]\n"     ,
                                    hsptymd_t.kazoku_id                       ); */
        C_DbgMsg( "家族ＩＤ                       =[%s]\n"     ,
                hsptymd_t.kazoku_id.arr                   );
        /* 2022/12/01 MCCM初版 ADD END */
        C_DbgMsg( "更新前月間家族ランクＵＰ金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_gekkan_kazoku_rankup_taisho_kingaku);
        C_DbgMsg( "更新前年間家族ランクＵＰ金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_nenkan_kazoku_rankup_taisho_kingaku);
        C_DbgMsg( "リアル更新日時              nl =[%d]\n"     ,
                hsptymd_t.real_koshin_ymd                 );
        C_DbgMsg( "リアル更新ＡＰＬバージョン  nl =[%s]\n"     ,
                hsptymd_t.real_koshin_apl_version         );
        C_DbgMsg( "ディレイ更新日時         today =[%d]\n"     ,
                hsptymd_t.delay_koshin_ymd                );
        C_DbgMsg( "ディレイ更新ＡＰＬバージョン   =[%s]\n"     ,
                hsptymd_t.delay_koshin_apl_version        );
        C_DbgMsg( "相殺フラグ                   0 =[%d]\n"     ,
                hsptymd_t.sosai_flg                       );
        C_DbgMsg( "明細チェックフラグ           0 =[%d]\n"     ,
                hsptymd_t.mesai_check_flg                 );
        C_DbgMsg( "明細チェック区分             0 =[%d]\n"     ,
                hsptymd_t.mesai_check_kbn                 );
        C_DbgMsg( "作業企業コード               0 =[%d]\n"     ,
                hsptymd_t.sagyo_kigyo_cd                  );
        C_DbgMsg( "作業者ＩＤ                   0 =[%f]\n"    ,
                hsptymd_t.sagyosha_id                     );
        C_DbgMsg( "作業年月日                   0 =[%d]\n"     ,
                hsptymd_t.sagyo_ymd                       );
        C_DbgMsg( "作業時刻                     0 =[%d]\n"     ,
                hsptymd_t.sagyo_hms                       );
        C_DbgMsg( "バッチ更新日             today =[%d]\n"     ,
                hsptymd_t.batch_koshin_ymd                );
        C_DbgMsg( "最終更新日               today =[%d]\n"     ,
                hsptymd_t.saishu_koshin_ymd               );
        C_DbgMsg( "最終更新日時             today =[%f]\n"    ,
                hsptymd_t.saishu_koshin_ymdhms            );
        C_DbgMsg( "最終更新プログラムＩＤ         =[%s]\n"     ,
                hsptymd_t.saishu_koshin_programid         );
        C_DbgMsg( "要求利用Ｐ内訳フラグ         0 =[%d]\n"     ,
                hsptymd_t.yokyu_riyo_putiwake_flg         );
        C_DbgMsg( "更新前利用可能通常Ｐ基準年度   =[%d]\n"     ,
                hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo );
        C_DbgMsg( "更新前利用可能通常Ｐ前年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo    );
        C_DbgMsg( "更新前利用可能通常Ｐ当年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo     );
        C_DbgMsg( "更新前利用可能通常Ｐ翌年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo   );
        C_DbgMsg( "要求付与通常Ｐ               0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_tujo_point           );
        C_DbgMsg( "要求付与通常Ｐ基準年度        =[%d]\n",
                hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "要求付与通常Ｐ前年度         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_zennendo  );
        C_DbgMsg( "要求付与通常Ｐ当年度         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_tonendo  );
        C_DbgMsg( "要求利用通常Ｐ               0 =[%f]\n",
                hsptymd_t.yokyu_riyo_tujo_point           );
        C_DbgMsg( "要求利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo           );
        C_DbgMsg( "要求利用通常Ｐ前年度         0 =[%f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_zennendo  );
        C_DbgMsg( "要求利用通常Ｐ当年度         0 =[%f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_tonendo   );
        C_DbgMsg( "要求利用通常Ｐ翌年度         0 =[%f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_yokunendo );
        C_DbgMsg( "更新付与通常Ｐ               0 =[%f]\n",
                hsptymd_t.koshin_fuyo_tujo_point          );
        C_DbgMsg( "更新付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t. koshin_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "更新付与通常Ｐ前年度         0 =[%f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_zennendo );
        C_DbgMsg( "更新付与通常Ｐ当年度         0 =[%f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_tonendo  );
        C_DbgMsg( "更新利用通常Ｐ               0 =[%f]\n",
                hsptymd_t.koshin_riyo_tujo_point          );
        C_DbgMsg( "更新利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.koshin_riyo_tujo_point_kijun_nendo          );
        C_DbgMsg( "更新利用通常Ｐ前年度         0 =[%f]\n",
                hsptymd_t.koshin_riyo_tujo_point_zennendo );
        C_DbgMsg( "更新利用通常Ｐ当年度         0 =[%f]\n",
                hsptymd_t.koshin_riyo_tujo_point_tonendo  );
        C_DbgMsg( "更新利用通常Ｐ翌年度         0 =[%f]\n",
                hsptymd_t.koshin_riyo_tujo_point_yokunendo);
        C_DbgMsg( "更新前期間限定Ｐ基準月         =[%d]\n"     ,
                hsptymd_t.koshinmae_kikan_gentei_point_kijun_month    );
        C_DbgMsg( "更新前利用可能期間限定Ｐ０     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0     );
        C_DbgMsg( "更新前利用可能期間限定Ｐ１     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1     );
        C_DbgMsg( "更新前利用可能期間限定Ｐ２     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2     );
        C_DbgMsg( "更新前利用可能期間限定Ｐ３     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3     );
        C_DbgMsg( "更新前利用可能期間限定Ｐ４   0 =[%f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4     );
        C_DbgMsg( "要求付与期間限定Ｐ           0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point   );
        C_DbgMsg( "要求付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求付与期間限定Ｐ０         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point0  );
        C_DbgMsg( "要求付与期間限定Ｐ１         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point1  );
        C_DbgMsg( "要求付与期間限定Ｐ２         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point2  );
        C_DbgMsg( "要求付与期間限定Ｐ３         0 =[%f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point3  );
        C_DbgMsg( "要求利用期間限定Ｐ             =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point   );
        C_DbgMsg( "要求利用期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求利用期間限定Ｐ０           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point0  );
        C_DbgMsg( "要求利用期間限定Ｐ１         0 =[%f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point1  );
        C_DbgMsg( "要求利用期間限定Ｐ２         0 =[%f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point2  );
        C_DbgMsg( "要求利用期間限定Ｐ３         0 =[%f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point3  );
        C_DbgMsg( "要求利用期間限定Ｐ４         0 =[%f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point4  );
        C_DbgMsg( "要求利用期間限定Ｐ４         0 =[%f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point4  );
        C_DbgMsg( "更新付与期間限定Ｐ           0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point  );
        C_DbgMsg( "更新付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month );
        C_DbgMsg( "更新付与期間限定Ｐ０１       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point01);
        C_DbgMsg( "更新付与期間限定Ｐ０２       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point02);
        C_DbgMsg( "更新付与期間限定Ｐ０３       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point03);
        C_DbgMsg( "更新付与期間限定Ｐ０４       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point04);
        C_DbgMsg( "更新付与期間限定Ｐ０５       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point05);
        C_DbgMsg( "更新付与期間限定Ｐ０６       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point06);
        C_DbgMsg( "更新付与期間限定Ｐ０７       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point07);
        C_DbgMsg( "更新付与期間限定Ｐ０８       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point08);
        C_DbgMsg( "更新付与期間限定Ｐ０９       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point09);
        C_DbgMsg( "更新付与期間限定Ｐ１０       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point10);
        C_DbgMsg( "更新付与期間限定Ｐ１１       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point11);
        C_DbgMsg( "更新付与期間限定Ｐ１２       0 =[%f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point12);
        C_DbgMsg( "更新利用期間限定Ｐ             =[%10.0f]\n" ,
                hsptymd_t.koshin_riyo_kikan_gentei_point  );
        C_DbgMsg( "更新利用期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month  );
        C_DbgMsg( "更新利用期間限定Ｐ０１         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point01);
        C_DbgMsg( "更新利用期間限定Ｐ０２         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point02);
        C_DbgMsg( "更新利用期間限定Ｐ０３         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point03);
        C_DbgMsg( "更新利用期間限定Ｐ０４         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point04);
        C_DbgMsg( "更新利用期間限定Ｐ０５         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point05);
        C_DbgMsg( "更新利用期間限定Ｐ０６         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point06);
        C_DbgMsg( "更新利用期間限定Ｐ０７         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point07);
        C_DbgMsg( "更新利用期間限定Ｐ０８         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point08);
        C_DbgMsg( "更新利用期間限定Ｐ０９         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point09);
        C_DbgMsg( "更新利用期間限定Ｐ１０         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point10);
        C_DbgMsg( "更新利用期間限定Ｐ１１         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point11);
        C_DbgMsg( "更新利用期間限定Ｐ１２         =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point12);
        /* 2022/12/01 MCCM初版 ADD START */
        C_DbgMsg( "会社コードＭＣＣ               =[%d]\n",
                hsptymd_t.kaisha_cd_mcc                   );
        C_DbgMsg( "店番号ＭＣＣ                   =[%d]\n",
                hsptymd_t.mise_no_mcc                     );
        C_DbgMsg( "期間限定Ｐ明細数               =[%d]\n",
                hsptymd_t.meisai_su                       );
        C_DbgMsg( "入会会社コードＭＣＣ           =[%d]\n",
                hsptymd_t.nyukai_kaisha_cd_mcc            );
        C_DbgMsg( "入会店番号ＭＣＣ               =[%d]\n",
                hsptymd_t.nyukai_tenpo_mcc                );
        /* 2022/12/01 MCCM初版 ADD END */
        /*----------------------------------------------------------------*/

        /* HSポイント日別情報追加 */
        rtn_cd = cmBTfuncB.C_InsertDayPoint(hsptymd_t, i_sysdate, rtn_status);
        if(rtn_cd != C_const_OK ){
            APLOG_WT( "903", 0, null, "C_InsertDayPoint" ,
                    rtn_cd, 0, 0, 0, 0);
            /*----------------------------------------------------------------*/
            C_DbgMsg( "*** cmBTkpskB_insertHS *** C_InsertDayPoint rtn_cd=[%d]\n",
                    rtn_cd);
            C_DbgMsg( "*** cmBTkpskB_insertHS *** C_InsertDayPoint rtn_status=[%d]\n", rtn_status.arr);
            /*----------------------------------------------------------------*/
            /* 処理を終了する */
            return C_const_NG;
        }
        w_seq=hsptymd_t.shori_seq.floatVal();
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_insertHS処理処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTkpskB_insertHS処理r Bottom-----------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_updTS                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*    int  cmBTkpskB_updTS ();                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報の更新を行う。                                   */
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
    public int  cmBTkpskB_updTS()
    {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_updTS処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(256);                         /*  バッファ                      */

        /* 失効対象月のフラグを立たせる */
        cmBTkpskB_upBit();

        /* 該当データを更新する */

        // EXEC SQL UPDATE TS利用可能ポイント情報
        // SET 最終更新日   = :i_sysdate,
        //     最終更新日時 = SYSDATE,
        //     最終更新プログラムＩＤ = :Program_Name,
        //     期間限定Ｐ失効フラグ = :h_shikko_flg
        // WHERE 顧客番号 = :h_kokyaku_no;


        StringDto sql = new StringDto();
        sql.arr = "UPDATE TS利用可能ポイント情報 " +
                "SET 最終更新日 = ?, 最終更新日時 = SYSDATE(), " +
                "最終更新プログラムＩＤ = ?, " +
                "期間限定Ｐ失効フラグ = ? " +
                "WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(i_sysdate, Program_Name, h_shikko_flg, h_kokyaku_no);


        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", buff, 0, 0);

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_updTSt *** TS利用可能情報更新 エラー%s\n", buff);
            C_DbgEnd(" cmBTkpskB_updTS処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "更新完了 顧客番号:[%d]、期間限定Ｐ失効フラグ:[%d]",
                h_kokyaku_no, h_shikko_flg);
        C_DbgMsg("*** cmBTkpskB_updTSt *** %s\n", buff);
        C_DbgEnd(" cmBTkpskB_updTS処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTkpskB_updTS Bottom-------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_upBit                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_upBit( )                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     有効期限切れの期間限定に対応するbitをON(1)にする。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*             なし                                                           */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void cmBTkpskB_upBit()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_upBit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
         int check_flg;                   /* bit演算用フラグ        */
        StringDto buff = new StringDto(160);                   /* バッファ               */

        /*--------------------------------------*/
        /*  初期処理                            */
        /*--------------------------------------*/
        check_flg = BASE_BIT;

        /* 失効フラグ設定*/
        check_flg =( check_flg << (12 - gl_i_shikko_month) | CHENGE_BIT);

        sprintf (buff, "失効月：[%d]、期間限定Ｐ失効フラグ：[%#x]、月のbit：[%#x]",
                gl_i_shikko_month, h_shikko_flg,  check_flg);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_upBit *** %s\n", buff);
        /*------------------------------------------------------------*/

        /* 期間限定Ｐ失効フラグ更新 */
        h_shikko_flg.arr = h_shikko_flg.intVal() | check_flg;

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_upBit *** 期間限定Ｐ失効フラグ更新：[%#x]\n",
                h_shikko_flg);
        C_DbgEnd("cmBTkpskiB_upBit処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTkpskB_upBit処理 ----------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_setMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTkpskB_setMonth( int i_month)           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の期間限定Ｐを構造体に設定する。                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void   cmBTkpskB_setMonth( int i_month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_setMonth処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(40);                             /*  バッファ                  */
        /*--------------------------------------*/


        switch(i_month % 12){
            case 1 : hsptymd_t.koshin_riyo_kikan_gentei_point01.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０１  */
                break;
            case 2 : hsptymd_t.koshin_riyo_kikan_gentei_point02.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０２  */
                break;
            case 3 : hsptymd_t.koshin_riyo_kikan_gentei_point03.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０３  */
                break;
            case 4 : hsptymd_t.koshin_riyo_kikan_gentei_point04.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０４  */
                break;
            case 5 : hsptymd_t.koshin_riyo_kikan_gentei_point05.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０５  */
                break;
            case 6 : hsptymd_t.koshin_riyo_kikan_gentei_point06.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０６  */
                break;
            case 7 : hsptymd_t.koshin_riyo_kikan_gentei_point07.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０７  */
                break;
            case 8 : hsptymd_t.koshin_riyo_kikan_gentei_point08.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０８  */
                break;
            case 9 : hsptymd_t.koshin_riyo_kikan_gentei_point09.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ０９  */
                break;
            case 10 : hsptymd_t.koshin_riyo_kikan_gentei_point10.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ１０ */
                break;
            case 11 : hsptymd_t.koshin_riyo_kikan_gentei_point11.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ１１ */
                break;
            case 0 : hsptymd_t.koshin_riyo_kikan_gentei_point12.arr = h_kikan_p.floatVal();
                /* 更新利用期間限定Ｐ１２  */
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d]", i_month);
        C_DbgMsg("*** cmBTkpskB_setMonth *** %s\n", buff);
        C_DbgEnd("cmBTkpskB_setMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/


        /*--- cmBTkpskB_etMonth処理 --------------------------------------*/
    }

    /* 2021.04.15 HSポイント日別情報設定値修正 Start */
    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_check_tujo_point                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_check_tujo_point( int ex_year,    */
    /*                      unisgned int this_year,  int next_year)       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度毎に失効状況を確認する                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*        int ex_year    前年度                                 */
    /*        int this_year  当年度                                 */
    /*        int next_year  翌年度                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void  cmBTkpskB_check_tujo_point(int ex_year, int this_year, int next_year)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_check_tujo_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
         int      rtn_cd;                  /* 戻り値                */

        /* 前年度の残ポイント確認 */
        if (h_zennen_p.intVal() != 0) {
            /* 前年度の失効状況確認 */
            rtn_cd = cmBTkpskB_check_tujo_bit(ex_year);
            if (rtn_cd == DEF_ON){
                h_zennen_p.arr = 0;
            }
        }

        /* 当年度の残ポイント確認 */
        if (h_tonen_p.intVal() != 0) {
            /* 当年度の失効状況確認 */
            rtn_cd = cmBTkpskB_check_tujo_bit(this_year);
            if (rtn_cd == DEF_ON){
                h_tonen_p.arr = 0;
            }
        }

        /* 翌年度の残ポイント確認 */
        if (h_yokunen_p.intVal() != 0) {
            /* 翌年度の失効状況確認 */
            rtn_cd = cmBTkpskB_check_tujo_bit(next_year);
            if (rtn_cd == DEF_ON){
                h_yokunen_p.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_check_tujo_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/
    }
    /*--- cmBTkpskB_check_tujo_point処理 -----------------------------------------*/
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_check_tujo_bit                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_check_tujo_bit( int year)   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度の通常ポイントが失効しているか確認する                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*        int year    対象年度                                  */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int  cmBTkpskB_check_tujo_bit(int year)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_check_tujoBit処理");
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
        wk_year_cd = (year % 5 );

        /* 対象年度フラグ設定*/
        check_flg =( TUJO_BASE_BIT << (4 - wk_year_cd) | TUJO_CHENGE_BIT);

        /* 失効状況確認 */
        bit_kekka = h_tujo_shikko_flg.intVal() & check_flg;
        if ( bit_kekka == check_flg){ /* 失効済み */
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                    "bit結果：[%#x]、失効済み",
                    year, h_tujo_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTkpskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }else{ /* 未失効 */
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                    "bit結果：[%#x]、未失効",
                    year,h_tujo_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTkpskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_check_tujo_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTkpskB_check_tujo_bit処理 -------------------------------------------*/
    }
    /* 2021.04.15 HSポイント日別情報設定値修正 End */
    /* 2021.05.19 更新前利用可能ポイントチェック追加 Start */
    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_check_kikan_point                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTkpskB_check_kikan_point()                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象月毎に失効状況を確認する                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       なし                                                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public void  cmBTkpskB_check_kikan_point()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_check_kikan_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
         int      rtn_cd;                 /* 戻り値                 */

        /* バッチ処理月の残ポイント確認 */
        if (h_kikan_p1.intVal() != 0) {
            /* バッチ処理月の失効状況確認 */
            rtn_cd = cmBTkpskB_checkBit(gl_bat_month);
            if (rtn_cd == DEF_ON){
                h_kikan_p1.arr = 0;
            }
        }

        /* バッチ処理月+1の残ポイント確認 */
        if (h_kikan_p2.intVal() != 0) {
            /* バッチ処理月+1の失効状況確認 */
            rtn_cd = cmBTkpskB_checkBit(gl_bat_month+1);
            if (rtn_cd == DEF_ON){
                h_kikan_p2.arr = 0;
            }
        }

        /* バッチ処理月+2の残ポイント確認 */
        if (h_kikan_p3.intVal() != 0) {
            /* バッチ処理月+2の失効状況確認 */
            rtn_cd = cmBTkpskB_checkBit(gl_bat_month+2);
            if (rtn_cd == DEF_ON){
                h_kikan_p3.arr = 0;
            }
        }

        /* バッチ処理月+3の残ポイント確認 */
        if (h_kikan_p4.intVal() != 0) {
            /* バッチ処理月+3の失効状況確認 */
            rtn_cd = cmBTkpskB_checkBit(gl_bat_month+3);
            if (rtn_cd == DEF_ON){
                h_kikan_p4.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_check_kikan_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTkpskB_check_kikan_point処理 ----------------------------------------*/
    }


    /* 2021.05.19 更新前利用可能ポイントチェック追加 End */

    /* 2022/12/01 MCCM初版 ADD START */
    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTkpskB_kikan_insertHSUchiwake                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            int  cmBTkpskB_kikan_insertHSUchiwake(int kobai_kbn )    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別内訳情報（期間限定ポイント失効）登録。                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int             kobai_kbn       購買区分 1:購買 2:非購買 3:その他     */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /******************************************************************************/
    @Override
    public int cmBTkpskB_kikan_insertHSUchiwake()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTkpskB_kikan_insertHSUchiwake処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
       int wk_i_loop;                /* 明細番号                  */
       int wk_data_no;                     /* 登録データ番号            */
       double[] wk_riyo_point = new double[3];               /* 利用ポイント              */
       int[] wk_kobai_kbn = new int[3];                /* 購買区分                  */
       StringDto wk_column = new StringDto(4096);                /* 登録列                    */
       StringDto wk_column_data = new StringDto(4096);           /* 登録データ                */
       StringDto wk_data_no_text = new StringDto(9);             /* データ番号文字列          */
       StringDto buff = new StringDto(2048);                     /* バッファ                  */

       StringDto wk_c = new StringDto(4096);                     /* 登録列                    */
       StringDto wk_cd = new StringDto(4096);                    /* 登録データ                    */

        double       wk_shori_seq;
        /*-----------------------------------------------*/
        /*  変数初期化                                   */
        /*-----------------------------------------------*/
        // memset(wk_riyo_point, 0x00, sizeof(wk_riyo_point));
        // memset(wk_kobai_kbn, 0x00, sizeof(wk_kobai_kbn));
        memset(wk_c, 0x00, sizeof(wk_c));
        memset(wk_cd, 0x00, sizeof(wk_cd));
        memset(wk_data_no_text, 0x00, sizeof(wk_data_no_text));
        memset(wk_column, 0x00, sizeof(wk_column));
        memset(wk_column_data, 0x00, sizeof(wk_column_data));
        wk_data_no = 0;

        /* 登録データ配列に購買Ｐデータをセットする */
        if( h_kikan_kobai_before_month.intVal() != 0 ){
            wk_riyo_point[wk_data_no] = h_kikan_kobai_before_month.floatVal();
            wk_kobai_kbn[wk_data_no] = 1;
            wk_data_no++;
        }

        /* 登録データ配列に非購買Ｐデータをセットする */
        if ( h_kikan_hikobai_before_month.intVal() != 0 ){
            wk_riyo_point[wk_data_no] = h_kikan_hikobai_before_month.floatVal();
            wk_kobai_kbn[wk_data_no] = 2;
            wk_data_no++;
        }

        /* 登録データ配列にその他Ｐデータをセットする */
        if ( h_kikan_sonota_before_month.intVal() != 0 ){
            wk_riyo_point[wk_data_no] = h_kikan_sonota_before_month.floatVal();
            wk_kobai_kbn[wk_data_no] = 3;
            wk_data_no++;
        }
        C_DbgMsg( "h_kikan_kobai_before_month : %f\n", h_kikan_kobai_before_month);
        C_DbgMsg( "h_kikan_hikobai_before_month : %f\n", h_kikan_hikobai_before_month);
        C_DbgMsg( "h_kikan_sonota_before_month : %f\n", h_kikan_sonota_before_month);

        wk_shori_seq = 0;
        wk_shori_seq = w_seq;

        /* 登録列作成 */
        for ( wk_i_loop=0 ; wk_i_loop < wk_data_no ; wk_i_loop++ ){
            C_DbgMsg( "wk_i_loop : %d\n", wk_i_loop);

            memset(wk_data_no_text, 0x00, sizeof(wk_data_no_text));

            switch(wk_i_loop){
                case 0 : strcpy(wk_data_no_text, "０１");
                    break;
                case 1 : strcpy(wk_data_no_text, "０２");
                    break;
                case 2 : strcpy(wk_data_no_text, "０３");
                    break;
            }
            /* 登録列SQL作成 */
            memset(wk_column, 0x00, sizeof(wk_column));
            sprintf(wk_column,
                    " %s," +
                    " 付与利用区分%s," +
                    " ポイント種別%s," +
                    " 利用ポイント%s," +
                    " 通常期間限定区分%s," +
                    " ポイント有効期限%s," +
                    " 購買区分%s ",
                    wk_column, wk_data_no_text, wk_data_no_text, wk_data_no_text,
                    wk_data_no_text, wk_data_no_text, wk_data_no_text );
            strcat(wk_c, wk_column);

            /* 登録データSQL作成 */
            memset(wk_column_data, 0x00, sizeof(wk_column_data));
            sprintf( wk_column_data,
                    " %s, 2, 2, %f, 2, " +
                    " TO_CHAR ( TRUNC ( LAST_DAY( ADD_MONTHS( TO_DATE(%s,'YYYYMMDD'),-1))),'YYYYMMDD')," +
                    " %d ",
                    wk_column_data, wk_riyo_point[wk_i_loop], gl_bat_date, wk_kobai_kbn[wk_i_loop] );
            strcat(wk_cd, wk_column_data);
            C_DbgMsg( "wk_cd : %s\n", wk_cd);
            C_DbgMsg( "wk_c  : %s\n", wk_c);

        }

        /* SQL文字列作成 */
        sprintf(h_str_sql,
                " INSERT INTO HSポイント日別内訳情報%d%02d" +
                " ( " +
                " システム年月日," +
                " 顧客番号," +
                " 処理通番," +
                " 枝番 " +
                " %s " +
                " ) " +
                " VALUES " +
                " ( " +
                " %s, %s, %f, 1 " +
                " %s " +
                " ) " ,
                gl_bat_year, gl_bat_month, wk_c,
                gl_bat_date, h_vr_kokyaku_no.arr, wk_shori_seq, wk_cd );

        /*----------------------------------------------------------------*/
        C_DbgMsg( "*** HSポイント日別内訳情報(期間限定Ｐ)追加結果 *** %s\n" , "入力引数情報" );
        C_DbgMsg( "システム年月日                 =[%d]\n"  , i_sysdate              );
        C_DbgMsg( "顧客番号                       =[%s]\n"  , h_vr_kokyaku_no.arr );
        C_DbgMsg( "利用ポイント０１               =[%f]\n" , wk_riyo_point[0]       );
        C_DbgMsg( "購買区分０１                   =[%d]\n"  , wk_kobai_kbn[0]        );
        C_DbgMsg( "利用ポイント０２               =[%f]\n" , wk_riyo_point[1]       );
        C_DbgMsg( "購買区分０２                   =[%d]\n"  , wk_kobai_kbn[1]        );
        C_DbgMsg( "利用ポイント０３               =[%f]\n" , wk_riyo_point[2]       );
        C_DbgMsg( "購買区分０３                   =[%d]\n"  , wk_kobai_kbn[2]        );
        /*----------------------------------------------------------------*/

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTkpskB_kikan_insertHSUchiwake *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        /* 動的ＳＱＬ文を解析する */
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_kikan_insertHSUchiwake *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd(" cmBTkpskB_kikan_insertHSUchiwake処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* ＳＱＬ文を実行する */
        sqlca.query();

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( buff, "顧客番号=[%s]", h_vr_kokyaku_no.arr );
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "HSポイント日別内訳情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTkpskB_updTSt *** HSポイント日別内訳情報更新 エラー%s\n", buff);
            C_DbgEnd(" cmBTkpskB_kikan_insertHSUchiwake処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTkpskB_kikan_insertHSUchiwake処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK;              /* 処理終了 */
        /*-----cmBTkpskB_kikan_insertHSUchiwake処理 Bottom-----------------------------*/
    }
    /* 2022/12/01 MCCM初版 ADD END */

}
