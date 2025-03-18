package jp.co.mcc.nttdata.batch.business.com.cmBTdmupB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTdmupB {

    public int cmBTdmupB_Main();               /* DM止め区分更新処理         */
    public int cmBTdmupB_ChkArgiInf(StringDto Arg_in);       /* 引数チェック処理           */
    public int cmBTdmupB_OpenFile();           /* ファイルオープン処理       */
    public int cmBTdmupB_ReadFile();           /* ファイル読み込み処理       */
    public int cmBTdmupB_WriteFile();          /* ファイル書き込み処理       */
    public int cmBTdmupB_WriteFileHead();  /* ファイル書き込み（先頭行）処理 */
    public int cmBTdmupB_CloseFile();          /* ファイルクローズ処理       */
    public int cmBTdmupB_SelRiyuInfo();        /* MS理由情報取得処理         */
    public int cmBTdmupB_SelCardInfo();        /* MSカード情報取得処理       */
    public int cmBTdmupB_UpdKigyobetuInfo();  /* MM顧客企業別属性情報更新処理*/
    public int cmBTdmupB_CheckNumber(StringDto numstring);     /* 数値チェック       */

}
