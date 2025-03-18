package jp.co.mcc.nttdata.batch.business.service.cmBTenupB;

public interface CmBTenupBService {

    public int  GetCardNo();                          /* 最新会員番号取得処理     */
    public int  UpdateMBaby();                    /* マタニティベビー情報更新処理 */
    public int  InsertMBabyHist();                /* マタニティベビー履歴情報登録 */
    public int  KokyakuLock();                        /* 顧客ロック処理           */
    public int  UpdateSeido();                        /* 顧客制度情報更新処理     */
    public int  UpdateRiyoKano();                  /* 利用可能ポイント情報更新処理 */

}
