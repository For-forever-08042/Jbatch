package jp.co.mcc.nttdata.batch.business.service.cmBTkpskB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTkpskBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    /* 失効主処理                                 */
    public int cmBTkpskB_main(IntegerDto ts_upd_cnt, IntegerDto hs_ins_cnt,
                              IntegerDto shik_point_total);

    /* 失効予定情報取得処理                       */
    public int cmBTkpskB_getInfo();

    /* 基準更新月算出処理                         */
    public void cmBTkpskB_4monthAgo(StringDto month_ago, IntegerDto wk_year);

    /* 月全角変換処理                             */
    public void cmBTkpskB_getMonth(int i_month, StringDto month);

    /* 年度コード取得                             */
    public int[] cmBTkpskB_getYear(int next_yyyy, int this_yyyy, int ex_yyyy);                                  /* 当年度、前年度取得処理                     */

    public void cmBTkpskB_getYearCd(int year, StringDto year_cd);

    /* TBL検索用日付取得                          */
    public int cmBTkpskB_setNowInf();

    /* 期間限定P失効処理                          */
    public int cmBTkpskB_shikko(StringDto month01, StringDto month02, StringDto month03,
                         StringDto month04, StringDto month05, StringDto next_yyyy_cd,
                         StringDto this_yyyy_cd, StringDto ex_yyyy_cd,
                         int next_yyyy,
                         int this_yyyy,
                         int ex_yyyy,
                         IntegerDto hs_ins_cnt, IntegerDto ts_upd_cnt,
                         IntegerDto shik_point_total);

    /* TS利用可能ポイント情報取得                 */
    public int cmBTkpskB_getRiyoKanoPoint(StringDto month01, StringDto month02,
                                   StringDto month03, StringDto month04, StringDto month05,
                                   StringDto next_year_cd, StringDto this_year_cd, StringDto ex_year_cd);

    /* 失効ポイント登録処理                       */
    public int cmBTkpskB_pointLost(int this_yyyy);

    /* MSカード情報取得処理                       */
    public void cmBTkpskB_getMSCard();

    /* TSポイント年別情報取得処理                 */
    public void cmBTkpskB_getTSYear();

    /* MS顧客制度情報取得処理                     */
    public void cmBTkpskB_getMSKokyaku();

    /* MS家族制度情報取得                         */
    public void cmBTkpskB_getMSKazoku();

    /* HSポイント情報登録処理                     */
    public int cmBTkpskB_insertHS(int this_yyyy);

    /* 期間限定Ｐ失効フラグ確認処理               */
    public int cmBTkpskB_checkBit(int month);

    /* TS利用可能ポイント情報更新処理             */
    public int cmBTkpskB_updTS();

    /* 失効フラグ更新処理                         */
    public void cmBTkpskB_upBit();

    /* 更新利用可能期間限定Ｐ設定処理             */
    public void cmBTkpskB_setMonth(int i_month);


    /* 2022/12/01 MCCM初版 ADD START */
    /* TSランク情報取得処理     */
    public int cmBTkpskB_getTSRank();

    /* HSポイント日別内訳情報（期間限定ポイント失効）登録処理 */
    public int cmBTkpskB_kikan_insertHSUchiwake();
    /* 2022/12/01 MCCM初版 ADD END */

    /* 通常Ｐ失効フラグ確認処理           */
    public int cmBTkpskB_check_tujo_bit(int year);

    /* 通常ポイント失効状況確認処理       */
    public void cmBTkpskB_check_tujo_point(int ex_year, int this_year, int next_year);

    /* 期間限定ポイント失効状況確認処理   */
    public void cmBTkpskB_check_kikan_point();

}
