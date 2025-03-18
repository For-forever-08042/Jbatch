package jp.co.mcc.nttdata.batch.business.service.cmBTecenB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTecenB.dto.EC_RENDO_DATA;
import jp.co.mcc.nttdata.batch.business.service.cmBTkpskB.CmBTkpskBService;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService.*;

/**********************************************************************************
 *   プログラム名   ： ＥＣ会員情報取込（cmBTecenB）
 *
 *   【処理概要】
 *       ＥＣ会員情報ファイルより、カード情報、顧客、顧客属性、顧客制度情報への
 *       顧客情報登録を行う。
 *       また、エラーデータを検知した場合は、当該機能終了時にエラーメール（共通機能）
 *       を送信する。
 *
 *   【引数説明】
 *       -debug(-DEBUG)                  : デバッグモードでの実行
 *                                        （トレース出力機能が有効）
 *
 *   【戻り値】
 *      10   ： 正常
 *      49   ： 警告
 *      99   ： 異常
 *
 *---------------------------------------------------------------------------------
 *   稼働環境
 *      Red Hat Enterprise Linux 6（64bit）
 *      (文字コード ： UTF8)
 *---------------------------------------------------------------------------------
 *   改定履歴
 *      1.00 : 2013/04/25 SSI.本田：初版
 *      1.01 : 2013/06/11 SSI.本田：在籍開始年月、入会店の設定を変更
 *      2.00 : 2013/07/04 SSI.上野：MM顧客情報の最終静態更新日、最終静態更新日時の
 *                                  設定値を変更。
 *      3.00 : 2013/08/16 SSI.本田：共通の住所取得処理SQLをバインド変数化したため
 *                                  再ビルド
 *      4.00 : 2016/03/18 SSI.田頭：モバイルカスタマーポータル対応
 *                                  モバイル会員を検索条件に追加
 *      5.00 : 2016/08/03 SSI.田頭：C-MAS対応
 *                                   退会時にあわせてモバイルの退会
 *                                   ファイルレイアウト変更に伴う修正。
 *                                   (ＤＭ止め区分、Ｅメール止め区分)
 *                                   MSサークル顧客情報、MMマタニティベビー情報の
 *                                   更新処理追加（ＤＭ止め区分、Ｅメール止め区分）
 *                                   退会時のEメールアドレス４クリア更新処理追加
 *                                  課題管理票No194
 *                                   MM顧客企業別属性情報の入会年月日更新を
 *                                   行わないよう修正。
 *      6.00 : 2016/11/07 SSI.田  ：退会機能無効のように修正
 *      7.00 : 2017/03/30 SSI.上野：電話番号更新変更
 *                                    電話番号1,2:ファイル内容をそのまま更新するように変更
 *                                    電話番号3:ファイル内容が未設定の場合更新しないように変更
 *     30.00 : 2021/02/02 NDBS.緒方: 期間限定Ｐ対応によりリコンパイル
 *                                  (顧客データロック処理内容更新のため)
 *                                  （利用可能ポイント　項目変更のためコメント）
 *     31.00 : 2021/12/09 SSI.上野：共通関数(C_InsertDayPoint)修正に伴いリコンパイル
 *     40.00 : 2022/10/11 SSI.申  ：MCCM初版
 *     41.00 : 2023/08/24 SSI.申  ：住所２桁数80⇒100桁に対応
 *---------------------------------------------------------------------------------
 *  $Id:$
 *---------------------------------------------------------------------------------
 *  Copyright (C) 2012 NTT DATA CORPORATION
 *********************************************************************************/
@Service
public class CmBTecenBServiceImpl extends CmABfuncLServiceImpl implements CmBTecenBService {
    /*-------------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                       */
    /*-------------------------------------------------------------------------------*/
    /*      内部関数単位にトレース出力要否が設定できるように定義                     */

    boolean DBG_LOG = true;                 /* デバッグメッセージ出力 */
    Integer EOF = -1;

    /*-------------------------------------------------------------------------------*/
    /*  ＯＲＡＣＬＥ                                                                 */
    /*-------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------------*/
    /*  定数定義                                                                     */
    /*-------------------------------------------------------------------------------*/
    int C_const_APWR = 49;         /* プログラム戻り値（警告）    */
    int DEF_OFF = 0;         /* OFF */
    int DEF_ON = 1;         /* ON */
    String PG_NAME = "ＥＣ会員情報取込";    /* プログラム名称 */

    int DEF_BUFSIZE2 = 2; /* 2バイト */
    int DEF_BUFSIZE4 = 4; /* 4バイト */
    int DEF_BUFSIZE5 = 5; /* 5バイト */
    int DEF_BUFSIZE6 = 6; /* 6バイト */
    int DEF_BUFSIZE7 = 7; /* 7バイト */
    int DEF_BUFSIZE8 = 8; /* 8バイト */
    int DEF_BUFSIZE10 = 10; /* 10バイト */
    int DEF_BUFSIZE13 = 13; /* 13バイト */
    int DEF_BUFSIZE15 = 15; /* 15バイト */
    int DEF_BUFSIZE16 = 16; /* 16バイト */
    int DEF_BUFSIZE20 = 20; /* 20バイト */
    int DEF_BUFSIZE30 = 30; /* 30バイト */
    int DEF_BUFSIZE32 = 32; /* 32バイト */
    int DEF_BUFSIZE40 = 40; /* 40バイト */
    int DEF_BUFSIZE50 = 50; /* 50バイト */
    int DEF_BUFSIZE80 = 80; /* 80バイト */
    int DEF_BUFSIZE120 = 120; /* 120バイト */
    //            int DEF_BUFSIZE50   =    50        ; /* 50バイト */
    int DEF_BUFSIZE256 = 256; /* 256バイト */
    int DEF_BUFSIZE4K = 4 * 1024; /* 4 * 1024バイト */
    int DEF_BUFSIZE8K = 8 * 1024; /* 8 * 1024バイト */
    int DEF_BUFSIZE16K = 16 * 1024; /* 16 * 1024バイト */

    /*-----  引数（引数の種類分定義する）----------*/
    String DEF_DEBUG = "-DEBUG";    /* デバッグスイッチ */
    String DEF_debug = "-debug";    /* デバッグスイッチ */
    String DEF_ARG_I = "-i";    /* 入力ファイル名   */

    /*---------------------------------------------*/
    int DEF_Read_EOF = 9;        /* File read EOF               */

    /*-----  ログ出力用メッセージID ----------*/
    String DEF_MSG_ID_102 = "102";
    String DEF_MSG_ID_103 = "103";
    String DEF_MSG_ID_104 = "104";
    String DEF_MSG_ID_105 = "105";
    String DEF_MSG_ID_106 = "106";
    String DEF_MSG_ID_107 = "107";
    String DEF_MSG_ID_108 = "108";
    String DEF_MSG_ID_902 = "902";
    String DEF_MSG_ID_903 = "903";
    String DEF_MSG_ID_904 = "904";
    String DEF_MSG_ID_910 = "910";
    String DEF_MSG_ID_912 = "912";

    String DEF_STR1 = "\"";
    String DEF_STR2 = ",";
    String DEF_STR3 = "\r\n";
    String DEF_STR4 = "\r";
    String DEF_STR5 = "\n";

    String DEF_OUTPUT_FILENAME = "_ec_kaiin_touroku_error.csv";   /* 出力ファイル名 */
    String DEF_KINOU_ID = "ECEN";           /* 機能ID */

    String HALF_SPACE4 = "    ";
    String HALF_SPACE10 = "          ";
    String HALF_SPACE11 = "           ";
    String HALF_SPACE13 = "             ";
    String HALF_SPACE15 = "               ";
    String HALF_SPACE20 = "                    ";
    String HALF_SPACE23 = "                       ";
    String HALF_SPACE40 = "                                        ";
    String HALF_SPACE60 = "                                                            ";
    String HALF_SPACE80 = "                                                                                ";

    /* 半角ZERO11桁 */
    String HALF_ZERO11 = "00000000000";

    String C_OUT_HEADER = "\"処理年月日\",\"登録日\",\"記入日\",\"入会企業コード\",\"入会企業名称\",\"入会店番号\",\"入会店舗名称\",\"会員番号\",\"氏名（漢字）\",\"理由コード\",\"エラー理由\"";

    /* 旧販社コード（SG） */
    int C_HANSHA_SG = 2;

    /*-------------------------------------------------------------------------------*/
    /*  ＨＯＳＴ変数                                                                 */
    /*-------------------------------------------------------------------------------*/
//    EXEC SQL BEGIN DECLARE SECTION;

    int chk_arg_i;                           /* 引数-iチェック用     */
    StringDto arg_i_Value = new StringDto(256);                     /* 引数i設定値          */

    /* 入会エラーリストファイル作成用                                           */
    ItemDto h_syori_ymd = new ItemDto();                         /* 処理年月日           */
    ItemDto h_data_kubun = new ItemDto();                        /* データ区分           */
    ItemDto h_irai_ymd = new ItemDto();                          /* 依頼年月日           */
    ItemDto h_batch_no = new ItemDto();                          /* バッチ番号           */
    ItemDto h_nyukai_ymd = new ItemDto();                        /* 入会年月日           */
    ItemDto h_moushikomi_kigyou_cd = new ItemDto();                /* 申込企業コード       */
    ItemDto h_kigyou_name = new ItemDto(40 * 3 + 1);               /* 企業名称             */
    ItemDto h_moushikomisyoten_no = new ItemDto();               /* 申込書店番号         */
    ItemDto h_kanji_tenpo_name = new ItemDto(80 * 3 + 1);          /* 漢字店舗名称         */
    ItemDto h_kaiin_no = new ItemDto();                          /* 会員番号             */
    ItemDto h_kaiin_name = new ItemDto(80 * 3 + 1);                /* 会員氏名             */
    ItemDto h_riyuu_cd = new ItemDto();                          /* 理由コード           */
    ItemDto h_error_msg = new ItemDto(80 + 1);                 /* エラー理由           */

    /* MS顧客制度情報静態情報用(静態状態チェック用)                             */
    ItemDto h_seitai_flag = new ItemDto();                       /* 静態取込済フラグ     */
    ItemDto h_birth_month = new ItemDto();                       /* 誕生月               */
    ItemDto h_zaiseki_yyyymm = new ItemDto();                    /* 在籍開始年月         */
    ItemDto h_tel_flag = new ItemDto();                          /* 電話番号登録フラグ   */
    ItemDto hi_msks_sw = new ItemDto();                          /* 追加(0)／更新(1)switch */
    /* 2022/10/12 MCCM初版 ADD START */
    ItemDto h_mcc_seido_kyodaku_flg = new ItemDto();             /* ＭＣＣ制度許諾フラグ   */
    ItemDto h_mcc_seido_kyodaku_koshinsha = new ItemDto();       /* ＭＣＣ制度許諾更新者   */
    ItemDto h_mcc_seido_kyodaku_koshin_ymdhms = new ItemDto();   /* ＭＣＣ制度許諾更新日時 */
    /* 2022/10/12 MCCM初版 ADD END */

    /* MM顧客情報用                                                             */
    ItemDto h_end_seitai_update_yyyymmdd = new ItemDto();        /* 最終静態更新日       */
    ItemDto h_end_seitai_update_hh24miss = new ItemDto();        /* 最終静態更新時刻     */
    ItemDto h_end_seitai_update_date = new ItemDto();            /* 最終静態更新日時     */
    ItemDto h_end_seitai_update_date_file = new ItemDto();       /* 最終更新日時（連動ファイル）*/
    ItemDto h_end_seitai_update_date_file_ymd = new ItemDto();   /* 最終更新日（連動ファイル）*/
    ItemDto h_end_seitai_update_date_file_hms = new ItemDto();   /* 最終更新時刻（連動ファイル）*/
    ItemDto h_last_upd_date_yyyymmddhhmmss = new ItemDto(19 + 1);/* 最終更新日時（連動ファイル）*/
    ItemDto h_bath_yyyy = new ItemDto();                         /* 誕生年               */
    ItemDto h_bath_mm = new ItemDto();                           /* 誕生月               */
    ItemDto h_seibetsu = new ItemDto();                          /* 性別                 */
    ItemDto h_kana_name = new ItemDto(40 * 3 + 1);                 /* 顧客カナ名称         */
    ItemDto h_emp_kbn = new ItemDto();                           /* 社員区分             */
    ItemDto h_age = new ItemDto();                               /* 年齢                 */
    ItemDto h_birth_year = new ItemDto();                        /* 誕生年               */
    ItemDto h_birth_day = new ItemDto();                         /* 誕生日               */
    ItemDto h_marriage = new ItemDto();                          /* 婚姻                 */
    ItemDto hi_mmki_sw = new ItemDto();                          /* 追加(0)／更新(1)switch */

    /* MM顧客属性情報用                                                         */
    ItemDto h_zip = new ItemDto(10 + 1);                         /* 郵便番号             */
    ItemDto h_zip_code = new ItemDto(23 + 1);                    /* 郵便番号コード       */
    ItemDto h_address1 = new ItemDto(10 * 3 + 1);                  /* 住所1                */
    ItemDto h_address2 = new ItemDto(100 * 3 + 1);                 /* 住所2                */
    //char           h_address3[80*3+1];                  /* 住所3                */                               /* 2023/01/23 MCCM初版 DEL */
    ItemDto h_address = new ItemDto(200 * 3 + 1);                  /* 住所                 */                                 /* 2022/10/12 MCCM初版 ADD */
    ItemDto h_telephone1 = new ItemDto(15 + 1);                  /* 電話番号1            */
    ItemDto h_telephone2 = new ItemDto(15 + 1);                  /* 電話番号2            */
    ItemDto h_kensaku_denwa_no_1 = new ItemDto(15 + 1);          /* 検索電話番号１       */
    ItemDto h_kensaku_denwa_no_2 = new ItemDto(15 + 1);          /* 検索電話番号２       */
    ItemDto h_email1 = new ItemDto(60 + 1);                      /* Eメールアドレス1     */
    ItemDto h_email2 = new ItemDto(60 + 1);                      /* Eメールアドレス2     */
    ItemDto h_telephone3 = new ItemDto(15 + 1);                  /* 電話番号３           */
    ItemDto h_telephone4 = new ItemDto(15 + 1);                  /* 電話番号４           */
    ItemDto h_kensaku_denwa_no_3 = new ItemDto(15 + 1);          /* 検索電話番号３       */
    ItemDto h_kensaku_denwa_no_4 = new ItemDto(15 + 1);          /* 検索電話番号４       */
    ItemDto h_job = new ItemDto(40 * 3 + 1);                       /* 職業                 */
    ItemDto h_jitaku_jusho_code = new ItemDto(11 + 1);           /* 自宅住所コード       */
    ItemDto h_todofuken_cd = new ItemDto();                      /* 都道府県コード       */                                 /* 2022/10/12 MCCM初版 ADD */
    ItemDto upd_h_zip = new ItemDto(10 + 1);                     /* 更新_郵便番号        */
    ItemDto upd_h_zip_code = new ItemDto(23 + 1);                /* 更新_郵便番号コード  */
    ItemDto upd_h_address1 = new ItemDto(10 * 3 + 1);              /* 更新_住所1           */
    ItemDto upd_h_address2 = new ItemDto(100 * 3 + 1);             /* 更新_住所2           */
    //char           upd_h_address3[80*3+1];              /* 更新_住所3           */                               /* 2023/01/23 MCCM初版 DEL */
    ItemDto upd_h_address = new ItemDto(200 * 3 + 1);              /* 更新_住所            */                                 /* 2022/10/12 MCCM初版 ADD */
    ItemDto upd_h_telephone1 = new ItemDto(15 + 1);              /* 更新_電話番号1       */
    ItemDto upd_h_telephone2 = new ItemDto(15 + 1);              /* 更新_電話番号2       */
    ItemDto upd_h_telephone3 = new ItemDto(15 + 1);              /* 更新_電話番号3       */
    ItemDto upd_h_telephone4 = new ItemDto(15 + 1);              /* 更新_電話番号4       */
    ItemDto upd_h_kensaku_denwa_no_1 = new ItemDto(15 + 1);      /* 更新_検索電話番号１  */
    ItemDto upd_h_kensaku_denwa_no_2 = new ItemDto(15 + 1);      /* 更新_検索電話番号２  */
    ItemDto upd_h_kensaku_denwa_no_3 = new ItemDto(15 + 1);      /* 更新_検索電話番号３  */
    ItemDto upd_h_kensaku_denwa_no_4 = new ItemDto(15 + 1);      /* 更新_検索電話番号４  */
    ItemDto upd_h_todofuken_cd = new ItemDto();                  /* 更新_都道府県コード  */                                 /* 2022/10/12 MCCM初版 ADD */

    /* MM顧客情報用・MM顧客属性情報(静態状態チェック用)                         */
    ItemDto h_db_name = new ItemDto(80 * 3 + 1);
    ItemDto h_db_kana_name = new ItemDto(40 * 3 + 1);
    ItemDto h_db_zip = new ItemDto(10 + 1);
    ItemDto h_db_address1 = new ItemDto(10 * 3 + 1);
    ItemDto h_db_address2 = new ItemDto(100 * 3 + 1);
    /* TS利用可能ポイント情報用                                                 */
    ItemDto h_riyo_kano_point = new ItemDto();                   /* 利用可能ポイント     */
    ItemDto h_nyukai_kigyou_cd = new ItemDto();                  /* 入会企業コード       */
    ItemDto h_nyukai_tenpo = new ItemDto();                      /* 入会店舗             */
    ItemDto h_nyukai_oldcorp_cd = new ItemDto();                 /* 入会旧販社コード     */
    ItemDto h_haken_kigyou_cd = new ItemDto();                   /* 発券企業コード       */
    ItemDto h_haken_tenpo = new ItemDto();                       /* 発券店舗             */

    /* WMバッチ処理実行管理用                                                   */
    ItemDto h_kinou_id = new ItemDto(6 + 1);                   /* 機能ID               */
    ItemDto h_jikkouzumi_cnt = new ItemDto();                    /* 実行済件数           */

    /* MSカード情報用                                                           */
    ItemDto h_card_status = new ItemDto();                       /* カードステータス     */
    ItemDto h_pid = new ItemDto(16);                           /* 会員番号 [入力F]     */
    ItemDto h_tenpo_no = new ItemDto();                          /* 入会店番号           */
    ItemDto h_uid = new ItemDto(16);                           /* 顧客番号 [/SQ顧客番号発番] */
    ItemDto h_cdkaiin_no = new ItemDto(16);                    /* 会員番号             */
    ItemDto h_service_shubetsu = new ItemDto();                  /* サービス種別         */
    ItemDto h_kigyo_cd = new ItemDto();                          /* 企業コード           */
    ItemDto h_kyu_hansha_cd = new ItemDto();                     /* 旧販社コード         */
    ItemDto h_cdriyu_cd = new ItemDto();                         /* 理由コード           */
    ItemDto h_hakko_ymd = new ItemDto();                         /* 発行年月日           */
    ItemDto h_apkaiin_no = new ItemDto(16);                    /* AP会員番号           */
    ItemDto h_goopon_no = new ItemDto(17);                     /* ＧＯＯＰＯＮ番号 [/ＧＯＯＰＯＮ番号発番] */             /* 2022/10/12 MCCM初版 ADD */

    /* パンチデータエラー情報用                                                 */
    ItemDto h_moushikomiten_no = new ItemDto();                  /* 申込店番号 [入力F]   */

    /* PS店表示情報用                                                           */
    ItemDto h_ps_corpid = new ItemDto();                         /* 企業コード           */
    ItemDto h_ps_oldcorp = new ItemDto();                        /* 旧販社コード         */
    ItemDto h_renkei_mise_no = new ItemDto();                    /* 連携用店番号         */                                 /* 2022/10/12 MCCM初版 ADD */

    /* MM顧客企業別属性情報用                                                   */
    ItemDto hi_mmkg_sw = new ItemDto();                          /* 追加(0)／更新(1)switch */
    ItemDto h_mmkg_kigyou_cd = new ItemDto();                    /* カード会員企業コード */
    ItemDto h_mmkg_nyukai_date = new ItemDto();                  /* カード会員入会年月日 */
    ItemDto h_mmkg_taikai_date = new ItemDto();                  /* カード会員退会年月日 */

    /* 処理・共通用                                                             */
    ItemDto h_uid_varchar = new ItemDto(15 + 1);                 /* 顧客番号             */
    ItemDto seitai_flg = new ItemDto();                          /* 静態状態有無フラグ   */
    ItemDto h_programid = new ItemDto(20 + 1);   /* 最終更新プログラムID */
    ItemDto h_programid_ver = new ItemDto(20 + 1);/* 最終更新プログラムID */
    ItemDto h_bat_yyyymmdd = new ItemDto();                      /* バッチ処理日付(当日) */
    ItemDto h_bat_yyyymmdd_1 = new ItemDto();                    /* バッチ処理日付(前日) */
    ItemDto h_kijyun_yyyymmdd = new ItemDto();                   /* 基準日               */
    ItemDto h_kijyun_yyyymm = new ItemDto();                     /* 基準年月             */
    ItemDto h_taikai_date = new ItemDto();                       /* 退会日               */
    StringDto str_sql = new StringDto(4096 * 4);                   /* 実行用SQL文字列      */
    ItemDto h_count = new ItemDto();                             /* 住所１チェック用     */
    ItemDto gh_thisyear_bot = new ItemDto(3);      /* 当年の下１桁 全角                  */
    ItemDto gh_thismonth = new ItemDto(6);         /* 当月 全角                          */
    ItemDto gh_day_kbn = new ItemDto(3);           /* 奇数年（１）／偶数年（０）区分     */
    ItemDto gl_bat_month = new ItemDto(7);
    /**
     * バッチ処理日付年月
     **/
    ItemDto gl_bat_year = new ItemDto(5);
    /**
     * バッチ処理日付年
     **/
    ItemDto gh_year = new ItemDto();                 /* 年                                 */
    ItemDto thisyear_1char_buf = new ItemDto();
    /**
     * 当年の下１桁
     **/
    ItemDto gh_pointy_gekkan_rankup_kingaku = new ItemDto(); /* ポイント年別 月間ランクＵＰ対象金額ＭＭ */
    ItemDto gh_kojin_nenji_rank = new ItemDto();             /* 顧客制度 年次ランクコードＹ             */
    ItemDto gh_kojin_getuji_rank = new ItemDto();            /* 顧客制度 月次ランクコードＮＭＭ         */
    ItemDto gh_kazoku_nenkan_rankup_kingaku = new ItemDto(); /* 家族制度 年間家族ランクアップ対象金額Ｙ */
    ItemDto gh_kazoku_gekkan_rankup_kingaku = new ItemDto(); /* 家族制度 月間家族ランクＵＰ金額ＮＭＭ   */
    ItemDto gh_kazoku_nenji_rank_cd = new ItemDto();         /* 家族制度 年次ランクコードＹ             */
    ItemDto gh_kazoku_getuji_rank_cd = new ItemDto();        /* 家族制度 月次ランクコードＮＭＭ         */
    ItemDto gh_shori_seq = new ItemDto();                    /* 処理通番                                */

    /* 使用テーブルヘッダーファイルをインクルード */
//    EXEC SQL INCLUDE    MM_KOKYAKU_INFO_DATA.h;         /* MM顧客情報                */
//    EXEC SQL INCLUDE    MM_KOKYAKU_ZOKUSE_INFO_DATA.h;  /* MM顧客属性情報            */
//    EXEC SQL INCLUDE    MM_KOKYAKU_KIGYOBETU_ZOKUSE.h;  /* MM顧客企業別属性情報      */
//    EXEC SQL INCLUDE    TM_PUNCH_DATA_ERROR_INFO.h;     /* TMパンチデータエラー情報  */
//    EXEC SQL INCLUDE    MS_KOKYAKU_SEDO_INFO_DATA.h;    /* MS顧客制度情報            */
//    EXEC SQL INCLUDE    MS_CARD_INFO.h;                 /* MSカード情報              */
//    EXEC SQL INCLUDE    TS_YEAR_POINT_DATA.h;          /* TSポイント年別情報         */
//    EXEC SQL INCLUDE TSHS_DAY_POINT_DATA.h;         /* HSポイント日別情報         */

    /* 使用テーブルデータ領域設定 */
    MM_KOKYAKU_INFO_TBL MmkiData = new MM_KOKYAKU_INFO_TBL();                /* MM顧客情報                */
    MM_KOKYAKU_ZOKUSE_INFO_TBL MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();             /* MM顧客属性情報            */
    MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL MmkgData = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();        /* MM顧客企業別属性情報      */
    TM_PUNCH_DATA_ERROR_INFO_TBL tmpe_buff = new TM_PUNCH_DATA_ERROR_INFO_TBL();          /* TMパンチデータエラー情報  */
    MS_KOKYAKU_SEDO_INFO_TBL MsksData = new MS_KOKYAKU_SEDO_INFO_TBL();               /* MS顧客制度情報            */
    MS_CARD_INFO_TBL MscdData = new MS_CARD_INFO_TBL();                   /* MSカード情報              */
    TS_YEAR_POINT_TBL TsptData = new TS_YEAR_POINT_TBL();   /* TSポイント年別情報バッファ         */
    TSHS_DAY_POINT_TBL HsptymdData = new TSHS_DAY_POINT_TBL();   /* HSポイント日別情報バッファ         */

    /* 2022/10/12 MCCM初版 ADD START */
    /* PS会員番号体系用                                                             */
    IntegerDto h_ps_kyu_hansha_cd = new IntegerDto();                    /* 旧販社コード         */
    ItemDto h_ps_taikei_count = new ItemDto();
    /* 2022/10/12 MCCM初版 ADD END */

//    EXEC SQL END DECLARE SECTION;


    /*-------------------------------------------------------------------------------*/
    /*  入出力ファイル                                                               */
    /*-------------------------------------------------------------------------------*/
    /* 出力ファイル */
    FileStatusDto fp_in;
    FileStatusDto fp_out;
    EC_RENDO_DATA in_ec_rendo = new EC_RENDO_DATA();

    int EC_RENDO_DATA_LEN = in_ec_rendo.size();

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
    int mscd_data_taikai_cnt;                   /* MSカード情報更新件数（退会） */
    int mmpd_data_taikai_cnt;                   /* MM顧客情報更新件数（退会） */
    int msps_data_taikai_cnt;                   /* MS顧客制度情報更新件数（退会） */
    int mmpz_data_taikai_cnt;                   /* MM顧客属性情報更新件数（退会） */
    int mmcp_data_taikai_cnt;                   /* MM顧客企業別属性情報更新件数（退会） */
    int tsup_data_taikai_cnt;                   /* TS利用可能ポイント情報更新件数（退会） */
    int hspd_data_taikai_cnt;                   /* HSポイント日別情報更新件数（退会） */
    int syorizumi_cnt;                          /* 前回処理済み件数 */
//int   mmmb_data_taikai_cnt;                   /* MMマタニティベビー情報更新件数（退会） */                   /* 2022/10/12 MCCM初版 DEL */
//int   mssk_data_taikai_cnt;                   /* MSサークル顧客情報更新件数（退会） */                       /* 2022/10/12 MCCM初版 DEL */

    StringDto path_in = new StringDto(DEF_BUFSIZE256);                /* 入力ファイルのディレクトリ＋ファイル名 */
    StringDto path_out = new StringDto(DEF_BUFSIZE256);               /* 出力ファイルのディレクトリ＋ファイル名 */

    StringDto out_format_buf = new StringDto(DEF_BUFSIZE4K);          /* APログフォーマット */
    StringDto in_dir = new StringDto(DEF_BUFSIZE4K);          /* 入力ファイルディレクトリ */
    StringDto ap_work_dir = new StringDto(DEF_BUFSIZE4K);             /* 出力ファイルディレクトリ */

    int card_flag;                              /* カード新旧フラグ */
    int point_kaiin_flag;                       /* ポイント会員フラグ */
    int uid_flg;                                /* 顧客データ有無フラグ */
    int ap_kaiin_flag;                          /* AP会員フラグ */
    int up_maternity_circle_flag;               /* マタニティベビー、サークル更新フラグ */

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
    public MainResultDto main(int argc, String[] argv) {
        IntegerDto rtn_cd = new IntegerDto();                    /* 関数戻り値 */
        IntegerDto rtn_status = new IntegerDto();               /* 関数ステータス */
        int arg_cnt;                   /* 引数チェック用カウンタ */
        String env_input;                /* 入力ファイルDIR */
        String env_output;               /* 出力ファイルDIR */
        StringDto arg_Work1 = new StringDto(DEF_BUFSIZE256); /* Work Buffer1 */

        /*-----------------------------------------------*/
        /*  プログラム名取得                             */
        /*-----------------------------------------------*/
        rtn_cd.arr = C_GetPgname(argv);
        if (rtn_cd.arr != C_const_OK) {
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetPgname", rtn_cd, 0, 0, 0, 0);

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*  開始メッセージ */
        APLOG_WT(DEF_MSG_ID_102, 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* HOST変数にプログラム名をコピー */
        memset(h_programid, 0x00, sizeof(h_programid));
        memset(h_programid_ver, 0x00, sizeof(h_programid_ver));
        memcpy(h_programid, Cg_Program_Name, sizeof(Cg_Program_Name));
        memcpy(h_programid_ver, Cg_Program_Name_Ver, sizeof(Cg_Program_Name_Ver));

        /*-----------------------------------------------*/
        /*  バッチデバッグ開始                           */
        /*-----------------------------------------------*/
        rtn_cd.arr = C_StartBatDbg(argc, argv);
        if (rtn_cd.arr != C_const_OK) {
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
            strcpy(arg_Work1, argv[arg_cnt]);
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック対象パラメータ = [%s]\n", arg_Work1);
                /*--------------------------------------------------------------------*/
            }
            /* デバッグモードの指定 */
            if (strcmp(arg_Work1, DEF_DEBUG) == 0 || strcmp(arg_Work1, DEF_debug) == 0) {
                continue;
            } else if (memcmp(arg_Work1, DEF_ARG_I, 2) == 0) { /* -iの場合         */
                rtn_cd.arr = cmBTcentB_Chk_Arg(arg_Work1.strVal());    /* パラメータチェック */
                if (rtn_cd.arr == C_const_OK) {
                    strcpy(arg_i_Value, arg_Work1.substring(2));
                } else {
                    sprintf(out_format_buf, "-i 引数の値が不正です（%s）", arg_Work1);
                    APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                    rtn_cd.arr = C_EndBatDbg();                 /* バッチデバッグ終了 */
                    return exit(C_const_APNG);
                }
            }
            /* 規定外パラメータ  */
            else {
                rtn_cd.arr = C_const_NG;
                sprintf(out_format_buf, "定義外の引数（%s）", arg_Work1);
            }
            if (DBG_LOG) {
                /*-------------------------------------------------------------*/
                C_DbgMsg("*** main *** チェック結果 = [%d]\n", rtn_cd);
                /*-------------------------------------------------------------*/
            }
            /* パラメータのチェック結果がNG */
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
                rtn_cd.arr = C_EndBatDbg();     /* バッチデバッグ終了処理 */
                return exit(C_const_APNG);
            }
        }

        /* 必須パラメータ未指定チェック */
        if (chk_arg_i == DEF_OFF) {
            sprintf(out_format_buf, "-i 引数の値が不正です");
            APLOG_WT("910", 0, null, out_format_buf, 0, 0, 0, 0, 0);
            rtn_cd.arr = C_EndBatDbg();                         /* バッチデバッグ終了 */
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
        rtn_cd.arr = C_OraDBConnect(C_ORACONN_MD, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** DBコネクトNG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** DBコネクトNG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_OraDBConnect", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd.arr = C_EndBatDbg();

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
        rtn_cd.arr = C_GetBatDate(0, bat_yyyymmdd, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得(当日指定)NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd.arr = C_EndBatDbg();

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
        strncpy(bat_yyyy, bat_yyyymmdd, 4);

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
        rtn_cd.arr = C_GetBatDate(-1, bat_yyyymmdd_1, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** バッチ処理日取得(前日指定)NG rtn= %d\n", rtn_cd);
                C_DbgMsg("*** main *** バッチ処理日取得(前日指定)NG status= %d\n", rtn_status);
            }
            /*  エラーメッセージ */
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_GetBatDate", rtn_cd, rtn_status, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd.arr = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        if (DBG_LOG) {
            C_DbgMsg("*** main *** バッチ処理日取得(前日指定)OK [%s]\n", bat_yyyymmdd_1);
        }

        h_bat_yyyymmdd_1.arr = atol(bat_yyyymmdd_1);
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
            rtn_cd.arr = C_EndBatDbg();

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
            rtn_cd.arr = C_EndBatDbg();

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
        mscd_data_taikai_cnt = 0;                         /* MSカード情報更新件数（退会） */
        mmpd_data_taikai_cnt = 0;                         /* MM顧客情報更新件数（退会） */
        msps_data_taikai_cnt = 0;                         /* MS顧客制度情報更新件数（退会） */
        mmpz_data_taikai_cnt = 0;                         /* MM顧客属性情報更新件数（退会） */
        mmcp_data_taikai_cnt = 0;                         /* MM顧客企業別属性情報更新件数（退会） */
        tsup_data_taikai_cnt = 0;                         /* TS利用可能ポイント情報更新件数（退会） */
        hspd_data_taikai_cnt = 0;                         /* HSポイント日別情報更新件数（退会） */
//  mmmb_data_taikai_cnt  = 0;                         /* MMマタニティベビー情報更新件数（退会） */          /* 2022/10/12 MCCM初版 DEL */
//  mssk_data_taikai_cnt  = 0;                         /* MSサークル顧客情報更新件数（退会） */              /* 2022/10/12 MCCM初版 DEL */

        /*-----------------------------------------------*/
        /*  入力ファイル名をオープンする                 */
        /*-----------------------------------------------*/
        /* ファイルをオープン */
        if ((fp_in = fopen(path_in.arr, SystemConstant.Shift_JIS, FileOpenType.r)).fd == C_const_NG) {
//        if((fp_in = fopen(path_in, "r" )) == 0x00){
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** OpenFile *** 入力ファイルオープンNG%s\n", "");
                C_DbgMsg("path_in:%s\n", path_in);
            }
            sprintf(out_format_buf, "fopen（%s）", path_in);
            APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, fp_in, 0, 0, 0, 0);

            /* バッチデバッグ終了処理 */
            rtn_cd.arr = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** OpenFile *** 入力ファイルオープン[%s]\n", path_in);
        }

        /*-----------------------------------------------*/
        /*  主処理                                       */
        /*-----------------------------------------------*/
        rtn_cd.arr = cmBTecenB_main();

        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** cmBTecenB_main NG rtn =[%d]\n", rtn_cd);
            }
            APLOG_WT(DEF_MSG_ID_912, 0, null, "ＥＣ会員情報取込処理に失敗しました", 0, 0, 0, 0, 0);

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
            rtn_cd.arr = C_EndBatDbg();

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
        /* MSカード情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MSカード情報（退会）", mscd_data_taikai_cnt, 0, 0, 0, 0);
        /* MM顧客情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客情報（退会）", mmpd_data_taikai_cnt, 0, 0, 0, 0);
        /* MS顧客制度情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MS顧客制度情報（退会）", msps_data_taikai_cnt, 0, 0, 0, 0);
        /* MM顧客属性情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客属性情報（退会）", mmpz_data_taikai_cnt, 0, 0, 0, 0);
        /* MM顧客企業別属性情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MM顧客企業別属性情報（退会）", mmcp_data_taikai_cnt, 0, 0, 0, 0);
        /* TS利用可能ポイント情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "TS利用可能ポイント情報（退会）", tsup_data_taikai_cnt, 0, 0, 0, 0);
        /* HSポイント日別情報（退会） */
        C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "HSポイント日別情報（退会）", hspd_data_taikai_cnt, 0, 0, 0, 0);
