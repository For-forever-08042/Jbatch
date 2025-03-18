package jp.co.mcc.nttdata.batch.business.service.cmBTpclrB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTpclrB.dto.CmBTpclrBServiceImplDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/* ******************************************************************************
 *   プログラム名   ： ポイントクリア（cmBTpclrB）
 *
 *   【処理概要】
 *     失効済みの通常ポイント・期間限定ポイントのポイントクリアを行う。
 *     １日更新最大件数まで処理する。
 *
 *   【引数説明】
 *     -c更新最大件数             :（必須）更新最大件数の設定
 *     -DEBUG(-debug)             :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *     10　　 ：　正常
 *     49　　 ：　正常(更新データなし)
 *     99　　 ：　異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *     40.00 : 2022/10/07 SSI.申：MCCM初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 NTT DATA CORPORATION
 ***************************************************************************** */
@Service
public class CmBTpclrBServiceImpl  extends CmABfuncLServiceImpl implements CmBTpclrBService{
    boolean DBG_LOG = true;                   /* デバッグメッセージ出力             */
    CmBTpclrBServiceImplDto cmBTpclrBServiceImplDto = new CmBTpclrBServiceImplDto();
            /*----------------------------------------------------------------------------*/
            /*  標準include                                                               */
            /*----------------------------------------------------------------------------*/
//            #include    <stdlib.h>
//#include    <stdio.h>
//#include    <memory.h>

    /*----------------------------------------------------------------------------*/
    /*  独自include                                                               */
    /*----------------------------------------------------------------------------*/
//#include    "C_aplcom1.h"               /* 共通関数                           */
//            #include    "BT_aplcom.h"               /* バッチＡＰ業務共通ヘッダファイル   */

    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL INCLUDE  sqlca.h;

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

//    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE  TS_RIYO_KANO_POINT_DATA.h;    /* TS利用可能ポイント情報     */
//    EXEC SQL INCLUDE  TS_KIKAN_POINT_DATA.h;        /* TS期間限定ポイント情報     */
//
//    TS_RIYO_KANO_POINT_TBL  tsriyo_t;       /* TS利用可能ポイント情報バッファ     */
//    TS_RIYO_KANO_POINT_TBL  tsriyo_t_chk;   /* TS利用可能ポイント情報バッファ     */
//    TS_RIYO_KANO_POINT_TBL  tsriyo_t_upd;   /* TS利用可能ポイント情報バッファ     */
//    TS_KIKAN_POINT_TBL      tskikan_t;      /* TS期間限定ポイント情報バッファ     */
//
//    /* 処理用 */
//    unsigned int    hi_bat_date;            /* バッチ処理日(当日)                 */
//    char            hc_Program_Name[10];    /* バージョンなしプログラム名         */
//    char            h_str_sql1[4096];       /* 実行用SQL文字列                    */
//    char            h_str_sql2[4096];       /* 実行用SQL文字列                    */

//    EXEC SQL END DECLARE SECTION;
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
            int DEF_OFF      = 0;                   /* OFF                                */
            int DEF_ON       = 1 ;                  /* ON                                 */
            String DEF_ARG_C =  "-c"  ;              /* 最大取得件数                       */
            String DEF_DEBUG =  "-DEBUG" ;           /* デバッグスイッチ                   */
            String DEF_debug =  "-debug"  ;          /* デバッグスイッチ                   */
            int C_const_APWR = 49 ;                /* 警告終了値                         */
            /*---------------------------------------------*/
            String C_PRGNAME =  "ポイントクリア" ;   /* APログ用機能名                     */
            int C_CREAE_TUJYO_NENDO  = 3 ;  /* 通常ポイントクリア対象年度ｎ年前       */
            int C_CREAE_KIKAN_MONTH  = 8  ; /* 期間限定ポイントクリア対象月ｎヶ月前   */
            int C_const_Ora_SNAP_OLD = -1555;    /* ORA-1555発生                      */
            int C_const_SNAP_OLD     = 55   ;    /* ORA-1555発生戻り値                */
                              /*-----------bit演算用----------------------*/
            int C_TUJO_BASE_BIT =    0x01 ;   /* 通常Ｐ失効フラグベース(00001)          */
            int C_TUJO_CHENGE_BIT=   0x20  ;  /* 通常Ｐ失効フラグ固定箇所（100000)      */
            int C_KIKAN_BASE_BIT  =  0x0001;
                                /* 期間限定Ｐ失効フラグベース(000000000001)       */
            int C_KIKAN_CHENGE_BIT = 0x1000;    /* 期間限定Ｐ失効フラグ固定箇所（1000000000000)   */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    long            gl_max_data_count;      /** 更新最大件数                     **/
    int             chk_arg_c;              /** 引数-cチェック用                 **/
    StringDto            arg_c_Value = new StringDto(256);       /** 引数c設定値                      **/
    StringDto            gc_bat_date = new StringDto(9);         /** バッチ処理日付(当日)             **/


    int gi_tujo_and_bit;     /* ビットＡＮＤ（通常Ｐ）             */
    int gi_kikan_and_bit;    /* ビットＡＮＤ（期間限定Ｐ）         */
    int gi_tujo_xor_bit;     /* ビットＸＯＲ（通常Ｐ）             */
    int gi_kikan_xor_bit;    /* ビットＸＯＲ（期間限定Ｐ）         */
    int    gi_clear_nendo;         /* クリア対象年度                     */
    int    gi_clear_month;         /* クリア対象月                       */
    StringDto    gc_clear_nendo_cd= new StringDto(8);   /* ポイントクリア年度コード           */
    StringDto    gc_clear_month_cd= new StringDto(8);   /* ポイントクリア月ＣＤ               */
    int             gi_tujo_clearflg;       /* クリアフラグ（通常Ｐ）             */
    int             gi_kikan_clearflg;      /* クリアフラグ（期間限定Ｐ）         */
    long            gl_tsriyo_updcnt;       /* TS利用可能ポイント情報更新件数     */
    long            gl_tskikan_updcnt;      /* TS期間限定ポイント情報更新件数     */
    int             gi_updcnt_flg;          /* 更新有無フラグ                     */




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
    public MainResultDto main(int argc, String[] argv )
    {
        int     i_rtn_cd;                           /* 関数戻り値                 */
        IntegerDto i_rtn_status = new IntegerDto();                       /* 関数結果ステータス         */
        int     i_arg_cnt;                          /* 引数チェック用カウンタ     */
        StringDto    c_arg_Work1 = new StringDto(256);                   /* Work Buffer1               */
        StringDto    c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        i_rtn_cd = C_const_OK;              /* 関数戻り値                         */
        gl_tsriyo_updcnt = 0;               /* TS利用可能ポイント情報更新件数     */
        gl_tskikan_updcnt = 0;              /* TS期間限定ポイント情報更新件数     */
        gi_updcnt_flg = 0;                  /* 更新有無フラグ                     */
        memset(c_format_buf, 0x00, sizeof(c_format_buf));

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        i_rtn_cd = C_GetPgname( argv );
        if (i_rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", i_rtn_cd,
                    0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);
        memset(cmBTpclrBServiceImplDto.hc_Program_Name, 0x00, sizeof(cmBTpclrBServiceImplDto.hc_Program_Name));
        /* バージョンなしプログラム名 */
        strcpy(cmBTpclrBServiceImplDto.hc_Program_Name, Cg_Program_Name);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        i_rtn_cd = C_StartBatDbg( argc, argv );
        if (i_rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", i_rtn_cd,
                    0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("*** main処理 ***");
                /*------------------------------------------------------------*/
        }

            /*-------------------------------------*/
            /*  引数チェック処理                   */
            /*-------------------------------------*/
            /* 初期化 */
            chk_arg_c = DEF_OFF;
        gl_max_data_count = 0;
        memset(arg_c_Value, 0x00, sizeof(arg_c_Value));

        for (i_arg_cnt = 1; i_arg_cnt < argc; i_arg_cnt++) {
            memset(c_arg_Work1, 0x00, sizeof(c_arg_Work1));
            strcpy(c_arg_Work1, argv[i_arg_cnt]);

            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", c_arg_Work1);
                        /*------------------------------------------------------------*/
            }
            if (0 == strcmp(c_arg_Work1, DEF_DEBUG) ||
                    0 == strcmp(c_arg_Work1, DEF_debug)) {
                continue;
            } else if (0 == memcmp(c_arg_Work1, DEF_ARG_C, 2)) {    /* -cの場合       */
                /* 引数のチェック */
                i_rtn_cd = cmBTpclrB_Chk_Arg(c_arg_Work1);
                if (i_rtn_cd == C_const_OK) {
                    strcpy(arg_c_Value, c_arg_Work1.arr.substring(2));
                } else {
                    /* バッチデバッグ終了処理 */
                    i_rtn_cd = C_EndBatDbg();
                    return exit( C_const_APNG );
                }
            } else {                                    /* 定義外パラメータ       */
                sprintf(c_format_buf, "定義外の引数（%s）", c_arg_Work1);
                APLOG_WT("910", 0, null, c_format_buf, 0, 0, 0, 0, 0);
                /* バッチデバッグ終了処理 */
                i_rtn_cd = C_EndBatDbg();
                return exit( C_const_APNG );
            }
        }

