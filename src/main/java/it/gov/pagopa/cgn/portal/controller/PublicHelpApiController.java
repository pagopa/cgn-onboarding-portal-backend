package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.converter.help.HelpCategoryConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.HelpRequestParams;
import it.gov.pagopa.cgn.portal.exception.InternalErrorException;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.recaptcha.GoogleRecaptchaApi;
import it.gov.pagopa.cgnonboardingportal.publicapi.api.HelpApi;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;


@RestController
public class PublicHelpApiController
        implements HelpApi {

    private HelpCategoryConverter helpCategoryConverter;
    private EmailNotificationFacade emailNotificationFacade;
    private GoogleRecaptchaApi googleRecaptchaApi;


    @Autowired
    public PublicHelpApiController(EmailNotificationFacade emailNotificationFacade,
                                   HelpCategoryConverter helpCategoryConverter,
                                   GoogleRecaptchaApi googleRecaptchaApi) {
        this.emailNotificationFacade = emailNotificationFacade;
        this.helpCategoryConverter = helpCategoryConverter;
        this.googleRecaptchaApi = googleRecaptchaApi;
    }


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return HelpApi.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> sendHelpRequest(HelpRequest helpRequest) {

        if (!googleRecaptchaApi.isTokenValid(helpRequest.getRecaptchaToken())) {
            throw new InvalidRequestException("Recaptcha Challenge Failed");
        }

        HelpRequestParams helpParams = HelpRequestParams.builder()
                                                        .helpCategory(helpCategoryConverter.helpCategoryFromEnum(
                                                                helpRequest.getCategory()))
                                                        .topic(Optional.ofNullable(helpRequest.getTopic()))
                                                        .message(helpRequest.getMessage())
                                                        .replyToEmailAddress(helpRequest.getEmailAddress())
                                                        .referentFirstName(helpRequest.getReferentFirstName())
                                                        .referentLastName(helpRequest.getReferentLastName())
                                                        .merchantLegalName(helpRequest.getLegalName())
                                                        .build();

        try {
            emailNotificationFacade.notifyDepartmentNewHelpRequest(helpParams);
            return ResponseEntity.noContent().build();

        } catch (Exception exc) {
            throw new InternalErrorException(exc.getMessage());
        }
    }
}
