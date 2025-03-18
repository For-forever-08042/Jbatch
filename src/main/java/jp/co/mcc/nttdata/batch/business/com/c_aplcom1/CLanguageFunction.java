package jp.co.mcc.nttdata.batch.business.com.c_aplcom1;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.*;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_NG;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_OK;

@Slf4j
public class CLanguageFunction {

    public static int rtn_cd() {
        return System.identityHashCode(Integer.valueOf(0));
    }

    public static int address(Object obj) {
        return System.identityHashCode(obj);
    }

    public static void memset(Object str, int hex, int size) {
        //noting to do
        if (null != str) {
            if (str instanceof StringDto) {
                ((StringDto) str).memset();
                ((StringDto) str).len = size;
            }
            if (str instanceof ItemDto) {
                ((ItemDto) str).memset();
                ((ItemDto) str).len = size;
            } else if (str instanceof String[]) {
                ((String[]) str)[0] = "";
            } else if (str instanceof String
                    || str instanceof Integer) {
                //nothing to do
            } else if (str instanceof TBLBaseDto) {
                ((TBLBaseDto) str).memset();
            } else {
//                Class<?> parent = str.getClass();
//                while (parent != null) {
//                    Field[] fields = parent.getDeclaredFields();
//                    for (Field field : fields) {
//                        try {
//                            if ((field.get(str) instanceof ItemDto || field.get(str) instanceof StringDto || field.get(str) instanceof IntegerDto) && field.get(str) == null) {
//                                Constructor<?> constructor = field.getDeclaringClass().getConstructor();
//                                field.set(str, constructor.newInstance());
//                            }
//                        } catch (Exception e) {
//                            log.error(ExceptionUtil.getExceptionMessage(e));
//                        }
//                    }
//                }
            }
        }
    }

    public static String appendZero(int size) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            i++;
            builder.append("0");
        }
        return builder.toString();
    }

    public static String toLowerCaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        char updatedFirstChar = Character.toLowerCase(firstChar);
        String remainder = str.substring(1);
        return updatedFirstChar + remainder;
    }

    public static void strncpy(StringDto str1, String str2, int len) {
        str1.arr = (str2 == null ? "" : str2.substring(0, Math.max(Math.min(len, str2.length()), 0)));
    }

    public static void strncpy(StringDto str1, StringDto str2, int len) {
        str1.arr = (str2.arr == null ? "" : str2.arr.substring(0, Math.max(Math.min(len, str2.arr.length()), 0)));
    }

    public static int malloc(int dataLen) {
        return dataLen;
    }

    public static String strncpy(String str1, String str2, int len) {

        return str2 == null ? "" : str2.substring(0, Math.max(Math.min(len, str2.length()), 0));
    }

    public static void strncpy(ItemDto str1, ItemDto str2, int len) {
        str1.arr = (str2.strVal() == null ? "" : str2.strVal().substring(0, Math.max(Math.min(len, str2.strVal().length()), 0)));
    }

    public static void strncpy(ItemDto str1, String str2, int len) {
        str1.arr = (str2 == null ? "" : str2.substring(0, Math.max(Math.min(len, str2.length()), 0)));
    }