        /* 必須パラメータ未指定チェック */
        if (chk_arg_c == DEF_OFF) {
            strcpy(c_format_buf, "-c 引数の値が不正です[必須引数なし]");
            APLOG_WT("910", 0, null, c_format_buf, 0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            i_rtn_cd = C_EndBatDbg();
            return exit( C_const_APNG );
        }

        /* グローバル変数(long)にセット */
        gl_max_data_count = atol(arg_c_Value);

        if(DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("引数の値(-c) =[%d]\n", gl_max_data_count);
                /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_SD);
                /*------------------------------------------------------------*/
        }
            i_rtn_status.arr = C_const_OK;          /* 関数結果ステータス                 */
        i_rtn_cd = C_OraDBConnect(C_ORACONN_SD, i_rtn_status);
        if (i_rtn_cd != C_const_OK) {
        if(DBG_LOG){
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** main *** DBコネクトNG rtn=[%d]\n", i_rtn_cd);
                    C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", i_rtn_status);
                    /*------------------------------------------------------------*/
        }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    i_rtn_cd, i_rtn_status, 0, 0, 0);
            i_rtn_cd = C_EndBatDbg();       /* バッチデバッグ終了処理 */
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /*  バッチ処理日取得処理               */
        /*-------------------------------------*/
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得 %s\n", "START");
                /*------------------------------------------------------------*/
        }
            i_rtn_cd = C_GetBatDate(0, gc_bat_date, i_rtn_status);
        if (i_rtn_cd != C_const_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** バッチ処理日取得NG rtn=[%d]\n", i_rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    i_rtn_cd, i_rtn_status, 0, 0, 0);
            i_rtn_cd = C_EndBatDbg();       /* バッチデバッグ終了処理 */
            return exit( C_const_APNG );
        }
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", gc_bat_date);
                /*------------------------------------------------------------*/
        }
            /* バッチ処理日(当日) */
            cmBTpclrBServiceImplDto.hi_bat_date = atoi(gc_bat_date);


        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        i_rtn_cd = cmBTpclrB_main();
        if (i_rtn_cd != C_const_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** cmBTpclrB_main NG rtn=[%d]\n", i_rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "ポイントクリアに失敗しました", 0, 0, 0, 0, 0);
            /* バッチデバッグ終了処理 */
            i_rtn_cd = C_EndBatDbg();
            /* ロールバック   */
//            EXEC SQL ROLLBACK WORK RELEASE;
            sqlca.rollback();
            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("*** main処理 ***", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

            /* バッチデバッグ終了処理 */
            i_rtn_cd = C_EndBatDbg();

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();


        if (gi_updcnt_flg == 0) {
            /* 正常 更新なし */
            i_rtn_cd = C_const_APWR;
        } else {
            /* 正常 更新あり */
            i_rtn_cd = C_const_APOK;
        }

        return exit( i_rtn_cd );
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int    cmBTpclrB_Chk_Arg(char *Arg_in)                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*              １）重複チェック                                              */
    /*              ２）桁数チェック                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              char    * Arg_in    ：(IN)引数値                              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTpclrB_Chk_Arg( StringDto Arg_in)
    {
        StringDto  c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */
        StringDto  c_arg_work= new StringDto(256);                    /* 引数チェック用エリア       */

        if(DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_Chk_Arg処理");
                C_DbgMsg("*** cmBTpclrB_Chk_Arg *** 引数=[%s]\n", Arg_in);
                /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(c_format_buf, 0x00, sizeof(c_format_buf));
        memset(c_arg_work, 0x00, sizeof(c_arg_work));

        if (0 == memcmp(Arg_in, DEF_ARG_C, 2)) {            /* -c更新最大件数         */
            if (chk_arg_c != DEF_OFF) {
                sprintf(c_format_buf, "-c 引数の値が不正です[重複]（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, c_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_c = DEF_ON;

            if (strlen(Arg_in) < 3) {                   /* 桁数チェック           */
                sprintf(c_format_buf, "-c 引数の値が不正です[未指定]（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, c_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            strcpy(c_arg_work, Arg_in.arr.substring(2));
            if (atol(c_arg_work) <= 0) {                /* 数字チェック           */
                sprintf(c_format_buf, "-c 引数の値が不正です[数字以外]（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, c_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if(DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgEnd("cmBTpclrB_Chk_Arg処理", 0, 0, 0);
                /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int cmBTpclrB_main()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ポイントクリア処理(主処理)                                    */
    /*              TS利用可能ポイント情報・TS期間限定ポイント情報の              */
    /*              ポイント数およびポイント失効フラグを更新する。                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int cmBTpclrB_main()
    {
        int       i_rtn_cd;                  /* 関数戻り値                        */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_main処理");
                /*------------------------------------------------------------*/
        }

        /*-------------------------------------------*/
        /*  処理基準年度・月・失効フラグ取得処理     */
        /*-------------------------------------------*/
        cmBTpclrB_getKijunInfo();

        /*-----------------------------------------------*/
        /*  ポイントクリア処理(TS利用可能ポイント情報)   */
        /*-----------------------------------------------*/
        while( true ){
            i_rtn_cd = cmBTpclrB_clearTSRiyokano();
            if (i_rtn_cd == C_const_SNAP_OLD) {
                /* ORA-1555 スナップショットが古すぎる場合 再実行 */
                continue;
            } else if(i_rtn_cd != C_const_OK){
                /* その他の異常の場合 処理終了 */
                APLOG_WT("912", 0, null,
                        "TS利用可能ポイント情報クリア処理に失敗しました",
                        0, 0, 0, 0, 0);
                return C_const_NG;
            } else {
                /* 正常の場合 ループ終了 */
                break;
            }
        }

        /* 更新処理件数出力 */
        APLOG_WT("107", 0, null, "TS利用可能ポイント情報",
                gl_tsriyo_updcnt, 0, 0, 0, 0);
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** TS利用可能ポイント情報 処理件数:[%d]\n",
                        gl_tsriyo_updcnt);
                /*------------------------------------------------------------*/
        }

        if (gl_tsriyo_updcnt > 0) {
            /* 更新有無フラグ 更新あり */
            gi_updcnt_flg = 1;
        }

        /*-----------------------------------------------*/
        /*  ポイントクリア処理(TS期間限定ポイント情報)   */
        /*-----------------------------------------------*/
        while( true){
            i_rtn_cd = cmBTpclrB_clearTSKikangentei();
            if (i_rtn_cd == C_const_SNAP_OLD) {
                /* ORA-1555 スナップショットが古すぎる場合 再実行 */
                continue;
            } else if(i_rtn_cd != C_const_OK){
                /* その他の異常の場合 処理終了 */
                APLOG_WT("912", 0, null,
                        "TS期間限定ポイント情報クリア処理に失敗しました",
                        0, 0, 0, 0, 0);
                return C_const_NG;
            } else {
                /* 正常の場合 ループ終了 */
                break;
            }
        }

        /* 更新処理件数出力 */
        APLOG_WT("107", 0, null, "TS期間限定ポイント情報",
                gl_tskikan_updcnt, 0, 0, 0, 0);
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** TS期間限定ポイント情報 処理件数:[%d]\n",
                        gl_tskikan_updcnt);
                /*------------------------------------------------------------*/
        }

        if (gl_tskikan_updcnt > 0) {
            /* 更新有無フラグ 更新あり */
            gi_updcnt_flg = 1;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpclrB_main処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_getKijunInfo                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTpclrB_getKijunInfo()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              バッチ処理日からポイントクリア処理の基準となる                */
    /*              年度・月・失効フラグの基準値を取得する。                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    public void  cmBTpclrB_getKijunInfo()
    {
        int i_check_flg;         /* bit演算用フラグ                */
        int    i_this_year;            /* バッチ処理日付の年             */
        int    i_this_month;           /* バッチ処理日付の月             */
        int    i_this_nendo;           /* バッチ処理日付の年度           */

        if(DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_getKijunInfo処理");
                /*---------------------------------------------------------------------*/
        }

            /*--------------------------------------*/
            /*  バッチ処理日付                      */
            /*--------------------------------------*/
            i_this_year = cmBTpclrBServiceImplDto.hi_bat_date / 10000;              /* バッチ処理日付の年 */
        i_this_month = (cmBTpclrBServiceImplDto.hi_bat_date / 100) % 100;       /* バッチ処理日付の月 */
        /* バッチ処理日付の年度 */
        if (i_this_month <= 3) {
            /* 年度跨ぎ */
            i_this_nendo = i_this_year - 1;
        } else {
            i_this_nendo = i_this_year;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** cmBTpclrBServiceImplDto.hi_bat_date[%d]\n", cmBTpclrBServiceImplDto.hi_bat_date);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** i_this_year[%d]\n", i_this_year);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** i_this_month[%d]\n", i_this_month);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** i_this_nendo[%d]\n", i_this_nendo);
                /*------------------------------------------------------------*/
        }

            /*--------------------------------------*/
            /*  クリア対象年度・月                  */
            /*--------------------------------------*/
            /* クリア対象年度:３年前*/
            gi_clear_nendo = i_this_nendo - C_CREAE_TUJYO_NENDO;

        /* クリア対象月：４か月前 */
        /* 2021/10/15 クリア対象月：８か月前に変更 */
        if (i_this_month <= C_CREAE_KIKAN_MONTH) {
            /* 年度跨ぎ */
            gi_clear_month = i_this_month - C_CREAE_KIKAN_MONTH + 12;
        } else {
            gi_clear_month = i_this_month - C_CREAE_KIKAN_MONTH;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_clear_nendo[%d]\n",
                        gi_clear_nendo);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_clear_month[%d]\n",
                        gi_clear_month);
                /*------------------------------------------------------------*/
        }

        /*------------------*/
        /* 年度コード取得   */
        /*------------------*/
        memset(gc_clear_nendo_cd, 0x00, sizeof(gc_clear_nendo_cd));
        cmBTpclrB_getYearCd(gi_clear_nendo, gc_clear_nendo_cd);

        /*--------------*/
        /* 月コード取得 */
        /*--------------*/
        memset(gc_clear_month_cd, 0x00, sizeof(gc_clear_month_cd));
        cmBTpclrB_getMonth(gi_clear_month, gc_clear_month_cd);

        /*------------------------------------------*/
        /* 通常Ｐ失効フラグＢＩＴ演算比較対象設定   */
        /*------------------------------------------*/
        /* 初期化処理 */
        i_check_flg = 0;

        /* ビットＸＯＲ演算時計算値*/
        i_check_flg = C_TUJO_BASE_BIT;
        gi_tujo_xor_bit = (i_check_flg << (4 - (gi_clear_nendo % 5)));

        /* ビットＡＮＤ演算時比較値*/
        gi_tujo_and_bit = (gi_tujo_xor_bit | C_TUJO_CHENGE_BIT);

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_tujo_xor_bit[%d]\n",
                        gi_tujo_xor_bit);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_tujo_and_bit[%d]\n",
                        gi_tujo_and_bit);
                /*------------------------------------------------------------*/
        }

            /*----------------------------------------------*/
            /* 期間限定Ｐ失効フラグＢＩＴ演算比較対象設定   */
            /*----------------------------------------------*/
            /* 初期化処理 */
            i_check_flg = 0;

        /* ビットＸＯＲ演算時計算値*/
        i_check_flg = C_KIKAN_BASE_BIT;
        gi_kikan_xor_bit = (i_check_flg << (12 - gi_clear_month));

        /* ビットＡＮＤ演算時比較値*/
        gi_kikan_and_bit = (gi_kikan_xor_bit | C_KIKAN_CHENGE_BIT);

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_kikan_xor_bit[%d]\n",
                        gi_kikan_xor_bit);
                C_DbgMsg("*** cmBTpclrB_getKijunInfo *** gi_kikan_and_bit[%d]\n",
                        gi_kikan_and_bit);
                C_DbgEnd("cmBTpclrB_getKijunInfo処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return;

        /*--- cmBTpclrB_getKijunInfo処理 Bottom --------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_getMonth                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTpclrB_getMonth(unsigned short int i_month,            */
    /*                                      char *month)                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              要求月の全角を取得する。                                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              unsigned short  i_month     ：(IN) 要求月                     */
    /*              char            * c_month   ：(OUT)月（全角）                 */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    public void   cmBTpclrB_getMonth(int i_month, StringDto c_month)
    {
        StringDto    c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_getMonth処理");
                /*------------------------------------------------------------*/
        }

        switch(i_month % 12){
            case 1 : strcpy(c_month, "０１");
                break;
            case 2 : strcpy(c_month, "０２");
                break;
            case 3 : strcpy(c_month, "０３");
                break;
            case 4 : strcpy(c_month, "０４");
                break;
            case 5 : strcpy(c_month, "０５");
                break;
            case 6 : strcpy(c_month, "０６");
                break;
            case 7 : strcpy(c_month, "０７");
                break;
            case 8 : strcpy(c_month, "０８");
                break;
            case 9 : strcpy(c_month, "０９");
                break;
            case 10 : strcpy(c_month, "１０");
                break;
            case 11 : strcpy(c_month, "１１");
                break;
            case 0 : strcpy(c_month, "１２");
                break;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                sprintf(c_format_buf, "要求月：[%d], 格納月:[%s]", i_month, c_month);
                C_DbgMsg("*** cmBTpclrB_getMonth *** %s\n", c_format_buf);
                C_DbgEnd("cmBTpclrB_getMonth処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return;

        /*--- cmBTpclrB_cmBTpclrB_getMonth処理 --------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_getYearCd                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*            void  cmBTpclrB_getYearCd(unsigned short int year,              */
    /*                                      char *year_cd)                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              要求年度の年度コードを取得する。                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              unsigned short int  i_year      ：(IN) 要求年度               */
    /*              char                *c_year_cd  ：(OUT)年度コード             */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */
    /******************************************************************************/
    public void  cmBTpclrB_getYearCd(int i_year, StringDto c_year_cd)
    {
        StringDto    c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_getYearCd処理");
                /*------------------------------------------------------------*/
        }

        /* 年度コードを算出 */
        switch(i_year % 5) {
            case 1 : strcpy(c_year_cd, "１");
                break;
            case 2 : strcpy(c_year_cd, "２");
                break;
            case 3 : strcpy(c_year_cd, "３");
                break;
            case 4 : strcpy(c_year_cd, "４");
                break;
            case 0 : strcpy(c_year_cd, "０");
                break;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                sprintf(c_format_buf, "年度:[%d]、年度コード:[%s]", i_year, c_year_cd);
                C_DbgMsg("*** cmBTpclrB_getYearCd *** %s\n", c_format_buf);
                C_DbgEnd("cmBTpclrB_getYearCd処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return;

        /*--- cmBTpclrB_getYearcd処理 -----------------------------------------------*/
    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_clearTSRiyokano                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static in cmBTpclrB_clearTSRiyokano()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ポイントクリア処理(TS利用可能ポイント情報)                    */
    /*              TS利用可能ポイント情報のポイント数およびポイント失効フラグを  */
    /*              更新する。                                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             55   :  ORA-1555発生                                           */
    /*                                                                            */
    /* **************************************************************************** */
    public int cmBTpclrB_clearTSRiyokano()
    {
        int     i_rtn_cd;                           /* 関数戻り値                 */
        IntegerDto     i_rtn_status = new IntegerDto();                       /* 関数結果ステータス         */
        long    l_max_data_count;                   /* 最大取得件数               */
        StringDto    c_kokyaku_no_buf = new StringDto(15+1);             /* 顧客番号                   */
        StringDto    c_sql_buf= new StringDto(C_const_SQLMaxLen);       /* 動的SQLバッファ            */
        StringDto    c_format_buf= new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_clearTSRiyokano処理");
                /*------------------------------------------------------------*/
        }

            /* 取得件数：引数指定値-更新済み件数（ORA1555再取得のため） */
            l_max_data_count = gl_max_data_count - gl_tsriyo_updcnt;

        sprintf(c_sql_buf,
                "SELECT "+
                " 顧客番号, "+
                " 利用可能通常Ｐ%s, "+
                " 通常Ｐ失効フラグ, "+
                " 利用可能期間限定Ｐ%s, "+
                " 期間限定Ｐ失効フラグ, "+
                " 利用可能通常購買Ｐ%s,       "+        /* 2022/10/07 MCCM初版 ADD */
                " 利用可能通常非購買Ｐ%s,     " +       /* 2022/10/07 MCCM初版 ADD */
                " 利用可能通常その他Ｐ%s,   "   +       /* 2022/10/07 MCCM初版 ADD */
                " 利用可能期間限定購買Ｐ%s,   " +       /* 2022/10/07 MCCM初版 ADD */
                " 利用可能期間限定非購買Ｐ%s, "  +      /* 2022/10/07 MCCM初版 ADD */
                " 利用可能期間限定その他Ｐ%s  "  +      /* 2022/10/07 MCCM初版 ADD */
                " FROM TS利用可能ポイント情報 "+
                " WHERE ( 利用可能通常Ｐ%s <> 0 "+
                " OR BITAND(CAST(通常Ｐ失効フラグ AS INTEGER),%d) = %d "+
                " OR 利用可能期間限定Ｐ%s <> 0 "+
                " OR BITAND(CAST(期間限定Ｐ失効フラグ AS INTEGER),%d) = %d ) "+
                " LIMIT %d "
                ,gc_clear_nendo_cd
                ,gc_clear_month_cd
                ,gc_clear_nendo_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd                         /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd
                ,gi_tujo_and_bit
                ,gi_tujo_and_bit
                ,gc_clear_month_cd
                ,gi_kikan_and_bit
                ,gi_kikan_and_bit
                ,l_max_data_count);
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** SQL=[%s]\n", c_sql_buf);
                /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpclrBServiceImplDto.h_str_sql1, c_sql_buf);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_tsriyo_sel from :h_str_sql1;
        sqlca.sql = cmBTpclrBServiceImplDto.h_str_sql1;
        SqlstmDto sqlca =sqlcaManager.get("CUR_TSRIYO_SEL");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 動的SQL 解析NG = %d\n",
                                sqlca.sqlcode);
                        /*-------------------------------------------------------------*/
            }
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", c_sql_buf, 0, 0);
            return C_const_NG;
        }

        /* TS利用可能ポイント情報取得 */
//        EXEC SQL DECLARE CUR_TSRIYO_SEL CURSOR FOR sql_tsriyo_sel;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_TSRIYO_SEL;
        sqlca.open();
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** " +
                                "TS利用可能ポイント情報 CURSOR OPEN sqlcode =[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
            }
            sprintf(c_format_buf, "クリア対象：通常Ｐ=[%d]年度 期間限定Ｐ=[%d]月",
                    gi_clear_nendo,gi_clear_month);
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode,
                    "TS利用可能ポイント情報", c_format_buf, 0, 0);
            return C_const_NG;
        }

        /* 対象データ数分繰り返す */
        while ( true ){
            /* 初期化 */
//            memset(&tsriyo_t, 0x00, sizeof(tsriyo_t));

//            EXEC SQL FETCH CUR_TSRIYO_SEL
//            INTO :tsriyo_t.kokyaku_no,
//                :tsriyo_t.riyo_kano_tujo_P0,
//                :tsriyo_t.tujo_p_shik_flg,
//                :tsriyo_t.riyo_kano_kikan_gentei_P01,
//                :tsriyo_t.kikan_gentei_p_shik_flg,
//                :tsriyo_t.riyo_kano_tujo_kobai_P0,              /* 2022/10/07 MCCM初版 ADD */
//                :tsriyo_t.riyo_kano_tujo_hikobai_P0,            /* 2022/10/07 MCCM初版 ADD */
//                :tsriyo_t.riyo_kano_tujo_sonota_P0,             /* 2022/10/07 MCCM初版 ADD */
//                :tsriyo_t.riyo_kano_kikan_gentei_kobai_P01,     /* 2022/10/07 MCCM初版 ADD */
//                :tsriyo_t.riyo_kano_kikan_gentei_hikobai_P01,   /* 2022/10/07 MCCM初版 ADD */
//                :tsriyo_t.riyo_kano_kikan_gentei_sonota_P01;    /* 2022/10/07 MCCM初版 ADD */

            sqlca.fetch();
            sqlca.recData(cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_P0,
                    cmBTpclrBServiceImplDto.tsriyo_t.tujo_p_shik_flg,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_P01,
                    cmBTpclrBServiceImplDto.tsriyo_t.kikan_gentei_p_shik_flg,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_kobai_P0,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_hikobai_P0,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_sonota_P0,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_kobai_P01,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_hikobai_P01,
                    cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_sonota_P01 );

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    sqlca.sqlcode != C_const_Ora_SNAP_OLD) {
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** TS利用可能ポイント情報" +
                                        "FETCH NG=[%d]\n", sqlca.sqlcode );
                                /*------------------------------------------------------------*/
                }
                sprintf(c_format_buf, "クリア対象：通常Ｐ=[%d]年度 " +
                        "期間限定Ｐ=[%d]月", gi_clear_nendo, gi_clear_month);
                APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                        "TS利用可能ポイント情報", c_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSRIYO_SEL;
