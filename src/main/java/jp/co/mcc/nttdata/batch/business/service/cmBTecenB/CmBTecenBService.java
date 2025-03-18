package jp.co.mcc.nttdata.batch.business.service.cmBTecenB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.cmBTecenB.dto.EC_RENDO_DATA;

public interface CmBTecenBService {
    /*-------------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                         */
    /*-------------------------------------------------------------------------------*/
    public int cmBTecenB_main();
    public int WM_Batch_Update();
    public int Write_ErrorList_File();
    public int Update_Data();
    public void Split_Data(StringDto buf); /* 入力ファイルに変更があれば、要修正 */

    public int CheckMscard(EC_RENDO_DATA in_ec_rendo,int punch_errno);    /* カード情報チェック処理 */
    public int SeitaiCheck(EC_RENDO_DATA in_ec_rendo,int punch_errno);    /* 静態情報更新チェック処理 */
    public int UpdateCard(EC_RENDO_DATA in_ec_rendo,int punch_errno);     /* カード情報更新 */
    public int UpdateCardForTaikai(EC_RENDO_DATA in_ec_rendo,int punch_errno);     /* カード情報更新（退会） */
    public int UpdateKokyaku(EC_RENDO_DATA in_ec_rendo,int punch_errno);  /* 顧客情報更新 */
    public int InsertKokyakudata();                                                /* MS顧客制度情報・MM顧客情報追加 */
    public int UpdateKokyakudata();                                                /* MS顧客制度情報・MM顧客情報更新 */
    public int UpdateKokyakuForTaikai(EC_RENDO_DATA in_ec_rendo,int punch_errno);  /* 顧客情報更新（退会） */
    public int InsertKokyakudataForTaikai();                                        /* MS顧客制度情報・MM顧客情報追加（退会） */
    public int UpdateKokyakudataForTaikai();                                        /* MS顧客制度情報・MM顧客情報更新（退会） */
    public int UpdateKokyakuzokusei(EC_RENDO_DATA in_ec_rendo,
                              int              punch_errno);               /* 顧客属性情報更新 */
    public int InsertKokyakuzokuseidata();                                         /* 顧客属性情報追加 */
    public int UpdateKokyakuzokuseidata();                                         /* 顧客属性情報更新 */
    public int UpdateKokyakuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo,
                                       int              punch_errno);               /* 顧客属性情報更新（退会） */
    public int InsertKokyakuzokuseidataForTaikai();                                /* 顧客属性情報追加（退会） */
    public int UpdateKokyakuzokuseidataForTaikai();                                /* 顧客属性情報更新（退会） */
    public int OutPunchdata(int punch_errno);                                      /* パンチエラーデータ更新 */

    public int UpdateKigyobetuzokusei(EC_RENDO_DATA in_ec_rendo,
                                int              punch_errno);               /* 顧客企業別属性情報更新 */
    public int InsertKigyobetuzokuseidata();                                       /* 顧客企業別属性情報追加 */
    public int UpdateKigyobetuzokuseidata();                                       /* 顧客企業別属性情報更新 */
    public int UpdateKigyobetuzokuseiForTaikai(EC_RENDO_DATA in_ec_rendo,
                                         int              punch_errno);               /* 顧客企業別属性情報更新（退会） */
    public int InsertKigyobetuzokuseidataForTaikai();                              /* 顧客企業別属性情報追加（退会） */
    public int UpdateKigyobetuzokuseidataForTaikai();                              /* 顧客企業別属性情報更新（退会） */

//static int UpdateMaternityBabyInfoForTaikai();                               /* マタニティベビー情報更新（退会） */     /* 2022/10/12 MCCM初版 DEL */
//static int UpdateCircleKokyakuInfoForTaikai();                               /* サークル顧客情報更新（退会）     */     /* 2022/10/12 MCCM初版 DEL */

    public int UpdateRiyokanoPoint(EC_RENDO_DATA in_ec_rendo,
                                   int              punch_errno);               /* 利用可能ポイント情報更新 */
    public int  SetDate();                                              /* 各種日付設定処理                   */
    public int  PointLost();                                            /* ポイント失効処理                   */
    public int  SetPointYearInf();                                      /* ポイント日別情報登録(ポイント年別情報)処理 */
    public int  SetKokyakSedoInf();                                     /* ポイント日別情報登録(顧客制度情報)処理     */
    public int  InsPointYmdInf();                                       /* ポイント日別情報登録処理                   */
    public int  UpdRiyoKanoPoint();                                     /* 利用可能ポイント情報更新処理               */

    public int cmBTcentB_Chk_Arg( String Arg_in );                                  /* 引数チェック処理             */
    public void strchg(StringDto buf, String str1, String str2);            /* 文字列置換処理             */
    public int APLOG_WT(String msgid, int  msgidsbt, String dbkbn,
                        Object param1, Object param2, Object param3,
                        Object param4, Object param5, Object param6);
}
