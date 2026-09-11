package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgn.portal.service.AgreementInactivationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@Slf4j
public class InactivateAgreementsJob
        implements Job {

    private static final String JOB_LOG_NAME = "Inactivate Agreements Job";

    private final AgreementInactivationService agreementInactivationService;
    private final ParamFacade paramFacade;

    public InactivateAgreementsJob(AgreementInactivationService agreementInactivationService,
                                   ParamFacade paramFacade) {
        this.agreementInactivationService = agreementInactivationService;
        this.paramFacade = paramFacade;
    }

    @Override
    public void execute(JobExecutionContext context) {
        log.info(JOB_LOG_NAME + " started");
        Instant start = Instant.now();
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        agreementInactivationService.inactivateStaleAgreements(
            currentDate,
            paramFacade.getInactivateAgreementsJobExpiredStaleMonths());

        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + " ended in {} seconds", Duration.between(start, end).getSeconds());
    }
}