//                sqlca.close();
                sqlcaManager.close("CUR_TSRIYO_SEL");
                return C_const_NG;

            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            } 
        //     else if (sqlca.sqlcode == C_const_Ora_SNAP_OLD) {
        //         /* スナップショットが古すぎる場合 */
        //         sprintf(c_format_buf,
        //                 "DBエラー(FETCH) STATUS=%d(TBL:TS利用可能ポイント情報 KEY:" +
        //                 "クリア対象：通常Ｐ=[%d]年度 期間限定Ｐ=[%d]月",
        //                 C_const_Ora_SNAP_OLD, gi_clear_nendo, gi_clear_month);
        //         APLOG_WT("700", 0, null, c_format_buf, 0, 0, 0, 0, 0);
        //         if(DBG_LOG){
        //                         /*------------------------------------------------------------*/
        //                         C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** %s\n", "ORA-1555発生");
        //                         C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** [%d]件まで更新\n",
        //                                 gl_tsriyo_updcnt);
        //                         /*------------------------------------------------------------*/
        //         }
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSRIYO_SEL;
//                sqlca.close();
        //         sqlcaManager.close("CUR_TSRIYO_SEL");
        //         return C_const_SNAP_OLD;
        //     }

            if(DBG_LOG){
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 顧客番号=[%s]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能通常Ｐ=[%f]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_P0);
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 通常P失効フラグ=[%d]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.tujo_p_shik_flg);
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能期間限定Ｐ=[%f]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_P01);
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 期間限定P失効フラグ=[%d]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.kikan_gentei_p_shik_flg);
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能通常購買Ｐ=[%f]\n",              /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_kobai_P0);                                          /* 2022/10/07 MCCM初版 ADD */
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能通常非購買Ｐ=[%f]\n",            /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_hikobai_P0);                                        /* 2022/10/07 MCCM初版 ADD */
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能通常その他Ｐ=[%f]\n",            /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_tujo_sonota_P0);                                         /* 2022/10/07 MCCM初版 ADD */
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能期間限定購買Ｐ=[%f]\n",          /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_kobai_P01);                                 /* 2022/10/07 MCCM初版 ADD */
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能期間限定非購買Ｐ=[%f]\n",        /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_hikobai_P01);                               /* 2022/10/07 MCCM初版 ADD */
                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 利用可能期間限定その他Ｐ=[%f]\n",        /* 2022/10/07 MCCM初版 ADD */
                        cmBTpclrBServiceImplDto.tsriyo_t.riyo_kano_kikan_gentei_sonota_P01);                                /* 2022/10/07 MCCM初版 ADD */
                /*--------------------------------------------------------------------*/
            }

            /*--------------*/
            /*  更新処理    */
            /*--------------*/
            /* 顧客番号 */
            memset(c_kokyaku_no_buf, 0x00, sizeof(c_kokyaku_no_buf));
            strncpy(c_kokyaku_no_buf, cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr(),
                    cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.len );
            /* 顧客ロック処理 */
            i_rtn_cd = C_KdataLock(c_kokyaku_no_buf, "1", i_rtn_status);
            if (i_rtn_cd != C_const_OK) {
            if(DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTpclrB_clearTSRiyokano *** 顧客ロックNG=[%s]\n",
                                    c_kokyaku_no_buf);
                            /*------------------------------------------------------------*/
            }
                APLOG_WT("903", 0, null, "C_KdataLock(1)",
                        i_rtn_cd, i_rtn_status,
                        0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSRIYO_SEL;
                sqlcaManager.close("CUR_TSRIYO_SEL");
                return C_const_NG;
            }

            /* TS利用可能ポイント情報チェック処理 */
            i_rtn_cd = cmBTpclrB_chkTSRiyokano();
            if (i_rtn_cd != C_const_OK) {
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano ***" +
                                        "TS利用可能ポイント情報チェック処理 NG=[%d]\n", i_rtn_cd);
                                /*------------------------------------------------------------*/
                }
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSRIYO_SEL;
                sqlcaManager.close("CUR_TSRIYO_SEL");
                return C_const_NG;
            }

            /* TS利用可能ポイント情報更新処理 */
            i_rtn_cd = cmBTpclrB_updTSRiyokano();
            if (i_rtn_cd != C_const_OK) {
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSRiyokano ***" +
                                        "TS利用可能ポイント情報更新処理 NG=[%d]\n", i_rtn_cd);
                                /*------------------------------------------------------------*/
                }
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSRIYO_SEL;
                sqlcaManager.close("CUR_TSRIYO_SEL");
                return C_const_NG;
            }

            /* コミット */
