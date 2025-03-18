package jp.co.mcc.nttdata.batch.business.service.cmBTtzdkB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTtzdkBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    public int  cmBTtzdkB_ChkArgdInf(StringDto Arg_in); /* 引数"-d"チェック処理       */
    public void        cmBTtzdkB_GetYearCd(int year, StringDto year_cd);    /* 年度コード取得*/
    public int  cmBTtzdkB_GetTujyoBit(int nendo);   /* 通常Ｐ失効フラグ基準値取得 */
    public int  cmBTtzdkB_PreTotal();           /* 集計前処理                 */
     /*----------------------------------------------------------------------------*/
    public int  cmBTtzdkB_CreateTujyoPoint();  /* 通常ポイント集計情報作成処理*/
    public int  cmBTtzdkB_CreateTujyoPointFile();/* 通常ポイント集計表作成処理*/
    public int  cmBTtzdkB_CreatePointTicket();   /* ポイント券集計情報作成処理*/
    public int  cmBTtzdkB_CreatePointTicketFile(); /* ポイント券集計表作成処理*/
}
