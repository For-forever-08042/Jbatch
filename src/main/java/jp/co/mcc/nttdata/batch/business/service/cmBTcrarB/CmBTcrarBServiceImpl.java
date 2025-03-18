package jp.co.mcc.nttdata.batch.business.service.cmBTcrarB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTcrarB.dto.WRITE_DATA;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_SD;

/**********************************************************************************
 *   プログラム名   ： 顧客番号洗い替えリスト連動（cmBTcrarB）
 *
 *   【処理概要】
 *       HSカード変更情報からCRM連動用顧客番号洗い替えリストファイルを作成する。
 *
 *   【引数説明】
 *       -o                             ：顧客番号洗い替えリストファイル名
 *       -debug(-DEBUG)                  : デバッグモードでの実行
 *                                        （トレース出力機能が有効）
 *
 *   【戻り値】
 *      10   ： 正常
 *      99   ： 異常
 *
 *---------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *---------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/12/07 SSI.横山直人 ： 初版
 *     40.00 :  2023/04/17 NDBS.緒方    :  MCCM対応（MK会員除外）
 *---------------------------------------------------------------------------------
 *  $Id:$
 *---------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 *********************************************************************************/
@Service
public class CmBTcrarBServiceImpl extends CmABfuncLServiceImpl implements CmBTcrarBService {

    /*-------------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                       */
    /*-------------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                     */
    boolean DBG_LOG = true;                                /* デバッグメッセージ出力 */

    /*-------------------------------------------------------------------------------*/
    /*  定数定義                                                                     */
    /*-------------------------------------------------------------------------------*/
    int DEF_OFF = 0;                               /* OFF */
    int DEF_ON = 1;                               /* ON */
    String PG_NAME = "顧客番号洗い替えリスト連動"; /* プログラム名称 */

    int DEF_BUFSIZE4 = 4;      /* 4バイト */
    int DEF_BUFSIZE5 = 5;      /* 5バイト */
    int DEF_BUFSIZE6 = 6;      /* 6バイト */
    int DEF_BUFSIZE7 = 7;      /* 7バイト */
    int DEF_BUFSIZE8 = 8;      /* 8バイト */
    int DEF_BUFSIZE10 = 10;      /* 10バイト */
    int DEF_BUFSIZE13 = 13;      /* 13バイト */
    int DEF_BUFSIZE15 = 15;      /* 15バイト */
    int DEF_BUFSIZE16 = 16;      /* 16バイト */
    int DEF_BUFSIZE20 = 20;      /* 20バイト */
    int DEF_BUFSIZE30 = 30;      /* 30バイト */
    int DEF_BUFSIZE32 = 32;      /* 32バイト */
    int DEF_BUFSIZE40 = 40;      /* 40バイト */
    int DEF_BUFSIZE50 = 50;      /* 50バイト */
    int DEF_BUFSIZE80 = 80;      /* 80バイト */
    int DEF_BUFSIZE256 = 256;      /* 256バイト */
    int DEF_BUFSIZE4K = 4 * 1024;      /* 4 * 1024バイト */
    int DEF_BUFSIZE8K = 8 * 1024;      /* 8 * 1024バイト */
    int DEF_BUFSIZE16K = 16 * 1024;      /* 16 * 1024バイト */

    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_O = "-o";                    /* エラーファイル名 */
    String DEF_DEBUG = "-DEBUG";                    /* デバッグスイッチ */
    String DEF_debug = "-debug";                    /* デバッグスイッチ */
    /*---------------------------------------------*/

    /*-----  ログ出力用メッセージID ----------*/
    String DEF_MSG_ID_102 = "102";
    String DEF_MSG_ID_103 = "103";
    String DEF_MSG_ID_105 = "105";
    String DEF_MSG_ID_902 = "902";
    String DEF_MSG_ID_903 = "903";
    String DEF_MSG_ID_904 = "904";
    String DEF_MSG_ID_910 = "910";
    String DEF_MSG_ID_912 = "912";

    String DEF_STR1 = "\"";
    String DEF_STR2 = ",";
    String DEF_STR3 = "\n";

    int ERR_FILE_REC_LEN = 128;                /* エラーファイルレコード長 */

