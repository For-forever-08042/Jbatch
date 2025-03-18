package jp.co.mcc.nttdata.batch.business.service.cmBTspcdB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTspcdBService {

    /*----------------------------------------------------------------------------*/
    /*  関数プロトタイプ宣言                                                      */
    /*----------------------------------------------------------------------------*/
     int  cmBTspcdB_main();                   /* 番号変換マスタ連動主処理   */
     int  cmBTspcdB_OpenFile();               /* ファイルオープン処理       */
     int  cmBTspcdB_ChangeID();               /* 連動対象データ抽出処理     */
     int  cmBTspcdB_Chk_Arg(StringDto Arg_in);    /* 引数チェック処理           */
     int  cmBTspcdB_Chk_Date();               /* 日付妥当性チェック処理     */
    void    cmBTspcdB_CutYMD(StringDto  date_buf, IntegerDto year,
                             IntegerDto  month, IntegerDto  day,
                             IntegerDto  yearmonth);
    /* 日付分割処理               */
    void    cmBTspcdB_CutHMS(StringDto time_buf,IntegerDto hour,IntegerDto minute,IntegerDto second);
    /* 時刻分割処理               */
    int  cmBTspcdB_checkYMD(IntegerDto year,IntegerDto month,IntegerDto day);
    /* 日付妥当性チェック処理     */
    int  cmBTspcdB_daysInMonth(int year,int month);  /* 月末日取得処理     */
    int  cmBTspcdB_isLeapYear(int year);     /* 閏年判定処理               */
    int  cmBTspcdB_RemoveFile();           /* ファイル削除処理         */
}