//  /* MMマタニティベビー情報（退会） */                                                                                                                                                          /* 2022/10/12 MCCM初版 DEL */
//  C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MMマタニティベビー情報（退会）", mmmb_data_taikai_cnt, 0, 0, 0, 0);      /* 2022/10/12 MCCM初版 DEL */
//  /* MSサークル顧客情報（退会） */                                                                                                                                                              /* 2022/10/12 MCCM初版 DEL */
//  C_APLogWrite(DEF_MSG_ID_107, "出力テーブル名：[%s] 正常処理件数：[%d] エラー件数：[%d]", " ", "MSサークル顧客情報（退会）", mssk_data_taikai_cnt, 0, 0, 0, 0);          /* 2022/10/12 MCCM初版 DEL */
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

        /* WMバッチ処理実行管理を更新 */
        h_jikkouzumi_cnt.arr = 0;
        rtn_cd.arr = WM_Batch_Update();

        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** WM_Batch_Update() NG rtn =[%d]\n", rtn_cd);
            }

            /* ロールバック */
//            EXEC SQL ROLLBACK RELEASE;
            sqlca.rollback();

            /* バッチデバッグ終了処理 */
            rtn_cd.arr = C_EndBatDbg();

            /* 異常終了 */
            return exit(C_const_APNG);
        }

        /*  終了メッセージ */
        APLOG_WT(DEF_MSG_ID_103, 0, null, PG_NAME, 0, 0, 0, 0, 0);

        /* バッチデバッグ終了処理 */
        rtn_cd.arr = C_EndBatDbg();

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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： cmBTecenB_main                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  cmBTecenB_main()                                              */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               ＥＣ会員情報取込主処理                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*                                                                            */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTecenB_main() {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値 */

        if (DBG_LOG) {
            C_DbgStart("*** cmBTecenB_main *** ＥＣ会員情報取込主処理");
        }

        /*-----------------------------------------------*/
        /*  入会連動データ登録処理                       */
        /*-----------------------------------------------*/
        /*入会連動データ登録 */
        rtn_cd.arr = Update_Data();

        if (rtn_cd.arr == C_const_NG) {
            /* 処理終了 */
            return (C_const_NG);
        }

        /*-----------------------------------------------*/
        /*  出力ファイル名をオープンする                 */
        /*-----------------------------------------------*/
        /* ファイルをオープン */
        if ((fp_out = open(path_out.arr)).fd == -1) {
//        if((fp_out = fopen(path_out, "a" )) == 0x00){
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
        rtn_cd.arr = Write_ErrorList_File();

        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** main *** cmBTecenB_main NG rtn =[%d]\n", rtn_cd);
            }
            APLOG_WT(DEF_MSG_ID_912, 0, null, "入会エラーリストファイル作成に失敗しました", 0, 0, 0, 0, 0);

            /* 処理終了 */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            C_DbgEnd("*** cmBTecenB_main *** ＥＣ会員情報取込主処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
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

    /******************************************************************************/
    public int Update_Data() {
        IntegerDto rtn_cd = new IntegerDto();                     /* 関数戻り値 */
        int punch_errno = 0;                /* パンチエラー番号 */
        StringDto buf = new StringDto(512);
//        char *pret;                      /* ファイル読み込みチェック用 */
        int lock_sbt;                   /* ロック種別 */
        StringDto wk_lock = new StringDto(2);             /* ロック種別 */
        StringDto wk_kaiin_no = new StringDto(15 + 1);        /* 会員番号ワーク */
        StringDto wk_nyukaibi = new StringDto(8 + 1);         /* 申込年月日ワーク */
        StringDto wk_taikaibi = new StringDto(8 + 1);         /* 退会年月日ワーク */
        IntegerDto rtn_status = new IntegerDto();                 /* 関数ステータス */

        if (DBG_LOG) {
            C_DbgStart("*** Update_Data *** ＥＣ会員情報取込主処理");
        }

        /* 初期化 */
        h_jikkouzumi_cnt.arr = 0;

        /* WMバッチ処理実行管理更新処理 */
//        EXEC SQL SELECT シーケンス番号
//        INTO :h_jikkouzumi_cnt
//        FROM WMバッチ処理実行管理
//        WHERE 機能ＩＤ = :h_kinou_id;
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

        while (true) {

            memset(wk_kaiin_no, 0x00, sizeof(wk_kaiin_no));
            memset(wk_nyukaibi, 0x00, sizeof(wk_nyukaibi));
            memset(wk_taikaibi, 0x00, sizeof(wk_taikaibi));

            /* WMバッチ処理実行管理を更新 */
            rtn_cd.arr = WM_Batch_Update();

            if (rtn_cd.arr != C_const_OK) {
                /* 処理をNGで終了 */
                return (C_const_NG);
            }

            /* コミットを実行 */
//            EXEC SQL COMMIT WORK;
            sqlca.commit();

            /* 実行済み件数をカウントアップ */
//            h_jikkouzumi_cnt ++;
            h_jikkouzumi_cnt.arr = h_jikkouzumi_cnt.intVal() + 1;

            /* 1行読み込む */
            fgets(buf, sizeof(buf), fp_in);

            /* ファイルエンド */
            if (buf.arr == null) {
                if (DBG_LOG) {
                    C_DbgMsg("Update_Data() fgets() == NULL break; %s\n", "");
                }
                break;
            }

            /* ファイルエラーチェック */
            rtn_cd.arr = ferror(fp_in);
            if (rtn_cd.arr != C_const_OK) {
                fclose(fp_in);
                fp_in = null;
                sprintf(out_format_buf, "fgets(%s)", path_in);
                /*  エラーメッセージ */
                APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, 0, 0, 0, 0, 0);
                return (C_const_NG);
            }

            /* 構造体にデータを格納 */
            Split_Data(buf);

            /* 入力データ件数をカウントアップ */
            input_data_cnt++;

            /* 実行済件数分読み飛ばす */
            if (syorizumi_cnt > 0) {
                if (input_data_cnt <= syorizumi_cnt) {
                    continue;
                }
            }

            /* 会員番号 */
            strncpy(wk_kaiin_no, in_ec_rendo.kaiin_no.strVal(), strlen(in_ec_rendo.kaiin_no));
            h_kaiin_no.arr = atol(wk_kaiin_no);

            /* 入会年月日 */
            strncpy(wk_nyukaibi, in_ec_rendo.nyukaibi_yyyymmdd.strVal(), strlen(in_ec_rendo.nyukaibi_yyyymmdd));
            h_kijyun_yyyymmdd.arr = atoi(wk_nyukaibi);
            MmkiData.nyukai_entry_paper_ymd.arr = atoi(wk_nyukaibi);                                                   /* 2022/10/12 MCCM初版 ADD */
            /* 基準年月を設定 */
            h_kijyun_yyyymm.arr = h_kijyun_yyyymmdd.intVal() / 100;
            if (h_kijyun_yyyymm.intVal() < 201304) {
                h_kijyun_yyyymm.arr = 201304;
            }

            /* 退会年月日 */
            strncpy(wk_taikaibi, in_ec_rendo.taikaibi_yyyymmdd.strVal(), strlen(in_ec_rendo.taikaibi_yyyymmdd));
            h_taikai_date.arr = atoi(wk_taikaibi);

            /* カード情報チェック */
            if (DBG_LOG) {
                C_DbgMsg("*** Update_Data *** カード情報チェック%s\n", "");
                C_DbgMsg("基準日        =[%d]\n", h_kijyun_yyyymmdd);
                C_DbgMsg("基準年月      =[%d]\n", h_kijyun_yyyymm);
                C_DbgMsg("退会日        =[%d]\n", h_taikai_date);
            }

            rtn_cd.arr = CheckMscard(in_ec_rendo, punch_errno);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** カード情報チェックNG %s\n", "");
                }
                APLOG_WT(DEF_MSG_ID_912, 0, null, "入力連動データ登録チェックに失敗しました", 0, 0, 0, 0, 0);

                /* 処理をNGで終了 */
                return (C_const_NG);
            }
            /* 2022/10/12 MCCM初版 ADD START */
            h_ps_corpid.arr = 0;
            /*---PS会員番号体系テーブルから旧販社コード取得---*/
            /* ＳＱＬを実行する */
//            EXEC SQL SELECT
//            NVL(旧販社コード,0),
//                    企業コード
//            INTO :h_ps_kyu_hansha_cd,
//                           :h_ps_corpid
//                    FROM
//            PS会員番号体系@CMSD
//                    WHERE
//            会員番号開始 <= :h_kaiin_no
//            AND 会員番号終了 >= :h_kaiin_no
//            AND サービス種別 = 2;

            sqlca.sql = new StringDto("SELECT NVL(旧販社コード,0), 企業コード FROM PS会員番号体系 WHERE 会員番号開始 <= ? AND 会員番号終了 >= ? AND サービス種別 = 2");
            sqlca.restAndExecute(h_kaiin_no, h_kaiin_no);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "PS会員番号体系", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }

            sqlca.fetch();
            sqlca.recData(h_ps_kyu_hansha_cd, h_ps_corpid);

            if (DBG_LOG) {
                C_DbgMsg("PS会員番号体系から旧販社コード        =[%d]\n", h_ps_kyu_hansha_cd);
            }

            /* 2022/10/12 MCCM初版 ADD END */
            /* 退会日未設定 */
            if (h_taikai_date.intVal() == 0) {

                /*--------------------*/
                /*---退会済チェック---*/
                /*--------------------*/
                /*---MM顧客企業別属性情報検索処理---*/
                /* 初期化 */
                MmkgData = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
                memset(MmkgData, 0x00, 0);
                /* ＳＱＬを実行する */
//                EXEC SQL SELECT  企業コード,
//                        入会年月日,
//                        退会年月日,
//                        ＤＭ止め区分,
//                        Ｅメール止め区分
//                INTO :MmkgData.kigyo_cd,
//                            :MmkgData.nyukai_ymd,
//                            :MmkgData.taikai_ymd,
//                            :MmkgData.dm_tome_kbn,
//                            :MmkgData.email_tome_kbn
//                FROM  MM顧客企業別属性情報
//                WHERE  顧客番号   = :h_uid
//                AND  企業コード = :h_kigyo_cd;

                sqlca.sql = new StringDto("SELECT  企業コード," +
                        " 入会年月日," +
                        " 退会年月日," +
                        " ＤＭ止め区分," +
                        " Ｅメール止め区分" +
                        " FROM  MM顧客企業別属性情報" +
                        " WHERE  顧客番号   = ?" +
                        " AND  企業コード = ?");
                sqlca.restAndExecute(h_uid, h_kigyo_cd);
                sqlca.fetch();
                sqlca.recData(MmkgData.kigyo_cd, MmkgData.nyukai_ymd, MmkgData.taikai_ymd, MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn);

                /* ＳＱＬを実行結果を判定する */
                if (sqlca.sqlcode == C_const_Ora_OK) {
                    hi_mmkg_sw.arr = 1; /* データ有:Switch Update */
                    if (StringUtils.isEmpty(MmkgData.taikai_ymd.strVal()) || MmkgData.taikai_ymd.intVal() != 0) {
                        /* ロールバック処理呼び出し*/
//                        EXEC SQL ROLLBACK;
                        sqlca.rollback();
                        if (DBG_LOG) {
                            sprintf(out_format_buf, "顧客番号=[%s]、企業コード=[%d]", h_uid, h_kigyo_cd);
                            C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8050(退会済みエラー)[%s]\n", out_format_buf);
                        }
                        /* ８０５０（退会済みエラー）パンチエラーデータ出力 */
                        rtn_cd.arr = OutPunchdata(C_PUNCH_ERRORNO5);
                        if (rtn_cd.arr != C_const_OK) {
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
                    sprintf(out_format_buf, "顧客番号=[%s]、企業コード=[%d]", h_uid, h_kigyo_cd);
                    APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                            "MM顧客企業別属性情報", out_format_buf, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }
                /* 2022/10/12 MCCM初版 ADD START */
                /*---MSカード情報検索処理---*/
                /* 初期化 */
                MmkzData = new MM_KOKYAKU_ZOKUSE_INFO_TBL();
                memset(MmkzData, 0x00, 0);
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
//                AND T3.サービス種別 = 2
//                              );

                sqlca.sql = new StringDto("SELECT T1.顧客番号 FROM MM顧客属性情報 T1,MM顧客情報 T2 WHERE" +
                        " T1.顧客番号 = T2.顧客番号" +
                        " AND T2.属性管理主体システム = 2" +
                        " AND EXISTS (SELECT 1 FROM MSカード情報 T3" +
                        " WHERE 会員番号 = ? AND T3.顧客番号 = T1.顧客番号" +
                        " AND T3.サービス種別 = 2)");
                sqlca.restAndExecute(h_kaiin_no);
                sqlca.fetch();
                sqlca.recData(MmkzData.kokyaku_no);

                /* ＳＱＬを実行結果を判定する */
                if (sqlca.sqlcode == C_const_Ora_OK) {
                    hi_mmkg_sw.arr = 1; /* データ有:Switch Update */

                    /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                    sqlca.rollback();
                    if (DBG_LOG) {
                        sprintf(out_format_buf, "顧客番号=[%s]、会員番号=[%d]", MmkzData.kokyaku_no.arr, h_kaiin_no);
                        C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8070(MK顧客番号エラー)[%s]\n", out_format_buf);
                    }
                    /* ８０７０（MK顧客番号エラー）パンチエラーデータ出力 */
                    rtn_cd.arr = OutPunchdata(C_PUNCH_ERRORNO7);
                    if (rtn_cd.arr != C_const_OK) {
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
                    APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
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

                sqlca.sql = new StringDto("SELECT COUNT(1) FROM PS会員番号体系" +
                        " WHERE 会員番号開始 <= ? AND 会員番号終了 >= ? AND " +
                        " 企業コード IN (3010, 3020, 3040, 3050, 3060)");
                sqlca.restAndExecute(h_kaiin_no, h_kaiin_no);
                sqlca.fetch();
                sqlca.recData(h_ps_taikei_count);

                /* ＳＱＬを実行結果を判定する */

                if (sqlca.sqlcode != C_const_Ora_OK) {
                    sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                    APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                            "PS会員番号体系", out_format_buf, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }
                if (h_ps_taikei_count.intVal() > 0) {

                    /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                    sqlca.rollback();
                    if (DBG_LOG) {
                        sprintf(out_format_buf, "会員番号=[%d]", h_kaiin_no);
                        C_DbgMsg("*** Update_Data *** 会員企業コード取得NG 8080(MK管理会員番号エラー)[%s]\n", out_format_buf);
                    }
                    /* ８０８０（MK顧客番号エラー）パンチエラーデータ出力 */
                    rtn_cd.arr = OutPunchdata(C_PUNCH_ERRORNO8);
                    if (rtn_cd.arr != C_const_OK) {
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
                /* 2022/10/12 MCCM初版 ADD END */
                /* ポイント・顧客ロック */
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** ポイント・顧客ロック%s\n", "");
                }
                rtn_status.arr = 0;

                /* ロック種別は、1(更新) */
                lock_sbt = 1;
                sprintf(wk_lock, "%d", lock_sbt);

                rtn_cd.arr = C_KdataLock(h_uid.strDto(), wk_lock.strVal(), rtn_status);
                if (rtn_cd.arr == C_const_NG) {
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** ポイント・顧客ロック status= %d\n", rtn_status);
                    }
                    APLOG_WT(DEF_MSG_ID_903, 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

                /* 顧客データ有無フラグセット */
                uid_flg = rtn_cd.arr;

                /* マスター更新処理 */
                /* カード情報更新 */
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** カード情報更新%s\n", "");
                }
                rtn_cd.arr = UpdateCard(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
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
                rtn_cd.arr = SeitaiCheck(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 静態情報更新チェックNG %s\n", "");
                    }
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "静態情報更新チェック処理に失敗しました", 0, 0, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

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

                /* 利用可能ポイント情報更新 */
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 利用可能ポイント情報更新%s\n", "");
                }
                rtn_cd.arr = UpdateRiyokanoPoint(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 利用可能ポイント情報更新NG %s\n", "");
                    }
                    APLOG_WT(DEF_MSG_ID_912, 0, null, "利用可能ポイント情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                    /* 処理を終了する */
                    return (C_const_NG);
                }

                if (punch_errno > 0) {
                    /* ロールバック処理呼び出し*/
//                    EXEC SQL ROLLBACK;
                    sqlca.rollback();

                    /* パンチエラーデータ出力 */
                    rtn_cd.arr = OutPunchdata(punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
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

                /* 顧客情報更新 */
                if (DBG_LOG) {
                    C_DbgMsg("*** Update_Data *** 顧客情報更新%s\n", "");
                }
                rtn_cd.arr = UpdateKokyaku(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
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
                    rtn_cd.arr = OutPunchdata(punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
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
                rtn_cd.arr = UpdateKokyakuzokusei(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
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
                    rtn_cd.arr = OutPunchdata(punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
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
                rtn_cd.arr = UpdateKigyobetuzokusei(in_ec_rendo, punch_errno);
                if (rtn_cd.arr != C_const_OK) {
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
                    rtn_cd.arr = OutPunchdata(punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
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
            }
            /* 退会処理 */
            else {
                /*** 2016/11/07 DEL 退会機能無効にするように修正 START ***/
                if (h_taikai_date.intVal() == 0) {
                    /*---MM顧客企業別属性情報検索処理---*/
                    /* 初期化 */
//                    memset(&MmkgData, 0x00, sizeof(MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL));
                    MmkgData = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL();
                    /* ＳＱＬを実行する */
//                    EXEC SQL SELECT  企業コード,
//                            入会年月日,
//                            退会年月日
//                    INTO :MmkgData.kigyo_cd,
//                                :MmkgData.nyukai_ymd,
//                                :MmkgData.taikai_ymd
//                    FROM  MM顧客企業別属性情報
//                    WHERE  顧客番号   = :h_uid
//                    AND  企業コード = :h_kigyo_cd;

                    sqlca.sql = new StringDto("SELECT  企業コード, 入会年月日, 退会年月日 FROM  MM顧客企業別属性情報 WHERE  顧客番号   = ? AND  企業コード = ?");
                    sqlca.restAndExecute(h_uid, h_kigyo_cd);
                    sqlca.fetch();
                    sqlca.recData(MmkgData.kigyo_cd, MmkgData.nyukai_ymd, MmkgData.taikai_ymd);

                    /* ＳＱＬを実行結果を判定する */
                    if (sqlca.sqlcode == C_const_Ora_OK) {
                        hi_mmkg_sw.arr = 1;
                    } /* データ有:Switch Update */ else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
                        hi_mmkg_sw.arr = 0;
                    } /* データ無:Switch Insert */ else { /* DBERR */
                        sprintf(out_format_buf, "顧客番号=[%s]、企業コード=[%d]", h_uid, h_kigyo_cd);
                        APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                                "MM顧客企業別属性情報", out_format_buf, 0, 0);
                        /* 処理を終了する */
                        return C_const_NG;
                    }

                    /* ポイント・顧客ロック */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** ポイント・顧客ロック%s\n", "");
                    }
                    rtn_status = new IntegerDto();

                    /* ロック種別は、1(更新) */
                    lock_sbt = 1;
                    sprintf(wk_lock, "%d", lock_sbt);

                    rtn_cd.arr = C_KdataLock(h_uid.strDto(), wk_lock.strVal(), rtn_status);
                    if (rtn_cd.arr == C_const_NG) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** ポイント・顧客ロック status= %d\n", rtn_status);
                        }
                        APLOG_WT(DEF_MSG_ID_903, 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

                    /* 顧客データ有無フラグセット */
                    uid_flg = rtn_cd.arr;

                    /* 静態情報更新チェック処理 */
                    /* 更新チェックは不要だが各データ取得のために実行 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 静態情報更新チェック%s\n", "");
                    }
                    rtn_cd.arr = SeitaiCheck(in_ec_rendo, punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** 静態情報更新チェックNG %s\n", "");
                        }
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "静態情報更新チェック処理に失敗しました", 0, 0, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

                    /* マスター更新処理 */
                    /* カード情報更新 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** カード情報更新%s\n", "");
                    }
                    rtn_cd.arr = UpdateCardForTaikai(in_ec_rendo, punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** カード情報更新NG %s\n", "");
                        }
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "カード情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

                    /* 顧客情報更新 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 顧客情報更新%s\n", "");
                    }
                    rtn_cd.arr = UpdateKokyakuForTaikai(in_ec_rendo, punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** 顧客情報更新NG %s\n", "");
                        }
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

                    /* 顧客属性情報更新 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 顧客属性情報更新%s\n", "");
                    }
                    rtn_cd.arr = UpdateKokyakuzokuseiForTaikai(in_ec_rendo, punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** 顧客属性情報更新NG %s\n", "");
                        }
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客属性情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

                    /* 顧客企業別属性情報更新 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** Update_Data *** 顧客企業別属性情報更新%s\n", "");
                    }
                    rtn_cd.arr = UpdateKigyobetuzokuseiForTaikai(in_ec_rendo, punch_errno);
                    if (rtn_cd.arr != C_const_OK) {
                        if (DBG_LOG) {
                            C_DbgMsg("*** Update_Data *** 顧客企業別属性情報更新NG %s\n", "");
                        }
                        APLOG_WT(DEF_MSG_ID_912, 0, null, "顧客企業別属性情報更新処理に失敗しました", 0, 0, 0, 0, 0);

                        /* 処理を終了する */
                        return (C_const_NG);
                    }

//                /* マタニティベビー情報更新 */                                                                          /* 2022/10/12 MCCM初版 DEL */
//if( DBG_LOG){                                                                                                             /* 2022/10/12 MCCM初版 DEL */
//                C_DbgMsg( "*** Update_Data *** マタニティベビー情報更新%s\n", "" );                                     /* 2022/10/12 MCCM初版 DEL */
//}                                                                                                                  /* 2022/10/12 MCCM初版 DEL */
//                rtn_cd.arr = UpdateMaternityBabyInfoForTaikai();                                                            /* 2022/10/12 MCCM初版 DEL */
//                if(rtn_cd.arr != C_const_OK){                                                                               /* 2022/10/12 MCCM初版 DEL */
//if( DBG_LOG){                                                                                                             /* 2022/10/12 MCCM初版 DEL */
//                    C_DbgMsg("*** Update_Data *** マタニティベビー情報更新NG %s\n", "");                                /* 2022/10/12 MCCM初版 DEL */
//}                                                                                                                  /* 2022/10/12 MCCM初版 DEL */
//                    APLOG_WT(DEF_MSG_ID_912, 0, null, "マタニティベビー情報更新処理に失敗しました", 0, 0, 0, 0, 0);     /* 2022/10/12 MCCM初版 DEL */

//                    /* 処理を終了する */                                                                                /* 2022/10/12 MCCM初版 DEL */
//                    return(C_const_NG);                                                                                 /* 2022/10/12 MCCM初版 DEL */
//                }                                                                                                       /* 2022/10/12 MCCM初版 DEL */

//                /* サークル顧客情報更新 */                                                                              /* 2022/10/12 MCCM初版 DEL */
//if( DBG_LOG){                                                                                                             /* 2022/10/12 MCCM初版 DEL */
//                C_DbgMsg( "*** Update_Data *** サークル顧客情報更新%s\n", "" );                                         /* 2022/10/12 MCCM初版 DEL */
//}                                                                                                                  /* 2022/10/12 MCCM初版 DEL */
//                rtn_cd.arr = UpdateCircleKokyakuInfoForTaikai();                                                            /* 2022/10/12 MCCM初版 DEL */
//                if(rtn_cd.arr != C_const_OK){                                                                               /* 2022/10/12 MCCM初版 DEL */
//if( DBG_LOG){                                                                                                             /* 2022/10/12 MCCM初版 DEL */
//                    C_DbgMsg("*** Update_Data *** サークル顧客情報更新NG %s\n", "");                                    /* 2022/10/12 MCCM初版 DEL */
//}                                                                                                                  /* 2022/10/12 MCCM初版 DEL */
//                    APLOG_WT(DEF_MSG_ID_912, 0, null, "サークル顧客情報更新処理に失敗しました", 0, 0, 0, 0, 0);         /* 2022/10/12 MCCM初版 DEL */

//                    /* 処理を終了する */                                                                                /* 2022/10/12 MCCM初版 DEL */
//                    return(C_const_NG);                                                                                 /* 2022/10/12 MCCM初版 DEL */
//                }                                                                                                       /* 2022/10/12 MCCM初版 DEL */

                    /* 正常処理データ件数カウントアップ */
                    ok_data_cnt++;
                    /* MSカード情報更新件数カウントアップ */
                    mscd_data_taikai_cnt++;
                    /* MM顧客情報更新件数カウントアップ */
                    mmpd_data_taikai_cnt++;
                    /* MS顧客制度情報更新件数カウントアップ */
                    msps_data_taikai_cnt++;
                    /* MM顧客属性情報更新件数カウントアップ */
                    mmpz_data_taikai_cnt++;
                    /* MM顧客企業別属性情報更新件数カウントアップ */
                    mmcp_data_taikai_cnt++;


                    if (point_kaiin_flag == 0
                            && (h_riyo_kano_point.intVal() > 0 || h_riyo_kano_point.intVal() < 0)) {  /* 利用可能ポイントあり */
                        rtn_cd.arr = PointLost();
                        if (rtn_cd.arr != C_const_OK) {
                            APLOG_WT("912", 0, null, "ポイント失効処理に失敗しました", 0, 0, 0, 0, 0);
                            return C_const_NG;
                        }
                        /* TS利用可能ポイント情報更新件数カウントアップ */
                        tsup_data_taikai_cnt++;
                        /* HSポイント日別情報更新件数カウントアップ */
                        hspd_data_taikai_cnt++;
                    }
                }
                /*** 2016/11/07 DEL 退会機能無効にするように修正 END ***/
            }
        }

        if (DBG_LOG) {
            C_DbgEnd("*** Update_Data *** ＥＣ会員情報取込主処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
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

    /******************************************************************************/
    public int WM_Batch_Update() {

        if (DBG_LOG) {
            C_DbgStart("WM_Batch_Update処理");
        }
        /* WMバッチ処理実行管理更新処理 */
//        EXEC SQL UPDATE WMバッチ処理実行管理
//        SET シーケンス番号 = :h_jikkouzumi_cnt
//        WHERE 機能ＩＤ = :h_kinou_id;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE WMバッチ処理実行管理 " +
                "SET シーケンス番号 = '" + h_jikkouzumi_cnt + "'" +
                "WHERE 機能ＩＤ = '" + h_kinou_id + "'";
        sqlca.sql = sql;
        sqlca.restAndExecute();

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            /* 処理をNGで終了 */
            return (C_const_NG);
        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* WMバッチ処理実行管理新規作成処理 */
//            EXEC SQL INSERT INTO WMバッチ処理実行管理
//                    (シーケンス番号, 機能ＩＤ)
//            VALUES
//                    (:h_jikkouzumi_cnt, :h_kinou_id);

            StringDto WRKSQL = new StringDto();
            sprintf(WRKSQL, "INSERT INTO WMバッチ処理実行管理 (シーケンス番号, 機能ＩＤ)" +
                    " VALUES(?,?)");
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(h_jikkouzumi_cnt, h_kinou_id);

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

/******************************************************************************/
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

    /******************************************************************************/
    public int Write_ErrorList_File() {
        IntegerDto rtn_cd = new IntegerDto();                              /* 関数戻り値 */
        StringDto wk_sql = new StringDto(DEF_BUFSIZE16K);              /* 動的SQLバッファ */
        StringDto wbuf = new StringDto(512);                           /* 一時格納エリア (出力) */
        StringDto outbuf = new StringDto(DEF_BUFSIZE8K);               /* 出力する行のバッファ */
        int i_loop;                              /* パンチエラーデータ情報カウント */
        StringDto wk_buff_in = new StringDto(512);                     /* 取得文字列格納バッファ     */
        StringDto wk_buff_full = new StringDto(512);                   /* 全角変換バッファ           */
        StringDto wk_header = new StringDto(DEF_BUFSIZE8K);            /* ヘッダー変換バッファ       */
        StringDto wk_sjis_str = new StringDto(512);                    /* SJIS変換後文字列           */
        IntegerDto wk_sjis_len;                         /* SJIS変換後文字列のレングス */

        if (DBG_LOG) {
            C_DbgStart("Write_ErrorList_File処理");
        }

        /* 変数初期化 */
        memset(str_sql, 0x00, sizeof(str_sql));
        memset(wk_sql, 0x00, sizeof(wk_sql));

        /* SQL文作成 */
        sprintf(wk_sql, "SELECT "
                        + "TMパンチデータエラー情報.処理年月日, "
                        + "TMパンチデータエラー情報.データ区分, "
                        + "TMパンチデータエラー情報.依頼日, "
                        + "TMパンチデータエラー情報.バッチ番号, "
                        + "TMパンチデータエラー情報.入会年月日, "
                        + "TMパンチデータエラー情報.申込書企業コード, "
                        + "NVL(RPAD(PS店表示情報.企業名称 ,LENGTH(PS店表示情報.企業名称)), ' '), "
                        + "TMパンチデータエラー情報.申込書店番号, "
                        + "NVL(RPAD(PS店表示情報.漢字店舗名称 ,LENGTH(PS店表示情報.漢字店舗名称)), ' '), "
                        + "TMパンチデータエラー情報.会員番号, "
                        + "NVL(RPAD(TMパンチデータエラー情報.会員氏名 ,LENGTH(TMパンチデータエラー情報.会員氏名)), ' '), "
                        + "TMパンチデータエラー情報.理由コード, "
                        + "CASE TMパンチデータエラー情報.理由コード WHEN 8010 THEN '会員番号エラー' WHEN 8040 THEN '店番号エラー' WHEN 8050 THEN '退会済エラー' WHEN 8070 THEN 'MK顧客番号エラー' WHEN 8080 THEN 'MK管理会員番号エラー' ELSE NULL END "
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
                        + "AND TMパンチデータエラー情報.データ区分 = 2 ",
                bat_shori_yyyymmdd, bat_shori_yyyymmdd, bat_shori_yyyymmdd);

        strcpy(str_sql, wk_sql);

        /* 動的SQL文解析 */
//        EXEC SQL PREPARE sql_tsptmsinf FROM :str_sql;

        SqlstmDto sqlca = sqlcaManager.get("crm_cur");
        sqlca.sql = wk_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File () *** 動的SQL 解析NG = %d\n", sqlca.sqlcode);
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File () *** 動的SQL 解析NG = %s\n", str_sql);
            }

            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, str_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル宣言 */
//        EXEC SQL DECLARE crm_cur CURSOR FOR sql_tsptmsinf;
        sqlca.declare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソル宣言 crm_cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, "CURSOR ERR", 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソルをオープン */
//        EXEC SQL OPEN crm_cur;
        sqlca.open();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* エラー発生 */
            if (DBG_LOG) {
                C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソルオープン tsptmsinf_cur : sqlcode = %d\n", sqlca.sqlcode);
            }
            APLOG_WT(DEF_MSG_ID_902, 0, null, sqlca.sqlcode, "CURSOR OPEN ERR", 0, 0, 0, 0);
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
//            EXEC SQL FETCH crm_cur
//            INTO :h_syori_ymd,
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
            sqlca.recData(h_syori_ymd, h_data_kubun, h_irai_ymd, h_batch_no, h_nyukai_ymd, h_moushikomi_kigyou_cd, h_kigyou_name, h_moushikomisyoten_no, h_kanji_tenpo_name, h_kaiin_no, h_kaiin_name, h_riyuu_cd, h_error_msg);

            /* データ無し以外エラーの場合処理を異常終了 */
            if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
                /* エラー発生 */
                if (DBG_LOG) {
                    C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** カーソルFETCHエラー = %d\n", sqlca.sqlcode);
                    C_DbgEnd("Make_Coupon_CrmFile処理", C_const_NG, 0, 0);
                }
                APLOG_WT(DEF_MSG_ID_904, 0, null, "FETCH",
                        sqlca.sqlcode,
                        "TMパンチエラーデータ情報",
                        bat_shori_yyyymmdd, 0, 0);

                /* カーソルをクローズ */
//                EXEC SQL CLOSE crm_cur;
                sqlcaManager.close(sqlca);

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
                memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));

                strcpy(wk_header, C_OUT_HEADER);
                rtn_cd.arr = C_ConvUT2SJ(wk_header, strlen(wk_header), wk_sjis_str, wk_sjis_len);
                if (rtn_cd.arr != C_const_OK) {
                    APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
                    return (C_const_NG);
                }
                /* 出力エリアへ設定 */
                memcpy(outbuf, wk_sjis_str, wk_sjis_len.arr);
                /* ------------------------------------------------ */

                /* 改行コード */
                strcat(outbuf, DEF_STR3);

                /* 出力ファイル書込み */
                rtn_cd.arr = fputs(outbuf, fp_out);
                if (rtn_cd.arr == EOF) {
                    /* エラー発生 */
                    if (DBG_LOG) {
                        C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** fputs NG rtn=[%d]\n", rtn_cd);
                    }
                    sprintf(out_format_buf, "fputs（%s）", path_out);
                    APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, rtn_cd, 0, 0, 0, 0);
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
            BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd.arr = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));
            rtn_cd.arr = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
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
            BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd.arr = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));
            rtn_cd.arr = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
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
            BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd.arr = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));
            rtn_cd.arr = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
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
            BT_Rtrim(wk_buff_in, strlen(wk_buff_in));
            rtn_cd.arr = C_ConvHalf2Full(wk_buff_in, wk_buff_full);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
            /*    UTF8 → SJIS変換処理 */
            wk_sjis_len = new IntegerDto();
            memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));
            rtn_cd.arr = C_ConvUT2SJ(wk_buff_full, strlen(wk_buff_full), wk_sjis_str, wk_sjis_len);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
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
            rtn_cd.arr = fputs(outbuf, fp_out);
            if (rtn_cd.arr == EOF) {
                /* エラー発生 */
                if (DBG_LOG) {
                    C_DbgMsg("*** cmBTcentB Write_ErrorList_File() *** fputs NG rtn=[%d]\n", rtn_cd);
                }
                sprintf(out_format_buf, "fputs（%s）", path_out);
                APLOG_WT(DEF_MSG_ID_903, 0, null, out_format_buf, rtn_cd, 0, 0, 0, 0);
                return (C_const_NG);
            }
        }

        /* カーソルをクローズ */