//            EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* TS利用可能ポイント情報更新件数 */
            gl_tsriyo_updcnt++;

        } /* FETCH LOOP */

        /* カーソルクローズ */
//        EXEC SQL  CLOSE CUR_TSRIYO_SEL;
        sqlcaManager.close("CUR_TSRIYO_SEL");

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpclrB_clearTSRiyokano処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_chkTSRiyokano                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int cmBTpclrB_chkTSRiyokano()                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              TS利用可能ポイント情報から最新の失効フラグを取得し、          */
    /*              未失効／失効済みを確認する。                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int cmBTpclrB_chkTSRiyokano()
    {
        StringDto   c_sql_buf = new StringDto(C_const_SQLMaxLen);       /* 動的SQLバッファ            */
        StringDto   c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        if(DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("*** cmBTpclrB_chkTSRiyokano処理 ***");
                /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(c_sql_buf, 0x00, sizeof(c_sql_buf));
//        memset(h_str_sql2, 0x00, sizeof(h_str_sql2));
//        memset(tsriyo_t_chk, 0x00, sizeof(tsriyo_t_chk));
//        memset(tsriyo_t_upd, 0x00, sizeof(tsriyo_t_upd));

        /* TS利用可能ポイント情報取得 */
        sprintf(c_sql_buf,
                "SELECT "+
                " 顧客番号, "+
                " 利用可能通常Ｐ%s, "+
                " 通常Ｐ失効フラグ, "+
                " 利用可能期間限定Ｐ%s, "+
                " 期間限定Ｐ失効フラグ "+
                "FROM  TS利用可能ポイント情報 "+
                "WHERE 顧客番号= ? "
                ,gc_clear_nendo_cd
                ,gc_clear_month_cd);

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 抽出SQL [%s]\n", c_sql_buf);
                /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpclrBServiceImplDto.h_str_sql2, c_sql_buf);

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_tsriyo_chk from :h_str_sql2;
        sqlca.sql = cmBTpclrBServiceImplDto.h_str_sql2;
        SqlstmDto sqlca =sqlcaManager.get("CUR_TSRIYO_CHK");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(c_format_buf, "顧客番号=[%s]", cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "SELECT(PREPARE)", sqlca.sqlcode,
                    "TS利用可能ポイント情報", c_format_buf, 0, 0);
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 動的SQL 解析NG = %d\n",
                                sqlca.sqlcode);
                        C_DbgEnd("cmBTpclrB_chkTSRiyokano", 0, 0, 0);
                        /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_TSRIYO_CHK CURSOR FOR sql_tsriyo_chk;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_TSRIYO_CHK USING :tsriyo_t.kokyaku_no;
        sqlca.open(cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** " +
                                "TS利用可能ポイント情報 CURSOR OPEN sqlcode =[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("904", 0, null, "OPEN CURSOR(CUR_TSRIYO_CHK)",
                    sqlca.sqlcode, "TS利用可能ポイント情報情報",
                    C_PRGNAME, 0, 0);
            return C_const_NG;
        }
        /* カーソルフェッチ */
