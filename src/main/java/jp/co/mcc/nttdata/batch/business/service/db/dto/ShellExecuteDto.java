package jp.co.mcc.nttdata.batch.business.service.db.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ShellExecuteDto {

    public String result;
    private String error;
    public String fileName;

    public ArrayList<String> commands = new ArrayList<>();

    public String[] getCommands() {
        return Arrays.copyOf(commands.toArray(), commands.size(), String[].class);
    }

    public ShellExecuteDto setCommand(String... command) {
        commands.addAll(Arrays.asList(command));
        return this;
    }

    public String getResultData() {
        String format = result.replaceAll("\\s+", " ");
        return format.substring(Math.max(format.indexOf(" ") + 1, 0));
    }

    public boolean RTN0() {
        return !result.startsWith("0");
    }

    public boolean RTN10() {
        return !result.startsWith("10");
    }

    public ShellExecuteDto setCommand(ArrayList<String> command) {
        this.commands = command;
        return this;
    }

    public ShellExecuteDto setResult(String result) {
        log.info("sh result :" + result);
        this.result = result;
        return this;
    }

    public ShellExecuteDto setError(String error) {
        log.info("sh error :" + error);
        this.error = error;
        return this;
    }

    public ShellExecuteDto setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    Map<String, String> evn = new HashMap<>();

    public ShellExecuteDto addEvn(String key, String value) {
        evn.put(key, value);
        return this;
    }

    public ShellExecuteDto CONNECT_SD(String str) {
        evn.put(C_aplcom1Service.CONNECT_SD, str);
        return this;
    }

    public ShellExecuteDto defaultEvn(NtBasicTask ntBasicTask) {
        if (ntBasicTask.CONNECT_SD != null) {
            evn.put(C_aplcom1Service.CONNECT_SD, ntBasicTask.CONNECT_SD);
            evn.put(C_aplcom1Service.CONNECT_DB, ntBasicTask.CONNECT_SD);
        }
        if (ntBasicTask.CONNECT_USR != null) {
            evn.put(C_aplcom1Service.CONNECT_USR, ntBasicTask.CONNECT_USR);
        }

        evn.put(C_aplcom1Service.CM_MYPRGID, ntBasicTask.CM_MYPRGID);
        evn.put(C_aplcom1Service.CM_APWORK_DATE, ntBasicTask.CM_APWORK_DATE);
        if (ntBasicTask.CM_APSQL != null) {
            evn.put(C_aplcom1Service.CM_APSQL, ntBasicTask.CM_APSQL);
        }
        return this;
    }

    public ShellExecuteDto CM_MYPRGID(String str) {
        evn.put(C_aplcom1Service.CM_MYPRGID, str);
        return this;
    }

    public ShellExecuteDto CM_APSQL(String str) {
        evn.put(C_aplcom1Service.CM_APSQL, str);
        return this;
    }

    public ShellExecuteDto CM_APWORK_DATE(String str) {
        evn.put(C_aplcom1Service.CM_APWORK_DATE, str);
        return this;
    }

    public ShellExecuteDto addEvn(Map<String, String> evn) {
        this.evn = evn;
        return this;
    }


    public String getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

    public String getFileName() {
        return fileName;
    }

    public Map<String, String> getEvn() {
        return evn;
    }

    public ShellExecuteDto execute() {
        ShellClientManager.executeCommand(this);
        return this;
    }
}
