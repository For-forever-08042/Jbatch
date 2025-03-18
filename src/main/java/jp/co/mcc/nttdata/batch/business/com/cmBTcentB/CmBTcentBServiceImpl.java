package jp.co.mcc.nttdata.batch.business.com.cmBTcentB;

import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmBTcentB.dto.NYUKAI_RENDO_DATA;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.*;

@Service
public class CmBTcentBServiceImpl extends CmABfuncLServiceImpl implements CmBTcentBService {


    String PG_NAME = "カード入会連動";
    Integer EOF = -1;
    /*-------------------------------------------------------------------------------*/
    /*  定数定義                                                                     */
    /*-------------------------------------------------------------------------------*/
    int C_const_APWR = 49;          /* プログラム戻り値（警告）    */
    int DEF_OFF = 0;          /* OFF */
    int DEF_ON = 1;         /* ON */

    int DEF_BUFSIZE2 = 2;         /* 2バイト */
    int DEF_BUFSIZE4 = 4;         /* 4バイト */
    int DEF_BUFSIZE5 = 5;         /* 5バイト */
    int DEF_BUFSIZE6 = 6;         /* 6バイト */
    int DEF_BUFSIZE7 = 7;         /* 7バイト */
    int DEF_BUFSIZE8 = 8;         /* 8バイト */
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
    int DEF_BUFSIZE120 = 120;        /* 120バイト */
    int DEF_BUFSIZE256 = 256;        /* 256バイト */
    int DEF_BUFSIZE4K = 4 * 1024;   /* 4 * 1024バイト */
    int DEF_BUFSIZE8K = 8 * 1024;   /* 8 * 1024バイト */
    int DEF_BUFSIZE16K = 16 * 1024;  /* 16 * 1024バイト */
    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";       /* デバッグスイッチ */
    String DEF_debug = "-debug";       /* デバッグスイッチ */
    String DEF_ARG_I = "-i";            /* 入力ファイル名   */

    String REC_TYPE_DATA = "2";        /* レコードタイプ：データ部    */
    int DEF_Read_EOF = 9;       /* File read EOF               */

    /*-----  ログ出力用メッセージID ----------*/
    String DEF_MSG_ID_102 = "102";
    String DEF_MSG_ID_103 = "103";
    String DEF_MSG_ID_104 = "104";
    String DEF_MSG_ID_105 = "105";
    String DEF_MSG_ID_106 = "106";
    String DEF_MSG_ID_107 = "107";
    String DEF_MSG_ID_108 = "108";
    String DEF_MSG_ID_700 = "700";
    String DEF_MSG_ID_902 = "902";
    String DEF_MSG_ID_903 = "903";
    String DEF_MSG_ID_904 = "904";
    String DEF_MSG_ID_910 = "910";
    String DEF_MSG_ID_912 = "912";

    String DEF_STR1 = "\"";
    String DEF_STR2 = ",";
    String DEF_STR3 = "\r\n";
    NYUKAI_RENDO_DATA in_nyukai_rendo = new NYUKAI_RENDO_DATA();
    String DEF_OUTPUT_FILENAME = "_kaiin_touroku_error.csv";    /* 出力ファイル名 */

    int ERR_FILE_REC_LEN = 128;              /* エラーファイルレコード長 */

    int DEF_STR_SIZE100 = 100;          /* 構造体配列数100 */
    int DEF_STR_SIZE1000 = 1000;           /* 構造体配列数1000 */

    String DEF_KINOU_ID = "CENT  ";          /* 機能ID */

    String DEF_DATA_KUBUN = "入会";          /* データ区分(固定) */

    /*-----  電話番号  -----*/
    String DEF_TELTOP_070 = "070";
    String DEF_TELTOP_080 = "080";
    String DEF_TELTOP_090 = "090";
    ;
    String HALF_SPACE1 = " ";
    String HALF_SPACE4 = "    ";
    String HALF_SPACE10 = "          ";
    String HALF_SPACE11 = "           ";
    String HALF_SPACE13 = "             ";
    String HALF_SPACE15 = "               ";
    String HALF_SPACE20 = "                    ";
    String HALF_SPACE23 = "                       ";
    String HALF_SPACE40 = "                                        ";
    String HALF_SPACE50 = "                                        ";
    String HALF_SPACE60 = "                                                            ";
    String HALF_SPACE80 = "                                                                                ";

    /* 半角ZERO11桁 */
    String HALF_ZERO11 = "00000000000";

    String C_OUT_HEADER = "\"処理年月日\",\"登録日\",\"記入日\",\"入会企業コード\",\"入会企業名称\",\"入会店番号\",\"入会店舗名称\",\"会員番号\",\"氏名（漢字）\",\"理由コード\",\"エラー理由\"";

    /*-------------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                                 */
    /*-------------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;
//
//    int            chk_arg_i;                           /* 引数-iチェック用     */
//    String        arg_i_Value[256];                    /* 引数i設定値          */
//
//    /* 入会エラーリストファイル作成用                                           */
//    long           h_syori_ymd;                         /* 処理年月日           */
//    int            h_data_kubun;                        /* データ区分           */
//    long           h_irai_ymd;                          /* 依頼年月日           */
//    long           h_batch_no;                          /* バッチ番号           */
//    long           h_nyukai_ymd;                        /* 入会年月日           */
//    long           h_moushikomi_kigyou_cd;              /* 申込企業コード       */
//    String        h_kigyou_name[40*3+1];               /* 企業名称             */
//    long           h_moushikomisyoten_no;               /* 申込書店番号         */
//    String        h_kanji_tenpo_name[80*3+1];          /* 漢字店舗名称         */
//    long           h_kaiin_no;                          /* 会員番号             */
//    String        h_kaiin_name[80*3+1];                /* 会員氏名             */
//    long           h_riyuu_cd;                          /* 理由コード           */
//    String        h_error_msg[80 + 1];                 /* エラー理由           */
//
//    /* MS顧客制度情報静態情報用(静態状態チェック用)                             */
//    int            h_seitai_flag;                       /* 静態取込済フラグ     */
//    int            h_birth_month;                       /* 誕生月               */
//    int            h_zaiseki_yyyymm;                    /* 在籍開始年月         */
//    int            h_ec_flag;                           /* ＥＣ会員フラグ       */
//    int            h_tel_flag;                          /* 電話番号登録フラグ   */
//    int            hi_msks_sw;                          /* 追加(0)／更新(1)switch */
//    /* 2022/10/13 MCCM初版 ADD START */
//    int            h_mcc_seido_kyodaku_flg;             /* ＭＣＣ制度許諾フラグ   */
//    int            h_mcc_seido_kyodaku_koshinsha;       /* ＭＣＣ制度許諾更新者   */
//    long           h_mcc_seido_kyodaku_koshin_ymdhms;   /* ＭＣＣ制度許諾更新日時 */
//    /* 2022/10/13 MCCM初版 ADD END */
//
//    /* MM顧客情報用                                                             */
//    int            h_end_seitai_update_yyyymmdd;        /* 最終静態更新日       */
//    int            h_end_seitai_update_hh24miss;        /* 最終静態更新時刻     */
//    int            h_bath_yyyy;                         /* 誕生年               */
//    int            h_bath_mm;                           /* 誕生月               */
//    int            h_seibetsu;                               /* 性別                 */
//    String        h_kana_name[40*3+1];                 /* 顧客カナ名称         */
//    int            h_emp_kbn;                           /* 社員区分             */
//    unsigned int   h_age;                               /* 年齢                 */
//    unsigned int   h_birth_year;                        /* 誕生年               */
//    unsigned int   h_birth_day;                         /* 誕生日               */
//    unsigned int   h_marriage;                          /* 婚姻                 */
//    int            hi_mmki_sw;                          /* 追加(0)／更新(1)switch */
//
//    /* MM顧客属性情報用                                                         */
//    String        h_zip[10+1];                         /* 郵便番号             */
//    String        h_zip_code[23+1];                    /* 郵便番号コード       */
//    String        h_address1[10*3+1];                  /* 住所1                */
//    String        h_address2[80*3+1];                  /* 住所2                */
//    String        h_address3[80*3+1];                  /* 住所3                */
//    String        h_address[200*3+1];                  /* 住所                 */                                 /* 2022/10/13 MCCM初版 ADD */
//    String        h_telephone1[15+1];                  /* 電話番号1            */
//    String        h_telephone2[15+1];                  /* 電話番号2            */
//    String        h_kensaku_denwa_no_1[15+1];          /* 検索電話番号１       */
//    String        h_kensaku_denwa_no_2[15+1];          /* 検索電話番号２       */
//    String        h_email1[255+1];                     /* Eメールアドレス1     */                                 /* 2023/02/10 MCCM初版 ADD */
//    String        h_email2[60+1];                      /* Eメールアドレス2     */                                 /* 2023/02/10 MCCM初版 ADD */
//    String        h_email3[255+1];                     /* Eメールアドレス3     */                                 /* 2023/02/10 MCCM初版 ADD */
//    String        h_telephone3[15+1];                  /* 電話番号３           */
//    String        h_telephone4[15+1];                  /* 電話番号４           */
//    String        h_kensaku_denwa_no_3[15+1];          /* 検索電話番号３       */
//    String        h_kensaku_denwa_no_4[15+1];          /* 検索電話番号４       */
//    String        h_job[40*3+1];                       /* 職業                 */
//    String        h_jitaku_jusho_code[11+1];           /* 自宅住所コード       */
//    int            h_todofuken_cd;                      /* 都道府県コード       */                                 /* 2022/10/13 MCCM初版 ADD */
//    String        upd_h_zip[10+1];                     /* 更新_郵便番号        */
//    String        upd_h_zip_code[23+1];                /* 更新_郵便番号コード  */
//    String        upd_h_address1[10*3+1];              /* 更新_住所1           */
//    String        upd_h_address2[80*3+1];              /* 更新_住所2           */
//    String        upd_h_address3[80*3+1];              /* 更新_住所3           */
//    String        upd_h_address[200*3+1];              /* 更新_住所            */                                 /* 2022/10/13 MCCM初版 ADD */
//    String        upd_h_telephone1[15+1];              /* 更新_電話番号1       */
//    String        upd_h_telephone2[15+1];              /* 更新_電話番号2       */
//    String        upd_h_kensaku_denwa_no_1[15+1];      /* 更新_検索電話番号１  */
//    String        upd_h_kensaku_denwa_no_2[15+1];      /* 更新_検索電話番号２  */
//    /* 2023/02/10 MCCM初版 ADD */
//    String        upd_h_email1[255+1];                 /* 更新_Eメールアドレス1  */                               /* 2023/02/10 MCCM初版 ADD */
//    String        upd_h_email3[255+1];                 /* 更新_Eメールアドレス3  */                               /* 2023/02/10 MCCM初版 ADD */
//    /* 2023/02/10 MCCM初版 ADD */
//    int            upd_h_todofuken_cd;                  /* 更新_都道府県コード  */                                 /* 2022/10/13 MCCM初版 ADD */
//
//    /* TS利用可能ポイント情報用                                                 */
//    int            h_nyukai_kigyou_cd;                  /* 入会企業コード       */
//    int            h_nyukai_tenpo;                      /* 入会店舗             */
//    int            h_nyukai_oldcorp_cd;                 /* 入会旧販社コード     */
//    int            h_haken_kigyou_cd;                   /* 発券企業コード       */
//    int            h_haken_tenpo;                       /* 発券店舗             */
//    int            h_nyukai_kaisha_cd_mcc;              /* 入会会社コードＭＣＣ */                                 /* 2022/10/13 MCCM初版 ADD */
//    int            h_nyukai_tenpo_mcc;                  /* 入会店舗ＭＣＣ       */                                 /* 2022/10/13 MCCM初版 ADD */
//
//    /* WMバッチ処理実行管理用                                                   */
//    String        h_kinou_id[6 + 1];                   /* 機能ID               */
//    long           h_jikkouzumi_cnt;                    /* 実行済件数           */
//
//    /* MSカード情報用                                                           */
//    int            h_card_status;                       /* カードステータス     */
//    String        h_pid[16];                           /* 会員番号 [入力F]     */
//    int            h_nyukaiten_no;                      /* 入会店番号           */
//    String        h_uid[16];                           /* 顧客番号 [/SQ顧客番号発番] */
//    String        h_cdkaiin_no[16];                    /* 会員番号             */
//    int            h_service_shubetsu;                  /* サービス種別         */
//    int            h_kigyo_cd;                          /* 企業コード           */
//    int            h_cdriyu_cd;                         /* 理由コード           */
//    int            h_hakko_ymd;                         /* 発行年月日           */
//    String        h_goopon_no[17];                     /* 顧客番号 [/ＧＯＯＰＯＮ番号発番] */                     /* 2022/10/13 MCCM初版 ADD */
//
//    /* パンチデータエラー情報用                                                 */
//    int            h_moushikomiten_no;                  /* 申込店番号 [入力F]   */
//
//    /* PS店表示情報用                                                           */
//    int            h_renkei_mise_no;                    /* 連携用店番号         */
//
//    /* MM顧客企業別属性情報用                                                   */
//    int            hi_mmkg_sw;                          /* 追加(0)／更新(1)switch */
//    int            h_dm_tome_kbn;                       /* ＤＭ止め区分         */
//    int            h_email_tome_kbn=new ItemDto();                 /* Ｅメール止め区分     */
//
//    /* 処理・共通用                                                             */
    StringDto h_uid_varchar = new StringDto(15 + 1);                 /* 顧客番号             */
    ItemDto seitai_flg = new ItemDto();                       /* 静態状態有無フラグ   */
    StringDto h_saishu_koshin_programid = new StringDto(20 + 1);   /* 最終更新プログラムID */
    ItemDto h_bat_yyyymmdd = new ItemDto();                   /* バッチ処理日付(当日) */
    ItemDto h_bat_yyyymmdd_1 = new ItemDto();                 /* バッチ処理日付(前日) */
    ItemDto h_kijyun_yyyymmdd = new ItemDto();                /* 基準日               */
    ItemDto h_kijyun_yyyymm = new ItemDto();                  /* 基準年月             */
    ItemDto h_nyukai_yyyymmdd = new ItemDto();                /* 入会年月日           */
    StringDto str_sql = new StringDto(4096 * 4);                /* 実行用SQL文字列      */
    ItemDto h_count = new ItemDto();                          /* 住所１チェック用     */
    //
//    /* 使用テーブルヘッダーファイルをインクルード */
//    EXEC SQL INCLUDE    MM_KOKYAKU_INFO_DATA.h;         /* MM顧客情報                */
//    EXEC SQL INCLUDE    MM_KOKYAKU_ZOKUSE_INFO_DATA.h;  /* MM顧客属性情報            */
//    EXEC SQL INCLUDE    MM_KOKYAKU_KIGYOBETU_ZOKUSE.h;  /* MM顧客企業別属性情報      */
//    EXEC SQL INCLUDE    TM_PUNCH_DATA_ERROR_INFO.h;     /* TMパンチデータエラー情報  */
//    EXEC SQL INCLUDE    MS_KOKYAKU_SEDO_INFO_DATA.h;    /* MS顧客制度情報            */
//    EXEC SQL INCLUDE    MS_CARD_INFO.h;                 /* MSカード情報              */
//    /* 使用テーブルデータ領域設定 */
    MM_KOKYAKU_INFO_TBL MmkiData = new MM_KOKYAKU_INFO_TBL();                /* MM顧客情報                */
    MM_KOKYAKU_ZOKUSE_INFO_TBL MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();             /* MM顧客属性情報            */
    MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL MmkgData = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();        /* MM顧客企業別属性情報      */
    TM_PUNCH_DATA_ERROR_INFO_TBL tmpe_buff = new TM_PUNCH_DATA_ERROR_INFO_TBL();          /* TMパンチデータエラー情報  */
    MS_KOKYAKU_SEDO_INFO_TBL MsksData = new MS_KOKYAKU_SEDO_INFO_TBL();               /* MS顧客制度情報            */
    MS_CARD_INFO_TBL MscdData = new MS_CARD_INFO_TBL();                   /* MSカード情報              */
//
//    /* 2022/10/13 MCCM初版 ADD START */
//    /* PS会員番号体系用                                                              */
//    unsigned int     h_kyu_hansya_cd;                       /* 旧販社コード          */
//    int              h_ps_corpid;                         /* 企業コード           */
//    int              h_ps_taikei_count;
//    /* 2022/10/13 MCCM初版 ADD END */
//
//    EXEC SQL END DECLARE SECTION;
    //    EXEC SQL BEGIN DECLARE SECTION;

    int chk_arg_i;                           /* 引数-iチェック用     */
    StringDto arg_i_Value = new StringDto(256);                    /* 引数i設定値          */

    /* 入会エラーリストファイル作成用                                           */
    ItemDto h_syori_ymd = new ItemDto();                      /* 処理年月日           */
    ItemDto h_data_kubun = new ItemDto();                     /* データ区分           */
    ItemDto h_irai_ymd = new ItemDto();                       /* 依頼年月日           */
    ItemDto h_batch_no = new ItemDto();                       /* バッチ番号           */
    ItemDto h_nyukai_ymd = new ItemDto();                     /* 入会年月日           */
    ItemDto h_moushikomi_kigyou_cd = new ItemDto();           /* 申込企業コード       */
    ItemDto h_kigyou_name = new ItemDto(40 * 3 + 1);               /* 企業名称             */
    ItemDto h_moushikomisyoten_no = new ItemDto();            /* 申込書店番号         */
    ItemDto h_kanji_tenpo_name = new ItemDto(80 * 3 + 1);          /* 漢字店舗名称         */
    ItemDto h_kaiin_no = new ItemDto();                       /* 会員番号             */
    StringDto h_kaiin_name = new StringDto(80 * 3 + 1);                /* 会員氏名             */
    ItemDto h_riyuu_cd = new ItemDto();                       /* 理由コード           */
    ItemDto h_error_msg = new ItemDto(80 + 1);                 /* エラー理由           */

    /* MS顧客制度情報静態情報用(静態状態チェック用)                             */
    ItemDto h_seitai_flag = new ItemDto();                           /* 静態取込済フラグ     */
    ItemDto h_birth_month = new ItemDto();                           /* 誕生月               */
    ItemDto h_zaiseki_yyyymm = new ItemDto();                        /* 在籍開始年月         */
    ItemDto h_ec_flag = new ItemDto();                               /* ＥＣ会員フラグ       */
    ItemDto h_tel_flag = new ItemDto();                              /* 電話番号登録フラグ   */
    ItemDto hi_msks_sw = new ItemDto();                              /* 追加(0)／更新(1)switch */
    /* 2022/10/13 MCCM初版 ADD START */
    ItemDto h_mcc_seido_kyodaku_flg = new ItemDto();                 /* ＭＣＣ制度許諾フラグ   */
    ItemDto h_mcc_seido_kyodaku_koshinsha = new ItemDto();           /* ＭＣＣ制度許諾更新者   */
    ItemDto h_mcc_seido_kyodaku_koshin_ymdhms = new ItemDto();   /* ＭＣＣ制度許諾更新日時 */
    /* 2022/10/13 MCCM初版 ADD END */

    /* MM顧客情報用                                                             */
    ItemDto h_end_seitai_update_yyyymmdd = new ItemDto();            /* 最終静態更新日       */
    ItemDto h_end_seitai_update_hh24miss = new ItemDto();            /* 最終静態更新時刻     */
    ItemDto h_bath_yyyy = new ItemDto();                             /* 誕生年               */
    ItemDto h_bath_mm = new ItemDto();                               /* 誕生月               */
    ItemDto h_seibetsu = new ItemDto();                                   /* 性別                 */
    StringDto h_kana_name = new StringDto(40 * 3 + 1);                 /* 顧客カナ名称         */
    ItemDto h_emp_kbn = new ItemDto();                               /* 社員区分             */
    ItemDto h_age = new ItemDto();                                   /* 年齢                 */
    ItemDto h_birth_year = new ItemDto();                            /* 誕生年               */
    ItemDto h_birth_day = new ItemDto();                             /* 誕生日               */
    ItemDto h_marriage = new ItemDto();                              /* 婚姻                 */
    ItemDto hi_mmki_sw = new ItemDto();                              /* 追加(0)／更新(1)switch */

    /* MM顧客属性情報用                                                         */
    StringDto h_zip = new StringDto();                     /* 郵便番号             */
    StringDto h_zip_code = new StringDto(23 + 1);                    /* 郵便番号コード       */
    StringDto h_address1 = new StringDto(10 * 3 + 1);                  /* 住所1                */
    StringDto h_address2 = new StringDto(80 * 3 + 1);                  /* 住所2                */
    StringDto h_address3 = new StringDto(80 * 3 + 1);                  /* 住所3                */
    StringDto h_address = new StringDto(200 * 3 + 1);                  /* 住所                 */                                 /* 2022/10/13 MCCM初版 ADD */
    StringDto h_telephone1 = new StringDto(15 + 1);                  /* 電話番号1            */
    StringDto h_telephone2 = new StringDto(15 + 1);                  /* 電話番号2            */
    StringDto h_kensaku_denwa_no_1 = new StringDto(15 + 1);          /* 検索電話番号１       */
    StringDto h_kensaku_denwa_no_2 = new StringDto(15 + 1);          /* 検索電話番号２       */
    StringDto h_email1 = new StringDto(255 + 1);                     /* Eメールアドレス1     */                                 /* 2023/02/10 MCCM初版 ADD */
    StringDto h_email2 = new StringDto(60 + 1);                      /* Eメールアドレス2     */                                 /* 2023/02/10 MCCM初版 ADD */
    StringDto h_email3 = new StringDto(255 + 1);                     /* Eメールアドレス3     */                                 /* 2023/02/10 MCCM初版 ADD */
    StringDto h_telephone3 = new StringDto(15 + 1);                  /* 電話番号３           */
    StringDto h_telephone4 = new StringDto(15 + 1);                  /* 電話番号４           */
    StringDto h_kensaku_denwa_no_3 = new StringDto(15 + 1);          /* 検索電話番号３       */
    StringDto h_kensaku_denwa_no_4 = new StringDto(15 + 1);          /* 検索電話番号４       */
    StringDto h_job = new StringDto(40 * 3 + 1);                       /* 職業                 */
    StringDto h_jitaku_jusho_code = new StringDto(11 + 1);           /* 自宅住所コード       */
    ItemDto h_todofuken_cd = new ItemDto();                      /* 都道府県コード       */                                 /* 2022/10/13 MCCM初版 ADD */
    StringDto upd_h_zip = new StringDto(10 + 1);                     /* 更新_郵便番号        */
    StringDto upd_h_zip_code = new StringDto(23 + 1);                /* 更新_郵便番号コード  */
    StringDto upd_h_address1 = new StringDto(10 * 3 + 1);              /* 更新_住所1           */
    StringDto upd_h_address2 = new StringDto(80 * 3 + 1);              /* 更新_住所2           */
    StringDto upd_h_address3 = new StringDto(80 * 3 + 1);              /* 更新_住所3           */
    StringDto upd_h_address = new StringDto(200 * 3 + 1);              /* 更新_住所            */                                 /* 2022/10/13 MCCM初版 ADD */
    StringDto upd_h_telephone1 = new StringDto(15 + 1);              /* 更新_電話番号1       */
    StringDto upd_h_telephone2 = new StringDto(15 + 1);              /* 更新_電話番号2       */
    StringDto upd_h_kensaku_denwa_no_1 = new StringDto(15 + 1);      /* 更新_検索電話番号１  */
    StringDto upd_h_kensaku_denwa_no_2 = new StringDto(15 + 1);      /* 更新_検索電話番号２  */
    /* 2023/02/10 MCCM初版 ADD */
    StringDto upd_h_email1 = new StringDto(255 + 1);                 /* 更新_Eメールアドレス1  */                               /* 2023/02/10 MCCM初版 ADD */
    StringDto upd_h_email3 = new StringDto(255 + 1);                 /* 更新_Eメールアドレス3  */                               /* 2023/02/10 MCCM初版 ADD */
    /* 2023/02/10 MCCM初版 ADD */
    ItemDto upd_h_todofuken_cd = new ItemDto();                  /* 更新_都道府県コード  */                                 /* 2022/10/13 MCCM初版 ADD */

    /* TS利用可能ポイント情報用                                                 */
    IntegerDto h_nyukai_kigyou_cd = new IntegerDto();                  /* 入会企業コード       */
    ItemDto h_nyukai_tenpo = new ItemDto();                      /* 入会店舗             */
    IntegerDto h_nyukai_oldcorp_cd = new IntegerDto();                 /* 入会旧販社コード     */
    ItemDto h_haken_kigyou_cd = new ItemDto();                   /* 発券企業コード       */
    ItemDto h_haken_tenpo = new ItemDto();                       /* 発券店舗             */
    ItemDto h_nyukai_kaisha_cd_mcc = new ItemDto();              /* 入会会社コードＭＣＣ */                                 /* 2022/10/13 MCCM初版 ADD */
    ItemDto h_nyukai_tenpo_mcc = new ItemDto();                  /* 入会店舗ＭＣＣ       */                                 /* 2022/10/13 MCCM初版 ADD */

    /* WMバッチ処理実行管理用                                                   */
    ItemDto h_kinou_id = new ItemDto(6 + 1);                   /* 機能ID               */
    ItemDto h_jikkouzumi_cnt = new ItemDto();                    /* 実行済件数           */

    /* MSカード情報用                                                           */
    ItemDto h_card_status = new ItemDto();
    ItemDto h_pid = new ItemDto(16);
    ItemDto h_nyukaiten_no = new ItemDto();                      /* 入会店番号           */
    StringDto h_uid = new StringDto(16);
    ItemDto h_cdkaiin_no = null;                    /* 会員番号             */
    ItemDto h_service_shubetsu = new ItemDto();                  /* サービス種別         */
    ItemDto h_kigyo_cd = new ItemDto();                          /* 企業コード           */
    ItemDto h_cdriyu_cd = new ItemDto();                         /* 理由コード           */
    ItemDto h_hakko_ymd = new ItemDto();                         /* 発行年月日           */
    ItemDto h_goopon_no = new ItemDto(17);                                    /* 2022/10/13 MCCM初版 ADD */

    /* パンチデータエラー情報用                                                 */
    ItemDto h_moushikomiten_no = new ItemDto();                  /* 申込店番号 = new StringDto(入力F)   */

    /* PS店表示情報用                                                           */
    ItemDto h_renkei_mise_no = new ItemDto();                   /* 連携用店番号         */

    /* MM顧客企業別属性情報用                                                   */
    ItemDto hi_mmkg_sw = new ItemDto();                          /* 追加(0)／更新(1)switch */
    ItemDto h_dm_tome_kbn = new ItemDto();                       /* ＤＭ止め区分         */
    ItemDto h_email_tome_kbn = new ItemDto();                    /* Ｅメール止め区分     */

    /* 2022/10/13 MCCM初版 ADD START */
    /* PS会員番号体系用                                                              */
    IntegerDto h_kyu_hansya_cd = new IntegerDto();                       /* 旧販社コード          */
    IntegerDto h_ps_corpid = new IntegerDto();                         /* 企業コード           */
    IntegerDto h_ps_taikei_count = new IntegerDto();

    /*-------------------------------------------------------------------------------*/
    /*  入出力ファイル                                                               */
    /*-------------------------------------------------------------------------------*/
    /* 出力ファイル */
    FileStatusDto fp_in;                         /* CRM用ファイルポインタ */
    FileStatusDto fp_out;                        /* PC_月別用ファイルポインタ */

    /*-------------------------------------------------------------------------------*/
    /*  変数定義                                                                     */
    /*-------------------------------------------------------------------------------*/
    StringDto bat_yyyymmdd = new StringDto(DEF_BUFSIZE8 + 1);        /* バッチ処理日付(当日) */
    StringDto bat_yyyymmdd_1 = new StringDto(DEF_BUFSIZE8 + 1);      /* バッチ処理日付(前日) */

    StringDto bat_yyyy = new StringDto(DEF_BUFSIZE4 + 1);            /* バッチ処理日付(当日)の年 */
    StringDto bat_mm = new StringDto(DEF_BUFSIZE2 + 1);              /* バッチ処理日付(前日)の日 */

