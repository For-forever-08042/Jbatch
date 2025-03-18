package jp.co.mcc.nttdata.batch.business.service.cmBTmsstB;

/**
 * サービス(CmBTmsstS)
 */
public interface CmBTmsstBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
     int  cmBTmsstB_Set_Block_All();     /* 店舗階層情報設定処理        */
     int  cmBTmsstB_Get_Block_All();     /* 店舗全階層情報取得処理      */
     int  cmBTmsstB_Get_Block();         /* 店舗１階層情報取得処理      */

}
