package jp.co.mcc.nttdata.batch.business.job.cmBTpcreS;

import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicJob;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.com.constants.ServiceIdManager;
import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CmBTpcreSJob extends NtBasicJob {

    @Bean(ServiceIdManager.cmBTpcreS)
    @Override
    public Job job() {
        return super.job();
    }

    @Override
    public KshScriptTypes getServiceId() {
        return KshScriptTypes.cmBTpcreS;
    }
}