//        EXEC SQL CLOSE crm_cur;
        sqlcaManager.close(sqlca);

        if (DBG_LOG) {
            C_DbgEnd("Write_ErrorList_File処理", 0, 0, 0);
        }

        /* 正常終了 */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： CheckMscard                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  CheckMscard(EC_RENDO_DATA in_ec_rendo,                        */
    /*                          int *punch_errno)                                 */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報チェック処理                                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int CheckMscard(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値                       */
//  int     rtn_status;         /* 関数ステータス                   */ /* 2022/10/11 MCCM初版 DEL */
//  int     corpid;             /* 会員企業コード                   */ /* 2022/10/11 MCCM初版 DEL */
        StringDto utf8str = new StringDto(4069);      /* UTF8変換後の文字列               */
        IntegerDto utf8len = new IntegerDto();            /* UTF8変換後の文字列のレングス     */
        StringDto wk_kanji_name = new StringDto(81);  /* 漢字氏名                         */
        StringDto wk_kana_name = new StringDto(81);   /* カナ氏名(全角)                   */
        StringDto wk_kana_name_han = new StringDto(80 * 3 + 1); /* カナ氏名(半角変換後)     */
        StringDto wk_kana_name_zen = new StringDto(80 * 3 + 1); /* カナ氏名(漢字氏名未設定時のカナ氏名全角変換後)*/
        StringDto wk_zip = new StringDto(7 + 1);            /* ハイフンなし郵便番号             */
        StringDto wk_address1 = new StringDto(11);    /* 住所1                            */
        StringDto wk_address2 = new StringDto(201);   /* 住所2                            */
        StringDto wk_address3 = new StringDto(81);    /* 住所3                            */
        StringDto wk_tenpo = new StringDto(6 + 1);    /* 入力データ.入会店番号 数値化ＷＫ */
        StringDto wk_sjis_str = new StringDto(512);                    /* SJIS変換後文字列           */
        IntegerDto wk_sjis_len = new IntegerDto();                         /* SJIS変換後文字列のレングス */

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
//    memset(h_address3, 0x00, sizeof(h_address3));                    /* 2023/01/23 MCCM初版 DEL */
        memset(wk_kanji_name, 0x00, sizeof(wk_kanji_name));
        memset(wk_kana_name, 0x00, sizeof(wk_kana_name));
        memset(wk_kana_name_han, 0x00, sizeof(wk_kana_name_han));
        memset(wk_kana_name_zen, 0x00, sizeof(wk_kana_name_zen));
        memset(wk_zip, 0x00, sizeof(wk_zip));
        memset(wk_address1, 0x00, sizeof(wk_address1));
        memset(wk_address2, 0x00, sizeof(wk_address2));
        memset(wk_address3, 0x00, sizeof(wk_address3));
        rtn_cd.arr = C_const_OK;
//  rtn_status = C_const_Stat_OK;                                      /* 2022/10/11 MCCM初版 DEL */
//  corpid = 0;                                                        /* 2022/10/11 MCCM初版 DEL */
        h_cdriyu_cd.arr = 0;
        h_hakko_ymd.arr = 0;
        h_ps_oldcorp.arr = 0;
        h_service_shubetsu.arr = 0;
        memset(h_cdkaiin_no, 0x00, sizeof(h_cdkaiin_no));
        h_kigyo_cd.arr = 0;
        card_flag = 0;
        point_kaiin_flag = 0;
        memset(h_apkaiin_no, 0x00, sizeof(h_apkaiin_no));
        ap_kaiin_flag = 0;
        up_maternity_circle_flag = 0;
        h_renkei_mise_no.arr = 0;                                               /* 2022/10/11 MCCM初版 ADD*/

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        /* ＨＯＳＴ変数にセット */
        /* 会員番号             */
        strncpy(h_pid, in_ec_rendo.kaiin_no, strlen(in_ec_rendo.kaiin_no));
        if (DBG_LOG) {
            C_DbgMsg("*** CheckMscard *** 会員番号=[%s] \n", h_pid);
        }
        /* 漢字氏名 */
        /* UTF8に変換 */
        strncpy(wk_kanji_name, in_ec_rendo.kanji_name.strVal(), strlen(in_ec_rendo.kanji_name));
        rtn_cd.arr = C_ConvSJ2UT(wk_kanji_name, strlen(wk_kanji_name)
                , utf8str, utf8len);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(漢字氏名)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        BT_Rtrim(utf8str, utf8len.arr);
        strcpy(h_kaiin_name, utf8str);
        if (DBG_LOG) {
            C_DbgMsg("漢字氏名 =[%s]\n", h_kaiin_name);
        }

        /* 初期化 */
        memset(utf8str, 0x00, sizeof(utf8str));

        /* カナ氏名 */
        /* UTF8に変換 */
        strncpy(wk_kana_name, in_ec_rendo.kana_name.strVal(), strlen(in_ec_rendo.kana_name));
        rtn_cd.arr = C_ConvSJ2UT(wk_kana_name, strlen(wk_kana_name)
                , utf8str, utf8len);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(カナ氏名)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        BT_Rtrim(utf8str, utf8len.arr);

        /* 全角半角変換 */
        rtn_cd.arr = C_ConvFull2Half(utf8str, wk_kana_name_han);
        if (rtn_cd.arr == C_const_NG) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** 全角半角変換NG %s\n", "");
            }
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* ------------------------------------------------ */
        /*    UTF8 → SJIS変換処理 */
        wk_sjis_len.arr = 0;
        memset(wk_sjis_str, 0x00, sizeof(wk_sjis_str));

        rtn_cd.arr = C_ConvUT2SJ(wk_kana_name_han, strlen(wk_kana_name_han), wk_sjis_str, wk_sjis_len);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT(DEF_MSG_ID_903, 0, null, "C_ConvUT2SJ", rtn_cd, 0, 0, 0, 0);
            return (C_const_NG);
        }
        /* ------------------------------------------------ */

        /* 40が最大文字数 */
        if (wk_sjis_len.arr > DEF_BUFSIZE40) {
            /* 初期化 */
            memset(utf8str, 0x00, sizeof(utf8str));
            memset(wk_kana_name, 0x00, sizeof(wk_kana_name));
            utf8len.arr = 0;

            /* 40バイトで切り再度UTF8に変換 */
            strncpy(wk_kana_name, wk_sjis_str, DEF_BUFSIZE40);

            rtn_cd.arr = C_ConvSJ2UT(wk_kana_name, strlen(wk_kana_name)
                    , utf8str, utf8len);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** UTF8変換(カナ氏名)NG %s\n", "");
                }
                APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }
            strncpy(h_kana_name.strDto(), utf8str, utf8len.arr);
        } else {
            strcpy(h_kana_name, wk_kana_name_han);
        }

        if (DBG_LOG) {
            C_DbgMsg("カナ氏名 =[%s]\n", h_kana_name);
        }

        /* 郵便番号 （ハイフンを削除） */
        if (strlen(in_ec_rendo.zip) != 0) {
            strncpy(wk_zip, in_ec_rendo.zip.strVal(), strlen(in_ec_rendo.zip));
            rtn_cd.arr = C_ConvTelNo(wk_zip, strlen(wk_zip), h_zip.strDto());
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------------*/
                    C_DbgMsg("*** CheckMscard *** 郵便番号変換(郵便番号)NG %s\n", "");
                    /*---------------------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_ConvTelNo", rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            /* スペース削除 */
            BT_Rtrim(h_zip.strDto(), strlen(h_zip));

            if (strlen(h_zip) != 7) {
                memset(h_zip, 0x00, sizeof(h_zip));
            }
        }

        if (DBG_LOG) {
            C_DbgMsg("郵便番号 =[%s]\n", h_zip);
        }

        /* 初期化 */
        memset(utf8str, 0x00, sizeof(utf8str));

        /* 住所1 */
        /* UTF8に変換 */
        strncpy(wk_address1, in_ec_rendo.address1.strVal(), strlen(in_ec_rendo.address1));
        rtn_cd.arr = C_ConvSJ2UT(wk_address1, strlen(wk_address1), utf8str, utf8len);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(住所1)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* スペース削除 */
        BT_Rtrim(utf8str, utf8len.arr);
        strncpy(h_address1.strDto(), utf8str, utf8len.arr);

        if (DBG_LOG) {
            C_DbgMsg("住所１ =[%s]\n", h_address1);
        }

        /* 初期化 */
        memset(utf8str, 0x00, sizeof(utf8str));

        /* 住所2 */
        /* UTF8に変換 */
        strncpy(wk_address2, in_ec_rendo.address2.strVal(), strlen(in_ec_rendo.address2));
        rtn_cd.arr = C_ConvSJ2UT(wk_address2, strlen(wk_address2), utf8str, utf8len);
        if (rtn_cd.arr != C_const_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** UTF8変換(住所2)NG %s\n", "");
            }
            APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }
        /* スペース削除 */
        BT_Rtrim(utf8str, utf8len.arr);
        strcpy(h_address2, utf8str);

        if (DBG_LOG) {
            C_DbgMsg("住所２=[%s]\n", h_address2);
        }

        /* 初期化 */
        memset(utf8str, 0x00, sizeof(utf8str));

        /* 20230123 DEL MCCM */
//    /* 住所3 */
//    /* UTF8に変換 */
//    strncpy(wk_address3, in_ec_rendo.address3, strlen(in_ec_rendo.address3));
//    rtn_cd.arr = C_ConvSJ2UT(wk_address3, strlen(wk_address3) ,utf8str, utf8len);
//    if(rtn_cd.arr != C_const_OK ){
//if( DBG_LOG){
//        C_DbgMsg( "*** CheckMscard *** UTF8変換(住所3)NG %s\n" , "" );
//}
//        APLOG_WT( "903", 0, null, "C_ConvSJ2UT", rtn_cd ,
//                                                                   0 ,0 ,0 ,0);
//
//        /* 処理を終了する */
//        return(C_const_NG);
//    }
//    /* スペース削除 */
//    BT_Rtrim( utf8str, utf8len );
//    strcpy(h_address3, utf8str);
//
//if( DBG_LOG){
//    C_DbgMsg("住所３ =[%s]\n", h_address3);
//}
        /* 20230123 DEL MCCM */

        /* 会員企業コード取得 */
        strncpy(wk_tenpo, in_ec_rendo.tenpo_no.strVal(), strlen(in_ec_rendo.tenpo_no));
        h_tenpo_no.arr = atoi(wk_tenpo);
        h_moushikomiten_no.arr = atoi(wk_tenpo);

        if (h_tenpo_no.intVal() != 0) {
//            EXEC SQL SELECT 連携用店番号                                                                           /* 2022/10/12 MCCM初版 MOD */
//            INTO :h_renkei_mise_no                                                                      /* 2022/10/12 MCCM初版 MOD */
//            FROM PS店表示情報@CMSD                                                                      /* 2022/10/12 MCCM初版 MOD */
//            WHERE 店番号       =  :h_tenpo_no
//            AND 開始年月日   <= :h_kijyun_yyyymmdd
//            AND 終了年月日   >= :h_kijyun_yyyymmdd;

            sqlca.sql = new StringDto("SELECT 連携用店番号   FROM PS店表示情報 WHERE 店番号       =  ? AND 開始年月日   <= ? AND 終了年月日   >= ?");
            sqlca.restAndExecute(h_tenpo_no, h_kijyun_yyyymmdd, h_kijyun_yyyymmdd);
            sqlca.fetch();
            sqlca.recData(h_renkei_mise_no);

            /* ＳＱＬを実行結果を判定する */
            if (sqlca.sqlcode == C_const_Ora_OK) {
            } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) { /* データ無し */
                if (DBG_LOG) {
                    sprintf(out_format_buf, "店番号=[%d]、基準日=[%d]", h_tenpo_no, h_kijyun_yyyymmdd);
                    C_DbgMsg("*** CheckMscard *** 店情報取得NG 8040(店番号エラー)[%s]\n", out_format_buf);
                }
                /* 入会店は 0 として登録 */
                h_tenpo_no.arr = 0;
            } else { /* DBERR */
                sprintf(out_format_buf, "店番号=[%d]、基準日=[%d]", h_tenpo_no, h_kijyun_yyyymmdd);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "PS店表示情報", out_format_buf, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
        }

        /* MSカード情報検索処理 */
//        EXEC SQL SELECT サービス種別,
//            会員番号,
//        to_char(NVL(顧客番号,0), 'FM000000000000000' ),
//                NVL(企業コード,0),
//                NVL(旧販社コード,0),
//                理由コード,
//                NVL(発行年月日,0),
//                NVL(ＧＯＯＰＯＮ番号,0)                                                                    /* 2022/10/12 MCCM初版 ADD */
//        INTO :h_service_shubetsu,
//                    :h_cdkaiin_no,
//                    :h_uid,
//                    :h_kigyo_cd,
//                    :h_kyu_hansha_cd,
//                    :h_cdriyu_cd,
//                    :h_hakko_ymd,
//                    :h_goopon_no                                                                               /* 2022/10/12 MCCM初版 ADD */
//        FROM MSカード情報@CMSD
//        WHERE 会員番号     = :h_pid
//        AND サービス種別 = 2;

        sqlca.sql = new StringDto("SELECT サービス種別,会員番号," +
                " to_char(NVL(顧客番号,0), 'FM000000000000000' )," +
                " NVL(企業コード,0)," +
                " NVL(旧販社コード,0)," +
                " 理由コード," +
                " NVL(発行年月日,0)," +
                " NVL(ＧＯＯＰＯＮ番号,0)" +
                " FROM MSカード情報" +
                " WHERE 会員番号     = ?" +
                " AND サービス種別 = 2");
        sqlca.restAndExecute(h_pid);
        sqlca.fetch();
        sqlca.recData(h_service_shubetsu, h_cdkaiin_no, h_uid, h_kigyo_cd, h_kyu_hansha_cd, h_cdriyu_cd, h_hakko_ymd, h_goopon_no);

        /* データ無しエラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("*** CheckMscard *** MSカード情報検索NG %s\n", "");
            }
            /* DBERR */
            sprintf(out_format_buf, "会員番号=[%s]", h_pid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
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
        } else {
            card_flag = 0;
        }

        if (DBG_LOG) {
            C_DbgMsg("*** CheckMscard *** MSカード情報 card_flag=[%d]\n", card_flag);
        }

        /* データ無しまたは顧客番号=0またはＧＯＯＰＯＮ番号=0の場合 */                                             /* 2022/10/12 MCCM初版 MOD */
        if (card_flag == 0 || atof(h_uid) == 0 || atof(h_goopon_no) == 0) {                                       /* 2022/10/12 MCCM初版 MOD */
            if (card_flag == 0 || atof(h_uid) == 0) {                                                               /* 2022/10/12 MCCM初版 ADD */
                /* 顧客番号にSQ顧客番号発番をセット */                                                                 /* 2022/10/12 MCCM初版 ADD */
//                EXEC SQL SELECT SQ顧客番号発番.NEXTVAL@CMSD                                                            /* 2022/10/12 MCCM初版 ADD */
//                        INTO :h_uid                                             /* 顧客番号 */                      /* 2022/10/12 MCCM初版 ADD */
//                FROM DUAL@CMSD;                                                                             /* 2022/10/12 MCCM初版 ADD */

                sqlca.sql = new StringDto("SELECT SQ顧客番号発番 FROM cmsho_sq顧客番号発番");
                sqlca.restAndExecute();
                sqlca.fetch();
                sqlca.recData(h_uid);
            }                                                                                                    /* 2022/10/12 MCCM初版 ADD */

            if (card_flag == 0 || atof(h_goopon_no) == 0) {                                                           /* 2022/10/12 MCCM初版 ADD */
                /* ＧＯＯＰＯＮ番号をセット */                                                                         /* 2022/10/13 MCCM初版 ADD */
                sprintf(h_goopon_no, "%s%s", "8", h_uid);                  /* ＧＯＯＰＯＮ番号 */                      /* 2023/01/05 MCCM初版 MOD */
            }                                                                                                      /* 2022/10/12 MCCM初版 ADD */
        }                                                                                                          /* 2022/10/12 MCCM初版 ADD */
        /* 顧客番号が紐づいている場合はポイント会員と統合しているかをチェック */
        else {
            /* MSカード情報検索処理 */
//            EXEC SQL SELECT 会員番号
//            FROM MSカード情報@CMSD
//            WHERE 顧客番号     = :h_uid
//            AND サービス種別 IN (1,3)
//            AND カードステータス IN (0, 7)
//            AND ROWNUM = 1;

            sqlca.sql = new StringDto("SELECT 会員番号" +
                    " FROM MSカード情報" +
                    " WHERE 顧客番号     = ?" +
                    " AND サービス種別 IN (1,3)" +
                    " AND カードステータス IN (0, 7)" +
                    " LIMIT 1");
            sqlca.restAndExecute(h_uid);
            sqlca.fetch();
            sqlca.recData();

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** MSカード情報検索２NG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "MSカード情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (sqlca.sqlcode != C_const_Ora_NOTFOUND) { /* データ有 */
                /* ポイント会員フラグを立てる */
                point_kaiin_flag = 1;
            }

            /* 顧客番号が紐づいている場合は退会済みではないＡＰ会員と統合しているかをチェック */
            /* MSカード情報検索処理 */
//            EXEC SQL SELECT T.会員番号
//            INTO :h_apkaiin_no
//            FROM (
//                    SELECT 会員番号
//                    FROM MSカード情報@CMSD
//            WHERE 顧客番号         = :h_uid
//            AND サービス種別     = 3
//            AND カードステータス = 1                                                            /* 2022/10/12 MCCM初版 MOD */
//            ORDER BY 発行年月日 DESC
//                   ) T
//            WHERE ROWNUM = 1;

            sqlca.sql = new StringDto("SELECT T.会員番号" +
                    " FROM (" +
                    "         SELECT 会員番号" +
                    "         FROM MSカード情報" +
                    " WHERE 顧客番号         = ?" +
                    " AND サービス種別     = 3" +
                    " AND カードステータス = 1 " +
                    " ORDER BY 発行年月日 DESC" +
                    " LIMIT 1) T" +
                    " ");
            sqlca.restAndExecute(h_uid);
            sqlca.fetch();
            sqlca.recData(h_apkaiin_no);

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** MSカード情報検索３NG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]サービス種別=[3]カードステータス=[0]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "MSカード情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (sqlca.sqlcode != C_const_Ora_NOTFOUND) { /* データ有 */
                /* ＡＰ会員フラグを立てる */
                ap_kaiin_flag = 1;
            }

            /* 顧客番号が紐づいている場合は退会済みではないＡＰ会員と統合しているかをチェック */
            /* MSカード情報検索処理 */
//            EXEC SQL SELECT 顧客番号
//            FROM MSカード情報@CMSD
//            WHERE 顧客番号     = :h_uid
//            AND サービス種別 = 1
//            AND カードステータス IN (0, 7)
//            AND ROWNUM = 1;

            sqlca.sql = new StringDto("SELECT 顧客番号" +
                    " FROM MSカード情報" +
                    " WHERE 顧客番号     = ?" +
                    " AND サービス種別 = 1" +
                    " AND カードステータス IN (0, 7)" +
                    " LIMIT 1");
            sqlca.restAndExecute(h_uid);
            sqlca.fetch();
            sqlca.recData();

            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK
                    && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** MSカード情報検索４%s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]サービス種別=[1]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
                        "MSカード情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (sqlca.sqlcode == C_const_Ora_NOTFOUND) { /* データ有 */
                /* ＡＰ会員フラグを立てる */
                up_maternity_circle_flag = 1;
            }


        }

        if (DBG_LOG) {
            C_DbgEnd("カード情報チェック処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateCard                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateCard(EC_RENDO_DATA in_ec_rendo,                         */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報更新処理                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateCard(EC_RENDO_DATA in_ec_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("カード情報更新処理");
            C_DbgMsg("会員番号          =[%s]\n", h_pid);
            C_DbgMsg("顧客番号          =[%s]\n", h_uid);
        }

        /*--------------------*/
        /* ＨＯＳＴ変数セット */
        /*--------------------*/
        /* 初期化 */
//        memset(&MscdData, 0x00, sizeof(MS_CARD_INFO_TBL));
        MscdData = new MS_CARD_INFO_TBL();
        memset(MscdData, 0x00, 0);
        /* 会員番号         */
        memcpy(MscdData.kaiin_no, h_pid, sizeof(h_pid) - 1);
        MscdData.kaiin_no.len = strlen(h_pid);
        /* 顧客番号         */
        memcpy(MscdData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
        MscdData.kokyaku_no.len = strlen(h_uid);
        /* 2022/10/12 MCCM初版 ADD START */
        /* ＧＯＯＰＯＮ番号 */
        memcpy(MscdData.goopon_no, h_goopon_no, sizeof(h_goopon_no) - 1);
        MscdData.goopon_no.len = strlen(h_goopon_no);
        /* 2022/10/12 MCCM初版 ADD END */

        /* カードステータス */
        h_card_status.arr = 1;                                                                                         /* 2022/10/12 MCCM初版 MOD */

        /* 理由コード */
        if (h_cdriyu_cd.intVal() == 0) {
            h_cdriyu_cd.arr = 2000;
        }

        /* カード発行年月日 */
        if (h_hakko_ymd.intVal() == 0 || h_kijyun_yyyymmdd.intVal() < h_hakko_ymd.intVal()) { /* 基準日を設定 */
            h_hakko_ymd = h_kijyun_yyyymmdd;
        }

        /* カードマスタあり */
        if (card_flag == 1) {
            if (h_kigyo_cd.intVal() == 0) {
                h_kigyo_cd = h_ps_corpid;
            }
            /* MSカード情報更新処理 */
//            EXEC SQL UPDATE MSカード情報@CMSD
//            SET 顧客番号       = :MscdData.kokyaku_no,
//                    ＧＯＯＰＯＮ番号 = :MscdData.goopon_no,                                                     /* 2022/10/12 MCCM初版 ADD */
//                    理由コード     = :h_cdriyu_cd,
//                    発行年月日     = :h_hakko_ymd,
//                    企業コード     = DECODE(NVL(企業コード,0),0,:h_ps_corpid,企業コード),
//            旧販社コード   = DECODE(NVL(旧販社コード,0),0,:h_ps_kyu_hansha_cd,旧販社コード),          /* 2022/10/12 MCCM初版 MOD */
//            バッチ更新日   = :h_bat_yyyymmdd,
//                    最終更新日     = :h_bat_yyyymmdd,
//                    最終更新日時   = sysdate,
//                    最終更新プログラムＩＤ = :h_programid
//            WHERE 会員番号       = :h_pid
//            AND サービス種別 = 2;

            StringDto sql = new StringDto();
            sql.arr = "UPDATE MSカード情報" +
                    " SET 顧客番号       = ?," +
                    "         ＧＯＯＰＯＮ番号 = ?, " +
                    "         理由コード     = ?," +
                    "         発行年月日     = ?," +
                    "         企業コード     = DECODE(NVL(企業コード,0),'0',?,企業コード)," +
                    " 旧販社コード   = DECODE(NVL(旧販社コード,0),'0',?,旧販社コード),       " +
                    " バッチ更新日   = ?," +
                    "         最終更新日     = ?," +
                    "         最終更新日時   = sysdate()," +
                    "         最終更新プログラムＩＤ = ?" +
                    " WHERE 会員番号       = ?" +
                    " AND サービス種別 = 2";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.restAndExecute(MscdData.kokyaku_no, MscdData.goopon_no, h_cdriyu_cd, h_hakko_ymd, h_ps_corpid, h_ps_kyu_hansha_cd, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_pid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCard *** MSカード情報UPDNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE",
                        sqlca.sqlcode, "MSカード情報",
                        out_format_buf, 0, 0);

                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_NG);
            }
            /* カードマスタなし */
        } else {
            /* 企業コード設定 */
            h_kigyo_cd = h_ps_corpid;
            /* MSカード情報更新処理 */
//            EXEC SQL INSERT INTO MSカード情報@CMSD
//                    (サービス種別,
//                            会員番号,
//                            顧客番号,
//                            ＧＯＯＰＯＮ番号,                                                             /* 2022/10/12 MCCM初版 ADD */
//                            カードステータス,
//                            理由コード,
//                            発行年月日,
//                            終了年月日,
//                            有効期限,
//                            企業コード,
//                            旧販社コード,
//                            作業企業コード,
//                            作業者ＩＤ,
//                            作業年月日,
//                            作業時刻,
//                            バッチ更新日,
//                            最終更新日,
//                            最終更新日時,
//                            最終更新プログラムＩＤ)
//                    VALUES
//            (2,
//                                 :MscdData.kaiin_no,
//                                 :MscdData.kokyaku_no,
//                                 :MscdData.goopon_no,                                                          /* 2022/10/12 MCCM初版 ADD */
//                                 :h_card_status,
//                                 :h_cdriyu_cd,
//                                 :h_hakko_ymd,
//                    0,
//                    0,
//                                 :h_ps_corpid,
//                                 :h_ps_kyu_hansha_cd,                                                          /* 2022/10/12 MCCM初版 MOD */
//                    0,
//                    0,
//                    0,
//                    0,
//                                 :h_bat_yyyymmdd,
//                                 :h_bat_yyyymmdd,
//                    sysdate,
//                                 :h_programid);

            StringDto WRKSQL = new StringDto();
            WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MSカード情報" +
                    " (サービス種別," +
                    " 会員番号," +
                    " 顧客番号," +
                    " ＧＯＯＰＯＮ番号," +
                    " カードステータス," +
                    " 理由コード," +
                    " 発行年月日," +
                    " 終了年月日," +
                    " 有効期限," +
                    " 企業コード," +
                    " 旧販社コード," +
                    " 作業企業コード," +
                    " 作業者ＩＤ," +
                    " 作業年月日," +
                    " 作業時刻," +
                    " バッチ更新日," +
                    " 最終更新日," +
                    " 最終更新日時," +
                    " 最終更新プログラムＩＤ)" +
                    " VALUES" +
                    " (2," +
                    " ?," +
                    " ?," +
                    " ?," +
                    " ?," +
                    " ?," +
                    " ?," +
                    " 0," +
                    " 0," +
                    " ?," +
                    " ?," +
                    " 0," +
                    " 0," +
                    " 0," +
                    " 0," +
                    " ?," +
                    " ?," +
                    " sysdate()," +
                    " ?))");
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(MscdData.kaiin_no,
                    MscdData.kokyaku_no,
                    MscdData.goopon_no,
                    h_card_status,
                    h_cdriyu_cd,
                    h_hakko_ymd,
                    h_ps_corpid,
                    h_ps_kyu_hansha_cd,
                    h_bat_yyyymmdd,
                    h_bat_yyyymmdd,
                    h_programid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCard *** MSカード情報INSNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT",
                        sqlca.sqlcode, "MSカード情報",
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateCardForTaikai                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateCardForTaikai(EC_RENDO_DATA in_ec_rendo,                */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               カード情報更新処理                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateCardForTaikai(EC_RENDO_DATA in_ec_rendo, int punch_errno) {

        if (DBG_LOG) {
            C_DbgStart("カード情報更新（退会）処理");
            C_DbgMsg("会員番号          =[%s]\n", h_pid);
            C_DbgMsg("顧客番号          =[%s]\n", h_uid);
        }

        /*--------------------*/
        /* ＨＯＳＴ変数セット */
        /*--------------------*/
        /* 初期化 */
//        memset(&MscdData, 0x00, sizeof(MS_CARD_INFO_TBL));
        MscdData = new MS_CARD_INFO_TBL();
        /* 会員番号         */
        memcpy(MscdData.kaiin_no.strDto(), h_pid, sizeof(h_pid) - 1);
        MscdData.kaiin_no.len = strlen(h_pid);
        /* 顧客番号         */
        memcpy(MscdData.kokyaku_no.strDto(), h_uid, sizeof(h_uid) - 1);
        MscdData.kokyaku_no.len = strlen(h_uid);

        /* カードステータス */
        h_card_status.arr = 8;
        /* 理由コード */
        h_cdriyu_cd.arr = 2011;
        /* カード発行年月日 */
        h_hakko_ymd.arr = 0;

        /* カードマスタあり */
        if (card_flag == 1) {
            if (h_kigyo_cd.intVal() == 0) {
                h_kigyo_cd = h_ps_corpid;
            }
            /* MSカード情報更新処理 */
            /* 2016/08/17 発行年月日はクリアしない        */
            /*             発行年月日     = :h_hakko_ymd, */
//            EXEC SQL UPDATE MSカード情報@CMSD
//            SET カードステータス = :h_card_status,
//                    理由コード     = :h_cdriyu_cd,
//                    終了年月日     = :h_taikai_date,
//                    企業コード     = DECODE(NVL(企業コード,0),0,:h_ps_corpid,企業コード),
//            旧販社コード   = DECODE(NVL(旧販社コード,0),0,:h_ps_oldcorp,旧販社コード),
//            バッチ更新日   = :h_bat_yyyymmdd,
//                    最終更新日     = :h_bat_yyyymmdd,
//                    最終更新日時   = sysdate,
//                    最終更新プログラムＩＤ = :h_programid
//            WHERE 会員番号     = :h_pid
//            AND サービス種別 = 2;

            StringDto sql = new StringDto();
            sql.arr = "UPDATE MSカード情報" +
                    " SET   カードステータス = ?," +
                    "         理由コード     = ?," +
                    "         終了年月日     = ?," +
                    "         企業コード     = DECODE(NVL(企業コード,0),'0',?,企業コード)," +
                    " 旧販社コード   = DECODE(NVL(旧販社コード,0),'0',?,旧販社コード)," +
                    " バッチ更新日   = ?," +
                    "         最終更新日     = ?," +
                    "         最終更新日時   = sysdate()," +
                    "         最終更新プログラムＩＤ = ?" +
                    " WHERE 会員番号     = ?" +
                    " AND サービス種別 = 2";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.restAndExecute(h_card_status, h_cdriyu_cd, h_taikai_date, h_ps_corpid, h_ps_oldcorp, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_pid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCardForTaikai *** MSカード情報UPDNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE",
                        sqlca.sqlcode, "MSカード情報",
                        out_format_buf, 0, 0);

                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_NG);
            }


            if (ap_kaiin_flag == 1) {
                /* MSカード情報更新処理(AP会員) */
                /* 2016/08/17 発行年月日はクリアしない        */
                /*             発行年月日     = :h_hakko_ymd, */
//                EXEC SQL UPDATE MSカード情報@CMSD
//                SET カードステータス = :h_card_status,
//                        理由コード     = :h_cdriyu_cd,
//                        終了年月日     = :h_taikai_date,
//                        バッチ更新日   = :h_bat_yyyymmdd,
//                        最終更新日     = :h_bat_yyyymmdd,
//                        最終更新日時   = sysdate,
//                        最終更新プログラムＩＤ = :h_programid
//                WHERE 会員番号     = :h_apkaiin_no
//                AND サービス種別 = 3;

                sql = new StringDto();
                sql.arr = "UPDATE MSカード情報" +
                        " SET   カードステータス = ?" +
                        "         理由コード     = ?" +
                        "         終了年月日     = ?" +
                        "         バッチ更新日   = ?" +
                        "         最終更新日     = ?" +
                        "         最終更新日時   = sysdate()," +
                        "         最終更新プログラムＩＤ = ?" +
                        " WHERE 会員番号     = ?" +
                        " AND サービス種別 = 3";
                sqlca.sql = sql;
                sqlca.prepare();
                sqlca.restAndExecute(h_card_status, h_cdriyu_cd, h_taikai_date, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_apkaiin_no);

                /* エラーの場合処理を異常終了する */
                if (sqlca.sqlcode != C_const_Ora_OK) {
                    if (DBG_LOG) {
                        C_DbgMsg("*** UpdateCardForTaikai *** MSカード情報(AP)UPDNG %s\n", "");
                    }
                    /* DBERR */
                    sprintf(out_format_buf, "会員番号=[%s]サービス種別=[3]", h_apkaiin_no);
                    APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE",
                            sqlca.sqlcode, "MSカード情報",
                            out_format_buf, 0, 0);

                    /* パンチエラー番号セット */
                    punch_errno = C_PUNCH_OK;

                    /* 処理を終了する */
                    return (C_const_NG);
                }
            }

            /* カードマスタなし */
        } else {
            /* 企業コード設定 */
            h_kigyo_cd = h_ps_corpid;
            /* MSカード情報更新処理 */
//            EXEC SQL INSERT INTO MSカード情報@CMSD
//                    (サービス種別,
//                            会員番号,
//                            顧客番号,
//                            ＧＯＯＰＯＮ番号,                                                             /* 2022/10/12 MCCM初版 ADD */
//                            カードステータス,
//                            理由コード,
//                            発行年月日,
//                            終了年月日,
//                            有効期限,
//                            企業コード,
//                            旧販社コード,
//                            作業企業コード,
//                            作業者ＩＤ,
//                            作業年月日,
//                            作業時刻,
//                            バッチ更新日,
//                            最終更新日,
//                            最終更新日時,
//                            最終更新プログラムＩＤ)
//                    VALUES
//            (2,
//                                 :MscdData.kaiin_no,
//                                 :MscdData.kokyaku_no,
//                                 :MscdData.goopon_no,                                                          /* 2022/10/12 MCCM初版 ADD */
//                                 :h_card_status,
//                                 :h_cdriyu_cd,
//                                 :h_hakko_ymd,
//                                 :h_taikai_date,
//                    0,
//                                 :h_ps_corpid,
//                                 :h_ps_oldcorp,
//                    0,
//                    0,
//                    0,
//                    0,
//                                 :h_bat_yyyymmdd,
//                                 :h_bat_yyyymmdd,
//                    sysdate,
//                                 :h_programid);

            StringDto WRKSQL = new StringDto();
            WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MSカード情報" +
                    " (サービス種別, 会員番号, 顧客番号, ＧＯＯＰＯＮ番号, カードステータス, 理由コード, 発行年月日, 終了年月日, 有効期限, 企業コード, 旧販社コード, 作業企業コード, 作業者ＩＤ, 作業年月日, 作業時刻, バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ)" +
                    " VALUES (2, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0, 0, 0, 0, ?, ?, sysdate(), ?)");
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(MscdData.kaiin_no, MscdData.kokyaku_no, MscdData.goopon_no, h_card_status, h_cdriyu_cd, h_hakko_ymd, h_taikai_date, h_ps_corpid, h_ps_oldcorp, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** UpdateCardForTaikai *** MSカード情報INSNG %s\n", "");
                }
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT",
                        sqlca.sqlcode, "MSカード情報",
                        out_format_buf, 0, 0);

                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return (C_const_NG);
            }
        }

        if (DBG_LOG) {
            C_DbgEnd("カード情報更新（退会）処理", 0, 0, 0);
        }
        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        /* 処理を終了する */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： SeitaiCheck                                                     */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  SeitaiCheck(EC_RENDO_DATA in_ec_rendo,                        */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               静態情報更新チェック処理                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int SeitaiCheck(EC_RENDO_DATA in_ec_rendo, int punch_errno) {

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
        memcpy(h_uid_varchar, h_uid, sizeof(h_uid) - 1);
        h_uid_varchar.len = strlen(h_uid);

        h_seitai_flag.arr = 0;
        h_birth_year.arr = 0;
        h_birth_month.arr = 0;
        h_birth_day.arr = 0;
        h_age.arr = 0;
        h_zaiseki_yyyymm.arr = 0;
        h_tel_flag.arr = 0;

        seitai_flg.arr = 0;
        hi_msks_sw.arr = 0;        /* MS顧客制度情報「なし(0)／あり(1)」 */
        hi_mmki_sw.arr = 0;        /* MM顧客情報    「なし(0)／あり(1)」 */

        /* 2022/10/12 MCCM初版 ADD START */
        h_mcc_seido_kyodaku_flg.arr = 0;
        h_mcc_seido_kyodaku_koshinsha.arr = 0;
        h_mcc_seido_kyodaku_koshin_ymdhms.arr = 0;
        /* 2022/10/12 MCCM初版 ADD END */

        /* MS顧客制度情報取得処理 */
//        EXEC SQL SELECT
//        NVL(MSKS.静態取込済みフラグ, 0),
//                NVL(MSKS.誕生月, 0),
//                NVL(MSKS.在籍開始年月, 0),
//                NVL(MSKS.電話番号登録フラグ, 0),
//                NVL(ＭＣＣ制度許諾フラグ, 0),                                                              /* 2022/10/13 MCCM初版 ADD */
//                NVL(ＭＣＣ制度許諾更新者, 0),                                                              /* 2022/10/13 MCCM初版 ADD */
//                NVL( TO_NUMBER( TO_CHAR( ＭＣＣ制度許諾更新日時, 'YYYYMMDDHHMMSS' ) ), 0 )                 /* 2022/10/13 MCCM初版 ADD */
//        INTO
//        :h_seitai_flag,
//                    :h_birth_month,
//                    :h_zaiseki_yyyymm,
//                    :h_tel_flag,
//                    :h_mcc_seido_kyodaku_flg,                                                                  /* 2022/10/12 MCCM初版 ADD */
//                    :h_mcc_seido_kyodaku_koshinsha,                                                            /* 2022/10/12 MCCM初版 ADD */
//                    :h_mcc_seido_kyodaku_koshin_ymdhms                                                         /* 2022/10/12 MCCM初版 ADD */
//        FROM MS顧客制度情報@CMSD MSKS
//        WHERE MSKS.顧客番号 = :h_uid_varchar;

        sqlca.sql = new StringDto("SELECT NVL(MSKS.静態取込済みフラグ, 0)," +
                " NVL(MSKS.誕生月, 0)," +
                " NVL(MSKS.在籍開始年月, 0)," +
                " NVL(MSKS.電話番号登録フラグ, 0)," +
                " NVL(ＭＣＣ制度許諾フラグ, 0), " +
                " NVL(ＭＣＣ制度許諾更新者, 0), " +
                " NVL( TO_NUMBER( TO_CHAR( ＭＣＣ制度許諾更新日時, 'YYYYMMDDHHMMSS' ) ), 0 )  " +
                " FROM MS顧客制度情報 MSKS" +
                " WHERE MSKS.顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_seitai_flag, h_birth_month, h_zaiseki_yyyymm, h_tel_flag, h_mcc_seido_kyodaku_flg, h_mcc_seido_kyodaku_koshinsha, h_mcc_seido_kyodaku_koshin_ymdhms);

        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            /* データ無しエラー以外のエラーの場合処理を異常終了する */
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** MS顧客制度情報@CMSD NG %s\n", "");
            }
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode, "MS顧客制度情報",
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
        h_end_seitai_update_hh24miss.arr = 0;
        h_end_seitai_update_date.arr = 0;
        h_bath_yyyy.arr = 0;
        h_bath_mm.arr = 0;
        h_seibetsu.arr = 0;
        memset(h_db_name, 0x00, sizeof(h_db_name));
        memset(h_db_kana_name, 0x00, sizeof(h_db_kana_name));
        memset(h_db_zip, 0x00, sizeof(h_db_zip));
        memset(h_db_address1, 0x00, sizeof(h_db_address1));
        memset(h_db_address2, 0x00, sizeof(h_db_address2));

        /* MM顧客情報検索処理 */
//        EXEC SQL SELECT
//        NVL(MMKI.最終静態更新日, 0),
//                NVL(MMKI.最終静態更新時刻, 0),
//                NVL(MMKI.誕生年, 0),
//                NVL(MMKI.誕生月, 0),
//                NVL(MMKI.性別, 0),
//                NVL(MMKI.顧客名称, ' '),
//                NVL(MMKI.顧客カナ名称, ' '),
//                NVL(MMKZ.郵便番号, ' '),
//                NVL(MMKZ.住所１, ' '),
//                NVL(MMKZ.住所２, ' ')
//        INTO
//        :h_end_seitai_update_yyyymmdd,
//            :h_end_seitai_update_hh24miss,
//            :h_bath_yyyy,
//            :h_bath_mm,
//            :h_seibetsu,
//            :h_db_name,
//            :h_db_kana_name,
//            :h_db_zip,
//            :h_db_address1,
//            :h_db_address2
//        FROM MM顧客情報 MMKI,
//            MM顧客属性情報 MMKZ
//        WHERE MMKI.顧客番号 = MMKZ.顧客番号
//        AND   MMKI.顧客番号 = :h_uid_varchar;


        sqlca.sql = new StringDto("SELECT NVL(MMKI.最終静態更新日, 0)," +
                " NVL(MMKI.最終静態更新時刻, 0)," +
                " NVL(MMKI.誕生年, 0)," +
                " NVL(MMKI.誕生月, 0)," +
                " NVL(MMKI.性別, 0)," +
                " NVL(RPAD(MMKI.顧客名称,LENGTH(MMKI.顧客名称)), ' ')," +
                " NVL(RPAD(MMKI.顧客カナ名称,LENGTH(MMKI.顧客カナ名称)), ' ')," +
                " NVL(RPAD(MMKZ.郵便番号,LENGTH(MMKZ.郵便番号)), ' ')," +
                " NVL(RPAD(MMKZ.住所１,LENGTH(MMKZ.住所１)), ' ')," +
                " NVL(RPAD(MMKZ.住所２,LENGTH(MMKZ.住所２)), ' ')" +
                " FROM MM顧客情報 MMKI," +
                "     MM顧客属性情報 MMKZ" +
                " WHERE MMKI.顧客番号 = MMKZ.顧客番号" +
                " AND   MMKI.顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_end_seitai_update_yyyymmdd, h_end_seitai_update_hh24miss, h_bath_yyyy, h_bath_mm, h_seibetsu, h_db_name, h_db_kana_name, h_db_zip, h_db_address1, h_db_address2);

        /* エラーの場合処理を異常終了する */
        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** MM顧客情報 SELECT NG %s\n", "");
            }
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode, "MM顧客情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        } else {
            if (sqlca.sqlcode == C_const_Ora_OK) {
                /* 正常終了 */
                hi_mmki_sw.arr = 1;        /* MM顧客情報    「なし(0)／あり(1)」 */

                /* スペース削除 */
                BT_Rtrim(h_db_name.strDto(), strlen(h_db_name));
                BT_Rtrim(h_db_kana_name.strDto(), strlen(h_db_kana_name));
                BT_Rtrim(h_db_zip.strDto(), strlen(h_db_zip));
                BT_Rtrim(h_db_address1.strDto(), strlen(h_db_address1));
                BT_Rtrim(h_db_address2.strDto(), strlen(h_db_address2));

            }
        }

        /* 初期化 */
        h_riyo_kano_point.arr = 0;
        h_nyukai_kigyou_cd.arr = 0;
        h_nyukai_tenpo.arr = 0;
        h_nyukai_oldcorp_cd.arr = 0;
        h_haken_kigyou_cd.arr = 0;
        h_haken_tenpo.arr = 0;

        /* TS利用可能ポイント情報検索処理 */
        /* 2021/03/04 期間限定ポイント対応 利用可能ポイントコメントアウト */
        /*                NVL(利用可能ポイント,0), */
        /*                :h_riyo_kano_point, */

//        EXEC SQL SELECT
//        NVL(入会企業コード,0),
//                NVL(入会店舗,0),
//                NVL(入会旧販社コード,0),
//                NVL(発券企業コード,0),
//                NVL(発券店舗,0)
//        INTO
//        :h_nyukai_kigyou_cd,
//                    :h_nyukai_tenpo,
//                    :h_nyukai_oldcorp_cd,
//                    :h_haken_kigyou_cd,
//                    :h_haken_tenpo
//        FROM  TS利用可能ポイント情報@CMSD
//        WHERE 顧客番号 = :h_uid_varchar;

        sqlca.sql = new StringDto("SELECT NVL(入会企業コード,0)," +
                " NVL(入会店舗,0)," +
                " NVL(入会旧販社コード,0)," +
                " NVL(発券企業コード,0)," +
                " NVL(発券店舗,0)" +
                " FROM  TS利用可能ポイント情報" +
                " WHERE 顧客番号 = ?");
        sqlca.restAndExecute(h_uid_varchar);
        sqlca.fetch();
        sqlca.recData(h_nyukai_kigyou_cd, h_nyukai_tenpo, h_nyukai_oldcorp_cd, h_haken_kigyou_cd, h_haken_tenpo);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** TS利用可能ポイント情報 SELECT NG %s\n", "");
            }
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 初期化 */
        h_mmkg_kigyou_cd.arr = 0;
        h_mmkg_nyukai_date.arr = 0;
        h_mmkg_taikai_date.arr = 0;

        /* MM顧客企業別属性情報検索処理 */
        /* 2016/08/17 カード情報取得のため、1040も除外 */
        /*                    AND 企業コード <> :h_kigyo_cd */
