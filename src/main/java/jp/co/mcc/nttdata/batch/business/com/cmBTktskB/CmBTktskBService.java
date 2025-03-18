package jp.co.mcc.nttdata.batch.business.com.cmBTktskB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto.HSUCHIWAKE_INSERT;
import jp.co.mcc.nttdata.batch.business.com.cmBTktskB.dto.TS_SERCH;

public interface CmBTktskBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    public int  cmBTktskB_main( ItemDto  ts_ryokp_upd_cnt_buf,
                                ItemDto  hs_pdayp_ins_cnt_tujo_buf,
                                ItemDto  hs_pdayp_ins_cnt_kikan_buf);
    /* 顧客退会ポイント失効主処理         */
    public void        cmBTktskB_getYear(  ItemDto next_yyyy,
                                           ItemDto this_yyyy,   ItemDto ex_yyyy);
    /* 年度算出処理                       */
    public void        cmBTktskB_getYearCd( ItemDto year, ItemDto year_cd);
    /* 年度コード（全角）変換処理         */
    public void        cmBTktskB_getMonth(  int i_month, ItemDto month);
    /* 対象月（全角）変換処理             */
    public int  cmBTktskB_shikko( TS_SERCH ts_ser,
                                  ItemDto ts_ryokp_upd_cnt_buf,
                                  ItemDto hs_pdayp_ins_cnt_tujo_buf,
                                  ItemDto hs_pdayp_ins_cnt_kikan_buf);
    /* 顧客退会ポイント失効処理           */
    public void        cmBTktskB_setSql( TS_SERCH ts_ser_sql);
    /* SQL作成処理                        */
    public void        cmBTktskB_setSql_ora1555( TS_SERCH ts_ser_sql);
    /* SQL作成処理(ORA-1555発生時)        */
    public int  cmBTktskB_getTSriyo( TS_SERCH ts_ser_sql);
    /* TS利用可能ポイント情報取得         */
    public void        cmBTktskB_check_tujo_point( ItemDto ex_year,
                                                   ItemDto this_year,  ItemDto next_year);
    /* 通常ポイント失効状況確認処理       */
    public int  cmBTktskB_check_tujo_bit( int year);
    /* 通常Ｐ失効フラグ確認処理           */
    public void        cmBTktskB_check_kikan_point();
    /* 期間限定ポイント失効状況確認処理   */
    public int  cmBTktskB_check_kikan_bit(  int month);
    /* 期間限定Ｐ失効フラグ確認処理       */
    public int  cmBTktskB_pointLost( TS_SERCH ts_ser,
                                     ItemDto ts_ryokp_upd_cnt_buf,
                                     ItemDto hs_pdayp_ins_cnt_tujo_buf,
                                     ItemDto hs_pdayp_ins_cnt_kikan_buf);
    /* ポイント失効処理                   */
    public int  cmBTktskB_getMSCard();      /* MSカード情報取得処理               */
    public int  cmBTktskB_getTSYear(String month);
    /* TSポイント年別情報取得処理         */
    public int   cmBTktskB_getMSKokyaku(String month);
    /* MS顧客制度情報取得処理             */
    public int  cmBTktskB_getMSKazoku(String month);
    /* MS家族制度情報取得処理             */
    public int  cmBTktskB_updTS();          /* TS利用可能ポイント情報更新処理     */
    public int  cmBTktskB_tujo_insertHS( TS_SERCH ts_ser );
    /* HSポイント日別情報（通常ポイント失効）登録処理*/
    public int  cmBTktskB_kikan_insertHS( TS_SERCH ts_ser );
    /* HSポイント日別情報（期間限定ポイント失効）登録処理*/
    public void        cmBTktskB_setMonth(  int i_month, double kikan_p);
    /* 利用可能期間限定ＰＭＭ設定処理     */
    /* 2022/11/30 MCCM初版 ADD START */
    public int  cmBTktskB_getTSRank( TS_SERCH ts_ser); /* TSランク情報取得処理 */
    public int   cmBTktskB_insertHSUchiwake(int point_kbn, int meisai_su, HSUCHIWAKE_INSERT[] hsuchiwake_insert);
    /* HSポイント日別内訳情報（通常ポイント失効）登録処理     */
    public int daysInMonth(int year, int month);                                 /* 月末日を取得する */
    public int isLeapYear( int year );                                                   /* 閏年判定 */
}
