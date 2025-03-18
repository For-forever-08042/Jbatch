package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class MS_BLOCK_INFO_TBL  extends TBLBaseDto {
    public ItemDto svblock_cd = new ItemDto();                     /* ＳＶブロックコード  PK */
    public ItemDto hakko_ymd = new ItemDto();                         /* 発行日              PK */
    public ItemDto sikko_ymd = new ItemDto();                         /* 失効日                 */
    public ItemDto kakisha_cd = new ItemDto();                        /* 会社コード             */
    public ItemDto sv_block_kanji = new ItemDto();            /* ＳＶブロック名         */
    public ItemDto sv_block_short = new ItemDto();            /* ＳＶブロック短縮名     */
    public ItemDto sv_block_kana = new ItemDto();             /* ＳＶブロック名カナ     */
    public ItemDto kigyo_kana = new ItemDto();                /* 企業名称カナ           */
    public ItemDto tantou_sv_id = new ItemDto();                /* 担当ＳＶＩＤ           */
    public ItemDto joui_svblock_cd = new ItemDto();                   /* 上位ＳＶブロックコード */
    public ItemDto kaisou = new ItemDto();                       /* 階層                   */
    public ItemDto saikasou_flg = new ItemDto();                 /* 最下層フラグ           */
    public ItemDto batch_koshin_ymd = new ItemDto();                  /* バッチ更新日           */
    public ItemDto saishu_koshin_ymd = new ItemDto();                 /* 最終更新日             */
    public ItemDto saishu_koshin_ymdhms = new ItemDto();              /* 最終更新日時           */
    public ItemDto saishu_koshin_programid = new ItemDto();     /* 最終更新プログラムＩＤ */
}
