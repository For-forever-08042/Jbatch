package jp.co.mcc.nttdata.batch.fw.com.conf;

import jp.co.mcc.nttdata.batch.fw.com.constants.BatchConst;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Bean(BatchConst.transactionManager)
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean(BatchConst.JobRepository)
    public JobRepository jobRepository(@Qualifier(BatchConst.transactionManager) PlatformTransactionManager transactionManager) throws Exception {
        MapJobRepositoryFactoryBean jobRepositoryFactoryBean = new MapJobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean(BatchConst.JobLauncher)
    public JobLauncher jobLauncher(@Qualifier(BatchConst.JobRepository) JobRepository customJobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(customJobRepository);
        return jobLauncher;
    }

}
