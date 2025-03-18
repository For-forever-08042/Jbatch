package jp.co.mcc.nttdata.batch.fw.com.basic;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.CLanguageFunction;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.ParamsExecuteDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import jp.co.mcc.nttdata.batch.fw.util.TaskResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.*;

/**
 * ksh logic
 */
@Slf4j
public abstract class NtBasicTask implements Tasklet {

    public int Rtn_OK = 10;
    public int Rtn_NG = 49;

    public KshScriptTypes taskType;
    //引数定義
    public String CONNECT_MD;
    public String CONNECT_SD;
    public String CONNECT_BD;
    public String CONNECT_HD;

    public String csv = ".csv";
    public String dmp = ".dmp";
    public String lp = "/";

    public String ARG_ALL;
    //定数定義
    public static String FW = "-FW";
    public static String FE = "-FE";
    public static String FI = "-FI";

    public String IFS;
    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;
    public String CM_MYPRGID = null;
    public String CM_MYPRGNAME = null;
    public String CONNECT_DB = null;
    public String CONNECT_USR = null;
    String[] args = null;


    /* 環境変数                                                                    */
    public String CLASSPATH;
    public String SYS_YYYYMMDD;
    public String SYS_HHMMSS;
    public String CM_APWORK_DATE;
    public String CM_APSQL;
    public String C_TRACE_FILE_DIR = "";
    public String CM_APWORK = "";
    public String CM_APJCL = "";
    public String CM_SERVER_ID = "";
    public String CM_APBIN = "";
    public String CM_MAILTEXT = "";
    public String CM_JAVA_APBIN = "";
    public String CM_APPARAM = "";
    public String CM_APLOG = "";
    public String CM_CRMRENKEI = "";
    public String CM_FILEWATSND = "";
    public String CM_FILENOWSND = "";
    public String CM_FILEAFTSND = "";
    public String CM_FILE_RCV = "";
    public String CM_FILEWATRCV = "";
    public String CM_FILENOWRCV = "";
    public String CM_FILEAFTRCV = "";
    public String CM_TRACELOG = "";
    public String CM_JOBRESULTLOG = "";
    public String CM_TRACEBAT = "";
    public String CM_TODAY = "";
    public String CM_TRACEJAVABAT = "";
    public String WK_PWD = "";
    public String CM_SA_NO = "";
    public String CM_USR_MD = "";
    public String CM_USR_SD = "";
    public String CM_USR_BD = "";
    public String CM_USR_HD = "";
    public String CM_PSW_SD = "";
    public String CM_PSW_BD = "";
    public String CM_PSW_HD = "";
    public String CM_PSW_MD = "";
    public String CM_ORA_SID_SD = "";
    public String CM_ORA_SID_BD = "";
    public String CM_ORA_SID_HD = "";
    public String CM_ORA_SID_MD = "";
    public String CM_POS_SID_SD = "";
    public String CM_POS_SID_HD = "";
    public String CM_APRESULT = "";
    public String CM_BKUPRESULT = "";
    public String CM_BKUPRCV = "";
    public String NLS_LANG = "";
    public String CM_PCRENKEI_KOJINNASHI ="";

    public void setConnectConf() {
        CONNECT_MD = getenv(C_aplcom1Service.CM_USR_MD) + "/" + getenv(C_aplcom1Service.CM_PSW_MD) + "@" + getenv(C_aplcom1Service.CM_ORA_SID_MD);
        CONNECT_SD = getenv(C_aplcom1Service.CM_USR_SD) + ":" + getenv(C_aplcom1Service.CM_PSW_SD) + "@" + getenv(C_aplcom1Service.CM_POS_SID_SD);
        CONNECT_BD = getenv(C_aplcom1Service.CM_USR_BD) + "/" + getenv(C_aplcom1Service.CM_PSW_BD) + "@" + getenv(C_aplcom1Service.CM_ORA_SID_BD);
        CONNECT_HD = getenv(C_aplcom1Service.CM_USR_HD) + ":" + getenv(C_aplcom1Service.CM_PSW_HD) + "@" + getenv(C_aplcom1Service.CM_POS_SID_HD);
    }

    public boolean checkUnicodeLength(String str) {
        return str != null && str.length() != str.getBytes().length && (str.length() * 2) != str.getBytes().length;
    }

    public String basename(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        String[] names = null;
        if (name.contains("\\")) {
            names = name.split("\\\\");
        } else {
            names = name.split("/");
        }
        return names[names.length - 1];
    }

