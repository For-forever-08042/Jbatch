package jp.co.mcc.nttdata.batch.business.com.cmABterbB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.SQLDA;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABendeB.CmABendeBService;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.constanst.CmABfuncLConstant;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_OUT_FILE;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_OUT_REC;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_PARA;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
/******************************************************************************/
/*   プログラム名       ： 連動ファイル作成（cmABterbB.pc）                    */
/*                                                                            */
/*   【処理概要】 連動ファイル作成 (可変長)                                   */
/*                  指定されたファイル名で、カレントディレクトリに出力する    */
/*                                                                            */
/*   【引数説明】                                                             */
/*      -P入力パラメータファイル名 : （必須）桁数は９桁(バージョン番号なし)   */
/*      -F出力ファイル名 : （必須）                                           */
/*      -Tテーブル名用日付指定 : （任意） (YYYYMMDD)                          */
/*      -D日付指定 : （任意）             (YYYYMMDD)                          */
/*      -C企業指定 : （任意）             ("SE" or "SO")                      */
/*      -E汎用パラメータ : （任意）                                           */
/*      -S接続先ＤＢ区分 : （任意）       ("SD","MD","HD","BD")               */
/*      -A : （任意）  改行コード(0x0a)の付加を指示する                       */
/*                                                                            */
/*   【戻り値】                                                               */
/*       10      ： 正常                                                      */
/*       99      ： 異常                                                      */
/*                                                                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 : 2012/10/18 SSI.吉岡      ： 初版                               */
/*     40.00 : 2022/11/09 西山          ： MCCM初版                           */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */
/******************************************************************************/
@Service
public class CmABterbBServiceImpl extends CmABfuncLServiceImpl implements CmABterbBService {

    @Autowired
    CmABendeBService cmABendeBService;
    /*----------------------------------------------------------------------------*/
    /*	トレース出力要否設定（0:不要、1:必要）                                */
    /*----------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                  */
    boolean DBG_main = true;                          /* main               */
    boolean DBG_APLOG_WT        = true;                          /* APLOG_WT           */
    boolean DBG_PARSE_PARAM     = true;
    boolean DBG_MAIN_PROC       = true;
    boolean DBG_READ_PARAM_FILE = true;
    boolean DBG_SPLIT_REC       = true;


    /*-----------------------------------------------------------------------------*/
    /*	定数定義                                                               */
    /*-----------------------------------------------------------------------------*/
    int TERA_COLUMN_COUNT_MAX = 255; /* 最大項目数 */

    String TERA_DATATYPE_U = "U";
    String TERA_DATATYPE_X = "X";
    /* 2022/11/09 MCCM初版 ADD START */
    String TERA_DATATYPE_MP1 = "MP1";
    String TERA_DATATYPE_MP2 = "MP2";
    String TERA_DATATYPE_SD = "SD";
    String TERA_DATATYPE_GL = "GL";
    /* 2022/11/09 MCCM初版 ADD END */
    int TERA_DATATYPE_LEN = 3; /* データタイプのサイズ */
    int TERA_KOMOKUMEI_LEN = 512; /* 項目名のサイズ       */
    int TERA_CORPCD_LEN = 10;

    int TERA_FILE_PATH_MAX = 1000;/* フルパスのファイル名等の最大長 */

    /*-----------------------------------------------------------------------------*/
    /*      型定義                                                                 */
    /*-----------------------------------------------------------------------------*/

    /* 引数 */
//    typedef struct {
//        char in_file_name[9+1];           /* パラメータファイル名 */
//        char out_file_name[TERA_FILE_PATH_MAX]; /* 出力ファイル名 */
//        char table_name_ymd[9];           /* テーブル名用日付 */
//        char ymd[9];                      /* 日付 */
//        char corp_cd[TERA_CORPCD_LEN+1];  /* 企業 */
//        char com_param[1000];             /* 汎用パラメータ */
//        char db_kbn[2+1];                 /* ＤＢ区分 */
//        char kaigyo_kbn[2];               /* 改行指定 -Aの場合、0x0aがセットされる */
//    } TERA_PARA;

    /* 出力ファイル情報 */
//    typedef struct {
//        int rec_len; /* レコード長       */
//        int fld_cnt; /* フィールド数     */
//        int rec_cnt; /* 出力レコード件数 */
//    } TERA_OUT_FILE;

    /* 出力レコード情報 */
//    typedef struct {
//        int  offset[TERA_COLUMN_COUNT_MAX];                            /* オフセット */
//        int  length[TERA_COLUMN_COUNT_MAX];                            /* レングス */
//        char dattyp[TERA_COLUMN_COUNT_MAX][TERA_DATATYPE_LEN+1];       /* データタイプ */
//        char komoku[TERA_COLUMN_COUNT_MAX][TERA_KOMOKUMEI_LEN+1];      /* 項目名 */
//        int  sjiscv[TERA_COLUMN_COUNT_MAX];                            /* SJIS変換する場合は１ */
//        int  encdec[TERA_COLUMN_COUNT_MAX];                            /* 暗号復号する場合は１ */
//    } TERA_OUT_REC;



    /*-----------------------------------------------------------------------------*/
    /*	変数定義                                                               */
    /*-----------------------------------------------------------------------------*/
    /* トレースログ出力のフラグ */
//    int dbg_getaplogfmt;
//    int dbg_aplogwrite;
//    int dbg_oradbconnect;
//    int dbg_convut2sj;

    StringDto s_server_id= new StringDto();  /* サーバーＩＤ（２桁） */

    /*-----------------------------------------------------------------------------*/
    /*	ホスト変数定義                                                         */
    /*-----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;
    StringDto H_SQLSTR = new StringDto(64000);
//    EXEC SQL END DECLARE SECTION;

    /******************************************************************************/
    /*                                                                            */
    /*	メイン関数                                                                */
    /*	 int  main(int argc, char** argv)                                         */
    /*                                                                            */
    /*            argc ： 起動時の引数の数                                        */
    /*            argv ： 起動時の引数の文字列                                    */
    /*                                                                            */
    /*	【説明】                                                                  */
    /*              連動ファイル作成                                              */
    /*                                                                            */
    /*	【引数】                                                                  */
    /*		プログラムヘッダ参照                                                  */
    /*                                                                            */
    /*	【戻り値】                                                                */
    /*              プログラムヘッダ参照                                          */
    /*                                                                            */
    /******************************************************************************/
    public MainResultDto main(int argc, String[] argv)
    {
        /*-------------------------------------*/
        /*	ローカル変数定義               */
        /*-------------------------------------*/
        int rtn_cd;				/** 関数戻り値               **/
        int rtn_status;			/** 関数ステータス           **/
        int i;
        int wk_cnt;
        String[] wk_server_id = new String[3];
        String penv;
        FileStatusDto fo;               /* 出力ファイル */
        TERA_PARA wk_para = new TERA_PARA(); /* 引数を解析して格納する */
        TERA_OUT_FILE wk_out_file = new TERA_OUT_FILE();
        TERA_OUT_REC wk_out_rec = new TERA_OUT_REC();
        StringDto wk_inp_file_dir = new StringDto(TERA_FILE_PATH_MAX); /* 入力ファイルのディレクトリ */
        StringDto wk_inp_file_nam = new StringDto(TERA_FILE_PATH_MAX); /* 入力ファイル名（フルパス） */
        StringDto wk_sqlstr = new StringDto(64000);
        StringDto msg_param = new StringDto(C_const_MsgMaxLen);	/** APログ置換文字           **/



        /*#####################################*/
        /*	初期処理                       */
        /*#####################################*/
        /* 2022/11/09 MCCM初版 ADD START */
        rtn_status = 0;
        /* 2022/11/09 MCCM初版 ADD END */
        /*#####################################*/
        /*	プログラム名取得処理呼び出し   */
        /*#####################################*/
        rtn_cd = C_GetPgname(argv);
        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "%s:getPgname", Cg_Program_Name);
            APLOG_WT_903(msg_param, rtn_cd, 0);
            return exit(C_const_APNG);
        }

        /*#####################################*/
        /*	バッチデバッグ開始処理呼び出し */
        /*#####################################*/
        rtn_cd = C_StartBatDbg(argc, argv);
        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "%s:C_StartBatDbg", Cg_Program_Name);
            APLOG_WT_903(msg_param, rtn_cd, 0);
            return exit(C_const_APNG);
        }

        C_DbgStart("cmABteraB");

        /*#####################################*/
        /*	環境変数の取得                 */
        /*#####################################*/
        penv = getenv("CM_SERVER_ID"); /* サーバ識別 */
        if (StringUtils.isEmpty(penv)) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "getenv(%s)", "CM_SERVER_ID");
            APLOG_WT_903(msg_param, 0, 0);
            return exit(C_const_APNG);
        }

        if(DBG_main){
                C_DbgMsg("cmABteraB : getenv(CM_SERVER_ID)=%s\n", penv);
        }

        strcpy(wk_server_id, penv);
        if (wk_server_id[0].charAt(0) == 'D') {
//        if (wk_server_id[1] == 'D') {
//            memcpy(s_server_id, wk_server_id, 2);
//            s_server_id[2] = '\0';
            memcpy(s_server_id, wk_server_id[0], 2);
        }
        else {
            strcpy(s_server_id, "SD"); /* 顧客制度ＤＢをデファルトの接続先とする */
        }

        if(DBG_main){
                C_DbgMsg("cmABteraB : s_server_id=%s\n", s_server_id);
        }

            /*#####################################*/
            /*	引数の個数のチェック           */
            /*#####################################*/
            wk_cnt = 0;
        /* デバッグオプションのカウント */
        for (i = 1; i < argc; i++) {
            if (strcmp(argv[i],"-debug") == 0) wk_cnt++;
            if (strcmp(argv[i],"-DEBUG") == 0) wk_cnt++;
        }

        if ((3 <= (argc-wk_cnt)) && ((argc-wk_cnt) <= 9)) {
            /* OK */
        }
        else {
            /* ＡＰログ出力して終了 */
            APLOG_WT( "910", 0, s_server_id.arr, "コマンドライン引数が誤っています", 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*#####################################*/
        /*	主処理                         */
        /*#####################################*/

        if(DBG_main){
                C_DbgMsg("cmABteraB : %s\n", "主処理");
        }

        /* 引数解析処理を呼び出す */
        memset(wk_para, 0x00, sizeof(wk_para));
        rtn_cd = parseArg(argc, argv, wk_para);

        if(DBG_main){
                C_DbgMsg("cmABteraB : parseArg=%d\n", rtn_cd);
        }

        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            APLOG_WT( "910", 0, s_server_id.arr, "コマンドライン引数解析エラー", 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /*** 環境変数の取得 ***/
        penv = getenv("CM_APPARAM"); /* パラメータファイルの格納場所 */
        if (StringUtils.isEmpty(penv)) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "getenv(%s)", "CM_APPARAM");
            APLOG_WT_903(msg_param, 0, 0);
            return exit(C_const_APNG);
        }
        strcpy(wk_inp_file_dir, penv);

        /* 入力パラメータファイル名の取得 */
        memset(wk_inp_file_nam, 0x00, sizeof(wk_inp_file_nam));
        rtn_cd = getParamFileName(wk_para.in_file_name.strVal(), wk_inp_file_dir, wk_inp_file_nam);

        if(DBG_main){
                C_DbgMsg("cmABteraB : getParamFileName=%d\n", rtn_cd);
                C_DbgMsg("cmABteraB : getParamFileName=%s\n", wk_inp_file_nam);
        }

        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "入力パラメータファイル名の取得異常(%s)", wk_para.in_file_name);
            APLOG_WT( "911", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /* 入力パラメータファイルの解析 */
        memset(wk_sqlstr, 0x00, sizeof(wk_sqlstr));
        rtn_cd = readParamFile(wk_inp_file_nam.arr, wk_inp_file_dir.arr, wk_para, wk_sqlstr, wk_out_file, wk_out_rec);

        if(DBG_main){
                C_DbgMsg("cmABteraB : readParamFile=%d\n", rtn_cd);
        }

        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "入力パラメータファイルの解析異常(%s)", wk_inp_file_nam);
            APLOG_WT( "912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
            return exit(C_const_APNG);
        }

        /* 出力ファイルをオープンする */
        fo = fopen(wk_para.out_file_name.strDto(), SystemConstant.UTF8, FileOpenType.w);

