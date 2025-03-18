package jp.co.mcc.nttdata.batch.business.service.cmBTleavB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTleavB.dto.*;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.C_ORACONN_MD;

/*******************************************************************************
 *   プログラム名   ： 顧客退会処理（cmBTleavB）
 *
 *   【処理概要】
 *    退会顧客データファイルの顧客に対して、
 *    退会処理（カード停止、顧客静態クリア、エントリー停止等）を行う。
 *
 *   【引数説明】
 *     -i退会顧客データファイル名 :（必須）入力ファイル名（$CM_APWORK_DATE）
 *     -k顧客ステータス退会ファイル名 :（必須）入力ファイル名（$CM_APWORK_DATE）
 *     -o処理結果ファイル名       :（必須）出力ファイル名（$CM_APWORK_DATE）
 *     -DEBUG(-debug)             :（任意）デバッグモードでの実行
 *
 *   【戻り値】
 *     10 ： 正常
 *     99 ： 異常
 *
 *------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 5（64bit）
 *      (文字コード ： UTF8)
 *------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 :  2012/11/07 SSI.越後谷 ： 初版
 *           :  2012/12/07 SSI.本田   ： 顧客企業別属性情報の更新条件に
 *                                       退会年月日を追加
 *           :  2013/02/19 SSI.本田   ： MM顧客属性情報のカラム変更対応
 *                                        携帯電話番号->電話番号３
 *                                        検索携帯電話番号->電話番号４
 *                                        携帯Ｅメールアドレス１->検索電話番号３
 *                                        携帯Ｅメールアドレス２->検索電話番号４
 *           :  2014/11/26 SSI.上野   ： MSカード情報を更新条件に
 *                                       仮退会カード条件を追加
 *                                       家族顧客番号の削除時に
 *                                       顧客番号にNULLを設定するよう変更
 *      2.00 :  2016/03/18 SSI.田頭   :  モバイルカスタマーポータル対応
 *                                       MM顧客属性情報.Ｅメールアドレス３を追加
 *      3.00 :  2016/08/08 SSI.田頭　 :  C-MAS対応
 *                                        MM顧客属性情報.Ｅメールアドレス４追加
 *                                        MS顧客制度情報.ＥＣ会員フラグ追加
 *                                        MS顧客制度情報.アプリ会員フラグ追加
 *      4.00 :  2017/12/04 SSI.吉田　 :  自動退会対象仕様変更対応
 *                                        全チャンネル無効状態の会員のうち、
 *                                        顧客依頼退会日から31日経過した顧客情報を
 *                                        退会顧客データファイルに出力する
 *     30.00 :  2021/02/02 NDBS.緒方  :  期間限定Ｐ対応によりリコンパイル
 *                                       (TS利用可能ポイント情報構造体/
 *                                        顧客データロック処理内容更新のため)
 *     40.00 : 2022/10/14  SSI.申     :  MCCM初版
 *     41.00 : 2023/05/08  SSI.陳セイキン：MCCMPH2
 *     42.00 : 2024/03/19  SSI.申     :  HS-0206 MS顧客制度情報の
 *                                      「デジタル会員ＥＣ入会フラグ」と
 *                                      「デジタル会員アプリ入会フラグ」
 *                                       0で更新するように改修する
 *           : 2024/04/03  SSI.申     :  HS-0216 顧客ステータス退会更新の時
 *                                       MS顧客制度情報の
 *                                      「グローバル会員フラグ」
 *                                      「コーポレート会員フラグ」
 *                                      「サンプリング要否フラグ」
 *                                      「デジタル会員ＥＣ入会フラグ」
 *                                      「デジタル会員アプリ入会フラグ」
 *                                       に更新するように改修する
 *------------------------------------------------------------------------------
 *  $Id:$
 *------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 ******************************************************************************/
@Service
public class CmBTleavBServiceImpl extends CmABfuncLServiceImpl implements CmBTleavBService{
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                    */
    /*----------------------------------------------------------------------------*/
    boolean DBG_LOG = true;                /* デバッグメッセージ出力             */
    Integer EOF = -1;

    /*----------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                              */
    /*----------------------------------------------------------------------------*/

    /*----------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                              */
    /*----------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    /* 使用テーブルヘッダーファイルをインクルード */
//    EXEC SQL INCLUDE MM_KOKYAKU_INFO_DATA.h;        /* MM顧客情報                     */
//    EXEC SQL INCLUDE MM_KOKYAKU_ZOKUSE_INFO_DATA.h; /* MM顧客属性情報                 */
//    EXEC SQL INCLUDE MM_KOKYAKU_KIGYOBETU_ZOKUSE.h; /* MM顧客企業別属性情報           */
//    EXEC SQL INCLUDE MM_MATERNITY_BABY_INFO.h;      /* MMマタニティベビー情報         */
//    EXEC SQL INCLUDE TM_MEMO.h;                     /* TMメモ                         */
//    EXEC SQL INCLUDE MM_OTODOKESAKI_INFO.h;         /* MMお届け先情報                 */                                                                                 /* 2023/05/08 MCCMPH2 ADD  */
//    EXEC SQL INCLUDE MS_CARD_INFO.h;                /* MSカード情報                   */
//    EXEC SQL INCLUDE MS_CIRCLE_KOKYAKU_INFO.h;      /* MSサークル顧客情報             */
//    EXEC SQL INCLUDE MS_KOKYAKU_SEDO_INFO_DATA.h;   /* MS顧客制度情報                 */
//    EXEC SQL INCLUDE MS_KAZOKU_SEDO_INFO.h;         /* MS家族制度情報                 */
//    EXEC SQL INCLUDE MS_RANKBETSU_POINT_INFO.h;     /* MSランク別ボーナスポイント情報 */
//    EXEC SQL INCLUDE TS_YEAR_POINT_DATA.h;          /* TS年間ポイント情報             */
//    EXEC SQL INCLUDE TS_RIYO_KANO_POINT_DATA.h;     /* TS利用可能ポイント情報         */
//    EXEC SQL INCLUDE TS_RANK_INFO.h;                /* TSランク情報                   */                                                                                 /* 2022/12/12 MCCM初版 ADD */

    MM_KOKYAKU_INFO_TBL mmkoinf_t;      /* MM顧客情報                     */
    MM_KOKYAKU_ZOKUSE_INFO_TBL mmkozok_t;      /* MM顧客属性情報                 */
    MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL mmkokgb_t;      /* MM顧客企業別属性情報           */
    MM_MATERNITY_BABY_INFO_TBL mmmbaby_t;      /* MMマタニティベビー情報         */
    TM_MEMO_TBL                     tmmemo_t;       /* TMメモ                         */
    MM_OTODOKESAKI_INFO_TBL         mmotoinf_t;      /* MMお届け先情報                 */                                                                                 /* 2023/05/08 MCCMPH2 ADD  */
    MS_CARD_INFO_TBL mscard_t;       /* MSカード情報                   */
    MS_CIRCLE_KOKYAKU_INFO_TBL      mscirclek_t;    /* MSサークル顧客情報             */
    MS_KOKYAKU_SEDO_INFO_TBL        mskosed_t;      /* MS顧客制度情報                 */
    MS_KAZOKU_SEDO_INFO_TBL         mskased_t;      /* MS家族制度情報                 */
    MS_RANKBETSU_POINT_INFO_TBL     msranki_t;      /* MSランク別ボーナスポイント情報 */
    TS_YEAR_POINT_TBL               tsyearpoint_t;  /* TS年間ポイント情報             */
    TS_RIYO_KANO_POINT_TBL          tsriyokanop_t;  /* TS利用可能ポイント情報         */
    TS_RANK_INFO_TBL                h_ts_rank_info_data;        /* TSランク情報データ */                                                                                 /* 2022/12/12 MCCM初版 ADD */

    /* 処理用 */
    ItemDto  this_date = new ItemDto();                /* バッチ日付                         */
    ItemDto  gh_lastmonth = new ItemDto(9);          /* ホスト変数 処理日付(前日)前月      */
    ItemDto  h_shori_date_month = new ItemDto();       /* 処理日付の月                       */
    ItemDto  kazoku_rankup_kingaku_koshin_date = new ItemDto();
    /* 家族ランクアップ対象金額更新日     */
    ItemDto       sys_time = new ItemDto(10);             /* 取得用システム時刻                 */
    ItemDto       this_hms = new ItemDto();                 /* 更新用システム時刻                 */
    ItemDto       syori_kokyaku_no = new ItemDto(15+1);   /* 処理顧客番号                       */
    ItemDto       horyu_kaiin_no = new ItemDto(15+1);     /* 保留会員番号                       */
    ItemDto       tenpo = new ItemDto(3+1);               /* 店舗（会員番号の頭３桁）           */
    ItemDto       seiki_kaiin_no = new ItemDto(15+1);     /* 正規会員番号                       */
    ItemDto       Program_Name_Ver = new ItemDto(13);     /* バージョン付きプログラム名         */
    ItemDto       Program_Name = new ItemDto(10);         /* バージョンなしプログラム名         */
    ItemDto       str_sql = new ItemDto(4096);            /* 動的ＳＱＬ用                       */
    ItemDto       h_uid = new ItemDto(15+1);              /* 顧客番号                           */
    ItemDto       WRKSQL = new ItemDto(4096*2);           /* SQL文・テーブル名                  */                                                                                 /* 2022/12/27 MCCM初版 MOD */

    ItemDto        h_wk_double = new ItemDto();              /* 汎用                               */
    ItemDto        kaijo_kokyaku_no = new ItemDto(15+1);   /* 家族ＩＤ削除用の顧客番号           */
    ItemDto        h_bat_date_zennen = new ItemDto(9);     /* バッチ日付(前年)                   */                                                                                 /* 2022/10/14 MCCM初版 ADD */
    ItemDto        h_shori_date_zennichi = new ItemDto(9); /* 処理日付(前日)                     */                                                                                 /* 2022/10/14 MCCM初版 ADD */
    ItemDto        h_shori_date = new ItemDto(9);          /* 処理日付                           */                                                                                 /* 2022/10/14 MCCM初版 ADD */

//    EXEC SQL END DECLARE SECTION;

    /*----------------------------------------------------------------------------*/
    /*  定数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int DEF_OFF   =             0 ;    /* OFF                                */
    int DEF_ON    =             1 ;    /* ON                                 */
    int DEF_EOF   =             9 ;    /* File read EOF                      */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_ARG_I  = "-i"      ;         /* 入力ファイル名                     */
    String DEF_ARG_S  = "-s"      ;         /* 入力ファイル名2                    */                                                                                 /* 2022/10/14 MCCM初版 ADD */
    String DEF_ARG_O  = "-o"      ;         /* 出力ファイル名                     */
    String DEF_ARG_KS = "-ks"     ;         /* 特別処理期間開始日                 */
    String DEF_ARG_KE = "-ke"     ;         /* 特別処理期間終了日                 */
    String DEF_DEBUG  = "-DEBUG"  ;         /* デバッグスイッチ                   */
    String DEF_debug  = "-debug"  ;         /* デバッグスイッチ                   */
    /*---------------------------------------------*/
    String C_PRGNAME  =  "顧客退会" ;        /* APログ用機能名                     */
    int C_RANK_MAX =    100     ;         /* MSランク別ボーナスポイント情報数
                                                         (メモリ持ちする件数) */

    /*----------------------------------------------------------------------------*/
    /*  変数定義                                                                  */
    /*----------------------------------------------------------------------------*/
    int           gl_rank_cnt;              /** ランク情報メモリ持ちする件数  **/
    int           taisyo_cnt;               /** 処理対象件数                  **/
    int           ok_cnt;                   /** 正常処理件数                  **/
    int           chk_arg_i;                /** 引数-iチェック用              **/
    int           chk_arg_s;                /** 引数-sチェック用              **/                                                                                    /* 2022/10/14 MCCM初版 ADD */
    int           chk_arg_o;                /** 引数-oチェック用              **/
    int           chk_arg_ks;               /** 引数-ksチェック用             **/
    int           chk_arg_ke;               /** 引数-keチェック用             **/
    StringDto         arg_i_Value = new StringDto();         /** 引数i設定値                   **/
    StringDto         arg_s_Value = new StringDto();         /** 引数s設定値                   **/                                                                                    /* 2022/10/14 MCCM初版 ADD */
    StringDto          arg_o_Value = new StringDto(256);         /** 引数o設定値                   **/
    StringDto          inp_file_dir= new StringDto(4096);       /** 入力ファイルディレクトリ      **/
    StringDto          out_file_dir= new StringDto(4096);       /** 出力ファイルディレクトリ      **/
    StringDto[]          inp_fl_name = new StringDto[2];     /** 入力ファイル・パス名          **/                                                                                    /* 2022/10/14 MCCM初版 MOD */
    StringDto          out_fl_name = new StringDto(4096);        /** 出力ファイル・パス名          **/
    ItemDto          lock_chk = new ItemDto();                 /** 顧客ロックデータ有無フラグ    **/
    StringDto          chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログ用          **/
    int           togo_flg;                 /** 統合データフラグ              **/

    /* 共通関数のトレースログのフラグ */
     int dbg_kdatalock;

    /* ランク情報 */
//    static struct {                            /** ランク取得内部テーブル           **/
//        unsigned int  rank_shubetsu_buf;       /*  ランク種別                        */
//        unsigned int  rank_cd_buf;             /*  ランクコード                      */
//        double        hitsuyo_kingaku_buf;     /*  必要金額                          */
//    } sRankbuf[C_RANK_MAX];
    SRankbuf[] sRankbuf =  new SRankbuf[C_RANK_MAX];

    /* 年月ランクＵＰ金額 */
//    typedef struct {                           /** 年月ランクＵＰ金額 内部テーブル  **/
//        double        year_buf[10]  ;          /* 年間家族ランクＵＰ金額[配列]       */
//        double        month0_buf[12];          /* 月間家族ランクＵＰ金額[配列]偶数年 */
//        double        month1_buf[12];          /* 月間家族ランクＵＰ金額[配列]奇数年 */
//    } RANK_UP_MONEY;

    /* 処理用日付 */
//    typedef struct {                           /** 処理用日付 内部テーブル          **/
//        char          yyyymmdd[9]     ;        /*  処理用バッチ日付    (半角)        */
//        char          hlf_yyyy[5]     ;        /*  処理用バッチ日付年  (半角)        */
//        char          hlf_mm[3]       ;        /*  処理用バッチ日付月  (半角)        */
//        char          hlf_dd[3]       ;        /*  処理用バッチ日付日  (半角)        */
//        char          hlf_yyyymm[7]   ;        /*  処理用バッチ日付年月(半角)        */
//        char          hlf_y_bottom[2] ;        /*  年 下１桁           (半角)        */
//        char          all_yyyymmdd[24];        /*  処理用バッチ日付    (全角)        */
//        char          all_yyyy[12]    ;        /*  処理用バッチ日付年  (全角)        */
//        char          all_mm[6]       ;        /*  処理用バッチ日付月  (全角)        */
//        char          all_dd[6]       ;        /*  処理用バッチ日付日  (全角)        */
//        char          all_yyyymm[18]  ;        /*  処理用バッチ日付年月(全角)        */
//        char          all_y_bottom[3] ;        /*  年 下１桁           (全角)        */
//        unsigned int  int_yyyymmdd    ;        /*  処理用バッチ日付    (数値)        */
//        unsigned int  int_yyyy        ;        /*  処理用バッチ日付年  (数値)        */
//        unsigned int  int_mm          ;        /*  処理用バッチ日付月  (数値)        */
//        unsigned int  int_dd          ;        /*  処理用バッチ日付日  (数値)        */
//        unsigned int  int_yyyymm      ;        /*  処理用バッチ日付年月(数値)        */
//        unsigned int  int_y_bottom    ;        /*  年 下１桁           (数値)        */
//    } PROC_BATCH_DATE;

    PROC_BATCH_DATE gstr_yesterday;      /* 処理用日付(前日)当年 */
    PROC_BATCH_DATE   gstr_lastyear;       /* 処理用日付(前日)前年 */
    PROC_BATCH_DATE   gstr_lastmonth;      /* 処理用日付(前日)前月 */

    /*----------------------------------------------------------------------------*/
    /*  入力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* 退会顧客データファイル構造体                                               */
    /*----------------------------------------------------------------------------*/
    FileStatusDto[] fp_inp = new FileStatusDto[2];                  /* 入力ファイル用ポインタ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
//    typedef struct {
//        char        kokyaku_no[15 + 1];        /* 顧客番号                        */
//    } TAIKAI_KOKYAKU_DATA;
    TAIKAI_KOKYAKU_DATA inp_taikai_t;

    /*----------------------------------------------------------------------------*/
    /*  出力ファイル                                                              */
    /*----------------------------------------------------------------------------*/
    /* 処理結果ファイル構造体                                                     */
    /*----------------------------------------------------------------------------*/
    FileStatusDto        fp_out;                    /* 出力ファイル用ポインタ             */
//    typedef struct {
//        char       kokyaku_no[15+1];             /*  1 顧客番号                   */
//        char       kokyaku_mesho[80*3+1];        /*  2 顧客名称                   */                                                                                 /* 2022/10/14 MCCM初版 MOD */
//        char       kokyaku_kana_mesho[80*3+1];   /*  3 顧客カナ名称               */                                                                                 /* 2022/10/14 MCCM初版 MOD */
//        char       nenre[3+1];                   /*  4 年齢                       */
//        char       tanjo_y[4+1];                 /*  5 誕生年                     */
//        char       tanjo_m[2+1];                 /*  6 誕生月                     */
//        char       tanjo_d[2+1];                 /*  7 誕生日                     */
//        char       sebetsu[1+1];                 /*  8 性別                       */
//        char       yubin_no[10+1];               /*  9 郵便番号                   */
//        char       yubin_no_cd[23+1];            /* 10 郵便番号コード             */
//        char       jusho_1[10*3+1];              /* 11 住所１                     */
//        char       jusho_2[200*3+1];             /* 12 住所２                     */                                                                                 /* 2022/10/14 MCCM初版 MOD */
////  char       jusho_3[80*3+1];              /* 13 住所３                     */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//        char       denwa_no_1[15+1];             /* 14 電話番号１                 */
//        char       denwa_no_2[15+1];             /* 15 電話番号２                 */
//        char       denwa_no_3[15+1];             /* 16 電話番号３                 */
//        char       denwa_no_4[15+1];             /* 17 電話番号４                 */
//        char       kensaku_denwa_no_1[15+1];     /* 18 検索電話番号１             */
//        char       kensaku_denwa_no_2[15+1];     /* 19 検索電話番号２             */
//        char       kensaku_denwa_no_3[15+1];     /* 20 検索電話番号３             */
//        char       kensaku_denwa_no_4[15+1];     /* 21 検索電話番号４             */
//        char       email_address_1[60+1];        /* 22 Ｅメールアドレス１         */
//        char       email_address_2[60+1];        /* 23 Ｅメールアドレス２         */
//        char       shokugyo[40*3+1];             /* 24 職業                       */
//        char       kinmu_kbn[3+1];               /* 25 勤務区分                   */
//        char       email_address_3[100+1];       /* 26 Ｅメールアドレス３         */
//        char       email_address_4[100+1];       /* 27 Ｅメールアドレス４         */
//    } SYORI_KEKKA_DATA;
    SYORI_KEKKA_DATA out_kekka_t;

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
    public MainResultDto main(int argc, String[] argv)
    {
        int     rtn_cd;                         /* 関数戻り値                         */
        IntegerDto rtn_status = new IntegerDto();                     /* 関数結果ステータス                 */
        int     arg_cnt;                        /* 引数チェック用カウンタ             */
        String env_inpdir;                    /* 入力ファイルDIR                    */
        String env_outdir;                    /* 出力ファイルDIR                    */
        StringDto    arg_Work1 = new StringDto(256);                 /* Work Buffer1                       */



        /*-----------------------------------------------*/
        /*  初期処理                                     */
        /*-----------------------------------------------*/
        taisyo_cnt = 0;                     /* 処理対象件数                       */
        ok_cnt     = 0;                     /* 正常処理件数                       */
        rtn_cd = C_const_OK;                /* 関数戻り値                         */

        /*-------------------------------------*/
        /*  プログラム名取得処理               */
        /*-------------------------------------*/
        rtn_cd = C_GetPgname( argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*  開始メッセージ */
        rtn_cd = 0;
        APLOG_WT("102", 0, null, "顧客退会", rtn_cd, 0, 0, 0, 0);
        memset( Program_Name_Ver, 0x00, sizeof(Program_Name_Ver) );
        strcpy( Program_Name_Ver, Cg_Program_Name_Ver ); /* バージョン付きプログラム名 */
        memset( Program_Name, 0x00, sizeof(Program_Name) );
        strcpy( Program_Name, Cg_Program_Name );         /* バージョンなしプログラム名 */

        /*-------------------------------------*/
        /*  バッチデバッグ開始処理             */
        /*-------------------------------------*/
        rtn_cd = C_StartBatDbg( argc, argv );
        if ( rtn_cd != C_const_OK ) {
            APLOG_WT("903", 0, null, "C_StartBatDbg", rtn_cd, 0, 0, 0, 0);
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /*  入力引数チェック                   */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("*** main処理 ***");
                /*------------------------------------------------------------*/
        }
            /** 変数初期化 **/
            rtn_cd = C_const_OK;
        chk_arg_i = DEF_OFF;
        chk_arg_s = DEF_OFF;                                                                                                                                         /* 2022/10/14 MCCM初版 ADD */
        chk_arg_o = DEF_OFF;
        memset( arg_i_Value, 0x00, sizeof(arg_i_Value) );
        memset( arg_s_Value, 0x00, sizeof(arg_s_Value) );                                                                                                            /* 2022/10/14 MCCM初版 ADD */
        memset( arg_o_Value, 0x00, sizeof(arg_o_Value) );

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** 入力引数チェック %s\n", "START");
                /*------------------------------------------------------------*/
        }
        /*** 引数チェック ***/
        for ( arg_cnt = 1; arg_cnt < argc; arg_cnt++ ) {
            memset( arg_Work1, 0x00, sizeof(arg_Work1) );
            strcpy( arg_Work1, argv[arg_cnt] );

            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** チェック対象パラメータ=[%s]\n", arg_Work1);
                        /*------------------------------------------------------------*/
            }
            if ( 0 == strcmp(arg_Work1, DEF_DEBUG) || 0 == strcmp(arg_Work1, DEF_debug) ) {
                continue;

            } else if( 0 == memcmp( arg_Work1, DEF_ARG_I, 2 ) ) { /* -iの場合         */
                rtn_cd = cmBTleavB_Chk_Arg( arg_Work1 );    /* パラメータチェック */
                if ( rtn_cd == C_const_OK ) {
                    strcpy(arg_i_Value, arg_Work1.substring(2));
                } else {
                    sprintf( chg_format_buf, "-i 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit( C_const_APNG );
                }
            } else if( 0 == memcmp( arg_Work1, DEF_ARG_S, 2 ) ) { /* -sの場合         */                                                                                 /* 2022/10/14 MCCM初版 ADD */
                rtn_cd = cmBTleavB_Chk_Arg( arg_Work1 );    /* パラメータチェック */                                                                                 /* 2022/10/14 MCCM初版 ADD */
                if ( rtn_cd == C_const_OK ) {                                                                                                                        /* 2022/10/14 MCCM初版 ADD */
                    strcpy(arg_s_Value, arg_Work1.substring(2));                                                                                                              /* 2022/10/14 MCCM初版 ADD */
                } else {                                                                                                                                             /* 2022/10/14 MCCM初版 ADD */
                    sprintf( chg_format_buf, "-s 引数の値が不正です（%s）", arg_Work1);                                                                              /* 2022/10/14 MCCM初版 ADD */
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);                                                                                         /* 2022/10/14 MCCM初版 ADD */
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */                                                                                 /* 2022/10/14 MCCM初版 ADD */
                    return exit( C_const_APNG );                                                                                                                            /* 2022/10/14 MCCM初版 ADD */
                }                                                                                                                                                    /* 2022/10/14 MCCM初版 ADD */
            }else if( 0 == memcmp( arg_Work1, DEF_ARG_O, 2 ) ) { /* -oの場合         */
                rtn_cd = cmBTleavB_Chk_Arg( arg_Work1 );    /* パラメータチェック */
                if ( rtn_cd == C_const_OK ) {
                    strcpy(arg_o_Value, arg_Work1.substring(2));
                } else {
                    sprintf( chg_format_buf, "-o 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit( C_const_APNG );
                }
            } else {                        /* 定義外パラメータ                   */
                sprintf( chg_format_buf, "定義外の引数（%s）", arg_Work1 );
                APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();                     /* バッチデバッグ終了 */
                return exit( C_const_APNG );
            }
        }

        /* 必須パラメータ未指定チェック */
        if ( chk_arg_i == DEF_OFF ) {
            sprintf( chg_format_buf, "-i 引数の値が不正です" );
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit( C_const_APNG );
        }
        if ( chk_arg_o == DEF_OFF ) {
            sprintf( chg_format_buf, "-o 引数の値が不正です" );
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit( C_const_APNG );
        }
        /* 2022/10/14 MCCM初版 ADD START */
        if ( chk_arg_s == DEF_OFF ) {
            sprintf( chg_format_buf, "-s 引数の値が不正です" );
            APLOG_WT("910", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit( C_const_APNG );
        }
        /* 2022/10/14 MCCM初版 ADD END */

        /*-------------------------------------*/
        /*  環境変数取得                       */
        /*-------------------------------------*/
        /*-------------------------------------*/
        /*  入力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", "CM_APWORK_DATE");
                /*-------------------------------------------------------------*/
        }
            env_inpdir = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_inpdir)) {
            if (DBG_LOG){
                        /*---------------------------------------------*/
                        C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
                        /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        memset(inp_file_dir, 0x00, sizeof(inp_file_dir));
        strcpy(inp_file_dir, env_inpdir);

        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）[%s]\n", inp_file_dir);
                /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  出力ファイルＤＩＲの取得           */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "CM_APWORK_DATE");
                /*-------------------------------------------------------------*/
        }
            env_outdir = getenv("CM_APWORK_DATE");
        if ( StringUtils.isEmpty(env_outdir) ) {
            if (DBG_LOG){
                        /*---------------------------------------------*/
                        C_DbgMsg("*** main *** 環境変数取得NG [CM_APWORK_DATE]%s\n", "NULL");
                        /*---------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        memset(out_file_dir, 0x00, sizeof(out_file_dir));
        strcpy(out_file_dir, env_outdir);

        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）[%s]\n", out_file_dir);
                /*-------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /*  DBコネクト処理                     */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** DBコネクト(%s)\n", C_ORACONN_MD);
                /*------------------------------------------------------------*/
        }
            rtn_status.arr = C_const_OK;            /* 関数結果ステータス                 */
        rtn_cd = C_OraDBConnect( C_ORACONN_MD, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** DBコネクトNG rtn =[%d]\n", rtn_cd);
                        C_DbgMsg("*** main *** DBコネクトNG status =[%d]\n", rtn_status);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_OraDBConnect",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*-------------------------------------*/
        /*  ファイルオープン                   */
        /*-------------------------------------*/
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** main *** ファイルオープン(%s)\n", "START");
                /*------------------------------------------------------------*/
        }
            rtn_cd = cmBTleavB_OpenFile();
        if( rtn_cd != C_const_OK ) {
            if (DBG_LOG){
                        /*---------------------------------------------*/
                        C_DbgMsg("*** main *** ファイルオープンNG rtn=[%d]\n", rtn_cd);
                        /*---------------------------------------------*/
            }
                    rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  主処理（顧客退会処理）                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTleavB_main();
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** main *** cmBTleavB_main NG rtn=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("912", 0, null, "顧客退会処理に失敗しました", 0, 0, 0, 0, 0);

            fclose( fp_inp[0] );            /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
            fclose( fp_inp[1] );            /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
            fclose( fp_out );               /* 出力ファイルＣＬＯＳＥ             */

//            EXEC SQL ROLLBACK;              /* ロールバック                       */
            sqlca.rollback();
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return exit( C_const_APNG );
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 出力件数を出力 */
        APLOG_WT("106", 0, null, "MM顧客情報、MS顧客制度情報",
                taisyo_cnt, ok_cnt, 0, 0, 0);

        fclose( fp_inp[0] );                /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
        fclose( fp_inp[1] );                /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
        fclose( fp_out );                   /* 出力ファイルＣＬＯＳＥ             */

        /*  終了メッセージ */
        APLOG_WT("103", 0, null, C_PRGNAME, 0, 0, 0, 0, 0);

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("*** main処理 ***", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

            rtn_cd = C_EndBatDbg();             /* バッチデバッグ終了処理             */

        /*  コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        /* 警告: ‘sqlstm’ defined but not used : とりあえず参照だけする対処 */
        arg_cnt = sqlca.arrsiz(); /* ワーニングを出ないようにするおまじない arg_cntは意味なし */

        return exit( C_const_APOK );
        /*-----cmBTleavB_main Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTleavB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTleavB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     顧客退会処理                                                           */
    /*     退会顧客データファイルの顧客に対して、                                 */
    /*     退会処理（カード停止、顧客静態クリア、エントリー停止等）を行う。       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTleavB_main()
    {
        IntegerDto       rtn_status = new IntegerDto();               /* 関数結果ステータス                 */
        int       rtn_cd;                   /* 関数戻り値                         */
        StringDto      bat_date = new StringDto(9);              /* バッチ処理日付（当日）             */

//        char      kazoku_kokyaku_wk[6][15+8+6+1]; /* 家族n顧客番号 + 家族n登録日+ 家族n登録時刻 + \0 */                                                              /* 2022/12/19 MCCM初版 MOD */
        StringDto[] kazoku_kokyaku_wk = new StringDto[6];
        int       i;
        StringDto      lock_kokyaku_no = new StringDto(15+1);    /* 家族顧客番号のロック用 */
        int       ko_no_cnt = 0;                /* 顧客番号の件数 */
        StringDto      sql_buf = new StringDto(4096*2);          /* ＳＱＬ文編集用 */                                                                                                     /* 2022/12/27 MCCM初版 MOD */
        int       this_date_month;          /* バッチ処理日付の月 */                                                                                                 /* 2022/10/14 MCCM初版 ADD */


//        char      reset_kazoku_kokyaku[6][15+8+6+1]; /* 家族n顧客番号 + 家族n登録日+ 家族n登録時刻 + \0 */                                                           /* 2022/12/19 MCCM初版 MOD */
        StringDto[] reset_kazoku_kokyaku = new StringDto[6];
        StringDto      prefectures = new StringDto(10*3+1);             /* 都道府県名 */                                                                                                  /* 2022/12/19 MCCM初版 ADD */

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTleavB_main処理");
                /*------------------------------------------------------------*/
        }
            /* 初期化 */
            rtn_cd = C_const_OK;
        memset( bat_date, 0x00, sizeof(bat_date) );

        /*-----------------------------------------------*/
        /* （３）バッチ処理日付を取得（当日） */
        /*-----------------------------------------------*/
        rtn_cd = C_GetBatDate( 0, bat_date, rtn_status );
        if ( rtn_cd != C_const_OK ) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** バッチ処理日取得NG rtn=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetBatDate",
                    rtn_cd, rtn_status, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return ( C_const_NG );
        }

        if (DBG_LOG){
        /*------------------------------------------------------------*/
        C_DbgMsg("*** cmBTleavB_main *** バッチ処理日取得 bat_date=[%s]\n", bat_date);
        /*------------------------------------------------------------*/
    }

            this_date.arr = atoi( bat_date );

        /* 2022/10/14 MCCM初版 ADD START */
        /*-----------------------------------------------*/
        /* 処理日付を取得                                */
        /*-----------------------------------------------*/
        /* 初期化 */
        memset(h_bat_date_zennen, 0x00, sizeof(h_bat_date_zennen));
        memset(h_shori_date, 0x00, sizeof(h_shori_date));
        h_shori_date_month.arr = 0;

        /* バッチ処理日付の月 */
        this_date_month = this_date.intVal() / 100 % 100;

        /* バッチ処理日付の前年度の日付を取得 */
//        EXEC SQL SELECT
//        TO_CHAR(ADD_MONTHS(TO_DATE(:this_date,'YYYYMMDD'), - 12), 'YYYYMMDD')
//        INTO  :h_bat_date_zennen
//        FROM DUAL;
        sqlca.sql = new StringDto("SELECT TO_CHAR(ADD_MONTHS(TO_DATE(?,'YYYYMMDD'), - 12), 'YYYYMMDD') FROM DUAL ");
        sqlca.restAndExecute(this_date);
        sqlca.fetch();
        sqlca.recData(h_bat_date_zennen);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "前年取得SQLが失敗しました", 0, 0, 0);
            return( C_const_NG ) ;
        }

        /* 処理日付 */
        if(this_date_month <= 3){
            strcpy(h_shori_date, h_bat_date_zennen);

        }else{
            strcpy(h_shori_date, bat_date);
        }
        /* 処理日付の月 */
        h_shori_date_month.arr = atoi(h_shori_date) / 100 % 100;

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTleavB_main *** 処理日付取得 処理日付=[%s]\n", h_shori_date);
                C_DbgMsg("*** cmBTleavB_main *** 処理日付取得 処理日付の月=[%d]\n", h_shori_date_month);
                /*------------------------------------------------------------*/
        }
        /*-----------------------------------------------*/
        /* 処理日付の前日を取得                          */
        /*-----------------------------------------------*/
        gstr_yesterday = new PROC_BATCH_DATE();
        memset(gstr_yesterday, 0x00, 0);
        memset(h_shori_date_zennichi, 0x00, sizeof(h_shori_date_zennichi));

//        EXEC SQL SELECT
//        TO_CHAR(TO_DATE(:h_shori_date, 'YYYYMMDD') + INTERVAL '-1' DAY , 'YYYYMMDD')
//        INTO :h_shori_date_zennichi
//        FROM DUAL;

        sqlca.sql = new StringDto("SELECT TO_CHAR(TO_DATE(?, 'YYYYMMDD') + INTERVAL '-1' DAY , 'YYYYMMDD') FROM DUAL ");
        sqlca.restAndExecute(h_shori_date);
        sqlca.fetch();
        sqlca.recData(h_shori_date_zennichi);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "前日取得SQLが失敗しました", 0, 0, 0);
            return( C_const_NG ) ;
        }

        /* 処理日付の前日 */
        strcpy(gstr_yesterday.yyyymmdd, h_shori_date_zennichi);
        /* 2022/10/14 MCCM初版 ADD END */

        /* 2022/10/14 MCCM初版 DEL START */
//  /*-----------------------------------------------*/
//  /* バッチ処理日付を取得（前日） */
//  /*-----------------------------------------------*/
//  memset(&gstr_yesterday, 0x00, sizeof(PROC_BATCH_DATE));
//  /* バッチ処理日付を取得（前日） */
//  rtn_cd = C_GetBatDate( -1, gstr_yesterday.yyyymmdd, rtn_status );
//  if ( rtn_cd != C_const_OK ) {
//if (DBG_LOG){
//      /*------------------------------------------------------------*/
//      C_DbgMsg("*** cmBTleavB_main *** バッチ処理前日取得NG rtn=[%d]\n", rtn_cd);
//      /*------------------------------------------------------------*/
//}
//      APLOG_WT("903", 0, null, "C_GetBatDate",
//                     rtn_cd, rtn_status, 0, 0, 0);
//      rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
//      return C_const_NG;
//  }
        /* 2022/10/14 MCCM初版 DEL END */

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTleavB_main *** 処理日付前日取得 yesterday=[%s]\n", gstr_yesterday.yyyymmdd);                                                               /* 2022/10/14 MCCM初版 MOD */
                /*------------------------------------------------------------*/
        }
        /* 処理用日付（処理日付（前日））を設定 */                                                                                                                   /* 2022/10/14 MCCM初版 MOD */
        gstr_yesterday.int_yyyymmdd.arr     = atoi( gstr_yesterday.yyyymmdd )       ; /*  処理用バッチ日付    (数値) */
        memcpy(gstr_yesterday.hlf_yyyy     ,    gstr_yesterday.yyyymmdd    , 4 ); /*  処理用バッチ日付年  (半角) */
        memcpy(gstr_yesterday.hlf_mm       , gstr_yesterday.yyyymmdd.strVal().substring(4), 2 ); /*  処理用バッチ日付月  (半角) */
        memcpy(gstr_yesterday.hlf_y_bottom , gstr_yesterday.yyyymmdd.strVal().substring(3), 1 ); /*  年 下１桁           (半角) */
        gstr_yesterday.int_yyyy    .arr    = atoi( gstr_yesterday.hlf_yyyy )       ; /*  処理用バッチ日付年  (数値) */
        gstr_yesterday.int_mm      .arr    = atoi( gstr_yesterday.hlf_mm )         ; /*  処理用バッチ日付月  (数値) */
        gstr_yesterday.int_y_bottom.arr    = atoi( gstr_yesterday.hlf_y_bottom )   ; /*  年 下１桁           (数値) */

        if((gstr_yesterday.int_mm.intVal() == 12) && (h_shori_date_month.intVal() == 1)){
            gstr_yesterday.int_yyyy.arr = gstr_yesterday.int_yyyy.intVal() + 1;
            gstr_yesterday.int_y_bottom.arr = gstr_yesterday.int_yyyy.intVal() % 10;
        }
        if((gstr_yesterday.int_mm.intVal()  == 3) && (h_shori_date_month.intVal()  == 4)){
            gstr_yesterday.int_yyyy.arr = gstr_yesterday.int_yyyy.intVal() - 1;
            gstr_yesterday.int_y_bottom.arr = gstr_yesterday.int_yyyy.intVal() % 10;
        }

        /* 処理用日付（処理日付（前日）前年）を設定 */
        /* 2022/10/14 MCCM初版 MOD */
        gstr_lastyear = new PROC_BATCH_DATE();
        memset(gstr_lastyear, 0x00, 0);
        gstr_lastyear.int_yyyy    .arr     = ( gstr_yesterday.int_yyyy.intVal() -1 )        ; /*  処理用バッチ日付年  (数値) */
        gstr_lastyear.int_y_bottom.arr     = ( gstr_lastyear.int_yyyy.intVal() % 10 )       ; /*  年 下１桁           (数値) */
        /* 処理用日付（バ処理日付（前日）前月）を設定 */                                                                                                             /* 2022/10/14 MCCM初版 MOD */
        memset(gh_lastmonth, 0x00, sizeof(gh_lastmonth));