//        EXEC SQL FETCH CUR_TSRIYO_CHK
//        INTO :tsriyo_t_chk.kokyaku_no,
//            :tsriyo_t_chk.riyo_kano_tujo_P0,
//            :tsriyo_t_chk.tujo_p_shik_flg,
//            :tsriyo_t_chk.riyo_kano_kikan_gentei_P01,
//            :tsriyo_t_chk.kikan_gentei_p_shik_flg;

        sqlca.fetch();
        sqlca.recData(cmBTpclrBServiceImplDto.tsriyo_t_chk.kokyaku_no,
                cmBTpclrBServiceImplDto.tsriyo_t_chk.riyo_kano_tujo_P0,
                cmBTpclrBServiceImplDto.tsriyo_t_chk.tujo_p_shik_flg,
                cmBTpclrBServiceImplDto.tsriyo_t_chk.riyo_kano_kikan_gentei_P01,
                cmBTpclrBServiceImplDto.tsriyo_t_chk.kikan_gentei_p_shik_flg);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** TS利用可能ポイント情報" +
                                "FETCH NG=[%d]\n", sqlca.sqlcode );
                        /*------------------------------------------------------------*/
            }
            sprintf(c_format_buf, "顧客番号=%s", cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "FETCH(CUR_TSRIYO_CHK)",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    c_format_buf, 0, 0);
            /* カーソルクローズ */
