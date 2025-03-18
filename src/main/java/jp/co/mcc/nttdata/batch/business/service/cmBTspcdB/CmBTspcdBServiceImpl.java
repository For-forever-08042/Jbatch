package jp.co.mcc.nttdata.batch.business.service.cmBTspcdB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;
import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/*******************************************************************************
 *   プログラム名   ： 番号変換マスタデータ連動（cmBTspcdB）
 *
 *   【処理概要】
 *     HSカード変更情報、PS店表示情報より番号変換マスタデータを抽出する
 *
 *   【引数説明】
 *     -sd 抽出対象開始日付:（任意）抽出対象範囲　開始日付（YYYYMMDD）
 *     -ed 抽出対象終了日付:（任意）抽出対象範囲　終了日付（YYYYMMDD）
 *     -DEBUG(-debug)      :（任意）デバッグモードでの実行
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
 *      30.00 : 2021/08/06  SSI.張：初版
 *      31.00 : 2021/11/19  SSI.張：EC会員EC取引重複対応
 *      31.01 : 2021/11/30  SSI.張：重複除去対応(出力ファイル名修正)
 *      32.00 : 2022/10/13  NDBS高橋：AdobeCampaign連携
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
 ******************************************************************************/
@Service
public class CmBTspcdBServiceImpl extends CmABfuncLServiceImpl implements CmBTspcdBService {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */


    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    ItemDto h_daytime_fm = new ItemDto(8 + 1);      /* 抽出期間開始(YYYYMMDD)       */
    ItemDto h_daytime_to = new ItemDto(8 + 1);      /* 抽出期間開始(YYYYMMDD)       */
    StringDto h_str_sql = new StringDto(4096 * 4);        /* 実行用SQL文字列              */
    StringDto bat_date_ymd_yes = new StringDto(8 + 1);  /* バッチシステム前日付         */
    int h_sys_date;             /* システム日付(整数)           */
    int h_sys_batdate_yes;      /* バッチシステム前日日付(整数) */

    /*  番号変換マスタデータ用変数 */
    StringDto h_gupon_id = new StringDto(16 + 1);        /* グーポン番号⇒CF会員番号        */
    ItemDto h_gid_before = new ItemDto();            /* 変換前グーポン番号⇒変換前CF会員番号  */
    ItemDto h_user_id = new ItemDto();               /* 顧客番号⇒グーポン番号           */
    ItemDto h_uid_before = new ItemDto();            /* 変換前顧客番号⇒変換前グーポン番号      */
    ItemDto h_service_type = new ItemDto();          /* サービス種別        */
    ItemDto h_del_flag = new ItemDto();              /* 削除フラグ          */
    ItemDto h_update_date = new ItemDto(14 + 1);     /* 最終更新日時日      */
    ItemDto h_use_start_date = new ItemDto(8 + 1);   /* 発行年月日          */
    ItemDto h_use_end_date = new ItemDto(8 + 1);     /* 終了年月日          */


    //    EXEC SQL END DECLARE SECTION;
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0; /* OFF                                */
    int DEF_ON = 1;    /* ON                                 */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    String DEF_YMD_1 = "-sd";            /* 抽出対象開始日付                   */
    String DEF_YMD_2 = "-ed";            /* 抽出対象終了日付                   */

    String C_PRGNAME = "番号変換マスタデータ連動";  /* APログ用機能名           */


    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int chk_arg_debug;                 /*  引数-debugチェック用              */
    StringDto arg_debug_Value = new StringDto(6 + 1);          /*  引数debug設定値                   */
    int chk_arg_sd;                    /*  引数-sdチェック用                 */
    StringDto arg_sd_Value = new StringDto(8 + 1);              /*  引数sd設定値                      */
    int chk_arg_ed;                    /*  引数-edチェック用                 */
    StringDto arg_ed_Value = new StringDto(8 + 1);              /*  引数ed設定値                      */

    StringDto out_file_dir = new StringDto(4094);             /*  ワークディレクトリ                */
    StringDto fl_name = new StringDto(4094);                 /*  ファイル名(フルパス)              */
    StringDto wk_out_buff = new StringDto(1024);             /* 出力情報格納用                     */

    /* 件数カウンタ */
    long input_data_cnt;                /* 処理対象件数件数                   */
    long ok_data_cnt;                   /* 正常処理データ件数                 */
    /* 現在日時 */
    StringDto sys_date = new StringDto(8 + 1);                 /* システム年月日                     */
    StringDto sys_time = new StringDto(6 + 1);                 /* システム時分秒                     */

