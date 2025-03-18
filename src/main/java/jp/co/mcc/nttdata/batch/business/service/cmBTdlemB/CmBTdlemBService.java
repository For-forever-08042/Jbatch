package jp.co.mcc.nttdata.batch.business.service.cmBTdlemB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTdlemBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
      int  cmBTdlemB_Emailmain()         ; /* Ｅメールリスト作成主処理   */
      int  cmBTdlemB_EmailMake()         ; /* Ｅメールリスト作成処理     */
      int  cmBTdlemB_MmKoInf()           ; /* 顧客情報取得処理           */
      int  cmBTdlemB_MmKoKigyo()         ; /* 顧客企業別属性情報取得処理 */
      int  cmBTdlemB_MsCardInf()         ; /* カード情報取得処理         */
      int  cmBTdlemB_MsCnpnInf()         ; /* キャンペーン情報更新処理   */
      int  cmBTdlemB_Chk_Arg(StringDto Arg_in)   ; /* 引数チェック処理           */
      int  cmBTdlemB_OpenInpF()          ; /* 入力ファイルオープン処理   */
      int  cmBTdlemB_OpenOutF()          ; /* 出力ファイルオープン処理   */
      int  cmBTdlemB_ReadInpF()          ; /* ファイル読込処理           */
      int  cmBTdlemB_WriteOutF()         ; /* ファイル出力処理           */
      int  cmBTdlemB_CloseInpF()         ; /* 入力ファイルクローズ処理   */
      int  cmBTdlemB_CloseOutF()         ; /* 出力ファイルクローズ処理   */
    /* パラメータファイル名取得   */
     int  getFileName(StringDto  file_id, StringDto  file_dir, StringDto  file_path);
    /* 2016/02/03 ファイル削除処理削除 */
    /* 別バッチ処理（クーポン顧客登録）で顧客リストを使用するため */
    /* 当処理では顧客リストを削除しない。                         */
    /*static int  cmBTdlemB_RemoveFile();*/    /* ファイル削除処理            */
}
