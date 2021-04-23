package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.AgreementUserEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
public class AgreementService extends AgreementServiceLight {


    private final AgreementUserService userService;

    private final ProfileService profileService;

    private final DiscountService discountService;


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

    public AgreementEntity requestApproval(String agreementId) {
        AgreementEntity agreementEntity = findById(agreementId);

        profileService.getProfile(agreementId)
                .orElseThrow(() -> new InvalidRequestException("Profile not found. Agreement not approvable"));
        List<DiscountEntity> discounts = discountService.getDiscounts(agreementId);
        if (CollectionUtils.isEmpty(discounts)) {
            throw new InvalidRequestException("Discounts not found. Agreement not approvable");
        }
        //TODO check documents

        agreementEntity.setState(AgreementStateEnum.PENDING);
        return agreementRepository.save(agreementEntity);
    }

    private AgreementEntity createAgreement(String agreementId) {
        AgreementEntity agreementEntity = new AgreementEntity();
        agreementEntity.setId(agreementId);
        agreementEntity.setState(AgreementStateEnum.DRAFT);
        return agreementRepository.save(agreementEntity);
    }

    @Autowired
    public AgreementService(AgreementRepository agreementRepository, AgreementUserService userService,
                            ProfileService profileService, DiscountService discountService) {
        super(agreementRepository);
        this.userService = userService;
        this.profileService = profileService;
        this.discountService = discountService;
    }

}