    StringDto system_yyyymmdd = new StringDto(DEF_BUFSIZE8 + 1);     /* システム年月日 */
    StringDto system_hhmmss = new StringDto(DEF_BUFSIZE6 + 1);       /* システム時分秒 */

    long bat_shori_yyyymmdd;                    /* バッチ処理年月日(当日) */
    long bat_shori_yyyymmdd_1;                  /* バッチ処理年月日(前日) */

    int input_data_cnt;                         /* 入力データ件数 */
    int ok_data_cnt;                            /* 正常処理データ件数 */
    int ng_data_cnt;                            /* エラーデータ件数 */
    int mscd_data_cnt;                          /* MSカード情報更新件数 */
    int mmpd_data_cnt;                          /* MM顧客情報更新件数 */
    int msps_data_cnt;                          /* MS顧客制度情報更新件数 */
    int mmpz_data_cnt;                          /* MM顧客属性情報更新件数 */
    int punch_data_cnt;                         /* TMパンチエラーデータ情報更新件数 */
    int mmcp_data_cnt;                          /* MM顧客企業別属性情報更新件数 */
    int tsup_data_cnt;                          /* TS利用可能ポイント情報更新件数 */
    int syorizumi_cnt;                          /* 前回処理済み件数 */

    StringDto path_in = new StringDto(DEF_BUFSIZE256);                /* 入力ファイルのディレクトリ＋ファイル名 */
    StringDto path_out = new StringDto(DEF_BUFSIZE256);               /* 出力ファイルのディレクトリ＋ファイル名 */

    StringDto out_format_buf = new StringDto(DEF_BUFSIZE4K);          /* APログフォーマット */
    StringDto in_dir = new StringDto(DEF_BUFSIZE4K);          /* 入力ファイルディレクトリ */
    StringDto ap_work_dir = new StringDto(DEF_BUFSIZE4K);             /* 出力ファイルディレクトリ */

    int card_flag;                              /* カード新旧フラグ */
    int uid_flg;                                /* 顧客データ有無フラグ */

    StringDto wk_fullspace = new StringDto(80 * 3 + 1);               /* 全角スペースワーク */
    IntegerDto wk_fullspace_len = new IntegerDto();                       /* 全角スペースサイズ */

    StringDto bt_date_yyyy = new StringDto(5);                        /* バッチ処理年(年齢算出用) */
    StringDto bt_date_mm = new StringDto(3);                          /* バッチ処理月(年齢算出用) */
    StringDto bt_date_d = new StringDto(3);                           /* バッチ処理日(年齢算出用) */

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
        int rtn_cd;                    /* 関数戻り値 */
        IntegerDto rtn_status = new IntegerDto();               /* 関数ステータス */
        int arg_cnt;                   /* 引数チェック用カウンタ */
        String env_input;                /* 入力ファイルDIR */
        String env_output;               /* 出力ファイルDIR */
        String arg_Work1 = null; /* Work Buffer1 */
        DBG_LOG = true;

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


        /* HOST変数にプログラム名をコピー */
        memset(h_saishu_koshin_programid, 0x00, sizeof(h_saishu_koshin_programid));
        memcpy(h_saishu_koshin_programid, Cg_Program_Name, sizeof(Cg_Program_Name));

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

        /** 変数初期化 **/
        chk_arg_i = DEF_OFF;
        memset(arg_i_Value, 0x00, sizeof(arg_i_Value));

        /*-----------------------------------------------*/
        /*  入力引数チェック                             */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgStart("*** main処理 ***");
        }

        /* 初期化 */
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (DBG_LOG) {
            C_DbgMsg("*** main *** 入力引数チェック%s\n", "START");
        }

        /*** 引数チェック ***/

        /* 引数の個数分、ループする */
        for (arg_cnt = 1; arg_cnt < argc; arg_cnt++) {
            arg_Work1 = strcpy(arg_Work1, argv[arg_cnt]);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
            /* デバッグモードの指定 */
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else if (memcmp(arg_Work1, DEF_ARG_I, 2) == 0) { /* -iの場合         */
                rtn_cd = cmBTcentB_Chk_Arg(arg_Work1);    /* パラメータチェック */
                if (rtn_cd == C_const_OK) {
                    strcpy(arg_i_Value, arg_Work1.substring(2));
                } else {
                    sprintf(out_format_buf, "-i 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            }
            /* 規定外パラメータ  */
            else {
                rtn_cd = C_const_NG;
                sprintf(out_format_buf, "定義外の引数（%s）", arg_Work1);
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = [%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            /* パラメータのチェック結果がNG */
            if (rtn_cd != C_const_OK) {
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                rtn_cd = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
        }

        /* 必須パラメータ未指定チェック */
        if (chk_arg_i == DEF_OFF) {
            sprintf(out_format_buf, "-i 引数の値が不正です");
            APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
            rtn_cd = C_EndBatDbg();                         /* バッチデバッグ終了 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  WMバッチ処理実行管理に使用する機能ＩＤ設定   */
        /*-----------------------------------------------*/
        memset(h_kinou_id, 0x00, sizeof(h_kinou_id));
        strcpy(h_kinou_id, DEF_KINOU_ID);

        /*-----------------------------------------------*/
        /*  DBコネクト                                   */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** DBコネクト%s\n", "");
        }
        rtn_cd = C_OraDBConnect(BT_aplcomService.C_ORACONN_MD, rtn_status);

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
        /*  バッチ処理日取得(当日指定)                   */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得(当日指定)%s\n", "");
        }

        memset(bat_yyyymmdd, 0x00, sizeof(bat_yyyymmdd));

        // 当日を指定
        rtn_cd = C_GetBatDate(0, bat_yyyymmdd, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得(当日指定)OK [%s]\n", bat_yyyymmdd);
        }

        h_bat_yyyymmdd.arr = atol(bat_yyyymmdd);
        bat_shori_yyyymmdd = atol(bat_yyyymmdd);

        /* バッチ処理日付(当日)の年 */
        memset(bat_yyyy, 0x00, sizeof(bat_yyyy));
        strncpy(bat_yyyy, bat_yyyymmdd.arr, 4);

        /* バッチ処理日付(当日)の日 */
        memset(bat_mm, 0x00, sizeof(bat_mm));
        strncpy(bat_mm, bat_yyyymmdd.arr.substring(4), 2);

        if (DBG_LOG) {
            C_DbgMsg("*** バッチ処理日(当日指定)年月日 =[%d]\n", bat_shori_yyyymmdd);
        }

        /*-----------------------------------------------*/
        /*  バッチ処理日取得(前日指定)                   */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得(前日指定)%s\n", "");
        }

        memset(bat_yyyymmdd_1, 0x00, sizeof(bat_yyyymmdd_1));

        // 当日を指定
        rtn_cd = C_GetBatDate(-1, bat_yyyymmdd_1, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得(前日指定)NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得(前日指定)NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得(前日指定)OK [%s]\n", bat_yyyymmdd_1);
        }

        h_bat_yyyymmdd_1.arr = (int) atol(bat_yyyymmdd_1);
        bat_shori_yyyymmdd_1 = atol(bat_yyyymmdd_1);

        if (DBG_LOG) {
            C_DbgMsg("*** バッチ処理日(前日指定)年月日 =[%d]\n", bat_shori_yyyymmdd_1);
        }

        /*-----------------------------------------------*/
        /*  環境変数取得                                 */
        /*-----------------------------------------------*/
        if (DBG_LOG) {
            C_DbgMsg("*** main *** 環境変数取得（入力ファイルDIR）%s\n", "");
        }
        env_input = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_input)) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** 環境変数取得NG（入力ファイルDIR）%s\n", "getenv()");
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** 環境変数取得（出力ファイルDIR）%s\n", "");
        }
        env_output = getenv("CM_APWORK_DATE");
        if (StringUtils.isEmpty(env_output)) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** 環境変数取得NG（出力ファイルDIR）%s\n", "getenv()");
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "getenv(CM_APWORK_DATE)", 0, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /* 入力ファイル用デレクトリ設定 */
        memset(in_dir, 0x00, sizeof(in_dir));
        strcpy(in_dir, env_input);

        /* 出力ファイル用デレクトリ設定 */
        memset(ap_work_dir, 0x00, sizeof(ap_work_dir));
        strcpy(ap_work_dir, env_output);

        if (DBG_LOG) {
            C_DbgMsg("*** main *** 環境変数取得OK（入力ファイルDIR）%s\n", in_dir);
            C_DbgMsg("*** main *** 環境変数取得OK（出力ファイルDIR）%s\n", ap_work_dir);
        }

        /*-----------------------------------------------*/
        /*  入力ファイル名を設定する                     */
        /*-----------------------------------------------*/
        /* ファイル名を作成 */
        memset(path_in, 0x00, sizeof(path_in));
        strcpy(path_in, in_dir);
        strcat(path_in, "/");
        strcat(path_in, arg_i_Value);

        /*-----------------------------------------------*/
        /*  出力ファイル名を設定する                     */
        /*-----------------------------------------------*/
        /* ファイル名を作成 */
        memset(path_out, 0x00, sizeof(path_out));
        strcpy(path_out, ap_work_dir);
        strcat(path_out, "/");
        strcat(path_out, bat_yyyymmdd);
        strcat(path_out, DEF_OUTPUT_FILENAME);

        /*-----------------------------------------------*/
        /*  データ件数初期化                             */
        /*-----------------------------------------------*/
        input_data_cnt = 0;                                /* 入力データ件数 */
        ok_data_cnt = 0;                                /* 正常処理データ件数 */
        ng_data_cnt = 0;                                /* エラーデータ件数 */
        mscd_data_cnt = 0;                                /* MSカード情報更新件数 */
        mmpd_data_cnt = 0;                                /* MM顧客情報更新件数 */
        msps_data_cnt = 0;                                /* MS顧客制度情報更新件数 */
        mmpz_data_cnt = 0;                                /* MM顧客属性情報更新件数 */
        punch_data_cnt = 0;                                /* TMパンチエラーデータ情報更新件数 */
        mmcp_data_cnt = 0;                                /* MM顧客企業別属性情報更新件数 */
        tsup_data_cnt = 0;                                /* TS利用可能ポイント情報更新件数 */

        /*-----------------------------------------------*/
        /*  入力ファイル名をオープンする                 */
        /*-----------------------------------------------*/
        /* ファイルをオープン */
//        if((fp_in = fopen(path_in, "r" )) == 0x00){
        if ((fp_in = fopen(path_in.arr, SystemConstant.Shift_JIS,FileOpenType.r)).fd == C_const_NG) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** OpenFile *** 入力ファイルオープンNG%s\n", "");
                C_DbgMsg("path_in:%s\n", path_in);
            }
            sprintf(out_format_buf, "fopen（%s）", path_in);
            APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, fp_in, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** OpenFile *** 入力ファイルオープン[%s]\n", path_in);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd = cmBTcentB_main();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** cmBTcentB_main NG rtn =[%d]\n", rtn_cd);
            }
            APLOG_WT(DEF_MSG_ID_912, 0, null, "カード入会連動処理に失敗しました", 0, 0, 0, 0, 0);

            /* 出力ファイルがオープンされていれば、クローズ */
            if (fp_in != null) {
                fclose(fp_in);
            }

            if (fp_out != null) {
                fclose(fp_out);
            }

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*-----------------------------------------------*/
        /*  終了処理                                     */
        /*-----------------------------------------------*/
        /* 処理結果をログに出力 */

        /* 入力テーブルデータ件数 */
        if (syorizumi_cnt == 0) {
            C_APLogWrite(DEF_MSG_ID_104, "入力ファイル名：[%s] 入力データ件数：[%d] 正常処理件数：[%d] エラー件数：[%d]", " ", arg_i_Value, input_data_cnt, ok_data_cnt, ng_data_cnt, 0, 0);
        } else {
            C_APLogWrite(DEF_MSG_ID_108, "入力ファイル名：[%s] 入力データ件数：[%d] 前回処理件数：[%d] 正常処理件数：[%d] エラー件数：[%d]", " ", arg_i_Value, input_data_cnt, syorizumi_cnt, ok_data_cnt, ng_data_cnt, 0);
        }

        /* MSカード情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MSカード情報", mscd_data_cnt, 0, 0, 0, 0);

        /* MM顧客情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客情報", mmpd_data_cnt, 0, 0, 0, 0);

        /* MS顧客制度情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MS顧客制度情報", msps_data_cnt, 0, 0, 0, 0);

        /* MM顧客属性情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客属性情報", mmpz_data_cnt, 0, 0, 0, 0);

        /* MM顧客企業別属性情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客企業別属性情報", mmcp_data_cnt, 0, 0, 0, 0);

        /* TS利用可能ポイント情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "TS利用可能ポイント情報", tsup_data_cnt, 0, 0, 0, 0);

        /* TMパンチエラーデータ情報 */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "TMパンチエラーデータ情報", punch_data_cnt, 0, 0, 0, 0);

        if (DBG_LOG) {
            C_DbgEnd("*** main処理 ***", 0, 0, 0);
        }

        /* ファイルがオープンされていれば、クローズ */
        if (fp_in != null) {
            fclose(fp_in);
        }

        if (fp_out != null) {
            fclose(fp_out);
        }

        /* 実行済件数を設定 */
        h_jikkouzumi_cnt.arr = 0;

        /* WMバッチ処理実行管理を更新 */
        rtn_cd = WM_Batch_Update();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** WM_Batch_Update() NG rtn =[%d]\n", rtn_cd);
            }

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* バッチデバッグ終了処理 */
            rtn_cd = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*  終了メッセージ */
        APLOG_WT(DEF_MSG_ID_103, 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd = C_EndBatDbg();

        /* コミット解放処理 */
//        EXEC SQL COMMIT WORK RELEASE;
        sqlcaManager.commitRelease();

        if (ng_data_cnt > 0) {
            /* 警告終了 */
            return exit(C_const_APWR);
        } else {
            /* 正常終了 */
            return exit(C_const_APOK);
        }
    }

    /* **************************************************************************** */
    /*                                                                            */
    /*  関数名 ： cmBTcentB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcentB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード入会連動主処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int cmBTcentB_main() {
        int rtn_cd;             /* 関数戻り値 */

        if (DBG_LOG) {
            C_DbgStart("*** cmBTcentB_main *** カード入会連動主処理");
        }

        /*-----------------------------------------------*/
        /*  入会連動データ登録処理                       */
        /*-----------------------------------------------*/
        /*入会連動データ登録 */
        rtn_cd = Update_Data();

        if (rtn_cd == C_const_NG) {
            /* APログ出力 */
            APLOG_WT(DEF_MSG_ID_912, 0, null, "カード入会連動処理に失敗しました", 0, 0, 0, 0, 0);

            /* 処理終了 */
            return (C_const_NG);
        }

        /*-----------------------------------------------*/
        /*  出力ファイル名をオープンする                 */
        /*-----------------------------------------------*/
        /* ファイルをオープン */
//        if((fp_out = fopen(path_out, "a" )) == 0x00){
        if ((fp_out = open(path_out.arr)).fd == -1) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** OpenFile *** 出力ファイルオープンNG%s\n", "");
                C_DbgMsg("path_out:%s\n", path_out);
            }
            sprintf(out_format_buf, "fopen（%s）", path_out);
            APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, fp_out, 0, 0, 0, 0);

            /* 処理終了 */
            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** OpenFile *** 出力ファイルオープン[%s]\n", path_out);
        }

        /*-----------------------------------------------*/
        /*  入会エラーリスト作成処理                     */
        /*-----------------------------------------------*/
        /* 入会エラーリストファイル作成処理 */
        rtn_cd = Write_ErrorList_File();

        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** cmBTcentB_main NG rtn =[%d]\n", rtn_cd);
            }
            APLOG_WT(DEF_MSG_ID_912, 0, null, "入会エラーリストファイル作成に失敗しました", 0, 0, 0, 0, 0);

            /* 処理終了 */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            C_DbgEnd("*** cmBTcentB_main *** カード入会連動主処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： Update_Data()                                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Update_Data()                                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               入会連動データ登録処理                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int Update_Data() {
        int rtn_cd;                     /* 関数戻り値 */
        int punch_errno = 0;                /* パンチエラー番号 */
        int lock_sbt;                   /* ロック種別 */
        StringDto wk_lock = new StringDto();                 /* ロック種別 */
        IntegerDto rtn_status;                 /* 関数ステータス */
        StringDto wk_kaiin_no = new StringDto(16);        /* 会員番号ワーク */
        StringDto wk_moushikomi_date = new StringDto(9);
        ;  /* 申込年月日ワーク */
        String wk_buf = null;                /* ワークバッファ */

        if (DBG_LOG) {
            C_DbgStart("*** Update_Data *** カード入会連動主処理");
        }

        /* 初期化 */
        h_jikkouzumi_cnt.arr = 0;
        memset(wk_kaiin_no, 0x00, sizeof(wk_kaiin_no));
        memset(wk_moushikomi_date, 0x00, sizeof(wk_moushikomi_date));

        /* WMバッチ処理実行管理更新処理 */
//            EXEC SQL SELECT シーケンス番号
//            INTO :h_jikkouzumi_cnt
//            FROM WMバッチ処理実行管理
//            WHERE 機能ＩＤ = :h_kinou_id;
        sqlca.sql = new StringDto("SELECT シーケンス番号 FROM WMバッチ処理実行管理  WHERE 機能ＩＤ = '" + h_kinou_id + "'");
        sqlca.restAndExecute();
        sqlca.fetch();
        sqlca.recData(h_jikkouzumi_cnt);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode,
                    "WMバッチ処理実行管理",
                    h_kinou_id, 0, 0);

            /* 処理をNGで終了 */
            return (C_const_NG);
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* 実行済件数を設定 */
            h_jikkouzumi_cnt.arr = 0;
        }

        /* 前回処理件数を設定 */
        syorizumi_cnt = h_jikkouzumi_cnt.intVal();

        /* ファイルの先頭16バイトは無視 */
        memset(wk_buf, 0x00, sizeof(wk_buf));
        wk_buf = fread(wk_buf, 16, 1, fp_in);

        while (true) {
            /* 構造体にデータを格納 */
            if ((rtn_cd = ReadFile()) == DEF_Read_EOF) {
                break;
            } else if (rtn_cd != C_const_OK) {
                return C_const_NG;
            }

            /* 入力データ件数をカウントアップ */
            input_data_cnt++;

            sprintf(in_nyukai_rendo.dm_kbn, "%s", "0");

            /* 実行済件数分読み飛ばす */
            if (syorizumi_cnt > 0) {
                if (input_data_cnt <= syorizumi_cnt) {
                    continue;
                }
            }

            /* WMバッチ処理実行管理を更新 */
            rtn_cd = WM_Batch_Update();

            if (rtn_cd != C_const_OK) {
                /* 処理をNGで終了 */
                return (C_const_NG);
            }

            /* コミットを実行 */
//                EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* 実行済み件数をカウントアップ */
            h_jikkouzumi_cnt.arr = h_jikkouzumi_cnt.intVal() + 1;

            /* 1桁目が「2」でない場合は次レコード取得 */
            if (strncmp(in_nyukai_rendo.recode_type.strVal(), REC_TYPE_DATA, 1) == 1) {
                ok_data_cnt++;
                continue;
            }

            /* 会員番号 */
            strncpy(wk_kaiin_no, in_nyukai_rendo.kaiin_no.strVal(), sizeof(wk_kaiin_no) - 1);
            h_kaiin_no.arr = atol(wk_kaiin_no);

            /* 申込年月日 */
            strncpy(wk_moushikomi_date, in_nyukai_rendo.moushikomi_date.strVal(), sizeof(wk_moushikomi_date) - 1);
            h_kijyun_yyyymmdd.arr = atoi(wk_moushikomi_date);
            /* 基準年月を設定 */
            h_kijyun_yyyymm.arr = h_kijyun_yyyymmdd.intVal() / 100;
            if (h_kijyun_yyyymm.intVal() < 201304) {
                h_kijyun_yyyymm.arr = 201304;
            } else if (h_kijyun_yyyymmdd.intVal() > h_bat_yyyymmdd_1.intVal()) {
                h_kijyun_yyyymm.arr = h_bat_yyyymmdd_1.intVal() / 100;
            }

            /* カード情報チェック */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** カード情報チェック%s\n", "");
                C_DbgMsg("基準日        =[%d]\n", h_kijyun_yyyymmdd);
                C_DbgMsg("基準年月      =[%d]\n", h_kijyun_yyyymm);
            }
            rtn_cd = CheckMscard(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** カード情報チェックNG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "入力連動データ登録チェックに失敗しました", 0, 0, 0, 0, 0);

                /* 処理をNGで終了 */
                return (C_const_NG);
            }

            /* 2022/10/13 MCCM初版 ADD START */
            h_ps_corpid.arr = 0;
            h_kyu_hansya_cd.arr = 0;
            /*---PS会員番号体系テーブルから旧販社コード取得---*/
            /* ＳＱＬを実行する */
//                EXEC SQL SELECT
//                NVL(旧販社コード,0)
//                        ,企業コード
//                INTO :h_kyu_hansya_cd,
//                           :h_ps_corpid
//                        FROM
//                PS会員番号体系@CMSD
//                        WHERE
//                会員番号開始 <= :h_kaiin_no
//                AND 会員番号終了 >= :h_kaiin_no
//                AND サービス種別 = 1;
            sqlca.sql = new StringDto("SELECT NVL(旧販社コード,0), 企業コード FROM PS会員番号体系 WHERE 会員番号開始 <= ? AND 会員番号終了 >= ? AND サービス種別 = 1");
            sqlca.restAndExecute(h_kaiin_no, h_kaiin_no);
            sqlca.fetch();
            sqlca.recData(h_kyu_hansya_cd, h_ps_corpid);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "PS会員番号体系", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 企業コードと旧販社コードが取得出来ているか判定する */
            if (h_kyu_hansya_cd.arr == 0 || h_ps_corpid.arr == 0) {
                /* パンチデータの登録 */
                rtn_cd = OutPunchdata(C_PUNCH_ERRORNO1);

                /* パンチデータの登録がが正常終了しているか判定 */
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return (C_const_NG);
                }
                /* 処理をNGで終了 */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* 次の行へ行く */
                continue;
            }

            if (DBG_LOG) {
                C_DbgMsg("旧販社コード        =[%d]\n", h_kyu_hansya_cd);
                C_DbgMsg("企業コード          =[%d]\n", h_ps_corpid);
            }
            /* 2022/10/13 MCCM初版 ADD END */

            /*--------------------*/
            /*---退会済チェック---*/
            /*--------------------*/
            /*---MM顧客企業別属性情報検索処理---*/
            /* 初期化 */
            MmkgData = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
//                memset(MmkgData, 0x00, sizeof(MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL));
            /* ＳＱＬを実行する */
            /* 2015/07/30 仮退会チェック追加 */
//                EXEC SQL SELECT  企業コード,
//                        入会年月日,
//                        退会年月日,
//                        ＤＭ止め区分,
//                        Ｅメール止め区分,
//                        仮退会年月日
//                INTO :MmkgData.kigyo_cd,
//                    :MmkgData.nyukai_ymd,
//                    :MmkgData.taikai_ymd,
//                    :MmkgData.dm_tome_kbn,
//                    :MmkgData.email_tome_kbn,
//                    :MmkgData.kari_taikai_ymd
//                FROM  (
//                        SELECT  企業コード,
//                        入会年月日,
//                        退会年月日,
//                        ＤＭ止め区分,
//                        Ｅメール止め区分,
//                        仮退会年月日
//                        FROM    MM顧客企業別属性情報
//                        WHERE   顧客番号   = :h_uid
//                AND   企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060)
//                ORDER BY
//                企業コード
//                      ) T
//                WHERE ROWNUM = 1;

            sqlca.sql = new StringDto("SELECT  企業コード,入会年月日,退会年月日,ＤＭ止め区分,Ｅメール止め区分,仮退会年月日 FROM  (\n" +
                    "        SELECT  企業コード,\n" +
                    "        入会年月日,\n" +
                    "        退会年月日,\n" +
                    "        ＤＭ止め区分,\n" +
                    "        Ｅメール止め区分,\n" +
                    "        仮退会年月日\n" +
                    "        FROM    MM顧客企業別属性情報\n" +
                    "        WHERE   顧客番号   = ?\n" +
                    "AND   企業コード NOT IN (1020,1040,3010,3050,3020,3040,3060)\n" +
                    "ORDER BY 企業コード LIMIT 1) T\n" +
                    "");
            sqlca.restAndExecute(h_uid);
            sqlca.fetch();
            sqlca.recData(MmkgData.kigyo_cd, MmkgData.nyukai_ymd, MmkgData.taikai_ymd, MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn, MmkgData.kari_taikai_ymd);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode == C_const_Ora_OK) {
                hi_mmkg_sw.arr = 1; /* データ有:Switch Update */
                if (MmkgData.taikai_ymd.intVal() != 0) {
                    /* ロールバック処理呼び出し*/
//                        EXEC SQL ROLLBACK;
                    sqlca.rollback();
                    if (DBG_LOG) {
                        sprintf(out_format_buf, "顧客番号=[%s]、企業コード=[%d]", h_uid, MmkgData.kigyo_cd);
                        C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8050(退会済みエラー)[%s]\n", out_format_buf);
                    }
                    /* ８０５０（退会済みエラー）パンチエラーデータ出力 */
                    rtn_cd = OutPunchdata(C_PUNCH_ERRORNO5);
                    if (rtn_cd != C_const_OK) {
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);
                        /* 処理を終了する */
                        return (C_const_NG);
                    }
                    /* 処理をNGで終了 */
//                        EXEC SQL COMMIT WORK;
                    sqlca.commit();
                    /* 次の行へ行く */
                    continue;
                }
                /* 2015/07/30 仮退会チェック追加 */
                else if (MmkgData.kari_taikai_ymd.intVal() != 0) {
                    /* ロールバック処理呼び出し*/
//                        EXEC SQL ROLLBACK;
                    sqlca.rollback();
                    if (DBG_LOG) {
                        sprintf(out_format_buf, "顧客番号=[%s]、企業コード=[%d]", h_uid, MmkgData.kigyo_cd);
                        C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8060(仮退会済みエラー)[%s]\n", out_format_buf);
                    }
                    /* ８０６０（仮退会済みエラー）パンチエラーデータ出力 */
                    rtn_cd = OutPunchdata(C_PUNCH_ERRORNO6);
                    if (rtn_cd != C_const_OK) {
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);
                        /* 処理を終了する */
                        return (C_const_NG);
                    }
                    /* 処理をNGで終了 */
//                        EXEC SQL COMMIT WORK;
                    sqlca.commit();
                    /* 次の行へ行く */
                    continue;
                }
            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                hi_mmkg_sw.arr = 0;
            } /* データ無:Switch Insert */ else { /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", (long) sqlca.sqlcode,
                        "MM顧客企業別属性情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }

            /* 2022/10/13 MCCM初版 ADD START */
            /*---MSカード情報検索処理---*/
            /* 初期化 */
            MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
//                memset(MmkzData, 0x00, sizeof(MM_KOKYAKU_ZOKUSE_INFO_TBL));
            /* ＳＱＬを実行する */
//                EXEC SQL SELECT
//                T1.顧客番号
//                INTO :MmkzData.kokyaku_no
//                        FROM
//                MM顧客属性情報 T1,MM顧客情報 T2
//                        WHERE
//                T1.顧客番号 = T2.顧客番号
//                AND T2.属性管理主体システム = 2
//                AND EXISTS (SELECT 1
//                FROM
//                MSカード情報@CMSD T3
//                WHERE
//                会員番号 = :h_kaiin_no
//                        AND
//                T3.顧客番号 = T1.顧客番号
//                AND T3.サービス種別 = 1
//                              );

            sqlca.sql = new StringDto("SELECT T1.顧客番号 FROM MM顧客属性情報 T1,MM顧客情報 T2 WHERE\n" +
                    "T1.顧客番号 = T2.顧客番号\n" +
                    "AND T2.属性管理主体システム = 2\n" +
                    "AND EXISTS (SELECT 1 FROM MSカード情報 T3\n" +
                    "WHERE 会員番号 = ? AND T3.顧客番号 = T1.顧客番号\n" +
                    "AND T3.サービス種別 = 1)");
            sqlca.restAndExecute(h_kaiin_no);
            sqlca.fetch();
            sqlca.recData(MmkzData.kokyaku_no);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode == C_const_Ora_OK) {

                /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                sqlca.rollback();
                if (DBG_LOG) {
                    sprintf(out_format_buf, "顧客番号=[%s]、会員番号=[%d]", MmkzData.kokyaku_no.arr, h_kaiin_no);
                    C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8070(MK顧客番号エラー)[%s]\n", out_format_buf);
                }
                /* ８０７０（MK顧客番号エラー）パンチエラーデータ出力 */
                rtn_cd = OutPunchdata(C_PUNCH_ERRORNO7);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return (C_const_NG);
                }
                /* 処理をNGで終了 */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* 次の行へ行く */
                continue;
            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            } else { /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]、会員番号=[%d]", MmkzData.kokyaku_no.arr, h_kaiin_no);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", (long) sqlca.sqlcode,
                        "MSカード情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }


            /*---PS会員番号体系検索処理---*/
            h_ps_taikei_count.arr = 0;

            /* ＳＱＬを実行する */
