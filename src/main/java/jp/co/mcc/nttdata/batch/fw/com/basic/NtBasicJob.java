package jp.co.mcc.nttdata.batch.fw.com.basic;

import jp.co.mcc.nttdata.batch.business.service.db.JDBCUtil;
import jp.co.mcc.nttdata.batch.fw.EvnPrepare;
import jp.co.mcc.nttdata.batch.fw.com.constants.BatchConst;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.com.constants.SystemConstant;
import jp.co.mcc.nttdata.batch.fw.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Slf4j
public abstract class NtBasicJob {

    @Autowired
    @Qualifier(BatchConst.JobRepository)
    JobRepository jobRepository;

    @Autowired
    @Qualifier(BatchConst.transactionManager)
    PlatformTransactionManager transactionManager;

    @Autowired
    JDBCUtil oracleDBUtil;

    @Value("${ntt.common.localEvn:false}")
    public boolean enableLocalEvn;

    public abstract KshScriptTypes getServiceId();

    public NtBasicTask getTaskLet() {
        NtBasicTask task = (NtBasicTask) SpringUtils.getBean(getServiceId().clazz);
        task.taskType = getServiceId();
        return task;
    }

    /**
     * please Override  when custome listener
     *
     * @return
     */
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    /**
     * please Override  when custome listener
     *
     * @return
     */
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * @return Job catalog job
     */
    public Job job() {
        return new JobBuilder(getServiceId().name())
                .incrementer(new RunIdIncrementer())
                .repository(getJobRepository())
                .flow(new StepBuilder(getServiceId().name())
                        .repository(getJobRepository())
                        .transactionManager(getTransactionManager())
                        .tasklet(getTaskLet())
                        .build())
                .end()
                .listener(getDefaultJobListener())
                .build();
    }


    /**
     * please Override  when custome listener
     *
     * @return
     */
    public JobExecutionListener getDefaultJobListener() {
        return new JobExecutionListener() {

            private long startTime;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                startTime = System.currentTimeMillis();
                log.info("JobId: " + jobExecution.getJobId() + " execute with: {}", jobExecution.getJobParameters());
                if (enableLocalEvn) {
                    for (EvnPrepare item : EvnPrepare.values()) {
                        System.setProperty(item.name(), item.val);
                    }
                }
                for (Map.Entry<String, String> env : System.getenv().entrySet()) {
                    System.setProperty(env.getKey(), env.getValue());
                }
                oracleDBUtil.doBefore(decode(jobExecution));

            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("JobId: " + jobExecution.getJobId() + " execute status : {}", jobExecution.getStatus());
                log.debug("Job Cost Time : {}ms", (System.currentTimeMillis() - startTime));

                oracleDBUtil.doAfter(jobExecution.getStatus() == BatchStatus.COMPLETED);
            }
        };
    }

    public static String[] decode(JobExecution jobExecution) {
        Map<String, JobParameter> jobParameterMap = jobExecution.getJobParameters().getParameters();
        String arg = (String) jobParameterMap.get(SystemConstant.ARGS_KEY).getValue();
        return arg.split(SystemConstant.ARGS_KEY_SPLIT);
    }

}
