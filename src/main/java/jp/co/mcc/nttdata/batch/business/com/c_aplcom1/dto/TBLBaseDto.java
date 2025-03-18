package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class TBLBaseDto {
    public TBLBaseDto() {
    }

    public void memset() {
        Class<?> parent = getClass();
        Field[] fields = parent.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.get(this) instanceof ItemDto[]) {
                    ItemDto[] list = (ItemDto[]) field.get(this);
                    for (int i = 0; i < list.length; i++) {
                        list[i] = new ItemDto();
                    }
                    field.set(this, list);
                } else {
                    if (field.get(this) instanceof ItemDto) {
                        ItemDto data = (ItemDto) field.get(this);
                        data.arr = "";
                        field.set(this, data);
                    }
                }
            } catch (Exception e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
        }
    }

}
