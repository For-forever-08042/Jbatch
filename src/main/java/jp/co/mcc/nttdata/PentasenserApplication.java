package jp.co.mcc.nttdata;

import jp.co.mcc.nttdata.batch.fw.JobLaunchService;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.com.dto.JobResult;
import jp.co.mcc.nttdata.batch.fw.com.exception.BatchBusinessException;
import jp.co.mcc.nttdata.cmAOclibJ.util.CMAOConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@EnableBatchProcessing
//@MapperScan("jp.co.mcc.nttdata.batch.**.dao")
@SpringBootApplication
public class PentasenserApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(PentasenserApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
        try {
//            for (EvnPrepare item : EvnPrepare.values()) {
//                System.setProperty(item.name(), item.val);
//            }
//
//            for (Map.Entry<String, String> env : System.getenv().entrySet()) {
//                System.setProperty(env.getKey(), env.getValue());
////                log.info("{}={}",env.getKey(), env.getValue());
//            }
            args = new String[] { "cmBTktskS", "-debug" };
            System.out.println("start args: " + String.join(", ", args));

            String jobName = args[0];

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put(SystemConstant.ARGS_KEY, String.join(SystemConstant.ARGS_KEY_SPLIT, args));

            launchJob(context, jobName, parameterMap);
        } catch (Exception e) {
            if (e instanceof BatchBusinessException) {
                log.warn(e.getMessage());
            } else {
                log.error("", e);
            }
        } finally {

            //実施完了
            if (context != null) {
                SpringApplication.exit(context);

                // アプリケーション戻り値を設定・終了
                if (true) {
                    System.exit(CMAOConst.RETURN_OK);
                } else {
                    System.exit(CMAOConst.RETURN_NG);
                }
            }
        }
    }

    /**
     * Start job
     *
     * @param context context
     * @param jobName job name
     */
    private static void launchJob(ConfigurableApplicationContext context, String jobName,
                                  Map<String, String> parameterInput)
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        JobLaunchService service = context.getBean(JobLaunchService.class);
        JobResult result = service.launchJob(context, jobName, parameterInput);
        if (!ExitStatus.COMPLETED.getExitCode().equals(result.getJobExitStatus().getExitCode())) {
            throw new BatchBusinessException();
        }
    }

    private static void stopJob(ConfigurableApplicationContext context, String jobName) {
        try {
            JobOperator jobOperator = context.getBean(JobOperator.class);
            Set<Long> runningExecutions = jobOperator.getRunningExecutions(jobName);
            for (Long executionId : runningExecutions) {
                try {
                    jobOperator.stop(executionId);
                } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
                    log.warn("進行中プロセスではありません。");
                }
            }
        } catch (NoSuchJobException e) {
            log.warn("正しくバッチ名入力してください。");
        } catch (Exception e) {
            log.error("", e);
        }
    }
    
}
