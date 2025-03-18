package jp.co.mcc.nttdata.batch.business.job.cmBTkaicS;

import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicJob;
import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.com.constants.ServiceIdManager;
import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CmBTkaicSJob extends NtBasicJob {

    //TODO change bean name manual, must be Override
    @Bean(ServiceIdManager.cmBTkaicS)
    @Override
    public Job job() {
        return super.job();
    }

    @Override
    public KshScriptTypes getServiceId() {
        // return the server type
        return KshScriptTypes.cmBTkaicS;
    }

}