//        EXEC SQL SELECT  企業コード,
//        NVL(入会年月日,0),
//                NVL(退会年月日,0)
//        INTO :h_mmkg_kigyou_cd,
//                    :h_mmkg_nyukai_date,
//                    :h_mmkg_taikai_date
//        FROM  (
//                SELECT 企業コード,
//                入会年月日,
//                退会年月日
//                FROM MM顧客企業別属性情報
//                WHERE 顧客番号   =  :h_uid
//        AND 企業コード NOT IN (:h_kigyo_cd , 1040,3010,3050,3020,3040,3060)                      /* 2022/12/09 MCCM初版 MOD */
//        ORDER BY
//        企業コード
//                     ) T
//        WHERE ROWNUM = 1;

        sqlca.sql = new StringDto("SELECT  企業コード, NVL(入会年月日,0), NVL(退会年月日,0)" +
                " FROM  (SELECT 企業コード, 入会年月日, 退会年月日" +
                " FROM MM顧客企業別属性情報" +
                " WHERE 顧客番号   =  ?" +
                " AND 企業コード NOT IN (? , 1040,3010,3050,3020,3040,3060) " +
                " ORDER BY 企業コード LIMIT 1) T" +
                " ");
        sqlca.restAndExecute(h_uid, h_kigyo_cd);
        sqlca.fetch();
        sqlca.recData(h_mmkg_kigyou_cd, h_mmkg_nyukai_date, h_mmkg_taikai_date);

        if ((sqlca.sqlcode != C_const_Ora_OK) && (sqlca.sqlcode != C_const_Ora_NOTFOUND)) {
            /* エラーの場合処理を異常終了する */
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** MM顧客企業別属性情報 SELECT NG %s\n", "");
            }
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode, "MM顧客企業別属性情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }


        /*** 2013/07/04 最終更新日/時刻（連動ファイル）取得追加 ***/
        /* 初期化 */
        h_end_seitai_update_date_file.arr = 0;
        h_end_seitai_update_date_file_ymd.arr = 0;
        h_end_seitai_update_date_file_hms.arr = 0;
        memset(h_last_upd_date_yyyymmddhhmmss, 0x00, sizeof(h_last_upd_date_yyyymmddhhmmss));

        /* 最終更新日時（連動ファイル） */
        strncpy(h_last_upd_date_yyyymmddhhmmss, in_ec_rendo.last_upd_date_yyyymmddhhmmss, strlen(in_ec_rendo.last_upd_date_yyyymmddhhmmss));


        /* 連動ファイルの最終更新日時を変換 */
//        EXEC SQL SELECT
//        TO_NUMBER(TO_CHAR(TO_DATE(:h_last_upd_date_yyyymmddhhmmss, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDDHH24MISS')),
//        TO_NUMBER(TO_CHAR(TO_DATE(:h_last_upd_date_yyyymmddhhmmss, 'YYYY/MM/DD HH24:MI:SS'), 'YYYYMMDD')),
//        TO_NUMBER(TO_CHAR(TO_DATE(:h_last_upd_date_yyyymmddhhmmss, 'YYYY/MM/DD HH24:MI:SS'), 'HH24MISS'))
//        INTO
//        :h_end_seitai_update_date_file,
//                    :h_end_seitai_update_date_file_ymd,
//                    :h_end_seitai_update_date_file_hms
//        FROM DUAL;

        sqlca.sql = new StringDto("SELECT" +
                " TO_NUMBER(TO_CHAR(TO_DATE(CAST(? AS TEXT), 'YYYYMMDDHH24MISS'), 'YYYYMMDDHH24MISS'))," +
                " TO_NUMBER(TO_CHAR(TO_DATE(CAST(? AS TEXT), 'YYYYMMDDHH24MISS'), 'YYYYMMDD'))," +
                " TO_NUMBER(TO_CHAR(TO_DATE(CAST(? AS TEXT), 'YYYYMMDDHH24MISS'), 'HH24MISS'))" +
                " FROM DUAL");
        sqlca.restAndExecute(h_last_upd_date_yyyymmddhhmmss, h_last_upd_date_yyyymmddhhmmss, h_last_upd_date_yyyymmddhhmmss);
        sqlca.fetch();
        sqlca.recData(h_end_seitai_update_date_file, h_end_seitai_update_date_file_ymd, h_end_seitai_update_date_file_hms);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** SeitaiCheck *** 連動ファイル最終更新日時変換 NG %s\n", "");
            }
            sprintf(out_format_buf, "最終更新日時=[%s]", h_last_upd_date_yyyymmddhhmmss);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "SELECT",
                    sqlca.sqlcode, "TS利用可能ポイント情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 最終静態更新日時 */
        h_end_seitai_update_date.arr = (h_end_seitai_update_yyyymmdd.intVal() * 1000000) + h_end_seitai_update_hh24miss.intVal();

        if (DBG_LOG) {
            C_DbgMsg("*** SeitaiCheck *** 最終更新日時（ファイル） [%d]\n", h_end_seitai_update_date_file.longVal());
            C_DbgMsg("*** SeitaiCheck *** 最終静態更新日時         [%d]\n", h_end_seitai_update_date.longVal());
            C_DbgMsg("*** SeitaiCheck *** 最終静態更新日           [%d]\n", h_end_seitai_update_yyyymmdd.longVal());
            C_DbgMsg("*** SeitaiCheck *** 入会旧販社コード         [%d]\n", h_nyukai_oldcorp_cd.longVal());
            C_DbgMsg("*** SeitaiCheck *** 企業コード（カード会員） [%d]\n", h_mmkg_kigyou_cd.longVal());
            C_DbgMsg("*** SeitaiCheck *** 入会年月日（カード会員） [%d]\n", h_mmkg_nyukai_date.longVal());
            C_DbgMsg("*** SeitaiCheck *** 退会年月日（カード会員） [%d]\n", h_mmkg_taikai_date.longVal());
            C_DbgMsg("*** SeitaiCheck *** 顧客名称（DB)            [%s]\n", h_db_name);
            C_DbgMsg("*** SeitaiCheck *** 顧客カナ名称（DB)        [%s]\n", h_db_kana_name);
            C_DbgMsg("*** SeitaiCheck *** 郵便番号（DB)            [%s]\n", h_db_zip);
            C_DbgMsg("*** SeitaiCheck *** 住所１（DB)              [%s]\n", h_db_address1);
            C_DbgMsg("*** SeitaiCheck *** 住所２（DB)              [%s]\n", h_db_address2);
            C_DbgMsg("*** SeitaiCheck *** 最終更新日（ファイル）   [%d]\n", h_end_seitai_update_date_file_ymd.longVal());
            C_DbgMsg("*** SeitaiCheck *** 最終更新時刻（ファイル） [%d]\n", h_end_seitai_update_date_file_hms.longVal());
        }

        if (h_seitai_flag.intVal() != 1
                || h_end_seitai_update_date.longVal() <= h_end_seitai_update_date_file.longVal()
                || ((h_nyukai_oldcorp_cd.longVal() != C_HANSHA_SG && h_end_seitai_update_yyyymmdd.longVal() < 20130401)
                || (h_nyukai_oldcorp_cd.longVal() == C_HANSHA_SG && h_end_seitai_update_yyyymmdd.longVal() < 20130701))
                || (strlen(h_db_name) == 0 || strlen(h_db_kana_name) == 0 || strlen(h_db_zip) == 0
                || strlen(h_db_address1) == 0 || strlen(h_db_address2) == 0)) {
            seitai_flg.arr = 1;
        }

        if (DBG_LOG) {
            C_DbgEnd("静態情報更新チェック処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyaku                                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyaku(EC_RENDO_DATA in_ec_rendo,                      */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新処理                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyaku(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto birth_year = new StringDto(5);      /* 誕生年                      */
        StringDto birth_month = new StringDto(3);     /* 誕生月                      */
        StringDto birth_day = new StringDto(3);       /* 誕生日                      */
        IntegerDto age = new IntegerDto();                /* 年齢                        */
        StringDto storeno = new StringDto(7);         /* 店番号                      */
        StringDto tanto_code = new StringDto(11);     /* 担当者コード                */
//  int     i_loop;             /* ループ                      */      /* 2022/10/11 MCCM初版 DEL */
        StringDto wk_seibetsu = new StringDto(2);          /* 性別                        */

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新処理");
        }
        /* 初期化 */
        h_end_seitai_update_hh24miss.arr = 0;
        memset(bt_date_yyyy, 0x00, sizeof(bt_date_yyyy));
        memset(bt_date_mm, 0x00, sizeof(bt_date_mm));
        memset(bt_date_d, 0x00, sizeof(bt_date_d));
        memset(birth_year, 0x00, sizeof(birth_year));
        memset(birth_month, 0x00, sizeof(birth_month));
        memset(birth_day, 0x00, sizeof(birth_day));
        memset(storeno, 0x00, sizeof(storeno));
        memset(tanto_code, 0x00, sizeof(tanto_code));
        memset(wk_seibetsu, 0x00, sizeof(wk_seibetsu));
//  i_loop = 0;                                                        /* 2022/10/11 MCCM初版 DEL */
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /*                                               */
        /* ＨＯＳＴ変数セット                            */
        /*                                               */
        /*----------------------------------*/
        /*---MS顧客制度情報-----------------*/
        /*----------------------------------*/
        /* 電話番号登録フラグ */
        if (strlen(in_ec_rendo.telephone1) != 0 || strlen(in_ec_rendo.telephone2) != 0
                || strlen(in_ec_rendo.telephone3) != 0 || strlen(in_ec_rendo.telephone4) != 0) {
            /* 設定あり */
            h_tel_flag.arr = 1;
        }
        /* 静態取込済みフラグ */
        h_seitai_flag.arr = 1;    /*（取込済）*/

        /*----------------------------------*/
        /*---MM顧客情報---------------------*/
        /*----------------------------------*/
        if (strlen(in_ec_rendo.birth_date) != 0) {
            /* 誕生年 */
            strncpy(birth_year, in_ec_rendo.birth_date.strVal(), sizeof(birth_year) - 1);
            h_birth_year.arr = atoi(birth_year);
            /* 誕生月 */
            strncpy(birth_month, in_ec_rendo.birth_date.strVal().substring(4), sizeof(birth_month) - 1);
            h_birth_month.arr = atoi(birth_month);
            /* 誕生日 */
            strncpy(birth_day, in_ec_rendo.birth_date.strVal().substring(6), sizeof(birth_day) - 1);
            h_birth_day.arr = atoi(birth_day);
            /* 年齢 */
            strncpy(bt_date_yyyy, bat_yyyymmdd, 4);
            strncpy(bt_date_mm, bat_yyyymmdd.strVal().substring(4), 2);
            strncpy(bt_date_d, bat_yyyymmdd.strVal().substring(6), 2);
            rtn_cd.arr = C_CountAge(h_birth_year.intVal(), h_birth_month.intVal(), h_birth_day.intVal(),
                    atoi(bt_date_yyyy), atoi(bt_date_mm), atoi(bt_date_d), age);
            if (rtn_cd.arr != C_const_OK) {
                sprintf(out_format_buf, "誕生日=%04d%02d%02d",
                        h_birth_year.longVal(), h_birth_month.longVal(), h_birth_day.longVal());
                APLOG_WT("903", 0, null, "C_CountAge", rtn_cd,
                        out_format_buf, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
            h_age.arr = age;
            if (DBG_LOG) {
                C_DbgMsg("*** UpdateKokyaku *** 年齢計算(年齢=[%d])\n", age);
            }
        }
        /* 性別 */
        strncpy(wk_seibetsu, in_ec_rendo.seibetsu.strVal(), strlen(in_ec_rendo.seibetsu));
        h_seibetsu.arr = atoi(wk_seibetsu);
        /* 婚姻 */
        h_marriage.arr = 9;
        /* 社員区分 */
        h_emp_kbn.arr = 0;
        /* 最終静態更新時刻 */
//        EXEC SQL SELECT TO_NUMBER(TO_CHAR(SYSDATE,'HH24MISS'))
//        INTO :h_end_seitai_update_hh24miss
//        FROM DUAL@CMSD;
        sqlca.sql = new StringDto("SELECT TO_NUMBER(TO_CHAR(SYSDATE(),'HH24MISS')) FROM DUAL");
        sqlca.restAndExecute();
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
            rtn_cd.arr = InsertKokyakudata();
            if (rtn_cd.arr != C_const_OK && rtn_cd.arr != C_const_DUPL) {
                /* 重複エラー以外のエラーの場合 */
                /* 処理を終了する */
                return (C_const_NG);
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd.arr == C_const_OK) {
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
        memset(MmkiData, 0x00, sizeof(MmkiData));
        memset(MsksData, 0x00, sizeof(MsksData));

        /* 顧客番号セット */
        strcpy(MmkiData.kokyaku_no, h_uid);
        MmkiData.kokyaku_no.len = strlen(MmkiData.kokyaku_no);

        /* 顧客情報取得 */
        rtn_cd.arr = cmBTfuncB.C_GetCmMaster(MmkiData, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCmMaster", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no);

        /* 顧客制度情報取得 */
        rtn_cd.arr = cmBTfuncB.C_GetCsMaster(MsksData, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCsMaster", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                   */
        /* MS顧客制度情報・MM顧客情報更新                    */
        /* 顧客情報ありの場合                                */
        /*                                                   */
        rtn_cd.arr = UpdateKokyakudata();
        if (rtn_cd.arr != C_const_OK) {
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

/******************************************************************************/
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

    /******************************************************************************/
    public int InsertKokyakudata() {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ      */
        /* 2022/10/12 MCCM初版 ADD START */
        StringDto kaiin_name = new StringDto(80 * 3 + 1);             /* 会員氏名        */
        StringDto kokyaku_myoji = new StringDto(40 * 3 + 1);          /* 顧客名字        */
        StringDto kokyaku_name = new StringDto(40 * 3 + 1);           /* 顧客名前        */
        StringDto kaiin_kana_name = new StringDto(80 * 3 + 1);        /* 会員カナ名称    */
        StringDto utf8str = new StringDto(4069);                  /* UTF8変換後の文字列 */
        IntegerDto utf8len;                        /* UTF8変換後の文字列のレングス */
        StringDto wk_kanji_name = new StringDto(81);              /* 漢字氏名                     */
        StringDto kana_name_zen = new StringDto(80 * 3 + 1);              /* カナ氏名(全角)               */
        StringDto wk_kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1); /* カナ顧客名字(全角)           */
        StringDto wk_kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);  /* カナ顧客名前(全角)           */
        StringDto kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1);    /* カナ顧客名字(全角)           */
        StringDto kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);     /* カナ顧客名前(全角)           */
        StringDto wk_nyukai_entry_paper_ymd = new StringDto(8 + 1);    /* 入会申込用紙記載日wk         */
        int i = 0;
        int j = 0;
        StringDto wk_buf = new StringDto(4);  /* 1漢字格納用 */
        /* 2022/10/12 MCCM初版 ADD END */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報・MM顧客情報追加処理");
        }

        /* 2022/10/12 MCCM初版 ADD START */
        memset(kaiin_name, 0x00, sizeof(kaiin_name));
        memset(kaiin_kana_name, 0x00, sizeof(kaiin_kana_name));
        memset(utf8str, 0x00, sizeof(utf8str));
        memset(wk_kanji_name, 0x00, sizeof(wk_kanji_name));
        memset(kana_name_zen, 0x00, sizeof(kana_name_zen));
        memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
        memset(kokyaku_name, 0x00, sizeof(kokyaku_name));
        memset(wk_kana_kokyaku_myoji_zen, 0x00, sizeof(wk_kana_kokyaku_myoji_zen));
        memset(wk_kana_kokyaku_name_zen, 0x00, sizeof(wk_kana_kokyaku_name_zen));
        memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
        memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));
        memset(wk_nyukai_entry_paper_ymd, 0x00, sizeof(wk_nyukai_entry_paper_ymd));
        utf8len = new IntegerDto();

        if (0 == strlen(h_kaiin_name)) {
        } else {
            /* 漢字氏名 */
            /* UTF8に変換 */
            strncpy(wk_kanji_name, in_ec_rendo.kanji_name.strVal(), strlen(in_ec_rendo.kanji_name));
            rtn_cd.arr = C_ConvSJ2UT(wk_kanji_name, strlen(wk_kanji_name), utf8str, utf8len);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** UTF8変換(漢字氏名)NG %s\n", "");
                }
                APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }
            strcpy(kaiin_name, utf8str);

            /* 顧客名字 */
            if (strlen(kaiin_name) != 0) {
//                while (kaiin_name[i] != '\0') {
//
//                    wk_buf[0] = kaiin_name[i];
//                    wk_buf[1] = kaiin_name[i+1];
//                    wk_buf[2] = kaiin_name[i+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0) {
//                        break;
//                    }
//                    i = i + 3;
//                }
//
//                /* 顧客名字 */
//                strncpy(kokyaku_myoji, kaiin_name, i);
//                /* 顧客名前 */
//                strcpy(kokyaku_name, kaiin_name + i + 3);
                i = kaiin_name.strVal().indexOf("　") < 0 ? kaiin_name.size() : kaiin_name.strVal().indexOf("　");
                /* 顧客名字 */
                strncpy(kokyaku_myoji, kaiin_name.strVal(), i);
                /* 顧客名前 */
                strcpy(kokyaku_name, kaiin_name.strVal().substring(i + 3));

                /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
                if (strlen(kokyaku_myoji) > 20 * 3 || strlen(kokyaku_name) > 20 * 3) {

                    memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
                    memset(kokyaku_name, 0x00, sizeof(kokyaku_name));

                    /* 顧客名字 */
                    strncpy(kokyaku_myoji, kaiin_name, 60);
                    /* 顧客名前 */
                    strcpy(kokyaku_name, kaiin_name.substring(60));

                }
            }

            /* 全角に変換する */
            rtn_cd.arr = C_ConvHalf2Full(h_kana_name.strDto(), kana_name_zen);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
        }
        if (DBG_LOG) {
            C_DbgMsg("*** h_kana_name *** [%s]\n", h_kana_name);
            C_DbgMsg("*** kana_name_zen *** [%s]\n", kana_name_zen);
        }
        /* カナ顧客氏名 */
//        while(kana_name_zen[j] != '\0') {
//            wk_buf[0] = kana_name_zen[j];
//            wk_buf[1] = kana_name_zen[j+1];
//            wk_buf[2] = kana_name_zen[j+2];
//            wk_buf[3] = '\0';
//
//            if (strcmp(wk_buf, "　") == 0)
//            {
//                break;
//            }
//            j = j + 3;
//        }
//
//        /* 顧客カナ名字 */
//        strncpy(kana_kokyaku_myoji_zen, kana_name_zen, j);
//        /* 顧客カナ名前 */
//        strcpy(kana_kokyaku_name_zen, kana_name_zen + j + 3);

        j = kana_name_zen.arr.indexOf("　") < 0 ? kana_name_zen.size() : kana_name_zen.arr.indexOf("　");

        /* 顧客カナ名字 */
        strncpy(kana_kokyaku_myoji_zen, kana_name_zen, j);
        /* 顧客カナ名前 */
        strcpy(kana_kokyaku_name_zen, kana_name_zen.substring(j + 3));

        /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
        if (strlen(kana_kokyaku_myoji_zen) > 20 * 3 || strlen(kana_kokyaku_name_zen) > 20 * 3) {

            memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
            memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));

            /* 顧客名字 */
            strncpy(kana_kokyaku_myoji_zen, kana_name_zen, 60);
            /* 顧客名前 */
            strcpy(kana_kokyaku_name_zen, kana_name_zen.substring(60));

        }
        /* 2022/10/12 MCCM初版 ADD END */

        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no);

        /* ＳＱＬ文をセットする */
        sprintf(wk_sql,
                "INSERT INTO MS顧客制度情報 "
                        + "( 顧客番号,"
                        + "  誕生月,"
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
                        + "  ＥＣ会員フラグ,"
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
                        + "  顧客ステータス,"                                                                                /* 2022/10/12 MCCM初版 ADD */
                        + "  会員資格区分,"                                                                                  /* 2022/10/12 MCCM初版 ADD */
                        + "  グローバル会員フラグ,"                                                                          /* 2022/10/12 MCCM初版 ADD */
                        + "  ＬＩＮＥコネクト状況,"                                                                          /* 2022/10/12 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾フラグ,"                                                                          /* 2022/10/12 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾更新者,"                                                                          /* 2022/10/12 MCCM初版 ADD */
                        + "  ＭＣＣ制度許諾更新日時,"                                                                        /* 2022/10/12 MCCM初版 ADD */
                        + "  コーポレート会員フラグ,"                                                                        /* 2022/10/12 MCCM初版 ADD */
                        + "  属性管理主体システム ) "                                                                        /* 2022/10/12 MCCM初版 ADD */
                        + "VALUES "
                        + "( ?,"
                        + "  ?,"
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
                        + "  1,"
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
                        + "  1,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  1,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  1,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  4,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  sysdate(),"                                                                                       /* 2022/10/12 MCCM初版 ADD */
                        + "  0,"                                                                                             /* 2022/10/12 MCCM初版 ADD */
                        + "  1)"                                                                                             /* 2022/10/12 MCCM初版 ADD */
        );

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudata *** クエリ=[%s]\n", wk_sql);
        }

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat1 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat1");
        sqlca.sql = wk_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ 解析NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);

            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ 解析OK %s\n", "");
        }

        /* 動的ＳＱＬ文を実行する */
