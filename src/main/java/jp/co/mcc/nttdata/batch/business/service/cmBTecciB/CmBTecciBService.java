package jp.co.mcc.nttdata.batch.business.service.cmBTecciB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTecciBService {
     int  cmBTecciB_main();                   /* ＥＣ会員情報連動主処理     */
     int  cmBTecciB_Set_SedoDB();             /* 制度ＤＢ連動設定処理       */
     int  FileOutputS();                      /* 顧客情報ファイル作成処理   */
     int  cmBTecciB_Set_KanriDB();            /* 管理ＤＢ連動設定処理       */
     int  FileOutputM();                      /* 顧客情報ファイル作成処理   */
     int  cmBTecciB_Chk_Arg( StringDto Arg_in );  /* 引数チェック処理           */
     int  cmBTecciB_OpenFile(int i);               /* ファイルオープン処理       */
     int  cmBTecciB_Make_WrkTBL();            /* ワークテーブル作成         */
}
