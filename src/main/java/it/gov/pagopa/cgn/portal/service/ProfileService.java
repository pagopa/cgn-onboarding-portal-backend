package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.SalesChannelEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import it.gov.pagopa.cgn.portal.repository.ProfileRepository;
import it.gov.pagopa.cgn.portal.util.ValidationUtils;
import it.gov.pagopa.cgnonboardingportal.model.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Service
public class ProfileService {

    private final ValidatorFactory factory;
    private final AgreementServiceLight agreementServiceLight;
    private final ProfileRepository profileRepository;
    private final DocumentService documentService;
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
    private final BiConsumer<ProfileEntity, List<SecondaryReferentEntity>> updateSecondaryReferents = (profileEntity, secondaryReferentList) -> {
        if (!CollectionUtils.isEmpty(profileEntity.getSecondaryReferentList())) {
            profileEntity.removeAllSecondaryReferents();
        }
        profileEntity.addSecondaryReferentsList(secondaryReferentList);
    };
    private final BiConsumer<ProfileEntity, ProfileEntity> updateConsumer = (toUpdateEntity, dbEntity) -> {
        dbEntity.setName(toUpdateEntity.getName());
        dbEntity.setNameEn(toUpdateEntity.getNameEn());
        dbEntity.setNameDe(toUpdateEntity.getNameDe());
        dbEntity.setDescription(toUpdateEntity.getDescription());
        dbEntity.setDescriptionEn(toUpdateEntity.getDescriptionEn());
        dbEntity.setDescriptionDe(toUpdateEntity.getDescriptionDe());
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
        updateSecondaryReferents.accept(dbEntity, toUpdateEntity.getSecondaryReferentList());
        dbEntity.setWebsiteUrl(toUpdateEntity.getWebsiteUrl());
        // fullname will never arrive from converted api model
        // we will update it only internally so we have to check that it's not null
        if (toUpdateEntity.getFullName()!=null) {
            dbEntity.setFullName(toUpdateEntity.getFullName());
        }
    };

    @Autowired
    public ProfileService(ValidatorFactory factory,
                          ProfileRepository profileRepository,
                          AgreementServiceLight agreementServiceLight,
                          DocumentService documentService) {
        this.factory = factory;
        this.profileRepository = profileRepository;
        this.agreementServiceLight = agreementServiceLight;
        this.documentService = documentService;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity createProfile(ProfileEntity profileEntity, String agreementId) {
        AgreementEntity agreement = agreementServiceLight.findAgreementById(agreementId);
        if (profileRepository.existsProfileEntityByAgreementId(agreementId)) {
            throw new InvalidRequestException(ErrorCodeEnum.PROFILE_ALREADY_EXISTS_FOR_AGREEMENT_PROVIDED.getValue());
        }
        profileEntity.setAgreement(agreement);
        validateProfile(profileEntity);
        return profileRepository.save(profileEntity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Optional<ProfileEntity> getProfile(String agreementId) {
        return profileRepository.findByAgreementId(agreementId);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity updateProfile(String agreementId, ProfileEntity newUpdateProfile) {
        ProfileEntity profileEntity = getProfileFromAgreementId(agreementId);
        updateConsumer.accept(newUpdateProfile, profileEntity);

        // fix for misalignments with addresses
        if (!profileEntity.getSalesChannel().equals(SalesChannelEnum.ONLINE) &&
            profileEntity.getAddressList().isEmpty() && Boolean.FALSE.equals(profileEntity.getAllNationalAddresses())) {
            profileEntity.setAllNationalAddresses(true);
        }

        validateProfile(profileEntity);
        profileEntity = profileRepository.save(profileEntity);

        // check agreement
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

        return profileEntity;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public ProfileEntity getProfileFromAgreementId(String agreementId) {
        return profileRepository.findByAgreementId(agreementId)
                                .orElseThrow(() -> new InvalidRequestException(ErrorCodeEnum.AGREEMENT_NOT_FOUND.getValue()));
    }

    private void validateProfile(ProfileEntity profileEntity) {
        ValidationUtils.performConstraintValidation(factory.getValidator(), profileEntity);
    }

}