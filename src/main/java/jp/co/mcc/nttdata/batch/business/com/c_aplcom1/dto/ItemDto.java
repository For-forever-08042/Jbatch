
package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

public class ItemDto {
    public Object arr;
    public int len;

    public ItemDto() {
    }

    public void memset() {
        arr = "";
    }

    public ItemDto(int len) {
        this.len = len;
    }

    public ItemDto(Object obj1) {
        this.arr = obj1;
    }

    public int size() {
        if (arr != null) {
            return strVal().length();
        }
        return 0;
    }

    public long longVal() {
        if (arr instanceof Double) {
            return ((Double) arr).longValue();
        } else if (arr instanceof Float) {
            return ((Float) arr).longValue();
        } else if (arr instanceof BigDecimal) {
            return ((Number) arr).longValue();
        }
        return Long.valueOf((null == arr || "".equals(arr)) ? "0" : String.valueOf(arr));
    }

    public double floatVal() {
        if (arr instanceof BigDecimal) {
            return ((Number) arr).doubleValue();
        }
        if (arr instanceof Double) {
            return ((Double) arr).doubleValue();
        }
        return Double.valueOf((null == arr || "".equals(arr)) ? "0" : String.valueOf(arr));
    }

    public Integer intVal() {
        if (arr instanceof BigDecimal) {
            return ((Number) arr).intValue();
        }
        if (arr instanceof Double) {
            return ((Double) arr).intValue();
        }
        return Integer.valueOf((null == arr || "".equals(arr)) ? "0" : String.valueOf(arr));
    }

    public String strVal() {
        return arr==null? "":String.valueOf(arr);
    }

    public String arr() {
        return arr==null? "":String.valueOf(arr);
    }


    public boolean isNull() {
        return arr == null ? true : strVal().isEmpty();
    }


    public StringDto strDto() {
        StringDto stringDto = new StringDto(strVal());
        stringDto.len = len;

        return stringDto;
    }

    public void reduce(ItemDto d){
       arr= longVal()-d.longVal();
    }

    @Override
    public String toString() {
        return String.valueOf(arr);
    }

}
