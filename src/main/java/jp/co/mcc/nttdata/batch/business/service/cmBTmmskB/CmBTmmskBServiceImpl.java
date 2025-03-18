package jp.co.mcc.nttdata.batch.business.service.cmBTmmskB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTmmskB.dto.CmBTmmskBDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： 月別会員別購買金額連携作成（cmBTmmskB.pc）
 *
 *   【処理概要】
 *     月別会員別購買金額を集計しCSVファイルを作成する。
 *
 *   【引数説明】
 *     -DEBUG(-debug)   :（任意）デバッグモードでの実行
 *
 * 2023/01/12 H016CF会員除外条件追加 ADD START *
 *     -cf : （任意）CF会員除外実行
 * 2023/01/12 H016CF会員除外条件追加 ADD END *
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
 *      40.00 :  2022/09/06 SSI.本多 ： MCCM 初版
 *      40.00 :  2023/01/12 SSI.石   ： H016CF会員除外条件追加
 *      41.00 :  2023/08/10 SSI.上野 ： HS-0046ステージ判定金額取得形式変更
 *
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2022 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTmmskBServiceImpl extends CmABfuncLServiceImpl implements CmBTmmskBService {

    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                    /* デバッグメッセージ出力             */
    int C_OUTINF = 10000;                               /* 出力件数情報境界    */

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
    CmBTmmskBDto cmBTmmskBDto = new CmBTmmskBDto();

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;      /* OFF                                */
    int DEF_ON = 1;      /* ON                                 */

    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";            /* デバッグスイッチ                   */
    String DEF_debug = "-debug";            /* デバッグスイッチ                   */
    /* 2023/01/12 H016CF会員除外条件追加 ADD START */
    String DEF_cf = "-cf";                  /* CF会員除外                         */
    /* 2023/01/12 H016CF会員除外条件追加 ADD END */
    /*---------------------------------------------*/
    String C_PRGNAME = "月別会員別購買金額連携作成";        /* APログ用機能名    */
    int C_MAXCOL = 220;                           /* 出力ファイルMAXカラム数 */
    int C_MAXLEN = 200;                          /* カラム最大長            */
    int C_YYYYMMLEN = 6;                             /* 日付 年月部長さ         */
    int C_YYYYLEN = 4;                             /* 日付 年部長さ           */
    int C_MMLEN = 2;                             /* 日付 月部長さ           */
    int C_MMDDLEN = 4;                             /* 日付 月日部長さ         */
    int C_FISCAL_Y_DATE = 331;                        /* 前年度判定 基準日       */
    String C_UPDKBN_DEL = "3";                           /* 更新区分 3(取消、削除)  */
    /* 2023/01/12 H016CF会員除外条件追加 MOD START */
    String C_FL_NAME = "KKMI0080.csv";                /* 出力ファイル名   -cfなし  */
    /* 2023/01/12 H016CF会員除外条件追加 MOD END */
    /* 2023/01/12 H016CF会員除外条件追加 ADD START */
    String C_FL_NAME1 = "KKCR0080.csv";                /* 出力ファイル名   -cfあり  */
    /* 2023/01/12 H016CF会員除外条件追加 ADD END */
    /*---------------------------------------------*/
    int DEF_Read_EOF = 9;                 /* File read EOF                      */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    long file_write_cnt;                 /* ファイル出力件数             */
    StringDto out_file_dir = new StringDto(4096);             /* ワークディレクトリ           */
    StringDto fl_name = new StringDto(256);                   /* 出力ファイル名               */
    StringDto read_buf = new StringDto(6000);                 /* 出力ファイル読み込みバッファ */
    String[][] column = new String[C_MAXCOL][C_MAXLEN + 1];   /* 出力ファイルカラム分割用     */
    StringDto log_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用                  */
    FileStatusDto fp_out;                        /* 出力FP                       */
    /* 2023/01/12 H016CF会員除外条件追加 ADD START */
    char flg;                             /* CF判定用                  */
    /* 2023/01/12 H016CF会員除外条件追加 ADD END */

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
        int rtn_cd;                         /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス             */
        int arg_cnt;                        /* 引数チェック用カウンタ         */
        StringDto arg_Work1 = new StringDto(256);                 /* Work Buffer1                   */
        String env_wrk;                       /* 環境変数取得用                 */
        StringDto bat_date = new StringDto(9);                    /* バッチ処理日付（当日）         */
        StringDto wk_str = new StringDto(8);                      /* 文字列加工用ワーク             */

        file_write_cnt = 0;
        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
//        int date_today = 0;                         /* 処理日付                       */
//        int date_yesterday = 0;                      /* 前日日付                       */
//        int date_yesterday_yyyymm = 0;               //* 前日年月                       *//*
//        int date_yesterday_yyyy = 0;                 //* 前日年                         *//*
//        int date_yesterday_mm = 0;                   //* 前日月                         *//*
//        int date_yesterday_mmdd = 0;                 //* 前日月日                       *//*
        /* 2023/01/12 H016CF会員除外条件追加 MOD START */
        strcpy(fl_name, C_FL_NAME);              /* 出力ファイル名   -cfなし       */
        /* 2023/01/12 H016CF会員除外条件追加 MOD END */

        /* 2023/01/12 H016CF会員除外条件追加 ADD START */
        flg = 0;                                 /* CF判定用                       */
        /* 2023/01/12 H016CF会員除外条件追加 ADD END */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT("102", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
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

            if (0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug)) {
                continue;
            }
            /* 2023/01/12 H016CF会員除外条件追加 ADD START */
            else if (0 == strcmp(arg_Work1, DEF_cf)) {
                flg = 1;
                strcpy(fl_name, C_FL_NAME1);              /* 出力ファイル名   -cfあり       */
                continue;
            }
            /* 2023/01/12 H016CF会員除外条件追加 ADD END */
            else {    /* 定義外パラメータ   */
                sprintf(log_format_buf, "定義外の引数（%s）", arg_Work1);
                APLOG_WT("910", 0, null, log_format_buf, 0, 0, 0, 0, 0);
                if (DBG_LOG) {
                    /*---------------------------------------------*/
                    C_DbgEnd("main処理", C_const_APNG, 0, 0);
                    /*---------------------------------------------*/
                }
                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();
                return exit(C_const_APNG);
            }
        } /* FOR END */

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "CM_APWORK_DATE");
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
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /* 出力ファイルDIRセット */
        strcpy(out_file_dir, env_wrk);

        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
            /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** DBコネクト(%s)\n", BT_aplcomService.C_ORACONN_SD);
            /*------------------------------------------------------------*/
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_SD, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクトNG rtn   =[%d]\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status=[%d]\n", rtn_status);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（当日）        */
        /*-------------------------------------*/
        rtn_status.arr = 0;
        rtn_cd = C_const_OK;
        memset(bat_date, 0x00, sizeof(bat_date));

        /* バッチ処理日付を取得（当日） */
        rtn_cd = C_GetBatDate(0, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(当日)取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        cmBTmmskBDto.date_today.arr = atoi(bat_date);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理日付(当日)=[%d]\n", cmBTmmskBDto.date_today.intVal());
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* バッチ処理日付を取得（前日）        */
        /*-------------------------------------*/
        rtn_status.arr = 0;
        rtn_cd = C_const_OK;
        memset(bat_date, 0x00, sizeof(bat_date));

        /* バッチ処理日付を取得（前日） */
        rtn_cd = C_GetBatDate(-1, bat_date, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgMsg("*** main *** バッチ処理日(前日)取得NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理             */
            return exit(C_const_APNG);
        }
        cmBTmmskBDto.date_yesterday.arr = atoi(bat_date);
        memset(wk_str, 0x00, sizeof(wk_str));
        strncpy(wk_str, bat_date.arr, C_YYYYMMLEN);
        cmBTmmskBDto.date_yesterday_yyyymm.arr = atoi(wk_str);
        memset(wk_str, 0x00, sizeof(wk_str));
        strncpy(wk_str, bat_date.arr, C_YYYYLEN);
        cmBTmmskBDto.date_yesterday_yyyy.arr = atoi(wk_str);
        memset(wk_str, 0x00, sizeof(wk_str));
        sprintf(wk_str, "%c%c", bat_date.arr.charAt(4), bat_date.arr.charAt(5));
        cmBTmmskBDto.date_yesterday_mm.arr = atoi(wk_str);
        memset(wk_str, 0x00, sizeof(wk_str));
        sprintf(wk_str, "%c%c%c%c", bat_date.arr.charAt(4), bat_date.arr.charAt(5), bat_date.arr.charAt(6), bat_date.arr.charAt(7));
        cmBTmmskBDto.date_yesterday_mmdd.arr = atoi(wk_str);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 処理日付(前日)=[%d]\n", cmBTmmskBDto.date_yesterday.intVal());
            /*------------------------------------------------------------*/
        }

        /*--------------------------------*/
        /* 出力ファイルOPEN               */
        /*--------------------------------*/
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** main *** 連携ファイル %s\n", "オープン");
            /*------------------------------------------------------------*/
        }
        rtn_cd = cmBTmmskB_OpenFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmmskB_OpenFile処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return exit(C_const_APNG);
        }


        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /*-------------------------------------------------------*/
        /*  連携ファイル作成処理                                 */
        /*-------------------------------------------------------*/
        rtn_cd = cmBTmmskB_MakeFile();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** cmBTmmskB_MakeFile NG rtn=[%d]\n", rtn_cd);
                /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "月別会員別購買金額連携作成処理に失敗しました", 0, 0, 0, 0, 0);

            /* ファイルがオープンされていれば、クローズ */
            if (fp_out != null) {
                fclose(fp_out);
            }

            /* ロールバック */
            // EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /*--------------------------------*/
        /* 出力件数をAPログに出力する     */
        /*--------------------------------*/
        sprintf(log_format_buf, "%s/%s", out_file_dir, fl_name);
        APLOG_WT("105", 0, null, log_format_buf, file_write_cnt, 0, 0, 0, 0);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        /* コミット解放処理 */
        // EXEC SQL COMMIT WORK RELEASE;
        sqlca.commitRelease();

        /* 出力ファイルクローズ */
        cmBTmmskB_CloseFile();

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmmskB_MakeFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*            static int  cmBTmmskB_MakeFile()                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      連携ファイル作成処理                                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmmskB_MakeFile() {
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);        /* 動的SQLバッファ              */
        int rtn_cd;                           /* 関数戻り値                   */

        if (DBG_LOG) {
            /*------------------------------------------------------------------------*/
            C_DbgStart("*** cmBTmmskB_Make_File処理 ***");
            /*------------------------------------------------------------------------*/
        }

        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(cmBTmmskBDto.str_sql1, 0x00, sizeof(cmBTmmskBDto.str_sql1.toString()));

        /* 年→年度の補正 */
        /* ～3/31は前年度のため、年を-1する */
        if (cmBTmmskBDto.date_yesterday_mmdd.intVal() <= C_FISCAL_Y_DATE) {
            cmBTmmskBDto.date_yesterday_yyyy.arr = cmBTmmskBDto.date_yesterday_yyyy.intVal() - 1;
        }

        /* 列名の判定 */
        /* 年度が2で割り切れれば月間ランクＵＰ対象金額０XX、余りが出るなら月間ランクＵＰ対象金額１XX */
        if (cmBTmmskBDto.date_yesterday_yyyy.intVal() % 2 == 0) {
            switch (cmBTmmskBDto.date_yesterday_mm.intVal()) {
                case 1:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００１");
                    break;
                case 2:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００２");
                    break;
                case 3:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００３");
                    break;
                case 4:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００４");
                    break;
                case 5:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００５");
                    break;
                case 6:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００６");
                    break;
                case 7:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００７");
                    break;
                case 8:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００８");
                    break;
                case 9:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額００９");
                    break;
                case 10:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額０１０");
                    break;
                case 11:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額０１１");
                    break;
                case 12:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額０１２");
                    break;
                default:
                    break;
            }
        } else {
            switch (cmBTmmskBDto.date_yesterday_mm.intVal()) {
                case 1:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０１");
                    break;
                case 2:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０２");
                    break;
                case 3:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０３");
                    break;
                case 4:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０４");
                    break;
                case 5:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０５");
                    break;
                case 6:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０６");
                    break;
                case 7:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０７");
                    break;
                case 8:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０８");
                    break;
                case 9:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１０９");
                    break;
                case 10:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１１０");
                    break;
                case 11:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１１１");
                    break;
                case 12:
                    strcpy(cmBTmmskBDto.wk_col_name_sql, "月間ランクＵＰ対象金額１１２");
                    break;
                default:
                    break;
            }
        }
        /* 2023/01/12 H016CF会員除外条件追加 MOD START */
        if (flg == 0) {
            sprintf(wk_sql, " SELECT DISTINCT NULLIF(TRIM(TO_CHAR(A.ＧＯＯＰＯＮ番号, '0000000000000000')),'')," +
                            " '%d'," +
                            /* 2028/08/10 ステージ判定金額取得形式変更 */
                            " TO_CHAR(B.%s, 'S00000000')," +
                            " '%d'" +
                            " FROM MSカード情報 A" +
                            " LEFT OUTER JOIN TSランク情報 B ON A.顧客番号 = B.顧客番号" +
                            " WHERE B.最終更新日 >= '%d' AND A.ＧＯＯＰＯＮ番号 > 0",
                    cmBTmmskBDto.date_yesterday_yyyymm.intVal(),
                    cmBTmmskBDto.wk_col_name_sql,
                    cmBTmmskBDto.date_yesterday.intVal(),
                    cmBTmmskBDto.date_yesterday.intVal()
            );
            /* 2023/01/12 H016CF会員除外条件追加 MOD END */
            /* 2023/01/12 H016CF会員除外条件追加 ADD START */
        } else {
            sprintf(wk_sql, " SELECT DISTINCT NULLIF(TRIM(TO_CHAR(A.ＧＯＯＰＯＮ番号, '0000000000000000')),'')," +
                            " '%d'," +
                            /* 2028/08/10 ステージ判定金額取得形式変更 */
                            " TO_CHAR(B.%s, 'S00000000')," +
                            " '%d'" +
                            " FROM MSカード情報 A" +
                            " LEFT OUTER JOIN TSランク情報 B ON A.顧客番号 = B.顧客番号" +
                            " JOIN PS会員番号体系 C" +
                            " ON C.カード種別  NOT IN( 504, 505, 998 )" +
                            " AND A.サービス種別 = C.サービス種別" +
                            " AND A.会員番号  >= C.会員番号開始" +
                            " AND A.会員番号  <= C.会員番号終了" +
                            " WHERE B.最終更新日 >= '%d'",
                    cmBTmmskBDto.date_yesterday_yyyymm.intVal(),
                    cmBTmmskBDto.wk_col_name_sql,
                    cmBTmmskBDto.date_yesterday.intVal(),
                    cmBTmmskBDto.date_yesterday.intVal()
            );
        }
        /* 2023/01/12 H016CF会員除外条件追加 ADD END */


        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmmskB_Make_File *** 動的ＳＱＬ=[%s]\n", wk_sql);
            /*-------------------------------------------------------------*/
        }

        /* ＨＯＳＴ変数にセット */
        strcpy(cmBTmmskBDto.str_sql1, wk_sql);

        /* 動的ＳＱＬ文の解析 */
        // EXEC SQL PREPARE sql_stat_sec from :str_sql1;
        SqlstmDto sqlca = sqlcaManager.get("CUR_RKKS");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (DBG_LOG) {
            /*-------------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmmskB_Make_File *** 動的ＳＱＬ 解析結果=[%d]\n", sqlca.sqlcode);
            /*-------------------------------------------------------------------------------*/
        }

        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(最終更新日 = %d)\n", cmBTmmskBDto.date_yesterday.intVal());
            APLOG_WT("904", 0, null, "PREPARE", sqlca.sqlcode,
                    "MSカード情報",
                    log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmmskB_Make_File処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* カーソル宣言                   */
        /*--------------------------------*/
        // EXEC SQL DECLARE CUR_RKKS CURSOR FOR sql_stat_sec;
        sqlca.declare();

        /*--------------------------------*/
        /* カーソルオープン               */
        /*--------------------------------*/
        // EXEC SQL OPEN CUR_RKKS;
        sqlca.open();

        if (DBG_LOG) {
            /*------------------------------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmmskB_Make_File *** MSカード情報 CURSOR OPEN " +
                    "sqlcode =[%d]\n", sqlca.sqlcode);
            /*------------------------------------------------------------------------------------------*/
        }


        /* エラーの場合 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            sprintf(log_format_buf, "(最終更新日 = %d)\n", cmBTmmskBDto.date_yesterday.intVal());
            APLOG_WT("904", 0, null, "CURSOR OPEN", sqlca.sqlcode,
                    "MSカード情報", log_format_buf, 0, 0);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgEnd("cmBTmmskB_Make_File処理", C_const_NG, 0, 0);
                /*--------------------------------------------------------------------*/
            }
            return (C_const_NG);
        }

        /*--------------------------------*/
        /* テーブル読込みループ           */
        /*--------------------------------*/
        for (; ; ) {

            /* 初期化 */
            memset(cmBTmmskBDto.h_goopon_no, 0x00, sizeof(cmBTmmskBDto.h_goopon_no.toString()));
            memset(cmBTmmskBDto.h_taisho_ym, 0x00, sizeof(cmBTmmskBDto.h_taisho_ym.toString()));
            memset(cmBTmmskBDto.h_stage_hantei_kingaku, 0x00, sizeof(cmBTmmskBDto.h_stage_hantei_kingaku.toString()));
            memset(cmBTmmskBDto.h_koushin_ymd, 0x00, sizeof(cmBTmmskBDto.h_koushin_ymd.toString()));

            /*EXEC SQL FETCH CUR_RKKS
            INTO :h_goopon_no,
             :h_taisho_ym,
             :h_stage_hantei_kingaku,
             :h_koushin_ymd;*/
            sqlca.fetch();
            sqlca.recData(cmBTmmskBDto.h_goopon_no, cmBTmmskBDto.h_taisho_ym,
                    cmBTmmskBDto.h_stage_hantei_kingaku, cmBTmmskBDto.h_koushin_ymd);

            if (DBG_LOG) {
                /*------------------------------------------------------------------------------------*/
                C_DbgMsg("*** cmBTmmskB_Make_File *** MSカード情報 FETCH " +
                        "sqlcode =[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------------------------------*/
            }
            /* データ無し以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {

                sprintf(log_format_buf, "(最終更新日 = %d)\n", cmBTmmskBDto.date_yesterday.intVal());
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "MSカード情報", log_format_buf, 0, 0);

                // EXEC SQL CLOSE CUR_RKKS; /* カーソルクローズ                   */
                sqlcaManager.close("CUR_RKKS");
//                sqlca.close();
                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("cmBTmmskB_Make_File処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    /*--------------------------------------------------------------------------------------------------*/
                    C_DbgMsg("*** cmBTmmskB_Make_File *** MSカード情報 FETCH NOTFOUND=[%d]\n",
                            sqlca.sqlcode);
                    /*--------------------------------------------------------------------------------------------------*/
                }
                break;
            }

            /*------------------------------*/
            /* 取得データのファイル出力     */
            /*------------------------------*/
            rtn_cd = cmBTmmskB_WriteFile();
            if (rtn_cd != C_const_OK) {

                // EXEC SQL CLOSE CUR_RKKS;  /* カーソルクローズ                    */
//                sqlca.close();
                sqlcaManager.close("CUR_RKKS");
                if (DBG_LOG) {
                    /*--------------------------------------------------------------------*/
                    C_DbgEnd("cmBTmmskB_GetTanpin処理", C_const_NG, 0, 0);
                    /*--------------------------------------------------------------------*/
                }
                return (C_const_NG);
            }

            /* ファイル出力件数カウント*/
            file_write_cnt++;

            /* 処理件数（途中経過）を出力 */
            if (((file_write_cnt) % C_OUTINF) == 0) {
                APLOG_WT("107", 0, null, "MSカード情報", file_write_cnt, 0, 0, 0, 0);
            }

        } /* LOOP END */


        /* カーソルクローズ */
        // EXEC SQL CLOSE CUR_RKKS;
        sqlcaManager.close("CUR_RKKS");

        if (DBG_LOG) {
            /*--------------------------------------------------------------------*/
            C_DbgEnd("cmBTmmskB_Make_File処理", C_const_OK, 0, 0);
            /*--------------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmmskB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTmmskB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルオープン処理                                          */
    /*              出力ファイルをオープンする。                                  */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmmskB_OpenFile() {
        StringDto fp_name = new StringDto(4096);                      /** 出力ファイルパス名      **/

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("mcBTtrglB_OpenFile処理");
            /*------------------------------------------------------------*/
        }

        /* ファイルオープン */
        sprintf(fp_name, "%s/%s", out_file_dir, fl_name);

        if ((fp_out = open(fp_name.arr)).fd < 0) {
            /* APLOG(903) */
            sprintf(log_format_buf, "fopen(%s)", fl_name);
            APLOG_WT("903", 0, null, log_format_buf, 0, 0, 0, 0, 0);

            if (DBG_LOG) {
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTmmskB_OpenFile *** ファイルオープンERR%s\n", "");
                /*------------------------------------------------------------*/
            }
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("mcBTtrglB_OpenFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return C_const_OK;
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmmskB_WriteFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTmmskB_WriteFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイル出力処理                                              */
    /*              ファイルを書き込みする                                        */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmmskB_WriteFile() {
        StringDto wk_buf1 = new StringDto(512);                    /* 編集バッファ             */
        int rtn_cd;                          /* 関数戻り値               */
        int buf_len;                         /* 編集用                   */


        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgStart("cmBTmmskB_WriteFile処理");
            /*----------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(wk_buf1, 0x00, sizeof(wk_buf1));

        /*--------------------------------*/
        /* 出力レコードバッファの編集     */
        /*--------------------------------*/
        sprintf(wk_buf1, "%s,%s,%s,%s\n",
                cmBTmmskBDto.h_goopon_no,                         /* GOOPON番号       */
                cmBTmmskBDto.h_taisho_ym,                         /* 対象年月         */
                cmBTmmskBDto.h_stage_hantei_kingaku,              /* ステージ判定金額 */
                cmBTmmskBDto.h_koushin_ymd                        /* 更新日           */
        );

        buf_len = strlen(wk_buf1);

        if (DBG_LOG) {
            /*-----------------------------------------------------------------------*/
            C_DbgMsg("*** cmBTmmskB_WriteFile *** buf=[%s]\n", wk_buf1);
            /*-----------------------------------------------------------------------*/
        }

        fp_out.write(wk_buf1.arr);
        rtn_cd = ferror(fp_out);
        if (rtn_cd != C_const_OK) {
            /* ファイル書き込みエラー */
            sprintf(log_format_buf, "fwrite(%s/%s)", out_file_dir, fl_name);
            APLOG_WT("903", 0, null, log_format_buf, rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*------------------------------------------------------------------*/
                C_DbgEnd("cmBTmmskB_WriteFile処理", C_const_NG, 0, 0);
                /*------------------------------------------------------------------*/
            }
            /* 処理をNGで終了する */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            /*----------------------------------------------------------------------*/
            C_DbgEnd("cmBTmmskB_WriteFile処理", C_const_OK, 0, 0);
            /*----------------------------------------------------------------------*/
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTmmskB_CloseFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTmmskB_CloseFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              ファイルクローズ処理                                          */
    /*                                                                            */
    /*  【引数】    なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int cmBTmmskB_CloseFile() {
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("cmBTmmskB_CloseFile処理");
            /*------------------------------------------------------------*/
        }
        fclose(fp_out);
        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("cmBTmmskB_CloseFile処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }
        return C_const_OK;
    }


}
