package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

public class PS_MISE_HYOJI_INFO_TBL  extends TBLBaseDto {
    public ItemDto mise_no = new ItemDto();
    public ItemDto kaishi_ymd = new ItemDto();
    public ItemDto shuryo_ymd = new ItemDto();
    public ItemDto kigyo_cd = new ItemDto();
    public ItemDto block_cd = new ItemDto();

    public ItemDto kigyo_kanji = new ItemDto();
    public ItemDto kigyo_short = new ItemDto();
    public ItemDto kigyo_kana = new ItemDto();
    public ItemDto bu_cd = new ItemDto();
    public ItemDto bu_kanji = new ItemDto();
    public ItemDto bu_short = new ItemDto();
    public ItemDto bu_kana = new ItemDto();
    public ItemDto zone_cd = new ItemDto();
    public ItemDto zone_kanji = new ItemDto();
    public ItemDto zone_short = new ItemDto();
    public ItemDto zone_kana = new ItemDto();
    public ItemDto block_kanji = new ItemDto();
    public ItemDto block_short = new ItemDto();
    public ItemDto block_kana = new ItemDto();
}
