package jp.co.mcc.nttdata.batch.business.service.cmBTkzdkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTkzdkBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    public int  cmBTkzdkB_ChkArgdInf(StringDto Arg_in);         /* 引数"-d"チェック処理     */
    public int  cmBTkzdkB_PreTotal();             /* 集計前処理               */
    public int  cmBTkzdkB_CreKikangenteiPData();  /* ポイント集計情報作成処理 */
    public int  cmBTkzdkB_CreKikangenteiPFile();  /* ポイント集計表作成処理   */
    public void cmBTkzdkB_SetMonth();             /* 対象月変換処理           */
    public void cmBTkzdkB_SetMonthMM(int month, StringDto monthNM);    /* 対象月変換処理           */
}
