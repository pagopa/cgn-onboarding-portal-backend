package it.gov.pagopa.service;

import it.gov.pagopa.BaseTest;
import it.gov.pagopa.enums.AgreementStateEnum;
import it.gov.pagopa.model.AgreementEntity;
import it.gov.pagopa.model.AgreementUserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementServiceTest extends BaseTest {
    @Autowired
    private AgreementService agreementService;

    @BeforeEach
    void beforeEach() {
        agreementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void Create_CreateAgreementWithInitializedData_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        Assertions.assertNotNull(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getProfileModifiedDate());
        Assertions.assertNull(agreementEntity.getDiscountsModifiedDate());
        Assertions.assertNull(agreementEntity.getDocumentsModifiedDate());
    }

    @Test
    void Create_CreatedAgreementWithValidId_Ok() {
        AgreementEntity agreementEntity = this.agreementService.createAgreementIfNotExists();
        Optional<AgreementUserEntity> userEntityOptional;
        userEntityOptional = this.userRepository.findAll().stream()
                .filter((user) -> user.getSubscriptionId().equals(agreementEntity.getId())).findFirst();
        Assertions.assertTrue(userEntityOptional.isPresent());
    }

    @Test
    void Create_CreateMultipleAgreement_CreatedOnlyOneAgreement() {
        AgreementEntity userCreated1 = this.agreementService.createAgreementIfNotExists();
        AgreementEntity userCreated2 = this.agreementService.createAgreementIfNotExists();
        Assertions.assertEquals(userCreated1, userCreated2);
    }
}
