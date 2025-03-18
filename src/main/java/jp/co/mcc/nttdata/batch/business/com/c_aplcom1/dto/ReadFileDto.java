package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
@Slf4j
public class ReadFileDto {

    String data;
    int status;
    List<String> lineData;

    public static ReadFileDto getInstance() {
        return new ReadFileDto();
    }

    public ReadFileDto readFile(String fileName) {
        data = FileUtil.readFile(fileName);
        lineData=Arrays.asList(data.split("\n")) ;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public ReadFileDto filter(Predicate<? super String> linePredicate) {
        //row
        lineData = lineData.stream().filter(linePredicate).collect(Collectors.toList());
        return this;
    }

    public ReadFileDto loopBySize(ReadFileDtoRev readFileDtoRev, int size) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            String[] str = new String[size];
            for (int i = 0; i < str.length; i++) {
                str[i] = getByIndex(itemData, i);
            }
            status = readFileDtoRev.rev(str);
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }

    public ReadFileDto loop2(ReadFileDtoRev2 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0),
                    getByIndex(itemData, 1));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }

    public ReadFileDto loop3(ReadFileDtoRev3 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0),
                    getByIndex(itemData, 1),
                    getByIndex(itemData, 2));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }

    public ReadFileDto loop4(ReadFileDtoRev4 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0),
                    getByIndex(itemData, 1),
                    getByIndex(itemData, 2),
                    getByIndex(itemData, 3));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }

    public ReadFileDto loop5(ReadFileDtoRev5 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0),
                    getByIndex(itemData, 1),
                    getByIndex(itemData, 2),
                    getByIndex(itemData, 3),
                    getByIndex(itemData, 4));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }
    public ReadFileDto loop1(ReadFileDtoRev1 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }

    public ReadFileDto loop6(ReadFileDtoRev6 readFileDtoRev) {
        for (String item : lineData) {
            String[] itemData = splitData(item);
            status = readFileDtoRev.rev(getByIndex(itemData, 0),
                    getByIndex(itemData, 1),
                    getByIndex(itemData, 2),
                    getByIndex(itemData, 3),
                    getByIndex(itemData, 4),
                    getByIndex(itemData, 5));
            if (status != 10) {
                log.info("loop error exit");
                break;
            }
        }
        return this;
    }
    private String[] splitData(String item){
        return FileUtil.splitData(item);
    }

    private String getByIndex(String[] item, int index) {
        if (index > item.length - 1) {
            return "";
        } else {
            return item[index];
        }
    }

    public interface ReadFileDtoRev4 {
        int rev(String READ_FLD1, String READ_FLD2, String READ_FLD3, String READ_FLD4);
    }

    public interface ReadFileDtoRev5 {
        int rev(String READ_FLD1, String READ_FLD2, String READ_FLD3, String READ_FLD4, String READ_FLD5);
    }

    public interface ReadFileDtoRev2 {
        int rev(String READ_FLD1, String READ_FLD2);
    }

    public interface ReadFileDtoRev3 {
        int rev(String READ_FLD0, String READ_FLD1, String READ_FLD2);
    }

    public interface ReadFileDtoRev1 {
        int rev(String READ_FLD1);
    }

    public interface ReadFileDtoRev6 {
        int rev(String READ_FLD1, String READ_FLD2, String READ_FLD3, String READ_FLD4, String READ_FLD5, String READ_FLD6);
    }

    public interface ReadFileDtoRev {
        int rev(String... READ_FLDS);
    }
}
