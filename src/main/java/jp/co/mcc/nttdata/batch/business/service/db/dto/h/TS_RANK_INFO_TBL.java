package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

/*******************************************************************************/
/*   名前： ホスト変数構造体 TSランク情報 定義ファイル                         */
/*                 TS_RANK_INFO.h                                              */
/*-----------------------------------------------------------------------------*/
/*   稼働環境                                                                  */
/*      Red Hat Enterprise Linux 5（64bit）                                    */
/*      (文字コード ： UTF8)                                                   */
/*-----------------------------------------------------------------------------*/
/*   改定履歴                                                                  */
/*      40.00 : 2022/09/06 SSI.岩井：MCCM初版                                  */
/*-----------------------------------------------------------------------------*/
/*   $Id:$                                                                     */
/*-----------------------------------------------------------------------------*/
/*   Copyright (C) 2012 NTT DATA CORPORATION                                   */

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/*******************************************************************************/

/*-----------------------------------------------------------------------------*/
/*グローバル変数                                                               */
/*-----------------------------------------------------------------------------*/
/* TSランク情報構造体 */
public class TS_RANK_INFO_TBL  extends TBLBaseDto {

    public ItemDto kokyaku_no = new ItemDto(15+1);                       /* 顧客番号                    PK */
    public ItemDto nenji_rank_cd_0 = new ItemDto();                        /* 年次ランクコード０             */
    public ItemDto nenji_rank_cd_1 = new ItemDto();                        /* 年次ランクコード１             */
    public ItemDto  nenji_rank_cd_2 = new ItemDto();                        /* 年次ランクコード２             */
    public ItemDto  nenji_rank_cd_3 = new ItemDto();                        /* 年次ランクコード３             */
    public ItemDto  nenji_rank_cd_4 = new ItemDto();                        /* 年次ランクコード４             */
    public ItemDto  nenji_rank_cd_5 = new ItemDto();                        /* 年次ランクコード５             */
    public ItemDto  nenji_rank_cd_6 = new ItemDto();                        /* 年次ランクコード６             */
    public ItemDto  nenji_rank_cd_7 = new ItemDto();                        /* 年次ランクコード７             */
    public ItemDto  nenji_rank_cd_8 = new ItemDto();                        /* 年次ランクコード８             */
    public ItemDto  nenji_rank_cd_9 = new ItemDto();                        /* 年次ランクコード９             */
    public ItemDto  nenkan_rankup_taisho_kingaku_0 = new ItemDto();         /* 年間ランクＵＰ対象金額０       */
    public ItemDto  nenkan_rankup_taisho_kingaku_1 = new ItemDto();         /* 年間ランクＵＰ対象金額１       */
    public ItemDto  nenkan_rankup_taisho_kingaku_2 = new ItemDto();         /* 年間ランクＵＰ対象金額２       */
    public ItemDto  nenkan_rankup_taisho_kingaku_3 = new ItemDto();         /* 年間ランクＵＰ対象金額３       */
    public ItemDto  nenkan_rankup_taisho_kingaku_4 = new ItemDto();         /* 年間ランクＵＰ対象金額４       */
    public ItemDto  nenkan_rankup_taisho_kingaku_5 = new ItemDto();         /* 年間ランクＵＰ対象金額５       */
    public ItemDto  nenkan_rankup_taisho_kingaku_6 = new ItemDto();         /* 年間ランクＵＰ対象金額６       */
    public ItemDto  nenkan_rankup_taisho_kingaku_7 = new ItemDto();         /* 年間ランクＵＰ対象金額７       */
    public ItemDto  nenkan_rankup_taisho_kingaku_8 = new ItemDto();         /* 年間ランクＵＰ対象金額８       */
    public ItemDto  nenkan_rankup_taisho_kingaku_9 = new ItemDto();         /* 年間ランクＵＰ対象金額９       */
    public ItemDto  getsuji_rank_cd_004 = new ItemDto();                    /* 月次ランクコード００４         */
    public ItemDto  getsuji_rank_cd_005 = new ItemDto();                    /* 月次ランクコード００５         */
    public ItemDto  getsuji_rank_cd_006 = new ItemDto();                    /* 月次ランクコード００６         */
    public ItemDto  getsuji_rank_cd_007 = new ItemDto();                    /* 月次ランクコード００７         */
    public ItemDto  getsuji_rank_cd_008 = new ItemDto();                    /* 月次ランクコード００８         */
    public ItemDto  getsuji_rank_cd_009 = new ItemDto();                    /* 月次ランクコード００９         */
    public ItemDto  getsuji_rank_cd_010 = new ItemDto();                    /* 月次ランクコード０１０         */
    public ItemDto  getsuji_rank_cd_011 = new ItemDto();                    /* 月次ランクコード０１１         */
    public ItemDto  getsuji_rank_cd_012 = new ItemDto();                    /* 月次ランクコード０１２         */
    public ItemDto  getsuji_rank_cd_001 = new ItemDto();                    /* 月次ランクコード００１         */
    public ItemDto  getsuji_rank_cd_002 = new ItemDto();                    /* 月次ランクコード００２         */
    public ItemDto  getsuji_rank_cd_003 = new ItemDto();                    /* 月次ランクコード００３         */
    public ItemDto  getsuji_rank_cd_104 = new ItemDto();                    /* 月次ランクコード１０４         */
    public ItemDto  getsuji_rank_cd_105 = new ItemDto();                    /* 月次ランクコード１０５         */
    public ItemDto  getsuji_rank_cd_106 = new ItemDto();                    /* 月次ランクコード１０６         */
    public ItemDto  getsuji_rank_cd_107 = new ItemDto();                    /* 月次ランクコード１０７         */
    public ItemDto  getsuji_rank_cd_108 = new ItemDto();                    /* 月次ランクコード１０８         */
    public ItemDto  getsuji_rank_cd_109 = new ItemDto();                    /* 月次ランクコード１０９         */
    public ItemDto  getsuji_rank_cd_110 = new ItemDto();                    /* 月次ランクコード１１０         */
    public ItemDto  getsuji_rank_cd_111 = new ItemDto();                    /* 月次ランクコード１１１         */
    public ItemDto  getsuji_rank_cd_112 = new ItemDto();                    /* 月次ランクコード１１２         */
    public ItemDto  getsuji_rank_cd_101 = new ItemDto();                    /* 月次ランクコード１０１         */
    public ItemDto  getsuji_rank_cd_102 = new ItemDto();                    /* 月次ランクコード１０２         */
    public ItemDto  getsuji_rank_cd_103 = new ItemDto();                    /* 月次ランクコード１０３         */
    public ItemDto  gekkan_rankup_taisho_kingaku_004 = new ItemDto();       /* 月間ランクＵＰ対象金額００４   */
    public ItemDto  gekkan_rankup_taisho_kingaku_005 = new ItemDto();       /* 月間ランクＵＰ対象金額００５   */
    public ItemDto  gekkan_rankup_taisho_kingaku_006 = new ItemDto();       /* 月間ランクＵＰ対象金額００６   */
    public ItemDto  gekkan_rankup_taisho_kingaku_007 = new ItemDto();       /* 月間ランクＵＰ対象金額００７   */
    public ItemDto  gekkan_rankup_taisho_kingaku_008 = new ItemDto();       /* 月間ランクＵＰ対象金額００８   */
    public ItemDto  gekkan_rankup_taisho_kingaku_009 = new ItemDto();       /* 月間ランクＵＰ対象金額００９   */
    public ItemDto  gekkan_rankup_taisho_kingaku_010 = new ItemDto();       /* 月間ランクＵＰ対象金額０１０   */
    public ItemDto  gekkan_rankup_taisho_kingaku_011 = new ItemDto();       /* 月間ランクＵＰ対象金額０１１   */
    public ItemDto  gekkan_rankup_taisho_kingaku_012 = new ItemDto();       /* 月間ランクＵＰ対象金額０１２   */
    public ItemDto  gekkan_rankup_taisho_kingaku_001 = new ItemDto();       /* 月間ランクＵＰ対象金額００１   */
    public ItemDto  gekkan_rankup_taisho_kingaku_002 = new ItemDto();       /* 月間ランクＵＰ対象金額００２   */
    public ItemDto  gekkan_rankup_taisho_kingaku_003 = new ItemDto();       /* 月間ランクＵＰ対象金額００３   */
    public ItemDto  gekkan_rankup_taisho_kingaku_104 = new ItemDto();       /* 月間ランクＵＰ対象金額１０４   */
    public ItemDto  gekkan_rankup_taisho_kingaku_105 = new ItemDto();       /* 月間ランクＵＰ対象金額１０５   */
    public ItemDto  gekkan_rankup_taisho_kingaku_106 = new ItemDto();       /* 月間ランクＵＰ対象金額１０６   */
    public ItemDto  gekkan_rankup_taisho_kingaku_107 = new ItemDto();       /* 月間ランクＵＰ対象金額１０７   */
    public ItemDto  gekkan_rankup_taisho_kingaku_108 = new ItemDto();       /* 月間ランクＵＰ対象金額１０８   */
    public ItemDto  gekkan_rankup_taisho_kingaku_109 = new ItemDto();       /* 月間ランクＵＰ対象金額１０９   */
    public ItemDto  gekkan_rankup_taisho_kingaku_110 = new ItemDto();       /* 月間ランクＵＰ対象金額１１０   */
    public ItemDto  gekkan_rankup_taisho_kingaku_111 = new ItemDto();       /* 月間ランクＵＰ対象金額１１１   */
    public ItemDto  gekkan_rankup_taisho_kingaku_112 = new ItemDto();       /* 月間ランクＵＰ対象金額１１２   */
    public ItemDto  gekkan_rankup_taisho_kingaku_101 = new ItemDto();       /* 月間ランクＵＰ対象金額１０１   */
    public ItemDto  gekkan_rankup_taisho_kingaku_102 = new ItemDto();       /* 月間ランクＵＰ対象金額１０２   */
    public ItemDto  gekkan_rankup_taisho_kingaku_103 = new ItemDto();       /* 月間ランクＵＰ対象金額１０３   */
    public ItemDto  gekkan_premium_point_kingaku_004 = new ItemDto();       /* 月間プレミアムポイント数００４ */
    public ItemDto  gekkan_premium_point_kingaku_005 = new ItemDto();       /* 月間プレミアムポイント数００５ */
    public ItemDto  gekkan_premium_point_kingaku_006 = new ItemDto();       /* 月間プレミアムポイント数００６ */
    public ItemDto  gekkan_premium_point_kingaku_007 = new ItemDto();       /* 月間プレミアムポイント数００７ */
    public ItemDto  gekkan_premium_point_kingaku_008 = new ItemDto();       /* 月間プレミアムポイント数００８ */
    public ItemDto  gekkan_premium_point_kingaku_009 = new ItemDto();       /* 月間プレミアムポイント数００９ */
    public ItemDto  gekkan_premium_point_kingaku_010 = new ItemDto();       /* 月間プレミアムポイント数０１０ */
    public ItemDto  gekkan_premium_point_kingaku_011 = new ItemDto();       /* 月間プレミアムポイント数０１１ */
    public ItemDto  gekkan_premium_point_kingaku_012 = new ItemDto();       /* 月間プレミアムポイント数０１２ */
    public ItemDto  gekkan_premium_point_kingaku_001 = new ItemDto();       /* 月間プレミアムポイント数００１ */
    public ItemDto  gekkan_premium_point_kingaku_002 = new ItemDto();       /* 月間プレミアムポイント数００２ */
    public ItemDto  gekkan_premium_point_kingaku_003 = new ItemDto();       /* 月間プレミアムポイント数００３ */
    public ItemDto  gekkan_premium_point_kingaku_104 = new ItemDto();       /* 月間プレミアムポイント数１０４ */
    public ItemDto  gekkan_premium_point_kingaku_105 = new ItemDto();       /* 月間プレミアムポイント数１０５ */
    public ItemDto  gekkan_premium_point_kingaku_106 = new ItemDto();       /* 月間プレミアムポイント数１０６ */
    public ItemDto  gekkan_premium_point_kingaku_107 = new ItemDto();       /* 月間プレミアムポイント数１０７ */
    public ItemDto  gekkan_premium_point_kingaku_108 = new ItemDto();       /* 月間プレミアムポイント数１０８ */
    public ItemDto  gekkan_premium_point_kingaku_109 = new ItemDto();       /* 月間プレミアムポイント数１０９ */
    public ItemDto  gekkan_premium_point_kingaku_110 = new ItemDto();       /* 月間プレミアムポイント数１１０ */
    public ItemDto  gekkan_premium_point_kingaku_111 = new ItemDto();       /* 月間プレミアムポイント数１１１ */
    public ItemDto  gekkan_premium_point_kingaku_112 = new ItemDto();       /* 月間プレミアムポイント数１１２ */
    public ItemDto  gekkan_premium_point_kingaku_101 = new ItemDto();       /* 月間プレミアムポイント数１０１ */
    public ItemDto  gekkan_premium_point_kingaku_102 = new ItemDto();       /* 月間プレミアムポイント数１０２ */
    public ItemDto  gekkan_premium_point_kingaku_103 = new ItemDto();       /* 月間プレミアムポイント数１０３ */
    public ItemDto  saishu_koshin_ymd = new ItemDto();                      /* 最終更新日                     */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();                   /* 最終更新日時                   */
    public ItemDto saishu_koshin_programid = new ItemDto(20+1);          /* 最終更新プログラムＩＤ         */

}
