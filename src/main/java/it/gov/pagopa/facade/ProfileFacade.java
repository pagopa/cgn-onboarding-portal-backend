package it.gov.pagopa.facade;


import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import it.gov.pagopa.converter.profile.CreateProfileConverter;
import it.gov.pagopa.converter.profile.ProfileConverter;
import it.gov.pagopa.converter.profile.UpdateProfileConverter;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfileFacade {

    private final ProfileService profileService;
    private final CreateProfileConverter createProfileConverter;
    private final UpdateProfileConverter updateProfileConverter;
    private final ProfileConverter profileConverter;


    public ResponseEntity<Profile> createProfile(String agreementId, CreateProfile createRegistryDto) {
        ProfileEntity registry = createProfileConverter.toEntity(createRegistryDto);
        registry = profileService.createRegistry(registry, agreementId);
        return ResponseEntity.ok(profileConverter.toDto(registry));
    }

    public ResponseEntity<Profile> getProfile(String agreementId) {
        Optional<Profile> optionalProfile = profileService.getProfile(agreementId);
        return optionalProfile.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Profile> updateProfile(String agreementId, UpdateProfile updateProfile) {
        ProfileEntity profileEntity = updateProfileConverter.toEntity(updateProfile);
        return ResponseEntity.ok(profileService.updateProfile(agreementId, profileEntity));
    }

    @Autowired
    public ProfileFacade(ProfileService profileService, CreateProfileConverter createProfileConverter,
                         UpdateProfileConverter updateProfileConverter, ProfileConverter profileConverter) {
        this.profileService = profileService;
        this.createProfileConverter = createProfileConverter;
        this.updateProfileConverter = updateProfileConverter;
        this.profileConverter = profileConverter;
    }
}
