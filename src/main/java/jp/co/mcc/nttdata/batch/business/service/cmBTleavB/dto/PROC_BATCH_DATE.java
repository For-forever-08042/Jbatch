package jp.co.mcc.nttdata.batch.business.service.cmBTleavB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class PROC_BATCH_DATE {
    public ItemDto  yyyymmdd = new ItemDto(9)     ;        /*  処理用バッチ日付    (半角)        */
    public ItemDto  hlf_yyyy = new ItemDto(5)     ;        /*  処理用バッチ日付年  (半角)        */
    public ItemDto  hlf_mm = new ItemDto(3)       ;        /*  処理用バッチ日付月  (半角)        */
    public ItemDto  hlf_dd = new ItemDto(3)       ;        /*  処理用バッチ日付日  (半角)        */
    public ItemDto  hlf_yyyymm= new ItemDto(7)   ;        /*  処理用バッチ日付年月(半角)        */
    public ItemDto  hlf_y_bottom= new ItemDto(2) ;        /*  年 下１桁           (半角)        */
    public ItemDto  all_yyyymmdd= new ItemDto(24);        /*  処理用バッチ日付    (全角)        */
    public ItemDto  all_yyyy= new ItemDto(12)    ;        /*  処理用バッチ日付年  (全角)        */
    public ItemDto  all_mm = new ItemDto(6)       ;        /*  処理用バッチ日付月  (全角)        */
    public ItemDto  all_dd = new ItemDto(6)       ;        /*  処理用バッチ日付日  (全角)        */
    public ItemDto  all_yyyymm= new ItemDto(18)  ;        /*  処理用バッチ日付年月(全角)        */
    public ItemDto  all_y_bottom= new ItemDto(3) ;        /*  年 下１桁           (全角)        */
    public ItemDto  int_yyyymmdd = new ItemDto(3)   ;        /*  処理用バッチ日付    (数値)        */
    public ItemDto  int_yyyy      = new ItemDto(3) ;        /*  処理用バッチ日付年  (数値)        */
    public ItemDto  int_mm        = new ItemDto(3) ;        /*  処理用バッチ日付月  (数値)        */
    public ItemDto  int_dd        = new ItemDto(3) ;        /*  処理用バッチ日付日  (数値)        */
    public ItemDto  int_yyyymm    = new ItemDto(3) ;        /*  処理用バッチ日付年月(数値)        */
    public ItemDto  int_y_bottom  = new ItemDto(3) ;        /*  年 下１桁           (数値)        */
}
