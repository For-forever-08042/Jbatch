package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/******************************************************************************/
/*   名前： ホスト変数構造体 MS家族制度情報 定義ファイル                      */
/*                 C_MS_KAZOKU_SEDO_INFO.h                                    */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*     40.00 :2022/09/06 SSI.岩井   ： MCCM初版                               */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MS家族制度情報構造体 */
public class MS_KAZOKU_SEDO_INFO_TBL  extends TBLBaseDto {

    public ItemDto kazoku_id = new  ItemDto(10+1);                  /* 家族ＩＤ          PK */
    /*    VARCHAR         kazoku_oya_kokyaku_no; */     /* 家族親顧客番号       */
    public ItemDto kazoku_1_kokyaku_no = new  ItemDto(15+1);        /* 家族１顧客番号       */
    public ItemDto kazoku_2_kokyaku_no = new  ItemDto(15+1);        /* 家族２顧客番号       */
    public ItemDto kazoku_3_kokyaku_no = new  ItemDto(15+1);        /* 家族３顧客番号       */
    public ItemDto kazoku_4_kokyaku_no = new  ItemDto(15+1);        /* 家族４顧客番号       */
    public ItemDto kazoku_5_kokyaku_no = new  ItemDto(15+1);        /* 家族５顧客番号       */
    public ItemDto kazoku_6_kokyaku_no = new  ItemDto(15+1);        /* 家族６顧客番号       */
    /*    public ItemDto kazoku_oya_toroku_ymd;   */         /* 家族親登録日         */
    public ItemDto  kazoku_1_toroku_ymd = new ItemDto();              /* 家族１登録日         */
    public ItemDto  kazoku_2_toroku_ymd = new ItemDto();              /* 家族２登録日         */
    public ItemDto  kazoku_3_toroku_ymd = new ItemDto();              /* 家族３登録日         */
    public ItemDto  kazoku_4_toroku_ymd = new ItemDto();              /* 家族４登録日         */
    public ItemDto  kazoku_5_toroku_ymd = new ItemDto();              /* 家族５登録日         */
    public ItemDto  kazoku_6_toroku_ymd = new ItemDto();              /* 家族６登録日         */
    public ItemDto kazoku_1_toroku_time = new ItemDto();             /* 家族１登録時刻       */
    public ItemDto kazoku_2_toroku_time = new ItemDto();             /* 家族２登録時刻       */
    public ItemDto kazoku_3_toroku_time = new ItemDto();             /* 家族３登録時刻       */
    public ItemDto kazoku_4_toroku_time = new ItemDto();             /* 家族４登録時刻       */
    public ItemDto kazoku_5_toroku_time = new ItemDto();             /* 家族５登録時刻       */
    public ItemDto kazoku_6_toroku_time = new ItemDto();             /* 家族６登録時刻       */
    public ItemDto kazoku_sakusei_ymd = new ItemDto();               /* 家族作成日           */
    public ItemDto kazoku_rankup_kingaku_saishu_koshin_ymd = new ItemDto(); /* 家族ランクＵＰ金額最終更新日 */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_0 = new ItemDto();   /* 年間家族ランクＵＰ対象金額０ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_1 = new ItemDto();   /* 年間家族ランクＵＰ対象金額１ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_2 = new ItemDto();   /* 年間家族ランクＵＰ対象金額２ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_3 = new ItemDto();   /* 年間家族ランクＵＰ対象金額３ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_4 = new ItemDto();   /* 年間家族ランクＵＰ対象金額４ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_5 = new ItemDto();   /* 年間家族ランクＵＰ対象金額５ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_6 = new ItemDto();   /* 年間家族ランクＵＰ対象金額６ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_7 = new ItemDto();   /* 年間家族ランクＵＰ対象金額７ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_8 = new ItemDto();   /* 年間家族ランクＵＰ対象金額８ */
    public ItemDto  nenkan_kazoku_rankup_taisho_kingaku_9 = new ItemDto();   /* 年間家族ランクＵＰ対象金額９ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_001 = new ItemDto(); /* 月間家族ランクＵＰ金額００１ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_002 = new ItemDto(); /* 月間家族ランクＵＰ金額００２ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_003 = new ItemDto(); /* 月間家族ランクＵＰ金額００３ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_004 = new ItemDto(); /* 月間家族ランクＵＰ金額００４ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_005 = new ItemDto(); /* 月間家族ランクＵＰ金額００５ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_006 = new ItemDto(); /* 月間家族ランクＵＰ金額００６ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_007 = new ItemDto(); /* 月間家族ランクＵＰ金額００７ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_008 = new ItemDto(); /* 月間家族ランクＵＰ金額００８ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_009 = new ItemDto(); /* 月間家族ランクＵＰ金額００９ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_010 = new ItemDto(); /* 月間家族ランクＵＰ金額０１０ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_011 = new ItemDto(); /* 月間家族ランクＵＰ金額０１１ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_012 = new ItemDto(); /* 月間家族ランクＵＰ金額０１２ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_101 = new ItemDto(); /* 月間家族ランクＵＰ金額１０１ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_102 = new ItemDto(); /* 月間家族ランクＵＰ金額１０２ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_103 = new ItemDto(); /* 月間家族ランクＵＰ金額１０３ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_104 = new ItemDto(); /* 月間家族ランクＵＰ金額１０４ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_105 = new ItemDto(); /* 月間家族ランクＵＰ金額１０５ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_106 = new ItemDto(); /* 月間家族ランクＵＰ金額１０６ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_107 = new ItemDto(); /* 月間家族ランクＵＰ金額１０７ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_108 = new ItemDto(); /* 月間家族ランクＵＰ金額１０８ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_109 = new ItemDto(); /* 月間家族ランクＵＰ金額１０９ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_110 = new ItemDto(); /* 月間家族ランクＵＰ金額１１０ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_111 = new ItemDto(); /* 月間家族ランクＵＰ金額１１１ */
    public ItemDto  gekkan_kazoku_rankup_taisho_kingaku_112 = new ItemDto(); /* 月間家族ランクＵＰ金額１１２ */
    public ItemDto  nenji_rank_cd_0 = new ItemDto();                  /* 年次ランクコード０     */
    public ItemDto  nenji_rank_cd_1 = new ItemDto();                  /* 年次ランクコード１     */
    public ItemDto  nenji_rank_cd_2 = new ItemDto();                  /* 年次ランクコード２     */
    public ItemDto  nenji_rank_cd_3 = new ItemDto();                  /* 年次ランクコード３     */
    public ItemDto  nenji_rank_cd_4 = new ItemDto();                  /* 年次ランクコード４     */
    public ItemDto  nenji_rank_cd_5 = new ItemDto();                  /* 年次ランクコード５     */
    public ItemDto  nenji_rank_cd_6 = new ItemDto();                  /* 年次ランクコード６     */
    public ItemDto  nenji_rank_cd_7 = new ItemDto();                  /* 年次ランクコード７     */
    public ItemDto  nenji_rank_cd_8 = new ItemDto();                  /* 年次ランクコード８     */
    public ItemDto  nenji_rank_cd_9 = new ItemDto();                  /* 年次ランクコード９     */
    public ItemDto  getuji_rank_cd_001 = new ItemDto();               /* 月次ランクコード００１ */
    public ItemDto  getuji_rank_cd_002 = new ItemDto();               /* 月次ランクコード００２ */
    public ItemDto  getuji_rank_cd_003 = new ItemDto();               /* 月次ランクコード００３ */
    public ItemDto  getuji_rank_cd_004 = new ItemDto();               /* 月次ランクコード００４ */
    public ItemDto  getuji_rank_cd_005 = new ItemDto();               /* 月次ランクコード００５ */
    public ItemDto  getuji_rank_cd_006 = new ItemDto();               /* 月次ランクコード００６ */
    public ItemDto  getuji_rank_cd_007 = new ItemDto();               /* 月次ランクコード００７ */
    public ItemDto  getuji_rank_cd_008 = new ItemDto();               /* 月次ランクコード００８ */
    public ItemDto  getuji_rank_cd_009 = new ItemDto();               /* 月次ランクコード００９ */
    public ItemDto  getuji_rank_cd_010 = new ItemDto();               /* 月次ランクコード０１０ */
    public ItemDto  getuji_rank_cd_011 = new ItemDto();               /* 月次ランクコード０１１ */
    public ItemDto  getuji_rank_cd_012 = new ItemDto();               /* 月次ランクコード０１２ */
    public ItemDto  getuji_rank_cd_101 = new ItemDto();               /* 月次ランクコード１０１ */
    public ItemDto  getuji_rank_cd_102 = new ItemDto();               /* 月次ランクコード１０２ */
    public ItemDto  getuji_rank_cd_103 = new ItemDto();               /* 月次ランクコード１０３ */
    public ItemDto  getuji_rank_cd_104 = new ItemDto();               /* 月次ランクコード１０４ */
    public ItemDto  getuji_rank_cd_105 = new ItemDto();               /* 月次ランクコード１０５ */
    public ItemDto  getuji_rank_cd_106 = new ItemDto();               /* 月次ランクコード１０６ */
    public ItemDto  getuji_rank_cd_107 = new ItemDto();               /* 月次ランクコード１０７ */
    public ItemDto  getuji_rank_cd_108 = new ItemDto();               /* 月次ランクコード１０８ */
    public ItemDto  getuji_rank_cd_109 = new ItemDto();               /* 月次ランクコード１０９ */
    public ItemDto  getuji_rank_cd_110 = new ItemDto();               /* 月次ランクコード１１０ */
    public ItemDto  getuji_rank_cd_111 = new ItemDto();               /* 月次ランクコード１１１ */
    public ItemDto  getuji_rank_cd_112 = new ItemDto();               /* 月次ランクコード１１２ */
    public ItemDto  kazoku_sakujo_ymd = new ItemDto();                /* 家族削除日             */
    public ItemDto  sagyo_kigyo_cd = new ItemDto();                   /* 作業企業コード         */
    public ItemDto  sagyosha_id = new ItemDto();                      /* 作業者ＩＤ             */
    public ItemDto  sagyo_ymd = new ItemDto();                        /* 作業年月日             */
    public ItemDto  sagyo_hms = new ItemDto();                        /* 作業時刻               */
    public ItemDto  batch_koshin_ymd = new ItemDto();                 /* バッチ更新日           */
    public ItemDto  saishu_koshin_ymd = new ItemDto();                /* 最終更新日             */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();             /* 最終更新日時           */
    public ItemDto saishu_koshin_programid = new  ItemDto(20+1);    /* 最終更新プログラムＩＤ */
}
