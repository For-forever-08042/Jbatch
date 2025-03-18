package jp.co.mcc.nttdata.batch.fw.util;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.IOException;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_NG;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_OK;

@Slf4j
public class ZipUtil {
    public static int unzip(String zipFilePath, String destDirectoryPath) {
        ZipFile zipFile = new ZipFile(zipFilePath);
        try {
            zipFile.extractAll(destDirectoryPath);
        } catch (ZipException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
        return C_const_OK;
    }

    public static int unzip(String zipFilePath, String destDirectoryPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            zipFile.setPassword(password.toCharArray()); // 设置ZIP文件的密码
            zipFile.extractAll(destDirectoryPath);
        } catch (ZipException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
        return C_const_OK;
    }

    public static int zipFile(String zipFileName, String sourceFile, StringDto resultDto) {
        boolean result = false;
        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            File sorce = new File(sourceFile);
            ZipParameters zipParameters = getParamNonePass();
            addFileToZip(zipFile, zipParameters, sorce);
            result = zipFile.isValidZipFile();
            zipFile.close();
        } catch (ZipException e) {
            resultDto.arr = e.getType().name();
            log.error(ExceptionUtil.getExceptionMessage(e));
        } catch (IOException e) {
            resultDto.arr = e.getMessage();
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return result ? C_const_OK : C_const_NG;
    }

    public static int zipFileAndDelete(String zipFileName, String sourceFile, StringDto resultDto) {
        int result = zipFile(zipFileName, sourceFile, resultDto);
        FileUtil.deleteFile(sourceFile);
        return result;
    }

    public static int zipFileAndDelete(String zipFileName, String sourceFile) {
        int result = zipFile(zipFileName, sourceFile);
        FileUtil.deleteFile(sourceFile);
        return result;
    }


    public static int zipFile(String zipFileName, String sourceFile) {
        boolean result = false;
        try {
            ZipFile zipFile = new ZipFile(zipFileName);
            File sorce = new File(sourceFile);
            ZipParameters zipParameters = getParamNonePass();
            addFileToZip(zipFile, zipParameters, sorce);
            result = zipFile.isValidZipFile();
            zipFile.close();
        } catch (ZipException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return result ? C_const_OK : C_const_NG;
    }


    public static void main(String[] args) {
        zipFile("D:\\ROOT\\20240304\\prod.zip", "D:\\ROOT\\20240304\\param\\cmBTpthcP040");
    }


    public static int zipFileAndDelete(String zipFileName, String sourceFile, String password, StringDto resultOutput) {
        int result = zipFile(zipFileName, sourceFile, password, resultOutput);
        FileUtil.deleteFile(sourceFile);
        return result;
    }


    public static int zipFileAndDelete(String zipFileName, String sourceFile, String password) {
        int result = zipFile(zipFileName, sourceFile, password);
        FileUtil.deleteFile(sourceFile);
        return result;
    }


    public static int zipFile(String zipFileName, String sourceFile, String password, StringDto resultOutput) {
        boolean result = false;
        try {
            ZipFile zipFile = new ZipFile(zipFileName, password.toCharArray());
            File sorce = new File(sourceFile);
            ZipParameters zipParameters = getParamPass();
            addFileToZip(zipFile, zipParameters, sorce);
            result = zipFile.isValidZipFile();
            zipFile.close();
        } catch (ZipException e) {
            resultOutput.arr = e.getType().name();
            log.error(ExceptionUtil.getExceptionMessage(e));
        } catch (Exception e) {
            resultOutput.arr = e.getMessage();
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return result ? C_const_OK : C_const_NG;
    }


    public static int zipFile(String zipFileName, String sourceFile, String password) {
        boolean result = false;
        try {
            ZipFile zipFile = new ZipFile(zipFileName, password.toCharArray());
            File sorce = new File(sourceFile);
            ZipParameters zipParameters = getParamPass();
            addFileToZip(zipFile, zipParameters, sorce);
            result = zipFile.isValidZipFile();
            zipFile.close();
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return result? C_const_OK : C_const_NG;
    }

    private static void addFileToZip(ZipFile zipFile, ZipParameters zipParameters, File sorce) throws ZipException {
        if (sorce.isDirectory()) {
            for (File file : sorce.listFiles()) {
                if (file.isDirectory()) {
                    zipFile.addFolder(file, zipParameters);
                } else {
                    zipFile.addFile(file, zipParameters);
                }
            }
        } else {
            zipFile.addFile(sorce, zipParameters);
        }
    }

    private static ZipParameters getParamPass() {
        ZipParameters parameters = getParamNonePass();
        parameters.setEncryptionMethod(EncryptionMethod.AES);
        parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        return parameters;
    }

    private static ZipParameters getParamNonePass() {
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        return parameters;
    }
}