    StringDto chg_format_buf = new StringDto(C_const_MsgMaxLen); /*  APログ用                      */
    int is_get;                        /* 引数両方取得チェック               */

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    FileStatusDto fp_out;                    /*  出力ファイルポインタ              */


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
        int rtn_cd;                     /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数結果ステータス                 */
        int arg_cnt;                    /* 引数チェック用カウンタ             */
        String env_wrk;                   /* 出力ファイルDIR                    */
        StringDto arg_Work1 = new StringDto(256);             /* Work Buffer1                       */
        int flg_sd;                     /* -sd引数の入力を確認するフラグ      */
        int flg_ed;                     /* -ed引数の入力を確認するフラグ      */

        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname",
                    rtn_cd, 0, 0, 0, 0);
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

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("*** main処理 ***");
            /*------------------------------------------------------------*/
        }
        /*  変数初期化 */
        rtn_cd = C_const_OK;                /*  関数戻り値                        */
        chk_arg_debug = DEF_OFF;
        chk_arg_sd = DEF_OFF;
        chk_arg_ed = DEF_OFF;
        memset(arg_debug_Value, 0x00, sizeof(arg_debug_Value));
        memset(arg_sd_Value, 0x00, sizeof(arg_sd_Value));
        memset(arg_ed_Value, 0x00, sizeof(arg_ed_Value));
        flg_sd = 0;                        /*  引数入力ありなしフラグ             */
        flg_ed = 0;
        is_get = 0;                        /*  引数両方取得ありなしフラグ         */
        h_sys_date = 0;                    /*  システム日付初期化                 */
        h_sys_batdate_yes = 0;             /*  バッチシステム前日日付初期化       */

        /*--------------------------------*/
        /* 現在日時の取得                 */
        /*--------------------------------*/
        memset(sys_date, 0x00, sizeof(sys_date));
        memset(sys_time, 0x00, sizeof(sys_time));
        rtn_cd = C_GetSysDateTime(sys_date, sys_time);
        h_sys_date = atoi(sys_date);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null,
                    "C_GetSysDateTime", rtn_cd, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTskbrB_CreateFile処理", C_const_APNG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
            /*------------------------------------------------------------*/
        }
        /*** 引数チェック ***/

        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                /*------------------------------------------------------------*/
            }
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 ||
                    strcmp(arg_Work1, DEF_debug) == 0) {
                continue;

            } else if (memcmp(arg_Work1, DEF_YMD_1, 3) == 0) {   /* -sdの場合      */
                rtn_cd = cmBTspcdB_Chk_Arg(arg_Work1);        /* パラメータCHK  */
                if (rtn_cd == C_const_OK) {
                    strncpy(arg_sd_Value, arg_Work1.arr.substring(3), 8);
                    flg_sd = 1;
                } else {
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else if (memcmp(arg_Work1, DEF_YMD_2, 3) == 0) {   /* -edの場合      */
                rtn_cd = cmBTspcdB_Chk_Arg(arg_Work1);        /* パラメータCHK  */
                if (rtn_cd == C_const_OK) {
                    strncpy(arg_ed_Value, arg_Work1.arr.substring(3), 8);
                    flg_ed = 1;
                } else {
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            } else {                                        /* 定義外パラメータ   */
                sprintf(chg_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit(C_const_APNG);
            }
        } /* FOR END */

        C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_sd_Value);

        if ((flg_sd == 1) && (flg_ed == 1)) {              /*  引数入力あり場合    */
            /* パラメータ妥当性チェック */
            rtn_cd = cmBTspcdB_Chk_Date();

            /* 抽出範囲を取得 */
            memset(h_daytime_fm, 0x00, sizeof(h_daytime_fm));
            memset(h_daytime_to, 0x00, sizeof(h_daytime_to));

            sprintf(h_daytime_fm, "%s", arg_sd_Value);
            sprintf(h_daytime_to, "%s", arg_ed_Value);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTspcdB_main *** 取得開始=[%s]\n", h_daytime_fm);
                C_DbgMsg("*** cmBTspcdB_main *** 取得終了=[%s]\n", h_daytime_to);
                /*--------------------------------------------------------------------*/
            }
        } else {
            is_get = 1;                            /*  引数入力なし場合     */
        }       /*if END */

        if (rtn_cd != C_const_OK) {
            rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了     */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("引数の値(-sd)    =[%s]\n", arg_sd_Value);
            C_DbgMsg("引数の値(-ed)    =[%s]\n", arg_ed_Value);
            /*-------------------------------------------------------------*/
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
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理  */
            return exit(C_const_APNG);
        }

        /* ファイルDIRセット */
        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n",
                    out_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
            /*------------------------------------------------------------*/
        }
        rtn_status.arr = C_const_OK;            /* 関数結果ステータス     */
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
            rtn_cd = C_EndBatDbg();        /* バッチデバッグ終了処理  */
            return exit(C_const_APNG);
        }

        /*  初期化    */
        rtn_status.arr = C_const_OK;
        memset(bat_date_ymd_yes, 0x00, sizeof(bat_date_ymd_yes));
        /* バッチ前日日付とる*/
        rtn_cd = C_GetBatDate(-1, bat_date_ymd_yes, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate(-1)", rtn_cd,
                    rtn_status, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日前日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日前日取得NG status= %d\n",
                        rtn_status);
                /*---------------------------------------------*/
            }
            rtn_cd = C_EndBatDbg();    /* バッチデバッグ終了処理  */
            return exit(C_const_APNG);
        }
        h_sys_batdate_yes = atoi(bat_date_ymd_yes);


        /*--------------------------------------*/
        /*  番号変換マスタデータファイルOPEN  */
        /*--------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 番号変換マスタデータファイル(%s)\n", "オープン");
            /*------------------------------------------------------------*/
        }
        rtn_cd = cmBTspcdB_OpenFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** main *** 番号変換マスタデータファイルオープン NG" +
                        " rtn=[%d]\n", rtn_cd);
                /*---------------------------------------------*/
            }
            APLOG_WT("912", 0, null,
                    "番号変換マスタデータファイル作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理  */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*  番号変換マスタデータ連動主処理             */
        rtn_cd = cmBTspcdB_main();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTspcdB_main NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null,
                    "番号変換マスタデータファイル作成処理に失敗しました",
                    0, 0, 0, 0, 0);
            fclose(fp_out);               /* File Close                 */
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理     */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        fclose(fp_out);                       /* File Close             */

        /* 処理件数を出力 */
        APLOG_WT("106", 0, null, "HSカード変更情報,MSカード情報",
                input_data_cnt, ok_data_cnt,
                0, 0, 0);

        if (ok_data_cnt != 0) {
            /* 出力データ件数が0以上の場合はファイル名を出力 */
            APLOG_WT("105", 0, null, fl_name, ok_data_cnt,
                    0, 0, 0, 0);
        } else {
            /* 出力データ件数が0の場合はファイルを削除 */
            rtn_cd = cmBTspcdB_RemoveFile();
            if (rtn_cd != C_const_OK) {

                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("cmBTspcdB_RemoveFile処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return functionCode(C_const_NG);
            }
        }

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease(sqlca);

        return exit(C_const_APOK);
        /*--- main Bottom ------------------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*        public int  cmBTspcdB_main()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     番号変換マスタデータ連動主処理                                         */
    /*     番号変換マスタデータファイルを作成する。                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTspcdB_main() {
        int rtn_cd;                      /* 関数戻り値                        */

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_main処理");
            /*--------------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;
        input_data_cnt = 0;
        ok_data_cnt = 0;

        /* 番号変換マスタデータ連動対象データ抽出１ */
        rtn_cd = cmBTspcdB_ChangeID();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTspcdB_ChangeID NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTspcdB_main処理", 0, 0, 0);
            /*--------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTspcdB_main Bottom --------------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              番号変換マスタデータファイルを追加書き込みモードで            */
    /*              オープンする。                                                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTspcdB_OpenFile() {
        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_OpenFile処理");
            /*--------------------------------------------------------------------*/
        }

        /* ファイル名作成、オープン */
        memset(fl_name, 0x00, sizeof(fl_name));
        sprintf(fl_name, "%s/tmp_%s%s_GPNCHG.csv",
                out_file_dir, sys_date, sys_time);

        if ((fp_out = fopen(fl_name.arr, SystemConstant.UTF8, FileOpenType.a)).fd == C_const_NG) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTspcdB_OpenFile *** 番号変換マスタ" +
                        "データファイルオープンERR%s\n", "");
                /*--------------------------------------------------------------------*/
            }
            memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
            sprintf(chg_format_buf, "fopen（%s）", fl_name);
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_OpenFile *** 番号変換マスタデータファイル%s\n",
                    fl_name);
            C_DbgEnd("cmBTspcdB_OpenFile処理", 0, 0, 0);
            /*--------------------------------------------------------------------*/
        }
        return C_const_OK;
        /*--- cmBTspcdB_OpenFile Bottom ----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_ChangeID                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*        public int  cmBTspcdB_ChangeID()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*             連動対象データ抽出                                             */
    /*  HSカード変更情報、MSカード情報より番号変換マスタデータを抽出する          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    なし                                                                    */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTspcdB_ChangeID() {
        int rtn_cd;                      /* 関数戻り値                        */
        StringDto wk_sql = new StringDto(4096 * 4);                /* 動的SQLバッファ                   */
        int wk_st_date;                  /* ワーク発行年月日                  */
        int wk_ed_date;                  /* ワーク終了年月日                  */


        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_ChangeID処理");
            /*------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(h_str_sql, 0x00, sizeof(h_str_sql));

        /* 2023/02/17 MCCM初版 MOD START */
        if (is_get == 1) {
            /* 抽出範囲年月日が指定なし */
            /* ＳＱＬ文をセットする */
            sprintf(wk_sql,
                    "SELECT  "
                            + "    グーポン番号, "
                            + "    変換前グーポン番号, "
                            + "    顧客番号, "
                            + "    変換前顧客番号, "
                            + "    サービス種別, "
                            + "    削除フラグ, "
                            + "    最終更新日時, "
                            + "    発行年月日, "
                            + "    終了年月日 "
                            + "FROM "
                            + "( "
                            + "    SELECT  "                                                                                                               /* ①入会情報１ START                     */
                            + "        DECODE(T1.サービス種別,'2', CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        0 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス = 0 "*/
                            + "    WHERE T1.カードステータス IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.顧客番号<>0 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND 発行年月日 =  ? "
                            + "    AND NOT EXISTS(SELECT * "
                            + "    FROM CMUSER.HSカード変更情報 A "
                            + "    WHERE A.企業コード = T1.企業コード "
                            + "    AND A.会員番号 = T1.会員番号 "
                            + "    AND A.顧客番号 = T1.顧客番号 "
                            + "    AND A.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + "    AND TO_CHAR(A.最終更新日時, 'YYYYMMDD') =  ?) "                                                                        /* ①入会情報１ END                       */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ②入会情報２ START                     */
                            + "        DECODE(T1.サービス種別,'2',CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        0 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス = 0 "*/
                            + "    WHERE T1.カードステータス IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.顧客番号<>0 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND TO_CHAR(T1.最終更新日時, 'YYYYMMDD') =  ? "
                            + "    AND NOT EXISTS ( SELECT * "
                            + "    FROM CMUSER.HSカード変更情報 A "
                            + "    WHERE A.企業コード = T1.企業コード "
                            + "    AND A.会員番号 = T1.会員番号 "
                            + "    AND A.顧客番号 = T1.顧客番号 "
                            + "    AND (TO_CHAR(A.最終更新日時, 'YYYYMMDD') =  ? "
                            + "        OR A.統合切替解除年月日 =  ?)) "                                                                                   /* ②入会情報２ END                       */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ③退会情報 START                       */
                            + "        DECODE(T1.サービス種別,'2',CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        1 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス <> 0 "*/
                            + "    WHERE T1.カードステータス NOT IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.顧客番号<>0 "
                            + "    AND T1.理由コード<> 2021 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND TO_CHAR(T1.最終更新日時, 'YYYYMMDD') =  ? "                                                                        /* ③退会情報 END                         */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ④カード切替・統合情報 START           */
                            + "    DECODE(T1.企業コード, '1020', CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "    T1.旧会員番号 AS 変換前グーポン番号, "
                            + "    T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "    T1.旧ＧＯＯＰＯＮ番号 AS 変換前顧客番号, "
                            + "    DECODE(T1.企業コード,'1020',2,'1040',3,1) AS サービス種別, "
                            + "    0 AS 削除フラグ, "
                            + "    TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "    NVL(T2.発行年月日 ,0 ) AS 発行年月日, "
                            + "    NVL(T2.終了年月日 ,0 ) AS 終了年月日 "
                            + " FROM PS会員番号体系 T3,HSカード変更情報 T1 "
                            + " INNER JOIN MSカード情報 T2   ON T1.会員番号 = T2.会員番号 AND T1.顧客番号 = T2.顧客番号 "
                            + " WHERE T1.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T1.旧企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND NVL(T1.統合切替解除年月日,0) = 0 "
                            + " AND  T3.カード種別 IN (504, 505, 998) "
                            + " AND  (   (T1.企業コード   = T3.企業コード AND T1.会員番号   >= T3.会員番号開始 AND T1.会員番号 <= T3.会員番号終了 ) "
                            + "       OR (T1.旧企業コード = T3.企業コード AND T1.旧会員番号 >= T3.会員番号開始 AND T1.旧会員番号 <= T3.会員番号終了) ) "
                            + " AND T1.最終更新日 =  ? "                                                                                                  /* ④カード切替・統合情報 END             */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ⑤カード切替解除・統合解除情報 START   */
                            + "    DECODE(T1.企業コード, '1020', CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "    T1.旧会員番号 AS 変換前グーポン番号, "
                            + "    T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "    T1.旧ＧＯＯＰＯＮ番号 AS 変換前顧客番号, "
                            + "    DECODE(T1.企業コード,'1020',2,'1040',3,1) AS サービス種別, "
                            + "    1 AS 削除フラグ, "
                            + "    CONCAT(TO_CHAR(T1.統合切替解除年月日, 'FM00000000') , TO_CHAR(T1.統合切替解除時刻, 'FM000000')) AS 最終更新日時, "
                            + "    NVL(T2.発行年月日 ,0 )  AS 発行年月日, "
                            + "    NVL(T2.終了年月日 ,0 )  AS 終了年月日 "
                            + " FROM PS会員番号体系 T3,HSカード変更情報 T1 "
                            + " INNER JOIN MSカード情報 T2 ON T1.会員番号 = T2.会員番号 AND T1.顧客番号 = T2.顧客番号 "
                            + " WHERE T1.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T1.旧企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T3.カード種別 IN (504, 505, 998) "
                            + " AND  (   (T1.企業コード   = T3.企業コード AND T1.会員番号   >= T3.会員番号開始 AND T1.会員番号 <= T3.会員番号終了 ) "
                            + "       OR (T1.旧企業コード = T3.企業コード AND T1.旧会員番号 >= T3.会員番号開始 AND T1.旧会員番号 <= T3.会員番号終了) ) "
                            + " AND T1.統合切替解除年月日 =  ? "                                                                                          /* ⑤カード切替解除・統合解除情報 END     */
                            + ") "
                            + "ORDER BY 最終更新日時 "
            );
        } else {
            /* 抽出範囲年月日が指定あり */
            /* ＳＱＬ文をセットする */
            sprintf(wk_sql,
                    "SELECT  "
                            + "    グーポン番号, "
                            + "    変換前グーポン番号, "
                            + "    顧客番号, "
                            + "    変換前顧客番号, "
                            + "    サービス種別, "
                            + "    削除フラグ, "
                            + "    最終更新日時, "
                            + "    発行年月日, "
                            + "    終了年月日 "
                            + "FROM "
                            + "( "
                            + "    SELECT  "                                                                                                               /* ①入会情報１ START                     */
                            + "        DECODE(T1.サービス種別,'2', CONCAT(T1.会員番号,'E'), CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        0 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス = 0 "*/
                            + "    WHERE T1.カードステータス IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.顧客番号<>0 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND 発行年月日 BETWEEN  ? AND  ? "
                            + "    AND NOT EXISTS(SELECT * "
                            + "    FROM CMUSER.HSカード変更情報 A "
                            + "    WHERE A.企業コード = T1.企業コード "
                            + "    AND A.会員番号 = T1.会員番号 "
                            + "    AND A.顧客番号 = T1.顧客番号 "
                            + "    AND A.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + "    AND TO_CHAR(A.最終更新日時, 'YYYYMMDD') BETWEEN  ? AND  ?) "                                                          /* ①入会情報１ END                       */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ②入会情報２ START                     */
                            + "        DECODE(T1.サービス種別,'2',CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        0 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス = 0 "*/
                            + "    WHERE T1.カードステータス IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.顧客番号<>0 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND TO_CHAR(T1.最終更新日時, 'YYYYMMDD') BETWEEN  ? AND  ? "
                            + "    AND NOT EXISTS ( SELECT * "
                            + "    FROM CMUSER.HSカード変更情報 A "
                            + "    WHERE A.企業コード = T1.企業コード "
                            + "    AND A.会員番号 = T1.会員番号 "
                            + "    AND A.顧客番号 = T1.顧客番号 "
                            + "    AND (TO_CHAR(A.最終更新日時, 'YYYYMMDD') BETWEEN  ? AND  ? "
                            + "        OR A.統合切替解除年月日 BETWEEN  ? AND  ?)) "                                                                     /* ②入会情報２ END                       */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ③退会情報 START                       */
                            + "        DECODE(T1.サービス種別,'2',CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "        0 AS 変換前グーポン番号, "
                            + "        T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "        0 AS 変換前顧客番号, "
                            + "        T1.サービス種別 AS サービス種別, "
                            + "        1 AS 削除フラグ, "
                            + "        TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "        NVL(T1.発行年月日 ,0 ) AS 発行年月日, "
                            + "        NVL(T1.終了年月日 ,0 ) AS 終了年月日 "
                            + "    FROM MSカード情報 T1,PS会員番号体系 T2 "
                            /* 2023/02/17 MCCM初版 MOD START */
                            /*                "    WHERE T1.カードステータス <> 0 "*/
                            + "    WHERE T1.カードステータス NOT IN (0, 1) "
                            /* 2023/02/17 MCCM初版 MOD END */
                            + "    AND T1.ＧＯＯＰＯＮ番号<>0 "
                            + "    AND T1.理由コード<> 2021 "
                            + "    AND T2.カード種別 IN (504, 505, 998) "
                            + "    AND T1.サービス種別 = T2.サービス種別 "
                            + "    AND T1.会員番号 >= T2.会員番号開始 "
                            + "    AND T1.会員番号 <= T2.会員番号終了 "
                            + "    AND TO_CHAR(T1.最終更新日時, 'YYYYMMDD') BETWEEN  ? AND  ? "                                                          /* ③退会情報 END                         */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ④カード切替・統合情報 START           */
                            + "    DECODE(T1.企業コード, '1020', CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "    T1.旧会員番号 AS 変換前グーポン番号, "
                            + "    T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "    T1.旧ＧＯＯＰＯＮ番号 AS 変換前顧客番号, "
                            + "    DECODE(T1.企業コード,'1020',2,'1040',3,1) AS サービス種別, "
                            + "    0 AS 削除フラグ, "
                            + "    TO_CHAR(T1.最終更新日時, 'YYYYMMDDHH24MISS') AS 最終更新日時, "
                            + "    NVL(T2.発行年月日 ,0 ) AS 発行年月日, "
                            + "    NVL(T2.終了年月日 ,0 ) AS 終了年月日 "
                            + " FROM PS会員番号体系 T3,HSカード変更情報 T1 "
                            + " INNER JOIN MSカード情報 T2   ON T1.会員番号 = T2.会員番号 AND T1.顧客番号 = T2.顧客番号 "
                            + " WHERE T1.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T1.旧企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND NVL(T1.統合切替解除年月日,0) = 0 "
                            + " AND  T3.カード種別 IN (504, 505, 998) "
                            + " AND  (   (T1.企業コード   = T3.企業コード AND T1.会員番号   >= T3.会員番号開始 AND T1.会員番号 <= T3.会員番号終了 ) "
                            + "       OR (T1.旧企業コード = T3.企業コード AND T1.旧会員番号 >= T3.会員番号開始 AND T1.旧会員番号 <= T3.会員番号終了) ) "
                            + " AND T1.最終更新日 BETWEEN  ? AND  ? "                                                                                    /* ④カード切替・統合情報 END             */
                            + "    UNION "
                            + "    SELECT "                                                                                                                /* ⑤カード切替解除・統合解除情報 START   */
                            + "    DECODE(T1.企業コード, '1020', CONCAT(T1.会員番号,'E'),CAST(T1.会員番号 AS TEXT)) AS グーポン番号, "
                            + "    T1.旧会員番号 AS 変換前グーポン番号, "
                            + "    T1.ＧＯＯＰＯＮ番号 AS 顧客番号, "
                            + "    T1.旧ＧＯＯＰＯＮ番号 AS 変換前顧客番号, "
                            + "    DECODE(T1.企業コード,'1020',2,'1040',3,1) AS サービス種別, "
                            + "    1 AS 削除フラグ, "
                            + "    CONCAT(TO_CHAR(T1.統合切替解除年月日, 'FM00000000') , TO_CHAR(T1.統合切替解除時刻, 'FM000000')) AS 最終更新日時, "
                            + "    NVL(T2.発行年月日 ,0 )  AS 発行年月日, "
                            + "    NVL(T2.終了年月日 ,0 )  AS 終了年月日 "
                            + " FROM PS会員番号体系 T3,HSカード変更情報 T1 "
                            + " INNER JOIN MSカード情報 T2 ON T1.会員番号 = T2.会員番号 AND T1.顧客番号 = T2.顧客番号 "
                            + " WHERE T1.企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T1.旧企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060) "
                            + " AND T3.カード種別 IN (504, 505, 998) "
                            + " AND  (   (T1.企業コード   = T3.企業コード AND T1.会員番号   >= T3.会員番号開始 AND T1.会員番号 <= T3.会員番号終了 ) "
                            + "       OR (T1.旧企業コード = T3.企業コード AND T1.旧会員番号 >= T3.会員番号開始 AND T1.旧会員番号 <= T3.会員番号終了) ) "
                            + " AND T1.統合切替解除年月日 BETWEEN  ? AND  ? "                                                                            /* ⑤カード切替解除・統合解除情報 END     */
                            + ") "
                            + "ORDER BY 最終更新日時 "
            );
        }
        /* 2023/02/17 MCCM初版 MOD END */
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_ChangeID *** 抽出SQL [%s]\n", wk_sql);
            /*------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(h_str_sql, wk_sql);

        SqlstmDto sqlca = sqlcaManager.get("SEL_SPPD");
        sqlca.sql = h_str_sql;
        sqlca.prepare();
        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE SEL_SPPD from:
//        h_str_sql;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "HSカード変更情報,MSカード情報 PREPARE SEL_SPPD", 0, 0, 0, 0);
            return C_const_NG;
        }

        sqlca.declare();
        /* カーソル宣言 */
