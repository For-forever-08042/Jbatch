package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前： ホスト変数構造体 HSポイント日別内訳情報 定義ファイル              */
/*                 HS_DAY_POINT_HIBETSU_UCHIWAKE_DATA.h                       */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      40.00 : 2022/09/06 SSI.岩井：MCCM初版                                 */
/*      40.01 : 2024/03/19 SSI.荻荘：企画ＩＤの型桁を変更                     */
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
/* HSポイント日別内訳情報構造体 */
public class HS_DAY_POINT_HIBETSU_UCHIWAKE_TBL  extends TBLBaseDto {

    public ItemDto system_ymd = new ItemDto();                                   /* システム年月日              PK */
    public ItemDto kokyaku_no = new ItemDto(15+1);                             /* 顧客番号                    PK */
    public ItemDto shori_seq = new ItemDto();                                    /* 処理通番                    PK */
    public ItemDto eda_no = new ItemDto();                                       /* 枝番                        PK */
    public ItemDto fuyo_riyo_kbn_01 = new ItemDto();                             /* 付与利用区分０１               */
    public ItemDto meisai_no_01 = new ItemDto();                                 /* 明細番号０１                   */
    public ItemDto kikaku_id_01 = new ItemDto();                                 /* 企画ＩＤ０１                   */
    public ItemDto kikaku_ver_01 = new ItemDto();                                /* 企画バージョン０１             */
    public ItemDto point_category_01 = new ItemDto();                            /* ポイントカテゴリ０１           */
    public ItemDto point_syubetsu_01 = new ItemDto();                            /* ポイント種別０１               */
    public ItemDto kaiage_daka_point_syubetsu_01 = new ItemDto();                /* 買上高ポイント種別０１         */
    public ItemDto fuyo_point_01 = new ItemDto();                                /* 付与ポイント０１               */
    public ItemDto riyo_point_01 = new ItemDto();                                /* 利用ポイント０１               */
    public ItemDto point_taisho_kingaku_01 = new ItemDto();                      /* ポイント対象金額０１           */
    public ItemDto jan_cd_01 = new ItemDto(13+1);                              /* ＪＡＮコード０１               */
    public ItemDto shohin_konyu_su_01 = new ItemDto();                           /* 商品購入数０１                 */
    public ItemDto shohin_percent_p_fuyoritsu_01 = new ItemDto();                /* 商品パーセントＰ付与率０１     */
    public ItemDto tujo_kikan_gentei_kbn_01 = new ItemDto();                     /* 通常期間限定区分０１           */
    public ItemDto point_yukokigen_01 = new ItemDto();                           /* ポイント有効期限０１           */
    public ItemDto kobai_kbn_01 = new ItemDto();                                 /* 購買区分０１                   */
    public ItemDto fuyo_riyo_kbn_02 = new ItemDto();                             /* 付与利用区分０２               */
    public ItemDto meisai_no_02 = new ItemDto();                                 /* 明細番号０２                   */
    public ItemDto kikaku_id_02 = new ItemDto();                                 /* 企画ＩＤ０２                   */
    public ItemDto kikaku_ver_02 = new ItemDto();                                /* 企画バージョン０２             */
    public ItemDto point_category_02 = new ItemDto();                            /* ポイントカテゴリ０２           */
    public ItemDto point_syubetsu_02 = new ItemDto();                            /* ポイント種別０２               */
    public ItemDto kaiage_daka_point_syubetsu_02 = new ItemDto();                /* 買上高ポイント種別０２         */
    public ItemDto fuyo_point_02 = new ItemDto();                                /* 付与ポイント０２               */
    public ItemDto riyo_point_02 = new ItemDto();                                /* 利用ポイント０２               */
    public ItemDto point_taisho_kingaku_02 = new ItemDto();                      /* ポイント対象金額０２           */
    public ItemDto jan_cd_02 = new ItemDto(13+1);                              /* ＪＡＮコード０２               */
    public ItemDto shohin_konyu_su_02 = new ItemDto();                           /* 商品購入数０２                 */
    public ItemDto shohin_percent_p_fuyoritsu_02 = new ItemDto();                /* 商品パーセントＰ付与率０２     */
    public ItemDto tujo_kikan_gentei_kbn_02 = new ItemDto();                     /* 通常期間限定区分０２           */
    public ItemDto point_yukokigen_02 = new ItemDto();                           /* ポイント有効期限０２           */
    public ItemDto kobai_kbn_02 = new ItemDto();                                 /* 購買区分０２                   */
    public ItemDto fuyo_riyo_kbn_03 = new ItemDto();                             /* 付与利用区分０３               */
    public ItemDto meisai_no_03 = new ItemDto();                                 /* 明細番号０３                   */
    public ItemDto kikaku_id_03 = new ItemDto();                                 /* 企画ＩＤ０３                   */
    public ItemDto kikaku_ver_03 = new ItemDto();                                /* 企画バージョン０３             */
    public ItemDto point_category_03 = new ItemDto();                            /* ポイントカテゴリ０３           */
    public ItemDto point_syubetsu_03 = new ItemDto();                            /* ポイント種別０３               */
    public ItemDto kaiage_daka_point_syubetsu_03 = new ItemDto();                /* 買上高ポイント種別０３         */
    public ItemDto fuyo_point_03 = new ItemDto();                                /* 付与ポイント０３               */
    public ItemDto riyo_point_03 = new ItemDto();                                /* 利用ポイント０３               */
    public ItemDto point_taisho_kingaku_03 = new ItemDto();                      /* ポイント対象金額０３           */
    public ItemDto jan_cd_03 = new ItemDto(13+1);                              /* ＪＡＮコード０３               */
    public ItemDto shohin_konyu_su_03 = new ItemDto();                           /* 商品購入数０３                 */
    public ItemDto shohin_percent_p_fuyoritsu_03 = new ItemDto();                /* 商品パーセントＰ付与率０３     */
    public ItemDto tujo_kikan_gentei_kbn_03 = new ItemDto();                     /* 通常期間限定区分０３           */
    public ItemDto point_yukokigen_03 = new ItemDto();                           /* ポイント有効期限０３           */
    public ItemDto kobai_kbn_03 = new ItemDto();                                 /* 購買区分０３                   */
    public ItemDto fuyo_riyo_kbn_04 = new ItemDto();                             /* 付与利用区分０４               */
    public ItemDto meisai_no_04 = new ItemDto();                                 /* 明細番号０４                   */
    public ItemDto kikaku_id_04 = new ItemDto();                                 /* 企画ＩＤ０４                   */
    public ItemDto kikaku_ver_04 = new ItemDto();                                /* 企画バージョン０４             */
    public ItemDto point_category_04 = new ItemDto();                            /* ポイントカテゴリ０４           */
    public ItemDto point_syubetsu_04 = new ItemDto();                            /* ポイント種別０４               */
    public ItemDto kaiage_daka_point_syubetsu_04 = new ItemDto();                /* 買上高ポイント種別０４         */
    public ItemDto fuyo_point_04 = new ItemDto();                                /* 付与ポイント０４               */
    public ItemDto riyo_point_04 = new ItemDto();                                /* 利用ポイント０４               */
    public ItemDto point_taisho_kingaku_04 = new ItemDto();                      /* ポイント対象金額０４           */
    public ItemDto jan_cd_04 = new ItemDto(13+1);                              /* ＪＡＮコード０４               */
    public ItemDto shohin_konyu_su_04 = new ItemDto();                           /* 商品購入数０４                 */
    public ItemDto shohin_percent_p_fuyoritsu_04 = new ItemDto();                /* 商品パーセントＰ付与率０４     */
    public ItemDto tujo_kikan_gentei_kbn_04 = new ItemDto();                     /* 通常期間限定区分０４           */
    public ItemDto point_yukokigen_04 = new ItemDto();                           /* ポイント有効期限０４           */
    public ItemDto kobai_kbn_04 = new ItemDto();                                 /* 購買区分０４                   */
    public ItemDto fuyo_riyo_kbn_05 = new ItemDto();                             /* 付与利用区分０５               */
    public ItemDto meisai_no_05 = new ItemDto();                                 /* 明細番号０５                   */
    public ItemDto kikaku_id_05 = new ItemDto();                                 /* 企画ＩＤ０５                   */
    public ItemDto kikaku_ver_05 = new ItemDto();                                /* 企画バージョン０５             */
    public ItemDto point_category_05 = new ItemDto();                            /* ポイントカテゴリ０５           */
    public ItemDto point_syubetsu_05 = new ItemDto();                            /* ポイント種別０５               */
    public ItemDto kaiage_daka_point_syubetsu_05 = new ItemDto();                /* 買上高ポイント種別０５         */
    public ItemDto fuyo_point_05 = new ItemDto();                                /* 付与ポイント０５               */
    public ItemDto riyo_point_05 = new ItemDto();                                /* 利用ポイント０５               */
    public ItemDto point_taisho_kingaku_05 = new ItemDto();                      /* ポイント対象金額０５           */
    public ItemDto jan_cd_05 = new ItemDto(13+1);                              /* ＪＡＮコード０５               */
    public ItemDto shohin_konyu_su_05 = new ItemDto();                           /* 商品購入数０５                 */
    public ItemDto shohin_percent_p_fuyoritsu_05 = new ItemDto();                /* 商品パーセントＰ付与率０５     */
    public ItemDto tujo_kikan_gentei_kbn_05 = new ItemDto();                     /* 通常期間限定区分０５           */
    public ItemDto point_yukokigen_05 = new ItemDto();                           /* ポイント有効期限０５           */
    public ItemDto kobai_kbn_05 = new ItemDto();                                 /* 購買区分０５                   */
    public ItemDto fuyo_riyo_kbn_06 = new ItemDto();                             /* 付与利用区分０６               */
    public ItemDto meisai_no_06 = new ItemDto();                                 /* 明細番号０６                   */
    public ItemDto kikaku_id_06 = new ItemDto();                                 /* 企画ＩＤ０６                   */
    public ItemDto kikaku_ver_06 = new ItemDto();                                /* 企画バージョン０６             */
    public ItemDto point_category_06 = new ItemDto();                            /* ポイントカテゴリ０６           */
    public ItemDto point_syubetsu_06 = new ItemDto();                            /* ポイント種別０６               */
    public ItemDto kaiage_daka_point_syubetsu_06 = new ItemDto();                /* 買上高ポイント種別０６         */
    public ItemDto fuyo_point_06 = new ItemDto();                                /* 付与ポイント０６               */
    public ItemDto riyo_point_06 = new ItemDto();                                /* 利用ポイント０６               */
    public ItemDto point_taisho_kingaku_06 = new ItemDto();                      /* ポイント対象金額０６           */
    public ItemDto jan_cd_06 = new ItemDto(13+1);                              /* ＪＡＮコード０６               */
    public ItemDto shohin_konyu_su_06 = new ItemDto();                           /* 商品購入数０６                 */
    public ItemDto shohin_percent_p_fuyoritsu_06 = new ItemDto();                /* 商品パーセントＰ付与率０６     */
    public ItemDto tujo_kikan_gentei_kbn_06 = new ItemDto();                     /* 通常期間限定区分０６           */
    public ItemDto point_yukokigen_06 = new ItemDto();                           /* ポイント有効期限０６           */
    public ItemDto kobai_kbn_06 = new ItemDto();                                 /* 購買区分０６                   */
    public ItemDto fuyo_riyo_kbn_07 = new ItemDto();                             /* 付与利用区分０７               */
    public ItemDto meisai_no_07 = new ItemDto();                                 /* 明細番号０７                   */
    public ItemDto kikaku_id_07 = new ItemDto();                                 /* 企画ＩＤ０７                   */
    public ItemDto kikaku_ver_07 = new ItemDto();                                /* 企画バージョン０７             */
    public ItemDto point_category_07 = new ItemDto();                            /* ポイントカテゴリ０７           */
    public ItemDto point_syubetsu_07 = new ItemDto();                            /* ポイント種別０７               */
    public ItemDto kaiage_daka_point_syubetsu_07 = new ItemDto();                /* 買上高ポイント種別０７         */
    public ItemDto fuyo_point_07 = new ItemDto();                                /* 付与ポイント０７               */
    public ItemDto riyo_point_07 = new ItemDto();                                /* 利用ポイント０７               */
    public ItemDto point_taisho_kingaku_07 = new ItemDto();                      /* ポイント対象金額０７           */
    public ItemDto jan_cd_07 = new ItemDto(13+1);                              /* ＪＡＮコード０７               */
    public ItemDto shohin_konyu_su_07 = new ItemDto();                           /* 商品購入数０７                 */
    public ItemDto shohin_percent_p_fuyoritsu_07 = new ItemDto();                /* 商品パーセントＰ付与率０７     */
    public ItemDto tujo_kikan_gentei_kbn_07 = new ItemDto();                     /* 通常期間限定区分０７           */
    public ItemDto point_yukokigen_07 = new ItemDto();                           /* ポイント有効期限０７           */
    public ItemDto kobai_kbn_07 = new ItemDto();                                 /* 購買区分０７                   */
    public ItemDto fuyo_riyo_kbn_08 = new ItemDto();                             /* 付与利用区分０８               */
    public ItemDto meisai_no_08 = new ItemDto();                                 /* 明細番号０８                   */
    public ItemDto kikaku_id_08 = new ItemDto();                                 /* 企画ＩＤ０８                   */
    public ItemDto kikaku_ver_08 = new ItemDto();                                /* 企画バージョン０８             */
    public ItemDto point_category_08 = new ItemDto();                            /* ポイントカテゴリ０８           */
    public ItemDto point_syubetsu_08 = new ItemDto();                            /* ポイント種別０８               */
    public ItemDto kaiage_daka_point_syubetsu_08 = new ItemDto();                /* 買上高ポイント種別０８         */
    public ItemDto fuyo_point_08 = new ItemDto();                                /* 付与ポイント０８               */
    public ItemDto riyo_point_08 = new ItemDto();                                /* 利用ポイント０８               */
    public ItemDto point_taisho_kingaku_08 = new ItemDto();                      /* ポイント対象金額０８           */
    public ItemDto jan_cd_08 = new ItemDto(13+1);                              /* ＪＡＮコード０８               */
    public ItemDto shohin_konyu_su_08 = new ItemDto();                           /* 商品購入数０８                 */
    public ItemDto shohin_percent_p_fuyoritsu_08 = new ItemDto();                /* 商品パーセントＰ付与率０８     */
    public ItemDto tujo_kikan_gentei_kbn_08 = new ItemDto();                     /* 通常期間限定区分０８           */
    public ItemDto point_yukokigen_08 = new ItemDto();                           /* ポイント有効期限０８           */
    public ItemDto kobai_kbn_08 = new ItemDto();                                 /* 購買区分０８                   */
    public ItemDto fuyo_riyo_kbn_09 = new ItemDto();                             /* 付与利用区分０９               */
    public ItemDto meisai_no_09 = new ItemDto();                                 /* 明細番号０９                   */
    public ItemDto kikaku_id_09 = new ItemDto();                                 /* 企画ＩＤ０９                   */
    public ItemDto kikaku_ver_09 = new ItemDto();                                /* 企画バージョン０９             */
    public ItemDto point_category_09 = new ItemDto();                            /* ポイントカテゴリ０９           */
    public ItemDto point_syubetsu_09 = new ItemDto();                            /* ポイント種別０９               */
    public ItemDto kaiage_daka_point_syubetsu_09 = new ItemDto();                /* 買上高ポイント種別０９         */
    public ItemDto fuyo_point_09 = new ItemDto();                                /* 付与ポイント０９               */
    public ItemDto riyo_point_09 = new ItemDto();                                /* 利用ポイント０９               */
    public ItemDto point_taisho_kingaku_09 = new ItemDto();                      /* ポイント対象金額０９           */
    public ItemDto jan_cd_09 = new ItemDto(13+1);                              /* ＪＡＮコード０９               */
    public ItemDto shohin_konyu_su_09 = new ItemDto();                           /* 商品購入数０９                 */
    public ItemDto shohin_percent_p_fuyoritsu_09 = new ItemDto();                /* 商品パーセントＰ付与率０９     */
    public ItemDto tujo_kikan_gentei_kbn_09 = new ItemDto();                     /* 通常期間限定区分０９           */
    public ItemDto point_yukokigen_09 = new ItemDto();                           /* ポイント有効期限０９           */
    public ItemDto kobai_kbn_09 = new ItemDto();                                 /* 購買区分０９                   */
    public ItemDto fuyo_riyo_kbn_10 = new ItemDto();                             /* 付与利用区分１０               */
    public ItemDto meisai_no_10 = new ItemDto();                                 /* 明細番号１０                   */
    public ItemDto kikaku_id_10 = new ItemDto();                                 /* 企画ＩＤ１０                   */
    public ItemDto kikaku_ver_10 = new ItemDto();                                /* 企画バージョン１０             */
    public ItemDto point_category_10 = new ItemDto();                            /* ポイントカテゴリ１０           */
    public ItemDto point_syubetsu_10 = new ItemDto();                            /* ポイント種別１０               */
    public ItemDto kaiage_daka_point_syubetsu_10 = new ItemDto();                /* 買上高ポイント種別１０         */
    public ItemDto fuyo_point_10 = new ItemDto();                                /* 付与ポイント１０               */
    public ItemDto riyo_point_10 = new ItemDto();                                /* 利用ポイント１０               */
    public ItemDto point_taisho_kingaku_10 = new ItemDto();                      /* ポイント対象金額１０           */
    public ItemDto jan_cd_10 = new ItemDto(13+1);                              /* ＪＡＮコード１０               */
    public ItemDto shohin_konyu_su_10 = new ItemDto();                           /* 商品購入数１０                 */
    public ItemDto shohin_percent_p_fuyoritsu_10 = new ItemDto();                /* 商品パーセントＰ付与率１０     */
    public ItemDto tujo_kikan_gentei_kbn_10 = new ItemDto();                     /* 通常期間限定区分１０           */
    public ItemDto point_yukokigen_10 = new ItemDto();                           /* ポイント有効期限１０           */
    public ItemDto kobai_kbn_10 = new ItemDto();                                 /* 購買区分１０                   */

}