//        EXEC SQL SELECT
//        TO_CHAR(ADD_MONTHS(TO_DATE(:h_shori_date_zennichi,'YYYYMMDD'), -1), 'YYYYMMDD')                                                                     /* 2022/10/14 MCCM初版 MOD */
//        INTO  :gh_lastmonth
//        FROM dual;

        sqlca.sql = new StringDto("SELECT TO_CHAR(ADD_MONTHS(TO_DATE(?,'YYYYMMDD'), -1), 'YYYYMMDD') FROM dual ");
        sqlca.restAndExecute(h_shori_date_zennichi);
        sqlca.fetch();
        sqlca.recData(gh_lastmonth);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "前月取得SQLが失敗しました", 0, 0, 0);
            return( C_const_NG ) ;
        }
        gstr_lastmonth = new PROC_BATCH_DATE();
        memset(gstr_lastmonth, 0x00, 0);
        strcpy(gstr_lastmonth.yyyymmdd, gh_lastmonth);
        gstr_lastmonth.int_yyyymmdd .arr    = atoi( gh_lastmonth )                  ; /*  処理用バッチ日付    (数値) */
        memcpy(gstr_lastmonth.hlf_yyyy     ,    gstr_lastmonth.yyyymmdd    , 4 ); /*  処理用バッチ日付年  (半角) */
        memcpy(gstr_lastmonth.hlf_mm       ,  gstr_lastmonth.yyyymmdd.strVal().substring(4), 2 ); /*  処理用バッチ日付月  (半角) */
        memcpy(gstr_lastmonth.hlf_y_bottom ,  gstr_lastmonth.yyyymmdd.strVal().substring(3), 1 ); /*  年 下１桁           (半角) */
        gstr_lastmonth.int_yyyy    .arr     = atoi( gstr_lastmonth.hlf_yyyy )       ; /*  処理用バッチ日付年  (数値) */
        gstr_lastmonth.int_mm      .arr     = atoi( gstr_lastmonth.hlf_mm )         ; /*  処理用バッチ日付月  (数値) */
        gstr_lastmonth.int_y_bottom.arr     = atoi( gstr_lastmonth.hlf_y_bottom )   ; /*  年 下１桁           (数値) */

        if((gstr_yesterday.int_mm.intVal() == 12) && (h_shori_date_month.intVal() == 1)){
            gstr_lastmonth.int_yyyy.arr = gstr_lastmonth.int_yyyy.intVal() + 1;
            gstr_lastmonth.int_y_bottom.arr = gstr_lastmonth.int_yyyy.intVal() % 10;
        }
        if((gstr_yesterday.int_mm.intVal() == 3) && (h_shori_date_month.intVal() == 4)){
            gstr_lastmonth.int_yyyy.arr = gstr_lastmonth.int_yyyy.intVal() - 1;
            gstr_lastmonth.int_y_bottom.arr = gstr_lastmonth.int_yyyy.intVal() % 10;
        }

        if(gstr_lastmonth.int_mm.intVal() == 12){
            gstr_lastmonth.int_yyyy.arr = gstr_lastmonth.int_yyyy.intVal() + 1;
            gstr_lastmonth.int_y_bottom.arr = gstr_lastmonth.int_yyyy.intVal() % 10;
        }
        if(gstr_lastmonth.int_mm.intVal() == 3){
            gstr_lastmonth.int_yyyy.arr = gstr_lastmonth.int_yyyy.intVal() - 1;
            gstr_lastmonth.int_y_bottom .arr= gstr_lastmonth.int_yyyy.intVal() % 10;
        }
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)     年=[%d]\n", gstr_yesterday.int_yyyy);
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)     月=[%d]\n", gstr_yesterday.int_mm);
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日)   前年=[%d]\n", gstr_lastyear.int_yyyy);
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月)   =[%d]\n", gstr_lastmonth.int_yyyymmdd);
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月) 年=[%d]\n", gstr_lastmonth.int_yyyy);
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日付(前日前月) 月=[%d]\n", gstr_lastmonth.int_mm);
                /*------------------------------------------------------------*/
        }

        /*-----------------------------------------------*/
        /* （４）システム時刻取得 */
        /*-----------------------------------------------*/
        memset( sys_time, 0x00, sizeof(sys_time) );
//        EXEC SQL SELECT TO_CHAR(SYSDATE,'HH24MISS')
//        INTO :sys_time
//        FROM DUAL;

        sqlca.sql = new StringDto("SELECT TO_CHAR(SYSDATE(),'HH24MISS') FROM DUAL ");
        sqlca.restAndExecute();
        sqlca.fetch();
        sqlca.recData(sys_time);
        /* データ無し以外のエラーの場合処理終了 */
        if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** SYSDATE SELECT NG sqlcode=[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, "DUAL", 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTleavB_main *** システム時刻取得 sys_time=[%s]\n", sys_time);
                /*------------------------------------------------------------*/
        }

            this_hms.arr = atoi( sys_time );

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** cmBTleavB_main *** バッチ処理日=[%d]\n", this_date);
                C_DbgMsg("*** cmBTleavB_main *** システム時刻=[%d]\n", this_hms);
                /*------------------------------------------------------------*/
        }

            /*-----------------------------------------------*/
            /* （５）ランク情報を展開 */
            /*-----------------------------------------------*/
            /* ランク情報の取得 */
            rtn_cd = setRankInfo();
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** ランク情報取得NG rtn=[%d]\n", rtn_cd);
                        /*------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "setRankInfo",
                    rtn_cd, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();         /* バッチデバッグ終了処理             */
            return ( C_const_NG );
        }



        /*-----------------------------------------------*/
        /* （６）対象データ数分繰り返す                  */
        /*  入力ファイル読み込みループ                   */
        /*-----------------------------------------------*/
        int file_flg = 0;                                                                                                                                            /* 2022/10/14 MCCM初版 ADD */
        for (file_flg = 0; file_flg < 2; file_flg++){                                                                                                                /* 2022/10/14 MCCM初版 ADD */
            while ( true ) {
               SqlstmDto sqlca=sqlcaManager.get("SYORI_KEKKA_DATA");
                /* 処理結果ファイル編集バッファの初期化 */
                out_kekka_t = new SYORI_KEKKA_DATA();
                memset( out_kekka_t, 0x00, sizeof(out_kekka_t) );
                togo_flg = 0;

                /* （６）１．退会顧客データの読み込み */
                rtn_cd = cmBTleavB_ReadFile( file_flg ); /* File Read                 */                                                                                 /* 2022/10/14 MCCM初版 MOD */
                if ( rtn_cd == DEF_EOF ) {           /* EOF ループから抜ける          */
                    break;
                } else if ( rtn_cd != C_const_OK ) { /* ERR エラーで戻る              */
                    return C_const_NG;
                }

                taisyo_cnt++;                   /* （６）２．処理対象件数カウントアップ */

                /* （６）３．ホスト変数、処理結果ファイル構造体に変数の値をコピー */
                memset( syori_kokyaku_no.arr, 0x00, sizeof(syori_kokyaku_no.arr) );
                strncpy(syori_kokyaku_no, inp_taikai_t.kokyaku_no.strVal(), sizeof(syori_kokyaku_no));
                syori_kokyaku_no.len = strlen(inp_taikai_t.kokyaku_no);

                /* （６）４．退会済みチェック、MM顧客企業別属性情報を検索し存在をチェック */
                mmkokgb_t = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
                memset( mmkokgb_t.kokyaku_no.arr, 0x00, sizeof(mmkokgb_t.kokyaku_no.arr) );

                /* 2017.12.04 自動退会対応 Sta */

//                EXEC SQL SELECT distinct to_char(T1.顧客番号, 'FM000000000000000')
//                INTO :mmkokgb_t.kokyaku_no
//                FROM MM顧客企業別属性情報 T1
//                WHERE T1.顧客番号   = to_number(:syori_kokyaku_no)
//                AND exists (select 1 from cmuser.MSカード情報@CMSD T3 where T3.顧客番号=T1.顧客番号)
//                ;

                sqlca.sql = new StringDto("SELECT distinct to_char(T1.顧客番号, 'FM000000000000000') FROM MM顧客企業別属性情報 T1 WHERE T1.顧客番号   = to_number(?)" +
                        " AND exists (select 1 from cmuser.MSカード情報 T3 where T3.顧客番号=T1.顧客番号)");
                sqlca.restAndExecute(syori_kokyaku_no);
                sqlca.fetch();
                sqlca.recData(mmkokgb_t.kokyaku_no);
                /* 2017.12.04 自動退会対応 End */

                if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** MM顧客企業別属性情報 SELECT RESULT=[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
                }
                /* データ無し以外のエラーの場合処理終了 */
                if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                    if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** MM顧客企業別属性情報 SELECT ERR=[%d]\n",
                                sqlca.sqlcode);
                        /*------------------------------------------------------------*/
                    }
                    memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                    sprintf( chg_format_buf, "顧客番号=%s", syori_kokyaku_no.arr );
                    APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                            "MM顧客企業別属性情報", chg_format_buf, 0, 0);
                    return C_const_NG;
                }

                /* データ無しの場合処理 */
                if ( sqlca.sqlcode == C_const_Ora_NOTFOUND ) {
                    if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_main *** MM顧客企業別属性情報 SELECT=[%s]\n", "NOTFOUND");
                        /*------------------------------------------------------------*/
                    }
                    if(file_flg == 0){
                        if (DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTleavB_main *** MM顧客企業別属性情報 顧客番号=%s\n", syori_kokyaku_no.arr);
                            /*------------------------------------------------------------*/
                        }
                        strncpy(mmkokgb_t.kokyaku_no, inp_taikai_t.kokyaku_no, sizeof(mmkokgb_t.kokyaku_no.arr));
                        mmkokgb_t.kokyaku_no.len = strlen(inp_taikai_t.kokyaku_no);
                        togo_flg = 1;
                    }else{
                        if (DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTleavB_main *** スキップ 顧客番号=%s\n", syori_kokyaku_no.arr);
                            /*------------------------------------------------------------*/
                        }
                        continue;
                    }
                }

                /*-----------------------------------------------*/
                /* （６）５．顧客情報取得 （C_GetCmMaster）*/
                /*-----------------------------------------------*/
                /* 初期化 */
                rtn_status = new IntegerDto();
                mmkoinf_t = new MM_KOKYAKU_INFO_TBL();
                memset(mmkoinf_t, 0x00, sizeof(mmkoinf_t) );
                strcpy(mmkoinf_t.kokyaku_no, mmkokgb_t.kokyaku_no);
                mmkoinf_t.kokyaku_no.len = strlen(mmkokgb_t.kokyaku_no);

                rtn_cd = cmBTfuncB.C_GetCmMaster( mmkoinf_t, rtn_status );

                if ( rtn_cd == C_const_NG ) {   /* エラー発生時 処理終了              */
                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** C_GetCmMaster NG=[%d]\n",rtn_cd);
                                        /*------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_GetCmMaster",
                            rtn_cd, rtn_status, 0, 0, 0);
                    return C_const_NG;
                }else if( rtn_cd == C_const_NOTEXISTS ){
                    rtn_cd = cmBTleavB_WriteFile();   /* ★処理結果ファイル書込み */
                    if ( rtn_cd != C_const_OK ) {
                        return C_const_NG;
                    }
                    continue;
                }

                /* 検索結果の処理結果ファイルへの設定 */
                strncpy( out_kekka_t.kokyaku_no,                /*  1 顧客番号        */
                        mmkoinf_t.kokyaku_no, sizeof(out_kekka_t.kokyaku_no)-1 );

                /*  2 顧客名称        */
                sprintf(out_kekka_t.kokyaku_mesho, "%s　%s",mmkoinf_t.kokyaku_myoji, mmkoinf_t.kokyaku_name);                                            /* 2022/10/14 MCCM初版 ADD */
//      strncpy( out_kekka_t.kokyaku_mesho,                                                                                                                      /* 2022/10/14 MCCM初版 DEL */
//                mmkoinf_t.kokyaku_mesho, sizeof(out_kekka_t.kokyaku_mesho)-1 );                                                                        /* 2022/10/14 MCCM初版 DEL */

                /*  3 顧客カナ名称    */
                sprintf(out_kekka_t.kokyaku_mesho, "%s　%s",mmkoinf_t.kana_kokyaku_myoji, mmkoinf_t.kana_kokyaku_name);                                  /* 2022/10/14 MCCM初版 ADD */
