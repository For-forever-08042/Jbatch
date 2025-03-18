package jp.co.mcc.nttdata.batch.business.com.cmABpaswB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*******************************************************************************
 *   プログラム名   ： パスワード発行処理（cmABpaswB）
 *
 *   【処理概要】
 *       引数で指定された値を元にパスワードを作成し
 *       標準出力に出力する。
 *
 *   【引数説明】
 *  -d算出元データ    : 算出元データ
 *  -DEBUG(-debug)    : デバッグモードでの実行
 *                      （標準出力に出力される）
 *
 *   【戻り値】
 *      10    ： 正常
 *      99    ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/12/20 SSI.Suyama ： 初版
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2010 NTT DATA CORPORATION
 ******************************************************************************/
/*----------------------------------------------------------------------------*/
/*  標準include                                                               */
/*----------------------------------------------------------------------------*/
@Service
public class CmABpaswBServiceImpl extends CmABfuncLServiceImpl implements CmABpaswBService {
    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF = 0;   /* OFF                         */
    int DEF_ON = 1;   /* ON                          */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_D = "-d";     /* 算出元データ                   */
    String DEF_DEBUG = "-DEBUG";     /* デバッグスイッチ               */
    String DEF_debug = "-debug";     /* デバッグスイッチ               */
    /*---------------------------------------------*/
    int R_const_OK = 0; /* 関数戻り値（正常）             */
    int R_const_NG = 1; /* 関数戻り値（異常）             */
    int R_const_APOK = 10; /* プログラム戻り値（正常）       */
    int R_const_APNG = 99; /* プログラム戻り値（異常）       */
    int UMU_ARI = 1; /* データ有無 - 有                */
    int UMU_NASI = 0; /* データ有無 - 無                */
    String PSW_HEAD = "cm"; /* パスワードの先頭文字列         */


    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    /*-----  引数（引数の種類分定義する）----------*/
    int arg_d_chk;
    /**
     * 引数dチェック用
     **/
    String arg_d_Value;
    /**
     * 引数d設定値
     **/
    /*---------------------------------------------*/
    int DBG_LOG;
    /**
     * デバッグメッセージ出力
     **/
    StringDto str_data = new StringDto();       /** パスワード                **/

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

        /*-------------------------------------*/
        /*  ローカル変数定義                   */
        /*-------------------------------------*/
        int rtn_cd;                         /** 関数戻り値                       **/
        int rtn_cd_arg;                     /** 引数チェック関数戻り値           **/
        int arg_chk;                        /** 引数の種類チェック結果           **/
        int arg_cnt;                        /** 引数チェック用カウンタ           **/
        String arg_Work1 = null;                /** Work Buffer1                     **/

        /*                                               */
        /*  初期処理                                     */
        /*                                               */
        /** 変数初期化                **/
        arg_d_chk = DEF_OFF;
        rtn_cd = R_const_OK;
        rtn_cd_arg = 0;
        DBG_LOG = 0;

        /*                                     */
        /*  入力引数チェック                   */
        /*                                     */
        /*** 引数チェック ***/
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            memset(arg_Work1, 0x00, 256);
            arg_Work1 = strcpy(arg_Work1, argv[arg_cnt]);
            arg_chk = UMU_ARI;

            if (strcmp(arg_Work1, DEF_DEBUG) == 0) {
                DBG_LOG = 1;
                continue;
            } else if (strcmp(arg_Work1, DEF_debug) == 0) {
                DBG_LOG = 1;
                continue;
            } else if (memcmp(arg_Work1, DEF_ARG_D, 2) == 0) {
                /* 算出元データ指定 -d */
                rtn_cd_arg = Chk_ArgdInf(arg_Work1);
            } else {
                /* 規定外パラメータ  */
                arg_chk = UMU_NASI;
            }

            /* 規定外パラメータ  */
            if (arg_chk != UMU_ARI) {
                if (DBG_LOG == 1) {
                    /*-----------------------------------------------------------*/
                    printf("*** main *** チェックNG(規定外) %s\n", "");
                    /*-----------------------------------------------------------*/
                }
                return exit(R_const_APNG);
            }

