package jp.co.mcc.nttdata.batch.business.com.cmBTfuncB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CmBTfuncBImpl extends CmABfuncLServiceImpl implements CmBTfuncB {
    boolean DBG_LOG = true;

    @Override
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
        /*#####################################*/
        /*  ローカル変数定義                   */
        /*#####################################*/
        String[] outFlg = new String[2];             /** APログフラグ              **/
        String[] outFormat = new String[2];          /** APログフォーマット        **/
        IntegerDto outStatus = new IntegerDto();             /** フォーマット取得結果      **/
        int rtnCd;                 /** 関数戻り値                **/
        boolean DBG_LOG = true;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("APLOG_WT処理");
            /*---------------------------------------------------------------------*/
        }

        /*#####################################*/
        /*  APログフォーマット取得処理         */
        /*#####################################*/
        rtnCd = C_GetAPLogFmt(msgid, msgidsbt, dbkbn, outFlg, outFormat, outStatus);
        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgMsg("*** APLOG_WT *** APログフォーマット取得結果 = %d\n", rtnCd);
            /*---------------------------------------------------------------------*/
        }

        /*#####################################*/
        /*  APログ出力処理                     */
        /*#####################################*/
        rtnCd = C_APLogWrite(msgid, outFormat, outFlg, param1, param2, param3, param4, param5, param6);

        if (DBG_LOG) {
            C_DbgMsg("*** APLOG_WT *** APログ出力結果 = %d\n", rtnCd);
            C_DbgEnd("APLOG_WT処理", 0, 0, 0);
        }
        return C_const_OK;
    }
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_InsertDayPoint                                            */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_InsertDayPoint(DAY_POINT_DATA  *dayPointData, int *date,        */
    /*                           int *status)                                     */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ポイント日別情報追加処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              DAY_POINT_DATA * dayPointData ： ポイント日別情報構造体取得   */
    /*                                               パラメータ                   */
    /*              int           date   ： 更新対象テーブル日付(YYYYMMDD)        */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_InsertDayPoint(TSHS_DAY_POINT_TBL tshs_day_point, int date, IntegerDto status) {
        DBG_LOG = true;

        String tbl_nam = null;
        StringDto sql_buf = new StringDto();     /* ＳＱＬ文編集用 */
        StringDto sql_buf2 = new StringDto();    /* ＳＱＬ文編集用 */
        StringDto sql_buf3 = new StringDto();    /* ＳＱＬ文編集用 */
        String buff = null;
        String w_date = null;
        String out_format_buf = null; /* APログフォーマット        */
        String penv = null;


        StringDto WRKSQL2 = new StringDto();

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;
//
        TSHS_DAY_POINT_TBL h_tshs_day_point_buff = new TSHS_DAY_POINT_TBL(); /* TSHSポイント日別情報 */
//
//
//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_InsertDayPoint : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (tshs_day_point == null || date == 0 || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* ホスト変数を編集する */
//        memset(&h_tshs_day_point_buff, 0x00, sizeof(TSHS_DAY_POINT_TBL));
        h_tshs_day_point_buff.kokyaku_no.arr = memcpy(h_tshs_day_point_buff.kokyaku_no.arr(), tshs_day_point.kokyaku_no.arr(), tshs_day_point.kokyaku_no.len);
        h_tshs_day_point_buff.kokyaku_no.len = tshs_day_point.kokyaku_no.len;

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)", 0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        if (strcmp(Cg_ORASID, penv) == 0) {
        	C_DbgEnd("penv==Cg_ORASID", 0, 0, 0);
        	WRKSQL2.arr = "SELECT NEXTVAL('SQポイント日別処理通番')  FROM DUAL";

//            EXEC SQL SELECT SQポイント日別処理通番.NEXTVAL
//            INTO:
//            h_tshs_day_point_buff.shori_seq /* 処理通番 */
//            FROM DUAL;
        } else {
        	C_DbgEnd("penv!=Cg_ORASID", 0, 0, 0);
            WRKSQL2.arr = "SELECT SQポイント日別処理通番 FROM cmsho_sqポイント日別処理通番";
//            EXEC SQL SELECT SQポイント日別処理通番.NEXTVAL @CMSD
//           h_tshs_day_point_buff.shori_seq /* 処理通番 */
//            FROM DUAL @CMSD ;
        }
        SqlstmDto sqlca = sqlcaManager.get("sql_kdatalock2");
        sqlca.sql = WRKSQL2;
        sqlca.restAndExecute();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : SELECT SEQ : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            APLOG_WT("904", 0, null, "SEQ SELECT", sqlca.sqlcode, "SQポイント日別", "", 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        sqlca.fetch();

        ItemDto idto = new ItemDto();
        sqlca.recData(idto);

        h_tshs_day_point_buff.shori_seq.arr = idto.floatVal();

        /* ホスト変数設定 */
        h_tshs_day_point_buff.system_ymd = tshs_day_point.system_ymd;                             /* システム年月日               */
        memcpy(h_tshs_day_point_buff.kokyaku_no, tshs_day_point.kokyaku_no.arr(), tshs_day_point.kokyaku_no.len);          /* 顧客番号                     */
        h_tshs_day_point_buff.kokyaku_no.len = tshs_day_point.kokyaku_no.len;
        /*h_tshs_day_point_buff.shori_seq                              = EXEC SQL SELECT SQポイント日別処理通番.NEXTVAL             処理通番                     */
        h_tshs_day_point_buff.kaiin_kigyo_cd = tshs_day_point.kaiin_kigyo_cd;                         /* 会員企業コード               */
        h_tshs_day_point_buff.kaiin_kyu_hansya_cd = tshs_day_point.kaiin_kyu_hansya_cd;                    /* 会員旧販社コード             */
        memcpy(h_tshs_day_point_buff.kaiin_no, tshs_day_point.kaiin_no.arr(), tshs_day_point.kaiin_no.len);                /* 会員番号                     */
        h_tshs_day_point_buff.kaiin_no.len = tshs_day_point.kaiin_no.len;
        h_tshs_day_point_buff.nyukai_kigyo_cd = tshs_day_point.nyukai_kigyo_cd;                        /* 入会企業コード               */
        h_tshs_day_point_buff.nyukai_tenpo = tshs_day_point.nyukai_tenpo;                           /* 入会店舗                     */
        h_tshs_day_point_buff.hakken_kigyo_cd = tshs_day_point.hakken_kigyo_cd;                        /* 発券企業コード               */
        h_tshs_day_point_buff.hakken_tenpo = tshs_day_point.hakken_tenpo;                           /* 発券店舗                     */
        h_tshs_day_point_buff.seisan_ymd = tshs_day_point.seisan_ymd;                             /* 精算年月日                   */
        h_tshs_day_point_buff.toroku_ymd = tshs_day_point.toroku_ymd;                             /* 登録年月日                   */
        h_tshs_day_point_buff.data_ymd = tshs_day_point.data_ymd;                               /* データ年月日                 */
        h_tshs_day_point_buff.kigyo_cd = tshs_day_point.kigyo_cd;                               /* 企業コード                   */
        h_tshs_day_point_buff.mise_no = tshs_day_point.mise_no;                                /* 店番号                       */
        h_tshs_day_point_buff.terminal_no = tshs_day_point.terminal_no;                            /* ターミナル番号               */
        h_tshs_day_point_buff.torihiki_no = tshs_day_point.torihiki_no;                            /* 取引番号                     */
        h_tshs_day_point_buff.jikoku_hms = tshs_day_point.jikoku_hms;                             /* 時刻                         */
        h_tshs_day_point_buff.riyu_cd = tshs_day_point.riyu_cd;                                /* 理由コード                   */
        h_tshs_day_point_buff.circle_id = tshs_day_point.circle_id;                              /* サークルＩＤ                 */
        h_tshs_day_point_buff.card_nyuryoku_kbn = tshs_day_point.card_nyuryoku_kbn;                      /* カード入力区分               */
        h_tshs_day_point_buff.shori_taisho_file_record_no = tshs_day_point.shori_taisho_file_record_no;            /* 処理対象ファイルレコード番号 */
        h_tshs_day_point_buff.real_koshin_flg = tshs_day_point.real_koshin_flg;                        /* リアル更新フラグ             */
        h_tshs_day_point_buff.fuyo_point = tshs_day_point.fuyo_point;                             /* 付与ポイント                 */
        h_tshs_day_point_buff.riyo_point = tshs_day_point.riyo_point;                             /* 利用ポイント                 */
        h_tshs_day_point_buff.kihon_pritsu_taisho_point = tshs_day_point.kihon_pritsu_taisho_point;              /* 基本Ｐ率対象ポイント         */
        h_tshs_day_point_buff.rankup_taisho_kingaku = tshs_day_point.rankup_taisho_kingaku;                  /* ランクＵＰ対象金額           */
        h_tshs_day_point_buff.point_taisho_kingaku = tshs_day_point.point_taisho_kingaku;                   /* ポイント対象金額             */
        h_tshs_day_point_buff.service_hakko_maisu = tshs_day_point.service_hakko_maisu;                   /* サービス券発行枚数           */
        h_tshs_day_point_buff.service_riyo_maisu = tshs_day_point.service_riyo_maisu;                    /* サービス券利用枚数           */
        h_tshs_day_point_buff.kojin_getuji_rank_cd = tshs_day_point.kojin_getuji_rank_cd;                   /* 個人月次ランクコード         */
        h_tshs_day_point_buff.kojin_nenji_rank_cd = tshs_day_point.kojin_nenji_rank_cd;                    /* 個人年次ランクコード         */
        h_tshs_day_point_buff.kazoku_getuji_rank_cd = tshs_day_point.kazoku_getuji_rank_cd;                  /* 家族月次ランクコード         */
        h_tshs_day_point_buff.kazoku_nenji_rank_cd = tshs_day_point.kazoku_nenji_rank_cd;                   /* 家族年次ランクコード         */
        h_tshs_day_point_buff.shiyo_rank_cd = tshs_day_point.shiyo_rank_cd;                          /* 使用ランクコード             */
        h_tshs_day_point_buff.kaiage_kingaku = tshs_day_point.kaiage_kingaku;                         /* 買上額                       */
        h_tshs_day_point_buff.kaiage_cnt = tshs_day_point.kaiage_cnt;                             /* 買上回数                     */
        h_tshs_day_point_buff.koshinmae_riyo_kano_point = tshs_day_point.koshinmae_riyo_kano_point;              /* 更新前利用可能ポイント       */
        h_tshs_day_point_buff.koshinmae_fuyo_point = tshs_day_point.koshinmae_fuyo_point;                   /* 更新前付与ポイント           */
        h_tshs_day_point_buff.koshinmae_kihon_pritsu_taisho_point = tshs_day_point.koshinmae_kihon_pritsu_taisho_point;    /* 更新前基本Ｐ率対象ポイント   */
        h_tshs_day_point_buff.koshinmae_gekkan_kojin_rankup_taisho_kingaku = tshs_day_point.koshinmae_gekkan_kojin_rankup_taisho_kingaku; /* 更新前月間ランクＵＰ対象金額 */
        h_tshs_day_point_buff.koshinmae_nenkan_kojin_rankup_taisho_kingaku = tshs_day_point.koshinmae_nenkan_kojin_rankup_taisho_kingaku; /* 更新前年間ランクＵＰ対象金額 */
        h_tshs_day_point_buff.koshinmae_point_taisho_kingaku = tshs_day_point.koshinmae_point_taisho_kingaku;         /* 更新前ポイント対象金額       */
        h_tshs_day_point_buff.koshinmae_kaiage_kingaku = tshs_day_point.koshinmae_kaiage_kingaku;               /* 更新前買上額                 */
        /*h_tshs_day_point_buff.kazoku_id                              = tshs_day_point.kazoku_id;                                 家族ＩＤ                     */
        memcpy(h_tshs_day_point_buff.kazoku_id, tshs_day_point.kazoku_id.arr(), tshs_day_point.kazoku_id.len);             /* 家族ＩＤ                     */
        h_tshs_day_point_buff.kazoku_id.len = tshs_day_point.kazoku_id.len;
        h_tshs_day_point_buff.koshinmae_gekkan_kazoku_rankup_taisho_kingaku = tshs_day_point.koshinmae_gekkan_kazoku_rankup_taisho_kingaku; /* 更新前月間家族ランクＵＰ金額 */
        h_tshs_day_point_buff.koshinmae_nenkan_kazoku_rankup_taisho_kingaku = tshs_day_point.koshinmae_nenkan_kazoku_rankup_taisho_kingaku; /* 更新前年間家族ランクＵＰ金額 */
        h_tshs_day_point_buff.real_koshin_ymd = tshs_day_point.real_koshin_ymd;                        /* リアル更新日時               */
        strcpy(h_tshs_day_point_buff.real_koshin_apl_version, tshs_day_point.real_koshin_apl_version);               /* リアル更新ＡＰＬバージョン   */
        h_tshs_day_point_buff.delay_koshin_ymd = tshs_day_point.delay_koshin_ymd;                       /* ディレイ更新日時             */
        strcpy(h_tshs_day_point_buff.delay_koshin_apl_version, tshs_day_point.delay_koshin_apl_version);              /* ディレイ更新ＡＰＬバージョン */
        h_tshs_day_point_buff.sosai_flg = tshs_day_point.sosai_flg;                              /* 相殺フラグ                   */
        h_tshs_day_point_buff.mesai_check_flg = tshs_day_point.mesai_check_flg;                        /* 明細チェックフラグ           */
        h_tshs_day_point_buff.mesai_check_kbn = tshs_day_point.mesai_check_kbn;                        /* 明細チェック区分             */
        h_tshs_day_point_buff.sagyo_kigyo_cd = tshs_day_point.sagyo_kigyo_cd;                         /* 作業企業コード               */
        h_tshs_day_point_buff.sagyosha_id = tshs_day_point.sagyosha_id;                            /* 作業者ＩＤ                   */
        h_tshs_day_point_buff.sagyo_ymd = tshs_day_point.sagyo_ymd;                              /* 作業年月日                   */
        h_tshs_day_point_buff.sagyo_hms = tshs_day_point.sagyo_hms;                              /* 作業時刻                     */
        h_tshs_day_point_buff.batch_koshin_ymd = tshs_day_point.batch_koshin_ymd;                       /* バッチ更新日                 */
        h_tshs_day_point_buff.saishu_koshin_ymd = tshs_day_point.saishu_koshin_ymd;                      /* 最終更新日                   */
        h_tshs_day_point_buff.saishu_koshin_ymdhms = tshs_day_point.saishu_koshin_ymdhms;                   /* 最終更新日時                 */
        strcpy(h_tshs_day_point_buff.saishu_koshin_programid, tshs_day_point.saishu_koshin_programid);               /* 最終更新プログラムＩＤ       */
        h_tshs_day_point_buff.yokyu_riyo_putiwake_flg = tshs_day_point.yokyu_riyo_putiwake_flg;                /* 要求利用Ｐ内訳フラグ         */
        h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_kijun_nendo = tshs_day_point.koshinmae_riyo_kano_tujo_point_kijun_nendo; /* 更新前利用可能通常Ｐ基準年度 */
        h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_zennendo = tshs_day_point.koshinmae_riyo_kano_tujo_point_zennendo;/* 更新前利用可能通常Ｐ前年度  */
        h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_tonendo = tshs_day_point.koshinmae_riyo_kano_tujo_point_tonendo; /* 更新前利用可能通常Ｐ当年度   */
        h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_yokunendo = tshs_day_point.koshinmae_riyo_kano_tujo_point_yokunendo; /* 更新前利用可能通常Ｐ翌年度 */
        h_tshs_day_point_buff.yokyu_fuyo_tujo_point = tshs_day_point.yokyu_fuyo_tujo_point;                   /* 要求付与通常Ｐ              */
        h_tshs_day_point_buff.yokyu_fuyo_tujo_point_kijun_nendo = tshs_day_point.yokyu_fuyo_tujo_point_kijun_nendo;       /* 要求付与通常Ｐ基準年度      */
        h_tshs_day_point_buff.yokyu_fuyo_tujo_poin_zennendo = tshs_day_point.yokyu_fuyo_tujo_poin_zennendo;           /* 要求付与通常Ｐ前年度        */
        h_tshs_day_point_buff.yokyu_fuyo_tujo_poin_tonendo = tshs_day_point.yokyu_fuyo_tujo_poin_tonendo;            /* 要求付与通常Ｐ当年度        */
        h_tshs_day_point_buff.yokyu_riyo_tujo_point = tshs_day_point.yokyu_riyo_tujo_point;                   /* 要求利用通常Ｐ              */
        h_tshs_day_point_buff.yokyu_riyo_tujo_point_kijun_nendo = tshs_day_point.yokyu_riyo_tujo_point_kijun_nendo;       /* 要求利用通常Ｐ基準年度      */
        h_tshs_day_point_buff.yokyu_riyo_tujo_point_zennendo = tshs_day_point.yokyu_riyo_tujo_point_zennendo;          /* 要求利用通常Ｐ前年度        */
        h_tshs_day_point_buff.yokyu_riyo_tujo_point_tonendo = tshs_day_point.yokyu_riyo_tujo_point_tonendo;           /* 要求利用通常Ｐ当年度        */
        h_tshs_day_point_buff.yokyu_riyo_tujo_point_yokunendo = tshs_day_point.yokyu_riyo_tujo_point_yokunendo;         /* 要求利用通常Ｐ翌年度        */
        h_tshs_day_point_buff.koshin_fuyo_tujo_point = tshs_day_point.koshin_fuyo_tujo_point;                  /* 更新付与通常Ｐ              */
        h_tshs_day_point_buff.koshin_fuyo_tujo_point_kijun_nendo = tshs_day_point.koshin_fuyo_tujo_point_kijun_nendo;      /* 更新付与通常Ｐ基準年度      */
        h_tshs_day_point_buff.koshin_fuyo_tujo_point_zennendo = tshs_day_point.koshin_fuyo_tujo_point_zennendo;         /* 更新付与通常Ｐ前年度        */
        h_tshs_day_point_buff.koshin_fuyo_tujo_point_tonendo = tshs_day_point.koshin_fuyo_tujo_point_tonendo;          /* 更新付与通常Ｐ当年度        */
        h_tshs_day_point_buff.koshin_riyo_tujo_point = tshs_day_point.koshin_riyo_tujo_point;                  /* 更新利用通常Ｐ              */
        h_tshs_day_point_buff.koshin_riyo_tujo_point_kijun_nendo = tshs_day_point.koshin_riyo_tujo_point_kijun_nendo;      /* 更新利用通常Ｐ基準年度      */
        h_tshs_day_point_buff.koshin_riyo_tujo_point_zennendo = tshs_day_point.koshin_riyo_tujo_point_zennendo;         /* 更新利用通常Ｐ前年度        */
        h_tshs_day_point_buff.koshin_riyo_tujo_point_tonendo = tshs_day_point.koshin_riyo_tujo_point_tonendo;          /* 更新利用通常Ｐ当年度        */
        h_tshs_day_point_buff.koshin_riyo_tujo_point_yokunendo = tshs_day_point.koshin_riyo_tujo_point_yokunendo;        /* 更新利用通常Ｐ翌年度        */
        h_tshs_day_point_buff.koshinmae_kikan_gentei_point_kijun_month = tshs_day_point.koshinmae_kikan_gentei_point_kijun_month; /* 更新前期間限定Ｐ基準月      */
        h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point0 = tshs_day_point.koshinmae_riyo_kano_kikan_gentei_point0;  /*  更新前利用可能期間限定Ｐ０ */
        h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point1 = tshs_day_point.koshinmae_riyo_kano_kikan_gentei_point1;  /*  更新前利用可能期間限定Ｐ１ */
        h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point2 = tshs_day_point.koshinmae_riyo_kano_kikan_gentei_point2;  /*  更新前利用可能期間限定Ｐ２ */
        h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point3 = tshs_day_point.koshinmae_riyo_kano_kikan_gentei_point3;  /*  更新前利用可能期間限定Ｐ３ */
        h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point4 = tshs_day_point.koshinmae_riyo_kano_kikan_gentei_point4;  /*  更新前利用可能期間限定Ｐ４ */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point = tshs_day_point.yokyu_fuyo_kikan_gentei_point;            /*  要求付与期間限定Ｐ         */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point_kijun_month = tshs_day_point.yokyu_fuyo_kikan_gentei_point_kijun_month; /* 要求付与期間限定Ｐ基準月  */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point0 = tshs_day_point.yokyu_fuyo_kikan_gentei_point0;         /* 要求付与期間限定Ｐ０          */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point1 = tshs_day_point.yokyu_fuyo_kikan_gentei_point1;         /* 要求付与期間限定Ｐ１          */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point2 = tshs_day_point.yokyu_fuyo_kikan_gentei_point2;         /* 要求付与期間限定Ｐ２          */
        h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point3 = tshs_day_point.yokyu_fuyo_kikan_gentei_point3;         /* 要求付与期間限定Ｐ３          */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point = tshs_day_point.yokyu_riyo_kikan_gentei_point;          /* 要求利用期間限定Ｐ            */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point_kijun_month = tshs_day_point.yokyu_riyo_kikan_gentei_point_kijun_month; /* 要求利用期間限定Ｐ基準月  */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point0 = tshs_day_point.yokyu_riyo_kikan_gentei_point0;         /* 要求利用期間限定Ｐ０          */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point1 = tshs_day_point.yokyu_riyo_kikan_gentei_point1;         /* 要求利用期間限定Ｐ１          */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point2 = tshs_day_point.yokyu_riyo_kikan_gentei_point2;         /* 要求利用期間限定Ｐ２          */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point3 = tshs_day_point.yokyu_riyo_kikan_gentei_point3;         /* 要求利用期間限定Ｐ３          */
        h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point4 = tshs_day_point.yokyu_riyo_kikan_gentei_point4;         /* 要求利用期間限定Ｐ４          */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point = tshs_day_point.koshin_fuyo_kikan_gentei_point;         /* 更新付与期間限定Ｐ            */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point_kijun_month = tshs_day_point.koshin_fuyo_kikan_gentei_point_kijun_month; /* 更新付与期間限定Ｐ基準月*/
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point01 = tshs_day_point.koshin_fuyo_kikan_gentei_point01;       /* 更新付与期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point02 = tshs_day_point.koshin_fuyo_kikan_gentei_point02;       /* 更新付与期間限定Ｐ０２        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point03 = tshs_day_point.koshin_fuyo_kikan_gentei_point03;       /* 更新付与期間限定Ｐ０３        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point04 = tshs_day_point.koshin_fuyo_kikan_gentei_point04;       /* 更新付与期間限定Ｐ０４        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point05 = tshs_day_point.koshin_fuyo_kikan_gentei_point05;       /* 更新付与期間限定Ｐ０５        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point06 = tshs_day_point.koshin_fuyo_kikan_gentei_point06;       /* 更新付与期間限定Ｐ０６        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point07 = tshs_day_point.koshin_fuyo_kikan_gentei_point07;       /* 更新付与期間限定Ｐ０７        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point08 = tshs_day_point.koshin_fuyo_kikan_gentei_point08;       /* 更新付与期間限定Ｐ０８        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point09 = tshs_day_point.koshin_fuyo_kikan_gentei_point09;       /* 更新付与期間限定Ｐ０９        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point10 = tshs_day_point.koshin_fuyo_kikan_gentei_point10;       /* 更新付与期間限定Ｐ１０        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point11 = tshs_day_point.koshin_fuyo_kikan_gentei_point11;       /* 更新付与期間限定Ｐ１１        */
        h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point12 = tshs_day_point.koshin_fuyo_kikan_gentei_point12;       /* 更新付与期間限定Ｐ１２        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point = tshs_day_point.koshin_riyo_kikan_gentei_point;         /* 更新利用期間限定Ｐ            */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point_kijun_month = tshs_day_point.koshin_riyo_kikan_gentei_point_kijun_month; /* 更新利用期間限定Ｐ基準月*/
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point01 = tshs_day_point.koshin_riyo_kikan_gentei_point01;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point02 = tshs_day_point.koshin_riyo_kikan_gentei_point02;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point03 = tshs_day_point.koshin_riyo_kikan_gentei_point03;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point04 = tshs_day_point.koshin_riyo_kikan_gentei_point04;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point05 = tshs_day_point.koshin_riyo_kikan_gentei_point05;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point06 = tshs_day_point.koshin_riyo_kikan_gentei_point06;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point07 = tshs_day_point.koshin_riyo_kikan_gentei_point07;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point08 = tshs_day_point.koshin_riyo_kikan_gentei_point08;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point09 = tshs_day_point.koshin_riyo_kikan_gentei_point09;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point10 = tshs_day_point.koshin_riyo_kikan_gentei_point10;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point11 = tshs_day_point.koshin_riyo_kikan_gentei_point11;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point12 = tshs_day_point.koshin_riyo_kikan_gentei_point12;      /* 更新利用期間限定Ｐ０１        */
        h_tshs_day_point_buff.mk_mise_no = tshs_day_point.mk_mise_no;                             /* 店番号                        */
        h_tshs_day_point_buff.mk_torihiki_no = tshs_day_point.mk_torihiki_no;                         /* 取引番号                      */
        h_tshs_day_point_buff.nyukai_kaisha_cd_mcc = tshs_day_point.nyukai_kaisha_cd_mcc;                   /* 入会会社コードＭＣＣ          */
        h_tshs_day_point_buff.nyukai_tenpo_mcc = tshs_day_point.nyukai_tenpo_mcc;                       /* 入会店舗ＭＣＣ                */
        h_tshs_day_point_buff.kaisha_cd_mcc = tshs_day_point.kaisha_cd_mcc;                          /* 会社コードＭＣＣ              */
        h_tshs_day_point_buff.mise_no_mcc = tshs_day_point.mise_no_mcc;                            /* 店番号ＭＣＣ                  */
        h_tshs_day_point_buff.card_syubetsu = tshs_day_point.card_syubetsu;                          /* カード種別                    */
        strcpy(h_tshs_day_point_buff.touroku_keiro, tshs_day_point.touroku_keiro);                                              /* 登録経路                      */
        strcpy(h_tshs_day_point_buff.torihiki_kbn, tshs_day_point.torihiki_kbn);                                                /* 取引区分                      */
        h_tshs_day_point_buff.kangen_shubetsu = tshs_day_point.kangen_shubetsu;                        /* 還元種別                      */
        h_tshs_day_point_buff.meisai_su = tshs_day_point.meisai_su;                              /* 明細数                        */
        h_tshs_day_point_buff.raiten_point_fuyo_taisyo_kbn = tshs_day_point.raiten_point_fuyo_taisyo_kbn;           /* 来店ポイント付与対象区分      */


        strcpy(sql_buf2, " (システム年月日,");
        strcat(sql_buf2, "顧客番号,");
        strcat(sql_buf2, "処理通番,");
        strcat(sql_buf2, "会員企業コード,");
        strcat(sql_buf2, "会員旧販社コード,");
        strcat(sql_buf2, "会員番号,");
        strcat(sql_buf2, "入会企業コード,");
        strcat(sql_buf2, "入会店舗,");
        strcat(sql_buf2, "発券企業コード,");
        strcat(sql_buf2, "発券店舗,");
        strcat(sql_buf2, "精算年月日,");
        strcat(sql_buf2, "登録年月日,");
        strcat(sql_buf2, "データ年月日,");
        strcat(sql_buf2, "企業コード,");
        strcat(sql_buf2, "店番号,");
        strcat(sql_buf2, "ターミナル番号,");
        strcat(sql_buf2, "取引番号,");
        strcat(sql_buf2, "時刻,");
        strcat(sql_buf2, "理由コード,");
        strcat(sql_buf2, "サークルＩＤ,");
        strcat(sql_buf2, "カード入力区分,");
        strcat(sql_buf2, "処理対象ファイルレコード番号,");
        strcat(sql_buf2, "リアル更新フラグ,");
        strcat(sql_buf2, "付与ポイント,");
        strcat(sql_buf2, "利用ポイント,");
        strcat(sql_buf2, "基本Ｐ率対象ポイント,");
        strcat(sql_buf2, "ランクＵＰ対象金額,");
        strcat(sql_buf2, "ポイント対象金額,");
        strcat(sql_buf2, "サービス券発行枚数,");
        strcat(sql_buf2, "サービス券利用枚数,");
        strcat(sql_buf2, "個人月次ランクコード,");
        strcat(sql_buf2, "個人年次ランクコード,");
        strcat(sql_buf2, "家族月次ランクコード,");
        strcat(sql_buf2, "家族年次ランクコード,");
        strcat(sql_buf2, "使用ランクコード,");
        strcat(sql_buf2, "買上額,");
        strcat(sql_buf2, "買上回数,");
        strcat(sql_buf2, "更新前利用可能ポイント,");
        strcat(sql_buf2, "更新前付与ポイント,");
        strcat(sql_buf2, "更新前基本Ｐ率対象ポイント,");
        strcat(sql_buf2, "更新前月間ランクＵＰ対象金額,");
        strcat(sql_buf2, "更新前年間ランクＵＰ対象金額,");
        strcat(sql_buf2, "更新前ポイント対象金額,");
        strcat(sql_buf2, "更新前買上額,");
        strcat(sql_buf2, "家族ＩＤ,");
        strcat(sql_buf2, "更新前月間家族ランクＵＰ金額,");
        strcat(sql_buf2, "更新前年間家族ランクＵＰ金額,");
        strcat(sql_buf2, "リアル更新日時,");
        strcat(sql_buf2, "リアル更新ＡＰＬバージョン,");
        strcat(sql_buf2, "ディレイ更新日時,");
        strcat(sql_buf2, "ディレイ更新ＡＰＬバージョン,");
        strcat(sql_buf2, "相殺フラグ,");
        strcat(sql_buf2, "明細チェックフラグ,");
        strcat(sql_buf2, "明細チェック区分,");
        strcat(sql_buf2, "作業企業コード,");
        strcat(sql_buf2, "作業者ＩＤ,");
        strcat(sql_buf2, "作業年月日,");
        strcat(sql_buf2, "作業時刻,");
        strcat(sql_buf2, "バッチ更新日,");
        strcat(sql_buf2, "最終更新日,");
        strcat(sql_buf2, "最終更新日時,");
        strcat(sql_buf2, "最終更新プログラムＩＤ,");
        strcat(sql_buf2, "要求利用Ｐ内訳フラグ,");
        strcat(sql_buf2, "更新前利用可能通常Ｐ基準年度,");
        strcat(sql_buf2, "更新前利用可能通常Ｐ前年度,");
        strcat(sql_buf2, "更新前利用可能通常Ｐ当年度,");
        strcat(sql_buf2, "更新前利用可能通常Ｐ翌年度,");
        strcat(sql_buf2, "要求付与通常Ｐ,");
        strcat(sql_buf2, "要求付与通常Ｐ基準年度,");
        strcat(sql_buf2, "要求付与通常Ｐ前年度,");
        strcat(sql_buf2, "要求付与通常Ｐ当年度,");
        strcat(sql_buf2, "要求利用通常Ｐ,");
        strcat(sql_buf2, "要求利用通常Ｐ基準年度,");
        strcat(sql_buf2, "要求利用通常Ｐ前年度,");
        strcat(sql_buf2, "要求利用通常Ｐ当年度,");
        strcat(sql_buf2, "要求利用通常Ｐ翌年度,");
        strcat(sql_buf2, "更新付与通常Ｐ,");
        strcat(sql_buf2, "更新付与通常Ｐ基準年度,");
        strcat(sql_buf2, "更新付与通常Ｐ前年度,");
        strcat(sql_buf2, "更新付与通常Ｐ当年度,");
        strcat(sql_buf2, "更新利用通常Ｐ,");
        strcat(sql_buf2, "更新利用通常Ｐ基準年度,");
        strcat(sql_buf2, "更新利用通常Ｐ前年度,");
        strcat(sql_buf2, "更新利用通常Ｐ当年度,");
        strcat(sql_buf2, "更新利用通常Ｐ翌年度,");
        strcat(sql_buf2, "更新前期間限定Ｐ基準月,");
        strcat(sql_buf2, "更新前利用可能期間限定Ｐ０,");
        strcat(sql_buf2, "更新前利用可能期間限定Ｐ１,");
        strcat(sql_buf2, "更新前利用可能期間限定Ｐ２,");
        strcat(sql_buf2, "更新前利用可能期間限定Ｐ３,");
        strcat(sql_buf2, "更新前利用可能期間限定Ｐ４,");
        strcat(sql_buf2, "要求付与期間限定Ｐ,");
        strcat(sql_buf2, "要求付与期間限定Ｐ基準月,");
        strcat(sql_buf2, "要求付与期間限定Ｐ０,");
        strcat(sql_buf2, "要求付与期間限定Ｐ１,");
        strcat(sql_buf2, "要求付与期間限定Ｐ２,");
        strcat(sql_buf2, "要求付与期間限定Ｐ３,");
        strcat(sql_buf2, "要求利用期間限定Ｐ,");
        strcat(sql_buf2, "要求利用期間限定Ｐ基準月,");
        strcat(sql_buf2, "要求利用期間限定Ｐ０,");
        strcat(sql_buf2, "要求利用期間限定Ｐ１,");
        strcat(sql_buf2, "要求利用期間限定Ｐ２,");
        strcat(sql_buf2, "要求利用期間限定Ｐ３,");
        strcat(sql_buf2, "要求利用期間限定Ｐ４,");
        strcat(sql_buf2, "更新付与期間限定Ｐ,");
        strcat(sql_buf2, "更新付与期間限定Ｐ基準月,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０１,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０２,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０３,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０４,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０５,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０６,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０７,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０８,");
        strcat(sql_buf2, "更新付与期間限定Ｐ０９,");
        strcat(sql_buf2, "更新付与期間限定Ｐ１０,");
        strcat(sql_buf2, "更新付与期間限定Ｐ１１,");
        strcat(sql_buf2, "更新付与期間限定Ｐ１２,");
        strcat(sql_buf2, "更新利用期間限定Ｐ,");
        strcat(sql_buf2, "更新利用期間限定Ｐ基準月,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０１,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０２,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０３,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０４,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０５,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０６,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０７,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０８,");
        strcat(sql_buf2, "更新利用期間限定Ｐ０９,");
        strcat(sql_buf2, "更新利用期間限定Ｐ１０,");
        strcat(sql_buf2, "更新利用期間限定Ｐ１１,");
        strcat(sql_buf2, "更新利用期間限定Ｐ１２,");
        strcat(sql_buf2, "ＭＫ店番号,");
        strcat(sql_buf2, "ＭＫ取引番号,");
        strcat(sql_buf2, "入会会社コードＭＣＣ,");
        strcat(sql_buf2, "入会店舗ＭＣＣ,");
        strcat(sql_buf2, "会社コードＭＣＣ,");
        strcat(sql_buf2, "店番号ＭＣＣ,");
        strcat(sql_buf2, "カード種別,");
        strcat(sql_buf2, "登録経路,");
        strcat(sql_buf2, "取引区分,");
        strcat(sql_buf2, "還元種別,");
        strcat(sql_buf2, "明細数,");
        strcat(sql_buf2, "来店ポイント付与対象区分) VALUES (");

        strcpy(sql_buf3, "?,");      /* システム年月日               */
        strcat(sql_buf3, "?,");      /* 顧客番号                     */
        strcat(sql_buf3, "?,");      /* 処理通番                     */
        strcat(sql_buf3, "?,");      /* 会員企業コード               */
        strcat(sql_buf3, "?,");      /* 会員旧販社コード             */
        strcat(sql_buf3, "?,");      /* 会員番号                     */
        strcat(sql_buf3, "?,");      /* 入会企業コード               */
        strcat(sql_buf3, "?,");      /* 入会店舗                     */
        strcat(sql_buf3, "?,");      /* 発券企業コード               */
        strcat(sql_buf3, "?,");     /* 発券店舗                     */
        strcat(sql_buf3, "?,");     /* 精算年月日                   */
        strcat(sql_buf3, "?,");     /* 登録年月日                   */
        strcat(sql_buf3, "?,");     /* データ年月日                 */
        strcat(sql_buf3, "?,");     /* 企業コード                   */
        strcat(sql_buf3, "?,");     /* 店番号                       */
        strcat(sql_buf3, "?,");     /* ターミナル番号               */
        strcat(sql_buf3, "?,");     /* 取引番号                     */
        strcat(sql_buf3, "?,");     /* 時刻                         */
        strcat(sql_buf3, "?,");     /* 理由コード                   */
        strcat(sql_buf3, "?,");     /* サークルＩＤ                 */
        strcat(sql_buf3, "?,");     /* カード入力区分               */
        strcat(sql_buf3, "?,");     /* 処理対象ファイルレコード番号 */
        strcat(sql_buf3, "?,");     /* リアル更新フラグ             */
        strcat(sql_buf3, "?,");     /* 付与ポイント                 */
        strcat(sql_buf3, "?,");     /* 利用ポイント                 */
        strcat(sql_buf3, "?,");     /* 基本Ｐ率対象ポイント         */
        strcat(sql_buf3, "?,");     /* ランクＵＰ対象金額           */
        strcat(sql_buf3, "?,");     /* ポイント対象金額             */
        strcat(sql_buf3, "?,");     /* サービス券発行枚数           */
        strcat(sql_buf3, "?,");     /* サービス券利用枚数           */
        strcat(sql_buf3, "?,");     /* 個人月次ランクコード         */
        strcat(sql_buf3, "?,");     /* 個人年次ランクコード         */
        strcat(sql_buf3, "?,");     /* 家族月次ランクコード         */
        strcat(sql_buf3, "?,");     /* 家族年次ランクコード         */
        strcat(sql_buf3, "?,");     /* 使用ランクコード             */
        strcat(sql_buf3, "?,");     /* 買上額                       */
        strcat(sql_buf3, "?,");     /* 買上回数                     */
        strcat(sql_buf3, "?,");     /* 更新前利用可能ポイント       */
        strcat(sql_buf3, "?,");     /* 更新前付与ポイント           */
        strcat(sql_buf3, "?,");     /* 更新前基本Ｐ率対象ポイント   */
        strcat(sql_buf3, "?,");     /* 更新前月間ランクＵＰ対象金額 */
        strcat(sql_buf3, "?,");     /* 更新前年間ランクＵＰ対象金額 */
        strcat(sql_buf3, "?,");     /* 更新前ポイント対象金額       */
        strcat(sql_buf3, "?,");     /* 更新前買上額                 */
        strcat(sql_buf3, "?,");     /* 家族ＩＤ                     */
        strcat(sql_buf3, "?,");     /* 更新前月間家族ランクＵＰ金額 */
        strcat(sql_buf3, "?,");     /* 更新前年間家族ランクＵＰ金額 */
        strcat(sql_buf3, "null,");     /* リアル更新日時               */
        strcat(sql_buf3, "null,");     /* リアル更新ＡＰＬバージョン   */
        strcat(sql_buf3, "SYSDATE(),");  /* ディレイ更新日時             */
        strcat(sql_buf3, "?,");     /* ディレイ更新ＡＰＬバージョン */
        strcat(sql_buf3, "?,");     /* 相殺フラグ                   */
        strcat(sql_buf3, "?,");     /* 明細チェックフラグ           */
        strcat(sql_buf3, "?,");     /* 明細チェック区分             */
        strcat(sql_buf3, "?,");     /* 作業企業コード               */
        strcat(sql_buf3, "?,");     /* 作業者ＩＤ                   */
        strcat(sql_buf3, "?,");     /* 作業年月日                   */
        strcat(sql_buf3, "?,");     /* 作業時刻                     */
        strcat(sql_buf3, "?,");     /* バッチ更新日                 */
        strcat(sql_buf3, "?,");     /* 最終更新日                   */
        strcat(sql_buf3, "SYSDATE(),");  /* 最終更新日時                 */
        strcat(sql_buf3, "?,");     /* 最終更新プログラムＩＤ       */
        strcat(sql_buf3, "?,");     /* 要求利用Ｐ内訳フラグ         */
        strcat(sql_buf3, "?,");     /* 更新前利用可能通常Ｐ基準年度 */
        strcat(sql_buf3, "?,");     /* 更新前利用可能通常Ｐ前年度   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能通常Ｐ当年度   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能通常Ｐ翌年度   */
        strcat(sql_buf3, "?,");     /* 要求付与通常Ｐ               */
        strcat(sql_buf3, "?,");     /* 要求付与通常Ｐ基準年度       */
        strcat(sql_buf3, "?,");     /* 要求付与通常Ｐ前年度         */
        strcat(sql_buf3, "?,");     /* 要求付与通常Ｐ当年度         */
        strcat(sql_buf3, "?,");     /* 要求利用通常Ｐ               */
        strcat(sql_buf3, "?,");     /* 要求利用通常Ｐ基準年度       */
        strcat(sql_buf3, "?,");     /* 要求利用通常Ｐ前年度         */
        strcat(sql_buf3, "?,");     /* 要求利用通常Ｐ当年度         */
        strcat(sql_buf3, "?,");     /* 要求利用通常Ｐ翌年度         */
        strcat(sql_buf3, "?,");     /* 更新付与通常Ｐ               */
        strcat(sql_buf3, "?,");     /* 更新付与通常Ｐ基準年度       */
        strcat(sql_buf3, "?,");     /* 更新付与通常Ｐ前年度         */
        strcat(sql_buf3, "?,");     /* 更新付与通常Ｐ当年度         */
        strcat(sql_buf3, "?,");     /* 更新利用通常Ｐ               */
        strcat(sql_buf3, "?,");     /* 更新利用通常Ｐ基準年度       */
        strcat(sql_buf3, "?,");     /* 更新利用通常Ｐ前年度         */
        strcat(sql_buf3, "?,");     /* 更新利用通常Ｐ当年度         */
        strcat(sql_buf3, "?,");     /* 更新利用通常Ｐ翌年度         */
        strcat(sql_buf3, "?,");     /* 更新前期間限定Ｐ基準月       */
        strcat(sql_buf3, "?,");     /* 更新前利用可能期間限定Ｐ０   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能期間限定Ｐ１   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能期間限定Ｐ２   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能期間限定Ｐ３   */
        strcat(sql_buf3, "?,");     /* 更新前利用可能期間限定Ｐ４   */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ           */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ基準月     */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ０         */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ１         */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ２         */
        strcat(sql_buf3, "?,");     /* 要求付与期間限定Ｐ３         */
        strcat(sql_buf3, "?,");     /* 要求利用期間限定Ｐ           */
        strcat(sql_buf3, "?,");     /* 要求利用期間限定Ｐ基準月     */
        strcat(sql_buf3, "?,");    /* 要求利用期間限定Ｐ０         */
        strcat(sql_buf3, "?,");    /* 要求利用期間限定Ｐ１         */
        strcat(sql_buf3, "?,");    /* 要求利用期間限定Ｐ２         */
        strcat(sql_buf3, "?,");    /* 要求利用期間限定Ｐ３         */
        strcat(sql_buf3, "?,");    /* 要求利用期間限定Ｐ４         */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ           */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ基準月     */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０１       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０２       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０３       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０４       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０５       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０６       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０７       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０８       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ０９       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ１０       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ１１       */
        strcat(sql_buf3, "?,");    /* 更新付与期間限定Ｐ１２       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ           */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ基準月     */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０１       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０２       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０３       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０４       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０５       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０６       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０７       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０８       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ０９       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ１０       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ１１       */
        strcat(sql_buf3, "?,");    /* 更新利用期間限定Ｐ１２       */
        strcat(sql_buf3, "?,");    /* ＭＫ店番号                   */
        strcat(sql_buf3, "?,");    /* ＭＫ取引番号                 */
        strcat(sql_buf3, "?,");    /* 入会会社コードＭＣＣ         */
        strcat(sql_buf3, "?,");    /* 入会店舗ＭＣＣ               */
        strcat(sql_buf3, "?,");    /* 会社コードＭＣＣ             */
        strcat(sql_buf3, "?,");    /* 店番号ＭＣＣ                 */
        strcat(sql_buf3, "?,");    /* カード種別                   */
        strcat(sql_buf3, "?,");    /* 登録経路                     */
        strcat(sql_buf3, "?,");    /* 取引区分                     */
        strcat(sql_buf3, "?,");    /* 還元種別                     */
        strcat(sql_buf3, "?,");    /* 明細数                       */
        strcat(sql_buf3, "?)");    /* 来店ポイント付与対象区分    */

        /* テーブル名編集 */
        memset(buff, 0x00, sizeof(buff));
        buff = sprintf(buff, "%d", date);
        memset(w_date, 0x00, sizeof(w_date));
        w_date = memcpy(w_date, buff, 6);
        memset(tbl_nam, 0x00, sizeof(tbl_nam));
        tbl_nam = sprintf(tbl_nam, "HSポイント日別情報%s", w_date);
        if (strcmp(Cg_ORASID, penv) != 0) {
            tbl_nam = strcat(tbl_nam, "");
        }

        strcpy(sql_buf, "INSERT INTO ");
        strcat(sql_buf, tbl_nam);
        strcat(sql_buf, sql_buf2);
        strcat(sql_buf, sql_buf3);


        if (DBG_LOG) {
            C_DbgMsg("C_InsertDayPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
//        memset(WRKSQL2.arr, 0x00, sizeof(WRKSQL2.arr));
        strcpy(WRKSQL2, sql_buf);
        WRKSQL2.len = strlen(WRKSQL2.arr);


//        EXEC SQL PREPARE sql_kdatalock2 from:
        sqlca.sql=WRKSQL2;
        sqlca.prepare();

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : PREPARE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年月日 =[%d], 顧客番号=[%s],処理通番号=[%f]", h_tshs_day_point_buff.data_ymd, h_tshs_day_point_buff.kokyaku_no.arr, h_tshs_day_point_buff.shori_seq);
            APLOG_WT("904", 0, null, "INSERT PREPARE", sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* INSERT文を実行する */
//        EXEC SQL EXECUTE sql_kdatalock2 USING

        sqlca.query(h_tshs_day_point_buff.system_ymd.longVal(),                              /* システム年月日               */
                h_tshs_day_point_buff.kokyaku_no.longVal(),                              /* 顧客番号                     */
                h_tshs_day_point_buff.shori_seq.longVal(),                               /* 処理通番                     */
                h_tshs_day_point_buff.kaiin_kigyo_cd.longVal(),                          /* 会員企業コード               */
                h_tshs_day_point_buff.kaiin_kyu_hansya_cd.longVal(),                     /* 会員旧販社コード             */
                h_tshs_day_point_buff.kaiin_no.longVal(),                                /* 会員番号                     */
                h_tshs_day_point_buff.nyukai_kigyo_cd.longVal(),                         /* 入会企業コード               */
                h_tshs_day_point_buff.nyukai_tenpo.longVal(),                            /* 入会店舗                     */
                h_tshs_day_point_buff.hakken_kigyo_cd.longVal(),                         /* 発券企業コード               */
                h_tshs_day_point_buff.hakken_tenpo.longVal(),                            /* 発券店舗                     */
                h_tshs_day_point_buff.seisan_ymd.longVal(),                              /* 精算年月日                   */
                h_tshs_day_point_buff.toroku_ymd.longVal(),                              /* 登録年月日                   */
                h_tshs_day_point_buff.data_ymd.longVal(),                                /* データ年月日                 */
                h_tshs_day_point_buff.kigyo_cd.longVal(),                                /* 企業コード                   */
                h_tshs_day_point_buff.mise_no.longVal(),                                 /* 店番号                       */
                h_tshs_day_point_buff.terminal_no.longVal(),                             /* ターミナル番号               */
                h_tshs_day_point_buff.torihiki_no.longVal(),                             /* 取引番号                     */
                h_tshs_day_point_buff.jikoku_hms.longVal(),                              /* 時刻                         */
                h_tshs_day_point_buff.riyu_cd.longVal(),                                 /* 理由コード                   */
                h_tshs_day_point_buff.circle_id.longVal(),                               /* サークルＩＤ                 */
                h_tshs_day_point_buff.card_nyuryoku_kbn.longVal(),                       /* カード入力区分               */
                h_tshs_day_point_buff.shori_taisho_file_record_no.longVal(),             /* 処理対象ファイルレコード番号 */
                h_tshs_day_point_buff.real_koshin_flg.longVal(),                         /* リアル更新フラグ             */
                h_tshs_day_point_buff.fuyo_point.longVal(),                              /* 付与ポイント                 */
                h_tshs_day_point_buff.riyo_point.longVal(),                              /* 利用ポイント                 */
                h_tshs_day_point_buff.kihon_pritsu_taisho_point.longVal(),               /* 基本Ｐ率対象ポイント         */
                h_tshs_day_point_buff.rankup_taisho_kingaku.longVal(),                   /* ランクＵＰ対象金額           */
                h_tshs_day_point_buff.point_taisho_kingaku.longVal(),                    /* ポイント対象金額             */
                h_tshs_day_point_buff.service_hakko_maisu.longVal(),                     /* サービス券発行枚数           */
                h_tshs_day_point_buff.service_riyo_maisu.longVal(),                      /* サービス券利用枚数           */
                h_tshs_day_point_buff.kojin_getuji_rank_cd.longVal(),                    /* 個人月次ランクコード         */
                h_tshs_day_point_buff.kojin_nenji_rank_cd.longVal(),                     /* 個人年次ランクコード         */
                h_tshs_day_point_buff.kazoku_getuji_rank_cd.longVal(),                   /* 家族月次ランクコード         */
                h_tshs_day_point_buff.kazoku_nenji_rank_cd.longVal(),                    /* 家族年次ランクコード         */
                h_tshs_day_point_buff.shiyo_rank_cd.longVal(),                           /* 使用ランクコード             */
                h_tshs_day_point_buff.kaiage_kingaku.longVal(),                          /* 買上額                       */
                h_tshs_day_point_buff.kaiage_cnt.longVal(),                              /* 買上回数                     */
                h_tshs_day_point_buff.koshinmae_riyo_kano_point.longVal(),               /* 更新前利用可能ポイント       */
                h_tshs_day_point_buff.koshinmae_fuyo_point.longVal(),                    /* 更新前付与ポイント           */
                h_tshs_day_point_buff.koshinmae_kihon_pritsu_taisho_point.longVal(),     /* 更新前基本Ｐ率対象ポイント   */
                h_tshs_day_point_buff.koshinmae_gekkan_kojin_rankup_taisho_kingaku.longVal(), /* 更新前月間ランクＵＰ対象金額 */
                h_tshs_day_point_buff.koshinmae_nenkan_kojin_rankup_taisho_kingaku.longVal(), /* 更新前年間ランクＵＰ対象金額 */
                h_tshs_day_point_buff.koshinmae_point_taisho_kingaku.longVal(),          /* 更新前ポイント対象金額       */
                h_tshs_day_point_buff.koshinmae_kaiage_kingaku.longVal(),                /* 更新前買上額                 */
                h_tshs_day_point_buff.kazoku_id.longVal(),                               /* 家族ＩＤ                     */
                h_tshs_day_point_buff.koshinmae_gekkan_kazoku_rankup_taisho_kingaku.longVal(), /* 更新前月間家族ランクＵＰ金額 */
                h_tshs_day_point_buff.koshinmae_nenkan_kazoku_rankup_taisho_kingaku.longVal(), /* 更新前年間家族ランクＵＰ金額 */
                h_tshs_day_point_buff.delay_koshin_apl_version,                /* ディレイ更新ＡＰＬバージョン */
                h_tshs_day_point_buff.sosai_flg.longVal(),                               /* 相殺フラグ                   */
                h_tshs_day_point_buff.mesai_check_flg.longVal(),                         /* 明細チェックフラグ           */
                h_tshs_day_point_buff.mesai_check_kbn.longVal(),                         /* 明細チェック区分             */
                h_tshs_day_point_buff.sagyo_kigyo_cd.longVal(),                          /* 作業企業コード               */
                h_tshs_day_point_buff.sagyosha_id.longVal(),                             /* 作業者ＩＤ                   */
                h_tshs_day_point_buff.sagyo_ymd.longVal(),                               /* 作業年月日                   */
                h_tshs_day_point_buff.sagyo_hms.longVal(),                               /* 作業時刻                     */
                h_tshs_day_point_buff.batch_koshin_ymd.longVal(),                        /* バッチ更新日                 */
                h_tshs_day_point_buff.saishu_koshin_ymd.longVal(),                       /* 最終更新日                   */
                h_tshs_day_point_buff.saishu_koshin_programid,                 /* 最終更新プログラムＩＤ       */
                h_tshs_day_point_buff.yokyu_riyo_putiwake_flg.longVal(),                 /* 要求利用Ｐ内訳フラグ         */
                h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_kijun_nendo.longVal(), /* 更新前利用可能通常Ｐ基準年度 */
                h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_zennendo.longVal(),    /* 更新前利用可能通常Ｐ前年度   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_tonendo.longVal(),  /* 更新前利用可能通常Ｐ当年度   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_tujo_point_yokunendo.longVal(),/* 更新前利用可能通常Ｐ翌年度   */
                h_tshs_day_point_buff.yokyu_fuyo_tujo_point.longVal(),                   /* 要求付与通常Ｐ               */
                h_tshs_day_point_buff.yokyu_fuyo_tujo_point_kijun_nendo.longVal(),       /* 要求付与通常Ｐ基準年度       */
                h_tshs_day_point_buff.yokyu_fuyo_tujo_poin_zennendo.longVal(),           /* 要求付与通常Ｐ前年度         */
                h_tshs_day_point_buff.yokyu_fuyo_tujo_poin_tonendo.longVal(),            /* 要求付与通常Ｐ当年度         */
                h_tshs_day_point_buff.yokyu_riyo_tujo_point.longVal(),                   /* 要求利用通常Ｐ               */
                h_tshs_day_point_buff.yokyu_riyo_tujo_point_kijun_nendo.longVal(),       /* 要求利用通常Ｐ基準年度       */
                h_tshs_day_point_buff.yokyu_riyo_tujo_point_zennendo.longVal(),          /* 要求利用通常Ｐ前年度         */
                h_tshs_day_point_buff.yokyu_riyo_tujo_point_tonendo.longVal(),           /* 要求利用通常Ｐ当年度         */
                h_tshs_day_point_buff.yokyu_riyo_tujo_point_yokunendo.longVal(),         /* 要求利用通常Ｐ翌年度         */
                h_tshs_day_point_buff.koshin_fuyo_tujo_point.longVal(),                  /* 更新付与通常Ｐ               */
                h_tshs_day_point_buff.koshin_fuyo_tujo_point_kijun_nendo.longVal(),      /* 更新付与通常Ｐ基準年度       */
                h_tshs_day_point_buff.koshin_fuyo_tujo_point_zennendo.longVal(),         /* 更新付与通常Ｐ前年度         */
                h_tshs_day_point_buff.koshin_fuyo_tujo_point_tonendo.longVal(),          /* 更新付与通常Ｐ当年度         */
                h_tshs_day_point_buff.koshin_riyo_tujo_point.longVal(),                  /* 更新利用通常Ｐ               */
                h_tshs_day_point_buff.koshin_riyo_tujo_point_kijun_nendo.longVal(),      /* 更新利用通常Ｐ基準年度       */
                h_tshs_day_point_buff.koshin_riyo_tujo_point_zennendo.longVal(),         /* 更新利用通常Ｐ前年度         */
                h_tshs_day_point_buff.koshin_riyo_tujo_point_tonendo.longVal(),          /* 更新利用通常Ｐ当年度         */
                h_tshs_day_point_buff.koshin_riyo_tujo_point_yokunendo.longVal(),        /* 更新利用通常Ｐ翌年度         */
                h_tshs_day_point_buff.koshinmae_kikan_gentei_point_kijun_month.longVal(),/* 更新前期間限定Ｐ基準月       */
                h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point0.longVal(), /* 更新前利用可能期間限定Ｐ０   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point1.longVal(), /* 更新前利用可能期間限定Ｐ１   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point2.longVal(), /* 更新前利用可能期間限定Ｐ２   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point3.longVal(), /* 更新前利用可能期間限定Ｐ３   */
                h_tshs_day_point_buff.koshinmae_riyo_kano_kikan_gentei_point4.longVal(), /* 更新前利用可能期間限定Ｐ４   */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point.longVal(),           /* 要求付与期間限定Ｐ           */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point_kijun_month.longVal(),/* 要求付与期間限定Ｐ基準月 */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point0.longVal(),          /* 要求付与期間限定Ｐ０         */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point1.longVal(),          /* 要求付与期間限定Ｐ１         */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point2.longVal(),          /* 要求付与期間限定Ｐ２         */
                h_tshs_day_point_buff.yokyu_fuyo_kikan_gentei_point3.longVal(),          /* 要求付与期間限定Ｐ３         */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point.longVal(),           /* 要求利用期間限定Ｐ           */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point_kijun_month.longVal(), /* 要求利用期間限定Ｐ基準月   */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point0.longVal(),          /* 要求利用期間限定Ｐ０         */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point1.longVal(),          /* 要求利用期間限定Ｐ１         */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point2.longVal(),          /* 要求利用期間限定Ｐ２         */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point3.longVal(),          /* 要求利用期間限定Ｐ３         */
                h_tshs_day_point_buff.yokyu_riyo_kikan_gentei_point4.longVal(),          /* 要求利用期間限定Ｐ４         */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point.longVal(),          /* 更新付与期間限定Ｐ           */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point_kijun_month.longVal(), /* 更新付与期間限定Ｐ基準月  */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point01.longVal(),        /* 更新付与期間限定Ｐ０１       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point02.longVal(),        /* 更新付与期間限定Ｐ０２       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point03.longVal(),        /* 更新付与期間限定Ｐ０３       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point04.longVal(),        /* 更新付与期間限定Ｐ０４       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point05.longVal(),        /* 更新付与期間限定Ｐ０５       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point06.longVal(),        /* 更新付与期間限定Ｐ０６       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point07.longVal(),        /* 更新付与期間限定Ｐ０７       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point08.longVal(),        /* 更新付与期間限定Ｐ０８       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point09.longVal(),        /* 更新付与期間限定Ｐ０９       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point10.longVal(),        /* 更新付与期間限定Ｐ１０       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point11.longVal(),        /* 更新付与期間限定Ｐ１１       */
                h_tshs_day_point_buff.koshin_fuyo_kikan_gentei_point12.longVal(),        /* 更新付与期間限定Ｐ１２       */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point.longVal(),          /* 更新利用期間限定Ｐ           */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point_kijun_month.longVal(), /* 更新利用期間限定Ｐ基準月  */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point01.longVal(),           /* 更新利用期間限定Ｐ０１    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point02.longVal(),           /* 更新利用期間限定Ｐ０２    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point03.longVal(),           /* 更新利用期間限定Ｐ０３    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point04.longVal(),           /* 更新利用期間限定Ｐ０４    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point05.longVal(),           /* 更新利用期間限定Ｐ０５    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point06.longVal(),           /* 更新利用期間限定Ｐ０６    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point07.longVal(),           /* 更新利用期間限定Ｐ０７    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point08.longVal(),           /* 更新利用期間限定Ｐ０８    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point09.longVal(),           /* 更新利用期間限定Ｐ０９    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point10.longVal(),           /* 更新利用期間限定Ｐ１０    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point11.longVal(),           /* 更新利用期間限定Ｐ１１    */
                h_tshs_day_point_buff.koshin_riyo_kikan_gentei_point12.longVal(),           /* 更新利用期間限定Ｐ１２    */
                h_tshs_day_point_buff.mk_mise_no.longVal(),                                 /* ＭＫ店番号                */
                h_tshs_day_point_buff.mk_torihiki_no.longVal(),                             /* ＭＫ取引番号              */
                h_tshs_day_point_buff.nyukai_kaisha_cd_mcc.longVal(),                       /* 入会会社コードＭＣＣ      */
                h_tshs_day_point_buff.nyukai_tenpo_mcc.longVal(),                           /* 入会店舗ＭＣＣ            */
                h_tshs_day_point_buff.kaisha_cd_mcc.longVal(),                              /* 会社コードＭＣＣ          */
                h_tshs_day_point_buff.mise_no_mcc.longVal(),                                /* 店番号ＭＣＣ              */
                h_tshs_day_point_buff.card_syubetsu.longVal(),                              /* カード種別                */
                h_tshs_day_point_buff.touroku_keiro.longVal(),                              /* 登録経路                  */
                h_tshs_day_point_buff.torihiki_kbn.longVal(),                               /* 取引区分                  */
                h_tshs_day_point_buff.kangen_shubetsu.longVal(),                            /* 還元種別                  */
                h_tshs_day_point_buff.meisai_su.longVal(),                                  /* 明細数                    */
                h_tshs_day_point_buff.raiten_point_fuyo_taisyo_kbn.longVal());               /* 来店ポイント付与対象区分  */

        /* 重複の場合 */
        if (sqlca.sqlcode == C_const_Ora_DUPL) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : DUPLICATE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            out_format_buf = sprintf(out_format_buf, "年月日 =[%d], 顧客番号=[%s],処理通番号=[%f]", h_tshs_day_point_buff.data_ymd, h_tshs_day_point_buff.kokyaku_no.arr, h_tshs_day_point_buff.shori_seq);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);

            status.arr = C_const_Stat_OK;
            return C_const_DUPL;
        }

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : EXECUTE : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
            out_format_buf = sprintf(out_format_buf, "年月日 =[%d], 顧客番号=[%s],処理通番号=[%f]", h_tshs_day_point_buff.data_ymd, h_tshs_day_point_buff.kokyaku_no.arr, h_tshs_day_point_buff.shori_seq.floatVal());
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }



        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_InsertDayPoint : %s\n", "end");
        }

        tshs_day_point.shori_seq = h_tshs_day_point_buff.shori_seq;
        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_InsertDayPoint Bottom----------------------------------------------*/
    }


    /******************************************************************************/
    /*                                                                            */
    /*  関数名 ： BT_Rtrim                                                        */
    /*                                                                            */
    /*  書式                                                                      */
    /*  int       BT_Rtrim()                                                      */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*               スペース削除処理                                             */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*      char       * strp          : 編集文字列                               */
    /*      int          size          : サイズ                                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0   ： 正常                                                   */
    /*                                                                            */

    /******************************************************************************/
    @Override
    // TODO:
    public int BT_Rtrim(StringDto strp, int size) {
        StringBuilder sb = new StringBuilder();
        sb.replace(0, strp.arr.length(), strp.arr);
        int char_cnt = size;

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgStart("スペース削除処理");
            /*---------------------------------------------------------------------*/
        }