//            EXEC SQL CLOSE CUR_TSRIYO_CHK;
//            sqlca.close();
            sqlcaManager.close("CUR_TSRIYO_CHK");
            return C_const_NG;
        }

        /* カーソルクローズ */
//        EXEC SQL CLOSE CUR_TSRIYO_CHK;
//        sqlca.close();

        sqlcaManager.close("CUR_TSRIYO_CHK");

        if(DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 顧客番号=[%s]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t_chk.kokyaku_no.arr);
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 利用可能通常P=[%f]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t_chk.riyo_kano_tujo_P0);
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 通常Ｐ失効フラグ=[%d]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t_chk.tujo_p_shik_flg);
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 利用可能期間限定P=[%f]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t_chk.riyo_kano_kikan_gentei_P01);
                C_DbgMsg("*** cmBTpclrB_chkTSRiyokano *** 期間限定Ｐ失効フラグ=[%d]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t_chk.kikan_gentei_p_shik_flg);
                C_DbgEnd("cmBTpclrB_chkTSRiyokano", 0, 0, 0);
                /*---------------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  通常Ｐ失効フラグチェック                     */
        /*-----------------------------------------------*/
        if ((cmBTpclrBServiceImplDto.tsriyo_t_chk.tujo_p_shik_flg.intVal() & gi_tujo_and_bit) == gi_tujo_and_bit) {
            /* 通常Ｐ失効フラグの対象年度：1(ON)失効フラグが立っている場合*/
            /* 0(OFF)クリア */
            //TODO
//            tsriyo_t_upd.tujo_p_shik_flg =
//                    tsriyo_t_chk.tujo_p_shik_flg ^ gi_tujo_xor_bit;
            /* クリアフラグ（通常Ｐ）:ON    */
            gi_tujo_clearflg = DEF_ON;
        } else {
            /* 通常Ｐ失効フラグの対象年度:0(OFF)失効フラグが立っていない場合*/
            /* 現在値 */
            cmBTpclrBServiceImplDto.tsriyo_t_upd.tujo_p_shik_flg = cmBTpclrBServiceImplDto.tsriyo_t_chk.tujo_p_shik_flg;
            /* クリアフラグ（通常Ｐ）:OFF   */
            gi_tujo_clearflg = DEF_OFF;
        }

        /*-----------------------------------------------*/
        /*  期間限定Ｐ失効フラグチェック                 */
        /*-----------------------------------------------*/
        if ((cmBTpclrBServiceImplDto.tsriyo_t_chk.kikan_gentei_p_shik_flg.intVal() & gi_kikan_and_bit)
                == gi_kikan_and_bit) {
            /* 期間限定Ｐ失効フラグの対象年度：1(ON)失効フラグが立っている場合*/
            /* 0(OFF)クリア */
            //TODO
//            tsriyo_t_upd.kikan_gentei_p_shik_flg =
//                    tsriyo_t_chk.kikan_gentei_p_shik_flg ^ gi_kikan_xor_bit;
            /* クリアフラグ（期間限定Ｐ）:ON    */
            gi_kikan_clearflg = DEF_ON;
        } else {
            /* 期間限定Ｐ失効フラグの対象年度:0(OFF)失効フラグが立っていない場合*/
            /* 現在値 */
            cmBTpclrBServiceImplDto.tsriyo_t_upd.kikan_gentei_p_shik_flg =
                    cmBTpclrBServiceImplDto.tsriyo_t_chk.kikan_gentei_p_shik_flg;
            /* クリアフラグ（期間限定Ｐ）:OFF   */
            gi_kikan_clearflg = DEF_OFF;
        }

        /* 処理を終了する */
        return C_const_OK;

        /*--- cmBTpclrB_chkTSRiyokano Bottom -----------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_updTSRiyokano                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int cmBTpclrB_updTSRiyokano()                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              TS利用可能ポイント情報の更新を行う。                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int cmBTpclrB_updTSRiyokano()
    {
        StringDto      c_sql_buf = new StringDto(C_const_SQLMaxLen);     /* 動的SQLバッファ            */
        StringDto      c_format_buf = new StringDto(C_const_MsgMaxLen);  /* APログフォーマット         */
        StringDto      c_buf = new StringDto(C_const_MsgMaxLen);         /* 動的SQL編集バッファ        */

if(DBG_LOG){
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTpclrB_updTSRiyokano処理");
        /*------------------------------------------------------------*/
}

        /* 初期化*/
        memset(c_sql_buf, 0x00, sizeof(c_sql_buf));
//        memset(h_str_sql2, 0x00, sizeof(h_str_sql2));

        /* ＳＱＬ文をセットする */
        sprintf(c_sql_buf,
                "UPDATE TS利用可能ポイント情報 " +
                "SET 利用可能通常Ｐ%s = 利用可能通常Ｐ%s+(利用可能通常Ｐ%s *(-1)),"+
                " 利用可能期間限定Ｐ%s = "+
                "利用可能期間限定Ｐ%s+(利用可能期間限定Ｐ%s *(-1)),"+
                "利用可能通常購買Ｐ%s = 利用可能通常購買Ｐ%s+(利用可能通常購買Ｐ%s *(-1)),"  +                   /* 2022/10/07 MCCM初版 ADD */
                "利用可能通常非購買Ｐ%s = 利用可能通常非購買Ｐ%s+(利用可能通常非購買Ｐ%s *(-1))," +              /* 2022/10/07 MCCM初版 ADD */
                "利用可能通常その他Ｐ%s = 利用可能通常その他Ｐ%s+(利用可能通常その他Ｐ%s *(-1))," +              /* 2022/10/07 MCCM初版 ADD */
                "利用可能期間限定購買Ｐ%s = 利用可能期間限定購買Ｐ%s+(利用可能期間限定購買Ｐ%s *(-1))," +        /* 2022/10/07 MCCM初版 ADD */
                "利用可能期間限定非購買Ｐ%s = 利用可能期間限定非購買Ｐ%s+(利用可能期間限定非購買Ｐ%s *(-1))," +  /* 2022/10/07 MCCM初版 ADD */
                "利用可能期間限定その他Ｐ%s = 利用可能期間限定その他Ｐ%s+(利用可能期間限定その他Ｐ%s *(-1)),"   /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd ,gc_clear_nendo_cd ,gc_clear_nendo_cd
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd
                ,gc_clear_nendo_cd ,gc_clear_nendo_cd ,gc_clear_nendo_cd                                        /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd ,gc_clear_nendo_cd ,gc_clear_nendo_cd                                        /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_nendo_cd ,gc_clear_nendo_cd ,gc_clear_nendo_cd                                        /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd                                        /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd                                        /* 2022/10/07 MCCM初版 ADD */
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd);                                      /* 2022/10/07 MCCM初版 ADD */

        /* 通常Ｐ失効フラグ更新ありの場合 */
        if (gi_tujo_clearflg == DEF_ON) {
            sprintf(c_buf, " 通常Ｐ失効フラグ = %d,", cmBTpclrBServiceImplDto.tsriyo_t_upd.tujo_p_shik_flg);
            strcat(c_sql_buf, c_buf);
        }

        /* 期間限定Ｐ失効フラグ更新ありの場合 */
        if (gi_kikan_clearflg == DEF_ON) {
            sprintf(c_buf, " 期間限定Ｐ失効フラグ = %d,",
                    cmBTpclrBServiceImplDto.tsriyo_t_upd.kikan_gentei_p_shik_flg);
            strcat(c_sql_buf, c_buf);
        }

        strcat(c_sql_buf,
                " 最終更新日      = ?,"+
                " 最終更新日時 = SYSDATE(),"+
                " 最終更新プログラムＩＤ = ? "+
                " WHERE 顧客番号 = ?");

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_updTSRiyokano *** 更新SQL [%s]\n", c_sql_buf);
                /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpclrBServiceImplDto.h_str_sql2, c_sql_buf);

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_tsriyo_upd from :h_str_sql2;
        sqlca.sql = cmBTpclrBServiceImplDto.h_str_sql2;
        sqlca.prepare();

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf(c_format_buf, "顧客番号=[%s]", cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE(PREPARE)", sqlca.sqlcode,
                    "TS利用可能ポイント情報", c_format_buf, 0, 0);
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_updTSRiyokanot *** 動的SQL 解析NG = %d\n",
                                sqlca.sqlcode);
                        C_DbgEnd("cmBTpclrB_updTSRiyokanot", 0, 0, 0);
                        /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        /* 動的ＳＱＬ文を実行する */
