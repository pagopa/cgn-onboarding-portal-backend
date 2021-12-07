package it.gov.pagopa.cgn.portal.service;

import it.gov.pagopa.cgn.portal.IntegrationAbstractTest;
import it.gov.pagopa.cgn.portal.TestUtils;
import it.gov.pagopa.cgn.portal.enums.AgreementStateEnum;
import it.gov.pagopa.cgn.portal.enums.DocumentTypeEnum;
import it.gov.pagopa.cgn.portal.exception.InvalidRequestException;
import it.gov.pagopa.cgn.portal.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"dev"})
class AgreementServiceLightTest extends IntegrationAbstractTest {

    @Test
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void getOne_Ok() {
        AgreementEntity agreementEntity = agreementService.createAgreementIfNotExists(TestUtils.FAKE_ID);
        Assertions.assertNotNull(agreementEntity.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, agreementEntity.getState());
        Assertions.assertNull(agreementEntity.getStartDate());
        Assertions.assertNull(agreementEntity.getEndDate());
        Assertions.assertNull(agreementEntity.getImageUrl());
        Assertions.assertNull(agreementEntity.getRejectReasonMessage());

        var found = agreementServiceLight.getOne(agreementEntity.getId());
        Assertions.assertNotNull(found.getId());
        Assertions.assertEquals(AgreementStateEnum.DRAFT, found.getState());
        Assertions.assertNull(found.getStartDate());
        Assertions.assertNull(found.getEndDate());
        Assertions.assertNull(found.getImageUrl());
        Assertions.assertNull(found.getRejectReasonMessage());
    }

    @Test
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void getOne_Ko() {
        var notFound = agreementServiceLight.getOne("not_exist");
        Assertions.assertNull(notFound);
    }
}
