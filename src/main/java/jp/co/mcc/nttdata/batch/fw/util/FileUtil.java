package jp.co.mcc.nttdata.batch.fw.util;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import jp.co.mcc.nttdata.batch.business.com.bt_aplcom.BT_aplcomService;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileReadBaseDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_NG;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_OK;
import static org.apache.commons.lang3.StringUtils.LF;

@Slf4j
public class FileUtil {

    public enum FileType {
        File,
        Directory
    }

    public static long contentLength(String file) {
        return new File(file).length();
    }

    public static boolean isFileExistByRegx(String distDirectorPath, String regx) {
        File file = new File(distDirectorPath);
        File[] files = file.listFiles((File dir, String name) -> {
            Pattern pattern = Pattern.compile(regx);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });
        return files != null && files.length > 0;
    }

    public static boolean deleteFileByRegx(String dstDir, String regx) {
        File[] files = new File(dstDir).listFiles((File dir, String name) -> {
            Pattern pattern = Pattern.compile(regx);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });
        for (File item : files) {
            item.delete();
        }
        return true;
    }


    public static boolean copyFileByRegx(String fromDir, String toDir, String fileName, String fromFileRegx) {
        if (!createFolder(toDir, false)) {
            return false;
        }
        File[] files = new File(fromDir).listFiles((File dir, String name) -> {
            Pattern pattern = Pattern.compile(fromFileRegx);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });
        boolean result = false;
        for (File item : files) {
            if (copyFile(item, new File(toDir + "/" + fileName))) result = true;
        }
        return result;
    }

    public static boolean copyFileByRegx(String fromDir, String toDir, String fromFileRegx) {
        if (!createFolder(toDir, false)) {
            return false;
        }
        File[] files = new File(fromDir).listFiles((File dir, String name) -> {
            Pattern pattern = Pattern.compile(fromFileRegx);
            Matcher matcher = pattern.matcher(name);
            return matcher.find();
        });
        boolean result = false;
        for (File item : files) {
            if (copyFile(item, new File(toDir + "/" + item.getName()))) result = true;
        }
        return result;
    }

