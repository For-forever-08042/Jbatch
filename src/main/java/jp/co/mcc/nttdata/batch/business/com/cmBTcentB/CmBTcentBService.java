package jp.co.mcc.nttdata.batch.business.com.cmBTcentB;

import jp.co.mcc.nttdata.batch.business.com.cmBTcentB.dto.NYUKAI_RENDO_DATA;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MM_KOKYAKU_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MM_KOKYAKU_ZOKUSE_INFO_TBL;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.MS_KOKYAKU_SEDO_INFO_TBL;

public interface CmBTcentBService {
    public int cmBTcentB_main();
    public int WM_Batch_Update();
    public int Write_ErrorList_File();
    public int Update_Data();
    public int ReadFile();      /* 入力ファイルに変更があれば、要修正 */

    public int CheckMscard(NYUKAI_RENDO_DATA in_nyukai_rendo, int punch_errno);    /* カード情報チェック処理 */
    public int SeitaiCheck(NYUKAI_RENDO_DATA in_nyukai_rendo,int punch_errno);    /* 静態情報更新チェック処理 */
    public int UpdateCard(NYUKAI_RENDO_DATA in_nyukai_rendo,int punch_errno);     /* カード情報更新 */
    public int UpdateKokyaku(NYUKAI_RENDO_DATA in_nyukai_rendo,int punch_errno);  /* 顧客情報更新 */
    public int InsertKokyakudata();                                                /* MS顧客制度情報・MM顧客情報追加 */
    public int UpdateKokyakudata();                                                /* MS顧客制度情報・MM顧客情報更新 */
    public int UpdateKokyakuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo, int              punch_errno);               /* 顧客属性情報更新 */
    public int InsertKokyakuzokuseidata();                                         /* 顧客属性情報追加 */
    public int UpdateKokyakuzokuseidata();                                         /* 顧客属性情報更新 */
    public int OutPunchdata(int punch_errno);                                      /* パンチエラーデータ更新 */

    public int UpdateKigyobetuzokusei(NYUKAI_RENDO_DATA in_nyukai_rendo, int              punch_errno);               /* 顧客企業別属性情報更新 */
    public int InsertKigyobetuzokuseidata();                                       /* 顧客企業別属性情報追加 */
    public int UpdateKigyobetuzokuseidata();                                       /* 顧客企業別属性情報更新 */
    public int UpdateRiyokanoPoint(NYUKAI_RENDO_DATA in_nyukai_rendo, int      punch_errno);               /* 利用可能ポイント情報更新 */
    public int UpdateKokyakuNyukaiTen(NYUKAI_RENDO_DATA in_nyukai_rendo, int              punch_errno);               /* 顧客情報（入会店）更新 */
    public int UpdateKokyakuNyukaiNengappi(NYUKAI_RENDO_DATA in_nyukai_rendo, int              punch_errno);         /* 顧客情報（入会年月日）更新 */
    public int UpdateZaisekiKaishiNengetsu(NYUKAI_RENDO_DATA in_nyukai_rendo, int              punch_errno);         /* 顧客情報（在籍開始年月）更新 */     /* 引数チェック処理             */
    public int APLOG_WT(String msgid, int msgidsbt, String dbkbn, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6);
}