//      strncpy( out_kekka_t.kokyaku_kana_mesho,                                                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                mmkoinf_t.kokyaku_kana_mesho, sizeof(out_kekka_t.kokyaku_kana_mesho)-1 );                                                              /* 2022/10/14 MCCM初版 DEL */

                sprintf( out_kekka_t.nenre,                     /*  4 年齢            */
                        "%d", mmkoinf_t.nenre );
                sprintf( out_kekka_t.tanjo_y,                   /*  5 誕生年          */
                        "%d", mmkoinf_t.tanjo_y );
                sprintf( out_kekka_t.tanjo_m,                   /*  6 誕生月          */
                        "%d", mmkoinf_t.tanjo_m );
                sprintf( out_kekka_t.tanjo_d,                   /*  7 誕生日          */
                        "%d", mmkoinf_t.tanjo_d );
                sprintf( out_kekka_t.sebetsu,                   /*  8 性別            */
                        "%d", mmkoinf_t.sebetsu );

                /*-----------------------------------------------*/
                /* （６）６．MSカード情報を更新                  */ /* ※顧客ステータス退会ファイルの場合はスキップ */                                                   /* 2022/10/14 MCCM初版 MOD */
                /*-----------------------------------------------*/
                if(file_flg == 0 && togo_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                    /* 初期化 */
                    mmkoinf_t = new MM_KOKYAKU_INFO_TBL();
                    memset(mmkoinf_t, 0x00, sizeof(mmkoinf_t) );
                    /* MSカード情報更新処理 */
                    /*  2014/11/26 更新対象(仮退会)追加 */
//                    EXEC SQL UPDATE /*+ INDEX(IXMSCARD00 MSカード情報) */
//                    MSカード情報@CMSD
//                    SET カードステータス = 8,
//                            理由コード       = 2011,                                                                                                                        /* 2022/10/14 MCCM初版 MOD */
//                    終了年月日       = :this_date,
//                            バッチ更新日     = :this_date,
//                            最終更新日       = :this_date,
//                            最終更新日時     = SYSDATE,
//                            最終更新プログラムＩＤ = :Program_Name
//                    WHERE 顧客番号 = to_number(:syori_kokyaku_no)
//                    AND ( カードステータス = 0 or カードステータス = 7 or カードステータス = 1                                                                          /* 2022/12/19 MCCM初版 MOD */
//                            or ( カードステータス = 8 AND 理由コード = 2011 ) ) ;

                    sqlca=sqlcaManager.get("MM_KOKYAKU_INFO_TBL");
                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MSカード情報 SET カードステータス = 8,理由コード       = 2011," +
                            " 終了年月日       = ?," +
                            " バッチ更新日     = ?," +
                            " 最終更新日       = ?," +
                            " 最終更新日時     = SYSDATE()," +
                            " 最終更新プログラムＩＤ = ?" +
                            " WHERE 顧客番号 = to_number(?)" +
                            " AND ( カードステータス = 0 or カードステータス = 7 or カードステータス = 1 " +
                            "         or ( カードステータス = 8 AND 理由コード = 2011 ) )";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.query(this_date, this_date, this_date, Program_Name, syori_kokyaku_no);
                    /* エラーの場合処理を異常終了する */
                    switch ( sqlca.sqlcode ) {
                        case   C_const_Ora_OK:
                            break;
                        case   C_const_Ora_NOTFOUND:
                            break;
                        default:
                            if (DBG_LOG){
                                                        /*----------------------------------------------------------------*/
                                                        C_DbgMsg( "*** UpdateCard *** MSカード情報更新NG %s\n", "");
                                                        /*----------------------------------------------------------------*/
                            }
                            /* DBERR */
                            sprintf( chg_format_buf,  "顧客番号=%s", syori_kokyaku_no.arr );
                            APLOG_WT( "904", 0, null, "UPDATE",
                                    sqlca.sqlcode, "MSカード情報",
                                chg_format_buf, 0, 0);
                            /* 処理を終了する */
                            return C_const_NG;
                    }
                }                                                                                                                                                     /* 2022/10/14 MCCM初版 ADD */

                /*-----------------------------------------------*/
                /* （６）１０．家族制度処理                      */
                /*-----------------------------------------------*/

                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_main *** 家族制度処理 %s\n", "start");
                                /*------------------------------------------------------------*/
                }
                /*-----------------------------------------------*/
                /* （６）７．顧客制度情報取得 */
                /*-----------------------------------------------*/
                mskosed_t = new MS_KOKYAKU_SEDO_INFO_TBL();
                memset(mskosed_t, 0x00, sizeof(mskosed_t));
                memcpy(mskosed_t.kokyaku_no, mmkokgb_t.kokyaku_no, sizeof(mskosed_t.kokyaku_no));
                mskosed_t.kokyaku_no.len = mmkokgb_t.kokyaku_no.len;
                rtn_cd = cmBTfuncB.C_GetCsMaster(mskosed_t, rtn_status);
                if (rtn_cd == C_const_NG) {
                    /* error */
                    APLOG_WT("903", 0, null, "C_GetCsMaster", rtn_cd, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }

                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_main *** 家族制度処理 mskosed_t.kazoku_id=%s\n", mskosed_t.kazoku_id.arr);                                                       /* 2022/10/14 MCCM初版 MOD */
                                /*------------------------------------------------------------*/
                }

                /*-----------------------------------------------*/
                /* （６）８．家族制度情報で、TS利用可能ポイント情報をロック */
                /*-----------------------------------------------*/
                /* MS顧客制度情報ロック(WAIT) */
                if (atol(mskosed_t.kazoku_id.arr) <= 0) {                                                                                                        /* 2022/10/14 MCCM初版 MOD */
                    /* 家族ＩＤ＞０でない場合 */
                    rtn_status =  new IntegerDto();
                    rtn_cd = C_const_OK;

                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** ロック 顧客番号=[%s]\n",
                                                mmkokgb_t.kokyaku_no.arr);
                                        dbg_kdatalock = 0;
                                        /*------------------------------------------------------------*/
                    }
                        rtn_cd = C_KdataLock( mmkokgb_t.kokyaku_no.strDto(), "1", rtn_status);

                    if ( rtn_cd != C_const_OK ) {   /* ERROR */
                        if (DBG_LOG){
                                                /*-----------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** ポイント・顧客ロック ret=[%d]\n", rtn_cd);
                                                C_DbgMsg("*** cmBTleavB_main *** ポイント・顧客ロック sts=[%d]\n", rtn_status);
                                                dbg_kdatalock = 0;
                                                /*-----------------------------------------------------------------*/
                        }
                        if ( rtn_cd == C_const_NOTEXISTS ) {  /* NOT FOUND */
                            APLOG_WT("913", 0, null, mmkokgb_t.kokyaku_no.arr, 0, 0, 0, 0, 0);
                            rtn_cd = cmBTleavB_WriteFile();   /* ★処理結果ファイル書込み */
                            if ( rtn_cd != C_const_OK ) {
                                return C_const_NG;
                            }
                            continue;
                        } else {                              /* ERROR */
                            APLOG_WT("903", 0, null, "C_KdataLock",
                                    rtn_cd, rtn_status, 0, 0, 0);
                            return C_const_NG;
                        }
                    }
                }else{
                    /* 家族ＩＤ＞０の場合 */
                    mskased_t = new MS_KAZOKU_SEDO_INFO_TBL();
                    memset(mskased_t, 0x00, sizeof(mskased_t));
                    /* 家族ＩＤでＭＳ家族情報を取得する */
                    strcpy(mskased_t.kazoku_id, mskosed_t.kazoku_id);                                                                        /* 2022/10/14 MCCM初版 MOD */
                    mskased_t.kazoku_id.len = mskosed_t.kazoku_id.len;                                                                                               /* 2022/12/12 MCCM初版 ADD */
                    rtn_cd = getKazokuSedoInfo(mskased_t, rtn_status);
                    if (rtn_cd != C_const_OK) {
                        if (DBG_LOG){
                                                /*------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** ＭＳ家族情報 SELECT rtn_cd=[%d]\n", rtn_cd);
                                                /*------------------------------------------------------------*/
                        }
                        /* error */
                        APLOG_WT("903", 0, null, "getKazokuSedoInfo", rtn_cd, rtn_status, 0, 0, 0);
                        /* 処理を終了する */
                        return C_const_NG;
                    }

                    /* 家族ｎ顧客番号を顧客ロックする(顧客ＩＤ順) */

                    memset(kazoku_kokyaku_wk[0], 0x00, 30);    /* 家族１顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[0] = new StringDto();
                    memset(kazoku_kokyaku_wk[1], 0x00, 30);    /* 家族２顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[1] = new StringDto();
                    memset(kazoku_kokyaku_wk[2], 0x00, 30);    /* 家族３顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[2] = new StringDto();
                    memset(kazoku_kokyaku_wk[3], 0x00, 30);    /* 家族４顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[3] = new StringDto();
                    memset(kazoku_kokyaku_wk[4], 0x00, 30);    /* 家族５顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[4] = new StringDto();
                    memset(kazoku_kokyaku_wk[5], 0x00, 30);    /* 家族６顧客番号 */                                                                                  /* 2022/12/19 MCCM初版 MOD */
                    kazoku_kokyaku_wk[5] = new StringDto();

//              sprintf(kazoku_kokyaku_wk[0], "%015ld%08d", atol((char *)mskased_t.kazoku_oya_kokyaku_no.arr), mskased_t.kazoku_oya_toroku_ymd);                 /* 2022/10/14 MCCM初版 DEL */
                    sprintf(kazoku_kokyaku_wk[0], "%015d%08.0f%06d", atol(mskased_t.kazoku_1_kokyaku_no.longVal()), mskased_t.kazoku_1_toroku_ymd.floatVal(), mskased_t.kazoku_1_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */
                    sprintf(kazoku_kokyaku_wk[1], "%015d%08.0f%06d", atol(mskased_t.kazoku_2_kokyaku_no.longVal()), mskased_t.kazoku_2_toroku_ymd.floatVal(), mskased_t.kazoku_2_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */
                    sprintf(kazoku_kokyaku_wk[2], "%015d%08.0f%06d", atol(mskased_t.kazoku_3_kokyaku_no.longVal()), mskased_t.kazoku_3_toroku_ymd.floatVal(), mskased_t.kazoku_3_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */
                    sprintf(kazoku_kokyaku_wk[3], "%015d%08.0f%06d", atol(mskased_t.kazoku_4_kokyaku_no.longVal()), mskased_t.kazoku_4_toroku_ymd.floatVal(), mskased_t.kazoku_4_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */
                    sprintf(kazoku_kokyaku_wk[4], "%015d%08.0f%06d", atol(mskased_t.kazoku_5_kokyaku_no.longVal()), mskased_t.kazoku_5_toroku_ymd.floatVal(), mskased_t.kazoku_5_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */
                    sprintf(kazoku_kokyaku_wk[5], "%015d%08.0f%06d", atol(mskased_t.kazoku_6_kokyaku_no.longVal()), mskased_t.kazoku_6_toroku_ymd.floatVal(), mskased_t.kazoku_6_toroku_time.intVal());                       /* 2022/12/19 MCCM初版 MOD */

                    /* ワークをソートする */
                    qsort(kazoku_kokyaku_wk, 6, 30);

                    /* 2022/12/19 MCCM初版 MOD */

                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid1=[%s]\n", kazoku_kokyaku_wk[0]);                                                            /* 2022/10/14 MCCM初版 MOD */
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid2=[%s]\n", kazoku_kokyaku_wk[1]);                                                            /* 2022/10/14 MCCM初版 MOD */
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid3=[%s]\n", kazoku_kokyaku_wk[2]);                                                            /* 2022/10/14 MCCM初版 MOD */
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid4=[%s]\n", kazoku_kokyaku_wk[3]);                                                            /* 2022/10/14 MCCM初版 MOD */
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid5=[%s]\n", kazoku_kokyaku_wk[4]);                                                            /* 2022/10/14 MCCM初版 MOD */
                                        C_DbgMsg("*** cmBTleavB_main *** C_KdataLock uid6=[%s]\n", kazoku_kokyaku_wk[5]);                                                            /* 2022/10/14 MCCM初版 ADD */
                                        /*------------------------------------------------------------*/
                    }

                            lock_chk.arr = DEF_OFF;

                    for (i = 0; i < 6; i++) {                                                                                                                        /* 2022/10/14 MCCM初版 MOD */
                        if (memcmp(kazoku_kokyaku_wk[i], "000000000000000",15) != 0) {
                            memset(lock_kokyaku_no, 0x00, sizeof(lock_kokyaku_no));
                            memcpy(lock_kokyaku_no, kazoku_kokyaku_wk[i],15);
                            /* 顧客ロック処理 */
                            rtn_cd = C_KdataLock(lock_kokyaku_no, "1", rtn_status);

                            if (rtn_cd == C_const_NOTEXISTS) {
                                /* 該当顧客なし */
                                APLOG_WT("913", 0, null, lock_kokyaku_no, 0, 0, 0, 0, 0);
                                rtn_cd = cmBTleavB_WriteFile();   /* ★処理結果ファイル書込み */
                                if ( rtn_cd != C_const_OK ) {
                                    return C_const_NG;
                                }
                                lock_chk.arr = DEF_ON;
                                break;
                            }
                            else if (rtn_cd != C_const_OK) {
                                if (DBG_LOG){
                                                                /*------------------------------------------------------------*/
                                                                C_DbgMsg("*** cmBTleavB_main *** C_KdataLock NG rtn_cd=[%d]\n", rtn_cd);
                                                                C_DbgMsg("*** cmBTleavB_main *** C_KdataLock NG rtn_status=[%d]\n", rtn_status);
                                                                /*------------------------------------------------------------*/
                                }

                                APLOG_WT("903", 0, null, "C_KdataLock",
                                        rtn_cd, rtn_status, 0, 0, 0);
                                return C_const_NG;

                            }
                        }
                    }

                    /* 該当顧客なし   */
                    /* 次のレコードへ */
                    if(lock_chk.intVal() == DEF_ON){
                        continue;
                    }
                }

                /*-----------------------------------------------*/
                /*  （６）９．顧客属性情報取得 */
                /*-----------------------------------------------*/
                /* 初期化 */
                rtn_status =  new IntegerDto();
                mmkozok_t = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
                memset( mmkozok_t, 0x00, sizeof(mmkozok_t) );
                strcpy(mmkozok_t.kokyaku_no, mmkokgb_t.kokyaku_no);
                mmkozok_t.kokyaku_no.len = strlen(mmkokgb_t.kokyaku_no);

                rtn_cd = cmBTfuncB.C_GetCzMaster( mmkozok_t, rtn_status );
                if ( rtn_cd == C_const_NG ) {   /* エラー発生時 処理終了              */
                    APLOG_WT("903", 0, null, "C_GetCzMaster",
                            rtn_cd, rtn_status, 0, 0, 0);
                    return C_const_NG;
                }else if ( rtn_cd == C_const_NOTEXISTS ) {
                    rtn_cd = cmBTleavB_WriteFile();   /* ★処理結果ファイル書込み */
                    if ( rtn_cd != C_const_OK ) {
                        return C_const_NG;
                    }
                    continue;
                }

                /* 2022/10/14 MCCM初版 ADD START */
                /* 都道府県名取得 */
                memset( prefectures, 0x00, sizeof(prefectures) );
                if(mmkozok_t.todofuken_cd.intVal() != 0){
                    rtn_cd = C_GetPrefectures( mmkozok_t.todofuken_cd.intVal(), prefectures, rtn_status );
                    /* 都道府県名取得処理が異常の場合 */
                    if (rtn_cd != C_const_OK) {
                        APLOG_WT("903", 0, null, "C_GetPrefectures", rtn_cd,
                                rtn_status, 0, 0, 0);
                        /* 処理を終了する */
                        return (C_const_NG);
                    }
                }
                /* 2022/10/14 MCCM初版 ADD END */

                /* 処理結果ファイルへの設定 */
                strncpy( out_kekka_t.yubin_no,                  /*  9 郵便番号        */
                        mmkozok_t.yubin_no, sizeof(out_kekka_t.yubin_no)-1 );
                strncpy( out_kekka_t.yubin_no_cd,               /* 10 郵便番号コード  */
                        mmkozok_t.yubin_no_cd, sizeof(out_kekka_t.yubin_no_cd)-1 );
                strncpy( out_kekka_t.jusho_1,                   /* 11 住所１          */
                        prefectures.arr, sizeof(prefectures)-1 );                                                                                                      /* 2022/10/14 MCCM初版 MOD */
                strncpy( out_kekka_t.jusho_2,                   /* 12 住所２          */
                        mmkozok_t.address, sizeof(out_kekka_t.jusho_2)-1 );                                                                                        /* 2022/10/14 MCCM初版 MOD */
//      strncpy( out_kekka_t.jusho_3,                   /* 13 住所３          */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//            mmkozok_t.jusho_3, sizeof(out_kekka_t.jusho_3)-1 );                                                                                        /* 2022/10/14 MCCM初版 DEL */
                strncpy( out_kekka_t.denwa_no_1,                /* 14 電話番号１      */
                        mmkozok_t.denwa_no_1, sizeof(out_kekka_t.denwa_no_1)-1 );
                strncpy( out_kekka_t.denwa_no_2,                /* 15 電話番号２      */
                        mmkozok_t.denwa_no_2, sizeof(out_kekka_t.denwa_no_2)-1 );
                strncpy( out_kekka_t.denwa_no_3,                /* 16 電話番号３      */
                        mmkozok_t.denwa_no_3, sizeof(out_kekka_t.denwa_no_3)-1 );
                strncpy( out_kekka_t.denwa_no_4,                /* 17 電話番号４      */
                        mmkozok_t.denwa_no_4, sizeof(out_kekka_t.denwa_no_4)-1 );
                strncpy( out_kekka_t.kensaku_denwa_no_1,        /* 18 検索電話番号１  */
                        mmkozok_t.kensaku_denwa_no_1, sizeof(out_kekka_t.kensaku_denwa_no_1)-1 );
                strncpy( out_kekka_t.kensaku_denwa_no_2,        /* 19 検索電話番号２  */
                        mmkozok_t.kensaku_denwa_no_2, sizeof(out_kekka_t.kensaku_denwa_no_2)-1 );
                strncpy( out_kekka_t.kensaku_denwa_no_3,        /* 20 検索電話番号３  */
                        mmkozok_t.kensaku_denwa_no_3, sizeof(out_kekka_t.kensaku_denwa_no_3)-1 );
                strncpy( out_kekka_t.kensaku_denwa_no_4,        /* 21 検索電話番号４  */
                        mmkozok_t.kensaku_denwa_no_4, sizeof(out_kekka_t.kensaku_denwa_no_4)-1 );
                strncpy( out_kekka_t.email_address_1,           /* 22 Ｅメールアドレス１ */
                        mmkozok_t.email_address_1, sizeof(out_kekka_t.email_address_1)-1 );
                strncpy( out_kekka_t.email_address_2,           /* 23 Ｅメールアドレス２ */
                        mmkozok_t.email_address_2, sizeof(out_kekka_t.email_address_2)-1 );
                strncpy( out_kekka_t.shokugyo,                  /* 24 職業            */
                        mmkozok_t.shokugyo, sizeof(out_kekka_t.shokugyo)-1 );

                sprintf( out_kekka_t.kinmu_kbn,                 /* 25 勤務区分        */
                        "%d", mmkozok_t.kinmu_kbn );
                strncpy( out_kekka_t.email_address_3,           /* 26 Ｅメールアドレス３ */
                        mmkozok_t.email_address_3, sizeof(out_kekka_t.email_address_3)-1 );

                strncpy( out_kekka_t.email_address_4,           /* 27 Ｅメールアドレス４ */
                        mmkozok_t.email_address_4, sizeof(out_kekka_t.email_address_4)-1 );

                /* MS家族制度情報を更新する */
                if (atol(mskosed_t.kazoku_id.arr) > 0 && (file_flg == 0) && (togo_flg == 0)) {                                                                                      /* 2022/10/14 MCCM初版 MOD */
                    /* 家族ＩＤ＞０の場合 */
                    /*-----------------------------------------------*/
                    /*  （６）１０．MS家族制度情報更新               */ /* ※顧客ステータス退会ファイルの場合はスキップ */                                               /* 2022/10/14 MCCM初版 MOD */
                    /*-----------------------------------------------*/

                    /* 家族の顧客番号の有効な件数をカウントする */

                    ko_no_cnt = 0;
//              if (atol((char *)mskased_t.kazoku_oya_kokyaku_no.arr) != 0) ko_no_cnt++;                                                                         /* 2022/10/14 MCCM初版 DEL */
                    if (atol(mskased_t.kazoku_1_kokyaku_no.arr) != 0) ko_no_cnt++;
                    if (atol(mskased_t.kazoku_2_kokyaku_no.arr) != 0) ko_no_cnt++;
                    if (atol(mskased_t.kazoku_3_kokyaku_no.arr) != 0) ko_no_cnt++;
                    if (atol(mskased_t.kazoku_4_kokyaku_no.arr) != 0) ko_no_cnt++;
                    if (atol(mskased_t.kazoku_5_kokyaku_no.arr) != 0) ko_no_cnt++;                                                                           /* 2022/10/14 MCCM初版 ADD */
                    if (atol(mskased_t.kazoku_6_kokyaku_no.arr) != 0) ko_no_cnt++;                                                                           /* 2022/10/14 MCCM初版 ADD */


                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** ＭＳ家族情報 ko_no_cnt=%d\n", ko_no_cnt);
                                        /*------------------------------------------------------------*/
                    }

                    if (ko_no_cnt >= 3) {
                        /* 家族処理情報が維持される場合 */
                    if (DBG_LOG){
                                            /*------------------------------------------------------------*/
                                            C_DbgMsg("*** cmBTleavB_main *** ＭＳ家族情報 (%s)\n", "家族処理情報が維持される場合");
                                            /*------------------------------------------------------------*/
                    }
                                /* 年間家族ランクＵＰ対象金額０～９、           *
                                 * 年次ランクコード０～９、                     *
                                 * 月間家族ランクＵＰ金額００１～０１２、       *
                                 * 月間家族ランクＵＰ金額１０１～１１２、       *
                                 * 家族ランクＵＰ金額最終更新日          を設定 */
                                /* ランクＵＰ情報取得処理 */
                                rtn_cd = getRankUpMoney();
                    if (DBG_LOG){
                                            /*------------------------------------------------------------*/
                                            C_DbgMsg("*** cmBTleavB_main *** getRankUpMoney ret=%d\n", rtn_cd);
                                            /*------------------------------------------------------------*/
                    }

                        if (rtn_cd == C_const_NG) {
                            /* error */
                            APLOG_WT("903", 0, null, "getRankUpMoney", rtn_cd, 0, 0, 0, 0);
                            /* 処理を終了する */
                            return C_const_NG;
                        }

                        /* 家族顧客番号・家族登録日・家族登録時刻 */
                        memset(reset_kazoku_kokyaku[0], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        memset(reset_kazoku_kokyaku[1], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        memset(reset_kazoku_kokyaku[2], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        memset(reset_kazoku_kokyaku[3], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        memset(reset_kazoku_kokyaku[4], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        memset(reset_kazoku_kokyaku[5], 0x00, 30);                                                                                              /* 2022/12/19 MCCM初版 MOD */
                        reset_kazoku_kokyaku[0] = new StringDto();
                        reset_kazoku_kokyaku[1] = new StringDto();
                        reset_kazoku_kokyaku[2] = new StringDto();
                        reset_kazoku_kokyaku[3] = new StringDto();
                        reset_kazoku_kokyaku[4] = new StringDto();
                        reset_kazoku_kokyaku[5] = new StringDto();

//                      sprintf(reset_kazoku_kokyaku[0], "%015ld%08d", atol((char *)mskased_t.kazoku_oya_kokyaku_no.arr), mskased_t.kazoku_oya_toroku_ymd);      /* 2022/10/14 MCCM初版 DEL */
                        sprintf(reset_kazoku_kokyaku[0], "%015d%08.0f%06d", atol(mskased_t.kazoku_1_kokyaku_no.longVal()), mskased_t.kazoku_1_toroku_ymd.floatVal(), mskased_t.kazoku_1_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */
                        sprintf(reset_kazoku_kokyaku[1], "%015d%08.0f%06d", atol(mskased_t.kazoku_2_kokyaku_no.longVal()), mskased_t.kazoku_2_toroku_ymd.floatVal(), mskased_t.kazoku_2_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */
                        sprintf(reset_kazoku_kokyaku[2], "%015d%08.0f%06d", atol(mskased_t.kazoku_3_kokyaku_no.longVal()), mskased_t.kazoku_3_toroku_ymd.floatVal(), mskased_t.kazoku_3_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */
                        sprintf(reset_kazoku_kokyaku[3], "%015d%08.0f%06d", atol(mskased_t.kazoku_4_kokyaku_no.longVal()), mskased_t.kazoku_4_toroku_ymd.floatVal(), mskased_t.kazoku_4_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */
                        sprintf(reset_kazoku_kokyaku[4], "%015d%08.0f%06d", atol(mskased_t.kazoku_5_kokyaku_no.longVal()), mskased_t.kazoku_5_toroku_ymd.floatVal(), mskased_t.kazoku_5_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */
                        sprintf(reset_kazoku_kokyaku[5], "%015d%08.0f%06d", atol(mskased_t.kazoku_6_kokyaku_no.longVal()), mskased_t.kazoku_6_toroku_ymd.floatVal(), mskased_t.kazoku_6_toroku_time.intVal());            /* 2022/12/19 MCCM初版 MOD */

                        /* MM顧客企業別属性情報の顧客番号と一致するデータを削除する */
                        for (i = 0; i < 6; i++) {                                                                                                                /* 2022/10/14 MCCM初版 MOD */
                            if (memcmp(mmkokgb_t.kokyaku_no.strDto(), reset_kazoku_kokyaku[i].strVal(), 15) == 0) {
                                memset(reset_kazoku_kokyaku[i], '0', 29);
                            }
                        }

                        /* 削除した顧客番号より下位の顧客番号を上にあげる */
                        if (atol(reset_kazoku_kokyaku[0]) == 0) {
                            strcpy(reset_kazoku_kokyaku[0], reset_kazoku_kokyaku[1]);
                            strcpy(reset_kazoku_kokyaku[1], reset_kazoku_kokyaku[2]);
                            strcpy(reset_kazoku_kokyaku[2], reset_kazoku_kokyaku[3]);
                            strcpy(reset_kazoku_kokyaku[3], reset_kazoku_kokyaku[4]);
                            strcpy(reset_kazoku_kokyaku[4], reset_kazoku_kokyaku[5]);                                                                            /* 2022/10/14 MCCM初版 ADD */
//                            memset(reset_kazoku_kokyaku[5], '0', 29);
                            reset_kazoku_kokyaku[5].arr= appendZero(29);
                            /* 2022/10/14 MCCM初版 MOD */
                        }else if (atol(reset_kazoku_kokyaku[1]) == 0) {
                            strcpy(reset_kazoku_kokyaku[1], reset_kazoku_kokyaku[2]);
                            strcpy(reset_kazoku_kokyaku[2], reset_kazoku_kokyaku[3]);
                            strcpy(reset_kazoku_kokyaku[3], reset_kazoku_kokyaku[4]);
                            strcpy(reset_kazoku_kokyaku[4], reset_kazoku_kokyaku[5]);                                                                            /* 2022/10/14 MCCM初版 ADD */
//                            memset(reset_kazoku_kokyaku[5], '0', 29);
                            reset_kazoku_kokyaku[5].arr= appendZero(29);
                            /* 2022/10/14 MCCM初版 MOD */
                        }else if (atol(reset_kazoku_kokyaku[2]) == 0) {
                            strcpy(reset_kazoku_kokyaku[2], reset_kazoku_kokyaku[3]);
                            strcpy(reset_kazoku_kokyaku[3], reset_kazoku_kokyaku[4]);
                            strcpy(reset_kazoku_kokyaku[4], reset_kazoku_kokyaku[5]);                                                                            /* 2022/10/14 MCCM初版 ADD */
//                            memset(reset_kazoku_kokyaku[5], '0', 29);
                            reset_kazoku_kokyaku[5].arr= appendZero(29);
                            /* 2022/10/14 MCCM初版 MOD */
                        }else if (atol(reset_kazoku_kokyaku[3]) == 0) {
                            strcpy(reset_kazoku_kokyaku[3], reset_kazoku_kokyaku[4]);
                            strcpy(reset_kazoku_kokyaku[4], reset_kazoku_kokyaku[5]);                                                                            /* 2022/10/14 MCCM初版 ADD */
//                            memset(reset_kazoku_kokyaku[5], '0', 29);
                            reset_kazoku_kokyaku[5].arr= appendZero(29);
                            /* 2022/10/14 MCCM初版 MOD */
                        }else if (atol(reset_kazoku_kokyaku[4]) == 0) {                                                                                          /* 2022/10/14 MCCM初版 ADD */
                            strcpy(reset_kazoku_kokyaku[4], reset_kazoku_kokyaku[5]);                                                                            /* 2022/10/14 MCCM初版 ADD */
//                            memset(reset_kazoku_kokyaku[5], '0', 29);
                            reset_kazoku_kokyaku[5].arr= appendZero(29);
                            /* 2022/10/14 MCCM初版 ADD */
                        }                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */

                        /* ホスト変数を上書きする */
                        /*  2014/11/26 顧客番号削除の場合、NULL設定 */
//                      if ( atol(reset_kazoku_kokyaku[0]) != 0 ) {                                                                                              /* 2022/10/14 MCCM初版 DEL */
//                          memcpy(mskased_t.kazoku_oya_kokyaku_no.arr, reset_kazoku_kokyaku[0], 15);                                                            /* 2022/10/14 MCCM初版 DEL */
//                          mskased_t.kazoku_oya_kokyaku_no.len = 15;                                                                                            /* 2022/10/14 MCCM初版 DEL */
//                      } else {                                                                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                          memset(mskased_t.kazoku_oya_kokyaku_no.arr, 0x00, sizeof(mskased_t.kazoku_oya_kokyaku_no.arr));                                      /* 2022/10/14 MCCM初版 DEL */
//                          mskased_t.kazoku_oya_kokyaku_no.len = 0;                                                                                             /* 2022/10/14 MCCM初版 DEL */
//                      }                                                                                                                                        /* 2022/10/14 MCCM初版 DEL */
                        if (! StringUtils.equals(reset_kazoku_kokyaku[0].strVal(),"00000000000000000000000000000")  ) {                                                                                              /* 2022/10/14 MCCM初版 MOD */
                            memcpy(mskased_t.kazoku_1_kokyaku_no, reset_kazoku_kokyaku[0], 15);                                                              /* 2022/10/14 MCCM初版 MOD */
                            mskased_t.kazoku_1_kokyaku_no.len = 15;
                        } else {
                            memset(mskased_t.kazoku_1_kokyaku_no, 0x00, sizeof(mskased_t.kazoku_1_kokyaku_no));
//                            mskased_t.kazoku_1_kokyaku_no.len = 0;
                        }
                        if ( ! StringUtils.equals(reset_kazoku_kokyaku[1].strVal(),"00000000000000000000000000000") ) {                                                                                              /* 2022/10/14 MCCM初版 MOD */
                            memcpy(mskased_t.kazoku_2_kokyaku_no, reset_kazoku_kokyaku[1], 15);                                                              /* 2022/10/14 MCCM初版 MOD */
                            mskased_t.kazoku_2_kokyaku_no.len = 15;
                        } else {
                            memset(mskased_t.kazoku_2_kokyaku_no, 0x00, sizeof(mskased_t.kazoku_2_kokyaku_no));
//                            mskased_t.kazoku_2_kokyaku_no.len = 0;
                        }
                        if (! StringUtils.equals(reset_kazoku_kokyaku[2].strVal(),"00000000000000000000000000000")  ) {                                                                                              /* 2022/10/14 MCCM初版 MOD */
                            memcpy(mskased_t.kazoku_3_kokyaku_no, reset_kazoku_kokyaku[2], 15);                                                              /* 2022/10/14 MCCM初版 MOD */
                            mskased_t.kazoku_3_kokyaku_no.len = 15;
                        } else {
                            memset(mskased_t.kazoku_3_kokyaku_no, 0x00, sizeof(mskased_t.kazoku_3_kokyaku_no));
//                            mskased_t.kazoku_3_kokyaku_no.len = 0;
                        }
                        if (! StringUtils.equals(reset_kazoku_kokyaku[3].strVal(),"00000000000000000000000000000")  ) {                                                                                              /* 2022/10/14 MCCM初版 MOD */
                            memcpy(mskased_t.kazoku_4_kokyaku_no, reset_kazoku_kokyaku[3], 15);                                                              /* 2022/10/14 MCCM初版 MOD */
                            mskased_t.kazoku_4_kokyaku_no.len = 15;
                        } else {
                            memset(mskased_t.kazoku_4_kokyaku_no, 0x00, sizeof(mskased_t.kazoku_4_kokyaku_no));
//                            mskased_t.kazoku_4_kokyaku_no.len = 0;
                        }
                        if ( ! StringUtils.equals(reset_kazoku_kokyaku[4].strVal(),"00000000000000000000000000000") ) {                                                                                              /* 2022/10/14 MCCM初版 ADD */
                            memcpy(mskased_t.kazoku_5_kokyaku_no, reset_kazoku_kokyaku[4], 15);                                                              /* 2022/10/14 MCCM初版 ADD */
                            mskased_t.kazoku_5_kokyaku_no.len = 15;                                                                                              /* 2022/10/14 MCCM初版 ADD */
                        } else {                                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
                            memset(mskased_t.kazoku_5_kokyaku_no , 0x00, sizeof(mskased_t.kazoku_5_kokyaku_no ));                                          /* 2022/10/14 MCCM初版 ADD */
//                            mskased_t.kazoku_5_kokyaku_no.len = 0;                                                                                               /* 2022/10/14 MCCM初版 ADD */
                        }
                        if ( ! StringUtils.equals(reset_kazoku_kokyaku[5].strVal(),"00000000000000000000000000000") ) {                                                                                              /* 2022/10/14 MCCM初版 ADD */
                            memcpy(mskased_t.kazoku_6_kokyaku_no, reset_kazoku_kokyaku[5], 15);                                                              /* 2022/10/14 MCCM初版 ADD */
                            mskased_t.kazoku_6_kokyaku_no.len = 15;                                                                                              /* 2022/10/14 MCCM初版 ADD */
                        } else {                                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
                            memset(mskased_t.kazoku_6_kokyaku_no, 0x00, sizeof(mskased_t.kazoku_6_kokyaku_no ));                                          /* 2022/10/14 MCCM初版 ADD */
//                            mskased_t.kazoku_6_kokyaku_no.len = 0;                                                                                               /* 2022/10/14 MCCM初版 ADD */
                        }                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */

//                      mskased_t.kazoku_oya_toroku_ymd = atoi(&(reset_kazoku_kokyaku[0][15]));                                                                  /* 2022/10/14 MCCM初版 DEL */

                        StringDto wk_kazoku_1_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */
                        StringDto wk_kazoku_2_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */
                        StringDto wk_kazoku_3_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */
                        StringDto wk_kazoku_4_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */
                        StringDto wk_kazoku_5_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */
                        StringDto wk_kazoku_6_toroku_ymd = new StringDto(8+1);                                                                                                        /* 2022/12/19 MCCM初版 ADD */

                        memset(wk_kazoku_1_toroku_ymd, 0x00, sizeof(wk_kazoku_1_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */
                        memset(wk_kazoku_2_toroku_ymd, 0x00, sizeof(wk_kazoku_2_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */
                        memset(wk_kazoku_3_toroku_ymd, 0x00, sizeof(wk_kazoku_3_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */
                        memset(wk_kazoku_4_toroku_ymd, 0x00, sizeof(wk_kazoku_4_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */
                        memset(wk_kazoku_5_toroku_ymd, 0x00, sizeof(wk_kazoku_5_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */
                        memset(wk_kazoku_6_toroku_ymd, 0x00, sizeof(wk_kazoku_6_toroku_ymd));                                                                    /* 2022/12/19 MCCM初版 ADD */

                        memcpy(wk_kazoku_1_toroku_ymd, reset_kazoku_kokyaku[0].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */
                        memcpy(wk_kazoku_2_toroku_ymd, reset_kazoku_kokyaku[1].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */
                        memcpy(wk_kazoku_3_toroku_ymd, reset_kazoku_kokyaku[2].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */
                        memcpy(wk_kazoku_4_toroku_ymd, reset_kazoku_kokyaku[3].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */
                        memcpy(wk_kazoku_5_toroku_ymd, reset_kazoku_kokyaku[4].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */
                        memcpy(wk_kazoku_6_toroku_ymd, reset_kazoku_kokyaku[5].substring(15), 8);                                                                       /* 2022/12/19 MCCM初版 ADD */

                        mskased_t.kazoku_1_toroku_ymd.arr = atoi(wk_kazoku_1_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_2_toroku_ymd.arr = atoi(wk_kazoku_2_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_3_toroku_ymd.arr = atoi(wk_kazoku_3_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_4_toroku_ymd.arr = atoi(wk_kazoku_4_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_5_toroku_ymd.arr = atoi(wk_kazoku_5_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 ADD */
                        mskased_t.kazoku_6_toroku_ymd.arr = atoi(wk_kazoku_6_toroku_ymd);                                                                            /* 2022/12/19 MCCM初版 ADD */

                        mskased_t.kazoku_1_toroku_time.arr = atoi(reset_kazoku_kokyaku[0].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_2_toroku_time.arr = atoi(reset_kazoku_kokyaku[1].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_3_toroku_time.arr = atoi(reset_kazoku_kokyaku[2].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_4_toroku_time.arr = atoi(reset_kazoku_kokyaku[3].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_5_toroku_time.arr = atoi(reset_kazoku_kokyaku[4].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */
                        mskased_t.kazoku_6_toroku_time.arr = atoi(reset_kazoku_kokyaku[5].substring(23));                                                                    /* 2022/12/19 MCCM初版 MOD */

                        /*------------------------------------------------------------*/
                        /* 維持される家族処理情報の更新 */
                        /*------------------------------------------------------------*/
                        /* MS家族制度情報の更新 */
                        strcpy(sql_buf, "UPDATE MS家族制度情報          ");
                        /*                      strcat(sql_buf, "SET 家族親顧客番号           = :A1, ");      */                                                                         /* 2022/10/14 MCCM初版 DEL */
                        strcat(sql_buf, "SET 家族１顧客番号           = ?, ");                                                                                 /* 2022/10/14 MCCM初版 MOD */
                        strcat(sql_buf, "家族２顧客番号               = ?, ");
                        strcat(sql_buf, "家族３顧客番号               = ?, ");
                        strcat(sql_buf, "家族４顧客番号               = ?, ");
                        strcat(sql_buf, "家族５顧客番号               = ?, ");                                                                                 /* 2022/10/14 MCCM初版 ADD */
                        strcat(sql_buf, "家族６顧客番号               = ?, ");                                                                                 /* 2022/10/14 MCCM初版 ADD */
                        /*                      strcat(sql_buf, "家族親登録日                 = :A6, ");      */                                                                         /* 2022/10/14 MCCM初版 DEL */
                        strcat(sql_buf, "家族１登録日                 = ?, ");
                        strcat(sql_buf, "家族２登録日                 = ?, ");
                        strcat(sql_buf, "家族３登録日                 = ?, ");
                        strcat(sql_buf, "家族４登録日                 = ?,");
                        strcat(sql_buf, "家族５登録日                 = ?, ");                                                                                 /* 2022/10/14 MCCM初版 ADD */
                        strcat(sql_buf, "家族６登録日                 = ?,");                                                                                  /* 2022/10/14 MCCM初版 ADD */
                        strcat(sql_buf, "家族ランクＵＰ金額最終更新日 = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額０ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額１ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額２ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額３ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額４ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額５ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額６ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額７ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額８ = ?,");
                        strcat(sql_buf, "年間家族ランクＵＰ対象金額９ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００１ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００２ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００３ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００４ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００５ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００６ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００７ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００８ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額００９ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額０１０ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額０１１ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額０１２ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０１ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０２ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０３ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０４ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０５ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０６ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０７ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０８ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１０９ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１１０ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１１１ = ?,");
                        strcat(sql_buf, "月間家族ランクＵＰ金額１１２ = ?,");
                        strcat(sql_buf, "年次ランクコード０           = ?,");
                        strcat(sql_buf, "年次ランクコード１           = ?,");
                        strcat(sql_buf, "年次ランクコード２           = ?,");
                        strcat(sql_buf, "年次ランクコード３           = ?,");
                        strcat(sql_buf, "年次ランクコード４           = ?,");
                        strcat(sql_buf, "年次ランクコード５           = ?,");
                        strcat(sql_buf, "年次ランクコード６           = ?,");
                        strcat(sql_buf, "年次ランクコード７           = ?,");
                        strcat(sql_buf, "年次ランクコード８           = ?,");
                        strcat(sql_buf, "年次ランクコード９           = ?,");
                        strcat(sql_buf, "月次ランクコード００１       = ?,");
                        strcat(sql_buf, "月次ランクコード００２       = ?,");
                        strcat(sql_buf, "月次ランクコード００３       = ?,");
                        strcat(sql_buf, "月次ランクコード００４       = ?,");
                        strcat(sql_buf, "月次ランクコード００５       = ?,");
                        strcat(sql_buf, "月次ランクコード００６       = ?,");
                        strcat(sql_buf, "月次ランクコード００７       = ?,");
                        strcat(sql_buf, "月次ランクコード００８       = ?,");
                        strcat(sql_buf, "月次ランクコード００９       = ?,");
                        strcat(sql_buf, "月次ランクコード０１０       = ?,");
                        strcat(sql_buf, "月次ランクコード０１１       = ?,");
                        strcat(sql_buf, "月次ランクコード０１２       = ?,");
                        strcat(sql_buf, "月次ランクコード１０１       = ?,");
                        strcat(sql_buf, "月次ランクコード１０２       = ?,");
                        strcat(sql_buf, "月次ランクコード１０３       = ?,");
                        strcat(sql_buf, "月次ランクコード１０４       = ?,");
                        strcat(sql_buf, "月次ランクコード１０５       = ?,");
                        strcat(sql_buf, "月次ランクコード１０６       = ?,");
                        strcat(sql_buf, "月次ランクコード１０７       = ?,");
                        strcat(sql_buf, "月次ランクコード１０８       = ?,");
                        strcat(sql_buf, "月次ランクコード１０９       = ?,");
                        strcat(sql_buf, "月次ランクコード１１０       = ?,");
                        strcat(sql_buf, "月次ランクコード１１１       = ?,");
                        strcat(sql_buf, "月次ランクコード１１２       = ?,");
                        strcat(sql_buf, "バッチ更新日                 =  ?,");
                        strcat(sql_buf, "最終更新日                   = ?,");
                        strcat(sql_buf, "最終更新日時              = SYSDATE(),");
                        strcat(sql_buf, "最終更新プログラムＩＤ       = ?,");
                        strcat(sql_buf, "家族１登録時刻               = ?,");
                        strcat(sql_buf, "家族２登録時刻               = ?,");
                        strcat(sql_buf, "家族３登録時刻               = ?,");
                        strcat(sql_buf, "家族４登録時刻               = ?,");
                        strcat(sql_buf, "家族５登録時刻               = ?,");
                        strcat(sql_buf, "家族６登録時刻               = ? ");
                        strcat(sql_buf, "WHERE 家族ＩＤ               = ? ");
                        if (DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("MS家族制度情報UPDATE : sqlbuf=[%s]\n", sql_buf);
                            /*------------------------------------------------------------*/
                        }
                        /* ＳＱＬ文をセットする */
                        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
                        strcpy(WRKSQL, sql_buf);
                        WRKSQL.len = strlen(WRKSQL);

//                        EXEC SQL PREPARE sql_kdatalock from :WRKSQL;
                        sqlca = sqlcaManager.get("sql_kdatalock");
                        sqlca.sql = WRKSQL.strDto();
                        sqlca.prepare();
                        if (sqlca.sqlcode != C_const_Ora_OK) {
                            /* error */
                            if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_main *** MS家族制度情報 PREPARE : sqlca.sqlcode=[%d]\n",sqlca.sqlcode);
                                /*------------------------------------------------------------*/
                            }
                            sprintf(chg_format_buf, "家族ＩＤ=%s", mskased_t.kazoku_id.arr);                                                                 /* 2022/10/14 MCCM初版 MOD */
                            APLOG_WT( "904", 0, null, "PREPARE", sqlca.sqlcode,
                                    "MS家族制度情報", chg_format_buf, 0, 0);
                            /* 処理を終了する */
                            return C_const_NG;
                        }

                        /* UPDATE文を実行する */
//                        EXEC SQL EXECUTE sql_kdatalock
//                        USING
//                        /*                          :mskased_t.kazoku_oya_kokyaku_no,                        家族親顧客番号               */                                             /* 2022/10/14 MCCM初版 DEL */
//                        :mskased_t.kazoku_1_kokyaku_no,                       /* 家族１顧客番号               */
//                            :mskased_t.kazoku_2_kokyaku_no,                       /* 家族２顧客番号               */
//                            :mskased_t.kazoku_3_kokyaku_no,                       /* 家族３顧客番号               */
//                            :mskased_t.kazoku_4_kokyaku_no,                       /* 家族４顧客番号               */
//                            :mskased_t.kazoku_5_kokyaku_no,                       /* 家族５顧客番号               */                                             /* 2022/10/14 MCCM初版 ADD */
//                            :mskased_t.kazoku_6_kokyaku_no,                       /* 家族６顧客番号               */                                             /* 2022/10/14 MCCM初版 ADD */
//                        /*                          :mskased_t.kazoku_oya_toroku_ymd,                        家族親登録日                 */                                             /* 2022/10/14 MCCM初版 DEL */
//                            :mskased_t.kazoku_1_toroku_ymd,                       /* 家族１登録日                 */
//                            :mskased_t.kazoku_2_toroku_ymd,                       /* 家族２登録日                 */
//                            :mskased_t.kazoku_3_toroku_ymd,                       /* 家族３登録日                 */
//                            :mskased_t.kazoku_4_toroku_ymd,                       /* 家族４登録日                 */
//                            :mskased_t.kazoku_5_toroku_ymd,                       /* 家族５登録日                 */                                             /* 2022/10/14 MCCM初版 ADD */
//                            :mskased_t.kazoku_6_toroku_ymd,                       /* 家族６登録日                 */                                             /* 2022/10/14 MCCM初版 ADD */
//                            :mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd,   /* 家族ランクＵＰ金額最終更新日 */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,     /* 年間家族ランクＵＰ対象金額０ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,     /* 年間家族ランクＵＰ対象金額１ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,     /* 年間家族ランクＵＰ対象金額２ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,     /* 年間家族ランクＵＰ対象金額３ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,     /* 年間家族ランクＵＰ対象金額４ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,     /* 年間家族ランクＵＰ対象金額５ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,     /* 年間家族ランクＵＰ対象金額６ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,     /* 年間家族ランクＵＰ対象金額７ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,     /* 年間家族ランクＵＰ対象金額８ */
//                            :mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,     /* 年間家族ランクＵＰ対象金額９ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,   /* 月間家族ランクＵＰ金額００１ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,   /* 月間家族ランクＵＰ金額００２ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,   /* 月間家族ランクＵＰ金額００３ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,   /* 月間家族ランクＵＰ金額００４ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,   /* 月間家族ランクＵＰ金額００５ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,   /* 月間家族ランクＵＰ金額００６ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,   /* 月間家族ランクＵＰ金額００７ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,   /* 月間家族ランクＵＰ金額００８ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,   /* 月間家族ランクＵＰ金額００９ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,   /* 月間家族ランクＵＰ金額０１０ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,   /* 月間家族ランクＵＰ金額０１１ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,   /* 月間家族ランクＵＰ金額０１２ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,   /* 月間家族ランクＵＰ金額１０１ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,   /* 月間家族ランクＵＰ金額１０２ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,   /* 月間家族ランクＵＰ金額１０３ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,   /* 月間家族ランクＵＰ金額１０４ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,   /* 月間家族ランクＵＰ金額１０５ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,   /* 月間家族ランクＵＰ金額１０６ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,   /* 月間家族ランクＵＰ金額１０７ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,   /* 月間家族ランクＵＰ金額１０８ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,   /* 月間家族ランクＵＰ金額１０９ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,   /* 月間家族ランクＵＰ金額１１０ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,   /* 月間家族ランクＵＰ金額１１１ */
//                            :mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112,   /* 月間家族ランクＵＰ金額１１２ */
//                            :mskased_t.nenji_rank_cd_0,                           /* 年次ランクコード０           */
//                            :mskased_t.nenji_rank_cd_1,                           /* 年次ランクコード１           */
//                            :mskased_t.nenji_rank_cd_2,                           /* 年次ランクコード２           */
//                            :mskased_t.nenji_rank_cd_3,                           /* 年次ランクコード３           */
//                            :mskased_t.nenji_rank_cd_4,                           /* 年次ランクコード４           */
//                            :mskased_t.nenji_rank_cd_5,                           /* 年次ランクコード５           */
//                            :mskased_t.nenji_rank_cd_6,                           /* 年次ランクコード６           */
//                            :mskased_t.nenji_rank_cd_7,                           /* 年次ランクコード７           */
//                            :mskased_t.nenji_rank_cd_8,                           /* 年次ランクコード８           */
//                            :mskased_t.nenji_rank_cd_9,                           /* 年次ランクコード９           */
//                            :mskased_t.getuji_rank_cd_001,                        /* 月次ランクコード００１       */
//                            :mskased_t.getuji_rank_cd_002,                        /* 月次ランクコード００２       */
//                            :mskased_t.getuji_rank_cd_003,                        /* 月次ランクコード００３       */
//                            :mskased_t.getuji_rank_cd_004,                        /* 月次ランクコード００４       */
//                            :mskased_t.getuji_rank_cd_005,                        /* 月次ランクコード００５       */
//                            :mskased_t.getuji_rank_cd_006,                        /* 月次ランクコード００６       */
//                            :mskased_t.getuji_rank_cd_007,                        /* 月次ランクコード００７       */
//                            :mskased_t.getuji_rank_cd_008,                        /* 月次ランクコード００８       */
//                            :mskased_t.getuji_rank_cd_009,                        /* 月次ランクコード００９       */
//                            :mskased_t.getuji_rank_cd_010,                        /* 月次ランクコード０１０       */
//                            :mskased_t.getuji_rank_cd_011,                        /* 月次ランクコード０１１       */
//                            :mskased_t.getuji_rank_cd_012,                        /* 月次ランクコード０１２       */
//                            :mskased_t.getuji_rank_cd_101,                        /* 月次ランクコード１０１       */
//                            :mskased_t.getuji_rank_cd_102,                        /* 月次ランクコード１０２       */
//                            :mskased_t.getuji_rank_cd_103,                        /* 月次ランクコード１０３       */
//                            :mskased_t.getuji_rank_cd_104,                        /* 月次ランクコード１０４       */
//                            :mskased_t.getuji_rank_cd_105,                        /* 月次ランクコード１０５       */
//                            :mskased_t.getuji_rank_cd_106,                        /* 月次ランクコード１０６       */
//                            :mskased_t.getuji_rank_cd_107,                        /* 月次ランクコード１０７       */
//                            :mskased_t.getuji_rank_cd_108,                        /* 月次ランクコード１０８       */
//                            :mskased_t.getuji_rank_cd_109,                        /* 月次ランクコード１０９       */
//                            :mskased_t.getuji_rank_cd_110,                        /* 月次ランクコード１１０       */
//                            :mskased_t.getuji_rank_cd_111,                        /* 月次ランクコード１１１       */
//                            :mskased_t.getuji_rank_cd_112,                        /* 月次ランクコード１１２       */
//                            :this_date,                                           /* バッチ更新日                 */
//                            :this_date,                                           /* 最終更新日                   */
//                            :Program_Name,                                        /* 最終更新プログラムＩＤ       */
//                            :mskased_t.kazoku_1_toroku_time,                      /* 家族１登録時刻               */
//                            :mskased_t.kazoku_2_toroku_time,                      /* 家族２登録時刻               */
//                            :mskased_t.kazoku_3_toroku_time,                      /* 家族３登録時刻               */
//                            :mskased_t.kazoku_4_toroku_time,                      /* 家族４登録時刻               */
//                            :mskased_t.kazoku_5_toroku_time,                      /* 家族５登録時刻               */
//                            :mskased_t.kazoku_6_toroku_time,                      /* 家族６登録時刻               */
//                            :mskased_t.kazoku_id;                                 /* 家族ＩＤ                     */

                        sqlca.restAndExecute(							mskased_t.kazoku_1_kokyaku_no,                       /* 家族１顧客番号               */
                                mskased_t.kazoku_2_kokyaku_no,                       /* 家族２顧客番号               */
                                mskased_t.kazoku_3_kokyaku_no,                       /* 家族３顧客番号               */
                                mskased_t.kazoku_4_kokyaku_no,                       /* 家族４顧客番号               */
                                mskased_t.kazoku_5_kokyaku_no,                       /* 家族５顧客番号               */                                             /* 2022/10/14 MCCM初版 ADD */
                                mskased_t.kazoku_6_kokyaku_no,                       /* 家族６顧客番号               */                                             /* 2022/10/14 MCCM初版 ADD */
                                /*                          mskased_t.kazoku_oya_toroku_ymd,                        家族親登録日                 */                                             /* 2022/10/14 MCCM初版 DEL */
                                mskased_t.kazoku_1_toroku_ymd,                       /* 家族１登録日                 */
                                mskased_t.kazoku_2_toroku_ymd,                       /* 家族２登録日                 */
                                mskased_t.kazoku_3_toroku_ymd,                       /* 家族３登録日                 */
                                mskased_t.kazoku_4_toroku_ymd,                       /* 家族４登録日                 */
                                mskased_t.kazoku_5_toroku_ymd,                       /* 家族５登録日                 */                                             /* 2022/10/14 MCCM初版 ADD */
                                mskased_t.kazoku_6_toroku_ymd,                       /* 家族６登録日                 */                                             /* 2022/10/14 MCCM初版 ADD */
                                mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd,   /* 家族ランクＵＰ金額最終更新日 */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0,     /* 年間家族ランクＵＰ対象金額０ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1,     /* 年間家族ランクＵＰ対象金額１ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2,     /* 年間家族ランクＵＰ対象金額２ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3,     /* 年間家族ランクＵＰ対象金額３ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4,     /* 年間家族ランクＵＰ対象金額４ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5,     /* 年間家族ランクＵＰ対象金額５ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6,     /* 年間家族ランクＵＰ対象金額６ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7,     /* 年間家族ランクＵＰ対象金額７ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8,     /* 年間家族ランクＵＰ対象金額８ */
                                mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9,     /* 年間家族ランクＵＰ対象金額９ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001,   /* 月間家族ランクＵＰ金額００１ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002,   /* 月間家族ランクＵＰ金額００２ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003,   /* 月間家族ランクＵＰ金額００３ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004,   /* 月間家族ランクＵＰ金額００４ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005,   /* 月間家族ランクＵＰ金額００５ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006,   /* 月間家族ランクＵＰ金額００６ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007,   /* 月間家族ランクＵＰ金額００７ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008,   /* 月間家族ランクＵＰ金額００８ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009,   /* 月間家族ランクＵＰ金額００９ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010,   /* 月間家族ランクＵＰ金額０１０ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011,   /* 月間家族ランクＵＰ金額０１１ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012,   /* 月間家族ランクＵＰ金額０１２ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101,   /* 月間家族ランクＵＰ金額１０１ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102,   /* 月間家族ランクＵＰ金額１０２ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103,   /* 月間家族ランクＵＰ金額１０３ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104,   /* 月間家族ランクＵＰ金額１０４ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105,   /* 月間家族ランクＵＰ金額１０５ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106,   /* 月間家族ランクＵＰ金額１０６ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107,   /* 月間家族ランクＵＰ金額１０７ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108,   /* 月間家族ランクＵＰ金額１０８ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109,   /* 月間家族ランクＵＰ金額１０９ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110,   /* 月間家族ランクＵＰ金額１１０ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111,   /* 月間家族ランクＵＰ金額１１１ */
                                mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112,   /* 月間家族ランクＵＰ金額１１２ */
                                mskased_t.nenji_rank_cd_0,                           /* 年次ランクコード０           */
                                mskased_t.nenji_rank_cd_1,                           /* 年次ランクコード１           */
                                mskased_t.nenji_rank_cd_2,                           /* 年次ランクコード２           */
                                mskased_t.nenji_rank_cd_3,                           /* 年次ランクコード３           */
                                mskased_t.nenji_rank_cd_4,                           /* 年次ランクコード４           */
                                mskased_t.nenji_rank_cd_5,                           /* 年次ランクコード５           */
                                mskased_t.nenji_rank_cd_6,                           /* 年次ランクコード６           */
                                mskased_t.nenji_rank_cd_7,                           /* 年次ランクコード７           */
                                mskased_t.nenji_rank_cd_8,                           /* 年次ランクコード８           */
                                mskased_t.nenji_rank_cd_9,                           /* 年次ランクコード９           */
                                mskased_t.getuji_rank_cd_001,                        /* 月次ランクコード００１       */
                                mskased_t.getuji_rank_cd_002,                        /* 月次ランクコード００２       */
                                mskased_t.getuji_rank_cd_003,                        /* 月次ランクコード００３       */
                                mskased_t.getuji_rank_cd_004,                        /* 月次ランクコード００４       */
                                mskased_t.getuji_rank_cd_005,                        /* 月次ランクコード００５       */
                                mskased_t.getuji_rank_cd_006,                        /* 月次ランクコード００６       */
                                mskased_t.getuji_rank_cd_007,                        /* 月次ランクコード００７       */
                                mskased_t.getuji_rank_cd_008,                        /* 月次ランクコード００８       */
                                mskased_t.getuji_rank_cd_009,                        /* 月次ランクコード００９       */
                                mskased_t.getuji_rank_cd_010,                        /* 月次ランクコード０１０       */
                                mskased_t.getuji_rank_cd_011,                        /* 月次ランクコード０１１       */
                                mskased_t.getuji_rank_cd_012,                        /* 月次ランクコード０１２       */
                                mskased_t.getuji_rank_cd_101,                        /* 月次ランクコード１０１       */
                                mskased_t.getuji_rank_cd_102,                        /* 月次ランクコード１０２       */
                                mskased_t.getuji_rank_cd_103,                        /* 月次ランクコード１０３       */
                                mskased_t.getuji_rank_cd_104,                        /* 月次ランクコード１０４       */
                                mskased_t.getuji_rank_cd_105,                        /* 月次ランクコード１０５       */
                                mskased_t.getuji_rank_cd_106,                        /* 月次ランクコード１０６       */
                                mskased_t.getuji_rank_cd_107,                        /* 月次ランクコード１０７       */
                                mskased_t.getuji_rank_cd_108,                        /* 月次ランクコード１０８       */
                                mskased_t.getuji_rank_cd_109,                        /* 月次ランクコード１０９       */
                                mskased_t.getuji_rank_cd_110,                        /* 月次ランクコード１１０       */
                                mskased_t.getuji_rank_cd_111,                        /* 月次ランクコード１１１       */
                                mskased_t.getuji_rank_cd_112,                        /* 月次ランクコード１１２       */
                                this_date,                                           /* バッチ更新日                 */
                                this_date,                                           /* 最終更新日                   */
                                Program_Name,                                        /* 最終更新プログラムＩＤ       */
                                mskased_t.kazoku_1_toroku_time,                      /* 家族１登録時刻               */
                                mskased_t.kazoku_2_toroku_time,                      /* 家族２登録時刻               */
                                mskased_t.kazoku_3_toroku_time,                      /* 家族３登録時刻               */
                                mskased_t.kazoku_4_toroku_time,                      /* 家族４登録時刻               */
                                mskased_t.kazoku_5_toroku_time,                      /* 家族５登録時刻               */
                                mskased_t.kazoku_6_toroku_time,                      /* 家族６登録時刻               */
                                mskased_t.kazoku_id                                 /* 家族ＩＤ                     */);

                        if (sqlca.sqlcode != C_const_Ora_OK) {
                            /* error */
                            if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_main *** MS家族制度情報 EXECUTE sqlcode=[%d]\n",sqlca.sqlcode);
                                /*------------------------------------------------------------*/
                            }
                            sprintf(chg_format_buf, "家族ＩＤ=%s", mskased_t.kazoku_id.arr);                                                                 /* 2022/10/14 MCCM初版 MOD */
                            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "MS家族制度情報", chg_format_buf, 0, 0);
                            /* 処理を終了する */
                            return C_const_NG;
                        }

                    }

                    else {
                        /* 家族処理情報が維持されない場合 */
                        /*  2014/11/26 顧客番号削除の場合、NULL設定 */
                        if (DBG_LOG){
                            /*------------------------------------------------------------*/
                            C_DbgMsg("*** cmBTleavB_main *** ＭＳ家族情報 (%s)\n", "家族処理情報が維持されない場合");
                            /*------------------------------------------------------------*/
                        }
//                        EXEC SQL UPDATE MS家族制度情報@CMSD
//                        SET  家族親顧客番号 = NULL,
//                                家族１顧客番号 = NULL,
//                                家族２顧客番号 = NULL,
//                                家族３顧客番号 = NULL,
//                                家族４顧客番号 = NULL,
//                                家族５顧客番号 = NULL,                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                                家族６顧客番号 = NULL,                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                                家族親登録日   = 0,
//                                家族１登録日   = 0,
//                                家族２登録日   = 0,
//                                家族３登録日   = 0,
//                                家族４登録日   = 0,
//                                家族５登録日   = 0,                                                                                                             /* 2022/10/14 MCCM初版 ADD */
//                                家族６登録日   = 0,                                                                                                             /* 2022/10/14 MCCM初版 ADD */
//                                家族１登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                                家族２登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                                家族３登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                                家族４登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                                家族５登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                                家族６登録時刻 = 0,                                                                                                             /* 2022/12/19 MCCM初版 ADD */
//                        家族削除日     = :this_date,
//                                バッチ更新日   = :this_date,
//                                最終更新日     = :this_date,
//                                最終更新日時   = SYSDATE,
//                                最終更新プログラムＩＤ = :Program_Name
//                        WHERE 家族ＩＤ = :mskased_t.kazoku_id;


                        StringDto sql = new StringDto();
                        sql.arr = "UPDATE MS家族制度情報" +
                                " SET  家族親顧客番号 = NULL," +
                                "        家族１顧客番号 = NULL," +
                                "        家族２顧客番号 = NULL," +
                                "        家族３顧客番号 = NULL," +
                                "        家族４顧客番号 = NULL," +
                                "        家族５顧客番号 = NULL," +
                                "        家族６顧客番号 = NULL," +
                                "        家族親登録日   = 0," +
                                "        家族１登録日   = 0," +
                                "        家族２登録日   = 0," +
                                "        家族３登録日   = 0," +
                                "        家族４登録日   = 0," +
                                "        家族５登録日   = 0," +
                                "        家族６登録日   = 0," +
                                "        家族１登録時刻 = 0," +
                                "        家族２登録時刻 = 0," +
                                "        家族３登録時刻 = 0," +
                                "        家族４登録時刻 = 0," +
                                "        家族５登録時刻 = 0," +
                                "        家族６登録時刻 = 0," +
                                " 家族削除日     = ?," +
                                " バッチ更新日   = ?," +
                                " 最終更新日     = ?," +
                                " 最終更新日時   = SYSDATE()," +
                                " 最終更新プログラムＩＤ = ?" +
                                " WHERE 家族ＩＤ = ?";
                        sqlca.sql = sql;
                        sqlca.prepare();
                        sqlca.restAndExecute(this_date, this_date, this_date, Program_Name, mskased_t.kazoku_id);

                        if (sqlca.sqlcode != C_const_Ora_OK) {
                            /* error */

                            if (DBG_LOG){
                                                        /*------------------------------------------------------------*/
                                                        C_DbgMsg("*** cmBTleavB_main *** MS家族制度情報 UPDATE NG sqlcode=[%d]\n",
                                                                sqlca.sqlcode);
                                                        /*------------------------------------------------------------*/
                            }

                            sprintf(chg_format_buf, "家族ＩＤ=%s", mskased_t.kazoku_id.arr);                                                                 /* 2022/10/14 MCCM初版 MOD */
                            APLOG_WT( "904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "MS家族制度情報", chg_format_buf, 0, 0);
                            /* 処理を終了する */
                            return C_const_NG;

                        }

                    }
                }
                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_main *** 家族制度処理 %s\n", "normal end");
                                /*------------------------------------------------------------*/
                }


                /*-----------------------------------------------*/
                /* （６）１１．MM顧客情報を更新 */
                /*-----------------------------------------------*/
                if(file_flg == 0 && togo_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                    /* MM顧客情報の更新 */
//                    EXEC SQL UPDATE MM顧客情報
//                    /*           SET 顧客名称       = NULL,                               */                                                                                         /* 2022/10/14 MCCM初版 DEL */
//                    /*               顧客カナ名称   = NULL,                               */                                                                                         /* 2022/10/14 MCCM初版 DEL */
//                    SET 顧客名字       = NULL,                                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                            顧客名前       = NULL,                                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                            カナ顧客名字   = NULL,                                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                            カナ顧客名前   = NULL,                                                                                                                          /* 2022/10/14 MCCM初版 ADD */
//                            年齢           = 0,
//                            誕生年         = 0,
//                            誕生月         = 0,
//                            誕生日         = 0,
//                            性別           = 0,
//                    バッチ更新日   = :this_date,
//                            最終更新日     = :this_date,
//                            最終更新日時   = SYSDATE,
//                            最終更新プログラムＩＤ = :Program_Name
//                    WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;

                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MM顧客情報         " +
                            " SET 顧客名字       = NULL,           " +
                            " 顧客名前       = NULL,               " +
                            " カナ顧客名字   = NULL,               " +
                            " カナ顧客名前   = NULL,               " +
                            " 年齢           = 0," +
                            " 誕生年         = 0," +
                            " 誕生月         = 0," +
                            " 誕生日         = 0," +
                            " 性別           = 0," +
                            " バッチ更新日   = ?," +
                            " 最終更新日     = ?," +
                            " 最終更新日時   = SYSDATE()," +
                            " 最終更新プログラムＩＤ = ?" +
                            " WHERE 顧客番号 = ?";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.restAndExecute(this_date,
                            this_date,
                            Program_Name,
                            mmkokgb_t.kokyaku_no);

                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** MM顧客情報 UPDATE 2 sqlcode=[%d]\n",
                                                sqlca.sqlcode);
                                        /*------------------------------------------------------------*/
                    }

                    /* エラーの場合処理終了 */
                    if ( sqlca.sqlcode != C_const_Ora_OK ) {
                        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                        sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                "MM顧客情報", chg_format_buf, 0, 0);
                        return C_const_NG;
                    }
                }else if(file_flg == 1){                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
                    /* MM顧客情報の更新 */                                                                                                                             /* 2022/10/14 MCCM初版 ADD */
//                    EXEC SQL UPDATE MM顧客情報                                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    SET バッチ更新日   = :this_date,                                                                                                                    /* 2022/10/14 MCCM初版 ADD */
//                            最終更新日     = :this_date,                                                                                                                    /* 2022/10/14 MCCM初版 ADD */
//                            最終更新日時   = SYSDATE,                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
//                            最終更新プログラムＩＤ = :Program_Name,                                                                                                         /* 2022/10/14 MCCM初版 ADD */
//                            顧客ステータス = 9                                                                                                                              /* 2022/10/14 MCCM初版 ADD */
//                    WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;                                                                                                               /* 2022/10/14 MCCM初版 ADD */

                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MM顧客情報                   " +
                            " SET バッチ更新日   = ?,               " +
                            "         最終更新日     = ?,           " +
                            "         最終更新日時   = SYSDATE(),       " +
                            "         最終更新プログラムＩＤ = ?," +
                            "         顧客ステータス = 9        " +
                            " WHERE 顧客番号 = ?";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.restAndExecute(this_date,
                            this_date,
                            Program_Name,
                            mmkokgb_t.kokyaku_no);

                    if (DBG_LOG){                                                                                                                                                      /* 2022/10/14 MCCM初版 ADD */
                                        /*------------------------------------------------------------*/                                                                                     /* 2022/10/14 MCCM初版 ADD */
                                        C_DbgMsg("*** cmBTleavB_main *** MM顧客情報 UPDATE 2 sqlcode=[%d]\n",                                                                                /* 2022/10/14 MCCM初版 ADD */
                                                sqlca.sqlcode);                                                                                                     /* 2022/10/14 MCCM初版 ADD */
                                        /*------------------------------------------------------------*/                                                                                     /* 2022/10/14 MCCM初版 ADD */
                    }                                                                                                                                                           /* 2022/10/14 MCCM初版 ADD */

                    /* エラーの場合処理終了 */                                                                                                                               /* 2022/10/14 MCCM初版 ADD */
                    if ( sqlca.sqlcode != C_const_Ora_OK ) {                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
                        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );                                                                                              /* 2022/10/14 MCCM初版 ADD */
                        sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );                                                                                  /* 2022/10/14 MCCM初版 ADD */
                        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,                                                                                      /* 2022/10/14 MCCM初版 ADD */
                                "MM顧客情報", chg_format_buf, 0, 0);                                                                                        /* 2022/10/14 MCCM初版 ADD */
                        return C_const_NG;                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
                    }                                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */
                }                                                                                                                                                          /* 2022/10/14 MCCM初版 ADD */

                /*-----------------------------------------------*/
                /* （６）１２．MS顧客制度情報を更新 */
                /*-----------------------------------------------*/
                if(file_flg == 0 && togo_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                    /* MS顧客制度情報の更新 */
//                    EXEC SQL UPDATE MS顧客制度情報@CMSD
//                    SET 誕生月              = 0,
//                            エントリー          = 0,
//                            シニア              = 0,
//                            サークルＩＤ１      = 0,
//                            サークルＩＤ２      = 0,
//                            サークルＩＤ３      = 0,
//                            サークルＩＤ４      = 0,
//                            サークルＩＤ５      = 0,
//                            静態取込済みフラグ  = 0,
//                            家族ＩＤ            = 0,
//                            ＥＣ会員フラグ      = 0,
//                            アプリ会員フラグ    = 0,
//                    バッチ更新日        = :this_date,
//                            最終更新日          = :this_date,
//                            最終更新日時        = SYSDATE,
//                            最終更新プログラムＩＤ = :Program_Name,
//                            グローバル会員フラグ   = CASE WHEN グローバル会員フラグ = 1 THEN 2 END,                                                                         /* 2022/10/14 MCCM初版 ADD */
//                            コーポレート会員フラグ = 0,                                                                                                                     /* 2022/10/14 MCCM初版 ADD */
//                            サンプリング要否フラグ = 0,                                                                                                                     /* 2023/05/08 MCCMPH2  ADD */
//                            デジタル会員ＥＣ入会フラグ = 0,                                                                                                                 /* 2024/03/19 MCCMPH2  ADD */
//                            デジタル会員アプリ入会フラグ = 0                                                                                                                /* 2024/03/19 MCCMPH2  ADD */
//                    WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;

                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MS顧客制度情報" +
                            " SET 誕生月              = 0," +
                            "        エントリー          = 0," +
                            "        シニア              = 0," +
                            "        サークルＩＤ１      = 0," +
                            "        サークルＩＤ２      = 0," +
                            "        サークルＩＤ３      = 0," +
                            "        サークルＩＤ４      = 0," +
                            "        サークルＩＤ５      = 0," +
                            "        静態取込済みフラグ  = 0," +
                            "        家族ＩＤ            = 0," +
                            "        ＥＣ会員フラグ      = 0," +
                            "        アプリ会員フラグ    = 0," +
                            " バッチ更新日        = ?," +
                            "        最終更新日          = ?," +
                            "        最終更新日時        = SYSDATE()," +
                            "        最終更新プログラムＩＤ = ?," +
                            "        グローバル会員フラグ   = CASE WHEN グローバル会員フラグ = '1' THEN 2 END, " +
                            "        コーポレート会員フラグ = 0,                                            " +
                            "        サンプリング要否フラグ = 0,                                            " +
                            "        デジタル会員ＥＣ入会フラグ = 0,                                         " +
                            "        デジタル会員アプリ入会フラグ = 0                                        " +
                            " WHERE 顧客番号 = ?";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.restAndExecute(this_date,
                            this_date,
                            Program_Name,
                            mmkokgb_t.kokyaku_no);

                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** MS顧客制度情報 UPDATE sqlcode=[%d]\n",
                                                sqlca.sqlcode);
                                        /*------------------------------------------------------------*/
                    }

                    /* エラーの場合処理終了 */
                    if ( sqlca.sqlcode != C_const_Ora_OK ) {
                        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                        sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                "MS顧客制度情報", chg_format_buf, 0, 0);
                        return C_const_NG;
                    }

                    /*------------------------------------------------------*/
                    /* 家族ＩＤが設定済みかつ家族処理情報が維持されない場合 */
                    /*------------------------------------------------------*/
                    if ( 0 < (atol(mskosed_t.kazoku_id.arr)) && ko_no_cnt < 3) {                                                                                     /* 2022/10/14 MCCM初版 MOD */
                        memset(kaijo_kokyaku_no, 0x00, sizeof(kaijo_kokyaku_no));
                        /* 退会顧客でない方の顧客番号の家族ＩＤを更新 */
                        if (memcmp(mmkokgb_t.kokyaku_no.strDto(), mskased_t.kazoku_1_kokyaku_no.strVal(), 15) == 0) {                                                                    /* 2022/10/14 MCCM初版 DEL */
                            memcpy(kaijo_kokyaku_no, mskased_t.kazoku_2_kokyaku_no, sizeof(mskased_t.kazoku_2_kokyaku_no));                                                    /* 2022/10/14 MCCM初版 DEL */
                        }else{                                                                                                                                                 /* 2022/10/14 MCCM初版 DEL */
                            memcpy(kaijo_kokyaku_no, mskased_t.kazoku_1_kokyaku_no, sizeof(mskased_t.kazoku_1_kokyaku_no));                                                    /* 2022/10/14 MCCM初版 DEL */
                        }                                                                                                                                                      /* 2022/10/14 MCCM初版 DEL */

                        /* MS顧客制度情報の更新 */
//                        EXEC SQL UPDATE MS顧客制度情報@CMSD
//                        SET 家族ＩＤ         = 0,
//                        バッチ更新日     = :this_date,
//                                最終更新日       = :this_date,
//                                最終更新日時     = SYSDATE,
//                                最終更新プログラムＩＤ = :Program_Name
//                        WHERE 顧客番号 = :kaijo_kokyaku_no;

                        sql = new StringDto();
                        sql.arr = "UPDATE MS顧客制度情報                  " +
                                " SET 家族ＩＤ         = 0, バッチ更新日   = ?, " +
                                "         最終更新日     = ?,           " +
                                "         最終更新日時   = SYSDATE(),       " +
                                "         最終更新プログラムＩＤ = ?" +
                                " WHERE 顧客番号 = ?";
                        sqlca.sql = sql;
                        sqlca.prepare();
                        sqlca.restAndExecute(this_date,
                                this_date,
                                Program_Name,
                                kaijo_kokyaku_no);

                        if (DBG_LOG){
                                                /*------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** MS顧客制度情報 UPDATE sqlcode=[%d]\n",
                                                        sqlca.sqlcode);
                                                /*------------------------------------------------------------*/
                        }

                        /* エラーの場合処理終了 */
                        if ( sqlca.sqlcode != C_const_Ora_OK ) {
                            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                            sprintf( chg_format_buf, "顧客番号=%s", kaijo_kokyaku_no );
                            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "MS顧客制度情報", chg_format_buf, 0, 0);
                            return C_const_NG;
                        }
                    }
                }else if(file_flg == 1){                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
                    /* MS顧客制度情報の更新 */                                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    EXEC SQL UPDATE MS顧客制度情報@CMSD                                                                                                                      /* 2022/10/14 MCCM初版 ADD */
//                    SET バッチ更新日        = :this_date,                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                            最終更新日          = :this_date,                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                            最終更新日時        = SYSDATE,                                                                                                                  /* 2022/10/14 MCCM初版 ADD */
//                            最終更新プログラムＩＤ = :Program_Name,                                                                                                         /* 2022/10/14 MCCM初版 ADD */
//                            顧客ステータス         = 9,                                                                                                                     /* 2022/10/14 MCCM初版 ADD */
//                            グローバル会員フラグ   = CASE WHEN グローバル会員フラグ = 1 THEN 2 END,                                                                         /* 2024/04/03 MCCMPH2  ADD */
//                            コーポレート会員フラグ = 0,                                                                                                                     /* 2024/04/03 MCCMPH2  ADD */
//                            サンプリング要否フラグ = 0,                                                                                                                     /* 2024/04/03 MCCMPH2  ADD */
//                            デジタル会員ＥＣ入会フラグ = 0,                                                                                                                 /* 2024/04/03 MCCMPH2  ADD */
//                            デジタル会員アプリ入会フラグ = 0                                                                                                                /* 2024/04/03 MCCMPH2  ADD */
//                    WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;                                                                                                               /* 2022/10/14 MCCM初版 ADD */

                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MS顧客制度情報                                          " +
                            "SET バッチ更新日        = ?,                                          " +
                            "        最終更新日          = ?,                                      " +
                            "        最終更新日時        = SYSDATE(),                                         " +
                            "        最終更新プログラムＩＤ = ?,                                " +
                            "        顧客ステータス         = 9,                                            " +
                            "        グローバル会員フラグ   = CASE WHEN グローバル会員フラグ = '1' THEN 2 END," +
                            "        コーポレート会員フラグ = 0,                                            " +
                            "        サンプリング要否フラグ = 0,                                            " +
                            "        デジタル会員ＥＣ入会フラグ = 0,                                        " +
                            "        デジタル会員アプリ入会フラグ = 0                                       " +
                            "WHERE 顧客番号 = ?";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.restAndExecute(this_date,
                            this_date,
                            Program_Name,
                            mmkokgb_t.kokyaku_no);

                    if (DBG_LOG){                                                                                                                                                      /* 2022/10/14 MCCM初版 ADD */
                                        /*------------------------------------------------------------*/                                                                                     /* 2022/10/14 MCCM初版 ADD */
                                        C_DbgMsg("*** cmBTleavB_main *** MS顧客制度情報 UPDATE sqlcode=[%d]\n",                                                                              /* 2022/10/14 MCCM初版 ADD */
                                                sqlca.sqlcode);                                                                                                     /* 2022/10/14 MCCM初版 ADD */
                                        /*------------------------------------------------------------*/                                                                                     /* 2022/10/14 MCCM初版 ADD */
                    }                                                                                                                                                           /* 2022/10/14 MCCM初版 ADD */

                    /* エラーの場合処理終了 */                                                                                                                               /* 2022/10/14 MCCM初版 ADD */
                    if ( sqlca.sqlcode != C_const_Ora_OK ) {                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
                        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );                                                                                              /* 2022/10/14 MCCM初版 ADD */
                        sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );                                                                                  /* 2022/10/14 MCCM初版 ADD */
                        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,                                                                                      /* 2022/10/14 MCCM初版 ADD */
                                "MS顧客制度情報", chg_format_buf, 0, 0);                                                                                    /* 2022/10/14 MCCM初版 ADD */
                        return C_const_NG;                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
                    }                                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */
                }                                                                                                                                                           /* 2022/10/14 MCCM初版 ADD */

                /*-----------------------------------------------*/
                /* （６）１３．MM顧客属性情報を更新 */
                /*-----------------------------------------------*/
                if(file_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                    if(togo_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                        /* MM顧客属性情報の更新 */
//                        EXEC SQL UPDATE MM顧客属性情報
//                        SET 郵便番号               = NULL,
//                                郵便番号コード         = '00000000000000000000000',
//                                /*               住所１                 = NULL,                               */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                                /*               住所２                 = NULL,                               */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                                /*               住所３                 = NULL,                               */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                                住所                   = NULL,                                                                                                                  /* 2022/10/14 MCCM初版 ADD */
//                                電話番号１             = NULL,
//                                電話番号２             = NULL,
//                                検索電話番号１         = NULL,
//                                検索電話番号２         = NULL,
//                                Ｅメールアドレス１     = NULL,
//                                Ｅメールアドレス２     = NULL,
//                                電話番号３             = NULL,
//                                電話番号４             = NULL,
//                                検索電話番号３         = NULL,
//                                検索電話番号４         = NULL,
//                                職業                   = NULL,
//                                勤務区分               = 0,
//                                自宅住所コード         = NULL,
//                        バッチ更新日           = :this_date,
//                                最終更新日             = :this_date,
//                                最終更新日時           = SYSDATE,
//                                最終更新プログラムＩＤ = :Program_Name,
//                                Ｅメールアドレス３     = NULL,
//                                Ｅメールアドレス４     = NULL,
//                                都道府県コード         = 0,                                                                                                                     /* 2022/10/14 MCCM初版 ADD */
//                                Ｘ座標コード           = 0,                                                                                                                     /* 2022/10/14 MCCM初版 ADD */
//                                Ｙ座標コード           = 0,                                                                                                                     /* 2022/10/14 MCCM初版 ADD */
//                                会社名                 = NULL,                                                                                                                  /* 2022/10/14 MCCM初版 ADD */
//                                部署名                 = NULL,                                                                                                                  /* 2022/10/14 MCCM初版 ADD */
//                                関心分野コード         = NULL                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
//                        WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;

                        StringDto sql = new StringDto();
                        sql.arr = "UPDATE MM顧客属性情報" +
                                " SET 郵便番号               = NULL," +
                                " 郵便番号コード         = '00000000000000000000000'," +
                                " 住所                   = NULL," +
                                " 電話番号１             = NULL," +
                                " 電話番号２             = NULL," +
                                " 検索電話番号１         = NULL," +
                                " 検索電話番号２         = NULL," +
                                " Ｅメールアドレス１     = NULL," +
                                " Ｅメールアドレス２     = NULL," +
                                " 電話番号３             = NULL," +
                                " 電話番号４             = NULL," +
                                " 検索電話番号３         = NULL," +
                                " 検索電話番号４         = NULL," +
                                " 職業                   = NULL," +
                                " 勤務区分               = 0," +
                                " 自宅住所コード         = NULL," +
                                " バッチ更新日           = ?," +
                                " 最終更新日             = ?," +
                                " 最終更新日時           = SYSDATE()," +
                                " 最終更新プログラムＩＤ = ?,\n" +
                                " Ｅメールアドレス３     = NULL," +
                                " Ｅメールアドレス４     = NULL," +
                                " 都道府県コード         = 0,                                                      " +
                                " Ｘ座標コード           = 0,                                                      " +
                                " Ｙ座標コード           = 0,                                                      " +
                                " 会社名                 = NULL,                                                   " +
                                " 部署名                 = NULL,                                                   " +
                                " 関心分野コード         = NULL                                                    " +
                                " WHERE 顧客番号 = ?";
                        sqlca.sql = sql;
                        sqlca.prepare();
                        sqlca.restAndExecute(this_date,
                                this_date,
                                Program_Name,
                                mmkokgb_t.kokyaku_no);

                        if (DBG_LOG){
                                                /*------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** MM顧客属性情報 UPDATE sqlcode=[%d]\n",
                                                        sqlca.sqlcode);
                                                /*------------------------------------------------------------*/
                        }

                        /* エラーの場合処理終了 */
                        if ( sqlca.sqlcode != C_const_Ora_OK ) {
                            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                            sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "MM顧客属性情報報", chg_format_buf, 0, 0);
                            return C_const_NG;
                        }

                        /*-----------------------------------------------*/
                        /* （６）１４．MM顧客企業別属性情報を更新 */
                        /*-----------------------------------------------*/
                        /* MM顧客企業別属性情報の更新 */
//                        EXEC SQL UPDATE MM顧客企業別属性情報
//                        SET 退会年月日             = :this_date,
//                                ＴＥＬ止め区分         = 0,
//                                ＤＭ止め区分           = 3092,
//                                Ｅメール止め区分       = 5092,
//                                携帯ＴＥＬ止め区分     = 0,
//                                携帯Ｅメール止め区分   = 0,
//                                バッチ更新日           = :this_date,
//                                最終更新日             = :this_date,
//                                最終更新日時           = SYSDATE,
//                                最終更新プログラムＩＤ = :Program_Name
//                        WHERE 顧客番号 = :mmkokgb_t.kokyaku_no
//                        AND   退会年月日 = 0;

                        sql = new StringDto();
                        sql.arr = "UPDATE MM顧客企業別属性情報" +
                                " SET 退会年月日             = ?," +
                                "        ＴＥＬ止め区分         = 0," +
                                "        ＤＭ止め区分           = 3092," +
                                "        Ｅメール止め区分       = 5092," +
                                "        携帯ＴＥＬ止め区分     = 0," +
                                "        携帯Ｅメール止め区分   = 0," +
                                "        バッチ更新日           = ?," +
                                "        最終更新日             = ?," +
                                "        最終更新日時           = SYSDATE()," +
                                "        最終更新プログラムＩＤ = ?" +
                                " WHERE 顧客番号 = ?" +
                                " AND   退会年月日 = 0";
                        sqlca.sql = sql;
                        sqlca.prepare();
                        sqlca.restAndExecute(this_date,this_date,
                                this_date,
                                Program_Name,
                                mmkokgb_t.kokyaku_no);

                        if (DBG_LOG){
                                                /*------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** MM顧客企業別属性情報 UPDATE sqlcode=[%d]\n",
                                                        sqlca.sqlcode);
                                                /*------------------------------------------------------------*/
                        }

                        /* エラーの場合(NOTFOUND以外)処理終了 */
                        if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {  /* 2018.01.11 自動退会対応 NOTFOUND以外 追加 */
                            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                            sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "MM顧客企業別属性情報", chg_format_buf, 0, 0);
                            return C_const_NG;
                        }

                        /* 2022/10/14 MCCM初版 DEL START */
                        /*-----------------------------------------------*/
                        /* （６）１５．MMマタニティベビー情報を更新 */
                        /*-----------------------------------------------*/
                        /* MMマタニティベビー情報の更新 */
                        /*      EXEC SQL UPDATE MMマタニティベビー情報           */
                        /*           SET 退会年月日           = :this_date,      */
                        /*               会員種別             = 0,               */
                        /*               第１子出産予定日     = 0,               */
                        /*               第１子名称           = NULL,            */
                        /*               第１子カナ名称       = NULL,            */
                        /*               第１子性別           = 0,               */
                        /*               第１子年齢           = 0,               */
                        /*               第１子生年月日       = 0,               */
                        /*               第２子出産予定日     = 0,               */
                        /*               第２子名称           = NULL,            */
                        /*               第２子カナ名称       = NULL,            */
                        /*               第２子性別           = 0,               */
                        /*               第２子年齢           = 0,               */
                        /*               第２子生年月日       = 0,               */
                        /*               第３子出産予定日     = 0,               */
                        /*               第３子名称           = NULL,            */
                        /*               第３子カナ名称       = NULL,            */
                        /*               第３子性別           = 0,               */
                        /*               第３子年齢           = 0,               */
                        /*               第３子生年月日       = 0,               */
                        /*               ＴＥＬ止め区分       = 0,               */
                        /*               ＤＭ止め区分         = 3092,            */
                        /*               Ｅメール止め区分     = 5092,            */
                        /*               携帯ＴＥＬ止め区分   = 0,               */
                        /*               携帯Ｅメール止め区分 = 0,               */
                        /*               有効期限             = 0,               */
                        /*               削除フラグ           = 2,               */
                        /*               バッチ更新日         = :this_date,      */
                        /*               最終更新日           = :this_date,      */
                        /*               最終更新日時         = SYSDATE,         */
                        /*               最終更新プログラムＩＤ = :Program_Name  */
                        /*         WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;       */

//if (DBG_LOG){
//          /*------------------------------------------------------------*/
//          C_DbgMsg("*** cmBTleavB_main *** MMマタニティベビー情報 UPDATE sqlcode=[%d]\n",
//                                           sqlca.sqlcode);
//          /*------------------------------------------------------------*/
//}

                        /* エラーの場合(NOTFOUND以外)処理終了 */
//      if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
//          memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
//          sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
//          APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
//                                   "MMマタニティベビー情報", chg_format_buf, 0, 0);
//          return C_const_NG;
//        }
                        /* 2022/10/14 MCCM初版 DEL END */
                    }

                    /* 2023/05/08 MCCMPH2 ADD START */
                    /*-----------------------------------------------*/
                    /* （６）１６．A MMお届け先情報を更新 */
                    /*-----------------------------------------------*/
                    /* MMお届け先情報の更新 */
//                    EXEC SQL UPDATE MMお届け先情報
//                    SET お届け先表示名     = ' ',
//                            お届け先氏名漢字姓 = ' ',
//                            お届け先氏名漢字名 = ' ',
//                            お届け先氏名カナ姓 = ' ',
//                            お届け先氏名カナ名 = ' ',
//                            お届け先郵便番号１ = ' ',
//                            お届け先郵便番号２ = ' ',
//                            お届け先都道府県   = ' ',
//                            お届け先住所       = ' ',
//                            お届け先電話番号   = ' ',
//                            お届け先会社名     = ' ',
//                            お届け先部署名     = ' ',
//                            削除フラグ     = 1,
//                    バッチ更新日   = :this_date,
//                            最終更新日     = :this_date,
//                            最終更新日時   = SYSDATE,
//                            最終更新プログラムＩＤ = :Program_Name
//                    WHERE 顧客番号       = :mmkokgb_t.kokyaku_no;

                    StringDto sql = new StringDto();
                    sql.arr = "UPDATE MMお届け先情報" +
                            " SET お届け先表示名     = ' '," +
                            "        お届け先氏名漢字姓 = ' '," +
                            "        お届け先氏名漢字名 = ' '," +
                            "        お届け先氏名カナ姓 = ' '," +
                            "        お届け先氏名カナ名 = ' '," +
                            "        お届け先郵便番号１ = ' '," +
                            "        お届け先郵便番号２ = ' '," +
                            "        お届け先都道府県   = ' '," +
                            "        お届け先住所       = ' '," +
                            "        お届け先電話番号   = ' '," +
                            "        お届け先会社名     = ' '," +
                            "        お届け先部署名     = ' '," +
                            "        削除フラグ     = 1," +
                            " バッチ更新日   = ?," +
                            "        最終更新日     = ?," +
                            "        最終更新日時   = SYSDATE()," +
                            "        最終更新プログラムＩＤ = ?" +
                            " WHERE 顧客番号       = ?";
                    sqlca.sql = sql;
                    sqlca.prepare();
                    sqlca.restAndExecute(this_date,
                            this_date,
                            Program_Name,
                            mmkokgb_t.kokyaku_no);

                    if (DBG_LOG){
                                        /*------------------------------------------------------------*/
                                        C_DbgMsg("*** cmBTleavB_main *** MMお届け先情報 UPDATE sqlcode=[%d]\n",
                                                sqlca.sqlcode);
                                        /*------------------------------------------------------------*/
                    }

                    /* エラーの場合(NOTFOUND以外)処理終了 */
                    if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                        sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                        APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                "MMお届け先情報", chg_format_buf, 0, 0);
                        return C_const_NG;
                    }
                    /* 2023/05/08 MCCMPH2 ADD END */


                    if(togo_flg == 0){                                                                                                                                       /* 2022/10/14 MCCM初版 ADD */
                        /*-----------------------------------------------*/
                        /* （６）１６．TMメモを更新 */
                        /*-----------------------------------------------*/
                        /* TMメモの更新 */
//                        EXEC SQL UPDATE TMメモ
//                        SET 削除フラグ     = 1,
//                        バッチ更新日   = :this_date,
//                                最終更新日     = :this_date,
//                                最終更新日時   = SYSDATE,
//                                最終更新プログラムＩＤ = :Program_Name
//                        WHERE 顧客番号       = :mmkokgb_t.kokyaku_no;

                        sql = new StringDto();
                        sql.arr = "UPDATE TMメモ" +
                                " SET 削除フラグ     = 1," +
                                " バッチ更新日   = ?," +
                                "         最終更新日     = ?," +
                                "         最終更新日時   = SYSDATE()," +
                                "         最終更新プログラムＩＤ = ?" +
                                " WHERE 顧客番号       = ?";
                        sqlca.sql = sql;
                        sqlca.prepare();
                        sqlca.restAndExecute(this_date,
                                this_date,
                                Program_Name,
                                mmkokgb_t.kokyaku_no);

                        if (DBG_LOG){
                                                /*------------------------------------------------------------*/
                                                C_DbgMsg("*** cmBTleavB_main *** TMメモ UPDATE sqlcode=[%d]\n",
                                                        sqlca.sqlcode);
                                                /*------------------------------------------------------------*/
                        }

                        /* エラーの場合(NOTFOUND以外)処理終了 */
                        if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                            sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
                            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                                    "TMメモ", chg_format_buf, 0, 0);
                            return C_const_NG;
                        }



                        /* 2022/10/14 MCCM初版 DEL START */
                        /*-----------------------------------------------*/
                        /* （６）１７．MSサークル顧客情報更新 */
                        /*-----------------------------------------------*/
                        /* MSサークル情報の更新 */
                        /*      EXEC SQL UPDATE MSサークル顧客情報@CMSD          */
                        /*           SET ＴＥＬ止め区分       = 0,               */
                        /*               ＤＭ止め区分         = 3092,            */
                        /*               Ｅメール止め区分     = 5092,            */
                        /*               携帯ＴＥＬ止め区分   = 0,               */
                        /*               携帯Ｅメール止め区分 = 0,               */
                        /*               退会日               = :this_date,      */
                        /*               削除フラグ           = 2,               */
                        /*               バッチ更新日         = :this_date,      */
                        /*               最終更新日           = :this_date,      */
                        /*               最終更新日時         = SYSDATE,         */
                        /*               最終更新プログラムＩＤ = :Program_Name  */
                        /*         WHERE 顧客番号 = :mmkokgb_t.kokyaku_no;       */

//if (DBG_LOG){
//          /*------------------------------------------------------------*/
//          C_DbgMsg("*** cmBTleavB_main *** MSサークル顧客情報 UPDATE sqlcode=[%d]\n",
//                                           sqlca.sqlcode);
//          /*------------------------------------------------------------*/
//}

                        /* エラーの場合(NOTFOUND以外)処理終了 */
//      if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
//          memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
//          sprintf( chg_format_buf, "顧客番号=%s", mmkokgb_t.kokyaku_no.arr );
//          APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
//                                   "MSサークル顧客情報", chg_format_buf, 0, 0);
//          return C_const_NG;
//      }
                        /* 2022/10/14 MCCM初版 DEL END */

                        /*-----------------------------------------------*/
                        /* （６）１８．処理結果ファイル書込み処理        */
                        /*-----------------------------------------------*/
                        rtn_cd = cmBTleavB_WriteFile(); /* ★処理結果ファイル書込み           */
                        if ( rtn_cd != C_const_OK ) {
                            return C_const_NG;
                        }
                    }
                }                                                                                                                                                          /* 2022/10/14 MCCM初版 ADD */

                ok_cnt++;                           /* 正常処理件数カウントアップ         */

//                EXEC SQL COMMIT WORK;               /* コミット                           */
                sqlca.commit();

            }   /* ループ１（FILE READ LOOP）*/
        }/*  forループEND */

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("cmBTleavB_main処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return C_const_OK;              /* 処理終了                           */
        /*-----cmBTleavB_main Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTleavB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTleavB_Cchk_Arg( char *Arg_in )                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*              １）重複チェック                                              */
    /*              ２）桁数チェック                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       *    Arg_in      ：引数値                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTleavB_Chk_Arg( StringDto Arg_in )
    {
        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("cmBTleavB_Chk_Arg処理");
                C_DbgMsg("*** cmBTleavB_Chk_Arg *** 引数=[%s]\n", Arg_in);
                /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );

        if ( 0 == memcmp( Arg_in, DEF_ARG_I, 2 ) ) {       /* -i入力ファイルチェック  */
            if( chk_arg_i != DEF_OFF ) {
                return C_const_NG;
            }
            chk_arg_i = DEF_ON;

            if ( strlen(Arg_in) <= 2 ) {               /* 桁数チェック            */
                return C_const_NG;
            }

        } else if( 0 == memcmp( Arg_in, DEF_ARG_O, 2 ) ) { /* -o出力ファイルチェック  */
            if( chk_arg_o != DEF_OFF ) {
                return C_const_NG;
            }
            chk_arg_o = DEF_ON;

            if ( strlen(Arg_in) <= 2 ) {               /* 桁数チェック            */
                return C_const_NG;
            }
        } else if( 0 == memcmp( Arg_in, DEF_ARG_S, 2 ) ) { /* -s入力ファイルチェック */                                                                                  /* 2022/10/14 MCCM初版 ADD */
            if( chk_arg_s != DEF_OFF ) {                                                                                                                             /* 2022/10/14 MCCM初版 ADD */
                return C_const_NG;                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
            }                                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */
            chk_arg_s = DEF_ON;                                                                                                                                      /* 2022/10/14 MCCM初版 ADD */
            /* 2022/10/14 MCCM初版 ADD */
            if ( strlen(Arg_in) <= 2 ) {               /* 桁数チェック            */                                                                                 /* 2022/10/14 MCCM初版 ADD */
                return C_const_NG;                                                                                                                                   /* 2022/10/14 MCCM初版 ADD */
            }                                                                                                                                                        /* 2022/10/14 MCCM初版 ADD */
        }

        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgEnd("cmBTleavB_Chk_Arg処理", 0, 0, 0);
                /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----cmBTleavB_Chk_Arg Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTleavB_OpenFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTleavB_OpenFile()                                          */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルと出力ファイルをオープンする                      */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTleavB_OpenFile()
    {
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("cmBTleavB_OpenFile処理");
                /*------------------------------------------------------------*/
        }
        /* 初期化 */
//  memset (inp_fl_name, 0x00, sizeof(inp_fl_name) );                                                                                                            /* 2022/10/14 MCCM初版 DEL */
        memset (out_fl_name, 0x00, sizeof(out_fl_name) );
        /* 2022/10/14 MCCM初版 ADD START */
//        char* arg_value[2] = {arg_i_Value, arg_s_Value};
        String[] arg_value = {arg_i_Value.arr, arg_s_Value.arr};
        int  i = 0;
        /* 2022/10/14 MCCM初版 ADD END */

        for(i=0;i<2;i++){                                                                                                                                            /* 2022/10/14 MCCM初版 ADD */
//            memset (inp_fl_name[i], 0x00, 0);
            inp_fl_name[i] = new StringDto();
            /* 2022/10/14 MCCM初版 ADD */
            /* 入力ファイルのオープン */
            sprintf( inp_fl_name[i], "%s/%s", inp_file_dir, arg_value[i]);                                                                                               /* 2022/10/14 MCCM初版 MOD */
            if ((fp_inp[i] = fopen(inp_fl_name[i], SystemConstant.Shift_JIS,FileOpenType.r)).fd == C_const_NG) {
//            if (( fp_inp[i] = fopen(inp_fl_name[i], "r" )) == null) {                                                                                                    /* 2022/10/14 MCCM初版 MOD */
                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** cmBTleavB_OpenFile *** 入力ファイルオープンNG[%s]\n",inp_fl_name[i]);                                                                      /* 2022/10/14 MCCM初版 MOD */
                                /*------------------------------------------------------------*/
                }
                memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
                sprintf( chg_format_buf, "fopen（%s）", inp_fl_name[i] );                                                                                                /* 2022/10/14 MCCM初版 MOD */
                APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
                return C_const_NG;
            }
            if (DBG_LOG){                                                                                                                                                      /* 2022/10/14 MCCM初版 ADD */
                        /*------------------------------------------------------------*/                                                                                             /* 2022/10/14 MCCM初版 ADD */
                        C_DbgMsg("*** cmBTleavB_OpenFile *** 入力ファイル[%s]\n", inp_fl_name[i]);                                                                                   /* 2022/10/14 MCCM初版 ADD */
                        /*------------------------------------------------------------*/                                                                                             /* 2022/10/14 MCCM初版 ADD */
            }                                                                                                                                                           /* 2022/10/14 MCCM初版 ADD */
        }                                                                                                                                                              /* 2022/10/14 MCCM初版 ADD */

        /* 出力ファイルのオープン */
        sprintf( out_fl_name, "%s/%s", out_file_dir, arg_o_Value);

        if ((fp_out = fopen(out_fl_name, SystemConstant.Shift_JIS,FileOpenType.w)).fd == C_const_NG) {
//        if (( fp_out = fopen(out_fl_name, "w" )) == null) {
            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_OpenFile *** 出力ファイルオープンNG[%s]\n",out_fl_name);
                        /*------------------------------------------------------------*/
            }
            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
            sprintf( chg_format_buf, "fopen（%s）", out_fl_name );
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            fclose( fp_inp[0] );            /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
            fclose( fp_inp[1] );            /* 入力ファイルＣＬＯＳＥ             */                                                                                 /* 2022/10/14 MCCM初版 MOD */
            return C_const_NG;
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
        //  C_DbgMsg("*** cmBTleavB_OpenFile *** 入力ファイル[%s]\n", inp_fl_name);                                                                                      /* 2022/10/14 MCCM初版 MOD */
                C_DbgMsg("*** cmBTleavB_OpenFile *** 出力ファイル[%s]\n", out_fl_name);
                C_DbgEnd("cmBTleavB_OpenFile処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----cmBTleavB_OpenFile Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTleavB_ReadFile                                              */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTleavB_ReadFile(int file_flg)                              */                                                                                 /* 2022/10/14 MCCM初版 MOD */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルより１レコードを読み込む                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              file_flg                                                      */                                                                                 /* 2022/10/14 MCCM初版 MOD */
    /*              0   :  退会顧客データファイル                                 */                                                                                 /* 2022/10/14 MCCM初版 ADD */
    /*              1   :  顧客ステータス退会ファイル                             */                                                                                 /* 2022/10/14 MCCM初版 ADD */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTleavB_ReadFile(int file_flg)                                                                                                                     /* 2022/10/14 MCCM初版 MOD */
    {
        /* size_t sz_ret; */
        /* char *next; */
//        char *pret;
        StringDto buf = new StringDto(256);

        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("cmBTleavB_ReadFile処理");
                /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        inp_taikai_t=new TAIKAI_KOKYAKU_DATA();
        memset( inp_taikai_t, 0x00, sizeof(inp_taikai_t) );

        /* 読込み */
        /*sz_ret = fread(&inp_taikai_t, sizeof(inp_taikai_t), 1, fp_inp);*/
        /* sz_ret = fgets(buf, sizeof(buf), fp_inp); */
        /* 2014/11/26 fgets関数の戻り値誤りのため修正 */
        fgets(buf, sizeof(buf), fp_inp[file_flg]);                                                                                                            /* 2022/10/14 MCCM初版 MOD */

        if ( feof( fp_inp[file_flg] ) != C_const_OK ) {  /* ＥＯＦ                          */
        if (DBG_LOG){
                    /*---------------------------------------------*/
                    C_DbgMsg("*** cmBTleavB_ReadFile *** READ EOF status=[%d]\n", feof( fp_inp[file_flg] ));                                                                 /* 2022/10/14 MCCM初版 MOD */
                    /*---------------------------------------------*/
        }
            return DEF_EOF;
        }

        if ( ferror( fp_inp[file_flg] ) != C_const_OK ) { /* ＲＥＡＤエラー                 */                                                                       /* 2022/10/14 MCCM初版 MOD */
            if (DBG_LOG){
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_ReadFile *** 入力ファイルリードNG%s\n", "ERR");
                        /*-------------------------------------------------------------*/
            }
            sprintf( chg_format_buf, "fgets（%s）", inp_fl_name[file_flg] );                                                                                         /* 2022/10/14 MCCM初版 MOD */
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /* ＲＥＡＤサイズエラー           */
        /*    if (sz_ret < 1) { */
        /*if (DBG_LOG){ */
        /*-------------------------------------------------------------*/
        /*        C_DbgMsg("*** cmBTleavB_ReadFile *** ファイルリードサイズNG=[%ld]\n", */
        /*                                             sizeof(inp_taikai_t)); */
        /*-------------------------------------------------------------*/
        /*} */
        /*        sprintf( chg_format_buf, "fgets（%s）", inp_fl_name ); */
        /*        APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0); */
        /*        return C_const_NG; */
        /*    } */

        /* 改行コードを削除 */
        strtok(buf, "\n");

        /* 改行コードをNULL0にしておく */
        /*if (inp_taikai_t.lf[0] == '\n') inp_taikai_t.lf[0] = 0x00;*/

        strcpy(inp_taikai_t.kokyaku_no, buf);

        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                /* C_DbgMsg("*** cmBTleavB_ReadFile *** fgets=[%ld]\n", sz_ret); */
                C_DbgMsg("*** cmBTleavB_ReadFile *** kaiin=[%s]\n", inp_taikai_t.kokyaku_no);
                /*-------------------------------------------------------------*/
        }
        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgEnd("cmBTleavB_ReadFile処理", 0, 0, 0);
                /*-------------------------------------------------------------*/
        }
        return C_const_OK;
        /*-----cmBTleavB_ReadFile Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTleavB_WriteFile                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTleavB_WriteFile()                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              編集バッファを処理結果ファイルに書込む                        */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /******************************************************************************/
    public int  cmBTleavB_WriteFile()
    {
        int           rtn_cd;                   /* 関数戻り値                         */
        StringDto          chg_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */

        StringDto outbuf = new StringDto(2000); /* 出力する行のバッファ */

        if (DBG_LOG){
                /*---------------------------------------------------------------------*/
                C_DbgStart("cmBTleavB_WriteFile処理");
                /*---------------------------------------------------------------------*/
        }

        /*** 行編集 ***/
        strcpy(outbuf, "");

        /* 顧客番号         */
        strcat(outbuf, out_kekka_t.kokyaku_no.strVal());
        strcat(outbuf, ",");

        /* 顧客名称         */
        strcat(outbuf, out_kekka_t.kokyaku_mesho.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 顧客カナ名称     */
        strcat(outbuf, out_kekka_t.kokyaku_kana_mesho.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 年齢             */
        strcat(outbuf, out_kekka_t.nenre.strVal());
        strcat(outbuf, ",");

        /* 誕生年           */
        strcat(outbuf, out_kekka_t.tanjo_y.strVal());
        strcat(outbuf, ",");

        /* 誕生月           */
        strcat(outbuf, out_kekka_t.tanjo_m.strVal());
        strcat(outbuf, ",");

        /* 誕生日           */
        strcat(outbuf, out_kekka_t.tanjo_d.strVal());
        strcat(outbuf, ",");

        /* 性別             */
        strcat(outbuf, out_kekka_t.sebetsu.strVal());
        strcat(outbuf, ",");

        /* 郵便番号         */
        strcat(outbuf, out_kekka_t.yubin_no.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 郵便番号コード   */
        strcat(outbuf, out_kekka_t.yubin_no_cd.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 住所１           */
        strcat(outbuf, out_kekka_t.jusho_1.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 住所２           */
        strcat(outbuf, out_kekka_t.jusho_2.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 住所３           */
//  strcat(outbuf, out_kekka_t.jusho_3);                                                                                                                         /* 2022/10/14 MCCM初版 DEL */
//  trimRight(outbuf);                                                                                                                                           /* 2022/10/14 MCCM初版 DEL */
//  strcat(outbuf, ",");                                                                                                                                         /* 2022/10/14 MCCM初版 DEL */

        /* 電話番号１       */
        strcat(outbuf, out_kekka_t.denwa_no_1.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 電話番号２       */
        strcat(outbuf, out_kekka_t.denwa_no_2.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 電話番号３       */
        strcat(outbuf, out_kekka_t.denwa_no_3.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 電話番号４       */
        strcat(outbuf, out_kekka_t.denwa_no_4.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 検索電話番号１   */
        strcat(outbuf, out_kekka_t.kensaku_denwa_no_1.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 検索電話番号２   */
        strcat(outbuf, out_kekka_t.kensaku_denwa_no_2.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 検索電話番号３   */
        strcat(outbuf, out_kekka_t.kensaku_denwa_no_3.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 検索電話番号４   */
        strcat(outbuf, out_kekka_t.kensaku_denwa_no_4.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* Ｅメールアドレス１ */
        strcat(outbuf, out_kekka_t.email_address_1.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* Ｅメールアドレス２ */
        strcat(outbuf, out_kekka_t.email_address_2.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 職業             */
        strcat(outbuf, out_kekka_t.shokugyo.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* 勤務区分         */
        strcat(outbuf, out_kekka_t.kinmu_kbn.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* Ｅメールアドレス３ */
        strcat(outbuf, out_kekka_t.email_address_3.strVal());
        trimRight(outbuf);
        strcat(outbuf, ",");

        /* Ｅメールアドレス４ */
        strcat(outbuf, out_kekka_t.email_address_4.strVal());
        trimRight(outbuf);
        strcat(outbuf, "\n");

        /* 処理結果ファイル書込み */
        rtn_cd = fputs(outbuf, fp_out);
        if (rtn_cd == EOF) {
            /* if ( (rtn_cd = ferror(fp_out)) != C_const_OK ) { */
            if (DBG_LOG){
                        /*-------------------------------------------------------------*/
                        C_DbgMsg("*** cmBTleavB_WriteFile *** fputs NG rtn=[%d]\n", rtn_cd);
                        /*-------------------------------------------------------------*/
            }
            memset( chg_format_buf, 0x00, sizeof(chg_format_buf) );
            sprintf( chg_format_buf, "fputs（%s）", out_fl_name );
            APLOG_WT("903", 0, null, chg_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG){
                /*-------------------------------------------------------------*/
                C_DbgEnd("cmBTleavB_WriteFile処理", 0, 0, 0);
                /*-------------------------------------------------------------*/
        }
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： getKazokuSedoInfo                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL *ksMaster, int *status) */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              家族制度情報取得                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              MS_KAZOKU_SEDO_INFO_TBL * ksMaster ： 家族制度情報構造体      */
    /*              int                     * status   ： 結果ステータス          */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */
    /******************************************************************************/
    public int getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL ksMaster, IntegerDto status)
    {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        MS_KAZOKU_SEDO_INFO_TBL h_ms_kazoku_sedo_buff = null; /* MS家族制度情報バッファ */
//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG){
                C_DbgMsg("getKazokuSedoInfo : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (ksMaster == null || status == null) {
            /* 入力引数エラー */
            C_DbgMsg("getKazokuSedoInfo : %s\n", "PRMERR(NULL)");
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            C_DbgMsg("getKazokuSedoInfo : %s\n", "DBERR(connect check NG)");
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* 出力エリアのクリア */
//      memset(ksMaster->kazoku_oya_kokyaku_no.arr,0x00,sizeof(ksMaster->kazoku_oya_kokyaku_no.arr));                                                            /* 2022/10/14 MCCM初版 DEL */
//      ksMaster->kazoku_oya_kokyaku_no.len = 0;  /* 家族親顧客番号 */                                                                                           /* 2022/10/14 MCCM初版 DEL */
        memset(ksMaster.kazoku_1_kokyaku_no ,0x00,sizeof(ksMaster.kazoku_1_kokyaku_no));
//        ksMaster.kazoku_1_kokyaku_no.len = 0;  /* 家族１顧客番号 */
        memset(ksMaster.kazoku_2_kokyaku_no ,0x00,sizeof(ksMaster.kazoku_2_kokyaku_no));
//        ksMaster.kazoku_2_kokyaku_no.len = 0;  /* 家族２顧客番号 */
        memset(ksMaster.kazoku_3_kokyaku_no,0x00,sizeof(ksMaster.kazoku_3_kokyaku_no));
//        ksMaster.kazoku_3_kokyaku_no.len = 0;  /* 家族３顧客番号 */
        memset(ksMaster.kazoku_4_kokyaku_no,0x00,sizeof(ksMaster.kazoku_4_kokyaku_no ));
//        ksMaster.kazoku_4_kokyaku_no.len = 0;  /* 家族４顧客番号 */
        memset(ksMaster.kazoku_5_kokyaku_no,0x00,sizeof(ksMaster.kazoku_5_kokyaku_no ));                                                                /* 2022/10/14 MCCM初版 ADD */
//        ksMaster.kazoku_5_kokyaku_no.len = 0;  /* 家族５顧客番号 */                                                                                             /* 2022/10/14 MCCM初版 ADD */
        memset(ksMaster.kazoku_6_kokyaku_no,0x00,sizeof(ksMaster.kazoku_6_kokyaku_no ));                                                                /* 2022/10/14 MCCM初版 ADD */
//        ksMaster.kazoku_6_kokyaku_no.len = 0;  /* 家族６顧客番号 */                                                                                             /* 2022/10/14 MCCM初版 ADD */

//      ksMaster->kazoku_oya_toroku_ymd = 0;    /* 家族親登録日         */                                                                                       /* 2022/10/14 MCCM初版 DEL */
        ksMaster.kazoku_1_toroku_ymd .arr  = 0;    /* 家族１登録日         */
        ksMaster.kazoku_2_toroku_ymd .arr  = 0;    /* 家族２登録日         */
        ksMaster.kazoku_3_toroku_ymd .arr  = 0;    /* 家族３登録日         */
        ksMaster.kazoku_4_toroku_ymd .arr  = 0;    /* 家族４登録日         */
        ksMaster.kazoku_5_toroku_ymd .arr  = 0;    /* 家族５登録日         */                                                                                       /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_6_toroku_ymd .arr  = 0;    /* 家族６登録日         */                                                                                       /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_sakusei_ymd  .arr  = 0;    /* 家族作成日           */

        ksMaster.kazoku_1_toroku_time.arr = 0;    /* 家族１登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_2_toroku_time.arr = 0;    /* 家族２登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_3_toroku_time.arr = 0;    /* 家族３登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_4_toroku_time.arr = 0;    /* 家族４登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_5_toroku_time.arr = 0;    /* 家族５登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_6_toroku_time.arr = 0;    /* 家族６登録時刻       */                                                                                       /* 2022/12/19 MCCM初版 ADD */


        ksMaster.kazoku_rankup_kingaku_saishu_koshin_ymd.arr = 0;   /* 家族ランクＵＰ金額最終更新日   */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_0.arr= 0.0;   /* 年間家族ランクアップ対象金額０ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_1.arr= 0.0;   /* 年間家族ランクアップ対象金額１ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_2.arr= 0.0;   /* 年間家族ランクアップ対象金額２ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_3.arr= 0.0;   /* 年間家族ランクアップ対象金額３ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_4.arr= 0.0;   /* 年間家族ランクアップ対象金額４ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_5.arr= 0.0;   /* 年間家族ランクアップ対象金額５ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_6.arr= 0.0;   /* 年間家族ランクアップ対象金額６ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_7.arr= 0.0;   /* 年間家族ランクアップ対象金額７ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_8.arr= 0.0;   /* 年間家族ランクアップ対象金額８ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_9.arr= 0.0;   /* 年間家族ランクアップ対象金額９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_001.arr= 0.0; /* 月間家族ランクＵＰ金額００１   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_002.arr= 0.0; /* 月間家族ランクＵＰ金額００２   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_003.arr= 0.0; /* 月間家族ランクＵＰ金額００３   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_004.arr= 0.0; /* 月間家族ランクＵＰ金額００４   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_005.arr= 0.0; /* 月間家族ランクＵＰ金額００５   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_006.arr= 0.0; /* 月間家族ランクＵＰ金額００６   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_007.arr= 0.0; /* 月間家族ランクＵＰ金額００７   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_008.arr= 0.0; /* 月間家族ランクＵＰ金額００８   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_009.arr= 0.0; /* 月間家族ランクＵＰ金額００９   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_010.arr= 0.0; /* 月間家族ランクＵＰ金額０１０   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_011.arr= 0.0; /* 月間家族ランクＵＰ金額０１１   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_012.arr= 0.0; /* 月間家族ランクＵＰ金額０１２   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_101.arr= 0.0; /* 月間家族ランクＵＰ金額１０１   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_102.arr= 0.0; /* 月間家族ランクＵＰ金額１０２   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_103.arr= 0.0; /* 月間家族ランクＵＰ金額１０３   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_104.arr= 0.0; /* 月間家族ランクＵＰ金額１０４   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_105.arr= 0.0; /* 月間家族ランクＵＰ金額１０５   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_106.arr= 0.0; /* 月間家族ランクＵＰ金額１０６   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_107.arr= 0.0; /* 月間家族ランクＵＰ金額１０７   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_108.arr= 0.0; /* 月間家族ランクＵＰ金額１０８   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_109.arr= 0.0; /* 月間家族ランクＵＰ金額１０９   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_110.arr= 0.0; /* 月間家族ランクＵＰ金額１１０   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_111.arr= 0.0; /* 月間家族ランクＵＰ金額１１１   */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_112.arr= 0.0; /* 月間家族ランクＵＰ金額１１２   */

        ksMaster.nenji_rank_cd_0    .arr= 0;             /* 年次ランクコード０       */
        ksMaster.nenji_rank_cd_1    .arr= 0;             /* 年次ランクコード１       */
        ksMaster.nenji_rank_cd_2    .arr= 0;             /* 年次ランクコード２       */
        ksMaster.nenji_rank_cd_3    .arr= 0;             /* 年次ランクコード３       */
        ksMaster.nenji_rank_cd_4    .arr= 0;             /* 年次ランクコード４       */
        ksMaster.nenji_rank_cd_5    .arr= 0;             /* 年次ランクコード５       */
        ksMaster.nenji_rank_cd_6    .arr= 0;             /* 年次ランクコード６       */
        ksMaster.nenji_rank_cd_7    .arr= 0;             /* 年次ランクコード７       */
        ksMaster.nenji_rank_cd_8    .arr= 0;             /* 年次ランクコード８       */
        ksMaster.nenji_rank_cd_9    .arr= 0;             /* 年次ランクコード９       */
        ksMaster.getuji_rank_cd_001 .arr= 0;             /* 月次ランクコード００１   */
        ksMaster.getuji_rank_cd_002 .arr= 0;             /* 月次ランクコード００２   */
        ksMaster.getuji_rank_cd_003 .arr= 0;             /* 月次ランクコード００３   */
        ksMaster.getuji_rank_cd_004 .arr= 0;             /* 月次ランクコード００４   */
        ksMaster.getuji_rank_cd_005 .arr= 0;             /* 月次ランクコード００５   */
        ksMaster.getuji_rank_cd_006 .arr= 0;             /* 月次ランクコード００６   */
        ksMaster.getuji_rank_cd_007 .arr= 0;             /* 月次ランクコード００７   */
        ksMaster.getuji_rank_cd_008 .arr= 0;             /* 月次ランクコード００８   */
        ksMaster.getuji_rank_cd_009 .arr= 0;             /* 月次ランクコード００９   */
        ksMaster.getuji_rank_cd_010 .arr= 0;             /* 月次ランクコード０１０   */
        ksMaster.getuji_rank_cd_011 .arr= 0;             /* 月次ランクコード０１１   */
        ksMaster.getuji_rank_cd_012 .arr= 0;             /* 月次ランクコード０１２   */
        ksMaster.getuji_rank_cd_101 .arr= 0;             /* 月次ランクコード１０１   */
        ksMaster.getuji_rank_cd_102 .arr= 0;             /* 月次ランクコード１０２   */
        ksMaster.getuji_rank_cd_103 .arr= 0;             /* 月次ランクコード１０３   */
        ksMaster.getuji_rank_cd_104 .arr= 0;             /* 月次ランクコード１０４   */
        ksMaster.getuji_rank_cd_105 .arr= 0;             /* 月次ランクコード１０５   */
        ksMaster.getuji_rank_cd_106 .arr= 0;             /* 月次ランクコード１０６   */
        ksMaster.getuji_rank_cd_107 .arr= 0;             /* 月次ランクコード１０７   */
        ksMaster.getuji_rank_cd_108 .arr= 0;             /* 月次ランクコード１０８   */
        ksMaster.getuji_rank_cd_109 .arr= 0;             /* 月次ランクコード１０９   */
        ksMaster.getuji_rank_cd_110 .arr= 0;             /* 月次ランクコード１１０   */
        ksMaster.getuji_rank_cd_111 .arr= 0;             /* 月次ランクコード１１１   */
        ksMaster.getuji_rank_cd_112 .arr= 0;             /* 月次ランクコード１１２   */

        ksMaster.kazoku_sakujo_ymd .arr = 0;             /* 家族削除日           */

        ksMaster.sagyo_kigyo_cd    .arr = 0;             /* 作業企業コード       */
        ksMaster.sagyosha_id       .arr = 0.0;           /* 作業者ＩＤ           */
        ksMaster.sagyo_ymd         .arr = 0;             /* 作業年月日           */
        ksMaster.sagyo_hms         .arr = 0;             /* 作業時刻             */

        ksMaster.batch_koshin_ymd  .arr = 0;             /* バッチ更新日         */
        ksMaster.saishu_koshin_ymd .arr = 0;             /* 最終更新日           */
        ksMaster.saishu_koshin_ymdhms.arr = 0.0;         /* 最終更新日時         */
        strcpy(ksMaster.saishu_koshin_programid, "                    "); /* スペース２０桁 */


        /* ホスト変数を編集する */
        /* (検索条件)           */
        h_ms_kazoku_sedo_buff = new MS_KAZOKU_SEDO_INFO_TBL();
        memset(h_ms_kazoku_sedo_buff, 0x00, 0);
        strcpy(h_ms_kazoku_sedo_buff.kazoku_id, ksMaster.kazoku_id);                                                                    /* 2022/10/14 MCCM初版 MOD */
        h_ms_kazoku_sedo_buff.kazoku_id.len = ksMaster.kazoku_id.len;                                                                                           /* 2022/12/12 MCCM初版 ADD */

        if (DBG_LOG){
                C_DbgMsg("getKazokuSedoInfo : kazoku_id=%s\n", h_ms_kazoku_sedo_buff.kazoku_id.arr);                                                                 /* 2022/10/14 MCCM初版 MOD */
        }

        /* ＳＱＬを実行する */

        /* 環境変数の取得 */
        if (strcmp(Cg_ORASID, "CMSD") == 0) {
            /* 顧客データベースに接続している場合 */

            if (DBG_LOG){
                        C_DbgMsg("getKazokuSedoInfo : %s\n", "@ CMMD");
            }



//            EXEC SQL
//            /*                  SELECT  NVL(家族親顧客番号,0),                            */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//            SELECT  NVL(家族１顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 MOD */
//            NVL(家族２顧客番号,0),
//                    NVL(家族３顧客番号,0),
//                    NVL(家族４顧客番号,0),
//                    NVL(家族５顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族６顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    /*                          NVL(家族親登録日,0),                              */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                    NVL(家族１登録日,0),
//                    NVL(家族２登録日,0),
//                    NVL(家族３登録日,0),
//                    NVL(家族４登録日,0),
//                    NVL(家族５登録日,0),                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族６登録日,0),                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族作成日,0),
//                    NVL(家族ランクＵＰ金額最終更新日,0),
//                    NVL(年間家族ランクＵＰ対象金額０,0),
//                    NVL(年間家族ランクＵＰ対象金額１,0),
//                    NVL(年間家族ランクＵＰ対象金額２,0),
//                    NVL(年間家族ランクＵＰ対象金額３,0),
//                    NVL(年間家族ランクＵＰ対象金額４,0),
//                    NVL(年間家族ランクＵＰ対象金額５,0),
//                    NVL(年間家族ランクＵＰ対象金額６,0),
//                    NVL(年間家族ランクＵＰ対象金額７,0),
//                    NVL(年間家族ランクＵＰ対象金額８,0),
//                    NVL(年間家族ランクＵＰ対象金額９,0),
//                    NVL(月間家族ランクＵＰ金額００１,0),
//                    NVL(月間家族ランクＵＰ金額００２,0),
//                    NVL(月間家族ランクＵＰ金額００３,0),
//                    NVL(月間家族ランクＵＰ金額００４,0),
//                    NVL(月間家族ランクＵＰ金額００５,0),
//                    NVL(月間家族ランクＵＰ金額００６,0),
//                    NVL(月間家族ランクＵＰ金額００７,0),
//                    NVL(月間家族ランクＵＰ金額００８,0),
//                    NVL(月間家族ランクＵＰ金額００９,0),
//                    NVL(月間家族ランクＵＰ金額０１０,0),
//                    NVL(月間家族ランクＵＰ金額０１１,0),
//                    NVL(月間家族ランクＵＰ金額０１２,0),
//                    NVL(月間家族ランクＵＰ金額１０１,0),
//                    NVL(月間家族ランクＵＰ金額１０２,0),
//                    NVL(月間家族ランクＵＰ金額１０３,0),
//                    NVL(月間家族ランクＵＰ金額１０４,0),
//                    NVL(月間家族ランクＵＰ金額１０５,0),
//                    NVL(月間家族ランクＵＰ金額１０６,0),
//                    NVL(月間家族ランクＵＰ金額１０７,0),
//                    NVL(月間家族ランクＵＰ金額１０８,0),
//                    NVL(月間家族ランクＵＰ金額１０９,0),
//                    NVL(月間家族ランクＵＰ金額１１０,0),
//                    NVL(月間家族ランクＵＰ金額１１１,0),
//                    NVL(月間家族ランクＵＰ金額１１２,0),
//                    NVL(年次ランクコード０,0),
//                    NVL(年次ランクコード１,0),
//                    NVL(年次ランクコード２,0),
//                    NVL(年次ランクコード３,0),
//                    NVL(年次ランクコード４,0),
//                    NVL(年次ランクコード５,0),
//                    NVL(年次ランクコード６,0),
//                    NVL(年次ランクコード７,0),
//                    NVL(年次ランクコード８,0),
//                    NVL(年次ランクコード９,0),
//                    NVL(月次ランクコード００１,0),
//                    NVL(月次ランクコード００２,0),
//                    NVL(月次ランクコード００３,0),
//                    NVL(月次ランクコード００４,0),
//                    NVL(月次ランクコード００５,0),
//                    NVL(月次ランクコード００６,0),
//                    NVL(月次ランクコード００７,0),
//                    NVL(月次ランクコード００８,0),
//                    NVL(月次ランクコード００９,0),
//                    NVL(月次ランクコード０１０,0),
//                    NVL(月次ランクコード０１１,0),
//                    NVL(月次ランクコード０１２,0),
//                    NVL(月次ランクコード１０１,0),
//                    NVL(月次ランクコード１０２,0),
//                    NVL(月次ランクコード１０３,0),
//                    NVL(月次ランクコード１０４,0),
//                    NVL(月次ランクコード１０５,0),
//                    NVL(月次ランクコード１０６,0),
//                    NVL(月次ランクコード１０７,0),
//                    NVL(月次ランクコード１０８,0),
//                    NVL(月次ランクコード１０９,0),
//                    NVL(月次ランクコード１１０,0),
//                    NVL(月次ランクコード１１１,0),
//                    NVL(月次ランクコード１１２,0),
//                    NVL(家族削除日,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(最終更新日時,'yyyymmddhh24miss')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(家族１登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族２登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族３登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族４登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族５登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族６登録時刻,0)                                                                                                                /* 2022/12/19 MCCM初版 ADD */
//            /*                    INTO  :h_ms_kazoku_sedo_buff.kazoku_oya_kokyaku_no,                      家族親顧客番号       */                                           /* 2022/10/14 MCCM初版 DEL */
//            INTO  :h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,                     /* 家族１顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,                     /* 家族２顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,                     /* 家族３顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,                     /* 家族４顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,                     /* 家族５顧客番号       */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,                     /* 家族６顧客番号       */                                           /* 2022/10/14 MCCM初版 ADD */
//            /*                          :h_ms_kazoku_sedo_buff.kazoku_oya_toroku_ymd,                      家族親登録日         */                                           /* 2022/10/14 MCCM初版 DEL */
//                            :h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,                     /* 家族１登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,                     /* 家族２登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,                     /* 家族３登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,                     /* 家族４登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,                     /* 家族５登録日         */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,                     /* 家族６登録日         */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,                      /* 家族作成日           */
//                            :h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd, /* 家族ランクＵＰ金額最終更新日 */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,   /* 年間家族ランクＵＰ対象金額０ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,   /* 年間家族ランクＵＰ対象金額１ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,   /* 年間家族ランクＵＰ対象金額２ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,   /* 年間家族ランクＵＰ対象金額３ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,   /* 年間家族ランクＵＰ対象金額４ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,   /* 年間家族ランクＵＰ対象金額５ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,   /* 年間家族ランクＵＰ対象金額６ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,   /* 年間家族ランクＵＰ対象金額７ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,   /* 年間家族ランクＵＰ対象金額８ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,   /* 年間家族ランクＵＰ対象金額９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001, /* 月間家族ランクＵＰ金額００１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002, /* 月間家族ランクＵＰ金額００２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003, /* 月間家族ランクＵＰ金額００３ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004, /* 月間家族ランクＵＰ金額００４ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005, /* 月間家族ランクＵＰ金額００５ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006, /* 月間家族ランクＵＰ金額００６ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007, /* 月間家族ランクＵＰ金額００７ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008, /* 月間家族ランクＵＰ金額００８ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009, /* 月間家族ランクＵＰ金額００９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010, /* 月間家族ランクＵＰ金額０１０ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011, /* 月間家族ランクＵＰ金額０１１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012, /* 月間家族ランクＵＰ金額０１２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101, /* 月間家族ランクＵＰ金額１０１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102, /* 月間家族ランクＵＰ金額１０２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103, /* 月間家族ランクＵＰ金額１０３ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104, /* 月間家族ランクＵＰ金額１０４ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105, /* 月間家族ランクＵＰ金額１０５ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106, /* 月間家族ランクＵＰ金額１０６ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107, /* 月間家族ランクＵＰ金額１０７ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108, /* 月間家族ランクＵＰ金額１０８ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109, /* 月間家族ランクＵＰ金額１０９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110, /* 月間家族ランクＵＰ金額１１０ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111, /* 月間家族ランクＵＰ金額１１１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112, /* 月間家族ランクＵＰ金額１１２ */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_0,                         /* 年次ランクコード０     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_1,                         /* 年次ランクコード１     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_2,                         /* 年次ランクコード２     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_3,                         /* 年次ランクコード３     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_4,                         /* 年次ランクコード４     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_5,                         /* 年次ランクコード５     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_6,                         /* 年次ランクコード６     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_7,                         /* 年次ランクコード７     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_8,                         /* 年次ランクコード８     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_9,                         /* 年次ランクコード９     */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_001,                      /* 月次ランクコード００１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_002,                      /* 月次ランクコード００２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_003,                      /* 月次ランクコード００３ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_004,                      /* 月次ランクコード００４ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_005,                      /* 月次ランクコード００５ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_006,                      /* 月次ランクコード００６ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_007,                      /* 月次ランクコード００７ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_008,                      /* 月次ランクコード００８ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_009,                      /* 月次ランクコード００９ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_010,                      /* 月次ランクコード０１０ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_011,                      /* 月次ランクコード０１１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_012,                      /* 月次ランクコード０１２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_101,                      /* 月次ランクコード１０１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_102,                      /* 月次ランクコード１０２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_103,                      /* 月次ランクコード１０３ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_104,                      /* 月次ランクコード１０４ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_105,                      /* 月次ランクコード１０５ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_106,                      /* 月次ランクコード１０６ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_107,                      /* 月次ランクコード１０７ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_108,                      /* 月次ランクコード１０８ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_109,                      /* 月次ランクコード１０９ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_110,                      /* 月次ランクコード１１０ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_111,                      /* 月次ランクコード１１１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_112,                      /* 月次ランクコード１１２ */
//                            :h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,                       /* 家族削除日             */
//                            :h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,                          /* 作業企業コード         */
//                            :h_ms_kazoku_sedo_buff.sagyosha_id,                             /* 作業者ＩＤ             */
//                            :h_ms_kazoku_sedo_buff.sagyo_ymd,                               /* 作業年月日             */
//                            :h_ms_kazoku_sedo_buff.sagyo_hms,                               /* 作業時刻               */
//                            :h_ms_kazoku_sedo_buff.batch_koshin_ymd,                        /* バッチ更新日           */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymd,                       /* 最終更新日             */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,                    /* 最終更新日時           */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_programid,                 /* 最終更新プログラムＩＤ */
//                            :h_ms_kazoku_sedo_buff.kazoku_1_toroku_time,                    /* 家族１登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_toroku_time,                    /* 家族２登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_toroku_time,                    /* 家族３登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_toroku_time,                    /* 家族４登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_toroku_time,                    /* 家族５登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_toroku_time                     /* 家族６登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//            FROM  MS家族制度情報
//            WHERE 家族ＩＤ = :h_ms_kazoku_sedo_buff.kazoku_id;

            sqlca.sql = new StringDto("SELECT  NVL(家族１顧客番号,0)," +
                    " NVL(家族２顧客番号,0)," +
                    "         NVL(家族３顧客番号,0)," +
                    "         NVL(家族４顧客番号,0)," +
                    "         NVL(家族５顧客番号,0)," +
                    "         NVL(家族６顧客番号,0)," +
                    "         NVL(家族１登録日,0)," +
                    "         NVL(家族２登録日,0)," +
                    "         NVL(家族３登録日,0)," +
                    "         NVL(家族４登録日,0)," +
                    "         NVL(家族５登録日,0)," +
                    "         NVL(家族６登録日,0)," +
                    "         NVL(家族作成日,0)," +
                    "         NVL(家族ランクＵＰ金額最終更新日,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額０,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額１,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額２,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額３,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額４,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額５,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額６,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額７,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額８,0)," +
                    "         NVL(年間家族ランクＵＰ対象金額９,0)," +
                    "         NVL(月間家族ランクＵＰ金額００１,0)," +
                    "         NVL(月間家族ランクＵＰ金額００２,0)," +
                    "         NVL(月間家族ランクＵＰ金額００３,0)," +
                    "         NVL(月間家族ランクＵＰ金額００４,0)," +
                    "         NVL(月間家族ランクＵＰ金額００５,0)," +
                    "         NVL(月間家族ランクＵＰ金額００６,0)," +
                    "         NVL(月間家族ランクＵＰ金額００７,0)," +
                    "         NVL(月間家族ランクＵＰ金額００８,0)," +
                    "         NVL(月間家族ランクＵＰ金額００９,0)," +
                    "         NVL(月間家族ランクＵＰ金額０１０,0)," +
                    "         NVL(月間家族ランクＵＰ金額０１１,0)," +
                    "         NVL(月間家族ランクＵＰ金額０１２,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０１,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０２,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０３,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０４,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０５,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０６,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０７,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０８,0)," +
                    "         NVL(月間家族ランクＵＰ金額１０９,0)," +
                    "         NVL(月間家族ランクＵＰ金額１１０,0)," +
                    "         NVL(月間家族ランクＵＰ金額１１１,0)," +
                    "         NVL(月間家族ランクＵＰ金額１１２,0)," +
                    "         NVL(年次ランクコード０,0)," +
                    "         NVL(年次ランクコード１,0)," +
                    "         NVL(年次ランクコード２,0)," +
                    "         NVL(年次ランクコード３,0)," +
                    "         NVL(年次ランクコード４,0)," +
                    "         NVL(年次ランクコード５,0)," +
                    "         NVL(年次ランクコード６,0)," +
                    "         NVL(年次ランクコード７,0)," +
                    "         NVL(年次ランクコード８,0)," +
                    "         NVL(年次ランクコード９,0)," +
                    "         NVL(月次ランクコード００１,0)," +
                    "         NVL(月次ランクコード００２,0)," +
                    "         NVL(月次ランクコード００３,0)," +
                    "         NVL(月次ランクコード００４,0)," +
                    "         NVL(月次ランクコード００５,0)," +
                    "         NVL(月次ランクコード００６,0)," +
                    "         NVL(月次ランクコード００７,0)," +
                    "         NVL(月次ランクコード００８,0)," +
                    "         NVL(月次ランクコード００９,0)," +
                    "         NVL(月次ランクコード０１０,0)," +
                    "         NVL(月次ランクコード０１１,0)," +
                    "         NVL(月次ランクコード０１２,0)," +
                    "         NVL(月次ランクコード１０１,0)," +
                    "         NVL(月次ランクコード１０２,0)," +
                    "         NVL(月次ランクコード１０３,0)," +
                    "         NVL(月次ランクコード１０４,0)," +
                    "         NVL(月次ランクコード１０５,0)," +
                    "         NVL(月次ランクコード１０６,0)," +
                    "         NVL(月次ランクコード１０７,0)," +
                    "         NVL(月次ランクコード１０８,0)," +
                    "         NVL(月次ランクコード１０９,0)," +
                    "         NVL(月次ランクコード１１０,0)," +
                    "         NVL(月次ランクコード１１１,0)," +
                    "         NVL(月次ランクコード１１２,0)," +
                    "         NVL(家族削除日,0)," +
                    "         NVL(作業企業コード,0)," +
                    "         NVL(作業者ＩＤ,0)," +
                    "         NVL(作業年月日,0)," +
                    "         NVL(作業時刻,0)," +
                    "         NVL(バッチ更新日,0)," +
                    "         NVL(最終更新日,0)," +
                    "         to_number(to_char(最終更新日時,'yyyymmddhh24miss'))," +
                    "         NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "         NVL(家族１登録時刻,0),    " +
                    "         NVL(家族２登録時刻,0),    " +
                    "         NVL(家族３登録時刻,0),    " +
                    "         NVL(家族４登録時刻,0),    " +
                    "         NVL(家族５登録時刻,0),    " +
                    "         NVL(家族６登録時刻,0)     " +
                    " FROM  MS家族制度情報" +
                    " WHERE 家族ＩＤ = ?");
            sqlca.restAndExecute(h_ms_kazoku_sedo_buff.kazoku_id);
            sqlca.fetch();
            sqlca.recData(h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,                     /* 家族１顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,                     /* 家族２顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,                     /* 家族３顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,                     /* 家族４顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,                     /* 家族５顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,                     /* 家族６顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,                     /* 家族１登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,                     /* 家族２登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,                     /* 家族３登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,                     /* 家族４登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,                     /* 家族５登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,                     /* 家族６登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,                      /* 家族作成日           */
                    h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd, /* 家族ランクＵＰ金額最終更新日 */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,   /* 年間家族ランクＵＰ対象金額０ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,   /* 年間家族ランクＵＰ対象金額１ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,   /* 年間家族ランクＵＰ対象金額２ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,   /* 年間家族ランクＵＰ対象金額３ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,   /* 年間家族ランクＵＰ対象金額４ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,   /* 年間家族ランクＵＰ対象金額５ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,   /* 年間家族ランクＵＰ対象金額６ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,   /* 年間家族ランクＵＰ対象金額７ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,   /* 年間家族ランクＵＰ対象金額８ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,   /* 年間家族ランクＵＰ対象金額９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001, /* 月間家族ランクＵＰ金額００１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002, /* 月間家族ランクＵＰ金額００２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003, /* 月間家族ランクＵＰ金額００３ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004, /* 月間家族ランクＵＰ金額００４ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005, /* 月間家族ランクＵＰ金額００５ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006, /* 月間家族ランクＵＰ金額００６ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007, /* 月間家族ランクＵＰ金額００７ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008, /* 月間家族ランクＵＰ金額００８ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009, /* 月間家族ランクＵＰ金額００９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010, /* 月間家族ランクＵＰ金額０１０ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011, /* 月間家族ランクＵＰ金額０１１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012, /* 月間家族ランクＵＰ金額０１２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101, /* 月間家族ランクＵＰ金額１０１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102, /* 月間家族ランクＵＰ金額１０２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103, /* 月間家族ランクＵＰ金額１０３ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104, /* 月間家族ランクＵＰ金額１０４ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105, /* 月間家族ランクＵＰ金額１０５ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106, /* 月間家族ランクＵＰ金額１０６ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107, /* 月間家族ランクＵＰ金額１０７ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108, /* 月間家族ランクＵＰ金額１０８ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109, /* 月間家族ランクＵＰ金額１０９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110, /* 月間家族ランクＵＰ金額１１０ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111, /* 月間家族ランクＵＰ金額１１１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112, /* 月間家族ランクＵＰ金額１１２ */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_0,                         /* 年次ランクコード０     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_1,                         /* 年次ランクコード１     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_2,                         /* 年次ランクコード２     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_3,                         /* 年次ランクコード３     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_4,                         /* 年次ランクコード４     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_5,                         /* 年次ランクコード５     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_6,                         /* 年次ランクコード６     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_7,                         /* 年次ランクコード７     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_8,                         /* 年次ランクコード８     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_9,                         /* 年次ランクコード９     */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_001,                      /* 月次ランクコード００１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_002,                      /* 月次ランクコード００２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_003,                      /* 月次ランクコード００３ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_004,                      /* 月次ランクコード００４ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_005,                      /* 月次ランクコード００５ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_006,                      /* 月次ランクコード００６ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_007,                      /* 月次ランクコード００７ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_008,                      /* 月次ランクコード００８ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_009,                      /* 月次ランクコード００９ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_010,                      /* 月次ランクコード０１０ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_011,                      /* 月次ランクコード０１１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_012,                      /* 月次ランクコード０１２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_101,                      /* 月次ランクコード１０１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_102,                      /* 月次ランクコード１０２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_103,                      /* 月次ランクコード１０３ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_104,                      /* 月次ランクコード１０４ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_105,                      /* 月次ランクコード１０５ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_106,                      /* 月次ランクコード１０６ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_107,                      /* 月次ランクコード１０７ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_108,                      /* 月次ランクコード１０８ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_109,                      /* 月次ランクコード１０９ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_110,                      /* 月次ランクコード１１０ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_111,                      /* 月次ランクコード１１１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_112,                      /* 月次ランクコード１１２ */
                    h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,                       /* 家族削除日             */
                    h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,                          /* 作業企業コード         */
                    h_ms_kazoku_sedo_buff.sagyosha_id,                             /* 作業者ＩＤ             */
                    h_ms_kazoku_sedo_buff.sagyo_ymd,                               /* 作業年月日             */
                    h_ms_kazoku_sedo_buff.sagyo_hms,                               /* 作業時刻               */
                    h_ms_kazoku_sedo_buff.batch_koshin_ymd,                        /* バッチ更新日           */
                    h_ms_kazoku_sedo_buff.saishu_koshin_ymd,                       /* 最終更新日             */
                    h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,                    /* 最終更新日時           */
                    h_ms_kazoku_sedo_buff.saishu_koshin_programid,                 /* 最終更新プログラムＩＤ */
                    h_ms_kazoku_sedo_buff.kazoku_1_toroku_time,                    /* 家族１登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_2_toroku_time,                    /* 家族２登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_3_toroku_time,                    /* 家族３登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_4_toroku_time,                    /* 家族４登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_5_toroku_time,                    /* 家族５登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_6_toroku_time                     /* 家族６登録時刻         */ );

        }
        else {
            /* 顧客データベース以外に接続している場合 */

            if (DBG_LOG){
                        C_DbgMsg("getKazokuSedoInfo : %s\n", "@ CMSD");
            }

//            EXEC SQL
//            /*                  SELECT  NVL(家族親顧客番号,0),                            */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//            SELECT  NVL(家族１顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 MOD */
//            NVL(家族２顧客番号,0),
//                    NVL(家族３顧客番号,0),
//                    NVL(家族４顧客番号,0),
//                    NVL(家族５顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族６顧客番号,0),                                                                                                               /* 2022/10/14 MCCM初版 ADD */
//                    /*                          NVL(家族親登録日,0),                              */                                                                                 /* 2022/10/14 MCCM初版 DEL */
//                    NVL(家族１登録日,0),
//                    NVL(家族２登録日,0),
//                    NVL(家族３登録日,0),
//                    NVL(家族４登録日,0),
//                    NVL(家族５登録日,0),                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族６登録日,0),                                                                                                                 /* 2022/10/14 MCCM初版 ADD */
//                    NVL(家族作成日,0),
//                    NVL(家族ランクＵＰ金額最終更新日,0),
//                    NVL(年間家族ランクＵＰ対象金額０,0),
//                    NVL(年間家族ランクＵＰ対象金額１,0),
//                    NVL(年間家族ランクＵＰ対象金額２,0),
//                    NVL(年間家族ランクＵＰ対象金額３,0),
//                    NVL(年間家族ランクＵＰ対象金額４,0),
//                    NVL(年間家族ランクＵＰ対象金額５,0),
//                    NVL(年間家族ランクＵＰ対象金額６,0),
//                    NVL(年間家族ランクＵＰ対象金額７,0),
//                    NVL(年間家族ランクＵＰ対象金額８,0),
//                    NVL(年間家族ランクＵＰ対象金額９,0),
//                    NVL(月間家族ランクＵＰ金額００１,0),
//                    NVL(月間家族ランクＵＰ金額００２,0),
//                    NVL(月間家族ランクＵＰ金額００３,0),
//                    NVL(月間家族ランクＵＰ金額００４,0),
//                    NVL(月間家族ランクＵＰ金額００５,0),
//                    NVL(月間家族ランクＵＰ金額００６,0),
//                    NVL(月間家族ランクＵＰ金額００７,0),
//                    NVL(月間家族ランクＵＰ金額００８,0),
//                    NVL(月間家族ランクＵＰ金額００９,0),
//                    NVL(月間家族ランクＵＰ金額０１０,0),
//                    NVL(月間家族ランクＵＰ金額０１１,0),
//                    NVL(月間家族ランクＵＰ金額０１２,0),
//                    NVL(月間家族ランクＵＰ金額１０１,0),
//                    NVL(月間家族ランクＵＰ金額１０２,0),
//                    NVL(月間家族ランクＵＰ金額１０３,0),
//                    NVL(月間家族ランクＵＰ金額１０４,0),
//                    NVL(月間家族ランクＵＰ金額１０５,0),
//                    NVL(月間家族ランクＵＰ金額１０６,0),
//                    NVL(月間家族ランクＵＰ金額１０７,0),
//                    NVL(月間家族ランクＵＰ金額１０８,0),
//                    NVL(月間家族ランクＵＰ金額１０９,0),
//                    NVL(月間家族ランクＵＰ金額１１０,0),
//                    NVL(月間家族ランクＵＰ金額１１１,0),
//                    NVL(月間家族ランクＵＰ金額１１２,0),
//                    NVL(年次ランクコード０,0),
//                    NVL(年次ランクコード１,0),
//                    NVL(年次ランクコード２,0),
//                    NVL(年次ランクコード３,0),
//                    NVL(年次ランクコード４,0),
//                    NVL(年次ランクコード５,0),
//                    NVL(年次ランクコード６,0),
//                    NVL(年次ランクコード７,0),
//                    NVL(年次ランクコード８,0),
//                    NVL(年次ランクコード９,0),
//                    NVL(月次ランクコード００１,0),
//                    NVL(月次ランクコード００２,0),
//                    NVL(月次ランクコード００３,0),
//                    NVL(月次ランクコード００４,0),
//                    NVL(月次ランクコード００５,0),
//                    NVL(月次ランクコード００６,0),
//                    NVL(月次ランクコード００７,0),
//                    NVL(月次ランクコード００８,0),
//                    NVL(月次ランクコード００９,0),
//                    NVL(月次ランクコード０１０,0),
//                    NVL(月次ランクコード０１１,0),
//                    NVL(月次ランクコード０１２,0),
//                    NVL(月次ランクコード１０１,0),
//                    NVL(月次ランクコード１０２,0),
//                    NVL(月次ランクコード１０３,0),
//                    NVL(月次ランクコード１０４,0),
//                    NVL(月次ランクコード１０５,0),
//                    NVL(月次ランクコード１０６,0),
//                    NVL(月次ランクコード１０７,0),
//                    NVL(月次ランクコード１０８,0),
//                    NVL(月次ランクコード１０９,0),
//                    NVL(月次ランクコード１１０,0),
//                    NVL(月次ランクコード１１１,0),
//                    NVL(月次ランクコード１１２,0),
//                    NVL(家族削除日,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(最終更新日時,'yyyymmddhh24miss')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(家族１登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族２登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族３登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族４登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族５登録時刻,0),                                                                                                               /* 2022/12/19 MCCM初版 ADD */
//                    NVL(家族６登録時刻,0)                                                                                                                /* 2022/12/19 MCCM初版 ADD */
//            /*                    INTO  :h_ms_kazoku_sedo_buff.kazoku_oya_kokyaku_no,                      家族親顧客番号       */                                           /* 2022/10/14 MCCM初版 DEL */
//            INTO  :h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,                     /* 家族１顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,                     /* 家族２顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,                     /* 家族３顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,                     /* 家族４顧客番号       */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,                     /* 家族５顧客番号       */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,                     /* 家族６顧客番号       */                                           /* 2022/10/14 MCCM初版 ADD */
//            /*                          :h_ms_kazoku_sedo_buff.kazoku_oya_toroku_ymd,                      家族親登録日         */                                           /* 2022/10/14 MCCM初版 DEL */
//                            :h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,                     /* 家族１登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,                     /* 家族２登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,                     /* 家族３登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,                     /* 家族４登録日         */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,                     /* 家族５登録日         */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,                     /* 家族６登録日         */                                           /* 2022/10/14 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,                      /* 家族作成日           */
//                            :h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd, /* 家族ランクＵＰ金額最終更新日 */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,   /* 年間家族ランクＵＰ対象金額０ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,   /* 年間家族ランクＵＰ対象金額１ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,   /* 年間家族ランクＵＰ対象金額２ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,   /* 年間家族ランクＵＰ対象金額３ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,   /* 年間家族ランクＵＰ対象金額４ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,   /* 年間家族ランクＵＰ対象金額５ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,   /* 年間家族ランクＵＰ対象金額６ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,   /* 年間家族ランクＵＰ対象金額７ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,   /* 年間家族ランクＵＰ対象金額８ */
//                            :h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,   /* 年間家族ランクＵＰ対象金額９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001, /* 月間家族ランクＵＰ金額００１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002, /* 月間家族ランクＵＰ金額００２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003, /* 月間家族ランクＵＰ金額００３ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004, /* 月間家族ランクＵＰ金額００４ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005, /* 月間家族ランクＵＰ金額００５ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006, /* 月間家族ランクＵＰ金額００６ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007, /* 月間家族ランクＵＰ金額００７ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008, /* 月間家族ランクＵＰ金額００８ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009, /* 月間家族ランクＵＰ金額００９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010, /* 月間家族ランクＵＰ金額０１０ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011, /* 月間家族ランクＵＰ金額０１１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012, /* 月間家族ランクＵＰ金額０１２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101, /* 月間家族ランクＵＰ金額１０１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102, /* 月間家族ランクＵＰ金額１０２ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103, /* 月間家族ランクＵＰ金額１０３ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104, /* 月間家族ランクＵＰ金額１０４ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105, /* 月間家族ランクＵＰ金額１０５ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106, /* 月間家族ランクＵＰ金額１０６ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107, /* 月間家族ランクＵＰ金額１０７ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108, /* 月間家族ランクＵＰ金額１０８ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109, /* 月間家族ランクＵＰ金額１０９ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110, /* 月間家族ランクＵＰ金額１１０ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111, /* 月間家族ランクＵＰ金額１１１ */
//                            :h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112, /* 月間家族ランクＵＰ金額１１２ */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_0,                         /* 年次ランクコード０     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_1,                         /* 年次ランクコード１     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_2,                         /* 年次ランクコード２     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_3,                         /* 年次ランクコード３     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_4,                         /* 年次ランクコード４     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_5,                         /* 年次ランクコード５     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_6,                         /* 年次ランクコード６     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_7,                         /* 年次ランクコード７     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_8,                         /* 年次ランクコード８     */
//                            :h_ms_kazoku_sedo_buff.nenji_rank_cd_9,                         /* 年次ランクコード９     */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_001,                      /* 月次ランクコード００１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_002,                      /* 月次ランクコード００２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_003,                      /* 月次ランクコード００３ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_004,                      /* 月次ランクコード００４ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_005,                      /* 月次ランクコード００５ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_006,                      /* 月次ランクコード００６ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_007,                      /* 月次ランクコード００７ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_008,                      /* 月次ランクコード００８ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_009,                      /* 月次ランクコード００９ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_010,                      /* 月次ランクコード０１０ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_011,                      /* 月次ランクコード０１１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_012,                      /* 月次ランクコード０１２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_101,                      /* 月次ランクコード１０１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_102,                      /* 月次ランクコード１０２ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_103,                      /* 月次ランクコード１０３ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_104,                      /* 月次ランクコード１０４ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_105,                      /* 月次ランクコード１０５ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_106,                      /* 月次ランクコード１０６ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_107,                      /* 月次ランクコード１０７ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_108,                      /* 月次ランクコード１０８ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_109,                      /* 月次ランクコード１０９ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_110,                      /* 月次ランクコード１１０ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_111,                      /* 月次ランクコード１１１ */
//                            :h_ms_kazoku_sedo_buff.getuji_rank_cd_112,                      /* 月次ランクコード１１２ */
//                            :h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,                       /* 家族削除日             */
//                            :h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,                          /* 作業企業コード         */
//                            :h_ms_kazoku_sedo_buff.sagyosha_id,                             /* 作業者ＩＤ             */
//                            :h_ms_kazoku_sedo_buff.sagyo_ymd,                               /* 作業年月日             */
//                            :h_ms_kazoku_sedo_buff.sagyo_hms,                               /* 作業時刻               */
//                            :h_ms_kazoku_sedo_buff.batch_koshin_ymd,                        /* バッチ更新日           */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymd,                       /* 最終更新日             */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,                    /* 最終更新日時           */
//                            :h_ms_kazoku_sedo_buff.saishu_koshin_programid,                 /* 最終更新プログラムＩＤ */
//                            :h_ms_kazoku_sedo_buff.kazoku_1_toroku_time,                    /* 家族１登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_2_toroku_time,                    /* 家族２登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_3_toroku_time,                    /* 家族３登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_4_toroku_time,                    /* 家族４登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_5_toroku_time,                    /* 家族５登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//                            :h_ms_kazoku_sedo_buff.kazoku_6_toroku_time                     /* 家族６登録時刻         */                                         /* 2022/12/19 MCCM初版 ADD */
//            FROM  MS家族制度情報@CMSD
//            WHERE 家族ＩＤ = :h_ms_kazoku_sedo_buff.kazoku_id;

            sqlca.sql = new StringDto("SELECT  NVL(家族１顧客番号,0)," +
                    "            NVL(家族２顧客番号,0)," +
                    "                    NVL(家族３顧客番号,0)," +
                    "                    NVL(家族４顧客番号,0)," +
                    "                    NVL(家族５顧客番号,0)," +
                    "                    NVL(家族６顧客番号,0)," +
                    "                    NVL(家族１登録日,0)," +
                    "                    NVL(家族２登録日,0)," +
                    "                    NVL(家族３登録日,0)," +
                    "                    NVL(家族４登録日,0)," +
                    "                    NVL(家族５登録日,0)," +
                    "                    NVL(家族６登録日,0)," +
                    "                    NVL(家族作成日,0)," +
                    "                    NVL(家族ランクＵＰ金額最終更新日,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額０,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額１,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額２,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額３,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額４,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額５,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額６,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額７,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額８,0)," +
                    "                    NVL(年間家族ランクＵＰ対象金額９,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００１,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００２,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００３,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００４,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００５,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００６,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００７,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００８,0)," +
                    "                    NVL(月間家族ランクＵＰ金額００９,0)," +
                    "                    NVL(月間家族ランクＵＰ金額０１０,0)," +
                    "                    NVL(月間家族ランクＵＰ金額０１１,0)," +
                    "                    NVL(月間家族ランクＵＰ金額０１２,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０１,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０２,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０３,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０４,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０５,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０６,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０７,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０８,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１０９,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１１０,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１１１,0)," +
                    "                    NVL(月間家族ランクＵＰ金額１１２,0)," +
                    "                    NVL(年次ランクコード０,0)," +
                    "                    NVL(年次ランクコード１,0)," +
                    "                    NVL(年次ランクコード２,0)," +
                    "                    NVL(年次ランクコード３,0)," +
                    "                    NVL(年次ランクコード４,0)," +
                    "                    NVL(年次ランクコード５,0)," +
                    "                    NVL(年次ランクコード６,0)," +
                    "                    NVL(年次ランクコード７,0)," +
                    "                    NVL(年次ランクコード８,0)," +
                    "                    NVL(年次ランクコード９,0)," +
                    "                    NVL(月次ランクコード００１,0)," +
                    "                    NVL(月次ランクコード００２,0)," +
                    "                    NVL(月次ランクコード００３,0)," +
                    "                    NVL(月次ランクコード００４,0)," +
                    "                    NVL(月次ランクコード００５,0)," +
                    "                    NVL(月次ランクコード００６,0)," +
                    "                    NVL(月次ランクコード００７,0)," +
                    "                    NVL(月次ランクコード００８,0)," +
                    "                    NVL(月次ランクコード００９,0)," +
                    "                    NVL(月次ランクコード０１０,0)," +
                    "                    NVL(月次ランクコード０１１,0)," +
                    "                    NVL(月次ランクコード０１２,0)," +
                    "                    NVL(月次ランクコード１０１,0)," +
                    "                    NVL(月次ランクコード１０２,0)," +
                    "                    NVL(月次ランクコード１０３,0)," +
                    "                    NVL(月次ランクコード１０４,0)," +
                    "                    NVL(月次ランクコード１０５,0)," +
                    "                    NVL(月次ランクコード１０６,0)," +
                    "                    NVL(月次ランクコード１０７,0)," +
                    "                    NVL(月次ランクコード１０８,0)," +
                    "                    NVL(月次ランクコード１０９,0)," +
                    "                    NVL(月次ランクコード１１０,0)," +
                    "                    NVL(月次ランクコード１１１,0)," +
                    "                    NVL(月次ランクコード１１２,0)," +
                    "                    NVL(家族削除日,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(最終更新日時,'yyyymmddhh24miss'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(家族１登録時刻,0)," +
                    "                    NVL(家族２登録時刻,0)," +
                    "                    NVL(家族３登録時刻,0)," +
                    "                    NVL(家族４登録時刻,0)," +
                    "                    NVL(家族５登録時刻,0)," +
                    "                    NVL(家族６登録時刻,0) " +
                    "            FROM  MS家族制度情報" +
                    "            WHERE 家族ＩＤ = ?");
            sqlca.restAndExecute(h_ms_kazoku_sedo_buff.kazoku_id);
            sqlca.fetch();
            sqlca.recData(h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no,                     /* 家族１顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no,                     /* 家族２顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no,                     /* 家族３顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no,                     /* 家族４顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no,                     /* 家族５顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no,                     /* 家族６顧客番号       */
                    h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd,                     /* 家族１登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd,                     /* 家族２登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd,                     /* 家族３登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd,                     /* 家族４登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd,                     /* 家族５登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd,                     /* 家族６登録日         */
                    h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd,                      /* 家族作成日           */
                    h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd, /* 家族ランクＵＰ金額最終更新日 */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0,   /* 年間家族ランクＵＰ対象金額０ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1,   /* 年間家族ランクＵＰ対象金額１ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2,   /* 年間家族ランクＵＰ対象金額２ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3,   /* 年間家族ランクＵＰ対象金額３ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4,   /* 年間家族ランクＵＰ対象金額４ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5,   /* 年間家族ランクＵＰ対象金額５ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6,   /* 年間家族ランクＵＰ対象金額６ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7,   /* 年間家族ランクＵＰ対象金額７ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8,   /* 年間家族ランクＵＰ対象金額８ */
                    h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9,   /* 年間家族ランクＵＰ対象金額９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001, /* 月間家族ランクＵＰ金額００１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002, /* 月間家族ランクＵＰ金額００２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003, /* 月間家族ランクＵＰ金額００３ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004, /* 月間家族ランクＵＰ金額００４ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005, /* 月間家族ランクＵＰ金額００５ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006, /* 月間家族ランクＵＰ金額００６ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007, /* 月間家族ランクＵＰ金額００７ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008, /* 月間家族ランクＵＰ金額００８ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009, /* 月間家族ランクＵＰ金額００９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010, /* 月間家族ランクＵＰ金額０１０ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011, /* 月間家族ランクＵＰ金額０１１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012, /* 月間家族ランクＵＰ金額０１２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101, /* 月間家族ランクＵＰ金額１０１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102, /* 月間家族ランクＵＰ金額１０２ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103, /* 月間家族ランクＵＰ金額１０３ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104, /* 月間家族ランクＵＰ金額１０４ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105, /* 月間家族ランクＵＰ金額１０５ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106, /* 月間家族ランクＵＰ金額１０６ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107, /* 月間家族ランクＵＰ金額１０７ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108, /* 月間家族ランクＵＰ金額１０８ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109, /* 月間家族ランクＵＰ金額１０９ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110, /* 月間家族ランクＵＰ金額１１０ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111, /* 月間家族ランクＵＰ金額１１１ */
                    h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112, /* 月間家族ランクＵＰ金額１１２ */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_0,                         /* 年次ランクコード０     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_1,                         /* 年次ランクコード１     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_2,                         /* 年次ランクコード２     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_3,                         /* 年次ランクコード３     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_4,                         /* 年次ランクコード４     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_5,                         /* 年次ランクコード５     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_6,                         /* 年次ランクコード６     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_7,                         /* 年次ランクコード７     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_8,                         /* 年次ランクコード８     */
                    h_ms_kazoku_sedo_buff.nenji_rank_cd_9,                         /* 年次ランクコード９     */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_001,                      /* 月次ランクコード００１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_002,                      /* 月次ランクコード００２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_003,                      /* 月次ランクコード００３ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_004,                      /* 月次ランクコード００４ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_005,                      /* 月次ランクコード００５ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_006,                      /* 月次ランクコード００６ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_007,                      /* 月次ランクコード００７ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_008,                      /* 月次ランクコード００８ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_009,                      /* 月次ランクコード００９ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_010,                      /* 月次ランクコード０１０ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_011,                      /* 月次ランクコード０１１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_012,                      /* 月次ランクコード０１２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_101,                      /* 月次ランクコード１０１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_102,                      /* 月次ランクコード１０２ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_103,                      /* 月次ランクコード１０３ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_104,                      /* 月次ランクコード１０４ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_105,                      /* 月次ランクコード１０５ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_106,                      /* 月次ランクコード１０６ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_107,                      /* 月次ランクコード１０７ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_108,                      /* 月次ランクコード１０８ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_109,                      /* 月次ランクコード１０９ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_110,                      /* 月次ランクコード１１０ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_111,                      /* 月次ランクコード１１１ */
                    h_ms_kazoku_sedo_buff.getuji_rank_cd_112,                      /* 月次ランクコード１１２ */
                    h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd,                       /* 家族削除日             */
                    h_ms_kazoku_sedo_buff.sagyo_kigyo_cd,                          /* 作業企業コード         */
                    h_ms_kazoku_sedo_buff.sagyosha_id,                             /* 作業者ＩＤ             */
                    h_ms_kazoku_sedo_buff.sagyo_ymd,                               /* 作業年月日             */
                    h_ms_kazoku_sedo_buff.sagyo_hms,                               /* 作業時刻               */
                    h_ms_kazoku_sedo_buff.batch_koshin_ymd,                        /* バッチ更新日           */
                    h_ms_kazoku_sedo_buff.saishu_koshin_ymd,                       /* 最終更新日             */
                    h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms,                    /* 最終更新日時           */
                    h_ms_kazoku_sedo_buff.saishu_koshin_programid,                 /* 最終更新プログラムＩＤ */
                    h_ms_kazoku_sedo_buff.kazoku_1_toroku_time,                    /* 家族１登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_2_toroku_time,                    /* 家族２登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_3_toroku_time,                    /* 家族３登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_4_toroku_time,                    /* 家族４登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_5_toroku_time,                    /* 家族５登録時刻         */
                    h_ms_kazoku_sedo_buff.kazoku_6_toroku_time                     /* 家族６登録時刻         */  );
        }

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "家族ＩＤ=%s",                                                                                                           /* 2022/10/14 MCCM初版 MOD */
                    h_ms_kazoku_sedo_buff.kazoku_id.arr);                                                                                                   /* 2022/10/14 MCCM初版 MOD */
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MS家族制度情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        /* 出力引数の設定 */
//      strcpy((char *)ksMaster->kazoku_oya_kokyaku_no.arr, (char *)h_ms_kazoku_sedo_buff.kazoku_oya_kokyaku_no.arr);                                            /* 2022/10/14 MCCM初版 DEL */
//      ksMaster->kazoku_oya_kokyaku_no.len = strlen((char *)ksMaster->kazoku_oya_kokyaku_no.arr);  /* 家族親顧客番号 */                                         /* 2022/10/14 MCCM初版 DEL */
        strcpy(ksMaster.kazoku_1_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_1_kokyaku_no);
        ksMaster.kazoku_1_kokyaku_no.len = strlen(ksMaster.kazoku_1_kokyaku_no);      /* 家族１顧客番号 */
        strcpy(ksMaster.kazoku_2_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_2_kokyaku_no);
        ksMaster.kazoku_2_kokyaku_no.len = strlen(ksMaster.kazoku_2_kokyaku_no);      /* 家族２顧客番号 */
        strcpy(ksMaster.kazoku_3_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_3_kokyaku_no);
        ksMaster.kazoku_3_kokyaku_no.len = strlen(ksMaster.kazoku_3_kokyaku_no);      /* 家族３顧客番号 */
        strcpy(ksMaster.kazoku_4_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_4_kokyaku_no);
        ksMaster.kazoku_4_kokyaku_no.len = strlen(ksMaster.kazoku_4_kokyaku_no);      /* 家族４顧客番号 */
        strcpy(ksMaster.kazoku_5_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_5_kokyaku_no);                                                /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_5_kokyaku_no.len = strlen(ksMaster.kazoku_5_kokyaku_no);      /* 家族５顧客番号 */                                         /* 2022/10/14 MCCM初版 ADD */
        strcpy(ksMaster.kazoku_6_kokyaku_no, h_ms_kazoku_sedo_buff.kazoku_6_kokyaku_no);                                                /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_6_kokyaku_no.len = strlen(ksMaster.kazoku_6_kokyaku_no);      /* 家族６顧客番号 */                                         /* 2022/10/14 MCCM初版 ADD */

//      ksMaster->kazoku_oya_toroku_ymd = h_ms_kazoku_sedo_buff.kazoku_oya_toroku_ymd;    /* 家族親登録日         */                                             /* 2022/10/14 MCCM初版 DEL */
        ksMaster.kazoku_1_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_1_toroku_ymd;      /* 家族１登録日         */
        ksMaster.kazoku_2_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_2_toroku_ymd;      /* 家族２登録日         */
        ksMaster.kazoku_3_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_3_toroku_ymd;      /* 家族３登録日         */
        ksMaster.kazoku_4_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_4_toroku_ymd;      /* 家族４登録日         */
        ksMaster.kazoku_5_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_5_toroku_ymd;      /* 家族５登録日         */                                             /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_6_toroku_ymd   = h_ms_kazoku_sedo_buff.kazoku_6_toroku_ymd;      /* 家族６登録日         */                                             /* 2022/10/14 MCCM初版 ADD */
        ksMaster.kazoku_sakusei_ymd    = h_ms_kazoku_sedo_buff.kazoku_sakusei_ymd;       /* 家族作成日           */

        ksMaster.kazoku_1_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_1_toroku_time;    /* 家族１登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_2_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_2_toroku_time;    /* 家族２登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_3_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_3_toroku_time;    /* 家族３登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_4_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_4_toroku_time;    /* 家族４登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_5_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_5_toroku_time;    /* 家族５登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */
        ksMaster.kazoku_6_toroku_time  = h_ms_kazoku_sedo_buff.kazoku_6_toroku_time;    /* 家族６登録時刻       */                                               /* 2022/12/19 MCCM初版 ADD */

        ksMaster.kazoku_rankup_kingaku_saishu_koshin_ymd = h_ms_kazoku_sedo_buff.kazoku_rankup_kingaku_saishu_koshin_ymd; /* 家族ランクＵＰ金額最終更新日 */

        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_0   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_0;   /* 年間家族ランクＵＰ対象金額０ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_1   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_1;   /* 年間家族ランクＵＰ対象金額１ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_2   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_2;   /* 年間家族ランクＵＰ対象金額２ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_3   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_3;   /* 年間家族ランクＵＰ対象金額３ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_4   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_4;   /* 年間家族ランクＵＰ対象金額４ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_5   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_5;   /* 年間家族ランクＵＰ対象金額５ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_6   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_6;   /* 年間家族ランクＵＰ対象金額６ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_7   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_7;   /* 年間家族ランクＵＰ対象金額７ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_8   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_8;   /* 年間家族ランクＵＰ対象金額８ */
        ksMaster.nenkan_kazoku_rankup_taisho_kingaku_9   = h_ms_kazoku_sedo_buff.nenkan_kazoku_rankup_taisho_kingaku_9;   /* 年間家族ランクＵＰ対象金額９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_001 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_001; /* 月間家族ランクＵＰ金額００１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_002 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_002; /* 月間家族ランクＵＰ金額００２ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_003 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_003; /* 月間家族ランクＵＰ金額００３ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_004 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_004; /* 月間家族ランクＵＰ金額００４ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_005 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_005; /* 月間家族ランクＵＰ金額００５ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_006 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_006; /* 月間家族ランクＵＰ金額００６ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_007 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_007; /* 月間家族ランクＵＰ金額００７ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_008 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_008; /* 月間家族ランクＵＰ金額００８ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_009 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_009; /* 月間家族ランクＵＰ金額００９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_010 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_010; /* 月間家族ランクＵＰ金額０１０ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_011 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_011; /* 月間家族ランクＵＰ金額０１１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_012 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_012; /* 月間家族ランクＵＰ金額０１２ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_101 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_101; /* 月間家族ランクＵＰ金額１０１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_102 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_102; /* 月間家族ランクＵＰ金額１０２ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_103 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_103; /* 月間家族ランクＵＰ金額１０３ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_104 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_104; /* 月間家族ランクＵＰ金額１０４ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_105 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_105; /* 月間家族ランクＵＰ金額１０５ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_106 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_106; /* 月間家族ランクＵＰ金額１０６ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_107 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_107; /* 月間家族ランクＵＰ金額１０７ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_108 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_108; /* 月間家族ランクＵＰ金額１０８ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_109 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_109; /* 月間家族ランクＵＰ金額１０９ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_110 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_110; /* 月間家族ランクＵＰ金額１１０ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_111 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_111; /* 月間家族ランクＵＰ金額１１１ */
        ksMaster.gekkan_kazoku_rankup_taisho_kingaku_112 = h_ms_kazoku_sedo_buff.gekkan_kazoku_rankup_taisho_kingaku_112; /* 月間家族ランクＵＰ金額１１２ */

                ksMaster.nenji_rank_cd_0    = h_ms_kazoku_sedo_buff.nenji_rank_cd_0;             /* 年次ランクコード０     */
                ksMaster.nenji_rank_cd_1    = h_ms_kazoku_sedo_buff.nenji_rank_cd_1;             /* 年次ランクコード１     */
                ksMaster.nenji_rank_cd_2    = h_ms_kazoku_sedo_buff.nenji_rank_cd_2;             /* 年次ランクコード２     */
                ksMaster.nenji_rank_cd_3    = h_ms_kazoku_sedo_buff.nenji_rank_cd_3;             /* 年次ランクコード３     */
                ksMaster.nenji_rank_cd_4    = h_ms_kazoku_sedo_buff.nenji_rank_cd_4;             /* 年次ランクコード４     */
                ksMaster.nenji_rank_cd_5    = h_ms_kazoku_sedo_buff.nenji_rank_cd_5;             /* 年次ランクコード５     */
                ksMaster.nenji_rank_cd_6    = h_ms_kazoku_sedo_buff.nenji_rank_cd_6;             /* 年次ランクコード６     */
                ksMaster.nenji_rank_cd_7    = h_ms_kazoku_sedo_buff.nenji_rank_cd_7;             /* 年次ランクコード７     */
                ksMaster.nenji_rank_cd_8    = h_ms_kazoku_sedo_buff.nenji_rank_cd_8;             /* 年次ランクコード８     */
                ksMaster.nenji_rank_cd_9    = h_ms_kazoku_sedo_buff.nenji_rank_cd_9;             /* 年次ランクコード９     */
                ksMaster.getuji_rank_cd_001 = h_ms_kazoku_sedo_buff.getuji_rank_cd_001;          /* 月次ランクコード００１ */
                ksMaster.getuji_rank_cd_002 = h_ms_kazoku_sedo_buff.getuji_rank_cd_002;          /* 月次ランクコード００２ */
                ksMaster.getuji_rank_cd_003 = h_ms_kazoku_sedo_buff.getuji_rank_cd_003;          /* 月次ランクコード００３ */
                ksMaster.getuji_rank_cd_004 = h_ms_kazoku_sedo_buff.getuji_rank_cd_004;          /* 月次ランクコード００４ */
                ksMaster.getuji_rank_cd_005 = h_ms_kazoku_sedo_buff.getuji_rank_cd_005;          /* 月次ランクコード００５ */
                ksMaster.getuji_rank_cd_006 = h_ms_kazoku_sedo_buff.getuji_rank_cd_006;          /* 月次ランクコード００６ */
                ksMaster.getuji_rank_cd_007 = h_ms_kazoku_sedo_buff.getuji_rank_cd_007;          /* 月次ランクコード００７ */
                ksMaster.getuji_rank_cd_008 = h_ms_kazoku_sedo_buff.getuji_rank_cd_008;          /* 月次ランクコード００８ */
                ksMaster.getuji_rank_cd_009 = h_ms_kazoku_sedo_buff.getuji_rank_cd_009;          /* 月次ランクコード００９ */
                ksMaster.getuji_rank_cd_010 = h_ms_kazoku_sedo_buff.getuji_rank_cd_010;          /* 月次ランクコード０１０ */
                ksMaster.getuji_rank_cd_011 = h_ms_kazoku_sedo_buff.getuji_rank_cd_011;          /* 月次ランクコード０１１ */
                ksMaster.getuji_rank_cd_012 = h_ms_kazoku_sedo_buff.getuji_rank_cd_012;          /* 月次ランクコード０１２ */
                ksMaster.getuji_rank_cd_101 = h_ms_kazoku_sedo_buff.getuji_rank_cd_101;          /* 月次ランクコード１０１ */
                ksMaster.getuji_rank_cd_102 = h_ms_kazoku_sedo_buff.getuji_rank_cd_102;          /* 月次ランクコード１０２ */
                ksMaster.getuji_rank_cd_103 = h_ms_kazoku_sedo_buff.getuji_rank_cd_103;          /* 月次ランクコード１０３ */
                ksMaster.getuji_rank_cd_104 = h_ms_kazoku_sedo_buff.getuji_rank_cd_104;          /* 月次ランクコード１０４ */
                ksMaster.getuji_rank_cd_105 = h_ms_kazoku_sedo_buff.getuji_rank_cd_105;          /* 月次ランクコード１０５ */
                ksMaster.getuji_rank_cd_106 = h_ms_kazoku_sedo_buff.getuji_rank_cd_106;          /* 月次ランクコード１０６ */
                ksMaster.getuji_rank_cd_107 = h_ms_kazoku_sedo_buff.getuji_rank_cd_107;          /* 月次ランクコード１０７ */
                ksMaster.getuji_rank_cd_108 = h_ms_kazoku_sedo_buff.getuji_rank_cd_108;          /* 月次ランクコード１０８ */
                ksMaster.getuji_rank_cd_109 = h_ms_kazoku_sedo_buff.getuji_rank_cd_109;          /* 月次ランクコード１０９ */
                ksMaster.getuji_rank_cd_110 = h_ms_kazoku_sedo_buff.getuji_rank_cd_110;          /* 月次ランクコード１１０ */
                ksMaster.getuji_rank_cd_111 = h_ms_kazoku_sedo_buff.getuji_rank_cd_111;          /* 月次ランクコード１１１ */
                ksMaster.getuji_rank_cd_112 = h_ms_kazoku_sedo_buff.getuji_rank_cd_112;          /* 月次ランクコード１１２ */

                ksMaster.kazoku_sakujo_ymd  = h_ms_kazoku_sedo_buff.kazoku_sakujo_ymd;           /* 家族削除日             */

        ksMaster.sagyo_kigyo_cd       = h_ms_kazoku_sedo_buff.sagyo_kigyo_cd;            /* 作業企業コード         */
        ksMaster.sagyosha_id          = h_ms_kazoku_sedo_buff.sagyosha_id;               /* 作業者ＩＤ             */
        ksMaster.sagyo_ymd            = h_ms_kazoku_sedo_buff.sagyo_ymd;                 /* 作業年月日             */
        ksMaster.sagyo_hms            = h_ms_kazoku_sedo_buff.sagyo_hms;                 /* 作業時刻               */
        ksMaster.batch_koshin_ymd     = h_ms_kazoku_sedo_buff.batch_koshin_ymd;          /* バッチ更新日           */
        ksMaster.saishu_koshin_ymd    = h_ms_kazoku_sedo_buff.saishu_koshin_ymd;         /* 最終更新日             */
        ksMaster.saishu_koshin_ymdhms = h_ms_kazoku_sedo_buff.saishu_koshin_ymdhms;      /* 最終更新日時           */
        strcpy(ksMaster.saishu_koshin_programid,                                         /* 最終更新プログラムＩＤ */
                h_ms_kazoku_sedo_buff.saishu_koshin_programid);

        /* 戻り値の設定 */

        if (DBG_LOG){
                C_DbgMsg("getKazokuSedoInfo : %s\n", "end");
        }

            status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----getKazokuSedoInfo Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： setRankInfo                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  setRankInfo()                                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ランク情報取得処理                                                     */
    /*     MSランク別ボーナスポイント情報を全件取得し保持する                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */
    /******************************************************************************/
    public int  setRankInfo()
    {
        int     loop_cnt;
        int     rank_cnt;

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("setRankInfo処理");
                /*------------------------------------------------------------*/
        }

        /* 初期化 */
        for ( loop_cnt=0; loop_cnt<C_RANK_MAX; loop_cnt++ ) {
            sRankbuf[loop_cnt] = new SRankbuf();
            sRankbuf[loop_cnt].rank_shubetsu_buf   .arr= 0;    /* ランク種別          */
            sRankbuf[loop_cnt].hitsuyo_kingaku_buf .arr= 0;    /* 必要金額            */
            sRankbuf[loop_cnt].rank_cd_buf         .arr= 0;    /* ランクコード        */
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 CURSOR MSRK01 [%s]\n", "DECLARE");
                /*------------------------------------------------------------*/
        }
        /* カーソル定義 */
//        EXEC SQL DECLARE KYUM_MSRK01 CURSOR FOR
//        SELECT DISTINCT
//        ランク種別,
//                必要金額,
//                ランクコード
//        FROM   MSランク別ボーナスポイント情報@CMSD
//            WHERE  (ランク種別 = 1 OR ランク種別 = 2)
//        ORDER BY ランク種別,必要金額;
        StringDto wk_sql = new StringDto();
        sprintf(wk_sql, "SELECT DISTINCT ランク種別, 必要金額,ランクコード" +
                        " FROM   MSランク別ボーナスポイント情報" +
                        " WHERE  (ランク種別 = 1 OR ランク種別 = 2)" +
                        " ORDER BY ランク種別,必要金額");
        SqlstmDto sqlca = sqlcaManager.get("KYUM_MSRK01");
        sqlca.sql = wk_sql;
        sqlca.declare();

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 CURSOR MSRK01 [%s]\n", "OPEN");
                /*------------------------------------------------------------*/
        }
        /* カーソルオープン */
//        EXEC SQL OPEN KYUM_MSRK01;
        sqlca.open();

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 OPEN sqlcode=[%d]\n", sqlca.sqlcode);
                /*------------------------------------------------------------*/
        }

        if ( sqlca.sqlcode != C_const_Ora_OK ) {
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "MSランク別ボーナスポイント情報", "全件検索", 0, 0);
            return C_const_NG;
        }

        rank_cnt = 0;
        while( true ) {
            /* 初期化 */
            msranki_t=new MS_RANKBETSU_POINT_INFO_TBL();
            msranki_t.rank_shubetsu  .arr= 0;
            msranki_t.hitsuyo_kingaku.arr= 0;
            msranki_t.rank_cd        .arr= 0;

            /* カーソルフェッチ */
//            EXEC SQL FETCH KYUM_MSRK01
//            INTO :msranki_t.rank_shubetsu,
//             :msranki_t.hitsuyo_kingaku,
//             :msranki_t.rank_cd;
            sqlca.fetchInto(msranki_t.rank_shubetsu, msranki_t.hitsuyo_kingaku, msranki_t.rank_cd);

            /* データ無し以外のエラーの場合処理を異常終了する */
            if ( sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND ) {
                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 FETCH NG sqlcode=[%d]\n",
                                        sqlca.sqlcode);
                                /*------------------------------------------------------------*/
                }
                APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                        "MSランク別ボーナスポイント情報", "全件検索", 0, 0);
//                EXEC SQL CLOSE KYUM_MSRK01; /* カーソルクローズ                   */
                sqlcaManager.close(sqlca);
                return C_const_NG;
            }

            /* データ無し */
            if ( sqlca.sqlcode == C_const_Ora_NOTFOUND ) {
                if (DBG_LOG){
                                /*------------------------------------------------------------*/
                                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 FETCH NOTFOUND=[%d]\n",
                                        sqlca.sqlcode);
                                /*------------------------------------------------------------*/
                }
                break;
            }

            if (DBG_LOG){
                        /*------------------------------------------------------------*/
                        C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 ランク種別=[%d]\n",
                                msranki_t.rank_shubetsu);
                        C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 必要金額    =[%d]\n",
                                msranki_t.hitsuyo_kingaku);
                        C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 ランクコード=[%d]\n",
                                msranki_t.rank_cd);
                        /*------------------------------------------------------------*/
            }
            sRankbuf[rank_cnt].rank_shubetsu_buf   = msranki_t.rank_shubetsu;
            sRankbuf[rank_cnt].rank_cd_buf         = msranki_t.rank_cd;
            sRankbuf[rank_cnt].hitsuyo_kingaku_buf = msranki_t.hitsuyo_kingaku;

            rank_cnt++;
        }

        gl_rank_cnt = rank_cnt;         /* ランク情報メモリ持ちする件数保持   */

        if (DBG_LOG){
                /* 保持した内容を出力 */
                C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 件数=[%d] \n", gl_rank_cnt);
                for ( loop_cnt=0; loop_cnt<rank_cnt; loop_cnt++ ) {
                    /*------------------------------------------------------------*/
                    C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 [%d] 配列目\n", loop_cnt);
                    C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 必要金額    =[%d]\n",
                            sRankbuf[loop_cnt].hitsuyo_kingaku_buf);
                    C_DbgMsg("*** setRankInfo *** MSランク別ボーナスポイント情報 ランクコード=[%d]\n",
                            sRankbuf[loop_cnt].rank_cd_buf);
                    /*------------------------------------------------------------*/
                }
        }

//        EXEC SQL CLOSE KYUM_MSRK01;         /* カーソルクローズ                   */
        sqlcaManager.close(sqlca);

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("setRankInfo処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return C_const_OK;          /* 正常終了 */
        /*-----setRankInfo Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： getMaxRank                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  getMaxRank(double kingaku, int p_shubetsu, int *rank_cd)    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     MAXランク取得処理                                                      */
    /*     引数の金額を条件にしてMAXのランクを引数にセットして返す                */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              double                  kingaku   金額                        */
    /*              int                     shubetsu  種別 月(1)／年(2)           */
    /*              int             *       rank_cd : ランク（MAXを格納)          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*                                                                            */
    /******************************************************************************/
    public int getMaxRank(double p_kingaku, int p_shubetsu, IntegerDto p_rank_cd)
    {
        int i;
        int wk_rank_cd;

        wk_rank_cd = 1;
        for (i = 0; i < gl_rank_cnt; i++) {
            if (( sRankbuf[i].rank_shubetsu_buf .intVal()  == p_shubetsu )  && /* 引数：月(1)／年(2) 区分に準じて */
                    ( sRankbuf[i].hitsuyo_kingaku_buf.intVal() <= p_kingaku )   && /* 引数_金額の方が大きい           */
                    ( wk_rank_cd < sRankbuf[i].rank_cd_buf.intVal() ))  {          /* より大きいランクコードを設定    */
                wk_rank_cd = sRankbuf[i].rank_cd_buf.intVal();
            }
        }

        p_rank_cd.arr = wk_rank_cd; /* 最大値をセットする */

        return C_const_OK;

        /*-----getMaxRank Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： getRankUpMoney                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  getRankUpMoney()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ランクＵＰ情報取得処理                                                 */
    /*     TSポイント年別情報からMS家族制度情報(退会用)の                         */
    /*     家族ランクＵＰ情報を設定する                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */
    /******************************************************************************/
    public int  getRankUpMoney()
    {
        IntegerDto       rtn_status = new IntegerDto();                  /* 関数結果ステータス           */
        int       rtn_cd;                      /* 関数戻り値                   */

        StringDto      out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット */

        int       wi_idx;                      /* ＬＯＯＰ処理用               */
        IntegerDto       wk_rank = new IntegerDto();                     /* ランクコード                 */

        /* 2023/01/09 MCCM初版 ADD END */
//    int       i_shubetsu_tuki = 1;         /* ランク種別(月) */
//    int       i_shubetsu_nen  = 2;         /* ランク種別(年) */
        int       i_shubetsu_tuki = 2;         /* ランク種別(月) */
        int       i_shubetsu_nen  = 1;         /* ランク種別(年) */
        /* 2023/01/09 MCCM初版 ADD END */

        RANK_UP_MONEY wstr_RankUpMoneybuf = null; /* 年月ランクＵＰ金額   */

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgStart("getRankUpMoney処理");
                /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------------*/
        /* TSポイント年別情報(前日当年)から「ランクＵＰ対象金額」を取得する */
        /*------------------------------------------------------------------*/
        /* 2022/12/12 MCCM初版 DEL START */
        //memset(&tsyearpoint_t, 0x00, sizeof(TS_YEAR_POINT_TBL));
        /* 引数 */
        //tsyearpoint_t.year = gstr_yesterday.int_yyyy;
        //strcpy((char *)tsyearpoint_t.kokyaku_no.arr, (char *)mmkokgb_t.kokyaku_no.arr);
        //tsyearpoint_t.kokyaku_no.len = mmkokgb_t.kokyaku_no.len;
        /* ポイント年別情報取得 */
        //rtn_cd = C_GetYearPoint(&tsyearpoint_t, gstr_yesterday.int_yyyy, rtn_status);
        /* 2022/12/12 MCCM初版 DEL END */

        /* 2022/12/12 MCCM初版 ADD START */
        h_ts_rank_info_data = new TS_RANK_INFO_TBL();
        memset(h_ts_rank_info_data, 0x00, 0);
        strcpy(h_ts_rank_info_data.kokyaku_no, mmkokgb_t.kokyaku_no);
        h_ts_rank_info_data.kokyaku_no.len = mmkokgb_t.kokyaku_no.len;

        /* TSランク情報取得 */
        rtn_cd = GetTsRankInfo(h_ts_rank_info_data, rtn_status);
        /* 2022/12/12 MCCM初版 ADD END */
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo ret=%d\n", rtn_cd);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo sts=%d\n", rtn_status);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 対象年=%d\n", gstr_yesterday.int_yyyy);
                /*------------------------------------------------------------*/
        }

        if ( rtn_cd == C_const_NG ) {
            /* error */
            APLOG_WT("903", 0, null, "GetTsRankInfo", rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額０=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額１=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額２=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額３=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額４=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額５=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額６=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額７=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額８=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8)  ;
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額９=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9)  ;

                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００４=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００５=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００６=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００７=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００８=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００９=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１０=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００３=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003);

                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０４=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０５=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０６=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０７=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０８=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０９=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１０=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102);
                C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０３=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103);
                /*------------------------------------------------------------*/
        }

        ItemDto nenkan_rankup_taisho_kingaku_yesterday[] = {
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9 };

        /*--- 年月ランクＵＰ金額設定（初期化０クリア） ---*/
        wstr_RankUpMoneybuf = new RANK_UP_MONEY();
        wstr_RankUpMoneybuf.memset();
        for (wi_idx = 0; wi_idx < 10; wi_idx++) {
            wstr_RankUpMoneybuf.year_buf[wi_idx].arr = 0;
        }
        for (wi_idx = 0; wi_idx < 12; wi_idx++) {
            wstr_RankUpMoneybuf.month0_buf[wi_idx].arr = 0;
            wstr_RankUpMoneybuf.month1_buf[wi_idx].arr = 0;
        }
        /*--- 年月ランクＵＰ金額設定（ポイント年別情報:前日当年） ---*/
        /* 年ランクＵＰ金額設定 */
        if (rtn_cd == C_const_OK) {   /* 取得情報あり */
            wstr_RankUpMoneybuf.year_buf[gstr_yesterday.int_y_bottom.intVal()].arr = nenkan_rankup_taisho_kingaku_yesterday[gstr_yesterday.int_y_bottom.intVal()];
        }
        /* 月ランクＵＰ金額設定 前日（当月：翌月ランク対象） */
        if ((( gstr_yesterday.int_y_bottom.intVal() % 2) == 0 ) &&  /* 偶数年 */
                ( rtn_cd == C_const_OK ))                 {   /* 取得情報あり */
            if ((gstr_yesterday.int_mm.intVal() ==1 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 1 )) {
                wstr_RankUpMoneybuf.month0_buf[0]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001 ;
            }
            if ((gstr_yesterday.int_mm.intVal() ==2 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 2 )) {
                wstr_RankUpMoneybuf.month0_buf[1]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002;
            }
            if ((gstr_yesterday.int_mm.intVal() ==3 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 3 )) {
                wstr_RankUpMoneybuf.month0_buf[2]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003;
            }
            if ((gstr_yesterday.int_mm.intVal() ==4 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 4 )) {
                wstr_RankUpMoneybuf.month0_buf[3]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004;
            }
            if ((gstr_yesterday.int_mm.intVal() ==5 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 5 )) {
                wstr_RankUpMoneybuf.month0_buf[4]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005;
            }
            if ((gstr_yesterday.int_mm.intVal() ==6 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 6 )) {
                wstr_RankUpMoneybuf.month0_buf[5]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006;
            }
            if ((gstr_yesterday.int_mm.intVal() ==7 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 7 )) {
                wstr_RankUpMoneybuf.month0_buf[6]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007;
            }
            if ((gstr_yesterday.int_mm.intVal() ==8 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 8 )) {
                wstr_RankUpMoneybuf.month0_buf[7]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008;
            }
            if ((gstr_yesterday.int_mm.intVal() ==9 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 9 )) {
                wstr_RankUpMoneybuf.month0_buf[8]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009;
            }
            if ((gstr_yesterday.int_mm.intVal() ==10 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 10 )) {
                wstr_RankUpMoneybuf.month0_buf[9]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010;
            }
            if ((gstr_yesterday.int_mm.intVal() ==11 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 11 )) {
                wstr_RankUpMoneybuf.month0_buf[10] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011;
            }
            if ((gstr_yesterday.int_mm.intVal() ==12 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 12 )) {
                wstr_RankUpMoneybuf.month0_buf[11] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012;
            }
        }
        if ((( gstr_yesterday.int_y_bottom.intVal() % 2) == 1 ) &&  /* 奇数年 */
                ( rtn_cd == C_const_OK ))                 {   /* 取得情報あり */
            if ((gstr_yesterday.int_mm.intVal() ==1 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 1 )) {
                wstr_RankUpMoneybuf.month1_buf[0]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101;
            }
            if ((gstr_yesterday.int_mm.intVal() ==2 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 2 )) {
                wstr_RankUpMoneybuf.month1_buf[1]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102;
            }
            if ((gstr_yesterday.int_mm.intVal() ==3 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 3 )) {
                wstr_RankUpMoneybuf.month1_buf[2]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103;
            }
            if ((gstr_yesterday.int_mm.intVal() ==4 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 4 )) {
                wstr_RankUpMoneybuf.month1_buf[3]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104;
            }
            if ((gstr_yesterday.int_mm.intVal() ==5 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 5 )) {
                wstr_RankUpMoneybuf.month1_buf[4]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105;
            }
            if ((gstr_yesterday.int_mm.intVal() ==6 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 6 )) {
                wstr_RankUpMoneybuf.month1_buf[5]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106;
            }
            if ((gstr_yesterday.int_mm.intVal() ==7 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 7 )) {
                wstr_RankUpMoneybuf.month1_buf[6]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107;
            }
            if ((gstr_yesterday.int_mm.intVal() ==8 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 8 )) {
                wstr_RankUpMoneybuf.month1_buf[7]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108;
            }
            if ((gstr_yesterday.int_mm.intVal() ==9 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 9 )) {
                wstr_RankUpMoneybuf.month1_buf[8]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109;
            }
            if ((gstr_yesterday.int_mm.intVal() ==10 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 10 )) {
                wstr_RankUpMoneybuf.month1_buf[9]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110;
            }
            if ((gstr_yesterday.int_mm.intVal() ==11 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 11 )) {
                wstr_RankUpMoneybuf.month1_buf[10] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111;
            }
            if ((gstr_yesterday.int_mm.intVal() ==12 ) ||
                    (gstr_lastmonth.int_yyyy == gstr_yesterday.int_yyyy && gstr_lastmonth.int_mm.intVal() == 12 )) {
                wstr_RankUpMoneybuf.month1_buf[11] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112;
            }
        }

        /*------------------------------------------------------------------*/
        /* TSポイント年別情報(前日前年)から「ランクＵＰ対象金額」を取得する */
        /*------------------------------------------------------------------*/
        /* 2022/12/12 MCCM初版 DEL START */
        //memset(&h_ts_rank_info_data, 0x00, sizeof(TS_YEAR_POINT_TBL));
        /* 引数 */
        //h_ts_rank_info_data.year = gstr_lastyear.int_yyyy;
        //strcpy((char *)h_ts_rank_info_data.kokyaku_no.arr, (char *)mmkokgb_t.kokyaku_no.arr);
        //h_ts_rank_info_data.kokyaku_no.len = mmkokgb_t.kokyaku_no.len;
        /* ポイント年別情報取得 */
        //rtn_cd = C_GetYearPoint(&h_ts_rank_info_data, gstr_lastyear.int_yyyy, rtn_status);
        /* 2022/12/12 MCCM初版 DEL END */

        /* 2022/12/12 MCCM初版 ADD START */
        memset(h_ts_rank_info_data, 0x00, sizeof(h_ts_rank_info_data));
        strcpy(h_ts_rank_info_data.kokyaku_no, mmkokgb_t.kokyaku_no);
        h_ts_rank_info_data.kokyaku_no.len = mmkokgb_t.kokyaku_no.len;

        /* TSランク情報取得 */
        rtn_cd = GetTsRankInfo(h_ts_rank_info_data, rtn_status);
        /* 2022/12/12 MCCM初版 ADD END */
        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo ret=%d\n", rtn_cd);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo sts=%d\n", rtn_status);
                C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 対象年=%d\n", gstr_lastyear.int_yyyy);
                /*------------------------------------------------------------*/
        }

        if (rtn_cd == C_const_NG) {
            /* error */
            APLOG_WT("903", 0, null, "GetTsRankInfo", rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

if (DBG_LOG){
        /*------------------------------------------------------------*/
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額０=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額１=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額２=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額３=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額４=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額５=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額６=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額７=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額８=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8)  ;
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 年間ランクＵＰ対象金額９=%f\n"    , h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9)  ;

        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００４=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００５=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００６=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００７=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００８=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００９=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１０=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額０１２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002);
        C_DbgMsg("*** getRankUpMoney *** GetTsRankInfo 月間ランクＵＰ対象金額００３=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003);

        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０４=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０５=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０６=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０７=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０８=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０９=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１０=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１１２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０１=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０２=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102);
        C_DbgMsg("*** getRankUpMoney *** C_GetYearPoint 月間ランクＵＰ対象金額１０３=%f\n", h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103);
        /*------------------------------------------------------------*/
}

        ItemDto nenkan_rankup_taisho_kingaku_lastyear[] = {h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_0,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_1,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_2,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_3,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_4,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_5,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_6,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_7,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_8,
                h_ts_rank_info_data.nenkan_rankup_taisho_kingaku_9 };

        /*--- 年月ランクＵＰ金額設定（ポイント年別情報:前日前年） ---*/
        /* 年ランクＵＰ金額設定 */
        if (rtn_cd == C_const_OK) {   /* 取得情報あり */
            wstr_RankUpMoneybuf.year_buf[gstr_lastyear.int_y_bottom.intVal()] = nenkan_rankup_taisho_kingaku_lastyear[gstr_lastyear.int_y_bottom.intVal()];
        }
        /* 月ランクＵＰ金額設定 */
        if (gstr_lastmonth.int_yyyy != gstr_yesterday.int_yyyy){
            if ((( gstr_lastmonth.int_y_bottom.intVal() % 2) == 0 ) &&  /* 偶数年 */
                    ( rtn_cd == C_const_OK ))                 {   /* 取得情報あり */
                if (gstr_lastmonth.int_mm.intVal() ==1 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[0]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_001;
                }
                if (gstr_lastmonth.int_mm.intVal() ==2 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[1]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_002;
                }
                if (gstr_lastmonth.int_mm.intVal() ==3 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[2]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_003;
                }
                if (gstr_lastmonth.int_mm.intVal() ==4 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[3]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_004;
                }
                if (gstr_lastmonth.int_mm.intVal() ==5 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[4]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_005;
                }
                if (gstr_lastmonth.int_mm.intVal() ==6 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[5]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_006;
                }
                if (gstr_lastmonth.int_mm.intVal() ==7 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[6]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_007;
                }
                if (gstr_lastmonth.int_mm.intVal() ==8 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[7]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_008;
                }
                if (gstr_lastmonth.int_mm.intVal() ==9 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[8]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_009;
                }
                if (gstr_lastmonth.int_mm.intVal() ==10 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[9]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_010;
                }
                if (gstr_lastmonth.int_mm.intVal() ==11 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[10] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_011;
                }
                if (gstr_lastmonth.int_mm.intVal() ==12 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month0_buf[11] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_012;
                }
            }
            if ((( gstr_lastmonth.int_y_bottom.intVal() % 2) == 1 ) &&  /* 奇数年 */
                    ( rtn_cd == C_const_OK ))                 {   /* 取得情報あり */
                if (gstr_lastmonth.int_mm.intVal() ==1 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[0]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_101;
                }
                if (gstr_lastmonth.int_mm.intVal() ==2 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[1]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_102;
                }
                if (gstr_lastmonth.int_mm.intVal() ==3 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[2]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_103;
                }
                if (gstr_lastmonth.int_mm.intVal() ==4 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[3]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_104;
                }
                if (gstr_lastmonth.int_mm.intVal() ==5 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[4]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_105;
                }
                if (gstr_lastmonth.int_mm.intVal() ==6 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[5]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_106;
                }
                if (gstr_lastmonth.int_mm.intVal() ==7 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[6]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_107;
                }
                if (gstr_lastmonth.int_mm.intVal() ==8 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[7]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_108;
                }
                if (gstr_lastmonth.int_mm.intVal() ==9 ) {      /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[8]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_109;
                }
                if (gstr_lastmonth.int_mm.intVal() ==10 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[9]  = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_110;
                }
                if (gstr_lastmonth.int_mm.intVal() ==11 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[10] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_111;
                }
                if (gstr_lastmonth.int_mm.intVal() ==12 ) {     /* 前日（当月：翌月ランク対象） */
                    wstr_RankUpMoneybuf.month1_buf[11] = h_ts_rank_info_data.gekkan_rankup_taisho_kingaku_112;
                }
            }
            /* TSポイント年別情報(前日前年)から取得 end*/
        }


        if (DBG_LOG){
                /*------------------------------------------------------------*/
                memset( out_format_buf, 0x00, sizeof(out_format_buf) );
                for (wi_idx = 0; wi_idx < 10; wi_idx++) {
                    memset( out_format_buf, 0x00, sizeof(out_format_buf) );
                    sprintf( out_format_buf, "年間家族ランクＵＰ対象金額%d =[%10.0f]\n", wi_idx, wstr_RankUpMoneybuf.year_buf[wi_idx] );
                    C_DbgMsg("*** getRankUpMoney *** ランクＵＰ対象金額 %s\n"           , out_format_buf);
                }
                for (wi_idx = 0; wi_idx < 12; wi_idx++) {
                    memset( out_format_buf, 0x00, sizeof(out_format_buf) );
                    sprintf( out_format_buf, "月間家族ランクＵＰ金額%d%02d =[%10.0f]\n", 0, (wi_idx + 1), wstr_RankUpMoneybuf.month0_buf[wi_idx] );
                    C_DbgMsg("*** getRankUpMoney *** ランクＵＰ対象金額 %s\n"           , out_format_buf);
                }
                for (wi_idx = 0; wi_idx < 12; wi_idx++) {
                    memset( out_format_buf, 0x00, sizeof(out_format_buf) );
                    sprintf( out_format_buf, "月間家族ランクＵＰ金額%d%02d =[%10.0f]\n", 1, (wi_idx + 1), wstr_RankUpMoneybuf.month1_buf[wi_idx] );
                    C_DbgMsg("*** getRankUpMoney *** ランクＵＰ対象金額 %s\n"           , out_format_buf);
                }
                /*------------------------------------------------------------*/
        }

        /*------------------------------------------------------------------*/
        /* 家族ランクＵＰ金額最終更新日設定  */
        /*------------------------------------------------------------------*/
        /* ランクアップ対象金額に変更がある場合、バッチ処理日付を設定する */
        for (wi_idx = 0; wi_idx < 10; wi_idx++) {
            if ( wstr_RankUpMoneybuf.year_buf[wi_idx].intVal() != 0 ) {
                mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd = this_date;
                break;
            }
        }
        if ( mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd != this_date ) {
            for (wi_idx = 0; wi_idx < 12; wi_idx++) {
                if ( wstr_RankUpMoneybuf.month0_buf[wi_idx] .intVal()!= 0 ) {
                    mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd = this_date;
                    break;
                }
                if ( wstr_RankUpMoneybuf.month1_buf[wi_idx]  .intVal()!= 0 ) {
                    mskased_t.kazoku_rankup_kingaku_saishu_koshin_ymd = this_date;
                    break;
                }
            }
        }

        /*------------------------------------------------------------------*/
        /* ランクＵＰ金額設定  */
        /*------------------------------------------------------------------*/
        /*--- 年ランクＵＰ金額設定 ---*/
        /* 前年／当年 下１桁マッチ(ランクＵＰ対象金額あり) ならば、当年／翌年 年間家族ランク情報を更新 */
        if ( wstr_RankUpMoneybuf.year_buf[0] .intVal() != 0 ) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.reduce( wstr_RankUpMoneybuf.year_buf[0]);    /* 年間家族ランクＵＰ対象金額０ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_0.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_1.arr = wk_rank;                                                   /* 年次ランクコード０           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[1] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.reduce( wstr_RankUpMoneybuf.year_buf[1]);    /* 年間家族ランクＵＰ対象金額１ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_1.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_2 .arr = wk_rank;                                                   /* 年次ランクコード１           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[2] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.reduce( wstr_RankUpMoneybuf.year_buf[2]);    /* 年間家族ランクＵＰ対象金額２ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_2.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_3 .arr = wk_rank;                                                   /* 年次ランクコード２           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[3] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.reduce( wstr_RankUpMoneybuf.year_buf[3]);    /* 年間家族ランクＵＰ対象金額３ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_3.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_4 .arr = wk_rank;                                                   /* 年次ランクコード３           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[4] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.reduce( wstr_RankUpMoneybuf.year_buf[4]);    /* 年間家族ランクＵＰ対象金額４ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_4.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_5 .arr = wk_rank;                                                   /* 年次ランクコード４           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[5] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.reduce( wstr_RankUpMoneybuf.year_buf[5]);    /* 年間家族ランクＵＰ対象金額５ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_5.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_6 .arr = wk_rank;                                                   /* 年次ランクコード５           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[6] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.reduce( wstr_RankUpMoneybuf.year_buf[6]);    /* 年間家族ランクＵＰ対象金額６ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_6.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_7 .arr = wk_rank;                                                   /* 年次ランクコード６           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[7] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.reduce( wstr_RankUpMoneybuf.year_buf[7]);    /* 年間家族ランクＵＰ対象金額７ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_7.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_8 .arr = wk_rank;                                                   /* 年次ランクコード７           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[8] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.reduce( wstr_RankUpMoneybuf.year_buf[8]);    /* 年間家族ランクＵＰ対象金額８ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_8.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_9 .arr = wk_rank;                                                   /* 年次ランクコード８           */
        }
        if ( wstr_RankUpMoneybuf.year_buf[9] .intVal() != 0) {
            mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.reduce( wstr_RankUpMoneybuf.year_buf[9]);    /* 年間家族ランクＵＰ対象金額９ */
            getMaxRank(mskased_t.nenkan_kazoku_rankup_taisho_kingaku_9.floatVal(), i_shubetsu_nen, wk_rank); /* MAXランク取得処理            */
            mskased_t.nenji_rank_cd_0 .arr = wk_rank;                                                   /* 年次ランクコード９           */
        }

        /*--- 月ランクＵＰ金額設定 ---*/
        /* 前月／当月がマッチ(ランクＵＰ対象金額あり) ならば、当月／翌月 月間家族ランク情報を更新 */
        if ( wstr_RankUpMoneybuf.month0_buf[0]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.reduce( wstr_RankUpMoneybuf.month0_buf[0]);   /* 月間家族ランクＵＰ金額００１ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_001.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_002 .arr = wk_rank;                                                   /* 月次ランクコード００２       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[1]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.reduce( wstr_RankUpMoneybuf.month0_buf[1]);   /* 月間家族ランクＵＰ金額００２ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_002.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_003 .arr = wk_rank;                                                   /* 月次ランクコード００３       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[2]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.reduce( wstr_RankUpMoneybuf.month0_buf[2]);   /* 月間家族ランクＵＰ金額００３ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_003.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_104 .arr = wk_rank;                                                   /* 月次ランクコード１０４       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[3]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.reduce( wstr_RankUpMoneybuf.month0_buf[3]);   /* 月間家族ランクＵＰ金額００４ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_004.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_005 .arr = wk_rank;                                                   /* 月次ランクコード００５       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[4]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.reduce( wstr_RankUpMoneybuf.month0_buf[4]);   /* 月間家族ランクＵＰ金額００５ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_005.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_006 .arr = wk_rank;                                                   /* 月次ランクコード００６       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[5]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.reduce( wstr_RankUpMoneybuf.month0_buf[5]);   /* 月間家族ランクＵＰ金額００６ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_006.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_007 .arr = wk_rank;                                                   /* 月次ランクコード００７       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[6]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.reduce( wstr_RankUpMoneybuf.month0_buf[6]);   /* 月間家族ランクＵＰ金額００７ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_007.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_008 .arr = wk_rank;                                                   /* 月次ランクコード００８       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[7]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.reduce( wstr_RankUpMoneybuf.month0_buf[7]);   /* 月間家族ランクＵＰ金額００８ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_008.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_009 .arr = wk_rank;                                                   /* 月次ランクコード００９       */
        }
        if (( gstr_yesterday.int_mm.intVal()              ==  9 ) &&
                ( wstr_RankUpMoneybuf.month0_buf[8]  .intVal() != 0)) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.reduce( wstr_RankUpMoneybuf.month0_buf[8]);   /* 月間家族ランクＵＰ金額００９ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_009.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_010 .arr = wk_rank;                                                   /* 月次ランクコード０１０       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[9]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.reduce( wstr_RankUpMoneybuf.month0_buf[9]);   /* 月間家族ランクＵＰ金額０１０ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_010.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_011 .arr = wk_rank;                                                   /* 月次ランクコード０１１       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[10] .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.reduce( wstr_RankUpMoneybuf.month0_buf[10]);  /* 月間家族ランクＵＰ金額０１１ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_011.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_012 .arr = wk_rank;                                                   /* 月次ランクコード０１２       */
        }
        if ( wstr_RankUpMoneybuf.month0_buf[11] .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.reduce( wstr_RankUpMoneybuf.month0_buf[11]);  /* 月間家族ランクＵＰ金額０１２ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_012.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_001 .arr = wk_rank;                                                   /* 月次ランクコード００１       */
        }

        if ( wstr_RankUpMoneybuf.month1_buf[0]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.reduce( wstr_RankUpMoneybuf.month1_buf[0]);   /* 月間家族ランクＵＰ金額１０１ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_101.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_102 .arr = wk_rank;                                                   /* 月次ランクコード１０２       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[1]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.reduce( wstr_RankUpMoneybuf.month1_buf[1]);   /* 月間家族ランクＵＰ金額１０２ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_102.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_103 .arr = wk_rank;                                                   /* 月次ランクコード１０３       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[2]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.reduce( wstr_RankUpMoneybuf.month1_buf[2]);   /* 月間家族ランクＵＰ金額１０３ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_103.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_004 .arr = wk_rank;                                                   /* 月次ランクコード００４       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[3]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.reduce( wstr_RankUpMoneybuf.month1_buf[3]);   /* 月間家族ランクＵＰ金額１０４ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_104.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_105 .arr = wk_rank;                                                   /* 月次ランクコード１０５       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[4]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.reduce( wstr_RankUpMoneybuf.month1_buf[4]);   /* 月間家族ランクＵＰ金額１０５ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_105.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_106 .arr = wk_rank;                                                   /* 月次ランクコード１０６       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[5]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.reduce( wstr_RankUpMoneybuf.month1_buf[5]);   /* 月間家族ランクＵＰ金額１０６ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_106.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_107 .arr = wk_rank;                                                   /* 月次ランクコード１０７       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[6]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.reduce( wstr_RankUpMoneybuf.month1_buf[6]);   /* 月間家族ランクＵＰ金額１０７ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_107.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_108 .arr = wk_rank;                                                   /* 月次ランクコード１０８       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[7]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.reduce( wstr_RankUpMoneybuf.month1_buf[7]);   /* 月間家族ランクＵＰ金額１０８ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_108.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_109 .arr = wk_rank;                                                   /* 月次ランクコード１０９       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[8]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.reduce( wstr_RankUpMoneybuf.month1_buf[8]);   /* 月間家族ランクＵＰ金額１０９ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_109.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_110 .arr = wk_rank;                                                   /* 月次ランクコード１１０       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[9]  .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.reduce( wstr_RankUpMoneybuf.month1_buf[9]);   /* 月間家族ランクＵＰ金額１１０ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_110.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_111 .arr = wk_rank;                                                   /* 月次ランクコード１１１       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[10] .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.reduce( wstr_RankUpMoneybuf.month1_buf[10]);  /* 月間家族ランクＵＰ金額１１１ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_111.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_112 .arr = wk_rank;                                                   /* 月次ランクコード１１２       */
        }
        if ( wstr_RankUpMoneybuf.month1_buf[11] .intVal() != 0) {
            mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.reduce( wstr_RankUpMoneybuf.month1_buf[11]);  /* 月間家族ランクＵＰ金額１１２ */
            getMaxRank(mskased_t.gekkan_kazoku_rankup_taisho_kingaku_112.floatVal(), i_shubetsu_tuki, wk_rank); /* MAXランク取得処理            */
            mskased_t.getuji_rank_cd_101 .arr = wk_rank;                                                   /* 月次ランクコード１０１       */
        }


        if (DBG_LOG){
                /*------------------------------------------------------------*/
                C_DbgEnd("getRankUpMoney処理", 0, 0, 0);
                /*------------------------------------------------------------*/
        }

        return ( C_const_OK );              /* 処理終了                           */
        /*-----getRankUpMoney Bottom----------------------------------------------*/
    }


/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： trimRight                                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  trimRight(char *pbuf)                                       */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     後ろスペースの削除処理                                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              char             *       pbuf : 削除対象                      */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*                                                                            */
    /******************************************************************************/
    public int trimRight(StringDto pbuf)
    {
        int i;
        int wlen;


        wlen = pbuf.arr.length();
//        for (i = 0; i < wlen; i++) {
//            if (pbuf[wlen - 1 - i] != ' ') break;
//
//            pbuf[wlen - 1 - i] = '\0';
//
//        }
        pbuf.arr= pbuf.arr.replaceAll("\\s+$","");

        return C_const_OK;

        /*-----trimRight Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： GetTsRankInfo                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int GetTsRankInfo(TS_RANK_INFO_TBL  *tsRankInfo, int *status)         */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              TSランク情報取得                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*          TS_RANK_INFO_TBL *tsRankInfo ： TSランク情報構造体取得パラメータ  */
    /*          int              *status     ： 結果ステータス                    */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */
    /******************************************************************************/
    public int GetTsRankInfo(TS_RANK_INFO_TBL tsRankInfo, IntegerDto status)
    {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        TS_RANK_INFO_TBL h_ts_rank_info_buff = null; /* TSランク情報バッファ */
//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG){
                C_DbgMsg("GetTsRankInfo : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (tsRankInfo == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG){
                        C_DbgMsg("GetTsRankInfo : %s\n", "PRMERR(NULL)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }


        /* 出力エリアのクリア */
        tsRankInfo.nenji_rank_cd_0 .arr= 0;                               /* 年次ランクコード０             */
        tsRankInfo.nenji_rank_cd_1 .arr= 0;                               /* 年次ランクコード１             */
        tsRankInfo.nenji_rank_cd_2 .arr= 0;                               /* 年次ランクコード２             */
        tsRankInfo.nenji_rank_cd_3 .arr= 0;                               /* 年次ランクコード３             */
        tsRankInfo.nenji_rank_cd_4 .arr= 0;                               /* 年次ランクコード４             */
        tsRankInfo.nenji_rank_cd_5 .arr= 0;                               /* 年次ランクコード５             */
        tsRankInfo.nenji_rank_cd_6 .arr= 0;                               /* 年次ランクコード６             */
        tsRankInfo.nenji_rank_cd_7 .arr= 0;                               /* 年次ランクコード７             */
        tsRankInfo.nenji_rank_cd_8 .arr= 0;                               /* 年次ランクコード８             */
        tsRankInfo.nenji_rank_cd_9 .arr= 0;                               /* 年次ランクコード９             */
        tsRankInfo.nenkan_rankup_taisho_kingaku_0 .arr= 0;                /* 年間ランクＵＰ対象金額０       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_1 .arr= 0;                /* 年間ランクＵＰ対象金額１       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_2 .arr= 0;                /* 年間ランクＵＰ対象金額２       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_3 .arr= 0;                /* 年間ランクＵＰ対象金額３       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_4 .arr= 0;                /* 年間ランクＵＰ対象金額４       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_5 .arr= 0;                /* 年間ランクＵＰ対象金額５       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_6 .arr= 0;                /* 年間ランクＵＰ対象金額６       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_7 .arr= 0;                /* 年間ランクＵＰ対象金額７       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_8 .arr= 0;                /* 年間ランクＵＰ対象金額８       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_9 .arr= 0;                /* 年間ランクＵＰ対象金額９       */
        tsRankInfo.getsuji_rank_cd_004.arr = 0;                           /* 月次ランクコード００４         */
        tsRankInfo.getsuji_rank_cd_005.arr = 0;                           /* 月次ランクコード００５         */
        tsRankInfo.getsuji_rank_cd_006.arr = 0;                           /* 月次ランクコード００６         */
        tsRankInfo.getsuji_rank_cd_007.arr = 0;                           /* 月次ランクコード００７         */
        tsRankInfo.getsuji_rank_cd_008.arr = 0;                           /* 月次ランクコード００８         */
        tsRankInfo.getsuji_rank_cd_009.arr = 0;                           /* 月次ランクコード００９         */
        tsRankInfo.getsuji_rank_cd_010.arr = 0;                           /* 月次ランクコード０１０         */
        tsRankInfo.getsuji_rank_cd_011.arr = 0;                           /* 月次ランクコード０１１         */
        tsRankInfo.getsuji_rank_cd_012.arr = 0;                           /* 月次ランクコード０１２         */
        tsRankInfo.getsuji_rank_cd_001.arr = 0;                           /* 月次ランクコード００１         */
        tsRankInfo.getsuji_rank_cd_002.arr = 0;                           /* 月次ランクコード００２         */
        tsRankInfo.getsuji_rank_cd_003.arr = 0;                           /* 月次ランクコード００３         */
        tsRankInfo.getsuji_rank_cd_104.arr = 0;                           /* 月次ランクコード１０４         */
        tsRankInfo.getsuji_rank_cd_105.arr = 0;                           /* 月次ランクコード１０５         */
        tsRankInfo.getsuji_rank_cd_106.arr = 0;                           /* 月次ランクコード１０６         */
        tsRankInfo.getsuji_rank_cd_107.arr = 0;                           /* 月次ランクコード１０７         */
        tsRankInfo.getsuji_rank_cd_108.arr = 0;                           /* 月次ランクコード１０８         */
        tsRankInfo.getsuji_rank_cd_109.arr = 0;                           /* 月次ランクコード１０９         */
        tsRankInfo.getsuji_rank_cd_110.arr = 0;                           /* 月次ランクコード１１０         */
        tsRankInfo.getsuji_rank_cd_111.arr = 0;                           /* 月次ランクコード１１１         */
        tsRankInfo.getsuji_rank_cd_112.arr = 0;                           /* 月次ランクコード１１２         */
        tsRankInfo.getsuji_rank_cd_101.arr = 0;                           /* 月次ランクコード１０１         */
        tsRankInfo.getsuji_rank_cd_102.arr = 0;                           /* 月次ランクコード１０２         */
        tsRankInfo.getsuji_rank_cd_103.arr = 0;                           /* 月次ランクコード１０３         */
        tsRankInfo.gekkan_rankup_taisho_kingaku_004.arr= 0;              /* 月間ランクＵＰ対象金額００４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_005.arr= 0;              /* 月間ランクＵＰ対象金額００５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_006.arr= 0;              /* 月間ランクＵＰ対象金額００６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_007.arr= 0;              /* 月間ランクＵＰ対象金額００７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_008.arr= 0;              /* 月間ランクＵＰ対象金額００８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_009.arr= 0;              /* 月間ランクＵＰ対象金額００９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_010.arr= 0;              /* 月間ランクＵＰ対象金額０１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_011.arr= 0;              /* 月間ランクＵＰ対象金額０１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_012.arr= 0;              /* 月間ランクＵＰ対象金額０１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_001.arr= 0;              /* 月間ランクＵＰ対象金額００１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_002.arr= 0;              /* 月間ランクＵＰ対象金額００２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_003.arr= 0;              /* 月間ランクＵＰ対象金額００３   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_104.arr= 0;              /* 月間ランクＵＰ対象金額１０４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_105.arr= 0;              /* 月間ランクＵＰ対象金額１０５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_106.arr= 0;              /* 月間ランクＵＰ対象金額１０６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_107.arr= 0;              /* 月間ランクＵＰ対象金額１０７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_108.arr= 0;              /* 月間ランクＵＰ対象金額１０８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_109.arr= 0;              /* 月間ランクＵＰ対象金額１０９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_110.arr= 0;              /* 月間ランクＵＰ対象金額１１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_111.arr= 0;              /* 月間ランクＵＰ対象金額１１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_112.arr= 0;              /* 月間ランクＵＰ対象金額１１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_101.arr= 0;              /* 月間ランクＵＰ対象金額１０１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_102.arr= 0;              /* 月間ランクＵＰ対象金額１０２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_103.arr= 0;              /* 月間ランクＵＰ対象金額１０３   */
        tsRankInfo.gekkan_premium_point_kingaku_004.arr= 0;              /* 月間プレミアムポイント数００４ */
        tsRankInfo.gekkan_premium_point_kingaku_005.arr= 0;              /* 月間プレミアムポイント数００５ */
        tsRankInfo.gekkan_premium_point_kingaku_006.arr= 0;              /* 月間プレミアムポイント数００６ */
        tsRankInfo.gekkan_premium_point_kingaku_007.arr= 0;              /* 月間プレミアムポイント数００７ */
        tsRankInfo.gekkan_premium_point_kingaku_008.arr= 0;              /* 月間プレミアムポイント数００８ */
        tsRankInfo.gekkan_premium_point_kingaku_009.arr= 0;              /* 月間プレミアムポイント数００９ */
        tsRankInfo.gekkan_premium_point_kingaku_010.arr= 0;              /* 月間プレミアムポイント数０１０ */
        tsRankInfo.gekkan_premium_point_kingaku_011.arr= 0;              /* 月間プレミアムポイント数０１１ */
        tsRankInfo.gekkan_premium_point_kingaku_012.arr= 0;              /* 月間プレミアムポイント数０１２ */
        tsRankInfo.gekkan_premium_point_kingaku_001.arr= 0;              /* 月間プレミアムポイント数００１ */
        tsRankInfo.gekkan_premium_point_kingaku_002.arr= 0;              /* 月間プレミアムポイント数００２ */
        tsRankInfo.gekkan_premium_point_kingaku_003.arr= 0;              /* 月間プレミアムポイント数００３ */
        tsRankInfo.gekkan_premium_point_kingaku_104.arr= 0;              /* 月間プレミアムポイント数１０４ */
        tsRankInfo.gekkan_premium_point_kingaku_105.arr= 0;              /* 月間プレミアムポイント数１０５ */
        tsRankInfo.gekkan_premium_point_kingaku_106.arr= 0;              /* 月間プレミアムポイント数１０６ */
        tsRankInfo.gekkan_premium_point_kingaku_107.arr= 0;              /* 月間プレミアムポイント数１０７ */
        tsRankInfo.gekkan_premium_point_kingaku_108.arr= 0;              /* 月間プレミアムポイント数１０８ */
        tsRankInfo.gekkan_premium_point_kingaku_109.arr= 0;              /* 月間プレミアムポイント数１０９ */
        tsRankInfo.gekkan_premium_point_kingaku_110.arr= 0;              /* 月間プレミアムポイント数１１０ */
        tsRankInfo.gekkan_premium_point_kingaku_111.arr= 0;              /* 月間プレミアムポイント数１１１ */
        tsRankInfo.gekkan_premium_point_kingaku_112.arr= 0;              /* 月間プレミアムポイント数１１２ */
        tsRankInfo.gekkan_premium_point_kingaku_101.arr= 0;              /* 月間プレミアムポイント数１０１ */
        tsRankInfo.gekkan_premium_point_kingaku_102.arr= 0;              /* 月間プレミアムポイント数１０２ */
        tsRankInfo.gekkan_premium_point_kingaku_103.arr= 0;              /* 月間プレミアムポイント数１０３ */
        tsRankInfo.saishu_koshin_ymd.arr = 0;                             /* 最終更新日                     */
        tsRankInfo.saishu_koshin_ymdhms.arr = 0;                          /* 最終更新日時                   */
        strcpy(tsRankInfo.saishu_koshin_programid,
                "                    ");                                /* 最終更新プログラムＩＤ         */

        /* ホスト変数を編集する */
        h_ts_rank_info_buff = new TS_RANK_INFO_TBL();
        memset(h_ts_rank_info_buff, 0x00, 0);
        memcpy(h_ts_rank_info_buff.kokyaku_no, tsRankInfo.kokyaku_no,tsRankInfo.kokyaku_no.len);
        h_ts_rank_info_buff.kokyaku_no.len = tsRankInfo.kokyaku_no.len;

        /* ＳＱＬを実行する */
//        EXEC SQL SELECT NVL(年次ランクコード０,0),
//        NVL(年次ランクコード１,0),
//                NVL(年次ランクコード２,0),
//                NVL(年次ランクコード３,0),
//                NVL(年次ランクコード４,0),
//                NVL(年次ランクコード５,0),
//                NVL(年次ランクコード６,0),
//                NVL(年次ランクコード７,0),
//                NVL(年次ランクコード８,0),
//                NVL(年次ランクコード９,0),
//                NVL(年間ランクＵＰ対象金額０,0),
//                NVL(年間ランクＵＰ対象金額１,0),
//                NVL(年間ランクＵＰ対象金額２,0),
//                NVL(年間ランクＵＰ対象金額３,0),
//                NVL(年間ランクＵＰ対象金額４,0),
//                NVL(年間ランクＵＰ対象金額５,0),
//                NVL(年間ランクＵＰ対象金額６,0),
//                NVL(年間ランクＵＰ対象金額７,0),
//                NVL(年間ランクＵＰ対象金額８,0),
//                NVL(年間ランクＵＰ対象金額９,0),
//                NVL(月次ランクコード００４,0),
//                NVL(月次ランクコード００５,0),
//                NVL(月次ランクコード００６,0),
//                NVL(月次ランクコード００７,0),
//                NVL(月次ランクコード００８,0),
//                NVL(月次ランクコード００９,0),
//                NVL(月次ランクコード０１０,0),
//                NVL(月次ランクコード０１１,0),
//                NVL(月次ランクコード０１２,0),
//                NVL(月次ランクコード００１,0),
//                NVL(月次ランクコード００２,0),
//                NVL(月次ランクコード００３,0),
//                NVL(月次ランクコード１０４,0),
//                NVL(月次ランクコード１０５,0),
//                NVL(月次ランクコード１０６,0),
//                NVL(月次ランクコード１０７,0),
//                NVL(月次ランクコード１０８,0),
//                NVL(月次ランクコード１０９,0),
//                NVL(月次ランクコード１１０,0),
//                NVL(月次ランクコード１１１,0),
//                NVL(月次ランクコード１１２,0),
//                NVL(月次ランクコード１０１,0),
//                NVL(月次ランクコード１０２,0),
//                NVL(月次ランクコード１０３,0),
//                NVL(月間ランクＵＰ対象金額００４,0),
//                NVL(月間ランクＵＰ対象金額００５,0),
//                NVL(月間ランクＵＰ対象金額００６,0),
//                NVL(月間ランクＵＰ対象金額００７,0),
//                NVL(月間ランクＵＰ対象金額００８,0),
//                NVL(月間ランクＵＰ対象金額００９,0),
//                NVL(月間ランクＵＰ対象金額０１０,0),
//                NVL(月間ランクＵＰ対象金額０１１,0),
//                NVL(月間ランクＵＰ対象金額０１２,0),
//                NVL(月間ランクＵＰ対象金額００１,0),
//                NVL(月間ランクＵＰ対象金額００２,0),
//                NVL(月間ランクＵＰ対象金額００３,0),
//                NVL(月間ランクＵＰ対象金額１０４,0),
//                NVL(月間ランクＵＰ対象金額１０５,0),
//                NVL(月間ランクＵＰ対象金額１０６,0),
//                NVL(月間ランクＵＰ対象金額１０７,0),
//                NVL(月間ランクＵＰ対象金額１０８,0),
//                NVL(月間ランクＵＰ対象金額１０９,0),
//                NVL(月間ランクＵＰ対象金額１１０,0),
//                NVL(月間ランクＵＰ対象金額１１１,0),
//                NVL(月間ランクＵＰ対象金額１１２,0),
//                NVL(月間ランクＵＰ対象金額１０１,0),
//                NVL(月間ランクＵＰ対象金額１０２,0),
//                NVL(月間ランクＵＰ対象金額１０３,0),
//                NVL(月間プレミアムポイント数００４,0),
//                NVL(月間プレミアムポイント数００５,0),
//                NVL(月間プレミアムポイント数００６,0),
//                NVL(月間プレミアムポイント数００７,0),
//                NVL(月間プレミアムポイント数００８,0),
//                NVL(月間プレミアムポイント数００９,0),
//                NVL(月間プレミアムポイント数０１０,0),
//                NVL(月間プレミアムポイント数０１１,0),
//                NVL(月間プレミアムポイント数０１２,0),
//                NVL(月間プレミアムポイント数００１,0),
//                NVL(月間プレミアムポイント数００２,0),
//                NVL(月間プレミアムポイント数００３,0),
//                NVL(月間プレミアムポイント数１０４,0),
//                NVL(月間プレミアムポイント数１０５,0),
//                NVL(月間プレミアムポイント数１０６,0),
//                NVL(月間プレミアムポイント数１０７,0),
//                NVL(月間プレミアムポイント数１０８,0),
//                NVL(月間プレミアムポイント数１０９,0),
//                NVL(月間プレミアムポイント数１１０,0),
//                NVL(月間プレミアムポイント数１１１,0),
//                NVL(月間プレミアムポイント数１１２,0),
//                NVL(月間プレミアムポイント数１０１,0),
//                NVL(月間プレミアムポイント数１０２,0),
//                NVL(月間プレミアムポイント数１０３,0),
//                NVL(最終更新日,0),
//                to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                NVL(最終更新プログラムＩＤ,'                    ')
//        INTO  :h_ts_rank_info_buff.nenji_rank_cd_0,
//                            :h_ts_rank_info_buff.nenji_rank_cd_1,
//                            :h_ts_rank_info_buff.nenji_rank_cd_2,
//                            :h_ts_rank_info_buff.nenji_rank_cd_3,
//                            :h_ts_rank_info_buff.nenji_rank_cd_4,
//                            :h_ts_rank_info_buff.nenji_rank_cd_5,
//                            :h_ts_rank_info_buff.nenji_rank_cd_6,
//                            :h_ts_rank_info_buff.nenji_rank_cd_7,
//                            :h_ts_rank_info_buff.nenji_rank_cd_8,
//                            :h_ts_rank_info_buff.nenji_rank_cd_9,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8,
//                            :h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_004,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_005,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_006,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_007,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_008,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_009,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_010,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_011,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_012,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_001,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_002,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_003,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_104,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_105,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_106,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_107,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_108,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_109,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_110,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_111,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_112,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_101,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_102,
//                            :h_ts_rank_info_buff.getsuji_rank_cd_103,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102,
//                            :h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_004,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_005,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_006,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_007,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_008,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_009,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_010,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_011,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_012,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_001,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_002,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_003,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_104,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_105,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_106,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_107,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_108,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_109,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_110,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_111,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_112,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_101,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_102,
//                            :h_ts_rank_info_buff.gekkan_premium_point_kingaku_103,
//                            :h_ts_rank_info_buff.saishu_koshin_ymd,
//                            :h_ts_rank_info_buff.saishu_koshin_ymdhms,
//                            :h_ts_rank_info_buff.saishu_koshin_programid
//        FROM  TSランク情報@CMSD
//        WHERE 顧客番号       = :h_ts_rank_info_buff.kokyaku_no;

        sqlca.sql = new StringDto("SELECT NVL(年次ランクコード０,0)," +
                "        NVL(年次ランクコード１,0)," +
                "                NVL(年次ランクコード２,0)," +
                "                NVL(年次ランクコード３,0)," +
                "                NVL(年次ランクコード４,0)," +
                "                NVL(年次ランクコード５,0)," +
                "                NVL(年次ランクコード６,0)," +
                "                NVL(年次ランクコード７,0)," +
                "                NVL(年次ランクコード８,0)," +
                "                NVL(年次ランクコード９,0)," +
                "                NVL(年間ランクＵＰ対象金額０,0)," +
                "                NVL(年間ランクＵＰ対象金額１,0)," +
                "                NVL(年間ランクＵＰ対象金額２,0)," +
                "                NVL(年間ランクＵＰ対象金額３,0)," +
                "                NVL(年間ランクＵＰ対象金額４,0)," +
                "                NVL(年間ランクＵＰ対象金額５,0)," +
                "                NVL(年間ランクＵＰ対象金額６,0)," +
                "                NVL(年間ランクＵＰ対象金額７,0)," +
                "                NVL(年間ランクＵＰ対象金額８,0)," +
                "                NVL(年間ランクＵＰ対象金額９,0)," +
                "                NVL(月次ランクコード００４,0)," +
                "                NVL(月次ランクコード００５,0)," +
                "                NVL(月次ランクコード００６,0)," +
                "                NVL(月次ランクコード００７,0)," +
                "                NVL(月次ランクコード００８,0)," +
                "                NVL(月次ランクコード００９,0)," +
                "                NVL(月次ランクコード０１０,0)," +
                "                NVL(月次ランクコード０１１,0)," +
                "                NVL(月次ランクコード０１２,0)," +
                "                NVL(月次ランクコード００１,0)," +
                "                NVL(月次ランクコード００２,0)," +
                "                NVL(月次ランクコード００３,0)," +
                "                NVL(月次ランクコード１０４,0)," +
                "                NVL(月次ランクコード１０５,0)," +
                "                NVL(月次ランクコード１０６,0)," +
                "                NVL(月次ランクコード１０７,0)," +
                "                NVL(月次ランクコード１０８,0)," +
                "                NVL(月次ランクコード１０９,0)," +
                "                NVL(月次ランクコード１１０,0)," +
                "                NVL(月次ランクコード１１１,0)," +
                "                NVL(月次ランクコード１１２,0)," +
                "                NVL(月次ランクコード１０１,0)," +
                "                NVL(月次ランクコード１０２,0)," +
                "                NVL(月次ランクコード１０３,0)," +
                "                NVL(月間ランクＵＰ対象金額００４,0)," +
                "                NVL(月間ランクＵＰ対象金額００５,0)," +
                "                NVL(月間ランクＵＰ対象金額００６,0)," +
                "                NVL(月間ランクＵＰ対象金額００７,0)," +
                "                NVL(月間ランクＵＰ対象金額００８,0)," +
                "                NVL(月間ランクＵＰ対象金額００９,0)," +
                "                NVL(月間ランクＵＰ対象金額０１０,0)," +
                "                NVL(月間ランクＵＰ対象金額０１１,0)," +
                "                NVL(月間ランクＵＰ対象金額０１２,0)," +
                "                NVL(月間ランクＵＰ対象金額００１,0)," +
                "                NVL(月間ランクＵＰ対象金額００２,0)," +
                "                NVL(月間ランクＵＰ対象金額００３,0)," +
                "                NVL(月間ランクＵＰ対象金額１０４,0)," +
                "                NVL(月間ランクＵＰ対象金額１０５,0)," +
                "                NVL(月間ランクＵＰ対象金額１０６,0)," +
                "                NVL(月間ランクＵＰ対象金額１０７,0)," +
                "                NVL(月間ランクＵＰ対象金額１０８,0)," +
                "                NVL(月間ランクＵＰ対象金額１０９,0)," +
                "                NVL(月間ランクＵＰ対象金額１１０,0)," +
                "                NVL(月間ランクＵＰ対象金額１１１,0)," +
                "                NVL(月間ランクＵＰ対象金額１１２,0)," +
                "                NVL(月間ランクＵＰ対象金額１０１,0)," +
                "                NVL(月間ランクＵＰ対象金額１０２,0)," +
                "                NVL(月間ランクＵＰ対象金額１０３,0)," +
                "                NVL(月間プレミアムポイント数００４,0)," +
                "                NVL(月間プレミアムポイント数００５,0)," +
                "                NVL(月間プレミアムポイント数００６,0)," +
                "                NVL(月間プレミアムポイント数００７,0)," +
                "                NVL(月間プレミアムポイント数００８,0)," +
                "                NVL(月間プレミアムポイント数００９,0)," +
                "                NVL(月間プレミアムポイント数０１０,0)," +
                "                NVL(月間プレミアムポイント数０１１,0)," +
                "                NVL(月間プレミアムポイント数０１２,0)," +
                "                NVL(月間プレミアムポイント数００１,0)," +
                "                NVL(月間プレミアムポイント数００２,0)," +
                "                NVL(月間プレミアムポイント数００３,0)," +
                "                NVL(月間プレミアムポイント数１０４,0)," +
                "                NVL(月間プレミアムポイント数１０５,0)," +
                "                NVL(月間プレミアムポイント数１０６,0)," +
                "                NVL(月間プレミアムポイント数１０７,0)," +
                "                NVL(月間プレミアムポイント数１０８,0)," +
                "                NVL(月間プレミアムポイント数１０９,0)," +
                "                NVL(月間プレミアムポイント数１１０,0)," +
                "                NVL(月間プレミアムポイント数１１１,0)," +
                "                NVL(月間プレミアムポイント数１１２,0)," +
                "                NVL(月間プレミアムポイント数１０１,0)," +
                "                NVL(月間プレミアムポイント数１０２,0)," +
                "                NVL(月間プレミアムポイント数１０３,0)," +
                "                NVL(最終更新日,0)," +
                "                to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                "                NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')" +
                "        FROM  TSランク情報" +
                "        WHERE 顧客番号       = ?");
        sqlca.restAndExecute(h_ts_rank_info_buff.kokyaku_no);
        sqlca.fetch();
        sqlca.recData(h_ts_rank_info_buff.nenji_rank_cd_0,
                h_ts_rank_info_buff.nenji_rank_cd_1,
                h_ts_rank_info_buff.nenji_rank_cd_2,
                h_ts_rank_info_buff.nenji_rank_cd_3,
                h_ts_rank_info_buff.nenji_rank_cd_4,
                h_ts_rank_info_buff.nenji_rank_cd_5,
                h_ts_rank_info_buff.nenji_rank_cd_6,
                h_ts_rank_info_buff.nenji_rank_cd_7,
                h_ts_rank_info_buff.nenji_rank_cd_8,
                h_ts_rank_info_buff.nenji_rank_cd_9,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8,
                h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9,
                h_ts_rank_info_buff.getsuji_rank_cd_004,
                h_ts_rank_info_buff.getsuji_rank_cd_005,
                h_ts_rank_info_buff.getsuji_rank_cd_006,
                h_ts_rank_info_buff.getsuji_rank_cd_007,
                h_ts_rank_info_buff.getsuji_rank_cd_008,
                h_ts_rank_info_buff.getsuji_rank_cd_009,
                h_ts_rank_info_buff.getsuji_rank_cd_010,
                h_ts_rank_info_buff.getsuji_rank_cd_011,
                h_ts_rank_info_buff.getsuji_rank_cd_012,
                h_ts_rank_info_buff.getsuji_rank_cd_001,
                h_ts_rank_info_buff.getsuji_rank_cd_002,
                h_ts_rank_info_buff.getsuji_rank_cd_003,
                h_ts_rank_info_buff.getsuji_rank_cd_104,
                h_ts_rank_info_buff.getsuji_rank_cd_105,
                h_ts_rank_info_buff.getsuji_rank_cd_106,
                h_ts_rank_info_buff.getsuji_rank_cd_107,
                h_ts_rank_info_buff.getsuji_rank_cd_108,
                h_ts_rank_info_buff.getsuji_rank_cd_109,
                h_ts_rank_info_buff.getsuji_rank_cd_110,
                h_ts_rank_info_buff.getsuji_rank_cd_111,
                h_ts_rank_info_buff.getsuji_rank_cd_112,
                h_ts_rank_info_buff.getsuji_rank_cd_101,
                h_ts_rank_info_buff.getsuji_rank_cd_102,
                h_ts_rank_info_buff.getsuji_rank_cd_103,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102,
                h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_004,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_005,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_006,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_007,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_008,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_009,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_010,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_011,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_012,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_001,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_002,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_003,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_104,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_105,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_106,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_107,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_108,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_109,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_110,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_111,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_112,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_101,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_102,
                h_ts_rank_info_buff.gekkan_premium_point_kingaku_103,
                h_ts_rank_info_buff.saishu_koshin_ymd,
                h_ts_rank_info_buff.saishu_koshin_ymdhms,
                h_ts_rank_info_buff.saishu_koshin_programid);

        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf( out_format_buf, "顧客番号=%s",
                    h_ts_rank_info_buff.kokyaku_no.arr );

            APLOG_WT( "904", 0, null, "SELECT", sqlca.sqlcode,
                    "TSランク情報", out_format_buf, 0, 0);

            /* 処理を終了する */
                status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
                status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        /* 出力引数の設定 */
        tsRankInfo.nenji_rank_cd_0                         = h_ts_rank_info_buff.nenji_rank_cd_0;                     /* 年次ランクコード０             */
        tsRankInfo.nenji_rank_cd_1                         = h_ts_rank_info_buff.nenji_rank_cd_1;                     /* 年次ランクコード１             */
        tsRankInfo.nenji_rank_cd_2                         = h_ts_rank_info_buff.nenji_rank_cd_2;                     /* 年次ランクコード２             */
        tsRankInfo.nenji_rank_cd_3                         = h_ts_rank_info_buff.nenji_rank_cd_3;                     /* 年次ランクコード３             */
        tsRankInfo.nenji_rank_cd_4                         = h_ts_rank_info_buff.nenji_rank_cd_4;                     /* 年次ランクコード４             */
        tsRankInfo.nenji_rank_cd_5                         = h_ts_rank_info_buff.nenji_rank_cd_5;                     /* 年次ランクコード５             */
        tsRankInfo.nenji_rank_cd_6                         = h_ts_rank_info_buff.nenji_rank_cd_6;                     /* 年次ランクコード６             */
        tsRankInfo.nenji_rank_cd_7                         = h_ts_rank_info_buff.nenji_rank_cd_7;                     /* 年次ランクコード７             */
        tsRankInfo.nenji_rank_cd_8                         = h_ts_rank_info_buff.nenji_rank_cd_8;                     /* 年次ランクコード８             */
        tsRankInfo.nenji_rank_cd_9                         = h_ts_rank_info_buff.nenji_rank_cd_9;                     /* 年次ランクコード９             */
        tsRankInfo.nenkan_rankup_taisho_kingaku_0          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_0;      /* 年間ランクＵＰ対象金額０       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_1          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_1;      /* 年間ランクＵＰ対象金額１       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_2          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_2;      /* 年間ランクＵＰ対象金額２       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_3          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_3;      /* 年間ランクＵＰ対象金額３       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_4          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_4;      /* 年間ランクＵＰ対象金額４       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_5          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_5;      /* 年間ランクＵＰ対象金額５       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_6          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_6;      /* 年間ランクＵＰ対象金額６       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_7          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_7;      /* 年間ランクＵＰ対象金額７       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_8          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_8;      /* 年間ランクＵＰ対象金額８       */
        tsRankInfo.nenkan_rankup_taisho_kingaku_9          = h_ts_rank_info_buff.nenkan_rankup_taisho_kingaku_9;      /* 年間ランクＵＰ対象金額９       */
        tsRankInfo.getsuji_rank_cd_004                     = h_ts_rank_info_buff.getsuji_rank_cd_004;                 /* 月次ランクコード００４         */
        tsRankInfo.getsuji_rank_cd_005                     = h_ts_rank_info_buff.getsuji_rank_cd_005;                 /* 月次ランクコード００５         */
        tsRankInfo.getsuji_rank_cd_006                     = h_ts_rank_info_buff.getsuji_rank_cd_006;                 /* 月次ランクコード００６         */
        tsRankInfo.getsuji_rank_cd_007                     = h_ts_rank_info_buff.getsuji_rank_cd_007;                 /* 月次ランクコード００７         */
        tsRankInfo.getsuji_rank_cd_008                     = h_ts_rank_info_buff.getsuji_rank_cd_008;                 /* 月次ランクコード００８         */
        tsRankInfo.getsuji_rank_cd_009                     = h_ts_rank_info_buff.getsuji_rank_cd_009;                 /* 月次ランクコード００９         */
        tsRankInfo.getsuji_rank_cd_010                     = h_ts_rank_info_buff.getsuji_rank_cd_010;                 /* 月次ランクコード０１０         */
        tsRankInfo.getsuji_rank_cd_011                     = h_ts_rank_info_buff.getsuji_rank_cd_011;                 /* 月次ランクコード０１１         */
        tsRankInfo.getsuji_rank_cd_012                     = h_ts_rank_info_buff.getsuji_rank_cd_012;                 /* 月次ランクコード０１２         */
        tsRankInfo.getsuji_rank_cd_001                     = h_ts_rank_info_buff.getsuji_rank_cd_001;                 /* 月次ランクコード００１         */
        tsRankInfo.getsuji_rank_cd_002                     = h_ts_rank_info_buff.getsuji_rank_cd_002;                 /* 月次ランクコード００２         */
        tsRankInfo.getsuji_rank_cd_003                     = h_ts_rank_info_buff.getsuji_rank_cd_003;                 /* 月次ランクコード００３         */
        tsRankInfo.getsuji_rank_cd_104                     = h_ts_rank_info_buff.getsuji_rank_cd_104;                 /* 月次ランクコード１０４         */
        tsRankInfo.getsuji_rank_cd_105                     = h_ts_rank_info_buff.getsuji_rank_cd_105;                 /* 月次ランクコード１０５         */
        tsRankInfo.getsuji_rank_cd_106                     = h_ts_rank_info_buff.getsuji_rank_cd_106;                 /* 月次ランクコード１０６         */
        tsRankInfo.getsuji_rank_cd_107                     = h_ts_rank_info_buff.getsuji_rank_cd_107;                 /* 月次ランクコード１０７         */
        tsRankInfo.getsuji_rank_cd_108                     = h_ts_rank_info_buff.getsuji_rank_cd_108;                 /* 月次ランクコード１０８         */
        tsRankInfo.getsuji_rank_cd_109                     = h_ts_rank_info_buff.getsuji_rank_cd_109;                 /* 月次ランクコード１０９         */
        tsRankInfo.getsuji_rank_cd_110                     = h_ts_rank_info_buff.getsuji_rank_cd_110;                 /* 月次ランクコード１１０         */
        tsRankInfo.getsuji_rank_cd_111                     = h_ts_rank_info_buff.getsuji_rank_cd_111;                 /* 月次ランクコード１１１         */
        tsRankInfo.getsuji_rank_cd_112                     = h_ts_rank_info_buff.getsuji_rank_cd_112;                 /* 月次ランクコード１１２         */
        tsRankInfo.getsuji_rank_cd_101                     = h_ts_rank_info_buff.getsuji_rank_cd_101;                 /* 月次ランクコード１０１         */
        tsRankInfo.getsuji_rank_cd_102                     = h_ts_rank_info_buff.getsuji_rank_cd_102;                 /* 月次ランクコード１０２         */
        tsRankInfo.getsuji_rank_cd_103                     = h_ts_rank_info_buff.getsuji_rank_cd_103;                 /* 月次ランクコード１０３         */
        tsRankInfo.gekkan_rankup_taisho_kingaku_004        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_004;    /* 月間ランクＵＰ対象金額００４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_005        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_005;    /* 月間ランクＵＰ対象金額００５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_006        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_006;    /* 月間ランクＵＰ対象金額００６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_007        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_007;    /* 月間ランクＵＰ対象金額００７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_008        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_008;    /* 月間ランクＵＰ対象金額００８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_009        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_009;    /* 月間ランクＵＰ対象金額００９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_010        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_010;    /* 月間ランクＵＰ対象金額０１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_011        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_011;    /* 月間ランクＵＰ対象金額０１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_012        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_012;    /* 月間ランクＵＰ対象金額０１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_001        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_001;    /* 月間ランクＵＰ対象金額００１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_002        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_002;    /* 月間ランクＵＰ対象金額００２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_003        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_003;    /* 月間ランクＵＰ対象金額００３   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_104        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_104;    /* 月間ランクＵＰ対象金額１０４   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_105        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_105;    /* 月間ランクＵＰ対象金額１０５   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_106        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_106;    /* 月間ランクＵＰ対象金額１０６   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_107        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_107;    /* 月間ランクＵＰ対象金額１０７   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_108        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_108;    /* 月間ランクＵＰ対象金額１０８   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_109        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_109;    /* 月間ランクＵＰ対象金額１０９   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_110        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_110;    /* 月間ランクＵＰ対象金額１１０   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_111        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_111;    /* 月間ランクＵＰ対象金額１１１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_112        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_112;    /* 月間ランクＵＰ対象金額１１２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_101        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_101;    /* 月間ランクＵＰ対象金額１０１   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_102        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_102;    /* 月間ランクＵＰ対象金額１０２   */
        tsRankInfo.gekkan_rankup_taisho_kingaku_103        = h_ts_rank_info_buff.gekkan_rankup_taisho_kingaku_103;    /* 月間ランクＵＰ対象金額１０３   */
        tsRankInfo.gekkan_premium_point_kingaku_004        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_004;    /* 月間プレミアムポイント数００４ */
        tsRankInfo.gekkan_premium_point_kingaku_005        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_005;    /* 月間プレミアムポイント数００５ */
        tsRankInfo.gekkan_premium_point_kingaku_006        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_006;    /* 月間プレミアムポイント数００６ */
        tsRankInfo.gekkan_premium_point_kingaku_007        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_007;    /* 月間プレミアムポイント数００７ */
        tsRankInfo.gekkan_premium_point_kingaku_008        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_008;    /* 月間プレミアムポイント数００８ */
        tsRankInfo.gekkan_premium_point_kingaku_009        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_009;    /* 月間プレミアムポイント数００９ */
        tsRankInfo.gekkan_premium_point_kingaku_010        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_010;    /* 月間プレミアムポイント数０１０ */
        tsRankInfo.gekkan_premium_point_kingaku_011        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_011;    /* 月間プレミアムポイント数０１１ */
        tsRankInfo.gekkan_premium_point_kingaku_012        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_012;    /* 月間プレミアムポイント数０１２ */
        tsRankInfo.gekkan_premium_point_kingaku_001        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_001;    /* 月間プレミアムポイント数００１ */
        tsRankInfo.gekkan_premium_point_kingaku_002        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_002;    /* 月間プレミアムポイント数００２ */
        tsRankInfo.gekkan_premium_point_kingaku_003        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_003;    /* 月間プレミアムポイント数００３ */
        tsRankInfo.gekkan_premium_point_kingaku_104        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_104;    /* 月間プレミアムポイント数１０４ */
        tsRankInfo.gekkan_premium_point_kingaku_105        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_105;    /* 月間プレミアムポイント数１０５ */
        tsRankInfo.gekkan_premium_point_kingaku_106        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_106;    /* 月間プレミアムポイント数１０６ */
        tsRankInfo.gekkan_premium_point_kingaku_107        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_107;    /* 月間プレミアムポイント数１０７ */
        tsRankInfo.gekkan_premium_point_kingaku_108        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_108;    /* 月間プレミアムポイント数１０８ */
        tsRankInfo.gekkan_premium_point_kingaku_109        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_109;    /* 月間プレミアムポイント数１０９ */
        tsRankInfo.gekkan_premium_point_kingaku_110        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_110;    /* 月間プレミアムポイント数１１０ */
        tsRankInfo.gekkan_premium_point_kingaku_111        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_111;    /* 月間プレミアムポイント数１１１ */
        tsRankInfo.gekkan_premium_point_kingaku_112        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_112;    /* 月間プレミアムポイント数１１２ */
        tsRankInfo.gekkan_premium_point_kingaku_101        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_101;    /* 月間プレミアムポイント数１０１ */
        tsRankInfo.gekkan_premium_point_kingaku_102        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_102;    /* 月間プレミアムポイント数１０２ */
        tsRankInfo.gekkan_premium_point_kingaku_103        = h_ts_rank_info_buff.gekkan_premium_point_kingaku_103;    /* 月間プレミアムポイント数１０３ */
        tsRankInfo.saishu_koshin_ymd                       = h_ts_rank_info_buff.saishu_koshin_ymd;                   /* 最終更新日                     */
        tsRankInfo.saishu_koshin_ymdhms                    = h_ts_rank_info_buff.saishu_koshin_ymdhms;                /* 最終更新日時                   */
        strcpy(tsRankInfo.saishu_koshin_programid, h_ts_rank_info_buff.saishu_koshin_programid);                      /* 最終更新プログラムＩＤ         */

        /* 戻り値の設定 */
        if (DBG_LOG){
                C_DbgMsg("顧客番号      : %s\n", h_ts_rank_info_buff.kokyaku_no.arr);
                C_DbgMsg("GetTsRankInfo : %s\n", "end");
        }

            status.arr = C_const_Stat_OK;
        return C_const_OK;

    }
}
