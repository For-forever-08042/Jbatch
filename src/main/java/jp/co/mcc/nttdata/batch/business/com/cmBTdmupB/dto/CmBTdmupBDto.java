package jp.co.mcc.nttdata.batch.business.com.cmBTdmupB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_CARD_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_RIYU_INFO_TBL;

public class CmBTdmupBDto {

    public MS_RIYU_INFO_TBL msryinf_t = new MS_RIYU_INFO_TBL();          /* MS理由情報   バッファ      */
    public MS_CARD_INFO_TBL mscd_buff = new MS_CARD_INFO_TBL();          /* MSカード情報 バッファ      */

    public int h_bat_yyyymmdd;                         /* バッチ処理日付(当日)       */
    public StringDto h_saishu_koshin_programid = new StringDto(20 + 1);      /* 最終更新プログラムID       */

}
