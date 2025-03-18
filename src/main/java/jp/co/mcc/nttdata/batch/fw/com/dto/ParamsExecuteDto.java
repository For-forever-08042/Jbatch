package jp.co.mcc.nttdata.batch.fw.com.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ParamsExecuteDto {

    ArrayList<String> params = new ArrayList<>();

    public ParamsExecuteDto(String[] args) {
        params.addAll(Arrays.asList(args));
    }

    public String[] doInput() {
        return params.stream().collect(Collectors.toList()).toArray(new String[0]);
    }

    public String[] doReplaceNullInput() {
        return params.stream().filter(item -> item != null).collect(Collectors.toList()).toArray(new String[0]);
    }

    public ParamsExecuteDto add(String[] args) {
        if (args != null) {
            for (String item : args) {
                params.add(item);
            }
        }
        return this;
    }

    public ParamsExecuteDto add(String param) {
        if (StringUtils.isNotEmpty(param)) {
            params.add(param);
        }
        return this;
    }

    public ParamsExecuteDto sD(String param) {
        params.add("-D");
        params.add(param);
        return this;
    }
    public ParamsExecuteDto sA(String param) {
        params.add("-A");
        params.add(param);
        return this;
    }
    public ParamsExecuteDto sB(String param) {
        params.add("-B");
        params.add(param);
        return this;
    }
    public ParamsExecuteDto sN(String param) {
        params.add("-N");
        params.add(param);
        return this;
    }
    public ParamsExecuteDto ss(String param) {
        params.add("-s");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sS(String param) {
        params.add("-S");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sS() {
        params.add("-S");
        return this;
    }

    public ParamsExecuteDto ss() {
        params.add("-s");
        return this;
    }

    public ParamsExecuteDto sG() {
        params.add("-G");
        return this;
    }

    public ParamsExecuteDto sG(String param) {
        params.add("-G");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sD() {
        params.add("-D");
        return this;
    }

    public ParamsExecuteDto DEL() {
        params.add("-DEL");
        return this;
    }

    public ParamsExecuteDto sT(String param) {
        params.add("-T");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sT() {
        params.add("-T");
        return this;
    }

    public ParamsExecuteDto sO() {
        params.add("-O");
        return this;
    }
    public ParamsExecuteDto sO(String o) {
        params.add("-O");
        params.add(o);
        return this;
    }

    public ParamsExecuteDto sI() {
        params.add("-I");
        return this;
    }

    public ParamsExecuteDto sI(String param) {
        params.add("-I");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sZ() {
        params.add("-Z");
        return this;
    }

    public ParamsExecuteDto sZ(String param) {
        params.add("-Z");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sC(String param) {
        params.add("-C");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sC() {
        params.add("-C");
        return this;
    }

    public ParamsExecuteDto sF(String param) {
        params.add("-F");
        params.add(param);
        return this;
    }

    public ParamsExecuteDto sF() {
        params.add("-F");
        return this;
    }

    public ParamsExecuteDto P(String param) {
        params.add("-P" + param);
        return this;
    }

    public ParamsExecuteDto S(String param) {
        params.add("-S" + param);
        return this;
    }

    public ParamsExecuteDto M(String param) {
        params.add("-M" + param);
        return this;
    }

    public ParamsExecuteDto sE() {
        params.add("-E");
        return this;
    }

    public ParamsExecuteDto E(String param) {
        params.add("-E" + param);
        return this;
    }

    public ParamsExecuteDto FE() {
        params.add("-FE");
        return this;
    }

    public ParamsExecuteDto FI() {
        params.add("-FI");
        return this;
    }

    public ParamsExecuteDto FW() {
        params.add("-FW");
        return this;
    }
}
