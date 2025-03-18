package jp.co.mcc.nttdata.batch.business.service.cmBTspmeB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTspmeB.dto.Koubai_Rereki;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


/*******************************************************************************
 *   プログラム名   ： 購買履歴作成処理（cmBTspmeB）
 *
 *   【処理概要】
 *     本機能は、顧客管理基盤において、SPSS(MK分析システム)に連携する用の
 *   「購買履歴(POS・EC)」データファイルを作成するものである。
 *
 *   【引数説明】
 *     -sd 抽出対象開始日付:（任意）抽出対象範囲　開始日付（YYYYMMDD）
 *     -ed 抽出対象終了日付:（任意）抽出対象範囲　終了日付（YYYYMMDD）
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
 *      30.00 :  2021/08/10 NBS.高橋：初版
 *      31.00 :  2021/09/22 NBS.山本：取引データ重複対応
 *      32.00 :  2021/09/24 NBS.高橋：免税フラグ対応
 *      33.00 :  2021/11/19 SSI.張：  EC会員.EC取引重複対応
 *      33.01 :  2021/11/29 SSI.張：  MK店舗CF会員を除外する条件を追加
 *      33.02 :  2021/12/02 SSI.張：重複除去対応(出力ファイル名修正)
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 ******************************************************************************/
@Service
public class CmBTspmeBServiceImpl extends CmABfuncLServiceImpl implements CmBTspmeBService {


    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    StringDto h_str_sql = new StringDto(12288);              /* 実行用SQL文字列              */
    int h_daytime_fm;                  /* 抽出期間開始(YYYYMMDD)       */
    int h_daytime_to;                  /* 抽出期間開始(YYYYMMDD)       */
    int h_bat_date_ymd_yes;            /* バッチ処理前日付(YYYYMMDD)   */
    int h_daytime_yyyymm;              /* 抽出年月                     */
    Koubai_Rereki koubai_t = new Koubai_Rereki();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    String DEF_YMD_1 = "-sd";               /* 抽出対象範囲日付                   */
    String DEF_YMD_2 = "-ed";               /* 抽出対象範囲日付                   */
    /*---------------------------------------------*/
    String C_PRGNAME = "購買履歴作成";    /* APログ用機能名                       */
    int DEF_MONTH_AGO = 15;     /* 過去検索可能範囲（か月）             */

    String DEF_FILE_NAME = "_KOBAIURI";   /* 出力ファイル名                       */
    int DEF_COMPANY_CD = 2500;   /* 会社コード                           */
    int DEF_STORE_CD = 9904;   /* 店舗コード                           */
    String DEF_ITEM_NAME = "商品名なし";  /* 商品名                               */
    int DEF_TAX_FREE_FLG = 9;      /* 免税フラグ                           */
    String DEF_NULL = "NULL";        /* 商品名なし                           */
    String DEF_HIKAIINNNO = "2999999999999";     /* 非会員会員番号                 */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /* 引数用 */
    int chk_arg_debug;                 /*  引数-debugチェック用              */
    StringDto arg_debug_value = new StringDto(6 + 1);          /*  引数debug設定値                   */
    int chk_arg_sd;                    /*  引数-sdチェック用                 */
    StringDto arg_sd_value = new StringDto(8 + 1);             /*  引数sd設定値                      */
    int chk_arg_ed;                    /*  引数-edチェック用                 */
    StringDto arg_ed_value = new StringDto(8 + 1);             /*  引数ed設定値                      */
    int is_get;                        /* 引数両方取得チェック               */

    /* ファイル用 */
    StringDto out_file_dir = new StringDto(4096);
    /**
     * ワークディレクトリ
     **/
    StringDto fl_name = new StringDto(4096);
    /**
     * ファイル名
     **/

    /* 件数カウンタ */
    long get_cnt;                      /* 取得件数                            */
    long out_cnt;                      /* 出力件数                            */
    StringDto file_date = new StringDto(8 + 1);               /* ファイル作成日                      */

    /* 現在日時 */
    StringDto sys_date = new StringDto(8 + 1);                 /* システム年月日                     */
    StringDto sys_time = new StringDto(6 + 1);                 /* システム時分秒                     */

    /* 対象テーブル年月 */
    StringDto tbl_yyyymm = new StringDto(6 + 1);               /* 対象テーブル年月                   */

    /* ----------------------------- */
    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /*  APログ用                      */
    StringDto bat_date_prev = new StringDto(9);
    /**
     * バッチ処理日付前日
     **/
    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_out;                          /* 出力ファイルポインタ         */

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
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/
        int rtn_cd;                     /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数結果ステータス                 */
        String env_wrk;                   /* 出力ファイルDIR                    */
        int arg_cnt;                    /* 引数チェック用カウンタ             */
        StringDto arg_work = new StringDto(256);              /* 引数用ワークバッファ               */
        int flg_sd;                     /* -sd引数の入力を確認するフラグ      */
        int flg_ed;                     /* -ed引数の入力を確認するフラグ      */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        memset(arg_debug_value, 0x00, sizeof(arg_debug_value));
        memset(arg_sd_value, 0x00, sizeof(arg_sd_value));
        memset(arg_ed_value, 0x00, sizeof(arg_ed_value));
        chk_arg_sd = DEF_OFF;
        chk_arg_ed = DEF_OFF;
        flg_sd = 0;                        /*  引数入力ありなしフラグ             */
        flg_ed = 0;
        is_get = 0;                        /*  引数両方取得ありなしフラグ         */
        memset(tbl_yyyymm, 0x00, sizeof(tbl_yyyymm));

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd,
                    0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg",
                    rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*------------------------------------------------------------*/
        C_DbgStart("*** main処理 ***");
        /*------------------------------------------------------------*/