//        StringDto tmp = new StringDto(strp.arr);
//        while (true) {
//            String tmp2 = tmp.arr;
//            tmp.arr = tmp.cut(char_cnt - 3);
//            if (char_cnt - 3 >= 0 && strncmp(tmp.arr, "　", 3) == 0) {
//                /* スペース(全角)  */
//                char_cnt -= 3;
//                if (char_cnt == 0) {
//                    sb.setCharAt(0, (char) 0x00);
//                    break;
//                }
//                continue;
//            }
//            tmp.arr = tmp2;
//            tmp.arr = tmp.cut(char_cnt - 1);
//            if (char_cnt - 1 >= 0 && tmp.arr.charAt(tmp.arr.getBytes().length - 1) == 0x20) {
//                /* スペース(半角)  */
//                char_cnt--;
//                if (char_cnt == 0) {
//                    sb.setCharAt(0, (char) 0x00);
//                    break;
//                }
//                continue;
//            }
//            tmp.arr = tmp2;
//            if (char_cnt != size) {
//                sb.setCharAt(char_cnt, (char) 0x00);
//            }
//            break;
//        }

        while (trim(strp)) {

        }

        if (DBG_LOG) {
            /*---------------------------------------------------------------------*/
            C_DbgEnd("スペース削除処理", C_const_OK, 0, 0);
            /*---------------------------------------------------------------------*/
        }

