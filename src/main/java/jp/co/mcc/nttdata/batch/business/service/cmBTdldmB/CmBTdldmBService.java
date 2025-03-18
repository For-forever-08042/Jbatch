package jp.co.mcc.nttdata.batch.business.service.cmBTdldmB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTdldmBService {
     int  cmBTdldmB_Emailmain()         ; /* DMリスト作成主処理   */
     int  cmBTdldmB_EmailMake()         ; /* DMリスト作成処理     */
     int  cmBTdldmB_MmKoInf()           ; /* 顧客情報取得処理           */
     int  cmBTdldmB_MmKoKigyo()         ; /* 顧客企業別属性情報取得処理 */
     int  cmBTdldmB_MsCardInf()         ; /* カード情報取得処理         */
     int  cmBTdldmB_MsCnpnInf()         ; /* キャンペーン情報更新処理   */
     int  cmBTdldmB_Chk_Arg(StringDto Arg_in)   ; /* 引数チェック処理           */
     int  cmBTdldmB_OpenInpF()          ; /* 入力ファイルオープン処理   */
     int  cmBTdldmB_OpenOutF()          ; /* 出力ファイルオープン処理   */
     int  cmBTdldmB_ReadInpF()          ; /* ファイル読込処理           */
     int  cmBTdldmB_WriteOutF()         ; /* ファイル出力処理           */
     int  cmBTdldmB_CloseInpF()         ; /* 入力ファイルクローズ処理   */
     int  cmBTdldmB_CloseOutF()         ; /* 出力ファイルクローズ処理   */
    /* パラメータファイル名取得   */
      int  getFileName(StringDto file_id, StringDto file_dir, StringDto file_path);
}
