package jp.co.mcc.nttdata.batch.business.service.cmBTnnrgB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTnnrgBService {

    public int  cmBTnnrgB_main();                  /* 翌年年別情報初期登録主処理  */
    public int  cmBTnnrgB_Chk_Arg(StringDto Arg_in);   /* 引数チェック処理            */
    /* 翌年年別情報初期登録対象顧客検索対象日付取得処理 */
    public int  GetBatchSeqno();
    /* 翌年年別情報初期登録対象顧客検索対象日付更新処理 */
    public int  UpdateBatchSeqno();
    public int  YokunenNenbetsuRegist();           /* 翌年年別情報初期登録処理    */
    /* 翌年年別情報登録処理        */
    public int  InsertYokunenNenbetsu(int regist_year);

}
