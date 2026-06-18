package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.service.AgreementInactivationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class InactivateAgreementsJob
        implements Job {

    private static final String JOB_LOG_NAME = "Inactivate Agreements Job";

    private final AgreementInactivationService agreementInactivationService;

    public InactivateAgreementsJob(AgreementInactivationService agreementInactivationService) {
        this.agreementInactivationService = agreementInactivationService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        log.info(JOB_LOG_NAME + " started");
        Instant start = Instant.now();

        agreementInactivationService.inactivateStaleAgreements();

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + " ended in {} seconds", Duration.between(start, end).getSeconds());
    }
}