//        if (fo == NULL) {
        if (fo.fd ==C_const_NG) {
            /* ＡＰログ出力して終了 */
            sprintf(msg_param, "fopen(%s)", wk_para.out_file_name);
            APLOG_WT_903(msg_param, 0, 0);
            return exit(C_const_APNG);
        }

        /* 出力ファイルにデータを出力する */
        rtn_cd = mainProc(fo, wk_out_file, wk_out_rec, wk_sqlstr, wk_para);

        if(DBG_main){
                C_DbgMsg("cmABteraB : mainProc=%d\n", rtn_cd);
        }

        if (rtn_cd != C_const_OK) {
            /* ＡＰログを出力して終了 */
            sprintf(msg_param, "データレコードの出力に失敗しました");
            APLOG_WT( "912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);

            fclose(fo);

            return exit(C_const_APNG);
        }

        /* 出力ファイルをクローズする */
        rtn_cd = fclose(fo);

        if(DBG_main){
                C_DbgMsg("cmABteraB : %s \n", "fclose");
        }



            /* 出力件数をＡＰログに出力する */
        CmABfuncLConstant.dbg_aplogwrite = 0;
        rtn_cd = APLOG_WT( "101", 0, s_server_id.arr, wk_para.out_file_name, wk_out_file.rec_cnt, 0, 0, 0, 0);
        CmABfuncLConstant.dbg_aplogwrite = 0;

        C_DbgEnd("cmABteraB", rtn_cd, rtn_status, 0);

        /*#####################################*/
        /*	バッチデバッグ終了処理呼び出し */
        /*#####################################*/
        rtn_cd = C_EndBatDbg();

        return exit(C_const_APOK);

    }

