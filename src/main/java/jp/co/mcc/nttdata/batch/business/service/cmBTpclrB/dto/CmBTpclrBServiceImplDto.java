package jp.co.mcc.nttdata.batch.business.service.cmBTpclrB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_KIKAN_POINT_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RIYO_KANO_POINT_TBL;

public class CmBTpclrBServiceImplDto {

    /* 使用テーブルヘッダーファイルをインクルード                                 */
//    EXEC SQL INCLUDE  TS_RIYO_KANO_POINT_DATA.h;    /* TS利用可能ポイント情報     */
//    EXEC SQL INCLUDE  TS_KIKAN_POINT_DATA.h;        /* TS期間限定ポイント情報     */

    public TS_RIYO_KANO_POINT_TBL tsriyo_t = new TS_RIYO_KANO_POINT_TBL();       /* TS利用可能ポイント情報バッファ     */
    public TS_RIYO_KANO_POINT_TBL  tsriyo_t_chk = new TS_RIYO_KANO_POINT_TBL();   /* TS利用可能ポイント情報バッファ     */
    public TS_RIYO_KANO_POINT_TBL  tsriyo_t_upd = new TS_RIYO_KANO_POINT_TBL();   /* TS利用可能ポイント情報バッファ     */
    public TS_KIKAN_POINT_TBL tskikan_t = new TS_KIKAN_POINT_TBL();      /* TS期間限定ポイント情報バッファ     */

    /* 処理用 */
    public int    hi_bat_date;            /* バッチ処理日(当日)                 */
    public StringDto hc_Program_Name = new StringDto(10);    /* バージョンなしプログラム名         */
    public StringDto    h_str_sql1= new StringDto(4096);       /* 実行用SQL文字列                    */
    public StringDto    h_str_sql2= new StringDto(4096);       /* 実行用SQL文字列                    */
}
