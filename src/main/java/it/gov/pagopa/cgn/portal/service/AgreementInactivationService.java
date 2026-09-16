package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.repository.AgreementRepository;
import it.gov.pagopa.cgn.portal.repository.MerchantRepository;
import it.gov.pagopa.cgn.portal.repository.OfflineMerchantRepository;
import it.gov.pagopa.cgn.portal.repository.OnlineMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AgreementInactivationService {

    private final AgreementRepository agreementRepository;
    private final ChangeAuditService changeAuditService;
    private final MerchantRepository merchantRepository;
    private final OnlineMerchantRepository onlineMerchantRepository;
    private final OfflineMerchantRepository offlineMerchantRepository;

    public AgreementInactivationService(AgreementRepository agreementRepository,
                                        ChangeAuditService changeAuditService,
                                        MerchantRepository merchantRepository,
                                        OnlineMerchantRepository onlineMerchantRepository,
                                        OfflineMerchantRepository offlineMerchantRepository) {
        this.agreementRepository = agreementRepository;
        this.changeAuditService = changeAuditService;
        this.merchantRepository = merchantRepository;
        this.onlineMerchantRepository = onlineMerchantRepository;
        this.offlineMerchantRepository = offlineMerchantRepository;
    }

    @Transactional
    public int inactivateStaleAgreements(LocalDate currentDate,
                                         int expiredAgreementStaleMonths) {
        LocalDate expiredAgreementCutoff = currentDate.minusMonths(expiredAgreementStaleMonths);

        List<AgreementEntity> activeAgreementsToExpire = agreementRepository.findActiveAgreementsToExpire(
            currentDate);
        List<AgreementEntity> expiredAgreementsToInactivate = agreementRepository.findExpiredAgreementsWithoutValidDiscounts(
            currentDate)
                                             .stream()
                                             .filter(agreement -> changeAuditService.findAgreementStateSince(
                                                     agreement.getId(),
                                                     AgreementStateEnum.EXPIRED)
                                                 .map(stateSince -> !stateSince.toLocalDate()
                                                              .isAfter(
                                                                  expiredAgreementCutoff))
                                                 .orElse(false))
                                             .toList();

        if (activeAgreementsToExpire.isEmpty() &&
            expiredAgreementsToInactivate.isEmpty()) {
            log.info("No stale agreements found with current date [{}] and expired cutoff [{}]",
                     currentDate,
                     expiredAgreementCutoff);
            return 0;
        }

        activeAgreementsToExpire.forEach(agreement -> {
            agreement.setState(AgreementStateEnum.EXPIRED);
            agreement.setInformationLastUpdateDate(currentDate);
        });
        expiredAgreementsToInactivate.forEach(agreement -> {
            agreement.setState(AgreementStateEnum.INACTIVE);
            agreement.setInformationLastUpdateDate(currentDate);
        });

        List<AgreementEntity> agreementsToUpdate = new ArrayList<>();
        agreementsToUpdate.addAll(activeAgreementsToExpire);
        agreementsToUpdate.addAll(expiredAgreementsToInactivate);

        agreementRepository.saveAllAndFlush(agreementsToUpdate);
        refreshMerchantMaterializedViews();

        log.info("Processed [{}] stale agreements with current date [{}]: expired [{}] active agreements, inactivated [{}] expired agreements with cutoff [{}]",
                 agreementsToUpdate.size(),
                 currentDate,
                 activeAgreementsToExpire.size(),
                 expiredAgreementsToInactivate.size(),
                 expiredAgreementCutoff);
        return agreementsToUpdate.size();
    }

    private void refreshMerchantMaterializedViews() {
        merchantRepository.refreshView();
        onlineMerchantRepository.refreshView();
        offlineMerchantRepository.refreshView();
    }
}
