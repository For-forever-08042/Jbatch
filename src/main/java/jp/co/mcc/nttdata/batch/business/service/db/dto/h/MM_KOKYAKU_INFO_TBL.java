package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/
/*   名前： ホスト変数構造体 MM顧客情報 定義ファイル                          */
/*                 C_MM_KOKYAKU_INFO_DATA.h                                   */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*     40.00 :2022/09/06 SSI.岩井   ： MCCM初版                               */
/*     41.00 :2023/05/24 SSI.石     ： MCCMPH2                                */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MM顧客情報構造体 */
public class MM_KOKYAKU_INFO_TBL extends TBLBaseDto {
  public ItemDto  kokyaku_no = new ItemDto(15+1);                 /* 顧客番号          PK */
  public ItemDto kyumin_flg = new ItemDto();                       /* 休眠フラグ           */
  public ItemDto kokyaku_mesho = new ItemDto(80*3+1);            /* 顧客名称             */
  public ItemDto kokyaku_kana_mesho = new ItemDto(40*3+1);       /* 顧客カナ名称         */
  public ItemDto nenre = new ItemDto();                            /* 年齢                 */
  public ItemDto tanjo_y = new ItemDto();                          /* 誕生年               */
  public ItemDto tanjo_m = new ItemDto();                          /* 誕生月               */
  public ItemDto tanjo_d = new ItemDto();                          /* 誕生日               */
  public ItemDto sebetsu = new ItemDto();                          /* 性別                 */
  public ItemDto konin = new ItemDto();                            /* 婚姻                 */
  public ItemDto nyukai_kigyo_cd = new ItemDto();                  /* 入会企業コード       */
  public ItemDto nyukai_tenpo = new ItemDto();                     /* 入会店舗             */
  public ItemDto hakken_kigyo_cd = new ItemDto();                  /* 発券企業コード       */
  public ItemDto hakken_tenpo = new ItemDto();                     /* 発券店舗             */
  public ItemDto shain_kbn = new ItemDto();                        /* 社員区分             */
  public ItemDto portal_nyukai_ymd = new ItemDto();                /* ポータル入会年月日   */
  public ItemDto portal_taikai_ymd = new ItemDto();                /* ポータル退会年月日   */
  public ItemDto sagyo_kigyo_cd = new ItemDto();                   /* 作業企業コード       */
  public ItemDto  sagyosha_id = new ItemDto();                      /* 作業者ＩＤ           */
  public ItemDto sagyo_ymd = new ItemDto();                        /* 作業年月日           */
  public ItemDto sagyo_hms = new ItemDto();                        /* 作業時刻             */
  public ItemDto batch_koshin_ymd = new ItemDto();                 /* バッチ更新日         */
  public ItemDto  saishu_setai_ymd = new ItemDto();                 /* 最終静態更新日       */
  public ItemDto  saishu_setai_hms = new ItemDto();                 /* 最終静態更新時刻     */
  public ItemDto  saishu_koshin_ymd = new ItemDto();                /* 最終更新日           */
  public ItemDto  saishu_koshin_ymdhms = new ItemDto();             /* 最終更新日時         */
  public ItemDto saishu_koshin_programid = new ItemDto(20+1);    /* 最終更新プログラムＩＤ */
  public ItemDto kokyaku_myoji = new ItemDto(40*3+1);            /* 顧客名字             */
  public ItemDto kokyaku_name = new ItemDto(40*3+1);             /* 顧客名前             */
  public ItemDto kana_kokyaku_myoji = new ItemDto(40*3+1);       /* カナ顧客名字         */
  public ItemDto kana_kokyaku_name = new ItemDto(40*3+1);        /* カナ顧客名前         */
  public ItemDto  kokyaku_status = new ItemDto();                   /* 顧客ステータス       */
  public ItemDto  nyukai_entry_paper_ymd = new ItemDto();           /* 入会申込用紙記載日   */
  public ItemDto  seitai_touroku_ymd = new ItemDto();               /* 静態登録日           */
  public ItemDto touroku_cd = new ItemDto(5+1);                  /* 登録コード           */
  public ItemDto goopon_kaiin_pw = new ItemDto(32+1);            /* ＧＯＯＰＯＮ会員パスワード */
  public ItemDto  nyukai_kaisha_cd_mcc = new ItemDto();             /* 入会会社コードＭＣＣ */
  public ItemDto  nyukai_tenpo_mcc = new ItemDto();                 /* 入会店舗ＭＣＣ       */
  public ItemDto ap_user_id = new ItemDto(36+1);                 /* アプリユーザＩＤ     */
  public ItemDto  senior = new ItemDto();                           /* シニア               */
  public ItemDto  zokusei_kanri_shutai_system = new ItemDto();      /* 属性管理主体システム */
  public ItemDto nickname = new ItemDto(40*3+1);                 /* ニックネーム         */
  public ItemDto  push_tsuchikyoka_flg = new ItemDto();             /* プッシュ通知許可フラグ */
  public ItemDto  mail_address_1_soshin_flg = new ItemDto();        /* メールアドレス１送信フラグ */
  public ItemDto  mail_address_2_soshin_flg = new ItemDto();        /* メールアドレス２送信フラグ */
  public ItemDto  mail_address_3_soshin_flg = new ItemDto();        /* メールアドレス３送信フラグ */
  /* 2023/05/24 MCCMPH2 ADD START */
  public ItemDto keiyaku_no = new ItemDto(16+1);                 /* 契約番号             */
  public ItemDto kyu_keiyaku_no = new ItemDto(16+1);             /* 旧契約番号           */
  public ItemDto  teikeisaki_soshiki_cd_3 = new ItemDto();          /* 提携先組織コード３   */
  public ItemDto  henkan_funo_moji_umu_kbn = new ItemDto();         /* 変換不能文字有無区分 */
  /* 2023/05/24 MCCMPH2 ADD END */
}
