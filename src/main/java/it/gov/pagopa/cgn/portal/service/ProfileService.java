package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AddressEntity;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgn.portal.model.ReferentEntity;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Service
public class ProfileService {

    private final AgreementServiceLight agreementServiceLight;
    private final ProfileRepository profileRepository;


    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity createProfile(ProfileEntity profileEntity, String agreementId) {
        AgreementEntity agreement = agreementServiceLight.findById(agreementId);
        profileEntity.setAgreement(agreement);
        if (profileRepository.existsProfileEntityByAgreementId(agreementId)) {
            throw new InvalidRequestException("A registry already exist for the agreement: " + agreementId);
        }
        return profileRepository.save(profileEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Optional<ProfileEntity> getProfile(String agreementId) {
        return getOptProfileFromAgreementId(agreementId);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity updateProfile(String agreementId, ProfileEntity newUpdateProfile) {
        ProfileEntity profileEntity = getProfileFromAgreementId(agreementId);
        updateConsumer.accept(newUpdateProfile, profileEntity);
        return profileRepository.save(profileEntity);
    }


    @Autowired
    public ProfileService(ProfileRepository profileRepository, AgreementServiceLight agreementServiceLight) {
        this.profileRepository = profileRepository;
        this.agreementServiceLight = agreementServiceLight;
    }

    private ProfileEntity getProfileFromAgreementId(String agreementId) {
        return getOptProfileFromAgreementId(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Updating profile was not found for agreement " + agreementId));
    }

    private Optional<ProfileEntity> getOptProfileFromAgreementId(String agreementId) {
        return profileRepository.findByAgreementId(agreementId);
    }
    private final BiConsumer<ReferentEntity, ReferentEntity> updateReferent = (toUpdateEntity, dbEntity) -> {
        dbEntity.setFirstName(toUpdateEntity.getFirstName());
        dbEntity.setLastName(toUpdateEntity.getLastName());
        dbEntity.setEmailAddress(toUpdateEntity.getEmailAddress());
        dbEntity.setTelephoneNumber(toUpdateEntity.getTelephoneNumber());
        dbEntity.setRole(toUpdateEntity.getRole());
    };

    private final BiConsumer<ProfileEntity, List<AddressEntity>> updateAddress = (profileEntity, addressesList) -> {
        if (!CollectionUtils.isEmpty(profileEntity.getAddressList())) {
            profileEntity.removeAllAddress();
        }
        profileEntity.addAddressList(addressesList);
    };

    private final BiConsumer<ProfileEntity, ProfileEntity> updateConsumer = (toUpdateEntity, dbEntity) -> {
      dbEntity.setName(toUpdateEntity.getName());
      dbEntity.setDescription(toUpdateEntity.getDescription());
      dbEntity.setPecAddress(toUpdateEntity.getPecAddress());
      dbEntity.setSalesChannel(toUpdateEntity.getSalesChannel());
      dbEntity.setLegalOffice(toUpdateEntity.getLegalOffice());
      dbEntity.setLegalRepresentativeTaxCode(toUpdateEntity.getLegalRepresentativeTaxCode());
      dbEntity.setLegalRepresentativeFullName(toUpdateEntity.getLegalRepresentativeFullName());
      dbEntity.setDiscountCodeType(toUpdateEntity.getDiscountCodeType());
      dbEntity.setTelephoneNumber(toUpdateEntity.getTelephoneNumber());
      updateReferent.accept(toUpdateEntity.getReferent(), dbEntity.getReferent());
      updateAddress.accept(dbEntity, toUpdateEntity.getAddressList());
      dbEntity.setWebsiteUrl(toUpdateEntity.getWebsiteUrl());
    };

}