    public String dirname(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        String[] names = name.split("/");
        String dirname = name.replaceAll(names[names.length - 1], "");
        if (dirname.endsWith("/")) {
            dirname = dirname.substring(0, dirname.length() - 1);
        }
        return dirname;
    }

    /**
     * 日付・時刻取得 / システム日付
     */
    public void setCM_APWORK_DATE() {
        // 日付・時刻取得
        // システム日付
        SYS_YYYYMMDD = DateUtil.nowDateFormat("yyyyMMdd");
        CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK_DATE);
        if (StringUtils.isEmpty(CM_APWORK_DATE)) {
            CM_APWORK_DATE = getenv(C_aplcom1Service.CM_APWORK) + "/" + SYS_YYYYMMDD;
        }
        setenv(C_aplcom1Service.CM_APWORK_DATE, CM_APWORK_DATE);

    }

    private void evnLoad() {
        setCM_APWORK_DATE();
        CM_APSQL = getenv(C_aplcom1Service.CM_APSQL);
        C_TRACE_FILE_DIR = getenv(C_aplcom1Service.C_TRACE_FILE_DIR);
        CM_APWORK = getenv(C_aplcom1Service.CM_APWORK);
        CM_APJCL = getenv(C_aplcom1Service.CM_APJCL);
        CM_SERVER_ID = getenv(C_aplcom1Service.CM_SERVER_ID);
        CM_APBIN = getenv(C_aplcom1Service.CM_APBIN);
        CM_MAILTEXT = getenv(C_aplcom1Service.CM_MAILTEXT);
        CM_JAVA_APBIN = getenv(C_aplcom1Service.CM_JAVA_APBIN);
        CM_APPARAM = getenv(C_aplcom1Service.CM_APPARAM);
        CM_APLOG = getenv(C_aplcom1Service.CM_APLOG);
        CM_CRMRENKEI = getenv(C_aplcom1Service.CM_CRMRENKEI);
        CM_FILEWATSND = getenv(C_aplcom1Service.CM_FILEWATSND);
        CM_FILENOWSND = getenv(C_aplcom1Service.CM_FILENOWSND);
        CM_FILEAFTSND = getenv(C_aplcom1Service.CM_FILEAFTSND);
        CM_FILE_RCV = getenv(C_aplcom1Service.CM_FILE_RCV);
        CM_FILEWATRCV = getenv(C_aplcom1Service.CM_FILEWATRCV);
        CM_FILENOWRCV = getenv(C_aplcom1Service.CM_FILENOWRCV);
        CM_FILEAFTRCV = getenv(C_aplcom1Service.CM_FILEAFTRCV);
        CM_TRACELOG = getenv(C_aplcom1Service.CM_TRACELOG);
        CM_JOBRESULTLOG = getenv(C_aplcom1Service.CM_JOBRESULTLOG);
        CM_TRACEBAT = getenv(C_aplcom1Service.CM_TRACEBAT);
        CM_TODAY = getenv(C_aplcom1Service.CM_TODAY);
        CM_TRACEJAVABAT = getenv(C_aplcom1Service.CM_TRACEJAVABAT);
        WK_PWD = getenv(C_aplcom1Service.WK_PWD);
        CM_SA_NO = getenv(C_aplcom1Service.CM_SA_NO);
        CM_USR_MD = getenv(C_aplcom1Service.CM_USR_MD);
        CM_USR_SD = getenv(C_aplcom1Service.CM_USR_SD);
        CM_USR_BD = getenv(C_aplcom1Service.CM_USR_BD);
        CM_USR_HD = getenv(C_aplcom1Service.CM_USR_HD);
        CM_PSW_SD = getenv(C_aplcom1Service.CM_PSW_SD);
        CM_PSW_BD = getenv(C_aplcom1Service.CM_PSW_BD);
        CM_PSW_HD = getenv(C_aplcom1Service.CM_PSW_HD);
        CM_PSW_MD = getenv(C_aplcom1Service.CM_PSW_MD);
        CM_ORA_SID_SD = getenv(C_aplcom1Service.CM_ORA_SID_SD);
        CM_ORA_SID_BD = getenv(C_aplcom1Service.CM_ORA_SID_BD);
        CM_ORA_SID_HD = getenv(C_aplcom1Service.CM_ORA_SID_HD);
        CM_ORA_SID_MD = getenv(C_aplcom1Service.CM_ORA_SID_MD);
        CM_APRESULT = getenv(C_aplcom1Service.CM_APRESULT);
        CM_BKUPRESULT = getenv(C_aplcom1Service.CM_BKUPRESULT);
        CM_BKUPRCV = getenv(C_aplcom1Service.CM_BKUPRCV);
        NLS_LANG = getenv(C_aplcom1Service.NLS_LANG);
        CLASSPATH = getenv(C_aplcom1Service.CLASSPATH);
        CM_PCRENKEI_KOJINNASHI= getenv(C_aplcom1Service.CM_PCRENKEI_KOJINNASHI);
    }

    public boolean checkDBSize() {
        String[] tmp1 = CONNECT_DB.split("/");
        int count = 0;
        for (String t : tmp1) {
            String[] ti = t.split("@");
            count += ti.length;
        }
        //ローカル　4 |　本番環境　3
        return count != 3 && count != 4;
    }

    public boolean checkConnectionType(String option) {
        //#  接続先チェック
        switch (option) {
            case "SD":
                CONNECT_DB = CONNECT_SD;
                CONNECT_USR = getenv(C_aplcom1Service.CM_USR_SD);
                break;
            case "MD":
                CONNECT_DB = CONNECT_MD;
                CONNECT_USR = getenv(C_aplcom1Service.CM_USR_MD);
                break;
            case "BD":
                CONNECT_DB = CONNECT_BD;
                CONNECT_USR = getenv(C_aplcom1Service.CM_USR_BD);
                break;
            case "HD":
                CONNECT_DB = CONNECT_HD;
                CONNECT_USR = getenv(C_aplcom1Service.CM_USR_HD);
                break;
            default:
                APLOG_WT("引数エラー  [" + ARG_ALL + "]\n接続先DB指定エラー  [" + option + "]", FW);
                return false;
        }
        return true;
    }

    public void cmName() {
//        ARG_ALL = args.length;
//        if (ARG_ALL > 1) {

        ARG_ALL = StringUtils.join(args, " ");
        //CM_MYPRGID = args[0].substring(0, 9);
        //args[0] = CM_MYPRGID;
        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);
        CM_MYPRGNAME = taskType.programmeName;
    }

    public ParamsExecuteDto getExecuteParam() {
        return new ParamsExecuteDto(args);
    }

    public ParamsExecuteDto getExecuteBaseParam() {
        return new ParamsExecuteDto(new String[1]);
    }

    public ParamsExecuteDto getBServiceParam() {
        return new ParamsExecuteDto(new String[1]).add(args);
    }

    public String[] taskBefore(ChunkContext chunkContext) {
//        List<String> list = new ArrayList<>();
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
//        for (Object value : jobParameters.values()) {
//            list.add(String.valueOf(value));
//        }
        String arg = (String) jobParameters.get(SystemConstant.ARGS_KEY);
        return arg.split(SystemConstant.ARGS_KEY_SPLIT);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        args = taskBefore(chunkContext);

        TaskResultUtil.RESULT_CODE = main(args);
        return RepeatStatus.FINISHED;
    }

    public int main(ParamsExecuteDto paramsExecuteDto) {
        String[] strings = paramsExecuteDto.doInput();
        return main(strings);
    }

    public int main(String[] argvs) {
        try {
            if (argvs != null && argvs[0] == null) {
                argvs[0] = toLowerCaseFirst(this.getClass().getSimpleName().substring(0, 9));
                CM_MYPRGID = argvs[0];
            }

            if (argvs[0].endsWith("B")) {
                // プログラムIDを環境変数に設定
                CM_MYPRGID = argvs[0];
            } else {
                if (argvs.length >= 1) {
                    argvs = Arrays.copyOfRange(argvs, 1, argvs.length);
                }
                // プログラムIDを環境変数に設定
                CM_MYPRGID = toLowerCaseFirst(this.getClass().getSimpleName().substring(0, 9));
            }

            args = argvs;
            cmName();
            setConnectConf();
            evnLoad();

            return taskExecuteCustom(args);
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return Rtn_NG;
        }
    }


    /**
     * 如果需要自定义，请重写这个方法
     *
     * @param args
     */
    abstract public int taskExecuteCustom(String[] args);

    public String getenv(String key) {
        return CLanguageFunction.getenv(key);
    }

    public String setenv(String key, String value) {
        return CLanguageFunction.setenv(key, value);
    }


    /**
     * APログ出力関数
     *
     * @param args
     */
    public void APLOG_WT(String... args) {
        IFS = "@";
        setenv("CM_MYPRGID",CM_MYPRGID);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().add(args));
        IFS = getenv(CmABfuncLServiceImpl.BKUP_IFS);
    }

    public void sleep(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout); // seconds unit
        } catch (InterruptedException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
    }
}