            /* パラメータのチェック結果がNG */
            if (rtn_cd_arg == R_const_NG) {
                if (DBG_LOG == 1) {
                    /*-----------------------------------------------------------*/
                    printf("*** main *** チェックNG(結果) %s\n", "");
                    /*-----------------------------------------------------------*/
                }
                return exit(R_const_APNG);
            }
        }

        /* 必須パラメータ未指定の場合エラー出力 */
        if (arg_d_chk == DEF_OFF) {
            return exit(R_const_APNG);
        }
        if (DBG_LOG == 1) {
            /*-----------------------------------------------------*/
            printf("*** main *** チェックOK %s\n", "");
            /*-----------------------------------------------------*/
        }

        /*                                               */
        /*  主処理                                       */
        /*                                               */
        if (DBG_LOG == 1) {
            /*-------------------------------------------------------------*/
            printf("*** cmABpaswB_main *** パスワード作成処理%s\n", "");
            /*-------------------------------------------------------------*/
        }
        rtn_cd = cmABpaswB_main();
        if (rtn_cd != R_const_OK) {
            return exit(R_const_APNG);
        }

        /*                                               */
        /*  終了処理                                     */
        /*                                               */
        printf("%s\n", str_data);
        if (DBG_LOG == 1) {
            /*---------------------------------------------*/
            printf("main処理 終了\n");
            /*---------------------------------------------*/
        }

        return exit(R_const_APOK);
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
    public int Chk_ArgdInf(String Arg_in) {
        /*                                     */
        /*  ローカル変数定義                   */
        /*                                     */
        int DEF_ARG_Leng = 14 + 2;      /* 文字サイズ                  */

        /* 初期化 */
        memset(arg_d_Value, 0x00, sizeof(arg_d_Value));

        if (DBG_LOG == 1) {
            /*---------------------------------------------------------------------*/
            printf("Chk_ArgdInf処理 開始\n");
            /*---------------------------------------------------------------------*/
        }
        /*                                     */
        /*  重複指定チェック                   */
        /*                                     */
        if (arg_d_chk != DEF_OFF) {

            if (DBG_LOG == 1) {
                /*-------------------------------------------------------------*/
                printf("*** Chk_ArgdInf *** 重複指定NG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }
        arg_d_chk = DEF_ON;
        /*                                     */
        /*  値の内容チェック                   */
        /*                                     */
        /*  文字サイズチェック  */
        if (strlen(Arg_in) != DEF_ARG_Leng) {

            if (DBG_LOG == 1) {
                /*-------------------------------------------------------------*/
                printf("*** Chk_ArgdInf *** 文字サイズNG = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        /*  設定値Nullチェック  */
        if (StringUtils.isEmpty(Arg_in)) {

            if (DBG_LOG == 1) {
                /*-------------------------------------------------------------*/
                printf("*** Chk_ArgdInf *** 設定値Null = %s\n", Arg_in);
                /*-------------------------------------------------------------*/
            }

            return R_const_NG;
        }

        if (DBG_LOG == 1) {
            /*---------------------------------------------------------------------*/
            printf("Chk_ArgdInf処理 終了\n");
            /*---------------------------------------------------------------------*/
        }
        arg_d_Value = strcpy(arg_d_Value, Arg_in.substring(2));

        return R_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmABpaswB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmABpaswB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               パスワード作成処理                                           */
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
    public int cmABpaswB_main() {
        int i_yyyy;           /* 計算用変数（年）      */
        int i_hhmm;           /* 計算用変数（時分）    */
        int i_data;           /* 計算用変数（加算後）  */
        String str_yyyy = null;      /* 編集用変数（年）      */
        String str_hhmm = null;      /* 編集用変数（時分）    */
        String str_mmdd = null;      /* 編集用変数（月日）    */
        String str_r_data = null;    /* 逆に配置する用変数    */

        /** 変数初期化                **/
        memset(str_yyyy, 0x00, sizeof(str_yyyy));
        memset(str_hhmm, 0x00, sizeof(str_hhmm));
        memset(str_mmdd, 0x00, sizeof(str_mmdd));
        memset(str_data, 0x00, sizeof(str_data));
        memset(str_r_data, 0x00, sizeof(str_r_data));

        /** 引数から使用するデータ取得  **/
        str_yyyy = strncpy(str_yyyy, arg_d_Value, 4);
        str_hhmm = strncpy(str_hhmm, arg_d_Value.substring(8), 4);
        str_mmdd = strncpy(str_mmdd, arg_d_Value.substring(4), 4);
        if (DBG_LOG == 1) {
            /*-------------------------------------------------------------*/
            printf("*** cmABpaswB_main *** str_yyyy=[%s]\n", str_yyyy);
            printf("*** cmABpaswB_main *** str_hhmm=[%s]\n", str_hhmm);
            printf("*** cmABpaswB_main *** str_mmdd=[%s]\n", str_mmdd);
            /*-------------------------------------------------------------*/
        }

        /** 数値に変換                  **/
        i_yyyy = atoi(str_yyyy);
        i_hhmm = atoi(str_hhmm);

        /** 加算                        **/
        i_data = i_yyyy + i_hhmm;

        /** 加算した値+月日             **/
        str_r_data = sprintf(str_r_data, "%d", i_data);
        str_r_data = strcat(str_r_data, str_mmdd);

        if (DBG_LOG == 1) {
            /*-------------------------------------------------------------*/
            printf("*** cmABpaswB_main *** str_r_data=[%s]\n", str_r_data);
            /*-------------------------------------------------------------*/
        }
        /** 先頭に固定文字列を付加して     **/
        /** 「加算した値+月日」を逆に配置  **/
        strcat(str_data, PSW_HEAD);
        strncat(str_data, str_r_data.substring(7, 8), 1);
        strncat(str_data, str_r_data.substring(6, 7), 1);
        strncat(str_data, str_r_data.substring(5, 6), 1);
        strncat(str_data, str_r_data.substring(4, 5), 1);
        strncat(str_data, str_r_data.substring(3, 4), 1);
        strncat(str_data, str_r_data.substring(2, 3), 1);
        strncat(str_data, str_r_data.substring(1, 2), 1);
        strncat(str_data, str_r_data, 1);

        /* 処理を終了する */
        return R_const_OK;

    }
}
