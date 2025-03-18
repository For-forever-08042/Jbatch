package jp.co.mcc.nttdata.batch.fw.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.CLanguageFunction.getenv;

/**
 * @author shirukai
 */
public class EnvUtils {
    public static void setEnv(String name, String value)  {

        try {
            getModifiableEnvironment().put(name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Map<String, String> getModifiableEnvironment() throws Exception {
        Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
        Method getenv = pe.getDeclaredMethod("getenv");
        getenv.setAccessible(true);
        Object unmodifiableEnvironment = getenv.invoke(null);
        Class<?> map = Class.forName("java.util.Collections$UnmodifiableMap");
        Field m = map.getDeclaredField("m");
        m.setAccessible(true);
        return (Map<String, String>) m.get(unmodifiableEnvironment);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("TEST_SET_ENV","test-set-env");

        System.out.println(getenv("TEST_SET_ENV"));
    }
}