//                EXEC SQL SELECT
//                COUNT(1)
//                INTO :h_ps_taikei_count
//                        FROM
//                PS会員番号体系@CMSD
//                        WHERE
//                会員番号開始 <= :h_kaiin_no
//                AND 会員番号終了 >= :h_kaiin_no
//                AND 企業コード IN (3010, 3020, 3040, 3050, 3060);

            sqlca.sql = new StringDto("SELECT COUNT(1) FROM PS会員番号体系\n" +
                    "WHERE 会員番号開始 <= ? AND 会員番号終了 >= ? AND \n" +
                    "企業コード IN (3010, 3020, 3040, 3050, 3060)");
            sqlca.restAndExecute(h_kaiin_no, h_kaiin_no);
            sqlca.fetch();
            sqlca.recData(h_ps_taikei_count);

            /* ＳＱＬを実行結果を判定する */

            if (sqlca.sqlcode != C_const_Ora_OK) {
                sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", (long) sqlca.sqlcode,
                        "PS会員番号体系", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            if (h_ps_taikei_count.arr > 0) {

                /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                sqlca.rollback();
                if (DBG_LOG) {
                    sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                    C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8080(MK管理会員番号エラー)[%s]\n", out_format_buf);
                }
                /* ８０８０（MK顧客番号エラー）パンチエラーデータ出力 */
                rtn_cd = OutPunchdata(C_PUNCH_ERRORNO8);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return (C_const_NG);
                }
                /* 処理をNGで終了 */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* 次の行へ行く */
                continue;
            }
            /* 2022/10/13 MCCM初版 ADD END */

            /* ポイント・顧客ロック */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** ポイント・顧客ロック%s\n", "");
            }
            rtn_status = new IntegerDto();

            /* ロック種別は、1(更新) */
            lock_sbt = 1;
            sprintf(wk_lock, "%d", lock_sbt);

            rtn_cd = C_KdataLock(h_uid, wk_lock.arr, rtn_status);
            if (rtn_cd == C_const_NG) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** ポイント・顧客ロック status= %d\n", rtn_status);
                }
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客データ有無フラグセット */
            uid_flg = rtn_cd;

            /* マスター更新処理 */
            /* カード情報更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** カード情報更新%s\n", "");
            }
            rtn_cd = UpdateCard(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** カード情報更新NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "カード情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 静態情報更新チェック処理 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 静態情報更新チェック%s\n", "");
            }
            rtn_cd = SeitaiCheck(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 静態情報更新チェックNG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "静態情報更新チェック処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 利用可能ポイント情報（入会店）更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 利用可能ポイント情報更新%s\n", "");
            }
            rtn_cd = UpdateRiyokanoPoint(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 利用可能ポイント情報更新NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "利用可能ポイント情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客情報（入会店）更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客情報（入会店）更新%s\n", "");
            }
            rtn_cd = UpdateKokyakuNyukaiTen(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 顧客情報更新（入会店）NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客情報更新（入会店）処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客情報（入会年月日）更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客情報（入会年月日）更新%s\n", "");
            }
            rtn_cd = UpdateKokyakuNyukaiNengappi(in_nyukai_rendo, punch_errno);

            /* 顧客情報（在籍開始年月）更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客情報（在籍開始年月）更新%s\n", "");
            }
            rtn_cd = UpdateZaisekiKaishiNengetsu(in_nyukai_rendo, punch_errno);

            /* 静態更新有無チェック */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 静態情報更新チェック 静態状態有無[%d]\n", seitai_flg);
            }
            if (seitai_flg.intVal() == 0) {
                /* 正常処理データ件数カウントアップ */
                ok_data_cnt++;
                /* コミットする */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* 次の行へ行く */
                continue;
            }

            /* 顧客情報更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客情報更新%s\n", "");
            }
            rtn_cd = UpdateKokyaku(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 顧客情報更新NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (punch_errno > 0) {
                /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                sqlca.rollback();

                /* パンチエラーデータ出力 */
                rtn_cd = OutPunchdata(punch_errno);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

                /* コミットする */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();

                /* 次の行へ行く */
                continue;
            }

            /* 顧客属性情報更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客属性情報更新%s\n", "");
            }
            rtn_cd = UpdateKokyakuzokusei(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 顧客属性情報更新NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客属性情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (punch_errno > 0) {
                /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                sqlca.rollback();

                /* パンチエラーデータ出力 */
                rtn_cd = OutPunchdata(punch_errno);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

                /* コミットする */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();
                /* 次の行へ行く */
                continue;
            }

            /* 顧客企業別属性情報更新 */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** 顧客企業別属性情報更新%s\n", "");
            }
            rtn_cd = UpdateKigyobetuzokusei(in_nyukai_rendo, punch_errno);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 顧客企業別属性情報更新NG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客企業別属性情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (punch_errno > 0) {
                /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                sqlca.rollback();

                /* パンチエラーデータ出力 */
                rtn_cd = OutPunchdata(punch_errno);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "パンチエラーデータ出力に失敗しました", 0, 0, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

                /* コミットする */
//                    EXEC SQL COMMIT WORK;
                sqlca.commit();

                /* 次の行へ行く */
                continue;
            }

            /* コミットする */
//                EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* 正常処理データ件数カウントアップ */
            ok_data_cnt++;
            /* MSカード情報更新件数カウントアップ */
            mscd_data_cnt++;
            /* MM顧客情報更新件数カウントアップ */
            mmpd_data_cnt++;
            /* MS顧客制度情報更新件数カウントアップ */
            msps_data_cnt++;
            /* MM顧客属性情報更新件数カウントアップ */
            mmpz_data_cnt++;
            /* MM顧客企業別属性情報更新件数カウントアップ */
            mmcp_data_cnt++;
            /* TS利用可能ポイント情報更新件数カウントアップ */
            tsup_data_cnt++;

            if (input_data_cnt % 10000 == 0) {
                /* 入力テーブルデータ件数 */
                if (syorizumi_cnt == 0) {
                    C_APLogWrite(DEF_MSG_ID_104, "入力ファイル名：[%s] 入力データ件数：[%d] 正常処理件数：[%d] エラー件数：[%d]", " ", arg_i_Value, input_data_cnt, ok_data_cnt, ng_data_cnt, 0, 0);
                } else {
                    C_APLogWrite(DEF_MSG_ID_108, "入力ファイル名：[%s] 入力データ件数：[%d] 前回処理件数：[%d] 正常処理件数：[%d] エラー件数：[%d]", " ", arg_i_Value, input_data_cnt, syorizumi_cnt, ok_data_cnt, ng_data_cnt, 0);
                }

                /* MSカード情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MSカード情報", mscd_data_cnt, 0, 0, 0, 0);

                /* MM顧客情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客情報", mmpd_data_cnt, 0, 0, 0, 0);

                /* MS顧客制度情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MS顧客制度情報", msps_data_cnt, 0, 0, 0, 0);

                /* MM顧客属性情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客属性情報", mmpz_data_cnt, 0, 0, 0, 0);

                /* MM顧客企業別属性情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客企業別属性情報", mmcp_data_cnt, 0, 0, 0, 0);

                /* TS利用可能ポイント情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "TS利用可能ポイント情報", tsup_data_cnt, 0, 0, 0, 0);

                /* TMパンチエラーデータ情報 */
                C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "TMパンチエラーデータ情報", punch_data_cnt, 0, 0, 0, 0);
            }

        }

        if (DBG_LOG) {
            C_DbgEnd("*** Update_Data *** カード入会連動主処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： ReadFile                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  ReadFile()                                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入力ファイルより１レコードを読み込む                          */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int ReadFile() {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("ReadFile処理");
            /*---------------------------------------------------------------------*/
        }
        memset(in_nyukai_rendo, 0x00, 0);

        int NYUKAI_RENDO_DATA_LEN = in_nyukai_rendo.size();
        fread(in_nyukai_rendo, NYUKAI_RENDO_DATA_LEN, 1, fp_in);

        if (feof(fp_in) != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgMsg("*** ReadFile *** READ EOF status= %d\n", feof(fp_in));
                /*---------------------------------------------*/
            }
            return DEF_Read_EOF;
        }
        if (ferror(fp_in) != C_const_OK) {
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** ReadFile *** 入力ファイルリードNG%s\n", "");
                /*-------------------------------------------------------------*/
            }

            sprintf(out_format_buf, "fread(%s)", path_in);
            APLOG_WT("903", 0, null, out_format_buf, 0, 0, 0, 0, 0);
            return C_const_NG;
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgEnd("ReadFile処理", 0, 0, 0);
            /*-------------------------------------------------------------*/
        }
        return C_const_OK;
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： WM_Batch_Update                                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  WM_Batch_Update()                                             */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               WMバッチ処理実行管理更新処理                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int WM_Batch_Update() {

        if (DBG_LOG) {
            C_DbgStart("WM_Batch_Update処理");
        }

        /* WMバッチ処理実行管理更新処理 */
//            EXEC SQL UPDATE WMバッチ処理実行管理
//            SET シーケンス番号 = :h_jikkouzumi_cnt
//            WHERE 機能ＩＤ = :h_kinou_id;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE WMバッチ処理実行管理 " +
                "SET シーケンス番号 = ?" +
                "WHERE 機能ＩＤ = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_jikkouzumi_cnt, h_kinou_id);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            /* 処理をNGで終了 */
            return (C_const_NG);
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* WMバッチ処理実行管理新規作成処理 */
//                EXEC SQL INSERT INTO WMバッチ処理実行管理
//                        (シーケンス番号, 機能ＩＤ)
//                VALUES
//                        (:h_jikkouzumi_cnt, :h_kinou_id);

            StringDto WRKSQL = new StringDto();
            WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO WMバッチ処理実行管理 (シーケンス番号, 機能ＩＤ)\n" +
                            "VALUES('%s','%s')"
                    , h_jikkouzumi_cnt, h_kinou_id);
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute();

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* 処理をNGで終了 */
                return (C_const_NG);
            }
        }

        if (DBG_LOG) {
            C_DbgEnd("WM_Batch_Update処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： Write_ErrorList_File                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  Write_ErrorList_File()                                        */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              入会連動に失敗したパンチエラーデータ情報を                    */
    /*              入会エラーリストファイルとして出力                            */
    /*                                                                            */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*              なし                                                          */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int Write_ErrorList_File() {
        int rtn_cd;                              /* 関数戻り値 */
        StringDto wk_sql = new StringDto(DEF_BUFSIZE16K);              /* 動的SQLバッファ */
        StringDto wbuf = new StringDto();                           /* 一時格納エリア (出力) */
        StringDto outbuf = new StringDto();               /* 出力する行のバッファ */
        int i_loop;                              /* パンチエラーデータ情報カウント */
        StringDto wk_buff_in = new StringDto();                     /* 取得文字列格納バッファ     */
        StringDto wk_buff_full = new StringDto();                   /* 全角変換バッファ           */
        StringDto wk_header = new StringDto();            /* ヘッダー変換バッファ       */
        StringDto wk_sjis_str = new StringDto();                    /* SJIS変換後文字列           */
        IntegerDto wk_sjis_len;                         /* SJIS変換後文字列のレングス */

        if (DBG_LOG) {
            C_DbgStart("Write_ErrorList_File処理");
        }

        /* 変数初期化 */
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_sql, 0x00, sizeof(wk_sql));

        /* SQL文作成 */
        /* 2015/07/30 エラーコード（仮退会済）追加 */
        sprintf(wk_sql, "SELECT "
                        + "TMパンチデータエラー情報.処理年月日, "
                        + "TMパンチデータエラー情報.データ区分, "
                        + "TMパンチデータエラー情報.依頼日, "
                        + "TMパンチデータエラー情報.バッチ番号, "
                        + "TMパンチデータエラー情報.入会年月日, "
                        + "TMパンチデータエラー情報.申込書企業コード, "
                        + "NVL(RPAD(PS店表示情報.企業名称,LENGTH(PS店表示情報.企業名称)), ' '), "
                        + "TMパンチデータエラー情報.申込書店番号, "
                        + "NVL(RPAD(PS店表示情報.漢字店舗名称,LENGTH(PS店表示情報.漢字店舗名称)), ' '), "
                        + "TMパンチデータエラー情報.会員番号, "
                        + "NVL(RPAD(TMパンチデータエラー情報.会員氏名,LENGTH(TMパンチデータエラー情報.会員氏名)), ' '), "
                        + "TMパンチデータエラー情報.理由コード, "
                        + "CASE TMパンチデータエラー情報.理由コード WHEN 8010 THEN '会員番号エラー' WHEN 8040 THEN '店番号エラー' WHEN 8050 THEN '退会済エラー' WHEN 8060 THEN '仮退会済エラー' WHEN 8070 THEN 'MK顧客番号エラー' WHEN 8080 THEN 'MK管理会員番号エラー' ELSE NULL END "
                        + "FROM "
                        + "TMパンチデータエラー情報 "
                        + "LEFT JOIN ( "
                        + "SELECT * "
                        + " FROM "
                        + "PS店表示情報 "
                        + " WHERE "
                        + "開始年月日 <= %d "
                        + "AND 終了年月日 >= %d "
                        + ") PS店表示情報 "
                        + "ON TMパンチデータエラー情報.申込書店番号 = PS店表示情報.店番号 "
                        + "WHERE "
                        + "TMパンチデータエラー情報.処理年月日  = %d "
                        + "AND TMパンチデータエラー情報.データ区分 = 1 ",
                bat_shori_yyyymmdd, bat_shori_yyyymmdd, bat_shori_yyyymmdd);

        strcpy(str_sql, wk_sql);

        /* 動的SQL文解析 */
//            EXEC SQL PREPARE sql_tsptmsinf FROM :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("crm_cur");
        sqlca.sql = wk_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File () *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File () *** 動的SQL 解析NG = %s\n", str_sql);
            }

            APLOG_WT(DEF_MSG_ID_902, 0, null, (long) sqlca.sqlcode, str_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル宣言 */
//            EXEC SQL DECLARE crm_cur CURSOR FOR sql_tsptmsinf;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソル宣言 crm_cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, (long) sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソルをオープン */
//            EXEC SQL OPEN crm_cur;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソルオープン tsptmsinf_cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, (long) sqlca.sqlcode, "CURSOR OPEN ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        i_loop = 0;
        while (true) {
            /* 初期化 */
            h_syori_ymd.arr = 0;                                                /* 処理年月日 */
            h_data_kubun.arr = 0;                                               /* データ区分 */
            h_irai_ymd.arr = 0;                                                 /* 依頼年月日 */
            h_batch_no.arr = 0;                                                 /* バッチ番号 */
            h_nyukai_ymd.arr = 0;                                               /* 入会年月日 */
            h_moushikomi_kigyou_cd.arr = 0;                                     /* 申込企業コード */
            memset(h_kigyou_name, 0x00, sizeof(h_kigyou_name));             /* 企業名称 */
            h_moushikomisyoten_no.arr = 0;                                      /* 申込書店番号 */
            memset(h_kanji_tenpo_name, 0x00, sizeof(h_kanji_tenpo_name));   /* 漢字店舗名称 */
            h_kaiin_no.arr = 0;                                                 /* 会員番号 */
            memset(h_kaiin_name, 0x00, sizeof(h_kaiin_name));               /* 会員氏名 */
            h_riyuu_cd.arr = 0;                                                 /* 理由コード */
            memset(h_error_msg, 0x00, sizeof(h_error_msg));                 /* エラー理由 */

            /* フェッチ処理 */
//                EXEC SQL FETCH crm_cur
//                INTO :h_syori_ymd,
//                :h_data_kubun,
//                :h_irai_ymd,
//                :h_batch_no,
//                :h_nyukai_ymd,
//                :h_moushikomi_kigyou_cd,
//                :h_kigyou_name,
//                :h_moushikomisyoten_no,
//                :h_kanji_tenpo_name,
//                :h_kaiin_no,
//                :h_kaiin_name,
//                :h_riyuu_cd,
//                :h_error_msg;
            sqlca.fetch();
            sqlca.recData(h_syori_ymd, h_data_kubun, h_irai_ymd, h_batch_no, h_nyukai_ymd, h_moushikomi_kigyou_cd,
                    h_kigyou_name, h_moushikomisyoten_no, h_kanji_tenpo_name, h_kaiin_no, h_kaiin_name, h_riyuu_cd, h_error_msg);

            /* データ無し以外エラーの場合処理を異常終了 */
            if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
                /* エラー発生 */
                if (DBG_LOG) {
                    C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("Make_Coupon_CrmFile処理", C_const_NG, 0, 0);
                }
                APLOG_WT(DEF_MSG_ID_904, 0, null, "FETCH",
                        (long) sqlca.sqlcode,
                        "TMパンチエラーデータ情報",
                        (long) bat_shori_yyyymmdd, 0, 0);

                /* カーソルをクローズ */
//                    EXEC SQL CLOSE crm_cur;
//                    sqlca.close();
                sqlcaManager.close("crm_cur");

                if (DBG_LOG) {
                    C_DbgEnd("Write_ErrorList_File処理 フェッチエラー ", 0, 0, 0);
                }

                /* 処理をNGで終了 */
                return (C_const_NG);
            }

            /* データ無し */
            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                break;
            }

            /* パンチエラーデータ情報件数カウントアップ */
            i_loop++;

            if (i_loop == 1) { /* パンチエラーデータ情報１件目 */
                /* ------------ */
                /* ヘッダ行編集 */
                /* ------------ */
                memset(outbuf, 0x00, sizeof(outbuf));
                memset(wk_header, 0x00, sizeof(wk_header));

                /* ------------------------------------------------ */
                /*    UTF8 → SJIS変換処理 */
                wk_sjis_len = new IntegerDto();
                memset(wk_sjis_str, 0x00, wk_sjis_str.len);

                strcpy(wk_header, C_OUT_HEADER);
                rtn_cd = C_ConvUT2SJ(wk_header, strlen(wk_header), wk_sjis_str, wk_sjis_len);
                if (rtn_cd != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
                    return (C_const_NG);
                }
                /* 出力エリアへ設定 */
                memcpy(outbuf, wk_sjis_str, wk_sjis_len.arr);
                /* ------------------------------------------------ */

                /* 改行コード */
                strcat(outbuf, DEF_STR3);

                /* 出力ファイル書込み */
                rtn_cd = fputs(outbuf, fp_out);
                if (rtn_cd == EOF) {
                    /* エラー発生 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** fputs NG rtn=[%d]\n", rtn_cd);
                    }
                    sprintf(out_format_buf, "fputs（%s）", path_out);
                    APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, (long) rtn_cd, 0, 0, 0, 0);
                    return (C_const_NG);
                }
            }

            /* ---------- */
            /* 明細行編集 */
            /* ---------- */
            memset(outbuf, 0x00, sizeof(outbuf));

            /* 処理年月日設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_syori_ymd);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 登録日設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_irai_ymd);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 申込年月日設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_nyukai_ymd);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 入会企業コード設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_moushikomi_kigyou_cd);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 入会企業名称設定 */
            /* ------------------------------------------------ */
            /*     全角に変換する */
            memset(wk_buff_in, 0x00, sizeof(wk_buff_in));
            memset(wk_buff_full, 0x00, sizeof(wk_buff_full));
            memcpy(wk_buff_in, h_kigyou_name, strlen(h_kigyou_name));
            cmBTfuncB.BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, wk_sjis_str.len);
            rtn_cd = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            memset(wbuf, 0x00, sizeof(wbuf));
            memcpy(wbuf, wk_sjis_str, wk_sjis_len.arr);
            /* 出力エリアへ設定 */
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);
            /* ------------------------------------------------ */

            /* 入会店番号設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_moushikomisyoten_no);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 入会店舗名称設定 */
            /* ------------------------------------------------ */
            /*     全角に変換する */
            memset(wk_buff_in, 0x00, sizeof(wk_buff_in));
            memset(wk_buff_full, 0x00, sizeof(wk_buff_full));
            memcpy(wk_buff_in, h_kanji_tenpo_name, strlen(h_kanji_tenpo_name));
            cmBTfuncB.BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, wk_sjis_str.len);
            rtn_cd = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            memset(wbuf, 0x00, sizeof(wbuf));
            memcpy(wbuf, wk_sjis_str, wk_sjis_len.arr);
            /* 出力エリアへ設定 */
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);
            /* ------------------------------------------------ */

            /* 会員番号設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_kaiin_no);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* 氏名（漢字）設定 */
            /* ------------------------------------------------ */
            /*     全角に変換する */
            memset(wk_buff_in, 0x00, sizeof(wk_buff_in));
            memset(wk_buff_full, 0x00, sizeof(wk_buff_full));
            memcpy(wk_buff_in, h_kaiin_name, strlen(h_kaiin_name));
            cmBTfuncB.BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, wk_sjis_str.len);
            rtn_cd = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            memset(wbuf, 0x00, sizeof(wbuf));
            memcpy(wbuf, wk_sjis_str, wk_sjis_len.arr);
            /* 出力エリアへ設定 */
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);
            /* ------------------------------------------------ */

            /* 理由コード設定 */
            strcat(outbuf, DEF_STR1);
            sprintf(wbuf, "%d", h_riyuu_cd);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, DEF_STR2);

            /* エラー理由設定 */
            /* ------------------------------------------------ */
            /*     全角に変換する */
            memset(wk_buff_in, 0x00, sizeof(wk_buff_in));
            memset(wk_buff_full, 0x00, sizeof(wk_buff_full));
            memcpy(wk_buff_in, h_error_msg, strlen(h_error_msg));
            cmBTfuncB.BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, wk_sjis_str.len);
            rtn_cd = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            memset(wbuf, 0x00, sizeof(wbuf));
            memcpy(wbuf, wk_sjis_str, wk_sjis_len.arr);
            /* 出力エリアへ設定 */
            strcat(outbuf, DEF_STR1);
            strcat(outbuf, wbuf);
            strcat(outbuf, DEF_STR1);
            /* ------------------------------------------------ */

            /* 改行コード */
            strcat(outbuf, DEF_STR3);

            /* 出力ファイル書込み */
            rtn_cd = fputs(outbuf, fp_out);
            if (rtn_cd == EOF) {
                /* エラー発生 */
                if (DBG_LOG) {
                    C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** fputs NG rtn=[%d]\n", rtn_cd);
                }
                sprintf(out_format_buf, "fputs（%s）", path_out);
                APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, (long) rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
        }

        /* カーソルをクローズ */