//        EXEC SQL EXECUTE sql_tsriyo_upd
//        USING   :hi_bat_date,
//                     :hc_Program_Name,
//                     :tsriyo_t.kokyaku_no;
        sqlca.restAndExecute(cmBTpclrBServiceImplDto.hi_bat_date, cmBTpclrBServiceImplDto.hc_Program_Name, cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no);

        /* エラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(c_format_buf, "顧客番号=[%s]", cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS利用可能ポイント情報", c_format_buf, 0, 0);
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_updTSRiyokanot *** 動的SQL 更新NG = %d\n",
                                sqlca.sqlcode);
                        C_DbgEnd("cmBTpclrB_updTSRiyokanot", 0, 0, 0);
                        /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_updTSRiyokanot *** 更新完了 顧客番号:[%s]\n",
                        cmBTpclrBServiceImplDto.tsriyo_t.kokyaku_no.arr);
                C_DbgEnd(" cmBTpclrB_updTSRiyokano処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*-----cmBTpclrB_updTSRiyokano Bottom-----------------------------------------*/
    }

/* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_clearTSKikangentei                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static in cmBTpclrB_clearTSKikangentei()                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ポイントクリア処理(TS期間限定ポイント情報)                    */
    /*              TS期間限定ポイント情報のポイント数を更新する。                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*             55   :  ORA-1555発生                                           */
    /*                                                                            */
    /* **************************************************************************** */
    public int cmBTpclrB_clearTSKikangentei()
    {
        int     i_rtn_cd;                           /* 関数戻り値                 */
        IntegerDto     i_rtn_status = new IntegerDto();                       /* 関数結果ステータス         */
        long    l_max_data_count;                   /* 最大取得件数               */
        StringDto    c_kokyaku_no_buf = new StringDto(15+1);             /* 顧客番号                   */
        StringDto    c_sql_buf = new StringDto(C_const_SQLMaxLen);       /* 動的SQLバッファ            */
        StringDto    c_format_buf = new StringDto(C_const_MsgMaxLen);    /* APログフォーマット         */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_clearTSKikangentei処理");
                /*------------------------------------------------------------*/
        }

            /* 取得件数：引数指定値-更新済み件数（ORA1555再取得のため） */
            l_max_data_count = gl_max_data_count - gl_tskikan_updcnt;

        sprintf(c_sql_buf,
                "SELECT " +
                " 顧客番号, " +
                " 付与期間限定Ｐ%s, " +
                " 利用期間限定Ｐ%s " +
                " FROM TS期間限定ポイント情報 " +
                " WHERE (付与期間限定Ｐ%s <> 0 " +
                " OR 利用期間限定Ｐ%s <> 0 ) " +
                " LIMIT %d "
                ,gc_clear_month_cd
                ,gc_clear_month_cd
                ,gc_clear_month_cd
                ,gc_clear_month_cd
                ,l_max_data_count);
        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** SQL=[%s]\n", c_sql_buf);
                /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpclrBServiceImplDto.h_str_sql1, c_sql_buf);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_tskikan_sel from :h_str_sql1;
        sqlca.sql = cmBTpclrBServiceImplDto.h_str_sql1;
        SqlstmDto sqlca =sqlcaManager.get("CUR_TSKIKAN_SEL");
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** 動的SQL 解析NG = %d\n",
                                sqlca.sqlcode);
                        /*-------------------------------------------------------------*/
            }
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "TS期間限定ポイント情報", c_sql_buf, 0, 0);
            return C_const_NG;
        }

        /* TS期間限定ポイント情報取得 */
//        EXEC SQL DECLARE CUR_TSKIKAN_SEL CURSOR FOR sql_tskikan_sel;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN CUR_TSKIKAN_SEL;
        sqlca.open();
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** " +
                                "TS期間限定ポイント情報 CURSOR OPEN sqlcode =[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
            }
            sprintf(c_format_buf, "クリア対象：期間限定Ｐ=[%d]月", gi_clear_month);
            APLOG_WT("904", 0, null, "OPEN", sqlca.sqlcode,
                    "TS期間限定ポイント情報", c_format_buf, 0, 0);
            return C_const_NG;
        }

        /* 対象データ数分繰り返す */
        while ( true ){
            /* 初期化 */
//            memset(&tskikan_t, 0x00, sizeof(tskikan_t));

//            EXEC SQL FETCH CUR_TSKIKAN_SEL
//            INTO :tskikan_t.kokyaku_no,
//                :tskikan_t.fuyo_kikan_gentei_point01,
//                :tskikan_t.riyo_kikan_gentei_point01;

            sqlca.fetchInto(cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no, cmBTpclrBServiceImplDto.tskikan_t.fuyo_kikan_gentei_point01, cmBTpclrBServiceImplDto.tskikan_t.riyo_kikan_gentei_point01);

            if (sqlca.sqlcode != C_const_Ora_OK &&
                    sqlca.sqlcode != C_const_Ora_NOTFOUND &&
                    sqlca.sqlcode != C_const_Ora_SNAP_OLD) {
            if(DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** " +
                                    "TS期間限定ポイント情報 FETCH NG=[%d]\n", sqlca.sqlcode );
                            /*------------------------------------------------------------*/
            }
                sprintf(c_format_buf, "クリア対象：期間限定Ｐ=[%d]月",
                        gi_clear_month);
                APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                        "TS期間限定ポイント情報", c_format_buf, 0, 0);

                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSKIKAN_SEL;
