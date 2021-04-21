package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AgreementService {

    private final AgreementRepository agreementRepository;

    private final AgreementUserService userService;

    public AgreementEntity findById(String agreementId) {
        return agreementRepository.findById(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Agreement not found"));
    }

    public AgreementEntity createAgreementIfNotExists() {
        AgreementEntity agreementEntity;
        AgreementUserEntity userAgreement;
        Optional<AgreementUserEntity> userAgreementOpt = userService.findCurrentAgreementUser();
        if (userAgreementOpt.isPresent()) {
            userAgreement = userAgreementOpt.get();
            // current user has already an agreement. Find it
            agreementEntity = agreementRepository.findById(userAgreement.getAgreementId())
                    .orElseThrow(() -> new RuntimeException("User " + userAgreement.getUserId() + " doesn't have an agreement"));
        } else {
            userAgreement = userService.create();
            agreementEntity = createAgreement(userAgreement.getAgreementId());
        }
        return agreementEntity;
    }

    public void setRegistryDoneModifiedDate(AgreementEntity agreementEntity) {
        agreementEntity.setProfileModifiedDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    public void setDiscountsModifiedDate(AgreementEntity agreementEntity) {
        agreementEntity.setDiscountsModifiedDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    public void setDocumentsModifiedDate(AgreementEntity agreementEntity) {
        agreementEntity.setDocumentsModifiedDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    private AgreementEntity createAgreement(String agreementId) {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId(agreementId);
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        return agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementService(AgreementRepository agreementRepository, AgreementUserService userService) {
        this.agreementRepository = agreementRepository;
        this.userService = userService;
    }

}

