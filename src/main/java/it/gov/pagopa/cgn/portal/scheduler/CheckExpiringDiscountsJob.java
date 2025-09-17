package it.gov.pagopa.cgn.portal.scheduler;

import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.facade.ParamFacade;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.DiscountRepository;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class CheckExpiringDiscountsJob
        implements Job {

    private static final String JOB_LOG_NAME = "Expiring Discounts Notification Job ";

    private final DiscountService discountService;
    private final DiscountRepository discountRepository;
    private final ParamFacade paramFacade;

    @Autowired
    public CheckExpiringDiscountsJob(DiscountService discountService,
                                     DiscountRepository discountRepository,
                                     ParamFacade paramFacade) {
        this.discountService = discountService;
        this.discountRepository = discountRepository;
        this.paramFacade = paramFacade;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void execute(JobExecutionContext context) {

        log.info(JOB_LOG_NAME + "started");
        Instant start = Instant.now();
        List<DiscountEntity> discountList = discountRepository.findByStateAndExpirationWarningSentDateTimeIsNullAndEndDateLessThan(
                DiscountStateEnum.PUBLISHED,
                LocalDate.now().plusDays(paramFacade.getCheckExpiringDiscountsJobDays()));

        if (!CollectionUtils.isEmpty(discountList)) {
            log.info("Found " + discountList.size() + " discounts to notify");
            discountList.forEach(discountService::sendNotificationDiscountExpiring);
        }
        Instant end = Instant.now();
        log.info(JOB_LOG_NAME + "ended in " + Duration.between(start, end).getSeconds() + " seconds");
    }

}
