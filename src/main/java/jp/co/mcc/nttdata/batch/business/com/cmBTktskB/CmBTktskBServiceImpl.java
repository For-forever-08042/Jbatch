package jp.co.mcc.nttdata.batch.business.com.cmBTktskB;


import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTfuncB.CmBTfuncBImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto.HSUCHIWAKE_INSERT;
import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto.TS_SERCH;
import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto.TS_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TSHS_DAY_POINT_TBL;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/* ******************************************************************************
 *   プログラム名   ： 顧客退会ポイント失効処理（cmBTktskB）
 *
 *   【処理概要】
 *     仮退会またはネット利用停止から31日（1か月）経過している
 *     顧客情報について利用可能ポイントの失効処理を行う。
 *
 *   【引数説明】
 *     -DEBUG(-debug)            :（任意）デバッグモードでの実行
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
 *     30.00 :  2020/12/24 NDBS.緒方 ：初版
 *     31.00 :  2021/04/15 NDBS.亀谷 ：HSポイント日別情報の設定値修正
 *     32.00 :  2021/12/09 SSI.上野：共通関数(C_InsertDayPoint)修正によりリコンパイル
 *     33.00 :  2022/01/05 NDBS.緒方 : TSポイント年別情報データなし処理追加
 *     40.00 :  2022/11/30 SSI.山口  ： MCCM 初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2020 NTT DATA BUSINESS SYSTEMS CORPORATION
 ***************************************************************************** */
@Service
public class CmBTktskBServiceImpl extends CmABfuncLServiceImpl implements CmBTktskBService{

    HSUCHIWAKE_INSERT[] hsuchiwake_tsujyo_insert = new HSUCHIWAKE_INSERT[20];
    HSUCHIWAKE_INSERT[] hsuchiwake_kikan_insert = new HSUCHIWAKE_INSERT[20];

    TSHS_DAY_POINT_TBL hsptymd_t = new TSHS_DAY_POINT_TBL();

    TS_SERCH ts_serch = new TS_SERCH();
    TS_TBL h_ts = new       TS_TBL();
    /* 2023/07/10 MCCM初版 ADD end */

    /*---------MSカード情報用---------------*/
    public StringDto h_kaiin_no = new StringDto(18+1)      ;      /* 会員番号                           */
    public  ItemDto  h_kigyo_cd=new ItemDto();        /* 企業コード                         */
    public  ItemDto  h_kyu_hansya_cd=new ItemDto();        /* 旧販社コード                       */
    /*---------TSポイント年別情報用---------------*/
    public ItemDto             h_nenkan_rankup_kingaku=new ItemDto();       /* 年間ランクアップ対象金額     */
    public ItemDto             h_gekkan_rankup_kingaku=new ItemDto();       /* 月間ランクアップ対象金額     */
    public ItemDto             h_kaiage_cnt=new ItemDto();                  /* 年間買上回数                 */
    /*---------MS顧客制度情報用---------------*/
    public  ItemDto   h_nenji_rank_cd=new ItemDto();         /* 年次ランクコード                   */
    public  ItemDto   h_getuji_rank_cd=new ItemDto();        /* 月次ランクコード                   */
    public  ItemDto    h_kazoku_id=new ItemDto();             /* 家族ID                             */
    /*---------MS家族制度情報用---------------*/
    public  ItemDto   h_kazoku_nenji_rank_cd=new ItemDto();         /* 家族年次ランクコード        */
    public  ItemDto   h_kazoku_getuji_rank_cd=new ItemDto();        /* 家族月次ランクコード        */

    /* 2022/11/29 MCCM初版 ADD START */
    /*---------TSランク情報用-----------------*/
    public  ItemDto   h_rank_nenji_rank_cd=new ItemDto();               /* TSランク情報 年次ランクコード */
    public  ItemDto   h_rank_getuji_rank_cd=new ItemDto();              /* TSランク情報 月次ランクコード */
    public ItemDto              h_rank_nenkan_rankup_kingaku=new ItemDto();       /* TSランク情報 年間ランクアップ対象金額 */
    public ItemDto              h_rank_gekkan_rankup_kingaku=new ItemDto();       /* TSランク情報 月間ランクアップ対象金額 */
    /* 2022/11/29 MCCM初版 ADD END */

    /* -------各TBL検索用--------------------*/
    public  ItemDto    h_kokyaku_no=new ItemDto();            /* 顧客番号                           */

    /* 検索条件用 */
    public  int   gh_kijun_date;           /* バッチ処理日前日の 31日前の年月日  */
    public StringDto           Program_Name = new StringDto(10);        /* バージョンなしプログラム名         */
    public StringDto           Program_Name_Ver = new StringDto(13);    /* バージョンつきプログラム名         */
    public StringDto           h_str_sql = new StringDto(4096);         /* 実行用SQL文字列                    */
    public  ItemDto   h_sysdate=new ItemDto();                     /* システム日付                       */
    public  ItemDto   h_bat_date=new ItemDto();                    /* バッチ処理日付                     */

    public double  w_seq;
    public double  w_seq_2;
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL INCLUDE  sqlca.h;
    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;
//
//    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE TSHS_DAY_POINT_DATA.h;         /* HSポイント日別情報         */
//
//    TSHS_DAY_POINT_TBL         hsptymd_t;   /* HSポイント日別情報バッファ         */
//
//    /*---------TS利用可能ポイント情報用---------------*/
//    struct TS_TBL{
//        VARCHAR              vr_kokyaku_no[16];  /* 顧客番号                      */
//        double               yokunen_p;         /* 利用可能通常Ｐ翌年度           */
//        double               tonen_p;           /* 利用可能通常Ｐ当年度           */
//        double               zennen_p;          /* 利用可能通常Ｐ前年度           */
//         short int   tujo_p_shiko_flg;  /* 通常Ｐ失効フラグ               */
//         short int   nyukai_kigyo_cd;   /* 入会企業コード                 */
//         int         nyukai_tenpo;      /* 入会店舗                       */
//         short int   hakken_kigyo_cd;   /* 発券企業コード                 */
//         int         hakken_tenpo;      /* 発券店舗                       */
//        double               kikan_p01;         /* 利用可能期間限定Ｐ当月         */
//        double               kikan_p02;         /* 利用可能期間限定Ｐ当月＋１     */
//        double               kikan_p03;         /* 利用可能期間限定Ｐ当月＋２     */
//        double               kikan_p04;         /* 利用可能期間限定Ｐ当月＋３     */
//        double               kikan_p05;         /* 利用可能期間限定Ｐ当月＋４     */
//         short int   kikan_p_shikko_flg;   /* 期間限定Ｐ失効フラグ        */
//        /* 2022/11/29 MCCM初版 ADD START */
//        double               yokunen_kobai_p;   /* 利用可能通常購買Ｐ翌年度       */
//        double               tonen_kobai_p;     /* 利用可能通常購買Ｐ当年度       */
//        double               zennen_kobai_p;    /* 利用可能通常購買Ｐ前年度       */
//        double               yokunen_hikobai_p; /* 利用可能通常非購買Ｐ翌年度     */
//        double               tonen_hikobai_p;   /* 利用可能通常非購買Ｐ当年度     */
//        double               zennen_hikobai_p;  /* 利用可能通常非購買Ｐ前年度     */
//        double               yokunen_sonota_p;  /* 利用可能通常その他Ｐ翌年度     */
//        double               tonen_sonota_p;    /* 利用可能通常その他Ｐ当年度     */
//        double               zennen_sonota_p;   /* 利用可能通常その他Ｐ前年度     */
//        double               kikan_kobai_p01;   /* 利用可能期間限定購買Ｐ当月     */
//        double               kikan_kobai_p02;   /* 利用可能期間限定購買Ｐ当月＋１ */
//        double               kikan_kobai_p03;   /* 利用可能期間限定購買Ｐ当月＋２ */
//        double               kikan_kobai_p04;   /* 利用可能期間限定購買Ｐ当月＋３ */
//        double               kikan_kobai_p05;   /* 利用可能期間限定購買Ｐ当月＋４ */
//        double               kikan_hikobai_p01; /* 利用可能期間限定非購買Ｐ当月     */
//        double               kikan_hikobai_p02; /* 利用可能期間限定非購買Ｐ当月＋１ */
//        double               kikan_hikobai_p03; /* 利用可能期間限定非購買Ｐ当月＋２ */
//        double               kikan_hikobai_p04; /* 利用可能期間限定非購買Ｐ当月＋３ */
//        double               kikan_hikobai_p05; /* 利用可能期間限定非購買Ｐ当月＋４ */
//        double               kikan_sonota_p01;  /* 利用可能期間限定その他Ｐ当月     */
//        double               kikan_sonota_p02;  /* 利用可能期間限定その他Ｐ当月＋１ */
//        double               kikan_sonota_p03;  /* 利用可能期間限定その他Ｐ当月＋２ */
//        double               kikan_sonota_p04;  /* 利用可能期間限定その他Ｐ当月＋３ */
//        double               kikan_sonota_p05;  /* 利用可能期間限定その他Ｐ当月＋４ */
//         short int   nyukai_kigyo_cd_mcc;  /* 入会会社コードＭＣＣ        */
//         int         nyukai_tenpo_mcc;     /* 入会店舗ＭＣＣ              */
//        /* 2022/11/29 MCCM初版 ADD END */
//    } h_ts;
//    /* 2023/07/10 MCCM初版 ADD start */
//    /* 内訳テーブル登録用配列 */
//    typedef struct
//    {
//         int    fuyo_riyo_kbn;                             /* 付与利用区分               */
//         int    meisai_no;                                 /* 明細番号                   */
//         int    point_syubetsu;                            /* ポイント種別               */
//        long            riyo_point;                                /* 利用ポイント               */
//         int    tujo_kikan_gentei_kbn;                     /* 通常期間限定区分           */
//         int    point_yukokigen;                           /* ポイント有効期限           */
//         int    kobai_kbn;                                 /* 購買区分                   */
//    } HSUCHIWAKE_INSERT;
//    HSUCHIWAKE_INSERT hsuchiwake_tsujyo_insert[20];
//    HSUCHIWAKE_INSERT hsuchiwake_kikan_insert[20];
//    /* 2023/07/10 MCCM初版 ADD end */
//
//    /*---------MSカード情報用---------------*/
//    VARCHAR    h_kaiin_no[18+1]      ;      /* 会員番号                           */
//     short int   h_kigyo_cd;        /* 企業コード                         */
//     short int   h_kyu_hansya_cd;   /* 旧販社コード                       */
//    /*---------TSポイント年別情報用---------------*/
//    double               h_nenkan_rankup_kingaku; /* 年間ランクアップ対象金額     */
//    double               h_gekkan_rankup_kingaku; /* 月間ランクアップ対象金額     */
//    double               h_kaiage_cnt;            /* 年間買上回数                 */
//    /*---------MS顧客制度情報用---------------*/
//     short int   h_nenji_rank_cd;   /* 年次ランクコード                   */
//     short int   h_getuji_rank_cd;  /* 月次ランクコード                   */
//     long int    h_kazoku_id;       /* 家族ID                             */
//    /*---------MS家族制度情報用---------------*/
//     short int   h_kazoku_nenji_rank_cd;   /* 家族年次ランクコード        */
//     short int   h_kazoku_getuji_rank_cd;  /* 家族月次ランクコード        */
//
//    /* 2022/11/29 MCCM初版 ADD START */
//    /*---------TSランク情報用-----------------*/
//     short int   h_rank_nenji_rank_cd;         /* TSランク情報 年次ランクコード */
//     short int   h_rank_getuji_rank_cd;        /* TSランク情報 月次ランクコード */
//    double               h_rank_nenkan_rankup_kingaku; /* TSランク情報 年間ランクアップ対象金額 */
//    double               h_rank_gekkan_rankup_kingaku; /* TSランク情報 月間ランクアップ対象金額 */
//    /* 2022/11/29 MCCM初版 ADD END */
//
//    /* -------各TBL検索用--------------------*/
//     long int    h_kokyaku_no;      /* 顧客番号                           */
//
//    /* 検索条件用 */
//     int   gh_kijun_date;           /* バッチ処理日前日の 31日前の年月日  */
//    char           Program_Name[10];        /* バージョンなしプログラム名         */
//    char           Program_Name_Ver[13];    /* バージョンつきプログラム名         */
//    char           h_str_sql[4096];         /* 実行用SQL文字列                    */
//     int   h_sysdate;               /* システム日付                       */
//     int   h_bat_date;              /* バッチ処理日付                     */
//
//    double  w_seq;
//    double  w_seq_2;
//
//    EXEC SQL END DECLARE SECTION;
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF       =          0  ;  /* OFF                                */
    int DEF_ON        =          1  ;  /* ON                                 */
    String C_PRGNAME   ="顧客退会ポイント失効";/* APログ用機能名                   */
    int C_const_Ora_SNAP_OLD  = -1555  ; /* ORA-1555発生                      */
    int C_const_SNAP_OLD      = 55     ; /* ORA-1555発生                      */
    int TUJO_RIYU_CD          =1193    ; /* 顧客退会ポイント失効（通常）      */
    int KIKAN_RIYU_CD         =1194    ; /* 顧客退会ポイント失効（期間限定Ｐ）*/

            /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG  ="-DEBUG" ;         /* デバッグスイッチ                   */
    String DEF_debug  ="-debug" ;         /* デバッグスイッチ                   */
            /*-----------bit演算用----------------------*/
    int TUJO_BASE_BIT      =  0x01   ;/* 通常Ｐ失効フラグ用                 */
    int TUJO_CHENGE_BIT    =  0x20   ;/* 通常Ｐ失効フラグ用                 */
    int KIKAN_BASE_BIT     =  0x0001 ;/* 期間限定Ｐ失効フラグ用             */
    int KIKAN_CHENGE_BIT   =  0x1000 ;/* 期間限定Ｐ失効フラグ用             */
    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    StringDto gl_sysdate = new StringDto(9);        /* システム日付                           */
    StringDto          gl_systime= new StringDto(7);        /* システム時刻                           */
    StringDto          gl_bat_date= new StringDto(9);       /*  バッチ処理日付                        */
    StringDto          gl_bat_yesterday= new StringDto(9);  /*  バッチ処理日付(前日)                  */
    StringDto          gl_bat_date_yyyymm= new StringDto(7);/*  バッチ処理日付年月                    */
    StringDto          gl_bat_date_yyyy= new StringDto(5);  /*  バッチ処理日付年                      */
    StringDto          gl_bat_date_mm= new StringDto(3);    /*  バッチ処理日付月                      */
    StringDto          gl_bat_date_y= new StringDto(4);     /*  バッチ処理日付年下一桁                */
    StringDto          gl_bat_date_kbn= new StringDto(4);   /*  偶数年（０）/奇数年（１）区分         */
    int sql_flg;         /* ORA-1555発生フラグ                     */

    /* 2022/11/29 MCCM初版 ADD START */
    /* char          gl_bat_nendo_y[3]; */
    StringDto         gl_bat_nendo_y= new StringDto(4);            /*  バッチ処理日付の年度下一桁(全角) */
    StringDto         gl_bat_nendo_kbn= new StringDto(3);          /*  偶数年度（０）/奇数年度（１）区分 */
    StringDto         gl_bat_yesterday_yyyymm= new StringDto(7);   /*  バッチ処理日付前日の年月      */
    int gl_meisai_su;            /* 通常Ｐ明細数                   */
    int gl_kikan_meisai_su;      /* 期間限定Ｐ明細数               */
    double gl_kobai_kei;                        /* 購買計                         */
    double gl_hikobai_kei;                      /* 非購買計                       */
    double gl_sonota_kei;                       /* その他計                       */
    double gl_kikan_kobai_kei;                  /* 期間限定Ｐ購買計               */
    double gl_kikan_hikobai_kei;                /* 期間限定Ｐ非購買計             */
    double gl_kikan_sonota_kei;                 /* 期間限定Ｐその他計             */
    /* 2022/11/29 MCCM初版 ADD END */