    public static String readFile(String file, String charset) {
        try {
            StringBuffer sb = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file.replaceAll("\n", "")), charset));
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str + "\n");
            }
            in.close();
            String res = sb.toString();
            if (res.endsWith("\n")) {
                res = res.substring(0, res.length() - 1);
            }
            return res;
        } catch (Exception e) {
            return "";
        }

    }

    public static String readFileByRegex(String directory, String regex) {
        StringBuilder stringBuffer = new StringBuilder();
        File[] files = new File(directory).listFiles();
        if (null != files) {
            Pattern compile = Pattern.compile(regex);
            for (File file : files) {
                Matcher matcher = compile.matcher(file.getName());
                if (matcher.matches()) {
                    stringBuffer.append(readFile(file.getAbsolutePath()));
                }
            }
        }

        return stringBuffer.toString();

    }


    public static boolean readFileToDto(FileReadBaseDto obj, String fileName) {
        String fileContents = FileUtil.readFile(fileName);
        int start = 0;
        int end = 0;
        Field[] files = obj.getClass().getDeclaredFields();
        for (Field item : files) {
            try {
                ItemDto itemDto = (ItemDto) item.get(obj);
                end = itemDto.len;
                String content = fileContents.substring(start, end);
                start += end;
                item.set(obj, content);
                return true;
            } catch (IllegalAccessException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
        }
        return false;
    }

    public static boolean copyFile(File fromFile, File toFile) {
        return copyFile(fromFile, toFile, false);
    }

    public static boolean copyFile(File fromFile, File toFile, boolean append) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            boolean needAppendLine = false;
            if (append) {
                String str = readFile(toFile.getAbsolutePath());
                needAppendLine = !StringUtils.isEmpty(str) && !str.endsWith("\n");
            }

            fis = new FileInputStream(fromFile);
            // fos = new FileOutputStream(toFile, true);
            fos = new FileOutputStream(toFile, append);

            byte[] buf = new byte[4096];
            if (needAppendLine) {
                byte[] tmp = "\n".getBytes(StandardCharsets.UTF_8);
                fos.write(tmp);
            }
            int bytesRead;
            while ((bytesRead = fis.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
            fos.flush();
            fos.close();
            fis.close();

        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return false;
        }
        return true;
    }

    public static boolean copyFile(String from, String to, boolean append) {
        if (!createFolder(to, true)) {
            return false;
        }
        File fromFile = new File(from);
        File toFile = new File(to);
        return copyFile(fromFile, toFile, append);
    }

    public static boolean copyFile(String from, String to) {
        if (!createFolder(to, true)) {
            return false;
        }
        File fromFile = new File(from);
        File toFile = new File(to);
        return copyFile(fromFile, toFile);
    }

    public static void backupFile(String filePath) {
        String backupName = filePath + ".bak";
        File file = new File(backupName);
        if (file.exists()) {
            file.delete();
        }
        copyFile(filePath, backupName);
    }

    public static void copyDir(String fromDir, String toDir) throws IOException {
        new File(toDir).mkdirs();
        File[] file = new File(fromDir).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                String fromFile = file[i].getAbsolutePath();
                String toFile = toDir + "/" + file[i].getName();
                copyFile(fromFile, toFile);
            }
            if (file[i].isDirectory())
                copyDir(fromDir + "/" + file[i].getName(), toDir + "/"
                        + file[i].getName());
        }
    }

    public static int deleteFile(String path) {
        boolean result = true;
        File file = new File(path);
        if (file.exists()) {
            result = file.delete();
        }
        return result ? C_const_OK : C_const_NG;
    }

    public static int countLinesByRegex(String directory, String regex) {
        int i = 0;
        File[] files = new File(directory).listFiles();
        if (null != files) {
            Pattern compile = Pattern.compile(regex);
            for (File file : files) {
                Matcher matcher = compile.matcher(file.getName());
                if (matcher.matches()) {
                    i += file.length();
                }
            }
        }
        return i;
    }

    /**
     * @param directory
     * @param regex
     * @return filename list (xx.txt)
     */
    public static ArrayList<String> findByRegex(String directory, String regex) {
        ArrayList<String> stringList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        if (null != files) {
            Pattern compile = Pattern.compile(regex);
            for (File file : files) {
                Matcher matcher = compile.matcher(file.getName());
                if (matcher.matches()) {
                    stringList.add(file.getAbsolutePath());
                }
            }
        }
        return stringList;
    }

    public static void directoryFileNamesWritetoFile(String directory, String fileName) {
        ArrayList<String> stringList = new ArrayList<>();
        File[] files = new File(directory).listFiles();
        String fileNameStr = "";
        if (null != files) {
            for (File file : files) {
                stringList.add(file.getName());
            }

            fileNameStr = StringUtils.join(stringList, "\n");
        }
        FileUtil.writeFile(fileName, fileNameStr);
    }

    public static Integer countLines(List<String> files) {
        int sum = 0;
        for (String file : files) {
            Integer s = countLines(file);
            if (s == null) {
                return null;
            }
            sum += s;
        }
        return sum;
    }

    public static Integer countLines(String file) {
        int linesCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                linesCount++;
            }
            reader.close();
            return linesCount;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return null;
    }

    public static long countCharacters(String file) {
        try {
            return Files.size(Paths.get(file));
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return 0;
    }

    /**
     * 解压zip
     *
     * @param zip
     * @param dstPath
     * @throws IOException
     */
    public static int unzip(ZipFile zip, String dstPath) {
        File pathFile = new File(dstPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        try {

            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = zip.getInputStream(entry);
                    String outPath = (dstPath + "/" + zipEntryName).replaceAll("\\*", "/");

                    File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    if (new File(outPath).isDirectory()) {
                        continue;
                    }

                    out = new FileOutputStream(outPath);
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                } catch (IOException e) {
                    return -1;
                } finally {
                    if (null != in) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.error(ExceptionUtil.getExceptionMessage(e));

                        }
                    }

                    if (null != out) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            log.error(ExceptionUtil.getExceptionMessage(e));

                        }
                    }
                }
            }
            zip.close();
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return -1;
        }
        return 0;
    }

    public static int deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                int success = deleteDir(new File(dir, children[i]));
                if (success == C_const_NG) {
                    return C_const_NG;
                }
            }
        }
        return dir.delete() ? C_const_OK : C_const_NG;
    }

    public static byte[] readByte(InputStream is) {
        try {
            byte[] r = new byte[is.available()];
            is.read(r);
            return r;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return null;
    }

    public static byte[] readByte(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            byte[] r = new byte[fis.available()];
            fis.read(r);
            fis.close();
            return r;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return null;
    }

    public static String readFileToUtf8(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        String charset = getCharset(fileName);
        if (SystemConstant.UTF8.equals(charset)) {

            return readFile(fileName, charset);

        } else {
            return IconvUtil.main(charset, SystemConstant.UTF8, readFile(fileName));
        }
    }

    public static String readFileByShiftJis(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        return readFile(fileName, SystemConstant.Shift_JIS);
    }

    public static String readFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        String charset = getCharset(fileName);

        return readFile(fileName, charset);
    }

    public static String echoAndgrep(String str, String contains) {
        return Arrays.stream(str.split("\n")).filter(item -> item.contains(contains)).collect(Collectors.joining("\n"));
    }

    public static String readFileLineRpByRegx(String fileName, String charset, String regx, String des) {
        StringBuffer res = new StringBuffer();
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(fileName, Charset.forName(charset));
            bufferedReader = new BufferedReader(reader);
            String resData = null;
            while ((resData = bufferedReader.readLine()) != null) {
                res.append(resData.replaceAll(regx, des));
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return res.toString();

    }

    public static String[] containsFile(String dir) {
        File file = new File(dir);
        return file.list();
    }

    public static String lastModifyTime(String dirOrFile, String format) {
        long modifiedTime = 0;
        try {
            modifiedTime = Files.getLastModifiedTime(Path.of(dirOrFile)).toMillis();
            Instant instant = Instant.ofEpochMilli(modifiedTime);
            return instant.atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format)).toString();
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return "";
    }

    public static boolean isExistDir(String dir) {
        boolean isExist = false;
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            isExist = true;
        }
        return isExist;
    }

    public static boolean isExistFile(String dir) {
        boolean isExist = false;
        File file = new File(dir);
        if (file.exists() && file.isFile()) {
            isExist = true;
        }
        return isExist;
    }


    public static boolean isExistFileByRegx(String dir, String fileRegx) {
        if (findByRegex(dir, fileRegx).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean mkdir(String path) {
        if (StringUtils.isNotEmpty(path)) {
            File directory = new File(path);
            if (!directory.isDirectory()) {
                return directory.mkdir();
            }
            return true;
        }
        return false;
    }

    public static File getFile(FileType fileType, String path) throws IOException {
        if (StringUtils.isNotEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                if (FileType.File.equals(fileType)) {
                    file.createNewFile();
                } else {
                    file.mkdir();
                }
            }
            return file;
        }
        return null;
    }

//    public static void cpFile(String src, String dest) throws IOException {
//        Files.copy(Path.of(src), Path.of(dest));
//    }

    public static int writeFile(String fileName, String content) {
        return writeFile(fileName, content, "utf-8");
    }

    public static String[] splitData(String item) {
        return item.replaceAll("\\s+", " ").split(" ");
    }

    public static String SQL_CD_ORA_FILE(String fileName) {
        return SQL_CD_ORA_STR(FileUtil.readFile(fileName));
    }

    //        SQL_CD=`cat ${TEMP_FILE4} | sed s/ORA-/'\n'ORA-/ | grep "ORA-" |  cut -c5-9`
    public static String SQL_CD_ORA_STR(String str) {
        // return Arrays.stream(str.replaceAll("ORA-", "\nORA-").split("\n")).filter(item -> item.contains("ORA-")).map(item -> item.substring(4, 9)).findFirst().orElse("");
        return "";
    }

    public static String awk(String fileName, Predicate<? super String> filter, int mapIndex) {
        return Arrays.stream(readFile(fileName).split("\n")).filter(filter).map(item -> item.replaceAll("\\s+", " ").split(" ")[mapIndex]).collect(Collectors.joining("\n"));
    }

    public static String awk(String fileName, Predicate<? super String> filter) {
        return Arrays.stream(readFile(fileName).split("\n")).filter(filter).collect(Collectors.joining("\n"));
    }

    public static boolean createFolder(String path, boolean isFile) {
        File file = new File(path);
        if (isFile) {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    log.error(ExceptionUtil.getExceptionMessage(e));
                    return false;
                }
            }
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists())
                file.mkdirs();
        }
        return true;

    }

    public static int writeFileByAppend(String fileName, String content) {
        return writeFileByAppend(fileName, content, SystemConstant.UTF8);
    }

    public static int writeFileByAppend(String fileName, String content, String charset) {
        try {
            if (!createFolder(fileName, true)) {
                return C_const_NG;
            }
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, true), charset));
            if ((new File(fileName)).length() > 0) {
                out.write(LF + content);
            } else {
                out.write(content);
            }
            out.close();
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
        return C_const_OK;
    }

    public static int writeFile(String fileName, String content, String charset) {
        try {
            if (!createFolder(fileName, true)) {
                return C_const_NG;
            }
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), charset));
            out.write(content);
            out.close();
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
        return C_const_OK;
    }

    public static int mvFile(String src, String dest) {
        try {
//            File file = Path.of(dest).toFile();
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
            Files.move(Path.of(src), Path.of(dest), StandardCopyOption.REPLACE_EXISTING);
            return C_const_OK;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
    }

    public static int mvFileByRegx(String fromDir, String toDir, String fromFileRegx) {
        try {
            if (!createFolder(toDir, false)) {
                return C_const_NG;
            }
            File[] files = new File(fromDir).listFiles((File dir, String name) -> {
                Pattern pattern = Pattern.compile(fromFileRegx);
                Matcher matcher = pattern.matcher(name);
                return matcher.find();
            });
            for (File item : files) {
                Files.move(item.toPath(), Path.of(toDir + "/" + item.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
            return C_const_OK;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
    }

    public static boolean mvFileInWorkDir(String dir, String src, String dest) {
        try {
            File file = Path.of(dir + "/" + dest).toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            Files.move(Path.of(dir + "/" + src), Path.of(dir + "/" + dest), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return false;
        }
    }

    public static String getCharset(String file) {
        return getEncode(file);
    }
//    public static String getCharset(File file) {
//        String charset = "UTF-8";
//        byte[] first3Bytes = new byte[3];
//        try {
//            boolean checked = false;
//            BufferedInputStream bis = new BufferedInputStream(
//                    new FileInputStream(file));
//            bis.mark(0);
//            int read = bis.read(first3Bytes, 0, 3);
//            if (read == -1)
//                return charset;
//            if ((first3Bytes[0] == -1) && (first3Bytes[1] == -2)) {
//                charset = "UTF-16LE";
//                checked = true;
//            } else if ((first3Bytes[0] == -2) && (first3Bytes[1] == -1)) {
//                charset = "UTF-16BE";
//                checked = true;
//            } else if ((first3Bytes[0] == -17) && (first3Bytes[1] == -69)
//                    && (first3Bytes[2] == -65)) {
//                charset = "UTF-8";
//                checked = true;
//            } else if ((first3Bytes[0] == 35) && (first3Bytes[1] == 33)
//                    && (first3Bytes[2] == 47)) {
//                charset = "UTF-8";
//                checked = true;
//            } else {
//                charset = "UTF-8";
//            }
//
//            bis.reset();
//            if (!checked) {
//                if (detectShiftJISEncoding(bis)) {
//                    charset = "Shift_JIS";
//                    checked = true;
//                }
//            }
//            bis.reset();
//
//            if (!checked) {
//                int loc = 0;
//                while ((read = bis.read()) != -1) {
//                    loc++;
//                    if (read >= 240) {
//                        break;
//                    }
//                    if ((128 <= read) && (read <= 191))
//                        break;
//                    if ((192 <= read) && (read <= 223)) {
//                        read = bis.read();
//                        if ((128 > read) || (read > 191)) {
//                            break;
//                        }
//
//                    } else if ((224 <= read) && (read <= 239)) {
//                        read = bis.read();
//                        if ((128 > read) || (read > 191))
//                            break;
//                        read = bis.read();
//                        if ((128 > read) || (read > 191))
//                            break;
//                        charset = "UTF-8";
//                        break;
//                    }
//
//                }
//
//            }
//
//            bis.close();
//        } catch (Exception e) {
//            log.error(ExceptionUtil.getExceptionMessage(e));
//        }
//        return charset;
//    }

    public static boolean detectShiftJISEncoding(BufferedInputStream fis) {
        try {
            byte[] header = new byte[4096];
            int read = fis.read(header);
            if (read > 0) {
                return isShiftJIS(header, read);
            }
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return false;
    }

    private static boolean isShiftJIS(byte[] bytes, int length) {
        try {
            Charset.forName(SystemConstant.Shift_JIS).newDecoder().decode(ByteBuffer.wrap(bytes, 0, length));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getEncode(String fileName) {
        try {
            byte[] data = Files.readAllBytes(new File(fileName.replaceAll("\n", "")).toPath());
            CharsetDetector detector = new CharsetDetector();
            detector.setText(data);
            CharsetMatch match = detector.detect();
            return match.getName();
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return "UTF-8";
        }
    }

    public static int fileCoverToCRLF(String s, String d) {
        String coverStr = Objects.requireNonNull(readFile(s)).replaceAll(BT_aplcomService.LF, BT_aplcomService.CRLF);
        return writeFile(d, coverStr);
    }
}
