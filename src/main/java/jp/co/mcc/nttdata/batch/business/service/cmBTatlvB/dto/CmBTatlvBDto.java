package jp.co.mcc.nttdata.batch.business.service.cmBTatlvB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.TS_RIYO_KANO_POINT_TBL;

public class CmBTatlvBDto {

    public TS_RIYO_KANO_POINT_TBL tsryokp_t = new TS_RIYO_KANO_POINT_TBL();  /* TS利用可能ポイント情報バッファ     */

    /* バインド用 */
    /* 処理用     */
    public ItemDto Program_Name_Ver = new ItemDto(13);     /* バージョン付きプログラム名         */
    public ItemDto Program_Name = new ItemDto(10);         /* バージョンなしプログラム名         */

    /* 検索条件用 */
    /* 2022/10/06 MCCM初版 MOD START */
    public ItemDto gh_kijun_date3 = new ItemDto();           /* バッチ処理年月日  ※保存期間＝1日 */
    /* 2022/10/06 MCCM初版 MOD END */
    public ItemDto gh_kijun_date2 = new ItemDto();           /* バッチ処理年月日  ※保存期間１カ月 */
    public ItemDto  gh_kokyaku_no = new ItemDto();           /* 顧客番号                           */
    public ItemDto str_sql = new ItemDto(4096);                       /* 実行用SQL文字列        */
    public ItemDto gh_flg = new ItemDto();                  /* 0:退会顧客データファイル  1:顧客ステータス退会ファイル */  /* 2022/10/07 MCCM初版 ADD */

}
