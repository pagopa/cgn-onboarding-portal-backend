package it.gov.pagopa.controller;

import it.gov.pagopa.cgnonboardingportal.api.AgreementsApi;
import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.converter.profile.CreateProfileConverter;
import it.gov.pagopa.converter.AgreementConverter;
import it.gov.pagopa.converter.profile.ProfileConverter;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.service.ProfileService;
import it.gov.pagopa.service.AgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgreementController implements AgreementsApi {

    private final AgreementService agreementService;

    private final ProfileService profileService;

    private final AgreementConverter agreementConverter;
    private final CreateProfileConverter createProfileConverter;
    private final ProfileConverter profileConverter;

    @Override
    public ResponseEntity<Agreement> createAgreement() {
        return ResponseEntity.ok(
                agreementConverter.toDto(agreementService.createAgreementIfNotExists()));
    }

    @Override
    public ResponseEntity<Profile> createProfile(String subscriptionId, CreateProfile createRegistryDto) {
        ProfileEntity registry = createProfileConverter.toEntity(createRegistryDto);
        registry = profileService.createRegistry(registry, subscriptionId);
        return ResponseEntity.ok(profileConverter.toDto(registry));
    }

    @Autowired
    public AgreementController(AgreementService agreementService,
                               ProfileService profileService,
                               AgreementConverter agreementConverter,
                               CreateProfileConverter createProfileConverter,
                               ProfileConverter profileConverter) {
        this.agreementService = agreementService;
        this.profileService = profileService;
        this.agreementConverter = agreementConverter;
        this.createProfileConverter = createProfileConverter;
        this.profileConverter = profileConverter;
    }
}