    /*-----  企業コード ----------*/
    String DEF_CF_CARD = "1010";                /* CF/MCC現金カード  */
    String DEF_CF_EC = "1020";                /* CF_EC             */
    String DEF_CF_APL = "1040";                /* CF_APL            */
    String DEF_MCM_CARD = "3010";                /* MK/MCM現金カード  */

    /*-------------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                                 */
    /*-------------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    ItemDto h_new_kokyaku_no = new ItemDto(16);               /* 顧客番号 */
    ItemDto h_old_kokyaku_no = new ItemDto(16);                 /* 旧顧客番号 */
    ItemDto h_new_kaiin_no = new ItemDto(19);                 /* 会員番号 */
    ItemDto h_old_kaiin_no = new ItemDto(19);               /* 旧会員番号 */
    ItemDto h_new_kigyo_code = new ItemDto();        /* 企業コード */
    ItemDto h_old_kigyo_code = new ItemDto();        /* 旧企業コード */
    ItemDto h_count = new ItemDto();                            /* MK会員件数 */

    StringDto str_sql = new StringDto(4096 * 4);              /* 実行用SQL文字列 */

    //    EXEC SQL END DECLARE SECTION;
    /*-------------------------------------------------------------------------------*/
    /*  入出力ファイル                                                               */
    /*-------------------------------------------------------------------------------*/
    /* 出力ファイル */
    FileStatusDto fp;                        /* ファイルポインタ */

    WRITE_DATA write_t;
    /*-------------------------------------------------------------------------------*/
    /*  変数定義                                                                     */
    /*-------------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int arg_o_chk;                  /* 引数oチェック用 */
    StringDto arg_o_Value = new StringDto(DEF_BUFSIZE256);                /* 引数o設定値 */
    /*---------------------------------------------*/

    StringDto bat_yyyymmdd = new StringDto(DEF_BUFSIZE8 + 1);               /* バッチ処理日付 */
    StringDto bat_yyyymmdd_today = new StringDto(DEF_BUFSIZE8 + 1);              /* システム年月日 */
    StringDto system_hhmmss = new StringDto(DEF_BUFSIZE8 + 1);                /* システム時分秒 */

    int bat_shori_yyyymmdd;             /* バッチ処理年月日 */

    StringDto path_file = new StringDto(DEF_BUFSIZE256);                  /* 出力用ディレクトリ＋ファイル名 */

    int cnt;                        /* 出力ファイルデータ件数 */

    StringDto out_format_buf = new StringDto(DEF_BUFSIZE4K);                  /* APログフォーマット */
    StringDto ap_work_dir = new StringDto(DEF_BUFSIZE4K);                /* 出力ファイルディレクトリ */


/*********************************************************************************/
    /*                                                                               */
    /*  メイン関数                                                                   */
    /*   int  main(int argc, char** argv)                                            */
    /*                                                                               */
    /*            argc ： 起動時の引数の数                                           */
    /*            argv ： 起動時の引数の文字列                                       */
    /*                                                                               */
    /*  【説明】                                                                     */
    /*              メイン処理を行う                                                 */
    /*                                                                               */
    /*  【引数】                                                                     */
    /*              プログラムヘッダ参照                                             */
    /*                                                                               */
    /*  【戻り値】                                                                   */
    /*              プログラムヘッダ参照                                             */
    /*                                                                               */

    /*********************************************************************************/
    @Override
    public MainResultDto main(int argc, String[] argv) {
        int rtn_cd;                 /* 関数戻り値 */
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス */
        int arg_cnt;                /* 引数チェック用カウンタ */
        String env_wrk;                   /* 出力ファイルDIR */
        StringDto arg_Work1 = new StringDto(DEF_BUFSIZE256);          /* Work Buffer1 */

        /*-----------------------------------------------*/
        /*  プログラム名取得                             */
        /*-----------------------------------------------*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT(DEF_MSG_ID_102, 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /*-----------------------------------------------*/
        /*  バッチデバッグ開始                           */
        /*-----------------------------------------------*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  入力引数チェック                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgStart("*** main処理 ***");
        }

        /* 初期化 */
        arg_o_chk = DEF_OFF;
        memset(arg_o_Value, 0x00, sizeof(arg_o_Value));
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (DBG_LOG) {
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "START");
        }

