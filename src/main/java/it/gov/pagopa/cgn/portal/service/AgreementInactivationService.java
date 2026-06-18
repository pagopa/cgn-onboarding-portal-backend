package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.MerchantRepository;
import it.gov.pagopa.cgn.portal.repository.OfflineMerchantRepository;
import it.gov.pagopa.cgn.portal.repository.OnlineMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AgreementInactivationService {

    private final AgreementRepository agreementRepository;
    private final MerchantRepository merchantRepository;
    private final OnlineMerchantRepository onlineMerchantRepository;
    private final OfflineMerchantRepository offlineMerchantRepository;

    public AgreementInactivationService(AgreementRepository agreementRepository,
                                        MerchantRepository merchantRepository,
                                        OnlineMerchantRepository onlineMerchantRepository,
                                        OfflineMerchantRepository offlineMerchantRepository) {
        this.agreementRepository = agreementRepository;
        this.merchantRepository = merchantRepository;
        this.onlineMerchantRepository = onlineMerchantRepository;
        this.offlineMerchantRepository = offlineMerchantRepository;
    }

    @Transactional
    public int inactivateStaleAgreements() {
        return inactivateStaleAgreements(LocalDate.now().minusMonths(6));
    }

    @Transactional
    public int inactivateStaleAgreements(LocalDate cutoff) {
        List<AgreementEntity> agreementsToInactivate = agreementRepository.findAgreementsToInactivate(
                cutoff,
                AgreementStateEnum.APPROVED,
                AgreementStateEnum.ACTIVE,
                DiscountStateEnum.PUBLISHED);

        if (CollectionUtils.isEmpty(agreementsToInactivate)) {
            log.info("No stale agreements found with cutoff [{}]", cutoff);
            return 0;
        }

        LocalDate updateDate = LocalDate.now();
        agreementsToInactivate.forEach(agreement -> {
            agreement.setState(AgreementStateEnum.INACTIVE);
            agreement.setInformationLastUpdateDate(updateDate);
        });

        agreementRepository.saveAllAndFlush(agreementsToInactivate);
        refreshMerchantMaterializedViews();

        log.info("Inactivated [{}] stale agreements with cutoff [{}]", agreementsToInactivate.size(), cutoff);
        return agreementsToInactivate.size();
    }

    private void refreshMerchantMaterializedViews() {
        merchantRepository.refreshView();
        onlineMerchantRepository.refreshView();
        offlineMerchantRepository.refreshView();
    }
}
