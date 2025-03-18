package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import org.apache.commons.lang3.StringUtils;

public class StringDto {
    public String arr = "";
    public int len;
    public int strtokIndex = 0;
    public int strsepIndex = 0;
    public int strchrIndex = 0;


    public StringDto() {
    }

    public void memset() {
        arr = "";
        strtokIndex = 0;
        strsepIndex = 0;
        strchrIndex = 0;
    }

    public StringDto(String arr) {
        this.arr = arr;
    }

    public int size() {
        if (arr != null) {
            return arr.length();
        }
        return 0;
    }

    public long longVal() {
        return Long.valueOf(null == arr ? "0" : arr);
    }

    public double floatVal() {
        return Double.valueOf(null == arr ? "0" : arr);
    }

    public Integer intVal() {
        return Integer.valueOf(null == arr ? "0" : arr);
    }

    public String strVal() {
        return String.valueOf(arr);
    }

    public String arr() {
        return String.valueOf(arr);
    }

    public StringDto(int len) {
        this.len = len;
        this.arr = "";
    }

    public char charAt(int index) {
        return arr.charAt(index);
    }

    public String getArr() {
        return arr;
    }

    public void setArr(String arr) {
        this.arr = arr;
    }

    public int getLen() {
        return arr == null ? 0 : arr.length();
    }

    public String cut(int from) {
        byte[] b = arr.getBytes();
        byte[] b2 = new byte[b.length - from];
        for (int t = from; t < b.length; t++) {
            b2[t - from] = b[t];
        }
        return new String(b2);
    }

    public String substring(int from) {
        return arr.substring(from);
    }

    public String strp(String deiv) {
        if(StringUtils.isEmpty(arr)){
            return "";
        }
        String str = arr.split(deiv)[0];
        arr = arr.replaceFirst(str+",", "");
        return str;
    }

    public String strtok(Object src, String deiv) {
        String[] res = arr.split(deiv);
        String result = null;
        while (true) {
            if (res.length <= strtokIndex) {
                strtokIndex = 0;
                break;
            }
            result = res[strtokIndex];
            strtokIndex++;
            if (StringUtils.isEmpty(result)) {
                continue;
            }
            break;
        }
        return result;
    }

    public String strsep(String deiv) {
        String[] res = arr.split(deiv);
        String result = null;
        if (res.length > strsepIndex) {
            result = res[strsepIndex];
            strsepIndex++;
        } else {
            strsepIndex = 0;
        }
        return result;
    }

    @Override
    public String toString() {
        return arr;
    }

    public String strchr(char delimiter) {
        String[] res = arr.split(String.valueOf(delimiter));
        String result = null;
        if (res.length > strchrIndex) {
            result = res[strchrIndex];
            strchrIndex++;
        } else {
            strchrIndex = 0;
        }
        return result;
    }
}
