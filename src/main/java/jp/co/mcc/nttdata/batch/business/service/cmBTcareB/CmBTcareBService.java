package jp.co.mcc.nttdata.batch.business.service.cmBTcareB;

public interface CmBTcareBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
    public int  cmBTcareB_main();              /* カード統合洗替処理           */
    public int  UpdatePointDay();              /* ポイント日別情報洗替処理     */
    public int  UpdatePointTrade();            /* ポイント明細取引情報洗替処理 */
    public int  UpdatePointDayUchiwake();      /* ポイント日別内訳情報洗替処理 */                                            /* 2022/10/17 MCCM初版 ADD */
    public int  InsertGooponRiyoKokyakuInfo(); /* MSクーポン利用顧客情報nnnnn登録処理 */                                     /* 2022/10/17 MCCM初版 ADD */
     /* 2023/09/15 MCCM仕様変更JK215 ADD START */
    public int  UpdatePointRireki();           /* ポイント履歴情報洗替処理 */
    public int  UpdateKoubaiRireki();          /* 購買履歴情報洗替処理 */
     /* 2023/09/15 MCCM仕様変更JK215 ADD END */
      /* 2023/09/21 MCCM仕様変更JK206 ADD START */
    public int  UpdateDirectMailSendInfo();           /* DM送付状態情報洗替処理 */
                /* 2023/09/21 MCCM仕様変更JK206 ADD END */
}
