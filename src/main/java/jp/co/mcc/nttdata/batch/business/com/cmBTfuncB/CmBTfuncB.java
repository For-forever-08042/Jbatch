package jp.co.mcc.nttdata.batch.business.com.cmBTfuncB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;

/**
 * 共通関数サービス(cmABfuncB)
 */
public interface CmBTfuncB {

    public int C_InsertDayPoint(TSHS_DAY_POINT_TBL tshs_day_point, int date, IntegerDto status);

    public int BT_Rtrim(StringDto strp, int size);

    int C_GetCmMaster(MM_KOKYAKU_INFO_TBL cmMaster, IntegerDto status);

    int C_GetCsMaster(MS_KOKYAKU_SEDO_INFO_TBL ccMaster, IntegerDto status);

    int C_GetCzMaster(MM_KOKYAKU_ZOKUSE_INFO_TBL czMaster, IntegerDto status);

    int C_GetCcMaster(MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL ccMaster, IntegerDto status);

    int C_GetYearPoint(TS_YEAR_POINT_TBL tsYearPoint, int year, IntegerDto status);

    int C_GetRank(double rankup_taisho_kingaku, int kubun, IntegerDto p_rank_cd,
                  IntegerDto status);

    int C_GetFamilyRank(int p_kazoku_id, IntegerDto p_kazoku_honnen_rank_cd, IntegerDto p_kazoku_tougetsu_rank_cd,
                        IntegerDto status);

    int C_UpdateYearPoint(TS_YEAR_POINT_TBL yearPointData, int year, int month, IntegerDto status);
    int C_UpdateTotalPoint(TS_TOTAL_POINT_TBL  totalPointData, IntegerDto status);
    int C_UpdateKikanPoint(TS_KIKAN_POINT_TBL kikanPointData, IntegerDto status);
}
