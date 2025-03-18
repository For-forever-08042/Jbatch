/*
 *
 *  * ====================================================================
 *  * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 *  * ====================================================================
 *
 */

package jp.co.mcc.nttdata.batch.fw.com.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * バッチ配置
 *
 * @author NCIT
 */
@Configuration
public class ExecutorConfiguration {

    /**
     * thread pool for job
     *
     * @return thread pool
     */
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(50);
        threadPoolTaskExecutor.setMaxPoolSize(200);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        threadPoolTaskExecutor.setThreadNamePrefix("Batch-");
        return threadPoolTaskExecutor;
    }

}