/******************************************************************************/
    /*                                                                            */
    /*	関数名 ： APLOG_WT                                                        */
    /*                                                                            */
    /*	書式                                                                      */
    /*	static int  APLOG_WT( char *msgid, int  msgidsbt, char *dbkbn,            */
    /*                            caddr_t param1, caddr_t param2, caddr_t param3, */
    /*                            caddr_t param4, caddr_t param5, caddr_t param6) */
    /*                                                                            */
    /*	【説明】                                                                  */
    /*              ＡＰログ出力を行う                                            */
    /*                                                                            */
    /*                                                                            */
    /*	【引数】                                                                  */
    /*		char       *	msgid      ：メッセージＩＤ                           */
    /*		int     	msgidsbt   ：メッセージ登録種別                           */
    /*		char       *	dbkbn      ：ＤＢ区分                                 */
    /*		caddr_t       	param1     ：置換変数１                               */
    /*		caddr_t       	param2     ：置換変数２                               */
    /*		caddr_t       	param3     ：置換変数３                               */
    /*		caddr_t       	param4     ：置換変数４                               */
    /*		caddr_t       	param5     ：置換変数５                               */
    /*		caddr_t       	param6     ：置換変数６                               */
    /*                                                                            */
    /*	【戻り値】                                                                */
    /*              0	： 正常                                                   */
    /*              1	： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  APLOG_WT( String msgid, int  msgidsbt, String dbkbn,
                          Object param1, Object param2, Object param3,
                          Object param4, Object param5, Object param6)
    {
        /*#####################################*/
        /*	ローカル変数定義               */
        /*#####################################*/
        String[]	out_flg    = new String[2];				/** APログフラグ             **/
        String[]	out_format = new String[C_const_MsgMaxLen];		/** APログフォーマット       **/
        IntegerDto	out_status ;				/** フォーマット取得結果     **/
        int	rtn_cd;					/** 関数戻り値               **/

        if( DBG_APLOG_WT){
        /*--------------------------------------------------------------------*/
            C_DbgStart( "APLOG_WT処理" );
        }	/*--------------------------------------------------------------------*/

        /*#####################################*/
        /*	APログフォーマット取得処理     */
        /*#####################################*/

        memset(out_flg, 0x00, sizeof(out_flg));
        memset(out_format, 0x00, sizeof(out_format));
        out_status = new IntegerDto();
        CmABfuncLConstant.dbg_getaplogfmt = 1;
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        CmABfuncLConstant.dbg_getaplogfmt = 0;
        if (DBG_APLOG_WT){
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtn_cd);
        }	/*--------------------------------------------------------------------*/

            /*#####################################*/
            /*	APログ出力処理                 */
            /*#####################################*/
            rtn_cd = C_APLogWrite(msgid, out_format, out_flg,
            param1, param2, param3, param4, param5, param6);
        if (DBG_APLOG_WT){
                /*--------------------------------------------------------------------*/
                C_DbgMsg( "*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd ) ;
                C_DbgEnd( "APLOG_WT処理", 0, 0, 0 );
        } /*---------------------------------------------------------------------*/

        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： parseArg                                                    */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int parseArg(int argc, char **argv, TERA_PARA *para)                  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              引数解析処理                                                  */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int             argc ： 引数の数                              */
    /*              char       *    argv ： 引数の配列                            */
    /*              TERA_PARA  *    para ： 解析結果を格納する                    */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int parseArg(int argc, String[] argv, TERA_PARA para)
    {
        int i;
        int rtn_cd;
        StringDto msg_param = new StringDto(200); /* ＡＰメッセージ用 */
        int numchk; /* 文字チェック用 */
        int ii;
        IntegerDto sts = new IntegerDto();
        StringDto wk_date = new StringDto(9);



        if (DBG_PARSE_PARAM){
                C_DbgMsg("cmABteraB : parseArg : %s\n", "start");
        }

        /* 出力引数の初期化 */
        memset(para, 0x00, 0);

        /* 引数の解析ループ */
        for (i = 1; i < argc; i++) {
            if (strcmp(argv[i], "-DEBUG") == 0 || strcmp(argv[i], "-debug") == 0) {
                /* 処理なし */
            }

            else if (memcmp(argv[i], "-P", 2) == 0) {
                /* パラメータファイル名 */
                if (strlen(argv[i]) == 11) {
                    /* OK */
                    /* 入力ファイル名を設定する */
                    strcpy(para.in_file_name, argv[i].substring(2));
                }
                else {
                    /* 桁数エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "入力ファイル名引数が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }
            }

            else if (memcmp(argv[i], "-F", 2) == 0) {
                /* 出力ファイル名 */
                if (strlen(argv[i]) > 2) {
                    /* OK */
                    /* 出力ファイル名を設定する */
                    strcpy(para.out_file_name, argv[i].substring(2));
                }
                else {
                    /* 桁数エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "出力ファイル名引数が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }
            }

            else if (memcmp(argv[i], "-T", 2) == 0) {
                /* テーブル用日付 */
                numchk = 0;
                if (strlen(argv[i]) == 10) {
                    for (ii = 2; ii < 10; ii++) {
                        if (isdigit(argv[i].charAt(ii)) != 0) {
                            /* OK */
                        }
                        else {
                            numchk = 1;
                            break;
                        }
                    }
                }

                if (strlen(argv[i]) == 10 && numchk == 0) {
                    /* OK */
                    /* 日付を設定する */
                    strcpy(para.table_name_ymd, argv[i].substring(2));
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "日付指定引数が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

            }

            else if (memcmp(argv[i], "-D", 2) == 0) {
                /* 日付 */
                numchk = 0;
                if (strlen(argv[i]) == 10) {
                    /* 文字種のチェック */
                    for (ii = 2; ii < 10; ii++) {
                        if (isdigit(argv[i].charAt(ii)) != 0) {
                            /* OK */
                        }
                        else {
                            numchk = 1;
                            break;
                        }
                    }
                }

                if (strlen(argv[i]) == 10 && numchk == 0) {
                    /* OK */
                    /* 日付を設定する */
                    strcpy(para.ymd, argv[i].substring(2));
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "日付指定引数が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

            }

            else if (memcmp(argv[i], "-C", 2) == 0) {
                /* 企業 */
                if (strcmp(argv[i], "-CSE") == 0) {
                    strcpy(para.corp_cd, "1113");
                }
                else if (strcmp(argv[i], "-CSO") == 0) {
                    strcpy(para.corp_cd, "1202");
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "企業指定引数が誤っています(%s)", argv[i]);
                    CmABfuncLConstant.dbg_aplogwrite = 1;
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    CmABfuncLConstant.dbg_aplogwrite = 0;
                    return C_const_NG;
                }

            }

            else if (memcmp(argv[i], "-E", 2) == 0) {
                /* 汎用 */
                numchk = strlen(argv[i]);
                if (2 < numchk && numchk < 257) {
                    /* OK */
                    strcpy(para.com_param, argv[i].substring(2));
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "汎用パラメータ指定が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

            }

            else if (memcmp(argv[i], "-S", 2) == 0) {
                /* 接続先ＤＢ */
                if (strcmp(argv[i], "-SMD") == 0 || strcmp(argv[i], "-SSD") == 0
                        || strcmp(argv[i], "-SHD") == 0 || strcmp(argv[i], "-SBD") == 0) {
                    /* OK */
                    strcpy(para.db_kbn, argv[i].substring(2));
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "接続先ＤＢ区分が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

            }

            else if (memcmp(argv[i], "-A", 2) == 0) {
                /* 改行指定 */
                if (strlen(argv[i]) == 2) {
                    /* OK */
                    para.kaigyo_kbn[0] = 0x0a;
                    para.kaigyo_kbn[1] = 0x00;
                }
                else {
                    /* エラー */
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "改行指定が誤っています(%s)", argv[i]);
                    APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    return C_const_NG;
                }

            }

            else {
                /* 不正なパラメータ */

                /* ＡＰログを出力する */
                sprintf(msg_param, "定義外の引数(%s)", argv[i]);
                APLOG_WT( "910", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
        }

        if (DBG_PARSE_PARAM){
                C_DbgMsg("cmABteraB : parseArg : %s\n", "loopend");
                C_DbgMsg("cmABteraB : parseArg : -E=[%s]\n", para.com_param);
        }

        /*** ＤＢ接続 ***/
        if (strlen(para.db_kbn) == 0) {
            /* ＤＢ区分の設定 */
            if (s_server_id.arr.charAt(1) == 'D') {
//            if (s_server_id[1] == 'D') {
                strcpy(para.db_kbn, s_server_id);
            }
            else {
                strcpy(para.db_kbn, "SD");
            }
        }


        if (DBG_PARSE_PARAM){
                C_DbgMsg("cmABteraB : parseArg : %s\n", "before connect");
        }

        CmABfuncLConstant.dbg_oradbconnect = 0;
        rtn_cd = C_OraDBConnect(para.db_kbn.strVal(), sts);
        CmABfuncLConstant.dbg_oradbconnect = 0;

        if (DBG_PARSE_PARAM){
                C_DbgMsg("cmABteraB : parseArg : connect=%d\n", rtn_cd);
        }

        if (rtn_cd != C_const_OK) {
            /* ＡＰログ出力して終了 */
            APLOG_WT_903("C_OraDBConnect", rtn_cd, sts);
            return C_const_NG;
        }

        /* バッチ日付の取得 */
        if (strlen(para.table_name_ymd) == 0 || strlen(para.ymd) == 0) {
            memset(wk_date, 0x00, sizeof(wk_date));
            rtn_cd = C_GetBatDate(0, wk_date, sts);
            if (rtn_cd != C_const_OK) {
                /* ＡＰログ出力して終了 */
                APLOG_WT_903("C_GetBatDate", rtn_cd, sts);
                return C_const_NG;
            }

            if (strlen(para.table_name_ymd) == 0) strcpy(para.table_name_ymd, wk_date);
            if (strlen(para.ymd) == 0)            strcpy(para.ymd,            wk_date);

        }

        if (DBG_PARSE_PARAM){
                C_DbgMsg("cmABteraB : parseArg : %s\n", "end");
        }
        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： getParamFileName                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int getParamFileName(char *file_id, char *file_dir, char *file_name)  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              入力パラメータファイル名取得処理                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    file_id   ： ファイルＩＤ                     */
    /*              char       *    file_dir  ： 検索対象にするディレクトリ       */
    /*              char       *    file_name ： 最新バージョンのファイル名を返す */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int getParamFileName(String file_id, StringDto file_dir, StringDto file_name)
    {
        LinkedList<File> dir;
        int ret;
        StringDto logbuf = new StringDto(200);
        File dent;
        int v1;
        int v2;



        /* 初期化 */
        strcpy(file_name, "");
        /* 2022/11/09 MCCM初版 ADD START */
        ret = 0;
        /* 2022/11/09 MCCM初版 ADD END */
        /*#####################################*/

        /* ディレクトリのオープン */
        dir = opendir(file_dir.arr);
        if (dir == null) {
            sprintf(logbuf, "opendir(%s)", file_dir);
            ret = APLOG_WT_903(logbuf, 0, 0);
            return C_const_NG;
        }

        for ( ; ; ) {
            dent = readdir(dir);
            if (dent == null) break;

            if (strlen(dent.getName()) == 12) {
                if (memcmp(dent.getName(), file_id, 9) == 0) {
                    /* ９桁目まで一致する */
                    if (strlen(file_name) > 0) {
                        v1 = atoi(file_name.substring(9));
                        v2 = atoi(dent.getName().substring(9));

                        if (v1 < v2) {
                            /* バージョンが大きい */
                            strcpy(file_name, dent.getName());
                        }
                    }
                    else {
                        strcpy(file_name, dent.getName());
                    }
                }
            }
        }

        closedir(dir);

        if (strlen(file_name) == 0) return C_const_NG; /* 該当なし */

        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： readParamFile                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  readParamFile(char *filename, char *filedir, TERA_PARA *para,    */
    /*             char *sqlstr, TERA_OUT_FILE *out_file, TERA_OUT_REC *out_rec)  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              入力パラメータファイル解析処理                                */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char            *    filename ： ファイル名                   */
    /*              char            *    filedir  ： ディレクトリ名               */
    /*              TERA_PARA       *    para     ： 引数パラメータ               */
    /*              char            *    sqlstr   ： select文を生成して格納       */
    /*              TERA_OUT_FILE   *    out_file ： ファイル情報を格納           */
    /*              TERA_OUT_REC    *    out_rec  ： レコード情報を格納           */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int readParamFile(String filename, String filedir, TERA_PARA para, StringDto sqlstr, TERA_OUT_FILE out_file, TERA_OUT_REC out_rec)
    {
        FileStatusDto fi;
        StringDto fname = new StringDto(TERA_FILE_PATH_MAX);
        StringDto logbuf =new StringDto(200);
        int ret;
        StringDto buf =new StringDto(4096);

        IntegerDto out_cnt = new IntegerDto();
        String[] out_str = new String[512];
//        char p;
        StringDto msg_param = new StringDto(200); /* ＡＰメッセージ用 */
        StringDto hensyu_taisyo = new StringDto(20);
        StringDto wk_table = new StringDto(512);
        StringDto wk_table_add = new StringDto(20);
        StringDto wk_where = new StringDto(64000);
        int rep_para_cnt; /* 置換パラメータ数 */
        int rep_para_pos; /* 置換パラメータ位置 */
        String[] rep_para_arr = new String[256]; /* 置換パラメータ配列 */
        int loop_idx;
        int loop_ix2;
        int err_flg;
        int ii;



        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : %s\n", "start");
        }

        /* 初期化 */
        strcpy(sqlstr, "");

        out_file.rec_len.arr = 0;
        out_file.fld_cnt.arr = 0;
        out_file.rec_cnt.arr = 0;

        /* 入力ファイルのオープン */
        sprintf(fname, "%s/%s", filedir, filename);
//        fi = fopen(fname, "r");
        fi = fopen(fname.arr,  FileOpenType.r);
        if (fi == null) {
            sprintf(logbuf, "fopen(%s)", fname);
            ret = APLOG_WT_903(logbuf, 0, 0);
            return C_const_NG;

        }


        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : %s\n", "file open");
        }

        for ( ; ; ) {
            if (feof(fi) != 0) break;

            memset(buf, 0x00, sizeof(buf));
            fgets(buf, sizeof(buf), fi);
            if (buf == null) break;

            if (DBG_READ_PARAM_FILE){
                        C_DbgMsg("cmABteraB : readParamFile : read[%s]\n", buf);
            }

            ArrayList<String> readOutStr = new ArrayList<>();
            ret = splitRec(buf.arr, out_cnt, readOutStr);
            out_str = readOutStr.toArray(String[]::new);
            if (DBG_READ_PARAM_FILE){
                        C_DbgMsg("cmABteraB : readParamFile : splitRec=%d\n", ret);
                        C_DbgMsg("cmABteraB : readParamFile : out_cnt=%d\n", out_cnt);
                for (ii = 0; ii < out_cnt.arr; ii++) {
                            C_DbgMsg("cmABteraB : readParamFile : out_str=[%s]\n", out_str[ii]);
                        }
            }

            if (out_cnt.arr == 0) {
                continue;
            } else if (out_cnt.arr == 2 && strcmp(out_str[0], "FILE") == 0) {
                /* out_str[1]の数値チェック */
                ret = C_const_OK;
//                p = out_str[1];
                String v = out_str[1];
                for (char c : v.toCharArray()) {
                    if (isdigit(c) == 0) {
                        ret = C_const_NG;
                        break;
                    }
                }

                if (ret != C_const_OK) {
                    /* ＡＰログを出力する */
                    sprintf(msg_param, "パラメータファイル不正（レコード長）%s", out_str[1]);
                    APLOG_WT("912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);

                    fclose(fi);

                    return C_const_NG;
                }

                /* レコード長を設定する */
                out_file.rec_len.arr = atoi(out_str[1]);

            } else if (out_cnt.arr > 0 && strcmp(out_str[0], "DATA") == 0) {
                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : [%s]\n", "DATA");
                }
                strcpy(hensyu_taisyo, "DATA");

                /* 出力レコード情報構造体の初期化をする */
                out_rec.memset();
                out_rec.offset[out_file.fld_cnt.intVal()].arr = 0;
                out_rec.length[out_file.fld_cnt.intVal()].arr = 0;
                memset(out_rec.dattyp[out_file.fld_cnt.intVal()], 0x00, TERA_DATATYPE_LEN+1);
                memset(out_rec.komoku[out_file.fld_cnt.intVal()], 0x00, TERA_KOMOKUMEI_LEN+1);
                out_rec.sjiscv[out_file.fld_cnt.intVal()].arr= 0;
                out_rec.encdec[out_file.fld_cnt.intVal()].arr= 0;

            } else if (out_cnt.arr > 0 && strcmp(out_str[0], "TABLE") == 0) {

                strcpy(hensyu_taisyo, "TABLE");
                memset(wk_table, 0x00, sizeof(wk_table));

            } else if (out_cnt.arr > 0 && strcmp(out_str[0], "WHERE") == 0) {
                strcpy(hensyu_taisyo, "WHERE");

                memset(wk_where, 0x00, sizeof(wk_where)); /* 抽出条件 */

                rep_para_cnt = 0;
                rep_para_pos = 0;
                memset(rep_para_arr[0], 0x00, 128);

                for (loop_idx = 0; loop_idx < out_cnt.arr; loop_idx++) {
                    if (strcmp(out_str[loop_idx], "KEY=DATE") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]=   strcpy(rep_para_arr[rep_para_cnt-1], para.ymd.strVal());
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=YEAR") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]=  memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(0), 4);
//                        rep_para_arr[rep_para_cnt-1][4] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],4,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=MONTH") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]=  memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(4), 2);
//                        rep_para_arr[rep_para_cnt-1][2] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],2,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=DAY") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]=   memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(6), 2);
//                        rep_para_arr[rep_para_cnt-1][2] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],2,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "%S") == 0) {
                        /* 抽出条件に追加 */
                        strcat(wk_where, rep_para_arr[rep_para_pos]);
                        strcat(wk_where, " ");
                        rep_para_pos++;

                        /* MAXを超えた場合はMAXにする */
                        if (rep_para_pos > rep_para_cnt - 1) rep_para_pos = rep_para_cnt - 1;

                    }

                    else if (strcmp(out_str[loop_idx], "%C") == 0) {
                        /* 企業 */
                        if (strlen(para.corp_cd) > 0) {
                            /* 抽出条件に追加 */
                            strcat(wk_where, para.corp_cd.strDto());
                            strcat(wk_where, " ");
                        }

                    }

                    else if (strcmp(out_str[loop_idx], "%E") == 0) {
                        if (strlen(para.com_param) > 0) {
                            /* 抽出条件に追加 */
                            strcat(wk_where, para.com_param.strDto());
                            strcat(wk_where, " ");
                        }

                    }

                    else {
                        strcat(wk_where, out_str[loop_idx]);
                        strcat(wk_where, " ");
                    }

                }

            } /* WHERE end */ else if (out_cnt.arr > 0 && strcmp(out_str[0], "END") == 0) {
                /* ループを抜ける */
                break;

            } else if (out_cnt.arr == 6 && strcmp(hensyu_taisyo, "DATA") == 0) {
                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : [%s]\n", "DATA2");
                }
                        /* DATAの２行目以降 */

                        /*** エラーチェック ***/
                        err_flg = 0;

                /* オフセット数値チェック */
                if (err_flg == 0) {
//                    p = out_str[0];
                    String v = out_str[0];
                    for (char c : v.toCharArray()) {
                        if (isdigit(c) == 0) {
                            err_flg = 1;
                            break;
                        }
                    }
                }
                /* レングス数値チェック */
                if (err_flg == 0) {
//                    p = out_str[1];
                    String v = out_str[1];
                    for (char c : v.toCharArray()) {
                        if (isdigit(c) == 0) {
                            err_flg = 2;
                            break;
                        }
                    }
                }

                /* データタイプのチェック */
                if (err_flg == 0) {
                    err_flg = 3;
                    if      (strcmp(out_str[2], TERA_DATATYPE_U)  == 0) err_flg = 0;
                    else if (strcmp(out_str[2], TERA_DATATYPE_X)  == 0) err_flg = 0;
                        /* 2022/11/09 MCCM初版 ADD START */
                    else if ( (strcmp(out_str[2], TERA_DATATYPE_MP1)  == 0)
                            || (strcmp(out_str[2], TERA_DATATYPE_MP2)  == 0)
                            || (strcmp(out_str[2], TERA_DATATYPE_SD)  == 0)
                            || (strcmp(out_str[2], TERA_DATATYPE_GL)  == 0) ){
                        err_flg = 0;
                    }
                    /* 2022/11/09 MCCM初版 ADD END */
                }

                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : err_flg=[%d]\n", err_flg);
                }

                if (err_flg != 0) {
                    /* パラメータファイルの定義が不正 */
                    /* ＡＰログを出力する */
                    if (err_flg == 1) {
                        sprintf(msg_param, "パラメータファイル不正（オフセット）[%s]", out_str[0]);
                    }
                    else if (err_flg == 2) {
                        sprintf(msg_param, "パラメータファイル不正（レングス）[%s]", out_str[1]);
                    }
                    else if (err_flg == 3) {
                        sprintf(msg_param, "パラメータファイル不正（データタイプ）[%s]", out_str[2]);
                    }

                    APLOG_WT( "912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                    fclose(fi);
                    return C_const_NG;

                }

                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : [%s]\n", "DATA2 dataset start");
                }

                out_rec.offset[out_file.fld_cnt.intVal()].arr = atoi(out_str[0]);  /* オフセット */
                out_rec.length[out_file.fld_cnt.intVal()].arr = atoi(out_str[1]);  /* レングス */
                strcpy(out_rec.dattyp[out_file.fld_cnt.intVal()], out_str[2]); /* データタイプ */
                strcpy(out_rec.komoku[out_file.fld_cnt.intVal()], out_str[3]); /* 項目名 */

                if (strcmp("sjis",out_str[4]) == 0) out_rec.sjiscv[out_file.fld_cnt.intVal()].arr = 1;
                else                                out_rec.sjiscv[out_file.fld_cnt.intVal()].arr = 0;

                if (strcmp("yes",out_str[5]) == 0)  out_rec.encdec[out_file.fld_cnt.intVal()].arr = 1;
                else                                out_rec.encdec[out_file.fld_cnt.intVal()].arr = 0;

                out_file.fld_cnt.arr = out_file.fld_cnt.intVal() +1 ; /* レコード項目数を加算する */

                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : [%s]\n", "DATA2 clear next recinfo start");
                }

                /* 次のレコード情報の初期化 */
                out_rec.offset[out_file.fld_cnt.intVal()].arr = 0;
                out_rec.length[out_file.fld_cnt.intVal()].arr = 0;
                memset(out_rec.dattyp[out_file.fld_cnt.intVal()], 0x00, TERA_DATATYPE_LEN+1);
                memset(out_rec.komoku[out_file.fld_cnt.intVal()], 0x00, TERA_KOMOKUMEI_LEN+1);
                out_rec.sjiscv[out_file.fld_cnt.intVal()].arr = 0;
                out_rec.encdec[out_file.fld_cnt.intVal()].arr = 0;

                if (DBG_READ_PARAM_FILE){
                                C_DbgMsg("cmABteraB : readParamFile : [%s]\n", "DATA2 end");
                }

            } /* DATA2 end */ else if ((out_cnt.arr == 2 || out_cnt.arr == 3) && strcmp(hensyu_taisyo, "TABLE") == 0) {
                /* TABLEの２行目以降 */

                if (strlen(wk_table) == 0 && out_cnt.arr == 2) {
                    sprintf(wk_table, " FROM %s %s", out_str[0], out_str[1]);
                } else if (strlen(wk_table) > 0 && out_cnt.arr == 2) {
                    strcat(wk_table, ",");
                    strcat(wk_table, out_str[0]);
                    strcat(wk_table, " ");
                    strcat(wk_table, out_str[1]);
                } else if (out_cnt.arr == 3) {
                    memset(wk_table_add, 0x00, sizeof(wk_table_add));

                    if (strcmp(out_str[1], "%DATE") == 0) {
                        memcpy(wk_table_add, para.table_name_ymd, 8);
                    }
                    else if (strcmp(out_str[1], "%YEAR") == 0) {
                        memcpy(wk_table_add, para.table_name_ymd, 4);
                    }
                    else if (strcmp(out_str[1], "%MONTH") == 0) {
                        memcpy(wk_table_add, para.table_name_ymd, 6);
                    }
                    else {
                        /* パラメータファイルの定義が不正 */
                        /* ＡＰログを出力する */
                        sprintf(msg_param, "パラメータファイル不正（定義外）[%s]", out_str[1]);
                        APLOG_WT( "912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);
                        fclose(fi);
                        return C_const_NG;
                    }

                    if (DBG_READ_PARAM_FILE){
                                        C_DbgMsg("cmABteraB : readParamFile : wk_table_add=[%s]\n", wk_table_add);
                    }

                    if (strlen(wk_table) == 0) {
                        sprintf(wk_table, " FROM %s%s %s", out_str[0], wk_table_add, out_str[2]);
                    }
                    else {
                        strcat(wk_table, ",");
                        strcat(wk_table, out_str[0]);
                        strcat(wk_table, wk_table_add);
                        strcat(wk_table, " ");
                        strcat(wk_table, out_str[2]);
                    }
                }

            } /* TABLE2 end */ else if (out_cnt.arr > 0 && strcmp(hensyu_taisyo, "WHERE") == 0) {
                rep_para_cnt = 0;
                rep_para_pos = 0;
                /* WHEREの２行目以降 */
                for (loop_idx = 0; loop_idx < out_cnt.arr; loop_idx++) {
                    if (strcmp(out_str[loop_idx], "KEY=DATE") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]= strcpy(rep_para_arr[rep_para_cnt-1], para.ymd.strVal());
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=YEAR") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]=  memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(0), 4);
//                        rep_para_arr[rep_para_cnt-1][4] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],4,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=MONTH") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]= memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(4), 2);
//                        rep_para_arr[rep_para_cnt-1][2] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],2,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "KEY=DAY") == 0) {
                        rep_para_cnt++;
                        rep_para_arr[rep_para_cnt-1]= memcpy(rep_para_arr[rep_para_cnt-1],
                                para.ymd.strVal().substring(6), 2);
//                        rep_para_arr[rep_para_cnt-1][2] = '\0';
                        rep_para_arr[rep_para_cnt-1] = setChartAt(rep_para_arr[rep_para_cnt-1],2,'\0');
                        memset(rep_para_arr[rep_para_cnt], 0x00, 128);
                    }

                    else if (strcmp(out_str[loop_idx], "%S") == 0) {
                        /* 抽出条件に追加 */
                        strcat(wk_where, rep_para_arr[rep_para_pos]);
                        strcat(wk_where, " ");
                        rep_para_pos++;

                        /* MAXを超えた場合はMAXにする */
                        if (rep_para_pos > rep_para_cnt - 1) rep_para_pos = rep_para_cnt - 1;

                    }

                    else if (strcmp(out_str[loop_idx], "%C") == 0) {
                        /* 企業 */
                        if (strlen(para.corp_cd) > 0) {
                            /* 抽出条件に追加 */
                            strcat(wk_where, para.corp_cd.strVal());
                            strcat(wk_where, " ");
                        }

                    }

                    else if (strcmp(out_str[loop_idx], "%E") == 0) {
                        if (strlen(para.com_param) > 0) {
                            /* 抽出条件に追加 */
                            strcat(wk_where, para.com_param.strVal());
                            strcat(wk_where, " ");
                        }

                    }

                    else if (strcmp(out_str[loop_idx], "'%E'") == 0) {
                        if (strlen(para.com_param) > 0) {
                            /* 抽出条件に追加 */
                            strcat(wk_where, "'");
                            strcat(wk_where, para.com_param.strVal());
                            strcat(wk_where, "' ");
                        }

                    }

                    else {
                        strcat(wk_where, out_str[loop_idx]);
                        strcat(wk_where, " ");
                    }
                }
            } /* WHERE2 end */

            else {
                /* エラー */
                /* ＡＰログを出力する */
                sprintf(msg_param, "パラメータファイル不正（定義外）[%s]", out_str[1]);
                APLOG_WT( "912", 0, s_server_id.arr, msg_param, 0, 0, 0, 0, 0);

                fclose(fi);

                return C_const_NG;

            }
        }

        fclose(fi);

        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : %s\n", "loopend");
                C_DbgMsg("cmABteraB : readParamFile : out_file->rec_len=%d\n", out_file.rec_len);
                C_DbgMsg("cmABteraB : readParamFile : out_file->fld_cnt=%d\n", out_file.fld_cnt);
        }

        /* 不正（０件）の場合は処理を終了する */
        /*if (out_file->rec_len == 0*/
        if (out_file.fld_cnt.intVal() == 0
                || strlen(wk_table)  == 0) {
            return C_const_NG;

        }

        /* 実行ＳＱＬ文を生成する */
        strcpy(sqlstr, "select  ");

        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : out_file->fld_cnt=%d\n", out_file.fld_cnt);
        }
        for (loop_ix2 = 0; loop_ix2 < out_file.fld_cnt.intVal(); loop_ix2++) {
            if (DBG_READ_PARAM_FILE){
                        C_DbgMsg("cmABteraB : readParamFile : add komoku [%s]\n", out_rec.komoku[loop_ix2]);
            }
            strcat(sqlstr, out_rec.komoku[loop_ix2].strVal());
            if (loop_ix2 < out_file.fld_cnt.intVal() - 1) {
                strcat(sqlstr, ", ");
            }
            else {
                strcat(sqlstr, " ");
            }
        }

        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : wk_table=[%s]\n", wk_table);
                C_DbgMsg("cmABteraB : readParamFile : wk_where=[%s]\n", wk_where);
        }

        strcat(sqlstr, wk_table);
        strcat(sqlstr, " ");
        strcat(sqlstr, wk_where);

        if (DBG_READ_PARAM_FILE){
                C_DbgMsg("cmABteraB : readParamFile : sqlstr=[%s]\n", sqlstr);
        }

        return C_const_OK;

    }



/******************************************************************************/
    /*                                                                            */
    /*      関数名 ：mainProc                                                     */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int  int mainProc(FILE *fo, TERA_OUT_FILE *out_file,                  */
    /*                 TERA_OUT_REC *out_rec, char *sqlstr, TERA_PARA *t_para)    */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              連動ファイル出力処理                                          */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              FILE             * fo ： 出力ファイルのファイルポインタ       */
    /*              TERA_OUT_FILE    * out_file ： 出力ファイル情報               */
    /*              TERA_OUT_REC     * out_rec ： 出力レコード情報                */
    /*              char             * sqlstr ： ＳＱＬ文                         */
    /*              TERA_PARA        * t_para ： 引数パラメータ                   */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int mainProc(FileStatusDto fo, TERA_OUT_FILE out_file, TERA_OUT_REC out_rec, StringDto sqlstr, TERA_PARA t_para)
    {
//        SQLDA *sele_dp; /* 動的ＳＱＬの記述子 */
        int wk_size;
        int wk_name_len;
        int wk_ind_name_len;
        int ret = 0;
        StringDto msg_str=new StringDto(200);
        int loop_ix;
        int wk_typ;
        int wk_prec=0;
        int wk_scale =0;
        StringDto fld_buf=new StringDto(4096);
        StringDto edt_buf=new StringDto(4096);
        StringDto out_buf=new StringDto(4096);
        int  wk_len;
        int pind;
        int dtype =0;
        int nullok ;



        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : %s\n", "start");
        }

        /* ワーク変数の初期化 */
        out_file.rec_cnt.arr = 0;

        /* ホスト変数の編集 */
        strcpy(H_SQLSTR, sqlstr);
        H_SQLSTR.len = strlen(sqlstr);

        /*** 動的ＳＱＬの記述子を生成する ***/
        wk_size         = TERA_COLUMN_COUNT_MAX;
        wk_name_len     = TERA_COLUMN_COUNT_MAX;
        wk_ind_name_len = TERA_COLUMN_COUNT_MAX;
        SQLDA sele_dp = new SQLDA();
        if (sele_dp == null) {
            ret = APLOG_WT_903("SQLSQLDAAlloc()", 0, 0);

//            EXEC SQL ROLLBACK;
            sqlca.rollback();
            return C_const_NG;
        }

        sqlca.sql =H_SQLSTR;
        sqlca.prepare();
        /*** 動的ＳＱＬ文の解析(PREPARE) ***/
//        EXEC SQL PREPARE sql_main FROM :H_SQLSTR;
        if (sqlca.sqlcode != C_const_OK) {
            memset(msg_str, 0x00, sizeof(msg_str));
            sprintf(msg_str, "Parse error offset[%d]", sqlca.sqlcode);
            ret = APLOG_WT_902(sqlca.sqlcode, msg_str.arr);

            sqlca.rollback();
//            EXEC SQL ROLLBACK;

            return C_const_NG;
        }

        /*** カーソル宣言 ***/
        sqlca.declare();
//        EXEC SQL DECLARE cur_main CURSOR for sql_main;
        if (sqlca.sqlcode != C_const_OK) {
            ret = APLOG_WT_902(sqlca.sqlcode, "CURSOR ERR");
            sqlca.rollback();

            return C_const_NG;
        }

        /*** カーソルのオープン ***/
//        EXEC SQL OPEN cur_main;
        sqlca.open();
        if (sqlca.sqlcode != C_const_OK) {
            ret = APLOG_WT_902(sqlca.sqlcode, "CURSOR OPEN ERR");

//            EXEC SQL ROLLBACK;
sqlca.rollback();
            return C_const_NG;
        }

        sele_dp.init(sqlca);
        /*** 動的ＳＱＬ情報初期化 ***/
//        sqlca.fetchInto();
        sele_dp.N = TERA_COLUMN_COUNT_MAX;
//        EXEC SQL DESCRIBE SELECT LIST FOR sql_main INTO sele_dp;
        if (sqlca.sqlcode != C_const_OK) {
            ret = APLOG_WT_902(sqlca.sqlcode, "DESCRIBE SELECT LIST ERR");

            sqlca.rollback();
//            EXEC SQL ROLLBACK;

            return C_const_NG;
        }

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : sele_dp->F=%d\n", sele_dp.F);
        }

        /*** 項目数の設定 ***/
        sele_dp.N = sele_dp.F; /* 実際に取得した項目数にする */

//        sqlca.fetch();
        /*** 各項目のデータ型とデータ長を取得する ***/
        for (loop_ix = 0; loop_ix < sele_dp.F; loop_ix++) {
            /*** NULL/NOT NULL データ型の処理を行う ***/
            sele_dp.I[loop_ix] = 0;

            if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : type(before)=%d\n", Math.abs(sele_dp.T[loop_ix]));
            }

            dtype =  sele_dp.T[loop_ix] ;
            nullok =  sele_dp.I[loop_ix] ;
//            SQLColumnNullCheck(0,  sele_dp.T[loop_ix] , dtype, nullok);
            if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : nullok=%d\n", nullok);
            }
