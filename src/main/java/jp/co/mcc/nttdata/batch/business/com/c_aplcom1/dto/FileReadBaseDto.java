package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class FileReadBaseDto {

    public int size() {
        Object obj = this;
        Field[] fields = getClass().getDeclaredFields();
        int len = 0;
        for (Field i : fields) {
            try {
                Object itemDto = i.get(obj);
                if (itemDto instanceof ItemDto) {
                    len += ((ItemDto) itemDto).len;
                } else if (itemDto instanceof IntegerDto) {
                    len += String.valueOf(((IntegerDto) itemDto).arr).length();
                } else if (itemDto instanceof StringDto) {
                    len += ((StringDto) itemDto).len;
                }
            } catch (IllegalAccessException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
        }
        return len;
    }

}
