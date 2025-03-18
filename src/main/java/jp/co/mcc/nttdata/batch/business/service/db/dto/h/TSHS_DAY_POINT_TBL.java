package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前： ホスト変数構造体 TS/HSポイント日別情報 定義ファイル               */
/*                 TSHS_DAY_POINT_DATA.h                                      */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*       1.00 :2012/11/02 SSI 越後谷 ： 初版                                  */
/*       2.00 :2013/11/13 SSI 本田   ： サークルＩＤ追加                      */
/*      30.00 :2020/11/20 NDBS 緒方  : 期間限定Ｐ項目追加                     */
/*      31.00 :2021/09/10 SSI 張  : MK店番号,MK取引番号                       */
/*      40.00 :2022/09/06 SSI.岩井：MCCM初版                                  */
/*      40.01 :2024/03/19 SSI.荻荘：来店ポイント付与対象区分を追加            */
/*----------------------------------------------------------------------------*/
/*   $Id:$                                                                    */
/*----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                  */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* TS/HSポイント日別情報構造体 */
public class TSHS_DAY_POINT_TBL  extends TBLBaseDto {

    public ItemDto system_ymd = new ItemDto();                                   /* システム年月日              PK */
    public ItemDto kokyaku_no = new ItemDto(15 + 1);                             /* 顧客番号                    PK */
    public ItemDto  shori_seq = new ItemDto();                                    /* 処理通番                    PK */
    public ItemDto kaiin_kigyo_cd = new ItemDto();                               /* 会員企業コード                 */
    public ItemDto  kaiin_kyu_hansya_cd = new ItemDto();                          /* 会員旧販社コード               */
    public ItemDto kaiin_no = new ItemDto(18 + 1);                               /* 会員番号                       */
    public ItemDto nyukai_kigyo_cd = new ItemDto();                              /* 入会企業コード                 */
    public ItemDto nyukai_tenpo = new ItemDto();                                 /* 入会店舗                       */
    public ItemDto hakken_kigyo_cd = new ItemDto();                              /* 発券企業コード                 */
    public ItemDto hakken_tenpo = new ItemDto();                                 /* 発券店舗                       */
    public ItemDto  seisan_ymd = new ItemDto();                                   /* 精算年月日                     */
    public ItemDto  toroku_ymd = new ItemDto();                                   /* 登録年月日                     */
    public ItemDto  data_ymd = new ItemDto();                                     /* データ年月日                   */
    public ItemDto  kigyo_cd = new ItemDto();                                     /* 企業コード                     */
    public ItemDto  mise_no = new ItemDto();                                      /* 店番号                         */
    public ItemDto  terminal_no = new ItemDto();                                  /* ターミナル番号                 */
    public ItemDto  torihiki_no = new ItemDto();                                  /* 取引番号                       */
    public ItemDto  jikoku_hms = new ItemDto();                                   /* 時刻                           */
    public ItemDto  riyu_cd = new ItemDto();                                      /* 理由コード                     */
    public ItemDto  circle_id = new ItemDto();                                    /* サークルＩＤ                   */
    public ItemDto  card_nyuryoku_kbn = new ItemDto();                            /* カード入力区分                 */
    public ItemDto  shori_taisho_file_record_no = new ItemDto();                  /* 処理対象ファイルレコード番号   */
    public ItemDto  real_koshin_flg = new ItemDto();                              /* リアル更新フラグ               */
    public ItemDto  fuyo_point = new ItemDto();                                   /* 付与ポイント                   */
    public ItemDto  riyo_point = new ItemDto();                                   /* 利用ポイント                   */
    public ItemDto  kihon_pritsu_taisho_point = new ItemDto();                    /* 基本Ｐ率対象ポイント           */
    public ItemDto  rankup_taisho_kingaku = new ItemDto();                        /* ランクＵＰ対象金額             */
    public ItemDto  point_taisho_kingaku = new ItemDto();                         /* ポイント対象金額               */
    public ItemDto  service_hakko_maisu = new ItemDto();                          /* サービス券発行枚数             */
    public ItemDto  service_riyo_maisu = new ItemDto();                           /* サービス券利用枚数             */
    public ItemDto  kojin_getuji_rank_cd = new ItemDto();                         /* 個人月次ランクコード           */
    public ItemDto  kojin_nenji_rank_cd = new ItemDto();                          /* 個人年次ランクコード           */
    public ItemDto  kazoku_getuji_rank_cd = new ItemDto();                        /* 家族月次ランクコード           */
    public ItemDto  kazoku_nenji_rank_cd = new ItemDto();                         /* 家族年次ランクコード           */
    public ItemDto  shiyo_rank_cd = new ItemDto();                                /* 使用ランクコード               */
    public ItemDto  kaiage_kingaku = new ItemDto();                               /* 買上額                         */
    public ItemDto  kaiage_cnt = new ItemDto();                                   /* 買上回数                       */
    public ItemDto  koshinmae_riyo_kano_point = new ItemDto();                    /* 更新前利用可能ポイント         */
    public ItemDto  koshinmae_fuyo_point = new ItemDto();                         /* 更新前付与ポイント             */
    public ItemDto  koshinmae_kihon_pritsu_taisho_point = new ItemDto();          /* 更新前基本Ｐ率対象ポイント     */
    public ItemDto  koshinmae_gekkan_kojin_rankup_taisho_kingaku = new ItemDto(); /* 更新前月間ランクＵＰ対象金額   */
    public ItemDto  koshinmae_nenkan_kojin_rankup_taisho_kingaku = new ItemDto(); /* 更新前年間ランクＵＰ対象金額   */
    public ItemDto  koshinmae_point_taisho_kingaku = new ItemDto();               /* 更新前ポイント対象金額         */
    public ItemDto  koshinmae_kaiage_kingaku = new ItemDto();                     /* 更新前買上額                   */
    public ItemDto kazoku_id = new ItemDto(10 + 1);                              /* 家族ＩＤ                       */
    public ItemDto  koshinmae_gekkan_kazoku_rankup_taisho_kingaku = new ItemDto();/* 更新前月間家族ランクＵＰ金額   */
    public ItemDto  koshinmae_nenkan_kazoku_rankup_taisho_kingaku = new ItemDto();/* 更新前年間家族ランクＵＰ金額   */
    public ItemDto  real_koshin_ymd = new ItemDto();                              /* リアル更新日時                 */
    public ItemDto real_koshin_apl_version = new ItemDto(20 + 1);                /* リアル更新ＡＰＬバージョン     */
    public ItemDto  delay_koshin_ymd = new ItemDto();                             /* ディレイ更新日時               */
    public ItemDto delay_koshin_apl_version = new ItemDto(20 + 1);               /* ディレイ更新ＡＰＬバージョン   */
    public ItemDto  sosai_flg = new ItemDto();                                    /* 相殺フラグ                     */
    public ItemDto  mesai_check_flg = new ItemDto();                              /* 明細チェックフラグ             */
    public ItemDto  mesai_check_kbn = new ItemDto();                              /* 明細チェック区分               */
    public ItemDto  sagyo_kigyo_cd = new ItemDto();                               /* 作業企業コード                 */
    public ItemDto  sagyosha_id = new ItemDto();                                  /* 作業者ＩＤ                     */
    public ItemDto  sagyo_ymd = new ItemDto();                                    /* 作業年月日                     */
    public ItemDto  sagyo_hms = new ItemDto();                                    /* 作業時刻                       */
    public ItemDto  batch_koshin_ymd = new ItemDto();                             /* バッチ更新日                   */
    public ItemDto  saishu_koshin_ymd = new ItemDto();                            /* 最終更新日                     */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();                         /* 最終更新日時                   */
    public ItemDto saishu_koshin_programid = new ItemDto(20 + 1);                /* 最終更新プログラムＩＤ         */
    public ItemDto  yokyu_riyo_putiwake_flg = new ItemDto();                      /* 要求利用Ｐ内訳フラグ           */
    public ItemDto  koshinmae_riyo_kano_tujo_point_kijun_nendo = new ItemDto();   /* 更新前利用可能通常Ｐ基準年度   */
    public ItemDto  koshinmae_riyo_kano_tujo_point_zennendo = new ItemDto();      /* 更新前利用可能通常Ｐ前年度     */
    public ItemDto  koshinmae_riyo_kano_tujo_point_tonendo = new ItemDto();       /* 更新前利用可能通常Ｐ当年度     */
    public ItemDto  koshinmae_riyo_kano_tujo_point_yokunendo = new ItemDto();     /* 更新前利用可能通常Ｐ翌年度     */
    public ItemDto  yokyu_fuyo_tujo_point = new ItemDto();                        /* 要求付与通常Ｐ                 */
    public ItemDto  yokyu_fuyo_tujo_point_kijun_nendo = new ItemDto();            /* 要求付与通常Ｐ基準年度         */
    public ItemDto  yokyu_fuyo_tujo_poin_zennendo = new ItemDto();                /* 要求付与通常Ｐ前年度           */
    public ItemDto  yokyu_fuyo_tujo_poin_tonendo = new ItemDto();                 /* 要求付与通常Ｐ当年度           */
    public ItemDto  yokyu_riyo_tujo_point = new ItemDto();                        /* 要求利用通常Ｐ                 */
    public ItemDto  yokyu_riyo_tujo_point_kijun_nendo = new ItemDto();            /* 要求利用通常Ｐ基準年度         */
    public ItemDto  yokyu_riyo_tujo_point_zennendo = new ItemDto();               /* 要求利用通常Ｐ前年度           */
    public ItemDto  yokyu_riyo_tujo_point_tonendo = new ItemDto();                /* 要求利用通常Ｐ当年度           */
    public ItemDto  yokyu_riyo_tujo_point_yokunendo = new ItemDto();              /* 要求利用通常Ｐ翌年度           */
    public ItemDto  koshin_fuyo_tujo_point = new ItemDto();                       /* 更新付与通常Ｐ                 */
    public ItemDto  koshin_fuyo_tujo_point_kijun_nendo = new ItemDto();           /* 更新付与通常Ｐ基準年度         */
    public ItemDto  koshin_fuyo_tujo_point_zennendo = new ItemDto();              /* 更新付与通常Ｐ前年度           */
    public ItemDto  koshin_fuyo_tujo_point_tonendo = new ItemDto();               /* 更新付与通常Ｐ当年度           */
    public ItemDto  koshin_riyo_tujo_point = new ItemDto();                       /* 更新利用通常Ｐ                 */
    public ItemDto  koshin_riyo_tujo_point_kijun_nendo = new ItemDto();           /* 更新利用通常Ｐ基準年度         */
    public ItemDto  koshin_riyo_tujo_point_zennendo = new ItemDto();              /* 更新利用通常Ｐ前年度           */
    public ItemDto  koshin_riyo_tujo_point_tonendo = new ItemDto();               /* 更新利用通常Ｐ当年度           */
    public ItemDto  koshin_riyo_tujo_point_yokunendo = new ItemDto();             /* 更新利用通常Ｐ翌年度           */
    public ItemDto  koshinmae_kikan_gentei_point_kijun_month = new ItemDto();     /* 更新前期間限定Ｐ基準月         */
    public ItemDto  koshinmae_riyo_kano_kikan_gentei_point0 = new ItemDto();      /* 更新前利用可能期間限定Ｐ０     */
    public ItemDto  koshinmae_riyo_kano_kikan_gentei_point1 = new ItemDto();      /* 更新前利用可能期間限定Ｐ１     */
    public ItemDto  koshinmae_riyo_kano_kikan_gentei_point2 = new ItemDto();      /* 更新前利用可能期間限定Ｐ２     */
    public ItemDto  koshinmae_riyo_kano_kikan_gentei_point3 = new ItemDto();      /* 更新前利用可能期間限定Ｐ３     */
    public ItemDto  koshinmae_riyo_kano_kikan_gentei_point4 = new ItemDto();      /* 更新前利用可能期間限定Ｐ４     */
    public ItemDto  yokyu_fuyo_kikan_gentei_point = new ItemDto();                /* 要求付与期間限定Ｐ             */
    public ItemDto  yokyu_fuyo_kikan_gentei_point_kijun_month = new ItemDto();    /* 要求付与期間限定Ｐ基準月       */
    public ItemDto  yokyu_fuyo_kikan_gentei_point0 = new ItemDto();               /* 要求付与期間限定Ｐ０           */
    public ItemDto  yokyu_fuyo_kikan_gentei_point1 = new ItemDto();               /* 要求付与期間限定Ｐ１           */
    public ItemDto  yokyu_fuyo_kikan_gentei_point2 = new ItemDto();               /* 要求付与期間限定Ｐ２           */
    public ItemDto  yokyu_fuyo_kikan_gentei_point3 = new ItemDto();               /* 要求付与期間限定Ｐ３           */
    public ItemDto  yokyu_riyo_kikan_gentei_point = new ItemDto();                /* 要求利用期間限定Ｐ             */
    public ItemDto  yokyu_riyo_kikan_gentei_point_kijun_month = new ItemDto();    /* 要求利用期間限定Ｐ基準月       */
    public ItemDto  yokyu_riyo_kikan_gentei_point0 = new ItemDto();               /* 要求利用期間限定Ｐ０           */
    public ItemDto  yokyu_riyo_kikan_gentei_point1 = new ItemDto();               /* 要求利用期間限定Ｐ１           */
    public ItemDto  yokyu_riyo_kikan_gentei_point2 = new ItemDto();               /* 要求利用期間限定Ｐ２           */
    public ItemDto  yokyu_riyo_kikan_gentei_point3 = new ItemDto();               /* 要求利用期間限定Ｐ３           */
    public ItemDto  yokyu_riyo_kikan_gentei_point4 = new ItemDto();               /* 要求利用期間限定Ｐ４           */
    public ItemDto  koshin_fuyo_kikan_gentei_point = new ItemDto();               /* 更新付与期間限定Ｐ             */
    public ItemDto  koshin_fuyo_kikan_gentei_point_kijun_month = new ItemDto();   /* 更新付与期間限定Ｐ基準月       */
    public ItemDto  koshin_fuyo_kikan_gentei_point01 = new ItemDto();             /* 更新付与期間限定Ｐ０１         */
    public ItemDto  koshin_fuyo_kikan_gentei_point02 = new ItemDto();             /* 更新付与期間限定Ｐ０２         */
    public ItemDto  koshin_fuyo_kikan_gentei_point03 = new ItemDto();             /* 更新付与期間限定Ｐ０３         */
    public ItemDto  koshin_fuyo_kikan_gentei_point04 = new ItemDto();             /* 更新付与期間限定Ｐ０４         */
    public ItemDto  koshin_fuyo_kikan_gentei_point05 = new ItemDto();             /* 更新付与期間限定Ｐ０５         */
    public ItemDto  koshin_fuyo_kikan_gentei_point06 = new ItemDto();             /* 更新付与期間限定Ｐ０６         */
    public ItemDto  koshin_fuyo_kikan_gentei_point07 = new ItemDto();             /* 更新付与期間限定Ｐ０７         */
    public ItemDto  koshin_fuyo_kikan_gentei_point08 = new ItemDto();             /* 更新付与期間限定Ｐ０８         */
    public ItemDto  koshin_fuyo_kikan_gentei_point09 = new ItemDto();             /* 更新付与期間限定Ｐ０９         */
    public ItemDto  koshin_fuyo_kikan_gentei_point10 = new ItemDto();             /* 更新付与期間限定Ｐ１０         */
    public ItemDto  koshin_fuyo_kikan_gentei_point11 = new ItemDto();             /* 更新付与期間限定Ｐ１１         */
    public ItemDto  koshin_fuyo_kikan_gentei_point12 = new ItemDto();             /* 更新付与期間限定Ｐ１２         */
    public ItemDto  koshin_riyo_kikan_gentei_point = new ItemDto();               /* 更新利用期間限定Ｐ             */
    public ItemDto  koshin_riyo_kikan_gentei_point_kijun_month = new ItemDto();   /* 更新利用期間限定Ｐ基準月       */
    public ItemDto  koshin_riyo_kikan_gentei_point01 = new ItemDto();             /* 更新利用期間限定Ｐ０１         */
    public ItemDto  koshin_riyo_kikan_gentei_point02 = new ItemDto();             /* 更新利用期間限定Ｐ０２         */
    public ItemDto  koshin_riyo_kikan_gentei_point03 = new ItemDto();             /* 更新利用期間限定Ｐ０３         */
    public ItemDto  koshin_riyo_kikan_gentei_point04 = new ItemDto();             /* 更新利用期間限定Ｐ０４         */
    public ItemDto  koshin_riyo_kikan_gentei_point05 = new ItemDto();             /* 更新利用期間限定Ｐ０５         */
    public ItemDto  koshin_riyo_kikan_gentei_point06 = new ItemDto();             /* 更新利用期間限定Ｐ０６         */
    public ItemDto  koshin_riyo_kikan_gentei_point07 = new ItemDto();             /* 更新利用期間限定Ｐ０７         */
    public ItemDto  koshin_riyo_kikan_gentei_point08 = new ItemDto();             /* 更新利用期間限定Ｐ０８         */
    public ItemDto  koshin_riyo_kikan_gentei_point09 = new ItemDto();             /* 更新利用期間限定Ｐ０９         */
    public ItemDto  koshin_riyo_kikan_gentei_point10 = new ItemDto();             /* 更新利用期間限定Ｐ１０         */
    public ItemDto  koshin_riyo_kikan_gentei_point11 = new ItemDto();             /* 更新利用期間限定Ｐ１１         */
    public ItemDto  koshin_riyo_kikan_gentei_point12 = new ItemDto();             /* 更新利用期間限定Ｐ１２         */
    public ItemDto  mk_mise_no = new ItemDto();                                   /* MK店番号                       */
    public ItemDto  mk_torihiki_no = new ItemDto();                               /* MK取引番号                     */
    public ItemDto  card_syubetsu = new ItemDto();                                /* カード種別                     */
    public ItemDto henko_id = new ItemDto(10 + 1);                               /* 変更ＩＤ                       */
    public ItemDto  soshin_ymdhms = new ItemDto();                                /* 送信日時                       */
    public ItemDto  kounyu_ymdhms = new ItemDto();                                /* 購入日時                       */
    public ItemDto  denbun_seq = new ItemDto();                                   /* 電文ＳＥＱ番号                 */
    public ItemDto  uketsuke_seq = new ItemDto();                                 /* 受付ＳＥＱ番号                 */
    public ItemDto  torikeshi_taisho_denbun_seq = new ItemDto();                  /* 取消対象電文ＳＥＱ番号         */
    public ItemDto  point_shiharai_kingaku = new ItemDto();                       /* ポイント支払金額               */
    public ItemDto  rank_hanteiyou_point_shiharai_kingaku = new ItemDto();        /* ランク判定用ポイント支払金額   */
    public ItemDto  tasha_credit_kbn = new ItemDto();                             /* 他社クレジット区分             */
    public ItemDto pos_shubetsu = new ItemDto(1 + 1);                            /* ＰＯＳ種別                     */
    public ItemDto touroku_keiro = new ItemDto(1 + 1);                           /* 登録経路                       */
    public ItemDto torihiki_kbn = new ItemDto(5 + 1);                            /* 取引区分                       */
    public ItemDto  kaisha_cd_mcc = new ItemDto();                                /* 会社コードＭＣＣ               */
    public ItemDto  mise_no_mcc = new ItemDto();                                  /* 店番号ＭＣＣ                   */
    public ItemDto  kangen_shubetsu = new ItemDto();                              /* 還元種別                       */
    public ItemDto  meisai_su = new ItemDto();                                    /* 明細数                         */
    public ItemDto  kifu_kikaku_no = new ItemDto();                               /* 寄付企画番号                   */
    public ItemDto  kifu_kikaku_no_edaban = new ItemDto();                        /* 寄付企画番号枝番               */
    public ItemDto  nyukai_kaisha_cd_mcc = new ItemDto();                         /* 入会会社コードＭＣＣ           */
    public ItemDto  nyukai_tenpo_mcc = new ItemDto();                             /* 入会店舗ＭＣＣ                 */
    public ItemDto  point_jouto_taisho_service_shubetsu = new ItemDto();          /* ポイント譲渡対象サービス種別   */
    public ItemDto point_jouto_taisho_kaiin_no = new ItemDto(18 + 1);            /* ポイント譲渡対象会員番号       */
    public ItemDto  raiten_point_fuyo_taisyo_kbn = new ItemDto();                 /* 来店ポイント付与対象区分       */

}
