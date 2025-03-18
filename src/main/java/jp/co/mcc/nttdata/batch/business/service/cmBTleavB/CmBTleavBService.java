package jp.co.mcc.nttdata.batch.business.service.cmBTleavB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_KAZOKU_SEDO_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RANK_INFO_TBL;

public interface CmBTleavBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
//    void APLOG_WT( char *msgid, int  msgidsbt, char *dbkbn,
//                   caddr_t param1, caddr_t param2, caddr_t param3,
//                   caddr_t param4, caddr_t param5, caddr_t param6 );
//
//    int C_GetCmMaster(MM_KOKYAKU_INFO_TBL *cmMaster, int *status);
//    int C_GetCsMaster(MS_KOKYAKU_SEDO_INFO_TBL *csMaster, int *status);
//    int C_GetCzMaster(MM_KOKYAKU_ZOKUSE_INFO_TBL *czMaster, int *status);
//    int C_GetYearPoint(TS_YEAR_POINT_TBL *tsYearPoint, int year, int *status);

    public int  cmBTleavB_main();                                                  /* 顧客退会処理                 */
    public int  cmBTleavB_Chk_Arg( StringDto Arg_in );                                 /* 引数チェック処理             */
    public int  cmBTleavB_OpenFile();                                              /* ファイルオープン処理         */
    public int  cmBTleavB_ReadFile(int file_flg);                                  /* １レコード読み込み処理       */                                                /* 2022/10/14 MCCM初版 MOD */
    public int  cmBTleavB_WriteFile();                                             /* 処理結果ファイルに書込み処理 */
    public int  getKazokuSedoInfo(MS_KAZOKU_SEDO_INFO_TBL ksMaster, IntegerDto status); /* 家族制度情報検索             */
    public int  setRankInfo();                                                 /* ランク情報取得処理           */
    public int  getMaxRank(double p_kingaku, int p_shubetsu, IntegerDto p_rank_cd);      /* MAXランクのサーチ            */
    public int  getRankUpMoney();                                                  /* ランクＵＰ情報取得処理       */

    public int trimRight(StringDto pbuf); /* 後ろスペースの削除処理 */
    public int GetTsRankInfo(TS_RANK_INFO_TBL tsRankInfo, IntegerDto status);           /* TSランク情報取得             */                                                /* 2022/12/12 MCCM初版 ADD */


}