//        EXEC SQL DECLARE CUR_SPPD CURSOR FOR SEL_SPPD;

        /* カーソルオープン */
        if (is_get == 1) {                                     /* 引数指定なし場合 */
            sqlca.open(h_sys_batdate_yes, h_sys_batdate_yes,
                    h_sys_batdate_yes, h_sys_batdate_yes,
                    h_sys_batdate_yes, h_sys_batdate_yes,
                    h_sys_batdate_yes, h_sys_batdate_yes);
//            EXEC SQL OPEN CUR_SPPD USING:
//            h_sys_batdate_yes, :h_sys_batdate_yes,
//                                    :h_sys_batdate_yes, :h_sys_batdate_yes,
//                                    :h_sys_batdate_yes, :h_sys_batdate_yes,
//                                    :h_sys_batdate_yes, :h_sys_batdate_yes;

        } else {                                                /* 引数指定あり場合 */
            sqlca.open(h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to,
                    h_daytime_fm, h_daytime_to);
//            EXEC SQL OPEN CUR_SPPD USING:
//            h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to,
//                                    :h_daytime_fm, :h_daytime_to;
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTspcdB_ChangeID *** 検索NG %.70s\n",
                        sqlca.errorMsg);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode,
                    "HSカード変更情報,MSカード情報 OPEN CUR_SPPD", 0, 0, 0, 0);
            return C_const_NG;
        }

        /*-------------------------*/
        /* 連動対象データを取得    */
        /*-------------------------*/
        while (true) {
            /* 初期化 */
            memset(h_gupon_id, 0x00, sizeof(h_gupon_id));
            /* グーポン番号        */
            h_gid_before.arr = 0;            /* 変換前グーポン番号  */
            h_user_id.arr = 0;               /* 顧客番号            */
            h_uid_before.arr = 0;            /* 変換前顧客番号      */
            h_service_type.arr = 0;          /* サービス種別        */
            h_del_flag.arr = 0;              /* 削除フラグ          */
            memset(h_update_date, 0x00, sizeof(h_update_date));
            /* 最終更新日時日      */
            memset(h_use_start_date, 0x00, sizeof(h_use_start_date));
            /* 発行年月日          */
            memset(h_use_end_date, 0x00, sizeof(h_use_end_date));
            /* 終了年月日          */

            /* カーソルフェッチ */
            sqlca.fetchInto(h_gupon_id,   /* グーポン番号        */
                    h_gid_before,   /* 変換前グーポン番号  */
                    h_user_id,   /* 顧客番号            */
                    h_uid_before,   /* 変換前顧客番号      */
                    h_service_type,   /* サービス種別        */
                    h_del_flag,   /* 削除フラグ          */
                    h_update_date,   /* 最終更新日時日      */
                    h_use_start_date,   /* 発行年月日          */
                    h_use_end_date);   /* 終了年月日          */
//            EXEC SQL FETCH CUR_SPPD
//            INTO:
//                      h_gupon_id,              /* グーポン番号        */
//                     :h_gid_before,            /* 変換前グーポン番号  */
//                     :h_user_id,               /* 顧客番号            */
//                     :h_uid_before,            /* 変換前顧客番号      */
//                     :h_service_type,          /* サービス種別        */
//                     :h_del_flag,              /* 削除フラグ          */
//                     :h_update_date,           /* 最終更新日時日      */
//                     :h_use_start_date,        /* 発行年月日          */
//                     :h_use_end_date;          /* 終了年月日          */


            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                /* データ無しの場合、ブレーク */
                break;
            } else if (sqlca.sqlcode != C_const_Ora_OK) {
                /* データ無し以外エラーの場合、処理を異常終了する */
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                if (is_get == 0) {                             /* 引数指定あり場合 */
                    sprintf(chg_format_buf, "抽出期間=[%s]～[%s]",
                            h_daytime_fm, h_daytime_to);
                } else {                                        /* 引数指定なし場合 */
                    sprintf(chg_format_buf, "抽出日付=[%s]", bat_date_ymd_yes);
                }
                APLOG_WT("904", 0, null, "FETCH(CUR_SPPD)",
                        sqlca.sqlcode, "HSカード変更情報,MSカード情報",
                        chg_format_buf, 0, 0);
                /* カーソルクローズ */
//                EXEC SQL CLOSE CUR_SPPD;
                sqlcaManager.close(sqlca);
                return C_const_NG;
            }

            /* 処理対象件数カウントアップ */
            input_data_cnt++;

            /* 会員番号スペース削除      */
            BT_Rtrim(h_gupon_id, strlen(h_gupon_id));

            /*------------------------------------------*/
            /* 番号変換マスタデータファイル作成       */
            /*------------------------------------------*/

            /* 発行年月日、終了年月日　編集 */
            wk_st_date = 0;
            wk_ed_date = 0;

            wk_st_date = atoi(h_use_start_date);
            wk_ed_date = atoi(h_use_end_date);

            /* 0 の場合nullに変換 */
            if (wk_st_date == 0) {
                memset(h_use_start_date, 0x00, sizeof(h_use_start_date));
            }

            if (wk_ed_date == 0) {
                memset(h_use_end_date, 0x00, sizeof(h_use_end_date));
            }
            sprintf(wk_out_buff,
                    "%s,%d,%d,%d,%d,%s,%s,%d\n",
                    h_gupon_id,              /* グーポン番号        */
                    h_gid_before,            /* 変換前グーポン番号  */
                    h_user_id,               /* 顧客番号            */
                    h_uid_before,            /* 変換前顧客番号      */
                    h_service_type,          /* サービス種別        */
                    h_use_start_date,        /* 発行年月日          */
                    h_use_end_date,          /* 終了年月日          */
                    h_del_flag               /* 削除フラグ          */
            );

            fwrite(wk_out_buff, strlen(wk_out_buff), 1, fp_out);
            rtn_cd = ferror(fp_out);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*-------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTspcdB_ChangeID *** fwrite NG rtn=[%d]\n",
                            ferror(fp_out));
                    /*-------------------------------------------------------------*/
                }
                memset(chg_format_buf, 0x00, sizeof(chg_format_buf));
                sprintf(chg_format_buf, "fwrite（%s）", fl_name);
                APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