//
//    public static int sizeof(StringDto str) {
//        return str.len == 0 ? sizeof(str.arr) : str.len;
//    }
//
//    public static int sizeof(String str) {
//        return str == null ? 0 : str.length();
//    }

    public static int sizeof(Object str) {
        if (str instanceof ItemDto) {
            ItemDto io = (ItemDto) str;
            if (0 != io.len) {
                return io.len;
            }
            return io.arr().length();
        } else if (str instanceof StringDto) {
            StringDto io = (StringDto) str;
            if (0 != io.len) {
                return io.len;
            }
            return io.arr.length();
        } else if (str instanceof String) {
            return ((String) str).getBytes().length;
        } else {
            return String.valueOf(str).getBytes().length;
        }
    }

    public static int sizeof(String str, String encode) {
        try {
            return str.getBytes(encode).length;
        } catch (UnsupportedEncodingException e) {
            return str.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    public static void resetData(Object... objects) {
//        ArrayList<String> ps = new ArrayList<>();
//        for (int i = 0; i < format.length(); i++) {
//            if (format.charAt(i) == '%') {
//                for (String ip : defaultKey) {
//                    i++;
//                    if (ip.equals(format.charAt(i))) {
//                        ps.add(ip);
//                    }
//                }
//            }
//        }

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof StringDto) {
                objects[i] = ((StringDto) objects[i]).arr == null ? "" : ((StringDto) objects[i]).arr;
            } else if (objects[i] instanceof IntegerDto) {
                objects[i] = ((IntegerDto) objects[i]).arr;
            } else if (objects[i] instanceof String[]) {
                objects[i] = ((String[]) objects[i])[0] == null ? "" : ((String[]) objects[i])[0];
            }
            if (objects[i] instanceof ItemDto) {
                if (((ItemDto) objects[i]).arr instanceof Number) {
                    objects[i] = ((ItemDto) objects[i]).longVal();
                } else {
                    objects[i] = ((ItemDto) objects[i]).arr;
                }
            }
        }
    }

    private static String[] defaultKey = {"d", "s", "f"};

    public static void sprintf(String[] msg_param, String format, Object... objects) {
        resetData(objects);
        msg_param[0] = String.format(format, objects);
    }

    public static String sprintf(String format, String objects) {
        return String.format(format, objects);
    }

    public static String sprintf(String msg_param, String format, Object... objects) {
        resetData(objects);
        resetParamType(format, objects);
        return String.format(format, objects);
    }

    public static void sprintf(StringDto msg_param, String format, Object... objects) {
        resetData(objects);
        resetParamType(format, objects);
        msg_param.arr = String.format(format, objects);
    }

    public static void resetParamType(String format, Object... objects) {
        ArrayList<String> args = getPs(format);
        for (int i = 0; i < args.size(); i++) {
            String foi = args.get(i);
            if (foi.endsWith("f")) {
                String arr = String.valueOf(objects[i]);
                objects[i] = Float.valueOf((null == objects[i] || "".equals(arr)) ? "0" : arr);
            } else if (foi.endsWith("d")) {
                String arr = String.valueOf(objects[i]);
                objects[i] = Long.valueOf((null == objects[i] || "".equals(arr)) ? "0" : arr);
                ;
            } else if (foi.endsWith("s")) {
                objects[i] = String.valueOf(objects[i]);
            }
        }
    }

    public static ArrayList<String> getPs(String p) {
        String wk_type = "diuoxXeEfFgGcpsS";
        int i = 0;
        ArrayList<String> strings = new ArrayList<>();
        StringBuffer p_dst_tmp = new StringBuffer();
        while (null != p && i < p.length()) {

            if (p.charAt(i) == '%') {
                p_dst_tmp.append("%");
                while (true) {
                    i++;
                    if (p.length() < i) {
                        break;
                    }
                    p_dst_tmp.append(p.charAt(i));
                    if (wk_type.indexOf(p.charAt(i)) >= 0) {
                        break;
                    }
                }
                strings.add(p_dst_tmp.toString());
                p_dst_tmp.setLength(0);
            }
            i++;
        }
        return strings;
    }

    public static void sprintf(ItemDto msg_param, String format, Object... objects) {
        resetData(objects);
        msg_param.arr = String.format(format, objects);
    }

    public String strchr(StringDto src, char delimiter) {
        return src.strchr(delimiter);
    }

    public static int atoi(ItemDto msg_param) {
        return msg_param.intVal();
    }

    public static int atoi(char msg_param) {
        return Integer.valueOf(msg_param);
    }

    public static int atoi(StringDto msg_param) {
        return atoi(msg_param.arr);
    }

    public static double atof(StringDto msg_param) {
        return StringUtils.isEmpty(msg_param.arr) ? 0.0 : Double.valueOf(msg_param.arr);
    }

    public static double atof(ItemDto msg_param) {
        return msg_param.floatVal();
    }

    public static double atof(String msg_param) {
        return StringUtils.isEmpty(msg_param) ? 0.0 : Double.valueOf(msg_param);
    }

    public static int atoi(String msg_param) {
        try {
            return StringUtils.isBlank(msg_param) ? 0 : Integer.valueOf(msg_param.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static int memcmp(StringDto str1, String str2, int len) {
        return memcmp(str1.arr, str2, len);
    }


    public static String memcpy(String str1, String str2, int len) {
        return str2 == null ? "" : str2.substring(0, Math.min(len, str2.length()));
    }

    public static void memcpy(ItemDto str1, StringDto str2, IntegerDto len) {
        memcpy(str1, str2, len.arr);
    }

    /**
     * @param str1 StringDto ,ItemDto
     * @param str2 StringDto ,ItemDto ,String
     * @param len
     */
    public static void memcpy(ItemDto str1, Object str2, int len) {
        String s1, s2;
        if (str2 == null) {
            return;
        }
        if (str2 instanceof StringDto) {
            s2 = ((StringDto) str2).arr;
        } else if (str2 instanceof ItemDto) {
            s2 = ((ItemDto) str2).strVal();
        } else if (str2 instanceof String) {
            s2 = (String) str2;
        } else {
            s2 = String.valueOf(str2);
        }
        s1 = s2 == null ? "" : s2.substring(0, Math.min(len, s2.length()));
        str1.arr = s1;
    }

    public static void memcpy(StringDto str1, Object str2, int len) {
        String s1, s2;
        if (str2 == null) {
            return;
        }
        if (str2 instanceof StringDto) {
            s2 = ((StringDto) str2).arr;
        } else if (str2 instanceof ItemDto) {
            s2 = ((ItemDto) str2).strVal();
        } else if (str2 instanceof String) {
            s2 = (String) str2;
        } else {
            s2 = String.valueOf(str2);
        }
        s1 = s2 == null ? "" : s2.substring(0, Math.min(len, s2.length()));
        str1.arr = s1;
    }
//    public static void memcpy(StringDto str1, StringDto str2, int len) {
//        str1.arr = str2.arr == null ? "" : str2.arr.substring(0, Math.min(len, str2.size()));
//    }
//
//    public static void memcpy(StringDto str1, ItemDto str2, int len) {
//        str1.arr = str2.strVal() == null ? "" : str2.strVal().substring(0, Math.min(len, str2.size()));
//    }
//
//    public static void memcpy(StringDto str1, String str2, int len) {
//        str1.arr = str2 == null ? "" : str2.substring(0, Math.min(len, str2.length()));
//    }
//
//    public static void memcpy(ItemDto str1, StringDto str2, int len) {
//        str1.arr = str2 == null ? "" : str2.arr.substring(0, Math.min(len, str2.arr.length()));
//    }
//
//    public static void memcpy(ItemDto str1, String str2, int len) {
//        str1.arr = str2 == null ? "" : str2.substring(0, Math.min(len, str2.length()));
//    }
//
//    public static void memcpy(ItemDto str1, ItemDto str2, int len) {
//        str1.arr = str2.strVal() == null ? "" : str2.strVal().substring(0, Math.min(len, str2.strVal().length()));
//    }

    public static int memcmp(String str1, String str2, int len) {
        if (StringUtils.isNotEmpty(str1) && StringUtils.isNotEmpty(str2)) {
            if (str1.substring(0, len).equals(str2.substring(0, len))) {
                return 0;
            }
        }
        return 1;
    }

    public static int strlen(String str1, String character) {
        try {
            return str1 == null ? 0 : str1.getBytes(character).length;
        } catch (UnsupportedEncodingException e) {
            return str1.getBytes().length;
        }
    }

    public static int strlen(ItemDto str1) {
        if (str1 == null) {
            return 0;
        }
        return str1.arr == null ? 0 : str1.strVal().getBytes().length;
    }

    public static int strlen(StringDto str1) {
        if (str1 == null) {
            return 0;
        }
        return str1.arr == null ? 0 : str1.arr.getBytes().length;
    }

    public static int strlen(String str1) {
        return str1 == null ? 0 : str1.getBytes().length;
    }

    public static int strlenCp932(String str1) {
        try {
            return str1 == null ? 0 : str1.getBytes("CP932").length;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }


    /**
     * stdlib.h
     * char *__cdecl getenv(const char *_VarName) __MINGW_ATTRIB_DEPRECATED_SEC_WARN;
     *
     * @param key
     * @return
     */
    public static String getenv(String key) {
        String res = StringUtils.isEmpty(System.getProperty(key)) ? System.getenv(key) : System.getProperty(key);
        return StringUtils.isEmpty(res) ? "" : res;
    }

    public static String setenv(String key, String value) {
        return System.setProperty(key, value);
    }


    public static void strtok(StringDto dto, String value) {
        dto.arr = dto.arr.split(value)[0];
    }

    /**
     * need receive
     *
     * @param dist
     * @param src
     * @return
     */
    public static void strcpy(String[] dist, String src) {
        dist[0] = src;
    }

    public static String strcpy(String dist, String src) {
        return src;
    }


    public static void strcpy(StringDto dist, String src) {
        dist.arr = src;
    }

    public static void strcpy(StringDto dist, StringDto src) {
        dist.arr = src.arr;
    }

    public static void strcpy(ItemDto dist, ItemDto src) {
        dist.arr = src.arr;
    }

    public static void strcpy(ItemDto dist, String src) {
        dist.arr = src;
    }

    public static void strcpy(ItemDto dist, StringDto src) {
        dist.arr = src.arr;
    }

    public static String strcat(String dist, String src) {
        return dist + src;
    }

    public static void strcat(String[] dist, String src) {
        dist[0] = dist[0] + src;
    }

    public static void strcat(StringDto dist, String src) {
        String value = null == dist.arr ? "" : dist.arr;
        dist.arr = value + src;
    }

    public static void strcat(StringDto dist, StringDto src) {
        dist.arr = (dist.arr == null ? "" : dist.arr) + (src.arr == null ? "" : src.arr);
    }


    public static Integer strstr(String src, String dst) {
        int i = src.indexOf(dst);
        if (i < 0) {
            return null;
        }
        return i;
    }

    public static void strncat(StringDto str, String des, int len) {
        if (!StringUtils.isEmpty(des)) {
            des = des.substring(0, len);
        } else {
            des = "";
        }
        strcat(str, des);
    }

    public static Integer strncmp(String str1, String str2, int len) {
        if (StringUtils.isNotEmpty(str1) && StringUtils.isNotEmpty(str2)) {
            byte[] b1 = str1.getBytes();
            byte[] b2 = str2.getBytes();
            for (int i = 0; i < len; i++) {
//                if (b1.length != b2.length) {
//                    return 1;
//                }
                if (b1[i] != b2[i]) {
                    return 1;
                }
            }
//            if (str1.substring(0, Math.min(len,str1.length())).equals(str2.substring(0, Math.min(len,str2.length())))) {
//                return 0;
//            }
        } else if (StringUtils.isNotEmpty(str1)) {
            return 1;
        } else if (StringUtils.isNotEmpty(str2)) {
            return -1;
        }
        return 0;
    }

    public static int strpbrk(StringDto str1, String str2) {
        int res = str1.arr.indexOf(str2);
        return res;
    }

    public static int strcmp(StringDto str1, StringDto str2) {
        return strcmp(str1.arr, str2.arr);
    }

    public static int strcmp(StringDto str1, String str2) {
        return strcmp(str1.arr, str2);
    }

    public static int strcmp(String str1, String str2) {
        if (StringUtils.isNotEmpty(str1) && StringUtils.isNotEmpty(str2)) {
            return str1.compareTo(str2);
        }
        if (StringUtils.isNotEmpty(str1)) {
            return 1;
        }
        if (StringUtils.isNotEmpty(str2)) {
            return -1;
        }
        return 0;
    }


    public static LinkedList<File> opendir(String dir) {
        File file = new File(dir);
        LinkedList<File> re = new LinkedList<>();
        File[] result = file.listFiles();
        if (result == null) {
            return null;
        }
        re.addAll(Arrays.asList(result));
        return re;
    }

    public static File readdir(LinkedList<File> f) {
        if (CollectionUtils.isEmpty(f)) {
            return null;
        }
        return f.pop();
    }

    public static void closedir(LinkedList<File> f) {
        f.clear();
    }

    /*public static Integer strchr(String str1, char str2) {
        int index = str1.indexOf(str2);
        if (index < 0) {
            return null;
        }
        return index;
    }*/

    public static int stat(String str1, File str2) {
        return new File(str1).exists() ? 0 : -1;
    }

    public static FileStatusDto open(String str1) {
        FileStatusDto fileStatusDto = new FileStatusDto();
        try {
            File file = new File(str1);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileChannel channel = FileChannel.open(Paths.get(str1), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            fileStatusDto.fileChannel = channel;
            fileStatusDto.fileName = str1;
            fileStatusDto.setReaderChannel(FileChannel.open(Paths.get(str1), StandardOpenOption.READ));
            fileStatusDto.setReaderChannel(FileChannel.open(Paths.get(str1), StandardOpenOption.READ));
            fileStatusDto.fd = 0;
        } catch (IOException e) {
            fileStatusDto.fd = -1;
            fileStatusDto.error = e.getMessage();
        }
        return fileStatusDto;
    }

    public static FileStatusDto fopen(StringDto str1, String character, FileOpenType fileOpenType) {
        return fopen(str1.arr, character, fileOpenType);
    }

//    public static FileStatusDto fopen(String str1, String character, FileOpenType fileOpenType) {
//        return Fopen(str1, character, fileOpenType);
//    }

    public static int fscanf(FileStatusDto fp_inp, String format, ItemDto d1, ItemDto d2) {
        if ("%[^,],%[^\r\n]".equals(format)) {
            String data = fp_inp.fget(d1.len + d2.len);
            if (data == null) {
                return C_const_NG;
            }
            String[] datas = data.replace("\n", "").split(",");
            if (datas.length >= 2) {
                d1.arr = datas[0];
                d2.arr = datas[1];
                return C_const_OK;
            }
        }
        return C_const_NG;
    }

    public enum FileOpenType {
        r, w, a
    }

    public static FileStatusDto fopen(String str1, FileOpenType fileOpenType) {
        return fopen(str1, FileUtil.getCharset(str1), fileOpenType);
    }

    public static FileStatusDto fopen(String str1, String character, FileOpenType fileOpenType) {
        FileStatusDto fileStatusDto = new FileStatusDto();
        try {
            File file = new File(str1);
            fileStatusDto.fd = C_const_OK;
            switch (fileOpenType) {
                case a:
                    if (!file.exists()) {
                        if (null != file.getParentFile()) {
                            file.getParentFile().mkdirs();
                        }
                        file.createNewFile();
                    }
                    fileStatusDto.fileChannel = FileChannel.open(Paths.get(str1), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                    fileStatusDto.setReaderChannel(FileChannel.open(Paths.get(str1), StandardOpenOption.READ));
                    break;
                case w:
                    if (!file.exists()) {
//                        if (file.getParentFile() != null) {
//                            file.getParentFile().mkdirs();
//                        }
                        file.createNewFile();
                    }
                    fileStatusDto.fileChannel = FileChannel.open(Paths.get(str1), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                    fileStatusDto.fileChannel.truncate(0);
                    break;
                case r:
                    if (!file.exists()) {
                        fileStatusDto.fd = C_const_NG;
                    } else {
                        fileStatusDto.setReaderChannel(FileChannel.open(Paths.get(str1), StandardOpenOption.READ));
                    }
                    break;
            }

            fileStatusDto.fileName = str1;
            fileStatusDto.charset = Charset.forName(character);
        } catch (Exception e) {
            fileStatusDto.fd = C_const_NG;
            fileStatusDto.error = e.getMessage();
        }
        return fileStatusDto;
    }

    public static double getDoubleCus(Object o) {
        if (o instanceof StringDto) {
            return Double.parseDouble(((StringDto) o).arr);
        } else if (o instanceof IntegerDto) {
            return ((IntegerDto) o).arr;
        } else if (o instanceof ItemDto) {
            return ((ItemDto) o).floatVal();
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof ItemDto) {
            return ((ItemDto) o).floatVal();
        } else {
            return Double.parseDouble(o == null ? "0" : String.valueOf(o));
        }
    }

    public static long getLongCus(Object o) {
        if (o instanceof StringDto) {
            return Long.valueOf(((StringDto) o).arr == null ? "0" : ((StringDto) o).arr);
        } else if (o instanceof IntegerDto) {
            return ((IntegerDto) o).arr;
        } else if (o instanceof String) {
            return Long.valueOf((String) o);
        } else if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (o instanceof ItemDto) {
            return ((ItemDto) o).longVal();
        } else if (o instanceof FileStatusDto) {
            return ((FileStatusDto) o).fd;
        } else {
            return Long.valueOf(o == null ? "0" : String.valueOf(o));
        }
    }

    public static long atol(Object o) {
        try {
            if (o instanceof StringDto) {
                return Long.parseLong(((StringDto) o).arr);
            } else if (o instanceof IntegerDto) {
                return ((IntegerDto) o).arr;
            } else if (o instanceof ItemDto) {
                return ((ItemDto) o).longVal();
            } else if (o instanceof Number) {
                return ((Number) o).longValue();
            } else if (o instanceof ItemDto) {
                return ((ItemDto) o).longVal();
            } else {
                return Long.valueOf(o == null ? "0" : String.valueOf(o));
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public static int isdigit(char c) {
        return Character.isDigit(c) ? 1 : 0;
    }


//    public static void main(String[] args) {
//        NYUKAI_RENDO_DATA in_nyukai_rendo = new NYUKAI_RENDO_DATA();
//        FileStatusDto fp_in = open("D:\\ROOT\\20240304\\work\\cf20221124.txt", SystemConstant.Shift_JIS);
//        String ss = fread("", 16, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//        fread(in_nyukai_rendo, 0, 1, fp_in);
//    }

    public static void fread(FileReadBaseDto obj, int size, int count, FileStatusDto fp_in) {
        fp_in.freadDto(obj, count);
    }

    public static String fread(String obj, int size, int count, FileStatusDto fp_in) {
        return fp_in.freadStr(size, count);
    }

    public static int feof(FileStatusDto fp_in) {
        return fp_in.feof();
    }

    public static int fputs(StringDto outbuf, FileStatusDto fp_out) {
        return fp_out.write(outbuf.arr);
    }

    public static int remove(String fileName) {
        return FileUtil.deleteFile(fileName);
    }

    public static void fgets(StringDto s, int len, FileStatusDto fp_in) {
        s.arr = fp_in.fget(len);
    }

    public static StringDto[] qsort(StringDto[] base, int num, int width) {
        return Arrays.stream(base).sorted((st1, st2) -> strcmp(st1.arr, st2.arr)).toArray(StringDto[]::new);
    }

    public static String[] qsort(String[] base, int num, int width) {
        return Arrays.stream(base).sorted((st1, st2) -> strcmp(st1, st2)).toArray(String[]::new);
    }
}
