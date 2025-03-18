package jp.co.mcc.nttdata.batch.business.service.db;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.CLanguageFunction;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;


/**
 * SQL PLUS & SQLLDR
 */
@Slf4j
public class ShellClientManager {
    public static ShellExecuteDto getShellExecuteDto(String fileName) {
        if (!fileName.endsWith(".sh")) {
            fileName = CLanguageFunction.getenv(C_aplcom1Service.CM_APJCL) + "/sh/" + fileName + ".sh";
        }
        return new ShellExecuteDto().setCommand("sh", fileName);
    }

    public static ShellExecuteDto getSqlPlusExecuteDto(String fileName) {
        if (!fileName.endsWith(".sh")) {
            fileName = CLanguageFunction.getenv(C_aplcom1Service.CM_APJCL) + "/sh/" + fileName + ".sh";
        }
        return new ShellExecuteDto().setCommand("sh", fileName);
    }

    public static ShellExecuteDto getShellExecuteDto(String fileName, String... params) {
        return getSqlPlusExecuteDto(fileName).setFileName(fileName).setCommand(params);
    }

    public static void executeCommand(ShellExecuteDto shellExecuteDto) {
        log.debug("sh start:{},params:{},envs {}", shellExecuteDto.fileName,
                String.join(",", shellExecuteDto.commands),
                String.join(",", shellExecuteDto.getEvn().values())
        );
        ProcessBuilder pb = new ProcessBuilder(shellExecuteDto.getCommands());
        pb.environment().putAll(shellExecuteDto.getEvn());
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            //結果戻る
            shellExecuteDto.setResult(getStream2Str(in));
            //エラー戻る
            shellExecuteDto.setError(getStream2Str(err));

            process.waitFor();
            in.close();
            err.close();
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
    }

    private static String getStream2Str(BufferedReader in) {
        StringBuffer buffer = new StringBuffer();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return buffer.toString();
    }

    public static void main(String[] args) {

//        hashMap.put("CONNECT_SD", getenv("CM_USR_SD") + "/" + getenv("CM_PSW_SD") + "@" + getenv("CM_ORA_SID_SD"));

//        File file =new File("D:\\GIT\\Pentasenser\\20240604_非互換対応資材\\顧客バッチ\\jcl");
//        for(File item : file.listFiles()){
//            byte[] first3Bytes = new byte[3];
//            boolean checked = false;
//            BufferedInputStream bis = null;
//            try {
//                bis = new BufferedInputStream(
//                        new FileInputStream(item));
//                bis.mark(0);
//                try {
//                    int read = bis.read(first3Bytes, 0, 3);
//                    System.out.println(item.getName()+" : "+first3Bytes[0]+" "+first3Bytes[1]+" "+first3Bytes[2]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//        }
//        FileUtil.getCharset("C:\\Users\\cong.ding\\Desktop\\s2.txt");


        String ttt = FileUtil.getCharset("C:\\Users\\cong.ding\\Desktop\\ADDRESS_MASTER");
        System.out.println(ttt);
    }
}
