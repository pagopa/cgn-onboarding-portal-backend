package it.gov.pagopa.service;

import it.gov.pagopa.exception.InvalidRequestException;
import it.gov.pagopa.model.ProfileEntity;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final AgreementService agreementService;
    private final ProfileRepository profileRepository;


    public ProfileEntity createRegistry(ProfileEntity profileEntity, String agreementId) {
        AgreementEntity agreement = agreementService.findById(agreementId);
        profileEntity.setAgreement(agreement);
        if (profileRepository.existsProfileEntityByAgreementId(agreementId)) {
            throw new InvalidRequestException("A registry already exist for the agreement: " + agreementId);
        }
        agreementService.setRegistryDoneModifiedDate(agreement);
        return profileRepository.save(profileEntity);
    }


    @Autowired
    public ProfileService(ProfileRepository profileRepository, AgreementService agreementService) {
        this.profileRepository = profileRepository;
        this.agreementService = agreementService;
    }

}