//            if (nullok) SQLColumnNullCheck(0, (unsigned short *)(&(sele_dp.T[loop_ix])), (unsigned short *)(&(sele_dp.T[loop_ix])), &nullok);
            if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : nullok=%d\n", nullok);
            }


            if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : type(after)=%d\n", sele_dp.T[loop_ix]);
            }
            wk_typ = sele_dp.T[loop_ix];

            if (wk_typ == Types.CHAR) {
                /* CHAR 処理なし 1 */
            }
            else if (wk_typ == Types.NUMERIC) {
                /* NUMBER 2*/

                /* 精度と位取りの抽出 */
//                SQLNumberPrecV6((void *)0, (unsigned int *)(&(sele_dp.L[loop_ix])), &wk_prec, &wk_scale);

                if(DBG_MAIN_PROC){
                    C_DbgMsg("cmABteraB : mainProc : wk_prec=%d\n", wk_prec);
                    C_DbgMsg("cmABteraB : mainProc : wk_scale=%d\n", wk_scale);
                }

                /* 精度が０の場合は、４０に置き換える */
                if (wk_prec == 0) wk_prec = 40;

                /* 位取りが０より大きい場合、列値の長さ(L)にsizeof(float)を設定する */
                /* 位取りが０以下の場合、列値の長さ(L)にsizeof(int)を設定する */
                if (wk_scale > 0) {
//                    sele_dp.L[loop_ix] = 0;  /* sizeof(int)から変更 */
                }
                else {
//                    sele_dp.L[loop_ix] = 0; /* sizeof(int)から変更 */
                }

            }

            else if (wk_typ == Types.DOUBLE) {
                /* LONG 8*/

                /* 列値の長さ(L)に240を設定する */
                sele_dp.L[loop_ix] = 240;
            }

            else if (wk_typ ==  Types.ROWID) {
                /* ROWID 11*/

                /* 列値の長さ(L)に18を設定する */
                sele_dp.L[loop_ix] = 18;
            }

            else if (wk_typ ==  Types.DATE) {
                /* DATE 12*/

                /* 列値の長さ(L)に9を設定する */
                sele_dp.L[loop_ix] = 9;
            }

            else if (wk_typ ==  Types.BLOB) {
                /* RAW 処理なし 23*/

            }

            else if (wk_typ == Types.CLOB) {
                /* LONG RAW 24*/

                /* 列値の長さ(L)に240を設定する */
                sele_dp.L[loop_ix] = 240;
            }

            wk_typ = sele_dp.T[loop_ix];

            if (wk_typ != Types.NUMERIC && wk_typ != Types.CLOB) {
                /* CHARに設定しなおす */
                sele_dp.T[loop_ix] = Types.CHAR; /* CHAR */
                sele_dp.L[loop_ix] = sele_dp.L[loop_ix] * 3;
            }

            /* 列値を格納するバッファをアロケーションする */
            if (sele_dp.T[loop_ix] == Types.NUMERIC) {
              //  sele_dp.V[loop_ix] = malloc(sele_dp.L[loop_ix]);
            }
            else {
              //  sele_dp.V[loop_ix] = malloc(sele_dp.L[loop_ix] + 1); /* １桁多く確保する */
            }

            if (sele_dp.T[loop_ix] == Types.NUMERIC) {
                /* 数値型の場合、データ型を設定しなおす */
                if (wk_scale > 0) {
                    sele_dp.T[loop_ix] = Types.FLOAT; /* float */
                }
                else {
                    sele_dp.T[loop_ix] = Types.INTEGER; /* int */
                }
            }
        }

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : %s\n", "loop start");
        }
