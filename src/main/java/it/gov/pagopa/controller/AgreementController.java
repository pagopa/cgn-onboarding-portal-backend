package it.gov.pagopa.controller;

import it.gov.pagopa.cgnonboardingportal.api.AgreementsApi;
import it.gov.pagopa.cgnonboardingportal.model.Agreement;
import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.converter.AgreementConverter;
import it.gov.pagopa.converter.profile.CreateProfileConverter;
import it.gov.pagopa.converter.profile.ProfileConverter;
import it.gov.pagopa.converter.profile.UpdateProfileConverter;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.service.AgreementService;
import it.gov.pagopa.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AgreementController implements AgreementsApi {

    private final AgreementService agreementService;

    private final ProfileService profileService;

    private final AgreementConverter agreementConverter;
    private final CreateProfileConverter createProfileConverter;
    private final UpdateProfileConverter updateProfileConverter;
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

    @Override
    public ResponseEntity<Profile> getProfile(String agreementId) {
        Optional<Profile> optionalProfile = profileService.getProfile(agreementId);
        return optionalProfile.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Profile> updateProfile(String agreementId, UpdateProfile updateProfile) {
        ProfileEntity profileEntity = updateProfileConverter.toEntity(updateProfile);
        return ResponseEntity.ok(profileService.updateProfile(agreementId, profileEntity));
    }

    @Autowired
    public AgreementController(AgreementService agreementService,
                               ProfileService profileService,
                               AgreementConverter agreementConverter,
                               CreateProfileConverter createProfileConverter,
                               UpdateProfileConverter updateProfileConverter,
                               ProfileConverter profileConverter) {
        this.agreementService = agreementService;
        this.profileService = profileService;
        this.agreementConverter = agreementConverter;
        this.createProfileConverter = createProfileConverter;
        this.updateProfileConverter = updateProfileConverter;
        this.profileConverter = profileConverter;
    }
}

