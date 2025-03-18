package jp.co.mcc.nttdata.batch.business.service.cmBTpmdfB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;

public class CmBTpmdfBDto {

    public TS_YEAR_POINT_TBL tonen_point_data = new TS_YEAR_POINT_TBL();    /* 当年ポイント情報     */
    public TS_YEAR_POINT_TBL    zennen_point_data = new TS_YEAR_POINT_TBL();   /* 前年ポイント情報     */
    public TS_YEAR_POINT_TBL    zenzennen_point_data = new TS_YEAR_POINT_TBL();/* 前々年ポイント情報   */
    public TSHS_DAY_POINT_TBL honjitsu_point_data = new TSHS_DAY_POINT_TBL(); /* 本日ポイント日別情報 */
    public TS_YEAR_POINT_TBL    point_year_data = new TS_YEAR_POINT_TBL();     /* 当年ポイント年別情報 */
    public TS_TOTAL_POINT_TBL   total_point_data = new TS_TOTAL_POINT_TBL();    /* ポイント累計情報     */
    /* 2021/02/17 TS期間限定ポイント情報追加 Start*/
    public TS_KIKAN_POINT_TBL kikan_point_data = new TS_KIKAN_POINT_TBL();    /* 期間限定ポイント情報     */
    /* 2021/02/17 TS期間限定ポイント情報追加 End*/
    public MM_KOKYAKU_INFO_TBL csMaster = new MM_KOKYAKU_INFO_TBL();            /* MM顧客情報           */
    public MS_CARD_INFO_TBL ms_card_data = new MS_CARD_INFO_TBL();        /* MSカード情報         */
    public MS_KOKYAKU_SEDO_INFO_TBL kokyaku_seido_data = new MS_KOKYAKU_SEDO_INFO_TBL(); /* MS顧客制度情報    */
    public MS_KAZOKU_SEDO_INFO_TBL     mskased_t = new MS_KAZOKU_SEDO_INFO_TBL();    /* MS家族制度情報バッファ      */
    /*TS_RIYO_KANO_POINT_TBL  tsrkpoint_t;*/  /* TS利用可能ポイント情報バッファ */
    public MS_RANKBETSU_POINT_INFO_TBL msrk_buff = new MS_RANKBETSU_POINT_INFO_TBL();    /* MSランク別ボーナスポイント情報 */
    public PS_MISE_HYOJI_INFO_TBL      ps_mise_hyoji_date = new PS_MISE_HYOJI_INFO_TBL();   /* 店表示情報 */
    public ItemDto h_bat_date = new ItemDto();                           /* バッチ処理日付(当日)                 */
    /* 2022/09/27 MCCM初版 ADD START */
    public TS_RANK_INFO_TBL h_ts_rank_info_data = new TS_RANK_INFO_TBL();     /* TSランク情報データ */
    public PS_MISE_HYOJI_INFO_MCC_TBL ps_mise_hyoji_mcc_data = new PS_MISE_HYOJI_INFO_MCC_TBL();   /* 店表示情報MCC */
    public HS_DAY_POINT_HIBETSU_UCHIWAKE_TBL hs_day_point_hibetsu_uchiwake_data = new HS_DAY_POINT_HIBETSU_UCHIWAKE_TBL(); /* HSポイント日別内訳情報 */

    public POINT_SHUSEI_RESULT point_shuse_data = new POINT_SHUSEI_RESULT();
    public TS_TBL h_ts = new TS_TBL();