//            EXEC SQL CLOSE crm_cur;
//            sqlca.close();
        sqlcaManager.close("crm_cur");


        if (DBG_LOG) {
            C_DbgEnd("Write_ErrorList_File処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

    /* **************************************************************************** */
    /*                                                                            */
    /*  関数名 ： CheckMscard                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  CheckMscard(NYUKAI_RENDO_DATA in_nyukai_rendo,                */
    /*                          int *punch_errno)                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報チェック処理                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int CheckMscard(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {
        int rtn_cd;             /* 関数戻り値                       */
//  int     rtn_status;         /* 関数ステータス                   */                                         /* 2022/10/13 MCCM初版 DEL */
//  int     corpid;             /* 会員企業コード                   */                                         /* 2022/10/13 MCCM初版 DEL */
        StringDto utf8str = new StringDto();      /* UTF8変換後の文字列               */
        IntegerDto utf8len = new IntegerDto();            /* UTF8変換後の文字列のレングス     */
        StringDto wk_kanji_name = new StringDto(81);  /* 漢字氏名                         */
        StringDto wk_kana_name = new StringDto(81);   /* カナ氏名(全角)                   */
        StringDto wk_kana_name_han = new StringDto(241); /* カナ氏名(半角変換後)     */
        StringDto wk_kana_name_zen = new StringDto(241); /* カナ氏名(漢字氏名未設定時のカナ氏名全角変換後)*/
        StringDto wk_zip = new StringDto(8);            /* ハイフンなし郵便番号             */
        StringDto wk_address1 = new StringDto(11);    /* 住所1                            */
        StringDto wk_address2 = new StringDto(81);    /* 住所2                            */
        StringDto wk_address3 = new StringDto(81);    /* 住所3                            */
        StringDto wk_tenpo = new StringDto(7);   /* 入力データ.入会店番号 数値化ＷＫ */
        StringDto wk_sjis_str = new StringDto();                   /* SJIS変換後文字列           */
        IntegerDto wk_sjis_len;                         /* SJIS変換後文字列のレングス */

        String wk_p;              /* カナ氏名切出位置判定             */
        String wk_c;         /* カナ氏名切出位置判定             */
        int wk_c_len;           /* カナ氏名切出位置                 */

        if (DBG_LOG) {
            C_DbgStart("カード情報チェック処理");
        }
        /* 初期化 */
        memset(h_uid, 0x00, sizeof(h_uid));
        memset(h_pid, 0x00, sizeof(h_pid));
        memset(utf8str, 0x00, sizeof(utf8str));
        memset(h_kaiin_name, 0x00, sizeof(h_kaiin_name));
        memset(h_kana_name, 0x00, sizeof(h_kana_name));
        memset(h_zip, 0x00, sizeof(h_zip));
        memset(h_address1, 0x00, sizeof(h_address1));
        memset(h_address2, 0x00, sizeof(h_address2));
        memset(h_address3, 0x00, sizeof(h_address3));
        memset(wk_kanji_name, 0x00, sizeof(wk_kanji_name));
        memset(wk_kana_name, 0x00, sizeof(wk_kana_name));
        memset(wk_kana_name_han, 0x00, sizeof(wk_kana_name_han));
        memset(wk_kana_name_zen, 0x00, sizeof(wk_kana_name_zen));
        memset(wk_zip, 0x00, sizeof(wk_zip));
        memset(wk_address1, 0x00, sizeof(wk_address1));
        memset(wk_address2, 0x00, sizeof(wk_address2));
        memset(wk_address3, 0x00, sizeof(wk_address3));
        rtn_cd = C_const_OK;
//  rtn_status = C_const_Stat_OK;                                                                              /* 2022/10/13 MCCM初版 DEL */
//  corpid = 0;                                                                                                /* 2022/10/13 MCCM初版 DEL */
        h_cdriyu_cd.arr = 0;
        h_hakko_ymd.arr = 0;
        h_service_shubetsu.arr = 0;
        memset(h_cdkaiin_no, 0x00, sizeof(h_cdkaiin_no));
        h_kigyo_cd.arr = 0;
        h_renkei_mise_no.arr = 0;                                                                                       /* 2022/10/11 MCCM初版 ADD*/

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        /* 全角スペース取得 */
        memset(wk_fullspace, 0x00, sizeof(wk_fullspace));
        wk_fullspace_len.arr = 0;
        rtn_cd = C_SetFullSpace(40, 1, wk_fullspace, wk_fullspace_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** 全角スペース取得NG%s\n", "");
            }
            APLOG_WT("903", 0, null, "C_SetFullSpace", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ＨＯＳＴ変数にセット */
        /* 会員番号             */
        strncpy(h_pid, in_nyukai_rendo.kaiin_no.strVal(), sizeof(h_pid) - 1);
        if (DBG_LOG) {
            C_DbgMsg("*** CheckMscard *** 会員番号=[%s] \n", h_pid);
        }
        /* 漢字氏名 */
        /* UTF8に変換 */
        strncpy(wk_kanji_name, in_nyukai_rendo.kanji_name.strVal(), sizeof(wk_kanji_name) - 1);
        rtn_cd = C_ConvSJ2UT(wk_kanji_name, strlen(wk_kanji_name)
                , utf8str, utf8len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(漢字氏名)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(utf8str, utf8len.arr);
        strcpy(h_kaiin_name, utf8str);
        if (DBG_LOG) {
            C_DbgMsg("漢字氏名 =[%s]\n", h_kaiin_name);
        }

        /* 初期化 */
        memset(utf8str, 0x00, utf8str.len);

        /* カナ氏名 */
        /* UTF8に変換 */
        strncpy(wk_kana_name, in_nyukai_rendo.kana_name.strVal(), sizeof(wk_kana_name) - 1);
        rtn_cd = C_ConvSJ2UT(wk_kana_name, strlen(wk_kana_name)
                , utf8str, utf8len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(カナ氏名)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(utf8str, utf8len.arr);

        /* 全角半角変換 */
        rtn_cd = C_ConvFull2Half(utf8str, wk_kana_name_han);
        if (rtn_cd == C_const_NG) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", (long) rtn_cd, 0, 0, 0, 0);
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** 全角半角変換NG %s\n", "");
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ------------------------------------------------ */
        /*    UTF8 → SJIS変換処理 */
//            wk_sjis_len = 0;
        wk_sjis_len = new IntegerDto();
        memset(wk_sjis_str, 0x00, wk_sjis_str.len);

        rtn_cd = C_ConvUT2SJ(wk_kana_name_han, strlen(wk_kana_name_han), wk_sjis_str, wk_sjis_len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** SJIS変換(カナ氏名)NG %s\n", "");
            }
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", (long) rtn_cd, 0, 0, 0, 0);
            return (C_const_NG);
        }
        /* ------------------------------------------------ */

        /* 40が最大文字数 */
        if (wk_sjis_len.arr > DEF_BUFSIZE40) {
            /* 初期化 */
            memset(utf8str, 0x00, utf8str.len);
            memset(wk_kana_name, 0x00, sizeof(wk_kana_name));
            utf8len = new IntegerDto();

            /* 40バイト目が全角の場合、39バイトで切りだす */
//                wk_p = wk_sjis_str;
            wk_c_len = 0;

//                while( *wk_p != '\0' ) {
//                    wk_c = *wk_p;
//                    if ( ( 0x81 <= wk_c && wk_c <= 0x9f ) || ( 0xe0 <= wk_c && wk_c <= 0xfc ) ) {
//
//                        /* 全角文字 */
//                        wk_p++;
//                        wk_p++;
//                        if ( wk_c_len + 2 > DEF_BUFSIZE40 ) {
//                            break;
//                        }
//                        wk_c_len = wk_c_len + 2;
//                    } else {
//                        /* 半角文字 */
//                        wk_p++;
//                        if ( wk_c_len + 1 > DEF_BUFSIZE40 ) {
//                            break;
//                        }
//                        wk_c_len = wk_c_len + 1;
//                    }
//                }

            for (int i = 0; i < wk_sjis_str.arr.length(); i++) {
                wk_c_len = wk_c_len + wk_sjis_str.arr.substring(i, i + 1).length();
                if (wk_c_len > 40) {
                    break;
                }
            }


            /* 再度UTF8に変換 */
            strncpy(wk_kana_name, wk_sjis_str, wk_c_len);
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** 切出[%d]\n", wk_c_len);
                C_DbgMsg("*** CheckMscard *** wk_kana_name[%s]\n", wk_kana_name);
            }

            rtn_cd = C_ConvSJ2UT(wk_kana_name, strlen(wk_kana_name)
                    , utf8str, utf8len);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** UTF8変換(カナ氏名40)NG %s\n", "");
                }
                APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }
            strncpy(h_kana_name, utf8str, utf8len.arr);
        } else {
            strcpy(h_kana_name, wk_kana_name_han);
        }

        if (DBG_LOG) {
            C_DbgMsg("カナ氏名 =[%s]\n", h_kana_name);
        }

        /* 郵便番号 （ハイフンを削除） */
        strncpy(wk_zip, in_nyukai_rendo.zip.strVal(), sizeof(wk_zip) - 1);
        rtn_cd = C_ConvTelNo(wk_zip, strlen(wk_zip), h_zip);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*---------------------------------------------------------------------------*/
                C_DbgMsg("*** UpdateKokyakuzokusei *** 郵便番号変換(郵便番号)NG %s\n", "");
                /*---------------------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_ConvTelNo", (long) rtn_cd, 0, 0, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }
        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(h_zip, strlen(h_zip));

        /* 2015/07/30 郵便番号桁数チェック削除 */
        /*if (strlen(h_zip) != 7) { */
        /*    memset(h_zip, 0x00, sizeof(h_zip));   */
        /*} */

        if (DBG_LOG) {
            C_DbgMsg("郵便番号 =[%s]\n", h_zip);
        }

        /* 初期化 */
        memset(utf8str, 0x00, utf8str.len);

        /* 住所1 */
        /* UTF8に変換 */
        strncpy(wk_address1, in_nyukai_rendo.address1.strVal(), sizeof(wk_address1) - 1);
        rtn_cd = C_ConvSJ2UT(wk_address1, strlen(wk_address1), utf8str, utf8len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(住所1)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(utf8str, utf8len.arr);
        strncpy(h_address1, utf8str, sizeof(h_address1) - 1);

        /* 2015/07/30 住所１都道府県チェック削除 */
        /* 住所１チェック */
        /*if (strlen(h_address1) != 0) {    */
        /*    EXEC SQL SELECT COUNT(*)  */
        /*               INTO :h_count  */
        /*               FROM MM住所コード情報  */
        /*              WHERE substr(:h_address1, 1, 4) like trim(住所１) || '%';*/

        /* ＳＱＬを実行結果を判定する */
        /*    if      (sqlca.sqlcode == C_const_Ora_OK )      { }   */
        /*    else {    */ /* DBERR */
        /*        APLOG_WT( DEF_MSG_ID_904, 0, null, "SELECT", (long)sqlca.sqlcode,     */
        /*                "MM住所コード情報", " ", 0, 0);   */
        /* 処理を終了する */
        /*        return C_const_NG;    */
        /*    } */

        /* 住所１に該当する都道府県がない場合 */
        /*    if (h_count == 0 ) {  */
        /* 更新対象外 */
        /*        memset(h_address1, 0x00, sizeof(h_address1)); */
        /*    } */
        /*} */

        if (DBG_LOG) {
            C_DbgMsg("住所１ =[%s]\n", h_address1);
        }

        /* 初期化 */
        memset(utf8str, 0x00, utf8str.len);

        /* 住所2 */
        /* UTF8に変換 */
        strncpy(wk_address2, in_nyukai_rendo.address2.strVal(), sizeof(wk_address2) - 1);
        rtn_cd = C_ConvSJ2UT(wk_address2, strlen(wk_address2), utf8str, utf8len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(住所2)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(utf8str, utf8len.arr);
        strcpy(h_address2, utf8str);

        if (DBG_LOG) {
            C_DbgMsg("住所２=[%s]\n", h_address2);
        }

        /* 初期化 */
        memset(utf8str, 0x00, utf8str.len);

        /* 住所3 */
        /* UTF8に変換 */
        strncpy(wk_address3, in_nyukai_rendo.address3.strVal(), sizeof(wk_address3) - 1);
        rtn_cd = C_ConvSJ2UT(wk_address3, strlen(wk_address3), utf8str, utf8len);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(住所3)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", (long) rtn_cd,
                    0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(utf8str, utf8len.arr);
        strcpy(h_address3, utf8str);

        if (DBG_LOG) {
            C_DbgMsg("住所３ =[%s]\n", h_address3);
        }

        /* 会員企業コード取得 */
        strncpy(wk_tenpo, in_nyukai_rendo.nyukaiten_no.strVal(), sizeof(wk_tenpo) - 1);
        h_nyukaiten_no.arr = atoi(wk_tenpo);
        h_moushikomiten_no.arr = atoi(wk_tenpo);

        if (h_nyukaiten_no.intVal() != 0) {
//                EXEC SQL SELECT 連携用店番号                                                                            /* 2022/10/13 MCCM初版 MOD */
//                INTO :h_renkei_mise_no                                                                       /* 2022/10/13 MCCM初版 MOD */
//                FROM PS店表示情報@CMSD
//                WHERE 店番号       =  :h_nyukaiten_no
//                AND 開始年月日   <= :h_kijyun_yyyymmdd
//                AND 終了年月日   >= :h_kijyun_yyyymmdd;

            sqlca.sql = new StringDto("SELECT 連携用店番号   FROM PS店表示情報 WHERE 店番号       =  ? AND 開始年月日   <= ? AND 終了年月日   >= ?");
            sqlca.restAndExecute(h_nyukaiten_no, h_kijyun_yyyymmdd, h_kijyun_yyyymmdd);
            sqlca.fetch();
            sqlca.recData(h_renkei_mise_no);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode == C_const_Ora_OK) {
            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) { /* データ無し */
                if (DBG_LOG) {
                    sprintf(out_format_buf, "店番号=[%d]、基準日=[%d]", h_nyukaiten_no, h_kijyun_yyyymmdd);
                    C_DbgMsg("*** CheckMscard *** 店情報取得NG 8040(店番号エラー)[%s]\n", out_format_buf);
                }
                /* 入会店は 0 として登録 */
                h_nyukaiten_no.arr = 0;
            } else { /* DBERR */
                sprintf(out_format_buf, "店番号=[%d]、基準日=[%d]", h_nyukaiten_no, h_kijyun_yyyymmdd);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", (long) sqlca.sqlcode,
                        "PS店表示情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
        }

        /* MSカード情報検索処理 */
//            EXEC SQL SELECT サービス種別,
//                会員番号,
//            to_char(NVL(顧客番号,0), 'FM000000000000000' ),
//                    NVL(企業コード,0),
//                    理由コード,
//                    NVL(発行年月日,0),
//                    NVL(ＧＯＯＰＯＮ番号,0),                                                                   /* 2022/10/13 MCCM初版 ADD */
//                    カードステータス                                                                           /* 2022/10/13 MCCM初版 ADD */
//            INTO :h_service_shubetsu,
//                    :h_cdkaiin_no,
//                    :h_uid,
//                    :h_kigyo_cd,
//                    :h_cdriyu_cd,
//                    :h_hakko_ymd,
//                    :h_goopon_no,                                                                              /* 2022/10/13 MCCM初版 ADD */
//                    :h_card_status                                                                             /* 2022/10/13 MCCM初版 ADD */
//            FROM MSカード情報@CMSD
//            WHERE 会員番号     = :h_pid
//            AND サービス種別 = 1;

        sqlca.sql = new StringDto("SELECT サービス種別,会員番号,to_char(NVL(顧客番号,0), 'FM000000000000000' ),NVL(企業コード,0),\n" +
                "理由コード,NVL(発行年月日,0),NVL(ＧＯＯＰＯＮ番号,0), カードステータス \n" +
                "FROM MSカード情報 WHERE 会員番号     = ? AND サービス種別 = 1");
        sqlca.restAndExecute(h_pid);
        sqlca.fetch();
        sqlca.recData(h_service_shubetsu, h_cdkaiin_no, h_uid, h_kigyo_cd, h_cdriyu_cd, h_hakko_ymd, h_goopon_no, h_card_status);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** MSカード情報検索NG %s\n", "");
            }
            /* DBERR */
            sprintf(out_format_buf, "会員番号=[%s]", h_pid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", (long) sqlca.sqlcode,
                    "MSカード情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** CheckMscard *** MSカード情報sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
        }

        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) { /* データ有 */
            /* カード新旧フラグを立てる */
            card_flag = 1;
        } else { /* データ無 */
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** 会員番号取得NG 8010(会員番号エラー) 会員番号=[%s]\n", h_pid);
            }
            card_flag = 0;
            /* ８０１０（会員番号エラー）警告出力 */
            sprintf(out_format_buf, "会員番号=[%s]", h_pid);
            APLOG_WT("711", 0, null, "MSカード情報", out_format_buf, 0, 0, 0, 0);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** CheckMscard *** MSカード情報 card_flag=[%d]\n", card_flag);
        }

        /* データ無しまたは顧客番号=0またはＧＯＯＰＯＮ番号=0の場合 */                                             /* 2022/10/13 MCCM初版 MOD */
        if (card_flag == 0 || atof(h_uid) == 0.0 || atof(h_goopon_no) == 0.0) {                                       /* 2022/10/13 MCCM初版 MOD */
            if (card_flag == 0 || atof(h_uid) == 0.0) {                                                               /* 2022/10/13 MCCM初版 ADD */
                /* 顧客番号にSQ顧客番号発番をセット */                                                                 /* 2022/10/13 MCCM初版 ADD */
//                    EXEC SQL SELECT SQ顧客番号発番.NEXTVAL@CMSD                                                            /* 2022/10/13 MCCM初版 ADD */
//                            INTO :h_uid                                             /* 顧客番号 */                      /* 2022/10/13 MCCM初版 ADD */
//                    FROM DUAL@CMSD;                                                                             /* 2022/10/13 MCCM初版 ADD */
                sqlca.sql = new StringDto("SELECT SQ顧客番号発番 FROM cmsho_sq顧客番号発番");
                sqlca.restAndExecute();
                sqlca.fetch();
                sqlca.recData(h_uid);
            }                                                                                                    /* 2022/10/13 MCCM初版 ADD */

            if (card_flag == 0 || atof(h_goopon_no) == 0.0) {                                                           /* 2022/10/13 MCCM初版 ADD */
                /* ＧＯＯＰＯＮ番号をセット */                                                                         /* 2022/10/13 MCCM初版 ADD */
                sprintf(h_goopon_no, "%s%s", "8", h_uid);                  /* ＧＯＯＰＯＮ番号 */                      /* 2023/01/05 MCCM初版 MOD */
            }                                                                                                      /* 2022/10/13 MCCM初版 ADD */
        }                                                                                                          /* 2022/10/13 MCCM初版 ADD */

        if (DBG_LOG) {
            C_DbgEnd("カード情報チェック処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateCard                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateCard(NYUKAI_RENDO_DATA in_nyukai_rendo,                 */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報更新処理                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int UpdateCard(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("カード情報更新処理");
            C_DbgMsg("会員番号          =[%s]\n", h_pid);
            C_DbgMsg("顧客番号          =[%s]\n", h_uid);
        }

        /*--------------------*/
        /* ＨＯＳＴ変数セット */
        /*--------------------*/
        /* 初期化 */
        MscdData = new MS_CARD_INFO_TBL();
//            memset(MscdData, 0x00, sizeof(MS_CARD_INFO_TBL));
        /* 会員番号         */
        memcpy(MscdData.kaiin_no, h_pid, sizeof(h_pid) - 1);
        MscdData.kaiin_no.len = strlen(h_pid);
        /* 顧客番号         */
        memcpy(MscdData.kokyaku_no, h_uid.arr, sizeof(h_uid) - 1);
        MscdData.kokyaku_no.len = strlen(h_uid);
        /* 2022/10/13 MCCM初版 ADD START */
        /* ＧＯＯＰＯＮ番号 */
        memcpy(MscdData.goopon_no, h_goopon_no, sizeof(h_goopon_no) - 1);
        MscdData.goopon_no.len = strlen(h_goopon_no);
        /* 2022/10/13 MCCM初版 ADD END */

        /* カードステータス */
        if (h_card_status.intVal() == 0) {                                                                                 /* 2022/10/13 MCCM初版 MOD */
            h_card_status.arr = 1;                                                                                     /* 2022/10/13 MCCM初版 MOD */
        }                                                                                                          /* 2022/10/13 MCCM初版 MOD */

        /* 理由コード */
        if (h_cdriyu_cd.intVal() == 0) {
            h_cdriyu_cd.arr = 2000;
        }

        /* カード発行年月日 */
        if (h_hakko_ymd.intVal() == 0) { /* 基準日を設定 */
            h_hakko_ymd = h_kijyun_yyyymmdd;
        }

        /* カードマスタあり */
        if (card_flag == 1) {
            if (h_kigyo_cd.intVal() == 0) {
                h_kigyo_cd.arr = h_ps_corpid.arr;
            }
            /* MSカード情報更新処理 */
//                EXEC SQL UPDATE MSカード情報@CMSD
//                SET 顧客番号       = :MscdData.kokyaku_no,
//                        ＧＯＯＰＯＮ番号 = :MscdData.goopon_no,                                                   /* 2022/10/13 MCCM初版 ADD */
//                        カードステータス = :h_card_status,                                                        /* 2022/10/13 MCCM初版 ADD */
//                        理由コード     = :h_cdriyu_cd,
//                        発行年月日     = :h_hakko_ymd,
///*
//                     企業コード     = DECODE(NVL(企業コード,0),0,:h_ps_corpid,企業コード),
//                     旧販社コード   = DECODE(NVL(旧販社コード,0),0,:h_ps_oldcorp,旧販社コード),
//*/
//                        企業コード     = DECODE(:h_ps_corpid,0,企業コード,:h_ps_corpid),
//                旧販社コード   = DECODE(:h_kyu_hansya_cd,0,旧販社コード,:h_kyu_hansya_cd),
//                バッチ更新日   = :h_bat_yyyymmdd,
//                        最終更新日     = :h_bat_yyyymmdd,
//                        最終更新日時   = sysdate,
//                        最終更新プログラムＩＤ = :h_saishu_koshin_programid
//                WHERE 会員番号       = :h_pid
//                AND サービス種別 = 1;

            StringDto sql = new StringDto();
            sql.arr = "UPDATE MSカード情報 " +
                    "SET 顧客番号 = ?,  " +
                    "ＧＯＯＰＯＮ番号 = ?,  " +
                    "カードステータス = ?,  " +
                    "理由コード = ?,  " +
                    "発行年月日 = ?,  " +
                    "企業コード = DECODE(?,0,企業コード,?),  " +
                    "旧販社コード = DECODE(?,0,旧販社コード,?),  " +
                    "バッチ更新日 = ?,  " +
                    "最終更新日 = ?,  " +
                    "最終更新日時 = sysdate(),  " +
                    "最終更新プログラムＩＤ = ? " +
                    "WHERE 会員番号 = ? AND サービス種別   = 1";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.restAndExecute(MscdData.kokyaku_no, MscdData.goopon_no, h_card_status, h_cdriyu_cd, h_hakko_ymd,
                    h_ps_corpid, h_ps_corpid, h_kyu_hansya_cd, h_kyu_hansya_cd, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid, h_pid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCard *** MSカード情報UPDNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE",
                        (long) sqlca.sqlcode, "MSカード情報",
                        out_format_buf, 0, 0);

                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_NG);
            }
            /* カードマスタなし */
        } else {
            /* 企業コード設定 */
            h_kigyo_cd.arr = h_ps_corpid.arr;
            /* MSカード情報更新処理 */
//                EXEC SQL INSERT INTO MSカード情報@CMSD
//                        (サービス種別,
//                                会員番号,
//                                顧客番号,
//                                ＧＯＯＰＯＮ番号,                                                             /* 2022/10/12 MCCM初版 ADD */
//                                カードステータス,
//                                理由コード,
//                                発行年月日,
//                                終了年月日,
//                                有効期限,
//                                企業コード,
//                                旧販社コード,
//                                作業企業コード,
//                                作業者ＩＤ,
//                                作業年月日,
//                                作業時刻,
//                                バッチ更新日,
//                                最終更新日,
//                                最終更新日時,
//                                最終更新プログラムＩＤ)
//                        VALUES
//                (1,
//                                 :MscdData.kaiin_no,
//                                 :MscdData.kokyaku_no,
//                                 :MscdData.goopon_no,                                                          /* 2022/10/12 MCCM初版 ADD */
//                        1,                                                                            /* 2022/10/12 MCCM初版 MOD */
//                                 :h_cdriyu_cd,
//                                 :h_hakko_ymd,
//                        0,
//                        0,
//                                 :h_ps_corpid,
//                                 :h_kyu_hansya_cd,                                                             /* 2022/10/12 MCCM初版 MOD */
//                        0,
//                        0,
//                        0,
//                        0,
//                                 :h_bat_yyyymmdd,
//                                 :h_bat_yyyymmdd,
//                        sysdate,
//                                 :h_saishu_koshin_programid);

            StringDto WRKSQL = new StringDto();
            WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MSカード情報(サービス種別,会員番号,顧客番号,ＧＯＯＰＯＮ番号,カードステータス,理由コード,発行年月日,終了年月日,有効期限,企業コード," +
                            "旧販社コード,作業企業コード,作業者ＩＤ,作業年月日,作業時刻,バッチ更新日,最終更新日,最終更新日時,最終更新プログラムＩＤ)\n" +
                            "VALUES(1,'%s','%s','%s',1,'%s','%s',0,0,'%s','%s',0,0,0,0,'%s','%s',sysdate(),'%s')"
                    , MscdData.kaiin_no, MscdData.kokyaku_no, MscdData.goopon_no, h_cdriyu_cd, h_hakko_ymd,
                    h_ps_corpid, h_kyu_hansya_cd, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid);
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute();

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCard *** MSカード情報INSNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT",
                        (long) sqlca.sqlcode, "MSカード情報",
                        out_format_buf, 0, 0);

                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_NG);
            }
        }

        if (DBG_LOG) {
            C_DbgEnd("カード情報更新処理", 0, 0, 0);
        }
        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： SeitaiCheck                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  SeitaiCheck(NYUKAI_RENDO_DATA in_nyukai_rendo,                */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               静態情報更新チェック処理                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int SeitaiCheck(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("静態情報更新チェック処理");
            C_DbgMsg("顧客番号          =[%s]\n", h_uid);
        }

        /*--------------------*/
        /* ＨＯＳＴ変数セット */
        /*--------------------*/
        /* 初期化 */
        memset(h_uid_varchar, 0x00, sizeof(h_uid_varchar));
        /* 顧客番号         */
        memcpy(h_uid_varchar, h_uid.arr, sizeof(h_uid) - 1);
        h_uid_varchar.len = strlen(h_uid);

        h_seitai_flag.arr = 0;
        h_birth_month.arr = 0;
        h_zaiseki_yyyymm.arr = 0;
        h_ec_flag.arr = 0;
        h_tel_flag.arr = 0;

        seitai_flg.arr = 0;
        hi_msks_sw.arr = 0;        /* MS顧客制度情報「なし(0)／あり(1)」 */
        hi_mmki_sw.arr = 0;        /* MM顧客情報    「なし(0)／あり(1)」 */

        /* 2022/10/13 MCCM初版 ADD START */
        h_mcc_seido_kyodaku_flg.arr = 0;
        h_mcc_seido_kyodaku_koshinsha.arr = 0;
        h_mcc_seido_kyodaku_koshin_ymdhms.arr = 0;
        /* 2022/10/13 MCCM初版 ADD END */

        /* MS顧客制度情報取得処理 */
//            EXEC SQL SELECT
//            NVL(静態取込済みフラグ, 0),
//                    NVL(誕生月, 0),
//                    NVL(在籍開始年月, 0),
//                    NVL(ＥＣ会員フラグ, 0),
//                    NVL(電話番号登録フラグ, 0),
//                    NVL(ＭＣＣ制度許諾フラグ, 0),                                                              /* 2022/10/13 MCCM初版 ADD */
//                    NVL(ＭＣＣ制度許諾更新者, 0),                                                              /* 2022/10/13 MCCM初版 ADD */
//                    NVL( TO_NUMBER( TO_CHAR( ＭＣＣ制度許諾更新日時, 'YYYYMMDDHHMMSS' ) ), 0 )                 /* 2022/10/13 MCCM初版 ADD */
//            INTO
//            :h_seitai_flag,
//                    :h_birth_month,
//                    :h_zaiseki_yyyymm,
//                    :h_ec_flag,
//                    :h_tel_flag,
//                    :h_mcc_seido_kyodaku_flg,                                                                  /* 2022/10/13 MCCM初版 ADD */
//                    :h_mcc_seido_kyodaku_koshinsha,                                                            /* 2022/10/13 MCCM初版 ADD */
//                    :h_mcc_seido_kyodaku_koshin_ymdhms                                                         /* 2022/10/13 MCCM初版 ADD */
//            FROM MS顧客制度情報@CMSD
//            WHERE 顧客番号 = :h_uid_varchar;

        sqlca.sql = new StringDto("SELECT\n" +
                "NVL(静態取込済みフラグ, 0),\n" +
                "NVL(誕生月, 0),\n" +
                "NVL(在籍開始年月, 0),\n" +
                "NVL(ＥＣ会員フラグ, 0),\n" +
                "NVL(電話番号登録フラグ, 0),\n" +
                "NVL(ＭＣＣ制度許諾フラグ, 0),                                                         \n" +
                "NVL(ＭＣＣ制度許諾更新者, 0),                                                           \n" +
                "NVL( TO_NUMBER( TO_CHAR( ＭＣＣ制度許諾更新日時, 'YYYYMMDDHHMMSS' ) ), 0 )                \n" +
                "FROM  MS顧客制度情報\n" +
                "WHERE 顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_seitai_flag, h_birth_month, h_zaiseki_yyyymm, h_ec_flag, h_tel_flag,
                h_mcc_seido_kyodaku_flg, h_mcc_seido_kyodaku_koshinsha, h_mcc_seido_kyodaku_koshin_ymdhms);

        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** MS顧客制度情報@CMSD NG %s\n", "");
            }
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    (long) sqlca.sqlcode, "MS顧客制度情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* データ無しエラー */
            seitai_flg.arr = 1;
        } else {
            /* 正常終了 */
            hi_msks_sw.arr = 1;        /* MS顧客制度情報「なし(0)／あり(1)」 */
        }

        if ((sqlca.sqlcode != C_const_Ora_NOTFOUND) && (h_seitai_flag.intVal() != 1)) {
            seitai_flg.arr = 1;
        }

        /* 初期化 */
        h_end_seitai_update_yyyymmdd.arr = 0;
        h_bath_yyyy.arr = 0;
        h_bath_mm.arr = 0;
        h_seibetsu.arr = 0;

        /* MM顧客情報検索処理 */
//            EXEC SQL SELECT
//            NVL(最終静態更新日, 0),
//                    NVL(誕生年, 0),
//                    NVL(誕生月, 0),
//                    NVL(性別, 0)
//            INTO
//            :h_end_seitai_update_yyyymmdd,
//            :h_bath_yyyy,
//            :h_bath_mm,
//            :h_seibetsu
//            FROM MM顧客情報
//            WHERE 顧客番号 = :h_uid_varchar;

        sqlca.sql = new StringDto("SELECT NVL(最終静態更新日, 0),\n" +
                "NVL(誕生年, 0),\n" +
                "NVL(誕生月, 0),\n" +
                "NVL(性別, 0)\n" +
                "FROM MM顧客情報\n" +
                "WHERE 顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_end_seitai_update_yyyymmdd, h_bath_yyyy, h_bath_mm, h_seibetsu);

        /* エラーの場合処理を異常終了する */
        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** MM顧客情報 SELECT NG %s\n", "");
            }
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    (long) sqlca.sqlcode, "MM顧客情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        } else {
            /* 2015/07/30 顧客静態更新チェック条件変更 */
            /* if( h_seitai_flag != 1 || h_end_seitai_update_yyyymmdd < h_kijyun_yyyymmdd ){ */
            if (h_seitai_flag.intVal() != 1 || h_end_seitai_update_yyyymmdd.intVal() <= h_kijyun_yyyymmdd.intVal()) {
                seitai_flg.arr = 1;
            }
            if (sqlca.sqlcode == C_const_Ora_OK) {
                /* 正常終了 */
                hi_mmki_sw.arr = 1;        /* MM顧客情報    「なし(0)／あり(1)」 */
            }
        }

        /* 初期化 */
        h_nyukai_kigyou_cd.arr = 0;
        h_nyukai_tenpo.arr = 0;
        h_nyukai_oldcorp_cd.arr = 0;
        h_haken_kigyou_cd.arr = 0;
        h_haken_tenpo.arr = 0;
        h_nyukai_kaisha_cd_mcc.arr = 0;                     /* 2022/10/13 MCCM初版 ADD */
        h_nyukai_tenpo_mcc.arr = 0;                         /* 2022/10/13 MCCM初版 ADD */


        /* TS利用可能ポイント情報検索処理 */
