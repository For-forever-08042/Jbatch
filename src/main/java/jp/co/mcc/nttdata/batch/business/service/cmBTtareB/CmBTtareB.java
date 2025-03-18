package jp.co.mcc.nttdata.batch.business.service.cmBTtareB;

public interface CmBTtareB {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    int cmBTtareB_main();                  /* 止め区分洗替処理            */

    int UpdateDMkbn();                     /* DM止め区分洗替処理          */

    int UpdateEMkbn();                     /* Eメール止め区分洗替処理     */

}
