package it.gov.pagopa.service;

import it.gov.pagopa.cgnonboardingportal.model.Profile;
import it.gov.pagopa.converter.profile.ProfileConverter;
import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.AddressEntity;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.model.ReferentEntity;
import it.gov.pagopa.repository.AddressRepository;
import it.gov.pagopa.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Service
public class ProfileService {

    private final AgreementService agreementService;
    private final ProfileRepository profileRepository;
    private final ProfileConverter profileConverter;
    private  AddressRepository addressRepository;


    public ProfileEntity createRegistry(ProfileEntity profileEntity, String agreementId) {
        AgreementEntity agreement = agreementService.findById(agreementId);
        profileEntity.setAgreement(agreement);
        if (profileRepository.existsProfileEntityByAgreementId(agreementId)) {
            throw new InvalidRequestException("A registry already exist for the agreement: " + agreementId);
        }
        agreementService.setRegistryDoneModifiedDate(agreement);
        return profileRepository.save(profileEntity);
    }

    public Optional<Profile> getProfile(String agreementId) {
        return profileConverter.toDto(getOptProfileFromAgreementId(agreementId));
    }

    public Profile updateProfile(String agreementId, ProfileEntity newUpdateProfile) {
        ProfileEntity profileEntity = getProfileFromAgreementId(agreementId);
        updateConsumer.accept(newUpdateProfile, profileEntity);
        return profileConverter.toDto(profileRepository.save(profileEntity));
    }


    @Autowired
    public ProfileService(ProfileRepository profileRepository, AgreementService agreementService,
                          ProfileConverter profileConverter, AddressRepository addressRepository) {
        this.profileRepository = profileRepository;
        this.agreementService = agreementService;
        this.profileConverter = profileConverter;
        this.addressRepository = addressRepository;
    }

    private ProfileEntity getProfileFromAgreementId(String agreementId) {
        return getOptProfileFromAgreementId(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Profile not found for agreement " + agreementId));
    }

    private Optional<ProfileEntity> getOptProfileFromAgreementId(String agreementId) {
        //todo check agreementId with token
        return profileRepository.findByAgreementId(agreementId);
    }
    private final BiConsumer<ReferentEntity, ReferentEntity> updateReferent = (toUpdateEntity, dbEntity) -> {
        dbEntity.setFirstName(toUpdateEntity.getFirstName());
        dbEntity.setLastName(toUpdateEntity.getLastName());
        dbEntity.setEmailAddress(toUpdateEntity.getEmailAddress());
        dbEntity.setTelephoneNumber(toUpdateEntity.getTelephoneNumber());
    };

    private final BiConsumer<ProfileEntity, List<AddressEntity>> updateAddress = (profileEntity, addressesList) -> {
        if (!CollectionUtils.isEmpty(profileEntity.getAddressList())) {
            profileEntity.getAddressList().forEach(a -> {
            //    a.setProfile(null);
                addressRepository.delete(a);
            });
        }
        addressesList.forEach(addressEntity -> addressEntity.setProfile(profileEntity));
        profileEntity.setAddressList(addressesList);
    };

    private final BiConsumer<ProfileEntity, ProfileEntity> updateConsumer = (toUpdateEntity, dbEntity) -> {
      dbEntity.setName(toUpdateEntity.getName());
      dbEntity.setDescription(toUpdateEntity.getDescription());
      dbEntity.setPecAddress(toUpdateEntity.getPecAddress());
      dbEntity.setSalesChannel(toUpdateEntity.getSalesChannel());
      updateReferent.accept(toUpdateEntity.getReferent(), dbEntity.getReferent());
      updateAddress.accept(dbEntity, toUpdateEntity.getAddressList());
      dbEntity.setAddressList(toUpdateEntity.getAddressList());
      dbEntity.setWebsiteUrl(toUpdateEntity.getWebsiteUrl());
    };

}