//        EXEC SQL  EXECUTE sql_stat1
//        USING   :MsksData.kokyaku_no,
//                      :h_birth_month,
//                      :h_kijyun_yyyymm,
//                      :h_tel_flag,
//                      :h_bat_yyyymmdd,
//                      :h_bat_yyyymmdd,
//                      :h_programid;
        sqlca.restAndExecute(MsksData.kokyaku_no, h_birth_month, h_kijyun_yyyymm, h_tel_flag, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* 重複エラー以外のエラーの場合 */
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudata *** 動的ＳＱＬ文NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode, "MS顧客制度情報", out_format_buf, 0, 0);

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
            rtn_cd.arr = C_KdataLock(h_uid.strDto(), "2", rtn_status);
            if (rtn_cd.arr == C_const_NG) {
                if (DBG_LOG) {
                    C_DbgMsg("*** InsertKokyakudata *** ポイント・顧客ロック status= %d\n", rtn_status);
                }
                APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客データ有無フラグセット */
            uid_flg = rtn_cd.arr;

            /* 重複エラー */
            return (C_const_DUPL);
        } else {
            /* 正常の場合 */
            /* 初期化 */
            memset(MmkiData, 0x00, sizeof(MmkiData));
            /* 顧客番号セット */
            memcpy(MmkiData.kokyaku_no, h_uid.strVal(), sizeof(h_uid) - 1);
            MmkiData.kokyaku_no.len = strlen(h_uid);
            /* 顧客名称         */
            if (strlen(h_kaiin_name) == 0) {
//          memcpy(MmkiData.kokyaku_mesho, " ", sizeof(MmkiData.kokyaku_mesho)-1);                             /* 2022/10/12 MCCM初版 DEL */
                memcpy(MmkiData.kokyaku_myoji, " ", sizeof(MmkiData.kokyaku_myoji) - 1);                             /* 2022/10/12 MCCM初版 ADD */
                memcpy(MmkiData.kokyaku_name, " ", sizeof(MmkiData.kokyaku_name) - 1);                              /* 2022/10/12 MCCM初版 ADD */
            } else {
//          memcpy(MmkiData.kokyaku_mesho, h_kaiin_name, sizeof(MmkiData.kokyaku_mesho)-1);                    /* 2022/10/12 MCCM初版 DEL */
                memcpy(MmkiData.kokyaku_myoji.strDto(), kokyaku_myoji, sizeof(MmkiData.kokyaku_myoji) - 1);                   /* 2022/10/12 MCCM初版 ADD */
                memcpy(MmkiData.kokyaku_name.strDto(), kokyaku_name, sizeof(MmkiData.kokyaku_name) - 1);                    /* 2022/10/12 MCCM初版 ADD */
            }
            /* 顧客カナ名称         */
            if (strlen(h_kana_name) == 0) {
//          memcpy(h_kana_name, " ", sizeof(h_kana_name)-1);                                                   /* 2022/10/12 MCCM初版 DEL */
                memcpy(MmkiData.kana_kokyaku_myoji, " ", sizeof(MmkiData.kana_kokyaku_myoji) - 1);                   /* 2022/10/12 MCCM初版 ADD */
                memcpy(MmkiData.kana_kokyaku_name, " ", sizeof(MmkiData.kana_kokyaku_name) - 1);                    /* 2022/10/12 MCCM初版 ADD */
            } else {                                                                                               /* 2022/10/12 MCCM初版 ADD */
                memcpy(MmkiData.kana_kokyaku_myoji.strDto(), kana_kokyaku_myoji_zen, sizeof(MmkiData.kana_kokyaku_myoji) - 1); /* 2022/10/12 MCCM初版 ADD*/
                memcpy(MmkiData.kana_kokyaku_name.strDto(), kana_kokyaku_name_zen, sizeof(MmkiData.kana_kokyaku_name) - 1);  /* 2022/10/12 MCCM初版 ADD*/
            }
            /* 入会申込用紙記載日   */
            memcpy(wk_nyukai_entry_paper_ymd, in_ec_rendo.nyukaibi_yyyymmdd, strlen(in_ec_rendo.nyukaibi_yyyymmdd));/* 2022/10/12 MCCM初版 ADD*/
            MmkiData.nyukai_entry_paper_ymd.arr = atoi(wk_nyukai_entry_paper_ymd);                                      /* 2022/10/12 MCCM初版 ADD*/

            /*** 2013/07/04 最終静態更新日、最終静態更新時刻の設定値変更 ***/
            /* INSERTする */
//            EXEC SQL INSERT INTO MM顧客情報
//                    (   顧客番号,
//                            休眠フラグ,
//                            顧客名字,                                                                              /* 2022/10/12 MCCM初版 MOD */
//                            顧客名前,                                                                              /* 2022/10/12 MCCM初版 ADD */
//                            カナ顧客名字,                                                                          /* 2022/10/12 MCCM初版 MOD */
//                            カナ顧客名前,                                                                          /* 2022/10/12 MCCM初版 ADD */
//                            年齢,
//                            誕生年,
//                            誕生月,
//                            誕生日,
//                            性別,
//                            婚姻,
//                            入会企業コード,
//                            /*                      入会店舗,                                                                            *//* 2022/10/12 MCCM初版 DEL */
//                            発券企業コード,
//                            発券店舗,
//                            社員区分,
//                            ポータル入会年月日,
//                            ポータル退会年月日,
//                            作業企業コード,
//                            作業者ＩＤ,
//                            作業年月日,
//                            作業時刻,
//                            バッチ更新日,
//                            最終静態更新日,
//                            最終静態更新時刻,
//                            最終更新日,
//                            最終更新日時,
//                            最終更新プログラムＩＤ,
//                            顧客ステータス,                                                                        /* 2022/10/12 MCCM初版 ADD */
//                            入会申込用紙記載日,                                                                    /* 2022/10/12 MCCM初版 ADD */
//                            静態登録日,                                                                            /* 2022/10/12 MCCM初版 ADD */
//                            入会会社コードＭＣＣ,                                                                  /* 2022/10/12 MCCM初版 ADD */
//                            入会店舗ＭＣＣ,                                                                        /* 2022/10/12 MCCM初版 ADD */
//                            シニア,                                                                                /* 2022/10/12 MCCM初版 ADD */
//                            属性管理主体システム,                                                                  /* 2022/10/12 MCCM初版 ADD */
//                            プッシュ通知許可フラグ,                                                                /* 2022/10/12 MCCM初版 ADD */
//                            メールアドレス１送信フラグ,                                                            /* 2022/10/12 MCCM初版 ADD */
//                            メールアドレス２送信フラグ,                                                            /* 2022/10/12 MCCM初版 ADD */
//                            メールアドレス３送信フラグ )                                                           /* 2022/10/12 MCCM初版 ADD */
//            VALUES (  :MmkiData.kokyaku_no,
//                    0,
//                        :MmkiData.kokyaku_myoji,                                                               /* 2022/10/12 MCCM初版 MOD */
//                        :MmkiData.kokyaku_name,                                                                /* 2022/10/12 MCCM初版 ADD */
//                        :MmkiData.kana_kokyaku_myoji,                                                          /* 2022/10/12 MCCM初版 MOD */
//                        :MmkiData.kana_kokyaku_name,                                                           /* 2022/10/12 MCCM初版 ADD */
//                        :h_age,
//                        :h_birth_year,
//                        :h_birth_month,
//                        :h_birth_day,
//                        :h_seibetsu,
//                        :h_marriage,
//                        :h_ps_corpid,
//            /*                      :h_tenpo_no,                                                                         *//* 2022/10/12 MCCM初版 DEL */
//                        :h_ps_corpid,
//                        :h_tenpo_no,
//                        :h_emp_kbn,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                        :h_bat_yyyymmdd,
//                        :h_end_seitai_update_date_file_ymd,
//                        :h_end_seitai_update_date_file_hms,
//                        :h_bat_yyyymmdd,
//                    sysdate,
//                        :h_programid,
//                    1,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                        :MmkiData.nyukai_entry_paper_ymd,                                                      /* 2022/10/12 MCCM初版 ADD */
//                        :h_bat_yyyymmdd,                                                                       /* 2022/10/12 MCCM初版 ADD */
//                    2500,                                                                                  /* 2022/10/12 MCCM初版 ADD */
//                        :h_renkei_mise_no,                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    0,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    1,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    0,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    0,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    0,                                                                                     /* 2022/10/12 MCCM初版 ADD */
//                    0);                                                                                    /* 2022/10/12 MCCM初版 ADD */
            StringDto WRKSQL = new StringDto();
            sprintf(WRKSQL, "INSERT INTO MM顧客情報" +
                    "                    (   顧客番号," +
                    "                            休眠フラグ," +
                    "                            顧客名字," +
                    "                            顧客名前," +
                    "                            カナ顧客名字," +
                    "                            カナ顧客名前," +
                    "                            年齢," +
                    "                            誕生年," +
                    "                            誕生月," +
                    "                            誕生日," +
                    "                            性別," +
                    "                            婚姻," +
                    "                            入会企業コード," +
                    "                            発券企業コード," +
                    "                            発券店舗," +
                    "                            社員区分," +
                    "                            ポータル入会年月日," +
                    "                            ポータル退会年月日," +
                    "                            作業企業コード," +
                    "                            作業者ＩＤ," +
                    "                            作業年月日," +
                    "                            作業時刻," +
                    "                            バッチ更新日," +
                    "                            最終静態更新日," +
                    "                            最終静態更新時刻," +
                    "                            最終更新日," +
                    "                            最終更新日時," +
                    "                            最終更新プログラムＩＤ," +
                    "                            顧客ステータス,                         " +
                    "                            入会申込用紙記載日,                     " +
                    "                            静態登録日,                            " +
                    "                            入会会社コードＭＣＣ,                    " +
                    "                            入会店舗ＭＣＣ,                         " +
                    "                            シニア,                                " +
                    "                            属性管理主体システム,                   " +
                    "                            プッシュ通知許可フラグ,                  " +
                    "                            メールアドレス１送信フラグ,              " +
                    "                            メールアドレス２送信フラグ,              " +
                    "                            メールアドレス３送信フラグ )             " +
                    "            VALUES (  ?," +
                    "                    0," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                        ?," +
                    "                    sysdate(), " +
                    "                        ?," +
                    "                    1,           " +
                    "                        ?,    " +
                    "                        ?,    " +
                    "                    2500,        " +
                    "                        ?,    " +
                    "                    0,           " +
                    "                    1,           " +
                    "                    0,           " +
                    "                    0,           " +
                    "                    0,           " +
                    "                    0)");
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(MmkiData.kokyaku_no, MmkiData.kokyaku_myoji, MmkiData.kokyaku_name, MmkiData.kana_kokyaku_myoji, MmkiData.kana_kokyaku_name,
                    h_age.intVal(), h_birth_year, h_birth_month, h_birth_day, h_seibetsu, h_marriage, h_ps_corpid, h_ps_corpid, h_tenpo_no, h_emp_kbn,
                    h_bat_yyyymmdd, h_end_seitai_update_date_file_ymd, h_end_seitai_update_date_file_hms, h_bat_yyyymmdd, h_programid,
                    MmkiData.nyukai_entry_paper_ymd, h_bat_yyyymmdd, h_renkei_mise_no);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", MmkiData.kokyaku_no.arr);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
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

/******************************************************************************/
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

    /******************************************************************************/
    public int UpdateKokyakudata() {
        IntegerDto rtn_cd = new IntegerDto();                           /* 関数戻り値     */
        StringDto wk_sql_buf = new StringDto(C_const_SQLMaxLen);    /* ＳＱＬ文編集用 */
        StringDto wk_sql_item = new StringDto(512);                 /* 動的SQLバッファ */
        /* 2022/10/12 MCCM初版 ADD START */
        StringDto kaiin_name = new StringDto(80 * 3 + 1);             /* 会員氏名        */
        StringDto kokyaku_myoji = new StringDto(40 * 3 + 1);          /* 顧客名字        */
        StringDto kokyaku_name = new StringDto(40 * 3 + 1);           /* 顧客名前        */
        StringDto kaiin_kana_name = new StringDto(80 * 3 + 1);        /* 会員カナ名称    */
        StringDto utf8str = new StringDto(4069);                  /* UTF8変換後の文字列 */
        StringDto wk_kanji_name = new StringDto(81);              /* 漢字氏名                     */
        StringDto kana_name_zen = new StringDto(80 * 3 + 1);              /* カナ氏名(全角)               */
        StringDto wk_kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1); /* カナ顧客名字(全角)           */
        StringDto wk_kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);  /* カナ顧客名前(全角)           */
        StringDto kana_kokyaku_myoji_zen = new StringDto(40 * 3 + 1);    /* カナ顧客名字(全角)           */
        StringDto kana_kokyaku_name_zen = new StringDto(40 * 3 + 1);     /* カナ顧客名前(全角)           */
        IntegerDto utf8len;                        /* UTF8変換後の文字列のレングス */
        int i = 0;
        int j = 0;
        StringDto wk_buf = new StringDto(4);  /* 1漢字格納用 */
        /* 2022/10/12 MCCM初版 ADD END */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報・MM顧客情報更新処理");
        }

        /* 2022/10/12 MCCM初版 ADD START */
        memset(kaiin_name, 0x00, sizeof(kaiin_name));
        memset(kaiin_kana_name, 0x00, sizeof(kaiin_kana_name));
        memset(utf8str, 0x00, sizeof(utf8str));
        memset(wk_kanji_name, 0x00, sizeof(wk_kanji_name));
        memset(kana_name_zen, 0x00, sizeof(kana_name_zen));
        memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
        memset(kokyaku_name, 0x00, sizeof(kokyaku_name));
        memset(wk_kana_kokyaku_myoji_zen, 0x00, sizeof(wk_kana_kokyaku_myoji_zen));
        memset(wk_kana_kokyaku_name_zen, 0x00, sizeof(wk_kana_kokyaku_name_zen));
        memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
        memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));
        utf8len = new IntegerDto();

        if (0 == strlen(h_kaiin_name)) {
        } else {
            /* 漢字氏名 */
            /* UTF8に変換 */
            strncpy(wk_kanji_name, in_ec_rendo.kanji_name.strVal(), strlen(in_ec_rendo.kanji_name));
            rtn_cd.arr = C_ConvSJ2UT(wk_kanji_name, strlen(wk_kanji_name), utf8str, utf8len);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("*** CheckMscard *** UTF8変換(漢字氏名)NG %s\n", "");
                }
                APLOG_WT("903", 0, null, "C_ConvSJ2UT", rtn_cd, 0, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }
            strcpy(kaiin_name, utf8str);

            /* 顧客名字 */
            if (strlen(kaiin_name) != 0) {
//                while (kaiin_name[i] != '\0') {
//
//                    wk_buf[0] = kaiin_name[i];
//                    wk_buf[1] = kaiin_name[i+1];
//                    wk_buf[2] = kaiin_name[i+2];
//                    wk_buf[3] = '\0';
//
//                    if (strcmp(wk_buf, "　") == 0) {
//                        break;
//                    }
//                    i = i + 3;
//                }
//
//                /* 顧客名字 */
//                strncpy(kokyaku_myoji, kaiin_name, i);
//                /* 顧客名前 */
//                strcpy(kokyaku_name, kaiin_name + i + 3);
                i = kaiin_name.strVal().indexOf("　") < 0 ? kaiin_name.size() : kaiin_name.strVal().indexOf("　");
                /* 顧客名字 */
                strncpy(kokyaku_myoji, kaiin_name.strVal(), i);
                /* 顧客名前 */
                strcpy(kokyaku_name, kaiin_name.strVal().substring(i + 3));

                /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
                if (strlen(kokyaku_myoji) > 20 * 3 || strlen(kokyaku_name) > 20 * 3) {

                    memset(kokyaku_myoji, 0x00, sizeof(kokyaku_myoji));
                    memset(kokyaku_name, 0x00, sizeof(kokyaku_name));

                    /* 顧客名字 */
                    strncpy(kokyaku_myoji, kaiin_name, 60);
                    /* 顧客名前 */
                    strcpy(kokyaku_name, kaiin_name.substring(60));

                }
            }

            /* 全角に変換する */
            rtn_cd.arr = C_ConvHalf2Full(h_kana_name.strDto(), kana_name_zen);
            if (rtn_cd.arr != C_const_OK) {
                APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                        0, 0, 0, 0);
                /* 処理を終了する */
                return (C_const_NG);
            }
        }
        ;

        /* カナ顧客氏名 */
//        while(kana_name_zen[j] != '\0') {
//
//            wk_buf[0] = kana_name_zen[j];
//            wk_buf[1] = kana_name_zen[j+1];
//            wk_buf[2] = kana_name_zen[j+2];
//            wk_buf[3] = '\0';
//
//            if (strcmp(wk_buf, "　") == 0)
//            {
//                break;
//            }
//            j = j + 3;
//        }
//
//        /* 顧客カナ名字 */
//        strncpy(kana_kokyaku_myoji_zen, kana_name_zen, j);
//        /* 顧客カナ名前 */
//        strcpy(kana_kokyaku_name_zen, kana_name_zen + j + 3);
        j = kana_name_zen.arr.indexOf("　") < 0 ? kana_name_zen.size() : kana_name_zen.arr.indexOf("　");

        /* 顧客カナ名字 */
        strncpy(kana_kokyaku_myoji_zen, kana_name_zen, j);
        /* 顧客カナ名前 */
        strcpy(kana_kokyaku_name_zen, kana_name_zen.substring(j + 3));

        /* 名字または名前どっちが20文字を超えた場合、苗字に20文字を格納し、名前に残りの文字数を設定、空白は削除しない */
        if (strlen(kana_kokyaku_myoji_zen) > 20 * 3 || strlen(kana_kokyaku_name_zen) > 20 * 3) {

            memset(kana_kokyaku_myoji_zen, 0x00, sizeof(kana_kokyaku_myoji_zen));
            memset(kana_kokyaku_name_zen, 0x00, sizeof(kana_kokyaku_name_zen));

            /* 顧客名字 */
            strncpy(kana_kokyaku_myoji_zen, kana_name_zen, 60);
            /* 顧客名前 */
            strcpy(kana_kokyaku_name_zen, kana_name_zen.substring(60));

        }
        /* 2022/10/12 MCCM初版 ADD END */

        /*----------------------------------*/
        /*---MM顧客情報更新処理-------------*/
        /*----------------------------------*/
        /* ＳＱＬを生成する */
        strcpy(wk_sql_buf, "UPDATE MM顧客情報  SET ");

        /* 顧客名字 */                                                                                             /* 2022/10/12 MCCM初版 MOD */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/12 MCCM初版 MOD */
        sprintf(wk_sql_item, "顧客名字 = '%s',", kokyaku_myoji);                                                   /* 2022/10/12 MCCM初版 MOD */
        strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/12 MCCM初版 MOD */
        /* 顧客名前 */                                                                                             /* 2022/10/12 MCCM初版 MOD */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/12 MCCM初版 MOD */
        sprintf(wk_sql_item, "顧客名前 = '%s',", kokyaku_name);                                                    /* 2022/10/12 MCCM初版 MOD */
        strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/12 MCCM初版 MOD */
        /* カナ顧客名字 */                                                                                         /* 2022/10/12 MCCM初版 MOD */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/12 MCCM初版 MOD */
        sprintf(wk_sql_item, "カナ顧客名字 = '%s',", kana_kokyaku_myoji_zen);                                      /* 2022/10/12 MCCM初版 MOD */
        strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/12 MCCM初版 MOD */
        /* カナ顧客名前 */                                                                                         /* 2022/10/12 MCCM初版 MOD */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/12 MCCM初版 MOD */
        sprintf(wk_sql_item, "カナ顧客名前 = '%s',", kana_kokyaku_name_zen);                                       /* 2022/10/12 MCCM初版 MOD */
        strcat(wk_sql_buf, wk_sql_item);                                                                         /* 2022/10/12 MCCM初版 MOD */
        /* 年齢 */
        if (strlen(in_ec_rendo.birth_date) != 0) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "年齢 = %d,", h_age);
            strcat(wk_sql_buf, wk_sql_item);
        }
        if (strlen(in_ec_rendo.birth_date) != 0) {
            /* 誕生年 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "誕生年 = %d,", h_birth_year);
            strcat(wk_sql_buf, wk_sql_item);
            /* 誕生月 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "誕生月 = %d,", h_birth_month);
            strcat(wk_sql_buf, wk_sql_item);
            /* 誕生日 */
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "誕生日 = %d,", h_birth_day);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 性別 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "性別 = %d,", h_seibetsu);
        strcat(wk_sql_buf, wk_sql_item);
        /* 入会企業コード */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "入会企業コード = %d,", h_nyukai_kigyou_cd);
        strcat(wk_sql_buf, wk_sql_item);
        /* 入会店舗 */
//  memset( wk_sql_item, 0x00, sizeof(wk_sql_item));                                                           /* 2022/10/12 MCCM初版 DEL */
//  sprintf(wk_sql_item, "入会店舗 = %d,", h_nyukai_tenpo);                                                    /* 2022/10/12 MCCM初版 DEL */
//  strcat( wk_sql_buf , wk_sql_item);                                                                         /* 2022/10/12 MCCM初版 DEL */
        /* 発券企業コード */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "発券企業コード = %d,", h_haken_kigyou_cd);
        strcat(wk_sql_buf, wk_sql_item);
        /* 発券店舗 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "発券店舗 = %d,", h_haken_tenpo);
        strcat(wk_sql_buf, wk_sql_item);
        /* バッチ更新日 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "バッチ更新日 = %d,", h_bat_yyyymmdd);
        strcat(wk_sql_buf, wk_sql_item);

        /*** 2013/07/04 最終静態更新日、最終静態更新時刻の設定値変更 ***/
        /* 最終静態更新日 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "最終静態更新日 = %d,", h_end_seitai_update_date_file_ymd);
        strcat(wk_sql_buf, wk_sql_item);

        /* 最終静態更新時刻 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "最終静態更新時刻 = %d,", h_end_seitai_update_date_file_hms);
        strcat(wk_sql_buf, wk_sql_item);

        /* 最終更新日・日時・プログラムＩＤ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item,
                "最終更新日 = %d," +
                        "最終更新日時 = SYSDATE()," +
                        "最終更新プログラムＩＤ = '%s', "
                , h_bat_yyyymmdd, h_programid);
        strcat(wk_sql_buf, wk_sql_item);

        /* 2022/10/12 MCCM初版 ADD START */
        /* 入会会社コードＭＣＣ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "入会会社コードＭＣＣ = %d,", 2500);
        strcat(wk_sql_buf, wk_sql_item);
        /* 入会店舗ＭＣＣ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "入会店舗ＭＣＣ = %d ", h_renkei_mise_no);
        strcat(wk_sql_buf, wk_sql_item);
        /* 2022/10/12 MCCM初版 ADD END */

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

//        EXEC SQL PREPARE sql_stat2 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat2");
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", sqlca.sqlcode,
                    "MM顧客情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* UPDATE文を実行する */
//        EXEC SQL EXECUTE sql_stat2;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
                    "MM顧客情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /*----------------------------------*/
        /*---MS顧客制度情報更新処理---------*/
        /*----------------------------------*/
        /* ＳＱＬを生成する */
        memset(wk_sql_buf, 0x00, sizeof(wk_sql_buf));
        strcpy(wk_sql_buf, "UPDATE MS顧客制度情報  SET ");

        /* 誕生月 */
        if (strlen(in_ec_rendo.birth_date) != 0) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "誕生月 = %d,", h_birth_month);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 在籍開始年月 */
        if (MmkgData.nyukai_ymd.intVal() == 0 || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal()) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "在籍開始年月 = %d,", h_kijyun_yyyymm);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 電話番号登録フラグ */
        if (h_tel_flag.intVal() == 1) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, "電話番号登録フラグ = %d,", h_tel_flag);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* ＥＣ会員フラグ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "ＥＣ会員フラグ = %d,", 1);
        strcat(wk_sql_buf, wk_sql_item);
        /* 静態取込済みフラグ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "静態取込済みフラグ = %d,", h_seitai_flag);
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
                        "最終更新プログラムＩＤ = '%s' "
                , h_bat_yyyymmdd, h_programid);
        strcat(wk_sql_buf, wk_sql_item);

        /* 2022/10/12 MCCM初版 ADD START */
        /* ＭＣＣ制度許諾フラグ  */
        if (h_mcc_seido_kyodaku_flg.intVal() == 0) {
            memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
            sprintf(wk_sql_item, ",ＭＣＣ制度許諾フラグ = %d, " +
                    "ＭＣＣ制度許諾更新者 = %d, " +
                    "ＭＣＣ制度許諾更新日時 = SYSDATE()", 1, 4);
            strcat(wk_sql_buf, wk_sql_item);
        }
        /* 2022/10/12 MCCM初版 ADD END */

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

//        EXEC SQL PREPARE sql_stat3 from :str_sql;
        sqlca = sqlcaManager.get("sql_stat3");
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* UPDATE文を実行する */
//        EXEC SQL EXECUTE sql_stat3;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuForTaikai                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuForTaikai(EC_RENDO_DATA in_ec_rendo,             */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客情報更新処理（退会）                                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyakuForTaikai(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto birth_year = new StringDto(5);      /* 誕生年                      */
        StringDto birth_month = new StringDto(3);     /* 誕生月                      */
        StringDto birth_day = new StringDto(3);       /* 誕生日                      */
        StringDto storeno = new StringDto(7);         /* 店番号                      */
        StringDto tanto_code = new StringDto(11);     /* 担当者コード                */
//  int     i_loop;             /* ループ                      */               /* 2022/10/11 MCCM初版 DEL */
        StringDto wk_seibetsu = new StringDto(2);          /* 性別                        */

        if (DBG_LOG) {
            C_DbgStart("顧客情報更新（退会）処理");
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
//  i_loop = 0;                                                                 /* 2022/10/11 MCCM初版 DEL */
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /* 婚姻 */
        h_marriage.arr = 9;

        /*                                                   */
        /* MS顧客制度情報・MM顧客情報追加                    */
        /* 顧客情報なし                                      */
        /*                                                   */
        if (DBG_LOG) {
            C_DbgMsg("*** UpdateKokyakuForTaikai *** 顧客情報 有無%s\n", "");
            C_DbgMsg("MS顧客制度情報「なし(0)／あり(1)」=[%d]\n", hi_msks_sw);
            C_DbgMsg("MM顧客情報    「なし(0)／あり(1)」=[%d]\n", hi_mmki_sw);
        }
        if (hi_msks_sw.intVal() == 0 && hi_mmki_sw.intVal() == 0) {
            rtn_cd.arr = InsertKokyakudataForTaikai();
            if (rtn_cd.arr != C_const_OK && rtn_cd.arr != C_const_DUPL) {
                /* 重複エラー以外のエラーの場合 */
                /* 処理を終了する */
                return (C_const_NG);
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd.arr == C_const_OK) {
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
        memset(MmkiData, 0x00, sizeof(MmkiData));
        memset(MsksData, 0x00, sizeof(MsksData));

        /* 顧客番号セット */
        strcpy(MmkiData.kokyaku_no, h_uid);
        MmkiData.kokyaku_no.len = strlen(MmkiData.kokyaku_no);

        /* 顧客情報取得 */
        rtn_cd.arr = cmBTfuncB.C_GetCmMaster(MmkiData, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCmMaster", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no);

        /* 顧客制度情報取得 */
        rtn_cd.arr = cmBTfuncB.C_GetCsMaster(MsksData, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetCsMaster", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                   */
        /* MS顧客制度情報・MM顧客情報更新                    */
        /* 顧客情報ありの場合                                */
        /*                                                   */
        rtn_cd.arr = UpdateKokyakudataForTaikai();
        if (rtn_cd.arr != C_const_OK) {
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            C_DbgEnd("顧客情報更新（退会）処理", 0, 0, 0);
        }

        /* 処理を終了する */
        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakudataForTaikai                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakudataForTaikai()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報・MM顧客情報追加（退会）                       */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              -2  ： 重複エラー                                             */
    /*                                                                            */

    /******************************************************************************/
    public int InsertKokyakudataForTaikai() {
        IntegerDto rtn_cd = new IntegerDto();             /* 関数戻り値                  */
        IntegerDto rtn_status = new IntegerDto();         /* 関数ステータス              */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen); /* 動的SQLバッファ      */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報・MM顧客情報追加（退会）処理");
        }
        /* 初期化 */
        memset(wk_sql, 0x00, sizeof(wk_sql));
        memset(str_sql, 0x00, sizeof(str_sql));
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /* 顧客番号セット */
        strcpy(MsksData.kokyaku_no, h_uid);
        MsksData.kokyaku_no.len = strlen(MsksData.kokyaku_no);

        /* ＳＱＬ文をセットする */
        sprintf(wk_sql,
                "INSERT INTO MS顧客制度情報 "
                        + "( 顧客番号,"
                        + "  誕生月,"
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
                        + "  ＥＣ会員フラグ,"
                        + "  作業企業コード,"
                        + "  作業者ＩＤ,"
                        + "  作業年月日,"
                        + "  作業時刻,"
                        + "  バッチ更新日,"
                        + "  最終更新日,"
                        + "  最終更新日時,"
                        + "  最終更新プログラムＩＤ,"
                        + "  アプリ会員フラグ) "
                        + "VALUES "
                        + "( ?,"
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
                        + "  0,"
                        + "  0,"
                        + "  0,"
                        + "  0,"
                        + "  0,"
                        + "  ?,"
                        + "  ?,"
                        + "  sysdate(),"
                        + "  ?,"
                        + "  0)"
        );

        /* ＨＯＳＴ変数にセット */
        strcpy(str_sql, wk_sql);

        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudataForTaikai *** クエリ=[%s]\n", wk_sql);
        }

        /* 動的ＳＱＬ文を解析する */
//        EXEC SQL PREPARE sql_stat1 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("sql_stat1");
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudataForTaikai *** 動的ＳＱＬ 解析NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);

            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudataForTaikai *** 動的ＳＱＬ 解析OK %s\n", "");
        }

        /* 動的ＳＱＬ文を実行する */
//        EXEC SQL  EXECUTE sql_stat1
//        USING   :MsksData.kokyaku_no,
//                      :h_bat_yyyymmdd,
//                      :h_bat_yyyymmdd,
//                      :h_programid;
        sqlca.restAndExecute(MsksData.kokyaku_no, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);
        if (sqlca.sqlcode != C_const_Ora_OK
                && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* 重複エラー以外のエラーの場合 */
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudataForTaikai *** 動的ＳＱＬ文NG = %d\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]", MsksData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode, "MS顧客制度情報", out_format_buf, 0, 0);

            return (C_const_NG);
        }
        if (DBG_LOG) {
            C_DbgMsg("*** InsertKokyakudataForTaikai *** 動的ＳＱＬ文OK %s\n", "");
        }

        /* 重複エラーの場合 */
        if (sqlca.sqlcode == C_const_Ora_DUPL) {
            if (DBG_LOG) {
                C_DbgMsg("*** InsertKokyakudataForTaikai *** ポイント・顧客ロック%s\n", "");
            }
            rtn_cd.arr = C_KdataLock(h_uid.strDto(), "2", rtn_status);
            if (rtn_cd.arr == C_const_NG) {
                if (DBG_LOG) {
                    C_DbgMsg("*** InsertKokyakudataForTaikai *** ポイント・顧客ロック status= %d\n", rtn_status);
                }
                APLOG_WT("903", 0, null, "C_KdataLock", rtn_cd, rtn_status, 0, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            /* 顧客データ有無フラグセット */
            uid_flg = rtn_cd.arr;

            /* 重複エラー */
            return (C_const_DUPL);
        } else {
            /* 正常の場合 */
            /* 初期化 */
            memset(MmkiData, 0x00, sizeof(MmkiData));
            /* 顧客番号セット */
            memcpy(MmkiData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
            MmkiData.kokyaku_no.len = strlen(h_uid);

            /* INSERTする */
//            EXEC SQL INSERT INTO MM顧客情報
//                    (   顧客番号,
//                            婚姻,
//                            入会企業コード,
//                            入会店舗,
//                            発券企業コード,
//                            発券店舗,
//                            作業企業コード,
//                            作業者ＩＤ,
//                            作業年月日,
//                            作業時刻,
//                            バッチ更新日,
//                            最終更新日,
//                            最終更新日時,
//                            最終更新プログラムＩＤ )
//            VALUES (  :MmkiData.kokyaku_no,
//                        :h_marriage,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                    0,
//                        :h_bat_yyyymmdd,
//                        :h_bat_yyyymmdd,
//                    sysdate,
//                        :h_programid);

            StringDto WRKSQL = new StringDto();
            sprintf(WRKSQL, "INSERT INTO MM顧客情報" +
                    "(   顧客番号," +
                    "婚姻," +
                    "入会企業コード," +
                    "入会店舗," +
                    "発券企業コード," +
                    "発券店舗," +
                    "作業企業コード," +
                    "作業者ＩＤ," +
                    "作業年月日," +
                    "作業時刻," +
                    "バッチ更新日," +
                    "最終更新日," +
                    "最終更新日時," +
                    "最終更新プログラムＩＤ )" +
                    "            VALUES (  ?," +
                    "                      ?," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                    0," +
                    "                        ?," +
                    "                        ?," +
                    "                    sysdate()," +
                    "                        ?)");
            sqlca.sql = WRKSQL;
            sqlca.restAndExecute(MmkiData.kokyaku_no, h_marriage, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", MmkiData.kokyaku_no.arr);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
                        "MM顧客情報", out_format_buf, 0, 0);

                /* 処理を終了する */
                return (C_const_NG);
            }

            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("MS顧客制度情報・MM顧客情報追加（退会）処理", 0, 0, 0);
                /*---------------------------------------------*/
            }
            /* 処理を終了する */
            return (C_const_OK);
        }
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakudataForTaikai                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakudataForTaikai()                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MS顧客制度情報更新（退会）                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyakudataForTaikai() {

        StringDto wk_sql_buf = new StringDto(C_const_SQLMaxLen);    /* ＳＱＬ文編集用 */
        StringDto wk_sql_item = new StringDto(512);                 /* 動的SQLバッファ */

        if (DBG_LOG) {
            C_DbgStart("MS顧客制度情報更新（退会）処理");
        }

        /*----------------------------------*/
        /*---MS顧客制度情報更新（退会）処理---------*/
        /*----------------------------------*/
        /* ＳＱＬを生成する */
        memset(wk_sql_buf, 0x00, sizeof(wk_sql_buf));
        strcpy(wk_sql_buf, "UPDATE MS顧客制度情報  SET ");

        /* ＥＣ会員フラグ */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, "ＥＣ会員フラグ = %d,", 0);
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
                        "最終更新プログラムＩＤ = '%s', " +
                        "アプリ会員フラグ = 0 "
                , h_bat_yyyymmdd, h_programid);
        strcat(wk_sql_buf, wk_sql_item);

        /* WHERE句 */
        memset(wk_sql_item, 0x00, sizeof(wk_sql_item));
        sprintf(wk_sql_item, " WHERE 顧客番号 = %s", h_uid);
        strcat(wk_sql_buf, wk_sql_item);

        if (DBG_LOG) {
            C_DbgMsg("***UpdateKokyakudataForTaikai*** MS顧客制度情報更新 : sqlbuf=[%s]\n", wk_sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql_buf);

//        EXEC SQL PREPARE sql_stat3 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("crm_sql_stat3cur");
        sqlca.sql = str_sql;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE PREPARE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        /* UPDATE文を実行する */
//        EXEC SQL EXECUTE sql_stat3;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return (C_const_NG);
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MS顧客制度情報更新（退会）処理", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokusei                                            */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokusei(EC_RENDO_DATA in_ec_rendo,               */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客属性情報更新処理                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyakuzokusei(EC_RENDO_DATA in_ec_rendo
            , int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();                 /* 関数戻り値                       */
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */
        StringDto custbarcode = new StringDto(30);       /* カスタマーバーコード             */
        StringDto tyoazcode = new StringDto(12);         /* 町字コード                       */
        StringDto wk_kensaku_tel1 = new StringDto(15 + 1);  /* ハイフンなし電話番号１           */
        StringDto wk_kensaku_tel2 = new StringDto(15 + 1);  /* ハイフンなし電話番号２           */
        StringDto wk_kensaku_tel3 = new StringDto(15 + 1);  /* ハイフンなし電話番号３           */
        StringDto wk_kensaku_tel4 = new StringDto(15 + 1);  /* ハイフンなし電話番号４           */
//  int     i_loop;                 /* ループ                           */      /* 2022/10/11 MCCM初版 DEL */

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
        memset(h_telephone3, 0x00, sizeof(h_telephone3));
        memset(h_telephone4, 0x00, sizeof(h_telephone4));
        memset(h_kensaku_denwa_no_3, 0x00, sizeof(h_kensaku_denwa_no_3));
        memset(h_kensaku_denwa_no_4, 0x00, sizeof(h_kensaku_denwa_no_4));
        memset(h_job, 0x00, sizeof(h_job));
        memset(custbarcode, 0x00, sizeof(custbarcode));
        memset(tyoazcode, 0x00, sizeof(tyoazcode));
        memset(wk_kensaku_tel1, 0x00, sizeof(wk_kensaku_tel1));
        memset(wk_kensaku_tel2, 0x00, sizeof(wk_kensaku_tel2));
        memset(wk_kensaku_tel3, 0x00, sizeof(wk_kensaku_tel3));
        memset(wk_kensaku_tel4, 0x00, sizeof(wk_kensaku_tel4));
        memset(h_jitaku_jusho_code, 0x00, sizeof(h_jitaku_jusho_code));
//  i_loop = 0;                                                                 /* 2022/10/11 MCCM初版 DEL */
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

        /*                                               */
        /* ＨＯＳＴ変数セット                            */
        /*                                               */

        /* 郵便番号コード */
        if (strlen(h_zip) != 0 || strlen(h_address) != 0) {                         /* 2022/10/11 MCCM初版 MOD */
            rtn_cd.arr = C_GetPostBarCode(h_address.strDto(),                                    /* 2022/10/11 MCCM初版 MOD */
                    h_zip.strDto(), custbarcode, rtn_status);
            if (rtn_cd.arr != C_const_OK) {
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
            strncpy(h_zip_code.strDto(), custbarcode, sizeof(h_zip_code) - 1);
        }

        /* 2017/03/30 電話番号更新変更 電話番号１:ファイル内容をそのまま更新するように変更 */
        /*    if ( strlen(in_ec_rendo.telephone1) != 0 ) { */
        /* 電話番号１ */
        strncpy(h_telephone1, in_ec_rendo.telephone1, strlen(in_ec_rendo.telephone1));
        /* 連続するハイフンは削除 */
        strchg(h_telephone1.strDto(), "--", "");
        if (strlen(h_telephone1) != 0) {
            /* 電話番号（ハイフンを削除） */
            rtn_cd.arr = C_ConvTelNo(h_telephone1.strDto(), strlen(h_telephone1), wk_kensaku_tel1);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号１)NG %s\n", "");
                    /*---------------------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_ConvTelNo", rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            /* 検索電話番号１ */
            strcpy(h_kensaku_denwa_no_1, wk_kensaku_tel1);

            /* スペース削除 */
            BT_Rtrim(h_telephone1.strDto(), strlen(h_telephone1));
            BT_Rtrim(h_kensaku_denwa_no_1.strDto(), strlen(h_kensaku_denwa_no_1));
        }

        /* 電話番号チェック */
/*
        if (strlen(h_kensaku_denwa_no_1) < 10) {
            memset(h_telephone1, 0x00, sizeof(h_telephone1));
            memset(h_kensaku_denwa_no_1, 0x00, sizeof(h_kensaku_denwa_no_1));
        }
*/
        /*    } */

        /* 2017/03/30 電話番号更新変更 電話番号２:ファイル内容をそのまま更新するように変更 */
        /*    if ( strlen(in_ec_rendo.telephone2) != 0 ) { */
        /* 電話番号２ */
        strncpy(h_telephone2, in_ec_rendo.telephone2, strlen(in_ec_rendo.telephone2));
        /* 連続するハイフンは削除 */
        strchg(h_telephone2.strDto(), "--", "");
        if (strlen(h_telephone2) != 0) {
            /* 電話番号（ハイフンを削除） */
            rtn_cd.arr = C_ConvTelNo(h_telephone2.strDto(), strlen(h_telephone2), wk_kensaku_tel2);
            if (rtn_cd.arr != C_const_OK) {
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------------*/
                    C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号２)NG %s\n", "");
                    /*---------------------------------------------------------------------------*/
                }
                APLOG_WT("903", 0, null, "C_ConvTelNo", rtn_cd, 0, 0, 0, 0);
                /* 処理を終了する */
                return C_const_NG;
            }
            /* 検索電話番号２ */
            strcpy(h_kensaku_denwa_no_2, wk_kensaku_tel2);

            /* スペース削除 */
            BT_Rtrim(h_telephone2.strDto(), strlen(h_telephone2));
            BT_Rtrim(h_kensaku_denwa_no_2.strDto(), strlen(h_kensaku_denwa_no_2));
        }

        /* 電話番号チェック */