        /*** 引数チェック ***/
        rtn_cd = C_const_OK;                /* 関数戻り値 */
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, sizeof(arg_Work1));
            strcpy(arg_Work1, argv[arg_cnt]);

            if (DBG_LOG) {
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
            }
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                /* 引数の文字列が"-DEBUG"または、"-debug"の場合 */
                continue;
            } else if (memcmp(arg_Work1, DEF_ARG_O, 2) == 0) {
                /* 引数の文字列が"-o"の場合 */
                rtn_cd = Chk_ArgoInf(arg_Work1, arg_cnt);
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        C_DbgMsg("*** main *** -o 引数がエラー%s\n", "");
                        C_DbgEnd("main処理", C_const_APNG, 0, 0);
                    }
                    /*  エラーメッセージ */
                    APLOG_WT(DEF_MSG_ID_910, 0, null, out_format_buf, 0, 0, 0, 0, 0);

                    /* バッチデバッグ終了処理 */
                    rtn_cd = C_EndBatDbg();

                    /* 異常終了 */
                    return exit(C_const_APNG);
                }
            } else {
                /* 規定外パラメータ  */
                rtn_cd = C_const_NG;
                sprintf(out_format_buf, "定義外の引数（引数（%d番目）の文字列）", arg_cnt);
            }

            if (DBG_LOG) {
                C_DbgMsg("*** main *** チェック結果 = [%d]\n", rtn_cd);
            }

            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** main *** チェックNG = [%d]\n", rtn_cd);
                }
                /*  エラーメッセージ */
                APLOG_WT(DEF_MSG_ID_910, 0, null, out_format_buf, 0, 0, 0, 0, 0);

                /* バッチデバッグ終了処理 */
                rtn_cd = C_EndBatDbg();

                /* 異常終了 */
                return exit(C_const_APNG);
            }
        }

        /* -o の最終チェック */
        if (arg_o_chk == DEF_OFF) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** -o 引数が未指定%s\n", "");
                C_DbgEnd("main処理", C_const_APNG, 0, 0);
            }
            sprintf(out_format_buf, "出力ファイル名１が存在しません ");
            APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  環境変数取得                                 */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "");
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

        /* 出力ファイル用デレクトリ設定 */
        memset(ap_work_dir, 0x00, sizeof(ap_work_dir));
        strcpy(ap_work_dir, env_wrk);

        if (DBG_LOG) {
            C_DbgMsg("*** main *** 環境変数取得OK（出力ファイルDIR）%s\n", ap_work_dir);
        }

        /*-----------------------------------------------*/
        /*  DBコネクト                                   */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
        }
        rtn_cd = C_OraDBConnect(C_ORACONN_SD, rtn_status);

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_OraDBConnect", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** main *** DBコネクトOK rtn= %d\n", rtn_cd);
            C_DbgMsg("*** main *** DBコネクトOK status= %d\n", rtn_status);
        }

        /*-----------------------------------------------*/
        /*  バッチ処理日取得                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
        }

        memset(bat_yyyymmdd_today, 0x00, sizeof(bat_yyyymmdd_today));

        rtn_cd = C_GetBatDate(0, bat_yyyymmdd_today, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_yyyymmdd_today);
        }

        /*-----------------------------------------------*/
        /*  バッチ処理日取得                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得%s\n", "");
        }

        memset(bat_yyyymmdd, 0x00, sizeof(bat_yyyymmdd));
        rtn_cd = C_GetBatDate(-1, bat_yyyymmdd, rtn_status);

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得OK [%s]\n", bat_yyyymmdd);
        }

        bat_shori_yyyymmdd = atoi(bat_yyyymmdd);

        if (DBG_LOG) {
            C_DbgMsg("*** バッチ処理日年月日 =[%d]\n", bat_shori_yyyymmdd);
        }

        /*-----------------------------------------------*/
        /*  データ件数初期化                             */
        /*-----------------------------------------------*/
        cnt = 0;

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        /* 初期化 */
        fp = null;

        rtn_cd = cmBTcrarB_main();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** cmBTcrarB_main NG rtn =[%d]\n", rtn_cd);
            }
            APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客番号洗い替えリスト処理に失敗しました", 0, 0, 0, 0, 0);

            /* 出力ファイルがオープンされていれば、クローズ */
            if (fp != null) {
                fclose(fp);
            }

            /* ロールバック */
            sqlca.rollback();
