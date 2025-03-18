package jp.co.mcc.nttdata.batch.business.com.cmABfkbhB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmABfkbhBService {
    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
     int  ChkArgpCom( StringDto Arg_in )        ; /* 引数(P)チェック処理        */
     int  DecFilePid();
     int  ReadFile( );
     int WriteTmpFile();


}
