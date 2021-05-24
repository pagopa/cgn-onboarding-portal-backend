package it.gov.pagopa.cgn.portal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigProperties {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${exception_generic_message}")
    private String exceptionGenericMessage;

    @Value("${spring_cors_origin}")
    private String CORSOrigin;

    //storage configs
    @Value("${cgn.pe.storage.azure.default-endpoints-protocol}")
    private String defaultEndpointsProtocol;

    @Value("${cgn.pe.storage.azure.account-name}")
    private String accountName;

    @Value("${cgn.pe.storage.azure.account-key}")
    private String accountKey;

    @Value("${cgn.pe.storage.azure.blob-endpoint}")
    private String blobEndpoint;

    @Value("${cgn.pe.storage.azure.documents-container-name}")
    private String documentsContainerName;

    @Value("${cgn.pe.storage.azure.images-container-name}")
    private String imagesContainerName;

    @Value("${cgn.storage.document.sas.expiry.time.hours}")
    private Integer sasExpiryTimeHours;

    public String getAzureConnectionString() {
        return "DefaultEndpointsProtocol=" + defaultEndpointsProtocol +
                ";AccountName=" + accountName +
                ";AccountKey=" + accountKey +
                ";BlobEndpoint=" + blobEndpoint + ";";

    }

    @Value("${cgn.image.minWidth}")
    private Integer minWidth;

    @Value("${cgn.image.minHeight}")
    private Integer minHeight;

    @Value("${cgn.email.notification-sender}")
    private String cgnNotificationSender;

    @Value("${cgn.email.department-email}")
    private String cgnDepartmentEmail;

    @Value("${cgn.email.portal-base-url}")
    private String cgnPortalBaseUrl;

    @Value("classpath:images/cgn-logo.png")
    private Resource cgnLogo;

    @Value("${cgn.apim.resourceGroup}")
    private String apimResouceGroup;

    @Value("${cgn.apim.resource}")
    private String apimResouce;

    @Value("${cgn.apim.productId}")
    private String apimProductId;

    @Value("${cgn.apim.subscriptionKeyPrefix}")
    private String apimSubscriptionKeyPrefix;

    @Value("${cgn.recaptcha.secret-key}")
    private String recaptchaSecretKey;

    @Value("${cgn.recaptcha.google-host}")
    private String recaptchaGoogleHost;

    @Value("${check.expiring.discounts.job.cron}")
    private String expiringDiscountsJobCronExpression;

    @Value("${check.expiring.discounts.job.days}")
    private int expiringDiscountsJobDays;


    @Value("${cgn.geolocation.secret-token}")
    private String geolocationToken;

    public boolean isActiveProfileDev() {
        return "dev".equals(getActiveProfile());
    }
}