//                EXEC SQL CLOSE CUR_SPPD;           /* カーソルクローズ        */
                sqlcaManager.close(sqlca);
                return C_const_NG;
            }
            fflush(fp_out);
            /* 正常件数カウントアップ */
            ok_data_cnt++;

            /* 10000件ごとにログ出力 */
            if (input_data_cnt % 10000 == 0) {
                APLOG_WT("106", 0, null, "HSカード変更情報,MSカード情報",
                        input_data_cnt,
                        ok_data_cnt, 0, 0, 0);
            }
        } /* END for loop */

//        EXEC SQL CLOSE CUR_SPPD;           /* カーソルクローズ        */
        sqlcaManager.close(sqlca);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTspcdB_ChangeID処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTspcdB_ChangeID Bottom --------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_Chk_Arg( char *Arg_in )                             */
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
    public int cmBTspcdB_Chk_Arg(StringDto Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_Chk_Arg処理");
            C_DbgMsg("*** cmBTspcdB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        int rtn_cd;                         /* 関数戻り値                     */
        int loop_cnt;                       /* ループカウンタ                 */
        StringDto wk_ymd = new StringDto(9);                      /* 処理日付変換用                 */

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        if (memcmp(Arg_in, DEF_YMD_1, 3) == 0) {        /* -sd処理対象日数CHK     */
            if (chk_arg_sd != DEF_OFF) {
                sprintf(chg_format_buf, "-sd 引数の値が不正です(重複)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_sd = DEF_ON;

            if (strlen(Arg_in) < 11) {                /* 桁数チェック           */
                sprintf(chg_format_buf, "-sd 引数の値が不正です(桁数)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 8);
            for (loop_cnt = 0; loop_cnt < 8; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-sd 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }
            }

        } else if (memcmp(Arg_in, DEF_YMD_2, 3) == 0) { /* -ed処理対象日数CHK     */
            if (chk_arg_ed != DEF_OFF) {
                sprintf(chg_format_buf, "-ed 引数の値が不正です(重複)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            chk_arg_ed = DEF_ON;

            if (strlen(Arg_in) < 11) {                /* 桁数チェック           */
                sprintf(chg_format_buf, "-ed 引数の値が不正です(桁数)（%s）",
                        Arg_in);
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 8);
            for (loop_cnt = 0; loop_cnt < 8; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-ed 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }
            }

            memset(wk_ymd, 0x00, sizeof(wk_ymd));    /* 数字チェック            */
            strncpy(wk_ymd, Arg_in.arr.substring(3), 6);
            for (loop_cnt = 0; loop_cnt < 6; loop_cnt++) {
                rtn_cd = isdigit(wk_ymd.charAt(loop_cnt));
                if (rtn_cd == 0) {
                    sprintf(chg_format_buf, "-t2 引数の値が不正です(数値)（%s）",
                            Arg_in);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTspcdB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTspcdB_Chk_Arg Bottom -----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_Chk_Date                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_Chk_Date( )                                         */
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
    public int cmBTspcdB_Chk_Date() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_Chk_Date処理");
            /*---------------------------------------------------------------------*/
        }
        int rtn_cd;                 /* 関数戻り値                             */


        /* 抽出期間開始日 */
        IntegerDto i_fm_yyyy = new IntegerDto();                      /* 抽出期間開始日（YYYY）         */
        IntegerDto i_fm_mm = new IntegerDto();                           /* 抽出期間開始日（MM）           */
        IntegerDto i_fm_dd = new IntegerDto();                          /* 抽出期間開始日（DD）           */
        IntegerDto i_fm_yyyymm = new IntegerDto();                      /* 抽出期間開始日（YYYYMM）       */
        /* 抽出期間終了日 */
        IntegerDto i_to_yyyy = new IntegerDto();                         /* 抽出期間終了日（YYYY）         */
        IntegerDto i_to_mm = new IntegerDto();                           /* 抽出期間終了日（MM）           */
        IntegerDto i_to_dd = new IntegerDto();                          /* 抽出期間終了日（DD）           */
        IntegerDto i_to_yyyymm = new IntegerDto();                       /* 抽出期間終了日（YYYYMM）       */

        /* 初期化 */
        memset(chg_format_buf, 0x00, sizeof(chg_format_buf));

        /* 抽出期間開始日 */
        cmBTspcdB_CutYMD(arg_sd_Value,
                i_fm_yyyy, i_fm_mm, i_fm_dd, i_fm_yyyymm);
        /* 抽出期間終了日 */
        cmBTspcdB_CutYMD(arg_ed_Value,
                i_to_yyyy, i_to_mm, i_to_dd, i_to_yyyymm);

        /*********************/
        /* 妥当性チェック    */
        /*********************/
        /* 抽出期間開始日 */
        rtn_cd = cmBTspcdB_checkYMD(i_fm_yyyy, i_fm_mm, i_fm_dd);
        if (rtn_cd != 1) {
            sprintf(chg_format_buf, "-sd 引数の値が不正です(日付型)（%s）",
                    arg_sd_Value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* 抽出期間終了日 */
        rtn_cd = cmBTspcdB_checkYMD(i_to_yyyy, i_to_mm, i_to_dd);
        if (rtn_cd != 1) {
            sprintf(chg_format_buf, "-ed 引数の値が不正です(日付型)（%s）",
                    arg_ed_Value);
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTspcdB_Chk_Date処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*--- cmBTspcdB_Chk_Date Bottom ----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_CutYMD                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_CutYMD( char *date_buf, int *year, int *month,      */
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
    public void cmBTspcdB_CutYMD(StringDto date_buf, IntegerDto year, IntegerDto month, IntegerDto day,
                                 IntegerDto yearmonth) {
        StringDto wk_buf = new StringDto(256);                    /* 編集バッファ                   */

        /* 年 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf, 4);
        year.arr = atoi(wk_buf);

        /* 月 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.arr.substring(4), 2);
        month.arr = atoi(wk_buf);

        /* 日 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf.substring(6), 2);
        day.arr = atoi(wk_buf);

        /* 年月 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, date_buf, 6);
        yearmonth.arr = atoi(wk_buf);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_CutYMD *** 年月日 [%s]\n", date_buf);
            C_DbgMsg("*** cmBTspcdB_CutYMD *** 年 [%d]\n", year);
            C_DbgMsg("*** cmBTspcdB_CutYMD *** 月 [%d]\n", month);
            C_DbgMsg("*** cmBTspcdB_CutYMD *** 日 [%d]\n", day);
            C_DbgMsg("*** cmBTspcdB_CutYMD *** 年月 [%d]\n", yearmonth);
            /*------------------------------------------------------------*/
        }
        return;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_CutHMS                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_CutHMS( char *time_buf, int *hour, int *minute,     */
    /*                                int *second )                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              指定された時刻を時、分、秒に分解する。                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*    IN  ： char *time_buf   対象時刻                                        */
    /*    OUT ： int  *hour       対象時                                          */
    /*        ： int  *minute     対象分                                          */
    /*        ： int  *second     対象秒                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              なし                                                          */
    /*                                                                            */

    /******************************************************************************/
    public void cmBTspcdB_CutHMS(StringDto time_buf, IntegerDto hour, IntegerDto minute, IntegerDto second) {
        StringDto wk_buf = new StringDto(256);                    /* 編集バッファ                   */

        /* 時 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, time_buf, 2);
        hour.arr = atoi(wk_buf);

        /* 分 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, time_buf.substring(2), 2);
        minute.arr = atoi(wk_buf);

        /* 秒 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        memcpy(wk_buf, time_buf.substring(4), 2);
        second.arr = atoi(wk_buf);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_CutHMS *** 時刻 [%s]\n", time_buf);
            C_DbgMsg("*** cmBTspcdB_CutHMS *** 時 [%d]\n", hour);
            C_DbgMsg("*** cmBTspcdB_CutHMS *** 分 [%d]\n", minute);
            C_DbgMsg("*** cmBTspcdB_CutHMS *** 秒 [%d]\n", second);
            /*------------------------------------------------------------*/
        }
        return;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_checkYMD                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_checkYMD( int year, int month, int day )            */
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
    public int cmBTspcdB_checkYMD(IntegerDto year, IntegerDto month, IntegerDto day) {
        int isCheck;
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_checkYMD処理");
            C_DbgMsg("*** cmBTspcdB_checkYMD *** 年 [%d]\n", year);
            C_DbgMsg("*** cmBTspcdB_checkYMD *** 月 [%d]\n", month);
            C_DbgMsg("*** cmBTspcdB_checkYMD *** 日 [%d]\n", day);
            /*------------------------------------------------------------*/
        }

        if (year.arr < 1900) {
            isCheck = 0;
        } else {
            isCheck = ((month.arr >= 1) && (month.arr <= 12) &&
                    (day.arr >= 1) &&
                    (day.arr <= cmBTspcdB_daysInMonth(year.arr, month.arr))) ? 1 : 0;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_checkYMD *** 日付妥当性チェック [%d]\n", isCheck);
            C_DbgEnd("cmBTspcdB_checkYMD処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return isCheck;
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_daysInMonth                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_daysInMonth()                                       */
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
    /*              0   ： 閏年ではない                                           */
    /*              1   ： 閏年である                                             */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTspcdB_daysInMonth(int year, int month) {
        int lastDayInMonth;
        int nDaysInMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_daysInMonth処理");
            /*------------------------------------------------------------*/
        }

        if ((month < 1) || (12 < month)) {
            lastDayInMonth = 0;
        } else if ((2 == month) && cmBTspcdB_isLeapYear(year) == 1) {
            lastDayInMonth = 29;
        } else {
            lastDayInMonth = nDaysInMonth[month - 1];
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_daysInMonth *** 月末日取得 [%d]\n", lastDayInMonth);
            C_DbgEnd("cmBTspcdB_daysInMonth処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return lastDayInMonth;
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_isLeapYear                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_isLeapYear()                                        */
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
    public int cmBTspcdB_isLeapYear(int year) {
        int isLeapYear;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_isLeapYear処理");
            /*------------------------------------------------------------*/
        }

        isLeapYear = (0 == (year % 400)) ||
                ((0 != (year % 100)) && (0 == (year % 4))) ? 1 : 0;

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** cmBTspcdB_isLeapYear *** 閏年判定 [%d]\n", isLeapYear);
            C_DbgEnd("isLeapYear処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return isLeapYear;
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTspcdB_RemoveFile                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTspcdB_RemoveFile()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル削除処理                                              */
    /*              ファイルを削除する。                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTspcdB_RemoveFile() {
        int rtn_cd;
        StringDto fp_name = new StringDto(6096);                      /** ファイルパス名      **/

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTspcdB_RemoveFile処理");
            /*------------------------------------------------------------*/
        }

        /* ファイルパス名作成 */
        sprintf(fp_name, "%s", fl_name);

        rtn_cd = remove(fp_name.arr);
        if (rtn_cd != C_const_OK) {
            /* APLOG(903) */
            sprintf(chg_format_buf, "remove(%s)", fp_name);
            APLOG_WT("903", 0, null, chg_format_buf, rtn_cd,
                    0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTspcdB_RemoveFile *** ファイル削除ERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTspcdB_RemoveFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

}