//            EXEC SQL SELECT
//                入会企業コード,
//                入会店舗,
//                入会旧販社コード,
//                発券企業コード,
//                発券店舗,
//                入会会社コードＭＣＣ,           /* 2022/10/13 MCCM初版 ADD */
//                入会店舗ＭＣＣ                  /* 2022/10/13 MCCM初版 ADD */
//            INTO
//                    :h_nyukai_kigyou_cd,
//                    :h_nyukai_tenpo,
//                    :h_nyukai_oldcorp_cd,
//                    :h_haken_kigyou_cd,
//                    :h_haken_tenpo,
//                    :h_nyukai_kaisha_cd_mcc,        /* 2022/10/13 MCCM初版 ADD */
//                    :h_nyukai_tenpo_mcc             /* 2022/10/13 MCCM初版 ADD */
//            FROM  TS利用可能ポイント情報@CMSD
//            WHERE 顧客番号 = :h_uid_varchar;

        sqlca.sql = new StringDto("SELECT\n" +
                "入会企業コード,\n" +
                "入会店舗,\n" +
                "入会旧販社コード,\n" +
                "発券企業コード,\n" +
                "発券店舗,\n" +
                "入会会社コードＭＣＣ,        \n" +
                "入会店舗ＭＣＣ                \n" +
                "FROM  TS利用可能ポイント情報\n" +
                "WHERE 顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_nyukai_kigyou_cd, h_nyukai_tenpo, h_nyukai_oldcorp_cd, h_haken_kigyou_cd,
                h_haken_tenpo, h_nyukai_kaisha_cd_mcc, h_nyukai_tenpo_mcc);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** TS利用可能ポイント情報 SELECT NG %s\n", "");
            }
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    (long) sqlca.sqlcode, "TS利用可能ポイント情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            C_DbgEnd("静態情報更新チェック処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyaku                                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyaku(NYUKAI_RENDO_DATA in_nyukai_rendo,              */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新処理                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* **************************************************************************** */
    public int UpdateKokyaku(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {
        int rtn_cd;             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto birth_year = new StringDto(5);      /* 誕生年                      */
        StringDto birth_month = new StringDto(3);     /* 誕生月                      */
        StringDto birth_day = new StringDto(3);       /* 誕生日                      */
        IntegerDto age = new IntegerDto();                /* 年齢                        */
        StringDto storeno = new StringDto(7);         /* 店番号                      */
        StringDto tanto_code = new StringDto(11);     /* 担当者コード                */
//  int     i_loop;             /* ループ                      */                                              /* 2022/10/13 MCCM初版 DEL */
        StringDto wk_seibetsu = new StringDto(2);          /* 性別                        */

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新処理");
        }
        /* 初期化 */
        memset(bt_date_yyyy, 0x00, sizeof(bt_date_yyyy));
        memset(bt_date_mm, 0x00, sizeof(bt_date_mm));
        memset(bt_date_d, 0x00, sizeof(bt_date_d));
        memset(birth_year, 0x00, sizeof(birth_year));
        memset(birth_month, 0x00, sizeof(birth_month));
        memset(birth_day, 0x00, sizeof(birth_day));
        memset(storeno, 0x00, sizeof(storeno));
        memset(tanto_code, 0x00, sizeof(tanto_code));
        memset(wk_seibetsu, 0x00, sizeof(wk_seibetsu));
//  i_loop = 0;                                                                                                /* 2022/10/13 MCCM初版 DEL */
        rtn_cd = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /*                                               */
        /* ＨＯＳＴ変数セット                            */
        /*                                               */
        /*----------------------------------*/
        /*---MM顧客情報・MS顧客制度情報-----*/
        /*----------------------------------*/
        /* 誕生月 */
        strncpy(birth_month, in_nyukai_rendo.birth_date.strVal(), sizeof(birth_month) - 1);
        h_birth_month.arr = atoi(birth_month);

        /*----------------------------------*/
        /*---MS顧客制度情報-----------------*/
        /*----------------------------------*/
        /* 電話番号登録フラグ */
        if (memcmp(in_nyukai_rendo.telephone1.strVal(), HALF_SPACE13, strlen(HALF_SPACE13)) != 0
                || memcmp(in_nyukai_rendo.telephone2.strVal(), HALF_SPACE13, strlen(HALF_SPACE13)) != 0) {
            /* 設定あり */
            h_tel_flag.arr = 1;
        } else {
            /* 設定なし */
            h_tel_flag.arr = 0;
        }
        /* 静態取込済みフラグ */
        h_seitai_flag.arr = 1;    /*（取込済）*/

        /*----------------------------------*/
        /*---MM顧客情報---------------------*/
        /*----------------------------------*/
        /* 誕生年 */
        strncpy(birth_year, in_nyukai_rendo.birth_date.strVal(), sizeof(birth_year) - 1);
        h_birth_year.arr = atoi(birth_year);
        /* 誕生日 */
        strncpy(birth_day, in_nyukai_rendo.birth_date.strVal(), sizeof(birth_day) - 1);
        h_birth_day.arr = atoi(birth_day);
        /* 年齢 */
        strncpy(bt_date_yyyy, bat_yyyymmdd.arr, 4);
        strncpy(bt_date_mm, bat_yyyymmdd.arr, 2);
        strncpy(bt_date_d, bat_yyyymmdd.arr, 2);
        rtn_cd = C_CountAge(h_birth_year.intVal(), h_birth_month.intVal(), h_birth_day.intVal(),
                bt_date_yyyy.intVal(), bt_date_mm.intVal(), bt_date_d.intVal(), age);
        if (rtn_cd != C_const_OK) {
            sprintf(out_format_buf, "誕生日=%04d%02d%02d",
                    h_birth_year, h_birth_month, h_birth_day);
            APLOG_WT("903", 0, null, "C_CountAge", (long) rtn_cd,
                    out_format_buf, 0, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }
        h_age.arr = age;
        if (DBG_LOG) {
            C_DbgMsg("*** UpdateKokyaku *** 年齢計算(年齢=[%d])\n", age);
        }
        /* 性別 */
        strncpy(wk_seibetsu, in_nyukai_rendo.seibetsu.strVal(), 1);
        h_seibetsu.arr = atoi(wk_seibetsu);
        if (h_seibetsu.intVal() == 3) {
            h_seibetsu.arr = 0;
        }
        /* 婚姻 */
        //h_marriage = 9; 20240227
        h_marriage.arr = 0;
        /* 社員区分 */
        h_emp_kbn.arr = 0;
        /* 最終静態更新時刻 */
//            EXEC SQL SELECT to_number(to_char(sysdate,'hh24miss'))
//            INTO :h_end_seitai_update_hh24miss
//            FROM DUAL@CMSD;

        sqlca.sql = new StringDto("SELECT to_number(to_char(sysdate(),'hh24miss')) FROM DUAL");
        sqlca.restAndExecute(h_kinou_id);
        sqlca.fetch();
        sqlca.recData(h_end_seitai_update_hh24miss);


        /*                                                   */
        /* MS顧客制度情報・MM顧客情報追加                    */
        /* 顧客情報なし                                      */
        /*                                                   */
        if (DBG_LOG) {
            C_DbgMsg("*** UpdateKokyaku *** 顧客情報 有無%s\n", "");
            C_DbgMsg("MS顧客制度情報「なし(0)／あり(1)」=[%d]\n", hi_msks_sw);
            C_DbgMsg("MM顧客情報    「なし(0)／あり(1)」=[%d]\n", hi_mmki_sw);
        }
        if (hi_msks_sw.intVal() == 0 && hi_mmki_sw.intVal() == 0) {
            rtn_cd = InsertKokyakudata();
            if (rtn_cd != C_const_OK && rtn_cd != C_const_DUPL) {
                /* 重複エラー以外のエラーの場合 */
                /* 処理を終了する */
                return (C_const_NG);
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd == C_const_OK) {
                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_OK);
            }
        } else if (hi_msks_sw.intVal() == 0 || hi_mmki_sw.intVal() == 0) {
            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                   */
        /* 顧客情報ありの場合                                */
        /* 退会済み・重複チェック                            */
        /*                                                   */
        /* 初期化 */
        MmkiData = new MM_KOKYAKU_INFO_TBL();
        MsksData = new MS_KOKYAKU_SEDO_INFO_TBL();
//            memset(MmkiData, 0x00, sizeof(MmkiData));
//            memset(MsksData, 0x00, sizeof(MsksData));

        /* 顧客番号セット */
        strcpy(MmkiData.kokyaku_no, h_uid);
        MmkiData.kokyaku_no.len = strlen(MmkiData.kokyaku_no.arr());

        /* 顧客情報取得 */
        rtn_cd = cmBTfuncB.C_GetCmMaster(MmkiData, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCmMaster", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no.strVal());

        /* 顧客制度情報取得 */
        rtn_cd = cmBTfuncB.C_GetCsMaster(MsksData, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCsMaster", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                   */
        /* MS顧客制度情報・MM顧客情報更新                    */
        /* 顧客情報ありの場合                                */
        /*                                                   */
        rtn_cd = UpdateKokyakudata();
        if (rtn_cd != C_const_OK) {
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            C_DbgEnd("顧客情報更新処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakudata                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakudata()                                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報・MM顧客情報追加                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              -2  ： 重複エラー                                             */
    /*                                                                            */
    /* **************************************************************************** */
    public int InsertKokyakudata() {
        int rtn_cd;             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto wk_sql = new StringDto(); /* 動的SQLバッファ      */
        /* 2022/10/13 MCCM初版 ADD START */
        StringDto kokyaku_myoji = new StringDto(40 * 3 + 1);          /* 顧客名字        */
        StringDto kokyaku_name = new StringDto(40 * 3 + 1);           /* 顧客名前        */
        StringDto kaiin_kana_name = new StringDto(80 * 3 + 1);        /* 会員カナ名称    */
        StringDto kana_name_han = new StringDto(81);              /* カナ氏名(半角)               */
        StringDto wk_kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1); /* カナ顧客名字(全角)           */
        StringDto wk_kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);  /* カナ顧客名前(全角)           */
        StringDto kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1);    /* カナ顧客名字(全角)           */
        StringDto kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);     /* カナ顧客名前(全角)           */
        StringDto wk_nyukai_entry_paper_ymd = new StringDto(8 + 1);    /* 入会申込用紙記載日wk         */
        int i = 0;
        int j = 0;
        String[] wk_buf = new String[4];   /* 一漢字格納用           */
        /* 2022/10/13 MCCM初版 ADD END */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報・MM顧客情報追加処理");
        }

        /* 2022/10/13 MCCM初版 ADD START */
        memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
        memset(kokyaku_name, 0x00, sizeof(kokyaku_name));
        memset(kaiin_kana_name, 0x00, sizeof(kaiin_kana_name));
        memset(kana_name_han, 0x00, sizeof(kana_name_han));
        memset(wk_kana_kokyaku_myoji_zen, 0x00, sizeof(wk_kana_kokyaku_myoji_zen));
        memset(wk_kana_kokyaku_name_zen, 0x00, sizeof(wk_kana_kokyaku_name_zen));
        memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
        memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));
        memset(wk_nyukai_entry_paper_ymd, 0x00, sizeof(wk_nyukai_entry_paper_ymd));

        if (0 == strlen(h_kaiin_name)) {
        } else {
            /* 漢字氏名 */
//                while(h_kaiin_name[i] != '\0') {
//
//                    wk_buf[0] = h_kaiin_name[i];
//                    wk_buf[1] = h_kaiin_name[i+1];
//                    wk_buf[2] = h_kaiin_name[i+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0)
//                    {
//                        break;
//                    }
//                    i = i + 3;
//                }
//
            i = h_kaiin_name.arr.indexOf("　") < 0 ? h_kaiin_name.size() : h_kaiin_name.arr.indexOf("　");
            /* 顧客名字 */
            strncpy(kokyaku_myoji, h_kaiin_name, i);
            /* 顧客名前 */
            strcpy(kokyaku_name, h_kaiin_name);

            /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
            if (strlen(kokyaku_myoji) > 20 * 3 || strlen(kokyaku_name) > 20 * 3) {

                memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
                memset(kokyaku_name, 0x00, sizeof(kokyaku_name));

                /* 顧客名字 */
                strncpy(kokyaku_myoji, h_kaiin_name, 60);
                /* 顧客名前 */
                strcpy(kokyaku_name, h_kaiin_name);

            }

            if (DBG_LOG) {
                C_DbgMsg("kokyaku_myoji [%s]\n", kokyaku_myoji);
                C_DbgMsg("kokyaku_name [%s]\n", kokyaku_name);
            }
        }

        if (0 == strlen(h_kana_name)) {
        } else {

            /* 全角に変換する */
            rtn_cd = C_ConvHalf2Full(h_kana_name, kaiin_kana_name);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvHalf2Full", (long) rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* カナ顧客氏名 */
//                while(kaiin_kana_name[j] != '\0') {
//
//                    wk_buf[0] = kaiin_kana_name[j];
//                    wk_buf[1] = kaiin_kana_name[j+1];
//                    wk_buf[2] = kaiin_kana_name[j+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0)
//                    {
//                        break;
//                    }
//                    j = j + 3;
//                }

            j = h_kaiin_name.arr.indexOf("　") < 0 ? h_kaiin_name.size() : h_kaiin_name.arr.indexOf("　");
            /* 顧客カナ名字 */
            strncpy(kana_kokyaku_myoji_zen, kaiin_kana_name, j);
            /* 顧客カナ名前 */
            strcpy(kana_kokyaku_name_zen, kaiin_kana_name);

            /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
            if (strlen(kana_kokyaku_myoji_zen) > 20 * 3 || strlen(kana_kokyaku_name_zen) > 20 * 3) {

                memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
                memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));

                /* 顧客名字 */
                strncpy(kana_kokyaku_myoji_zen, kaiin_kana_name, 60);
                /* 顧客名前 */
                strcpy(kana_kokyaku_name_zen, kaiin_kana_name);

            }
            if (DBG_LOG) {
                C_DbgMsg("kana_kokyaku_myoji_zen [%s]\n", kana_kokyaku_myoji_zen);
                C_DbgMsg("kana_kokyaku_name_zen [%s]\n", kana_kokyaku_name_zen);
            }
        }
        /* 2022/10/13 MCCM初版 ADD END */

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        rtn_cd = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no.strVal());

        /* ＳＱＬ文をセットする */
        sprintf(wk_sql,
                "INSERT INTO MS顧客制度情報 "
                        + "( 顧客番号,"
                        + "  誕生月,"
                        + "  エントリー,"
                        + "  年次ランクコード０,"
                        + "  年次ランクコード１,"
                        + "  年次ランクコード２,"
                        + "  年次ランクコード３,"
                        + "  年次ランクコード４,"
                        + "  年次ランクコード５,"
                        + "  年次ランクコード６,"
                        + "  年次ランクコード７,"
                        + "  年次ランクコード８,"
                        + "  年次ランクコード９,"
                        + "  月次ランクコード００１,"
                        + "  月次ランクコード００２,"
                        + "  月次ランクコード００３,"
                        + "  月次ランクコード００４,"
                        + "  月次ランクコード００５,"
                        + "  月次ランクコード００６,"
                        + "  月次ランクコード００７,"
                        + "  月次ランクコード００８,"
                        + "  月次ランクコード００９,"
                        + "  月次ランクコード０１０,"
                        + "  月次ランクコード０１１,"
                        + "  月次ランクコード０１２,"
                        + "  月次ランクコード１０１,"
                        + "  月次ランクコード１０２,"
                        + "  月次ランクコード１０３,"
                        + "  月次ランクコード１０４,"
                        + "  月次ランクコード１０５,"
                        + "  月次ランクコード１０６,"
                        + "  月次ランクコード１０７,"
                        + "  月次ランクコード１０８,"
                        + "  月次ランクコード１０９,"
                        + "  月次ランクコード１１０,"
                        + "  月次ランクコード１１１,"
                        + "  月次ランクコード１１２,"
                        + "  在籍開始年月,"
                        + "  社員区分,"
                        + "  電話番号登録フラグ,"
                        + "  静態取込済みフラグ,"
                        + "  家族ＩＤ,"
                        + "  フラグ１,"
                        + "  フラグ２,"
                        + "  フラグ３,"
                        + "  フラグ４,"
                        + "  フラグ５,"
                        + "  作業企業コード,"
                        + "  作業者ＩＤ,"
                        + "  作業年月日,"
                        + "  作業時刻,"
                        + "  バッチ更新日,"
                        + "  最終更新日,"
                        + "  最終更新日時,"
                        + "  最終更新プログラムＩＤ,"
                        + "  顧客ステータス,"                                                                                /* 2022/10/13 MCCM初版 ADD */
                        + "  会員資格区分,"                                                                                  /* 2022/10/13 MCCM初版 ADD */
                        + "  グローバル会員フラグ,"                                                                          /* 2022/10/13 MCCM初版 ADD */
                        + "  ＬＩＮＥコネクト状況,"                                                                          /* 2022/10/13 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾フラグ,"                                                                          /* 2022/10/13 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾更新者,"                                                                          /* 2022/10/13 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾更新日時,"                                                                        /* 2022/10/13 MCCM初版 ADD */
                        + "  コーポレート会員フラグ,"                                                                        /* 2022/10/13 MCCM初版 ADD */
                        + "  属性管理主体システム ) "                                                                        /* 2022/10/13 MCCM初版 ADD */
                        + "VALUES "
                        + "( ?,"
                        + "  ?,"
                        + "  0,"
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  0,"                                                                                             /* 2022/10/25 MCCM初版 MOD */
                        + "  ?,"
                        + "  0,"
                        + "  ?,"
                        + "  1,"
                        + "  0,"
                        + "  NULL,"
                        + "  NULL,"
                        + "  NULL,"
                        + "  NULL,"
                        + "  NULL,"
                        + "  0,"
                        + "  0,"
                        + "  0,"
                        + "  0,"
                        + "  ?,"
                        + "  ?,"
                        + "  sysdate(),"
                        + "  ?,"
                        + "  1,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  1,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  1,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  4,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  sysdate(),"                                                                                       /* 2022/10/13 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/13 MCCM初版 ADD */
                        + "  1)"                                                                                             /* 2022/10/13 MCCM初版 ADD */
        );

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudata *** クエリ=[%s]\n", wk_sql);
        }

        /* 動的ＳＱＬ文を解析する */
//            EXEC SQL PREPARE sql_stat1 from :str_sql;
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ 解析NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "PREPARE", (long) sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);

            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ 解析OK %s\n", "");
        }

        /* 動的ＳＱＬ文を実行する */
