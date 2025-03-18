package jp.co.mcc.nttdata.batch.fw.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NTRegexUtil {

    public static String find(String regx, String source) {
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(source);
        StringBuilder numbers = new StringBuilder();
        while (matcher.find()) {
            numbers.append(matcher.group());
        }
        return numbers.toString();
    }

    public static boolean any(String regx, String source) {
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(source);
        return matcher.find();
    }

}
