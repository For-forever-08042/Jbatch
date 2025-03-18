package jp.co.mcc.nttdata.batch.business.com.cmBTkaicB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTkaicBService {

    public int  cmBTkaicB_main();              /* 会員情報作成主処理              */
    public int  OpenFile();                    /* 出力ファイルオープン処理        */
    public int  InsertWSKokyakuNo();           /* WS顧客番号２登録処理              */
    public int  InsertWMSPSSKaiin();           /* WMＳＰＳＳ会員情報登録処理      */
    public int  SetOutFile();                  /* 出力ファイル情報設定処理        */
    public void MakeSqlStr( StringDto wk_sqlbuf ); /* 会員情報データ取得ＳＱＬ文作成処理 */
    public int  FileOutput();                  /* ファイル出力処理                */
    public int  ChkArgoInf( StringDto Arg_in );    /* 引数oスイッチチェック処理       */
    public int  ChkArgbInf( StringDto Arg_in );    /* 引数bスイッチチェック処理       */
    public int  ChkArgdInf( StringDto Arg_in );    /* 引数dスイッチチェック処理       */
}