//            EXEC SQL  EXECUTE sql_stat1
//            USING   :MsksData.kokyaku_no,
//                      :h_birth_month,
//                      :h_kijyun_yyyymm,
//                      :h_tel_flag,
//                      :h_bat_yyyymmdd,
//                      :h_bat_yyyymmdd,
//                      :h_saishu_koshin_programid;

        sqlca.restAndExecute(MsksData.kokyaku_no, h_birth_month, h_kijyun_yyyymm, h_tel_flag, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid);

        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* 重複エラー以外のエラーの場合 */
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ文NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", (long) sqlca.sqlcode, "MS顧客制度情報", out_format_buf, 0, 0);

            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ文OK %s\n", "");
        }

        /* 重複エラーの場合 */
        if (sqlca.sqlcode == C_const_Ora_DUPL) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudata *** ポイント・顧客ロック%s\n", "");
            }
            rtn_cd = C_KdataLock(h_uid, "2", rtn_status);
            if (rtn_cd == C_const_NG) {
                if (DBG_LOG) {
                    C_DbgMsg("*** InsertKokyakudata *** ポイント・顧客ロック status= %d\n", rtn_status);
                }
                APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客データ有無フラグセット */
            uid_flg = rtn_cd;

            /* 重複エラー */
            return (C_const_DUPL);
        } else {
            /* 正常の場合 */
            /* 初期化 */
//                memset(MmkiData, 0x00, sizeof(MmkiData));
            MmkiData = new MM_KOKYAKU_INFO_TBL();
            /* 顧客番号セット */
            memcpy(MmkiData.kokyaku_no, h_uid.arr, sizeof(h_uid) - 1);
            MmkiData.kokyaku_no.len = strlen(h_uid);
            /* 顧客名称         */
            if (strlen(h_kaiin_name) == 0) {
//          memcpy(MmkiData.kokyaku_mesho, " ", sizeof(MmkiData.kokyaku_mesho)-1);                             /* 2022/10/13 MCCM初版 DEL */
                memcpy(MmkiData.kokyaku_myoji, " ", sizeof(MmkiData.kokyaku_myoji.strVal()) - 1);                             /* 2022/10/13 MCCM初版 ADD */
                memcpy(MmkiData.kokyaku_name, " ", sizeof(MmkiData.kokyaku_name.strVal()) - 1);                              /* 2022/10/13 MCCM初版 ADD */
            } else {
//          memcpy(MmkiData.kokyaku_mesho, h_kaiin_name, sizeof(MmkiData.kokyaku_mesho)-1);                    /* 2022/10/13 MCCM初版 DEL */
                memcpy(MmkiData.kokyaku_myoji, kokyaku_myoji.arr, sizeof(MmkiData.kokyaku_myoji.strVal()) - 1);                   /* 2022/10/13 MCCM初版 ADD */
                memcpy(MmkiData.kokyaku_name, kokyaku_name.arr, sizeof(MmkiData.kokyaku_name.strVal()) - 1);                    /* 2022/10/13 MCCM初版 ADD */
            }
            /* 顧客カナ名称         */
            if (strlen(h_kana_name) == 0) {
//          memcpy(h_kana_name, " ", sizeof(h_kana_name)-1);                                                     /* 2022/10/13 MCCM初版 DEL */
                memcpy(MmkiData.kana_kokyaku_myoji, " ", sizeof(MmkiData.kana_kokyaku_myoji.strVal()) - 1);                     /* 2022/10/13 MCCM初版 ADD */
                memcpy(MmkiData.kana_kokyaku_name, " ", sizeof(MmkiData.kana_kokyaku_name.strVal()) - 1);                      /* 2022/10/13 MCCM初版 ADD */
            } else {                                                                                                 /* 2022/10/13 MCCM初版 ADD */
                memcpy(MmkiData.kana_kokyaku_myoji, kana_kokyaku_myoji_zen.arr, sizeof(MmkiData.kana_kokyaku_myoji.strVal()) - 1);  /* 2022/10/13 MCCM初版 ADD */
                memcpy(MmkiData.kana_kokyaku_name, kana_kokyaku_name_zen.arr, sizeof(MmkiData.kana_kokyaku_name.strVal()) - 1);    /* 2022/10/13 MCCM初版 ADD */
            }
            /* 入会申込用紙記載日   */
            memcpy(wk_nyukai_entry_paper_ymd, in_nyukai_rendo.moushikomi_date.strVal(), sizeof(in_nyukai_rendo.moushikomi_date.strVal()));   /* 2022/10/13 MCCM初版 ADD */
            MmkiData.nyukai_entry_paper_ymd.arr = atoi(wk_nyukai_entry_paper_ymd);                                             /* 2022/10/13 MCCM初版 ADD */

            if (DBG_LOG) {
                C_DbgMsg("kana_kokyaku_name : sqlbuf=[%s]\n", MmkiData.kana_kokyaku_name);
            }

            /* INSERTする */
//                EXEC SQL INSERT INTO MM顧客情報
//                        (   顧客番号,
//                                休眠フラグ,
//                                顧客名字,                                                                              /* 2022/10/13 MCCM初版 MOD */
//                                顧客名前,                                                                              /* 2022/10/13 MCCM初版 ADD */
//                                カナ顧客名字,                                                                          /* 2022/10/13 MCCM初版 MOD */
//                                カナ顧客名前,                                                                          /* 2022/10/13 MCCM初版 ADD */
//                                年齢,
//                                誕生年,
//                                誕生月,
//                                誕生日,
//                                性別,
//                                婚姻,
//                                入会企業コード,
//                                /*                      入会店舗,                                                                            *//* 2022/10/13 MCCM初版 DEL */
//                                発券企業コード,
//                                発券店舗,
//                                社員区分,
//                                ポータル入会年月日,
//                                ポータル退会年月日,
//                                作業企業コード,
//                                作業者ＩＤ,
//                                作業年月日,
//                                作業時刻,
//                                バッチ更新日,
//                                最終静態更新日,
//                                最終静態更新時刻,
//                                最終更新日,
//                                最終更新日時,
//                                最終更新プログラムＩＤ,
//                                顧客ステータス,                                                                        /* 2022/10/13 MCCM初版 ADD */
//                                入会申込用紙記載日,                                                                    /* 2022/10/13 MCCM初版 ADD */
//                                静態登録日,                                                                            /* 2022/10/13 MCCM初版 ADD */
//                                入会会社コードＭＣＣ,                                                                  /* 2022/10/13 MCCM初版 ADD */
//                                入会店舗ＭＣＣ,                                                                        /* 2022/10/13 MCCM初版 ADD */
//                                シニア,                                                                                /* 2022/10/13 MCCM初版 ADD */
//                                属性管理主体システム,                                                                  /* 2022/10/13 MCCM初版 ADD */
//                                プッシュ通知許可フラグ,                                                                /* 2022/10/13 MCCM初版 ADD */
//                                メールアドレス１送信フラグ,                                                            /* 2022/10/13 MCCM初版 ADD */
//                                メールアドレス２送信フラグ,                                                            /* 2022/10/13 MCCM初版 ADD */
//                                メールアドレス３送信フラグ )                                                           /* 2022/10/13 MCCM初版 ADD */
//                VALUES (  :MmkiData.kokyaku_no,
//                        0,
//                        :MmkiData.kokyaku_myoji,                                                               /* 2022/10/13 MCCM初版 MOD */
//                        :MmkiData.kokyaku_name,                                                                /* 2022/10/13 MCCM初版 ADD */
//                        :MmkiData.kana_kokyaku_myoji,                                                          /* 2022/10/13 MCCM初版 MOD */
//                        :MmkiData.kana_kokyaku_name,                                                           /* 2022/10/13 MCCM初版 ADD */
//                        :h_age,
//                        :h_birth_year,
//                        :h_birth_month,
//                        :h_birth_day,
//                        :h_seibetsu,
//                        :h_marriage,
//                        :h_ps_corpid,
//                /*                      :h_nyukaiten_no,                                                                      *//* 2022/10/13 MCCM初版 DEL */
//                        :h_ps_corpid,
//                        :h_nyukaiten_no,
//                        :h_emp_kbn,
//                        0,
//                        0,
//                        0,
//                        0,
//                        0,
//                        0,
//                        :h_bat_yyyymmdd,
//                        :h_bat_yyyymmdd,
//                        :h_end_seitai_update_hh24miss,
//                        :h_bat_yyyymmdd,
//                        sysdate,
//                        :h_saishu_koshin_programid,
//                        1,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        :MmkiData.nyukai_entry_paper_ymd,                                                      /* 2022/10/13 MCCM初版 ADD */
//                        :h_bat_yyyymmdd,                                                                       /* 2022/10/13 MCCM初版 ADD */
//                        2500,                                                                                  /* 2022/10/13 MCCM初版 ADD */
//                        :h_renkei_mise_no,                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        0,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        1,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        0,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        0,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        0,                                                                                     /* 2022/10/13 MCCM初版 ADD */
//                        0);                                                                                    /* 2022/10/13 MCCM初版 ADD */


            StringDto WRKSQL = new StringDto();
            sprintf(WRKSQL, "INSERT INTO MM顧客情報" +
                            "                    (   顧客番号," +
                            "                        休眠フラグ," +
                            "                        顧客名字,  " +
                            "                        顧客名前,  " +
                            "                        カナ顧客名字," +
                            "                        カナ顧客名前," +
                            "                        年齢," +
                            "                        誕生年," +
                            "                        誕生月," +
                            "                        誕生日," +
                            "                        性別," +
                            "                        婚姻," +
                            "                        入会企業コード," +
                            "                        発券企業コード,\n" +
                            "                        発券店舗,\n" +
                            "                        社員区分,\n" +
                            "                        ポータル入会年月日,\n" +
                            "                        ポータル退会年月日,\n" +
                            "                        作業企業コード,\n" +
                            "                        作業者ＩＤ,\n" +
                            "                        作業年月日,\n" +
                            "                        作業時刻,\n" +
                            "                        バッチ更新日,\n" +
                            "                        最終静態更新日,\n" +
                            "                        最終静態更新時刻,\n" +
                            "                        最終更新日,\n" +
                            "                        最終更新日時,\n" +
                            "                        最終更新プログラムＩＤ,\n" +
                            "                        顧客ステータス,                                                                        \n" +
                            "                        入会申込用紙記載日,                                                                  \n" +
                            "                        静態登録日,                                                             \n" +
                            "                        入会会社コードＭＣＣ,                                                    \n" +
                            "                        入会店舗ＭＣＣ,                                                          \n" +
                            "                        シニア,                                                                               \n" +
                            "                        属性管理主体システム,                                                                \n" +
                            "                        プッシュ通知許可フラグ,                                                                 \n" +
                            "                        メールアドレス１送信フラグ,                                                            \n" +
                            "                        メールアドレス２送信フラグ,                                                            \n" +
                            "                        メールアドレス３送信フラグ )                                                           \n" +
                            "              VALUES (  %d,\n" +
                            "                        0,\n" +
                            "                        '%s',                                                               \n" +
                            "                        '%s',                                                          \n" +
                            "                        '%s',                                                          \n" +
                            "                        '%s',                                                          \n" +
                            "                        '%s',\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        0,\n" +
                            "                        0,\n" +
                            "                        0,\n" +
                            "                        0,\n" +
                            "                        0,\n" +
                            "                        0,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        %d,\n" +
                            "                        sysdate(),\n" +
                            "                        '%s',\n" +
                            "                        1,                                                                                     \n" +
                            "                        %d,                                                      \n" +
                            "                        %d,                                                      \n" +
                            "                        2500,                                                                                  \n" +
                            "                        %d,                                                                     \n" +
                            "                        0,                                                                                    \n" +
                            "                        1,                                                                                    \n" +
                            "                        0,                                                                                    \n" +
                            "                        0,                                                                                    \n" +
                            "                        0,                                                                                    \n" +
                            "                        0)"
                    , MmkiData.kokyaku_no.longVal(), MmkiData.kokyaku_myoji, MmkiData.kokyaku_name, MmkiData.kana_kokyaku_myoji, MmkiData.kana_kokyaku_name,
                    h_age, h_birth_year.longVal(), h_birth_month.longVal(), h_birth_day.longVal(), h_seibetsu.longVal(), h_marriage.longVal(), h_ps_corpid, h_ps_corpid,
                    h_nyukaiten_no.longVal(), h_emp_kbn.longVal(), h_bat_yyyymmdd.longVal(),
                    h_bat_yyyymmdd.longVal(), h_end_seitai_update_hh24miss.longVal(), h_bat_yyyymmdd.longVal(), h_saishu_koshin_programid, MmkiData.nyukai_entry_paper_ymd.longVal(),
                    h_bat_yyyymmdd.longVal(), h_renkei_mise_no.longVal());
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute();

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", MmkiData.kokyaku_no.arr);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", (long) sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("MS顧客制度情報・MM顧客情報追加処理", 0, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_OK);
        }
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakudata                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakudata()                                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報・MM顧客情報更新                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKokyakudata() {

        StringDto wk_sql_buf = new StringDto(C_const_SQLMaxLen);    /* ＳＱＬ文編集用 */
        StringDto wk_sql_item = new StringDto(512);                /* 動的SQLバッファ */
        int wi_upditem_sw;                    /* 項目変更しない(0)／する(1)ＳＷＩＴＣＨ */
        /* 2015/07/30 誕生月設定条件変更　未使用変数削除 */
        int rtn_cd;                                /* 関数戻り値 */
        /*int        wi_age;*/                            /* 年齢 */
        /* 2022/10/13 MCCM初版 ADD START */
        StringDto kokyaku_myoji = new StringDto();          /* 顧客名字        */
        StringDto kokyaku_name = new StringDto();           /* 顧客名前        */
        StringDto kaiin_kana_name = new StringDto();        /* 会員カナ名称    */
        StringDto kana_name_han = new StringDto();              /* カナ氏名(半角)               */
        StringDto wk_kana_kokyaku_myoji_zen = new StringDto(); /* カナ顧客名字(全角)           */
        StringDto wk_kana_kokyaku_name_zen = new StringDto();  /* カナ顧客名前(全角)           */
        StringDto kana_kokyaku_myoji_zen = new StringDto();    /* カナ顧客名字(全角)           */
        StringDto kana_kokyaku_name_zen = new StringDto();     /* カナ顧客名前(全角)           */
        int i = 0;
        int j = 0;
        StringDto wk_buf = new StringDto(4);   /* 一漢字格納用           */
        /* 2022/10/13 MCCM初版 ADD END */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報・MM顧客情報更新処理");
        }

        /*----------------------------------*/
        /*---項目共通の更新条件設定---------*/
        /*----------------------------------*/
    /* ☆:[MS顧客制度情報.ＥＣ会員フラグ]=1(入会済み) かつ
        入力データの[会員氏名(漢字)[[会員氏名(カナ)][郵便番号][住所１][住所２]
        のいずれかの項目が未設定 */
        if (MsksData.ec_kaiin_flg.intVal() == 1 &&
                (0 == strlen(h_kaiin_name) || 0 == strlen(h_kana_name) || 0 == strlen(h_zip)
                        || 0 == strlen(h_address1) || 0 == strlen(h_address2))) {
            wi_upditem_sw = 0;
        } else {
            wi_upditem_sw = 1;
        }

        /* 2022/10/13 MCCM初版 ADD START */
        memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
        memset(kokyaku_name, 0x00, sizeof(kokyaku_name));
        memset(kaiin_kana_name, 0x00, sizeof(kaiin_kana_name));
        memset(kana_name_han, 0x00, sizeof(kana_name_han));
        memset(wk_kana_kokyaku_myoji_zen, 0x00, sizeof(wk_kana_kokyaku_myoji_zen));
        memset(wk_kana_kokyaku_name_zen, 0x00, sizeof(wk_kana_kokyaku_name_zen));
        memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
        memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));

        if (0 == strlen(h_kaiin_name)) {
        } else {

//                while(h_kaiin_name[i] != '\0') {
//
//                    wk_buf[0] = h_kaiin_name[i];
//                    wk_buf[1] = h_kaiin_name[i+1];
//                    wk_buf[2] = h_kaiin_name[i+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0)
//                    {
//                        break;
//                    }
//                    i = i + 3;
//                }
            i = h_kaiin_name.arr.indexOf("　") < 0 ? h_kaiin_name.size() : h_kaiin_name.arr.indexOf("　");
            /* 顧客名字 */
            strncpy(kokyaku_myoji, h_kaiin_name, i);
            /* 顧客名前 */
            strcpy(kokyaku_name, h_kaiin_name);

            /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
            if (strlen(kokyaku_myoji) > 20 * 3 || strlen(kokyaku_name) > 20 * 3) {

                memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
                memset(kokyaku_name, 0x00, sizeof(kokyaku_name));

                /* 顧客名字 */
                strncpy(kokyaku_myoji, h_kaiin_name, 60);
                /* 顧客名前 */
                strcpy(kokyaku_name, h_kaiin_name);

            }

            if (DBG_LOG) {
                C_DbgMsg("kokyaku_myoji [%s]\n", kokyaku_myoji);
                C_DbgMsg("kokyaku_name [%s]\n", kokyaku_name);
            }
        }

        if (0 == strlen(h_kana_name)) {
        } else {

            /* 全角に変換する */
            rtn_cd = C_ConvHalf2Full(h_kana_name, kaiin_kana_name);
            if (rtn_cd != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvHalf2Full", (long) rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* カナ顧客氏名 */
//                while(kaiin_kana_name[j] != '\0') {
//
//                    wk_buf[0] = kaiin_kana_name[j];
//                    wk_buf[1] = kaiin_kana_name[j+1];
//                    wk_buf[2] = kaiin_kana_name[j+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0)
//                    {
//                        break;
//                    }
//                    j = j + 3;
//                }

            j = kaiin_kana_name.arr.indexOf("　") < 0 ? kaiin_kana_name.size() : kaiin_kana_name.arr.indexOf("　");

            /* 顧客カナ名字 */
            strncpy(kana_kokyaku_myoji_zen, kaiin_kana_name, j);
            /* 顧客カナ名前 */
            strcpy(kana_kokyaku_name_zen, kaiin_kana_name);

            /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
            if (strlen(kana_kokyaku_myoji_zen) > 20 * 3 || strlen(kana_kokyaku_name_zen) > 20 * 3) {

                memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
                memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));

                /* 顧客名字 */
                strncpy(kana_kokyaku_myoji_zen, kaiin_kana_name, 60);
                /* 顧客名前 */
                strcpy(kana_kokyaku_name_zen, kaiin_kana_name);

            }
            if (DBG_LOG) {
                C_DbgMsg("kana_kokyaku_myoji_zen [%s]\n", kana_kokyaku_myoji_zen);
                C_DbgMsg("kana_kokyaku_name_zen [%s]\n", kana_kokyaku_name_zen);
            }
        }
        /* 2022/10/13 MCCM初版 ADD END */

        if (wi_upditem_sw == 1) {
            /*----------------------------------*/
            /*---MM顧客情報更新処理-------------*/
            /*----------------------------------*/
            /* ＳＱＬを生成する */
            strcpy(wk_sql_buf, "UPDATE MM顧客情報  SET ");

            if (0 == strlen(h_kaiin_name) || wi_upditem_sw == 0) {
            }                                                  /* 2022/10/13 MCCM初版 MOD */ else {                                                                                                      /* 2022/10/13 MCCM初版 MOD */
                /* 顧客名字 */                                                                                             /* 2022/10/13 MCCM初版 MOD */
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/13 MCCM初版 MOD */
                sprintf(wk_sql_item, "顧客名字 = '%s',", kokyaku_myoji);                                                   /* 2022/10/13 MCCM初版 MOD */
                strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/13 MCCM初版 MOD */
                /* 顧客名前 */                                                                                             /* 2022/10/13 MCCM初版 MOD */
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/13 MCCM初版 MOD */
                sprintf(wk_sql_item, "顧客名前 = '%s',", kokyaku_name);                                                    /* 2022/10/13 MCCM初版 MOD */
                strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/13 MCCM初版 MOD */
            }                                                                                                          /* 2022/10/13 MCCM初版 MOD */

            if (0 == strlen(h_kana_name) || wi_upditem_sw == 0) {
            }                                                   /* 2022/10/13 MCCM初版 MOD */ else {                                                                                                      /* 2022/10/13 MCCM初版 MOD */
                /* カナ顧客名字 */                                                                                         /* 2022/10/13 MCCM初版 MOD */
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/13 MCCM初版 MOD */
                sprintf(wk_sql_item, "カナ顧客名字 = '%s',", kana_kokyaku_myoji_zen);                                      /* 2022/10/13 MCCM初版 MOD */
                strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/13 MCCM初版 MOD */
                /* カナ顧客名前 */                                                                                         /* 2022/10/13 MCCM初版 MOD */
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/13 MCCM初版 MOD */
                sprintf(wk_sql_item, "カナ顧客名前 = '%s',", kana_kokyaku_name_zen);                                       /* 2022/10/13 MCCM初版 MOD */
                strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/13 MCCM初版 MOD */
            }                                                                                                          /* 2022/10/13 MCCM初版 MOD */

            /* 年齢 */
            if ((h_birth_year.intVal() == 0 && h_birth_month.intVal() == 0 && h_birth_day.intVal() == 0) || wi_upditem_sw == 0) {
            } else {
                /* 2015/07/30 誕生月設定条件変更                               */
                /* ☆:[MS顧客制度情報.ＥＣ会員フラグ]=1(入会済み) かつ         */
                /*  入力データの[会員氏名(漢字)[[会員氏名(カナ)][郵便番号][住所１][住所２]すべて設定あり かつ */
                /*  [MS顧客制度情報.誕生月]≠0(設定あり) の場合、              */
                /*  [MS顧客制度情報.誕生月]で計算する→入力データで計算する    */
                /*if ( MsksData.ec_kaiin_flg == 1 && MmkiData.tanjo_m != 0 ){   */
                /*    rtn_cd = C_CountAge(h_birth_year, MmkiData.tanjo_m, h_birth_day,  */
                /*                atoi(bt_date_yyyy), atoi(bt_date_mm), atoi(bt_date_d),wi_age);  */
                /*    if(rtn_cd != C_const_OK ){    */
                /*        sprintf( out_format_buf, "誕生日=%04d%02d%02d",   */
                /*                 h_birth_year, MmkiData.tanjo_m, h_birth_day );   */
                /*        APLOG_WT( "903", 0, null, "C_CountAge", (long)rtn_cd,     */
                /*                                            out_format_buf ,0 ,0 ,0); */
                /* 処理を終了する */
                /*        return(C_const_NG);   */
                /*    } */
                /*    h_age = wi_age;   */
                /*} */
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "年齢 = %d,", h_age.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 誕生年 */
            if (h_birth_year.intVal() == 0 || wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "誕生年 = %d,", h_birth_year.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 誕生月 */
            /* 2015/07/30 誕生月設定条件変更                               */
            /* ☆:[MS顧客制度情報.ＥＣ会員フラグ]=1(入会済み) かつ         */
            /*  入力データの[会員氏名(漢字)[[会員氏名(カナ)][郵便番号][住所１][住所２]すべて設定あり かつ */
            /*  [MS顧客制度情報.誕生月]≠0(設定あり) の場合、更新しない→更新する */
            /*if ( h_birth_month    == 0 || wi_upditem_sw == 0 ||   */
            /*    (MsksData.ec_kaiin_flg == 1 && MmkiData.tanjo_m != 0 && wi_upditem_sw != 0) ){ }  */
            if (h_birth_month.intVal() == 0 || wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "誕生月 = %d,", h_birth_month.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 誕生日 */
            if (h_birth_day.intVal() == 0 || wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "誕生日 = %d,", h_birth_day.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 性別 */
            if (wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "性別 = %d,", h_seibetsu.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /*入会企業コード */
            if (h_nyukaiten_no.intVal() != 0) {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "入会企業コード = %d,", h_ps_corpid);
                strcat(wk_sql_buf, wk_sql_item);
            }
//        /* 入会店舗 */                                                                                       /* 2022/10/13 MCCM初版 DEL */
//        if ( h_nyukaiten_no != 0 ) {                                                                         /* 2022/10/13 MCCM初版 DEL */
//            memset( wk_sql_item, 0x00, sizeof(wk_sql_item));                                                 /* 2022/10/13 MCCM初版 DEL */
//            sprintf(wk_sql_item, "入会店舗 = %d,", h_nyukaiten_no);
//            strcat( wk_sql_buf , wk_sql_item);
//        }
            /* 発券企業コード */
            if (h_nyukaiten_no.intVal() != 0) {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "発券企業コード = %d,", h_ps_corpid);
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 発券店舗 */
            if (h_nyukaiten_no.intVal() != 0) {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "発券店舗 = %d,", h_nyukaiten_no.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* バッチ更新日 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "バッチ更新日 = %d,", h_bat_yyyymmdd.intVal());
            strcat(wk_sql_buf, wk_sql_item);

            /* 最終静態更新日 */
            if (wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "最終静態更新日 = %d,", h_bat_yyyymmdd.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 最終静態更新時刻 */
            if (wi_upditem_sw == 0) {
            } else {
                memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
                sprintf(wk_sql_item, "最終静態更新時刻 = %d,", h_end_seitai_update_hh24miss.intVal());
                strcat(wk_sql_buf, wk_sql_item);
            }
            /* 最終更新日・日時・プログラムＩＤ */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item,
                    "最終更新日 = %d," +
                            "最終更新日時 = SYSDATE()," +
                            "最終更新プログラムＩＤ = '%s', "
                    , h_bat_yyyymmdd.intVal(), h_saishu_koshin_programid);
            strcat(wk_sql_buf, wk_sql_item);

            /* 2022/10/13 MCCM初版 ADD START */
            /* 入会会社コードＭＣＣ */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "入会会社コードＭＣＣ = %d,", 2500);
            strcat(wk_sql_buf, wk_sql_item);
            /* 入会店舗ＭＣＣ */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "入会店舗ＭＣＣ = %d", h_renkei_mise_no);
            strcat(wk_sql_buf, wk_sql_item);
            /* 2022/10/13 MCCM初版 ADD END */

            /* WHERE句 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, " WHERE 顧客番号 = %s", h_uid);
            strcat(wk_sql_buf, wk_sql_item);

            if (DBG_LOG) {
                C_DbgMsg("***UpdateKokyakudata*** MM顧客情報更新 : sqlbuf=[%s]\n", wk_sql_buf);
            }

            /* ＳＱＬ文をセットする */
            memset(str_sql, 0x00, sizeof(str_sql));
            strcpy(str_sql, wk_sql_buf);

//                EXEC SQL PREPARE sql_stat2 from :str_sql;
            sqlca.sql = str_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", (long) sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* UPDATE文を実行する */
//                EXEC SQL EXECUTE sql_stat2;
            sqlca.restAndExecute();

            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
        }

        /*----------------------------------*/
        /*---MS顧客制度情報更新処理---------*/
        /*----------------------------------*/
        /* ＳＱＬを生成する */
        memset(wk_sql_buf, 0x00, sizeof(wk_sql_buf));
        strcpy(wk_sql_buf, "UPDATE MS顧客制度情報  SET ");

        /* 誕生月 */
        /* 2015/07/30 誕生月設定条件変更                               */
        /* ☆:[MS顧客制度情報.ＥＣ会員フラグ]=1(入会済み) かつ         */
        /*  入力データの[会員氏名(漢字)[[会員氏名(カナ)][郵便番号][住所１][住所２]すべて設定あり かつ */
        /*  [MS顧客制度情報.誕生月]≠0(設定あり) の場合、更新しない→更新する */
        /*if ( h_birth_month    == 0 || wi_upditem_sw == 0 ||   */
        /*            (MsksData.ec_kaiin_flg == 1 && MsksData.tanjo_m != 0 && wi_upditem_sw != 0) ){ }  */
        if (h_birth_month.intVal() == 0 || wi_upditem_sw == 0) {
        } else {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "誕生月 = %d,", h_birth_month);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 在籍開始年月 */
        /*    if ( MsksData.zaiseki_kaishi_ym != 0 ){ */
        /*        if (MmkgData.nyukai_ymd == 0 */
        /*               || h_kijyun_yyyymmdd < MmkgData.nyukai_ymd) { */ /* 基準日を設定 */
        /*            memset( wk_sql_item, 0x00, sizeof(wk_sql_item)); */
        /*            sprintf(wk_sql_item, "在籍開始年月 = %d,", h_kijyun_yyyymm); */
        /*            strcat( wk_sql_buf , wk_sql_item); */
        /*        } */
        /*    } */
        /*    else{  */
        /*        memset( wk_sql_item, 0x00, sizeof(wk_sql_item)); */
        /*        sprintf(wk_sql_item, "在籍開始年月 = %d,", h_kijyun_yyyymm); */
        /*        strcat( wk_sql_buf , wk_sql_item); */
        /*    } */
        /* 電話番号登録フラグ */
        if (h_tel_flag.intVal() == 1) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "電話番号登録フラグ = %d,", h_tel_flag);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 静態取込済みフラグ */
        if (wi_upditem_sw == 0) {
        } else {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "静態取込済みフラグ = %d,", h_seitai_flag);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* バッチ更新日 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "バッチ更新日 = %d,", h_bat_yyyymmdd);
        strcat(wk_sql_buf, wk_sql_item);
        /* 最終更新日・日時・プログラムＩＤ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item,
                "最終更新日 = %d," +
                        "最終更新日時 = SYSDATE()," +
                        "最終更新プログラムＩＤ = '%s' "
                , h_bat_yyyymmdd, h_saishu_koshin_programid);
        strcat(wk_sql_buf, wk_sql_item);

        /* 2022/10/13 MCCM初版 ADD START */
        /* ＭＣＣ制度許諾フラグ  */
        if (h_mcc_seido_kyodaku_flg.intVal() == 0) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, ",ＭＣＣ制度許諾フラグ = %d, " +
                    "ＭＣＣ制度許諾更新者 = %d, " +
                    "ＭＣＣ制度許諾更新日時 = SYSDATE() ", 1, 4);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 2022/10/13 MCCM初版 ADD END */

        /* WHERE句 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, " WHERE 顧客番号 = %s", h_uid);
        strcat(wk_sql_buf, wk_sql_item);

        if (DBG_LOG) {
            C_DbgMsg("***UpdateKokyakudata*** MS顧客制度情報更新 : sqlbuf=[%s]\n", wk_sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql_buf);

//            EXEC SQL PREPARE sql_stat3 from :str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", (long) sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* UPDATE文を実行する */
//            EXEC SQL EXECUTE sql_stat3;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }


        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MS顧客制度情報・MM顧客情報更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokusei                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo,       */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客属性情報更新処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKokyakuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo
            , int punch_errno) {
        int rtn_cd;                 /* 関数戻り値                       */
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */
        StringDto custbarcode = new StringDto(30);        /* カスタマーバーコード             */
        StringDto tyoazcode = new StringDto(12);          /* 町字コード                       */
        StringDto wk_kensaku_tel1 = new StringDto(15 + 1);  /* ハイフンなし電話番号１           */
        StringDto wk_kensaku_tel2 = new StringDto(15 + 1);  /* ハイフンなし電話番号２           */
//  int     i_loop;                 /* ループ                           */                                     /* 2022/10/13 MCCM初版 DEL */
        int wi_upditem_sw;          /* 項目変更しない(0)／する(1)ＳＷＩＴＣＨ */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客属性情報更新処理");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(h_zip_code, 0x00, sizeof(h_zip_code));
        memset(h_telephone1, 0x00, sizeof(h_telephone1));
        memset(h_telephone2, 0x00, sizeof(h_telephone2));
        memset(h_kensaku_denwa_no_1, 0x00, sizeof(h_kensaku_denwa_no_1));
        memset(h_kensaku_denwa_no_2, 0x00, sizeof(h_kensaku_denwa_no_2));
        memset(h_email1, 0x00, sizeof(h_email1));
        memset(h_email2, 0x00, sizeof(h_email2));
        memset(h_email3, 0x00, sizeof(h_email3));                                                                  /* 2023/02/14 MCCM初版 ADD */
        memset(h_telephone3, 0x00, sizeof(h_telephone3));
        memset(h_telephone4, 0x00, sizeof(h_telephone4));
        memset(h_kensaku_denwa_no_3, 0x00, sizeof(h_kensaku_denwa_no_3));
        memset(h_kensaku_denwa_no_4, 0x00, sizeof(h_kensaku_denwa_no_4));
        memset(h_job, 0x00, sizeof(h_job));
        memset(custbarcode, 0x00, sizeof(custbarcode));
        memset(tyoazcode, 0x00, sizeof(tyoazcode));
        memset(wk_kensaku_tel1, 0x00, sizeof(wk_kensaku_tel1));
        memset(wk_kensaku_tel2, 0x00, sizeof(wk_kensaku_tel2));
        memset(h_jitaku_jusho_code, 0x00, sizeof(h_jitaku_jusho_code));
//  i_loop = 0;                                                                                                /* 2022/10/13 MCCM初版 DEL */
        rtn_cd = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /*                                               */
        /* ＨＯＳＴ変数セット                            */
        /*                                               */

        /* 郵便番号コード */
        if (strlen(h_zip) != 0 || strlen(h_address) != 0) {
            rtn_cd = C_GetPostBarCode(h_address,                                                                   /* 2022/10/13 MCCM初版 MOD */
                    h_zip, custbarcode, rtn_status);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*----------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKokyakuzokusei *** 郵便番号コード取得(追加)NG %s\n", "");
                    /*----------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_GetPostBarCode",
                        rtn_cd, rtn_status,
                        0, 0, 0);

                return C_const_NG;
            }
            strncpy(h_zip_code, custbarcode.arr, sizeof(h_zip_code) - 1);
        }

        if (memcmp(in_nyukai_rendo.telephone1.strVal(), HALF_SPACE13, strlen(HALF_SPACE13)) != 0) {
            /* 電話番号１ */
            /* 2022/12/01 MCCM初版 MOD */
//        strncpy(h_telephone1, in_nyukai_rendo.telephone1, sizeof(h_telephone1) - 1);
            strncpy(h_telephone1, in_nyukai_rendo.telephone1.strVal(), sizeof(in_nyukai_rendo.telephone1.strVal()));
            /* 2022/12/01 MCCM初版 MOD */

            /* 電話番号（ハイフンを削除） */
            rtn_cd = C_ConvTelNo(h_telephone1, strlen(h_telephone1), wk_kensaku_tel1);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号１)NG %s\n", "");
                    /*---------------------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_ConvTelNo", (long) rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            /* 検索電話番号１ */
            strcpy(h_kensaku_denwa_no_1, wk_kensaku_tel1);

            /* スペース削除 */
            cmBTfuncB.BT_Rtrim(h_telephone1, strlen(h_telephone1));
            cmBTfuncB.BT_Rtrim(h_kensaku_denwa_no_1, strlen(h_kensaku_denwa_no_1));

            /* 電話番号チェック */
            /* 2015/07/30 電話番号桁数チェック削除 */
            /*if (strlen(h_kensaku_denwa_no_1) < 10) {  */
            /*    memset(h_telephone1, 0x00, sizeof(h_telephone1)); */
            /*    memset(h_kensaku_denwa_no_1, 0x00, sizeof(h_kensaku_denwa_no_1)); */
            /*} */
        }

        if (memcmp(in_nyukai_rendo.telephone2.strVal(), HALF_SPACE13, strlen(HALF_SPACE13)) != 0) {
            /* 電話番号２ */
            /* 2022/12/01 MCCM初版 MOD */
            //strncpy(h_telephone2, in_nyukai_rendo.telephone2, sizeof(h_telephone2) - 1);
            strncpy(h_telephone2, in_nyukai_rendo.telephone2.strVal(), sizeof(in_nyukai_rendo.telephone2.strVal()));
            /* 2022/12/01 MCCM初版 MOD */

            /* 電話番号（ハイフンを削除） */
            rtn_cd = C_ConvTelNo(h_telephone2, strlen(h_telephone2), wk_kensaku_tel2);
            if (rtn_cd != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号２)NG %s\n", "");
                    /*---------------------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_ConvTelNo", (long) rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            /* 検索電話番号２ */
            strcpy(h_kensaku_denwa_no_2, wk_kensaku_tel2);

            /* スペース削除 */
            cmBTfuncB.BT_Rtrim(h_telephone2, strlen(h_telephone2));
            cmBTfuncB.BT_Rtrim(h_kensaku_denwa_no_2, strlen(h_kensaku_denwa_no_2));

            /* 電話番号チェック */
            /* 2015/07/30 電話番号桁数チェック削除 */
            /*if (strlen(h_kensaku_denwa_no_2) < 10) {  */
            /*    memset(h_telephone2, 0x00, sizeof(h_telephone2)); */
            /*    memset(h_kensaku_denwa_no_2, 0x00, sizeof(h_kensaku_denwa_no_2)); */
            /*} */
        }

        /* Eメールアドレス1 */
        //memcpy(h_email1, HALF_SPACE50, sizeof(HALF_SPACE50));
        /* 2023/02/10 MCCM初版 MOD */
        if (memcmp(in_nyukai_rendo.mail_address_pc.strVal(), HALF_SPACE50, strlen(HALF_SPACE50)) != 0) {
            strncpy(h_email1, in_nyukai_rendo.mail_address_pc.strVal(), sizeof(in_nyukai_rendo.mail_address_pc.strVal()));
        }
        /* 2023/02/10 MCCM初版 MOD */

        /* Eメールアドレス2 */
        memcpy(h_email2, HALF_SPACE60, sizeof(HALF_SPACE60));

        /* 2023/02/14 MCCM初版 ADD */
        /* Eメールアドレス3 */
        if (memcmp(in_nyukai_rendo.mail_address_sp.strVal(), HALF_SPACE50, strlen(HALF_SPACE50)) != 0) {
            strncpy(h_email3, in_nyukai_rendo.mail_address_sp.strVal(), sizeof(in_nyukai_rendo.mail_address_sp.strVal()));
        }
        /* 2023/02/14 MCCM初版 ADD */

        /* 電話番号３ */
        memcpy(h_telephone3, HALF_SPACE15, sizeof(HALF_SPACE15));
        /* 電話番号４ */
        memcpy(h_telephone4, HALF_SPACE15, sizeof(HALF_SPACE15));

        /* 検索電話番号３ */
        memcpy(h_kensaku_denwa_no_3, HALF_SPACE15, sizeof(HALF_SPACE15));
        /* 検索電話番号４ */
        memcpy(h_kensaku_denwa_no_4, HALF_SPACE15, sizeof(HALF_SPACE15));

        /* 職業 */
        memcpy(h_job, HALF_SPACE40, sizeof(HALF_SPACE40));

        /* 自宅住所コード */
        memcpy(h_jitaku_jusho_code, HALF_ZERO11, sizeof(HALF_SPACE11));

        /*                                                   */
        /* MM顧客属性情報追加                                */
        /* 顧客情報なし                                      */
        /*                                                   */
        if (hi_msks_sw.intVal() == 0 && hi_mmki_sw.intVal() == 0) {
            rtn_cd = InsertKokyakuzokuseidata();
            if (rtn_cd != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd == C_const_OK) {
                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return C_const_OK;
            }
        } else if (hi_msks_sw.intVal() == 0 || hi_mmki_sw.intVal() == 0) {
            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                        */
        /* MM顧客属性情報追加                                     */
        /* 顧客情報ありの場合                                     */
        /*                                                        */
        /* 初期化 */
//            memset(MmkzData,                0x00, sizeof(MmkzData));
        MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
        memset(upd_h_zip, 0x00, sizeof(upd_h_zip));
        memset(upd_h_zip_code, 0x00, sizeof(upd_h_zip_code));
        memset(upd_h_address1, 0x00, sizeof(upd_h_address1));
        memset(upd_h_address2, 0x00, sizeof(upd_h_address2));
        memset(upd_h_address3, 0x00, sizeof(upd_h_address3));
        memset(upd_h_address, 0x00, sizeof(upd_h_address));                                             /* 2022/10/13 MCCM初版 ADD */
        memset(upd_h_telephone1, 0x00, sizeof(upd_h_telephone1));
        memset(upd_h_telephone2, 0x00, sizeof(upd_h_telephone2));
        memset(upd_h_kensaku_denwa_no_1, 0x00, sizeof(upd_h_kensaku_denwa_no_1));
        memset(upd_h_kensaku_denwa_no_2, 0x00, sizeof(upd_h_kensaku_denwa_no_2));
        /* 2023/02/10 MCCM初版 ADD START */
        memset(upd_h_email1, 0x00, sizeof(upd_h_email1));                                              /* 2023/02/10 MCCM初版 ADD */
        memset(upd_h_email3, 0x00, sizeof(upd_h_email3));                                              /* 2023/02/10 MCCM初版 ADD */
        /* 2023/02/10 MCCM初版 ADD END */

        /* 顧客番号セット */
        strcpy(MmkzData.kokyaku_no, h_uid);
        MmkzData.kokyaku_no.len = strlen(MmkzData.kokyaku_no.strVal());

        /* 顧客属性情報取得 */
        rtn_cd = cmBTfuncB.C_GetCzMaster(MmkzData, rtn_status);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null,
                    "C_GetCzMaster", (long) rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /*--------------------------*/
        /* ＨＯＳＴ変数セット(元値) */
        /*--------------------------*/
        memcpy(upd_h_zip, MmkzData.yubin_no.strVal(), sizeof(MmkzData.yubin_no.strVal()));
        memcpy(upd_h_zip_code, MmkzData.yubin_no_cd.strVal(), sizeof(MmkzData.yubin_no_cd.strVal()));
        memcpy(upd_h_address1, MmkzData.jusho_1.strVal(), sizeof(MmkzData.jusho_1.strVal()));
        memcpy(upd_h_address2, MmkzData.jusho_2.strVal(), sizeof(MmkzData.jusho_2.strVal()));
        memcpy(upd_h_address3, MmkzData.jusho_3.strVal(), sizeof(MmkzData.jusho_3.strVal()));
        memcpy(upd_h_telephone1, MmkzData.denwa_no_1.strVal(), sizeof(MmkzData.denwa_no_1.strVal()));
        memcpy(upd_h_telephone2, MmkzData.denwa_no_2.strVal(), sizeof(MmkzData.denwa_no_2.strVal()));
        memcpy(upd_h_kensaku_denwa_no_1, MmkzData.kensaku_denwa_no_1.strVal(), sizeof(MmkzData.kensaku_denwa_no_1.strVal()));
        memcpy(upd_h_kensaku_denwa_no_2, MmkzData.kensaku_denwa_no_2.strVal(), sizeof(MmkzData.kensaku_denwa_no_2.strVal()));
        /* 2023/02/10 MCCM初版 ADD START */
        memcpy(upd_h_email1, MmkzData.email_address_1.strVal(), sizeof(MmkzData.email_address_1.strVal()));                            /* 2023/02/10 MCCM初版 ADD */
        memcpy(upd_h_email3, MmkzData.email_address_3.strVal(), sizeof(MmkzData.email_address_3.strVal()));                            /* 2023/02/10 MCCM初版 ADD */
        /* 2023/02/10 MCCM初版 ADD END */

        /* スペース削除 */
        cmBTfuncB.BT_Rtrim(upd_h_zip, strlen(upd_h_zip));
        cmBTfuncB.BT_Rtrim(upd_h_address1, strlen(upd_h_address1));
        cmBTfuncB.BT_Rtrim(upd_h_address2, strlen(upd_h_address2));
        cmBTfuncB.BT_Rtrim(upd_h_address3, strlen(upd_h_address3));

        /*----------------------------------*/
        /*---項目共通の更新条件設定---------*/
        /*----------------------------------*/
    /* ☆:[MS顧客制度情報.ＥＣ会員フラグ]=1(入会済み) かつ
        入力データの[会員氏名(漢字)[[会員氏名(カナ)][郵便番号][住所１][住所２]
        のいずれかの項目が未設定 */
        if (MsksData.ec_kaiin_flg.intVal() == 1 &&
                (0 == strlen(h_kaiin_name) || 0 == strlen(h_kana_name) || 0 == strlen(h_zip)
                        || 0 == strlen(h_address1) || 0 == strlen(h_address2))) {
            wi_upditem_sw = 0;
        } else {
            wi_upditem_sw = 1;
        }

        /*----------------------*/
        /* ＨＯＳＴ変数再セット */
        /*----------------------*/
        /* 郵便番号 */
        if (0 == strlen(h_zip) || wi_upditem_sw == 0) {
        } else {
            memcpy(upd_h_zip, h_zip, sizeof(h_zip));
        }
        /* 2015/07/30 住所設定条件変更                               */
        /*            住所1設定ありの場合住所1,2,3設定               */
        /*            →住所1,2,3いずれか設定有りの場合住所1,2,3設定 */
        /* 住所1　住所2　住所3 */
        if ((0 == strlen(h_address1) && 0 == strlen(h_address2) && 0 == strlen(h_address3))
                || wi_upditem_sw == 0) {
        } else {
            memcpy(upd_h_address1, h_address1, sizeof(h_address1));
            memcpy(upd_h_address2, h_address2, sizeof(h_address2));
            memcpy(upd_h_address3, h_address3, sizeof(h_address3));
            /* 住所 */
            sprintf(upd_h_address, "%s%s%s", upd_h_address1, upd_h_address2, upd_h_address3);                      /* 2022/10/13 MCCM初版 ADD */
        }
        /* 郵便番号コード */
        if (wi_upditem_sw == 0) {
        } else {
            if (0 != strlen(upd_h_zip) || 0 != strlen(upd_h_address1) || 0 != strlen(upd_h_address2)
                    || 0 != strlen(upd_h_address3)) {
                /* 住所データありの場合、郵便番号コード取得 */
                rtn_cd = C_GetPostBarCode(upd_h_address,                                                           /* 2022/10/13 MCCM初版 MOD */
                        upd_h_zip, custbarcode, rtn_status);
                if (rtn_cd != C_const_OK) {
                    if (DBG_LOG) {
                        /*----------------------------------------------------------------*/
                        C_DbgMsg("*** UpdateKokyakuzokusei *** 郵便番号コード取得(追加)NG %s\n", "");
                        /*----------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_GetPostBarCode",
                            (long) rtn_cd, rtn_status,
                            0, 0, 0);

                    return C_const_NG;
                }
                strncpy(upd_h_zip_code, custbarcode.arr, sizeof(upd_h_zip_code) - 1);
            }
        }
        /* 電話番号１,検索電話番号１ */
        if (0 == strlen(h_telephone1)) {
        } else {
            /* 電話番号１ */
            strncpy(upd_h_telephone1, h_telephone1.arr, sizeof(h_telephone1) - 1);
            /* 検索電話番号１ */
            strcpy(upd_h_kensaku_denwa_no_1, h_kensaku_denwa_no_1);
        }

        /* 電話番号２,検索電話番号２ */
        if (0 == strlen(h_telephone2)) {
        } else {
            /* 電話番号２ */
            strncpy(upd_h_telephone2, h_telephone2.arr, sizeof(h_telephone2) - 1);
            /* 検索電話番号２ */
            strcpy(upd_h_kensaku_denwa_no_2, h_kensaku_denwa_no_2);
        }
        /* 2023/02/10 MCCM初版 ADD START*/
        /* Ｅメールアドレス１ */                                                                                   /* 2023/02/10 MCCM初版 ADD */
        if (0 == strlen(h_email1)) {
        } else {
            /* Ｅメールアドレス１ */
            strncpy(upd_h_email1, h_email1.arr, sizeof(h_email1) - 1);
        }

        /* Ｅメールアドレス３ */                                                                                   /* 2023/02/10 MCCM初版 ADD */
        if (0 == strlen(h_email3)) {
        } else {
            /* Ｅメールアドレス３ */
            strncpy(upd_h_email3, h_email3.arr, sizeof(h_email3) - 1);
        }
        /* 2023/02/10 MCCM初版 ADD END*/
        /*                                       */
        /* MS顧客属性情報更新                    */
        /* 顧客情報ありの場合                    */
        /*                                       */
        rtn_cd = UpdateKokyakuzokuseidata();
        if (rtn_cd != C_const_OK) {
            /* 処理を終了する */
            return C_const_NG;
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客属性情報更新処理", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakuzokuseidata                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakuzokuseidata()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客属性情報追加                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              -2  ： 重複エラー                                             */
    /*                                                                            */
    /* *****************************************************************************/
    public int InsertKokyakuzokuseidata() {
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */                                         /* 2022/10/13 MCCM初版 ADD */
        IntegerDto precde = new IntegerDto();                 /* 都道府県コード                   */                                         /* 2022/10/13 MCCM初版 ADD */
        int rtn_cd = 0;                 /* 関数戻り値                       */                                         /* 2022/10/13 MCCM初版 ADD */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報追加");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
//            memset(MmkzData, 0x00, sizeof(MmkzData));
        MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
        /* 顧客番号       */
        memcpy(MmkzData.kokyaku_no, h_uid.arr, sizeof(h_uid) - 1);
        MmkzData.kokyaku_no.len = strlen(h_uid);

        /* 郵便番号 */
        if (strlen(h_zip) == 0) {
            memcpy(h_zip, " ", sizeof(h_zip) - 1);
        }
        /* 郵便番号コード */
        if (strlen(h_zip_code) == 0) {
            memcpy(h_zip_code, " ", sizeof(h_zip_code) - 1);
        }
        /* 住所１ */
        if (strlen(h_address1) == 0) {
            memcpy(h_address1, " ", sizeof(h_address1) - 1);
        }
        /* 住所２ */
        if (strlen(h_address2) == 0) {
            memcpy(h_address2, " ", sizeof(h_address2) - 1);
        }
        /* 住所３ */
        if (strlen(h_address3) == 0) {
            memcpy(h_address3, " ", sizeof(h_address3) - 1);
        }
        /* 住所 */                                                                                                 /* 2022/10/13 MCCM初版 ADD */
        sprintf(h_address, "%s%s%s", h_address1, h_address2, h_address3);                                      /* 2022/10/13 MCCM初版 ADD */
        /* 電話番号１ */
        if (strlen(h_telephone1) == 0) {
            memcpy(h_telephone1, " ", sizeof(h_telephone1) - 1);
        }
        /* 電話番号２ */
        if (strlen(h_telephone2) == 0) {
            memcpy(h_telephone2, " ", sizeof(h_telephone2) - 1);
        }
        /* 検索電話番号１ */
        if (strlen(h_kensaku_denwa_no_1) == 0) {
            memcpy(h_kensaku_denwa_no_1, " ", sizeof(h_kensaku_denwa_no_1) - 1);
        }
        /* 検索電話番号２ */
        if (strlen(h_kensaku_denwa_no_2) == 0) {
            memcpy(h_kensaku_denwa_no_2, " ", sizeof(h_kensaku_denwa_no_2) - 1);
        }
        /* 2023/02/10 MCCM初版 ADD START*/
        /*  Eメールアドレス1 */                                                                                    /* 2023/02/10 MCCM初版 ADD */
        if (strlen(h_email1) == 0) {
            memcpy(h_email1, " ", sizeof(h_email1) - 1);
        }
        /*  Eメールアドレス3 */                                                                                    /* 2023/02/10 MCCM初版 ADD */
        if (strlen(h_email3) == 0) {
            memcpy(h_email3, " ", sizeof(h_email3) - 1);
        }
        /* 2023/02/10 MCCM初版 ADD END*/

        /* 2022/10/13 MCCM初版 ADD */
        /* 都道府県コード */
        rtn_cd = C_GetPrefecturesCode(h_address.arr,
                precde, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** InsertKokyakuzokuseidata *** 都道府県コード取得NG %s\n", "");
                /*----------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetPrefecturesCode",
                    (long) rtn_cd, rtn_status,
                    0, 0, 0);
            return C_const_NG;
        }
        h_todofuken_cd.arr = precde.arr;
        /* 2022/10/13 MCCM初版 END */

        /* INSERTする */
//            EXEC SQL INSERT INTO MM顧客属性情報
//                (  顧客番号,
//                        休眠フラグ,
//                        郵便番号,
//                        郵便番号コード,
//                        /*                      住所１,                                                                              *//* 2022/10/13 MCCM初版 DEL */
//                        /*                      住所２,                                                                              *//* 2022/10/13 MCCM初版 DEL */
//                        /*                      住所３,                                                                              *//* 2022/10/13 MCCM初版 DEL */
//                        住所,                                                                                  /* 2022/10/13 MCCM初版 ADD */
//                        電話番号１,
//                        電話番号２,
//                        検索電話番号１,
//                        検索電話番号２,
//                        Ｅメールアドレス１,
//                        Ｅメールアドレス２,
//                        Ｅメールアドレス３,                                                                    /* 2023/02/14 MCCM初版 ADD */
//                        電話番号３,
//                        電話番号４,
//                        検索電話番号３,
//                        検索電話番号４,
//                        職業,
//                        勤務区分,
//                        自宅住所コード,
//                        作業企業コード,
//                        作業者ＩＤ,
//                        作業年月日,
//                        作業時刻,
//                        バッチ更新日,
//                        最終更新日,
//                        最終更新日時,
//                        最終更新プログラムＩＤ,
//                        都道府県コード)                                                                        /* 2022/10/13 MCCM初版 ADD */
//            VALUES (  :MmkzData.kokyaku_no,
//                0,
//                        :h_zip,
//                        :h_zip_code,
//            /*                      :h_address1,                                                                         *//* 2022/10/13 MCCM初版 DEL */
//            /*                      :h_address2,                                                                         *//* 2022/10/13 MCCM初版 DEL */
//            /*                      :h_address3,                                                                         *//* 2022/10/13 MCCM初版 DEL */
//                        :h_address,                                                                            /* 2022/10/13 MCCM初版 ADD */
//                        :h_telephone1,
//                        :h_telephone2,
//                        :h_kensaku_denwa_no_1,
//                        :h_kensaku_denwa_no_2,
//                        :h_email1,
//                        :h_email2,
//                        :h_email3,                                                                            /* 2023/02/14 MCCM初版 ADD */
//                        :h_telephone3,
//                        :h_telephone4,
//                        :h_kensaku_denwa_no_3,
//                        :h_kensaku_denwa_no_4,
//                        :h_job,
//                0,
//                        :h_jitaku_jusho_code,
//                0,
//                0,
//                0,
//                0,
//                        :h_bat_yyyymmdd,
//                        :h_bat_yyyymmdd,
//                sysdate,
//                        :h_saishu_koshin_programid,
//                        :h_todofuken_cd);                                                                      /* 2022/10/13 MCCM初版 ADD */

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = "INSERT INTO MM顧客属性情報 (  顧客番号, 休眠フラグ, 郵便番号, 郵便番号コード, 住所," +
                        " 電話番号１, 電話番号２, 検索電話番号１, 検索電話番号２, Ｅメールアドレス１, Ｅメールアドレス２, Ｅメールアドレス３, 電話番号３," +
                        " 電話番号４, 検索電話番号３, 検索電話番号４, 職業, 勤務区分, 自宅住所コード, 作業企業コード, 作業者ＩＤ, 作業年月日, 作業時刻," +
                        " バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ, 都道府県コード)\n" +
                        "VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 0, 0, 0, 0, ?, ?, sysdate(), ?, ?)";
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(MmkzData.kokyaku_no, h_zip, h_zip_code, h_address, h_telephone1, h_telephone2,
                h_kensaku_denwa_no_1, h_kensaku_denwa_no_2, h_email1, h_email2, h_email3, h_telephone3,
                h_telephone4, h_kensaku_denwa_no_3, h_kensaku_denwa_no_4, h_job, h_jitaku_jusho_code, h_bat_yyyymmdd,
                h_bat_yyyymmdd, h_saishu_koshin_programid, h_todofuken_cd);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkzData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", (long) sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客属性情報追加", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokuseidata                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokuseidata()                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客属性情報更新                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKokyakuzokuseidata() {
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */                                         /* 2022/10/13 MCCM初版 ADD */
        IntegerDto upd_precde = new IntegerDto();             /* 都道府県コード                   */                                         /* 2022/10/13 MCCM初版 ADD */
        int rtn_cd;                 /* 関数戻り値                       */                                         /* 2022/10/13 MCCM初版 ADD */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報更新");
            C_DbgMsg("郵便番号[%s]\n", upd_h_zip);
            C_DbgMsg("郵便番号コード[%s]\n", upd_h_zip_code);
            C_DbgMsg("住所１[%s]\n", upd_h_address1);
            C_DbgMsg("住所２[%s]\n", upd_h_address2);
            C_DbgMsg("住所３[%s]\n", upd_h_address3);
            C_DbgMsg("住所[%s]\n", upd_h_address);                                                        /* 2022/10/13 MCCM初版 ADD */
            C_DbgMsg("電話番号１[%s]\n", upd_h_telephone1);
            C_DbgMsg("電話番号２[%s]\n", upd_h_telephone2);
            C_DbgMsg("検索電話番号１[%s]\n", upd_h_kensaku_denwa_no_1);
            C_DbgMsg("検索電話番号２[%s]\n", upd_h_kensaku_denwa_no_2);
            /* 2023/02/10 MCCM初版 ADD */
            C_DbgMsg("Ｅメールアドレス１[%s]\n", upd_h_email1);                                                       /* 2023/02/10 MCCM初版 ADD */
            C_DbgMsg("Ｅメールアドレス３[%s]\n", upd_h_email3);                                                       /* 2023/02/10 MCCM初版 ADD */
            /* 2023/02/10 MCCM初版 ADD */
            /*---------------------------------------------------------------------*/
        }

        /* 2022/10/13 MCCM初版 ADD START */
        /* 都道府県コード */
        rtn_cd = C_GetPrefecturesCode(upd_h_address.arr,
                upd_precde, rtn_status);
        if (rtn_cd != C_const_OK) {
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** InsertKokyakuzokuseidata *** 都道府県コード取得NG %s\n", "");
                /*----------------------------------------------------------------*/
            }
            APLOG_WT("903", 0, null, "C_GetPrefecturesCode",
                    rtn_cd, rtn_status,
                    0, 0, 0);
            return C_const_NG;
        }
        upd_h_todofuken_cd.arr = upd_precde.arr;
        /* 2022/10/13 MCCM初版 ADD END */
        /* MM顧客属性情報更新処理 */
//            EXEC SQL UPDATE MM顧客属性情報
//            SET 郵便番号               =  :upd_h_zip,
//                郵便番号コード         =  :upd_h_zip_code,
//                /*          住所１                 =  :upd_h_address1,                                                       *//* 2022/10/13 MCCM初版 DEL */
//                /*          住所２                 =  :upd_h_address2,                                                       *//* 2022/10/13 MCCM初版 DEL */
//                /*          住所３                 =  :upd_h_address3,                                                       *//* 2022/10/13 MCCM初版 DEL */
//                住所                   =  :upd_h_address,                                                          /* 2022/10/13 MCCM初版 ADD */
//                電話番号１             =  :upd_h_telephone1,
//                電話番号２             =  :upd_h_telephone2,
//                検索電話番号１         =  :upd_h_kensaku_denwa_no_1,
//                検索電話番号２         =  :upd_h_kensaku_denwa_no_2,
//                /* 2023/02/10 MCCM初版 ADD */
//                Ｅメールアドレス１     =  :upd_h_email1,                                                            /* 2023/02/10 MCCM初版 ADD */
//                Ｅメールアドレス３     =  :upd_h_email3,                                                            /* 2023/02/10 MCCM初版 ADD */
//                /* 2023/02/10 MCCM初版 ADD */
//                バッチ更新日           =  :h_bat_yyyymmdd,
//                最終更新日             =  :h_bat_yyyymmdd,
//                最終更新日時           =  sysdate,
//                最終更新プログラムＩＤ = :h_saishu_koshin_programid,
//                都道府県コード         = :upd_h_todofuken_cd                                                       /* 2022/10/13 MCCM初版 ADD */
//            WHERE 顧客番号       = :h_uid;


        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客属性情報 " +
                "SET 郵便番号 = ?, " +
                "郵便番号コード = ?, " +
                "住所 = ?, " +
                "電話番号１ = ?, " +
                "電話番号２ = ?, " +
                "検索電話番号１ = ?, " +
                "検索電話番号２ = ?, " +
                "Ｅメールアドレス１ = ?, " +
                "Ｅメールアドレス３ = ?, " +
                "バッチ更新日 = ?, " +
                "最終更新日 = ?, " +
                "最終更新日時 = sysdate(), " +
                "最終更新プログラムＩＤ = ?, " +
                "都道府県コード = ?" +
                "WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(upd_h_zip, upd_h_zip_code, upd_h_address, upd_h_telephone1, upd_h_telephone2, upd_h_kensaku_denwa_no_1,
                upd_h_kensaku_denwa_no_2, upd_h_email1, upd_h_email3, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid, upd_h_todofuken_cd, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客属性情報更新", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKigyobetuzokusei                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKigyobetuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo,     */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               １０．内部関数（顧客企業別属性情報更新）                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKigyobetuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {
        int rtn_cd;                /* 関数戻り値                       */
//  int        rtn_status;            /* 関数ステータス                   */                                   /* 2022/10/13 MCCM初版 DEL */
        StringDto wk_dm_tome_kbn = new StringDto(1 + 1);
        StringDto wk_email_tome_kbn = new StringDto(4 + 1);

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客企業別属性情報更新");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd = C_const_OK;
//  rtn_status = C_const_Stat_OK;                                                                              /* 2022/10/13 MCCM初版 DEL */
        h_dm_tome_kbn.arr = 0;
        h_email_tome_kbn.arr = 0;

        /*------------------------------------*/
        /*---追加／更新用のホスト変数を編集---*/
        /*------------------------------------*/
        /* 入会年月日 */
        if (MmkgData.nyukai_ymd.intVal() == 0
                || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal()) { /* 基準日を設定 */
            MmkgData.nyukai_ymd.arr = h_kijyun_yyyymmdd.arr;
        }
        /* ＤＭ止め区分 */
        if (memcmp(in_nyukai_rendo.dm_kbn.strVal(), HALF_SPACE1, strlen(HALF_SPACE1)) != 0) {
            strncpy(wk_dm_tome_kbn, in_nyukai_rendo.dm_kbn.strVal(), sizeof(wk_dm_tome_kbn) - 1);
            h_dm_tome_kbn.arr = atoi(wk_dm_tome_kbn);
            if (h_dm_tome_kbn.intVal() == 0) {
                /* 20180619 ＤＭ止め区分更新条件変更対応 start */
                /* MmkgData.dm_tome_kbn = C_SEND_OK_3000; */

                if (!(MmkgData.dm_tome_kbn.intVal() == 3001
                        || MmkgData.dm_tome_kbn.intVal() == 3007
                        || MmkgData.dm_tome_kbn.intVal() == 3031
                        || MmkgData.dm_tome_kbn.intVal() == 3093)) {
                    MmkgData.dm_tome_kbn.arr = C_SEND_OK_3000;
                }

                /* 20180619 ＤＭ止め区分更新条件変更対応 end */

            } else {
                MmkgData.dm_tome_kbn.arr = C_SEND_NG_3031;
            }
        } else {
            /* 顧客企業別属性情報の新規追加の場合 */
            if (hi_mmkg_sw.intVal() == 0) {
                MmkgData.dm_tome_kbn.arr = C_SEND_NG_3099;
            }
        }
        /* Ｅメール止め区分 */
        if (memcmp(in_nyukai_rendo.merumaga_kbn.strVal(), HALF_SPACE4, strlen(HALF_SPACE4)) != 0) {
            strncpy(wk_email_tome_kbn, in_nyukai_rendo.merumaga_kbn.strVal(), sizeof(wk_email_tome_kbn) - 1);
            h_email_tome_kbn.arr = atoi(wk_email_tome_kbn);
            if (h_email_tome_kbn.intVal() == 0) {
                /* 20180619 ＤＭ止め区分更新条件変更対応 start */
                /* MmkgData.email_tome_kbn = C_SEND_OK_5000; */

                if (!(MmkgData.email_tome_kbn.intVal() == 5001
                        || MmkgData.email_tome_kbn.intVal() == 5007
                        || MmkgData.email_tome_kbn.intVal() == 5031
                        || MmkgData.email_tome_kbn.intVal() == 5093
                        || MmkgData.email_tome_kbn.intVal() == 5099)) {
                    MmkgData.email_tome_kbn.arr = C_SEND_OK_5000;
                }
                /* 20180619 ＤＭ止め区分更新条件変更対応 end */

            } else {
                MmkgData.email_tome_kbn.arr = C_SEND_NG_5031;
            }
        } else {
            /* 顧客企業別属性情報の新規追加の場合 */
            if (hi_mmkg_sw.intVal() == 0) {
                MmkgData.email_tome_kbn.arr = C_SEND_NG_5099;
            }
        }

        /* 顧客番号セット */
        memcpy(MmkgData.kokyaku_no, h_uid.arr, sizeof(h_uid) - 1);
        MmkgData.kokyaku_no.len = strlen(h_uid);

        /*------------------------------------------------------*/
        /*---顧客企業別属性情報存在を判定し、追加／更新を実行---*/
        /*------------------------------------------------------*/
        if (hi_mmkg_sw.intVal() == 0) {
            rtn_cd = InsertKigyobetuzokuseidata();
            if (rtn_cd != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }
        } else {
            rtn_cd = UpdateKigyobetuzokuseidata();
            if (rtn_cd != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客企業別属性情報更新", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*-----UpdateKigyobetuzokusei Bottom------------------------------------------*/
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKigyobetuzokuseidata                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKigyobetuzokuseidata()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報追加                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常(重複エラー含む)                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int InsertKigyobetuzokuseidata() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報追加");
            /*---------------------------------------------------------------------*/
        }
        /* INSERTする */
//            EXEC SQL INSERT INTO MM顧客企業別属性情報
//                (  顧客番号,
//                        企業コード,
//                        入会年月日,
//                        退会年月日,
//                        ＴＥＬ止め区分,
//                        ＤＭ止め区分,
//                        Ｅメール止め区分,
//                        携帯ＴＥＬ止め区分,
//                        携帯Ｅメール止め区分,
//                        作業企業コード,
//                        作業者ＩＤ,
//                        作業年月日,
//                        作業時刻,
//                        バッチ更新日,
//                        最終更新日,
//                        最終更新日時,
//                        最終更新プログラムＩＤ)
//            VALUES (  :MmkgData.kokyaku_no,
//                          :h_ps_corpid,
//                          :MmkgData.nyukai_ymd,
//                0,
//                0,
//                          :MmkgData.dm_tome_kbn,
//                          :MmkgData.email_tome_kbn,
//                0,
//                0,
//                0,
//                0,
//                0,
//                0,
//                          :h_bat_yyyymmdd,
//                          :h_bat_yyyymmdd,
//                sysdate,
//                          :h_saishu_koshin_programid);

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MM顧客企業別属性情報 ( 顧客番号, 企業コード, 入会年月日, 退会年月日, ＴＥＬ止め区分, " +
                        "ＤＭ止め区分, Ｅメール止め区分, 携帯ＴＥＬ止め区分, 携帯Ｅメール止め区分, 作業企業コード, 作業者ＩＤ, 作業年月日, 作業時刻," +
                        " バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) \n" +
                        "VALUES('%s', '%s', '%s', 0, 0, '%s', '%s', 0, 0, 0, 0, 0, 0, '%s', '%s', sysdate(), '%s')"
                , MmkgData.kokyaku_no, h_ps_corpid, MmkgData.nyukai_ymd, MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn,
                h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid);
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute();

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkgData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", (long) sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客企業別属性情報追加", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;

        /*-----InsertKigyobetuzokuseidata Bottom--------------------------------------*/
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKigyobetuzokuseidata                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKigyobetuzokuseidata()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報更新                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKigyobetuzokuseidata() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報更新");
            /*---------------------------------------------------------------------*/
        }

        /* MM顧客企業別属性情報更新 */
//            EXEC SQL UPDATE MM顧客企業別属性情報
//            SET ＤＭ止め区分           = DECODE(ＤＭ止め区分, 3092, ＤＭ止め区分, :MmkgData.dm_tome_kbn),
//            Ｅメール止め区分       = DECODE(Ｅメール止め区分, 5092, Ｅメール止め区分, :MmkgData.email_tome_kbn),
//            バッチ更新日           = :h_bat_yyyymmdd,
//                最終更新日             = :h_bat_yyyymmdd,
//                最終更新日時           = SYSDATE,
//                最終更新プログラムＩＤ = :h_saishu_koshin_programid
//            WHERE 企業コード = :MmkgData.kigyo_cd
//            AND 顧客番号   = :h_uid;


        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客企業別属性情報 " +
                "SET ＤＭ止め区分 = DECODE(ＤＭ止め区分, '3092', ＤＭ止め区分, ?), " +
                "Ｅメール止め区分 = DECODE(Ｅメール止め区分, '5092', Ｅメール止め区分, ?), " +
                "バッチ更新日 = ?, " +
                "最終更新日 = ?, " +
                "最終更新日時 = SYSDATE(), " +
                "最終更新プログラムＩＤ = ? " +
                "WHERE 企業コード = ? AND 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid, MmkgData.kigyo_cd, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客企業別属性情報更新", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;

        /*-----UpdateKigyobetuzokuseidata Bottom--------------------------------------*/
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateRiyokanoPoint                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateRiyokanoPoint(NYUKAI_RENDO_DATA in_nyukai_rendo,        */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               内部関数（利用可能ポイント情報更新）                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateRiyokanoPoint(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {
//  int        rtn_cd;                /* 関数戻り値                       */                                   /* 2022/10/13 MCCM初版 DEL */
//  int        rtn_status;            /* 関数ステータス                   */                                   /* 2022/10/13 MCCM初版 DEL */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("利用可能ポイント情報更新");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
//  rtn_cd     = C_const_OK;                                                                                   /* 2022/10/13 MCCM初版 DEL */
//  rtn_status = C_const_Stat_OK;                                                                              /* 2022/10/13 MCCM初版 DEL */

        /*------------------------------*/
        /*---更新用のホスト変数を編集---*/
        /*------------------------------*/
        /* 入会企業コード */
        if (h_nyukaiten_no.intVal() != 0) {
            h_nyukai_kigyou_cd = h_ps_corpid;
        }
        /* 入会旧販社コード */
        if (h_nyukaiten_no.intVal() != 0) {
            h_nyukai_oldcorp_cd = h_kyu_hansya_cd;
        }
        /* 入会店舗       */
        if (h_nyukaiten_no.intVal() != 0) {
            h_nyukai_tenpo = h_nyukaiten_no;
        }
        /* 発券企業コード */
        if (h_nyukaiten_no.intVal() != 0) {
            h_haken_kigyou_cd.arr = h_ps_corpid.arr;
        }
        /* 発券店舗       */
        if (h_nyukaiten_no.intVal() != 0) {
            h_haken_tenpo = h_nyukaiten_no;
        }
        /* 入会会社コードＭＣＣ */
        if (h_nyukaiten_no.intVal() != 0) {
            h_nyukai_kaisha_cd_mcc.arr = 2500;
        }
        /* 入会店舗ＭＣＣ*/
        if (h_nyukaiten_no.intVal() != 0) {
            h_nyukai_tenpo_mcc = h_renkei_mise_no;
        }


        /*------------------------------------*/
        /*---TS利用可能ポイント情報更新処理---*/
        /*------------------------------------*/
        /* TS利用可能ポイント情報更新 */
//            EXEC SQL UPDATE TS利用可能ポイント情報@CMSD
//            SET 入会企業コード         = :h_nyukai_kigyou_cd,
//                /*                  入会店舗               = :h_nyukai_tenpo,                                                *//* 2022/10/13 MCCM初版 DEL */
//                入会旧販社コード       = :h_nyukai_oldcorp_cd,
//                発券企業コード         = :h_haken_kigyou_cd,
//                発券店舗               = :h_haken_tenpo,
//                最終更新日             = :h_bat_yyyymmdd,
//                最終更新日時           = SYSDATE,
//                最終更新プログラムＩＤ = :h_saishu_koshin_programid,
//                入会会社コードＭＣＣ   = :h_nyukai_kaisha_cd_mcc,                                          /* 2022/10/13 MCCM初版 ADD */
//                入会店舗ＭＣＣ         = :h_nyukai_tenpo_mcc                                               /* 2022/10/13 MCCM初版 ADD */
//            WHERE 顧客番号 = :h_uid_varchar;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE TS利用可能ポイント情報 " +
                "SET 入会企業コード = ?,  " +
                "入会旧販社コード = ?, " +
                "発券企業コード = ?," +
                "発券店舗 = ?, " +
                "最終更新日 = ?, " +
                "最終更新日時 = SYSDATE(), " +
                "最終更新プログラムＩＤ = ?, " +
                "入会会社コードＭＣＣ = ?, " +
                "入会店舗ＭＣＣ = ? " +
                "WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_nyukai_kigyou_cd, h_nyukai_oldcorp_cd, h_haken_kigyou_cd, h_haken_tenpo, h_bat_yyyymmdd, h_saishu_koshin_programid, h_nyukai_kaisha_cd_mcc,
                h_nyukai_tenpo_mcc, h_uid_varchar);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                    "TS利用可能ポイント情報更新", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("利用可能ポイント情報更新", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*-----UpdateRiyokanoPoint Bottom------------------------------------------*/
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuNyukaiTen                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuNyukaiTen(NYUKAI_RENDO_DATA in_nyukai_rendo,     */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新（入会店）処理                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKokyakuNyukaiTen(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {
//  int     rtn_cd;             /* 関数戻り値                  */                                              /* 2022/10/13 MCCM初版 DEL */
//  int     rtn_status;         /* 関数ステータス              */                                              /* 2022/10/13 MCCM初版 DEL */
        StringDto wk_sql_buf = new StringDto();    /* ＳＱＬ文編集用 */
        StringDto wk_sql_item = new StringDto();                 /* 動的SQLバッファ */

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新（入会店）処理");
        }
        /* 初期化 */
//  rtn_cd = C_const_OK;                                                                                       /* 2022/10/13 MCCM初版 DEL */
//  rtn_status = C_const_Stat_OK;                                                                              /* 2022/10/13 MCCM初版 DEL */

        /*----------------------------------*/
        /*---MM顧客情報---------------------*/
        /*----------------------------------*/

        if (DBG_LOG) {
            C_DbgMsg("*** UpdateKokyakuNyukaiTen *** 顧客情報 有無%s\n", "");
            C_DbgMsg("MM顧客情報    「なし(0)／あり(1)」=[%d]\n", hi_mmki_sw);
        }
        if (hi_mmki_sw.intVal() == 0) {
            /* パンチエラー番号セット */
            punch_errno = C_PUNCH_OK;

            /* 処理を終了する */
            return (C_const_OK);
        }

        /*                                                   */
        /* MM顧客情報（入会店）更新                          */
        /* 顧客情報ありの場合                                */
        /*                                                   */
        if (h_nyukaiten_no.intVal() != 0) {
            /* ＳＱＬを生成する */
            strcpy(wk_sql_buf, "UPDATE MM顧客情報  SET ");

            /*入会企業コード */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "入会企業コード = %d,", h_ps_corpid);
            strcat(wk_sql_buf, wk_sql_item);
//      /* 入会店舗 */                                                                                         /* 2022/10/13 MCCM初版 DEL */
//      memset( wk_sql_item, 0x00, sizeof(wk_sql_item));                                                       /* 2022/10/13 MCCM初版 DEL */
//      sprintf(wk_sql_item, "入会店舗 = %d,", h_nyukaiten_no);                                                /* 2022/10/13 MCCM初版 DEL */
//      strcat( wk_sql_buf , wk_sql_item);                                                                     /* 2022/10/13 MCCM初版 DEL */
            /* 発券企業コード */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "発券企業コード = %d,", h_ps_corpid);
            strcat(wk_sql_buf, wk_sql_item);
            /* 発券店舗 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "発券店舗 = %d,", h_nyukaiten_no);
            strcat(wk_sql_buf, wk_sql_item);
            /* バッチ更新日 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "バッチ更新日 = %d,", h_bat_yyyymmdd);
            strcat(wk_sql_buf, wk_sql_item);

            /* 最終更新日・日時・プログラムＩＤ */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item,
                    "最終更新日 = %d," +
                            "最終更新日時 = SYSDATE()," +
                            "最終更新プログラムＩＤ = '%s', "
                    , h_bat_yyyymmdd, h_saishu_koshin_programid);
            strcat(wk_sql_buf, wk_sql_item);

            /* 入会会社コードＭＣＣ */                                                                             /* 2022/10/13 MCCM初版 ADD */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                       /* 2022/10/13 MCCM初版 ADD */
            sprintf(wk_sql_item, "入会会社コードＭＣＣ = %d,", 2500);                                              /* 2022/10/13 MCCM初版 ADD */
            strcat(wk_sql_buf, wk_sql_item);                                                                     /* 2022/10/13 MCCM初版 ADD */

            /* 入会店舗ＭＣＣ */                                                                                   /* 2022/10/13 MCCM初版 ADD */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                       /* 2022/10/13 MCCM初版 ADD */
            sprintf(wk_sql_item, "入会店舗ＭＣＣ = %d", h_renkei_mise_no);                                         /* 2022/10/13 MCCM初版 ADD */
            strcat(wk_sql_buf, wk_sql_item);                                                                     /* 2022/10/13 MCCM初版 ADD */

            /* WHERE句 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, " WHERE 顧客番号 = %s", h_uid);
            strcat(wk_sql_buf, wk_sql_item);

            if (DBG_LOG) {
                C_DbgMsg("***UpdateKokyakuNyukaiTenpo*** MM顧客情報更新 : sqlbuf=[%s]\n", wk_sql_buf);
            }

            /* ＳＱＬ文をセットする */
            memset(str_sql, 0x00, sizeof(str_sql));
            strcpy(str_sql, wk_sql_buf);

//                EXEC SQL PREPARE sql_stat4 from :str_sql;
            sqlca.sql = str_sql;
            sqlca.prepare();
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", (long) sqlca.sqlcode,
                        "MM顧客情報（入会店）", out_format_buf, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }

            /* UPDATE文を実行する */
//                EXEC SQL EXECUTE sql_stat4;
            sqlca.restAndExecute();

            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                        "MM顧客情報（入会店）", out_format_buf, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            C_DbgEnd("顧客情報更新（入会店）処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuNyukaiNengappi                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuNyukaiNengappi(NYUKAI_RENDO_DATA in_nyukai_rendo,*/
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新（入会年月日）処理                               */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateKokyakuNyukaiNengappi(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新（入会年月日）処理");
        }

        if (DBG_LOG) {
            C_DbgMsg("*** UpdateKokyakuNyukaiNengappi *** 顧客情報 有無%s\n", "");
            C_DbgMsg("MM顧客情報    「なし(0)／あり(1)」=[%d]\n", hi_mmki_sw);
        }
        if (hi_mmki_sw.intVal() == 0) {
            /* パンチエラー番号セット */
            punch_errno = C_PUNCH_OK;

            /* 処理を終了する */
            return (C_const_OK);
        }

        /*                                                   */
        /* MM顧客企業別属性情報（入会年月日）更新            */
        /* 顧客情報ありの場合                                */
        /*                                                   */
        /* 入会年月日 */
        h_nyukai_yyyymmdd.arr = MmkgData.nyukai_ymd.intVal();
        if (MmkgData.nyukai_ymd.intVal() == 0
                || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal()) { /* 基準日を設定 */
            h_nyukai_yyyymmdd = h_kijyun_yyyymmdd;
        }

//            EXEC SQL UPDATE MM顧客企業別属性情報
//            SET 入会年月日             = :h_nyukai_yyyymmdd,
//                バッチ更新日           = :h_bat_yyyymmdd,
//                最終更新日             = :h_bat_yyyymmdd,
//                最終更新日時           = SYSDATE,
//                最終更新プログラムＩＤ = :h_saishu_koshin_programid
//            WHERE 企業コード = :MmkgData.kigyo_cd
//            AND 顧客番号   = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客企業別属性情報 " +
                "SET 入会年月日 = ?,  " +
                "バッチ更新日 = ?,  " +
                "最終更新日 = ?,  " +
                "最終更新日時 = SYSDATE(),  " +
                "最終更新プログラムＩＤ = ? " +
                "WHERE 企業コード = ? AND 顧客番号   = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_nyukai_yyyymmdd, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid, MmkgData.kigyo_cd, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "入会年月日更新失敗 顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_700, 0, "0x00", out_format_buf, 0, 0, 0, 0, 0);

            /* 警告メッセージを出力し処理を継続 */
            return (C_const_OK);
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            C_DbgEnd("顧客情報更新（入会年月日）処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateZaisekiKaishiNengetsu                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateZaisekiKaishiNengetsu(NYUKAI_RENDO_DATA in_nyukai_rendo,*/
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新（在籍開始年月）処理                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      NYUKAI_RENDO_DATA     in_nyukai_rendo : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int UpdateZaisekiKaishiNengetsu(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新（在籍開始年月）処理");
        }

        if (DBG_LOG) {
            C_DbgMsg("*** UpdateZaisekiKaishiNengetsu *** 顧客情報 有無%s\n", "");
            C_DbgMsg("MS顧客制度情報    「なし(0)／あり(1)」=[%d]\n", hi_msks_sw);
        }
        if (hi_msks_sw.intVal() == 0) {
            /* パンチエラー番号セット */
            punch_errno = C_PUNCH_OK;

            /* 処理を終了する */
            return (C_const_OK);
        }

        /*                                                   */
        /* MS顧客制度情報（在籍開始年月）更新                */
        /* 顧客制度情報ありの場合                                */
        /*                                                   */
        /* 在籍開始年月 */
        if (h_zaiseki_yyyymm.intVal() != 0) {
            if (h_kijyun_yyyymm.intVal() < h_zaiseki_yyyymm.intVal()) { /* 基準日を設定 */
                h_zaiseki_yyyymm = h_kijyun_yyyymm;
            }
        } else {
            h_zaiseki_yyyymm = h_kijyun_yyyymm;
        }

//            EXEC SQL UPDATE MS顧客制度情報@CMSD
//            SET 在籍開始年月           = :h_zaiseki_yyyymm,
//                バッチ更新日           = :h_bat_yyyymmdd,
//                最終更新日             = :h_bat_yyyymmdd,
//                最終更新日時           = SYSDATE,
//                最終更新プログラムＩＤ = :h_saishu_koshin_programid
//            WHERE 顧客番号   = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MS顧客制度情報 " +
                "SET 在籍開始年月 = ?, " +
                "バッチ更新日 = ?, " +
                "最終更新日 = ?, " +
                "最終更新日時 = SYSDATE(), " +
                "最終更新プログラムＩＤ = ?" +
                "WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_zaiseki_yyyymm, h_bat_yyyymmdd, h_bat_yyyymmdd, h_saishu_koshin_programid, h_uid);

        /* エラーの場合処理を警告メッセージを出力 */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "在籍開始年月更新失敗 顧客番号=[%s] (%d)", h_uid, (long) sqlca.sqlcode);
            APLOG_WT(DEF_MSG_ID_700, 0, "0x00", out_format_buf, 0, 0, 0, 0, 0);

            /* 警告メッセージを出力し処理を継続 */
            return (C_const_OK);
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            C_DbgEnd("顧客情報更新（在籍開始年月）処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： OutPunchdata                                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  OutPunchdata(int punch_errno)                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               パンチエラーデータ出力処理                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int OutPunchdata(int punch_errno) {
        StringDto wk_storeno = new StringDto(4);         /* 店番号                           */
        StringDto wk_request_date = new StringDto(9);    /* 依頼日                           */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("パンチエラーデータ出力処理");
            /*---------------------------------------------------------------------*/
            C_DbgMsg("処理年月日        =[%d]\n", h_bat_yyyymmdd);
            C_DbgMsg("会員番号          =[%s]\n", h_pid);
            C_DbgMsg("データ区分        =[%d]\n", 1);
            C_DbgMsg("申込書企業コード  =[%d]\n", h_ps_corpid);
            C_DbgMsg("申込書店番号      =[%d]\n", h_moushikomiten_no);
            C_DbgMsg("理由コード        =[%d]\n", punch_errno);
            C_DbgMsg("申込年月日        =[%d]\n", h_kijyun_yyyymmdd);
            C_DbgMsg("バッチ番号        =[%d]\n", 0);
        }

        /* 初期化 */
        tmpe_buff = new TM_PUNCH_DATA_ERROR_INFO_TBL();
//            memset(tmpe_buff, 0x00, sizeof(TM_PUNCH_DATA_ERROR_INFO_TBL));
        memset(wk_storeno, 0x00, sizeof(wk_storeno));
        memset(wk_request_date, 0x00, sizeof(wk_request_date));

        /*--------------------*/
        /* ＨＯＳＴ変数セット */
        /*--------------------*/
        /* 処理年月日       */
        tmpe_buff.shori_ymd = h_bat_yyyymmdd;
        /* 会員番号         */
        memcpy(tmpe_buff.kaiin_no, h_pid, sizeof(h_pid) - 1);
        tmpe_buff.kaiin_no.len = strlen(h_pid);
        /* データ区分       */
        tmpe_buff.data_kbn.arr = 1;
        /* 申込書企業コード */
        tmpe_buff.entry_kigyo_cd.arr = h_ps_corpid.arr;
        /* 申込書店番号     */
        tmpe_buff.entry_mise_no = h_moushikomiten_no;
        /* 理由コード(パンチエラー番号) */
        tmpe_buff.riyu_cd.arr = punch_errno;
        /* 依頼日           */
        tmpe_buff.irai_ymd = h_kijyun_yyyymmdd;
        /* バッチ番号       */
        tmpe_buff.batch_no.arr = 0;
        /* 入会年月日       */
        tmpe_buff.nyukai_ymd = h_kijyun_yyyymmdd;
        /* 会員氏名         */
        memcpy(tmpe_buff.kaiin_mesho, h_kaiin_name.arr, sizeof(h_kaiin_name) - 1);

        /* INSERTする */
//            EXEC SQL INSERT INTO TMパンチデータエラー情報
//                (  処理年月日,
//                        会員番号,
//                        データ区分,
//                        申込書企業コード,
//                        申込書店番号,
//                        理由コード,
//                        依頼日,
//                        バッチ番号,
//                        入会年月日,
//                        会員氏名 )
//            VALUES
//                    (  :tmpe_buff.shori_ymd,
//                        :tmpe_buff.kaiin_no,
//                        :tmpe_buff.data_kbn,
//                        :tmpe_buff.entry_kigyo_cd,
//                        :tmpe_buff.entry_mise_no,
//                        :tmpe_buff.riyu_cd,
//                        :tmpe_buff.irai_ymd,
//                        :tmpe_buff.batch_no,
//                        :tmpe_buff.nyukai_ymd,
//                        :tmpe_buff.kaiin_mesho);


        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO TMパンチデータエラー情報 (  処理年月日, 会員番号, データ区分, 申込書企業コード, 申込書店番号, 理由コード, 依頼日, バッチ番号, 入会年月日, 会員氏名 )\n" +
                        "VALUES(  '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')"
                , tmpe_buff.shori_ymd, tmpe_buff.kaiin_no, tmpe_buff.data_kbn, tmpe_buff.entry_kigyo_cd,
                tmpe_buff.entry_mise_no, tmpe_buff.riyu_cd, tmpe_buff.irai_ymd, tmpe_buff.batch_no,
                tmpe_buff.nyukai_ymd, tmpe_buff.kaiin_mesho);
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute();

        /* 重複エラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* DBERR */
            sprintf(out_format_buf, "会員番号=[%s]", h_pid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", (long) sqlca.sqlcode,
                    "TMパンチデータエラー情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* 重複エラーの場合UPDATEする */
        if (sqlca.sqlcode == C_const_Ora_DUPL) {
            /* TMパンチデータエラー情報更新処理 */
//                EXEC SQL UPDATE TMパンチデータエラー情報
//                SET 申込書企業コード   = :tmpe_buff.entry_kigyo_cd,
//                        申込書店番号       = :tmpe_buff.entry_mise_no,
//                        理由コード         = :tmpe_buff.riyu_cd,
//                        依頼日             = :tmpe_buff.irai_ymd,
//                        バッチ番号         = :tmpe_buff.batch_no,
//                        入会年月日         = :tmpe_buff.nyukai_ymd,
//                        会員氏名           = :tmpe_buff.kaiin_mesho
//                WHERE  処理年月日         = :h_bat_yyyymmdd
//                AND    会員番号           = :h_pid
//                AND    データ区分         = :tmpe_buff.data_kbn;

            StringDto sql = new StringDto();
            sql.arr = "UPDATE TMパンチデータエラー情報 " +
                    "SET 申込書企業コード = ?,  " +
                    "申込書店番号 = ?, " +
                    "理由コード = ?, " +
                    "依頼日 = ?, " +
                    "バッチ番号 = ?, " +
                    "入会年月日 = ?, " +
                    "会員氏名 = ?" +
                    "WHERE 処理年月日 = ? AND    会員番号 = ? AND    データ区分 = ?";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.query(tmpe_buff.entry_kigyo_cd, tmpe_buff.entry_mise_no, tmpe_buff.riyu_cd, tmpe_buff.irai_ymd,
                    tmpe_buff.batch_no, tmpe_buff.nyukai_ymd, tmpe_buff.kaiin_mesho, h_bat_yyyymmdd, h_pid, tmpe_buff.data_kbn);
            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", (long) sqlca.sqlcode,
                        "TMパンチデータエラー情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return C_const_NG;
            }
        }

        /* TMパンチデータエラー情報追加更新件数カウントアップ */
        punch_data_cnt++;
        /* エラーデータ件数カウントアップ */
        ng_data_cnt++;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("パンチエラーデータ出力処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        return C_const_OK;
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTcentB_Chk_Arg                                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTcentB_Cchk_Arg( char *Arg_in )                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*              引数のチェックを行う                                          */
    /*              １）重複チェック                                              */
    /*              ２）桁数チェック                                              */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      String    *    Arg_in      ：引数値                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
    /* *****************************************************************************/
    public int cmBTcentB_Chk_Arg(String Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTcentB_Chk_Arg処理");
            C_DbgMsg("*** cmBTcentB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (0 == memcmp(Arg_in, DEF_ARG_I, 2)) {       /* -i入力ファイルチェック  */
            if (chk_arg_i != DEF_OFF) {
                return C_const_NG;
            }
            chk_arg_i = DEF_ON;

            if (strlen(Arg_in) <= 2) {               /* 桁数チェック            */
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("cmBTcentB_Chk_Arg処理", 0, 0, 0);
            /*---------------------------------------------------------------------*/
        }

        return C_const_OK;
        /*-----cmBTcentB_Chk_Arg Bottom----------------------------------------------*/
    }

    /* *****************************************************************************/
    /*                                                                            */
    /*    関数名 ： APLOG_WT                                                      */
    /*                                                                            */
    /*    書式                                                                    */
    /*    static int  APLOG_WT(char *msgid, int  msgidsbt, char *dbkbn,           */
    /*                            caddr_t param1, caddr_t param2, caddr_t param3, */
    /*                            caddr_t param4, caddr_t param5, caddr_t param6) */
    /*                                                                            */
    /*    【説明】                                                                */
    /*              ＡＰログ出力を行う                                            */
    /*                                                                            */
    /*                                                                            */
    /*    【引数】                                                                */
    /*        String     *msgid     ：メッセージＩＤ                             */
    /*        int         msgidsbt   ：メッセージ登録種別                         */
    /*        String     *dbkbn     ：ＤＢ区分                                   */
    /*        caddr_t     param1     ：置換変数１                                 */
    /*        caddr_t     param2     ：置換変数２                                 */
    /*        caddr_t     param3     ：置換変数３                                 */
    /*        caddr_t     param4     ：置換変数４                                 */
    /*        caddr_t     param5     ：置換変数５                                 */
    /*        caddr_t     param6     ：置換変数６                                 */
    /*                                                                            */
    /*    【戻り値】                                                              */
    /*              0    ： 正常                                                  */
    /*              1    ： 異常                                                  */
    /*                                                                            */
    /* *****************************************************************************/
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn,
                        String param1, int param2, String param3,
                        String param4, int param5, int param6) {
        String[] out_flg = new String[2];                    /* APログフラグ */
        String[] out_format = new String[DEF_BUFSIZE8K];       /* APログフォーマット */
        IntegerDto out_status;                        /* フォーマット取得結果 */
        int rtn_cd;                            /* 関数戻り値 */

        if (DBG_LOG) {
            C_DbgStart("APLOG_WT処理");
        }

        /*#####################################*/
        /*    APログフォーマット取得処理         */
        /*#####################################*/

        memset(out_flg, 0x00, out_flg.length);
        memset(out_format, 0x00, out_format.length);
        out_status = new IntegerDto();
        /*dbg_getaplogfmt = 1;*/
        rtn_cd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        /*dbg_getaplogfmt = 0; */

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtn_cd);
        }

        /*#####################################*/
        /*    APログ出力処理                     */
        /*#####################################*/
        rtn_cd = C_APLogWrite(msgid, out_format, out_flg, param1, param2, param3, param4, param5, param6);

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        }

        return (C_const_OK);
    }

}
