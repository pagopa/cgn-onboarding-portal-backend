package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.converter.help.HelpCategoryConverter;
import it.gov.pagopa.cgn.portal.email.EmailNotificationFacade;
import it.gov.pagopa.cgn.portal.email.HelpRequestParams;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.model.HelpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
public class HelpService {

    private EmailNotificationFacade emailNotificationFacade;
    private ProfileService profileService;
    private HelpCategoryConverter helpCategoryConverter;

    @Transactional
    public void sendHelpMessage(String agreementId, HelpRequest.CategoryEnum helpCategory, Optional<String> topic, String message) {

        ProfileEntity profile = profileService.getProfile(agreementId).orElseThrow(() -> new InvalidRequestException("Profile not found"));

        HelpRequestParams helpParams = HelpRequestParams.builder()
                .helpCategory(helpCategoryConverter.helpCategoryFromEnum(helpCategory))
                .topic(topic)
                .message(message)
                .replyToEmailAddress(profile.getReferent().getEmailAddress())
                .referentFirstName(profile.getReferent().getFirstName())
                .referentLastName(profile.getReferent().getLastName())
                .merchantLegalName(profile.getFullName())
                .build();

        try {
            emailNotificationFacade.notifyDepartmentNewHelpRequest(helpParams);
        } catch (Exception exc) {
            throw new RuntimeException("Help request error", exc);
        }
    }

    @Autowired
    public HelpService(ProfileService profileService, EmailNotificationFacade emailNotificationFacade, HelpCategoryConverter helpCategoryConverter) {
        this.profileService = profileService;
        this.emailNotificationFacade = emailNotificationFacade;
        this.helpCategoryConverter = helpCategoryConverter;
    }

}
