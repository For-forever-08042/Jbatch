package jp.co.mcc.nttdata.batch.business.service.cmBTcpntB;

public interface CmBTcpntBService {

    public int cmBTcpntB_main()                  ; /* 顧客入会情報更新主処理     */
    public int InsertWmkokyakuWk()               ; /* WM顧客番号登録処理         */
    /* 2023/02/13 顧客ステータス情報更新処理 ADD START */
    public int cmBTcpntB_main2()                 ; /* 顧客ステータス情報更新主処理*/
    public int InsertWmkokyakuStatusWk()         ; /* WM顧客ステータス情報更新処理*/
    /* 2023/02/13 顧客ステータス情報更新処理 ADD END */
    
}
