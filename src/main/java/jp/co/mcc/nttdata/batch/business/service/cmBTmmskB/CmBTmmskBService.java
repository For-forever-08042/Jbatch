package jp.co.mcc.nttdata.batch.business.service.cmBTmmskB;

public interface CmBTmmskBService {

    public int cmBTmmskB_MakeFile();             /* 連携ファイル作成処理        */
    public int cmBTmmskB_OpenFile();             /* ファイルオープン処理        */
    public int cmBTmmskB_WriteFile();            /* ファイルライト処理       */
    public int cmBTmmskB_CloseFile();            /* ファイルクローズ処理        */

}
