package jp.co.mcc.nttdata.batch.fw.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DateUtil {

    public static String nowDateFormat(String format) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return now.format(formatter);
    }

    public static String getYYYYMMDD() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(calendar.getTime());
    }


    public static String getHHMMSS() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        return sdf.format(calendar.getTime());
    }

    public static String getTHHMMSS() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(calendar.getTime());
    }

    public static String getYYYYMMDDHHMMSS() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(calendar.getTime());
    }

    /**
     * 前日取得
     *
     * @return
     */
    public static String getYYYYMMDD_Y() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(calendar.getTime());
    }

    /**
     * 前月取得
     *
     * @return
     */
    public static String getLM_YYYYMM() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        return previousMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
    /**
     * 取得月
     *
     * @return
     */
    public static String getYYYYMM() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        return sdf.format(calendar.getTime());
    }
    /**
     * 前月初日取得
     *
     * @return
     */
    public static String getLMFirs_YYYYMMDD() {
        return getLM_YYYYMM() + "01";
    }

    /**
     * 前月末日取得
     *
     * @return
     */
    public static String getLMLast_YYYYMMDD() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate lastDayOfMonth = previousMonth.atDay(previousMonth.lengthOfMonth());
        return lastDayOfMonth.format(DateTimeFormatter.ofPattern("yyyMMdd"));
    }
}
