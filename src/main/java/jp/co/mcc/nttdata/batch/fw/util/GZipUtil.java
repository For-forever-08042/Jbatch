package jp.co.mcc.nttdata.batch.fw.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GZipUtil {

    public static boolean compressGZipFile(String filePath) {
        if (!FileUtil.isExistFile(filePath)) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        GZIPOutputStream gzipOutputStream = null;

        try {
            String outputFileName = filePath + ".gz";
            fileOutputStream = new FileOutputStream(outputFileName);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            gzipOutputStream.write(FileUtil.readByte(filePath));
            gzipOutputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.getLocalizedMessage();
                }
            }
            if (gzipOutputStream != null) {
                try {
                    gzipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean compressGzip(String outputName, String sourceFile) {
        if (!FileUtil.isExistFile(sourceFile)) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        GZIPOutputStream gzipOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(outputName);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            gzipOutputStream.write(FileUtil.readByte(sourceFile));
            gzipOutputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.getLocalizedMessage();
                }
            }
            if (gzipOutputStream != null) {
                try {
                    gzipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str
                .getBytes("ISO-8859-1"));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toString();
    }
}