        /*--------------------------------*/
        /* システム日時の取得                 */
        /*--------------------------------*/
        memset(sys_date, 0x00, sizeof(sys_date));
        memset(sys_time, 0x00, sizeof(sys_time));
        rtn_cd = C_GetSysDateTime(sys_date, sys_time);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null,
                    "C_GetSysDateTime", rtn_cd, 0, 0, 0, 0);
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** システム日付取得エラー %d\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*------------------------------------------------------------*/
            return exit(C_const_APNG);
        }
        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
        /*------------------------------------------------------------*/

        /* 引数チェック */
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_work, 0x00, sizeof(arg_work));
            strcpy(arg_work, argv[arg_cnt]);

            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_work);
            /*------------------------------------------------------------*/

            if (0 == strcmp(arg_work, DEF_DEBUG) ||
                    0 == strcmp(arg_work, DEF_debug)) {
                continue;

            } else if (0 == memcmp(arg_work, DEF_YMD_1, 3)) {   /* -sdの場合      */
                rtn_cd = cmBTspmeB_Chk_Arg(arg_work);        /* パラメータCHK  */
                if (rtn_cd == C_const_OK) {
                    strncpy(arg_sd_value, arg_work.arr.substring(3), 8);
                    flg_sd = 1;
                } else {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
                    /*------------------------------------------------------------*/
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else if (0 == memcmp(arg_work, DEF_YMD_2, 3)) {   /* -edの場合      */
                rtn_cd = cmBTspmeB_Chk_Arg(arg_work);        /* パラメータCHK  */
                if (rtn_cd == C_const_OK) {
                    strncpy(arg_ed_value, arg_work.arr.substring(3), 8);
                    flg_ed = 1;
                } else {
                    /*------------------------------------------------------------*/
                    C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
                    /*------------------------------------------------------------*/
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else {                                        /* 定義外パラメータ   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_work);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                /*------------------------------------------------------------*/
                C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
                /*------------------------------------------------------------*/
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */


        if ((flg_sd == 1) && (flg_ed == 1)) {              /*  引数入力あり場合    */
            /* パラメータ妥当性チェック */
            rtn_cd = cmBTspmeB_Chk_Date();
            if (rtn_cd != C_const_OK) {
                /*------------------------------------------------------------*/
                C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
                /*------------------------------------------------------------*/
                rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了     */
                return exit(C_const_APNG);
            }
            /* 抽出範囲を取得 */
            h_daytime_fm = atoi(arg_sd_value);
            h_daytime_to = atoi(arg_ed_value);

            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_main *** 取得開始=[%d]\n", h_daytime_fm);
            C_DbgMsg("*** cmBTspcdB_main *** 取得終了=[%d]\n", h_daytime_to);
            /*--------------------------------------------------------------------*/
        } else {
            is_get = 1;                            /*  引数入力なし場合     */
        }       /*if END */

        /*-------------------------------------------------------------*/
        C_DbgMsg("引数の値(-sd)    =[%s]\n", arg_sd_value);
        C_DbgMsg("引数の値(-ed)    =[%s]\n", arg_ed_value);
        /*-------------------------------------------------------------*/

        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n",
                "CM_APWORK_DATE");
        /*-------------------------------------------------------------*/
        env_wrk = getenv(CM_APWORK_DATE);
        if (StringUtils.isEmpty(env_wrk)) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*---------------------------------------------*/
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* ファイルDIRセット */
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_wrk);

        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n",
                out_file_dir);
        C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
        /*-------------------------------------------------------------*/

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_SD);
        /*------------------------------------------------------------*/

        rtn_status.arr = C_const_OK; /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  バッチ処理日（前日）取得処理呼び出し                 */
        /*-----------------------------------------------*/
        /*-------------------------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
        /*-------------------------------------------------------------*/

        memset(bat_date_prev, 0x00, sizeof(bat_date_prev));

        rtn_cd = C_GetBatDate(-1, bat_date_prev, rtn_status);
        if (rtn_cd != C_const_OK) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*---------------------------------------------*/
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理 */
            return exit(C_const_APNG);
        }

        /*---------------------------------------------*/
        C_DbgMsg("*** main *** バッチ処理日取得（前日）OK [%s]\n",
                bat_date_prev);
        /*---------------------------------------------*/

        h_bat_date_ymd_yes = atoi(bat_date_prev);

        /*-------------------------------------*/
        /*  出力ファイルOPEN                   */
        /*-------------------------------------*/
        /*------------------------------------------------------------*/
        C_DbgMsg("*** main *** 購買履歴作成データファイル(%s)\n", "オープン");
        /*------------------------------------------------------------*/
        rtn_cd = cmBTspmeB_OpenFile();
        if (rtn_cd != C_const_OK) {
            /*---------------------------------------------*/
            C_DbgMsg("*** main *** 購買履歴作成データデータファイルオープン NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*---------------------------------------------*/
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTspmeB_main();

        if (rtn_cd != C_const_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** cmBspmeB_main NG rtn=[%d]\n", rtn_cd);
            C_DbgEnd("*** main処理 ***", C_const_APNG, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("912", 0, null, "購買履歴作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            fclose(fp_out);               /* File Close                         */
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        fclose(fp_out);                         /* File Close                     */

        /* 処理件数を出力 */
        sprintf(chg_format_buf, "HSポイント明細商品情報%s", tbl_yyyymm);
        APLOG_WT("106", 0, null, chg_format_buf,
                get_cnt, get_cnt, 0, 0, 0);
        if (out_cnt != 0) {
            APLOG_WT("105", 0, null, fl_name, out_cnt, 0, 0, 0, 0);
        }
        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*------------------------------------------------------------*/
        C_DbgEnd("*** main処理 ***", 0, 0, 0);
        /*------------------------------------------------------------*/

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /*  コミット解放処理 */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease(sqlca);

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_Chk_Arg( char *Arg_in )                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*              １）重複チェック                                              */
    /*              ２）桁数チェック                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： char   *   Arg_in      引数値                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_Chk_Arg(StringDto Arg_in) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_Chk_Arg処理");
        C_DbgMsg("*** cmBTspmeB_Chk_Arg *** 引数=[%s]\n", Arg_in);
        /*---------------------------------------------------------------------*/

        int rtn_cd;                         /* 関数戻り値                     */
        int loop_cnt;                       /* ループカウンタ                 */
        StringDto wk_ymd = new StringDto(9);                      /* 処理日付変換用                 */

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        if (0 == memcmp(Arg_in, DEF_YMD_1, 3)) {        /* -sd処理対象日数CHK     */
            if (chk_arg_sd != DEF_OFF) {
                sprintf(chg_format_buf, "-sd 引数の値が不正です(重複)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                /*----------------------------------------------------------------*/
                C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------------*/
                return C_const_NG;
            }
            chk_arg_sd = DEF_ON;

            if (strlen(Arg_in) < 11) {                /* 桁数チェック           */
                sprintf(chg_format_buf, "-sd 引数の値が不正です(桁数)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                /*----------------------------------------------------------------*/
                C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------------*/
                return C_const_NG;
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 8);
            for (loop_cnt = 0; loop_cnt < 8; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.arr.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-sd 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }

        } else if (0 == memcmp(Arg_in, DEF_YMD_2, 3)) { /* -ed処理対象日数CHK     */
            if (chk_arg_ed != DEF_OFF) {
                sprintf(chg_format_buf, "-ed 引数の値が不正です(重複)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                /*----------------------------------------------------------------*/
                C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------------*/
                return C_const_NG;
            }
            chk_arg_ed = DEF_ON;

            if (strlen(Arg_in) < 11) {                /* 桁数チェック           */
                sprintf(chg_format_buf, "-ed 引数の値が不正です(桁数)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                /*----------------------------------------------------------------*/
                C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                /*----------------------------------------------------------------*/
                return C_const_NG;
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 8);
            for (loop_cnt = 0; loop_cnt < 8; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.arr.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-ed 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 6);
            for (loop_cnt = 0; loop_cnt < 6; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.arr.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-t2 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

                    /*------------------------------------------------------------*/
                    C_DbgEnd("cmBTspcdB_Chk_Arg処理", C_const_NG, 0, 0);
                    /*------------------------------------------------------------*/
                    return C_const_NG;
                }
            }
        }

        /*---------------------------------------------------------------------*/
        C_DbgEnd("cmBTspmeB_Chk_Arg処理", 0, 0, 0);
        /*---------------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTspmeB_Chk_Arg Bottom -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_Chk_Date                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_Chk_Date( )                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数の日付妥当性チェックを行う                                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_Chk_Date() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_Chk_Date処理");
        /*---------------------------------------------------------------------*/

        /* ローカル変数 */
        int rtn_cd;                 /* 関数戻り値                             */
        IntegerDto i_sysdate_yyyy = new IntegerDto();         /* システム年月日（YYYY）                 */
        IntegerDto i_sysdate_mm = new IntegerDto();           /* システム年月日（MM）                   */
        IntegerDto i_sysdate_dd = new IntegerDto();           /* システム年月日（MM）                   */
        IntegerDto i_sysdate_yyyymm = new IntegerDto();       /* システム年月日（YYYYMM）               */
        /* 抽出期間開始日 */
        IntegerDto i_fm_yyyy = new IntegerDto();                      /* 抽出期間開始日（YYYY）         */
        IntegerDto i_fm_mm = new IntegerDto();                       /* 抽出期間開始日（MM）           */
        IntegerDto i_fm_dd = new IntegerDto();                       /* 抽出期間開始日（DD）           */
        IntegerDto i_fm_yyyymm = new IntegerDto();                   /* 抽出期間開始日（YYYYMM）       */
        /* 抽出期間終了日 */
        IntegerDto i_to_yyyy = new IntegerDto();                      /* 抽出期間終了日（YYYY）         */
        IntegerDto i_to_mm = new IntegerDto();                        /* 抽出期間終了日（MM）           */
        IntegerDto i_to_dd = new IntegerDto();                        /* 抽出期間終了日（DD）           */
        IntegerDto i_to_yyyymm = new IntegerDto();                    /* 抽出期間終了日（YYYYMM）       */

        /* 過去検索可能年月 */
        int i_ago_yyyymm;              /* 過去検索可能年月（YYYY）       */
        StringDto c_ago_yyyymm = new StringDto(9);           /* 過去検索可能年月（YYYY）       */

        StringDto buff = new StringDto(80);                      /* バッファ                       */

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        /* 抽出期間開始日 */
        cmBTspmeB_CutYMD(arg_sd_value,
                i_fm_yyyy, i_fm_mm, i_fm_dd, i_fm_yyyymm);
        /* 抽出期間終了日 */
        cmBTspmeB_CutYMD(arg_ed_value,
                i_to_yyyy, i_to_mm, i_to_dd, i_to_yyyymm);

        /* システム日付 */
        cmBTspmeB_CutYMD(sys_date,
                i_sysdate_yyyy, i_sysdate_mm, i_sysdate_dd,
                i_sysdate_yyyymm);

        /* 抽出年月 */
        h_daytime_yyyymm = i_fm_yyyymm.arr;

        /*********************/
        /* 日付型チェック    */
        /*********************/
        /* 抽出期間開始日 */
        rtn_cd = cmBTspmeB_checkYMD(i_fm_yyyy, i_fm_mm, i_fm_dd);
        if (rtn_cd != 1) {
            sprintf(chg_format_buf, "-sd 引数の値が不正です(日付型)（%s）",
                    arg_sd_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /* 抽出期間終了日 */
        rtn_cd = cmBTspmeB_checkYMD(i_to_yyyy, i_to_mm, i_to_dd);
        if (rtn_cd != 1) {
            sprintf(chg_format_buf, "-ed 引数の値が不正です(日付型)（%s）",
                    arg_ed_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /*********************/
        /* 年月範囲チェック  */
        /*********************/
        /* 過去検索可能年月設定（15か月前） */
        sprintf(c_ago_yyyymm, "%d%02d", (i_sysdate_yyyy.arr - 1),
                (i_sysdate_mm.arr - DEF_MONTH_AGO + 12));
        i_ago_yyyymm = atoi(c_ago_yyyymm);

        /*------------------------------------------------------------*/
        sprintf(buff, "抽出可能年月：[%d]～[%d]", i_ago_yyyymm, i_sysdate_yyyymm);
        C_DbgMsg("*** cmBTspmeB_Chk_Date ***  検索可能年月=[%d]\n",
                i_ago_yyyymm);
        C_DbgMsg("*** cmBTspmeB_Chk_Date ***  %s\n", buff);
        /*------------------------------------------------------------*/

        /* 抽出期間開始日 */
        if (i_fm_yyyymm.arr < i_ago_yyyymm || i_fm_yyyymm.arr > i_sysdate_yyyymm.arr) {
            sprintf(chg_format_buf, "-sd 引数の値が不正です(日付範囲)（%s）",
                    arg_sd_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /* 抽出期間終了日 */
        if (i_to_yyyymm.arr < i_ago_yyyymm || i_to_yyyymm.arr > i_sysdate_yyyymm.arr) {
            sprintf(chg_format_buf, "-ed 引数の値が不正です(日付範囲)（%s）",
                    arg_ed_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /*********************/
        /* 日付比較チェック  */
        /*********************/
        if (atoi(arg_sd_value) > atoi(arg_ed_value)) {
            sprintf(chg_format_buf, "-sd -ed 引数の値が不正です(日付)（%s）（%s）",
                    arg_sd_value, arg_ed_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /*********************/
        /* 年月チェック  */
        /*********************/
        if (i_fm_yyyymm.arr.longValue() != i_to_yyyymm.arr.longValue()) {
            sprintf(chg_format_buf, "-sd -ed 引数の値が不正です(年月不一致) （%s）（%s）", arg_sd_value, arg_ed_value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspmeB_Chk_Date処理", C_const_NG, 0, 0);
            /*--------------------------------------------------------------------*/
            return C_const_NG;
        }

        /*---------------------------------------------------------------------*/
        C_DbgEnd("cmBTspmeB_Chk_Date処理", 0, 0, 0);
        /*---------------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTspmeB_Chk_Date Bottom ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_CutYMD                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_CutYMD( char *date_buf, int *year, int *month,      */
    /*                                int *day, int *yearmonth )                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              指定された年月日を年、月、日に分解する。                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： char *date_buf   対象年月日                                      */
    /*    OUT ： int  *year       対象年                                          */
    /*        ： int  *month      対象月                                          */
    /*        ： int  *day        対象日                                          */
    /*        ： int  *yearmonth  対象年月                                        */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public void cmBTspmeB_CutYMD(StringDto date_buf, IntegerDto year, IntegerDto month, IntegerDto day, IntegerDto yearmonth) {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_CutYMD処理");
        /*---------------------------------------------------------------------*/

        /* ローカル変数 */
        StringDto wk_buf = new StringDto(256);                    /* 編集バッファ                   */

        /* 年 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.arr, 4);
        year.arr = atoi(wk_buf);

        /* 月 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.arr.substring(4), 2);
        month.arr = atoi(wk_buf);

        /* 日 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.arr.substring(6), 2);
        day.arr = atoi(wk_buf);

        /* 年月 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.arr, 6);
        yearmonth.arr = atoi(wk_buf);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_CutYMD *** 年月日 [%s]\n", date_buf);
        C_DbgMsg("*** cmBTspmeB_CutYMD *** 年 [%d]\n", year);
        C_DbgMsg("*** cmBTspmeB_CutYMD *** 月 [%d]\n", month);
        C_DbgMsg("*** cmBTspmeB_CutYMD *** 日 [%d]\n", day);
        C_DbgMsg("*** cmBTspmeB_CutYMD *** 年月 [%d]\n", yearmonth);
        C_DbgEnd("cmBTspmeB_CutYMD処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return;
        /*--- cmBTspmeB_Chk_Date Bottom ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_checkYMD                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_checkYMD( int year, int month, int day )            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              指定された年月日の妥当性をチェックをする。                    */
    /*              1900年未満は不正とします。                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： int  year        対象年                                          */
    /*        ： int  month       対象月                                          */
    /*        ： int  day         対象日                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 日付として妥当でない                                   */
    /*              1   ： 日付として妥当である                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_checkYMD(IntegerDto year, IntegerDto month, IntegerDto day) {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_checkYMD処理");
        C_DbgMsg("*** cmBTspmeB_checkYMD *** 年 [%d]\n", year);
        C_DbgMsg("*** cmBTspmeB_checkYMD *** 月 [%d]\n", month);
        C_DbgMsg("*** cmBTspmeB_checkYMD *** 日 [%d]\n", day);
        /*------------------------------------------------------------*/

        /* ローカル変数 */
        int isCheck;

        if (year.arr < 1900) {
            isCheck = 0;
        } else {
            isCheck = ((month.arr >= 1) && (month.arr <= 12) &&
                    (day.arr >= 1) &&
                    (day.arr <= cmBTspmeB_daysInMonth(year.arr, month.arr))) ? 1 : 0;
        }

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_checkYMD *** 日付妥当性チェック [%d]\n", isCheck);
        C_DbgEnd("cmBTspmeB_checkYMD処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return isCheck;
        /*--- cmBTspmeB_Chk_YMD Bottom ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_daysInMonth                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_daysInMonth()                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*           指定された年月の月末日を取得する。                               */
    /*           指定された年が1900未満の場合、０を返却する。                     */
    /*           指定された月が不正(１～１２以外）の場合、０を返却する。          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： int  year        対象年                                          */
    /*        ： int  month       対象月                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              指定された年月の月末日                                        */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_daysInMonth(int year, int month) {
        /* ローカル変数 */
        int lastDayInMonth;
        int nDaysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        /*------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_daysInMonth処理");
        /*------------------------------------------------------------*/

        if ((month < 1) || (12 < month)) {
            lastDayInMonth = 0;
        } else if ((2 == month) && (1 == cmBTspmeB_isLeapYear(year))) {
            lastDayInMonth = 29;
        } else {
            lastDayInMonth = nDaysInMonth[month - 1];
        }

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_daysInMonth *** 月末日取得 [%d]\n", lastDayInMonth);
        C_DbgEnd("cmBTspmeB_daysInMonth処理", 0, 0, 0);
        /*------------------------------------------------------------*/
        return lastDayInMonth;
        /*--- cmBTspmeB_daysInMonth Bottom -------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_isLeapYear                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_isLeapYear()                                        */
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
    public int cmBTspmeB_isLeapYear(int year) {
        /* ローカル変数 */
        int isLeapYear;

        /*------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_isLeapYear処理");
        /*------------------------------------------------------------*/

        isLeapYear = (0 == (year % 400)) ||
                ((0 != (year % 100)) && (0 == (year % 4))) ? 1 : 0;

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_isLeapYear *** 閏年判定 [%d]\n", isLeapYear);
        C_DbgEnd("cmBTspmeB_isLeapYear処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return isLeapYear;
        /*--- cmBTspmeB_isLeapYear Bottom -------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTspmeB_main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     購買履歴作成データファイル作成主処理                                   */
    /*     購買履歴作成データファイルを作成する。                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_main() {
        int rtn_cd;                      /* 関数戻り値                       */
        StringDto wk_out_buff = new StringDto(4096);           /* ログ出力情報格納用               */

        /*------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_main処理");
        /*------------------------------------------------------------*/

        /* 初期化 */
        get_cnt = 0;
        out_cnt = 0;

        /*SQL文をセットする*/
        if (is_get == 1) {
            /* 抽出対象範囲が指定なし */
            cmBTspmeB_set_Sql();
        } else {
            /* 抽出対象範囲が指定なあり */
            cmBTspmeB_set_Sql_Arg();
        }

        /*動的SQL解析*/
        // EXEC SQL PREPARE sql_stat1 from :h_str_sql;
        SqlstmDto sqlca = sqlcaManager.get("CUR_SPME");
        sqlca.sql = h_str_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspmeB_main *** 動的SQL 解析NG = %d\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTspmeB_main処理", C_const_NG, 0, 0);
            /*------------------------------------------------------------*/
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    h_str_sql, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* カーソル宣言 */
        // EXEC SQL DECLARE CUR_SPME CURSOR FOR sql_stat1;

        /* カーソルオープン */
        if (is_get == 1) {                                     /* 引数指定なし場合 */
            /*EXEC SQL OPEN CUR_SPME USING :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes;*/
            sqlca.query(h_bat_date_ymd_yes, h_bat_date_ymd_yes,
                    h_bat_date_ymd_yes, h_bat_date_ymd_yes,
                    h_bat_date_ymd_yes, h_bat_date_ymd_yes);
        } else {                                                /* 引数指定あり場合 */
            /*EXEC SQL OPEN CUR_SPME USING :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_daytime_fm,
                                    :h_daytime_to,
                                    :h_bat_date_ymd_yes,
                                    :h_bat_date_ymd_yes,
                                    :h_daytime_fm,
                                    :h_daytime_to;*/
            sqlca.query(h_bat_date_ymd_yes, h_bat_date_ymd_yes,
                    h_daytime_fm, h_daytime_to,
                    h_bat_date_ymd_yes, h_bat_date_ymd_yes,
                    h_daytime_fm, h_daytime_to);

        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspmeB_main *** CURSOL OPEN ERR=[%d]\n",
                    sqlca.sqlcode);
            C_DbgEnd("cmBTspmeB_main処理", C_const_NG, 0, 0);
            /*------------------------------------------------------------*/
            sprintf(chg_format_buf, "HSポイント明細商品情報%s CURSOL OPEN ERR",
                    tbl_yyyymm);
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    chg_format_buf, 0, 0, 0, 0);
            return C_const_NG;
        }
        /*------------------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_main *** CURSOR OPEN sqlcode =[%d]\n", sqlca.sqlcode);
        /*------------------------------------------------------------------------*/

        /*-------------------------*/
        /* 連動対象データを取得    */
        /*-------------------------*/
        while (true) {
            /* 初期化 */
            // memset(&koubai_t, 0x00, sizeof(koubai_t) );

            /* カーソルフェッチ */
            /*EXEC SQL FETCH CUR_SPME
            INTO :koubai_t.sales_date,       *//* 売上日                    *//*
                     :koubai_t.store_cd,         *//* 店舗コード                *//*
                     :koubai_t.regi_no,          *//* レジNo                    *//*
                     :koubai_t.receipt_no,       *//* レシートNo                *//*
                     :koubai_t.line_num,         *//* 行番号                    *//*
                     :koubai_t.sales_time,       *//* 売上時刻                  *//*
                     :koubai_t.goopn_num,        *//* グーポン番号              *//*
                     :koubai_t.jan_cd,           *//* JANコード                 *//*
                     :koubai_t.item_name,        *//* 商品名                    *//*
                     :koubai_t.sales_vol,        *//* 売上数量                  *//*
                     :koubai_t.sales_amount_in_tax,*//* 売上金額（税込）        *//*
                     :koubai_t.coupon_nebiki_amount,  *//* クーポン値引金額     *//*
                     :koubai_t.coupon_waribiki_amount,   *//* クーポン割引金額  *//*
                     :koubai_t.coupon_nbk_amt_kkk_cd,
            *//* クーポン値引金額企画コード*//*
                     :koubai_t.sales_amount,    *//* 売上金額（税抜）          *//*
                     :koubai_t.tax_free_flg;    *//* 免税フラグ                */
            sqlca.fetch();
            sqlca.recData(koubai_t.sales_date, koubai_t.store_cd,
                    koubai_t.regi_no, koubai_t.receipt_no,
                    koubai_t.line_num, koubai_t.sales_time,
                    koubai_t.goopn_num, koubai_t.jan_cd,
                    koubai_t.item_name, koubai_t.sales_vol,
                    koubai_t.sales_amount_in_tax, koubai_t.coupon_nebiki_amount,
                    koubai_t.coupon_waribiki_amount, koubai_t.coupon_nbk_amt_kkk_cd,
                    koubai_t.sales_amount, koubai_t.tax_free_flg);

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* データ無しの場合、ブレーク */
                break;
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                /* データ無し以外エラーの場合、処理を異常終了する */
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                if (is_get == 0) {                             /* 引数指定あり場合 */
                    sprintf(chg_format_buf,
                            "HSポイント明細商品情報%s 抽出期間=[%d]～[%d]",
                            tbl_yyyymm, h_daytime_fm, h_daytime_to);
                } else {                                        /* 引数指定なし場合 */
                    sprintf(chg_format_buf,
                            "HSポイント明細商品情報%s 抽出日付=[%d]",
                            tbl_yyyymm, h_bat_date_ymd_yes);
                }
                APLOG_WT("904", 0, null, "FETCH(CUR_SPME)",
                        sqlca.sqlcode, "購買履歴作成",
                        chg_format_buf, 0, 0);
                /* カーソルクローズ */
                // EXEC SQL CLOSE CUR_SPME;
//                sqlca.close();
                sqlcaManager.close("CUR_SPME");
                return C_const_NG;
            }
            /* 処理対象件数カウントアップ */
            get_cnt++;

            /* 末尾のスペース削除 */
            BT_Rtrim(koubai_t.item_name, strlen(koubai_t.item_name.toString()));
            BT_Rtrim(koubai_t.jan_cd, strlen(koubai_t.jan_cd.toString()));
            BT_Rtrim(koubai_t.goopn_num, strlen(koubai_t.goopn_num.toString()));

            /*--------------------------------------------------------------------*/
            sprintf(wk_out_buff,
                    "売上日:[%s],店舗コード:[%d],レジNo:[%s],レシートNo:[%d]," +
                            "行番号:[%d],売上時刻:[%s],グーポン番号:[%s],JANコード:[%s]," +
                            "商品名:[%s],売上数量:[%d],売上金額（税込）:[%d]," +
                            "クーポン値引金額:[%d],クーポン割引金額:[%d]," +
                            "クーポン値引金額企画コード:[%d],売上金額（税抜）:[%d]," +
                            "免税フラグ:[%d]"
                    , koubai_t.sales_date            /* 売上日                    */
                    , koubai_t.store_cd.intVal()              /* 店舗コード                */
                    , koubai_t.regi_no               /* レジNo                    */
                    , koubai_t.receipt_no.intVal()            /* レシートNo                */
                    , koubai_t.line_num.intVal()              /* 行番号                    */
                    , koubai_t.sales_time            /* 売上時刻                  */
                    , koubai_t.goopn_num             /* グーポン番号              */
                    , koubai_t.jan_cd                /* JANコード                 */
                    , koubai_t.item_name             /* 商品名                    */
                    , koubai_t.sales_vol.intVal()             /* 売上数量                  */
                    , koubai_t.sales_amount_in_tax.intVal()   /* 売上金額（税込）          */
                    , koubai_t.coupon_nebiki_amount.intVal()  /* クーポン値引金額          */
                    , koubai_t.coupon_waribiki_amount.intVal()/* クーポン割引金額          */
                    , koubai_t.coupon_nbk_amt_kkk_cd.intVal() /* クーポン値引金額企画コード*/
                    , koubai_t.sales_amount.intVal()          /* 売上金額（税抜）          */
                    , koubai_t.tax_free_flg.intVal()          /* 免税フラグ                */
            );

            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspmeB_main *** FETCH=%s\n", wk_out_buff);
            /*--------------------------------------------------------------------*/

            /*------------------------------------------*/
            /* 購買履歴データファイル作成           */
            /*------------------------------------------*/
            rtn_cd = cmBTspmeB_WriteFile();
            if (rtn_cd == C_const_NG) {
                /* カーソルクローズ */
                // EXEC SQL CLOSE CUR_SPME;
//                sqlca.close();
                sqlcaManager.close("CUR_SPME");
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 出力件数カウントアップ */
            out_cnt++;
        }/* ループ終了 */

        /*------------------------------------------------------------*/
        C_DbgEnd("cmBTspmeB_main処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return (C_const_OK);              /* 処理終了 */
        /*--- cmBTspmeB_main Bottom ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              ファイルを書き込みモードでオープンする。                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_OpenFile() {
        /*------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_OpenFile処理");
        /*------------------------------------------------------------*/

        /* ファイル名作成、オープン */
        memset(fl_name, 0x00, sizeof(fl_name));
        sprintf(fl_name, "%s/tmp_%s%s%s.csv", out_file_dir, sys_date, sys_time,
                DEF_FILE_NAME);

        if ((fp_out = open(fl_name.arr)).fd < 0) {
            sprintf(chg_format_buf, "fopen（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);

            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspmeB_OpenFile *** ファイルオープンERR%s\n",
                    "");
            /*------------------------------------------------------------*/
            return C_const_NG;
        }

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBspmeB_OpenFile *** ファイル%s\n", fl_name);
        C_DbgEnd("cmBTspmeB_OpenFile処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        return C_const_OK;
        /*--- cmBTspmeB_OpenFile Bottom ----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_WriteFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspmeB_WriteFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル書き込み処理                                          */
    /*              取得情報を編集し、ファイル出力を行う。                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTspmeB_WriteFile() {
        StringDto wk_out_buff = new StringDto(4096);  /* 出力情報格納用               */
        StringDto wk_goopn_num = new StringDto(256);  /* 会員番号番号編集用           */

        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_WriteFile処理");
        /*---------------------------------------------------------------------*/

        /*----------------------------------*/
        /* データファイルの編集             */
        /*----------------------------------*/
        memset(wk_out_buff, 0x00, sizeof(wk_out_buff));
        memset(wk_goopn_num, 0x00, sizeof(wk_goopn_num));

        /* 定数 および 固定文言設定 */
        /* 会社コード */
        koubai_t.company_cd.arr = DEF_COMPANY_CD;

        /* 店舗コード */
        if (koubai_t.store_cd.intVal() == 0) {
            koubai_t.store_cd.arr = DEF_STORE_CD;
        }

        /* 商品名 */
        if (strcmp(koubai_t.item_name.toString(), DEF_NULL) == 0) {
            sprintf(koubai_t.item_name, "%s", DEF_ITEM_NAME);
        }

        /* グーポン番号 */
        if (strncmp(koubai_t.goopn_num.toString(), DEF_HIKAIINNNO, 13) != 0) {
            /* 非会員以外の場合、設定 */
            sprintf(wk_goopn_num, "%s", koubai_t.goopn_num);
        }

        /* 免税フラグ */
        /*koubai_t.tax_free_flg = DEF_TAX_FREE_FLG;*/

        /* ファイル出力形式編集 */
        sprintf(wk_out_buff,
                "%s,%d,%d,%d%d,%s,%d,%d,%s,%s,       ," +
                        "%s,%s,%d,%d,%d,%d,%d,%d,%d,,,,%d,%d\n"
                , koubai_t.sales_date            /* 売上日                    */
                , koubai_t.company_cd.intVal()            /* 会社コード                */
                , koubai_t.store_cd.intVal()              /* 店舗コード                */
                , koubai_t.company_cd.intVal()            /* 会社コード                */
                , koubai_t.store_cd.intVal()              /* 店舗コード                */
                , koubai_t.regi_no               /* レジNo                    */
                , koubai_t.receipt_no.intVal()            /* レシートNo                */
                , koubai_t.line_num.intVal()              /* 行番号                    */
                , koubai_t.sales_time            /* 売上時刻                  */
                , wk_goopn_num                   /* グーポン番号              */
                /* 部門コード:半角空白       */
                , koubai_t.jan_cd                /* JANコード                 */
                , koubai_t.item_name             /* 商品名                    */
                , koubai_t.sales_vol.intVal()             /* 売上数量                  */
                , koubai_t.sales_amount_in_tax.intVal()   /* 売上金額（税込）          */
                , koubai_t.arari_amount.intVal()          /* 粗利金額                  */
                , koubai_t.coupon_nebiki_amount.intVal()  /* クーポン値引金額          */
                , koubai_t.coupon_waribiki_amount.intVal()/* クーポン割引金額          */
                , koubai_t.coupon_nbk_amt_kkk_cd.intVal() /* クーポン値引金額企画コード*/
                , koubai_t.coupon_wbk_amt_kkk_cd.intVal() /* クーポン割引金額企画コード*/
                /* 会社部門コード：null      */
                /* 計上部門コード：null      */
                /* カテゴリーコード：null    */
                , koubai_t.sales_amount.intVal()          /* 売上金額（税抜）          */
                , koubai_t.tax_free_flg.intVal()          /* 免税フラグ                */
        );

        /*----------------------------------*/
        /* データファイルの出力             */
        /*----------------------------------*/
        fp_out.write(wk_out_buff.arr);
        if (ferror(fp_out) != C_const_OK) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspmeB_WriteFile *** fwrite NG rtn=[%d]\n",
                    ferror(fp_out));
            /*-------------------------------------------------------------*/
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fwrite（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        fp_out.flush();

        /*---------------------------------------------------------------------*/
        C_DbgEnd("cmBTspmeB_WriteFile処理", 0, 0, 0);
        /*---------------------------------------------------------------------*/

        return C_const_OK;
        /*-----cmBTspmeB_WriteFile Bottom---------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_set_Sql                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*      void  cmBTspmeB_set_Sql()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          ＳＱＬ文を編集する（引数無し）                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     なし                                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public void cmBTspmeB_set_Sql() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_set_Sql処理");
        /*---------------------------------------------------------------------*/

        /* ローカル変数 */
        StringDto wk_bat_yes_ym = new StringDto(6 + 1);          /* バッチ処理日前日の年月      */

        /* 初期化 */
        memset(wk_bat_yes_ym, 0x00, sizeof(wk_bat_yes_ym));

        /* バッチ処理日前日の年月を取得 */
        memcpy(wk_bat_yes_ym, bat_date_prev.arr, 6);

        /* 動的ＳＱＬ文編集 */
        sprintf(h_str_sql, "SELECT " +
                        "TO_CHAR(TO_DATE (CONCAT(t4.精算日付 , LPAD(t4.登録時刻,6,0))," +
                        "'YYYYMMDDHH24MISS'), 'YYYY/MM/DD HH24:MI:SS') as 売上日" +
                        " , t4.連携用店番号 as 店番コード " +
                        " , LPAD(t4.登録ターミナル番号,4,'0') as レジNO " +
                        " , t4.登録取引番号 as レシートNO " +
                        " , t4.アイテムシークエンス番号 as 行番号 " +
                        " , TO_CHAR(TO_TIMESTAMP(LPAD(t4.登録時刻,6,0),'HH24MISS'),'HH24:MI:SS')" +
                        " as 売上時刻 " +
                        " , t4.会員番号 as グーポン番号 " +
                        " , LPAD (t4.単品コード, CAST(t4.単品コード桁数 AS INTEGER), '0') as JANコード " +
                        " , t4.商品名称 as 商品名 " +
                        " , t4.数量 " +
                        " , t4.売価 - t4.クーポン割引額 + " +
                        "t4.利用ポイント按分金額 as 売上金額税込 " +
                        " , t4.クーポン値引額 as クーポン値引金額 " +
                        " , t4.クーポン割引額 as クーポン割引金額 " +
                        " , t4.利用クーポン１ as クーポン値引企画コード " +
                        " , t4.売価 - t4.クーポン割引額 - t4.内税額 +" +
                        "TRUNC (t4.利用ポイント按分金額 / (1 + t4.税率)) as 売上金額税抜 " +
                        " , t4.重点コード as 免税フラグ " +
                        "FROM " +
                        "( " +
                        "SELECT " +
                        "  t1.精算日付 as 精算日付 " +
                        ", t1.登録時刻 as 登録時刻 " +
                        ", t1.登録店番号 as 店番号 " +
                        ", t1.登録ターミナル番号 as 登録ターミナル番号 " +
                        ", DECODE(t1.登録企業コード,'1020',t1.取引連番,t1.登録取引番号) as 登録取引番号 " +
                        ", t2.アイテムシークエンス番号 as アイテムシークエンス番号 " +
                        ", DECODE(t1.登録企業コード,'1020',CONCAT(t1.会員番号  , 'E') ,CAST(t1.会員番号 AS TEXT)) as 会員番号 " +
                        ", NULLIF(TRIM(t2.単品コード),'') as 単品コード " +
                        ", NVL(NULLIF(TRIM(t5.商品名),''), '%s') as 商品名称 " +
                        ", t2.数量 as 数量 " +
                        ", t2.売価 as 売価 " +
                        ", DECODE(t1.登録企業コード, '1020', t2.小計値引按分金額, " +
                        "t2.サービス券按分額) as 利用ポイント按分金額 " +
                        ", t2.クーポン割引額 as クーポン値引額 " +
                        ", DECODE(t1.登録企業コード, '1020', 0, " +
                        "t2.小計値引按分金額 - t2.サービス券按分額)" +
                        "as クーポン割引額 " +
                        ", NVL(t2.利用クーポン１,0) as 利用クーポン１ " +
                        ", t2.内税額 as 内税額 " +
                        ", t2.税率/100 as 税率 " +
                        ", DECODE(t2.重点コード, '0000000001',0,9) as 重点コード " +
                        ", NVL(t3.連携用店番号,0) as 連携用店番号 " +
                        ", NVL(t5.単品コード桁数,13) as 単品コード桁数 " +
                        "FROM " +
                        "HSポイント明細取引情報%s t1 " +
                        "INNER JOIN " +
                        "HSポイント明細商品情報%s t2 " +
                        "ON t1.システム年月日 = t2.システム年月日 " +
                        "AND t1.サービス対応番号 = t2.サービス対応番号 " +
                        "AND t1.取引通番 = t2.取引通番 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 店番号, 連携用店番号 " +
                        "FROM " +
                        "(SELECT  店番号, 連携用店番号,ROW_NUMBER() OVER" +
                        "(PARTITION BY 店番号 ORDER BY 終了年月日 DESC ) G_row " +
                        "FROM PS店表示情報 " +
                        "where 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ") t3 " +
                        "ON t3.店番号=t1.登録店番号 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名 " +
                        "FROM " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名, ROW_NUMBER() " +
                        "OVER (PARTITION BY 単品コード ORDER BY 終了年月日 DESC, 開始年月日 DESC ) " +
                        "G_row " +
                        "FROM MS商品コード情報 " +
                        "WHERE 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ")t5 " +
                        "ON t5.単品コード = t2.単品コード " +
                        "WHERE t1.システム年月日 = ? " +
                        "AND t1.取引区分 IN (100, 102) " +
                        "AND t1.カード忘れフラグ IN (0, 1) " +
                        "AND t1.登録企業コード <>3000 " +
                        "AND CAST(CONCAT(t2.大分類コード , t2.中分類コード) AS NUMERIC ) <> 623 " +
                        "AND CAST(SUBSTR(LPAD(t2.単品コード,CAST(NVL(t5.単品コード桁数,13) AS INTEGER ),'0'),1,3) AS NUMERIC ) <> 200 " +
                        "UNION " +
                        "SELECT " +
                        "  t1.精算日付 as 精算日付 " +
                        ", t1.登録時刻 as 登録時刻 " +
                        ", t1.登録店番号 as 店番号 " +
                        ", t1.登録ターミナル番号 as 登録ターミナル番号 " +
                        ", DECODE(t1.登録企業コード,'1020',t1.取引連番,t1.登録取引番号) as 登録取引番号 " +
                        ", t2.アイテムシークエンス番号 as アイテムシークエンス番号 " +
                        ", DECODE(t1.登録企業コード,'1020',CONCAT(t1.会員番号 , 'E') ,CAST(t1.会員番号 AS TEXT)) as 会員番号 " +
                        ", NULLIF(TRIM(t2.単品コード),'') as 単品コード " +
                        ", NVL(NULLIF(TRIM(t5.商品名),''), '%s') as 商品名称 " +
                        ", t2.数量 as 数量 " +
                        ", t2.売価 as 売価 " +
                        ", DECODE(t1.登録企業コード, '1020', t2.小計値引按分金額, " +
                        "t2.サービス券按分額) as 利用ポイント按分金額 " +
                        ", t2.クーポン割引額 as クーポン値引額 " +
                        ", DECODE(t1.登録企業コード, '1020', 0, " +
                        "t2.小計値引按分金額 - t2.サービス券按分額)" +
                        "as クーポン割引額 " +
                        ", NVL(t2.利用クーポン１,0) as 利用クーポン１ " +
                        ", t2.内税額 as 内税額 " +
                        ", t2.税率/100 as 税率 " +
                        ", DECODE(t2.重点コード, '0000000001',0,9) as 重点コード " +
                        ", NVL(t3.連携用店番号,0) as 連携用店番号 " +
                        ", NVL(t5.単品コード桁数,13) as 単品コード桁数 " +
                        "FROM " +
                        "HSポイント明細取引情報%s t1 " +
                        "INNER JOIN " +
                        "HSポイント明細商品情報%s t2 " +
                        "ON t1.システム年月日 = t2.システム年月日 " +
                        "AND t1.サービス対応番号 = t2.サービス対応番号 " +
                        "AND t1.取引通番 = t2.取引通番 " +
                        "LEFT OUTER JOIN  " +
                        "(SELECT 店番号, 連携用店番号 " +
                        "FROM " +
                        "(SELECT 店番号, 連携用店番号,ROW_NUMBER() OVER " +
                        "(PARTITION BY 店番号 ORDER BY 終了年月日 DESC ) G_row " +
                        "FROM PS店表示情報 " +
                        "where 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ") t3 " +
                        "ON t3.店番号 = t1.登録店番号 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名 " +
                        "FROM " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名, ROW_NUMBER() " +
                        "OVER (PARTITION BY 単品コード ORDER BY 終了年月日 DESC, 開始年月日 DESC ) " +
                        "G_row " +
                        "FROM MS商品コード情報 " +
                        "WHERE 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ")t5  " +
                        "ON t5.単品コード = t2.単品コード " +
                        "WHERE t1.後付け登録日付 = ? " +
                        "AND t1.取引区分 IN (100, 102) " +
                        "AND t1.カード忘れフラグ = 1 " +
                        "AND t1.登録企業コード <>3000 " +
                        "AND CAST(CONCAT(t2.大分類コード , t2.中分類コード) AS NUMERIC) <> 623 " +
                        "AND CAST(SUBSTR(LPAD(t2.単品コード,CAST(NVL(t5.単品コード桁数,13) AS INTEGER ),'0'),1,3) AS NUMERIC ) <> 200 " +
                        ") t4 " +
                        "ORDER BY " +
                        " 売上日 " +
                        ",店番コード " +
                        ",レジNo " +
                        ",レシートNo " +
                        ",行番号 "
                , DEF_NULL, wk_bat_yes_ym, wk_bat_yes_ym
                , DEF_NULL, wk_bat_yes_ym, wk_bat_yes_ym
        );

        /* 対象テーブル年月　格納 */
        strcpy(tbl_yyyymm, wk_bat_yes_ym);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_set_Sql *** 抽出SQL [%s]\n", h_str_sql);
        C_DbgEnd("cmBTspmeB_set_Sql処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTspmeB_set_Sql Bottom -----------------------------------------------*/
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspmeB_set_Sql_Arg                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*      void  cmBTspmeB_set_Sql_Arg()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          ＳＱＬ文を編集する（引数あり）                                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     なし                                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public void cmBTspmeB_set_Sql_Arg() {
        /*---------------------------------------------------------------------*/
        C_DbgStart("cmBTspmeB_set_Sql_Arg処理");
        /*---------------------------------------------------------------------*/

        sprintf(h_str_sql, "SELECT " +
                        "TO_CHAR(TO_DATE (CONCAT(t4.精算日付 , LPAD(t4.登録時刻,6,0))," +
                        "'YYYYMMDDHH24MISS'), 'YYYY/MM/DD HH24:MI:SS') as 売上日" +
                        " , t4.連携用店番号 as 店番コード " +
                        " , LPAD(t4.登録ターミナル番号,4,'0') as レジNO " +
                        " , t4.登録取引番号 as レシートNO " +
                        " , t4.アイテムシークエンス番号 as 行番号 " +
                        " , TO_CHAR(TO_TIMESTAMP(LPAD(t4.登録時刻,6,0),'HH24MISS'),'HH24:MI:SS')" +
                        " as 売上時刻 " +
                        " , t4.会員番号 as グーポン番号 " +
                        " , LPAD (t4.単品コード, CAST(t4.単品コード桁数 AS INTEGER), '0') as JANコード " +
                        " , t4.商品名称 as 商品名 " +
                        " , t4.数量 " +
                        " , t4.売価 - t4.クーポン割引額 + " +
                        "t4.利用ポイント按分金額 as 売上金額税込 " +
                        " , t4.クーポン値引額 as クーポン値引金額 " +
                        " , t4.クーポン割引額 as クーポン割引金額 " +
                        " , t4.利用クーポン１ as クーポン値引企画コード " +
                        " , t4.売価 - t4.クーポン割引額 - t4.内税額 +" +
                        "TRUNC (t4.利用ポイント按分金額 / (1 + t4.税率)) as 売上金額税抜 " +
                        " , t4.重点コード as 免税フラグ " +
                        "FROM " +
                        "( " +
                        "SELECT " +
                        "  t1.精算日付 as 精算日付 " +
                        ", t1.登録時刻 as 登録時刻 " +
                        ", t1.登録店番号 as 店番号 " +
                        ", t1.登録ターミナル番号 as 登録ターミナル番号 " +
                        ", DECODE(t1.登録企業コード,'1020',t1.取引連番,t1.登録取引番号) as 登録取引番号 " +
                        ", t2.アイテムシークエンス番号 as アイテムシークエンス番号 " +
                        ", DECODE(t1.登録企業コード,'1020',CONCAT(t1.会員番号  , 'E') ,CAST(t1.会員番号 AS TEXT)) as 会員番号 " +
                        ", NULLIF(TRIM(t2.単品コード),'') as 単品コード " +
                        ", NVL(NULLIF(TRIM(t5.商品名),''), '%s') as 商品名称 " +
                        ", t2.数量 as 数量 " +
                        ", t2.売価 as 売価 " +
                        ", DECODE(t1.登録企業コード, '1020', t2.小計値引按分金額, " +
                        "t2.サービス券按分額) as 利用ポイント按分金額 " +
                        ", t2.クーポン割引額 as クーポン値引額 " +
                        ", DECODE(t1.登録企業コード, '1020', 0, " +
                        "t2.小計値引按分金額 - t2.サービス券按分額)" +
                        "as クーポン割引額 " +
                        ", NVL(t2.利用クーポン１,0) as 利用クーポン１ " +
                        ", t2.内税額 as 内税額 " +
                        ", t2.税率/100 as 税率 " +
                        ", DECODE(t2.重点コード, '0000000001',0,9) as 重点コード " +
                        ", NVL(t3.連携用店番号,0) as 連携用店番号 " +
                        ", NVL(t5.単品コード桁数,13) as 単品コード桁数 " +
                        "FROM " +
                        "HSポイント明細取引情報%d t1 " +
                        "INNER JOIN " +
                        "HSポイント明細商品情報%d t2 " +
                        "ON t1.システム年月日 = t2.システム年月日 " +
                        "AND t1.サービス対応番号 = t2.サービス対応番号 " +
                        "AND t1.取引通番 = t2.取引通番 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 店番号, 連携用店番号 " +
                        "FROM " +
                        "(SELECT  店番号, 連携用店番号,ROW_NUMBER() " +
                        "OVER (PARTITION BY 店番号 ORDER BY 終了年月日 DESC ) " +
                        "G_row " +
                        "FROM PS店表示情報 where 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ") t3 " +
                        "ON t3.店番号=t1.登録店番号 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名 " +
                        "FROM " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名, ROW_NUMBER() " +
                        "OVER (PARTITION BY 単品コード ORDER BY 終了年月日 DESC, 開始年月日 DESC ) " +
                        "G_row " +
                        "FROM MS商品コード情報 " +
                        "WHERE 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ")t5 " +
                        "ON t5.単品コード = t2.単品コード " +
                        "WHERE t1.システム年月日 >= ? " +
                        "AND t1.システム年月日 <= ? " +
                        "AND t1.取引区分 IN (100, 102) " +
                        "AND t1.カード忘れフラグ IN (0, 1) " +
                        "AND t1.登録企業コード <>3000 " +
                        "AND CAST(CONCAT(t2.大分類コード , t2.中分類コード) AS NUMERIC ) <> 623 " +
                        "AND CAST(SUBSTR(LPAD(t2.単品コード,CAST(NVL(t5.単品コード桁数,13) AS INTEGER ),'0'),1,3) AS NUMERIC ) <> 200 " +
                        "UNION " +
                        "SELECT " +
                        "  t1.精算日付 as 精算日付 " +
                        ", t1.登録時刻 as 登録時刻 " +
                        ", t1.登録店番号 as 店番号 " +
                        ", t1.登録ターミナル番号 as 登録ターミナル番号 " +
                        ", DECODE(t1.登録企業コード,'1020',t1.取引連番,t1.登録取引番号) as 登録取引番号 " +
                        ", t2.アイテムシークエンス番号 as アイテムシークエンス番号 " +
                        ", DECODE(t1.登録企業コード,'1020',CONCAT(t1.会員番号 , 'E') ,CAST(t1.会員番号 AS TEXT)) as 会員番号 " +
                        ", NULLIF(TRIM(t2.単品コード),'') as 単品コード " +
                        ", NVL(NULLIF(TRIM(t5.商品名),''), '%s') as 商品名称 " +
                        ", t2.数量 as 数量 " +
                        ", t2.売価 as 売価 " +
                        ", DECODE(t1.登録企業コード, '1020', t2.小計値引按分金額, " +
                        "t2.サービス券按分額) as 利用ポイント按分金額 " +
                        ", t2.クーポン割引額 as クーポン値引額 " +
                        ", DECODE(t1.登録企業コード, '1020', 0, " +
                        "t2.小計値引按分金額 - t2.サービス券按分額)" +
                        "as クーポン割引額 " +
                        ", NVL(t2.利用クーポン１,0) as 利用クーポン１ " +
                        ", t2.内税額 as 内税額 " +
                        ", t2.税率/100 as 税率 " +
                        ", DECODE(t2.重点コード, '0000000001',0,9) as 重点コード " +
                        ", NVL(t3.連携用店番号,0) as 連携用店番号 " +
                        ", NVL(t5.単品コード桁数,13) as 単品コード桁数 " +
                        "FROM " +
                        "HSポイント明細取引情報%d t1 " +
                        "INNER JOIN " +
                        "HSポイント明細商品情報%d t2 " +
                        "ON t1.システム年月日 = t2.システム年月日 " +
                        "AND t1.サービス対応番号 = t2.サービス対応番号 " +
                        "AND t1.取引通番 = t2.取引通番 " +
                        "LEFT OUTER JOIN  " +
                        "(SELECT 店番号, 連携用店番号 " +
                        "FROM " +
                        "(SELECT 店番号, 連携用店番号,ROW_NUMBER() OVER " +
                        "(PARTITION BY 店番号 ORDER BY 終了年月日 DESC ) G_row " +
                        " FROM CMUSER.PS店表示情報 " +
                        " where 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ") t3 " +
                        "ON t3.店番号 = t1.登録店番号 " +
                        "LEFT OUTER JOIN " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名 " +
                        "FROM " +
                        "(SELECT 単品コード, 単品コード桁数, 商品名, ROW_NUMBER() " +
                        "OVER (PARTITION BY 単品コード ORDER BY 終了年月日 DESC, 開始年月日 DESC )" +
                        " G_row " +
                        "FROM CMUSER.MS商品コード情報 " +
                        "WHERE 開始年月日 <= ? " +
                        ") " +
                        "WHERE G_row = 1 " +
                        ")t5  " +
                        "ON t5.単品コード = t2.単品コード " +
                        "WHERE t1.後付け登録日付 >= ? " +
                        "AND t1.後付け登録日付 <= ? " +
                        "AND t1.取引区分 IN (100, 102) " +
                        "AND t1.カード忘れフラグ = 1 " +
                        "AND t1.登録企業コード <>3000 " +
                        "AND CAST(CONCAT(t2.大分類コード , t2.中分類コード) AS NUMERIC) <> 623 " +
                        "AND CAST(SUBSTR(LPAD(t2.単品コード,CAST(NVL(t5.単品コード桁数,13) AS INTEGER ),'0'),1,3) AS NUMERIC ) <> 200 " +
                        ") t4 " +
                        "ORDER BY " +
                        " 売上日 " +
                        ",店番コード " +
                        ",レジNo " +
                        ",レシートNo " +
                        ",行番号 "
                , DEF_NULL, h_daytime_yyyymm, h_daytime_yyyymm
                , DEF_NULL, h_daytime_yyyymm, h_daytime_yyyymm
        );

        /* 対象テーブル年月　格納 */
        sprintf(tbl_yyyymm, "%d", h_daytime_yyyymm);

        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTspmeB_set_Sql_Arg *** 抽出SQL [%s]\n", h_str_sql);
        C_DbgEnd("cmBTspmeB_set_Sql_Arg処理", 0, 0, 0);
        /*------------------------------------------------------------*/

        /*--- cmBTspmeB_set_Sql_Arg Bottom -------------------------------------------*/
    }

}