    /*-----  TS利用可能ポイント情報検索用----------*/
//    struct TS_SERCH{
//         short int next_year;      /* 翌年度                              */
//         short int this_year;      /* 当年度                              */
//         short int ex_year;        /* 当年度                              */
//        char    next_year_cd[4];           /* 翌年度コード（全角）                */
//        char    this_year_cd[4];           /* 当年度コード（全角）                */
//        char    ex_year_cd[4];             /* 前年度コード（全角）                */
//        char month01[7];                /*  対象月1(全角)                         */
//        char month02[7];                /*  対象月2(全角)                         */
//        char month03[7];                /*  対象月3(全角)                         */
//        char month04[7];                /*  対象月4(全角)                         */
//        char month05[7];                /*  対象月5(全角)                         */
//    };

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
//    static int  cmBTktskB_main(  int  *ts_ryokp_upd_cnt_buf,
//                                 int  *hs_pdayp_ins_cnt_tujo_buf,
//                                 int  *hs_pdayp_ins_cnt_kikan_buf);
//    /* 顧客退会ポイント失効主処理         */
//    void        cmBTktskB_getYear( intnext_yyyy,
//                                   intthis_yyyy,  intex_yyyy);
//    /* 年度算出処理                       */
//    void        cmBTktskB_getYearCd( short int year, char *year_cd);
//    /* 年度コード（全角）変換処理         */
//    void        cmBTktskB_getMonth( short int i_month, char *month);
//    /* 対象月（全角）変換処理             */
//    static int  cmBTktskB_shikko(struct TS_SERCH ts_ser,
//                                  int ts_ryokp_upd_cnt_buf,
//                                  int hs_pdayp_ins_cnt_tujo_buf,
//                                  int hs_pdayp_ins_cnt_kikan_buf);
//    /* 顧客退会ポイント失効処理           */
//    void        cmBTktskB_setSql(struct TS_SERCH ts_ser_sql);
//    /* SQL作成処理                        */
//    void        cmBTktskB_setSql_ora1555(struct TS_SERCH ts_ser_sql);
//    /* SQL作成処理(ORA-1555発生時)        */
//    static int  cmBTktskB_getTSriyo(struct TS_SERCH ts_ser_sql);
//    /* TS利用可能ポイント情報取得         */
//    void        cmBTktskB_check_tujo_point( int ex_year,
//                                            int this_year,  int next_year);
//    /* 通常ポイント失効状況確認処理       */
//    static int  cmBTktskB_check_tujo_bit( short int year);
//    /* 通常Ｐ失効フラグ確認処理           */
//    void        cmBTktskB_check_kikan_point();
//    /* 期間限定ポイント失効状況確認処理   */
//    static int  cmBTktskB_check_kikan_bit( short int month);
//    /* 期間限定Ｐ失効フラグ確認処理       */
//    static int  cmBTktskB_pointLost(struct TS_SERCH ts_ser,
//                                     int ts_ryokp_upd_cnt_buf,
//                                     int hs_pdayp_ins_cnt_tujo_buf,
//                                     int hs_pdayp_ins_cnt_kikan_buf);
//    /* ポイント失効処理                   */
//    static int  cmBTktskB_getMSCard();      /* MSカード情報取得処理               */
//    static int  cmBTktskB_getTSYear(char *month);
//    /* TSポイント年別情報取得処理         */
//    static int   cmBTktskB_getMSKokyaku(char *month);
//    /* MS顧客制度情報取得処理             */
//    static int  cmBTktskB_getMSKazoku(char *month);
//    /* MS家族制度情報取得処理             */
//    static int  cmBTktskB_updTS();          /* TS利用可能ポイント情報更新処理     */
//    static int  cmBTktskB_tujo_insertHS(struct TS_SERCH ts_ser );
//    /* HSポイント日別情報（通常ポイント失効）登録処理*/
//    static int  cmBTktskB_kikan_insertHS(struct TS_SERCH ts_ser );
//    /* HSポイント日別情報（期間限定ポイント失効）登録処理*/
//    void        cmBTktskB_setMonth( short int i_month, double kikan_p);
//    /* 利用可能期間限定ＰＭＭ設定処理     */
//    /* 2022/11/30 MCCM初版 ADD START */
//    static int  cmBTktskB_getTSRank(struct TS_SERCH ts_ser); /* TSランク情報取得処理 */
//    static int   cmBTktskB_insertHSUchiwake( short int point_kbn, int meisai_su, HSUCHIWAKE_INSERT hsuchiwake_insert[10]);
//    /* HSポイント日別内訳情報（通常ポイント失効）登録処理     */
//    static int daysInMonth(int year, int month);                                 /* 月末日を取得する */
//    static int isLeapYear( int year );                                                   /* 閏年判定 */
    /* 2022/11/30 MCCM初版 ADD START */


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
    public MainResultDto main( int argc, String[] argv )
    {
        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        /*------------------------------------------------------------*/
        /*--------------------------------------*/
        /*  ローカル変数定義                    */
        /*--------------------------------------*/
        int     rtn_cd;                     /* 関数戻り値                         */
        IntegerDto     rtn_status = new IntegerDto();                 /* 関数結果ステータス                 */
        int     arg_cnt;                    /* 引数チェック用カウンタ             */
         ItemDto    ts_ryokp_upd_cnt_buf;
            /* TS利用可能ポイント情報（ポイント失効）更新件数 */
        ItemDto    hs_pdayp_ins_cnt_tujo_buf;
            /* HSポイント日別情報（通常ポイント失効）登録件数 */
        ItemDto    hs_pdayp_ins_cnt_kikan_buf;
                    /* HSポイント日別情報（期間限定ポイント失効）登録件数 */
        StringDto    arg_Work1 = new StringDto(256);            /* Work Buffer1                        */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        ts_ryokp_upd_cnt_buf = new ItemDto();
        hs_pdayp_ins_cnt_tujo_buf = new ItemDto();
        hs_pdayp_ins_cnt_kikan_buf = new ItemDto();
        memset( gl_bat_date_yyyymm, 0x00, sizeof(gl_bat_date_yyyymm));
        memset( gl_bat_date_yyyy, 0x00, sizeof(gl_bat_date_yyyy));
        memset( gl_bat_date_mm, 0x00, sizeof(gl_bat_date_mm));
        sql_flg = DEF_OFF;

        /* 2022/11/30 MCCM初版 ADD START */
        memset( gl_bat_yesterday_yyyymm, 0x00, sizeof(gl_bat_yesterday_yyyymm));
        /* 2022/11/30 MCCM初版 ADD END */


        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname( argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_GetPgname",
                    rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        memset(Program_Name, 0x00, sizeof(Program_Name));
        strcpy(Program_Name,  Cg_Program_Name);     /* バージョンなしプログラム名 */
        strcpy(Program_Name_Ver, Cg_Program_Name_Ver);
        /* バージョンなしプログラム名 */
        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg( argc, argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_StartBatDbg",
                    rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
        /*------------------------------------------------------------*/
        for ( arg_cnt = 1; arg_cnt < argc; arg_cnt++ ) {
            memset( arg_Work1, 0x00, sizeof(arg_Work1) );
            strcpy( arg_Work1, argv[arg_cnt] );

            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 引数チェック対象パラメータ=[%s]\n", arg_Work1);
            /*------------------------------------------------------------*/
            if ( 0==strcmp(arg_Work1, DEF_DEBUG) || 0==strcmp(arg_Work1, DEF_debug) ) {
                continue;
            } else {                        /* 定義外パラメータ                   */
                APLOG_WT("910", 0, null, arg_Work1, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
                return exit( C_const_APNG );
            }
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
        /*------------------------------------------------------------*/
        rtn_cd = C_OraDBConnect( C_ORACONN_SD, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトNG rtn=[%d]\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*------------------------------------------*/
        /*  バッチ処理日取得処理                    */
        /*------------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日取得 %s\n", "START");
        /*------------------------------------------------------------*/
        /* バッチ処理日（当日）取得 */
        rtn_cd = C_GetBatDate( 0, gl_bat_date, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate（当日）",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*------------------------------------------*/
        /*  バッチ処理日取得(前日)処理              */
        /*------------------------------------------*/
        rtn_cd = C_GetBatDate( -1, gl_bat_yesterday, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日(前日)取得NG rtn=[%d]\n", rtn_cd);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate（前日）",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日取得(当日)OK [%s]\n", gl_bat_date);
        C_DbgMsg("*** main *** バッチ処理日取得(前日)OK [%s]\n", gl_bat_yesterday);
        /*------------------------------------------------------------*/

        /*------------------------------------------*/
        /*  バッチ処理日編集                        */
        /*------------------------------------------*/
        /* バッチ処理日付年月の取得*/
        memcpy(gl_bat_date_yyyymm, gl_bat_date, 6);

        /* バッチ処理日付年の取得*/
        memcpy(gl_bat_date_yyyy, gl_bat_date, 4);

        /* バッチ処理日付月の取得*/
        memcpy(gl_bat_date_mm, gl_bat_date.arr.substring(4), 2);

        /* 2022/11/30 MCCM初版 ADD START */
        /* バッチ処理日付前日の年月値取得*/
        memcpy(gl_bat_yesterday_yyyymm, gl_bat_yesterday, 6);
        /* 2022/11/30 MCCM初版 ADD END */

        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日付年月 [%s]\n", gl_bat_date_yyyymm);
        C_DbgMsg("*** main *** バッチ処理日付年   [%s]\n", gl_bat_date_yyyy);
        C_DbgMsg("*** main *** バッチ処理日付月   [%s]\n", gl_bat_date_mm);
        /*------------------------------------------------------------*/

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

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTktskB_main( ts_ryokp_upd_cnt_buf,
                             hs_pdayp_ins_cnt_tujo_buf,
                             hs_pdayp_ins_cnt_kikan_buf);
        if ( rtn_cd != C_const_OK ) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** cmBTktskB_main NG rtn=[%d]\n", rtn_cd);
            /*------------------------------------------------------------*/

            APLOG_WT("912", 0, null,
                    "顧客退会ポイント失効処理に失敗しました", 0, 0, 0, 0, 0);
            /* 各テーブル更新処理件数を出力 */
            APLOG_WT("107", 0, null,
                    "TS利用可能ポイント情報（顧客退会ポイント失効）",
                    ts_ryokp_upd_cnt_buf.intVal(),0,0,0,0);
            APLOG_WT("107", 0, null,
                    "HSポイント日別情報（顧客退会通常ポイント失効）",
                    hs_pdayp_ins_cnt_tujo_buf.intVal(),0,0,0,0);
            APLOG_WT("107", 0, null,
                    "HSポイント日別情報（顧客退会期間限定ポイント失効）",
                    hs_pdayp_ins_cnt_kikan_buf.intVal(),0,0,0,0);
            rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了処理     */
//            EXEC SQL ROLLBACK WORK RELEASE;         /* ロールバック               */
            sqlca.rollback();
            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 各テーブル更新処理件数を出力 */
        APLOG_WT("107", 0, null,
                "TS利用可能ポイント情報（顧客退会ポイント失効）",
                ts_ryokp_upd_cnt_buf.intVal(),0,0,0,0);
        APLOG_WT("107", 0, null,
                "HSポイント日別情報（顧客退会通常ポイント失効）",
                hs_pdayp_ins_cnt_tujo_buf.intVal(),0,0,0,0);
        APLOG_WT("107", 0, null,
                "HSポイント日別情報（顧客退会期間限定ポイント失効）",
                hs_pdayp_ins_cnt_kikan_buf.intVal(),0,0,0,0);
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** TS利用可能ポイント情報（顧客退会ポイント失効）"+
                " 処理件数=[%d]\n",ts_ryokp_upd_cnt_buf.intVal());
        C_DbgMsg("*** main *** HSポイント日別情報（顧客退会通常ポイント失効）"+
                " 処理件数=[%d]\n",hs_pdayp_ins_cnt_tujo_buf.intVal());
        C_DbgMsg("*** main *** HSポイント日別情報（顧客退会期間限定ポイント失効）" +
                " 処理件数=[%d]\n",hs_pdayp_ins_cnt_kikan_buf.intVal());
        /*------------------------------------------------------------*/

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        /*------------------------------------------------------------*/
        C_DbgEnd("*** main処理 ***", 0, 0, 0);
        /*------------------------------------------------------------*/

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        return exit( C_const_APOK );
    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ：  cmBTktskB_main                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  cmBTktskB_main( unsigned int  *ts_ryokp_upd_cnt_buf,        */
    /*                                unsigned int  *hs_pdayp_ins_cnt_tujo_buf,   */
    /*                                unsigned int  *hs_pdayp_ins_cnt_kikan_buf)  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     顧客退会ポイント失効主処理                                             */
    /*     「仮退会またはネット利用停止から31日（1カ月）経過した、                */
    /*       正常または無効[7]の会員番号を保持していない顧客の                    */
    /*       ポイントがある場合、ポイントの失効を行う。」                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /* unsigned int * ts_ryokp_upd_cnt_buf：                                      */
    /*                             TS利用可能ポイント情報（ポイント失効）更新件数 */
    /* unsigned int * hs_pdayp_ins_cnt_tujo_buf：                                 */
    /*                             HSポイント日別情報 （通常ポイント失効）登録件数*/
    /* unsigned int * hs_pdayp_ins_cnt_kikan_buf：                                */
    /*                         HSポイント日別情報 （期間限定ポイント失効）登録件数*/
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*    55   ： 警告（UNDO領域なし）                                            */
    /*                                                                            */
    /* *****************************************************************************/
    public int  cmBTktskB_main( ItemDto  ts_ryokp_upd_cnt_buf,
                                ItemDto  hs_pdayp_ins_cnt_tujo_buf,
                                ItemDto  hs_pdayp_ins_cnt_kikan_buf)
    {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_main処理");
        /*------------------------------------------------------------*/
        /*--------------------------------------*/
        /*  ローカル変数定義                    */
        /*--------------------------------------*/
        int wk_month;       /* バッチ処理月                        */
        StringDto    taikai_yyyymmdd = new StringDto(9);        /* 退会対象年月日 ※保存期間１カ月     */
        StringDto    taikai_yyyymm= new StringDto(7);          /* 退会対象年月             〃         */
        StringDto    taikai_yyyy= new StringDto(5);            /* 退会対象年               〃         */
        StringDto    taikai_ymd= new StringDto(9);             /* 退会対象年月日(参照用）  〃         */
        StringDto    wk_y= new StringDto(2);                   /* 年下一桁                            */
        int wk_date;              /* バッチ処理日付                      */
        int wk_year;        /* 年                                  */
        IntegerDto     rtn_status = new IntegerDto();                /* 関数結果ステータス                  */
        int     rtn_cd;                    /* 関数戻り値                          */

        /* TS利用可能ポイント情報検索用構造体宣言 */
//        struct TS_SERCH ts_serch;

        /* 初期化 */
//        memset(ts_serch, 0x00, sizeof(ts_serch));
        memset( wk_y, 0x00, sizeof(wk_y));
        memset(gl_bat_date_kbn,  0x00, sizeof(gl_bat_date_kbn));

        /* 対象年度を設定 */
        cmBTktskB_getYear(ts_serch.next_year,
        ts_serch.this_year, ts_serch.ex_year);

        /* 対象年度（全角）変換 */
        cmBTktskB_getYearCd(ts_serch.next_year, ts_serch.next_year_cd); /* 翌年度*/
        cmBTktskB_getYearCd(ts_serch.this_year, ts_serch.this_year_cd); /* 当年度*/
        cmBTktskB_getYearCd(ts_serch.ex_year, ts_serch.ex_year_cd);     /* 前年度*/

        wk_month = atoi(gl_bat_date_mm);
        /*対象月（全角）設定 */
        cmBTktskB_getMonth(wk_month, ts_serch.month01);
        cmBTktskB_getMonth(wk_month+1, ts_serch.month02);
        cmBTktskB_getMonth(wk_month+2, ts_serch.month03);
        cmBTktskB_getMonth(wk_month+3, ts_serch.month04);
        cmBTktskB_getMonth(wk_month+4, ts_serch.month05);

        /* 保存期間取得処理 */
        rtn_cd = C_GetSaveKkn("顧客退会２", gl_bat_yesterday.arr,
                taikai_yyyymmdd,
                taikai_yyyymm,
                taikai_yyyy,
                taikai_ymd, rtn_status);

        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg( "*** cmBTtskB_main *** 退会対象年月日取得NG rtn =[%d]\n",
                    rtn_cd );
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetSaveKkn", rtn_cd,
                    rtn_status, 0,0,0);
            return (C_const_NG);
        }

        /* MS顧客制度、MS家族制度、TSポイント年別情報検索用日付編集 */
        /* 年下一桁取得 */
//        memcpy(wk_y, &(gl_bat_date[3]), 1);
        memcpy(wk_y, String.valueOf(gl_bat_date.arr.charAt(3)), 1);
        rtn_cd = C_ConvHalf2Full(wk_y, gl_bat_date_y);
        if (rtn_cd != C_const_OK){
            APLOG_WT("903", 0, null, "C_ConvHalf2Full(gl_sys_y)",
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_main *** %s\n", "年下一桁全角エラー");
            C_DbgEnd("cmBTktskB_main処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* 偶数年（０）／奇数年（１）区分取得 */
        wk_date = atoi(gl_bat_date);
        wk_year = (wk_date / 10000) % 10;
        if ((wk_year % 2) == 0){ strcpy(gl_bat_date_kbn, "０");}/*偶数年 */
        else{ strcpy(gl_bat_date_kbn, "１");}

        /* 2022/12/02 MCCM初版 ADD START */
        /* 年度下一桁取得 */
        /* sprintf( wk_y , "%d" , ts_serch.this_year%10 ); */
        sprintf( wk_y, "%d" , ts_serch.this_year.intVal()%10 );
        rtn_cd = C_ConvHalf2Full(wk_y, gl_bat_nendo_y);
        if (rtn_cd != C_const_OK){
            APLOG_WT("903", 0, null, "C_ConvHalf2Full(gl_bat_nendo_y)",
                    rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_main *** %s\n", "年下一桁全角エラー");
            C_DbgEnd("cmBTktskB_main処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* 偶数年度（０）／奇数年度（１）区分取得 */
        if ((ts_serch.this_year.intVal() % 2) == 0){ strcpy(gl_bat_nendo_kbn, "０");}/*偶数年 */
        else{ strcpy(gl_bat_nendo_kbn, "１");}
        /* 2022/12/02 MCCM初版 MOD END */

        /* HOST変数に設定 */
        gh_kijun_date = atoi(taikai_yyyymmdd);
        /*------------------------------------------------------------*/
        C_DbgMsg( "*** cmBTshikB_set_date *** 退会対象年月日取得OK rtn =[%d]\n",
                gh_kijun_date );
        /*------------------------------------------------------------*/

        /* 顧客退会ポイント失効処理 */
        while( true ){
            rtn_cd = cmBTktskB_shikko(ts_serch,ts_ryokp_upd_cnt_buf,
                    hs_pdayp_ins_cnt_tujo_buf,
                    hs_pdayp_ins_cnt_kikan_buf);
            if( rtn_cd == C_const_SNAP_OLD){
                continue;
            }else {
                if(rtn_cd == C_const_OK){
                    break;
                }else{
                    APLOG_WT("912", 0, null,
                            "顧客退会ポイント失効処理に失敗しました",
                            0, 0, 0, 0, 0);
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTktskB_main処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }
        }
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_main処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK;              /* 処理終了                           */
        /*--- cmBTtskB_main処理 ------------------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getYear                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*    void  cmBTktskB_getYear( unsigned short int *next_yyyy,                 */
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
    /* *****************************************************************************/
    public void  cmBTktskB_getYear(ItemDto next_yyyy,
                                   ItemDto this_yyyy, ItemDto ex_yyyy)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto               buff = new StringDto(160);                   /* バッファ               */
        int wk_mm;                       /* バッチ処理月           */
        int wk_yyyy;                     /* バッチ処理年           */

        wk_mm = atoi(gl_bat_date_mm);
        wk_yyyy = atoi(gl_bat_date_yyyy);

        /* 月の当年度算出 */
        if ( wk_mm <=3){
            this_yyyy.arr = wk_yyyy-1; /* 年度切替前 */
        }else{
            this_yyyy.arr = wk_yyyy;   /* 年度切替後 */
        }

        /* 前年度算出 */
        ex_yyyy.arr = this_yyyy.intVal() -1;
       /* 翌年度算出 */
        next_yyyy.arr = this_yyyy.intVal() +1;

        /*------------------------------------------------------------*/
        sprintf( buff, "翌年度:[%d]、当年度:[%d]、前年度:[%d]",
                next_yyyy,this_yyyy, ex_yyyy);
        C_DbgMsg("*** cmBTktskB_getYear *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTktskB_getYear処理 -----\--------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getYearCd                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*     voidt  cmBTktskB_getYearCd(unsigned short int year, char *year_cd)     */
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
    /* *****************************************************************************/
    public void  cmBTktskB_getYearCd(ItemDto year, ItemDto year_cd)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getYearCd処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto               buff = new StringDto(160);                   /* バッファ               */

        /* 年度コードを算出 */
        switch( year.intVal() % 5){
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
        C_DbgMsg("*** cmBTktskB_getYearCd *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getYearCd処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTktskB_getYearcd処理 -----------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTktskB_getMonth(unsigned short int i_month,           */
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
    /* *****************************************************************************/
    public void   cmBTktskB_getMonth(int i_month, ItemDto month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getMonth処理");
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
        C_DbgMsg("*** cmBTktskB_getMonth *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTktskB_getMonth処理 -------------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_shikko                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_shikko(struct TS_SERCH ts_ser,            */
    /*                                  unsigned int *ts_ryokp_upd_cnt_buf,       */
    /*                                  unsigned int *hs_pdayp_ins_cnt_tujo_buf,  */
    /*                                 unsigned int *hs_pdayp_ins_cnt_kiakan_buf) */
    /*  【説明】                                                                  */
    /*     顧客退会したポイントを失効する。                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser              TS利用可能ポイント情報検索用      */
    /*      unsigned int *ts_ryokp_upd_cnt_buf  TS利用可能ポイント情報更新件数    */
    /*      unsigned int *hs_pdayp_ins_cnt_tujo_buf                               */
    /*                             HSポイント日別情報（通常ポイント失効）更新件数 */
    /*      unsigned int *hs_pdayp_ins_cnt_kikan_buf                              */
    /*                         HSポイント日別情報（期間限定ポイント失効）更新件数 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             55   :  ORA-1555発生                                           */
    /* *****************************************************************************/
    public int  cmBTktskB_shikko(TS_SERCH ts_ser,
                                 ItemDto ts_ryokp_upd_cnt_buf,
                                 ItemDto hs_pdayp_ins_cnt_tujo_buf,
                                 ItemDto hs_pdayp_ins_cnt_kikan_buf)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_shikko処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto               wk_kokyaku_no = new StringDto(16);       /* 顧客番号                   */
        int          rtn_cd;                  /* 戻り値                     */
        IntegerDto                rtn_status = new IntegerDto();              /* 関数結果ステータス         */
        StringDto               buff = new StringDto(160);               /* バッファ                   */

        /* ポイント失効退会顧客抽出 */
        if (sql_flg == DEF_OFF){
            /* 初回時 */
            cmBTktskB_setSql(ts_ser);
        }else{
            /* ORA-1555発生時 */
            cmBTktskB_setSql_ora1555(ts_ser);
        }

        SqlstmDto sqlca= sqlcaManager.get("CUR_SQL01");
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat1 from :h_str_sql;
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskiB_getInfo *** 動的SQL 解析NG = %d\n",
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
//        EXEC SQL OPEN CUR_SQL01;
        sqlca.open();

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TS利用可能ポイント情報", C_PRGNAME, 0, 0);
            return C_const_NG;
        }
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_shikko *** TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        while(true){
//            memset(&h_ts, 0x00, sizeof(h_ts));

            /* カーソルフェッチ */
//            EXEC SQL FETCH CUR_SQL01
//            INTO :h_ts.vr_kokyaku_no;        /* 顧客番号              */
            sqlca.fetch();
            sqlca.recData(h_ts.vr_kokyaku_no);

            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND){
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_SQL01;
//                sqlca.close();
                sqlcaManager.close("CUR_SQL01");
                break; /* ループ抜ける */
            }
            /* スナップショットが古すぎる場合 */
            else if(sqlca.sqlcode == C_const_Ora_SNAP_OLD){
                /* スナップショットが古すぎる場合 */
                sprintf(buff,
                        "DBエラー(FETCH) STATUS=%d(TBL:TS利用可能ポイント情報 )"
                        , C_const_Ora_SNAP_OLD);
                APLOG_WT("700", 0, null, buff, 0, 0, 0, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTktskB_shikko *** %s\n", "ORA-1555発生");
                /*------------------------------------------------------------*/
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_SQL01;
                sqlcaManager.close("CUR_SQL01");
                /* ORA-1555発生フラグON */
                sql_flg = DEF_ON;
                return C_const_SNAP_OLD;
            }
            /* スナップショットが古すぎる、データ無し以外エラーの場合 */
            else if(sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    sqlca.sqlcode != C_const_Ora_SNAP_OLD) {
                APLOG_WT("904", 0, null, "FETCH(CUR_SQL01)",
                        sqlca.sqlcode, "TS利用可能ポイント情報",
                        C_PRGNAME, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_SQL01;
                sqlcaManager.close("CUR_SQL01");
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTktskB_shikko *** %s\n", "フェッチエラー");
                C_DbgEnd("cmBTktskB_shikko処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG; /* 処理を異常終了する */
            }

            /* 顧客番号（数値）設定 */
            h_kokyaku_no.arr =  atol(h_ts.vr_kokyaku_no);

            /*顧客情報ロック */
            sprintf(wk_kokyaku_no, "%15d", h_kokyaku_no);
            rtn_cd = C_KdataLock(wk_kokyaku_no, "1", rtn_status);
            if (rtn_cd == C_const_NG){
                APLOG_WT( "903", 0, null, "C_KdataLock(1)",
                        rtn_cd, rtn_status, 0, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTktskB_shikko *** ロックエラー顧客=[%s]\n",
                        wk_kokyaku_no);
                C_DbgEnd("cmBTktskB_shikko処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------*/
//                EXEC SQL CLOSE CUR_SQL01; /* カーソルクローズ       */
                sqlcaManager.close("CUR_SQL01");
                return C_const_NG;
            }
            /* ポイント失効データの取得 */
            rtn_cd = cmBTktskB_getTSriyo(ts_ser);
            if (rtn_cd != C_const_OK){
//                EXEC SQL CLOSE CUR_SQL01; /* カーソルクローズ       */
                sqlcaManager.close("CUR_SQL01");
                return C_const_NG;
            }

            /* 対象年度の失効状態を確認 */
            cmBTktskB_check_tujo_point(ts_ser.ex_year, ts_ser.this_year,
                    ts_ser.next_year);

            /* 対象月の失効状態を確認 */
            cmBTktskB_check_kikan_point();

            /* ポイント失効処理 */
            rtn_cd = cmBTktskB_pointLost(ts_ser, ts_ryokp_upd_cnt_buf,
                    hs_pdayp_ins_cnt_tujo_buf, hs_pdayp_ins_cnt_kikan_buf);
            if (rtn_cd != C_const_OK){
//                EXEC SQL CLOSE CUR_SQL01;/* カーソルクローズ */

                sqlcaManager.close("CUR_SQL01");
                /*-----------------------------------------------------*/
                C_DbgEnd("cmBTktskB_shikko処理", C_const_NG, 0, 0);
                /*-----------------------------------------------------*/
                return C_const_NG;
            }
//            EXEC SQL COMMIT WORK;           /* コミット  */
            sqlca.commit();
        }/* ループ終了 */

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL01;

        sqlcaManager.close("CUR_SQL01");

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_shikko処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return C_const_OK;
        /*--- cmBTktskB_shikko処理 ------------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_setSql                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void        cmBTktskB_setSql(struct TS_SERCH ts_ser_sql)        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント失効対象の顧客を抽出するＳＱＬを作成する。                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /* *****************************************************************************/
    public void  cmBTktskB_setSql(TS_SERCH ts_ser_sql)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_setSql処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/

        /* ポイント失効退会顧客抽出 */
        sprintf(h_str_sql,
                "SELECT  "+
                        "    A.顧客番号  "+
                        "FROM (  "+
                        "  SELECT  TS.顧客番号 "+
                        "    FROM TS利用可能ポイント情報 TS, "+
                        "      ( "+
                        "        SELECT A.顧客番号, A.企業コード, "+
                        /* 2022/11/28 MCCM初版 ADD START */
/*        "          CASE WHEN A.企業コード = 1020 THEN A.退会年月日 "
        "          WHEN A.企業コード = 1040 THEN A.退会年月日 "
        "          WHEN A.企業コード = 3020 THEN A.退会年月日 "
        "          WHEN A.企業コード = 3040 THEN A.退会年月日 "
        "          ELSE A.仮退会年月日 END AS 退会日, "
*/
                        "          A.退会年月日 AS 退会日, "+
                        /* 2022/11/28 MCCM初版 ADD END */
                        "          ROW_NUMBER() OVER ( "+
                        "            PARTITION BY A.顧客番号 "+
                        /* 2022/11/28 MCCM初版 MOD START */
/*
        "            ORDER BY CASE WHEN A.企業コード = 1020 THEN A.退会年月日 "
        "            WHEN A.企業コード = 1040 THEN A.退会年月日 "
        "            ELSE A.仮退会年月日 END DESC, 企業コード) AS G_ROW "
*/
                        " ORDER BY A.退会年月日 DESC, 企業コード) AS G_ROW "+
                        /* 2022/11/28 MCCM初版 MOD END */
                        "        FROM MM顧客企業別属性情報 A "+
                        "      ) B "+
                        "    WHERE B.G_ROW = 1 "+
                        "    AND B.退会日 = %d "+
                        "    AND not exists (select 1 from MSカード情報 C "+
                        /* 2022/11/28 MCCM初版 MOD START */
                        /*      "       where C.顧客番号 = B.顧客番号 and C.カードステータス in (0,7)) " */
                        "       where C.顧客番号 = B.顧客番号 and C.カードステータス in (0,1,7)) "+
                        /* 2022/11/28 MCCM初版 MOD END */
                        "    AND TS.顧客番号 = B.顧客番号 "+
                        "    AND (TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   )"+
                        "     ) A "
                , gh_kijun_date, ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd,
                ts_ser_sql.next_year_cd, ts_ser_sql.month01, ts_ser_sql.month02,
                ts_ser_sql.month03, ts_ser_sql.month04);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_setSql *** 動的ＳＱＬ=[%s]\n", h_str_sql);
        C_DbgEnd("cmBTktskB_setSql処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTktskB_setSql処理 ---------------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_setSql_ora1555                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTktskB_setSql_ora1555(struct TS_SERCH ts_ser_sql)     */
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
    /* *****************************************************************************/
    public void  cmBTktskB_setSql_ora1555( TS_SERCH ts_ser_sql)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_setSql_ora1555処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
         int wk_sysdate;                           /* システム日付        */

        wk_sysdate = atoi(gl_sysdate);

        /* ポイント失効退会顧客抽出 */
        sprintf(h_str_sql,
                "SELECT  "+
                        "    A.顧客番号  "+
                        "FROM (  "+
                        "  SELECT TS.顧客番号 "+
                        "    FROM TS利用可能ポイント情報 TS, "+
                        "      ( "+
                        "        SELECT A.顧客番号, A.企業コード, "+
                        /* 2022/11/28 MCCM初版 ADD START */
/*
        "          CASE WHEN A.企業コード = 1020 THEN A.退会年月日 "
        "          WHEN A.企業コード = 1040 THEN A.退会年月日 "
        "          ELSE A.仮退会年月日 END AS 退会日, "
*/
                        "          A.退会年月日 AS 退会日, "+
                        /* 2022/11/28 MCCM初版 ADD END */
                        "          ROW_NUMBER() OVER ( "+
                        "            PARTITION BY A.顧客番号 "+
                        /* 2022/11/28 MCCM初版 ADD START */
/*
        "            ORDER BY CASE WHEN A.企業コード = 1020 THEN A.退会年月日 "
        "            WHEN A.企業コード = 1040 THEN A.退会年月日 "
        "          WHEN A.企業コード = 3020 THEN A.退会年月日 "
        "          WHEN A.企業コード = 3040 THEN A.退会年月日 "
        "            ELSE A.仮退会年月日 END DESC, 企業コード) AS G_ROW "
*/
                        /* 2022/11/28 MCCM初版 ADD END */
                        "           ORDER BY A.退会年月日 DESC, 企業コード) AS G_ROW "+
                        "        FROM MM顧客企業別属性情報 A "+
                        "      ) B "+
                        "    WHERE B.G_ROW = 1 "+
                        "    AND B.退会日 = %d "+
                        "    AND not exists (select 1 from MSカード情報 C "+
                        /* 2022/11/28 MCCM初版 MOD START */
                        /*      "       where C.顧客番号 = B.顧客番号 and C.カードステータス in (0,7)) " */
                        "       where C.顧客番号 = B.顧客番号 and C.カードステータス in (0,1,7)) "+
                        /* 2022/11/28 MCCM初版 MOD END */
                        "    AND TS.顧客番号 = B.顧客番号 "+
                        "    AND (TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能通常Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   TS.利用可能期間限定Ｐ%s <> 0   )"+
                        "    AND NOT (TS.最終更新プログラムＩＤ = '%s' "+
                        "             AND TS.最終更新日= %d)"+
                        "     ) A "
                , gh_kijun_date, ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd,
                ts_ser_sql.next_year_cd, ts_ser_sql.month01, ts_ser_sql.month02,
                ts_ser_sql.month03, ts_ser_sql.month04, Program_Name, wk_sysdate);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_setSql_ora1555 *** 動的ＳＱＬ=[%s]\n", h_str_sql);
        C_DbgEnd("cmBTktskB_setSql_ora1555処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTktskB_setSql_ora1555処理 -------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getTSriyo                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int    cmBTktskB_getTSriyo(struct TS_SERCH ts_ser_sql)   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TS利用可能ポイント情報ＴＢＬから                                       */
    /*     ポイント失効データを取得する。                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_getTSriyo( TS_SERCH ts_ser_sql)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getTSriyo処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto               buff = new StringDto(2048);                  /* バッファ               */
        int wk_month;             /* バッチ処理月                  */
        int wk_year;                      /* 年                    */
        int getsumatsu = 0;                                               /* 月末 */

        /* 2022/11/29 MCCM初版 MOD START */
        /*-----------------------------------------------*/
        /*  変数初期化                                   */
        /*-----------------------------------------------*/
        gl_meisai_su = 0;
        gl_kikan_meisai_su = 0;
        gl_kobai_kei = 0;
        gl_hikobai_kei = 0;
        gl_sonota_kei = 0;
        gl_kikan_kobai_kei = 0;
        gl_kikan_hikobai_kei = 0;
        gl_kikan_sonota_kei = 0;
        for (int i = 0 ; i <hsuchiwake_tsujyo_insert.length;i++){
            hsuchiwake_tsujyo_insert[i]=new HSUCHIWAKE_INSERT();
        }
        for (int i = 0 ; i <hsuchiwake_tsujyo_insert.length;i++){
            hsuchiwake_kikan_insert[i]=new HSUCHIWAKE_INSERT();
        }

//        memset(hsuchiwake_tsujyo_insert, 0x00, sizeof(hsuchiwake_tsujyo_insert));
//        memset(hsuchiwake_kikan_insert, 0x00, sizeof(hsuchiwake_kikan_insert));
        wk_month = atoi(gl_bat_date_mm);
        wk_year = atoi(gl_bat_date_yyyy);
        /* 2022/11/29 MCCM初版 MOD END */

        /* ポイント失効退会顧客抽出 */
        sprintf(h_str_sql,
                "SELECT  /*+ INDEX(TS利用可能ポイント情報 PKTSPTPTRY00) */ "+
                        "    入会企業コード,  "+
                        "    入会店舗,  "+
                        "    発券企業コード,  "+
                        "    発券店舗,  "+
                        "    利用可能通常Ｐ%s,  "+
                        "    利用可能通常Ｐ%s,  "+
                        "    利用可能通常Ｐ%s,  "+
                        "    通常Ｐ失効フラグ,  "+
                        "    利用可能期間限定Ｐ%s,  "+
                        "    利用可能期間限定Ｐ%s,  "+
                        "    利用可能期間限定Ｐ%s,  "+
                        "    利用可能期間限定Ｐ%s,  "+
                        "    利用可能期間限定Ｐ%s,  "+
                        /* 2022/11/29 MCCM初版 MOD START */
                        /*      "    期間限定Ｐ失効フラグ  " */
                        "    期間限定Ｐ失効フラグ, "+
                        "    利用可能通常購買Ｐ%s,  "+
                        "    利用可能通常購買Ｐ%s,  "+
                        "    利用可能通常購買Ｐ%s,  "+
                        "    利用可能通常非購買Ｐ%s,  "+
                        "    利用可能通常非購買Ｐ%s,  "+
                        "    利用可能通常非購買Ｐ%s,  "+
                        "    利用可能通常その他Ｐ%s,  "+
                        "    利用可能通常その他Ｐ%s,  "+
                        "    利用可能通常その他Ｐ%s,  "+
                        "    利用可能期間限定購買Ｐ%s,  "+
                        "    利用可能期間限定購買Ｐ%s,  "+
                        "    利用可能期間限定購買Ｐ%s,  "+
                        "    利用可能期間限定購買Ｐ%s,  "+
                        "    利用可能期間限定購買Ｐ%s,  "+
                        "    利用可能期間限定非購買Ｐ%s,  "+
                        "    利用可能期間限定非購買Ｐ%s,  "+
                        "    利用可能期間限定非購買Ｐ%s,  "+
                        "    利用可能期間限定非購買Ｐ%s,  "+
                        "    利用可能期間限定非購買Ｐ%s,  "+
                        "    利用可能期間限定その他Ｐ%s,  "+
                        "    利用可能期間限定その他Ｐ%s,  "+
                        "    利用可能期間限定その他Ｐ%s,  "+
                        "    利用可能期間限定その他Ｐ%s,  "+
                        "    利用可能期間限定その他Ｐ%s,  "+
                        "    入会会社コードＭＣＣ, "+
                        "    入会店舗ＭＣＣ "+
                        /* 2022/11/29 MCCM初版 MOD END */
                        "FROM  TS利用可能ポイント情報 "+
                        "WHERE 顧客番号= ? "+
                        "AND ( 利用可能通常Ｐ%s <> 0   "+
                        "    OR   利用可能通常Ｐ%s <> 0   "+
                        "    OR   利用可能通常Ｐ%s <> 0   "+
                        "    OR   利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   利用可能期間限定Ｐ%s <> 0   "+
                        "    OR   利用可能期間限定Ｐ%s <> 0   )"
                ,ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd,
                ts_ser_sql.next_year_cd, ts_ser_sql.month01, ts_ser_sql.month02,
                ts_ser_sql.month03, ts_ser_sql.month04, ts_ser_sql.month05,
                /* 2022/11/29 MCCM初版 ADD START */
                ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd, ts_ser_sql.next_year_cd,
                ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd, ts_ser_sql.next_year_cd,
                ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd, ts_ser_sql.next_year_cd,
                ts_ser_sql.month01, ts_ser_sql.month02,ts_ser_sql.month03, ts_ser_sql.month04, ts_ser_sql.month05,
                ts_ser_sql.month01, ts_ser_sql.month02,ts_ser_sql.month03, ts_ser_sql.month04, ts_ser_sql.month05,
                ts_ser_sql.month01, ts_ser_sql.month02,ts_ser_sql.month03, ts_ser_sql.month04, ts_ser_sql.month05,
                /* 2022/11/29 MCCM初版 ADD END */
                ts_ser_sql.ex_year_cd, ts_ser_sql.this_year_cd,
                ts_ser_sql.next_year_cd, ts_ser_sql.month01, ts_ser_sql.month02,
                ts_ser_sql.month03, ts_ser_sql.month04);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_geTSriyo *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/

        SqlstmDto sqlca = sqlcaManager.get("CUR_SQL02");
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat2 from :h_str_sql;
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_getTSriyo *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTktskB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL02 CURSOR FOR sql_stat2;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL02 USING :h_kokyaku_no;
        sqlca.open(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getTSriyo *** "+
                "TS利用可能ポイント情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("904", 0, null, "OPEN CURSOR(CUR_SQL02)",
                    sqlca.sqlcode, "TS利用可能ポイント情報情報",
                    C_PRGNAME, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_getTSriyo *** %s\n","カーソルエラー");
            C_DbgEnd("cmBTktskB_getTSriyo処理", 0, 0, 0);
            /*------------------------------------------------------------*/

            return C_const_NG;
        }
        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL02
//        INTO :h_ts.nyukai_kigyo_cd,        /* 入会企業コード                     */
//          :h_ts.nyukai_tenpo,           /* 入会店舗                           */
//          :h_ts.hakken_kigyo_cd,        /* 発券企業コード                     */
//          :h_ts.hakken_tenpo,           /* 発券店舗                           */
//          :h_ts.zennen_p,               /* 利用可能通常Ｐ前年度               */
//          :h_ts.tonen_p,                /* 利用可能通常Ｐ当年度               */
//          :h_ts.yokunen_p,              /* 利用可能通常Ｐ翌年度               */
//          :h_ts.tujo_p_shiko_flg,       /* 通常Ｐ失効フラグ                   */
//          :h_ts.kikan_p01,              /* 利用可能期間限定Ｐ当月             */
//          :h_ts.kikan_p02,              /* 利用可能期間限定Ｐ当月＋１         */
//          :h_ts.kikan_p03,              /* 利用可能期間限定Ｐ当月＋２         */
//          :h_ts.kikan_p04,              /* 利用可能期間限定Ｐ当月＋３         */
//          :h_ts.kikan_p05,              /* 利用可能期間限定Ｐ当月＋４         */
//        /* 2022/11/29 MCCM初版 MOD START */
//        /*        :h_ts.kikan_p_shikko_flg; */  /* 期間限定Ｐ失効フラグ               */
//          :h_ts.kikan_p_shikko_flg,     /* 期間限定Ｐ失効フラグ               */
//          :h_ts.zennen_kobai_p,         /* 利用可能通常購買Ｐ前年度           */
//          :h_ts.tonen_kobai_p,          /* 利用可能通常購買Ｐ当年度           */
//          :h_ts.yokunen_kobai_p,        /* 利用可能通常購買Ｐ翌年度           */
//          :h_ts.zennen_hikobai_p,       /* 利用可能通常非購買Ｐ前年度         */
//          :h_ts.tonen_hikobai_p,        /* 利用可能通常非購買Ｐ当年度         */
//          :h_ts.yokunen_hikobai_p,      /* 利用可能通常非購買Ｐ翌年度         */
//          :h_ts.zennen_sonota_p,        /* 利用可能通常その他Ｐ前年度         */
//          :h_ts.tonen_sonota_p,         /* 利用可能通常その他Ｐ当年度         */
//          :h_ts.yokunen_sonota_p,       /* 利用可能通常その他Ｐ翌年度         */
//          :h_ts.kikan_kobai_p01,        /* 利用可能期間限定購買Ｐ当月         */
//          :h_ts.kikan_kobai_p02,        /* 利用可能期間限定購買Ｐ当月         */
//          :h_ts.kikan_kobai_p03,        /* 利用可能期間限定購買Ｐ当月         */
//          :h_ts.kikan_kobai_p04,        /* 利用可能期間限定購買Ｐ当月         */
//          :h_ts.kikan_kobai_p05,        /* 利用可能期間限定購買Ｐ当月         */
//          :h_ts.kikan_hikobai_p01,      /* 利用可能期間限定非購買Ｐ当月       */
//          :h_ts.kikan_hikobai_p02,      /* 利用可能期間限定非購買Ｐ当月       */
//          :h_ts.kikan_hikobai_p03,      /* 利用可能期間限定非購買Ｐ当月       */
//          :h_ts.kikan_hikobai_p04,      /* 利用可能期間限定非購買Ｐ当月       */
//          :h_ts.kikan_hikobai_p05,      /* 利用可能期間限定非購買Ｐ当月       */
//          :h_ts.kikan_sonota_p01,       /* 利用可能期間限定その他Ｐ当月       */
//          :h_ts.kikan_sonota_p02,       /* 利用可能期間限定その他Ｐ当月       */
//          :h_ts.kikan_sonota_p03,       /* 利用可能期間限定その他Ｐ当月       */
//          :h_ts.kikan_sonota_p04,       /* 利用可能期間限定その他Ｐ当月       */
//          :h_ts.kikan_sonota_p05,       /* 利用可能期間限定その他Ｐ当月       */
//          :h_ts.nyukai_kigyo_cd_mcc,    /* 入会会社コードＭＣＣ               */
//          :h_ts.nyukai_tenpo_mcc;       /* 入会店舗ＭＣＣ                     */
        sqlca.fetch();
        sqlca.recData(h_ts.nyukai_kigyo_cd,        /* 入会企業コード                     */
                h_ts.nyukai_tenpo,           /* 入会店舗                           */
                h_ts.hakken_kigyo_cd,        /* 発券企業コード                     */
                h_ts.hakken_tenpo,           /* 発券店舗                           */
                h_ts.zennen_p,               /* 利用可能通常Ｐ前年度               */
                h_ts.tonen_p,                /* 利用可能通常Ｐ当年度               */
                h_ts.yokunen_p,              /* 利用可能通常Ｐ翌年度               */
                h_ts.tujo_p_shiko_flg,       /* 通常Ｐ失効フラグ                   */
                h_ts.kikan_p01,              /* 利用可能期間限定Ｐ当月             */
                h_ts.kikan_p02,              /* 利用可能期間限定Ｐ当月＋１         */
                h_ts.kikan_p03,              /* 利用可能期間限定Ｐ当月＋２         */
                h_ts.kikan_p04,              /* 利用可能期間限定Ｐ当月＋３         */
                h_ts.kikan_p05,              /* 利用可能期間限定Ｐ当月＋４         */
                /* 2022/11/29 MCCM初版 MOD START */
                /*        :h_ts.kikan_p_shikko_flg; */  /* 期間限定Ｐ失効フラグ               */
                h_ts.kikan_p_shikko_flg,     /* 期間限定Ｐ失効フラグ               */
                h_ts.zennen_kobai_p,         /* 利用可能通常購買Ｐ前年度           */
                h_ts.tonen_kobai_p,          /* 利用可能通常購買Ｐ当年度           */
                h_ts.yokunen_kobai_p,        /* 利用可能通常購買Ｐ翌年度           */
                h_ts.zennen_hikobai_p,       /* 利用可能通常非購買Ｐ前年度         */
                h_ts.tonen_hikobai_p,        /* 利用可能通常非購買Ｐ当年度         */
                h_ts.yokunen_hikobai_p,      /* 利用可能通常非購買Ｐ翌年度         */
                h_ts.zennen_sonota_p,        /* 利用可能通常その他Ｐ前年度         */
                h_ts.tonen_sonota_p,         /* 利用可能通常その他Ｐ当年度         */
                h_ts.yokunen_sonota_p,       /* 利用可能通常その他Ｐ翌年度         */
                h_ts.kikan_kobai_p01,        /* 利用可能期間限定購買Ｐ当月         */
                h_ts.kikan_kobai_p02,        /* 利用可能期間限定購買Ｐ当月         */
                h_ts.kikan_kobai_p03,        /* 利用可能期間限定購買Ｐ当月         */
                h_ts.kikan_kobai_p04,        /* 利用可能期間限定購買Ｐ当月         */
                h_ts.kikan_kobai_p05,        /* 利用可能期間限定購買Ｐ当月         */
                h_ts.kikan_hikobai_p01,      /* 利用可能期間限定非購買Ｐ当月       */
                h_ts.kikan_hikobai_p02,      /* 利用可能期間限定非購買Ｐ当月       */
                h_ts.kikan_hikobai_p03,      /* 利用可能期間限定非購買Ｐ当月       */
                h_ts.kikan_hikobai_p04,      /* 利用可能期間限定非購買Ｐ当月       */
                h_ts.kikan_hikobai_p05,      /* 利用可能期間限定非購買Ｐ当月       */
                h_ts.kikan_sonota_p01,       /* 利用可能期間限定その他Ｐ当月       */
                h_ts.kikan_sonota_p02,       /* 利用可能期間限定その他Ｐ当月       */
                h_ts.kikan_sonota_p03,       /* 利用可能期間限定その他Ｐ当月       */
                h_ts.kikan_sonota_p04,       /* 利用可能期間限定その他Ｐ当月       */
                h_ts.kikan_sonota_p05,       /* 利用可能期間限定その他Ｐ当月       */
                h_ts.nyukai_kigyo_cd_mcc,    /* 入会会社コードＭＣＣ               */
                h_ts.nyukai_tenpo_mcc      /* 入会店舗ＭＣＣ                     */);
        /* 2022/11/29 MCCM初版 MOD END */

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            APLOG_WT("904", 0, null, "FETCH(CUR_SQL02)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    C_PRGNAME, 0, 0);
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL02;
//            sqlca.close();
            sqlcaManager.close("CUR_SQL02");
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_getTSriyo *** %s\n","フェッチエラー");
            C_DbgEnd("cmBTktskB_getTSriyo 処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL02;
        sqlcaManager.close("CUR_SQL02");

        /* 2022/11/29 MCCM初版 ADD START */
        gl_kobai_kei = h_ts.zennen_kobai_p.floatVal() + h_ts.tonen_kobai_p.floatVal() + h_ts.yokunen_kobai_p.floatVal();
        gl_hikobai_kei = h_ts.zennen_hikobai_p.floatVal() + h_ts.tonen_hikobai_p.floatVal() + h_ts.yokunen_hikobai_p.floatVal();
        gl_sonota_kei = h_ts.zennen_sonota_p.floatVal() + h_ts.tonen_sonota_p.floatVal() + h_ts.yokunen_sonota_p.floatVal();
        gl_kikan_kobai_kei = h_ts.kikan_kobai_p01.floatVal() + h_ts.kikan_kobai_p02.floatVal() + h_ts.kikan_kobai_p03.floatVal()
                + h_ts.kikan_kobai_p04.floatVal() + h_ts.kikan_kobai_p05.floatVal();
        gl_kikan_hikobai_kei = h_ts.kikan_hikobai_p01.floatVal() + h_ts.kikan_hikobai_p02.floatVal() + h_ts.kikan_hikobai_p03.floatVal()
                + h_ts.kikan_hikobai_p04.floatVal() + h_ts.kikan_hikobai_p05.floatVal();
        gl_kikan_sonota_kei = h_ts.kikan_sonota_p01.floatVal() + h_ts.kikan_sonota_p02.floatVal() + h_ts.kikan_sonota_p03.floatVal()
                + h_ts.kikan_sonota_p04.floatVal() + h_ts.kikan_sonota_p05.floatVal();

        /* 購買Ｐ */
        if (h_ts.zennen_kobai_p.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.zennen_kobai_p.longVal();                              /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.ex_year.intVal()+2)*10000+331;              /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.tonen_kobai_p.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.tonen_kobai_p.longVal();                               /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.this_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.yokunen_kobai_p.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.yokunen_kobai_p.longVal();                             /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.next_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }

        /* 非購買Ｐ */
        if (h_ts.zennen_hikobai_p.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.zennen_hikobai_p.longVal();                            /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.ex_year.intVal()+2)*10000+331;              /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.tonen_hikobai_p.intVal() != 0 ){     /* 非購買ＰＰ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.tonen_hikobai_p.longVal();                               /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.this_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.yokunen_hikobai_p.intVal() != 0 ){     /* 非購買ＰＰ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.yokunen_hikobai_p.longVal();                             /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.next_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }

        /* その他Ｐ */
        if (h_ts.zennen_sonota_p.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.zennen_sonota_p.longVal();                            /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.ex_year.intVal()+2)*10000+331;              /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.tonen_sonota_p.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.tonen_sonota_p.longVal();                               /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.this_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }
        if (h_ts.yokunen_sonota_p.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].meisai_no.arr = gl_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].riyo_point.arr = h_ts.yokunen_sonota_p.longVal();                             /* 利用ポイント               */
            hsuchiwake_tsujyo_insert[gl_meisai_su].tujo_kikan_gentei_kbn.arr = 1;                                     /* 通常期間限定区分           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].point_yukokigen.arr = (ts_ser_sql.next_year.intVal()+2)*10000+331;            /* ポイント有効期限           */
            hsuchiwake_tsujyo_insert[gl_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_meisai_su++;
        }



        /* 期間限定購買 */
        if (h_ts.kikan_kobai_p01.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_kobai_p01.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + wk_month * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_kobai_p02.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_kobai_p02.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+1);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+1) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_kobai_p03.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_kobai_p03.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+2);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+2) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_kobai_p04.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_kobai_p04.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+3);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+3) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_kobai_p05.intVal() != 0 ){     /* 購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_kobai_p05.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+4);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+4) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 1;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }


        /* 期間限定非購買 */
        if (h_ts.kikan_hikobai_p01.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_hikobai_p01.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + wk_month * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_hikobai_p02.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_hikobai_p02.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+1);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+1) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_hikobai_p03.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_hikobai_p03.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+2);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+2) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_hikobai_p04.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_hikobai_p04.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+3);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+3) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_hikobai_p05.intVal() != 0 ){     /* 非購買Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_hikobai_p05.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+4);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+4) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 2;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }

        /* 期間限定その他 */
        if (h_ts.kikan_sonota_p01.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_sonota_p01.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + wk_month * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_sonota_p02.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_sonota_p02.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+1);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+1) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_sonota_p03.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_sonota_p03.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+2);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+2) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_sonota_p04.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_sonota_p04.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+3);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+3) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        if (h_ts.kikan_sonota_p05.intVal() != 0 ){     /* その他Ｐ計 <> 0 の場合、通常Ｐ明細数 +1   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].fuyo_riyo_kbn.arr = 2;                                             /* 付与利用区分               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].meisai_no.arr = gl_kikan_meisai_su + 1;                                      /* 明細番号                   */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_syubetsu.arr = 2;                                            /* ポイント種別               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].riyo_point.arr = h_ts.kikan_sonota_p05.longVal();                              /* 利用ポイント               */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].tujo_kikan_gentei_kbn.arr = 2;                                     /* 通常期間限定区分           */
            getsumatsu = daysInMonth(wk_year, wk_month+4);
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].point_yukokigen.arr = wk_year * 10000 + (wk_month+4) * 100 + getsumatsu;              /* ポイント有効期限           */
            hsuchiwake_kikan_insert[gl_kikan_meisai_su].kobai_kbn.arr = 3;                                                 /* 購買区分                   */
            gl_kikan_meisai_su++;
        }
        /* 2022/11/29 MCCM初版 ADD END */

        /*------------------------------------------------------------*/
        sprintf(buff,
                "入会企業コード:[%d],入会店舗:[%d],発券企業コード:[%d],発券店舗:[%d],"+
                "利用可能通常Ｐ%s:[%10.0f],利用可能通常Ｐ%s:[%10.0f],"+
                "利用可能通常Ｐ%s:[%10.0f],通常Ｐ失効フラグ:[%d],"+
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],"+
                "利用可能期間限定Ｐ%s:[%10.0f],利用可能期間限定Ｐ%s:[%10.0f],"+
                /* 2022/11/29 MCCM初版 MOD START */
                /*      "利用可能期間限定Ｐ%s:[%10.0f],期間限定Ｐ失効フラグ:[%d]", */
                "利用可能期間限定Ｐ%s:[%10.0f],期間限定Ｐ失効フラグ:[%d],"+
                "入会会社コードＭＣＣ:[%d],入会店舗ＭＣＣ:[%d]",
                /* 2022/11/29 MCCM初版 MOD END */

                h_ts.nyukai_kigyo_cd, h_ts.nyukai_tenpo, h_ts.hakken_kigyo_cd, h_ts.hakken_tenpo,
                ts_ser_sql.ex_year_cd, h_ts.zennen_p.floatVal(), ts_ser_sql.this_year_cd, h_ts.tonen_p.floatVal(),
                ts_ser_sql.next_year_cd, h_ts.yokunen_p.floatVal(), h_ts.tujo_p_shiko_flg,
                ts_ser_sql.month01, h_ts.kikan_p01.floatVal(), ts_ser_sql.month02, h_ts.kikan_p02.floatVal(),
                ts_ser_sql.month03, h_ts.kikan_p03.floatVal(), ts_ser_sql.month04, h_ts.kikan_p04.floatVal(),
                /* 2022/11/29 MCCM初版 MOD START */
                /*      h_ts.kikan_p05, h_ts.kikan_p_shikko_flg); */
                ts_ser_sql.month05, h_ts.kikan_p05.floatVal(), h_ts.kikan_p_shikko_flg,
                h_ts.nyukai_kigyo_cd_mcc,h_ts.nyukai_tenpo_mcc);
        /* 2022/11/29 MCCM初版 MOD END */

        C_DbgMsg("*** cmBTktskB_getTSriyo *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getTSriyo処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK ;
        /*--- cmBTktskB_getTSriyo処理 ------------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_check_tujo_point                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTktskB_check_tujo_point(unsigned int ex_year,          */
    /*                      unisgned int this_year, unsigned int next_year)       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     対象年度毎に失効状況を確認する                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*       unsigned short int ex_year    前年度                                 */
    /*       unsigned short int this_year  当年度                                 */
    /*       unsigned short int next_year  翌年度                                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 未失効                                                 */
    /*              1   ： 失効済み                                               */
    /*                                                                            */
    /* *****************************************************************************/
    public void  cmBTktskB_check_tujo_point(ItemDto ex_year,
                                            ItemDto this_year, ItemDto next_year)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_check_tujo_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int      rtn_cd;                  /* 戻り値                */

        /* 前年度の残ポイント確認 */
        if (h_ts.zennen_p.intVal() != 0) {
            /* 前年度の失効状況確認 */
            rtn_cd = cmBTktskB_check_tujo_bit(ex_year.intVal());
            if (rtn_cd == DEF_ON){
                h_ts.zennen_p.arr = 0;
            }
        }

        /* 当年度の残ポイント確認 */
        if (h_ts.tonen_p.intVal() != 0) {
            /* 当年度の失効状況確認 */
            rtn_cd = cmBTktskB_check_tujo_bit(this_year.intVal());
            if (rtn_cd == DEF_ON){
                h_ts.tonen_p.arr = 0;
            }
        }

        /* 翌年度の残ポイント確認 */
        if (h_ts.yokunen_p.intVal() != 0) {
            /* 翌年度の失効状況確認 */
            rtn_cd = cmBTktskB_check_tujo_bit(next_year.intVal());
            if (rtn_cd == DEF_ON){
                h_ts.yokunen_p.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_check_tujo_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTktskB_check_tujo_point処理 ------------------------------------------*/}
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_check_tujo_bit                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_check_tujo_bit(unsigned short int year)   */
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
    /* *****************************************************************************/

    public int  cmBTktskB_check_tujo_bit(int year)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_check_tujoBit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int shikko_flg;                  /* 失効状況確認用         */
        int wk_year_cd;                  /* 対象年度コード         */
        int check_flg;                   /* 対象年度フラグ         */
        int bit_kekka;                   /* bit計算結果            */
        StringDto               buff = new StringDto(160);                   /* バッファ               */

        /* 対象年度コード算出 */
        wk_year_cd = (year % 5 );

        /* 対象年度フラグ設定*/
        check_flg =( TUJO_BASE_BIT << (4 - wk_year_cd) | TUJO_CHENGE_BIT);

        /* 失効状況確認 */
        bit_kekka = h_ts.tujo_p_shiko_flg.intVal() & check_flg;
        if ( bit_kekka == check_flg){ /* 失効済み */
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                    "bit結果：[%#x]、失効済み",
                    year, h_ts.tujo_p_shiko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTktskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }else{ /* 未失効 */
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象年度：[%d]、通常Ｐ失効フラグ：[%#x]、対象年度のbit：[%#x]、" +
                    "bit結果：[%#x]、失効処理継続",
                    year,h_ts.tujo_p_shiko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTktskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/

            /* 対象年度の失効フラグを失効済みにする */
            h_ts.tujo_p_shiko_flg.arr=  h_ts.tujo_p_shiko_flg.intVal() | check_flg;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "通常Ｐ失効フラグ更新：[%#x]", h_ts.tujo_p_shiko_flg);
            C_DbgMsg("*** cmBTktskB_check_tujo_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_check_tujo_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTktskB_check_tujo_bit処理 -------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_check_kikan_point                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTktskB_check_kikan_point()                             */
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
    /* *****************************************************************************/
    public void  cmBTktskB_check_kikan_point()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_check_kikan_point処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int      wk_month;               /* バッチ処理月           */
        int      rtn_cd;                 /* 戻り値                 */

        wk_month = atoi(gl_bat_date_mm);

        /* バッチ処理月の残ポイント確認 */
        if (h_ts.kikan_p01.intVal() != 0) {
            /* バッチ処理月の失効状況確認 */
            rtn_cd = cmBTktskB_check_kikan_bit(wk_month);
            if (rtn_cd == DEF_ON){
                h_ts.kikan_p01.arr = 0;
            }
        }

        /* バッチ処理月+1の残ポイント確認 */
        if (h_ts.kikan_p02.intVal() != 0) {
            /* バッチ処理月+1の失効状況確認 */
            rtn_cd = cmBTktskB_check_kikan_bit(wk_month+1);
            if (rtn_cd == DEF_ON){
                h_ts.kikan_p02.arr = 0;
            }
        }

        /* バッチ処理月+2の残ポイント確認 */
        if (h_ts.kikan_p03.intVal() != 0) {
            /* バッチ処理月+2の失効状況確認 */
            rtn_cd = cmBTktskB_check_kikan_bit(wk_month+2);
            if (rtn_cd == DEF_ON){
                h_ts.kikan_p03.arr = 0;
            }
        }

        /* バッチ処理月+3の残ポイント確認 */
        if (h_ts.kikan_p04.intVal() != 0) {
            /* バッチ処理月+3の失効状況確認 */
            rtn_cd = cmBTktskB_check_kikan_bit(wk_month+3);
            if (rtn_cd == DEF_ON){
                h_ts.kikan_p04.arr = 0;
            }
        }

        /* バッチ処理月+4の残ポイント確認 */
        if (h_ts.kikan_p05.intVal() != 0) {
            /* バッチ処理月+4の失効状況確認 */
            rtn_cd = cmBTktskB_check_kikan_bit(wk_month+4);
            if (rtn_cd == DEF_ON){
                h_ts.kikan_p05.arr = 0;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_check_kikan_point処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTktskB_check_kikan_point処理 ----------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_check_kikan_bit                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_check_kikan_bit(unsigned short int month) */
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
    /* *****************************************************************************/
    public int  cmBTktskB_check_kikan_bit(int month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_check_kikan_bit処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int shikko_flg;                  /* 失効状況確認用         */
        int check_flg;                   /* bit演算用フラグ        */
        int bit_kekka;                   /* bit計算結果            */
        StringDto               buff = new StringDto(160);                   /* バッファ               */

        /* 対象月を算出する */
        month = (month % 12);
        if (month == 0){
            month = 12;
        }

        /* 失効確認フラグ設定*/
        check_flg =( KIKAN_BASE_BIT << (12 - month) | KIKAN_CHENGE_BIT);

        bit_kekka = h_ts.kikan_p_shikko_flg.intVal() & check_flg;

        /* 失効フラグの値確認 */
        if ( bit_kekka == check_flg){
            shikko_flg = DEF_ON;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、対象月のbit：[%#x]、" +
                    "bit結果：[%#x]、失効済み",
                    month, h_ts.kikan_p_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTktskB_check_kikan_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }else{
            shikko_flg = DEF_OFF;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "対象月：[%d]、期間限定Ｐ失効フラグ：[%#x]、対象月のbit：[%#x]、" +
                    "bit結果：[%#x]、失効処理継続",
                    month, h_ts.kikan_p_shikko_flg, check_flg, bit_kekka);
            C_DbgMsg("*** cmBTktskB_check_kikan_bit *** %s\n", buff);
            /*------------------------------------------------------------*/

            /* 対象月の失効フラグを失効済みにする */
            h_ts.kikan_p_shikko_flg.arr=  h_ts.kikan_p_shikko_flg.intVal() | check_flg;
            /*------------------------------------------------------------*/
            sprintf (buff,
                    "期間限定Ｐ失効フラグ更新：[%#x]", h_ts.kikan_p_shikko_flg);
            C_DbgMsg("*** cmBTktskB_check_kikan_bit *** %s\n", buff);
            /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_check_kikan_bit処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (shikko_flg);
        /*--- cmBTktskB_check_kikan_bit処理 ------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_pointLost                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_pointLost(struct TS_SERCH ts_ser          */
    /*                                  unsigned int *ts_ryokp_upd_cnt_buf,       */
    /*                                  unsigned int *hs_pdayp_ins_cnt_tujo_buf,  */
    /*                                 unsigned int *hs_pdayp_ins_cnt_kiakan_buf) */
    /*                                                                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     失効ポイント登録処理。                                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser_sql          TS利用可能ポイント情報検索用      */
    /*      unsigned int *ts_ryokp_upd_cnt_buf  TS利用可能ポイント情報更新件数    */
    /*      unsigned int *hs_pdayp_ins_cnt_tujo_buf                               */
    /*                             HSポイント日別情報（通常ポイント失効）更新件数 */
    /*      unsigned int *hs_pdayp_ins_cnt_kikan_buf                              */
    /*                         HSポイント日別情報（期間限定ポイント失効）更新件数 */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_pointLost( TS_SERCH ts_ser,
                                     ItemDto ts_ryokp_upd_cnt_buf,
                                     ItemDto hs_pdayp_ins_cnt_tujo_buf,
                                     ItemDto hs_pdayp_ins_cnt_kikan_buf)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_pointLost処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int     rtn_cd;                                 /* 戻り値                 */
        int     tujo_point_total;                       /* 通常Ｐ合算値           */
        int     kikan_point_total;                      /* 期間限定Ｐ合算値       */

        tujo_point_total = (h_ts.zennen_p.intVal() + h_ts.tonen_p.intVal() + h_ts.yokunen_p.intVal());
        kikan_point_total = (h_ts.kikan_p01.intVal() + h_ts.kikan_p02.intVal() + h_ts.kikan_p03.intVal() +
                h_ts.kikan_p04.intVal() + h_ts.kikan_p05.intVal());

        if ( tujo_point_total != 0 || kikan_point_total != 0){
            /* MSカード情報取得*/
            rtn_cd = cmBTktskB_getMSCard();
            if ( rtn_cd != C_const_OK ) {
                return C_const_NG;
            }

            /* TSポイント年別情報取得 */
            rtn_cd = cmBTktskB_getTSYear(ts_ser.month01.strVal());
            if ( rtn_cd != C_const_OK ) {
                return C_const_NG;
            }

            /* MS顧客制度報取得 */
            rtn_cd = cmBTktskB_getMSKokyaku(ts_ser.month01.strVal());
            if ( rtn_cd != C_const_OK ) {
                return C_const_NG;
            }

            /* MS家族制度報取得 */
            if ( h_kazoku_id.intVal() == 0 ) { /* 家族制度未入会 */
                h_kazoku_nenji_rank_cd .arr = 0;
                h_kazoku_getuji_rank_cd .arr= 0;
            } else                        { /* 家族制度入会済 */
                rtn_cd = cmBTktskB_getMSKazoku(ts_ser.month01.strVal());
                if ( rtn_cd != C_const_OK ) {
                    return C_const_NG;
                }
            }

            /* 2022/11/29 MCCM初版 ADD START */
            /* TSランク情報取得 */
            rtn_cd = cmBTktskB_getTSRank(ts_ser);
            if ( rtn_cd != C_const_OK ) {
                return C_const_NG;
            }
            /* 2022/11/29 MCCM初版 ADD END */

            /* システム日付取得 */
            rtn_cd = C_GetSysDateTime(gl_sysdate, gl_systime);
            if ( rtn_cd != C_const_OK){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** システム日付取得NG rtn=[%d]\n", rtn_cd);
                C_DbgEnd("*** main処理 ***", 0, 0, 0);
                /*------------------------------------------------------------*/
                APLOG_WT("903", 0, null, "C_GetSysDateTime",
                        rtn_cd, 0, 0, 0, 0);
                return C_const_NG;
            }

            /* TS利用可能ポイント情報の登録*/
            rtn_cd = cmBTktskB_updTS();
            if ( rtn_cd != C_const_OK ) {
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG;
            }

            /* TS利用可能ポイント情報更新件数アップ */
        ts_ryokp_upd_cnt_buf.arr = ts_ryokp_upd_cnt_buf.intVal()+ 1;

            /* HSポイント日別情報更新（通常ポイント失効） */
            if ( tujo_point_total != 0 ){
                w_seq = 0;
                rtn_cd = cmBTktskB_tujo_insertHS(ts_ser);
                if ( rtn_cd != C_const_OK ) {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
                /*HSポイント日別情報（通常ポイント失効）登録件数アップ */
            hs_pdayp_ins_cnt_tujo_buf.arr = hs_pdayp_ins_cnt_tujo_buf.intVal() + 1;
            }
            /* HSポイント日別情報更新（期間限定ポイント失効） */
            if ( kikan_point_total != 0 ){
                w_seq_2 = 0;
                rtn_cd = cmBTktskB_kikan_insertHS(ts_ser);
                if ( rtn_cd != C_const_OK ) {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
                /*HSポイント日別情報（期間限定ポイント失効）登録件数アップ */
            hs_pdayp_ins_cnt_kikan_buf.arr = hs_pdayp_ins_cnt_kikan_buf.intVal() + 1;
            }

            /* 2022/11/29 MCCM初版 ADD START */
            /* HSポイント日別内訳情報登録（通常ポイント） */
            if ( tujo_point_total != 0 ){
                rtn_cd = cmBTktskB_insertHSUchiwake(1, gl_meisai_su, hsuchiwake_tsujyo_insert);
                if ( rtn_cd != C_const_OK ) {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }
            /* HSポイント日別内訳情報登録（期間限定ポイント） */
            if ( kikan_point_total != 0 ){
                rtn_cd = cmBTktskB_insertHSUchiwake(2, gl_kikan_meisai_su, hsuchiwake_kikan_insert);
                if ( rtn_cd != C_const_OK ) {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }
            /* 2022/11/29 MCCM初版 ADD END */

            /* 2022/11/29 MCCM初版 MOD END */
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_pointLost処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTktskB_pointLost処理 -----------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getMSCard                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_getMScard()                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MSカード情報取得。                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*           なし                                                             */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_getMSCard()
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getMSCard処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    buff = new StringDto(512);                              /* バッファ               */
        long    kaiin_no;                  /* 会員番号               */

        /* 初期化 */
        memset(h_kaiin_no.arr, 0x00, sizeof(h_kaiin_no.arr));
        h_kaiin_no.len = 0;
        h_kigyo_cd.arr = 0;
        h_kyu_hansya_cd .arr= 0;

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
//        /* 2022/11/29 MCCM初版 MOD START */
///*                      AND     サービス種別 IN (1,2,3)
//                        AND     カードステータス IN (0,7,8)
//                        ORDER BY
//                        CASE    サービス種別
//                                    WHEN 1 THEN 1
//                                    WHEN 3 THEN 2
//                                    WHEN 2 THEN 3
//                                    ELSE 0
//                                END,
//                                    カードステータス ASC,*/
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
//        /* 2022/11/29 MCCM初版 MOD END */
//        発行年月日 DESC
//                        )MS
//        WHERE rownum  =  1;

        sqlca.sql=new StringDto("SELECT     MS.会員番号," +
                "     MS.企業コード," +
                "     MS.旧販社コード" +
                " FROM (SELECT    サービス種別," +
                "         会員番号," +
                "         企業コード," +
                "         旧販社コード" +
                "         FROM     MSカード情報" +
                "         WHERE    顧客番号 = ?" +
                " AND     サービス種別 IN (1,2,3,4,5)" +
                " AND     カードステータス IN (0,1,7,8)" +
                " ORDER BY" +
                " カードステータス ASC," +
                " CASE    サービス種別" +
                " WHEN 1 THEN 1" +
                " WHEN 4 THEN 2" +
                " WHEN 3 THEN 3" +
                " WHEN 2 THEN 4" +
                " ELSE 5" +
                " END ASC," +
                " 発行年月日 DESC" +
                " LIMIT 1         )MS" +
                " ");
        sqlca.restAndExecute(h_kokyaku_no);
        sqlca.fetch();
        sqlca.recData(h_kaiin_no,h_kigyo_cd,h_kyu_hansya_cd);
        /* エラーの場合処理を異常終了する */
        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MSカード情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSCard処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        kaiin_no = atol(h_kaiin_no.arr);
        sprintf(buff, "会員番号:[%d],企業コード:[%d],旧販社コード:[%d]",
                kaiin_no, h_kigyo_cd, h_kyu_hansya_cd);
        C_DbgMsg("*** cmBTktskB_getMScard *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getMSCard処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;              /* 処理終了                           */
        /*-----cmBTktskB_getMSCard Bottom---------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getTSyear                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_getTSYear(char *month)                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TSポイント年別情報取得。                                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char    *month              バッチ処理月（全角）                      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_getTSYear(String month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getTSYear処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_year;                      /* 年                    */
        StringDto    buff = new StringDto(512);                               /* バッファ              */

        /* 初期化 */
        h_nenkan_rankup_kingaku.arr = 0;
        h_gekkan_rankup_kingaku .arr= 0;
        h_kaiage_cnt .arr= 0;

        wk_year = atoi(gl_bat_date_yyyy);

        sprintf(h_str_sql,
                "SELECT 年間ランクＵＰ対象金額, " +
                "       月間ランクＵＰ対象金額%s, " +
                "       年間買上回数 " +
                "  FROM TSポイント年別情報%s " +
                " WHERE 年        =  %d " +
                "   AND 顧客番号  =  ?  ",
                month, gl_bat_date_yyyy, wk_year);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_geTSYear *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat3 from :h_str_sql;
        SqlstmDto sqlca =sqlcaManager.get("CUR_SQL03");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_getTSYear *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL03 CURSOR FOR sql_stat3;
                sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL03 USING :h_kokyaku_no;
        sqlca.open(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getTSYear *** TSポイント年別情報 CURSOR OPEN " +
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TSポイント年別情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL03
//        INTO :h_nenkan_rankup_kingaku, /* 年間ランクUP対象金額           */
//         :h_gekkan_rankup_kingaku, /* 月間ランクUP対象金額           */
//         :h_kaiage_cnt;            /* 年間買上回数                   */
        sqlca.fetch();
        sqlca.recData(h_nenkan_rankup_kingaku, h_gekkan_rankup_kingaku, h_kaiage_cnt);

        /* エラーの場合処理を異常終了する */
        /* 2022/01/05 TSポイント年別情報のデータなし追加 */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND)
        {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                "TSポイント年別情報",  buff, 0, 0);
//            EXEC SQL CLOSE CUR_SQL03; /* カーソルクローズ  */
//            sqlca.close();
            sqlcaManager.close("CUR_SQL03");
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getTSYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

//        EXEC SQL CLOSE CUR_SQL03; /* カーソルクローズ  */
        sqlcaManager.close("CUR_SQL03");
        /*------------------------------------------------------------*/
        sprintf(buff, "年間ランクＵＰ対象金額:[%10.0f]," +
                "月間ランクＵＰ対象金額%s:[%10.0f],年間買上回数:[%10.0f] ",
                h_nenkan_rankup_kingaku.floatVal(), month, h_gekkan_rankup_kingaku.floatVal(),
                h_kaiage_cnt.floatVal());
        C_DbgMsg("*** cmBTktskB_getTSYaer *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getTSYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTktskB_getTSYear Bottom---------------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getMSKokyaku                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_getMSKokyaku(char *month)                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MS顧客制度情報取得。                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char    *month             バッチ処理月（全角）                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int   cmBTktskB_getMSKokyaku(String month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getMSKokyaku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_year;                      /* 年                    */
        StringDto    buff = new StringDto(512);                               /* バッファ              */

        /* 初期化 */
        h_kazoku_id.arr = 0;
        h_nenji_rank_cd .arr= 0;
        h_getuji_rank_cd .arr= 0;

        wk_year = atoi(gl_bat_date_yyyy);

        sprintf(h_str_sql,
                "SELECT 年次ランクコード%s, " +
                "       月次ランクコード%s%s, " +
                "       家族ＩＤ " +
                "  FROM MS顧客制度情報 " +
                " WHERE 顧客番号  =  ? ",
                gl_bat_date_y, gl_bat_date_kbn, month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_geMSKokyaku*** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat4 from :h_str_sql;
        SqlstmDto sqlca= sqlcaManager.get("CUR_SQL04");
        sqlca.sql=h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_geMSKokyakur *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            /*------------------------------------------------------------*/
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL04 CURSOR FOR sql_stat4;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL04 USING :h_kokyaku_no;
        sqlca.open(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_geMSKokyaku *** MS顧客制度情報 CURSOR OPEN "+
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "MS顧客制度情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL04
//        INTO :h_nenji_rank_cd,         /* 年次ランクコード           */
//         :h_getuji_rank_cd,        /* 月次ランクコード           */
//         :h_kazoku_id;             /* 家族ID                     */
        sqlca.fetch();
        sqlca.recData(h_nenji_rank_cd, h_getuji_rank_cd, h_kazoku_id);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "MS顧客制度情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSKokyaku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL04;
            sqlcaManager.close("CUR_SQL04");
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],"+
                "月次ランクコード%s%s:[%d],家族ＩＤ:[%d]", gl_bat_date_y,
                h_nenji_rank_cd.intVal(), gl_bat_date_kbn, month, h_getuji_rank_cd.intVal(),
                h_kazoku_id.intVal());
        C_DbgMsg("*** cmBTktskB_geMSKokyaku *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getMSKokyakur処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL04;
        sqlcaManager.close("CUR_SQL04");
        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTktskB_getMSKokyakur Bottom-----------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getMSKazoku                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_getMSKazoku(char *month)                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MS家族制度情報取得。                                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char    *month          バッチ処理月（全角）                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_getMSKazoku(String month)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getMSKazoku処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_year;                      /* 年                    */
        StringDto    buff = new StringDto(512);                               /* バッファ              */

        /* 初期化 */
        h_kazoku_nenji_rank_cd.arr = 0;
        h_kazoku_getuji_rank_cd.arr = 0;

        wk_year = atoi(gl_bat_date_yyyy);

        sprintf(h_str_sql,
                "SELECT 年次ランクコード%s, "+
                "       月次ランクコード%s%s "+
                "  FROM MS家族制度情報 "+
                " WHERE 家族ＩＤ  =  ? ",
                gl_bat_date_y, gl_bat_date_kbn, month);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getMSKazoku *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat5 from :h_str_sql;
        SqlstmDto sqlca = sqlcaManager.get("CUR_SQL05");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL05 CURSOR FOR sql_stat5;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL05 USING :h_kazoku_id;
        sqlca.open(h_kazoku_id);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getMSKazoku *** MS家族制度情報 CURSOR OPEN "+
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "MS家族制度情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL05
//        INTO :h_kazoku_nenji_rank_cd,  /* 家族年次ランクコード           */
//         :h_kazoku_getuji_rank_cd; /* 家族月次ランクコード           */
        sqlca.fetch();
        sqlca.recData(h_kazoku_nenji_rank_cd, h_kazoku_getuji_rank_cd);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "MS家族制度情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getMSKazoku処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL05;
            sqlcaManager.close("CUR_SQL05");
            return C_const_NG;
        }
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL05;
        sqlcaManager.close("CUR_SQL05");

        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],月次ランクコード%s%s:[%d]",
                gl_bat_date_y, h_kazoku_nenji_rank_cd.intVal(), gl_bat_date_kbn, month,
                h_kazoku_getuji_rank_cd.intVal());
        C_DbgMsg("*** cmBTktskB_getMSKazoku *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getMSKazoku処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTktskB_getMSKazoku Bottom-------------------------------------------*/
    }
    /* 2022/11/29 MCCM初版 ADD START */
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_getTSRank                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_getTSRank(struct TS_SERCH ts_ser)         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     TSランク情報取得。                                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser          登録用データ格納構造体                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int  cmBTktskB_getTSRank( TS_SERCH ts_ser )
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_getTSRank処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    buff =new StringDto(512);                               /* バッファ              */

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
                gl_bat_nendo_y, gl_bat_nendo_kbn, ts_ser.month01,
                gl_bat_nendo_y, gl_bat_nendo_kbn, ts_ser.month01 );

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getTSRank *** 抽出SQL [%s]\n", h_str_sql);
        /*------------------------------------------------------------*/
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat6 from :h_str_sql;
        SqlstmDto sqlca=sqlcaManager.get("CUR_SQL06");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SQL06 CURSOR FOR sql_stat6;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_SQL06 USING :h_kokyaku_no;
        sqlca.open(h_kokyaku_no);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTktskB_getTSRank *** TSランク情報 CURSOR OPEN "+
                "sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------*/

        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_SQL06
//        INTO :h_rank_nenji_rank_cd,         /* TS年次ランクコード           */
//         :h_rank_getuji_rank_cd,        /* TS月次ランクコード           */
//         :h_rank_nenkan_rankup_kingaku, /* TSランク情報 年間ランクアップ対象金額 */
//         :h_rank_gekkan_rankup_kingaku; /* TSランク情報 月間ランクアップ対象金額 */
        sqlca.fetch();
        sqlca.recData(h_rank_nenji_rank_cd,
                h_rank_getuji_rank_cd,
                h_rank_nenkan_rankup_kingaku,
                h_rank_gekkan_rankup_kingaku);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK){
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "TSランク情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTktskB_getTSRank処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_SQL06;
//            sqlca.close();
            sqlcaManager.close("CUR_SQL06");
            return C_const_NG;
        }
        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_SQL06;
        sqlcaManager.close("CUR_SQL06");

        /*------------------------------------------------------------*/
        sprintf(buff, "年次ランクコード%s:[%d],"+
                "月次ランクコード%s%s:[%d],"+
                "年間ランクアップ対象金額%s:[%f]," +
                "月間ランクアップ対象金額%s%s:[%f],"+
                "顧客番号:[%d]",
                gl_bat_nendo_y, h_rank_nenji_rank_cd.intVal(),
                gl_bat_nendo_kbn, ts_ser.month01, h_rank_getuji_rank_cd.intVal(),
                gl_bat_nendo_y, h_rank_nenkan_rankup_kingaku.floatVal(),
                gl_bat_nendo_kbn, ts_ser.month01, h_rank_gekkan_rankup_kingaku.floatVal(),
                h_kokyaku_no.longVal());
        C_DbgMsg("*** cmBTktskB_getTSRank *** %s\n", buff);
        C_DbgEnd("cmBTktskB_getTSRank処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return ( C_const_OK );              /* 処理終了                           */
        /*-----cmBTktskB_getTSRank Bottom---------------------------------------------*/
    }
    /* 2022/11/29 MCCM初版 ADD END */

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_updTS                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  cmBTktskB_updTS ();                                         */
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
    /* *****************************************************************************/
    public int  cmBTktskB_updTS()
    {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_updTS処理");
        /*------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(256);                         /*  バッファ                      */

        h_sysdate.arr = atoi(gl_sysdate);

        /* 該当データを更新する */
//        EXEC SQL UPDATE TS利用可能ポイント情報
//        SET 最終更新日   = :h_sysdate,
//            最終更新日時 = SYSDATE,
//            最終更新プログラムＩＤ = :Program_Name,
//            通常Ｐ失効フラグ = :h_ts.tujo_p_shiko_flg,
//            期間限定Ｐ失効フラグ = :h_ts.kikan_p_shikko_flg
//        WHERE 顧客番号 = :h_kokyaku_no;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE TS利用可能ポイント情報 " +
                "SET 最終更新日 = ?," +
                "最終更新日時 = SYSDATE()," +
                "最終更新プログラムＩＤ = ?," +
                "通常Ｐ失効フラグ = ?," +
                "期間限定Ｐ失効フラグ = ? " +
                "WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.restAndExecute(h_sysdate, Program_Name, h_ts.tujo_p_shiko_flg, h_ts.kikan_p_shikko_flg, h_kokyaku_no);

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf( buff, "顧客番号=[%d]", h_kokyaku_no );
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", buff, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_updTSt *** TS利用可能情報更新 エラー%s\n", buff);
            C_DbgEnd(" cmBTktskB_updTS処理", 0, 0, 0);
            /*------------------------------------------------------------*/
            return C_const_NG;
        }
        /*------------------------------------------------------------*/
        sprintf(buff, "更新完了 顧客番号:[%d]、通常Ｐ失効フラグ:[%d]、"+
                "期間限定Ｐ失効フラグ:[%d]",
                h_kokyaku_no, h_ts.tujo_p_shiko_flg, h_ts.kikan_p_shikko_flg);
        C_DbgMsg("*** cmBTktskB_updTSt *** %s\n", buff);
        C_DbgEnd(" cmBTktskB_updTS処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK ;              /* 処理終了                           */
        /*-----cmBTktskB_updTS Bottom-------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_tujo_insertHS                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_tujo_insertHS(struct TS_SERCH ts_ser )    */
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
    /* *****************************************************************************/
    public int   cmBTktskB_tujo_insertHS( TS_SERCH ts_ser )
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_tujo_insertHS処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_i;                 /* 使用ランクコードの算出用Index */
        int[] wk_runk = new int[4];           /* 使用ランクコードの算出用配列  */
        int wk_month;             /* バッチ処理月                  */
        int rtn_cd;                        /* 戻り値                        */
        IntegerDto rtn_status = new IntegerDto();                          /* 結果ステータス                */

        h_sysdate.arr = atoi(gl_sysdate);
        wk_month = atoi(gl_bat_date_mm);

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        h_bat_date.arr = atoi(gl_bat_date);
        /* 2021.04.15 HSポイント日別情報設定値修正 End */


        /* ポイント日別情報構造体初期化 */
//        memset(&hsptymd_t, 0x00, sizeof(hsptymd_t));

        /* ---追加情報をセット--------------------------------------------------- */
        hsptymd_t.system_ymd.arr                             = h_sysdate;                           /* システム年月日               */
        strcpy(hsptymd_t.kokyaku_no,    h_ts.vr_kokyaku_no);
        hsptymd_t.kokyaku_no.len = strlen(h_ts.vr_kokyaku_no.strVal());
        /* 顧客番号                     */
        hsptymd_t.kaiin_kigyo_cd.arr                         = h_kigyo_cd;                          /* 会員企業コード               */
        hsptymd_t.kaiin_kyu_hansya_cd.arr                    = h_kyu_hansya_cd;                     /* 会員旧販社コード             */
        strcpy(hsptymd_t.kaiin_no,            h_kaiin_no.arr);
        hsptymd_t.kaiin_no.len = strlen(h_kaiin_no.arr);
        /* 会員番号                     */
        /* 2022/11/28 MCCM初版 DEL START */
        /*  hsptymd_t.nyukai_kigyo_cd                        = h_ts.nyukai_kigyo_cd; */             /* 入会企業コード               */
        /*  hsptymd_t.nyukai_tenpo                           = h_ts.nyukai_tenpo; */                /* 入会店舗                     */
        /*  hsptymd_t.hakken_kigyo_cd                        = h_ts.hakken_kigyo_cd; */             /* 発券企業コード               */
        /*  hsptymd_t.hakken_tenpo                           = h_ts.hakken_tenpo; */                /* 発券店舗                     */
        /* 2022/11/28 MCCM初版 DEL END */

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        hsptymd_t.data_ymd.arr                               = h_bat_date;                           /* データ年月日                 */
        hsptymd_t.toroku_ymd.arr                             = h_bat_date;                           /* 登録年月日                 */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */

        /* 2022/11/28 MCCM初版 DEL START */
        /*  hsptymd_t.kigyo_cd                               = h_ts.nyukai_kigyo_cd; */             /* 企業コード                   */
        /*  hsptymd_t.mise_no                                = h_ts.nyukai_tenpo; */                /* 店番号                       */
        /* 2022/11/28 MCCM初版 DEL END */

        hsptymd_t.riyu_cd.arr                                 = TUJO_RIYU_CD;                        /* 理由コード                   */
        hsptymd_t.card_nyuryoku_kbn.arr                       = 4;                                   /* カード入力区分               */
        hsptymd_t.riyo_point.arr      = (h_ts.zennen_p.floatVal() + h_ts.tonen_p.floatVal() + h_ts.yokunen_p.floatVal());            /* 利用ポイント                 */

        /* 2022/11/29 MCCM初版 MOD START */
        /*  hsptymd_t.kojin_getuji_rank_cd                   = h_getuji_rank_cd; */                 /* 個人月次ランクコード         */
        /*  hsptymd_t.kojin_nenji_rank_cd                    = h_nenji_rank_cd; */                  /* 個人年次ランクコード         */
        hsptymd_t.kojin_getuji_rank_cd.arr                    = h_rank_getuji_rank_cd;               /* 個人月次ランクコード         */
        hsptymd_t.kojin_nenji_rank_cd.arr                     = h_rank_nenji_rank_cd;                /* 個人年次ランクコード         */
        /* 2022/11/29 MCCM初版 MOD END */

        hsptymd_t.kazoku_getuji_rank_cd.arr                   = h_kazoku_getuji_rank_cd;             /* 家族月次ランクコード         */
        hsptymd_t.kazoku_nenji_rank_cd.arr                    = h_kazoku_nenji_rank_cd;              /* 家族年次ランクコード         */
        /* 使用ランクコードの算出 */
//    wk_runk[0]              = h_getuji_rank_cd;
        wk_runk[0]              = h_rank_getuji_rank_cd.intVal();
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
//    wk_runk[1]              = h_nenji_rank_cd;
        wk_runk[1]              = h_rank_nenji_rank_cd.intVal();
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        wk_runk[2]              = h_kazoku_nenji_rank_cd.intVal();
        wk_runk[3]              = h_kazoku_getuji_rank_cd.intVal();

        hsptymd_t.shiyo_rank_cd.arr = 0;

        for(wk_i=0;wk_i<=3;wk_i++)
        {
            if(wk_runk[wk_i] > hsptymd_t.shiyo_rank_cd.intVal())
            {
                hsptymd_t.shiyo_rank_cd.arr                  = wk_runk[wk_i];                       /* 使用ランクコード             */
            }
        }
        hsptymd_t.kaiage_cnt.arr                                    = h_kaiage_cnt;                 /* 買上回数                     */
        hsptymd_t.koshinmae_riyo_kano_point.arr
                =h_ts.zennen_p.floatVal() + h_ts.tonen_p.floatVal() + h_ts.yokunen_p.floatVal() + h_ts.kikan_p01.floatVal()
                + h_ts.kikan_p02.floatVal() + h_ts.kikan_p03.floatVal() + h_ts.kikan_p04.floatVal() + h_ts.kikan_p05.floatVal();
        /* 更新前利用可能ポイント       */
        /* 2022/11/29 MCCM初版 MOD START */
/*  hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku
hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku
        = h_gekkan_rankup_kingaku;      */
        /* 更新前月間ランクＵＰ対象金額 */
/*  hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku
        = h_nenkan_rankup_kingaku;     */
        /* 更新前年間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku.arr  = h_rank_gekkan_rankup_kingaku; /* 更新前月間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku.arr  = h_rank_nenkan_rankup_kingaku; /* 更新前年間ランクＵＰ対象金額 */
        /* 2022/11/29 MCCM初版 MOD END */

        /* 2022/11/30 MCCM初版 MOD START */
        /*  hsptymd_t.kazoku_id                                     = h_kazoku_id; */               /* 家族ＩＤ                     */
        sprintf(hsptymd_t.kazoku_id , "%d" , h_kazoku_id);                                     /* 家族ＩＤ                     */
        /* 2022/11/30 MCCM初版 MOD END */

        hsptymd_t.delay_koshin_ymd.arr                              = h_sysdate;                    /* ディレイ更新年月日           */
        strcpy(hsptymd_t.delay_koshin_apl_version,                Program_Name_Ver);            /* ディレイ更新ＡＰＬバージョン */
        hsptymd_t.batch_koshin_ymd.arr                              = h_sysdate;                    /* バッチ更新日                 */
        hsptymd_t.saishu_koshin_ymd.arr                             = h_sysdate;                    /* 最終更新日                   */
        hsptymd_t.saishu_koshin_ymdhms.arr                          =h_sysdate;                    /* 最終更新日時                 */
        strcpy(hsptymd_t.saishu_koshin_programid,                 Program_Name);                /* 最終更新プログラムＩＤ       */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo    = ts_ser.this_year;             /* 更新前利用可能通常Ｐ基準年度 */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo       = h_ts.zennen_p;                /* 更新前利用可能通常Ｐ前年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo        = h_ts.tonen_p;                 /* 更新前利用可能通常Ｐ当年度   */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo      = h_ts.yokunen_p;               /* 更新前利用可能通常Ｐ翌年度   */
        hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo             = ts_ser.this_year;
        /* 要求付与通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point.arr
                = (h_ts.zennen_p.floatVal() + h_ts.tonen_p.floatVal() + h_ts.yokunen_p.floatVal());
        /* 要求利用通常Ｐ               */
        hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo             = ts_ser.this_year;             /* 要求利用通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point_zennendo                = h_ts.zennen_p;
        /* 要求利用通常Ｐ前年度         */
        hsptymd_t.yokyu_riyo_tujo_point_tonendo                 = h_ts.tonen_p;
        /* 要求利用通常Ｐ当年度         */
        hsptymd_t.yokyu_riyo_tujo_point_yokunendo               = h_ts.yokunen_p;
        /* 要求利用通常Ｐ翌年度         */
        hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo            = ts_ser.this_year;
        /* 更新付与通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point.arr
                = (h_ts.zennen_p.floatVal() + h_ts.tonen_p.floatVal() + h_ts.yokunen_p.floatVal());
        /* 更新利用通常Ｐ               */
        hsptymd_t.koshin_riyo_tujo_point_kijun_nendo            = ts_ser.this_year;             /* 更新利用通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point_zennendo               = h_ts.zennen_p;
        /* 更新利用通常Ｐ前年度         */
        hsptymd_t.koshin_riyo_tujo_point_tonendo                = h_ts.tonen_p;
        /* 更新利用通常Ｐ当年度         */
        hsptymd_t.koshin_riyo_tujo_point_yokunendo              = h_ts.yokunen_p;
        /* 更新利用通常Ｐ翌年度         */
        hsptymd_t.koshinmae_kikan_gentei_point_kijun_month.arr     = wk_month;
        /* 更新前期間限定Ｐ基準月       */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0       = h_ts.kikan_p01;
        /* 更新前利用可能期間限定Ｐ０   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1       = h_ts.kikan_p02;
        /* 更新前利用可能期間限定Ｐ１   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2       = h_ts.kikan_p03;
        /* 更新前利用可能期間限定Ｐ２   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3       = h_ts.kikan_p04;
        /* 更新前利用可能期間限定Ｐ３   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4       = h_ts.kikan_p05;
        /* 更新前利用可能期間限定Ｐ４   */
        hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month.arr  = wk_month;
        /* 要求付与期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month.arr  = wk_month;
        /* 要求利用期間限定Ｐ基準月     */
        hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month.arr  = wk_month;
        /* 更新付与期間限定Ｐ基準月     */
        hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month.arr  = wk_month;
        /* 更新利用期間限定Ｐ基準月     */

        /* 2022/11/29 MCCM初版 ADD START */
        hsptymd_t.kaisha_cd_mcc                                 = h_ts.nyukai_kigyo_cd_mcc;     /* 会社コードＭＣＣ               */
        hsptymd_t.mise_no_mcc                                   = h_ts.nyukai_tenpo_mcc;        /* 店番号ＭＣＣ                   */
        hsptymd_t.meisai_su.arr                                     = gl_meisai_su;                 /* 通常Ｐ明細数                   */
        hsptymd_t.nyukai_kaisha_cd_mcc                          = h_ts.nyukai_kigyo_cd_mcc;     /* 入会会社コードＭＣＣ           */
        hsptymd_t.nyukai_tenpo_mcc                              = h_ts.nyukai_tenpo_mcc;        /* 入会店舗ＭＣＣ                 */
        /* 2022/11/29 MCCM初版 ADD END */

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
                hsptymd_t.torihiki_no.floatVal()                     );
        C_DbgMsg( "時刻                         0 =[%d]\n"     ,
                hsptymd_t.jikoku_hms                      );
        C_DbgMsg( "理由コード                1193 =[%d]\n"     ,
                hsptymd_t.riyu_cd                         );
        C_DbgMsg( "カード入力区分               4 =[%d]\n"     ,
                hsptymd_t.card_nyuryoku_kbn               );
        C_DbgMsg( "処理対象ファイルレコード番号 0 =[%d]\n"     ,
                hsptymd_t.shori_taisho_file_record_no     );
        C_DbgMsg( "リアル更新フラグ             0 =[%d]\n"     ,
                hsptymd_t.real_koshin_flg                 );
        C_DbgMsg( "付与ポイント                 0 =[%10.0f]\n",
                hsptymd_t.fuyo_point.floatVal()                      );
        C_DbgMsg( "利用ポイント                   =[%10.0f]\n",
                hsptymd_t.riyo_point.floatVal()                       );
        C_DbgMsg( "基本Ｐ率対象ポイント         0 =[%10.0f]\n",
                hsptymd_t.kihon_pritsu_taisho_point.floatVal()        );
        C_DbgMsg( "ランクＵＰ対象金額           0 =[%10.0f]\n",
                hsptymd_t.rankup_taisho_kingaku.floatVal()            );
        C_DbgMsg( "ポイント対象金額             0 =[%10.0f]\n",
                hsptymd_t.point_taisho_kingaku.floatVal()             );
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
                hsptymd_t.kaiage_kingaku.floatVal()                   );
        C_DbgMsg( "買上回数                       =[%10.0f]\n",
                hsptymd_t.kaiage_cnt.floatVal()                       );
        C_DbgMsg( "更新前利用可能ポイント         =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_point.floatVal()        );
        C_DbgMsg( "更新前付与ポイント             =[%10.0f]\n",
                hsptymd_t.koshinmae_fuyo_point.floatVal()             );
        C_DbgMsg( "更新前基本Ｐ率対象ポイント     =[%10.0f]\n",
                hsptymd_t.koshinmae_kihon_pritsu_taisho_point.floatVal() );
        C_DbgMsg( "更新前月間ランクＵＰ対象金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku.floatVal() );
        C_DbgMsg( "更新前年間ランクＵＰ対象金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku.floatVal() );
        C_DbgMsg( "更新前ポイント対象金額         =[%10.0f]\n",
                hsptymd_t.koshinmae_point_taisho_kingaku.floatVal()   );
        C_DbgMsg( "更新前買上額                   =[%10.0f]\n",
                hsptymd_t.koshinmae_kaiage_kingaku.floatVal()         );
        /* 2022/11/30 MCCM初版 ADD START */
/*  C_DbgMsg( "家族ＩＤ                       =[%d]\n"     ,
                                    hsptymd_t.kazoku_id                       ); */
        C_DbgMsg( "家族ＩＤ                       =[%s]\n"     ,
                hsptymd_t.kazoku_id.arr                   );
        /* 2022/11/30 MCCM初版 ADD END */
        C_DbgMsg( "更新前月間家族ランクＵＰ金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_gekkan_kazoku_rankup_taisho_kingaku.floatVal() );
        C_DbgMsg( "更新前年間家族ランクＵＰ金額   =[%10.0f]\n",
                hsptymd_t.koshinmae_nenkan_kazoku_rankup_taisho_kingaku.floatVal() );
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
                hsptymd_t.sagyosha_id.floatVal()                      );
        C_DbgMsg( "作業年月日                   0 =[%d]\n"     ,
                hsptymd_t.sagyo_ymd                       );
        C_DbgMsg( "作業時刻                     0 =[%d]\n"     ,
                hsptymd_t.sagyo_hms                       );
        C_DbgMsg( "バッチ更新日             today =[%d]\n"     ,
                hsptymd_t.batch_koshin_ymd                );
        C_DbgMsg( "最終更新日               today =[%d]\n"     ,
                hsptymd_t.saishu_koshin_ymd               );
        C_DbgMsg( "最終更新日時             today =[%f]\n"    ,
                hsptymd_t.saishu_koshin_ymdhms.floatVal()             );
        C_DbgMsg( "最終更新プログラムＩＤ         =[%s]\n"     ,
                hsptymd_t.saishu_koshin_programid         );
        C_DbgMsg( "要求利用Ｐ内訳フラグ         0 =[%d]\n"     ,
                hsptymd_t.yokyu_riyo_putiwake_flg         );
        C_DbgMsg( "更新前利用可能通常Ｐ基準年度   =[%d]\n"     ,
                hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo );
        C_DbgMsg( "更新前利用可能通常Ｐ前年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo.floatVal()     );
        C_DbgMsg( "更新前利用可能通常Ｐ当年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo.floatVal()      );
        C_DbgMsg( "更新前利用可能通常Ｐ翌年度     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo.floatVal()    );
        C_DbgMsg( "要求付与通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_point.floatVal()            );
        C_DbgMsg( "要求付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "要求付与通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_zennendo.floatVal()   );
        C_DbgMsg( "要求付与通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_tonendo.floatVal()   );
        C_DbgMsg( "要求利用通常Ｐ                 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point.floatVal()            );
        C_DbgMsg( "要求利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo           );
        C_DbgMsg( "要求利用通常Ｐ前年度           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_zennendo.floatVal()   );
        C_DbgMsg( "要求利用通常Ｐ当年度           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_tonendo.floatVal()    );
        C_DbgMsg( "要求利用通常Ｐ翌年度           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_yokunendo.floatVal()  );
        C_DbgMsg( "更新付与通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point.floatVal()           );
        C_DbgMsg( "更新付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "更新付与通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_zennendo.floatVal()  );
        C_DbgMsg( "更新付与通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_tonendo.floatVal()   );
        C_DbgMsg( "更新利用通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point.floatVal()           );
        C_DbgMsg( "更新利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.koshin_riyo_tujo_point_kijun_nendo          );
        C_DbgMsg( "更新利用通常Ｐ前年度           =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_zennendo.floatVal()  );
        C_DbgMsg( "更新利用通常Ｐ当年度           =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_tonendo.floatVal()   );
        C_DbgMsg( "更新利用通常Ｐ翌年度           =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_yokunendo.floatVal() );
        C_DbgMsg( "更新前期間限定Ｐ基準月         =[%d]\n"     ,
                hsptymd_t.koshinmae_kikan_gentei_point_kijun_month    );
        C_DbgMsg( "更新前利用可能期間限定Ｐ０   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0.floatVal()      );
        C_DbgMsg( "更新前利用可能期間限定Ｐ１   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1.floatVal()      );
        C_DbgMsg( "更新前利用可能期間限定Ｐ２   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2.floatVal()      );
        C_DbgMsg( "更新前利用可能期間限定Ｐ３   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3.floatVal()      );
        C_DbgMsg( "更新前利用可能期間限定Ｐ４   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4.floatVal()      );
        C_DbgMsg( "要求付与期間限定Ｐ           0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point.floatVal()    );
        C_DbgMsg( "要求付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求付与期間限定Ｐ０         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point0.floatVal()   );
        C_DbgMsg( "要求付与期間限定Ｐ１         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point1.floatVal()   );
        C_DbgMsg( "要求付与期間限定Ｐ２         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point2.floatVal()   );
        C_DbgMsg( "要求付与期間限定Ｐ３         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point3.floatVal()   );
        C_DbgMsg( "要求利用期間限定Ｐ           0 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point.floatVal()    );
        C_DbgMsg( "要求利用期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求利用期間限定Ｐ０           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point0.floatVal()   );
        C_DbgMsg( "要求利用期間限定Ｐ１         0 =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point1.floatVal()   );
        C_DbgMsg( "要求利用期間限定Ｐ２         0 =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point2.floatVal()   );
        C_DbgMsg( "要求利用期間限定Ｐ３         0 =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point3.floatVal()   );
        C_DbgMsg( "要求利用期間限定Ｐ４         0 =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point4.floatVal()   );
        C_DbgMsg( "更新付与期間限定Ｐ           0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point.floatVal()   );
        C_DbgMsg( "更新付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month );
        C_DbgMsg( "更新付与期間限定Ｐ０１       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point01.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０２       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point02.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０３       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point03.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０４       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point04.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０５       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point05.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０６       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point06.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０７       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point07.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０８       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point08.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ０９       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point09.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ１０       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point10.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ１１       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point11.floatVal() );
        C_DbgMsg( "更新付与期間限定Ｐ１２       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point12.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ           0 =[%10.0f]\n" ,
                hsptymd_t.koshin_riyo_kikan_gentei_point.floatVal()   );
        C_DbgMsg( "更新利用期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month  );
        C_DbgMsg( "更新利用期間限定Ｐ０１       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point01.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０２       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point02.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０３       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point03.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０４       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point04.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０５       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point05.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０６       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point06.floatVal() );
        C_DbgMsg( "更新利用期間限定Ｐ０７       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point07.floatVal());
        C_DbgMsg( "更新利用期間限定Ｐ０８       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point08.floatVal());
        C_DbgMsg( "更新利用期間限定Ｐ０９       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point09.floatVal());
        C_DbgMsg( "更新利用期間限定Ｐ１０       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point10.floatVal());
        C_DbgMsg( "更新利用期間限定Ｐ１１       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point11.floatVal());
        C_DbgMsg( "更新利用期間限定Ｐ１２       0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_kikan_gentei_point12.floatVal());
        /*----------------------------------------------------------------*/

        /* HSポイント日別情報追加 */
        /* rtn_cd = C_InsertDayPoint(&hsptymd_t, h_sysdate, rtn_status); */
        rtn_cd = cmBTfuncB.C_InsertDayPoint(hsptymd_t, atoi(gl_bat_date), rtn_status);
        if(rtn_cd != C_const_OK ){
            APLOG_WT( "903", 0, null, "C_InsertDayPoint(通常ポイント失効）" ,
                    rtn_cd, 0, 0, 0, 0);
            /*----------------------------------------------------------------*/
            C_DbgMsg( "*** cmBTktskB_tujo_insertHS *** "+
                    "C_InsertDayPoint rtn_cd=[%d]\n", rtn_cd);
            C_DbgMsg( "*** cmBTktskB_tujo_insertHS *** C_InsertDayPoint "+
                    "rtn_status=[%d]\n", rtn_status);
            /*----------------------------------------------------------------*/
            /* 処理を終了する */
            return C_const_NG;
        }
        w_seq = hsptymd_t.shori_seq.floatVal();
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_tujo_insertHS処理処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK;              /* 処理終了                           */
        /*-----cmBTktskB_tujo_insertHS処理r Bottom------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_kikan_insertHS                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_kikan_insertHS(struct TS_SERCH ts_ser )   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別情報（期間限定ポイント失効）登録。                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      struct TS_SERCH ts_ser          登録用データ格納構造体                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int   cmBTktskB_kikan_insertHS( TS_SERCH ts_ser )
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_kikan_insertHS処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int wk_i;                 /* 使用ランクコードの算出用Index */
        int[] wk_runk = new int[4];           /* 使用ランクコードの算出用配列  */
        int wk_month;             /* バッチ処理月                  */
        int rtn_cd;                        /* 戻り値                        */
        IntegerDto rtn_status = new IntegerDto();                          /* 結果ステータス                */

        h_sysdate.arr = atoi(gl_sysdate);
        wk_month = atoi(gl_bat_date_mm);

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        h_bat_date.arr = atoi(gl_bat_date);
        /* 2021.04.15 HSポイント日別情報設定値修正 End */

        /* ポイント日別情報構造体初期化 */
//        memset(hsptymd_t, 0x00, sizeof(hsptymd_t));

        /* ---追加情報をセット--------------------------------------------------- */
        hsptymd_t.system_ymd.arr                             = h_sysdate;                           /* システム年月日               */
        strcpy(hsptymd_t.kokyaku_no,    h_ts.vr_kokyaku_no);
        hsptymd_t.kokyaku_no.len = strlen(h_ts.vr_kokyaku_no.strVal());
        /* 顧客番号                     */
        hsptymd_t.kaiin_kigyo_cd.arr                         = h_kigyo_cd;                          /* 会員企業コード               */
        hsptymd_t.kaiin_kyu_hansya_cd.arr                    = h_kyu_hansya_cd;                     /* 会員旧販社コード             */
        strcpy(hsptymd_t.kaiin_no,            h_kaiin_no.arr);
        hsptymd_t.kaiin_no.len = strlen(h_kaiin_no.arr);
        /* 会員番号                     */

        /* 2022/11/29 MCCM初版 DEL START */
        /*  hsptymd_t.nyukai_kigyo_cd                        = h_ts.nyukai_kigyo_cd; */             /* 入会企業コード               */
        /*  hsptymd_t.nyukai_tenpo                           = h_ts.nyukai_tenpo; */                /* 入会店舗                     */
        /*  hsptymd_t.hakken_kigyo_cd                        = h_ts.hakken_kigyo_cd; */             /* 発券企業コード               */
        /*  hsptymd_t.hakken_tenpo                           = h_ts.hakken_tenpo; */                /* 発券店舗                     */
        /* 2022/11/29 MCCM初版 DEL END */

        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
        hsptymd_t.data_ymd.arr                               = h_bat_date;                           /* データ年月日                 */
        hsptymd_t.toroku_ymd.arr                             = h_bat_date;                           /* 登録年月日                 */
        /* 2021.04.15 HSポイント日別情報設定値修正 End */

        /* 2022/11/29 MCCM初版 DEL START */
        /*  hsptymd_t.kigyo_cd                               = h_ts.nyukai_kigyo_cd; */             /* 企業コード                   */
        /*  hsptymd_t.mise_no                                = h_ts.nyukai_tenpo; */                /* 店番号                       */
        /* 2022/11/29 MCCM初版 DEL END */

        hsptymd_t.riyu_cd.arr                                = KIKAN_RIYU_CD;
        /* 理由コード                   */
        hsptymd_t.card_nyuryoku_kbn.arr                      = 4;                                   /* カード入力区分               */
        hsptymd_t.riyo_point.arr
                = (h_ts.kikan_p01.floatVal() + h_ts.kikan_p02.floatVal() + h_ts.kikan_p03.floatVal() + h_ts.kikan_p04.floatVal()
                + h_ts.kikan_p05.floatVal());
        /* 利用ポイント                 */

        /* 2022/11/29 MCCM初版 MOD START */
        /*  hsptymd_t.kojin_getuji_rank_cd                   = h_getuji_rank_cd; */                 /* 個人月次ランクコード         */
        /*  hsptymd_t.kojin_nenji_rank_cd                    = h_nenji_rank_cd; */                  /* 個人年次ランクコード         */
        hsptymd_t.kojin_getuji_rank_cd.arr                   = h_rank_getuji_rank_cd;               /* 個人月次ランクコード         */
        hsptymd_t.kojin_nenji_rank_cd.arr                    = h_rank_nenji_rank_cd;                /* 個人年次ランクコード         */
        /* 2022/11/29 MCCM初版 MOD END */

        hsptymd_t.kazoku_getuji_rank_cd.arr                  = h_kazoku_getuji_rank_cd;             /* 家族月次ランクコード         */
        hsptymd_t.kazoku_nenji_rank_cd.arr                   = h_kazoku_nenji_rank_cd;              /* 家族年次ランクコード         */
        /* 使用ランクコードの算出 */
//    wk_runk[0]              = h_getuji_rank_cd;
        wk_runk[0]              = h_rank_getuji_rank_cd.intVal();
        /* 2021.04.15 HSポイント日別情報設定値修正 Start */
//    wk_runk[1]              = h_nenji_rank_cd;
        wk_runk[1]              = h_rank_nenji_rank_cd.intVal();
        /* 2021.04.15 HSポイント日別情報設定値修正 End */
        wk_runk[2]              = h_kazoku_nenji_rank_cd.intVal();
        wk_runk[3]              = h_kazoku_getuji_rank_cd.intVal();
        hsptymd_t.shiyo_rank_cd.arr = 0;
        for(wk_i=0;wk_i<=3;wk_i++)
        {
            if(wk_runk[wk_i] > hsptymd_t.shiyo_rank_cd.intVal())
            {
                hsptymd_t.shiyo_rank_cd.arr                  = wk_runk[wk_i];                       /* 使用ランクコード             */
            }
        }
        hsptymd_t.kaiage_cnt.arr                                    = h_kaiage_cnt;                 /* 買上回数                     */
        hsptymd_t.koshinmae_riyo_kano_point.arr
                =(h_ts.kikan_p01.floatVal()
                + h_ts.kikan_p02.floatVal() + h_ts.kikan_p03.floatVal() + h_ts.kikan_p04.floatVal() + h_ts.kikan_p05.floatVal());
        /* 更新前利用可能ポイント       */
        /* 2022/11/29 MCCM初版 MOD START */
/*  hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku  *
hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku
        = h_gekkan_rankup_kingaku;      */
        /* 更新前月間ランクＵＰ対象金額 */
/*  hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku
        = h_nenkan_rankup_kingaku;     */
        /* 更新前年間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_gekkan_kojin_rankup_taisho_kingaku.arr  = h_rank_gekkan_rankup_kingaku; /* 更新前月間ランクＵＰ対象金額 */
        hsptymd_t.koshinmae_nenkan_kojin_rankup_taisho_kingaku.arr  = h_rank_nenkan_rankup_kingaku; /* 更新前年間ランクＵＰ対象金額 */
        /* 2022/11/29 MCCM初版 MOD END */

        /* 2022/11/30 MCCM初版 MOD START */
        /*  hsptymd_t.kazoku_id                                     = h_kazoku_id; */               /* 家族ＩＤ                     */
        sprintf(hsptymd_t.kazoku_id , "%d" , h_kazoku_id);                                     /* 家族ＩＤ                     */
        /* 2022/11/30 MCCM初版 MOD END */
        hsptymd_t.delay_koshin_ymd.arr                              = h_sysdate;                    /* ディレイ更新年月日           */
        strcpy(hsptymd_t.delay_koshin_apl_version,                Program_Name_Ver);            /* ディレイ更新ＡＰＬバージョン */
        hsptymd_t.batch_koshin_ymd.arr                              = h_sysdate;                    /* バッチ更新日                 */
        hsptymd_t.saishu_koshin_ymd.arr                             = h_sysdate;                    /* 最終更新日                   */
        hsptymd_t.saishu_koshin_ymdhms.arr                          = h_sysdate;                    /* 最終更新日時                 */
        strcpy(hsptymd_t.saishu_koshin_programid,                 Program_Name);                /* 最終更新プログラムＩＤ       */
        hsptymd_t.koshinmae_riyo_kano_tujo_point_kijun_nendo    = ts_ser.this_year;             /* 更新前利用可能通常Ｐ基準年度 */
        hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo             = ts_ser.this_year;
        /* 要求付与通常Ｐ基準年度       */
        hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo             = ts_ser.this_year;             /* 要求利用通常Ｐ基準年度       */
        hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo            = ts_ser.this_year;
        /* 更新付与通常Ｐ基準年度       */
        hsptymd_t.koshin_riyo_tujo_point_kijun_nendo            = ts_ser.this_year;             /* 更新利用通常Ｐ基準年度       */
        hsptymd_t.koshinmae_kikan_gentei_point_kijun_month.arr      = wk_month;
        /* 更新前期間限定Ｐ基準月       */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point0       = h_ts.kikan_p01;
        /* 更新前利用可能期間限定Ｐ０   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point1       = h_ts.kikan_p02;
        /* 更新前利用可能期間限定Ｐ１   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point2       = h_ts.kikan_p03;
        /* 更新前利用可能期間限定Ｐ２   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point3       = h_ts.kikan_p04;
        /* 更新前利用可能期間限定Ｐ３   */
        hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4       = h_ts.kikan_p05;
        /* 更新前利用可能期間限定Ｐ４   */
        hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month.arr     = wk_month;
        /* 要求付与期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point.arr
                = (h_ts.kikan_p01.floatVal() + h_ts.kikan_p02.floatVal() + h_ts.kikan_p03.floatVal() + h_ts.kikan_p04.floatVal()
                + h_ts.kikan_p05.floatVal());
        /* 要求利用期間限定Ｐ           */
        hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month.arr     = wk_month;
        /* 要求利用期間限定Ｐ基準月     */
        hsptymd_t.yokyu_riyo_kikan_gentei_point0                = h_ts.kikan_p01;
        /* 要求利用期間限定Ｐ０         */
        hsptymd_t.yokyu_riyo_kikan_gentei_point1                = h_ts.kikan_p02;
        /* 要求利用期間限定Ｐ１         */
        hsptymd_t.yokyu_riyo_kikan_gentei_point2                = h_ts.kikan_p03;
        /* 要求利用期間限定Ｐ２         */
        hsptymd_t.yokyu_riyo_kikan_gentei_point3                = h_ts.kikan_p04;
        /* 要求利用期間限定Ｐ３         */
        hsptymd_t.yokyu_riyo_kikan_gentei_point4                = h_ts.kikan_p05;
        /* 要求利用期間限定Ｐ４         */
        hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month.arr    = wk_month;
        /* 更新付与期間限定Ｐ基準月     */
        hsptymd_t.koshin_riyo_kikan_gentei_point.arr
                = (h_ts.kikan_p01.floatVal() + h_ts.kikan_p02.floatVal() + h_ts.kikan_p03.floatVal() + h_ts.kikan_p04.floatVal()
                + h_ts.kikan_p05.floatVal());
        /* 更新利用期間限定Ｐ           */
        hsptymd_t.koshin_riyo_kikan_gentei_point_kijun_month.arr    = wk_month;
        /* 更新利用期間限定Ｐ基準月     */

        /* 対象月に対応した更新利用期間限定ＰＭＭに値を設定する */
        if (h_ts.kikan_p01.intVal() != 0){
            cmBTktskB_setMonth(wk_month, h_ts.kikan_p01.intVal());
        }
        if (h_ts.kikan_p02.intVal() != 0){
            cmBTktskB_setMonth(wk_month+1, h_ts.kikan_p02.intVal());
        }
        if (h_ts.kikan_p03.intVal() != 0){
            cmBTktskB_setMonth(wk_month+2, h_ts.kikan_p03.intVal());
        }
        if (h_ts.kikan_p04.intVal() != 0){
            cmBTktskB_setMonth(wk_month+3, h_ts.kikan_p04.intVal());
        }
        if (h_ts.kikan_p05.intVal() != 0){
            cmBTktskB_setMonth(wk_month+4, h_ts.kikan_p05.intVal());
        }

        /* 2022/11/29 MCCM初版 ADD START */
        hsptymd_t.kaisha_cd_mcc                                 = h_ts.nyukai_kigyo_cd_mcc;     /* 会社コードＭＣＣ               */
        hsptymd_t.mise_no_mcc                                   = h_ts.nyukai_tenpo_mcc;        /* 店番号ＭＣＣ                   */
        hsptymd_t.meisai_su.arr                                     = gl_kikan_meisai_su;           /* 期間限定Ｐ明細数               */
        hsptymd_t.nyukai_kaisha_cd_mcc                          = h_ts.nyukai_kigyo_cd_mcc;     /* 入会会社コードＭＣＣ           */
        hsptymd_t.nyukai_tenpo_mcc                              = h_ts.nyukai_tenpo_mcc;        /* 入会店舗ＭＣＣ                 */
        /* 2022/11/29 MCCM初版 ADD END */

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
        C_DbgMsg( "理由コード                1194 =[%d]\n"     ,
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
        /* 2022/11/30 MCCM初版 ADD START */
/*  C_DbgMsg( "家族ＩＤ                       =[%d]\n"     ,
                                    hsptymd_t.kazoku_id                       ); */
        C_DbgMsg( "家族ＩＤ                       =[%s]\n"     ,
                hsptymd_t.kazoku_id.arr                   );
        /* 2022/11/30 MCCM初版 ADD END */
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
        C_DbgMsg( "更新前利用可能通常Ｐ前年度   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_zennendo    );
        C_DbgMsg( "更新前利用可能通常Ｐ当年度   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_tonendo     );
        C_DbgMsg( "更新前利用可能通常Ｐ翌年度   0 =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_tujo_point_yokunendo   );
        C_DbgMsg( "要求付与通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_point           );
        C_DbgMsg( "要求付与通常Ｐ基準年度        =[%d]\n",
                hsptymd_t.yokyu_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "要求付与通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_zennendo  );
        C_DbgMsg( "要求付与通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_tujo_poin_tonendo  );
        C_DbgMsg( "要求利用通常Ｐ                 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point           );
        C_DbgMsg( "要求利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.yokyu_riyo_tujo_point_kijun_nendo           );
        C_DbgMsg( "要求利用通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_zennendo  );
        C_DbgMsg( "要求利用通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_tonendo   );
        C_DbgMsg( "要求利用通常Ｐ翌年度         0 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_tujo_point_yokunendo );
        C_DbgMsg( "更新付与通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point          );
        C_DbgMsg( "更新付与通常Ｐ基準年度         =[%d]\n",
                hsptymd_t.koshin_fuyo_tujo_point_kijun_nendo);
        C_DbgMsg( "更新付与通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_zennendo );
        C_DbgMsg( "更新付与通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_fuyo_tujo_point_tonendo  );
        C_DbgMsg( "更新利用通常Ｐ               0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point          );
        C_DbgMsg( "更新利用通常Ｐ基準年度         =[%d]\n" ,
                hsptymd_t.koshin_riyo_tujo_point_kijun_nendo          );
        C_DbgMsg( "更新利用通常Ｐ前年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_zennendo );
        C_DbgMsg( "更新利用通常Ｐ当年度         0 =[%10.0f]\n",
                hsptymd_t.koshin_riyo_tujo_point_tonendo  );
        C_DbgMsg( "更新利用通常Ｐ翌年度         0 =[%10.0f]\n",
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
        C_DbgMsg( "更新前利用可能期間限定Ｐ４     =[%10.0f]\n",
                hsptymd_t.koshinmae_riyo_kano_kikan_gentei_point4     );
        C_DbgMsg( "要求付与期間限定Ｐ           0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point   );
        C_DbgMsg( "要求付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_fuyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求付与期間限定Ｐ０         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point0  );
        C_DbgMsg( "要求付与期間限定Ｐ１         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point1  );
        C_DbgMsg( "要求付与期間限定Ｐ２         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point2  );
        C_DbgMsg( "要求付与期間限定Ｐ３         0 =[%10.0f]\n",
                hsptymd_t.yokyu_fuyo_kikan_gentei_point3  );
        C_DbgMsg( "要求利用期間限定Ｐ           0 =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point   );
        C_DbgMsg( "要求利用期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point_kijun_month   );
        C_DbgMsg( "要求利用期間限定Ｐ０           =[%10.0f]\n",
                hsptymd_t.yokyu_riyo_kikan_gentei_point0  );
        C_DbgMsg( "要求利用期間限定Ｐ１           =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point1  );
        C_DbgMsg( "要求利用期間限定Ｐ２           =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point2  );
        C_DbgMsg( "要求利用期間限定Ｐ３           =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point3  );
        C_DbgMsg( "要求利用期間限定Ｐ４           =[%10.0f]\n"     ,
                hsptymd_t.yokyu_riyo_kikan_gentei_point4  );
        C_DbgMsg( "更新付与期間限定Ｐ           0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point  );
        C_DbgMsg( "更新付与期間限定Ｐ基準月       =[%d]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point_kijun_month );
        C_DbgMsg( "更新付与期間限定Ｐ０１       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point01);
        C_DbgMsg( "更新付与期間限定Ｐ０２       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point02);
        C_DbgMsg( "更新付与期間限定Ｐ０３       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point03);
        C_DbgMsg( "更新付与期間限定Ｐ０４       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point04);
        C_DbgMsg( "更新付与期間限定Ｐ０５       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point05);
        C_DbgMsg( "更新付与期間限定Ｐ０６       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point06);
        C_DbgMsg( "更新付与期間限定Ｐ０７       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point07);
        C_DbgMsg( "更新付与期間限定Ｐ０８       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point08);
        C_DbgMsg( "更新付与期間限定Ｐ０９       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point09);
        C_DbgMsg( "更新付与期間限定Ｐ１０       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point10);
        C_DbgMsg( "更新付与期間限定Ｐ１１       0 =[%10.0f]\n"     ,
                hsptymd_t.koshin_fuyo_kikan_gentei_point11);
        C_DbgMsg( "更新付与期間限定Ｐ１２       0 =[%10.0f]\n"     ,
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
        /*----------------------------------------------------------------*/

        /* HSポイント日別情報追加 */
        /* rtn_cd = C_InsertDayPoint(&hsptymd_t, h_sysdate, rtn_status); */
        rtn_cd = cmBTfuncB.C_InsertDayPoint(hsptymd_t, atoi(gl_bat_date), rtn_status);
        if(rtn_cd != C_const_OK ){
            APLOG_WT( "903", 0, null, "C_InsertDayPoint(期間限定ポイント失効）" ,
                    rtn_cd, 0, 0, 0, 0);
            /*----------------------------------------------------------------*/
            C_DbgMsg( "*** cmBTktskB_kikan_insertHS *** " +
                    "C_InsertDayPoint rtn_cd=[%d]\n", rtn_cd);
            C_DbgMsg( "*** cmBTktskB_kikan_insertHS *** C_InsertDayPoint " +
                    "rtn_status=[%d]\n", rtn_status);
            /*----------------------------------------------------------------*/
            /* 処理を終了する */
            return C_const_NG;
        }
        w_seq_2 = hsptymd_t.shori_seq.floatVal();
        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_kikan_insertHS処理処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK;              /* 処理終了                           */
        /*-----cmBTktskB_kikan_insertHS処理 Bottom------------------------------------*/
    }
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_setMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void   cmBTktskB_setMonth(unsigned short int i_month,           */
    /*                                      double kikan_p)                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*    代入された月の利用可能期間限定ＰＭＭに値を設定する。                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     char     i_month     要求月                                            */
    /*     double   kikan_p     期間限定ポイント                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /* *****************************************************************************/
    public void   cmBTktskB_setMonth(int i_month, double kikan_p)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_setMonth処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto buff = new StringDto(40);                             /*  バッファ                  */
        /*--------------------------------------*/

        switch(i_month % 12){
            case 1 : hsptymd_t.koshin_riyo_kikan_gentei_point01.arr = kikan_p;
                /* 更新利用期間限定Ｐ０１  */
                break;
            case 2 : hsptymd_t.koshin_riyo_kikan_gentei_point02.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０２  */
                break;
            case 3 : hsptymd_t.koshin_riyo_kikan_gentei_point03.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０３  */
                break;
            case 4 : hsptymd_t.koshin_riyo_kikan_gentei_point04.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０４  */
                break;
            case 5 : hsptymd_t.koshin_riyo_kikan_gentei_point05.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０５  */
                break;
            case 6 : hsptymd_t.koshin_riyo_kikan_gentei_point06.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０６  */
                break;
            case 7 : hsptymd_t.koshin_riyo_kikan_gentei_point07.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０７  */
                break;
            case 8 : hsptymd_t.koshin_riyo_kikan_gentei_point08.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０８  */
                break;
            case 9 : hsptymd_t.koshin_riyo_kikan_gentei_point09.arr  = kikan_p;
                /* 更新利用期間限定Ｐ０９  */
                break;
            case 10 : hsptymd_t.koshin_riyo_kikan_gentei_point10.arr  = kikan_p;
                /* 更新利用期間限定Ｐ１０ */
                break;
            case 11 : hsptymd_t.koshin_riyo_kikan_gentei_point11.arr  = kikan_p;
                /* 更新利用期間限定Ｐ１１ */
                break;
            case 0 : hsptymd_t.koshin_riyo_kikan_gentei_point12.arr  = kikan_p;
                /* 更新利用期間限定Ｐ１２  */
                break;
        }

        /*------------------------------------------------------------*/
        sprintf(buff, "要求月：[%d]", i_month);
        C_DbgMsg("*** cmBTktskB_setMonth *** %s\n", buff);
        C_DbgEnd("cmBTktskB_setMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        /*--- cmBTktskB_setMonth処理 -------------------------------------------------*/
    }

    /* 2022/11/29 MCCM初版 ADD START */
/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTktskB_insertHSUchiwake                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTktskB_insertHSUchiwake()                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     HSポイント日別内訳情報登録                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      unsigned short int  point_kbn      通常期間限定区分                   */
    /*                                         1:通常ポイント，2:期間限定ポイント */
    /*      double              kobai_point    購買ポイント                       */
    /*      double              hikobai_point  非購買ポイント                     */
    /*      double              sonota_point   その他ポイント                     */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /* *****************************************************************************/
    public int   cmBTktskB_insertHSUchiwake(int point_kbn, int meisai_su, HSUCHIWAKE_INSERT [] hsuchiwake_insert)
    {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTktskB_insertHSUchiwake処理");
        /*---------------------------------------------------------------------*/
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        StringDto    wk_sql = new StringDto(40*2);                                 /* 動的SQLバッファ                    */
        StringDto    wk_sql2= new StringDto(40*2);                                /* 動的SQLバッファ                    */
        StringDto    wk_sql3= new StringDto(40*2);                                /* 動的SQLバッファ                    */
        StringDto    wk_sql4= new StringDto(40*2);                                /* 動的SQLバッファ                    */
        StringDto       buff= new StringDto(40);                          /*  バッファ                  */
        String []   strs = {"０１","０２","０３","０４","０５",
            "０６","０７","０８","０９","１０"};

        double       wk_shori_seq;

        /*-----------------------------------------------*/
        /*  変数初期化                                   */
        /*-----------------------------------------------*/
        memset(h_str_sql, 0x00, sizeof(h_str_sql));
        memset(wk_sql, 0x00, sizeof(wk_sql));                   /* 動的SQLバッファ                    */
        memset(wk_sql3, 0x00, sizeof(wk_sql3));                   /* 動的SQLバッファ                    */

        wk_shori_seq = 0;
        if(point_kbn==1){
            wk_shori_seq=w_seq;
        }else{
            wk_shori_seq=w_seq_2;
        }

        /* 通常期間限定区分: 1：通常ポイント　2：期間限定ポイント  */
        /* 購買区分        : 1: 購買　2:非購買　3:その他           */

        int i = 0;
        int j = 0;
        int cnt = meisai_su / 10;
        if (meisai_su % 10 > 0) {
            cnt++;
        }

        for (j = 0; j < cnt; j++) {

            memset(h_str_sql, 0x00, sizeof(h_str_sql));
            memset(wk_sql, 0x00, sizeof(wk_sql));                   /* 動的SQLバッファ                    */
            memset(wk_sql3, 0x00, sizeof(wk_sql3));                   /* 動的SQLバッファ                    */

            for (i = j*10; i < (j+1)*10; i++) {
                if (hsuchiwake_insert[i].fuyo_riyo_kbn.intVal() == 0) {
                    break;
                }
                memset(wk_sql2, 0x00, sizeof(wk_sql2));
                sprintf(wk_sql2,
                        "            ,付与利用区分%s                   " +
                        "            ,明細番号%s                       " +
                        "            ,ポイント種別%s                   " +
                        "            ,利用ポイント%s                   " +
                        "            ,通常期間限定区分%s               " +
                        "            ,ポイント有効期限%s               " +
                        "            ,購買区分%s                       "
                        , strs[i%10], strs[i%10], strs[i%10], strs[i%10], strs[i%10], strs[i%10], strs[i%10]);

                strcat(wk_sql, wk_sql2);

                memset(wk_sql4, 0x00, sizeof(wk_sql4));
                sprintf(wk_sql4,
                        "            ,%d               " +
                        "            ,%d               " +
                        "            ,%d               " +
                        "            ,%d              " +
                        "            ,%d               " +
                        "            ,%d               " +
                        "            ,%d               "
                        , hsuchiwake_insert[i].fuyo_riyo_kbn
                        , hsuchiwake_insert[i].meisai_no
                        , hsuchiwake_insert[i].point_syubetsu
                        , hsuchiwake_insert[i].riyo_point
                        , hsuchiwake_insert[i].tujo_kikan_gentei_kbn
                        , hsuchiwake_insert[i].point_yukokigen
                        , hsuchiwake_insert[i].kobai_kbn);

                strcat(wk_sql3, wk_sql4);
            }
            sprintf(h_str_sql,
                    " INSERT INTO HSポイント日別内訳情報%s" +
                    " ( " +
                    " システム年月日," +
                    " 顧客番号," +
                    " 処理通番," +
                    " 枝番 " +
                    " %s " +
                    " ) " +
                    " VALUES " +
                    " ( " +
                    " %d, %s, %d, %d " +
                    " %s " +
                    " ) " ,
                    gl_bat_date_yyyymm, wk_sql,
                    h_sysdate, h_ts.vr_kokyaku_no.arr, (long)wk_shori_seq, j+1, wk_sql3 );

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTktskB_insertHSUchiwake *** 登録SQL [%s]\n", h_str_sql);
            /*------------------------------------------------------------*/

            /* 動的ＳＱＬ文を解析する */
//            EXEC SQL PREPARE sql_stat_hsuchiwake from :h_str_sql;
            sqlca.sql = h_str_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                APLOG_WT( "902", 0, null, sqlca.sqlcode,
                        h_str_sql, 0, 0, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTktskB_insertHSUchiwake *** 動的SQL 解析NG = %d\n",
                        sqlca.sqlcode);
                C_DbgEnd(" cmBTktskB_insertHSUchiwake処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return (C_const_NG);
            }

            /* ＳＱＬ文を実行する */
//            EXEC SQL EXECUTE sql_stat_hsuchiwake;
            sqlca.restAndExecute();

            if ( sqlca.sqlcode != C_const_Ora_OK ) {
                sprintf( buff, "顧客番号=[%s]", h_ts.vr_kokyaku_no.arr );
                APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                        "HSポイント日別内訳情報", buff, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTktskB_updTSt *** HSポイント日別内訳情報更新 エラー%s\n", buff);
                C_DbgEnd(" cmBTktskB_insertHSUchiwake処理", 0, 0, 0);
                /*------------------------------------------------------------*/
                return C_const_NG;
            }
        }

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTktskB_insertHSUchiwake処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return  C_const_OK;              /* 処理終了 */
        /*-----cmBTktskB_insertHSUchiwake処理 Bottom-----------------------------*/
    }
    /* 2022/11/29 MCCM初版 ADD END */

/* *****************************************************************************/
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
    /* *****************************************************************************/
    public int daysInMonth( int year, int month )
    {
        int   lastDayInMonth;
        int   wk_month;
        int   wk_year;
        int[] nDaysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        if( DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("daysInMonth処理");
                /*------------------------------------------------------------*/
        }

            wk_month = month;
        wk_year = year;

        if (wk_month > 12) {
            wk_month = month - 12;
            wk_year = year + 1;
        }
        if (( wk_month < 1 ) || ( 12 < wk_month )) {
            lastDayInMonth =  0;
        } else if ( 2 == wk_month  && isLeapYear( wk_year ) == 1) {
            lastDayInMonth = 29;
        } else {
            lastDayInMonth = nDaysInMonth[wk_month - 1];
        }

        if( DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** daysInMonth *** 月末日取得 [%d]\n",lastDayInMonth);
                C_DbgEnd("daysInMonth処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }
        return lastDayInMonth;
    }

/* *****************************************************************************/
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
    /* *****************************************************************************/
    public int isLeapYear( int year )
    {
        int isLeapYear;

        if( DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("isLeapYear処理");
                /*------------------------------------------------------------*/
        }

            isLeapYear = 0;
        isLeapYear = (0 == (year % 400)) || ((0 != (year % 100)) && (0 == (year % 4))) ?0 :1;

        if( DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** isLeapYear *** 閏年判定 [%d]\n",isLeapYear);
                C_DbgEnd("isLeapYear処理", 0, 0, 0);
                /*------------------------------------------------------------*/

        }
        return isLeapYear;
    }


}
