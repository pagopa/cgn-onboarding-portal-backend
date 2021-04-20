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

    public AgreementEntity findById(String subscriptionId) {
        return agreementRepository.findById(subscriptionId)
                .orElseThrow(() -> new InvalidRequestException("Agreement not found"));
    }

    public AgreementEntity createAgreementIfNotExists() {
        AgreementEntity agreementEntity;
        AgreementUserEntity userSubscription;
        Optional<AgreementUserEntity> userAgreementOpt = userService.findCurrentAgreementUser();
        if (userAgreementOpt.isPresent()) {
            userSubscription = userAgreementOpt.get();
            // current user has already an agreement. Find it
            agreementEntity = agreementRepository.findById(userSubscription.getSubscriptionId())
                    .orElseThrow(() -> new RuntimeException("User " + userSubscription.getUserId() + " doesn't have an agreement"));
        } else {
            userSubscription = userService.create();
            agreementEntity = createSubscription(userSubscription.getSubscriptionId());
        }
        return agreementEntity;
    }

    public void setRegistryDoneModifiedDate(AgreementEntity agreementEntity) {
        agreementEntity.setProfileModifiedDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    public void setDiscountsModifiedDate(AgreementEntity subscription) {
        subscription.setDiscountsModifiedDate(LocalDate.now());
        agreementRepository.save(subscription);
    }

    public void setDocumentsModifiedDate(AgreementEntity agreementEntity) {
        agreementEntity.setDocumentsModifiedDate(LocalDate.now());
        agreementRepository.save(agreementEntity);
    }

    private AgreementEntity createSubscription(String agreementId) {
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

