package jp.co.mcc.nttdata.batch.business.service.cmBTpclrB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTpclrBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    public int cmBTpclrB_Chk_Arg(StringDto Arg_in); /* 引数チェック処理               */
    public int cmBTpclrB_main( );            /* ポイントクリア処理(主処理)     */
    public  void cmBTpclrB_getKijunInfo( );          /* 基準値取得処理                 */
    public  void cmBTpclrB_getMonth(   int i_month, StringDto c_month);
    /* 月取得                         */
    public  void cmBTpclrB_getYearCd(   int i_year, StringDto c_year_cd);
     /* 年度取得                       */
    public int cmBTpclrB_clearTSRiyokano( ); /* TS利用可能ポイント情報クリア   */
    public int cmBTpclrB_chkTSRiyokano( );   /* TS利用可能ポイント情報チェック */
    public int cmBTpclrB_updTSRiyokano( );   /* TS利用可能ポイント情報更新     */
    public int cmBTpclrB_clearTSKikangentei( ); /* TS期間限定ポイント情報クリア*/
    public int cmBTpclrB_updTSKikangentei( );   /* TS期間限定ポイント情報更新  */
}
