package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前   ： ホスト変数構造体 PS店表示情報ＭＣＣ 定義ファイル               */
/*                 PS_MISE_HYOJI_INFO_MCC.h                                   */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      40.00 :2022/09/05 SSI.岩井   ： MCCM初版                              */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/* PS店表示情報ＭＣＣ構造体                                                         */
/*----------------------------------------------------------------------------*/
public class PS_MISE_HYOJI_INFO_MCC_TBL  extends TBLBaseDto {

    public ItemDto kaisha_cd = new ItemDto();                      /* 会社コード          PK */
    public ItemDto mise_no = new ItemDto();                        /* 店番号              PK */
    public ItemDto kaishi_ymd = new ItemDto();                     /* 開始年月日          PK */
    public ItemDto shuryo_ymd = new ItemDto();                     /* 終了年月日             */
    public ItemDto kaisha_name = new ItemDto(80*3+1);            /* 会社名                 */
    public ItemDto kaisha_name_kana = new ItemDto(50*3+1);       /* 会社名カナ             */
    public ItemDto kaisha_tanshuku_name = new ItemDto(32*3+1);   /* 会社短縮名             */
    public ItemDto gyotai_cd = new ItemDto();                      /* 業態コード             */
    public ItemDto kyu_kigyo_cd = new ItemDto();                   /* 旧企業コード           */
    public ItemDto sv_block_cd = new ItemDto(6+1);               /* ＳＶブロックコード     */
    public ItemDto sv_block_meisho = new ItemDto(40*3+1);        /* ＳＶブロック名称       */
    public ItemDto sv_block_meisho_kana = new ItemDto(60*3+1);   /* ＳＶブロック名称カナ   */
    public ItemDto kanji_tenpo_meisho = new ItemDto(80*3+1);     /* 漢字店舗名称           */
    public ItemDto tenpo_kana_meisho = new ItemDto(60*3+1);      /* 店舗カナ名称           */
    public ItemDto tenpo_tanshuku_meisho = new ItemDto(40*3+1);  /* 店舗短縮名称           */
    public ItemDto tenpo_keitai_kbn = new ItemDto();               /* 店舗形態区分           */
    public ItemDto chokuei_kbn = new ItemDto();                    /* 直営区分               */
    public ItemDto area_cd = new ItemDto();                        /* エリアコード           */
    public ItemDto kaiten_ymd = new ItemDto();                     /* 開店日                 */
    public ItemDto heiten_ymd = new ItemDto();                     /* 閉店日                 */
    public ItemDto cyozai_otc_kbn = new ItemDto();                 /* 調剤ＯＴＣ区分         */
    public ItemDto kaso_tenpo_flg = new ItemDto();                 /* 仮想店舗フラグ         */
    public ItemDto zettai_tenban = new ItemDto();                  /* 絶対店番               */
    public ItemDto omuni_chanel_taisho_tenpo_flg = new ItemDto();  /* オムニチャネル対象店舗フラグ       */
    public ItemDto myshop_touroku_kahi_flg = new ItemDto();        /* マイショップ登録可否フラグ         */
    public ItemDto torioki_taisho_tenpo_flg = new ItemDto();       /* 取り置き対象店舗フラグ */
    public ItemDto toriyose_taisho_tenpo_flg = new ItemDto();      /* 取り寄せ対象店舗フラグ */
    public ItemDto kaiso_mail_tsuchi_kahi_flg = new ItemDto();     /* 改装メール通知可否フラグ */
    public ItemDto tenpo_takuhai_kahi_flg = new ItemDto();         /* 店舗宅配可否フラグ     */
    public ItemDto denwa_no = new ItemDto(14+1);                 /* 電話番号               */
    public ItemDto sap_mise_no = new ItemDto(8+1);               /* ＳＡＰ連携店舗番号     */
    public ItemDto sap_renkei_flg = new ItemDto();                 /* ＳＡＰ連携フラグ       */
    public ItemDto dwh_mise_no = new ItemDto(8+1);           /* ＤＷＨ連携時変換店舗コード */
    public ItemDto erp_mise_no = new ItemDto(8+1);           /* ＥＲＰ連携時変換店舗コード */
    public ItemDto hyp_renkei_flg = new ItemDto();                 /* ＨＹＰ連携フラグ       */
    public ItemDto hyp_mise_no = new ItemDto(8+1);           /* ＨＹＰ連携時変換店舗コード */
    public ItemDto sagyo_kigyo_cd = new ItemDto();                 /* 作業企業コード         */
    public ItemDto sagyosha_id = new ItemDto();                    /* 作業者ＩＤ             */
    public ItemDto sagyo_ymd = new ItemDto();                      /* 作業年月日             */
    public ItemDto sagyo_hms = new ItemDto();                      /* 作業時刻               */
    public ItemDto batch_koshin_ymd = new ItemDto();               /* バッチ更新日           */
    public ItemDto saishu_koshin_ymd = new ItemDto();              /* 最終更新日             */
    public ItemDto saishu_koshin_ymdhms = new ItemDto();           /* 最終更新日時           */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);  /* 最終更新プログラムＩＤ */

}
