package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.enums.DiscountStateEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import it.gov.pagopa.cgn.portal.model.DiscountEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementChangeAuditTest
        extends IntegrationAbstractTest {

    @Autowired
    private ChangeAuditService changeAuditService;

    @Test
    void CreateAgreement_ShouldAuditInsertWithOrganizationNameFallback() {
        setOperatorAuth();

        AgreementEntity agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                      EntityType.PRIVATE,
                                                                                      TestUtils.FAKE_ORGANIZATION_NAME);

        List<ChangeAuditEntity> audits = findAudits();
        Assertions.assertEquals(1, audits.size());

        ChangeAuditEntity audit = audits.get(0);
        Assertions.assertEquals(agreementEntity.getId(), audit.getSubjectId());
        Assertions.assertEquals(ChangeAuditSubjectTypeEnum.AGREEMENT, audit.getSubjectType());
        Assertions.assertEquals(ChangeAuditOperationTypeEnum.INSERT, audit.getOperationType());
        Assertions.assertEquals(TestUtils.FAKE_ID, audit.getActorRef());
        Assertions.assertEquals(TestUtils.FAKE_ORGANIZATION_NAME, audit.getPartnerFullName());
        Assertions.assertNotNull(audit.getInsertTime());

        Map<String, Object> value = audit.getValue();
        Assertions.assertEquals(agreementEntity.getId(), value.get("agreement_k"));
        Assertions.assertEquals("DRAFT", value.get("state"));
        Assertions.assertEquals(TestUtils.FAKE_ORGANIZATION_NAME, value.get("organization_name"));
        Assertions.assertNotNull(value.get("insert_time"));
    }

    @Test
    void UpdateAgreement_ShouldAuditUpdateWithProfileFullNameAndAdminActor() {
        setOperatorAuth();
        AgreementEntity agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                      EntityType.PRIVATE,
                                                                                      TestUtils.FAKE_ORGANIZATION_NAME);

        ProfileEntity profileEntity = TestUtils.createSampleProfileEntity(agreementEntity);
        profileService.createProfile(profileEntity, agreementEntity.getId());

        setAdminAuth();
        agreementEntity.setRejectReasonMessage("UPDATED_REASON");
        agreementRepository.save(agreementEntity);

        List<ChangeAuditEntity> audits = findAudits();
        Assertions.assertEquals(2, audits.size());

        ChangeAuditEntity audit = audits.get(1);
        Assertions.assertEquals(ChangeAuditSubjectTypeEnum.AGREEMENT, audit.getSubjectType());
        Assertions.assertEquals(ChangeAuditOperationTypeEnum.UPDATE, audit.getOperationType());
        Assertions.assertEquals(TestUtils.FAKE_ID, audit.getActorRef());
        Assertions.assertEquals(profileEntity.getFullName(), audit.getPartnerFullName());
        Assertions.assertEquals("UPDATED_REASON", audit.getValue().get("reject_reason_msg"));
    }

    @Test
    void DeleteAgreement_ShouldAuditDelete() {
        setAdminAuth();
        AgreementEntity agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID,
                                                                                      EntityType.PRIVATE,
                                                                                      TestUtils.FAKE_ORGANIZATION_NAME);

        agreementRepository.delete(agreementEntity);

        List<ChangeAuditEntity> audits = findAudits();
        Assertions.assertEquals(2, audits.size());

        ChangeAuditEntity audit = audits.get(1);
        Assertions.assertEquals(ChangeAuditSubjectTypeEnum.AGREEMENT, audit.getSubjectType());
        Assertions.assertEquals(ChangeAuditOperationTypeEnum.DELETE, audit.getOperationType());
        Assertions.assertEquals(TestUtils.FAKE_ID, audit.getActorRef());
        Assertions.assertEquals(TestUtils.FAKE_ORGANIZATION_NAME, audit.getPartnerFullName());
        Assertions.assertEquals(agreementEntity.getId(), audit.getValue().get("agreement_k"));
    }

    @Test
    void FindAgreementStateSince_ShouldReturnStartOfMostRecentCurrentStateBlock() {
        setAdminAuth();
        AgreementTestObject agreementTestObject = createApprovedAgreement();
        AgreementEntity agreementEntity = agreementTestObject.getAgreementEntity();
        DiscountEntity discountEntity = agreementTestObject.getDiscountEntityList().get(0);

        discountEntity.setState(DiscountStateEnum.TEST_PASSED);
        discountEntity = discountRepository.save(discountEntity);

        discountService.publishDiscount(agreementEntity.getId(), discountEntity.getId());
        agreementEntity = agreementRepository.findById(agreementEntity.getId()).orElseThrow();

        OffsetDateTime firstHistoricalActiveBlockStart = findFirstAuditInsertTimeForState(agreementEntity.getId(),
                                                                                          AgreementStateEnum.ACTIVE);

        agreementEntity.setInformationLastUpdateDate(LocalDate.now().plusDays(1));
        agreementEntity = agreementRepository.save(agreementEntity);

        agreementEntity.setState(AgreementStateEnum.INACTIVE);
        agreementEntity.setInformationLastUpdateDate(LocalDate.now().plusDays(2));
        agreementEntity = agreementRepository.save(agreementEntity);

        agreementEntity.setState(AgreementStateEnum.ACTIVE);
        agreementEntity.setInformationLastUpdateDate(LocalDate.now().plusDays(3));
        agreementEntity = agreementRepository.save(agreementEntity);

        OffsetDateTime currentActiveBlockStart = findLastAuditByAgreementId(agreementEntity.getId()).getInsertTime();

        agreementEntity.setInformationLastUpdateDate(LocalDate.now().plusDays(4));
        agreementEntity = agreementRepository.save(agreementEntity);

        Optional<OffsetDateTime> agreementStateSince = changeAuditService.findAgreementStateSince(agreementEntity.getId(),
                                                                                                  agreementEntity.getState());

        Assertions.assertTrue(agreementStateSince.isPresent());
        Assertions.assertEquals(currentActiveBlockStart, agreementStateSince.get());
        Assertions.assertNotEquals(firstHistoricalActiveBlockStart, agreementStateSince.get());
    }

    @Test
    void FindAgreementStateSince_ShouldReturnEmptyWhenAuditHistoryIsMissing() {
        setAdminAuth();
        AgreementEntity agreementEntity = createApprovedAgreement().getAgreementEntity();

        changeAuditRepository.deleteAll();
        changeAuditRepository.flush();

        Optional<OffsetDateTime> agreementStateSince = changeAuditService.findAgreementStateSince(agreementEntity.getId(),
                                                                                                  agreementEntity.getState());

        Assertions.assertTrue(agreementStateSince.isEmpty());
    }

    private List<ChangeAuditEntity> findAudits() {
        return changeAuditRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private ChangeAuditEntity findLastAuditByAgreementId(String agreementId) {
        return findAudits().stream()
                           .filter(audit -> agreementId.equals(audit.getSubjectId()))
                           .reduce((first, second) -> second)
                           .orElseThrow();
    }

    private OffsetDateTime findFirstAuditInsertTimeForState(String agreementId, AgreementStateEnum state) {
        return findAudits().stream()
                           .filter(audit -> agreementId.equals(audit.getSubjectId()))
                           .filter(audit -> state.name().equals(audit.getValue().get("state")))
                           .map(ChangeAuditEntity::getInsertTime)
                           .findFirst()
                           .orElseThrow();
    }
}