//        sqlca.curse_close();
//        sqlca.restAndExecute();
        for ( ; ; ) {
            memset(out_buf, 0x00, sizeof(out_buf));

            if(DBG_MAIN_PROC){
                        C_DbgMsg("cmABteraB : mainProc : %s\n", "fetch start");
            }
            /*** カーソルのフェッチ ***/

//            EXEC SQL FETCH cur_main USING DESCRIPTOR sele_dp;
            sqlca.fetch();
            if(DBG_MAIN_PROC){
                        C_DbgMsg("cmABteraB : mainProc : (FETCH) sqlca.sqlcode=%d\n", sqlca.sqlcode);
            }


            if (sqlca.sqlcode == C_const_Ora_OK) {

                sele_dp.updateData(sqlca.currentRow);

                if(DBG_MAIN_PROC){
                                C_DbgMsg("cmABteraB : mainProc : %s\n", "fetch OK");
                }

                /* 入力引数の文字列を編集する */
                for (loop_ix = 0; loop_ix < sele_dp.F; loop_ix++) {
                    memset(fld_buf, 0x00, sizeof(fld_buf));

                    pind   = sele_dp.I[loop_ix];
                    if(DBG_MAIN_PROC){
                                        /* if (pind != NULL) C_DbgMsg("cmABteraB : mainProc : *pind=%d\n", *pind); */
                                        /* else              C_DbgMsg("cmABteraB : mainProc : pind=%s\n", "null"); */
                    }
                    wk_typ = sele_dp.T[loop_ix];
                    wk_len = sele_dp.L[loop_ix];

                    if(DBG_MAIN_PROC){
                                        C_DbgMsg("cmABteraB : mainProc : loop_ix=%d\n", loop_ix);
                                        C_DbgMsg("cmABteraB : mainProc : ind=%d\n",  pind);
                                        C_DbgMsg("cmABteraB : mainProc : typ=%d\n", wk_typ);
                                        C_DbgMsg("cmABteraB : mainProc : len=%d\n", wk_len);
                    }

                    if ( pind < 0) {
                        /* nullの場合 スペースで埋める */
                        if (wk_typ == Types.FLOAT) {
                            sprintf(fld_buf, "%-"+( wk_len + 3)+"c",  ' ');
                        }
                        else {
                            /*  */
                            sprintf(fld_buf, "%-"+wk_len+"c",  ' ');
                        }
                    }

                                else {
                        if (wk_typ == Types.INTEGER) {
                            /* int -- wk_lenはsizeof(long)固定 */
                            sprintf(fld_buf, "%"+ out_rec.length[loop_ix]+"d",  sele_dp.V[loop_ix]);
                            /* sprintf(fld_buf, "%*ld", wk_len, *((long *)(sele_dp.V[loop_ix]))); */
                        }
                        else if (wk_typ == Types.FLOAT) {
                            /* float  -- wk_lenはsizeof(double)固定 */
                            sprintf(fld_buf, "%"+out_rec.length[loop_ix]+".2f" , (sele_dp.V[loop_ix]));
                            /* sprintf(fld_buf, "%*.2f", wk_len, *((double *)(sele_dp.V[loop_ix]))); */
                        }
                        else {
                            /* else */
                            if(DBG_MAIN_PROC){
                                                        C_DbgMsg("cmABteraB : mainProc : V=[%s]\n", sele_dp.V[loop_ix]);
                            }
                            /* sprintf(fld_buf, "%-*.*s", sele_dp.L[loop_ix], sele_dp.L[loop_ix], sele_dp.V[loop_ix]); */
                            /* sprintf(fld_buf, "%-*.*s", wk_len, wk_len, sele_dp.V[loop_ix]); */
                            /* sprintf(fld_buf, "%s", sele_dp.V[loop_ix]); */
                            sprintf(fld_buf, "%-"+wk_len+"."+wk_len+"s",  sele_dp.V[loop_ix]);
                            /* fld_buf[out_rec.length[loop_ix]] = '\0'; */
                        }

                    }

                    if(DBG_MAIN_PROC){
                                        C_DbgMsg("cmABteraB : mainProc : %s\n", "start edit");
                                        C_DbgMsg("cmABteraB : mainProc : dattyp=%s\n", out_rec.dattyp[loop_ix]);
                                        C_DbgMsg("cmABteraB : mainProc : fld_buf=[%s]\n", fld_buf);
                    }

                    /*** 数値型の編集 ***/

                    memset(edt_buf, 0x00, sizeof(edt_buf));

                    if (strcmp(out_rec.dattyp[loop_ix].strDto(), "U") == 0) {
                        /* 編集処理呼び出し */
                        ret = editUtype(fld_buf.strVal(), out_rec.length[loop_ix].intVal(), edt_buf);
                        if(DBG_MAIN_PROC){
                                                C_DbgMsg("cmABteraB : mainProc : %s\n", "editUtype");
                                                C_DbgMsg("cmABteraB : mainProc : before[%s]\n", fld_buf);
                                                C_DbgMsg("cmABteraB : mainProc : after [%s]\n", edt_buf);
                        }
                    }
                    else if (strcmp(out_rec.dattyp[loop_ix].strDto(), "X") == 0) {
                        /* 編集処理呼び出し */



                        ret = editXtype(fld_buf, out_rec.length[loop_ix].intVal(), edt_buf, out_rec.sjiscv[loop_ix].intVal(), out_rec.encdec[loop_ix].intVal());



                        if(DBG_MAIN_PROC){
                                                C_DbgMsg("cmABteraB : mainProc : strlen(edt_buf)=[%d]\n", strlen(edt_buf));
                        }
                    }
                    /* 2022/11/09 MCCM初版 ADD START */
                    else if ( (strcmp(out_rec.dattyp[loop_ix].strDto(), TERA_DATATYPE_MP1) == 0)
                            || (strcmp(out_rec.dattyp[loop_ix].strDto(), TERA_DATATYPE_MP2) == 0)
                            || (strcmp(out_rec.dattyp[loop_ix].strDto(), TERA_DATATYPE_SD) == 0)
                            || (strcmp(out_rec.dattyp[loop_ix].strDto(), TERA_DATATYPE_GL) == 0) ){

                        /* 暗号化タイプ編集処理呼び出し */
                        ret = editEDtype(fld_buf, out_rec.dattyp[loop_ix].strVal(), out_rec.length[loop_ix].intVal(), edt_buf, out_rec.encdec[loop_ix].intVal());

                        if(DBG_MAIN_PROC){
                                                C_DbgMsg("cmABteraB : mainProc : strlen(edt_buf)=[%d]\n", strlen(edt_buf));
                        }
                    }
                    /* 2022/11/09 MCCM初版 ADD END */

                    if (ret != C_const_OK) {
                        /* エラー */

                        return C_const_NG;

                    }

                    /* ファイル出力用バッファに、データを乗せかえる */
                    strncat( out_buf, edt_buf.strVal(), strlen(edt_buf));

                } /* col loop end */

                if(DBG_MAIN_PROC){
                                C_DbgMsg("cmABteraB : mainProc : %s\n", "fwrite start");
                }

                /*** １レコードファイル出力 ***/
                if (t_para.kaigyo_kbn[0] != '\0') {
                    /* 改行コードを付加する */
                    strcat(out_buf, String.valueOf(t_para.kaigyo_kbn[0]));
                    fwrite(out_buf, strlen(out_buf), 1, fo);
                }
                else {
                    fwrite(out_buf, strlen(out_buf), 1, fo);
                }

                fflush(fo);

                out_file.rec_cnt.arr = out_file.rec_cnt.intVal() + 1;

            }

            else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

                if(DBG_MAIN_PROC){
                                C_DbgMsg("cmABteraB : mainProc : %s\n", "fetch notfound");
                }

                /* データがない */
                break;

            }

            else {
                /* エラー */

                if(DBG_MAIN_PROC){
                                C_DbgMsg("cmABteraB : mainProc : %s\n", "fetch error");
                }

                        ret = APLOG_WT_902(sqlca.sqlcode, "パラメータファイル参照");

              sqlca.rollback();

                return C_const_NG;

            }

        } /* FETCH end */
        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : %s\n", "loop end");
        }



        /* 動的ＳＱＬの記述子を解放 */

