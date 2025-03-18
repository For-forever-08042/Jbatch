package jp.co.mcc.nttdata.batch.fw.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class PidUtil {
    public static String getPid() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getName().split("@")[0];
    }
}
