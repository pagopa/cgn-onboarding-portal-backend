package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
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
    private final DocumentService documentService;

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity createProfile(ProfileEntity profileEntity, String agreementId) {
        AgreementEntity agreement = agreementServiceLight.findById(agreementId);
        if (profileRepository.existsProfileEntityByAgreementId(agreementId)) {
            throw new InvalidRequestException("A registry already exist for the agreement: " + agreementId);
        }
        profileEntity.setAgreement(agreement);
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
        AgreementEntity agreementEntity = profileEntity.getAgreement();
        if (AgreementStateEnum.APPROVED.equals(agreementEntity.getState())) {
            agreementServiceLight.setInformationLastUpdateDate(profileEntity.getAgreement());
        }

        if (AgreementStateEnum.DRAFT.equals(profileEntity.getAgreement().getState())) {
            documentService.resetMerchantDocuments(agreementId);
        }

        if (AgreementStateEnum.REJECTED.equals(agreementEntity.getState())) {
            agreementServiceLight.setDraftAgreementFromRejected(agreementEntity);
            documentService.resetAllDocuments(agreementId);
        }
        return profileRepository.save(profileEntity);
    }


    @Autowired
    public ProfileService(ProfileRepository profileRepository, AgreementServiceLight agreementServiceLight, DocumentService documentService) {
        this.profileRepository = profileRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.documentService = documentService;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity getProfileFromAgreementId(String agreementId) {
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
        dbEntity.setAllNationalAddresses(toUpdateEntity.getAllNationalAddresses());
        updateReferent.accept(toUpdateEntity.getReferent(), dbEntity.getReferent());
        updateAddress.accept(dbEntity, toUpdateEntity.getAddressList());
        dbEntity.setWebsiteUrl(toUpdateEntity.getWebsiteUrl());
        // fullname will never arrive from converted api model
        // we will update it only internally so we have to check that it's not null
        if (toUpdateEntity.getFullName() != null) {
            dbEntity.setFullName(toUpdateEntity.getFullName());
        }
    };

}