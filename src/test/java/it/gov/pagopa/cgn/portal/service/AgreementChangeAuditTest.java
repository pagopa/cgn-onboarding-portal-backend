package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditOperationTypeEnum;
import it.gov.pagopa.cgn.portal.enums.ChangeAuditSubjectTypeEnum;
import it.gov.pagopa.cgn.portal.model.AgreementEntity;
import it.gov.pagopa.cgn.portal.model.ChangeAuditEntity;
import it.gov.pagopa.cgn.portal.model.ProfileEntity;
import it.gov.pagopa.cgnonboardingportal.backoffice.model.EntityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementChangeAuditTest
        extends IntegrationAbstractTest {

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

    private List<ChangeAuditEntity> findAudits() {
        return changeAuditRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}