package it.gov.pagopa.cgn.portal.facade;


import it.gov.pagopa.cgn.portal.converter.profile.CreateProfileConverter;
import it.gov.pagopa.cgn.portal.converter.profile.ProfileConverter;
import it.gov.pagopa.cgn.portal.converter.profile.UpdateProfileConverter;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.service.DiscountService;
import it.gov.pagopa.cgn.portal.service.ProfileService;
import it.gov.pagopa.cgnonboardingportal.model.CreateProfile;
import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.cgnonboardingportal.model.UpdateProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
public class ProfileFacade {

    private final ProfileService profileService;
    private final CreateProfileConverter createProfileConverter;
    private final UpdateProfileConverter updateProfileConverter;
    private final ProfileConverter profileConverter;
    private final DiscountService discountService;

    @Autowired
    public ProfileFacade(ProfileService profileService,
                         CreateProfileConverter createProfileConverter,
                         UpdateProfileConverter updateProfileConverter,
                         ProfileConverter profileConverter,
                         DiscountService discountService) {
        this.profileService = profileService;
        this.createProfileConverter = createProfileConverter;
        this.updateProfileConverter = updateProfileConverter;
        this.profileConverter = profileConverter;
        this.discountService = discountService;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Profile> createProfile(String agreementId, CreateProfile createRegistryDto) {
        ProfileEntity profileEntity = createProfileConverter.toEntity(createRegistryDto);
        profileEntity = profileService.createProfile(profileEntity, agreementId);
        return ResponseEntity.ok(profileConverter.toDto(profileEntity));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Profile> getProfile(String agreementId) {
        Optional<ProfileEntity> optionalProfile = profileService.getProfile(agreementId);
        Optional<Profile> profile = profileConverter.toDto(optionalProfile);
        return profile.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ResponseEntity<Profile> updateProfile(String agreementId, UpdateProfile updateProfile) {
        ProfileEntity profileEntity = updateProfileConverter.toEntity(updateProfile);
        ProfileEntity dbProfile = profileService.getProfileFromAgreementId(agreementId);
        if (!profileEntity.getSalesChannel().equals(dbProfile.getSalesChannel()) ||
            (profileEntity.getDiscountCodeType()!=null &&
             !profileEntity.getDiscountCodeType().equals(dbProfile.getDiscountCodeType()))) {
            // if sales channel or discount code type are changed we should unpublish all the discount of this profile
            discountService.getDiscounts(agreementId)
                           .stream()
                           .filter(d -> DiscountStateEnum.PUBLISHED.equals(d.getState()))
                           .forEach(d -> discountService.suspendDiscount(agreementId,
                                                                         d.getId(),
                                                                         "Hai cambiato la modalità di riconoscimento delle tue opportunità. Per riattivare l'opportunità, aggiorna i dati richiesti dalla nuova opportunità."));
        }
        profileEntity = profileService.updateProfile(agreementId, profileEntity);

        // refresh views
        discountService.refreshMaterializedViews(profileEntity);

        return ResponseEntity.ok(profileConverter.toDto(profileEntity));
    }
}
