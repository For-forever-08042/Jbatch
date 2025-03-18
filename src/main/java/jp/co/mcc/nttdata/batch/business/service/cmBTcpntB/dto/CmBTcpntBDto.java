package jp.co.mcc.nttdata.batch.business.service.cmBTcpntB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.service.db.dto.h.*;

public class CmBTcpntBDto {

    /* 使用テーブルヘッダーファイルをインクルード                                 */
    public MM_KOKYAKU_INFO_TBL mmkoinf_t = new MM_KOKYAKU_INFO_TBL(); /* MM顧客情報            バッファ */
    public MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL mmkigyo_t = new MM_KOKYAKU_KIGYOBETU_ZOKUSE_TBL(); /* MM顧客企業別属性情報  バッファ */
    public MS_KOKYAKU_SEDO_INFO_TBL mskosed_t = new MS_KOKYAKU_SEDO_INFO_TBL(); /* MS顧客制度情報        バッファ */
    public MS_KAZOKU_SEDO_INFO_TBL msfmsed_t = new MS_KAZOKU_SEDO_INFO_TBL() ; /* MS家族制度情報        バッファ */
    public MS_CARD_INFO_TBL mscardi_t = new MS_CARD_INFO_TBL(); /* MSカード情報          バッファ */
    public TS_RIYO_KANO_POINT_TBL tsrypnt_t = new TS_RIYO_KANO_POINT_TBL(); /* TS利用可能ポイント情報バッファ */
    public TS_YEAR_POINT_TBL tspntyr_t = new TS_YEAR_POINT_TBL() ; /* TSポイント年別情報    バッファ */

    /* 動的ＳＱＬ作成用 */
    public ItemDto str_sql = new ItemDto(4096)      ; /* 実行用SQL文字列             */

    /* 処理用 */
    public ItemDto gh_bat_date_prev = new ItemDto()                    ; /* 処理日付(前日)         */
    public ItemDto gh_bat_date_this = new ItemDto()                     ; /* 処理日付(当日)         */
    public ItemDto gh_kokyaku_no = new ItemDto(15+1)                  ; /* 顧客番号               */
    public ItemDto gh_saishu_koshin_programid = new ItemDto(20+1)     ; /* 最終更新プログラムＩＤ */

}