//        for (loop_ix = 0; loop_ix < sele_dp.F; loop_ix++) {
//            free(sele_dp.I[loop_ix]);
//            free(sele_dp.V[loop_ix]);
//        }

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : before(SQLSQLDAFree) sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

//        SQLSQLDAFree(0, sele_dp);

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : before2(SQLSQLDAFree) sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

//        EXEC SQL CLOSE cur_main;
        sqlca.curse_close();

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : (CLOSE) sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

//        EXEC SQL COMMIT;
        sqlca.commit();

        if(DBG_MAIN_PROC){
                C_DbgMsg("cmABteraB : mainProc : (COMMIT) sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

        return C_const_OK;

    }



/******************************************************************************/
    /*                                                                            */
    /*	関数名 ： APLOG_WT_902                                                */
    /*                                                                            */
    /*	書式                                                                  */
    /*	static int  APLOG_WT_902(int param1, char *param2)                    */
    /*                                                                            */
    /*	【説明】                                                              */
    /*              ＡＰログ出力を行う(MSGID=902)                                 */
    /*                                                                            */
    /*                                                                            */
    /*	【引数】                                                              */
    /*		int     	param1     ：エラーコード                     */
    /*		char       *	param2     ：テーブル名                       */
    /*                                                                            */
    /*	【戻り値】                                                            */
    /*              0	： 正常                                               */
    /*              1	： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int APLOG_WT_902(int param1, String param2)
    {
        int ret;
        ret = APLOG_WT("902", 0, null, param1, param2, 0, 0, 0, 0);

        return ret;
    }

/******************************************************************************/
    /*                                                                            */
    /*	関数名 ： APLOG_WT_903                                                */
    /*                                                                            */
    /*	書式                                                                  */
    /*	static int  APLOG_WT_903(char *param1, int param2, int param3)        */
    /*                                                                            */
    /*	【説明】                                                              */
    /*              ＡＰログ出力を行う(MSGID=903)                                 */
    /*                                                                            */
    /*                                                                            */
    /*	【引数】                                                              */
    /*		char       *	param1     ：関数名                           */
    /*		int     	param2     ：STATUS                           */
    /*		int     	param3     ：SUB                              */
    /*                                                                            */
    /*	【戻り値】                                                            */
    /*              0	： 正常                                               */
    /*              1	： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int APLOG_WT_903(String param1, int param2, int param3)
    {
        int ret;
        ret = APLOG_WT("903", 0, null, param1, param2, param3, 0, 0, 0);

        return ret;
    }

    /* レコード内文字列分解処理 */
    public int splitRec(String in_str, IntegerDto out_cnt, ArrayList<String> out_str)
    {
        int  loop_ix;
        int  wk_cnt; /* 編集文字数 */
        int  leftParen; /* 左かっこの数 */



        if (DBG_SPLIT_REC){
                C_DbgMsg("splitRec %s \n", "start");
        }

            /* 初期化 */
        out_cnt.arr = 0;
        out_str.add(out_cnt.arr, "");
//        memset(out_str, 0x00, 512);

        /* １桁目が#,改行コード,nullの場合は正常終了する */
        if (in_str.charAt(0) == '#' || in_str.charAt(0)  == '\n' || in_str.charAt(0)  == '\0') {
            return C_const_OK;
        }

        wk_cnt = 0;
        leftParen = 0;
        for (loop_ix = 0; loop_ix < in_str.length(); loop_ix++) {
            if (in_str.charAt(loop_ix)== ' ' || in_str.charAt(loop_ix) == '\t') {
                if (wk_cnt == 0) {
                    continue;
                }
                else {
                    if (leftParen > 0) {
                        out_str.set(out_cnt.arr, out_str.get(out_cnt.arr) + in_str.charAt(loop_ix));
                        wk_cnt++;
                    }
                    else {
                        wk_cnt = 0;
                        out_cnt.arr = out_cnt.arr + 1;
                        out_str.add(out_cnt.arr, "");
//                        memset(out_str[out_cnt.arr], 0x00, 512);
                    }
                }
            }
            else if (in_str.charAt(loop_ix) == '\n' || in_str.charAt(loop_ix) == '\0') {
                if (wk_cnt != 0) {
                    wk_cnt = 0;
                    leftParen = 0;
                    out_cnt.arr = out_cnt.arr + 1;
                    out_str.add(out_cnt.arr, "");
//                    memset(out_str[out_cnt.arr], 0x00, 512);
                }
                break;

            }
            else {
                if (in_str.charAt(loop_ix) == '(' ) leftParen++;
                if (in_str.charAt(loop_ix) == ')' ) leftParen--;

//                out_str[out_cnt][wk_cnt] = in_str[loop_ix];

                out_str.set(out_cnt.arr, out_str.get(out_cnt.arr) + in_str.charAt(loop_ix));
//                out_str[out_cnt]= setChartAt(out_str[out_cnt],out_str[out_cnt].charAt(wk_cnt),in_str.charAt(loop_ix));
                wk_cnt++;
            }
        }

        if (DBG_SPLIT_REC){
                C_DbgMsg("splitRec %s \n", "end");
        }

        return C_const_OK;

    }

    /* Ｕタイプの編集 */
    public int editUtype(String ibuf, int siz, StringDto obuf)
    {
        int i;
        int cnt;
        String p;
        String p_save;

        if (siz != 0) {
            memset(obuf, 0x00, siz);

            if (ibuf == null || strlen(ibuf) == 0 || atoi(ibuf) == 0) {
                strcpy(obuf, "0");
                return C_const_OK;
            }

            /* 半角スペースとｎｕｌｌコードの数をカウントする */
            cnt = 0;
            for (i = 0; i < siz; i++) {
                if (ibuf.charAt(i) == ' ' || ibuf.charAt(i) == '\0') {
                    cnt++;
                }
            }

            /* 前スペースをスキップする */
            p = ibuf;
//            while (*p == ' ') p++;
//            p_save = p;
//            int m = 0;
//            for(int j =0 ; j < p.getBytes().length; j++){
//                if(" ".equals(p.charAt(j))){
//                    m = j +1;
//                }
//            }
            p_save = p =p.replaceAll("^\\s+","");
//            p_save = p.substring(m);

            /* 先頭の＋－の処理 */
//            if (p_save == '+' || p_save == '-') {
//                cnt++;
//                p = p_save + 1;
//            }
//            else {
//                p = p_save;
//            }
            if(p_save.startsWith("+") || p_save.startsWith("-")){
                p = p_save.substring(1);
            }

//            memcpy(obuf, p, siz - cnt); /* 数値をコピーする */
            memcpy(obuf, p, strlen(ibuf));
        }
        else {
            memcpy(obuf, ibuf, strlen(ibuf));
        }

        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ：editXtype                                                    */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int                                                                   */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              Ｘタイプの編集                                                */
    /*                  指定の桁数で左詰めし、余白は半角スペースで埋める          */
    /*                  引数で指定された場合は、コード変換(UTF8からSJIS)する      */
    /*                  引数で指定された場合は、暗号の再変換(tera用に)を行う      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    ibuf ： 入力文字列                            */
    /*              int        *    siz  ： 出力する桁数                          */
    /*              char       *    obuf ： 出力文字列                            */
    /*              int        *    sjis_flg  ： コード変換(1:変換する)           */
    /*              int        *    dec_flg  ： 暗号の複号暗号化(1:複号暗号する)  */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int editXtype(StringDto ibuf, int siz, StringDto obuf, int sjis_flg, int dec_flg)
    {
        int ret;
        int ilen;
        IntegerDto olen;
        StringDto wk_buf = new StringDto(4096);

        /* 初期化 */
        memset(obuf, 0x00, siz);

        /* ＳＪＩＳ変換 */
        if (sjis_flg == 1) {
            ilen = strlen(ibuf);
            memset(wk_buf, 0x00, sizeof(wk_buf));
            olen =  new IntegerDto();
            ret = C_ConvUT2SJ(ibuf, ilen, wk_buf, olen);
            if (ret != C_const_OK) {
                ret = APLOG_WT_903("C_ConvUT2SJ", ret, 0);
                return C_const_NG;
            }
            BT_Rtrim(wk_buf, olen.arr);
            memcpy(obuf, wk_buf, siz);
        }
        else {
            if (siz != 0) {
                memcpy(obuf, ibuf, siz);
            }
            else {
                BT_Rtrim(ibuf, strlen(ibuf));
                memcpy(obuf, ibuf, strlen(ibuf));
            }
        }

        /* 後ろスペース */
        if (strlen(obuf) < siz) {
//            memset(obuf[strlen(obuf)], ' ', siz - strlen(obuf));
            obuf.arr= setChartAt(obuf.arr,strlen(obuf),' ');
        }

        /* 復号化処理 -> 機能削除 */
        /* if (dec_flg == 1) {
                memset(wk_buf, 0x00, sizeof(wk_buf));
                memcpy(wk_buf, ibuf, 16);
                ret = C_EncOrDec(wk_buf, wk_buf, 2);
                if (ret != C_const_OK) {
                        ret = APLOG_WT_903("C_EncOrDec", ret, 0);
                        return C_const_NG;
                }
                memset(obuf, ' ', siz);
                memcpy(obuf, wk_buf, strlen(wk_buf));
        } */

        return C_const_OK;

    }

    /* 2022/11/09 MCCM初版 ADD START */
    /******************************************************************************/
    /*                                                                            */
    /*      関数名 ：editEDtype                                                   */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int                                                                   */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              暗号化タイプの編集                                            */
    /*                  入力引数に指定された文字列を暗号化して、出力引数に返す    */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              char       *    ibuf     ： 入力文字列                        */
    /*              char       *    dattyp   ： データタイプ                      */
    /*              int        *    siz      ： 出力する桁数                      */
    /*              char       *    obuf     ： 出力文字列                        */
    /*              int        *    encdec_flg ： コード変換(1:変換する)            */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */
    /******************************************************************************/
    public int editEDtype(StringDto ibuf, String dattyp, int siz, StringDto obuf, int encdec_flg)
    {

        /*  ローカル変数定義        */
        int ret;
        int olen;
        StringDto wk_buf = new StringDto(4096);
        StringDto wk_in_buf = new StringDto(1024);
        StringDto wk_space  = new StringDto(1024);

        /* 初期化 */
        memset(obuf, 0x00, siz);
        memset(wk_in_buf, 0x00, sizeof(wk_in_buf));
        memset(wk_space, 0x20, sizeof(wk_space));

        /* 暗号化 */
        if (encdec_flg == 1) {
            /* 暗号複合会員番号補正処理呼び出し */
            strncpy(wk_in_buf, ibuf, siz);
//            wk_in_buf[siz] = '\0';
//            wk_in_buf.arr = setChartAt(wk_in_buf.arr,siz,'\0');
            if (DBG_SPLIT_REC){
                        C_DbgMsg("siz [%d] \n", siz);
                        C_DbgMsg("wk_in_buf [%s] \n", wk_in_buf);
            }
                    ret = cmABendeBService.C_EncDec_Pid(1, dattyp, wk_in_buf.strVal(), wk_buf);
            if (ret != C_const_OK) {
                ret = APLOG_WT_903("C_EncDec_Pid", ret, 0);
                return C_const_NG;
            }
            olen = strlen(wk_buf);
            if (strncmp(wk_in_buf.strVal(), wk_space.strVal(), siz) == 0) {
                memcpy(obuf, wk_space, olen);
            } else {
                memcpy(obuf, wk_buf, olen);
            }
        }
        else {
            if (siz != 0) {
                memcpy(obuf, ibuf, siz);
            }
            else {
                BT_Rtrim(ibuf, strlen(ibuf));
                memcpy(obuf, ibuf, strlen(ibuf));
            }
        }

        /* 後ろスペース */
        if (strlen(obuf) < siz) {
            obuf.arr= setChartAt(obuf.arr,strlen(obuf),' ');
//            memset(obuf[strlen(obuf)], ' ', siz - strlen(obuf));
        }

        return C_const_OK;

    }
    /* 2022/11/09 MCCM初版 ADD END */
}
