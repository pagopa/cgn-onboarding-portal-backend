package it.gov.pagopa.cgn.portal.facade;


import it.gov.pagopa.cgn.portal.config.ConfigProperties;
import it.gov.pagopa.cgn.portal.enums.ParamGroupEnum;
import it.gov.pagopa.cgn.portal.service.ParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ParamFacade {

    private final ConfigProperties configProperties;
    private final ParamService paramService;

    private  boolean isDev = false;

    @Autowired
    public ParamFacade(ParamService paramService, ConfigProperties configProperties) {
        this.configProperties = configProperties;
        this.paramService = paramService;
        this.isDev = configProperties.isActiveProfileDev();
        log.info("ParamFacade.isDev = {}", isDev);
    }

    public String[] getEycaJobMailTo(){
        return (isDev ? configProperties.getEycaJobMailTo() : paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.job.mailto")).split(";");

    }
    public String[] getEycaAdminMailTo() {
        return (isDev ? configProperties.getEycaAdminMailTo() : paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "eyca.admin.mailto")).split(";");
    }

    public String getCheckExpiringDiscountsJobCronExpression() {
        return isDev ? configProperties.getCheckExpiringDiscountsJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.CHECK_EXPIRING_DISC_JOB, "check.expiring.discounts.job.cron");
    }

    public int getCheckExpiringDiscountsJobDays() {
        return isDev ? configProperties.getCheckExpiringDiscountsJobDays()
                     : Integer.parseInt(paramService.getParam(ParamGroupEnum.CHECK_EXPIRING_DISC_JOB, "check.expiring.discounts.job.days"));
    }

    public String getCheckAvailableDiscountBucketCodesJobCronExpression() {
        return isDev ? configProperties.getCheckAvailableDiscountBucketCodesJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.CHECK_AVAILABLE_DISC_JOB, "check.available.discounts.bucket.codes.job.cron");
    }

    public String getSendLowDiscountBucketCodesNotificationJobCronExpression() {
        return isDev ? configProperties.getSendLowDiscountBucketCodesNotificationJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.SEND_LOW_DISC_BUCKET_CODES_NOTIF_JOB, "send.low.bucket.codes.notification.job.cron");
    }

    public String getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression() {
        return isDev ? configProperties.getSuspendDiscountsWithoutAvailableBucketCodesJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.SUSPEND_DISCOUNTS_JOB, "suspend.discounts.without.available.bucket.codes.job.cron");
    }

    public String getSendWeeklyDiscountBucketCodesSummaryJobCronExpression() {
        return isDev ? configProperties.getSendWeeklyDiscountBucketCodesSummaryJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.SEND_WEEKLY_SUMMARY_JOB, "send.weekly.discount.bucket.codes.summary.job.cron");
    }

    public String getSendDiscountsToEycaJobCronExpression() {
        return isDev ? configProperties.getSendDiscountsToEycaJobCronExpression()
                     : paramService.getParam(ParamGroupEnum.SEND_DISCOUNTS_EYCA_JOB, "send.discounts.to.eyca.job.cron");
    }

    public String getSuspendReferentsMailSending() {

        return isDev ? configProperties.getSuspendReferentsMailSending()
                : paramService.getParam(ParamGroupEnum.CGN_JOB_FLAGS, "suspend.referents.mail.sending");
    }
}
