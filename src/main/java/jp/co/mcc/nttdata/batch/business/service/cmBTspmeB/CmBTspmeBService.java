package jp.co.mcc.nttdata.batch.business.service.cmBTspmeB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTspmeBService {

    int  cmBTspmeB_Chk_Arg(StringDto Arg_in);  /* 引数チェック処理           */
    int  cmBTspmeB_Chk_Date();               /* 引数妥当性チェック         */
    void cmBTspmeB_CutYMD(StringDto date_buf, IntegerDto year, IntegerDto month, IntegerDto day, IntegerDto yearmonth);        /* 年月日分割処理             */
    int cmBTspmeB_checkYMD( IntegerDto year, IntegerDto month, IntegerDto day );
    /* 年月日妥当性チェック       */
    int cmBTspmeB_daysInMonth( int year, int month );
    /* 月末日取得処理             */
    int cmBTspmeB_isLeapYear( int year );
    /* 閏年チェック処理           */
    int  cmBTspmeB_main()                    ; /* 店舗マスタ連動主処理     */
    int  cmBTspmeB_OpenFile()                ; /* ファイルオープン処理     */
    int  cmBTspmeB_WriteFile()               ; /* ファイル書込処理         */
    void cmBTspmeB_set_Sql();
    void cmBTspmeB_set_Sql_Arg();                     /* SQL（引数あり）編集処理  */

}