//                EXEC SQL ROLLBACK RELEASE;

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/

        /* 出力ファイルがオープンされていれば、クローズ */
        if (fp != null) {
            fclose(fp);
        }

        /* データ件数 */
        C_APLogWrite(DEF_MSG_ID_105, "出力ファイル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", path_file, cnt, 0, 0, 0, 0)
        ;

        if (DBG_LOG) {
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
        }

        /*  終了メッセージ */
        APLOG_WT(DEF_MSG_ID_103, 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        /* コミット解放処理 */
//            EXEC SQL COMMIT WORK RELEASE;

        sqlcaManager.commitRelease();
        /* 正常終了 */
        return exit(C_const_APOK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTcrarB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcrarB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客番号洗い替えリスト連動主処理                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/

    public int cmBTcrarB_main() {
        int rtn_cd;             /* 関数戻り値 */

        if (DBG_LOG) {
            C_DbgStart("*** cmBTcrarB_main *** 顧客番号洗い替えリスト連動主処理");
        }

        /*-----------------------------------------------*/
        /*  顧客番号洗い替えリストを作成                 */
        /*-----------------------------------------------*/
        rtn_cd = Make_List_File();

        if (rtn_cd == C_const_NG) {
            /* APログ出力 */
            APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客番号洗い替えリスト出力処理に失敗しました", 0, 0, 0, 0, 0);

            /* 出力ファイルがオープンされていれば、クローズ */
            if (fp != null) {
                fclose(fp);
            }

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 処理終了 */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            C_DbgEnd("*** cmBTcrarB_main *** 顧客番号洗い替えリスト連動主処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Make_List_File                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Make_List_File()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              HSカード変更情報からCRM連動用顧客番号洗い替えリストファイル   */
    /*              を作成する                                                    */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int Make_List_File() {
        StringDto wk_sql = new StringDto(DEF_BUFSIZE16K);  /* 動的SQLバッファ                          */
        StringDto dbuf = new StringDto(DEF_BUFSIZE256);    /* エラー表示用バッファ                     */
        StringDto wbuf = new StringDto(DEF_BUFSIZE256);    /* エラー表示用バッファ                     */

        if (DBG_LOG) {
            C_DbgStart("Make_List_File処理");
        }

        /* 変数初期化 */
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_sql, 0x00, sizeof(wk_sql));

        /* SQL文作成 */
        sprintf(wk_sql, "SELECT CC.顧客番号, "
                        + "CC.旧顧客番号, "
                        /* 2023/04/17 MCCM対応 START */
                        + "CC.会員番号, "
                        + "CC.旧会員番号, "
                        + "CC.企業コード, "
                        + "CC.旧企業コード "
                        /* 2023/04/17 MCCM対応 END */
                        + "FROM   HSカード変更情報 CC "
                        + "WHERE  CC.最終更新日 >= %d "
                        + "AND ( "
                        + "  MOD(CC.理由コード, 100) = 3 "
                        + "  OR ( "
                        + "      MOD(CC.理由コード, 100) = 2 "
                        + "      AND CC.旧顧客番号 <> CC.顧客番号 "
                        + "  ) "
                        + " ) "
                        /* 2023/04/17 MCCM対応 START */
                        + "AND ( "
                        + "  CC.企業コード IN (1010, 1020, 1040, 3010 ) "
                        + "   AND "
                        + "  CC.旧企業コード IN (1010, 1020, 1040, 3010 ) "
                        + " ) "
                        /* 2023/04/17 MCCM対応 END */
                        + "ORDER BY "
                        + "  CC.最終更新日時 ",
                bat_shori_yyyymmdd);

        strcpy(str_sql, wk_sql);

        /* 動的SQL文解析 */
//        EXEC SQL PREPARE sql_tsptmsinf FROM:
//        str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_tsptmsinf");
        sqlca.prepare(str_sql);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcrarB Make_Coupon_CrmFile() *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgMsg("*** cmBTcrarB Make_Coupon_CrmFile() *** 動的SQL 解析NG = %s\n", str_sql);
            }
            /*APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, str_sql, 0, 0, 0, 0);*/
            memset(dbuf, 0x00, sizeof(dbuf));
            memset(wbuf, 0x00, sizeof(wbuf));
            strcpy(dbuf, "顧客番号洗い替えリストファイル動的SQL文解析エラー ");
            sprintf(wbuf, "バッチ処理日：%d ", bat_shori_yyyymmdd);
            strcat(dbuf, wbuf);

            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, dbuf, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE cur CURSOR FOR sql_tsptmsinf;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcrarB Make_Coupon_CrmFile() *** カーソル宣言 cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソルをオープン */
//        EXEC SQL OPEN cur;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcrarB Make_Coupon_CrmFile() *** カーソルオープン cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, "CURSOR OPEN ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* ファイル名を作成 */
        memset(path_file, 0x00, sizeof(path_file));
        strcpy(path_file, ap_work_dir);
        strcat(path_file, "/");
        strcat(path_file, arg_o_Value);

        /* ファイルをオープン */
        if ((fp = fopen(path_file.arr, SystemConstant.Shift_JIS,FileOpenType.a)).fd == C_const_NG) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** OpenFile *** ファイルオープンNG%s\n", "");
            }
            sprintf(out_format_buf, "fopen（%s）", path_file);
            APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, fp.fd, 0, 0, 0, 0);
            return (C_const_NG);
        }

        while (true) {
            /* 初期化 */
            memset(h_new_kokyaku_no, 0x00, sizeof(h_new_kokyaku_no));                               /* 顧客番号 */
            memset(h_old_kokyaku_no, 0x00, sizeof(h_old_kokyaku_no));                               /* 旧顧客番号 */
            /* 2023/04/17 MCCM対応 SATRT */
            memset(h_new_kaiin_no, 0x00, sizeof(h_new_kaiin_no)); /* 会員番号 */
            memset(h_old_kaiin_no, 0x00, sizeof(h_old_kaiin_no)); /* 旧会員番号 */
            h_new_kigyo_code.arr = 0;                                 /* 企業コード */
            h_old_kigyo_code.arr = 0;                                 /* 旧企業コード */
            memset(write_t, 0x00, sizeof(write_t));
            write_t=new WRITE_DATA();
            /* 2023/04/17 MCCM対応 END */

            /* フェッチ処理 */
            sqlca.fetchInto(h_new_kokyaku_no,
                    h_old_kokyaku_no,
                    h_new_kaiin_no,
                    h_old_kaiin_no,
                    h_new_kigyo_code,
                    h_old_kigyo_code);
            /* データ無し以外エラーの場合処理を異常終了 */
            if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
                /* エラー発生 */
                if (DBG_LOG) {
                    C_DbgMsg("*** cmBTcrarB Make_List_File() *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("Make_List_File処理", C_const_NG, 0, 0);
                }
                sprintf(wbuf, "MOD(理由コード,100) = 3, AND 最終更新日 >= [%s]", bat_yyyymmdd);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "FETCH",
                        sqlca.sqlcode,
                        "HSカード変更情報",
                        wbuf, 0, 0);

                /* ファイルをクローズ */
                fclose(fp);

                /* カーソルをクローズ */
//                EXEC SQL CLOSE cur;
                sqlcaManager.close(sqlca);

                if (DBG_LOG) {
                    C_DbgEnd("Make_Coupon_CrmFile処理 フェッチエラー ", 0, 0, 0);
                }

                /* 処理をNGで終了 */
                return (C_const_NG);
            }

            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            sprintf(write_t.new_kokyaku_no, "%d", atol(h_new_kokyaku_no)); /* 顧客番号設定 */
            sprintf(write_t.old_kokyaku_no, "%d", atol(h_old_kokyaku_no)); /* 旧顧客番号設定 */

            /* 2023/04/17 MCCM対応 SATRT */
            /* MK会員の統合かチェック */
            if (h_new_kigyo_code.intVal() == 3010 || h_old_kigyo_code.intVal() == 3010) {

                /* 初期化 */
                h_count.arr = 0;

                SqlstmDto sqlcaTmp = sqlcaManager.get("sqlcaTmpMSカード情報");
                sqlcaTmp.sql = new StringDto("SELECT COUNT ( *)AS 件数" +
                        "                FROM MSカード情報 T" +
                        "                WHERE T.企業コード = 3010" +
                        "                AND T.会員番号 IN (?, ?)" +
                        "                AND T.旧販社コード = 20");

                sqlcaTmp.restAndExecute(h_new_kaiin_no, h_old_kaiin_no);
                sqlcaTmp.fetchInto(h_count);

                /* エラーの場合処理をAPLOGする */
                if (sqlcaTmp.sqlcode != C_const_Ora_OK) {

                    sprintf(wbuf,
                            "会員番号（%d, %d）", atol(h_new_kaiin_no),
                            atol(h_old_kaiin_no));

                    APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                            sqlcaTmp.sqlcode,
                            "MSカード情報",
                            wbuf, 0, 0);
                    /* ファイルをクローズ */
                    fclose(fp);

                    /* カーソルをクローズ */
                    sqlcaManager.close(sqlca);

                    if (DBG_LOG) {
                        C_DbgEnd("Make_List_File処理", 0, 0, 0);
                    }

                    /* 処理をNGで終了 */
                    return (C_const_NG);

                }

                /* MK会員の統合でない場合 */
                if (h_count.intVal() == 0) {
                    /* ファイルにデータを書き込む */
                    if (C_const_NG == Write_List_File()) {
                        /* エラー発生 */
                        APLOG_WT(DEF_MSG_ID_912, 0, null,
                                "データレコード出力処理に失敗しました", 0, 0, 0, 0, 0);

                        /* ファイルをクローズ */
                        fclose(fp);

                        /* カーソルをクローズ */
                        sqlcaManager.close(sqlca);
                        if (DBG_LOG) {
                            C_DbgEnd("Make_List_File処理", 0, 0, 0);
                        }

                        /* 処理をNGで終了 */
                        return (C_const_NG);
                    }

                }

            }
            /* MK会員の統合でない場合 */
            else {
                /* ファイルにデータを書き込む */
                if (C_const_NG == Write_List_File()) {
                    /* エラー発生 */
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "データレコード出力処理に失敗しました", 0, 0, 0, 0, 0);

                    /* ファイルをクローズ */
                    fclose(fp);

                    /* カーソルをクローズ */
                    sqlcaManager.close(sqlca);

                    if (DBG_LOG) {
                        C_DbgEnd("Make_List_File処理", 0, 0, 0);
                    }

                    /* 処理をNGで終了 */
                    return (C_const_NG);
                }
            }
        }

        /* ファイルをクローズ */
        fclose(fp);
        fp = null;

        /* カーソルをクローズ */
        sqlcaManager.close(sqlca);

        if (DBG_LOG) {
            C_DbgEnd("Make_List_File処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Wriet_List_File                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Wriet_List_File()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              リストファイルにデータを書き込む                              */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int Write_List_File() {
        int rtn_cd;                 /* 関数戻り値 */

        StringDto outbuf = new StringDto(DEF_BUFSIZE8K);          /* 出力する行のバッファ */

        if (DBG_LOG) {
            C_DbgStart("Write_List_File処理");
        }

        /* 行編集 */
        memset(outbuf, 0x00, sizeof(outbuf));

        /* ホスト変数_顧客番号 */
        strcat(outbuf, write_t.new_kokyaku_no.strVal());
        strcat(outbuf, DEF_STR2);

        /* ホスト変数_旧顧客番号 */
        strcat(outbuf, write_t.old_kokyaku_no.strVal());
        strcat(outbuf, DEF_STR2);

        /* ホスト変数_バッチ処理日付 */
        strcat(outbuf, bat_yyyymmdd_today);

        /* 改行コード */
        strcat(outbuf, DEF_STR3);

        /* CRM出力ファイル書込み */
        rtn_cd = fputs(outbuf, fp);
        if (rtn_cd == C_const_NG) {
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcrarB Write_List_File() *** fputs NG rtn=[%d]\n", rtn_cd);
            }
            sprintf(out_format_buf, "fputs（%s）", path_file);
            APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, rtn_cd, 0, 0, 0, 0);

            return (C_const_NG);
        }

        /* 正常処理件数カウントアップ */
        cnt++;

        if (DBG_LOG) {
            C_DbgEnd("Write_List_File処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Chk_ArgoInf                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Chk_ArgoInf( char *Arg_in, int arg_cnt )                      */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数（-o スイッチ）のチェックを行う                           */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：-o スイッチの引数                       */
    /*      int             arg_cnt     ：引数番号                                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int Chk_ArgoInf(StringDto Arg_in, int arg_cnt) {
        /*-----------------------------------------------*/
        /*  ローカル変数定義                             */
        /*-----------------------------------------------*/

        if (DBG_LOG) {
            C_DbgStart("Chk_ArgoInf処理");
        }

        /*-----------------------------------------------*/
        /*  重複指定チェック                             */
        /*-----------------------------------------------*/
        if (arg_o_chk != DEF_OFF) {
            if (DBG_LOG) {
                C_DbgMsg("*** Chk_ArgoInf *** 重複指定NG = %s\n", Arg_in);
            }
            sprintf(out_format_buf, "-o 引数が重複しています（%s）", Arg_in);
            return (C_const_NG);
        }

        /*-----------------------------------------------*/
        /*  値の内容チェック                             */
        /*-----------------------------------------------*/
        /*  設定値nullチェック  */
        if (Arg_in.len <= 2) {

            if (DBG_LOG) {
                C_DbgMsg("*** Chk_ArgiInf *** 設定値null = %s\n", Arg_in);
            }

            sprintf(out_format_buf, "出力ファイル名１が誤っています（引数（%d番目）の文字列）", arg_cnt);

            return (C_const_NG);
        }

        if (DBG_LOG) {
            C_DbgEnd("Chk_ArgoInf処理", 0, 0, 0);
        }
        strcpy(arg_o_Value, Arg_in.arr.substring(2));
        arg_o_chk = DEF_ON;

        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： APLOG_WT                                                    */
    /*                                                                            */
    /*  書式                                                                  */
    /*  static int  APLOG_WT(char *msgid, int  msgidsbt, char *dbkbn,         */
    /*                            caddr_t param1, caddr_t param2, caddr_t param3, */
    /*                            caddr_t param4, caddr_t param5, caddr_t param6) */
    /*                                                                            */
    /*  【説明】                                                              */
    /*              ＡＰログ出力を行う                                            */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                              */
    /*      char    *msgid     ：メッセージＩＤ                           */
    /*      int     msgidsbt   ：メッセージ登録種別                       */
    /*      char    *dbkbn     ：ＤＢ区分                                 */
    /*      caddr_t     param1     ：置換変数１                           */
    /*      caddr_t     param2     ：置換変数２                           */
    /*      caddr_t     param3     ：置換変数３                           */
    /*      caddr_t     param4     ：置換変数４                           */
    /*      caddr_t     param5     ：置換変数５                           */
    /*      caddr_t     param6     ：置換変数６                           */
    /*                                                                            */
    /*  【戻り値】                                                            */
    /*              0   ： 正常                                               */
    /*              1   ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object
            param3, Object param4, Object param5, Object param6) {
        String[] out_flg = new String[2];            /* APログフラグ */
        String[] out_format = new String[2];     /* APログフォーマット */
        IntegerDto out_status = new IntegerDto();             /* フォーマット取得結果 */
        int rtn_cd;             /* 関数戻り値 */

        if (DBG_LOG) {
            C_DbgStart("APLOG_WT処理");
        }

        /*#####################################*/
        /*  APログフォーマット取得処理         */
        /*#####################################*/

        memset(out_flg, 0x00, sizeof(out_flg));
        memset(out_format, 0x00, sizeof(out_format));
        out_status.arr = 0;
        /*dbg_getaplogfmt = 1;*/
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        /*dbg_getaplogfmt = 0; */

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtn_cd);
        }

        /*#####################################*/
        /*  APログ出力処理                     */
        /*#####################################*/
        rtn_cd = C_APLogWrite(msgid, out_format, out_flg, param1, param2, param3, param4, param5, param6);

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        }

        return (C_const_OK);
    }
}