    /* ファイル修正データ構造体 */
    public class POINT_SHUSEI_RESULT {
        public ItemDto gyomu_ymd = new ItemDto();                            /* 業務日付         */
        public ItemDto shori_seq = new ItemDto();                            /* 処理通番         */
        public ItemDto irai_kigyo = new ItemDto();                           /* 依頼企業コード   */
        public ItemDto irai_tenpo = new ItemDto();                           /* 依頼店舗         */
        public ItemDto shuse_taisho_nen = new ItemDto();                     /* 修正対象年       */
        public ItemDto shuse_taisho_tsuki = new ItemDto();                   /* 修正対象月       */
        public ItemDto data_ymd = new ItemDto();                             /* データ年月日     */
        public ItemDto kigyo_code = new ItemDto();                           /* 企業コード       */
        public ItemDto kaiin_no = new ItemDto(7+1);                       /* 会員番号         */
        public ItemDto kokyaku_name = new ItemDto(80*3+1);                 /* 顧客名称         */
        public ItemDto kaiage_nissu_koshin_mae = new ItemDto();              /* 買上日数更新前   */
        public ItemDto rankup_taisho_kingaku_koshin_mae = new ItemDto();/* ランクＵＰ対象金額更新前 */
        public ItemDto rankup_taisho_kingaku_kagenti = new ItemDto();        /* ランクＵＰ対象金額加減値 */
        public ItemDto riyo_kano_point_koshin_mae = new ItemDto();          /* 利用可能ポイント更新前 */
        /*double          riyo_kano_point_kagenti;*/       /* 利用可能ポイント加減値 */
        public ItemDto tujo_point_kagenti_zennen = new ItemDto();          /* 通常Ｐ加減値（前年）       */
        public ItemDto tujo_point_kagenti_tonen = new ItemDto();           /* 通常Ｐ加減値（当年）       */
        public ItemDto kikan_point_kagenti_month0 = new ItemDto();         /* 期間限定Ｐ加減値（当月）   */
        public ItemDto kikan_point_kagenti_month1 = new ItemDto();         /* 期間限定Ｐ加減値（1ヶ月後) */
        public ItemDto kikan_point_kagenti_month2 = new ItemDto();         /* 期間限定Ｐ加減値（2ヶ月後）*/
        public ItemDto kikan_point_kagenti_month3 = new ItemDto();         /* 期間限定Ｐ加減値（3ヶ月後）*/
        public ItemDto riyu_code = new ItemDto();                            /* 理由コード       */
        public ItemDto shori_kekka = new ItemDto();                          /* 処理結果         */
    }

    /*---------TS利用可能ポイント情報用---------------*/
    public class TS_TBL {
        public ItemDto yokunen_p = new ItemDto();         /* 利用可能通常Ｐ翌年度           */
        public ItemDto tonen_p = new ItemDto();           /* 利用可能通常Ｐ当年度           */
        public ItemDto zennen_p = new ItemDto();          /* 利用可能通常Ｐ前年度           */
        public ItemDto nyukai_kigyo_cd = new ItemDto();   /* 入会企業コード                 */
        public ItemDto nyukai_tenpo = new ItemDto();      /* 入会店舗                       */
        public ItemDto hakken_kigyo_cd = new ItemDto();   /* 発券企業コード                 */
        public ItemDto hakken_tenpo = new ItemDto();      /* 発券店舗                       */
        public ItemDto kikan_p01 = new ItemDto();         /* 利用可能期間限定Ｐ当月         */
        public ItemDto kikan_p02 = new ItemDto();         /* 利用可能期間限定Ｐ当月＋１     */
        public ItemDto kikan_p03 = new ItemDto();         /* 利用可能期間限定Ｐ当月＋２     */
        public ItemDto kikan_p04 = new ItemDto();         /* 利用可能期間限定Ｐ当月＋３     */
        public ItemDto kikan_p05 = new ItemDto();         /* 利用可能期間限定Ｐ当月＋４     */
        public ItemDto saishu_koshin_programid = new ItemDto(16);
    };

    public ItemDto h_str_sql = new ItemDto(4096);         /* 実行用SQL文字列                    */
    /* 2022/10/03 MCCM初版 ADD START */
    public ItemDto h_card_shubetsu = new ItemDto();          /* PS会員番号体系テーブルのカード種別 */

}