//                sqlca.close();
                sqlcaManager.close("CUR_TSKIKAN_SEL");
                return C_const_NG;

            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            } else if (sqlca.sqlcode == C_const_Ora_SNAP_OLD) {
                /* スナップショットが古すぎる場合 */
                sprintf(c_format_buf,
                        "DBエラー(FETCH) STATUS=%d(TBL:TS利用可能ポイント情報 KEY:"+
                        "クリア対象：期間限定Ｐ=[%d]月",
                        C_const_Ora_SNAP_OLD, gi_clear_month);
                APLOG_WT("700", 0, null, c_format_buf, 0, 0, 0, 0, 0);
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** %s\n",
                                        "ORA-1555発生");
                                C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** [%d]件まで更新\n",
                                        gl_tskikan_updcnt);
                                /*------------------------------------------------------------*/
                }
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSKIKAN_SEL;
                sqlcaManager.close("CUR_TSKIKAN_SEL");
                return C_const_SNAP_OLD;
            }

            if(DBG_LOG){
                        /*--------------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** 顧客番号=[%s]\n",
                                cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.arr);
                        C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** 付与期間限定Ｐ=[%f]\n",
                                cmBTpclrBServiceImplDto.tskikan_t.fuyo_kikan_gentei_point01);
                        C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** 利用期間限定Ｐ=[%f]\n",
                                cmBTpclrBServiceImplDto.tskikan_t.riyo_kikan_gentei_point01);
                        /*--------------------------------------------------------------------*/
            }

            /*--------------*/
            /*  更新処理    */
            /*--------------*/
            /* 顧客番号 */
            memset(c_kokyaku_no_buf, 0x00, sizeof(c_kokyaku_no_buf));
            strncpy(c_kokyaku_no_buf, cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.arr(),
                    cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.len );
            /* 顧客ロック処理 */
            i_rtn_cd = C_KdataLock(c_kokyaku_no_buf, "1", i_rtn_status);
            if (i_rtn_cd != C_const_OK) {
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSKikangentei *** 顧客ロックNG=[%s]\n",
                                        c_kokyaku_no_buf);
                                /*------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_KdataLock(1)",
                        i_rtn_cd, i_rtn_status,
                        0, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSKIKAN_SEL;
                sqlcaManager.close("CUR_TSKIKAN_SEL");
                return C_const_NG;
            }

            /* TS期間限定ポイント情報更新処理 */
            i_rtn_cd = cmBTpclrB_updTSKikangentei();
            if (i_rtn_cd != C_const_OK) {
                if(DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTpclrB_clearTSKikangentei ***" +
                                        "TS期間限定ポイント情報更新処理 NG=[%d]\n", i_rtn_cd);
                                /*------------------------------------------------------------*/
                }
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_TSKIKAN_SEL;
                sqlcaManager.close("CUR_TSKIKAN_SEL");
                return C_const_NG;
            }

            /* コミット */
//            EXEC SQL COMMIT WORK;
           sqlcaManager.commit();

            /* TS期間限定ポイント情報更新件数 */
            gl_tskikan_updcnt++;

        } /* FETCH LOOP */

        /* カーソルクローズ */
//        EXEC SQL  CLOSE CUR_TSKIKAN_SEL;
        sqlcaManager.close("CUR_TSKIKAN_SEL");

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTpclrB_clearTSKikangentei処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTpclrB_updTSKikangentei                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int cmBTpclrB_updTSKikangentei()                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              TS期間限定ポイント情報の更新を行う。                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int cmBTpclrB_updTSKikangentei()
    {
        StringDto     c_sql_buf = new StringDto(C_const_SQLMaxLen);     /* 動的SQLバッファ            */
        StringDto     c_format_buf = new StringDto(C_const_MsgMaxLen);  /* APログフォーマット         */

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTpclrB_updTSKikangentei処理");
                /*------------------------------------------------------------*/
        }

        /* 初期化*/
        memset(c_sql_buf, 0x00, sizeof(c_sql_buf));
//        memset(h_str_sql2, 0x00, sizeof(h_str_sql2));

        /* ＳＱＬ文をセットする */
        sprintf(c_sql_buf,
                "UPDATE TS期間限定ポイント情報 " +
                "SET 付与期間限定Ｐ%s = 付与期間限定Ｐ%s+(付与期間限定Ｐ%s *(-1))," +
                " 利用期間限定Ｐ%s = 利用期間限定Ｐ%s+(利用期間限定Ｐ%s *(-1))," +
                " バッチ更新日 = ?," +
                " 最終更新日 = ?," +
                " 最終更新日時 = SYSDATE()," +
                " 最終更新プログラムＩＤ = ? " +
                " WHERE 顧客番号 = ?"
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd
                ,gc_clear_month_cd ,gc_clear_month_cd ,gc_clear_month_cd);

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_updTSKikangentei *** 更新SQL [%s]\n", c_sql_buf);
                /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTpclrBServiceImplDto.h_str_sql2, c_sql_buf);

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_tskikan_upd from :h_str_sql2;
        sqlca.sql = cmBTpclrBServiceImplDto.h_str_sql2;
        sqlca.prepare();

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            sprintf(c_format_buf, "顧客番号=[%s]", cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE(PREPARE)", sqlca.sqlcode,
                    "TS期間限定ポイント情報", c_format_buf, 0, 0);
if(DBG_LOG){
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTpclrB_updTSKikangentei *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTpclrB_updTSKikangentei", 0, 0, 0);
            /*------------------------------------------------------------*/
}
            return C_const_NG;
        }

        /* 動的ＳＱＬ文を実行する */
//        EXEC SQL EXECUTE sql_tskikan_upd
//        USING   :hi_bat_date,
//                     :hi_bat_date,
//                     :hc_Program_Name,
//                     :tskikan_t.kokyaku_no;
        sqlca.restAndExecute(cmBTpclrBServiceImplDto.hi_bat_date, cmBTpclrBServiceImplDto.hi_bat_date, cmBTpclrBServiceImplDto.hc_Program_Name, cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no);

        /* エラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(c_format_buf, "顧客番号=[%s]", cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS期間限定ポイント情報", c_format_buf, 0, 0);
            if(DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTpclrB_updTSKikangentei *** 動的SQL 更新NG = %d\n",
                                sqlca.sqlcode);
                        C_DbgEnd("cmBTpclrB_updTSKikangentei", 0, 0, 0);
                        /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if(DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTpclrB_updTSKikangentei *** 更新完了 顧客番号:[%s]\n",
                        cmBTpclrBServiceImplDto.tskikan_t.kokyaku_no.arr);
                C_DbgEnd(" cmBTpclrB_updTSKikangentei処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*-----cmBTpclrB_updTSKikangentei Bottom--------------------------------------*/
    }
}
