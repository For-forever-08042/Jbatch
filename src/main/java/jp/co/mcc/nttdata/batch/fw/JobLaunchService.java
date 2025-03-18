/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw;

import jp.co.mcc.nttdata.batch.fw.com.constants.BatchConst;
import jp.co.mcc.nttdata.batch.fw.com.constants.ServiceIdManager;
import jp.co.mcc.nttdata.batch.fw.com.dto.JobResult;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;

/**
 * Job Launch service
 *
 * @author NCIT
 */
@Service
public class JobLaunchService {

    /**
     * launch job function
     *
     * @param context        context
     * @param jobName        jobName
     * @param parameterInput parameterInput
     * @return JobResult {@link JobResult}
     * @throws JobInstanceAlreadyCompleteException {@link JobInstanceAlreadyCompleteException}
     * @throws JobExecutionAlreadyRunningException {@link JobExecutionAlreadyRunningException}
     * @throws JobParametersInvalidException       {@link JobParametersInvalidException}
     * @throws JobRestartException                 {@link JobRestartException}
     */
    public JobResult launchJob(ConfigurableApplicationContext context, String jobName,
                               Map<String, String> parameterInput) throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

        jobParametersBuilder.addLong("date", System.currentTimeMillis());

        //パラメータ設定
        if (parameterInput != null) {
            for (Map.Entry<String, String> entry : parameterInput.entrySet()) {
                jobParametersBuilder.addString(entry.getKey(), entry.getValue());
            }
        }

        JobParameters jobParameters = jobParametersBuilder.toJobParameters();

        JobLauncher jobLauncher = (JobLauncher) context.getBean(BatchConst.JobLauncher);
        Job job = null;
        if (jobName.endsWith("B")) {
            job = (Job) context.getBean(ServiceIdManager.cmBTBase);
        } else {
            job = (Job) context.getBean(jobName);
        }

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        return JobResult.builder()
                .jobName(job.getName())
                .jobId(jobExecution.getJobId())
                .jobExitStatus(jobExecution.getExitStatus())
                .timestamp(Calendar.getInstance().getTimeInMillis())
                .build();
    }

}