//        strp.arr = strp.arr;
        return C_const_OK;

        /*-----BT_Rtrim Bottom----------------------------------------------*/
    }

    public boolean trim(StringDto str) {
        if (str.arr.endsWith(" ")) {
            str.arr = str.arr.substring(0, str.arr.lastIndexOf(" "));
            return true;
        } else if (str.arr.endsWith("　")) {
            str.arr = str.arr.substring(0, str.arr.lastIndexOf("　"));
            return true;
        }
        return false;
    }

    public int BT_Rtrim(String[] strp, int size) {
        StringDto item = new StringDto();
        item.arr = strp[0];
        int result = BT_Rtrim(item, size);
        strp[0] = item.arr;
        return result;
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetCmMaster                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetCmMaster(CM_MASTER  *cmMaster, int *status)                  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              顧客情報取得処理                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              CM_MASTER * cmMaster ： 顧客情報構造体取得パラメータ          */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetCmMaster(MM_KOKYAKU_INFO_TBL cmMaster, IntegerDto status) {

        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;


        /* ホスト変数 */
//        sqlcaManager.get("");
//        EXEC SQL BEGIN DECLARE SECTION;

        MM_KOKYAKU_INFO_TBL h_mm_kokyaku_buff = new MM_KOKYAKU_INFO_TBL(); /* MM顧客情報バッファ */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_GetCmMaster : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (cmMaster == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetCmMaster : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetCmMaster : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }


        /* 出力エリアのクリア */
        cmMaster.kyumin_flg.arr = 0;                  /* 休眠フラグ           */

        /* 顧客名称             */
        strcpy(cmMaster.kokyaku_mesho,
                "　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　");
        /* 顧客カナ名称         */
        memset(cmMaster.kokyaku_kana_mesho, ' ',
                sizeof(cmMaster.kokyaku_kana_mesho));
        cmMaster.nenre.arr = 0;                       /* 年齢                   */
        cmMaster.tanjo_y.arr = 0;                     /* 誕生年                 */
        cmMaster.tanjo_m.arr = 0;                     /* 誕生月                 */
        cmMaster.tanjo_d.arr = 0;                     /* 誕生日                 */
        cmMaster.sebetsu.arr = 0;                     /* 性別                   */
        cmMaster.konin.arr = 0;                       /* 婚姻                   */
        cmMaster.nyukai_kigyo_cd.arr = 0;             /* 入会企業コード         */
        cmMaster.nyukai_tenpo.arr = 0;                /* 入会店舗               */
        cmMaster.hakken_kigyo_cd.arr = 0;             /* 発券企業コード         */
        cmMaster.hakken_tenpo.arr = 0;                /* 発券店舗               */
        cmMaster.shain_kbn.arr = 0;                   /* 社員区分               */
        cmMaster.portal_nyukai_ymd.arr = 0;           /* ポータル入会年月日     */
        cmMaster.portal_taikai_ymd.arr = 0;           /* ポータル退会年月日     */
        cmMaster.sagyo_kigyo_cd.arr = 0;              /* 作業企業コード         */
        cmMaster.sagyosha_id.arr = 0;                 /* 作業者ＩＤ             */
        cmMaster.sagyo_ymd.arr = 0;                   /* 作業年月日             */
        cmMaster.sagyo_hms.arr = 0;                   /* 作業時刻               */
        cmMaster.batch_koshin_ymd.arr = 0;            /* バッチ更新日           */
        cmMaster.saishu_setai_ymd.arr = 0;            /* 最終静態更新日         */
        cmMaster.saishu_setai_hms.arr = 0;            /* 最終静態更新時刻       */
        cmMaster.saishu_koshin_ymd.arr = 0;           /* 最終更新日             */
        cmMaster.saishu_koshin_ymdhms.arr = 0;        /* 最終更新日時           */
        strcpy(cmMaster.saishu_koshin_programid,
                "                    ");            /* 最終更新プログラムＩＤ */
        /* 2023/05/24 MCCMPH2 ADD START */
        strcpy(cmMaster.keiyaku_no,
                "                ");                 /* 契約番号              */
        strcpy(cmMaster.kyu_keiyaku_no,
                "                ");                 /* 旧契約番号            */
        cmMaster.teikeisaki_soshiki_cd_3.arr = 0;      /* 提携先組織コード３    */
        cmMaster.henkan_funo_moji_umu_kbn.arr = 0;     /* 変換不能文字有無区分  */

        /* 2023/05/24 MCCMPH2 ADD END */
        /* ホスト変数を編集する */
//        memset(h_mm_kokyaku_buff, 0x00, sizeof(MM_KOKYAKU_INFO_TBL.class));
        memset(h_mm_kokyaku_buff, 0x00, sizeof(0));
        memcpy(h_mm_kokyaku_buff.kokyaku_no, cmMaster.kokyaku_no,
                cmMaster.kokyaku_no.len);
        h_mm_kokyaku_buff.kokyaku_no.len = cmMaster.kokyaku_no.len;

        if (DBG_LOG) {
            C_DbgMsg("顧客番号          [%s]\n", h_mm_kokyaku_buff.kokyaku_no.arr);
        }

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_MD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_MD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを実行する */
        sqlca.sql = new StringDto();
        /* 2023/05/24 MCCMPH2 MOD START */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客データベースに接続している場合 */
            sqlca.sql.arr = "SELECT NVL(休眠フラグ,0)," +
                    "            NVL(RPAD(顧客名称,LENGTH(顧客名称)),'　　　　　　　　　　')," +
                    "                    NVL(RPAD(顧客カナ名称,LENGTH(顧客カナ名称)),'                    ')," +
                    "                    NVL(年齢,0)," +
                    "                    NVL(誕生年,0)," +
                    "                    NVL(誕生月,0)," +
                    "                    NVL(誕生日,0)," +
                    "                    NVL(性別,0)," +
                    "                    NVL(婚姻,0)," +
                    "                    NVL(入会企業コード,0)," +
                    "                    NVL(入会店舗,0)," +
                    "                    NVL(発券企業コード,0)," +
                    "                    NVL(発券店舗,0)," +
                    "                    NVL(社員区分,0)," +
                    "                    NVL(ポータル入会年月日,0)," +
                    "                    NVL(ポータル退会年月日,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終静態更新日,0)," +
                    "                    NVL(最終静態更新時刻,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(契約番号,'                ')," +
                    "                    NVL(旧契約番号,'                ')," +
                    "                    NVL(提携先組織コード３,0)," +
                    "                    NVL(変換不能文字有無区分,0)" +
                    "            FROM  MM顧客情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(休眠フラグ,0),
//            NVL(顧客名称,'　　　　　　　　　　'),
//                    NVL(顧客カナ名称,'                    '),
//                    NVL(年齢,0),
//                    NVL(誕生年,0),
//                    NVL(誕生月,0),
//                    NVL(誕生日,0),
//                    NVL(性別,0),
//                    NVL(婚姻,0),
//                    NVL(入会企業コード,0),
//                    NVL(入会店舗,0),
//                    NVL(発券企業コード,0),
//                    NVL(発券店舗,0),
//                    NVL(社員区分,0),
//                    NVL(ポータル入会年月日,0),
//                    NVL(ポータル退会年月日,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終静態更新日,0),
//                    NVL(最終静態更新時刻,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(NVL(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(契約番号,'                '),
//                    NVL(旧契約番号,'                '),
//                    NVL(提携先組織コード３,0),
//                    NVL(変換不能文字有無区分,0)
//            INTO :h_mm_kokyaku_buff.kyumin_flg,
//                            :h_mm_kokyaku_buff.kokyaku_mesho,
//                            :h_mm_kokyaku_buff.kokyaku_kana_mesho,
//                            :h_mm_kokyaku_buff.nenre,
//                            :h_mm_kokyaku_buff.tanjo_y,
//                            :h_mm_kokyaku_buff.tanjo_m,
//                            :h_mm_kokyaku_buff.tanjo_d,
//                            :h_mm_kokyaku_buff.sebetsu,
//                            :h_mm_kokyaku_buff.konin,
//                            :h_mm_kokyaku_buff.nyukai_kigyo_cd,
//                            :h_mm_kokyaku_buff.nyukai_tenpo,
//                            :h_mm_kokyaku_buff.hakken_kigyo_cd,
//                            :h_mm_kokyaku_buff.hakken_tenpo,
//                            :h_mm_kokyaku_buff.shain_kbn,
//                            :h_mm_kokyaku_buff.portal_nyukai_ymd,
//                            :h_mm_kokyaku_buff.portal_taikai_ymd,
//                            :h_mm_kokyaku_buff.sagyo_kigyo_cd,
//                            :h_mm_kokyaku_buff.sagyosha_id,
//                            :h_mm_kokyaku_buff.sagyo_ymd,
//                            :h_mm_kokyaku_buff.sagyo_hms,
//                            :h_mm_kokyaku_buff.batch_koshin_ymd,
//                            :h_mm_kokyaku_buff.saishu_setai_ymd,
//                            :h_mm_kokyaku_buff.saishu_setai_hms,
//                            :h_mm_kokyaku_buff.saishu_koshin_ymd,
//                            :h_mm_kokyaku_buff.saishu_koshin_ymdhms,
//                            :h_mm_kokyaku_buff.saishu_koshin_programid,
//                            :h_mm_kokyaku_buff.keiyaku_no,
//                            :h_mm_kokyaku_buff.kyu_keiyaku_no,
//                            :h_mm_kokyaku_buff.teikeisaki_soshiki_cd_3,
//                            :h_mm_kokyaku_buff.henkan_funo_moji_umu_kbn
//            FROM  MM顧客情報
//            WHERE 顧客番号       = :h_mm_kokyaku_buff.kokyaku_no;
            sqlca.restAndExecute(h_mm_kokyaku_buff.kokyaku_no);
        } else {
            /* 顧客データベース以外に接続している場合 */
            sqlca.sql.arr = "SELECT NVL(休眠フラグ,0)," +
                    "            NVL(RPAD(顧客名称,LENGTH(顧客名称)),'　　　　　　　　　　')," +
                    "                    NVL(RPAD(顧客カナ名称,LENGTH(顧客カナ名称)),'                    ')," +
                    "                    NVL(年齢,0)," +
                    "                    NVL(誕生年,0)," +
                    "                    NVL(誕生月,0)," +
                    "                    NVL(誕生日,0)," +
                    "                    NVL(性別,0)," +
                    "                    NVL(婚姻,0)," +
                    "                    NVL(入会企業コード,0)," +
                    "                    NVL(入会店舗,0)," +
                    "                    NVL(発券企業コード,0)," +
                    "                    NVL(発券店舗,0)," +
                    "                    NVL(社員区分,0)," +
                    "                    NVL(ポータル入会年月日,0)," +
                    "                    NVL(ポータル退会年月日,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終静態更新日,0)," +
                    "                    NVL(最終静態更新時刻,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(契約番号,'                ')," +
                    "                    NVL(旧契約番号,'                ')," +
                    "                    NVL(提携先組織コード３,0)," +
                    "                    NVL(変換不能文字有無区分,0)" +
                    "            FROM  MM顧客情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(休眠フラグ,0),
//            NVL(顧客名称,'　　　　　　　　　　'),
//                    NVL(顧客カナ名称,'                    '),
//                    NVL(年齢,0),
//                    NVL(誕生年,0),
//                    NVL(誕生月,0),
//                    NVL(誕生日,0),
//                    NVL(性別,0),
//                    NVL(婚姻,0),
//                    NVL(入会企業コード,0),
//                    NVL(入会店舗,0),
//                    NVL(発券企業コード,0),
//                    NVL(発券店舗,0),
//                    NVL(社員区分,0),
//                    NVL(ポータル入会年月日,0),
//                    NVL(ポータル退会年月日,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終静態更新日,0),
//                    NVL(最終静態更新時刻,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(NVL(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(契約番号,'                '),
//                    NVL(旧契約番号,'                '),
//                    NVL(提携先組織コード３,0),
//                    NVL(変換不能文字有無区分,0)
//                       INTO :h_mm_kokyaku_buff.kyumin_flg,
//                            :h_mm_kokyaku_buff.kokyaku_mesho,
//                            :h_mm_kokyaku_buff.kokyaku_kana_mesho,
//                            :h_mm_kokyaku_buff.nenre,
//                            :h_mm_kokyaku_buff.tanjo_y,
//                            :h_mm_kokyaku_buff.tanjo_m,
//                            :h_mm_kokyaku_buff.tanjo_d,
//                            :h_mm_kokyaku_buff.sebetsu,
//                            :h_mm_kokyaku_buff.konin,
//                            :h_mm_kokyaku_buff.nyukai_kigyo_cd,
//                            :h_mm_kokyaku_buff.nyukai_tenpo,
//                            :h_mm_kokyaku_buff.hakken_kigyo_cd,
//                            :h_mm_kokyaku_buff.hakken_tenpo,
//                            :h_mm_kokyaku_buff.shain_kbn,
//                            :h_mm_kokyaku_buff.portal_nyukai_ymd,
//                            :h_mm_kokyaku_buff.portal_taikai_ymd,
//                            :h_mm_kokyaku_buff.sagyo_kigyo_cd,
//                            :h_mm_kokyaku_buff.sagyosha_id,
//                            :h_mm_kokyaku_buff.sagyo_ymd,
//                            :h_mm_kokyaku_buff.sagyo_hms,
//                            :h_mm_kokyaku_buff.batch_koshin_ymd,
//                            :h_mm_kokyaku_buff.saishu_setai_ymd,
//                            :h_mm_kokyaku_buff.saishu_setai_hms,
//                            :h_mm_kokyaku_buff.saishu_koshin_ymd,
//                            :h_mm_kokyaku_buff.saishu_koshin_ymdhms,
//                            :h_mm_kokyaku_buff.saishu_koshin_programid,
//                            :h_mm_kokyaku_buff.keiyaku_no,
//                            :h_mm_kokyaku_buff.kyu_keiyaku_no,
//                            :h_mm_kokyaku_buff.teikeisaki_soshiki_cd_3,
//                            :h_mm_kokyaku_buff.henkan_funo_moji_umu_kbn
//            FROM  MM顧客情報@CMMD
//            WHERE 顧客番号       = :h_mm_kokyaku_buff.kokyaku_no;
            sqlca.restAndExecute(h_mm_kokyaku_buff.kokyaku_no);
        }
        /* 2023/05/24 MCCMPH2 MOD END */
        sqlca.fetch();
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s",
                    h_mm_kokyaku_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "SELECT", (long) sqlca.sqlcode,
                    "MM顧客情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }
        sqlca.recData(h_mm_kokyaku_buff.kyumin_flg,
                h_mm_kokyaku_buff.kokyaku_mesho,
                h_mm_kokyaku_buff.kokyaku_kana_mesho,
                h_mm_kokyaku_buff.nenre,
                h_mm_kokyaku_buff.tanjo_y,
                h_mm_kokyaku_buff.tanjo_m,
                h_mm_kokyaku_buff.tanjo_d,
                h_mm_kokyaku_buff.sebetsu,
                h_mm_kokyaku_buff.konin,
                h_mm_kokyaku_buff.nyukai_kigyo_cd,
                h_mm_kokyaku_buff.nyukai_tenpo,
                h_mm_kokyaku_buff.hakken_kigyo_cd,
                h_mm_kokyaku_buff.hakken_tenpo,
                h_mm_kokyaku_buff.shain_kbn,
                h_mm_kokyaku_buff.portal_nyukai_ymd,
                h_mm_kokyaku_buff.portal_taikai_ymd,
                h_mm_kokyaku_buff.sagyo_kigyo_cd,
                h_mm_kokyaku_buff.sagyosha_id,
                h_mm_kokyaku_buff.sagyo_ymd,
                h_mm_kokyaku_buff.sagyo_hms,
                h_mm_kokyaku_buff.batch_koshin_ymd,
                h_mm_kokyaku_buff.saishu_setai_ymd,
                h_mm_kokyaku_buff.saishu_setai_hms,
                h_mm_kokyaku_buff.saishu_koshin_ymd,
                h_mm_kokyaku_buff.saishu_koshin_ymdhms,
                h_mm_kokyaku_buff.saishu_koshin_programid,
                h_mm_kokyaku_buff.keiyaku_no,
                h_mm_kokyaku_buff.kyu_keiyaku_no,
                h_mm_kokyaku_buff.teikeisaki_soshiki_cd_3,
                h_mm_kokyaku_buff.henkan_funo_moji_umu_kbn);

        /* 出力引数の設定 */
        cmMaster.kyumin_flg = h_mm_kokyaku_buff.kyumin_flg;           /* 休眠フラグ             */
        memcpy(cmMaster.kokyaku_mesho, h_mm_kokyaku_buff.kokyaku_mesho,
                sizeof(h_mm_kokyaku_buff.kokyaku_mesho));                          /* 顧客名称               */
        memcpy(cmMaster.kokyaku_kana_mesho,
                h_mm_kokyaku_buff.kokyaku_kana_mesho,
                sizeof(h_mm_kokyaku_buff.kokyaku_kana_mesho));                     /* 顧客カナ名称           */
        cmMaster.nenre = h_mm_kokyaku_buff.nenre;                /* 年齢                   */
        cmMaster.tanjo_y = h_mm_kokyaku_buff.tanjo_y;              /* 誕生年                 */
        cmMaster.tanjo_m = h_mm_kokyaku_buff.tanjo_m;              /* 誕生月                 */
        cmMaster.tanjo_d = h_mm_kokyaku_buff.tanjo_d;              /* 誕生日                 */
        cmMaster.sebetsu = h_mm_kokyaku_buff.sebetsu;              /* 性別                   */
        cmMaster.konin = h_mm_kokyaku_buff.konin;                /* 婚姻                   */
        cmMaster.nyukai_kigyo_cd = h_mm_kokyaku_buff.nyukai_kigyo_cd;      /* 入会企業コード         */
        cmMaster.nyukai_tenpo = h_mm_kokyaku_buff.nyukai_tenpo;         /* 入会店舗               */
        cmMaster.hakken_kigyo_cd = h_mm_kokyaku_buff.hakken_kigyo_cd;      /* 発券企業コード         */
        cmMaster.hakken_tenpo = h_mm_kokyaku_buff.hakken_tenpo;         /* 発券店舗               */
        cmMaster.shain_kbn = h_mm_kokyaku_buff.shain_kbn;            /* 社員区分               */
        cmMaster.portal_nyukai_ymd = h_mm_kokyaku_buff.portal_nyukai_ymd;    /* ポータル入会年月日     */
        cmMaster.portal_taikai_ymd = h_mm_kokyaku_buff.portal_taikai_ymd;    /* ポータル退会年月日     */
        cmMaster.sagyo_kigyo_cd = h_mm_kokyaku_buff.sagyo_kigyo_cd;       /* 作業企業コード         */
        cmMaster.sagyosha_id = h_mm_kokyaku_buff.sagyosha_id;          /* 作業者ＩＤ             */
        cmMaster.sagyo_ymd = h_mm_kokyaku_buff.sagyo_ymd;            /* 作業年月日             */
        cmMaster.sagyo_hms = h_mm_kokyaku_buff.sagyo_hms;            /* 作業時刻               */
        cmMaster.batch_koshin_ymd = h_mm_kokyaku_buff.batch_koshin_ymd;     /* バッチ更新日           */
        cmMaster.saishu_setai_ymd = h_mm_kokyaku_buff.saishu_setai_ymd;     /* 最終静態更新日         */
        cmMaster.saishu_setai_hms = h_mm_kokyaku_buff.saishu_setai_hms;     /* 最終静態更新時刻       */
        cmMaster.saishu_koshin_ymd = h_mm_kokyaku_buff.saishu_koshin_ymd;    /* 最終更新日             */
        cmMaster.saishu_koshin_ymdhms = h_mm_kokyaku_buff.saishu_koshin_ymdhms; /* 最終更新日時           */
        strcpy(cmMaster.saishu_koshin_programid,
                h_mm_kokyaku_buff.saishu_koshin_programid);                        /* 最終更新プログラムＩＤ */
        /* 2023/05/24 MCCMPH2 ADD START */
        strcpy(cmMaster.keiyaku_no,
                h_mm_kokyaku_buff.keiyaku_no);                                     /* 契約番号               */
        strcpy(cmMaster.kyu_keiyaku_no,
                h_mm_kokyaku_buff.kyu_keiyaku_no);                                 /* 旧契約番号             */
        cmMaster.teikeisaki_soshiki_cd_3 = h_mm_kokyaku_buff.teikeisaki_soshiki_cd_3;    /* 提携先組織コード３                   */
        cmMaster.henkan_funo_moji_umu_kbn = h_mm_kokyaku_buff.henkan_funo_moji_umu_kbn;   /* 変換不能文字有無区分                 */
        /* 2023/05/24 MCCMPH2 ADD END */

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_GetCmMaster : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_GetCmMaster Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetCsMaster                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetCsMaster(CS_MASTER  *csMaster, int status)                  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              顧客制度情報取得                                              */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              CS_MASTER * csMaster ： 顧客制度情報構造体取得パラメータ      */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetCsMaster(MS_KOKYAKU_SEDO_INFO_TBL csMaster, IntegerDto status) {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        MS_KOKYAKU_SEDO_INFO_TBL h_ms_kokyaku_seido_buff = new MS_KOKYAKU_SEDO_INFO_TBL(); /* MS顧客制度情報バッファ */
//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_GetcsMaster : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (csMaster == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetcsMaster : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetcsMaster : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }


        /* 出力エリアのクリア */
        csMaster.tanjo_m.arr = 0;                        /* 誕生月                   */
        csMaster.entry.arr = 0;                          /* エントリー               */
        csMaster.senior.arr = 0;                         /* シニア                   */
        csMaster.nenji_rank_cd_0.arr = 0;                /* 年次ランクコード０       */
        csMaster.nenji_rank_cd_1.arr = 0;                /* 年次ランクコード１       */
        csMaster.nenji_rank_cd_2.arr = 0;                /* 年次ランクコード２       */
        csMaster.nenji_rank_cd_3.arr = 0;                /* 年次ランクコード３       */
        csMaster.nenji_rank_cd_4.arr = 0;                /* 年次ランクコード４       */
        csMaster.nenji_rank_cd_5.arr = 0;                /* 年次ランクコード５       */
        csMaster.nenji_rank_cd_6.arr = 0;                /* 年次ランクコード６       */
        csMaster.nenji_rank_cd_7.arr = 0;                /* 年次ランクコード７       */
        csMaster.nenji_rank_cd_8.arr = 0;                /* 年次ランクコード８       */
        csMaster.nenji_rank_cd_9.arr = 0;                /* 年次ランクコード９       */
        csMaster.getuji_rank_cd_001.arr = 0;             /* 月次ランクコード００１   */
        csMaster.getuji_rank_cd_002.arr = 0;             /* 月次ランクコード００２   */
        csMaster.getuji_rank_cd_003.arr = 0;             /* 月次ランクコード００３   */
        csMaster.getuji_rank_cd_004.arr = 0;             /* 月次ランクコード００４   */
        csMaster.getuji_rank_cd_005.arr = 0;             /* 月次ランクコード００５   */
        csMaster.getuji_rank_cd_006.arr = 0;             /* 月次ランクコード００６   */
        csMaster.getuji_rank_cd_007.arr = 0;             /* 月次ランクコード００７   */
        csMaster.getuji_rank_cd_008.arr = 0;             /* 月次ランクコード００８   */
        csMaster.getuji_rank_cd_009.arr = 0;             /* 月次ランクコード００９   */
        csMaster.getuji_rank_cd_010.arr = 0;             /* 月次ランクコード０１０   */
        csMaster.getuji_rank_cd_011.arr = 0;             /* 月次ランクコード０１１   */
        csMaster.getuji_rank_cd_012.arr = 0;             /* 月次ランクコード０１２   */
        csMaster.getuji_rank_cd_101.arr = 0;             /* 月次ランクコード１０１   */
        csMaster.getuji_rank_cd_102.arr = 0;             /* 月次ランクコード１０２   */
        csMaster.getuji_rank_cd_103.arr = 0;             /* 月次ランクコード１０３   */
        csMaster.getuji_rank_cd_104.arr = 0;             /* 月次ランクコード１０４   */
        csMaster.getuji_rank_cd_105.arr = 0;             /* 月次ランクコード１０５   */
        csMaster.getuji_rank_cd_106.arr = 0;             /* 月次ランクコード１０６   */
        csMaster.getuji_rank_cd_107.arr = 0;             /* 月次ランクコード１０７   */
        csMaster.getuji_rank_cd_108.arr = 0;             /* 月次ランクコード１０８   */
        csMaster.getuji_rank_cd_109.arr = 0;             /* 月次ランクコード１０９   */
        csMaster.getuji_rank_cd_110.arr = 0;             /* 月次ランクコード１１０   */
        csMaster.getuji_rank_cd_111.arr = 0;             /* 月次ランクコード１１１   */
        csMaster.getuji_rank_cd_112.arr = 0;             /* 月次ランクコード１１２   */
        csMaster.circle_id_1.arr = 0;                    /* サークルＩＤ１           */
        csMaster.circle_id_2.arr = 0;                    /* サークルＩＤ２           */
        csMaster.circle_id_3.arr = 0;                    /* サークルＩＤ３           */
        csMaster.circle_id_4.arr = 0;                    /* サークルＩＤ４           */
        csMaster.circle_id_5.arr = 0;                    /* サークルＩＤ５           */
        csMaster.zaiseki_kaishi_ym.arr = 0;              /* 在籍開始年月             */
        csMaster.shussan_coupon_hakko_flg_1.arr = 0;     /* 出産クーポン発行可否１   */
        csMaster.shussan_coupon_hakko_flg_2.arr = 0;     /* 出産クーポン発行可否２   */
        csMaster.shussan_coupon_hakko_flg_3.arr = 0;     /* 出産クーポン発行可否３   */
        csMaster.shain_kbn.arr = 0;                      /* 社員区分                 */
        csMaster.portal_kaiin_flg.arr = 0;               /* ポータル会員フラグ       */
        csMaster.ec_kaiin_flg.arr = 0;                   /* ＥＣ会員フラグ           */
        csMaster.mobile_kaiin_flg.arr = 0;               /* モバイル会員フラグ       */
        csMaster.denwa_no_toroku_flg.arr = 0;            /* 電話番号登録フラグ       */
        csMaster.setai_torikomizumi_flg.arr = 0;         /* 静態取込済みフラグ       */
        /*csMaster.kazoku_id.arr = 0;                         家族ＩＤ                 */
        strcpy(csMaster.kazoku_id, "0");   /* 家族ＩＤ                 */
        csMaster.flg_1.arr = 0;                          /* フラグ１                 */
        csMaster.flg_2.arr = 0;                          /* フラグ２                 */
        csMaster.flg_3.arr = 0;                          /* フラグ３                 */
        csMaster.flg_4.arr = 0;                          /* フラグ４                 */
        csMaster.flg_5.arr = 0;                          /* フラグ５                 */
        csMaster.sagyo_kigyo_cd.arr = 0;                 /* 作業企業コード           */
        csMaster.sagyosha_id.arr = 0;                    /* 作業者ＩＤ               */
        csMaster.sagyo_ymd.arr = 0;                      /* 作業年月日               */
        csMaster.sagyo_hms.arr = 0;                      /* 作業時刻                 */
        csMaster.batch_koshin_ymd.arr = 0;               /* バッチ更新日             */
        csMaster.saishu_koshin_ymd.arr = 0;              /* 最終更新日               */
        csMaster.saishu_koshin_ymdhms.arr = 0;           /* 最終更新日時             */
        strcpy(csMaster.saishu_koshin_programid,
                "                    ");               /* 最終更新プログラムＩＤ   */
        /* 2023/05/24 MCCMPH2 ADD START */
        csMaster.sampling_yohi_flg.arr = 0;                      /* サンプリング要否フラグ          */
        csMaster.corporate_id_sentaku_mail_shubetsu.arr = 0;     /* コーポレートＩＤ選択メール種別  */
        csMaster.dejitalu_ec_kaiin_nyukai_flg.arr = 0;           /* デジタル会員ＥＣ入会フラグ      */
        csMaster.dejitalu_ec_kaiin_nyukai_ymdhms.arr = 0;        /* デジタル会員ＥＣ入会更新日時    */
        csMaster.dejitalu_ap_kaiin_nyukai_flg.arr = 0;           /* デジタル会員アプリ入会フラグ    */
        csMaster.dejitalu_ap_kaiin_nyukai_ymdhms.arr = 0;        /* デジタル会員アプリ入会更新日時  */
        csMaster.kyu_nyukai_ymd.arr = 0;                         /* 旧入会年月日                    */

        /* 2023/05/24 MCCMPH2 ADD END */

        /* ホスト変数を編集する */
        memset(h_ms_kokyaku_seido_buff, 0x00, sizeof(0));
//        memset(h_ms_kokyaku_seido_buff, 0x00, sizeof(MS_KOKYAKU_SEDO_INFO_TBL));
        memcpy(h_ms_kokyaku_seido_buff.kokyaku_no, csMaster.kokyaku_no,
                csMaster.kokyaku_no.len);
        h_ms_kokyaku_seido_buff.kokyaku_no.len = csMaster.kokyaku_no.len;


        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        sqlca.sql = new StringDto();
        /* ＳＱＬを実行する */
        /* 2023/05/24 MCCMPH2 MOD START */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客制度データベースに接続している場合     */
            /* 休眠フラグは参照不可なので０固定とする */
            sqlca.sql.arr = " SELECT NVL(誕生月,0)," +
                    "            NVL(エントリー,0)," +
                    "                    NVL(シニア,0)," +
                    "                    NVL(年次ランクコード０,0)," +
                    "                    NVL(年次ランクコード１,0)," +
                    "                    NVL(年次ランクコード２,0)," +
                    "                    NVL(年次ランクコード３,0)," +
                    "                    NVL(年次ランクコード４,0)," +
                    "                    NVL(年次ランクコード５,0)," +
                    "                    NVL(年次ランクコード６,0)," +
                    "                    NVL(年次ランクコード７,0)," +
                    "                    NVL(年次ランクコード８,0)," +
                    "                    NVL(年次ランクコード９,0)," +
                    "                    NVL(月次ランクコード００１,0)," +
                    "                    NVL(月次ランクコード００２,0)," +
                    "                    NVL(月次ランクコード００３,0)," +
                    "                    NVL(月次ランクコード００４,0)," +
                    "                    NVL(月次ランクコード００５,0)," +
                    "                    NVL(月次ランクコード００６,0)," +
                    "                    NVL(月次ランクコード００７,0)," +
                    "                    NVL(月次ランクコード００８,0)," +
                    "                    NVL(月次ランクコード００９,0)," +
                    "                    NVL(月次ランクコード０１０,0)," +
                    "                    NVL(月次ランクコード０１１,0)," +
                    "                    NVL(月次ランクコード０１２,0)," +
                    "                    NVL(月次ランクコード１０１,0)," +
                    "                    NVL(月次ランクコード１０２,0)," +
                    "                    NVL(月次ランクコード１０３,0)," +
                    "                    NVL(月次ランクコード１０４,0)," +
                    "                    NVL(月次ランクコード１０５,0)," +
                    "                    NVL(月次ランクコード１０６,0)," +
                    "                    NVL(月次ランクコード１０７,0)," +
                    "                    NVL(月次ランクコード１０８,0)," +
                    "                    NVL(月次ランクコード１０９,0)," +
                    "                    NVL(月次ランクコード１１０,0)," +
                    "                    NVL(月次ランクコード１１１,0)," +
                    "                    NVL(月次ランクコード１１２,0)," +
                    "                    NVL(サークルＩＤ１,0)," +
                    "                    NVL(サークルＩＤ２,0)," +
                    "                    NVL(サークルＩＤ３,0)," +
                    "                    NVL(サークルＩＤ４,0)," +
                    "                    NVL(サークルＩＤ５,0)," +
                    "                    NVL(在籍開始年月,0)," +
                    "                    NVL(出産クーポン発行可否１,0)," +
                    "                    NVL(出産クーポン発行可否２,0)," +
                    "                    NVL(出産クーポン発行可否３,0)," +
                    "                    NVL(社員区分,0)," +
                    "                    NVL(ポータル会員フラグ,0)," +
                    "                    NVL(ＥＣ会員フラグ,0)," +
                    "                    NVL(モバイル会員フラグ,0)," +
                    "                    NVL(電話番号登録フラグ,0)," +
                    "                    NVL(静態取込済みフラグ,0)," +
                    "                    NVL(家族ＩＤ,0)," +
                    "                    NVL(フラグ１,0)," +
                    "                    NVL(フラグ２,0)," +
                    "                    NVL(フラグ３,0)," +
                    "                    NVL(フラグ４,0)," +
                    "                    NVL(フラグ５,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(サンプリング要否フラグ,0)," +
                    "                    NVL(コーポレートＩＤ選択メール種別,0)," +
                    "                    NVL(デジタル会員ＥＣ入会フラグ,0)," +
                    "                    to_number(to_char(coalesce(デジタル会員ＥＣ入会更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(デジタル会員アプリ入会フラグ,0)," +
                    "                    to_number(to_char(coalesce(デジタル会員アプリ入会更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(旧入会年月日,0)" +
                    "            FROM  MS顧客制度情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(誕生月,0),
//            NVL(エントリー,0),
//                    NVL(シニア,0),
//                    NVL(年次ランクコード０,0),
//                    NVL(年次ランクコード１,0),
//                    NVL(年次ランクコード２,0),
//                    NVL(年次ランクコード３,0),
//                    NVL(年次ランクコード４,0),
//                    NVL(年次ランクコード５,0),
//                    NVL(年次ランクコード６,0),
//                    NVL(年次ランクコード７,0),
//                    NVL(年次ランクコード８,0),
//                    NVL(年次ランクコード９,0),
//                    NVL(月次ランクコード００１,0),
//                    NVL(月次ランクコード００２,0),
//                    NVL(月次ランクコード００３,0),
//                    NVL(月次ランクコード００４,0),
//                    NVL(月次ランクコード００５,0),
//                    NVL(月次ランクコード００６,0),
//                    NVL(月次ランクコード００７,0),
//                    NVL(月次ランクコード００８,0),
//                    NVL(月次ランクコード００９,0),
//                    NVL(月次ランクコード０１０,0),
//                    NVL(月次ランクコード０１１,0),
//                    NVL(月次ランクコード０１２,0),
//                    NVL(月次ランクコード１０１,0),
//                    NVL(月次ランクコード１０２,0),
//                    NVL(月次ランクコード１０３,0),
//                    NVL(月次ランクコード１０４,0),
//                    NVL(月次ランクコード１０５,0),
//                    NVL(月次ランクコード１０６,0),
//                    NVL(月次ランクコード１０７,0),
//                    NVL(月次ランクコード１０８,0),
//                    NVL(月次ランクコード１０９,0),
//                    NVL(月次ランクコード１１０,0),
//                    NVL(月次ランクコード１１１,0),
//                    NVL(月次ランクコード１１２,0),
//                    NVL(サークルＩＤ１,0),
//                    NVL(サークルＩＤ２,0),
//                    NVL(サークルＩＤ３,0),
//                    NVL(サークルＩＤ４,0),
//                    NVL(サークルＩＤ５,0),
//                    NVL(在籍開始年月,0),
//                    NVL(出産クーポン発行可否１,0),
//                    NVL(出産クーポン発行可否２,0),
//                    NVL(出産クーポン発行可否３,0),
//                    NVL(社員区分,0),
//                    NVL(ポータル会員フラグ,0),
//                    NVL(ＥＣ会員フラグ,0),
//                    NVL(モバイル会員フラグ,0),
//                    NVL(電話番号登録フラグ,0),
//                    NVL(静態取込済みフラグ,0),
//                    NVL(家族ＩＤ,0),
//                    NVL(フラグ１,0),
//                    NVL(フラグ２,0),
//                    NVL(フラグ３,0),
//                    NVL(フラグ４,0),
//                    NVL(フラグ５,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(サンプリング要否フラグ,0),
//                    NVL(コーポレートＩＤ選択メール種別,0),
//                    NVL(デジタル会員ＥＣ入会フラグ,0),
//                    to_number(to_char(nvl(デジタル会員ＥＣ入会更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(デジタル会員アプリ入会フラグ,0),
//                    to_number(to_char(nvl(デジタル会員アプリ入会更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(旧入会年月日,0)
//            INTO  :h_ms_kokyaku_seido_buff.tanjo_m,
//                            :h_ms_kokyaku_seido_buff.entry,
//                            :h_ms_kokyaku_seido_buff.senior,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_0,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_1,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_2,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_3,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_4,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_5,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_6,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_7,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_8,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_9,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_001,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_002,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_003,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_004,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_005,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_006,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_007,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_008,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_009,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_010,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_011,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_012,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_101,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_102,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_103,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_104,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_105,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_106,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_107,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_108,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_109,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_110,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_111,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_112,
//                            :h_ms_kokyaku_seido_buff.circle_id_1,
//                            :h_ms_kokyaku_seido_buff.circle_id_2,
//                            :h_ms_kokyaku_seido_buff.circle_id_3,
//                            :h_ms_kokyaku_seido_buff.circle_id_4,
//                            :h_ms_kokyaku_seido_buff.circle_id_5,
//                            :h_ms_kokyaku_seido_buff.zaiseki_kaishi_ym,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_1,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_2,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_3,
//                            :h_ms_kokyaku_seido_buff.shain_kbn,
//                            :h_ms_kokyaku_seido_buff.portal_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.ec_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.mobile_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.denwa_no_toroku_flg,
//                            :h_ms_kokyaku_seido_buff.setai_torikomizumi_flg,
//                            :h_ms_kokyaku_seido_buff.kazoku_id,
//                            :h_ms_kokyaku_seido_buff.flg_1,
//                            :h_ms_kokyaku_seido_buff.flg_2,
//                            :h_ms_kokyaku_seido_buff.flg_3,
//                            :h_ms_kokyaku_seido_buff.flg_4,
//                            :h_ms_kokyaku_seido_buff.flg_5,
//                            :h_ms_kokyaku_seido_buff.sagyo_kigyo_cd,
//                            :h_ms_kokyaku_seido_buff.sagyosha_id,
//                            :h_ms_kokyaku_seido_buff.sagyo_ymd,
//                            :h_ms_kokyaku_seido_buff.sagyo_hms,
//                            :h_ms_kokyaku_seido_buff.batch_koshin_ymd,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_ymd,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_ymdhms,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_programid,
//                            :h_ms_kokyaku_seido_buff.sampling_yohi_flg,
//                            :h_ms_kokyaku_seido_buff.corporate_id_sentaku_mail_shubetsu,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_flg,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_ymdhms,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_flg,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_ymdhms,
//                            :h_ms_kokyaku_seido_buff.kyu_nyukai_ymd
//            FROM  MS顧客制度情報
//            WHERE 顧客番号       = :h_ms_kokyaku_seido_buff.kokyaku_no;
            sqlca.restAndExecute(h_ms_kokyaku_seido_buff.kokyaku_no);
        } else {
            /* 顧客制度データベース以外に接続している場合 */
            /* 休眠フラグは参照不可なので０固定とする */
            sqlca.sql.arr = "SELECT NVL(誕生月,0)," +
                    "            NVL(エントリー,0)," +
                    "                    NVL(シニア,0)," +
                    "                    NVL(年次ランクコード０,0)," +
                    "                    NVL(年次ランクコード１,0)," +
                    "                    NVL(年次ランクコード２,0)," +
                    "                    NVL(年次ランクコード３,0)," +
                    "                    NVL(年次ランクコード４,0)," +
                    "                    NVL(年次ランクコード５,0)," +
                    "                    NVL(年次ランクコード６,0)," +
                    "                    NVL(年次ランクコード７,0)," +
                    "                    NVL(年次ランクコード８,0)," +
                    "                    NVL(年次ランクコード９,0)," +
                    "                    NVL(月次ランクコード００１,0)," +
                    "                    NVL(月次ランクコード００２,0)," +
                    "                    NVL(月次ランクコード００３,0)," +
                    "                    NVL(月次ランクコード００４,0)," +
                    "                    NVL(月次ランクコード００５,0)," +
                    "                    NVL(月次ランクコード００６,0)," +
                    "                    NVL(月次ランクコード００７,0)," +
                    "                    NVL(月次ランクコード００８,0)," +
                    "                    NVL(月次ランクコード００９,0)," +
                    "                    NVL(月次ランクコード０１０,0)," +
                    "                    NVL(月次ランクコード０１１,0)," +
                    "                    NVL(月次ランクコード０１２,0)," +
                    "                    NVL(月次ランクコード１０１,0)," +
                    "                    NVL(月次ランクコード１０２,0)," +
                    "                    NVL(月次ランクコード１０３,0)," +
                    "                    NVL(月次ランクコード１０４,0)," +
                    "                    NVL(月次ランクコード１０５,0)," +
                    "                    NVL(月次ランクコード１０６,0)," +
                    "                    NVL(月次ランクコード１０７,0)," +
                    "                    NVL(月次ランクコード１０８,0)," +
                    "                    NVL(月次ランクコード１０９,0)," +
                    "                    NVL(月次ランクコード１１０,0)," +
                    "                    NVL(月次ランクコード１１１,0)," +
                    "                    NVL(月次ランクコード１１２,0)," +
                    "                    NVL(サークルＩＤ１,0)," +
                    "                    NVL(サークルＩＤ２,0)," +
                    "                    NVL(サークルＩＤ３,0)," +
                    "                    NVL(サークルＩＤ４,0)," +
                    "                    NVL(サークルＩＤ５,0)," +
                    "                    NVL(在籍開始年月,0)," +
                    "                    NVL(出産クーポン発行可否１,0)," +
                    "                    NVL(出産クーポン発行可否２,0)," +
                    "                    NVL(出産クーポン発行可否３,0)," +
                    "                    NVL(社員区分,0)," +
                    "                    NVL(ポータル会員フラグ,0)," +
                    "                    NVL(ＥＣ会員フラグ,0)," +
                    "                    NVL(モバイル会員フラグ,0)," +
                    "                    NVL(電話番号登録フラグ,0)," +
                    "                    NVL(静態取込済みフラグ,0)," +
                    "                    NVL(家族ＩＤ,0)," +
                    "                    NVL(フラグ１,0)," +
                    "                    NVL(フラグ２,0)," +
                    "                    NVL(フラグ３,0)," +
                    "                    NVL(フラグ４,0)," +
                    "                    NVL(フラグ５,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(サンプリング要否フラグ,0)," +
                    "                    NVL(コーポレートＩＤ選択メール種別,0)," +
                    "                    NVL(デジタル会員ＥＣ入会フラグ,0)," +
                    "                    to_number(to_char(coalesce(デジタル会員ＥＣ入会更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(デジタル会員アプリ入会フラグ,0)," +
                    "                    to_number(to_char(coalesce(デジタル会員アプリ入会更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(旧入会年月日,0)" +
                    "            FROM  MS顧客制度情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(誕生月,0),
//            NVL(エントリー,0),
//                    NVL(シニア,0),
//                    NVL(年次ランクコード０,0),
//                    NVL(年次ランクコード１,0),
//                    NVL(年次ランクコード２,0),
//                    NVL(年次ランクコード３,0),
//                    NVL(年次ランクコード４,0),
//                    NVL(年次ランクコード５,0),
//                    NVL(年次ランクコード６,0),
//                    NVL(年次ランクコード７,0),
//                    NVL(年次ランクコード８,0),
//                    NVL(年次ランクコード９,0),
//                    NVL(月次ランクコード００１,0),
//                    NVL(月次ランクコード００２,0),
//                    NVL(月次ランクコード００３,0),
//                    NVL(月次ランクコード００４,0),
//                    NVL(月次ランクコード００５,0),
//                    NVL(月次ランクコード００６,0),
//                    NVL(月次ランクコード００７,0),
//                    NVL(月次ランクコード００８,0),
//                    NVL(月次ランクコード００９,0),
//                    NVL(月次ランクコード０１０,0),
//                    NVL(月次ランクコード０１１,0),
//                    NVL(月次ランクコード０１２,0),
//                    NVL(月次ランクコード１０１,0),
//                    NVL(月次ランクコード１０２,0),
//                    NVL(月次ランクコード１０３,0),
//                    NVL(月次ランクコード１０４,0),
//                    NVL(月次ランクコード１０５,0),
//                    NVL(月次ランクコード１０６,0),
//                    NVL(月次ランクコード１０７,0),
//                    NVL(月次ランクコード１０８,0),
//                    NVL(月次ランクコード１０９,0),
//                    NVL(月次ランクコード１１０,0),
//                    NVL(月次ランクコード１１１,0),
//                    NVL(月次ランクコード１１２,0),
//                    NVL(サークルＩＤ１,0),
//                    NVL(サークルＩＤ２,0),
//                    NVL(サークルＩＤ３,0),
//                    NVL(サークルＩＤ４,0),
//                    NVL(サークルＩＤ５,0),
//                    NVL(在籍開始年月,0),
//                    NVL(出産クーポン発行可否１,0),
//                    NVL(出産クーポン発行可否２,0),
//                    NVL(出産クーポン発行可否３,0),
//                    NVL(社員区分,0),
//                    NVL(ポータル会員フラグ,0),
//                    NVL(ＥＣ会員フラグ,0),
//                    NVL(モバイル会員フラグ,0),
//                    NVL(電話番号登録フラグ,0),
//                    NVL(静態取込済みフラグ,0),
//                    NVL(家族ＩＤ,0),
//                    NVL(フラグ１,0),
//                    NVL(フラグ２,0),
//                    NVL(フラグ３,0),
//                    NVL(フラグ４,0),
//                    NVL(フラグ５,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(サンプリング要否フラグ,0),
//                    NVL(コーポレートＩＤ選択メール種別,0),
//                    NVL(デジタル会員ＥＣ入会フラグ,0),
//                    to_number(to_char(nvl(デジタル会員ＥＣ入会更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(デジタル会員アプリ入会フラグ,0),
//                    to_number(to_char(nvl(デジタル会員アプリ入会更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(旧入会年月日,0)
//                      INTO  :h_ms_kokyaku_seido_buff.tanjo_m,
//                            :h_ms_kokyaku_seido_buff.entry,
//                            :h_ms_kokyaku_seido_buff.senior,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_0,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_1,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_2,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_3,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_4,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_5,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_6,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_7,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_8,
//                            :h_ms_kokyaku_seido_buff.nenji_rank_cd_9,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_001,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_002,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_003,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_004,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_005,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_006,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_007,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_008,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_009,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_010,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_011,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_012,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_101,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_102,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_103,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_104,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_105,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_106,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_107,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_108,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_109,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_110,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_111,
//                            :h_ms_kokyaku_seido_buff.getuji_rank_cd_112,
//                            :h_ms_kokyaku_seido_buff.circle_id_1,
//                            :h_ms_kokyaku_seido_buff.circle_id_2,
//                            :h_ms_kokyaku_seido_buff.circle_id_3,
//                            :h_ms_kokyaku_seido_buff.circle_id_4,
//                            :h_ms_kokyaku_seido_buff.circle_id_5,
//                            :h_ms_kokyaku_seido_buff.zaiseki_kaishi_ym,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_1,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_2,
//                            :h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_3,
//                            :h_ms_kokyaku_seido_buff.shain_kbn,
//                            :h_ms_kokyaku_seido_buff.portal_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.ec_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.mobile_kaiin_flg,
//                            :h_ms_kokyaku_seido_buff.denwa_no_toroku_flg,
//                            :h_ms_kokyaku_seido_buff.setai_torikomizumi_flg,
//                            :h_ms_kokyaku_seido_buff.kazoku_id,
//                            :h_ms_kokyaku_seido_buff.flg_1,
//                            :h_ms_kokyaku_seido_buff.flg_2,
//                            :h_ms_kokyaku_seido_buff.flg_3,
//                            :h_ms_kokyaku_seido_buff.flg_4,
//                            :h_ms_kokyaku_seido_buff.flg_5,
//                            :h_ms_kokyaku_seido_buff.sagyo_kigyo_cd,
//                            :h_ms_kokyaku_seido_buff.sagyosha_id,
//                            :h_ms_kokyaku_seido_buff.sagyo_ymd,
//                            :h_ms_kokyaku_seido_buff.sagyo_hms,
//                            :h_ms_kokyaku_seido_buff.batch_koshin_ymd,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_ymd,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_ymdhms,
//                            :h_ms_kokyaku_seido_buff.saishu_koshin_programid,
//                            :h_ms_kokyaku_seido_buff.sampling_yohi_flg,
//                            :h_ms_kokyaku_seido_buff.corporate_id_sentaku_mail_shubetsu,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_flg,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_ymdhms,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_flg,
//                            :h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_ymdhms,
//                            :h_ms_kokyaku_seido_buff.kyu_nyukai_ymd
//            FROM  MS顧客制度情報@CMSD
//            WHERE 顧客番号       = :h_ms_kokyaku_seido_buff.kokyaku_no;
            sqlca.restAndExecute(h_ms_kokyaku_seido_buff.kokyaku_no);
        }
        sqlca.fetch();
        /* 2023/05/24 MCCMPH2 MOD END */
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s",
                    h_ms_kokyaku_seido_buff.kokyaku_no.arr);

            APLOG_WT("904", 0, null, "SELECT", (long) sqlca.sqlcode,
                    "MS顧客制度情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        sqlca.recData(h_ms_kokyaku_seido_buff.tanjo_m,
                h_ms_kokyaku_seido_buff.entry,
                h_ms_kokyaku_seido_buff.senior,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_0,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_1,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_2,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_3,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_4,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_5,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_6,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_7,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_8,
                h_ms_kokyaku_seido_buff.nenji_rank_cd_9,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_001,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_002,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_003,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_004,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_005,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_006,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_007,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_008,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_009,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_010,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_011,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_012,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_101,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_102,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_103,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_104,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_105,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_106,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_107,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_108,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_109,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_110,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_111,
                h_ms_kokyaku_seido_buff.getuji_rank_cd_112,
                h_ms_kokyaku_seido_buff.circle_id_1,
                h_ms_kokyaku_seido_buff.circle_id_2,
                h_ms_kokyaku_seido_buff.circle_id_3,
                h_ms_kokyaku_seido_buff.circle_id_4,
                h_ms_kokyaku_seido_buff.circle_id_5,
                h_ms_kokyaku_seido_buff.zaiseki_kaishi_ym,
                h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_1,
                h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_2,
                h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_3,
                h_ms_kokyaku_seido_buff.shain_kbn,
                h_ms_kokyaku_seido_buff.portal_kaiin_flg,
                h_ms_kokyaku_seido_buff.ec_kaiin_flg,
                h_ms_kokyaku_seido_buff.mobile_kaiin_flg,
                h_ms_kokyaku_seido_buff.denwa_no_toroku_flg,
                h_ms_kokyaku_seido_buff.setai_torikomizumi_flg,
                h_ms_kokyaku_seido_buff.kazoku_id,
                h_ms_kokyaku_seido_buff.flg_1,
                h_ms_kokyaku_seido_buff.flg_2,
                h_ms_kokyaku_seido_buff.flg_3,
                h_ms_kokyaku_seido_buff.flg_4,
                h_ms_kokyaku_seido_buff.flg_5,
                h_ms_kokyaku_seido_buff.sagyo_kigyo_cd,
                h_ms_kokyaku_seido_buff.sagyosha_id,
                h_ms_kokyaku_seido_buff.sagyo_ymd,
                h_ms_kokyaku_seido_buff.sagyo_hms,
                h_ms_kokyaku_seido_buff.batch_koshin_ymd,
                h_ms_kokyaku_seido_buff.saishu_koshin_ymd,
                h_ms_kokyaku_seido_buff.saishu_koshin_ymdhms,
                h_ms_kokyaku_seido_buff.saishu_koshin_programid,
                h_ms_kokyaku_seido_buff.sampling_yohi_flg,
                h_ms_kokyaku_seido_buff.corporate_id_sentaku_mail_shubetsu,
                h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_flg,
                h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_ymdhms,
                h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_flg,
                h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_ymdhms,
                h_ms_kokyaku_seido_buff.kyu_nyukai_ymd);
        /* 出力引数の設定 */
        csMaster.tanjo_m = h_ms_kokyaku_seido_buff.tanjo_m;                        /* 誕生月                   */
        csMaster.entry = h_ms_kokyaku_seido_buff.entry;                          /* エントリー               */
        csMaster.senior = h_ms_kokyaku_seido_buff.senior;                         /* シニア                   */
        csMaster.nenji_rank_cd_0 = h_ms_kokyaku_seido_buff.nenji_rank_cd_0;                /* 年次ランクコード０       */
        csMaster.nenji_rank_cd_1 = h_ms_kokyaku_seido_buff.nenji_rank_cd_1;                /* 年次ランクコード１       */
        csMaster.nenji_rank_cd_2 = h_ms_kokyaku_seido_buff.nenji_rank_cd_2;                /* 年次ランクコード２       */
        csMaster.nenji_rank_cd_3 = h_ms_kokyaku_seido_buff.nenji_rank_cd_3;                /* 年次ランクコード３       */
        csMaster.nenji_rank_cd_4 = h_ms_kokyaku_seido_buff.nenji_rank_cd_4;                /* 年次ランクコード４       */
        csMaster.nenji_rank_cd_5 = h_ms_kokyaku_seido_buff.nenji_rank_cd_5;                /* 年次ランクコード５       */
        csMaster.nenji_rank_cd_6 = h_ms_kokyaku_seido_buff.nenji_rank_cd_6;                /* 年次ランクコード６       */
        csMaster.nenji_rank_cd_7 = h_ms_kokyaku_seido_buff.nenji_rank_cd_7;                /* 年次ランクコード７       */
        csMaster.nenji_rank_cd_8 = h_ms_kokyaku_seido_buff.nenji_rank_cd_8;                /* 年次ランクコード８       */
        csMaster.nenji_rank_cd_9 = h_ms_kokyaku_seido_buff.nenji_rank_cd_9;                /* 年次ランクコード９       */
        csMaster.getuji_rank_cd_001 = h_ms_kokyaku_seido_buff.getuji_rank_cd_001;             /* 月次ランクコード００１   */
        csMaster.getuji_rank_cd_002 = h_ms_kokyaku_seido_buff.getuji_rank_cd_002;             /* 月次ランクコード００２   */
        csMaster.getuji_rank_cd_003 = h_ms_kokyaku_seido_buff.getuji_rank_cd_003;             /* 月次ランクコード００３   */
        csMaster.getuji_rank_cd_004 = h_ms_kokyaku_seido_buff.getuji_rank_cd_004;             /* 月次ランクコード００４   */
        csMaster.getuji_rank_cd_005 = h_ms_kokyaku_seido_buff.getuji_rank_cd_005;             /* 月次ランクコード００５   */
        csMaster.getuji_rank_cd_006 = h_ms_kokyaku_seido_buff.getuji_rank_cd_006;             /* 月次ランクコード００６   */
        csMaster.getuji_rank_cd_007 = h_ms_kokyaku_seido_buff.getuji_rank_cd_007;             /* 月次ランクコード００７   */
        csMaster.getuji_rank_cd_008 = h_ms_kokyaku_seido_buff.getuji_rank_cd_008;             /* 月次ランクコード００８   */
        csMaster.getuji_rank_cd_009 = h_ms_kokyaku_seido_buff.getuji_rank_cd_009;             /* 月次ランクコード００９   */
        csMaster.getuji_rank_cd_010 = h_ms_kokyaku_seido_buff.getuji_rank_cd_010;             /* 月次ランクコード０１０   */
        csMaster.getuji_rank_cd_011 = h_ms_kokyaku_seido_buff.getuji_rank_cd_011;             /* 月次ランクコード０１１   */
        csMaster.getuji_rank_cd_012 = h_ms_kokyaku_seido_buff.getuji_rank_cd_012;             /* 月次ランクコード０１２   */
        csMaster.getuji_rank_cd_101 = h_ms_kokyaku_seido_buff.getuji_rank_cd_101;             /* 月次ランクコード１０１   */
        csMaster.getuji_rank_cd_102 = h_ms_kokyaku_seido_buff.getuji_rank_cd_102;             /* 月次ランクコード１０２   */
        csMaster.getuji_rank_cd_103 = h_ms_kokyaku_seido_buff.getuji_rank_cd_103;             /* 月次ランクコード１０３   */
        csMaster.getuji_rank_cd_104 = h_ms_kokyaku_seido_buff.getuji_rank_cd_104;             /* 月次ランクコード１０４   */
        csMaster.getuji_rank_cd_105 = h_ms_kokyaku_seido_buff.getuji_rank_cd_105;             /* 月次ランクコード１０５   */
        csMaster.getuji_rank_cd_106 = h_ms_kokyaku_seido_buff.getuji_rank_cd_106;             /* 月次ランクコード１０６   */
        csMaster.getuji_rank_cd_107 = h_ms_kokyaku_seido_buff.getuji_rank_cd_107;             /* 月次ランクコード１０７   */
        csMaster.getuji_rank_cd_108 = h_ms_kokyaku_seido_buff.getuji_rank_cd_108;             /* 月次ランクコード１０８   */
        csMaster.getuji_rank_cd_109 = h_ms_kokyaku_seido_buff.getuji_rank_cd_109;             /* 月次ランクコード１０９   */
        csMaster.getuji_rank_cd_110 = h_ms_kokyaku_seido_buff.getuji_rank_cd_110;             /* 月次ランクコード１１０   */
        csMaster.getuji_rank_cd_111 = h_ms_kokyaku_seido_buff.getuji_rank_cd_111;             /* 月次ランクコード１１１   */
        csMaster.getuji_rank_cd_112 = h_ms_kokyaku_seido_buff.getuji_rank_cd_112;             /* 月次ランクコード１１２   */
        csMaster.circle_id_1 = h_ms_kokyaku_seido_buff.circle_id_1;                    /* サークルＩＤ１           */
        csMaster.circle_id_2 = h_ms_kokyaku_seido_buff.circle_id_2;                    /* サークルＩＤ２           */
        csMaster.circle_id_3 = h_ms_kokyaku_seido_buff.circle_id_3;                    /* サークルＩＤ３           */
        csMaster.circle_id_4 = h_ms_kokyaku_seido_buff.circle_id_4;                    /* サークルＩＤ４           */
        csMaster.circle_id_5 = h_ms_kokyaku_seido_buff.circle_id_5;                    /* サークルＩＤ５           */
        csMaster.zaiseki_kaishi_ym = h_ms_kokyaku_seido_buff.zaiseki_kaishi_ym;              /* 在籍開始年月             */
        csMaster.shussan_coupon_hakko_flg_1 = h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_1;     /* 出産クーポン発行可否１   */
        csMaster.shussan_coupon_hakko_flg_2 = h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_2;     /* 出産クーポン発行可否２   */
        csMaster.shussan_coupon_hakko_flg_3 = h_ms_kokyaku_seido_buff.shussan_coupon_hakko_flg_3;     /* 出産クーポン発行可否３   */
        csMaster.shain_kbn = h_ms_kokyaku_seido_buff.shain_kbn;                      /* 社員区分                 */
        csMaster.portal_kaiin_flg = h_ms_kokyaku_seido_buff.portal_kaiin_flg;               /* ポータル会員フラグ       */
        csMaster.ec_kaiin_flg = h_ms_kokyaku_seido_buff.ec_kaiin_flg;                   /* ＥＣ会員フラグ           */
        csMaster.mobile_kaiin_flg = h_ms_kokyaku_seido_buff.mobile_kaiin_flg;               /* モバイル会員フラグ       */
        csMaster.denwa_no_toroku_flg = h_ms_kokyaku_seido_buff.denwa_no_toroku_flg;            /* 電話番号登録フラグ       */
        csMaster.setai_torikomizumi_flg = h_ms_kokyaku_seido_buff.setai_torikomizumi_flg;         /* 静態取込済みフラグ       */
        csMaster.kazoku_id = h_ms_kokyaku_seido_buff.kazoku_id;                      /* 家族ＩＤ                 */
        csMaster.flg_1 = h_ms_kokyaku_seido_buff.flg_1;                          /* フラグ１                 */
        csMaster.flg_2 = h_ms_kokyaku_seido_buff.flg_2;                          /* フラグ２                 */
        csMaster.flg_3 = h_ms_kokyaku_seido_buff.flg_3;                          /* フラグ３                 */
        csMaster.flg_4 = h_ms_kokyaku_seido_buff.flg_4;                          /* フラグ４                 */
        csMaster.flg_5 = h_ms_kokyaku_seido_buff.flg_5;                          /* フラグ５                 */
        csMaster.sagyo_kigyo_cd = h_ms_kokyaku_seido_buff.sagyo_kigyo_cd;                 /* 作業企業コード           */
        csMaster.sagyosha_id = h_ms_kokyaku_seido_buff.sagyosha_id;                    /* 作業者ＩＤ               */
        csMaster.sagyo_ymd = h_ms_kokyaku_seido_buff.sagyo_ymd;                      /* 作業年月日               */
        csMaster.sagyo_hms = h_ms_kokyaku_seido_buff.sagyo_hms;                      /* 作業時刻                 */
        csMaster.batch_koshin_ymd = h_ms_kokyaku_seido_buff.batch_koshin_ymd;               /* バッチ更新日             */
        csMaster.saishu_koshin_ymd = h_ms_kokyaku_seido_buff.saishu_koshin_ymd;              /* 最終更新日               */
        csMaster.saishu_koshin_ymdhms = h_ms_kokyaku_seido_buff.saishu_koshin_ymdhms;           /* 最終更新日時             */
        strcpy(csMaster.saishu_koshin_programid,
                h_ms_kokyaku_seido_buff.saishu_koshin_programid);                                           /* 最終更新プログラムＩＤ   */
        /* 2023/05/24 MCCMPH2 ADD START */
        csMaster.sampling_yohi_flg = h_ms_kokyaku_seido_buff.sampling_yohi_flg;                                    /* サンプリング要否フラグ                         */
        csMaster.corporate_id_sentaku_mail_shubetsu = h_ms_kokyaku_seido_buff.corporate_id_sentaku_mail_shubetsu;   /* コーポレートＩＤ選択メール種別                 */
        csMaster.dejitalu_ec_kaiin_nyukai_flg = h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_flg;               /* デジタル会員ＥＣ入会フラグ                     */
        csMaster.dejitalu_ec_kaiin_nyukai_ymdhms = h_ms_kokyaku_seido_buff.dejitalu_ec_kaiin_nyukai_ymdhms;         /* デジタル会員ＥＣ入会更新日時                   */
        csMaster.dejitalu_ap_kaiin_nyukai_flg = h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_flg;               /* デジタル会員アプリ入会フラグ                   */
        csMaster.dejitalu_ap_kaiin_nyukai_ymdhms = h_ms_kokyaku_seido_buff.dejitalu_ap_kaiin_nyukai_ymdhms;         /* デジタル会員アプリ入会更新日時                 */
        csMaster.kyu_nyukai_ymd = h_ms_kokyaku_seido_buff.kyu_nyukai_ymd;                                           /* 旧入会年月日                                   */
        /* 2023/05/24 MCCMPH2 ADD END */

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_GetcsMaster : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_GetcsMaster Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetCzMaster                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetCzMaster(CZ_MASTER  *czMaster, int status.arr)                  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              顧客属性情報取得処理                                          */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              CZ_MASTER * czMaster ： 顧客属性情報構造体取得パラメータ      */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetCzMaster(MM_KOKYAKU_ZOKUSE_INFO_TBL czMaster, IntegerDto status) {
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        MM_KOKYAKU_ZOKUSE_INFO_TBL h_mm_kokyaku_zokuse_info_buff = new MM_KOKYAKU_ZOKUSE_INFO_TBL(); /* MM顧客属性情報バッファ */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_GetczMaster : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (czMaster == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetczMaster : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetczMaster : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }



        /* 出力エリアのクリア */
        czMaster.kyumin_flg.arr = 0;                                                    /* 休眠フラグ            */
        strcpy(czMaster.yubin_no, "          ");              /* 郵便番号              */
        strcpy(czMaster.yubin_no_cd, "                       "); /* 郵便番号コード        */
        strcpy(czMaster.jusho_1, "          "); /* 住所１ */
        strcpy(czMaster.jusho_2, "                                                                                "); /* 住所２ */
        strcpy(czMaster.jusho_3, "                                                                                "); /* 住所３ */
        strcpy(czMaster.denwa_no_1, "               ");         /* 電話番号１            */
        strcpy(czMaster.denwa_no_2, "               ");         /* 電話番号２            */
        strcpy(czMaster.kensaku_denwa_no_1, "               ");         /* 検索電話番号１        */
        strcpy(czMaster.kensaku_denwa_no_2, "               ");         /* 検索電話番号２        */
        strcpy(czMaster.email_address_1, "                                                            "); /* Ｅメールアドレス１     */
        strcpy(czMaster.email_address_2, "                                                            "); /* Ｅメールアドレス２     */
        strcpy(czMaster.denwa_no_3, "               ");         /* 電話番号３             */
        strcpy(czMaster.denwa_no_4, "               ");         /* 電話番号４             */
        strcpy(czMaster.kensaku_denwa_no_3, "               ");         /* 検索電話番号３         */
        strcpy(czMaster.kensaku_denwa_no_4, "               ");         /* 検索電話番号４         */
        strcpy(czMaster.shokugyo, "                                        "); /* 職業  */
        czMaster.kinmu_kbn.arr = 0;                                                     /* 勤務区分               */
        strcpy(czMaster.jitaku_jusho_cd, "           ");             /* 自宅住所コード         */
        czMaster.sagyo_kigyo_cd.arr = 0;                                                /* 作業企業コード         */
        czMaster.sagyosha_id.arr = 0;                                                   /* 作業者ＩＤ             */
        czMaster.sagyo_ymd.arr = 0;                                                     /* 作業年月日             */
        czMaster.sagyo_hms.arr = 0;                                                     /* 作業時刻               */
        czMaster.batch_koshin_ymd.arr = 0;                                              /* バッチ更新日           */
        czMaster.saishu_koshin_ymd.arr = 0;                                             /* 最終更新日             */
        czMaster.saishu_koshin_ymdhms.arr = 0;                                          /* 最終更新日時           */
        strcpy(czMaster.saishu_koshin_programid, "                    ");            /* 最終更新プログラムＩＤ */
        strcpy(czMaster.email_address_3, "                                                                                                    "); /* Ｅメールアドレス３     */
        strcpy(czMaster.email_address_4, "                                                                                                    "); /* Ｅメールアドレス４     */
        czMaster.todofuken_cd.arr = 0;                                                  /* 都道府県コード         */
        strcpy(czMaster.address, "                                         "); /* 住所 */

        /* ホスト変数を編集する */
        memset(h_mm_kokyaku_zokuse_info_buff, 0x00, sizeof(0));
//        memset(h_mm_kokyaku_zokuse_info_buff, 0x00, sizeof(MM_KOKYAKU_ZOKUSE_INFO_TBL));
        memcpy(h_mm_kokyaku_zokuse_info_buff.kokyaku_no, czMaster.kokyaku_no, czMaster.kokyaku_no.len);
        h_mm_kokyaku_zokuse_info_buff.kokyaku_no.len = czMaster.kokyaku_no.len;


        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_MD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_MD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        sqlca.sql = new StringDto();
        /* ＳＱＬを実行する */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客データベースに接続している場合 */
            sqlca.sql.arr = "SELECT NVL(休眠フラグ,0)," +
                    "            NVL(RPAD(郵便番号,LENGTH(郵便番号)),'          ')," +
                    "                    NVL(RPAD(郵便番号コード,LENGTH(郵便番号コード)),'                       ')," +
                    "                    NVL(RPAD(住所１,LENGTH(住所１)),'          ')," +
                    "                    NVL(RPAD(住所２,LENGTH(住所２)),'                                                                                ')," +
                    "                    NVL(RPAD(住所３,LENGTH(住所３)),'                                                                                ')," +
                    "                    NVL(RPAD(電話番号１,LENGTH(電話番号１)),'               ')," +
                    "                    NVL(RPAD(電話番号２,LENGTH(電話番号２)),'               ')," +
                    "                    NVL(RPAD(検索電話番号１,LENGTH(検索電話番号１)),'               ')," +
                    "                    NVL(RPAD(検索電話番号２,LENGTH(検索電話番号２)),'               ')," +
                    "                    NVL(RPAD(Ｅメールアドレス１,LENGTH(Ｅメールアドレス１)),'                                                            ')," +
                    "                    NVL(RPAD(Ｅメールアドレス２,LENGTH(Ｅメールアドレス２)),'                                                            ')," +
                    "                    NVL(RPAD(電話番号３,LENGTH(電話番号３)),'               ')," +
                    "                    NVL(RPAD(電話番号４,LENGTH(電話番号４)),'               ')," +
                    "                    NVL(RPAD(検索電話番号３,LENGTH(検索電話番号３)),'               ')," +
                    "                    NVL(RPAD(検索電話番号４,LENGTH(検索電話番号４)),'               ')," +
                    "                    NVL(RPAD(職業,LENGTH(職業)),'                                        ')," +
                    "                    NVL(勤務区分,0)," +
                    "                    NVL(RPAD(自宅住所コード,LENGTH(自宅住所コード)),'           ')," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(RPAD(Ｅメールアドレス３,LENGTH(Ｅメールアドレス３)),'                                                                                                    ')," +
                    "                    NVL(RPAD(Ｅメールアドレス４,LENGTH(Ｅメールアドレス４)),'                                                                                                    ')," +
                    "                    NVL(都道府県コード,0)," +
                    "                    NVL(RPAD(住所,LENGTH(住所)),'                                                                                ')" +
                    "            FROM MM顧客属性情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(休眠フラグ,0),
//            NVL(郵便番号,'          '),
//                    NVL(郵便番号コード,'                       '),
//                    NVL(住所１,'          '),
//                    NVL(住所２,'                                                                                '),
//                    NVL(住所３,'                                                                                '),
//                    NVL(電話番号１,'               '),
//                    NVL(電話番号２,'               '),
//                    NVL(検索電話番号１,'               '),
//                    NVL(検索電話番号２,'               '),
//                    NVL(Ｅメールアドレス１,'                                                            '),
//                    NVL(Ｅメールアドレス２,'                                                            '),
//                    NVL(電話番号３,'               '),
//                    NVL(電話番号４,'               '),
//                    NVL(検索電話番号３,'               '),
//                    NVL(検索電話番号４,'               '),
//                    NVL(職業,'                                        '),
//                    NVL(勤務区分,0),
//                    NVL(自宅住所コード,'           '),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(Ｅメールアドレス３,'                                                                                                    '),
//                    NVL(Ｅメールアドレス４,'                                                                                                    '),
//                    NVL(都道府県コード,0),
//                    NVL(住所,'                                                                                ')
//            INTO :h_mm_kokyaku_zokuse_info_buff.kyumin_flg,             /* 休眠フラグ               */
//                            :h_mm_kokyaku_zokuse_info_buff.yubin_no,               /* 郵便番号                 */
//                            :h_mm_kokyaku_zokuse_info_buff.yubin_no_cd,            /* 郵便番号コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_1,                /* 住所１                   */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_2,                /* 住所２                   */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_3,                /* 住所３                   */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_1,             /* 電話番号１               */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_2,             /* 電話番号２               */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_1,     /* 検索電話番号１           */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_2,     /* 検索電話番号２           */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_1,        /* Ｅメールアドレス１       */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_2,        /* Ｅメールアドレス２       */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_3,             /* 電話番号３               */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_4,             /* 電話番号４               */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_3,     /* 検索電話番号３           */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_4,     /* 検索電話番号４           */
//                            :h_mm_kokyaku_zokuse_info_buff.shokugyo,               /* 職業                     */
//                            :h_mm_kokyaku_zokuse_info_buff.kinmu_kbn,              /* 勤務区分                 */
//                            :h_mm_kokyaku_zokuse_info_buff.jitaku_jusho_cd,        /* 自宅住所コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_kigyo_cd,         /* 作業企業コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyosha_id,            /* 作業者ＩＤ               */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_ymd,              /* 作業年月日               */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_hms,              /* 作業時刻                 */
//                            :h_mm_kokyaku_zokuse_info_buff.batch_koshin_ymd,       /* バッチ更新日             */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymd,      /* 最終更新日               */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymdhms,   /* 最終更新日時             */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_programid, /* 最終更新プログラムＩＤ   */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_3,        /* Ｅメールアドレス３       */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_4,        /* Ｅメールアドレス４       */
//                            :h_mm_kokyaku_zokuse_info_buff.todofuken_cd,           /* 都道府県コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.address                 /* 住所                     */
//            FROM MM顧客属性情報
//            WHERE 顧客番号       = :h_mm_kokyaku_zokuse_info_buff.kokyaku_no;
            sqlca.restAndExecute(h_mm_kokyaku_zokuse_info_buff.kokyaku_no);
        } else {
            /* 顧客データベース以外に接続している場合 */
            sqlca.sql.arr = " SELECT NVL(休眠フラグ,0)," +
                    "            NVL(RPAD(郵便番号,LENGTH(郵便番号)),'          ')," +
                    "                    NVL(RPAD(郵便番号コード,LENGTH(郵便番号コード)),'                       ')," +
                    "                    NVL(RPAD(住所１,LENGTH(住所１)),'          ')," +
                    "                    NVL(RPAD(住所２,LENGTH(住所２)),'                                                                                ')," +
                    "                    NVL(RPAD(住所３,LENGTH(住所３)),'                                                                                ')," +
                    "                    NVL(RPAD(電話番号１,LENGTH(電話番号１)),'               ')," +
                    "                    NVL(RPAD(電話番号２,LENGTH(電話番号２)),'               ')," +
                    "                    NVL(RPAD(検索電話番号１,LENGTH(検索電話番号１)),'               ')," +
                    "                    NVL(RPAD(検索電話番号２,LENGTH(検索電話番号２)),'               ')," +
                    "                    NVL(RPAD(Ｅメールアドレス１,LENGTH(Ｅメールアドレス１)),'                                                            ')," +
                    "                    NVL(RPAD(Ｅメールアドレス２,LENGTH(Ｅメールアドレス２)),'                                                            ')," +
                    "                    NVL(RPAD(電話番号３,LENGTH(電話番号３)),'               ')," +
                    "                    NVL(RPAD(電話番号４,LENGTH(電話番号４)),'               ')," +
                    "                    NVL(RPAD(検索電話番号３,LENGTH(検索電話番号３)),'               ')," +
                    "                    NVL(RPAD(検索電話番号４,LENGTH(検索電話番号４)),'               ')," +
                    "                    NVL(RPAD(職業,LENGTH(職業)),'                                        ')," +
                    "                    NVL(勤務区分,0)," +
                    "                    NVL(RPAD(自宅住所コード,LENGTH(自宅住所コード)),'           ')," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')," +
                    "                    NVL(RPAD(Ｅメールアドレス３,LENGTH(Ｅメールアドレス３)),'                                                                                                    ')," +
                    "                    NVL(RPAD(Ｅメールアドレス４,LENGTH(Ｅメールアドレス４)),'                                                                                                    ')," +
                    "                    NVL(都道府県コード,0)," +
                    "                    NVL(RPAD(住所,LENGTH(住所)),'                                                                                ')" +
                    "            FROM MM顧客属性情報" +
                    "            WHERE 顧客番号       = ?";
//            EXEC SQL SELECT NVL(休眠フラグ,0),
//            NVL(郵便番号,'          '),
//                    NVL(郵便番号コード,'                       '),
//                    NVL(住所１,'          '),
//                    NVL(住所２,'                                                                                '),
//                    NVL(住所３,'                                                                                '),
//                    NVL(電話番号１,'               '),
//                    NVL(電話番号２,'               '),
//                    NVL(検索電話番号１,'               '),
//                    NVL(検索電話番号２,'               '),
//                    NVL(Ｅメールアドレス１,'                                                            '),
//                    NVL(Ｅメールアドレス２,'                                                            '),
//                    NVL(電話番号３,'               '),
//                    NVL(電話番号４,'               '),
//                    NVL(検索電話番号３,'               '),
//                    NVL(検索電話番号４,'               '),
//                    NVL(職業,'                                        '),
//                    NVL(勤務区分,0),
//                    NVL(自宅住所コード,'           '),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(nvl(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    '),
//                    NVL(Ｅメールアドレス３,'                                                                                                    '),
//                    NVL(Ｅメールアドレス４,'                                                                                                    '),
//                    NVL(都道府県コード,0),
//                    NVL(住所,'                                                                                ')
//                       INTO :h_mm_kokyaku_zokuse_info_buff.kyumin_flg,             /* 休眠フラグ               */
//                            :h_mm_kokyaku_zokuse_info_buff.yubin_no,               /* 郵便番号                 */
//                            :h_mm_kokyaku_zokuse_info_buff.yubin_no_cd,            /* 郵便番号コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_1,                /* 住所１                   */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_2,                /* 住所２                   */
//                            :h_mm_kokyaku_zokuse_info_buff.jusho_3,                /* 住所３                   */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_1,             /* 電話番号１               */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_2,             /* 電話番号２               */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_1,     /* 検索電話番号１           */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_2,     /* 検索電話番号２           */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_1,        /* Ｅメールアドレス１       */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_2,        /* Ｅメールアドレス２       */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_3,             /* 電話番号３               */
//                            :h_mm_kokyaku_zokuse_info_buff.denwa_no_4,             /* 電話番号４               */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_3,     /* 検索電話番号３           */
//                            :h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_4,     /* 検索電話番号４           */
//                            :h_mm_kokyaku_zokuse_info_buff.shokugyo,               /* 職業                     */
//                            :h_mm_kokyaku_zokuse_info_buff.kinmu_kbn,              /* 勤務区分                 */
//                            :h_mm_kokyaku_zokuse_info_buff.jitaku_jusho_cd,        /* 自宅住所コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_kigyo_cd,         /* 作業企業コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyosha_id,            /* 作業者ＩＤ               */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_ymd,              /* 作業年月日               */
//                            :h_mm_kokyaku_zokuse_info_buff.sagyo_hms,              /* 作業時刻                 */
//                            :h_mm_kokyaku_zokuse_info_buff.batch_koshin_ymd,       /* バッチ更新日             */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymd,      /* 最終更新日               */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymdhms,   /* 最終更新日時             */
//                            :h_mm_kokyaku_zokuse_info_buff.saishu_koshin_programid, /* 最終更新プログラムＩＤ   */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_3,        /* Ｅメールアドレス３       */
//                            :h_mm_kokyaku_zokuse_info_buff.email_address_4,        /* Ｅメールアドレス４       */
//                            :h_mm_kokyaku_zokuse_info_buff.todofuken_cd,           /* 都道府県コード           */
//                            :h_mm_kokyaku_zokuse_info_buff.address                 /* 住所                     */
//            FROM MM顧客属性情報@CMMD
//            WHERE 顧客番号       = :h_mm_kokyaku_zokuse_info_buff.kokyaku_no;
            sqlca.restAndExecute(h_mm_kokyaku_zokuse_info_buff.kokyaku_no);
        }
        sqlca.fetch();
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s",
                    h_mm_kokyaku_zokuse_info_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "SELECT",
                    (long) sqlca.sqlcode, "MM顧客属性情報",
                    out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }
        sqlca.recData(h_mm_kokyaku_zokuse_info_buff.kyumin_flg,
                h_mm_kokyaku_zokuse_info_buff.yubin_no,
                h_mm_kokyaku_zokuse_info_buff.yubin_no_cd,
                h_mm_kokyaku_zokuse_info_buff.jusho_1,
                h_mm_kokyaku_zokuse_info_buff.jusho_2,
                h_mm_kokyaku_zokuse_info_buff.jusho_3,
                h_mm_kokyaku_zokuse_info_buff.denwa_no_1,
                h_mm_kokyaku_zokuse_info_buff.denwa_no_2,
                h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_1,
                h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_2,
                h_mm_kokyaku_zokuse_info_buff.email_address_1,
                h_mm_kokyaku_zokuse_info_buff.email_address_2,
                h_mm_kokyaku_zokuse_info_buff.denwa_no_3,
                h_mm_kokyaku_zokuse_info_buff.denwa_no_4,
                h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_3,
                h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_4,
                h_mm_kokyaku_zokuse_info_buff.shokugyo,
                h_mm_kokyaku_zokuse_info_buff.kinmu_kbn,
                h_mm_kokyaku_zokuse_info_buff.jitaku_jusho_cd,
                h_mm_kokyaku_zokuse_info_buff.sagyo_kigyo_cd,
                h_mm_kokyaku_zokuse_info_buff.sagyosha_id,
                h_mm_kokyaku_zokuse_info_buff.sagyo_ymd,
                h_mm_kokyaku_zokuse_info_buff.sagyo_hms,
                h_mm_kokyaku_zokuse_info_buff.batch_koshin_ymd,
                h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymd,
                h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymdhms,
                h_mm_kokyaku_zokuse_info_buff.saishu_koshin_programid,
                h_mm_kokyaku_zokuse_info_buff.email_address_3,
                h_mm_kokyaku_zokuse_info_buff.email_address_4,
                h_mm_kokyaku_zokuse_info_buff.todofuken_cd,
                h_mm_kokyaku_zokuse_info_buff.address);
        /* 出力引数の設定 */
        czMaster.kyumin_flg = h_mm_kokyaku_zokuse_info_buff.kyumin_flg;                    /* 休眠フラグ               */
        memcpy(czMaster.yubin_no, h_mm_kokyaku_zokuse_info_buff.yubin_no,
                sizeof(h_mm_kokyaku_zokuse_info_buff.yubin_no));                 /* 郵便番号                 */
        memcpy(czMaster.yubin_no_cd, h_mm_kokyaku_zokuse_info_buff.yubin_no_cd,
                sizeof(h_mm_kokyaku_zokuse_info_buff.yubin_no_cd));              /* 郵便番号コード           */
        memcpy(czMaster.jusho_1, h_mm_kokyaku_zokuse_info_buff.jusho_1,
                sizeof(h_mm_kokyaku_zokuse_info_buff.jusho_1));                  /* 住所１                   */
        memcpy(czMaster.jusho_2, h_mm_kokyaku_zokuse_info_buff.jusho_2,
                sizeof(h_mm_kokyaku_zokuse_info_buff.jusho_2));                  /* 住所２                   */
        memcpy(czMaster.jusho_3, h_mm_kokyaku_zokuse_info_buff.jusho_3,
                sizeof(h_mm_kokyaku_zokuse_info_buff.jusho_3));                  /* 住所３                   */
        memcpy(czMaster.denwa_no_1, h_mm_kokyaku_zokuse_info_buff.denwa_no_1,
                sizeof(h_mm_kokyaku_zokuse_info_buff.denwa_no_1));               /* 電話番号１               */
        memcpy(czMaster.denwa_no_2, h_mm_kokyaku_zokuse_info_buff.denwa_no_2,
                sizeof(h_mm_kokyaku_zokuse_info_buff.denwa_no_2));               /* 電話番号２               */
        memcpy(czMaster.kensaku_denwa_no_1, h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_1,
                sizeof(h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_1));       /* 検索電話番号１           */
        memcpy(czMaster.kensaku_denwa_no_2, h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_2,
                sizeof(h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_2));       /* 検索電話番号２           */
        memcpy(czMaster.email_address_1, h_mm_kokyaku_zokuse_info_buff.email_address_1,
                sizeof(h_mm_kokyaku_zokuse_info_buff.email_address_1));          /* Ｅメールアドレス１       */
        memcpy(czMaster.email_address_2, h_mm_kokyaku_zokuse_info_buff.email_address_2,
                sizeof(h_mm_kokyaku_zokuse_info_buff.email_address_2));          /* Ｅメールアドレス２       */
        memcpy(czMaster.denwa_no_3, h_mm_kokyaku_zokuse_info_buff.denwa_no_3,
                sizeof(h_mm_kokyaku_zokuse_info_buff.denwa_no_3));               /* 電話番号３               */
        memcpy(czMaster.denwa_no_4, h_mm_kokyaku_zokuse_info_buff.denwa_no_4,
                sizeof(h_mm_kokyaku_zokuse_info_buff.denwa_no_4));               /* 電話番号４               */
        memcpy(czMaster.kensaku_denwa_no_3, h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_3,
                sizeof(h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_3));       /* 検索電話番号３           */
        memcpy(czMaster.kensaku_denwa_no_4, h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_4,
                sizeof(h_mm_kokyaku_zokuse_info_buff.kensaku_denwa_no_4));       /* 検索電話番号４           */
        memcpy(czMaster.shokugyo, h_mm_kokyaku_zokuse_info_buff.shokugyo,
                sizeof(h_mm_kokyaku_zokuse_info_buff.shokugyo));                 /* 職業                     */
        czMaster.kinmu_kbn = h_mm_kokyaku_zokuse_info_buff.kinmu_kbn;                  /* 勤務区分                 */
        memcpy(czMaster.jitaku_jusho_cd, h_mm_kokyaku_zokuse_info_buff.jitaku_jusho_cd,
                sizeof(h_mm_kokyaku_zokuse_info_buff.jitaku_jusho_cd));          /* 自宅住所コード           */
        czMaster.sagyo_kigyo_cd = h_mm_kokyaku_zokuse_info_buff.sagyo_kigyo_cd;             /* 作業企業コード           */
        czMaster.sagyosha_id = h_mm_kokyaku_zokuse_info_buff.sagyosha_id;                /* 作業者ＩＤ               */
        czMaster.sagyo_ymd = h_mm_kokyaku_zokuse_info_buff.sagyo_ymd;                  /* 作業年月日               */
        czMaster.sagyo_hms = h_mm_kokyaku_zokuse_info_buff.sagyo_hms;                  /* 作業時刻                 */
        czMaster.batch_koshin_ymd = h_mm_kokyaku_zokuse_info_buff.batch_koshin_ymd;           /* バッチ更新日             */
        czMaster.saishu_koshin_ymd = h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymd;          /* 最終更新日               */
        czMaster.saishu_koshin_ymdhms = h_mm_kokyaku_zokuse_info_buff.saishu_koshin_ymdhms;       /* 最終更新日時             */
        strcpy(czMaster.saishu_koshin_programid, h_mm_kokyaku_zokuse_info_buff.saishu_koshin_programid); /* 最終更新プログラムＩＤ   */
        memcpy(czMaster.email_address_3, h_mm_kokyaku_zokuse_info_buff.email_address_3,
                sizeof(h_mm_kokyaku_zokuse_info_buff.email_address_3));          /* Ｅメールアドレス３       */
        memcpy(czMaster.email_address_4, h_mm_kokyaku_zokuse_info_buff.email_address_4,
                sizeof(h_mm_kokyaku_zokuse_info_buff.email_address_4));          /* Ｅメールアドレス４       */
        czMaster.todofuken_cd = h_mm_kokyaku_zokuse_info_buff.todofuken_cd;               /* 都道府県コード           */
        memcpy(czMaster.address, h_mm_kokyaku_zokuse_info_buff.address,
                sizeof(h_mm_kokyaku_zokuse_info_buff.address));                  /* 住所                    */


        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_GetczMaster : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_GetczMaster Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetCcMaster                                               */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetCcMaster( CC_MASTER  *ccMaster, int status.arr )                */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              顧客企業別属性情報取得                                        */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              CC_MASTER * ccMaster ： 顧客企業別属性情報取得パラメータ      */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetCcMaster(MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL ccMaster, IntegerDto status) {

        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL h_mm_kigyobetu_buff = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL(); /* MS顧客企業別属性情報バッファ */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_GetccMaster : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (ccMaster == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetccMaster : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetccMaster : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }


        /* 出力エリアのクリア */
        ccMaster.nyukai_ymd.arr = 0;                  /* 入会年月日             */
        ccMaster.taikai_ymd.arr = 0;                  /* 退会年月日             */
        ccMaster.tel_tome_kbn.arr = 0;                /* ＴＥＬ止め区分         */
        ccMaster.dm_tome_kbn.arr = 0;                 /* ＤＭ止め区分           */
        ccMaster.email_tome_kbn.arr = 0;              /* Ｅメール止め区分       */
        ccMaster.ketai_tel_tome_kbn.arr = 0;          /* 携帯ＴＥＬ止め区分     */
        ccMaster.ketai_email_tome_kbn.arr = 0;        /* 携帯Ｅメール止め区分   */
        ccMaster.sagyo_kigyo_cd.arr = 0;              /* 作業企業コード         */
        ccMaster.sagyosha_id.arr = 0;                 /* 作業者ＩＤ             */
        ccMaster.sagyo_ymd.arr = 0;                   /* 作業年月日             */
        ccMaster.sagyo_hms.arr = 0;                   /* 作業時刻               */
        ccMaster.batch_koshin_ymd.arr = 0;            /* バッチ更新日           */
        ccMaster.saishu_koshin_ymd.arr = 0;           /* 最終更新日             */
        ccMaster.saishu_koshin_ymdhms.arr = 0;        /* 最終更新日時           */
        strcpy(ccMaster.saishu_koshin_programid,
                "                    ");            /* 最終更新プログラムＩＤ */

        /* ホスト変数を編集する */
        memset(h_mm_kigyobetu_buff, 0x00, sizeof(0));
//        memset(h_mm_kigyobetu_buff, 0x00, sizeof(MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL));
        memcpy(h_mm_kigyobetu_buff.kokyaku_no, ccMaster.kokyaku_no,
                ccMaster.kokyaku_no.len);
        h_mm_kigyobetu_buff.kokyaku_no.len = ccMaster.kokyaku_no.len;
        h_mm_kigyobetu_buff.kigyo_cd = ccMaster.kigyo_cd;

        if (DBG_LOG) {
            C_DbgMsg("顧客番号          [%s]\n", h_mm_kigyobetu_buff.kokyaku_no.arr);
            C_DbgMsg("企業コード        [%d]\n", h_mm_kigyobetu_buff.kigyo_cd);
        }

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_MD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_MD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        sqlca.sql = new StringDto();
        /* ＳＱＬを実行する */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客データベースに接続している場合 */
            sqlca.sql.arr = "SELECT NVL(入会年月日,0)," +
                    "            NVL(退会年月日,0)," +
                    "                    NVL(ＴＥＬ止め区分,0)," +
                    "                    NVL(ＤＭ止め区分,0)," +
                    "                    NVL(Ｅメール止め区分,0)," +
                    "                    NVL(携帯ＴＥＬ止め区分,0)," +
                    "                    NVL(携帯Ｅメール止め区分,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')" +
                    "            FROM  MM顧客企業別属性情報" +
                    "            WHERE 顧客番号       = ?" +
                    "            AND 企業コード     = ?";
//            EXEC SQL SELECT NVL(入会年月日,0),
//            NVL(退会年月日,0),
//                    NVL(ＴＥＬ止め区分,0),
//                    NVL(ＤＭ止め区分,0),
//                    NVL(Ｅメール止め区分,0),
//                    NVL(携帯ＴＥＬ止め区分,0),
//                    NVL(携帯Ｅメール止め区分,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(NVL(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    ')
//            INTO :h_mm_kigyobetu_buff.nyukai_ymd,                  /* 入会年月日             */
//                            :h_mm_kigyobetu_buff.taikai_ymd,                  /* 退会年月日             */
//                            :h_mm_kigyobetu_buff.tel_tome_kbn,                /* ＴＥＬ止め区分         */
//                            :h_mm_kigyobetu_buff.dm_tome_kbn,                 /* ＤＭ止め区分           */
//                            :h_mm_kigyobetu_buff.email_tome_kbn,              /* Ｅメール止め区分       */
//                            :h_mm_kigyobetu_buff.ketai_tel_tome_kbn,          /* 携帯ＴＥＬ止め区分     */
//                            :h_mm_kigyobetu_buff.ketai_email_tome_kbn,        /* 携帯Ｅメール止め区分   */
//                            :h_mm_kigyobetu_buff.sagyo_kigyo_cd,              /* 作業企業コード         */
//                            :h_mm_kigyobetu_buff.sagyosha_id,                 /* 作業者ＩＤ             */
//                            :h_mm_kigyobetu_buff.sagyo_ymd,                   /* 作業年月日             */
//                            :h_mm_kigyobetu_buff.sagyo_hms,                   /* 作業時刻               */
//                            :h_mm_kigyobetu_buff.batch_koshin_ymd,            /* バッチ更新日           */
//                            :h_mm_kigyobetu_buff.saishu_koshin_ymd,           /* 最終更新日             */
//                            :h_mm_kigyobetu_buff.saishu_koshin_ymdhms,        /* 最終更新日時           */
//                            :h_mm_kigyobetu_buff.saishu_koshin_programid      /* 最終更新プログラムＩＤ */
//            FROM  MM顧客企業別属性情報
//            WHERE 顧客番号       = :h_mm_kigyobetu_buff.kokyaku_no
//            AND 企業コード     = :h_mm_kigyobetu_buff.kigyo_cd;
            sqlca.query(h_mm_kigyobetu_buff.kokyaku_no, h_mm_kigyobetu_buff.kigyo_cd);
        } else {
            /* 顧客データベース以外に接続している場合 */
            sqlca.sql.arr = "SELECT NVL(入会年月日,0)," +
                    "            NVL(退会年月日,0)," +
                    "                    NVL(ＴＥＬ止め区分,0)," +
                    "                    NVL(ＤＭ止め区分,0)," +
                    "                    NVL(Ｅメール止め区分,0)," +
                    "                    NVL(携帯ＴＥＬ止め区分,0)," +
                    "                    NVL(携帯Ｅメール止め区分,0)," +
                    "                    NVL(作業企業コード,0)," +
                    "                    NVL(作業者ＩＤ,0)," +
                    "                    NVL(作業年月日,0)," +
                    "                    NVL(作業時刻,0)," +
                    "                    NVL(バッチ更新日,0)," +
                    "                    NVL(最終更新日,0)," +
                    "                    to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS'))," +
                    "                    NVL(RPAD(最終更新プログラムＩＤ,LENGTH(最終更新プログラムＩＤ)),'                    ')" +
                    "            FROM  MM顧客企業別属性情報" +
                    "            WHERE 顧客番号       = ?" +
                    "            AND 企業コード     = ?";
//            EXEC SQL SELECT NVL(入会年月日,0),
//            NVL(退会年月日,0),
//                    NVL(ＴＥＬ止め区分,0),
//                    NVL(ＤＭ止め区分,0),
//                    NVL(Ｅメール止め区分,0),
//                    NVL(携帯ＴＥＬ止め区分,0),
//                    NVL(携帯Ｅメール止め区分,0),
//                    NVL(作業企業コード,0),
//                    NVL(作業者ＩＤ,0),
//                    NVL(作業年月日,0),
//                    NVL(作業時刻,0),
//                    NVL(バッチ更新日,0),
//                    NVL(最終更新日,0),
//                    to_number(to_char(NVL(最終更新日時,sysdate),'YYYYMMDDHHMISS')),
//                    NVL(最終更新プログラムＩＤ,'                    ')
//                       INTO :h_mm_kigyobetu_buff.nyukai_ymd,                  /* 入会年月日             */
//                            :h_mm_kigyobetu_buff.taikai_ymd,                  /* 退会年月日             */
//                            :h_mm_kigyobetu_buff.tel_tome_kbn,                /* ＴＥＬ止め区分         */
//                            :h_mm_kigyobetu_buff.dm_tome_kbn,                 /* ＤＭ止め区分           */
//                            :h_mm_kigyobetu_buff.email_tome_kbn,              /* Ｅメール止め区分       */
//                            :h_mm_kigyobetu_buff.ketai_tel_tome_kbn,          /* 携帯ＴＥＬ止め区分     */
//                            :h_mm_kigyobetu_buff.ketai_email_tome_kbn,        /* 携帯Ｅメール止め区分   */
//                            :h_mm_kigyobetu_buff.sagyo_kigyo_cd,              /* 作業企業コード         */
//                            :h_mm_kigyobetu_buff.sagyosha_id,                 /* 作業者ＩＤ             */
//                            :h_mm_kigyobetu_buff.sagyo_ymd,                   /* 作業年月日             */
//                            :h_mm_kigyobetu_buff.sagyo_hms,                   /* 作業時刻               */
//                            :h_mm_kigyobetu_buff.batch_koshin_ymd,            /* バッチ更新日           */
//                            :h_mm_kigyobetu_buff.saishu_koshin_ymd,           /* 最終更新日             */
//                            :h_mm_kigyobetu_buff.saishu_koshin_ymdhms,        /* 最終更新日時           */
//                            :h_mm_kigyobetu_buff.saishu_koshin_programid      /* 最終更新プログラムＩＤ */
//            FROM  MM顧客企業別属性情報@CMMD
//            WHERE 顧客番号       = :h_mm_kigyobetu_buff.kokyaku_no
//            AND 企業コード     = :h_mm_kigyobetu_buff.kigyo_cd;
            sqlca.query(h_mm_kigyobetu_buff.kokyaku_no, h_mm_kigyobetu_buff.kigyo_cd);
        }

        sqlca.fetch();
        /* データ無し以外エラーの場合処理を異常終了する */
        if (sqlca.sqlcode != C_const_Ora_OK &&
                sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "顧客番号=%s" + "企業コード=%d",
                    h_mm_kigyobetu_buff.kokyaku_no.arr, h_mm_kigyobetu_buff.kigyo_cd);
            APLOG_WT("904", 0, null, "SELECT", (long) sqlca.sqlcode,
                    "MM顧客企業別属性情報", out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }
        sqlca.recData(h_mm_kigyobetu_buff.nyukai_ymd,
                h_mm_kigyobetu_buff.taikai_ymd,
                h_mm_kigyobetu_buff.tel_tome_kbn,
                h_mm_kigyobetu_buff.dm_tome_kbn,
                h_mm_kigyobetu_buff.email_tome_kbn,
                h_mm_kigyobetu_buff.ketai_tel_tome_kbn,
                h_mm_kigyobetu_buff.ketai_email_tome_kbn,
                h_mm_kigyobetu_buff.sagyo_kigyo_cd,
                h_mm_kigyobetu_buff.sagyosha_id,
                h_mm_kigyobetu_buff.sagyo_ymd,
                h_mm_kigyobetu_buff.sagyo_hms,
                h_mm_kigyobetu_buff.batch_koshin_ymd,
                h_mm_kigyobetu_buff.saishu_koshin_ymd,
                h_mm_kigyobetu_buff.saishu_koshin_ymdhms,
                h_mm_kigyobetu_buff.saishu_koshin_programid);

        /* 出力引数の設定 */
        ccMaster.nyukai_ymd = h_mm_kigyobetu_buff.nyukai_ymd;           /* 入会年月日             */
        ccMaster.taikai_ymd = h_mm_kigyobetu_buff.taikai_ymd;           /* 退会年月日             */
        ccMaster.tel_tome_kbn = h_mm_kigyobetu_buff.tel_tome_kbn;         /* ＴＥＬ止め区分         */
        ccMaster.dm_tome_kbn = h_mm_kigyobetu_buff.dm_tome_kbn;          /* ＤＭ止め区分           */
        ccMaster.email_tome_kbn = h_mm_kigyobetu_buff.email_tome_kbn;       /* Ｅメール止め区分       */
        ccMaster.ketai_tel_tome_kbn = h_mm_kigyobetu_buff.ketai_tel_tome_kbn;   /* 携帯ＴＥＬ止め区分     */
        ccMaster.ketai_email_tome_kbn = h_mm_kigyobetu_buff.ketai_email_tome_kbn; /* 携帯Ｅメール止め区分   */
        ccMaster.sagyo_kigyo_cd = h_mm_kigyobetu_buff.sagyo_kigyo_cd;       /* 作業企業コード         */
        ccMaster.sagyosha_id = h_mm_kigyobetu_buff.sagyosha_id;          /* 作業者ＩＤ             */
        ccMaster.sagyo_ymd = h_mm_kigyobetu_buff.sagyo_ymd;            /* 作業年月日             */
        ccMaster.sagyo_hms = h_mm_kigyobetu_buff.sagyo_hms;            /* 作業時刻               */
        ccMaster.batch_koshin_ymd = h_mm_kigyobetu_buff.batch_koshin_ymd;     /* バッチ更新日           */
        ccMaster.saishu_koshin_ymd = h_mm_kigyobetu_buff.saishu_koshin_ymd;    /* 最終更新日             */
        ccMaster.saishu_koshin_ymdhms = h_mm_kigyobetu_buff.saishu_koshin_ymdhms; /* 最終更新日時           */
        strcpy(ccMaster.saishu_koshin_programid,
                h_mm_kigyobetu_buff.saishu_koshin_programid);                        /* 最終更新プログラムＩＤ */

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_GetccMaster : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_GetccMaster Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetYearPoint                                              */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetYearPoint(YEAR_POINT_DATA  *yearPointData, , int year,       */
    /*                         int status.arr)                                       */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ポイント年別情報取得処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              YEAR_POINT_DATA * yearPointData ： ポイント年別情報構造体取得 */
    /*                                                 パラメータ                 */
    /*              int           year   ： 年                                    */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetYearPoint(TS_YEAR_POINT_TBL tsYearPoint, int year, IntegerDto status) {

        StringDto tbl_nam = new StringDto();
        StringDto sql_buf = new StringDto();     /* ＳＱＬ文編集用 */
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        int res;
        String penv;


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        TS_YEAR_POINT_TBL h_ts_year_point_buff = new TS_YEAR_POINT_TBL(); /* TSポイント年別情報 */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_GetYearPoint : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (tsYearPoint == null || year == 0 || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 || strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* 出力エリアのクリア */
        tsYearPoint.nenkan_fuyo_point.arr = 0;                      /* 年間付与ポイント               */
        tsYearPoint.nenkan_riyo_point.arr = 0;                      /* 年間利用ポイント               */
        tsYearPoint.nenkan_kihon_pritsu_taisho_point.arr = 0;       /* 年間基本Ｐ率対象ポイント       */
        tsYearPoint.nenkan_rankup_taisho_kingaku.arr = 0;           /* 年間ランクＵＰ対象金額         */
        tsYearPoint.nenkan_point_taisho_kingaku.arr = 0;            /* 年間ポイント対象金額           */
        tsYearPoint.nenkan_kaiage_kingaku.arr = 0;                  /* 年間買上額                     */
        tsYearPoint.nenkan_kaiage_cnt.arr = 0;                      /* 年間買上回数                   */
        tsYearPoint.nenkan_kaiage_nissu.arr = 0;                    /* 年間買上日数                   */
        tsYearPoint.nenkan_kaimonoken_hakko_point.arr = 0;          /* 年間買物券発行ポイント         */
        tsYearPoint.gekkan_fuyo_point_01.arr = 0;                   /* 月間付与ポイント０１           */
        tsYearPoint.gekkan_fuyo_point_02.arr = 0;                   /* 月間付与ポイント０２           */
        tsYearPoint.gekkan_fuyo_point_03.arr = 0;                   /* 月間付与ポイント０３           */
        tsYearPoint.gekkan_fuyo_point_04.arr = 0;                   /* 月間付与ポイント０４           */
        tsYearPoint.gekkan_fuyo_point_05.arr = 0;                   /* 月間付与ポイント０５           */
        tsYearPoint.gekkan_fuyo_point_06.arr = 0;                   /* 月間付与ポイント０６           */
        tsYearPoint.gekkan_fuyo_point_07.arr = 0;                   /* 月間付与ポイント０７           */
        tsYearPoint.gekkan_fuyo_point_08.arr = 0;                   /* 月間付与ポイント０８           */
        tsYearPoint.gekkan_fuyo_point_09.arr = 0;                   /* 月間付与ポイント０９           */
        tsYearPoint.gekkan_fuyo_point_10.arr = 0;                   /* 月間付与ポイント１０           */
        tsYearPoint.gekkan_fuyo_point_11.arr = 0;                   /* 月間付与ポイント１１           */
        tsYearPoint.gekkan_fuyo_point_12.arr = 0;                   /* 月間付与ポイント１２           */
        tsYearPoint.gekkan_riyo_point_01.arr = 0;                   /* 月間利用ポイント０１           */
        tsYearPoint.gekkan_riyo_point_02.arr = 0;                   /* 月間利用ポイント０２           */
        tsYearPoint.gekkan_riyo_point_03.arr = 0;                   /* 月間利用ポイント０３           */
        tsYearPoint.gekkan_riyo_point_04.arr = 0;                   /* 月間利用ポイント０４           */
        tsYearPoint.gekkan_riyo_point_05.arr = 0;                   /* 月間利用ポイント０５           */
        tsYearPoint.gekkan_riyo_point_06.arr = 0;                   /* 月間利用ポイント０６           */
        tsYearPoint.gekkan_riyo_point_07.arr = 0;                   /* 月間利用ポイント０７           */
        tsYearPoint.gekkan_riyo_point_08.arr = 0;                   /* 月間利用ポイント０８           */
        tsYearPoint.gekkan_riyo_point_09.arr = 0;                   /* 月間利用ポイント０９           */
        tsYearPoint.gekkan_riyo_point_10.arr = 0;                   /* 月間利用ポイント１０           */
        tsYearPoint.gekkan_riyo_point_11.arr = 0;                   /* 月間利用ポイント１１           */
        tsYearPoint.gekkan_riyo_point_12.arr = 0;                   /* 月間利用ポイント１２           */
        tsYearPoint.gekkan_rankup_taisho_kingaku_01.arr = 0;        /* 月間ランクＵＰ対象金額０１     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_02.arr = 0;        /* 月間ランクＵＰ対象金額０２     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_03.arr = 0;        /* 月間ランクＵＰ対象金額０３     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_04.arr = 0;        /* 月間ランクＵＰ対象金額０４     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_05.arr = 0;        /* 月間ランクＵＰ対象金額０５     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_06.arr = 0;        /* 月間ランクＵＰ対象金額０６     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_07.arr = 0;        /* 月間ランクＵＰ対象金額０７     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_08.arr = 0;        /* 月間ランクＵＰ対象金額０８     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_09.arr = 0;        /* 月間ランクＵＰ対象金額０９     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_10.arr = 0;        /* 月間ランクＵＰ対象金額１０     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_11.arr = 0;        /* 月間ランクＵＰ対象金額１１     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_12.arr = 0;        /* 月間ランクＵＰ対象金額１２     */
        tsYearPoint.gekkan_kaiage_cnt_01.arr = 0;                   /* 月間買上回数０１               */
        tsYearPoint.gekkan_kaiage_cnt_02.arr = 0;                   /* 月間買上回数０２               */
        tsYearPoint.gekkan_kaiage_cnt_03.arr = 0;                   /* 月間買上回数０３               */
        tsYearPoint.gekkan_kaiage_cnt_04.arr = 0;                   /* 月間買上回数０４               */
        tsYearPoint.gekkan_kaiage_cnt_05.arr = 0;                   /* 月間買上回数０５               */
        tsYearPoint.gekkan_kaiage_cnt_06.arr = 0;                   /* 月間買上回数０６               */
        tsYearPoint.gekkan_kaiage_cnt_07.arr = 0;                   /* 月間買上回数０７               */
        tsYearPoint.gekkan_kaiage_cnt_08.arr = 0;                   /* 月間買上回数０８               */
        tsYearPoint.gekkan_kaiage_cnt_09.arr = 0;                   /* 月間買上回数０９               */
        tsYearPoint.gekkan_kaiage_cnt_10.arr = 0;                   /* 月間買上回数１０               */
        tsYearPoint.gekkan_kaiage_cnt_11.arr = 0;                   /* 月間買上回数１１               */
        tsYearPoint.gekkan_kaiage_cnt_12.arr = 0;                   /* 月間買上回数１２               */
        tsYearPoint.saishu_koshin_ymd.arr = 0;                      /* 最終更新日                     */
        tsYearPoint.saishu_koshin_ymdhms.arr = 0;                   /* 最終更新日時                   */
        strcpy(tsYearPoint.saishu_koshin_programid, "                    ");  /* 最終更新プログラムＩＤ */

        /* ホスト変数を編集する */
//        memset(h_ts_year_point_buff, 0x00, sizeof(TS_YEAR_POINT_TBL));
        memset(h_ts_year_point_buff, 0x00, sizeof(0));
        memcpy(h_ts_year_point_buff.kokyaku_no, tsYearPoint.kokyaku_no,
                tsYearPoint.kokyaku_no.len);
        h_ts_year_point_buff.kokyaku_no.len = tsYearPoint.kokyaku_no.len;
        h_ts_year_point_buff.year = tsYearPoint.year;

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを生成する */
        strcpy(sql_buf, "SELECT ");
        strcat(sql_buf, "NVL(年間付与ポイント,0),");
        strcat(sql_buf, "NVL(年間利用ポイント,0),");
        strcat(sql_buf, "NVL(年間基本Ｐ率対象ポイント,0),");
        strcat(sql_buf, "NVL(年間ランクＵＰ対象金額,0),");
        strcat(sql_buf, "NVL(年間ポイント対象金額,0),");
        strcat(sql_buf, "NVL(年間買上額,0),");
        strcat(sql_buf, "NVL(年間買上回数,0),");
        strcat(sql_buf, "NVL(年間買上日数,0),");
        strcat(sql_buf, "NVL(年間買物券発行ポイント,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０１,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０２,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０３,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０４,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０５,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０６,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０７,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０８,0),");
        strcat(sql_buf, "NVL(月間付与ポイント０９,0),");
        strcat(sql_buf, "NVL(月間付与ポイント１０,0),");
        strcat(sql_buf, "NVL(月間付与ポイント１１,0),");
        strcat(sql_buf, "NVL(月間付与ポイント１２,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０１,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０２,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０３,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０４,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０５,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０６,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０７,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０８,0),");
        strcat(sql_buf, "NVL(月間利用ポイント０９,0),");
        strcat(sql_buf, "NVL(月間利用ポイント１０,0),");
        strcat(sql_buf, "NVL(月間利用ポイント１１,0),");
        strcat(sql_buf, "NVL(月間利用ポイント１２,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０１,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０２,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０３,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０４,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０５,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０６,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０７,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０８,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額０９,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額１０,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額１１,0),");
        strcat(sql_buf, "NVL(月間ランクＵＰ対象金額１２,0),");
        strcat(sql_buf, "NVL(月間買上回数０１,0),");
        strcat(sql_buf, "NVL(月間買上回数０２,0),");
        strcat(sql_buf, "NVL(月間買上回数０３,0),");
        strcat(sql_buf, "NVL(月間買上回数０４,0),");
        strcat(sql_buf, "NVL(月間買上回数０５,0),");
        strcat(sql_buf, "NVL(月間買上回数０６,0),");
        strcat(sql_buf, "NVL(月間買上回数０７,0),");
        strcat(sql_buf, "NVL(月間買上回数０８,0),");
        strcat(sql_buf, "NVL(月間買上回数０９,0),");
        strcat(sql_buf, "NVL(月間買上回数１０,0),");
        strcat(sql_buf, "NVL(月間買上回数１１,0),");
        strcat(sql_buf, "NVL(月間買上回数１２,0),");
        strcat(sql_buf, "nvl(最終更新日,0),");
        strcat(sql_buf, "to_number(to_char(coalesce(最終更新日時,sysdate()),'YYYYMMDDHHMISS')),");
        strcat(sql_buf, "nvl(rpad(最終更新プログラムＩＤ,length(最終更新プログラムＩＤ)),' ')");

        sprintf(tbl_nam, " FROM TSポイント年別情報%d", year);
        if (strcmp(Cg_ORASID, penv) != 0) {
            strcat(tbl_nam, "");
        }
        strcat(sql_buf, tbl_nam);

        strcat(sql_buf, " WHERE 年 = ?");
        strcat(sql_buf, " AND 顧客番号       = ?");

        if (DBG_LOG) {
            C_DbgMsg("C_GetYearPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);

        if (DBG_LOG) {
            C_DbgMsg("C_GetYearPoint : 年=%d\n",
                    h_ts_year_point_buff.year);
            C_DbgMsg("C_GetYearPoint : 顧客番号=%s\n",
                    h_ts_year_point_buff.kokyaku_no.arr);
        }

//        SqlstmDto sqlca = sqlcaManager.get("sql_kdatalock1");
        sqlca.sql = WRKSQL;
//        EXEC SQL PREPARE sql_kdatalock1 from :WRKSQL;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年=%d,顧客番号=%s",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "SELECT PREPARE",
                    (long) sqlca.sqlcode, tbl_nam, out_format_buf,
                    0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        sqlca.declare();
//        EXEC SQL DECLARE cur_kdatalock1 cursor for sql_kdatalock1;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : DECLARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年=%d", year);
            APLOG_WT("904", 0, null, "SELECT DECLARE",
                    (long) sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* select文を発行する */

        /* カーソルのオープン */
//        EXEC SQL OPEN cur_kdatalock1 using :h_ts_year_point_buff.year,
//                      :h_ts_year_point_buff.kokyaku_no;
        sqlca.open(h_ts_year_point_buff.year
                , h_ts_year_point_buff.kokyaku_no);
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : OPEN : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年=%d", year);
            APLOG_WT("904", 0, null, "SELECT OPEN",
                    sqlca.sqlcode, tbl_nam, out_format_buf, 0, 0);
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }


        /* カーソルのフェッチ */
        sqlca.fetch();
//
//        EXEC SQL FETCH cur_kdatalock1 into
//            :h_ts_year_point_buff.nenkan_fuyo_point,                      /* 年間付与ポイント               */
//            :h_ts_year_point_buff.nenkan_riyo_point,                      /* 年間利用ポイント               */
//            :h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point,       /* 年間基本Ｐ率対象ポイント       */
//            :h_ts_year_point_buff.nenkan_rankup_taisho_kingaku,           /* 年間ランクＵＰ対象金額         */
//            :h_ts_year_point_buff.nenkan_point_taisho_kingaku,            /* 年間ポイント対象金額           */
//            :h_ts_year_point_buff.nenkan_kaiage_kingaku,                  /* 年間買上額                     */
//            :h_ts_year_point_buff.nenkan_kaiage_cnt,                      /* 年間買上回数                   */
//            :h_ts_year_point_buff.nenkan_kaiage_nissu,                    /* 年間買上日数                   */
//            :h_ts_year_point_buff.nenkan_kaimonoken_hakko_point,          /* 年間買物券発行ポイント         */
//            :h_ts_year_point_buff.gekkan_fuyo_point_01,                   /* 月間付与ポイント０１           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_02,                   /* 月間付与ポイント０２           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_03,                   /* 月間付与ポイント０３           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_04,                   /* 月間付与ポイント０４           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_05,                   /* 月間付与ポイント０５           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_06,                   /* 月間付与ポイント０６           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_07,                   /* 月間付与ポイント０７           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_08,                   /* 月間付与ポイント０８           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_09,                   /* 月間付与ポイント０９           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_10,                   /* 月間付与ポイント１０           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_11,                   /* 月間付与ポイント１１           */
//            :h_ts_year_point_buff.gekkan_fuyo_point_12,                   /* 月間付与ポイント１２           */
//            :h_ts_year_point_buff.gekkan_riyo_point_01,                   /* 月間利用ポイント０１           */
//            :h_ts_year_point_buff.gekkan_riyo_point_02,                   /* 月間利用ポイント０２           */
//            :h_ts_year_point_buff.gekkan_riyo_point_03,                   /* 月間利用ポイント０３           */
//            :h_ts_year_point_buff.gekkan_riyo_point_04,                   /* 月間利用ポイント０４           */
//            :h_ts_year_point_buff.gekkan_riyo_point_05,                   /* 月間利用ポイント０５           */
//            :h_ts_year_point_buff.gekkan_riyo_point_06,                   /* 月間利用ポイント０６           */
//            :h_ts_year_point_buff.gekkan_riyo_point_07,                   /* 月間利用ポイント０７           */
//            :h_ts_year_point_buff.gekkan_riyo_point_08,                   /* 月間利用ポイント０８           */
//            :h_ts_year_point_buff.gekkan_riyo_point_09,                   /* 月間利用ポイント０９           */
//            :h_ts_year_point_buff.gekkan_riyo_point_10,                   /* 月間利用ポイント１０           */
//            :h_ts_year_point_buff.gekkan_riyo_point_11,                   /* 月間利用ポイント１１           */
//            :h_ts_year_point_buff.gekkan_riyo_point_12,                   /* 月間利用ポイント１２           */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01,        /* 月間ランクＵＰ対象金額０１     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02,        /* 月間ランクＵＰ対象金額０２     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03,        /* 月間ランクＵＰ対象金額０３     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04,        /* 月間ランクＵＰ対象金額０４     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05,        /* 月間ランクＵＰ対象金額０５     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06,        /* 月間ランクＵＰ対象金額０６     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07,        /* 月間ランクＵＰ対象金額０７     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08,        /* 月間ランクＵＰ対象金額０８     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09,        /* 月間ランクＵＰ対象金額０９     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10,        /* 月間ランクＵＰ対象金額１０     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11,        /* 月間ランクＵＰ対象金額１１     */
//            :h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12,        /* 月間ランクＵＰ対象金額１２     */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_01,                   /* 月間買上回数０１               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_02,                   /* 月間買上回数０２               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_03,                   /* 月間買上回数０３               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_04,                   /* 月間買上回数０４               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_05,                   /* 月間買上回数０５               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_06,                   /* 月間買上回数０６               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_07,                   /* 月間買上回数０７               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_08,                   /* 月間買上回数０８               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_09,                   /* 月間買上回数０９               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_10,                   /* 月間買上回数１０               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_11,                   /* 月間買上回数１１               */
//            :h_ts_year_point_buff.gekkan_kaiage_cnt_12,                   /* 月間買上回数１２               */
//            :h_ts_year_point_buff.saishu_koshin_ymd,                      /* 最終更新日                     */
//            :h_ts_year_point_buff.saishu_koshin_ymdhms,                   /* 最終更新日時                   */
//            :h_ts_year_point_buff.saishu_koshin_programid;                /* 最終更新プログラムＩＤ         */

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : FETCH : sqlca.sqlcode=[%d]\n", sqlca.sqlcode);
            }
//            sqlcaManager.close("sql_kdatalock1");
            sqlca.curse_close();
//            EXEC SQL CLOSE cur_kdatalock1;
            if (sqlca.sqlcode != C_const_Ora_OK) {
                if (DBG_LOG) {
                    C_DbgMsg("C_GetYearPoint : CLOSE : sqlca.sqlcode=[%d]\n",
                            sqlca.sqlcode);
                }
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        sqlca.recData(h_ts_year_point_buff.nenkan_fuyo_point,
                h_ts_year_point_buff.nenkan_riyo_point,
                h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point,
                h_ts_year_point_buff.nenkan_rankup_taisho_kingaku,
                h_ts_year_point_buff.nenkan_point_taisho_kingaku,
                h_ts_year_point_buff.nenkan_kaiage_kingaku,
                h_ts_year_point_buff.nenkan_kaiage_cnt,
                h_ts_year_point_buff.nenkan_kaiage_nissu,
                h_ts_year_point_buff.nenkan_kaimonoken_hakko_point,
                h_ts_year_point_buff.gekkan_fuyo_point_01,
                h_ts_year_point_buff.gekkan_fuyo_point_02,
                h_ts_year_point_buff.gekkan_fuyo_point_03,
                h_ts_year_point_buff.gekkan_fuyo_point_04,
                h_ts_year_point_buff.gekkan_fuyo_point_05,
                h_ts_year_point_buff.gekkan_fuyo_point_06,
                h_ts_year_point_buff.gekkan_fuyo_point_07,
                h_ts_year_point_buff.gekkan_fuyo_point_08,
                h_ts_year_point_buff.gekkan_fuyo_point_09,
                h_ts_year_point_buff.gekkan_fuyo_point_10,
                h_ts_year_point_buff.gekkan_fuyo_point_11,
                h_ts_year_point_buff.gekkan_fuyo_point_12,
                h_ts_year_point_buff.gekkan_riyo_point_01,
                h_ts_year_point_buff.gekkan_riyo_point_02,
                h_ts_year_point_buff.gekkan_riyo_point_03,
                h_ts_year_point_buff.gekkan_riyo_point_04,
                h_ts_year_point_buff.gekkan_riyo_point_05,
                h_ts_year_point_buff.gekkan_riyo_point_06,
                h_ts_year_point_buff.gekkan_riyo_point_07,
                h_ts_year_point_buff.gekkan_riyo_point_08,
                h_ts_year_point_buff.gekkan_riyo_point_09,
                h_ts_year_point_buff.gekkan_riyo_point_10,
                h_ts_year_point_buff.gekkan_riyo_point_11,
                h_ts_year_point_buff.gekkan_riyo_point_12,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11,
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12,
                h_ts_year_point_buff.gekkan_kaiage_cnt_01,
                h_ts_year_point_buff.gekkan_kaiage_cnt_02,
                h_ts_year_point_buff.gekkan_kaiage_cnt_03,
                h_ts_year_point_buff.gekkan_kaiage_cnt_04,
                h_ts_year_point_buff.gekkan_kaiage_cnt_05,
                h_ts_year_point_buff.gekkan_kaiage_cnt_06,
                h_ts_year_point_buff.gekkan_kaiage_cnt_07,
                h_ts_year_point_buff.gekkan_kaiage_cnt_08,
                h_ts_year_point_buff.gekkan_kaiage_cnt_09,
                h_ts_year_point_buff.gekkan_kaiage_cnt_10,
                h_ts_year_point_buff.gekkan_kaiage_cnt_11,
                h_ts_year_point_buff.gekkan_kaiage_cnt_12,
                h_ts_year_point_buff.saishu_koshin_ymd,
                h_ts_year_point_buff.saishu_koshin_ymdhms,
                h_ts_year_point_buff.saishu_koshin_programid);

        res = sqlca.sqlcode;

        /* カーソルのクローズ */
//        EXEC SQL CLOSE cur_kdatalock1;
//        sqlcaManager.close("sql_kdatalock1");
        sqlca.curse_close();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_GetYearPoint : CLOSE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
        }

        /* データ無し以外エラーの場合処理を異常終了する */
        if (res != C_const_Ora_OK && res != C_const_Ora_NOTFOUND) {
            /* DBERR */
            sprintf(out_format_buf, "年=%d,顧客番号=%s",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "SELECT PREPARE", (long) res,
                    tbl_nam, out_format_buf, 0, 0);

            /* 処理を終了する */
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        /* データ無しエラーの場合処理を正常終了する */
        else if (res == C_const_Ora_NOTFOUND) {

            /* 処理を終了する */
            status.arr = C_const_Stat_OK;
            return C_const_NOTEXISTS;
        }

        /* 出力引数の設定 */
        tsYearPoint.nenkan_fuyo_point = h_ts_year_point_buff.nenkan_fuyo_point;                      /* 年間付与ポイント               */
        tsYearPoint.nenkan_riyo_point = h_ts_year_point_buff.nenkan_riyo_point;                      /* 年間利用ポイント               */
        tsYearPoint.nenkan_kihon_pritsu_taisho_point = h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point;       /* 年間基本Ｐ率対象ポイント       */
        tsYearPoint.nenkan_rankup_taisho_kingaku = h_ts_year_point_buff.nenkan_rankup_taisho_kingaku;           /* 年間ランクＵＰ対象金額         */
        tsYearPoint.nenkan_point_taisho_kingaku = h_ts_year_point_buff.nenkan_point_taisho_kingaku;            /* 年間ポイント対象金額           */
        tsYearPoint.nenkan_kaiage_kingaku = h_ts_year_point_buff.nenkan_kaiage_kingaku;                  /* 年間買上額                     */
        tsYearPoint.nenkan_kaiage_cnt = h_ts_year_point_buff.nenkan_kaiage_cnt;                      /* 年間買上回数                   */
        tsYearPoint.nenkan_kaiage_nissu = h_ts_year_point_buff.nenkan_kaiage_nissu;                    /* 年間買上日数                   */
        tsYearPoint.nenkan_kaimonoken_hakko_point = h_ts_year_point_buff.nenkan_kaimonoken_hakko_point;          /* 年間買物券発行ポイント         */
        tsYearPoint.gekkan_fuyo_point_01 = h_ts_year_point_buff.gekkan_fuyo_point_01;                   /* 月間付与ポイント０１           */
        tsYearPoint.gekkan_fuyo_point_02 = h_ts_year_point_buff.gekkan_fuyo_point_02;                   /* 月間付与ポイント０２           */
        tsYearPoint.gekkan_fuyo_point_03 = h_ts_year_point_buff.gekkan_fuyo_point_03;                   /* 月間付与ポイント０３           */
        tsYearPoint.gekkan_fuyo_point_04 = h_ts_year_point_buff.gekkan_fuyo_point_04;                   /* 月間付与ポイント０４           */
        tsYearPoint.gekkan_fuyo_point_05 = h_ts_year_point_buff.gekkan_fuyo_point_05;                   /* 月間付与ポイント０５           */
        tsYearPoint.gekkan_fuyo_point_06 = h_ts_year_point_buff.gekkan_fuyo_point_06;                   /* 月間付与ポイント０６           */
        tsYearPoint.gekkan_fuyo_point_07 = h_ts_year_point_buff.gekkan_fuyo_point_07;                   /* 月間付与ポイント０７           */
        tsYearPoint.gekkan_fuyo_point_08 = h_ts_year_point_buff.gekkan_fuyo_point_08;                   /* 月間付与ポイント０８           */
        tsYearPoint.gekkan_fuyo_point_09 = h_ts_year_point_buff.gekkan_fuyo_point_09;                   /* 月間付与ポイント０９           */
        tsYearPoint.gekkan_fuyo_point_10 = h_ts_year_point_buff.gekkan_fuyo_point_10;                   /* 月間付与ポイント１０           */
        tsYearPoint.gekkan_fuyo_point_11 = h_ts_year_point_buff.gekkan_fuyo_point_11;                   /* 月間付与ポイント１１           */
        tsYearPoint.gekkan_fuyo_point_12 = h_ts_year_point_buff.gekkan_fuyo_point_12;                   /* 月間付与ポイント１２           */
        tsYearPoint.gekkan_riyo_point_01 = h_ts_year_point_buff.gekkan_riyo_point_01;                   /* 月間利用ポイント０１           */
        tsYearPoint.gekkan_riyo_point_02 = h_ts_year_point_buff.gekkan_riyo_point_02;                   /* 月間利用ポイント０２           */
        tsYearPoint.gekkan_riyo_point_03 = h_ts_year_point_buff.gekkan_riyo_point_03;                   /* 月間利用ポイント０３           */
        tsYearPoint.gekkan_riyo_point_04 = h_ts_year_point_buff.gekkan_riyo_point_04;                   /* 月間利用ポイント０４           */
        tsYearPoint.gekkan_riyo_point_05 = h_ts_year_point_buff.gekkan_riyo_point_05;                   /* 月間利用ポイント０５           */
        tsYearPoint.gekkan_riyo_point_06 = h_ts_year_point_buff.gekkan_riyo_point_06;                   /* 月間利用ポイント０６           */
        tsYearPoint.gekkan_riyo_point_07 = h_ts_year_point_buff.gekkan_riyo_point_07;                   /* 月間利用ポイント０７           */
        tsYearPoint.gekkan_riyo_point_08 = h_ts_year_point_buff.gekkan_riyo_point_08;                   /* 月間利用ポイント０８           */
        tsYearPoint.gekkan_riyo_point_09 = h_ts_year_point_buff.gekkan_riyo_point_09;                   /* 月間利用ポイント０９           */
        tsYearPoint.gekkan_riyo_point_10 = h_ts_year_point_buff.gekkan_riyo_point_10;                   /* 月間利用ポイント１０           */
        tsYearPoint.gekkan_riyo_point_11 = h_ts_year_point_buff.gekkan_riyo_point_11;                   /* 月間利用ポイント１１           */
        tsYearPoint.gekkan_riyo_point_12 = h_ts_year_point_buff.gekkan_riyo_point_12;                   /* 月間利用ポイント１２           */
        tsYearPoint.gekkan_rankup_taisho_kingaku_01 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01;        /* 月間ランクＵＰ対象金額０１     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_02 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02;        /* 月間ランクＵＰ対象金額０２     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_03 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03;        /* 月間ランクＵＰ対象金額０３     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_04 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04;        /* 月間ランクＵＰ対象金額０４     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_05 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05;        /* 月間ランクＵＰ対象金額０５     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_06 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06;        /* 月間ランクＵＰ対象金額０６     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_07 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07;        /* 月間ランクＵＰ対象金額０７     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_08 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08;        /* 月間ランクＵＰ対象金額０８     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_09 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09;        /* 月間ランクＵＰ対象金額０９     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_10 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10;        /* 月間ランクＵＰ対象金額１０     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_11 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11;        /* 月間ランクＵＰ対象金額１１     */
        tsYearPoint.gekkan_rankup_taisho_kingaku_12 = h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12;        /* 月間ランクＵＰ対象金額１２     */
        tsYearPoint.gekkan_kaiage_cnt_01 = h_ts_year_point_buff.gekkan_kaiage_cnt_01;                   /* 月間買上回数０１               */
        tsYearPoint.gekkan_kaiage_cnt_02 = h_ts_year_point_buff.gekkan_kaiage_cnt_02;                   /* 月間買上回数０２               */
        tsYearPoint.gekkan_kaiage_cnt_03 = h_ts_year_point_buff.gekkan_kaiage_cnt_03;                   /* 月間買上回数０３               */
        tsYearPoint.gekkan_kaiage_cnt_04 = h_ts_year_point_buff.gekkan_kaiage_cnt_04;                   /* 月間買上回数０４               */
        tsYearPoint.gekkan_kaiage_cnt_05 = h_ts_year_point_buff.gekkan_kaiage_cnt_05;                   /* 月間買上回数０５               */
        tsYearPoint.gekkan_kaiage_cnt_06 = h_ts_year_point_buff.gekkan_kaiage_cnt_06;                   /* 月間買上回数０６               */
        tsYearPoint.gekkan_kaiage_cnt_07 = h_ts_year_point_buff.gekkan_kaiage_cnt_07;                   /* 月間買上回数０７               */
        tsYearPoint.gekkan_kaiage_cnt_08 = h_ts_year_point_buff.gekkan_kaiage_cnt_08;                   /* 月間買上回数０８               */
        tsYearPoint.gekkan_kaiage_cnt_09 = h_ts_year_point_buff.gekkan_kaiage_cnt_09;                   /* 月間買上回数０９               */
        tsYearPoint.gekkan_kaiage_cnt_10 = h_ts_year_point_buff.gekkan_kaiage_cnt_10;                   /* 月間買上回数１０               */
        tsYearPoint.gekkan_kaiage_cnt_11 = h_ts_year_point_buff.gekkan_kaiage_cnt_11;                   /* 月間買上回数１１               */
        tsYearPoint.gekkan_kaiage_cnt_12 = h_ts_year_point_buff.gekkan_kaiage_cnt_12;                   /* 月間買上回数１２               */
        tsYearPoint.saishu_koshin_ymd = h_ts_year_point_buff.saishu_koshin_ymd;                      /* 最終更新日                     */
        tsYearPoint.saishu_koshin_ymdhms = h_ts_year_point_buff.saishu_koshin_ymdhms;                   /* 最終更新日時                   */
        strcpy(tsYearPoint.saishu_koshin_programid, h_ts_year_point_buff.saishu_koshin_programid);               /* 最終更新プログラムＩＤ         */

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_GetYearPoint : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_GetYearPoint Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetFamilyRank                                             */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetFamilyRank(int p_kazoku_id, int *p_kazoku_honnen_rank_cd,    */
    /*                                           int *status)                     */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              家族ランクコード取得処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              int     p_kazoku_id              ： (i)家族ＩＤ               */
    /*              int   * p_kazoku_honnen_rank_cd  ： (o)家族本年ランクコード   */
    /*              int   * p_kazoku_tougetsu_rank_cd： (o)家族当月ランクコード   */
    /*              int   * status                   ： (o)結果ステータス         */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetFamilyRank(int p_kazoku_id, IntegerDto p_kazoku_honnen_rank_cd, IntegerDto p_kazoku_tougetsu_rank_cd,
                               IntegerDto status) {
        int rtn_cd;
        IntegerDto rtn_status = new IntegerDto();
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        StringDto w_bat_date = new StringDto(9); /* バッチ処理日付 */
        StringDto w_bat_yyyy = new StringDto(5); /* バッチ処理年   */
        StringDto w_bat_mm = new StringDto(3);   /* バッチ処理月   */
        int w_honnen;
        int w_tougetsu;
        String penv;

        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;
        MS_KAZOKU_SEDO_INFO_TBL h_ms_kazoku_seido_buff; /* MS家族制度情報バッファ */
//        EXEC SQL END DECLARE SECTION;


        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : %s\n", "start");
        }

        /* 引数のチェックを行う */
        if (p_kazoku_honnen_rank_cd == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetFamilyRank : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetFamilyRank : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }

        /* バッチ処理日付を取得 */
        memset(w_bat_date, 0x00, sizeof(w_bat_date));
        memset(w_bat_yyyy, 0x00, sizeof(w_bat_yyyy));
        memset(w_bat_mm, 0x00, sizeof(w_bat_mm));

        rtn_cd = C_GetBatDate(0, w_bat_date, rtn_status);
        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : C_GetBatDate=%d\n", rtn_cd);
        }
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_GetBatDate", rtn_cd,
                    0, 0, 0, 0);
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }
        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : C_GetBatDate=[%s]\n", w_bat_date);
        }
        if (strlen(w_bat_date) != 8) {
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 出力エリアのクリア */
        p_kazoku_honnen_rank_cd.arr = 0;
        p_kazoku_tougetsu_rank_cd.arr = 0;

        /* 本年当月の設定                                                       */
        memcpy(w_bat_yyyy, w_bat_date, 4);
        memcpy(w_bat_mm, (w_bat_date.substring(4)), 2);
        w_honnen = (atoi(w_bat_yyyy)) % 10;
        w_tougetsu = (atoi(w_bat_mm));

        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : w_honnen=%d\n", w_honnen);
            C_DbgMsg("C_GetFamilyRank : w_tougetsu=%d\n", w_tougetsu);
        }

        /* 引数の家族ＩＤをキーに、MS家族情報のランクコードを検索する */

        /* ホスト変数を編集する */
        h_ms_kazoku_seido_buff = new MS_KAZOKU_SEDO_INFO_TBL();
        memset(h_ms_kazoku_seido_buff, 0x00, sizeof(h_ms_kazoku_seido_buff));
        //h_ms_kazoku_seido_buff.kazoku_id = p_kazoku_id;
        sprintf(h_ms_kazoku_seido_buff.kazoku_id, "%d", p_kazoku_id);
        h_ms_kazoku_seido_buff.kazoku_id.len = strlen(h_ms_kazoku_seido_buff.kazoku_id);

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを実行する */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客制度データベースに接続している場合 */
            sqlca.sql.arr = " SELECT NVL (年次ランクコード０, 0)," +
                    "            NVL(年次ランクコード１, 0)," +
                    "                    NVL(年次ランクコード２, 0)," +
                    "                    NVL(年次ランクコード３, 0)," +
                    "                    NVL(年次ランクコード４, 0)," +
                    "                    NVL(年次ランクコード５, 0)," +
                    "                    NVL(年次ランクコード６, 0)," +
                    "                    NVL(年次ランクコード７, 0)," +
                    "                    NVL(年次ランクコード８, 0)," +
                    "                    NVL(年次ランクコード９, 0)," +
                    "                    NVL(月次ランクコード００１, 0)," +
                    "                    NVL(月次ランクコード００２, 0)," +
                    "                    NVL(月次ランクコード００３, 0)," +
                    "                    NVL(月次ランクコード００４, 0)," +
                    "                    NVL(月次ランクコード００５, 0)," +
                    "                    NVL(月次ランクコード００６, 0)," +
                    "                    NVL(月次ランクコード００７, 0)," +
                    "                    NVL(月次ランクコード００８, 0)," +
                    "                    NVL(月次ランクコード００９, 0)," +
                    "                    NVL(月次ランクコード０１０, 0)," +
                    "                    NVL(月次ランクコード０１１, 0)," +
                    "                    NVL(月次ランクコード０１２, 0)," +
                    "                    NVL(月次ランクコード１０１, 0)," +
                    "                    NVL(月次ランクコード１０２, 0)," +
                    "                    NVL(月次ランクコード１０３, 0)," +
                    "                    NVL(月次ランクコード１０４, 0)," +
                    "                    NVL(月次ランクコード１０５, 0)," +
                    "                    NVL(月次ランクコード１０６, 0)," +
                    "                    NVL(月次ランクコード１０７, 0)," +
                    "                    NVL(月次ランクコード１０８, 0)," +
                    "                    NVL(月次ランクコード１０９, 0)," +
                    "                    NVL(月次ランクコード１１０, 0)," +
                    "                    NVL(月次ランクコード１１１, 0)," +
                    "                    NVL(月次ランクコード１１２, 0)" +
                    "            FROM MS家族制度情報" +
                    "            WHERE 家族ＩＤ = ?";
            sqlca.restAndExecute(h_ms_kazoku_seido_buff.kazoku_id);
            sqlca.fetchInto(h_ms_kazoku_seido_buff.nenji_rank_cd_0,    /* 年次ランクコード０     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_1,    /* 年次ランクコード１     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_2,    /* 年次ランクコード２     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_3,    /* 年次ランクコード３     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_4,    /* 年次ランクコード４     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_5,    /* 年次ランクコード５     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_6,    /* 年次ランクコード６     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_7,    /* 年次ランクコード７     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_8,    /* 年次ランクコード８     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_9,    /* 年次ランクコード９     */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_001, /* 月次ランクコード００１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_002, /* 月次ランクコード００２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_003, /* 月次ランクコード００３ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_004, /* 月次ランクコード００４ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_005, /* 月次ランクコード００５ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_006, /* 月次ランクコード００６ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_007, /* 月次ランクコード００７ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_008, /* 月次ランクコード００８ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_009, /* 月次ランクコード００９ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_010, /* 月次ランクコード０１０ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_011, /* 月次ランクコード０１１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_012, /* 月次ランクコード０１２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_101, /* 月次ランクコード１０１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_102, /* 月次ランクコード１０２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_103, /* 月次ランクコード１０３ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_104, /* 月次ランクコード１０４ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_105, /* 月次ランクコード１０５ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_106, /* 月次ランクコード１０６ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_107, /* 月次ランクコード１０７ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_108, /* 月次ランクコード１０８ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_109, /* 月次ランクコード１０９ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_110, /* 月次ランクコード１１０ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_111, /* 月次ランクコード１１１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_112  /* 月次ランクコード１１２ */);
        } else {
            /* 顧客制度データベース以外に接続している場合 */
            sqlca.sql.arr = "SELECT NVL (年次ランクコード０, 0)," +
                    "            NVL(年次ランクコード１, 0)," +
                    "                    NVL(年次ランクコード２, 0)," +
                    "                    NVL(年次ランクコード３, 0)," +
                    "                    NVL(年次ランクコード４, 0)," +
                    "                    NVL(年次ランクコード５, 0)," +
                    "                    NVL(年次ランクコード６, 0)," +
                    "                    NVL(年次ランクコード７, 0)," +
                    "                    NVL(年次ランクコード８, 0)," +
                    "                    NVL(年次ランクコード９, 0)," +
                    "                    NVL(月次ランクコード００１, 0)," +
                    "                    NVL(月次ランクコード００２, 0)," +
                    "                    NVL(月次ランクコード００３, 0)," +
                    "                    NVL(月次ランクコード００４, 0)," +
                    "                    NVL(月次ランクコード００５, 0)," +
                    "                    NVL(月次ランクコード００６, 0)," +
                    "                    NVL(月次ランクコード００７, 0)," +
                    "                    NVL(月次ランクコード００８, 0)," +
                    "                    NVL(月次ランクコード００９, 0)," +
                    "                    NVL(月次ランクコード０１０, 0)," +
                    "                    NVL(月次ランクコード０１１, 0)," +
                    "                    NVL(月次ランクコード０１２, 0)," +
                    "                    NVL(月次ランクコード１０１, 0)," +
                    "                    NVL(月次ランクコード１０２, 0)," +
                    "                    NVL(月次ランクコード１０３, 0)," +
                    "                    NVL(月次ランクコード１０４, 0)," +
                    "                    NVL(月次ランクコード１０５, 0)," +
                    "                    NVL(月次ランクコード１０６, 0)," +
                    "                    NVL(月次ランクコード１０７, 0)," +
                    "                    NVL(月次ランクコード１０８, 0)," +
                    "                    NVL(月次ランクコード１０９, 0)," +
                    "                    NVL(月次ランクコード１１０, 0)," +
                    "                    NVL(月次ランクコード１１１, 0)," +
                    "                    NVL(月次ランクコード１１２, 0)" +
                    "  FROM MS家族制度情報 " +
                    "            WHERE 家族ＩＤ = ?";
            sqlca.restAndExecute(h_ms_kazoku_seido_buff.kazoku_id);
            sqlca.fetchInto(h_ms_kazoku_seido_buff.nenji_rank_cd_0,    /* 年次ランクコード０     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_1,    /* 年次ランクコード１     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_2,    /* 年次ランクコード２     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_3,    /* 年次ランクコード３     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_4,    /* 年次ランクコード４     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_5,    /* 年次ランクコード５     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_6,    /* 年次ランクコード６     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_7,    /* 年次ランクコード７     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_8,    /* 年次ランクコード８     */
                    h_ms_kazoku_seido_buff.nenji_rank_cd_9,    /* 年次ランクコード９     */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_001, /* 月次ランクコード００１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_002, /* 月次ランクコード００２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_003, /* 月次ランクコード００３ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_004, /* 月次ランクコード００４ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_005, /* 月次ランクコード００５ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_006, /* 月次ランクコード００６ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_007, /* 月次ランクコード００７ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_008, /* 月次ランクコード００８ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_009, /* 月次ランクコード００９ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_010, /* 月次ランクコード０１０ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_011, /* 月次ランクコード０１１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_012, /* 月次ランクコード０１２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_101, /* 月次ランクコード１０１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_102, /* 月次ランクコード１０２ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_103, /* 月次ランクコード１０３ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_104, /* 月次ランクコード１０４ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_105, /* 月次ランクコード１０５ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_106, /* 月次ランクコード１０６ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_107, /* 月次ランクコード１０７ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_108, /* 月次ランクコード１０８ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_109, /* 月次ランクコード１０９ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_110, /* 月次ランクコード１１０ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_111, /* 月次ランクコード１１１ */
                    h_ms_kazoku_seido_buff.getuji_rank_cd_112  /* 月次ランクコード１１２ */);
        }

        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : kazoku_id=%d\n", p_kazoku_id);
            C_DbgMsg("C_GetFamilyRank : sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

        if (sqlca.sqlcode == C_const_Ora_OK) {
            /* データあり */

            if (w_honnen == 0) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_0.intVal();  /* 年次ランクコード０
                 */
            } else if (w_honnen == 1) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_1.intVal();  /* 年次ランクコード１     */
            } else if (w_honnen == 2) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_2.intVal();  /* 年次ランクコード２     */
            } else if (w_honnen == 3) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_3.intVal();  /* 年次ランクコード３     */
            } else if (w_honnen == 4) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_4.intVal();  /* 年次ランクコード４     */
            } else if (w_honnen == 5) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_5.intVal();  /* 年次ランクコード５     */
            } else if (w_honnen == 6) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_6.intVal();  /* 年次ランクコード６     */
            } else if (w_honnen == 7) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_7.intVal();  /* 年次ランクコード７     */
            } else if (w_honnen == 8) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_8.intVal();  /* 年次ランクコード８     */
            } else if (w_honnen == 9) {
                p_kazoku_honnen_rank_cd.arr = h_ms_kazoku_seido_buff.nenji_rank_cd_9.intVal();  /* 年次ランクコード９     */
            }
            if ((w_honnen % 2) == 0) {
                /* 偶数年 */
                if (w_tougetsu == 1)
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_001.intVal(); /* 月次ランクコード００１ */
            } else if (w_tougetsu == 2) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_002.intVal(); /* 月次ランクコード００２ */
            } else if (w_tougetsu == 3) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_003.intVal(); /* 月次ランクコード００３ */
            } else if (w_tougetsu == 4) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_004.intVal(); /* 月次ランクコード００４ */
            } else if (w_tougetsu == 5) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_005.intVal(); /* 月次ランクコード００５ */
            } else if (w_tougetsu == 6) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_006.intVal(); /* 月次ランクコード００６ */
            } else if (w_tougetsu == 7) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_007.intVal(); /* 月次ランクコード００７ */
            } else if (w_tougetsu == 8) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_008.intVal(); /* 月次ランクコード００８ */
            } else if (w_tougetsu == 9) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_009.intVal(); /* 月次ランクコード００９ */
            } else if (w_tougetsu == 10) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_010.intVal(); /* 月次ランクコード０１０ */
            } else if (w_tougetsu == 11) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_011.intVal(); /* 月次ランクコード０１１ */
            } else if (w_tougetsu == 12) {
                p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_012.intVal(); /* 月次ランクコード０１２ */
            } else {
                /* 奇数年 */
                if (w_tougetsu == 1) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_101.intVal(); /* 月次ランクコード１０１ */
                } else if (w_tougetsu == 2) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_102.intVal(); /* 月次ランクコード１０２ */
                } else if (w_tougetsu == 3) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_103.intVal(); /* 月次ランクコード１０３ */
                } else if (w_tougetsu == 4) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_104.intVal(); /* 月次ランクコード１０４ */
                } else if (w_tougetsu == 5) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_105.intVal(); /* 月次ランクコード１０５ */
                } else if (w_tougetsu == 6) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_106.intVal(); /* 月次ランクコード１０６ */
                } else if (w_tougetsu == 7) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_107.intVal(); /* 月次ランクコード１０７ */
                } else if (w_tougetsu == 8) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_108.intVal(); /* 月次ランクコード１０８ */
                } else if (w_tougetsu == 9) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_109.intVal(); /* 月次ランクコード１０９ */
                } else if (w_tougetsu == 10) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_110.intVal(); /* 月次ランクコード１１０ */
                } else if (w_tougetsu == 11) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_111.intVal(); /* 月次ランクコード１１１ */
                } else if (w_tougetsu == 12) {
                    p_kazoku_tougetsu_rank_cd.arr = h_ms_kazoku_seido_buff.getuji_rank_cd_112.intVal(); /* 月次ランクコード１１２ */
                }
            }
            rtn_cd = C_const_OK;
            status.arr = C_const_Stat_OK;

        } else if (sqlca.sqlcode == C_const_Ora_NOTFOUND) {
            /* データなし */
            p_kazoku_honnen_rank_cd.arr = 0;
            p_kazoku_tougetsu_rank_cd.arr = 0;
            rtn_cd = C_const_NOTEXISTS;
            status.arr = C_const_Stat_OK;

        } else {
            /* DBERR */
            sprintf(out_format_buf, "家族ＩＤ=%d", p_kazoku_id);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MS家族制度情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            rtn_cd = C_const_NG;
        }

        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : %s\n", "end");
        }

        return rtn_cd;

        /*-----C_GetFamilyRank Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_GetRank                                                   */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_GetRank(double rankup_taisho_kingaku, int *p_rank_cd,           */
    /*                                           int *status)                     */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ランクコード設定処理                                          */
    /*              引数のランクアップ対象金額よりランクを設定する                */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              double  rankup_taisho_kingaku   ： (i)ランクアップ対象金額    */
    /*              int   kubun                     ： (i)ランク種別              */
    /*                                       年／月区分　（１：月　／　２：年）   */
    /*              int   * p_rank_cd               ： (o)設定ランクコード        */
    /*              int   * status                  ： (o)結果ステータス          */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_GetRank(double rankup_taisho_kingaku, int kubun, IntegerDto p_rank_cd, IntegerDto status) {
        int rtn_cd;
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;

        /* ホスト変数 */
        /* EXEC SQL BEGIN DECLARE SECTION; */

        double h_kingaku;                       /* ランクアップ対象金額      */
        IntegerDto h_rank_cd = new IntegerDto();                       /* ランクコード              */
        int h_rank_shubetsu;                 /* ランク種別                */
        /* EXEC SQL END DECLARE SECTION; */


        if (DBG_LOG) {
            C_DbgMsg("C_GetRank : %s\n", "start");
        }
        /* 引数のチェックを行う */
        if ((p_rank_cd == null || status == null ||
                kubun < 1 || kubun > 2)) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetRank : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_GetRank : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 初期化 */
        p_rank_cd.arr = 1;
        h_rank_cd.arr = 0;

        /* 引数のランクアップ対象金額をキーに、MSランク別ボーナスポイント情報より */
        /* ランクコードを検索する                               */

        /* ホスト変数に設定する */
        h_kingaku = rankup_taisho_kingaku;
        h_rank_shubetsu = kubun;

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを実行する */
        if (strcmp(Cg_ORASID, penv) == 0) {
            /* 顧客制度データベースに接続している場合 */
            /* 2023/11/27 MCCMPH2 MOD START */
            sqlca.sql.arr = "SELECT NVL(MAX(ランクコード), 0)" +
                    "                 FROM  MSランク別ボーナスポイント情報" +
                    "                 WHERE ランク種別    = ?" +
                    "                 AND   必要金額     <= ?";
        /* EXEC SQL SELECT NVL(MAX(ランクコード), 0)
                 INTO   :h_rank_cd
                 FROM  MSランク別ボーナスポイント情報
                 WHERE ランク種別    = :h_rank_shubetsu
                 AND   必要金額     <= :h_kingaku; */
            sqlca.restAndExecute(h_rank_shubetsu, h_kingaku);
        } else {
            /* 顧客制度データベース以外に接続している場合 */
        /* EXEC SQL SELECT NVL(MAX(ランクコード), 0)
        /o 2023/11/27 MCCMPH2 MOD END o/
                 INTO   :h_rank_cd
                 FROM  MSランク別ボーナスポイント情報@CMSD
                 WHERE ランク種別    = :h_rank_shubetsu
                 AND   必要金額     <= :h_kingaku; */
            sqlca.sql.arr = "SELECT NVL(MAX(ランクコード), 0)" +
                    "FROM  MSランク別ボーナスポイント情報" +
                    "                 WHERE ランク種別    = ?" +
                    "                 AND   必要金額     <= ?";
            sqlca.restAndExecute(h_rank_shubetsu, h_kingaku);
        }
        sqlca.fetchInto(h_rank_cd);
        if (DBG_LOG) {
            C_DbgMsg("C_GetRank : h_rank_shubetsu=%d\n", h_rank_shubetsu);
            C_DbgMsg("C_GetRank : h_kingaku=%f\n", h_kingaku);
            C_DbgMsg("C_GetRank : h_rank_cd=%d\n", h_rank_cd);
            C_DbgMsg("C_GetRank : sqlca.sqlcode=%d\n", sqlca.sqlcode);
        }

        if (sqlca.sqlcode == C_const_Ora_OK) {
            /* データあり */
            p_rank_cd.arr = h_rank_cd.arr;
            rtn_cd = C_const_OK;
            status.arr = C_const_Stat_OK;
        } else {
            /* DBERR */
            sprintf(out_format_buf, "必要金額=%f", h_kingaku);
            APLOG_WT("904", 0, null, "SELECT", sqlca.sqlcode,
                    "MSランク別ボーナスポイント情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            rtn_cd = C_const_NG;
        }

        if (DBG_LOG) {
            C_DbgMsg("C_GetRank : %s\n", "end");
        }
        return rtn_cd;
        /*-----C_GetRank Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_UpdateYearPoint                                           */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_UpdateYearPoint(YEAR_POINT_DATA *yearPointData, int *year,      */
    /*                            int *status )                                   */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ポイント年別情報更新処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              YEAR_POINT_DATA * yearPointData ： ポイント年別情報構造体取得 */
    /*                                                 パラメータ                 */
    /*              int           year   ： 更新対象テーブル年(YYYY)              */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_UpdateYearPoint(TS_YEAR_POINT_TBL yearPointData, int year, int month, IntegerDto status) {

        int rtn_cd;
        StringDto w_bat_mm = new StringDto(3);   /* バッチ処理月     */
        StringDto w_mm_full = new StringDto(6);  /* バッチ処理月全角 */
        StringDto wk_item1 = new StringDto(512); /* 項目名編集バッファ 月間付与ポイントMM */
        StringDto wk_item2 = new StringDto(512); /* 項目名編集バッファ 月間利用ポイントMM */
        StringDto wk_item3 = new StringDto(512); /* 項目名編集バッファ 月間ランクＵＰ対象金額MM */
        StringDto wk_item4 = new StringDto(512); /* 項目名編集バッファ 月間買上回数MM */
        StringDto tbl_nam = new StringDto(200);
        StringDto sql_buf = new StringDto(4096);     /* ＳＱＬ文編集用 */
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        TS_YEAR_POINT_TBL h_ts_year_point_buff; /* TSポイント年別情報 */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_UpdateYearPoint : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (yearPointData == null || year == 0 || status == null ||
                month < 1 || month > 12) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }



        /* ホスト変数を編集する */
//        memset(  h_ts_year_point_buff, 0x00, sizeof(TS_YEAR_POINT_TBL));
        h_ts_year_point_buff = new TS_YEAR_POINT_TBL();
        memset(h_ts_year_point_buff, 0x00, 0);
        memcpy(h_ts_year_point_buff.kokyaku_no,
                yearPointData.kokyaku_no, yearPointData.kokyaku_no.len);
        h_ts_year_point_buff.kokyaku_no.len = yearPointData.kokyaku_no.len;
        h_ts_year_point_buff.year.arr = year;

        /* ホスト変数(処理月)を編集する */
        memset(w_bat_mm, 0x00, sizeof(w_bat_mm));
        memset(w_mm_full, 0x00, sizeof(w_mm_full));

        sprintf(w_bat_mm, "%02d", month);
        rtn_cd = C_ConvHalf2Full(w_bat_mm, w_mm_full);
        if (rtn_cd != C_const_OK) {
            APLOG_WT("903", 0, null, "C_ConvHalf2Full", rtn_cd, 0, 0, 0, 0);
            return C_const_NG;
        }

        if (DBG_LOG) {
            C_DbgMsg("C_GetFamilyRank : バッチ処理月=%s\n", w_mm_full);
        }

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを生成する */
        /* テーブル名編集 */
        sprintf(tbl_nam, "TSポイント年別情報%d", year);
        if (strcmp(Cg_ORASID, penv) != 0) {
            strcat(tbl_nam, "");
        }

        /* 項目名編集 */
        memset(wk_item1, 0x00, sizeof(wk_item1));
        memset(wk_item2, 0x00, sizeof(wk_item2));
        memset(wk_item3, 0x00, sizeof(wk_item3));
        memset(wk_item4, 0x00, sizeof(wk_item4));
        sprintf(wk_item1, "月間付与ポイント%s       = 月間付与ポイント%s       + ?,", w_mm_full, w_mm_full);
        sprintf(wk_item2, "月間利用ポイント%s       = 月間利用ポイント%s       + ?,", w_mm_full, w_mm_full);
        sprintf(wk_item3, "月間ランクＵＰ対象金額%s = 月間ランクＵＰ対象金額%s + ?,", w_mm_full, w_mm_full);
        sprintf(wk_item4, "月間買上回数%s           = 月間買上回数%s           + ?,", w_mm_full, w_mm_full);

        /* ホスト変数設定 */
        h_ts_year_point_buff.year = yearPointData.year;                             /* 年                             */
        /*h_ts_year_point_buff.month                            = yearPointData->month;                             月                             */
        h_ts_year_point_buff.nenkan_fuyo_point = yearPointData.nenkan_fuyo_point;                /* 年間付与ポイント               */
        h_ts_year_point_buff.nenkan_riyo_point = yearPointData.nenkan_riyo_point;                /* 年間利用ポイント               */
        h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point = yearPointData.nenkan_kihon_pritsu_taisho_point; /* 年間基本Ｐ率対象ポイント       */
        h_ts_year_point_buff.nenkan_rankup_taisho_kingaku = yearPointData.nenkan_rankup_taisho_kingaku;     /* 年間ランクＵＰ対象金額         */
        h_ts_year_point_buff.nenkan_point_taisho_kingaku = yearPointData.nenkan_point_taisho_kingaku;      /* 年間ポイント対象金額           */
        h_ts_year_point_buff.nenkan_kaiage_kingaku = yearPointData.nenkan_kaiage_kingaku;            /* 年間買上額                     */
        h_ts_year_point_buff.nenkan_kaiage_cnt = yearPointData.nenkan_kaiage_cnt;                /* 年間買上回数                   */
        h_ts_year_point_buff.nenkan_kaiage_nissu = yearPointData.nenkan_kaiage_nissu;              /* 年間買上日数                   */
        h_ts_year_point_buff.nenkan_kaimonoken_hakko_point = yearPointData.nenkan_kaimonoken_hakko_point;    /* 年間買物券発行ポイント         */
        h_ts_year_point_buff.saishu_koshin_ymd = yearPointData.saishu_koshin_ymd;                /* 最終更新日                     */
        strcpy(h_ts_year_point_buff.saishu_koshin_programid, yearPointData.saishu_koshin_programid);         /* 最終更新プログラムＩＤ         */


        strcpy(sql_buf, "UPDATE ");
        strcat(sql_buf, tbl_nam);

        strcat(sql_buf, " SET 年間付与ポイント    = 年間付与ポイント         + ?,");
        strcat(sql_buf, "年間利用ポイント         = 年間利用ポイント         + ?,");
        strcat(sql_buf, "年間基本Ｐ率対象ポイント = 年間基本Ｐ率対象ポイント    + ?,");
        strcat(sql_buf, "年間ランクＵＰ対象金額   = 年間ランクＵＰ対象金額     + ?,");
        strcat(sql_buf, "年間ポイント対象金額     = 年間ポイント対象金額       + ?,");
        strcat(sql_buf, "年間買上額               = 年間買上額             + ?,");
        strcat(sql_buf, "年間買上回数             = 年間買上回数            + ?,");
        strcat(sql_buf, "年間買上日数             = 年間買上日数            + ?,");
        strcat(sql_buf, "年間買物券発行ポイント   = 年間買物券発行ポイント      + ?,");
        strcat(sql_buf, wk_item1);
        strcat(sql_buf, wk_item2);
        strcat(sql_buf, wk_item3);
        strcat(sql_buf, wk_item4);
        strcat(sql_buf, "最終更新日               =                            ?,");
        strcat(sql_buf, "最終更新日時             =                         SYSDATE(),");
        strcat(sql_buf, "最終更新プログラムＩＤ   =                             ?");
        strcat(sql_buf, " WHERE 年 = ? AND ");
        strcat(sql_buf, "顧客番号 = ?");


        if (DBG_LOG) {
            C_DbgMsg("C_UpdateYearPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);


        sqlca.sql = WRKSQL;
//        EXEC SQL PREPARE sql_kdatalock3 from:
//        WRKSQL;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年 =[%d], 顧客番号=[%s]",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE PREPARE",
                    sqlca.sqlcode, tbl_nam,
                    out_format_buf, 0, 0);
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* UPDATE文を実行する */
        sqlca.restAndExecute(h_ts_year_point_buff.nenkan_fuyo_point.longVal(),                 /* 年間付与ポイント               */
                h_ts_year_point_buff.nenkan_riyo_point.longVal(),                 /* 年間利用ポイント               */
                h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point.longVal(),  /* 年間基本Ｐ率対象ポイント       */
                h_ts_year_point_buff.nenkan_rankup_taisho_kingaku.longVal(),      /* 年間ランクＵＰ対象金額         */
                h_ts_year_point_buff.nenkan_point_taisho_kingaku.longVal(),       /* 年間ポイント対象金額           */
                h_ts_year_point_buff.nenkan_kaiage_kingaku.longVal(),             /* 年間買上額                     */
                h_ts_year_point_buff.nenkan_kaiage_cnt.longVal(),                 /* 年間買上回数                   */
                h_ts_year_point_buff.nenkan_kaiage_nissu.longVal(),               /* 年間買上日数                   */
                h_ts_year_point_buff.nenkan_kaimonoken_hakko_point.longVal(),     /* 年間買物券発行ポイント         */
                h_ts_year_point_buff.nenkan_fuyo_point.longVal(),                 /* 年間付与ポイント               */
                h_ts_year_point_buff.nenkan_riyo_point.longVal(),                 /* 年間利用ポイント               */
                h_ts_year_point_buff.nenkan_rankup_taisho_kingaku.longVal(),      /* 年間ランクＵＰ対象金額         */
                h_ts_year_point_buff.nenkan_kaiage_cnt.longVal(),                 /* 年間買上回数                   */
                h_ts_year_point_buff.saishu_koshin_ymd.longVal(),                 /* 最終更新日                     */
                h_ts_year_point_buff.saishu_koshin_programid,           /* 最終更新プログラムＩＤ         */
                h_ts_year_point_buff.year.longVal(),                              /* 年                             */
                h_ts_year_point_buff.kokyaku_no.longVal());                        /* 顧客番号                       */


        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode !=
                C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年 =[%d],顧客番号=[%s]",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    tbl_nam, out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 正常の場合終了 */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* 戻り値の設定 */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : %s\n", "end");
            }

            status.arr = C_const_Stat_OK;
            return C_const_OK;
        }


        /* 追加用ホスト変数設定 */
        if ((atoi(w_bat_mm) == 1)) {
            h_ts_year_point_buff.gekkan_fuyo_point_01 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０１           */
            h_ts_year_point_buff.gekkan_riyo_point_01 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０１           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０１     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_01 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０１               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_01.arr = 0;
            /* 月間付与ポイント０１           */
            h_ts_year_point_buff.gekkan_riyo_point_01.arr = 0;
            /* 月間利用ポイント０１           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01.arr = 0;                                              /* 月間ランクＵＰ対象金額０１     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_01.arr = 0;                                                         /* 月間買上回数０１               */
        }

        if ((atoi(w_bat_mm) == 2)) {
            h_ts_year_point_buff.gekkan_fuyo_point_02 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０２           */
            h_ts_year_point_buff.gekkan_riyo_point_02 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０２           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０２     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_02 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０２               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_02.arr = 0;                                                         /* 月間付与ポイント０２           */
            h_ts_year_point_buff.gekkan_riyo_point_02.arr = 0;                                                         /* 月間利用ポイント０２           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02.arr = 0;                                              /* 月間ランクＵＰ対象金額０２     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_02.arr = 0;                                                         /* 月間買上回数０２               */
        }

        if ((atoi(w_bat_mm) == 3)) {
            h_ts_year_point_buff.gekkan_fuyo_point_03 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０３           */
            h_ts_year_point_buff.gekkan_riyo_point_03 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０３           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０３     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_03 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０３               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_03.arr = 0;                                                         /* 月間付与ポイント０３           */
            h_ts_year_point_buff.gekkan_riyo_point_03.arr = 0;                                                         /* 月間利用ポイント０３           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03.arr = 0;                                              /* 月間ランクＵＰ対象金額０３     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_03.arr = 0;                                                         /* 月間買上回数０３               */
        }

        if ((atoi(w_bat_mm) == 4)) {
            h_ts_year_point_buff.gekkan_fuyo_point_04 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０４           */
            h_ts_year_point_buff.gekkan_riyo_point_04 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０４           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０４     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_04 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０４               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_04.arr = 0;                                                         /* 月間付与ポイント０４           */
            h_ts_year_point_buff.gekkan_riyo_point_04.arr = 0;                                                         /* 月間利用ポイント０４           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04.arr = 0;                                              /* 月間ランクＵＰ対象金額０４     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_04.arr = 0;                                                         /* 月間買上回数０４               */
        }

        if ((atoi(w_bat_mm) == 5)) {
            h_ts_year_point_buff.gekkan_fuyo_point_05 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０５           */
            h_ts_year_point_buff.gekkan_riyo_point_05 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０５           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０５     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_05 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０５               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_05.arr = 0;                                                         /* 月間付与ポイント０５           */
            h_ts_year_point_buff.gekkan_riyo_point_05.arr = 0;                                                         /* 月間利用ポイント０５           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05.arr = 0;                                              /* 月間ランクＵＰ対象金額０５     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_05.arr = 0;                                                         /* 月間買上回数０５               */
        }

        if ((atoi(w_bat_mm) == 6)) {
            h_ts_year_point_buff.gekkan_fuyo_point_06 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０６           */
            h_ts_year_point_buff.gekkan_riyo_point_06 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０６           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０６     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_06 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０６               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_06.arr = 0;                                                         /* 月間付与ポイント０６           */
            h_ts_year_point_buff.gekkan_riyo_point_06.arr = 0;                                                         /* 月間利用ポイント０６           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06.arr = 0;                                              /* 月間ランクＵＰ対象金額０６     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_06.arr = 0;                                                         /* 月間買上回数０６               */
        }

        if ((atoi(w_bat_mm) == 7)) {
            h_ts_year_point_buff.gekkan_fuyo_point_07 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０７           */
            h_ts_year_point_buff.gekkan_riyo_point_07 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０７           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０７     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_07 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０７               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_07.arr = 0;                                                         /* 月間付与ポイント０７           */
            h_ts_year_point_buff.gekkan_riyo_point_07.arr = 0;                                                         /* 月間利用ポイント０７           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07.arr = 0;                                              /* 月間ランクＵＰ対象金額０７     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_07.arr = 0;                                                         /* 月間買上回数０７               */
        }

        if ((atoi(w_bat_mm) == 8)) {
            h_ts_year_point_buff.gekkan_fuyo_point_08 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０８           */
            h_ts_year_point_buff.gekkan_riyo_point_08 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０８           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０８     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_08 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０８               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_08.arr = 0;                                                         /* 月間付与ポイント０８           */
            h_ts_year_point_buff.gekkan_riyo_point_08.arr = 0;                                                         /* 月間利用ポイント０８           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08.arr = 0;                                              /* 月間ランクＵＰ対象金額０８     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_08.arr = 0;                                                         /* 月間買上回数０８               */
        }

        if ((atoi(w_bat_mm) == 9)) {
            h_ts_year_point_buff.gekkan_fuyo_point_09 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント０９           */
            h_ts_year_point_buff.gekkan_riyo_point_09 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント０９           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額０９     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_09 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数０９               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_09.arr = 0;                                                         /* 月間付与ポイント０９           */
            h_ts_year_point_buff.gekkan_riyo_point_09.arr = 0;                                                         /* 月間利用ポイント０９           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09.arr = 0;                                              /* 月間ランクＵＰ対象金額０９     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_09.arr = 0;                                                         /* 月間買上回数０９               */
        }

        if ((atoi(w_bat_mm) == 10)) {
            h_ts_year_point_buff.gekkan_fuyo_point_10 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント１０           */
            h_ts_year_point_buff.gekkan_riyo_point_10 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント１０           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額１０     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_10 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数１０               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_10.arr = 0;                                                         /* 月間付与ポイント１０           */
            h_ts_year_point_buff.gekkan_riyo_point_10.arr = 0;                                                         /* 月間利用ポイント１０           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10.arr = 0;                                              /* 月間ランクＵＰ対象金額１０     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_10.arr = 0;                                                         /* 月間買上回数１０               */
        }

        if ((atoi(w_bat_mm) == 11)) {
            h_ts_year_point_buff.gekkan_fuyo_point_11 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント１１           */
            h_ts_year_point_buff.gekkan_riyo_point_11 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント１１           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額１１     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_11 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数１１               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_11.arr = 0;                                                         /* 月間付与ポイント１１           */
            h_ts_year_point_buff.gekkan_riyo_point_11.arr = 0;                                                         /* 月間利用ポイント１１           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11.arr = 0;                                              /* 月間ランクＵＰ対象金額１１     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_11.arr = 0;                                                         /* 月間買上回数１１               */
        }

        if ((atoi(w_bat_mm) == 12)) {
            h_ts_year_point_buff.gekkan_fuyo_point_12 = yearPointData.nenkan_fuyo_point;                       /* 月間付与ポイント１２           */
            h_ts_year_point_buff.gekkan_riyo_point_12 = yearPointData.nenkan_riyo_point;                       /* 月間利用ポイント１２           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12 = yearPointData.nenkan_rankup_taisho_kingaku; /* 月間ランクＵＰ対象金額１２     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_12 = yearPointData.nenkan_kaiage_cnt;                       /* 月間買上回数１２               */
        } else {
            h_ts_year_point_buff.gekkan_fuyo_point_12.arr = 0;                                                         /* 月間付与ポイント１２           */
            h_ts_year_point_buff.gekkan_riyo_point_12.arr = 0;                                                         /* 月間利用ポイント１２           */
            h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12.arr = 0;                                              /* 月間ランクＵＰ対象金額１２     */
            h_ts_year_point_buff.gekkan_kaiage_cnt_12.arr = 0;                                                         /* 月間買上回数１２               */
        }

        /* ＳＱＬを生成する */
        strcpy(sql_buf, "INSERT INTO ");
        strcat(sql_buf, tbl_nam);

        strcat(sql_buf, " (年,");
        strcat(sql_buf, "顧客番号,");
        strcat(sql_buf, "年間付与ポイント,");
        strcat(sql_buf, "年間利用ポイント,");
        strcat(sql_buf, "年間基本Ｐ率対象ポイント,");
        strcat(sql_buf, "年間ランクＵＰ対象金額,");
        strcat(sql_buf, "年間ポイント対象金額,");
        strcat(sql_buf, "年間買上額,");
        strcat(sql_buf, "年間買上回数,");
        strcat(sql_buf, "年間買上日数,");
        strcat(sql_buf, "年間買物券発行ポイント,");
        strcat(sql_buf, "月間付与ポイント０１,");
        strcat(sql_buf, "月間付与ポイント０２,");
        strcat(sql_buf, "月間付与ポイント０３,");
        strcat(sql_buf, "月間付与ポイント０４,");
        strcat(sql_buf, "月間付与ポイント０５,");
        strcat(sql_buf, "月間付与ポイント０６,");
        strcat(sql_buf, "月間付与ポイント０７,");
        strcat(sql_buf, "月間付与ポイント０８,");
        strcat(sql_buf, "月間付与ポイント０９,");
        strcat(sql_buf, "月間付与ポイント１０,");
        strcat(sql_buf, "月間付与ポイント１１,");
        strcat(sql_buf, "月間付与ポイント１２,");
        strcat(sql_buf, "月間利用ポイント０１,");
        strcat(sql_buf, "月間利用ポイント０２,");
        strcat(sql_buf, "月間利用ポイント０３,");
        strcat(sql_buf, "月間利用ポイント０４,");
        strcat(sql_buf, "月間利用ポイント０５,");
        strcat(sql_buf, "月間利用ポイント０６,");
        strcat(sql_buf, "月間利用ポイント０７,");
        strcat(sql_buf, "月間利用ポイント０８,");
        strcat(sql_buf, "月間利用ポイント０９,");
        strcat(sql_buf, "月間利用ポイント１０,");
        strcat(sql_buf, "月間利用ポイント１１,");
        strcat(sql_buf, "月間利用ポイント１２,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０１,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０２,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０３,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０４,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０５,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０６,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０７,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０８,");
        strcat(sql_buf, "月間ランクＵＰ対象金額０９,");
        strcat(sql_buf, "月間ランクＵＰ対象金額１０,");
        strcat(sql_buf, "月間ランクＵＰ対象金額１１,");
        strcat(sql_buf, "月間ランクＵＰ対象金額１２,");
        strcat(sql_buf, "月間買上回数０１,");
        strcat(sql_buf, "月間買上回数０２,");
        strcat(sql_buf, "月間買上回数０３,");
        strcat(sql_buf, "月間買上回数０４,");
        strcat(sql_buf, "月間買上回数０５,");
        strcat(sql_buf, "月間買上回数０６,");
        strcat(sql_buf, "月間買上回数０７,");
        strcat(sql_buf, "月間買上回数０８,");
        strcat(sql_buf, "月間買上回数０９,");
        strcat(sql_buf, "月間買上回数１０,");
        strcat(sql_buf, "月間買上回数１１,");
        strcat(sql_buf, "月間買上回数１２,");
        strcat(sql_buf, "最終更新日,");
        strcat(sql_buf, "最終更新日時,");
        strcat(sql_buf, "最終更新プログラムＩＤ) VALUES (");

        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "SYSDATE(),");
        strcat(sql_buf, "?)");

        if (DBG_LOG) {
            C_DbgMsg("C_UpdateYearPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);

//        EXEC SQL PREPARE sql_kdatalock6 from:
//        WRKSQL;
        sqlca.sql = WRKSQL;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年 =[%d],顧客番号=[%s]",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT PREPARE",
                    sqlca.sqlcode, tbl_nam,
                    out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* INSERT文を実行する */
        sqlca.restAndExecute(h_ts_year_point_buff.year,                             /* 年                             */
                h_ts_year_point_buff.kokyaku_no,                       /* 顧客番号                       */
                h_ts_year_point_buff.nenkan_fuyo_point,                /* 年間付与ポイント               */
                h_ts_year_point_buff.nenkan_riyo_point,                /* 年間利用ポイント               */
                h_ts_year_point_buff.nenkan_kihon_pritsu_taisho_point, /* 年間基本Ｐ率対象ポイント       */
                h_ts_year_point_buff.nenkan_rankup_taisho_kingaku,     /* 年間ランクＵＰ対象金額         */
                h_ts_year_point_buff.nenkan_point_taisho_kingaku,      /* 年間ポイント対象金額           */
                h_ts_year_point_buff.nenkan_kaiage_kingaku,            /* 年間買上額                     */
                h_ts_year_point_buff.nenkan_kaiage_cnt,                /* 年間買上回数                   */
                h_ts_year_point_buff.nenkan_kaiage_nissu,              /* 年間買上日数                   */
                h_ts_year_point_buff.nenkan_kaimonoken_hakko_point,    /* 年間買物券発行ポイント         */
                h_ts_year_point_buff.gekkan_fuyo_point_01,             /* 月間付与ポイント０１           */
                h_ts_year_point_buff.gekkan_fuyo_point_02,             /* 月間付与ポイント０２           */
                h_ts_year_point_buff.gekkan_fuyo_point_03,             /* 月間付与ポイント０３           */
                h_ts_year_point_buff.gekkan_fuyo_point_04,             /* 月間付与ポイント０４           */
                h_ts_year_point_buff.gekkan_fuyo_point_05,             /* 月間付与ポイント０５           */
                h_ts_year_point_buff.gekkan_fuyo_point_06,             /* 月間付与ポイント０６           */
                h_ts_year_point_buff.gekkan_fuyo_point_07,             /* 月間付与ポイント０７           */
                h_ts_year_point_buff.gekkan_fuyo_point_08,             /* 月間付与ポイント０８           */
                h_ts_year_point_buff.gekkan_fuyo_point_09,             /* 月間付与ポイント０９           */
                h_ts_year_point_buff.gekkan_fuyo_point_10,             /* 月間付与ポイント１０           */
                h_ts_year_point_buff.gekkan_fuyo_point_11,             /* 月間付与ポイント１１           */
                h_ts_year_point_buff.gekkan_fuyo_point_12,             /* 月間付与ポイント１２           */
                h_ts_year_point_buff.gekkan_riyo_point_01,             /* 月間利用ポイント０１           */
                h_ts_year_point_buff.gekkan_riyo_point_02,             /* 月間利用ポイント０２           */
                h_ts_year_point_buff.gekkan_riyo_point_03,             /* 月間利用ポイント０３           */
                h_ts_year_point_buff.gekkan_riyo_point_04,             /* 月間利用ポイント０４           */
                h_ts_year_point_buff.gekkan_riyo_point_05,             /* 月間利用ポイント０５           */
                h_ts_year_point_buff.gekkan_riyo_point_06,             /* 月間利用ポイント０６           */
                h_ts_year_point_buff.gekkan_riyo_point_07,             /* 月間利用ポイント０７           */
                h_ts_year_point_buff.gekkan_riyo_point_08,             /* 月間利用ポイント０８           */
                h_ts_year_point_buff.gekkan_riyo_point_09,             /* 月間利用ポイント０９           */
                h_ts_year_point_buff.gekkan_riyo_point_10,             /* 月間利用ポイント１０           */
                h_ts_year_point_buff.gekkan_riyo_point_11,             /* 月間利用ポイント１１           */
                h_ts_year_point_buff.gekkan_riyo_point_12,             /* 月間利用ポイント１２           */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_01,  /* 月間ランクＵＰ対象金額０１     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_02,  /* 月間ランクＵＰ対象金額０２     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_03,  /* 月間ランクＵＰ対象金額０３     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_04,  /* 月間ランクＵＰ対象金額０４     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_05,  /* 月間ランクＵＰ対象金額０５     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_06,  /* 月間ランクＵＰ対象金額０６     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_07,  /* 月間ランクＵＰ対象金額０７     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_08,  /* 月間ランクＵＰ対象金額０８     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_09,  /* 月間ランクＵＰ対象金額０９     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_10,  /* 月間ランクＵＰ対象金額１０     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_11,  /* 月間ランクＵＰ対象金額１１     */
                h_ts_year_point_buff.gekkan_rankup_taisho_kingaku_12,  /* 月間ランクＵＰ対象金額１２     */
                h_ts_year_point_buff.gekkan_kaiage_cnt_01,             /* 月間買上回数０１               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_02,             /* 月間買上回数０２               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_03,             /* 月間買上回数０３               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_04,             /* 月間買上回数０４               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_05,             /* 月間買上回数０５               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_06,             /* 月間買上回数０６               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_07,             /* 月間買上回数０７               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_08,             /* 月間買上回数０８               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_09,             /* 月間買上回数０９               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_10,             /* 月間買上回数１０               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_11,             /* 月間買上回数１１               */
                h_ts_year_point_buff.gekkan_kaiage_cnt_12,             /* 月間買上回数１２               */
                h_ts_year_point_buff.saishu_koshin_ymd,                /* 最終更新日                     */
                h_ts_year_point_buff.saishu_koshin_programid);          /* 最終更新プログラムＩＤ         */
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateYearPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "年 =[%d],顧客番号=[%s]",
                    h_ts_year_point_buff.year,
                    h_ts_year_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    tbl_nam, out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_UpdateYearPoint : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_UpdateYearPoint Bottom----------------------------------------------*/
    }

/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_UpdateTotalPoint                                          */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_UpdateTotalPoint(TOTAL_POINT_DATA *totalPointData, int *status) */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              ポイント累計情報更新処理                                      */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              TOTAL_POINT_DATA * totalPointData：ポイント累計情報構造体取得 */
    /*                                                 パラメータ                 */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_UpdateTotalPoint(TS_TOTAL_POINT_TBL totalPointData, IntegerDto status) {

        StringDto tbl_nam = new StringDto(200);
        StringDto sql_buf = new StringDto(4096);     /* ＳＱＬ文編集用 */
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        TS_TOTAL_POINT_TBL h_ts_total_point_buff; /* TSポイント累計情報 */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_UpdateTotalPoint : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (totalPointData == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateTotalPoint : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateTotalPoint : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }



        /* ホスト変数を編集する */
        h_ts_total_point_buff = new TS_TOTAL_POINT_TBL();
        memset(h_ts_total_point_buff, 0x00, 0);
        memcpy(h_ts_total_point_buff.kokyaku_no,
                totalPointData.kokyaku_no, totalPointData.kokyaku_no.len);
        h_ts_total_point_buff.kokyaku_no.len = totalPointData.kokyaku_no.len;

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを生成する */
        /* テーブル名編集 */
        strcpy(tbl_nam, "TSポイント累計情報");
        if (strcmp(Cg_ORASID, penv) != 0) {
            strcat(tbl_nam, "");
        }

        /* ホスト変数設定 */
        h_ts_total_point_buff.ruike_fuyo_point = totalPointData.ruike_fuyo_point;                /* 累計付与ポイント               */
        h_ts_total_point_buff.ruike_riyo_point = totalPointData.ruike_riyo_point;                /* 累計利用ポイント               */
        h_ts_total_point_buff.ruike_kihon_pritsu_taisho_point = totalPointData.ruike_kihon_pritsu_taisho_point; /* 累計基本Ｐ率対象ポイント       */
        h_ts_total_point_buff.ruike_rankup_taisho_kingaku = totalPointData.ruike_rankup_taisho_kingaku;     /* 累計ランクＵＰ対象金額         */
        h_ts_total_point_buff.ruike_point_taisho_kingaku = totalPointData.ruike_point_taisho_kingaku;      /* 累計ポイント対象金額           */
        h_ts_total_point_buff.ruike_kaiage_kingaku = totalPointData.ruike_kaiage_kingaku;            /* 累計買上額                     */
        h_ts_total_point_buff.ruike_kaiage_cnt = totalPointData.ruike_kaiage_cnt;                /* 累計買上回数                   */
        h_ts_total_point_buff.ruike_kaiage_nissu = totalPointData.ruike_kaiage_nissu;              /* 累計買上日数                   */
        h_ts_total_point_buff.saishu_koshin_ymd = totalPointData.saishu_koshin_ymd;               /* 最終更新日                     */
        strcpy(h_ts_total_point_buff.saishu_koshin_programid, totalPointData.saishu_koshin_programid);          /* 最終更新プログラムＩＤ         */
        h_ts_total_point_buff.ruike_fuyo_tsujo_point = totalPointData.ruike_fuyo_tsujo_point;          /* 累計付与通常Ｐ                 */
        h_ts_total_point_buff.ruike_riyo_tsujo_point = totalPointData.ruike_riyo_tsujo_point;          /* 累計利用通常Ｐ                 */
        h_ts_total_point_buff.ruike_fuyo_kikan_gentei_point = totalPointData.ruike_fuyo_kikan_gentei_point;   /* 累計付与期間限定Ｐ             */
        h_ts_total_point_buff.ruike_riyo_kikan_gentei_point = totalPointData.ruike_riyo_kikan_gentei_point;   /* 累計利用期間限定Ｐ             */


        strcpy(sql_buf, "UPDATE ");
        strcat(sql_buf, tbl_nam);


        strcat(sql_buf, " SET 累計付与ポイント    = 累計付与ポイント         + ?,");
        strcat(sql_buf, "累計利用ポイント         = 累計利用ポイント         + ?,");
        strcat(sql_buf, "累計基本Ｐ率対象ポイント = 累計基本Ｐ率対象ポイント + ?,");
        strcat(sql_buf, "累計ランクＵＰ対象金額   = 累計ランクＵＰ対象金額   + ?,");
        strcat(sql_buf, "累計ポイント対象金額     = 累計ポイント対象金額     + ?,");
        strcat(sql_buf, "累計買上額               = 累計買上額               + ?,");
        strcat(sql_buf, "累計買上回数             = 累計買上回数             + ?,");
        strcat(sql_buf, "累計買上日数             = 累計買上日数             + ?,");
        strcat(sql_buf, "最終更新日               =                            ?,");
        strcat(sql_buf, "最終更新日時             =                        SYSDATE(),");
        strcat(sql_buf, "最終更新プログラムＩＤ   =                            ?,");
        strcat(sql_buf, "累計付与通常Ｐ           = 累計付与通常Ｐ           + ?,");
        strcat(sql_buf, "累計利用通常Ｐ           = 累計利用通常Ｐ           + ?,");
        strcat(sql_buf, "累計付与期間限定Ｐ       = 累計付与期間限定Ｐ       + ?,");
        strcat(sql_buf, "累計利用期間限定Ｐ       = 累計利用期間限定Ｐ       + ?");
        strcat(sql_buf, " WHERE 顧客番号 = ?");


        if (DBG_LOG) {
            C_DbgMsg("C_UpdateTotalPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);

        sqlca.sql = WRKSQL;

        sqlca.prepare();
//        EXEC SQL PREPARE sql_kdatalock4 from:
//        WRKSQL;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateTotalPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_total_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE PREPARE",
                    sqlca.sqlcode, "TSポイント累計情報",
                    out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* UPDATE文を実行する */
        sqlca.restAndExecute(h_ts_total_point_buff.ruike_fuyo_point.longVal(),                 /* 累計付与ポイント               */
                h_ts_total_point_buff.ruike_riyo_point.longVal(),                 /* 累計利用ポイント               */
                h_ts_total_point_buff.ruike_kihon_pritsu_taisho_point.longVal(),  /* 累計基本Ｐ率対象ポイント       */
                h_ts_total_point_buff.ruike_rankup_taisho_kingaku.longVal(),      /* 累計ランクＵＰ対象金額         */
                h_ts_total_point_buff.ruike_point_taisho_kingaku.longVal(),       /* 累計ポイント対象金額           */
                h_ts_total_point_buff.ruike_kaiage_kingaku.longVal(),             /* 累計買上額                     */
                h_ts_total_point_buff.ruike_kaiage_cnt.longVal(),                 /* 累計買上回数                   */
                h_ts_total_point_buff.ruike_kaiage_nissu.longVal(),               /* 累計買上日数                   */
                h_ts_total_point_buff.saishu_koshin_ymd.longVal(),                /* 最終更新日                     */
                h_ts_total_point_buff.saishu_koshin_programid,          /* 最終更新プログラムＩＤ         */
                h_ts_total_point_buff.ruike_fuyo_tsujo_point.longVal(),           /* 累計付与通常Ｐ                 */
                h_ts_total_point_buff.ruike_riyo_tsujo_point.longVal(),           /* 累計利用通常Ｐ                 */
                h_ts_total_point_buff.ruike_fuyo_kikan_gentei_point.longVal(),    /* 累計付与期間限定Ｐ             */
                h_ts_total_point_buff.ruike_riyo_kikan_gentei_point.longVal(),    /* 累計利用通常Ｐ                 */
                h_ts_total_point_buff.kokyaku_no.longVal());                       /* 顧客番号                       */

        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode !=
                C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateTotalPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_total_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TSポイント累計情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 正常の場合終了 */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* 戻り値の設定 */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateTotalPoint : %s\n", "end");
            }

            status.arr = C_const_Stat_OK;
            return C_const_OK;
        }

        /* ＳＱＬを生成する */
        strcpy(sql_buf, "INSERT INTO ");
        strcat(sql_buf, tbl_nam);

        strcat(sql_buf, " (顧客番号,");
        strcat(sql_buf, "累計付与ポイント,");
        strcat(sql_buf, "累計利用ポイント,");
        strcat(sql_buf, "累計基本Ｐ率対象ポイント,");
        strcat(sql_buf, "累計ランクＵＰ対象金額,");
        strcat(sql_buf, "累計ポイント対象金額,");
        strcat(sql_buf, "累計買上額,");
        strcat(sql_buf, "累計買上回数,");
        strcat(sql_buf, "累計買上日数,");
        strcat(sql_buf, "最終更新日,");
        strcat(sql_buf, "最終更新日時,");
        strcat(sql_buf, "最終更新プログラムＩＤ,");
        strcat(sql_buf, "累計付与通常Ｐ,");
        strcat(sql_buf, "累計利用通常Ｐ,");
        strcat(sql_buf, "累計付与期間限定Ｐ,");
        strcat(sql_buf, "累計利用期間限定Ｐ");
        strcat(sql_buf, ") VALUES (");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "SYSDATE(),");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?)");


        if (DBG_LOG) {
            C_DbgMsg("C_UpdateTotalPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL.arr, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);


        sqlca.sql = WRKSQL;
        sqlca.prepare();
//        EXEC SQL PREPARE sql_kdatalock5 from:
//        WRKSQL;
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_total_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT PREPARE",
                    sqlca.sqlcode, "TSポイント累計情報",
                    out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* INSERT文を実行する */
        sqlca.restAndExecute(h_ts_total_point_buff.kokyaku_no,                       /* 顧客番号                       */
                h_ts_total_point_buff.ruike_fuyo_point,                 /* 累計付与ポイント               */
                h_ts_total_point_buff.ruike_riyo_point,                 /* 累計利用ポイント               */
                h_ts_total_point_buff.ruike_kihon_pritsu_taisho_point,  /* 累計基本Ｐ率対象ポイント       */
                h_ts_total_point_buff.ruike_rankup_taisho_kingaku,      /* 累計ランクＵＰ対象金額         */
                h_ts_total_point_buff.ruike_point_taisho_kingaku,       /* 累計ポイント対象金額           */
                h_ts_total_point_buff.ruike_kaiage_kingaku,             /* 累計買上額                     */
                h_ts_total_point_buff.ruike_kaiage_cnt,                 /* 累計買上回数                   */
                h_ts_total_point_buff.ruike_kaiage_nissu,               /* 累計買上日数                   */
                h_ts_total_point_buff.saishu_koshin_ymd,                /* 最終更新日                     */
                h_ts_total_point_buff.saishu_koshin_programid,          /* 最終更新プログラムＩＤ         */
                h_ts_total_point_buff.ruike_fuyo_tsujo_point,           /* 累計付与通常Ｐ                 */
                h_ts_total_point_buff.ruike_riyo_tsujo_point,           /* 累計利用通常Ｐ                 */
                h_ts_total_point_buff.ruike_fuyo_kikan_gentei_point,    /* 累計付与期間限定Ｐ             */
                h_ts_total_point_buff.ruike_riyo_kikan_gentei_point);    /* 累計利用期間限定Ｐ             */

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_InsertDayPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_total_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "TSポイント累計情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }



        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_UpdateTotalPoint : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_UpdateTotalPoint Bottom----------------------------------------------*/
    }

    /* 2021/02/17 TS期間限定ポイント情報更新処理追加 Start */
/******************************************************************************/
    /*                                                                            */
    /*      関数名 ： C_UpdateKikanPoint                                          */
    /*                                                                            */
    /*      書式                                                                  */
    /*      int C_UpdateKikanPoint(TS_KIKAN_POINT_DATA *kikanPointData,           */
    /*                                              int *status)                  */
    /*                                                                            */
    /*      【説明】                                                              */
    /*              期間限定ポイント情報更新処理                                  */
    /*                                                                            */
    /*      【引数】                                                              */
    /*              TS_KIKAN_POINT_DATA * kikanPointData：                        */
    /*                                  期間限定ポイント情報構造体取得 パラメータ */
    /*              int         * status ： 結果ステータス                        */
    /*                                                                            */
    /*      【戻り値】                                                            */
    /*              0       ： 正常                                               */
    /*              1       ： 異常                                               */
    /*             -1       ： データなし                                         */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_UpdateKikanPoint(TS_KIKAN_POINT_TBL kikanPointData, IntegerDto status) {

        StringDto tbl_nam = new StringDto(200);
        StringDto sql_buf = new StringDto(4096);     /* ＳＱＬ文編集用 */
        StringDto out_format_buf = new StringDto(C_const_MsgMaxLen); /* APログフォーマット        */
        String penv;


        /* ホスト変数 */
//        EXEC SQL BEGIN DECLARE SECTION;

        TS_KIKAN_POINT_TBL h_ts_kikan_point_buff; /* TS期間限定ポイント情報 */

//        EXEC SQL END DECLARE SECTION;

        if (DBG_LOG) {
            C_DbgMsg("C_UpdateKikanPoint : %s\n", "start");
        }


        /* 引数のチェックを行う */
        if (kikanPointData == null || status == null) {
            /* 入力引数エラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : %s\n", "PRMERR(null)");
            }
            if (status != null) status.arr = C_const_Stat_PRMERR;
            return C_const_NG;
        }

        /* ＤＢコネクトのチェックを行う */
        if (strlen(Cg_ORASID) == 0 || strlen(Cg_ORAUSR) == 0 ||
                strlen(Cg_ORAPWD) == 0) {
            /* ＤＢアクセスエラー */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : %s\n", "DBERR(connect check NG)");
            }
            status.arr = C_const_Stat_DBERR;
            return C_const_NG;

        }


        /* ホスト変数を編集する */
        h_ts_kikan_point_buff = new TS_KIKAN_POINT_TBL();
        memset(h_ts_kikan_point_buff, 0x00, 0);
        memcpy(h_ts_kikan_point_buff.kokyaku_no,
                kikanPointData.kokyaku_no, kikanPointData.kokyaku_no.len);
        h_ts_kikan_point_buff.kokyaku_no.len = kikanPointData.kokyaku_no.len;

        /* 環境変数の取得 */
        penv = getenv(C_CM_ORA_SID_SD);
        if (StringUtils.isEmpty(penv)) {
            /* 環境変数エラー */
            APLOG_WT("903", 0, null, "getenv(C_CM_ORA_SID_SD)",
                    0, 0, 0, 0, 0);
            status.arr = C_const_Stat_ENVERR;
            return C_const_NG;
        }

        /* ＳＱＬを生成する */
        /* テーブル名編集 */
        strcpy(tbl_nam, "TS期間限定ポイント情報");
        if (strcmp(Cg_ORASID, penv) != 0) {
            strcat(tbl_nam, "");
        }

        /* ホスト変数設定 */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point01 = kikanPointData.fuyo_kikan_gentei_point01;  /* 付与期間限定Ｐ０１         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point02 = kikanPointData.fuyo_kikan_gentei_point02;  /* 付与期間限定Ｐ０２         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point03 = kikanPointData.fuyo_kikan_gentei_point03;  /* 付与期間限定Ｐ０３         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point04 = kikanPointData.fuyo_kikan_gentei_point04;  /* 付与期間限定Ｐ０４         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point05 = kikanPointData.fuyo_kikan_gentei_point05;  /* 付与期間限定Ｐ０５         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point06 = kikanPointData.fuyo_kikan_gentei_point06;  /* 付与期間限定Ｐ０６         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point07 = kikanPointData.fuyo_kikan_gentei_point07;  /* 付与期間限定Ｐ０７         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point08 = kikanPointData.fuyo_kikan_gentei_point08;  /* 付与期間限定Ｐ０８         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point09 = kikanPointData.fuyo_kikan_gentei_point09;  /* 付与期間限定Ｐ０９         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point10 = kikanPointData.fuyo_kikan_gentei_point10;  /* 付与期間限定Ｐ１０         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point11 = kikanPointData.fuyo_kikan_gentei_point11;  /* 付与期間限定Ｐ１１         */
        h_ts_kikan_point_buff.fuyo_kikan_gentei_point12 = kikanPointData.fuyo_kikan_gentei_point12;  /* 付与期間限定Ｐ１２         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point01 = kikanPointData.riyo_kikan_gentei_point01;  /* 利用期間限定Ｐ０１         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point02 = kikanPointData.riyo_kikan_gentei_point02;  /* 利用期間限定Ｐ０２         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point03 = kikanPointData.riyo_kikan_gentei_point03;  /* 利用期間限定Ｐ０３         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point04 = kikanPointData.riyo_kikan_gentei_point04;  /* 利用期間限定Ｐ０４         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point05 = kikanPointData.riyo_kikan_gentei_point05;  /* 利用期間限定Ｐ０５         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point06 = kikanPointData.riyo_kikan_gentei_point06;  /* 利用期間限定Ｐ０６         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point07 = kikanPointData.riyo_kikan_gentei_point07;  /* 利用期間限定Ｐ０７         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point08 = kikanPointData.riyo_kikan_gentei_point08;  /* 利用期間限定Ｐ０８         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point09 = kikanPointData.riyo_kikan_gentei_point09;  /* 利用期間限定Ｐ０９         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point10 = kikanPointData.riyo_kikan_gentei_point10;  /* 利用期間限定Ｐ１０         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point11 = kikanPointData.riyo_kikan_gentei_point11;  /* 利用期間限定Ｐ１１         */
        h_ts_kikan_point_buff.riyo_kikan_gentei_point12 = kikanPointData.riyo_kikan_gentei_point12;  /* 利用期間限定Ｐ１２         */
        h_ts_kikan_point_buff.sagyo_kigyo_cd = kikanPointData.sagyo_kigyo_cd;             /* 作業企業コード             */
        h_ts_kikan_point_buff.sagyosha_id = kikanPointData.sagyosha_id;                /* 作業者ＩＤ                 */
        h_ts_kikan_point_buff.sagyo_ymd = kikanPointData.sagyo_ymd;                  /* 作業年月日                 */
        h_ts_kikan_point_buff.sagyo_hms = kikanPointData.sagyo_hms;                  /* 作業時刻                   */
        h_ts_kikan_point_buff.batch_koshin_ymd = kikanPointData.batch_koshin_ymd;           /* バッチ更新日               */
        h_ts_kikan_point_buff.saishu_koshin_ymd = kikanPointData.saishu_koshin_ymd;          /* 最終更新日                 */
        h_ts_kikan_point_buff.saishu_koshin_ymdhms = kikanPointData.saishu_koshin_ymdhms;       /* 最終更新日時               */
        strcpy(h_ts_kikan_point_buff.saishu_koshin_programid, kikanPointData.saishu_koshin_programid);   /* 最終更新プログラムＩＤ     */


        strcpy(sql_buf, "UPDATE ");
        strcat(sql_buf, tbl_nam);


        strcat(sql_buf, " SET 付与期間限定Ｐ０１    = 付与期間限定Ｐ０１         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０２         = 付与期間限定Ｐ０２         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０３         = 付与期間限定Ｐ０３         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０４         = 付与期間限定Ｐ０４         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０５         = 付与期間限定Ｐ０５         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０６         = 付与期間限定Ｐ０６         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０７         = 付与期間限定Ｐ０７         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０８         = 付与期間限定Ｐ０８         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ０９         = 付与期間限定Ｐ０９         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ１０         = 付与期間限定Ｐ１０         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ１１         = 付与期間限定Ｐ１１         + ?,");
        strcat(sql_buf, "付与期間限定Ｐ１２         = 付与期間限定Ｐ１２         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０１         = 利用期間限定Ｐ０１         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０２         = 利用期間限定Ｐ０２         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０３         = 利用期間限定Ｐ０３         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０４         = 利用期間限定Ｐ０４         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０５         = 利用期間限定Ｐ０５         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０６         = 利用期間限定Ｐ０６         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０７         = 利用期間限定Ｐ０７         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０８         = 利用期間限定Ｐ０８         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ０９         = 利用期間限定Ｐ０９         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ１０         = 利用期間限定Ｐ１０         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ１１         = 利用期間限定Ｐ１１         + ?,");
        strcat(sql_buf, "利用期間限定Ｐ１２         = 利用期間限定Ｐ１２         + ?,");
        /* 2021/11/17 UPDATE文から更新項目削除（作業企業コード,作業者ＩＤ,作業年月日,作業時刻 */
        /*strcat(sql_buf, "作業企業コード             =                              :A25,"); */
        /*strcat(sql_buf, "作業者ＩＤ                 =                              :A26,"); */
        /*strcat(sql_buf, "作業年月日                 =                              :A27,"); */
        /*strcat(sql_buf, "作業時刻                   =                              :A28,"); */
        strcat(sql_buf, "バッチ更新日               =                              ?,");
        strcat(sql_buf, "最終更新日                 =                              ?,");
        strcat(sql_buf, "最終更新日時               =                           SYSDATE(),");
        strcat(sql_buf, "最終更新プログラムＩＤ     =                              ?");
        strcat(sql_buf, " WHERE 顧客番号 = ?");


        if (DBG_LOG) {
            C_DbgMsg("C_UpdateKikanPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);


//        EXEC SQL PREPARE sql_kdatalock7 from:
        sqlca.sql = WRKSQL;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_kikan_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE PREPARE",
                    sqlca.sqlcode, "TS期間限定ポイント情報",
                    out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* UPDATE文を実行する */
        sqlca.restAndExecute(h_ts_kikan_point_buff.fuyo_kikan_gentei_point01.longVal(),                 /* 付与期間限定Ｐ０１ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point02.longVal(),    /* 付与期間限定Ｐ０２ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point03.longVal(),    /* 付与期間限定Ｐ０３ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point04.longVal(),    /* 付与期間限定Ｐ０４ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point05.longVal(),    /* 付与期間限定Ｐ０５ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point06.longVal(),    /* 付与期間限定Ｐ０６ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point07.longVal(),    /* 付与期間限定Ｐ０７ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point08.longVal(),    /* 付与期間限定Ｐ０８ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point09.longVal(),    /* 付与期間限定Ｐ０９ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point10.longVal(),    /* 付与期間限定Ｐ１０ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point11.longVal(),    /* 付与期間限定Ｐ１１ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point12.longVal(),    /* 付与期間限定Ｐ１２ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point01.longVal(),    /* 利用期間限定Ｐ０１ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point02.longVal(),    /* 利用期間限定Ｐ０２ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point03.longVal(),    /* 利用期間限定Ｐ０３ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point04.longVal(),    /* 利用期間限定Ｐ０４ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point05.longVal(),    /* 利用期間限定Ｐ０５ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point06.longVal(),    /* 利用期間限定Ｐ０６ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point07.longVal(),    /* 利用期間限定Ｐ０７ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point08.longVal(),    /* 利用期間限定Ｐ０８ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point09.longVal(),    /* 利用期間限定Ｐ０９ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point10.longVal(),    /* 利用期間限定Ｐ１０ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point11.longVal(),    /* 利用期間限定Ｐ１１ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point12.longVal(),    /* 利用期間限定Ｐ１２ */
                /* 2021/11/17 UPDATE文から更新項目削除（作業企業コード,作業者ＩＤ,作業年月日,作業時刻 */
                /* :h_ts_kikan_point_buff.sagyo_kigyo_cd,                            *//* 作業企業コード     */
                /* :h_ts_kikan_point_buff.sagyosha_id,                               *//* 作業者ＩＤ         */
                /* :h_ts_kikan_point_buff.sagyo_ymd,                                 *//* 作業年月日         */
                /* :h_ts_kikan_point_buff.sagyo_hms,                                 *//* 作業時刻           */
                h_ts_kikan_point_buff.batch_koshin_ymd.longVal(),                          /* バッチ更新日       */
                h_ts_kikan_point_buff.saishu_koshin_ymd.longVal(),                         /* 最終更新日         */
                h_ts_kikan_point_buff.saishu_koshin_programid,                   /* 最終更新プログラムＩＤ */
                h_ts_kikan_point_buff.kokyaku_no.longVal());                                /* 顧客番号           */
        if (sqlca.sqlcode != C_const_Ora_OK && sqlca.sqlcode !=
                C_const_Ora_NOTFOUND) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_kikan_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "UPDATE", sqlca.sqlcode,
                    "TS期間限定ポイント情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* 正常の場合終了 */
        if (sqlca.sqlcode != C_const_Ora_NOTFOUND) {
            /* 戻り値の設定 */
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : %s\n", "end");
            }

            status.arr = C_const_Stat_OK;
            return C_const_OK;
        }

        /* ＳＱＬを生成する */
        strcpy(sql_buf, "INSERT INTO ");
        strcat(sql_buf, tbl_nam);

        strcat(sql_buf, " (顧客番号,");
        strcat(sql_buf, "付与期間限定Ｐ０１,");
        strcat(sql_buf, "付与期間限定Ｐ０２,");
        strcat(sql_buf, "付与期間限定Ｐ０３,");
        strcat(sql_buf, "付与期間限定Ｐ０４,");
        strcat(sql_buf, "付与期間限定Ｐ０５,");
        strcat(sql_buf, "付与期間限定Ｐ０６,");
        strcat(sql_buf, "付与期間限定Ｐ０７,");
        strcat(sql_buf, "付与期間限定Ｐ０８,");
        strcat(sql_buf, "付与期間限定Ｐ０９,");
        strcat(sql_buf, "付与期間限定Ｐ１０,");
        strcat(sql_buf, "付与期間限定Ｐ１１,");
        strcat(sql_buf, "付与期間限定Ｐ１２,");
        strcat(sql_buf, "利用期間限定Ｐ０１,");
        strcat(sql_buf, "利用期間限定Ｐ０２,");
        strcat(sql_buf, "利用期間限定Ｐ０３,");
        strcat(sql_buf, "利用期間限定Ｐ０４,");
        strcat(sql_buf, "利用期間限定Ｐ０５,");
        strcat(sql_buf, "利用期間限定Ｐ０６,");
        strcat(sql_buf, "利用期間限定Ｐ０７,");
        strcat(sql_buf, "利用期間限定Ｐ０８,");
        strcat(sql_buf, "利用期間限定Ｐ０９,");
        strcat(sql_buf, "利用期間限定Ｐ１０,");
        strcat(sql_buf, "利用期間限定Ｐ１１,");
        strcat(sql_buf, "利用期間限定Ｐ１２,");
        strcat(sql_buf, "作業企業コード,");
        strcat(sql_buf, "作業者ＩＤ,");
        strcat(sql_buf, "作業年月日,");
        strcat(sql_buf, "作業時刻,");
        strcat(sql_buf, "バッチ更新日,");
        strcat(sql_buf, "最終更新日,");
        strcat(sql_buf, "最終更新日時,");
        strcat(sql_buf, "最終更新プログラムＩＤ");
        strcat(sql_buf, ") VALUES (");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "?,");
        strcat(sql_buf, "SYSDATE(),");
        strcat(sql_buf, "?)");


        if (DBG_LOG) {
            C_DbgMsg("C_UpdateKikanPoint : sqlbuf=[%s]\n", sql_buf);
        }

        /* ＳＱＬ文をセットする */
        memset(WRKSQL, 0x00, sizeof(WRKSQL.arr));
        strcpy(WRKSQL, sql_buf);
        WRKSQL.len = strlen(WRKSQL.arr);


//        EXEC SQL PREPARE sql_kdatalock8 from:
        sqlca.sql = WRKSQL;
        sqlca.prepare();
        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : PREPARE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_kikan_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT PREPARE",
                    sqlca.sqlcode, "TS期間限定ポイント情報",
                    out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }

        /* INSERT文を実行する */
        sqlca.restAndExecute(h_ts_kikan_point_buff.kokyaku_no,                                /* 顧客番号           */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point01,                 /* 付与期間限定Ｐ０１ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point02,                 /* 付与期間限定Ｐ０２ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point03,                 /* 付与期間限定Ｐ０３ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point04,                 /* 付与期間限定Ｐ０４ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point05,                 /* 付与期間限定Ｐ０５ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point06,                 /* 付与期間限定Ｐ０６ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point07,                 /* 付与期間限定Ｐ０７ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point08,                 /* 付与期間限定Ｐ０８ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point09,                 /* 付与期間限定Ｐ０９ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point10,                 /* 付与期間限定Ｐ１０ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point11,                 /* 付与期間限定Ｐ１１ */
                h_ts_kikan_point_buff.fuyo_kikan_gentei_point12,                 /* 付与期間限定Ｐ１２ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point01,                 /* 利用期間限定Ｐ０１ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point02,                 /* 利用期間限定Ｐ０２ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point03,                 /* 利用期間限定Ｐ０３ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point04,                 /* 利用期間限定Ｐ０４ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point05,                 /* 利用期間限定Ｐ０５ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point06,                 /* 利用期間限定Ｐ０６ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point07,                 /* 利用期間限定Ｐ０７ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point08,                 /* 利用期間限定Ｐ０８ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point09,                 /* 利用期間限定Ｐ０９ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point10,                 /* 利用期間限定Ｐ１０ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point11,                 /* 利用期間限定Ｐ１１ */
                h_ts_kikan_point_buff.riyo_kikan_gentei_point12,                 /* 利用期間限定Ｐ１２ */
                h_ts_kikan_point_buff.sagyo_kigyo_cd,                            /* 作業企業コード     */
                h_ts_kikan_point_buff.sagyosha_id,                               /* 作業者ＩＤ         */
                h_ts_kikan_point_buff.sagyo_ymd,                                 /* 作業年月日         */
                h_ts_kikan_point_buff.sagyo_hms,                                 /* 作業時刻           */
                h_ts_kikan_point_buff.batch_koshin_ymd,                          /* バッチ更新日       */
                h_ts_kikan_point_buff.saishu_koshin_ymd,                         /* 最終更新日         */
                h_ts_kikan_point_buff.saishu_koshin_programid);                   /* 最終更新プログラムＩＤ */

        if (sqlca.sqlcode != C_const_Ora_OK) {
            if (DBG_LOG) {
                C_DbgMsg("C_UpdateKikanPoint : EXECUTE : sqlca.sqlcode=[%d]\n",
                        sqlca.sqlcode);
            }
            sprintf(out_format_buf, "顧客番号=[%s]",
                    h_ts_kikan_point_buff.kokyaku_no.arr);
            APLOG_WT("904", 0, null, "INSERT", sqlca.sqlcode,
                    "TS期間限定ポイント情報", out_format_buf, 0, 0);

            status.arr = C_const_Stat_DBERR;
            return C_const_NG;
        }



        /* 戻り値の設定 */
        if (DBG_LOG) {
            C_DbgMsg("C_UpdateKikanPoint : %s\n", "end");
        }

        status.arr = C_const_Stat_OK;
        return C_const_OK;

        /*-----C_UpdateKikanPoint Bottom----------------------------------------------*/
    }
}
