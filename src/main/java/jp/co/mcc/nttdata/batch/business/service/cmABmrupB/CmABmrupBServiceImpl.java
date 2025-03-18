package jp.co.mcc.nttdata.batch.business.service.cmABmrupB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.springframework.stereotype.Service;

import java.nio.file.FileStore;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;

/*******************************************************************************
 *   プログラム名   ： 当日更新分反映処理（cmABmrupB）
 *
 *   【処理概要】
 *       下記テーブルについて、当日更新されたレコードのUPDATE/INSERTを行う。
 *       MM顧客情報
 *       MM顧客属性情報
 *       +MM顧客企業別属性情報
 * 2023/05/29 MCCMPH2 ADD START *
 *       MMお届け先情報
 * 2023/05/29 MCCMPH2 ADD END *
 * 2022/11/25 MCCM初版 DEL START *
 *       //+MMマタニティベビー情報
 * 2022/11/25 MCCM初版 DEL END *
 *       MS顧客制度情報
 * 2022/11/25 MCCM初版 DEL START *
 *       //MSサークル管理情報
 *       //MSサークル顧客情報
 * 2022/11/25 MCCM初版 DEL END *
 *       MSカード情報
 *       MS家族制度情報
 *       TSポイント年別情報
 *       +TS利用可能ポイント情報
 * 2022/11/25 MCCM初版 ADD START *
 *       MS外部認証情報
 *       TSランク情報
 * 2022/11/25 MCCM初版 ADD END *
 *
 *   【引数説明】
 *  -t対象テーブル名  : 対象テーブル名
 *  -d処理対象日付    : 処理対象日付
 *  -uユーザ          : ユーザ
 *  -pパスワード      : パスワード
 *  -sSID             : SID
 *  -o出力ファイル名  : 出力ファイル名
 *  -DEBUG(-debug)    : デバッグモードでの実行
 *                      （標準出力に出力される）
 *
 *   【戻り値】
 *      10     ： 正常
 *      88     ： 引数エラー
 *      77     ： 出力ファイルオープンエラー
 *      99     ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 : 2012/12/20 SSI.Suyama：初版
 *      2.00 : 2013/08/20 SSI.本田  ：TS利用可能ポイント情報のカラム追加
 *      3.00 : 2014/03/29 SSI.上野  ：TS利用可能ポイント情報のカラム追加
 *      4.00 : 2014/07/10 SSI.吉田  ：MS顧客制度情報のカラム追加
 *      5.00 : 2015/04/15 SSI.上野  ：MM顧客企業別属性情報のカラム追加
 *      6.00 : 2016/03/28 SSI.石戸  ：MM顧客属性情報、MS顧客制度情報の項目追加
 *      7.00 : 2016/08/08 SSI.田頭  ：MM顧客属性情報の項目追加
 *      8.00 : 2020/12/22 NBS.亀谷  ：TS利用可能ポイント情報のカラム追加
 *     40.00 : 2022/11/25 SSI.川内  ：MCCM初版
 *     41.00 : 2023/05/29 SSI.石    ：MCCMPH2
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmABmrupBServiceImpl extends CmABfuncLServiceImpl implements CmABmrupBService {

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    ItemDto bt_date = new ItemDto(9);             /* バッチ処理日付              */
    /* 2022/11/25 MCCM初版 MOD START */
    //char             h_pid[16];              /* 会員番号                    */
    ItemDto h_pid = new ItemDto(19);              /* 会員番号                    */
    /* 2022/11/25 MCCM初版 MOD END */
    ItemDto h_uid = new ItemDto(16);              /* 顧客番号                    */
    ItemDto h_kigyo_cd = new ItemDto();             /* 企業コード                  */
    /* 2023/05/29 MCCMPH2 ADD START */
    ItemDto otodokesaki_no = new ItemDto();          /* お届け先番号                */
    /* 2023/05/29 MCCMPH2 ADD START */
    ItemDto h_svc = new ItemDto(3);               /* サービス種別                */
    /* 2022/11/25 MCCM初版 MOD START */
    //char             h_kazokuid[8];          /* 家族ＩＤ                    */
    ItemDto h_kazokuid = new ItemDto(11);          /* 家族ＩＤ                    */
    /* 2022/11/25 MCCM初版 MOD END */
    /* 2022/11/25 MCCM初版 DEL START */
    //unsigned int     h_circle_id;            /* サークルＩＤ                */
    //unsigned int     h_circle_store;         /* サークル有効店番号          */
    /* 2022/11/25 MCCM初版 DEL END */
    ItemDto h_year = new ItemDto();                 /* 年                          */
    /* 2022/11/25 MCCM初版 ADD START */
    ItemDto h_gaibuninsho_status = new ItemDto(2);/* 外部認証種別                */
    ItemDto h_gaibuninsho_id = new ItemDto(256);  /* 外部認証ID                  */
    /* 2022/11/25 MCCM初版 ADD END */
    ItemDto ORAUSR = new ItemDto(32);             /* 接続用ユーザ                */
    ItemDto PASSWD = new ItemDto(32);             /* 接続用パスワード            */
    ItemDto ORASID = new ItemDto(32);             /* 接続用SID                   */
    ItemDto h_rowid = new ItemDto(18);            /* ROWID                       */
    ItemDto str_sql = new ItemDto(8192);          /* 実行用SQL文字列             */

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;       /* OFF                         */
    int DEF_ON  = 1;       /* ON                          */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_T = "-t";            /* 対象テーブル名                 */
    String DEF_ARG_D = "-d";            /* 処理対象日付                   */
    String DEF_ARG_U = "-u";            /* ユーザ                         */
    String DEF_ARG_P = "-p";            /* パスワード                     */
    String DEF_ARG_S = "-s";            /* SID                            */
    String DEF_ARG_O = "-o";            /* 出力ファイル名                 */
    String DEF_DEBUG = "-DEBUG";        /* デバッグスイッチ               */
    String DEF_debug = "-debug";        /* デバッグスイッチ               */
    /*---------------------------------------------*/
    int R_const_OK         =    0;   /* 関数戻り値（正常）             */
    int R_const_NG         =    1;   /* 関数戻り値（異常）             */
    int R_const_APOK       =   10;   /* プログラム戻り値（正常）       */
    int R_const_APNG       =   99;   /* プログラム戻り値（異常）       */
    int R_const_APPRMNG    =   88;   /* プログラム戻り値（引数エラー） */
    int R_const_APFILENG   =   77;   /* プログラム戻り値               */
    /* （出力ファイルオープンエラー） */
    int R_const_Ora_OK       =  0;   /* オラクル処理ＯＫ               */
    int R_const_Ora_NOTFOUND =  1403;   /* オラクル処理該当データなし     */
    int R_const_SQLMaxLen    =  8192;   /* SQL文文字列最大サイズ          */
    int UMU_ARI              =  1;       /* データ有無 - 有                */
    int UMU_NASI             =  0;       /* データ有無 - 無                */
    int COMMIT_CHK           =  50000;       /* コミット件数                   */

    String MMINFO_TBL = "MM顧客情報";             /* MM顧客情報 */
    String MMZOKU_TBL = "MM顧客属性情報";         /* MM顧客属性情報 */
    String MMKIGY_TBL = "MM顧客企業別属性情報";   /* MM顧客企業別属性情報 */
    /* 2023/05/29 MCCMPH2 ADD START */
    String MMOTODOKE_TBL = "MMお届け先情報";   /* MMお届け先情報 */
    /* 2023/05/29 MCCMPH2 ADD END */
    /* 2022/11/25 MCCM初版 DEL START */
    //#define MMMATA_TBL    "MMマタニティベビー情報" /* MMマタニティベビー情報 */
    /* 2022/11/25 MCCM初版 DEL END */
    String MSSEDO_TBL = "MS顧客制度情報";         /* MS顧客制度情報 */
    String MSCARD_TBL = "MSカード情報";           /* MSカード情報 */
    String TSRIYO_TBL = "TS利用可能ポイント情報"; /* TS利用可能ポイント情報 */
    String TSPTYR_TBL = "TSポイント年別情報";     /* TSポイント年別情報 */
    String MSCRKR_TBL = "MSサークル管理情報";     /* MSサークル管理情報 */
    String MSCRKK_TBL = "MSサークル顧客情報";     /* MSサークル顧客情報 */
    String MSFAMI_TBL = "MS家族制度情報";         /* MS家族制度情報 */
    /* 2022/11/25 MCCM初版 ADD START */
    String MSGAIB_TBL = "MS外部認証情報";         /* MS外部認証情報 */
    String TSRANK_TBL = "TSランク情報";           /* TSランク情報 */
    /* 2022/11/25 MCCM初版 ADD END */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int     arg_t_chk;          /** 引数tチェック用           **/
    int     arg_d_chk;          /** 引数dチェック用           **/
    int     arg_u_chk;          /** 引数uチェック用           **/
    int     arg_p_chk;          /** 引数pチェック用           **/
    int     arg_s_chk;          /** 引数sチェック用           **/
    int     arg_o_chk;          /** 引数sチェック用           **/
    StringDto arg_t_Value = new StringDto(256);   /** 引数t設定値               **/
    StringDto arg_d_Value = new StringDto(9);     /** 引数d設定値               **/
    StringDto arg_u_Value = new StringDto(256);   /** 引数u設定値               **/
    StringDto arg_p_Value = new StringDto(256);   /** 引数p設定値               **/
    StringDto arg_s_Value = new StringDto(256);   /** 引数s設定値               **/
    StringDto arg_o_Value = new StringDto(256);   /** 引数o設定値               **/
    /*---------------------------------------------*/
    /*** 出力ファイル ***/
    FileStatusDto fp_out = new FileStatusDto();              /* 出力ファイル用ポインタ      */
    /*---------------------------------------------*/
    int     upd_ins_data_cnt;   /** 更新件数（COMMITチェック用）         **/
    int     out_data_cnt;       /** 更新件数（結果出力用）               **/
    int     out_del_cnt;        /** 削除件数（結果出力用）               **/
    int     DBG_LOG;            /** デバッグメッセージ出力               **/


    SqlstmDto sqlca = new SqlstmDto();


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
        int     rtn_cd;                         /** 関数戻り値                       **/
        int     rtn_cd_arg;                     /** 引数チェック関数戻り値           **/
        int     arg_chk;                        /** 引数の種類チェック結果           **/
        int     arg_cnt;                        /** 引数チェック用カウンタ           **/
        StringDto    arg_Work1 = new StringDto(256);                /** Work Buffer1                     **/
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /*                                               */
        /*  初期処理                                     */
        /*                                               */
        /** 変数初期化                **/
        arg_t_chk = DEF_OFF;
        arg_d_chk = DEF_OFF;
        arg_u_chk = DEF_OFF;
        arg_p_chk = DEF_OFF;
        arg_s_chk = DEF_OFF;
        arg_o_chk = DEF_OFF;
        rtn_cd = R_const_OK;
        rtn_cd_arg = 0;
        DBG_LOG = 0;
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        /*                                     */
        /*  入力引数チェック                   */
        /*                                     */
        /*** 引数チェック ***/
        for( arg_cnt = 1 ; arg_cnt < argc ; arg_cnt++ ) {
            memset( arg_Work1 , 0x00 , 256 ) ;
            strcpy( arg_Work1 , argv[arg_cnt] ) ;
            arg_chk  = UMU_ARI ;

            if(DBG_LOG == 1){
                /*-----------------------------------------------------------*/
                printf( "*** main *** チェック %s\n", arg_Work1 ) ;
                /*-----------------------------------------------------------*/
            }
            if( 0 == strcmp( arg_Work1 , DEF_DEBUG )) {
                DBG_LOG = 1;
                continue ;
            } else if( 0 == strcmp( arg_Work1 , DEF_debug )) {
                DBG_LOG = 1;
                continue ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_T , 2 )) {
                /* 対象テーブル名指定 -t */
                rtn_cd_arg  = Chk_ArgtInf( arg_Work1 ) ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_D , 2 )) {
                /* 処理対象日付指定 -d */
                rtn_cd_arg  = Chk_ArgdInf( arg_Work1 ) ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_U , 2 )) {
                /* ユーザ指定 -u */
                rtn_cd_arg  = Chk_ArguInf( arg_Work1 ) ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_P , 2 )) {
                /* パスワード指定 -p */
                rtn_cd_arg  = Chk_ArgpInf( arg_Work1 ) ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_S , 2 )) {
                /* SID指定 -s */
                rtn_cd_arg  = Chk_ArgsInf( arg_Work1 ) ;
            } else if( 0 == memcmp( arg_Work1 , DEF_ARG_O , 2 )) {
                /* 出力ファイル名指定 -o */
                rtn_cd_arg  = Chk_ArgoInf( arg_Work1 ) ;
            } else {
                /* 規定外パラメータ  */
                arg_chk  = UMU_NASI ;
            }

            /* 規定外パラメータ  */
            if( arg_chk != UMU_ARI ) {
                if(DBG_LOG == 1){
                    /*-----------------------------------------------------------*/
                    printf( "*** main *** チェックNG(規定外) %s\n", "" ) ;
                    /*-----------------------------------------------------------*/
                }
                return exit( R_const_APPRMNG ) ;
            }

            /* パラメータのチェック結果がNG */
            if( rtn_cd_arg == R_const_NG ) {
                if(DBG_LOG == 1){
                    /*-----------------------------------------------------------*/
                    printf( "*** main *** チェックNG(結果) %s\n", "" ) ;
                    /*-----------------------------------------------------------*/
                }
                return exit( R_const_APPRMNG ) ;
            }
        }

        /* 必須パラメータ未指定の場合エラー出力 */
        if ( arg_t_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if ( arg_d_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if ( arg_u_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if ( arg_p_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if ( arg_s_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if ( arg_o_chk == DEF_OFF )
        {
            return exit( R_const_APPRMNG ) ;
        }
        if(DBG_LOG == 1){
            /*-----------------------------------------------------*/
            printf( "*** main *** チェックOK %s\n", "" ) ;
            /*-----------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /*  出力ファイルオープン                             */
        /*-----------------------------------------------*/
        rtn_cd = OpenOutFile();
        if (rtn_cd != R_const_OK) {
            if(DBG_LOG == 1){
                /*---------------------------------------------*/
                printf( "*** main *** 出力ファイルオープンNG rtn= %d\n", rtn_cd ) ;
                /*---------------------------------------------*/
            }
            return exit (R_const_APFILENG);
        }
        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "*** main *** 出力ファイルオープンOK rtn= %d\n", rtn_cd ) ;
            /*---------------------------------------------*/
        }

        /*                                     */
        /*  DBコネクト処理                     */
        /*                                     */
        /*  ユーザ        */
        memset(ORAUSR, 0x00, sizeof(ORAUSR.arr));
        ORAUSR.len = 0;
        strncpy(ORAUSR, arg_u_Value.arr, strlen(arg_u_Value));
        ORAUSR.len = strlen(ORAUSR);
        /*  パスワード    */
        memset(PASSWD, 0x00, sizeof(PASSWD.arr));
        PASSWD.len = 0;
        strncpy(PASSWD, arg_p_Value.arr, strlen(arg_p_Value));
        PASSWD.len = strlen(PASSWD);
        /*  SID           */
        memset(ORASID, 0x00, sizeof(ORASID.arr));
        ORASID.len = 0;
        strncpy(ORASID, arg_s_Value.arr, strlen(arg_s_Value));
        ORASID.len = strlen(ORASID);
        if(DBG_LOG == 1){
            /*-------------------------------------------------------------*/
            printf( "*** main *** DBコネクト%s\n", "" );
            /*-------------------------------------------------------------*/
        }
        // EXEC SQL CONNECT :ORAUSR IDENTIFIED BY :PASSWD USING :ORASID;
        sqlca.ORASID = ORASID.strDto();
        sqlca.ORAUSR = ORAUSR.strDto();
        sqlca.PASSWD = PASSWD.strDto();
        sqlcaManager.init(sqlca);

        if (sqlca.sqlcode != R_const_Ora_OK) {
            if(DBG_LOG == 1){
                printf("Connect : %s\n", "接続NG");
                printf("Connect : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            sprintf(out_rec_buff, "DB接続エラー sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 出力ファイル クローズ */
            fclose(fp_out);

            return exit( R_const_APNG ) ;
        }

        /*  バッチ処理日付     */
        memset(bt_date, 0x00, sizeof(bt_date));
        strncpy(bt_date, arg_d_Value.arr, 8);

        /*                                     */
        /*  処理件数領域を初期化               */
        /*                                     */
        if(DBG_LOG == 1){
            /*-------------------------------------------------------------*/
            printf( "*** main *** 処理件数領域を初期化%s\n", "" );
            /*-------------------------------------------------------------*/
        }
        upd_ins_data_cnt = 0;
        out_data_cnt = 0;

        /*                                               */
        /*  主処理                                       */
        /*                                               */
        if(DBG_LOG == 1){
            /*-------------------------------------------------------------*/
            printf( "*** cmABmrupB_main *** 当日更新分反映主処理%s\n", "" );
            /*-------------------------------------------------------------*/
        }
        rtn_cd = cmABmrupB_main();
        if ( rtn_cd != R_const_OK )
        {
            /*  ロールバック処理呼び出し     */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* 出力ファイル クローズ */
            fclose(fp_out);

            return exit( R_const_APNG ) ;
        }


        /*                                               */
        /*  終了処理                                     */
        /*                                               */
        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "main処理 終了\n" );
            /*---------------------------------------------*/
        }

        /*  コミット処理呼び出し     */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        /* 出力ファイル クローズ */
        fclose(fp_out);

        return exit( R_const_APOK ) ;
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgtInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgtInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-t スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-t スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArgtInf(StringDto Arg_in) {
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgtInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_t_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgtInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_t_chk = DEF_ON;
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgtInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgtInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_t_Value , Arg_in.substring(2));
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgdInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgdInf( char *Arg_in )                                   */
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
    @Override
    public int Chk_ArgdInf(StringDto Arg_in) {
        /*                                     */
        /*  ローカル変数定義                   */
        /*                                     */
        int DEF_ARG_Leng = 8 + 2;          /* 文字サイズ                  */

        /* 初期化 */
        memset(arg_d_Value,0x00,sizeof(arg_d_Value));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgdInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_d_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgdInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_d_chk = DEF_ON;
        /*                                     */
        /*  値の内容チェック                   */
        /*                                     */
        /*  文字サイズチェック  */
        if( strlen(Arg_in) != DEF_ARG_Leng ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgdInf *** 文字サイズNG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgdInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG ;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgdInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }
        strcpy(arg_d_Value,Arg_in.substring(2));

        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArguInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArguInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-u スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-u スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArguInf(StringDto Arg_in) {
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArguInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_u_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArguInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_u_chk = DEF_ON;
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArguInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArguInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_u_Value , Arg_in.substring(2));
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgpInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgpInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-p スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-p スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArgpInf(StringDto Arg_in) {
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgpInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_p_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgpInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_p_chk = DEF_ON;
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgpInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgpInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_p_Value , Arg_in.substring(2));
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgsInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgsInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-s スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-s スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArgsInf(StringDto Arg_in) {
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgsInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_s_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgsInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_s_chk = DEF_ON;
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgsInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgsInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_s_Value , Arg_in.substring(2));
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgoInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgoInf( char *Arg_in )                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-o スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-o スイッチの引数                       */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    @Override
    public int Chk_ArgoInf(StringDto Arg_in) {
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgoInf処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if( arg_o_chk != DEF_OFF ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgoInf *** 重複指定NG = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_o_chk = DEF_ON;
        /*  設定値Nullチェック  */
        if( Arg_in.size() <= 2 || Arg_in.charAt(2) == '\0' ) {

            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** Chk_ArgoInf *** 設定値Null = %s\n", Arg_in ) ;
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "Chk_ArgoInf処理 終了\n" );
            /*---------------------------------------------------------------------*/
        }

        strcpy(arg_o_Value , Arg_in.substring(2));
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： OpenOutFile                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OpenOutFile()                                                 */
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
        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "OpenOutFile処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }

        /* 出力ファイルのオープン */
        if ((fp_out = fopen(arg_o_Value.arr, FileOpenType.a)).fd == C_const_NG) {
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** OpenOutFile *** 出力ファイルオープンNG%s\n", "" ) ;
                /*-------------------------------------------------------------*/
            }
            return (R_const_NG);
        }
        if(DBG_LOG == 1){
            /*-------------------------------------------------------------*/
            printf( "OpenOutFile処理 終了\n" );
            /*-------------------------------------------------------------*/
        }

        return(R_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmABmrupB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmABmrupB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               当日更新分反映主処理                                         */
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
    public int cmABmrupB_main() {
        int     rtn_cd;                         /** 関数戻り値                       **/
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));
        rtn_cd = R_const_OK;

        if ( 0 == strcmp(arg_t_Value, MMINFO_TBL) ){
            /* MM顧客情報_BK更新処理 */
            rtn_cd = UpdateKokyakuinfo_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MM顧客情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strcmp(arg_t_Value, MMZOKU_TBL) ){
            /* MM顧客属性情報_BK更新処理 */
            rtn_cd = UpdateKokyakuzokusei_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MM顧客属性情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strcmp(arg_t_Value, MMKIGY_TBL) ){
            /* MM顧客企業別属性情報_BK更新処理 */
            rtn_cd = UpdateKokyakukigybetu_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }
            /* 2023/05/29 MCCMPH2 ADD START */
        }else if ( 0 == strcmp(arg_t_Value, MMOTODOKE_TBL) ){
            /* MMお届け先情報_BK更新処理 */
            rtn_cd = UpdateOtodokesaki_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MMお届け先情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }
            /* 2023/05/29 MCCMPH2 ADD END */
            /* 2022/11/25 MCCM初版 DEL START */
//    }else if ( !strcmp(arg_t_Value, MMMATA_TBL) ){
//            /* MMマタニティベビー情報_BK更新処理 */
//            rtn_cd = UpdateMatababy_bk();
//            if ( rtn_cd != R_const_OK )
//            {
//                sprintf(out_rec_buff, "MMマタニティベビー情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
//                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
//
//                return R_const_NG;
//            }
            /* 2022/11/25 MCCM初版 DEL END */
        }else if ( 0 == strcmp(arg_t_Value, MSSEDO_TBL) ){
            /* MS顧客制度情報_BK更新処理 */
            rtn_cd = UpdateKokyakusedo_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MS顧客制度情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strcmp(arg_t_Value, MSCARD_TBL) ){
            /* MSカード情報_BK更新処理 */
            rtn_cd = UpdateMscard_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MSカード情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strncmp(arg_t_Value.arr, TSRIYO_TBL, strlen(TSRIYO_TBL)) ){
            /* TS利用可能ポイント情報_BK更新処理 */
            rtn_cd = UpdateRiyoukapoint_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strncmp(arg_t_Value.arr, TSPTYR_TBL, strlen(TSPTYR_TBL)) ){
            /* TSポイント年別情報_BK更新処理 */
            rtn_cd = UpdatePointyear_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* 2022/11/25 MCCM初版 DEL START */
//    }else if ( !strcmp(arg_t_Value, MSCRKR_TBL) ){
//            /* MSサークル管理情報_BK更新処理 */
//            rtn_cd = UpdateCirclekanri_bk();
//            if ( rtn_cd != R_const_OK )
//            {
//                sprintf(out_rec_buff, "MSサークル管理情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
//                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
//
//                return R_const_NG;
//            }
//
//            /* MSサークル管理情報_BK削除処理 */
//            rtn_cd = DeleteCirclekanri_bk();
//            if ( rtn_cd != R_const_OK )
//            {
//                sprintf(out_rec_buff, "MSサークル管理情報_BK削除処理異常終了 rtn_cd=[%d]\n", rtn_cd);
//                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
//
//                return R_const_NG;
//            }
//
//    }else if ( !strcmp(arg_t_Value, MSCRKK_TBL) ){
//            /* MSサークル顧客情報_BK更新処理 */
//            rtn_cd = UpdateCirclekokyaku_bk();
//            if ( rtn_cd != R_const_OK )
//            {
//                sprintf(out_rec_buff, "MSサークル顧客情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
//                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
//
//                return R_const_NG;
//            }
//
//            /* MSサークル顧客情報_BK削除処理 */
//            rtn_cd = DeleteCirclekokyaku_bk();
//            if ( rtn_cd != R_const_OK )
//            {
//                sprintf(out_rec_buff, "MSサークル顧客情報_BK削除処理異常終了 rtn_cd=[%d]\n", rtn_cd);
//                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
//
//                return R_const_NG;
//            }
//
            /* 2022/11/25 MCCM初版 DEL END */
        }else if ( 0 == strcmp(arg_t_Value, MSFAMI_TBL) ){
            /* MS家族制度情報_BK更新処理 */
            rtn_cd = UpdateKazokusedo_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MS家族制度情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* MS家族制度情報_BK削除処理 */
            rtn_cd = DeleteKazokusedo_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MS家族制度情報_BK削除処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* 2022/11/25 MCCM初版 ADD START */
        }else if ( 0 == strcmp(arg_t_Value, MSGAIB_TBL) ){
            /* MS外部認証情報_BK更新処理 */
            rtn_cd = UpdateGaibuninsho_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MS外部認証情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* MS外部認証情報_BK削除処理 */
            rtn_cd = DeleteGaibuninsho_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "MS外部認証情報_BK削除処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

        }else if ( 0 == strcmp(arg_t_Value, TSRANK_TBL) ){
            /* TSランク情報_BK更新処理 */
            rtn_cd = UpdateTsrank_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "TSランク情報_BK更新処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* TSランク情報_BK削除処理 */
            rtn_cd = DeleteTsrank_bk();
            if ( rtn_cd != R_const_OK )
            {
                sprintf(out_rec_buff, "TSランク情報_BK削除処理異常終了 rtn_cd=[%d]\n", rtn_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                return R_const_NG;
            }

            /* 2022/11/25 MCCM初版 ADD END */
        }

        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuinfo_bk                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuinfo_bk()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客情報_BK更新処理                                        */
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
    public int UpdateKokyakuinfo_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MM顧客情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MM顧客情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MMKI01 CURSOR FOR
        SELECT  ROWID,顧客番号
        FROM  MM顧客情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MMKI01 = sqlcaManager.get("MRUP_MMKI01");
        StringDto workSql = new StringDto();
        workSql.arr = "SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,顧客番号\n" +
                "        FROM  MM顧客情報\n" +
                "        WHERE 最終更新日   >= ?";
        MRUP_MMKI01.sql = workSql;

        // EXEC SQL OPEN MRUP_MMKI01;
        MRUP_MMKI01.declare();
        MRUP_MMKI01.open(bt_date);

        /* エラーの場合処理を異常終了する */
        if (MRUP_MMKI01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MM顧客情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MMKI01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_MMKI01
            INTO :h_rowid, :h_uid;
            */
            MRUP_MMKI01.fetch();
            MRUP_MMKI01.recData(h_rowid, h_uid);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MMKI01.sqlcode != R_const_Ora_OK
                    && MRUP_MMKI01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MMKI01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKI01;
                MRUP_MMKI01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MMKI01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateKokyakuinfo_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateKokyakuinfo_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }
            /* 2023/05/29 MCCMPH2 MOD START */
            /* MM顧客情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MM顧客情報_BK MM_BK
            SET (MM_BK.休眠フラグ,
                    MM_BK.顧客名称,
                    MM_BK.顧客カナ名称,
                    MM_BK.年齢,
                    MM_BK.誕生年,
                    MM_BK.誕生月,
                    MM_BK.誕生日,
                    MM_BK.性別,
                    MM_BK.婚姻,
                    MM_BK.入会企業コード,
                    MM_BK.入会店舗,
                    MM_BK.発券企業コード,
                    MM_BK.発券店舗,
                    MM_BK.社員区分,
                    MM_BK.ポータル入会年月日,
                    MM_BK.ポータル退会年月日,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終静態更新日,
                    MM_BK.最終静態更新時刻,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_BK.最終更新プログラムＩＤ
                    MM_BK.最終更新プログラムＩＤ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_BK.顧客名字,
                    MM_BK.顧客名前,
                    MM_BK.カナ顧客名字,
                    MM_BK.カナ顧客名前,
                    MM_BK.顧客ステータス,
                    MM_BK.入会申込用紙記載日,
                    MM_BK.静態登録日,
                    MM_BK.登録コード,
                    MM_BK.ＧＯＯＰＯＮ会員パスワード,
                    MM_BK.入会会社コードＭＣＣ,
                    MM_BK.入会店舗ＭＣＣ,
                    MM_BK.アプリユーザＩＤ,
                    MM_BK.シニア,
                    MM_BK.属性管理主体システム,
                    MM_BK.ニックネーム,
                    MM_BK.プッシュ通知許可フラグ,
                    MM_BK.メールアドレス１送信フラグ,
                    MM_BK.メールアドレス２送信フラグ,
                    MM_BK.メールアドレス３送信フラグ,
                    MM_BK.契約番号,
                    MM_BK.旧契約番号,
                    MM_BK.提携先組織コード３,
                    MM_BK.変換不能文字有無区分
                    *//* 2022/11/25 MCCM初版 ADD END *//*
            ) = (
                    SELECT MM_I.休眠フラグ,
                    MM_I.顧客名称,
                    MM_I.顧客カナ名称,
                    MM_I.年齢,
                    MM_I.誕生年,
                    MM_I.誕生月,
                    MM_I.誕生日,
                    MM_I.性別,
                    MM_I.婚姻,
                    MM_I.入会企業コード,
                    MM_I.入会店舗,
                    MM_I.発券企業コード,
                    MM_I.発券店舗,
                    MM_I.社員区分,
                    MM_I.ポータル入会年月日,
                    MM_I.ポータル退会年月日,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終静態更新日,
                    MM_I.最終静態更新時刻,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                          MM_I.最終更新プログラムＩＤ
                    MM_I.最終更新プログラムＩＤ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_I.顧客名字,
                    MM_I.顧客名前,
                    MM_I.カナ顧客名字,
                    MM_I.カナ顧客名前,
                    MM_I.顧客ステータス,
                    MM_I.入会申込用紙記載日,
                    MM_I.静態登録日,
                    MM_I.登録コード,
                    MM_I.ＧＯＯＰＯＮ会員パスワード,
                    MM_I.入会会社コードＭＣＣ,
                    MM_I.入会店舗ＭＣＣ,
                    MM_I.アプリユーザＩＤ,
                    MM_I.シニア,
                    MM_I.属性管理主体システム,
                    MM_I.ニックネーム,
                    MM_I.プッシュ通知許可フラグ,
                    MM_I.メールアドレス１送信フラグ,
                    MM_I.メールアドレス２送信フラグ,
                    MM_I.メールアドレス３送信フラグ,
                    MM_I.契約番号,
                    MM_I.旧契約番号,
                    MM_I.提携先組織コード３,
                    MM_I.変換不能文字有無区分
            *//* 2022/11/25 MCCM初版 ADD END *//*
            FROM MM顧客情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid;
            */
            workSql = new StringDto("UPDATE MM顧客情報_BK MM_BK\n" +
                    "SET (休眠フラグ,\n" +
                    "        顧客名称,\n" +
                    "        顧客カナ名称,\n" +
                    "        年齢,\n" +
                    "        誕生年,\n" +
                    "        誕生月,\n" +
                    "        誕生日,\n" +
                    "        性別,\n" +
                    "        婚姻,\n" +
                    "        入会企業コード,\n" +
                    "        入会店舗,\n" +
                    "        発券企業コード,\n" +
                    "        発券店舗,\n" +
                    "        社員区分,\n" +
                    "        ポータル入会年月日,\n" +
                    "        ポータル退会年月日,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終静態更新日,\n" +
                    "        最終静態更新時刻,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ,\n" +
                    "        顧客名字,\n" +
                    "        顧客名前,\n" +
                    "        カナ顧客名字,\n" +
                    "        カナ顧客名前,\n" +
                    "        顧客ステータス,\n" +
                    "        入会申込用紙記載日,\n" +
                    "        静態登録日,\n" +
                    "        登録コード,\n" +
                    "        ＧＯＯＰＯＮ会員パスワード,\n" +
                    "        入会会社コードＭＣＣ,\n" +
                    "        入会店舗ＭＣＣ,\n" +
                    "        アプリユーザＩＤ,\n" +
                    "        シニア,\n" +
                    "        属性管理主体システム,\n" +
                    "        ニックネーム,\n" +
                    "        プッシュ通知許可フラグ,\n" +
                    "        メールアドレス１送信フラグ,\n" +
                    "        メールアドレス２送信フラグ,\n" +
                    "        メールアドレス３送信フラグ,\n" +
                    "        契約番号,\n" +
                    "        旧契約番号,\n" +
                    "        提携先組織コード３,\n" +
                    "        変換不能文字有無区分\n" +
                    ") = (\n" +
                    "        SELECT MM_I.休眠フラグ,\n" +
                    "        MM_I.顧客名称,\n" +
                    "        MM_I.顧客カナ名称,\n" +
                    "        MM_I.年齢,\n" +
                    "        MM_I.誕生年,\n" +
                    "        MM_I.誕生月,\n" +
                    "        MM_I.誕生日,\n" +
                    "        MM_I.性別,\n" +
                    "        MM_I.婚姻,\n" +
                    "        MM_I.入会企業コード,\n" +
                    "        MM_I.入会店舗,\n" +
                    "        MM_I.発券企業コード,\n" +
                    "        MM_I.発券店舗,\n" +
                    "        MM_I.社員区分,\n" +
                    "        MM_I.ポータル入会年月日,\n" +
                    "        MM_I.ポータル退会年月日,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終静態更新日,\n" +
                    "        MM_I.最終静態更新時刻,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ,\n" +
                    "        MM_I.顧客名字,\n" +
                    "        MM_I.顧客名前,\n" +
                    "        MM_I.カナ顧客名字,\n" +
                    "        MM_I.カナ顧客名前,\n" +
                    "        MM_I.顧客ステータス,\n" +
                    "        MM_I.入会申込用紙記載日,\n" +
                    "        MM_I.静態登録日,\n" +
                    "        MM_I.登録コード,\n" +
                    "        MM_I.ＧＯＯＰＯＮ会員パスワード,\n" +
                    "        MM_I.入会会社コードＭＣＣ,\n" +
                    "        MM_I.入会店舗ＭＣＣ,\n" +
                    "        MM_I.アプリユーザＩＤ,\n" +
                    "        MM_I.シニア,\n" +
                    "        MM_I.属性管理主体システム,\n" +
                    "        MM_I.ニックネーム,\n" +
                    "        MM_I.プッシュ通知許可フラグ,\n" +
                    "        MM_I.メールアドレス１送信フラグ,\n" +
                    "        MM_I.メールアドレス２送信フラグ,\n" +
                    "        MM_I.メールアドレス３送信フラグ,\n" +
                    "        MM_I.契約番号,\n" +
                    "        MM_I.旧契約番号,\n" +
                    "        MM_I.提携先組織コード３,\n" +
                    "        MM_I.変換不能文字有無区分\n" +
                    "FROM MM顧客情報 MM_I\n" +
                    "WHERE ROW (MM_I.顧客番号) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "WHERE MM_BK.顧客番号 = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_uid);

            /* 2023/05/29 MCCMPH2 MOD END */
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKI01;
                MRUP_MMKI01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MM顧客情報_BK
                SELECT * FROM MM顧客情報
                WHERE ROWID = :h_rowid;
                */
                workSql.setArr("INSERT   INTO MM顧客情報_BK\n" +
                        "                SELECT * FROM MM顧客情報\n" +
                        "                WHERE ROW (顧客番号) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MM顧客情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MMKI01;
                    MRUP_MMKI01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MM顧客情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_MMKI01;
        MRUP_MMKI01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MM顧客情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MM顧客情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokusei_bk                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokusei_bk()                                     */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客属性情報_BK更新処理                                    */
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
    public int UpdateKokyakuzokusei_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MM顧客属性情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MM顧客属性情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MMKZ01 CURSOR FOR
        SELECT  ROWID,顧客番号
        FROM  MM顧客属性情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MMKZ01 = sqlcaManager.get("MRUP_MMKZ01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,顧客番号\n" +
                "        FROM  MM顧客属性情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MMKZ01.sql = workSql;

        // EXEC SQL OPEN MRUP_MMKZ01;
        MRUP_MMKZ01.declare();
        MRUP_MMKZ01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MMKZ01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MM顧客属性情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MMKZ01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_MMKZ01
            INTO :h_rowid, :h_uid;
            */
            MRUP_MMKZ01.fetch();
            MRUP_MMKZ01.recData(h_rowid, h_uid);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MMKZ01.sqlcode != R_const_Ora_OK
                    && MRUP_MMKZ01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客属性情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MMKZ01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKZ01;
                MRUP_MMKZ01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MMKZ01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateKokyakuzokusei_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateKokyakuzokusei_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }

            /* MM顧客属性情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MM顧客属性情報_BK MM_BK
            SET (MM_BK.休眠フラグ,
                    MM_BK.郵便番号,
                    MM_BK.郵便番号コード,
                    MM_BK.住所１,
                    MM_BK.住所２,
                    MM_BK.住所３,
                    MM_BK.電話番号１,
                    MM_BK.電話番号２,
                    MM_BK.検索電話番号１,
                    MM_BK.検索電話番号２,
                    MM_BK.Ｅメールアドレス１,
                    MM_BK.Ｅメールアドレス２,
                    MM_BK.電話番号３,
                    MM_BK.検索電話番号３,
                    MM_BK.電話番号４,
                    MM_BK.検索電話番号４,
                    MM_BK.職業,
                    MM_BK.勤務区分,
                    MM_BK.自宅住所コード,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ,
                    MM_BK.Ｅメールアドレス３,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_BK.Ｅメールアドレス４
                    MM_BK.Ｅメールアドレス４,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_BK.都道府県コード,
                    MM_BK.住所,
                    MM_BK.Ｘ座標コード,
                    MM_BK.Ｙ座標コード,
                    MM_BK.会社名,
                    MM_BK.部署名,
                    MM_BK.関心分野コード
                    *//* 2022/11/25 MCCM初版 ADD END *//*
            ) = (
                    SELECT MM_I.休眠フラグ,
                    MM_I.郵便番号,
                    MM_I.郵便番号コード,
                    MM_I.住所１,
                    MM_I.住所２,
                    MM_I.住所３,
                    MM_I.電話番号１,
                    MM_I.電話番号２,
                    MM_I.検索電話番号１,
                    MM_I.検索電話番号２,
                    MM_I.Ｅメールアドレス１,
                    MM_I.Ｅメールアドレス２,
                    MM_I.電話番号３,
                    MM_I.検索電話番号３,
                    MM_I.電話番号４,
                    MM_I.検索電話番号４,
                    MM_I.職業,
                    MM_I.勤務区分,
                    MM_I.自宅住所コード,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ,
                    MM_I.Ｅメールアドレス３,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                          MM_I.Ｅメールアドレス４
                    MM_I.Ｅメールアドレス４,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_I.都道府県コード,
                    MM_I.住所,
                    MM_I.Ｘ座標コード,
                    MM_I.Ｙ座標コード,
                    MM_I.会社名,
                    MM_I.部署名,
                    MM_I.関心分野コード
            *//* 2022/11/25 MCCM初版 ADD END *//*
            FROM MM顧客属性情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid;
            */
            workSql = new StringDto("UPDATE MM顧客属性情報_BK MM_BK\n" +
                    "SET (休眠フラグ,\n" +
                    "        郵便番号,\n" +
                    "        郵便番号コード,\n" +
                    "        住所１,\n" +
                    "        住所２,\n" +
                    "        住所３,\n" +
                    "        電話番号１,\n" +
                    "        電話番号２,\n" +
                    "        検索電話番号１,\n" +
                    "        検索電話番号２,\n" +
                    "        Ｅメールアドレス１,\n" +
                    "        Ｅメールアドレス２,\n" +
                    "        電話番号３,\n" +
                    "        検索電話番号３,\n" +
                    "        電話番号４,\n" +
                    "        検索電話番号４,\n" +
                    "        職業,\n" +
                    "        勤務区分,\n" +
                    "        自宅住所コード,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ,\n" +
                    "        Ｅメールアドレス３,\n" +
                    "        Ｅメールアドレス４,\n" +
                    "        都道府県コード,\n" +
                    "        住所,\n" +
                    "        Ｘ座標コード,\n" +
                    "        Ｙ座標コード,\n" +
                    "        会社名,\n" +
                    "        部署名,\n" +
                    "        関心分野コード\n" +
                    ") = (\n" +
                    "        SELECT MM_I.休眠フラグ,\n" +
                    "        MM_I.郵便番号,\n" +
                    "        MM_I.郵便番号コード,\n" +
                    "        MM_I.住所１,\n" +
                    "        MM_I.住所２,\n" +
                    "        MM_I.住所３,\n" +
                    "        MM_I.電話番号１,\n" +
                    "        MM_I.電話番号２,\n" +
                    "        MM_I.検索電話番号１,\n" +
                    "        MM_I.検索電話番号２,\n" +
                    "        MM_I.Ｅメールアドレス１,\n" +
                    "        MM_I.Ｅメールアドレス２,\n" +
                    "        MM_I.電話番号３,\n" +
                    "        MM_I.検索電話番号３,\n" +
                    "        MM_I.電話番号４,\n" +
                    "        MM_I.検索電話番号４,\n" +
                    "        MM_I.職業,\n" +
                    "        MM_I.勤務区分,\n" +
                    "        MM_I.自宅住所コード,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ,\n" +
                    "        MM_I.Ｅメールアドレス３,\n" +
                    "        MM_I.Ｅメールアドレス４,\n" +
                    "        MM_I.都道府県コード,\n" +
                    "        MM_I.住所,\n" +
                    "        MM_I.Ｘ座標コード,\n" +
                    "        MM_I.Ｙ座標コード,\n" +
                    "        MM_I.会社名,\n" +
                    "        MM_I.部署名,\n" +
                    "        MM_I.関心分野コード\n" +
                    "FROM MM顧客属性情報 MM_I\n" +
                    "ROW (MM_I.顧客番号) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "WHERE MM_BK.顧客番号 = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_uid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客属性情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKZ01;
                MRUP_MMKZ01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MM顧客属性情報_BK
                SELECT * FROM MM顧客属性情報
                WHERE ROWID = :h_rowid;
                */
                workSql = new StringDto("INSERT   INTO MM顧客属性情報_BK\n" +
                        "                SELECT * FROM MM顧客属性情報\n" +
                        "                WHERE ROW (顧客番号) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MM顧客属性情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MMKZ01;
                    MRUP_MMKZ01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MM顧客属性情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_MMKZ01;
        MRUP_MMKZ01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MM顧客属性情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MM顧客属性情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakukigybetu_bk                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakukigybetu_bk()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報_BK更新処理                              */
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
    public int UpdateKokyakukigybetu_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MM顧客企業別属性情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MM顧客企業別属性情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MMKG01 CURSOR FOR
        SELECT  ROWID,顧客番号,企業コード
        FROM  MM顧客企業別属性情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MMKG01 = sqlcaManager.get("MRUP_MMKG01");
        StringDto workSql = new StringDto(" SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号,企業コード) AS TEXT)),'') AS ROWID,顧客番号,企業コード\n" +
                "        FROM  MM顧客企業別属性情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MMKG01.sql = workSql;

        // EXEC SQL OPEN MRUP_MMKG01;
        MRUP_MMKG01.declare();
        MRUP_MMKG01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MMKG01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MMKG01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;
            h_kigyo_cd.arr = 0;

            /*
            EXEC SQL FETCH MRUP_MMKG01
            INTO :h_rowid, :h_uid, :h_kigyo_cd;
            */
            MRUP_MMKG01.fetch();
            MRUP_MMKG01.recData(h_rowid, h_uid, h_kigyo_cd);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MMKG01.sqlcode != R_const_Ora_OK
                    && MRUP_MMKG01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MMKG01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKG01;
                MRUP_MMKG01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MMKG01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateKokyakukigybetu_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateKokyakukigybetu_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }

            /* MM顧客企業別属性情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MM顧客企業別属性情報_BK MM_BK
            SET (MM_BK.企業コード,
                    MM_BK.入会年月日,
                    MM_BK.退会年月日,
                    MM_BK.ＴＥＬ止め区分,
                    MM_BK.ＤＭ止め区分,
                    MM_BK.Ｅメール止め区分,
                    MM_BK.携帯ＴＥＬ止め区分,
                    MM_BK.携帯Ｅメール止め区分,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ,
                    MM_BK.仮退会年月日,
                    MM_BK.仮退会解除年月日
            ) = (
                    SELECT MM_I.企業コード,
                    MM_I.入会年月日,
                    MM_I.退会年月日,
                    MM_I.ＴＥＬ止め区分,
                    MM_I.ＤＭ止め区分,
                    MM_I.Ｅメール止め区分,
                    MM_I.携帯ＴＥＬ止め区分,
                    MM_I.携帯Ｅメール止め区分,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ,
                    MM_I.仮退会年月日,
                    MM_I.仮退会解除年月日
            FROM MM顧客企業別属性情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid
            AND   MM_BK.企業コード = :h_kigyo_cd;
            */
            workSql = new StringDto("UPDATE MM顧客企業別属性情報_BK MM_BK\n" +
                    "SET (企業コード,\n" +
                    "        入会年月日,\n" +
                    "        退会年月日,\n" +
                    "        ＴＥＬ止め区分,\n" +
                    "        ＤＭ止め区分,\n" +
                    "        Ｅメール止め区分,\n" +
                    "        携帯ＴＥＬ止め区分,\n" +
                    "        携帯Ｅメール止め区分,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ,\n" +
                    "        仮退会年月日,\n" +
                    "        仮退会解除年月日\n" +
                    ") = (\n" +
                    "        SELECT MM_I.企業コード,\n" +
                    "        MM_I.入会年月日,\n" +
                    "        MM_I.退会年月日,\n" +
                    "        MM_I.ＴＥＬ止め区分,\n" +
                    "        MM_I.ＤＭ止め区分,\n" +
                    "        MM_I.Ｅメール止め区分,\n" +
                    "        MM_I.携帯ＴＥＬ止め区分,\n" +
                    "        MM_I.携帯Ｅメール止め区分,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ,\n" +
                    "        MM_I.仮退会年月日,\n" +
                    "        MM_I.仮退会解除年月日\n" +
                    "FROM MM顧客企業別属性情報 MM_I\n" +
                    "WHERE ROW (MM_I.顧客番号,MM_I.企業コード) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "WHERE MM_BK.顧客番号 = ?\n" +
                    "AND   MM_BK.企業コード = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_uid, h_kigyo_cd);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s] 企業コード[%d]\n", sqlca.sqlcode,h_uid,h_kigyo_cd);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMKG01;
                MRUP_MMKG01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MM顧客企業別属性情報_BK
                SELECT * FROM MM顧客企業別属性情報
                WHERE ROWID = :h_rowid;
                */
                workSql = new StringDto("INSERT   INTO MM顧客企業別属性情報_BK\n" +
                        "                SELECT * FROM MM顧客企業別属性情報\n" +
                        "                WHERE ROW (顧客番号,企業コード) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MMKG01;
                    MRUP_MMKG01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MM顧客企業別属性情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        //EXEC SQL CLOSE MRUP_MMKG01;
        MRUP_MMKG01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MM顧客企業別属性情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MM顧客企業別属性情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateOtodokesaki_bk                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateOtodokesaki_bk()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MMお届け先情報_BK更新処理                                    */
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
    public int UpdateOtodokesaki_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MMお届け先情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MM顧客情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MMOT01 CURSOR FOR
        SELECT  ROWID,顧客番号,お届け先番号
        FROM  MMお届け先情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MMOT01 = sqlcaManager.get("MRUP_MMOT01");
        StringDto workSql = new StringDto("SELECT   NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号,お届け先番号) AS TEXT)),'') AS ROWID,顧客番号,お届け先番号\n" +
                "        FROM  MMお届け先情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MMOT01.sql = workSql;

        // EXEC SQL OPEN MRUP_MMOT01;
        MRUP_MMOT01.declare();
        MRUP_MMOT01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MMOT01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MMお届け先情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MMOT01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;
            otodokesaki_no.arr = 0;

            /*
            EXEC SQL FETCH MRUP_MMOT01
            INTO :h_rowid, :h_uid, :otodokesaki_no;
            */
            MRUP_MMOT01.fetch();
            MRUP_MMOT01.recData(h_rowid, h_uid, otodokesaki_no);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MMOT01.sqlcode != R_const_Ora_OK
                    && MRUP_MMOT01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MMお届け先情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MMOT01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MMOT01;
                MRUP_MMOT01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MMOT01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateOtodokesaki_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateOtodokesaki_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }
            /* MMお届け先情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MMお届け先情報_BK MM_BK
            SET (MM_BK.お届け先番号,
                    MM_BK.お届け先表示名,
                    MM_BK.お届け先氏名漢字姓,
                    MM_BK.お届け先氏名漢字名,
                    MM_BK.お届け先氏名カナ姓,
                    MM_BK.お届け先氏名カナ名,
                    MM_BK.お届け先郵便番号１,
                    MM_BK.お届け先郵便番号２,
                    MM_BK.お届け先都道府県,
                    MM_BK.お届け先住所,
                    MM_BK.お届け先電話番号,
                    MM_BK.お届け先会社名,
                    MM_BK.お届け先部署名,
                    MM_BK.デフォルトお届け先フラグ,
                    MM_BK.削除フラグ,
                    MM_BK.登録日時,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ
            ) = (
                    SELECT MM_I.お届け先番号,
                    MM_I.お届け先表示名,
                    MM_I.お届け先氏名漢字姓,
                    MM_I.お届け先氏名漢字名,
                    MM_I.お届け先氏名カナ姓,
                    MM_I.お届け先氏名カナ名,
                    MM_I.お届け先郵便番号１,
                    MM_I.お届け先郵便番号２,
                    MM_I.お届け先都道府県,
                    MM_I.お届け先住所,
                    MM_I.お届け先電話番号,
                    MM_I.お届け先会社名,
                    MM_I.お届け先部署名,
                    MM_I.デフォルトお届け先フラグ,
                    MM_I.削除フラグ,
                    MM_I.登録日時,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ
            FROM MMお届け先情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid
            AND   MM_BK.お届け先番号 = :otodokesaki_no;
            */
            workSql = new StringDto("UPDATE MMお届け先情報_BK MM_BK\n" +
                    "            SET (お届け先番号,\n" +
                    "                    お届け先表示名,\n" +
                    "                    お届け先氏名漢字姓,\n" +
                    "                    お届け先氏名漢字名,\n" +
                    "                    お届け先氏名カナ姓,\n" +
                    "                    お届け先氏名カナ名,\n" +
                    "                    お届け先郵便番号１,\n" +
                    "                    お届け先郵便番号２,\n" +
                    "                    お届け先都道府県,\n" +
                    "                    お届け先住所,\n" +
                    "                    お届け先電話番号,\n" +
                    "                    お届け先会社名,\n" +
                    "                    お届け先部署名,\n" +
                    "                    デフォルトお届け先フラグ,\n" +
                    "                    削除フラグ,\n" +
                    "                    登録日時,\n" +
                    "                    作業企業コード,\n" +
                    "                    作業者ＩＤ,\n" +
                    "                    作業年月日,\n" +
                    "                    作業時刻,\n" +
                    "                    バッチ更新日,\n" +
                    "                    最終更新日,\n" +
                    "                    最終更新日時,\n" +
                    "                    最終更新プログラムＩＤ\n" +
                    "            ) = (\n" +
                    "                    SELECT MM_I.お届け先番号,\n" +
                    "                    MM_I.お届け先表示名,\n" +
                    "                    MM_I.お届け先氏名漢字姓,\n" +
                    "                    MM_I.お届け先氏名漢字名,\n" +
                    "                    MM_I.お届け先氏名カナ姓,\n" +
                    "                    MM_I.お届け先氏名カナ名,\n" +
                    "                    MM_I.お届け先郵便番号１,\n" +
                    "                    MM_I.お届け先郵便番号２,\n" +
                    "                    MM_I.お届け先都道府県,\n" +
                    "                    MM_I.お届け先住所,\n" +
                    "                    MM_I.お届け先電話番号,\n" +
                    "                    MM_I.お届け先会社名,\n" +
                    "                    MM_I.お届け先部署名,\n" +
                    "                    MM_I.デフォルトお届け先フラグ,\n" +
                    "                    MM_I.削除フラグ,\n" +
                    "                    MM_I.登録日時,\n" +
                    "                    MM_I.作業企業コード,\n" +
                    "                    MM_I.作業者ＩＤ,\n" +
                    "                    MM_I.作業年月日,\n" +
                    "                    MM_I.作業時刻,\n" +
                    "                    MM_I.バッチ更新日,\n" +
                    "                    MM_I.最終更新日,\n" +
                    "                    MM_I.最終更新日時,\n" +
                    "                    MM_I.最終更新プログラムＩＤ\n" +
                    "            FROM MMお届け先情報 MM_I\n" +
                    "            WHERE ROW (MM_I.顧客番号,MM_I.お届け先番号) = (SELECT ? FROM DUAL)\n" +
                    "            AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "            WHERE MM_BK.顧客番号 = ?\n" +
                    "            AND   MM_BK.お届け先番号 = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_uid, otodokesaki_no);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MMお届け先情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s]お届け先番号[%d]\n", sqlca.sqlcode,h_uid,otodokesaki_no);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                //EXEC SQL CLOSE MRUP_MMOT01;
                MRUP_MMOT01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MMお届け先情報_BK
                SELECT * FROM MMお届け先情報
                WHERE ROWID = :h_rowid;
                */
                workSql = new StringDto("INSERT   INTO MMお届け先情報_BK\n" +
                        "                SELECT * FROM MMお届け先情報\n" +
                        "                WHERE ROW (顧客番号,お届け先番号) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MMお届け先情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MMOT01;
                    MRUP_MMOT01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MMお届け先情報_BK更新処理件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        //EXEC SQL CLOSE MRUP_MMOT01;
        MRUP_MMOT01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MMお届け先情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MMお届け先情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakusedo_bk                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakusedo_bk()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報_BK更新処理                                    */
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
    public int UpdateKokyakusedo_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MS顧客制度情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MS顧客制度情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MSKSC01 CURSOR FOR
        SELECT  ROWID,顧客番号
        FROM  MS顧客制度情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MSKSC01 = sqlcaManager.get("MRUP_MSKSC01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,顧客番号\n" +
                "        FROM  MS顧客制度情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MSKSC01.sql = workSql;

        //EXEC SQL OPEN MRUP_MSKSC01;
        MRUP_MSKSC01.declare();
        MRUP_MSKSC01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MSKSC01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MS顧客制度情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MSKSC01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_MSKSC01
            INTO :h_rowid, :h_uid;
            */
            MRUP_MSKSC01.fetch();
            MRUP_MSKSC01.recData(h_rowid, h_uid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MSKSC01.sqlcode != R_const_Ora_OK
                    && MRUP_MSKSC01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS顧客制度情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MSKSC01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSKSC01;
                MRUP_MSKSC01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MSKSC01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateKokyakusedo_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateKokyakusedo_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }
            /* 2023/05/29 MCCMPH2 MOD START */
            /* MS顧客制度情報_BK更新処理 */

            /*EXEC SQL UPDATE MS顧客制度情報_BK MM_BK
            SET (MM_BK.誕生月,
                    MM_BK.エントリー,
                    MM_BK.シニア,
                    MM_BK.年次ランクコード０,
                    MM_BK.年次ランクコード１,
                    MM_BK.年次ランクコード２,
                    MM_BK.年次ランクコード３,
                    MM_BK.年次ランクコード４,
                    MM_BK.年次ランクコード５,
                    MM_BK.年次ランクコード６,
                    MM_BK.年次ランクコード７,
                    MM_BK.年次ランクコード８,
                    MM_BK.年次ランクコード９,
                    MM_BK.月次ランクコード００１,
                    MM_BK.月次ランクコード００２,
                    MM_BK.月次ランクコード００３,
                    MM_BK.月次ランクコード００４,
                    MM_BK.月次ランクコード００５,
                    MM_BK.月次ランクコード００６,
                    MM_BK.月次ランクコード００７,
                    MM_BK.月次ランクコード００８,
                    MM_BK.月次ランクコード００９,
                    MM_BK.月次ランクコード０１０,
                    MM_BK.月次ランクコード０１１,
                    MM_BK.月次ランクコード０１２,
                    MM_BK.月次ランクコード１０１,
                    MM_BK.月次ランクコード１０２,
                    MM_BK.月次ランクコード１０３,
                    MM_BK.月次ランクコード１０４,
                    MM_BK.月次ランクコード１０５,
                    MM_BK.月次ランクコード１０６,
                    MM_BK.月次ランクコード１０７,
                    MM_BK.月次ランクコード１０８,
                    MM_BK.月次ランクコード１０９,
                    MM_BK.月次ランクコード１１０,
                    MM_BK.月次ランクコード１１１,
                    MM_BK.月次ランクコード１１２,
                    MM_BK.サークルＩＤ１,
                    MM_BK.サークルＩＤ２,
                    MM_BK.サークルＩＤ３,
                    MM_BK.サークルＩＤ４,
                    MM_BK.サークルＩＤ５,
                    MM_BK.在籍開始年月,
                    MM_BK.出産クーポン発行可否１,
                    MM_BK.出産クーポン発行可否２,
                    MM_BK.出産クーポン発行可否３,
                    MM_BK.社員区分,
                    MM_BK.ポータル会員フラグ,
                    MM_BK.ＥＣ会員フラグ,
                    MM_BK.モバイル会員フラグ,
                    MM_BK.電話番号登録フラグ,
                    MM_BK.静態取込済みフラグ,
                    MM_BK.家族ＩＤ,
                    MM_BK.フラグ１,
                    MM_BK.フラグ２,
                    MM_BK.フラグ３,
                    MM_BK.フラグ４,
                    MM_BK.フラグ５,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ,
                    MM_BK.提携先紐付登録年月日１,
                    MM_BK.提携先紐付解除年月日１,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_BK.アプリ会員フラグ
                    MM_BK.アプリ会員フラグ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_BK.顧客ステータス,
                    MM_BK.広告配信許諾フラグ,
                    MM_BK.広告配信許諾フラグ登録日,
                    MM_BK.広告配信許諾フラグ更新日時,
                    MM_BK.会員資格区分,
                    MM_BK.契約確定年月日,
                    MM_BK.契約管理種別,
                    MM_BK.クレジット希望フラグ,
                    MM_BK.グローバル会員フラグ,
                    MM_BK.グローバル会員国コード,
                    MM_BK.自国用会員番号,
                    MM_BK.ＬＩＮＥコネクト状況,
                    MM_BK.ＬＩＮＥコネクト状況登録日,
                    MM_BK.ＬＩＮＥコネクト状況更新日時,
                    MM_BK.ＭＣＣ制度許諾フラグ,
                    MM_BK.ＭＣＣ制度許諾更新者,
                    MM_BK.ＭＣＣ制度許諾更新日時,
                    MM_BK.コーポレート会員フラグ,
                    MM_BK.コーポレート会員ステータス,
                    MM_BK.コーポレート会員登録日,
                    MM_BK.属性管理主体システム,
                    MM_BK.サンプリング要否フラグ,
                    MM_BK.コーポレートＩＤ選択メール種別,
                    MM_BK.デジタル会員ＥＣ入会フラグ,
                    MM_BK.デジタル会員ＥＣ入会更新日時,
                    MM_BK.デジタル会員アプリ入会フラグ,
                    MM_BK.デジタル会員アプリ入会更新日時,
                    MM_BK.旧入会年月日
                    *//* 2022/11/25 MCCM初版 ADD END *//*
            ) = (
                    SELECT MM_I.誕生月,
                    MM_I.エントリー,
                    MM_I.シニア,
                    MM_I.年次ランクコード０,
                    MM_I.年次ランクコード１,
                    MM_I.年次ランクコード２,
                    MM_I.年次ランクコード３,
                    MM_I.年次ランクコード４,
                    MM_I.年次ランクコード５,
                    MM_I.年次ランクコード６,
                    MM_I.年次ランクコード７,
                    MM_I.年次ランクコード８,
                    MM_I.年次ランクコード９,
                    MM_I.月次ランクコード００１,
                    MM_I.月次ランクコード００２,
                    MM_I.月次ランクコード００３,
                    MM_I.月次ランクコード００４,
                    MM_I.月次ランクコード００５,
                    MM_I.月次ランクコード００６,
                    MM_I.月次ランクコード００７,
                    MM_I.月次ランクコード００８,
                    MM_I.月次ランクコード００９,
                    MM_I.月次ランクコード０１０,
                    MM_I.月次ランクコード０１１,
                    MM_I.月次ランクコード０１２,
                    MM_I.月次ランクコード１０１,
                    MM_I.月次ランクコード１０２,
                    MM_I.月次ランクコード１０３,
                    MM_I.月次ランクコード１０４,
                    MM_I.月次ランクコード１０５,
                    MM_I.月次ランクコード１０６,
                    MM_I.月次ランクコード１０７,
                    MM_I.月次ランクコード１０８,
                    MM_I.月次ランクコード１０９,
                    MM_I.月次ランクコード１１０,
                    MM_I.月次ランクコード１１１,
                    MM_I.月次ランクコード１１２,
                    MM_I.サークルＩＤ１,
                    MM_I.サークルＩＤ２,
                    MM_I.サークルＩＤ３,
                    MM_I.サークルＩＤ４,
                    MM_I.サークルＩＤ５,
                    MM_I.在籍開始年月,
                    MM_I.出産クーポン発行可否１,
                    MM_I.出産クーポン発行可否２,
                    MM_I.出産クーポン発行可否３,
                    MM_I.社員区分,
                    MM_I.ポータル会員フラグ,
                    MM_I.ＥＣ会員フラグ,
                    MM_I.モバイル会員フラグ,
                    MM_I.電話番号登録フラグ,
                    MM_I.静態取込済みフラグ,
                    MM_I.家族ＩＤ,
                    MM_I.フラグ１,
                    MM_I.フラグ２,
                    MM_I.フラグ３,
                    MM_I.フラグ４,
                    MM_I.フラグ５,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ,
                    MM_I.提携先紐付登録年月日１,
                    MM_I.提携先紐付解除年月日１,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_I.アプリ会員フラグ
                    MM_I.アプリ会員フラグ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_I.顧客ステータス,
                    MM_I.広告配信許諾フラグ,
                    MM_I.広告配信許諾フラグ登録日,
                    MM_I.広告配信許諾フラグ更新日時,
                    MM_I.会員資格区分,
                    MM_I.契約確定年月日,
                    MM_I.契約管理種別,
                    MM_I.クレジット希望フラグ,
                    MM_I.グローバル会員フラグ,
                    MM_I.グローバル会員国コード,
                    MM_I.自国用会員番号,
                    MM_I.ＬＩＮＥコネクト状況,
                    MM_I.ＬＩＮＥコネクト状況登録日,
                    MM_I.ＬＩＮＥコネクト状況更新日時,
                    MM_I.ＭＣＣ制度許諾フラグ,
                    MM_I.ＭＣＣ制度許諾更新者,
                    MM_I.ＭＣＣ制度許諾更新日時,
                    MM_I.コーポレート会員フラグ,
                    MM_I.コーポレート会員ステータス,
                    MM_I.コーポレート会員登録日,
                    MM_I.属性管理主体システム,
                    MM_I.サンプリング要否フラグ,
                    MM_I.コーポレートＩＤ選択メール種別,
                    MM_I.デジタル会員ＥＣ入会フラグ,
                    MM_I.デジタル会員ＥＣ入会更新日時,
                    MM_I.デジタル会員アプリ入会フラグ,
                    MM_I.デジタル会員アプリ入会更新日時,
                    MM_I.旧入会年月日
            *//* 2022/11/25 MCCM初版 ADD END *//*
            FROM MS顧客制度情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid;*/
            workSql = new StringDto("UPDATE MS顧客制度情報_BK MM_BK\n" +
                    "SET (誕生月,\n" +
                    "        エントリー,\n" +
                    "        シニア,\n" +
                    "        年次ランクコード０,\n" +
                    "        年次ランクコード１,\n" +
                    "        年次ランクコード２,\n" +
                    "        年次ランクコード３,\n" +
                    "        年次ランクコード４,\n" +
                    "        年次ランクコード５,\n" +
                    "        年次ランクコード６,\n" +
                    "        年次ランクコード７,\n" +
                    "        年次ランクコード８,\n" +
                    "        年次ランクコード９,\n" +
                    "        月次ランクコード００１,\n" +
                    "        月次ランクコード００２,\n" +
                    "        月次ランクコード００３,\n" +
                    "        月次ランクコード００４,\n" +
                    "        月次ランクコード００５,\n" +
                    "        月次ランクコード００６,\n" +
                    "        月次ランクコード００７,\n" +
                    "        月次ランクコード００８,\n" +
                    "        月次ランクコード００９,\n" +
                    "        月次ランクコード０１０,\n" +
                    "        月次ランクコード０１１,\n" +
                    "        月次ランクコード０１２,\n" +
                    "        月次ランクコード１０１,\n" +
                    "        月次ランクコード１０２,\n" +
                    "        月次ランクコード１０３,\n" +
                    "        月次ランクコード１０４,\n" +
                    "        月次ランクコード１０５,\n" +
                    "        月次ランクコード１０６,\n" +
                    "        月次ランクコード１０７,\n" +
                    "        月次ランクコード１０８,\n" +
                    "        月次ランクコード１０９,\n" +
                    "        月次ランクコード１１０,\n" +
                    "        月次ランクコード１１１,\n" +
                    "        月次ランクコード１１２,\n" +
                    "        サークルＩＤ１,\n" +
                    "        サークルＩＤ２,\n" +
                    "        サークルＩＤ３,\n" +
                    "        サークルＩＤ４,\n" +
                    "        サークルＩＤ５,\n" +
                    "        在籍開始年月,\n" +
                    "        出産クーポン発行可否１,\n" +
                    "        出産クーポン発行可否２,\n" +
                    "        出産クーポン発行可否３,\n" +
                    "        社員区分,\n" +
                    "        ポータル会員フラグ,\n" +
                    "        ＥＣ会員フラグ,\n" +
                    "        モバイル会員フラグ,\n" +
                    "        電話番号登録フラグ,\n" +
                    "        静態取込済みフラグ,\n" +
                    "        家族ＩＤ,\n" +
                    "        フラグ１,\n" +
                    "        フラグ２,\n" +
                    "        フラグ３,\n" +
                    "        フラグ４,\n" +
                    "        フラグ５,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ,\n" +
                    "        提携先紐付登録年月日１,\n" +
                    "        提携先紐付解除年月日１,\n" +
                    "        アプリ会員フラグ,\n" +
                    "        顧客ステータス,\n" +
                    "        広告配信許諾フラグ,\n" +
                    "        広告配信許諾フラグ登録日,\n" +
                    "        広告配信許諾フラグ更新日時,\n" +
                    "        会員資格区分,\n" +
                    "        契約確定年月日,\n" +
                    "        契約管理種別,\n" +
                    "        クレジット希望フラグ,\n" +
                    "        グローバル会員フラグ,\n" +
                    "        グローバル会員国コード,\n" +
                    "        自国用会員番号,\n" +
                    "        ＬＩＮＥコネクト状況,\n" +
                    "        ＬＩＮＥコネクト状況登録日,\n" +
                    "        ＬＩＮＥコネクト状況更新日時,\n" +
                    "        ＭＣＣ制度許諾フラグ,\n" +
                    "        ＭＣＣ制度許諾更新者,\n" +
                    "        ＭＣＣ制度許諾更新日時,\n" +
                    "        コーポレート会員フラグ,\n" +
                    "        コーポレート会員ステータス,\n" +
                    "        コーポレート会員登録日,\n" +
                    "        属性管理主体システム,\n" +
                    "        サンプリング要否フラグ,\n" +
                    "        コーポレートＩＤ選択メール種別,\n" +
                    "        デジタル会員ＥＣ入会フラグ,\n" +
                    "        デジタル会員ＥＣ入会更新日時,\n" +
                    "        デジタル会員アプリ入会フラグ,\n" +
                    "        デジタル会員アプリ入会更新日時,\n" +
                    "        旧入会年月日\n" +
                    ") = (\n" +
                    "        SELECT MM_I.誕生月,\n" +
                    "        MM_I.エントリー,\n" +
                    "        MM_I.シニア,\n" +
                    "        MM_I.年次ランクコード０,\n" +
                    "        MM_I.年次ランクコード１,\n" +
                    "        MM_I.年次ランクコード２,\n" +
                    "        MM_I.年次ランクコード３,\n" +
                    "        MM_I.年次ランクコード４,\n" +
                    "        MM_I.年次ランクコード５,\n" +
                    "        MM_I.年次ランクコード６,\n" +
                    "        MM_I.年次ランクコード７,\n" +
                    "        MM_I.年次ランクコード８,\n" +
                    "        MM_I.年次ランクコード９,\n" +
                    "        MM_I.月次ランクコード００１,\n" +
                    "        MM_I.月次ランクコード００２,\n" +
                    "        MM_I.月次ランクコード００３,\n" +
                    "        MM_I.月次ランクコード００４,\n" +
                    "        MM_I.月次ランクコード００５,\n" +
                    "        MM_I.月次ランクコード００６,\n" +
                    "        MM_I.月次ランクコード００７,\n" +
                    "        MM_I.月次ランクコード００８,\n" +
                    "        MM_I.月次ランクコード００９,\n" +
                    "        MM_I.月次ランクコード０１０,\n" +
                    "        MM_I.月次ランクコード０１１,\n" +
                    "        MM_I.月次ランクコード０１２,\n" +
                    "        MM_I.月次ランクコード１０１,\n" +
                    "        MM_I.月次ランクコード１０２,\n" +
                    "        MM_I.月次ランクコード１０３,\n" +
                    "        MM_I.月次ランクコード１０４,\n" +
                    "        MM_I.月次ランクコード１０５,\n" +
                    "        MM_I.月次ランクコード１０６,\n" +
                    "        MM_I.月次ランクコード１０７,\n" +
                    "        MM_I.月次ランクコード１０８,\n" +
                    "        MM_I.月次ランクコード１０９,\n" +
                    "        MM_I.月次ランクコード１１０,\n" +
                    "        MM_I.月次ランクコード１１１,\n" +
                    "        MM_I.月次ランクコード１１２,\n" +
                    "        MM_I.サークルＩＤ１,\n" +
                    "        MM_I.サークルＩＤ２,\n" +
                    "        MM_I.サークルＩＤ３,\n" +
                    "        MM_I.サークルＩＤ４,\n" +
                    "        MM_I.サークルＩＤ５,\n" +
                    "        MM_I.在籍開始年月,\n" +
                    "        MM_I.出産クーポン発行可否１,\n" +
                    "        MM_I.出産クーポン発行可否２,\n" +
                    "        MM_I.出産クーポン発行可否３,\n" +
                    "        MM_I.社員区分,\n" +
                    "        MM_I.ポータル会員フラグ,\n" +
                    "        MM_I.ＥＣ会員フラグ,\n" +
                    "        MM_I.モバイル会員フラグ,\n" +
                    "        MM_I.電話番号登録フラグ,\n" +
                    "        MM_I.静態取込済みフラグ,\n" +
                    "        MM_I.家族ＩＤ,\n" +
                    "        MM_I.フラグ１,\n" +
                    "        MM_I.フラグ２,\n" +
                    "        MM_I.フラグ３,\n" +
                    "        MM_I.フラグ４,\n" +
                    "        MM_I.フラグ５,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ,\n" +
                    "        MM_I.提携先紐付登録年月日１,\n" +
                    "        MM_I.提携先紐付解除年月日１,\n" +
                    "        MM_I.アプリ会員フラグ,\n" +
                    "        MM_I.顧客ステータス,\n" +
                    "        MM_I.広告配信許諾フラグ,\n" +
                    "        MM_I.広告配信許諾フラグ登録日,\n" +
                    "        MM_I.広告配信許諾フラグ更新日時,\n" +
                    "        MM_I.会員資格区分,\n" +
                    "        MM_I.契約確定年月日,\n" +
                    "        MM_I.契約管理種別,\n" +
                    "        MM_I.クレジット希望フラグ,\n" +
                    "        MM_I.グローバル会員フラグ,\n" +
                    "        MM_I.グローバル会員国コード,\n" +
                    "        MM_I.自国用会員番号,\n" +
                    "        MM_I.ＬＩＮＥコネクト状況,\n" +
                    "        MM_I.ＬＩＮＥコネクト状況登録日,\n" +
                    "        MM_I.ＬＩＮＥコネクト状況更新日時,\n" +
                    "        MM_I.ＭＣＣ制度許諾フラグ,\n" +
                    "        MM_I.ＭＣＣ制度許諾更新者,\n" +
                    "        MM_I.ＭＣＣ制度許諾更新日時,\n" +
                    "        MM_I.コーポレート会員フラグ,\n" +
                    "        MM_I.コーポレート会員ステータス,\n" +
                    "        MM_I.コーポレート会員登録日,\n" +
                    "        MM_I.属性管理主体システム,\n" +
                    "        MM_I.サンプリング要否フラグ,\n" +
                    "        MM_I.コーポレートＩＤ選択メール種別,\n" +
                    "        MM_I.デジタル会員ＥＣ入会フラグ,\n" +
                    "        MM_I.デジタル会員ＥＣ入会更新日時,\n" +
                    "        MM_I.デジタル会員アプリ入会フラグ,\n" +
                    "        MM_I.デジタル会員アプリ入会更新日時,\n" +
                    "        MM_I.旧入会年月日\n" +
                    "FROM MS顧客制度情報 MM_I\n" +
                    "WHERE ROW (MM_I.顧客番号) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "WHERE MM_BK.顧客番号 = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_uid);

            /* 2023/05/29 MCCMPH2 MOD END */
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS顧客制度情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSKSC01;
                MRUP_MSKSC01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MS顧客制度情報_BK
                SELECT * FROM MS顧客制度情報
                WHERE ROWID = :h_rowid;
                */
                workSql = new StringDto("INSERT   INTO MS顧客制度情報_BK\n" +
                        "                SELECT * FROM MS顧客制度情報\n" +
                        "                WHERE ROW (顧客番号) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MS顧客制度情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MSKSC01;
                    MRUP_MSKSC01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MS顧客制度情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_MSKSC01;
        MRUP_MSKSC01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MS顧客制度情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MS顧客制度情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;

    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateMscard_bk                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateMscard_bk()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MSカード情報_BK更新処理                                      */
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
    public int UpdateMscard_bk() {
        StringDto    out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MSカード情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MSカード情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MSCD01 CURSOR FOR
        SELECT  ROWID,会員番号,サービス種別
        FROM  MSカード情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MSCD01 = sqlcaManager.get("MRUP_MSCD01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(会員番号,サービス種別) AS TEXT)),'') AS ROWID,会員番号,サービス種別\n" +
                "        FROM  MSカード情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MSCD01.sql = workSql;

        // EXEC SQL OPEN MRUP_MSCD01;
        MRUP_MSCD01.declare();
        MRUP_MSCD01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MSCD01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MSカード情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MSCD01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_pid, 0x00, sizeof(h_pid));
            memset(h_svc, 0x00, sizeof(h_svc));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_MSCD01
            INTO :h_rowid, :h_pid, :h_svc;
            */
            MRUP_MSCD01.fetch();
            MRUP_MSCD01.recData(h_rowid, h_pid, h_svc);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MSCD01.sqlcode != R_const_Ora_OK
                    && MRUP_MSCD01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MSカード情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MSCD01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSCD01;
                MRUP_MSCD01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MSCD01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateMscard_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateMscard_bk *** 会員番号=[%s]\n", h_pid );
                /*-------------------------------------------------------------*/
            }

            /* MSカード情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MSカード情報_BK MM_BK
            SET (MM_BK.顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_BK.ＧＯＯＰＯＮ番号,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    MM_BK.カードステータス,
                    MM_BK.理由コード,
                    MM_BK.発行年月日,
                    MM_BK.終了年月日,
                    MM_BK.有効期限,
                    MM_BK.企業コード,
                    MM_BK.旧販社コード,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ
            ) = (
                    SELECT MM_I.顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_I.ＧＯＯＰＯＮ番号,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    MM_I.カードステータス,
                    MM_I.理由コード,
                    MM_I.発行年月日,
                    MM_I.終了年月日,
                    MM_I.有効期限,
                    MM_I.企業コード,
                    MM_I.旧販社コード,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ
            FROM MSカード情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.会員番号=MM_BK.会員番号
            AND MM_I.サービス種別=MM_BK.サービス種別)
            WHERE MM_BK.会員番号 = :h_pid
            AND MM_BK.サービス種別 = :h_svc;
            */
            workSql = new StringDto("UPDATE MSカード情報_BK MM_BK\n" +
                    "SET (顧客番号,\n" +
                    "        ＧＯＯＰＯＮ番号,\n" +
                    "        カードステータス,\n" +
                    "        理由コード,\n" +
                    "        発行年月日,\n" +
                    "        終了年月日,\n" +
                    "        有効期限,\n" +
                    "        企業コード,\n" +
                    "        旧販社コード,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ\n" +
                    ") = (\n" +
                    "        SELECT MM_I.顧客番号,\n" +
                    "        MM_I.ＧＯＯＰＯＮ番号,\n" +
                    "        MM_I.カードステータス,\n" +
                    "        MM_I.理由コード,\n" +
                    "        MM_I.発行年月日,\n" +
                    "        MM_I.終了年月日,\n" +
                    "        MM_I.有効期限,\n" +
                    "        MM_I.企業コード,\n" +
                    "        MM_I.旧販社コード,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ\n" +
                    "FROM MSカード情報 MM_I\n" +
                    "WHERE ROW (MM_I.会員番号,MM_I.サービス種別) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.会員番号=MM_BK.会員番号\n" +
                    "AND MM_I.サービス種別=MM_BK.サービス種別)\n" +
                    "WHERE MM_BK.会員番号 = ?\n" +
                    "AND MM_BK.サービス種別 = ?");
            sqlca.sql = workSql;
            sqlca.restAndExecute(h_rowid, h_pid, h_svc);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MSカード情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],会員番号-[%s]\n", sqlca.sqlcode,h_pid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSCD01;
                MRUP_MSCD01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MSカード情報_BK
                SELECT * FROM MSカード情報
                WHERE ROWID = :h_rowid;
                */
                workSql = new StringDto("INSERT   INTO MSカード情報_BK\n" +
                        "                SELECT * FROM MSカード情報\n" +
                        "                WHERE ROW (会員番号,サービス種別) = (SELECT ? FROM DUAL)");
                sqlca.sql = workSql;
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MSカード情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],会員番号-[%s]\n", sqlca.sqlcode,h_pid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MSCD01;
                    MRUP_MSCD01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MSカード情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_MSCD01;
        MRUP_MSCD01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MSカード情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MSカード情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateRiyoukapoint_bk                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateRiyoukapoint_bk()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TS利用可能ポイント情報_BK更新処理                            */
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
    public int UpdateRiyoukapoint_bk() {
        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "TS利用可能ポイント情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* TS利用可能ポイント情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_TSRY01 CURSOR FOR
        SELECT  ROWID,顧客番号
        FROM  TS利用可能ポイント情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_TSRY01 = sqlcaManager.get("MRUP_TSRY01");
        MRUP_TSRY01.sql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,顧客番号\n" +
                "        FROM  TS利用可能ポイント情報\n" +
                "        WHERE 最終更新日   >= ?");

        // EXEC SQL OPEN MRUP_TSRY01;
        MRUP_TSRY01.declare();
        MRUP_TSRY01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_TSRY01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_TSRY01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_TSRY01
            INTO :h_rowid, :h_uid;
            */
            MRUP_TSRY01.fetchInto(h_rowid, h_uid);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_TSRY01.sqlcode != R_const_Ora_OK
                    && MRUP_TSRY01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_TSRY01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSRY01;
                MRUP_TSRY01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_TSRY01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateRiyoukapoint_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateRiyoukapoint_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }

            /* TS利用可能ポイント情報_BK更新処理 */
            /* 2020.12.22 TS利用可能ポイント情報カラム追加 Sta  */
            /*
            EXEC SQL UPDATE TS利用可能ポイント情報_BK MM_BK
            SET (MM_BK.利用可能通常Ｐ１,
                    MM_BK.利用可能ポイントＬＦ,
                    MM_BK.最終買上日,
                    MM_BK.入会企業コード,
                    MM_BK.入会店舗,
                    MM_BK.入会旧販社コード,
                    MM_BK.発券企業コード,
                    MM_BK.発券店舗,
                    MM_BK.バースディクーポン発行日,
                    MM_BK.在籍期間クーポン発行日,
                    MM_BK.出産クーポン発行日１,
                    MM_BK.出産クーポン発行日２,
                    MM_BK.出産クーポン発行日３,
                    MM_BK.出産クーポン発行区分１,
                    MM_BK.出産クーポン発行区分２,
                    MM_BK.出産クーポン発行区分３,
                    MM_BK.全会員クーポン発行日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ,
                    MM_BK.チャージ実施日,
                    MM_BK.利用可能通常Ｐ０,
                    MM_BK.利用可能通常Ｐ２,
                    MM_BK.利用可能通常Ｐ３,
                    MM_BK.利用可能通常Ｐ４,
                    MM_BK.通常Ｐ失効フラグ,
                    MM_BK.利用可能期間限定Ｐ０１,
                    MM_BK.利用可能期間限定Ｐ０２,
                    MM_BK.利用可能期間限定Ｐ０３,
                    MM_BK.利用可能期間限定Ｐ０４,
                    MM_BK.利用可能期間限定Ｐ０５,
                    MM_BK.利用可能期間限定Ｐ０６,
                    MM_BK.利用可能期間限定Ｐ０７,
                    MM_BK.利用可能期間限定Ｐ０８,
                    MM_BK.利用可能期間限定Ｐ０９,
                    MM_BK.利用可能期間限定Ｐ１０,
                    MM_BK.利用可能期間限定Ｐ１１,
                    MM_BK.利用可能期間限定Ｐ１２,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_BK.期間限定Ｐ失効フラグ
                    MM_BK.期間限定Ｐ失効フラグ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_BK.利用可能通常購買Ｐ０,
                    MM_BK.利用可能通常購買Ｐ１,
                    MM_BK.利用可能通常購買Ｐ２,
                    MM_BK.利用可能通常購買Ｐ３,
                    MM_BK.利用可能通常購買Ｐ４,
                    MM_BK.利用可能通常非購買Ｐ０,
                    MM_BK.利用可能通常非購買Ｐ１,
                    MM_BK.利用可能通常非購買Ｐ２,
                    MM_BK.利用可能通常非購買Ｐ３,
                    MM_BK.利用可能通常非購買Ｐ４,
                    MM_BK.利用可能通常その他Ｐ０,
                    MM_BK.利用可能通常その他Ｐ１,
                    MM_BK.利用可能通常その他Ｐ２,
                    MM_BK.利用可能通常その他Ｐ３,
                    MM_BK.利用可能通常その他Ｐ４,
                    MM_BK.利用可能期間限定購買Ｐ０１,
                    MM_BK.利用可能期間限定購買Ｐ０２,
                    MM_BK.利用可能期間限定購買Ｐ０３,
                    MM_BK.利用可能期間限定購買Ｐ０４,
                    MM_BK.利用可能期間限定購買Ｐ０５,
                    MM_BK.利用可能期間限定購買Ｐ０６,
                    MM_BK.利用可能期間限定購買Ｐ０７,
                    MM_BK.利用可能期間限定購買Ｐ０８,
                    MM_BK.利用可能期間限定購買Ｐ０９,
                    MM_BK.利用可能期間限定購買Ｐ１０,
                    MM_BK.利用可能期間限定購買Ｐ１１,
                    MM_BK.利用可能期間限定購買Ｐ１２,
                    MM_BK.利用可能期間限定非購買Ｐ０１,
                    MM_BK.利用可能期間限定非購買Ｐ０２,
                    MM_BK.利用可能期間限定非購買Ｐ０３,
                    MM_BK.利用可能期間限定非購買Ｐ０４,
                    MM_BK.利用可能期間限定非購買Ｐ０５,
                    MM_BK.利用可能期間限定非購買Ｐ０６,
                    MM_BK.利用可能期間限定非購買Ｐ０７,
                    MM_BK.利用可能期間限定非購買Ｐ０８,
                    MM_BK.利用可能期間限定非購買Ｐ０９,
                    MM_BK.利用可能期間限定非購買Ｐ１０,
                    MM_BK.利用可能期間限定非購買Ｐ１１,
                    MM_BK.利用可能期間限定非購買Ｐ１２,
                    MM_BK.利用可能期間限定その他Ｐ０１,
                    MM_BK.利用可能期間限定その他Ｐ０２,
                    MM_BK.利用可能期間限定その他Ｐ０３,
                    MM_BK.利用可能期間限定その他Ｐ０４,
                    MM_BK.利用可能期間限定その他Ｐ０５,
                    MM_BK.利用可能期間限定その他Ｐ０６,
                    MM_BK.利用可能期間限定その他Ｐ０７,
                    MM_BK.利用可能期間限定その他Ｐ０８,
                    MM_BK.利用可能期間限定その他Ｐ０９,
                    MM_BK.利用可能期間限定その他Ｐ１０,
                    MM_BK.利用可能期間限定その他Ｐ１１,
                    MM_BK.利用可能期間限定その他Ｐ１２,
                    MM_BK.入会会社コードＭＣＣ,
                    MM_BK.入会店舗ＭＣＣ,
                    MM_BK.来店ポイント付与日
                    *//* 2022/11/25 MCCM初版 ADD END *//*
            ) = (
                    SELECT MM_I.利用可能通常Ｐ１,
                    MM_I.利用可能ポイントＬＦ,
                    MM_I.最終買上日,
                    MM_I.入会企業コード,
                    MM_I.入会店舗,
                    MM_I.入会旧販社コード,
                    MM_I.発券企業コード,
                    MM_I.発券店舗,
                    MM_I.バースディクーポン発行日,
                    MM_I.在籍期間クーポン発行日,
                    MM_I.出産クーポン発行日１,
                    MM_I.出産クーポン発行日２,
                    MM_I.出産クーポン発行日３,
                    MM_I.出産クーポン発行区分１,
                    MM_I.出産クーポン発行区分２,
                    MM_I.出産クーポン発行区分３,
                    MM_I.全会員クーポン発行日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ,
                    MM_I.チャージ実施日,
                    MM_I.利用可能通常Ｐ０,
                    MM_I.利用可能通常Ｐ２,
                    MM_I.利用可能通常Ｐ３,
                    MM_I.利用可能通常Ｐ４,
                    MM_I.通常Ｐ失効フラグ,
                    MM_I.利用可能期間限定Ｐ０１,
                    MM_I.利用可能期間限定Ｐ０２,
                    MM_I.利用可能期間限定Ｐ０３,
                    MM_I.利用可能期間限定Ｐ０４,
                    MM_I.利用可能期間限定Ｐ０５,
                    MM_I.利用可能期間限定Ｐ０６,
                    MM_I.利用可能期間限定Ｐ０７,
                    MM_I.利用可能期間限定Ｐ０８,
                    MM_I.利用可能期間限定Ｐ０９,
                    MM_I.利用可能期間限定Ｐ１０,
                    MM_I.利用可能期間限定Ｐ１１,
                    MM_I.利用可能期間限定Ｐ１２,
                    *//* 2022/11/25 MCCM初版 MOD START *//*
//                      MM_I.期間限定Ｐ失効フラグ
                    MM_I.期間限定Ｐ失効フラグ,
                    *//* 2022/11/25 MCCM初版 MOD END *//*
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    MM_I.利用可能通常購買Ｐ０,
                    MM_I.利用可能通常購買Ｐ１,
                    MM_I.利用可能通常購買Ｐ２,
                    MM_I.利用可能通常購買Ｐ３,
                    MM_I.利用可能通常購買Ｐ４,
                    MM_I.利用可能通常非購買Ｐ０,
                    MM_I.利用可能通常非購買Ｐ１,
                    MM_I.利用可能通常非購買Ｐ２,
                    MM_I.利用可能通常非購買Ｐ３,
                    MM_I.利用可能通常非購買Ｐ４,
                    MM_I.利用可能通常その他Ｐ０,
                    MM_I.利用可能通常その他Ｐ１,
                    MM_I.利用可能通常その他Ｐ２,
                    MM_I.利用可能通常その他Ｐ３,
                    MM_I.利用可能通常その他Ｐ４,
                    MM_I.利用可能期間限定購買Ｐ０１,
                    MM_I.利用可能期間限定購買Ｐ０２,
                    MM_I.利用可能期間限定購買Ｐ０３,
                    MM_I.利用可能期間限定購買Ｐ０４,
                    MM_I.利用可能期間限定購買Ｐ０５,
                    MM_I.利用可能期間限定購買Ｐ０６,
                    MM_I.利用可能期間限定購買Ｐ０７,
                    MM_I.利用可能期間限定購買Ｐ０８,
                    MM_I.利用可能期間限定購買Ｐ０９,
                    MM_I.利用可能期間限定購買Ｐ１０,
                    MM_I.利用可能期間限定購買Ｐ１１,
                    MM_I.利用可能期間限定購買Ｐ１２,
                    MM_I.利用可能期間限定非購買Ｐ０１,
                    MM_I.利用可能期間限定非購買Ｐ０２,
                    MM_I.利用可能期間限定非購買Ｐ０３,
                    MM_I.利用可能期間限定非購買Ｐ０４,
                    MM_I.利用可能期間限定非購買Ｐ０５,
                    MM_I.利用可能期間限定非購買Ｐ０６,
                    MM_I.利用可能期間限定非購買Ｐ０７,
                    MM_I.利用可能期間限定非購買Ｐ０８,
                    MM_I.利用可能期間限定非購買Ｐ０９,
                    MM_I.利用可能期間限定非購買Ｐ１０,
                    MM_I.利用可能期間限定非購買Ｐ１１,
                    MM_I.利用可能期間限定非購買Ｐ１２,
                    MM_I.利用可能期間限定その他Ｐ０１,
                    MM_I.利用可能期間限定その他Ｐ０２,
                    MM_I.利用可能期間限定その他Ｐ０３,
                    MM_I.利用可能期間限定その他Ｐ０４,
                    MM_I.利用可能期間限定その他Ｐ０５,
                    MM_I.利用可能期間限定その他Ｐ０６,
                    MM_I.利用可能期間限定その他Ｐ０７,
                    MM_I.利用可能期間限定その他Ｐ０８,
                    MM_I.利用可能期間限定その他Ｐ０９,
                    MM_I.利用可能期間限定その他Ｐ１０,
                    MM_I.利用可能期間限定その他Ｐ１１,
                    MM_I.利用可能期間限定その他Ｐ１２,
                    MM_I.入会会社コードＭＣＣ,
                    MM_I.入会店舗ＭＣＣ,
                    MM_I.来店ポイント付与日
            *//* 2022/11/25 MCCM初版 ADD END *//*
            FROM TS利用可能ポイント情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.顧客番号=MM_BK.顧客番号)
            WHERE MM_BK.顧客番号 = :h_uid;
            */
            sqlca.sql = new StringDto("UPDATE TS利用可能ポイント情報_BK MM_BK\n" +
                    "SET (利用可能通常Ｐ１,\n" +
                    "        利用可能ポイントＬＦ,\n" +
                    "        最終買上日,\n" +
                    "        入会企業コード,\n" +
                    "        入会店舗,\n" +
                    "        入会旧販社コード,\n" +
                    "        発券企業コード,\n" +
                    "        発券店舗,\n" +
                    "        バースディクーポン発行日,\n" +
                    "        在籍期間クーポン発行日,\n" +
                    "        出産クーポン発行日１,\n" +
                    "        出産クーポン発行日２,\n" +
                    "        出産クーポン発行日３,\n" +
                    "        出産クーポン発行区分１,\n" +
                    "        出産クーポン発行区分２,\n" +
                    "        出産クーポン発行区分３,\n" +
                    "        全会員クーポン発行日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ,\n" +
                    "        チャージ実施日,\n" +
                    "        利用可能通常Ｐ０,\n" +
                    "        利用可能通常Ｐ２,\n" +
                    "        利用可能通常Ｐ３,\n" +
                    "        利用可能通常Ｐ４,\n" +
                    "        通常Ｐ失効フラグ,\n" +
                    "        利用可能期間限定Ｐ０１,\n" +
                    "        利用可能期間限定Ｐ０２,\n" +
                    "        利用可能期間限定Ｐ０３,\n" +
                    "        利用可能期間限定Ｐ０４,\n" +
                    "        利用可能期間限定Ｐ０５,\n" +
                    "        利用可能期間限定Ｐ０６,\n" +
                    "        利用可能期間限定Ｐ０７,\n" +
                    "        利用可能期間限定Ｐ０８,\n" +
                    "        利用可能期間限定Ｐ０９,\n" +
                    "        利用可能期間限定Ｐ１０,\n" +
                    "        利用可能期間限定Ｐ１１,\n" +
                    "        利用可能期間限定Ｐ１２,\n" +
                    "        期間限定Ｐ失効フラグ,\n" +
                    "        利用可能通常購買Ｐ０,\n" +
                    "        利用可能通常購買Ｐ１,\n" +
                    "        利用可能通常購買Ｐ２,\n" +
                    "        利用可能通常購買Ｐ３,\n" +
                    "        利用可能通常購買Ｐ４,\n" +
                    "        利用可能通常非購買Ｐ０,\n" +
                    "        利用可能通常非購買Ｐ１,\n" +
                    "        利用可能通常非購買Ｐ２,\n" +
                    "        利用可能通常非購買Ｐ３,\n" +
                    "        利用可能通常非購買Ｐ４,\n" +
                    "        利用可能通常その他Ｐ０,\n" +
                    "        利用可能通常その他Ｐ１,\n" +
                    "        利用可能通常その他Ｐ２,\n" +
                    "        利用可能通常その他Ｐ３,\n" +
                    "        利用可能通常その他Ｐ４,\n" +
                    "        利用可能期間限定購買Ｐ０１,\n" +
                    "        利用可能期間限定購買Ｐ０２,\n" +
                    "        利用可能期間限定購買Ｐ０３,\n" +
                    "        利用可能期間限定購買Ｐ０４,\n" +
                    "        利用可能期間限定購買Ｐ０５,\n" +
                    "        利用可能期間限定購買Ｐ０６,\n" +
                    "        利用可能期間限定購買Ｐ０７,\n" +
                    "        利用可能期間限定購買Ｐ０８,\n" +
                    "        利用可能期間限定購買Ｐ０９,\n" +
                    "        利用可能期間限定購買Ｐ１０,\n" +
                    "        利用可能期間限定購買Ｐ１１,\n" +
                    "        利用可能期間限定購買Ｐ１２,\n" +
                    "        利用可能期間限定非購買Ｐ０１,\n" +
                    "        利用可能期間限定非購買Ｐ０２,\n" +
                    "        利用可能期間限定非購買Ｐ０３,\n" +
                    "        利用可能期間限定非購買Ｐ０４,\n" +
                    "        利用可能期間限定非購買Ｐ０５,\n" +
                    "        利用可能期間限定非購買Ｐ０６,\n" +
                    "        利用可能期間限定非購買Ｐ０７,\n" +
                    "        利用可能期間限定非購買Ｐ０８,\n" +
                    "        利用可能期間限定非購買Ｐ０９,\n" +
                    "        利用可能期間限定非購買Ｐ１０,\n" +
                    "        利用可能期間限定非購買Ｐ１１,\n" +
                    "        利用可能期間限定非購買Ｐ１２,\n" +
                    "        利用可能期間限定その他Ｐ０１,\n" +
                    "        利用可能期間限定その他Ｐ０２,\n" +
                    "        利用可能期間限定その他Ｐ０３,\n" +
                    "        利用可能期間限定その他Ｐ０４,\n" +
                    "        利用可能期間限定その他Ｐ０５,\n" +
                    "        利用可能期間限定その他Ｐ０６,\n" +
                    "        利用可能期間限定その他Ｐ０７,\n" +
                    "        利用可能期間限定その他Ｐ０８,\n" +
                    "        利用可能期間限定その他Ｐ０９,\n" +
                    "        利用可能期間限定その他Ｐ１０,\n" +
                    "        利用可能期間限定その他Ｐ１１,\n" +
                    "        利用可能期間限定その他Ｐ１２,\n" +
                    "        入会会社コードＭＣＣ,\n" +
                    "        入会店舗ＭＣＣ,\n" +
                    "        来店ポイント付与日\n" +
                    ") = (\n" +
                    "        SELECT MM_I.利用可能通常Ｐ１,\n" +
                    "        MM_I.利用可能ポイントＬＦ,\n" +
                    "        MM_I.最終買上日,\n" +
                    "        MM_I.入会企業コード,\n" +
                    "        MM_I.入会店舗,\n" +
                    "        MM_I.入会旧販社コード,\n" +
                    "        MM_I.発券企業コード,\n" +
                    "        MM_I.発券店舗,\n" +
                    "        MM_I.バースディクーポン発行日,\n" +
                    "        MM_I.在籍期間クーポン発行日,\n" +
                    "        MM_I.出産クーポン発行日１,\n" +
                    "        MM_I.出産クーポン発行日２,\n" +
                    "        MM_I.出産クーポン発行日３,\n" +
                    "        MM_I.出産クーポン発行区分１,\n" +
                    "        MM_I.出産クーポン発行区分２,\n" +
                    "        MM_I.出産クーポン発行区分３,\n" +
                    "        MM_I.全会員クーポン発行日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ,\n" +
                    "        MM_I.チャージ実施日,\n" +
                    "        MM_I.利用可能通常Ｐ０,\n" +
                    "        MM_I.利用可能通常Ｐ２,\n" +
                    "        MM_I.利用可能通常Ｐ３,\n" +
                    "        MM_I.利用可能通常Ｐ４,\n" +
                    "        MM_I.通常Ｐ失効フラグ,\n" +
                    "        MM_I.利用可能期間限定Ｐ０１,\n" +
                    "        MM_I.利用可能期間限定Ｐ０２,\n" +
                    "        MM_I.利用可能期間限定Ｐ０３,\n" +
                    "        MM_I.利用可能期間限定Ｐ０４,\n" +
                    "        MM_I.利用可能期間限定Ｐ０５,\n" +
                    "        MM_I.利用可能期間限定Ｐ０６,\n" +
                    "        MM_I.利用可能期間限定Ｐ０７,\n" +
                    "        MM_I.利用可能期間限定Ｐ０８,\n" +
                    "        MM_I.利用可能期間限定Ｐ０９,\n" +
                    "        MM_I.利用可能期間限定Ｐ１０,\n" +
                    "        MM_I.利用可能期間限定Ｐ１１,\n" +
                    "        MM_I.利用可能期間限定Ｐ１２,\n" +
                    "        MM_I.期間限定Ｐ失効フラグ,\n" +
                    "        MM_I.利用可能通常購買Ｐ０,\n" +
                    "        MM_I.利用可能通常購買Ｐ１,\n" +
                    "        MM_I.利用可能通常購買Ｐ２,\n" +
                    "        MM_I.利用可能通常購買Ｐ３,\n" +
                    "        MM_I.利用可能通常購買Ｐ４,\n" +
                    "        MM_I.利用可能通常非購買Ｐ０,\n" +
                    "        MM_I.利用可能通常非購買Ｐ１,\n" +
                    "        MM_I.利用可能通常非購買Ｐ２,\n" +
                    "        MM_I.利用可能通常非購買Ｐ３,\n" +
                    "        MM_I.利用可能通常非購買Ｐ４,\n" +
                    "        MM_I.利用可能通常その他Ｐ０,\n" +
                    "        MM_I.利用可能通常その他Ｐ１,\n" +
                    "        MM_I.利用可能通常その他Ｐ２,\n" +
                    "        MM_I.利用可能通常その他Ｐ３,\n" +
                    "        MM_I.利用可能通常その他Ｐ４,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０１,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０２,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０３,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０４,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０５,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０６,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０７,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０８,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ０９,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ１０,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ１１,\n" +
                    "        MM_I.利用可能期間限定購買Ｐ１２,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０１,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０２,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０３,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０４,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０５,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０６,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０７,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０８,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ０９,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ１０,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ１１,\n" +
                    "        MM_I.利用可能期間限定非購買Ｐ１２,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０１,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０２,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０３,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０４,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０５,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０６,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０７,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０８,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ０９,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ１０,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ１１,\n" +
                    "        MM_I.利用可能期間限定その他Ｐ１２,\n" +
                    "        MM_I.入会会社コードＭＣＣ,\n" +
                    "        MM_I.入会店舗ＭＣＣ,\n" +
                    "        MM_I.来店ポイント付与日\n" +
                    "FROM TS利用可能ポイント情報 MM_I\n" +
                    "WHERE ROW (MM_I.顧客番号) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.顧客番号=MM_BK.顧客番号)\n" +
                    "WHERE MM_BK.顧客番号 = ?");
            sqlca.restAndExecute(h_rowid, h_uid);

            /* 2020.12.22 TS利用可能ポイント情報カラム追加 End  */
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSRY01;
                MRUP_TSRY01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO TS利用可能ポイント情報_BK
                SELECT * FROM TS利用可能ポイント情報
                WHERE ROWID = :h_rowid;
                */
                sqlca.sql = new StringDto("INSERT   INTO TS利用可能ポイント情報_BK\n" +
                        "                SELECT * FROM TS利用可能ポイント情報\n" +
                        "                WHERE ROW (顧客番号) = (SELECT ? FROM DUAL)");
                sqlca.restAndExecute(h_rowid);

                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号-[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_TSRY01;
                    MRUP_TSRY01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* TS利用可能ポイント情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_TSRY01;
        MRUP_TSRY01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "TS利用可能ポイント情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "TS利用可能ポイント情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdatePointyear_bk                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdatePointyear_bk()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TSポイント年別情報_BK更新処理                                */
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
    public int UpdatePointyear_bk() {
        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/
        StringDto wk_sql = new StringDto(R_const_SQLMaxLen);      /** 動的SQLバッファ                  **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "TSポイント年別情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }

        /* ＳＱＬ文をセットする */
        sprintf(wk_sql,
                "SELECT NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,年,顧客番号 FROM %s " +
                "WHERE  最終更新日  >=  ?", arg_t_Value);

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql );

        /* 動的ＳＱＬ文を解析する */
        //EXEC SQL PREPARE sql_stat1 from :str_sql;
        SqlstmDto MRUP_TSPT01 = sqlcaManager.get("sql_stat1");
        MRUP_TSPT01.sql = wk_sql;

        MRUP_TSPT01.prepare();
        if (MRUP_TSPT01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理 動的SQL解析エラー sqlca.sqlcode=[%d]\n", MRUP_TSPT01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        // EXEC SQL  DECLARE MRUP_TSPT01 cursor for sql_stat1;
        MRUP_TSPT01.declare();

        // EXEC SQL  OPEN MRUP_TSPT01 USING :bt_date;
        MRUP_TSPT01.open(bt_date);
        if (MRUP_TSPT01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_TSPT01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            h_year.arr = 0;
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_TSPT01
            INTO :h_rowid,
                      :h_year,
                      :h_uid;
            */
            MRUP_TSPT01.fetchInto(h_rowid, h_year, h_uid);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_TSPT01.sqlcode != R_const_Ora_OK
                    && MRUP_TSPT01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_TSPT01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSPT01;
                MRUP_TSPT01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_TSPT01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdatePointyear_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdatePointyear_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }

            memset(wk_sql, 0x00, sizeof(wk_sql));
            memset(str_sql, 0x00, sizeof(str_sql));
            /* ＳＱＬ文をセットする */
            sprintf(wk_sql,
                    "UPDATE %s_BK MM_BK " +
                    " SET (年間付与ポイント, " +
                    " 年間利用ポイント," +
                    " 年間基本Ｐ率対象ポイント," +
                    " 年間ランクＵＰ対象金額," +
                    " 年間ポイント対象金額," +
                    " 年間買上額," +
                    " 年間買上回数," +
                    " 年間買上日数," +
                    " 年間買物券発行ポイント," +
                    " 月間付与ポイント０１," +
                    " 月間付与ポイント０２," +
                    " 月間付与ポイント０３," +
                    " 月間付与ポイント０４," +
                    " 月間付与ポイント０５," +
                    " 月間付与ポイント０６," +
                    " 月間付与ポイント０７," +
                    " 月間付与ポイント０８," +
                    " 月間付与ポイント０９," +
                    " 月間付与ポイント１０," +
                    " 月間付与ポイント１１," +
                    " 月間付与ポイント１２," +
                    " 月間利用ポイント０１," +
                    " 月間利用ポイント０２," +
                    " 月間利用ポイント０３," +
                    " 月間利用ポイント０４," +
                    " 月間利用ポイント０５," +
                    " 月間利用ポイント０６," +
                    " 月間利用ポイント０７," +
                    " 月間利用ポイント０８," +
                    " 月間利用ポイント０９," +
                    " 月間利用ポイント１０," +
                    " 月間利用ポイント１１," +
                    " 月間利用ポイント１２," +
                    " 月間ランクＵＰ対象金額０１," +
                    " 月間ランクＵＰ対象金額０２," +
                    " 月間ランクＵＰ対象金額０３," +
                    " 月間ランクＵＰ対象金額０４," +
                    " 月間ランクＵＰ対象金額０５," +
                    " 月間ランクＵＰ対象金額０６," +
                    " 月間ランクＵＰ対象金額０７," +
                    " 月間ランクＵＰ対象金額０８," +
                    " 月間ランクＵＰ対象金額０９," +
                    " 月間ランクＵＰ対象金額１０," +
                    " 月間ランクＵＰ対象金額１１," +
                    " 月間ランクＵＰ対象金額１２," +
                    " 月間買上回数０１," +
                    " 月間買上回数０２," +
                    " 月間買上回数０３," +
                    " 月間買上回数０４," +
                    " 月間買上回数０５," +
                    " 月間買上回数０６," +
                    " 月間買上回数０７," +
                    " 月間買上回数０８," +
                    " 月間買上回数０９," +
                    " 月間買上回数１０," +
                    " 月間買上回数１１," +
                    " 月間買上回数１２," +
                    " 最終更新日," +
                    " 最終更新日時," +
                    " 最終更新プログラムＩＤ " +
                    " ) = ( " +
                    " SELECT MM_I.年間付与ポイント, " +
                    " MM_I.年間利用ポイント," +
                    " MM_I.年間基本Ｐ率対象ポイント," +
                    " MM_I.年間ランクＵＰ対象金額," +
                    " MM_I.年間ポイント対象金額," +
                    " MM_I.年間買上額," +
                    " MM_I.年間買上回数," +
                    " MM_I.年間買上日数," +
                    " MM_I.年間買物券発行ポイント," +
                    " MM_I.月間付与ポイント０１," +
                    " MM_I.月間付与ポイント０２," +
                    " MM_I.月間付与ポイント０３," +
                    " MM_I.月間付与ポイント０４," +
                    " MM_I.月間付与ポイント０５," +
                    " MM_I.月間付与ポイント０６," +
                    " MM_I.月間付与ポイント０７," +
                    " MM_I.月間付与ポイント０８," +
                    " MM_I.月間付与ポイント０９," +
                    " MM_I.月間付与ポイント１０," +
                    " MM_I.月間付与ポイント１１," +
                    " MM_I.月間付与ポイント１２," +
                    " MM_I.月間利用ポイント０１," +
                    " MM_I.月間利用ポイント０２," +
                    " MM_I.月間利用ポイント０３," +
                    " MM_I.月間利用ポイント０４," +
                    " MM_I.月間利用ポイント０５," +
                    " MM_I.月間利用ポイント０６," +
                    " MM_I.月間利用ポイント０７," +
                    " MM_I.月間利用ポイント０８," +
                    " MM_I.月間利用ポイント０９," +
                    " MM_I.月間利用ポイント１０," +
                    " MM_I.月間利用ポイント１１," +
                    " MM_I.月間利用ポイント１２," +
                    " MM_I.月間ランクＵＰ対象金額０１," +
                    " MM_I.月間ランクＵＰ対象金額０２," +
                    " MM_I.月間ランクＵＰ対象金額０３," +
                    " MM_I.月間ランクＵＰ対象金額０４," +
                    " MM_I.月間ランクＵＰ対象金額０５," +
                    " MM_I.月間ランクＵＰ対象金額０６," +
                    " MM_I.月間ランクＵＰ対象金額０７," +
                    " MM_I.月間ランクＵＰ対象金額０８," +
                    " MM_I.月間ランクＵＰ対象金額０９," +
                    " MM_I.月間ランクＵＰ対象金額１０," +
                    " MM_I.月間ランクＵＰ対象金額１１," +
                    " MM_I.月間ランクＵＰ対象金額１２," +
                    " MM_I.月間買上回数０１," +
                    " MM_I.月間買上回数０２," +
                    " MM_I.月間買上回数０３," +
                    " MM_I.月間買上回数０４," +
                    " MM_I.月間買上回数０５," +
                    " MM_I.月間買上回数０６," +
                    " MM_I.月間買上回数０７," +
                    " MM_I.月間買上回数０８," +
                    " MM_I.月間買上回数０９," +
                    " MM_I.月間買上回数１０," +
                    " MM_I.月間買上回数１１," +
                    " MM_I.月間買上回数１２," +
                    " MM_I.最終更新日," +
                    " MM_I.最終更新日時," +
                    " MM_I.最終更新プログラムＩＤ " +
                    " FROM %s MM_I " +
                    " WHERE ROW (MM_I.年,MM_I.顧客番号) =(SELECT ? FROM DUAL) " +
                    "   AND MM_I.年=MM_BK.年 " +
                    "   AND MM_I.顧客番号=MM_BK.顧客番号) " +
                    " WHERE MM_BK.年 = ?" +
                    "   AND MM_BK.顧客番号 = ?", arg_t_Value, arg_t_Value);
            sqlca.sql = wk_sql;

            /* ＨＯＳＴ変数にセット */
            strcpy(str_sql, wk_sql );

            /* 動的ＳＱＬ文を解析する */
            // EXEC SQL PREPARE sql_stat2 from :str_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != R_const_Ora_OK) {
                sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理UPDATE 動的SQL解析エラー sqlca.sqlcode=[%d],テーブル名=[%s]\n", sqlca.sqlcode,arg_t_Value);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSPT01;
                MRUP_TSPT01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }

            /* 動的ＳＱＬ文を実行する */
            /*
            EXEC SQL  EXECUTE sql_stat2
            USING   :h_rowid,
                          :h_year,
                          :h_uid;
            */
            sqlca.restAndExecute(h_rowid, h_year, h_uid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],テーブル名=[%s],顧客番号-[%s]\n", sqlca.sqlcode,arg_t_Value,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSPT01;
                MRUP_TSPT01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                memset(wk_sql, 0x00, sizeof(wk_sql));
                memset(str_sql, 0x00, sizeof(str_sql));
                /* ＳＱＬ文をセットする */
                sprintf(wk_sql,
                        "INSERT INTO %s_BK SELECT * FROM %s WHERE ROW (年,顧客番号) = (SELECT ? FROM DUAL)", arg_t_Value, arg_t_Value);

                /* ＨＯＳＴ変数にセット */
                strcpy(str_sql, wk_sql );

                /* 動的ＳＱＬ文を解析する */
                // EXEC SQL PREPARE sql_stat3 from :str_sql;
                sqlca.sql = wk_sql;
                sqlca.prepare();
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理INSERT 動的SQL解析エラー sqlca.sqlcode=[%d],テーブル名=[%s]\n", sqlca.sqlcode,arg_t_Value);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    //EXEC SQL CLOSE MRUP_TSPT01;
                    MRUP_TSPT01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
                /* 動的ＳＱＬ文を実行する */
                /*
                EXEC SQL  EXECUTE sql_stat3
                USING   :h_rowid;
                */
                sqlca.restAndExecute(h_rowid);
                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "TSポイント年別情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],テーブル名=[%s],顧客番号-[%s]\n", sqlca.sqlcode,arg_t_Value,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_TSPT01;
                    MRUP_TSPT01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* TSポイント年別情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        //EXEC SQL CLOSE MRUP_TSPT01;
        MRUP_TSPT01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        /* クリアする */
        upd_ins_data_cnt = 0;
        sprintf(out_rec_buff, "TSポイント年別情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "TSポイント年別情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKazokusedo_bk                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKazokusedo_bk()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS家族制度情報_BK更新処理                                    */
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
    public int UpdateKazokusedo_bk() {
        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MS家族制度情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MS家族制度情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MSKS01 CURSOR FOR
        SELECT  ROWID,家族ＩＤ
        FROM  MS家族制度情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MSKS01 = sqlcaManager.get("MRUP_MSKS01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(家族ＩＤ) AS TEXT)),'') AS ROWID,家族ＩＤ\n" +
                "        FROM  MS家族制度情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MSKS01.sql = workSql;

        //EXEC SQL OPEN MRUP_MSKS01;
        MRUP_MSKS01.declare();
        MRUP_MSKS01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MSKS01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MS家族制度情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MSKS01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_kazokuid, 0x00, sizeof(h_kazokuid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_MSKS01
            INTO  :h_rowid, :h_kazokuid;
            */
            MRUP_MSKS01.fetchInto(h_rowid, h_kazokuid);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MSKS01.sqlcode != R_const_Ora_OK
                    && MRUP_MSKS01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS家族制度情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MSKS01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSKS01;
                MRUP_MSKS01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MSKS01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateKazokusedo_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateKazokusedo_bk *** 家族ＩＤ=[%s]\n", h_kazokuid );
                /*-------------------------------------------------------------*/
            }

            /* MS家族制度情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MS家族制度情報_BK B
            SET (B.家族親顧客番号,
                    B.家族１顧客番号,
                    B.家族２顧客番号,
                    B.家族３顧客番号,
                    B.家族４顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    B.家族５顧客番号,
                    B.家族６顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    B.家族親登録日,
                    B.家族１登録日,
                    B.家族２登録日,
                    B.家族３登録日,
                    B.家族４登録日,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    B.家族５登録日,
                    B.家族６登録日,
                    B.家族１登録時刻,
                    B.家族２登録時刻,
                    B.家族３登録時刻,
                    B.家族４登録時刻,
                    B.家族５登録時刻,
                    B.家族６登録時刻,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    B.家族作成日,
                    B.家族ランクＵＰ金額最終更新日,
                    B.年間家族ランクＵＰ対象金額０,
                    B.年間家族ランクＵＰ対象金額１,
                    B.年間家族ランクＵＰ対象金額２,
                    B.年間家族ランクＵＰ対象金額３,
                    B.年間家族ランクＵＰ対象金額４,
                    B.年間家族ランクＵＰ対象金額５,
                    B.年間家族ランクＵＰ対象金額６,
                    B.年間家族ランクＵＰ対象金額７,
                    B.年間家族ランクＵＰ対象金額８,
                    B.年間家族ランクＵＰ対象金額９,
                    B.月間家族ランクＵＰ金額００１,
                    B.月間家族ランクＵＰ金額００２,
                    B.月間家族ランクＵＰ金額００３,
                    B.月間家族ランクＵＰ金額００４,
                    B.月間家族ランクＵＰ金額００５,
                    B.月間家族ランクＵＰ金額００６,
                    B.月間家族ランクＵＰ金額００７,
                    B.月間家族ランクＵＰ金額００８,
                    B.月間家族ランクＵＰ金額００９,
                    B.月間家族ランクＵＰ金額０１０,
                    B.月間家族ランクＵＰ金額０１１,
                    B.月間家族ランクＵＰ金額０１２,
                    B.月間家族ランクＵＰ金額１０１,
                    B.月間家族ランクＵＰ金額１０２,
                    B.月間家族ランクＵＰ金額１０３,
                    B.月間家族ランクＵＰ金額１０４,
                    B.月間家族ランクＵＰ金額１０５,
                    B.月間家族ランクＵＰ金額１０６,
                    B.月間家族ランクＵＰ金額１０７,
                    B.月間家族ランクＵＰ金額１０８,
                    B.月間家族ランクＵＰ金額１０９,
                    B.月間家族ランクＵＰ金額１１０,
                    B.月間家族ランクＵＰ金額１１１,
                    B.月間家族ランクＵＰ金額１１２,
                    B.年次ランクコード０,
                    B.年次ランクコード１,
                    B.年次ランクコード２,
                    B.年次ランクコード３,
                    B.年次ランクコード４,
                    B.年次ランクコード５,
                    B.年次ランクコード６,
                    B.年次ランクコード７,
                    B.年次ランクコード８,
                    B.年次ランクコード９,
                    B.月次ランクコード００１,
                    B.月次ランクコード００２,
                    B.月次ランクコード００３,
                    B.月次ランクコード００４,
                    B.月次ランクコード００５,
                    B.月次ランクコード００６,
                    B.月次ランクコード００７,
                    B.月次ランクコード００８,
                    B.月次ランクコード００９,
                    B.月次ランクコード０１０,
                    B.月次ランクコード０１１,
                    B.月次ランクコード０１２,
                    B.月次ランクコード１０１,
                    B.月次ランクコード１０２,
                    B.月次ランクコード１０３,
                    B.月次ランクコード１０４,
                    B.月次ランクコード１０５,
                    B.月次ランクコード１０６,
                    B.月次ランクコード１０７,
                    B.月次ランクコード１０８,
                    B.月次ランクコード１０９,
                    B.月次ランクコード１１０,
                    B.月次ランクコード１１１,
                    B.月次ランクコード１１２,
                    B.家族削除日,
                    B.作業企業コード,
                    B.作業者ＩＤ,
                    B.作業年月日,
                    B.作業時刻,
                    B.バッチ更新日,
                    B.最終更新日,
                    B.最終更新日時,
                    B.最終更新プログラムＩＤ
            ) = (
                    SELECT I.家族親顧客番号,
                    I.家族１顧客番号,
                    I.家族２顧客番号,
                    I.家族３顧客番号,
                    I.家族４顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    I.家族５顧客番号,
                    I.家族６顧客番号,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    I.家族親登録日,
                    I.家族１登録日,
                    I.家族２登録日,
                    I.家族３登録日,
                    I.家族４登録日,
                    *//* 2022/11/25 MCCM初版 ADD START *//*
                    I.家族５登録日,
                    I.家族６登録日,
                    I.家族１登録時刻,
                    I.家族２登録時刻,
                    I.家族３登録時刻,
                    I.家族４登録時刻,
                    I.家族５登録時刻,
                    I.家族６登録時刻,
                    *//* 2022/11/25 MCCM初版 ADD END *//*
                    I.家族作成日,
                    I.家族ランクＵＰ金額最終更新日,
                    I.年間家族ランクＵＰ対象金額０,
                    I.年間家族ランクＵＰ対象金額１,
                    I.年間家族ランクＵＰ対象金額２,
                    I.年間家族ランクＵＰ対象金額３,
                    I.年間家族ランクＵＰ対象金額４,
                    I.年間家族ランクＵＰ対象金額５,
                    I.年間家族ランクＵＰ対象金額６,
                    I.年間家族ランクＵＰ対象金額７,
                    I.年間家族ランクＵＰ対象金額８,
                    I.年間家族ランクＵＰ対象金額９,
                    I.月間家族ランクＵＰ金額００１,
                    I.月間家族ランクＵＰ金額００２,
                    I.月間家族ランクＵＰ金額００３,
                    I.月間家族ランクＵＰ金額００４,
                    I.月間家族ランクＵＰ金額００５,
                    I.月間家族ランクＵＰ金額００６,
                    I.月間家族ランクＵＰ金額００７,
                    I.月間家族ランクＵＰ金額００８,
                    I.月間家族ランクＵＰ金額００９,
                    I.月間家族ランクＵＰ金額０１０,
                    I.月間家族ランクＵＰ金額０１１,
                    I.月間家族ランクＵＰ金額０１２,
                    I.月間家族ランクＵＰ金額１０１,
                    I.月間家族ランクＵＰ金額１０２,
                    I.月間家族ランクＵＰ金額１０３,
                    I.月間家族ランクＵＰ金額１０４,
                    I.月間家族ランクＵＰ金額１０５,
                    I.月間家族ランクＵＰ金額１０６,
                    I.月間家族ランクＵＰ金額１０７,
                    I.月間家族ランクＵＰ金額１０８,
                    I.月間家族ランクＵＰ金額１０９,
                    I.月間家族ランクＵＰ金額１１０,
                    I.月間家族ランクＵＰ金額１１１,
                    I.月間家族ランクＵＰ金額１１２,
                    I.年次ランクコード０,
                    I.年次ランクコード１,
                    I.年次ランクコード２,
                    I.年次ランクコード３,
                    I.年次ランクコード４,
                    I.年次ランクコード５,
                    I.年次ランクコード６,
                    I.年次ランクコード７,
                    I.年次ランクコード８,
                    I.年次ランクコード９,
                    I.月次ランクコード００１,
                    I.月次ランクコード００２,
                    I.月次ランクコード００３,
                    I.月次ランクコード００４,
                    I.月次ランクコード００５,
                    I.月次ランクコード００６,
                    I.月次ランクコード００７,
                    I.月次ランクコード００８,
                    I.月次ランクコード００９,
                    I.月次ランクコード０１０,
                    I.月次ランクコード０１１,
                    I.月次ランクコード０１２,
                    I.月次ランクコード１０１,
                    I.月次ランクコード１０２,
                    I.月次ランクコード１０３,
                    I.月次ランクコード１０４,
                    I.月次ランクコード１０５,
                    I.月次ランクコード１０６,
                    I.月次ランクコード１０７,
                    I.月次ランクコード１０８,
                    I.月次ランクコード１０９,
                    I.月次ランクコード１１０,
                    I.月次ランクコード１１１,
                    I.月次ランクコード１１２,
                    I.家族削除日,
                    I.作業企業コード,
                    I.作業者ＩＤ,
                    I.作業年月日,
                    I.作業時刻,
                    I.バッチ更新日,
                    I.最終更新日,
                    I.最終更新日時,
                    I.最終更新プログラムＩＤ
            FROM MS家族制度情報 I
            WHERE I.ROWID = :h_rowid
            AND I.家族ＩＤ=B.家族ＩＤ)
            WHERE B.家族ＩＤ = :h_kazokuid;*/
            sqlca.sql = new StringDto("UPDATE MS家族制度情報_BK B\n" +
                    "SET (家族親顧客番号,\n" +
                    "        家族１顧客番号,\n" +
                    "        家族２顧客番号,\n" +
                    "        家族３顧客番号,\n" +
                    "        家族４顧客番号,\n" +
                    "        家族５顧客番号,\n" +
                    "        家族６顧客番号,\n" +
                    "        家族親登録日,\n" +
                    "        家族１登録日,\n" +
                    "        家族２登録日,\n" +
                    "        家族３登録日,\n" +
                    "        家族４登録日,\n" +
                    "        家族５登録日,\n" +
                    "        家族６登録日,\n" +
                    "        家族１登録時刻,\n" +
                    "        家族２登録時刻,\n" +
                    "        家族３登録時刻,\n" +
                    "        家族４登録時刻,\n" +
                    "        家族５登録時刻,\n" +
                    "        家族６登録時刻,\n" +
                    "        家族作成日,\n" +
                    "        家族ランクＵＰ金額最終更新日,\n" +
                    "        年間家族ランクＵＰ対象金額０,\n" +
                    "        年間家族ランクＵＰ対象金額１,\n" +
                    "        年間家族ランクＵＰ対象金額２,\n" +
                    "        年間家族ランクＵＰ対象金額３,\n" +
                    "        年間家族ランクＵＰ対象金額４,\n" +
                    "        年間家族ランクＵＰ対象金額５,\n" +
                    "        年間家族ランクＵＰ対象金額６,\n" +
                    "        年間家族ランクＵＰ対象金額７,\n" +
                    "        年間家族ランクＵＰ対象金額８,\n" +
                    "        年間家族ランクＵＰ対象金額９,\n" +
                    "        月間家族ランクＵＰ金額００１,\n" +
                    "        月間家族ランクＵＰ金額００２,\n" +
                    "        月間家族ランクＵＰ金額００３,\n" +
                    "        月間家族ランクＵＰ金額００４,\n" +
                    "        月間家族ランクＵＰ金額００５,\n" +
                    "        月間家族ランクＵＰ金額００６,\n" +
                    "        月間家族ランクＵＰ金額００７,\n" +
                    "        月間家族ランクＵＰ金額００８,\n" +
                    "        月間家族ランクＵＰ金額００９,\n" +
                    "        月間家族ランクＵＰ金額０１０,\n" +
                    "        月間家族ランクＵＰ金額０１１,\n" +
                    "        月間家族ランクＵＰ金額０１２,\n" +
                    "        月間家族ランクＵＰ金額１０１,\n" +
                    "        月間家族ランクＵＰ金額１０２,\n" +
                    "        月間家族ランクＵＰ金額１０３,\n" +
                    "        月間家族ランクＵＰ金額１０４,\n" +
                    "        月間家族ランクＵＰ金額１０５,\n" +
                    "        月間家族ランクＵＰ金額１０６,\n" +
                    "        月間家族ランクＵＰ金額１０７,\n" +
                    "        月間家族ランクＵＰ金額１０８,\n" +
                    "        月間家族ランクＵＰ金額１０９,\n" +
                    "        月間家族ランクＵＰ金額１１０,\n" +
                    "        月間家族ランクＵＰ金額１１１,\n" +
                    "        月間家族ランクＵＰ金額１１２,\n" +
                    "        年次ランクコード０,\n" +
                    "        年次ランクコード１,\n" +
                    "        年次ランクコード２,\n" +
                    "        年次ランクコード３,\n" +
                    "        年次ランクコード４,\n" +
                    "        年次ランクコード５,\n" +
                    "        年次ランクコード６,\n" +
                    "        年次ランクコード７,\n" +
                    "        年次ランクコード８,\n" +
                    "        年次ランクコード９,\n" +
                    "        月次ランクコード００１,\n" +
                    "        月次ランクコード００２,\n" +
                    "        月次ランクコード００３,\n" +
                    "        月次ランクコード００４,\n" +
                    "        月次ランクコード００５,\n" +
                    "        月次ランクコード００６,\n" +
                    "        月次ランクコード００７,\n" +
                    "        月次ランクコード００８,\n" +
                    "        月次ランクコード００９,\n" +
                    "        月次ランクコード０１０,\n" +
                    "        月次ランクコード０１１,\n" +
                    "        月次ランクコード０１２,\n" +
                    "        月次ランクコード１０１,\n" +
                    "        月次ランクコード１０２,\n" +
                    "        月次ランクコード１０３,\n" +
                    "        月次ランクコード１０４,\n" +
                    "        月次ランクコード１０５,\n" +
                    "        月次ランクコード１０６,\n" +
                    "        月次ランクコード１０７,\n" +
                    "        月次ランクコード１０８,\n" +
                    "        月次ランクコード１０９,\n" +
                    "        月次ランクコード１１０,\n" +
                    "        月次ランクコード１１１,\n" +
                    "        月次ランクコード１１２,\n" +
                    "        家族削除日,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ\n" +
                    ") = (\n" +
                    "        SELECT I.家族親顧客番号,\n" +
                    "        I.家族１顧客番号,\n" +
                    "        I.家族２顧客番号,\n" +
                    "        I.家族３顧客番号,\n" +
                    "        I.家族４顧客番号,\n" +
                    "        I.家族５顧客番号,\n" +
                    "        I.家族６顧客番号,\n" +
                    "        I.家族親登録日,\n" +
                    "        I.家族１登録日,\n" +
                    "        I.家族２登録日,\n" +
                    "        I.家族３登録日,\n" +
                    "        I.家族４登録日,\n" +
                    "        I.家族５登録日,\n" +
                    "        I.家族６登録日,\n" +
                    "        I.家族１登録時刻,\n" +
                    "        I.家族２登録時刻,\n" +
                    "        I.家族３登録時刻,\n" +
                    "        I.家族４登録時刻,\n" +
                    "        I.家族５登録時刻,\n" +
                    "        I.家族６登録時刻,\n" +
                    "        I.家族作成日,\n" +
                    "        I.家族ランクＵＰ金額最終更新日,\n" +
                    "        I.年間家族ランクＵＰ対象金額０,\n" +
                    "        I.年間家族ランクＵＰ対象金額１,\n" +
                    "        I.年間家族ランクＵＰ対象金額２,\n" +
                    "        I.年間家族ランクＵＰ対象金額３,\n" +
                    "        I.年間家族ランクＵＰ対象金額４,\n" +
                    "        I.年間家族ランクＵＰ対象金額５,\n" +
                    "        I.年間家族ランクＵＰ対象金額６,\n" +
                    "        I.年間家族ランクＵＰ対象金額７,\n" +
                    "        I.年間家族ランクＵＰ対象金額８,\n" +
                    "        I.年間家族ランクＵＰ対象金額９,\n" +
                    "        I.月間家族ランクＵＰ金額００１,\n" +
                    "        I.月間家族ランクＵＰ金額００２,\n" +
                    "        I.月間家族ランクＵＰ金額００３,\n" +
                    "        I.月間家族ランクＵＰ金額００４,\n" +
                    "        I.月間家族ランクＵＰ金額００５,\n" +
                    "        I.月間家族ランクＵＰ金額００６,\n" +
                    "        I.月間家族ランクＵＰ金額００７,\n" +
                    "        I.月間家族ランクＵＰ金額００８,\n" +
                    "        I.月間家族ランクＵＰ金額００９,\n" +
                    "        I.月間家族ランクＵＰ金額０１０,\n" +
                    "        I.月間家族ランクＵＰ金額０１１,\n" +
                    "        I.月間家族ランクＵＰ金額０１２,\n" +
                    "        I.月間家族ランクＵＰ金額１０１,\n" +
                    "        I.月間家族ランクＵＰ金額１０２,\n" +
                    "        I.月間家族ランクＵＰ金額１０３,\n" +
                    "        I.月間家族ランクＵＰ金額１０４,\n" +
                    "        I.月間家族ランクＵＰ金額１０５,\n" +
                    "        I.月間家族ランクＵＰ金額１０６,\n" +
                    "        I.月間家族ランクＵＰ金額１０７,\n" +
                    "        I.月間家族ランクＵＰ金額１０８,\n" +
                    "        I.月間家族ランクＵＰ金額１０９,\n" +
                    "        I.月間家族ランクＵＰ金額１１０,\n" +
                    "        I.月間家族ランクＵＰ金額１１１,\n" +
                    "        I.月間家族ランクＵＰ金額１１２,\n" +
                    "        I.年次ランクコード０,\n" +
                    "        I.年次ランクコード１,\n" +
                    "        I.年次ランクコード２,\n" +
                    "        I.年次ランクコード３,\n" +
                    "        I.年次ランクコード４,\n" +
                    "        I.年次ランクコード５,\n" +
                    "        I.年次ランクコード６,\n" +
                    "        I.年次ランクコード７,\n" +
                    "        I.年次ランクコード８,\n" +
                    "        I.年次ランクコード９,\n" +
                    "        I.月次ランクコード００１,\n" +
                    "        I.月次ランクコード００２,\n" +
                    "        I.月次ランクコード００３,\n" +
                    "        I.月次ランクコード００４,\n" +
                    "        I.月次ランクコード００５,\n" +
                    "        I.月次ランクコード００６,\n" +
                    "        I.月次ランクコード００７,\n" +
                    "        I.月次ランクコード００８,\n" +
                    "        I.月次ランクコード００９,\n" +
                    "        I.月次ランクコード０１０,\n" +
                    "        I.月次ランクコード０１１,\n" +
                    "        I.月次ランクコード０１２,\n" +
                    "        I.月次ランクコード１０１,\n" +
                    "        I.月次ランクコード１０２,\n" +
                    "        I.月次ランクコード１０３,\n" +
                    "        I.月次ランクコード１０４,\n" +
                    "        I.月次ランクコード１０５,\n" +
                    "        I.月次ランクコード１０６,\n" +
                    "        I.月次ランクコード１０７,\n" +
                    "        I.月次ランクコード１０８,\n" +
                    "        I.月次ランクコード１０９,\n" +
                    "        I.月次ランクコード１１０,\n" +
                    "        I.月次ランクコード１１１,\n" +
                    "        I.月次ランクコード１１２,\n" +
                    "        I.家族削除日,\n" +
                    "        I.作業企業コード,\n" +
                    "        I.作業者ＩＤ,\n" +
                    "        I.作業年月日,\n" +
                    "        I.作業時刻,\n" +
                    "        I.バッチ更新日,\n" +
                    "        I.最終更新日,\n" +
                    "        I.最終更新日時,\n" +
                    "        I.最終更新プログラムＩＤ\n" +
                    "FROM MS家族制度情報 I\n" +
                    "WHERE ROW (I.家族ＩＤ) = (SELECT ? FROM DUAL)\n" +
                    "AND I.家族ＩＤ=B.家族ＩＤ)\n" +
                    "WHERE B.家族ＩＤ = ?");
            sqlca.restAndExecute(h_rowid, h_kazokuid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS家族制度情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],家族ＩＤ=[%s]\n", sqlca.sqlcode,h_kazokuid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                //EXEC SQL CLOSE MRUP_MSKS01;
                MRUP_MSKS01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO MS家族制度情報_BK
                SELECT * FROM MS家族制度情報
                WHERE ROWID = :h_rowid;
                */
                sqlca.sql = new StringDto("INSERT   INTO MS家族制度情報_BK\n" +
                        "                SELECT * FROM MS家族制度情報\n" +
                        "                WHERE ROW (家族ＩＤ) = (SELECT ? FROM DUAL) ");
                sqlca.restAndExecute(h_rowid);
                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MS家族制度情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],家族ＩＤ=[%s]\n", sqlca.sqlcode,h_kazokuid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MSKS01;
                    MRUP_MSKS01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MS家族制度情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        //EXEC SQL CLOSE MRUP_MSKS01;
        MRUP_MSKS01.curse_close();

        /* コミットする */
        //EXEC SQL COMMIT WORK;
        sqlca.commit();
        /* クリアする */
        upd_ins_data_cnt = 0;
        sprintf(out_rec_buff, "MS家族制度情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MS家族制度情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： DeleteKazokusedo_bk                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  DeleteKazokusedo_bk()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS家族制度情報_BK削除処理                                    */
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
    public int DeleteKazokusedo_bk() {
        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MS家族制度情報_BK削除処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }

        /* MS家族制度情報_BK削除処理 */
        /*
        EXEC SQL DELETE FROM MS家族制度情報_BK MM_BK
        WHERE NOT EXISTS(
                SELECT MM_I.家族ＩＤ
                FROM MS家族制度情報 MM_I
                WHERE MM_BK.家族ＩＤ = MM_I.家族ＩＤ);
        */
        sqlca.sql = new StringDto("DELETE FROM MS家族制度情報_BK MM_BK\n" +
                "        WHERE NOT EXISTS(\n" +
                "                SELECT MM_I.家族ＩＤ\n" +
                "                FROM MS家族制度情報 MM_I\n" +
                "                WHERE MM_BK.家族ＩＤ = MM_I.家族ＩＤ)");
        sqlca.restAndExecute();

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (   sqlca.sqlcode != R_const_Ora_OK
                && sqlca.sqlcode != R_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_rec_buff, "MS家族制度情報_BK削除処理DELETEエラー sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }
        out_del_cnt = sqlca.updateCount;
        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MS家族制度情報_BK更新正常\n　DELETE件数=[%d]\n", out_del_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MS家族制度情報_BK削除処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateGaibuninsho_bk                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateGaibuninsho_bk()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS外部認証情報_BK更新処理                                    */
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
    public int UpdateGaibuninsho_bk() {
        StringDto    out_rec_buff = new StringDto(1024);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MS外部認証情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* MS外部認証情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_MSGA01 CURSOR FOR
        SELECT  ROWID,外部認証種別,外部認証ＩＤ
        FROM  MS外部認証情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_MSGA01 = sqlcaManager.get("MRUP_MSGA01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(外部認証種別,外部認証ＩＤ) AS TEXT)),'') AS ROWID,外部認証種別,外部認証ＩＤ\n" +
                "        FROM  MS外部認証情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_MSGA01.sql = workSql;

        //EXEC SQL OPEN MRUP_MSGA01;
        MRUP_MSGA01.declare();
        MRUP_MSGA01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_MSGA01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "MS外部認証情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_MSGA01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_gaibuninsho_status, 0x00, sizeof(h_gaibuninsho_status));
            memset(h_gaibuninsho_id.arr, 0x00, sizeof(h_gaibuninsho_id.arr));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*EXEC SQL FETCH MRUP_MSGA01
            INTO :h_rowid,
                      :h_gaibuninsho_status,
                      :h_gaibuninsho_id;
            */
            MRUP_MSGA01.fetchInto(h_rowid, h_gaibuninsho_status, h_gaibuninsho_id);
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_MSGA01.sqlcode != R_const_Ora_OK
                    && MRUP_MSGA01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS外部認証情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_MSGA01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSGA01;
                MRUP_MSGA01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_MSGA01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateGaibuninsho_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateGaibuninsho_bk *** 外部認証種別=[%s]\n", h_gaibuninsho_status );
                printf( "*** UpdateGaibuninsho_bk *** 外部認証ＩＤ=[%s]\n", h_gaibuninsho_id.arr );
                /*-------------------------------------------------------------*/
            }

            /* MS外部認証情報_BK更新処理 */
            /*
            EXEC SQL UPDATE MS外部認証情報_BK MM_BK
            SET (MM_BK.サービス種別,
                    MM_BK.会員番号,
                    MM_BK.削除フラグ,
                    MM_BK.登録日,
                    MM_BK.作業企業コード,
                    MM_BK.作業者ＩＤ,
                    MM_BK.作業年月日,
                    MM_BK.作業時刻,
                    MM_BK.バッチ更新日,
                    MM_BK.最終更新日,
                    MM_BK.最終更新日時,
                    MM_BK.最終更新プログラムＩＤ
            ) = (
                    SELECT MM_I.サービス種別,
                    MM_I.会員番号,
                    MM_I.削除フラグ,
                    MM_I.登録日,
                    MM_I.作業企業コード,
                    MM_I.作業者ＩＤ,
                    MM_I.作業年月日,
                    MM_I.作業時刻,
                    MM_I.バッチ更新日,
                    MM_I.最終更新日,
                    MM_I.最終更新日時,
                    MM_I.最終更新プログラムＩＤ
            FROM MS外部認証情報 MM_I
            WHERE MM_I.ROWID = :h_rowid
            AND MM_I.外部認証種別 = MM_BK.外部認証種別
            AND MM_I.外部認証ＩＤ = MM_BK.外部認証ＩＤ)
            WHERE MM_BK.外部認証種別 = :h_gaibuninsho_status
            AND   MM_BK.外部認証ＩＤ = :h_gaibuninsho_id;*/
            sqlca.sql = new StringDto("UPDATE MS外部認証情報_BK MM_BK\n" +
                    "SET (サービス種別,\n" +
                    "        会員番号,\n" +
                    "        削除フラグ,\n" +
                    "        登録日,\n" +
                    "        作業企業コード,\n" +
                    "        作業者ＩＤ,\n" +
                    "        作業年月日,\n" +
                    "        作業時刻,\n" +
                    "        バッチ更新日,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ\n" +
                    ") = (\n" +
                    "        SELECT MM_I.サービス種別,\n" +
                    "        MM_I.会員番号,\n" +
                    "        MM_I.削除フラグ,\n" +
                    "        MM_I.登録日,\n" +
                    "        MM_I.作業企業コード,\n" +
                    "        MM_I.作業者ＩＤ,\n" +
                    "        MM_I.作業年月日,\n" +
                    "        MM_I.作業時刻,\n" +
                    "        MM_I.バッチ更新日,\n" +
                    "        MM_I.最終更新日,\n" +
                    "        MM_I.最終更新日時,\n" +
                    "        MM_I.最終更新プログラムＩＤ\n" +
                    "FROM MS外部認証情報 MM_I\n" +
                    "WHERE ROW (MM_I.外部認証種別,外部認証ＩＤ) = (SELECT ? FROM DUAL)\n" +
                    "AND MM_I.外部認証種別 = MM_BK.外部認証種別\n" +
                    "AND MM_I.外部認証ＩＤ = MM_BK.外部認証ＩＤ)\n" +
                    "WHERE MM_BK.外部認証種別 = ?\n" +
                    "AND   MM_BK.外部認証ＩＤ = ?");
            sqlca.restAndExecute(h_rowid, h_gaibuninsho_status, h_gaibuninsho_id);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "MS外部認証情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],外部認証種別=[%s],外部認証ＩＤ=[%s]\n", sqlca.sqlcode,h_gaibuninsho_status,h_gaibuninsho_id.arr);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_MSGA01;
                MRUP_MSGA01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT INTO MS外部認証情報_BK
                SELECT * FROM MS外部認証情報
                WHERE ROWID     = :h_rowid;
                */
                sqlca.sql = new StringDto("INSERT INTO MS外部認証情報_BK\n" +
                        "                SELECT * FROM MS外部認証情報\n" +
                        "                WHERE ROW (外部認証種別,外部認証ＩＤ) = (SELECT ? FROM DUAL)");
                sqlca.restAndExecute(h_rowid);
                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "MS外部認証情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],外部認証種別=[%s],外部認証ＩＤ=[%s]\n", sqlca.sqlcode,h_gaibuninsho_status,h_gaibuninsho_id.arr);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_MSGA01;
                    MRUP_MSGA01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* MS外部認証情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_MSGA01;
        MRUP_MSGA01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MS外部認証情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
        /* クリアする */
        upd_ins_data_cnt = 0;

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MS外部認証情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： DeleteGaibuninsho_bk                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  DeleteGaibuninsho_bk()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS外部認証情報_BK削除処理                                    */
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
    public int DeleteGaibuninsho_bk() {

        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "MS外部認証情報_BK削除処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }

        /* MS外部認証情報_BK削除処理 */
        /*
        EXEC SQL DELETE FROM MS外部認証情報_BK MM_BK
        WHERE NOT EXISTS(
                SELECT MM_I.外部認証ＩＤ
                FROM MS外部認証情報 MM_I
                WHERE MM_BK.外部認証種別     = MM_I.外部認証種別
                AND   MM_BK.外部認証ＩＤ = MM_I.外部認証ＩＤ);
        */
        sqlca.sql = new StringDto("DELETE FROM MS外部認証情報_BK MM_BK\n" +
                "        WHERE NOT EXISTS(\n" +
                "                SELECT MM_I.外部認証ＩＤ\n" +
                "                FROM MS外部認証情報 MM_I\n" +
                "                WHERE MM_BK.外部認証種別     = MM_I.外部認証種別\n" +
                "                AND   MM_BK.外部認証ＩＤ = MM_I.外部認証ＩＤ)");
        sqlca.restAndExecute();

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (   sqlca.sqlcode != R_const_Ora_OK
                && sqlca.sqlcode != R_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_rec_buff, "MS外部認証情報_BK削除処理DELETEエラー sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        out_del_cnt = sqlca.updateCount;
        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "MS外部認証情報_BK更新正常\n　DELETE件数=[%d]\n", out_del_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "MS外部認証情報_BK削除処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateTsrank_bk                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateTsrank_bk()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TSランク情報_BK更新処理                                      */
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
    public int UpdateTsrank_bk() {
        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "TSランク情報_BK更新処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }
        /* TSランク情報取得処理 */
        /*
        EXEC SQL DECLARE MRUP_TSRA01 CURSOR FOR
        SELECT  ROWID,顧客番号
        FROM  TSランク情報
        WHERE 最終更新日   >= :bt_date;
        */
        SqlstmDto MRUP_TSRA01 = sqlcaManager.get("MRUP_TSRA01");
        StringDto workSql = new StringDto("SELECT  NULLIF(TRIM(BOTH '()' FROM CAST(ROW(顧客番号) AS TEXT)),'') AS ROWID,顧客番号\n" +
                "        FROM  TSランク情報\n" +
                "        WHERE 最終更新日   >= ?");
        MRUP_TSRA01.sql = workSql;

        //EXEC SQL OPEN MRUP_TSRA01;
        MRUP_TSRA01.declare();
        MRUP_TSRA01.open(bt_date);
        /* エラーの場合処理を異常終了する */
        if (MRUP_TSRA01.sqlcode != R_const_Ora_OK) {
            /* DBERR */
            sprintf(out_rec_buff, "TSランク情報_BK更新処理CURSOR OPENエラー sqlca.sqlcode=[%d]\n", MRUP_TSRA01.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }

        for ( ; ; ) {
            /* 初期化 */
            memset(h_uid, 0x00, sizeof(h_uid));
            memset(h_rowid.arr, 0x00, sizeof(h_rowid.arr));
            h_rowid.len = 0;

            /*
            EXEC SQL FETCH MRUP_TSRA01
            INTO  :h_rowid, :h_uid;
            */
            MRUP_TSRA01.fetchInto(h_rowid, h_uid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (   MRUP_TSRA01.sqlcode != R_const_Ora_OK
                    && MRUP_TSRA01.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TSランク情報_BK更新処理FETCHエラー sqlca.sqlcode=[%d]\n", MRUP_TSRA01.sqlcode);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSRA01;
                MRUP_TSRA01.curse_close();

                /* 処理を終了する */
                return R_const_NG;
            }
            /* データ無しエラーの場合処理を終了する */
            else if (MRUP_TSRA01.sqlcode == R_const_Ora_NOTFOUND) {
                if(DBG_LOG == 1){
                    /*-------------------------------------------------------------*/
                    printf( "*** UpdateTsrank_bk *** データなし 終了%s\n", "" );
                    /*-------------------------------------------------------------*/
                }
                break;
            }
            if(DBG_LOG == 1){
                /*-------------------------------------------------------------*/
                printf( "*** UpdateTsrank_bk *** 顧客番号=[%s]\n", h_uid );
                /*-------------------------------------------------------------*/
            }

            /* TSランク情報_BK更新処理 */
            /*
            EXEC SQL UPDATE TSランク情報_BK B
            SET (B.年次ランクコード０,
                    B.年次ランクコード１,
                    B.年次ランクコード２,
                    B.年次ランクコード３,
                    B.年次ランクコード４,
                    B.年次ランクコード５,
                    B.年次ランクコード６,
                    B.年次ランクコード７,
                    B.年次ランクコード８,
                    B.年次ランクコード９,
                    B.年間ランクＵＰ対象金額０,
                    B.年間ランクＵＰ対象金額１,
                    B.年間ランクＵＰ対象金額２,
                    B.年間ランクＵＰ対象金額３,
                    B.年間ランクＵＰ対象金額４,
                    B.年間ランクＵＰ対象金額５,
                    B.年間ランクＵＰ対象金額６,
                    B.年間ランクＵＰ対象金額７,
                    B.年間ランクＵＰ対象金額８,
                    B.年間ランクＵＰ対象金額９,
                    B.月次ランクコード００４,
                    B.月次ランクコード００５,
                    B.月次ランクコード００６,
                    B.月次ランクコード００７,
                    B.月次ランクコード００８,
                    B.月次ランクコード００９,
                    B.月次ランクコード０１０,
                    B.月次ランクコード０１１,
                    B.月次ランクコード０１２,
                    B.月次ランクコード００１,
                    B.月次ランクコード００２,
                    B.月次ランクコード００３,
                    B.月次ランクコード１０４,
                    B.月次ランクコード１０５,
                    B.月次ランクコード１０６,
                    B.月次ランクコード１０７,
                    B.月次ランクコード１０８,
                    B.月次ランクコード１０９,
                    B.月次ランクコード１１０,
                    B.月次ランクコード１１１,
                    B.月次ランクコード１１２,
                    B.月次ランクコード１０１,
                    B.月次ランクコード１０２,
                    B.月次ランクコード１０３,
                    B.月間ランクＵＰ対象金額００４,
                    B.月間ランクＵＰ対象金額００５,
                    B.月間ランクＵＰ対象金額００６,
                    B.月間ランクＵＰ対象金額００７,
                    B.月間ランクＵＰ対象金額００８,
                    B.月間ランクＵＰ対象金額００９,
                    B.月間ランクＵＰ対象金額０１０,
                    B.月間ランクＵＰ対象金額０１１,
                    B.月間ランクＵＰ対象金額０１２,
                    B.月間ランクＵＰ対象金額００１,
                    B.月間ランクＵＰ対象金額００２,
                    B.月間ランクＵＰ対象金額００３,
                    B.月間ランクＵＰ対象金額１０４,
                    B.月間ランクＵＰ対象金額１０５,
                    B.月間ランクＵＰ対象金額１０６,
                    B.月間ランクＵＰ対象金額１０７,
                    B.月間ランクＵＰ対象金額１０８,
                    B.月間ランクＵＰ対象金額１０９,
                    B.月間ランクＵＰ対象金額１１０,
                    B.月間ランクＵＰ対象金額１１１,
                    B.月間ランクＵＰ対象金額１１２,
                    B.月間ランクＵＰ対象金額１０１,
                    B.月間ランクＵＰ対象金額１０２,
                    B.月間ランクＵＰ対象金額１０３,
                    B.月間プレミアムポイント数００４,
                    B.月間プレミアムポイント数００５,
                    B.月間プレミアムポイント数００６,
                    B.月間プレミアムポイント数００７,
                    B.月間プレミアムポイント数００８,
                    B.月間プレミアムポイント数００９,
                    B.月間プレミアムポイント数０１０,
                    B.月間プレミアムポイント数０１１,
                    B.月間プレミアムポイント数０１２,
                    B.月間プレミアムポイント数００１,
                    B.月間プレミアムポイント数００２,
                    B.月間プレミアムポイント数００３,
                    B.月間プレミアムポイント数１０４,
                    B.月間プレミアムポイント数１０５,
                    B.月間プレミアムポイント数１０６,
                    B.月間プレミアムポイント数１０７,
                    B.月間プレミアムポイント数１０８,
                    B.月間プレミアムポイント数１０９,
                    B.月間プレミアムポイント数１１０,
                    B.月間プレミアムポイント数１１１,
                    B.月間プレミアムポイント数１１２,
                    B.月間プレミアムポイント数１０１,
                    B.月間プレミアムポイント数１０２,
                    B.月間プレミアムポイント数１０３,
                    B.最終更新日,
                    B.最終更新日時,
                    B.最終更新プログラムＩＤ
            ) = (
                    SELECT I.年次ランクコード０,
                    I.年次ランクコード１,
                    I.年次ランクコード２,
                    I.年次ランクコード３,
                    I.年次ランクコード４,
                    I.年次ランクコード５,
                    I.年次ランクコード６,
                    I.年次ランクコード７,
                    I.年次ランクコード８,
                    I.年次ランクコード９,
                    I.年間ランクＵＰ対象金額０,
                    I.年間ランクＵＰ対象金額１,
                    I.年間ランクＵＰ対象金額２,
                    I.年間ランクＵＰ対象金額３,
                    I.年間ランクＵＰ対象金額４,
                    I.年間ランクＵＰ対象金額５,
                    I.年間ランクＵＰ対象金額６,
                    I.年間ランクＵＰ対象金額７,
                    I.年間ランクＵＰ対象金額８,
                    I.年間ランクＵＰ対象金額９,
                    I.月次ランクコード００４,
                    I.月次ランクコード００５,
                    I.月次ランクコード００６,
                    I.月次ランクコード００７,
                    I.月次ランクコード００８,
                    I.月次ランクコード００９,
                    I.月次ランクコード０１０,
                    I.月次ランクコード０１１,
                    I.月次ランクコード０１２,
                    I.月次ランクコード００１,
                    I.月次ランクコード００２,
                    I.月次ランクコード００３,
                    I.月次ランクコード１０４,
                    I.月次ランクコード１０５,
                    I.月次ランクコード１０６,
                    I.月次ランクコード１０７,
                    I.月次ランクコード１０８,
                    I.月次ランクコード１０９,
                    I.月次ランクコード１１０,
                    I.月次ランクコード１１１,
                    I.月次ランクコード１１２,
                    I.月次ランクコード１０１,
                    I.月次ランクコード１０２,
                    I.月次ランクコード１０３,
                    I.月間ランクＵＰ対象金額００４,
                    I.月間ランクＵＰ対象金額００５,
                    I.月間ランクＵＰ対象金額００６,
                    I.月間ランクＵＰ対象金額００７,
                    I.月間ランクＵＰ対象金額００８,
                    I.月間ランクＵＰ対象金額００９,
                    I.月間ランクＵＰ対象金額０１０,
                    I.月間ランクＵＰ対象金額０１１,
                    I.月間ランクＵＰ対象金額０１２,
                    I.月間ランクＵＰ対象金額００１,
                    I.月間ランクＵＰ対象金額００２,
                    I.月間ランクＵＰ対象金額００３,
                    I.月間ランクＵＰ対象金額１０４,
                    I.月間ランクＵＰ対象金額１０５,
                    I.月間ランクＵＰ対象金額１０６,
                    I.月間ランクＵＰ対象金額１０７,
                    I.月間ランクＵＰ対象金額１０８,
                    I.月間ランクＵＰ対象金額１０９,
                    I.月間ランクＵＰ対象金額１１０,
                    I.月間ランクＵＰ対象金額１１１,
                    I.月間ランクＵＰ対象金額１１２,
                    I.月間ランクＵＰ対象金額１０１,
                    I.月間ランクＵＰ対象金額１０２,
                    I.月間ランクＵＰ対象金額１０３,
                    I.月間プレミアムポイント数００４,
                    I.月間プレミアムポイント数００５,
                    I.月間プレミアムポイント数００６,
                    I.月間プレミアムポイント数００７,
                    I.月間プレミアムポイント数００８,
                    I.月間プレミアムポイント数００９,
                    I.月間プレミアムポイント数０１０,
                    I.月間プレミアムポイント数０１１,
                    I.月間プレミアムポイント数０１２,
                    I.月間プレミアムポイント数００１,
                    I.月間プレミアムポイント数００２,
                    I.月間プレミアムポイント数００３,
                    I.月間プレミアムポイント数１０４,
                    I.月間プレミアムポイント数１０５,
                    I.月間プレミアムポイント数１０６,
                    I.月間プレミアムポイント数１０７,
                    I.月間プレミアムポイント数１０８,
                    I.月間プレミアムポイント数１０９,
                    I.月間プレミアムポイント数１１０,
                    I.月間プレミアムポイント数１１１,
                    I.月間プレミアムポイント数１１２,
                    I.月間プレミアムポイント数１０１,
                    I.月間プレミアムポイント数１０２,
                    I.月間プレミアムポイント数１０３,
                    I.最終更新日,
                    I.最終更新日時,
                    I.最終更新プログラムＩＤ
            FROM TSランク情報 I
            WHERE I.ROWID = :h_rowid
            AND I.顧客番号=B.顧客番号)
            WHERE B.顧客番号 = :h_uid;*/
            sqlca.sql = new StringDto("UPDATE TSランク情報_BK B\n" +
                    "SET (年次ランクコード０,\n" +
                    "        年次ランクコード１,\n" +
                    "        年次ランクコード２,\n" +
                    "        年次ランクコード３,\n" +
                    "        年次ランクコード４,\n" +
                    "        年次ランクコード５,\n" +
                    "        年次ランクコード６,\n" +
                    "        年次ランクコード７,\n" +
                    "        年次ランクコード８,\n" +
                    "        年次ランクコード９,\n" +
                    "        年間ランクＵＰ対象金額０,\n" +
                    "        年間ランクＵＰ対象金額１,\n" +
                    "        年間ランクＵＰ対象金額２,\n" +
                    "        年間ランクＵＰ対象金額３,\n" +
                    "        年間ランクＵＰ対象金額４,\n" +
                    "        年間ランクＵＰ対象金額５,\n" +
                    "        年間ランクＵＰ対象金額６,\n" +
                    "        年間ランクＵＰ対象金額７,\n" +
                    "        年間ランクＵＰ対象金額８,\n" +
                    "        年間ランクＵＰ対象金額９,\n" +
                    "        月次ランクコード００４,\n" +
                    "        月次ランクコード００５,\n" +
                    "        月次ランクコード００６,\n" +
                    "        月次ランクコード００７,\n" +
                    "        月次ランクコード００８,\n" +
                    "        月次ランクコード００９,\n" +
                    "        月次ランクコード０１０,\n" +
                    "        月次ランクコード０１１,\n" +
                    "        月次ランクコード０１２,\n" +
                    "        月次ランクコード００１,\n" +
                    "        月次ランクコード００２,\n" +
                    "        月次ランクコード００３,\n" +
                    "        月次ランクコード１０４,\n" +
                    "        月次ランクコード１０５,\n" +
                    "        月次ランクコード１０６,\n" +
                    "        月次ランクコード１０７,\n" +
                    "        月次ランクコード１０８,\n" +
                    "        月次ランクコード１０９,\n" +
                    "        月次ランクコード１１０,\n" +
                    "        月次ランクコード１１１,\n" +
                    "        月次ランクコード１１２,\n" +
                    "        月次ランクコード１０１,\n" +
                    "        月次ランクコード１０２,\n" +
                    "        月次ランクコード１０３,\n" +
                    "        月間ランクＵＰ対象金額００４,\n" +
                    "        月間ランクＵＰ対象金額００５,\n" +
                    "        月間ランクＵＰ対象金額００６,\n" +
                    "        月間ランクＵＰ対象金額００７,\n" +
                    "        月間ランクＵＰ対象金額００８,\n" +
                    "        月間ランクＵＰ対象金額００９,\n" +
                    "        月間ランクＵＰ対象金額０１０,\n" +
                    "        月間ランクＵＰ対象金額０１１,\n" +
                    "        月間ランクＵＰ対象金額０１２,\n" +
                    "        月間ランクＵＰ対象金額００１,\n" +
                    "        月間ランクＵＰ対象金額００２,\n" +
                    "        月間ランクＵＰ対象金額００３,\n" +
                    "        月間ランクＵＰ対象金額１０４,\n" +
                    "        月間ランクＵＰ対象金額１０５,\n" +
                    "        月間ランクＵＰ対象金額１０６,\n" +
                    "        月間ランクＵＰ対象金額１０７,\n" +
                    "        月間ランクＵＰ対象金額１０８,\n" +
                    "        月間ランクＵＰ対象金額１０９,\n" +
                    "        月間ランクＵＰ対象金額１１０,\n" +
                    "        月間ランクＵＰ対象金額１１１,\n" +
                    "        月間ランクＵＰ対象金額１１２,\n" +
                    "        月間ランクＵＰ対象金額１０１,\n" +
                    "        月間ランクＵＰ対象金額１０２,\n" +
                    "        月間ランクＵＰ対象金額１０３,\n" +
                    "        月間プレミアムポイント数００４,\n" +
                    "        月間プレミアムポイント数００５,\n" +
                    "        月間プレミアムポイント数００６,\n" +
                    "        月間プレミアムポイント数００７,\n" +
                    "        月間プレミアムポイント数００８,\n" +
                    "        月間プレミアムポイント数００９,\n" +
                    "        月間プレミアムポイント数０１０,\n" +
                    "        月間プレミアムポイント数０１１,\n" +
                    "        月間プレミアムポイント数０１２,\n" +
                    "        月間プレミアムポイント数００１,\n" +
                    "        月間プレミアムポイント数００２,\n" +
                    "        月間プレミアムポイント数００３,\n" +
                    "        月間プレミアムポイント数１０４,\n" +
                    "        月間プレミアムポイント数１０５,\n" +
                    "        月間プレミアムポイント数１０６,\n" +
                    "        月間プレミアムポイント数１０７,\n" +
                    "        月間プレミアムポイント数１０８,\n" +
                    "        月間プレミアムポイント数１０９,\n" +
                    "        月間プレミアムポイント数１１０,\n" +
                    "        月間プレミアムポイント数１１１,\n" +
                    "        月間プレミアムポイント数１１２,\n" +
                    "        月間プレミアムポイント数１０１,\n" +
                    "        月間プレミアムポイント数１０２,\n" +
                    "        月間プレミアムポイント数１０３,\n" +
                    "        最終更新日,\n" +
                    "        最終更新日時,\n" +
                    "        最終更新プログラムＩＤ\n" +
                    ") = (\n" +
                    "        SELECT I.年次ランクコード０,\n" +
                    "        I.年次ランクコード１,\n" +
                    "        I.年次ランクコード２,\n" +
                    "        I.年次ランクコード３,\n" +
                    "        I.年次ランクコード４,\n" +
                    "        I.年次ランクコード５,\n" +
                    "        I.年次ランクコード６,\n" +
                    "        I.年次ランクコード７,\n" +
                    "        I.年次ランクコード８,\n" +
                    "        I.年次ランクコード９,\n" +
                    "        I.年間ランクＵＰ対象金額０,\n" +
                    "        I.年間ランクＵＰ対象金額１,\n" +
                    "        I.年間ランクＵＰ対象金額２,\n" +
                    "        I.年間ランクＵＰ対象金額３,\n" +
                    "        I.年間ランクＵＰ対象金額４,\n" +
                    "        I.年間ランクＵＰ対象金額５,\n" +
                    "        I.年間ランクＵＰ対象金額６,\n" +
                    "        I.年間ランクＵＰ対象金額７,\n" +
                    "        I.年間ランクＵＰ対象金額８,\n" +
                    "        I.年間ランクＵＰ対象金額９,\n" +
                    "        I.月次ランクコード００４,\n" +
                    "        I.月次ランクコード００５,\n" +
                    "        I.月次ランクコード００６,\n" +
                    "        I.月次ランクコード００７,\n" +
                    "        I.月次ランクコード００８,\n" +
                    "        I.月次ランクコード００９,\n" +
                    "        I.月次ランクコード０１０,\n" +
                    "        I.月次ランクコード０１１,\n" +
                    "        I.月次ランクコード０１２,\n" +
                    "        I.月次ランクコード００１,\n" +
                    "        I.月次ランクコード００２,\n" +
                    "        I.月次ランクコード００３,\n" +
                    "        I.月次ランクコード１０４,\n" +
                    "        I.月次ランクコード１０５,\n" +
                    "        I.月次ランクコード１０６,\n" +
                    "        I.月次ランクコード１０７,\n" +
                    "        I.月次ランクコード１０８,\n" +
                    "        I.月次ランクコード１０９,\n" +
                    "        I.月次ランクコード１１０,\n" +
                    "        I.月次ランクコード１１１,\n" +
                    "        I.月次ランクコード１１２,\n" +
                    "        I.月次ランクコード１０１,\n" +
                    "        I.月次ランクコード１０２,\n" +
                    "        I.月次ランクコード１０３,\n" +
                    "        I.月間ランクＵＰ対象金額００４,\n" +
                    "        I.月間ランクＵＰ対象金額００５,\n" +
                    "        I.月間ランクＵＰ対象金額００６,\n" +
                    "        I.月間ランクＵＰ対象金額００７,\n" +
                    "        I.月間ランクＵＰ対象金額００８,\n" +
                    "        I.月間ランクＵＰ対象金額００９,\n" +
                    "        I.月間ランクＵＰ対象金額０１０,\n" +
                    "        I.月間ランクＵＰ対象金額０１１,\n" +
                    "        I.月間ランクＵＰ対象金額０１２,\n" +
                    "        I.月間ランクＵＰ対象金額００１,\n" +
                    "        I.月間ランクＵＰ対象金額００２,\n" +
                    "        I.月間ランクＵＰ対象金額００３,\n" +
                    "        I.月間ランクＵＰ対象金額１０４,\n" +
                    "        I.月間ランクＵＰ対象金額１０５,\n" +
                    "        I.月間ランクＵＰ対象金額１０６,\n" +
                    "        I.月間ランクＵＰ対象金額１０７,\n" +
                    "        I.月間ランクＵＰ対象金額１０８,\n" +
                    "        I.月間ランクＵＰ対象金額１０９,\n" +
                    "        I.月間ランクＵＰ対象金額１１０,\n" +
                    "        I.月間ランクＵＰ対象金額１１１,\n" +
                    "        I.月間ランクＵＰ対象金額１１２,\n" +
                    "        I.月間ランクＵＰ対象金額１０１,\n" +
                    "        I.月間ランクＵＰ対象金額１０２,\n" +
                    "        I.月間ランクＵＰ対象金額１０３,\n" +
                    "        I.月間プレミアムポイント数００４,\n" +
                    "        I.月間プレミアムポイント数００５,\n" +
                    "        I.月間プレミアムポイント数００６,\n" +
                    "        I.月間プレミアムポイント数００７,\n" +
                    "        I.月間プレミアムポイント数００８,\n" +
                    "        I.月間プレミアムポイント数００９,\n" +
                    "        I.月間プレミアムポイント数０１０,\n" +
                    "        I.月間プレミアムポイント数０１１,\n" +
                    "        I.月間プレミアムポイント数０１２,\n" +
                    "        I.月間プレミアムポイント数００１,\n" +
                    "        I.月間プレミアムポイント数００２,\n" +
                    "        I.月間プレミアムポイント数００３,\n" +
                    "        I.月間プレミアムポイント数１０４,\n" +
                    "        I.月間プレミアムポイント数１０５,\n" +
                    "        I.月間プレミアムポイント数１０６,\n" +
                    "        I.月間プレミアムポイント数１０７,\n" +
                    "        I.月間プレミアムポイント数１０８,\n" +
                    "        I.月間プレミアムポイント数１０９,\n" +
                    "        I.月間プレミアムポイント数１１０,\n" +
                    "        I.月間プレミアムポイント数１１１,\n" +
                    "        I.月間プレミアムポイント数１１２,\n" +
                    "        I.月間プレミアムポイント数１０１,\n" +
                    "        I.月間プレミアムポイント数１０２,\n" +
                    "        I.月間プレミアムポイント数１０３,\n" +
                    "        I.最終更新日,\n" +
                    "        I.最終更新日時,\n" +
                    "        I.最終更新プログラムＩＤ\n" +
                    "FROM TSランク情報 I\n" +
                    "WHERE ROW (I.顧客番号) = (SELECT ? FROM DUAL)\n" +
                    "AND I.顧客番号=B.顧客番号)\n" +
                    "WHERE B.顧客番号 = ?");
            sqlca.restAndExecute(h_rowid, h_uid);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != R_const_Ora_OK
                    &&  sqlca.sqlcode != R_const_Ora_NOTFOUND) {
                /* DBERR */
                sprintf(out_rec_buff, "TSランク情報_BK更新処理UPDATEエラー sqlca.sqlcode=[%d],顧客番号=[%s]\n", sqlca.sqlcode,h_uid);
                fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                // EXEC SQL CLOSE MRUP_TSRA01;
                MRUP_TSRA01.curse_close();

                /* 処理を終了する */
                return R_const_NG;

            }else if (sqlca.sqlcode == R_const_Ora_NOTFOUND) {

                /*
                EXEC SQL INSERT   INTO TSランク情報_BK
                SELECT * FROM TSランク情報
                WHERE ROWID = :h_rowid;
                */
                sqlca.sql = new StringDto("INSERT   INTO TSランク情報_BK\n" +
                        "                SELECT * FROM TSランク情報\n" +
                        "                WHERE ROW (顧客番号) = (SELECT ? FROM DUAL)");
                sqlca.restAndExecute(h_rowid);
                /* データ無しエラー以外のエラーの場合処理を異常終了する */
                if (sqlca.sqlcode != R_const_Ora_OK) {
                    /* DBERR */
                    sprintf(out_rec_buff, "TSランク情報_BK更新処理INSERTエラー sqlca.sqlcode=[%d],顧客番号=[%s]\n", sqlca.sqlcode,h_uid);
                    fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

                    // EXEC SQL CLOSE MRUP_TSRA01;
                    MRUP_TSRA01.curse_close();

                    /* 処理を終了する */
                    return R_const_NG;

                }
            }

            /* TSランク情報_BK更新件数カウントアップ */
            upd_ins_data_cnt++;
            out_data_cnt++;

            /* 50,000件更新毎にコミットする */
            if( upd_ins_data_cnt >= COMMIT_CHK ) {
                /* コミットする */
                // EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* クリアする */
                upd_ins_data_cnt = 0;
            }
        }

        // EXEC SQL CLOSE MRUP_TSRA01;
        MRUP_TSRA01.curse_close();

        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        /* クリアする */
        upd_ins_data_cnt = 0;
        sprintf(out_rec_buff, "TSランク情報_BK更新正常\n　UPDATE/INSERT件数=[%d]\n", out_data_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "TSランク情報_BK更新処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： DeleteTsrank_bk                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  DeleteTsrank_bk()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               TSランク情報_BK削除処理                                      */
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
    public int DeleteTsrank_bk() {

        StringDto out_rec_buff = new StringDto(256);              /** 出力レコードバッファ             **/

        /** 変数初期化                **/
        memset(out_rec_buff, 0x00, sizeof(out_rec_buff));

        if(DBG_LOG == 1){
            /*---------------------------------------------------------------------*/
            printf( "TSランク情報_BK削除処理 開始\n" );
            /*---------------------------------------------------------------------*/
        }

        /* TSランク情報_BK削除処理 */
        /*
        EXEC SQL DELETE FROM TSランク情報_BK MM_BK
        WHERE NOT EXISTS(
                SELECT MM_I.顧客番号
                FROM TSランク情報 MM_I
                WHERE MM_BK.顧客番号 = MM_I.顧客番号);
        */
        sqlca.sql = new StringDto("DELETE FROM TSランク情報_BK MM_BK\n" +
                "        WHERE NOT EXISTS(\n" +
                "                SELECT MM_I.顧客番号\n" +
                "                FROM TSランク情報 MM_I\n" +
                "                WHERE MM_BK.顧客番号 = MM_I.顧客番号)");
        sqlca.restAndExecute();

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (   sqlca.sqlcode != R_const_Ora_OK
                && sqlca.sqlcode != R_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_rec_buff, "TSランク情報_BK削除処理DELETEエラー sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);
            /* 処理を終了する */
            return R_const_NG;
        }
        out_del_cnt = sqlca.updateCount;
        /* コミットする */
        // EXEC SQL COMMIT WORK;
        sqlca.commit();
        sprintf(out_rec_buff, "TSランク情報_BK更新正常\n　DELETE件数=[%d]\n", out_del_cnt);
        fwrite(out_rec_buff, strlen(out_rec_buff), 1, fp_out);

        if(DBG_LOG == 1){
            /*---------------------------------------------*/
            printf( "TSランク情報_BK削除処理 終了\n" );
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return R_const_OK;
    }

}
