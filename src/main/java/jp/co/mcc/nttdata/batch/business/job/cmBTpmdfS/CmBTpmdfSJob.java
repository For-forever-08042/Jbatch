package jp.co.mcc.nttdata.batch.business.job.cmBTpmdfS;

import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicJob;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.com.constants.ServiceIdManager;

@Component
public class CmBTpmdfSJob extends NtBasicJob {

    @Bean(ServiceIdManager.cmBTpmdfS)
    @Override
    public Job job() {
        return super.job();
    }

    @Override
    public KshScriptTypes getServiceId() {
        return KshScriptTypes.cmBTpmdfS;
    }

}
