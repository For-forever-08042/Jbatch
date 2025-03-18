package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

public class IntegerDto {
    public Integer arr;

    public IntegerDto() {
    }

    public IntegerDto(Integer arr) {
        this.arr = arr;
    }

    @Override
    public String toString() {
        if (null != arr) {
            return arr.toString();
        }
        return null;
    }
}
