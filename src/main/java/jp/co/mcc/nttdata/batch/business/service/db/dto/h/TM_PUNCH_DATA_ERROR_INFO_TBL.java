package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class TM_PUNCH_DATA_ERROR_INFO_TBL  extends TBLBaseDto {
    public ItemDto shori_ymd= new ItemDto();           /* 処理年月日       */
    public ItemDto entry_kigyo_cd= new ItemDto();      /* 申込書企業コード */
    public ItemDto entry_mise_no= new ItemDto();       /* 申込書店番号     */
    public ItemDto kaiin_no= new ItemDto();      /* 会員番号         */
    public ItemDto data_kbn= new ItemDto();            /* データ区分       */
    public ItemDto riyu_cd= new ItemDto();             /* 理由コード       */
    public ItemDto irai_ymd= new ItemDto();            /* 依頼日           */
    public ItemDto batch_no= new ItemDto();            /* バッチ番号       */
    public ItemDto nyukai_ymd= new ItemDto();          /* 入会年月日       */
    public ItemDto kaiin_mesho= new ItemDto(); /* 会員氏名         */
    public ItemDto punch_center_tochaku_ymd= new ItemDto(); /* パンチセンター到着日       */
    public ItemDto kokyaku_zokusei_error_flg= new ItemDto(); /* 顧客属性エラーフラグ      */
}
