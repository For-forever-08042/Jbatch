package jp.co.mcc.nttdata.batch.business.service.cmBTpmdfB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_KAZOKU_SEDO_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RANK_INFO_TBL;

public interface CmBTpmdfBService {

    public int Chk_ArgiInf(StringDto Arg_in );
    public int ReadFile();
    public int OpenInFile();
    public int OpenOutFile();
    public int UpdatePoint(IntegerDto in_rec_cnt, IntegerDto ok_rec_cnt, IntegerDto total_cnt, IntegerDto ng_rec_cnt,
                           IntegerDto hsph_in_rec_cnt,
                           IntegerDto tspn_up_rec_cnt, IntegerDto tspr_up_rec_cnt,
                           IntegerDto tspu_up_rec_cnt, IntegerDto tskp_up_rec_cnt);
    public int GetUid(int kigyo_cd,StringDto kaiin_no,StringDto kokyaku_no);
    public int CheckTenpo(int kigyo_cd, int tenpo_cd);
    public int GetKokyakuTeikeiRank(StringDto kokyaku_no,
                                   StringDto kokyaku_name,
                                    IntegerDto kojin_honnen_rank_cd,
                                    IntegerDto kojin_tougetu_rank_cd,
                                    IntegerDto kazoku_honnen_rank_cd,
                                    IntegerDto kazoku_tougetu_rank_cd,
                                    IntegerDto kazoku_id);
    public int InsertDayPoint(IntegerDto in_rec_cnt,
                              IntegerDto hsph_in_rec_cnt,
                              IntegerDto tspn_up_rec_cnt,
                              IntegerDto tspr_up_rec_cnt,
                              IntegerDto tskp_up_rec_cnt,
                              StringDto kokyaku_no,
                              int kojin_honnen_rank_cd,
                              int kojin_tougetu_rank_cd,
                              int kazoku_honnen_rank_cd,
                              int kazoku_tougetu_rank_cd,
                              int kazoku_id, CmBTpmdfBServiceImpl.CHECK_OVER_YM check_over_ym);

    public int getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL ksMaster, IntegerDto status);
    public void  getAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn , int year, int month, StringDto value);
    public void  setAboutRankColumn(int tbl_kbn, int ym_kbn, int colkbn , int year, int month, double value);
    public int TsRiyoKanoPointUpdate(int uard_flg);   /* 2022/10/04 MCCM初版 MOD */
    public void cmBTpmdfB_getYear(IntegerDto next_yyyy,
                                 IntegerDto this_yyyy, IntegerDto ex_yyyy);
    /* 年度算出処理                       */
    public void cmBTpmdfB_getYearCd(IntegerDto year, StringDto year_cd);
    /* 年度コード（全角）変換処理         */
    public void cmBTpmdfB_getMonth(int i_month, StringDto month);
    /* 対象月（全角）変換処理             */
    public int  cmBTpmdfB_check_kijun_nendo();
    /* 通常Ｐ基準年度チェック処理         */
    public int  cmBTpmdfB_check_kijun_month();
    /* 期間限定Ｐ基準月チェック処理       */
    public int  cmBTpmdfB_write_err();
    /* エラー書き込み処理                 */
    public void  cmBTpmdfB_check_over_years(CmBTpmdfBServiceImpl.CHECK_OVER_YM c);
    /* 年跨ぎ判定処理                     */
    public void  cmBTpmdfB_check_over_months(CmBTpmdfBServiceImpl.CHECK_OVER_YM c);
    /* 月跨り判定処理                     */
    public int  cmBTpmdfB_getTSriyo();      /* TS利用可能ポイント情報取得処理     */
    public int  cmBTpmdfB_check_updval(CmBTpmdfBServiceImpl.CHECK_OVER_YM check_over_ym);
    /* 期間限定Ｐ更新値マイナスチェック   */
    public void  cmBTpmdfB_setMonth(int i_month, double kikan_p);
    /* ポイント日別．更新付与期間限定Ｐ設定処理         */
    public void  cmBTpmdfB_setKikanPoint(int i_month, double kikan_p);
    /* 期間限定ポイント情報．付与期間限定Ｐ設定処理     */
    public int  cmBTpmdfB_write_file();     /* ポイント修正結果書き込み処理       */
    /* 2022/09/27 MCCM初版 ADD START */
    public int GetTsRankInfo(TS_RANK_INFO_TBL tsRankInfo, IntegerDto status);  /* TSランク情報取得 */
    public int GetCardShubetsu();                                  /* カード種別の取得処理 */
    public int UpdateTsRankInfo(int year, int month);              /* TSランク情報更新 */
    public void  GetYukokigen(int point_flg, IntegerDto yukokigen);                       /* 有効期限の取得 */
    public int daysInMonth(int year, int month);                                 /* 月末日を取得する */
    public int isLeapYear( int year );                                                   /* 閏年判定 */
    /* HSポイント日別内訳情報追加処理 */
    public int InsertDayPointUchiwake();
    /* 2022/09/27 MCCM初版 ADD END */

}
