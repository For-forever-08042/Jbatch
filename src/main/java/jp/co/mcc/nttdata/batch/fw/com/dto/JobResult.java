/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.com.dto;

import lombok.*;
import org.springframework.batch.core.ExitStatus;
import org.springframework.stereotype.Component;

/**
 * JobResult
 *
 * @author NCIT
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(doNotUseGetters = true)
@AllArgsConstructor
@Builder
@Component
public class JobResult {
    private long jobId;
    private String jobName;
    private ExitStatus jobExitStatus;
    private long timestamp;
}
