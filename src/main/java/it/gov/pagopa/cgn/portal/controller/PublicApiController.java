package it.gov.pagopa.cgn.portal.controller;

import it.gov.pagopa.cgn.portal.converter.help.HelpCategoryConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.HelpRequestParams;
import it.gov.pagopa.cgnonboardingportal.publicapi.api.HelpApi;
import it.gov.pagopa.cgnonboardingportal.publicapi.model.HelpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
public class PublicApiController implements HelpApi {

    private HelpCategoryConverter helpCategoryConverter;
    private EmailNotificationFacade emailNotificationFacade;


    @Autowired
    public PublicApiController(EmailNotificationFacade emailNotificationFacade, HelpCategoryConverter helpCategoryConverter) {
        this.emailNotificationFacade = emailNotificationFacade;
        this.helpCategoryConverter = helpCategoryConverter;
    }

    @Override
    public ResponseEntity<Void> sendHelpRequest(HelpRequest helpRequest) {

        HelpRequestParams helpParams = HelpRequestParams.builder()
                .helpCategory(helpCategoryConverter.helpCategoryFromEnum(helpRequest.getCategory()))
                .topic(Optional.ofNullable(helpRequest.getTopic()))
                .message(helpRequest.getMessage())
                .replyToEmailAddress(helpRequest.getEmailAddress())
                .referentFirstName(helpRequest.getReferentFirstName())
                .referentLastName(helpRequest.getReferentLastName())
                .merchantLegalName(helpRequest.getLegalName())
                .build();

        try {
            emailNotificationFacade.notifyDepartmentNewHelpRequest(helpParams);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        return ResponseEntity.noContent().build();
    }

}