/*
        if (strlen(h_kensaku_denwa_no_2) < 10) {
            memset(h_telephone2, 0x00, sizeof(h_telephone2));
            memset(h_kensaku_denwa_no_2, 0x00, sizeof(h_kensaku_denwa_no_2));
        }
*/
        /*    } */

        /* Eメールアドレス1 */
        memcpy(h_email1, HALF_SPACE60, sizeof(HALF_SPACE60));
        /* Eメールアドレス2 */
        memcpy(h_email2, HALF_SPACE60, sizeof(HALF_SPACE60));

        /* 2017/03/30 電話番号更新変更 電話番号３:ファイル内容が未設定の場合更新しないように変更 */
        if (strlen(in_ec_rendo.telephone3) != 0) {
            /* 電話番号３ */
            strncpy(h_telephone3, in_ec_rendo.telephone3, strlen(in_ec_rendo.telephone3));
            /* 連続するハイフンは削除 */
            strchg(h_telephone3.strDto(), "--", "");
            if (strlen(h_telephone3) != 0) {
                /* 電話番号（ハイフンを削除） */
                rtn_cd.arr = C_ConvTelNo(h_telephone3.strDto(), strlen(h_telephone3), wk_kensaku_tel3);
                if (rtn_cd.arr != C_const_OK) {
                    if (DBG_LOG) {
                        /*---------------------------------------------------------------------------*/
                        C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号３)NG %s\n", "");
                        /*---------------------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_ConvTelNo", rtn_cd, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }
                /* 検索電話番号３ */
                strcpy(h_kensaku_denwa_no_3, wk_kensaku_tel3);

                /* スペース削除 */
                BT_Rtrim(h_telephone3.strDto(), strlen(h_telephone3));
                BT_Rtrim(h_kensaku_denwa_no_3.strDto(), strlen(h_kensaku_denwa_no_3));
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------*/
                    C_DbgMsg("電話番号３設定[%s]\n", h_telephone3);
                    C_DbgMsg("検索電話番号３設定[%s]\n", h_kensaku_denwa_no_3);
                    /*---------------------------------------------------------------------*/
                }
            }
            /* 電話番号チェック */
/*
    if (strlen(h_kensaku_denwa_no_3) < 10) {
        memset(h_telephone3, 0x00, sizeof(h_telephone3));
        memset(h_kensaku_denwa_no_3, 0x00, sizeof(h_kensaku_denwa_no_3));
    }
*/
        }

        if (strlen(in_ec_rendo.telephone4) != 0) {
            /* 電話番号４ */
            strncpy(h_telephone4, in_ec_rendo.telephone4, strlen(in_ec_rendo.telephone4));
            /* 連続するハイフンは削除 */
            strchg(h_telephone4.strDto(), "--", "");
            if (strlen(h_telephone4) != 0) {
                /* 電話番号（ハイフンを削除） */
                rtn_cd.arr = C_ConvTelNo(h_telephone4.strDto(), strlen(h_telephone4), wk_kensaku_tel4);
                if (rtn_cd.arr != C_const_OK) {
                    if (DBG_LOG) {
                        /*---------------------------------------------------------------------------*/
                        C_DbgMsg("*** UpdateKokyakuzokusei *** 電話番号変換(電話番号４)NG %s\n", "");
                        /*---------------------------------------------------------------------------*/
                    }
                    APLOG_WT("903", 0, null, "C_ConvTelNo", rtn_cd, 0, 0, 0, 0);
                    /* 処理を終了する */
                    return C_const_NG;
                }
                /* 検索電話番号４ */
                strcpy(h_kensaku_denwa_no_4, wk_kensaku_tel4);

                /* スペース削除 */
                BT_Rtrim(h_telephone4.strDto(), strlen(h_telephone4));
                BT_Rtrim(h_kensaku_denwa_no_4.strDto(), strlen(h_kensaku_denwa_no_4));
                if (DBG_LOG) {
                    /*---------------------------------------------------------------------*/
                    C_DbgMsg("電話番号４設定[%s]\n", h_telephone4);
                    C_DbgMsg("検索電話番号４設定[%s]\n", h_kensaku_denwa_no_4);
                    /*---------------------------------------------------------------------*/
                }
            }

            /* 電話番号チェック */
/*
        if (strlen(h_kensaku_denwa_no_4) < 10) {
            memset(h_telephone4, 0x00, sizeof(h_telephone4));
            memset(h_kensaku_denwa_no_4, 0x00, sizeof(h_kensaku_denwa_no_4));
        }
*/
        }

        /* 職業 */
        memcpy(h_job, HALF_SPACE40, sizeof(HALF_SPACE40));

        /* 自宅住所コード */
        memcpy(h_jitaku_jusho_code, HALF_ZERO11, sizeof(HALF_SPACE11));

        /*                                                   */
        /* MM顧客属性情報追加                                */
        /* 顧客情報なし                                      */
        /*                                                   */
        if (hi_msks_sw.intVal() == 0 && hi_mmki_sw.intVal() == 0) {
            rtn_cd.arr = InsertKokyakuzokuseidata();
            if (rtn_cd.arr != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd.arr == C_const_OK) {
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
        memset(MmkzData, 0x00, sizeof(MmkzData));
        memset(upd_h_zip, 0x00, sizeof(upd_h_zip));
        memset(upd_h_zip_code, 0x00, sizeof(upd_h_zip_code));
        memset(upd_h_address1, 0x00, sizeof(upd_h_address1));
        memset(upd_h_address2, 0x00, sizeof(upd_h_address2));
//    memset(upd_h_address3,           0x00, sizeof(upd_h_address3));                                          /* 2022/01/23 MCCM初版 DEL */
        memset(upd_h_address, 0x00, sizeof(upd_h_address));                                             /* 2022/10/12 MCCM初版 ADD */
        memset(upd_h_telephone1, 0x00, sizeof(upd_h_telephone1));
        memset(upd_h_telephone2, 0x00, sizeof(upd_h_telephone2));
        memset(upd_h_telephone3, 0x00, sizeof(upd_h_telephone3));
        memset(upd_h_telephone4, 0x00, sizeof(upd_h_telephone4));
        memset(upd_h_kensaku_denwa_no_1, 0x00, sizeof(upd_h_kensaku_denwa_no_1));
        memset(upd_h_kensaku_denwa_no_2, 0x00, sizeof(upd_h_kensaku_denwa_no_2));
        memset(upd_h_kensaku_denwa_no_3, 0x00, sizeof(upd_h_kensaku_denwa_no_3));
        memset(upd_h_kensaku_denwa_no_4, 0x00, sizeof(upd_h_kensaku_denwa_no_4));

        /* 顧客番号セット */
        strcpy(MmkzData.kokyaku_no, h_uid);
        MmkzData.kokyaku_no.len = strlen(MmkzData.kokyaku_no);

        /* 顧客属性情報取得 */
        rtn_cd.arr = cmBTfuncB.C_GetCzMaster(MmkzData, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null,
                    "C_GetCzMaster", rtn_cd, 0, 0, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /*--------------------------*/
        /* ＨＯＳＴ変数セット(元値) */
        /*--------------------------*/
        /* 2017/03/30 電話番号更新変更 電話番号1,2:ファイル内容をそのまま更新するように変更 */
        /*                             電話番号3:ファイル内容が未設定の場合更新しないように変更 */
        /*memcpy(upd_h_zip,        MmkzData.yubin_no,    sizeof(MmkzData.yubin_no));*/
        /*memcpy(upd_h_zip_code,   MmkzData.yubin_no_cd, sizeof(MmkzData.yubin_no_cd));*/
        /*memcpy(upd_h_address1,   MmkzData.jusho_1,     sizeof(MmkzData.jusho_1));*/
        /*memcpy(upd_h_address2,   MmkzData.jusho_2,     sizeof(MmkzData.jusho_2));*/
        /*memcpy(upd_h_address3,   MmkzData.jusho_3,     sizeof(MmkzData.jusho_3));*/
        /*memcpy(upd_h_telephone1, MmkzData.denwa_no_1,  sizeof(MmkzData.denwa_no_1));*/
        /*memcpy(upd_h_telephone2, MmkzData.denwa_no_2,  sizeof(MmkzData.denwa_no_2));*/
        memcpy(upd_h_telephone3, MmkzData.denwa_no_3, sizeof(MmkzData.denwa_no_3));
        memcpy(upd_h_telephone4, MmkzData.denwa_no_4, sizeof(MmkzData.denwa_no_4));
        /*memcpy(upd_h_kensaku_denwa_no_1, MmkzData.kensaku_denwa_no_1 , sizeof(MmkzData.kensaku_denwa_no_1));*/
        /*memcpy(upd_h_kensaku_denwa_no_2, MmkzData.kensaku_denwa_no_2 , sizeof(MmkzData.kensaku_denwa_no_2));*/
        memcpy(upd_h_kensaku_denwa_no_3, MmkzData.kensaku_denwa_no_3, sizeof(MmkzData.kensaku_denwa_no_3));
        memcpy(upd_h_kensaku_denwa_no_4, MmkzData.kensaku_denwa_no_4, sizeof(MmkzData.kensaku_denwa_no_4));
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("ＨＯＳＴ変数：電話番号３[%s]\n", upd_h_telephone3);
            C_DbgMsg("ＨＯＳＴ変数：電話番号４[%s]\n", upd_h_telephone4);
            C_DbgMsg("ＨＯＳＴ変数：検索電話番号３[%s]\n", upd_h_kensaku_denwa_no_3);
            C_DbgMsg("ＨＯＳＴ変数：検索電話番号４[%s]\n", upd_h_kensaku_denwa_no_4);
            /*---------------------------------------------------------------------*/
        }

        /*----------------------*/
        /* ＨＯＳＴ変数再セット */
        /*----------------------*/
        /* 郵便番号 */
        memcpy(upd_h_zip, h_zip, sizeof(h_zip));
        /* 住所1 */
        memcpy(upd_h_address1, h_address1, sizeof(h_address1));
        /* 住所2 */
        memcpy(upd_h_address2, h_address2, sizeof(h_address2));
//    /* 住所3 */
//    memcpy(upd_h_address3, h_address3, sizeof(h_address3));                         /* 2022/10/12 MCCM初版 ADD */
        /* 住所 */
        sprintf(upd_h_address, "%s%s", upd_h_address1, upd_h_address2); /* 2022/10/12 MCCM初版 ADD */
        /* 郵便番号コード */
        /* 住所データありの場合、郵便番号コード取得 */
        rtn_cd.arr = C_GetPostBarCode(upd_h_address.strDto(),                                          /* 2022/10/11 MCCM初版 MOD */
                upd_h_zip.strDto(), custbarcode, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
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
        strncpy(upd_h_zip_code.strDto(), custbarcode, sizeof(upd_h_zip_code) - 1);

        /* 2017/03/30 電話番号更新変更 電話番号１:ファイル内容をそのまま更新するように変更 */
        /* 電話番号１,検索電話番号１ */
        /*if ( 0 == strlen(h_telephone1) ){ } */
        /*else{ */
        /* 電話番号１ */
        strncpy(upd_h_telephone1, h_telephone1, strlen(h_telephone1));
        /* 検索電話番号１ */
        strcpy(upd_h_kensaku_denwa_no_1, h_kensaku_denwa_no_1);
        /*} */

        /* 2017/03/30 電話番号更新変更 電話番号２:ファイル内容をそのまま更新するように変更 */
        /* 電話番号２,検索電話番号２ */
        /*if ( 0 == strlen(h_telephone2) ){ } */
        /*else{ */
        /* 電話番号２ */
        strncpy(upd_h_telephone2, h_telephone2, strlen(h_telephone2));
        /* 検索電話番号２ */
        strcpy(upd_h_kensaku_denwa_no_2, h_kensaku_denwa_no_2);
        /*} */

        /* 2017/03/30 電話番号更新変更 電話番号３:ファイル内容が未設定の場合更新しないように変更 */
        /* 電話番号３,検索電話番号３ */
        if (0 == strlen(h_telephone3)) {
        } else {
            memset(upd_h_telephone3, 0x00, sizeof(upd_h_telephone3));
            memset(upd_h_kensaku_denwa_no_3, 0x00, sizeof(upd_h_kensaku_denwa_no_3));

            /* 電話番号３ */
            strncpy(upd_h_telephone3, h_telephone3, strlen(h_telephone3));
            /* 検索電話番号３ */
            strcpy(upd_h_kensaku_denwa_no_3, h_kensaku_denwa_no_3);
            if (DBG_LOG) {
                /*---------------------------------------------------------------------*/
                C_DbgMsg("ＨＯＳＴ変数：(ファイル)電話番号３[%s]\n", h_telephone3);
                C_DbgMsg("ＨＯＳＴ変数：(ファイル)検索電話番号３[%s]\n", h_kensaku_denwa_no_3);
                C_DbgMsg("ＨＯＳＴ変数：(更新)電話番号３[%s]\n", upd_h_telephone3);
                C_DbgMsg("ＨＯＳＴ変数：(更新)検索電話番号３[%s]\n", upd_h_kensaku_denwa_no_3);
                /*---------------------------------------------------------------------*/
            }
        }

        /* 電話番号４,検索電話番号４ */
        if (0 == strlen(h_telephone4)) {
        } else {
            memset(upd_h_telephone4, 0x00, sizeof(upd_h_telephone4));
            memset(upd_h_kensaku_denwa_no_4, 0x00, sizeof(upd_h_kensaku_denwa_no_4));

            /* 電話番号４" */
            strncpy(upd_h_telephone4, h_telephone4, strlen(h_telephone4));
            /* 検索電話番号４ */
            strcpy(upd_h_kensaku_denwa_no_4, h_kensaku_denwa_no_4);
            if (DBG_LOG) {
                /*---------------------------------------------------------------------*/
                C_DbgMsg("ＨＯＳＴ変数：(ファイル)電話番号４[%s]\n", h_telephone4);
                C_DbgMsg("ＨＯＳＴ変数：(ファイル)検索電話番号４[%s]\n", h_kensaku_denwa_no_4);
                C_DbgMsg("ＨＯＳＴ変数：(更新)電話番号４[%s]\n", upd_h_telephone4);
                C_DbgMsg("ＨＯＳＴ変数：(更新)検索電話番号４[%s]\n", upd_h_kensaku_denwa_no_4);
                /*---------------------------------------------------------------------*/
            }
        }

        /*                                       */
        /* MS顧客属性情報更新                    */
        /* 顧客情報ありの場合                    */
        /*                                       */
        rtn_cd.arr = UpdateKokyakuzokuseidata();
        if (rtn_cd.arr != C_const_OK) {
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

/******************************************************************************/
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

    /******************************************************************************/
    public int InsertKokyakuzokuseidata() {
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */                                         /* 2022/10/12 MCCM初版 ADD */
        IntegerDto precde = new IntegerDto();                 /* 都道府県コード                   */                                         /* 2022/10/12 MCCM初版 ADD */
        IntegerDto rtn_cd = new IntegerDto();                 /* 関数戻り値                       */                                         /* 2022/10/12 MCCM初版 ADD */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報追加");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(MmkzData, 0x00, sizeof(MmkzData));
        /* 顧客番号       */
        memcpy(MmkzData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
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
//    /* 住所３ */
//    if ( strlen(h_address3) == 0 ) {                                                             /* 2023/01/23 MCCM初版 DEL */
//        memcpy(h_address3, " ", sizeof(h_address3) - 1);                                         /* 2023/01/23 MCCM初版 DEL */
//    }
        /* 住所 */                                                                                     /* 2022/10/12 MCCM初版 ADD */
        sprintf(h_address, "%s%s", h_address1, h_address2);                                      /* 2022/10/12 MCCM初版 ADD */
        /* 電話番号１ */
        if (strlen(h_telephone1) == 0) {
            memcpy(h_telephone1, " ", sizeof(h_telephone1) - 1);
        }
        /* 電話番号２ */
        if (strlen(h_telephone2) == 0) {
            memcpy(h_telephone2, " ", sizeof(h_telephone2) - 1);
        }
        /* 電話番号３ */
        if (strlen(h_telephone3) == 0) {
            memcpy(h_telephone3, " ", sizeof(h_telephone3) - 1);
        }
        /* 電話番号４ */
        if (strlen(h_telephone4) == 0) {
            memcpy(h_telephone4, " ", sizeof(h_telephone4) - 1);
        }
        /* 検索電話番号１ */
        if (strlen(h_kensaku_denwa_no_1) == 0) {
            memcpy(h_kensaku_denwa_no_1, " ", sizeof(h_kensaku_denwa_no_1) - 1);
        }
        /* 検索電話番号２ */
        if (strlen(h_kensaku_denwa_no_2) == 0) {
            memcpy(h_kensaku_denwa_no_2, " ", sizeof(h_kensaku_denwa_no_2) - 1);
        }
        /* 検索電話番号３ */
        if (strlen(h_kensaku_denwa_no_3) == 0) {
            memcpy(h_kensaku_denwa_no_3, " ", sizeof(h_kensaku_denwa_no_3) - 1);
        }
        /* 検索電話番号４ */
        if (strlen(h_kensaku_denwa_no_4) == 0) {
            memcpy(h_kensaku_denwa_no_4, " ", sizeof(h_kensaku_denwa_no_4) - 1);
        }

        /* 都道府県コード */
        rtn_cd.arr = C_GetPrefecturesCode(h_address.strVal(),
                precde, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
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
        h_todofuken_cd.arr = precde;

        /* INSERTする */
//        EXEC SQL INSERT INTO MM顧客属性情報
//            (  顧客番号,
//                    休眠フラグ,
//                    郵便番号,
//                    郵便番号コード,
//                    /*                      住所１,                                                                              *//* 2022/10/12 MCCM初版 DEL */
//                    /*                      住所２,                                                                              *//* 2022/10/12 MCCM初版 DEL */
//                    /*                      住所３,                                                                              *//* 2022/10/12 MCCM初版 DEL */
//                    住所,                                                                                  /* 2022/10/12 MCCM初版 ADD */
//                    電話番号１,
//                    電話番号２,
//                    検索電話番号１,
//                    検索電話番号２,
//                    Ｅメールアドレス１,
//                    Ｅメールアドレス２,
//                    電話番号３,
//                    電話番号４,
//                    検索電話番号３,
//                    検索電話番号４,
//                    職業,
//                    勤務区分,
//                    自宅住所コード,
//                    作業企業コード,
//                    作業者ＩＤ,
//                    作業年月日,
//                    作業時刻,
//                    バッチ更新日,
//                    最終更新日,
//                    最終更新日時,
//                    最終更新プログラムＩＤ,
//                    都道府県コード)                                                                        /* 2022/10/12 MCCM初版 ADD */
//        VALUES (  :MmkzData.kokyaku_no,
//            0,
//                        :h_zip,
//                        :h_zip_code,
//        /*                      :h_address1,                                                                         *//* 2022/10/12 MCCM初版 DEL */
//        /*                      :h_address2,                                                                         *//* 2022/10/12 MCCM初版 DEL */
//        /*                      :h_address3,                                                                         *//* 2022/10/12 MCCM初版 DEL */
//                        :h_address,                                                                            /* 2022/10/12 MCCM初版 ADD */
//                        :h_telephone1,
//                        :h_telephone2,
//                        :h_kensaku_denwa_no_1,
//                        :h_kensaku_denwa_no_2,
//                        :h_email1,
//                        :h_email2,
//                        :h_telephone3,
//                        :h_telephone4,
//                        :h_kensaku_denwa_no_3,
//                        :h_kensaku_denwa_no_4,
//                        :h_job,
//            0,
//                        :h_jitaku_jusho_code,
//            0,
//            0,
//            0,
//            0,
//                        :h_bat_yyyymmdd,
//                        :h_bat_yyyymmdd,
//            sysdate,
//                        :h_programid,
//                        :h_todofuken_cd);                                                                      /* 2022/10/12 MCCM初版 ADD */

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = "INSERT INTO MM顧客属性情報 (  顧客番号," +
                " 休眠フラグ," +
                " 郵便番号," +
                " 郵便番号コード," +
                " 住所," +
                " 電話番号１," +
                " 電話番号２," +
                " 検索電話番号１," +
                " 検索電話番号２," +
                " Ｅメールアドレス１," +
                " Ｅメールアドレス２," +
                " 電話番号３," +
                " 電話番号４," +
                " 検索電話番号３," +
                " 検索電話番号４," +
                " 職業," +
                " 勤務区分," +
                " 自宅住所コード," +
                " 作業企業コード," +
                " 作業者ＩＤ," +
                " 作業年月日," +
                " 作業時刻," +
                " バッチ更新日," +
                " 最終更新日," +
                " 最終更新日時," +
                " 最終更新プログラムＩＤ," +
                " 都道府県コード)" +
                " VALUES (?," +
                " 0," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " ?," +
                " 0," +
                " ?," +
                " 0," +
                " 0," +
                " 0," +
                " 0," +
                " ?," +
                " ?," +
                " sysdate()," +
                " ?," +
                " ?)";
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(MmkzData.kokyaku_no, h_zip, h_zip_code, h_address, h_telephone1, h_telephone2, h_kensaku_denwa_no_1,
                h_kensaku_denwa_no_2, h_email1, h_email2, h_telephone3, h_telephone4, h_kensaku_denwa_no_3, h_kensaku_denwa_no_4,
                h_job, h_jitaku_jusho_code, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_todofuken_cd);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkzData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
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

/******************************************************************************/
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

    /******************************************************************************/
    public int UpdateKokyakuzokuseidata() {
        IntegerDto rtn_status = new IntegerDto();             /* 関数ステータス                   */                                         /* 2022/10/12 MCCM初版 ADD */
        IntegerDto upd_precde = new IntegerDto();             /* 都道府県コード                   */                                         /* 2022/10/12 MCCM初版 ADD */
        IntegerDto rtn_cd = new IntegerDto();                 /* 関数戻り値                       */                                         /* 2022/10/12 MCCM初版 ADD */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報更新");
            C_DbgMsg("郵便番号[%s]\n", upd_h_zip);
            C_DbgMsg("郵便番号コード[%s]\n", upd_h_zip_code);
            C_DbgMsg("住所１[%s]\n", upd_h_address1);
            C_DbgMsg("住所２[%s]\n", upd_h_address2);
            //    C_DbgMsg( "住所３[%s]\n",         upd_h_address3 );                                                      /* 2023/01/23 MCCM初版 DEL */
            C_DbgMsg("住所[%s]\n", upd_h_address);
            C_DbgMsg("電話番号１[%s]\n", upd_h_telephone1);
            C_DbgMsg("電話番号２[%s]\n", upd_h_telephone2);
            C_DbgMsg("電話番号３[%s]\n", upd_h_telephone3);
            C_DbgMsg("電話番号４[%s]\n", upd_h_telephone4);
            C_DbgMsg("検索電話番号１[%s]\n", upd_h_kensaku_denwa_no_1);
            C_DbgMsg("検索電話番号２[%s]\n", upd_h_kensaku_denwa_no_2);
            C_DbgMsg("検索電話番号３[%s]\n", upd_h_kensaku_denwa_no_3);
            C_DbgMsg("検索電話番号４[%s]\n", upd_h_kensaku_denwa_no_4);
            /*---------------------------------------------------------------------*/
        }

        /* 2022/10/12 MCCM初版 ADD START */
        /* 都道府県コード */
        rtn_cd.arr = C_GetPrefecturesCode(upd_h_address.strVal(),
                upd_precde, rtn_status);
        if (rtn_cd.arr != C_const_OK) {
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
        upd_h_todofuken_cd.arr = upd_precde;
        /* 2022/10/12 MCCM初版 ADD END */
        /* MM顧客属性情報更新処理 */
//        EXEC SQL UPDATE MM顧客属性情報
//        SET 郵便番号               =  :upd_h_zip,
//            郵便番号コード         =  :upd_h_zip_code,
//            /*          住所１                 =  :upd_h_address1,                                                       *//* 2022/10/12 MCCM初版 DEL */
//            /*          住所２                 =  :upd_h_address2,                                                       *//* 2022/10/12 MCCM初版 DEL */
//            /*          住所３                 =  :upd_h_address3,                                                       *//* 2022/10/12 MCCM初版 DEL */
//            住所                   =  :upd_h_address,                                                          /* 2022/10/12 MCCM初版 ADD */
//            電話番号１             =  :upd_h_telephone1,
//            電話番号２             =  :upd_h_telephone2,
//            電話番号３             =  :upd_h_telephone3,
//            電話番号４             =  :upd_h_telephone4,
//            検索電話番号１         =  :upd_h_kensaku_denwa_no_1,
//            検索電話番号２         =  :upd_h_kensaku_denwa_no_2,
//            検索電話番号３         =  :upd_h_kensaku_denwa_no_3,
//            検索電話番号４         =  :upd_h_kensaku_denwa_no_4,
//            バッチ更新日           =  :h_bat_yyyymmdd,
//            最終更新日             =  :h_bat_yyyymmdd,
//            最終更新日時           =  sysdate,
//            最終更新プログラムＩＤ = :h_programid,
//            都道府県コード         = :upd_h_todofuken_cd                                                       /* 2022/10/12 MCCM初版 ADD */
//        WHERE 顧客番号       = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客属性情報" +
                " SET 郵便番号           =  ?," +
                " 郵便番号コード         =  ?, " +
                " 住所                   =  ?, " +
                " 電話番号１             =  ?," +
                " 電話番号２             =  ?," +
                " 電話番号３             =  ?," +
                " 電話番号４             =  ?," +
                " 検索電話番号１         =  ?," +
                " 検索電話番号２         =  ?," +
                " 検索電話番号３         =  ?," +
                " 検索電話番号４         =  ?," +
                " バッチ更新日           =  ?," +
                " 最終更新日             =  ?," +
                " 最終更新日時           =  sysdate()," +
                " 最終更新プログラムＩＤ =  ?," +
                " 都道府県コード         =  ?" +
                " WHERE 顧客番号         = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.restAndExecute(upd_h_zip, upd_h_zip_code, upd_h_address, upd_h_telephone1, upd_h_telephone2, upd_h_telephone3, upd_h_telephone4,
                upd_h_kensaku_denwa_no_1, upd_h_kensaku_denwa_no_2, upd_h_kensaku_denwa_no_3, upd_h_kensaku_denwa_no_4, h_bat_yyyymmdd,
                h_bat_yyyymmdd, h_programid, upd_h_todofuken_cd.intVal(), h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokuseiForTaikai                                   */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo,      */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               顧客属性情報更新処理（退会）                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyakuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo
            , int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();                 /* 関数戻り値                       */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客属性情報更新処理");
            /*---------------------------------------------------------------------*/
        }

        /*                                                   */
        /* MM顧客属性情報追加                                */
        /* 顧客情報なし                                      */
        /*                                                   */
        if (hi_msks_sw.intVal() == 0 && hi_mmki_sw.intVal() == 0) {
            rtn_cd.arr = InsertKokyakuzokuseidataForTaikai();
            if (rtn_cd.arr != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }

            /*                                                   */
            /* 顧客情報なしの場合はここで終了                    */
            /*                                                   */
            if (rtn_cd.arr == C_const_OK) {
                /* パンチエラー番号セット */
                punch_errno = C_PUNCH_OK;

                /* 処理を終了する */
                return C_const_OK;
            }
        } else if (hi_msks_sw.intVal() == 0 || hi_mmki_sw.intVal() == 0) {
            /* 処理を終了する */
            return (C_const_NG);
        }

        /*                                                   */
        /* MM顧客属性情報追加                                */
        /* 顧客情報なし                                      */
        /* 2016/08/17 Eメールアドレス４クリア追加            */
        /*                                                   */
        rtn_cd.arr = UpdateKokyakuzokuseidataForTaikai();
        if (rtn_cd.arr != C_const_OK) {
            /* 処理を終了する */
            return C_const_NG;
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客属性情報更新処理（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKokyakuzokuseidataForTaikai                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKokyakuzokuseidataForTaikai()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客属性情報追加（退会）                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*              -2  ： 重複エラー                                             */
    /*                                                                            */

    /******************************************************************************/
    public int InsertKokyakuzokuseidataForTaikai() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報追加（退会）");
            /*---------------------------------------------------------------------*/
        }

        /* 初期化 */
        memset(MmkzData, 0x00, sizeof(MmkzData));
        /* 顧客番号       */
        memcpy(MmkzData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
        MmkzData.kokyaku_no.len = strlen(h_uid);

        /* INSERTする */
//        EXEC SQL INSERT INTO MM顧客属性情報
//            (  顧客番号,
//                    勤務区分,
//                    作業企業コード,
//                    作業者ＩＤ,
//                    作業年月日,
//                    作業時刻,
//                    バッチ更新日,
//                    最終更新日,
//                    最終更新日時,
//                    最終更新プログラムＩＤ)
//        VALUES (  :MmkzData.kokyaku_no,
//            0,
//            0,
//            0,
//            0,
//            0,
//                        :h_bat_yyyymmdd,
//                        :h_bat_yyyymmdd,
//            sysdate,
//                        :h_programid);

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = "INSERT INTO MM顧客属性情報" +
                " (  顧客番号," +
                " 勤務区分," +
                " 作業企業コード," +
                " 作業者ＩＤ," +
                " 作業年月日," +
                " 作業時刻," +
                " バッチ更新日," +
                " 最終更新日," +
                " 最終更新日時," +
                " 最終更新プログラムＩＤ)" +
                " VALUES (  ?," +
                " 0," +
                " 0," +
                " 0," +
                " 0," +
                " 0," +
                " ?," +
                " ?," +
                " sysdate()," +
                " ?)";
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(MmkzData.kokyaku_no, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkzData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客属性情報追加（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKigyobetuzokusei                                          */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKigyobetuzokusei(EC_RENDO_DATA in_ec_rendo,             */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               １０．内部関数（顧客企業別属性情報更新）                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKigyobetuzokusei(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();                /* 関数戻り値                       */
//  int        rtn_status;            /* 関数ステータス                   */          /* 2022/10/11 MCCM初版 DEL */
        int wk_dm_zyushin_kahi;    /* ＤＭ止め区分                     */
        int wk_email_zyushin_kahi; /* Ｅメール止め区分                 */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客企業別属性情報更新");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd.arr = C_const_OK;
//  rtn_status = C_const_Stat_OK;                                                     /* 2022/10/11 MCCM初版 DEL */

        /*------------------------------------*/
        /*---追加／更新用のホスト変数を編集---*/
        /*------------------------------------*/
        /* 入会年月日 */
        if (MmkgData.nyukai_ymd.intVal() == 0 || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal()) { /* 基準日を設定 */
            MmkgData.nyukai_ymd = h_kijyun_yyyymmdd;
        }
        /* 顧客番号セット */
        memcpy(MmkgData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
        MmkgData.kokyaku_no.len = strlen(h_uid);

        /* ＤＭ止め区分 */
        wk_dm_zyushin_kahi = atoi(in_ec_rendo.dm_zyushin_kahi);
        if (wk_dm_zyushin_kahi == 3000 || wk_dm_zyushin_kahi == 3031) {
            MmkgData.dm_tome_kbn.arr = wk_dm_zyushin_kahi;
        } else {
            /* 登録時の設定 */
            if (hi_mmkg_sw.intVal() == 0) {
                MmkgData.dm_tome_kbn.arr = 3099;
            }
        }

        /* Ｅメール止め区分 */
        wk_email_zyushin_kahi = atoi(in_ec_rendo.email_zyushin_kahi);
        if (wk_email_zyushin_kahi == 5000 || wk_email_zyushin_kahi == 5031) {
            MmkgData.email_tome_kbn.arr = wk_email_zyushin_kahi;
        } else {
            /* 登録時の設定 */
            if (hi_mmkg_sw.intVal() == 0) {
                MmkgData.email_tome_kbn.arr = 5099;
            }
        }

        /*------------------------------------------------------*/
        /*---顧客企業別属性情報存在を判定し、追加／更新を実行---*/
        /*------------------------------------------------------*/
        if (hi_mmkg_sw.intVal() == 0) {
            rtn_cd.arr = InsertKigyobetuzokuseidata();
            if (rtn_cd.arr != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }
        } else {
            rtn_cd.arr = UpdateKigyobetuzokuseidata();
            if (rtn_cd.arr != C_const_OK) {
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

/******************************************************************************/
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

    /******************************************************************************/
    public int InsertKigyobetuzokuseidata() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報追加");
            /*---------------------------------------------------------------------*/
        }
        /* INSERTする */
//        EXEC SQL INSERT INTO MM顧客企業別属性情報
//            (  顧客番号,
//                    企業コード,
//                    入会年月日,
//                    退会年月日,
//                    ＴＥＬ止め区分,
//                    ＤＭ止め区分,
//                    Ｅメール止め区分,
//                    携帯ＴＥＬ止め区分,
//                    携帯Ｅメール止め区分,
//                    作業企業コード,
//                    作業者ＩＤ,
//                    作業年月日,
//                    作業時刻,
//                    バッチ更新日,
//                    最終更新日,
//                    最終更新日時,
//                    最終更新プログラムＩＤ)
//        VALUES (  :MmkgData.kokyaku_no,
//                          :h_kigyo_cd,
//                          :MmkgData.nyukai_ymd,
//            0,
//            0,
//                          :MmkgData.dm_tome_kbn,
//                          :MmkgData.email_tome_kbn,
//            0,
//            0,
//            0,
//            0,
//            0,
//            0,
//                          :h_bat_yyyymmdd,
//                          :h_bat_yyyymmdd,
//            sysdate,
//                          :h_programid);

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MM顧客企業別属性情報 ( 顧客番号, 企業コード, 入会年月日, 退会年月日, ＴＥＬ止め区分, " +
                "ＤＭ止め区分, Ｅメール止め区分, 携帯ＴＥＬ止め区分, 携帯Ｅメール止め区分, 作業企業コード, 作業者ＩＤ, 作業年月日, 作業時刻," +
                " バッチ更新日, 最終更新日, 最終更新日時, 最終更新プログラムＩＤ) \n" +
                "VALUES(?, ?, ?, 0, 0, ?, ?, 0, 0, 0, 0, 0, 0, ?, ?, sysdate(), ?)");
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(MmkgData.kokyaku_no, h_kigyo_cd, MmkgData.nyukai_ymd, MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn,
                h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkgData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
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

/******************************************************************************/
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

    /******************************************************************************/
    public int UpdateKigyobetuzokuseidata() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報更新");
            /*---------------------------------------------------------------------*/
        }

        /* MM顧客企業別属性情報更新 */
//        EXEC SQL UPDATE MM顧客企業別属性情報
//        SET 企業コード             = DECODE(NVL(企業コード,0),0,:h_ps_corpid,企業コード),
//        入会年月日             = :MmkgData.nyukai_ymd,
//            ＤＭ止め区分           = :MmkgData.dm_tome_kbn,
//            Ｅメール止め区分       = :MmkgData.email_tome_kbn,
//            バッチ更新日           = :h_bat_yyyymmdd,
//            最終更新日             = :h_bat_yyyymmdd,
//            最終更新日時           = SYSDATE,
//            最終更新プログラムＩＤ = :h_programid
//        WHERE 企業コード = :MmkgData.kigyo_cd
//        AND 顧客番号   = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客企業別属性情報 " +
                " SET 企業コード             = DECODE(NVL(企業コード,0),'0',?,企業コード), " +
                " 入会年月日             = ?, " +
                " ＤＭ止め区分             = ?, " +
                " Ｅメール止め区分             = ?, " +
                " バッチ更新日 = ?, " +
                " 最終更新日 = ?, " +
                " 最終更新日時 = SYSDATE(), " +
                " 最終更新プログラムＩＤ = ? " +
                " WHERE 企業コード = ? AND 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_ps_corpid, MmkgData.nyukai_ymd, MmkgData.dm_tome_kbn, MmkgData.email_tome_kbn, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, MmkgData.kigyo_cd, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKigyobetuzokuseiForTaikai                                 */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKigyobetuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo,    */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               １０．内部関数（顧客企業別属性情報更新）                     */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKigyobetuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
        IntegerDto rtn_cd = new IntegerDto();                /* 関数戻り値                       */
//  int        rtn_status;            /* 関数ステータス                   */          /* 2022/10/11 MCCM初版 DEL */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("顧客企業別属性情報更新（退会）");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        rtn_cd.arr = C_const_OK;
//  rtn_status = C_const_Stat_OK;                                                     /* 2022/10/11 MCCM初版 DEL */

        /*------------------------------------*/
        /*---追加／更新用のホスト変数を編集---*/
        /*------------------------------------*/
        /* 入会年月日 */
        MmkgData.nyukai_ymd.arr = 0;
        /* 顧客番号セット */
        memcpy(MmkgData.kokyaku_no, h_uid, sizeof(h_uid) - 1);
        MmkgData.kokyaku_no.len = strlen(h_uid);

        /*------------------------------------------------------*/
        /*---顧客企業別属性情報存在を判定し、追加／更新を実行---*/
        /*------------------------------------------------------*/
        if (hi_mmkg_sw.intVal() == 0) {
            rtn_cd.arr = InsertKigyobetuzokuseidataForTaikai();
            if (rtn_cd.arr != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }
        } else {
            rtn_cd.arr = UpdateKigyobetuzokuseidataForTaikai();
            if (rtn_cd.arr != C_const_OK) {
                /* 処理を終了する */
                return C_const_NG;
            }
        }

        /* パンチエラー番号セット */
        punch_errno = C_PUNCH_OK;

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("顧客企業別属性情報更新（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }

        /* 処理を終了する */
        return C_const_OK;

        /*-----UpdateKigyobetuzokusei Bottom------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsertKigyobetuzokuseidataForTaikai                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  InsertKigyobetuzokuseidataForTaikai()                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報追加（退会）                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常(重複エラー含む)                                   */
    /*                                                                            */

    /******************************************************************************/
    public int InsertKigyobetuzokuseidataForTaikai() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報追加（退会）");
            /*---------------------------------------------------------------------*/
        }
        /* INSERTする */
//        EXEC SQL INSERT INTO MM顧客企業別属性情報
//            (  顧客番号,
//                    企業コード,
//                    入会年月日,
//                    退会年月日,
//                    ＴＥＬ止め区分,
//                    ＤＭ止め区分,
//                    Ｅメール止め区分,
//                    携帯ＴＥＬ止め区分,
//                    携帯Ｅメール止め区分,
//                    作業企業コード,
//                    作業者ＩＤ,
//                    作業年月日,
//                    作業時刻,
//                    バッチ更新日,
//                    最終更新日,
//                    最終更新日時,
//                    最終更新プログラムＩＤ)
//        VALUES (  :MmkgData.kokyaku_no,
//                          :h_kigyo_cd,
//                          :MmkgData.nyukai_ymd,
//                          :h_taikai_date,
//            0,
//            3092,
//            5092,
//            0,
//            0,
//            0,
//            0,
//            0,
//            0,
//                          :h_bat_yyyymmdd,
//                          :h_bat_yyyymmdd,
//            sysdate,
//                          :h_programid);

        StringDto WRKSQL = new StringDto();
        WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO MM顧客企業別属性情報\n" +
                "            (  顧客番号," +
                "                    企業コード," +
                "                    入会年月日," +
                "                    退会年月日," +
                "                    ＴＥＬ止め区分," +
                "                    ＤＭ止め区分," +
                "                    Ｅメール止め区分," +
                "                    携帯ＴＥＬ止め区分," +
                "                    携帯Ｅメール止め区分," +
                "                    作業企業コード," +
                "                    作業者ＩＤ," +
                "                    作業年月日," +
                "                    作業時刻," +
                "                    バッチ更新日," +
                "                    最終更新日," +
                "                    最終更新日時," +
                "                    最終更新プログラムＩＤ)" +
                "        VALUES (  ?," +
                "                          ?," +
                "                          ?," +
                "                          ?," +
                "            0," +
                "            3092," +
                "            5092," +
                "            0," +
                "            0," +
                "            0," +
                "            0," +
                "            0," +
                "            0," +
                "            ?," +
                "            ?," +
                "            sysdate()," +
                "            ?)");
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(MmkgData.kokyaku_no, h_kigyo_cd, MmkgData.nyukai_ymd, h_taikai_date, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid);
        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", MmkgData.kokyaku_no.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);
            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客企業別属性情報追加（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;

        /*-----InsertKigyobetuzokuseidataForTaikai Bottom--------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKigyobetuzokuseidataForTaikai                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKigyobetuzokuseidataForTaikai()                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客企業別属性情報更新（退会）                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKigyobetuzokuseidataForTaikai() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客企業別属性情報更新（退会）");
            /*---------------------------------------------------------------------*/
        }

        /* MM顧客企業別属性情報更新 */
//        EXEC SQL UPDATE MM顧客企業別属性情報
//        SET 企業コード             = DECODE(NVL(企業コード,0),0,:h_ps_corpid,企業コード),
//        退会年月日             = :h_taikai_date,
//            ＤＭ止め区分           = 3092,
//            Ｅメール止め区分       = 5092,
//            バッチ更新日           = :h_bat_yyyymmdd,
//            最終更新日             = :h_bat_yyyymmdd,
//            最終更新日時           = SYSDATE,
//            最終更新プログラムＩＤ = :h_programid
//        WHERE 企業コード = :MmkgData.kigyo_cd
//        AND 顧客番号   = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客企業別属性情報" +
                " SET 企業コード             = DECODE(NVL(企業コード,0),'0',?,企業コード)," +
                " 退会年月日             = ?," +
                "     ＤＭ止め区分           = 3092," +
                "     Ｅメール止め区分       = 5092," +
                "     バッチ更新日           = ?," +
                "     最終更新日             = ?," +
                "     最終更新日時           = SYSDATE()," +
                "     最終更新プログラムＩＤ = ?" +
                " WHERE 企業コード = ?" +
                " AND 顧客番号   = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_ps_corpid, h_taikai_date, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, MmkgData.kigyo_cd, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (ap_kaiin_flag == 1) {
            /* MM顧客企業別属性情報更新 */
//            EXEC SQL UPDATE MM顧客企業別属性情報
//            SET 退会年月日             = :h_taikai_date,
//                    ＤＭ止め区分           = 3092,
//                    Ｅメール止め区分       = 5092,
//                    バッチ更新日           = :h_bat_yyyymmdd,
//                    最終更新日             = :h_bat_yyyymmdd,
//                    最終更新日時           = SYSDATE,
//                    最終更新プログラムＩＤ = :h_programid
//            WHERE 企業コード = 1040
//            AND 顧客番号   = :h_uid;

            sql = new StringDto();
            sql.arr = "UPDATE MM顧客企業別属性情報" +
                    "            SET 退会年月日                 = ?," +
                    "                    ＤＭ止め区分           = 3092," +
                    "                    Ｅメール止め区分       = 5092," +
                    "                    バッチ更新日           = ?," +
                    "                    最終更新日             = ?," +
                    "                    最終更新日時           = SYSDATE()," +
                    "                    最終更新プログラムＩＤ = ?" +
                    "            WHERE 企業コード = 1040" +
                    "            AND 顧客番号   = ?";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.query(h_taikai_date, h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_uid);

            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
                        "MM顧客企業別属性情報(AP)", out_format_buf, 0, 0);

                /* 処理を終了する */
                return C_const_NG;
            }
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客企業別属性情報更新（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;

        /*-----UpdateKigyobetuzokuseidataForTaikai Bottom--------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateRiyokanoPoint                                             */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateRiyokanoPoint(EC_RENDO_DATA in_ec_rendo,                */
    /*                         int *punch_errno)                                  */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               １１．内部関数（利用可能ポイント情報更新）                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      EC_RENDO_DATA         in_ec_rendo     : 入力データ構造体              */
    /*      int                *  punch_errno     : パンチエラー番号              */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateRiyokanoPoint(EC_RENDO_DATA in_ec_rendo, int punch_errno) {
//  int        rtn_cd;                /* 関数戻り値                       */          /* 2022/10/11 MCCM初版 DEL */
//  int        rtn_status;            /* 関数ステータス                   */          /* 2022/10/11 MCCM初版 DEL */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("利用可能ポイント情報更新");
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
//  rtn_cd     = C_const_OK;                                                          /* 2022/10/11 MCCM初版 DEL */
//  rtn_status = C_const_Stat_OK;                                                     /* 2022/10/11 MCCM初版 DEL */

        /*------------------------------*/
        /*---更新用のホスト変数を編集---*/
        /*------------------------------*/
        /* 入会企業コード */
        /* 入会旧販社コード */
        /* 入会店舗       */
        if (h_nyukai_tenpo.intVal() == 0
                || ((MmkgData.nyukai_ymd.intVal() == 0 || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal())
                && h_kijyun_yyyymmdd.intVal() < h_mmkg_nyukai_date.intVal())) {
            h_nyukai_kigyou_cd = h_ps_corpid;
            h_nyukai_oldcorp_cd = h_ps_oldcorp;
            h_nyukai_tenpo = h_tenpo_no;
        }
        /* 発券企業コード */
        /* 発券店舗       */
        if (h_haken_tenpo.intVal() == 0
                || ((MmkgData.nyukai_ymd.intVal() == 0 || h_kijyun_yyyymmdd.intVal() < MmkgData.nyukai_ymd.intVal())
                && h_kijyun_yyyymmdd.intVal() < h_mmkg_nyukai_date.intVal())) {
            h_haken_kigyou_cd = h_ps_corpid;
            h_haken_tenpo = h_tenpo_no;
        }

        /*------------------------------------*/
        /*---TS利用可能ポイント情報更新処理---*/
        /*------------------------------------*/
        /* TS利用可能ポイント情報更新 */
//        EXEC SQL UPDATE TS利用可能ポイント情報@CMSD
//        SET 入会企業コード         = :h_nyukai_kigyou_cd,
//            /*                  入会店舗               = :h_nyukai_tenpo,                       *//* 2022/10/11 MCCM初版 DEL */
//            入会旧販社コード       = :h_nyukai_oldcorp_cd,
//            発券企業コード         = :h_haken_kigyou_cd,
//            発券店舗               = :h_haken_tenpo,
//            最終更新日             = :h_bat_yyyymmdd,
//            最終更新日時           = SYSDATE,
//            最終更新プログラムＩＤ = :h_programid,
//            入会会社コードＭＣＣ   = 2500,                                    /* 2022/10/11 MCCM初版 ADD */
//            入会店舗ＭＣＣ         = :h_renkei_mise_no                        /* 2022/10/11 MCCM初版 ADD */
//        WHERE 顧客番号 = :h_uid_varchar;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE TS利用可能ポイント情報" +
                " SET 入会企業コード         = ?   ," +
                " 入会旧販社コード       = ?," +
                " 発券企業コード         = ?," +
                " 発券店舗               = ?," +
                " 最終更新日             = ?," +
                " 最終更新日時           = SYSDATE()," +
                " 最終更新プログラムＩＤ = ?," +
                " 入会会社コードＭＣＣ   = 2500," +
                " 入会店舗ＭＣＣ         = ? " +
                " WHERE 顧客番号 = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.restAndExecute(h_nyukai_kigyou_cd, h_nyukai_oldcorp_cd, h_haken_kigyou_cd, h_haken_tenpo, h_bat_yyyymmdd, h_programid, h_renkei_mise_no, h_uid_varchar);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： PointLost                                                       */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  PointLost();                                                */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント失効処理                                                       */
    /*     利用可能ポイントがある顧客に対しポイントの失効を行う。                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int PointLost() {

        IntegerDto rtn_cd = new IntegerDto();                        /* 関数戻り値                     */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("PointLost処理");
            /*------------------------------------------------------------*/
        }

        /*-------------------------------------*/
        /* 各種日付設定処理                    */
        /*-------------------------------------*/
        rtn_cd.arr = SetDate();
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("912", 0, null, "各種日付設定処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*--------------------------------------------*/
        /* ポイント日別情報登録(ポイント年別情報)処理 */
        /*--------------------------------------------*/
        rtn_cd.arr = SetPointYearInf();
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("912", 0, null, "ポイント日別情報登録(ポイント年別情報)処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*--------------------------------------------*/
        /* ポイント日別情報登録(顧客制度情報)処理     */
        /*--------------------------------------------*/
        rtn_cd.arr = SetKokyakSedoInf();
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("912", 0, null, "ポイント日別情報登録(顧客制度情報)処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*--------------------------------------------*/
        /* ポイント日別情報登録(家族制度情報)処理     */
        /*--------------------------------------------*/
        gh_kazoku_nenji_rank_cd.arr = 0;
        gh_kazoku_getuji_rank_cd.arr = 0;

        /*--------------------------------------------*/
        /* ポイント日別情報登録処理                   */
        /*--------------------------------------------*/
        rtn_cd.arr = InsPointYmdInf();
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("912", 0, null, "ポイント日別情報登録処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        /*--------------------------------------------*/
        /* 利用可能ポイント情報更新処理               */
        /*--------------------------------------------*/
        rtn_cd.arr = UpdRiyoKanoPoint();
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("912", 0, null, "利用可能ポイント情報更新処理に失敗しました", 0, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("PointLost処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----PointLost Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： SetCartInf                                                      */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  SetCartInf();                                               */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント日別情報登録(ポイント年別情報)処理                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int SetPointYearInf() {

        StringDto ap_log_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット             */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);     /* 動的SQLバッファ                */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("SetPointYearInf処理");
            /*------------------------------------------------------------*/
        }

        /*---TSポイント年別情報---------------------------------------------------*/
        /*------------------------------------------------------------------------*/
//        memset(TsptData, 0x00, sizeof(TS_YEAR_POINT_TBL));
        TsptData = new TS_YEAR_POINT_TBL();
        memset(TsptData, 0x00, 0);
        gh_pointy_gekkan_rankup_kingaku.arr = 0;
        memset(wk_sql, 0x00, sizeof(wk_sql));
        sprintf(wk_sql,
                "SELECT 年間付与ポイント, "
                        + "       年間基本Ｐ率対象ポイント, "
                        + "       年間ランクＵＰ対象金額, "
                        + "       年間ポイント対象金額, "
                        + "       年間買上額, "
                        + "       年間買上回数, "
                        + "       月間ランクＵＰ対象金額%s "
                        + "  FROM TSポイント年別情報%s "
                        + " WHERE 年        =  %d "
                        + "   AND 顧客番号  =  ?  "
                , gh_thismonth, gl_bat_year, gh_year);

        /* HOST変数に設定 */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat2 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("ECEN_TSPN02");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** SetPointYearInf*** 動的ＳＱＬ 解析NG = %d\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
//        EXEC SQL DECLARE ECEN_TSPN02 cursor for sql_stat2;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN ECEN_TSPN02 USING :h_uid_varchar;
        sqlca.open(h_uid_varchar);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(ap_log_buf, 0x00, sizeof(ap_log_buf));
            sprintf(ap_log_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "TSポイント年別情報", ap_log_buf, 0, 0);
            return C_const_NG;
        }

        /* カーソルフェッチ１件 */
//        EXEC SQL FETCH ECEN_TSPN02
//        INTO :TsptData.nenkan_fuyo_point,
//         :TsptData.nenkan_kihon_pritsu_taisho_point,
//         :TsptData.nenkan_rankup_taisho_kingaku,
//         :TsptData.nenkan_point_taisho_kingaku,
//         :TsptData.nenkan_kaiage_kingaku,
//         :TsptData.nenkan_kaiage_cnt,
//         :gh_pointy_gekkan_rankup_kingaku;
        sqlca.fetchInto(TsptData.nenkan_fuyo_point, TsptData.nenkan_kihon_pritsu_taisho_point, TsptData.nenkan_rankup_taisho_kingaku,
                TsptData.nenkan_point_taisho_kingaku, TsptData.nenkan_kaiage_kingaku, TsptData.nenkan_kaiage_cnt, gh_pointy_gekkan_rankup_kingaku);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(ap_log_buf, 0x00, sizeof(ap_log_buf));
            sprintf(ap_log_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "TSポイント年別情報", ap_log_buf, 0, 0);
//            EXEC SQL CLOSE ECEN_TSPN02; /* カーソルクローズ ポイント年別情報  */
            sqlcaManager.close(sqlca);
            return C_const_NG;
        }

//        EXEC SQL CLOSE ECEN_TSPN02;     /* カーソルクローズ ポイント年別情報  */
        sqlcaManager.close(sqlca);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("SetPointYearInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----SetPointYearInf Bottom--------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： SetKokyakSedoInf                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  SetKokyakSedoInf();                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント日別情報登録(顧客制度情報)処理                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int SetKokyakSedoInf() {

        StringDto ap_log_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット             */
        StringDto wk_sql = new StringDto(C_const_SQLMaxLen);     /* 動的SQLバッファ                */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("SetKokyakSedoInf処理");
            /*------------------------------------------------------------*/
        }

        /*---MS顧客制度情報取得---------------------------------------------------*/
        /*------------------------------------------------------------------------*/
//        memset(MsksData, 0x00, sizeof(MS_KOKYAKU_SEDO_INFO_TBL));
        MsksData = new MS_KOKYAKU_SEDO_INFO_TBL();
        gh_kojin_nenji_rank.arr = 0;
        gh_kojin_getuji_rank.arr = 0;
        memset(wk_sql, 0x00, sizeof(wk_sql));
        sprintf(wk_sql,
                "SELECT 年次ランクコード%s, "
                        + "       月次ランクコード%s%s "
                        + "  FROM MS顧客制度情報 "
                        + " WHERE 顧客番号  =  ?  "
                , gh_thisyear_bot, gh_day_kbn, gh_thismonth);

        /* HOST変数に設定 */
        memset(str_sql, 0x00, sizeof(str_sql));
        strcpy(str_sql, wk_sql);

        /* 動的ＳＱＬ文の解析 */
//        EXEC SQL PREPARE sql_stat3 from :str_sql;
        SqlstmDto sqlca = sqlcaManager.get("ECEN_MSKS03");
        sqlca.sql = wk_sql;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                /*--------------------------------------------------------------------*/
                C_DbgMsg("*** PointLost*** 動的ＳＱＬ 解析NG = %d\n",
                        sqlca.sqlcode);
                /*--------------------------------------------------------------------*/
            }
            APLOG_WT("902", 0, null, sqlca.sqlcode, wk_sql, 0, 0, 0, 0);
            return (C_const_NG);
        }

        /* カーソル定義 */
//        EXEC SQL DECLARE ECEN_MSKS03 cursor for sql_stat3;
        sqlca.declare();

        /* カーソルオープン */
//        EXEC SQL OPEN ECEN_MSKS03 USING :h_uid_varchar;
        sqlca.open(h_uid_varchar);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(ap_log_buf, 0x00, sizeof(ap_log_buf));
            sprintf(ap_log_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT("904", 0, null, "OPEN CURSOR", sqlca.sqlcode,
                    "MS顧客制度情報", ap_log_buf, 0, 0);
            return C_const_NG;
        }

        /* カーソルフェッチ１件 */
//        EXEC SQL FETCH ECEN_MSKS03
//        INTO :gh_kojin_nenji_rank,
//         :gh_kojin_getuji_rank;
        sqlca.fetchInto(gh_kojin_nenji_rank, gh_kojin_getuji_rank);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(ap_log_buf, 0x00, sizeof(ap_log_buf));
            sprintf(ap_log_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT("904", 0, null, "FETCH", sqlca.sqlcode,
                    "MS顧客制度情報", ap_log_buf, 0, 0);
//            EXEC SQL CLOSE ECEN_MSKS03; /* カーソルクローズ MS顧客制度情報    */
            sqlcaManager.close(sqlca);
            return C_const_NG;
        }

//        EXEC SQL CLOSE ECEN_MSKS03;     /* カーソルクローズ MS顧客制度情報    */
        sqlcaManager.close(sqlca);

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("SetKokyakSedoInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----SetKokyakSedoInf Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： InsPointYmdInf                                                  */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  InsPointYmdInf();                                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     ポイント日別情報登録処理                                               */
    /*     ポイント日別情報レコードの登録を行う。                                 */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int InsPointYmdInf() {

        IntegerDto rtn_cd = new IntegerDto();                        /* 関数戻り値                     */
        IntegerDto rtn_status = new IntegerDto();                    /* 関数ステータス                 */

        int wk_i;                          /* 使用ランクコードの算出用Index */
        int[] wk_runk = new int[4];                    /* 使用ランクコードの算出用配列 */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("InsPointYmdInf処理");
            /*------------------------------------------------------------*/
        }

        /* 初期化 */
        rtn_cd.arr = C_const_OK;
        rtn_status.arr = C_const_Stat_OK;

//        memset(&HsptymdData, 0x00, sizeof(HsptymdData));
        HsptymdData = new TSHS_DAY_POINT_TBL();
        memset(HsptymdData, 0x00, 0);
        /* ---追加情報をセット------------------------------------------------------ */
        HsptymdData.system_ymd = h_bat_yyyymmdd;                              /* システム年月日               */
        memcpy(HsptymdData.kokyaku_no, h_uid, sizeof(h_uid) - 1);                      /* 顧客番号                     */
        HsptymdData.kokyaku_no.len = strlen(h_uid);
        /* 処理通番の取得 */
//        EXEC SQL SELECT SQポイント日別処理通番.NEXTVAL
//        INTO :gh_shori_seq
//        FROM DUAL;
        sqlca.sql = new StringDto("SELECT NEXTVAL('SQポイント日別処理通番') FROM DUAL");
        sqlca.restAndExecute();
        sqlca.fetch();
        sqlca.recData(gh_shori_seq);

        HsptymdData.shori_seq = gh_shori_seq;                                /* 処理通番                     */
        HsptymdData.kaiin_kigyo_cd = h_kigyo_cd;                                  /* 会員企業コード               */
        HsptymdData.kaiin_kyu_hansya_cd = h_kyu_hansha_cd;                             /* 会員旧販社コード             */
        memcpy(HsptymdData.kaiin_no, h_pid, sizeof(h_pid) - 1);                      /* 会員番号                     */
        HsptymdData.kaiin_no.len = strlen(h_pid);
        HsptymdData.nyukai_kigyo_cd = h_nyukai_kigyou_cd;                          /* 入会企業コード               */
        HsptymdData.nyukai_tenpo = h_nyukai_tenpo;                              /* 入会店舗                     */
        HsptymdData.hakken_kigyo_cd = h_haken_kigyou_cd;                           /* 発券企業コード               */
        HsptymdData.hakken_tenpo = h_haken_tenpo;                               /* 発券店舗                     */
        HsptymdData.seisan_ymd.arr = 0;                                           /* 精算年月日                   */
        HsptymdData.toroku_ymd.arr = 0;                                           /* 登録年月日                   */
        HsptymdData.data_ymd = h_bat_yyyymmdd;                              /* データ年月日                 */
        HsptymdData.kigyo_cd = h_nyukai_kigyou_cd;                          /* 企業コード                   */
        HsptymdData.mise_no = h_nyukai_tenpo;                              /* 店番号                       */
        HsptymdData.terminal_no.arr = 0;                                           /* ターミナル番号               */
        HsptymdData.torihiki_no.arr = 0;                                           /* 取引番号                     */
        HsptymdData.jikoku_hms.arr = 0;                                           /* 時刻                         */
        HsptymdData.riyu_cd.arr = 90;                                          /* 理由コード                   */
        HsptymdData.card_nyuryoku_kbn.arr = 2;                                           /* カード入力区分               */
        HsptymdData.shori_taisho_file_record_no.arr = 0;                                           /* 処理対象ファイルレコード番号 */
        HsptymdData.real_koshin_flg.arr = 0;                                           /* リアル更新フラグ             */
        /* 付与ポイント                 */
        HsptymdData.fuyo_point.arr = (h_riyo_kano_point.intVal()) * (-1);
        HsptymdData.riyo_point.arr = 0;                                           /* 利用ポイント                 */
        HsptymdData.kihon_pritsu_taisho_point.arr = 0;                                           /* 基本Ｐ率対象ポイント         */
        HsptymdData.rankup_taisho_kingaku.arr = 0;                                           /* ランクＵＰ対象金額           */
        HsptymdData.point_taisho_kingaku.arr = 0;                                           /* ポイント対象金額             */
        HsptymdData.service_hakko_maisu.arr = 0;                                           /* サービス券発行枚数           */
        HsptymdData.service_riyo_maisu.arr = 0;                                           /* サービス券利用枚数           */
        HsptymdData.kojin_getuji_rank_cd = gh_kojin_getuji_rank;                        /* 個人月次ランクコード         */
        HsptymdData.kojin_nenji_rank_cd = gh_kojin_nenji_rank;                         /* 個人年次ランクコード         */
        HsptymdData.kazoku_getuji_rank_cd = gh_kazoku_getuji_rank_cd;                    /* 家族月次ランクコード         */
        HsptymdData.kazoku_nenji_rank_cd = gh_kazoku_nenji_rank_cd;                     /* 家族年次ランクコード         */
        /* 使用ランクコードの算出 */
        wk_runk[0] = gh_kojin_nenji_rank.intVal();
        wk_runk[1] = gh_kojin_getuji_rank.intVal();
        wk_runk[2] = gh_kazoku_nenji_rank_cd.intVal();
        wk_runk[3] = gh_kazoku_getuji_rank_cd.intVal();
        HsptymdData.shiyo_rank_cd.arr = 0;
        for (wk_i = 0; wk_i <= 3; wk_i++) {
            if (wk_runk[wk_i] > HsptymdData.shiyo_rank_cd.intVal()) {
                HsptymdData.shiyo_rank_cd.arr = wk_runk[wk_i];                                /* 使用ランクコード             */
            }
        }
        HsptymdData.kaiage_kingaku.arr = 0;                                       /* 買上額                       */
        HsptymdData.kaiage_cnt = TsptData.nenkan_kaiage_cnt;              /* 買上回数                     */
        HsptymdData.koshinmae_riyo_kano_point = h_riyo_kano_point;               /* 更新前利用可能ポイント       */
        HsptymdData.koshinmae_gekkan_kojin_rankup_taisho_kingaku = gh_pointy_gekkan_rankup_kingaku;         /* 更新前月間ランクＵＰ対象金額 */
        HsptymdData.koshinmae_nenkan_kojin_rankup_taisho_kingaku = TsptData.nenkan_rankup_taisho_kingaku;   /* 更新前年間ランクＵＰ対象金額 */
//  HsptymdData.kazoku_id                                     = MsksData.kazoku_id;                      /* 家族ＩＤ                     */ /* 2022/10/11 MCCM初版 DEL */
        strcpy(HsptymdData.kazoku_id, MsksData.kazoku_id);            /* 家族ＩＤ                     */ /* 2022/10/11 MCCM初版 ADD */
        HsptymdData.delay_koshin_ymd = h_bat_yyyymmdd;                          /* ディレイ更新年月日           */
        strcpy(HsptymdData.delay_koshin_apl_version, h_programid_ver);                       /* ディレイ更新ＡＰＬバージョン */
        HsptymdData.sosai_flg.arr = 0;                                       /* 相殺フラグ                   */
        HsptymdData.mesai_check_flg.arr = 0;                                       /* 明細チェックフラグ           */
        HsptymdData.mesai_check_kbn.arr = 0;                                       /* 明細チェック区分             */
        HsptymdData.sagyo_kigyo_cd.arr = 0;                                       /* 作業企業コード               */
        HsptymdData.sagyosha_id.arr = 0;                                       /* 作業者ＩＤ                   */
        HsptymdData.sagyo_ymd.arr = 0;                                       /* 作業年月日                   */
        HsptymdData.sagyo_hms.arr = 0;                                       /* 作業時刻                     */
        HsptymdData.batch_koshin_ymd = h_bat_yyyymmdd;                          /* バッチ更新日                 */
        HsptymdData.saishu_koshin_ymd = h_bat_yyyymmdd;                          /* 最終更新日                   */
        HsptymdData.saishu_koshin_ymdhms = h_bat_yyyymmdd;                          /* 最終更新日時                 */
        strcpy(HsptymdData.saishu_koshin_programid, h_programid);                            /* 最終更新プログラムＩＤ       */

        if (DBG_LOG) {
            /*----------------------------------------------------------------*/
            C_DbgMsg("*** HSポイント日別情報追加結果 *** %s\n", "入力引数情報");
            C_DbgMsg("システム年月日                 =[%d]\n", HsptymdData.system_ymd);
            C_DbgMsg("顧客番号                       =[%s]\n", HsptymdData.kokyaku_no.arr);
            C_DbgMsg("処理通番                       =[%10.0f]\n", HsptymdData.shori_seq);
            C_DbgMsg("会員企業コード                 =[%d]\n", HsptymdData.kaiin_kigyo_cd);
            C_DbgMsg("会員旧販社コード               =[%d]\n", HsptymdData.kaiin_kyu_hansya_cd);
            C_DbgMsg("会員番号                       =[%s]\n", HsptymdData.kaiin_no.arr);
            C_DbgMsg("入会企業コード                 =[%d]\n", HsptymdData.nyukai_kigyo_cd);
            C_DbgMsg("入会店舗                       =[%d]\n", HsptymdData.nyukai_tenpo);
            C_DbgMsg("発券企業コード                 =[%d]\n", HsptymdData.hakken_kigyo_cd);
            C_DbgMsg("発券店舗                       =[%d]\n", HsptymdData.hakken_tenpo);
            C_DbgMsg("精算年月日                   0 =[%d]\n", HsptymdData.seisan_ymd);
            C_DbgMsg("登録年月日                   0 =[%d]\n", HsptymdData.toroku_ymd);
            C_DbgMsg("データ年月日             today =[%d]\n", HsptymdData.data_ymd);
            C_DbgMsg("企業コード                     =[%d]\n", HsptymdData.kigyo_cd);
            C_DbgMsg("店番号                         =[%d]\n", HsptymdData.mise_no);
            C_DbgMsg("ターミナル番号               0 =[%d]\n", HsptymdData.terminal_no);
            C_DbgMsg("取引番号                     0 =[%f]\n", HsptymdData.torihiki_no);                                 /* 2022/10/11 MCCM初版 MOD */
            C_DbgMsg("時刻                         0 =[%d]\n", HsptymdData.jikoku_hms);
            C_DbgMsg("理由コード                  90 =[%d]\n", HsptymdData.riyu_cd);
            C_DbgMsg("カード入力区分               2 =[%d]\n", HsptymdData.card_nyuryoku_kbn);
            C_DbgMsg("処理対象ファイルレコード番号 0 =[%d]\n", HsptymdData.shori_taisho_file_record_no);
            C_DbgMsg("リアル更新フラグ             0 =[%d]\n", HsptymdData.real_koshin_flg);
            C_DbgMsg("付与ポイント                   =[%10.0f]\n", HsptymdData.fuyo_point);
            C_DbgMsg("利用ポイント                 0 =[%10.0f]\n", HsptymdData.riyo_point);
            C_DbgMsg("基本Ｐ率対象ポイント         0 =[%10.0f]\n", HsptymdData.kihon_pritsu_taisho_point);
            C_DbgMsg("ランクＵＰ対象金額           0 =[%10.0f]\n", HsptymdData.rankup_taisho_kingaku);
            C_DbgMsg("ポイント対象金額             0 =[%10.0f]\n", HsptymdData.point_taisho_kingaku);
            C_DbgMsg("サービス券発行枚数           0 =[%d]\n", HsptymdData.service_hakko_maisu);
            C_DbgMsg("サービス券利用枚数           0 =[%d]\n", HsptymdData.service_riyo_maisu);
            C_DbgMsg("個人月次ランクコード           =[%d]\n", HsptymdData.kojin_getuji_rank_cd);
            C_DbgMsg("個人年次ランクコード           =[%d]\n", HsptymdData.kojin_nenji_rank_cd);
            C_DbgMsg("家族月次ランクコード           =[%d]\n", HsptymdData.kazoku_getuji_rank_cd);
            C_DbgMsg("家族年次ランクコード           =[%d]\n", HsptymdData.kazoku_nenji_rank_cd);
            C_DbgMsg("使用ランクコード               =[%d]\n", HsptymdData.shiyo_rank_cd);
            C_DbgMsg("買上額                       0 =[%10.0f]\n", HsptymdData.kaiage_kingaku);
            C_DbgMsg("買上回数                       =[%10.0f]\n", HsptymdData.kaiage_cnt);
            C_DbgMsg("更新前利用可能ポイント         =[%10.0f]\n", HsptymdData.koshinmae_riyo_kano_point);
            C_DbgMsg("更新前付与ポイント             =[%10.0f]\n", HsptymdData.koshinmae_fuyo_point);
            C_DbgMsg("更新前基本Ｐ率対象ポイント     =[%10.0f]\n", HsptymdData.koshinmae_kihon_pritsu_taisho_point);
            C_DbgMsg("更新前月間ランクＵＰ対象金額   =[%10.0f]\n", HsptymdData.koshinmae_gekkan_kojin_rankup_taisho_kingaku);
            C_DbgMsg("更新前年間ランクＵＰ対象金額   =[%10.0f]\n", HsptymdData.koshinmae_nenkan_kojin_rankup_taisho_kingaku);
            C_DbgMsg("更新前ポイント対象金額         =[%10.0f]\n", HsptymdData.koshinmae_point_taisho_kingaku);
            C_DbgMsg("更新前買上額                   =[%10.0f]\n", HsptymdData.koshinmae_kaiage_kingaku);
            C_DbgMsg("家族ＩＤ                       =[%s]\n", HsptymdData.kazoku_id.arr);                                 /* 2022/10/11 MCCM初版 MOD */
            C_DbgMsg("更新前月間家族ランクＵＰ金額   =[%10.0f]\n", HsptymdData.koshinmae_gekkan_kazoku_rankup_taisho_kingaku);
            C_DbgMsg("更新前年間家族ランクＵＰ金額   =[%10.0f]\n", HsptymdData.koshinmae_nenkan_kazoku_rankup_taisho_kingaku);
            C_DbgMsg("リアル更新日時              nl =[%d]\n", HsptymdData.real_koshin_ymd);
            C_DbgMsg("リアル更新ＡＰＬバージョン  nl =[%s]\n", HsptymdData.real_koshin_apl_version);
            C_DbgMsg("ディレイ更新日時         today =[%d]\n", HsptymdData.delay_koshin_ymd);
            C_DbgMsg("ディレイ更新ＡＰＬバージョン   =[%s]\n", HsptymdData.delay_koshin_apl_version);
            C_DbgMsg("相殺フラグ                   0 =[%d]\n", HsptymdData.sosai_flg);
            C_DbgMsg("明細チェックフラグ           0 =[%d]\n", HsptymdData.mesai_check_flg);
            C_DbgMsg("明細チェック区分             0 =[%d]\n", HsptymdData.mesai_check_kbn);
            C_DbgMsg("作業企業コード               0 =[%d]\n", HsptymdData.sagyo_kigyo_cd);
            C_DbgMsg("作業者ＩＤ                   0 =[%10.0f]\n", HsptymdData.sagyosha_id);
            C_DbgMsg("作業年月日                   0 =[%d]\n", HsptymdData.sagyo_ymd);
            C_DbgMsg("作業時刻                     0 =[%d]\n", HsptymdData.sagyo_hms);
            C_DbgMsg("バッチ更新日             today =[%d]\n", HsptymdData.batch_koshin_ymd);
            C_DbgMsg("最終更新日               today =[%d]\n", HsptymdData.saishu_koshin_ymd);
            C_DbgMsg("最終更新日時             today =[%lf]\n    ", HsptymdData.saishu_koshin_ymdhms);
            C_DbgMsg("最終更新プログラムＩＤ         =[%s]\n", HsptymdData.saishu_koshin_programid);
            /*----------------------------------------------------------------*/
        }

        /* HSポイント日別情報追加 */
        rtn_cd.arr = cmBTfuncB.C_InsertDayPoint(HsptymdData, h_bat_yyyymmdd.intVal(), rtn_status);
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_InsertDayPoint",
                    rtn_cd, 0, 0, 0, 0);
            if (DBG_LOG) {
                /*----------------------------------------------------------------*/
                C_DbgMsg("*** InsPointYmdInf *** C_InsertDayPoint rtn_cd=[%d]\n", rtn_cd);
                C_DbgMsg("*** InsPointYmdInf *** C_InsertDayPoint rtn_status=[%d]\n", rtn_status);
                /*----------------------------------------------------------------*/
            }
            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("InsPointYmdInf処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----InsPointYmdInf Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdRiyoKanoPoint                                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  UpdRiyoKanoPoint();                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     利用可能ポイント情報更新処理                                           */
    /*     利用可能ポイント情報レコードの更新を行う。                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int UpdRiyoKanoPoint() {

        StringDto ap_log_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット             */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("UpdRiyoKanoPoint処理");
            /*------------------------------------------------------------*/
        }

        /* 利用可能ポイント情報の編集 */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgMsg("*** UpdRiyoKanoPoint *** MS利用可能ポイント情報 [%s]\n", "UPDATE");
            C_DbgMsg("*** UpdRiyoKanoPoint *** 顧客番号  =[%s]\n", h_uid_varchar.arr);
            /*------------------------------------------------------------*/
        }

        /* 該当データを更新する */
        /* 2021/03/04 期間限定ポイント対応 利用可能ポイントコメントアウト */
        /*    SET 利用可能ポイント = ((利用可能ポイント * (-1)) + 利用可能ポイント), */

//        EXEC SQL UPDATE TS利用可能ポイント情報@CMSD
//        SET 入会企業コード         = :h_nyukai_kigyou_cd,
//            入会店舗               = :h_nyukai_tenpo,
//            入会旧販社コード       = :h_nyukai_oldcorp_cd,
//            発券企業コード         = :h_haken_kigyou_cd,
//            発券店舗               = :h_haken_tenpo,
//            最終更新日             = :h_bat_yyyymmdd,
//            最終更新日時           = SYSDATE,
//            最終更新プログラムＩＤ = :h_programid
//        WHERE 顧客番号 = :h_uid_varchar;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE TS利用可能ポイント情報" +
                "SET 入会企業コード     = ?," +
                "入会店舗               = ?," +
                "入会旧販社コード       = ?," +
                "発券企業コード         = ?," +
                "発券店舗               = ?," +
                "最終更新日             = ?," +
                "最終更新日時           = SYSDATE()," +
                "最終更新プログラムＩＤ = ?," +
                "WHERE 顧客番号         = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_nyukai_kigyou_cd, h_nyukai_tenpo, h_nyukai_oldcorp_cd, h_haken_kigyou_cd, h_haken_tenpo, h_bat_yyyymmdd, h_programid, h_uid_varchar);

        if (sqlca.sqlcode != C_const_Ora_OK) {
            memset(ap_log_buf, 0x00, sizeof(ap_log_buf));
            sprintf(ap_log_buf, "顧客番号=[%s]", h_uid_varchar.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "MS利用可能ポイント情報", ap_log_buf, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("UpdRiyoKanoPoint処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----UpdRiyoKanoPoint Bottom----------------------------------------------*/
    }

/******************************************************************************/
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

    /******************************************************************************/
    public int OutPunchdata(int punch_errno) {
        StringDto wk_storeno = new StringDto(4);         /* 店番号                           */
        StringDto wk_request_date = new StringDto(9);    /* 依頼日                           */

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("パンチエラーデータ出力処理");
            /*---------------------------------------------------------------------*/
            C_DbgMsg("処理年月日        =[%d]\n", h_bat_yyyymmdd);
            C_DbgMsg("会員番号          =[%s]\n", h_pid);
            C_DbgMsg("データ区分        =[%d]\n", 2);
            C_DbgMsg("申込書企業コード  =[%d]\n", h_ps_corpid);
            C_DbgMsg("申込書店番号      =[%d]\n", h_moushikomiten_no);
            C_DbgMsg("理由コード        =[%d]\n", punch_errno);
            C_DbgMsg("申込年月日        =[%d]\n", h_kijyun_yyyymmdd);
            C_DbgMsg("バッチ番号        =[%d]\n", 0);
        }

        /* 初期化 */
        tmpe_buff = new TM_PUNCH_DATA_ERROR_INFO_TBL();
        memset(tmpe_buff, 0x00, 0);
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
        tmpe_buff.data_kbn.arr = 2;
        /* 申込書企業コード */
        tmpe_buff.entry_kigyo_cd = h_ps_corpid;
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
        memcpy(tmpe_buff.kaiin_mesho, h_kaiin_name, sizeof(h_kaiin_name) - 1);

        /* INSERTする */
//        EXEC SQL INSERT INTO TMパンチデータエラー情報
//            (  処理年月日,
//                    会員番号,
//                    データ区分,
//                    申込書企業コード,
//                    申込書店番号,
//                    理由コード,
//                    依頼日,
//                    バッチ番号,
//                    入会年月日,
//                    会員氏名 )
//        VALUES
//                (  :tmpe_buff.shori_ymd,
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
        WRKSQL.arr = sprintf(WRKSQL.arr, "INSERT INTO TMパンチデータエラー情報" +
                " (  処理年月日," +
                " 会員番号," +
                " データ区分," +
                " 申込書企業コード," +
                " 申込書店番号," +
                " 理由コード," +
                " 依頼日," +
                " バッチ番号," +
                " 入会年月日," +
                " 会員氏名 )" +
                "        VALUES" +
                "        (  ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ?," +
                "        ? )");
        sqlca.sql = WRKSQL;
        sqlca.restAndExecute(tmpe_buff.shori_ymd, tmpe_buff.kaiin_no, tmpe_buff.data_kbn, tmpe_buff.entry_kigyo_cd,
                tmpe_buff.entry_mise_no, tmpe_buff.riyu_cd, tmpe_buff.irai_ymd, tmpe_buff.batch_no, tmpe_buff.nyukai_ymd, tmpe_buff.kaiin_mesho);

        /* 重複エラー以外のエラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_DUPL) {
            /* DBERR */
            sprintf(out_format_buf, "会員番号=[%s]", h_pid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "INSERT", sqlca.sqlcode,
                    "TMパンチデータエラー情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        /* 重複エラーの場合UPDATEする */
        if (sqlca.sqlcode == C_const_Ora_DUPL) {
            /* TMパンチデータエラー情報更新処理 */
//            EXEC SQL UPDATE TMパンチデータエラー情報
//            SET 申込書企業コード   = :tmpe_buff.entry_kigyo_cd,
//                    申込書店番号       = :tmpe_buff.entry_mise_no,
//                    理由コード         = :tmpe_buff.riyu_cd,
//                    依頼日             = :tmpe_buff.irai_ymd,
//                    バッチ番号         = :tmpe_buff.batch_no,
//                    入会年月日         = :tmpe_buff.nyukai_ymd,
//                    会員氏名           = :tmpe_buff.kaiin_mesho
//            WHERE  処理年月日         = :h_bat_yyyymmdd
//            AND    会員番号           = :h_pid
//            AND    データ区分         = :tmpe_buff.data_kbn;

            StringDto sql = new StringDto();
            sql.arr = "UPDATE TMパンチデータエラー情報" +
                    " SET 申込書企業コード   = ?," +
                    "         申込書店番号   = ?," +
                    "         理由コード     = ?," +
                    "         依頼日         = ?," +
                    "         バッチ番号     = ?," +
                    "         入会年月日     = ?," +
                    "         会員氏名       = ?," +
                    " WHERE  処理年月日      = ?," +
                    " AND    会員番号        = ?," +
                    " AND    データ区分      = ?";
            sqlca.sql = sql;
            sqlca.prepare();
            sqlca.query(tmpe_buff.entry_kigyo_cd, tmpe_buff.entry_mise_no, tmpe_buff.riyu_cd, tmpe_buff.irai_ymd, tmpe_buff.batch_no,
                    tmpe_buff.nyukai_ymd, tmpe_buff.kaiin_mesho, h_bat_yyyymmdd, h_pid, tmpe_buff.data_kbn);
            /* エラーの場合処理を異常終了する */
            if (sqlca.sqlcode != C_const_Ora_OK) {
                /* DBERR */
                sprintf(out_format_buf, "会員番号=[%s]", h_pid);
                APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
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

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： SetDate                                                         */
    /*                                                                            */
    /*  書式                                                                      */
    /*    static int  SetDate()                                                   */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*     各種日付設定処理                                                       */
    /*     処理に必要な各種日付を設定する                                         */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*     なし                                                                   */
    /*  【戻り値】                                                                */
    /*     0   ： 正常                                                            */
    /*     1   ： 異常                                                            */
    /*                                                                            */

    /******************************************************************************/
    public int SetDate() {
        StringDto wk_buff = new StringDto(512);                    /* ファイル出力内容編集バッファ  */
        StringDto wk_buff2 = new StringDto(512);                   /* 編集バッファ                  */
        IntegerDto rtn_cd = new IntegerDto();                          /* 関数戻り値                    */
        int wk_year;                         /* 年                            */

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgStart("SetDate処理");
            /*------------------------------------------------------------*/
        }

        /*---ポイント日別情報ＴＢＬ名(yyyymm)取得---------------------------------*/
        memset(gl_bat_month, 0x00, sizeof(gl_bat_month));
        memcpy(gl_bat_month.strDto(), bat_yyyymmdd, 6);

        /*---ポイント年別情報ＴＢＬ名(yyyy)取得-----------------------------------*/
        memset(gl_bat_year, 0x00, sizeof(gl_bat_year));
        memcpy(gl_bat_year.strDto(), bat_yyyymmdd, 4);
        gh_year.arr = atoi(gl_bat_year);  /* バッチ処理年の数値変換 */

        /*---日付別のＴＢＬ項目名(Y/MM/nMM)取得-----------------------------------*/
        memset(wk_buff, 0x00, sizeof(wk_buff));
        memset(wk_buff2, 0x00, sizeof(wk_buff2));
        /* 年下１桁を全角に変換する */
        memset(gh_thisyear_bot, 0x00, sizeof(gh_thisyear_bot));
        memcpy(wk_buff, (bat_yyyymmdd.substring(3)), 1);
        rtn_cd.arr = C_ConvHalf2Full(wk_buff, gh_thisyear_bot.strDto());
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("各種日付設定処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return (C_const_NG);  /* 処理を終了する */
        }
        /* 月を全角に変換する */
        memset(gh_thismonth, 0x00, sizeof(gh_thismonth));
        memcpy(wk_buff2, (bat_yyyymmdd.substring(4)), 2);
        rtn_cd.arr = C_ConvHalf2Full(wk_buff2, gh_thismonth.strDto());
        if (rtn_cd.arr != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd,
                    0, 0, 0, 0);
            if (DBG_LOG) {
                /*---------------------------------------------*/
                C_DbgEnd("各種日付設定処理", C_const_NG, 0, 0);
                /*---------------------------------------------*/
            }
            return (C_const_NG);  /* 処理を終了する */
        }
        if (DBG_LOG) {
            /*-------------------------------------------------------------*/
            C_DbgMsg("*** SetDate *** C_ConvHalf2Full=%d\n", rtn_cd);
            C_DbgMsg("*** SetDate *** C_ConvHalf2Full from [%s]\n", wk_buff);
            C_DbgMsg("*** SetDate *** C_ConvHalf2Full to   [%s]\n", gh_thisyear_bot);
            C_DbgMsg("*** SetDate *** C_ConvHalf2Full from [%s]\n", wk_buff2);
            C_DbgMsg("*** SetDate *** C_ConvHalf2Full to   [%s]\n", gh_thismonth);
            /*-------------------------------------------------------------*/
        }
        /* 奇数年（１）／偶数年（０）区分を取得 */
        memset(gh_day_kbn, 0x00, sizeof(gh_day_kbn));
        wk_year = (h_bat_yyyymmdd.intVal() / 10000) % 10;
        if ((wk_year % 2) == 0) {
            strcpy(gh_day_kbn, "０");
        }  /* 偶数年 */ else {
            strcpy(gh_day_kbn, "１");
        }  /* 奇数年 */


        if (DBG_LOG) {
            /*------------------------------------------------------------------------------------------*/
            C_DbgMsg("*** SetDate *** バッチ処理日数値                 =[%d]\n", h_bat_yyyymmdd);
            C_DbgMsg("*** SetDate *** ポイント日別情報ＴＢＬ名(yyyymm) =[%s]\n", gl_bat_month);
            C_DbgMsg("*** SetDate *** 日付別のＴＢＬ項目名 当年下１桁  =[%s]\n", gh_thisyear_bot);
            C_DbgMsg("*** SetDate *** 日付別のＴＢＬ項目名 区分        =[%s]\n", gh_day_kbn);
            C_DbgMsg("*** SetDate *** 日付別のＴＢＬ項目名 当月        =[%s]\n", gh_thismonth);
            /*------------------------------------------------------------------------------------------*/
        }

        if (DBG_LOG) {
            /*------------------------------------------------------------*/
            C_DbgEnd("SetDate処理", 0, 0, 0);
            /*------------------------------------------------------------*/
        }

        return (C_const_OK);              /* 処理終了                           */
        /*-----SetDate Bottom----------------------------------------------*/
    }

/******************************************************************************/
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
    /*      char       *    Arg_in      ：引数値                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int cmBTcentB_Chk_Arg(String Arg_in) {
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("cmBTcentB_Chk_Arg処理");
            C_DbgMsg("*** cmBTcentB_Chk_Arg *** 引数=[%s]\n", Arg_in);
            /*---------------------------------------------------------------------*/
        }
        /* 初期化 */
        memset(out_format_buf, 0x00, sizeof(out_format_buf));

        if (memcmp(Arg_in, DEF_ARG_I, 2) == 0) {       /* -i入力ファイルチェック  */
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

/******************************************************************************/
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
    /*        char        *msgid     ：メッセージＩＤ                             */
    /*        int         msgidsbt   ：メッセージ登録種別                         */
    /*        char        *dbkbn     ：ＤＢ区分                                   */
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

    /******************************************************************************/
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn,
                        Object param1, Object param2, Object param3,
                        Object param4, Object param5, Object param6) {
        String[] out_flg = new String[2];                //** APログフラグ             **//*
        String[] out_format = new String[DEF_BUFSIZE8K];         //** APログフォーマット       **//*
        IntegerDto out_status;                        //* フォーマット取得結果 *//*
        IntegerDto rtn_cd = new IntegerDto();                            //* 関数戻り値 *//*

        if (DBG_LOG) {
            C_DbgStart("APLOG_WT処理");
        }

        //*#####################################*//*
        //*    APログフォーマット取得処理         *//*
        //*#####################################*//*

        memset(out_flg, 0x00, sizeof(out_flg));
        memset(out_format, 0x00, sizeof(out_format));
        out_status = new IntegerDto();
        //*dbg_getaplogfmt = 1;*//*
        rtn_cd.arr = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, out_flg, out_format, out_status);
        //*dbg_getaplogfmt = 0; *//*

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtn_cd);
        }

        //*#####################################*//*
        //*    APログ出力処理                     *//*
        //*#####################################*//*
        rtn_cd.arr = C_APLogWrite(msgid, out_format, out_flg, param1, param2, param3, param4, param5, param6);

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtn_cd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        }

        return (C_const_OK);
    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： Split_Data(char *buf)                                           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static void Split_Data(char *buf)                                         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      指定された文字列データを構造体に格納する処理                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      buf : 入力ファイルの１レコード                                        */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*      なし                                                                  */
    /*                                                                            */

    /******************************************************************************/
    public void Split_Data(StringDto buf) {
        int pt;

        if (DBG_LOG) {
            C_DbgStart("*** Split_Data*** データ格納処理");
        }

        memset(in_ec_rendo, 0x00, sizeof(in_ec_rendo));

        /* 改行コードを削除 */
        strtok(buf, DEF_STR4);
        strtok(buf, DEF_STR5);

        /* 会員番号設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.kaiin_no, buf.strp(DEF_STR2), pt - 0);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 店舗番号設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.tenpo_no, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 会員氏名（漢字）設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.kanji_name, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 会員氏名（カナ）設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.kana_name, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 郵便番号設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.zip, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 住所１設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.address1, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 住所２設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.address2, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 20230123 DEL MCCM */
//    /* 住所３設定 */
//    pt = strpbrk( buf, DEF_STR2 );
//    memcpy(in_ec_rendo.address3, buf, pt -buf);
//    buf = buf + (pt - buf) + 1;
        /* 20230123 DEL MCCM */

        /* 電話番号１設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.telephone1, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 電話番号２設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.telephone2, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 電話番号３設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.telephone3, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 電話番号４設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.telephone4, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 生年月日定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.birth_date, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 性別設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.seibetsu, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 最終更新日時設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.last_upd_date_yyyymmddhhmmss, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 入会日設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.nyukaibi_yyyymmdd, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 退会日設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.taikaibi_yyyymmdd, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* 連動日設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.rendoubi_yyyymmdd, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* e-mail受信可否設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.email_zyushin_kahi, buf.strp(DEF_STR2), pt);
        // buf.len = buf.len + (pt - buf.len) + 1;

        /* ＤＭ受信可否設定 */
        pt = strpbrk(buf, DEF_STR2);
        memcpy(in_ec_rendo.dm_zyushin_kahi, buf, strlen(buf));

        if (DBG_LOG) {
            C_DbgMsg("会員番号         =[%s]\n", in_ec_rendo.kaiin_no);
            C_DbgMsg("店舗番号         =[%s]\n", in_ec_rendo.tenpo_no);
            C_DbgMsg("生年月日         =[%s]\n", in_ec_rendo.birth_date);
            C_DbgMsg("最終更新日時     =[%s]\n", in_ec_rendo.last_upd_date_yyyymmddhhmmss);
            C_DbgMsg("入会日           =[%s]\n", in_ec_rendo.nyukaibi_yyyymmdd);
            C_DbgMsg("退会日           =[%s]\n", in_ec_rendo.taikaibi_yyyymmdd);
            C_DbgMsg("連動日           =[%s]\n", in_ec_rendo.rendoubi_yyyymmdd);
            C_DbgMsg("e-mail受信可否   =[%s]\n", in_ec_rendo.email_zyushin_kahi);
            C_DbgMsg("ＤＭ受信可否     =[%s]\n", in_ec_rendo.dm_zyushin_kahi);
            C_DbgEnd("*** Split_Data*** データ格納処理", 0, 0, 0);
        }

    }

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： strchg(char *buf, const char *str1, const char *str2)           */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static void strchg(char *buf, const char *str1, const char *str2)         */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*      指定された置換対象文字列を置換文字列に変換する処理                    */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      buf  : 置換する文字列                                                 */
    /*      str1 : 置換対象文字列                                                 */
    /*      str2 : 置換後文字列                                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*      なし                                                                  */
    /*                                                                            */

    /******************************************************************************/
    public void strchg(StringDto buf, String str1, String str2) {
        buf.arr = buf.arr.replaceAll(str1, str2);
//        StringDto tmp = new StringDto(1024+1);
//        char *p;
//
//        while ( (p = strstr(buf, str1)) != NULL) {
//        *p = '\0';
//            p += strlen(str1);
//            strcpy(tmp, p);
//            strcat(buf, str2);
//            strcat(buf, tmp);
//        }
    }
    /* 2022/10/12 MCCM初版 DEL START */
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateMaternityBabyInfoForTaikai                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateMaternityBabyInfoForTaikai()                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MMマタニティベビー情報更新（退会）                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
/******************************************************************************/
//static int  UpdateMaternityBabyInfoForTaikai()
//{

//if( DBG_LOG){
//    /*---------------------------------------------------------------------*/
//    C_DbgStart( "MMマタニティベビー情報更新（退会）" );
//    /*---------------------------------------------------------------------*/
//}

//    if(up_maternity_circle_flag == 1){

//        /* MMマタニティベビー情報検索処理 */
    /*        EXEC SQL SELECT 顧客番号                                                 */
    /*                    FROM MMマタニティベビー情報                                  */
    /*                  WHERE 顧客番号     = :h_uid;                                   */

//        /* データ無しエラー以外のエラーの場合処理を異常終了する */
//        if(sqlca.sqlcode != C_const_Ora_OK
//            && sqlca.sqlcode != C_const_Ora_NOTFOUND){
//if( DBG_LOG){
//                C_DbgMsg( "*** CheckMM *** MMマタニティベビー情報検索%s\n", "");
//}
//                /* DBERR */
//                sprintf( out_format_buf, "顧客番号=[%s]",h_uid );
//                APLOG_WT( DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
//                         "MMマタニティベビー情報", out_format_buf, 0, 0);

//                /* 処理を終了する */
//                return(C_const_NG);
//        }

//        /* 更新対象がない場合、正常を返す */
//        if(sqlca.sqlcode == C_const_Ora_NOTFOUND){
//            /* 処理を終了する */
//            return C_const_OK;
//        }

//        /* MMマタニティベビー情報更新 */
    /*        EXEC SQL UPDATE MMマタニティベビー情報                                   */
    /*                    SET ＤＭ止め区分           = 3092,                           */
    /*                        Ｅメール止め区分       = 5092,                           */
    /*                        バッチ更新日           = :h_bat_yyyymmdd,                */
    /*                        最終更新日             = :h_bat_yyyymmdd,                */
    /*                        最終更新日時           = SYSDATE,                        */
    /*                        最終更新プログラムＩＤ = :h_programid                    */
    /*                  WHERE 顧客番号 = :h_uid;                                       */

//        /* エラーの場合処理を異常終了する */
//        if (sqlca.sqlcode != C_const_Ora_OK ) {
//            /* DBERR */
//            sprintf( out_format_buf, "顧客番号=[%s]",h_uid );
//            APLOG_WT( DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
//                    "MMマタニティベビー情報", out_format_buf, 0, 0);

//            /* 処理を終了する */
//            return C_const_NG;
//        }

//        /* MMマタニティベビー更新件数カウントアップ */
//        mmmb_data_taikai_cnt++;

//    }

//if( DBG_LOG){
//    /*---------------------------------------------*/
//    C_DbgEnd( "MMマタニティベビー情報更新（退会）" ,0 ,0 ,0);
//    /*---------------------------------------------*/
//}
//    /* 処理を終了する */
//    return C_const_OK;

///*-----UpdateMaternityBabyInfoForTaikai Bottom--------------------------------------*/
//}
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateCircleKokyakuInfoForTaikai                                */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateCircleKokyakuInfoForTaikai()                            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MSサークル顧客情報情報更新（退会）                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */
/******************************************************************************/
//static int  UpdateCircleKokyakuInfoForTaikai()
//{

//if( DBG_LOG){
//    /*---------------------------------------------------------------------*/
//    C_DbgStart( "MSサークル顧客情報更新（退会）" );
//    /*---------------------------------------------------------------------*/
//}

//    if(up_maternity_circle_flag == 1){

//        /* MSサークル顧客情報検索処理 */
    /*        EXEC SQL SELECT DISTINCT 顧客番号                                   */
    /*                   FROM MSサークル顧客情報@CMSD                             */
    /*                  WHERE 顧客番号     = :h_uid;                              */

//        /* データ無しエラー以外のエラーの場合処理を異常終了する */
//        if(sqlca.sqlcode != C_const_Ora_OK
//            && sqlca.sqlcode != C_const_Ora_NOTFOUND){
//if( DBG_LOG){
//                C_DbgMsg( "*** CheckMM *** MSサークル顧客情報検索%s\n", "");
//}
//                /* DBERR */
//                sprintf( out_format_buf, "顧客番号=[%s]",h_uid );
//                APLOG_WT( DEF_MSG_ID_904, 0, null, "SELECT", sqlca.sqlcode,
//                          "MSサークル顧客情報", out_format_buf, 0, 0);

//                /* 処理を終了する */
//                return(C_const_NG);
//        }

//        /* 更新対象がない場合、正常を返す */
//        if(sqlca.sqlcode == C_const_Ora_NOTFOUND){
//            /* 処理を終了する */
//            return C_const_OK;
//        }

//        /* MSサークル顧客情報更新 */
    /*        EXEC SQL UPDATE MSサークル顧客情報@CMSD                             */
    /*                    SET ＤＭ止め区分           = 3092,                      */
    /*                        Ｅメール止め区分       = 5092,                      */
    /*                        バッチ更新日           = :h_bat_yyyymmdd,           */
    /*                        最終更新日             = :h_bat_yyyymmdd,           */
    /*                        最終更新日時           = SYSDATE,                   */
    /*                        最終更新プログラムＩＤ = :h_programid               */
    /*                  WHERE 顧客番号 = :h_uid;                                  */

//        /* エラーの場合処理を異常終了する */
//        if (sqlca.sqlcode != C_const_Ora_OK ) {
//            /* DBERR */
//            sprintf( out_format_buf, "顧客番号=[%s]",h_uid );
//            APLOG_WT( DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
//                    "MSサークル顧客情報", out_format_buf, 0, 0);

//            /* 処理を終了する */
//            return C_const_NG;
//        }

//        /* MSサークル顧客情報更新件数カウントアップ */
//        mssk_data_taikai_cnt++;
//    }

//if( DBG_LOG){
//    /*---------------------------------------------*/
//    C_DbgEnd( "MSサークル顧客情報更新（退会）" ,0 ,0 ,0);
//    /*---------------------------------------------*/
//}
//    /* 処理を終了する */
//    return C_const_OK;

///*-----UpdateCircleKokyakuInfoForTaikai Bottom--------------------------------------*/
//}
    /* 2022/10/12 MCCM初版 DELEND */

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： UpdateKokyakuzokuseidataForTaikai                               */
    /*                                                                            */
    /*  書式                                                                      */
    /*  static int  UpdateKokyakuzokuseidataForTaikai()                           */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               MM顧客属性情報更新（退会）                                   */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      なし                                                                  */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*              1   ： 異常                                                   */
    /*                                                                            */

    /******************************************************************************/
    public int UpdateKokyakuzokuseidataForTaikai() {

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("MM顧客属性情報更新（退会）");
            /*---------------------------------------------------------------------*/
        }
        /* MM顧客属性情報更新処理 */
//        EXEC SQL UPDATE MM顧客属性情報
//        SET Ｅメールアドレス４     = NULL,
//        バッチ更新日           =  :h_bat_yyyymmdd,
//            最終更新日             =  :h_bat_yyyymmdd,
//            最終更新日時           =  sysdate,
//            最終更新プログラムＩＤ = :h_programid
//        WHERE 顧客番号       = :h_uid;

        StringDto sql = new StringDto();
        sql.arr = "UPDATE MM顧客属性情報" +
                "        SET Ｅメールアドレス４     = NULL," +
                "        バッチ更新日           =  ?," +
                "            最終更新日             =  ?," +
                "            最終更新日時           =  sysdate()," +
                "            最終更新プログラムＩＤ = ?" +
                "        WHERE 顧客番号       = ?";
        sqlca.sql = sql;
        sqlca.prepare();
        sqlca.query(h_bat_yyyymmdd, h_bat_yyyymmdd, h_programid, h_uid);

        /* エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=[%s]", h_uid);
            APLOG_WT(DEF_MSG_ID_904, 0, null, "UPDATE", sqlca.sqlcode,
                    "MM顧客属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            return C_const_NG;
        }

        if (DBG_LOG) {
            /*---------------------------------------------*/
            C_DbgEnd("MM顧客属性情報更新（退会）", 0, 0, 0);
            /*---------------------------------------------*/
        }
        /* 処理を終了する */
        return C_const_OK;
    }
}
